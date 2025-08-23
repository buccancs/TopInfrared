package com.topdon.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConnectionConfiguration {
    
    public static final int TRY_RECONNECT_TIMES_INFINITE = -1;
    int discoverServicesDelayMillis = 600;
    int connectTimeoutMillis = 10000;
    int requestTimeoutMillis = 3000;
    int tryReconnectMaxTimes = TRY_RECONNECT_TIMES_INFINITE;
    int reconnectImmediatelyMaxTimes = 3;
    boolean isAutoReconnect = true;
    @RequiresApi(Build.VERSION_CODES.M)
    int transport = BluetoothDevice.TRANSPORT_LE;
    @RequiresApi(Build.VERSION_CODES.O)
    int phy = BluetoothDevice.PHY_LE_1M_MASK;
    @NonNull
    final List<Pair<Integer, Integer>> scanIntervalPairsInAutoReconnection;
    private final Map<String, WriteOptions> defaultWriteOptionsMap = new HashMap<>();

    public ConnectionConfiguration() {
        scanIntervalPairsInAutoReconnection = new ArrayList<>();
        scanIntervalPairsInAutoReconnection.add(Pair.create(0, 2000));
        scanIntervalPairsInAutoReconnection.add(Pair.create(1, 5000));
        scanIntervalPairsInAutoReconnection.add(Pair.create(3, 10000));
        scanIntervalPairsInAutoReconnection.add(Pair.create(5, 30000));
        scanIntervalPairsInAutoReconnection.add(Pair.create(10, 60000));
    }

    public ConnectionConfiguration setDiscoverServicesDelayMillis(int discoverServicesDelayMillis) {
        this.discoverServicesDelayMillis = discoverServicesDelayMillis;
        return this;
    }

    public ConnectionConfiguration setConnectTimeoutMillis(int connectTimeoutMillis) {
        if (requestTimeoutMillis >= 1000) {
            this.connectTimeoutMillis = connectTimeoutMillis;
        }
        return this;
    }

    public ConnectionConfiguration setRequestTimeoutMillis(int requestTimeoutMillis) {
        if (requestTimeoutMillis >= 1000) {
            this.requestTimeoutMillis = requestTimeoutMillis;
        }
        return this;
    }

    public ConnectionConfiguration setTryReconnectMaxTimes(int tryReconnectMaxTimes) {
        this.tryReconnectMaxTimes = tryReconnectMaxTimes;
        return this;
    }

    public ConnectionConfiguration setReconnectImmediatelyMaxTimes(int reconnectImmediatelyMaxTimes) {
        this.reconnectImmediatelyMaxTimes = reconnectImmediatelyMaxTimes;
        return this;
    }

    public ConnectionConfiguration setAutoReconnect(boolean autoReconnect) {
        isAutoReconnect = autoReconnect;
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    public ConnectionConfiguration setTransport(int transport) {
        this.transport = transport;
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public ConnectionConfiguration setPhy(int phy) {
        this.phy = phy;
        return this;
    }

    public ConnectionConfiguration setScanIntervalPairsInAutoReconnection(List<Pair<Integer, Integer>> parameters) {
        Inspector.requireNonNull(parameters, "parameters can't be null");
        scanIntervalPairsInAutoReconnection.clear();
        scanIntervalPairsInAutoReconnection.addAll(parameters);
        return this;
    }

    public ConnectionConfiguration setDefaultWriteOptions(UUID service, UUID characteristic, WriteOptions options) {
        Inspector.requireNonNull(service, "service can't be null");
        Inspector.requireNonNull(characteristic, "characteristic can't be null");
        Inspector.requireNonNull(options, "options can't be null");
        defaultWriteOptionsMap.put(service + ":" + characteristic, options);
        return this;
    }
    
    @Nullable
    WriteOptions getDefaultWriteOptions(UUID service, UUID characteristic) {
        return defaultWriteOptionsMap.get(service + ":" + characteristic);
    }
}
