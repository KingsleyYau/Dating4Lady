package com.qpidnetwork.request.item;

import com.qpidnetwork.request.RequestEnum.Country;
import com.qpidnetwork.request.RequestEnum.PHOTO_STATUS;

public class ManListItem {
	public ManListItem() {
		
	}
	/**
	 * 获取男士列表结构体
	 * @param man_id		男士ID
	 * @param firstname		女士first name
	 * @param lastname		女士last name
	 * @param age			年龄
	 * @param country		国家,参考枚举 <RequestEnum.Country>
	 * @param province		省份 
	 * @param photo_url		图片URL
	 * @param photo_status	头像状态
	 */
	public ManListItem(
		 String man_id, 
		 String firstname,
		 String lastname,
		 int age,
		 int country,
		 String province,
		 String photo_url,
		 int photo_status
			) {
		this.man_id = man_id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.age = age;
		
		this.country = Country.Unknow;
		if ( country >= 0 && country < Country.values().length ) {
			this.country = Country.values()[country];
		}
		this.province = province;
		this.photo_url = photo_url;
		
		this.photo_status = PHOTO_STATUS.None;
		if ( photo_status >= 0 && photo_status < PHOTO_STATUS.values().length) {
			this.photo_status = PHOTO_STATUS.values()[photo_status];
		}
	}
	
	public String toString() {
		String result = "{ ";
		result += "man_id = " + man_id + ", ";
		result += "firstname = " + firstname + ", ";
		result += "lastname = " + lastname + ", ";
		result += "age = " + age + ", \n";
		result += "country = " + country.name() + ", ";
		result += "province = " + province + ", ";
		result += "photo_url = " + photo_url + ", ";
		result += "photo_status = " + photo_status.name();
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

	public PHOTO_STATUS photo_status;
}
