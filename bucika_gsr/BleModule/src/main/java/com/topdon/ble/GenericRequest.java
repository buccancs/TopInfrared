package com.topdon.ble;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.topdon.ble.callback.RequestCallback;

import java.util.Queue;
import java.util.UUID;

class GenericRequest implements Request, Comparable<GenericRequest> {
    Device device;
    private final String tag;
    RequestType type;
    UUID service;
    UUID characteristic;
    UUID descriptor;
    Object value;
    int priority;
    RequestCallback callback;
    WriteOptions writeOptions;
    byte[] descriptorTemp;
    Queue<byte[]> remainQueue;
    byte[] sendingBytes;

    GenericRequest(RequestBuilder builder) {
        tag = builder.tag;
        type = builder.type;
        service = builder.service;
        characteristic = builder.characteristic;
        descriptor = builder.descriptor;
        priority = builder.priority;
        value = builder.value;
        callback = builder.callback;
        writeOptions = builder.writeOptions;
    }

    @Override
    public int compareTo(GenericRequest other) {
        return Integer.compare(other.priority, priority);
    }

    @NonNull
    public Device getDevice() {
        return device;
    }

    @NonNull
    public RequestType getType() {
        return type;
    }

    @Nullable
    public String getTag() {
        return tag;
    }

    @Nullable
    public UUID getService() {
        return service;
    }

    @Nullable
    public UUID getCharacteristic() {
        return characteristic;
    }

    @Nullable
    public UUID getDescriptor() {
        return descriptor;
    }

    @Override
    public void execute(Connection connection) {
        if (connection != null) {
            connection.execute(this);
        }
    }
}
