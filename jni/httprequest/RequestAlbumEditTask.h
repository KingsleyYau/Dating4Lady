/* RequestAlbumEditTask.h
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 *  Description: 4.3 修改Album
 */
#ifndef _REQUESTALBUMEDITTASK_H
#define _REQUESTALBUMEDITTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestAlbumDefine.h"

class RequestAlbumEditTask;

class IRequestAlbumEditTaskCallback{
public:
	virtual ~IRequestAlbumEditTaskCallback(){}
	virtual void OnAlbumEdit(bool success, const string& errnum, const string& errmsg, RequestAlbumEditTask* task) = 0;
};

class RequestAlbumEditTask : public RequestBaseTask{
public:
	RequestAlbumEditTask();
	~RequestAlbumEditTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestAlbumEditTaskCallback* pCallback);

	/**
	 * albumId album的id
	 * type album类型（0：photo，1：video）
	 * title：名称
	 * desc：描述
	 */
	void SetParam(
			string albumId,
			ALBUMTYPE type,
			string title,
			string desc);

protected:
	IRequestAlbumEditTaskCallback* mpCallback;
};

#endif/*_REQUESTALBUMEDITTASK_H*/
