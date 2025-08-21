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

/**
 * BluetoothManager
 * 蓝牙管理工具
 *
 * @author chuanfeng.bi
 * @date 2021/11/19 11:10
 */
@SuppressLint("MissingPermission")
public class BluetoothManager implements EventObserver {
    public static boolean iSReset = false;//是否复位
    public static boolean isSending = false;//是否正在发送蓝牙数据
    public static boolean isClickStopCharging = false;//是否点击了停止充电
    private static BluetoothManager instance = null;
    private Device mDevice;
    private Connection connection;
    public static boolean isReceiveBleData = false;//是否接收蓝牙数据
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
            //设置MTU
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
            //开关通知
            boolean isEnabled = connection.isNotificationOrIndicationEnabled(UUID.fromString(UUIDManager.SERVICE_UUID), UUID.fromString(UUIDManager.NOTIFY_UUID));
            LLog.w("bcf_ble", "是否打开了Notifycation: " + isEnabled);
            RequestBuilder<NotificationChangeCallback> builder = new RequestBuilderFactory().getSetNotificationBuilder(UUID.fromString(UUIDManager.SERVICE_UUID), UUID.fromString(UUIDManager.NOTIFY_UUID), true);
            RequestBuilder<ReadCharacteristicCallback> builder1 = new RequestBuilderFactory().getReadCharacteristicBuilder(UUID.fromString(UUIDManager.SERVICE_UUID), UUID.fromString(UUIDManager.READ_UUID));
            //不设置回调，使用观察者模式接收结果
            builder.build().execute(connection);
            builder1.build().execute(connection);
        }
    }

    //取消监听
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
        connection = EasyBLE.getInstance().connect(device, config, this);//回调监听连接状态，设置此回调不影响观察者接收连接状态消息
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

    /**
     * 使用{@link Observe}确定要接收消息，{@link RunOn}指定在主线程执行方法，设置{@link Tag}防混淆后找不到方法
     */
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
//                setReadCallback();
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

    /**
     * 使用{@link Observe}确定要接收消息，方法在{@link EasyBLEBuilder#setMethodDefaultThreadMode(ThreadMode)}指定的线程执行
     */
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

    /**
     * 向蓝牙写入数据
     *
     * @param data
     */
    public boolean writeBuletoothData(byte[] data) {
        if (mDevice == null || !mDevice.isConnected()) {
            return false;
        }
        writeCharact = connection.getCharacteristic(UUID.fromString(UUIDManager.SERVICE_UUID), UUID.fromString(UUIDManager.WRITE_UUID));
        connection.getGatt().setCharacteristicNotification(writeCharact, true); // 设置监听
        // 当数据传递到蓝牙之后 会回调BluetoothGattCallback里面的write方法
        writeCharact.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        writeCharact.setValue(data);
//        LLog.d("ble_bcf_data", "发送到蓝牙的数据为：" + StringUtils.toHex(data));
        return connection.getGatt().writeCharacteristic(writeCharact);
    }

    @Observe
    @Override
    public void onCharacteristicRead(Request request, byte[] value) {
        //如果推送的是十六进制的数据的写法
        String data = StringUtils.toHex(value); // 将字节转化为String字符串
//        Log.d("ble_bcf_data", "onCharacteristicRead: " + data);
    }

    /**
     * 接收蓝牙设备返回的数据
     *
     * @param device         设备
     * @param service        服务UUID
     * @param characteristic 特征UUID
     * @param value          数据
     */
    @Observe
    @Override
    public void onCharacteristicChanged(Device device, UUID service, UUID characteristic, byte[] value) {
        Log.e("ble_bcf_data", "接收蓝牙数据：" + StringUtils.toHex(value));
        EventBus.getDefault().post(value);
    }

    public static void setBleData(String message) {
//        String savePath = ActivityUtils.getTopActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// HH:mm:ss
//        //获取当前时间
//        Date date = new Date(System.currentTimeMillis());
//
//        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
//        //获取当前时间
//        Date date1 = new Date(System.currentTimeMillis());
//
//        FileIOUtils.writeFileFromString(savePath + "/log/" + simpleDateFormat.format(date) + ".txt", simpleDateFormat1.format(date1) + ":" + message + "\n", true);
    }

}
