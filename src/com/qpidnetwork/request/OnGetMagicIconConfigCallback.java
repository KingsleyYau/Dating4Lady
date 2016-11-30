package com.qpidnetwork.request;

import com.qpidnetwork.request.item.MagicIconConfig;

/**
 * 获取魔法表情配置回调接口
 * @author Hunter
 * @since 2016.4.7
 */
public interface OnGetMagicIconConfigCallback {
	public void OnGetMagicIconConfig(boolean isSuccess, String errno, String errmsg, MagicIconConfig config);
}
