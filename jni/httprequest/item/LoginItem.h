/*
 * LoginItem.h
 *
 *  Created on: 2015-3-6
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#ifndef LOGINITEM_H_
#define LOGINITEM_H_

#include <string>
using namespace std;

#include <json/json/json.h>

#include "../RequestAuthorizationDefine.h"

class LoginItem {
public:
	void Parse(const Json::Value& root) {
		if( root.isObject() ) {
			/* 女士id */
			if( root[AUTHORIZATION_LADY_ID].isString() ) {
				lady_id = root[AUTHORIZATION_LADY_ID].asString();
			}

			/* sid */
			if( root[AUTHORIZATION_SESSIONID].isString() ) {
				sid = root[AUTHORIZATION_SESSIONID].asString();
			}

			/* 用户first name */
			if( root[AUTHORIZATION_FIRSTNAME].isString() ) {
				firstname = root[AUTHORIZATION_FIRSTNAME].asString();
			}

			/* 用户last name */
			if( root[AUTHORIZATION_LASTNAME].isString() ) {
				lastname = root[AUTHORIZATION_LASTNAME].asString();
			}

			/* 头像URL */
			if( root[AUTHORIZATION_PHOTO_URL].isString() ) {
				photo_url = root[AUTHORIZATION_PHOTO_URL].asString();
			}

			/* 机构id */
			if( root[AUTHORIZATION_AGENT].isString() ) {
				agent = root[AUTHORIZATION_AGENT].asString();
			}

			/* 权限许可 */
			if( root[AUTHORIZATION_AUTH].isObject() ) {
				Json::Value permission = root[AUTHORIZATION_AUTH];

				if( permission[AUTHORIZATION_AUTH_LOGIN].isInt() ) {
					login = permission[AUTHORIZATION_AUTH_LOGIN].asInt();
				}

				if( permission[AUTHORIZATION_AUTH_SEARCH].isInt() ) {
					search = permission[AUTHORIZATION_AUTH_SEARCH].asInt();
				}

				if( permission[AUTHORIZATION_AUTH_ADMIRERMAIL].isInt() ) {
					admirermail = permission[AUTHORIZATION_AUTH_ADMIRERMAIL].asInt();
				}

				if( permission[AUTHORIZATION_AUTH_LIVECHAT].isInt() ) {
					livechat = permission[AUTHORIZATION_AUTH_LIVECHAT].asInt();
				}

				if( permission[AUTHORIZATION_AUTH_VIDEO].isInt() ) {
					video = permission[AUTHORIZATION_AUTH_VIDEO].asInt();
				}
			}
		}
	}

	/**
	 * 登录成功结构体
	 * @param lady_id			女士id
	 * @param sid				用于LiveChat登录等
	 * @param firstname			用户first name
	 * @param lastname			用户last name
	 * @param photo_url			头像URL
	 * @param agent				机构id
	 *
	 * @param login				登录许可
	 * @param search			是否允许查询男士
	 * @param admirermail		是否允许发送意向信
	 * @param livechat			是否允许使用livechat
	 * @param video				是否允许使用videochat
	 */
	LoginItem() {
		lady_id = "";
		sid = "";
		firstname = "";
		lastname = "";
		photo_url = "";
		agent = "";

		login = true;
		search = true;
		admirermail = true;
		livechat = true;
		video = true;
	}

	virtual ~LoginItem() {

	}

	string lady_id;
	string sid;
	string firstname;
	string lastname;
	string photo_url;
	string agent;

	bool login;
	bool search;
	bool admirermail;
	bool livechat;
	bool video;
};

#endif /* LOGINITEM_H_ */
