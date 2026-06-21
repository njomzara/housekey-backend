package com.housekey.shared.error;

public interface LocalizedException {

    String getMessageKey();

    Object[] getMessageArgs();
}
