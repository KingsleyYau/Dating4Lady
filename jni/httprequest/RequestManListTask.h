/*
 * RequestManListTask.h
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#ifndef REQUESTMANLISTTASK_H_
#define REQUESTMANLISTTASK_H_

#include <list>
using namespace std;

#include "RequestBaseTask.h"
#include "RequestManDefine.h"
#include "item/ManListItem.h"

class RequestManListTask;

class IRequestManListCallback {
public:
	virtual ~IRequestManListCallback(){};
	virtual void OnQueryManList(bool success, const string& errnum, const string& errmsg, const list<ManListItem>& itemList, int totalCount, RequestManListTask* task) = 0;
};

class RequestManListTask : public RequestBaseTask {
public:
	RequestManListTask();
	virtual ~RequestManListTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestManListCallback* pCallback);

    /**
     * 3.1.查询男士列表（http post）
     * @param pageIndex			当前页数
     * @param pageSize			每页行数
     * @param query_type		查询类型
     * @param man_id			女士ID(长度等于0：默认)
     * @param from_age			起始年龄(小于0：默认)
     * @param to_age			结束年龄(小于0：默认)
     * @param country			国家代码
     * @param photo				是否有照片
     */
	void SetParam(
			int pageIndex,
			int pageSize,
			QUERYTYPE query_type,
			const string& man_id,
			int from_age,
			int to_age,
			int country,
			bool photo
			);

protected:
	IRequestManListCallback* mpCallback;
};

#endif /* REQUESTMANLISTTASK_H_ */
