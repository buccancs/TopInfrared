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

public class AppVersionUtil {
    private Context mContext;
    private DownloadCompleteReceiver completeReceiver;
    private DownloadManager dowanloadmanager = null;
    private DotIsShowListener dotIsShowListener = null;
    private String fileName = "";
    private Long mDownloadId = 0l;

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

    private float getDealVersionCode() {
        return AppUtil.getVersionCode(mContext) / 10;
    }

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
                            SharedManager.INSTANCE.setVersionCheckDate(System.currentTimeMillis());
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

    private void startDownload(String url) {
        completeReceiver = new DownloadCompleteReceiver();

        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT < 33) {
            mContext.registerReceiver(completeReceiver, intentFilter);
        } else {
            mContext.registerReceiver(completeReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        }

        Uri uri = Uri.parse(url);
        DownloadManager.Request down = new DownloadManager.Request(uri);
        down.setTitle(mContext.getString(R.string.tips_download_information));
        down.setDescription(mContext.getString(R.string.installation_package_download_progress));
        down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        down.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        fileName = "topinfrared" + System.currentTimeMillis() + ".zip";
        down.setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, fileName);
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(DOWNLOAD_SERVICE);
        mDownloadId = downloadManager.enqueue(down);
        VersionTools.INSTANCE.setMDownloadId(mDownloadId);
    }

    private class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            {
                installApk();
            }
        }
    }

    public void installApk() {
        mDownloadId = 0l;
        VersionTools.INSTANCE.setMDownloadId(0l);
        mContext.unregisterReceiver(completeReceiver);
        try {
            File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), fileName);
            File localFile = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
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

    public void installApkNew() {
        try {
            File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), fileName);
            File localFile = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
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
