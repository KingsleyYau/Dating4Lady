package com.qpidnetwork.request;

public class RequestJniOther {
	/**
	 * 修改密码
	 * 
	 * @param oldPassword
	 * @param newPassword
	 * @param callback
	 * @return
	 */
	public static native long ModifyPassword(String oldPassword,
			String newPassword, OnRequestCallback callback);

	/**
	 * 查询高级表情配置
	 * 
	 * @param callback
	 * @return
	 */
	public static native long EmotionConfig(
			OnOtherEmotionConfigCallback callback);

	public enum ActionType {
		SETUP, // 新安装
		NEWUSER // 新用户
	}

	/**
	 * 收集手机硬件信息
	 * 
	 * @param userAccount
	 *            userAccount（格式：[会员类型]:[用户ID]|[会员类型]:[用户ID
	 *            ]，多个用"|"隔开）（会员类型：P2：cl，P3：ida，P4：cd，P12：ld）
	 * @param verName
	 *            客户端显示版本号
	 * @param action
	 *            新用户类型（1：新安装，2：新用户）
	 * @param siteId
	 *            站点ID
	 * @param width
	 *            屏幕宽度
	 * @param height
	 *            屏幕高度
	 * @param deviceId
	 *            设备唯一标识
	 * @param callback
	 * @return
	 */
	public static long PhoneInfo(String userAccount, String verName,
			ActionType action, int siteId, int width, int height,
			String deviceId, OnRequestCallback callback) {
		return PhoneInfo(userAccount, verName, action.ordinal(), siteId, width,
				height, deviceId, callback);
	}

	protected static native long PhoneInfo(String userAccount, String verName,
			int action, int siteId, int width, int height, String deviceId,
			OnRequestCallback callback);

	/**
	 * 检查客户端更新
	 * @param callback
	 * @return
	 */
	public static native long VersionCheck(OnVersionCheckCallback callback);

	/**
	 * 同步配置
	 * 
	 * @param callback
	 * @return
	 */
	static public native long SynConfig(OnSynConfigCallback callback);


	/**
	 * 上传错误日志
	 * 
	 * @param deviceId
	 *            设备ID
	 * @param directory
	 *            错误日志目录
	 * @param tmpDicectory
	 *            临时目录
	 * @param callback
	 * @return
	 */
	public static native long UploadCrashLog(String deviceId, String directory,
			String tmpDicectory, OnRequestCallback callback);

	/**
	 * 获取机构信息
	 * @param callback
	 * @return
	 */
	public static native long GetAgentInfo(OnOtherGetAgentInfoCallback callback);
	
	/**
	 * 查询个人资料
	 * @param callback
	 * @return
	 */
	public static native long QueryMyProfile(OnQueryMyProfileCallback callback);
}
