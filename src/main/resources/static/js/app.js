// ChePlay Main Application
// Entry point and initialization

import { api } from './api.js';
import { UI } from './ui.js';
import { Charts } from './charts.js';
import { Tabs } from './tabs.js';

// ==================== State Management ====================

const state = {
  users: [],
  currentUser: null,
  lastResults: {
    friends: null,
    songs: null,
    movies: null,
    marathon: null,
    trending: null
  }
};

// ==================== Initialization ====================

async function init() {
  try {
    // Initialize tabs
    Tabs.init();
    
    // Load users
    await loadUsers();
    
    // Load initial trending data
    await loadTrending();
    
    // Setup event listeners
    setupEventListeners();
    
    // Show success message
    UI.showToast('Aplicación cargada correctamente', 'success', 2000);
  } catch (error) {
    console.error('Error initializing app:', error);
    UI.showToast('Error al inicializar la aplicación', 'error');
  }
}

// ==================== User Management ====================

async function loadUsers() {
  try {
    const users = await api.getUsers();
    const userList = Array.isArray(users) ? users : (users.value || users);
    state.users = userList;
    
    // Populate all user selects
    const selectIds = [
      'friendUser',
      'songUser',
      'playlistUser',
      'movieUser',
      'marathonUser',
      'analyticsUser'
    ];
    
    selectIds.forEach(selectId => {
      UI.populateSelect(selectId, userList, {
        valueKey: 'id',
        labelKey: 'label'
      });
    });
    
    // Update current user display
    if (userList.length > 0) {
      const firstUser = userList[0];
      const userName = firstUser.label || firstUser.name || firstUser.id;
      state.currentUser = firstUser.id || firstUser;
      document.getElementById('currentUser').textContent = userName;
    }
    
    // Update stats
    document.getElementById('statsUsers').textContent = userList.length;
  } catch (error) {
    console.error('Error loading users:', error);
    UI.showToast('Error al cargar usuarios', 'error');
  }
}

// ==================== Friend Recommendations ====================

async function handleFriendRecommendations() {
  const user = document.getElementById('friendUser').value;
  const k = parseInt(document.getElementById('friendK').value, 10) || 5;
  
  if (!user) {
    UI.showToast('Seleccione un usuario', 'warning');
    return;
  }
  
  try {
    const friendResultsElement = document.getElementById('friendResults');
    friendResultsElement.classList.remove('hidden');
    
    // Show loading spinner inside the container
    friendResultsElement.innerHTML = `
      <div class="flex justify-center items-center py-8">
        <div class="loading-spinner"></div>
      </div>
    `;
    
    const data = await api.recommendations.getClosest(user, k);
    state.lastResults.friends = data;
    
    // Clear loading and render results
    friendResultsElement.innerHTML = `
      <div>
        <h3 class="text-sm font-semibold text-gray-700 mb-2">
          Conexiones Directas
        </h3>
        <div id="friendDirect" class="space-y-2"></div>
      </div>
      <div>
        <h3 class="text-sm font-semibold text-gray-700 mb-2">
          Por Camino Más Corto
        </h3>
        <div id="friendShortest" class="space-y-2"></div>
      </div>
      <div>
        <h3 class="text-sm font-semibold text-gray-700 mb-2">
          Amigo Más Cercano
        </h3>
        <div id="friendClosest" class="space-y-2"></div>
      </div>
    `;
    
    // Render results
    renderFriendResults(data);
    
    // Create network chart
    Charts.createFriendNetworkChart('friendNetworkChartContainer', data);
    
    UI.showToast('Recomendaciones obtenidas', 'success', 2000);
  } catch (error) {
    console.error('Error fetching friend recommendations:', error);
    const friendResultsElement = document.getElementById('friendResults');
    friendResultsElement.innerHTML = 
      UI.createEmptyState('Error al cargar recomendaciones', '❌');
    UI.showToast('Error al obtener recomendaciones', 'error');
  }
}

function renderFriendResults(data) {
  const sharedSongs = data.sharedSongs || {};
  
  // Direct recommendations
  const directHtml = UI.createList(
    data.directRecommendations || [],
    (item) => {
      const friendId = item.id;
      const sharedSongsList = sharedSongs[friendId] || [];
      return UI.createFriendCard(item, sharedSongsList, 'weight');
    }
  );
  UI.setElementContent('friendDirect', directHtml);
  
  // Shortest path recommendations
  const shortestHtml = UI.createList(
    data.shortestPathRecommendations || [],
    (item) => {
      const friendId = item.id;
      const sharedSongsList = sharedSongs[friendId] || [];
      return UI.createFriendCard(item, sharedSongsList, 'distance');
    }
  );
  UI.setElementContent('friendShortest', shortestHtml);
  
  // Closest friend
  const closest = data.closest;
  if (closest) {
    const closestId = closest.id;
    const closestSharedSongs = sharedSongs[closestId] || [];
    const closestHtml = UI.createFriendCard(closest, closestSharedSongs, 'distance', true);
    UI.setElementContent('friendClosest', closestHtml);
  } else {
    UI.setElementContent(
      'friendClosest',
      UI.createEmptyState('No hay amigos cercanos', '🤷')
    );
  }
}

// ==================== Song Recommendations ====================

async function handleSongRecommendations() {
  const user = document.getElementById('songUser').value;
  const k = parseInt(document.getElementById('songK').value, 10) || 10;
  
  if (!user) {
    UI.showToast('Seleccione un usuario', 'warning');
    return;
  }
  
  try {
    UI.showLoading('songResults');
    
    const songs = await api.recommendations.getSongs(user, k);
    state.lastResults.songs = songs;
    
    const songsHtml = UI.createList(songs, (song) => 
      UI.createResultCard(song, 'song')
    );
    UI.setElementContent('songResults', songsHtml);
    
    UI.showToast('Canciones recomendadas', 'success', 2000);
  } catch (error) {
    console.error('Error fetching song recommendations:', error);
    UI.showToast('Error al obtener canciones', 'error');
    UI.setElementContent(
      'songResults',
      UI.createEmptyState('Error al cargar canciones', '❌')
    );
  }
}

// ==================== Playlist Generation ====================

async function handlePlaylistGeneration() {
  const user = document.getElementById('playlistUser').value;
  const size = parseInt(document.getElementById('playlistSize').value, 10) || 5;
  const uniqueArtist = document.getElementById('playlistUnique').checked;
  const minDuration = document.getElementById('playlistMinDuration').value;
  const maxDuration = document.getElementById('playlistMaxDuration').value;
  
  if (!user) {
    UI.showToast('Seleccione un usuario', 'warning');
    return;
  }
  
  try {
    UI.showLoading('playlistResults');
    
    const minDur = minDuration ? parseInt(minDuration, 10) : null;
    const maxDur = maxDuration ? parseInt(maxDuration, 10) : null;
    
    const playlist = await api.playlist.generate(
      user,
      size,
      uniqueArtist,
      minDur,
      maxDur
    );
    
    if (!playlist) {
      UI.setElementContent(
        'playlistSummary',
        '<p class="text-gray-600">No se encontró una playlist que cumpla los criterios.</p>'
      );
      UI.clearElement('playlistResults');
      return;
    }
    
    // Render summary
    const totalDuration = playlist.totalDuration || 0;
    const durationMinutes = (totalDuration / 60).toFixed(1);
    const summaryHtml = `
      <div class="bg-purple-50 rounded-lg p-4 space-y-2">
        <div class="flex justify-between">
          <span class="text-sm text-gray-600">Canciones:</span>
          <span class="font-semibold">${playlist.size}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-sm text-gray-600">Duración total:</span>
          <span class="font-semibold">${totalDuration}s (${durationMinutes} min)</span>
        </div>
      </div>
    `;
    UI.setElementContent('playlistSummary', summaryHtml);
    
    // Render songs
    const songsHtml = UI.createList(
      playlist.songs || [],
      (song) => UI.createResultCard(song, 'song')
    );
    UI.setElementContent('playlistResults', songsHtml);
    
    UI.showToast('Playlist generada', 'success', 2000);
  } catch (error) {
    console.error('Error generating playlist:', error);
    UI.showToast('Error al generar playlist', 'error');
    UI.setElementContent(
      'playlistResults',
      UI.createEmptyState('Error al generar playlist', '❌')
    );
  }
}

// ==================== Trending ====================

async function loadTrending() {
  try {
    UI.showLoading('trendingResults');
    
    const items = await api.trending.getGlobal(10);
    state.lastResults.trending = items;
    
    // Render list
    const trendingHtml = UI.createList(items, (item, index) => {
      const title = item.title || item.songId || 'Desconocido';
      const artist = item.artist ? ` — ${item.artist}` : '';
      const plays = item.plays || 0;
      
      return `
        <div class="flex items-center justify-between p-4 bg-gray-50 
                    rounded-lg hover:bg-gray-100 transition">
          <div class="flex items-center gap-3">
            <span class="text-2xl font-bold text-purple-600">
              ${index + 1}
            </span>
            <div>
              <div class="font-medium text-gray-900">
                ${title}${artist}
              </div>
              <div class="text-sm text-gray-500">${plays} plays</div>
            </div>
          </div>
        </div>
      `;
    });
    UI.setElementContent('trendingResults', trendingHtml);
    
    // Create chart
    Charts.createTrendingChart('trendingChartContainer', items, {
      title: 'Top 10 Trending',
      labelKey: 'title',
      valueKey: 'plays'
    });
  } catch (error) {
    console.error('Error loading trending:', error);
    UI.setElementContent(
      'trendingResults',
      UI.createEmptyState('Error al cargar trending', '❌')
    );
  }
}

// ==================== Movie Recommendations ====================

async function handleMovieRecommendations() {
  const user = document.getElementById('movieUser').value;
  const k = parseInt(document.getElementById('movieK').value, 10) || 10;
  const algorithm = document.getElementById('movieAlgorithm').value;
  
  if (!user) {
    UI.showToast('Seleccione un usuario', 'warning');
    return;
  }
  
  try {
    UI.showLoading('movieResults');
    
    const movies = await api.movies.recommend(algorithm, user, k);
    state.lastResults.movies = movies;
    
    const moviesHtml = UI.createList(movies, (movie) => 
      UI.createResultCard(movie, 'movie')
    );
    UI.setElementContent('movieResults', moviesHtml);
    
    // Update genre distribution chart
    if (movies && movies.length > 0) {
      Charts.createGenreDistributionChart(
        'genreDistributionChartContainer',
        movies
      );
    }
    
    UI.showToast('Películas recomendadas', 'success', 2000);
  } catch (error) {
    console.error('Error fetching movie recommendations:', error);
    UI.showToast('Error al obtener películas', 'error');
    UI.setElementContent(
      'movieResults',
      UI.createEmptyState('Error al cargar películas', '❌')
    );
  }
}

// ==================== Marathon Planning ====================

async function handleMarathonPlanning() {
  const user = document.getElementById('marathonUser').value;
  const totalMinutes = parseInt(
    document.getElementById('marathonTime').value,
    10
  ) || 360;
  const minRating = parseFloat(
    document.getElementById('marathonRating').value
  ) || 3.5;
  const algorithm = document.getElementById('marathonAlgorithm').value;
  
  if (!user) {
    UI.showToast('Seleccione un usuario', 'warning');
    return;
  }
  
  try {
    UI.showLoading('marathonResults');
    
    const plan = await api.marathon.plan(
      user,
      totalMinutes,
      minRating,
      algorithm
    );
    state.lastResults.marathon = plan;
    
    // Render summary
    const summaryHtml = `
      <div class="bg-purple-50 rounded-lg p-4 space-y-2">
        <div class="flex justify-between">
          <span class="text-sm text-gray-600">Algoritmo:</span>
          <span class="font-semibold">${plan.algorithm}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-sm text-gray-600">Duración total:</span>
          <span class="font-semibold">
            ${plan.totalDuration} min (${(plan.totalDuration / 60).toFixed(1)} h)
          </span>
        </div>
        <div class="flex justify-between">
          <span class="text-sm text-gray-600">Rating promedio:</span>
          <span class="font-semibold">⭐ ${plan.averageRating.toFixed(2)}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-sm text-gray-600">Películas:</span>
          <span class="font-semibold">${plan.movies.length}</span>
        </div>
      </div>
    `;
    UI.setElementContent('marathonSummary', summaryHtml);
    
    // Render movies
    const moviesHtml = UI.createList(plan.movies || [], (movie) => 
      UI.createResultCard(movie, 'movie')
    );
    UI.setElementContent('marathonResults', moviesHtml);
    
    // Update chart
    if (plan.movies && plan.movies.length > 0) {
      Charts.createMarathonDurationChart(
        'marathonDurationChartContainer',
        plan
      );
    }
    
    UI.showToast('Maratón planificado', 'success', 2000);
  } catch (error) {
    console.error('Error planning marathon:', error);
    UI.showToast('Error al planificar maratón', 'error');
    UI.setElementContent(
      'marathonResults',
      UI.createEmptyState('Error al planificar maratón', '❌')
    );
  }
}

// ==================== Movie Trending ====================

async function handleMovieTrending() {
  const type = document.getElementById('movieTrendingType').value;
  const k = parseInt(
    document.getElementById('movieTrendingK').value,
    10
  ) || 10;
  const genre = document.getElementById('movieTrendingGenre').value.trim();
  
  try {
    UI.showLoading('movieTrendingResults');
    
    let movies;
    if (type === 'global') {
      movies = await api.movieTrending.getGlobal(k);
    } else if (type === 'by-genre') {
      if (!genre) {
        UI.showToast('Debe especificar un género', 'warning');
        return;
      }
      movies = await api.movieTrending.getByGenre(genre, k);
    } else if (type === 'influential') {
      movies = await api.movieTrending.getInfluential(k);
    }
    
    // Render list
    const moviesHtml = UI.createList(movies, (movie, index) => {
      const title = movie.title || movie.movieId || 'Desconocido';
      const movieGenre = movie.genre ? ` [${movie.genre}]` : '';
      
      let metric = '';
      if (type === 'influential') {
        const influence = movie.influenceScore || 0;
        metric = `Influencia: ${influence} películas`;
      } else {
        const watches = movie.watchCount || 0;
        metric = `${watches} views`;
      }
      
      return `
        <div class="flex items-center justify-between p-4 bg-gray-50 
                    rounded-lg hover:bg-gray-100 transition">
          <div class="flex items-center gap-3">
            <span class="text-2xl font-bold text-purple-600">
              ${index + 1}
            </span>
            <div>
              <div class="font-medium text-gray-900">
                ${title}${movieGenre}
              </div>
              <div class="text-sm text-gray-500">${metric}</div>
            </div>
          </div>
        </div>
      `;
    });
    UI.setElementContent('movieTrendingResults', moviesHtml);
    
    // Create chart
    Charts.createTrendingChart('movieTrendingChartContainer', movies, {
      title: `Top ${k} Películas`,
      labelKey: 'title',
      valueKey: type === 'influential' ? 'influenceScore' : 'watchCount'
    });
    
    UI.showToast('Trending actualizado', 'success', 2000);
  } catch (error) {
    console.error('Error loading movie trending:', error);
    UI.showToast('Error al cargar trending', 'error');
    UI.setElementContent(
      'movieTrendingResults',
      UI.createEmptyState('Error al cargar trending', '❌')
    );
  }
}

// ==================== Analytics ====================

async function handleAlgorithmComparison() {
  const user = document.getElementById('analyticsUser').value;
  const k = parseInt(document.getElementById('analyticsK').value, 10) || 10;
  
  if (!user) {
    UI.showToast('Seleccione un usuario', 'warning');
    return;
  }
  
  try {
    UI.showToast('Comparando algoritmos...', 'info', 5000);
    
    // Fetch recommendations from multiple algorithms in parallel
    const [bfs, dfs, diverse, sorted] = await Promise.all([
      api.movies.recommendBFS(user, k),
      api.movies.recommendDFS(user, k),
      api.movies.recommendDiverse(user, k),
      api.movies.recommendSorted(user, k)
    ]);
    
    const results = { bfs, dfs, diverse, sorted };
    
    // Create comparison chart
    Charts.createAlgorithmComparisonChart(
      'algorithmComparisonChartContainer',
      results
    );
    
    // Update stats
    document.getElementById('statsMovies').textContent = 
      new Set([...bfs, ...dfs, ...diverse, ...sorted].map(m => m.id || m.movieId)).size;
    
    UI.showToast('Comparación completada', 'success');
  } catch (error) {
    console.error('Error comparing algorithms:', error);
    UI.showToast('Error al comparar algoritmos', 'error');
  }
}

// ==================== Event Listeners ====================

function setupEventListeners() {
  // Friend recommendations
  document.getElementById('friendGo')
    .addEventListener('click', handleFriendRecommendations);
  
  // Song recommendations
  document.getElementById('songGo')
    .addEventListener('click', handleSongRecommendations);
  
  // Playlist generation
  document.getElementById('playlistGenerate')
    .addEventListener('click', handlePlaylistGeneration);
  
  // Trending refresh
  document.getElementById('trendingRefresh')
    .addEventListener('click', loadTrending);
  
  // Movie recommendations
  document.getElementById('movieGo')
    .addEventListener('click', handleMovieRecommendations);
  
  // Marathon planning
  document.getElementById('marathonGo')
    .addEventListener('click', handleMarathonPlanning);
  
  // Movie trending
  document.getElementById('movieTrendingGo')
    .addEventListener('click', handleMovieTrending);
  
  // Movie trending type change
  document.getElementById('movieTrendingType')
    .addEventListener('change', (e) => {
      const genreContainer = document.getElementById('genreInputContainer');
      if (e.target.value === 'by-genre') {
        genreContainer.classList.remove('hidden');
      } else {
        genreContainer.classList.add('hidden');
      }
    });
  
  // Analytics comparison
  document.getElementById('analyticsCompare')
    .addEventListener('click', handleAlgorithmComparison);
}

// ==================== Start Application ====================

// Wait for DOM to be ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', init);
} else {
  init();
}
