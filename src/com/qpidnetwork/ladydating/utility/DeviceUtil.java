package com.qpidnetwork.ladydating.utility;

import com.qpidnetwork.ladydating.QpidApplication;

import android.graphics.Point;
import android.util.DisplayMetrics;

public class DeviceUtil {
	
	public static Point getScreenSize(){
		Point size = new Point();
		DisplayMetrics display = QpidApplication.getProcess().getResources().getDisplayMetrics();
		size.x = display.widthPixels;
		size.y = display.heightPixels;

		return size;
	}

}
