var CACHE = 'joyplan-v1';
var CORE = ['/css/app.css', '/js/app.js', '/offline.html', '/icons/icon.svg', '/manifest.json'];

self.addEventListener('install', function (event) {
    event.waitUntil(caches.open(CACHE).then(function (cache) {
        return cache.addAll(CORE);
    }));
    self.skipWaiting();
});

self.addEventListener('activate', function (event) {
    event.waitUntil(caches.keys().then(function (keys) {
        return Promise.all(keys.filter(function (key) {
            return key !== CACHE;
        }).map(function (key) {
            return caches.delete(key);
        }));
    }).then(function () {
        return self.clients.claim();
    }));
});

function isStatic(url) {
    return url.hostname === 'cdn.jsdelivr.net'
        || (url.origin === self.location.origin
            && (url.pathname.indexOf('/css/') === 0 || url.pathname.indexOf('/js/') === 0
                || url.pathname.indexOf('/icons/') === 0));
}

function isPlanPage(url) {
    return url.origin === self.location.origin && /^\/plans\/\d+$/.test(url.pathname);
}

self.addEventListener('fetch', function (event) {
    var request = event.request;
    if (request.method !== 'GET') {
        return;
    }
    var url = new URL(request.url);

    if (isStatic(url)) {
        event.respondWith(caches.match(request).then(function (cached) {
            return cached || fetch(request).then(function (response) {
                var copy = response.clone();
                caches.open(CACHE).then(function (cache) {
                    cache.put(request, copy);
                });
                return response;
            });
        }));
        return;
    }

    if (isPlanPage(url)) {
        event.respondWith(fetch(request).then(function (response) {
            if (response.ok && !response.redirected) {
                var copy = response.clone();
                caches.open(CACHE).then(function (cache) {
                    cache.put(url.pathname, copy);
                });
            }
            return response;
        }).catch(function () {
            return caches.match(url.pathname).then(function (cached) {
                return cached || caches.match('/offline.html');
            });
        }));
        return;
    }

    if (request.mode === 'navigate') {
        event.respondWith(fetch(request).catch(function () {
            return caches.match('/offline.html');
        }));
    }
});
