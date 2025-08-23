package com.topdon.ble;

import android.bluetooth.BluetoothGattCharacteristic;

public class WriteOptions {
    final int packageWriteDelayMillis;
    final int requestWriteDelayMillis;
    int packageSize;
    final boolean isWaitWriteResult;
    final int writeType;
    final boolean useMtuAsPackageSize;

    private WriteOptions(Builder builder) {
        packageWriteDelayMillis = builder.packageWriteDelayMillis;
        requestWriteDelayMillis = builder.requestWriteDelayMillis;
        packageSize = builder.packageSize;
        isWaitWriteResult = builder.isWaitWriteResult;
        writeType = builder.writeType;
        useMtuAsPackageSize = builder.useMtuAsPackageSize;
    }

    public int getPackageWriteDelayMillis() {
        return packageWriteDelayMillis;
    }

    public int getRequestWriteDelayMillis() {
        return requestWriteDelayMillis;
    }

    public int getPackageSize() {
        return packageSize;
    }

    public boolean isWaitWriteResult() {
        return isWaitWriteResult;
    }

    public int getWriteType() {
        return writeType;
    }

    public static class Builder {
        private int packageWriteDelayMillis = 0;
        private int requestWriteDelayMillis = -1;
        private int packageSize = 20;
        private boolean isWaitWriteResult = true;
        private int writeType = -1;
        private boolean useMtuAsPackageSize = false;

        public Builder setPackageWriteDelayMillis(int packageWriteDelayMillis) {
            this.packageWriteDelayMillis = packageWriteDelayMillis;
            return this;
        }

        public Builder setRequestWriteDelayMillis(int requestWriteDelayMillis) {
            this.requestWriteDelayMillis = requestWriteDelayMillis;
            return this;
        }

        public Builder setPackageSize(int packageSize) {
            if (packageSize > 0) {
                this.packageSize = packageSize;
            }
            return this;
        }

        public Builder setWaitWriteResult(boolean waitWriteResult) {
            isWaitWriteResult = waitWriteResult;
            return this;
        }

        public Builder setWriteType(int writeType) {
            if (writeType == BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT ||
                    writeType == BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE ||
                    writeType == BluetoothGattCharacteristic.WRITE_TYPE_SIGNED) {
                this.writeType = writeType;
            }            
            return this;
        }

        public Builder setMtuAsPackageSize() {
            useMtuAsPackageSize = true;
            return this;
        }
        
        public WriteOptions build() {
            return new WriteOptions(this);
        }
    }
}
