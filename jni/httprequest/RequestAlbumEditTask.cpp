/* RequestAlbumEditTask.cpp
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 *  Description: 4.3 修改Album
 */
#include "RequestAlbumEditTask.h"

RequestAlbumEditTask::RequestAlbumEditTask(){
	mUrl = ALBUM_EDIT_ALBUM;
}

RequestAlbumEditTask::~RequestAlbumEditTask(){

}

void RequestAlbumEditTask::SetCallback(IRequestAlbumEditTaskCallback* pCallback){
	mpCallback = pCallback;
}

void RequestAlbumEditTask::SetParam(string albumId, ALBUMTYPE type, string title, string desc){

	mHttpEntiy.Reset();

	char temp[32] = {0};

	if ( !albumId.empty() ) {
		mHttpEntiy.AddContent(ALBUM_ALBUM_ID, albumId.c_str());
	}

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

	FileLog("httprequest", "RequestAlbumEditTask::SetParam( "
			"albumId : %s "
			"type : %d "
			"title : %s "
			"desc : %s "
			")",
			albumId.c_str(),
			type,
			title.c_str(),
			desc.c_str()
			);
}

bool RequestAlbumEditTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestAlbumEditTask::HandleCallback( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestAlbumEditTask::HandleCallback( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		bFlag = HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue);
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL ) {
		mpCallback->OnAlbumEdit(bFlag, errnum, errmsg, this);
	}

	return bFlag;
}
