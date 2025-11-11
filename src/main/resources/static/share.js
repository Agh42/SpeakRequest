/**
 * Share Module - Cross-platform URL sharing with Web Share API fallback
 * 
 * Provides a reusable share functionality that:
 * - Uses Web Share API when available (mobile/desktop system share)
 * - Falls back to Copy/Email/WhatsApp/SMS options when not available
 * - Handles all user activation requirements
 * - Provides accessible UI components
 */

(function(window) {
  'use strict';

  /**
   * Initialize a share button with cross-platform sharing capabilities
   * @param {HTMLElement} buttonElement - The button element to attach sharing functionality to
   * @param {Object} options - Configuration options
   * @param {string} options.shareUrl - The absolute URL to share (required)
   * @param {string} [options.title] - Share title
   * @param {string} [options.text] - Share text/description
   * @returns {Object} API object with update and destroy methods
   */
  function initShareButton(buttonElement, options) {
    if (!buttonElement) {
      console.error('Share button element is required');
      return null;
    }

    // Validate and normalize shareUrl
    const shareUrl = validateUrl(options.shareUrl);
    if (!shareUrl) {
      console.error('Valid absolute shareUrl is required');
      return null;
    }

    const config = {
      shareUrl: shareUrl,
      title: options.title || 'Join Meeting',
      text: options.text || 'Join this meeting on SPEEK.NOW:'
    };

    // Create fallback modal
    const modal = createFallbackModal();
    document.body.appendChild(modal);

    // Attach click handler
    const clickHandler = async (event) => {
      event.preventDefault();
      await handleShare(config, modal);
    };

    buttonElement.addEventListener('click', clickHandler);

    // Ensure button has proper accessibility attributes
    if (!buttonElement.getAttribute('aria-label')) {
      buttonElement.setAttribute('aria-label', 'Share meeting link');
    }

    // API object
    return {
      update: (newOptions) => {
        if (newOptions.shareUrl) {
          const newUrl = validateUrl(newOptions.shareUrl);
          if (newUrl) config.shareUrl = newUrl;
        }
        if (newOptions.title) config.title = newOptions.title;
        if (newOptions.text) config.text = newOptions.text;
      },
      destroy: () => {
        buttonElement.removeEventListener('click', clickHandler);
        if (modal && modal.parentNode) {
          modal.parentNode.removeChild(modal);
        }
      }
    };
  }

  /**
   * Validate and ensure URL is absolute
   */
  function validateUrl(url) {
    if (!url) return null;
    
    try {
      const parsed = new URL(url, window.location.origin);
      // Ensure it's an absolute URL
      if (parsed.protocol === 'http:' || parsed.protocol === 'https:') {
        return parsed.href;
      }
    } catch (e) {
      console.error('Invalid URL:', url, e);
    }
    
    return null;
  }

  /**
   * Handle the share action - try Web Share API first, then fallback
   */
  async function handleShare(config, modal) {
    const used = await tryWebShare(config);
    if (!used) {
      showFallbackModal(modal, config);
    }
  }

  /**
   * Try to use the Web Share API
   * Returns true if successful, false if unavailable or user cancelled
   */
  async function tryWebShare(config) {
    if (!navigator.share || typeof navigator.share !== 'function') {
      return false;
    }

    try {
      await navigator.share({
        title: config.title,
        text: config.text,
        url: config.shareUrl
      });
      return true;
    } catch (error) {
      // User cancelled or error occurred - fall through to fallback
      if (error.name !== 'AbortError') {
        console.log('Web Share API error:', error);
      }
      return false;
    }
  }

  /**
   * Create the fallback modal element
   */
  function createFallbackModal() {
    const modal = document.createElement('div');
    modal.id = 'shareModal';
    modal.className = 'share-modal';
    modal.style.cssText = `
      display: none;
      position: fixed;
      inset: 0;
      background: rgba(15, 23, 42, 0.95);
      z-index: 10000;
      align-items: center;
      justify-content: center;
    `;
    modal.setAttribute('role', 'dialog');
    modal.setAttribute('aria-modal', 'true');
    modal.setAttribute('aria-labelledby', 'shareModalTitle');
    modal.setAttribute('aria-hidden', 'true');

    const panel = document.createElement('div');
    panel.className = 'share-modal-panel';
    panel.style.cssText = `
      background: linear-gradient(180deg, rgba(255,255,255,.1), rgba(255,255,255,.05));
      border: 1px solid rgba(255,255,255,.2);
      border-radius: 14px;
      padding: 24px;
      max-width: 480px;
      width: 90%;
      color: #e5e7eb;
      box-shadow: 0 8px 30px rgba(0,0,0,0.5);
    `;

    const title = document.createElement('h3');
    title.id = 'shareModalTitle';
    title.textContent = 'Share Meeting Link';
    title.style.cssText = 'margin: 0 0 16px 0; font-size: 1.5rem;';

    const buttonsContainer = document.createElement('div');
    buttonsContainer.style.cssText = 'display: flex; flex-direction: column; gap: 12px;';

    // Copy button
    const copyBtn = document.createElement('button');
    copyBtn.className = 'share-option-btn';
    copyBtn.textContent = '📋 Copy Link';
    copyBtn.setAttribute('aria-label', 'Copy link to clipboard');
    copyBtn.dataset.action = 'copy';
    styleButton(copyBtn);

    // Email button
    const emailBtn = document.createElement('button');
    emailBtn.className = 'share-option-btn';
    emailBtn.textContent = '📧 Share via Email';
    emailBtn.setAttribute('aria-label', 'Share via email');
    emailBtn.dataset.action = 'email';
    styleButton(emailBtn);

    // WhatsApp button
    const whatsappBtn = document.createElement('button');
    whatsappBtn.className = 'share-option-btn';
    whatsappBtn.textContent = '💬 Share via WhatsApp';
    whatsappBtn.setAttribute('aria-label', 'Share via WhatsApp');
    whatsappBtn.dataset.action = 'whatsapp';
    styleButton(whatsappBtn);

    // SMS button (optional, best-effort)
    const smsBtn = document.createElement('button');
    smsBtn.className = 'share-option-btn';
    smsBtn.textContent = '📱 Share via SMS';
    smsBtn.setAttribute('aria-label', 'Share via SMS');
    smsBtn.dataset.action = 'sms';
    styleButton(smsBtn);

    // Close button
    const closeBtn = document.createElement('button');
    closeBtn.className = 'share-close-btn';
    closeBtn.textContent = '✖ Close';
    closeBtn.setAttribute('aria-label', 'Close share dialog');
    closeBtn.dataset.action = 'close';
    closeBtn.style.cssText = `
      margin-top: 8px;
      padding: 10px 20px;
      background: rgba(255,255,255,.1);
      border: 1px solid rgba(255,255,255,.2);
      border-radius: 8px;
      color: #e5e7eb;
      cursor: pointer;
      font-size: 1rem;
      transition: all 0.2s;
    `;

    // Status message
    const statusMsg = document.createElement('div');
    statusMsg.id = 'shareStatusMsg';
    statusMsg.className = 'share-status-msg';
    statusMsg.style.cssText = `
      margin-top: 16px;
      padding: 8px;
      border-radius: 4px;
      text-align: center;
      font-size: 0.9rem;
      display: none;
    `;

    buttonsContainer.appendChild(copyBtn);
    buttonsContainer.appendChild(emailBtn);
    buttonsContainer.appendChild(whatsappBtn);
    buttonsContainer.appendChild(smsBtn);
    buttonsContainer.appendChild(closeBtn);

    panel.appendChild(title);
    panel.appendChild(buttonsContainer);
    panel.appendChild(statusMsg);
    modal.appendChild(panel);

    // Event delegation for all buttons
    panel.addEventListener('click', (e) => {
      const btn = e.target.closest('[data-action]');
      if (!btn) return;

      const action = btn.dataset.action;
      const config = modal._shareConfig;

      if (action === 'close') {
        hideFallbackModal(modal);
      } else if (action === 'copy') {
        copyToClipboard(config.shareUrl, statusMsg);
      } else if (action === 'email') {
        shareViaEmail(config);
      } else if (action === 'whatsapp') {
        shareViaWhatsApp(config);
      } else if (action === 'sms') {
        shareViaSMS(config);
      }
    });

    // Close on backdrop click
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        hideFallbackModal(modal);
      }
    });

    // Close on Escape key
    modal.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        hideFallbackModal(modal);
      }
    });

    return modal;
  }

  /**
   * Style a button element
   */
  function styleButton(btn) {
    btn.style.cssText = `
      padding: 12px 20px;
      background: linear-gradient(180deg, rgba(96,165,250,.25), rgba(96,165,250,.15));
      border: 1px solid rgba(96,165,250,.4);
      border-radius: 8px;
      color: #e5e7eb;
      cursor: pointer;
      font-size: 1rem;
      text-align: left;
      transition: all 0.2s;
    `;
    
    btn.addEventListener('mouseenter', () => {
      btn.style.background = 'linear-gradient(180deg, rgba(96,165,250,.35), rgba(96,165,250,.25))';
      btn.style.transform = 'translateX(4px)';
    });
    
    btn.addEventListener('mouseleave', () => {
      btn.style.background = 'linear-gradient(180deg, rgba(96,165,250,.25), rgba(96,165,250,.15))';
      btn.style.transform = 'translateX(0)';
    });
  }

  /**
   * Show the fallback modal
   */
  function showFallbackModal(modal, config) {
    modal._shareConfig = config;
    modal.style.display = 'flex';
    modal.setAttribute('aria-hidden', 'false');
    
    // Focus management
    const firstBtn = modal.querySelector('button');
    if (firstBtn) {
      setTimeout(() => firstBtn.focus(), 50);
    }

    // Hide status message
    const statusMsg = modal.querySelector('#shareStatusMsg');
    if (statusMsg) {
      statusMsg.style.display = 'none';
    }
  }

  /**
   * Hide the fallback modal
   */
  function hideFallbackModal(modal) {
    modal.style.display = 'none';
    modal.setAttribute('aria-hidden', 'true');
  }

  /**
   * Copy URL to clipboard
   */
  async function copyToClipboard(url, statusMsg) {
    try {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        await navigator.clipboard.writeText(url);
        showStatusMessage(statusMsg, '✓ Link copied to clipboard!', 'success');
      } else {
        // Fallback to prompt
        const result = window.prompt('Copy this link:', url);
        if (result !== null) {
          showStatusMessage(statusMsg, '✓ Link ready to copy', 'success');
        }
      }
    } catch (error) {
      // Fallback to prompt on error
      window.prompt('Copy this link:', url);
    }
  }

  /**
   * Share via Email
   */
  function shareViaEmail(config) {
    const subject = encodeURIComponent(config.title);
    const body = encodeURIComponent(`${config.text}\n\n${config.shareUrl}`);
    const mailtoUrl = `mailto:?subject=${subject}&body=${body}`;
    window.open(mailtoUrl, '_blank', 'noopener');
  }

  /**
   * Share via WhatsApp
   */
  function shareViaWhatsApp(config) {
    const text = encodeURIComponent(`${config.text} ${config.shareUrl}`);
    const whatsappUrl = `https://wa.me/?text=${text}`;
    window.open(whatsappUrl, '_blank', 'noopener,noreferrer');
  }

  /**
   * Share via SMS (best-effort, platform-dependent)
   */
  function shareViaSMS(config) {
    const text = encodeURIComponent(`${config.text} ${config.shareUrl}`);
    // iOS uses '&', Android/others use '?'
    const smsUrl = `sms:?&body=${text}`;
    window.location.href = smsUrl;
  }

  /**
   * Show status message in modal
   */
  function showStatusMessage(statusMsg, message, type) {
    if (!statusMsg) return;

    statusMsg.textContent = message;
    statusMsg.style.display = 'block';
    
    if (type === 'success') {
      statusMsg.style.background = 'rgba(34, 197, 94, 0.2)';
      statusMsg.style.border = '1px solid rgba(34, 197, 94, 0.4)';
      statusMsg.style.color = '#86efac';
    } else {
      statusMsg.style.background = 'rgba(239, 68, 68, 0.2)';
      statusMsg.style.border = '1px solid rgba(239, 68, 68, 0.4)';
      statusMsg.style.color = '#fca5a5';
    }

    // Auto-hide after 3 seconds
    setTimeout(() => {
      statusMsg.style.display = 'none';
    }, 3000);
  }

  // Export to global scope
  window.ShareButton = {
    init: initShareButton,
    version: '1.0.0'
  };

})(window);
