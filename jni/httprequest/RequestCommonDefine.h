/*
 * RequestCommonDefine.h
 *
 *  Created on: 2015-11-18
 *      Author: Samson
 * Description: 非协议公共接口定义
 */
#ifndef REQUESTCOMMONDEFINE_H
#define REQUESTCOMMONDEFINE_H

#include "RequestDefine.h"

/* 翻译接口 */
//path
#define TRANSLATETEXT_PATH                 "/v2/http.svc/Translate"

//请求参数
#define TRANSLATETEXT_APPID			        "appid"
#define TRANSLATETEXT_FROM			        "from"
#define TRANSLATETEXT_TO			        "to"
#define TRANSLATETEXT_TEXT			        "text"
#define TRANSLATETEXT_TO			        "to"
#define TRANSLATETEXT_CONTENTTYPE			"contentType"
#define TRANSLATETEXT_CONTENTTYPE_VALUE			"text/html"

//返回参数
#define TRANSLATETEXT_STRING				"string"

#endif/*REQUESTCOMMONDEFINE_H*/
