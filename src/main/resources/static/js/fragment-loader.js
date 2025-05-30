class FragmentLoader {
  constructor() {
    this.cache = new Map();
  }

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

  async initCommonFragments() {
    await this.loadFragment('navi-bar.html', '#header-fragment');
    await this.loadFragment('footer.html', '#footer-fragment');
    this.initMobileMenu(); // Manually initialize menu
  }

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
}

const fragmentLoader = new FragmentLoader();

document.addEventListener('DOMContentLoaded', async () => {
  await fragmentLoader.initCommonFragments();
  fragmentLoader.updateActiveNavLink();
});

window.fragmentLoader = fragmentLoader;
