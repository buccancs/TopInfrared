package com.topdon.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Build;

import androidx.annotation.IntRange;

import androidx.annotation.RequiresApi;

import com.topdon.ble.callback.MtuChangeCallback;
import com.topdon.ble.callback.NotificationChangeCallback;
import com.topdon.ble.callback.PhyChangeCallback;
import com.topdon.ble.callback.ReadCharacteristicCallback;
import com.topdon.ble.callback.ReadRssiCallback;

import java.util.UUID;

public class RequestBuilderFactory {
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public RequestBuilder<MtuChangeCallback> getChangeMtuBuilder(@IntRange(from = 23, to = 517) int mtu) {
        if (mtu < 23) {
            mtu = 23;
        } else if (mtu > 517) {
            mtu = 517;
        }
        RequestBuilder<MtuChangeCallback> builder = new RequestBuilder<>(RequestType.CHANGE_MTU);
        builder.value = mtu;
        return builder;
    }

    public RequestBuilder<ReadCharacteristicCallback> getReadCharacteristicBuilder(UUID service, UUID characteristic) {
        RequestBuilder<ReadCharacteristicCallback> builder = new RequestBuilder<>(RequestType.READ_CHARACTERISTIC);
        builder.service = service;
        builder.characteristic = characteristic;
        return builder;
    }

    public RequestBuilder<NotificationChangeCallback> getSetNotificationBuilder(UUID service, UUID characteristic,
                                                                                boolean enable) {
        RequestBuilder<NotificationChangeCallback> builder = new RequestBuilder<>(RequestType.SET_NOTIFICATION);
        builder.service = service;
        builder.characteristic = characteristic;
        builder.value = enable ? 1 : 0;
        return builder;
    }

    public RequestBuilder<NotificationChangeCallback> getSetIndicationBuilder(UUID service, UUID characteristic,
                                                                               boolean enable) {
        RequestBuilder<NotificationChangeCallback> builder = new RequestBuilder<>(RequestType.SET_INDICATION);
        builder.service = service;
        builder.characteristic = characteristic;
        builder.value = enable ? 1 : 0;
        return builder;
    }

    public RequestBuilder<NotificationChangeCallback> getReadDescriptorBuilder(UUID service, UUID characteristic,
                                                                                UUID descriptor) {
        RequestBuilder<NotificationChangeCallback> builder = new RequestBuilder<>(RequestType.READ_DESCRIPTOR);
        builder.service = service;
        builder.characteristic = characteristic;
        builder.descriptor = descriptor;
        return builder;
    }

    public WriteCharacteristicBuilder getWriteCharacteristicBuilder(UUID service, UUID characteristic,
                                                                            byte[] value) {
        Inspector.requireNonNull(value, "value can't be null");
        WriteCharacteristicBuilder builder = new WriteCharacteristicBuilder();
        builder.service = service;
        builder.characteristic = characteristic;
        builder.value = value;
        return builder;
    }

    public RequestBuilder<ReadRssiCallback> getReadRssiBuilder() {
        return new RequestBuilder<>(RequestType.READ_RSSI);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public RequestBuilder<PhyChangeCallback> getReadPhyBuilder() {
        return new RequestBuilder<>(RequestType.READ_PHY);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public RequestBuilder<PhyChangeCallback> getSetPreferredPhyBuilder(int txPhy, int rxPhy, int phyOptions) {
        RequestBuilder<PhyChangeCallback> builder = new RequestBuilder<>(RequestType.SET_PREFERRED_PHY);
        builder.value = new int[]{txPhy, rxPhy, phyOptions};
        return builder;
    }
}
