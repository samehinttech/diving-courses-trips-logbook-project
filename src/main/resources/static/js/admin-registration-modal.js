document.addEventListener('DOMContentLoaded', function () {
  initializeModalHandling();
  initializeFormHandling();
});

function initializeModalHandling() {
  const modalTriggers = document.querySelectorAll('[data-modal-target]');
  modalTriggers.forEach(trigger => {
    trigger.addEventListener('click', function (e) {
      e.preventDefault();
      const targetSelector = this.getAttribute('data-modal-target');
      const modal = document.querySelector(targetSelector);
      if (modal) {
        modal.style.display = 'flex';
        clearFormErrors();
      }
    });
  });

  const modalCloses = document.querySelectorAll('[data-modal-close]');
  modalCloses.forEach(close => {
    close.addEventListener('click', function () {
      const modal = this.closest('.message-modal-overlay');
      if (modal) {
        modal.style.display = 'none';
      }
    });
  });

  window.addEventListener('click', function (e) {
    if (e.target.classList.contains('message-modal-overlay')) {
      e.target.style.display = 'none';
    }
  });
}

function initializeFormHandling() {
  const adminForm = document.querySelector('form[action*="/admin/register"]');
  if (adminForm) {
    adminForm.addEventListener('submit', function (e) {
      clearFormErrors();
    });
  }
}

function closeMessageModal(button) {
  const modal = button.closest('.message-modal-overlay');
  if (modal) {
    modal.style.display = 'none';

    const registrationForm = document.querySelector(
        'form[action*="/admin/register"]');
    if (registrationForm) {
      registrationForm.reset();
      clearFormErrors();
    }

    const registrationModal = document.getElementById(
        'admin-registration-modal');
    if (registrationModal) {
      registrationModal.style.display = 'none';
    }
  }
}

function clearFormErrors() {
  const errorInputs = document.querySelectorAll('.form-input.error');
  errorInputs.forEach(input => {
    input.classList.remove('error');
  });
  const errorMessages = document.querySelectorAll('.field-error');
  errorMessages.forEach(error => {
    error.style.display = 'none';
  });
}

function clearForm(formSelector) {
  const form = document.querySelector(formSelector);
  if (form) {
    form.reset();
    clearFormErrors();
  }
}

function showNotification(message, type = 'info') {
  const notification = document.createElement('div');
  notification.className = `message-modal-overlay`;
  notification.innerHTML = `
        <div class="message-modal ${type}">
            <div class="message-modal-icon">
                <i class="fas ${type === 'success' ? 'fa-check-circle'
      : 'fa-exclamation-circle'}"></i>
            </div>
            <h3 class="message-modal-title">${type === 'success' ? 'Success!'
      : 'Error'}</h3>
            <p class="message-modal-content">${message}</p>
            <a href="#" class="message-modal-close" onclick="this.closest('.message-modal-overlay').remove()">
                ${type === 'success' ? 'Continue' : 'Try Again'}
            </a>
        </div>
    `;

  document.body.appendChild(notification);

  setTimeout(() => {
    if (notification.parentNode) {
      notification.remove();
    }
  }, 5000);
}