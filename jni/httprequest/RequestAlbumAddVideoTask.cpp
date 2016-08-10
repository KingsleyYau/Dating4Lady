/* RequestAlbumAddVideoTask.cpp
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 *  Description: 4.9 添加Video
 */
#include "RequestAlbumAddVideoTask.h"

RequestAlbumAddVideoTask::RequestAlbumAddVideoTask(){
	mUrl = ALBUM_ADD_VIDEO_URL;
}

RequestAlbumAddVideoTask::~RequestAlbumAddVideoTask(){

}

void RequestAlbumAddVideoTask::SetCallback(IRequestAlbumAddVideoTaskCallback* pCallback){
	mpCallback = pCallback;
}

void RequestAlbumAddVideoTask::SetParam(
		string albumId,
		string videoTitle,
		string shortVideoKey,
		string hidFileMd5ID,
		const string& thumbFilePath){

	mHttpEntiy.Reset();

	if ( !albumId.empty() ) {
		mHttpEntiy.AddContent(ALBUM_ALBUM_ID, albumId.c_str());
	}

	if ( !videoTitle.empty() ) {
		mHttpEntiy.AddContent(ALBUM_VIDEO_TITLE, videoTitle.c_str());
	}

	if ( !shortVideoKey.empty() ) {
		mHttpEntiy.AddContent(ALBUM_ADD_VIDEO_SHORTKEY, shortVideoKey.c_str());
	}

	if ( !hidFileMd5ID.empty() ) {
		mHttpEntiy.AddContent(ALBUM_ADD_VIDEO_FILEMD5ID, hidFileMd5ID.c_str());
	}

	if ( !thumbFilePath.empty() ) {
		mHttpEntiy.AddFile(ALBUM_ADD_VIDEO_THUMBFILE, thumbFilePath.c_str());
	}

	FileLog("httprequest", "RequestAlbumAddVideoTask::SetParam( "
			"albumId : %s "
			"videoTitle : %s "
			"shortVideoKey : %s "
			"hidFileMd5ID : %s "
			"thumbFilePath : %s "
			")",
			albumId.c_str(),
			videoTitle.c_str(),
			shortVideoKey.c_str(),
			hidFileMd5ID.c_str(),
			thumbFilePath.c_str()
			);
}

bool RequestAlbumAddVideoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestAlbumAddVideoTask::HandleCallback( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestAlbumAddVideoTask::HandleCallback( buf( %d ) : %s )", size, buf);
	}

	string videoId = "";
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		if(HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue)){
			if(dataJson[ALBUM_VIDEO_ID].isString()){
				videoId = dataJson[ALBUM_VIDEO_ID].asString();
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
		mpCallback->OnAlbumAddVideo(bFlag, errnum, errmsg, videoId, this);
	}

	return bFlag;
}
