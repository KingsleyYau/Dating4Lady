/*
 * RequestManRecentViewListTask.h
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#ifndef REQUESTMANRECENTVIEWLISTTASK_H_
#define REQUESTMANRECENTVIEWLISTTASK_H_

#include <list>
using namespace std;

#include "RequestBaseTask.h"
#include "RequestManDefine.h"
#include "item/ManRecentViewListItem.h"

class RequestManRecentViewListTask;

class IRequestManRecentViewListCallback {
public:
	virtual ~IRequestManRecentViewListCallback(){};
	virtual void OnQueryRecentViewList(bool success, const string& errnum, const string& errmsg, const list<ManRecentViewListItem>& itemList, RequestManRecentViewListTask* task) = 0;
};

/**
 * 3.3.查询已收藏的男士列表（http post）
 */
class RequestManRecentViewListTask : public RequestBaseTask {
public:
	RequestManRecentViewListTask();
	virtual ~RequestManRecentViewListTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestManRecentViewListCallback* pCallback);

protected:
	IRequestManRecentViewListCallback* mpCallback;
};

#endif /* REQUESTMANRECENTVIEWLISTTASK_H_ */
