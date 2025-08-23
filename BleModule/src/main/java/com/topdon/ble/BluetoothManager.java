package com.topdon.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.topdon.ble.callback.MtuChangeCallback;
import com.topdon.ble.callback.NotificationChangeCallback;
import com.topdon.ble.callback.ReadCharacteristicCallback;
import com.topdon.commons.UUIDManager;
import com.topdon.commons.observer.Observable;
import com.topdon.commons.observer.Observe;
import com.topdon.commons.poster.RunOn;
import com.topdon.commons.poster.Tag;
import com.topdon.commons.poster.ThreadMode;
import com.topdon.commons.util.LLog;
import com.topdon.commons.util.StringUtils;
import com.topdon.lms.sdk.xutils.common.util.MD5;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressLint("MissingPermission")
public class BluetoothManager implements EventObserver {
    public static boolean iSReset = false;
    public static boolean isSending = false;
    public static boolean isClickStopCharging = false;
    private static BluetoothManager instance = null;
    private Device mDevice;
    private Connection connection;
    public static boolean isReceiveBleData = false;
    private BluetoothGattCharacteristic writeCharact = null;

    public static BluetoothManager getInstance() {
        if (instance == null)
            instance = new BluetoothManager();
        return instance;
    }

    public BluetoothManager() {
    }

    public Device getDevice() {
        return mDevice;
    }

    private void setMTUValue() {
        if (mDevice.isConnected()) {
            Log.e("bcf_ble", "连接设备名称：" + mDevice.getName() + "");
            RequestBuilder<MtuChangeCallback> builder = null;
            if (mDevice.getName().contains("T-darts") || mDevice.getName().contains("TD")) {
                builder = new RequestBuilderFactory().getChangeMtuBuilder(240);
            } else {
                builder = new RequestBuilderFactory().getChangeMtuBuilder(503);
            }
            Request request = builder.setCallback(new MtuChangeCallback() {
                @Override
                public void onMtuChanged(@NonNull Request request, int mtu) {
                    Log.d("wangchen", "MTU修改成功，新值：" + mtu);
                    setReadCallback();
                }

                @Override
                public void onRequestFailed(@NonNull Request request, int failType, @Nullable Object value) {
                    Log.d("bcf", "MTU修改失败");
                }

            }).build();
            connection.execute(request);
        }
    }

    private void setReadCallback() {
        if (mDevice.isConnected()) {
            isSending = false;
            boolean isEnabled = connection.isNotificationOrIndicationEnabled(UUID.fromString(UUIDManager.SERVICE_UUID), UUID.fromString(UUIDManager.NOTIFY_UUID));
            LLog.w("bcf_ble", "是否打开了Notifycation: " + isEnabled);
            RequestBuilder<NotificationChangeCallback> builder = new RequestBuilderFactory().getSetNotificationBuilder(UUID.fromString(UUIDManager.SERVICE_UUID), UUID.fromString(UUIDManager.NOTIFY_UUID), true);
            RequestBuilder<ReadCharacteristicCallback> builder1 = new RequestBuilderFactory().getReadCharacteristicBuilder(UUID.fromString(UUIDManager.SERVICE_UUID), UUID.fromString(UUIDManager.READ_UUID));
            builder.build().execute(connection);
            builder1.build().execute(connection);
        }
    }

    public void setCancelListening() {
        Observable observable = EasyBLE.getInstance().getObservable();
        if (observable != null) {
            EasyBLE.getInstance().unregisterObserver(this);
        }
    }

    public Connection connect(Device device) {
        mDevice = device;
        ConnectionConfiguration config = new ConnectionConfiguration();
        config.setConnectTimeoutMillis(10000);
        config.setRequestTimeoutMillis(7000);
        config.setAutoReconnect(false);
        config.setReconnectImmediatelyMaxTimes(3);
        connection = EasyBLE.getInstance().connect(device, config, this);
        connection.setBluetoothGattCallback(new BluetoothGattCallback() {
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.d("ble_bcf_data", "原始写入数据状态：status: " + status + "  内容：" + StringUtils.toHex(characteristic.getValue()));
                setBleData("原始写入数据状态：status: " + status + "  内容：" + StringUtils.toHex(characteristic.getValue()));
            }
        });
        return connection;
    }

    public Connection connect(String mac,String name){
        ConnectionConfiguration configuration = new ConnectionConfiguration();
        configuration.setConnectTimeoutMillis(10000);
        configuration.setRequestTimeoutMillis(7000);
        configuration.setAutoReconnect(false);
        configuration.setReconnectImmediatelyMaxTimes(3);
        connection = EasyBLE.getInstance().connect(mac, configuration, this);
        mDevice = connection.getDevice();
        mDevice.setName(name);
        return connection;
    }

    public void release() {
        Log.d("bcf", "释放所有BLE连接");
        EasyBLE.getInstance().disconnectConnection(mDevice);
        EasyBLE.getInstance().release();
        EasyBLE.getInstance().releaseConnection(mDevice);
    }

    public boolean isConnected() {
        if (mDevice == null)
            return false;
        return mDevice.isConnected();
    }

    @Tag("onConnectionStateChanged")
    @Observe
    @RunOn(ThreadMode.MAIN)
    @Override
    public void onConnectionStateChanged(@NonNull Device device) {
        if (device.getConnectionState() != ConnectionState.SERVICE_DISCOVERED || device.getConnectionState() != ConnectionState.DISCONNECTED) {
            EventBus.getDefault().post(device.getConnectionState());
            Log.e("wangchen", "发送广播--" + device.getConnectionState());
        }
        Log.d("ywq", "MyObserver 连接状态：" + device.getConnectionState() + " 是否已连接： " + device.isConnected() + "-----名称：" + device.getName() + "-------mac: " + device.getAddress());
        switch (device.getConnectionState()) {
            case SCANNING_FOR_RECONNECTION:
                break;
            case CONNECTING:
                break;
            case CONNECTED:
                break;
            case DISCONNECTED:
                EventBus.getDefault().post(ConnectionState.DISCONNECTED.name());
                break;
            case RELEASED:
                EventBus.getDefault().post(ConnectionState.RELEASED.name());
                break;
            case SERVICE_DISCOVERED:
                setMTUValue();
                if (device.isConnected()) {
                    EventBus.getDefault().post(ConnectionState.SERVICE_DISCOVERED.name());
                }
                break;
        }
    }

    @Override
    public void onConnectFailed(Device device, int failType) {
        Log.e("bcf_ble", "连接失败" + device.getName());
        EventBus.getDefault().post(device.getConnectionState());
    }

    @Override
    public void onConnectTimeout(Device device, int type) {
        Log.e("bcf_ble", "连接超时");
    }

    @Observe
    @Override
    public void onNotificationChanged(@NonNull Request request, boolean isEnabled) {
        String typeTag = "";
        if (request.getType() == RequestType.SET_NOTIFICATION) {
            typeTag = "通知";
            EventBus.getDefault().post(ConnectionState.MTU_SUCCESS);
        } else {
            typeTag = "Indication";
        }
        Log.d("bcf_ble", "onNotificationChanged ：" + typeTag + "：" + (isEnabled ? "开启" : "关闭"));
    }

    public boolean writeBuletoothData(byte[] data) {
        if (mDevice == null || !mDevice.isConnected()) {
            return false;
        }
        writeCharact = connection.getCharacteristic(UUID.fromString(UUIDManager.SERVICE_UUID), UUID.fromString(UUIDManager.WRITE_UUID));
        connection.getGatt().setCharacteristicNotification(writeCharact, true);
        writeCharact.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        writeCharact.setValue(data);
        return connection.getGatt().writeCharacteristic(writeCharact);
    }

    @Observe
    @Override
    public void onCharacteristicRead(Request request, byte[] value) {
        String data = StringUtils.toHex(value);
    }

    @Observe
    @Override
    public void onCharacteristicChanged(Device device, UUID service, UUID characteristic, byte[] value) {
        Log.e("ble_bcf_data", "接收蓝牙数据：" + StringUtils.toHex(value));
        EventBus.getDefault().post(value);
    }

    public static void setBleData(String message) {
    }

}
