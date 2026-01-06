/**
 * i18n.js - Internationalization module for SPEEK.NOW
 * Loads translations from the server and provides translation functions
 */

const i18n = (() => {
  let messages = {};
  let loaded = false;
  let loading = null;

  /**
   * Load messages from the server
   * @returns {Promise<void>}
   */
  async function loadMessages() {
    if (loading) return loading;
    if (loaded) return Promise.resolve();

    loading = fetch('/api/i18n/messages', {
      headers: {
        'Accept-Language': navigator.language || 'en'
      }
    })
      .then(response => response.json())
      .then(data => {
        messages = data;
        loaded = true;
        console.log('i18n messages loaded:', Object.keys(messages).length, 'keys');
      })
      .catch(error => {
        console.error('Failed to load i18n messages:', error);
        // Continue with empty messages - will fall back to keys
        loaded = true;
      })
      .finally(() => {
        loading = null;
      });

    return loading;
  }

  /**
   * Get a translated message
   * @param {string} key - The message key (e.g., 'chair.title')
   * @param {Object|Array} params - Optional parameters to substitute in the message
   * @returns {string} The translated message, or the key if not found
   */
  function t(key, params = null) {
    let message = messages[key] || key;

    // If params provided, substitute placeholders
    if (params) {
      if (Array.isArray(params)) {
        // Array of positional parameters: {0}, {1}, etc.
        params.forEach((param, index) => {
          message = message.replace(new RegExp(`\\{${index}\\}`, 'g'), param);
        });
      } else {
        // Object with named parameters: {name}, {value}, etc.
        Object.keys(params).forEach(key => {
          message = message.replace(new RegExp(`\\{${key}\\}`, 'g'), params[key]);
        });
      }
    }

    return message;
  }

  /**
   * Translate all elements with data-i18n attribute
   * Usage in HTML: <div data-i18n="chair.title"></div>
   * With parameters: <div data-i18n="error.room.notfound" data-i18n-params='["ROOM1"]'></div>
   */
  function translatePage() {
    document.querySelectorAll('[data-i18n]').forEach(element => {
      const key = element.getAttribute('data-i18n');
      const paramsAttr = element.getAttribute('data-i18n-params');
      let params = null;

      if (paramsAttr) {
        try {
          params = JSON.parse(paramsAttr);
        } catch (e) {
          console.warn('Invalid i18n params:', paramsAttr);
        }
      }

      element.textContent = t(key, params);
    });

    // Translate placeholders
    document.querySelectorAll('[data-i18n-placeholder]').forEach(element => {
      const key = element.getAttribute('data-i18n-placeholder');
      element.placeholder = t(key);
    });

    // Translate aria-labels
    document.querySelectorAll('[data-i18n-aria]').forEach(element => {
      const key = element.getAttribute('data-i18n-aria');
      element.setAttribute('aria-label', t(key));
    });

    // Translate titles
    document.querySelectorAll('[data-i18n-title]').forEach(element => {
      const key = element.getAttribute('data-i18n-title');
      element.title = t(key);
    });
  }

  /**
   * Initialize i18n and translate the page
   * @returns {Promise<void>}
   */
  async function init() {
    await loadMessages();
    translatePage();
  }

  /**
   * Check if messages are loaded
   * @returns {boolean}
   */
  function isLoaded() {
    return loaded;
  }

  /**
   * Get all messages
   * @returns {Object}
   */
  function getAllMessages() {
    return { ...messages };
  }

  // Public API
  return {
    init,
    loadMessages,
    t,
    translatePage,
    isLoaded,
    getAllMessages
  };
})();

// Auto-initialize when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => i18n.init());
} else {
  i18n.init();
}
