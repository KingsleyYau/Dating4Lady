package com.qpidnetwork.ladydating.chat.picture;

import java.io.Serializable;

import com.qpidnetwork.request.item.LCPhotoListPhotoItem;

public class AlbumPhotoItem implements Serializable{

	private static final long serialVersionUID = 5317107581578738145L;
	
	/**
	 * 根据界面需要定义当前私密照状态
	 * @author Hunter
	 *
	 */
	public enum PhotoSendStatus{
		NONE, //正常状态
		CHECKING,  //检测中
		FAIL_SENDED //已发送过给当前男士（检测错误）
	}
	
	public LCPhotoListPhotoItem photoItem;
	public PhotoSendStatus photoStatus = PhotoSendStatus.NONE;
	
	public AlbumPhotoItem(){
		
	}
	
	public AlbumPhotoItem(LCPhotoListPhotoItem photoItem){
		this.photoItem = photoItem;
		this.photoStatus = PhotoSendStatus.NONE;
	}

}
