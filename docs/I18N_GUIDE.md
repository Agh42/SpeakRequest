# Internationalization (i18n) Guide

This document describes how the internationalization system works in SPEEK.NOW and how to use it.

## Overview

The application uses Spring Boot's standard internationalization mechanisms with:
- **messages.properties** file for storing translations
- **MessageSource** for resolving messages in Java code
- **REST API** endpoint for serving translations to JavaScript
- **i18n.js** module for client-side translation

## Architecture

### Backend (Java)

#### 1. Configuration
`InternationalizationConfig.java` configures:
- `MessageSource` bean to load messages from `messages.properties`
- `LocaleResolver` to determine the user's locale from the Accept-Language header

#### 2. Message Service
`MessageService.java` provides convenient methods for getting translated messages:
```java
// Simple translation
String message = messageService.getMessage("chair.title");

// With parameters
String message = messageService.getMessage("error.room.notfound", new Object[]{"ROOM1"});
```

#### 3. Localizable Enums
Enums that need translation implement the `Localizable` interface:
- `MeetingGoal`
- `ParticipationFormat`
- `DecisionRule`
- `Deliverable`

Each enum provides:
- `getNameKey()` - returns the message key for the name (e.g., "goal.MAKE_DECISIONS.name")
- `getDescriptionKey()` - returns the message key for the description
- `getDisplayName()` - returns the fallback English display name
- `getDescription()` - returns the fallback English description

#### 4. I18n REST API
`I18nController.java` provides the `/api/i18n/messages` endpoint that:
- Returns all translations as JSON
- Respects the Accept-Language header
- Used by JavaScript clients to load translations

### Frontend (JavaScript)

#### 1. i18n.js Module
The `i18n.js` module provides:
- Automatic loading of translations from the API
- `t(key, params)` function for translating messages
- Automatic page translation via data attributes
- Parameter substitution support

#### 2. Using in HTML

##### Option 1: Data Attributes (Declarative)
```html
<!-- Simple translation -->
<h2 data-i18n="chair.title"></h2>

<!-- With parameters -->
<div data-i18n="error.room.notfound" data-i18n-params='["ROOM1"]'></div>

<!-- Placeholder translation -->
<input data-i18n-placeholder="chair.speakers.behalf.placeholder" />

<!-- Aria-label translation -->
<button data-i18n-aria="chair.room.button.share"></button>

<!-- Title translation -->
<span data-i18n-title="chair.controls.title"></span>
```

##### Option 2: JavaScript API (Programmatic)
```javascript
// Wait for i18n to load
await i18n.init();

// Simple translation
const title = i18n.t('chair.title');

// With array parameters
const error = i18n.t('error.room.notfound', ['ROOM1']);

// With named parameters
const message = i18n.t('poll.totalvotes', {count: 42});

// Manually translate page elements
i18n.translatePage();
```

#### 3. Loading in HTML Files
Add the i18n.js script to your HTML files:
```html
<head>
  <!-- Other scripts -->
  <script src="i18n.js"></script>
</head>
```

The module automatically initializes when the DOM is ready.

## Message Keys

Message keys in `messages.properties` follow this naming convention:

### General Structure
```
category.subcategory.element
```

### Examples
```properties
# Landing page
landing.create.title=Chair a Meeting
landing.create.button=Create New Room

# Chair view
chair.speakers.title=Manage speakers
chair.speakers.button.next=Next speaker

# Errors
error.room.notfound=Room not found: {0}
error.chair.unauthorized=Unauthorized chair access: {0}

# Enums
goal.MAKE_DECISIONS.name=Make Decisions
goal.MAKE_DECISIONS.description=Reach agreement or choose a course of action collaboratively.
```

### Parameter Substitution
Use `{0}`, `{1}`, etc. for positional parameters:
```properties
error.room.notfound=Room not found: {0}
poll.totalvotes=Total votes: {0}
```

## Adding New Translations

### 1. Add to messages.properties
```properties
my.new.message=My translated text
my.new.message.with.param=Hello {0}!
```

### 2. Use in Java
```java
String message = messageService.getMessage("my.new.message");
String withParam = messageService.getMessage("my.new.message.with.param", new Object[]{"World"});
```

### 3. Use in HTML
```html
<div data-i18n="my.new.message"></div>
<div data-i18n="my.new.message.with.param" data-i18n-params='["World"]'></div>
```

### 4. Use in JavaScript
```javascript
const message = i18n.t('my.new.message');
const withParam = i18n.t('my.new.message.with.param', ['World']);
```

## Adding New Languages

To add support for a new language (e.g., German):

1. Create `messages_de.properties` in `src/main/resources/`
2. Translate all keys from `messages.properties`
3. The system will automatically use the new translations when the Accept-Language header indicates German

Example `messages_de.properties`:
```properties
app.name=SPEEK.NOW
app.tagline=Meeting Manager
landing.create.title=Moderiere ein Meeting
landing.create.button=Neuen Raum erstellen
```

## Best Practices

1. **Always extract user-facing strings** - Any text shown to users should be in messages.properties
2. **Don't extract log messages** - Debug and error logs should remain in English in the code
3. **Use descriptive keys** - Keys should clearly indicate where the text is used
4. **Group related keys** - Use consistent prefixes for related messages
5. **Provide fallbacks** - Enum classes keep English defaults in case translations fail
6. **Test with data attributes** - Prefer declarative HTML translation where possible
7. **Keep parameters simple** - Use positional parameters for simple substitutions

## Current Status

### Extracted Strings
- ✅ Landing page (all user-facing text)
- ✅ Chair view (all user-facing text)
- ✅ Participant view (all user-facing text)
- ✅ Error messages
- ✅ Meeting goals
- ✅ Participation formats
- ✅ Decision rules
- ✅ Deliverables
- ✅ Poll-related text

### Not Extracted (By Design)
- ❌ Log messages (log.debug, log.error, etc.)
- ❌ Technical error messages in stack traces
- ❌ Developer-facing comments and documentation

## Testing

To test the internationalization:

1. **Check message loading:**
   ```javascript
   // In browser console
   console.log(i18n.getAllMessages());
   ```

2. **Test translation function:**
   ```javascript
   // In browser console
   i18n.t('chair.title');  // Should return "Chair View"
   ```

3. **Change browser language:**
   - Change your browser's preferred language
   - Reload the page
   - Verify that translations are applied

4. **Test API directly:**
   ```bash
   curl http://localhost:8080/api/i18n/messages \
     -H "Accept-Language: en"
   ```

## Future Enhancements

Potential improvements:
- Add language selector UI component
- Store user's language preference in localStorage
- Add more language translations
- Implement lazy loading for large translation files
- Add translation management UI for editors
