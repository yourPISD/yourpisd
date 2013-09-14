package com.sunstreaks.mypisd;

import java.util.List;

import android.app.Application;
import android.support.v4.app.Fragment;

import com.sunstreaks.mypisd.net.DataGrabber;

public class MyApplication extends Application {
	
	public DataGrabber dg;
	public List<Fragment> mFragments;
	
}
