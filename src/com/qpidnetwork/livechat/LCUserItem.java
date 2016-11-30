package com.qpidnetwork.livechat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCMessageItem.SendType;
import com.qpidnetwork.livechat.LCMessageItem.StatusType;
import com.qpidnetwork.livechat.jni.LiveChatClient.ClientType;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserSexType;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEventType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkMsgType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;

/**
 * LiveChat的 User 对象
 * @author Samson Fan
 */
public class LCUserItem implements Serializable{

	private static final long serialVersionUID = -2059118989300697623L;
	/**
	 * 用户ID
	 */
	public String userId;
	/**
	 * 用户名称
	 */
	public String userName;
	/**
	 * 用户头像URL
	 */
	public String imgUrl;
	/**
	 * 用户性别
	 */
	public UserSexType sexType;
	/**
	 * 用户年龄
	 */
	public int age;
	/**
	 * 国家
	 */
	public String country;
	/**
	 * 用户使用的客户端类型
	 */
	public ClientType clientType;
	/**
	 * 客户端版本
	 */
	public String clientVer;
	/**
	 * 用户在线状态
	 */
	public UserStatusType statusType;
	/**
	 * 与用户的当前聊天状态
	 */
	public ChatType chatType;
	/**
	 * 邀请ID（仅InChat状态）
	 */
	public String inviteId;
	/**
	 * 排序分值（用于邀请排序）
	 */
	public int order;
	/**
	 * 待发送消息
	 */
	public ArrayList<LCMessageItem> sendMsgList;
	/**
	 * 聊天记录列表
	 */
	protected ArrayList<LCMessageItem> msgList;
	/**
	 * 发送消息规则处理器
	 */
	private LCSendMsgRuleHandler mSendMsgRule;
	
	/**
	 * 聊天状态
	 */
	public enum ChatType {
		InChatCharge, 		// 收费
		InChatUseTryTicket, // 试聊券
		WomanInvite, 		// 女士邀请
		ManInvite,			// 男士邀请 
		Other, 				// 其它
	}

	public LCUserItem() {
		userId = "";
		userName = "";
		imgUrl = "";
		sexType = UserSexType.USER_SEX_MALE;
		age = 0;
		country = "";
		clientType = ClientType.CLIENT_ANDROID;
		clientVer = "";
		statusType = UserStatusType.USTATUS_OFFLINE_OR_HIDDEN;
		chatType = ChatType.Other;
		inviteId = "";
		order = 0;
		msgList = new ArrayList<LCMessageItem>();
		sendMsgList = new ArrayList<LCMessageItem>();
		mSendMsgRule = new LCSendMsgRuleHandler(this);
	}
	
	/**
	 * 根据获取邀请/在聊用户列表user item更新用户信息
	 * @param listItem
	 * @return 有否更新
	 */
	public boolean UpdateWithLiveChatTalkUserListItem(LiveChatTalkUserListItem item)
	{
		boolean result = false;
		
		if (this.userName.compareTo(item.userName) != 0) {
			this.userName = item.userName;
			result = true;
		}
		
		if (this.imgUrl.compareTo(item.imgUrl) != 0) {
			this.imgUrl = item.imgUrl;
			result = true;
		}
		
		if (this.sexType != item.sexType) {
			this.sexType = item.sexType;
			result = true;
		}
		
		if (this.age != item.age) {
			this.age = item.age;
			result = true;
		}
		
		if (this.country.compareTo(item.country) != 0) {
			this.country = item.country;
			result = true;
		}
		
		if (this.statusType != item.statusType) { 
			this.statusType = item.statusType;
			result = true;
		}
		
		if (this.clientType != item.clientType) {
			this.clientType = item.clientType;
			result = true;
		}
		
		if (this.clientVer.compareTo(item.clientVersion) != 0) {
			this.clientVer = item.clientVersion;
			result = true;
		}
		
		if (this.order != item.orderValue) {
			this.order = item.orderValue;
			result = true;
		}
		return result;
	}

	/**
	 * 获取聊天记录列表（已按时间排序）
	 */
	public final ArrayList<LCMessageItem> getMsgList() {
		return msgList;
	}

	/**
	 * 排序插入聊天记录
	 * @param item
	 * @return
	 */
	public boolean insertSortMsgList(LCMessageItem item) {
		boolean result = false;
		synchronized (msgList) {
			result = msgList.add(item);
			if (result) {
				Collections.sort(msgList, LCMessageItem.getComparator());
				
				// 若是女士发送，且chat状态为其它，则把 chat状态置为WomanInvite
				if (item.sendType == SendType.Send
					&& this.chatType == ChatType.Other)
				{
					setChatType(ChatType.WomanInvite);
				}
			}
		}
		
		if (result) {
			item.setUserItem(this);
			mSendMsgRule.InsertMessage(item);
		}
		return result;
	}
	
	/**
	 * 删除聊天记录
	 * @param item	消息item
	 * @return
	 */
	public boolean removeSortMsgList(LCMessageItem item) {
		boolean result = false;
		if (null != item) {
			synchronized (msgList) {
				result = msgList.remove(item);
				if (result) {
					item.setUserItem(null);
					Collections.sort(msgList, LCMessageItem.getComparator());
				}
			}
		}
		return result;
	}
	
	/**
	 * 清除所有聊天记录
	 */
	public void clearMsgList() 
	{
		// 清除所有聊天记录
		synchronized (msgList) {
			for (Iterator<LCMessageItem> iter = msgList.iterator(); iter.hasNext(); ) {
				LCMessageItem item = iter.next();
				item.clear();
			}
			msgList.clear();
		}
		
		// 清除所有待发消息
		clearSendingMsgList();
		
		// 清除所有发送消息规则
		mSendMsgRule.Clear();
	}
	
	/**
	 * 添加待发送消息
	 * @param item	消息item
	 */
	public void addSendingMsg(LCMessageItem item)
	{
		synchronized (sendMsgList)
		{
			sendMsgList.add(item);
		}
	}
	
	/**
	 * 清空待发送消息列表
	 */
	private void clearSendingMsgList()
	{
		synchronized (sendMsgList)
		{
			sendMsgList.clear();
		}
	}
	
	/**
	 * 清除所有已完成的聊天记录
	 */
	public void clearFinishedMsgList() {
		ArrayList<LCMessageItem> tempList = new ArrayList<LCMessageItem>();
		synchronized (msgList) {
			for (Iterator<LCMessageItem> iter = msgList.iterator(); iter.hasNext(); ) {
				LCMessageItem item = iter.next();
				if (item.statusType == StatusType.Finish) {
					tempList.add(item);
					item.clear();
				}
			}
			msgList.removeAll(tempList);
		}
	}
	
	/**
	 * 根据消息ID获取LCMessageItem
	 * @param msgId	消息ID
	 * @return
	 */
	public LCMessageItem getMsgItemWithId(int msgId) {
		LCMessageItem item = null;
		synchronized (msgList) {
			for (Iterator<LCMessageItem> iter = msgList.iterator(); iter.hasNext(); ) {
				LCMessageItem msgItem = iter.next();
				if (msgItem.msgId == msgId) {
					item = msgItem;
					break;
				}
			}
		}
		return item;
	}
	
	/**
	 * 设置聊天状态(根据会话状态)
	 * @param eventType		聊天事件
	 * @return 聊天状态是否改变	
	 */
	public boolean setChatTypeWithEventType(TalkEventType eventType) 
	{
		boolean result = false;
		
		ChatType chatType = this.chatType; 
		switch (eventType) {
		case EndTalk:
			chatType = ChatType.Other;
			break;
		case StartCharge:
			if (!IsInChat())
			{
				chatType = ChatType.InChatCharge;
			}
			break;
		case StopCharge:
			chatType = ChatType.Other;
			break;
		case NoMoney:
		case VideoNoMoney:
			chatType = ChatType.Other;
			break;
		case TargetNotFound:
			chatType = ChatType.Other;
			break;
		default:
			break;
		}
		
		result = setChatType(chatType);
		
		return result;
	}
	
	/**
	 * 设置聊天状态(根据聊天消息类型)
	 * @param inviteId	邀请ID 
	 * @param charge	是否付费
	 * @param msgType	聊天消息类型
	 * @return 聊天状态是否改变
	 */
	public boolean setChatTypeWithTalkMsgType(String inviteId, boolean charge, TalkMsgType msgType) 
	{
		boolean result = false;
		if (this.inviteId.equals(inviteId))
		{
			// 邀请ID没变，之前不是inchat状态才改变当前会话状态
			UpdateInChatStatus();
		}
		else 
		{
			// 邀请ID改变，重置会话状态
			this.inviteId = inviteId;
			ChatType chatType = getChatTypeWithTalkMsgType(charge, msgType);
			result = setChatType(chatType);
		}
		
		return result;
	}
	
	/**
	 * 设置聊天状态
	 * @param chatType	聊天状态
	 * @return
	 */
	public boolean setChatType(ChatType chatType)
	{
		boolean result = false;
		if (chatType != this.chatType) {
			this.chatType = chatType;
			result = true;
		}
		
		
		if (result) {
			// 通知发送消息规则聊天状态改变
			mSendMsgRule.ChangeChatType(chatType);
		}
		return result;
	}
	
	/**
	 * 根据 TalkMsgType 获取聊天状态
	 * @param charge	是否已付费
	 * @param msgType	消息收费类型
	 * @return
	 */
	public static ChatType getChatTypeWithTalkMsgType(boolean charge, TalkMsgType msgType) 
	{
		ChatType chatType = ChatType.Other; 
		switch(msgType) {
		case TMT_FREE:
			if (!charge) {
				// TMT_FREE 及 charge=false，则为邀请
				chatType = ChatType.ManInvite;
			}
			else {
				// charge=true，则为InChatCharge
				chatType = ChatType.InChatCharge;
			}
			break;
		case TMT_CHARGE:
			chatType = ChatType.InChatCharge;
			break;
		case TMT_CHARGE_FREE:
			chatType = ChatType.InChatUseTryTicket;
			break;
		default:
			chatType = ChatType.Other;
			break;
		}
		return chatType;
	}
	
	/**
	 * 获取对方发出的最后一条聊天消息
	 * @return
	 */
	public LCMessageItem getTheOtherLastMessage()
	{
		LCMessageItem item = null;
		synchronized(msgList) {
			if (msgList.size() > 0) 
			{
				for (int i = msgList.size() - 1; i >= 0 ; i--) 
				{
					LCMessageItem tempItem = msgList.get(i);
					if (tempItem.sendType == SendType.Recv) {
						item = tempItem;
						break;
					}
				}
			}
		}
		return item;
	}
	
	/**
	 * 结束聊天处理
	 */
	public void endTalk() {
		chatType = ChatType.Other;
		clearMsgList();
	}
	
	/**
	 * 判断是否可发送消息返回定义
	 */
	public enum CanSendErrType {
		OK,					// 可以发送
		UnknowErr,			// 未知错误
		SendMsgFrequency,	// 发送消息过快
		NoInChat,			// 非在聊状态
	}
	/**
	 * 判断是否可发送消息 
	 * @param msgType	消息类型
	 * @return
	 */
	public CanSendErrType CanSendMessage(LCMessageItem.MessageType msgType)
	{
		CanSendErrType result = CanSendErrType.OK;
		if (!IsInChat()	// 由于LiveChat服务器不能及时提供在聊状态，把判断是否在聊改为只要有互相发过消息
			&& (msgType == MessageType.Photo
					|| msgType == MessageType.Voice
					|| msgType == MessageType.Video
					|| msgType == MessageType.MagicIcon))
		{
			result = CanSendErrType.NoInChat; 
		}
		else if (!mSendMsgRule.CanSendMessage(msgType)) {
			result = CanSendErrType.SendMsgFrequency;
		}
		return result;
	}
	
	/**
	 * 是否与该男士在指定会话互相发过消息
	 * @return
	 */
	private boolean HaveTalk(String inviteId)
	{
		boolean result = false;
		synchronized(msgList) 
		{
			boolean send = false;
			boolean recv = false;
			for (int i = msgList.size() - 1; i >= 0 ; i--) 
			{
				LCMessageItem tempItem = msgList.get(i);
				if (inviteId.equals(tempItem.inviteId))
				{
					if (tempItem.sendType == SendType.Recv) {
						send = true;
					}
					else if (tempItem.sendType == SendType.Send) {
						recv = true;
					}
				}
				
				if (send && recv) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * 获取最后一条聊天消息
	 */
	public LCMessageItem GetLastTalkMsg()
	{
		LCMessageItem msgItem = null;
		synchronized (msgList)
		{
			for (int i = msgList.size()-1; i >= 0; i--)
			{
				LCMessageItem item = msgList.get(i);
				if (null != item
					&& (item.sendType == SendType.Recv || item.sendType == SendType.Send))
				{
					msgItem = item;
					break;
				}
			}
		}
		return msgItem;
	}
	
	/**
	 * 是否inchat状态
	 * @return
	 */
	public boolean IsInChat()
	{
		return chatType == ChatType.InChatCharge
				|| chatType == ChatType.InChatUseTryTicket;
	}
	
	/**
	 * 更新inchat状态
	 * @return
	 */
	public boolean UpdateInChatStatus()
	{
		boolean result = false;
		if (!IsInChat())
		{
			// 本会话有互相发送消息
			if (HaveTalk(inviteId)) 
			{
				result = setChatType(ChatType.InChatCharge);
			}
		}
		return result;
	}
}
