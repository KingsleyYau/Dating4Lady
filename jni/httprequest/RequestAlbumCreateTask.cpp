/* RequestAlbumCreateTask.cpp
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 *  Description: 4.2 创建Album
 */
#include "RequestAlbumCreateTask.h"

RequestAlbumCreateTask::RequestAlbumCreateTask(){
	mUrl = ALBUM_CREATE_ALBUM;
}

RequestAlbumCreateTask::~RequestAlbumCreateTask(){

}

void RequestAlbumCreateTask::SetCallback(IRequestAlbumCreateTaskCallback* pCallback){
	mpCallback = pCallback;
}

void RequestAlbumCreateTask::SetParam(ALBUMTYPE type, string title, string desc){

	mHttpEntiy.Reset();

	char temp[32] = {0};

	if(type != ALBUM_UNKNOWN){
		sprintf(temp, "%d", type);
		mHttpEntiy.AddContent(ALBUM_ALBUM_TYPE, temp);
	}

	if ( !title.empty() ) {
		mHttpEntiy.AddContent(ALBUM_ALBUM_TITLE, title.c_str());
	}

	if ( !desc.empty() ) {
		mHttpEntiy.AddContent(ALBUM_ALBUM_DESC, desc.c_str());
	}

	FileLog("httprequest", "RequestAlbumCreateTask::SetParam( "
			"type : %d "
			"title : %s "
			"desc : %s "
			")",
			type,
			title.c_str(),
			desc.c_str()
			);
}

bool RequestAlbumCreateTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestAlbumCreateTask::HandleCallback( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestAlbumCreateTask::HandleCallback( buf( %d ) : %s )", size, buf);
	}

	string albumId = "";
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue) ) {
			if(dataJson[ALBUM_ALBUM_ID].isString()){
				albumId = dataJson[ALBUM_ALBUM_ID].asString();
				bFlag = true;
			}else {
				// parsing fail
				bFlag = false;
				errnum = LOCAL_ERROR_CODE_PARSEFAIL;
				errmsg = LOCAL_ERROR_CODE_PARSEFAIL_DESC;
			}
		}
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL ) {
		mpCallback->OnAlbumCreate(bFlag, errnum, errmsg, albumId, this);
	}

	return bFlag;
}
