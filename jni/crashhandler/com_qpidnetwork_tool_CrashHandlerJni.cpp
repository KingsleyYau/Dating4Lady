#include "com_qpidnetwork_tool_CrashHandlerJni.h"

#include <crashhandler/CrashHandler.h>

#include <common/command.h>
/*
 * Class:     com_qpidnetwork_tool_CrashHandlerJni
 * Method:    SetCrashLogDirectory
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_tool_CrashHandlerJni_SetCrashLogDirectory
 (JNIEnv *env, jclass cls, jstring directory) {
	const char *cpDirectory = env->GetStringUTFChars(directory, 0);
	CrashHandler::GetInstance()->SetCrashLogDirectory(cpDirectory);
	env->ReleaseStringUTFChars(directory, cpDirectory);
}

