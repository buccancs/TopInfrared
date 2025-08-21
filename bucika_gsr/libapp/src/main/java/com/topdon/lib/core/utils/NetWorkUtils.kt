package com.topdon.lib.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import com.elvishew.xlog.XLog
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.repository.TS004Repository

/**
 * des:
 * author: CaiSongL
 * date: 2024/3/5 9:07
 **/
object NetWorkUtils {

    private var mNetworkCallback: ConnectivityManager.NetworkCallback ?= null
    private var netWorkListener : ((network: Network?) -> Unit) ?= null
    val connectivityManager by lazy {
        BaseApplication.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val wifiManager by lazy {
        BaseApplication.instance.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun isWifiNameValid(context: Context, prefixes: List<String>): Boolean {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ssid = wifiInfo.ssid.replace("\"", "") // 移除双引号
        for (prefix in prefixes) {
            if (ssid.startsWith(prefix)) {
                return true
            }
        }
        return false
    }

    fun connectWifi(ssid: String, password: String, listener: ((network: Network?) -> Unit)? = null) {
        netWorkListener = listener
        if (Build.VERSION.SDK_INT < 29) {//低于 Android10
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)//不需要能访问 internet
                .build()
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    XLog.e("测试","onAvailable")
                    if (WifiUtil.getCurrentWifiSSID(BaseApplication.instance) == ssid) {
                        connectivityManager.unregisterNetworkCallback(this)
                        listener?.invoke(network)
                    }
                }

                override fun onUnavailable() {
                    XLog.e("测试","onUnavailable")
                    connectivityManager.unregisterNetworkCallback(this)
                    listener?.invoke(null)
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    XLog.e("测试","onCapabilitiesChanged")
                    super.onCapabilitiesChanged(network, networkCapabilities)
                }

                override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
                    super.onBlockedStatusChanged(network, blocked)
                    XLog.e("测试","onBlockedStatusChanged")
                }

                override fun onLinkPropertiesChanged(
                    network: Network,
                    linkProperties: LinkProperties
                ) {
                    super.onLinkPropertiesChanged(network, linkProperties)
                    XLog.e("测试","onLinkPropertiesChanged")
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    XLog.e("测试","onLosing")
                }
            }
            connectivityManager.registerNetworkCallback(request, callback)

            val configuration = WifiConfiguration()
            configuration.SSID = "\"$ssid\""
            configuration.preSharedKey = "\"$password\""
            configuration.hiddenSSID = false
            configuration.status = WifiConfiguration.Status.ENABLED
            val id = wifiManager.addNetwork(configuration)
            val isSuccess = wifiManager.enableNetwork(id, true)
            if (!isSuccess) {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        } else {
            val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build()
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build()
            if (mNetworkCallback == null){
                mNetworkCallback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        XLog.i("onAvailable() "+netWorkListener.hashCode())
                        netWorkListener?.invoke(network)
                    }

                    override fun onUnavailable() {
                        super.onUnavailable()
                        XLog.i("onUnavailable()")
                        netWorkListener?.invoke(null)
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)
                        XLog.i("onLost()")
                    }
                }
            }
            connectivityManager.requestNetwork(request, mNetworkCallback!!)
        }
    }


    fun switchNetwork(isWifi: Boolean, listener: ((network: Network?) -> Unit)? = null) {
        if (Build.VERSION.SDK_INT < 29) {//低于 Android10
            return
        }
        if (isWifi){
            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.boundNetworkForProcess)
            if (networkCapabilities != null &&
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                XLog.i("已经是wifi,跳过")
                return
            }
        }
        val request: NetworkRequest = NetworkRequest.Builder()
            .addTransportType(if (isWifi) NetworkCapabilities.TRANSPORT_WIFI else NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                XLog.i("切换到 ${if (isWifi) "WIFI" else "流量"} onAvailable()")
                if (isWifi) {
                    TS004Repository.netWork = network
                }
                connectivityManager.bindProcessToNetwork(network)
                connectivityManager.unregisterNetworkCallback(this)
                listener?.invoke(network)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                connectivityManager.unregisterNetworkCallback(this)
                XLog.w("切换到 ${if (isWifi) "WIFI" else "流量"} onUnavailable()")
                listener?.invoke(null)
            }
        })
    }
}