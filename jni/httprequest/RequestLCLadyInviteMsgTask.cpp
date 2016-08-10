/*
 * RequestLCLadyInviteMsgTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 查询男士历史聊天消息
 */

#include "RequestLCLadyInviteMsgTask.h"

RequestLCLadyInviteMsgTask::RequestLCLadyInviteMsgTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_LADYINVITEMSG_PATH;
	m_inviteId = "";
}

RequestLCLadyInviteMsgTask::~RequestLCLadyInviteMsgTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCLadyInviteMsgTask::SetCallback(IRequestLCLadyInviteMsgCallback* pCallback)
{
	mpCallback = pCallback;
}

void RequestLCLadyInviteMsgTask::SetParam(const string& inviteId)
{
	mHttpEntiy.Reset();

	if( inviteId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_LADYINVITEMSG_INVITEID, inviteId.c_str());
		m_inviteId = inviteId;
	}

	FileLog("httprequest", "RequestLCLadyInviteMsgTask::SetParam( "
			"inviteId : %s "
			")",
			inviteId.c_str()
			);
}

bool RequestLCLadyInviteMsgTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCLadyInviteMsgTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCLadyInviteMsgTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	LiveChatRecordList theList;
	int dbTime = 0;
	bool bContinue = true;
	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue) ) {
			bFlag = true;

			if ( dataJson[LC_LADYINVITEMSG_DBTIME].isIntegral() ) {
				dbTime = dataJson[LC_LADYINVITEMSG_DBTIME].asInt();
			}

			if ( dataJson[LC_LADYINVITEMSG_INVITEMSG].isArray() ) {
				for(int i = 0; i < dataJson[LC_LADYINVITEMSG_INVITEMSG].size(); i++ ) {
					LiveChatRecordListItem item;
					if ( item.Parse(dataJson[LC_LADYINVITEMSG_INVITEMSG].get(i, Json::Value::null)) )
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
		mpCallback->OnLadyInviteMsg(bFlag, errnum, errmsg, dbTime, theList, m_inviteId, this);
	}

	return bFlag;
}

