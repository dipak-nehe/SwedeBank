package CommonUtility;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;

public class BusinessFunctions 
{
	public static String APPKEY = "clientAppkey";
	public static String CLIENTSECRET = "clientSecret";
	public static String BASEURI = "baseURI";
	public static String INDICATIVERATECCYPAIRENDPOINT = "indicativeRateCcyPairURL";
	public static String HEADERNAMEFORCCYPAIR = "headerNameCcyIndPair";
	public static String INDICATIVERATECCYPAIRVALUEENDPOINT = "indicativeRateSingleCcyPair";
	public static String MKTORDERAPI = "marketOrderApiUrl";

	//get base uri from propery file
	public static String getBaseURIForEndPoint() throws IOException
	{
		//Get the restbase base URL
				return  ReadPropertyFile.readPropFileAndReturnPropertyValue(BusinessFunctions.BASEURI);
	}
	
	//get API ID
	public static String getAppId() throws IOException
	{
		//Get the restbase base URL
				return  ReadPropertyFile.readPropFileAndReturnPropertyValue(BusinessFunctions.APPKEY);
	}
	
	//get timestamp in milliseconds
	public static long getMilliSeconds()
	{
		 return ZonedDateTime.now().toInstant().toEpochMilli();
	}
	
	//enum for ccy pair
	public static enum ccyPair
	{
		EURNOK,DKKNOK,SARSEK,JPYDKK,NOKDKK,SEKDKK,USDILS,AUDJPY,ZARNOK,SGDHKD,PLNHUF,GBPSEK,INRSEK,USDSEK,JPYHKD,EURSEK,AUDUSD,NZDDKK,USDHKD,BGNNOK,INRNOK,AUDCAD,GBPJPY,CZKSEK,MXNNOK,GBPNOK,CADCHF,AUDDKK,EURAED,EURBGN,CHFSEK,EURCNH,RONSEK,DKKSEK,NZDSEK,HKDNOK,GBPCAD,CHFDKK,HUFNOK,AEDSEK,NZDNOK,HKDSEK,AEDNOK,CNHDKK,CADCNH,GBPNZD,CADJPY,SGDNOK,JPYSEK,USDCHF,CHFJPY,EURSAR,EURSGD,PLNNOK,AUDCNH,USDRUB,EURCZK,HUFSEK,NZDCHF,USDHUF,RONNOK,USDCAD,ILSNOK,GBPCHF,AUDSEK,GBPCNH,CNHNOK,BGNSEK,EURDKK,EURNZD,EURMXN,USDPLN,EURCAD,CZKNOK,USDDKK,NZDCNH,CADDKK,USDSGD,USDRON,USDCNH,NZDCAD,GBPSGD,ZARSEK,GBPUSD,EURZAR,JPYNOK,AUDNOK,EURAUD,PLNSEK,EURHKD,EURJPY,RUBNOK,NZDJPY,CNHSEK,SGDSEK,EURINR,THBNOK,USDTHB,CHFCNH,NOKSEK,RUBSEK,USDCZK,ILSSEK,USDMXN,EURGBP,EURCHF,USDZAR,CADNOK,EURHUF,MXNSEK,EURRUB,EURRON,SEKNOK,EURTHB,CHFNOK,USDJPY,USDSAR,CADSEK,GBPDKK,GBPAUD,AUDNZD,NZDUSD,USDNOK,AUDCHF,EURILS,EURUSD,EURPLN,SARNOK,THBSEK;
		//EURNOK,DKKNOK,SARSEK;
	}

	//generaye unqiue ResultFileName
	public static String returnUniqueFileName()
	{
		String fileName = "ExcelResulCcyPairRate";
		fileName = fileName + "_" + System.currentTimeMillis()/1000+".xlsx";
		return fileName;
	}
	
	//verify if given value is String
	public static boolean verifyIfString(String valueToVerify)
	{
		if(valueToVerify instanceof String)
        {
        	return true;
        	
        }
		else
		{
			return false;
		}
		
	}
	
	    //verify if given value is String
		public static boolean verifyIfDouble(Double valueToVerify)
		{
			if(valueToVerify instanceof Double)
	        {
	        	return true;
	        	
	        }
			else
			{
				return false;
			}
			
		}
		
		//get date in ISO8601 format
		public static String getDateInISO8601()
		{
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();			
			return df.format(date);
			
		}
	


}
