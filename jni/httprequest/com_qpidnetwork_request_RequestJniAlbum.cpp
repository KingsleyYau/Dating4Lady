#include "com_qpidnetwork_request_RequestJniAlbum.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"

#include <common/command.h>

#include "RequestOperator.h"
#include "RequestAlbumListTask.h"
#include "RequestAlbumCreateTask.h"
#include "RequestAlbumEditTask.h"
#include "RequestAlbumDelTask.h"
#include "RequestAlbumPhotoListTask.h"
#include "RequestAlbumAddPhotoTask.h"
#include "RequestAlbumEditPhotoTask.h"
#include "RequestAlbumVideoListTask.h"
#include "RequestAlbumAddVideoTask.h"
#include "RequestAlbumEditVideoTask.h"
#include "RequestAlbumUploadVideoTask.h"

/**************************	4.1. QueryAlbumList **************************/

class RequestAlbumListTaskCallback : public IRequestAlbumListTaskCallback{
	void OnQueryAlbumList(bool success, const string& errnum, const string& errmsg, const list<AlbumItem>& itemList, RequestAlbumListTask* task){
		FileLog("httprequest", "JNI::OnQueryAlbumList( success : %s, task : %p )", success?"true":"false", task);

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnQueryAlbumList( iRet : %d )", iRet);

		jobjectArray jItemArray = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(ALBUM_LIST_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			FileLog("httprequest", "JNI::OnQueryAlbumList( itr != gJavaItemMap.end() )");
			jclass cls = env->GetObjectClass(itr->second);
			jmethodID init = env->GetMethodID(cls, "<init>", "("
					"Ljava/lang/String;"
					"I"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"I"
					"I"
					")V");

			if( itemList.size() > 0 ) {
				jItemArray = env->NewObjectArray(itemList.size(), cls, NULL);
				int i = 0;
				for(list<AlbumItem>::const_iterator itr = itemList.begin(); itr != itemList.end(); itr++, i++) {
					jstring jAlbumId = env->NewStringUTF(itr->albumId.c_str());
					int type = AlbumTypeToInt(itr->albumType);
					jstring jAlbumTitle = env->NewStringUTF(itr->albumTitle.c_str());
					jstring jAlbumDesc = env->NewStringUTF(itr->albumDesc.c_str());
					jstring jAlbumPhotoUrl = env->NewStringUTF(itr->albumPhotoUrl.c_str());

					jobject item = env->NewObject(cls, init,
							jAlbumId,
							type,
							jAlbumTitle,
							jAlbumDesc,
							jAlbumPhotoUrl,
							itr->childCount,
							itr->createTime
							);

					env->SetObjectArrayElement(jItemArray, i, item);

					env->DeleteLocalRef(jAlbumId);
					env->DeleteLocalRef(jAlbumTitle);
					env->DeleteLocalRef(jAlbumDesc);
					env->DeleteLocalRef(jAlbumPhotoUrl);

					env->DeleteLocalRef(item);
				}
			}
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		if( callbackObj != NULL ) {
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;";
			signure += "[L";
			signure += ALBUM_LIST_ITEM_CLASS;
			signure += ";";
			signure += ")V";
			jmethodID callback = env->GetMethodID(callbackCls, "OnQueryAlbumList", signure.c_str());
			FileLog("httprequest", "JNI::OnQueryAlbumList( callback : %p, signure : %s )",
					callback, signure.c_str());

			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest", "JNI::OnQueryAlbumList( CallObjectMethod "
						"jItemArray : %p )", jItemArray);

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItemArray);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}


		if( jItemArray != NULL ) {
			env->DeleteLocalRef(jItemArray);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestAlbumListTaskCallback gRequestAlbumListTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniAlbum
 * Method:    QueryAlbumList
 * Signature: (Lcom/qpidnetwork/request/OnQueryAlbumListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAlbum_QueryAlbumList
  (JNIEnv *env, jclass cls, jobject callback){
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	//获取相册列表
	RequestAlbumListTask* task = new RequestAlbumListTask();
	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestAlbumListTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long)task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/**************************	4.2. CreateAlbum **************************/

class RequestAlbumCreateTaskCallback : public IRequestAlbumCreateTaskCallback{
	void OnAlbumCreate(bool success, const string& errnum, const string& errmsg, const string& albumId, RequestAlbumCreateTask* task){
		FileLog("httprequest", "JNI::OnAlbumCreate( success : %s, task : %p )", success?"true":"false", task);

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnAlbumCreate( iRet : %d )", iRet);

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		if( callbackObj != NULL ) {
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
			jmethodID callback = env->GetMethodID(callbackCls, "onCrateAlbum", signure.c_str());
			FileLog("httprequest", "JNI::OnAlbumCreate( callback : %p, signure : %s )",
					callback, signure.c_str());

			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jstring jAlbumId = env->NewStringUTF(albumId.c_str());

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jAlbumId);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
				env->DeleteLocalRef(jAlbumId);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestAlbumCreateTaskCallback gRequestAlbumCreateTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniAlbum
 * Method:    CreateAlbum
 * Signature: (ILcom/lang/String;Lcom/lang/String;Lcom/qpidnetwork/request/OnCreateAlbumCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAlbum_CreateAlbum
  (JNIEnv *env, jclass cls, jint type, jstring title, jstring desc, jobject callback){
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	//获取相册列表
	RequestAlbumCreateTask* task = new RequestAlbumCreateTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			IntToAlbumType(type)
			, JString2String(env, title)
			, JString2String(env, desc));
	task->SetCallback(&gRequestAlbumCreateTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long)task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/**************************	4.3. EditAlbum **************************/

class RequestAlbumEditTaskCallback : public IRequestAlbumEditTaskCallback{
	void OnAlbumEdit(bool success, const string& errnum, const string& errmsg, RequestAlbumEditTask* task){
		FileLog("httprequest", "JNI::OnAlbumEdit( success : %s, task : %p )", success?"true":"false", task);

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnAlbumEdit( iRet : %d )", iRet);

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		if( callbackObj != NULL ) {
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
			jmethodID callback = env->GetMethodID(callbackCls, "onEditAlbum", signure.c_str());
			FileLog("httprequest", "JNI::OnAlbumEdit( callback : %p, signure : %s )",
					callback, signure.c_str());

			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestAlbumEditTaskCallback gRequestAlbumEditTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniAlbum
 * Method:    EditAlbum
 * Signature: (Lcom/lang/String;ILcom/lang/String;Lcom/lang/String;Lcom/qpidnetwork/request/OnEditAlbumCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAlbum_EditAlbum
  (JNIEnv *env, jclass cls, jstring albumId, jint type, jstring title, jstring desc, jobject callback){
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	//获取相册列表
	RequestAlbumEditTask* task = new RequestAlbumEditTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			JString2String(env, albumId)
			, IntToAlbumType(type)
			, JString2String(env, title)
			, JString2String(env, desc));
	task->SetCallback(&gRequestAlbumEditTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long)task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/**************************	4.4. DeleteAlbum **************************/

class RequestAlbumDelTaskCallback : public IRequestAlbumDelTaskCallback{
	void OnAlbumDel(bool success, const string& errnum, const string& errmsg, RequestAlbumDelTask* task){
		FileLog("httprequest", "JNI::OnAlbumDel( success : %s, task : %p )", success?"true":"false", task);

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnAlbumDel( iRet : %d )", iRet);

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		if( callbackObj != NULL ) {
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
			jmethodID callback = env->GetMethodID(callbackCls, "onDeleteAlbum", signure.c_str());
			FileLog("httprequest", "JNI::OnAlbumDel( callback : %p, signure : %s )",
					callback, signure.c_str());

			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestAlbumDelTaskCallback gRequestAlbumDelTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniAlbum
 * Method:    DeleteAlbum
 * Signature: (Lcom/lang/String;ILcom/qpidnetwork/request/OnDeleteAlbumCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAlbum_DeleteAlbum
  (JNIEnv *env, jclass cls, jstring albumId, jint type, jobject callback){
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	//获取相册列表
	RequestAlbumDelTask* task = new RequestAlbumDelTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			JString2String(env, albumId)
			, IntToAlbumType(type));
	task->SetCallback(&gRequestAlbumDelTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long)task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/**************************	4.5. QueryAlbumPhotoList **************************/

class RequestAlbumPhotoListTaskCallback : public IRequestAlbumPhotoListTaskCallback{
	void OnAlbumPhotoList(bool success, const string& errnum, const string& errmsg, const list<AlbumPhotoItem>& itemList, RequestAlbumPhotoListTask* task){
		FileLog("httprequest", "JNI::OnAlbumPhotoList( success : %s, task : %p )", success?"true":"false", task);

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnAlbumPhotoList( iRet : %d )", iRet);

		jobjectArray jItemArray = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(ALBUM_PHOTO_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			FileLog("httprequest", "JNI::OnAlbumPhotoList( itr != gJavaItemMap.end() )");
			jclass cls = env->GetObjectClass(itr->second);
			jmethodID init = env->GetMethodID(cls, "<init>", "("
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"I"
					"I"
					")V");

			if( itemList.size() > 0 ) {
				jItemArray = env->NewObjectArray(itemList.size(), cls, NULL);
				int i = 0;
				for(list<AlbumPhotoItem>::const_iterator itr = itemList.begin(); itr != itemList.end(); itr++, i++) {
					jstring jPhotoId = env->NewStringUTF(itr->photoId.c_str());
					jstring jPhotoTitle = env->NewStringUTF(itr->photoTitle.c_str());
					jstring jPhotoThumbUrl = env->NewStringUTF(itr->photoThumbUrl.c_str());
					jstring jPhotoUrl = env->NewStringUTF(itr->photoUrl.c_str());
					int reviewStatus = ReviewStatusToInt(itr->reviewStatus);
					int reviewReason = ReviewReasonToInt(itr->reviewReason);

					jobject item = env->NewObject(cls, init,
							jPhotoId,
							jPhotoTitle,
							jPhotoThumbUrl,
							jPhotoUrl,
							reviewStatus,
							reviewReason
							);

					env->SetObjectArrayElement(jItemArray, i, item);

					env->DeleteLocalRef(jPhotoId);
					env->DeleteLocalRef(jPhotoTitle);
					env->DeleteLocalRef(jPhotoThumbUrl);
					env->DeleteLocalRef(jPhotoUrl);

					env->DeleteLocalRef(item);
				}
			}
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		if( callbackObj != NULL ) {
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;";
			signure += "[L";
			signure += ALBUM_PHOTO_ITEM_CLASS;
			signure += ";";
			signure += ")V";
			jmethodID callback = env->GetMethodID(callbackCls, "OnQueryAlbumPhotoList", signure.c_str());
			FileLog("httprequest", "JNI::OnAlbumPhotoList( callback : %p, signure : %s )",
					callback, signure.c_str());

			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest", "JNI::OnAlbumPhotoList( CallObjectMethod "
						"jItemArray : %p )", jItemArray);

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItemArray);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}


		if( jItemArray != NULL ) {
			env->DeleteLocalRef(jItemArray);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestAlbumPhotoListTaskCallback gRequestAlbumPhotoListTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniAlbum
 * Method:    QueryAlbumPhotoList
 * Signature: (Lcom/lang/String;Lcom/qpidnetwork/request/OnQueryAlbumPhotoListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAlbum_QueryAlbumPhotoList
  (JNIEnv *env, jclass cls, jstring albumId, jobject callback){
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	//获取相册列表
	RequestAlbumPhotoListTask* task = new RequestAlbumPhotoListTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(JString2String(env, albumId));
	task->SetCallback(&gRequestAlbumPhotoListTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long)task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/**************************	4.6. AddAlbumPhoto **************************/

class RequestAlbumAddPhotoTaskCallback : public IRequestAlbumAddPhotoTaskCallback{
	void OnAlbumAddPhoto(bool success, const string& errnum, const string& errmsg,const string& photoId, RequestAlbumAddPhotoTask* task){
		FileLog("httprequest", "JNI::OnAlbumAddPhoto( success : %s, task : %p )", success?"true":"false", task);

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnAlbumAddPhoto( iRet : %d )", iRet);

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		if( callbackObj != NULL ) {
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
			jmethodID callback = env->GetMethodID(callbackCls, "onSaveAlbumPhoto", signure.c_str());
			FileLog("httprequest", "JNI::OnAlbumAddPhoto( callback : %p, signure : %s )",
					callback, signure.c_str());

			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jstring jPhotoId = env->NewStringUTF(photoId.c_str());

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jPhotoId);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
				env->DeleteLocalRef(jPhotoId);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestAlbumAddPhotoTaskCallback gRequestAlbumAddPhotoTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniAlbum
 * Method:    AddAlbumPhoto
 * Signature: (Lcom/lang/String;Lcom/lang/String;Lcom/lang/String;Lcom/qpidnetwork/request/OnSaveAlbumPhotoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAlbum_AddAlbumPhoto
  (JNIEnv *env, jclass cls, jstring albumId, jstring photoTile, jstring filePath, jobject callback){
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	//获取相册列表
	RequestAlbumAddPhotoTask* task = new RequestAlbumAddPhotoTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			JString2String(env, albumId)
			, JString2String(env, photoTile)
			, JString2String(env, filePath));
	task->SetCallback(&gRequestAlbumAddPhotoTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long)task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/**************************	4.7. EditAlbumPhoto **************************/

class RequestAlbumEditPhotoTaskCallback : public IRequestAlbumEditPhotoTaskCallback{
	void OnAlbumEditPhoto(bool success, const string& errnum, const string& errmsg, RequestAlbumEditPhotoTask* task){
		FileLog("httprequest", "JNI::OnAlbumEditPhoto( success : %s, task : %p )", success?"true":"false", task);

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnAlbumEditPhoto( iRet : %d )", iRet);

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		if( callbackObj != NULL ) {
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
			jmethodID callback = env->GetMethodID(callbackCls, "onEditAlbumPhoto", signure.c_str());
			FileLog("httprequest", "JNI::OnAlbumEditPhoto( callback : %p, signure : %s )",
					callback, signure.c_str());

			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestAlbumEditPhotoTaskCallback gRequestAlbumEditPhotoTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniAlbum
 * Method:    EditAlbumPhoto
 * Signature: (Lcom/lang/String;Lcom/lang/String;Lcom/qpidnetwork/request/OnEditAlbumPhotoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAlbum_EditAlbumPhoto
  (JNIEnv *env, jclass cls, jstring photoId, jstring photoTitle, jobject callback){
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	//获取相册列表
	RequestAlbumEditPhotoTask* task = new RequestAlbumEditPhotoTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			JString2String(env, photoId)
			, JString2String(env, photoTitle));
	task->SetCallback(&gRequestAlbumEditPhotoTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long)task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/**************************	4.8. QueryAlbumVideoList **************************/

class RequestAlbumVideoListTaskCallback : public IRequestAlbumVideoListTaskCallback{
	void OnAlbumVideoList(bool success, const string& errnum, const string& errmsg, const list<AlbumVideoItem>& itemList, RequestAlbumVideoListTask* task){
		FileLog("httprequest", "JNI::OnAlbumVideoList( success : %s, task : %p )", success?"true":"false", task);

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnAlbumVideoList( iRet : %d )", iRet);

		jobjectArray jItemArray = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(ALBUM_VIDEO_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			FileLog("httprequest", "JNI::OnAlbumVideoList( itr != gJavaItemMap.end() )");
			jclass cls = env->GetObjectClass(itr->second);
			jmethodID init = env->GetMethodID(cls, "<init>", "("
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"I"
					"I"
					"I"
					")V");

			if( itemList.size() > 0 ) {
				jItemArray = env->NewObjectArray(itemList.size(), cls, NULL);
				int i = 0;
				for(list<AlbumVideoItem>::const_iterator itr = itemList.begin(); itr != itemList.end(); itr++, i++) {
					jstring jvideoId = env->NewStringUTF(itr->videoId.c_str());
					jstring jvideoTitle = env->NewStringUTF(itr->videoTitle.c_str());
					jstring jvideoThumbUrl = env->NewStringUTF(itr->videoThumbUrl.c_str());
					jstring jvideoPreviewUrl = env->NewStringUTF(itr->videoPreviewUrl.c_str());
					jstring jvideoUrl = env->NewStringUTF(itr->videoUrl.c_str());
					int videoProcessStatus = VideoProcessStatusToInt(itr->videoProcessStatus);
					int reviewStatus = ReviewStatusToInt(itr->reviewStatus);
					int reviewReason = ReviewReasonToInt(itr->reviewReason);

					jobject item = env->NewObject(cls, init,
							jvideoId,
							jvideoTitle,
							jvideoThumbUrl,
							jvideoPreviewUrl,
							jvideoUrl,
							videoProcessStatus,
							reviewStatus,
							reviewReason
							);

					env->SetObjectArrayElement(jItemArray, i, item);

					env->DeleteLocalRef(jvideoId);
					env->DeleteLocalRef(jvideoTitle);
					env->DeleteLocalRef(jvideoThumbUrl);
					env->DeleteLocalRef(jvideoPreviewUrl);
					env->DeleteLocalRef(jvideoUrl);

					env->DeleteLocalRef(item);
				}
			}
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		if( callbackObj != NULL ) {
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;";
			signure += "[L";
			signure += ALBUM_VIDEO_ITEM_CLASS;
			signure += ";";
			signure += ")V";
			jmethodID callback = env->GetMethodID(callbackCls, "OnQueryAlbumVideoList", signure.c_str());
			FileLog("httprequest", "JNI::OnAlbumVideoList( callback : %p, signure : %s )",
					callback, signure.c_str());

			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest", "JNI::OnAlbumVideoList( CallObjectMethod "
						"jItemArray : %p )", jItemArray);

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItemArray);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}


		if( jItemArray != NULL ) {
			env->DeleteLocalRef(jItemArray);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestAlbumVideoListTaskCallback gRequestAlbumVideoListTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniAlbum
 * Method:    QueryAlbumVideoList
 * Signature: (Lcom/lang/String;Lcom/qpidnetwork/request/OnQueryAlbumVideoListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAlbum_QueryAlbumVideoList
  (JNIEnv *env, jclass cls, jstring albumId, jobject callback){
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	//获取相册列表
	RequestAlbumVideoListTask* task = new RequestAlbumVideoListTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			JString2String(env, albumId));
	task->SetCallback(&gRequestAlbumVideoListTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long)task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/**************************	4.10. AddAlbumVideo **************************/

class RequestAlbumUploadVideoTaskCallback : public IRequestAlbumUploadVideoTaskCallback{
	void OnAlbumUploadVideo(bool success, const string& errnum, const string& errmsg, const string& identifyValues, RequestAlbumUploadVideoTask* task){
		FileLog("httprequest", "JNI::OnAlbumUploadVideo( success : %s, task : %p )", success?"true":"false", task);
		FileLog("httprequest", "JNI::OnAlbumUploadVideo( identifyValues : %s)", identifyValues.c_str());

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnAlbumUploadVideo( iRet : %d )", iRet);

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		if( callbackObj != NULL ) {
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
			jmethodID callback = env->GetMethodID(callbackCls, "OnUploadVideo", signure.c_str());
			FileLog("httprequest", "JNI::OnAlbumUploadVideo( callback : %p, signure : %s )",
					callback, signure.c_str());

			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jstring jidentifyValues = env->NewStringUTF(identifyValues.c_str());

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jidentifyValues);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
				env->DeleteLocalRef(jidentifyValues);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestAlbumUploadVideoTaskCallback gRequestAlbumUploadVideoTaskCallback;


/*
 * Class:     com_qpidnetwork_request_RequestJniAlbum
 * Method:    UploadVideoFile
 * Signature: (Lcom/lang/String;Lcom/lang/String;ILcom/lang/String;ILcom/lang/String;Lcom/lang/String;Lcom/qpidnetwork/request/OnUploadVideoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAlbum_UploadVideoFile
  (JNIEnv *env, jclass cls, jstring agencyID, jstring womanID, jint siteId, jstring shortVideoKey, jint serverType, jstring filePath, jstring mimeType, jobject callback){
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	//获取相册列表
	RequestAlbumUploadVideoTask* task = new RequestAlbumUploadVideoTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			JString2String(env, agencyID)
			, JString2String(env, womanID)
			, siteId
			, JString2String(env, shortVideoKey)
			, serverType
			, JString2String(env, filePath)
			, JString2String(env, mimeType));
	task->SetCallback(&gRequestAlbumUploadVideoTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long)task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/**************************	4.10. AddAlbumVideo **************************/

class RequestAlbumAddVideoTaskCallback : public IRequestAlbumAddVideoTaskCallback{
	void OnAlbumAddVideo(bool success, const string& errnum, const string& errmsg, const string& videoId, RequestAlbumAddVideoTask* task){
		FileLog("httprequest", "JNI::OnAlbumAddVideo( success : %s, task : %p )", success?"true":"false", task);

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnAlbumAddVideo( iRet : %d )", iRet);

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		if( callbackObj != NULL ) {
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
			jmethodID callback = env->GetMethodID(callbackCls, "onSaveAlbumVideo", signure.c_str());
			FileLog("httprequest", "JNI::OnAlbumAddVideo( callback : %p, signure : %s )",
					callback, signure.c_str());

			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jstring jvideoId = env->NewStringUTF(videoId.c_str());

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jvideoId);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
				env->DeleteLocalRef(jvideoId);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestAlbumAddVideoTaskCallback gRequestAlbumAddVideoTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniAlbum
 * Method:    AddAlbumVideo
 * Signature: (Lcom/lang/String;Lcom/lang/String;Lcom/lang/String;Lcom/lang/String;Lcom/lang/String;Lcom/qpidnetwork/request/OnSaveAlbumVideoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAlbum_AddAlbumVideo
  (JNIEnv *env, jclass cls, jstring albumId, jstring videoTitle, jstring shortVideoKey, jstring hidFileMd5ID, jstring thumbFilePath, jobject callback){
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	//获取相册列表
	RequestAlbumAddVideoTask* task = new RequestAlbumAddVideoTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			JString2String(env, albumId)
			, JString2String(env, videoTitle)
			, JString2String(env, shortVideoKey)
			, JString2String(env, hidFileMd5ID)
			, JString2String(env, thumbFilePath));
	task->SetCallback(&gRequestAlbumAddVideoTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long)task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/**************************	4.10. EditAlbumVideo **************************/

class RequestAlbumEditVideoTaskCallback : public IRequestAlbumEditVideoTaskCallback{
	void OnAlbumEditVideo(bool success, const string& errnum, const string& errmsg, RequestAlbumEditVideoTask* task){
		FileLog("httprequest", "JNI::OnAlbumEditVideo( success : %s, task : %p )", success?"true":"false", task);

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnAlbumEditVideo( iRet : %d )", iRet);

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		if( callbackObj != NULL ) {
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
			jmethodID callback = env->GetMethodID(callbackCls, "onEditAlbumVideo", signure.c_str());
			FileLog("httprequest", "JNI::OnAlbumEditVideo( callback : %p, signure : %s )",
					callback, signure.c_str());

			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestAlbumEditVideoTaskCallback gRequestAlbumEditVideoTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniAlbum
 * Method:    EditAlbumVideo
 * Signature: (Lcom/lang/String;Lcom/lang/String;Lcom/lang/String;Lcom/qpidnetwork/request/OnEditAlbumVideoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAlbum_EditAlbumVideo
  (JNIEnv *env, jclass cls, jstring videoId, jstring videoTitle, jstring videoThumbPath, jobject callback){
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	//获取相册列表
	RequestAlbumEditVideoTask* task = new RequestAlbumEditVideoTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			JString2String(env, videoId)
			, JString2String(env, videoTitle)
			, JString2String(env, videoThumbPath));
	task->SetCallback(&gRequestAlbumEditVideoTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long)task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}
