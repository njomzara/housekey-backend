package com.housekey.shared.error;

import java.util.Arrays;

import org.springframework.security.access.AccessDeniedException;

public class LocalizedAccessDeniedException extends AccessDeniedException implements LocalizedException {

    private final String messageKey;
    private final Object[] messageArgs;

    public LocalizedAccessDeniedException(String messageKey, Object... messageArgs) {
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
