package com.qpidnetwork.livechat;

import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.OnLCCheckSendVideoCallback;
import com.qpidnetwork.request.RequestJniLivechat.VideoPhotoType;
import com.qpidnetwork.request.item.LCVideoListGroupItem;
import com.qpidnetwork.request.item.LCVideoListVideoItem;

/**
 * LiveChat微视频回调接口类
 * @author Samson Fan
 *
 */
public interface LiveChatManagerVideoListener {
	// ---------------- 图片回调函数(Private Album) ----------------
	/**
	 * 检测视频是否可发送
	 * @param errType	处理结果错误代码
	 * @param result	是否可发送结果
	 * @param errno		php返回错误代码
	 * @param errmsg	php返回错误描述
	 * @param userItem	用户item
	 * @param videoItem	视频item
	 */
	public void OnCheckSendVideo(LiveChatErrType errType, OnLCCheckSendVideoCallback.ResultType result, 
			String errno, String errmsg, LCUserItem userItem, LCVideoItem videoItem);
	
	/**
	 * 发送视频回调
	 * @param errType	处理结果错误代码
	 * @param errno		上传文件的错误代码
	 * @param errmsg	处理结果描述
	 * @param item		消息item
	 * @return
	 */
	public void OnSendVideo(LiveChatErrType errType, String errno, String errmsg, LCMessageItem item);
	
	/**
	 * 获取视频图片回调
	 * @param errType	处理结果错误代码
	 * @param errno		下载请求失败的错误代码
	 * @param errmsg	处理结果描述
	 * @param photoType	图片类型
	 * @param item		视频item
	 * @return
	 */
	public void OnGetVideoPhoto(LiveChatErrType errType, String errno, String errmsg, VideoPhotoType photoType, LCVideoItem item);
	
	/**
	 * 获取视频回调
	 * @param errType	处理结果错误代码
	 * @param errno		下载请求失败的错误代码
	 * @param errmsg	处理结果描述
	 * @param videoItem	视频item
	 */
	public void OnGetVideo(LiveChatErrType errType, String errno, String errmsg, LCMessageItem item);
	
	/**
	 * 接收视频被查看回调
	 * @param userItem	用户item
	 * @param videoId	视频ID
	 * @param videoDesc	视频描述
	 */
	public void OnRecvShowPhoto(LCUserItem userItem, String videoId, String videoDesc);
	
	/**
	 * 获取视频列表回调
	 * @param isSuccess	是否成功
	 * @param errno		错误代码
	 * @param errmsg	错误描述
	 * @param groups	视频组数组
	 * @param videos	视频数组
	 */
	public void OnGetVideoList(boolean isSuccess, String errno, String errmsg, LCVideoListGroupItem[] groups, LCVideoListVideoItem[] videos);
	
	/**
	 * 接收视频被查看回调
	 * @param userItem		用户item
	 * @param videoId		视频ID
	 * @param videoDesc		视频描述
	 */
	public void OnRecvShowVideo(LCUserItem userItem, String videoId, String videoDesc);
}
