package SwedBank;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import CommonUtility.BusinessFunctions;
import CommonUtility.BusinessFunctions.ccyPair;
import CommonUtility.ReadPropertyFile;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class restApiTest 
{
	private static String indcativeRateCcyPairResponse ="";
	private static String indcativeSingleRateCcyPairResponse ="";
	private static String baseUri;
	private static String appId;
	private static String indicativeRateEndPoint;
	private static String indicativeRateSingleCcyRateEndPoint;
	private static String headerName;
	private static List<String> exchangeRateList = new LinkedList<String>();
	private static String mktOrder;
	
	//initilze the global variables through use of contructor
	public restApiTest() throws IOException
	{
		restApiTest.baseUri =	BusinessFunctions.getBaseURIForEndPoint();
		restApiTest.appId = BusinessFunctions.getAppId();
		restApiTest.indicativeRateEndPoint = ReadPropertyFile.readPropFileAndReturnPropertyValue(BusinessFunctions.INDICATIVERATECCYPAIRENDPOINT);
		restApiTest.headerName = ReadPropertyFile.readPropFileAndReturnPropertyValue(BusinessFunctions.HEADERNAMEFORCCYPAIR);	
		restApiTest.indicativeRateSingleCcyRateEndPoint = ReadPropertyFile.readPropFileAndReturnPropertyValue(BusinessFunctions.INDICATIVERATECCYPAIRVALUEENDPOINT);
		restApiTest.mktOrder = ReadPropertyFile.readPropFileAndReturnPropertyValue(BusinessFunctions.MKTORDERAPI);
	}
	
	
	
	@Test(enabled=true,priority=0)	
	public static void getIndecativeRateCcyPairList() throws IOException
	{
		//Get the restbase base URL
		RestAssured.baseURI= restApiTest.baseUri;
		
		//format end point with app id
		String indicativeRateEndPointFinal = String.format(restApiTest.indicativeRateEndPoint, restApiTest.appId);
		
		//Get header name and generate unique value for that
		String uniqueRequestId = ""+ BusinessFunctions.getMilliSeconds();
		
		//make the rest call
		Response response = RestAssured.given()
							.header(restApiTest.headerName, uniqueRequestId)
			                .log()
			                .all()
			                .when()
			                .get(indicativeRateEndPointFinal);//.then().assertThat().statusCode(200);
		
		Assert.assertEquals(response.getStatusCode(),200);
		System.out.println(response.asString());	
		indcativeRateCcyPairResponse = response.asString();
				               
	}
	
	@Test(enabled=true,priority=1)
	public static void validateIndicativeCcyPairResponse()
	{
		//using string as is
		for(BusinessFunctions.ccyPair pair : BusinessFunctions.ccyPair.values())
		{
			if(indcativeRateCcyPairResponse.contains(pair.toString()))
			{
				System.out.println("The CCY Pair "+pair.toString()+" exists!!");
			}
			else
			{
				System.out.println("The CCY Pair "+pair.toString()+" does not exists!!");

			}
			
		}
		
		
		//using list
		//indecativeRateCcyPairResponse=indecativeRateCcyPairResponse.replace("[", "");
		//indecativeRateCcyPairResponse=indecativeRateCcyPairResponse.replace("]", "");
		indcativeRateCcyPairResponse=indcativeRateCcyPairResponse.replaceAll("\",\"", ",");
		indcativeRateCcyPairResponse=indcativeRateCcyPairResponse.replaceAll("\"", "");

		//System.out.println("Web service response list:"+indecativeRateCcyPairResponse);

		
		List<String> myList = new ArrayList<String>(Arrays.asList(indcativeRateCcyPairResponse.split(",")));
		//ListIterator<String> it = myList.listIterator();
		List<ccyPair> enumValues = Arrays.asList(BusinessFunctions.ccyPair.values());
		System.out.println("Web service response list:"+indcativeRateCcyPairResponse);
		System.out.println("Enum list:"+enumValues);


		
		if(myList.equals(enumValues))
		{
			System.out.println("Expected and Actual Response matches");
		}
		else
		{
			System.out.println("not match");
		}
		
		
				
	}
	
	//data provider
	@DataProvider(name = "data-provider")
    public Iterator<String> dataProviderMethod() {
		
		
		java.util.LinkedList<String> itemList = new LinkedList<String>();
	    for (BusinessFunctions.ccyPair s : BusinessFunctions.ccyPair.values()) {
	    	itemList.add(s.name());
	    }
	   //Object[] arrayString =  itemList.toArray();
	    Iterator<String> it =  itemList.listIterator();
	   
        return it;
    }
	
	//get the rate for each ccy pair and display on scree
	@Test(enabled=true,priority=2,dataProvider = "data-provider")
	public static void getIndividualExchangeRateForGivenCCY(String ccyPair) throws IOException, ParseException
	{
		//Get the restbase base URL
				RestAssured.baseURI= restApiTest.baseUri;
				//format end point with app id
				String indicativeSingleCCYRateEndPointFinal = String.format(restApiTest.indicativeRateSingleCcyRateEndPoint, ccyPair,restApiTest.appId);
				
				//Get header name and generate unique value for that
				String uniqueRequestId = ""+ BusinessFunctions.getMilliSeconds();
				
				//make the rest call
				Response response = RestAssured.given()
									.header(restApiTest.headerName, uniqueRequestId)
					                .log()
					                .all()
					                .when()
					                .get(indicativeSingleCCYRateEndPointFinal);//.then().assertThat().statusCode(200);
				
				//Assert.assertEquals(response.getStatusCode(),200);
				//System.out.println(response.asString());	
				indcativeSingleRateCcyPairResponse = response.asString();
				System.out.println(indcativeSingleRateCcyPairResponse);	
				//add the results for later parsing and adding to excel
				exchangeRateList.add(indcativeSingleRateCcyPairResponse);
				//JSON parser object to parse read file
		         JSONParser jsonParser = new JSONParser();	
		        JSONObject jObject = (JSONObject) jsonParser.parse(indcativeSingleRateCcyPairResponse);
		       
		        //verify if its string
		        if(BusinessFunctions.verifyIfString(jObject.get("currencyPair").toString()))
		        {
		        	System.out.println("Valid Format of CCY pair");
		        }
		        
		        Double pt = (Double) jObject.get("midRate");
		        if(BusinessFunctions.verifyIfDouble(pt))
		        {
		        	System.out.println("Valid Format of Exchange rate");
		        }
		        
		        
		        System.out.println("CurrecnyPair:"+jObject.get("currencyPair"));
		        System.out.println("MidRate:"+jObject.get("midRate"));
		        System.out.println("TimeStamp:"+jObject.get("rateTimestamp"));
		        try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		       				
	}
	
	@Test(enabled=true,priority=3)
	public static void writeToExcelRate() throws FileNotFoundException, ParseException
	{
		XSSFWorkbook workbook = new XSSFWorkbook();
		 
		XSSFSheet sheet = workbook.createSheet("ExchangeRate");
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 6000);
		sheet.setColumnWidth(2, 6000);

		 
		XSSFRow header = sheet.createRow(0);
		 
		XSSFCellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		 
		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
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
		
		ListIterator<String> itr = exchangeRateList.listIterator();
		int i =1;
		
		while(itr.hasNext())
		{
		
		JSONParser jsonParser = new JSONParser();	
		JSONObject jObject = (JSONObject) jsonParser.parse(itr.next());
		        System.out.println("CurrecnyPair:"+jObject.get("currencyPair"));
		        System.out.println("MidRate:"+jObject.get("midRate"));
		        System.out.println("TimeStamp:"+jObject.get("rateTimestamp"));
		XSSFRow row = sheet.createRow(i);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue(jObject.get("currencyPair").toString());
		cell.setCellStyle(style);
		 
		cell = row.createCell(1);
		cell.setCellValue(jObject.get("midRate").toString());
		cell.setCellStyle(style);
		
		cell = row.createCell(2);
		cell.setCellValue(jObject.get("rateTimestamp").toString());
		cell.setCellStyle(style);
		i++;
		}
		
		//File currDir = new File(".");
		//String path = currDir.getAbsolutePath();
		String fileName = BusinessFunctions.returnUniqueFileName();
		String fileLocation = "./Output/"+fileName;
		 
		FileOutputStream outputStream = new FileOutputStream(fileLocation);
		try {
			workbook.write(outputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			workbook.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//market order API
	@Test(enabled=true,priority=4)
	public void marketOrdersTest()
	{
		//Get the restbase base URL
		RestAssured.baseURI= restApiTest.baseUri;
	
		//Get header name and generate unique value for that
		String uniqueRequestId = ""+ BusinessFunctions.getMilliSeconds();
		String mktIdApiUrl = String.format(restApiTest.mktOrder, BusinessFunctions.getDateInISO8601(),restApiTest.appId);
		
		//make the rest call
		Response response = RestAssured.given()
							.header(restApiTest.headerName, uniqueRequestId)
			                .log()
			                .all()
			                .when()
			                .get(mktIdApiUrl);//.then().assertThat().statusCode(200);
		
		System.out.println(response.asString());
		
	}
	
	
}
