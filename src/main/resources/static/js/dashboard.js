// dashboard.js

// ✅ CSRF (dynamic from meta)
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

// ✅ Update Task Status
function updateStatus(select, taskId) {
    const newStatus = select.value;
    const row = select.closest('tr');

    fetch(`/employee/tasks/${taskId}/update-status`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({ status: newStatus })
    })
    .then(res => res.json())
    .then(data => {
        if (!data.success) throw new Error(data.message || 'Update failed');

        // ✅ Update badge UI
        const badge = row.querySelector('.badge');
        if (badge) {
            badge.textContent = newStatus;
            badge.className = 'badge ' +
                (newStatus === 'Completed' ? 'completed' :
                 newStatus === 'In Progress' ? 'progress' :
                 'pending');
        }

        // ✅ Save previous value
        select.setAttribute('data-prev', newStatus);

        showToast('Task updated successfully', 'success');

        // ✅ Update stats instantly
        refreshStats();
    })
    .catch(err => {
        showToast('Error: ' + err.message, 'error');
        select.value = select.getAttribute('data-prev');
    });
}

// ✅ Toast (if not already present)
function showToast(message, type) {
    const toast = document.createElement('div');
    toast.className = 'toast ' + (type === 'success' ? 'toast-success' : 'toast-error');
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => toast.remove(), 3000);
}

// ✅ Refresh stats from UI (NO API call needed)
function refreshStats() {
    const rows = document.querySelectorAll('.task-row');

    let total = 0, completed = 0, pending = 0;

    rows.forEach(row => {
        if (row.style.display === 'none') return;

        total++;
        const status = row.querySelector('.badge').textContent.trim();

        if (status === 'Completed') completed++;
        else if (status === 'Pending') pending++;
    });

    document.querySelector('#totalTasks').textContent = total;
    document.querySelector('#completedTasks').textContent = completed;
    document.querySelector('#pendingTasks').textContent = pending;
}

// ✅ Filter tasks (UPDATED → works with table rows)
function filterTasks(statusFilter) {
    const rows = document.querySelectorAll('.task-row');

    rows.forEach(row => {
        const status = row.querySelector('.badge').textContent.trim();

        if (statusFilter === 'All' || status === statusFilter) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });

    // update stats based on filtered rows
    refreshStats();
}

// ✅ Initialize
document.addEventListener('DOMContentLoaded', () => {
    const totalCard = document.querySelector('#totalCard');
    const completedCard = document.querySelector('#completedCard');
    const pendingCard = document.querySelector('#pendingCard');

    if (totalCard) totalCard.addEventListener('click', () => filterTasks('All'));
    if (completedCard) completedCard.addEventListener('click', () => filterTasks('Completed'));
    if (pendingCard) pendingCard.addEventListener('click', () => filterTasks('Pending'));

    // Initial stats
    refreshStats();
});