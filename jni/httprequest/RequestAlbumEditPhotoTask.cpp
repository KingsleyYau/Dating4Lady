/* RequestAlbumEditPhotoTask.cpp
 *
 *  Created on: 2016.7.19
 *      Author: Hunter.Mun
 *  Description: 4.7 修改Photo
 */
#include "RequestAlbumEditPhotoTask.h"

RequestAlbumEditPhotoTask::RequestAlbumEditPhotoTask(){
	mUrl = ALBUM_EDIT_PHOTO_URL;
}

RequestAlbumEditPhotoTask::~RequestAlbumEditPhotoTask(){

}

void RequestAlbumEditPhotoTask::SetCallback(IRequestAlbumEditPhotoTaskCallback* pCallback){
	mpCallback = pCallback;
}

void RequestAlbumEditPhotoTask::SetParam(string photoId, string photoTitle){

	mHttpEntiy.Reset();

	if ( !photoId.empty() ) {
		mHttpEntiy.AddContent(ALBUM_PHOTO_ID, photoId.c_str());
	}

	if ( !photoTitle.empty() ) {
		mHttpEntiy.AddContent(ALBUM_PHOTO_TITLE, photoTitle.c_str());
	}

	FileLog("httprequest", "RequestAlbumEditPhotoTask::SetParam( "
			"photoId : %s "
			"photoTitle : %s "
			")",
			photoId.c_str(),
			photoTitle.c_str()
			);
}

bool RequestAlbumEditPhotoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestAlbumEditPhotoTask::HandleCallback( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestAlbumEditPhotoTask::HandleCallback( buf( %d ) : %s )", size, buf);
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
		mpCallback->OnAlbumEditPhoto(bFlag, errnum, errmsg, this);
	}

	return bFlag;
}
