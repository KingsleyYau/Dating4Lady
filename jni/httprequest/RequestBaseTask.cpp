/*
 * RequestBaseTask.cpp
 *
 *  Created on: 2015-9-10
 *      Author: Max
 */

#include "RequestBaseTask.h"

RequestBaseTask::RequestBaseTask() {
	// TODO Auto-generated constructor stub
	mRequestId = -1;
	mUrl = "";
	mSiteType = AppSite;
	mNoCache = false;
	mbFinishOK = false;
	mErrCode = "";
	mpErrcodeHandler = NULL;
}

RequestBaseTask::~RequestBaseTask() {
	// TODO Auto-generated destructor stub
}

void RequestBaseTask::Init(HttpRequestManager *pHttpRequestManager) {
	mpHttpRequestManager = pHttpRequestManager;
}

void RequestBaseTask::SetErrcodeHandler(ErrcodeHandler* pErrcodeHandler) {
	mpErrcodeHandler = pErrcodeHandler;
}

bool RequestBaseTask::Start() {
	if( BaseTask::Start() ) {
		if( -1 != StartRequest() ) {
			return true;
		} else {
			OnTaskFinish();
		}
	}
	return false;
}

void RequestBaseTask::Stop() {
	StopRequest();
	return BaseTask::Stop();
}

bool RequestBaseTask::IsFinishOK() {
	return mbFinishOK;
}

const char* RequestBaseTask::GetErrCode() {
	return mErrCode.c_str();
}

unsigned long RequestBaseTask::StartRequest() {
	if( mpHttpRequestManager != NULL && mUrl.length() > 0 ) {
		mRequestId = mpHttpRequestManager->StartRequest(mUrl, mHttpEntiy, this, mSiteType, mNoCache);
	}
	return mRequestId;
}

bool RequestBaseTask::StopRequest() {
	bool bFlag = false;
	if( mpHttpRequestManager != NULL && mRequestId != -1 ) {
		bFlag = mpHttpRequestManager->StopRequest(mRequestId);
		mRequestId = 0;
	}
	return bFlag;
}

bool RequestBaseTask::HandleResult(const char* buf, int size, string &errnum, string &errmsg, Json::Value *data, Json::Value *errdata, bool* bContinue) {
	bool bFlag = false;
	bool bIsParseSuccess = false;
	if( bContinue != NULL ) {
		*bContinue = true;
	}

	/* try to parse json */
	Json::Value root;
	Json::Reader reader;

	if( reader.parse(buf, root, false) ) {
		FileLog("httprequest", "RequestBaseTask::HandleResult( parse Json finish )");
		if( root.isObject() ) {
			bIsParseSuccess = true;

			if( root[COMMON_RESULT].isInt() && root[COMMON_RESULT].asInt() == 1 ) {
				errnum = "";
				errmsg = "";
				if( data != NULL ) {
					*data = root[COMMON_DATA];
				}

				bFlag = true;
			} else {
				if( root[COMMON_ERRNO].isString() ) {
					errnum = root[COMMON_ERRNO].asString();

					if( mpErrcodeHandler != NULL ) {
						bool bc = mpErrcodeHandler->ErrcodeHandle(this, errnum);
						if( bContinue != NULL ) {
							*bContinue = bc;
						}
					}
				}

				if( root[COMMON_ERRMSG].isString() ) {
					errmsg = root[COMMON_ERRMSG].asString();
				}

				if( errdata != NULL ) {
					*errdata = root[COMMON_ERRDATA];
				}

				bFlag = false;
			}
		}
	}

	// parse protocol fail
	if (!bIsParseSuccess) {
		errnum = LOCAL_ERROR_CODE_PARSEFAIL;
		errmsg = LOCAL_ERROR_CODE_PARSEFAIL_DESC;
	}

	FileLog("httprequest", "RequestBaseTask::HandleResult( handle json result %s )", bFlag?"OK":"Fail");
	mErrCode = errnum;
	return bFlag;
}

bool RequestBaseTask::HandleResult(const char* buf, int size, string &errnum, string &errmsg, TiXmlDocument &doc, bool* bContinue) {
	bool bFlag = false;
	bool bIsParseSuccess = false;
	if( bContinue != NULL ) {
		*bContinue = true;
	}

	/* try to parse xml */
	TiXmlElement* itemElement;
	doc.Parse(buf);
	const char *p = NULL;

	if ( !doc.Error() ) {
		TiXmlNode *rootNode = doc.FirstChild(COMMON_ROOT);
		if( rootNode != NULL ) {
			TiXmlNode *resultNode = rootNode->FirstChild(COMMON_RESULT);
			if( resultNode != NULL ) {
				bIsParseSuccess = true;

				TiXmlNode *statusNode = resultNode->FirstChild(COMMON_STATUS);
				if( statusNode != NULL ) {
					itemElement = statusNode->ToElement();
					if ( itemElement != NULL ) {
						p = itemElement->GetText();
						if( p != NULL && 1 == atoi(p) ) {
							bFlag = true;
						}
					}
				}

				TiXmlNode *errcodeNode = resultNode->FirstChild(COMMON_ERRCODE);
				if( errcodeNode != NULL ) {
					itemElement = errcodeNode->ToElement();
					if ( itemElement != NULL ) {
						p = itemElement->GetText();
						if( p != NULL ) {
							errnum = p;
							if( mpErrcodeHandler != NULL ) {
								bool bc = mpErrcodeHandler->ErrcodeHandle(this, errnum);
								if( bContinue != NULL ) {
									*bContinue = bc;
								}
							}
						}
					}
				}

				TiXmlNode *errmsgNode = resultNode->FirstChild(COMMON_ERRMSG);
				if( errmsgNode != NULL ) {
					itemElement = errmsgNode->ToElement();
					if ( itemElement != NULL ) {
						p = itemElement->GetText();
						if( p != NULL ) {
							errmsg = p;
						}
					}
				}
			}
		}
	}

	// parse protocol fail
	if (!bIsParseSuccess) {
		errnum = LOCAL_ERROR_CODE_PARSEFAIL;
		errmsg = LOCAL_ERROR_CODE_PARSEFAIL_DESC;
	}

	FileLog("httprequest", "RequestBaseTask::HandleResult( "
			"Value() : %s, "
			"ErrorDesc() : %s, "
			"ErrorRow() : %d, "
			"ErrorCol() : %d "
			")",
			doc.Value(),
			doc.ErrorDesc(),
			doc.ErrorRow(),
			doc.ErrorCol()
			);
	mErrCode = errnum;
	return bFlag;
}

/* IHttpRequestManagerCallback */
void RequestBaseTask::onSuccess(long requestId, string url, const char* buf, int size) {
	// Handle in sub class
	mbFinishOK = HandleCallback(url, true, buf, size);

	// Send message to main thread
	OnTaskFinish();
}

void RequestBaseTask::onFail(long requestId, string url) {
	// Handle in sub class
	mbFinishOK = HandleCallback(url, false, NULL, 0);

	// Send message to main thread
	OnTaskFinish();
}

/* Other */
// 获取下载总数据量及已收数据字节数
void RequestBaseTask::GetRecvDataCount(int& total, int& recv) const
{
	if (NULL != mpHttpRequestManager) {
		const HttpRequest* request = mpHttpRequestManager->GetRequestById(mRequestId);
		if (NULL != request) {
			request->GetRecvDataCount(total, recv);
		}
	}
}

// 获取上传总数据量及已收数据字节数
void RequestBaseTask::GetSendDataCount(int& total, int& send) const
{
	if (NULL != mpHttpRequestManager) {
		const HttpRequest* request = mpHttpRequestManager->GetRequestById(mRequestId);
		if (NULL != request) {
			request->GetSendDataCount(total, send);
		}
	}
}

// 获取 Content-Type
string RequestBaseTask::GetContentType() const
{
	string contentType("");
	if (NULL != mpHttpRequestManager) {
		const HttpRequest* request = mpHttpRequestManager->GetRequestById(mRequestId);
		if (NULL != request) {
			contentType = request->GetContentType();
		}
	}
	return contentType;
}
