/*
 * com_qpidnetwork_tool_ImageHandler.cpp
 *
 *  Created on: 2015-05-12
 *      Author: Samson Fan
 */

#include "com_qpidnetwork_tool_ImageHandler.h"
#include <jni.h>
#include <string>
#include <common/KLog.h>
#include <imghandle/PngHandler.h>

using namespace std;

/* JNI_OnLoad */
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	// Get JNI
	JNIEnv* env;
	if (JNI_OK != vm->GetEnv(reinterpret_cast<void**> (&env),
                           JNI_VERSION_1_4)) {
		return -1;
	}

	FileLog("ImageHandler.jni", "JNI_OnLoad()");

	return JNI_VERSION_1_4;
}

/*
 * Class:     com_qpidnetwork_tool_ImageHandler
 * Method:    SetLogPath
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_tool_ImageHandler_SetLogPath
  (JNIEnv *env, jclass cls, jstring path)
{
	string strPath("");
	const char* cpPath = env->GetStringUTFChars(path, 0);
	strPath = cpPath;
	env->ReleaseStringUTFChars(path, cpPath);

	KLog::SetLogDirectory(strPath);
}

/*
 * Class:     com_qpidnetwork_tool_ImageHandler
 * Method:    ConvertEmotionPng
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_tool_ImageHandler_ConvertEmotionPng
  (JNIEnv *env, jclass cls, jstring path)
{
	FileLog("ImageHandler", "ConvertEmotionPng() begin");

	jboolean result = false;

	string strPath("");
	const char *cpPath = env->GetStringUTFChars(path, 0);
	strPath = cpPath;
	env->ReleaseStringUTFChars(path, cpPath);

	result = PngHandler::ConvertImage(strPath);

	FileLog("ImageHandler", "ConvertEmotionPng() result:%d", result);

	return result;
}
