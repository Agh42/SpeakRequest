/**
 * Metadata Loader
 * Fetches and caches enum metadata from the backend with localStorage caching
 */

const MetadataLoader = {
  CACHE_PREFIX: 'speek_metadata_',
  ENDPOINTS: {
    meetingGoal: '/api/metadata/meeting-goals',
    participationFormat: '/api/metadata/participation-formats',
    decisionRule: '/api/metadata/decision-rules',
    deliverable: '/api/metadata/deliverables'
  },

  /**
   * Fetch metadata for a specific type with caching
   * @param {string} type - One of: meetingGoal, participationFormat, decisionRule, deliverable
   * @returns {Promise<Object>} - Object mapping enum values to {displayName, description}
   */
  async fetch(type) {
    const endpoint = this.ENDPOINTS[type];
    if (!endpoint) {
      console.error(`Unknown metadata type: ${type}`);
      return {};
    }

    const cacheKey = this.CACHE_PREFIX + type;
    
    try {
      // Try to load from cache first
      const cached = localStorage.getItem(cacheKey);
      if (cached) {
        const parsed = JSON.parse(cached);
        // Check if cache is valid (version matches)
        if (parsed.version === '1.0') {
          return this._transformToMap(parsed.data);
        }
      }

      // Fetch from server
      const response = await fetch(endpoint);
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const result = await response.json();
      
      // Cache the result
      localStorage.setItem(cacheKey, JSON.stringify(result));
      
      return this._transformToMap(result.data);
    } catch (error) {
      console.error(`Failed to load metadata for ${type}:`, error);
      // Return fallback value
      return this._getFallback();
    }
  },

  /**
   * Transform array of {value, displayName, description} to map
   * @private
   */
  _transformToMap(dataArray) {
    const map = {};
    for (const item of dataArray) {
      map[item.value] = {
        displayName: item.displayName,
        description: item.description
      };
    }
    return map;
  },

  /**
   * Get fallback value when fetch fails
   * @private
   */
  _getFallback() {
    return {
      'NOT_FOUND': {
        displayName: 'NOT FOUND',
        description: 'NOT FOUND'
      }
    };
  },

  /**
   * Load all metadata types at once
   * @returns {Promise<Object>} - Object with all metadata types
   */
  async loadAll() {
    const [meetingGoal, participationFormat, decisionRule, deliverable] = await Promise.all([
      this.fetch('meetingGoal'),
      this.fetch('participationFormat'),
      this.fetch('decisionRule'),
      this.fetch('deliverable')
    ]);

    return {
      meetingGoal,
      participationFormat,
      decisionRule,
      deliverable
    };
  },

  /**
   * Clear all cached metadata (useful for testing or forced refresh)
   */
  clearCache() {
    for (const type in this.ENDPOINTS) {
      localStorage.removeItem(this.CACHE_PREFIX + type);
    }
  }
};
