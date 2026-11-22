(function () {
    var prefs = ['jp-large-text', 'jp-high-contrast'];

    prefs.forEach(function (pref) {
        try {
            if (localStorage.getItem(pref) === '1') {
                document.body.classList.add(pref);
            }
        } catch (e) {
        }
    });

    document.querySelectorAll('[data-toggle-pref]').forEach(function (button) {
        var pref = button.getAttribute('data-toggle-pref');
        button.setAttribute('aria-pressed', document.body.classList.contains(pref) ? 'true' : 'false');
        button.addEventListener('click', function () {
            var on = document.body.classList.toggle(pref);
            button.setAttribute('aria-pressed', on ? 'true' : 'false');
            try {
                localStorage.setItem(pref, on ? '1' : '0');
            } catch (e) {
            }
        });
    });

    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!window.confirm(form.getAttribute('data-confirm'))) {
                event.preventDefault();
            }
        });
    });

    document.querySelectorAll('[data-print]').forEach(function (button) {
        button.addEventListener('click', function () {
            window.print();
        });
    });

    var banner = document.getElementById('offline-banner');

    function updateOnlineState() {
        if (banner) {
            banner.classList.toggle('d-none', navigator.onLine);
        }
    }

    window.addEventListener('online', updateOnlineState);
    window.addEventListener('offline', updateOnlineState);
    updateOnlineState();

    if ('serviceWorker' in navigator) {
        window.addEventListener('load', function () {
            navigator.serviceWorker.register('/sw.js').catch(function () {
            });
        });
    }
})();
