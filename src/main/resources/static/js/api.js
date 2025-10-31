// ChePlay API Module
// Centralized API calls to backend

export const api = {
  // ==================== User Management ====================
  
  async getUsers() {
    const response = await fetch('/api/recommendations/users');
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
    return await response.json();
  },

  // ==================== Friend Recommendations ====================
  
  recommendations: {
    async getClosest(user, k = 10) {
      const url = `/api/recommendations/closest?user=${encodeURIComponent(
        user
      )}&k=${k}`;
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    },

    async getSongs(user, k = 10, window = null, lambda = null) {
      const params = new URLSearchParams({
        user,
        k: k.toString()
      });
      if (window != null) params.set('window', window.toString());
      if (lambda != null) params.set('lambda', lambda.toString());
      
      const response = await fetch(
        `/api/recommendations/songs?${params.toString()}`
      );
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    },

    async debugUser(user, limit = 20) {
      const url = `/api/recommendations/debug?user=${encodeURIComponent(
        user
      )}&limit=${limit}`;
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    }
  },

  // ==================== Movie Recommendations ====================
  
  movies: {
    async recommend(algorithm, user, k = 10, options = {}) {
      const params = new URLSearchParams({
        user,
        k: k.toString()
      });
      
      // Add algorithm-specific parameters
      if (options.maxDepth) {
        params.set('maxDepth', options.maxDepth.toString());
      }
      if (options.genre) {
        params.set('genre', options.genre);
      }
      
      const response = await fetch(
        `/api/movies/recommendations/${algorithm}?${params.toString()}`
      );
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    },

    async recommendBFS(user, k = 10, maxDepth = null) {
      return this.recommend('bfs', user, k, { maxDepth });
    },

    async recommendDFS(user, k = 10, maxDepth = null) {
      return this.recommend('dfs', user, k, { maxDepth });
    },

    async recommendDiverse(user, k = 10) {
      return this.recommend('diverse', user, k);
    },

    async recommendSorted(user, k = 10) {
      return this.recommend('sorted', user, k);
    },

    async recommendByGenre(user, genre, k = 10) {
      return this.recommend('by-genre', user, k, { genre });
    },

    async debugWatched(user, limit = 20) {
      const url = `/api/movies/recommendations/debug?user=${
        encodeURIComponent(user)
      }&limit=${limit}`;
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    }
  },

  // ==================== Movie Marathon Planning ====================
  
  marathon: {
    async plan(user, totalMinutes, minRating, algorithm = 'dp') {
      const params = new URLSearchParams({
        user,
        totalMinutes: totalMinutes.toString(),
        minRating: minRating.toString(),
        algorithm
      });
      
      const response = await fetch(
        `/api/movies/marathon/quick?${params.toString()}`
      );
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    }
  },

  // ==================== Movie Trending ====================
  
  movieTrending: {
    async getGlobal(k = 10) {
      const response = await fetch(
        `/api/movies/trending/global?k=${k}`
      );
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    },

    async getByGenre(genre, k = 10) {
      const url = `/api/movies/trending/by-genre?genre=${
        encodeURIComponent(genre)
      }&k=${k}`;
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    },

    async getInfluential(k = 10) {
      const response = await fetch(
        `/api/movies/trending/influential?k=${k}`
      );
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    }
  },

  // ==================== Music Trending ====================
  
  trending: {
    async getGlobal(k = 10, platforms = []) {
      const params = new URLSearchParams({ k: k.toString() });
      if (platforms && platforms.length > 0) {
        params.set('platforms', platforms.join(','));
      }
      
      const response = await fetch(
        `/api/trending/global?${params.toString()}`
      );
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    }
  },

  // ==================== Playlist Generation ====================
  
  playlist: {
    async generate(user, size, uniqueArtist, minDuration, maxDuration) {
      const params = new URLSearchParams({
        user,
        size: size.toString(),
        uniqueArtist: uniqueArtist.toString()
      });
      
      if (minDuration != null) {
        params.set('minDuration', minDuration.toString());
      }
      if (maxDuration != null) {
        params.set('maxDuration', maxDuration.toString());
      }
      
      const response = await fetch(
        `/api/playlist/generate?${params.toString()}`
      );
      
      if (response.status === 404) {
        return null;
      }
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      return await response.json();
    }
  }
};

