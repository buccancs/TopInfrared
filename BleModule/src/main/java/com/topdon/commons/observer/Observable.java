package com.topdon.commons.observer;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.topdon.commons.poster.MethodInfo;
import com.topdon.commons.poster.PosterDispatcher;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Observable {
    private final List<ObserverInfo> observerInfos = new ArrayList<>();
    private final PosterDispatcher posterDispatcher;
    private final ObserverMethodHelper helper;

    public Observable(@NonNull PosterDispatcher posterDispatcher, boolean isObserveAnnotationRequired) {
        this.posterDispatcher = posterDispatcher;
        helper = new ObserverMethodHelper(isObserveAnnotationRequired);
    }

    public PosterDispatcher getPosterDispatcher() {
        return posterDispatcher;
    }

    public void registerObserver(@NonNull Observer observer) {
        Objects.requireNonNull(observer, "observer can't be null");
        synchronized (observerInfos) {
            boolean registered = false;
            for (Iterator<ObserverInfo> it = observerInfos.iterator(); it.hasNext(); ) {
                ObserverInfo info = it.next();
                Observer o = info.weakObserver.get();
                if (o == null) {
                    it.remove();
                } else if (o == observer) {
                    registered = true;
                }
            }
            if (registered) {
                Log.e("Observable", "", new Error("Observer " + observer + " is already registered."));
                return;
            }
            Map<String, Method> methodMap = helper.findObserverMethod(observer);
            observerInfos.add(new ObserverInfo(observer, methodMap));
        }
    }

    public boolean isRegistered(@NonNull Observer observer) {
        synchronized (observerInfos) {
            for (ObserverInfo info : observerInfos) {
                if (info.weakObserver.get() == observer) {
                    return true;
                }
            }
            return false;
        }
    }

    public void unregisterObserver(@NonNull Observer observer) {
        synchronized (observerInfos) {
            for (Iterator<ObserverInfo> it = observerInfos.iterator(); it.hasNext(); ) {
                ObserverInfo info = it.next();
                Observer o = info.weakObserver.get();
                if (o == null || observer == o) {
                    it.remove();
                }
            }
        }
    }

    public void unregisterAll() {
        synchronized (observerInfos) {
            observerInfos.clear();
        }
        helper.clearCache();
    }

    private List<ObserverInfo> getObserverInfos() {
        synchronized (observerInfos) {
            ArrayList<ObserverInfo> infos = new ArrayList<>();
            for (ObserverInfo info : observerInfos) {
                Observer observer = info.weakObserver.get();
                if (observer != null) {
                    infos.add(info);
                }
            }
            return infos;
        }
    }

    public void notifyObservers(@NonNull String methodName, @Nullable MethodInfo.Parameter... parameters) {
        notifyObservers(new MethodInfo(methodName, parameters));
    }

    public void notifyObservers(@NonNull MethodInfo info) {
        List<ObserverInfo> infos = getObserverInfos();
        for (ObserverInfo oi : infos) {
            Observer observer = oi.weakObserver.get();
            if (observer != null) {
                String key = helper.generateKey(info.getTag(), info.getName(), info.getParameterTypes());
                Method method = oi.methodMap.get(key);
                if (method != null) {
                    Runnable runnable = helper.generateRunnable(observer, method, info);
                    posterDispatcher.post(method, runnable);
                }
            }
        }
    }
}
