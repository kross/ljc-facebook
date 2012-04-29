package uk.co.kimross.ljc.facebook.controller;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


//http://developers.facebook.com/docs/reference/api/
@Controller
public class InfoController {

	@RequestMapping("/me")
	public String me(@RequestParam(value="accessToken") String accessToken, Model model) throws IOException, JSONException {
		String body = facebookCall("https://graph.facebook.com/me?access_token=" + accessToken);
		model.addAttribute("response", body);
		return "responseBody";
	}
	
	@RequestMapping("/likes")
	public String likes(@RequestParam(value="accessToken") String accessToken, Model model) throws IOException, JSONException {
		String body = facebookCall("https://graph.facebook.com/me/likes?access_token=" + accessToken);
		model.addAttribute("response", body);
		return "responseBody";
	}
	
	@RequestMapping("/friends")
	public String friends(@RequestParam(value="accessToken") String accessToken, Model model) throws IOException, JSONException {
		String body = facebookCall("https://graph.facebook.com/me/friends?access_token=" + accessToken);
		model.addAttribute("response", body);
		return "responseBody";
	}
	
	private String facebookCall(String uri) throws HttpException, IOException {
		HttpClient client = new HttpClient();
		GetMethod getter = new GetMethod(uri);
		client.executeMethod(getter);
		return getter.getResponseBodyAsString();
	}
	
}
