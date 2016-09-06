package app.sunstreak.yourpisd.net.data;

/**
 * Thrown when a parse error occurs.
 */
public class ParseException extends Exception {
    public ParseException() {
    }

    public ParseException(String detailMessage) {
        super(detailMessage);
    }

    public ParseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ParseException(Throwable throwable) {
        super(throwable);
    }
}
