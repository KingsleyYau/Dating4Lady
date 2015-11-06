package com.qpidnetwork.request.item;

import java.io.Serializable;

/**
 * 登录成功结构体
 * @author Max.chiu
 *
 */
public class LoginItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -899283677195011786L;
	public LoginItem() {
		this.lady_id = "";
		this.firstname = "";
		this.lastname = "";	
		this.photo_url = "";
		this.agent = "";
		
		this.login = false;
		this.search = false;
		this.admirermail = false;
		this.livechat = false;
		this.video = false;
	}
	
	/**
	 * 登录成功结构体
	 * @param lady_id			女士id
	 * @param firstname			用户first name
	 * @param lastname			用户last name
	 * @param photo_url			头像URL
	 * @param agent				机构id
	 * 
	 * @param login				登录许可
	 * @param search			是否允许查询男士
	 * @param admirermail		是否允许发送意向信
	 * @param livechat			是否允许使用livechat
	 * @param video				是否允许使用videochat
	 */
	public LoginItem(
			String lady_id,
			String firstname,
			String lastname,	
			String photo_url,
			String agent,
			
			boolean login,
			boolean search,
			boolean admirermail,
			boolean livechat,
			boolean video
			) {
		this.lady_id = lady_id;
		this.firstname = firstname;
		this.lastname = lastname;	
		this.photo_url = photo_url;
		this.agent = agent;
		
		this.login = login;
		this.search = search;
		this.admirermail = admirermail;
		this.livechat = livechat;
		this.video = video;
	}
	
	public String toString() {
		String result = "{ ";
		result += "lady_id = " + lady_id + ", ";
		result += "firstname = " + firstname + ", ";
		result += "lastname = " + lastname + ", ";
		result += "login = " + login + ", ";
		result += "search = " + search + ", ";
		result += "admirermail = " + admirermail + ", ";
		result += "livechat = " + livechat;
		result += " }";
		return result;
	}
	
	public String lady_id;
	public String firstname;
	public String lastname;	
	public String photo_url;
	public String agent;
	
	public boolean login;
	public boolean search;
	public boolean admirermail;
	public boolean livechat;
	public boolean video;
}
