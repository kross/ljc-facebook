package uk.co.kimross.ljc.facebook.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import uk.co.kimross.ljc.facebook.FacebookConstants;

/*
 * Note:  This is a rough example - it should not be used in production.  It does not handle errors gracefully & is fickle
 * It was written purely to demonstrate some facebook goodness at the LJC
 */
@Controller
public class FacebookController {

	// http://developers.facebook.com/docs/authentication/canvas/
	@RequestMapping("/fb/")
	public String fb(@RequestParam(value = "signed_request") String signedRequest, Model model) throws IOException, JSONException, NoSuchAlgorithmException, InvalidKeyException {
		String[] parts = signedRequest.split("\\.", 2);
		Base64 decoder = new Base64(true); //Gotcha - decoder must be base64 URL 
        
		String sig = new String(decoder.decode(parts[0].getBytes()));
        checkSignature(sig, parts[1]);
        String data = new String(decoder.decode(parts[1].getBytes()));
        

		if (data.contains("user_id")) {
			// This means the user has already authenticated the app, you can get the id & Access token
			JSONObject sReq = new JSONObject(data);
			model.addAttribute("accessToken", sReq.getString("oauth_token"));
			return "facebook";
		} else {
			// Not authenticated -
			// http://developers.facebook.com/docs/reference/dialogs/oauth/
			StringBuilder redirectUrl = new StringBuilder("http://www.facebook.com/dialog/oauth/?client_id=");
			redirectUrl.append(FacebookConstants.APP_ID);
			redirectUrl.append("&redirect_uri=");
			redirectUrl.append(URLEncoder.encode("http://apps.facebook.com/ljc-presentation/auth/", "UTF-8"));
			redirectUrl.append("&state="); //Use unique code to prevent cross-site request forgery, + app state
			redirectUrl.append("MYSTATE");
			redirectUrl.append("&scope=");
			redirectUrl.append("email,user_likes"); // http://developers.facebook.com/docs/authentication/permissions/
			return "redirect:" + redirectUrl.toString();
		}

	}

	private void checkSignature(String sig, String data) throws NoSuchAlgorithmException, InvalidKeyException {
		SecretKeySpec secret = new SecretKeySpec(FacebookConstants.APP_SECRET.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secret);
        mac.update(data.getBytes());
        String hmac = new String(mac.doFinal());

        if(!hmac.equals(sig)){
        	throw new IllegalAccessError("Bad signature");  //You probably want to handle this differently
        }
	}

	// Server side authentication flow
	// http://developers.facebook.com/docs/authentication/server-side/
	@RequestMapping("/fb/auth/")
	public String auth(HttpServletRequest request, Model model) throws IOException, JSONException {
		if (request.getParameter("error") != null) { // User denied app
			return "aww";
		}

		String state = request.getParameter("state"); //This is the state I initially set in fb above, check it :)
		String code = request.getParameter("code"); //We use the code to get the access token
		
		StringBuilder accessTokenUri = new StringBuilder("https://graph.facebook.com/oauth/access_token?client_id=");
		accessTokenUri.append(FacebookConstants.APP_ID);
		accessTokenUri.append("&redirect_uri=");
		accessTokenUri.append(URLEncoder.encode("http://apps.facebook.com/ljc-presentation/auth/", "UTF-8"));  //Must be IDENTICAL to redirect in fb method abpve
		accessTokenUri.append("&client_secret=");
		accessTokenUri.append(FacebookConstants.APP_SECRET);
		accessTokenUri.append("&code=");
		accessTokenUri.append(URLEncoder.encode(code, "UTF-8"));
		
		HttpClient client = new HttpClient();
		GetMethod getter = new GetMethod(accessTokenUri.toString());
		client.executeMethod(getter);
		
		String response = getter.getResponseBodyAsString();
		String accessToken = response.substring(response.indexOf("=") + 1, response.indexOf("&expires")); //This is a really ugly way of parsing response
		model.addAttribute("accessToken", accessToken);
		
		return "facebook";
	}

	

}
