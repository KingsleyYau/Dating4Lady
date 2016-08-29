/*
 * RequestAlbumDefine.h
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 */

#ifndef RequestAlbumDefine_H_
#define RequestAlbumDefine_H_

typedef enum{
	ALBUM_UNKNOWN = -1,
	ALBUM_PHOTO = 0,
	ALBUM_VIDEO = 1
}ALBUMTYPE;

static const int AlbumType[] = {
	ALBUM_UNKNOWN,
	ALBUM_PHOTO,
	ALBUM_VIDEO
};

// 协议值转类型
ALBUMTYPE IntToAlbumType(int value);
// 类型转协议值
int AlbumTypeToInt(ALBUMTYPE type);

/**************************** 4.1 查询Album列表 ******************************/
#define ALBUM_GET_LIST				"/lady/albumlist"

//parse param
#define ALBUM_GET_LIST_ID           "id"
#define ALBUM_GET_LIST_TYPE         "type"
#define ALBUM_GET_LIST_TITLE        "title"
#define ALBUM_GET_LIST_DESC         "desc"
#define ALBUM_GET_LIST_IMAGEURL     "imageurl"
#define ALBUM_GET_LIST_COUNT        "count"
#define ALBUM_GET_LIST_CREATETIME   "createtime"

/**************************** 4.2 创建Album ******************************/
#define ALBUM_CREATE_ALBUM			"/lady/albumcreate"

//agrv
#define ALBUM_ALBUM_TYPE            "type"
#define ALBUM_ALBUM_TITLE           "title"
#define ALBUM_ALBUM_DESC            "desc"

//parse param
#define ALBUM_ALBUM_ID           	"id"

/**************************** 4.3 修改Album ******************************/
#define ALBUM_EDIT_ALBUM			"/lady/albummodify"

/**************************** 4.4 删除Album ******************************/
#define ALBUM_DELETE_ALBUM			"/lady/albumremove"

typedef enum{
	UNKNOWN,
	AGENT_REVIEWING,
	ASIA_MEDIA_REVIEWING,
	REVIEWED,
	REJESTED,
	REVISED
}REVIEWSTATUS;

static const int ReviewStatus[] = {
	UNKNOWN,
	AGENT_REVIEWING,
	ASIA_MEDIA_REVIEWING,
	REVIEWED,
	REJESTED,
	REVISED
};

// 类型转协议值
int ReviewStatusToInt(REVIEWSTATUS type);


//Photo review reason
typedef enum{
	PHOTO_REASON_OTHERS = 120,
	PHOTO_REASON_NON_SELF = 1,
	PHOTO_REASON_SIMILAR_PHOTO = 2,
	PHOTO_REASON_FACIAL_BLUR = 3,
	PHOTO_REASON_PHOTO_UPSIDE_DOWN = 4,
	PHOTO_REASON_APPEARANCE_NOT_MATCH = 5,
	PHOTO_REASON_FACE_PROFILEPHOTO_NOMATCH = 6,
	PHOTO_REASON_PHOTO_CONTAIN_TEXTORWATERMARK = 7,
	PHOTO_REASON_REVISED_DESC_NOSTANDARD = PHOTO_REASON_OTHERS + 1,
	PHOTO_REASON_REVISED_COVER_NOSTANDARD = PHOTO_REASON_OTHERS + 2,
	PHOTO_REASON_REVISED_COVERANDDESC_NOSTANDARD = PHOTO_REASON_OTHERS + 3
}PHOTOREVIEWREASON;

static const int PhotoReviewReason[] = {
	PHOTO_REASON_OTHERS,
	PHOTO_REASON_NON_SELF,
	PHOTO_REASON_SIMILAR_PHOTO,
	PHOTO_REASON_FACIAL_BLUR,
	PHOTO_REASON_PHOTO_UPSIDE_DOWN,
	PHOTO_REASON_APPEARANCE_NOT_MATCH,
	PHOTO_REASON_FACE_PROFILEPHOTO_NOMATCH,
	PHOTO_REASON_PHOTO_CONTAIN_TEXTORWATERMARK,
	PHOTO_REASON_REVISED_DESC_NOSTANDARD,
	PHOTO_REASON_REVISED_COVER_NOSTANDARD,
	PHOTO_REASON_REVISED_COVERANDDESC_NOSTANDARD
};
// 类型转协议值
int PhotoReviewReasonToInt(PHOTOREVIEWREASON type);


//Video review reason
typedef enum{
	VIDEO_REASON_OTHERS = 120,
	VIDEO_REASON_NON_SELF = 1,
	VIDEO_REASON_SIMILAR_VIDEOSHOW = 2,
	VIDEO_REASON_FACIAL_BLUR = 3,
	VIDEO_REASON_VIDEO_UPSIDE_DOWN = 4,
	VIDEO_REASON_APPEARANCE_NOT_MATCH = 5,
	VIDEO_REASON_FACE_PHOTO_NOMATCH = 6,
	VIDEO_REASON_EXIST_SIMILAR_SHORTVIDEO = 7,
	VIDEO_REASON_VIDEO_TOO_SHORT = 8,
	VIDEO_REASON_VIDEO_PIX_NOMATCH = 9,
	VIDEO_REASON_VIDEO_BLUR = 10,
	VIDEO_REASON_VIDEO_VOICE_NOMATCH = 11,
	VIDEO_REASON_VIDEO_CONTAIN_TEXTORWATERMARK = 12,
	VIDEO_REASON_VIDEO_CONTAIN_CONTACTINFO = 13,
	VIDEO_REASON_VIDEO_PROFILE_NOMATCH = 14,
	VIDEO_REASON_VIDEO_SITE_NOMATCH = 15,
	VIDEO_REASON_REVISED_DESC_NOSTANDARD = VIDEO_REASON_OTHERS + 1,
	VIDEO_REASON_REVISED_COVER_NOSTANDARD = VIDEO_REASON_OTHERS + 2,
	VIDEO_REASON_REVISED_COVERANDDESC_NOSTANDARD = VIDEO_REASON_OTHERS + 3
}VIDEOREVIEWREASON;

static const int VideoReviewReason[] = {
	VIDEO_REASON_OTHERS,
	VIDEO_REASON_NON_SELF,
	VIDEO_REASON_SIMILAR_VIDEOSHOW,
	VIDEO_REASON_FACIAL_BLUR,
	VIDEO_REASON_VIDEO_UPSIDE_DOWN,
	VIDEO_REASON_APPEARANCE_NOT_MATCH,
	VIDEO_REASON_FACE_PHOTO_NOMATCH,
	VIDEO_REASON_EXIST_SIMILAR_SHORTVIDEO,
	VIDEO_REASON_VIDEO_TOO_SHORT,
	VIDEO_REASON_VIDEO_PIX_NOMATCH,
	VIDEO_REASON_VIDEO_BLUR,
	VIDEO_REASON_VIDEO_VOICE_NOMATCH,
	VIDEO_REASON_VIDEO_CONTAIN_TEXTORWATERMARK,
	VIDEO_REASON_VIDEO_CONTAIN_CONTACTINFO,
	VIDEO_REASON_VIDEO_PROFILE_NOMATCH,
	VIDEO_REASON_VIDEO_SITE_NOMATCH,
	VIDEO_REASON_REVISED_DESC_NOSTANDARD,
	VIDEO_REASON_REVISED_COVER_NOSTANDARD,
	VIDEO_REASON_REVISED_COVERANDDESC_NOSTANDARD
};
// 类型转协议值
int VideoReviewReasonToInt(VIDEOREVIEWREASON type);

//Video
typedef enum{
	PROCESS_STATUS_UNKNOWN = -1,
	WAITING_FOR_TRANSCODING = 0,
	TRANSCODED_ADN_DOWNLOAD = 1,
	ERROE_AFTER_DOWNLOAD = 7,
	VIDEO_PROCESS_FAILED = 8
}VIDEOPROCESSSTATUS;

static const int VideoProcessStatus[] = {
	PROCESS_STATUS_UNKNOWN,
	WAITING_FOR_TRANSCODING,
	TRANSCODED_ADN_DOWNLOAD,
	ERROE_AFTER_DOWNLOAD,
	VIDEO_PROCESS_FAILED
};

// 类型转协议值
int VideoProcessStatusToInt(VIDEOPROCESSSTATUS type);

/**************************** 4.5 查询Photo列表******************************/
#define ALBUM_PHOTO_LIST			"/lady/albumphotolist"

//parse param
#define ALBUM_PHOTO_ID           	"id"
#define ALBUM_PHOTO_TITLE           "title"
#define ALBUM_PHOTO_THUMBURL        "thumburl"
#define ALBUM_PHOTO_URL           	"url"
#define ALBUM_PHOTO_REVIEWSTATUS    "reviewstatus"
#define ALBUM_PHOTO_REVIEWREASON    "reviewreason"

/**************************** 4.6 添加Photo ******************************/
#define ALBUM_ADD_PHOTO_URL			"/lady/albumaddphoto"

//agrv
#define ALBUM_ADD_PHOTO_SRCFILE     "srcfile"

/**************************** 4.7 修改Photo ******************************/
#define ALBUM_EDIT_PHOTO_URL		"/lady/albummodifyphoto"

/**************************** 4.8 查询Video列表 ******************************/
#define ALBUM_VIDEO_LIST_URL		"/lady/videolist"

//parsing
#define ALBUM_VIDEO_ID              "id"
#define ALBUM_VIDEO_TITLE			"title"
#define ALBUM_VIDEO_THUMBURL        "thumburl"
#define ALBUM_VIDEO_PRIVIEWURL      "previewurl"
#define ALBUM_VIDEO_URL             "url"
#define ALBUM_VIDEO_HANDLECODE      "handlecode"
#define ALBUM_VIDEO_REVIEWSTATUS    "reviewstatus"
#define ALBUM_VIDEO_REVIEWREASON    "reviewreason"

/**************************** 4.9 上传Video文件 ******************************/
#define ALBUM_UPLOAD_VIDEO_URL		"/FileUpload/ShortVideoServlet.htm"

//argv
#define ALBUM_UPLOAD_VIDEO_NUM      "num"
#define ALBUM_UPLOAD_VIDEO_AGENCY   "agencyID"
#define ALBUM_UPLOAD_VIDEO_WOMANID  "womanID"
#define ALBUM_UPLOAD_VIDEO_SITEID   "siteID"
#define ALBUM_UPLOAD_VIDEO_SERVER   "serverType"
#define ALBUM_UPLOAD_VIDEO_FILEDATA "Filedata"

/**************************** 4.10 添加Video ******************************/
#define ALBUM_ADD_VIDEO_URL			"/lady/albumaddvideo"

//argv
#define ALBUM_ADD_VIDEO_SHORTKEY    "short_video_key"
#define ALBUM_ADD_VIDEO_FILEMD5ID   "hidFileMd5ID"

//agrv
#define ALBUM_ADD_VIDEO_SRCFILE     "srcfile"
#define ALBUM_ADD_VIDEO_THUMBFILE   "thumbfile"

/**************************** 4.10 修改Video ******************************/
#define ALBUM_EDIT_VIDEO_URL		"/lady/albummodifyvideo"

#endif/*RequestAlbumDefine_H_*/
