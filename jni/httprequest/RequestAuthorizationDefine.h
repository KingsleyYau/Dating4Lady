/*
 * RequestAuthorizationDefine.h
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#ifndef REQUESTAUTHORIZATIONDEFINE_H_
#define REQUESTAUTHORIZATIONDEFINE_H_

#include "RequestDefine.h"

/* ########################	认证相关 模块  ######################## */

/* 2.1.登录 */
/* 接口路径  */
#define LOGIN_PATH "/lady/logincheck"

/**
 * 请求
 */
#define AUTHORIZATION_ACCOUNT			"account"
#define AUTHORIZATION_PASSWORD			"password"
#define AUTHORIZATION_DEVICE_ID			"device_id"
#define AUTHORIZATION_MODEL				"model"
#define AUTHORIZATION_MANUFACTURER		"manufacturer"

/**
 * 返回
 */
#define AUTHORIZATION_LADY_ID			"lady_id"
#define AUTHORIZATION_SESSIONID			"sessionid"
#define AUTHORIZATION_FIRSTNAME 		"firstname"
#define AUTHORIZATION_LASTNAME 			"lastname"
#define AUTHORIZATION_PHOTO_URL 		"photo_url"
#define AUTHORIZATION_AGENT 			"agent"

#define AUTHORIZATION_AUTH				"auth"
#define AUTHORIZATION_AUTH_LOGIN 		"login"
#define AUTHORIZATION_AUTH_SEARCH 		"search"
#define AUTHORIZATION_AUTH_ADMIRERMAIL	"admirermail"
#define AUTHORIZATION_AUTH_LIVECHAT 	"livechat"
#define AUTHORIZATION_AUTH_VIDEO 		"video"


#endif /* REQUESTAUTHORIZATIONDEFINE_H_ */
