package com.topdon.ble;

public class EasyBLEException extends RuntimeException {
    private static final long serialVersionUID = -7775315841108791634L;

    public EasyBLEException(String message) {
        super(message);
    }

    public EasyBLEException(String message, Throwable cause) {
        super(message, cause);
    }

    public EasyBLEException(Throwable cause) {
        super(cause);
    }
}
