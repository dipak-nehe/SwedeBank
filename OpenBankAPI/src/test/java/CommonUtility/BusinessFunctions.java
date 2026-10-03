package CommonUtility;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BusinessFunctions 
{
	public static String APPKEY_ENV = "SWEDBANK_APP_KEY";
	public static String BASEURI = "baseURI";
	public static String INDICATIVERATECCYPAIRENDPOINT = "indicativeRateCcyPairURL";
	public static String HEADERNAMEFORCCYPAIR = "headerNameCcyIndPair";
	public static String INDICATIVERATECCYPAIRVALUEENDPOINT = "indicativeRateSingleCcyPair";
	public static String MKTORDERAPI = "marketOrderApiUrl";
	public static String REQUESTDELAYMS = "requestDelayMs";

	//get base uri from propery file
	public static String getBaseURIForEndPoint() throws IOException
	{
		//Get the restbase base URL
				return  ReadPropertyFile.readPropFileAndReturnPropertyValue(BusinessFunctions.BASEURI);
	}
	
	//get API ID from the SWEDBANK_APP_KEY environment variable (never from a committed file)
	public static String getAppId()
	{
		String appId = System.getenv(BusinessFunctions.APPKEY_ENV);
		if(appId == null || appId.trim().isEmpty())
		{
			throw new IllegalStateException("Set the " + BusinessFunctions.APPKEY_ENV + " environment variable to your Swedbank sandbox app key");
		}
		return appId.trim();
	}
	
	//generate a unique value for the x-request-id header
	public static String getRequestId()
	{
		 return UUID.randomUUID().toString();
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
	
		//get date in ISO8601 format
		public static String getDateInISO8601()
		{
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();			
			return df.format(date);
			
		}
	


}
