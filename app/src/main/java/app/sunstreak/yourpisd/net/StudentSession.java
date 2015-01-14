package app.sunstreak.yourpisd.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;

import app.sunstreak.yourpisd.util.HTTPResponse;
import app.sunstreak.yourpisd.util.Request;

public class StudentSession extends Session {
	public StudentSession(String username, String password) {
		this.username = username;
		this.password = password;
		this.domain = Domain.STUDENT;
	}

	@Override
	public int login() throws MalformedURLException, IOException {
		String response;
		int responseCode;
		String postParams;

		HTTPResponse portalDefaultPage = Request.sendGet(domain.loginAddress,
				cookies);

		response = portalDefaultPage.getData();
		responseCode = portalDefaultPage.getResponseCode();

		String[][] requestProperties = new String[][] {
				{ "Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8" },
				{ "Accept-Encoding", "gzip,deflate,sdch" },
				{ "Accept-Language", "en-US,en;q=0.8,es;q=0.6" },
				{ "Cache-Control", "max-age=0" },
				{ "Connection", "keep-alive" },
				{ "Content-Type", "application/x-www-form-urlencoded" },
				{ "Host", "sso.portal.mypisd.net" },
				{ "Origin", "https://sso.portal.mypisd.net" },
				{
						"Referer",
						"https://sso.portal.mypisd.net/cas/login?service=http%3A%2F%2Fportal.mypisd.net%2Fc%2Fportal%2Flogin" } };

		ArrayList<String[]> rp = new ArrayList<String[]>(
				java.util.Arrays.asList(requestProperties));

		String lt = Parser.portalLt(response);

		postParams = "username=" + URLEncoder.encode(username, "UTF-8")
				+ "&password=" + URLEncoder.encode(password, "UTF-8") + "&lt="
				+ lt + "&_eventId=submit";
		try {
			HTTPResponse portalLogin = Request.sendPost(domain.loginAddress,
					cookies, rp, true, postParams);

			response = portalLogin.getData();
			responseCode = portalLogin.getResponseCode();

			// weird AF way of checking for bad password.
			if (Request.getRedirectLocation() == null)
				return -1;

			HTTPResponse ticket = Request.sendGet(
					Request.getRedirectLocation(), cookies);

			if (ticket == null)
				return -2;

			response = ticket.getData();
			responseCode = ticket.getResponseCode();

			passthroughCredentials = Parser.passthroughCredentials(response);
			return 1;
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			return -2;
		}
	}

	@Override
	public boolean logout() {
		try {
			HTTPResponse logout = Request.sendGet(
					"http://portal.mypisd.net/c/portal/logout", cookies);
			int responseCode = logout.getResponseCode();

			return responseCode == 302 || responseCode == 200;
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}
}
