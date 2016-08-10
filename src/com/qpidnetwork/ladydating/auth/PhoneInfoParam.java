package com.qpidnetwork.ladydating.auth;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 收集手机硬件信息缓存item 
 * @author Samson Fan
 *
 */
public class PhoneInfoParam implements Serializable {

	private static final long serialVersionUID = -3992016580455076460L;
	// ------ 成员 ------
	/**
	 * 以前登录过的用户ID列表
	 */
	public ArrayList<String> preLoginUserList;

	public PhoneInfoParam()
	{
		preLoginUserList = new ArrayList<String>();
	}
}
