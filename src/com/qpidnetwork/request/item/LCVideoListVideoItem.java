package com.qpidnetwork.request.item;

import java.io.Serializable;

/**
 * 微视频item
 * @author Max
 *
 */
public class LCVideoListVideoItem implements Serializable {
	private static final long serialVersionUID = 8034642233239840502L;
	public LCVideoListVideoItem() {
		groupId = "";
		videoId = "";
		title = "";
	}

	/**
	 * @param groupId	组id
	 * @param videoId	视频id
	 * @param title		视频标题
	 */
	public LCVideoListVideoItem(
			String groupId,
			String videoId,
			String title
			) {
		this.groupId = groupId;
		this.videoId = videoId;
		this.title = title;
	}
	
	public String toString() {
		String result = "";
		result += "{groupId=";
		result += groupId;
		result += ", ";
		result += "videoId=";
		result += videoId;
		result += ", ";
		result += "title=";
		result += title;
		result += "}";
		return result;
	}
	
	public String groupId;
	public String videoId;
	public String title;
}
