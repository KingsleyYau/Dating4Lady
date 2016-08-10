package com.qpidnetwork.ladydating.chat.contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.ladydating.QpidApplication;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.album.VideoUploadManager;
import com.qpidnetwork.ladydating.auth.LoginActivity;
import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack;
import com.qpidnetwork.ladydating.authorization.KickOffNotification;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.chat.LCMessageHelper;
import com.qpidnetwork.ladydating.chat.LiveChatNotification;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCPhotoItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LCUserItem.ChatType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerEmotionListener;
import com.qpidnetwork.livechat.LiveChatManagerMessageListener;
import com.qpidnetwork.livechat.LiveChatManagerOtherListener;
import com.qpidnetwork.livechat.LiveChatManagerPhotoListener;
import com.qpidnetwork.livechat.LiveChatManagerVoiceListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.manager.PreferenceManager;
import com.qpidnetwork.request.OnLCCheckSendPhotoCallback.ResultType;
import com.qpidnetwork.request.item.EmotionConfigItem;
import com.qpidnetwork.request.item.LCPhotoListAlbumItem;
import com.qpidnetwork.request.item.LCPhotoListPhotoItem;
import com.qpidnetwork.request.item.LoginItem;

public class ContactManager implements LiveChatManagerOtherListener,
		LiveChatManagerMessageListener, LiveChatManagerEmotionListener,
		LiveChatManagerPhotoListener, LiveChatManagerVoiceListener ,
		IAuthorizationCallBack{
	
	private static final int ON_MESSAGE_RECEIVE = 1;
	private static final int ON_CONTACT_LIST_CHANGE_RECEIVE = 2;
	private static final int ON_KICK_OFF_RECEIVE = 3;
	private static final int ON_LOGIN_SESSION_ERROR = 4;

	private Context mContext;
	private Handler mHandler;
	private static ContactManager mContactManager;
	private LiveChatManager mLiveChatManager;
	
	//联系人列表数据维护
	private HashMap<String, Integer> mContactUnreadNum; //存储当前联系人未读消息数目
	private List<OnContactListChangeListener> mContactListChangeListeners;
	private List<OnUnreadCountUpdateListener> mOnUnreadCountUpdateListeners;//帮助统计未读条数
	
	public String mManId = "";// 当前正在聊天的男士Id，方便未读统计
	
	private int mLivechatLoginExceptionCount = 0;//PHPsession过期等导致Livechat登陆异常后处理
	

	private ContactManager(Context context) {
		this.mContext = context;
		mContactListChangeListeners = new ArrayList<OnContactListChangeListener>();
		mOnUnreadCountUpdateListeners = new ArrayList<OnUnreadCountUpdateListener>();
		mLiveChatManager = LiveChatManager.getInstance();
		mContactUnreadNum = new HashMap<String, Integer>();
		initListener();
		initHandler();
	}

	public static ContactManager newInstance(Context context) {
		if (mContactManager == null) {
			mContactManager = new ContactManager(context);
		}
		return mContactManager;
	}

	public static ContactManager getInstance() {
		return mContactManager;
	}
	
	public void RegisterUnreadCountChangeListener(OnUnreadCountUpdateListener listener){
		synchronized (mOnUnreadCountUpdateListeners) {
			mOnUnreadCountUpdateListeners.add(listener);
		}
	}
	
	public void UnregisterUnreadCountChangeListener(OnUnreadCountUpdateListener listener){
		synchronized (mOnUnreadCountUpdateListeners) {
			if (listener != null) {
				if (mOnUnreadCountUpdateListeners.contains(listener)) {
					mOnUnreadCountUpdateListeners.remove(listener);
				}
			}
		}
	}
	
	public void RegisterContactListChangeListener(OnContactListChangeListener listener){
		synchronized (mContactListChangeListeners) {
			mContactListChangeListeners.add(listener);
		}
	}
	
	public void UnregisterContactListChangeListener(OnContactListChangeListener listener){
		synchronized (mContactListChangeListeners) {
			if (listener != null) {
				if (mContactListChangeListeners.contains(listener)) {
					mContactListChangeListeners.remove(listener);
				}
			}
		}
	}
	
	/**
	 * 1.收到、发送消息；
	 * 2.会话时间改变（OnRecvTalkEvent）；
	 * 3.男士状态改变更新（OnUpdateStatus）
	 */
	private void onContactListChangeUpdate(){
		synchronized (mContactListChangeListeners) {
			if(mContactListChangeListeners != null){
				for(OnContactListChangeListener listener : mContactListChangeListeners){
					listener.onContactListChange();
				}
			}
		}
	}

	private void initListener() {
		if(mLiveChatManager != null){
			mLiveChatManager.RegisterOtherListener(this);
			mLiveChatManager.RegisterMessageListener(this);
			mLiveChatManager.RegisterEmotionListener(this);
			mLiveChatManager.RegisterVoiceListener(this);
			mLiveChatManager.RegisterPhotoListener(this);
		}
	}
	
	
	
	/**
	 * 子线程主线程转换
	 */
	private void initHandler(){
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case ON_MESSAGE_RECEIVE:{
					LCMessageItem item = (LCMessageItem)msg.obj;
					String userId = "";
					if(item != null){
						userId = item.getUserItem().userId;
					}
					
					if(!userId.equals(mManId)){
						//非在聊男士
						updateUnreadNum(item.getUserItem().userId);
					}
					
					if(!userId.equals(mManId) || SystemUtil.isBackground(mContext)){
						onNewMsgNotify(item);
					}
					onContactListChangeUpdate();
				}break;
				case ON_CONTACT_LIST_CHANGE_RECEIVE:{
					onContactListChangeUpdate();
				}break;
				case ON_KICK_OFF_RECEIVE:{
					KickOfflineType type = KickOfflineType.values()[msg.arg1];
					onKickOff(type);
				}break;
				
				case ON_LOGIN_SESSION_ERROR:{
					LoginManager.getInstance().LoginBySessionOuttime();
				}break;
				
				default:
					break;
				}
			}
		};
	}
	
	/**
	 * 消息通知
	 * @param item
	 */
	private void onNewMsgNotify(LCMessageItem item){
		PreferenceManager preference = new PreferenceManager(mContext);
		boolean bSound = preference.getNotificationSwitchSetting();
		LiveChatNotification nt = LiveChatNotification.newInstance(mContext);
		String tips = "";
		if (item.msgType != MessageType.Text) {
			if(item.getUserItem() != null){
				tips = item.getUserItem().userName + ": " + LCMessageHelper.generateMsgHint(mContext, item);
			}
		} else {
			String msgTemp = (item.getTextItem().message != null) ? item
					.getTextItem().message : "";
			msgTemp = msgTemp.replaceAll("\\[\\w*:[0-9]*\\]",
					"[smiley]");
			if(item.getUserItem() != null){
				tips = item.getUserItem().userName + ": " + msgTemp;
			}
		}
		if(!TextUtils.isEmpty(tips)){
			nt.ShowNotification(R.drawable.logo_40dp, tips, bSound);
		}
	}
	
	/**
	 * 被踢逻辑处理
	 * @param type
	 */
	private void onKickOff(KickOfflineType type){
		/* Livechat 被踢处理,发送广播通知界面 */
		QpidApplication.isKickOff = true;
		QpidApplication.kickOffType = type;
		QpidApplication.lastestKickoffTime = (int)(System.currentTimeMillis()/1000);
		
		if(SystemUtil.isBackground(mContext)){
			//后台被踢时，notify user
			PreferenceManager preference = new PreferenceManager(mContext);
			boolean bSound = preference.getNotificationSwitchSetting();
			
			String tips = "";
			if(QpidApplication.kickOffType == KickOfflineType.Maintain){
				tips = mContext.getString(R.string.livechat_kickoff_by_sever_update);
			}
			else{
				tips = mContext.getString(R.string.livechat_kickoff_by_other);
			}
			KickOffNotification.newInstance(mContext).ShowNotification(R.drawable.logo_40dp,tips, bSound);
		}
		
		Intent intent = new Intent();
		intent.setAction(BaseFragmentActivity.LIVECHAT_KICKOFF_ACTION);
		intent.putExtra(LoginActivity.LIVE_CHAT_KICK_OFF, type.ordinal());
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * 更新指定男士未读数目条数
	 * @param manId
	 */
	private void updateUnreadNum(String manId){
		int unreadCount = 0;
		synchronized (mContactUnreadNum) {
			if(mContactUnreadNum.containsKey(manId)){
				unreadCount = mContactUnreadNum.get(manId);
			}
			unreadCount++;
			mContactUnreadNum.put(manId, unreadCount);
			synchronized (mOnUnreadCountUpdateListeners) {
				if((mOnUnreadCountUpdateListeners != null) && mOnUnreadCountUpdateListeners.size() > 0){
					int countNum = 0;
					Iterator<Entry<String, Integer>> itr = mContactUnreadNum.entrySet().iterator();
					while(itr.hasNext()){
						countNum += itr.next().getValue();
					}
					for(OnUnreadCountUpdateListener listener : mOnUnreadCountUpdateListeners){
						listener.onUnreadUpdate(countNum);
					}
					
				}
			}
		}
	}
	
	/**
	 * 清除指定联系人未读数目
	 * @param manid
	 */
	public void clearContactUnreadCount(String manid){
		synchronized (mContactUnreadNum) {
			if(mContactUnreadNum.containsKey(manid)){
				mContactUnreadNum.remove(manid);
			}
			onContactListChangeUpdate();
			synchronized (mOnUnreadCountUpdateListeners) {
				if((mOnUnreadCountUpdateListeners != null) && mOnUnreadCountUpdateListeners.size() > 0){
					int countNum = 0;
					Iterator<Entry<String, Integer>> itr = mContactUnreadNum.entrySet().iterator();
					while(itr.hasNext()){
						countNum += itr.next().getValue();
					}
					for(OnUnreadCountUpdateListener listener : mOnUnreadCountUpdateListeners){
						listener.onUnreadUpdate(countNum);
					}
					
				}
			}
		}
	}
	
	/**
	 * 获取当前联系人未读消息条数
	 * 
	 * @return
	 */
	public int getAllUnreadCount() {
		int unreadCount = 0;
		synchronized (mContactUnreadNum) {
			if(mContactUnreadNum != null){
				Iterator<Entry<String, Integer>> itr = mContactUnreadNum.entrySet().iterator();
				while(itr.hasNext()){
					unreadCount += itr.next().getValue();
				}
			}
		}
		return unreadCount;
	}
	
	/**
	 * 获取指定联系人是否已读状态
	 * @param man_id
	 * @return
	 */
	public boolean getReadFlag(String man_id){
		boolean readFlag = true;
		synchronized (mContactUnreadNum){
			if(mContactUnreadNum.containsKey(man_id)){
				readFlag = false;
			}
		}
		return readFlag;
	}
	
	

	private void deInitListener() {
		if(mLiveChatManager != null){
			mLiveChatManager.UnregisterOtherListener(this);
			mLiveChatManager.UnregisterMessageListener(this);
			mLiveChatManager.UnregisterEmotionListener(this);
			mLiveChatManager.UnregisterVoiceListener(this);
			mLiveChatManager.UnregisterPhotoListener(this);
		}
	}
	
	public void onDestroy(){
		deInitListener();
		synchronized (mContactUnreadNum) {
			mContactUnreadNum.clear();
		}
		synchronized (mContactListChangeListeners) {
			mContactListChangeListeners.clear();
		}
	}
	
	/**
	 * 女士端发送消息统一处理
	 * @param item
	 */
	public void onSendMessage(LCMessageItem item){
		if(item.getUserItem().chatType == ChatType.InChatCharge
				|| item.getUserItem().chatType == ChatType.InChatUseTryTicket || (mLiveChatManager.IsInContactList(item.toId))){
			/*邀请不影响联系人列表*/
			onContactListChangeUpdate();
		}
	}
	
	/**
	 * 女士端收到消息统一处理
	 * @param item
	 */
	private void onReceiveMessage(LCMessageItem item){
		Message msg = Message.obtain();
		msg.what = ON_MESSAGE_RECEIVE;
		msg.obj = item;
		mHandler.sendMessage(msg);
	}

	/*********************** LivechatManager listener *****************************************/
	@Override
	public void OnSendVoice(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetVoice(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvVoice(LCMessageItem item) {
		/*收到语音聊天*/
		onReceiveMessage(item);
	}

	@Override
	public void OnSendPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvPhoto(LCMessageItem item) {
		/*图片聊天消息*/
		onReceiveMessage(item);
	}

	@Override
	public void OnGetPhotoList(boolean isSuccess, String errno, String errmsg,
			LCPhotoListAlbumItem[] albums, LCPhotoListPhotoItem[] photos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetEmotionConfig(boolean success, String errno,
			String errmsg, EmotionConfigItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSendEmotion(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvEmotion(LCMessageItem item) {
		/*图片高级表情消息*/
		onReceiveMessage(item);
	}

	@Override
	public void OnGetEmotionImage(boolean success, LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetEmotionPlayImage(boolean success, LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSendMessage(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvMessage(LCMessageItem item) {
		/*普通文字聊天消息*/
		onReceiveMessage(item);		
	}

	@Override
	public void OnRecvWarning(LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvEditMsg(String fromId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvSystemMsg(LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSendMessageListFail(LiveChatErrType errType,
			ArrayList<LCMessageItem> msgList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnLogin(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		if(errType == LiveChatErrType.Success){
			mLivechatLoginExceptionCount = 0;
		}else{
			if((errType == LiveChatErrType.NoSession ||
					errType == LiveChatErrType.InvalidPassword) && !isAutoLogin){			
				if(mLivechatLoginExceptionCount < 1){
					Message msg = Message.obtain();
					msg.what = ON_LOGIN_SESSION_ERROR;
					msg.arg1 = errType.ordinal();
					mHandler.sendMessage(msg);
				}else{
					//连续两次异常，当服务器主动踢
					Message msg = Message.obtain();
					msg.what = ON_KICK_OFF_RECEIVE;
					msg.arg1 = KickOfflineType.Maintain.ordinal();
					mHandler.sendMessage(msg);
				}
				mLivechatLoginExceptionCount++;
			}
		}
	}

	@Override
	public void OnLogout(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetHistoryMessage(boolean success, String errno,
			String errmsg, LCUserItem userItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSetStatus(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void OnUpdateStatus(LCUserItem userItem) {

	}

	@Override
	public void OnChangeOnlineStatus(LCUserItem userItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvKickOffline(KickOfflineType kickType) {
		/* Livechat 被踢处理 */
		Message msg = Message.obtain();
		msg.what = ON_KICK_OFF_RECEIVE;
		msg.arg1 = kickType.ordinal();
		mHandler.sendMessage(msg);		
	}

	@Override
	public void OnReplyIdentifyCode(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvIdentifyCode(byte[] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvTalkEvent(LCUserItem item) {

	}

	@Override
	public void OnContactListChange() {
		/*联系人状态修改*/
		Message msg = Message.obtain();
		msg.what = ON_CONTACT_LIST_CHANGE_RECEIVE;
		mHandler.sendMessage(msg);
	}

	@Override
	public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSearchOnlineMan(LiveChatErrType errType, String errmsg,
			String[] userIds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetUsersInfo(LiveChatErrType errType, String errmsg, int seq,
			LiveChatTalkUserListItem[] list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnCheckSendPhoto(LiveChatErrType errType, ResultType result,
			String errno, String errmsg, LCUserItem userItem,
			LCPhotoItem photoItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetSelfPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCPhotoItem photoItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnTransStatusChange() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvShowPhoto(LCUserItem userItem, String photoId,
			String photoDesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnLogin(OperateType operateType, boolean isSuccess,
			String errno, String errmsg, LoginItem item) {
		if(operateType == OperateType.MANUAL){
//			initListener();
			VideoUploadManager.getInstance().reInit();
		}
	}

	@Override
	public void OnLogout(OperateType operateType) {
		if(operateType == OperateType.MANUAL){
//			onDestroy();
			//清除所有video下载
			VideoUploadManager.getInstance().onDestroy();
		}
	}
}
