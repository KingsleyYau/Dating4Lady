package com.qpidnetwork.request;

public interface OnQueryFavourListCallback {
	public void OnQueryFavourList(boolean isSuccess, String errno, String errmsg, String[] itemList);
}
