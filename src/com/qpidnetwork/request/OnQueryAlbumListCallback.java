package com.qpidnetwork.request;

import com.qpidnetwork.request.item.AlbumListItem;

/**
 * @author Yanni
 * 
 * @version 2016-6-17
 */
public interface OnQueryAlbumListCallback {
	public void OnQueryAlbumList(boolean isSuccess, String errno,String errmsg, AlbumListItem[] itemList);
}
