package com.qpidnetwork.request;

import com.qpidnetwork.request.item.AgentInfoItem;

/**
 * 获取机构信息回调 
 * @author Samson Fan
 *
 */
public interface OnOtherGetAgentInfoCallback {
	/**
	 * 获取机构信息回调
	 * @param requestId	请求ID
	 * @param isSuccess	是否请求成功
	 * @param errno		请求错误代码
	 * @param errmsg	请求错误描述
	 * @param item		机构信息item
	 */
	public void OnOtherGetAgentInfo(long requestId, boolean isSuccess, String errno
			, String errmsg, AgentInfoItem item);
}
