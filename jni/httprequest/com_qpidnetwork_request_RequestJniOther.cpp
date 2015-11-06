#include "com_qpidnetwork_request_RequestJniOther.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"

#include <common/KZip.h>
#include <common/command.h>

#include "RequestOperator.h"
#include "RequestModifyPasswordTask.h"
#include "RequestEmotionConfigTask.h"
#include "RequestPhoneInfoTask.h"
#include "RequestVersionCheckTask.h"
#include "RequestSynConfigTask.h"
#include "RequestUploadCrashLogTask.h"

#define OS_TYPE "Android"

/************************* Modify password ***********************/
class RequestModifyPasswordTaskCallback: public IRequestModifyPasswordTaskCallback {
	void onModifyPasswordCallback(bool isSuccess, const string& errnum,
			const string& errmsg, RequestModifyPasswordTask* task) {
		FileLog("httprequest",
				"JNI::onModifyPasswordCallback( success : %s )",
				isSuccess ? "true" : "false");

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**) &env, JNI_VERSION_1_4);
		if (env == NULL) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **) &env, NULL);
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long) task);
		jclass callbackCls = env->GetObjectClass(callbackObj);

		string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
		jmethodID callback = env->GetMethodID(callbackCls, "OnRequest",
				signure.c_str());
		FileLog("httprequest",
				"JNI::onModifyPasswordCallback( callbackCls : %p, callback : %p, signure : %s )",
				callbackCls, callback, signure.c_str());

		if (callbackObj != NULL && callback != NULL) {
			jstring jerrno = env->NewStringUTF(errnum.c_str());
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			FileLog("httprequest",
					"JNI::onModifyPasswordCallback( CallObjectMethod )");

			env->CallVoidMethod(callbackObj, callback, isSuccess, jerrno,
					jerrmsg);

			env->DeleteGlobalRef(callbackObj);

			env->DeleteLocalRef(jerrno);
			env->DeleteLocalRef(jerrmsg);
		}

		if (iRet == JNI_OK) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestModifyPasswordTaskCallback gRequestModifyPasswordTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    ModifyPassword
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_ModifyPassword(
		JNIEnv *env, jclass cls, jstring oldPassword, jstring newPassword,
		jobject callback) {

	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestModifyPasswordTask *task = new RequestModifyPasswordTask();
	task->Init(&gHttpRequestManager);
	task->setParam(JString2String(env, oldPassword).c_str(),
			JString2String(env, newPassword).c_str());
	task->setCallback(&gRequestModifyPasswordTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	request->Start();

	return (long) task;
}

/************************* Emotion config ***********************/

class RequestEmotionConfigCallback: public IRequestEmotionConfigCallback {
	void onEmotionConfigCallback(bool isSuccess, const string& errnum,
			const string& errmsg, const EmotionConfigItem& emotionConfigItem,
			RequestEmotionConfigTask* task) {
		FileLog("httprequest",
				"JNI::onEmotionConfigCallback( success : %s )",
				isSuccess ? "true" : "false");

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**) &env, JNI_VERSION_1_4);
		if (env == NULL) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **) &env, NULL);
		}

		jobject jItem = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(
				OTHER_EMOTION_CONFIG_CLASS);
		if (itr != gJavaItemMap.end()) {
			jclass cls = env->GetObjectClass(itr->second);
			if (cls != NULL) {
				string strInit = "(ILjava/lang/String;";
				strInit += "[L";
				strInit += OTHER_EMOTION_CONFIG_TYPE_CLASS;
				strInit += ";[L";
				strInit += OTHER_EMOTION_CONFIG_TAG_CLASS;
				strInit += ";[L";
				strInit += OTHER_EMOTION_CONFIG_EMOTION_CLASS;
				strInit += ";[L";
				strInit += OTHER_EMOTION_CONFIG_EMOTION_CLASS;
				strInit += ";)V";
				jmethodID init = env->GetMethodID(cls, "<init>",
						strInit.c_str());
				FileLog("httprequest",
						"JNI::onEmotionConfigCallback( GetMethodID <init> : %p )",
						init);
				// ----- type list -----
				JavaItemMap::iterator typeItr = gJavaItemMap.find(OTHER_EMOTION_CONFIG_TYPE_CLASS);
				jobject jTypeItemObj = typeItr->second;
				jclass jTypeItemCls = env->GetObjectClass(jTypeItemObj);

				// create init method
				jmethodID typeInit = env->GetMethodID(jTypeItemCls, "<init>", "("
											"I"						// toFlag
											"Ljava/lang/String;"	// typeId
											"Ljava/lang/String;"	// typeName
											")V");

				// create type list
				jobjectArray jTypeArray = env->NewObjectArray(emotionConfigItem.typeList.size(), jTypeItemCls, NULL);
				int iTypeIndex;
				EmotionConfigItem::TypeList::const_iterator typeItemIter;
				for (iTypeIndex = 0, typeItemIter = emotionConfigItem.typeList.begin();
						typeItemIter != emotionConfigItem.typeList.end();
						typeItemIter++, iTypeIndex++)
				{
					jobject jTypeItem = env->NewObject(jTypeItemCls, typeInit,
							typeItemIter->toflag,
							env->NewStringUTF(typeItemIter->typeId.c_str()),
							env->NewStringUTF(typeItemIter->typeName.c_str())
							);

					env->SetObjectArrayElement(jTypeArray, iTypeIndex, jTypeItem);
					env->DeleteLocalRef(jTypeItem);
				}
				// release
				env->DeleteLocalRef(jTypeItemCls);

				// ----- tag list -----
				JavaItemMap::iterator tagItr = gJavaItemMap.find(OTHER_EMOTION_CONFIG_TAG_CLASS);
				jobject jTagItemObj = tagItr->second;
				jclass jTagItemCls = env->GetObjectClass(jTagItemObj);

				// create init method
				jmethodID tagInit = env->GetMethodID(jTagItemCls, "<init>", "("
											"I"						// toFlag
											"Ljava/lang/String;"	// typeId
											"Ljava/lang/String;"	// tagId
											"Ljava/lang/String;"	// tagName
											")V");

				// create tag list
				jobjectArray jTagArray = env->NewObjectArray(emotionConfigItem.tagList.size(), jTagItemCls, NULL);
				int iTagIndex;
				EmotionConfigItem::TagList::const_iterator tagItemIter;
				for (iTagIndex = 0, tagItemIter = emotionConfigItem.tagList.begin();
						tagItemIter != emotionConfigItem.tagList.end();
						tagItemIter++, iTagIndex++)
				{
					jobject jTagItem = env->NewObject(jTagItemCls, tagInit,
							tagItemIter->toflag,
							env->NewStringUTF(tagItemIter->typeId.c_str()),
							env->NewStringUTF(tagItemIter->tagId.c_str()),
							env->NewStringUTF(tagItemIter->tagName.c_str())
							);

					env->SetObjectArrayElement(jTagArray, iTagIndex, jTagItem);
					env->DeleteLocalRef(jTagItem);
				}
				// release
				env->DeleteLocalRef(jTagItemCls);

				// ----- emotin list -----
				JavaItemMap::iterator emotionItr = gJavaItemMap.find(OTHER_EMOTION_CONFIG_EMOTION_CLASS);
				jobject jEmotionItemObj = emotionItr->second;
				jclass jEmotionItemCls = env->GetObjectClass(jEmotionItemObj);

				// create init method
				jmethodID emotionInit = env->GetMethodID(jEmotionItemCls, "<init>", "("
											"Ljava/lang/String;"	// fileName
											"D"						// price
											"Z"						// isNew
											"Z"						// isSale
											"I"						// sortId
											"Ljava/lang/String;"	// typeId
											"Ljava/lang/String;"	// tagId
											"Ljava/lang/String;"	// title
											")V");

				// create man emotion list
				jobjectArray jManEmotionArray = env->NewObjectArray(emotionConfigItem.manEmotionList.size(), jEmotionItemCls, NULL);
				int iEmotionIndex;
				EmotionConfigItem::EmotionList::const_iterator emotionItemIter;
				for (iEmotionIndex = 0, emotionItemIter = emotionConfigItem.manEmotionList.begin();
						emotionItemIter != emotionConfigItem.manEmotionList.end();
						emotionItemIter++, iEmotionIndex++)
				{
					jobject jEmotionItem = env->NewObject(jEmotionItemCls, emotionInit,
							env->NewStringUTF(emotionItemIter->fileName.c_str()),
							emotionItemIter->price,
							emotionItemIter->isNew,
							emotionItemIter->isSale,
							emotionItemIter->sortId,
							env->NewStringUTF(emotionItemIter->typeId.c_str()),
							env->NewStringUTF(emotionItemIter->tagId.c_str()),
							env->NewStringUTF(emotionItemIter->title.c_str())
							);

					env->SetObjectArrayElement(jManEmotionArray, iEmotionIndex, jEmotionItem);
					env->DeleteLocalRef(jEmotionItem);
				}

				// create lady emotion list
				jobjectArray jLadyEmotionArray = env->NewObjectArray(emotionConfigItem.ladyEmotionList.size(), jEmotionItemCls, NULL);
				for (iEmotionIndex = 0, emotionItemIter = emotionConfigItem.ladyEmotionList.begin();
						emotionItemIter != emotionConfigItem.ladyEmotionList.end();
						emotionItemIter++, iEmotionIndex++)
				{
					jobject jEmotionItem = env->NewObject(jEmotionItemCls, emotionInit,
							env->NewStringUTF(emotionItemIter->fileName.c_str()),
							emotionItemIter->price,
							emotionItemIter->isNew,
							emotionItemIter->isSale,
							emotionItemIter->sortId,
							env->NewStringUTF(emotionItemIter->typeId.c_str()),
							env->NewStringUTF(emotionItemIter->tagId.c_str()),
							env->NewStringUTF(emotionItemIter->title.c_str())
							);

					env->SetObjectArrayElement(jLadyEmotionArray, iEmotionIndex, jEmotionItem);
					env->DeleteLocalRef(jEmotionItem);
				}
				// release
				env->DeleteLocalRef(jEmotionItemCls);

				jItem = env->NewObject(cls, init,
									emotionConfigItem.version,
									env->NewStringUTF(emotionConfigItem.path.c_str()),
									jTypeArray,
									jTagArray,
									jManEmotionArray,
									jLadyEmotionArray
									);
				env->DeleteLocalRef(jLadyEmotionArray);
				env->DeleteLocalRef(jTagArray);
				env->DeleteLocalRef(jTypeArray);
				env->DeleteLocalRef(cls);
			}
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long) task);
		jclass callbackCls = env->GetObjectClass(callbackObj);

		string signure = "(ZLjava/lang/String;Ljava/lang/String;";
		signure += "L";
		signure += OTHER_EMOTION_CONFIG_CLASS;
		signure += ";";
		signure += ")V";

		jmethodID jCallback = env->GetMethodID(callbackCls, "OnOtherEmotionConfig", signure.c_str());

		if( callbackObj != NULL && jCallback != NULL ) {
			jstring jerrno = env->NewStringUTF(errnum.c_str());
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(callbackObj, jCallback, isSuccess, jerrno, jerrmsg, jItem);

			env->DeleteLocalRef(jerrno);
			env->DeleteLocalRef(jerrmsg);
		}

		// delete callback object & class
		if (callbackObj != NULL) {
			env->DeleteGlobalRef(callbackObj);
		}
		if (callbackCls != NULL) {
			env->DeleteLocalRef(callbackCls);
		}

		if( jItem != NULL ) {
			env->DeleteLocalRef(jItem);
		}

		if (iRet == JNI_OK) {
			gJavaVM->DetachCurrentThread();
		}
	}
};


RequestEmotionConfigCallback gRequestEmotionConfigCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    EmotionConfig
 * Signature: (Lcom/qpidnetwork/request/OnOtherEmotionConfigCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_EmotionConfig(
		JNIEnv *env, jclass cls, jobject callback) {

	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestEmotionConfigTask* task = new RequestEmotionConfigTask();
	task->Init(&gHttpRequestManager);
	task->setCallback(&gRequestEmotionConfigCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	request->Start();

	return (long) task;
}


/*************************** Phone Info ****************************************/

class RequestPhoneInfoTaskCallback : public IRequestPhoneInfoTaskCallback{
	void onPhoneInfoCallback(bool isSuccess, const string& errnum, const string& errmsg, RequestPhoneInfoTask* task){
		FileLog("httprequest","JNI::onPhoneInfoCallback( success : %s )",
						isSuccess ? "true" : "false");
		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**) &env, JNI_VERSION_1_4);
		if (env == NULL) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **) &env, NULL);
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long) task);
		jclass callbackCls = env->GetObjectClass(callbackObj);

		string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
		jmethodID callback = env->GetMethodID(callbackCls, "OnRequest",
				signure.c_str());
		FileLog("httprequest","JNI::onPhoneInfoCallback( callbackCls : %p, callback : %p, signure : %s )",
				callbackCls, callback, signure.c_str());

		if (callbackObj != NULL && callback != NULL) {
			jstring jerrno = env->NewStringUTF(errnum.c_str());
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			FileLog("httprequest","JNI::onPhoneInfoCallback( CallObjectMethod )");

			env->CallVoidMethod(callbackObj, callback, isSuccess, jerrno,
					jerrmsg);

			env->DeleteGlobalRef(callbackObj);

			env->DeleteLocalRef(jerrno);
			env->DeleteLocalRef(jerrmsg);
		}

		if (iRet == JNI_OK) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestPhoneInfoTaskCallback gRequestPhoneInfoTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    PhoneInfo
 * Signature: (Ljava/lang/String;Ljava/lang/String;IIIILjava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_PhoneInfo
  (JNIEnv *env, jclass cls, jstring userAccount, jstring verName, jint action, jint siteId, jint width, jint height, jstring deviceId, jobject callback){

	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestPhoneInfoTask* task = new RequestPhoneInfoTask();
	task->Init(&gHttpRequestManager);
	task->setCallback(&gRequestPhoneInfoTaskCallback);

	string strDensityDpi = GetPhoneDensityDPI();
	string strModel = GetPhoneModel();
	string strManufacturer = GetPhoneManufacturer();
	string strOS = OS_TYPE;
	string strRelease = GetPhoneBuildVersion();
	string strSDK = GetPhoneBuildSDKVersion();
	string strLanguage = GetPhoneLocalLanguage();
	string strCountry = GetPhoneLocalRegion();

	task->setParams(strModel, strManufacturer, strOS, strRelease, strSDK, strDensityDpi, width, height,
			JString2String(env, userAccount).c_str(), JString2String(env, verName).c_str(), strLanguage, strCountry, siteId,
			action, JString2String(env, deviceId).c_str());

	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	request->Start();

	return (long) task;
}

/*************************** Check version ********************************************/
class RequestVersionCheckTaskCallback : public IRequestVersionCheckTaskCallback{
	void onVersionCheckCallback(bool isSuccess, const string& errnum, const string& errmsg, const VersionCheckItem& item, RequestVersionCheckTask* task){
		FileLog("httprequest","JNI::onVersionCheckCallback( success : %s )", isSuccess ? "true" : "false");
		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**) &env, JNI_VERSION_1_4);
		if (env == NULL) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **) &env, NULL);
		}

		jobject jItem = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(OTHER_CHECK_VERSION_CLASS);
		if(itr != gJavaItemMap.end()){
			jclass jItemCls = env->GetObjectClass(itr->second);
			FileLog("httprequest","JNI::onVersionCheckCallback OTHER_CHECK_VERSION_CLASS");
			jmethodID jItemInit = env->GetMethodID(jItemCls, "<init>", "(ILjava/lang/String;Ljava/lang/String;)V");
			jItem = env->NewObject(jItemCls, jItemInit, item.apkVersionCode, env->NewStringUTF(item.apkVersionName.c_str()), env->NewStringUTF(item.apkUrl.c_str()));
			env->DeleteLocalRef(jItemCls);
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long) task);
		jclass callbackCls = env->GetObjectClass(callbackObj);

		string signure = "(ZLjava/lang/String;Ljava/lang/String;";
		signure	+= "L";
		signure	+= OTHER_CHECK_VERSION_CLASS;
		signure	+= ";)V";
		jmethodID callback = env->GetMethodID(callbackCls, "OnVersionCheck", signure.c_str());

		if (callbackObj != NULL && callback != NULL) {
			jstring jerrno = env->NewStringUTF(errnum.c_str());
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			FileLog("httprequest","JNI::onVersionCheckCallback( CallObjectMethod )");

			env->CallVoidMethod(callbackObj, callback, isSuccess, jerrno, jerrmsg, jItem);

			env->DeleteGlobalRef(callbackObj);

			env->DeleteLocalRef(jerrno);
			env->DeleteLocalRef(jerrmsg);
		}
		env->DeleteLocalRef(jItem);

		if (iRet == JNI_OK) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestVersionCheckTaskCallback gRequestVersionCheckTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    VersionCheck
 * Signature: (Lcom/qpidnetwork/request/OnVersionCheckCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_VersionCheck
  (JNIEnv *env, jclass cls, jobject callback){

	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestVersionCheckTask* task = new RequestVersionCheckTask();
	task->Init(&gHttpRequestManager);
	task->setCallback(&gRequestVersionCheckTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	request->Start();

	return (long) task;
}

/************************ Syn config *********************************/

class RequestSynConfigTaskCallback : public IRequestSynConfigTaskCallback{
	void onSynConfigCallback(bool isSuccess, const string& errnum, const string& errmsg, const SynConfigItem& item, RequestSynConfigTask* task){
		FileLog("httprequest","JNI::onSynConfigCallback( success : %s )", isSuccess ? "true" : "false");
		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**) &env, JNI_VERSION_1_4);
		if (env == NULL) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **) &env, NULL);
		}

		jobject jItem = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(OTHER_SYN_CONFIG_CLASS);
		if(itr != gJavaItemMap.end()){
			jclass jItemClass = env->GetObjectClass(itr->second);
			string itemInitString = "(Ljava/lang/String;ILjava/lang/String;ILjava/lang/String;[Ljava/lang/String;"
					"ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
			jmethodID jItemInit = env->GetMethodID(jItemClass, "<init>", itemInitString.c_str());

			//languages
			jobjectArray jLanguages = env->NewObjectArray(item.translateLanguage.size(), env->FindClass("java/lang/String"), NULL);
			list<string>::const_iterator languageItr;
			int iLanguageIndex;
			for(iLanguageIndex=0, languageItr=item.translateLanguage.begin(); languageItr!=item.translateLanguage.end(); iLanguageIndex++, languageItr++){
				env->SetObjectArrayElement(jLanguages, iLanguageIndex, env->NewStringUTF((*languageItr).c_str()));
			}
			jItem = env->NewObject(jItemClass, jItemInit, env->NewStringUTF(item.socketHost.c_str()),
					item.socketPort, env->NewStringUTF(item.socketVersion.c_str()), item.socketFromId,
					env->NewStringUTF(item.translateUrl.c_str()), jLanguages, item.apkVersionCode, env->NewStringUTF(item.apkVersionName.c_str()),
					env->NewStringUTF(item.apkVersionUrl.c_str()), env->NewStringUTF(item.siteUrl.c_str()));
			env->DeleteLocalRef(jLanguages);
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long) task);
		jclass callbackCls = env->GetObjectClass(callbackObj);

		string signure = "(ZLjava/lang/String;Ljava/lang/String;";
		signure	+= 	"L";
		signure	+= 	OTHER_SYN_CONFIG_CLASS;
		signure	+= 	";)V";
		jmethodID callback = env->GetMethodID(callbackCls, "OnSynConfig", signure.c_str());

		if (callbackObj != NULL && callback != NULL) {
			jstring jerrno = env->NewStringUTF(errnum.c_str());
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			FileLog("httprequest","JNI::onSynConfigCallback( CallObjectMethod )");

			env->CallVoidMethod(callbackObj, callback, isSuccess, jerrno, jerrmsg, jItem);

			env->DeleteGlobalRef(callbackObj);

			env->DeleteLocalRef(jerrno);
			env->DeleteLocalRef(jerrmsg);
		}
		env->DeleteLocalRef(jItem);

		if (iRet == JNI_OK) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestSynConfigTaskCallback gRequestSynConfigTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    SynConfig
 * Signature: (Lcom/qpidnetwork/request/OnSynConfigCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_SynConfig
  (JNIEnv *env, jclass cls, jobject callback){

	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestSynConfigTask* task = new RequestSynConfigTask();
	task->Init(&gHttpRequestManager);
	task->setCallback(&gRequestSynConfigTaskCallback);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	request->Start();

	return (long) task;
}

/*************************** Upload Crash *********************************/
class RequestUploadCrashLogTaskCallback : public IRequestUploadCrashLogTaskCallback{
	void onUploadCrashLogCallback(bool isSuccess, const string& errnum, const string& errmsg, RequestUploadCrashLogTask* task){
		FileLog("httprequest","JNI::onUploadCrashLogCallback( success : %s )",
								isSuccess ? "true" : "false");
		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**) &env, JNI_VERSION_1_4);
		if (env == NULL) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **) &env, NULL);
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long) task);
		jclass callbackCls = env->GetObjectClass(callbackObj);

		string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
		jmethodID callback = env->GetMethodID(callbackCls, "OnRequest",
				signure.c_str());
		FileLog("httprequest","JNI::onUploadCrashLogCallback( callbackCls : %p, callback : %p, signure : %s )",
				callbackCls, callback, signure.c_str());

		if (callbackObj != NULL && callback != NULL) {
			jstring jerrno = env->NewStringUTF(errnum.c_str());
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			FileLog("httprequest","RequestUploadCrashLogTaskCallback::onUploadCrashLogCallback( CallObjectMethod )");

			env->CallVoidMethod(callbackObj, callback, isSuccess, jerrno,
					jerrmsg);

			env->DeleteGlobalRef(callbackObj);

			env->DeleteLocalRef(jerrno);
			env->DeleteLocalRef(jerrmsg);
		}

		if (iRet == JNI_OK) {
			gJavaVM->DetachCurrentThread();
		}
	}
};

RequestUploadCrashLogTaskCallback gRequestUploadCrashLogTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    UploadCrashLog
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_UploadCrashLog
  (JNIEnv *env, jclass cls, jstring deviceId, jstring directory, jstring tempDirectory, jobject callback){

	const char *cpDirectory = env->GetStringUTFChars(directory, 0);
	const char *cpTmpDirectory = env->GetStringUTFChars(tempDirectory, 0);

	FileLog("httprequest", "UploadCrashLog ( directory : %s, tmpDirectory : %s ) ", cpDirectory, cpTmpDirectory);

	time_t stm = time(NULL);
	struct tm tTime;
	localtime_r(&stm, &tTime);
	char pZipFileName[1024] = {'\0'};
	snprintf(pZipFileName, sizeof(pZipFileName), "%s/crash-%d-%02d-%02d_[%02d-%02d-%02d].zip", \
			cpTmpDirectory, tTime.tm_year + 1900, tTime.tm_mon + 1, \
			tTime.tm_mday, tTime.tm_hour, tTime.tm_min, tTime.tm_sec);

	// create zip
	KZip zip;
	string comment = "";
	const char password[] = {
			0x51, 0x70, 0x69, 0x64, 0x5F, 0x44, 0x61, 0x74, 0x69, 0x6E, 0x67, 0x00
	}; // Qpid_Dating

	bool bFlag = zip.CreateZipFromDir(cpDirectory, pZipFileName, password, comment);

	FileLog("httprequest", "UploadCrashLog ( pZipFileName : %s  zip  : %s ) ", pZipFileName, bFlag?"ok":"fail");

	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestUploadCrashLogTask* task = new RequestUploadCrashLogTask();
	task->Init(&gHttpRequestManager);
	task->setCallback(&gRequestUploadCrashLogTaskCallback);
	task->setParam(JString2String(env, deviceId).c_str(), pZipFileName);
	task->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);

	env->ReleaseStringUTFChars(directory, cpDirectory);
	env->ReleaseStringUTFChars(tempDirectory, cpTmpDirectory);

	jobject obj = env->NewGlobalRef(callback);
	long id = (long) task;
	gCallbackMap.Insert(id, obj);

	gRequestMapMutex.lock();
	gRequestMap.insert(RequestMap::value_type((long)task, true));
	gRequestMap.insert(RequestMap::value_type((long)request, true));
	gRequestMapMutex.unlock();

	request->SetTask(task);
	request->SetTaskCallback((ITaskCallback*) &gRequestFinishCallback);
	request->Start();

	return (long) task;
}
