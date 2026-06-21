package com.housekey.shared.i18n;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class LocalizedMessages {

    private static final List<Locale> SUPPORTED_LOCALES = List.of(LocaleConfig.DEFAULT_LOCALE, Locale.ENGLISH);

    private final MessageSource messageSource;

    public LocalizedMessages(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, locale());
    }

    public String get(HttpServletRequest request, String key, Object... args) {
        return messageSource.getMessage(key, args, locale(request));
    }

    public String getOrDefault(String keyOrMessage, Object... args) {
        if (keyOrMessage == null) {
            return null;
        }
        try {
            return messageSource.getMessage(keyOrMessage, args, locale());
        } catch (NoSuchMessageException ex) {
            return keyOrMessage;
        }
    }

    public String get(MessageSourceResolvable resolvable) {
        return messageSource.getMessage(resolvable, locale());
    }

    public Map<String, String> localizeFieldErrors(Map<String, String> fieldErrors) {
        Map<String, String> localized = new LinkedHashMap<>();
        fieldErrors.forEach((field, message) -> localized.put(field, getOrDefault(message)));
        return localized;
    }

    private Locale locale() {
        return LocaleContextHolder.getLocale();
    }

    private Locale locale(HttpServletRequest request) {
        String header = request.getHeader("Accept-Language");
        if (header == null || header.isBlank()) {
            return LocaleConfig.DEFAULT_LOCALE;
        }
        try {
            Locale locale = Locale.lookup(Locale.LanguageRange.parse(header), SUPPORTED_LOCALES);
            return locale == null ? LocaleConfig.DEFAULT_LOCALE : locale;
        } catch (IllegalArgumentException ex) {
            return LocaleConfig.DEFAULT_LOCALE;
        }
    }
}
