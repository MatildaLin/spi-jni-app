#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <android/log.h>

#include "bbbandroidHAL.h"

#define BBBANDROID_NATIVE_TAG "NDK_BBBAndroidApplication"

jstring Java_com_reald_spiapp_MainActivity_stringFromJNI(JNIEnv *env, jobject this)
{
    #if defined(__arm__)
        #if defined(__ARM_ARCH_7A__)
          #if defined(__ARM_NEON__)
            #if defined(__ARM_PCS_VFP)
              #define ABI "armeabi-v7a/NEON (hard-float)"
            #else
              #define ABI "armeabi-v7a/NEON"
            #endif
          #else
            #if defined(__ARM_PCS_VFP)
              #define ABI "armeabi-v7a (hard-float)"
            #else
              #define ABI "armeabi-v7a"
            #endif
          #endif
        #else
         #define ABI "armeabi"
        #endif
    #elif defined(__i386__)
        #define ABI "x86"
    #elif defined(__x86_64__)
        #define ABI "x86_64"
    #elif defined(__mips64)  /* mips64el-* toolchain defines __mips__ too */
        #define ABI "mips64"
    #elif defined(__mips__)
        #define ABI "mips"
    #elif defined(__aarch64__)
    #define ABI "arm64-v8a"
    #else
        #define ABI "unknown"
    #endif

    return (*env)->NewStringUTF(env, "Send Command. Compiled with ABI " ABI ".");
}

jint Java_com_reald_spiapp_MainActivity_spiOpen(JNIEnv *env, jobject this, jint bus, jint device, jint speed, jint mode, jint bpw)
{
    jint ret;
    ret = spiOpen(bus, device, speed, mode, bpw);

    if (ret == -1) {
        __android_log_print(ANDROID_LOG_ERROR, BBBANDROID_NATIVE_TAG, "spiOpen(%d, %d, %d, %d, %d) failed!", (unsigned int) bus, (unsigned int) device,
                            (unsigned int) speed, (unsigned int) mode, (unsigned int) bpw);
        return -1;
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, BBBANDROID_NATIVE_TAG, "spiOpen(%d, %d, %d, %d, %d) succeeded", (unsigned int) bus, (unsigned int) device,
                            (unsigned int) speed, (unsigned int) mode, (unsigned int) bpw);
    }

    return ret;
}

jbyte Java_com_reald_spiapp_MainActivity_spiReadByte(JNIEnv *env, jobject this, jint spiFD, jint regAdd)
{
    jint ret;
    ret = spiReadByte(spiFD, regAdd);

    if (ret == -1) {
        __android_log_print(ANDROID_LOG_ERROR, BBBANDROID_NATIVE_TAG, "spiReadByte(%d, %d) failed!", (unsigned int) spiFD, (unsigned int) regAdd);
        return -1;
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, BBBANDROID_NATIVE_TAG, "spiReadByte(%d, %d) succeeded", (unsigned int) spiFD, (unsigned int) regAdd);
    }

    return ret;
}

jint Java_com_reald_spiapp_MainActivity_spiWriteRegByte(JNIEnv *env, jobject this, jint spiFD, jint regAdd, jbyte data)
{
    jint ret;

    unsigned char value = data;

    ret = spiWriteRegByte(spiFD, regAdd, value);

    if (ret == -1) {
        __android_log_print(ANDROID_LOG_ERROR, BBBANDROID_NATIVE_TAG, "spiWriteRegByte(%d, %d, %d) failed!", (unsigned int) spiFD, (unsigned int) regAdd,
                            (unsigned int) data);
        return -1;
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, BBBANDROID_NATIVE_TAG, "spiWriteRegByte(%d, %d, %d) succeeded", (unsigned int) spiFD, (unsigned int) regAdd,
                            (unsigned int) data);
    }

    return ret;
}

jint Java_com_reald_spiapp_MainActivity_spiWriteByte(JNIEnv *env, jobject this, jint spiFD, jbyte data)
{
    jint ret;

    unsigned char value = data;
    unsigned char null = 0x00;

    ret = spiTransfer(spiFD, &value, &null, 1);

    if (ret == -1) {
        __android_log_print(ANDROID_LOG_ERROR, BBBANDROID_NATIVE_TAG, "spiWriteByte(%d, %d) failed!", (unsigned int) spiFD, (unsigned int) data);
        return -1;
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, BBBANDROID_NATIVE_TAG, "spiWriteByte(%d, %d) succeeded", (unsigned int) spiFD, (unsigned int) data);
    }

    return ret;
}

jint Java_com_reald_spiapp_MainActivity_spiPause(JNIEnv *env, jobject this, jint spiFD, jint data)
{
    jint ret;

    unsigned char value = data;
    unsigned char null = 0x00;

    ret = spiTransfer(spiFD, &value, &null, 1);

    if (ret == -1) {
        __android_log_print(ANDROID_LOG_ERROR, BBBANDROID_NATIVE_TAG, "spiPause(%d, %d) failed!", (unsigned int) spiFD, (unsigned int) data);
        return -1;
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, BBBANDROID_NATIVE_TAG, "spiPause(%d, %d) succeeded", (unsigned int) spiFD, (unsigned int) data);
    }

    return ret;
}

void Java_com_reald_spiapp_MainActivity_spiClose(JNIEnv *env, jobject this, jint spiFD)
{
    spiClose(spiFD);

    __android_log_print(ANDROID_LOG_DEBUG, BBBANDROID_NATIVE_TAG, "spiClose(%d, bytearray) succeeded", (unsigned int) spiFD);

}
