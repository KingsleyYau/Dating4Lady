package com.qpidnetwork.request;

public class ConfigManagerJni {
	/**
	 * 增加回调
	 * @param callback
	 */
	static public native void AddCallback(OnConfigManagerCallback callback);
	
	/**
	 * 增加回调
	 * @param callback
	 */
	static public native void RemoveCallback(OnConfigManagerCallback callback);
	
    /**
     * 同步配置
     * @return
     */
    static public native void Sync();
}
