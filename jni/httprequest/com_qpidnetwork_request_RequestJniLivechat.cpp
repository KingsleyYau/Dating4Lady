#include "com_qpidnetwork_request_RequestJniLivechat.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"

#include "RequestLCInviteTemplateTask.h"
#include "RequestLCSystemInviteTemplateTask.h"
#include "RequestLCAddInviteTemplateTask.h"
#include "RequestOperator.h"
#include "RequestLCDelCustomTemplateTask.h"

/******************************** 5.1 查询个人邀请模板 ***************************/
class RequestLCInviteTemplateCallback : public IRequestLCInviteTemplateCallback{
	void OnInviteTemplate(bool success, const string& errnum, const string& errmsg, const LiveChatInviteTemplateList& theList, RequestLCInviteTemplateTask* task){
		FileLog("httprequest", "JNI::OnInviteTemplate( success : %s, task : %p )", success?"true":"false", task);
		JNIEnv *env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if(env == NULL ){
			iRet = gJavaVM->AttachCurrentThread((JNIEnv**)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnInviteTemplate( iRet : %d )", iRet);

		jobjectArray jItemArray = NULL;
		JavaItemMap :: iterator itr = gJavaItemMap.find(LIVE_CHAT_CUSTOM_TEMPLATE_CLASS);
		if(itr != gJavaItemMap.end()){
			FileLog("httprequest", "JNI::OnInviteTemplate( itr != gJavaItemMap.end() )");
			jclass cls = env->GetObjectClass(itr->second);
			jmethodID init = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;Ljava/lang/String;I)V");
			if(theList.size() > 0){
				FileLog("httprequest", "JNI::OnInviteTemplate theList size: %d", theList.size());
				jItemArray = env->NewObjectArray(theList.size(), cls, NULL);
				jint iItrIndex = 0;
				for(LiveChatInviteTemplateList :: const_iterator itemItr = theList.begin(); itemItr != theList.end(); itemItr++, iItrIndex++){
					jstring tempId = env->NewStringUTF(itemItr->tempId.c_str());
					jstring tempContent = env->NewStringUTF(itemItr->tempContent.c_str());
					jobject jItem = env->NewObject(cls, init, tempId, tempContent, (jint)itemItr->tempStatus);
					env->SetObjectArrayElement(jItemArray, iItrIndex, jItem);
					env->DeleteLocalRef(tempId);
					env->DeleteLocalRef(tempContent);
					env->DeleteLocalRef(jItem);
				}

			}
		}

		/*callback object*/
		jobject callBackObject = gCallbackMap.Erase((long)task);
		if(callBackObject != NULL){
			jclass callBackCls = env->GetObjectClass(callBackObject);
			string signature = "(ZLjava/lang/String;Ljava/lang/String;";
			signature += "[L";
			signature += LIVE_CHAT_CUSTOM_TEMPLATE_CLASS;
			signature += ";";
			signature += ")V";
			jmethodID callbackMethod = env->GetMethodID(callBackCls, "onCustomTemplate", signature.c_str());
			FileLog("httprequest", "JNI::OnInviteTemplate( callback : %p, signature : %s )",
						callbackMethod, signature.c_str());
			if(callbackMethod != NULL){
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				env->CallVoidMethod(callBackObject, callbackMethod, success, jerrno, jerrmsg, jItemArray);

				env->DeleteGlobalRef(callBackObject);
				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
		}

		if(jItemArray != NULL){
			env->DeleteLocalRef(jItemArray);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}

	}
};

RequestLCInviteTemplateCallback gRequestLCInviteTemplateCallback;

JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_GetMyCustomTemplate
  (JNIEnv *env, jclass cls, jobject callback){

	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestLCInviteTemplateTask* task = new RequestLCInviteTemplateTask();
	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCInviteTemplateCallback);
	task->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);

	jobject callbackObj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, callbackObj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);
	request->Start();

	return (long)task;
}

/*********************************** 5.2 查询系统模板列表 ***************************************/

class RequestLCSystemInviteTemplateCallback : public IRequestLCSystemInviteTemplateCallback{
	void OnSystemInviteTemplate(bool success, const string& errnum, const string& errmsg, const list<string>& theList, RequestLCSystemInviteTemplateTask* task){
		FileLog("httprequest", "JNI::OnSystemInviteTemplate( success : %s, task : %p )", success?"true":"false", task);
		JNIEnv *env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if(env == NULL){
			iRet = gJavaVM->AttachCurrentThread((JNIEnv**)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnSystemInviteTemplate( iRet : %d )", iRet);

		jobjectArray jItemArray;
		if(theList.size() > 0){
			jItemArray = env->NewObjectArray(theList.size(), env->FindClass("java/lang/String"), NULL);
			jint iListIndex = 0;
			for(list<string> :: const_iterator itr = theList.begin(); itr != theList.end(); itr++, iListIndex++){
				jstring templateContent = env->NewStringUTF(itr->c_str());
				env->SetObjectArrayElement(jItemArray, iListIndex, templateContent);
				env->DeleteLocalRef(templateContent);
			}
		}

		/*** callback *******/
		jobject callbackObj = gCallbackMap.Erase((long)task);
		if(callbackObj != NULL){
			jclass callbackCls = env->GetObjectClass(callbackObj);
			string signature = "(ZLjava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V";
			jmethodID callbackMethod = env->GetMethodID(callbackCls, "onSystemTemplate", signature.c_str());
			FileLog("httprequest", "JNI::OnSystemInviteTemplate( callback : %p, signature : %s )",
									callbackMethod, signature.c_str());
			if(callbackMethod != NULL){
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				env->CallVoidMethod(callbackObj, callbackMethod, success, jerrno, jerrmsg, jItemArray);
				if(callbackObj != NULL){
					env->DeleteGlobalRef(callbackObj);
				}
				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
		}

		if(jItemArray != NULL){
			env->DeleteLocalRef(jItemArray);
		}

		if(iRet == JNI_OK){
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestLCSystemInviteTemplateCallback gRequestLCSystemInviteTemplateCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    GetSystemTemplate
 * Signature: (Lcom/qpidnetwork/request/OnLCSystemTemplateCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_GetSystemTemplate
  (JNIEnv *env, jclass cls, jobject callback){

	RequestOperator * request = new RequestOperator;

	RequestLCSystemInviteTemplateTask* task = new RequestLCSystemInviteTemplateTask;
	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCSystemInviteTemplateCallback);
	task->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);

	jobject callbackObj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, callbackObj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap :: value_type((long)task, true));
	gRequestMap.insert(RequestMap :: value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);
	request->Start();

	return (long)task;
}


/******************************* 新建邀请模板  *****************************/
class RequestLCAddInviteTemplateCallback : public IRequestLCAddInviteTemplateCallback{
	void OnAddInviteTemplate(bool success, const string& errnum, const string& errmsg, RequestLCAddInviteTemplateTask* task){
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);

		if(env == NULL){
			iRet = gJavaVM->AttachCurrentThread((JNIEnv**)&env, NULL);
		}

		/*callback*/
		jobject callbackObj = gCallbackMap.Erase((long)task);

		if(callbackObj != NULL){
			jclass callbackCls = env->GetObjectClass(callbackObj);
			string signature = "(ZLjava/lang/String;Ljava/lang/String;)V";
			jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnRequest", signature.c_str());
			if(callbackMethod != NULL){
				jstring jerrnum = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				env->CallVoidMethod(callbackObj, callbackMethod, success, jerrnum, jerrmsg);
				env->DeleteGlobalRef(callbackObj);
				env->DeleteLocalRef(jerrnum);
				env->DeleteLocalRef(jerrmsg);
			}
		}

		if(iRet == JNI_OK){
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestLCAddInviteTemplateCallback gRequestLCAddInviteTemplateCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    AddCustomTemplate
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_AddCustomTemplate
  (JNIEnv *env, jclass cls, jstring tempContent, jobject callback){

	const char* cpTempContent = env->GetStringUTFChars(tempContent, 0);
	RequestOperator* request = new RequestOperator;

	RequestLCAddInviteTemplateTask* task = new RequestLCAddInviteTemplateTask;
	task->Init(&gHttpRequestManager);
	task->SetParam(cpTempContent);
	task->SetCallback(&gRequestLCAddInviteTemplateCallback);
	task->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);

	jobject callbackObj = env->NewGlobalRef(callback);
	long id = (long)task;
	gCallbackMap.Insert(id, callbackObj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);
	request->Start();

	env->ReleaseStringUTFChars(tempContent, cpTempContent);

	return (long)task;
}

/*************************** 5.4. 删除个人自定义邀请模板  ****************************/

class RequestLCDelCustomTemplateTaskCallback : public IRequestLCDelCustomTemplateTaskCallback{
	void onDelCustomTemplate(bool success, const string& errnum, const string& errmsg, RequestLCDelCustomTemplateTask* task){
		FileLog("httprequest", "JNI::onDelCustomTemplate( success : %s, task : %p )", success?"true":"false", task);
		JNIEnv *env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if(env == NULL){
			iRet = gJavaVM->AttachCurrentThread((JNIEnv**)&env, NULL);
		}
		FileLog("httprequest", "JNI::onDelCustomTemplate( iRet : %d )", iRet);

		jobject callbackObj = gCallbackMap.Erase((long)task);
		if(callbackObj != NULL){
			jclass callbackCls = env->GetObjectClass(callbackObj);
			if(callbackCls != NULL){
				string signature = "(ZLjava/lang/String;Ljava/lang/String;)V";
				jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnRequest", signature.c_str());
				if(callbackMethod != NULL){
					jstring jerrnum = env->NewStringUTF(errnum.c_str());
					jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
					env->CallVoidMethod(callbackObj, callbackMethod, success, jerrnum, jerrmsg);
					env->DeleteLocalRef(jerrnum);
					env->DeleteLocalRef(jerrmsg);
				}
			}
			env->DeleteGlobalRef(callbackObj);
		}

		if(iRet == JNI_OK){
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestLCDelCustomTemplateTaskCallback gRequestLCDelCustomTemplateTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    DelCustomTemplates
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_DelCustomTemplates
  (JNIEnv *env, jclass cls, jstring tempIds, jobject callback){
	RequestOperator* request = new RequestOperator;

	RequestLCDelCustomTemplateTask* task = new RequestLCDelCustomTemplateTask;
	task->Init(&gHttpRequestManager);
	const char* cpTempId = env->GetStringUTFChars(tempIds, NULL);
	task->setParams(cpTempId);
	task->setCallback(&gRequestLCDelCustomTemplateTaskCallback);
	task->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);

	jobject callbackObj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, callbackObj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);
	request->Start();

	env->ReleaseStringUTFChars(tempIds, cpTempId);
	return (long)task;
}


