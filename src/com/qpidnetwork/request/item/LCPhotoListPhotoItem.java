package com.qpidnetwork.request.item;

import java.io.Serializable;

/**
 * 获取私密照列表照片item
 * @author Samson Fan
 *
 */
public class LCPhotoListPhotoItem implements Serializable{

	private static final long serialVersionUID = -1359101635606989766L;
	public LCPhotoListPhotoItem() {
		photoId = "";
		albumId = "";
		title = "";
	}

	/**
	 * @param photoId	照片ID
	 * @param albumId	相册ID
	 * @param title		照片标题
	 */
	public LCPhotoListPhotoItem(
			String photoId,
			String albumId,
			String title
			) 
	{
		this.photoId = photoId;
		this.albumId = albumId;
		this.title = title;
	}
	
	public String photoId;
	public String albumId;
	public String title;
}
