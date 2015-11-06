package com.qpidnetwork.request;

import com.qpidnetwork.request.item.SynConfigItem;


public interface OnSynConfigCallback {
	public void OnSynConfig(boolean isSuccess, String errno, String errmsg, SynConfigItem item);
}
