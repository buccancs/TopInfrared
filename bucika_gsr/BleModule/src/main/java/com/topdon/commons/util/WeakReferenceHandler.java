package com.topdon.commons.util;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

/**
 * @Desc Handle软引用工具类
 * @ClassName WeakReferenceHandler
 * @Email 616862466@qq.com
 * @Author 子墨
 * @Date 2022/10/12 9:25
 */
public class WeakReferenceHandler<T> extends Handler {

    private final WeakReference<T> mReference;

    public WeakReferenceHandler(T referencedObject) {
        mReference = new WeakReference<T>(referencedObject);
    }

    public WeakReferenceHandler(Looper looper, T referencedObject) {
        super(looper);
        mReference = new WeakReference<T>(referencedObject);
    }

    protected T getReferencedObject() {
        return mReference.get();
    }

}