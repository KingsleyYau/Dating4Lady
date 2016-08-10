/* RequestAlbumDelTask.h
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 *  Description: 4.4 删除Album
 */
#include "RequestAlbumDelTask.h"

RequestAlbumDelTask::RequestAlbumDelTask(){
	mUrl = ALBUM_DELETE_ALBUM;
}

RequestAlbumDelTask::~RequestAlbumDelTask(){

}

void RequestAlbumDelTask::SetCallback(IRequestAlbumDelTaskCallback* pCallback){
	mpCallback = pCallback;
}

void RequestAlbumDelTask::SetParam(string albumId, ALBUMTYPE type){

	mHttpEntiy.Reset();

	char temp[32] = {0};

	if ( !albumId.empty() ) {
		mHttpEntiy.AddContent(ALBUM_ALBUM_ID, albumId.c_str());
	}

	if(type != ALBUM_UNKNOWN){
		sprintf(temp, "%d", type);
		mHttpEntiy.AddContent(ALBUM_ALBUM_TYPE, temp);
	}

	FileLog("httprequest", "RequestAlbumDelTask::SetParam( "
			"albumId : %s "
			"type : %d "
			")",
			albumId.c_str(),
			type
			);
}

bool RequestAlbumDelTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestAlbumDelTask::HandleCallback( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestAlbumDelTask::HandleCallback( buf( %d ) : %s )", size, buf);
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
		mpCallback->OnAlbumDel(bFlag, errnum, errmsg, this);
	}

	return bFlag;
}
