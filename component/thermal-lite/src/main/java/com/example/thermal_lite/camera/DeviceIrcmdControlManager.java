package com.example.thermal_lite.camera;

import android.util.Log;

import com.energy.ac020library.IrcamEngine;
import com.energy.ac020library.IrcmdEngine;
import com.energy.ac020library.bean.IrcmdError;
import com.energy.commoncomponent.Const;
import com.energy.commonlibrary.util.FileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class DeviceIrcmdControlManager {

    private static final String TAG = "DeviceIrcmdControlManager";
    private IrcamEngine mIrcamEngine;
    private IrcmdEngine mIrcmdEngine;

    private boolean mSendFPGACommand = false;
    private boolean mSendISPCommand = false;

    private String ispParamPath;

    private DeviceIrcmdControlManager() {

    }

    private static DeviceIrcmdControlManager mInstance;

    public static synchronized DeviceIrcmdControlManager getInstance() {
        if (mInstance == null) {
            mInstance = new DeviceIrcmdControlManager();
        }
        return mInstance;
    }

    public IrcmdEngine getIrcmdEngine() {
        return mIrcmdEngine;
    }

    public void setIrcmdEngine(IrcmdEngine ircmdEngine) {
        this.mIrcmdEngine = ircmdEngine;
    }

    public IrcamEngine getIrcamEngine() {
        return mIrcamEngine;
    }

    public void setIrcamEngine(IrcamEngine ircamEngine) {
        this.mIrcamEngine = ircamEngine;
    }

    public void setSendFPGACommand(boolean sendFPGACommand) {
        mSendFPGACommand = sendFPGACommand;
    }

    public void sendFPGAParam() {
        if (!mSendFPGACommand) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "sendFPGAParam");
                try {
                    String fpga_param_path = Const.DATA_FILE_SAVE_PATH + File.separator + "fpga.json";
                    File file = new File(fpga_param_path);
                    if (!file.exists()) {
                        return;
                    }
                    String fpgaParams = FileUtil.getStringFromFile(fpga_param_path);
                    int firstAddress = 0x0096;

                    JSONArray jsonArray = new JSONArray(fpgaParams);
                    Log.d(TAG, "first jsonArray length : " + jsonArray.length());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int[] params = new int[1];
                        String name = jsonObject.getString("name");
                        String address = jsonObject.getString("address");
                        double value = jsonObject.getDouble("value");
                        params[0] = (int) value;
                        Log.d(TAG, "first params value : " + params[0]);
                        int reAddress = Integer.parseInt(address.substring(2), 16);
                        Log.d(TAG, "first address string : " + reAddress);
                        if (mIrcmdEngine != null) {
                            IrcmdError algorithmParametersWriteGet = mIrcmdEngine
                                    .advAlgorithmParametersWrite(reAddress, params);
                            Log.d(TAG, "algorithmParametersWriteGet result = " + algorithmParametersWriteGet);

                            int[] algorithmParametersReadData = new int[1];
                            IrcmdError algorithmParametersReadGet = mIrcmdEngine
                                    .advAlgorithmParametersRead(reAddress, algorithmParametersReadData);

                            Log.d(TAG, "algorithmParametersReadGet result = " + algorithmParametersReadGet);

                            for (int j = 0; j < algorithmParametersReadData.length; j++) {
                                Log.d(TAG, "algorithmParametersReadGet value = " + algorithmParametersReadData[j]);
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSendFPGACommand = false;
            }
        }).start();
    }

    public static byte[] intToBytes2(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    public static byte[] intToBytes2(long value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    public static int bytesToInt2(byte[] src, int offset) {
        int value =
                (((src[offset] & 0xFF) << 24) | ((src[offset + 1] & 0xFF) << 16) | ((src[offset + 2] & 0xFF) << 8) | (src[offset + 3] & 0xFF));
        return value;
    }

    public static String getReadValue(String name, byte[] ispParamReadByteArray, int byteWidth, int begin, int end) {
        StringBuilder ispParamReadByteArrStr = new StringBuilder();
        for (int i = 0; i < ispParamReadByteArray.length; i++) {
            ispParamReadByteArrStr.append(String.format("%8s",
                    Integer.toBinaryString(ispParamReadByteArray[i] & 0xFF)).replace(' ', '0'));
        }
        Log.i(TAG, "name = " + name + " ispParamReadByteArrStr = " + ispParamReadByteArrStr.toString() +
                " ispParamReadByteArrStrInt = " + Long.parseLong(ispParamReadByteArrStr.toString(), 2));

        String orgValue = ispParamReadByteArrStr.substring(byteWidth * 8 - end - 1, byteWidth * 8 - begin);
        Log.i(TAG, "name = " + name + " orgValue = " + orgValue +
                " orgValueInt = " + Long.parseLong(orgValue, 2));

        return String.valueOf(Long.parseLong(orgValue, 2));
    }

    public static long byteArrToBinStr(String name, byte[] ispParamReadByteArray, int byteWidth, int begin, int end,
                                       byte[] valueArray) {
        StringBuilder ispParamReadByteArrStr = new StringBuilder();
        for (int i = 0; i < ispParamReadByteArray.length; i++) {
            ispParamReadByteArrStr.append(String.format("%8s",
                    Integer.toBinaryString(ispParamReadByteArray[i] & 0xFF)).replace(' ', '0'));
        }
        Log.i(TAG, "name = " + name + " ispParamReadByteArrStr = " + ispParamReadByteArrStr.toString() +
                " ispParamReadByteArrStrInt = " + Long.parseLong(ispParamReadByteArrStr.toString(), 2));

        StringBuilder valueArrStr = new StringBuilder();
        for (int i = 0; i < valueArray.length; i++) {
            valueArrStr.append(String.format("%8s", Integer.toBinaryString(valueArray[i] & 0xFF)).replace(' ', '0'));
        }
        Log.i(TAG, "name = " + name + " valueArrStr = " + valueArrStr.toString());

        String orgValue = ispParamReadByteArrStr.substring(byteWidth * 8 - end - 1, byteWidth * 8 - begin);
        Log.i(TAG, "name = " + name + " orgValue = " + orgValue +
                " orgValueInt = " + Long.parseLong(orgValue, 2));

        String valueStr = ispParamReadByteArrStr.replace(byteWidth * 8 - end - 1, byteWidth * 8 - begin,
                valueArrStr.substring(byteWidth * 8 - end - 1, byteWidth * 8 - begin)).toString();

        Log.i(TAG, "name = " + name + " valueStr = " + valueArrStr.toString() + " valueStr = " + valueStr +
                " valueStrInt = " + Long.parseLong(valueStr, 2));

        return Long.parseLong(valueStr, 2);
    }

    public void setISPChangePath(String param_path) {
        ispParamPath = param_path;
        mSendISPCommand = true;
    }

    public void sendISPParam() {
        if (!mSendISPCommand) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "sendISPParam");
                try {
                    if (ispParamPath == null || ispParamPath.isEmpty()) {
                        return;
                    }
                    File file = new File(ispParamPath);
                    if (!file.exists()) {
                        return;
                    }
                    String fpgaParams = FileUtil.getStringFromFile(ispParamPath);

                    JSONArray jsonArray = new JSONArray(fpgaParams);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        long[] ispParamWriteData = new long[1];
                        String name = jsonObject.getString("name");
                        String address = jsonObject.getString("address");
                        if (address.startsWith("0x") || address.startsWith("0X")) {
                            address = address.substring(2);
                        }
                        int begin = jsonObject.getInt("begin");
                        int end = jsonObject.getInt("end");
                        int value = jsonObject.getInt("value");
                        int reAddress = Integer.parseInt(address, 16);
                        if (mIrcmdEngine != null) {
                            long[] ispParamReadData = new long[1];
                            if (IrcmdError.IRCMD_SUCCESS != mIrcmdEngine.advISPParamRead(reAddress, ispParamReadData)) {
                                throw new IllegalArgumentException("The method advISPParamRead execute fail.");
                            }
                            ispParamWriteData[0] = byteArrToBinStr(name, intToBytes2(ispParamReadData[0]), 4,
                                    begin, end, intToBytes2(value));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSendISPCommand = false;
                ispParamPath = null;
            }
        }).start();
    }
}
