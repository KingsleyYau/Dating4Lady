/*
 * com_qpidnetwork_http_request_RequestJni.cpp
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include "com_qpidnetwork_request_RequestJni.h"
#include "ITask.h"

#include <crashhandler/CrashHandler.h>

#include <common/IPAddress.h>
#include <common/md5.h>
#include <common/command.h>
#include <common/KZip.h>
#include "RequestBaseTask.h"

#define VERSIONCODE_KEY "versioncode"

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetLogDirectory
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetLogDirectory
  (JNIEnv *env, jclass, jstring directory) {
	string strDirectory = JString2String(env, directory);

	KLog::SetLogDirectory(strDirectory);
	HttpClient::SetLogDirectory(strDirectory);
	CrashHandler::GetInstance()->SetLogDirectory(strDirectory);

	GetPhoneInfo();

	FileLog("httprequest", "Jni::SetLogDirectory ( directory : %s ) ", strDirectory.c_str());
	FileLog("httprequest", "Jni::SetLogDirectory ( Android CPU ABI : %s ) ", GetPhoneCpuAbi().c_str());
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetVersionCode
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetVersionCode
  (JNIEnv *env, jclass cls, jstring version) {
	string strVersion = JString2String(env, version);
	gHttpRequestManager.SetVersionCode(VERSIONCODE_KEY, strVersion);
	CrashHandler::GetInstance()->SetVersion(strVersion);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetCookiesDirectory
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetCookiesDirectory
  (JNIEnv *env, jclass, jstring directory) {
	string strDirectory = JString2String(env, directory);

	HttpClient::SetCookiesDirectory(strDirectory);

	FileLog("httprequest", "Jni::SetCookiesDirectory ( directory : %s ) ", strDirectory.c_str());
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetWebSite
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetWebSite
  (JNIEnv *env, jclass cls, jstring webSite, jstring appSite) {
	string strWebSite = JString2String(env, webSite);
	string strAppSite = JString2String(env, appSite);

	gHttpRequestHostManager.SetWebSite(strWebSite);
	gHttpRequestHostManager.SetAppSite(strAppSite);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetTransSite
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetTransSite
  (JNIEnv *env, jclass cls, jstring transSite)
{
	string strTransSite = JString2String(env, transSite);
	gHttpRequestHostManager.SetTransSite(strTransSite);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetVideoUploadSite
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetVideoUploadSite
  (JNIEnv *env, jclass cls, jstring videoUploadSite){
	string strVideoUploadSite = JString2String(env, videoUploadSite);
	gHttpRequestHostManager.SetVideoUploadSite(strVideoUploadSite);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetAuthorization
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetAuthorization
  (JNIEnv *env, jclass, jstring user, jstring password) {
	string strUser = JString2String(env, user);
	string strPassword = JString2String(env, password);
	gHttpRequestManager.SetAuthorization(strUser, strPassword);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    CleanCookies
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_CleanCookies
  (JNIEnv *, jclass) {
	HttpClient::CleanCookies();
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetCookies
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_qpidnetwork_request_RequestJni_GetCookies
  (JNIEnv *env, jclass, jstring site) {
	string strSite = JString2String(env, site);
	string cookies = HttpClient::GetCookies(strSite);
	return env->NewStringUTF(cookies.c_str());
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    StopRequest
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_StopRequest
  (JNIEnv *env, jclass, jlong requestId) {
	ITask* task = (ITask*) requestId;
	FileLog("httprequest", "Jni::StopRequest ( task : %p ) ", task);

	gRequestMapMutex.lock();
	RequestMap::iterator itr = gRequestMap.find((long long)task);
	if( itr != gRequestMap.end() ) {
		if( task != NULL ) {
			task->Stop();
		}
	}
	gRequestMapMutex.unlock();
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetRequestFinishCallback
 * Signature: (Lcom/qpidnetwork/request/OnRequestFinishCallback;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetRequestFinishCallback
  (JNIEnv *env, jclass, jobject callback) {
	if( requestFinishCallback != NULL ) {
		env->DeleteGlobalRef(requestFinishCallback);
	}

	if( callback != NULL ) {
		requestFinishCallback = env->NewGlobalRef(callback);
	}
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    StopAllRequest
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_StopAllRequest
  (JNIEnv *env, jclass) {
//	gHttpRequestManager.StopAllRequest();
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetDeviceId
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_qpidnetwork_request_RequestJni_GetDeviceId
  (JNIEnv *env, jclass) {
	string macAddress = "";

	list<IpAddressNetworkInfo> infoList = IPAddress::GetNetworkInfoList();
	if( infoList.size() > 0 && infoList.begin() != infoList.end() ) {
		IpAddressNetworkInfo info = *(infoList.begin());
		macAddress = info.mac;
	}

	char deviceId[128] = {'\0'};
	memset(deviceId, '\0', sizeof(deviceId));
	GetMD5String(macAddress.c_str(), deviceId);

	return env->NewStringUTF(deviceId);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetDeviceId
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetDeviceId
  (JNIEnv *env, jclass, jstring deviceId) {
	string strDeviceId = JString2String(env, deviceId);
	CrashHandler::GetInstance()->SetDeviceId(strDeviceId);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetDownloadContentLength
 * Signature: (J)IV
 */
JNIEXPORT jint JNICALL Java_com_qpidnetwork_request_RequestJni_GetDownloadContentLength
  (JNIEnv *env, jclass cls, jlong task)
{
	jint jiContentLength = 0;
	if(gRequestMap.find(task) != gRequestMap.end()){
		const RequestBaseTask* baseTask = task;
		int iContentLength = 0;
		int iRecvLength = 0;
		baseTask->GetRecvDataCount(iContentLength, iRecvLength);
		jiContentLength = iContentLength;
	}
//	const HttpRequest* request = gHttpRequestManager.GetRequestById(requestId);
//	if (NULL != request) {
//		int iContentLength = 0;
//		int iRecvLength = 0;
//		request->GetRecvDataCount(iContentLength, iRecvLength);
//		jiContentLength = iContentLength;
//	}
	return jiContentLength;
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetRecvLength
 * Signature: (J)IV
 */
JNIEXPORT jint JNICALL Java_com_qpidnetwork_request_RequestJni_GetRecvLength
  (JNIEnv *env, jclass cls, jlong task)
{
	jint jiRecvLength = 0;
	if(gRequestMap.find(task) != gRequestMap.end()){
		const RequestBaseTask* baseTask = task;
		int iContentLength = 0;
		int iRecvLength = 0;
		baseTask->GetRecvDataCount(iContentLength, iRecvLength);
		jiRecvLength = iRecvLength;
	}
//	const HttpRequest* request = gHttpRequestManager.GetRequestById(requestId);
//	if (NULL != request) {
//		int iContentLength = 0;
//		int iRecvLength = 0;
//		request->GetRecvDataCount(iContentLength, iRecvLength);
//		jiRecvLength = iRecvLength;
//	}
	return jiRecvLength;
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetUploadContentLength
 * Signature: (J)IV
 */
JNIEXPORT jint JNICALL Java_com_qpidnetwork_request_RequestJni_GetUploadContentLength
  (JNIEnv *env, jclass cls, jlong task)
{
	jint jiContentLength = 0;
	if(gRequestMap.find(task) != gRequestMap.end()){
		const RequestBaseTask* baseTask = task;
		int iContentLength = 0;
		int iSendLength = 0;
		baseTask->GetSendDataCount(iContentLength, iSendLength);
		jiContentLength = iContentLength;
	}
//	const HttpRequest* request = gHttpRequestManager.GetRequestById(requestId);
//	if (NULL != request) {
//		int iContentLength = 0;
//		int iSendLength = 0;
//		request->GetSendDataCount(iContentLength, iSendLength);
//		jiContentLength = iContentLength;
//	}
	return jiContentLength;
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetSendLength
 * Signature: (J)IV
 */
JNIEXPORT jint JNICALL Java_com_qpidnetwork_request_RequestJni_GetSendLength
  (JNIEnv *env, jclass cls, jlong task)
{
	jint jiSendLength = 0;
	if(gRequestMap.find(task) != gRequestMap.end()){
		const RequestBaseTask* baseTask = task;
		int iContentLength = 0;
		int iSendLength = 0;
		baseTask->GetSendDataCount(iContentLength, iSendLength);
		jiSendLength = iSendLength;
	}
//	const HttpRequest* request = gHttpRequestManager.GetRequestById(requestId);
//	if (NULL != request) {
//		int iContentLength = 0;
//		int iSendLength = 0;
//		request->GetSendDataCount(iContentLength, iSendLength);
//		jiSendLength = iSendLength;
//	}
	return jiSendLength;
}
