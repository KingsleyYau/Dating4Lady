package com.qpidnetwork.ladydating.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;

public class FileUtil {
	
	
	/**
	 * 
	 * @param srcBmp  original image bitmap
	 * @param dsFileUrl  destination file url
	 * @param quality  quality from 1 - 100
	 * @param override  delete if destination exists.
	 */
	public static boolean writeBitmapToFile(Bitmap srcBmp, String dsFileUrl, boolean override){
		if (override) new File(dsFileUrl).delete();
		return writeBitmapToFile(srcBmp, dsFileUrl, 100, Bitmap.CompressFormat.PNG);
	}
	
	public static boolean writeBitmapToFile(Bitmap srcBmp, String dsFileUrl){
		return writeBitmapToFile(srcBmp, dsFileUrl, 100, Bitmap.CompressFormat.PNG);
	}
	
	public static boolean writeBitmapToFile(Bitmap srcBmp, String dsFileUrl, int quality, Bitmap.CompressFormat format){
		FileOutputStream out = null;
		
		try {
		    out = new FileOutputStream(dsFileUrl);
		    srcBmp.compress(format, quality, out);
		    out.close();
		    return true;
		} catch (Exception e) {
			//exception
		}
		
		if (out != null)
			try {
				out.close();
			} catch (IOException e) {
		}
		
		return false;
	}

}
