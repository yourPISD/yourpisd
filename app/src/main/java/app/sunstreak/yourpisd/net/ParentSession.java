package app.sunstreak.yourpisd.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;

import app.sunstreak.yourpisd.util.HTTPResponse;
import app.sunstreak.yourpisd.util.Request;


public class ParentSession extends Session {
	public ParentSession(String username, String password) {
		this.username = username;
		this.password = password;
		this.domain = Domain.PARENT;
	}

	@Override
	public int login() throws MalformedURLException, IOException {
		String postParams;

		String[][] requestProperties1 = new String[][] {
				{ "Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8" },
				{ "Accept-Encoding", "gzip,deflate,sdch" },
				{ "Accept-Language", "en-US,en;q=0.8,es;q=0.6" },
				{ "Connection", "keep-alive" },
				{ "Content-Type", "application/x-www-form-urlencoded" },
				{ "Host", "parentviewer.pisd.edu" },
				{ "Origin", "https://parentviewer.pisd.edu" },
				{ "Referer", "https://parentviewer.pisd.edu/" },
				{
						"User-Agent",
						"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/30.0.1599.114 Chrome/30.0.1599.114 Safari/537.36" } };

		ArrayList<String[]> rp1 = new ArrayList<String[]>(
				java.util.Arrays.asList(requestProperties1));

		postParams = "__LASTFOCUS="
				+ "&__EVENTTARGET="
				+ "&__EVENTARGUMENT="
				+ "&__VIEWSTATE=%2FwEPDwULLTEwNjY5NzA4NTBkZMM%2FuYdqyffE27bFnREF10B%2FRqD4"
				+ "&__SCROLLPOSITIONX=0"
				+ "&__SCROLLPOSITIONY=0"
				+ "&__EVENTVALIDATION=%2FwEdAASCW34hepkNwIXSnvGxEUTlqcZt0XO7QUOibAd3ocrpayqHxD2e5zCnWBj9%2Bm7TCi0S%2BC76MEjhL0ie%2FPsBbOp%2BShjkt2W533uAqvBQcWZNXoh672M%3D"
				+ "&ctl00%24ContentPlaceHolder1%24portalLogin%24UserName="
				+ URLEncoder.encode(username, "UTF-8")
				+ "&ctl00%24ContentPlaceHolder1%24portalLogin%24Password="
				+ URLEncoder.encode(password, "UTF-8")
				+ "&ctl00%24ContentPlaceHolder1%24portalLogin%24LoginButton=Login";

		HTTPResponse login = Request.sendPost("https://parentviewer.pisd.edu/",
				postParams, cookies, rp1);

		postParams = "username=" + URLEncoder.encode(username, "UTF-8")
				+ "&password=" + URLEncoder.encode(password, "UTF-8");

		HTTPResponse cookieAuth = Request
				.sendPost(
                        "http://parent.mypisd.net/CookieAuth?domain=www.parent.mypisd.net",
                        postParams, cookies);

		if (Parser.accessGrantedEditure(cookieAuth.getData())) {
			System.out.println("Editure access granted!");
			editureLogin = 1;

			passthroughCredentials = Parser.passthroughCredentials(cookieAuth
					.getData());

			return 1;
		} else {
			System.out.println("Bad username/password 1!");
			System.out.println(cookieAuth.getData());
			editureLogin = -1;
			return -1;
		}
	}

	@Override
	public boolean logout() {
		try {
			Request.sendGet(
					"https://gradebook.pisd.edu/Pinnacle/Gradebook/Logon.aspx?Action=Logout",
					cookies);

			HTTPResponse logout = Request.sendGet(
					"http://www.parent.mypisd.net/Logout", cookies);
			int responseCode = logout.getResponseCode();

			return responseCode == 302 || responseCode == 200;
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

}
