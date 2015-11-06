/*
 * RequestDefine.h
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#ifndef REQUESTDEFINE_H_
#define REQUESTDEFINE_H_

/* 本地错误代码 */
#define LOCAL_ERROR_CODE_TIMEOUT			"LOCAL_ERROR_CODE_TIMEOUT"
#define LOCAL_ERROR_CODE_TIMEOUT_DESC		"Trouble connecting to the server, please try again later."
#define LOCAL_ERROR_CODE_PARSEFAIL			"LOCAL_ERROR_CODE_PARSEFAIL"
#define LOCAL_ERROR_CODE_PARSEFAIL_DESC		"Trouble connecting to the server, please try again later."
#define LOCAL_ERROR_CODE_FILEOPTFAIL		"LOCAL_ERROR_CODE_FILEOPTFAIL"
#define LOCAL_ERROR_CODE_FILEOPTFAIL_DESC	"Error encountered while writing your file storage."

/**
 * 服务器错误码
 */
/* 未登录 */
#define ERROR_CODE_MBCE0003			"0003"

/* 公共字段 */
#define COMMON_RESULT 		"result"
#define COMMON_ERRNO 		"errno"
#define COMMON_ERRMSG 		"errmsg"
#define COMMON_ERRDATA		"errdata"
#define COMMON_EXT			"ext"
#define COMMON_DATA			"data"
#define COMMON_DATA_COUNT	"dataCount"
#define COMMON_DATA_LIST	"datalist"

#define COMMON_PAGE_INDEX 	"page_index"
#define COMMON_PAGE_SIZE 	"page_size"

#define	COMMON_SID			"sid"
#define	COMMON_SESSION_ID	"sessionid"
#define COMMON_VERSION_CODE	"versioncode"

/* for xml */
#define COMMON_ROOT			"ROOT"
#define COMMON_RESULT		"result"
#define COMMON_STATUS		"status"
#define COMMON_ERRCODE		"errcode"
#define COMMON_ERRMSG		"errmsg"
#define COMMON_INFO			"info"

/* 站点类型定义 */
#define OTHER_SYNCONFIG_CL				"0"
#define OTHER_SYNCONFIG_IDA				"1"
#define OTHER_SYNCONFIG_CD				"4"
#define OTHER_SYNCONFIG_LA				"5"
typedef enum {
	OTHER_SITE_ALL = 0,		// All
	OTHER_SITE_CL = 1,		// ChnLove
	OTHER_SITE_IDA = 2,		// iDateAsia
	OTHER_SITE_CD = 4,		// CharmingDate
	OTHER_SITE_LA = 8,		// LatamDate
	OTHER_SITE_UNKNOW = OTHER_SITE_ALL,	// Unknow
} OTHER_SITE_TYPE;
inline const char* GetSiteId(OTHER_SITE_TYPE siteType) {
	const char* siteId = "";
	switch (siteType) {
	case OTHER_SITE_CL:
		siteId = OTHER_SYNCONFIG_CL;
		break;
	case OTHER_SITE_IDA:
		siteId = OTHER_SYNCONFIG_IDA;
		break;
	case OTHER_SITE_CD:
		siteId = OTHER_SYNCONFIG_CD;
		break;
	case OTHER_SITE_LA:
		siteId = OTHER_SYNCONFIG_LA;
		break;
	}
	return siteId;
};

#endif /* REQUESTDEFINE_H_ */
