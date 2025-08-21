package com.topdon.ble;

/**
 * date: 2021/8/12 12:08
 * author: bichuanfeng
 */
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
