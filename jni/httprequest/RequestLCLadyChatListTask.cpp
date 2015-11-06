/*
 * RequestLCLadyChatListTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 查询男士聊天历史
 */

#include "RequestLCLadyChatListTask.h"

RequestLCLadyChatListTask::RequestLCLadyChatListTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_LADYCHATLIST_PATH;
}

RequestLCLadyChatListTask::~RequestLCLadyChatListTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCLadyChatListTask::SetCallback(IRequestLCLadyChatListCallback* pCallback)
{
	mpCallback = pCallback;
}

void RequestLCLadyChatListTask::SetParam(const string& manId)
{
	mHttpEntiy.Reset();

	if( manId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_LADYCHATLIST_MANID, manId.c_str());
	}

	FileLog("httprequest", "RequestLCLadyChatListTask::SetParam( "
			"manId : %s "
			")",
			manId.c_str()
			);
}

bool RequestLCLadyChatListTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCLadyChatListTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCLadyChatListTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	LiveChatConversationList theList;
	bool bContinue = true;
	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue) ) {
			bFlag = true;

			if ( dataJson[COMMON_DATA_LIST].isArray() ) {
				for(int i = 0; i < dataJson[COMMON_DATA_LIST].size(); i++ ) {
					LiveChatConversationListItem item;
					if ( item.Parse(dataJson[COMMON_DATA_LIST].get(i, Json::Value::null)) )
					{
						theList.push_back(item);
					}
				}
			}
		}
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL ) {
		mpCallback->OnLadyChatList(bFlag, errnum, errmsg, theList, this);
	}
	return bFlag;
}

