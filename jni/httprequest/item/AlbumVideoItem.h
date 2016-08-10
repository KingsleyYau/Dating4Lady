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
		reviewReason = REASON_OTHERS;
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
			if(root[ALBUM_VIDEO_REVIEWREASON].isInt()){
				int temp = REASON_OTHERS;
				if(reviewStatus == REVISED){
					//打回时在拒绝基础上处理
					temp = REASON_OTHERS + root[ALBUM_VIDEO_REVIEWREASON].asInt();
				}else if(reviewStatus == REJESTED){
					temp = root[ALBUM_VIDEO_REVIEWREASON].asInt();
				}
				reviewReason = ((REASON_NON_SELF <= temp && temp <= REASON_VIDEO_TOO_SHORT)
						||(REASON_REVISED_DESC_NOSTANDARD <= temp && temp <= REASON_REVISED_COVERANDDESC_NOSTANDARD)
						? (REVIEWREASON)temp : REASON_OTHERS);
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
	REVIEWREASON reviewReason;
};

#endif/*_ALBUMVIDEOITEM_H*/
