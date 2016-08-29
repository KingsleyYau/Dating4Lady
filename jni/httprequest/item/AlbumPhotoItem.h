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
		reviewReason = PHOTO_REASON_OTHERS;
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
			if(root[ALBUM_PHOTO_REVIEWREASON].isString()){
				string reviewReasonStr = root[ALBUM_PHOTO_REVIEWREASON].asString();
				if(reviewReasonStr == "A"){
					reviewReason =  PHOTO_REASON_NON_SELF;
				}else if(reviewReasonStr == "B"){
					reviewReason =  PHOTO_REASON_SIMILAR_PHOTO;
				}else if(reviewReasonStr == "C"){
					reviewReason =  PHOTO_REASON_FACIAL_BLUR;
				}else if(reviewReasonStr == "D"){
					reviewReason =  PHOTO_REASON_PHOTO_UPSIDE_DOWN;
				}else if(reviewReasonStr == "E"){
					reviewReason =  PHOTO_REASON_APPEARANCE_NOT_MATCH;
				}else if(reviewReasonStr == "F"){
					reviewReason =  PHOTO_REASON_FACE_PROFILEPHOTO_NOMATCH;
				}else if(reviewReasonStr == "G"){
					reviewReason =  PHOTO_REASON_PHOTO_CONTAIN_TEXTORWATERMARK;
				}else if(reviewReasonStr == "1"){
					reviewReason =  PHOTO_REASON_REVISED_DESC_NOSTANDARD;
				}else if(reviewReasonStr == "2"){
					reviewReason =  PHOTO_REASON_REVISED_COVER_NOSTANDARD;
				}else if(reviewReasonStr == "3"){
					reviewReason =  PHOTO_REASON_REVISED_COVERANDDESC_NOSTANDARD;
				}else{
					reviewReason =  PHOTO_REASON_OTHERS;
				}
			}
		}
	}

public:
	string photoId;
	string photoTitle;
	string photoThumbUrl;
	string photoUrl;
	REVIEWSTATUS reviewStatus;
	PHOTOREVIEWREASON reviewReason;
};

#endif/*_ALBUMPHOTOITEM_H*/
