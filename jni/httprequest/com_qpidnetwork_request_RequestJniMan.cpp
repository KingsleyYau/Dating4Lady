#include "com_qpidnetwork_request_RequestJniMan.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"

#include "RequestOperator.h"
#include "RequestManListTask.h"
#include "RequestManDetailTask.h"
#include "RequestManFavourListTask.h"
#include "RequestManAddFavourTask.h"
#include "RequestManRemoveFavourTask.h"
#include "RequestManRecentChatListTask.h"
#include "RequestManRecentViewListTask.h"

/**************************	QueryManList **************************/
class RequestManListCallback : public IRequestManListCallback {
	void OnQueryManList(bool success, const string& errnum, const string& errmsg, const list<ManListItem>& itemList, int totalCount, RequestManListTask* task) {
		FileLog("httprequest", "JNI::OnQueryManList( success : %s, task : %p )", success?"true":"false", task);

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}
		FileLog("httprequest", "JNI::OnQueryManList( iRet : %d )", iRet);

		jobjectArray jItemArray = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(MAN_LIST_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			FileLog("httprequest", "JNI::OnQueryManList( itr != gJavaItemMap.end() )");
			jclass cls = env->GetObjectClass(itr->second);
			jmethodID init = env->GetMethodID(cls, "<init>", "("
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"I"
					"I"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"I"
					")V");

			if( itemList.size() > 0 ) {
				jItemArray = env->NewObjectArray(itemList.size(), cls, NULL);
				int i = 0;
				for(list<ManListItem>::const_iterator itr = itemList.begin(); itr != itemList.end(); itr++, i++) {
					jstring man_id = env->NewStringUTF(itr->man_id.c_str());
					jstring firstname = env->NewStringUTF(itr->firstname.c_str());
					jstring lastname = env->NewStringUTF(itr->lastname.c_str());
					jstring province = env->NewStringUTF(itr->province.c_str());
					jstring photo_url = env->NewStringUTF(itr->photo_url.c_str());

					jobject item = env->NewObject(cls, init,
							man_id,
							firstname,
							lastname,
							itr->age,
							itr->country,
							province,
							photo_url,
							itr->photo_status
							);

					env->SetObjectArrayElement(jItemArray, i, item);

					env->DeleteLocalRef(man_id);
					env->DeleteLocalRef(firstname);
					env->DeleteLocalRef(lastname);
					env->DeleteLocalRef(province);
					env->DeleteLocalRef(photo_url);

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
			signure += MAN_LIST_ITEM_CLASS;
			signure += ";";
			signure += "I";
			signure += ")V";
			jmethodID callback = env->GetMethodID(callbackCls, "OnQueryManList", signure.c_str());
			FileLog("httprequest", "JNI::OnQueryManList( callback : %p, signure : %s )",
					callback, signure.c_str());

			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest", "JNI::OnQueryManList( CallObjectMethod "
						"jItemArray : %p )", jItemArray);

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItemArray, totalCount);

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
RequestManListCallback gRequestManListCallback;

/*
 * Class:     _com_qpidnetwork_request_RequestJniMan_QueryManList
 * Method:    QueryManList
 * Signature: (IIILjava/lang/String;IIIZLcom/qpidnetwork/request/OnQueryManListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniMan_QueryManList
  (JNIEnv *env, jclass, jint pageIndex, jint pageSize, jint query_type, jstring man_id, jint from_age,
		  jint to_age, jint country, jboolean photo, jobject callback) {

	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	// 获取男士列表
	RequestManListTask *task = new RequestManListTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			pageIndex,
			pageSize,
			(QUERYTYPE)query_type,
			JString2String(env, man_id).c_str(),
			from_age,
			to_age,
			country,
			photo
			);
	task->SetCallback(&gRequestManListCallback);
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

/**************************	QueryManDetail **************************/
class RequestManDetailCallback : public IRequestManDetailCallback {
	void OnQueryManDetail(bool success, const string& errnum, const string& errmsg, const ManDetailItem& item, RequestManDetailTask* task) {
		FileLog("httprequest", "JNI::OnQueryManDetail( success : %s )", success?"true":"false");

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}

		jobject jItem = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(MAN_DETAIL_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jclass cls = env->GetObjectClass(itr->second);
			if( cls != NULL) {
				jmethodID init = env->GetMethodID(cls, "<init>",
						"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIIIIIIIIIIILjava/lang/String;ZZLjava/lang/String;Ljava/lang/String;IZ)V"
						);

				FileLog("httprequest", "JNI::OnQueryManDetail( GetMethodID <init> : %p )", init);

				if( init != NULL ) {
					jstring man_id = env->NewStringUTF(item.man_id.c_str());
					jstring firstname = env->NewStringUTF(item.firstname.c_str());
					jstring lastname = env->NewStringUTF(item.lastname.c_str());

					jstring province = env->NewStringUTF(item.province.c_str());
					jstring city = env->NewStringUTF(item.city.c_str());
					jstring join_date = env->NewStringUTF(item.join_date.c_str());
					jstring birthday = env->NewStringUTF(item.birthday.c_str());

					jstring about_me = env->NewStringUTF(item.about_me.c_str());

					jstring photo_url = env->NewStringUTF(item.photo_url.c_str());
					jstring photo_big_url = env->NewStringUTF(item.photo_big_url.c_str());

					jItem = env->NewObject(cls, init,
							man_id,
							firstname,
							lastname,

							item.country,
							province,
							city,

							join_date,
							birthday,

							item.weight,
							item.height,
							item.smoke,
							item.drink,
							item.language,
							item.religion,
							item.education,
							item.profession,
							item.children,
							item.marry,
							item.income,
							item.ethnicity,

							about_me,
							item.online,
							item.favorite,

							photo_url,
							photo_big_url,
							item.photo_status,

							item.receive_admirer
							);

					env->DeleteLocalRef(man_id);
					env->DeleteLocalRef(firstname);
					env->DeleteLocalRef(lastname);
					env->DeleteLocalRef(province);
					env->DeleteLocalRef(city);
					env->DeleteLocalRef(join_date);
					env->DeleteLocalRef(birthday);
					env->DeleteLocalRef(about_me);
					env->DeleteLocalRef(photo_url);
					env->DeleteLocalRef(photo_big_url);


					FileLog("httprequest", "JNI::OnQueryManDetail( NewObject : %p )", jItem);
				}
			}
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long long)task);
		jclass callbackCls = env->GetObjectClass(callbackObj);

		string signure = "(ZLjava/lang/String;Ljava/lang/String;";
		signure += "L";
		signure += MAN_DETAIL_ITEM_CLASS;
		signure += ";";
		signure += ")V";
		jmethodID callback = env->GetMethodID(callbackCls, "OnQueryManDetail", signure.c_str());
		FileLog("httprequest", "JNI::OnQueryManDetail( callback : %p, signure : %s )",
				callback, signure.c_str());

		if( callbackObj != NULL ) {
			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest", "JNI::OnQueryManDetail( CallObjectMethod "
						"jItem : %p )", jItem);

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItem);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}

		if( jItem != NULL ) {
			env->DeleteLocalRef(jItem);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}
};
RequestManDetailCallback gRequestManDetailCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniMan
 * Method:    QueryManDetail
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnQueryManDetailCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniMan_QueryManDetail
  (JNIEnv *env, jclass, jstring man_id, jobject callback) {
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestManDetailTask *task = new RequestManDetailTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			JString2String(env, man_id).c_str()
			);
	task->SetCallback(&gRequestManDetailCallback);
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

/**************************	QueryFavourList **************************/

class RequestManFavourListCallback : public IRequestManFavourListCallback {
	void OnQueryFavourList(bool success, const string& errnum, const string& errmsg, const list<string>& itemList, RequestManFavourListTask* task) {
		FileLog("httprequest", "RequestManFavourListCallback::OnQueryFavourList( success : %s )", success?"true":"false");

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}

		jobjectArray jItemArray = NULL;
		if( itemList.size() > 0 ) {
			jItemArray = env->NewObjectArray(itemList.size(), env->FindClass("java/lang/String"), NULL);
			int i = 0;
			for(list<string>::const_iterator itr = itemList.begin(); itr != itemList.end(); itr++, i++) {
				jstring man_id = env->NewStringUTF(itr->c_str());
				env->SetObjectArrayElement(jItemArray, i, man_id);
				env->DeleteLocalRef(man_id);
			}
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		jclass callbackCls = env->GetObjectClass(callbackObj);

		string signure = "(ZLjava/lang/String;Ljava/lang/String;";
		signure += "[L";
		signure += "java/lang/String";
		signure += ";";
		signure += ")V";
		jmethodID callback = env->GetMethodID(callbackCls, "OnQueryFavourList", signure.c_str());
		FileLog("httprequest", "RequestManFavourListCallback::OnQueryFavourList( callback : %p, signure : %s )",
				callback, signure.c_str());

		if( callbackObj != NULL ) {
			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest", "RequestManFavourListCallback::OnQueryFavourList( CallObjectMethod "
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
RequestManFavourListCallback gRequestManFavourListCallback;
/*
 * Class:     com_qpidnetwork_request_RequestJniMan
 * Method:    QueryFavourList
 * Signature: (Lcom/qpidnetwork/request/OnQueryFavourListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniMan_QueryFavourList
  (JNIEnv *env, jclass, jobject callback) {
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestManFavourListTask *task = new RequestManFavourListTask();
	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestManFavourListCallback);
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

/**************************	AddFavourites **************************/

class RequestManAddFavourCallback : public IRequestManAddFavourCallback {
	void OnAddFavourites(bool success, const string& errnum, const string& errmsg, RequestManAddFavourTask* task) {
		FileLog("httprequest", "JNI::OnAddFavourites( success : %s )", success?"true":"false");

			/* turn object to java object here */
			JNIEnv* env;
			jint iRet = JNI_ERR;
			gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
			if( env == NULL ) {
				iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
			}

			/* real callback java */
			jobject callbackObj = gCallbackMap.Erase((long)task);
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
			jmethodID callback = env->GetMethodID(callbackCls, "OnRequest", signure.c_str());
			FileLog("httprequest", "JNI::OnAddFavourites( callbackCls : %p, callback : %p, signure : %s )",
					callbackCls, callback, signure.c_str());

			if( callbackObj != NULL ) {
				if( callback != NULL ) {
					jstring jerrno = env->NewStringUTF(errnum.c_str());
					jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

					FileLog("httprequest", "JNI::OnAddFavourites( CallObjectMethod )");

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
RequestManAddFavourCallback gRequestManAddFavourCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniMan
 * Method:    AddFavourites
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniMan_AddFavourites
  (JNIEnv *env, jclass, jstring man_id, jobject callback) {
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestManAddFavourTask *task = new RequestManAddFavourTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			JString2String(env, man_id).c_str()
			);
	task->SetCallback(&gRequestManAddFavourCallback);
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

/**************************	RemoveFavourites **************************/
class RequestManRemoveFavourCallback : public IRequestManRemoveFavourCallback {
	void OnRemoveFavourites(bool success, const string& errnum, const string& errmsg, RequestManRemoveFavourTask* task) {
		FileLog("httprequest", "JNI::OnRemoveFavourites( success : %s )", success?"true":"false");

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		jclass callbackCls = env->GetObjectClass(callbackObj);

		string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
		jmethodID callback = env->GetMethodID(callbackCls, "OnRequest", signure.c_str());
		FileLog("httprequest", "JNI::OnRemoveFavourites( callbackCls : %p, callback : %p, signure : %s )",
				callbackCls, callback, signure.c_str());

		if( callbackObj != NULL ) {
			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest", "JNI::OnRemoveFavourites( CallObjectMethod )");

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
RequestManRemoveFavourCallback gRequestManRemoveFavourCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniMan
 * Method:    RemoveFavourites
 * Signature: ([Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniMan_RemoveFavourites
  (JNIEnv *env, jclass, jobjectArray man_ids, jobject callback) {
	list<string> man_id_list;
	for(int i = 0; i < env->GetArrayLength(man_ids); i++) {
		jstring man_id = (jstring)env->GetObjectArrayElement(man_ids, i);
		man_id_list.push_back(JString2String(env, man_id));
	}

	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestManRemoveFavourTask *task = new RequestManRemoveFavourTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			man_id_list
			);
	task->SetCallback(&gRequestManRemoveFavourCallback);
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

/**************************	QueryManRecentChatList **************************/
class RequestManRecentChatListCallback : public IRequestManRecentChatListCallback {
	void OnQueryManRecentChatList(bool success, const string& errnum, const string& errmsg, const list<ManRecentChatListItem>& itemList, int totalCount, RequestManRecentChatListTask* task) {
		FileLog("httprequest", "JNI::OnQueryManRecentChatList( success : %s )", success?"true":"false");

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}

		jobjectArray jItemArray = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(MAN_RECENT_CHAT_LIST_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jclass cls = env->GetObjectClass(itr->second);
			jmethodID init = env->GetMethodID(cls, "<init>",
					"("
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"I"
					"I"
					"Ljava/lang/String;"
					"I"
					"I"
					")V");

			if( itemList.size() > 0 ) {
				jItemArray = env->NewObjectArray(itemList.size(), cls, NULL);
				int i = 0;
				for(list<ManRecentChatListItem>::const_iterator itr = itemList.begin(); itr != itemList.end(); itr++, i++) {
					jstring man_id = env->NewStringUTF(itr->man_id.c_str());
					jstring firstname = env->NewStringUTF(itr->firstname.c_str());
					jstring lastname = env->NewStringUTF(itr->lastname.c_str());
					jstring photo_url = env->NewStringUTF(itr->photo_url.c_str());

					jobject item = env->NewObject(cls, init,
							man_id,
							firstname,
							lastname,
							itr->age,
							itr->country,
							photo_url,
							itr->status,
							itr->client_type
							);

					env->SetObjectArrayElement(jItemArray, i, item);

					env->DeleteLocalRef(man_id);
					env->DeleteLocalRef(firstname);
					env->DeleteLocalRef(lastname);
					env->DeleteLocalRef(photo_url);

					env->DeleteLocalRef(item);
				}
			}
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		jclass callbackCls = env->GetObjectClass(callbackObj);

		string signure = "(ZLjava/lang/String;Ljava/lang/String;";
		signure += "[L";
		signure += MAN_RECENT_CHAT_LIST_ITEM_CLASS;
		signure += ";";
		signure += "I";
		signure += ")V";
		jmethodID callback = env->GetMethodID(callbackCls, "OnQueryManRecentChatList", signure.c_str());
		FileLog("httprequest", "JNI::OnQueryManRecentChatList( callback : %p, signure : %s )",
				callback, signure.c_str());

		if( callbackObj != NULL) {
			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest", "JNI::OnQueryManRecentChatList( CallObjectMethod "
						"jItemArray : %p )", jItemArray);

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItemArray, totalCount);

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
RequestManRecentChatListCallback gRequestManRecentChatListCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniMan
 * Method:    QueryManRecentChatList
 * Signature: (IIILcom/qpidnetwork/request/OnQueryManRecentChatListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniMan_QueryManRecentChatList
  (JNIEnv *env, jclass, jint pageIndex, jint pageSize, jint query_type, jobject callback) {
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestManRecentChatListTask *task = new RequestManRecentChatListTask();
	task->Init(&gHttpRequestManager);
	task->SetParam(
			pageIndex,
			pageSize,
			(RECENT_CHAT_QUERYTYPE)query_type
			);
	task->SetCallback(&gRequestManRecentChatListCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long long id = (long long)task;
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

/**************************	QueryManRecentViewList **************************/
class RequestManRecentViewListCallback : public IRequestManRecentViewListCallback {
	void OnQueryRecentViewList(bool success, const string& errnum, const string& errmsg, const list<ManRecentViewListItem>& itemList, RequestManRecentViewListTask* task) {
		FileLog("httprequest", "JNI::OnQueryRecentViewList( success : %s )", success?"true":"false");

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}

		jobjectArray jItemArray = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(MAN_RECENT_VIEW_LIST_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jclass cls = env->GetObjectClass(itr->second);
			jmethodID init = env->GetMethodID(cls, "<init>",
					"("
					"Ljava/lang/String;"
					"I"
					")V");

			if( itemList.size() > 0 ) {
				jItemArray = env->NewObjectArray(itemList.size(), cls, NULL);
				int i = 0;
				for(list<ManRecentViewListItem>::const_iterator itr = itemList.begin(); itr != itemList.end(); itr++, i++) {
					jstring man_id = env->NewStringUTF(itr->man_id.c_str());

					jobject item = env->NewObject(cls, init,
							man_id,
							itr->last_time
							);

					env->SetObjectArrayElement(jItemArray, i, item);

					env->DeleteLocalRef(man_id);

					env->DeleteLocalRef(item);
				}
			}
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long)task);
		jclass callbackCls = env->GetObjectClass(callbackObj);

		string signure = "(ZLjava/lang/String;Ljava/lang/String;";
		signure += "[L";
		signure += MAN_RECENT_VIEW_LIST_ITEM_CLASS;
		signure += ";";
		signure += ")V";
		jmethodID callback = env->GetMethodID(callbackCls, "OnQueryManRecentViewList", signure.c_str());
		FileLog("httprequest", "JNI::OnQueryRecentViewList( callback : %p, signure : %s )",
				callback, signure.c_str());

		if( callbackObj != NULL ) {
			if( callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest", "JNI::OnQueryRecentViewList( CallObjectMethod "
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
RequestManRecentViewListCallback gRequestManRecentViewListCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniMan
 * Method:    QueryManRecentViewList
 * Signature: (Lcom/qpidnetwork/request/OnQueryManRecentViewListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniMan_QueryManRecentViewList
  (JNIEnv *env, jclass, jobject callback) {
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestManRecentViewListTask *task = new RequestManRecentViewListTask();
	task->Init(&gHttpRequestManager);
	task->SetCallback(&gRequestManRecentViewListCallback);
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
