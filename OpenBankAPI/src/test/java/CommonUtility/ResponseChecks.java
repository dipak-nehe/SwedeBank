package CommonUtility;

import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import io.restassured.response.Response;

//checks that every response from the sandbox must pass, whatever the endpoint
public class ResponseChecks 
{
	//the slowest real call seen was about 2 s (market orders); 10 s means something is wrong
	public static final long MAX_RESPONSE_MS = 10000;

	//status 200, a JSON body, our request id echoed back and a reasonable response time
	public static void assertOk(Response response, String sentRequestId, String what)
	{
		Assert.assertEquals(response.getStatusCode(), 200, statusMessage(response, what));
		
		String contentType = response.getContentType();
		Assert.assertTrue(contentType != null && contentType.startsWith("application/json"),
				what+": expected a JSON response but Content-Type was "+contentType);
		
		//the sandbox returns the x-request-id we sent, which ties each response to its request
		Assert.assertEquals(response.getHeader("X-Request-ID"), sentRequestId,
				what+": response X-Request-ID doesn't match the one sent");
		
		long ms = response.getTimeIn(TimeUnit.MILLISECONDS);
		Assert.assertTrue(ms < MAX_RESPONSE_MS, what+": response took "+ms+" ms (limit "+MAX_RESPONSE_MS+" ms)");
	}

	//failure message for a non-200 response; on 429 it says when the sandbox's hourly quota resets
	public static String statusMessage(Response response, String what)
	{
		String message = "Unexpected status for "+what+", body: "+response.asString();
		if(response.getStatusCode() == 429)
		{
			message += " (sandbox rate limit of "+response.getHeader("X-Rate-Limit-Limit")
					+" requests reached; resets in "+response.getHeader("X-Rate-Limit-Reset")+" seconds)";
		}
		return message;
	}
}
