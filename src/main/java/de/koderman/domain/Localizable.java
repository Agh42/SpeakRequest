package de.koderman.domain;

/**
 * Interface for enums that support internationalization
 */
public interface Localizable {
    
    /**
     * Get the message key for the display name
     * @return The message key (e.g., "goal.MAKE_DECISIONS.name")
     */
    String getNameKey();
    
    /**
     * Get the message key for the description
     * @return The message key (e.g., "goal.MAKE_DECISIONS.description")
     */
    String getDescriptionKey();
    
    /**
     * Get the default display name (fallback if no translation is available)
     * @return The default display name
     */
    String getDisplayName();
    
    /**
     * Get the default description (fallback if no translation is available)
     * @return The default description
     */
    String getDescription();
}
