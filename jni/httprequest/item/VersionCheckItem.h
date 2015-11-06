/*
 * RequestVersionCheckTask.h
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#ifndef VERSIONCHECKITEM_H
#define VERSIONCHECKITEM_H

using namespace std;
#include <string>
#include <common/CommonFunc.h>
#include <json/json/json.h>
#include "../RequestOtherDefine.h"

class VersionCheckItem{
public:
	VersionCheckItem(){
		apkVersionCode = 0;
		apkVersionName = "";
		apkUrl = "";
	};
	~VersionCheckItem(){};
public:
	bool Parsing(const Json::Value& data){
		bool result = false;
		if(data.isObject()){
			if(data[CHECK_VERSION_APKVERSION_CODE].isInt()){
				apkVersionCode = data[CHECK_VERSION_APKVERSION_CODE].asInt();
			}
			if(data[CHECK_VERSION_SDKVERSION_NAME].isString()){
				apkVersionName = data[CHECK_VERSION_SDKVERSION_NAME].asString();
			}
			if(data[CHECK_VERSION_APKURL].isString()){
				apkUrl = data[CHECK_VERSION_APKURL].asString();
			}
			if(!apkUrl.empty()){
				result = true;
			}
		}
		return result;
	}
public:
	int apkVersionCode;
	string apkVersionName;
	string apkUrl;
};

#endif/*VERSIONCHECKITEM_H*/
