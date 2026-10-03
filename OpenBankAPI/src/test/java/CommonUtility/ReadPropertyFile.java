package CommonUtility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ReadPropertyFile {
	//Property File Path
	private static String propertyFilePath = "./config.properties";
	private static Properties prop;

	//Read and return proerty file path; a -DpropName=value on the mvn command line overrides the file
	public static synchronized String readPropFileAndReturnPropertyValue(String propName ) throws IOException 
	{
		String override = System.getProperty(propName);
		if(override != null)
		{
			return override;
		}
		if(prop == null)
		{
			Properties loaded = new Properties();
			try(FileInputStream ip = new FileInputStream(propertyFilePath))
			{
				loaded.load(ip);
			}
			prop = loaded;
		}
		return prop.getProperty(propName);		
	}
}
