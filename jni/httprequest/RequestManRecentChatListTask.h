/*
 * RequestManRecentChatListTask.h
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#ifndef REQUESTMANRECENTCHATLISTTASK_H_
#define REQUESTMANRECENTCHATLISTTASK_H_

#include <list>
using namespace std;

#include "RequestBaseTask.h"
#include "RequestManDefine.h"
#include "item/ManRecentChatListItem.h"

class RequestManRecentChatListTask;

class IRequestManRecentChatListCallback {
public:
	virtual ~IRequestManRecentChatListCallback(){};
	virtual void OnQueryManRecentChatList(bool success, const string& errnum, const string& errmsg, const list<ManRecentChatListItem>& itemList, int totalCount, RequestManRecentChatListTask* task) = 0;
};

class RequestManRecentChatListTask : public RequestBaseTask {
public:
	RequestManRecentChatListTask();
	virtual ~RequestManRecentChatListTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestManRecentChatListCallback* pCallback);

	/**
	 * 3.6.获取最近聊天男士列表（http post）
	 * @param pageIndex			当前页数
	 * @param pageSize			每页行数
	 * @param query_type		查询类型
	 */
	void SetParam(
			int pageIndex,
			int pageSize,
			RECENT_CHAT_QUERYTYPE query_type
			);

protected:
	IRequestManRecentChatListCallback* mpCallback;
};

#endif /* REQUESTMANRECENTCHATLISTTASK_H_ */
