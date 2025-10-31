// ChePlay Tabs Module
// Tab navigation system

export const Tabs = {
  currentTab: 'music',
  
  // ==================== Initialization ====================
  
  init() {
    this.setupTabButtons();
    this.showTab(this.currentTab);
  },

  // ==================== Tab Management ====================
  
  setupTabButtons() {
    const tabButtons = document.querySelectorAll('[data-tab]');
    
    tabButtons.forEach(button => {
      button.addEventListener('click', (e) => {
        e.preventDefault();
        const tabId = button.getAttribute('data-tab');
        this.showTab(tabId);
      });
    });
  },

  showTab(tabId) {
    // Hide all tab contents
    const allContents = document.querySelectorAll('.tab-content');
    allContents.forEach(content => {
      content.classList.remove('active');
    });

    // Remove active class from all buttons
    const allButtons = document.querySelectorAll('[data-tab]');
    allButtons.forEach(button => {
      button.classList.remove('active');
    });

    // Show selected tab content
    const selectedContent = document.getElementById(`${tabId}-tab`);
    if (selectedContent) {
      selectedContent.classList.add('active');
    }

    // Add active class to selected button
    const selectedButton = document.querySelector(
      `[data-tab="${tabId}"]`
    );
    if (selectedButton) {
      selectedButton.classList.add('active');
    }

    this.currentTab = tabId;
    
    // Store in session storage for persistence
    sessionStorage.setItem('activeTab', tabId);
  },

  // ==================== Get Active Tab ====================
  
  getActiveTab() {
    return this.currentTab;
  },

  // ==================== Restore Last Active Tab ====================
  
  restoreLastTab() {
    const lastTab = sessionStorage.getItem('activeTab');
    if (lastTab) {
      this.showTab(lastTab);
    }
  },

  // ==================== Section Collapse/Expand ====================
  
  setupCollapsibleSections() {
    const headers = document.querySelectorAll('.section-header');
    
    headers.forEach(header => {
      header.addEventListener('click', () => {
        const section = header.parentElement;
        const content = section.querySelector('.section-content');
        
        if (content) {
          const isExpanded = content.classList.contains('expanded');
          
          if (isExpanded) {
            content.classList.remove('expanded');
            content.style.maxHeight = '0';
            header.classList.remove('expanded');
          } else {
            content.classList.add('expanded');
            content.style.maxHeight = content.scrollHeight + 'px';
            header.classList.add('expanded');
          }
        }
      });
    });
  },

  // ==================== Expand Section ====================
  
  expandSection(sectionId) {
    const section = document.getElementById(sectionId);
    if (!section) return;

    const content = section.querySelector('.section-content');
    const header = section.querySelector('.section-header');

    if (content && !content.classList.contains('expanded')) {
      content.classList.add('expanded');
      content.style.maxHeight = content.scrollHeight + 'px';
      if (header) {
        header.classList.add('expanded');
      }
    }
  },

  // ==================== Collapse Section ====================
  
  collapseSection(sectionId) {
    const section = document.getElementById(sectionId);
    if (!section) return;

    const content = section.querySelector('.section-content');
    const header = section.querySelector('.section-header');

    if (content && content.classList.contains('expanded')) {
      content.classList.remove('expanded');
      content.style.maxHeight = '0';
      if (header) {
        header.classList.remove('expanded');
      }
    }
  },

  // ==================== Scroll to Section ====================
  
  scrollToSection(sectionId) {
    const section = document.getElementById(sectionId);
    if (section) {
      section.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }
};

