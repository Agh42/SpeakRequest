package de.koderman.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/i18n")
@RequiredArgsConstructor
public class I18nController {
    
    private final MessageSource messageSource;
    
    /**
     * Get all messages for a specific locale based on Accept-Language header
     * Returns all messages from messages.properties as a flat key-value map
     */
    @GetMapping("/messages")
    public Map<String, String> getMessages(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage) {
        
        Locale locale = parseLocale(acceptLanguage);
        
        // Get all message keys and their values
        Map<String, String> messages = new HashMap<>();
        
        // Get all known message keys and resolve them
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        Enumeration<String> keys = bundle.getKeys();
        
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            try {
                String message = messageSource.getMessage(key, null, locale);
                messages.put(key, message);
            } catch (Exception e) {
                // Skip keys that can't be resolved
            }
        }
        
        return messages;
    }
    
    /**
     * Parse locale from Accept-Language header
     * Examples: "en", "en-US", "de", "de-DE"
     */
    private Locale parseLocale(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            return Locale.ENGLISH;
        }
        
        // Take the first locale from the Accept-Language header
        String[] parts = acceptLanguage.split(",")[0].split("-");
        
        if (parts.length == 1) {
            return Locale.forLanguageTag(parts[0]);
        } else if (parts.length >= 2) {
            return Locale.forLanguageTag(parts[0] + "-" + parts[1]);
        }
        
        return Locale.ENGLISH;
    }
}
