package SwedBank;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.testng.asserts.SoftAssert;
import CommonUtility.BusinessFunctions;
import CommonUtility.ReadPropertyFile;
import CommonUtility.ResponseChecks;
import io.qameta.allure.Allure;
import io.qameta.allure.restassured.AllureRestAssured;
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
	private static final AtomicInteger marketOrderCall = new AtomicInteger();
	//"2026-10-03T16:23 CEST": the sandbox's rateTimestamp format
	private static final DateTimeFormatter RATE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm z", Locale.ENGLISH);
	//real spreads are about 0.2-0.4% of the mid rate; more than 5% means a broken or swapped rate
	private static final double MAX_SPREAD = 0.05;
	//rates are live: a timestamp older than this means the sandbox stopped updating
	private static final Duration MAX_RATE_AGE = Duration.ofDays(3);
	//a pair and its inverse (SEKNOK, NOKSEK) multiply to about 1; allow for both spreads
	private static final double INVERSE_TOLERANCE = 0.01;
	private static final String[] EXCEL_COLUMNS = { "CurrencyPair", "BidRate", "AskRate", "MidRate", "TimeStamp" };
	
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
		//attach every request and response to its test in the Allure report
		RestAssured.replaceFiltersWith(new AllureRestAssured());
	}
	
	
	
	@Test(enabled=true,priority=0,groups="FXRates",description="Currency pair list returns 200 with JSON")	
	public static void getIndecativeRateCcyPairList() throws IOException
	{
		//Get the restbase base URL
		RestAssured.baseURI= restApiTest.baseUri;
		
		//format end point with app id
		String indicativeRateEndPointFinal = String.format(restApiTest.indicativeRateEndPoint, restApiTest.appId);
		
		//make the rest call
		String requestId = BusinessFunctions.getRequestId();
		Response response = RestAssured.given()
							.header(restApiTest.headerName, requestId)
			                .log()
			                .all()
			                .when()
			                .get(indicativeRateEndPointFinal);
		
		ResponseChecks.assertOk(response, requestId, "currency pair list");
		System.out.println(response.asString());	
		indcativeRateCcyPairResponse = response.asString();
				               
	}
	
	@Test(enabled=true,priority=1,groups="FXRates",dependsOnMethods="getIndecativeRateCcyPairList",description="Currency pair list is well formed and contains every expected pair")
	public static void validateIndicativeCcyPairResponse() throws ParseException
	{
		//parse the JSON array instead of string matching, so "EURSEKX" can't match "EURSEK"
		Object parsed = new JSONParser().parse(indcativeRateCcyPairResponse);
		Assert.assertTrue(parsed instanceof JSONArray, "Currency pair list is not a JSON array: "+indcativeRateCcyPairResponse);
		JSONArray pairs = (JSONArray) parsed;
		Assert.assertFalse(pairs.isEmpty(), "Currency pair list is empty");
		
		//check every entry, and report all bad ones together
		SoftAssert soft = new SoftAssert();
		Set<String> actualPairs = new LinkedHashSet<String>();
		for(Object pair : pairs)
		{
			String p = String.valueOf(pair);
			soft.assertTrue(pair instanceof String && p.matches("[A-Z]{6}"), "Not a 6-letter currency pair: "+pair);
			soft.assertTrue(p.length() != 6 || !p.substring(0, 3).equals(p.substring(3)), "Pair has the same currency twice: "+p);
			soft.assertTrue(actualPairs.add(p), "Duplicate currency pair: "+p);
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
		soft.assertTrue(missingPairs.isEmpty(), "Expected CCY Pairs missing from the API response: "+missingPairs);
		soft.assertAll();
	}
	
	//mid rate: halfway between the bid and ask rates (the API stopped returning midRate itself)
	private static double midRate(JSONObject rate)
	{
		return (((Number) rate.get("bidRate")).doubleValue() + ((Number) rate.get("askRate")).doubleValue()) / 2;
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
	@Test(enabled=true,priority=2,dataProvider = "data-provider",groups="FXRates",description="Indicative rate")
	public static void getIndividualExchangeRateForGivenCCY(String ccyPair) throws IOException, ParseException, InterruptedException
	{
		//name each report entry after its pair, so a failed pair is visible in the list
		Allure.getLifecycle().updateTest(t -> t.setName("Indicative rate: "+ccyPair));
		//Get the restbase base URL
				RestAssured.baseURI= restApiTest.baseUri;
				//format end point with app id
				String indicativeSingleCCYRateEndPointFinal = String.format(restApiTest.indicativeRateSingleCcyRateEndPoint, ccyPair,restApiTest.appId);
				
				//make the rest call
				String requestId = BusinessFunctions.getRequestId();
				Response response = RestAssured.given()
									.header(restApiTest.headerName, requestId)
					                .log()
					                .all()
					                .when()
					                .get(indicativeSingleCCYRateEndPointFinal);
				
				//throttle before asserting, so a failure doesn't make the next call hit the rate limit
				Thread.sleep(restApiTest.requestDelayMs);
				
				String body = response.asString();
				System.out.println(body);	
				ResponseChecks.assertOk(response, requestId, ccyPair);
				
				//JSON parser object to parse read file
		        Object parsed = new JSONParser().parse(body);
		        Assert.assertTrue(parsed instanceof JSONObject, ccyPair+": rate is not a JSON object: "+body);
		        JSONObject jObject = (JSONObject) parsed;
		        
		        //check every field, and report all problems with this rate together
		        SoftAssert soft = new SoftAssert();
		        soft.assertEquals(jObject.get("currencyPair"), ccyPair, "Wrong or missing currencyPair");
		        
		        //json-simple returns Long for whole numbers and Double otherwise, so check for Number
		        Object bid = jObject.get("bidRate");
		        Object ask = jObject.get("askRate");
		        boolean numbers = bid instanceof Number && ask instanceof Number;
		        soft.assertTrue(numbers, "bidRate and askRate must be numbers: bid="+bid+", ask="+ask);
		        if(numbers)
		        {
		        	double b = ((Number) bid).doubleValue();
		        	double a = ((Number) ask).doubleValue();
		        	soft.assertTrue(b > 0 && a > 0, "Rates must be positive: bid="+b+", ask="+a);
		        	soft.assertTrue(b <= a, "bidRate is above askRate: bid="+b+", ask="+a);
		        	double spread = (a - b) / ((a + b) / 2);
		        	soft.assertTrue(spread < MAX_SPREAD, String.format("Spread of %.2f%% is too wide: bid=%s, ask=%s", spread * 100, b, a));
		        }
		        
		        //the timestamp must be readable, recent and not in the future
		        Object timestamp = jObject.get("rateTimestamp");
		        try
		        {
		        	Instant at = ZonedDateTime.parse(String.valueOf(timestamp), RATE_TIMESTAMP).toInstant();
		        	Instant now = Instant.now();
		        	soft.assertTrue(at.isAfter(now.minus(MAX_RATE_AGE)), "rateTimestamp is more than "+MAX_RATE_AGE.toDays()+" days old: "+timestamp);
		        	soft.assertTrue(at.isBefore(now.plus(Duration.ofHours(1))), "rateTimestamp is in the future: "+timestamp);
		        }
		        catch(DateTimeParseException e)
		        {
		        	soft.fail("rateTimestamp is missing or not in the form 2026-10-03T16:23 CEST: "+timestamp);
		        }
		        soft.assertAll();
		        
		        System.out.println("CurrecnyPair:"+jObject.get("currencyPair"));
		        System.out.println("Bid:"+bid+" Ask:"+ask+" Mid:"+midRate(jObject));
		        System.out.println("TimeStamp:"+timestamp);
		        
		        //add the validated result for writing to excel
		        exchangeRateList.add(jObject);
	}
	
	//a pair and its inverse must agree: SEKNOK x NOKSEK is about 1
	@Test(enabled=true,priority=3,groups="FXRates",dependsOnMethods="getIndividualExchangeRateForGivenCCY",alwaysRun=true,description="Inverse pairs agree")
	public static void inversePairsAgree()
	{
		Map<String, Double> mids = new TreeMap<String, Double>();
		for(JSONObject rate : exchangeRateList)
		{
			mids.put(rate.get("currencyPair").toString(), midRate(rate));
		}
		
		SoftAssert soft = new SoftAssert();
		Set<String> checked = new HashSet<String>();
		for(Map.Entry<String, Double> e : mids.entrySet())
		{
			String pair = e.getKey();
			String inverse = pair.substring(3) + pair.substring(0, 3);
			if(mids.containsKey(inverse) && checked.add(inverse))
			{
				checked.add(pair);
				double product = e.getValue() * mids.get(inverse);
				soft.assertTrue(Math.abs(product - 1) < INVERSE_TOLERANCE,
						String.format("%s x %s = %.4f, expected about 1", pair, inverse, product));
			}
		}
		if(checked.isEmpty())
		{
			throw new SkipException("No pair and its inverse both passed, nothing to compare");
		}
		System.out.println("Compared "+checked.size()/2+" pairs with their inverses");
		soft.assertAll();
	}
	
	//alwaysRun so the rates that did succeed are still written when some pairs fail
	@Test(enabled=true,priority=4,groups="FXRates",dependsOnMethods="getIndividualExchangeRateForGivenCCY",alwaysRun=true,description="Rates written to Excel")
	public static void writeToExcelRate() throws IOException
	{
		if(exchangeRateList.isEmpty())
		{
			throw new SkipException("No valid exchange rates to write");
		}
		
		try(XSSFWorkbook workbook = new XSSFWorkbook())
		{
		XSSFSheet sheet = workbook.createSheet("ExchangeRate");
		for(int c = 0; c < EXCEL_COLUMNS.length; c++)
		{
			sheet.setColumnWidth(c, 6000);
		}

		 
		XSSFRow header = sheet.createRow(0);
		 
		XSSFCellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		 
		XSSFFont font = workbook.createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 16);
		font.setBold(true);
		headerStyle.setFont(font);
		 
		for(int c = 0; c < EXCEL_COLUMNS.length; c++)
		{
			XSSFCell headerCell = header.createCell(c);
			headerCell.setCellValue(EXCEL_COLUMNS[c]);
			headerCell.setCellStyle(headerStyle);
		}
		
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
		 
		//write the rates as numbers so excel can sort and calculate with them
		double[] rates = { ((Number) jObject.get("bidRate")).doubleValue(), ((Number) jObject.get("askRate")).doubleValue(), midRate(jObject) };
		for(int c = 0; c < rates.length; c++)
		{
			cell = row.createCell(c + 1);
			cell.setCellValue(rates[c]);
			cell.setCellStyle(style);
		}
		
		cell = row.createCell(4);
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
		
		//read the file back: every validated rate must be there, in order, with the same values
		try(FileInputStream in = new FileInputStream(fileLocation); XSSFWorkbook saved = new XSSFWorkbook(in))
		{
			XSSFSheet savedSheet = saved.getSheet("ExchangeRate");
			Assert.assertNotNull(savedSheet, "Sheet ExchangeRate missing from "+fileLocation);
			Assert.assertEquals(savedSheet.getLastRowNum(), exchangeRateList.size(), "Rows in "+fileLocation+" (not counting the header)");
			for(int c = 0; c < EXCEL_COLUMNS.length; c++)
			{
				Assert.assertEquals(savedSheet.getRow(0).getCell(c).getStringCellValue(), EXCEL_COLUMNS[c], "Header of column "+(c + 1));
			}
			SoftAssert soft = new SoftAssert();
			for(int r = 1; r <= exchangeRateList.size(); r++)
			{
				JSONObject expected = exchangeRateList.get(r - 1);
				XSSFRow row = savedSheet.getRow(r);
				String pair = expected.get("currencyPair").toString();
				soft.assertEquals(row.getCell(0).getStringCellValue(), pair, "Pair in row "+r);
				soft.assertEquals(row.getCell(3).getNumericCellValue(), midRate(expected), 1e-9, "Mid rate of "+pair);
				soft.assertEquals(row.getCell(4).getStringCellValue(), expected.get("rateTimestamp").toString(), "Timestamp of "+pair);
			}
			soft.assertAll();
		}
		}
	}
	
	//market order API
	@Test(enabled=true,priority=5,groups="MarketOrder",invocationCount=10,description="Market orders return a JSON list")
	public void marketOrdersTest() throws ParseException
	{
		//the 10 calls are separate entries in the report, not one test with 9 retries
		int call = marketOrderCall.incrementAndGet();
		Allure.parameter("call", call);
		Allure.getLifecycle().updateTest(t -> t.setName("Market orders return a JSON list (call "+call+" of 10)"));
		//Get the restbase base URL
		RestAssured.baseURI= restApiTest.baseUri;
	
		String mktIdApiUrl = String.format(restApiTest.mktOrder, BusinessFunctions.getDateInISO8601(),restApiTest.appId);
		
		//make the rest call
		String requestId = BusinessFunctions.getRequestId();
		Response response = RestAssured.given()
							.header(restApiTest.headerName, requestId)
			                .log()
			                .all()
			                .when()
			                .get(mktIdApiUrl);
		
		String body = response.asString();
		System.out.println(body);
		ResponseChecks.assertOk(response, requestId, "market orders");
		//the sandbox returns a list of orders (empty when there are none); parse() fails on invalid JSON
		Object orders = new JSONParser().parse(body);
		Assert.assertTrue(orders instanceof JSONArray, "Market orders is not a JSON list: "+body);
		
	}
	
	
}
