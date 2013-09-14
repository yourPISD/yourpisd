package com.sunstreaks.mypisd;

import java.util.List;

import android.app.Application;
import android.support.v4.app.Fragment;

import com.sunstreaks.mypisd.net.DataGrabber;

public class YourPISDApplication extends Application {
	
	private DataGrabber dg;
	public List<Fragment> mFragments;
	
	public DataGrabber getDataGrabber() {
		return dg;
	}
	
	public void setDataGrabber(DataGrabber dg) {
		this.dg = dg;
	}
}
