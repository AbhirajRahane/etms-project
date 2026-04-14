document.addEventListener('DOMContentLoaded', () => {
    const table = document.querySelector('table');
    const totalCard = document.querySelector('#totalCard h3');
    const completedCard = document.querySelector('#completedCard h3');
    const pendingCard = document.querySelector('#pendingCard h3');

    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // ===================== INLINE EDIT =====================
    table.addEventListener('change', async e => {
        const target = e.target;
        const row = target.closest('tr');
        if (!row) return;
        const taskId = row.dataset.taskId;
        let payload = {};
        if (target.classList.contains('inline-title')) payload.title = target.value;
        else if (target.classList.contains('inline-status')) payload.status = target.value;
        else if (target.classList.contains('inline-assigned')) payload.assignedToId = target.value;
        else return;

        try {
            const res = await fetch(`/manager/tasks/${taskId}/update-inline`, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (data.success) {
                row.classList.add('highlight');
                setTimeout(() => row.classList.remove('highlight'), 500);
                showToast('Task updated successfully', 'green');
                refreshTaskCounts();
            } else showToast('Task update failed', 'red');
        } catch (err) { console.error(err); showToast('Task update failed', 'red'); }
    });

    // ===================== DELETE =====================
    table.addEventListener('click', async e => {
        const target = e.target;
        if (!target.classList.contains('delete-btn')) return;

        const row = target.closest('tr');
        const taskId = row.dataset.taskId;
        if (!confirm('Are you sure you want to delete this task?')) return;

        try {
            const res = await fetch(`/manager/delete-task/${taskId}`, { 
                method: 'DELETE',
                headers: { [csrfHeader]: csrfToken }
            });
            const data = await res.json();
            if (data.success) {
                row.remove();
                showToast('Task deleted successfully', 'green');
                refreshTaskCounts();
            } else showToast('Task delete failed', 'red');
        } catch (err) { console.error(err); showToast('Task delete failed', 'red'); }
    });

    // ===================== CREATE TASK =====================
    const addBtn = document.getElementById('addTaskBtn');
    const createModal = document.getElementById('createTaskModal');
    addBtn.onclick = () => createModal.style.display = 'flex';
    createModal.querySelector('.cancel-btn').onclick = () => createModal.style.display = 'none';

    createModal.querySelector('.save-btn').onclick = async () => {
        const title = document.getElementById('newTaskTitle').value.trim();
        const description = document.getElementById('newTaskDesc').value.trim();
        const status = document.getElementById('newTaskStatus').value;
        const assigned = document.getElementById('newTaskAssigned').value;

        if (!title || !status || !assigned) {
            showToast('Please fill all fields', 'red');
            return;
        }

        const payload = { title, description, status, userId: assigned };

        try {
            const res = await fetch(`/manager/save-task`, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(payload)
            });
            const data = await res.json();

            if (data.success) {
                const task = data.task;
                const tbody = document.querySelector('#taskTableBody');
                const newRow = document.createElement('tr');
                newRow.classList.add('task-row');
                newRow.dataset.taskId = task.id;
                newRow.innerHTML = `
                    <td><input type="text" class="inline-edit inline-title" value="${task.title}"></td>
                    <td>
                        <select class="inline-edit inline-status">
                            <option ${task.status==='Pending'?'selected':''}>Pending</option>
                            <option ${task.status==='In Progress'?'selected':''}>In Progress</option>
                            <option ${task.status==='Completed'?'selected':''}>Completed</option>
                        </select>
                    </td>
                    <td>
                        <select class="inline-edit inline-assigned">
                            <option value="${task.assignedTo.id}" selected>${task.assignedTo.email}</option>
                        </select>
                    </td>
                    <td><button class="btn btn-danger delete-btn">Delete</button></td>
                `;
                tbody.prepend(newRow);
                attachInlineEdit(newRow);
                newRow.querySelector('.delete-btn').addEventListener('click', async e => {
                    const row = e.target.closest('tr');
                    const taskId = row.dataset.taskId;
                    if (!confirm('Are you sure you want to delete this task?')) return;
                    try {
                        const res = await fetch(`/manager/delete-task/${taskId}`, { 
                            method: 'DELETE',
                            headers: { [csrfHeader]: csrfToken }
                        });
                        const data = await res.json();
                        if (data.success) {
                            row.remove();
                            showToast('Task deleted successfully', 'green');
                            refreshTaskCounts();
                        } else showToast('Task delete failed', 'red');
                    } catch (err) { console.error(err); showToast('Task delete failed', 'red'); }
                });

                showToast('Task added successfully', 'green');
                refreshTaskCounts();
                createModal.style.display = 'none';

                // Clear inputs
                document.getElementById('newTaskTitle').value = '';
                document.getElementById('newTaskDesc').value = '';
                document.getElementById('newTaskStatus').value = 'Pending';
                document.getElementById('newTaskAssigned').value = '';

            } else {
                showToast(data.message || 'Task creation failed', 'red');
            }
        } catch (err) {
            console.error(err);
            showToast('Task creation failed', 'red');
        }
    };

    // ===================== INLINE EDIT ATTACH =====================
    function attachInlineEdit(row){
        const taskId = row.dataset.taskId;
        const title = row.querySelector('.inline-title');
        const status = row.querySelector('.inline-status');
        const assigned = row.querySelector('.inline-assigned');
        [title,status,assigned].forEach(el => {
            el.addEventListener('change', async () => {
                const payload = {
                    title: title.value,
                    status: status.value,
                    assignedToId: assigned.value
                };
                try {
                    const res = await fetch(`/manager/tasks/${taskId}/update-inline`, {
                        method: 'POST',
                        headers: { 'Content-Type':'application/json', [csrfHeader]: csrfToken },
                        body: JSON.stringify(payload)
                    });
                    const data = await res.json();
                    if(data.success) showToast('Task updated', 'green');
                    else showToast('Error updating', 'red');
                    refreshTaskCounts();
                } catch (err) { console.error(err); showToast('Error updating', 'red'); }
            });
        });
    }
    document.querySelectorAll('.task-row').forEach(attachInlineEdit);

    // ===================== FILTER =====================
    document.querySelectorAll('.stat-card').forEach(card => {
        card.addEventListener('click', () => {
            const filter = card.dataset.filter;
            document.querySelectorAll('.task-row').forEach(row => {
                const status = row.querySelector('.inline-status').value;
                row.style.display = (filter === 'All' || status === filter) ? '' : 'none';
            });
        });
    });

    // ===================== TASK COUNTS =====================
    async function refreshTaskCounts() {
        try {
            const res = await fetch('/manager/task-counts');
            const data = await res.json();
            totalCard.textContent = data.total ?? 0;
            completedCard.textContent = data.completed ?? 0;
            pendingCard.textContent = data.pending ?? 0;
        } catch (err) { console.error(err); }
    }

    // ===================== TOAST =====================
    function showToast(message, color) {
        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.style.background = color;
        toast.textContent = message;
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 2000);
    }

    // Initial task counts
    refreshTaskCounts();
});

// ===================== FILTER =====================
document.querySelectorAll('.stat-card').forEach(card => {
    card.addEventListener('click', () => {
        const filter = card.dataset.filter;
        let total = 0, completed = 0, pending = 0;

        document.querySelectorAll('.task-row').forEach(row => {
            const status = row.querySelector('.inline-status').value;

            // Show or hide row based on filter
            row.style.display = (filter === 'All' || status === filter) ? '' : 'none';

            // Count only visible rows
            if (row.style.display !== 'none') {
                total++;
                if (status === 'Completed') completed++;
                if (status === 'Pending') pending++;
            }
        });

        // Update card numbers dynamically
        totalCard.textContent = total;
        completedCard.textContent = completed;
        pendingCard.textContent = pending;
    });
});