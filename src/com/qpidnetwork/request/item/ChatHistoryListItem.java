package com.qpidnetwork.request.item;

import java.io.Serializable;

public class ChatHistoryListItem implements Serializable{

	private static final long serialVersionUID = 5614764875488103900L;
	
	public ChatHistoryListItem(){
		
	}
	
	/**
	 * 
	 * @param inviteId  	  邀请id
	 * @param startTime      当此会话开始时间
	 * @param duringTime     当此会话时长
	 * @param manId          男士id
	 * @param womanName      女士英文名称
	 * @param manName        男士first name
	 * @param cnName         女士姓名
	 * @param translatorId   翻译id
	 * @param translatorName 翻译名字
	 */
	public ChatHistoryListItem(String inviteId,
				String startTime,
				String duringTime,
				String manId,
				String womanName,
				String manName,
				String cnName,
				String translatorId,
				String translatorName){
		this.inviteId = inviteId;
		this.startTime = startTime;
		this.duringTime = duringTime;
		this.manId = manId;
		this.womanName = womanName;
		this.manName = manName;
		this.cnName = cnName;
		this.translatorId = translatorId;
		this.translatorName = translatorName;
	}
	
	public String inviteId;
	public String startTime;
	public String duringTime;
	public String manId;
	public String womanName;
	public String manName;
	public String cnName;
	public String translatorId;
	public String translatorName;
	
}
