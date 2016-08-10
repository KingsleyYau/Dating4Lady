package com.qpidnetwork.request;


/**
 * 非协议的公共请求接口（如：翻译接口）
 * @author Samson Fan
 *
 */
public class RequestJniCommon 
{
	/**
	 * 翻译文本
	 * @param appId		翻译的AppId
	 * @param from		源语言缩写(如：en)(若为null或空字符串则表示不指定源语言)
	 * @param to		目标语言缩写(如：en)
	 * @param text		待翻译文字
	 * @param callback
	 * @return
	 */
	public static native long TranslateText(String appId, String from, String to, String text, OnTranslateTextCallback callback);
}
