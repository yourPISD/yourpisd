package app.sunstreak.yourpisd.net;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import app.sunstreak.yourpisd.util.HTTPResponse;
import app.sunstreak.yourpisd.util.Request;

public class StudentSession extends Session {

    public StudentSession(String username, String password) {
        this.username = username;
        this.password = password;
        this.domain = Domain.STUDENT;
    }

    @Override
    public int login() throws IOException
    {
        final URL LOAD_URL = new URL(LOGON);
        final String USERNAME_FIELD = "ctl00$ContentPlaceHolder$Username";
        final String PASSWORD_FIELD = "ctl00$ContentPlaceHolder$Password";

        try
        {
            // Submitting form data.
            Document html = Jsoup.parse(LOAD_URL, 60000);
            FormElement form = (FormElement) html.getElementsByTag("form").get(0);
            Connection conn = form.submit();

            // Find username and password field.
            Connection.KeyVal usernameField = null;
            Connection.KeyVal passwordField = null;
            for (Connection.KeyVal field : conn.request().data())
                if (field.key().equals(USERNAME_FIELD))
                    usernameField = field;
                else if (field.key().equals(PASSWORD_FIELD))
                    passwordField = field;

            if (usernameField == null)
            {
                System.err.println("EXPECTED: Form field for username.");
                return -2;
            }

            if (passwordField == null)
            {
                System.err.println("EXPECTED: Form field for password.");
                return -2;
            }

            // Submit username and password
            usernameField.value(username);
            passwordField.value(password);
            Connection.Response resp = conn.execute();

            if (resp.url().equals(LOAD_URL))
                return -1;
            else
            {
                System.out.println(resp.cookies());
                return 1;
            }
        }
        catch (SocketTimeoutException e)
        {
            e.printStackTrace();
            return -2;
        }
    }

    @Override
    public boolean logout() {
        try {
            return Jsoup.connect(LOGOFF).timeout(60000).cookies(cookies).execute()
                    .statusCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }
}
