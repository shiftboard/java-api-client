import java.util.Base64;
import java.util.Base64.Encoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.net.*;
import java.io.*;

/* To make this work stand-alone with no external dependencies, this is not used.
 * This is a good library for dealing with JSON in Java.
 * https://github.com/douglascrockford/JSON-java
import org.json.JSONObject;
*/

public class ShiftboardAPIClient {

    public static void main(String[] args) {
        /*
         * If you were using JSONObject, you'd make a new object, put stuff in it, and convert it to a string.
        JSONObject params = new JSONObject();
        params.put("dinner", "nachos");
        String paramString = params.toString();
        */

        // Since we're not using JSONObject, here's a hand-rolled JSON string.
        String paramString = "{\"dinner\":\"nachos\"}";
        
        String jsonString = callAPI("system.echo", paramString);
        
        System.out.println(jsonString);
    }
    
    private static String getAPIKey() {
        return "Your Shiftboard API key";
    }
    
    private static String getSignatureKey() {
        return "Your Shiftboard API signature key -- keep this very protected";
    }

    public static String callAPI(String method, String params) {
        String apiKey = getAPIKey();
        String signatureKey = getSignatureKey();

        // Make an encoder object that does the URI encoding as well.
        Encoder base64 = Base64.getUrlEncoder().withoutPadding();
        
        // Encode the params
        String paramsEncoded = base64.encodeToString(params.getBytes());

        // Create the signature
        String signatureBits = "method" + method + "params" + params;
        String signature = hmacSha1(signatureBits, signatureKey);

        // Assemble the URL
        String url = "https://api.shiftdata.com/api/api.cgi?jsonrpc=2.0"
            + "&access_key_id=" + apiKey
            + "&method=" + method
            + "&params=" + paramsEncoded
            + "&signature=" + signature
            + "&id=1";

        String results;
        try {
            results = fetchURL(url);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return results;
    }
    
    public static String hmacSha1(String value, String key) {
        try {
            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(key.getBytes("UTF-8"),
                    mac.getAlgorithm());
            mac.init(secret);
            byte [] digest = mac.doFinal(value.getBytes());

            // Base 64 Encode the results
            String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);

            return signature;
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String fetchURL(String urlString) throws Exception {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        BufferedReader in = new BufferedReader(
            new InputStreamReader( conn.getInputStream() )
        );

        String inputLine;
        String output = "";
        while ((inputLine = in.readLine()) != null) {
            output = output.concat(inputLine);
        }
        in.close();

        return output;
    }
}
