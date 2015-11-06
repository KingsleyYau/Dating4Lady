/*
 * RequestManRemoveFavourTask.h
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#ifndef REQUESTMANREMOVEFAVOURTASK_H_
#define REQUESTMANREMOVEFAVOURTASK_H_

#include <list>
using namespace std;

#include "RequestBaseTask.h"
#include "RequestManDefine.h"

class RequestManRemoveFavourTask;

class IRequestManRemoveFavourCallback {
public:
	virtual ~IRequestManRemoveFavourCallback(){};
	virtual void OnRemoveFavourites(bool success, const string& errnum, const string& errmsg, RequestManRemoveFavourTask* task) = 0;
};

class RequestManRemoveFavourTask : public RequestBaseTask {
public:
	RequestManRemoveFavourTask();
	virtual ~RequestManRemoveFavourTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestManRemoveFavourCallback* pCallback);

	/**
	 * 3.5.删除已收藏男士（http post）
	 * @param man_ids			男士ids
	 */
	void SetParam(list<string> man_ids);

protected:
	IRequestManRemoveFavourCallback* mpCallback;
};

#endif /* REQUESTMANREMOVEFAVOURTASK_H_ */
