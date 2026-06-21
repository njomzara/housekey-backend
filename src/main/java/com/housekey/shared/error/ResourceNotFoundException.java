package com.housekey.shared.error;

import java.util.Arrays;

public class ResourceNotFoundException extends RuntimeException implements LocalizedException {

    private final String messageKey;
    private final Object[] messageArgs;

    public ResourceNotFoundException(String messageKey, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = Arrays.copyOf(messageArgs, messageArgs.length);
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public Object[] getMessageArgs() {
        return Arrays.copyOf(messageArgs, messageArgs.length);
    }
}
