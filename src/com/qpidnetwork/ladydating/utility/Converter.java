package com.qpidnetwork.ladydating.utility;

import com.qpidnetwork.ladydating.QpidApplication;

public class Converter {
	
	public static int dp2px(float dp){
		return (int)(dp * QpidApplication.getProcess().getResources().getDisplayMetrics().density);
	}
}
