package com.qpidnetwork.tool;

public class ImageHandler {
	static {
		try {
			System.loadLibrary("imghandle-interface");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 设置log目录路径 
	 * @param logPath	log目录路径	
	 */
	static public native void SetLogPath(String logPath);
	
	/**
	 * 把高级表情png图片裁剪成一张张小图片
	 * @param path	高级表情png图片路径
	 * @return
	 */
	static synchronized public native boolean ConvertEmotionPng(String path);
}
