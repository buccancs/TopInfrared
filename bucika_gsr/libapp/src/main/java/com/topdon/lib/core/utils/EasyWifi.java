package com.topdon.lib.core.utils;

import android.content.Context;
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;

import com.topdon.lib.core.BaseApplication;

/**
 * des:
 * author: CaiSongL
 * date: 2024/5/23 17:39
 **/
public class EasyWifi {
    private static volatile EasyWifi mInstance;
    private WifiConnectCallback wifiConnectCallback;
    String TAG = "EasyWifi";
    private final WifiManager wifiManager = (WifiManager) BaseApplication.instance.getSystemService(Context.WIFI_SERVICE);
    private final ConnectivityManager connectivityManager = (ConnectivityManager) BaseApplication.instance.getSystemService(Context.CONNECTIVITY_SERVICE);

    /* loaded from: classes2.dex */
    public enum WiFiEncryptionStandard {
        WEP,
        WPA_EAP,
        WPA_PSK,
        WPA2,
        WPA3
    }

    /* loaded from: classes2.dex */
    public enum WifiCapability {
        WIFI_CIPHER_WEP,
        WIFI_CIPHER_WPA,
        WIFI_CIPHER_NO_PASS
    }

    /* loaded from: classes2.dex */
    public interface WifiConnectCallback {
        void onFailure();

        void onSuccess(Network network);
    }

    public static EasyWifi getInstance() {
        if (mInstance == null) {
            synchronized (EasyWifi.class) {
                if (mInstance == null) {
                    mInstance = new EasyWifi();
                }
            }
        }
        return mInstance;
    }

    public void useWifiFirst() {
        this.connectivityManager.setNetworkPreference(1);
    }

    public void setWifiConnectCallback(WifiConnectCallback wifiConnectCallback) {
        this.wifiConnectCallback = wifiConnectCallback;
    }

    public boolean isWifiEnabled() {
        return this.wifiManager.isWifiEnabled();
    }

    public WifiManager getWifiManager() {
        return this.wifiManager;
    }

    public ConnectivityManager getConnectivityManager() {
        return this.connectivityManager;
    }

    public void connectByNew(String str, String str2) {
        connectByNew(str, str2, WiFiEncryptionStandard.WPA2);
    }

    public void connectByNew(String str, String str2, WiFiEncryptionStandard wiFiEncryptionStandard) {
        WifiNetworkSpecifier build = new WifiNetworkSpecifier.Builder().setSsid(str).setWpa2Passphrase(str2).build();
        if (wiFiEncryptionStandard == WiFiEncryptionStandard.WPA3) {
            build = new WifiNetworkSpecifier.Builder().setSsid(str).setWpa3Passphrase(str2).build();
        }
        this.connectivityManager.requestNetwork(new NetworkRequest.Builder().addTransportType(1).addCapability(13).addCapability(14).setNetworkSpecifier(build).build(), new ConnectivityManager.NetworkCallback() { // from class: com.ir.networklib.EasyWifi.1
            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onAvailable(Network network) {
                super.onAvailable(network);
                if (EasyWifi.this.wifiConnectCallback != null) {
                    EasyWifi.this.wifiConnectCallback.onSuccess(network);
                }
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onUnavailable() {
                super.onUnavailable();
                if (EasyWifi.this.wifiConnectCallback != null) {
                    EasyWifi.this.wifiConnectCallback.onFailure();
                }
            }
        });
    }

    public boolean connectByOld(String str, String str2, WifiCapability wifiCapability) {
        int addNetwork = this.wifiManager.addNetwork(createWifiConfig(str, str2, wifiCapability));
        if (addNetwork == -1) {
            Log.e(this.TAG, "操作失败,需要您到手机wifi列表中取消对设备连接的保存");
        }
        boolean enableNetwork = this.wifiManager.enableNetwork(addNetwork, true);
        Log.d(this.TAG, "connectByOld: " + (enableNetwork ? "成功" : "失败"));
        return enableNetwork;
    }

    private WifiConfiguration isExist(String str) {
        for (WifiConfiguration wifiConfiguration : this.wifiManager.getConfiguredNetworks()) {
            if (wifiConfiguration.SSID.equals("\"" + str + "\"")) {
                return wifiConfiguration;
            }
        }
        return null;
    }

    private WifiConfiguration createWifiConfig(String str, String str2, WifiCapability wifiCapability) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.allowedAuthAlgorithms.clear();
        wifiConfiguration.allowedGroupCiphers.clear();
        wifiConfiguration.allowedKeyManagement.clear();
        wifiConfiguration.allowedPairwiseCiphers.clear();
        wifiConfiguration.allowedProtocols.clear();
        wifiConfiguration.SSID = "\"" + str + "\"";
        WifiConfiguration isExist = isExist(str);
        if (isExist != null) {
            Log.d(this.TAG, "createWifiConfig: 移除网路（true:成功，false:失败），结果=" + this.wifiManager.removeNetwork(isExist.networkId) + "移除后保存" + this.wifiManager.saveConfiguration());
        }
        Log.d(this.TAG, "createWifiConfig: 当前ssid=" + str);
        if (wifiCapability == WifiCapability.WIFI_CIPHER_NO_PASS) {
            wifiConfiguration.allowedKeyManagement.set(0);
        } else if (wifiCapability == WifiCapability.WIFI_CIPHER_WEP) {
            wifiConfiguration.hiddenSSID = true;
            wifiConfiguration.wepKeys[0] = "\"" + str2 + "\"";
            wifiConfiguration.allowedAuthAlgorithms.set(0);
            wifiConfiguration.allowedAuthAlgorithms.set(1);
            wifiConfiguration.allowedKeyManagement.set(0);
            wifiConfiguration.wepTxKeyIndex = 0;
        } else if (wifiCapability == WifiCapability.WIFI_CIPHER_WPA) {
            wifiConfiguration.preSharedKey = "\"" + str2 + "\"";
            wifiConfiguration.hiddenSSID = true;
            wifiConfiguration.allowedAuthAlgorithms.set(0);
            wifiConfiguration.allowedGroupCiphers.set(2);
            wifiConfiguration.allowedKeyManagement.set(1);
            wifiConfiguration.allowedPairwiseCiphers.set(1);
            wifiConfiguration.allowedGroupCiphers.set(3);
            wifiConfiguration.allowedPairwiseCiphers.set(2);
            wifiConfiguration.status = 2;
            wifiConfiguration.priority = 100000;
        }
        return wifiConfiguration;
    }

    public static boolean isNetConnected(ConnectivityManager connectivityManager) {
        return connectivityManager.getActiveNetwork() != null;
    }

    public static boolean isWifi(ConnectivityManager connectivityManager) {
        NetworkCapabilities networkCapabilities;
        if (connectivityManager.getActiveNetwork() != null && (networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork())) == null) {
            return networkCapabilities.hasTransport(1);
        }
        return false;
    }

    public void setNetworkType(NetType netType) {
        Log.d(this.TAG, "selectNetworkType: 强制使用wifi网络或者移动数据网络");
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        if (netType == NetType.WIFI) {
            builder.addTransportType(1);
        } else {
            builder.addTransportType(0);
        }
        getConnectivityManager().requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() { // from class: com.ir.networklib.EasyWifi.2
            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onAvailable(Network network) {
                try {
                    Log.d(EasyWifi.this.TAG, "设置网络类型时onAvailable: ");
                    EasyWifi.this.getConnectivityManager().bindProcessToNetwork(network);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getConnectSSID() {
        return this.wifiManager.getConnectionInfo().getSSID();
    }
}
