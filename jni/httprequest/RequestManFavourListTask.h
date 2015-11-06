/*
 * RequestManFavourListTask.h
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#ifndef REQUESTMANFAVOURLISTTASK_H_
#define REQUESTMANFAVOURLISTTASK_H_

#include <list>
using namespace std;

#include "RequestBaseTask.h"
#include "RequestManDefine.h"

class RequestManFavourListTask;

class IRequestManFavourListCallback {
public:
	virtual ~IRequestManFavourListCallback(){};
	virtual void OnQueryFavourList(bool success, const string& errnum, const string& errmsg, const list<string>& itemList, RequestManFavourListTask* task) = 0;
};

/**
 * 3.3.查询已收藏的男士列表（http post）
 */
class RequestManFavourListTask : public RequestBaseTask {
public:
	RequestManFavourListTask();
	virtual ~RequestManFavourListTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestManFavourListCallback* pCallback);

protected:
	IRequestManFavourListCallback* mpCallback;
};

#endif /* REQUESTMANFAVOURLISTTASK_H_ */
