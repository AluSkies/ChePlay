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
    const userNode = { 
      x: 0, 
      y: 0, 
      label: data.user || 'Usuario',
      type: 'user',
      id: data.user
    };
    
    // Direct recommendations (inner circle)
    const directNodes = (data.directRecommendations || []).map((f, i, arr) => {
      const angle = (i * 2 * Math.PI) / Math.max(arr.length, 1);
      return {
        x: Math.cos(angle) * 30,
        y: Math.sin(angle) * 30,
        label: f.label || f.name || f.id,
        type: 'direct',
        weight: f.weight,
        id: f.id
      };
    });
    
    // Shortest path recommendations (outer circle)
    const shortestNodes = (data.shortestPathRecommendations || []).map((f, i, arr) => {
      const angle = (i * 2 * Math.PI) / Math.max(arr.length, 1);
      return {
        x: Math.cos(angle) * 50,
        y: Math.sin(angle) * 50,
        label: f.label || f.name || f.id,
        type: 'shortest',
        distance: f.distance,
        id: f.id
      };
    });
    
    // Closest friend (highlighted)
    const closestNode = data.closest ? {
      x: 0,
      y: -40,
      label: data.closest.label || data.closest.name || data.closest.id,
      type: 'closest',
      distance: data.closest.distance,
      id: data.closest.id
    } : null;
    
    // Collect all nodes for drawing connections
    const allNodes = [userNode, ...directNodes, ...shortestNodes];
    if (closestNode) {
      allNodes.push(closestNode);
    }
    
    const datasets = [
      {
        label: 'Usuario',
        data: [userNode],
        backgroundColor: this.colors.primary,
        pointRadius: 20,
        pointHoverRadius: 22,
        borderWidth: 2,
        borderColor: '#667eea'
      }
    ];
    
    if (directNodes.length > 0) {
      datasets.push({
        label: 'Conexiones directas',
        data: directNodes,
        backgroundColor: this.colors.success,
        pointRadius: 15,
        pointHoverRadius: 17,
        borderWidth: 2,
        borderColor: '#10b981'
      });
    }
    
    if (shortestNodes.length > 0) {
      datasets.push({
        label: 'Por camino más corto',
        data: shortestNodes,
        backgroundColor: '#f59e0b',
        pointRadius: 12,
        pointHoverRadius: 14,
        borderWidth: 2,
        borderColor: '#d97706'
      });
    }
    
    if (closestNode) {
      datasets.push({
        label: 'Amigo más cercano',
        data: [closestNode],
        backgroundColor: '#ef4444',
        pointRadius: 18,
        pointHoverRadius: 20,
        borderWidth: 3,
        borderColor: '#dc2626'
      });
    }

    // Custom plugin to draw connections and labels
    const self = this;
    const networkPlugin = {
      id: 'networkConnections',
      afterDraw: (chart) => {
        self.drawNetworkConnections(chart, userNode, allNodes);
        self.drawNodeLabels(chart, allNodes);
      }
    };

    const chart = new Chart(ctx, {
      type: 'scatter',
      data: { datasets },
      plugins: [networkPlugin],
      options: {
        ...this.getBaseOptions('Red de Conexiones'),
        scales: {
          x: { 
            display: false,
            min: -60,
            max: 60
          },
          y: { 
            display: false,
            min: -60,
            max: 60
          }
        },
        plugins: {
          ...this.getBaseOptions().plugins,
          tooltip: {
            ...this.getBaseOptions().plugins.tooltip,
            callbacks: {
              label: (context) => {
                const point = context.raw;
                let label = point.label || 'Usuario';
                if (point.weight) {
                  label += ` (Peso: ${point.weight.toFixed(3)})`;
                }
                if (point.distance) {
                  label += ` (Distancia: ${point.distance.toFixed(3)})`;
                }
                return label;
              }
            }
          },
          legend: {
            display: true,
            position: 'bottom'
          }
        }
      }
    });

    this.instances['friendNetworkChart'] = chart;
    return chart;
  },

  drawNetworkConnections(chart, centerNode, allNodes) {
    const ctx = chart.ctx;
    const meta = chart.getDatasetMeta(0);
    const centerPoint = meta.data[0];
    
    if (!centerPoint) return;
    
    const centerX = centerPoint.x;
    const centerY = centerPoint.y;
    
    // Draw lines from center to all other nodes
    allNodes.forEach((node, nodeIndex) => {
      if (node.type === 'user') return; // Skip center node
      
      // Find the dataset and point for this node
      let targetPoint = null;
      for (let i = 0; i < chart.data.datasets.length; i++) {
        const datasetMeta = chart.getDatasetMeta(i);
        const nodeDataIndex = chart.data.datasets[i].data.findIndex(d => d.id === node.id);
        if (nodeDataIndex >= 0 && datasetMeta.data[nodeDataIndex]) {
          targetPoint = datasetMeta.data[nodeDataIndex];
          break;
        }
      }
      
      if (!targetPoint) return;
      
      const targetX = targetPoint.x;
      const targetY = targetPoint.y;
      
      // Determine line color based on node type
      let lineColor = 'rgba(156, 163, 175, 0.3)'; // default gray
      if (node.type === 'direct') {
        lineColor = 'rgba(16, 185, 129, 0.4)'; // green
      } else if (node.type === 'shortest') {
        lineColor = 'rgba(245, 158, 11, 0.4)'; // orange
      } else if (node.type === 'closest') {
        lineColor = 'rgba(239, 68, 68, 0.4)'; // red
      }
      
      // Draw line
      ctx.save();
      ctx.strokeStyle = lineColor;
      ctx.lineWidth = 2;
      ctx.setLineDash([]);
      ctx.beginPath();
      ctx.moveTo(centerX, centerY);
      ctx.lineTo(targetX, targetY);
      ctx.stroke();
      ctx.restore();
    });
  },

  drawNodeLabels(chart, allNodes) {
    const ctx = chart.ctx;
    
    allNodes.forEach((node) => {
      // Find the dataset and point for this node
      let targetPoint = null;
      let datasetIndex = -1;
      let nodeDataIndex = -1;
      
      for (let i = 0; i < chart.data.datasets.length; i++) {
        const nodeIdx = chart.data.datasets[i].data.findIndex(d => d.id === node.id);
        if (nodeIdx >= 0) {
          const datasetMeta = chart.getDatasetMeta(i);
          if (datasetMeta.data[nodeIdx]) {
            targetPoint = datasetMeta.data[nodeIdx];
            datasetIndex = i;
            nodeDataIndex = nodeIdx;
            break;
          }
        }
      }
      
      if (!targetPoint) return;
      
      const x = targetPoint.x;
      const y = targetPoint.y;
      
      // Get node radius based on type
      let radius = 10;
      if (node.type === 'user') radius = 20;
      else if (node.type === 'closest') radius = 18;
      else if (node.type === 'direct') radius = 15;
      else if (node.type === 'shortest') radius = 12;
      
      // Prepare label text (shorten if too long)
      let labelText = node.label || node.id || 'Usuario';
      if (labelText.length > 8) {
        labelText = labelText.substring(0, 6) + '...';
      }
      
      // Draw text background circle for better visibility
      ctx.save();
      ctx.fillStyle = 'rgba(255, 255, 255, 0.9)';
      ctx.beginPath();
      ctx.arc(x, y, radius * 0.7, 0, 2 * Math.PI);
      ctx.fill();
      ctx.restore();
      
      // Draw label text
      ctx.save();
      ctx.fillStyle = '#1f2937';
      ctx.font = 'bold 10px Arial';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(labelText, x, y);
      ctx.restore();
    });
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
        y: item.score || 0
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

