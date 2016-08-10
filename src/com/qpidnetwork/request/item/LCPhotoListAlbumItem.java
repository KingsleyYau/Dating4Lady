package com.qpidnetwork.request.item;

import java.io.Serializable;

/**
 * 获取私密照列表相册item
 * @author Samson Fan
 *
 */
public class LCPhotoListAlbumItem implements Serializable{
	private static final long serialVersionUID = 2642491560934082483L;
	
	public LCPhotoListAlbumItem() {
		albumId = "";
		title = "";
	}

	/**
	 * @param albumId	相册ID
	 * @param title		相册标题
	 */
	public LCPhotoListAlbumItem(
			String albumId,
			String title
			) 
	{
		this.albumId = albumId;
		this.title = title;
	}
	
	public String albumId;
	public String title;
}
