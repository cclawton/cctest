const CACHE_NAME = 'meditation-timer-v4';
const urlsToCache = [
  './',
  './index.html',
  './styles.css',
  './app.js',
  './manifest.json',
  './audio/start-bell.ogg',
  './audio/interval-bell.mp3',
  './audio/end-bell.ogg'
];

// Install event - cache resources
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => {
        console.log('Opened cache');
        return cache.addAll(urlsToCache);
      })
  );
  // Force the waiting service worker to become the active service worker
  self.skipWaiting();
});

// Activate event - clean up old caches
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== CACHE_NAME) {
            console.log('Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
  // Take control of all pages immediately
  self.clients.claim();
});

// Fetch event - serve from cache, fallback to network
self.addEventListener('fetch', (event) => {
  const request = event.request;
  const url = new URL(request.url);

  // Check if this is an audio file request
  const isAudio = url.pathname.includes('/audio/') ||
                  url.pathname.endsWith('.mp3') ||
                  url.pathname.endsWith('.wav') ||
                  url.pathname.endsWith('.ogg');

  // Handle range requests for audio files
  if (isAudio && request.headers.get('range')) {
    event.respondWith(handleRangeRequest(request));
    return;
  }

  // Standard caching strategy
  event.respondWith(
    caches.match(request)
      .then((response) => {
        // Cache hit - return response
        if (response) {
          return response;
        }

        // Clone the request
        const fetchRequest = request.clone();

        return fetch(fetchRequest).then((response) => {
          // Check if valid response
          if (!response || response.status !== 200 || response.type !== 'basic') {
            return response;
          }

          // Clone the response
          const responseToCache = response.clone();

          caches.open(CACHE_NAME)
            .then((cache) => {
              cache.put(request, responseToCache);
            });

          return response;
        }).catch(() => {
          // If both cache and network fail, could return a fallback page here
          return new Response('Offline - please check your connection', {
            status: 503,
            statusText: 'Service Unavailable',
            headers: new Headers({
              'Content-Type': 'text/plain'
            })
          });
        });
      })
  );
});

// Handle HTTP range requests for audio files
async function handleRangeRequest(request) {
  try {
    const cache = await caches.open(CACHE_NAME);

    // Try to get the full file from cache (ignore range header for cache lookup)
    const url = new URL(request.url);
    const cachedResponse = await cache.match(url.pathname);

    if (!cachedResponse) {
      // Not in cache, try network
      return fetch(request);
    }

    const rangeHeader = request.headers.get('range');
    const fullBlob = await cachedResponse.blob();
    const fullLength = fullBlob.size;

    // Parse range header (e.g., "bytes=0-1023")
    const rangeMatch = rangeHeader.match(/bytes=(\d+)-(\d*)/);
    if (!rangeMatch) {
      // Invalid range, return full file
      return cachedResponse;
    }

    const start = parseInt(rangeMatch[1], 10);
    const end = rangeMatch[2] ? parseInt(rangeMatch[2], 10) : fullLength - 1;

    // Extract the requested byte range
    const slicedBlob = fullBlob.slice(start, end + 1);

    // Create a 206 Partial Content response
    const responseHeaders = new Headers({
      'Content-Type': cachedResponse.headers.get('Content-Type') || 'audio/wav',
      'Content-Length': slicedBlob.size.toString(),
      'Content-Range': `bytes ${start}-${end}/${fullLength}`,
      'Accept-Ranges': 'bytes'
    });

    return new Response(slicedBlob, {
      status: 206,
      statusText: 'Partial Content',
      headers: responseHeaders
    });
  } catch (error) {
    console.error('Range request handler error:', error);
    // Fallback to network
    return fetch(request);
  }
}

// Handle messages from the main thread
self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});
