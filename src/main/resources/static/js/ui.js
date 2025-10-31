// ChePlay UI Components Module
// Reusable UI components and utilities

export const UI = {
  // ==================== Loading States ====================
  
  showLoading(elementId) {
    const element = document.getElementById(elementId);
    if (!element) return;
    
    element.innerHTML = `
      <div class="flex justify-center items-center py-8">
        <div class="loading-spinner"></div>
      </div>
    `;
    element.style.display = 'block';
  },

  hideLoading(elementId) {
    const element = document.getElementById(elementId);
    if (!element) return;
    element.style.display = 'none';
    element.innerHTML = '';
  },

  createSkeletonCard() {
    return `
      <div class="bg-white rounded-lg shadow p-6 animate-pulse">
        <div class="h-4 bg-gray-200 rounded w-3/4 mb-4"></div>
        <div class="h-3 bg-gray-200 rounded w-full mb-2"></div>
        <div class="h-3 bg-gray-200 rounded w-5/6"></div>
      </div>
    `;
  },

  // ==================== Toast Notifications ====================
  
  showToast(message, type = 'info', duration = 3000) {
    const existingToast = document.querySelector('.toast');
    if (existingToast) {
      existingToast.remove();
    }

    const icons = {
      success: '✓',
      error: '✕',
      warning: '⚠',
      info: 'ℹ'
    };

    const toast = document.createElement('div');
    toast.className = `toast toast-${type} fade-in`;
    toast.innerHTML = `
      <span class="text-xl">${icons[type] || icons.info}</span>
      <span class="flex-1">${message}</span>
      <button onclick="this.parentElement.remove()" 
              class="ml-2 opacity-70 hover:opacity-100">
        ✕
      </button>
    `;

    document.body.appendChild(toast);

    if (duration > 0) {
      setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
      }, duration);
    }
  },

  // ==================== Card Components ====================
  
  createCard(title, content, options = {}) {
    const { className = '', icon = '' } = options;
    
    return `
      <div class="bg-white rounded-lg shadow-md p-6 card-hover ${className}">
        ${title ? `
          <h3 class="text-lg font-semibold text-gray-800 mb-4 
                     flex items-center gap-2">
            ${icon ? `<span>${icon}</span>` : ''}
            ${title}
          </h3>
        ` : ''}
        <div class="card-content">
          ${content}
        </div>
      </div>
    `;
  },

  createResultCard(item, type = 'default') {
    const templates = {
      friend: (data) => `
        <div class="result-item flex items-center justify-between p-4 
                    bg-gray-50 rounded-lg hover:bg-gray-100 transition">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full bg-gradient-to-r 
                        from-purple-600 to-purple-800 flex items-center 
                        justify-center text-white font-semibold">
              ${(data.label || data.name || data.id || 'U')[0].toUpperCase()}
            </div>
            <div>
              <div class="font-medium text-gray-900">
                ${data.label || data.name || data.id}
              </div>
              ${data.sharedSongs ? `
                <div class="text-sm text-gray-500">
                  ${data.sharedSongs} canciones compartidas
                </div>
              ` : ''}
            </div>
          </div>
        </div>
      `,
      
      song: (data) => `
        <div class="result-item flex items-center justify-between p-4 
                    bg-gray-50 rounded-lg hover:bg-gray-100 transition">
          <div class="flex-1">
            <div class="font-medium text-gray-900">
              ${data.title || data.label || data.id}
            </div>
            ${data.artist ? `
              <div class="text-sm text-gray-500">${data.artist}</div>
            ` : ''}
          </div>
          ${data.score != null ? `
            <div class="badge badge-primary">
              Score: ${this.formatScore(data.score)}
            </div>
          ` : ''}
        </div>
      `,
      
      movie: (data) => `
        <div class="result-item flex items-center justify-between p-4 
                    bg-gray-50 rounded-lg hover:bg-gray-100 transition">
          <div class="flex-1">
            <div class="font-medium text-gray-900">
              ${data.title || data.movieId || data.id}
            </div>
            <div class="flex gap-2 mt-1">
              ${data.genre ? `
                <span class="badge badge-primary text-xs">
                  ${data.genre}
                </span>
              ` : ''}
              ${data.year ? `
                <span class="text-sm text-gray-500">${data.year}</span>
              ` : ''}
            </div>
          </div>
          ${data.score != null || data.rating != null ? `
            <div class="text-right">
              ${data.score != null ? `
                <div class="badge badge-success">
                  ${this.formatScore(data.score)}
                </div>
              ` : ''}
              ${data.rating != null ? `
                <div class="text-sm text-gray-500 mt-1">
                  ⭐ ${data.rating.toFixed(1)}
                </div>
              ` : ''}
            </div>
          ` : ''}
        </div>
      `,
      
      default: (data) => `
        <div class="p-4 bg-gray-50 rounded-lg">
          <pre class="text-sm">${JSON.stringify(data, null, 2)}</pre>
        </div>
      `
    };

    const template = templates[type] || templates.default;
    return template(item);
  },

  // ==================== Badges ====================
  
  createBadge(text, color = 'primary') {
    return `<span class="badge badge-${color}">${text}</span>`;
  },

  // ==================== Empty States ====================
  
  createEmptyState(message, icon = '📭') {
    return `
      <div class="empty-state">
        <div class="empty-state-icon">${icon}</div>
        <p class="text-gray-600">${message}</p>
      </div>
    `;
  },

  // ==================== Lists ====================
  
  createList(items, itemRenderer) {
    if (!items || items.length === 0) {
      return this.createEmptyState('No hay elementos para mostrar');
    }

    const itemsHtml = items.map(itemRenderer).join('');
    return `<div class="space-y-2">${itemsHtml}</div>`;
  },

  // ==================== Progress Bar ====================
  
  createProgressBar(current, total, label = '') {
    const percentage = total > 0 ? (current / total) * 100 : 0;
    
    return `
      <div class="mb-2">
        ${label ? `
          <div class="flex justify-between text-sm text-gray-600 mb-1">
            <span>${label}</span>
            <span>${current} / ${total}</span>
          </div>
        ` : ''}
        <div class="progress-bar">
          <div class="progress-bar-fill" 
               style="width: ${percentage}%"></div>
        </div>
      </div>
    `;
  },

  // ==================== Formatting Utilities ====================
  
  formatScore(score) {
    if (typeof score !== 'number') return score;
    return score.toFixed(3).replace(/\.000$/, '');
  },

  formatDuration(seconds) {
    if (!seconds) return '0s';
    
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    
    const parts = [];
    if (hours > 0) parts.push(`${hours}h`);
    if (minutes > 0) parts.push(`${minutes}m`);
    if (secs > 0 || parts.length === 0) parts.push(`${secs}s`);
    
    return parts.join(' ');
  },

  formatNumber(num) {
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1) + 'M';
    } else if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
  },

  // ==================== Form Helpers ====================
  
  populateSelect(selectId, items, options = {}) {
    const select = document.getElementById(selectId);
    if (!select) return;

    const {
      valueKey = 'id',
      labelKey = 'label',
      defaultValue = null
    } = options;

    select.innerHTML = '';

    items.forEach(item => {
      const option = document.createElement('option');
      
      if (typeof item === 'object') {
        option.value = item[valueKey] || item.id || item.value || '';
        option.textContent = item[labelKey] || 
                            item.label || 
                            item.name || 
                            item.username || 
                            option.value;
      } else {
        option.value = item;
        option.textContent = item;
      }
      
      select.appendChild(option);
    });

    if (defaultValue && select.options.length > 0) {
      select.value = defaultValue;
    } else if (select.options.length > 0) {
      select.value = select.options[0].value;
    }
  },

  getFormData(formId) {
    const form = document.getElementById(formId);
    if (!form) return {};

    const data = {};
    const inputs = form.querySelectorAll('input, select, textarea');
    
    inputs.forEach(input => {
      if (input.type === 'checkbox') {
        data[input.id || input.name] = input.checked;
      } else if (input.type === 'number') {
        data[input.id || input.name] = parseFloat(input.value) || 0;
      } else {
        data[input.id || input.name] = input.value;
      }
    });

    return data;
  },

  // ==================== Animation Helpers ====================
  
  fadeIn(elementId) {
    const element = document.getElementById(elementId);
    if (!element) return;
    element.classList.add('fade-in');
  },

  slideIn(elementId) {
    const element = document.getElementById(elementId);
    if (!element) return;
    element.classList.add('slide-in');
  },

  // ==================== Utility Functions ====================
  
  clearElement(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
      element.innerHTML = '';
    }
  },

  setElementContent(elementId, content) {
    const element = document.getElementById(elementId);
    if (element) {
      element.innerHTML = content;
    }
  }
};

