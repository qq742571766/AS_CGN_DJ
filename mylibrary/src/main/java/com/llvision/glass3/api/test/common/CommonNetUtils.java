package com.llvision.glass3.api.test.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * todo
 *
 * @author liuhui
 * @date 2018/12/24
 */
public class CommonNetUtils {



    static {
        System.loadLibrary("ParseCommonNet_jni");
    }

    private static final Map<String, CommonNetUtils> MAP = new ConcurrentHashMap<>();

    public static synchronized CommonNetUtils getInstance(int serviceId) {

        CommonNetUtils fp16Convert = MAP.get(serviceId + "");
        if (fp16Convert == null) {
            fp16Convert = new CommonNetUtils(serviceId);
            fp16Convert.mServiceId = serviceId;
            MAP.put(String.valueOf(serviceId), fp16Convert);
        }
        return fp16Convert;
    }

    private long mNativePtr;
    private int mServiceId;

    ReentrantLock mLock = new ReentrantLock(true);


    /**
     * @param serviceId
     */
    public CommonNetUtils(int serviceId) {
        mLock.lock();
        mNativePtr = nativeCreate(serviceId);
        mLock.unlock();
    }

    /**
     * release resource
     */
    public void destroy() {
        long ptr = 0;
        mLock.lock();
        ptr = mNativePtr;
        mNativePtr = 0;
        mLock.unlock();
        nativeDestroy(ptr);
        MAP.remove(String.valueOf(mServiceId));
    }


    /**
     * @param datas
     * @return
     */
    public ArrayList<int[]> parserCommonNet(byte[] datas,int width,int height) {
        ArrayList<int[]> result = new ArrayList<>();
        if (/*!BuildConfig.DEBUG &&*/ mNativePtr == 0) {
            //release
            return result;
        }
        nativeParserCommonNet(mNativePtr, datas, datas.length,width,height, result);
        return result;
    }




    public ArrayList<int[]> fp16ToFp32Classify(byte[] datas) {
        ArrayList<int[]> result = new ArrayList<>();
        if (mNativePtr == 0) {
            return result;
        }
        nativeFp16ToFp32Classify(mNativePtr, datas, datas.length, result);
        Collections.sort(result, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o2[1] - o1[1];
            }
        });
        return result;
    }




    private static final native long nativeCreate(int servcieId);

    private static final native void nativeDestroy(long nativePtr);

    private static final native void nativeParserCommonNet(long nativePtr, byte[] datas, int length,int width,int height, ArrayList<int[]> result);

    private static final native void nativeFp16ToFp32Classify(long nativePtr, byte[] datas, int length, ArrayList<int[]> result);



}
