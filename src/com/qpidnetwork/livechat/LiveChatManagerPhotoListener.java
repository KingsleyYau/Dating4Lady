package com.qpidnetwork.livechat;

import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.OnLCCheckSendPhotoCallback;
import com.qpidnetwork.request.item.LCPhotoListAlbumItem;
import com.qpidnetwork.request.item.LCPhotoListPhotoItem;

/**
 * LiveChat图片(私密照)回调接口类
 * @author Samson Fan
 *
 */
public interface LiveChatManagerPhotoListener {
	// ---------------- 图片回调函数(Private Album) ----------------
	/**
	 * 检测图片是否可发送
	 * @param errType	处理结果错误代码
	 * @param result	是否可发送结果
	 * @param errno		php返回错误代码
	 * @param errmsg	php返回错误描述
	 * @param userItem	用户item
	 * @param photoItem	图片item
	 */
	public void OnCheckSendPhoto(LiveChatErrType errType, OnLCCheckSendPhotoCallback.ResultType result, String errno, String errmsg, LCUserItem userItem, LCPhotoItem photoItem);
	
	/**
	 * 发送图片（包括上传图片文件(php)、发送图片(livechat)）回调
	 * @param errType	处理结果错误代码
	 * @param errno		上传文件的错误代码
	 * @param errmsg	处理结果描述
	 * @param item		消息item
	 * @return
	 */
	public void OnSendPhoto(LiveChatErrType errType, String errno, String errmsg, LCMessageItem item);
	
	/**
	 * 获取图片（获取对方私密照片(php)、显示图片(livechat)）回调
	 * @param errType	处理结果错误代码
	 * @param errno		下载请求失败的错误代码
	 * @param errmsg	处理结果描述
	 * @param item		消息item
	 * @return
	 */
	public void OnGetPhoto(LiveChatErrType errType, String errno, String errmsg, LCMessageItem item);
	
	/**
	 * 获取自己的图片回调
	 * @param errType	处理结果错误代码
	 * @param errno		购买/下载请求失败的错误代码
	 * @param errmsg	处理结果描述
	 * @param photoItem	图片item
	 */
	public void OnGetSelfPhoto(LiveChatErrType errType, String errno, String errmsg, LCPhotoItem photoItem);
	
	/**
	 * 接收图片消息回调
	 * @param item		消息item
	 */
	public void OnRecvPhoto(LCMessageItem item);
	
	/**
	 * 接收图片被查看回调
	 * @param userItem	用户item
	 * @param photoId	图片ID
	 * @param photoDesc	图片描述
	 */
	public void OnRecvShowPhoto(LCUserItem userItem, String photoId, String photoDesc);
	
	/**
	 * 获取私密照列表回调
	 * @param isSuccess	是否成功
	 * @param errno		错误代码
	 * @param errmsg	错误描述
	 * @param albums	相册数组
	 * @param photos	私密照数组
	 */
	public void OnGetPhotoList(boolean isSuccess, String errno, String errmsg, LCPhotoListAlbumItem[] albums, LCPhotoListPhotoItem[] photos);
}
