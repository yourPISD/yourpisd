package app.sunstreak.yourpisd;

import android.app.Application;

import app.sunstreak.yourpisd.net.Session;

public class YPApplication extends Application {

    public Session session;
    public boolean startingInternal; //true if we are starting an internal activity.
                                      //It will reset itself once the other activity has
                                      //stopped.
}
