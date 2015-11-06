/*
 * RequestLCLadyChatListTask.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 查询男士聊天历史
 */

#ifndef REQUESTLCLADYCHATLISTTASK_H_
#define REQUESTLCLADYCHATLISTTASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"
#include "item/LiveChatConversationListItem.h"

class RequestLCLadyChatListTask;

class IRequestLCLadyChatListCallback {
public:
	virtual ~IRequestLCLadyChatListCallback(){};
	virtual void OnLadyChatList(bool success
								, const string& errnum
								, const string& errmsg
								, const LiveChatConversationList& theList
								, RequestLCLadyChatListTask* task) = 0;
};

class RequestLCLadyChatListTask : public RequestBaseTask {
public:
	RequestLCLadyChatListTask();
	virtual ~RequestLCLadyChatListTask();

	// set request param
	void SetParam(const string& manId);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCLadyChatListCallback* pCallback);

protected:
	IRequestLCLadyChatListCallback* mpCallback;
};

#endif /* REQUESTLCLADYCHATLISTTASK_H_ */
