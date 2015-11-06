package com.qpidnetwork.request;

public class RequestJniLivechat {
	
	/**
	 * 5.1.查询个人邀请模板列表
	 * @param callback
	 * @return
	 */
	public static native long GetMyCustomTemplate(OnLCCustomTemplateCallback callback);
	
	/**
	 * 5.2.获取系统模板列表
	 */
	public static native long GetSystemTemplate(OnLCSystemTemplateCallback callback);
	
	
	/**
	 * 5.3.新建邀请模板
	 * @param tempContent
	 * @param callback
	 * @return
	 */
	public static native long AddCustomTemplate(String tempContent, OnRequestCallback callback);
	
	/**
	 * 5.4.删除用于自定义模板接口
	 * @param tempId
	 * @param callback
	 * @return
	 */
	public static native long DelCustomTemplates(String tempId, OnRequestCallback callback);
}
