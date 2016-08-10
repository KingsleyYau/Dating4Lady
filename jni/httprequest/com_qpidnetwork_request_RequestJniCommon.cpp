#include "com_qpidnetwork_request_RequestJniCommon.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"

#include <common/command.h>

#include "RequestOperator.h"

#include "RequestCommonTranslateTextTask.h"

/*************************** Translate Text *********************************/
class RequestCommonTranslateTextCallback : public IRequestCommonTranslateTextCallback
{
public:
	RequestCommonTranslateTextCallback() {};
	virtual ~RequestCommonTranslateTextCallback() {};
public:
	virtual void OnTranslateText(bool success, const string& text, RequestCommonTranslateTextTask* task)
	{
		FileLog("httprequest", "JNI::OnTranslateText( success : %s, task : %p )", success?"true":"false", task);
		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		jobject callbackObj = gCallbackMap.Erase((long)task);
		if(callbackObj != NULL)
		{
			jclass callbackCls = env->GetObjectClass(callbackObj);
			if(callbackCls != NULL)
			{
				string signature = "(JZ";
				signature += "Ljava/lang/String;";
				signature += ")V";
				jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnTranslateText", signature.c_str());
				if(callbackMethod != NULL){
					jstring jtext = env->NewStringUTF(text.c_str());
					jlong id = (jlong)task;
					env->CallVoidMethod(callbackObj, callbackMethod, id, success, jtext);
					env->DeleteLocalRef(jtext);
				}
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnTranslateText() task:%p, finish", task);
	}
};

RequestCommonTranslateTextCallback gRequestCommonTranslateTextCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniCommon
 * Method:    TranslateText
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnTranslateTextCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniCommon_TranslateText
  (JNIEnv *env, jclass cls, jstring appId, jstring from, jstring to, jstring text, jobject callback)
{
	RequestOperator* request = new RequestOperator;

	RequestCommonTranslateTextTask* task = new RequestCommonTranslateTextTask;
	string strAppId = JString2String(env, appId);
	string strFrom = JString2String(env, from);
	string strTo = JString2String(env, to);
	string strText = JString2String(env, text);
	task->SetParam(strAppId, strFrom, strTo, strText);
	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestCommonTranslateTextCallback);
	task->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);

	jobject callbackObj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, callbackObj);

	FileLog("httprequest", "JNI::TranslateText() id:%p, callbackObj:%p", task, callbackObj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}
