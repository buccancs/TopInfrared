package com.topdon.ble;

import com.topdon.ble.util.Logger;
import com.topdon.commons.observer.Observable;
import com.topdon.commons.poster.ThreadMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EasyBLEBuilder {
    private final static ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    BondController bondController;
    DeviceCreator deviceCreator;
    ThreadMode methodDefaultThreadMode = ThreadMode.MAIN;
    ExecutorService executorService = DEFAULT_EXECUTOR_SERVICE;
    ScanConfiguration scanConfiguration;
    Observable observable;
    Logger logger;
    boolean isObserveAnnotationRequired = false;
    ScannerType scannerType;

    EasyBLEBuilder() {
    }

    public EasyBLEBuilder setScannerType(ScannerType scannerType) {
        Inspector.requireNonNull(scannerType, "scannerType can't be null");
        this.scannerType = scannerType;
        return this;
    }

    public EasyBLEBuilder setExecutorService(ExecutorService executorService) {
        Inspector.requireNonNull(executorService, "executorService can't be null");
        this.executorService = executorService;
        return this;
    }

    public EasyBLEBuilder setDeviceCreator(DeviceCreator deviceCreator) {
        Inspector.requireNonNull(deviceCreator, "deviceCreator can't be null");
        this.deviceCreator = deviceCreator;
        return this;
    }

    public EasyBLEBuilder setBondController(BondController bondController) {
        Inspector.requireNonNull(bondController, "bondController can't be null");
        this.bondController = bondController;
        return this;
    }

    public EasyBLEBuilder setMethodDefaultThreadMode(ThreadMode mode) {
        Inspector.requireNonNull(mode, "mode can't be null");
        methodDefaultThreadMode = mode;
        return this;
    }

    public EasyBLEBuilder setScanConfiguration(ScanConfiguration scanConfiguration) {
        Inspector.requireNonNull(scanConfiguration, "scanConfiguration can't be null");
        this.scanConfiguration = scanConfiguration;
        return this;
    }

    public EasyBLEBuilder setLogger(Logger logger) {
        Inspector.requireNonNull(logger, "logger can't be null");
        this.logger = logger;
        return this;
    }

    public EasyBLEBuilder setObservable(Observable observable) {
        Inspector.requireNonNull(observable, "observable can't be null");
        this.observable = observable;
        return this;
    }

    public EasyBLEBuilder setObserveAnnotationRequired(boolean observeAnnotationRequired) {
        isObserveAnnotationRequired = observeAnnotationRequired;
        return this;
    }

    public EasyBLE build() {
        synchronized (EasyBLE.class) {
            if (EasyBLE.instance != null) {
                throw new EasyBLEException("EasyBLE instance already exists. It can only be instantiated once.");
            }
            EasyBLE.instance = new EasyBLE(this);
            return EasyBLE.instance;
        }
    }
}
