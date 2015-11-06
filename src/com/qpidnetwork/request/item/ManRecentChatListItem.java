package com.qpidnetwork.request.item;

import com.qpidnetwork.request.RequestEnum.*;

public class ManRecentChatListItem {
	public ManRecentChatListItem() {
		
	}
	/**
	 * 获取最近聊天男士列表结构体
	 * @param man_id		男士ID
	 * @param firstname		男士first name
	 * @param lastname		男士last name
	 * @param age			年龄
	 * @param country		国家,参考枚举 <RequestEnum.Country>
	 * @param photo_url		图片URL
	 * @param status		在线状态
	 * @param client_type	设备类型 默认0
	 */
	public ManRecentChatListItem(
		 String man_id, 
		 String firstname,
		 String lastname,
		 int age,
		 int country,
		 String photo_url,
		 int status,
		 int client_type
			) {
		
		this.man_id = man_id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.age = age;
		
		this.country = Country.Unknow;
		if ( country >= 0 && country < Country.values().length ) {
			this.country = Country.values()[country];
		}
		this.photo_url = photo_url;
		
		this.status = OnlineStatus.Offline;
		if ( status >= 0 && status < OnlineStatus.values().length ) {
			this.status = OnlineStatus.values()[status];
		}
		
		this.client_type = client_type;
	}
	
	public String toString() {
		String result = "{ ";
		result += "man_id = " + man_id + ", ";
		result += "firstname = " + firstname + ", ";
		result += "lastname = " + lastname + ", ";
		result += "age = " + age + ", ";
		result += "country = " + country.name() + ", ";
		result += "photo_url = " + photo_url + ", ";
		result += "status = " + status.name() + ", ";
		result += "client_type = " + client_type;
		result += " }";
		return result;
	}
	
	public String man_id;
	public String firstname;
	public String lastname;
	public int age;
	public Country country;
	public String province;
	public String photo_url;

	public OnlineStatus status;
	public int client_type;
}
