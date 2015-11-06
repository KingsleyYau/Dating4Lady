/*
 * RequestOperator.cpp
 *
 *  Created on: 2015-10-28
 *      Author: Max
 */

#include "RequestOperator.h"

class RequestOperator;
class RequestOperatorRunnable : public KRunnable {
public:
	RequestOperatorRunnable(RequestOperator *container) {
		mpContainer = container;
	}

	virtual ~RequestOperatorRunnable() {
		mpContainer = NULL;
	}

protected:
	void onRun() {
		mpContainer->HandleRunnable();
	}

private:
	RequestOperator *mpContainer;
};

RequestOperator::RequestOperator() {
	// TODO Auto-generated constructor stub
	mpRunnable = new RequestOperatorRunnable(this);
}

RequestOperator::~RequestOperator() {
	// TODO Auto-generated destructor stub
	if( mpRunnable != NULL ) {
		delete mpRunnable;
	}
}

void RequestOperator::SetTask(RequestBaseTask* pTask){
	mpTask = pTask;
	mpTask->SetErrcodeHandler(this);
}

bool RequestOperator::Start() {
	FileLog("httprequest", "RequestOperator::Start( task : %p )", this);
	if( BaseTask::Start() ) {
		mbAlreadyHandle = false;
		mbCanBeHandle = false;

		if( mpTask != NULL ) {
			mpOldITaskCallback = mpTask->GetTaskCallback();
			mpTask->SetTaskCallback(this);
			if( -1 != mKThread.start(mpRunnable) ) {
				return true;
			} else {
				mpTask->SetTaskCallback(mpOldITaskCallback);
				if( mpOldITaskCallback != NULL ) {
					mpOldITaskCallback->OnTaskFinish(mpTask);
				}
				BaseTask::OnTaskFinish();
			}
		} else {
			BaseTask::OnTaskFinish();
		}
	}

	return false;
}

void RequestOperator::Stop() {
	FileLog("httprequest", "RequestOperator::Stop()");
	mKThread.stop();
	BaseTask::Stop();
}

bool RequestOperator::IsFinishOK() {
	return true;
}

const char* RequestOperator::GetErrCode() {
	return "";
}

void RequestOperator::OnTaskFinish(ITask* pTask) {
	FileLog("httprequest", "RequestOperator::OnTaskFinish( "
			"pTask : %p "
			")",
			pTask
			);
	mKCond.signal();
}

bool RequestOperator::ErrcodeHandle(const RequestBaseTask* request, const string &errnum) {
	FileLog("httprequest", "RequestOperator::ErrcodeHandle( "
			"errnum : %s, "
			"mbAlreadyHandle : %s, "
			"mbCanBeHandle : %s "
			")",
			errnum.c_str(),
			mbAlreadyHandle?"true":"false",
			mbCanBeHandle?"true":"false"
			);

	// 符合处理逻辑, 并且未被处理
	if( strcmp(errnum.c_str(), ERROR_CODE_MBCE0003) == 0 && !mbAlreadyHandle ) {
		mbCanBeHandle = true;
		return false;
	}
	return true;
}

void RequestOperator::OnLogin(const LoginManager* pLoginManager, bool success, const string& errnum, const string& errmsg, const LoginItem& item) {
	// 登录返回
	mKCond.signal();
}

void RequestOperator::OnLogout(const LoginManager* pLoginManager, LogoutType type) {

}

void RequestOperator::HandleRunnable() {
	if( mpTask != NULL ) {
		FileLog("httprequest", "RequestOperator::HandleRunnable( start )");

		if( mpTask->Start() ) {
			mKCond.wait();

			// 符合处理逻辑, 并且未被处理
			if( mbCanBeHandle && !mbAlreadyHandle ) {
				mbAlreadyHandle = true;

				FileLog("httprequest", "RequestOperator::HandleRunnable( "
						"AutoLogin start "
						")"
						);

				// 需要重登录
				LoginManager::GetInstance().AddCallback(this);
				LoginManager::GetInstance().Logout(SESSIONTIMEOUT);
				LoginManager::GetInstance().AutoLogin();

				// 等待登录成功
				mKCond.wait();
				LoginManager::GetInstance().RemoveCallback(this);

				FileLog("httprequest", "RequestOperator::HandleRunnable( "
						"AutoLogin finish "
						")"
						);

				// 重新请求
				if( mpTask->Start() ) {
					mKCond.wait();
				}
			}
		}

		FileLog("httprequest", "RequestOperator::HandleRunnable( "
				"end "
				")"
				);

		// 处理完成
		mpTask->SetTaskCallback(mpOldITaskCallback);
		if( mpOldITaskCallback != NULL ) {
			mpOldITaskCallback->OnTaskFinish(mpTask);
		}
	}

	BaseTask::OnTaskFinish();
}
