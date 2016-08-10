/* RequestAlbumAddPhotoTask.cpp
 *
 *  Created on: 2016.7.19
 *      Author: Hunter.Mun
 *  Description: 4.6 添加Photo
 */
#include "RequestAlbumAddPhotoTask.h"

RequestAlbumAddPhotoTask::RequestAlbumAddPhotoTask(){
	mUrl = ALBUM_ADD_PHOTO_URL;
}

RequestAlbumAddPhotoTask::~RequestAlbumAddPhotoTask(){

}

void RequestAlbumAddPhotoTask::SetCallback(IRequestAlbumAddPhotoTaskCallback* pCallback){
	mpCallback = pCallback;
}

void RequestAlbumAddPhotoTask::SetParam(string albumId, string photoTitle, const string& filePath){

	mHttpEntiy.Reset();

	if ( !albumId.empty() ) {
		mHttpEntiy.AddContent(ALBUM_ALBUM_ID, albumId.c_str());
	}

	if ( !photoTitle.empty() ) {
		mHttpEntiy.AddContent(ALBUM_PHOTO_TITLE, photoTitle.c_str());
	}

	if ( !filePath.empty() ) {
		mHttpEntiy.AddFile(ALBUM_ADD_PHOTO_SRCFILE, filePath.c_str());
	}

	FileLog("httprequest", "RequestAlbumAddPhotoTask::SetParam( "
			"albumId : %s "
			"photoTitle : %s "
			"filePath : %s "
			")",
			albumId.c_str(),
			photoTitle.c_str(),
			filePath.c_str()
			);
}

bool RequestAlbumAddPhotoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestAlbumAddPhotoTask::HandleCallback( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestAlbumAddPhotoTask::HandleCallback( buf( %d ) : %s )", size, buf);
	}

	string photoId = "";
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		if(HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue)){
			if(dataJson[ALBUM_PHOTO_ID].isString()){
				photoId = dataJson[ALBUM_PHOTO_ID].asString();
				bFlag = true;
			}else{
				bFlag = false;
				errnum = LOCAL_ERROR_CODE_TIMEOUT;
				errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
			}
		}
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL ) {
		mpCallback->OnAlbumAddPhoto(bFlag, errnum, errmsg, photoId, this);
	}

	return bFlag;
}
