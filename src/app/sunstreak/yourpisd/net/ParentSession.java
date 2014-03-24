package app.sunstreak.yourpisd.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Set;

public class ParentSession extends Session {
	public ParentSession (String username, String password) {
		this.username = username;
		this.password = password;
		this.domain = Domain.PARENT;
	}

	public int login () throws MalformedURLException, IOException {
		String response;
		int responseCode;
		String postParams;

		String[][] requestProperties1 = new String[][] {
				{"Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"},
				{"Accept-Encoding","gzip,deflate,sdch"},
				{"Accept-Language","en-US,en;q=0.8,es;q=0.6"},
				{"Connection","keep-alive"},
				{"Content-Type","application/x-www-form-urlencoded"},
				{"Host", "parentviewer.pisd.edu"},
				{"Origin", "https://parentviewer.pisd.edu"},
				{"Referer", "https://parentviewer.pisd.edu/"},
				{"User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/30.0.1599.114 Chrome/30.0.1599.114 Safari/537.36"}
		};

		ArrayList<String[]> rp1 = new ArrayList<String[]>(java.util.Arrays.asList(requestProperties1));

		postParams = 
				"__LASTFOCUS=" +
						"&__EVENTTARGET=" +
						"&__EVENTARGUMENT=" +
						"&__VIEWSTATE=%2FwEPDwULLTEwNjY5NzA4NTBkZMM%2FuYdqyffE27bFnREF10B%2FRqD4" +
						"&__SCROLLPOSITIONX=0" +
						"&__SCROLLPOSITIONY=0" +
						"&__EVENTVALIDATION=%2FwEdAASCW34hepkNwIXSnvGxEUTlqcZt0XO7QUOibAd3ocrpayqHxD2e5zCnWBj9%2Bm7TCi0S%2BC76MEjhL0ie%2FPsBbOp%2BShjkt2W533uAqvBQcWZNXoh672M%3D" +
						"&ctl00%24ContentPlaceHolder1%24portalLogin%24UserName=" + URLEncoder.encode(username, "UTF-8") +
						"&ctl00%24ContentPlaceHolder1%24portalLogin%24Password=" + URLEncoder.encode(password, "UTF-8") +
						"&ctl00%24ContentPlaceHolder1%24portalLogin%24LoginButton=Login";
		Object[] login = Request.sendPost(
				"https://parentviewer.pisd.edu/", 
				postParams, 
				cookies,
				rp1);
		response = (String) login[0];
		responseCode = (Integer) login[1];
		cookies = (Set<String>) login[2];

		postParams = "username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
		Object[] cookieAuth = Request.sendPost(
				"http://parent.mypisd.net/CookieAuth?domain=www.parent.mypisd.net", 
				postParams, 
				cookies);
		response = (String) cookieAuth[0];
		responseCode = (Integer) cookieAuth[1];
		cookies = (Set<String>) cookieAuth[2];

		if (Parser.accessGrantedEditure(response)) {
			System.out.println("Editure access granted!");
			editureLogin = 1;

			passthroughCredentials = Parser.passthroughCredentials(response);

			return 1;
		}
		else {
			System.out.println("Bad username/password 1!");
			System.out.println(response);
			editureLogin = -1;
			return -1;
		}
	}
	
	public boolean logout () {
		try {
			Request.sendGet("https://gradebook.pisd.edu/Pinnacle/Gradebook/Logon.aspx?Action=Logout", cookies);
			
			Object[] logout = Request.sendGet("http://www.parent.mypisd.net/Logout", cookies);
			//String response = (String) logout[0];
			int responseCode = (Integer) logout[1];
			cookies = (Set<String>) logout[2];

			return responseCode==302 || responseCode==200;
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}
	
}
