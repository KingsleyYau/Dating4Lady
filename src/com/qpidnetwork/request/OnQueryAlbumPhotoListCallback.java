package com.qpidnetwork.request;

import com.qpidnetwork.request.item.AlbumPhotoItem;

/**
 * @author Yanni
 * 
 * @version 2016-6-17
 */
public interface OnQueryAlbumPhotoListCallback {
	public void OnQueryAlbumPhotoList(boolean isSuccess, String errno,String errmsg, AlbumPhotoItem[] itemList);
}
