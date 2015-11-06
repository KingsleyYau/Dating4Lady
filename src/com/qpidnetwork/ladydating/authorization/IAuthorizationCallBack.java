package com.qpidnetwork.ladydating.authorization;

import com.qpidnetwork.request.item.LoginItem;

public interface IAuthorizationCallBack {
	
	public enum OperateType{
		AUTO, //自动
		MANUAL //手动
	}
	
	/**
	 * 登录成功回调
	 * @param operateType       操作类型（目前区分自动、手动）
	 * @param isSuccess			是否登录成功
	 * @param errno				错误代码
	 * @param errmsg			错误信息
	 * @param item				登录成功结构（可空）
	 */
	public void OnLogin(OperateType operateType, boolean isSuccess, String errno, String errmsg, LoginItem item);
	
	/**
	 * 注销成功回调
	 * @param operateType       操作类型（目前区分自动、手动）
	 */
	public void OnLogout(OperateType operateType);
	
}
