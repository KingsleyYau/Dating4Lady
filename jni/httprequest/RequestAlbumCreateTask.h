/* RequestAlbumCreateTask.h
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 *  Description: 4.2 创建Album
 */
#ifndef _REQUESTALBUMCREATETASK_H
#define _REQUESTALBUMCREATETASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestAlbumDefine.h"

class RequestAlbumCreateTask;

class IRequestAlbumCreateTaskCallback{
public:
	virtual ~IRequestAlbumCreateTaskCallback(){}
	virtual void OnAlbumCreate(bool success, const string& errnum, const string& errmsg, const string& albumId, RequestAlbumCreateTask* task) = 0;
};

class RequestAlbumCreateTask : public RequestBaseTask{
public:
	RequestAlbumCreateTask();
	~RequestAlbumCreateTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestAlbumCreateTaskCallback* pCallback);

	/**
	 * type album类型（0：photo，1：video）
	 * title：名称
	 * desc：描述
	 */
	void SetParam(
			ALBUMTYPE type,
			string title,
			string desc);

protected:
	IRequestAlbumCreateTaskCallback* mpCallback;
};

#endif/*_REQUESTALBUMCREATETASK_H*/
