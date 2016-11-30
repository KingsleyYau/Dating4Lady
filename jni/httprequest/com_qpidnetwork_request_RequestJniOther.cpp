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
#include "RequestGetAgentInfoTask.h"
#include "RequestMyProfileTask.h"

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

		if (callbackObj != NULL ) {
			if( callback != NULL) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest",
						"JNI::onModifyPasswordCallback( CallObjectMethod )");

				env->CallVoidMethod(callbackObj, callback, isSuccess, jerrno,
						jerrmsg);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
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

	jobject obj = env->NewGlobalRef(callback);
	long id = (long) task;
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
					jstring jTypeId = env->NewStringUTF(typeItemIter->typeId.c_str());
					jstring jTypeName = env->NewStringUTF(typeItemIter->typeName.c_str());

					jobject jTypeItem = env->NewObject(jTypeItemCls, typeInit,
							typeItemIter->toflag,
							jTypeId,
							jTypeName
							);

					env->DeleteLocalRef(jTypeId);
					env->DeleteLocalRef(jTypeName);

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
					jstring jTypeId = env->NewStringUTF(tagItemIter->typeId.c_str());
					jstring jTagId = env->NewStringUTF(tagItemIter->tagId.c_str());
					jstring jTagName = env->NewStringUTF(tagItemIter->tagName.c_str());

					jobject jTagItem = env->NewObject(jTagItemCls, tagInit,
							tagItemIter->toflag,
							jTypeId,
							jTagId,
							jTagName
							);

					env->DeleteLocalRef(jTypeId);
					env->DeleteLocalRef(jTagId);
					env->DeleteLocalRef(jTagName);

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
					jstring jFileName = env->NewStringUTF(emotionItemIter->fileName.c_str());
					jstring jTypeId = env->NewStringUTF(emotionItemIter->typeId.c_str());
					jstring jTagId = env->NewStringUTF(emotionItemIter->tagId.c_str());
					jstring jTitle = env->NewStringUTF(emotionItemIter->title.c_str());

					jobject jEmotionItem = env->NewObject(jEmotionItemCls, emotionInit,
							jFileName,
							emotionItemIter->price,
							emotionItemIter->isNew,
							emotionItemIter->isSale,
							emotionItemIter->sortId,
							jTypeId,
							jTagId,
							jTitle
							);

					env->DeleteLocalRef(jFileName);
					env->DeleteLocalRef(jTypeId);
					env->DeleteLocalRef(jTagId);
					env->DeleteLocalRef(jTitle);

					env->SetObjectArrayElement(jManEmotionArray, iEmotionIndex, jEmotionItem);
					env->DeleteLocalRef(jEmotionItem);
				}

				// create lady emotion list
				jobjectArray jLadyEmotionArray = env->NewObjectArray(emotionConfigItem.ladyEmotionList.size(), jEmotionItemCls, NULL);
				for (iEmotionIndex = 0, emotionItemIter = emotionConfigItem.ladyEmotionList.begin();
						emotionItemIter != emotionConfigItem.ladyEmotionList.end();
						emotionItemIter++, iEmotionIndex++)
				{
					jstring jFileName = env->NewStringUTF(emotionItemIter->fileName.c_str());
					jstring jTypeId = env->NewStringUTF(emotionItemIter->typeId.c_str());
					jstring jTagId = env->NewStringUTF(emotionItemIter->tagId.c_str());
					jstring jTitle = env->NewStringUTF(emotionItemIter->title.c_str());

					jobject jEmotionItem = env->NewObject(jEmotionItemCls, emotionInit,
							jFileName,
							emotionItemIter->price,
							emotionItemIter->isNew,
							emotionItemIter->isSale,
							emotionItemIter->sortId,
							jTypeId,
							jTagId,
							jTitle
							);

					env->DeleteLocalRef(jFileName);
					env->DeleteLocalRef(jTypeId);
					env->DeleteLocalRef(jTagId);
					env->DeleteLocalRef(jTitle);

					env->SetObjectArrayElement(jLadyEmotionArray, iEmotionIndex, jEmotionItem);
					env->DeleteLocalRef(jEmotionItem);
				}
				// release
				env->DeleteLocalRef(jEmotionItemCls);

				jstring jPath = env->NewStringUTF(emotionConfigItem.path.c_str());
				jItem = env->NewObject(cls, init,
									emotionConfigItem.version,
									jPath,
									jTypeArray,
									jTagArray,
									jManEmotionArray,
									jLadyEmotionArray
									);
				env->DeleteLocalRef(jPath);

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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
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

		if(callbackObj != NULL ){
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
			jmethodID callback = env->GetMethodID(callbackCls, "OnRequest",
					signure.c_str());
			FileLog("httprequest","JNI::onPhoneInfoCallback( callbackCls : %p, callback : %p, signure : %s )",
					callbackCls, callback, signure.c_str());
			if( callback != NULL) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest","JNI::onPhoneInfoCallback( CallObjectMethod )");

				env->CallVoidMethod(callbackObj, callback, isSuccess, jerrno,
						jerrmsg);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
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
	FileLog("httprequest","JNI::PhoneInfo( strDensityDpi : %s, strModel : %s, strManufacturer : %s )",
			strDensityDpi.c_str(), strModel.c_str(), strManufacturer.c_str());

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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
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

			jstring jApkVersionName = env->NewStringUTF(item.apkVersionName.c_str());
			jstring jApkUrl = env->NewStringUTF(item.apkUrl.c_str());
			jItem = env->NewObject(jItemCls, jItemInit, item.apkVersionCode, jApkVersionName, jApkUrl);
			env->DeleteLocalRef(jApkVersionName);
			env->DeleteLocalRef(jApkUrl);

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

		if (callbackObj != NULL ) {
			if( callback != NULL) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest","JNI::onVersionCheckCallback( CallObjectMethod )");

				env->CallVoidMethod(callbackObj, callback, isSuccess, jerrno, jerrmsg, jItem);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
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
					"ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)V";
			jmethodID jItemInit = env->GetMethodID(jItemClass, "<init>", itemInitString.c_str());

			//languages
			jobjectArray jLanguages = env->NewObjectArray(item.translateLanguage.size(), env->FindClass("java/lang/String"), NULL);
			list<string>::const_iterator languageItr;
			int iLanguageIndex;
			for(iLanguageIndex=0, languageItr=item.translateLanguage.begin(); languageItr!=item.translateLanguage.end(); iLanguageIndex++, languageItr++){
				jstring jLanguage = env->NewStringUTF((*languageItr).c_str());
				env->SetObjectArrayElement(jLanguages, iLanguageIndex, jLanguage);
				env->DeleteLocalRef(jLanguage);
			}

			jstring jSocketHost = env->NewStringUTF(item.socketHost.c_str());
			jstring jSocketVersion = env->NewStringUTF(item.socketVersion.c_str());
			jstring jTranslateUrl = env->NewStringUTF(item.translateUrl.c_str());
			jstring jApkVersionName = env->NewStringUTF(item.apkVersionName.c_str());
			jstring jApkVersionUrl = env->NewStringUTF(item.apkVersionUrl.c_str());
			jstring jSiteUrl = env->NewStringUTF(item.siteUrl.c_str());
			jstring jLiveChatVoiceHost = env->NewStringUTF(item.liveChatVoiceHost.c_str());
			jItem = env->NewObject(jItemClass, jItemInit, jSocketHost,
					item.socketPort, jSocketVersion, item.socketFromId,
					jTranslateUrl, jLanguages, item.apkVersionCode, jApkVersionName,
					jApkVersionUrl, jSiteUrl, jLiveChatVoiceHost, item.privatePhotoMax, item.privatePhotoMin);
			env->DeleteLocalRef(jSocketHost);
			env->DeleteLocalRef(jSocketVersion);
			env->DeleteLocalRef(jTranslateUrl);
			env->DeleteLocalRef(jApkVersionName);
			env->DeleteLocalRef(jApkVersionUrl);
			env->DeleteLocalRef(jSiteUrl);
			env->DeleteLocalRef(jLiveChatVoiceHost);

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

		if (callbackObj != NULL ) {
			if( callback != NULL) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest","JNI::onSynConfigCallback( CallObjectMethod )");

				env->CallVoidMethod(callbackObj, callback, isSuccess, jerrno, jerrmsg, jItem);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
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

		if (callbackObj != NULL ) {
			if( callback != NULL) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest","RequestUploadCrashLogTaskCallback::onUploadCrashLogCallback( CallObjectMethod )");

				env->CallVoidMethod(callbackObj, callback, isSuccess, jerrno,
						jerrmsg);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
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

	string strDirectory = JString2String(env, directory);
	string strTmpDirectory = JString2String(env, tempDirectory);

	FileLog("httprequest", "UploadCrashLog ( directory : %s, tmpDirectory : %s ) ", strDirectory.c_str(), strTmpDirectory.c_str());

	time_t stm = time(NULL);
	struct tm tTime;
	localtime_r(&stm, &tTime);
	char pZipFileName[1024] = {'\0'};
	snprintf(pZipFileName, sizeof(pZipFileName), "%s/crash-%d-%02d-%02d_[%02d-%02d-%02d].zip", \
			strTmpDirectory.c_str(), tTime.tm_year + 1900, tTime.tm_mon + 1, \
			tTime.tm_mday, tTime.tm_hour, tTime.tm_min, tTime.tm_sec);

	// create zip
	KZip zip;
	string comment = "";
	const char password[] = {
			0x51, 0x70, 0x69, 0x64, 0x5F, 0x44, 0x61, 0x74, 0x69, 0x6E, 0x67, 0x00
	}; // Qpid_Dating

	bool bFlag = zip.CreateZipFromDir(strDirectory, pZipFileName, "", comment);

	FileLog("httprequest", "UploadCrashLog ( pZipFileName : %s  zip  : %s ) ", pZipFileName, bFlag?"ok":"fail");

	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestUploadCrashLogTask* task = new RequestUploadCrashLogTask();
	task->Init(&gHttpRequestManager);
	task->setCallback(&gRequestUploadCrashLogTaskCallback);
	task->setParam(JString2String(env, deviceId).c_str(), pZipFileName);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/*************************** GetAgentInfo *********************************/
class RequestGetAgentInfoTaskCallback : public IRequestGetAgentInfoTaskCallback
{
public:
	RequestGetAgentInfoTaskCallback() {};
	virtual ~RequestGetAgentInfoTaskCallback() {};
public:
	void onGetAgentInfoCallback(bool isSuccess, const string& errnum, const string& errmsg, const AgentInfoItem& item, RequestGetAgentInfoTask* task)
	{
		FileLog("httprequest","JNI::onGetAgentInfoCallback( success : %s )", isSuccess ? "true" : "false");
		/* turn object to java object here */
		JNIEnv* env;
		bool isAttachThread;
		GetEnv(&env, &isAttachThread);

		jobject jItem = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(OTHER_AGENTINFO_CLASS);
		if(itr != gJavaItemMap.end())
		{
			jclass jItemClass = env->GetObjectClass(itr->second);
			string itemInitString = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;"
					"Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;"
					"Ljava/lang/String;Ljava/lang/String;)V";
			jmethodID jItemInit = env->GetMethodID(jItemClass, "<init>", itemInitString.c_str());

			jstring jName = env->NewStringUTF(item.name.c_str());
			jstring jId = env->NewStringUTF(item.id.c_str());
			jstring jCity = env->NewStringUTF(item.city.c_str());
			jstring jAddr = env->NewStringUTF(item.addr.c_str());
			jstring jEmail = env->NewStringUTF(item.email.c_str());
			jstring jTel = env->NewStringUTF(item.tel.c_str());
			jstring jFax = env->NewStringUTF(item.fax.c_str());
			jstring jContact = env->NewStringUTF(item.contact.c_str());
			jstring jPostcode = env->NewStringUTF(item.postcode.c_str());
			jItem = env->NewObject(
						jItemClass
						, jItemInit
						, jName
						, jId
						, jCity
						, jAddr
						, jEmail
						, jTel
						, jFax
						, jContact
						, jPostcode);
			env->DeleteLocalRef(jName);
			env->DeleteLocalRef(jId);
			env->DeleteLocalRef(jCity);
			env->DeleteLocalRef(jAddr);
			env->DeleteLocalRef(jEmail);
			env->DeleteLocalRef(jTel);
			env->DeleteLocalRef(jFax);
			env->DeleteLocalRef(jContact);
			env->DeleteLocalRef(jPostcode);
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long) task);
		jclass callbackCls = env->GetObjectClass(callbackObj);

		string signure = "(JZLjava/lang/String;Ljava/lang/String;";
		signure	+= 	"L";
		signure	+= 	OTHER_AGENTINFO_CLASS;
		signure	+= 	";)V";
		jmethodID callback = env->GetMethodID(callbackCls, "OnOtherGetAgentInfo", signure.c_str());

		if (callbackObj != NULL ) {
			if( callback != NULL) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jlong requestId = (jlong)task;
				FileLog("httprequest","JNI::onGetAgentInfoCallback() callbackObj:%p, callback:%p", callbackObj, callback);

				env->CallVoidMethod(callbackObj, callback, requestId, isSuccess, jerrno, jerrmsg, jItem);

				FileLog("httprequest","JNI::onGetAgentInfoCallback() ok");
				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}
		env->DeleteLocalRef(jItem);

		ReleaseEnv(isAttachThread);
	}
};

RequestGetAgentInfoTaskCallback gRequestGetAgentInfoTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    GetAgentInfo
 * Signature: (Lcom/qpidnetwork/request/OnOtherGetAgentInfoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_GetAgentInfo
  (JNIEnv *env, jclass cls, jobject callback)
{
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestGetAgentInfoTask* task = new RequestGetAgentInfoTask();
	task->Init(&gHttpRequestManager);
	task->setCallback(&gRequestGetAgentInfoTaskCallback);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}

/*************************** QueryLadyDetail *********************************/
class RequestMyProfileTaskCallback : public IRequestMyProfileTaskCallback
{
public:
	RequestMyProfileTaskCallback() {};
	virtual ~RequestMyProfileTaskCallback() {};

public:
	virtual void onMyProfileCallback(bool isSuccess, const string& errnum, const string& errmsg, const MyProfileItem& item, RequestMyProfileTask* task)
	{
		FileLog("httprequest","JNI::onMyProfileCallback( success : %s )", isSuccess ? "true" : "false");
		/* turn object to java object here */
		JNIEnv* env;
		bool isAttachThread;
		GetEnv(&env, &isAttachThread);

		jobject jItem = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(OTHER_MYPROFILE_CLASS);
		if(itr != gJavaItemMap.end())
		{
			jclass jItemClass = env->GetObjectClass(itr->second);
			string itemInitString = "("
					"Ljava/lang/String;"		// ladyId
					"Ljava/lang/String;"		// firstname
					"Ljava/lang/String;"		// lastname
					"I"							// age
					"Ljava/lang/String;"		// country
					"Ljava/lang/String;"		// province
					"Ljava/lang/String;"		// city
					"Ljava/lang/String;"		// birthday
					"I"							// zodiac
					"Ljava/lang/String;"		// weight
					"Ljava/lang/String;"		// height
					"I"							// smoke
					"I"							// drink
					"I"							// english
					"I"							// religion
					"I"							// education
					"I"							// profession
					"I"							// children
					"I"							// marry
					"Ljava/lang/String;"		// aboutMe
					"Ljava/lang/String;"		// manAge1
					"Ljava/lang/String;"		// manAge2
					"Ljava/lang/String;"		// lastRefresh
					"Ljava/lang/String;"		// photoUrl
					"[Ljava/lang/String;"		// photoUrls
					"[Ljava/lang/String;"		// thumbUrls
					")V";
			jmethodID jItemInit = env->GetMethodID(jItemClass, "<init>", itemInitString.c_str());

			jstring jId = env->NewStringUTF(item.id.c_str());
			jstring jFirstname = env->NewStringUTF(item.firstname.c_str());
			jstring jLastname = env->NewStringUTF(item.lastname.c_str());
			jstring jCountry = env->NewStringUTF(item.country.c_str());
			jstring jProvince = env->NewStringUTF(item.province.c_str());
			jstring jCity = env->NewStringUTF(item.city.c_str());
			jstring jBirthday = env->NewStringUTF(item.birthday.c_str());
			jstring jWeight = env->NewStringUTF(item.weight.c_str());
			jstring jHeight = env->NewStringUTF(item.height.c_str());
			jstring jManAge1 = env->NewStringUTF(item.manAge1.c_str());
			jstring jManAge2 = env->NewStringUTF(item.manAge2.c_str());
			jstring jAboutMe = env->NewStringUTF(item.aboutMe.c_str());
			jstring jPhotoUrl = env->NewStringUTF(item.photoUrl.c_str());
			jstring jLastRefresh = env->NewStringUTF(item.lastRefresh.c_str());

			jclass jStringCls = env->FindClass("java/lang/String");
			// photoUrls
			jobjectArray jPhotoUrlsArray = env->NewObjectArray(item.photoUrls.size(), jStringCls, NULL);
			if (NULL != jPhotoUrlsArray) {
				int i = 0;
				for (list<string>::const_iterator itr = item.photoUrls.begin()
					; itr != item.photoUrls.end()
					; itr++, i++)
				{
					jstring str = env->NewStringUTF((*itr).c_str());
					env->SetObjectArrayElement(jPhotoUrlsArray, i, str);
					env->DeleteLocalRef(str);
				}
			}

			// thumbUrls
			jobjectArray jThumbUrlsArray = env->NewObjectArray(item.thumbUrls.size(), jStringCls, NULL);
			if (NULL != jThumbUrlsArray) {
				int i = 0;
				for (list<string>::const_iterator itr = item.thumbUrls.begin()
					; itr != item.thumbUrls.end()
					; itr++, i++)
				{
					jstring str = env->NewStringUTF((*itr).c_str());
					env->SetObjectArrayElement(jThumbUrlsArray, i, str);
					env->DeleteLocalRef(str);
				}
			}

			jItem = env->NewObject(jItemClass, jItemInit
						, jId
						, jFirstname
						, jLastname
						, item.age
						, jCountry
						, jProvince
						, jCity
						, jBirthday
						, item.zodiac
						, jWeight
						, jHeight
						, item.smoke
						, item.drink
						, item.english
						, item.religion
						, item.education
						, item.profession
						, item.children
						, item.marry
						, jAboutMe
						, jManAge1
						, jManAge2
						, jLastRefresh
						, jPhotoUrl
						, jPhotoUrlsArray
						, jThumbUrlsArray);
			env->DeleteLocalRef(jId);
			env->DeleteLocalRef(jFirstname);
			env->DeleteLocalRef(jLastname);
			env->DeleteLocalRef(jCountry);
			env->DeleteLocalRef(jProvince);
			env->DeleteLocalRef(jCity);
			env->DeleteLocalRef(jBirthday);
			env->DeleteLocalRef(jWeight);
			env->DeleteLocalRef(jHeight);
			env->DeleteLocalRef(jManAge1);
			env->DeleteLocalRef(jManAge2);
			env->DeleteLocalRef(jAboutMe);
			env->DeleteLocalRef(jPhotoUrl);
			env->DeleteLocalRef(jLastRefresh);
			env->DeleteLocalRef(jPhotoUrlsArray);
			env->DeleteLocalRef(jThumbUrlsArray);
		}

		/* real callback java */
		jobject callbackObj = gCallbackMap.Erase((long) task);
		jclass callbackCls = env->GetObjectClass(callbackObj);

		string signure = "(JZLjava/lang/String;Ljava/lang/String;";
		signure	+= 	"L";
		signure	+= 	OTHER_MYPROFILE_CLASS;
		signure	+= 	";)V";
		jmethodID callback = env->GetMethodID(callbackCls, "OnQueryMyProfileDetail", signure.c_str());

		if (callbackObj != NULL ) {
			if( callback != NULL) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
				jlong requestId = (jlong)task;
				FileLog("httprequest","JNI::onMyProfileCallback() callbackObj:%p, callback:%p", callbackObj, callback);

				env->CallVoidMethod(callbackObj, callback, requestId, isSuccess, jerrno, jerrmsg, jItem);

				FileLog("httprequest","JNI::onMyProfileCallback() ok");
				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
			env->DeleteGlobalRef(callbackObj);
		}
		env->DeleteLocalRef(jItem);

		ReleaseEnv(isAttachThread);
	}
};
RequestMyProfileTaskCallback gRequestMyProfileTaskCallback;

/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    QueryMyPrfile
 * Signature: (Lcom/qpidnetwork/request/OnQueryMyProfileCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_QueryMyProfile
  (JNIEnv *env, jclass cls, jobject callback)
{
	// 增加Session超时处理
	RequestOperator* request = new RequestOperator();

	RequestMyProfileTask* task = new RequestMyProfileTask();
	task->Init(&gHttpRequestManager);
	task->setCallback(&gRequestMyProfileTaskCallback);
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
	bool result = request->Start();

	return result ? (long)task : HTTPREQUEST_INVALIDREQUESTID;
}
