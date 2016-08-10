package com.qpidnetwork.request;

import java.util.List;

import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem.DeviceType;


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
	
	/**
	 * 查询男士聊天历史
	 * @param targetId	对方ID
	 * @return
	 */
	public static native long GetChatList(String targetId, OnLCGetChatListCallback callback);
	
	/**
	 * 查询聊天消息列表
	 * @param inviteId	邀请ID
	 * @return
	 */
	public static native long QueryChatRecord(String inviteId, OnQueryChatRecordCallback callback);
	
	/**
	 * 获取私密照列表
	 * @param sid		sid
	 * @param userId	用户ID
	 * @return
	 */
	public static native long GetPhotoList(String sid, String userId, OnLCGetPhotoListCallback callback);
	
	/**
	 * 发送私密照片
	 * @param targetId	接收方ID
	 * @param inviteId	邀请ID
	 * @param sid		sid
	 * @param userId	用户ID
	 * @param photoId	待发的照片ID	
	 * @return
	 */
	static public native long SendPhoto(String targetId, String inviteId, String photoId, String sid, String userId, OnLCSendPhotoCallback callback);
	
	/**
	 * 检测女士是否可发私密照
	 * @param userId	对方ID
	 * @param inviteId	邀请ID
	 * @param photoId	照片ID
	 * @param sid		sid
	 * @param userId	用户ID
	 * @return
	 */
	public static native long CheckSendPhoto(String targetId, String inviteId, String photoId, String sid, String userId, OnLCCheckSendPhotoCallback callback);
	
	/**
	 * 获取类型
	 */
	public enum ToFlagType {
		/**
		 * 女士获取男士
		 */
		WomanGetMan,
		/**
		 * 男士获取女士
		 */
		ManGetWoman,
		/**
		 * 女士获取自己
		 */
		WomanGetSelf,
		/**
		 * 男士获取自己
		 */
		ManGetSelf
	}
	
	/**
	 * 照片尺寸
	 */
	public enum PhotoSizeType {
		/**
		 * 大图
		 */
		Large,
		/**
		 * 中图
		 */
		Middle,
		/**
		 * 小图
		 */
		Small,
		/**
		 * 原图
		 */
		Original
	}
	
	/**
	 * 照片类型
	 */
	public enum PhotoModeType {
		/**
		 * 模糊
		 */
		Fuzzy,
		/**
		 * 清晰
		 */
		Clear
	}

	/**
	 * 获取对方私密照片
	 * @param toFlag	获取类型
	 * @param targetId	照片所有者ID
	 * @param sid		sid
	 * @param userId	用户ID
	 * @param photoId	照片ID
	 * @param sizeType	照片尺寸
	 * @param modeType	照片类型
	 * @param filePath	照片文件路径
	 * @return
	 */
	static public long GetPhoto(ToFlagType toFlag, String targetId, String sid, String userId, String photoId, PhotoSizeType sizeType, PhotoModeType modeType, String filePath, OnLCGetPhotoCallback callback) {
		return GetPhoto(toFlag.ordinal(), targetId, userId, sid, photoId, sizeType.ordinal(), modeType.ordinal(), filePath, callback);
	} 
	static protected native long GetPhoto(int toFlag, String targetId, String userId, String sid, String photoId, int sizeType, int modeType, String filePath, OnLCGetPhotoCallback callback);
	
	/**
	 * 上传语音文件
	 * @param voiceCode		语音验证码
	 * @param inviteId		邀请ID
	 * @param userId		自己的用户ID
	 * @param targetId		对方的用户ID
	 * @param siteId		站点ID		
	 * @param fileType		文件类型(mp3, aac...)
	 * @param voiceLength	语音时长
	 * @param filePath		语音文件路径
	 * @param callback
	 * @return
	 */
	static public native long UploadVoice(
			String voiceCode
			, String inviteId
			, String userId
			, String targetId
			, String siteId
			, String fileType
			, int voiceLength
			, String filePath
			, OnLCUploadVoiceCallback callback);
	
	/**
	 * 下载语音文件
	 * @param voiceId	语音ID
	 * @param siteId	站点ID
	 * @param filePath	文件路径
	 * @param callback
	 * @return
	 */
	static public native long PlayVoice(String voiceId, String siteId, String filePath, OnLCPlayVoiceCallback callback);
	
	/**
	 * 5.13.	获取微视频列表
	 * @param sid		sid
	 * @param userId	用户ID
	 * @return
	 */
	public static native long GetVideoList(String sid, String userId, OnLCGetVideoListCallback callback);
	
	/**
	 * 获取类型
	 */
	public enum VideoPhotoType {
		Default,
		Big,
	}
	
	/**
	 * 5.14.	获取微视频图片
	 * @param sid			sid
	 * @param userId		用户ID
	 * @param womanId		女士ID
	 * @param videoid		视频ID
	 * @param type			图片尺寸<VideoPhotoType>
	 * @param filePath		文件路径
	 * @param callback
	 * @return				请求唯一Id
	 */
	static public long GetVideoPhoto(
			String womanId, 
			String videoid,
			VideoPhotoType type,
			String sid,
			String userId,
			String filePath,
			OnLCGetVideoPhotoCallback callback
			) {
		return GetVideoPhoto(womanId, videoid, type.ordinal(), sid, userId, filePath, callback);
	}
	static protected native long GetVideoPhoto(
			String womanId, 
			String videoid,
			int type,
			String sid,
			String userId,
			String filePath,
			OnLCGetVideoPhotoCallback callback
			);
	
	/**
	 * 获取类型
	 */
	public enum VideoToFlagType {
		Woman,
		Man,
	}
	
	/**
	 * 5.15.	获取微视频文件URL
	 * @param targetId		对方ID
	 * @param videoid		视频ID
	 * @param inviteid		邀请ID
	 * @param toflag		客户端类型<VideoToFlagType>
	 * @param sendid		发送ID，在LiveChat收到女士端发出的消息中
  	 * @param sid			sid
	 * @param userId		用户ID
	 * @param callback
	 * @return				请求唯一Id
	 */
	static public long GetVideo(
			String targetId, 
			String videoid,
			String inviteid,
			VideoToFlagType toflag,
			String sendid,
			String sid,
			String userId,
			OnLCGetVideoCallback callback
			) {
		return GetVideo(targetId, videoid, inviteid, toflag.ordinal(), sendid, sid, userId, callback);
	}
	static protected native long GetVideo(
			String targetId, 
			String videoid,
			String inviteid,
			int toflag,
			String sendid,
			String sid,
			String userId,
			OnLCGetVideoCallback callback
			);
	
	/**
	 * 5.16.	检测女士是否可发微视频
	 * @param targetId		对方ID
	 * @param videoid		视频ID
	 * @param inviteid		邀请ID
	 * @param sid			sid
	 * @param userId		用户ID
	 * @return
	 */
	public static native long CheckSendVideo(
			String targetId, 
			String videoid,
			String inviteid,
			String sid, 
			String userId, 
			OnLCCheckSendVideoCallback callback
			);
	
	/**
	 * 5.17.	发送微视频
	 * @param targetId		对方ID
	 * @param videoid		视频ID
	 * @param inviteid		邀请ID
	 * @param sid			sid
	 * @param userId		用户ID
	 * @return
	 */
	public static native long SendVideo(
			String targetId, 
			String videoid,
			String inviteid,
			String sid, 
			String userId, 
			OnLCSendVideoCallback callback
			);
	
	
	/**
	 * 功能点列表
	 */
	public enum FunctionType{
		CHAT_TEXT, 
		CHAT_VIDEO,
		CHAT_EMOTION,
		CHAT_TRYTIKET,
		CHAT_GAME,
		CHAT_VOICE,
		CHAT_MAGICICON,
		CHAT_PRIVATEPHOTO,
		CHAT_SHORTVIDEO; 
	}
	
	/**
	 * 5.18检测功能是否开通
	 * @param functionList 待检测功能列表
	 * @param type 设备类型
	 * @param versionCode 待检测版本号
	 * @param user_sid session id
	 * @param user_id 用户ID
	 * @return
	 */
	public static long CheckFunctions(List<FunctionType> functionList,
				DeviceType type, String versionCode, String user_sid, String user_id,
				OnCheckFunctionsCallback callback){
		int[] functions = null;
		if(functionList != null){
			functions = new int[functionList.size()];
			for(int i=0; i<functionList.size(); i++){
				functions[i] = functionList.get(i).ordinal();
			}
		}
		return CheckFunctions(functions, type.ordinal(), versionCode, user_sid, user_id, callback);
	}
	
	static protected native long CheckFunctions( int[] functionIds, int deviceType, String versionCode, String user_sid, String user_id, OnCheckFunctionsCallback callback);
}
