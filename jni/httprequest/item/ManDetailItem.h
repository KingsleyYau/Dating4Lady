/*
 * ManDetailItem.h
 *
 *  Created on: 2015-3-2
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#ifndef MANDETAILITEM_H_
#define MANDETAILITEM_H_

#include <string>
using namespace std;

#include <common/CommonFunc.h>

#include <json/json/json.h>

#include "../RequestEnumDefine.h"
#include "../RequestManDefine.h"

class ManDetailItem {
public:
	void Parse(const Json::Value& root) {
		if( root.isObject() ) {
			if( root[MAN_DETAIL_MAN_ID].isString() ) {
				man_id = root[MAN_DETAIL_MAN_ID].asString();
			}

			if( root[MAN_DETAIL_FIRSTNAME].isString() ) {
				firstname = root[MAN_DETAIL_FIRSTNAME].asString();
			}

			if( root[MAN_DETAIL_LASTNAME].isString() ) {
				lastname = root[MAN_DETAIL_LASTNAME].asString();
			}

			if( root[MAN_DETAIL_COUNTRY].isString() ) {
				string strCountry = root[MAN_DETAIL_COUNTRY].asString();
				for(int i = 0; i < _countof(CountryArray); i++) {
					if( strCountry.compare(CountryArray[i]) == 0 ) {
						country = i;
					}
				}
			}

			if( root[MAN_DETAIL_PROVINCE].isString() ) {
				province = root[MAN_DETAIL_PROVINCE].asString();
			}

			if( root[MAN_DETAIL_PROVINCE].isString() ) {
				city = root[MAN_DETAIL_PROVINCE].asString();
			}

			if( root[MAN_DETAIL_JOIN_DATE].isString() ) {
				join_date = root[MAN_DETAIL_JOIN_DATE].asString();
			}

			if( root[MAN_DETAIL_BIRTHDAY].isString() ) {
				birthday = root[MAN_DETAIL_BIRTHDAY].asString();
			}

			if( root[MAN_DETAIL_WEIGHT].isString() ) {
				weight = atoi(root[MAN_DETAIL_WEIGHT].asString().c_str()) - MAN_DETAIL_WEIGHT_BEGINVALUE;
			}

			if( root[MAN_DETAIL_HEIGHT].isString() ) {
				height = atoi(root[MAN_DETAIL_HEIGHT].asString().c_str()) - MAN_DETAIL_HEIGHT_BEGINVALUE;
			}

			if( root[MAN_DETAIL_SMOKE].isString() ) {
				smoke = atoi(root[MAN_DETAIL_SMOKE].asString().c_str());
			}

			if( root[MAN_DETAIL_DRINK].isString() ) {
				drink = atoi(root[MAN_DETAIL_DRINK].asString().c_str());
			}

			if( root[MAN_DETAIL_LANGUAGE].isString() ) {
				language = atoi(root[MAN_DETAIL_LANGUAGE].asString().c_str());
			}

			if( root[MAN_DETAIL_RELIGION].isString() ) {
				religion = atoi(root[MAN_DETAIL_RELIGION].asString().c_str());
			}

			if( root[MAN_DETAIL_EDUCATION].isString() ) {
				education = atoi(root[MAN_DETAIL_EDUCATION].asString().c_str());
			}

			if( root[MAN_DETAIL_PROFESSION].isString() ) {
				profession = atoi(root[MAN_DETAIL_PROFESSION].asString().c_str());
			}

			if( root[MAN_DETAIL_CHILDREN].isString() ) {
				children = atoi(root[MAN_DETAIL_CHILDREN].asString().c_str());
			}

			if( root[MAN_DETAIL_MARRY].isString() ) {
				marry = atoi(root[MAN_DETAIL_MARRY].asString().c_str());
			}

			if( root[MAN_DETAIL_INCOME].isString() ) {
				income = atoi(root[MAN_DETAIL_INCOME].asString().c_str());
			}

			if( root[MAN_DETAIL_ETHNICITY].isString() ) {
				ethnicity = atoi(root[MAN_DETAIL_ETHNICITY].asString().c_str());
			}

			if( root[MAN_DETAIL_ABOUT_ME].isString() ) {
				about_me = root[MAN_DETAIL_ABOUT_ME].asString();
			}

			if( root[MAN_DETAIL_ONLINE].isInt() ) {
				online = (root[MAN_DETAIL_ONLINE].asInt() == 0)?false:true;
			}

			if( root[MAN_DETAIL_FAVORITE].isInt() ) {
				favorite = (root[MAN_DETAIL_FAVORITE].asInt() == 0)?false:true;
			}

			if( root[MAN_DETAIL_PHOTO_URL].isString() ) {
				photo_url = root[MAN_DETAIL_PHOTO_URL].asString();
			}

			if( root[MAN_DETAIL_PHOTO_BIG_URL].isString() ) {
				photo_big_url = root[MAN_DETAIL_PHOTO_BIG_URL].asString();
			}

			if ( root[MAN_DETAIL_PHOTO_STATUS].isInt() ) {
				int status = root[MAN_DETAIL_PHOTO_STATUS].asInt();
				photo_status = (NONE <= status && status <= FAIL
										? (PHOTO_STATUS)status : NONE);
			}

			if( root[MAN_DETAIL_RECEIVE_ADMIRER].isInt() ) {
				receive_admirer = (root[MAN_DETAIL_RECEIVE_ADMIRER].asInt() == 0)?false:true;
			}
		}
	}

	ManDetailItem() {		man_id = "";
		firstname = "";
		lastname = "";

		country = -1;
		province = "";
		city = "";

		join_date = "";
		birthday = "";

		marry = -1;
		height = -1;
		weight = -1;
		smoke = -1;
		drink = -1;
		language = -1;
		religion = -1;
		education = -1;
		profession = -1;
		ethnicity = -1;
		income= -1;
		children = -1;

		about_me = "";
		online = false;
		favorite = false;

		photo_url = "";
		photo_big_url = "";
		photo_status = NONE;
		receive_admirer = false;
	}
	virtual ~ManDetailItem() {

	}

	/**
	 * 查询女士详细信息结构体
	 * @param man_id			男士id
	 * @param firstname			男士first name
	 * @param lastname			男士last name
	 *
	 * @param country			国家
	 * @param province			省份
	 * @param city				城市
	 *
	 * @param join_date			注册时间
	 * @param birthday			出生日期
	 *
	 * @param weight			体重
	 * @param height			身高
	 * @param smoke				吸烟情况
	 * @param drink				喝酒情况
	 * @param language			语言
	 * @param religion			宗教情况
	 * @param education			教育情况
	 * @param profession		职业
	 * @param children			子女状况
	 * @param marry				婚姻状况
	 * @param income			收入情况
	 * @param ethnicity			种族
	 *
	 * @param about_me			个人简介
	 * @param online			是否在线
	 * @param favorite			是否收藏
	 *
	 * @param photo_url			男士头像URL
	 * @param photo_big_url		男士大头像URL
	 * @param photo_status		男士头像状态
	 * @param receive_admirer	是否接收意向信
	 */
	string man_id;
	string firstname;
	string lastname;

	int country;
	string province;
	string city;

	string join_date;
	string birthday;

	int height;
	int weight;
	int smoke;
	int drink;
	int language;
	int religion;
	int education;
	int profession;
	int children;
	int marry;
	int income;
	int ethnicity;

	string about_me;

	bool online;
	bool favorite;

	string photo_url;
	string photo_big_url;

	PHOTO_STATUS photo_status;

	bool receive_admirer;
};

#endif /* MANDETAILITEM_H_ */
