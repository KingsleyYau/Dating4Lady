package com.qpidnetwork.request.item;

import java.io.Serializable;

public class EmotionConfigTypeItem implements Serializable{
	
	private static final long serialVersionUID = 3668446725512021333L;
	
	public EmotionConfigTypeItem() {

	}

	/**
	 * 
	 * @param toflag
	 *            终端使用标志（男士端/女士端）
	 * @param typeId
	 *            分类ID
	 * @param typeName
	 *            分类名称
	 */
	public EmotionConfigTypeItem(int toflag, String typeId, String typeName) {
		this.toflag = toflag;
		this.typeId = typeId;
		this.typeName = typeName;
	}

	public int toflag;
	public String typeId;
	public String typeName;
}
