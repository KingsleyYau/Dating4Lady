package com.qpidnetwork.framework.util;

import android.view.View;
import android.view.View.MeasureSpec;

public class ViewTools {
	
	/**
	 * 预计算view大小
	 * @param view
	 */
	public static void PreCalculateViewSize(View view) {
		view.setDrawingCacheEnabled(false);	
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
	}
	
}
