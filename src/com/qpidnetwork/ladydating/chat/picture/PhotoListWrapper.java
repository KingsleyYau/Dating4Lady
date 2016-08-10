package com.qpidnetwork.ladydating.chat.picture;

import java.io.Serializable;

import com.qpidnetwork.request.item.LCPhotoListPhotoItem;

public class PhotoListWrapper implements Serializable{

	private static final long serialVersionUID = 3900193623080589559L;
	
	LCPhotoListPhotoItem[] photoList = null;
	
	public PhotoListWrapper(LCPhotoListPhotoItem[] photoList){
		this.photoList = photoList;
	}
}
