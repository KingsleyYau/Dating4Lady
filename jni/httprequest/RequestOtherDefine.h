/*
 * RequestOtherDefine.h
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#ifndef REQUESTOTHERDEFINE_H
#define REQUESTOTHERDEFINE_H

#include "RequestDefine.h"

/*6.1 修改密码相关*/
//path
#define MODIFY_PASSWORD_PATH                 "/lady/changpassword"

//请求
#define MODIFY_PASSWORD_OLD_PASSWORD         "old_password"
#define MODIFY_PASSWORD_NEW_PASSWORD         "new_password"

/*6.3 查询高级表情配置*/
//path
#define EMOTION_CONFIG_PATH                 "/member/emotionconfig"

//返回参数解析
#define OTHER_EMOTIONCONFIG_VERSION		    "face_senior_version"
#define OTHER_EMOTIONCONFIG_FSPATH		    "face_senior_path"
#define OTHER_EMOTIONCONFIG_TYPELIST	    "face_senior_typelist"
#define OTHER_EMOTIONCONFIG_TOFLAG		    "toflag"
#define OTHER_EMOTIONCONFIG_TYPEID		    "typeid"
#define OTHER_EMOTIONCONFIG_TYPENAME	    "typename"
#define OTHER_EMOTIONCONFIG_TAGSLIST	    "face_senior_tagslist"
#define OTHER_EMOTIONCONFIG_TAGSID		    "tagsid"
#define OTHER_EMOTIONCONFIG_TAGSNAME	    "tagsname"
#define OTHER_EMOTIONCONFIG_FORMANLIST	    "face_senior_forman"
#define OTHER_EMOTIONCONFIG_FILENAME	    "filename"
#define OTHER_EMOTIONCONFIG_PRICE		    "price"
#define OTHER_EMOTIONCONFIG_ISNEW		    "isnew"
#define OTHER_EMOTIONCONFIG_ISSALE		    "issale"
#define OTHER_EMOTIONCONFIG_SORTID		    "sortid"
#define OTHER_EMOTIONCONFIG_TITLE		    "title"
#define OTHER_EMOTIONCONFIG_FORLADYLIST	    "face_senior_forlady"

/*6.4 收集手机硬件信息*/
//path
#define PHONE_INFO_PATH                     "/lady/phoneinfo"

//请求
#define OTHER_REQUEST_MODEL			        "model"
#define OTHER_REQUEST_MANUFACT		        "manufacturer"
#define OTHER_REQUEST_OS			        "os"
#define OTHER_REQUEST_RELEASE		        "release"
#define OTHER_REQUEST_SDK			        "sdk"
#define OTHER_REQUEST_DENSITYDPI	        "densityDpi"
#define OTHER_REQUEST_WIDTH			        "width"
#define OTHER_REQUEST_HEIGHT		        "height"
#define OTHER_REQUEST_DATA			        "data"
#define OTHER_REQUEST_VERNAME		        "versionName"
#define OTHER_REQUEST_LANGUAGE		        "language"
#define OTHER_REQUEST_COUNTRY		        "country"
#define OTHER_REQUEST_SITEID		        "siteid"
#define OTHER_REQUEST_ACTION		        "action"
#define OTHER_REQUEST_DEVICEID		        "device_id"

/*6.5 检测客户端更新*/
//path
#define CHECK_VERSION_PATH                  "/lady/version_check"

//请求
#define CHECK_VERSION_APKVERSION			"apk_version"
#define CHECK_VERSION_SDKVERSION		    "sdk_version"

//返回参数
#define CHECK_VERSION_APKVERSION_CODE	    "apk_version_code"
#define CHECK_VERSION_SDKVERSION_NAME		"apk_version_name"
#define CHECK_VERSION_APKURL			    "apk_url"

/*6.6 同步配置*/
//path
#define SYN_CONFIG_PATH                     "/lady/syn_config"

//返回参数
#define SYN_CONFIG_SOCKET_HOST              "socket_host"
#define SYN_CONFIG_SOCKET_PORT              "socket_port"
#define SYN_CONFIG_SOCKET_VERSION           "socket_version"
#define SYN_CONFIG_SOCKET_FROMID            "socket_from_id"
#define SYN_CONFIG_TRANSLATE_URL            "translate_url"
#define SYN_CONFIG_TRANSLATE_LANGUAGES      "translate_languages"
#define SYN_CONFIG_APK_VERSIONCODE          "android_apk_version_code"
#define SYN_CONFIG_APK_VERSIONNAME          "android_apk_version_name"
#define SYN_CONFIG_APK_VERSIONURL           "android_apk_url"
#define SYN_CONFIG_SITE_URL                 "site_url"


/*6.7 提交crash dump文件*/
//path
#define UPLOAD_CRASHLOG_PATH                "/lady/crash_file"

//请求
#define UPLOAD_CRASHLOG_DEVICEID            "deviceId"
#define UPLOAD_CRASHLOG_CRASHFILE           "crashfile"



#endif/*REQUESTOTHERDEFINE_H*/
