package SwedBank;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import CommonUtility.BusinessFunctions;
import CommonUtility.ReadPropertyFile;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class restApiTest 
{
	private static String indcativeRateCcyPairResponse ="";
	private static String baseUri;
	private static String appId;
	private static String indicativeRateEndPoint;
	private static String indicativeRateSingleCcyRateEndPoint;
	private static String headerName;
	private static long requestDelayMs;
	//only rates that passed validation, ready to be written to excel
	private static List<JSONObject> exchangeRateList = new LinkedList<JSONObject>();
	private static String mktOrder;
	
	//initilze the global variables before any test runs (a missing app key shows as a setup failure)
	@BeforeClass(alwaysRun=true)
	public void setUp() throws IOException
	{
		restApiTest.baseUri =	BusinessFunctions.getBaseURIForEndPoint();
		restApiTest.appId = BusinessFunctions.getAppId();
		restApiTest.indicativeRateEndPoint = ReadPropertyFile.readPropFileAndReturnPropertyValue(BusinessFunctions.INDICATIVERATECCYPAIRENDPOINT);
		restApiTest.headerName = ReadPropertyFile.readPropFileAndReturnPropertyValue(BusinessFunctions.HEADERNAMEFORCCYPAIR);	
		restApiTest.indicativeRateSingleCcyRateEndPoint = ReadPropertyFile.readPropFileAndReturnPropertyValue(BusinessFunctions.INDICATIVERATECCYPAIRVALUEENDPOINT);
		restApiTest.mktOrder = ReadPropertyFile.readPropFileAndReturnPropertyValue(BusinessFunctions.MKTORDERAPI);
		String delay = ReadPropertyFile.readPropFileAndReturnPropertyValue(BusinessFunctions.REQUESTDELAYMS);
		restApiTest.requestDelayMs = delay == null ? 0 : Long.parseLong(delay.trim());
	}
	
	
	
	@Test(enabled=true,priority=0,groups="FXRates")	
	public static void getIndecativeRateCcyPairList() throws IOException
	{
		//Get the restbase base URL
		RestAssured.baseURI= restApiTest.baseUri;
		
		//format end point with app id
		String indicativeRateEndPointFinal = String.format(restApiTest.indicativeRateEndPoint, restApiTest.appId);
		
		//make the rest call
		Response response = RestAssured.given()
							.header(restApiTest.headerName, BusinessFunctions.getRequestId())
			                .log()
			                .all()
			                .when()
			                .get(indicativeRateEndPointFinal);
		
		Assert.assertEquals(response.getStatusCode(),200, "Unexpected status, body: "+response.asString());
		System.out.println(response.asString());	
		indcativeRateCcyPairResponse = response.asString();
				               
	}
	
	@Test(enabled=true,priority=1,groups="FXRates",dependsOnMethods="getIndecativeRateCcyPairList")
	public static void validateIndicativeCcyPairResponse() throws ParseException
	{
		//parse the JSON array instead of string matching, so "EURSEKX" can't match "EURSEK"
		JSONArray pairs = (JSONArray) new JSONParser().parse(indcativeRateCcyPairResponse);
		Set<String> actualPairs = new LinkedHashSet<String>();
		for(Object pair : pairs)
		{
			actualPairs.add(String.valueOf(pair));
		}
		
		Set<String> missingPairs = new LinkedHashSet<String>();
		for(BusinessFunctions.ccyPair pair : BusinessFunctions.ccyPair.values())
		{
			if(!actualPairs.remove(pair.name()))
			{
				missingPairs.add(pair.name());
			}
		}
		
		//pairs left over are new on the API side; report them but don't fail
		if(!actualPairs.isEmpty())
		{
			System.out.println("CCY Pairs returned by the API but not in the enum: "+actualPairs);
		}
		Assert.assertTrue(missingPairs.isEmpty(), "Expected CCY Pairs missing from the API response: "+missingPairs);
	}
	
	//data provider
	@DataProvider(name = "data-provider")
    public Iterator<String> dataProviderMethod() {
		
		
		java.util.LinkedList<String> itemList = new LinkedList<String>();
	    for (BusinessFunctions.ccyPair s : BusinessFunctions.ccyPair.values()) {
	    	itemList.add(s.name());
	    }
	    Iterator<String> it =  itemList.listIterator();
	   
        return it;
    }
	
	//get the rate for each ccy pair and display on scree
	@Test(enabled=true,priority=2,dataProvider = "data-provider",groups="FXRates")
	public static void getIndividualExchangeRateForGivenCCY(String ccyPair) throws IOException, ParseException, InterruptedException
	{
		//Get the restbase base URL
				RestAssured.baseURI= restApiTest.baseUri;
				//format end point with app id
				String indicativeSingleCCYRateEndPointFinal = String.format(restApiTest.indicativeRateSingleCcyRateEndPoint, ccyPair,restApiTest.appId);
				
				//make the rest call
				Response response = RestAssured.given()
									.header(restApiTest.headerName, BusinessFunctions.getRequestId())
					                .log()
					                .all()
					                .when()
					                .get(indicativeSingleCCYRateEndPointFinal);
				
				//throttle before asserting, so a failure doesn't make the next call hit the rate limit
				Thread.sleep(restApiTest.requestDelayMs);
				
				String body = response.asString();
				System.out.println(body);	
				Assert.assertEquals(response.getStatusCode(),200, "Unexpected status for "+ccyPair+", body: "+body);
				
				//JSON parser object to parse read file
		        JSONObject jObject = (JSONObject) new JSONParser().parse(body);
		       
		        Assert.assertEquals(jObject.get("currencyPair"), ccyPair, "Wrong or missing currencyPair");
		        
		        //json-simple returns Long for whole numbers and Double otherwise, so check for Number
		        Object midRate = jObject.get("midRate");
		        Assert.assertTrue(midRate instanceof Number, "midRate is not a number: "+midRate);
		        Assert.assertTrue(((Number) midRate).doubleValue() > 0, "midRate is not positive: "+midRate);
		        
		        Assert.assertNotNull(jObject.get("rateTimestamp"), "Missing rateTimestamp");
		        
		        System.out.println("CurrecnyPair:"+jObject.get("currencyPair"));
		        System.out.println("MidRate:"+midRate);
		        System.out.println("TimeStamp:"+jObject.get("rateTimestamp"));
		        
		        //add the validated result for writing to excel
		        exchangeRateList.add(jObject);
	}
	
	//alwaysRun so the rates that did succeed are still written when some pairs fail
	@Test(enabled=true,priority=3,groups="FXRates",dependsOnMethods="getIndividualExchangeRateForGivenCCY",alwaysRun=true)
	public static void writeToExcelRate() throws IOException
	{
		if(exchangeRateList.isEmpty())
		{
			throw new SkipException("No valid exchange rates to write");
		}
		
		try(XSSFWorkbook workbook = new XSSFWorkbook())
		{
		XSSFSheet sheet = workbook.createSheet("ExchangeRate");
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 6000);
		sheet.setColumnWidth(2, 6000);

		 
		XSSFRow header = sheet.createRow(0);
		 
		XSSFCellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		 
		XSSFFont font = workbook.createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 16);
		font.setBold(true);
		headerStyle.setFont(font);
		 
		XSSFCell headerCell = header.createCell(0);
		headerCell.setCellValue("CurrencyPair");
		headerCell.setCellStyle(headerStyle);
		 
		headerCell = header.createCell(1);
		headerCell.setCellValue("ExchangeRate");
		headerCell.setCellStyle(headerStyle);
		
		headerCell = header.createCell(2);
		headerCell.setCellValue("TimeStamp");
		headerCell.setCellStyle(headerStyle);
		
		//get the list for exchange rate list and write to excel
		
		XSSFCellStyle style = workbook.createCellStyle();
		style.setWrapText(true);
		
		int i =1;
		
		for(JSONObject jObject : exchangeRateList)
		{
		XSSFRow row = sheet.createRow(i);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue(jObject.get("currencyPair").toString());
		cell.setCellStyle(style);
		 
		//write the rate as a number so excel can sort and calculate with it
		cell = row.createCell(1);
		cell.setCellValue(((Number) jObject.get("midRate")).doubleValue());
		cell.setCellStyle(style);
		
		cell = row.createCell(2);
		cell.setCellValue(jObject.get("rateTimestamp").toString());
		cell.setCellStyle(style);
		i++;
		}
		
		Files.createDirectories(Paths.get("./Output"));
		String fileLocation = "./Output/"+BusinessFunctions.returnUniqueFileName();
		 
		try(FileOutputStream outputStream = new FileOutputStream(fileLocation))
		{
			workbook.write(outputStream);
		}
		System.out.println("Wrote "+exchangeRateList.size()+" rates to "+fileLocation);
		}
	}
	
	//market order API
	@Test(enabled=true,priority=4,groups="MarketOrder",invocationCount=10)
	public void marketOrdersTest() throws ParseException
	{
		//Get the restbase base URL
		RestAssured.baseURI= restApiTest.baseUri;
	
		String mktIdApiUrl = String.format(restApiTest.mktOrder, BusinessFunctions.getDateInISO8601(),restApiTest.appId);
		
		//make the rest call
		Response response = RestAssured.given()
							.header(restApiTest.headerName, BusinessFunctions.getRequestId())
			                .log()
			                .all()
			                .when()
			                .get(mktIdApiUrl);
		
		String body = response.asString();
		System.out.println(body);
		Assert.assertEquals(response.getStatusCode(),200, "Unexpected status, body: "+body);
		//fails if the body isn't valid JSON
		new JSONParser().parse(body);
		
	}
	
	
}
