package com.topdon.commons.util;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

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