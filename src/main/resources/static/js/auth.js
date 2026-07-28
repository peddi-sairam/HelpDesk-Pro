// HelpDesk Platform - Authentication Client Operations

async function handleLogin(email, password) {
    try {
        const response = await apiRequest('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
        
        if (response.success && response.data) {
            const data = response.data;
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            localStorage.setItem('email', data.email);
            localStorage.setItem('role', data.role);
            localStorage.setItem('fullName', data.fullName);
            localStorage.setItem('userId', data.userId);
            localStorage.setItem('companyId', data.companyId || "");
            
            showToast('Login successful! Redirecting...', 'success');
            setTimeout(() => {
                redirectByRole(data.role);
            }, 1000);
        }
    } catch (error) {
        console.error('Login error:', error);
    }
}

async function handleRegister(fullName, email, password, phone, companyId) {
    try {
        const response = await apiRequest('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ fullName, email, password, phone, companyId })
        });

        if (response.success) {
            showToast('Registration successful! Redirecting to login...', 'success');
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 1500);
        }
    } catch (error) {
        console.error('Registration error:', error);
    }
}

function handleLogout() {
    const email = localStorage.getItem('email');
    if (email) {
        apiRequest(`/auth/logout?email=${encodeURIComponent(email)}`, { method: 'POST' })
            .catch(err => console.error('Logout error on server:', err));
    }
    localStorage.clear();
    showToast('Logged out successfully', 'info');
    setTimeout(() => {
        window.location.href = 'login.html';
    }, 1000);
}

function redirectByRole(role) {
    switch (role) {
        case 'ROLE_SUPER_ADMIN':
            window.location.href = 'admin.html';
            break;
        case 'ROLE_COMPANY_ADMIN':
            window.location.href = 'dashboard.html';
            break;
        case 'ROLE_SUPPORT_AGENT':
            window.location.href = 'agent.html';
            break;
        case 'ROLE_CUSTOMER':
            window.location.href = 'customer.html';
            break;
        default:
            window.location.href = '404.html';
    }
}

function checkAuthentication() {
    const token = localStorage.getItem('accessToken');
    if (!token && !window.location.pathname.endsWith('login.html') && !window.location.pathname.endsWith('signup.html')) {
        window.location.href = 'login.html';
    }
}
