package com.qpidnetwork.livechat;

import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;

/**
 * LiveChat管理回调接口类
 * @author Hunter
 *
 */
public interface LiveChatManagerAutoInviteListener {

	/**
	 * 开启/关闭小助手回调
	 * @param errType
	 * @param errmsg
	 * @param isOpen
	 */
	public void OnSwitchAutoInviteMsg(LiveChatErrType errType, String errmsg, boolean isOpen) ;
	
}
