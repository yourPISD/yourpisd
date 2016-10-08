package app.sunstreak.yourpisd;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import static app.sunstreak.yourpisd.MainActivity.UserLogoutTask;
public class CreditActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //Login if session has been cleared.
        if (((YPApplication) getApplication()).session == null)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        ((YPApplication)getApplication()).startingInternal = true;
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.credit, menu);
        return true;
    }

    private void logout()
    {
        YPApplication app = ((YPApplication) getApplication());
        if (app.session != null)
        {
            UserLogoutTask logout = new UserLogoutTask();
            logout.execute(app.session);
            app.session = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Only log out if we are not exiting internally.
        YPApplication app = (YPApplication)getApplication();
        if (app.startingInternal)
            app.startingInternal = false;
        else
            logout();
    }
}
