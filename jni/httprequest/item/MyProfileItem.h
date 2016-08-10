/*
 * MyProfileItem.h
 *
 *  Created on: 2015-11-23
 *      Author: Samson
 */
#ifndef MYPROFILE_ITEM_H
#define MYPROFILE_ITEM_H

using namespace std;

#include <list>
#include <string>
#include <common/StringHandle.h>
#include <common/CommonFunc.h>
#include <json/json/json.h>
#include "../RequestOtherDefine.h"

class MyProfileItem
{
public:
	MyProfileItem()
	{
		id = "";
		firstname = "";
		lastname = "";
		age = 0;
		country = "";
		province = "";
		city = "";
		birthday = "";
		zodiac = 0;
		weight = "";
		height = "";
		smoke = 0;
		drink = 0;
		english = 0;
		religion = 0;
		education = 0;
		profession = 0;
		children = 0;
		marry = 0;
		manAge1 = "";
		manAge2 = "";
		aboutMe = "";
		photoUrl = "";
		lastRefresh = "";
	};

	MyProfileItem(const MyProfileItem& item)
	{
		id = item.id;
		firstname = item.firstname;
		lastname = item.lastname;
		age = item.age;
		country = item.country;
		province = item.province;
		city = item.city;
		birthday = item.birthday;
		zodiac = item.zodiac;
		weight = item.weight;
		height = item.height;
		smoke = item.smoke;
		drink = item.drink;
		english = item.english;
		religion = item.religion;
		education = item.education;
		profession = item.profession;
		children = item.children;
		marry = item.marry;
		manAge1 = item.manAge1;
		manAge2 = item.manAge2;
		aboutMe = item.aboutMe;
		photoUrl = item.photoUrl;
		lastRefresh = item.lastRefresh;
	}

	~MyProfileItem() {};

public:
	bool Parsing(const Json::Value& data)
	{
		bool result = false;
		if(data.isObject())
		{
			if (data[OTHER_MYPROFILE_ID].isString()) {
				id = data[OTHER_MYPROFILE_ID].asString();
			}
			if (data[OTHER_MYPROFILE_FIRSTNAME].isString()) {
				firstname = data[OTHER_MYPROFILE_FIRSTNAME].asString();
			}
			if (data[OTHER_MYPROFILE_LASTNAME].isString()) {
				lastname = data[OTHER_MYPROFILE_LASTNAME].asString();
			}
			if (data[OTHER_MYPROFILE_AGE].isIntegral()) {
				age = data[OTHER_MYPROFILE_AGE].asInt();
			}
			if (data[OTHER_MYPROFILE_COUNTRY].isString()) {
				country = data[OTHER_MYPROFILE_COUNTRY].asString();
			}
			if (data[OTHER_MYPROFILE_PROVINCE].isString()) {
				province = data[OTHER_MYPROFILE_PROVINCE].asString();
			}
			if (data[OTHER_MYPROFILE_CITY].isString()) {
				city = data[OTHER_MYPROFILE_CITY].asString();
			}
			if (data[OTHER_MYPROFILE_BIRTHDAY].isString()) {
				birthday = data[OTHER_MYPROFILE_BIRTHDAY].asString();
			}
			if (data[OTHER_MYPROFILE_ZODIAC].isString()) {
				string strZodiac = data[OTHER_MYPROFILE_ZODIAC].asString();
				zodiac = atoi(strZodiac.c_str());
			}
			if (data[OTHER_MYPROFILE_WEIGHT].isString()) {
				weight = data[OTHER_MYPROFILE_WEIGHT].asString();
			}
			if (data[OTHER_MYPROFILE_HEIGHT].isString()) {
				height = data[OTHER_MYPROFILE_HEIGHT].asString();
			}
			if (data[OTHER_MYPROFILE_SMOKE].isString()) {
				string strSmoke = data[OTHER_MYPROFILE_BIRTHDAY].asString();
				smoke = atoi(strSmoke.c_str());
			}
			if (data[OTHER_MYPROFILE_DRINK].isString()) {
				string strDrink = data[OTHER_MYPROFILE_DRINK].asString();
				drink = atoi(strDrink.c_str());
			}
			if (data[OTHER_MYPROFILE_ENGLISH].isString()) {
				string strEnglish = data[OTHER_MYPROFILE_ENGLISH].asString();
				english = atoi(strEnglish.c_str());
			}
			if (data[OTHER_MYPROFILE_RELIGION].isString()) {
				string strReligion = data[OTHER_MYPROFILE_RELIGION].asString();
				religion = atoi(strReligion.c_str());
			}
			if (data[OTHER_MYPROFILE_EDUCATION].isString()) {
				string strEducation = data[OTHER_MYPROFILE_EDUCATION].asString();
				education = atoi(strEducation.c_str());
			}
			if (data[OTHER_MYPROFILE_PROFESSION].isString()) {
				string strProfession = data[OTHER_MYPROFILE_PROFESSION].asString();
				profession = atoi(strProfession.c_str());
			}
			if (data[OTHER_MYPROFILE_CHILDREN].isString()) {
				string strChildren = data[OTHER_MYPROFILE_CHILDREN].asString();
				children = (strChildren == OTHER_MYPROFILE_CHILDREN_Y ? 2 : 1);
			}
			if (data[OTHER_MYPROFILE_MARRY].isString()) {
				string strMarry = data[OTHER_MYPROFILE_MARRY].asString();
				marry = atoi(strMarry.c_str());
			}
			if (data[OTHER_MYPROFILE_MANAGE1].isString()) {
				manAge1 = data[OTHER_MYPROFILE_MANAGE1].asString();
			}
			if (data[OTHER_MYPROFILE_MANAGE2].isString()) {
				manAge2 = data[OTHER_MYPROFILE_MANAGE2].asString();
			}
			if (data[OTHER_MYPROFILE_ABOUTME].isString()) {
				aboutMe = data[OTHER_MYPROFILE_ABOUTME].asString();
			}
			if (data[OTHER_MYPROFILE_PHOTOURL].isString()) {
				photoUrl = data[OTHER_MYPROFILE_PHOTOURL].asString();
			}
			if (data[OTHER_MYPROFILE_PHOTOURLS].isString()) {
				string strUrls = data[OTHER_MYPROFILE_PHOTOURLS].asString();
				photoUrls = StringHandle::split(strUrls, OTHER_MYPROFILE_PHOTOURLS_SEPARATOR);
			}
			if (data[OTHER_MYPROFILE_THUMBURLS].isString()) {
				string strUrls = data[OTHER_MYPROFILE_THUMBURLS].asString();
				thumbUrls = StringHandle::split(strUrls, OTHER_MYPROFILE_THUMBURLS_SEPARATOR);
			}
			if (data[OTHER_MYPROFILE_LASTREFRESH].isString()) {
				lastRefresh = data[OTHER_MYPROFILE_LASTREFRESH].asString();
			}
			if (data[OTHER_MYPROFILE_LASTREFRESH].isString()) {
				lastRefresh = data[OTHER_MYPROFILE_LASTREFRESH].asString();
			}

			if(!id.empty()){
				result = true;
			}
		}
		return result;
	}
public:
	string id;
	string firstname;
	string lastname;
	int age;
	string country;
	string province;
	string city;
	string birthday;
	int zodiac;
	string weight;
	string height;
	int smoke;
	int drink;
	int english;
	int religion;
	int education;
	int profession;
	int children;
	int marry;
	string manAge1;
	string manAge2;
	string aboutMe;
	string photoUrl;
	list<string> photoUrls;
	list<string> thumbUrls;
	string lastRefresh;
};

#endif/*MYPROFILE_ITEM_H*/
