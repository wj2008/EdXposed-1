#ifndef HOOK_MAIN_H
#define HOOK_MAIN_H

#include <jni.h>

void Java_lab_galaxy_yahfa_HookMain_init(JNIEnv *env, jclass clazz, jint sdkVersion);

jobject Java_lab_galaxy_yahfa_HookMain_findMethodNative(JNIEnv *env, jclass clazz,
                                                        jclass targetClass, jstring methodName, jstring methodSig);

jboolean Java_lab_galaxy_yahfa_HookMain_backupAndHookNative(JNIEnv *env, jclass clazz,
                                                            jobject target, jobject hook,
                                                            jobject backup);

void Java_lab_galaxy_yahfa_HookMain_ensureMethodCached(JNIEnv *env, jclass clazz,
                                                       jobject hook,
                                                       jobject backup);

static void* getResolvedMethodsAddr(JNIEnv*, jobject);

#endif // HOOK_MAIN_H