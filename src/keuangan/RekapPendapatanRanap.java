package laporan;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Rekap Pendapatan Pasien Reguler Rawat Inap per Bangsal (ICU, KEBIDANAN, RRUB) - satu tahun penuh, baris = bulan
 * (berdasarkan tanggal pulang), kolom = bangsal, plus Total. Chip bangsal di bagian atas menyaring seluruh tampilan ke
 * satu bangsal saja. Setiap tampilan dibandingkan dengan periode yang sama tahun lalu. Tampilan: Tabel, Grafik batang
 * bertumpuk per komponen biaya, Line Chart (total, per bangsal, per komponen) dan Peringkat. Tema gelap + animasi
 * sama dengan {@link RekapPendapatanPerpoli}. Aturan pembagian bangsal (kode kamar) ada pada pemuat data di dialog.
 */
public final class RekapPendapatanRanap extends JDialog {

    private static final String JUDUL = "PENDAPATAN PASIEN REGULER RANAP PER BANGSAL";
    private static final String AWALAN_FILE = "Rekap_Pendapatan_Ranap_Bangsal_";

    private static final Color BG1 = new Color(0x0B, 0x13, 0x2B);
    private static final Color BG2 = new Color(0x1C, 0x25, 0x41);
    private static final Color TEKS = new Color(0xE6, 0xEE, 0xF8);
    private static final Color TEKS_REDUP = new Color(0x8F, 0xA6, 0xBD);
    private static final Color EMAS = new Color(0xFF, 0xD5, 0x6B);
    private static final Color MERAH = new Color(0xFF, 0x8F, 0xA3);
    private static final Color BIRU = new Color(0x6E, 0xE7, 0xF9);
    private static final Color ORANYE = new Color(0xFF, 0xC3, 0x71);
    private static final Color HIJAU = new Color(0x7C, 0xE3, 0xA1);
    private static final Color AKSEN1 = new Color(0x2F, 0x80, 0xED);
    private static final Color AKSEN2 = new Color(0x00, 0xC2, 0xE0);

    private static final String[] NAMA_BULAN = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli",
        "Agustus", "September", "Oktober", "November", "Desember"};

    /** Format angka seperti pada lembar Excel contoh: 3,839,666 (ganti pemisah di sini bila perlu). */
    static String uang(long v) {
        return String.format(Locale.US, "%,d", v);
    }

    // ------------------------------------------------------------------
    //  Model data
    // ------------------------------------------------------------------
    /** Jumlah komponen biaya (urutan = urutan tumpukan pada grafik). */
    static final int NK = 7;
    /** TINDAKAN = tindakan dokter/paramedis ranap+ralan, KAMAR = kamar + service + harian, OBAT = obat + retur + resep pulang, LAINNYA = registrasi + tambahan + potongan. */
    static final String[] KOMP = {"TINDAKAN", "OPERASI", "KAMAR", "OBAT", "LABORAT", "RADIOLOGI", "LAINNYA"};
    static final String[] KOMP_NAMA = {"Tindakan", "Operasi", "Kamar", "Obat", "Laborat", "Radiologi", "Lainnya"};
    /** Kelompok bangsal = kolom tabel (urutan tetap). Aturan kode kamar ada pada pemuat data di dialog pemanggil. */
    static final String[] BANGSAL = {"ICU", "KEBIDANAN", "RRUB"};
    static final Color[] WARNA_BANGSAL = {new Color(0xFF, 0xB7, 0x4D), new Color(0xF4, 0x8F, 0xB1), new Color(0x4F, 0xC3, 0xF7)};

    static Color warnaBangsal(String nama) {
        for (int i = 0; i < BANGSAL.length; i++) {
            if (BANGSAL[i].equals(nama)) {
                return WARNA_BANGSAL[i];
            }
        }
        return TEKS;
    }

    private static final String[] NAMA_GRUP = {"BANGSAL"};
    private static final Color UNGU = new Color(0xB9, 0xA7, 0xFF);
    private static final Color[] WARNA_GRUP = {AKSEN1};

    /**
     * Data mentah satu periode: nilai[bulan 0..11][bangsal][komponen]. ada[bulan][bangsal] = true bila bangsal tsb punya
     * pasien pulang pada bulan itu (sel tabel dikosongkan bila tidak ada).
     */
    public static final class Mentah {

        final long[][][] nilai;
        final boolean[][] ada;

        public Mentah(long[][][] nilai, boolean[][] ada) {
            this.nilai = nilai;
            this.ada = ada;
        }

        static Mentah kosong() {
            return new Mentah(new long[12][BANGSAL.length][NK], new boolean[12][BANGSAL.length]);
        }

        /**
         * Bangun dari baris hasil query. Tiap baris = {Integer bulan 1..12, String bangsal (ICU / KEBIDANAN / RRUB),
         * long[NK] komponen} dengan urutan komponen: tindakan, operasi, kamar, obat, laborat, radiologi, lainnya.
         */
        public static Mentah bangun(java.util.List<Object[]> baris) {
            Mentah m = kosong();
            for (Object[] b : baris) {
                int bl = (Integer) b[0] - 1;
                int c = Arrays.asList(BANGSAL).indexOf((String) b[1]);
                if (bl < 0 || bl > 11 || c < 0) {
                    continue;
                }
                long[] k = (long[]) b[2];
                for (int i = 0; i < NK; i++) {
                    m.nilai[bl][c][i] += k[i];
                }
                m.ada[bl][c] = true;
            }
            return m;
        }
    }

    /** Data siap tampil: seluruh bangsal (only < 0) atau satu bangsal saja (only = indeks BANGSAL). */
    public static final class Data {

        public final String label;
        final String[] nama;
        final Long[][] nilai;                 // [bulan][kolom]; null = tidak ada pasien pulang
        final long[][] komp = new long[12][NK]; // [bulan][komponen] seluruh kolom yang ikut
        final long[] totalBulan = new long[12];
        final long[] totalKolom;
        final long total;
        final boolean ada;
        final int kolomTerbanyak;             // -1 bila kosong

        Data(Mentah m, int only) {
            this.label = only < 0 ? "SEMUA BANGSAL" : BANGSAL[only];
            final int n = only < 0 ? BANGSAL.length : 1;
            nama = new String[n];
            nilai = new Long[12][n];
            totalKolom = new long[n];
            boolean a = false;
            long tt = 0;
            for (int c = 0; c < n; c++) {
                int o = only < 0 ? c : only;
                nama[c] = BANGSAL[o];
                for (int i = 0; i < 12; i++) {
                    if (!m.ada[i][o]) {
                        continue;
                    }
                    long s = 0;
                    for (int k = 0; k < NK; k++) {
                        komp[i][k] += m.nilai[i][o][k];
                        s += m.nilai[i][o][k];
                    }
                    nilai[i][c] = s;
                    totalBulan[i] += s;
                    totalKolom[c] += s;
                    tt += s;
                    a = true;
                }
            }
            this.ada = a;
            this.total = tt;
            int best = -1;
            for (int c = 0; c < n; c++) {
                if (totalKolom[c] > 0 && (best < 0 || totalKolom[c] > totalKolom[best])) {
                    best = c;
                }
            }
            this.kolomTerbanyak = best;
        }

        static Data kosong() {
            return new Data(Mentah.kosong(), -1);
        }

        int jmlKolom() {
            return nama.length;
        }

        String namaKolom(int c) {
            return nama[c];
        }

        /** Rincian satu bulan (0..11) untuk grafik, urut seperti KOMP. */
        long[] komponen(int m) {
            return komp[m];
        }

        int cari(String nm) {
            for (int c = 0; c < nama.length; c++) {
                if (nama[c].equals(nm)) {
                    return c;
                }
            }
            return -1;
        }

        /** Pendapatan bangsal bernama nm pada bulan m (0..11); 0 bila tidak ada. */
        long bulanDari(String nm, int m) {
            int c = cari(nm);
            return c < 0 || nilai[m][c] == null ? 0 : nilai[m][c];
        }

        long tahunDari(String nm) {
            int c = cari(nm);
            return c < 0 ? 0 : totalKolom[c];
        }
    }

    /** Hasil muat satu tahun (tahun terpilih dan periode yang sama tahun lalu sebagai pembanding). */
    public static final class Paket {

        private final Mentah ini, lalu;
        private final Data[][] cache = new Data[2][BANGSAL.length + 1];

        public Paket(Mentah ini, Mentah lalu) {
            this.ini = ini == null ? Mentah.kosong() : ini;
            this.lalu = lalu == null ? Mentah.kosong() : lalu;
        }

        static Paket kosong() {
            return new Paket(null, null);
        }

        /** bangsal = null: semua bangsal; selain itu salah satu dari BANGSAL ("ICU", "KEBIDANAN", "RRUB"). */
        Data ambil(boolean dariLalu, String bangsal) {
            int idx = bangsal == null ? -1 : Arrays.asList(BANGSAL).indexOf(bangsal);
            int j = dariLalu ? 1 : 0;
            if (cache[j][idx + 1] == null) {
                cache[j][idx + 1] = new Data(dariLalu ? lalu : ini, idx);
            }
            return cache[j][idx + 1];
        }
    }

    /**
     * Pengambil data per tahun; dipanggil di thread latar belakang (bukan thread tampilan).
     * Periode tahun terpilih = 1 Januari s.d. {@code sampai} (hari ini bila tahun berjalan, 31 Desember bila tahun lalu);
     * pembandingnya = 1 Januari tahun sebelumnya s.d. {@code sampaiLalu} (tanggal yang sama, setahun sebelumnya).
     */
    public interface Pemuat {

        Paket muat(int tahun, LocalDate sampai, LocalDate sampaiLalu) throws Exception;
    }

    /** Tampilkan dialog rekap (modal). Bulan dipakai sebagai bulan yang disorot saat dibuka. */
    public static void tampil(Window owner, Pemuat pemuat, int bulan, int tahun) {
        RekapPendapatanRanap d = new RekapPendapatanRanap(owner, pemuat, bulan, tahun);
        d.setVisible(true);
        d.dispose();
    }

    // ------------------------------------------------------------------
    //  Fungsi bantu
    // ------------------------------------------------------------------
    private static Color alfa(Color c, int a) {
        return RekapPenyakitRalan.alfa(c, a);
    }

    private static Color campur(Color a, Color b, double t) {
        return RekapPenyakitRalan.campur(a, b, t);
    }

    private static double easeOut(double t) {
        return RekapPenyakitRalan.easeOut(t);
    }

    private static double klem(double v) {
        return RekapPenyakitRalan.klem(v);
    }

    private static void aa(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static void tengah(Graphics2D g2, String t, int x, int lebar, int baseline) {
        g2.drawString(t, x + (lebar - g2.getFontMetrics().stringWidth(t)) / 2, baseline);
    }

    private static String potong(String s, FontMetrics fm, int lebar) {
        if (fm.stringWidth(s) <= lebar) {
            return s;
        }
        String r = s;
        while (r.length() > 1 && fm.stringWidth(r + "\u2026") > lebar) {
            r = r.substring(0, r.length() - 1);
        }
        return r + "\u2026";
    }

    /** Bagi judul kolom menjadi maksimal dua baris di spasi terdekat dengan tengah kalimat. */
    private static String[] bagi(String s, FontMetrics fm, int lebar) {
        if (fm.stringWidth(s) <= lebar) {
            return new String[]{s};
        }
        int mid = s.length() / 2, best = -1;
        for (int c = 0; c < s.length(); c++) {
            if (s.charAt(c) == ' ' && (best < 0 || Math.abs(c - mid) < Math.abs(best - mid))) {
                best = c;
            }
        }
        if (best < 0) {
            return new String[]{potong(s, fm, lebar)};
        }
        return new String[]{potong(s.substring(0, best), fm, lebar), potong(s.substring(best + 1), fm, lebar)};
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static double lim(double v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    /** Ease-out kubik, nilai di luar 0..1 dijepit. */
    private static double kubik(double t) {
        double x = 1 - lim(t);
        return 1 - x * x * x;
    }

    /** Ease-out dengan sedikit pantulan di ujung (batang "membal" kecil saat tumbuh). */
    private static double balik(double t) {
        double x = lim(t) - 1, c1 = 1.1;
        return 1 + (c1 + 1) * x * x * x + c1 * x * x;
    }

    /** Angka ringkas untuk sumbu / label batang: 1.2 M, 3.4 jt, 560 rb. */
    static String ringkas(long v) {
        long a = Math.abs(v);
        String s;
        if (a >= 1_000_000_000L) {
            s = trimNol(String.format(Locale.US, "%.2f", a / 1e9)) + " M";
        } else if (a >= 1_000_000L) {
            s = trimNol(String.format(Locale.US, a >= 100_000_000L ? "%.0f" : "%.1f", a / 1e6)) + " jt";
        } else if (a >= 1_000L) {
            s = String.format(Locale.US, "%.0f rb", a / 1e3);
        } else {
            s = Long.toString(a);
        }
        return v < 0 ? "-" + s : s;
    }

    private static String trimNol(String s) {
        return s.indexOf('.') < 0 ? s : s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /** Selisih terhadap pembanding dalam persen, mis. "▲ 12.3%". Tanpa pembanding: "-" atau "baru". */
    static String delta(long ini, long lalu) {
        if (lalu <= 0) {
            return ini <= 0 ? "-" : "baru";
        }
        double p = (ini - lalu) * 100.0 / lalu;
        return (ini >= lalu ? "\u25B2 " : "\u25BC ") + String.format(Locale.US, "%.1f%%", Math.abs(p));
    }

    static Color warnaDelta(long ini, long lalu) {
        if (ini == lalu) {
            return TEKS_REDUP;
        }
        return ini > lalu ? HIJAU : MERAH;
    }

    // ------------------------------------------------------------------
    //  Header: judul + kartu ringkasan (masuk bergantian, angka menghitung naik)
    // ------------------------------------------------------------------
    static final class HeaderPanel extends JPanel {

        static final int TINGGI = 150;
        private static final long DURASI = 700L;
        private static final int N_KARTU = 4;
        private final Data d, lalu;
        private final int tahun;
        private int bulan;
        private boolean statis = true;
        private long mulai = 0, mulaiBulan = 0, berakhir = 0;
        private Timer timer;

        HeaderPanel(Data d, Data lalu, int tahun, int bulan) {
            this.d = d;
            this.lalu = lalu;
            this.tahun = tahun;
            this.bulan = bulan;
            setPreferredSize(new Dimension(900, TINGGI));
            setOpaque(true);
        }

        void mulaiAnimasi() {
            statis = false;
            mulai = System.currentTimeMillis();
            jalan(DURASI + N_KARTU * 110 + 60);
        }

        /** Ganti bulan terpilih: kartu bulan menghitung naik lagi. */
        void setBulan(int b) {
            bulan = b;
            mulaiBulan = System.currentTimeMillis();
            jalan(650);
        }

        private void jalan(long ms) {
            berakhir = Math.max(berakhir, System.currentTimeMillis() + ms);
            if (timer == null) {
                timer = new Timer(16, e -> {
                    repaint();
                    if (System.currentTimeMillis() >= berakhir) {
                        ((Timer) e.getSource()).stop();
                        statis = true;
                        repaint();
                    }
                });
            }
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        void hentikan() {
            if (timer != null) {
                timer.stop();
            }
        }

        /** i = -1 untuk judul, 0..3 untuk kartu (masuk bergantian). */
        private double kemajuan(int i) {
            if (statis) {
                return 1.0;
            }
            return easeOut((System.currentTimeMillis() - mulai - (long) (i + 1) * 110) / (double) DURASI);
        }

        private double kemajuanBulan() {
            if (!statis) {
                return kemajuan(1);
            }
            return easeOut((System.currentTimeMillis() - mulaiBulan) / 600.0);
        }

        private String cakupan() {
            return "seluruh komponen biaya";
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new LinearGradientPaint(0, 0, Math.max(w, 2), 0, new float[]{0f, 0.55f, 1f},
                    new Color[]{new Color(0x0F, 0x20, 0x27), new Color(0x20, 0x3A, 0x43), new Color(0x2C, 0x53, 0x64)}));
            g2.fillRect(0, 0, w, h);
            g2.setPaint(new RadialGradientPaint(w - 80, 10, 260f, new float[]{0f, 1f},
                    new Color[]{new Color(0x00, 0xD2, 0xFF, 70), new Color(0x00, 0xD2, 0xFF, 0)}));
            g2.fillRect(0, 0, w, h);

            java.awt.Composite lama = g2.getComposite();
            double p0 = kemajuan(-1);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) p0));
            g2.translate(-(1 - p0) * 24, 0);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
            g2.drawString(JUDUL, 28, 42);
            int tx = 28 + g2.getFontMetrics().stringWidth(JUDUL) + 16;
            // pil nama kelompok data (IRJ / IGD / IRJ + IGD)
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            int pw = g2.getFontMetrics().stringWidth(d.label) + 26;
            g2.setPaint(new GradientPaint(tx, 22, AKSEN1, tx + pw, 46, AKSEN2));
            g2.fillRoundRect(tx, 22, pw, 24, 24, 24);
            g2.setColor(Color.WHITE);
            g2.drawString(d.label, tx + 13, 39);
            g2.setColor(new Color(0x9F, 0xD8, 0xF0));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.drawString("Tahun " + tahun + "   \u2022   pasien pulang tanpa piutang   \u2022   " + cakupan() + "   \u2022   dibanding periode sama tahun " + (tahun - 1), 29, 64);
            g2.translate((1 - p0) * 24, 0);
            g2.setComposite(lama);

            long totalLalu = lalu == null ? 0 : lalu.total;
            String topNama = d.kolomTerbanyak < 0 ? "-" : d.namaKolom(d.kolomTerbanyak);
            long topNilai = d.kolomTerbanyak < 0 ? 0 : d.totalKolom[d.kolomTerbanyak];
            if (d.jmlKolom() == 1) {
                // satu bangsal dipilih: kartu keempat menampilkan bulan terbaik bangsal tsb
                int bm = 0;
                for (int i = 1; i < 12; i++) {
                    if (d.totalBulan[i] > d.totalBulan[bm]) {
                        bm = i;
                    }
                }
                topNama = d.totalBulan[bm] > 0 ? NAMA_BULAN[bm] : "-";
                topNilai = d.totalBulan[bm];
            }
            String[] teksLabel = {"TOTAL TAHUN " + tahun, "BULAN " + NAMA_BULAN[bulan - 1].toUpperCase(Locale.ROOT),
                "VS TAHUN " + (tahun - 1) + " (PERIODE SAMA)", d.jmlKolom() == 1 ? "BULAN TERBAIK" : "BANGSAL TERBESAR"};
            Color warnaDlt = lalu == null ? TEKS_REDUP : warnaDelta(d.total, totalLalu);
            Color[] warna = {BIRU, HIJAU, warnaDlt, EMAS};
            int[] lebar = {250, 250, 250, Math.max(240, w - 28 - 750 - 36 - 28)};
            int x = 28, y = 80;
            for (int i = 0; i < N_KARTU; i++) {
                double p = i == 1 ? kemajuanBulan() : kemajuan(i);
                if (p > 0.001) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) klem(p)));
                    g2.translate(0, (1 - p) * 14);
                    g2.setPaint(new GradientPaint(0, y, new Color(255, 255, 255, 38), 0, y + 56, new Color(255, 255, 255, 14)));
                    g2.fillRoundRect(x, y, lebar[i], 56, 16, 16);
                    g2.setStroke(new BasicStroke(1f));
                    g2.setColor(new Color(255, 255, 255, 60));
                    g2.drawRoundRect(x, y, lebar[i], 56, 16, 16);
                    g2.setColor(alfa(warna[i], 210));
                    g2.fillRoundRect(x + 8, y + 14, 3, 28, 3, 3);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.setColor(new Color(0x9F, 0xB8, 0xCC));
                    g2.drawString(teksLabel[i], x + 20, y + 19);
                    int fs = i == 3 ? 15 : (i == 2 ? 18 : 22);
                    if (i <= 1) {
                        String akhir = "Rp " + uang(i == 0 ? d.total : d.totalBulan[bulan - 1]);
                        while (fs > 13 && g2.getFontMetrics(new Font("Segoe UI", Font.BOLD, fs)).stringWidth(akhir) > lebar[i] - 36) {
                            fs--;
                        }
                    }
                    g2.setFont(new Font("Segoe UI", Font.BOLD, fs));
                    g2.setColor(i == 3 ? EMAS : (i == 2 ? warnaDlt : (i == 1 ? HIJAU : Color.WHITE)));
                    String v;
                    if (i == 0) {
                        v = "Rp " + uang(Math.round(d.total * p));
                    } else if (i == 1) {
                        v = "Rp " + uang(Math.round(d.totalBulan[bulan - 1] * p));
                    } else if (i == 2) {
                        if (lalu == null || totalLalu <= 0) {
                            v = d.total > 0 && lalu != null ? "Tidak ada pembanding" : "-";
                        } else {
                            double pct = (d.total - totalLalu) * 100.0 / totalLalu;
                            long sel = d.total - totalLalu;
                            v = (sel >= 0 ? "\u25B2 " : "\u25BC ") + String.format(Locale.US, "%.1f%%", Math.abs(pct) * p)
                                    + "  \u2022  " + (sel >= 0 ? "+" : "-") + ringkas(Math.round(Math.abs(sel) * p));
                        }
                    } else {
                        v = topNilai > 0 ? uang(Math.round(topNilai * p)) + "  \u2022  " + topNama : "-";
                    }
                    String asli = v;
                    FontMetrics fm = g2.getFontMetrics();
                    while (fm.stringWidth(v) > lebar[i] - 36 && v.length() > 4) {
                        v = v.substring(0, v.length() - 2);
                    }
                    if (!v.equals(asli)) {
                        v = v.trim() + "\u2026";
                    }
                    g2.drawString(v, x + 20, y + 44);
                    g2.translate(0, -(1 - p) * 14);
                    g2.setComposite(lama);
                }
                x += lebar[i] + 12;
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    //  Tabel: 12 bulan + baris total, kolom BULAN tetap saat digulir ke samping
    // ------------------------------------------------------------------
    static final class TabelPanel extends JPanel implements Scrollable {

        private static final int PAD = 24, PAD_ATAS = 22, H_GRUP = 34, H_SUB = 50, ROW_H = 40, BOTTOM = 30;
        private static final int W_BULAN = 128, W_COL = 150, W_TOTAL = 150, W_BANDING = 124, N_BARIS = 13;
        private static final Font F_HEAD = new Font("Segoe UI", Font.BOLD, 12);
        private static final Font F_ISI = new Font("Segoe UI", Font.PLAIN, 13);
        private static final Font F_TEBAL = new Font("Segoe UI", Font.BOLD, 13);

        private final Data data, lalu;
        private final int tahun;
        private final IntConsumer klikBulan;
        private final long max;
        private final int[][] grup; // {kolom awal, kolom akhir (eksklusif), kelompok 0..2}
        private int bulan; // 0..11
        private boolean statis = false;
        private boolean memuat = false;
        private long mulai = 0;
        private Timer timer, timerGerak, timerMuat;
        private int hover = -1;
        private double hoverPos = 0, hoverA = 0, selPos;
        private int[] gx = new int[0];
        private int gShift = 0;

        TabelPanel(Data data, Data lalu, int tahun, int bulan, IntConsumer klikBulan) {
            this.data = data;
            this.lalu = lalu;
            this.tahun = tahun;
            this.bulan = bulan - 1;
            this.selPos = bulan - 1;
            this.klikBulan = klikBulan;
            long mx = 0;
            for (long t : data.totalBulan) {
                mx = Math.max(mx, t);
            }
            this.max = mx;
            this.grup = buatGrup();
            setOpaque(true);
            setPreferredSize(new Dimension(2 * PAD + W_BULAN + data.jmlKolom() * W_COL + W_TOTAL + (lalu != null ? W_BANDING : 0),
                    PAD_ATAS + H_GRUP + H_SUB + N_BARIS * ROW_H + BOTTOM));
            ToolTipManager.sharedInstance().registerComponent(this);
            timerGerak = new Timer(16, e -> tickGerak());
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int idx = baris(e.getY());
                    if (idx != hover) {
                        hover = idx;
                        setToolTipText(idx >= 0 ? tooltip(idx) : null);
                        setCursor(Cursor.getPredefinedCursor(idx >= 0 && idx < 12 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                        jalankanGerak();
                    }
                    if (idx < 0) {
                        int kc = kolomKepala(e.getX(), e.getY());
                        setToolTipText(kc >= 0 ? tooltipKolom(kc) : null);
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    if (hover != -1) {
                        hover = -1;
                        jalankanGerak();
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    int idx = baris(e.getY());
                    if (idx >= 0 && idx < 12 && TabelPanel.this.klikBulan != null && SwingUtilitiesLeft(e)) {
                        TabelPanel.this.klikBulan.accept(idx + 1);
                    }
                }
            });
        }

        /** Seluruh kolom bangsal berada dalam satu kelompok. */
        private int[][] buatGrup() {
            return data.jmlKolom() == 0 ? new int[0][] : new int[][]{{0, data.jmlKolom(), 0}};
        }

        private static boolean SwingUtilitiesLeft(MouseEvent e) {
            return javax.swing.SwingUtilities.isLeftMouseButton(e);
        }

        private int baris(int y) {
            int y0 = PAD_ATAS + H_GRUP + H_SUB;
            int idx = (y - y0) / ROW_H;
            return (y < y0 || idx < 0 || idx >= N_BARIS) ? -1 : idx;
        }

        /** Kolom bangsal di bawah kursor pada area kepala tabel (-1 bila tidak ada). */
        private int kolomKepala(int x, int y) {
            if (y < PAD_ATAS || y >= PAD_ATAS + H_GRUP + H_SUB || gx.length < 4 || x < PAD + gShift + W_BULAN) {
                return -1;
            }
            for (int c = 0; c < data.jmlKolom(); c++) {
                if (x >= gx[c + 1] && x < gx[c + 2]) {
                    return c;
                }
            }
            return -1;
        }

        private String tooltipKolom(int c) {
            String nm = data.namaKolom(c);
            StringBuilder sb = new StringBuilder("<html><b>").append(esc(nm)).append("</b><br>Total tahun ").append(tahun)
                    .append(" : <b>Rp ").append(uang(data.totalKolom[c])).append("</b>");
            if (data.total > 0) {
                sb.append(String.format(Locale.US, " (%.1f%% dari total)", data.totalKolom[c] * 100.0 / data.total));
            }
            if (lalu != null) {
                long lw = lalu.tahunDari(nm);
                sb.append("<br>Tahun ").append(tahun - 1).append(" (periode sama) : <b>Rp ").append(uang(lw)).append("</b> &nbsp;")
                        .append(delta(data.totalKolom[c], lw));
            }
            return sb.append("</html>").toString();
        }

        private String tooltip(int idx) {
            if (idx == 12) {
                String s = "<html><b>Total tahun " + tahun + "</b><br>Pendapatan <b>Rp " + uang(data.total) + "</b>";
                if (lalu != null) {
                    s += "<br>Tahun " + (tahun - 1) + " (periode sama) : <b>Rp " + uang(lalu.total) + "</b> &nbsp;" + delta(data.total, lalu.total);
                }
                return s + "</html>";
            }
            StringBuilder sb = new StringBuilder("<html><b>" + NAMA_BULAN[idx] + " " + tahun + "</b><br>");
            sb.append("Total : <b>Rp ").append(uang(data.totalBulan[idx])).append("</b>");
            int best = -1, aktif = 0;
            for (int c = 0; c < data.jmlKolom(); c++) {
                Long v = data.nilai[idx][c];
                if (v != null && v > 0) {
                    aktif++;
                    if (best < 0 || v > data.nilai[idx][best]) {
                        best = c;
                    }
                }
            }
            if (best >= 0) {
                sb.append("<br>Bangsal terbesar : <b>").append(esc(data.namaKolom(best))).append("</b> (").append(uang(data.nilai[idx][best])).append(")");
            } else if (data.totalBulan[idx] == 0) {
                sb.append("<br>Tidak ada data pada bulan ini");
            }
            if (lalu != null) {
                sb.append("<br>Tahun ").append(tahun - 1).append(" : <b>Rp ").append(uang(lalu.totalBulan[idx])).append("</b> &nbsp;")
                        .append(delta(data.totalBulan[idx], lalu.totalBulan[idx]));
            }
            return sb.append("</html>").toString();
        }

        private void jalankanGerak() {
            if (!timerGerak.isRunning()) {
                timerGerak.start();
            }
        }

        /** Sorotan hover dan sorotan bulan terpilih meluncur halus (bukan melompat). */
        private void tickGerak() {
            boolean lagi = false;
            if (hover >= 0) {
                if (hoverA < 0.02) {
                    hoverPos = hover;
                }
                double d = hover - hoverPos;
                if (Math.abs(d) > 0.005) {
                    hoverPos += d * 0.35;
                    lagi = true;
                } else {
                    hoverPos = hover;
                }
                if (hoverA < 1) {
                    hoverA = Math.min(1, hoverA + 0.18);
                    lagi = true;
                }
            } else if (hoverA > 0) {
                hoverA = Math.max(0, hoverA - 0.12);
                lagi = true;
            }
            double ds = bulan - selPos;
            if (Math.abs(ds) > 0.005) {
                selPos += ds * 0.25;
                lagi = true;
            } else {
                selPos = bulan;
            }
            repaint();
            if (!lagi) {
                timerGerak.stop();
            }
        }

        /** Pindah bulan terpilih (1..12) - sorotan meluncur ke baris bulan tersebut. */
        void setBulan(int b) {
            bulan = b - 1;
            jalankanGerak();
        }

        void setStatis(boolean v) {
            statis = v;
        }

        /** Saat true: tabel diredupkan dan muncul penanda "Memuat data" (dipakai ketika ganti tahun). */
        void setMemuat(boolean v) {
            memuat = v;
            if (v) {
                if (timerMuat == null) {
                    timerMuat = new Timer(16, e -> repaint());
                }
                timerMuat.start();
            } else if (timerMuat != null) {
                timerMuat.stop();
            }
            repaint();
        }

        void mulaiAnimasi() {
            statis = false;
            mulai = System.currentTimeMillis();
            if (timer != null) {
                timer.stop();
            }
            final long durasiTotal = 650L + (long) N_BARIS * delay();
            timer = new Timer(16, e -> {
                repaint();
                if (System.currentTimeMillis() - mulai >= durasiTotal + 40) {
                    ((Timer) e.getSource()).stop();
                    statis = true;
                    repaint();
                }
            });
            timer.start();
        }

        private static int delay() {
            return Math.min(70, 700 / N_BARIS);
        }

        void hentikan() {
            if (timer != null) {
                timer.stop();
            }
            if (timerGerak != null) {
                timerGerak.stop();
            }
            if (timerMuat != null) {
                timerMuat.stop();
            }
        }

        /** Kemajuan linear 0..1 untuk baris ke-i (masuk bergantian). */
        private double mentah(int i) {
            if (statis) {
                return 1.0;
            }
            return klem((System.currentTimeMillis() - mulai - (long) i * delay()) / 650.0);
        }

        /** Angka sel diberi rona warna bangsalnya. */
        private Color warnaKolom(int c) {
            return campur(TEKS, warnaBangsal(data.namaKolom(c)), 0.42);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, BG1, 0, h, BG2));
            g2.fillRect(0, 0, w, h);

            // ---------- geometri kolom ----------
            int n = data.jmlKolom();
            int wB = lalu != null ? W_BANDING : 0;
            double colW = n > 0 ? Math.max(W_COL, (w - 2.0 * PAD - W_BULAN - W_TOTAL - wB) / n) : W_COL;
            gx = new int[n + 4];
            gx[0] = PAD;
            gx[1] = PAD + W_BULAN;
            for (int k = 2; k <= n + 1; k++) {
                gx[k] = gx[1] + (int) Math.round((k - 1) * colW);
            }
            gx[n + 2] = gx[n + 1] + W_TOTAL;
            gx[n + 3] = gx[n + 2] + wB;
            Rectangle vis = getVisibleRect();
            gShift = Math.max(0, vis.x - PAD);

            // 1) bagian yang ikut menggulir (di kanan kolom BULAN)
            Shape awal = g2.getClip();
            int kiriGulir = PAD + gShift + W_BULAN;
            g2.clipRect(kiriGulir, 0, Math.max(0, w - kiriGulir), h);
            lukis(g2, false);
            g2.setClip(awal);

            // 2) kolom BULAN yang menempel di kiri
            g2.clipRect(PAD + gShift - 8, 0, W_BULAN + 8, h);
            g2.setPaint(new GradientPaint(0, 0, BG1, 0, h, BG2));
            g2.fillRect(0, 0, w, h);
            lukis(g2, true);
            g2.setClip(awal);
            if (gShift > 0) {
                int sx = PAD + gShift + W_BULAN;
                g2.setPaint(new LinearGradientPaint(sx, 0, sx + 12, 0, new float[]{0f, 1f},
                        new Color[]{new Color(0, 0, 0, 90), new Color(0, 0, 0, 0)}));
                g2.fillRect(sx, 0, 12, h);
            }

            int y0 = PAD_ATAS + H_GRUP + H_SUB;

            // kilau cahaya yang menyapu isi tabel satu kali setelah baris-baris masuk
            if (!statis && !memuat && data.ada) {
                double sp = (System.currentTimeMillis() - mulai - 380) / 950.0;
                if (sp > 0 && sp < 1) {
                    int kw = 170;
                    int cx = (int) Math.round(vis.x - kw + (vis.width + 2.0 * kw) * kubik(sp));
                    Shape cLama = g2.getClip();
                    g2.clipRect(vis.x, y0, vis.width, N_BARIS * ROW_H);
                    int a = (int) Math.round(46 * Math.sin(Math.PI * sp));
                    g2.setPaint(new LinearGradientPaint(cx - kw / 2f, 0, cx + kw / 2f, 0, new float[]{0f, 0.5f, 1f},
                            new Color[]{new Color(255, 255, 255, 0), new Color(255, 255, 255, a), new Color(255, 255, 255, 0)}));
                    g2.fillRect(cx - kw / 2, y0, kw, N_BARIS * ROW_H);
                    g2.setClip(cLama);
                }
            }
            if (!data.ada && !memuat) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                g2.setColor(TEKS_REDUP);
                tengah(g2, "Tidak ada data pendapatan rawat inap pada tahun " + tahun + ".", vis.x, vis.width, y0 + N_BARIS * ROW_H + 22);
            }

            // ---------- lapisan "Memuat data" ----------
            if (memuat) {
                g2.setColor(alfa(BG1, 175));
                g2.fillRect(0, 0, w, h);
                long now = System.currentTimeMillis();
                int pw = 200, ph = 44;
                int px = vis.x + (vis.width - pw) / 2, py = y0 + 2 * ROW_H;
                g2.setColor(alfa(Color.WHITE, 22));
                g2.fillRoundRect(px, py, pw, ph, ph, ph);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(alfa(Color.WHITE, 60));
                g2.drawRoundRect(px, py, pw, ph, ph, ph);
                int sx = px + 16, sy = py + 11;
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(alfa(Color.WHITE, 40));
                g2.drawOval(sx, sy, 22, 22);
                g2.setColor(BIRU);
                g2.drawArc(sx, sy, 22, 22, -(int) ((now / 4) % 360), 100);
                g2.setFont(F_TEBAL);
                g2.setColor(TEKS);
                String titik = "...".substring(0, (int) ((now / 350) % 4));
                g2.drawString("Memuat data" + titik, px + 50, py + 27);
            }
            g2.dispose();
        }

        /** Gambar isi tabel; sticky=true hanya menggambar kolom BULAN, sticky=false menggambar sisanya. */
        private void lukis(Graphics2D g2, boolean sticky) {
            int n = data.jmlKolom();
            int wB = lalu != null ? W_BANDING : 0;
            int[] xs = gx;
            int kanan = xs[n + 3];
            int yG = PAD_ATAS, yS = yG + H_GRUP, y0 = yS + H_SUB;
            int tabelBawah = y0 + N_BARIS * ROW_H;
            Rectangle vis = getVisibleRect();

            // ---------- kepala tabel ----------
            g2.setPaint(new GradientPaint(0, yG, new Color(255, 255, 255, 26), 0, yG + H_GRUP + H_SUB, new Color(255, 255, 255, 10)));
            g2.fillRoundRect(PAD - 8, yG, kanan - PAD + 16, H_GRUP + H_SUB, 14, 14);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawRoundRect(PAD - 8, yG, kanan - PAD + 16, H_GRUP + H_SUB, 14, 14);

            int tengahKepala = yG + (H_GRUP + H_SUB) / 2;
            g2.setFont(F_HEAD);
            if (sticky) {
                g2.translate(gShift, 0);
                g2.setColor(TEKS);
                tengah(g2, "BULAN", xs[0], W_BULAN, tengahKepala + 5);
                g2.translate(-gShift, 0);
            } else {
                // kelompok kolom (IRJ / IGD / PENUNJANG): judul rata tengah di bagian yang terlihat + garis aksen yang tumbuh
                long kini = System.currentTimeMillis();
                double kg = statis ? 1.0 : easeOut((kini - mulai - 120) / 600.0);
                int kiriLihat = Math.max(xs[1], PAD + gShift + W_BULAN);
                int kananLihat = Math.min(xs[n + 1], vis.x + vis.width);
                for (int[] gr : grup) {
                    int ga = xs[gr[0] + 1], gb = xs[gr[1] + 1];
                    Color wg = WARNA_GRUP[gr[2]];
                    int l = Math.max(ga, kiriLihat), r = Math.min(gb, kananLihat);
                    if (r > l) {
                        g2.setColor(alfa(campur(wg, Color.WHITE, 0.55), (int) (255 * kg)));
                        tengah(g2, NAMA_GRUP[gr[2]], l, r - l, yG + 22);
                    }
                    g2.setPaint(new GradientPaint(ga, 0, wg, gb, 0, alfa(wg, 40)));
                    g2.fillRoundRect(ga + 6, yG + H_GRUP - 3, (int) Math.round(Math.max(0, gb - ga - 12) * kg), 3, 3, 3);
                }

                // judul tiap kolom (muncul bergantian dari kiri ke kanan)
                java.awt.Composite cLama = g2.getComposite();
                for (int c = 0; c < n; c++) {
                    int k = c + 1;
                    int lb = xs[k + 1] - xs[k];
                    double ca = statis ? 1.0 : easeOut((kini - mulai - 160 - c * 45L) / 420.0);
                    if (ca <= 0.01) {
                        continue;
                    }
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) klem(ca)));
                    int dy = (int) Math.round((1 - ca) * -7);
                    g2.setColor(warnaBangsal(data.namaKolom(c)));
                    String[] bs = bagi(data.namaKolom(c), g2.getFontMetrics(), lb - 14);
                    if (bs.length == 1) {
                        tengah(g2, bs[0], xs[k], lb, yS + H_SUB / 2 + 4 + dy);
                    } else {
                        tengah(g2, bs[0], xs[k], lb, yS + H_SUB / 2 - 3 + dy);
                        tengah(g2, bs[1], xs[k], lb, yS + H_SUB / 2 + 13 + dy);
                    }
                }
                g2.setComposite(cLama);
                g2.setColor(EMAS);
                tengah(g2, "TOTAL", xs[n + 1], W_TOTAL, tengahKepala + 5);
                if (lalu != null) {
                    g2.setColor(BIRU);
                    String[] bs = {"VS", "TAHUN " + (tahun - 1)};
                    tengah(g2, bs[0], xs[n + 2], wB, tengahKepala - 3);
                    tengah(g2, bs[1], xs[n + 2], wB, tengahKepala + 14);
                }

                // pemisah vertikal kepala
                g2.setColor(new Color(255, 255, 255, 30));
                for (int[] gr : grup) {
                    g2.drawLine(xs[gr[0] + 1], yG + 6, xs[gr[0] + 1], yS + H_SUB - 6);
                }
                g2.drawLine(xs[n + 1], yG + 6, xs[n + 1], yS + H_SUB - 6);
                if (lalu != null) {
                    g2.drawLine(xs[n + 2], yG + 6, xs[n + 2], yS + H_SUB - 6);
                }
                g2.setColor(new Color(255, 255, 255, 18));
                for (int k = 2; k <= n; k++) {
                    g2.drawLine(xs[k], yS + 6, xs[k], yS + H_SUB - 6);
                }
                g2.drawLine(xs[1], yS, xs[n + 1], yS);
            }

            // ---------- sorotan bulan terpilih & hover (meluncur halus) ----------
            if (!memuat) {
                int sy = y0 + (int) Math.round(selPos * ROW_H);
                g2.setPaint(new LinearGradientPaint(PAD - 8, 0, kanan + 8, 0, new float[]{0f, 0.5f, 1f},
                        new Color[]{alfa(AKSEN1, 84), alfa(AKSEN1, 34), alfa(AKSEN1, 18)}));
                g2.fillRoundRect(PAD - 8, sy + 2, kanan - PAD + 16, ROW_H - 4, 12, 12);
                g2.setColor(alfa(AKSEN2, 235));
                g2.fillRoundRect(PAD - 8, sy + 10, 3, ROW_H - 20, 3, 3);
                if (hoverA > 0.01) {
                    int hy = y0 + (int) Math.round(hoverPos * ROW_H);
                    g2.setColor(alfa(Color.WHITE, (int) (26 * hoverA)));
                    g2.fillRoundRect(PAD - 8, hy + 2, kanan - PAD + 16, ROW_H - 4, 12, 12);
                }
            }

            // ---------- baris data ----------
            for (int i = 0; i < N_BARIS; i++) {
                int y = y0 + i * ROW_H;
                double t = mentah(i);
                if (t <= 0) {
                    continue;
                }
                double p = easeOut(t);
                double pp = statis ? 1.0 : p;
                java.awt.Composite oldC = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) p));
                g2.translate((1 - p) * 28, 0);

                boolean total = i == 12;
                boolean top = !total && max > 0 && data.totalBulan[i] == max;
                if (total) {
                    g2.setPaint(new LinearGradientPaint(PAD - 8, 0, kanan + 8, 0, new float[]{0f, 0.6f, 1f},
                            new Color[]{alfa(EMAS, 60), alfa(EMAS, 28), alfa(EMAS, 16)}));
                    g2.fillRoundRect(PAD - 8, y + 2, kanan - PAD + 16, ROW_H - 4, 12, 12);
                } else if (top) {
                    g2.setPaint(new LinearGradientPaint(PAD - 8, 0, kanan + 8, 0, new float[]{0f, 0.6f, 1f},
                            new Color[]{alfa(EMAS, 46), alfa(EMAS, 12), alfa(EMAS, 0)}));
                    g2.fillRoundRect(PAD - 8, y + 2, kanan - PAD + 16, ROW_H - 4, 12, 12);
                } else if (i % 2 == 0) {
                    g2.setColor(new Color(255, 255, 255, 9));
                    g2.fillRoundRect(PAD - 8, y + 2, kanan - PAD + 16, ROW_H - 4, 12, 12);
                }
                int base = y + ROW_H / 2 + 5;

                if (sticky) {
                    g2.translate(gShift, 0);
                    if (total) {
                        g2.setFont(F_TEBAL);
                        g2.setColor(EMAS);
                        g2.drawString("TOTAL", xs[0] + 16, base);
                    } else {
                        boolean sel = i == bulan;
                        g2.setFont(sel || top ? F_TEBAL : F_ISI);
                        g2.setColor(top ? EMAS : (sel ? Color.WHITE : TEKS));
                        g2.drawString(NAMA_BULAN[i].toUpperCase(Locale.ROOT), xs[0] + 16, base);
                    }
                    g2.translate(-gShift, 0);
                } else {
                    for (int c = 0; c < n; c++) {
                        int k = c + 1;
                        Long v = total ? Long.valueOf(data.totalKolom[c]) : data.nilai[i][c];
                        if (v == null) {
                            continue;
                        }
                        g2.setFont(total ? F_TEBAL : F_ISI);
                        g2.setColor(total ? EMAS : (v == 0 ? TEKS_REDUP : warnaKolom(c)));
                        String sv = uang(Math.round(v * pp));
                        g2.drawString(sv, xs[k + 1] - 14 - g2.getFontMetrics().stringWidth(sv), base);
                    }
                    // kolom TOTAL + bilah mini (tumbuh saat animasi)
                    long tv = total ? data.total : data.totalBulan[i];
                    g2.setFont(F_TEBAL);
                    g2.setColor(total || top ? EMAS : Color.WHITE);
                    String st = uang(Math.round(tv * pp));
                    g2.drawString(st, xs[n + 2] - 16 - g2.getFontMetrics().stringWidth(st), base - (total ? 0 : 3));
                    if (!total) {
                        int bxl = xs[n + 1] + 16, bwMax = W_TOTAL - 32;
                        int bwTot = max > 0 && tv > 0 ? Math.max(3, (int) Math.round(bwMax * tv / (double) max)) : 0;
                        int bwAnim = (int) Math.round(bwTot * p);
                        g2.setColor(new Color(255, 255, 255, 22));
                        g2.fillRoundRect(bxl, y + ROW_H - 9, bwMax, 3, 3, 3);
                        if (bwAnim >= 2) {
                            g2.setPaint(new GradientPaint(bxl, 0, new Color(0xFF, 0xE2, 0x7A), bxl + bwTot, 0, new Color(0xE5, 0xA8, 0x00)));
                            g2.fillRoundRect(bxl, y + ROW_H - 9, bwAnim, 3, 3, 3);
                        }
                    }
                    if (lalu != null) {
                        long ini = total ? data.total : data.totalBulan[i];
                        long lw = total ? lalu.total : lalu.totalBulan[i];
                        gambarDelta(g2, xs[n + 2], W_BANDING, y, ini, lw);
                    }
                }
                g2.translate(-(1 - p) * 28, 0);
                g2.setComposite(oldC);
            }

            // garis pemisah halus antar kolom pada isi
            g2.setColor(new Color(255, 255, 255, 14));
            for (int k = 1; k <= n + (lalu != null ? 2 : 1); k++) {
                g2.drawLine(xs[k], y0 + 4, xs[k], tabelBawah - 4);
            }
            g2.setColor(new Color(255, 255, 255, 36));
            for (int[] gr : grup) {
                g2.drawLine(xs[gr[0] + 1], y0 + 4, xs[gr[0] + 1], tabelBawah - 4);
            }
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawLine(PAD - 8, tabelBawah, kanan + 8, tabelBawah);
        }

        /** Lencana selisih terhadap periode sama tahun lalu: hijau naik, merah turun, redup bila tidak ada pembanding. */
        private void gambarDelta(Graphics2D g2, int x, int lebar, int y, long ini, long lw) {
            String t = delta(ini, lw);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            Color c = warnaDelta(ini, lw);
            if (lw <= 0 && ini <= 0) {
                g2.setColor(TEKS_REDUP);
                tengah(g2, "-", x, lebar, y + ROW_H / 2 + 5);
                return;
            }
            int bw = fm.stringWidth(t) + 22, bh = 22, bx = x + (lebar - bw) / 2, by = y + (ROW_H - bh) / 2;
            g2.setColor(alfa(c, 34));
            g2.fillRoundRect(bx, by, bw, bh, bh, bh);
            g2.setColor(alfa(c, 120));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(bx, by, bw, bh, bh, bh);
            g2.setColor(c);
            tengah(g2, t, x, lebar, by + 15);
        }

        // --- Scrollable ---
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return o == SwingConstants.HORIZONTAL ? W_COL / 2 : ROW_H;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return o == SwingConstants.HORIZONTAL ? Math.max(W_COL, r.width - W_COL) : Math.max(60, r.height - 60);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getParent() instanceof JViewport
                    && ((JViewport) getParent()).getWidth() > getPreferredSize().width;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport
                    && ((JViewport) getParent()).getHeight() > getPreferredSize().height;
        }
    }

    // ------------------------------------------------------------------
    //  Kontrol segmen: pil aksen meluncur halus ke pilihan yang diklik
    // ------------------------------------------------------------------
    static final class Segmen extends JComponent {

        private static final Font FONT = new Font("Segoe UI", Font.BOLD, 12);
        private final String[] teks;
        private final IntConsumer aksi;
        private final int lebarItem;
        private int sel, hover = -1;
        private double pos;
        private boolean aktif = true;
        private Timer timer;

        Segmen(String[] teks, int awal, IntConsumer aksi) {
            this.teks = teks;
            this.aksi = aksi;
            this.sel = awal;
            this.pos = awal;
            int mx = 0;
            FontMetrics fm = getFontMetrics(FONT);
            for (String t : teks) {
                mx = Math.max(mx, fm.stringWidth(t));
            }
            lebarItem = mx + 30;
            setPreferredSize(new Dimension(lebarItem * teks.length + 8, 34));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int i = indeks(e.getX());
                    if (i != hover) {
                        hover = i;
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hover = -1;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    int i = indeks(e.getX());
                    if (aktif && i >= 0 && i != sel && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                        pilih(i, true);
                    }
                }
            });
        }

        private int indeks(int x) {
            int i = (x - 4) / lebarItem;
            return (x < 4 || i < 0 || i >= teks.length) ? -1 : i;
        }

        void pilih(int i, boolean panggil) {
            if (i < 0 || i >= teks.length) {
                return;
            }
            boolean beda = i != sel;
            sel = i;
            if (timer == null) {
                timer = new Timer(16, e -> {
                    double d = sel - pos;
                    if (Math.abs(d) < 0.01) {
                        pos = sel;
                        ((Timer) e.getSource()).stop();
                    } else {
                        pos += d * 0.28;
                    }
                    repaint();
                });
            }
            timer.start();
            if (panggil && beda && aksi != null) {
                aksi.accept(i);
            }
        }

        /** Pilih tanpa animasi dan tanpa memanggil aksi. */
        void setLangsung(int i) {
            sel = i;
            pos = i;
            repaint();
        }

        void setAktif(boolean a) {
            aktif = a;
            setCursor(Cursor.getPredefinedCursor(a ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            repaint();
        }

        void hentikan() {
            if (timer != null) {
                timer.stop();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(255, 255, 255, aktif ? 18 : 10));
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setColor(new Color(255, 255, 255, 46));
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
            int px = 4 + (int) Math.round(pos * lebarItem);
            if (aktif) {
                // cahaya lembut di bawah pil + pil sedikit "memanjang" saat sedang meluncur
                double geser = Math.min(1, Math.abs(sel - pos));
                int ext = (int) Math.round(geser * 10);
                Shape klipLama = g2.getClip();
                g2.clip(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, h, h));
                g2.setPaint(new RadialGradientPaint(px + lebarItem / 2f, h / 2f, lebarItem * 0.8f, new float[]{0f, 1f},
                        new Color[]{alfa(AKSEN2, 90), alfa(AKSEN2, 0)}));
                g2.fillRect(px - lebarItem / 3, 0, lebarItem + 2 * (lebarItem / 3), h);
                g2.setClip(klipLama);
                px -= ext / 2;
                g2.setPaint(new GradientPaint(px, 0, AKSEN1, px + lebarItem, h, AKSEN2));
            } else {
                g2.setColor(new Color(255, 255, 255, 40));
            }
            g2.fillRoundRect(px, 4, lebarItem + (aktif ? (int) Math.round(Math.min(1, Math.abs(sel - pos)) * 10) : 0), h - 8, h - 8, h - 8);
            g2.setFont(FONT);
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < teks.length; i++) {
                double dekat = 1 - Math.min(1, Math.abs(pos - i));
                Color c = campur(i == hover && aktif ? Color.WHITE : TEKS_REDUP, Color.WHITE, dekat);
                g2.setColor(aktif ? c : alfa(c, 120));
                tengah(g2, teks[i], 4 + i * lebarItem, lebarItem, (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    //  Grafik perbandingan: batang bertumpuk per bulan, tahun ini vs periode sama tahun lalu (pudar)
    // ------------------------------------------------------------------
    static final class GrafikPanel extends JPanel {

        private static final Color[][] WARNA = {
            {new Color(0x4F, 0xC3, 0xF7), new Color(0x21, 0x73, 0xE6)},
            {new Color(0xC9, 0xB8, 0xFF), new Color(0x7C, 0x5C, 0xE0)},
            {new Color(0x9B, 0xF0, 0xB8), new Color(0x2E, 0xA8, 0x5F)},
            {new Color(0xFF, 0xC8, 0x6E), new Color(0xFB, 0x8C, 0x00)},
            {new Color(0xFF, 0xA8, 0xB8), new Color(0xE0, 0x4A, 0x68)},
            {new Color(0x80, 0xDE, 0xEA), new Color(0x00, 0xAC, 0xC1)},
            {new Color(0xCF, 0xD8, 0xDC), new Color(0x78, 0x90, 0x9C)}};

        private static String namaKomp(int k) {
            return KOMP_NAMA[k];
        }

        private static final long DURASI_MS = 1300L;
        private static final int KH = 84; // tinggi kartu ringkasan di bawah grafik

        private Data d = Data.kosong(), lalu = Data.kosong();
        private int tahun, bulan;
        private int[] akt = {0, 1, 2, 3, 4, 5, 6};  // komponen yang punya nilai (dipakai legenda, kartu, tooltip)
        private double[][] cur = new double[12][NK], prev = new double[12][NK];             // nilai tujuan
        private double[][] curAsal = new double[12][NK], prevAsal = new double[12][NK];     // nilai awal (morph)
        private boolean statis, memuat;
        private double progress = 1, hoverA, selPos = 0;
        private boolean awalData = true;
        private long mulai;
        private int hover = -1, mx, my;
        private int pKiri, pAtas, pBawah;
        private double pGw = 1;
        private final IntConsumer klik;
        private Timer timer, timerMuat;

        GrafikPanel(IntConsumer klik) {
            this.klik = klik;
            setOpaque(true);
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mx = e.getX();
                    my = e.getY();
                    int baru = -1;
                    if (pGw > 1 && mx >= pKiri && mx < pKiri + pGw * 12 && my >= pAtas - 14 && my <= pBawah + 30) {
                        baru = Math.min(11, (int) ((mx - pKiri) / pGw));
                    }
                    if (baru != hover) {
                        hover = baru;
                        setCursor(Cursor.getPredefinedCursor(baru >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    }
                    jalan();
                    repaint();
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hover = -1;
                    jalan();
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (hover >= 0 && GrafikPanel.this.klik != null && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                        GrafikPanel.this.klik.accept(hover + 1);
                    }
                }
            });
        }

        // ---------- data & animasi ----------
        /** Ganti data. dariNol=false: batang bergerak (morph) dari tampilan sekarang ke data baru. */
        void setData(Data d, Data lalu, int tahun, int bulan, boolean dariNol) {
            double[][] asalC = dariNol ? new double[12][NK] : tampil(curAsal, cur);
            double[][] asalP = dariNol ? new double[12][NK] : tampil(prevAsal, prev);
            this.d = d;
            this.lalu = lalu == null ? Data.kosong() : lalu;
            this.tahun = tahun;
            this.bulan = bulan - 1;
            if (awalData) {
                selPos = this.bulan;
                awalData = false;
            }
            curAsal = asalC;
            prevAsal = asalP;
            for (int m = 0; m < 12; m++) {
                long[] a = d.komponen(m), b = this.lalu.komponen(m);
                for (int k = 0; k < NK; k++) {
                    cur[m][k] = a[k];
                    prev[m][k] = b[k];
                }
            }
            java.util.ArrayList<Integer> pakai = new java.util.ArrayList<>();
            for (int k = 0; k < NK; k++) {
                double sm = 0;
                for (int m = 0; m < 12; m++) {
                    sm += Math.abs(cur[m][k]) + Math.abs(prev[m][k]);
                }
                if (sm > 0) {
                    pakai.add(k);
                }
            }
            if (pakai.isEmpty()) {
                for (int k = 0; k < NK; k++) {
                    pakai.add(k);
                }
            }
            akt = new int[pakai.size()];
            for (int i = 0; i < akt.length; i++) {
                akt[i] = pakai.get(i);
            }
            mulaiAnimasi();
        }

        void setBulan(int b) {
            bulan = b - 1;
            jalan();
        }

        void mulaiAnimasi() {
            statis = false;
            progress = 0;
            mulai = System.currentTimeMillis();
            jalan();
        }

        /** Ulangi animasi dari batang kosong. */
        void ulang() {
            curAsal = new double[12][NK];
            prevAsal = new double[12][NK];
            mulaiAnimasi();
        }

        /** Tanpa animasi (untuk menyimpan gambar). */
        void setStatis() {
            statis = true;
            progress = 1;
            hover = -1;
            hoverA = 0;
            selPos = bulan;
            hentikan();
        }

        void setMemuat(boolean v) {
            memuat = v;
            if (v) {
                if (timerMuat == null) {
                    timerMuat = new Timer(16, e -> repaint());
                }
                timerMuat.start();
            } else if (timerMuat != null) {
                timerMuat.stop();
            }
            repaint();
        }

        void hentikan() {
            if (timer != null) {
                timer.stop();
            }
            if (timerMuat != null) {
                timerMuat.stop();
            }
        }

        private void jalan() {
            if (timer == null) {
                timer = new Timer(16, e -> tick());
            }
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        private void tick() {
            boolean lagi = false;
            if (!statis && progress < 1) {
                progress = Math.min(1, (System.currentTimeMillis() - mulai) / (double) DURASI_MS);
                lagi = progress < 1;
            }
            double ds = bulan - selPos;
            if (Math.abs(ds) > 0.005) {
                selPos += ds * 0.25;
                lagi = true;
            } else {
                selPos = bulan;
            }
            double target = hover >= 0 ? 1 : 0;
            if (Math.abs(target - hoverA) > 0.01) {
                hoverA = Math.max(0, Math.min(1, hoverA + (target > hoverA ? 0.18 : -0.12)));
                lagi = true;
            } else {
                hoverA = target;
            }
            repaint();
            if (!lagi) {
                timer.stop();
            }
        }

        /** Kemajuan batang bulan m: masuk bergantian dari kiri ke kanan, sedikit memantul. */
        private double eBulan(int m) {
            return statis ? 1.0 : balik(progress * 1.45 - m * 0.04);
        }

        private double[][] tampil(double[][] asal, double[][] tuj) {
            double[][] r = new double[12][NK];
            for (int m = 0; m < 12; m++) {
                double e = eBulan(m);
                for (int k = 0; k < NK; k++) {
                    r[m][k] = Math.max(0, asal[m][k] + (tuj[m][k] - asal[m][k]) * e);
                }
            }
            return r;
        }

        private static double jml(double[] v) {
            double s = 0;
            for (double a : v) {
                s += a;
            }
            return s;
        }

        private static double maksTumpuk(double[][] a, double[][] b) {
            double mk = 0;
            for (int m = 0; m < 12; m++) {
                mk = Math.max(mk, Math.max(jml(a[m]), jml(b[m])));
            }
            return mk;
        }

        private static double niceMax(double max) {
            if (max <= 0) {
                return 5;
            }
            double kasar = max / 5.0;
            double pangkat = Math.pow(10, Math.floor(Math.log10(kasar)));
            double f = kasar / pangkat, s = 10;
            for (double p : new double[]{1, 1.5, 2, 2.5, 3, 4, 5, 6, 8, 10}) {
                if (f <= p + 1e-9) {
                    s = p;
                    break;
                }
            }
            return s * pangkat * 5;
        }

        // ---------- lukisan ----------
        private void tumpuk(Graphics2D g, double x, double bw, double[] v, int base, int ph, double axis, boolean pudar, boolean glow) {
            double hTot = ph * jml(v) / axis;
            if (hTot < 0.6) {
                return;
            }
            double top = base - hTot;
            Shape lama = g.getClip();
            if (glow) {
                g.setColor(alfa(AKSEN2, 55));
                g.fill(new RoundRectangle2D.Double(x - 3, top - 3, bw + 6, hTot + 6, 14, 14));
            }
            g.clip(new Rectangle2D.Double(0, 0, 100000, base));
            g.clip(new RoundRectangle2D.Double(x, top, bw, hTot + bw, 10, 10));
            int a = pudar ? 105 : 255;
            double y = base;
            for (int k = 0; k < NK; k++) {
                double hk = ph * v[k] / axis;
                if (hk <= 0) {
                    continue;
                }
                g.setPaint(new GradientPaint(0, (float) (y - hk), alfa(WARNA[k][0], a), 0, (float) y, alfa(WARNA[k][1], a)));
                g.fill(new Rectangle2D.Double(x, y - hk, bw, hk));
                if (!pudar && hk > 2) {
                    g.setColor(new Color(0, 0, 0, 50));
                    g.fill(new Rectangle2D.Double(x, y - hk, bw, 1));
                }
                y -= hk;
            }
            if (!pudar) {
                g.setPaint(new GradientPaint((float) x, 0, new Color(255, 255, 255, 54), (float) (x + bw), 0, new Color(255, 255, 255, 0)));
                g.fill(new Rectangle2D.Double(x, top, bw, hTot));
            }
            g.setClip(lama);
        }

        private void lencana(Graphics2D g, String t, Color c, int cx, int y) {
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            int tw = g.getFontMetrics().stringWidth(t) + 18;
            g.setColor(alfa(BG1, 235));
            g.fillRoundRect(cx - tw / 2, y, tw, 20, 20, 20);
            g.setColor(alfa(c, 70));
            g.fillRoundRect(cx - tw / 2, y, tw, 20, 20, 20);
            g.setColor(alfa(c, 170));
            g.drawRoundRect(cx - tw / 2, y, tw, 20, 20, 20);
            g.setColor(c);
            g.drawString(t, cx - tw / 2 + 9, y + 14);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, BG1, 0, h, BG2));
            g.fillRect(0, 0, w, h);

            final int kiri = 76, kanan = 30, atas = 66;
            int bawah = h - KH - 50;
            int pw = w - kiri - kanan, ph = bawah - atas;
            if (pw < 160 || ph < 70) {
                g.dispose();
                return;
            }
            pKiri = kiri;
            pAtas = atas;
            pBawah = bawah;
            pGw = pw / 12.0;
            double eG = statis ? 1.0 : kubik(progress);

            if (!d.ada && !lalu.ada && !memuat) {
                g.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                g.setColor(TEKS_REDUP);
                tengah(g, "Tidak ada data pendapatan pada tahun " + tahun + " maupun " + (tahun - 1) + ".", 0, w, h / 2);
                g.dispose();
                return;
            }

            // ---- legenda ----
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            FontMetrics fm = g.getFontMetrics();
            int lx = kiri, ly = 32;
            for (int k : akt) {
                g.setPaint(new GradientPaint(0, ly - 11, WARNA[k][0], 0, ly + 1, WARNA[k][1]));
                g.fillRoundRect(lx, ly - 11, 12, 12, 4, 4);
                g.setColor(TEKS);
                String nm = namaKomp(k);
                g.drawString(nm, lx + 18, ly);
                lx += 18 + fm.stringWidth(nm) + 20;
            }
            lx += 12;
            g.setColor(alfa(Color.WHITE, 235));
            g.fillRoundRect(lx, ly - 11, 12, 12, 4, 4);
            g.setColor(TEKS);
            g.drawString("Tahun " + tahun, lx + 18, ly);
            lx += 18 + fm.stringWidth("Tahun " + tahun) + 20;
            g.setColor(alfa(Color.WHITE, 90));
            g.fillRoundRect(lx, ly - 11, 12, 12, 4, 4);
            g.setColor(TEKS);
            g.drawString("Tahun " + (tahun - 1) + " (pudar, periode sama)", lx + 18, ly);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.setColor(EMAS);
            String ket = "DATA : " + d.label.toUpperCase(Locale.ROOT);
            g.drawString(ket, w - kanan - g.getFontMetrics().stringWidth(ket), ly);

            // ---- skala (meluncur halus saat data berganti) ----
            double mkAsal = maksTumpuk(curAsal, prevAsal), mkTuj = maksTumpuk(cur, prev);
            double axTuj = niceMax(mkTuj);
            double axis = mkAsal <= 0 || statis ? axTuj : niceMax(mkAsal) + (axTuj - niceMax(mkAsal)) * eG;
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            fm = g.getFontMetrics();
            for (int i = 0; i <= 5; i++) {
                int y = bawah - (int) Math.round(ph * i / 5.0);
                g.setColor(new Color(255, 255, 255, i == 0 ? 60 : 20));
                g.drawLine(kiri, y, kiri + pw, y);
                g.setColor(TEKS_REDUP);
                String t = ringkas(Math.round(axis * i / 5.0));
                g.drawString(t, kiri - 8 - fm.stringWidth(t), y + 4);
            }

            // ---- sorotan bulan terpilih (meluncur) & bulan yang ditunjuk ----
            double sx = kiri + selPos * pGw;
            g.setPaint(new GradientPaint(0, atas - 8, alfa(AKSEN1, 70), 0, bawah + 30, alfa(AKSEN1, 10)));
            g.fillRoundRect((int) sx + 3, atas - 8, (int) pGw - 6, ph + 40, 14, 14);
            g.setColor(alfa(AKSEN2, 230));
            g.fillRoundRect((int) (sx + pGw / 2 - 18), atas - 8, 36, 3, 3, 3);
            if (hover >= 0 && hoverA > 0.01) {
                g.setColor(alfa(Color.WHITE, (int) (18 * hoverA)));
                g.fillRoundRect((int) (kiri + hover * pGw) + 3, atas - 8, (int) pGw - 6, ph + 40, 14, 14);
            }

            // ---- batang ----
            double[][] dc = tampil(curAsal, cur), dp = tampil(prevAsal, prev);
            double bw = Math.min(34, pGw * 0.30), gap = 4;
            for (int m = 0; m < 12; m++) {
                double cx = kiri + m * pGw + pGw / 2;
                double xPrev = cx - gap / 2 - bw, xCur = cx + gap / 2;
                boolean sorot = m == bulan || m == hover;
                tumpuk(g, xPrev, bw, dp[m], bawah, ph, axis, true, false);
                tumpuk(g, xCur, bw, dc[m], bawah, ph, axis, false, sorot);
                double fa = lim((eBulan(m) - 0.8) / 0.2);
                if (fa > 0.02) {
                    java.awt.Composite cLabel = g.getComposite();
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) fa));
                    g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    fm = g.getFontMetrics();
                    double tp = jml(prev[m]), tc = jml(cur[m]);
                    if (tp > 0 && pGw >= 70 && (m == bulan || m == hover)) {
                        String s = ringkas(Math.round(tp));
                        g.setColor(TEKS_REDUP);
                        g.drawString(s, (int) (xPrev + bw / 2 - fm.stringWidth(s) / 2.0), (int) (bawah - ph * tp / axis) - 5);
                    }
                    if (tc > 0) {
                        String s = ringkas(Math.round(tc));
                        g.setColor(Color.WHITE);
                        g.drawString(s, (int) (xCur + bw / 2 - fm.stringWidth(s) / 2.0), (int) (bawah - ph * tc / axis) - 5);
                    }
                    g.setComposite(cLabel);
                }
                boolean sel = m == bulan;
                g.setFont(new Font("Segoe UI", sel || m == hover ? Font.BOLD : Font.PLAIN, 11));
                fm = g.getFontMetrics();
                String bl = NAMA_BULAN[m].substring(0, 3);
                g.setColor(sel ? EMAS : (m == hover ? Color.WHITE : new Color(170, 185, 215)));
                g.drawString(bl, (int) (cx - fm.stringWidth(bl) / 2.0), bawah + 18);
            }

            // ---- lencana selisih untuk bulan terpilih ----
            if (statis || progress >= 0.98) {
                long tc = Math.round(jml(cur[bulan])), tp = Math.round(jml(prev[bulan]));
                if (tc > 0 || tp > 0) {
                    double hMax = ph * Math.max(tc, tp) / axis;
                    int by = Math.max(atas - 24, (int) (bawah - hMax) - 36);
                    lencana(g, delta(tc, tp), warnaDelta(tc, tp), (int) (kiri + bulan * pGw + pGw / 2), by);
                }
            }

            // ---- kartu ringkasan per komponen ----
            double[] sAsal = new double[NK], sTuj = new double[NK], sPrev = new double[NK];
            for (int m = 0; m < 12; m++) {
                for (int k = 0; k < NK; k++) {
                    sAsal[k] += curAsal[m][k];
                    sTuj[k] += cur[m][k];
                    sPrev[k] += prev[m][k];
                }
            }
            double[] sTampil = new double[NK];
            double sumT = 0;
            for (int k = 0; k < NK; k++) {
                sTampil[k] = statis ? sTuj[k] : sAsal[k] + (sTuj[k] - sAsal[k]) * eG;
                sumT += sTampil[k];
            }
            int yK = h - KH - 12;
            int nA = akt.length;
            int cw = (w - 2 * 28 - (nA - 1) * 12) / nA;
            for (int ik = 0; ik < nA; ik++) {
                int k = akt[ik];
                int x = 28 + ik * (cw + 12);
                // kartu naik & memudar masuk bergantian
                double cp = statis ? 1.0 : easeOut((progress - 0.12 - ik * 0.07) / 0.5);
                java.awt.Composite cKartu = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) klem(cp)));
                g.translate(0, (1 - cp) * 14);
                g.setPaint(new GradientPaint(0, yK, new Color(255, 255, 255, 34), 0, yK + KH, new Color(255, 255, 255, 12)));
                g.fillRoundRect(x, yK, cw, KH, 16, 16);
                g.setStroke(new BasicStroke(1f));
                g.setColor(new Color(255, 255, 255, 56));
                g.drawRoundRect(x, yK, cw, KH, 16, 16);
                g.setPaint(new GradientPaint(0, yK + 14, WARNA[k][0], 0, yK + 44, WARNA[k][1]));
                g.fillRoundRect(x + 8, yK + 14, 3, 30, 3, 3);
                g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g.setColor(new Color(0x9F, 0xB8, 0xCC));
                g.drawString(KOMP[k] + "  " + tahun, x + 20, yK + 20);
                g.setColor(Color.WHITE);
                String v = "Rp " + uang(Math.round(sTampil[k]));
                int fsz = 19;
                g.setFont(new Font("Segoe UI", Font.BOLD, fsz));
                fm = g.getFontMetrics();
                while (fm.stringWidth(v) > cw - 34 && fsz > 11) {
                    g.setFont(new Font("Segoe UI", Font.BOLD, --fsz));
                    fm = g.getFontMetrics();
                }
                if (fm.stringWidth(v) > cw - 34) {
                    v = "Rp " + ringkas(Math.round(sTampil[k]));
                }
                g.drawString(v, x + 20, yK + 45);
                long li = Math.round(sTuj[k]), ll = Math.round(sPrev[k]);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g.setColor(TEKS_REDUP);
                String s1 = "th " + (tahun - 1) + " : " + ringkas(ll) + "  ";
                g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                int wDelta = g.getFontMetrics(new Font("Segoe UI", Font.BOLD, 11)).stringWidth(delta(li, ll));
                if (g.getFontMetrics().stringWidth(s1) + wDelta > cw - 28) {
                    s1 = "'" + String.valueOf(tahun - 1).substring(2) + " : " + ringkas(ll) + " ";
                }
                g.drawString(s1, x + 20, yK + 64);
                int w1 = g.getFontMetrics().stringWidth(s1);
                g.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g.setColor(warnaDelta(li, ll));
                g.drawString(delta(li, ll), x + 20 + w1, yK + 64);
                // porsi terhadap total (tumbuh saat animasi)
                int bl = cw - 40, by = yK + KH - 9;
                g.setColor(new Color(255, 255, 255, 24));
                g.fillRoundRect(x + 20, by, bl, 3, 3, 3);
                int len = sumT > 0 ? (int) Math.round(bl * sTampil[k] / sumT) : 0;
                if (len >= 2) {
                    g.setPaint(new GradientPaint(x + 20, 0, WARNA[k][0], x + 20 + bl, 0, WARNA[k][1]));
                    g.fillRoundRect(x + 20, by, len, 3, 3, 3);
                }
                g.translate(0, -(1 - cp) * 14);
                g.setComposite(cKartu);
            }

            // ---- tooltip ----
            if (hover >= 0 && (statis || progress >= 1)) {
                gambarTooltip(g, w, h);
            }

            // ---- lapisan memuat ----
            if (memuat) {
                g.setColor(alfa(BG1, 175));
                g.fillRect(0, 0, w, h);
                long now = System.currentTimeMillis();
                int pw2 = 200, ph2 = 44, px = (w - pw2) / 2, py = h / 3;
                g.setColor(alfa(Color.WHITE, 22));
                g.fillRoundRect(px, py, pw2, ph2, ph2, ph2);
                g.setStroke(new BasicStroke(1f));
                g.setColor(alfa(Color.WHITE, 60));
                g.drawRoundRect(px, py, pw2, ph2, ph2, ph2);
                int s0 = px + 16, s1 = py + 11;
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(alfa(Color.WHITE, 40));
                g.drawOval(s0, s1, 22, 22);
                g.setColor(BIRU);
                g.drawArc(s0, s1, 22, 22, -(int) ((now / 4) % 360), 100);
                g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g.setColor(TEKS);
                g.drawString("Memuat data" + "...".substring(0, (int) ((now / 350) % 4)), px + 50, py + 27);
            }
            g.dispose();
        }

        private void gambarTooltip(Graphics2D g, int w, int h) {
            int m = hover;
            double tc = jml(cur[m]), tp = jml(prev[m]);
            Font fN = new Font("Segoe UI", Font.PLAIN, 12), fB = new Font("Segoe UI", Font.BOLD, 12);
            g.setFont(fN);
            FontMetrics fm = g.getFontMetrics();
            int lblW = 0, colW = 0;
            for (int k : akt) {
                lblW = Math.max(lblW, fm.stringWidth(namaKomp(k)));
            }
            for (int k : akt) {
                colW = Math.max(colW, Math.max(fm.stringWidth(uang(Math.round(cur[m][k]))), fm.stringWidth(uang(Math.round(prev[m][k])))));
            }
            colW = Math.max(colW, Math.max(fm.stringWidth(uang(Math.round(tc))), fm.stringWidth(uang(Math.round(tp))))) + 14;
            int bw = 14 + 18 + lblW + 2 * colW + 14, bh = 24 + 20 + akt.length * 19 + 10 + 22 + 24 + 8;
            int tx = mx + 18, ty = my + 14;
            if (tx + bw > w - 8) {
                tx = mx - 18 - bw;
            }
            if (ty + bh > h - 8) {
                ty = h - 8 - bh;
            }
            g.setColor(new Color(8, 14, 32, 240));
            g.fillRoundRect(tx, ty, bw, bh, 14, 14);
            g.setColor(new Color(255, 255, 255, 70));
            g.drawRoundRect(tx, ty, bw, bh, 14, 14);
            int x1 = tx + 14 + 18 + lblW + colW, x2 = x1 + colW;
            g.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g.setColor(EMAS);
            g.drawString(NAMA_BULAN[m], tx + 14, ty + 22);
            int y = ty + 44;
            g.setFont(fB);
            g.setColor(Color.WHITE);
            String a1 = String.valueOf(tahun), a2 = String.valueOf(tahun - 1);
            g.drawString(a1, x1 - fm.stringWidth(a1), y);
            g.setColor(TEKS_REDUP);
            g.drawString(a2, x2 - fm.stringWidth(a2), y);
            for (int k : akt) {
                y += 19;
                g.setPaint(new GradientPaint(0, y - 10, WARNA[k][0], 0, y + 1, WARNA[k][1]));
                g.fillRoundRect(tx + 14, y - 10, 10, 10, 4, 4);
                g.setFont(fN);
                g.setColor(TEKS);
                String nm = namaKomp(k);
                g.drawString(nm, tx + 14 + 18, y);
                String sc = uang(Math.round(cur[m][k])), sp = uang(Math.round(prev[m][k]));
                g.setColor(Color.WHITE);
                g.drawString(sc, x1 - fm.stringWidth(sc), y);
                g.setColor(TEKS_REDUP);
                g.drawString(sp, x2 - fm.stringWidth(sp), y);
            }
            y += 9;
            g.setColor(new Color(255, 255, 255, 40));
            g.drawLine(tx + 14, y, tx + bw - 14, y);
            y += 17;
            g.setFont(fB);
            g.setColor(EMAS);
            g.drawString("Total", tx + 14, y);
            String sc = uang(Math.round(tc)), sp = uang(Math.round(tp));
            g.setColor(Color.WHITE);
            g.drawString(sc, x1 - fm.stringWidth(sc), y);
            g.setColor(TEKS_REDUP);
            g.drawString(sp, x2 - fm.stringWidth(sp), y);
            y += 21;
            long li = Math.round(tc), ll = Math.round(tp);
            g.setColor(warnaDelta(li, ll));
            String sel = li == ll ? "Sama dengan tahun lalu" : (delta(li, ll) + "  (" + (li >= ll ? "+" : "-") + "Rp " + uang(Math.abs(li - ll)) + ")");
            g.drawString(sel, tx + 14, y);
        }
    }

    // ------------------------------------------------------------------
    //  Peringkat bangsal: dua daftar (bulan terpilih & setahun), batang tumbuh, urutan bergeser halus saat bulan diganti
    // ------------------------------------------------------------------
    static final class PeringkatPanel extends JPanel {

        private static final int ATAS = 70, BAWAH = 18, MARGIN = 28, JARAK = 30, MAKS_BARIS = 12, RH_MIN = 34, RH_MAKS = 88;
        private static final Color PERAK = new Color(0xD5, 0xDF, 0xE8), PERUNGGU = new Color(0xE3, 0xA2, 0x6B);

        /** Satu daftar peringkat: nilai & posisi tiap bangsal meluncur halus menuju target. */
        private static final class Lajur {

            double[] v = new double[0], p = new double[0], vt = new double[0], pt = new double[0];
            double maks, maksNow;
            int kandidat;

            void atur(long[] nilai, boolean dariNol) {
                int c = nilai.length;
                if (v.length != c) {
                    v = new double[c];
                    p = new double[c];
                    vt = new double[c];
                    pt = new double[c];
                    dariNol = true;
                }
                Integer[] urut = new Integer[c];
                for (int i = 0; i < c; i++) {
                    urut[i] = i;
                }
                Arrays.sort(urut, (a, b) -> nilai[b] != nilai[a] ? Long.compare(nilai[b], nilai[a]) : Integer.compare(a, b));
                kandidat = 0;
                maks = 0;
                for (int r = 0; r < c; r++) {
                    int col = urut[r];
                    pt[col] = r;
                    vt[col] = Math.max(0, nilai[col]);
                    if (nilai[col] > 0) {
                        kandidat++;
                    }
                    maks = Math.max(maks, vt[col]);
                }
                if (dariNol) {
                    for (int i = 0; i < c; i++) {
                        v[i] = 0;
                        p[i] = pt[i];
                    }
                    maksNow = maks;
                }
            }

            void snap() {
                for (int i = 0; i < v.length; i++) {
                    v[i] = vt[i];
                    p[i] = pt[i];
                }
                maksNow = maks;
            }

            boolean tick() {
                boolean lagi = false;
                for (int i = 0; i < v.length; i++) {
                    double dv = vt[i] - v[i];
                    if (Math.abs(dv) > Math.max(1, vt[i] * 0.002)) {
                        v[i] += dv * 0.16;
                        lagi = true;
                    } else {
                        v[i] = vt[i];
                    }
                    double dp = pt[i] - p[i];
                    if (Math.abs(dp) > 0.004) {
                        p[i] += dp * 0.2;
                        lagi = true;
                    } else {
                        p[i] = pt[i];
                    }
                }
                double dm = maks - maksNow;
                if (Math.abs(dm) > Math.max(1, maks * 0.002)) {
                    maksNow += dm * 0.16;
                    lagi = true;
                } else {
                    maksNow = maks;
                }
                return lagi;
            }
        }

        private Data d = Data.kosong(), lalu = Data.kosong();
        private int tahun, bulan;
        private long[] nilBulan = new long[0], nilTahun = new long[0], lalBulan = new long[0], lalTahun = new long[0];
        private final Lajur lajurBulan = new Lajur(), lajurTahun = new Lajur();
        private boolean statis, memuat;
        private long mulai = 0;
        private int hoverLajur = -1, hoverKolom = -1;
        private Timer timer, timerMuat;
        private final IntConsumer klik;
        // tata letak terakhir (dipakai hover)
        private int lRH = RH_MIN, lLebar = 100, lJml = 0;

        PeringkatPanel(IntConsumer klik) {
            this.klik = klik;
            setOpaque(true);
            ToolTipManager.sharedInstance().registerComponent(this);
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int l = -1, c = -1;
                    for (int j = 0; j < 2 && l < 0; j++) {
                        int x0 = MARGIN + j * (lLebar + JARAK);
                        if (e.getX() >= x0 && e.getX() < x0 + lLebar && e.getY() >= ATAS) {
                            Lajur lj = j == 0 ? lajurBulan : lajurTahun;
                            for (int i = 0; i < lj.p.length; i++) {
                                int y = ATAS + (int) Math.round(lj.p[i] * lRH);
                                if (lj.p[i] < lJml - 0.5 && lj.vt[i] > 0 && e.getY() >= y && e.getY() < y + lRH) {
                                    l = j;
                                    c = i;
                                    break;
                                }
                            }
                        }
                    }
                    if (l != hoverLajur || c != hoverKolom) {
                        hoverLajur = l;
                        hoverKolom = c;
                        setCursor(Cursor.getPredefinedCursor(l == 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                        setToolTipText(c < 0 ? null : tooltip(l, c));
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoverLajur = -1;
                    hoverKolom = -1;
                    repaint();
                }
            });
        }

        private String tooltip(int l, int c) {
            long ini = (l == 0 ? nilBulan : nilTahun)[c], lw = (l == 0 ? lalBulan : lalTahun)[c];
            return "<html><b>" + esc(d.namaKolom(c)) + "</b><br>" + (l == 0 ? NAMA_BULAN[bulan] + " " + tahun : "Tahun " + tahun)
                    + " : <b>Rp " + uang(ini) + "</b><br>" + (l == 0 ? NAMA_BULAN[bulan] + " " : "Periode sama ") + (tahun - 1)
                    + " : <b>Rp " + uang(lw) + "</b> &nbsp;" + delta(ini, lw) + "</html>";
        }

        // ---------- data & animasi ----------
        void setData(Data d, Data lalu, int tahun, int bulan, boolean dariNol) {
            this.d = d;
            this.lalu = lalu == null ? Data.kosong() : lalu;
            this.tahun = tahun;
            this.bulan = bulan - 1;
            hitung(true, dariNol);
            if (dariNol) {
                mulai = System.currentTimeMillis();
            }
            statis = false;
            jalan();
        }

        void setBulan(int b) {
            bulan = b - 1;
            hitung(false, false);
            jalan();
        }

        private void hitung(boolean tahunJuga, boolean dariNol) {
            int n = d.jmlKolom();
            nilBulan = new long[n];
            lalBulan = new long[n];
            for (int c = 0; c < n; c++) {
                nilBulan[c] = d.bulanDari(d.nama[c], bulan);
                lalBulan[c] = lalu.bulanDari(d.nama[c], bulan);
            }
            lajurBulan.atur(nilBulan, dariNol);
            if (tahunJuga) {
                nilTahun = new long[n];
                lalTahun = new long[n];
                for (int c = 0; c < n; c++) {
                    nilTahun[c] = d.totalKolom[c];
                    lalTahun[c] = lalu.tahunDari(d.nama[c]);
                }
                lajurTahun.atur(nilTahun, dariNol);
            }
        }

        /** Ulangi animasi masuk dari batang kosong. */
        void ulang() {
            hitung(true, true);
            statis = false;
            mulai = System.currentTimeMillis();
            jalan();
        }

        /** Tanpa animasi (untuk menyimpan gambar). */
        void setStatis() {
            statis = true;
            lajurBulan.snap();
            lajurTahun.snap();
            hoverLajur = -1;
            hentikan();
        }

        void setMemuat(boolean v) {
            memuat = v;
            if (v) {
                if (timerMuat == null) {
                    timerMuat = new Timer(16, e -> repaint());
                }
                timerMuat.start();
            } else if (timerMuat != null) {
                timerMuat.stop();
            }
            repaint();
        }

        void hentikan() {
            if (timer != null) {
                timer.stop();
            }
            if (timerMuat != null) {
                timerMuat.stop();
            }
        }

        private void jalan() {
            if (timer == null) {
                timer = new Timer(16, e -> {
                    boolean a = lajurBulan.tick();
                    boolean b = lajurTahun.tick();
                    boolean masuk = !statis && System.currentTimeMillis() - mulai < 650L + MAKS_BARIS * 70L;
                    repaint();
                    if (!a && !b && !masuk) {
                        ((Timer) e.getSource()).stop();
                    }
                });
            }
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        // ---------- lukisan ----------
        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, BG1, 0, h, BG2));
            g.fillRect(0, 0, w, h);
            if (statis) {
                lajurBulan.snap();
                lajurTahun.snap();
            }
            lLebar = Math.max(200, (w - 2 * MARGIN - JARAK) / 2);
            int tersedia = h - ATAS - BAWAH;
            int maksMuat = Math.max(1, Math.min(MAKS_BARIS, tersedia / RH_MIN));
            lJml = Math.max(1, Math.min(maksMuat, Math.max(lajurBulan.kandidat, lajurTahun.kandidat)));
            lRH = Math.max(RH_MIN, Math.min(RH_MAKS, tersedia / lJml));

            if (!d.ada && !memuat) {
                g.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                g.setColor(TEKS_REDUP);
                tengah(g, "Tidak ada data pendapatan rawat inap pada tahun " + tahun + ".", 0, w, h / 2);
                g.dispose();
                return;
            }
            long tb = d.ada ? d.totalBulan[bulan] : 0;
            lukisLajur(g, lajurBulan, nilBulan, lalBulan, tb, 0, MARGIN, lLebar,
                    "BULAN " + NAMA_BULAN[bulan].toUpperCase(Locale.ROOT) + " " + tahun, HIJAU);
            lukisLajur(g, lajurTahun, nilTahun, lalTahun, d.total, 1, MARGIN + lLebar + JARAK, lLebar,
                    "TOTAL TAHUN " + tahun, BIRU);

            if (memuat) {
                g.setColor(alfa(BG1, 175));
                g.fillRect(0, 0, w, h);
                long now = System.currentTimeMillis();
                int pw2 = 200, ph2 = 44, px = (w - pw2) / 2, py = h / 3;
                g.setColor(alfa(Color.WHITE, 22));
                g.fillRoundRect(px, py, pw2, ph2, ph2, ph2);
                g.setStroke(new BasicStroke(1f));
                g.setColor(alfa(Color.WHITE, 60));
                g.drawRoundRect(px, py, pw2, ph2, ph2, ph2);
                int s0 = px + 16, s1 = py + 11;
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(alfa(Color.WHITE, 40));
                g.drawOval(s0, s1, 22, 22);
                g.setColor(BIRU);
                g.drawArc(s0, s1, 22, 22, -(int) ((now / 4) % 360), 100);
                g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g.setColor(TEKS);
                g.drawString("Memuat data" + "...".substring(0, (int) ((now / 350) % 4)), px + 50, py + 27);
            }
            g.dispose();
        }

        private void lukisLajur(Graphics2D g, Lajur lj, long[] nil, long[] lal, long totalList, int idx, int x0, int lw,
                String judul, Color aksen) {
            long now = System.currentTimeMillis();
            // judul + garis aksen yang tumbuh
            double kj = statis ? 1.0 : easeOut((now - mulai - idx * 90L) / 550.0);
            java.awt.Composite lama = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) klem(kj)));
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g.setColor(aksen);
            g.drawString(judul, x0, 34);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.setColor(TEKS_REDUP);
            int aktif = 0;
            for (long v : nil) {
                if (v > 0) {
                    aktif++;
                }
            }
            String sub = "Rp " + uang(totalList) + "   \u2022   " + aktif + " bangsal";
            g.drawString(sub, x0, 53);
            g.setPaint(new GradientPaint(x0, 0, aksen, x0 + lw, 0, alfa(aksen, 20)));
            g.fillRoundRect(x0, 58, (int) Math.round(lw * klem(kj)), 3, 3, 3);
            g.setComposite(lama);

            int rh = lRH;
            for (int c = 0; c < lj.p.length; c++) {
                double pos = lj.p[c];
                if (lj.vt[c] <= 0 && lj.v[c] < 1 || pos > lJml - 0.5) {
                    continue;
                }
                double fade = klem((lJml - 0.25 - pos) / 0.75);
                int rank = (int) Math.round(lj.pt[c]);
                double em = statis ? 1.0 : easeOut((now - mulai - Math.min(rank, MAKS_BARIS) * 70L) / 600.0);
                double a = fade * klem(em);
                if (a <= 0.01) {
                    continue;
                }
                int y = ATAS + (int) Math.round(pos * rh) + (rh >= 60 ? (rh - 56) / 2 : 0);
                int dx = (int) Math.round((1 - em) * 26);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) a));
                g.translate(dx, 0);

                boolean sorot = hoverLajur == idx && hoverKolom == c;
                int rk = (int) Math.round(pos);
                if (sorot) {
                    g.setColor(alfa(Color.WHITE, 24));
                    g.fillRoundRect(x0 - 8 - dx, y + 1, lw + 16, rh - 2, 12, 12);
                } else if (rk % 2 == 0) {
                    g.setColor(alfa(Color.WHITE, 8));
                    g.fillRoundRect(x0 - 8 - dx, y + 1, lw + 16, rh - 2, 12, 12);
                }

                // lencana peringkat
                int bd = 26, by = y + (rh - bd) / 2;
                Color mc = rk == 0 ? EMAS : (rk == 1 ? PERAK : (rk == 2 ? PERUNGGU : null));
                if (mc != null) {
                    g.setPaint(new GradientPaint(x0, by, campur(mc, Color.WHITE, 0.35), x0, by + bd, mc));
                    g.fillOval(x0, by, bd, bd);
                    g.setColor(new Color(0x1B, 0x22, 0x38));
                } else {
                    g.setColor(alfa(Color.WHITE, 24));
                    g.fillOval(x0, by, bd, bd);
                    g.setColor(TEKS);
                }
                g.setFont(new Font("Segoe UI", Font.BOLD, 12));
                tengah(g, String.valueOf(rk + 1), x0, bd, by + 17);

                int xt = x0 + bd + 12, lt = lw - bd - 12;
                // nilai (menghitung naik) + lencana selisih
                long tampilNilai = Math.round(lj.v[c] * (statis ? 1 : klem(em)));
                String sv = "Rp " + uang(tampilNilai);
                g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g.getFontMetrics();
                int chipW = 74;
                boolean adaChip = nil[c] > 0;
                int xv = x0 + lw - (adaChip ? chipW + 8 : 0) - fm.stringWidth(sv);
                g.setColor(Color.WHITE);
                g.drawString(sv, xv, y + 18);
                if (adaChip) {
                    chip(g, delta(nil[c], lal[c]), warnaDelta(nil[c], lal[c]), x0 + lw - chipW, y + 4, chipW);
                }
                // nama
                g.setColor(rk == 0 ? EMAS : TEKS);
                String nm = potong(d.namaKolom(c), fm, Math.max(40, xv - xt - 12));
                g.drawString(nm, xt, y + 18);

                // batang
                int bh = rh >= 60 ? 16 : 8, byy = y + (rh >= 60 ? 31 : 26), tw = Math.max(20, lt - 52);
                g.setColor(alfa(Color.WHITE, 22));
                g.fillRoundRect(xt, byy, tw, bh, bh, bh);
                double frac = lj.maksNow > 0 ? Math.min(1.0, lj.v[c] / lj.maksNow) : 0;
                int fw = (int) Math.round(tw * frac);
                if (fw >= 3) {
                    Color wb = warnaBangsal(d.namaKolom(c)), c1 = campur(wb, Color.WHITE, 0.4), c2 = wb;
                    g.setPaint(new GradientPaint(xt, 0, c1, xt + Math.max(fw, 2), 0, c2));
                    g.fillRoundRect(xt, byy, fw, bh, bh, bh);
                    g.setPaint(new RadialGradientPaint(xt + fw, byy + bh / 2f, 16f, new float[]{0f, 1f},
                            new Color[]{alfa(c2, 120), alfa(c2, 0)}));
                    g.fillOval(xt + fw - 16, byy + bh / 2 - 16, 32, 32);
                }
                // porsi terhadap total daftar
                double porsi = totalList > 0 ? 100.0 * nil[c] / totalList : 0;
                g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g.setColor(TEKS_REDUP);
                String sp = String.format(Locale.US, "%.1f%%", porsi);
                g.drawString(sp, x0 + lw - g.getFontMetrics().stringWidth(sp), byy + bh);

                g.translate(-dx, 0);
                g.setComposite(lama);
            }
        }

        private void chip(Graphics2D g, String t, Color c, int x, int y, int lebar) {
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            int bh = 20;
            g.setColor(alfa(c, 34));
            g.fillRoundRect(x, y, lebar, bh, bh, bh);
            g.setColor(alfa(c, 120));
            g.setStroke(new BasicStroke(1f));
            g.drawRoundRect(x, y, lebar, bh, bh, bh);
            g.setColor(c);
            tengah(g, t, x, lebar, y + 14);
        }
    }

    // ------------------------------------------------------------------
    //  Line chart. Mode 1 "Total vs Tahun Lalu": garis mulus + area gradasi tahun ini dibanding garis putus-putus tahun lalu.
    //  Mode 2 "Per Bangsal": satu garis tiap bangsal. Mode 3 "Per Komponen": satu garis tiap komponen biaya.
    //  Legenda bisa diklik untuk menyembunyikan garis.
    // ------------------------------------------------------------------
    static final class GarisPanel extends JPanel {

        private static final long DURASI_MS = 1800L;
        private static final int KIRI = 84, KANAN = 34, BAWAH_PAD = 66, MAKS_SERI = 8;

        private Data d = Data.kosong(), lalu = Data.kosong();
        private int tahun, bulan, mode = 0;
        private int[] idSeri = new int[0];
        private boolean statis, memuat, awalData = true;
        private double progress = 1, selPos, axMax = 1, axTarget = 1;
        private long mulai;
        private int hoverBln = -1, hoverLeg = -1, mx, my;
        private int pAtas, pBawah, pKiri, pLebar;
        private double[] tot = new double[12], totL = new double[12];
        private String[] namaSeri = new String[0];
        private double[][] nilaiSeri = new double[0][12];
        private boolean[] tampil = new boolean[0];
        private Rectangle[] rLeg = new Rectangle[0];
        private Timer timer, timerMuat;
        private final IntConsumer klik;
        private final Segmen seg;

        GarisPanel(IntConsumer klik) {
            this.klik = klik;
            setOpaque(true);
            setLayout(null);
            seg = new Segmen(new String[]{"Total vs Tahun Lalu", "Per Bangsal", "Per Komponen"}, 0, this::gantiMode);
            add(seg);
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mx = e.getX();
                    my = e.getY();
                    hitung(mx, my);
                    jalan();
                    repaint();
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoverBln = -1;
                    hoverLeg = -1;
                    jalan();
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }
                    if (mode >= 1 && hoverLeg >= 0 && hoverLeg < tampil.length) {
                        int aktif = 0;
                        for (boolean b : tampil) {
                            aktif += b ? 1 : 0;
                        }
                        if (!tampil[hoverLeg] || aktif > 1) {
                            tampil[hoverLeg] = !tampil[hoverLeg];
                            hitungAxis();
                            jalan();
                            repaint();
                        }
                        return;
                    }
                    if (hoverBln >= 0 && GarisPanel.this.klik != null) {
                        GarisPanel.this.klik.accept(hoverBln + 1);
                    }
                }
            });
        }

        @Override
        public void doLayout() {
            Dimension p = seg.getPreferredSize();
            seg.setBounds(getWidth() - KANAN - p.width, 16, p.width, p.height);
        }

        // ---------- data ----------
        void setData(Data d, Data lalu, int tahun, int bulan, boolean dariNol) {
            this.d = d;
            this.lalu = lalu == null ? Data.kosong() : lalu;
            this.tahun = tahun;
            this.bulan = bulan - 1;
            if (awalData) {
                selPos = this.bulan;
                awalData = false;
            }
            hoverBln = -1;
            hoverLeg = -1;
            bangun();
            axMax = axTarget;
            mulaiAnimasi();
        }

        void setBulan(int b) {
            bulan = b - 1;
            jalan();
        }

        int modeSekarang() {
            return mode;
        }

        private void gantiMode(int i) {
            mode = i;
            hoverBln = -1;
            hoverLeg = -1;
            bangun();
            axMax = axTarget;
            mulaiAnimasi();
        }

        /** Untuk ekspor gambar: ubah mode tanpa memicu segmen. */
        void gantiModeLangsung(int i) {
            mode = i;
            seg.setLangsung(i);
            bangun();
            axMax = axTarget;
        }

        private void bangun() {
            int n = d.jmlKolom();
            for (int m = 0; m < 12; m++) {
                boolean a = false, b = false;
                for (int c = 0; c < n; c++) {
                    a |= d.nilai[m][c] != null;
                }
                for (int c = 0; c < lalu.jmlKolom(); c++) {
                    b |= lalu.nilai[m][c] != null;
                }
                tot[m] = a ? d.totalBulan[m] : Double.NaN;
                totL[m] = b ? lalu.totalBulan[m] : Double.NaN;
            }
            java.util.ArrayList<String> nm = new java.util.ArrayList<>();
            java.util.ArrayList<double[]> vv = new java.util.ArrayList<>();
            java.util.ArrayList<Integer> id = new java.util.ArrayList<>();
            if (mode == 1) {
                for (int c = 0; c < n; c++) {
                    if (d.totalKolom[c] <= 0) {
                        continue;
                    }
                    double[] v = new double[12];
                    for (int m = 0; m < 12; m++) {
                        v[m] = d.nilai[m][c] == null ? Double.NaN : d.nilai[m][c];
                    }
                    nm.add(d.namaKolom(c));
                    vv.add(v);
                    id.add(c);
                }
            } else if (mode == 2) {
                for (int k = 0; k < NK; k++) {
                    long s = 0;
                    for (int m = 0; m < 12; m++) {
                        s += d.komp[m][k];
                    }
                    if (s == 0) {
                        continue;
                    }
                    double[] v = new double[12];
                    for (int m = 0; m < 12; m++) {
                        v[m] = Double.isNaN(tot[m]) ? Double.NaN : d.komp[m][k];
                    }
                    nm.add(KOMP_NAMA[k]);
                    vv.add(v);
                    id.add(k);
                }
            }
            namaSeri = nm.toArray(new String[0]);
            nilaiSeri = vv.toArray(new double[0][]);
            idSeri = new int[id.size()];
            for (int i = 0; i < idSeri.length; i++) {
                idSeri[i] = id.get(i);
            }
            tampil = new boolean[namaSeri.length];
            Arrays.fill(tampil, true);
            hitungAxis();
        }

        private Color warnaSeri(int s) {
            if (mode == 1) {
                return warnaBangsal(namaSeri[s]);
            }
            return GrafikPanel.WARNA[idSeri[s]][0];
        }

        private void hitungAxis() {
            double mk = 0;
            if (mode == 0) {
                for (int m = 0; m < 12; m++) {
                    if (!Double.isNaN(tot[m])) {
                        mk = Math.max(mk, tot[m]);
                    }
                    if (!Double.isNaN(totL[m])) {
                        mk = Math.max(mk, totL[m]);
                    }
                }
            } else {
                for (int s = 0; s < nilaiSeri.length; s++) {
                    if (!tampil[s]) {
                        continue;
                    }
                    for (int m = 0; m < 12; m++) {
                        if (!Double.isNaN(nilaiSeri[s][m])) {
                            mk = Math.max(mk, nilaiSeri[s][m]);
                        }
                    }
                }
            }
            axTarget = GrafikPanel.niceMax(mk);
        }

        private boolean punyaData() {
            for (double v : tot) {
                if (!Double.isNaN(v) && v != 0) {
                    return true;
                }
            }
            return false;
        }

        // ---------- animasi ----------
        void mulaiAnimasi() {
            statis = false;
            progress = 0;
            mulai = System.currentTimeMillis();
            jalan();
        }

        void ulang() {
            mulaiAnimasi();
        }

        void setStatis() {
            statis = true;
            progress = 1;
            hoverBln = -1;
            hoverLeg = -1;
            selPos = bulan;
            axMax = axTarget;
            hentikan();
        }

        void setMemuat(boolean v) {
            memuat = v;
            if (v) {
                if (timerMuat == null) {
                    timerMuat = new Timer(16, e -> repaint());
                }
                timerMuat.start();
            } else if (timerMuat != null) {
                timerMuat.stop();
            }
            repaint();
        }

        void hentikan() {
            if (timer != null) {
                timer.stop();
            }
            if (timerMuat != null) {
                timerMuat.stop();
            }
            seg.hentikan();
        }

        private void jalan() {
            if (timer == null) {
                timer = new Timer(16, e -> tick());
            }
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        private void tick() {
            boolean lagi = false;
            if (!statis && progress < 1) {
                progress = Math.min(1, (System.currentTimeMillis() - mulai) / (double) DURASI_MS);
                lagi = progress < 1;
            }
            double ds = bulan - selPos;
            if (Math.abs(ds) > 0.005) {
                selPos += ds * 0.25;
                lagi = true;
            } else {
                selPos = bulan;
            }
            double da = axTarget - axMax;
            if (Math.abs(da) > axTarget * 0.002) {
                axMax += da * 0.18;
                lagi = true;
            } else {
                axMax = axTarget;
            }
            repaint();
            if (!lagi) {
                timer.stop();
            }
        }

        // ---------- interaksi ----------
        private void hitung(int x, int y) {
            hoverLeg = -1;
            for (int i = 0; i < rLeg.length; i++) {
                if (mode >= 1 && rLeg[i] != null && rLeg[i].contains(x, y)) {
                    hoverLeg = i;
                }
            }
            hoverBln = -1;
            if (hoverLeg < 0 && pLebar > 0 && x >= pKiri && x < pKiri + pLebar && y >= pAtas - 14 && y <= pBawah + 30) {
                hoverBln = Math.min(11, (int) ((x - pKiri) / (pLebar / 12.0)));
            }
            setCursor(Cursor.getPredefinedCursor(hoverLeg >= 0 || hoverBln >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        }

        // ---------- koordinat ----------
        private double xm(int m) {
            return pKiri + (m + 0.5) * (pLebar / 12.0);
        }

        private double ym(double v) {
            return pBawah - (pBawah - pAtas) * Math.min(1.04, Math.max(0, v) / axMax);
        }

        /** Jalur mulus (Catmull-Rom -> Bezier, tanpa melampaui nilai di antara dua titik) untuk run indeks a..b. */
        private java.awt.geom.Path2D.Double jalur(double[] v, int a, int b) {
            java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
            p.moveTo(xm(a), ym(v[a]));
            for (int i = a; i < b; i++) {
                double x0 = xm(i), x1 = xm(i + 1), y0 = ym(v[i]), y1 = ym(v[i + 1]);
                double xa = i > a ? xm(i - 1) : x0, ya = i > a ? ym(v[i - 1]) : y0;
                double xb = i + 1 < b ? xm(i + 2) : x1, yb = i + 1 < b ? ym(v[i + 2]) : y1;
                double lo = Math.min(y0, y1), hi = Math.max(y0, y1);
                double c1y = Math.max(lo, Math.min(hi, y0 + (y1 - ya) / 6.0));
                double c2y = Math.max(lo, Math.min(hi, y1 - (yb - y0) / 6.0));
                p.curveTo(x0 + (x1 - xa) / 6.0, c1y, x1 - (xb - x0) / 6.0, c2y, x1, y1);
            }
            return p;
        }

        /** Rentang berurutan tanpa celah: daftar {awal, akhir}. */
        private java.util.List<int[]> runs(double[] v) {
            java.util.ArrayList<int[]> r = new java.util.ArrayList<>();
            int a = -1;
            for (int m = 0; m <= 12; m++) {
                boolean ada = m < 12 && !Double.isNaN(v[m]);
                if (ada && a < 0) {
                    a = m;
                } else if (!ada && a >= 0) {
                    r.add(new int[]{a, m - 1});
                    a = -1;
                }
            }
            return r;
        }

        private double rev(int idx) {
            return statis ? 1.0 : easeOut(progress * 1.25 - idx * 0.07);
        }

        private void potongReveal(Graphics2D g, double r) {
            g.clip(new java.awt.geom.Rectangle2D.Double(pKiri - 14, 0, (pLebar + 28) * lim(r), getHeight()));
        }

        // ---------- lukisan ----------
        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, BG1, 0, h, BG2));
            g.fillRect(0, 0, w, h);
            pKiri = KIRI;
            pLebar = w - KIRI - KANAN;
            pBawah = h - BAWAH_PAD;
            rLeg = new Rectangle[namaSeri.length];
            // legenda mode 2: susun baris dulu agar tinggi area plot diketahui
            int baris = 0;
            if (mode >= 1) {
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                FontMetrics fm = g.getFontMetrics();
                int lx = KIRI, ly = 70;
                baris = 1;
                for (int s = 0; s < namaSeri.length; s++) {
                    int cw = 30 + fm.stringWidth(potong(namaSeri[s], fm, 150)) + 8;
                    if (lx + cw > w - KANAN && lx > KIRI) {
                        lx = KIRI;
                        ly += 28;
                        baris++;
                    }
                    rLeg[s] = new Rectangle(lx, ly, cw, 24);
                    lx += cw + 8;
                }
            }
            pAtas = mode == 0 ? 96 : 84 + baris * 28 + 8;
            if (pLebar < 260 || pBawah - pAtas < 120) {
                g.dispose();
                return;
            }
            if (!punyaData() && !memuat) {
                g.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                g.setColor(TEKS_REDUP);
                tengah(g, "Tidak ada data pendapatan rawat inap pada tahun " + tahun + ".", 0, w, h / 2);
                g.dispose();
                return;
            }
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.setColor(EMAS);
            g.drawString(d.label.toUpperCase(Locale.ROOT), KIRI, 30);

            kisi(g);
            if (mode == 0) {
                lukisTotal(g);
            } else {
                lukisSeri(g);
            }
            if (hoverBln >= 0 && (statis || progress > 0.85)) {
                bidik(g);
                tooltip(g, w, h);
            }
            if (memuat) {
                g.setColor(alfa(BG1, 175));
                g.fillRect(0, 0, w, h);
                long now = System.currentTimeMillis();
                int pw2 = 200, ph2 = 44, px = (w - pw2) / 2, py = h / 3;
                g.setColor(alfa(Color.WHITE, 22));
                g.fillRoundRect(px, py, pw2, ph2, ph2, ph2);
                g.setStroke(new BasicStroke(1f));
                g.setColor(alfa(Color.WHITE, 60));
                g.drawRoundRect(px, py, pw2, ph2, ph2, ph2);
                int s0 = px + 16, s1 = py + 11;
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(alfa(Color.WHITE, 40));
                g.drawOval(s0, s1, 22, 22);
                g.setColor(BIRU);
                g.drawArc(s0, s1, 22, 22, -(int) ((now / 4) % 360), 100);
                g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g.setColor(TEKS);
                g.drawString("Memuat data" + "...".substring(0, (int) ((now / 350) % 4)), px + 50, py + 27);
            }
            g.dispose();
        }

        private void kisi(Graphics2D g) {
            int ph = pBawah - pAtas;
            double band = pLebar / 12.0;
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i <= 5; i++) {
                int y = pBawah - (int) Math.round(ph * i / 5.0);
                g.setColor(new Color(255, 255, 255, i == 0 ? 60 : 17));
                g.drawLine(pKiri, y, pKiri + pLebar, y);
                g.setColor(TEKS_REDUP);
                String s = ringkas(Math.round(axMax * i / 5.0));
                g.drawString(s, pKiri - 8 - fm.stringWidth(s), y + 4);
            }
            // bulan terpilih (meluncur)
            double sxb = pKiri + selPos * band;
            g.setPaint(new GradientPaint(0, pAtas - 8, alfa(AKSEN1, 62), 0, pBawah + 28, alfa(AKSEN1, 8)));
            g.fillRoundRect((int) sxb + 3, pAtas - 8, (int) band - 6, ph + 38, 14, 14);
            g.setColor(alfa(AKSEN2, 230));
            g.fillRoundRect((int) (sxb + band / 2 - 18), pAtas - 8, 36, 3, 3, 3);
            for (int m = 0; m < 12; m++) {
                boolean sel = m == bulan;
                g.setFont(new Font("Segoe UI", sel || m == hoverBln ? Font.BOLD : Font.PLAIN, 11));
                fm = g.getFontMetrics();
                String bl = NAMA_BULAN[m].substring(0, 3);
                g.setColor(sel ? EMAS : (m == hoverBln ? Color.WHITE : new Color(170, 185, 215)));
                g.drawString(bl, (int) (pKiri + (m + 0.5) * band - fm.stringWidth(bl) / 2.0), pBawah + 18);
            }
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.setColor(TEKS_REDUP);
            tengah(g, "Bulan " + tahun, pKiri, pLebar, pBawah + 46);
            java.awt.geom.AffineTransform at = g.getTransform();
            g.rotate(-Math.PI / 2, 22, (pAtas + pBawah) / 2.0);
            tengah(g, "Pendapatan", 22 - (pBawah - pAtas) / 2, pBawah - pAtas, (pAtas + pBawah) / 2 + 4);
            g.setTransform(at);
        }

        // ----- mode 1: total tahun ini vs tahun lalu -----
        private void lukisTotal(Graphics2D g) {
            // legenda
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            FontMetrics fm = g.getFontMetrics();
            int lx = KIRI, ly = 62;
            g.setPaint(new GradientPaint(lx, 0, AKSEN1, lx + 26, 0, AKSEN2));
            g.fillRoundRect(lx, ly - 8, 26, 4, 4, 4);
            g.setColor(TEKS);
            g.drawString("Tahun " + tahun, lx + 34, ly);
            lx += 34 + fm.stringWidth("Tahun " + tahun) + 26;
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{5f, 4f}, 0f));
            g.setColor(alfa(EMAS, 210));
            g.drawLine(lx, ly - 6, lx + 26, ly - 6);
            g.setStroke(new BasicStroke(1f));
            g.setColor(TEKS);
            g.drawString("Tahun " + (tahun - 1) + " (periode sama)", lx + 34, ly);

            // rata-rata tahun ini
            double jumlah = 0;
            int cnt = 0, puncak = -1;
            for (int m = 0; m < 12; m++) {
                if (!Double.isNaN(tot[m])) {
                    jumlah += tot[m];
                    cnt++;
                    if (puncak < 0 || tot[m] > tot[puncak]) {
                        puncak = m;
                    }
                }
            }
            double rata = cnt > 0 ? jumlah / cnt : 0;
            double rv0 = rev(0);
            if (cnt > 0 && rata > 0) {
                int y = (int) ym(rata);
                g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{2f, 5f}, 0f));
                g.setColor(alfa(Color.WHITE, (int) (80 * lim(rv0))));
                g.drawLine(pKiri, y, pKiri + pLebar, y);
                g.setStroke(new BasicStroke(1f));
                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g.setColor(alfa(TEKS_REDUP, (int) (255 * lim(rv0))));
                String t = "rata-rata Rp " + ringkas(Math.round(rata));
                g.drawString(t, pKiri + pLebar - g.getFontMetrics().stringWidth(t) - 4, y - 5);
            }

            // area gradasi tahun ini (paling belakang agar garis tahun lalu tetap jelas)
            Shape klipAwal = g.getClip();
            potongReveal(g, rv0);
            for (int[] r : runs(tot)) {
                if (r[1] > r[0]) {
                    java.awt.geom.Path2D.Double area = new java.awt.geom.Path2D.Double(jalur(tot, r[0], r[1]));
                    area.lineTo(xm(r[1]), pBawah);
                    area.lineTo(xm(r[0]), pBawah);
                    area.closePath();
                    double mk = 0;
                    for (int m = r[0]; m <= r[1]; m++) {
                        mk = Math.max(mk, tot[m]);
                    }
                    g.setPaint(new GradientPaint(0, (float) ym(mk), alfa(AKSEN1, 120), 0, pBawah, alfa(AKSEN2, 0)));
                    g.fill(area);
                }
            }
            g.setClip(klipAwal);

            // tahun lalu (putus-putus)
            potongReveal(g, rev(1));
            for (int[] r : runs(totL)) {
                if (r[1] > r[0]) {
                    g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{7f, 6f}, 0f));
                    g.setColor(alfa(EMAS, 200));
                    g.draw(jalur(totL, r[0], r[1]));
                }
            }
            g.setStroke(new BasicStroke(1.6f));
            for (int m = 0; m < 12; m++) {
                if (!Double.isNaN(totL[m])) {
                    double x = xm(m), y = ym(totL[m]);
                    g.setColor(BG1);
                    g.fill(new java.awt.geom.Ellipse2D.Double(x - 3.5, y - 3.5, 7, 7));
                    g.setColor(alfa(EMAS, 220));
                    g.draw(new java.awt.geom.Ellipse2D.Double(x - 3.5, y - 3.5, 7, 7));
                }
            }
            g.setClip(klipAwal);

            // tahun ini: area + garis bercahaya
            potongReveal(g, rv0);
            java.awt.Paint garisCat = new LinearGradientPaint(pKiri, 0, pKiri + pLebar, 0, new float[]{0f, 0.55f, 1f},
                    new Color[]{AKSEN1, AKSEN2, new Color(0x7B, 0xF0, 0xC8)});
            for (int[] r : runs(tot)) {
                if (r[1] > r[0]) {
                    java.awt.geom.Path2D.Double p = jalur(tot, r[0], r[1]);
                    g.setPaint(garisCat);
                    g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.setColor(alfa(AKSEN1, 26));
                    g.draw(p);
                    g.setStroke(new BasicStroke(5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.setColor(alfa(AKSEN2, 46));
                    g.draw(p);
                    g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.setPaint(garisCat);
                    g.draw(p);
                }
            }
            g.setClip(klipAwal);

            // penanda titik (muncul saat garis melewatinya)
            for (int m = 0; m < 12; m++) {
                if (Double.isNaN(tot[m])) {
                    continue;
                }
                double x = xm(m), y = ym(tot[m]);
                double pop = statis ? 1.0 : balik((rv0 * (pLebar + 28) - (x - pKiri + 14)) / 46.0);
                if (pop <= 0.02) {
                    continue;
                }
                boolean sel = m == bulan, hov = m == hoverBln;
                double r = (sel ? 6.5 : (hov ? 6 : 4.4)) * pop;
                if (sel || hov) {
                    g.setPaint(new RadialGradientPaint((float) x, (float) y, 22f, new float[]{0f, 1f},
                            new Color[]{alfa(AKSEN2, 120), alfa(AKSEN2, 0)}));
                    g.fill(new java.awt.geom.Ellipse2D.Double(x - 22, y - 22, 44, 44));
                }
                g.setColor(sel ? Color.WHITE : BG1);
                g.fill(new java.awt.geom.Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
                g.setStroke(new BasicStroke(2.2f));
                g.setColor(sel ? AKSEN2 : campur(AKSEN1, AKSEN2, m / 11.0));
                g.draw(new java.awt.geom.Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
                g.setStroke(new BasicStroke(1f));
            }

            // lencana puncak & nilai bulan terpilih
            if ((statis || progress > 0.9) && puncak >= 0) {
                lencana(g, "\u25B2 Tertinggi  Rp " + ringkas(Math.round(tot[puncak])), xm(puncak), ym(tot[puncak]) - 16, EMAS);
            }
            if ((statis || progress > 0.9) && bulan >= 0 && bulan < 12 && !Double.isNaN(tot[bulan]) && bulan != puncak) {
                lencana(g, NAMA_BULAN[bulan].substring(0, 3) + "  Rp " + ringkas(Math.round(tot[bulan])), xm(bulan), ym(tot[bulan]) - 16, AKSEN2);
            }
        }

        private void lencana(Graphics2D g, String s, double cx, double cy, Color c) {
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(s) + 18, th = 22;
            int x = (int) Math.max(pKiri, Math.min(pKiri + pLebar - tw, cx - tw / 2.0));
            int y = (int) Math.max(pAtas - 22, cy - th);
            g.setColor(alfa(BG1, 230));
            g.fillRoundRect(x, y, tw, th, th, th);
            g.setColor(alfa(c, 170));
            g.setStroke(new BasicStroke(1f));
            g.drawRoundRect(x, y, tw, th, th, th);
            g.setColor(c);
            g.drawString(s, x + 9, y + 15);
        }

        // ----- mode 2/3: garis per bangsal / komponen -----
        private void lukisSeri(Graphics2D g) {
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            FontMetrics fm = g.getFontMetrics();
            for (int s = 0; s < namaSeri.length; s++) {
                Rectangle r = rLeg[s];
                if (r == null) {
                    continue;
                }
                boolean on = tampil[s], hv = s == hoverLeg;
                Color c = warnaSeri(s);
                g.setColor(new Color(255, 255, 255, hv ? 30 : 12));
                g.fillRoundRect(r.x, r.y, r.width, r.height, r.height, r.height);
                g.setColor(alfa(c, on ? 150 : 50));
                g.setStroke(new BasicStroke(1f));
                g.drawRoundRect(r.x, r.y, r.width, r.height, r.height, r.height);
                g.setPaint(new GradientPaint(r.x + 8, r.y + 6, on ? campur(c, Color.WHITE, 0.45) : new Color(90, 100, 125), r.x + 20, r.y + 18, on ? c : new Color(70, 80, 105)));
                g.fillOval(r.x + 9, r.y + 7, 10, 10);
                g.setColor(on ? TEKS : alfa(TEKS_REDUP, 140));
                g.drawString(potong(namaSeri[s], fm, 150), r.x + 26, r.y + 16);
            }
            // gambar: yang disorot (legenda) paling akhir
            for (int pass = 0; pass < 2; pass++) {
                for (int s = 0; s < namaSeri.length; s++) {
                    if (!tampil[s] || (pass == 0) == (s == hoverLeg)) {
                        continue;
                    }
                    garisSeri(g, s);
                }
            }
            // penanda bulan terpilih di tiap garis
            if (statis || progress > 0.9) {
                for (int s = 0; s < namaSeri.length; s++) {
                    if (tampil[s] && bulan >= 0 && bulan < 12 && !Double.isNaN(nilaiSeri[s][bulan])) {
                        double x = xm(bulan), y = ym(nilaiSeri[s][bulan]);
                        g.setColor(Color.WHITE);
                        g.fill(new java.awt.geom.Ellipse2D.Double(x - 5.5, y - 5.5, 11, 11));
                        g.setStroke(new BasicStroke(2.4f));
                        g.setColor(warnaSeri(s));
                        g.draw(new java.awt.geom.Ellipse2D.Double(x - 5.5, y - 5.5, 11, 11));
                        g.setStroke(new BasicStroke(1f));
                    }
                }
            }
        }

        private void garisSeri(Graphics2D g, int s) {
            Color c = warnaSeri(s);
            boolean sorot = s == hoverLeg, redup = hoverLeg >= 0 && !sorot;
            double[] v = nilaiSeri[s];
            Shape klipAwal = g.getClip();
            double rv = rev(s);
            potongReveal(g, rv);
            for (int[] r : runs(v)) {
                if (r[1] > r[0]) {
                    java.awt.geom.Path2D.Double p = jalur(v, r[0], r[1]);
                    if (sorot) {
                        java.awt.geom.Path2D.Double area = new java.awt.geom.Path2D.Double(p);
                        area.lineTo(xm(r[1]), pBawah);
                        area.lineTo(xm(r[0]), pBawah);
                        area.closePath();
                        g.setPaint(new GradientPaint(0, pAtas, alfa(c, 110), 0, pBawah, alfa(c, 0)));
                        g.fill(area);
                    }
                    g.setStroke(new BasicStroke(sorot ? 8f : 6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.setColor(alfa(c, redup ? 8 : 30));
                    g.draw(p);
                    g.setStroke(new BasicStroke(sorot ? 3.2f : 2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.setColor(alfa(c, redup ? 70 : 240));
                    g.draw(p);
                }
            }
            g.setClip(klipAwal);
            for (int m = 0; m < 12; m++) {
                if (Double.isNaN(v[m])) {
                    continue;
                }
                double x = xm(m), y = ym(v[m]);
                double pop = statis ? 1.0 : balik((rv * (pLebar + 28) - (x - pKiri + 14)) / 46.0);
                if (pop <= 0.02) {
                    continue;
                }
                double r = (m == hoverBln ? 5 : 3.3) * pop;
                g.setColor(redup ? alfa(c, 80) : c);
                g.fill(new java.awt.geom.Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
            }
        }

        // ----- bidik & tooltip -----
        private void bidik(Graphics2D g) {
            double x = xm(hoverBln);
            g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{3f, 4f}, 0f));
            g.setColor(alfa(Color.WHITE, 90));
            g.drawLine((int) x, pAtas - 6, (int) x, pBawah);
            g.setStroke(new BasicStroke(1f));
        }

        private void tooltip(Graphics2D g, int w, int h) {
            int m = hoverBln;
            java.util.ArrayList<String> k = new java.util.ArrayList<>(), v = new java.util.ArrayList<>();
            java.util.ArrayList<Color> wk = new java.util.ArrayList<>(), wv = new java.util.ArrayList<>();
            if (mode == 0) {
                if (Double.isNaN(tot[m]) && Double.isNaN(totL[m])) {
                    return;
                }
                long ini = Double.isNaN(tot[m]) ? 0 : Math.round(tot[m]), lw = Double.isNaN(totL[m]) ? 0 : Math.round(totL[m]);
                k.add("Tahun " + tahun);
                v.add("Rp " + uang(ini));
                wk.add(AKSEN2);
                wv.add(Color.WHITE);
                k.add("Tahun " + (tahun - 1));
                v.add("Rp " + uang(lw));
                wk.add(EMAS);
                wv.add(Color.WHITE);
                k.add("Selisih");
                v.add(delta(ini, lw) + (lw > 0 ? "  (" + (ini >= lw ? "+" : "-") + ringkas(Math.abs(ini - lw)) + ")" : ""));
                wk.add(TEKS_REDUP);
                wv.add(warnaDelta(ini, lw));
            } else {
                Integer[] urut = new Integer[namaSeri.length];
                for (int s = 0; s < urut.length; s++) {
                    urut[s] = s;
                }
                final int mm = m;
                Arrays.sort(urut, (a, b) -> Double.compare(Double.isNaN(nilaiSeri[b][mm]) ? -1 : nilaiSeri[b][mm], Double.isNaN(nilaiSeri[a][mm]) ? -1 : nilaiSeri[a][mm]));
                for (int s : urut) {
                    if (tampil[s] && !Double.isNaN(nilaiSeri[s][m])) {
                        k.add(namaSeri[s]);
                        v.add("Rp " + uang(Math.round(nilaiSeri[s][m])));
                        wk.add(warnaSeri(s));
                        wv.add(Color.WHITE);
                    }
                }
                if (k.isEmpty()) {
                    return;
                }
                k.add("Total semua");
                v.add("Rp " + uang(Double.isNaN(tot[m]) ? 0 : Math.round(tot[m])));
                wk.add(TEKS_REDUP);
                wv.add(EMAS);
            }
            Font fN = new Font("Segoe UI", Font.PLAIN, 12), fB = new Font("Segoe UI", Font.BOLD, 12);
            g.setFont(fN);
            FontMetrics fn = g.getFontMetrics();
            g.setFont(fB);
            FontMetrics fb = g.getFontMetrics();
            int kw = 0, vw = 0;
            for (int i = 0; i < k.size(); i++) {
                String kk = potong(k.get(i), fn, 190);
                k.set(i, kk);
                kw = Math.max(kw, fn.stringWidth(kk) + 14);
                vw = Math.max(vw, fb.stringWidth(v.get(i)));
            }
            String judul = NAMA_BULAN[m] + " " + tahun;
            int bw = Math.max(16 + kw + 18 + vw + 14, 16 + fb.stringWidth(judul) + 16), bh = 38 + k.size() * 19 + 6;
            int tx = mx + 18, ty = my + 12;
            if (tx + bw > w - 8) {
                tx = mx - 18 - bw;
            }
            if (ty + bh > h - 8) {
                ty = h - 8 - bh;
            }
            g.setColor(new Color(8, 14, 32, 242));
            g.fillRoundRect(tx, ty, bw, bh, 14, 14);
            g.setColor(alfa(AKSEN2, 130));
            g.setStroke(new BasicStroke(1f));
            g.drawRoundRect(tx, ty, bw, bh, 14, 14);
            g.setFont(fB);
            g.setColor(EMAS);
            g.drawString(judul, tx + 14, ty + 23);
            int y = ty + 42;
            for (int i = 0; i < k.size(); i++) {
                g.setColor(wk.get(i));
                g.fillOval(tx + 14, y - 5, 8, 8);
                g.setFont(fN);
                g.setColor(TEKS_REDUP);
                g.drawString(k.get(i), tx + 28, y + 4);
                g.setFont(fB);
                g.setColor(wv.get(i));
                g.drawString(v.get(i), tx + bw - 14 - fb.stringWidth(v.get(i)), y + 4);
                y += 19;
            }
        }
    }

    // ------------------------------------------------------------------
    //  Pemilih bangsal + tahun: chip bangsal (meluncur halus) menggantikan chip bulan
    // ------------------------------------------------------------------
    static final class PemilihBangsal extends JComponent {

        static final int TINGGI = 58;
        private static final Font F_CHIP = new Font("Segoe UI", Font.BOLD, 12);
        private static final Font F_KECIL = new Font("Segoe UI", Font.BOLD, 10);
        private static final int GAP = 6, PAD_CHIP = 18, H_CHIP = 32, BOX_W = 272;
        private static final String SEMUA = "SEMUA BANGSAL";

        private final java.util.function.Consumer<String> aksiPoli;
        private final IntConsumer aksiTahun;
        private final Runnable aksiSekarang;
        private String[] item = {SEMUA};
        private int[] cx = {0}, cw = {100};
        private int isiLebar = 100, lebarLihat = 0;
        private int sel = 0, hover = -1, hoverBtn = 0;
        private double pillX = 0, pillW = 100, pillTX = 0, pillTW = 100, offset = 0, targetOffset = 0;
        private boolean aktif = true, memuat = false, snap = true;
        private int bulan = 1, tahun = LocalDate.now().getYear();
        private Timer timer, timerMuat;
        private int vx0, vx1;
        private Rectangle rPrev = new Rectangle(), rNext = new Rectangle(), rYPrev = new Rectangle(), rYLabel = new Rectangle(),
                rYNext = new Rectangle(), rNow = new Rectangle(), rBox = new Rectangle();

        PemilihBangsal(java.util.function.Consumer<String> aksiPoli, IntConsumer aksiTahun, Runnable aksiSekarang) {
            this.aksiPoli = aksiPoli;
            this.aksiTahun = aksiTahun;
            this.aksiSekarang = aksiSekarang;
            setPreferredSize(new Dimension(100, TINGGI));
            setOpaque(true);
            ukur();
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    susun();
                    int c = chipDi(e.getX(), e.getY()), b = tombolDi(e.getX(), e.getY());
                    if (c != hover || b != hoverBtn) {
                        hover = c;
                        hoverBtn = b;
                        setCursor(Cursor.getPredefinedCursor(aktif && (c >= 0 || b > 0) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hover = -1;
                    hoverBtn = 0;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!aktif || !javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }
                    susun();
                    int c = chipDi(e.getX(), e.getY());
                    if (c >= 0) {
                        if (c != sel) {
                            pilih(c, true);
                        }
                        return;
                    }
                    int lebarLihat = vx1 - vx0;
                    switch (tombolDi(e.getX(), e.getY())) {
                        case 1:
                            targetOffset = Math.max(0, targetOffset - lebarLihat * 0.7);
                            jalan();
                            break;
                        case 2:
                            targetOffset = Math.min(maksOffset(), targetOffset + lebarLihat * 0.7);
                            jalan();
                            break;
                        case 3:
                            if (tahun > 2000 && aksiTahun != null) {
                                aksiTahun.accept(tahun - 1);
                            }
                            break;
                        case 4:
                            popupTahun();
                            break;
                        case 5:
                            if (tahun < LocalDate.now().getYear() && aksiTahun != null) {
                                aksiTahun.accept(tahun + 1);
                            }
                            break;
                        case 6:
                            if (aksiSekarang != null) {
                                aksiSekarang.run();
                            }
                            break;
                        default:
                    }
                }
            });
            addMouseWheelListener(e -> {
                susun();
                if (e.getX() >= vx0 - 40 && e.getX() <= vx1 + 40) {
                    targetOffset = Math.max(0, Math.min(maksOffset(), targetOffset + e.getWheelRotation() * 90));
                    jalan();
                }
            });
        }

        // ---------- API ----------
        /** Isi daftar poli (item pertama "SEMUA POLI" otomatis). terpilih = null berarti semua poli. */
        void setDaftar(java.util.List<String> poli, String terpilih) {
            TreeSet<String> s = new TreeSet<>(poli);
            if (terpilih != null) {
                s.add(terpilih);
            }
            item = new String[s.size() + 1];
            item[0] = SEMUA;
            int i = 1;
            for (String x : s) {
                item[i++] = x;
            }
            ukur();
            sel = terpilih == null ? 0 : Math.max(0, Arrays.asList(item).indexOf(terpilih));
            snap = true;
            repaint();
        }

        void setTerpilih(String p) {
            int i = p == null ? 0 : Arrays.asList(item).indexOf(p);
            sel = Math.max(0, i);
            susun();
            aturTarget(true);
            jalan();
        }

        void setPilihan(int b, int t) {
            bulan = b;
            tahun = t;
            repaint();
        }

        void pilihRelatif(int delta) {
            int ni = Math.max(0, Math.min(item.length - 1, sel + delta));
            if (aktif && ni != sel) {
                pilih(ni, true);
            }
        }

        void pilihSemua() {
            if (aktif && sel != 0) {
                pilih(0, true);
            }
        }

        boolean isAktif() {
            return aktif;
        }

        void setAktif(boolean a) {
            aktif = a;
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            repaint();
        }

        void setMemuat(boolean v) {
            memuat = v;
            if (v) {
                if (timerMuat == null) {
                    timerMuat = new Timer(16, e -> repaint());
                }
                timerMuat.start();
            } else if (timerMuat != null) {
                timerMuat.stop();
            }
            repaint();
        }

        boolean bisaMundur() {
            return !(tahun <= 2000 && bulan <= 1);
        }

        boolean bisaMaju() {
            LocalDate h = LocalDate.now();
            return !(tahun >= h.getYear() && bulan >= h.getMonthValue());
        }

        void hentikan() {
            if (timer != null) {
                timer.stop();
            }
            if (timerMuat != null) {
                timerMuat.stop();
            }
        }

        // ---------- geometri & gerak ----------
        private void ukur() {
            FontMetrics fm = getFontMetrics(F_CHIP);
            cx = new int[item.length];
            cw = new int[item.length];
            int x = 0;
            for (int i = 0; i < item.length; i++) {
                cw[i] = fm.stringWidth(item[i]) + 2 * PAD_CHIP;
                cx[i] = x;
                x += cw[i] + GAP;
            }
            isiLebar = Math.max(0, x - GAP);
            // chip sedikit: bagi rata sepanjang jalur agar tampak penuh
            if (lebarLihat > isiLebar && item.length > 0) {
                int extra = (lebarLihat - isiLebar) / item.length;
                x = 0;
                for (int i = 0; i < item.length; i++) {
                    cw[i] += extra;
                    cx[i] = x;
                    x += cw[i] + GAP;
                }
                isiLebar = Math.max(0, x - GAP);
            }
        }

        private void susun() {
            int w = getWidth(), h = getHeight(), cy = h / 2;
            rBox = new Rectangle(w - 16 - BOX_W, cy - 19, BOX_W, 38);
            rYPrev = new Rectangle(rBox.x + 6, cy - 14, 28, 28);
            rYLabel = new Rectangle(rBox.x + 36, cy - 14, 90, 28);
            rYNext = new Rectangle(rBox.x + 128, cy - 14, 28, 28);
            rNow = new Rectangle(rBox.x + 166, cy - 13, 98, 26);
            rPrev = new Rectangle(100, cy - 15, 30, 30);
            vx0 = rPrev.x + rPrev.width + 10;
            rNext = new Rectangle(rBox.x - 14 - 30, cy - 15, 30, 30);
            vx1 = rNext.x - 10;
        }

        private int maksOffset() {
            return Math.max(0, isiLebar - (vx1 - vx0));
        }

        private void aturTarget(boolean tengah) {
            pillTX = cx[sel];
            pillTW = cw[sel];
            if (tengah) {
                double t = cx[sel] + cw[sel] / 2.0 - (vx1 - vx0) / 2.0;
                targetOffset = Math.max(0, Math.min(maksOffset(), t));
            }
        }

        private void pilih(int i, boolean panggil) {
            sel = i;
            susun();
            aturTarget(true);
            jalan();
            if (panggil && aksiPoli != null) {
                aksiPoli.accept(i == 0 ? null : item[i]);
            }
        }

        private void jalan() {
            if (timer == null) {
                timer = new Timer(16, e -> tick());
            }
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        private void tick() {
            boolean lagi = false;
            double d1 = pillTX - pillX, d2 = pillTW - pillW, d3 = targetOffset - offset;
            if (Math.abs(d1) > 0.3 || Math.abs(d2) > 0.3) {
                pillX += d1 * 0.3;
                pillW += d2 * 0.3;
                lagi = true;
            } else {
                pillX = pillTX;
                pillW = pillTW;
            }
            if (Math.abs(d3) > 0.3) {
                offset += d3 * 0.22;
                lagi = true;
            } else {
                offset = targetOffset;
            }
            repaint();
            if (!lagi) {
                timer.stop();
            }
        }

        private int chipDi(int x, int y) {
            int cy = getHeight() / 2;
            if (y < cy - H_CHIP / 2 || y > cy + H_CHIP / 2 || x < vx0 || x > vx1) {
                return -1;
            }
            double px = x - vx0 + offset;
            for (int i = 0; i < item.length; i++) {
                if (px >= cx[i] && px < cx[i] + cw[i]) {
                    return i;
                }
            }
            return -1;
        }

        private int tombolDi(int x, int y) {
            if (rPrev.contains(x, y)) {
                return 1;
            }
            if (rNext.contains(x, y)) {
                return 2;
            }
            if (rYPrev.contains(x, y)) {
                return 3;
            }
            if (rYLabel.contains(x, y)) {
                return 4;
            }
            if (rYNext.contains(x, y)) {
                return 5;
            }
            return rNow.contains(x, y) ? 6 : 0;
        }

        private void popupTahun() {
            javax.swing.JPopupMenu pm = new javax.swing.JPopupMenu();
            int skr = LocalDate.now().getYear();
            for (int y = skr; y >= Math.max(2000, skr - 15); y--) {
                final int yy = y;
                javax.swing.JMenuItem mi = new javax.swing.JMenuItem(String.valueOf(y));
                mi.setFont(new Font("Segoe UI", y == tahun ? Font.BOLD : Font.PLAIN, 13));
                mi.addActionListener(e -> {
                    if (aktif && yy != tahun && aksiTahun != null) {
                        aksiTahun.accept(yy);
                    }
                });
                pm.add(mi);
            }
            pm.show(this, rYLabel.x, rYLabel.y + rYLabel.height + 2);
        }

        // ---------- lukisan ----------
        private void panah(Graphics2D g, Rectangle r, boolean kiri, boolean bisa, boolean sorot) {
            boolean on = bisa && aktif;
            g.setColor(new Color(255, 255, 255, on ? (sorot ? 42 : 18) : 6));
            g.fillOval(r.x, r.y, r.width, r.height);
            g.setStroke(new BasicStroke(1f));
            g.setColor(new Color(255, 255, 255, on ? 64 : 24));
            g.drawOval(r.x, r.y, r.width - 1, r.height - 1);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(on ? TEKS : alfa(TEKS_REDUP, 110));
            int mx = r.x + r.width / 2, my = r.y + r.height / 2, s = 4;
            if (kiri) {
                g.drawLine(mx + 2, my - s, mx - 2, my);
                g.drawLine(mx - 2, my, mx + 2, my + s);
            } else {
                g.drawLine(mx - 2, my - s, mx + 2, my);
                g.drawLine(mx + 2, my, mx - 2, my + s);
            }
            g.setStroke(new BasicStroke(1f));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            int w = getWidth(), h = getHeight(), cy = h / 2;
            susun();
            if (w > 0 && vx1 - vx0 != lebarLihat) {
                lebarLihat = vx1 - vx0;
                ukur();
                snap = true;
            }
            if (snap && w > 0) {
                aturTarget(true);
                offset = targetOffset;
                pillX = pillTX;
                pillW = pillTW;
                snap = false;
            }
            g.setPaint(new GradientPaint(0, 0, new Color(0x0B, 0x13, 0x2B), w, 0, new Color(0x0F, 0x1A, 0x33)));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(255, 255, 255, 22));
            g.drawLine(0, h - 1, w, h - 1);

            g.setFont(F_KECIL);
            g.setColor(TEKS_REDUP);
            g.drawString("BANGSAL", 22, cy + 4);

            // jalur chip
            int tx0 = rPrev.x - 5, tx1 = rNext.x + rNext.width + 5;
            g.setColor(new Color(255, 255, 255, aktif ? 14 : 8));
            g.fillRoundRect(tx0, cy - 21, tx1 - tx0, 42, 42, 42);
            g.setColor(new Color(255, 255, 255, 40));
            g.drawRoundRect(tx0, cy - 21, tx1 - tx0, 42, 42, 42);
            panah(g, rPrev, true, offset > 0.5, hoverBtn == 1);
            panah(g, rNext, false, offset < maksOffset() - 0.5, hoverBtn == 2);

            // chip poli
            Shape awal = g.getClip();
            g.clipRect(vx0, 0, Math.max(0, vx1 - vx0), h);
            int px = vx0 + (int) Math.round(pillX - offset);
            int pw = (int) Math.round(pillW);
            double geser = Math.min(1, Math.abs(pillTX - pillX) / 120.0);
            if (aktif) {
                g.setPaint(new RadialGradientPaint(px + pw / 2f, cy, Math.max(20f, pw * 0.8f), new float[]{0f, 1f},
                        new Color[]{alfa(AKSEN2, 80), alfa(AKSEN2, 0)}));
                g.fillRect(px - pw / 3, 0, pw + 2 * (pw / 3), h);
                g.setPaint(new GradientPaint(px, 0, AKSEN1, px + pw, 0, AKSEN2));
            } else {
                g.setColor(new Color(255, 255, 255, 40));
            }
            int ext = (int) Math.round(geser * 8);
            g.fillRoundRect(px - ext / 2, cy - H_CHIP / 2, pw + ext, H_CHIP, H_CHIP, H_CHIP);
            g.setFont(F_CHIP);
            FontMetrics fm = g.getFontMetrics();
            int base = cy + (fm.getAscent() - fm.getDescent()) / 2;
            for (int i = 0; i < item.length; i++) {
                int x = vx0 + cx[i] - (int) Math.round(offset);
                if (x + cw[i] < vx0 - 4 || x > vx1 + 4) {
                    continue;
                }
                double dekat = 1 - Math.min(1, Math.abs((pillX + pillW / 2) - (cx[i] + cw[i] / 2.0)) / (cw[i] * 0.9));
                Color c = campur(i == hover && aktif ? Color.WHITE : TEKS_REDUP, Color.WHITE, dekat);
                g.setColor(aktif ? c : alfa(c, 120));
                g.drawString(item[i], x + (cw[i] - fm.stringWidth(item[i])) / 2, base);
            }
            // pudar di tepi bila masih bisa digulir
            Color latar = campur(new Color(0x0D, 0x16, 0x30), Color.WHITE, 0.05);
            if (offset > 0.5) {
                g.setPaint(new LinearGradientPaint(vx0, 0, vx0 + 30, 0, new float[]{0f, 1f}, new Color[]{latar, alfa(latar, 0)}));
                g.fillRect(vx0, cy - 21, 30, 42);
            }
            if (offset < maksOffset() - 0.5) {
                g.setPaint(new LinearGradientPaint(vx1 - 30, 0, vx1, 0, new float[]{0f, 1f}, new Color[]{alfa(latar, 0), latar}));
                g.fillRect(vx1 - 30, cy - 21, 30, 42);
            }
            g.setClip(awal);

            // kotak tahun
            g.setColor(new Color(255, 255, 255, aktif ? 14 : 8));
            g.fillRoundRect(rBox.x, rBox.y, rBox.width, rBox.height, rBox.height, rBox.height);
            g.setColor(new Color(255, 255, 255, 46));
            g.drawRoundRect(rBox.x, rBox.y, rBox.width, rBox.height, rBox.height, rBox.height);
            panah(g, rYPrev, true, tahun > 2000, hoverBtn == 3);
            panah(g, rYNext, false, tahun < LocalDate.now().getYear(), hoverBtn == 5);
            if (hoverBtn == 4 && aktif) {
                g.setColor(new Color(255, 255, 255, 22));
                g.fillRoundRect(rYLabel.x, rYLabel.y, rYLabel.width, rYLabel.height, 14, 14);
            }
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g.setColor(aktif ? Color.WHITE : TEKS_REDUP);
            tengah(g, tahun + " \u25BE", rYLabel.x, rYLabel.width, rYLabel.y + 19);
            g.setColor(new Color(255, 255, 255, 36));
            g.drawLine(rBox.x + 160, rBox.y + 8, rBox.x + 160, rBox.y + rBox.height - 8);
            g.setColor(new Color(0x6E, 0xE7, 0xF9, hoverBtn == 6 && aktif ? 46 : 22));
            g.fillRoundRect(rNow.x, rNow.y, rNow.width, rNow.height, rNow.height, rNow.height);
            g.setFont(F_CHIP);
            g.setColor(aktif ? BIRU : alfa(BIRU, 110));
            tengah(g, "Bulan ini", rNow.x, rNow.width, rNow.y + 17);

            // penanda memuat: sapuan cahaya di dasar
            if (memuat) {
                long now = System.currentTimeMillis();
                int sx = (int) ((now / 5) % (w + 220)) - 220;
                g.setPaint(new LinearGradientPaint(sx, 0, sx + 220, 0, new float[]{0f, 0.5f, 1f},
                        new Color[]{alfa(AKSEN2, 0), alfa(AKSEN2, 230), alfa(AKSEN2, 0)}));
                g.fillRect(sx, h - 3, 220, 3);
            }
            g.dispose();
        }
    }

    // ------------------------------------------------------------------
    private final Pemuat pemuat;
    private TabelPanel tabel;
    private HeaderPanel header;
    private final GrafikPanel grafik = new GrafikPanel(this::klikBulan);
    private final PeringkatPanel peringkat = new PeringkatPanel(this::klikBulan);
    private final GarisPanel garis = new GarisPanel(this::klikBulan);
    private String bangsal = null;       // null = semua bangsal
    private Paket paket = Paket.kosong();
    private Data data = Data.kosong(), dataLalu = Data.kosong();
    private int dataTahun = 0;           // tahun dari data yang sedang tampil (0 = belum ada)
    private int bulan, tahun;
    private int urutan = 0;
    private int tampilan = 0;            // 0 = tabel, 1 = grafik, 2 = line chart, 3 = peringkat
    private final JPanel areaHeader = new JPanel(new BorderLayout());
    private final JPanel kartu = new JPanel(new CardLayout());
    private final JScrollPane sp;
    private final JLabel lblBanding = new JLabel(" ");
    private final Segmen segTampil = new Segmen(new String[]{"Tabel", "Grafik", "Line Chart", "Peringkat"}, 0, this::gantiTampilan);
    private final PemilihBangsal periode = new PemilihBangsal(this::gantiBangsal, this::gantiTahun, this::keBulanIni);
    private final RekapPenyakitRalan.Tombol btnReplay = new RekapPenyakitRalan.Tombol("Ulangi Animasi", BIRU);
    private final RekapPenyakitRalan.Tombol btnPng = new RekapPenyakitRalan.Tombol("Simpan PNG", ORANYE);
    private static final int TINGGI_KONTROL = 50;

    private RekapPendapatanRanap(Window owner, Pemuat pemuat, int bulan, int tahun) {
        super(owner, "Rekap Pendapatan Reguler Ranap per Bangsal", Dialog.ModalityType.APPLICATION_MODAL);
        this.pemuat = pemuat;
        LocalDate hari = LocalDate.now();
        int t = Math.max(2000, Math.min(tahun, hari.getYear()));
        int b = Math.max(1, Math.min(bulan, 12));
        if (t == hari.getYear() && b > hari.getMonthValue()) {
            b = hari.getMonthValue();
        }
        this.tahun = t;
        this.bulan = b;
        this.tabel = new TabelPanel(data, dataLalu, t, b, this::klikBulan);
        this.tabel.setMemuat(true);
        this.grafik.setMemuat(true);
        this.peringkat.setMemuat(true);
        this.garis.setMemuat(true);

        setLayout(new BorderLayout());
        header = new HeaderPanel(data, dataLalu, t, b);
        areaHeader.add(header, BorderLayout.CENTER);

        // baris kontrol: tampilan (Tabel / Grafik / Line Chart / Peringkat) dan keterangan periode
        JPanel kontrol = new JPanel(new BorderLayout());
        kontrol.setBackground(new Color(0x0F, 0x1A, 0x33));
        kontrol.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(255, 255, 255, 22)),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        kontrol.setPreferredSize(new Dimension(100, TINGGI_KONTROL));
        JPanel kiriK = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        kiriK.setOpaque(false);
        kiriK.add(labelKecil("TAMPILAN"));
        kiriK.add(segTampil);
        kontrol.add(kiriK, BorderLayout.WEST);
        lblBanding.setForeground(new Color(0x9F, 0xD8, 0xF0));
        lblBanding.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        kontrol.add(lblBanding, BorderLayout.EAST);

        JPanel selatanUtara = new JPanel(new BorderLayout());
        selatanUtara.add(periode, BorderLayout.NORTH);
        selatanUtara.add(kontrol, BorderLayout.CENTER);
        JPanel utara = new JPanel(new BorderLayout());
        utara.add(areaHeader, BorderLayout.CENTER);
        utara.add(selatanUtara, BorderLayout.SOUTH);
        add(utara, BorderLayout.NORTH);
        periode.setPilihan(b, t);

        sp = new JScrollPane(tabel);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(TabelPanel.ROW_H);
        sp.getHorizontalScrollBar().setUnitIncrement(TabelPanel.W_COL / 2);
        kartu.setOpaque(false);
        kartu.add(sp, "tabel");
        kartu.add(grafik, "grafik");
        kartu.add(garis, "garis");
        kartu.add(peringkat, "peringkat");
        add(kartu, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(0x0F, 0x1A, 0x33));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 22)),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        JLabel hint = new JLabel("Klik baris / batang: pilih bulan  \u2022  \u2190 \u2192 bulan  \u2022  \u2191 \u2193 bangsal  \u2022  T / G / L / P : tabel / grafik / line chart / peringkat  \u2022  Esc: tutup");
        hint.setForeground(TEKS_REDUP);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.add(hint, BorderLayout.WEST);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        tombol.setOpaque(false);
        RekapPenyakitRalan.Tombol btnTutup = new RekapPenyakitRalan.Tombol("Tutup", new Color(0x8F, 0xA6, 0xBD));
        btnReplay.addActionListener(e -> ulangiAnimasi());
        btnPng.addActionListener(e -> simpanPng());
        btnTutup.addActionListener(e -> dispose());
        tombol.add(btnReplay);
        tombol.add(btnPng);
        tombol.add(btnTutup);
        footer.add(tombol, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        JComponent rp = getRootPane();
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> geser(-1),
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> geser(1),
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> segTampil.pilih(0, true), KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> segTampil.pilih(1, true), KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> segTampil.pilih(2, true), KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> segTampil.pilih(3, true), KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> periode.pilihRelatif(-1), KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> periode.pilihRelatif(1), KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> periode.pilihSemua(), KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                atur(RekapPendapatanRanap.this.bulan, RekapPendapatanRanap.this.tahun);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                tabel.hentikan();
                header.hentikan();
                grafik.hentikan();
                peringkat.hentikan();
                garis.hentikan();
                segTampil.hentikan();
                periode.hentikan();
            }
        });
        setKontrolAktif(false);

        Dimension layar = Toolkit.getDefaultToolkit().getScreenSize();
        int tinggi = HeaderPanel.TINGGI + PemilihBangsal.TINGGI + TINGGI_KONTROL + (TabelPanel.PAD_ATAS + TabelPanel.H_GRUP
                + TabelPanel.H_SUB + TabelPanel.N_BARIS * TabelPanel.ROW_H + TabelPanel.BOTTOM) + 62 + 34;
        tinggi = Math.max(560, Math.min(tinggi, layar.height - 90));
        setSize(Math.min(1280, layar.width - 40), tinggi);
        setLocationRelativeTo(owner);
    }

    private static JLabel labelKecil(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(0x8F, 0xA6, 0xBD));
        return l;
    }

    // ------------------------------------------------------------------
    //  Pilihan data / tampilan / bulan / tahun
    // ------------------------------------------------------------------
    /** Pilih satu bangsal (null = semua bangsal): seluruh tampilan hanya memuat bangsal tsb. */
    private void gantiBangsal(String b) {
        if (!periode.isAktif()) {
            periode.setTerpilih(bangsal);
            return;
        }
        bangsal = b;
        periode.setTerpilih(b);
        if (dataTahun == 0) {
            return;
        }
        ambilData();
        bangunPanel(bulan, dataTahun);
        grafik.setData(data, dataLalu, dataTahun, bulan, false);
        garis.setData(data, dataLalu, dataTahun, bulan, false);
        peringkat.setData(data, dataLalu, dataTahun, bulan, false);
    }

    private void gantiTahun(int t) {
        if (periode.isAktif()) {
            atur(bulan, t);
        }
    }

    private void keBulanIni() {
        if (periode.isAktif()) {
            LocalDate h = LocalDate.now();
            atur(h.getMonthValue(), h.getYear());
        }
    }

    private void gantiTampilan(int i) {
        tampilan = i;
        ((CardLayout) kartu.getLayout()).show(kartu, i == 1 ? "grafik" : (i == 2 ? "garis" : (i == 3 ? "peringkat" : "tabel")));
        if (dataTahun != 0) {
            animasiTampilan();
        }
    }

    private void animasiTampilan() {
        if (tampilan == 1) {
            grafik.ulang();
        } else if (tampilan == 2) {
            garis.ulang();
        } else if (tampilan == 3) {
            peringkat.ulang();
        } else {
            tabel.mulaiAnimasi();
        }
    }

    private void ulangiAnimasi() {
        animasiTampilan();
        header.mulaiAnimasi();
    }

    private void ambilData() {
        data = paket.ambil(false, bangsal);
        dataLalu = paket.ambil(true, bangsal);
    }

    private void klikBulan(int b) {
        if (periode.isAktif() && b != bulan) {
            atur(b, tahun);
        }
    }

    private void geser(int delta) {
        if (!periode.isAktif() || (delta < 0 ? !periode.bisaMundur() : !periode.bisaMaju())) {
            return;
        }
        LocalDate t = LocalDate.of(tahun, bulan, 1).plusMonths(delta);
        atur(t.getMonthValue(), t.getYear());
    }

    /**
     * Terapkan bulan/tahun (dibatasi 2000 s.d. bulan ini). Ganti bulan dalam tahun yang sama hanya
     * memindahkan sorotan; ganti tahun memuat ulang data satu tahun (beserta pembandingnya).
     */
    private void atur(int b, int t) {
        LocalDate hari = LocalDate.now();
        if (t < 2000) {
            t = 2000;
            b = 1;
        }
        if (t > hari.getYear() || (t == hari.getYear() && b > hari.getMonthValue())) {
            t = hari.getYear();
            b = hari.getMonthValue();
        }
        bulan = b;
        tahun = t;
        periode.setPilihan(b, t);
        if (dataTahun == t) {
            tabel.setBulan(b);
            header.setBulan(b);
            grafik.setBulan(b);
            peringkat.setBulan(b);
            garis.setBulan(b);
        } else {
            muatData();
        }
    }

    private void setKontrolAktif(boolean aktif) {
        periode.setAktif(aktif);
        periode.setMemuat(!aktif);
        boolean ada = aktif && (data.ada || dataLalu.ada);
        btnReplay.setEnabled(ada);
        btnPng.setEnabled(ada);
    }

    private void muatData() {
        final int seq = ++urutan;
        final int b = bulan, t = tahun;
        final LocalDate hari = LocalDate.now();
        final LocalDate sampai = t == hari.getYear() ? hari : LocalDate.of(t, 12, 31);
        final LocalDate sampaiLalu = sampai.minusYears(1);
        setKontrolAktif(false);
        tabel.setMemuat(true);
        grafik.setMemuat(true);
        peringkat.setMemuat(true);
        garis.setMemuat(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Paket, Void>() {
            @Override
            protected Paket doInBackground() throws Exception {
                return pemuat.muat(t, sampai, sampaiLalu);
            }

            @Override
            protected void done() {
                if (seq != urutan) {
                    return;
                }
                setCursor(Cursor.getDefaultCursor());
                Paket hasil = Paket.kosong();
                try {
                    Paket g = get();
                    if (g != null) {
                        hasil = g;
                    }
                } catch (Exception ex) {
                    Throwable c = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(RekapPendapatanRanap.this,
                            "Gagal mengambil data rekap:\n" + c.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                terapkan(hasil, b, t, sampai, sampaiLalu);
                setKontrolAktif(true);
            }
        }.execute();
    }

    private void terapkan(Paket hasil, int b, int t, LocalDate sampai, LocalDate sampaiLalu) {
        paket = hasil;
        dataTahun = t;
        periode.setDaftar(Arrays.asList(BANGSAL), bangsal);
        ambilData();
        java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
        lblBanding.setText("Periode 1 Jan s.d. " + sampai.format(df) + "  \u2022  vs tahun " + (t - 1));
        bangunPanel(b, t);
        grafik.setMemuat(false);
        grafik.setData(data, dataLalu, t, b, true);
        peringkat.setMemuat(false);
        peringkat.setData(data, dataLalu, t, b, true);
        garis.setMemuat(false);
        garis.setData(data, dataLalu, t, b, true);
    }

    /** Buat ulang tabel + kartu header untuk data yang sedang dipilih, lalu mainkan animasi masuknya. */
    private void bangunPanel(int b, int t) {
        tabel.hentikan();
        header.hentikan();
        tabel = new TabelPanel(data, dataLalu, t, b, this::klikBulan);
        sp.setViewportView(tabel);
        header = new HeaderPanel(data, dataLalu, t, b);
        areaHeader.removeAll();
        areaHeader.add(header, BorderLayout.CENTER);
        areaHeader.revalidate();
        areaHeader.repaint();
        sp.getVerticalScrollBar().setValue(0);
        sp.getHorizontalScrollBar().setValue(0);
        tabel.mulaiAnimasi();
        header.mulaiAnimasi();
    }

    /** Gambar lengkap tabel (header + seluruh kolom + kolom pembanding) tanpa animasi; aman dipakai tanpa jendela terbuka. */
    static BufferedImage buatGambar(Data d, Data lalu, int tahun, int bulan, int lebar) {
        TabelPanel tb = new TabelPanel(d, lalu, tahun, bulan, null);
        tb.setStatis(true);
        HeaderPanel hd = new HeaderPanel(d, lalu, tahun, bulan);
        int lb = Math.max(lebar, tb.getPreferredSize().width);
        int tinggiTabel = tb.getPreferredSize().height;
        int th = HeaderPanel.TINGGI;
        hd.setBounds(0, 0, lb, th);
        tb.setBounds(0, th, lb, tinggiTabel);
        BufferedImage img = new BufferedImage(lb, th + tinggiTabel, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        hd.paint(g);
        g.translate(0, th);
        tb.paint(g);
        g.dispose();
        return img;
    }

    /** Gambar header + grafik perbandingan tanpa animasi. */
    static BufferedImage buatGambarGrafik(Data d, Data lalu, int tahun, int bulan, int lebar) {
        GrafikPanel gp = new GrafikPanel(null);
        gp.setData(d, lalu, tahun, bulan, true);
        gp.setStatis();
        HeaderPanel hd = new HeaderPanel(d, lalu, tahun, bulan);
        int lb = Math.max(lebar, 1000), th = HeaderPanel.TINGGI, tg = 560;
        hd.setBounds(0, 0, lb, th);
        gp.setBounds(0, th, lb, tg);
        BufferedImage img = new BufferedImage(lb, th + tg, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        hd.paint(g);
        g.translate(0, th);
        gp.paint(g);
        g.dispose();
        return img;
    }

    /** Gambar header + peringkat bangsal tanpa animasi. */
    static BufferedImage buatGambarPeringkat(Data d, Data lalu, int tahun, int bulan, int lebar) {
        PeringkatPanel pp = new PeringkatPanel(null);
        pp.setData(d, lalu, tahun, bulan, true);
        pp.setStatis();
        HeaderPanel hd = new HeaderPanel(d, lalu, tahun, bulan);
        int lb = Math.max(lebar, 1000), th = HeaderPanel.TINGGI, tg = 70 + 18 + 10 * 46;
        hd.setBounds(0, 0, lb, th);
        pp.setBounds(0, th, lb, tg);
        BufferedImage img = new BufferedImage(lb, th + tg, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        hd.paint(g);
        g.translate(0, th);
        pp.paint(g);
        g.dispose();
        return img;
    }

    /** Gambar header + line chart (mode: 0 = total vs tahun lalu, 1 = per bangsal, 2 = per komponen) tanpa animasi. */
    static BufferedImage buatGambarGaris(Data d, Data lalu, int tahun, int bulan, int lebar, int mode) {
        GarisPanel gp2 = new GarisPanel(null);
        gp2.setData(d, lalu, tahun, bulan, true);
        gp2.gantiModeLangsung(mode);
        gp2.setStatis();
        HeaderPanel hd = new HeaderPanel(d, lalu, tahun, bulan);
        int lb = Math.max(lebar, 1000), th = HeaderPanel.TINGGI, tg = 600;
        hd.setBounds(0, 0, lb, th);
        gp2.setBounds(0, th, lb, tg);
        gp2.doLayout();
        BufferedImage img = new BufferedImage(lb, th + tg, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        hd.paint(g);
        g.translate(0, th);
        gp2.paint(g);
        g.dispose();
        return img;
    }

    private void simpanPng() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Rekap sebagai Gambar");
        String kel = data.label.replace(" + ", "_").replace(' ', '_');
        fc.setSelectedFile(new java.io.File(AWALAN_FILE + dataTahun + "_" + kel + (tampilan == 1 ? "_grafik_" : (tampilan == 2 ? "_line_" : (tampilan == 3 ? "_peringkat_" : "_tabel_")))
                + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".png"));
        fc.setFileFilter(new FileNameExtensionFilter("Gambar PNG (*.png)", "png"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
            f = new java.io.File(f.getParentFile(), f.getName() + ".png");
        }
        if (f.exists() && JOptionPane.showConfirmDialog(this, "File sudah ada. Timpa?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int lebar = Math.max(1000, getWidth());
            BufferedImage img = tampilan == 1
                    ? buatGambarGrafik(data, dataLalu, dataTahun, bulan, lebar)
                    : (tampilan == 2
                            ? buatGambarGaris(data, dataLalu, dataTahun, bulan, lebar, garis.modeSekarang())
                            : (tampilan == 3
                                    ? buatGambarPeringkat(data, dataLalu, dataTahun, bulan, lebar)
                                    : buatGambar(data, dataLalu, dataTahun, bulan, lebar)));
            javax.imageio.ImageIO.write(img, "png", f);
            JOptionPane.showMessageDialog(this, "Gambar berhasil disimpan:\n" + f.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan gambar:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
