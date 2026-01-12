document.addEventListener('DOMContentLoaded', function() {

    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    const deleteButtons = document.querySelectorAll('[data-confirm]');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            const message = this.getAttribute('data-confirm') || 'Are you sure?';
            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });

    const textareas = document.querySelectorAll('textarea[maxlength]');
    textareas.forEach(textarea => {
        const maxLength = textarea.getAttribute('maxlength');
        const counterId = textarea.id + '-counter';

        const counter = document.createElement('small');
        counter.id = counterId;
        counter.className = 'form-text text-muted';
        counter.textContent = `0 / ${maxLength} characters`;
        textarea.parentNode.appendChild(counter);

        textarea.addEventListener('input', function() {
            const length = this.value.length;
            counter.textContent = `${length} / ${maxLength} characters`;

            if (length >= maxLength) {
                counter.classList.remove('text-muted');
                counter.classList.add('text-danger');
            } else {
                counter.classList.remove('text-danger');
                counter.classList.add('text-muted');
            }
        });
    });

    const fileInputs = document.querySelectorAll('input[type="file"][accept*="image"]');
    fileInputs.forEach(input => {
        input.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    const preview = document.createElement('img');
                    preview.src = e.target.result.toString();
                    preview.className = 'img-thumbnail mt-2';
                    preview.style.maxWidth = '200px';

                    const oldPreview = input.parentNode.querySelector('.img-thumbnail');
                    if (oldPreview) {
                        oldPreview.remove();
                    }

                    input.parentNode.appendChild(preview);
                };
                reader.readAsDataURL(file);
            }
        });
    });

    const scrollToTopBtn = document.getElementById('scrollToTop');
    if (scrollToTopBtn) {
        window.addEventListener('scroll', function() {
            if (window.scrollY > 300) {
                scrollToTopBtn.classList.add('show');
            } else {
                scrollToTopBtn.classList.remove('show');
            }
        });

        scrollToTopBtn.addEventListener('click', function() {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }

    const forms = document.querySelectorAll('.needs-validation');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!form.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });

    const ratingForms = document.querySelectorAll('form[action*="/matching/rate"]');
    ratingForms.forEach(form => {
        form.addEventListener('submit', function() {
            const button = this.querySelector('button[type="submit"]');
            button.disabled = true;
            button.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Processing...';
        });
    });

    const chatContainer = document.getElementById('messagesContainer');
    if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;

        const messageForm = document.querySelector('form[action*="/messages"]');
        if (messageForm) {
            messageForm.addEventListener('submit', function() {
                setTimeout(() => {
                    chatContainer.scrollTop = chatContainer.scrollHeight;
                }, 100);
            });
        }
    }

    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    [...tooltipTriggerList].forEach(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));

    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    [...popoverTriggerList].forEach(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));

    const formsWithLoading = document.querySelectorAll('form[data-loading]');
    formsWithLoading.forEach(form => {
        form.addEventListener('submit', function() {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                const originalText = submitBtn.innerHTML;
                submitBtn.setAttribute('data-original-text', originalText);
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Loading...';
            }
        });
    });

    const notificationDropdown = document.getElementById('notificationDropdown');
    if (notificationDropdown) {
        loadNotifications();
        setInterval(loadNotifications, 60000); // Refresh every minute

        const markAllReadBtn = document.getElementById('markAllRead');
        if (markAllReadBtn) {
            markAllReadBtn.addEventListener('click', function(e) {
                e.preventDefault();
                markAllNotificationsAsRead();
            });
        }
    }
});

function makeRequest(url, method = 'GET', data = null) {
    return fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: data ? JSON.stringify(data) : null
    })
    .then(response => response.json())
    .catch(error => {
        console.error('Request failed:', error);
        throw error;
    });
}

function loadNotifications() {
    fetch('/api/notifications/unread', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        credentials: 'include'
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                console.log('User not authenticated for notifications');
                return [];
            }
            throw new Error(`Failed to fetch notifications: ${response.status}`);
        }
        return response.json();
    })
    .then(notifications => {
        if (Array.isArray(notifications)) {
            updateNotificationBadge(notifications.length);
            updateNotificationList(notifications);
        }
    })
    .catch(error => {
        console.error('Error loading notifications:', error);
    });
}

function updateNotificationBadge(count) {
    const badge = document.getElementById('notificationBadge');
    if (badge) {
        if (count > 0) {
            badge.textContent = count > 99 ? '99+' : count;
            badge.style.display = 'inline-block';
        } else {
            badge.style.display = 'none';
        }
    }
}

function updateNotificationList(notifications) {
    const notificationList = document.getElementById('notificationList');
    if (!notificationList) return;

    if (notifications.length === 0) {
        notificationList.innerHTML = '<li class="text-center py-3 text-muted">No new notifications</li>';
        return;
    }

    notificationList.innerHTML = notifications.slice(0, 5).map(notification => {
        const timeAgo = formatTimeAgo(new Date(notification.createdAt));
        const icon = getNotificationIcon(notification.type);
        const photoUrl = notification.relatedUserPhotoUrl || '/images/default-avatar.png';

        return `
            <li>
                <a class="dropdown-item py-2 ${notification.isRead ? '' : 'bg-light'}" 
                   href="#" onclick="markNotificationAsRead(${notification.id}, event)">
                    <div class="d-flex align-items-start">
                        ${notification.relatedUserPhotoUrl ? 
                            `<img src="${photoUrl}" class="rounded-circle me-2" 
                                 style="width: 40px; height: 40px; object-fit: cover;" 
                                 onerror="this.src='/images/default-avatar.png'">` : 
                            `<i class="${icon} fs-4 me-2"></i>`
                        }
                        <div class="flex-grow-1">
                            <p class="mb-0 small">${notification.message}</p>
                            <small class="text-muted">${timeAgo}</small>
                        </div>
                    </div>
                </a>
            </li>
        `;
    }).join('');
}

function getNotificationIcon(type) {
    switch(type) {
        case 'NEW_MATCH':
            return 'bi bi-heart-fill text-danger';
        case 'NEW_MESSAGE':
            return 'bi bi-chat-fill text-primary';
        case 'PROFILE_VIEW':
            return 'bi bi-eye-fill text-info';
        case 'SYSTEM':
            return 'bi bi-info-circle-fill text-secondary';
        default:
            return 'bi bi-bell-fill';
    }
}

function formatTimeAgo(date) {
    const seconds = Math.floor((new Date() - date) / 1000);

    let interval = Math.floor(seconds / 31536000);
    if (interval >= 1) return interval + ' year' + (interval === 1 ? '' : 's') + ' ago';

    interval = Math.floor(seconds / 2592000);
    if (interval >= 1) return interval + ' month' + (interval === 1 ? '' : 's') + ' ago';

    interval = Math.floor(seconds / 86400);
    if (interval >= 1) return interval + ' day' + (interval === 1 ? '' : 's') + ' ago';

    interval = Math.floor(seconds / 3600);
    if (interval >= 1) return interval + ' hour' + (interval === 1 ? '' : 's') + ' ago';

    interval = Math.floor(seconds / 60);
    if (interval >= 1) return interval + ' minute' + (interval === 1 ? '' : 's') + ' ago';

    return 'just now';
}

function markNotificationAsRead(notificationId, event) {
    if (event) {
        event.preventDefault();
    }

    fetch(`/api/notifications/${notificationId}/read`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        credentials: 'include'
    })
    .then(response => {
        if (response.ok) {
            loadNotifications();
        } else {
            console.error('Failed to mark notification as read:', response.status);
        }
    })
    .catch(error => {
        console.error('Error marking notification as read:', error);
    });
}

function markAllNotificationsAsRead() {
    fetch('/api/notifications/read-all', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        credentials: 'include'
    })
    .then(response => {
        if (response.ok) {
            loadNotifications();
        } else {
            console.error('Failed to mark all notifications as read:', response.status);
        }
    })
    .catch(error => {
        console.error('Error marking all notifications as read:', error);
    });
}
