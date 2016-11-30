package com.qpidnetwork.ladydating.chat.history;

/**
 * ChatContactList 数据Item
 * @author Hunter Mun
 * @since 2016.9.13
 */
public class ChatContactItem {
	
	public ChatContactItem(){
		readFlag = false;
	}

	public String manId;
	public String manName;
	public String photoUrl;
	public int startTime;
	public boolean readFlag;
}
