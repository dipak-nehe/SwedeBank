package CommonUtility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ReadPropertyFile {
	//Property File Path
	private static String propertyFilePath = "./config.properties";
	
	//Read and return proerty file path
	public static String readPropFileAndReturnPropertyValue(String propName ) throws IOException 
	{
		Properties prop=new Properties();
		FileInputStream ip= new FileInputStream(propertyFilePath);
		prop.load(ip);
		return prop.getProperty(propName);		
	}
}
