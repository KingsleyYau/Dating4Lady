package com.qpidnetwork.framework.util;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;

import android.content.Context;

public class Log {

	/**
	 * 日志级别 <code>
	 * {@link android.util.Log#VERBOSE}
	 * {@link android.util.Log#DEBUG}
	 * {@link android.util.Log#INFO}
	 * {@link android.util.Log#WARN}
	 * {@link android.util.Log#ERROR}
	 * </code>
	 */
	private static int LOG_LEVEL = android.util.Log.DEBUG;

	private static boolean VERBOSE = false;
	private static boolean DEBUG = false;
	private static boolean INFO = false;
	private static boolean WARN = false;
	private static boolean ERROR = false;
	
	/**
	 * file log param
	 */
	private static String mDirPath = "";
	private static Context mContext = null;
	private static boolean mIsWriteFileLog = false;

	static {
		switch (LOG_LEVEL) {
		case android.util.Log.VERBOSE:
			VERBOSE = true;
		case android.util.Log.DEBUG:
			DEBUG = true;
		case android.util.Log.INFO:
			INFO = true;
		case android.util.Log.WARN:
			WARN = true;
		case android.util.Log.ERROR:
			ERROR = true;
		}
	}
	
	/**
	 * 设置log级别
	 * @param level	log级别（如：android.util.Log.DEBUG等）
	 */
	public static void SetLevel(int level)
	{
		LOG_LEVEL = level;
	}

	public static void v(String tag, String msg, Object... args) {
		if (VERBOSE) {
			android.util.Log.v(tag, String.format(msg, args));
		}
	}

	public static void d(String tag, String msg, Object... args) {
		if (DEBUG) {
			String m = String.format(msg, args);
			// m = (m.length() > 300 ? m.substring(0, 300) : m);
			android.util.Log.d(tag, m);
		}
	}

	public static void i(String tag, String msg, Object... args) {
		if (INFO) {
			android.util.Log.i(tag, String.format(msg, args));
		}
	}

	public static void w(String tag, String msg, Object... args) {
		if (WARN) {
			android.util.Log.w(tag, String.format(msg, args));
		}
	}

	public static void w(String tag, String msg, Throwable tr, Object... args) {
		if (WARN) {
			android.util.Log.w(tag, String.format(msg, args), tr);
		}
	}

	public static void e(String tag, String msg, Object... args) {
		if (ERROR) {
			android.util.Log.e(tag, String.format(msg, args));
		}
	}

	public static void e(String tag, String msg, Throwable tr, Object... args) {
		if (ERROR) {
			android.util.Log.e(tag, String.format(msg, args), tr);
		}
	}
	
	public static boolean initFileLog(String dirPath, Context context) {
		boolean result = false;
		if (!dirPath.isEmpty() && null != context) {
			mDirPath = dirPath;
			mContext = context;
			result = true;
		}
		return result;
	}
	
	public static void setWriteFileLog(boolean isWrite)
	{
		mIsWriteFileLog = isWrite;
	}
	
	public static void file(String tag, String msg, Object... agrs) {
		writeLog(mContext, mDirPath, tag, String.format(msg, agrs));
	}
	
	private static void writeLog(Context context, String dirPath, String tag, String strLog){
		if(!dirPath.isEmpty()  
			&& null != context
			&& mIsWriteFileLog)
		{
			final String LASTNAME_OF_LOGFILE = ".log";
			try {
				String strLogToFile = "";
		        // 系统时间
	            Calendar cal = Calendar.getInstance();
	            int year = cal.get(Calendar.YEAR);
	            int month = cal.get(Calendar.MONTH)+1;
	            int day = cal.get(Calendar.DAY_OF_MONTH);

	            int hour = cal.get(Calendar.HOUR_OF_DAY);
	            int minute = cal.get(Calendar.MINUTE);
	            int second = cal.get(Calendar.SECOND);
	            int milsecond = cal.get(Calendar.MILLISECOND);

	            strLogToFile = String.format("(%d-%02d-%02d %02d:%02d:%02d.%03d) [%s] %s\n"
	            		, year, month, day, hour, minute, second, milsecond
	            		, tag, strLog);

	            // 文件名
	            String packageName = context.getPackageName();
	            String fileName = packageName + "_" + year +"_"+ month + "_" + day + LASTNAME_OF_LOGFILE;
	            // 文件夹
	            String pathName = dirPath;

	            File path = new File(pathName);
	            File file = new File(pathName + fileName);
	            if(!path.exists()) {
	            	path.mkdirs();
	            }
	            if(!file.exists()) {
					file.createNewFile();
	            }
	            // 写文件
	            FileWriter writer = new FileWriter(file, true);
	            writer.write(strLogToFile);
	            writer.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}