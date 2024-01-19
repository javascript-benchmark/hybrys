package com.worldpay.exception;

import com.worldpay.service.mac.impl.MD5MacValidator;

/**
 * Exception thrown from the {@link MD5MacValidator} if there is a problem validating the mac code
 */
public class WorldpayMacValidationException extends WorldpayException {

    private static final long serialVersionUID = -8359161688887712978L;

    /**
     * Default constructor setting a message and a cause
     *
     * @param message
     * @param throwable
     */
    public WorldpayMacValidationException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Default constructor setting a message
     *
     * @param message
     */
    public WorldpayMacValidationException(final String message) {
        super(message);
    }
}
