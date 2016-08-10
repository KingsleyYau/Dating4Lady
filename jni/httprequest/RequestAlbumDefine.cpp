/*
 * RequestAlbumDefine.cpp
 *
 *  Created on: 2016-07-18
 *      Author: Hunter.Mun
 * Description: 处理Album数值与类型之间的转换
 */

#include "RequestAlbumDefine.h"
#include <common/CommonFunc.h>

ALBUMTYPE IntToAlbumType(int value){
	return (ALBUMTYPE)(value < _countof(AlbumType) ? AlbumType[value] : AlbumType[0]);
}

int AlbumTypeToInt(ALBUMTYPE type){
	int value = 0;
	for(int i=0; i<_countof(AlbumType); i++){
		if(type == AlbumType[i]){
			value = i;
			break;
		}
	}
	return value;
}


int ReviewStatusToInt(REVIEWSTATUS type){
	int value = 0;
	for(int i=0; i<_countof(ReviewStatus); i++){
		if(type == ReviewStatus[i]){
			value = i;
			break;
		}
	}
	return value;
}

int ReviewReasonToInt(REVIEWREASON type){
	int value = 0;
	for(int i=0; i<_countof(ReviewReason); i++){
		if(type == ReviewReason[i]){
			value = i;
			break;
		}
	}
	return value;
}

int VideoProcessStatusToInt(VIDEOPROCESSSTATUS type){
	int value = 0;
	for(int i=0; i<_countof(VideoProcessStatus); i++){
		if(type == VideoProcessStatus[i]){
			value = i;
			break;
		}
	}
	return value;
}
