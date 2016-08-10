/* RequestAlbumDelTask.h
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 *  Description: 4.4 删除Album
 */
#ifndef _REQUESTALBUMDELTASK_H
#define _REQUESTALBUMDELTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestAlbumDefine.h"

class RequestAlbumDelTask;

class IRequestAlbumDelTaskCallback{
public:
	virtual ~IRequestAlbumDelTaskCallback(){}
	virtual void OnAlbumDel(bool success, const string& errnum, const string& errmsg, RequestAlbumDelTask* task) = 0;
};

class RequestAlbumDelTask : public RequestBaseTask{
public:
	RequestAlbumDelTask();
	~RequestAlbumDelTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestAlbumDelTaskCallback* pCallback);

	/**
	 * albumId album的id
	 * type album类型（0：photo，1：video）
	 */
	void SetParam(
			string albumId,
			ALBUMTYPE type);

protected:
	IRequestAlbumDelTaskCallback* mpCallback;
};

#endif/*_REQUESTALBUMDELTASK_H*/
