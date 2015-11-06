/*
 * RequestLiveChatDefine.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 */

#ifndef REQUESTLIVECHATDEFINE_H_
#define REQUESTLIVECHATDEFINE_H_

#include "RequestDefine.h"

/* ########################	LiveChat相关 模块  ######################## */
/* ########################	查询个人邀请模板列表 ######################## */
/* 接口路径  */
#define LC_INVITETEMPLATE_PATH			"/lady/invitetemplatelist"

/**
 * 返回参数
 */
#define	LC_INVITETEMPLATE_TEMPID			"temp_id"			// 模板id
#define	LC_INVITETEMPLATE_TEMPCONTENT		"temp_content"		// 模板内容
#define	LC_INVITETEMPLATE_STATUS			"status"			// 审核状态

// 模板状态
typedef enum {
	TEMPSTATUS_WAIT_APPROVE = 0,	// 待审核
	TEMPSTATUS_APPROVE = 1,			// 已通过
	TEMPSTATUS_NOT_APPROVE = 2,		// 已否决
	TEMPSTATUS_BEGIN = TEMPSTATUS_WAIT_APPROVE,		// 起始值
	TEMPSTATUS_END = TEMPSTATUS_WAIT_APPROVE,		// 结束值
	TEMPSTATUS_DEFAULT = TEMPSTATUS_WAIT_APPROVE,	// 默认值
} TEMP_STATUS;
inline TEMP_STATUS GetTempStatusWithInt(int status) {
	return (TEMPSTATUS_BEGIN <= status && status <= TEMPSTATUS_END) ? (TEMP_STATUS)status : TEMPSTATUS_DEFAULT;
}

/* ########################	查询系统邀请模板列表   ######################## */
/* 接口路径  */
#define LC_SYSTEMINVITETEMPLATE_PATH	"/lady/invitetemplatedefault"

/* ########################	新建邀请模板  ######################## */
/* 接口路径  */
#define LC_ADDINVITETEMPLATE_PATH 		"/lady/addinvitetemplate"

/**
 * 请求参数
 */
#define	LC_ADDINVITETEMPLATE_CONTENT		"temp_content"		// 模板内容

/* ########################	删除自定义模板  ######################## */
/* 接口路径  */
#define LC_DELCUSTOMTEMPLATE_PATH 		"/lady/delinvitetemplate"

/**
 * 请求参数
 */
#define	LC_DELCUSTOMTEMPLAT_TEMPIDS		"temp_ids"		// 模板ids


/* ########################	查询男士聊天历史 ######################## */
/* 接口路径  */
#define LC_LADYCHATLIST_PATH 			"/lady/ladychatlist"

/**
 * 请求参数
 */
#define	LC_LADYCHATLIST_MANID				"man_id"			// 男士id

/**
 * 返回参数
 */
#define	LC_LADYCHATLIST_INVITEID			"invite_id"			// 邀请id
#define LC_LADYCHATLIST_STARTTIME			"start_time"		// 会话开始时间
#define LC_LADYCHATLIST_DURINGTIME			"during_time"		// 会话时长
#define LC_LADYCHATLIST_MANID				"man_id"			// 男士id
#define LC_LADYCHATLIST_MANNAME				"man_name"			// 男士名
#define LC_LADYCHATLIST_WOMANNAME			"woman_name"		// 女士英文名
#define LC_LADYCHATLIST_CNNAME				"cnname"			// 女士名
#define LC_LADYCHATLIST_TRANSLATORID		"translator_id"		// 翻译id
#define LC_LADYCHATLIST_TRANSLATORNAME		"translator_name"	// 翻译名

/* ########################	查询聊天消息列表  ######################## */
/* 接口路径  */
#define LC_LADYINVITEMSG_PATH 	"/lady/ladyinvitemsg"

/**
 * 请求参数
 */
#define	LC_LADYINVITEMSG_INVITEID			"inviteid"			// 邀请id

/**
 * 返回参数
 */
#define	LC_LADYINVITEMSG_DBTIME				"dbtime"			// 当前数据库时间
#define LC_LADYINVITEMSG_INVITEMSG			"invitemsg"			// 聊天记录(数组)
#define LC_LADYINVITEMSG_TOFLAG				"toflag"			// 是否发送
#define LC_LADYINVITEMSG_MSG				"msg"				// 聊天内容
#define LC_LADYINVITEMSG_MSGTR				"msg_tr"			// 聊天翻译内容
#define LC_LADYINVITEMSG_READED				"readed"			// 是否已读
#define LC_LADYINVITEMSG_ADDTIME			"add_time"			// 聊天记录生成时间
#define LC_LADYINVITEMSG_MSGTYPE			"msgtype"			// 消息类型
// voice id参数分隔符
#define LIVECHAT_VOICEID_DELIMIT	"-"
// video参数分隔符
#define LIVECHAT_VIDEO_DELIMIT		"|||"
// img参数分隔符
#define LIVECHAT_PHOTO_DELIMIT		"|||"
// 是否已扣费值定义
#define LIVECHAT_CHARGE_YES			"1"			// 是
#define LIVECHAT_CHARGE_NO			"0"			// 否

// 是否发送（发送类型）
typedef enum {
	MSGSENDTYPE_RECV = 0,	// 接收
	MSGSENDTYPE_SEND = 1,	// 发送
} MSG_SEND_TYPE;

// 是否已读（已读类型）
typedef enum {
	MSGREADTYPE_UNREAD = 0,	// 未读
	MSGREADTYPE_READ = 1,	// 已读
} MSG_READ_TYPE;

// 聊天记录协议消息类型
typedef enum {
	LIPM_TEXT = 0,		// 文本消息
	LIPM_INVITE = 1,	// 邀请消息（文本）
	LIPM_COUPON = 3,	// 优惠券（文本）
	LIPM_WARNING = 2,	// 警告消息
	LIPM_EMOTION = 4,	// 高级表情消息
	LIPM_AUTO_INVITE = 6,// 自动邀请（文本）
	LIPM_VOICE = 7,		// 语音消息
	LIPM_PHOTO = 8,		// 图片消息
	LIPM_VIDEO = 9,		// 微视频
	LIPM_MAGIC_ICON = 10, // Magic Icon
} LIVECHAT_INVITEMSG_PROTOCOL_MSGTYPE;
// 聊天记录消息内部定义类型
typedef enum {
	LIM_UNKNOW = -1,
	LIM_TEXT = 0,
	LIM_INVITE,
	LIM_WARNING,
	LIM_EMOTION,
	LIM_VOICE,
	LIM_PHOTO,
	LIM_VIDEO,
} LIVECHAT_INVITEMSG_MSGTYPE;

/* ########################	获取私密照列表  ######################## */
/* 接口路径  */
#define LC_LADYGETPHOTOLIST_PATH 	"/livechat/setstatus.php?action=lady_get_photolist"

/**
 * 返回参数
 */
#define	LC_LADYGETPHOTOLIST_ALBUM				"album"			// 相册（数组）
#define	LC_LADYGETPHOTOLIST_ALBUMID				"albumid"		// 相册id
#define	LC_LADYGETPHOTOLIST_PHOTO				"photo"			// 照片（数组）
#define	LC_LADYGETPHOTOLIST_PHOTOID				"photoid"		// 照片id
#define	LC_LADYGETPHOTOLIST_LIST				"list"			// 数组item标识
#define	LC_LADYGETPHOTOLIST_TITLE				"title"			// 标题

/* ######################## 发送私密照片 ######################## */
/* 接口路径  */
#define LC_LADYSENDPHOTO_PATH 	"/livechat/setstatus.php?action=lady_send_photo"

/**
 * 请求参数
 */
#define	LC_LADYSENDPHOTO_TARGETID				"targetid"		// 对方id
#define	LC_LADYSENDPHOTO_INVITEID				"inviteid"		// 邀请id
#define LC_LADYSENDPHOTO_PHOTOID				"photoid"		// 照片id

/**
 * 返回参数
 */
#define LC_LADYSENDPHOTO_SENDID					"sendId"		// 发送id

/* ######################## 检测女士是否可发私密照 ######################## */
/* 接口路径  */
#define LC_LADYCHECKPHOTO_PATH 	"/livechat/setstatus.php?action=lady_check_photo"

/**
 * 请求参数
 */
#define	LC_LADYCHECKPHOTO_TARGETID				"targetid"		// 对方id
#define	LC_LADYCHECKPHOTO_INVITEID				"inviteid"		// 邀请id
#define LC_LADYCHECKPHOTO_PHOTOID				"photoid"		// 照片id

/* ######################## 获取对方私密照片 ######################## */
/* 接口路径  */
#define LC_GETPHOTO_PATH 		"/livechat/setstatus.php?action=load_private_photo"

/**
 * 请求参数
 */
#define	LC_GETPHOTO_TOFLAG					"toflag"		// 获取类型
#define	LC_GETPHOTO_TARGETID				"inviteid"		// 对方id
#define LC_GETPHOTO_USERSID					"user_sid"		// 登录成功返回的sessionid
#define LC_GETPHOTO_USERID					"user_id"		// 登录成功返回的ladyid
#define LC_GETPHOTO_PHOTOID					"photoid"		// 照片id
#define LC_GETPHOTO_SIZE					"size"			// 照片尺寸
#define LC_GETPHOTO_MODE					"mode"			// 照片清晰度

// 获取类型
typedef enum {
	GETPHOTO_TOFLAGTYPE_WOMANGETMAN = 0,		// 女士获取男士
	GETPHOTO_TOFLAGTYPE_MANGETWOMAN = 1,		// 男士获取女士
	GETPHOTO_TOFLAGTYPE_WOMANGETSELF = 2,		// 女士获取自己
	GETPHOTO_TOFLAGTYPE_MANGETSELF = 3,			// 男士获取自己
} GETPHOTO_TOFLAG_TYPE;

// 照片尺寸
static const char* GETPHOTO_PHOTOSIZE_PROTOCOL[] = {
	"l",	// 大
	"m",	// 中
	"s",	// 小
	"o",	// 原始
};
typedef enum {
	GETPHOTO_SIZETYPE_LARGE = 0,	// 大
	GETPHOTO_SIZETYPE_MIDDLE,		// 中
	GETPHOTO_SIZETYPE_SMALL,		// 小
	GETPHOTO_SIZETYPE_ORIGINAL,		// 原始
	GETPHOTO_SIZETYPE_BEGIN = GETPHOTO_SIZETYPE_LARGE,	// 起始值
	GETPHOTO_SIZETYPE_END = GETPHOTO_SIZETYPE_ORIGINAL,	// 结束值
} GETPHOTO_SIZE_TYPE;

// 照片清晰度类型
typedef enum {
	GETPHOTO_MODETYPE_FUZZY = 0,		// 模糊
	GETPHOTO_MODETYPE_CLEAR = 1,		// 清晰
} GETPHOTO_MODE_TYPE;

/* ######################## 上传语音文件 ######################## */
/* 接口路径  */
#define LC_UPLOADVOICE_PATH		"/livechat/voice?r=%s"

/**
 * 请求参数
 */
#define LC_UPLOADVOICE_INVITEID					"iv"			// 语音验证码
#define LC_UPLOADVOICE_SEX						"s"				// 自己的性别
#define LC_UPLOADVOICE_SEX_WOMAN					"0"			// 女
#define LC_UPLOADVOICE_SEX_MAN						"1"			// 男
#define LC_UPLOADVOICE_MANID					"mid"			// 男士id
#define LC_UPLOADVOICE_WOMANID					"wid"			// 女士id
#define LC_UPLOADVOICE_FILETYPE					"type"			// 文件类型
#define LC_UPLOADVOICE_VOICELENGTH				"l"				// 语音时长（秒）
#define LC_UPLOADVOICE_SITEID					"dt"			// 站点id
#define LC_UPLOADVOICE_VOICEFILE				"voicefile"		// 语音文件

/* ######################## 获取语音文件 ######################## */
/* 接口路径  */
#define LC_PLAYVOICE_PATH			"/livechat/play?r=%s&t=%s&a=wp"
#define LC_PLAYVOICE_SUBPATH			"/livechat/play?"

#endif /* REQUESTLIVECHATDEFINE_H_ */
