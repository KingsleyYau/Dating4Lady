/*
 * ManRecentChatListItem.h
 *
 *  Created on: 2015-3-2
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#ifndef MANRECENTCHATLISTITEM_H_
#define MANRECENTCHATLISTITEM_H_

#include <string>
using namespace std;

#include <common/CommonFunc.h>

#include <json/json/json.h>

#include "../RequestEnumDefine.h"
#include "../RequestManDefine.h"

class ManRecentChatListItem {
public:
	void Parse(const Json::Value& root) {
		if( root.isObject() ) {
			if( root[MAN_RECENT_CHAT_LIST_AGE].isInt() ) {
				age = root[MAN_RECENT_CHAT_LIST_AGE].asInt();
			}

			if( root[MAN_RECENT_CHAT_LIST_MAN_ID].isString() ) {
				man_id = root[MAN_RECENT_CHAT_LIST_MAN_ID].asString();
			}

			if( root[MAN_RECENT_CHAT_LIST_FIRSTNAME].isString() ) {
				firstname = root[MAN_RECENT_CHAT_LIST_FIRSTNAME].asString();
			}

			if( root[MAN_RECENT_CHAT_LIST_LASTNAME].isString() ) {
				lastname = root[MAN_RECENT_CHAT_LIST_LASTNAME].asString();
			}

			if( root[MAN_RECENT_CHAT_LIST_COUNTRY].isString() ) {
				string strCountry = root[MAN_RECENT_CHAT_LIST_COUNTRY].asString();
				for(int i = 0; i < _countof(CountryArray); i++) {
					if( strCountry.compare(CountryArray[i]) == 0 ) {
						country = i;
					}
				}
			}

			if( root[MAN_RECENT_CHAT_LIST_PHOTO_URL].isString() ) {
				photo_url = root[MAN_RECENT_CHAT_LIST_PHOTO_URL].asString();
			}

			if ( root[MAN_RECENT_CHAT_LIST_CLIENT_TYPE].isInt() ) {
				int iStatus = root[MAN_RECENT_CHAT_LIST_CLIENT_TYPE].asInt();
				status = (ONLINESTATUS_ONLINE <= iStatus && iStatus <= ONLINESTATUS_OFFLINE
										? (OnlineStatus)iStatus : ONLINESTATUS_OFFLINE);
			}

			if( root[MAN_RECENT_CHAT_LIST_CLIENT_TYPE].isInt() ) {
				client_type = root[MAN_RECENT_CHAT_LIST_CLIENT_TYPE].asInt();
			}
		}
	}

	ManRecentChatListItem() {		age = 0;
		man_id = "";
		firstname = "";
		lastname = "";
		country = -1;
		photo_url = "";
		status = ONLINESTATUS_OFFLINE;
		client_type = 0;
	}
	virtual ~ManRecentChatListItem() {

	}

	/**
	 * 获取男士列表结构体
	 * @param man_id		男士ID
	 * @param firstname		女士first name
	 * @param lastname		女士last name
	 * @param age			年龄
	 * @param country		国家,参考枚举 <RequestEnum.Country>
	 * @param photo_url		图片URL
	 * @param status		在线状态
	 * @param client_type	设备类型 默认0
	 */
	int age;
	string man_id;
	string firstname;
	string lastname;
	int country;
	string province;
	string photo_url;
	OnlineStatus status;
	int client_type;
};

#endif /* MANRECENTCHATLISTITEM_H_ */
