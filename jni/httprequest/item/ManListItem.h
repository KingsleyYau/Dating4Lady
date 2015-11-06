/*
 * ManListItem.h
 *
 *  Created on: 2015-3-2
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#ifndef MANLISTITEM_H_
#define MANLISTITEM_H_

#include <string>
using namespace std;

#include <common/CommonFunc.h>

#include <json/json/json.h>

#include "../RequestEnumDefine.h"
#include "../RequestManDefine.h"

class ManListItem {
public:
	void Parse(const Json::Value& root) {
		if( root.isObject() ) {
			if( root[MAN_LIST_AGE].isInt() ) {
				age = root[MAN_LIST_AGE].asInt();
			}

			if( root[MAN_LIST_MAN_ID].isString() ) {
				man_id = root[MAN_LIST_MAN_ID].asString();
			}

			if( root[MAN_LIST_FIRSTNAME].isString() ) {
				firstname = root[MAN_LIST_FIRSTNAME].asString();
			}

			if( root[MAN_LIST_LASTNAME].isString() ) {
				lastname = root[MAN_LIST_LASTNAME].asString();
			}

			if( root[MAN_LIST_COUNTRY].isString() ) {
				string strCountry = root[MAN_LIST_COUNTRY].asString();
				for(int i = 0; i < _countof(CountryArray); i++) {
					if( strCountry.compare(CountryArray[i]) == 0 ) {
						country = i;
					}
				}
			}

			if( root[MAN_LIST_PROVINCE].isString() ) {
				province = root[MAN_LIST_PROVINCE].asString();
			}

			if( root[MAN_LIST_PHOTO_URL].isString() ) {
				photo_url = root[MAN_LIST_PHOTO_URL].asString();
			}

			if ( root[MAN_LIST_PHOTO_STATUS].isInt() ) {
				int status = root[MAN_LIST_PHOTO_STATUS].asInt();
				photo_status = (NONE <= status && status <= FAIL
										? (PHOTO_STATUS)status : NONE);
			}
		}
	}

	ManListItem() {		age = 0;
		man_id = "";
		firstname = "";
		lastname = "";
		country = -1;
		province = "";
		photo_url = "";
		photo_status = NONE;
	}
	virtual ~ManListItem() {

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
	 * @param photo_status	头像状态（0：无相片，1：有正式相片，2：相片待处理中，3：机构上报为不合格，4：我方设为不合格）（整型）
	 */
	int age;
	string man_id;
	string firstname;
	string lastname;
	int country;
	string province;
	string photo_url;
	PHOTO_STATUS photo_status;
};

#endif /* MANLISTITEM_H_ */
