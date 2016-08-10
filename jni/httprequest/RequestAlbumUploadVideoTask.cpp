/* RequestAlbumUploadVideoTask.cpp
 *
 *  Created on: 2016.7.19
 *      Author: Hunter.Mun
 *  Description: 4.9 上传Video文件
 */
#include "RequestAlbumUploadVideoTask.h"

RequestAlbumUploadVideoTask::RequestAlbumUploadVideoTask(){
	mUrl = ALBUM_UPLOAD_VIDEO_URL;
	mSiteType = VideoUploadSite;
}

RequestAlbumUploadVideoTask::~RequestAlbumUploadVideoTask(){

}

void RequestAlbumUploadVideoTask::SetCallback(IRequestAlbumUploadVideoTaskCallback* pCallback){
	mpCallback = pCallback;
}

void RequestAlbumUploadVideoTask::SetParam(
		string agencyID
		, string womanID
		, int siteId
		, string shortVideoKey
		, int serverType
		, const string& filePath
		, const string& mimeType){

	mHttpEntiy.Reset();

	char temp[32] = {0};

	sprintf(temp, "%d", 1);

	mUrl += "?";

	mUrl += ALBUM_UPLOAD_VIDEO_NUM;
	mUrl += "=";
	mUrl += temp;

	mUrl += "&";
	mUrl += ALBUM_UPLOAD_VIDEO_AGENCY;
	mUrl += "=";
	mUrl += agencyID;

	mUrl += "&";
	mUrl += ALBUM_UPLOAD_VIDEO_WOMANID;
	mUrl += "=";
	mUrl += womanID;

	sprintf(temp, "%d", siteId);
	mUrl += "&";
	mUrl += ALBUM_UPLOAD_VIDEO_SITEID;
	mUrl += "=";
	mUrl += temp;

	mUrl += "&";
	mUrl += ALBUM_ADD_VIDEO_SHORTKEY;
	mUrl += "=";
	mUrl += shortVideoKey;

	sprintf(temp, "%d", serverType);
	mUrl += "&";
	mUrl += ALBUM_UPLOAD_VIDEO_SERVER;
	mUrl += "=";
	mUrl += temp;
//	mHttpEntiy.AddContent(ALBUM_UPLOAD_VIDEO_NUM, temp);
//
//	if ( !agencyID.empty() ) {
//		mHttpEntiy.AddContent(ALBUM_UPLOAD_VIDEO_AGENCY, agencyID.c_str());
//	}
//
//	if ( !womanID.empty() ) {
//		mHttpEntiy.AddContent(ALBUM_UPLOAD_VIDEO_WOMANID, womanID.c_str());
//	}
//
//	sprintf(temp, "%d", siteId);
//	mHttpEntiy.AddContent(ALBUM_UPLOAD_VIDEO_SITEID, temp);
//
//	if ( !shortVideoKey.empty() ) {
//		mHttpEntiy.AddContent(ALBUM_ADD_VIDEO_SHORTKEY, shortVideoKey.c_str());
//	}
//
//	sprintf(temp, "%d", serverType);
//	mHttpEntiy.AddContent(ALBUM_UPLOAD_VIDEO_SERVER, temp);

	if ( !filePath.empty() &&  !mimeType.empty()) {
		mHttpEntiy.AddFile(ALBUM_UPLOAD_VIDEO_FILEDATA, filePath.c_str(), mimeType.c_str());
	}

	FileLog("httprequest", "RequestAlbumUploadVideoTask::SetParam( "
			"agencyID : %s "
			"womanID : %s "
			"siteId : %d "
			"shortVideoKey : %s "
			"serverType : %d "
			"filePath : %s "
			"mimeType : %s "
			")",
			agencyID.c_str(),
			womanID.c_str(),
			siteId,
			shortVideoKey.c_str(),
			serverType,
			filePath.c_str(),
			mimeType.c_str()
			);
}

bool RequestAlbumUploadVideoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestAlbumUploadVideoTask::HandleCallback( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestAlbumUploadVideoTask::HandleCallback( buf( %d ) : %s )", size, buf);
	}

	string identifyKey = "";
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;

	if (requestRet) {
		bFlag = true;
		identifyKey.assign(buf, size);
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL ) {
		mpCallback->OnAlbumUploadVideo(bFlag, errnum, errmsg, identifyKey, this);
	}

	return bFlag;
}
