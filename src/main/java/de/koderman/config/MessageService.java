package de.koderman.config;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageService {
    
    private final MessageSource messageSource;
    
    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    /**
     * Get a message for the current locale
     * @param key The message key
     * @return The localized message
     */
    public String getMessage(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, key, locale);
    }
    
    /**
     * Get a message with parameters for the current locale
     * @param key The message key
     * @param params Parameters to substitute in the message
     * @return The localized message with substituted parameters
     */
    public String getMessage(String key, Object[] params) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, params, key, locale);
    }
    
    /**
     * Get a message for a specific locale
     * @param key The message key
     * @param locale The locale
     * @return The localized message
     */
    public String getMessage(String key, Locale locale) {
        return messageSource.getMessage(key, null, key, locale);
    }
    
    /**
     * Get a message with parameters for a specific locale
     * @param key The message key
     * @param params Parameters to substitute in the message
     * @param locale The locale
     * @return The localized message with substituted parameters
     */
    public String getMessage(String key, Object[] params, Locale locale) {
        return messageSource.getMessage(key, params, key, locale);
    }
}
