package com.qpidnetwork.request.item;


/**
 * 机构信息
 * @author Samson Fan
 *
 */
public class AgentInfoItem
{
	public AgentInfoItem() 
	{
		this.name = "";
		this.id = "";
		this.city = "";
		this.addr = "";
		this.email = "";
		this.tel = "";
		this.fax = "";
		this.contact = "";
		this.postcode = "";
	}
	
	/**
	 * 机构信息
	 * @param name		机构名称
	 * @param id		机构ID
	 * @param city		城市
	 * @param addr		地址
	 * @param email		电子邮箱
	 * @param tel		电话
	 * @param fax		传真
	 * @param contact	联系人名称
	 * @param postcode	邮政编码
	 */
	public AgentInfoItem(
			String name,
			String id,
			String city,
			String addr,
			String email,
			String tel,
			String fax,
			String contact,
			String postcode
			) 
	{
		this.name = name;
		this.id = id;
		this.city = city;
		this.addr = addr;
		this.email = email;
		this.tel = tel;
		this.fax = fax;
		this.contact = contact;
		this.postcode = postcode;
	}
	
	public String name;
	public String id;
	public String city;
	public String addr;
	public String email;
	public String tel;
	public String fax;
	public String contact;
	public String postcode;
}
