package com.qpidnetwork.request.item;

public class ManRecentViewListItem {
	public ManRecentViewListItem() {
		
	}
	/**
	 * 获取最近访客列表构体
	 * @param man_id		男士ID
	 * @param last_time		最后访问时间的秒数
	 */
	public ManRecentViewListItem(
		 String man_id, 
		 int last_time
			) {
		
		this.man_id = man_id;
		this.last_time = last_time;
	}
	
	public String toString() {
		String result = "{ ";
		result += "man_id = " + man_id + ", ";
		result += "last_time = " + last_time;
		result += " }";
		return result;
	}
	
	public String man_id;
	public int last_time;
}
