package com.qpidnetwork.ladydating.bean;

import java.io.Serializable;

import com.qpidnetwork.livechat.jni.LiveChatClient;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.request.RequestEnum.PHOTO_STATUS;
import com.qpidnetwork.request.item.ManListItem;

public class ManInfoBean implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public String man_id = ""; //男士ID
	public String userName = ""; //男士姓名
	public int age = 0; //年龄
	public String country = ""; //国籍
	public String photoUrl = ""; //男士头像
	public PHOTO_STATUS photo_status = PHOTO_STATUS.Yes; //头像状态
	
	public int sort_id = 0;//排序Id 默认用于男士列表排序使用
	public boolean isOnline = false;// 是否在线
	
	public ManInfoBean(){
		
	}
	
	/**
	 * 解析器:LiveChatTalkUserListItem 转 ManInfoBean
	 * @param item
	 * @return
	 */
	public static ManInfoBean parse(LiveChatTalkUserListItem item){
		ManInfoBean bean = null;
		if(item != null){
			bean = new ManInfoBean();
			bean.man_id = item.userId;
			bean.userName = item.userName;
			bean.age = item.age;
			bean.country = item.country;
			bean.photoUrl = item.imgUrl;
			bean.isOnline = (item.statusType == LiveChatClient.UserStatusType.USTATUS_ONLINE ? true:false);
		}
		return bean;
	}
	
	/**
	 * 解析器:ManListItem 转 ManInfoBean
	 * @param item
	 * @return
	 */
	public static ManInfoBean parse(ManListItem item){
		ManInfoBean bean = null;
		if(item != null){
			bean = new ManInfoBean();
			bean.man_id = item.man_id;
			bean.userName = item.firstname;
			bean.age = item.age;
			bean.country = item.country.name();
			bean.photoUrl = item.photo_url;
			bean.photo_status = item.photo_status;
		}
		return bean;
	}
}
