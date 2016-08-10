/* RequestAlbumVideoListTask.cpp
 *
 *  Created on: 2016.7.19
 *      Author: Hunter.Mun
 *  Description: 4.8 查询Video列表
 */
#include "RequestAlbumVideoListTask.h"

RequestAlbumVideoListTask::RequestAlbumVideoListTask(){
	mUrl = ALBUM_VIDEO_LIST_URL;
}

RequestAlbumVideoListTask::~RequestAlbumVideoListTask(){

}

void RequestAlbumVideoListTask::SetCallback(IRequestAlbumVideoListTaskCallback* pCallback){
	mpCallback = pCallback;
}

void RequestAlbumVideoListTask::SetParam(string albumId){

	mHttpEntiy.Reset();

	if ( !albumId.empty() ) {
		mHttpEntiy.AddContent(ALBUM_ALBUM_ID, albumId.c_str());
	}

	FileLog("httprequest", "RequestAlbumVideoListTask::SetParam( "
			"albumId : %s "
			")",
			albumId.c_str()
			);
}

bool RequestAlbumVideoListTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestAlbumVideoListTask::HandleCallback( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestAlbumVideoListTask::HandleCallback( buf( %d ) : %s )", size, buf);
	}

	list<AlbumVideoItem> itemlist;
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue) ) {
			if( dataJson[COMMON_DATA_LIST].isArray() ) {
				for(int i = 0; i < dataJson[COMMON_DATA_LIST].size(); i++ ) {
					AlbumVideoItem item;
					item.Parse(dataJson[COMMON_DATA_LIST].get(i, Json::Value::null));
					itemlist.push_back(item);
				}
				bFlag = true;
			} else {
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
		mpCallback->OnAlbumVideoList(bFlag, errnum, errmsg, itemlist, this);
	}

	return bFlag;
}
