/**
 * Dive Log Modal Operations
 * Handles View, Edit, and Delete operations via REST API
 */

let currentDiveId = null;

// View Dive Log
function viewDive(id) {
  currentDiveId = id;

  fetch(`/api/dive-logs/${id}`, {
    method: 'GET',
    headers: {
      'Accept': 'application/json'
    }
  })
  .then(response => {
    if (!response.ok) {
      throw new Error('Failed to load dive log');
    }
    return response.json();
  })
  .then(dive => {
    const content = `
                  <div class="dive-detail-grid">
                      <div class="dive-detail">
                          <div class="dive-detail-label">Dive Number</div>
                          <div class="dive-detail-value">#${dive.diveNumber}</div>
                      </div>
                      <div class="dive-detail">
                          <div class="dive-detail-label">Location</div>
                          <div class="dive-detail-value">${escapeHtml(
        dive.location)}</div>
                      </div>
                      <div class="dive-detail">
                          <div class="dive-detail-label">Date</div>
                          <div class="dive-detail-value">${formatDate(
        dive.diveDate)}</div>
                      </div>
                      <div class="dive-detail">
                          <div class="dive-detail-label">Duration</div>
                          <div class="dive-detail-value">${formatDuration(
        dive.duration)}</div>
                      </div>
                      <div class="dive-detail">
                          <div class="dive-detail-label">Start Time</div>
                          <div class="dive-detail-value">${formatTimeOnly(
        dive.startTime)}</div>
                      </div>
                      <div class="dive-detail">
                          <div class="dive-detail-label">End Time</div>
                          <div class="dive-detail-value">${formatTimeOnly(
        dive.endTime)}</div>
                      </div>
                      <div class="dive-detail">
                          <div class="dive-detail-label">Water Temperature</div>
                          <div class="dive-detail-value">${dive.waterTemperature
        ? dive.waterTemperature.toFixed(1) + '°C' : 'Not recorded'}</div>
                      </div>
                      <div class="dive-detail">
                          <div class="dive-detail-label">Air Temperature</div>
                          <div class="dive-detail-value">${dive.airTemperature
        ? dive.airTemperature.toFixed(1) + '°C' : 'Not recorded'}</div>
                      </div>
                  </div>
                  ${dive.notes ? `
                      <div class="dive-detail" style="margin-top: var(--space-xl);">
                          <div class="dive-detail-label">Notes</div>
                          <div class="dive-detail-value" style="white-space: pre-wrap;">${escapeHtml(
        dive.notes)}</div>
                      </div>
                  ` : ''}
              `;

    document.getElementById('viewDiveContent').innerHTML = content;
    document.getElementById('viewDiveModal').style.display = 'block';
  })
  .catch(error => {
    console.error('Error fetching dive log:', error);
    alert('Failed to load dive log details. Please try again.');
  });
}

// Edit Dive Log
function editDive(id) {
  currentDiveId = id;

  fetch(`/api/dive-logs/${id}`, {
    method: 'GET',
    headers: {
      'Accept': 'application/json'
    }
  })
  .then(response => {
    if (!response.ok) {
      throw new Error('Failed to load dive log');
    }
    return response.json();
  })
  .then(dive => {
    // Populate form fields
    document.getElementById('editDiveId').value = dive.id;
    document.getElementById('editDiveNumber').value = dive.diveNumber;
    document.getElementById('editLocation').value = dive.location;
    document.getElementById('editDiveDate').value = dive.diveDate;
    document.getElementById('editStartTime').value = dive.startTime;
    document.getElementById('editEndTime').value = dive.endTime;
    document.getElementById('editWaterTemp').value = dive.waterTemperature
        || '';
    document.getElementById('editAirTemp').value = dive.airTemperature || '';
    document.getElementById('editNotes').value = dive.notes || '';

    // Update character count and calculate duration
    updateCharCount();
    calculateEditDuration();

    // Show modal
    document.getElementById('editDiveModal').style.display = 'block';
  })
  .catch(error => {
    console.error('Error fetching dive log:', error);
    alert('Failed to load dive log for editing. Please try again.');
  });
}

// Edit from view modal
function editDiveFromView() {
  closeViewModal();
  editDive(currentDiveId);
}

// Save Dive Log
function saveDiveLog() {
  const form = document.getElementById('editDiveForm');
  const formData = new FormData(form);

  // Build the data object
  const diveData = {
    id: parseInt(formData.get('id')),
    diveNumber: parseInt(formData.get('diveNumber')),
    location: formData.get('location'),
    diveDate: formData.get('diveDate'),
    startTime: formData.get('startTime'),
    endTime: formData.get('endTime'),
    waterTemperature: formData.get('waterTemperature') ? parseFloat(
        formData.get('waterTemperature')) : null,
    airTemperature: formData.get('airTemperature') ? parseFloat(
        formData.get('airTemperature')) : null,
    notes: formData.get('notes')
  };

  // Clear previous errors
  document.getElementById('editFormError').style.display = 'none';

  // Send PUT request
  fetch(`/api/dive-logs/${diveData.id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    body: JSON.stringify(diveData)
  })
  .then(response => {
    if (!response.ok) {
      return response.json().then(data => {
        throw new Error(data.error || 'Failed to update dive log');
      });
    }
    return response.json();
  })
  .then(updatedDive => {
    closeEditModal();
    // Reload the page to show updated data
    window.location.reload();
  })
  .catch(error => {
    document.getElementById('editFormError').textContent = error.message;
    document.getElementById('editFormError').style.display = 'block';
  });
}

// Delete Dive Log (enhanced version with API)
function deleteDive(id) {
  // First, try the API approach
  fetch(`/api/dive-logs/${id}`, {
    method: 'DELETE',
    headers: {
      'Accept': 'application/json'
    }
  })
  .then(response => {
    if (!response.ok) {
      throw new Error('Failed to delete dive log');
    }
    return response.json();
  })
  .then(data => {
    // Reload the page to show updated list
    window.location.reload();
  })
  .catch(error => {
    console.error('API delete failed, falling back to form submission:', error);
    // If API fails, the form submission will handle it
  });

  // Return false to prevent form submission if API succeeds
  return false;
}

// Modal control functions
function closeViewModal() {
  document.getElementById('viewDiveModal').style.display = 'none';
  currentDiveId = null;
}

function closeEditModal() {
  document.getElementById('editDiveModal').style.display = 'none';
  document.getElementById('editFormError').style.display = 'none';
  currentDiveId = null;
}

// Helper functions
function updateCharCount() {
  const notes = document.getElementById('editNotes').value;
  const charCount = notes ? notes.length : 0;
  document.querySelector(
      '.char-count').textContent = `${charCount} / 2000 characters`;
}

function calculateEditDuration() {
  const startTime = document.getElementById('editStartTime').value;
  const endTime = document.getElementById('editEndTime').value;

  if (startTime && endTime) {
    // Parse HH:mm or HH:mm:ss
    const [sh, sm] = startTime.split(':');
    const [eh, em] = endTime.split(':');
    if (sh !== undefined && sm !== undefined && eh !== undefined && em
        !== undefined) {
      const startMinutes = parseInt(sh, 10) * 60 + parseInt(sm, 10);
      const endMinutes = parseInt(eh, 10) * 60 + parseInt(em, 10);
      if (endMinutes > startMinutes) {
        const durationMinutes = endMinutes - startMinutes;
        document.getElementById('durationDisplay').textContent = formatDuration(
            durationMinutes);
        document.getElementById('editDuration').style.display = 'block';
      } else {
        document.getElementById('editDuration').style.display = 'none';
      }
    } else {
      document.getElementById('editDuration').style.display = 'none';
    }
  }
}

function formatDuration(minutes) {
  if (!minutes) {
    return '0 min';
  }

  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;

  if (hours > 0) {
    return `${hours}h ${mins.toString().padStart(2, '0')}m`;
  } else {
    return `${mins} min`;
  }
}

function formatDate(dateString) {
  if (!dateString) {
    return 'Not recorded';
  }
  const date = new Date(dateString);
  if (isNaN(date.getTime())) {
    return 'Invalid date';
  }
  return date.toLocaleDateString(navigator.language, {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
}

// Use only for date+time, not for time-only fields
function formatDateTime(dateTimeString) {
  if (!dateTimeString) {
    return 'Not recorded';
  }
  const dateTime = new Date(dateTimeString);
  if (isNaN(dateTime.getTime())) {
    return 'Invalid date';
  }
  return dateTime.toLocaleString(navigator.language, {
    hour: '2-digit',
    minute: '2-digit'
  });
}

// Handles time-only strings like "12:00:00.000000" or "12:00:00" or "12:00"
function formatTimeOnly(timeString) {
  if (!timeString) {
    return 'Not recorded';
  }
  const [h, m] = timeString.split(':');
  if (!h || !m) {
    return 'Invalid time';
  }
  return `${h.padStart(2, '0')}:${m.padStart(2, '0')}`;
}

function escapeHtml(text) {
  if (!text) {
    return '';
  }
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  return text.replace(/[&<>"']/g, m => map[m]);
}

// Event listeners
document.addEventListener('DOMContentLoaded', function () {
  // Notes textarea character count
  const notesTextarea = document.getElementById('editNotes');
  if (notesTextarea) {
    notesTextarea.addEventListener('input', updateCharCount);
  }

  // Time inputs for duration calculation
  const startTimeInput = document.getElementById('editStartTime');
  const endTimeInput = document.getElementById('editEndTime');
  if (startTimeInput) {
    startTimeInput.addEventListener('change', calculateEditDuration);
  }
  if (endTimeInput) {
    endTimeInput.addEventListener('change', calculateEditDuration);
  }

  // Close modals when clicking outside
  window.onclick = function (event) {
    const viewModal = document.getElementById('viewDiveModal');
    const editModal = document.getElementById('editDiveModal');

    if (event.target === viewModal) {
      closeViewModal();
    }
    if (event.target === editModal) {
      closeEditModal();
    }
  };

  // Escape key to close modals
  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
      const viewModal = document.getElementById('viewDiveModal');
      const editModal = document.getElementById('editDiveModal');

      if (viewModal.style.display === 'block') {
        closeViewModal();
      }
      if (editModal.style.display === 'block') {
        closeEditModal();
      }
    }
  });

  // Override delete form submissions to use API
  const deleteForms = document.querySelectorAll('.delete-form');
  deleteForms.forEach(form => {
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      const actionUrl = this.action;
      const id = actionUrl.substring(actionUrl.lastIndexOf('/') + 1);
      deleteDive(id);
    });
  });
});