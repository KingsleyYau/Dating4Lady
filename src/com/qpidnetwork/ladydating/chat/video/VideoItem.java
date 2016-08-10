package com.qpidnetwork.ladydating.chat.video;

import java.io.Serializable;

import com.qpidnetwork.request.item.LCVideoListVideoItem;

public class VideoItem implements Serializable{
	private static final long serialVersionUID = -2640043185618032341L;
	
	/**
	 * 根据界面需要定义当前Video状态
	 * @author Hunter
	 *
	 */
	public enum VideoSendStatus{
		NONE, //正常状态
		CHECKING,  //检测中
		FAIL_SENDED //已发送过给当前男士（检测错误）
	}
	
	public LCVideoListVideoItem videoItem;
	public VideoSendStatus videoStatus = VideoSendStatus.NONE;
	
	public VideoItem(LCVideoListVideoItem videoItem){
		this.videoItem = videoItem;
		this.videoStatus = VideoSendStatus.NONE;
	}
}
