package com.qpidnetwork.request;

import android.telephony.TelephonyManager;

import com.qpidnetwork.request.item.CookiesItem;

/**
 * @author Max.Chiu
 * 公共设置模块,设置的参数对所有接口请求都有效
 */
public class RequestJni {
	static {
		try {
			System.loadLibrary("httprequest");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 无效返回值
	 */
	static public final long InvalidRequestId = -1;
	
	/**
	 * 设置日志目录
	 * @param directory		日志目录
	 */
	static public native void SetLogDirectory(String directory);
	
	/**
	 * 设置客户端版本号
	 * @param version		客户端版本号
	 */
	static public native void SetVersionCode(String version);
	
	/**
	 * 设置cookies存放路径
	 * @param directory
	 */
	static public native void SetCookiesDirectory(String directory);
	
	/**
	 * 设置主站
	 * @param webSite	web站域名	("http://demo.chnlove.com")
	 * @param appSite	app站域名	("http://demo-mobile.chnlove.com")
	 */
    static public native void SetWebSite(String webSite, String appSite);

    /**
     * 设置翻译站点
     * @param transSite	翻译站点
     */
    static public native void SetTransSite(String transSite);
    
    /**
     * 设置 Video上传站点
     * @param videoUploadSite	Video上传站点
     */
    static public native void SetVideoUploadSite(String videoUploadSite);
    
	/**
	 * 设置认证
	 * @param user		账号
	 * @param password  密码
	 */
    static public native void SetAuthorization(String user, String password);
    
	/**
	 * 清楚cookiess
	 */
    static public native void CleanCookies();
    
    /**
     * 获取指定站点cookies
     * @param site		域名(例如demo-mobile.chnlove.com)
     * @return
     */
    static public native String GetCookies(String site);
    
    /**
     * 获取所有cookiesItem
     * @return
     */
    static public native CookiesItem[] GetCookiesItem();
    
    /**
     * 停止请求
     * @param requestId		请求Id
     */
    static public native void StopRequest(long requestId);
    
    /**
     * 设置请求完成回调
     * @param callback
     */
    static public native void SetRequestFinishCallback(OnRequestFinishCallback callback);
    
    /**
     * 停止所有请求
     */
    static public native void StopAllRequest();
    
    /**
     * 获取设备唯一Id
     * @param	tm		电话管理类<TelephonyManager>
     * @return			设备唯一Id
     */
    static public String GetDeviceId(TelephonyManager tm) {
		String uniqueId = ((TelephonyManager) tm).getDeviceId();
		if( uniqueId == null || 
				uniqueId.length() == 0 || 
				uniqueId.compareTo("000000000000000") == 0 ) {
			uniqueId = GetDeviceId();
		}
    	return uniqueId;
    }
    static protected native String GetDeviceId();
    
    /**
	 * 设置设备唯一Id
	 * @param tm		电话管理类<TelephonyManager>
	 */
    static public void SetDeviceId(TelephonyManager tm) {
    	String deviceId = GetDeviceId(tm);
    	SetDeviceId(deviceId);
    }
	static protected native void SetDeviceId(String deviceId);
	
    /**
     * 获取返回的body总长度（字节）
     * @param task		请求Id
     * @return
     */
    static public native int GetDownloadContentLength(long task);
    
    /**
     * 获取已收的body长度（字节）
     * @param task		请求Id
     * @return
     */
    static public native int GetRecvLength(long task);
    
    /**
     * 获取请求的body总长度（字节）
     * @param task		请求Id
     * @return
     */
    static public native int GetUploadContentLength(long task);
    
    /**
     * 获取已发的body长度（字节）
     * @param task		请求Id
     * @return
     */
    static public native int GetSendLength(long task);
}
