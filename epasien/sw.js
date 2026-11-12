// Service Worker untuk PWA EPasien RSU Bethesda Serukam
const CACHE_NAME = 'epasien-rsub-v1.0';
const STATIC_ASSETS = [
  './',
  'manifest.json',
  'favicon.ico',
  'images/icon-192.png',
  'images/icon-512.png',
  'css/modern-blue.css'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(STATIC_ASSETS).catch((err) => console.log('PWA cache error:', err));
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => {
          if (key !== CACHE_NAME) {
            return caches.delete(key);
          }
        })
      );
    })
  );
  self.clients.claim();
});

// Network-first strategy for dynamic healthcare data, fallback to cache
self.addEventListener('fetch', (event) => {
  // Hanya proses GET request untuk menghindari gangguan pada form POST transaksi / login
  if (event.request.method !== 'GET') {
    return;
  }

  event.respondWith(
    fetch(event.request)
      .then((networkResponse) => {
        // Jika sukses dari jaringan, perbarui cache untuk asset statis
        if (networkResponse && networkResponse.status === 200) {
          const responseToCache = networkResponse.clone();
          caches.open(CACHE_NAME).then((cache) => {
            // Hanya cache file statis (css, js, gambar)
            if (event.request.url.match(/\.(css|js|png|jpg|jpeg|svg|woff2|woff)$/)) {
              cache.put(event.request, responseToCache);
            }
          });
        }
        return networkResponse;
      })
      .catch(() => {
        // Jika offline atau jaringan terputus, gunakan cache
        return caches.match(event.request);
      })
  );
});
