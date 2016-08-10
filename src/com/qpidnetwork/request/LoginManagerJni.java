package com.qpidnetwork.request;

public class LoginManagerJni {
	
	/**
	 * 设置回调
	 * @param callback
	 */
	static public native void SetLoginCallback(OnLoginManagerCallback callback);
	
    /**
     * 登录
     * @param email				电子邮箱
     * @param password			密码
     * @param deviceId			设备唯一标识
     * @param versioncode		客户端内部版本号
     * @param model				移动设备型号
     * @param manufacturer		制造厂商
     * @return
     */
    static public native void Login(
    		String email, 
    		String password, 
    		String deviceId, 
    		String model, 
    		String manufacturer
    		);
    
    /**
     * 注销
     * @param type 0:手动  1：自动
     */
    static public native void Logout(int type);
    
    static public native String GetUser();
    
    static public native String GetPassword();
}
