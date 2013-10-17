package app.sunstreak.yourpisd.net;

public enum Domain {

	PARENT (/*"http://parent.mypisd.net/CookieAuth?domain=www.parent.mypisd.net",*/
			"https://parentviewer.pisd.edu/Login.aspx",
			"http://parent.mypisd.net", 
			0),
	STUDENT ("https://sso.portal.mypisd.net/cas/login?service=http%3A%2F%2Fportal.mypisd.net%2Fc%2Fportal%2Flogin",
			"https://sso.portal.mypisd.net/cas/login?service=http%3A%2F%2Fportal.mypisd.net%2Fc%2Fportal%2Flogin",
			1),
	TEST (null, null, 2);

	public final String loginAddress;
	public final String portalAddress;
	public final int index;

	Domain (String loginAddress, String portalAddress, int index) {
		this.index = index;
		this.loginAddress = loginAddress;
		this.portalAddress = portalAddress;
	}

}
