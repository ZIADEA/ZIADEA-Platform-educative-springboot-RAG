/**
 * EduForge - Application JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    
    // ===== Auto-dismiss alerts after 5 seconds =====
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });
    
    // ===== Smooth scroll for anchor links (excluding ALL Bootstrap components) =====
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        // Skip if this is a Bootstrap component or has any data-bs-* attribute
        if (anchor.getAttribute('data-bs-toggle') ||
            anchor.getAttribute('data-bs-target') ||
            anchor.getAttribute('data-bs-dismiss') ||
            anchor.getAttribute('data-bs-slide') ||
            anchor.getAttribute('data-bs-slide-to') !== null ||
            anchor.classList.contains('nav-link') ||
            anchor.classList.contains('dropdown-toggle') ||
            anchor.classList.contains('dropdown-item') ||
            anchor.classList.contains('list-group-item') ||
            anchor.classList.contains('page-link') ||
            anchor.closest('.nav-tabs') ||
            anchor.closest('.nav-pills') ||
            anchor.closest('.accordion') ||
            anchor.closest('.dropdown') ||
            anchor.closest('.carousel') ||
            anchor.closest('.modal') ||
            anchor.closest('.offcanvas') ||
            anchor.closest('.list-group')) {
            return;
        }
        
        anchor.addEventListener('click', function(e) {
            const targetId = this.getAttribute('href');
            // Only handle real anchors (not just "#")
            if (targetId && targetId !== '#' && targetId.length > 1) {
                try {
                    const target = document.querySelector(targetId);
                    if (target) {
                        e.preventDefault();
                        target.scrollIntoView({
                            behavior: 'smooth',
                            block: 'start'
                        });
                        // Update URL without triggering scroll
                        history.pushState(null, null, targetId);
                    }
                } catch (err) {
                    // Invalid selector, ignore
                    console.warn('Invalid anchor target:', targetId);
                }
            }
        });
    });
    
    console.log('EduForge JS loaded');
});
