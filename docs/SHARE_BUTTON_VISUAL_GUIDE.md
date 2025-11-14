# Share Button - Visual Implementation Guide

## Button Placement

### Chair View (`chair.html`)
```
┌─ Room Information ──────────────────────┐
│                                          │
│  Room Code: ABCD                         │
│                                          │
│  [📋 Copy Code]  [🔗 Copy Participant Link]│
│  [🔗 Share Participation Link]           │  ← NEW BUTTON
│                                          │
└──────────────────────────────────────────┘
```

### Participant View (`participant.html`)
```
┌─ Room Information ──────────────────────┐
│                                          │
│  Room Code: ABCD                         │
│                                          │
│  [📋 Copy Room Code]                     │
│  [🔗 Share Participation Link]           │  ← NEW BUTTON
│                                          │
└──────────────────────────────────────────┘
```

## User Flow

### Web Share API Available (iOS, Android, Modern Desktop)
```
User clicks                Native share
"Share" button    →       sheet appears     →    User selects
                          (System UI)             sharing method
                                ↓
                          Shares to:
                          - Messages
                          - WhatsApp
                          - Email
                          - Copy Link
                          - etc.
```

### Web Share API Not Available (Desktop Firefox, Old Browsers)
```
User clicks                Custom modal
"Share" button    →       appears           →    User selects option
                                ↓
                          Options:
                          [📋 Copy Link]
                          [📧 Share via Email]
                          [💬 Share via WhatsApp]
                          [📱 Share via SMS]
                          [✖ Close]
```

## Shared URL Format

```
https://[domain]/room/[4-char-code]

Examples:
- https://speek.now/room/ABCD
- https://localhost:8080/room/TEST
- https://example.com/room/XY12
```

## Fallback Modal Appearance

```
╔═══════════════════════════════════════╗
║  Share Meeting Link                   ║
║                                       ║
║  ┌─────────────────────────────────┐ ║
║  │  📋 Copy Link                   │ ║
║  └─────────────────────────────────┘ ║
║  ┌─────────────────────────────────┐ ║
║  │  📧 Share via Email             │ ║
║  └─────────────────────────────────┘ ║
║  ┌─────────────────────────────────┐ ║
║  │  💬 Share via WhatsApp          │ ║
║  └─────────────────────────────────┘ ║
║  ┌─────────────────────────────────┐ ║
║  │  📱 Share via SMS               │ ║
║  └─────────────────────────────────┘ ║
║  ┌─────────────────────────────────┐ ║
║  │  ✖ Close                        │ ║
║  └─────────────────────────────────┘ ║
║                                       ║
║  ✓ Link copied to clipboard!         ║  ← Status message
╚═══════════════════════════════════════╝
```

## Share Content

### Title
```
Join Meeting on SPEEK.NOW
```

### Text
```
Join the meeting with room code [CODE]:
```

### URL
```
https://[domain]/room/[CODE]
```

## Social Media Preview

When the URL is shared on platforms like WhatsApp, Slack, or Twitter:

```
┌───────────────────────────────────┐
│ [Preview Image]                   │
│                                   │
│ SPEEK.NOW - Join Meeting          │
│ Join this meeting on SPEEK.NOW    │
│ to request speaking time and      │
│ participate in polls.             │
│                                   │
│ https://speek.now/room/ABCD       │
└───────────────────────────────────┘
```

## Code Structure

```
share.js
├── initShareButton()          # Main initialization function
│   ├── validateUrl()          # Validate absolute URLs
│   ├── handleShare()          # Handle share action
│   │   ├── tryWebShare()      # Try Web Share API
│   │   └── showFallbackModal() # Show fallback if needed
│   └── createFallbackModal()  # Create modal DOM
│       ├── copyToClipboard()  # Copy link option
│       ├── shareViaEmail()    # Email option
│       ├── shareViaWhatsApp() # WhatsApp option
│       └── shareViaSMS()      # SMS option
└── ShareButton.init()         # Public API
```

## Integration Example

```javascript
// In chair.html or participant.html
const shareBtn = document.getElementById('btnShareParticipantUrl');
ShareButton.init(shareBtn, {
  shareUrl: `${window.location.origin}/room/${roomCode}`,
  title: 'Join Meeting on SPEEK.NOW',
  text: `Join the meeting with room code ${roomCode}:`
});
```

## Feature Detection

```javascript
// Detect Web Share API
if (navigator.share) {
  // Use native sharing
  await navigator.share({ title, text, url });
} else {
  // Show fallback modal
  showFallbackModal();
}
```

## Keyboard Navigation

```
Tab       → Move between buttons
Enter     → Activate button
Escape    → Close modal
Space     → Activate button
```

## Accessibility

```html
<!-- Button has proper label -->
<button 
  id="btnShareParticipantUrl" 
  aria-label="Share participation link"
>
  🔗 Share Participation Link
</button>

<!-- Modal has proper attributes -->
<div 
  role="dialog" 
  aria-modal="true" 
  aria-labelledby="shareModalTitle"
  aria-hidden="true"
>
  <h3 id="shareModalTitle">Share Meeting Link</h3>
  ...
</div>
```

## Error Handling

```
User cancels Web Share
  → Silent (no error message)

Clipboard API fails
  → Fallback to prompt()

User clicks backdrop
  → Close modal

User presses Escape
  → Close modal
```

## Browser Compatibility

```
✅ iOS 17+ Safari          → Web Share API
✅ Android 12+ Chrome      → Web Share API
✅ macOS Chrome/Edge       → Web Share API
✅ macOS Safari            → Web Share API
✅ Windows Chrome/Edge     → Web Share API
✅ Desktop Firefox         → Fallback Modal
✅ Older browsers          → Fallback Modal
⚠️  HTTP sites             → Fallback (clipboard may fail)
```

## Testing URLs

```
Development:
  http://localhost:8080/chair.html?room=TEST
  http://localhost:8080/participant.html?room=TEST
  http://localhost:8080/test-share.html

Production:
  https://speek.now/chair.html?room=ABCD
  https://speek.now/participant.html?room=ABCD
  https://speek.now/test-share.html
```

## File Sizes

```
share.js            : 423 lines (12.4 KB)
test-share.html     : 146 lines (4.3 KB)
docs/SHARE_BUTTON.md: 161 lines (6.6 KB)

Total new code: 730 lines (~23 KB)
```
