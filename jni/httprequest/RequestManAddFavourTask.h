/*
 * RequestManAddFavourTask.h
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#ifndef REQUESTMANADDFAVOURTASK_H_
#define REQUESTMANADDFAVOURTASK_H_

using namespace std;

#include "RequestBaseTask.h"
#include "RequestManDefine.h"

class RequestManAddFavourTask;

class IRequestManAddFavourCallback {
public:
	virtual ~IRequestManAddFavourCallback(){};
	virtual void OnAddFavourites(bool success, const string& errnum, const string& errmsg, RequestManAddFavourTask* task) = 0;
};

class RequestManAddFavourTask : public RequestBaseTask {
public:
	RequestManAddFavourTask();
	virtual ~RequestManAddFavourTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestManAddFavourCallback* pCallback);

	/**
	 * 3.4.收藏男士（http post）
	 * @param man_id			男士id
	 */
	void SetParam(const string& man_id);

protected:
	IRequestManAddFavourCallback* mpCallback;
};

#endif /* REQUESTMANADDFAVOURTASK_H_ */
