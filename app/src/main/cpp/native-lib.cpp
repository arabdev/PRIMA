#include <jni.h>
#include <string>

extern "C"
jstring
Java_kr_co_ipdisk_home35_prima_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
