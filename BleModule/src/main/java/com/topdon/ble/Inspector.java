package com.topdon.ble;

final class Inspector {
    
    static <T> T requireNonNull(T obj, String message) {
        if (obj == null)
            throw new EasyBLEException(message);
        return obj;
    }
}
