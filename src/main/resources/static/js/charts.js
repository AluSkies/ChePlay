// ChePlay Charts Module
// Data visualization using Chart.js

export const Charts = {
  // Store chart instances for cleanup
  instances: {},

  // ==================== Chart Utilities ====================
  
  destroyChart(chartId) {
    if (this.instances[chartId]) {
      this.instances[chartId].destroy();
      delete this.instances[chartId];
    }
  },

  getOrCreateCanvas(containerId, chartId) {
    const container = document.getElementById(containerId);
    if (!container) return null;

    // Destroy existing chart if any
    this.destroyChart(chartId);

    // Clear container and create new canvas
    container.innerHTML = '';
    const canvas = document.createElement('canvas');
    canvas.id = chartId;
    container.appendChild(canvas);

    return canvas;
  },

  // ==================== Color Schemes ====================
  
  colors: {
    primary: 'rgba(102, 126, 234, 1)',
    primaryLight: 'rgba(102, 126, 234, 0.2)',
    secondary: 'rgba(118, 75, 162, 1)',
    secondaryLight: 'rgba(118, 75, 162, 0.2)',
    success: 'rgba(16, 185, 129, 1)',
    successLight: 'rgba(16, 185, 129, 0.2)',
    warning: 'rgba(245, 158, 11, 1)',
    warningLight: 'rgba(245, 158, 11, 0.2)',
    danger: 'rgba(239, 68, 68, 1)',
    dangerLight: 'rgba(239, 68, 68, 0.2)',
    info: 'rgba(59, 130, 246, 1)',
    infoLight: 'rgba(59, 130, 246, 0.2)',
    
    getColorPalette(count) {
      const palette = [
        'rgba(102, 126, 234, 0.8)',
        'rgba(118, 75, 162, 0.8)',
        'rgba(16, 185, 129, 0.8)',
        'rgba(245, 158, 11, 0.8)',
        'rgba(239, 68, 68, 0.8)',
        'rgba(59, 130, 246, 0.8)',
        'rgba(236, 72, 153, 0.8)',
        'rgba(20, 184, 166, 0.8)',
        'rgba(251, 146, 60, 0.8)',
        'rgba(168, 85, 247, 0.8)'
      ];
      
      return Array.from({ length: count }, (_, i) => 
        palette[i % palette.length]
      );
    }
  },

  // ==================== Common Chart Options ====================
  
  getBaseOptions(title = '') {
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: true,
          position: 'top',
          labels: {
            font: { size: 12 },
            padding: 15
          }
        },
        title: {
          display: !!title,
          text: title,
          font: { size: 16, weight: 'bold' },
          padding: 20
        },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          padding: 12,
          cornerRadius: 8,
          titleFont: { size: 14, weight: 'bold' },
          bodyFont: { size: 13 }
        }
      }
    };
  },

  // ==================== Friend Network Chart ====================
  
  createFriendNetworkChart(containerId, data) {
    const canvas = this.getOrCreateCanvas(containerId, 'friendNetworkChart');
    if (!canvas) return null;

    const ctx = canvas.getContext('2d');
    
    // Prepare data for scatter plot (simulating network)
    const userNode = { x: 0, y: 0, label: data.user };
    const directNodes = (data.directRecommendations || []).map((f, i) => ({
      x: Math.cos(i * 2 * Math.PI / 8) * 30,
      y: Math.sin(i * 2 * Math.PI / 8) * 30,
      label: f.label || f.name || f.id
    }));

    const chart = new Chart(ctx, {
      type: 'scatter',
      data: {
        datasets: [
          {
            label: 'Usuario',
            data: [userNode],
            backgroundColor: this.colors.primary,
            pointRadius: 15,
            pointHoverRadius: 18
          },
          {
            label: 'Conexiones directas',
            data: directNodes,
            backgroundColor: this.colors.success,
            pointRadius: 10,
            pointHoverRadius: 13
          }
        ]
      },
      options: {
        ...this.getBaseOptions('Red de Conexiones'),
        scales: {
          x: { display: false },
          y: { display: false }
        },
        plugins: {
          ...this.getBaseOptions().plugins,
          tooltip: {
            ...this.getBaseOptions().plugins.tooltip,
            callbacks: {
              label: (context) => {
                const point = context.raw;
                return point.label || 'Usuario';
              }
            }
          }
        }
      }
    });

    this.instances['friendNetworkChart'] = chart;
    return chart;
  },

  // ==================== Shared Songs Bar Chart ====================
  
  createSharedSongsChart(containerId, data) {
    const canvas = this.getOrCreateCanvas(containerId, 'sharedSongsChart');
    if (!canvas) return null;

    const ctx = canvas.getContext('2d');
    
    // Extract shared songs data
    const sharedSongs = data.sharedSongs || {};
    const labels = Object.keys(sharedSongs);
    const values = labels.map(key => (sharedSongs[key] || []).length);

    const chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          label: 'Canciones compartidas',
          data: values,
          backgroundColor: this.colors.getColorPalette(labels.length),
          borderRadius: 8
        }]
      },
      options: {
        ...this.getBaseOptions('Top Amigos por Canciones Compartidas'),
        indexAxis: 'y',
        scales: {
          x: {
            beginAtZero: true,
            ticks: { stepSize: 1 }
          }
        }
      }
    });

    this.instances['sharedSongsChart'] = chart;
    return chart;
  },

  // ==================== Trending Bar Chart ====================
  
  createTrendingChart(containerId, items, options = {}) {
    const canvas = this.getOrCreateCanvas(containerId, 'trendingChart');
    if (!canvas) return null;

    const ctx = canvas.getContext('2d');
    
    const {
      labelKey = 'title',
      valueKey = 'plays',
      title = 'Top Trending'
    } = options;

    const labels = items.map(item => 
      item[labelKey] || item.songId || item.movieId || item.id || 'Unknown'
    );
    const values = items.map(item => item[valueKey] || 0);

    const chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          label: valueKey === 'plays' ? 'Reproducciones' : 'Visualizaciones',
          data: values,
          backgroundColor: this.colors.getColorPalette(labels.length),
          borderRadius: 8
        }]
      },
      options: {
        ...this.getBaseOptions(title),
        indexAxis: 'y',
        scales: {
          x: {
            beginAtZero: true
          }
        },
        plugins: {
          ...this.getBaseOptions().plugins,
          tooltip: {
            ...this.getBaseOptions().plugins.tooltip,
            callbacks: {
              label: (context) => {
                return `${context.dataset.label}: ${context.parsed.x}`;
              }
            }
          }
        }
      }
    });

    this.instances['trendingChart'] = chart;
    return chart;
  },

  // ==================== Genre Distribution Pie Chart ====================
  
  createGenreDistributionChart(containerId, movies) {
    const canvas = this.getOrCreateCanvas(
      containerId, 
      'genreDistributionChart'
    );
    if (!canvas) return null;

    const ctx = canvas.getContext('2d');
    
    // Count genres
    const genreCounts = {};
    movies.forEach(movie => {
      const genre = movie.genre || 'Desconocido';
      genreCounts[genre] = (genreCounts[genre] || 0) + 1;
    });

    const labels = Object.keys(genreCounts);
    const values = Object.values(genreCounts);

    const chart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: values,
          backgroundColor: this.colors.getColorPalette(labels.length),
          borderWidth: 2,
          borderColor: '#fff'
        }]
      },
      options: {
        ...this.getBaseOptions('Distribución por Género'),
        plugins: {
          ...this.getBaseOptions().plugins,
          legend: {
            position: 'right'
          }
        }
      }
    });

    this.instances['genreDistributionChart'] = chart;
    return chart;
  },

  // ==================== Algorithm Comparison Chart ====================
  
  createAlgorithmComparisonChart(containerId, results) {
    const canvas = this.getOrCreateCanvas(
      containerId, 
      'algorithmComparisonChart'
    );
    if (!canvas) return null;

    const ctx = canvas.getContext('2d');
    
    // results is an object like { bfs: [...], dfs: [...], ... }
    const algorithms = Object.keys(results);
    const datasets = algorithms.map((algo, index) => ({
      label: algo.toUpperCase(),
      data: results[algo].map((item, i) => ({
        x: i + 1,
        y: item.score || 0,
        title: item.title || item.id || `Película ${i + 1}`,
        genre: item.genre || null,
        year: item.year || null,
        movieData: item
      })),
      borderColor: this.colors.getColorPalette(algorithms.length)[index],
      backgroundColor: 
        this.colors.getColorPalette(algorithms.length)[index].replace(
          '0.8', 
          '0.1'
        ),
      tension: 0.4,
      fill: true
    }));

    const chart = new Chart(ctx, {
      type: 'line',
      data: { datasets },
      options: {
        ...this.getBaseOptions('Comparación de Algoritmos'),
        plugins: {
          tooltip: {
            callbacks: {
              title: (context) => {
                const dataPoint = context[0].raw;
                return dataPoint.title || `Ranking ${dataPoint.x}`;
              },
              label: (context) => {
                const algo = context.dataset.label;
                const score = context.parsed.y.toFixed(3);
                return `${algo}: ${score}`;
              },
              afterLabel: (context) => {
                const dataPoint = context.raw;
                const info = [];
                if (dataPoint.genre) {
                  info.push(`Género: ${dataPoint.genre}`);
                }
                if (dataPoint.year) {
                  info.push(`Año: ${dataPoint.year}`);
                }
                return info;
              }
            }
          }
        },
        scales: {
          x: {
            type: 'linear',
            title: {
              display: true,
              text: 'Ranking'
            }
          },
          y: {
            title: {
              display: true,
              text: 'Score'
            }
          }
        }
      }
    });

    this.instances['algorithmComparisonChart'] = chart;
    return chart;
  },

  // ==================== Marathon Duration Chart ====================
  
  createMarathonDurationChart(containerId, plan) {
    const canvas = this.getOrCreateCanvas(
      containerId, 
      'marathonDurationChart'
    );
    if (!canvas) return null;

    const ctx = canvas.getContext('2d');
    
    const movies = plan.movies || [];
    let cumulative = 0;
    const data = movies.map(movie => {
      cumulative += movie.duration || 0;
      return cumulative;
    });

    const chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: movies.map((m, i) => `${i + 1}`),
        datasets: [{
          label: 'Duración acumulada (min)',
          data: data,
          borderColor: this.colors.primary,
          backgroundColor: this.colors.primaryLight,
          tension: 0.4,
          fill: true,
          pointRadius: 5,
          pointHoverRadius: 7
        }]
      },
      options: {
        ...this.getBaseOptions('Duración del Maratón'),
        scales: {
          x: {
            title: {
              display: true,
              text: 'Película #'
            }
          },
          y: {
            beginAtZero: true,
            title: {
              display: true,
              text: 'Minutos'
            }
          }
        }
      }
    });

    this.instances['marathonDurationChart'] = chart;
    return chart;
  },

  // ==================== Rating Distribution Chart ====================
  
  createRatingDistributionChart(containerId, movies) {
    const canvas = this.getOrCreateCanvas(
      containerId, 
      'ratingDistributionChart'
    );
    if (!canvas) return null;

    const ctx = canvas.getContext('2d');
    
    // Create histogram bins for ratings
    const bins = [0, 1, 2, 3, 4, 5];
    const counts = bins.map((bin, i) => {
      if (i === bins.length - 1) return 0;
      const nextBin = bins[i + 1];
      return movies.filter(m => {
        const rating = m.rating || 0;
        return rating >= bin && rating < nextBin;
      }).length;
    });
    counts.pop(); // Remove last empty bin

    const chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['0-1', '1-2', '2-3', '3-4', '4-5'],
        datasets: [{
          label: 'Cantidad de películas',
          data: counts,
          backgroundColor: this.colors.getColorPalette(5),
          borderRadius: 8
        }]
      },
      options: {
        ...this.getBaseOptions('Distribución de Ratings'),
        scales: {
          y: {
            beginAtZero: true,
            ticks: { stepSize: 1 }
          }
        }
      }
    });

    this.instances['ratingDistributionChart'] = chart;
    return chart;
  },

  // ==================== Score Timeline Chart ====================
  
  createScoreTimelineChart(containerId, items) {
    const canvas = this.getOrCreateCanvas(
      containerId, 
      'scoreTimelineChart'
    );
    if (!canvas) return null;

    const ctx = canvas.getContext('2d');
    
    const labels = items.map((item, i) => 
      item.title || item.label || `#${i + 1}`
    );
    const scores = items.map(item => item.score || 0);

    const chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [{
          label: 'Score',
          data: scores,
          borderColor: this.colors.primary,
          backgroundColor: this.colors.primaryLight,
          tension: 0.4,
          fill: true,
          pointRadius: 5,
          pointHoverRadius: 7
        }]
      },
      options: {
        ...this.getBaseOptions('Evolución de Scores'),
        scales: {
          y: {
            beginAtZero: true
          }
        }
      }
    });

    this.instances['scoreTimelineChart'] = chart;
    return chart;
  },

  // ==================== Playlist Duration Chart ====================
  
  createPlaylistDurationChart(containerId, playlist) {
    const canvas = this.getOrCreateCanvas(
      containerId, 
      'playlistDurationChart'
    );
    if (!canvas) return null;

    const ctx = canvas.getContext('2d');
    
    const songs = playlist.songs || [];
    const labels = songs.map(s => s.title || s.id || 'Unknown');
    const durations = songs.map(s => s.duration || 0);

    const chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          label: 'Duración (seg)',
          data: durations,
          backgroundColor: this.colors.getColorPalette(labels.length),
          borderRadius: 8
        }]
      },
      options: {
        ...this.getBaseOptions('Duración de Canciones en Playlist'),
        scales: {
          y: {
            beginAtZero: true
          }
        }
      }
    });

    this.instances['playlistDurationChart'] = chart;
    return chart;
  },

  // ==================== Cleanup ====================
  
  destroyAll() {
    Object.keys(this.instances).forEach(chartId => {
      this.destroyChart(chartId);
    });
  }
};



