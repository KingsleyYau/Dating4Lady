/* RequestAlbumListTask.cpp
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 *  Description: 4.1 查询Album列表
 */
#include "RequestAlbumListTask.h"

RequestAlbumListTask::RequestAlbumListTask(){
	mUrl = ALBUM_GET_LIST;
}

RequestAlbumListTask::~RequestAlbumListTask(){

}

bool RequestAlbumListTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestAlbumListTask::HandleCallback( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestAlbumListTask::HandleCallback( buf( %d ) : %s )", size, buf);
	}

	list<AlbumItem> itemList;
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
					AlbumItem item;
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
		mpCallback->OnQueryAlbumList(bFlag, errnum, errmsg, itemList, this);
	}

	return bFlag;
}

void RequestAlbumListTask::SetCallback(IRequestAlbumListTaskCallback* pCallback){
	mpCallback = pCallback;
}

