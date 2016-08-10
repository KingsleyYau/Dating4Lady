package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LCPhotoListAlbumItem;
import com.qpidnetwork.request.item.LCPhotoListPhotoItem;

public interface OnLCGetPhotoListCallback {
	public void OnLCGetPhotoList(boolean isSuccess, String errno, String errmsg, LCPhotoListAlbumItem[] albums, LCPhotoListPhotoItem[] photos);
}
