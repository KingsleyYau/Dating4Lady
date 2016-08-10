package com.qpidnetwork.request.item;

import java.io.Serializable;

/**
 * 微视频列表组item
 * @author Max
 *
 */
public class LCVideoListGroupItem implements Serializable {
	private static final long serialVersionUID = 4153192838276183556L;
	
	public LCVideoListGroupItem() {
		groupId = "";
		groupTitle = "";
	}

	/**
	 * @param albumId			组id
	 * @param groupTitle		组标题
	 */
	public LCVideoListGroupItem(
			String groupId,
			String groupTitle
			) {
		this.groupId = groupId;
		this.groupTitle = groupTitle;
	}
	
	public String toString() {
		String result = "";
		result += "{groupId=";
		result += groupId;
		result += ", ";
		result += "groupTitle=";
		result += groupTitle;
		result += "}";
		return result;
	}
	
	public String groupId;
	public String groupTitle;
}
