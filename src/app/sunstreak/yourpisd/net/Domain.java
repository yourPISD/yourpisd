package app.sunstreak.yourpisd.net;

public enum Domain {

	PARENT (/*"http://parent.mypisd.net/CookieAuth?domain=www.parent.mypisd.net",*/
			"https://parentviewer.pisd.edu/Login.aspx",
			"http://parent.mypisd.net", 
			0,
			'U'),
	STUDENT ("https://sso.portal.mypisd.net/cas/login?service=http%3A%2F%2Fportal.mypisd.net%2Fc%2Fportal%2Flogin",
			"https://sso.portal.mypisd.net/cas/login?service=http%3A%2F%2Fportal.mypisd.net%2Fc%2Fportal%2Flogin",
			1,
			'S'),
	TEST (null, null, 2, 'S');

	public final String loginAddress;
	public final String portalAddress;
	public final int index;
	public final int hValue;

	Domain (String loginAddress, String portalAddress, int index, char hValue) {
		this.index = index;
		this.loginAddress = loginAddress;
		this.portalAddress = portalAddress;
		this.hValue = hValue;
	}

}
