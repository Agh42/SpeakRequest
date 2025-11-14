# Share Button Implementation

## Overview

The Share Button feature provides a cross-platform URL sharing mechanism for the chair and participant views. It allows users to easily share the meeting participation URL using the Web Share API on supported platforms, with fallback options for browsers that don't support native sharing.

## Features

### Primary Features
- **Web Share API Integration**: Uses the native system share sheet on mobile devices and modern desktop browsers
- **Cross-Platform Fallbacks**: Provides multiple fallback options when Web Share API is unavailable:
  - Copy link to clipboard
  - Share via Email
  - Share via WhatsApp
  - Share via SMS (best-effort, platform-dependent)
- **Accessible UI**: Fully keyboard navigable with proper ARIA labels
- **Zero Dependencies**: Implemented in vanilla JavaScript with no external dependencies

### Browser Support

| Platform | Browser | Share Method | Notes |
|----------|---------|--------------|-------|
| iOS 17+ | Safari | Web Share | Native system share sheet |
| Android 12+ | Chrome | Web Share | Native system share sheet |
| macOS | Chrome/Edge | Web Share | Native system share sheet |
| macOS | Safari | Web Share | Native system share sheet |
| Windows | Chrome/Edge | Web Share | Native system share sheet |
| Desktop | Firefox | Fallback | Copy/Email/WhatsApp/SMS modal |
| Any | HTTP | Fallback | Clipboard may fail, uses prompt |

## Integration

### Chair View

The share button is located in the "Room Information" section of the chair view (`chair.html`). It appears below the existing "Copy Code" and "Copy Participant Link" buttons.

**Button ID**: `btnShareParticipantUrl`

### Participant View

The share button is located in the "Room Information" section of the participant view (`participant.html`). It appears below the "Copy Room Code" button.

**Button ID**: `btnShareParticipantUrl`

## Technical Implementation

### Files

- **`share.js`**: Core share module with Web Share API implementation and fallback modal
- **`chair.html`**: Chair view with integrated share button
- **`participant.html`**: Participant view with integrated share button

### API Usage

The share button is initialized with the following configuration:

```javascript
ShareButton.init(buttonElement, {
  shareUrl: `${window.location.origin}/room/${roomCode}`,
  title: 'Join Meeting on SPEEK.NOW',
  text: `Join the meeting with room code ${roomCode}:`
});
```

### Share URL Format

The shared URL follows the pattern: `https://[domain]/room/[roomCode]`

Example: `https://speek.now/room/A1B2`

## Security & Privacy

- **HTTPS Required**: Web Share API and Clipboard API require HTTPS
- **Top-Level Context**: Web Share API only works in top-level browsing contexts (not in cross-origin iframes)
- **User Activation**: All share actions are triggered by user clicks (user activation requirement)
- **No Tracking**: No analytics or tracking is performed on share actions

## Social Media Preview

The following pages include Open Graph and Twitter Card meta tags for proper preview generation when shared on social media:

- **Landing Page** (`landing.html`)
- **Chair View** (`chair.html`)
- **Participant View** (`participant.html`)

### Meta Tags

```html
<!-- Open Graph / Facebook -->
<meta property="og:type" content="website"/>
<meta property="og:url" content="[page-url]"/>
<meta property="og:title" content="[title]"/>
<meta property="og:description" content="[description]"/>
<meta property="og:image" content="[absolute-image-url]"/>

<!-- Twitter -->
<meta name="twitter:card" content="summary_large_image"/>
<meta name="twitter:url" content="[page-url]"/>
<meta name="twitter:title" content="[title]"/>
<meta name="twitter:description" content="[description]"/>
<meta name="twitter:image" content="[absolute-image-url]"/>
```

## Limitations

1. **SMS Sharing**: SMS URL scheme support varies by platform. It works best on iOS and Android, but may not work consistently on desktop browsers.

2. **Clipboard API**: May not work over HTTP (insecure context). Falls back to `prompt()` dialog.

3. **Web Share API Availability**: Not available in all browsers. Firefox on desktop doesn't support it yet.

4. **Image Preview**: Social media preview images require absolute URLs and the page must be publicly accessible (no authentication walls).

## Testing

### Manual Testing Checklist

- [ ] Click share button on Android Chrome - should open system share sheet
- [ ] Click share button on iOS Safari - should open native share sheet
- [ ] Click share button on desktop Chrome/Edge - should open system share sheet
- [ ] Click share button on desktop Firefox - should show fallback modal
- [ ] Test "Copy Link" - should copy to clipboard or show prompt
- [ ] Test "Share via Email" - should open email client with pre-filled content
- [ ] Test "Share via WhatsApp" - should open WhatsApp with pre-filled message
- [ ] Test "Share via SMS" - should open SMS app on mobile
- [ ] Test keyboard navigation - all buttons should be accessible via Tab key
- [ ] Test Escape key - should close modal
- [ ] Test backdrop click - should close modal
- [ ] Share URL on WhatsApp/Slack/Twitter - should show proper preview card

### Error Handling

- User cancellation of Web Share API is silently ignored (no error message)
- Clipboard API failures fall back to `prompt()` dialog
- All share actions are non-blocking and don't throw errors to the console

## Future Enhancements

Potential improvements for future versions:

1. **QR Code Generation**: Generate a QR code for the participation URL
2. **Custom Share Text**: Allow room chairs to customize the share message
3. **Share Analytics**: Optional tracking of share button usage (with user consent)
4. **Deep Linking**: Support for app-specific deep links (if native mobile apps are developed)
5. **Shortened URLs**: Integration with URL shortener for cleaner share messages

## Support

For issues or questions related to the share functionality:

1. Check browser console for any error messages
2. Verify HTTPS is being used (required for Web Share and Clipboard APIs)
3. Confirm the page is loaded in a top-level browsing context
4. Test with a different browser to isolate browser-specific issues

## References

- [Web Share API Specification](https://www.w3.org/TR/web-share/)
- [MDN Web Share API Documentation](https://developer.mozilla.org/en-US/docs/Web/API/Navigator/share)
- [Open Graph Protocol](https://ogp.me/)
- [Twitter Cards Documentation](https://developer.twitter.com/en/docs/twitter-for-websites/cards/overview/abouts-cards)
