/**
 * api.js — Central Fetch Wrapper for E-Learning Platform
 * - Automatically attaches JWT token as Authorization header
 * - Normalizes JSON payloads and responses
 * - Handles 401 Unauthorized / Token Expiration gracefully
 * - Provides clean error messages to catch blocks
 */
(function() {
  const BASE_URL = window.API_BASE_URL || 'http://localhost:8080';

  function getToken() {
    return localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken') ||
           localStorage.getItem('token') || sessionStorage.getItem('token');
  }

  function handleAuthFailure(reason) {
    ['jwtToken', 'token', 'userRole', 'userName'].forEach(k => localStorage.removeItem(k));
    sessionStorage.removeItem('jwtToken');
    if (!window.location.pathname.endsWith('login.html') && !window.location.pathname.endsWith('register.html') && !window.location.pathname.endsWith('index.html')) {
      if (window.showToast) {
        window.showToast('Session expired. Please log in again.', 'warning');
      }
      setTimeout(() => {
        window.location.href = 'login.html?reason=expired';
      }, 1000);
    }
  }

  async function request(endpoint, options = {}) {
    const url = endpoint.startsWith('http') ? endpoint : `${BASE_URL}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;
    const token = getToken();

    const headers = {
      ...(options.headers || {})
    };

    if (token && !headers['Authorization']) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    if (!(options.body instanceof FormData) && !headers['Content-Type'] && options.method && options.method !== 'GET') {
      headers['Content-Type'] = 'application/json';
    }

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(url, config);

      if (response.status === 401) {
        handleAuthFailure('unauthorized');
        throw new Error('Unauthorized or session expired. Please sign in again.');
      }

      const contentType = response.headers.get('content-type');
      let data = null;

      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      } else {
        data = await response.text();
      }

      if (!response.ok) {
        let errorMsg = 'An error occurred while processing your request.';
        if (typeof data === 'object' && data !== null) {
          errorMsg = data.message || data.error || JSON.stringify(data);
        } else if (typeof data === 'string' && data.trim()) {
          errorMsg = data;
        }
        throw new Error(errorMsg);
      }

      return data;
    } catch (err) {
      if (err.name === 'TypeError' && err.message.includes('Failed to fetch')) {
        throw new Error('Unable to connect to the server. Please check your network or server status.');
      }
      throw err;
    }
  }

  window.api = {
    get: (url, options) => request(url, { method: 'GET', ...options }),
    post: (url, body, options) => request(url, { method: 'POST', body: body instanceof FormData ? body : JSON.stringify(body), ...options }),
    put: (url, body, options) => request(url, { method: 'PUT', body: body instanceof FormData ? body : JSON.stringify(body), ...options }),
    patch: (url, body, options) => request(url, { method: 'PATCH', body: body instanceof FormData ? body : JSON.stringify(body), ...options }),
    del: (url, options) => request(url, { method: 'DELETE', ...options }),
    upload: (url, formData, options) => request(url, { method: 'POST', body: formData, ...options })
  };
})();
