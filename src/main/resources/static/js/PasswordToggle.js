class PasswordToggle {
  constructor() {
    // Initialize immediately if DOM is ready, otherwise wait for it
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', () => this.initialize());
    } else {
      this.initialize();
    }
  }

  initialize() {
    // Find all buttons with the password toggle data attribute
    const toggleButtons = document.querySelectorAll('[data-password-toggle]');

    toggleButtons.forEach(button => {
      // Set initial aria-label and title
      button.setAttribute('aria-label', 'Show password');
      button.setAttribute('title', 'Show password');

      // Add click event listener
      button.addEventListener('click', (event) => {
        event.preventDefault(); // Prevent form submission
        const inputId = button.getAttribute('data-password-toggle');
        this.toggle(inputId);
      });
    });

    console.log(`PasswordToggle initialized for ${toggleButtons.length} button(s)`);
  }

  toggle(inputId) {
    const passwordInput = document.getElementById(inputId);
    const toggleIcon = document.querySelector(`[data-password-icon="${inputId}"]`);
    const toggleButton = toggleIcon?.parentElement;

    if (!passwordInput || !toggleIcon || !toggleButton) {
      console.warn(`PasswordToggle: Could not find elements for input ID: ${inputId}`);
      return;
    }

    const isCurrentlyHidden = passwordInput.type === 'password';

    // Toggle the input type
    passwordInput.type = isCurrentlyHidden ? 'text' : 'password';

    // Toggle the icon classes
    if (isCurrentlyHidden) {
      // Password is now visible
      toggleIcon.classList.remove('fa-eye');
      toggleIcon.classList.add('fa-eye-slash');
      toggleButton.setAttribute('aria-label', 'Hide password');
      toggleButton.setAttribute('title', 'Hide password');
    } else {
      // Password is now hidden
      toggleIcon.classList.remove('fa-eye-slash');
      toggleIcon.classList.add('fa-eye');
      toggleButton.setAttribute('aria-label', 'Show password');
      toggleButton.setAttribute('title', 'Show password');
    }
  }
}

// Create and initialize the PasswordToggle instance
const passwordToggler = new PasswordToggle();