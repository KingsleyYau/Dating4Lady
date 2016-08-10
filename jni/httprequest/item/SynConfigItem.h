/*
 * SynConfigItem.h
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#ifndef SYNCONFIGITEM_H
#define SYNCONFIGITEM_H

using namespace std;

#include <list>
#include <string>
#include <common/StringHandle.h>
#include <common/CommonFunc.h>
#include <json/json/json.h>
#include "../RequestOtherDefine.h"

class SynConfigItem{
public:
	SynConfigItem(){
		socketHost = "";
		socketPort = 0;
		socketVersion = "";
		socketFromId = 0;
		translateUrl = "";
		apkVersionCode = 0;
		apkVersionName = "";
		apkVersionUrl = "";
		siteUrl = "";
		liveChatVoiceHost = "";
		videoUploadHost = "";
	};

	SynConfigItem(const SynConfigItem& item) {
		socketHost = item.socketHost;
		socketPort = item.socketPort;
		socketVersion = item.socketVersion;
		socketFromId = item.socketFromId;
		translateUrl = item.translateUrl;
		apkVersionCode = item.apkVersionCode;
		apkVersionName = item.apkVersionName;
		apkVersionUrl = item.apkVersionUrl;
		siteUrl = item.siteUrl;
		liveChatVoiceHost = item.liveChatVoiceHost;
		videoUploadHost = item.videoUploadHost;
	}

	~SynConfigItem(){};

	SynConfigItem& operator=(const SynConfigItem& item) {
		socketHost = item.socketHost;
		socketPort = item.socketPort;
		socketVersion = item.socketVersion;
		socketFromId = item.socketFromId;
		translateUrl = item.translateUrl;
		apkVersionCode = item.apkVersionCode;
		apkVersionName = item.apkVersionName;
		apkVersionUrl = item.apkVersionUrl;
		siteUrl = item.siteUrl;
		liveChatVoiceHost = item.liveChatVoiceHost;
		videoUploadHost = item.videoUploadHost;
		return *this;
	}

	bool Parsing(const Json::Value& data){
		bool result = false;
		if(data.isObject()){
			if(data[SYN_CONFIG_SOCKET_HOST].isString()){
				socketHost = data[SYN_CONFIG_SOCKET_HOST].asString();
			}
			if(data[SYN_CONFIG_SOCKET_PORT].isInt()){
				socketPort = data[SYN_CONFIG_SOCKET_PORT].asInt();
			}
			if(data[SYN_CONFIG_SOCKET_VERSION].isString()){
				socketVersion = data[SYN_CONFIG_SOCKET_VERSION].asString();
			}
			if(data[SYN_CONFIG_SOCKET_FROMID].isInt()){
				socketFromId = data[SYN_CONFIG_SOCKET_FROMID].asInt();
			}
			if(data[SYN_CONFIG_TRANSLATE_URL].isString()){
				translateUrl = data[SYN_CONFIG_TRANSLATE_URL].asString();
			}
			if(data[SYN_CONFIG_TRANSLATE_LANGUAGES].isString()){
				string languages = data[SYN_CONFIG_TRANSLATE_LANGUAGES].asString();
				if(!languages.empty()){
					translateLanguage = StringHandle::split(languages, "#");
				}
			}
			if(data[SYN_CONFIG_APK_VERSIONCODE].isInt()){
				apkVersionCode = data[SYN_CONFIG_APK_VERSIONCODE].asInt();
			}
			if(data[SYN_CONFIG_APK_VERSIONNAME].isString()){
				apkVersionName = data[SYN_CONFIG_APK_VERSIONNAME].asString();
			}
			if(data[SYN_CONFIG_APK_VERSIONURL].isString()){
				apkVersionUrl = data[SYN_CONFIG_APK_VERSIONURL].asString();
			}
			if(data[SYN_CONFIG_SITE_URL].isString()){
				siteUrl = data[SYN_CONFIG_SITE_URL].asString();
			}
			if(data[SYN_CONFIG_LIVECHATVOICEHOST].isString()){
				liveChatVoiceHost = data[SYN_CONFIG_LIVECHATVOICEHOST].asString();
			}
			if(data[SYN_CONFIG_VIDEOUPLOADHOST].isString()){
				videoUploadHost = data[SYN_CONFIG_VIDEOUPLOADHOST].asString();
			}

			if(!socketHost.empty()){
				result = true;
			}
		}
		return result;
	}
public:
	string socketHost;
	int socketPort;
	string socketVersion;
	int socketFromId;
	string translateUrl;
	list<string> translateLanguage;
	int apkVersionCode;
	string apkVersionName;
	string apkVersionUrl;
	string siteUrl;
	string liveChatVoiceHost;
	string videoUploadHost;
};

#endif/*SYNCONFIGITEM_H*/
