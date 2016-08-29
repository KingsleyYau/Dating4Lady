/* AlbumVideoItem.h
 *
 *  Created on: 2016.7.19
 *      Author: Hunter.Mun
 *  Description: 4.5 相册中单个视频信息
 */
#ifndef _ALBUMVIDEOITEM_H
#define _ALBUMVIDEOITEM_H

#include "../RequestAlbumDefine.h"

class AlbumVideoItem{
public:
	AlbumVideoItem(){
		videoId = "";
		videoTitle = "";
		videoThumbUrl = "";
		videoPreviewUrl = "";
		videoUrl = "";
		videoProcessStatus = PROCESS_STATUS_UNKNOWN;
		reviewStatus = UNKNOWN;
		reviewReason = VIDEO_REASON_OTHERS;
	}
	~AlbumVideoItem(){

	}
public:
	void Parse(const Json::Value& root){
		if( root.isObject()){
			if(root[ALBUM_VIDEO_ID].isString()){
				videoId = root[ALBUM_VIDEO_ID].asString();
			}
			if(root[ALBUM_VIDEO_TITLE].isString()){
				videoTitle = root[ALBUM_VIDEO_TITLE].asString();
			}
			if(root[ALBUM_VIDEO_THUMBURL].isString()){
				videoThumbUrl = root[ALBUM_VIDEO_THUMBURL].asString();
			}
			if(root[ALBUM_VIDEO_PRIVIEWURL].isString()){
				videoPreviewUrl = root[ALBUM_VIDEO_PRIVIEWURL].asString();
			}
			if(root[ALBUM_VIDEO_URL].isString()){
				videoUrl = root[ALBUM_VIDEO_URL].asString();
			}
			if(root[ALBUM_VIDEO_HANDLECODE].isInt()){
				int processTemp = root[ALBUM_VIDEO_HANDLECODE].asInt();
				if(processTemp == 0){
					videoProcessStatus = WAITING_FOR_TRANSCODING;
				} else if(processTemp == 1){
					videoProcessStatus = TRANSCODED_ADN_DOWNLOAD;
				}else if(processTemp == 7){
					videoProcessStatus = ERROE_AFTER_DOWNLOAD;
				}else if(processTemp == 8){
					videoProcessStatus = VIDEO_PROCESS_FAILED;
				}
			}
			if(root[ALBUM_VIDEO_REVIEWSTATUS].isString()){
				string reviewStatusStr = root[ALBUM_VIDEO_REVIEWSTATUS].asString();
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
			if(root[ALBUM_VIDEO_REVIEWREASON].isString()){
				string reviewReasonStr = root[ALBUM_PHOTO_REVIEWREASON].asString();
				if(reviewStatus == REJESTED){
					if(reviewReasonStr == "1"){
						reviewReason =  VIDEO_REASON_NON_SELF;
					}else if(reviewReasonStr == "2"){
						reviewReason =  VIDEO_REASON_SIMILAR_VIDEOSHOW;
					}else if(reviewReasonStr == "3"){
						reviewReason =  VIDEO_REASON_FACIAL_BLUR;
					}else if(reviewReasonStr == "4"){
						reviewReason =  VIDEO_REASON_VIDEO_UPSIDE_DOWN;
					}else if(reviewReasonStr == "5"){
						reviewReason =  VIDEO_REASON_APPEARANCE_NOT_MATCH;
					}else if(reviewReasonStr == "6"){
						reviewReason =  VIDEO_REASON_FACE_PHOTO_NOMATCH;
					}else if(reviewReasonStr == "7"){
						reviewReason =  VIDEO_REASON_EXIST_SIMILAR_SHORTVIDEO;
					}else if(reviewReasonStr == "8"){
						reviewReason =  VIDEO_REASON_VIDEO_TOO_SHORT;
					}else if(reviewReasonStr == "9"){
						reviewReason =  VIDEO_REASON_VIDEO_PIX_NOMATCH;
					}else if(reviewReasonStr == "10"){
						reviewReason =  VIDEO_REASON_VIDEO_BLUR;
					}else if(reviewReasonStr == "11"){
						reviewReason =  VIDEO_REASON_VIDEO_VOICE_NOMATCH;
					}else if(reviewReasonStr == "12"){
						reviewReason =  VIDEO_REASON_VIDEO_CONTAIN_TEXTORWATERMARK;
					}else if(reviewReasonStr == "13"){
						reviewReason =  VIDEO_REASON_VIDEO_CONTAIN_CONTACTINFO;
					}else if(reviewReasonStr == "14"){
						reviewReason =  VIDEO_REASON_VIDEO_PROFILE_NOMATCH;
					}else if(reviewReasonStr == "15"){
						reviewReason =  VIDEO_REASON_VIDEO_SITE_NOMATCH;
					}
				}else if(reviewStatus == REVISED){
					if(reviewReasonStr == "1"
							|| reviewReasonStr == "0"){
						reviewReason =  VIDEO_REASON_REVISED_DESC_NOSTANDARD;
					}else if(reviewReasonStr == "2"){
						reviewReason =  VIDEO_REASON_REVISED_COVER_NOSTANDARD;
					}else if(reviewReasonStr == "3"){
						reviewReason =  VIDEO_REASON_REVISED_COVERANDDESC_NOSTANDARD;
					}
				}
			}
		}
	}

public:
	string videoId;
	string videoTitle;
	string videoThumbUrl;
	string videoPreviewUrl;
	string videoUrl;
	VIDEOPROCESSSTATUS videoProcessStatus;
	REVIEWSTATUS reviewStatus;
	VIDEOREVIEWREASON reviewReason;
};

#endif/*_ALBUMVIDEOITEM_H*/
