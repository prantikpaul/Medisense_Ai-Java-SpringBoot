// MediSense AI - Main JavaScript File

document.addEventListener('DOMContentLoaded', function() {
    // Password strength validation for signup form
    const passwordField = document.getElementById('password');
    if (passwordField) {
        passwordField.addEventListener('input', function() {
            validatePassword(this);
        });
    }
    
    // Form validation for signup
    const signupForm = document.querySelector('form[action="/signup"]');
    if (signupForm) {
        signupForm.addEventListener('submit', function(event) {
            if (!validateForm(this)) {
                event.preventDefault();
            }
        });
    }
    
    // Initialize tooltips if Bootstrap is available
    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function(tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
});

// Password strength validation
function validatePassword(passwordField) {
    const password = passwordField.value;
    const minLength = 8;
    
    // Check if password meets minimum length
    if (password.length < minLength) {
        passwordField.classList.add('is-invalid');
        passwordField.classList.remove('is-valid');
        
        // Update feedback message if it exists
        const feedbackElement = passwordField.nextElementSibling;
        if (feedbackElement && feedbackElement.classList.contains('form-text')) {
            feedbackElement.textContent = `Password must be at least ${minLength} characters long.`;
            feedbackElement.classList.add('text-danger');
            feedbackElement.classList.remove('text-success');
        }
        
        return false;
    } else {
        passwordField.classList.remove('is-invalid');
        passwordField.classList.add('is-valid');
        
        // Update feedback message if it exists
        const feedbackElement = passwordField.nextElementSibling;
        if (feedbackElement && feedbackElement.classList.contains('form-text')) {
            feedbackElement.textContent = 'Password strength: Good';
            feedbackElement.classList.remove('text-danger');
            feedbackElement.classList.add('text-success');
        }
        
        return true;
    }
}

// Form validation for signup
function validateForm(form) {
    let isValid = true;
    
    // Validate required fields
    const requiredFields = form.querySelectorAll('[required]');
    requiredFields.forEach(function(field) {
        if (!field.value.trim()) {
            field.classList.add('is-invalid');
            isValid = false;
        } else {
            field.classList.remove('is-invalid');
        }
    });
    
    // Validate email format if email field exists
    const emailField = form.querySelector('input[type="email"]');
    if (emailField && emailField.value.trim()) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(emailField.value.trim())) {
            emailField.classList.add('is-invalid');
            isValid = false;
        } else {
            emailField.classList.remove('is-invalid');
        }
    }
    
    // Validate password if password field exists
    const passwordField = form.querySelector('#password');
    if (passwordField) {
        isValid = validatePassword(passwordField) && isValid;
    }
    
    return isValid;
}