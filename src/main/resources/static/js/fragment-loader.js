/**
 * Class to load HTML fragments into the index page
 */
class FragmentLoader {
  constructor() {
    this.cache = new Map(); // Cache to store loaded fragments
  }

  // Load a fragment from a URL and insert it into the target element
  async loadFragment(url, targetSelector) {
    try {
      let html;
      if (this.cache.has(url)) {
        html = this.cache.get(url);
      } else {
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error(`Failed to load fragment: ${response.status}`);
        }
        html = await response.text();
        this.cache.set(url, html);
      }

      const target = document.querySelector(targetSelector);
      if (target) {
        target.innerHTML = html;
        this.executeScripts(target);
      } else {
        console.error(`Target element not found: ${targetSelector}`);
      }
    } catch (error) {
      console.error('Error loading fragment:', error);
    }
  }

  // Execute scripts in the loaded fragment
  executeScripts(container) {
    const scripts = container.querySelectorAll('script');
    scripts.forEach(oldScript => {
      const newScript = document.createElement('script');
      Array.from(oldScript.attributes).forEach(attr => {
        newScript.setAttribute(attr.name, attr.value);
      });
      newScript.textContent = oldScript.textContent;
      oldScript.parentNode.replaceChild(newScript, oldScript);
    });
  }

  // Load fragments in parallel 
  async initCommonFragments() {
    // Start both loads simultaneously
    const headerPromise = this.loadFragment('navi-bar.html', '#header-fragment');
    const footerPromise = this.loadFragment('footer.html', '#footer-fragment');

    // Wait for both to complete
    await Promise.all([headerPromise, footerPromise]);

    // Initialize menu after header is loaded
    this.initMobileMenu();
  }


// Load with visual feedback
  initMobileMenu() {
    const hamburger = document.querySelector('.hamburger-menu');
    const mobileNav = document.querySelector('.mobile-nav');

    if (hamburger && mobileNav) {
      hamburger.addEventListener('click', function () {
        hamburger.classList.toggle('open');
        mobileNav.classList.toggle('open');
      });
    }
  }
  // Update the active navigation link based on the current URL
  updateActiveNavLink() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-links a, .mobile-nav-links a');

    navLinks.forEach(link => {
      const linkPath = new URL(link.href).pathname;
      if (
          currentPath === linkPath ||
          (currentPath === '/' && linkPath.endsWith('index.html')) ||
          (currentPath.endsWith('index.html') && linkPath === '/')
      ) {
        link.classList.add('active');
      } else {
        link.classList.remove('active');
      }
    });
  }

  // Preload fragments for instant loading for future navigations
  async preloadFragments() {
    const fragmentUrls = ['navi-bar.html', 'footer.html'];
    const preloadPromises = fragmentUrls.map(url =>
        fetch(url).then(response => response.text()).then(html => {
          this.cache.set(url, html);
        }).catch(error => console.warn(`Failed to preload ${url}:`, error))
    );

    await Promise.all(preloadPromises);
    console.log('Fragments preloaded');
  }
}

const fragmentLoader = new FragmentLoader();

document.addEventListener('DOMContentLoaded', async () => {
  // Use parallel loading for better performance
  await fragmentLoader.initCommonFragments();
  fragmentLoader.updateActiveNavLink();
});

//Preload fragments as soon as script loads
fragmentLoader.preloadFragments().then(r => window.fragmentLoader.initCommonFragments());

