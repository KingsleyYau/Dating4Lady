/*
 * RequestLCPhotoListTask.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 查询图片（私密照）列表
 */

#ifndef REQUESTLCPHOTOLISTTASK_H_
#define REQUESTLCPHOTOLISTTASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"
#include "item/LiveChatAlbumListItem.h"
#include "item/LiveChatPhotoListItem.h"

class RequestLCPhotoListTask;

class IRequestLCPhotoListCallback {
public:
	virtual ~IRequestLCPhotoListCallback(){};
	virtual void OnPhotoList(bool success
							, const string& errnum
							, const string& errmsg
							, const LiveChatAlbumList& albumList
							, const LiveChatPhotoList& photoList
							, RequestLCPhotoListTask* task) = 0;
};

class RequestLCPhotoListTask : public RequestBaseTask {
public:
	RequestLCPhotoListTask();
	virtual ~RequestLCPhotoListTask();

	// set request param
	void SetParam(const string& sid, const string& userId);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCPhotoListCallback* pCallback);

protected:
	IRequestLCPhotoListCallback* mpCallback;
};

#endif /* REQUESTLCPHOTOLISTTASK_H_ */
