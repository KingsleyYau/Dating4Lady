package com.qpidnetwork.request.item;

import java.io.Serializable;

public class EmotionConfigItem implements Serializable {

	private static final long serialVersionUID = 2705779112170737355L;
	
	public EmotionConfigItem() {
		
	}

	/**
	 * 
	 * @param version			高级表情版本号
	 * @param path				路径
	 * @param typeList			分类列表
	 * @param tagList			tag列表
	 * @param manEmotionList	男士表情列表
	 * @param ladyEmotionList	女士表情列表
	 */
	public EmotionConfigItem(
			int version,
			String path,
			EmotionConfigTypeItem[] typeList,
			EmotionConfigTagItem[] tagList,
			EmotionConfigEmotionItem[] manEmotionList,
			EmotionConfigEmotionItem[] ladyEmotionList
			) {
		this.version = version;
		this.path = path;
		this.typeList = typeList;
		this.tagList = tagList;
		this.manEmotionList = manEmotionList;
		this.ladyEmotionList = ladyEmotionList;
	}
	
	public int version;
	public String path;
	public EmotionConfigTypeItem[] typeList;
	public EmotionConfigTagItem[] tagList;
	public EmotionConfigEmotionItem[] manEmotionList;
	public EmotionConfigEmotionItem[] ladyEmotionList;
}
