/**
 * SmartSpend 2.0 – Shared JavaScript Utilities
 * Loaded on every page.
 */

// ── Sidebar Toggle (mobile) ───────────────────────────────────────────────
function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('open');
}

// Close sidebar when clicking outside on mobile
document.addEventListener('click', function(e) {
    const sidebar = document.getElementById('sidebar');
    const toggle  = document.querySelector('.sidebar-toggle');
    if (sidebar && toggle && !sidebar.contains(e.target) && !toggle.contains(e.target)) {
        sidebar.classList.remove('open');
    }
});

// ── Logout ────────────────────────────────────────────────────────────────
async function logout() {
    try {
        await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
    } catch (_) {}
    window.location.href = '/login';
}

// ── Alert Helper ──────────────────────────────────────────────────────────
/**
 * Show a bootstrap-style alert in #alertPlaceholder.
 * @param {'success'|'danger'|'info'} type
 * @param {string} message
 */
function showAlert(type, message) {
    const placeholder = document.getElementById('alertPlaceholder');
    if (!placeholder) return;

    const div = document.createElement('div');
    div.className = 'alert alert-' + type;
    div.textContent = message;
    div.style.cursor = 'pointer';
    div.title = 'Click to dismiss';
    div.addEventListener('click', () => div.remove());

    placeholder.innerHTML = '';
    placeholder.appendChild(div);

    // Auto-dismiss after 5 seconds
    setTimeout(() => div.remove(), 5000);
}

// ── Number Formatting ─────────────────────────────────────────────────────
function formatINR(amount) {
    return '₹' + Number(amount).toLocaleString('en-IN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

// ── Active Nav Highlighting ───────────────────────────────────────────────
(function () {
    const path = window.location.pathname;
    document.querySelectorAll('.nav-item').forEach(link => {
        const href = link.getAttribute('href');
        if (href && path.startsWith(href) && href !== '/') {
            link.classList.add('active');
        }
    });
})();
