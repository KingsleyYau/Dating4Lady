package com.qpidnetwork.livechat;

import java.io.Serializable;


/**
 * 视频对象
 * @author Samson Fan
 */
public class LCVideoMsgItem implements Serializable{

	private static final long serialVersionUID = -2424845692833039512L;
	/**
	 * 视频item
	 */
	public LCVideoItem videoItem = null;
	/**
	 * 发送ID
	 */
	public String sendId = "";
	/**
	 * 是否已付费
	 */
	public boolean charge = false;
	
	public LCVideoMsgItem() {
	}
	
	public void init(
			LCVideoItem videoItem
			, String sendId
			, boolean charge) 
	{
		this.videoItem = videoItem;
		this.sendId = sendId;
		this.charge = charge;
	}
}
