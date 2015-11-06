package com.qpidnetwork.ladydating.utility;

import java.util.Random;

import com.qpidnetwork.ladydating.QpidApplication;
import com.qpidnetwork.ladydating.R;

import android.graphics.drawable.Drawable;
import android.os.Build;

public class DrawableUtil {
	
	public static void changeDrawableColor(Drawable drawable, int distinationColor){
		drawable.setColorFilter(distinationColor, android.graphics.PorterDuff.Mode.MULTIPLY);
	}
	
	public static Drawable getDrawable(int resourceId){
		if (Build.VERSION.SDK_INT >= 21){
			return QpidApplication.getProcess().getDrawable(resourceId);
		}else{
			return QpidApplication.getProcess().getResources().getDrawable(resourceId);
		}
	}
	
	public static Drawable getDrawable(int resourceId, int convertToColor){
		Drawable drawable;
		if (Build.VERSION.SDK_INT >= 21){
			drawable = QpidApplication.getProcess().getDrawable(resourceId);
		}else{
			drawable = QpidApplication.getProcess().getResources().getDrawable(resourceId);
		}
		
		changeDrawableColor(drawable, convertToColor);
		return drawable;
	}
	
	public static int[] getBrandingColorList(){
		return new int[]{
				R.color.brand_color_light11,
				R.color.brand_color_light12,
				R.color.brand_color_light13,
				R.color.brand_color_light14,
				R.color.brand_color_light15,
		};
	}
	
	public static int getRandomBrandingColor(){
		Random random = new Random();
		int[] colors = getBrandingColorList();
		return QpidApplication.getProcess().getResources().getColor(colors[random.nextInt(colors.length)]);
	}

}
