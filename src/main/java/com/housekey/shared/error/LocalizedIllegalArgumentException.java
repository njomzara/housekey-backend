package com.housekey.shared.error;

import java.util.Arrays;

public class LocalizedIllegalArgumentException extends IllegalArgumentException implements LocalizedException {

    private final String messageKey;
    private final Object[] messageArgs;

    public LocalizedIllegalArgumentException(String messageKey, Object... messageArgs) {
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
