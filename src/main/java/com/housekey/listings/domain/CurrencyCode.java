package com.housekey.listings.domain;

import java.util.Locale;

import com.housekey.shared.error.LocalizedIllegalArgumentException;

public enum CurrencyCode {
    USD,
    EUR,
    RSD;

    public static CurrencyCode from(String value) {
        if (value == null || value.isBlank()) {
            return USD;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "USD", "DOLLAR", "DOLLARS" -> USD;
            case "EUR", "EURO", "EUROS" -> EUR;
            case "RSD", "DINAR", "DINARS" -> RSD;
            default -> throw new LocalizedIllegalArgumentException("validation.currency.unsupported", value);
        };
    }
}
