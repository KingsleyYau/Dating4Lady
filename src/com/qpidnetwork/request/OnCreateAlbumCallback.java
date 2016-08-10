package com.qpidnetwork.request;

/**
 * @author Yanni
 * 
 * @version 2016-6-17
 */
public interface OnCreateAlbumCallback {
	public void onCrateAlbum(boolean isSuccess, String errno, String errmsg, String albumId);
}
