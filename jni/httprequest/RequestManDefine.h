/*
 * RequestManDefine.h
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#ifndef RequestManDefine_H_
#define RequestManDefine_H_

#include "RequestDefine.h"

/* ########################	男士相关 模块  ######################## */
/* ########################	3.1.查询男士列表（http post  ######################## */
/* 接口路径  */
#define MAN_LIST_PATH 	"/lady/mansearch"

/**
 * 请求
 */
#define	MAN_LIST_QUERY_TYPE		"query_type"
#define	MAN_LIST_MAN_ID			"man_id"
#define	MAN_LIST_FROM_AGE		"from_age"
#define	MAN_LIST_TO_AGE			"to_age"
#define	MAN_LIST_COUNTRY		"country"
#define	MAN_LIST_PHOTO			"photo"
#define	MAN_LIST_DATE_TYPE		"date_type"
#define	MAN_LIST_FROM_DATE		"from_date"
#define	MAN_LIST_TO_DATE		"to_date"

/**
 * 返回
 */
#define	MAN_LIST_FIRSTNAME		"firstname"
#define	MAN_LIST_LASTNAME		"lastname"
#define	MAN_LIST_AGE			"age"
#define MAN_LIST_COUNTRY		"country"
#define	MAN_LIST_PROVINCE		"province"
#define	MAN_LIST_PHOTO_URL		"photo_url"
#define	MAN_LIST_PHOTO_STATUS	"photo_status"

typedef enum {
	DEFAULT,
	BYID,
} QUERYTYPE;

// 男士列表图片状态
typedef enum {
	NONE = 0,				// 无相片
	YES = 1,				// 有正式相片
	VERIFING = 2,			// 相片待处理中
	INSTITUTIONSFAIL = 3,	// 机构上报为不合格
	FAIL = 4,				// 我方设为不合格
} PHOTO_STATUS;

/* ########################	3.2.查询男士详情（http post）  ######################## */
/* 接口路径  */
#define MAN_DETAIL_PATH 	"/lady/manprofile"

/**
 * 请求
 */
#define	MAN_DETAIL_MAN_ID				"man_id"

/**
 * 返回
 */
#define	MAN_DETAIL_FIRSTNAME			"firstname"
#define	MAN_DETAIL_LASTNAME				"lastname"
#define MAN_DETAIL_COUNTRY				"country"
#define	MAN_DETAIL_PROVINCE				"province"
#define	MAN_DETAIL_CITY					"city"
#define	MAN_DETAIL_JOIN_DATE			"join_date"
#define	MAN_DETAIL_BIRTHDAY				"birthday"
#define MAN_DETAIL_WEIGHT 				"weight"
#define MAN_DETAIL_WEIGHT_BEGINVALUE	5
#define MAN_DETAIL_HEIGHT				"height"
#define MAN_DETAIL_HEIGHT_BEGINVALUE	12
#define MAN_DETAIL_SMOKE				"smoke"
#define MAN_DETAIL_DRINK				"drink"
#define MAN_DETAIL_LANGUAGE				"language"
#define MAN_DETAIL_RELIGION				"religion"
#define MAN_DETAIL_EDUCATION			"education"
#define MAN_DETAIL_PROFESSION			"profession"
#define MAN_DETAIL_CHILDREN				"children"
#define MAN_DETAIL_MARRY				"marray"
#define MAN_DETAIL_INCOME				"income"
#define MAN_DETAIL_ETHNICITY			"ethnicity"
#define	MAN_DETAIL_ABOUT_ME				"about_me"
#define	MAN_DETAIL_ONLINE				"online"
#define	MAN_DETAIL_FAVORITE				"favorite"
#define	MAN_DETAIL_PHOTO_URL			"photo_url"
#define	MAN_DETAIL_PHOTO_BIG_URL		"photo_big_url"
#define	MAN_DETAIL_PHOTO_STATUS			"photo_status"
#define	MAN_DETAIL_RECEIVE_ADMIRER		"receive_admirer"

/* ########################	3.3.查询已收藏的男士列表（http post）  ######################## */
/* 接口路径  */
#define MAN_FAVOUR_LIST_PATH 	"/lady/favorites"

/**
 * 返回
 */
#define	MAN_FAVOUR_LIST_MAN_IDS			"man_ids"

/* ########################	3.4.收藏男士（http post）  ######################## */
/* 接口路径  */
#define MAN_ADD_FAVOUR_PATH 	"/lady/addfavorite"

/**
 * 请求
 */
#define	MAN_ADD_FAVOUR_MAN_ID				"man_id"

/* ########################	3.5.删除已收藏男士（http post） ######################## */
/* 接口路径  */
#define MAN_REMOVE_FAVOUR_PATH 	"/lady/delfavorite"

/**
 * 请求
 */
#define	MAN_REMOVE_FAVOUR_MAN_IDS				"man_ids"

/* ########################	3.6.获取最近聊天男士列表（http post） ######################## */
/* 接口路径  */
#define MAN_RECENT_CHAT_LIST_PATH 	"/lady/online_history"

/**
 * 请求
 */
typedef enum {
	ALL,
	ONLINE,
} RECENT_CHAT_QUERYTYPE;

#define	MAN_RECENT_CHAT_LIST_QUERY_TYPE				"query_type"

/**
 * 返回
 */
/**
 * 男士在线状态
 */
typedef enum OnlineStatus {
	ONLINESTATUS_ONLINE,
	ONLINESTATUS_HIDDEN,
	ONLINESTATUS_OFFLINE,
} OnlineStatus;

#define	MAN_RECENT_CHAT_LIST_MAN_ID				"man_id"
#define	MAN_RECENT_CHAT_LIST_FIRSTNAME			"firstname"
#define	MAN_RECENT_CHAT_LIST_LASTNAME			"lastname"
#define	MAN_RECENT_CHAT_LIST_AGE				"age"
#define MAN_RECENT_CHAT_LIST_COUNTRY			"country"
#define	MAN_RECENT_CHAT_LIST_PHOTO_URL			"photo_url"
#define	MAN_RECENT_CHAT_LIST_STATUS				"status"
#define	MAN_RECENT_CHAT_LIST_CLIENT_TYPE		"client_type"

/* ########################	3.7.获取最近访客列表（http post） ######################## */
/* 接口路径  */
#define MAN_RECENT_VIEW_LIST_PATH 	"/lady/viewme"

/**
 * 返回
 */
#define	MAN_RECENT_VIEW_LIST_MAN_ID				"man_id"
#define	MAN_RECENT_VIEW_LIST_LAST_TIME			"last_time"

#endif /* RequestManDefine_H_ */
