let currentDiveNumber = null;

// View Dive Log
function viewDive(diveNumber) {
  currentDiveNumber = diveNumber;

  fetch(`/api/dive-logs/${diveNumber}`, {
    method: 'GET',
    headers: {
      'Accept': 'application/json'
    }
  })
  .then(response => {
    if (!response.ok) {
      throw new Error(`Failed to load dive log: ${response.status} ${response.statusText}`);
    }
    return response.json();
  })
  .then(dive => {
    document.getElementById('viewDiveContent').innerHTML = generateDiveViewContent(dive);
    document.getElementById('viewDiveModal').style.display = 'block';
  })
  .catch(error => {
    alert('Failed to load dive log details. Please try again.');
  });
}

// Generate dive view
function generateDiveViewContent(dive) {
  return `
    <div class="dive-detail-grid">
        <div class="dive-detail">
            <div class="dive-detail-label">Dive Number</div>
            <div class="dive-detail-value">#${safeValue(dive.diveNumber, 'Unknown')}</div>
        </div>
        <div class="dive-detail">
            <div class="dive-detail-label">Location</div>
            <div class="dive-detail-value">${escapeHtml(safeValue(dive.location, 'Unknown'))}</div>
        </div>
        <div class="dive-detail">
            <div class="dive-detail-label">Date</div>
            <div class="dive-detail-value">${formatDate(dive.diveDate)}</div>
        </div>
        <div class="dive-detail">
            <div class="dive-detail-label">Duration</div>
            <div class="dive-detail-value">${formatDuration(dive.duration)}</div>
        </div>
        <div class="dive-detail">
            <div class="dive-detail-label">Start Time</div>
            <div class="dive-detail-value">${formatTimeOnly(dive.startTime)}</div>
        </div>
        <div class="dive-detail">
            <div class="dive-detail-label">End Time</div>
            <div class="dive-detail-value">${formatTimeOnly(dive.endTime)}</div>
        </div>
        <div class="dive-detail">
            <div class="dive-detail-label">Water Temperature</div>
            <div class="dive-detail-value">${formatTemperature(dive.waterTemperature)}</div>
        </div>
        <div class="dive-detail">
            <div class="dive-detail-label">Air Temperature</div>
            <div class="dive-detail-value">${formatTemperature(dive.airTemperature)}</div>
        </div>
    </div>
    ${dive.notes ? `
        <div class="dive-detail" style="margin-top: var(--space-xl);">
            <div class="dive-detail-label">Notes</div>
            <div class="dive-detail-value" style="white-space: pre-wrap;">${escapeHtml(dive.notes)}</div>
        </div>
    ` : ''}
  `;
}

// Edit Dive Log
function editDive(diveNumber) {
  currentDiveNumber = diveNumber;

  fetch(`/api/dive-logs/${diveNumber}`, {
    method: 'GET',
    headers: {
      'Accept': 'application/json'
    }
  })
  .then(response => {
    if (!response.ok) {
      throw new Error(`Failed to load dive log: ${response.status}`);
    }
    return response.json();
  })
  .then(dive => {
    populateEditForm(dive);
    document.getElementById('editDiveModal').style.display = 'block';
  })
  .catch(error => {
    alert('Failed to load dive log for editing. Please try again.');
  });
}

function populateEditForm(dive) {
  document.getElementById('editDiveId').value = safeValue(dive.id, '');
  document.getElementById('editDiveNumber').value = safeValue(dive.diveNumber, '');
  document.getElementById('editLocation').value = safeValue(dive.location, '');
  document.getElementById('editDiveDate').value = formatDateForInput(dive.diveDate);
  document.getElementById('editStartTime').value = formatTimeForInput(dive.startTime);
  document.getElementById('editEndTime').value = formatTimeForInput(dive.endTime);
  document.getElementById('editWaterTemp').value = dive.waterTemperature || '';
  document.getElementById('editAirTemp').value = dive.airTemperature || '';
  document.getElementById('editNotes').value = safeValue(dive.notes, '');
  updateCharCount();
  calculateEditDuration();
}

function formatDateForInput(dateValue) {
  if (!dateValue) return '';

  if (Array.isArray(dateValue) && dateValue.length >= 3) {
    const year = dateValue[0];
    const month = dateValue[1].toString().padStart(2, '0');
    const day = dateValue[2].toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  if (typeof dateValue === 'string' && dateValue.match(/^\d{4}-\d{2}-\d{2}$/)) {
    return dateValue;
  }

  if (typeof dateValue === 'object' && dateValue.year && dateValue.month && dateValue.day) {
    const year = dateValue.year;
    const month = dateValue.month.toString().padStart(2, '0');
    const day = dateValue.day.toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  if (typeof dateValue === 'string') {
    const date = new Date(dateValue);
    if (!isNaN(date.getTime())) {
      return date.toISOString().split('T')[0];
    }
  }

  return '';
}

// Save Dive Log
function saveDiveLog() {
  const form = document.getElementById('editDiveForm');
  const formData = new FormData(form);
  const diveData = buildDiveDataFromForm(formData)

  if (!diveData) return;

  document.getElementById('editFormError').style.display = 'none';

  fetch(`/api/dive-logs/${currentDiveNumber}`, {
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
        throw new Error(data.error || data.message || `Save failed: ${response.status}`);
      });
    }
    return response.json();
  })
  .then(updatedDive => {
    closeEditModal();
    window.location.reload();
  })
  .catch(error => {
    document.getElementById('editFormError').textContent = error.message;
    document.getElementById('editFormError').style.display = 'block';
  });
}

function buildDiveDataFromForm(formData) {
  const diveData = {
    id: parseInt(formData.get('id')),
    diveNumber: parseInt(formData.get('diveNumber')),
    location: formData.get('location'),
    diveDate: formData.get('diveDate'),
    startTime: formData.get('startTime'),
    endTime: formData.get('endTime'),
    waterTemperature: formData.get('waterTemperature') ? parseFloat(formData.get('waterTemperature')) : null,
    airTemperature: formData.get('airTemperature') ? parseFloat(formData.get('airTemperature')) : null,
    notes: formData.get('notes')
  };

  if (!diveData.id || !diveData.diveNumber || !diveData.location || !diveData.diveDate) {
    document.getElementById('editFormError').textContent = 'Please fill in all required fields';
    document.getElementById('editFormError').style.display = 'block';
    return null;
  }

  return diveData;
}

function deleteDive(diveNumber) {
  if (!confirm('Are you sure you want to delete this dive log?')) {
    return false;
  }

  fetch(`/api/dive-logs/${diveNumber}`, {
    method: 'DELETE',
    headers: {
      'Accept': 'application/json'
    }
  })
  .then(response => {
    if (!response.ok) {
      throw new Error(`Delete failed: ${response.status}`);
    }
    return response.json();
  })
  .then(data => {
    window.location.reload();
  })
  .catch(error => {
    alert('Failed to delete dive log. Please try again.');
  });

  return false;
}

function closeViewModal() {
  document.getElementById('viewDiveModal').style.display = 'none';
  currentDiveNumber = null;
}

function closeEditModal() {
  document.getElementById('editDiveModal').style.display = 'none';
  document.getElementById('editFormError').style.display = 'none';
  currentDiveNumber = null;
}

function updateCharCount() {
  const notes = document.getElementById('editNotes').value;
  const charCount = notes ? notes.length : 0;
  document.querySelector('.char-count').textContent = `${charCount} / 2000 characters`;
}

function calculateEditDuration() {
  const startTime = document.getElementById('editStartTime').value;
  const endTime = document.getElementById('editEndTime').value;

  if (startTime && endTime) {
    const [sh, sm] = startTime.split(':').map(Number);
    const [eh, em] = endTime.split(':').map(Number);

    if (!isNaN(sh) && !isNaN(sm) && !isNaN(eh) && !isNaN(em)) {
      const startMinutes = sh * 60 + sm;
      const endMinutes = eh * 60 + em;

      if (endMinutes > startMinutes) {
        const durationMinutes = endMinutes - startMinutes;
        document.getElementById('durationDisplay').textContent = formatDuration(durationMinutes);
        document.getElementById('editDuration').style.display = 'block';
      } else {
        document.getElementById('editDuration').style.display = 'none';
      }
    } else {
      document.getElementById('editDuration').style.display = 'none';
    }
  } else {
    document.getElementById('editDuration').style.display = 'none';
  }
}

function safeValue(value, defaultValue = 'Not recorded') {
  return value !== null && value !== undefined && value !== '' ? value : defaultValue;
}

function formatDuration(minutes) {
  if (!minutes || minutes <= 0) return '0 min';

  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;

  if (hours > 0) {
    return `${hours}h ${mins.toString().padStart(2, '0')}m`;
  } else {
    return `${mins} min`;
  }
}

function formatDate(dateString) {
  if (!dateString) return 'Not recorded';

  if (Array.isArray(dateString) && dateString.length >= 3) {
    const year = dateString[0];
    const month = dateString[1];
    const day = dateString[2];
    const date = new Date(year, month - 1, day);
    return date.toLocaleDateString(navigator.language, {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  const date = new Date(dateString);
  if (isNaN(date.getTime())) return 'Invalid date';

  return date.toLocaleDateString(navigator.language, {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
}

function formatTemperature(temp) {
  if (temp === null || temp === undefined) return 'Not recorded';
  return `${temp.toFixed(1)}°C`;
}

function formatTimeOnly(timeString) {
  if (!timeString) return 'Not recorded';

  if (Array.isArray(timeString)) {
    if (timeString.length >= 2) {
      const hours = timeString[0].toString().padStart(2, '0');
      const minutes = timeString[1].toString().padStart(2, '0');
      return `${hours}:${minutes}`;
    }
    return 'Invalid time';
  }

  if (typeof timeString === 'string') {
    const parts = timeString.split(':');
    if (parts.length >= 2) {
      const hours = parseInt(parts[0], 10);
      const minutes = parseInt(parts[1], 10);

      if (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59) {
        return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
      }
    }
  }

  if (typeof timeString === 'object' && timeString !== null) {
    if (timeString.hour !== undefined && timeString.minute !== undefined) {
      const hours = timeString.hour.toString().padStart(2, '0');
      const minutes = timeString.minute.toString().padStart(2, '0');
      return `${hours}:${minutes}`;
    }
  }

  return 'Invalid time';
}

function formatTimeForInput(timeString) {
  if (!timeString) return '';

  if (Array.isArray(timeString)) {
    if (timeString.length >= 2) {
      const hours = timeString[0].toString().padStart(2, '0');
      const minutes = timeString[1].toString().padStart(2, '0');
      return `${hours}:${minutes}`;
    }
    return '';
  }

  if (typeof timeString === 'string') {
    const parts = timeString.split(':');
    if (parts.length >= 2) {
      const hours = parseInt(parts[0], 10);
      const minutes = parseInt(parts[1], 10);

      if (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59) {
        return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
      }
    }
  }

  if (typeof timeString === 'object') {
    if (timeString.hour !== undefined && timeString.minute !== undefined) {
      const hours = timeString.hour.toString().padStart(2, '0');
      const minutes = timeString.minute.toString().padStart(2, '0');
      return `${hours}:${minutes}`;
    }
  }

  return '';
}

function escapeHtml(text) {
  if (!text) return '';

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
  const notesTextarea = document.getElementById('editNotes');
  if (notesTextarea) {
    notesTextarea.addEventListener('input', updateCharCount);
  }

  const startTimeInput = document.getElementById('editStartTime');
  const endTimeInput = document.getElementById('editEndTime');
  if (startTimeInput) {
    startTimeInput.addEventListener('change', calculateEditDuration);
  }
  if (endTimeInput) {
    endTimeInput.addEventListener('change', calculateEditDuration);
  }

  // Override delete form submissions to use API
  const deleteForms = document.querySelectorAll('.delete-form');
  deleteForms.forEach(form => {
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      const actionUrl = this.action;
      const diveNumber = actionUrl.substring(actionUrl.lastIndexOf('/') + 1);
      deleteDive(diveNumber);
    });
  });
});