package com.qpidnetwork.request.item;

import java.io.Serializable;

public class EmotionConfigTagItem implements Serializable{
	
	private static final long serialVersionUID = 2475537272091158689L;
	
	public EmotionConfigTagItem() {
		
	}
	
	/**
	 * 
	 * @param toflag	终端使用标志（男士端/女士端）
	 * @param typeId	分类ID
	 * @param tagId		tag ID
	 * @param tagName	tag名称
	 */
	public EmotionConfigTagItem(
			int toflag,
			String typeId,
			String tagId,
			String tagName
			) {
		this.toflag = toflag;
		this.typeId = typeId;
		this.tagId = tagId;
		this.tagName = tagName;
	}
	
	public int toflag;
	public String typeId;
	public String tagId;
	public String tagName;
	
}
