package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LCVideoListGroupItem;
import com.qpidnetwork.request.item.LCVideoListVideoItem;

public interface OnLCGetVideoListCallback {
	public void OnLCGetVideoList(boolean isSuccess, String errno, String errmsg, LCVideoListGroupItem[] groups, LCVideoListVideoItem[] videos);
}
