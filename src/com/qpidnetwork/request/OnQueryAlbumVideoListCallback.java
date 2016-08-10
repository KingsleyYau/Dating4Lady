package com.qpidnetwork.request;

import com.qpidnetwork.request.item.AlbumVideoItem;

/**
 * @author Yanni
 * 
 * @version 2016-6-17
 */
public interface OnQueryAlbumVideoListCallback {
	public void OnQueryAlbumVideoList(boolean isSuccess, String errno,String errmsg, AlbumVideoItem[] itemList);
}
