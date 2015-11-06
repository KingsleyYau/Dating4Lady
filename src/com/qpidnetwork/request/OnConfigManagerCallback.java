package com.qpidnetwork.request;

import com.qpidnetwork.request.item.SynConfigItem;

public interface OnConfigManagerCallback {
	public void OnSynConfig(boolean isSuccess, String errno, String errmsg, SynConfigItem item);
}
