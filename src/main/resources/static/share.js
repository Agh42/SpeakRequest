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
   * Create SVG icon element
   */
  function createIcon(type) {
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.setAttribute('width', '20');
    svg.setAttribute('height', '20');
    svg.setAttribute('viewBox', '0 0 24 24');
    svg.setAttribute('fill', 'currentColor');
    svg.style.cssText = 'margin-right: 8px; vertical-align: middle;';
    
    let path = '';
    switch(type) {
      case 'copy':
        path = 'M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z';
        break;
      case 'email':
        path = 'M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z';
        break;
      case 'whatsapp':
        path = 'M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413z';
        break;
      case 'twitter':
        path = 'M23.953 4.57a10 10 0 01-2.825.775 4.958 4.958 0 002.163-2.723c-.951.555-2.005.959-3.127 1.184a4.92 4.92 0 00-8.384 4.482C7.69 8.095 4.067 6.13 1.64 3.162a4.822 4.822 0 00-.666 2.475c0 1.71.87 3.213 2.188 4.096a4.904 4.904 0 01-2.228-.616v.06a4.923 4.923 0 003.946 4.827 4.996 4.996 0 01-2.212.085 4.936 4.936 0 004.604 3.417 9.867 9.867 0 01-6.102 2.105c-.39 0-.779-.023-1.17-.067a13.995 13.995 0 007.557 2.209c9.053 0 13.998-7.496 13.998-13.985 0-.21 0-.42-.015-.63A9.935 9.935 0 0024 4.59z';
        break;
      case 'sms':
        path = 'M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM9 11H7V9h2v2zm4 0h-2V9h2v2zm4 0h-2V9h2v2z';
        break;
      case 'close':
        path = 'M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z';
        break;
      default:
        path = 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z';
    }
    
    const pathElement = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    pathElement.setAttribute('d', path);
    svg.appendChild(pathElement);
    
    return svg;
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
    copyBtn.appendChild(createIcon('copy'));
    copyBtn.appendChild(document.createTextNode('Copy Link'));
    copyBtn.setAttribute('aria-label', 'Copy link to clipboard');
    copyBtn.dataset.action = 'copy';
    styleButton(copyBtn);

    // Email button
    const emailBtn = document.createElement('button');
    emailBtn.className = 'share-option-btn';
    emailBtn.appendChild(createIcon('email'));
    emailBtn.appendChild(document.createTextNode('Share via Email'));
    emailBtn.setAttribute('aria-label', 'Share via email');
    emailBtn.dataset.action = 'email';
    styleButton(emailBtn);

    // WhatsApp button
    const whatsappBtn = document.createElement('button');
    whatsappBtn.className = 'share-option-btn';
    whatsappBtn.appendChild(createIcon('whatsapp'));
    whatsappBtn.appendChild(document.createTextNode('Share via WhatsApp'));
    whatsappBtn.setAttribute('aria-label', 'Share via WhatsApp');
    whatsappBtn.dataset.action = 'whatsapp';
    styleButton(whatsappBtn);

    // Twitter button (replacing SMS)
    const twitterBtn = document.createElement('button');
    twitterBtn.className = 'share-option-btn';
    twitterBtn.appendChild(createIcon('twitter'));
    twitterBtn.appendChild(document.createTextNode('Share via Twitter'));
    twitterBtn.setAttribute('aria-label', 'Share via Twitter');
    twitterBtn.dataset.action = 'twitter';
    styleButton(twitterBtn);

    // Close button
    const closeBtn = document.createElement('button');
    closeBtn.className = 'share-close-btn';
    closeBtn.appendChild(createIcon('close'));
    closeBtn.appendChild(document.createTextNode('Close'));
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
    buttonsContainer.appendChild(twitterBtn);
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
      } else if (action === 'twitter') {
        shareViaTwitter(config);
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
   * Share via Twitter
   */
  function shareViaTwitter(config) {
    const text = encodeURIComponent(config.text);
    const url = encodeURIComponent(config.shareUrl);
    const twitterUrl = `https://twitter.com/intent/tweet?text=${text}&url=${url}`;
    window.open(twitterUrl, '_blank', 'noopener,noreferrer');
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
