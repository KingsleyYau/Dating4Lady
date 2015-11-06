package com.qpidnetwork.request;

import com.qpidnetwork.request.item.ManDetailItem;

public interface OnQueryManDetailCallback {
	public void OnQueryManDetail(boolean isSuccess, String errno, String errmsg, ManDetailItem item);
}
