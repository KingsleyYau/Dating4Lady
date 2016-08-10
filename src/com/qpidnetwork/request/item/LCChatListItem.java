package com.qpidnetwork.request.item;

import java.io.Serializable;

/**
 * 查询男士聊天历史item
 * @author Samson Fan
 *
 */
public class LCChatListItem implements Serializable{

	private static final long serialVersionUID = -9112496906585252112L;
	
	public LCChatListItem() {
		inviteId = "";
		startTime = "";
		duringTime = "";
		manId = "";
		manName = "";
		womanName = "";
		cnName = "";
		transId = "";
		transName = "";
	}

	/**
	 * @param inviteId		邀请ID
	 * @param startTime		开始时间
	 * @param duringTime	会话时长
	 * @param manId			男士ID
	 * @param manName		男士名称
	 * @param womanName		女士英语名
	 * @param cnName		女士名称
	 * @param transId		翻译ID
	 * @param transName		翻译名称
	 */
	public LCChatListItem(
			String inviteId,
			String startTime,
			String duringTime,
			String manId,
			String manName,
			String womanName,
			String cnName,
			String transId,
			String transName
			) 
	{
		this.inviteId = inviteId;
		this.startTime = startTime;
		this.duringTime = duringTime;
		this.manId = manId;
		this.manName = manName;
		this.womanName = womanName;
		this.cnName = cnName;
		this.transId = transId;
		this.transName = transName;
	}
	
	public String inviteId;
	public String startTime;
	public String duringTime;
	public String manId;
	public String manName;
	public String womanName;
	public String cnName;
	public String transId;
	public String transName;
}
