package com.qpidnetwork.request.item;

import java.io.Serializable;

public class LiveChatInviteTemplateListItem implements Serializable{
	
	private static final long serialVersionUID = 844584907588475725L;
	public enum TemplateStatus{
		Pending,
		Audited,
		Rejected
	}
	
	public LiveChatInviteTemplateListItem() {
		
	}
	
	/**
	 * 
	 * @param tempId  模板id
	 * @param tempContent 模板内容
	 * @param tempStatus 审核状态（0：待审核，1：已通过，2：已否决）
	 * @param autoflag 是否小助手模板（0：不是 1： 是）
	 */
	public LiveChatInviteTemplateListItem(String tempId,
					String tempContent,
					int tempStatus,
					boolean autoflag){
		this.tempId = tempId;
		this.tempContent = tempContent;
		this.tempStatus = TemplateStatus.values()[tempStatus];
		this.autoFlag = autoflag;
	}
	
	public String tempId = "";
	public String tempContent = "";
	public TemplateStatus tempStatus = TemplateStatus.Pending;
	public boolean autoFlag = false;
}
