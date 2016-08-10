package com.qpidnetwork.framework.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

public class FileUtil {
	
	
	/**
	 * 判断是否存在应用外部存储
	 * 
	 * @return
	 */
	public static boolean existSdcard() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	/**
	 * 获取持久化目录
	 * 
	 * @param context
	 * @param type 如："aaa/bbb"
	 * @return 如果不存在SD卡，则返回null, 不建议持久化文件在应用目录下
	 */
	public static File getStoreDir(Context context, String type) {
		if (existSdcard()) {
			return context.getExternalFilesDir(type);
		}
		return null;
	}

	/**
	 * 获取持久化文件
	 * 
	 * @param context
	 * @param type
	 * @param fileName
	 * @return
	 */
	public static File getStoreFile(Context context, String type, String fileName) {
		File dir = getStoreDir(context, type);
		File file = new File(dir, fileName);
		return file;
	}
	
	/**
	 * 获取指定文件后缀名
	 * @param file
	 * @return
	 */
	public static String getSuffix(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }
        String fileName = file.getName();
        if (fileName.equals("") || fileName.endsWith(".")) {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return null;
        }
	}

	/**
	 * 获取指定文件MIMETYPE
	 * @param file
	 * @return
	 */
	public static String getMimeType(File file){
		String suffix = getSuffix(file);
		if (suffix == null) {
			return "file/*";
		}
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
		if (type != null || !type.isEmpty()) {
			return type;
		}
		return "file/*";
	}
	
	/**
	 * 检测是否为*.wmv;*.avi;*.mp4;*.3gp格式的视频文件
	 * @param filePath
	 * @return
	 */
	public static boolean isValidVideo(String filePath){
		boolean isValid = false;
		if(!TextUtils.isEmpty(filePath)
				&& new File(filePath).exists()){
			String suffix = getSuffix(new File(filePath));
			if(!TextUtils.isEmpty(suffix)
					&& (suffix.equals("wmv")
							|| suffix.equals("avi")
							|| suffix.equals("mp4")
							|| suffix.equals("3gp"))){
				isValid = true;
			}
		}
		return isValid;
	}
	
	/**
	 * 
	 * @param srcBmp  original image bitmap
	 * @param dsFileUrl  destination file url
	 * @param quality  quality from 1 - 100
	 * @param override  delete if destination exists.
	 */
	public static boolean writeBitmapToFile(Bitmap srcBmp, String dsFileUrl, boolean override){
		if (override) new File(dsFileUrl).delete();
		return writeBitmapToFile(srcBmp, dsFileUrl, 100, Bitmap.CompressFormat.JPEG);
	}
	
	public static boolean writeBitmapToFile(Bitmap srcBmp, String dsFileUrl){
		return writeBitmapToFile(srcBmp, dsFileUrl, 100, Bitmap.CompressFormat.JPEG);
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
