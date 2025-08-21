package com.topdon.tc001.utils;

import static android.content.Context.DOWNLOAD_SERVICE;
import static com.topdon.lms.sdk.LMS.SUCCESS;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ZipUtils;
import com.elvishew.xlog.XLog;
import com.topdon.lib.core.common.SharedManager;
import com.topdon.lib.core.config.HttpConfig;
import com.topdon.lib.core.dialog.TipDialog;
import com.topdon.lib.core.utils.AppUtil;
import com.topdon.lms.sdk.LMS;
import com.topdon.lms.sdk.activity.LmsUpdateDialog;
import com.topdon.lms.sdk.bean.AppInfoBean;
import com.topdon.lms.sdk.utils.NetworkUtil;
import com.topdon.lms.sdk.weiget.TToast;
import com.topdon.lms.sdk.xutils.common.Callback;
import com.topdon.lms.sdk.xutils.common.task.PriorityExecutor;
import com.topdon.lms.sdk.xutils.http.RequestParams;
import com.topdon.tc001.R;
import com.topdon.tc001.tools.VersionTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * AppVersionUtil
 * APP版本检测工具类
 *
 * @author chuanfeng.bi
 * @date 2022/2/10 19:48
 */
public class AppVersionUtil {
    private Context mContext;
    private DownloadCompleteReceiver completeReceiver; // 声明一个下载完成的广播接收器
    private DownloadManager dowanloadmanager = null;
    private DotIsShowListener dotIsShowListener = null;
    private String fileName = "";//文件名称
    private Long mDownloadId = 0l;//下载id

    public AppVersionUtil(Context context, DotIsShowListener dotIsShow) {
        this.mContext = context;
        this.dotIsShowListener = dotIsShow;
    }

    public void checkVersion( boolean isShowDialog) {
        if (dowanloadmanager == null) {
            dowanloadmanager = (DownloadManager) mContext.getSystemService(DOWNLOAD_SERVICE);
        }

        if (!NetworkUtil.isConnected(mContext)) {
            TToast.shortToast(mContext, com.topdon.lms.sdk.R.string.lms_setting_http_error);
            return;
        }
        LMS.getInstance().checkAppUpdate(commonBean -> {
            if (commonBean.code == SUCCESS) {
                AppInfoBean appInfoBean = LMS.getInstance().getUpdateAppInfoBean();
                XLog.w("bcf", "app更新信息:" + GsonUtils.toJson(appInfoBean));
                if (appInfoBean != null) {
                    if (appInfoBean.getVersionCode() > getDealVersionCode()) {
                        if (isShowDialog) {
                            String information = "";
//                            showNewVersionDialog(appInfoBean);
                            if (appInfoBean.softConfigOtherTypeVOList != null) {
                                for (AppInfoBean.UpdateDescription updateDescription : appInfoBean.softConfigOtherTypeVOList) {
                                    if (updateDescription.descType == 3) {
                                        information = updateDescription.textDescription;
                                    }
                                }
                            }
                            showUpdateDialog(mContext, appInfoBean.downloadPackageUrl, information,Integer.parseInt(appInfoBean.forcedUpgradeFlag));
                        }
                        if (dotIsShowListener != null) {
                            dotIsShowListener.isShow(true);
                            dotIsShowListener.version(appInfoBean.versionNo);
                        }
                        HttpConfig.INSTANCE.setHasNewVersion(true);
                    } else {
                        HttpConfig.INSTANCE.setHasNewVersion(false);
                    }
                } else {
                    HttpConfig.INSTANCE.setHasNewVersion(false);
                }
            } else {
                HttpConfig.INSTANCE.setHasNewVersion(false);
            }
        });
    }

    /**
     * 获取处理过的本地版本code
     *
     * @return float
     */
    private float getDealVersionCode() {
        return AppUtil.getVersionCode(mContext) / 10;
    }

    /**
     * 弹出新版本信息提示框
     *
     * @param bean 版本更新实体类
     */
    private void showNewVersionDialog(AppInfoBean bean) {
        String information = "";
        if (bean.softConfigOtherTypeVOList != null) {
            for (AppInfoBean.UpdateDescription updateDescription : bean.softConfigOtherTypeVOList) {
                if (updateDescription.descType == 3) {
                    information = updateDescription.textDescription;
                }
            }
        }
        if (Integer.parseInt(bean.forcedUpgradeFlag) == 1) {
            // 强制更新
            new TipDialog.Builder(mContext)
                    .setMessage(information)
                    .setTitleMessage(mContext.getString(R.string.updata_new_version_update))
                    .setPositiveListener(R.string.app_confirm, new Function0<Unit>() {
                        @Override
                        public Unit invoke() {
                            if(mDownloadId>0l){
                                TToast.shortToast(mContext, mContext.getString(R.string.installation_package_downloading));
                                return null;
                            }else{
                                TToast.shortToast(mContext, mContext.getString(R.string.installation_package_downloading_tips));
                            }
                            startDownload(bean.downloadPackageUrl);
                            return null;
                        }
                    })
                    .create().show();
        } else {
            new TipDialog.Builder(mContext)
                    .setMessage(information)
                    .setTitleMessage(mContext.getString(R.string.updata_new_version_update))
                    .setPositiveListener(R.string.app_confirm, new Function0<Unit>() {
                        @Override
                        public Unit invoke() {
                            if(mDownloadId>0l){
                                TToast.shortToast(mContext, mContext.getString(R.string.installation_package_downloading));
                                return null;
                            }else{
                                TToast.shortToast(mContext, mContext.getString(R.string.installation_package_downloading_tips));
                            }
                            startDownload(bean.downloadPackageUrl);
                            return null;
                        }
                    })
                    .setCancelListener(R.string.app_cancel, new Function0<Unit>() {
                        @Override
                        public Unit invoke() {
                            SharedManager.INSTANCE.setVersionCheckDate(System.currentTimeMillis());//刷新版本提示时间
                            return null;
                        }
                    })
                    .create().show();
        }
    }

    public interface DotIsShowListener {
        void isShow(boolean show);

        void version(String version);
    }


    // 开始下载指定序号的apk文件
    private void startDownload(String url) {
        completeReceiver = new DownloadCompleteReceiver();
        // 注册接收器，注册之后才能正常接收广播

        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT < 33) {
            mContext.registerReceiver(completeReceiver, intentFilter);
        } else {
            mContext.registerReceiver(completeReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        }

        Uri uri = Uri.parse(url); // 根据下载地址构建一个Uri对象
        DownloadManager.Request down = new DownloadManager.Request(uri); // 创建一个下载请求对象，指定从哪里下载文件
        down.setTitle(mContext.getString(R.string.tips_download_information)); // 设置任务标题
        down.setDescription(mContext.getString(R.string.installation_package_download_progress)); // 设置任务描述
        // 设置允许下载的网络类型
        down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        // 设置通知栏在下载进行时与完成后都可见
        down.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 设置下载文件在私有目录的保存路径。从Android10开始，只有保存到公共目录的才会在系统下载页面显示，保存到私有目录的不在系统下载页面显示
        fileName = "topinfrared" + System.currentTimeMillis() + ".zip";
        down.setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, fileName);
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(DOWNLOAD_SERVICE);
        // 设置下载文件在公共目录的保存路径。保存到公共目录需要申请存储卡的读写权限
        mDownloadId = downloadManager.enqueue(down); // 把下载请求对象加入到下载队列
        VersionTools.INSTANCE.setMDownloadId(mDownloadId);
    }


    // 定义一个下载完成的广播接收器。用于接收下载完成事件
    private class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE))   // 下载完毕
            {
                // 从意图中解包获得下载编号
                installApk();
            }
        }
    }


    // 安装应用程序
    public void installApk() {
        mDownloadId = 0l;
        VersionTools.INSTANCE.setMDownloadId(0l);
        mContext.unregisterReceiver(completeReceiver);
        try {
            File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), fileName);
            File localFile = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());//本地文件
            List<File> files = ZipUtils.unzipFile(file, localFile);
            if (files != null && files.size() != 0) {
                AppUtil.installApp(mContext, files.get(0));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showUpdateDialog(Context context, String url, String content,int forcedUpgradeFlag) {
        LmsUpdateDialog.Build.INSTANCE.setContentStr(content)
                .setUpgradeFlag(forcedUpgradeFlag)
                .setSureEvent(() -> {
                    download(url);
                    return null;
                })
                .setCancelEvent(() -> {

                    return null;
                }).build(context);
    }

    public void download(String url) {
        RequestParams params = new RequestParams();
        try {
            //这里为了解决 xutils 会把url转义 照成签名不对
            String[] splitUrl = url.split("\\?");
            String[] urlParams = splitUrl[1].split("&");
            String[] params1 = urlParams[0].split("=");
            String[] params2 = urlParams[1].split("=");
            String[] params3 = urlParams[2].split("=");
            url = splitUrl[0];
            params.addBodyParameter(params1[0], params1[1]);
            params.addBodyParameter(params2[0], params2[1]);
            params.addBodyParameter(params3[0], params3[1]);
        } catch (Exception e) {
            XLog.e("bcf", "升级接口解析异常");
        }
        fileName = "topinfrared" + System.currentTimeMillis() + ".zip";
        String path = mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + fileName;
        XLog.e("bcf", "download path:" + path);
        params.setSaveFilePath(path);
        params.setCacheDirName(fileName);
        params.setAutoResume(true);
        params.setExecutor(new PriorityExecutor(3, true));
        params.setUri(url);

        com.topdon.lms.sdk.xutils.x.http().get(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {
                XLog.e("bcf", "onWaiting");
            }

            @Override
            public void onStarted() {
                XLog.e("bcf", "onStarted");
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                XLog.w("bcf", "onLoading： " + current + "/" + total);
                int progress = (int) (current * 100 / total);
                LmsUpdateDialog.Build.INSTANCE.setProgressNum(progress / 100f);
            }

            @Override
            public void onSuccess(File result) {
                XLog.e("bcf", "onSuccess,start install apk");
                LmsUpdateDialog.Build.INSTANCE.dismiss();
                installApkNew();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                XLog.e("bcf", "onError " + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
                XLog.e("bcf", "onCancelled " + cex.getMessage());
            }

            @Override
            public void onFinished() {
                XLog.e("bcf", "onFinished");
            }
        });
    }

    // 安装应用程序
    public void installApkNew() {
        try {
            File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), fileName);
            File localFile = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());//本地文件
            List<File> files = ZipUtils.unzipFile(file, localFile);
            if (files != null && files.size() != 0) {
                AppUtil.installApp(mContext, files.get(0));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
