// Class To load the Navbar and Footer components

class FragmentLoader {
  constructor() {
    this.cache = new Map();
  }

  /**
   * Loads an HTML fragment from a given URL and inserts it into the specified target element.
   * Executes any scripts contained within the loaded fragment.
   *
   * @param {string} url - The URL of the HTML fragment to load.
   * @param {string} targetSelector - The CSS selector of the target element where the fragment will be inserted.
   * @param {function} [callback] - An optional callback function to execute after the fragment is loaded and scripts are executed.
   * @return {Promise<void>} A promise that resolves once the HTML fragment is loaded, inserted, and scripts are executed.
   */
  async loadFragment(url, targetSelector, callback) {
    try{
      let html;
      if (this.cache.has(url)) {
        html = this.cache.get(url);
      }
      else{
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error(`Failed to load fragment: ${response.statusText}`);
        }
        html = await response.text();
        this.cache.set(url, html);
      }
      // Insert the loaded HTML into the target element
      const targetElement = document.querySelector(targetSelector);
      if(targetElement)
        targetElement.innerHTML = html;
      // Execute any scripts contained within the loaded HTML
      this.executeScripts(targetElement);
      // call the callback function if provided
      if (callback && typeof callback === 'function') {
        callback();
      }
      else  {
        console.error(`Target element not found: ${targetSelector}`);
      }
      }
      catch (error) {
        console.error(`Error loading fragment from ${url}:`, error);
      }
    }
  /**
   * Execute any script tags found in the loaded fragment
   * @param {HTMLElement} container - The container element
   */
  executeScripts(container) {
    const scripts = container.querySelectorAll('script');
    scripts.forEach(oldScript => {
      const newScript = document.createElement('script');

      // Copy attributes
      Array.from(oldScript.attributes).forEach(attr => {
        newScript.setAttribute(attr.name, attr.value);
      });

      // Copy content
      newScript.textContent = oldScript.textContent;

      // Replace an old script with a new one to execute it
      oldScript.parentNode.replaceChild(newScript, oldScript);
    });
  }

  /**
   * Initialize common fragments (header and footer)
   */
  async initCommonFragments() {
    // Load header
    await this.loadFragment('/templates/fragments/navi-bar.html', '#header-fragment', () => {
      // Initialize mobile menu after header loads
      this.initMobileMenu();
    });

    // Load footer
    await this.loadFragment('/templates/fragments/footer.html', '#footer-fragment');
  }

  /**
   * Initialize mobile menu functionality
   */
  initMobileMenu() {
    const hamburger = document.querySelector('.hamburger-menu');
    const mobileNav = document.querySelector('.mobile-nav');

    if (hamburger && mobileNav) {
      hamburger.addEventListener('click', function() {
        hamburger.classList.toggle('open');
        mobileNav.classList.toggle('open');
      });
    }
  }

  /**
   * Update active navigation link based on current page
   */
  updateActiveNavLink() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-links a, .mobile-nav-links a');

    navLinks.forEach(link => {
      const linkPath = new URL(link.href).pathname;
      if (currentPath === linkPath ||
          (currentPath === '/' && linkPath.endsWith('index.html')) ||
          (currentPath.endsWith('index.html') && linkPath === '/')) {
        link.classList.add('active');
      } else {
        link.classList.remove('active');
      }
    });
  }
}

// Create global instance
const fragmentLoader = new FragmentLoader();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', async () => {
  await fragmentLoader.initCommonFragments();
  fragmentLoader.updateActiveNavLink();
});

// Export for use in other scripts
window.fragmentLoader = fragmentLoader;