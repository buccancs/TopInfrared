package com.topdon.ble;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

public interface Request {
    
    @NonNull
    Device getDevice();

    @NonNull
    RequestType getType();

    @Nullable
    String getTag();

    @Nullable
    UUID getService();

    @Nullable
    UUID getCharacteristic();

    @Nullable
    UUID getDescriptor();

    void execute(Connection connection);
}
