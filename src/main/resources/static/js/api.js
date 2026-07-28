// HelpDesk Platform - Global API Configuration & Common Utilities

const API_BASE_URL = 'http://localhost:8080/api';

// Loading Spinner Functions
function showLoader() {
    const loader = document.getElementById('loaderWrapper');
    if (loader) loader.classList.add('active');
}

function hideLoader() {
    const loader = document.getElementById('loaderWrapper');
    if (loader) loader.classList.remove('active');
}

// Global Toast System
function showToast(message, type = 'success') {
    const toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) return;

    const id = 'toast_' + Date.now();
    const bgClass = type === 'success' ? 'bg-success' : type === 'danger' ? 'bg-danger' : type === 'warning' ? 'bg-warning' : 'bg-primary';

    const toastHTML = `
        <div id="${id}" class="toast align-items-center text-white ${bgClass} border-0" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="4000">
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;

    toastContainer.insertAdjacentHTML('beforeend', toastHTML);
    const toastElement = document.getElementById(id);
    const bsToast = new bootstrap.Toast(toastElement);
    bsToast.show();

    // Remove from DOM when hidden
    toastElement.addEventListener('hidden.bs.toast', () => {
        toastElement.remove();
    });
}

// Authenticated Fetch Wrapper
async function apiRequest(endpoint, options = {}) {
    const token = localStorage.getItem('accessToken');
    
    // Set headers
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers
    };

    try {
        showLoader();
        const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
        
        // Handle unauthorized / expired tokens
        if (response.status === 401) {
            localStorage.clear();
            if (!window.location.pathname.endsWith('login.html')) {
                window.location.href = 'login.html';
            }
            throw new Error('Session expired. Please log in again.');
        }

        const result = await response.json();
        
        if (!response.ok) {
            throw new Error(result.message || 'Something went wrong');
        }

        return result;
    } catch (error) {
        showToast(error.message, 'danger');
        throw error;
    } finally {
        hideLoader();
    }
}

// Dark Mode Toggle Support
function initTheme() {
    const currentTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', currentTheme);
    const themeBtn = document.getElementById('themeToggleBtn');
    if (themeBtn) {
        themeBtn.innerHTML = currentTheme === 'dark' ? '☀️' : '🌙';
    }
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    const themeBtn = document.getElementById('themeToggleBtn');
    if (themeBtn) {
        themeBtn.innerHTML = newTheme === 'dark' ? '☀️' : '🌙';
    }
}

// Run basic initializations
document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    const themeBtn = document.getElementById('themeToggleBtn');
    if (themeBtn) {
        themeBtn.addEventListener('click', toggleTheme);
    }
});
