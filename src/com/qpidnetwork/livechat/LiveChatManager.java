package com.qpidnetwork.livechat;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.QpidApplication;
import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack;
import com.qpidnetwork.ladydating.chat.LCMessageHelper;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCMessageItem.SendType;
import com.qpidnetwork.livechat.LCMessageItem.StatusType;
import com.qpidnetwork.livechat.LCUserItem.CanSendErrType;
import com.qpidnetwork.livechat.LCUserItem.ChatType;
import com.qpidnetwork.livechat.jni.LiveChatClient;
import com.qpidnetwork.livechat.jni.LiveChatClient.ClientType;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.WebsiteManager;
import com.qpidnetwork.request.ConfigManagerJni;
import com.qpidnetwork.request.OnConfigManagerCallback;
import com.qpidnetwork.request.OnLCCheckSendPhotoCallback;
import com.qpidnetwork.request.OnLCCheckSendVideoCallback;
import com.qpidnetwork.request.OnLCGetPhotoCallback;
import com.qpidnetwork.request.OnLCPlayVoiceCallback;
import com.qpidnetwork.request.OnLCSendPhotoCallback;
import com.qpidnetwork.request.OnLCSendVideoCallback;
import com.qpidnetwork.request.OnLCUploadVoiceCallback;
import com.qpidnetwork.request.OnQueryChatRecordCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniLivechat;
import com.qpidnetwork.request.RequestJniLivechat.PhotoModeType;
import com.qpidnetwork.request.RequestJniLivechat.PhotoSizeType;
import com.qpidnetwork.request.RequestJniLivechat.ToFlagType;
import com.qpidnetwork.request.RequestJniLivechat.VideoPhotoType;
import com.qpidnetwork.request.item.EmotionConfigItem;
import com.qpidnetwork.request.item.LCPhotoListAlbumItem;
import com.qpidnetwork.request.item.LCPhotoListPhotoItem;
import com.qpidnetwork.request.item.LCRecord;
import com.qpidnetwork.request.item.LCVideoListGroupItem;
import com.qpidnetwork.request.item.LCVideoListVideoItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.SynConfigItem;

/**
 * LiveChat管理类
 * @author Samson Fan
 *
 */
@SuppressLint("HandlerLeak")
public class LiveChatManager 
			extends LiveChatClientListener 
			implements LCEmotionManager.LCEmotionManagerCallback
						, IAuthorizationCallBack
						, OnConfigManagerCallback
						, LCPhotoManager.LCPhotoManagerListener
						, LCVideoManager.LCVideoManagerListener
{
	private Context mContext = null;
	/**
	 * LiveChat服务器IP list
	 */
	private ArrayList<String> mIpList = null;
	/**
	 * LiveChat服务器端口
	 */
	private int mPort = 0;
	/**
	 * LiveChat PHP服务器host
	 */
	private String mHost = "";
	/**
	 * LiveChat 站点Id
	 */
	private String mSiteId = "";
	/**
	 * 用户本人信息
	 */
	private LCSelfInfo mSelfInfo;
	/**
	 * 是否已登录的标志
	 */
	private boolean mIsLogin = false;
	/**
	 * 是否自动重登录
	 */
	private boolean mIsAutoRelogin = false;
	private final int mAutoReloginTime = 30 * 1000;	// 30秒 
	/**
	 * 正在获取聊天记录管理器
	 */
	private LCGetHistoryManager mGetHistoryMgr = null;
	/**
	 * 消息ID生成器
	 */
	private AtomicInteger mMsgIdIndex = null;
	private static final int MsgIdIndexBegin = 1;
	/**
	 * 文本消息管理器
	 */
	private LCTextManager mTextMgr = null;
	/**
	 * 高级表情管理器
	 */
	private LCEmotionManager mEmotionMgr = null;
	/**
	 * 语音管理器
	 */
	private LCVoiceManager mVoiceMgr = null;
	/**
	 * 图片管理器
	 */
	private LCPhotoManager mPhotoMgr = null;
	/**
	 * 视频管理器
	 */
	private LCVideoManager mVideoMgr = null;
	/**
	 * 用户管理器
	 */
	private LCUserManager mUserMgr = null;
	/**
	 * 黑名单管理器
	 */
	private LCBlockManager mBlockMgr = null;
	/**
	 * 联系人管理器
	 */
	private LCContactManager mContactMgr = null;
	/**
	 * LiveChatTalkUserListItem 管理器
	 */
	private LCUserInfoManager mUserInfoManager;
	/**
	 * Livechat 相关功能对方是否支持检测
	 */
	private LCFunctionCheckManager mLCFunctionCheckManager;
	
	/**
	 * 回调处理器
	 */
	private LiveChatManagerCallbackHandler mCallbackHandler = null;
	/**
	 * Handler
	 */
	private Handler mHandler = null;
	/**
	 * 单件模式
	 */
	private static LiveChatManager instanceLiveChatMgr = null;
	
	/**
	 * 请求操作类型
	 */
	private enum LiveChatRequestOptType {
		GetSynConfigFinish,			// 获取同步配置完成
		GetSelfInfo,				// 获取用户本人信息
		GetEmotionConfig,			// 获取高级表情配置
		GetPhotoList,				// 获取私密照列表
		GetVideoList,				// 获取视频列表
		AutoRelogin,				// 执行自动重登录流程
		GetUsersHistoryMessage,		// 获取聊天历史记录
		SendMessageList,			// 发送指定用户的待发消息
		SendMessageListConnectFail,	// 处理指定用户的待发消息发送不成功(连接失败)
		LoginWithLoginItem,			// 收到OnLogin回调登录LiveChat
		GetBlockList,				// 获取黑名单列表
		GetLadyChatInfo,			// 获取女士聊天信息
		GetFeeRecentContactList,	// 获取最近扣费联系人列表
		UploadClientVersion,		// 上传客户端版本
//		LoginManagerLogout,			// LoginManager注销
		UploadVoiceFile,			// 上传语音文件
		CheckFunctionsFinish,		//相关功能对方是否支持检测完成回调
	}
	
	public static LiveChatManager newInstance(Context context) {
		if (null == instanceLiveChatMgr) {
			if (null != context) {
				instanceLiveChatMgr = new LiveChatManager(context);
			}
		}
		return instanceLiveChatMgr;
	}
	
	public static LiveChatManager getInstance() {
		return instanceLiveChatMgr;
	}
	
	public LiveChatManager(Context context) {
		mContext = context;
		mIpList = new ArrayList<String>();
		mPort = 0;
		mHost = "";
		mSiteId = "";
		mSelfInfo = new LCSelfInfo();
		mIsLogin = false;
		mIsAutoRelogin = false;
		mGetHistoryMgr = new LCGetHistoryManager();
		mTextMgr = new LCTextManager();
		mEmotionMgr = new LCEmotionManager();
		mVoiceMgr = new LCVoiceManager();
		mPhotoMgr = new LCPhotoManager(this);
		mVideoMgr = new LCVideoManager(this);
		mUserMgr = new LCUserManager();
		mBlockMgr = new LCBlockManager();
		mContactMgr = new LCContactManager(mUserMgr);
		mUserInfoManager = new LCUserInfoManager();
		mLCFunctionCheckManager = new LCFunctionCheckManager(mUserMgr, this, mUserInfoManager);
		mCallbackHandler = new LiveChatManagerCallbackHandler();
		mMsgIdIndex = new AtomicInteger(MsgIdIndexBegin);

		// 初始化jni打log
		String logPath = FileCacheManager.getInstance().GetLogPath();
		LiveChatClient.SetLogDirectory(logPath);
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg)
			{
				
				switch (LiveChatRequestOptType.values()[msg.what]) {
				case GetSynConfigFinish: {
					GetSyncConfigFinish();
				}break;
				case GetSelfInfo:{
					LiveChatClient.GetUserInfo(mSelfInfo.mUserId);
				}break;
				case GetEmotionConfig: {
					GetEmotionConfig();
				}break;
				case GetPhotoList: {
					GetPhotoList();
				}break;
				case GetVideoList: {
					GetVideoList();
				}break;
				case AutoRelogin: {
					AutoRelogin();
				}break;
				case GetUsersHistoryMessage: {
					if (msg.obj instanceof String[]) {
						String[] userIds = (String[])msg.obj;
						GetUsersHistoryMessage(userIds);
					}
				}break;
				case SendMessageList: {
					if (msg.obj instanceof String) {
						String userId = (String)msg.obj;
						// 发送待发消息
						SendMessageList(userId);
					}
				}break;
				case SendMessageListConnectFail: {
					if (msg.obj instanceof String) {
						String userId = (String)msg.obj;
						// 回调待发消息发送失败
						SendMessageListFail(userId, LiveChatErrType.ConnectFail);
					}
				}break;
				case LoginWithLoginItem: {
					if (msg.obj instanceof LoginItem) {
						LoginItem item = (LoginItem)msg.obj;
						LoginWithLoginItem(item);
					}
				}break;
				case GetBlockList: {
					LiveChatClient.GetBlockList();
				}break;
				case GetFeeRecentContactList: {
					LiveChatClient.GetFeeRecentContactList();
				}break;
				case GetLadyChatInfo: {
					LiveChatClient.GetLadyChatInfo();
				}break;
				case UploadVoiceFile: {
					if (msg.obj instanceof String) {
						String voiceCode = (String)msg.obj;
						UploadVoiceFile(voiceCode);
					}
				}break;
				case UploadClientVersion: {
					String verCode = String.valueOf(QpidApplication.versionCode);
					LiveChatClient.UploadVer(verCode);
				}break;
//				case LoginManagerLogout: {
//					if (null != LoginManager.getInstance()) {
//						LoginManager.getInstance().LogoutAndClean(true);
//					}
//				}break;
				
				case CheckFunctionsFinish: {
					if (msg.obj instanceof String) {
						String userId = (String)msg.obj;
						// 回调待发消息发送失败
						LCUserItem userItem = mUserMgr.getUserItem(userId);
						CheckTryTicketAndSend(userItem);
					}
				}break;
				}
			}
		};
	}
	
	/**
	 * 注册Other回调
	 * @param listener
	 * @return
	 */
	public boolean RegisterOtherListener(LiveChatManagerOtherListener listener) 
	{
		return mCallbackHandler.RegisterOtherListener(listener);
	}
	
	/**
	 * 注销Other回调
	 * @param listener
	 * @return
	 */
	public boolean UnregisterOtherListener(LiveChatManagerOtherListener listener) 
	{
		return mCallbackHandler.UnregisterOtherListener(listener);
	}
	
	/**
	 * 注册文本消息(Message)回调
	 * @param listener
	 * @return
	 */
	public boolean RegisterMessageListener(LiveChatManagerMessageListener listener) 
	{
		return mCallbackHandler.RegisterMessageListener(listener);
	}
	
	/**
	 * 注销文本消息(Message)回调
	 * @param listener
	 * @return
	 */
	public boolean UnregisterMessageListener(LiveChatManagerMessageListener listener) 
	{
		return mCallbackHandler.UnregisterMessageListener(listener);
	}
	
	/**
	 * 注册高级表情(Emotion)回调
	 * @param listener
	 * @return
	 */
	public boolean RegisterEmotionListener(LiveChatManagerEmotionListener listener) 
	{
		return mCallbackHandler.RegisterEmotionListener(listener);
	}
	
	/**
	 * 注销高级表情(Emotion)回调
	 * @param listener
	 * @return
	 */
	public boolean UnregisterEmotionListener(LiveChatManagerEmotionListener listener) 
	{
		return mCallbackHandler.UnregisterEmotionListener(listener);
	}
	
	/**
	 * 注册私密照(Photo)回调
	 * @param listener
	 * @return
	 */
	public boolean RegisterPhotoListener(LiveChatManagerPhotoListener listener) 
	{
		return mCallbackHandler.RegisterPhotoListener(listener);
	}
	
	/**
	 * 注销私密照(Photo)回调
	 * @param listener
	 * @return
	 */
	public boolean UnregisterPhotoListener(LiveChatManagerPhotoListener listener) 
	{
		return mCallbackHandler.UnregisterPhotoListener(listener);
	}
	
	/**
	 * 注册语音(Voice)回调
	 * @param listener
	 * @return
	 */
	public boolean RegisterVoiceListener(LiveChatManagerVoiceListener listener) 
	{
		return mCallbackHandler.RegisterVoiceListener(listener);
	}
	
	/**
	 * 注册微视频(Video)回调
	 * @param listener
	 * @return
	 */
	public boolean RegisterVideoListener(LiveChatManagerVideoListener listener) 
	{
		return mCallbackHandler.RegisterVideoListener(listener);
	}
	
	/**
	 * 注销微视频(Video)回调
	 * @param listener
	 * @return
	 */
	public boolean UnregisterVideoListener(LiveChatManagerVideoListener listener) 
	{
		return mCallbackHandler.UnregisterVideoListener(listener);
	}
	
	/**
	 * 注销语音(Voice)回调
	 * @param listener
	 * @return
	 */
	public boolean UnregisterVoiceListener(LiveChatManagerVoiceListener listener) 
	{
		return mCallbackHandler.UnregisterVoiceListener(listener);
	}

	/**
	 * 初始化
	 * @param context		
	 * @param ips			LiveChat服务器IP数组
	 * @param port			LiveChat服务器端口
	 * @param webHost		网站host（如：http://www.chnlove.com）
	 * @param siteId		站点ID
	 * @return
	 */
	public boolean Init(String[] ips, int port, String webHost, String siteId) 
	{
		boolean result = false;
		
		Log.d("LiveChatManager", "LiveChatManager::Init() ips.length:%d, port:%d, webHost:%s", ips.length, port, webHost);
		
		if (ips.length > 0 
			&& port > 0
			&& !webHost.isEmpty())
		{
			String logPath = FileCacheManager.getInstance().GetLogPath();
			
			// 初始化高级表情管理器
			String emotionPath = FileCacheManager.getInstance().GetLCEmotionPath();
			result = mEmotionMgr.init(mContext, emotionPath, webHost, siteId, logPath, this);
			
			// 初始化图片管理器
			String photoPath = FileCacheManager.getInstance().GetLCPhotoPath();
			result = result && mPhotoMgr.init(photoPath);
			
			// 初始化语音管理器
			String voicePath = FileCacheManager.getInstance().GetLCVoicePath();
			result = result && mVoiceMgr.init(voicePath);
			
			// 初始化视频管理器
			String videoPath = FileCacheManager.getInstance().GetLCVideoPath();
			result = result && mVideoMgr.init(videoPath);
			
			// 初始化LiveChatClient
			result = result && LiveChatClient.Init(this, ips, port);
		}
		
		Log.d("LiveChatManager", "LiveChatManager::Init() end, result:%b", result);
		return result;
	}

	/**
	 * 判断是否无效seq
	 * @param seq
	 * @return
	 */
	public boolean IsInvalidSeq(int seq)
	{
		return LiveChatClient.IsInvalidSeq(seq);
	}
	
	/**
	 * 重置参数（用于注销后或登录前）
	 */
	private void ResetParam()
	{
		mSelfInfo.mUserId = "";
		mSelfInfo.mSid = "";
		mSelfInfo.mDeviceId = "";
		mSelfInfo.mRiskControl = false;
		mSelfInfo.mIsRecvVideoMsg = true;
		mMsgIdIndex.set(MsgIdIndexBegin);
		
		Log.d("LiveChatManager", "ResetParam() clear emotion begin");
		// 停止获取高级表情配置请求
		mEmotionMgr.StopGetEmotionConfig();
		Log.d("LiveChatManager", "ResetParam() clear emotion StopAllDownloadImage");
		mEmotionMgr.StopAllDownloadImage();
		Log.d("LiveChatManager", "ResetParam() clear emotion removeAllSendingItems");
		mEmotionMgr.removeAllSendingItems();
		
		Log.d("LiveChatManager", "ResetParam() clear photo begin");
		// 停止所有图片请求
		mPhotoMgr.clearAllRequestItems();
		mPhotoMgr.clearAllSelfPhotoRequestItems();
		Log.d("LiveChatManager", "ResetParam() clear photo clearAllSendingItems");
		// 停止所有图片消息请求
		mPhotoMgr.clearAllSendingItems();
		// 清除图片列表
		mPhotoMgr.clearPhotoList();
		
		Log.d("LiveChatManager", "ResetParam() clear voice begin");
		// 停止所有语音请求
		mVoiceMgr.clearAllRequestItem();
		Log.d("LiveChatManager", "ResetParam() clear voice clearAllSendingItems");
		// 停止所有发送语音消息
		mVoiceMgr.clearAllSendingItems();
		// 停止所有获取语音验证码消息
		mVoiceMgr.clearAllGetingCodeItems();
		
		Log.d("LiveChatManager", "ResetParam() clear video begin");
		// 停止所有视频的消息发送请求
		mVideoMgr.clearAllSendingRequestItems();
		mVideoMgr.clearAllSendingItems();
		// 清除视频列表
		mVideoMgr.clearVideoList();
		
		Log.d("LiveChatManager", "ResetParam() clear other begin");
		// 停止所有发送文本消息
		mTextMgr.removeAllSendingItems();
		Log.d("LiveChatManager", "ResetParam() clear other removeAllUserItem");
		// 清除所有用户
		mUserMgr.removeAllUserItem();
		// 清除联系人列表
		mContactMgr.Clear();
		
		// 清除所有资源文件
		removeSourceFile();
	}
	
	/**
	 * 清除所有资源文件（包括图片、语音等）
	 */
	private void removeSourceFile()
	{
		// 清除所有男士的图片文件
		mPhotoMgr.removeAllManPhotoFile();
		// 清除所有语音文件
		mVoiceMgr.removeAllVoiceFile();
	}
	
	/**
	 * 登录
	 * @param userId	用户ID
	 * @param password	php登录成功的session
	 * @param deviceId	设备唯一标识
	 * @return
	 */
	public synchronized boolean Login(String userId, String sid, String deviceId, boolean isRecvVideoMsg) 
	{
		Log.d("LiveChatManager", "LiveChatManager::Login() begin, userId:%s, mIsLogin:%b", userId, mIsLogin);
		
		boolean result = false;
		if (mIsLogin) {
			result = mIsLogin;
		}
		else {
			if (!mIsAutoRelogin) {
				// 重置参数
				ResetParam();
			}
			
			// LiveChat登录 
			result = LiveChatClient.Login(userId, sid, deviceId, ClientType.CLIENT_ANDROID);
			if (result) 
			{
				mIsAutoRelogin = true;
				mSelfInfo.mUserId = userId;
				mSelfInfo.mSid = sid;
				mSelfInfo.mDeviceId = deviceId;
				mSelfInfo.mIsRecvVideoMsg = isRecvVideoMsg;
			}
		}
		
		Log.d("LiveChatManager", "LiveChatManager::Login() end, userId:%s, result:%s", userId, Boolean.toString(result));
		return result;
	}
	
	/**
	 * 是否自动重登录
	 * @return
	 */
	private boolean IsAutoRelogin(LiveChatErrType errType)
	{
		if (mIsAutoRelogin)
		{
			mIsAutoRelogin = (errType == LiveChatErrType.ConnectFail);
		}
		return mIsAutoRelogin;
	}
	
	/**
	 * 自动重登录
	 */
	private void AutoRelogin()
	{
		Log.d("LiveChatManager", "LiveChatManager::AutoRelogin() begin, mUserId:%s, mSid:%s, mDeviceId:%s"
				, mSelfInfo.mUserId, mSelfInfo.mSid, mSelfInfo.mDeviceId);
		
		if (null != mSelfInfo.mUserId && !mSelfInfo.mUserId.isEmpty()
			&& null != mSelfInfo.mSid && !mSelfInfo.mSid.isEmpty()
			&& null != mSelfInfo.mDeviceId && !mSelfInfo.mDeviceId.isEmpty())
		{
			Login(mSelfInfo.mUserId, mSelfInfo.mSid, mSelfInfo.mDeviceId, mSelfInfo.mIsRecvVideoMsg);
		}
		
		Log.d("LiveChatManager", "LiveChatManager::AutoRelogin() end");
	}
	
	/**
	 * 注销
	 * @return
	 */
	public synchronized boolean Logout() 
	{
		Log.d("LiveChatManager", "LiveChatManager::Logout() begin");
		
		// 设置不自动重登录
		mIsAutoRelogin = false;
		boolean result =  LiveChatClient.Logout();
		
		Log.d("LiveChatManager", "LiveChatManager::Logout() end, result:%b", result);
		
		return result;
	}
	
	/**
	 * 是否已经登录
	 * @return
	 */
	public boolean IsLogin() 
	{
		return mIsLogin;
	}
	
	/**
	 * 是否处理发送操作
	 * @return
	 */
	private boolean IsHandleSendOpt()
	{
		boolean result = false;
		if (!mSelfInfo.mRiskControl
			 && mIsAutoRelogin)
		{
			// 没有风控且自动重连
			result = true;
		}
		return result;
	}
	
	/**
	 * 是否立即发送消息给用户
	 * @param userItem	用户item
	 * @return
	 */
	private boolean IsSendMessageNow(LCUserItem userItem)
	{
		boolean result = false;
		if (null != userItem)
		{
			// 已经登录及聊天状态为inchat或男士邀请
			result = IsLogin();
//					&& (userItem.chatType == ChatType.InChatCharge
//						|| userItem.chatType == ChatType.InChatUseTryTicket
//						|| userItem.chatType == ChatType.WomanInvite);
		}
		return result;
	}
	
	/**
	 * 是否等待登录后发送消息给用户
	 * @param userItem	用户item
	 * @return
	 */
	private boolean IsWaitForLoginToSendMessage(LCUserItem userItem)
	{
		boolean result = false;
		if (null != userItem)
		{
			// 已经登录及聊天状态为inchat或男士邀请
			result = !IsLogin();
//					&& (userItem.chatType == ChatType.InChatCharge
//						|| userItem.chatType == ChatType.InChatUseTryTicket
//						|| userItem.chatType == ChatType.ManInvite);
		}
		return result;
	}
	
	/**
	 * 获取用户本人信息
	 * @return
	 */
	public LCSelfInfo GetSelfInfo()
	{
		return mSelfInfo;
	}
	
	/**
	 * 设置在线状态 
	 * @param statusType	在线状态
	 * @return
	 */
	public boolean SetStatus(UserStatusType statusType)
	{
		return LiveChatClient.SetStatus(statusType);
	}
	
	/**
	 * 设置用户在线状态，若用户在线状态改变则callback通知listener
	 * @param userItem
	 * @param statusType
	 * @return 在线状态是否改变
	 */
	private boolean SetUserOnlineStatus(LCUserItem userItem, UserStatusType statusType)
	{
		boolean result = false;
		if (userItem.statusType != statusType)
		{
			result = true;
			userItem.statusType = statusType;
			mCallbackHandler.OnChangeOnlineStatus(userItem);
		}
		return result;
	}

	
	// ------------- 其它操作(Other) -------------
	/**
	 * 获取多个用户信息详情
	 * @param userIds	用户ID数组
	 * @return
	 */
	public int GetUsersInfo(String[] userIds)
	{
		int result = -1;
		if (IsLogin())
		{
			// log打印
			String context = "";
			for (int i = 0; i < userIds.length; i++) {
				if (!context.isEmpty()) {
					context += ",";
				}
				
				context += userIds[i];
			}
			Log.d("LiveChatManager", "GetUsersInfo() mIsLogin:%b, context:%s", mIsLogin, context);
			
			result = LiveChatClient.GetUsersInfo(userIds);
		}
		else
		{
			Log.d("LiveChatManager", "GetUsersInfo() fail, LiveChatClient is not login");
		}
		return result;
	}
	
	/**
	 * 用户历史聊天记录（包括文本、高级表情、语音、图片）
	 * @param userId	用户ID
	 * @return
	 */
	public boolean GetUserHistoryMessage(String userId)
	{
		boolean result = false;
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null != userItem)
		{
			if (!StringUtil.isEmpty(userItem.inviteId)
				&& mGetHistoryMgr.IsGetingHistoryMsgWithInviteId(userItem.inviteId))
			{
				// 正在请求中
				result = true;
			}
			else 
			{
				// 直接返回用户item
				result = true;
				mCallbackHandler.OnGetHistoryMessage(true, "", "", userItem);
			}
		}
		return result;
	}
	
	/**
	 * 请求接口获取单个用户历史聊天记录（包括文本、高级表情、语音、图片）
	 * @param userId	用户ID
	 * @return
	 */
	private boolean GetUserHistoryMessageProc(String userId)
	{
		boolean result = false;
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null != userItem && !userItem.inviteId.isEmpty()) 
		{
			if (mGetHistoryMgr.IsGetingHistoryMsgWithInviteId(userItem.inviteId))
			{
				// 未完成获取聊天记录请求
				result = true;
			}
			else 
			{
				// 请求获取聊天记录
				long requestId = RequestJniLivechat.QueryChatRecord(userItem.inviteId, new OnQueryChatRecordCallback() {
					
					@Override
					public void OnQueryChatRecord(boolean isSuccess, String errno,
							String errmsg, int dbTime, LCRecord[] recordList, String inviteId) 
					{
						// 设置服务器当前数据库时间
						LCMessageItem.SetDbTime(dbTime);
						
						// 插入聊天记录
						LCUserItem userItem = mUserMgr.getUserItemWithInviteId(inviteId);
						if (isSuccess && null != userItem && null != recordList) 
						{
							// 清除已完成的记录（保留未完成发送的记录） 
							userItem.clearFinishedMsgList();
							// 插入历史记录
							for (int i = 0; i < recordList.length; i++) 
							{
								LCMessageItem item = new LCMessageItem();
								if (item.InitWithRecord(
										mMsgIdIndex.getAndIncrement(), 
										mSelfInfo.mUserId, 
										userItem.userId,
										userItem.inviteId,
										recordList[i], 
										mEmotionMgr, 
										mVoiceMgr, 
										mPhotoMgr,
										mVideoMgr)) 
								{
									userItem.insertSortMsgList(item);
								}
							}
							// 合并图片聊天记录
							mPhotoMgr.combineMessageItem(userItem.msgList);
							// 合并视频聊天记录
							mVideoMgr.combineMessageItem(userItem.msgList);
						}
						mCallbackHandler.OnGetHistoryMessage(isSuccess, errno, errmsg, userItem);
						
						mGetHistoryMgr.SetGetHistoryMsgFinish(inviteId);
					}
				});

				result = (requestId != RequestJni.InvalidRequestId);
				
				// 设置正在下载聊天记录
				if (result) 
				{
					mGetHistoryMgr.SetGetingHistoryMsg(userId, requestId);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 获取多个用户历史聊天记录（包括文本、高级表情、语音、图片）
	 * @param userIds	用户ID数组
	 */
	private void GetUsersHistoryMessage(String[] userIds)
	{
		for (int i = 0; i < userIds.length; i++) {
			if (!StringUtil.isEmpty(userIds[i])) {
				GetUserHistoryMessageProc(userIds[i]);
			}
		}
	}
	
	/**
	 * 插入历史聊天记录（msgId及createTime由LiveChatManager生成）
	 * @param userId	对方用户ID
	 * @param item		消息item
	 * @return
	 */
	public boolean InsertHistoryMessage(String userId, LCMessageItem item) 
	{
		boolean result = false;
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null != userItem) {
			result = userItem.insertSortMsgList(item);
			item.msgId = mMsgIdIndex.getAndIncrement();
			item.createTime = (int)(System.currentTimeMillis() / 1000);
		}
		else {
			Log.e("LiveChatManager", String.format("%s::%s() userId:%s is not exist", "LiveChatManager", "InsertHistoryMessage", userId));
		}
		
		if (!result) {
			Log.e("LiveChatManager", String.format("%s::%s() fail, userId:%s, msgId:%d", "LiveChatManager", "InsertHistoryMessage", userId, item.msgId));
		}
		return result;
	}
	
	/**
	 * 删除历史消息记录
	 * @param item	消息item
	 */
	public boolean RemoveHistoryMessage(LCMessageItem item) 
	{
		boolean result = false;
		if (null != item && null != item.getUserItem()) 
		{
			LCUserItem userItem = item.getUserItem();
			result = userItem.removeSortMsgList(item);
		}
		return result;
	}
	
	/**
	 * 获取指定消息Id的消息Item
	 * @param userId	用户ID
	 * @param msgId		消息ID
	 * @return
	 */
	public LCMessageItem GetMessageWithMsgId(String userId, int msgId) {
		LCMessageItem item = null;
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (userItem != null) {
			item = userItem.getMsgItemWithId(msgId);
		}
		return item;
	}
	
	/**
	 * 获取指定用户Id的用户item
	 * @param userId	用户ID
	 * @return
	 */
	public LCUserItem GetUserWithId(String userId) {
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		return userItem;
	}
	
	/**
	 * 获取消息处理状态
	 * @param userId	用户ID
	 * @param msgId		消息ID
	 * @return
	 */
	public StatusType GetMessageItemStatus(String userId, int msgId) 
	{
		StatusType statusType = StatusType.Unprocess;
		LCMessageItem item = GetMessageWithMsgId(userId, msgId);
		if (null != item) {
			statusType = item.statusType;
		}
		return statusType;
	}
	
	/**
	 * 搜索在线男士
	 * @param beginAge	起始年龄
	 * @param endAge	结束年龄
	 * @return
	 */
	public boolean SearchOnlineMan(int beginAge, int endAge)
	{
		boolean result = false;
		Log.d("LiveChatManager", "SearchOnlineMan() mIsLogin:%b, beginAge:%d, endAge:%d", mIsLogin, beginAge, endAge);
		if (mIsLogin) {
			result = LiveChatClient.SearchOnlineMan(beginAge, endAge);
		}
		return result;
	}
	
	/**
	 * 获取联系人列表
	 * @return
	 */
	public ArrayList<LCUserItem> GetContactList()
	{
		return mContactMgr.GetContactUserList();
	}
	
	/**
	 * 获取最后一条聊天消息
	 * @param userId	用户ID
	 * @return
	 */
	public LCMessageItem GetLastTalkMsg(String userId)
	{
		LCMessageItem item = null;
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null != userItem) {
			item = userItem.GetLastTalkMsg();
		}
		return item;
	}
	
	/**
	 * 判断用户 是否在联系人列表
	 * @param userId	用户ID
	 * @return
	 */
	public boolean IsInContactList(String userId)
	{
		return mContactMgr.IsExist(userId);
	}
	
	/**
	 * 获取女士邀请用户列表
	 * @return
	 */
	public ArrayList<LCUserItem> GetWomanInviteUsers()
	{
		return mUserMgr.getWomanInviteUsers();
	}
	
	/**
	 * 判断是否可发送消息
	 * @param userId	用户ID
	 * @param msgType	消息类型
	 * @return
	 */
	public CanSendErrType CanSendMessage(String userId, LCMessageItem.MessageType msgType)
	{
		CanSendErrType result = CanSendErrType.UnknowErr;
		if (!StringUtil.isEmpty(userId))
		{
			LCUserItem userItem = mUserMgr.getUserItem(userId);
			if (null != userItem)
			{
				result = userItem.CanSendMessage(msgType);
			}
		}
		return result;
	}
	
	/**
	 * 回复验证码
	 * @param code	验证码
	 * @return
	 */
	public boolean ReplyIdentifyCode(String code)
	{
		boolean result = false;
		if (!StringUtil.isEmpty(code))
		{
			result = LiveChatClient.ReplyIdentifyCode(code);
		}
		return result;
	}
	
	/**
	 * 刷新验证码(服务器将返回OnRecvIdentifyCode())
	 * @return
	 */
	public boolean RefreshIdentifyCode()
	{
		boolean result = false;
		result = LiveChatClient.RefreshIdentifyCode();
		return result;
	}
	
	// ---------------- 待发送消息处理函数(sending message) ----------------
	
	/**
	 * 发送待发消息列表
	 * @param usreId
	 */
	private void SendMessageList(String userId)
	{
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		synchronized (userItem.sendMsgList) 
		{
			for (LCMessageItem item : userItem.sendMsgList) {
				// 发送消息item
				if(mLCFunctionCheckManager.localCheckFunctionSupport(item)){
					SendMessageItem(item);
				}else{
					onFunctionNotSupportedError(item);
				}
//				// 发送消息item
//				SendMessageItem(item);
			}
			userItem.sendMsgList.clear();
		}
	}
	
	/**
	 * 返回待发送消息错误
	 * @param errType
	 */
	private void SendMessageListFail(final String userId, final LiveChatErrType errType)
	{
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null != userItem) {
			synchronized (userItem.sendMsgList) 
			{
				for (LCMessageItem item : userItem.sendMsgList)
				{
					if(mLCFunctionCheckManager.localCheckFunctionSupport(item)){
						SendMessageFailCallback(errType, "", item);
						item.updateStatus(errType, "", "");
					}else{
						onFunctionNotSupportedError(item);
					}
//					item.updateStatus(errType, "", "");
				}
				
//				@SuppressWarnings("unchecked")
//				ArrayList<LCMessageItem> cloneMsgList = (ArrayList<LCMessageItem>)userItem.sendMsgList.clone();
				userItem.sendMsgList.clear();
				
//				mCallbackHandler.OnSendMessageListFail(errType, cloneMsgList);
			}
		}
		else {
			Log.e("LiveChatManager", "LiveChatManager::SendMessageListFail() get user item fail");
		}
	}
	
	/**
	 * 发送消息item
	 * @param item	消息item
	 */
	private void SendMessageItem(LCMessageItem item)
	{
		// 发送消息
		switch (item.msgType) 
		{
		case Text: 
			SendMessageProc(item);
			break;
		case Emotion:
			SendEmotionProc(item);
			break;
		case Photo:
			SendPhotoProc(item);
			break;
		case Voice:
			SendVoiceProc(item);
			break;
		case Video:
			SendVideoProc(item);
		default:
			Log.e("LiveChatManager", "LiveChatManager::SendMessageList() msgType error, msgType:%s", item.msgType.name());
			break;
		}
	}
	
	// ---------------- 文字聊天操作函数(message) ----------------
	/**
	 * 发送文本聊天消息
	 * @param userId	对方的用户ID
	 * @param message	消息内容
	 * @param illegal	消息内容是否合法
	 * @return 
	 */
	public LCMessageItem SendMessage(String userId, String message) 
	{
		// 判断是否处理发送操作
		if (!IsHandleSendOpt()) {
			Log.e("LiveChatManager", "LiveChatManager::SendMessage() IsHandleSendOpt()==false");
			return null;
		}
		
		// 获取用户item
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null == userItem) {
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, userId:%s", "LiveChatManager", "SendMessage", userId));
			return null;
		}
		
		// 构造消息item
		LCMessageItem item = null;
		if (!message.isEmpty()) {
			// 生成MessageItem
			item = new LCMessageItem();
			item.init(mMsgIdIndex.getAndIncrement()
					, SendType.Send
					, mSelfInfo.mUserId
					, userId
					, userItem.inviteId
					, StatusType.Processing);
			// 生成TextItem
			LCTextItem textItem = new LCTextItem();
			textItem.init(message);
			// 把TextItem加到MessageItem
			item.setTextItem(textItem);
			// 添加到历史记录
			userItem.insertSortMsgList(item);
			
			// 添加到待发送列表
			userItem.addSendingMsg(item);
			//获取对方支持功能列表
			mLCFunctionCheckManager.CheckFunctionSupported(userId);
			
//			if (IsWaitForLoginToSendMessage(userItem)) 
//			{
//				// 登录未成功，添加到待发送列表
//				userItem.addSendingMsg(item);
//			}
//			else if (IsSendMessageNow(userItem)) 
//			{
//				// 发送消息
//				SendMessageProc(item);
//			}
//			else 
//			{
//				// 暂时不能发，可能由于规则问题
//			}
		}
		else {
			Log.e("LiveChatManager", String.format("%s::%s() param error, userId:%s, message:%s", "LiveChatManager", "SendMessage", userId, message));
		}
		return item;
	}
	
	/**
	 * 发送文本消息处理
	 * @param item
	 */
	private void SendMessageProc(LCMessageItem item)
	{
		if (LiveChatClient.SendMessage(item.toId, item.getTextItem().message, item.getTextItem().illegal, item.msgId)) 
		{
			mTextMgr.addSendingItem(item);
		}
		else {
			item.updateStatus(LiveChatErrType.Fail, "", "");
			mCallbackHandler.OnSendMessage(LiveChatErrType.Fail, "", item);
		}
	}
	
	/**
	 * 根据错误类型生成警告消息
	 * @param userItem	用户item
	 * @param errType	服务器返回错误类型
	 */
	private void BuildAndInsertWarningWithErrType(LCUserItem userItem, LiveChatErrType errType) 
	{
		if (errType == LiveChatErrType.NoMoney) 
		{
			// 获取消息内容
//			String message = mContext.getString(R.string.livechat_msg_no_credit_warning);
//			String linkMsg = mContext.getString(R.string.livechat_msg_no_credit_warning_link);
			// 生成余额不足的警告消息
//			BuildAndInsertWarning(userItem, message, linkMsg);
		}
	}
	
	/**
	 * 插入warning消息
	 * @param userItem	用户item
	 * @param message	warning消息文字
	 * @return
	 */
	public boolean BuildAndInsertWarning(LCUserItem userItem, String message)
	{
		boolean result = false;
		if (!message.isEmpty()) {
			// 生成warning消息
			LCWarningItem warningItem = new LCWarningItem();
//			if (!linkMsg.isEmpty()) {
//				LCWarningLinkItem linkItem = new LCWarningLinkItem();
//				linkItem.init(linkMsg, LinkOptType.Rechange);
//				warningItem.initWithLinkMsg(message, linkItem);
//				warningItem.linkItem = linkItem;
//			}
			// 生成message消息
			LCMessageItem item = new LCMessageItem();
			item.init(mMsgIdIndex.getAndIncrement(), SendType.System, userItem.userId, mSelfInfo.mUserId, userItem.inviteId, StatusType.Finish);
			item.setWarningItem(warningItem);
			// 插入到聊天记录列表中
			userItem.insertSortMsgList(item);
			
			result = true;
			// 回调
			mCallbackHandler.OnRecvWarning(item);
		}
		return result;
	}
	
	/**
	 * 生成系统消息
	 * @param userId
	 * @param message
	 * @return
	 */
	public boolean BuildAndInsertSystemMsg(String userId, String message)
	{
		boolean result = false;
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null != userItem) {
			// 生成系统消息并回调
			LCSystemItem systemItem = new LCSystemItem();
			systemItem.message = message; 
			LCMessageItem item = new LCMessageItem();
			item.init(mMsgIdIndex.getAndIncrement(), SendType.System, userId, mSelfInfo.mUserId, userItem.inviteId, StatusType.Finish);
			item.setSystemItem(systemItem);
			userItem.insertSortMsgList(item);
			mCallbackHandler.OnRecvSystemMsg(item);
			
			result = true;
		}
		return result;
	}
	
	// ---------------- 高级表情操作函数(Emotion) ----------------
	/**
	 * 获取高级表情配置（在OnGetEmotionConfig中返回）
	 * @return
	 */
	public void GetEmotionConfig() 
	{
		mEmotionMgr.GetEmotionConfig();
	}
	
	/**
	 * 获取高级表情item
	 * @param emotionId	高级表情ID
	 * @return
	 */
	public LCEmotionItem GetEmotionInfo(String emotionId)
	{
		return mEmotionMgr.getEmotion(emotionId);
	}
	
	/**
	 * 发送高级表情
	 * @param userId	对方的用户ID
	 * @param emotionId	高级表情ID
	 * @param ticket	票根
	 * @return
	 */
	public LCMessageItem SendEmotion(String userId, String emotionId)
	{
		// 判断是否处理发送操作
		if (!IsHandleSendOpt()) {
			Log.e("LiveChatManager", "LiveChatManager::SendEmotion() IsHandleSendOpt()==false");
			return null;
		}
		
		// 获取用户item
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null == userItem) {
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, userId:%s", "LiveChatManager", "SendEmotion", userId));
			return null;
		}
		
		LCMessageItem item = null;
		if (!emotionId.isEmpty()) {
			// 生成MessageItem
			item = new LCMessageItem();
			item.init(mMsgIdIndex.getAndIncrement()
					, SendType.Send
					, mSelfInfo.mUserId
					, userId
					, userItem.inviteId
					, StatusType.Processing);
			// 获取EmotionItem
			LCEmotionItem emotionItem = mEmotionMgr.getEmotion(emotionId);
			// 把EmotionItem添加到MessageItem
			item.setEmotionItem(emotionItem);
			// 添加到历史记录
			userItem.insertSortMsgList(item);
			
			// 添加到待发送列表
			userItem.addSendingMsg(item);
			//获取对方支持功能列表
			mLCFunctionCheckManager.CheckFunctionSupported(userId);

//			if (IsWaitForLoginToSendMessage(userItem)) 
//			{
//				// 登录未成功，添加到待发送列表
//				userItem.addSendingMsg(item);
//			}
//			else if (IsSendMessageNow(userItem)) 
//			{
//				// 发送消息
//				SendEmotionProc(item);
//			}
//			else 
//			{
//				// 暂时不能发送，可能由于规则问题
//			}
		}
		else {
			Log.e("LiveChatManager", String.format("%s::%s() param error, userId:%s, emotionId:%s", "LiveChatManager", "SendEmotion", userId, emotionId));
		}
		return item;
	}
	
	/**
	 * 发送高级表情处理
	 * @param item
	 */
	private void SendEmotionProc(LCMessageItem item)
	{
		if (LiveChatClient.SendEmotion(item.toId, item.getEmotionItem().emotionId, item.msgId)) {
			mEmotionMgr.addSendingItem(item);
		}
		else {
			item.updateStatus(LiveChatErrType.Fail, "", "");
			mCallbackHandler.OnSendEmotion(LiveChatErrType.Fail, "", item);
		}
	}
	
	/**
	 * 手动下载/更新高级表情图片文件
	 * @param emotionId	高级表情ID
	 * @return
	 */
	public boolean GetEmotionImage(String emotionId) 
	{
		LCEmotionItem emotionItem = mEmotionMgr.getEmotion(emotionId);
		
		boolean result = false;
		// 判断文件是否存在，若不存在则下载
		if (!emotionItem.imagePath.isEmpty()) {
			File file  = new File(emotionItem.imagePath);
			if (file.exists() && file.isFile()) {
				mCallbackHandler.OnGetEmotionImage(true, emotionItem);
				result = true;
			}
		}
		
		// 文件不存在，需要下载
		if (!result) {
			result = mEmotionMgr.StartDownloadImage(emotionItem);
		}
		return result;
	}
	
	/**
	 * 手动下载/更新高级表情图片文件
	 * @param emotionId	高级表情ID
	 * @return
	 */
	public boolean GetEmotionPlayImage(String emotionId) 
	{
		LCEmotionItem emotionItem = mEmotionMgr.getEmotion(emotionId);
		
		boolean result = false;
		// 判断文件是否存在，若不存在则下载
		if (!emotionItem.playBigPath.isEmpty()) {
			File file  = new File(emotionItem.playBigPath);
			if (file.exists() && file.isFile()) {
				if (!emotionItem.playBigImages.isEmpty()) {
					result = true;
					for (String filePath : emotionItem.playBigImages)
					{
						File subFile = new File(filePath);
						if (!subFile.exists() || !subFile.isFile()) {
							result = false;
							break;
						}
					} 
				}
				
				// 所有文件都存在
				if (result) {
					mCallbackHandler.OnGetEmotionPlayImage(true, emotionItem);
				}
			}
		}
		
		// 有文件不存在，需要下载
		if (!result) {
			result = mEmotionMgr.StartDownloadPlayImage(emotionItem);
		}
		return result;
	}
	
	// ---------------- 图片操作函数(Private Album) ----------------
	/**
	 * 发送图片消息
	 * @param userId	对方的用户ID
	 * @param photoId	图片ID
	 * @return
	 */
	public LCMessageItem SendPhoto(
			String userId
			, String photoId)
	{
		// 判断是否处理发送操作
		if (!IsHandleSendOpt()) {
			Log.e("LiveChatManager", "LiveChatManager::SendPhoto() IsHandleSendOpt()==false");
			return null;
		}
		
		// 获取用户item
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null == userItem) {
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, userId:%s", "LiveChatManager", "SendPhoto", userId));
			return null;
		}
		
		LCMessageItem item = null;
		// 获取PhotoItem
		LCPhotoItem photoItem = mPhotoMgr.GetSelfPhoto(photoId);
		if (null != photoItem)
		{
			// 生成MessageItem
			item = new LCMessageItem();
			item.init(mMsgIdIndex.getAndIncrement()
					, SendType.Send
					, mSelfInfo.mUserId
					, userId
					, userItem.inviteId
					, StatusType.Processing);
			// 把PhotoItem添加到MessageItem
			item.setPhotoItem(photoItem);
			// 添加到历史记录
			userItem.insertSortMsgList(item);
			
			// 添加到待发送列表
			userItem.addSendingMsg(item);
			//获取对方支持功能列表
			mLCFunctionCheckManager.CheckFunctionSupported(userId);
			
//			if (IsWaitForLoginToSendMessage(userItem)) 
//			{
//				// 登录未成功，添加到待发送列表
//				userItem.addSendingMsg(item);
//			}
//			else if (IsSendMessageNow(userItem)) 
//			{
//				// 发送消息
//				SendPhotoProc(item);
//			}
//			else 
//			{
//				// 暂时不能发，可能由于规则问题
//			}
		}
		return item;
	}
	
	/**
	 * 发送图片处理
	 * @param userId
	 * @param inviteId
	 * @param photoPath
	 * @return
	 */
	private void SendPhotoProc(LCMessageItem item)
	{
		LCUserItem userItem = item.getUserItem();
		LCPhotoItem photoItem = item.getPhotoItem();
		// 请求上传文件
		long requestId = RequestJniLivechat.SendPhoto(
							userItem.userId
							, userItem.inviteId
							, photoItem.photoId
							, mSelfInfo.mUserId
							, mSelfInfo.mSid
							, new OnLCSendPhotoCallback() 
		{
			
			@Override
			public void OnLCSendPhoto(long requestId, boolean isSuccess, String errno,
					String errmsg, String sendId) 
			{
				LCMessageItem msgItem = mPhotoMgr.getAndRemoveRequestItem(requestId);
				if (null == msgItem) {
					Log.e("LiveChatManager", String.format("%s::%s() OnLCSendPhoto() get request item fail, requestId:%d", "LiveChatManager", "SendPhoto", requestId));
					return;
				}
				
				if (isSuccess) {
					LCPhotoItem photoItem = msgItem.getPhotoItem();
					photoItem.sendId = sendId;
					
					if (LiveChatClient.SendLadyPhoto(msgItem.toId
							, msgItem.getUserItem().inviteId
							, photoItem.photoId
							, photoItem.sendId
							, false
							, photoItem.photoDesc
							, msgItem.msgId)) 
					{
						// 添加到发送map
						mPhotoMgr.addSendingItem(msgItem);
					}
					else {
						// LiveChatClient发送不成功
						msgItem.updateStatus(LiveChatErrType.Fail, "", "");
						mCallbackHandler.OnSendPhoto(LiveChatErrType.Fail, "", "", msgItem);
					}
				}
				else {
					// 上传文件不成功
					msgItem.updateStatus(LiveChatErrType.Fail, errno, errmsg);
					mCallbackHandler.OnSendPhoto(LiveChatErrType.Fail, errno, errmsg, msgItem);
				}
			}
		});
		
		if (RequestJni.InvalidRequestId != requestId) {
			if (!mPhotoMgr.addRequestItem(requestId, item)) {
				Log.e("LiveChatManager", String.format("%s::%s() add request item fail, requestId:%d", "LiveChatManager", "SendPhoto", requestId));
			}
		}
		else {
			item.updateStatus(LiveChatErrType.Fail, "", "");
			mCallbackHandler.OnSendPhoto(LiveChatErrType.Fail, "", "", item);
		}
	}
	
	/**
	 * 检测图片是否可发送（不包括发送规则限制）
	 * @param userId	用户ID
	 * @param photoId	图片ID
	 * @return
	 */
	public boolean CheckSendPhotoMessage(String userId, String photoId)
	{
		boolean result = false;
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		LCPhotoItem photoItem = mPhotoMgr.GetSelfPhoto(photoId);
		if (null != userItem
			&& !StringUtil.isEmpty(userItem.inviteId)
			&& null != photoItem) 
		{
			// 停止旧检测请求
			long oldRequestId = mPhotoMgr.getLastCheckPhotoRequest();
			if (oldRequestId != RequestJni.InvalidRequestId) {
				LCPhotoCheckItem checkItem = mPhotoMgr.getAndRemoveCheckPhotoRequest(oldRequestId);
				if (null != checkItem) {
					RequestJni.StopRequest(oldRequestId);
					mCallbackHandler.OnCheckSendPhoto(
							LiveChatErrType.Fail
							, OnLCCheckSendPhotoCallback.ResultType.CannotSend
							, ""
							, ""
							, checkItem.userItem
							, checkItem.photoItem);
				}
			}
			
			// 检测请求
			long requestId = RequestJniLivechat.CheckSendPhoto(userItem.userId
					, userItem.inviteId
					, photoItem.photoId
					, mSelfInfo.mSid
					, mSelfInfo.mUserId
					, new OnLCCheckSendPhotoCallback() 
			{
				@Override
				public void OnLCCheckSendPhoto(long requestId, OnLCCheckSendPhotoCallback.ResultType result, String errno, String errmsg) 
				{
					LCPhotoCheckItem checkItem = mPhotoMgr.getAndRemoveCheckPhotoRequest(requestId);
					if (null != checkItem) {
						LiveChatErrType errType = (result == ResultType.AllowSend ? LiveChatErrType.Success : LiveChatErrType.Fail); 
						mCallbackHandler.OnCheckSendPhoto(errType, result, errno, errmsg, checkItem.userItem, checkItem.photoItem);
					}
				}
			});
			
			if (requestId != RequestJni.InvalidRequestId) {
				LCPhotoCheckItem checkItem = new LCPhotoCheckItem();
				checkItem.userItem = userItem;
				checkItem.photoItem = photoItem;
				mPhotoMgr.addCheckPhotoRequest(requestId, checkItem);
			}
		}
		return result;
	}
	
	/**
	 * 获取自己的图片item(若图片没有路径可调用GetSelfPhoto()下载)
	 * @param photoId	图片ID
	 * @return
	 */
	public LCPhotoItem GetSelfPhotoItem(String photoId)
	{
		return mPhotoMgr.GetSelfPhoto(photoId);
	}
	
	/**
	 * 根据消息ID下载图片
	 * @param msgId		消息ID
	 * @param sizeType	下载的照片尺寸	
	 * @return
	 */
	public boolean GetPhotoWithMessage(String userId, int msgId, PhotoSizeType sizeType)
	{
		boolean result = false;
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null != userItem) 
		{
			LCMessageItem item = userItem.getMsgItemWithId(msgId);
			LCPhotoItem photoItem = item.getPhotoItem();
			if (null != item
				&& item.msgType == MessageType.Photo
				&& photoItem != null
				&& !StringUtil.isEmpty(item.fromId)
				&& !StringUtil.isEmpty(photoItem.photoId))
			{
				// 判断是否正在下载
				result = photoItem.IsDownloading(PhotoModeType.Clear, sizeType);
				if (!result) 
				{
					if (item.sendType != SendType.Send) {
						// 男士发来的图片下载
						RequestJniLivechat.ToFlagType toFlag = RequestJniLivechat.ToFlagType.WomanGetMan; 
						long requestId = GetPhoto(toFlag, item.fromId, PhotoModeType.Clear, photoItem.photoId, sizeType);
						if (requestId != RequestJni.InvalidRequestId) 
						{
							// 请求成功，修改下载状态
							photoItem.AddDownloading(PhotoModeType.Clear, sizeType);
							// 添加到MessageItem请求map
							mPhotoMgr.addRequestItem(requestId, item);
							result = true;
						}
					}
					else {
						// 女士自己的图片
						result = GetSelfPhoto(photoItem.photoId, sizeType);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 根据图片ID下载图片
	 * @param photoId
	 * @param sizeType
	 * @return
	 */
	public boolean GetSelfPhoto(String photoId, PhotoSizeType sizeType)
	{
		boolean result = false;
		PhotoModeType modeType = PhotoModeType.Clear;
		LCPhotoItem photoItem = mPhotoMgr.GetSelfPhoto(photoId);
		if (null != photoItem)
		{
			// 若不是正在下载，则马上下载
			result = photoItem.IsDownloading(modeType, sizeType);
			if (!result)
			{
				long requestId = GetPhoto(
						RequestJniLivechat.ToFlagType.WomanGetSelf
						, mSelfInfo.mUserId
						, modeType
						, photoId
						, sizeType);
				if (requestId != RequestJni.InvalidRequestId)
				{
					// 请求成功，修改下载状态
					photoItem.AddDownloading(PhotoModeType.Clear, sizeType);
					// 添加到请求map
					mPhotoMgr.AddSelfPhotoRequestItem(requestId, photoItem);
					result = true;
				}
			}
		}
		return result;
	}
	
	/**
	 * 请求获取图片
	 * @param photoId	图片ID
	 * @param sizeType	尺寸类型
	 * @return
	 */
	private long GetPhoto(
			RequestJniLivechat.ToFlagType toFlag
			, String userId
			, PhotoModeType modeType
			, String photoId
			, PhotoSizeType sizeType)
	{
		long requestId = RequestJniLivechat.GetPhoto(
				toFlag
				, userId
				, mSelfInfo.mUserId
				, mSelfInfo.mSid
				, photoId
				, sizeType
				, PhotoModeType.Clear
				, mPhotoMgr.getTempPhotoPath(photoId, PhotoModeType.Clear, sizeType, toFlag == ToFlagType.WomanGetSelf)
				, new OnLCGetPhotoCallback() 
		{
			@Override
			public void OnLCGetPhoto(long requestId, boolean isSuccess,
					String errno, String errmsg, String photoId,
					PhotoSizeType sizeType, PhotoModeType modeType,
					String filePath) 
			{
				// 是否已经处理
				boolean isHandle = false;
				
				// 处理MessageItem消息下载
				LCMessageItem item = mPhotoMgr.getAndRemoveRequestItem(requestId);
				if (null != item && null != item.getPhotoItem()) 
				{
					LCPhotoItem photoItem = item.getPhotoItem();
					
					// 若未处理
					if (!isHandle) {
						// 成功则把临时文件名改为正式文件名，并赋值到对应路径变量
						if (isSuccess) {
							boolean isMine = (item.sendType == SendType.Send);
							String tempPath = mPhotoMgr.getTempPhotoPath(
													photoItem.photoId
													, PhotoModeType.Clear
													, sizeType
													, isMine);
							mPhotoMgr.tempToPhoto(photoItem, tempPath, modeType, sizeType, isMine);
						}
						// 移除下载状态
						photoItem.RemoveDownloading(modeType, sizeType);
					}

					// callback
					if (isSuccess) {
						mCallbackHandler.OnGetPhoto(LiveChatErrType.Success, "", "", item);
					}
					else {
						mCallbackHandler.OnGetPhoto(LiveChatErrType.Fail, errno, errmsg, item);
					}
				}
				
				// 处理PhotoItem下载
				LCPhotoItem photoItem = mPhotoMgr.GetAndRemoveSelfPhotoRequestItem(requestId);
				if (null != photoItem)
				{
					// 若未处理
					if (!isHandle) {
						// 成功则把临时文件名改为正式文件名，并赋值到对应路径变量
						if (isSuccess) {
							String tempPath = mPhotoMgr.getTempPhotoPath(photoItem.photoId, PhotoModeType.Clear, sizeType, true);
							mPhotoMgr.tempToPhoto(photoItem, tempPath, modeType, sizeType, true);
						}
						// 移除下载状态
						photoItem.RemoveDownloading(modeType, sizeType);
					}
					
					// callback
					if (isSuccess) {
						mCallbackHandler.OnGetSelfPhoto(LiveChatErrType.Success, "", "", photoItem);
					}
					else {
						mCallbackHandler.OnGetSelfPhoto(LiveChatErrType.Fail, errno, errmsg, photoItem);
					}
				}
			}
		});
		
		return requestId;
	}
	
	/**
	 * 获取图片发送/下载进度
	 * @param item	消息item
	 * @return
	 */
	public int GetPhotoProcessRate(LCMessageItem item) {
		int percent = 0;
		long requestId = mPhotoMgr.getRequestIdWithItem(item);
		if (requestId != RequestJni.InvalidRequestId) {
			int total = RequestJni.GetDownloadContentLength(requestId);
			int recv = RequestJni.GetRecvLength(requestId);
			
			if (total > 0) {
				recv = recv * 100;
				percent = recv / total;
			}
		}
		return percent;
	}
	
	/**
	 * 获取图片列表（结果在OnGetPhotoList回调中返回）
	 * @return
	 */
	public void GetPhotoList()
	{
		mPhotoMgr.GetPhotoList(mSelfInfo.mSid, mSelfInfo.mUserId);
	}
	
	// ---------------- 语音操作函数(Voice) ----------------
	/**
	 * 发送语音（包括获取语音验证码(livechat)、上传语音文件(livechat)、发送语音(livechat)）
	 * @param userId	对方的用户ID
	 * @param voicePath	语音文件本地路径
	 * @return
	 */
	public LCMessageItem SendVoice(String userId, String voicePath, String fileType, int timeLength)
	{
		// 判断是否处理发送操作
		if (!IsHandleSendOpt()) {
			Log.e("LiveChatManager", "LiveChatManager::SendVoice() IsHandleSendOpt()==false");
			return null;
		}
		
		// 获取用户item
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null == userItem) {
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, userId:%s", "LiveChatManager", "SendVoice", userId));
			return null;
		}

		// 生成MessageItem
		LCMessageItem item = new LCMessageItem();
		item.init(mMsgIdIndex.getAndIncrement()
				, SendType.Send
				, mSelfInfo.mUserId
				, userId
				, userItem.inviteId
				, StatusType.Processing);
		// 生成VoiceItem
		LCVoiceItem voiceItem = new LCVoiceItem();
		voiceItem.init(""
				, voicePath
				, timeLength
				, fileType
				, ""
				, true);
		// 把VoiceItem添加到MessageItem
		item.setVoiceItem(voiceItem);
		// 添加到聊天记录中
		userItem.insertSortMsgList(item);
		
		// 添加到待发送列表
		userItem.addSendingMsg(item);
		//获取对方支持功能列表
		mLCFunctionCheckManager.CheckFunctionSupported(userId);

//		if (IsWaitForLoginToSendMessage(userItem)) 
//		{
//			// 登录未成功，添加到待发送列表
//			userItem.addSendingMsg(item);
//		}
//		else if (IsSendMessageNow(userItem)) 
//		{
//			// 发送消息
//			SendVoiceProc(item);
//		}
//		else 
//		{
//			// 暂时不能发，可能由于规则问题
//		}
		return item;
	}
	
	/**
	 * 发送语音处理
	 * @param item
	 */
	private void SendVoiceProc(LCMessageItem item)
	{
		if (LiveChatClient.GetLadyVoiceCode(item.toId)) 
		{
			mVoiceMgr.addGetingCodeItem(item);
		}
		else {
			item.updateStatus(LiveChatErrType.Fail, "", "");
			mCallbackHandler.OnSendVoice(LiveChatErrType.Fail, "", "", item);
		}
	}
	
	/**
	 * 获取语音（包括下载语音(livechat)）
	 * @param item		消息ID
	 * @return
	 */
	public boolean GetVoice(LCMessageItem item)
	{
		if (item.msgType != MessageType.Voice
			&& null == item.getVoiceItem())
		{
			Log.e("LiveChatManager", String.format("%s::%s() param error.", "LiveChatManager", "GetVoice"));
			return false;
		}
		
		// 请求获取语音文件
		boolean result = false;
		LCVoiceItem voiceItem = item.getVoiceItem();
		String filePath = mVoiceMgr.getVoicePath(item);
		long requestId = RequestJniLivechat.PlayVoice(voiceItem.voiceId, mSiteId, filePath, new OnLCPlayVoiceCallback() {
			
			@Override
			public void OnLCPlayVoice(long requestId, boolean isSuccess, String errno,
					String errmsg, String filePath) 
			{
				LCMessageItem item = mVoiceMgr.getAndRemoveRquestItem(requestId);
				if (null != item) 
				{
					// 下载成功，设置语音文件路径
					if (isSuccess) 
					{
						item.getVoiceItem().filePath = filePath;
					}
					// 设置状态并callback
					LiveChatErrType errType = isSuccess ? LiveChatErrType.Success : LiveChatErrType.Fail;
					item.updateStatus(errType, errno, errmsg);
					mCallbackHandler.OnGetVoice(errType, errmsg, item);
				}
				else {
					Log.e("LiveChatManager", String.format("%s::%s() item is null, requestId:%d, isSuccess:%b, errno:%s, errmsg:%s, filePath:%s"
							, "LiveChatManager", "OnLCPlayVoice", requestId, isSuccess, errno, errmsg, filePath));
				}
			}
		});
		
		if (requestId != RequestJni.InvalidRequestId) {
			// 添加至请求map
			item.setProcessingStatus();
			mVoiceMgr.addRequestItem(requestId, item);
			result = true;
			
			Log.d("LiveChatManager", String.format("%s::%s() requestId:%d", "LiveChatManager", "OnLCPlayVoice", requestId));
		}
		else {
			Log.e("LiveChatManager", String.format("%s::%s() RequestJniLivechat.PlayVoice fail, voiceId:%s, siteId:%s, filePath:%s"
					, "LiveChatManager"
					, "GetVoice"
					, voiceItem.voiceId, mSiteId, voiceItem.filePath)); 
			LiveChatErrType errType = LiveChatErrType.Fail;
			item.updateStatus(errType, "", "");
			mCallbackHandler.OnGetVoice(errType, "", item);
			result = false;
		}
		return result;
	}
	
	/**
	 * 获取语音发送/下载进度
	 * @param item	消息item
	 * @return
	 */
	public int GetVoiceProcessRate(LCMessageItem item) 
	{
		int percent = 0;
		long requestId = mVoiceMgr.getRequestIdWithItem(item);
		if (requestId != RequestJni.InvalidRequestId) {
			int total = RequestJni.GetDownloadContentLength(requestId);
			int recv = RequestJni.GetRecvLength(requestId);
			
			if (total > 0) {
				recv = recv * 100;
				percent = recv / total;
			}
		}
		return percent;
	}
	
	/**
	 * 上传语音文件
	 * @param voiceCode	语音验证码
	 */
	private void UploadVoiceFile(String voiceCode)
	{
		LCMessageItem item = mVoiceMgr.getAndRemoveGetingCodeItem();
		if (!StringUtil.isEmpty(voiceCode) 
			&& null != item 
			&& null != item.getVoiceItem()) 
		{
			LCVoiceItem voiceItem = item.getVoiceItem();
			
			long requestId = RequestJniLivechat.UploadVoice(
					voiceCode
					, item.inviteId
					, item.fromId
					, item.toId
					, mSiteId
					, voiceItem.fileType
					, voiceItem.timeLength
					, voiceItem.filePath
					, new OnLCUploadVoiceCallback() {
						
						@Override
						public void OnLCUploadVoice(long requestId, boolean isSuccess,
								String errno, String errmsg, String voiceId) 
						{
							// 获取requestId对应的消息item
							LCMessageItem item = mVoiceMgr.getAndRemoveRquestItem(requestId);
							if (null == item || null == item.getVoiceItem()) 
							{
								Log.e("livechat", "LiveChatManager::OnLCUploadVoice() param fail. voiceItem is null.");
								item.updateStatus(LiveChatErrType.Fail, "", "");
								mCallbackHandler.OnSendVoice(LiveChatErrType.Fail, "", "", item);
							}
							else if (isSuccess) 
							{
								// 上传文件成功，发送语音消息
								LCVoiceItem voiceItem = item.getVoiceItem();
								voiceItem.voiceId = voiceId;
								if (LiveChatClient.SendVoice(item.toId, voiceItem.voiceId, voiceItem.timeLength, item.msgId)) {
									mVoiceMgr.addSendingItem(item.msgId, item);
								}
								else {
									item.updateStatus(LiveChatErrType.Fail, "", "");
									mCallbackHandler.OnSendVoice(LiveChatErrType.Fail, "", "", item);
								}
							}
							else {
								item.updateStatus(LiveChatErrType.Fail, errno, errmsg);
								mCallbackHandler.OnSendVoice(LiveChatErrType.Fail, errno, errmsg, item);
							}
						}
			});
			
			if (requestId != RequestJni.InvalidRequestId) {
				// 添加item到请求map
				mVoiceMgr.addRequestItem(requestId, item);
			}
			else {
				item.updateStatus(LiveChatErrType.Fail, "", "");
				mCallbackHandler.OnSendVoice(LiveChatErrType.Fail, "", "", item);
			}
		}
		else 
		{
			Log.e("LiveChatManager", "UploadVoiceFile() fail, voiceCode:%s, item:%s, voiceItem:%s"
					, voiceCode, item.toString(), item.getVoiceItem().toString());
			item.updateStatus(LiveChatErrType.Fail, "", "");
			mCallbackHandler.OnSendVoice(LiveChatErrType.Fail, "", "", item);
		}
	}
	
	// ---------------- 视频操作函数(Video) ----------------
	/**
	 * 检测视频是否可发送（不包括发送规则限制）
	 * @param userId	用户ID
	 * @param videoId	视频ID
	 * @return
	 */
	public boolean CheckSendVideo(String userId, String videoId)
	{
		boolean result = false;
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		LCVideoItem videoItem = mVideoMgr.GetVideo(videoId);
		if (null != userItem
			&& !StringUtil.isEmpty(userItem.inviteId)
			&& null != videoItem) 
		{
			// 停止旧检测请求
			long oldRequestId = mPhotoMgr.getLastCheckPhotoRequest();
			if (oldRequestId != RequestJni.InvalidRequestId) {
				LCVideoCheckItem checkItem = mVideoMgr.getAndRemoveCheckVideoRequest(oldRequestId);
				if (null != checkItem) {
					RequestJni.StopRequest(oldRequestId);
					mCallbackHandler.OnCheckSendVideo(
							LiveChatErrType.Fail
							, OnLCCheckSendVideoCallback.ResultType.CannotSend
							, ""
							, ""
							, checkItem.userItem
							, checkItem.videoItem);
				}
			}
			
			// 检测请求
			long requestId = RequestJniLivechat.CheckSendVideo(
								userItem.userId
								, videoItem.videoId
								, userItem.inviteId
								, mSelfInfo.mSid
								, mSelfInfo.mUserId
								, new OnLCCheckSendVideoCallback() 
			{
				@Override
				public void OnLCCheckSendVideo(long requestId, OnLCCheckSendVideoCallback.ResultType result, String errno, String errmsg) 
				{
					LCVideoCheckItem checkItem = mVideoMgr.getAndRemoveCheckVideoRequest(requestId);
					if (null != checkItem) {
						LiveChatErrType errType = 
								(result==OnLCCheckSendVideoCallback.ResultType.AllowSend ? LiveChatErrType.Success : LiveChatErrType.Fail); 
						mCallbackHandler.OnCheckSendVideo(errType, result, errno, errmsg, checkItem.userItem, checkItem.videoItem);
					}
				}
			});
			
			if (requestId != RequestJni.InvalidRequestId) {
				LCVideoCheckItem checkItem = new LCVideoCheckItem();
				checkItem.userItem = userItem;
				checkItem.videoItem = videoItem;
				mVideoMgr.addCheckVideoRequest(requestId, checkItem);
			}
		}
		return result;
	}
	
	/**
	 * 获取视频列表
	 */
	public void GetVideoList()
	{
		mVideoMgr.GetVideoList(mSelfInfo.mUserId, mSelfInfo.mSid);
	}
	
	@Override
	public void OnGetVideoList(boolean isSuccess, String errno, String errmsg,
			LCVideoListGroupItem[] groups, LCVideoListVideoItem[] videos) 
	{
		mCallbackHandler.OnGetVideoList(isSuccess, errno, errmsg, groups, videos);
	}
	
	/**
	 * 获取视频item
	 * @param videoId	视频ID
	 */
	public LCVideoItem GetVideoItem(String videoId)
	{
		return mVideoMgr.GetVideoWithExist(videoId);
	}
	
	/**
	 * 发送视频消息
	 * @param userId	用户ID
	 * @param videoId	视频ID
	 * @return
	 */
	public LCMessageItem SendVideo(String userId, String videoId)
	{
		// 判断是否处理发送操作
		if (!IsHandleSendOpt()) {
			Log.e("LiveChatManager", "LiveChatManager::SendVideo() IsHandleSendOpt()==false");
			return null;
		}
		
		// 获取用户item
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null == userItem) {
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, userId:%s", "LiveChatManager", "SendVideo", userId));
			return null;
		}
		
		LCMessageItem item = null;
		// 获取PhotoItem
		LCVideoItem videoItem = mVideoMgr.GetVideoWithExist(videoId);
		if (null != videoItem)
		{
			// 生成videoMsgItem
			LCVideoMsgItem videoMsgItem = new LCVideoMsgItem();
			videoMsgItem.videoItem = videoItem;
			// 生成MessageItem
			item = new LCMessageItem();
			item.init(mMsgIdIndex.getAndIncrement()
					, SendType.Send
					, mSelfInfo.mUserId
					, userId
					, userItem.inviteId
					, StatusType.Processing);
			// 把VideoItem添加到MessageItem
			item.setVideoItem(videoMsgItem);
			// 添加到历史记录
			userItem.insertSortMsgList(item);
			
			// 添加到待发送列表
			userItem.addSendingMsg(item);
			//获取对方支持功能列表
			mLCFunctionCheckManager.CheckFunctionSupported(userId);
			
//			if (IsWaitForLoginToSendMessage(userItem)) 
//			{
//				// 登录未成功，添加到待发送列表
//				userItem.addSendingMsg(item);
//			}
//			else if (IsSendMessageNow(userItem)) 
//			{
//				// 发送消息
//				SendVideoProc(item);
//			}
//			else 
//			{
//				// 暂时不能发，可能由于规则问题
//			}
		}
		return item;	
	}
	
	/**
	 * 发送视频处理
	 * @param item		消息item
	 * @return
	 */
	private void SendVideoProc(LCMessageItem item)
	{
		LCUserItem userItem = item.getUserItem();
		LCVideoMsgItem videoMsgItem = item.getVideoItem();
		// 请求发送视频
		long requestId = RequestJniLivechat.SendVideo(
							userItem.userId
							, videoMsgItem.videoItem.videoId
							, userItem.inviteId
							, mSelfInfo.mSid
							, mSelfInfo.mUserId
							, new OnLCSendVideoCallback() 
		{
			
			@Override
			public void OnLCSendVideo(long requestId, boolean isSuccess, String errno,
					String errmsg, String sendId) 
			{
				LCMessageItem msgItem = mVideoMgr.getAndRemoveSendingRequestItem(requestId);
				if (null == msgItem) {
					Log.e("LiveChatManager", String.format("%s::%s() OnLCSendVideo() get request item fail, requestId:%d", "LiveChatManager", "SendVideo", requestId));
					return;
				}
				
				if (isSuccess) 
				{
					LCVideoMsgItem videoMsgItem = msgItem.getVideoItem();
					LCVideoItem videoItem = videoMsgItem.videoItem; 
					
					videoMsgItem.sendId = sendId;
					
					if (LiveChatClient.SendLadyVideo(msgItem.toId
							, msgItem.getUserItem().inviteId
							, videoItem.videoId
							, videoMsgItem.sendId
							, false
							, videoItem.videoDesc
							, msgItem.msgId)) 
					{
						// 添加到发送map
						mVideoMgr.addSendingItem(msgItem);
					}
					else {
						// LiveChatClient发送不成功
						msgItem.updateStatus(LiveChatErrType.Fail, "", "");
						mCallbackHandler.OnSendVideo(LiveChatErrType.Fail, "", "", msgItem);
					}
				}
				else {
					// 请求发送不成功
					msgItem.updateStatus(LiveChatErrType.Fail, errno, errmsg);
					mCallbackHandler.OnSendVideo(LiveChatErrType.Fail, errno, errmsg, msgItem);
				}
			}
		});
		
		if (RequestJni.InvalidRequestId != requestId) {
			if (!mVideoMgr.addSendingRequestItem(requestId, item)) {
				Log.e("LiveChatManager", String.format("%s::%s() add request item fail, requestId:%d", "LiveChatManager", "SendPhoto", requestId));
			}
		}
		else {
			item.updateStatus(LiveChatErrType.Fail, "", "");
			mCallbackHandler.OnSendVideo(LiveChatErrType.Fail, "", "", item);
		}
	}
	
	/**
	 * 下载视频图片
	 * @param videoItem	视频item
	 * @param photoType	图片类型
	 * @return
	 */
	public boolean GetVideoPhoto(LCVideoItem videoItem, VideoPhotoType photoType)
	{
		boolean result = false;
		result = mVideoMgr.DownloadVideoPhoto(
					mSelfInfo.mUserId
					, mSelfInfo.mSid
					, videoItem
					, photoType);
		return result;
	}
	
	@Override
	public void OnDownloadVideoPhotoFinish(boolean isSuccess, String errno,
			String errmsg, VideoPhotoType photoType, LCVideoItem videoItem) 
	{
		LiveChatErrType errType = (isSuccess ? LiveChatErrType.Success : LiveChatErrType.Fail);
		mCallbackHandler.OnGetVideoPhoto(errType, errno, errmsg, photoType, videoItem);
	}
	
	/**
	 * 下载视频文件
	 */
	public boolean GetVideo(LCMessageItem item)
	{
		boolean result = false;
		result = mVideoMgr.DownloadVideo(mSelfInfo.mUserId, mSelfInfo.mSid, item);
		return result;
	}

	@Override
	public void OnDownloadVideoFinish(boolean isSuccess, String errno,
			String errmsg, LCMessageItem item) 
	{
		LiveChatErrType errType = (isSuccess ? LiveChatErrType.Success : LiveChatErrType.Fail);
		mCallbackHandler.OnGetVideo(errType, errno, errmsg, item);
	}
	
	// ------------- LiveChatClientListener abstract function -------------
	/**
	 * 登录回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 */
	@Override
	public void OnLogin(LiveChatErrType errType, String errmsg)
	{
		Log.d("LiveChatManager", String.format("OnLogin() begin errType:%s", errType.name()));
		boolean isAutoLogin = false;
		if (errType == LiveChatErrType.Success) {
			mIsLogin = true;
			
			// 获取用户本人信息
			Message msgGetSelfInfo = Message.obtain();
			msgGetSelfInfo.what = LiveChatRequestOptType.GetSelfInfo.ordinal();
			mHandler.sendMessage(msgGetSelfInfo);
			
			// 上传客户端内部版本号
			Message msgUploadVer = Message.obtain();
			msgUploadVer.what = LiveChatRequestOptType.UploadClientVersion.ordinal();
			mHandler.sendMessage(msgUploadVer);
			
			// 获取黑名单列表
			Message msgBlockList = Message.obtain();
			msgBlockList.what = LiveChatRequestOptType.GetBlockList.ordinal();
			mHandler.sendMessage(msgBlockList);
						
			// 获取联系人列表
			Message msgContactList = Message.obtain();
			msgContactList.what = LiveChatRequestOptType.GetFeeRecentContactList.ordinal();
			mHandler.sendMessage(msgContactList);
			
			// 获取女士聊天信息
			Message msgGetLadyChatInfo = Message.obtain();
			msgGetLadyChatInfo.what = LiveChatRequestOptType.GetLadyChatInfo.ordinal();
			mHandler.sendMessage(msgGetLadyChatInfo);
			
			// 获取高级表情配置
			Message msgGetEmotionConfig = Message.obtain();
			msgGetEmotionConfig.what = LiveChatRequestOptType.GetEmotionConfig.ordinal();
			mHandler.sendMessage(msgGetEmotionConfig);
			
			// 获取女士私密照列表
			Message msgGetPhotoList = Message.obtain();
			msgGetPhotoList.what = LiveChatRequestOptType.GetPhotoList.ordinal();
			mHandler.sendMessage(msgGetPhotoList);
			
			// 获取女士视频列表
			Message msgGetVideoList = Message.obtain();
			msgGetVideoList.what = LiveChatRequestOptType.GetVideoList.ordinal();
			mHandler.sendMessage(msgGetVideoList);
		}
		else if (IsAutoRelogin(errType)) {
			Log.d("LiveChatManager", "OnLogin() AutoRelogin() begin");
			// 自动重登录
			isAutoLogin = true;
			Message msgAutoRelogin = Message.obtain();
			msgAutoRelogin.what = LiveChatRequestOptType.AutoRelogin.ordinal();
			mHandler.sendMessageDelayed(msgAutoRelogin, mAutoReloginTime);
			Log.d("LiveChatManager", "OnLogin() AutoRelogin() end");
		}
		else {
			mSelfInfo.mUserId = "";
			mSelfInfo.mSid = "";
			mSelfInfo.mDeviceId = "";
		}
		
		Log.d("LiveChatManager", "OnLogin() callback");
		mCallbackHandler.OnLogin(errType, errmsg, isAutoLogin);
		Log.d("LiveChatManager", "OnLogin() end");
	}
	
	/**
	 * 注销/断线回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param isAutoLogin	是否自动登录
	 */
	@Override
	public void OnLogout(LiveChatErrType errType, String errmsg)
	{
		Log.d("LiveChatManager", "OnLogout() begin, errType:%s", errType.name());
		
		// 重置参数
		mIsLogin = false;
		
		// callback
		boolean isAutoLogin = IsAutoRelogin(errType);
		Log.d("LiveChatManager", "OnLogout(boolean bActive) callback OnLogout");
		mCallbackHandler.OnLogout(errType, errmsg, isAutoLogin);
		if (isAutoLogin) {
			// 自动重登录
			Log.d("LiveChatManager", "OnLogout(boolean bActive) AutoRelogin");
			Message msgAutoRelogin = Message.obtain();
			msgAutoRelogin.what = LiveChatRequestOptType.AutoRelogin.ordinal();
			mHandler.sendMessageDelayed(msgAutoRelogin, mAutoReloginTime);
		}
		else {
			// 重置参数
			ResetParam();
		}
		
		Log.d("LiveChatManager", "OnLogout(boolean bActive) end");
	}
	
	/**
	 * 设置在线状态回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 */
	@Override
	public void OnSetStatus(LiveChatErrType errType, String errmsg)
	{
		mCallbackHandler.OnSetStatus(errType, errmsg);
	}

	/**
	 * 发送消息回调
	 */
	@Override
	public void OnSendMessage(LiveChatErrType errType, String errmsg,
			String userId, String message, int ticket) 
	{
		// 已发消息处理
		LCMessageItem item = mTextMgr.getAndRemoveSendingItem(ticket);
		if (null != item) {
			item.updateStatus(errType, "", errmsg);
			mCallbackHandler.OnSendMessage(errType, errmsg, item);
		}
		else {
			Log.e("LiveChatManager", String.format("%s::%s() get sending item fail, ticket:%d", "LiveChatManager", "OnSendMessage", ticket));
		}

		if (null != item && null != item.getUserItem()) 
		{
			if (errType != LiveChatErrType.Success) {
					// 生成警告消息
					BuildAndInsertWarningWithErrType(item.getUserItem(), errType);
					
					// 修改聊天状态
					if (errType == LiveChatErrType.TargetNotExist)
					{
						if (item.getUserItem().setChatType(ChatType.Other)) {
							mCallbackHandler.OnContactListChange();
						}
					}
			}
			else {
				// 发送成功，更新聊天状态
				if (item.getUserItem().UpdateInChatStatus()) {
					mCallbackHandler.OnContactListChange();
				}
			}
		}
		
		Log.d("LiveChatManager", "OnSendMessage() errType:%s, userId:%s, message:%s", errType.name(), userId, message);
	}

	/**
	 * 发送高级表情回调
	 */
	@Override
	public void OnSendEmotion(LiveChatErrType errType, String errmsg,
			String userId, String emotionId, int ticket) 
	{
		// 已发消息处理
		LCMessageItem item = mEmotionMgr.getAndRemoveSendingItem(ticket);
		if (null != item) {
			item.updateStatus(errType, "", errmsg);
			mCallbackHandler.OnSendEmotion(errType, errmsg, item);
		}
		else {
			Log.e("LiveChatManager", String.format("%s::%s() get sending item fail, ticket:%d", "LiveChatManager", "OnSendEmotion", ticket));
		}

		if (null != item && null != item.getUserItem()) 
		{
			if (errType != LiveChatErrType.Success) {
				// 生成警告消息
				BuildAndInsertWarningWithErrType(item.getUserItem(), errType);
				
				// 修改聊天状态
				if (errType == LiveChatErrType.TargetNotExist
					|| errType == LiveChatErrType.EmotionError)
				{
					if (item.getUserItem().setChatType(ChatType.Other)) {
						mCallbackHandler.OnContactListChange();
					}
				}
			}
			else {
				// 发送成功，更新聊天状态
				if (item.getUserItem().UpdateInChatStatus()) {
					mCallbackHandler.OnContactListChange();
				}
			}
		}
	}

	/**
	 * 发送语音消息回调
	 */
	@Override
	public void OnSendVoice(LiveChatErrType errType, String errmsg,
			String userId, String voiceId, int ticket) 
	{
		LCMessageItem item = mVoiceMgr.getAndRemoveSendingItem(ticket);
		if (null != item) {
			item.updateStatus(errType, "", errmsg);
			mCallbackHandler.OnSendVoice(errType, "", errmsg, item);
		}

		if (null != item && null != item.getUserItem())
		{
			if (errType != LiveChatErrType.Success) {
				// 生成警告消息
				BuildAndInsertWarningWithErrType(item.getUserItem(), errType);
				
				// 修改聊天状态
				if (errType == LiveChatErrType.TargetNotExist
					|| errType == LiveChatErrType.VoiceError)
				{
					if (item.getUserItem().setChatType(ChatType.Other)) {
						mCallbackHandler.OnContactListChange();
					}
				}
			}
			else {
				// 发送成功，更新聊天状态
				if (item.getUserItem().UpdateInChatStatus()) {
					mCallbackHandler.OnContactListChange();
				}
			}
		}
	}
	
	/**
	 * 收到语音验证码通知
	 */
	@Override
	public void OnRecvLadyVoiceCode(String code) 
	{
		// 上传语音文件
		Message msg = Message.obtain();
		msg.what = LiveChatRequestOptType.UploadVoiceFile.ordinal();
		msg.obj = code;
		mHandler.sendMessage(msg);
	}

	/**
	 * 发送图片（私密照）消息回调
	 */
	@Override
	public void OnSendLadyPhoto(LiveChatErrType errType, String errmsg, int ticket) 
	{
		LCMessageItem item = mPhotoMgr.getAndRemoveSendingItem(ticket);
		if (null != item) {
			item.updateStatus(errType, "", errmsg);
			mCallbackHandler.OnSendPhoto(errType, "", errmsg, item);
		}
		else {
			Log.e("LiveChatManager", "OnSendPhoto() errType:%s, errmsg:%s, msgId:%d"
					, errType.name(), errmsg, ticket);
		}

		if (null != item && null != item.getUserItem())
		{
			if (errType != LiveChatErrType.Success) {
				// 生成警告消息
				BuildAndInsertWarningWithErrType(item.getUserItem(), errType);
				
				// 修改聊天状态
				if (errType == LiveChatErrType.TargetNotExist
					|| errType == LiveChatErrType.PhotoError)
				{
					if (item.getUserItem().setChatType(ChatType.Other)) {
						mCallbackHandler.OnContactListChange();
					}
				}
			}
			else {
				// 发送成功，更新聊天状态
				if (item.getUserItem().UpdateInChatStatus()) {
					mCallbackHandler.OnContactListChange();
				}
			}
		}
	}
	
	/**
	 * 发送视频消息回调
	 */
	@Override
	public void OnSendLadyVideo(LiveChatErrType errType, String errmsg, int ticket) 
	{
		LCMessageItem item = mVideoMgr.getAndRemoveSendingItem(ticket);
		if (null != item) {
			item.updateStatus(errType, "", errmsg);
			mCallbackHandler.OnSendVideo(errType, "", errmsg, item);
		}
		else {
			Log.e("LiveChatManager", "OnSendLadyVideo() errType:%s, errmsg:%s, msgId:%d"
					, errType.name(), errmsg, ticket);
		}

		if (null != item && null != item.getUserItem())
		{
			if (errType != LiveChatErrType.Success) {
				// 生成警告消息
				BuildAndInsertWarningWithErrType(item.getUserItem(), errType);
				
				// 修改聊天状态
				if (errType == LiveChatErrType.TargetNotExist
					|| errType == LiveChatErrType.MicVideo)
				{
					if (item.getUserItem().setChatType(ChatType.Other)) {
						mCallbackHandler.OnContactListChange();
					}
				}
			}
			else {
				// 发送成功，更新聊天状态
				if (item.getUserItem().UpdateInChatStatus()) {
					mCallbackHandler.OnContactListChange();
				}
			}
		}
	}

	/**
	 * 获取单个用户信息（已改为使用 GetUsersInfo()）
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param item		用户信息
	 */
	@Override
	public void OnGetUserInfo(LiveChatErrType errType, String errmsg, String userId,
			LiveChatTalkUserListItem item) 
	{
		Log.d("LiveChatManager", "OnGetUserInfo() errType:%s, errmsg:%s, item.userId:%s, mSelfInfo.mUserId:%s"
				, errType.name(), errmsg, item.userId, mSelfInfo.mUserId);
		if (errType == LiveChatErrType.Success)
		{
			// 更新本人信息
			if (item.userId.equals(mSelfInfo.mUserId))
			{
				mSelfInfo.UpdateInfo(item);
				mCallbackHandler.OnTransStatusChange();
			}else{
				//更新本地缓存男士信息
				mUserInfoManager.OnGetUserInfoUpdate(item);
			}
		}
		if(mLCFunctionCheckManager != null){
			//通知对方支持功能检测模块，男士信息更新
			mLCFunctionCheckManager.onGetUserInfo(errType, userId, item);
		}
	}

	/**
	 * 获取多个用户信息回调
	 * @param errType		错误类型
	 * @param errmsg		错误描述
	 * @param list			用户信息数组
	 */
	@Override
	public void OnGetUsersInfo(LiveChatErrType errType, String errmsg,
			int seq, LiveChatTalkUserListItem[] list) 
	{
		String log = "";
		
		// 若用户在联系人里，则更新信息
		boolean contactChange = false;
		for (int i = 0; i < list.length; i++)
		{
			// 更新用户信息
			boolean isUpdate = mUserMgr.updateUser(list[i]);
			
			// 联系人更新
			if (isUpdate && mContactMgr.IsExist(list[i].userId)) {
				contactChange = true;
			}
			
			log += list[i].userId + ":" + list[i].userName + ", ";
		}
		
		Log.d("LiveChatManager", "OnGetUsersInfo() %s", log);
		
		//更新本地缓存男士信息
		mUserInfoManager.OnGetUersInfoUpdate(list);
		
		// callback
		mCallbackHandler.OnGetUsersInfo(errType, errmsg, seq, list);
		// 联系人更新回调
		if (contactChange) {
			mCallbackHandler.OnContactListChange();
		}
	}

	/**
	 * 获取黑名单列表回调
	 */
	@Override
	public void OnGetBlockList(LiveChatErrType errType, String errmsg,
			LiveChatTalkUserListItem[] list) 
	{
		// log打印
		String blockLog = "";
		for (LiveChatTalkUserListItem item : list) 
		{
			// log打印
			if (!blockLog.isEmpty()) {
				blockLog += ",";
			}
			blockLog += item.userName;
			blockLog += "(" + item.userId + ")";
		}
		Log.d("LiveChatManager", "LiveChatManager::OnGetBlockList() errType:%s, errmsg:%s, block:%s"
				, errType.name(), errmsg, blockLog);
		
		// 插入用户管理器
		for (int i = 0; i < list.length; i++) 
		{
			mUserMgr.updateUser(list[i]);
		}
		
		// 更新黑名单
		mBlockMgr.UpdateWithBlockList(list);
	}

	/**
	 * 获取已付费联系人列表回调
	 */
	@Override
	public void OnGetFeeRecentContactList(LiveChatErrType errType,
			String errmsg, String[] userIds) 
	{
		// log打印
		String userLog = "";
		for (int i = 0; i < userIds.length; i++) 
		{
			if (!userLog.isEmpty()) {
				userLog += ",";
			}
			userLog += userIds[i];
			
			// 添加用户
			mUserMgr.getUserItem(userIds[i]);
			
			// 添加到联系人列表
			mContactMgr.AddContact(userIds[i]);
		}
		Log.d("LiveChatManager", "LiveChatManager::OnGetFeeRecentContactList() errType:%s, errmsg:%s, userIds:%s"
				, errType.name(), errmsg, userLog);
		
		// 获取用户信息
		GetUsersInfo(userIds);
	}

	/**
	 * 收到验证码通知回调
	 */
	@Override
	public void OnReplyIdentifyCode(LiveChatErrType errType, String errmsg) 
	{
		// log打印 
		Log.d("LiveChatManager", "LiveChatManager::OnReplyIdentifyCode() errType:%s, errmsg:%s"
				, errType.name(), errmsg);
		
		// callback
		mCallbackHandler.OnReplyIdentifyCode(errType, errmsg);
	}

	/**
	 * 获取女士聊天信息回调
	 */
	@Override
	public void OnGetLadyChatInfo(LiveChatErrType errType, String errmsg,
			String[] chattingUserIds, String[] chattingInviteIds,
			String[] missingUserIds, String[] missingInviteIds) 
	{
		
		String chattingLog = "";
		String missingLog = "";
		if (chattingUserIds.length == chattingInviteIds.length
			&& missingUserIds.length == missingInviteIds.length)
		{
			// chatting
			for (int i = 0; i < chattingUserIds.length; i++) 
			{
				// log打印
				if (!chattingLog.isEmpty()) {
					chattingLog += ",";
				}
				chattingLog += chattingUserIds[i];
				chattingLog += "(" + chattingInviteIds[i] + ")";
				
				// 添加inchat用户
				mUserMgr.addInChatUser(chattingUserIds[i], chattingInviteIds[i]);
				// 添加联系人列表
				mContactMgr.AddContact(chattingUserIds[i]);
			}
			
			// missing
			for (int j = 0; j < missingUserIds.length; j++) 
			{
				// log打印
				if (!missingLog.isEmpty()) {
					missingLog += ",";
				}
				missingLog += missingUserIds[j];
				missingLog += "(" + missingUserIds[j] + ")";
				
				// 添加在聊用户
				mUserMgr.addInviteUser(missingUserIds[j], missingInviteIds[j]);
				// 添加联系人列表
				mContactMgr.AddContact(missingUserIds[j]);
			}
		}
		
		// 获取在聊用户聊天记录
		Message msgInChat = Message.obtain();
		msgInChat.what = LiveChatRequestOptType.GetUsersHistoryMessage.ordinal();
		msgInChat.obj = chattingUserIds;
		mHandler.sendMessage(msgInChat);
		
		// 获取missing用户聊天记录
		Message msgMissing = Message.obtain();
		msgMissing.what = LiveChatRequestOptType.GetUsersHistoryMessage.ordinal();
		msgMissing.obj = missingUserIds;
		mHandler.sendMessage(msgMissing);
		
		Log.d("LiveChatManager", "LiveChatManager::OnGetLadyChatInfo() errType:%s, errmsg:%s, chatting:%s, missing:%s"
				, errType.name(), errmsg, chattingLog, missingLog);
	}

	/**
	 * 查询在线男士回调
	 */
	@Override
	public void OnSearchOnlineMan(LiveChatErrType errType, String errmsg,
			String[] userIds) 
	{
		// log打印
		String userLog = "";
		for (int i = 0; i < userIds.length; i++) 
		{
			if (!userLog.isEmpty()) {
				userLog += ",";
			}
			userLog += userIds;
		}
		Log.d("LiveChatManager", "LiveChatManager::OnSearchOnlineMan() errType:%s, errmsg:%s, user:%s"
				, errType.name(), errmsg, userLog);
		
		// callback
		mCallbackHandler.OnSearchOnlineMan(errType, errmsg, userIds);
	}

	/**
	 * 接收聊天文本消息回调
	 * @param toId		接收者ID
	 * @param fromId	发送者ID
	 * @param fromName	发送者用户名
	 * @param inviteId	邀请ID
	 * @param charge	是否已付费
	 * @param ticket	票根
	 * @param msgType	聊天消息类型
	 * @param message	消息内容
	 */
	@Override
	public void OnRecvMessage(String toId, String fromId, String fromName,
			String inviteId, boolean charge, int ticket, TalkMsgType msgType,
			String message) 
	{
		// 返回票根给服务器
		LiveChatClient.UploadTicket(fromId, ticket);
		
		// 判断是否处理消息
		if (!mBlockMgr.IsExist(fromId))
		{
			// 更新用户状态
			LCUserItem userItem = mUserMgr.getUserItem(fromId);
			if (null == userItem) {
				Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, fromId:%s", "LiveChatManager", "OnRecvMessage", fromId));
				return;
			}
			userItem.userName = fromName;
			boolean contactChange = userItem.setChatTypeWithTalkMsgType(inviteId, charge, msgType);
			contactChange = contactChange || SetUserOnlineStatus(userItem, UserStatusType.USTATUS_ONLINE);
			
			// 添加联系人
			if (mContactMgr.AddContactWithUserItem(userItem)) {
				// 获取新联系人用户信息
				String[] userIds = {fromId};
				GetUsersInfo(userIds);
				// 设置需要更新联系人标志
				contactChange = true;
			}
			
			// 生成MessageItem
			LCMessageItem item = new LCMessageItem();
			item.init(mMsgIdIndex.getAndIncrement()
					, SendType.Recv
					, fromId
					, toId
					, userItem.inviteId
					, StatusType.Finish);
			// 生成TextItem
			LCTextItem textItem = new LCTextItem();
			textItem.init(message);
			// 把TextItem添加到MessageItem
			item.setTextItem(textItem);
			// 添加到用户聊天记录中
			userItem.insertSortMsgList(item);

			// 联系人状态改变 callback
			if (contactChange) {
				mCallbackHandler.OnContactListChange();
			}
			// callback
			mCallbackHandler.OnRecvMessage(item);
		}
	}

	/**
	 * 接收高级表情消息回调
	 * @param toId		接收者ID
	 * @param fromId	发送者ID
	 * @param fromName	发送者用户名
	 * @param inviteId	邀请ID
	 * @param charge	是否已付费
	 * @param ticket	票根
	 * @param msgType	聊天消息类型
	 * @param emotionId	高级表情ID
	 */
	@Override
	public void OnRecvEmotion(String toId, String fromId, String fromName,
			String inviteId, boolean charge, int ticket, TalkMsgType msgType,
			String emotionId) 
	{
		
		// 返回票根给服务器
		LiveChatClient.UploadTicket(fromId, ticket);
		
		// 更新用户状态
		LCUserItem userItem = mUserMgr.getUserItem(fromId);
		if (null == userItem) {
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, fromId:%s", "LiveChatManager", "OnRecvEmotion", fromId));
			return;
		}
		userItem.userName = fromName;
		boolean contactChange = userItem.setChatTypeWithTalkMsgType(inviteId, charge, msgType);
		contactChange = contactChange || SetUserOnlineStatus(userItem, UserStatusType.USTATUS_ONLINE);
		
		// 添加联系人
		contactChange = contactChange || mContactMgr.AddContactWithUserItem(userItem);
		
		// 生成MessageItem
		LCMessageItem item = new LCMessageItem();
		item.init(mMsgIdIndex.getAndIncrement()
				, SendType.Recv
				, fromId
				, toId
				, userItem.inviteId
				, StatusType.Finish);
		// 获取EmotionItem
		LCEmotionItem emotionItem = mEmotionMgr.getEmotion(emotionId);
		// 把EmotionItem添加到MessageItem
		item.setEmotionItem(emotionItem);
		
		// 添加到用户聊天记录中
		userItem.insertSortMsgList(item);

		// 联系人状态改变 callback
		if (contactChange) {
			mCallbackHandler.OnContactListChange();
		}
		// callback
		mCallbackHandler.OnRecvEmotion(item);
	}

	/**
	 * 接收语音消息回调
	 * @param toId		接收者ID
	 * @param fromId	发送者ID
	 * @param fromName	发送者用户名
	 * @param inviteId	邀请ID
	 * @param charge	是否已付费
	 * @param msgType	聊天消息类型
	 * @param voiceId	语音ID
	 * @param fileType	语音文件类型
	 * @param timeLen	语音时长
	 */
	@Override
	public void OnRecvVoice(String toId, String fromId, String fromName,
			String inviteId, boolean charge, TalkMsgType msgType,
			String voiceId, String fileType, int timeLen) 
	{
		// 更新用户状态
		LCUserItem userItem = mUserMgr.getUserItem(fromId);
		if (null == userItem) {
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, fromId:%s", "LiveChatManager", "OnRecvVoice", fromId));
			return;
		}
		userItem.userName = fromName;
		boolean contactChange = userItem.setChatTypeWithTalkMsgType(inviteId, charge, msgType);
		contactChange = contactChange || SetUserOnlineStatus(userItem, UserStatusType.USTATUS_ONLINE);
		
		// 添加联系人
		contactChange = contactChange || mContactMgr.AddContactWithUserItem(userItem);
		
		// 生成MessageItem
		LCMessageItem item = new LCMessageItem();
		item.init(mMsgIdIndex.getAndIncrement()
				, SendType.Recv
				, fromId
				, toId
				, userItem.inviteId
				, StatusType.Finish);
		// 生成VoiceItem
		LCVoiceItem voiceItem = new LCVoiceItem();
		voiceItem.init(voiceId
				, mVoiceMgr.getVoicePath(voiceId, fileType)
				, timeLen
				, fileType
				, ""
				, charge);
		
		// 把VoiceItem添加到MessageItem
		item.setVoiceItem(voiceItem);
		
		// 添加到聊天记录中
		userItem.insertSortMsgList(item);
		
		// 联系人状态改变 callback
		if (contactChange) {
			mCallbackHandler.OnContactListChange();
		}
		// callback
		mCallbackHandler.OnRecvVoice(item);
	}

	/**
	 * 接收警告消息回调
	 * @param toId		接收者ID
	 * @param fromId	发送者ID
	 * @param fromName	发送者用户名
	 * @param inviteId	邀请ID
	 * @param charge	是否已付费
	 * @param ticket	票根
	 * @param msgType	聊天消息类型
	 * @param message	消息内容
	 */
	@Override
	public void OnRecvWarning(String toId, String fromId, String fromName,
			String inviteId, boolean charge, int ticket, TalkMsgType msgType,
			String message) 
	{
		// 返回票根给服务器
		LiveChatClient.UploadTicket(fromId, ticket);

		// 更新用户状态
		LCUserItem userItem = mUserMgr.getUserItem(fromId);
		if (null == userItem) {
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, fromId:%s", "LiveChatManager", "OnRecvWarning", fromId));
			return;
		}
		userItem.userName = fromName;
		boolean contactChange = userItem.setChatTypeWithTalkMsgType(inviteId,charge, msgType);
		contactChange = contactChange || SetUserOnlineStatus(userItem, UserStatusType.USTATUS_ONLINE);
		
		// 添加联系人
		contactChange = contactChange || mContactMgr.AddContactWithUserItem(userItem);
		
		// 生成MessageItem
		LCMessageItem item = new LCMessageItem();
		item.init(mMsgIdIndex.getAndIncrement()
				, SendType.Recv
				, fromId
				, toId
				, userItem.inviteId
				, StatusType.Finish);
		// 生成WarningItem
		LCWarningItem warningItem = new LCWarningItem();
		warningItem.init(message);
		// 把WarningItem添加到MessageItem
		item.setWarningItem(warningItem);
		
		// 添加到用户聊天记录中
		userItem.insertSortMsgList(item);
		
		// 联系人状态改变 callback
		if (contactChange) {
			mCallbackHandler.OnContactListChange();
		}
		// callback
		mCallbackHandler.OnRecvWarning(item);
	}

	/**
	 * 接收更新在线状态消息回调
	 * @param userId
	 * @param server
	 * @param clientType
	 * @param statusType
	 */
	@Override
	public void OnUpdateStatus(String userId, String server,
			ClientType clientType, UserStatusType statusType) 
	{
		if (mSelfInfo.mUserId.equals(userId))
		{
			// 用户自己状态改变
			if (statusType == UserStatusType.USTATUS_BIND
					|| statusType == UserStatusType.USTATUS_UNBIND)
			{
				// 用户翻译绑定状态改变
				mSelfInfo.mTransBind = (statusType == UserStatusType.USTATUS_BIND);
				
				// callback
				mCallbackHandler.OnTransStatusChange();
			}
		}
		else if (mSelfInfo.mTransUserId.equals(userId))
		{
			// 翻译在线状态改变
			if (statusType == UserStatusType.USTATUS_HIDDEN
					|| statusType == UserStatusType.USTATUS_OFFLINE_OR_HIDDEN
					|| statusType == UserStatusType.USTATUS_ONLINE)
				{
					mSelfInfo.mTransStatus = statusType;
					// 若翻译不在线则解除绑定
					if (mSelfInfo.mTransStatus != UserStatusType.USTATUS_ONLINE) {
						mSelfInfo.mTransBind = false;
						
						// callback
						mCallbackHandler.OnTransStatusChange();
					}
				}
		}
		else 
		{
			// 更新用户状态
			LCUserItem userItem = mUserMgr.getUserItem(userId);
			if (null == userItem) {
				Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, userId:%s", "LiveChatManager", "OnUpdateStatus", userId)); 
				return;
			}
			userItem.clientType = clientType;
			boolean contactChange = SetUserOnlineStatus(userItem, statusType);
			
			//更新本地缓存男士信息
			mUserInfoManager.OnUpdateStatus(userId);
			
			// callback
			mCallbackHandler.OnUpdateStatus(userItem);
			// 联系人状态改变 callback
			if (contactChange) {
				mCallbackHandler.OnContactListChange();
			}
		}
	}

	/**
	 * 更新票根回调
	 */
	@Override
	public void OnUpdateTicket(String fromId, int ticket) 
	{
		// 不用处理
	}

	/**
	 * 对方在正输入回调
	 */
	@Override
	public void OnRecvEditMsg(String fromId) 
	{
		// 更新用户状态
		LCUserItem userItem = mUserMgr.getUserItem(fromId);
		if (null != userItem) {
			// 更新用户在线状态
			if (SetUserOnlineStatus(userItem, UserStatusType.USTATUS_ONLINE)
				&& mContactMgr.IsExist(fromId)) 
			{
				// 联系人状态改变callback
				mCallbackHandler.OnContactListChange();
			}
		}
		else {
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, userId:%s", "LiveChatManager", "OnRecvEditMsg", fromId));
		}
		
		// callback
		mCallbackHandler.OnRecvEditMsg(fromId);
	}

	/**
	 * 会话状态改变回调
	 */
	@Override
	public void OnRecvTalkEvent(String userId, TalkEventType eventType) 
	{
		// 更新用户状态
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null == userItem) {
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, userId:%s, eventType:%s", "LiveChatManager", "OnRecvTalkEvent", userId, eventType.name()));
			return;
		}
		boolean contactChange = userItem.setChatTypeWithEventType(eventType);
		
		// 添加联系人
		contactChange = contactChange || mContactMgr.AddContactWithUserItem(userItem);
		
		// callback
		mCallbackHandler.OnRecvTalkEvent(userItem);
		// 联系人状态改变 callback
		if (contactChange) {
			mCallbackHandler.OnContactListChange();
		}
	}

	/**
	 * 收到EMF消息通知回调
	 */
	@Override
	public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType) 
	{
		mCallbackHandler.OnRecvEMFNotice(fromId, noticeType);
	}

	/**
	 * 被踢下线回调
	 */
	@Override
	public void OnRecvKickOffline(KickOfflineType kickType) 
	{
		Log.d("LiveChatManager", "LiveChatManager::OnRecvKickOffline() kickType:%s", kickType.name());
		
		// 用户在其它地方登录，被踢下线
		if (kickType == KickOfflineType.OtherLogin)
		{
			// 设置不自动重登录
			mIsAutoRelogin = false;
			
			// LoginManager注销 
//			Message msg = Message.obtain();
//			msg.what = LiveChatRequestOptType.LoginManagerLogout.ordinal();
//			mHandler.sendMessage(msg);
	
			// 回调
			mCallbackHandler.OnRecvKickOffline(kickType);
		}
		
		Log.d("LiveChatManager", "LiveChatManager::OnRecvKickOffline() end");
	}
	
	/**
	 * 收到验证码通知回调
	 */
	@Override
	public void OnRecvIdentifyCode(byte[] data) 
	{
		mCallbackHandler.OnRecvIdentifyCode(data);
	}

	// --------------- 图片回调 ---------------
	/**
	 * 图片（私密照）消息回调
	 */
	@Override
	public void OnRecvPhoto(String toId, String fromId, String fromName,
			String inviteId, String photoId, String sendId, boolean charge,
			String photoDesc, int ticket) 
	{
		// 返回票根给服务器
		LiveChatClient.UploadTicket(fromId, ticket);
		
		// 更新用户信息
		LCUserItem userItem = mUserMgr.getUserItem(fromId);
		if (null == userItem) {
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, fromId:%s", "LiveChatManager", "OnRecvPhoto", fromId));
			return;
		}
		userItem.inviteId = inviteId;
		userItem.userName = fromName;
		boolean contactChange = SetUserOnlineStatus(userItem, UserStatusType.USTATUS_ONLINE);
		
		// 添加联系人
		contactChange = contactChange || mContactMgr.AddContactWithUserItem(userItem);
		
		// 生成MessageItem
		LCMessageItem item = new LCMessageItem();
		item.init(mMsgIdIndex.getAndIncrement()
				, SendType.Recv
				, fromId
				, toId
				, userItem.inviteId
				, StatusType.Finish);
		// 生成PhotoItem(女士收到的图片一定是已付费)
		LCPhotoItem photoItem = new LCPhotoItem();
		photoItem.init(photoId
				, sendId
				, photoDesc
				, mPhotoMgr.getPhotoPath(photoId, PhotoModeType.Fuzzy, PhotoSizeType.Large, false)
				, mPhotoMgr.getPhotoPath(photoId, PhotoModeType.Fuzzy, PhotoSizeType.Middle, false)
				, mPhotoMgr.getPhotoPath(photoId, PhotoModeType.Clear, PhotoSizeType.Original, false)
				, mPhotoMgr.getPhotoPath(photoId, PhotoModeType.Clear, PhotoSizeType.Large, false)
				, mPhotoMgr.getPhotoPath(photoId, PhotoModeType.Clear, PhotoSizeType.Middle, false)
				, true);
		// 把PhotoItem添加到MessageItem
		item.setPhotoItem(photoItem);
		
		// 添加到用户聊天记录中
		userItem.insertSortMsgList(item);
		
		// 联系人状态改变 callback
		if (contactChange) {
			mCallbackHandler.OnContactListChange();
		}
		// callback
		mCallbackHandler.OnRecvPhoto(item);
	}
	
	/**
	 * 图片（私密照）被查看回调
	 */
	@Override
	public void OnRecvShowPhoto(String toId, String fromId, String fromName,
			String inviteId, String photoId, String sendId, boolean charge,
			String photoDesc, int ticket) 
	{
		// 返回票根给服务器
		LiveChatClient.UploadTicket(fromId, ticket);
		
		// 更新用户信息
		LCUserItem userItem = mUserMgr.getUserItem(fromId);
		if (null == userItem) {
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, fromId:%s", "LiveChatManager", "OnRecvShowPhoto", fromId));
			return;
		}
		userItem.userName = fromName;
		boolean contactChange = SetUserOnlineStatus(userItem, UserStatusType.USTATUS_ONLINE);
		
		// 添加联系人
		contactChange = contactChange || mContactMgr.AddContactWithUserItem(userItem);
	
		// 联系人状态改变 callback
		if (contactChange) {
			mCallbackHandler.OnContactListChange();
		}
		// callback
		mCallbackHandler.OnRecvShowPhoto(userItem, photoId, photoDesc);
	}
	
	/**
	 * 获取图片（私密照）列表回调
	 */
	@Override
	public void OnGetPhotoList(boolean isSuccess, String errno, String errmsg, LCPhotoListAlbumItem[] albums, LCPhotoListPhotoItem[] photos)
	{
		mCallbackHandler.OnGetPhotoList(isSuccess, errno, errmsg, albums, photos);
	}
	
	// --------------- 视频回调 ---------------
	/**
	 * 视频被查看回调
	 */
	@Override
	public void OnRecvShowVideo(String toId, String fromId, String fromName,
			String inviteId, String videoId, String sendId, boolean charge,
			String videoDesc, int ticket) 
	{
		// 返回票根给服务器
		LiveChatClient.UploadTicket(fromId, ticket);
		
		// 更新用户信息
		LCUserItem userItem = mUserMgr.getUserItem(fromId);
		if (null == userItem) {
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, fromId:%s", "LiveChatManager", "OnRecvShowVideo", fromId));
			return;
		}
		userItem.userName = fromName;
		boolean contactChange = SetUserOnlineStatus(userItem, UserStatusType.USTATUS_ONLINE);
		
		// 添加联系人
		contactChange = contactChange || mContactMgr.AddContactWithUserItem(userItem);
	
		// 联系人状态改变 callback
		if (contactChange) {
			mCallbackHandler.OnContactListChange();
		}
		// callback
		mCallbackHandler.OnRecvShowVideo(userItem, videoId, videoDesc);
	}

	// --------------- 高级表情回调 ---------------
	/**
	 * 下载高级表情图片回调
	 * @param result		下载结果
	 * @param emotionItem	高级表情item
	 */
	@Override
	public void OnDownloadEmotionImage(boolean result, LCEmotionItem emotionItem) 
	{
		mCallbackHandler.OnGetEmotionImage(result, emotionItem);
	}
	
	/**
	 * 下载高级表情播放图片回调
	 * @param result		下载结果
	 * @param emotionItem	高级表情item
	 */
	@Override
	public void OnDownloadEmotionPlayImage(boolean result, LCEmotionItem emotionItem) 
	{
		mCallbackHandler.OnGetEmotionPlayImage(result, emotionItem);
	}
	
	/**
	 * 获取高级表情配置回调
	 */
	@Override
	public void OnGetEmotionConfig(boolean success, String errno, String errmsg, EmotionConfigItem item)
	{
		mCallbackHandler.OnGetEmotionConfig(success, errno, errmsg, item);
	}

	// --------------- IAuthorizationCallBack ---------------
	/**
	 * 通过 LoginItem 登录
	 * @param loginItem
	 */
	public void LoginWithLoginItem(LoginItem loginItem)
	{
		Log.d("LiveChatManager", "LoginWithLoginItem()");
		
		// 登录成功前一定已经获取同步配置成功
		if( !mIpList.isEmpty() && mPort != -1 && !StringUtil.isEmpty(mHost) ) 
		{
			// 注销
			Logout();
			
			// 初始化
			Init(mIpList.toArray(new String[mIpList.size()])
					, mPort 
					, mHost
					, mSiteId);
			
			// 登录
			TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			Login(loginItem.lady_id 
					, loginItem.sid 
					, RequestJni.GetDeviceId(tm)
					, loginItem.video);
		}
		else {
			Log.e("LiveChatManager", "LoginWithLoginItem() fail! mIpList.size:%d, mPort:%d, mHost:%s"
					, mIpList.size(), mPort, mHost);
		}
	}
	
	/**
	 * LoginManager回调（php登录回调）
	 */
	@Override
	public void OnLogin(OperateType operateType, boolean isSuccess,
			String errno, String errmsg, LoginItem item) 
	{
		Log.d("LiveChatManager", "OnLogin() operateType:%s, isSuccess:%b, errno:%s, errmsg:%s", operateType.name(), isSuccess, errno, errmsg);
		if (isSuccess) 
		{
			// 手动登录成功
			mSelfInfo.mRiskControl = !item.livechat;
			if (!mSelfInfo.mRiskControl) 
			{
				// 登录成功且没有风控则登录LiveChat
				Message msg = Message.obtain();
				msg.what = LiveChatRequestOptType.LoginWithLoginItem.ordinal();
				msg.obj = item;
				mHandler.sendMessage(msg);
			}
			else {
				// 被风控登录失败
				mCallbackHandler.OnLogin(LiveChatErrType.Fail, "", false);
			}
		}
		else if (!isSuccess) {
			// 登录失败（可能由于登录超时，重登录失败导致），注销LiveChat
			if(IsLogin()){
				Logout();
			}
		}
	}

	/**
	 * LoginManager注销回调（php注销回调）
	 */
	@Override
	public void OnLogout(OperateType operateType) 
	{
		if (OperateType.MANUAL == operateType) 
		{ 
			Logout();
		}
	}

	@Override
	public void OnSynConfig(boolean isSuccess, String errno, String errmsg,
			SynConfigItem item) 
	{
		Log.d("LiveChatManager", "OnSynConfig() isSuccess:%b, errno:%s, errmsg:%s", isSuccess, errno, errmsg);
		
		// 获取同步配置成功
		if (isSuccess)
		{
			// livechat站点
			mIpList.add(item.socketHost);
			// livechat port
			mPort = item.socketPort;
			// web site host
			mHost = WebsiteManager.getInstance().mWebSite.webSiteHost;
			// web site id
			mSiteId = WebsiteManager.getInstance().mWebSite.websiteId;
			
			// 获取同步配置完成
			Message msg = Message.obtain();
			msg.what = LiveChatRequestOptType.GetSynConfigFinish.ordinal();
			mHandler.sendMessage(msg);
		}
	}
	
	/**
	 * 获取同步配置完成
	 */
	private void GetSyncConfigFinish()
	{
		// 移除callback
		ConfigManagerJni.RemoveCallback(this);		
	}
	
	/******************************修改发送逻辑，优先检测功能是否开启，获取功能检测后， 走正常发送逻辑******************************************/
	
	/**
	 * 本地拦截，返回发送失败时，回调判断
	 * @param msgItem
	 */
	private void SendMessageFailCallback(LiveChatErrType errType, String errMsg, LCMessageItem msgItem){
		switch (msgItem.msgType) {
		case Text:
			mCallbackHandler.OnSendMessage(errType, errMsg, msgItem);
			break;
		case Emotion:
			mCallbackHandler.OnSendEmotion(errType, errMsg, msgItem);
			break;
		case Photo:
			mCallbackHandler.OnSendPhoto(errType, "", errMsg, msgItem);
			break;
		case Voice:
			mCallbackHandler.OnSendVoice(errType, "", errMsg, msgItem);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 功能不支持错误回掉
	 * @param msgItem
	 */
	private void onFunctionNotSupportedError(LCMessageItem msgItem){
		Log.i("LiveChatManager", "onFunctionNotSupportedError userId: " + msgItem.getUserItem().userId );
		msgItem.updateStatus(LiveChatErrType.NotSupportedFunction, "", "");
		String message = LCMessageHelper.getNoSupportMessage(mContext, msgItem.msgType);
		SendMessageFailCallback(LiveChatErrType.NotSupportedFunction, message, msgItem);
		BuildAndInsertSystemMsg(msgItem.getUserItem().userId, message);
	}
	
	/**
	 * 检测试聊券等，然后发送
	 * @param userItem
	 */
	private void CheckTryTicketAndSend(LCUserItem userItem){
		Log.i("LiveChatManager", "CheckTryTicketAndSend userItem userId: " + userItem.userId);
		if(userItem != null){
			if (IsSendMessageNow(userItem)){
				// 不需要检测直接发送
				Message msg = Message.obtain();
				msg.what = LiveChatRequestOptType.SendMessageList.ordinal();
				msg.obj = userItem.userId;
				mHandler.sendMessage(msg);
			}else {

			}
		}
	}
	/**
	 * 功能检测完成
	 * @param userId
	 */
	public void onFunctionCheckFinish(String userId){
		Log.i("LiveChatManager", "onFunctionCheckFinish UserId: " + userId);
		Message msg = Message.obtain();
		msg.what = LiveChatRequestOptType.CheckFunctionsFinish.ordinal();
		msg.obj = userId;
		mHandler.sendMessage(msg);
	}
}
