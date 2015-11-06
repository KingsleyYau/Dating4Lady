package com.qpidnetwork.request.item;

import java.io.Serializable;

public class EmotionConfigEmotionItem implements Serializable{
	
	private static final long serialVersionUID = 3815798557810574143L;
	
	public EmotionConfigEmotionItem() {
		
	}

	/**
	 * 
	 * @param fileName	文件名
	 * @param price		所需点数
	 * @param isNew		是否有new标志
	 * @param isSale	是否有打折标志
	 * @param sortId	排序字段（降序）
	 * @param typeId	分类ID
	 * @param tagId		tag ID
	 * @param title		表情名称
	 */
	public EmotionConfigEmotionItem(
			String fileName,
			double price,
			boolean isNew,
			boolean isSale,
			int sortId,
			String typeId,
			String tagId,
			String title
			) {
		this.fileName = fileName;
		this.price = price;
		this.isNew = isNew;
		this.isSale = isSale;
		this.sortId = sortId;
		this.typeId = typeId;
		this.tagId = tagId;
		this.title = title;
	}
	
	public String fileName;
	public double price;
	public boolean isNew;
	public boolean isSale;
	public int sortId;
	public String typeId;
	public String tagId;
	public String title;
}
