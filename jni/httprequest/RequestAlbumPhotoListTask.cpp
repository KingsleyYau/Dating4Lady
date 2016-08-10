/* RequestAlbumPhotoListTask.cpp
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 *  Description: 4.5 查询Photo列表
 */
#include "RequestAlbumPhotoListTask.h"

RequestAlbumPhotoListTask::RequestAlbumPhotoListTask(){
	mUrl = ALBUM_PHOTO_LIST;
}

RequestAlbumPhotoListTask::~RequestAlbumPhotoListTask(){

}

void RequestAlbumPhotoListTask::SetCallback(IRequestAlbumPhotoListTaskCallback* pCallback){
	mpCallback = pCallback;
}

void RequestAlbumPhotoListTask::SetParam(string albumId){
	mHttpEntiy.Reset();

	if ( !albumId.empty() ) {
		mHttpEntiy.AddContent(ALBUM_ALBUM_ID, albumId.c_str());
	}

	FileLog("httprequest", "RequestAlbumPhotoListTask::SetParam( "
			"albumId : %s "
			")",
			albumId.c_str()
			);
}

bool RequestAlbumPhotoListTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestAlbumPhotoListTask::HandleCallback( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestAlbumPhotoListTask::HandleCallback( buf( %d ) : %s )", size, buf);
	}

	list<AlbumPhotoItem> itemList;
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
					AlbumPhotoItem item;
					item.Parse(dataJson[COMMON_DATA_LIST].get(i, Json::Value::null));
					itemList.push_back(item);
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
		mpCallback->OnAlbumPhotoList(bFlag, errnum, errmsg, itemList, this);
	}

	return bFlag;
}
