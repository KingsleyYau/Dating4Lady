/* RequestAlbumEditVideoTask.cpp
 *
 *  Created on: 2016.7.19
 *      Author: Hunter.Mun
 *  Description: 4.10 修改Video
 */
#include "RequestAlbumEditVideoTask.h"

RequestAlbumEditVideoTask::RequestAlbumEditVideoTask(){
	mUrl = ALBUM_EDIT_VIDEO_URL;
}

RequestAlbumEditVideoTask::~RequestAlbumEditVideoTask(){

}

void RequestAlbumEditVideoTask::SetCallback(IRequestAlbumEditVideoTaskCallback* pCallback){
	mpCallback = pCallback;
}

void RequestAlbumEditVideoTask::SetParam(
		string videoId,
		string videoTitle,
		const string& videoThumbPath){

	mHttpEntiy.Reset();

	if ( !videoId.empty() ) {
		mHttpEntiy.AddContent(ALBUM_VIDEO_ID, videoId.c_str());
	}

	if ( !videoTitle.empty() ) {
		mHttpEntiy.AddContent(ALBUM_VIDEO_TITLE, videoTitle.c_str());
	}

	if ( !videoThumbPath.empty() ) {
		mHttpEntiy.AddFile(ALBUM_ADD_VIDEO_THUMBFILE, videoThumbPath.c_str());
	}

	FileLog("httprequest", "RequestAlbumEditVideoTask::SetParam( "
			"videoId : %s "
			"videoTitle : %s "
			"videoThumbPath : %s "
			")",
			videoId.c_str(),
			videoTitle.c_str(),
			videoThumbPath.c_str()
			);
}

bool RequestAlbumEditVideoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestAlbumEditVideoTask::HandleCallback( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestAlbumEditVideoTask::HandleCallback( buf( %d ) : %s )", size, buf);
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
		mpCallback->OnAlbumEditVideo(bFlag, errnum, errmsg, this);
	}

	return bFlag;
}
