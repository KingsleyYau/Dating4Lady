/* AlbumPhotoItem.h
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 *  Description: 4.5 相册中单张图片信息
 */
#ifndef _ALBUMPHOTOITEM_H
#define _ALBUMPHOTOITEM_H

#include "../RequestAlbumDefine.h"

class AlbumPhotoItem{
public:
	AlbumPhotoItem(){
		photoId = "";
		photoTitle = "";
		photoThumbUrl = "";
		photoUrl = "";
		reviewStatus = UNKNOWN;
		reviewReason = REASON_OTHERS;
	}
	~AlbumPhotoItem(){

	}

public:
	void Parse(const Json::Value& root){
		if( root.isObject()){
			if(root[ALBUM_PHOTO_ID].isString()){
				photoId = root[ALBUM_PHOTO_ID].asString();
			}
			if(root[ALBUM_PHOTO_TITLE].isString()){
				photoTitle = root[ALBUM_PHOTO_TITLE].asString();
			}
			if(root[ALBUM_PHOTO_THUMBURL].isString()){
				photoThumbUrl = root[ALBUM_PHOTO_THUMBURL].asString();
			}
			if(root[ALBUM_PHOTO_URL].isString()){
				photoUrl = root[ALBUM_PHOTO_URL].asString();
			}
			if(root[ALBUM_PHOTO_REVIEWSTATUS].isString()){
				string reviewStatusStr = root[ALBUM_PHOTO_REVIEWSTATUS].asString();
				if(reviewStatusStr == "P"){
					reviewStatus = AGENT_REVIEWING;
				}else if(reviewStatusStr == "E"){
					reviewStatus = ASIA_MEDIA_REVIEWING;
				}else if(reviewStatusStr == "Y"){
					reviewStatus = REVIEWED;
				}else if(reviewStatusStr == "N"){
					reviewStatus = REJESTED;
				}else if(reviewStatusStr == "D"){
					reviewStatus = REVISED;
				}
			}
			if(root[ALBUM_PHOTO_REVIEWREASON].isInt()){
				int temp = REASON_OTHERS;
				if(reviewStatus == REVISED){
					//打回时在拒绝基础上处理
					temp = REASON_OTHERS + root[ALBUM_PHOTO_REVIEWREASON].asInt();
				}else if(reviewStatus == REJESTED){
					temp = root[ALBUM_PHOTO_REVIEWREASON].asInt();
				}
				reviewReason = ((REASON_NON_SELF <= temp && temp <= REASON_VIDEO_TOO_SHORT)
						||(REASON_REVISED_DESC_NOSTANDARD <= temp && temp <= REASON_REVISED_COVERANDDESC_NOSTANDARD)
						? (REVIEWREASON)temp : REASON_OTHERS);
			}
		}
	}

public:
	string photoId;
	string photoTitle;
	string photoThumbUrl;
	string photoUrl;
	REVIEWSTATUS reviewStatus;
	REVIEWREASON reviewReason;
};

#endif/*_ALBUMPHOTOITEM_H*/
