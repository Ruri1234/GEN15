package fungsi;

import java.util.prefs.Preferences;

/**
 * Lock out login sementara.
 * Setelah MAX_ATTEMPTS kali gagal login berturut-turut, login dikunci selama LOCK_MS.
 * Status lock disimpan di Preferences sehingga restart aplikasi tidak mereset lock.
 * Dipanggil dari BtnLoginActionPerformed SEBELUM kredensial diperiksa.
 */
public final class LoginGuard {
    public static final int  MAX_ATTEMPTS = 3;
    public static final long LOCK_MS      = 30L * 1000;   // lock 30 detik

    private final Preferences prefs = Preferences.userNodeForPackage(LoginGuard.class);

    private int  failCount;
    private long lockUntil;

    public LoginGuard() {
        try {
            failCount = prefs.getInt("login.failCount", 0);
            lockUntil = prefs.getLong("login.lockUntil", 0L);
        } catch (Exception e) {
            failCount = 0;
            lockUntil = 0L;
        }
        long now = System.currentTimeMillis();
        if (lockUntil - now > LOCK_MS) {   // jam sistem diubah-ubah: batasi lock maksimum
            lockUntil = now + LOCK_MS;
        }
    }

    public synchronized boolean isLocked() {
        long now = System.currentTimeMillis();
        if (lockUntil > now) {
            return true;
        }
        if (lockUntil != 0L) {      // lock sudah habis
            lockUntil = 0L;
            failCount = 0;
            save();
        }
        return false;
    }

    public synchronized long remainingLockMs() {
        return Math.max(0L, lockUntil - System.currentTimeMillis());
    }

    /** Catat satu kegagalan login. @return true jika kegagalan ini memicu lock out. */
    public synchronized boolean recordFailure() {
        failCount++;
        boolean locked = false;
        if (failCount >= MAX_ATTEMPTS) {
            lockUntil = System.currentTimeMillis() + LOCK_MS;
            failCount = 0;
            locked = true;
        }
        save();
        return locked;
    }

    public synchronized void recordSuccess() {
        failCount = 0;
        lockUntil = 0L;
        save();
    }

    private void save() {
        try {
            prefs.putInt("login.failCount", failCount);
            prefs.putLong("login.lockUntil", lockUntil);
            prefs.flush();
        } catch (Exception e) {
            System.out.println("Notif LoginGuard : " + e);
        }
    }
}
