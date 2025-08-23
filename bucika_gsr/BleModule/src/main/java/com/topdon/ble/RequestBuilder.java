package com.topdon.ble;

import com.topdon.ble.callback.RequestCallback;

import java.util.UUID;

import com.topdon.commons.observer.Observe;
import com.topdon.commons.poster.RunOn;

public class RequestBuilder<T extends RequestCallback> {
    String tag;
    RequestType type;
    UUID service;
    UUID characteristic;
    UUID descriptor;
    Object value;
    int priority;
    RequestCallback callback;
    WriteOptions writeOptions;

    RequestBuilder(RequestType type) {
        this.type = type;
    }

    public RequestBuilder<T> setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public RequestBuilder<T> setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public RequestBuilder<T> setCallback(T callback) {
        this.callback = callback;
        return this;
    }

    public Request build() {
        return new GenericRequest(this);
    }
}
