#include "com_qpidnetwork_request_RequestJniLivechat.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"

#include <AndroidCommon/DeviceJniIntToType.h>

#include "RequestLCInviteTemplateTask.h"
#include "RequestLCSystemInviteTemplateTask.h"
#include "RequestLCAddInviteTemplateTask.h"
#include "RequestOperator.h"
#include "RequestLCDelCustomTemplateTask.h"
#include "RequestLCLadyChatListTask.h"
#include "RequestLCLadyInviteMsgTask.h"
#include "RequestLCPhotoListTask.h"
#include "RequestLCSendPhotoTask.h"
#include "RequestLCCheckSendPhotoTask.h"
#include "RequestLCGetPhotoTask.h"
#include "RequestLCSendVoiceTask.h"
#include "RequestLCGetVoiceTask.h"

// video
#include "RequestLCVideoListTask.h"
#include "RequestLCGetVideoPhotoTask.h"
#include "RequestLCGetVideoTask.h"
#include "RequestLCCheckSendVideoTask.h"
#include "RequestLCSendVideoTask.h"

#include "RequestLCCheckFunctionsTask.h"
#include "RequestLCMagicIconConfigTask.h"

/******************************** 5.1 查询个人邀请模板 ***************************/
class RequestLCInviteTemplateCallback : public IRequestLCInviteTemplateCallback{
	void OnInviteTemplate(bool success, const string& errnum, const string& errmsg, const LiveChatInviteTemplateList& theList, RequestLCInviteTemplateTask* task){
		FileLog("httprequest", "JNI::OnInviteTemplate( success : %s, task : %p )", success?"true":"false", task);
		JNIEnv *env = NULL;
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
			jmethodID init = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;Ljava/lang/String;IZ)V");
			if(theList.size() > 0){
				FileLog("httprequest", "JNI::OnInviteTemplate theList size: %d", theList.size());
				jItemArray = env->NewObjectArray(theList.size(), cls, NULL);
				jint iItrIndex = 0;
				for(LiveChatInviteTemplateList :: const_iterator itemItr = theList.begin(); itemItr != theList.end(); itemItr++, iItrIndex++){
					jstring tempId = env->NewStringUTF(itemItr->tempId.c_str());
					jstring tempContent = env->NewStringUTF(itemItr->tempContent.c_str());
					jobject jItem = env->NewObject(cls, init, tempId, tempContent, (jint)itemItr->tempStatus, itemItr->autoFlag);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/*********************************** 5.2 查询系统模板列表 ***************************************/

class RequestLCSystemInviteTemplateCallback : public IRequestLCSystemInviteTemplateCallback{
	void OnSystemInviteTemplate(bool success, const string& errnum, const string& errmsg, const list<string>& theList, RequestLCSystemInviteTemplateTask* task){
		FileLog("httprequest", "JNI::OnSystemInviteTemplate( success : %s, task : %p )", success?"true":"false", task);
		JNIEnv *env = NULL;
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
				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
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
				env->DeleteLocalRef(jerrnum);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
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
 * Signature: (Ljava/lang/String;ZLcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_AddCustomTemplate
  (JNIEnv *env, jclass cls, jstring tempContent, jboolean isInviteAssistant, jobject callback){

	string strTempContent = JString2String(env, tempContent);
	RequestOperator* request = new RequestOperator;

	RequestLCAddInviteTemplateTask* task = new RequestLCAddInviteTemplateTask;
	task->Init(&gHttpRequestManager);
	task->SetParam(strTempContent, isInviteAssistant);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/*************************** 5.4. 删除个人自定义邀请模板  ****************************/

class RequestLCDelCustomTemplateTaskCallback : public IRequestLCDelCustomTemplateTaskCallback{
	void onDelCustomTemplate(bool success, const string& errnum, const string& errmsg, RequestLCDelCustomTemplateTask* task){
		FileLog("httprequest", "JNI::onDelCustomTemplate( success : %s, task : %p )", success?"true":"false", task);
		JNIEnv *env = NULL;
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
	string strTempId = JString2String(env, tempIds);
	task->setParams(strTempId);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/******************************* 查询男士聊天历史  *****************************/
jobjectArray GetArrayWithChatListItem(JNIEnv* env, const LiveChatConversationList& theList)
{
	jobjectArray jItemArray = NULL;
	JavaItemMap :: iterator itr = gJavaItemMap.find(LIVE_CHAT_LADY_CHAT_LIST_CLASS);
	if(itr != gJavaItemMap.end())
	{
		FileLog("httprequest", "JNI::GetArrayWithChatListItem( itr != gJavaItemMap.end() )");
		jclass cls = env->GetObjectClass(itr->second);
		string strInit = "(";
		strInit += "Ljava/lang/String;";	// inviteId		邀请ID
		strInit += "Ljava/lang/String;";	// startTime	开始时间
		strInit += "Ljava/lang/String;";	// duringTime	会话时长
		strInit += "Ljava/lang/String;";	// manId		男士ID
		strInit += "Ljava/lang/String;";	// manName		男士名称
		strInit += "Ljava/lang/String;";	// womanName	女士英语名
		strInit += "Ljava/lang/String;";	// cnName		女士名称
		strInit += "Ljava/lang/String;";	// transId		翻译ID
		strInit += "Ljava/lang/String;";	// transName	翻译名称
		strInit += ")V";
		jmethodID init = env->GetMethodID(cls, "<init>", strInit.c_str());
		if(theList.size() > 0)
		{
			FileLog("httprequest", "JNI::GetArrayWithChatListItem theList size: %d", theList.size());
			jItemArray = env->NewObjectArray(theList.size(), cls, NULL);
			int iItrIndex = 0;
			for(LiveChatConversationList::const_iterator itemItr = theList.begin();
					itemItr != theList.end();
					itemItr++, iItrIndex++)
			{
				jstring inviteId = env->NewStringUTF(itemItr->inviteId.c_str());
				jstring startTime = env->NewStringUTF(itemItr->startTime.c_str());
				jstring duringTime = env->NewStringUTF(itemItr->duringTime.c_str());
				jstring manId = env->NewStringUTF(itemItr->manId.c_str());
				jstring manName = env->NewStringUTF(itemItr->manName.c_str());
				jstring womanName = env->NewStringUTF(itemItr->womanName.c_str());
				jstring cnName = env->NewStringUTF(itemItr->cnName.c_str());
				jstring transId = env->NewStringUTF(itemItr->translatorId.c_str());
				jstring transName = env->NewStringUTF(itemItr->translatorName.c_str());

				jobject jItem = env->NewObject(cls
									, init
									, inviteId
									, startTime
									, duringTime
									, manId
									, manName
									, womanName
									, cnName
									, transId
									, transName);

				env->SetObjectArrayElement(jItemArray, iItrIndex, jItem);

				env->DeleteLocalRef(inviteId);
				env->DeleteLocalRef(startTime);
				env->DeleteLocalRef(duringTime);
				env->DeleteLocalRef(manId);
				env->DeleteLocalRef(manName);
				env->DeleteLocalRef(womanName);
				env->DeleteLocalRef(cnName);
				env->DeleteLocalRef(transId);
				env->DeleteLocalRef(transName);

				env->DeleteLocalRef(jItem);
			}

		}
	}

	return jItemArray;
}

class RequestLCLadyChatListCallback : public IRequestLCLadyChatListCallback
{
public:
	virtual void OnLadyChatList(bool success
								, const string& errnum
								, const string& errmsg
								, const LiveChatConversationList& theList
								, RequestLCLadyChatListTask* task)
	{
		FileLog("httprequest", "JNI::OnLadyChatList( success : %s, task : %p )", success?"true":"false", task);
		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		jobject callbackObj = gCallbackMap.Erase((long)task);
		if(callbackObj != NULL)
		{
			jclass callbackCls = env->GetObjectClass(callbackObj);
			if(callbackCls != NULL)
			{
				jobjectArray jItemArray = GetArrayWithChatListItem(env, theList);

				string signature = "(ZLjava/lang/String;Ljava/lang/String;";
				signature += "[L";
				signature += LIVE_CHAT_LADY_CHAT_LIST_CLASS;
				signature += ";";
				signature += ")V";
				jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnLCGetChatList", signature.c_str());
				if(callbackMethod != NULL){
					jstring jerrnum = env->NewStringUTF(errnum.c_str());
					jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
					env->CallVoidMethod(callbackObj, callbackMethod, success, jerrnum, jerrmsg, jItemArray);
					env->DeleteLocalRef(jerrnum);
					env->DeleteLocalRef(jerrmsg);
				}
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnLadyChatList() task:%p, finish", task);
	}
};
RequestLCLadyChatListCallback gRequestLCLadyChatListCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    GetChatList
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnGetChatListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_GetChatList
  (JNIEnv *env, jclass cls, jstring targetId, jobject callback)
{
	RequestOperator* request = new RequestOperator;

	RequestLCLadyChatListTask* task = new RequestLCLadyChatListTask;
	task->Init(&gHttpRequestManager);
	string strTargetId = JString2String(env, targetId);
	task->SetParam(strTargetId);
	task->SetCallback(&gRequestLCLadyChatListCallback);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}


/******************************* 查询聊天消息列表 *****************************/
jobjectArray GetArrayWithListRecord(JNIEnv* env, const LiveChatRecordList& recordList)
{
	FileLog("httprequest", "LiveChat.Native::GetArrayWithListRecord() begin, recordList.size:%d", recordList.size());
	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LIVE_CHAT_LADY_INVITE_MSG_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		FileLog("httprequest", "LiveChat.Native::GetArrayWithListRecord() cls:%p", cls);
		if( NULL != cls && recordList.size() > 0 )
		{
			jItemArray = env->NewObjectArray(recordList.size(), cls, NULL);
			int i = 0;
			for(LiveChatRecordList::const_iterator itr = recordList.begin();
					itr != recordList.end();
					itr++, i++)
			{
				jmethodID init = env->GetMethodID(cls, "<init>", "("
						"I"						// toflag
						"I"						// adddate
						"I"						// messageType
						"Ljava/lang/String;"	// textMsg
						"Ljava/lang/String;"	// inviteMsg
						"Ljava/lang/String;"	// warningMsg
						"Ljava/lang/String;"	// emotionId
						"Ljava/lang/String;"	// photoId
						"Ljava/lang/String;"	// photoSendId
						"Ljava/lang/String;"	// photoDesc
						"Z"						// photoCharge
						"Ljava/lang/String;"	// voiceId
						"Ljava/lang/String;"	// voiceType
						"I"						// voiceTime
						"Ljava/lang/String;"	// videoId
						"Ljava/lang/String;"	// videoSendId
						"Ljava/lang/String;"	// videoDesc
						"Z"						// videoCharge
						"Ljava/lang/String;"    // magicIconId
						")V");

				jstring textMsg = env->NewStringUTF(itr->textMsg.c_str());
				jstring inviteMsg = env->NewStringUTF(itr->inviteMsg.c_str());
				jstring warningMsg = env->NewStringUTF(itr->warningMsg.c_str());
				jstring emotionId = env->NewStringUTF(itr->emotionId.c_str());
				jstring photoId = env->NewStringUTF(itr->photoId.c_str());
				jstring photoSendId = env->NewStringUTF(itr->photoSendId.c_str());
				jstring photoDesc = env->NewStringUTF(itr->photoDesc.c_str());
				jstring voiceId = env->NewStringUTF(itr->voiceId.c_str());
				jstring voiceType = env->NewStringUTF(itr->voiceType.c_str());
				jstring videoId = env->NewStringUTF(itr->videoId.c_str());
				jstring videoSendId = env->NewStringUTF(itr->videoSendId.c_str());
				jstring videoDesc = env->NewStringUTF(itr->videoDesc.c_str());
				jstring magicIconId = env->NewStringUTF(itr->magicIconId.c_str());

				jobject item = env->NewObject(cls, init,
						itr->toflag,
						itr->addtime,
						itr->messageType,
						textMsg,
						inviteMsg,
						warningMsg,
						emotionId,
						photoId,
						photoSendId,
						photoDesc,
						itr->photoCharge,
						voiceId,
						voiceType,
						itr->voiceTime,
						videoId,
						videoSendId,
						videoDesc,
						itr->videoCharge,
						magicIconId
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(textMsg);
				env->DeleteLocalRef(inviteMsg);
				env->DeleteLocalRef(warningMsg);
				env->DeleteLocalRef(emotionId);
				env->DeleteLocalRef(photoId);
				env->DeleteLocalRef(photoSendId);
				env->DeleteLocalRef(photoDesc);
				env->DeleteLocalRef(voiceId);
				env->DeleteLocalRef(voiceType);
				env->DeleteLocalRef(videoId);
				env->DeleteLocalRef(videoSendId);
				env->DeleteLocalRef(videoDesc);
				env->DeleteLocalRef(magicIconId);

				env->DeleteLocalRef(item);
			}
		}
	}

	FileLog("httprequest", "LiveChat.Native::GetArrayWithListRecord() end");
	return jItemArray;
}

class RequestLCLadyInviteMsgCallback : public IRequestLCLadyInviteMsgCallback
{
public:
	virtual void OnLadyInviteMsg(bool success
								, const string& errnum
								, const string& errmsg
								, int dbTime
								, const LiveChatRecordList& theList
								, const string& inviteId
								, RequestLCLadyInviteMsgTask* task)
	{
		FileLog("httprequest", "JNI::OnLadyInviteMsg( success : %s, task : %p )", success?"true":"false", task);
		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		jobject callbackObj = gCallbackMap.Erase((long)task);
		if(callbackObj != NULL)
		{
			jclass callbackCls = env->GetObjectClass(callbackObj);
			if(callbackCls != NULL)
			{
				jobjectArray jItemArray = GetArrayWithListRecord(env, theList);

				string signature = "(ZLjava/lang/String;Ljava/lang/String;I";
				signature += "[L";
				signature += LIVE_CHAT_LADY_INVITE_MSG_CLASS;
				signature += ";";
				signature += "Ljava/lang/String;";
				signature += ")V";
				jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnQueryChatRecord", signature.c_str());
				if(callbackMethod != NULL){
					jstring jerrnum = env->NewStringUTF(errnum.c_str());
					jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
					jstring jinviteId = env->NewStringUTF(inviteId.c_str());
					jint jDbTime = dbTime;
					env->CallVoidMethod(callbackObj, callbackMethod, success, jerrnum, jerrmsg, jDbTime, jItemArray, jinviteId);
					env->DeleteLocalRef(jerrnum);
					env->DeleteLocalRef(jerrmsg);
					env->DeleteLocalRef(jinviteId);
				}
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnLadyInviteMsg() task:%p, finish", task);
	}
};
RequestLCLadyInviteMsgCallback gRequestLCLadyInviteMsgCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    QueryChatRecord
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnQueryChatRecordCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_QueryChatRecord
  (JNIEnv *env, jclass cls, jstring inviteId, jobject callback)
{
	RequestOperator* request = new RequestOperator;

	RequestLCLadyInviteMsgTask* task = new RequestLCLadyInviteMsgTask;
	task->Init(&gHttpRequestManager);
	string strInviteId = JString2String(env, inviteId);
	task->SetParam(strInviteId);
	task->SetCallback(&gRequestLCLadyInviteMsgCallback);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}


/******************************* 获取私密照列表 *****************************/
jobjectArray GetArrayWithAlbumList(JNIEnv* env, const LiveChatAlbumList& theList)
{
	FileLog("httprequest", "LiveChat.Native::GetArrayWithAlbumList() begin, theList.size:%d", theList.size());
	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LIVE_CHAT_GET_PHOTO_LIST_ALBUM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		FileLog("httprequest", "LiveChat.Native::GetArrayWithAlbumList() cls:%p", cls);
		if( NULL != cls && theList.size() > 0 )
		{
			jItemArray = env->NewObjectArray(theList.size(), cls, NULL);
			int i = 0;
			for(LiveChatAlbumList::const_iterator itr = theList.begin();
					itr != theList.end();
					itr++, i++)
			{
				jmethodID init = env->GetMethodID(cls, "<init>", "("
						"Ljava/lang/String;"	// albumId
						"Ljava/lang/String;"	// title
						")V");

				jstring albumId = env->NewStringUTF(itr->albumId.c_str());
				jstring title = env->NewStringUTF(itr->title.c_str());

				jobject item = env->NewObject(cls, init,
						albumId,
						title
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(albumId);
				env->DeleteLocalRef(title);

				env->DeleteLocalRef(item);
			}
		}
	}

	FileLog("httprequest", "LiveChat.Native::GetArrayWithAlbumList() end");
	return jItemArray;
}

jobjectArray GetArrayWithPhotoList(JNIEnv* env, const LiveChatPhotoList& theList)
{
	FileLog("httprequest", "LiveChat.Native::GetArrayWithPhotoList() begin, theList.size:%d", theList.size());
	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LIVE_CHAT_GET_PHOTO_LIST_PHOTO_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		FileLog("httprequest", "LiveChat.Native::GetArrayWithPhotoList() cls:%p", cls);
		if( NULL != cls && theList.size() > 0 )
		{
			jItemArray = env->NewObjectArray(theList.size(), cls, NULL);
			int i = 0;
			for(LiveChatPhotoList::const_iterator itr = theList.begin();
					itr != theList.end();
					itr++, i++)
			{
				jmethodID init = env->GetMethodID(cls, "<init>", "("
						"Ljava/lang/String;"	// photoId
						"Ljava/lang/String;"	// albumId
						"Ljava/lang/String;"	// title
						")V");

				jstring photoId = env->NewStringUTF(itr->photoId.c_str());
				jstring albumId = env->NewStringUTF(itr->albumId.c_str());
				jstring title = env->NewStringUTF(itr->title.c_str());

				jobject item = env->NewObject(cls, init,
						photoId,
						albumId,
						title
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(photoId);
				env->DeleteLocalRef(albumId);
				env->DeleteLocalRef(title);

				env->DeleteLocalRef(item);
			}
		}
	}

	FileLog("httprequest", "LiveChat.Native::GetArrayWithPhotoList() end");
	return jItemArray;
}

class RequestLCPhotoListCallback : public IRequestLCPhotoListCallback
{
public:
	virtual void OnPhotoList(bool success
							, const string& errnum
							, const string& errmsg
							, const LiveChatAlbumList& albumList
							, const LiveChatPhotoList& photoList
							, RequestLCPhotoListTask* task)
	{
		FileLog("httprequest", "JNI::OnPhotoList( success : %s, task : %p )", success?"true":"false", task);
		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		jobject callbackObj = gCallbackMap.Erase((long)task);
		if(callbackObj != NULL)
		{
			jclass callbackCls = env->GetObjectClass(callbackObj);
			if(callbackCls != NULL)
			{
				jobjectArray jAlbumArray = GetArrayWithAlbumList(env, albumList);
				jobjectArray jPhotoArray = GetArrayWithPhotoList(env, photoList);

				string signature = "(ZLjava/lang/String;Ljava/lang/String;";
				signature += "[L";
				signature += LIVE_CHAT_GET_PHOTO_LIST_ALBUM_CLASS;
				signature += ";";
				signature += "[L";
				signature += LIVE_CHAT_GET_PHOTO_LIST_PHOTO_CLASS;
				signature += ";";
				signature += ")V";
				jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnLCGetPhotoList", signature.c_str());
				if(callbackMethod != NULL){
					jstring jerrnum = env->NewStringUTF(errnum.c_str());
					jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
					env->CallVoidMethod(callbackObj, callbackMethod, success, jerrnum, jerrmsg, jAlbumArray, jPhotoArray);
					env->DeleteLocalRef(jerrnum);
					env->DeleteLocalRef(jerrmsg);
				}
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnPhotoList() task:%p, finish", task);
	}
};
RequestLCPhotoListCallback gRequestLCPhotoListCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    GetPhotoList
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnGetPhotoListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_GetPhotoList
  (JNIEnv *env, jclass cls, jstring sid, jstring userId, jobject callback)
{
	RequestOperator* request = new RequestOperator;

	RequestLCPhotoListTask* task = new RequestLCPhotoListTask;
	string strSid = JString2String(env, sid);
	string strUserId = JString2String(env, userId);
	task->Init(&gHttpRequestManager);
	task->SetParam(strSid, strUserId);
	task->SetCallback(&gRequestLCPhotoListCallback);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/******************************* 发送私密照片 *****************************/
class RequestLCSendPhotoCallback : public IRequestLCSendPhotoCallback
{
public:
	virtual void OnSendPhoto(bool success
								, const string& errnum
								, const string& errmsg
								, const string& sendId
								, RequestLCSendPhotoTask* task)
	{
		FileLog("httprequest", "JNI::OnSendPhoto( success : %s, task : %p )", success?"true":"false", task);
		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		jobject callbackObj = gCallbackMap.Erase((long)task);
		if(callbackObj != NULL)
		{
			jclass callbackCls = env->GetObjectClass(callbackObj);
			if(callbackCls != NULL)
			{
				string signature = "(JZLjava/lang/String;Ljava/lang/String;";
				signature += "Ljava/lang/String;";
				signature += ")V";
				jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnLCSendPhoto", signature.c_str());
				if(callbackMethod != NULL){
					jstring jerrnum = env->NewStringUTF(errnum.c_str());
					jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
					jstring jsendId = env->NewStringUTF(sendId.c_str());
					jlong id = (jlong)task;
					env->CallVoidMethod(callbackObj, callbackMethod, id, success, jerrnum, jerrmsg, jsendId);
					env->DeleteLocalRef(jerrnum);
					env->DeleteLocalRef(jerrmsg);
					env->DeleteLocalRef(jsendId);
				}
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnSendPhoto() task:%p, finish", task);
	}
};
RequestLCSendPhotoCallback gRequestLCSendPhotoCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    SendPhoto
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnLCSendPhotoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_SendPhoto
  (JNIEnv *env, jclass cls, jstring targetId, jstring inviteId, jstring photoId, jstring sid, jstring userId, jobject callback)
{
	RequestOperator* request = new RequestOperator;

	RequestLCSendPhotoTask* task = new RequestLCSendPhotoTask;
	string strTargetId = JString2String(env, targetId);
	string strInviteId = JString2String(env, inviteId);
	string strPhotoId = JString2String(env, photoId);
	string strSid = JString2String(env, sid);
	string strUserId = JString2String(env, userId);
	task->SetParam(strTargetId, strInviteId, strPhotoId, strSid, strUserId);
	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCSendPhotoCallback);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/******************************* 检测女士是否可发私密照 *****************************/
class RequestLCCheckSendPhotoCallback : public IRequestLCCheckSendPhotoCallback
{
public:
	virtual void OnCheckSendPhoto(LC_CHECKPHOTO_TYPE result
								, const string& errnum
								, const string& errmsg
								, RequestLCCheckSendPhotoTask* task)
	{
		FileLog("httprequest", "JNI::OnCheckSendPhoto( result : %d, task : %p )", result, task);

		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		/*callback*/
		jobject callbackObj = gCallbackMap.Erase((long)task);

		if(callbackObj != NULL){
			jclass callbackCls = env->GetObjectClass(callbackObj);
			string signature = "(JILjava/lang/String;Ljava/lang/String;)V";
			jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnLCCheckSendPhoto", signature.c_str());
			if(callbackMethod != NULL){
				jstring jerrnum = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jlong id = (jlong)task;
				env->CallVoidMethod(callbackObj, callbackMethod, id, result, jerrnum, jerrmsg);
				env->DeleteLocalRef(jerrnum);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnCheckSendPhoto() task:%p, finish", task);
	}
};
RequestLCCheckSendPhotoCallback gRequestLCCheckSendPhotoCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    CheckSendPhoto
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_CheckSendPhoto
  (JNIEnv *env, jclass cls, jstring targetId, jstring inviteId, jstring photoId, jstring sid, jstring userId, jobject callback)
{
	RequestOperator* request = new RequestOperator;

	RequestLCCheckSendPhotoTask* task = new RequestLCCheckSendPhotoTask;
	string strTargetId = JString2String(env, targetId);
	string strInviteId = JString2String(env, inviteId);
	string strPhotoId = JString2String(env, photoId);
	string strSid = JString2String(env, sid);
	string strUserId = JString2String(env, userId);
	task->SetParam(strTargetId, strInviteId, strPhotoId, strSid, strUserId);
	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCCheckSendPhotoCallback);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/******************************* 获取对方私密照片 *****************************/
class RequestLCGetPhotoCallback : public IRequestLCGetPhotoCallback
{
public:
	virtual void OnGetPhoto(bool success
							, const string& errnum
							, const string& errmsg
							, const string& photoId
							, GETPHOTO_SIZE_TYPE sizeType
							, GETPHOTO_MODE_TYPE modeType
							, const string& filePath
							, RequestLCGetPhotoTask* task)
	{
		FileLog("httprequest", "JNI::OnGetPhoto( success : %s, task : %p )", success?"true":"false", task);
		FileLog("httprequest", "JNI::OnGetPhoto() photoId:%s, size:%d, mode:%d, filePath:%s"
				, photoId.c_str(), sizeType, modeType, filePath.c_str());

		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		/*callback*/
		jobject callbackObj = gCallbackMap.Erase((long)task);

		if(callbackObj != NULL){
			jclass callbackCls = env->GetObjectClass(callbackObj);
			string signature = "(JZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;IILjava/lang/String;)V";
			jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnLCGetPhoto", signature.c_str());
			if(callbackMethod != NULL){
				jstring jerrnum = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jstring jphotoId = env->NewStringUTF(photoId.c_str());
				jstring jfilePath = env->NewStringUTF(filePath.c_str());
				jlong id = (jlong)task;
				env->CallVoidMethod(callbackObj, callbackMethod, id, success, jerrnum, jerrmsg, jphotoId, sizeType, modeType, jfilePath);
				env->DeleteLocalRef(jerrnum);
				env->DeleteLocalRef(jerrmsg);
				env->DeleteLocalRef(jphotoId);
				env->DeleteLocalRef(jfilePath);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnGetPhoto() task:%p, finish", task);
	}
};
RequestLCGetPhotoCallback gRequestLCGetPhotoCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    GetPhoto
 * Signature: (ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IILjava/lang/String;Lcom/qpidnetwork/request/OnLCGetPhotoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_GetPhoto
  (JNIEnv *env, jclass cls, jint toFlag, jstring targetId, jstring sid, jstring userId, jstring photoId, jint size, jint mode, jstring filePath, jobject callback)
{
	RequestOperator* request = new RequestOperator;

	RequestLCGetPhotoTask* task = new RequestLCGetPhotoTask;
	string strTargetId = JString2String(env, targetId);
	string strPhotoId = JString2String(env, photoId);
	string strSid = JString2String(env, sid);
	string strUserId = JString2String(env, userId);
	string strFilePath = JString2String(env, filePath);
	task->SetParam((GETPHOTO_TOFLAG_TYPE)toFlag
			, strTargetId
			, strSid
			, strUserId
			, strPhotoId
			, (GETPHOTO_SIZE_TYPE)size
			, (GETPHOTO_MODE_TYPE)mode
			, strFilePath);
	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCGetPhotoCallback);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/******************************* 上传语音文件 *****************************/
class RequestLCSendVoiceCallback : public IRequestLCSendVoiceCallback
{
public:
	virtual void OnSendVoice(bool success
							, const string& errnum
							, const string& errmsg
							, const string& voiceId
							, RequestLCSendVoiceTask* task)
	{
		FileLog("httprequest", "JNI::OnSendVoice( success : %s, task : %p )", success?"true":"false", task);

		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		/*callback*/
		jobject callbackObj = gCallbackMap.Erase((long)task);

		if(callbackObj != NULL){
			jclass callbackCls = env->GetObjectClass(callbackObj);
			string signature = "(JZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
			jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnLCUploadVoice", signature.c_str());
			if(callbackMethod != NULL){
				jstring jerrnum = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jstring jVoiceId = env->NewStringUTF(voiceId.c_str());
				jlong requestId = (jlong)task;
				env->CallVoidMethod(callbackObj, callbackMethod, requestId, success, jerrnum, jerrmsg, jVoiceId);
				env->DeleteLocalRef(jerrnum);
				env->DeleteLocalRef(jerrmsg);
				env->DeleteLocalRef(jVoiceId);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnSendVoice() task:%p, finish", task);
	}
};
RequestLCSendVoiceCallback gRequestLCSendVoiceCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    UploadVoice
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Lcom/qpidnetwork/request/OnLCUploadVoiceCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_UploadVoice
  (JNIEnv *env, jclass cls, jstring voiceCode, jstring inviteId, jstring userId, jstring targetId, jstring siteId, jstring fileType, jint voiceLength, jstring filePath, jobject callback)
{
	RequestOperator* request = new RequestOperator;

	RequestLCSendVoiceTask* task = new RequestLCSendVoiceTask;
	string strVoiceCode = JString2String(env, voiceCode);
	string strInviteId = JString2String(env, inviteId);
	string strUserId = JString2String(env, userId);
	string strTargetId = JString2String(env, targetId);
	string strSiteId = JString2String(env, siteId);
	string strFileType = JString2String(env, fileType);
	string strFilePath = JString2String(env, filePath);
	task->SetParam(strVoiceCode
			, strInviteId
			, strTargetId
			, strUserId
			, strFileType
			, voiceLength
			, strSiteId
			, strFilePath);
	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCSendVoiceCallback);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/******************************* 获取语音文件 *****************************/
class RequestLCGetVoiceCallback : public IRequestLCGetVoiceCallback
{
public:
	virtual void OnGetVoice(bool success
							, const string& errnum
							, const string& errmsg
							, const string& filePath
							, RequestLCGetVoiceTask* task)
	{
		FileLog("httprequest", "JNI::OnGetVoice( success : %s, task : %p )", success?"true":"false", task);
		FileLog("httprequest", "JNI::OnGetVoice() errnum:%s, errmsg:%s, filePath:%s", errnum.c_str(), errmsg.c_str(), filePath.c_str());

		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		/*callback*/
		jobject callbackObj = gCallbackMap.Erase((long)task);

		if(callbackObj != NULL){
			jclass callbackCls = env->GetObjectClass(callbackObj);
			string signature = "(JZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
			jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnLCPlayVoice", signature.c_str());
			FileLog("httprequest", "JNI::OnGetVoice() callbackObj:%p, callbackMethod:%p", callbackObj, callbackMethod);
			if(callbackMethod != NULL){
				jstring jerrnum = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jstring jFilePath = env->NewStringUTF(filePath.c_str());
				jlong requestId = (long)task;
				FileLog("httprequest", "JNI::OnGetVoice() callback begin, task:%p, errnum:%p, errmsg:%p, filePath:%p", task, jerrnum, jerrmsg, jFilePath);
				env->CallVoidMethod(callbackObj, callbackMethod, requestId, success, jerrnum, jerrmsg, jFilePath);
				FileLog("httprequest", "JNI::OnGetVoice() callback end");
				env->DeleteLocalRef(jerrnum);
				env->DeleteLocalRef(jerrmsg);
				env->DeleteLocalRef(jFilePath);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnGetVoice() task:%p, finish", task);
	}
};
RequestLCGetVoiceCallback gRequestLCGetVoiceCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    PlayVoice
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnLCPlayVoiceCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_PlayVoice
  (JNIEnv *env, jclass cls, jstring voiceId, jstring siteId, jstring filePath, jobject callback)
{
	RequestOperator* request = new RequestOperator;

	RequestLCGetVoiceTask* task = new RequestLCGetVoiceTask;
	string strVoiceId = JString2String(env, voiceId);
	string strSiteId = JString2String(env, siteId);
	string strFilePath = JString2String(env, filePath);
	task->SetParam(strVoiceId, strSiteId, strFilePath);
	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCGetVoiceCallback);
	task->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);

	jobject callbackObj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, callbackObj);

	FileLog("httprequest", "JNI::PlayVoice() id:%p, callbackObj:%p", task, callbackObj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}


/******************************* 5.13.	获取微视频列表 *****************************/
jobjectArray GetArrayWithGroupList(JNIEnv* env, const LiveChatVideoGroupList& theList)
{
	FileLog("httprequest", "LiveChat.Native::GetArrayWithGroupList() begin, theList.size:%d", theList.size());
	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LIVE_CHAT_GET_PHOTO_LIST_GROUP_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		FileLog("httprequest", "LiveChat.Native::GetArrayWithGroupList() cls:%p", cls);
		if( NULL != cls && theList.size() > 0 )
		{
			jItemArray = env->NewObjectArray(theList.size(), cls, NULL);
			int i = 0;
			for(LiveChatVideoGroupList::const_iterator itr = theList.begin();
					itr != theList.end();
					itr++, i++)
			{
				jmethodID init = env->GetMethodID(cls, "<init>", "("
						"Ljava/lang/String;"	// groupId
						"Ljava/lang/String;"	// groupTitle
						")V");

				jstring groupId = env->NewStringUTF(itr->groupId.c_str());
				jstring groupTitle = env->NewStringUTF(itr->groupTitle.c_str());

				jobject item = env->NewObject(cls, init,
						groupId,
						groupTitle
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(groupId);
				env->DeleteLocalRef(groupTitle);

				env->DeleteLocalRef(item);
			}
		}
	}

	FileLog("httprequest", "LiveChat.Native::GetArrayWithGroupList() end");
	return jItemArray;
}

jobjectArray GetArrayWithVideoList(JNIEnv* env, const LiveChatVideoList& theList)
{
	FileLog("httprequest", "LiveChat.Native::GetArrayWithVideoList() begin, theList.size:%d", theList.size());
	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LIVE_CHAT_GET_PHOTO_LIST_VIDEO_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		FileLog("httprequest", "LiveChat.Native::GetArrayWithVideoList() cls:%p", cls);
		if( NULL != cls && theList.size() > 0 )
		{
			jItemArray = env->NewObjectArray(theList.size(), cls, NULL);
			int i = 0;
			for(LiveChatVideoList::const_iterator itr = theList.begin();
					itr != theList.end();
					itr++, i++)
			{
				jmethodID init = env->GetMethodID(cls, "<init>", "("
						"Ljava/lang/String;"	// groupId
						"Ljava/lang/String;"	// videoId
						"Ljava/lang/String;"	// title
						")V");

				jstring groupId = env->NewStringUTF(itr->groupId.c_str());
				jstring videoId = env->NewStringUTF(itr->videoId.c_str());
				jstring title = env->NewStringUTF(itr->title.c_str());

				jobject item = env->NewObject(cls, init,
						groupId,
						videoId,
						title
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(groupId);
				env->DeleteLocalRef(videoId);
				env->DeleteLocalRef(title);

				env->DeleteLocalRef(item);
			}
		}
	}

	FileLog("httprequest", "LiveChat.Native::GetArrayWithVideoList() end");
	return jItemArray;
}

class RequestLCVideoListCallback : public IRequestLCVideoListCallback {
public:
	void OnVideoList(bool success,
			const string& errnum,
			const string& errmsg,
			const LiveChatVideoGroupList& groupList,
			const LiveChatVideoList& videoList,
			RequestLCVideoListTask* task
			) {
		FileLog("httprequest", "JNI::OnVideoList( success : %s, task : %p )", success?"true":"false", task);
		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		jobject callbackObj = gCallbackMap.Erase((long)task);
		if(callbackObj != NULL)
		{
			jclass callbackCls = env->GetObjectClass(callbackObj);
			if(callbackCls != NULL)
			{
				jobjectArray jGroupArray = GetArrayWithGroupList(env, groupList);
				jobjectArray jVideoArray = GetArrayWithVideoList(env, videoList);

				string signature = "(ZLjava/lang/String;Ljava/lang/String;";
				signature += "[L";
				signature += LIVE_CHAT_GET_PHOTO_LIST_GROUP_CLASS;
				signature += ";";
				signature += "[L";
				signature += LIVE_CHAT_GET_PHOTO_LIST_VIDEO_CLASS;
				signature += ";";
				signature += ")V";
				jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnLCGetVideoList", signature.c_str());
				if(callbackMethod != NULL){
					jstring jerrnum = env->NewStringUTF(errnum.c_str());
					jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
					env->CallVoidMethod(callbackObj, callbackMethod, success, jerrnum, jerrmsg, jGroupArray, jVideoArray);
					env->DeleteLocalRef(jerrnum);
					env->DeleteLocalRef(jerrmsg);
				}
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnVideoList() task:%p, finish", task);
	}
};
RequestLCVideoListCallback gRequestLCVideoListCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    GetVideoList
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnLCGetVideoListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_GetVideoList
  (JNIEnv *env, jclass, jstring sid, jstring userId, jobject callback) {
	RequestOperator* request = new RequestOperator;

	RequestLCVideoListTask* task = new RequestLCVideoListTask;
	task->SetParam(
			JString2String(env, sid),
			JString2String(env, userId)
			);

	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCVideoListCallback);
	task->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);

	jobject callbackObj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, callbackObj);

	FileLog("httprequest",
			"JNI::GetVideoList( "
			"sid : %s, "
			"userId ; %s "
			")",
			JString2String(env, sid).c_str(),
			JString2String(env, userId).c_str()
			);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/******************************* 5.14.	获取微视频图片 *****************************/
class RequestLCGetVideoPhotoCallback : public IRequestLCGetVideoPhotoCallback {
public:
	void OnGetVideoPhoto(
			bool success,
			const string& errnum,
			const string& errmsg,
			const string& videoId,
			GETVIDEOPHOTO_SIZE_TYPE sizeType,
			const string& filePath,
			RequestLCGetVideoPhotoTask* task
			) {
		FileLog("httprequest", "JNI::OnGetVideoPhoto( success : %s, task : %p )", success?"true":"false", task);

		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		/*callback*/
		jobject callbackObj = gCallbackMap.Erase((long)task);

		if(callbackObj != NULL){
			jclass callbackCls = env->GetObjectClass(callbackObj);
			string signature = "(JZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
			jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnLCGetVideoPhoto", signature.c_str());
			if(callbackMethod != NULL){
				jstring jerrnum = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jstring jvideoId = env->NewStringUTF(videoId.c_str());
				jstring jfilePath = env->NewStringUTF(filePath.c_str());
				jlong id = (jlong)task;
				env->CallVoidMethod(callbackObj, callbackMethod, id, success, jerrnum, jerrmsg, jvideoId, jfilePath);
				env->DeleteLocalRef(jerrnum);
				env->DeleteLocalRef(jerrmsg);
				env->DeleteLocalRef(jvideoId);
				env->DeleteLocalRef(jfilePath);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnGetVideoPhoto() task:%p, finish", task);
	}
};
RequestLCGetVideoPhotoCallback gRequestLCGetVideoPhotoCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    GetVideoPhoto
 * Signature: (Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestFileCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_GetVideoPhoto
  (JNIEnv *env, jclass, jstring womanId, jstring videoid, jint type, jstring sid, jstring userId, jstring filePath, jobject callback) {
	RequestOperator* request = new RequestOperator;

	RequestLCGetVideoPhotoTask* task = new RequestLCGetVideoPhotoTask;
	task->SetParam(
			JString2String(env, womanId),
			JString2String(env, videoid),
			(GETVIDEOPHOTO_SIZE_TYPE)type,
			JString2String(env, sid),
			JString2String(env, userId),
			JString2String(env, filePath)
			);

	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCGetVideoPhotoCallback);
	task->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);

	jobject callbackObj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, callbackObj);

	FileLog("httprequest", "JNI::GetVideoPhoto() id:%p, callbackObj:%p", task, callbackObj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/******************************* 5.15.	获取微视频文件URL *****************************/
class RequestLCGetVideoCallback : public IRequestLCGetVideoCallback {
public:
	void OnGetVideo(
			bool success,
			const string& errnum,
			const string& errmsg,
			const string& videoUrl,
			RequestLCGetVideoTask* task) {
		FileLog("httprequest", "JNI::OnGetVideo( success : %s, task : %p )", success?"true":"false", task);

		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		/*callback*/
		jobject callbackObj = gCallbackMap.Erase((long)task);

		if(callbackObj != NULL){
			jclass callbackCls = env->GetObjectClass(callbackObj);
			string signature = "(JZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
			jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnLCGetVideo", signature.c_str());
			if(callbackMethod != NULL){
				jstring jerrnum = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jstring jvideoUrl = env->NewStringUTF(videoUrl.c_str());
				jlong id = (jlong)task;
				env->CallVoidMethod(callbackObj, callbackMethod, id, success, jerrnum, jerrmsg, jvideoUrl);
				env->DeleteLocalRef(jerrnum);
				env->DeleteLocalRef(jerrmsg);
				env->DeleteLocalRef(jvideoUrl);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnGetVideoPhoto() task:%p, finish", task);
	}
};
RequestLCGetVideoCallback gRequestLCGetVideoCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    GetVideo
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnLCGetVideoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_GetVideo
  (JNIEnv *env, jclass, jstring targetId, jstring videoid, jstring inviteid, jint toflag, jstring sendid, jstring sid, jstring userId, jobject callback) {
	RequestOperator* request = new RequestOperator;

	RequestLCGetVideoTask* task = new RequestLCGetVideoTask;
	task->SetParam(
			JString2String(env, targetId),
			JString2String(env, videoid),
			JString2String(env, inviteid),
			(GETVIDEO_TO_FLAG_TYPE)toflag,
			JString2String(env, sendid),
			JString2String(env, sid),
			JString2String(env, userId)
			);

	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCGetVideoCallback);
	task->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);

	jobject callbackObj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, callbackObj);

	FileLog("httprequest", "JNI::GetVideo() id:%p, callbackObj:%p", task, callbackObj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/******************************* 5.16.	检测女士是否可发微视频 *****************************/
class RequestLCCheckSendVideoCallback : public IRequestLCCheckSendVideoCallback
{
public:
	virtual void OnCheckSendVideo(
			LC_CHECKVIDEO_TYPE result,
			const string& errnum,
			const string& errmsg,
			RequestLCCheckSendVideoTask* task
			)
	{
		FileLog("httprequest", "JNI::OnCheckSendVideo( result : %d, task : %p )", result, task);

		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		/*callback*/
		jobject callbackObj = gCallbackMap.Erase((long)task);

		if(callbackObj != NULL){
			jclass callbackCls = env->GetObjectClass(callbackObj);
			string signature = "(JILjava/lang/String;Ljava/lang/String;)V";
			jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnLCCheckSendVideo", signature.c_str());
			if(callbackMethod != NULL){
				jstring jerrnum = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jlong id = (jlong)task;
				env->CallVoidMethod(callbackObj, callbackMethod, id, result, jerrnum, jerrmsg);
				env->DeleteLocalRef(jerrnum);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnCheckSendVideo() task:%p, finish", task);
	}
};
RequestLCCheckSendVideoCallback gRequestLCCheckSendVideoCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    CheckSendVideo
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnLCCheckSendVideoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_CheckSendVideo
  (JNIEnv *env, jclass, jstring targetId, jstring videoid, jstring inviteid, jstring sid, jstring userId, jobject callback) {
	RequestOperator* request = new RequestOperator;

	RequestLCCheckSendVideoTask* task = new RequestLCCheckSendVideoTask;
	task->SetParam(
			JString2String(env, targetId),
			JString2String(env, videoid),
			JString2String(env, inviteid),
			JString2String(env, sid),
			JString2String(env, userId)
			);

	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCCheckSendVideoCallback);
	task->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);

	jobject callbackObj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, callbackObj);

	FileLog("httprequest", "JNI::CheckSendVideo() id:%p, callbackObj:%p", task, callbackObj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/******************************* 5.17.	发送微视频 *****************************/
class RequestLCSendVideoCallback : public IRequestLCSendVideoCallback {
public:
	void OnSendVideo(
			bool success,
			const string& errnum,
			const string& errmsg,
			const string& sendId,
			RequestLCSendVideoTask* task) {
		FileLog("httprequest", "JNI::OnSendVideo( success : %s, task : %p )", success?"true":"false", task);

		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		/*callback*/
		jobject callbackObj = gCallbackMap.Erase((long)task);

		if(callbackObj != NULL){
			jclass callbackCls = env->GetObjectClass(callbackObj);
			string signature = "(JZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
			jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnLCSendVideo", signature.c_str());
			if(callbackMethod != NULL){
				jstring jerrnum = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jstring jsendId = env->NewStringUTF(sendId.c_str());
				jlong id = (jlong)task;
				env->CallVoidMethod(callbackObj, callbackMethod, id, success, jerrnum, jerrmsg, jsendId);
				env->DeleteLocalRef(jerrnum);
				env->DeleteLocalRef(jerrmsg);
				env->DeleteLocalRef(jsendId);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnSendVideo() task:%p, finish", task);
	}
};
RequestLCSendVideoCallback gRequestLCSendVideoCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    SendVideo
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnLCSendVideoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_SendVideo
  (JNIEnv *env, jclass, jstring targetId, jstring videoid, jstring inviteid, jstring sid, jstring userId, jobject callback) {
	RequestOperator* request = new RequestOperator;

	RequestLCSendVideoTask* task = new RequestLCSendVideoTask;
	task->SetParam(
			JString2String(env, targetId),
			JString2String(env, videoid),
			JString2String(env, inviteid),
			JString2String(env, sid),
			JString2String(env, userId)
			);

	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCSendVideoCallback);
	task->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);

	jobject callbackObj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, callbackObj);

	FileLog("httprequest", "JNI::SendVideo() id:%p, callbackObj:%p", task, callbackObj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/******************************* 5.18.	检测功能是否开通 *****************************/

class RequestLCCheckFunctionsCallback : public IRequestLCCheckFunctionsCallback {
public:
	void OnCheckFunctions(
			bool success,
			const string& errnum,
			const string& errmsg,
			const list<string>& flagList,
			RequestLCCheckFunctionsTask* task) {

		FileLog("httprequest", "JNI::OnCheckFunctions( success : %s, task : %p )", success?"true":"false", task);

		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		jintArray flagsArray = env->NewIntArray(flagList.size());
		list<string>::const_iterator flagIter;
		int iFlagIndex;
		jint tmp[flagList.size()];
		for(iFlagIndex = 0, flagIter = flagList.begin();
				flagIter != flagList.end();
				iFlagIndex++, flagIter++){
			tmp[iFlagIndex] = atoi((*flagIter).c_str());
		}
		env->SetIntArrayRegion(flagsArray, 0, flagList.size(), tmp);

		/*callback*/
		jobject callbackObj = gCallbackMap.Erase((long)task);

		if(callbackObj != NULL){
			jclass callbackCls = env->GetObjectClass(callbackObj);
			string signature = "(ZLjava/lang/String;Ljava/lang/String;[I)V";
			jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnCheckFunctions", signature.c_str());

			FileLog("httprequest", "LiveChat.Native::OnCheckFunctions(), errnum:%s, errmsg:%s, flagList size:%d"
						, errnum.c_str(), errmsg.c_str(), flagList.size());

			if(callbackMethod != NULL){
				jstring jerrnum = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				env->CallVoidMethod(callbackObj, callbackMethod, success, jerrnum, jerrmsg, flagsArray);
				env->DeleteLocalRef(jerrnum);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		env->DeleteLocalRef(flagsArray);

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnCheckFunctions() task:%p, finish", task);
	}
};
RequestLCCheckFunctionsCallback gRequestLCCheckFunctionsCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    CheckFunctions
 * Signature: ([IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnCheckFunctionsCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_CheckFunctions
  (JNIEnv *env, jclass cls, jintArray functionArray, jint deviceType, jstring versionCode, jstring user_sid, jstring user_id, jobject callback){

	RequestOperator* request = new RequestOperator;
	//解析转换功能列表
	string functionIds = "";
	jsize len = env->GetArrayLength(functionArray);
	jint *functions = env->GetIntArrayElements(functionArray, false);
	for(int i = 0; i < len; i++) {
		char buffer[32] = {0};
		if(i < len-1){
			if(functions[i]>=0 && functions[i]<_countof(FunctionsArray)){
				snprintf(buffer, sizeof(buffer), "%d", FunctionsArray[functions[i]]);
				functionIds += buffer;
				functionIds += ",";
			}
		}else if(i == len-1){
			if(functions[i]>=0 && functions[i]<_countof(FunctionsArray)){
				snprintf(buffer, sizeof(buffer), "%d", FunctionsArray[functions[i]]);
				functionIds += buffer;
			}
		}
	}
	TDEVICE_TYPE type = IntToDeviceType(deviceType);

	RequestLCCheckFunctionsTask* task = new RequestLCCheckFunctionsTask;
	task->SetParam(
			functionIds,
			type,
			JString2String(env, versionCode),
			JString2String(env, user_sid),
			JString2String(env, user_id)
			);
	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCCheckFunctionsCallback);
	task->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);

	jobject callbackObj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, callbackObj);

	FileLog("httprequest", "JNI::CheckFunctions() id:%p, callbackObj:%p", task, callbackObj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/******************************* 5.19.	查询小高级表情配置  *****************************/

class RequestLCMagicIconConfigCallback : public IRequestLCMagicIconConfigCallback {
public:
	void OnGetMagicIconConfig(
			bool success,
			const string& errnum,
			const string& errmsg,
			const MagicIconConfig& magicIconConfig,
			RequestLCMagicIconConfigTask* task) {

		FileLog("httprequest", "JNI::OnGetMagicIconConfig( success : %s, task : %p )", success?"true":"false", task);

		JNIEnv *env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		/* create java item */
		jobject jMagicConfigItem = NULL;
		if (success) {
			/*create magic icon array */
			jobjectArray jMagicIconArray = NULL;
			JavaItemMap :: iterator itr = gJavaItemMap.find(LIVECHAT_MAGIC_ICON_TIME_CLASS);
			if(itr != gJavaItemMap.end()){
				jclass jMagicIconItemCls = env->GetObjectClass(itr->second);
				jmethodID magicIconInit = env->GetMethodID(jMagicIconItemCls, "<init>", "("
									"Ljava/lang/String;"
									"Ljava/lang/String;"
									"DI"
									"Ljava/lang/String;"
									"I"
									")V");
				jMagicIconArray = env->NewObjectArray(magicIconConfig.magicIconList.size(),jMagicIconItemCls, NULL);
				int iMagicIconIndex;
				MagicIconConfig::MagicIconList::const_iterator magicIconIter;
				for(iMagicIconIndex = 0, magicIconIter = magicIconConfig.magicIconList.begin();
						magicIconIter != magicIconConfig.magicIconList.end();
						iMagicIconIndex++, magicIconIter++){
					jstring jIconId = env->NewStringUTF(magicIconIter->iconId.c_str());
					jstring jIconTitle = env->NewStringUTF(magicIconIter->iconTitle.c_str());
					jstring jTypeId = env->NewStringUTF(magicIconIter->typeId.c_str());

					jobject jMagicItem = env->NewObject(jMagicIconItemCls, magicIconInit,
									jIconId,
									jIconTitle,
									magicIconIter->price,
									magicIconIter->hotflag,
									jTypeId,
									magicIconIter->updatetime);
					env->SetObjectArrayElement(jMagicIconArray, iMagicIconIndex, jMagicItem);
					env->DeleteLocalRef(jMagicItem);

					env->DeleteLocalRef(jIconId);
					env->DeleteLocalRef(jIconTitle);
					env->DeleteLocalRef(jTypeId);
				}
				env->DeleteLocalRef(jMagicIconItemCls);
			}
			FileLog("httprequest", "LiveChat.Native::OnGetMagicIconConfig(), jMagicIconArray:%p", jMagicIconArray);

			/*creat magic type array */
			jobjectArray jMagicTypeArray = NULL;
			itr = gJavaItemMap.find(LIVECHAT_MAGIC_TYPE_TIME_CLASS);
			if(itr != gJavaItemMap.end()){
				jclass jMagicTypeItemCls = GetJClass(env, LIVECHAT_MAGIC_TYPE_TIME_CLASS);
				jmethodID magicTypeInit = env->GetMethodID(jMagicTypeItemCls,"<init>", "("
														"Ljava/lang/String;"
														"Ljava/lang/String;"
														")V");
				jMagicTypeArray = env->NewObjectArray(magicIconConfig.typeList.size(), jMagicTypeItemCls, NULL);
				int iMagicTypeIndex;
				MagicIconConfig::MagicTypeList::const_iterator jMagicTypeIter;
				for(iMagicTypeIndex = 0, jMagicTypeIter = magicIconConfig.typeList.begin();
						jMagicTypeIter != magicIconConfig.typeList.end();
						iMagicTypeIndex++, jMagicTypeIter++){
					jstring jMagicTypeId = env->NewStringUTF(jMagicTypeIter->typeId.c_str());
					jstring jMagicTypeTitle = env->NewStringUTF(jMagicTypeIter->typeTitle.c_str());
					jobject jMagicTypeItem = env->NewObject(jMagicTypeItemCls, magicTypeInit,
												jMagicTypeId,
												jMagicTypeTitle);
					env->SetObjectArrayElement(jMagicTypeArray, iMagicTypeIndex, jMagicTypeItem);
					env->DeleteLocalRef(jMagicTypeItem);
					env->DeleteLocalRef(jMagicTypeId);
					env->DeleteLocalRef(jMagicTypeTitle);
				}
				env->DeleteLocalRef(jMagicTypeItemCls);
			}
			FileLog("httprequest", "LiveChat.Native::OnGetMagicIconConfig(), jMagicIconArray:%p, jMagicTypeArray: %p", jMagicIconArray, jMagicTypeArray);

			/*create magic config item*/
			itr = gJavaItemMap.find(LIVECHAT_MAGIC_CONFIG_ITEM_CLASS);
			if(itr != gJavaItemMap.end()){
				jclass jMagicConfigItemCls = GetJClass(env, LIVECHAT_MAGIC_CONFIG_ITEM_CLASS);
				jmethodID magicConfigInit = env->GetMethodID(jMagicConfigItemCls, "<init>", "("
						"Ljava/lang/String;"
						"I"
						"[L"
						LIVECHAT_MAGIC_ICON_TIME_CLASS
						";"
						"[L"
						LIVECHAT_MAGIC_TYPE_TIME_CLASS
						";"
						")V");
				jstring jPath = env->NewStringUTF(magicIconConfig.path.c_str());
				jMagicConfigItem = env->NewObject(jMagicConfigItemCls, magicConfigInit,
						jPath,
						magicIconConfig.maxupdatetime,
						jMagicIconArray,
						jMagicTypeArray);
				env->DeleteLocalRef(jPath);
				env->DeleteLocalRef(jMagicConfigItemCls);
				if(jMagicIconArray != NULL){
					env->DeleteLocalRef(jMagicIconArray);
				}
				if(jMagicTypeArray != NULL){
					env->DeleteLocalRef(jMagicTypeArray);
				}
			}
		}

		/*callback*/
		jobject callbackObj = gCallbackMap.Erase((long)task);

		if(callbackObj != NULL){
			jclass callbackCls = env->GetObjectClass(callbackObj);
			string signature = "(ZLjava/lang/String;Ljava/lang/String;"
					"L"
					LIVECHAT_MAGIC_CONFIG_ITEM_CLASS
					";"
					")V";
			jmethodID callbackMethod = env->GetMethodID(callbackCls, "OnGetMagicIconConfig", signature.c_str());

			FileLog("httprequest", "LiveChat.Native::OnGetMagicIconConfig(), errnum:%s, errmsg:%s"
						, errnum.c_str(), errmsg.c_str());

			if(callbackMethod != NULL){
				jstring jerrnum = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				env->CallVoidMethod(callbackObj, callbackMethod, success, jerrnum, jerrmsg, jMagicConfigItem);
				env->DeleteLocalRef(jerrnum);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		if(NULL != jMagicConfigItem){
			env->DeleteLocalRef(jMagicConfigItem);
		}

		ReleaseEnv(isAttachThread);

		FileLog("httprequest", "JNI::OnGetMagicIconConfig() task:%p, finish", task);
	}
};
RequestLCMagicIconConfigCallback gRequestLCMagicIconConfigCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    GetMagicIconConfig
 * Signature: (Lcom/qpidnetwork/request/OnGetMagicIconConfigCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_GetMagicIconConfig
  (JNIEnv *env, jclass cls, jobject callback){

	RequestOperator* request = new RequestOperator;

	RequestLCMagicIconConfigTask* task = new RequestLCMagicIconConfigTask;
	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestLCMagicIconConfigCallback);
	task->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);

	jobject callbackObj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, callbackObj);

	FileLog("httprequest", "JNI::GetMagicIconConfig() id:%p, callbackObj:%p", task, callbackObj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*)&gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}
