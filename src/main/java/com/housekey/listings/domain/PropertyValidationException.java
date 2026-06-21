package com.housekey.listings.domain;

import java.util.Arrays;
import java.util.Map;

import com.housekey.shared.error.LocalizedException;

public class PropertyValidationException extends RuntimeException implements LocalizedException {

    private final String messageKey;
    private final Object[] messageArgs;
    private final Map<String, String> fieldErrors;

    public PropertyValidationException(String messageKey, Map<String, String> fieldErrors, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = Arrays.copyOf(messageArgs, messageArgs.length);
        this.fieldErrors = Map.copyOf(fieldErrors);
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public Object[] getMessageArgs() {
        return Arrays.copyOf(messageArgs, messageArgs.length);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
