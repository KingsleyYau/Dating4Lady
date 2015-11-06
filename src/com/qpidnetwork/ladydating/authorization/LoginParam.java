package com.qpidnetwork.ladydating.authorization;

import java.io.Serializable;

import com.qpidnetwork.request.item.LoginItem;

public class LoginParam implements Serializable {

	private static final long serialVersionUID = -3519711941929090295L;
	
	public LoginParam() {
		this.email = "";
		this.password = "";
		this.item = null;
	}
	
	/**
	 * 登录成功回调
	 * @param email				电子邮箱
	 * @param password			密码
	 */
	public LoginParam(
			String email,
			String password,
			LoginItem item
			) {
		this.email = email;
		this.password = password;
		this.item = item;
	}
	public String email;
	public String password;
	public LoginItem item;

}
