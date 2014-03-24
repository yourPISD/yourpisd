package app.sunstreak.yourpisd.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Set;

public class StudentSession extends Session {
	public StudentSession (String username, String password) {
		this.username = username;
		this.password = password;
		this.domain = Domain.STUDENT;
	}

	public int login() throws MalformedURLException, IOException {
		String response;
		int responseCode;
		String postParams;

		Object[] portalDefaultPage = Request.sendGet(
				domain.loginAddress,
				cookies);

		response = (String) portalDefaultPage[0];
		responseCode = (Integer) portalDefaultPage[1];
		cookies = (Set<String>) portalDefaultPage[2];

		String[][] requestProperties = new String[][] {
				{"Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"},
				{"Accept-Encoding","gzip,deflate,sdch"},
				{"Accept-Language","en-US,en;q=0.8,es;q=0.6"},
				{"Cache-Control","max-age=0"},
				{"Connection","keep-alive"},
				{"Content-Type","application/x-www-form-urlencoded"},
				{"Host","sso.portal.mypisd.net"},
				{"Origin","https://sso.portal.mypisd.net"},
				{"Referer","https://sso.portal.mypisd.net/cas/login?service=http%3A%2F%2Fportal.mypisd.net%2Fc%2Fportal%2Flogin"}
		};

		ArrayList<String[]> rp = new ArrayList<String[]>(java.util.Arrays.asList(requestProperties));

		String lt = Parser.portalLt(response);

		postParams = "username=" + URLEncoder.encode(username, "UTF-8") + 
				"&password=" + URLEncoder.encode(password, "UTF-8") + 
				"&lt=" + lt +
				"&_eventId=submit";
		try {
			Object[] portalLogin = Request.sendPost(
					domain.loginAddress,
					cookies,
					rp,
					true,
					postParams);

			response = (String) portalLogin[0];
			responseCode = (Integer) portalLogin[1];
			cookies = (Set<String>) portalLogin[2];


			// weird AF way of checking for bad password.
			if (Request.getRedirectLocation() == null)
				return -1;

			Object[] ticket = Request.sendGet(
					Request.getRedirectLocation(),
					cookies);

			if (ticket == null)
				return -2;

			response = (String) ticket[0];
			responseCode = (Integer) ticket[1];
			cookies = (Set<String>) ticket[2];

			passthroughCredentials = Parser.passthroughCredentials(response);
			return 1;
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			return -2;
		}
	}

	public boolean logout () {
		try {
			Object[] logout = Request.sendGet("http://portal.mypisd.net/c/portal/logout", cookies);
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
