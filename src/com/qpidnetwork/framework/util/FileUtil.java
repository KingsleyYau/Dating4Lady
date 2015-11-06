package com.qpidnetwork.framework.util;

import java.io.File;

import android.content.Context;
import android.os.Environment;

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
}
