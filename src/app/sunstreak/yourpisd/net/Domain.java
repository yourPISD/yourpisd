package app.sunstreak.yourpisd.net;

public enum Domain {
	
	
	/*
	 * Parent account. Note that the String loginAddress is of inconsistent format with schools. NOTE: The constructor is different.
	 */
	PARENT (
			"http://parent.mypisd.net/CookieAuth?domain=www.parent.mypisd.net", 
			"http://parent.mypisd.net", 
			0),
	STUDENT (
			"https://sso.portal.mypisd.net/cas/login?service=http%3A%2F%2Fportal.mypisd.net%2Fc%2Fportal%2Flogin",
			"https://sso.portal.mypisd.net/cas/login?service=http%3A%2F%2Fportal.mypisd.net%2Fc%2Fportal%2Flogin",
			1),
	TEST (null, null, 2);
	/*
	 * Elementary schools
	 */
	
			/*
	ALDRIDGE ("aldridge.mypisd.net", "http://aldridge.mypisd.net", 1),
	ANDREWS ("andrews.mypisd.net", "http://andrews.mypisd.net", 2),
	BARKSDALE ("barksdale.mypisd.net", "http://barksdale.mypisd.net", 3),
	BETHANY ("bethany.mypisd.net", "http://bethany.mypisd.net", 4),
	BEVERLY ("beverly.mypisd.net", "http://beverly.mypisd.net", 5),
	BOGGESS ("boggess.mypisd.net", "http://boggess.mypisd.net", 6),
	BRINKER ("brinker.mypisd.net", "http://brinker.mypisd.net", 7),
	CARLISLE ("carlisle.mypisd.net", "http://carlisle.mypisd.net", 8),
	CENTENNIAL ("centennial.mypisd.net", "http://centennial.mypisd.net", 9),
	CHRISTIE ("christie.mypisd.net", "http://christie.mypisd.net", 10),
	DAFFRON ("daffron.mypisd.net", "http://daffron.mypisd.net", 11),
	DAVIS ("davis.mypisd.net", "http://davis.mypisd.net", 12),
	DOOLEY ("dooley.mypisd.net", "http://dooley.mypisd.net", 13),
	FORMAN ("forman.mypisd.net", "http://forman.mypisd.net", 14),
	GULLEDGE ("gulledge.mypisd.net", "http://gulledge.mypisd.net", 15),
	HAGGAR ("haggar.mypisd.net", "http://haggar.mypisd.net", 16),
	HARRINGTON ("harrington.mypisd.net", "http://harrington.mypisd.net", 17),
	HAUN ("haun.mypisd.net", "http://haun.mypisd.net", 18),
	HEDGCOXE ("hedgcoxe.mypisd.net", "http://hedgcoxe.mypisd.net", 19),
	HICKEY ("hickey.mypisd.net", "http://hickey.mypisd.net", 20),
	HIGHTOWER ("hightower.mypisd.net", "http://hightower.mypisd.net", 21),
	HUFFMAN ("huffman.mypisd.net", "http://huffman.mypisd.net", 22),
	HUGHSTON ("hughston.mypisd.net", "http://hughston.mypisd.net", 23),
	HUNT ("hunt.mypisd.net", "http://hunt.mypisd.net", 24),
	JACKSON ("jackson.mypisd.net", "http://jackson.mypisd.net", 25),
	MATHEWS ("mathews.mypisd.net", "http://mathews.mypisd.net", 26),
	MCCALL ("mccall.mypisd.net", "http://mccall.mypisd.net", 27),
	MEADOWS ("meadows.mypisd.net", "http://meadows.mypisd.net", 28),
	MEMORIAL ("memorial.mypisd.net", "http://memorial.mypisd.net", 29),
	MENDENHALL ("mendenhall.mypisd.net", "http://mendenhall.mypisd.net", 30),
	MILLER ("miller.mypisd.net", "http://miller.mypisd.net", 31),
	MITCHELL ("mitchell.mypisd.net", "http://mitchell.mypisd.net", 32),
	RASOR ("rasor.mypisd.net", "http://rasor.mypisd.net", 33),
	SAIGLING ("saigling.mypisd.net", "http://saigling.mypisd.net", 34),
	SCHELL ("schell.mypisd.net", "http://schell.mypisd.net", 35),
	SHEPARD ("shepard.mypisd.net", "http://shepard.mypisd.net", 36),
	SIGLER ("sigler.mypisd.net", "http://sigler.mypisd.net", 37),
	SKAGGS ("skaggs.mypisd.net", "http://skaggs.mypisd.net", 38),
	STINSON ("stinson.mypisd.net", "http://stinson.mypisd.net", 39),
	THOMAS ("thomas.mypisd.net", "http://thomas.mypisd.net", 40),
	WEATHERFORD ("weatherford.mypisd.net", "http://weatherford.mypisd.net", 41),
	WELLS ("wells.mypisd.net", "http://wells.mypisd.net", 42),
	WYATT ("wyatt.mypisd.net", "http://wyatt.mypisd.net", 43),
		*/


	/*
	 * Middle schools
	 */
			
		/*
	ARMSTRONG ("armstrong.mypisd.net", "http://armstrong.mypisd.net", 44),
	BOWMAN ("bowman.mypisd.net", "http://bowman.mypisd.net", 45),
	CARPENTER ("carpenter.mypisd.net", "http://carpenter.mypisd.net", 46),
	FRANKFORD ("frankford.mypisd.net", "http://frankford.mypisd.net", 47),
	HAGGARD ("haggard.mypisd.net", "http://haggard.mypisd.net", 48),
	HENDRICK ("hendrick.mypisd.net", "http://hendrick.mypisd.net", 49),
	MURPHY ("murphy.mypisd.net", "http://murphy.mypisd.net", 50),
	OTTO ("otto.mypisd.net", "http://otto.mypisd.net", 51),
	RENNER ("renner.mypisd.net", "http://renner.mypisd.net", 52),
	RICE ("rice.mypisd.net", "http://rice.mypisd.net", 53),
	ROBINSON ("robinson.mypisd.net", "http://robinson.mypisd.net", 54),
	SCHIMELPFENIG ("schimelpfenig.mypisd.net", "http://schimelpfenig.mypisd.net", 55),
	WILSON ("wilson.mypisd.net", "http://wilson.mypisd.net", 56),
		*/
			
	/*
	 * High schools
	 */
		/*
	MCMILLEN ("mcmillen.mypisd.net", "http://mcmillen.mypisd.net", 57),
	PLANO_EAST ("pesh.mypisd.net", "http://pesh.mypisd.net", 58),
	PLANO_SENIOR ("pshs.mypisd.net", "http://pshs.mypisd.net", 59),
	PLANO_WEST ("pwsh.mypisd.net", "http://pwsh.mypisd.net", 60),
	SHEPTON ("shepton.mypisd.net", "http://shepton.mypisd.net", 61),
	VINES ("vines.mypisd.net", "http://vines.mypisd.net", 62),
	WILLIAMS ("williams.mypisd.net", "http://williams.mypisd.net", 63);
		*/

	//public static final String MYPISD_LOGIN_PREFIX = "https://login1.mypisd.net/CookieAuth?domain=www.";

	public final String loginAddress;
	public final String portalAddress;
	public final int index;
	
	Domain (String loginAddress, String portalAddress, int index) {
		this.index = index;
		
//		if (loginAddress.equals("http://parent.mypisd.net/CookieAuth?domain=www.parent.mypisd.net"))
			this.loginAddress = loginAddress;
//		else
//			this.loginAddress = MYPISD_LOGIN_PREFIX + loginAddress;
		this.portalAddress = portalAddress;
	}

}
