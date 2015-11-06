package com.qpidnetwork.livechat;

import java.io.Serializable;

/**
 * 系统消息item
 * @author Samson Fan
 *
 */
public class LCSystemItem implements Serializable{

	private static final long serialVersionUID = 1606706562042191316L;
	/**
	 * 消息内容
	 */
	public String message;
	
	public LCSystemItem() {
		message = "";
	}
}
