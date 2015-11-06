package com.qpidnetwork.framework.util;

/**
 * 日期处理工具类
 * @author Hunter
 * @since 2015.4.25
 */
public class DateUtil {
	
	/**
	 * 将毫秒转成时分秒格式字符串
	 * @param time 原毫秒时间
	 * @return
	 */
	public static String generateTime(long time) {
		int totalSeconds = (int) (time / 1000);
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes,
				seconds) : String.format("%02d:%02d", minutes, seconds);
	}
	
	
	/**
	 * 获取分秒的时间显示
	 * 
	 * @param context
	 * @param t单位为妙
	 * @return
	 */
	public static String getTime(long t) {
		String time;
		long m = t / 60;
		long s = t % 60;
		if (m < 10) {
			time = "0" + m + ":";
		} else {
			time = m + ":";
		}
		if (s < 10) {
			time = time + "0" + s;
		} else {
			time = time + s;
		}
		return time;
	}
}
