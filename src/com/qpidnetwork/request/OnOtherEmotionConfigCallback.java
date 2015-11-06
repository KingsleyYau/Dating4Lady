package com.qpidnetwork.request;

import com.qpidnetwork.request.item.EmotionConfigItem;


public interface OnOtherEmotionConfigCallback {
	public void OnOtherEmotionConfig(boolean isSuccess, String errno, String errmsg, EmotionConfigItem item);
}
