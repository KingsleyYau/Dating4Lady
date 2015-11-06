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
import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.livechat.LCMessageItem.SendType;
import com.qpidnetwork.livechat.LCMessageItem.StatusType;
import com.qpidnetwork.livechat.LCUserItem.ChatType;
import com.qpidnetwork.livechat.jni.LiveChatClient;
import com.qpidnetwork.livechat.jni.LiveChatClient.ClientType;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserSexType;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.livechat.jni.LiveChatUserStatus;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.WebsiteManager;
import com.qpidnetwork.request.ConfigManagerJni;
import com.qpidnetwork.request.OnConfigManagerCallback;
import com.qpidnetwork.request.OnOtherEmotionConfigCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.item.EmotionConfigItem;
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
	 * 用户ID
	 */
	private String mUserId = "";
	/**
	 * sid
	 */
	private String mSid = "";
	/**
	 * 设置唯一标识
	 */
	private String mDeviceId = "";
	/**
	 * 是否风控
	 */
	private boolean mRiskControl = false;
	/**
	 * 是否接收视频消息
	 */
	private boolean mIsRecvVideoMsg = true;
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
	 * 获取多个用户历史聊天记录的requestId
	 */
	private long mGetUsersHistoryMsgRequestId;
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
//	/**
//	 * 视频管理器
//	 */
//	private LCVideoManager mVideoMgr;
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
	private LCFeeContactManager mFeeContactMgr = null;
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
		GetEmotionConfig,			// 获取高级表情配置
		AutoRelogin,				// 执行自动重登录流程
		GetUsersHistoryMessage,		// 获取聊天历史记录
		SendMessageList,			// 发送指定用户的待发消息
		SendMessageListConnectFail,	// 处理指定用户的待发消息发送不成功(连接失败)
		LoginWithLoginItem,			// 收到OnLogin回调登录LiveChat
		GetBlockList,				// 获取黑名单列表
		GetLadyChatInfo,			// 获取女士聊天信息
		GetFeeRecentContactList,	// 获取最近扣费联系人列表
		UploadClientVersion,		// 上传客户端版本
		LoginManagerLogout,			// LoginManager注销
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
		mUserId = "";
		mSid = "";
		mDeviceId = "";
		mRiskControl = false;
		mIsRecvVideoMsg = true;
		mIsLogin = false;
		mIsAutoRelogin = false;
		mGetUsersHistoryMsgRequestId =  RequestJni.InvalidRequestId;
		mTextMgr = new LCTextManager();
		mEmotionMgr = new LCEmotionManager();
		mVoiceMgr = new LCVoiceManager();
		mPhotoMgr = new LCPhotoManager();
//		mVideoMgr = new LCVideoManager();
		mUserMgr = new LCUserManager();
		mBlockMgr = new LCBlockManager();
		mFeeContactMgr = new LCFeeContactManager();
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
				case GetEmotionConfig: {
					GetEmotionConfig();
				}break;
				case AutoRelogin: {
					AutoRelogin();
				}break;
				case GetUsersHistoryMessage: {
//					LiveChatManager liveChatMgr = LiveChatManager.newInstance(null);
//					ArrayList<LCUserItem> userArray = liveChatMgr.GetChatingUsers();
//					ArrayList<String> userIdArray = new ArrayList<String>();
//					for (LCUserItem item : userArray) {
//						if (null != item.userId && !item.userId.isEmpty()) {
//							userIdArray.add(item.userId);
//						}
//					}
//					String[] userIds = new String[userIdArray.size()];
//					userIdArray.toArray(userIds);
//					liveChatMgr.GetUsersHistoryMessage(userIds);
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
				}
				case UploadClientVersion: {
//					String verCode = String.valueOf(QpidApplication.versionCode);
//					LiveChatClient.UploadVer(verCode);
				}break;
				case LoginManagerLogout: {
//					if (null != LoginManager.getInstance()) {
//						LoginManager.getInstance().LogoutAndClean(true);
//					}
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
	
//	/**
//	 * 注册微视频(Video)回调
//	 * @param listener
//	 * @return
//	 */
//	public boolean RegisterVideoListener(LiveChatManagerVideoListener listener) 
//	{
//		return mCallbackHandler.RegisterVideoListener(listener);
//	}
//	
//	/**
//	 * 注销微视频(Video)回调
//	 * @param listener
//	 * @return
//	 */
//	public boolean UnregisterVideoListener(LiveChatManagerVideoListener listener) 
//	{
//		return mCallbackHandler.UnregisterVideoListener(listener);
//	}
//	
//	/**
//	 * 注册私密照(Photo)回调
//	 * @param listener
//	 * @return
//	 */
//	public boolean RegisterPhotoListener(LiveChatManagerPhotoListener listener) 
//	{
//		return mCallbackHandler.RegisterPhotoListener(listener);
//	}
//	
//	/**
//	 * 注销私密照(Photo)回调
//	 * @param listener
//	 * @return
//	 */
//	public boolean UnregisterPhotoListener(LiveChatManagerPhotoListener listener) 
//	{
//		return mCallbackHandler.UnregisterPhotoListener(listener);
//	}
//	
//	/**
//	 * 注册语音(Voice)回调
//	 * @param listener
//	 * @return
//	 */
//	public boolean RegisterVoiceListener(LiveChatManagerVoiceListener listener) 
//	{
//		return mCallbackHandler.RegisterVoiceListener(listener);
//	}
//	
//	/**
//	 * 注销语音(Voice)回调
//	 * @param listener
//	 * @return
//	 */
//	public boolean UnregisterVoiceListener(LiveChatManagerVoiceListener listener) 
//	{
//		return mCallbackHandler.UnregisterVoiceListener(listener);
//	}

	/**
	 * 初始化
	 * @param context		
	 * @param ips			LiveChat服务器IP数组
	 * @param port			LiveChat服务器端口
	 * @param webHost		网站host（如：http://www.chnlove.com）
	 * @param emotionPath	高级表情本地缓存目录路径
	 * @param photoPath		私密照本地缓存目录路径
	 * @param voicePath		语音本地缓存目录路径	
	 * @param logPath		log目录路径
	 * @return
	 */
	public boolean Init(String[] ips, int port, String webHost) 
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
			result = mEmotionMgr.init(mContext, emotionPath, webHost, logPath, this);
			
			// 初始化图片管理器
			String photoPath = FileCacheManager.getInstance().GetLCPhotoPath();
			result = result && mPhotoMgr.init(photoPath);
			
			// 初始化语音管理器
			String voicePath = FileCacheManager.getInstance().GetLCVoicePath();
			result = result && mVoiceMgr.init(voicePath);
			
			// 初始化视频管理器
//			String videoPath = FileCacheManager.getInstance().GetLCVideoPath();
//			result = result && mVideoMgr.init(videoPath);
			
			// 初始化LiveChatClient
			result = result && LiveChatClient.Init(this, ips, port);
		}
		
		if (result) 
		{
			// 初始化成功
			// 清除资源文件
			removeSourceFile();
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
		mUserId = null;
		mSid = null;
		mDeviceId = null;
		mRiskControl = false;
		mIsRecvVideoMsg = true;
		mMsgIdIndex.set(MsgIdIndexBegin);
		
		Log.d("LiveChatManager", "ResetParam() clear emotion begin");
		// 停止获取高级表情配置请求
		if (RequestJni.InvalidRequestId != mEmotionMgr.mEmotionConfigReqId) {
//			RequestJni.StopRequest(mEmotionMgr.mEmotionConfigReqId);
			mEmotionMgr.mEmotionConfigReqId = RequestJni.InvalidRequestId;
		}
//		Log.d("LiveChatManager", "ResetParam() clear emotion StopAllDownload3gp");
//		mEmotionMgr.StopAllDownload3gp();
		Log.d("LiveChatManager", "ResetParam() clear emotion StopAllDownloadImage");
		mEmotionMgr.StopAllDownloadImage();
		Log.d("LiveChatManager", "ResetParam() clear emotion removeAllSendingItems");
		mEmotionMgr.removeAllSendingItems();
		
		Log.d("LiveChatManager", "ResetParam() clear photo begin");
		// 停止所有图片请求
		mPhotoMgr.clearAllRequestItems();
//		ArrayList<Long> photoRequestIds = mPhotoMgr.clearAllRequestItems();
//		if (null != photoRequestIds) {
//			for (Iterator<Long> iter = photoRequestIds.iterator(); iter.hasNext(); ) {
//				long requestId = iter.next();
//				RequestJni.StopRequest(requestId);
//			}
//		}
		Log.d("LiveChatManager", "ResetParam() clear photo clearAllSendingItems");
		mPhotoMgr.clearAllSendingItems();
		
		Log.d("LiveChatManager", "ResetParam() clear voice begin");
		// 停止所有语音请求
		mVoiceMgr.clearAllRequestItem();
//		ArrayList<Long> voiceRequestIds = mVoiceMgr.clearAllRequestItem();
//		if (null != voiceRequestIds) {
//			for (Iterator<Long> iter = voiceRequestIds.iterator(); iter.hasNext(); ) {
//				long requestId = iter.next();
//				RequestJni.StopRequest(requestId);
//			}
//		}
		Log.d("LiveChatManager", "ResetParam() clear voice clearAllSendingItems");
		mVoiceMgr.clearAllSendingItems();
		
		Log.d("LiveChatManager", "ResetParam() clear other begin");
		mTextMgr.removeAllSendingItems();
		Log.d("LiveChatManager", "ResetParam() clear other removeAllUserItem");
		mUserMgr.removeAllUserItem();
	}
	
	/**
	 * 清除所有图片、视频等资源文件
	 */
	private void removeSourceFile()
	{
		// 清除图片文件
		mPhotoMgr.removeAllPhotoFile();
//		// 清除视频文件
//		mVideoMgr.removeAllVideoFile();
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
			result = LiveChatClient.Login(userId, sid, deviceId, ClientType.CLIENT_ANDROID, UserSexType.USER_SEX_FEMALE);
			if (result) 
			{
				mIsAutoRelogin = true;
				mUserId = userId;
				mSid = sid;
				mDeviceId = deviceId;
				mIsRecvVideoMsg = isRecvVideoMsg;
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
		Log.d("LiveChatManager", "LiveChatManager::AutoRelogin() begin, mUserId:%s, mSid:%s, mDeviceId:%s", mUserId, mSid, mDeviceId);
		
		if (null != mUserId && !mUserId.isEmpty()
			&& null != mSid && !mSid.isEmpty()
			&& null != mDeviceId && !mDeviceId.isEmpty())
		{
			Login(mUserId, mSid, mDeviceId, mIsRecvVideoMsg);
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
		if (!mRiskControl
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
		// log打印
		String context = "";
		for (int i = 0; i < userIds.length; i++) {
			if (!context.isEmpty()) {
				context += ",";
			}
			
			context += userIds[i];
		}
		Log.d("LiveChatManager", "GetUsersInfo() mIsLogin:%b, context:%s", mIsLogin, context);
		
		return LiveChatClient.GetUsersInfo(userIds);
	}
	
	/**
	 * 获取单个用户历史聊天记录（包括文本、高级表情、语音、图片）
	 * @param userId	用户ID
	 * @return
	 */
	public boolean GetHistoryMessage(String userId)
	{
		boolean result = false;
//		LCUserItem userItem = mUserMgr.getUserItem(userId);
//		if (null != userItem) {
//			if (userItem.getMsgList().size() > 0
//				&& mGetUsersHistoryMsgRequestId == RequestJni.InvalidRequestId) // 未完成获取多个用户历史聊天记录的请求
//			{
//				result = true;
//				mCallbackHandler.OnGetHistoryMessage(true, "", "", userItem);
//			}
//			else if (!userItem.inviteId.isEmpty()) 
//			{
//				long requestId = RequestOperator.getInstance().QueryChatRecord(userItem.inviteId, new OnQueryChatRecordCallback() {
//					
//					@Override
//					public void OnQueryChatRecord(boolean isSuccess, String errno,
//							String errmsg, int dbTime, Record[] recordList, String inviteId) 
//					{
//						// TODO Auto-generated method stub
//						
//						// 设置服务器当前数据库时间
//						LCMessageItem.SetDbTime(dbTime);
//						
//						// 插入聊天记录
//						LCUserItem userItem = mUserMgr.getUserItemWithInviteId(inviteId);
//						if (isSuccess && userItem != null) {
//							// 清除已完成的记录（保留未完成发送的记录） 
//							userItem.clearFinishedMsgList();
//							// 插入历史记录
//							for (int i = 0; i < recordList.length; i++) 
//							{
//								LCMessageItem item = new LCMessageItem();
//								if (item.InitWithRecord(
//										mMsgIdIndex.getAndIncrement(), 
//										mUserId, 
//										userItem.userId,
//										userItem.inviteId,
//										recordList[i], 
//										mEmotionMgr, 
//										mVoiceMgr, 
//										mPhotoMgr,
//										mVideoMgr)) 
//								{
//									userItem.insertSortMsgList(item);
//								}
//							}
//							// 合并图片聊天记录
//							mPhotoMgr.combineMessageItem(userItem.msgList);
//							// 合并视频聊天记录
//							mVideoMgr.combineMessageItem(userItem.msgList);
//						}
//						mCallbackHandler.OnGetHistoryMessage(isSuccess, errno, errmsg, userItem);
//					}
//				});
//				result = (requestId != RequestJni.InvalidRequestId); 
//			}
//		}
		
		return result;
	}
	
	/**
	 * 获取多个用户历史聊天记录（包括文本、高级表情、语音、图片）
	 * @param userIds	用户ID数组
	 * @return
	 */
	public boolean GetUsersHistoryMessage(String[] userIds)
	{
		boolean result = false;
//		ArrayList<String> inviteIds = new ArrayList<String>();  
//		for (int i = 0; i < userIds.length; i++) {
//			if (!userIds[i].isEmpty()) {
//				LCUserItem userItem = mUserMgr.getUserItem(userIds[i]);
//				if (null != userItem)
//				{
//					if (!userItem.inviteId.isEmpty()) 
//					{
//						inviteIds.add(userItem.inviteId);
//					}
//				}
//			}
//		}
//		if (inviteIds.size() > 0) {
//			String[] inviteArray = new String[inviteIds.size()];
//			inviteIds.toArray(inviteArray);
//			mGetUsersHistoryMsgRequestId = RequestOperator.getInstance().QueryChatRecordMutiple(inviteArray, new OnQueryChatRecordMutipleCallback() {
//				
//				@Override
//				public void OnQueryChatRecordMutiple(boolean isSuccess, String errno, 
//						String errmsg, int dbTime, RecordMutiple[] recordMutipleList) 
//				{
//					// TODO Auto-generated method stub
//					LCUserItem[] userArray = null;
//					ArrayList<LCUserItem> userList = new ArrayList<LCUserItem>();
//					if (isSuccess
//						&& null != recordMutipleList) 
//					{
//						// 设置服务器当前数据库时间
//						LCMessageItem.SetDbTime(dbTime);
//						
//						// 插入聊天记录
//						for (int i = 0; i < recordMutipleList.length; i++) 
//						{
//							RecordMutiple record = recordMutipleList[i];
//							LCUserItem userItem = mUserMgr.getUserItemWithInviteId(record.inviteId);
//							if (null != record.recordList 
//								&& userItem != null) 
//							{
//								// 清除已完成的记录（保留未完成发送的记录） 
//								userItem.clearFinishedMsgList();
//								// 服务器返回的历史消息是倒序排列的
//								for (int k = record.recordList.length - 1; k >= 0; k--) 
//								{
//									LCMessageItem item = new LCMessageItem();
//									if (item.InitWithRecord(
//											mMsgIdIndex.getAndIncrement(), 
//											mUserId, 
//											userItem.userId,
//											userItem.inviteId,
//											record.recordList[k],
//											mEmotionMgr, 
//											mVoiceMgr, 
//											mPhotoMgr, 
//											mVideoMgr)) 
//									{
//										userItem.insertSortMsgList(item);
//									}
//								}
//								
//								// 合并图片聊天记录
//								mPhotoMgr.combineMessageItem(userItem.msgList);
//								// 合并视频聊天记录
//								mVideoMgr.combineMessageItem(userItem.msgList);
//								// 添加到用户数组
//								userList.add(userItem);
//							}
//						}
//						
//						userArray = new LCUserItem[userList.size()];
//						userList.toArray(userArray);
//					}
////					mCallbackHandler.OnGetUsersHistoryMessage(isSuccess, errno, errmsg, userArray);
//					
//					// 重置ReuqestId
//					mGetUsersHistoryMsgRequestId = RequestJni.InvalidRequestId;
//				}
//			});
//			result = (mGetUsersHistoryMsgRequestId != RequestJni.InvalidRequestId); 
//		}
		
		return result;
	}
	
	/**
	 * 插入历史聊天记录（msgId及createTime由LiveChatManager生成）
	 * @param userId	对方用户ID
	 * @param item		消息item
	 * @return
	 */
	public boolean InsertHistoryMessage(String userId, LCMessageItem item) {
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
	public ArrayList<LCUserItem> GetContactUsers()
	{
		return mUserMgr.getContactUsers();
	}
	
	/**
	 * 获取女士邀请用户列表
	 * @return
	 */
	public ArrayList<LCUserItem> GetWomanInviteUsers()
	{
		return mUserMgr.getWomanInviteUsers();
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
				SendMessageItem(item);
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
					item.statusType = StatusType.Fail;
				}
				
				@SuppressWarnings("unchecked")
				ArrayList<LCMessageItem> cloneMsgList = (ArrayList<LCMessageItem>)userItem.sendMsgList.clone();
				userItem.sendMsgList.clear();
				
				mCallbackHandler.OnSendMessageListFail(errType, cloneMsgList);
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
//			SendPhotoProc(item);
			break;
		case Voice:
//			SendVoiceProc(item);
			break;
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
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, userId:%s", "LiveChatManager", "SendPhoto", userId));
			return null;
		}
		
		// 构造消息item
		LCMessageItem item = null;
		if (!message.isEmpty()) {
			// 生成MessageItem
			item = new LCMessageItem();
			item.init(mMsgIdIndex.getAndIncrement()
					, SendType.Send
					, mUserId
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
			
			if (IsWaitForLoginToSendMessage(userItem)) 
			{
				// 登录未成功，添加到待发送列表
				userItem.sendMsgList.add(item);
			}
			else if (IsSendMessageNow(userItem)) 
			{
				// 发送消息
				SendMessageProc(item);
			}
			else 
			{
				// 未能发送 
			}
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
		if (LiveChatClient.SendMessage(item.toId, item.getTextItem().message, item.getTextItem().illegal, item.msgId)) {
			mTextMgr.addSendingItem(item);
		}
		else {
			item.statusType = StatusType.Fail;
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
	 * 生成警告消息
	 * @param userItem	用户item
	 * @param message	警告消息内容
	 * @param linkMsg	链接内容
	 */
	private void BuildAndInsertWarning(LCUserItem userItem, String message, String linkMsg) {
		if (!message.isEmpty()) {
			// 生成warning消息
			LCWarningItem warningItem = new LCWarningItem();
			if (!linkMsg.isEmpty()) {
				LCWarningLinkItem linkItem = new LCWarningLinkItem();
//				linkItem.init(linkMsg, LinkOptType.Rechange);
				warningItem.initWithLinkMsg(message, linkItem);
				warningItem.linkItem = linkItem;
			}
			// 生成message消息
			LCMessageItem item = new LCMessageItem();
			item.init(mMsgIdIndex.getAndIncrement(), SendType.System, userItem.userId, mUserId, userItem.inviteId, StatusType.Finish);
			item.setWarningItem(warningItem);
			// 插入到聊天记录列表中
			userItem.insertSortMsgList(item);
			// 回调
			mCallbackHandler.OnRecvWarning(item);
		}
	}
	
	/**
	 * 
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
			item.init(mMsgIdIndex.getAndIncrement(), SendType.System, userId, mUserId, userItem.inviteId, StatusType.Finish);
			item.setSystemItem(systemItem);
			userItem.insertSortMsgList(item);
			mCallbackHandler.OnRecvSystemMsg(item);
			
			result = true;
		}
		return result;
	}
	
	// ---------------- 高级表情操作函数(Emotion) ----------------
	/**
	 * 获取高级表情配置
	 */
	public synchronized boolean GetEmotionConfig()
	{
		if (mEmotionMgr.mEmotionConfigReqId != RequestJni.InvalidRequestId) {
			return true;
		}
		
		mEmotionMgr.mEmotionConfigReqId = RequestJniOther.EmotionConfig(new OnOtherEmotionConfigCallback() {
			
			@Override
			public void OnOtherEmotionConfig(boolean isSuccess, String errno,
					String errmsg, EmotionConfigItem item) {
				// TODO Auto-generated method stub
				Log.d("LiveChatManager", "GetEmotionConfig() OnOtherEmotionConfig begin");
				boolean success = isSuccess;
				EmotionConfigItem configItem = item;
				if (isSuccess) {
					// 请求成功
					if (mEmotionMgr.IsVerNewThenConfigItem(item.version)) {
						// 配置版本更新
						success = mEmotionMgr.UpdateConfigItem(item);
					}
					else {
						// 使用旧配置
						configItem = mEmotionMgr.GetConfigItem();
					}
				}
				Log.d("LiveChatManager", "GetEmotionConfig() OnOtherEmotionConfig callback");
				mCallbackHandler.OnGetEmotionConfig(success, errno, errmsg, configItem);
				mEmotionMgr.mEmotionConfigReqId = RequestJni.InvalidRequestId;
				Log.d("LiveChatManager", "GetEmotionConfig() OnOtherEmotionConfig end");
			}
		});
		return mEmotionMgr.mEmotionConfigReqId != RequestJni.InvalidRequestId;
	}
	
	/**
	 * 获取配置item（PS：本次获取可能是旧的，当收到OnGetEmotionConfig()回调时，需要重新调用本函数获取）
	 * @return
	 */
	public EmotionConfigItem GetEmotionConfigItem() {
		return mEmotionMgr.GetConfigItem();
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
			Log.e("LiveChatManager", String.format("%s::%s() getUserItem fail, userId:%s", "LiveChatManager", "SendPhoto", userId));
			return null;
		}
		
		LCMessageItem item = null;
		if (!emotionId.isEmpty()) {
			// 生成MessageItem
			item = new LCMessageItem();
			item.init(mMsgIdIndex.getAndIncrement()
					, SendType.Send
					, mUserId
					, userId
					, userItem.inviteId
					, StatusType.Processing);
			// 获取EmotionItem
			LCEmotionItem emotionItem = mEmotionMgr.getEmotion(emotionId);
			// 把EmotionItem添加到MessageItem
			item.setEmotionItem(emotionItem);
			// 添加到历史记录
			userItem.insertSortMsgList(item);

			if (IsSendMessageNow(userItem)) 
			{
				// 发送消息
				SendEmotionProc(item);
			}
			else if (IsWaitForLoginToSendMessage(userItem)) 
			{
				// 登录未成功，添加到待发送列表
				userItem.sendMsgList.add(item);
			}
			else 
			{
				// 正在使用试聊券，消息添加到待发列表
				userItem.sendMsgList.add(item);
			}
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
			item.statusType = StatusType.Fail;
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
				if (emotionItem.playBigImages.size() > 0) {
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
			mUserId = null;
			mSid = null;
			mDeviceId = null;
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
	 * 获取用户在线状态回调
	 * @param errType			处理结果类型
	 * @param errmsg			处理结果描述
	 * @param userStatusArray	用户在线状态数组
	 */
	@Override
	public void OnGetUserStatus(LiveChatErrType errType, String errmsg,
			LiveChatUserStatus[] userStatusArray) 
	{
		mCallbackHandler.OnGetUserStatus(errType, errmsg, userStatusArray);
	}
	
	@Override
	public void OnRecvTryTalkBegin(String toId, String fromId, int time) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvTryTalkEnd(String userId) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetTalkInfo(LiveChatErrType errType, String errmsg,
			String userId, String invitedId, boolean charget, int chatTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSendMessage(LiveChatErrType errType, String errmsg,
			String userId, String message, int ticket) 
	{
		// TODO Auto-generated method stub
		LCMessageItem item = mTextMgr.getAndRemoveSendingItem(ticket);
		if (null != item) {
			item.statusType = (errType==LiveChatErrType.Success ? StatusType.Finish : StatusType.Fail);
			mCallbackHandler.OnSendMessage(errType, errmsg, item);
		}
		else {
			Log.e("livechat", String.format("%s::%s() get sending item fail, ticket:%d", "LiveChatManager", "OnSendMessage", ticket));
		}
		
		// 生成警告消息
		if (errType != LiveChatErrType.Success) {
			if (null != item && null != item.getUserItem()) {
				BuildAndInsertWarningWithErrType(item.getUserItem(), errType);
			}
		}
		
		Log.d("livechat", "OnSendMessage() errType:%s, userId:%s, message:%s", errType.name(), userId, message);
	}

	@Override
	public void OnSendEmotion(LiveChatErrType errType, String errmsg,
			String userId, String emotionId, int ticket) 
	{
		// TODO Auto-generated method stub
		LCMessageItem item = mEmotionMgr.getAndRemoveSendingItem(ticket);
		if (null != item) {
			item.statusType = (errType==LiveChatErrType.Success ? StatusType.Finish : StatusType.Fail);
			mCallbackHandler.OnSendEmotion(errType, errmsg, item);
		}
		else {
			Log.e("livechat", String.format("%s::%s() get sending item fail, ticket:%d", "LiveChatManager", "OnSendEmotion", ticket));
		}
		
		// 生成警告消息
		if (errType != LiveChatErrType.Success) {
			if (null != item && null != item.getUserItem()) {
				BuildAndInsertWarningWithErrType(item.getUserItem(), errType);
			}
		}
	}

	@Override
	public void OnSendVGift(LiveChatErrType errType, String errmsg,
			String userId, String giftId, int ticket) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSendVoice(LiveChatErrType errType, String errmsg,
			String userId, String voiceId, int ticket) 
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void OnRecvLadyVoiceCode(String code) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSendPhoto(LiveChatErrType errType, String errmsg, int ticket) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnShowPhoto(LiveChatErrType errType, String errmsg, int ticket) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 获取单个用户信息（已改为使用 GetUsersInfo()）
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param item		用户信息
	 */
	@Override
	public void OnGetUserInfo(LiveChatErrType errType, String errmsg,
			LiveChatTalkUserListItem item) {
		// TODO Auto-generated method stub
		// 已改为使用 GetUsersInfo() 
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
		// 若用户在联系人里，则更新信息
		boolean contactChange = false;
		for (int i = 0; i < list.length; i++)
		{
			if (mUserMgr.isUserExists(list[i].userId))
			{
				LCUserItem userItem = mUserMgr.getUserItem(list[i].userId);
				userItem.UpdateWithLiveChatTalkUserListItem(list[i]);
				contactChange = true;
			}
		}
		
		// callback
		mCallbackHandler.OnGetUsersInfo(errType, errmsg, seq, list);
		// 联系人更新回调
		if (contactChange) {
			mCallbackHandler.OnContactStatusChange();
		}
	}

	@Override
	public void OnGetBlockList(LiveChatErrType errType, String errmsg,
			LiveChatTalkUserListItem[] list) {
		// TODO Auto-generated method stub
		// log打印
		String blockLog = "";
		for (int i = 0; i < list.length; i++) 
		{
			// log打印
			if (!blockLog.isEmpty()) {
				blockLog += ",";
			}
			blockLog += list[i].userName;
			blockLog += "(" + list[i].userId + ")";
		}
		Log.d("LiveChatManager", "LiveChatManager::OnGetBlockList() errType:%s, errmsg:%s, block:%s"
				, errType.name(), errmsg, blockLog);
		
		// 更新黑名单
		mBlockMgr.UpdateWithBlockList(list);
	}

	@Override
	public void OnGetContactList(LiveChatErrType errType, String errmsg,
			LiveChatTalkUserListItem[] list) {
		// TODO Auto-generated method stub
		
		// log打印
		String log = "";
		for (int i = 0; i < list.length; i++) 
		{
			// log打印
			if (!log.isEmpty()) {
				log += ",";
			}
			log += list[i].userName;
			log += "(" + list[i].userId + ")";
		}
		Log.d("LiveChatManager", "LiveChatManager::OnGetContactList() errType:%s, errmsg:%s, contact:%s"
				, errType.name(), errmsg, log);
		
		// 暂时没用
	}

	@Override
	public void OnGetRecentContactList(LiveChatErrType errType, String errmsg,
			String[] userIds) {
		// TODO Auto-generated method stub

		// log打印
		String log = "";
		for (int i = 0; i < userIds.length; i++) 
		{
			// log打印
			if (!log.isEmpty()) {
				log += ",";
			}
			log += userIds[i];
		}
		Log.d("LiveChatManager", "LiveChatManager::OnGetRecentContactList() errType:%s, errmsg:%s, RecentContact:%s"
				, errType.name(), errmsg, log);
		// 暂时没用
	}

	@Override
	public void OnGetFeeRecentContactList(LiveChatErrType errType,
			String errmsg, String[] userIds) 
	{
		// TODO Auto-generated method stub
		
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
		}
		Log.d("LiveChatManager", "LiveChatManager::OnGetFeeRecentContactList() errType:%s, errmsg:%s, userIds:%s"
				, errType.name(), errmsg, userLog);
		
		// 获取用户信息
		GetUsersInfo(userIds);
	}

	@Override
	public void OnReplyIdentifyCode(LiveChatErrType errType, String errmsg) 
	{
		// log打印 
		Log.d("LiveChatManager", "LiveChatManager::OnReplyIdentifyCode() errType:%s, errmsg:%s"
				, errType.name(), errmsg);
		
		// callback
		mCallbackHandler.OnReplyIdentifyCode(errType, errmsg);
	}

	@Override
	public void OnGetLadyChatInfo(LiveChatErrType errType, String errmsg,
			String[] chattingUserIds, String[] chattingInviteIds,
			String[] missingUserIds, String[] missingInviteIds) {
		// TODO Auto-generated method stub
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
			}
		}
		
		Log.d("LiveChatManager", "LiveChatManager::OnGetLadyChatInfo() errType:%s, errmsg:%s, chatting:%s, missing:%s"
				, errType.name(), errmsg, chattingLog, missingLog);
	}

	@Override
	public void OnSearchOnlineMan(LiveChatErrType errType, String errmsg,
			String[] userIds) {
		// TODO Auto-generated method stub
		
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
	 * @param charget	是否已付费
	 * @param ticket	票根
	 * @param msgType	聊天消息类型
	 * @param message	消息内容
	 */
	@Override
	public void OnRecvMessage(String toId, String fromId, String fromName,
			String inviteId, boolean charget, int ticket, TalkMsgType msgType,
			String message) 
	{
		// 返回票根给服务器
		LiveChatClient.UploadTicket(fromId, ticket);
		
		// 判断是否处理消息
		if (!mBlockMgr.IsExist(fromId))
		{
			// 添加用户到列表中（若列表中用户不存在）
			LCUserItem userItem = mUserMgr.getUserItem(fromId);
			if (null == userItem) {
				Log.e("livechat", String.format("%s::%s() getUserItem fail, fromId:%s", "LiveChatManager", "OnRecvMessage", fromId));
				return;
			}
			userItem.inviteId = inviteId;
			userItem.userName = fromName;
			boolean statusChange = userItem.setChatTypeWithTalkMsgType(charget, msgType);
			statusChange = statusChange || SetUserOnlineStatus(userItem, UserStatusType.USTATUS_ONLINE);
			
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
			
			// callback
			mCallbackHandler.OnRecvMessage(item);
			// 联系人状态改变 callback
			if (statusChange) {
				mCallbackHandler.OnContactStatusChange();
			}
		}
	}

	/**
	 * 接收高级表情消息回调
	 * @param toId		接收者ID
	 * @param fromId	发送者ID
	 * @param fromName	发送者用户名
	 * @param inviteId	邀请ID
	 * @param charget	是否已付费
	 * @param ticket	票根
	 * @param msgType	聊天消息类型
	 * @param emotionId	高级表情ID
	 */
	@Override
	public void OnRecvEmotion(String toId, String fromId, String fromName,
			String inviteId, boolean charget, int ticket, TalkMsgType msgType,
			String emotionId) 
	{
		// TODO Auto-generated method stub
		// 返回票根给服务器
		LiveChatClient.UploadTicket(fromId, ticket);
		
		// 添加用户到列表中（若列表中用户不存在）
		LCUserItem userItem = mUserMgr.getUserItem(fromId);
		if (null == userItem) {
			Log.e("livechat", String.format("%s::%s() getUserItem fail, fromId:%s", "LiveChatManager", "OnRecvEmotion", fromId));
			return;
		}
		userItem.inviteId = inviteId;
		userItem.userName = fromName;
		boolean statusChange = userItem.setChatTypeWithTalkMsgType(charget, msgType);
		statusChange = statusChange || SetUserOnlineStatus(userItem, UserStatusType.USTATUS_ONLINE);
		
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
		
		// callback
		mCallbackHandler.OnRecvEmotion(item);
		// 联系人状态改变 callback
		if (statusChange) {
			mCallbackHandler.OnContactStatusChange();
		}
	}

	/**
	 * 接收语音消息回调
	 * @param toId		接收者ID
	 * @param fromId	发送者ID
	 * @param fromName	发送者用户名
	 * @param inviteId	邀请ID
	 * @param charget	是否已付费
	 * @param msgType	聊天消息类型
	 * @param voiceId	语音ID
	 * @param fileType	语音文件类型
	 * @param timeLen	语音时长
	 */
	@Override
	public void OnRecvVoice(String toId, String fromId, String fromName,
			String inviteId, boolean charget, TalkMsgType msgType,
			String voiceId, String fileType, int timeLen) 
	{
		// TODO Auto-generated method stub
		// 添加用户到列表中（若列表中用户不存在）
		LCUserItem userItem = mUserMgr.getUserItem(fromId);
		if (null == userItem) {
			Log.e("livechat", String.format("%s::%s() getUserItem fail, fromId:%s", "LiveChatManager", "OnRecvVoice", fromId));
			return;
		}
		userItem.userName = fromName;
		userItem.inviteId = inviteId;
		boolean statusChange = userItem.setChatTypeWithTalkMsgType(charget, msgType);
		statusChange = statusChange || SetUserOnlineStatus(userItem, UserStatusType.USTATUS_ONLINE);
		
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
				, charget);
		
		// 把VoiceItem添加到MessageItem
		item.setVoiceItem(voiceItem);
		
		// 添加到聊天记录中
		userItem.insertSortMsgList(item);
		
		// callback
		mCallbackHandler.OnRecvVoice(item);
		// 联系人状态改变 callback
		if (statusChange) {
			mCallbackHandler.OnContactStatusChange();
		}
	}

	/**
	 * 接收警告消息回调
	 * @param toId		接收者ID
	 * @param fromId	发送者ID
	 * @param fromName	发送者用户名
	 * @param inviteId	邀请ID
	 * @param charget	是否已付费
	 * @param ticket	票根
	 * @param msgType	聊天消息类型
	 * @param message	消息内容
	 */
	@Override
	public void OnRecvWarning(String toId, String fromId, String fromName,
			String inviteId, boolean charget, int ticket, TalkMsgType msgType,
			String message) 
	{
		// TODO Auto-generated method stub
		// 返回票根给服务器
		LiveChatClient.UploadTicket(fromId, ticket);

		// 添加用户到列表中（若列表中用户不存在）
		LCUserItem userItem = mUserMgr.getUserItem(fromId);
		if (null == userItem) {
			Log.e("livechat", String.format("%s::%s() getUserItem fail, fromId:%s", "LiveChatManager", "OnRecvWarning", fromId));
			return;
		}
		userItem.inviteId = inviteId;
		userItem.userName = fromName;
		boolean statusChange = userItem.setChatTypeWithTalkMsgType(charget, msgType);
		statusChange = statusChange || SetUserOnlineStatus(userItem, UserStatusType.USTATUS_ONLINE);
		
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
		
		// callback
		mCallbackHandler.OnRecvWarning(item);
		// 联系人状态改变 callback
		if (statusChange) {
			mCallbackHandler.OnContactStatusChange();
		}
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
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null == userItem) {
			Log.e("livechat", String.format("%s::%s() getUserItem fail, userId:%s", "LiveChatManager", "OnUpdateStatus", userId)); 
			return;
		}
		userItem.clientType = clientType;
		boolean statusChange = SetUserOnlineStatus(userItem, statusType);
		
		// callback
		mCallbackHandler.OnUpdateStatus(userItem);
		// 联系人状态改变 callback
		if (statusChange) {
			mCallbackHandler.OnContactStatusChange();
		}
	}

	@Override
	public void OnUpdateTicket(String fromId, int ticket) 
	{
		// TODO Auto-generated method stub
		// 不用处理
	}

	@Override
	public void OnRecvEditMsg(String fromId) 
	{
		mCallbackHandler.OnRecvEditMsg(fromId);
	}

	@Override
	public void OnRecvTalkEvent(String userId, TalkEventType eventType) 
	{
		// TODO Auto-generated method stub
		LCUserItem userItem = mUserMgr.getUserItem(userId);
		if (null == userItem) {
			Log.e("livechat", String.format("%s::%s() getUserItem fail, userId:%s, eventType:%s", "LiveChatManager", "OnRecvTalkEvent", userId, eventType.name()));
			return;
		}
		boolean statusChange = userItem.setChatTypeWithEventType(eventType);
		
		mCallbackHandler.OnRecvTalkEvent(userItem);
		// 联系人状态改变 callback
		if (statusChange) {
			mCallbackHandler.OnContactStatusChange();
		}
	}

	@Override
	public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType) 
	{
		// TODO Auto-generated method stub
		mCallbackHandler.OnRecvEMFNotice(fromId, noticeType);
	}

	@Override
	public void OnRecvKickOffline(KickOfflineType kickType) 
	{
		// TODO Auto-generated method stub
		Log.d("livechat", "LiveChatManager::OnRecvKickOffline() kickType:%s", kickType.name());
		
		// 用户在其它地方登录，被踢下线
		if (kickType == KickOfflineType.OtherLogin)
		{
			// 设置不自动重登录
			mIsAutoRelogin = false;
			
			// LoginManager注销 
			Message msg = Message.obtain();
			msg.what = LiveChatRequestOptType.LoginManagerLogout.ordinal();
			mHandler.sendMessage(msg);
	
			// 回调
			mCallbackHandler.OnRecvKickOffline(kickType);
		}
		
		Log.d("livechat", "LiveChatManager::OnRecvKickOffline() end");
	}

	@Override
	public void OnRecvPhoto(String toId, String fromId, String fromName,
			String inviteId, String photoId, String sendId, boolean charget,
			String photoDesc, int ticket) 
	{
		// TODO Auto-generated method stub
//		// 返回票根给服务器
//		LiveChatClient.UploadTicket(fromId, ticket);
//		
//		// 添加用户到列表中（若列表中用户不存在）
//		LCUserItem userItem = mUserMgr.getUserItem(fromId);
//		if (null == userItem) {
//			Log.e("livechat", String.format("%s::%s() getUserItem fail, fromId:%s", "LiveChatManager", "OnRecvPhoto", fromId));
//			return;
//		}
//		userItem.inviteId = inviteId;
//		userItem.userName = fromName;
////		userItem.statusType = UserStatusType.USTATUS_ONLINE;
//		SetUserOnlineStatus(userItem, UserStatusType.USTATUS_ONLINE);
//		
//		// 生成MessageItem
//		LCMessageItem item = new LCMessageItem();
//		item.init(mMsgIdIndex.getAndIncrement()
//				, SendType.Recv
//				, fromId
//				, toId
//				, userItem.inviteId
//				, StatusType.Finish);
//		// 生成PhotoItem
//		LCPhotoItem photoItem = new LCPhotoItem();
//		photoItem.init(photoId
//				, sendId
//				, photoDesc
//				, mPhotoMgr.getPhotoPath(photoId, PhotoModeType.Fuzzy, PhotoSizeType.Large)
//				, mPhotoMgr.getPhotoPath(photoId, PhotoModeType.Fuzzy, PhotoSizeType.Middle)
//				, mPhotoMgr.getPhotoPath(photoId, PhotoModeType.Clear, PhotoSizeType.Original)
//				, mPhotoMgr.getPhotoPath(photoId, PhotoModeType.Clear, PhotoSizeType.Large)
//				, mPhotoMgr.getPhotoPath(photoId, PhotoModeType.Clear, PhotoSizeType.Middle)
//				, charget);
//		// 把PhotoItem添加到MessageItem
//		item.setPhotoItem(photoItem);
//		
//		// 添加到用户聊天记录中
//		userItem.insertSortMsgList(item);
//		
//		// callback
//		mCallbackHandler.OnRecvPhoto(item);
	}

	@Override
	public void OnRecvIdentifyCode(Byte[] data) 
	{
		String filePath = "";
		
		// callback
		mCallbackHandler.OnRecvIdentifyCode(filePath);
	}
	
	// --------------- 高级表情下载回调 ---------------
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

	// --------------- IAuthorizationCallBack ---------------
	/**
	 * 通过 LoginItem 登录
	 * @param loginItem
	 */
	public void LoginWithLoginItem(LoginItem loginItem)
	{
		Log.d("LiveChatManager", "LoginWithLoginItem()");
		
		// 登录成功前一定已经获取同步配置成功
		if( mIpList.size() > 0 && mPort != -1 && !StringUtil.isEmpty(mHost) ) 
		{
			// 注销
			Logout();
			
			// 初始化
			Init(mIpList.toArray(new String[mIpList.size()])
					, mPort 
					, mHost);
			
			// 登录
			String password = LoginManager.getInstance().GetLoginParam().password;
			loginItem = LoginManager.getInstance().GetLoginItem();
			TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			Login(loginItem.lady_id 
					, password 
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
			String errno, String errmsg, LoginItem item) {
		// TODO Auto-generated method stub
		
		Log.d("LiveChatManager", "OnLogin() operateType:%s, isSuccess:%b, errno:%s, errmsg:%s", operateType.name(), isSuccess, errno, errmsg);
		// 手动登录成功
		if (OperateType.MANUAL == operateType
				&& isSuccess) 
		{
			mRiskControl = !item.livechat;
			if (!mRiskControl) 
			{
				// 登录成功且没有风控则登录LiveChat
				Message msg = Message.obtain();
				msg.what = LiveChatRequestOptType.LoginWithLoginItem.ordinal();
				msg.obj = item;
				mHandler.sendMessage(msg);
			}
		}
	}

	/**
	 * LoginManager注销回调（php注销回调）
	 */
	@Override
	public void OnLogout(OperateType operateType) {
		// TODO Auto-generated method stub
		if (OperateType.MANUAL == operateType) 
		{ 
			Logout();
		}
	}

	@Override
	public void OnSynConfig(boolean isSuccess, String errno, String errmsg,
			SynConfigItem item) {
		// TODO Auto-generated method stub
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
		}
		
		// 获取同步配置完成
		Message msg = Message.obtain();
		msg.what = LiveChatRequestOptType.GetSynConfigFinish.ordinal();
		mHandler.sendMessage(msg);
	}
	
	/**
	 * 获取同步配置完成
	 */
	private void GetSyncConfigFinish()
	{
		// 移除callback
		ConfigManagerJni.RemoveCallback(this);		
	}
}
