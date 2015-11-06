package com.qpidnetwork.livechat;

import java.io.Serializable;

/**
 * 文本消息item
 * @author Samson Fan
 *
 */
public class LCTextItem implements Serializable{

	private static final long serialVersionUID = 7392858016957384450L;
	/**
	 * 消息内容
	 */
	public String message;
	/**
	 * 内容是否合法
	 */
	public boolean illegal;
	
	public LCTextItem() {
		message = "";
		illegal = false;
	}
	
	public void init(String message) {
		this.illegal = LCMessageFilter.isIllegalMessage(message);
		if (this.illegal) {
			this.message = LCMessageFilter.filterMessage(message);
		} 
		else {
			this.message = message;
		}
	}
}
