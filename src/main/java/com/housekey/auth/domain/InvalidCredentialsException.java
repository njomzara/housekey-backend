package com.housekey.auth.domain;

import com.housekey.shared.error.LocalizedException;

public class InvalidCredentialsException extends RuntimeException implements LocalizedException {

    private static final String MESSAGE_KEY = "error.auth.invalidCredentials";

    public InvalidCredentialsException() {
        super(MESSAGE_KEY);
    }

    @Override
    public String getMessageKey() {
        return MESSAGE_KEY;
    }

    @Override
    public Object[] getMessageArgs() {
        return new Object[0];
    }
}
