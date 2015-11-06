package com.qpidnetwork.livechat.jni;

/**
 * LiveChat客户端
 * @author Samson Fan
 */
public class LiveChatClient {
	static {
		try {
			System.loadLibrary("livechat-interface");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 客户端类型
	 */
	public enum ClientType {
		CLIENT_UNKNOW,	// 未知
		CLIENT_PC,		// PC端
		CLIENT_PC_JAVA,	// PC Java端
		CLIENT_PC_APP,	// PC app端
		CLIENT_PC_PAD,	// PC pad端
		CLIENT_ANDROID,	// Android客户端
		CLIENT_IPHONE,	// IPhone客户端
		CLIENT_IPAD,	// IPad客户端
		CLIENT_WEB,		// Web端
	}
	
	/**
	 * 用户性别
	 */
	public enum UserSexType{
		USER_SEX_UNKNOW,	// 未知
		USER_SEX_FEMALE,	// 女士
		USER_SEX_MALE,		// 男士
	}
	
	/**
	 * 用户在线状态
	 */
	public enum UserStatusType {
		USTATUS_UNKNOW,				// 未知
		USTATUS_OFFLINE_OR_HIDDEN,	// 离线或隐身
		USTATUS_ONLINE,		// 在线
	}
	
	/**
	 * 设置日志目录
	 * @param directory		日志目录
	 */
	static public native void SetLogDirectory(String directory);
	
	/**
	 * 初始化
	 * @param ips	LiveChat服务器ip列表
	 * @param port	LiveChat服务器端口
	 * @return
	 */
	static public native boolean Init(LiveChatClientListener listener, String[] ips, int port);
	
	/**
	 * 判断是否无效seq
	 * @param seq
	 * @return
	 */
	static public native boolean IsInvalidSeq(int seq);
	
	/**
	 * 连接服务器并登录
	 * @param user			用户名
	 * @param password		php登录成功的session
	 * @param deviceId		设备ID
	 * @param clientType	客户端类型
	 * @param sexType		性别
	 * @return
	 */
	static public boolean Login(String user, String password, String deviceId, ClientType clientType, UserSexType sexType) {
		return Login(user, password, deviceId, clientType.ordinal(), sexType.ordinal());
	}
	static protected native boolean Login(String user, String password, String deviceId, int clientType, int sexType);
	
	/**
	 * 注销并断开连接
	 * @return
	 */
	static public native boolean Logout();
	
	
	/**
	 * 上传客户端版本号
	 * @param ver	客户端版本号
	 * @return
	 */
	static public native boolean UploadVer(String ver);
	
	/**
	 * 设置在线状态 
	 * @param statusType	在线状态
	 * @return
	 */
	static public boolean SetStatus(UserStatusType statusType) {
		return SetStatus(statusType.ordinal());
	}
	static protected native boolean SetStatus(int statusType);
	
	/**
	 * 获取用户在线状态
	 * @param userIds	用户ID数组
	 * @return
	 */
	static public native boolean GetUserStatus(String[] userIds);
	
	/**
	 * 获取会话信息
	 * @param userId	对方用户ID
	 * @return
	 */
	static public native boolean GetTalkInfo(String userId);
	
	/**
	 * 更新票根
	 * @param userId	对方用户ID
	 * @param ticket	票根
	 * @return
	 */
	static public native boolean UploadTicket(String userId, int ticket);
	
	/**
	 * 发送文本消息
	 * @param userId	对方用户ID
	 * @param message	聊天文本消息
	 * @param illegal	是否触犯风控
	 * @param ticket	票根
	 * @return
	 */
	static public native boolean SendMessage(String userId, String message, boolean illegal, int ticket);
	
	/**
	 * 发送高级表情
	 * @param userId	对方用户ID
	 * @param emotionId	高级表情ID
	 * @param ticket	票根
	 * @return
	 */
	static public native boolean SendEmotion(String userId, String emotionId, int ticket);
	
	/**
	 * 获取女士语音发送验证码(new)
	 * @param userId	对方用户ID
	 * @param ticket	票根
	 * @return
	 */
	static public native boolean GetLadyVoiceCode(String userId);
	
	/**
	 * 发送语音消息
	 * @param userId	对方用户ID
	 * @param voiceId	语音ID
	 * @param length	语音时长
	 * @param ticket	票根
	 * @return
	 */
	static public native boolean SendVoice(String userId, String voiceId, int length, int ticket);
	
	/**
	 * 发送图片消息
	 * @param userId	对方用户ID
	 * @param inviteId	邀请ID
	 * @param photoId	图片ID
	 * @param sendId	图片发送ID
	 * @param charget	是否已扣费
	 * @param photoDesc	图片描述
	 * @param ticket	票根
	 * @return
	 */
	static public native boolean SendPhoto(String userId, String inviteId, String photoId, String sendId, boolean charget, String photoDesc, int ticket);
	
	/**
	 * 显示图片
	 * @param userId	对方用户ID
	 * @param inviteId	邀请ID
	 * @param photoId	图片ID
	 * @param sendId	图片发送ID
	 * @param charget	是否已扣费
	 * @param photoDesc	图片描述
	 * @param ticket	票根
	 * @return
	 */
	static public native boolean ShowPhoto(String userId, String inviteId, String photoId, String sendId, boolean charget, String photoDesc, int ticket);
	
	/**
	 * 获取用户信息
	 * @param userId	对方用户ID
	 * @return
	 */
	static public native boolean GetUserInfo(String userId);
	
	/**
	 * 获取多个用户信息(new)
	 * @param userIds	对方用户ID数组
	 * @return
	 */
	static public native int GetUsersInfo(String[] userIds);
	
	/**
	 * 获取黑名单列表
	 * @return
	 */
	static public native boolean GetBlockList();
	
	/**
	 * 获取LiveChat联系人列表
	 * @return
	 */
	static public native boolean GetContactList();
	
	/**
	 * 获取LiveChat最近联系人列表(new)
	 * @return
	 */
	static public native boolean GetRecentContactList();
	
	/**
	 * 查询在线男士(new)
	 * @param beginAge	起始年龄
	 * @param endAge	结束年龄
	 * @return
	 */
	static public native boolean SearchOnlineMan(int beginAge, int endAge);
	
	/**
	 * 回复验证码(new)
	 * @param identifyCode	验证码
	 * @return
	 */
	static public native boolean ReplyIdentifyCode(String identifyCode);
	
	/**
	 * 刷新验证码(new)
	 * @return
	 */
	static public native boolean RefreshIdentifyCode();
	
	/**
	 * 更新邀请模板(new)
	 * @return
	 */
	static public native boolean RefreshInviteTemplate();
	
	/**
	 * 获取已扣费最近联系人(new)
	 * @return
	 */
	static public native boolean GetFeeRecentContactList();
	
	/**
	 * 获取女士聊天信息(new)
	 * @return
	 */
	static public native boolean GetLadyChatInfo();
}
