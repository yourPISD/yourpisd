package app.sunstreak.yourpisd.util;


public class HTTPResponse {

	private final String data;
	private final int responseCode;

	public HTTPResponse(String data, int responseCode) {
		this.data = data;
		this.responseCode = responseCode;
	}

	public String getData() {
		return data;
	}

	public int getResponseCode() {
		return responseCode;
	}

}
