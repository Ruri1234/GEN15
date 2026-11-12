// PWA Registration & Install Banner - EPasien RS Bethesda Serukam
(function() {
  // 1. Registrasi Service Worker
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', function() {
      navigator.serviceWorker.register('sw.js')
        .then(function(reg) {
          console.log('PWA ServiceWorker berhasil aktif:', reg.scope);
        })
        .catch(function(err) {
          console.log('PWA ServiceWorker gagal:', err);
        });
    });
  }

  // 2. Banner Otomatis Install Aplikasi di HP
  let deferredPrompt;
  const isInstalled = window.matchMedia('(display-mode: standalone)').matches || window.navigator.standalone === true;

  if (!isInstalled) {
    window.addEventListener('beforeinstallprompt', (e) => {
      // Mencegah prompt default browser
      e.preventDefault();
      deferredPrompt = e;
      showPwaBanner();
    });
  }

  function showPwaBanner() {
    if (document.getElementById('pwa-install-banner') || localStorage.getItem('pwa_dismissed')) {
      return;
    }

    const banner = document.createElement('div');
    banner.id = 'pwa-install-banner';
    banner.innerHTML = `
      <div class="pwa-content">
        <img src="images/icon-192.png" alt="EPasien App Icon" class="pwa-icon" />
        <div class="pwa-text">
          <strong>Aplikasi EPasien RSU Bethesda</strong>
          <span>Pasang di HP Anda untuk kemudahan daftar & cek antrean</span>
        </div>
        <div class="pwa-actions">
          <button id="btn-pwa-install" class="pwa-btn-install"><i class="fa fa-download"></i> Pasang</button>
          <button id="btn-pwa-close" class="pwa-btn-close">&times;</button>
        </div>
      </div>
    `;

    document.body.appendChild(banner);

    document.getElementById('btn-pwa-install').addEventListener('click', async () => {
      if (deferredPrompt) {
        deferredPrompt.prompt();
        const { outcome } = await deferredPrompt.userChoice;
        console.log('User response to PWA prompt:', outcome);
        deferredPrompt = null;
        banner.remove();
      }
    });

    document.getElementById('btn-pwa-close').addEventListener('click', () => {
      banner.remove();
      // Simpan preferensi tutup sementara selama 1 hari
      localStorage.setItem('pwa_dismissed', Date.now());
    });
  }
})();
