package test;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class openBankAPITest {
	
    @SuppressWarnings("unchecked")
	@Test(enabled=true)
	public static void getTokenFromOpenBankAPI()
	{

      String baseUrl ="https://apisandbox.openbankproject.com";
       String Oauth = "/oauth/initiate";
       String url = baseUrl.concat(Oauth);
       
       long timestamp = System.currentTimeMillis() / 1000;
       String timeStampString = "";
       timeStampString= timeStampString+timestamp;
       
       //get timestamp in seconds since 1970
       long millis = System.currentTimeMillis();
       millis=(millis / 1000); // prints the same Unix timestamp in seconds
    	String seconds = "";
       	seconds= seconds + millis;
       
       Response  response = RestAssured.given()
    		  .param("oauth_callback", "oob")    		  
    		  .param("oauth_consumer_key", "jsdaizjnpx1k5yly0x5rrdp4uzz2xli3qmfsocgu")
    		  .param("oauth_nonce", timestamp)
    		  .param("oauth_signature", "znhbc4fjxzuxgxp3psekxfkt2yh0v4n0pawa53ln")
    		  .param("oauth_signature_method",  "HMAC-SHA256")
    		  .param("oauth_timestamp",  seconds)
    		  .param("oauth_version",  "1.0")
    		  .header("Content-Type", "application/json")
    		  .when()
    		  .log()
    		  .all()
    		  .post(url);

    		  
/*

    		  
// JSONObject is a class that represents a Simple JSON.
// We can add Key - Value pairs using the put method
       JSONObject requestParams = new JSONObject();
       requestParams.put("oauth_callback", "http%3A%2F%2Fgoogle.com%2F"); 
       requestParams.put("oauth_consumer_key", "jsdaizjnpx1k5yly0x5rrdp4uzz2xli3qmfsocgu");
       
       requestParams.put("oauth_nonce", timestamp);
       requestParams.put("oauth_signature", "znhbc4fjxzuxgxp3psekxfkt2yh0v4n0pawa53ln");
       requestParams.put("oauth_signature_method",  "HMAC-SHA1");	
       
       //get timestamp in seconds since 1970
       long millis = System.currentTimeMillis();
       millis=(millis / 1000); // prints the same Unix timestamp in seconds
       requestParams.put("oauth_timestamp",  millis);	
       requestParams.put("oauth_version",  "1.0");	

       // Add a header stating the Request body is a JSON
       request.header("Content-Type", "application/json");
 
       // Add the Json to the body of the request
       request.body(requestParams.toJSONString());
 
       // Post the request and check the response
       Response response = request.post(Oauth);
*/
       System.out.println(response.asString());

       
       //return null;
		
	}

}
