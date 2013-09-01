package com.sunstreaks.mypisd.net;

public enum Domain {
	
	
	/*
	 * Parent account. Note that the String loginAddress is of inconsistent format with schools. NOTE: The constructor is different.
	 */
	PARENT ("http://parent.mypisd.net/CookieAuth?domain=www.parent.mypisd.net", "http://parent.mypisd.net"),
	
	/*
	 * Elementary schools
	 */
	ALDRIDGE ("aldridge.mypisd.net", "http://aldridge.mypisd.net"),
	ANDREWS ("andrews.mypisd.net", "http://andrews.mypisd.net"),
	BARKSDALE ("barksdale.mypisd.net", "http://barksdale.mypisd.net"),
	BETHANY ("bethany.mypisd.net", "http://bethany.mypisd.net"),
	BEVERLY ("beverly.mypisd.net", "http://beverly.mypisd.net"),
	BOGGESS ("boggess.mypisd.net", "http://boggess.mypisd.net"),
	BRINKER ("brinker.mypisd.net", "http://brinker.mypisd.net"),
	CARLISLE ("carlisle.mypisd.net", "http://carlisle.mypisd.net"),
	CENTENNIAL ("centennial.mypisd.net", "http://centennial.mypisd.net"),
	CHRISTIE ("christie.mypisd.net", "http://christie.mypisd.net"),
	DAFFRON ("daffron.mypisd.net", "http://daffron.mypisd.net"),
	DAVIS ("davis.mypisd.net", "http://davis.mypisd.net"),
	DOOLEY ("dooley.mypisd.net", "http://dooley.mypisd.net"),
	FORMAN ("forman.mypisd.net", "http://forman.mypisd.net"),
	GULLEDGE ("gulledge.mypisd.net", "http://gulledge.mypisd.net"),
	HAGGAR ("haggar.mypisd.net", "http://haggar.mypisd.net"),
	HARRINGTON ("harrington.mypisd.net", "http://harrington.mypisd.net"),
	HAUN ("haun.mypisd.net", "http://haun.mypisd.net"),
	HEDGCOXE ("hedgcoxe.mypisd.net", "http://hedgcoxe.mypisd.net"),
	HICKEY ("hickey.mypisd.net", "http://hickey.mypisd.net"),
	HIGHTOWER ("hightower.mypisd.net", "http://hightower.mypisd.net"),
	HUFFMAN ("huffman.mypisd.net", "http://huffman.mypisd.net"),
	HUGHSTON ("hughston.mypisd.net", "http://hughston.mypisd.net"),
	HUNT ("hunt.mypisd.net", "http://hunt.mypisd.net"),
	JACKSON ("jackson.mypisd.net", "http://jackson.mypisd.net"),
	MATHEWS ("mathews.mypisd.net", "http://mathews.mypisd.net"),
	MCCALL ("mccall.mypisd.net", "http://mccall.mypisd.net"),
	MEADOWS ("meadows.mypisd.net", "http://meadows.mypisd.net"),
	MEMORIAL ("memorial.mypisd.net", "http://memorial.mypisd.net"),
	MENDENHALL ("mendenhall.mypisd.net", "http://mendenhall.mypisd.net"),
	MILLER ("miller.mypisd.net", "http://miller.mypisd.net"),
	MITCHELL ("mitchell.mypisd.net", "http://mitchell.mypisd.net"),
	RASOR ("rasor.mypisd.net", "http://rasor.mypisd.net"),
	SAIGLING ("saigling.mypisd.net", "http://saigling.mypisd.net"),
	SCHELL ("schell.mypisd.net", "http://schell.mypisd.net"),
	SHEPARD ("shepard.mypisd.net", "http://shepard.mypisd.net"),
	SIGLER ("sigler.mypisd.net", "http://sigler.mypisd.net"),
	SKAGGS ("skaggs.mypisd.net", "http://skaggs.mypisd.net"),
	STINSON ("stinson.mypisd.net", "http://stinson.mypisd.net"),
	THOMAS ("thomas.mypisd.net", "http://thomas.mypisd.net"),
	WEATHERFORD ("weatherford.mypisd.net", "http://weatherford.mypisd.net"),
	WELLS ("wells.mypisd.net", "http://wells.mypisd.net"),
	WYATT ("wyatt.mypisd.net", "http://wyatt.mypisd.net"),

	/*
	 * Middle schools
	 */
	ARMSTRONG ("armstrong.mypisd.net", "http://armstrong.mypisd.net"),
	BOWMAN ("bowman.mypisd.net", "http://bowman.mypisd.net"),
	CARPENTER ("carpenter.mypisd.net", "http://carpenter.mypisd.net"),
	FRANKFORD ("frankford.mypisd.net", "http://frankford.mypisd.net"),
	HAGGARD ("haggard.mypisd.net", "http://haggard.mypisd.net"),
	HENDRICK ("hendrick.mypisd.net", "http://hendrick.mypisd.net"),
	MURPHY ("murphy.mypisd.net", "http://murphy.mypisd.net"),
	OTTO ("otto.mypisd.net", "http://otto.mypisd.net"),
	RENNER ("renner.mypisd.net", "http://renner.mypisd.net"),
	RICE ("rice.mypisd.net", "http://rice.mypisd.net"),
	ROBINSON ("robinson.mypisd.net", "http://robinson.mypisd.net"),
	SCHIMELPFENIG ("schimelpfenig.mypisd.net", "http://schimelpfenig.mypisd.net"),
	WILSON ("wilson.mypisd.net", "http://wilson.mypisd.net"),

	/*
	 * High schools
	 */
	MCMILLEN ("mcmillen.mypisd.net", "http://mcmillen.mypisd.net"),
	PLANO_EAST ("pesh.mypisd.net", "http://pesh.mypisd.net"),
	PLANO_SENIOR ("pshs.mypisd.net", "http://pshs.mypisd.net"),
	PLANO_WEST ("pwsh.mypisd.net", "http://pwsh.mypisd.net"),
	SHEPTON ("shepton.mypisd.net", "http://shepton.mypisd.net"),
	VINES ("vines.mypisd.net", "http://vines.mypisd.net"),
	WILLIAMS ("williams.mypisd.net", "http://williams.mypisd.net");
	

	public static final String MYPISD_LOGIN_PREFIX = "https://login1.mypisd.net/CookieAuth?domain=www.";

	public final String loginAddress;
	public final String portalAddress;
	
	Domain (String loginAddress, String portalAddress) {
		if (loginAddress.equals("http://parent.mypisd.net/CookieAuth?domain=www.parent.mypisd.net"))
			this.loginAddress = loginAddress;
		else
			this.loginAddress = MYPISD_LOGIN_PREFIX + loginAddress;
		this.portalAddress = portalAddress;
	}

}
