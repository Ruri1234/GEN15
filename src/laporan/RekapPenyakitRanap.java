package laporan;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.Point;
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
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Rekap RL 4.2 - 10 Besar Penyakit Rawat Inap (berdasarkan tanggal keluar), per bulan.
 * Menampilkan tabel rekapan (jenis kelamin hidup+mati dan keluar mati) dengan tema gelap + animasi
 * yang sama dengan Rekap Penyakit Rawat Jalan: pemilih bulan/tahun, data dimuat di latar belakang.
 */
public final class RekapPenyakitRanap extends JDialog {

    private static final String JUDUL = "RL 4.2 - 10 BESAR PENYAKIT RAWAT INAP";
    private static final String DASAR = "berdasarkan tanggal keluar";
    private static final String GRUP1 = "Jumlah Pasien Hidup dan Mati Menurut Jenis Kelamin";
    private static final String GRUP2 = "Jumlah Pasien Keluar Mati";
    private static final String AWALAN_FILE = "Rekap_10_Besar_Penyakit_Ranap_";

    private static final Color BG1 = new Color(0x0B, 0x13, 0x2B);
    private static final Color BG2 = new Color(0x1C, 0x25, 0x41);
    private static final Color TEKS = new Color(0xE6, 0xEE, 0xF8);
    private static final Color TEKS_REDUP = new Color(0x8F, 0xA6, 0xBD);
    private static final Color EMAS = new Color(0xFF, 0xD5, 0x6B);
    private static final Color MERAH = new Color(0xFF, 0x8F, 0xA3);
    private static final Color BIRU = new Color(0x6E, 0xE7, 0xF9);
    private static final Color ORANYE = new Color(0xFF, 0xC3, 0x71);
    private static final Color HIJAU = new Color(0x7C, 0xE3, 0xA1);

    /** Angka dengan pemisah ribuan titik, mis. 1272 -> "1.272". */
    static String rib(long v) {
        String s = Long.toString(Math.abs(v));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) {
                sb.append('.');
            }
            sb.append(s.charAt(i));
        }
        return (v < 0 ? "-" : "") + sb;
    }

    /** Satu baris penyakit: pasien keluar (hidup+mati) per jenis kelamin dan yang keluar mati. */
    public static final class Baris {
        public final String kode, nama;
        public final int lk, pr, matiLk, matiPr;

        public Baris(String kode, String nama, int lk, int pr, int matiLk, int matiPr) {
            this.kode = kode == null ? "" : kode;
            this.nama = nama == null ? "" : nama;
            this.lk = lk;
            this.pr = pr;
            this.matiLk = matiLk;
            this.matiPr = matiPr;
        }

        public int total() {
            return lk + pr;
        }

        public int mati() {
            return matiLk + matiPr;
        }
    }


    /** Ikon tabel rekap berwarna - digambar sendiri, tidak butuh file gambar. */
    public static ImageIcon ikonTabel() {
        int n = 22;
        BufferedImage im = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        // bingkai + kepala tabel bergradasi
        g.setColor(new Color(0x3C, 0x4F, 0x6B));
        g.fillRoundRect(1, 2, 20, 18, 5, 5);
        g.setColor(Color.WHITE);
        g.fillRoundRect(2, 3, 18, 16, 4, 4);
        g.setPaint(new GradientPaint(0, 3, new Color(0x6E, 0xE7, 0xF9), 0, 8, new Color(0x2F, 0x80, 0xED)));
        g.fillRoundRect(2, 3, 18, 5, 4, 4);
        g.fillRect(2, 6, 18, 2);
        // garis baris & kolom
        g.setColor(new Color(0x3C, 0x4F, 0x6B));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(2, 11, 19, 11);
        g.drawLine(2, 15, 19, 15);
        g.drawLine(8, 8, 8, 18);
        g.drawLine(14, 8, 14, 18);
        // sel berwarna
        g.setPaint(new GradientPaint(0, 12, new Color(0xFF, 0xC3, 0x71), 0, 15, new Color(0xFF, 0x7A, 0x18)));
        g.fillRect(9, 12, 5, 3);
        g.setPaint(new GradientPaint(0, 16, new Color(0x7B, 0xF2, 0xA8), 0, 19, new Color(0x14, 0xA7, 0x6C)));
        g.fillRect(3, 16, 5, 3);
        g.dispose();
        return new ImageIcon(im);
    }

    private static final String[] NAMA_BULAN = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli",
        "Agustus", "September", "Oktober", "November", "Desember"};

    /** Pengambil data per rentang tanggal; dipanggil di thread latar belakang (bukan thread tampilan). */
    public interface Pemuat {

        List<Baris> muat(LocalDate dari, LocalDate sampai) throws Exception;
    }

    /**
     * Tampilkan dialog rekap per bulan (modal). Data bulan yang diberikan langsung dimuat;
     * bulan/tahun dapat diganti dari dalam dialog tanpa perlu mencari periode lagi.
     */
    public static void tampil(Window owner, Pemuat pemuat, int bulan, int tahun) {
        RekapPenyakitRanap d = new RekapPenyakitRanap(owner, pemuat, bulan, tahun);
        d.setVisible(true);
        d.dispose();
    }

    // ------------------------------------------------------------------
    //  Fungsi bantu warna & easing
    // ------------------------------------------------------------------
    static double klem(double v) {
        return Math.max(0, Math.min(1, v));
    }

    static double easeOut(double t) {
        return 1 - Math.pow(1 - klem(t), 3);
    }

    /** Easing dengan sedikit "memantul" di ujung (untuk lencana peringkat). */
    static double easeBack(double t) {
        t = klem(t);
        double c1 = 1.70158, c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    }

    static Color alfa(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    static Color campur(Color a, Color b, double t) {
        t = klem(t);
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                (int) Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    private static void aa(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /** Tulis teks rata tengah (horizontal) pada lebar tertentu. */
    private static void tengahX(Graphics2D g2, String t, int x, int lebar, int baseline) {
        g2.drawString(t, x + (lebar - g2.getFontMetrics().stringWidth(t)) / 2, baseline);
    }

    /** Tulis teks rata tengah (horizontal + vertikal) di dalam sebuah persegi. */
    private static void tengahBox(Graphics2D g2, String t, Rectangle r) {
        FontMetrics fm = g2.getFontMetrics();
        int ty = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(t, r.x + (r.width - fm.stringWidth(t)) / 2, ty);
    }

    // ------------------------------------------------------------------
    //  Tombol modern: pil bergradasi, transisi hover/tekan halus
    // ------------------------------------------------------------------
    static final class Tombol extends JButton {

        private final Color aksen;
        private double hv = 0, tk = 0;
        private boolean terpilih = false;
        private Timer tm;

        Tombol(String teks, Color aksen) {
            super(teks);
            this.aksen = aksen == null ? new Color(0x4C, 0x8D, 0xF5) : aksen;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tm = new Timer(16, e -> {
                boolean on = isEnabled();
                double th = (getModel().isRollover() && on) ? 1 : 0;
                double tp = (getModel().isArmed() && getModel().isPressed() && on) ? 1 : 0;
                hv += (th - hv) * 0.3;
                tk += (tp - tk) * 0.5;
                if (Math.abs(th - hv) < 0.01) {
                    hv = th;
                }
                if (Math.abs(tp - tk) < 0.01) {
                    tk = tp;
                }
                repaint();
                if (hv == th && tk == tp) {
                    ((Timer) e.getSource()).stop();
                }
            });
            addChangeListener(e -> {
                if (!tm.isRunning()) {
                    tm.start();
                }
            });
        }

        void setTerpilih(boolean v) {
            terpilih = v;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            if (isPreferredSizeSet()) {
                return super.getPreferredSize();
            }
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 44, 40);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            int w = getWidth(), h = getHeight();
            if (!isEnabled()) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
            }
            double k = terpilih ? Math.max(0.85, hv) : hv;
            int bx = 2, by = 2, bw = w - 4, bh = h - 4;
            if (k > 0.01) {
                g2.setColor(alfa(aksen, (int) (46 * k)));
                g2.fillRoundRect(0, 0, w, h, h, h);
            }
            g2.translate(0, tk);
            Color a1 = campur(new Color(0x2C, 0x44, 0x65), aksen, 0.12 + 0.40 * k);
            Color a2 = campur(new Color(0x1D, 0x31, 0x50), aksen, 0.05 + 0.22 * k);
            g2.setPaint(new GradientPaint(0, by, a1, 0, by + bh, a2));
            g2.fillRoundRect(bx, by, bw, bh, bh, bh);
            g2.setColor(alfa(Color.WHITE, 26));
            g2.drawLine(bx + bh / 2, by + 1, bx + bw - bh / 2, by + 1);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(campur(new Color(0x3E, 0x5C, 0x7E), aksen, 0.35 + 0.55 * k));
            g2.drawRoundRect(bx, by, bw - 1, bh - 1, bh, bh);
            if (tk > 0.01) {
                g2.setColor(alfa(Color.BLACK, (int) (55 * tk)));
                g2.fillRoundRect(bx, by, bw, bh, bh, bh);
            }
            g2.setFont(getFont());
            g2.setColor(campur(new Color(0xD6, 0xE2, 0xF0), Color.WHITE, k));
            tengahBox(g2, getText(), new Rectangle(0, 0, w, h));
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    //  Kontrol periode: chip bulan dengan sorotan meluncur + pilihan tahun
    // ------------------------------------------------------------------
    static final class PemilihPeriode extends JComponent {

        interface Pilih {

            void pilih(int bulan, int tahun);
        }

        static final int TINGGI = 68;
        private static final int TAHUN_MIN = 2000;
        private static final String[] SINGKAT = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
        private static final int ID_PREV = 12, ID_NEXT = 13, ID_TH_PREV = 14, ID_TH_NEXT = 15, ID_TH = 16, ID_INI = 17, JML_ID = 18;
        private static final Color LATAR = new Color(0x0F, 0x1A, 0x33);
        private static final Color AKSEN1 = new Color(0x2F, 0x80, 0xED);
        private static final Color AKSEN2 = new Color(0x00, 0xC2, 0xE0);
        private static final Font F_CHIP = new Font("Segoe UI", Font.BOLD, 12);
        private static final Font F_TAHUN = new Font("Segoe UI", Font.BOLD, 15);

        private final Pilih pendengar;
        private final Rectangle[] r = new Rectangle[JML_ID];
        private final double[] hov = new double[JML_ID];
        private final Timer timer;
        private int bulan = 1, tahun = 2000, tahunLama = 2000, arahTahun = 1;
        private int hoverId = -1;
        private double geserX = 0, animTahun = 1;
        private boolean aktif = true, memuat = false, awal = true;
        private int cX, cY, cW, cH, chipX, chipW;

        PemilihPeriode(Pilih p) {
            this.pendengar = p;
            for (int i = 0; i < JML_ID; i++) {
                r[i] = new Rectangle();
            }
            setPreferredSize(new Dimension(900, TINGGI));
            setOpaque(true);
            ToolTipManager.sharedInstance().registerComponent(this);
            timer = new Timer(16, e -> tick());
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    setHover(idDi(e.getPoint()));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setHover(-1);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }
                    int id = idDi(e.getPoint());
                    if (id >= 0 && boleh(id)) {
                        jalankan(id);
                    }
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        // ---------- API ----------
        void setPilihan(int b, int t) {
            if (awal) {
                awal = false;
                bulan = b;
                tahun = t;
                tahunLama = t;
                geserX = b - 1;
                animTahun = 1;
                repaint();
                return;
            }
            if (t != tahun) {
                tahunLama = tahun;
                arahTahun = t > tahun ? 1 : -1;
                animTahun = 0;
            }
            bulan = b;
            tahun = t;
            gerak();
        }

        void setAktif(boolean a) {
            aktif = a;
            gerak();
        }

        void setMemuat(boolean m) {
            memuat = m;
            gerak();
        }

        boolean isAktif() {
            return aktif;
        }

        boolean bisaMundur() {
            return !(tahun <= TAHUN_MIN && bulan == 1);
        }

        boolean bisaMaju() {
            LocalDate h = LocalDate.now();
            return tahun < h.getYear() || (tahun == h.getYear() && bulan < h.getMonthValue());
        }

        void hentikan() {
            timer.stop();
        }

        // ---------- status ----------
        private boolean valid(int id) {
            LocalDate h = LocalDate.now();
            if (id < 12) {
                return !(tahun == h.getYear() && id + 1 > h.getMonthValue());
            }
            switch (id) {
                case ID_PREV:
                    return bisaMundur();
                case ID_NEXT:
                    return bisaMaju();
                case ID_TH_PREV:
                    return tahun > TAHUN_MIN;
                case ID_TH_NEXT:
                    return tahun < h.getYear();
                case ID_TH:
                    return true;
                case ID_INI:
                    return !(bulan == h.getMonthValue() && tahun == h.getYear());
                default:
                    return false;
            }
        }

        private boolean boleh(int id) {
            return aktif && valid(id) && !(id < 12 && id + 1 == bulan);
        }

        private void setHover(int id) {
            if (id != hoverId) {
                hoverId = id;
                setCursor(Cursor.getPredefinedCursor(id >= 0 && boleh(id) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                gerak();
            }
        }

        private void gerak() {
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        private void tick() {
            boolean lagi = false;
            double tujuan = bulan - 1;
            double d = tujuan - geserX;
            if (Math.abs(d) > 0.003) {
                geserX += d * 0.22;
                lagi = true;
            } else {
                geserX = tujuan;
            }
            for (int i = 0; i < JML_ID; i++) {
                double tg = (i == hoverId && boleh(i)) ? 1 : 0;
                double dd = tg - hov[i];
                if (Math.abs(dd) > 0.01) {
                    hov[i] += dd * 0.3;
                    lagi = true;
                } else {
                    hov[i] = tg;
                }
            }
            if (animTahun < 1) {
                animTahun = Math.min(1, animTahun + 0.07);
                lagi = true;
            }
            if (memuat) {
                lagi = true;
            }
            repaint();
            if (!lagi) {
                timer.stop();
            }
        }

        // ---------- aksi ----------
        private void jalankan(int id) {
            LocalDate h = LocalDate.now();
            if (id < 12) {
                pendengar.pilih(id + 1, tahun);
            } else if (id == ID_PREV || id == ID_NEXT) {
                LocalDate d = LocalDate.of(tahun, bulan, 1).plusMonths(id == ID_PREV ? -1 : 1);
                pendengar.pilih(d.getMonthValue(), d.getYear());
            } else if (id == ID_TH_PREV) {
                pendengar.pilih(bulan, tahun - 1);
            } else if (id == ID_TH_NEXT) {
                pendengar.pilih(bulan, tahun + 1);
            } else if (id == ID_INI) {
                pendengar.pilih(h.getMonthValue(), h.getYear());
            } else if (id == ID_TH) {
                bukaPopupTahun();
            }
        }

        private void bukaPopupTahun() {
            Color lt = new Color(0x14, 0x20, 0x3D);
            JPopupMenu pm = new JPopupMenu();
            pm.setBorder(BorderFactory.createLineBorder(alfa(Color.WHITE, 50)));
            pm.setBackground(lt);
            JPanel grid = new JPanel(new GridLayout(0, 5, 6, 6));
            grid.setBackground(lt);
            grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            int sekarang = LocalDate.now().getYear();
            for (int y = sekarang; y >= TAHUN_MIN; y--) {
                final int yy = y;
                Tombol b = new Tombol(String.valueOf(y), AKSEN2);
                b.setTerpilih(y == tahun);
                b.setPreferredSize(new Dimension(62, 32));
                b.addActionListener(e -> {
                    pm.setVisible(false);
                    if (yy != tahun) {
                        pendengar.pilih(bulan, yy);
                    }
                });
                grid.add(b);
            }
            pm.add(grid);
            hitung();
            Rectangle yr = r[ID_TH];
            pm.show(this, Math.max(0, yr.x + yr.width / 2 - 177), cY + cH + 6);
        }

        // ---------- geometri ----------
        private int idDi(Point p) {
            hitung();
            for (int i = 0; i < JML_ID; i++) {
                if (r[i].contains(p)) {
                    return i;
                }
            }
            return -1;
        }

        private void hitung() {
            int w = getWidth(), h = getHeight();
            cX = 20;
            cW = Math.max(200, w - 40);
            cH = 46;
            cY = (h - cH) / 2 - 2;
            int cy = cY + cH / 2;
            int x = cX + cW - 10;
            r[ID_INI].setBounds(x - 92, cy - 15, 92, 30);
            x -= 92 + 10;
            r[ID_TH_NEXT].setBounds(x - 26, cy - 15, 26, 30);
            x -= 26;
            r[ID_TH].setBounds(x - 64, cy - 15, 64, 30);
            x -= 64;
            r[ID_TH_PREV].setBounds(x - 26, cy - 15, 26, 30);
            x -= 26 + 12;
            r[ID_NEXT].setBounds(x - 32, cy - 16, 32, 32);
            x -= 32 + 8;
            int kanan = x;
            int kiri = cX + 14 + 74;
            r[ID_PREV].setBounds(kiri, cy - 16, 32, 32);
            chipX = kiri + 32 + 8;
            chipW = Math.max(12 * 34, kanan - chipX);
            double cw = chipW / 12.0;
            for (int i = 0; i < 12; i++) {
                int x0 = chipX + (int) Math.round(i * cw);
                int x1 = chipX + (int) Math.round((i + 1) * cw);
                r[i].setBounds(x0, cy - 16, x1 - x0, 32);
            }
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            int id = idDi(e.getPoint());
            if (id < 0 || !valid(id)) {
                return null;
            }
            if (id < 12) {
                return "Pilih " + NAMA_BULAN[id] + " " + tahun;
            }
            switch (id) {
                case ID_PREV:
                    return "Bulan sebelumnya  (\u2190)";
                case ID_NEXT:
                    return "Bulan berikutnya  (\u2192)";
                case ID_TH_PREV:
                    return "Tahun sebelumnya";
                case ID_TH_NEXT:
                    return "Tahun berikutnya";
                case ID_TH:
                    return "Pilih tahun";
                case ID_INI:
                    return "Kembali ke bulan ini";
                default:
                    return null;
            }
        }

        // ---------- gambar ----------
        private void chevron(Graphics2D g2, Rectangle b, boolean kiri, double skala, Color c) {
            double cx = b.getCenterX(), cy = b.getCenterY();
            double dx = kiri ? -1 : 1;
            Path2D.Double p = new Path2D.Double();
            p.moveTo(cx - dx * 3 * skala, cy - 5 * skala);
            p.lineTo(cx + dx * 3 * skala, cy);
            p.lineTo(cx - dx * 3 * skala, cy + 5 * skala);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(c);
            g2.draw(p);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            hitung();
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, new Color(0x10, 0x1C, 0x38), 0, h, LATAR));
            g2.fillRect(0, 0, w, h);

            // wadah kaca
            g2.setColor(alfa(Color.WHITE, 13));
            g2.fillRoundRect(cX, cY, cW, cH, cH, cH);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(alfa(Color.WHITE, 34));
            g2.drawRoundRect(cX, cY, cW, cH, cH, cH);
            int cy = cY + cH / 2;

            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(TEKS_REDUP);
            g2.drawString("PERIODE", cX + 20, cy + 4);

            // tombol bulan sebelumnya / berikutnya (bulat)
            for (int id : new int[]{ID_PREV, ID_NEXT}) {
                Rectangle b = r[id];
                double hv = hov[id];
                g2.setColor(alfa(Color.WHITE, (int) (14 + 34 * hv)));
                g2.fillOval(b.x, b.y, b.width, b.height);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(alfa(Color.WHITE, (int) (40 + 60 * hv)));
                g2.drawOval(b.x, b.y, b.width - 1, b.height - 1);
                chevron(g2, b, id == ID_PREV, 1.0,
                        valid(id) ? campur(TEKS_REDUP, Color.WHITE, hv) : alfa(TEKS_REDUP, 60));
            }

            // jalur chip + sorotan yang meluncur
            double cw = chipW / 12.0;
            g2.setColor(alfa(Color.WHITE, 9));
            g2.fillRoundRect(chipX - 2, cy - 18, chipW + 4, 36, 36, 36);
            int ix = chipX + (int) Math.round(geserX * cw);
            int iw = (int) Math.round(cw);
            int iy = cy - 16;
            g2.setColor(alfa(AKSEN1, 55));
            g2.fillRoundRect(ix - 2, iy - 2, iw + 4, 36, 36, 36);
            g2.setPaint(new GradientPaint(ix, iy, AKSEN2, ix + iw, iy + 32, AKSEN1));
            g2.fillRoundRect(ix + 1, iy, iw - 2, 32, 32, 32);
            g2.setColor(alfa(Color.WHITE, 70));
            g2.drawRoundRect(ix + 1, iy, iw - 3, 31, 32, 32);

            g2.setFont(F_CHIP);
            for (int i = 0; i < 12; i++) {
                Rectangle c = r[i];
                boolean ok = boleh(i);
                double dekat = 1 - Math.min(1, Math.abs(geserX - i));
                if (ok && hov[i] > 0.01) {
                    g2.setColor(alfa(Color.WHITE, (int) (30 * hov[i] * (1 - dekat))));
                    g2.fillRoundRect(c.x + 1, c.y, c.width - 2, c.height, 32, 32);
                }
                Color dasar = valid(i) ? campur(TEKS_REDUP, TEKS, hov[i]) : alfa(TEKS_REDUP, 70);
                g2.setColor(campur(dasar, Color.WHITE, dekat));
                tengahBox(g2, SINGKAT[i], c);
            }

            // pemisah
            g2.setColor(alfa(Color.WHITE, 28));
            int dx1 = r[ID_TH_PREV].x - 7;
            g2.drawLine(dx1, cy - 11, dx1, cy + 11);
            int dx2 = r[ID_INI].x - 5;
            g2.drawLine(dx2, cy - 11, dx2, cy + 11);

            // tahun: panah + label (dengan animasi geser vertikal)
            for (int id : new int[]{ID_TH_PREV, ID_TH_NEXT}) {
                Rectangle b = r[id];
                double hv = hov[id];
                g2.setColor(alfa(Color.WHITE, (int) (30 * hv)));
                g2.fillRoundRect(b.x, b.y + 2, b.width, b.height - 4, 12, 12);
                chevron(g2, b, id == ID_TH_PREV, 0.8,
                        valid(id) ? campur(TEKS_REDUP, Color.WHITE, hv) : alfa(TEKS_REDUP, 60));
            }
            Rectangle yr = r[ID_TH];
            g2.setColor(alfa(Color.WHITE, (int) (12 + 24 * hov[ID_TH])));
            g2.fillRoundRect(yr.x, yr.y + 2, yr.width, yr.height - 4, 12, 12);
            Shape klipLama = g2.getClip();
            g2.clipRect(yr.x, yr.y, yr.width - 14, yr.height);
            g2.setFont(F_TAHUN);
            FontMetrics fmT = g2.getFontMetrics();
            int blT = yr.y + (yr.height - fmT.getHeight()) / 2 + fmT.getAscent();
            java.awt.Composite cOld = g2.getComposite();
            if (animTahun < 1) {
                double a = easeOut(animTahun);
                g2.setColor(Color.WHITE);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (1 - a)));
                tengahX(g2, String.valueOf(tahunLama), yr.x, yr.width - 14, (int) Math.round(blT - arahTahun * a * 14));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) a));
                tengahX(g2, String.valueOf(tahun), yr.x, yr.width - 14, (int) Math.round(blT + arahTahun * (1 - a) * 14));
                g2.setComposite(cOld);
            } else {
                g2.setColor(Color.WHITE);
                tengahX(g2, String.valueOf(tahun), yr.x, yr.width - 14, blT);
            }
            g2.setClip(klipLama);
            // segitiga kecil penanda dropdown
            int tx = yr.x + yr.width - 10, ty = yr.y + yr.height / 2;
            g2.setColor(alfa(TEKS_REDUP, 200));
            g2.fillPolygon(new int[]{tx - 4, tx + 4, tx}, new int[]{ty - 2, ty - 2, ty + 3}, 3);

            // "Bulan ini"
            Rectangle ir = r[ID_INI];
            boolean vi = valid(ID_INI);
            double hi = hov[ID_INI];
            g2.setColor(alfa(AKSEN2, vi ? (int) (22 + 50 * hi) : 8));
            g2.fillRoundRect(ir.x, ir.y, ir.width, ir.height, ir.height, ir.height);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(alfa(AKSEN2, vi ? (int) (100 + 120 * hi) : 40));
            g2.drawRoundRect(ir.x, ir.y, ir.width - 1, ir.height - 1, ir.height, ir.height);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(vi ? campur(new Color(0x9B, 0xEA, 0xF7), Color.WHITE, hi) : alfa(TEKS_REDUP, 90));
            tengahBox(g2, "Bulan ini", ir);

            // garis kemajuan saat memuat data
            if (memuat) {
                int lx = cX + cH / 2, lw = cW - cH, ly = cY + cH - 4;
                g2.setColor(alfa(Color.WHITE, 14));
                g2.fillRoundRect(lx, ly, lw, 2, 2, 2);
                double ph = (System.currentTimeMillis() % 1100) / 1100.0;
                int sw = Math.max(60, lw / 4);
                int sx = lx + (int) ((lw + sw) * ph) - sw;
                Shape k0 = g2.getClip();
                g2.clipRect(lx, ly - 1, lw, 4);
                g2.setPaint(new LinearGradientPaint(sx, 0, sx + sw, 0, new float[]{0f, 0.5f, 1f},
                        new Color[]{alfa(AKSEN2, 0), AKSEN2, alfa(AKSEN2, 0)}));
                g2.fillRoundRect(sx, ly, sw, 2, 2, 2);
                g2.setClip(k0);
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    static final class HeaderPanel extends JPanel {
        private static final long DURASI = 700L;
        private final String periode;
        private final int totalPasien, totalMati, topJumlah;
        private final String topNama;
        private boolean statis = true;
        private long mulai = 0;
        private Timer timer;

        HeaderPanel(String periode, List<Baris> data) {
            this.periode = periode;
            int t = 0, m = 0;
            for (Baris b : data) {
                t += b.total();
                m += b.mati();
            }
            this.totalPasien = t;
            this.totalMati = m;
            this.topNama = data.isEmpty() ? "-" : data.get(0).kode + " - " + data.get(0).nama;
            this.topJumlah = data.isEmpty() ? 0 : data.get(0).total();
            setPreferredSize(new Dimension(900, 150));
            setOpaque(true);
        }

        void mulaiAnimasi() {
            statis = false;
            mulai = System.currentTimeMillis();
            if (timer != null) {
                timer.stop();
            }
            timer = new Timer(16, e -> {
                repaint();
                if (System.currentTimeMillis() - mulai >= DURASI + 3 * 110 + 60) {
                    ((Timer) e.getSource()).stop();
                    statis = true;
                    repaint();
                }
            });
            timer.start();
        }

        void hentikan() {
            if (timer != null) {
                timer.stop();
            }
        }

        /** i = -1 untuk judul, 0..2 untuk kartu (masuk bergantian). */
        private double kemajuan(int i) {
            if (statis) {
                return 1.0;
            }
            return easeOut((System.currentTimeMillis() - mulai - (long) (i + 1) * 110) / (double) DURASI);
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
            g2.setColor(new Color(0x9F, 0xD8, 0xF0));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.drawString("Periode : " + periode + "   \u2022   " + DASAR, 29, 64);
            g2.translate((1 - p0) * 24, 0);
            g2.setComposite(lama);

            String[] teksLabel = {"TOTAL PASIEN KELUAR", "KELUAR MATI", "TERBANYAK"};
            Color[] warna = {BIRU, MERAH, EMAS};
            int[] lebar = {190, 150, Math.max(260, w - 28 - 190 - 150 - 24 - 28)};
            int x = 28, y = 80;
            for (int i = 0; i < 3; i++) {
                double p = kemajuan(i);
                if (p > 0.001) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) p));
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
                    g2.setFont(new Font("Segoe UI", Font.BOLD, i == 2 ? 15 : 22));
                    g2.setColor(i == 2 ? EMAS : (i == 1 && totalMati > 0 ? MERAH : Color.WHITE));
                    String v;
                    if (i == 0) {
                        v = rib(Math.round(totalPasien * p));
                    } else if (i == 1) {
                        v = rib(Math.round(totalMati * p));
                    } else {
                        v = topJumlah > 0 ? rib(Math.round(topJumlah * p)) + "  \u2022  " + topNama : "-";
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
    static final class TabelPanel extends JPanel implements Scrollable {
        private static final int PAD = 24, PAD_ATAS = 22, H_GRUP = 50, H_SUB = 32, ROW_H = 40, BOTTOM = 30;
        private static final int W_NO = 64, W_ICD = 96;
        private static final Font F_HEAD = new Font("Segoe UI", Font.BOLD, 13);
        private static final Font F_SUB = new Font("Segoe UI", Font.BOLD, 12);
        private static final Font F_ISI = new Font("Segoe UI", Font.PLAIN, 14);
        private static final Font F_TEBAL = new Font("Segoe UI", Font.BOLD, 14);

        private final List<Baris> data;
        private final int max;
        private final int[] rank;
        private boolean statis = false;
        private boolean memuat = false;
        private long mulai = 0;
        private Timer timer, timerHover, timerMuat;
        private int hover = -1;
        private double hoverPos = 0, hoverA = 0;

        TabelPanel(List<Baris> data) {
            this.data = data;
            int mx = 1;
            for (Baris b : data) {
                mx = Math.max(mx, b.total());
            }
            this.max = mx;
            this.rank = new int[data.size()];
            for (int i = 0; i < data.size(); i++) {
                rank[i] = (i > 0 && data.get(i).total() == data.get(i - 1).total()) ? rank[i - 1] : i + 1;
            }
            setOpaque(true);
            setPreferredSize(new Dimension(900, PAD_ATAS + H_GRUP + H_SUB + Math.max(3, data.size()) * ROW_H + BOTTOM));
            ToolTipManager.sharedInstance().registerComponent(this);
            timerHover = new Timer(16, e -> tickHover());
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int idx = (e.getY() - barisAwal()) / ROW_H;
                    if (e.getY() < barisAwal() || idx < 0 || idx >= TabelPanel.this.data.size()) {
                        idx = -1;
                    }
                    if (idx != hover) {
                        hover = idx;
                        setToolTipText(idx >= 0 ? tooltip(TabelPanel.this.data.get(idx)) : null);
                        jalankanHover();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    if (hover != -1) {
                        hover = -1;
                        jalankanHover();
                    }
                }
            });
        }

        private void jalankanHover() {
            if (!timerHover.isRunning()) {
                timerHover.start();
            }
        }

        /** Sorotan baris meluncur halus mengikuti kursor (bukan melompat). */
        private void tickHover() {
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
            repaint();
            if (!lagi) {
                timerHover.stop();
            }
        }

        private static int barisAwal() {
            return PAD_ATAS + H_GRUP + H_SUB;
        }

        private String tooltip(Baris d) {
            return "<html><b>" + esc(d.kode) + " - " + esc(d.nama) + "</b><br>"
                    + "Pasien keluar : Laki-laki <b>" + rib(d.lk) + "</b> &nbsp; Perempuan <b>" + rib(d.pr) + "</b> &nbsp; Total <b>" + rib(d.total()) + "</b><br>"
                    + "Keluar mati : <b>" + rib(d.mati()) + "</b> (L " + rib(d.matiLk) + ", P " + rib(d.matiPr) + ")</html>";
        }

        private static String esc(String s) {
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }

        void setStatis(boolean v) {
            statis = v;
        }

        /** Saat true: tabel diredupkan dan muncul penanda "Memuat data" (dipakai ketika ganti bulan). */
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
            final int delay = data.isEmpty() ? 0 : Math.min(70, 700 / data.size());
            final long durasiTotal = 650L + (long) data.size() * delay;
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

        void hentikan() {
            if (timer != null) {
                timer.stop();
            }
            if (timerHover != null) {
                timerHover.stop();
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
            int delay = data.isEmpty() ? 0 : Math.min(70, 700 / data.size());
            return klem((System.currentTimeMillis() - mulai - (long) i * delay) / 650.0);
        }

        private static Color medali(int rank) {
            switch (rank) {
                case 1:
                    return new Color(0xFF, 0xC8, 0x37);
                case 2:
                    return new Color(0xC9, 0xD1, 0xDB);
                default:
                    return new Color(0xD9, 0x8E, 0x4A);
            }
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

        private static void tengah(Graphics2D g2, String t, int x, int lebar, int baseline) {
            g2.drawString(t, x + (lebar - g2.getFontMetrics().stringWidth(t)) / 2, baseline);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, BG1, 0, h, BG2));
            g2.fillRect(0, 0, w, h);

            // ---------- geometri kolom ----------
            int numW = Math.max(60, Math.min(80, (w - 2 * PAD - W_NO - W_ICD - 300) / 6));
            int diagW = Math.max(240, w - 2 * PAD - W_NO - W_ICD - 6 * numW);
            int[] xs = new int[10];
            xs[0] = PAD;
            xs[1] = xs[0] + W_NO;
            xs[2] = xs[1] + W_ICD;
            xs[3] = xs[2] + diagW;
            for (int k = 4; k <= 9; k++) {
                xs[k] = xs[k - 1] + numW;
            }
            int kanan = xs[9];
            int yG = PAD_ATAS;
            int yS = yG + H_GRUP;
            int y0 = yS + H_SUB;
            int tabelBawah = y0 + data.size() * ROW_H;

            // ---------- kepala tabel ----------
            g2.setPaint(new GradientPaint(0, yG, new Color(255, 255, 255, 26), 0, y0, new Color(255, 255, 255, 10)));
            g2.fillRoundRect(PAD - 8, yG, kanan - PAD + 16, H_GRUP + H_SUB, 14, 14);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawRoundRect(PAD - 8, yG, kanan - PAD + 16, H_GRUP + H_SUB, 14, 14);

            g2.setFont(F_HEAD);
            g2.setColor(TEKS);
            int tengahKepala = yG + (H_GRUP + H_SUB) / 2;
            int bl = tengahKepala + 5;
            tengah(g2, "No.", xs[0], W_NO, bl);
            g2.setFont(F_SUB);
            tengah(g2, "Kelompok", xs[1], W_ICD, tengahKepala - 2);
            tengah(g2, "ICD-10", xs[1], W_ICD, tengahKepala + 13);
            g2.setFont(F_HEAD);
            tengah(g2, "Kelompok Diagnosa Penyakit", xs[2], diagW, bl);

            // judul kelompok + garis aksen
            String[] judulGrup = {GRUP1, GRUP2};
            Color[] aksen = {new Color(0x2F, 0x80, 0xED), new Color(0xE6, 0x39, 0x50)};
            int[] gx = {xs[3], xs[6]};
            int gw = 3 * numW;
            for (int k = 0; k < 2; k++) {
                g2.setFont(F_HEAD);
                g2.setColor(TEKS);
                FontMetrics fg = g2.getFontMetrics();
                String jd = judulGrup[k];
                if (fg.stringWidth(jd) <= gw - 12) {
                    tengah(g2, jd, gx[k], gw, yG + 29);
                } else { // bagi di spasi yang paling dekat dengan tengah kalimat
                    int mid = jd.length() / 2, best = -1;
                    for (int c = 0; c < jd.length(); c++) {
                        if (jd.charAt(c) == ' ' && (best < 0 || Math.abs(c - mid) < Math.abs(best - mid))) {
                            best = c;
                        }
                    }
                    tengah(g2, potong(jd.substring(0, best), fg, gw - 12), gx[k], gw, yG + 21);
                    tengah(g2, potong(jd.substring(best + 1), fg, gw - 12), gx[k], gw, yG + 37);
                }
                g2.setPaint(new GradientPaint(gx[k], 0, aksen[k], gx[k] + gw, 0, new Color(aksen[k].getRed(), aksen[k].getGreen(), aksen[k].getBlue(), 40)));
                g2.fillRoundRect(gx[k] + 6, yG + H_GRUP - 3, gw - 12, 3, 3, 3);
            }
            // sub-judul
            g2.setFont(F_SUB);
            String[] sub = {"Laki-Laki", "Perempuan", "Total", "Laki-Laki", "Perempuan", "Total"};
            Color[] warnaSub = {BIRU, ORANYE, EMAS, BIRU, ORANYE, EMAS};
            for (int k = 0; k < 6; k++) {
                g2.setColor(warnaSub[k]);
                tengah(g2, sub[k], xs[3 + k], numW, yS + 21);
            }
            // pemisah vertikal kepala
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawLine(xs[3], yG + 6, xs[3], y0 - 4);
            g2.drawLine(xs[6], yG + 6, xs[6], y0 - 4);
            g2.drawLine(xs[3], yS, xs[9], yS);

            // ---------- sorotan hover (meluncur halus) ----------
            if (hoverA > 0.01 && !data.isEmpty() && !memuat) {
                int hy = y0 + (int) Math.round(hoverPos * ROW_H);
                g2.setColor(alfa(Color.WHITE, (int) (30 * hoverA)));
                g2.fillRoundRect(PAD - 8, hy + 2, kanan - PAD + 16, ROW_H - 4, 12, 12);
                g2.setColor(alfa(BIRU, (int) (230 * hoverA)));
                g2.fillRoundRect(PAD - 8, hy + 10, 3, ROW_H - 20, 3, 3);
            }

            // ---------- baris data ----------
            for (int i = 0; i < data.size(); i++) {
                Baris d = data.get(i);
                int y = y0 + i * ROW_H;
                double t = mentah(i);
                if (t <= 0) {
                    continue;
                }
                double p = easeOut(t);
                java.awt.Composite oldC = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) p));
                g2.translate((1 - p) * 28, 0);

                if (rank[i] == 1) {
                    g2.setPaint(new LinearGradientPaint(PAD - 8, 0, kanan + 8, 0, new float[]{0f, 0.6f, 1f},
                            new Color[]{alfa(EMAS, 46), alfa(EMAS, 12), alfa(EMAS, 0)}));
                    g2.fillRoundRect(PAD - 8, y + 2, kanan - PAD + 16, ROW_H - 4, 12, 12);
                } else if (i % 2 == 0) {
                    g2.setColor(new Color(255, 255, 255, 9));
                    g2.fillRoundRect(PAD - 8, y + 2, kanan - PAD + 16, ROW_H - 4, 12, 12);
                }
                int cy = y + ROW_H / 2;
                int base = cy + 5;

                // lencana peringkat (muncul dengan efek memantul + cahaya untuk 3 besar)
                int cx = xs[0] + W_NO / 2;
                int dia = Math.max(2, (int) Math.round(28 * Math.max(0.01, easeBack(t))));
                int bx = cx - dia / 2, by = cy - dia / 2;
                if (i < 3) {
                    Color mc = medali(i + 1);
                    g2.setPaint(new RadialGradientPaint(cx, cy, 24f, new float[]{0f, 1f},
                            new Color[]{alfa(mc, 80), alfa(mc, 0)}));
                    g2.fillOval(cx - 24, cy - 24, 48, 48);
                    g2.setPaint(new GradientPaint(bx, by, mc.brighter(), bx, by + dia, mc.darker()));
                } else {
                    g2.setColor(new Color(0x2B, 0x3A, 0x5C));
                }
                g2.fillOval(bx, by, dia, dia);
                if (i >= 3) {
                    g2.setColor(new Color(255, 255, 255, 50));
                    g2.drawOval(bx, by, dia, dia);
                }
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(i < 3 ? new Color(0x1A, 0x1A, 0x2E) : TEKS);
                tengah(g2, String.valueOf(i + 1), xs[0], W_NO, cy + 5);

                // kode ICD-10 + nama penyakit
                g2.setFont(F_TEBAL);
                g2.setColor(BIRU);
                tengah(g2, d.kode, xs[1], W_ICD, base);
                g2.setFont(rank[i] == 1 ? F_TEBAL : F_ISI);
                g2.setColor(TEKS);
                g2.drawString(potong(d.nama, g2.getFontMetrics(), diagW - 16), xs[2] + 8, base);

                // angka (rata tengah), menghitung naik saat animasi; kolom "mati" merah bila > 0
                int[] nil = {d.lk, d.pr, d.total(), d.matiLk, d.matiPr, d.mati()};
                for (int k = 0; k < 6; k++) {
                    boolean mati = k >= 3;
                    boolean total = (k == 2 || k == 5);
                    g2.setFont(total || (mati && nil[k] > 0) ? F_TEBAL : F_ISI);
                    if (mati) {
                        g2.setColor(nil[k] > 0 ? MERAH : alfa(TEKS_REDUP, 150));
                    } else if (k == 2) {
                        g2.setColor(rank[i] == 1 ? EMAS : Color.WHITE);
                    } else {
                        g2.setColor(TEKS);
                    }
                    tengah(g2, rib(Math.round(nil[k] * (statis ? 1.0 : p))), xs[3 + k], numW, base);
                }

                // bilah mini di bawah angka Total (skala terhadap penyakit terbanyak; tumbuh saat animasi)
                int bxl = xs[5] + 16, bwMax = numW - 32;
                int bwTot = Math.max(3, (int) Math.round(bwMax * d.total() / (double) max));
                int bwAnim = (int) Math.round(bwTot * p);
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillRoundRect(bxl, y + ROW_H - 9, bwMax, 3, 3, 3);
                if (bwAnim >= 2) {
                    g2.setPaint(new GradientPaint(bxl, 0, new Color(0xFF, 0xE2, 0x7A), bxl + bwTot, 0, new Color(0xE5, 0xA8, 0x00)));
                    g2.fillRoundRect(bxl, y + ROW_H - 9, bwAnim, 3, 3, 3);
                }

                g2.translate(-(1 - p) * 28, 0);
                g2.setComposite(oldC);
            }

            if (data.isEmpty() && !memuat) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                g2.setColor(TEKS_REDUP);
                tengah(g2, "Tidak ada data pasien rawat inap yang keluar pada bulan ini.", 0, w, y0 + 2 * ROW_H);
            }

            // garis pemisah halus antar kolom pada isi
            g2.setColor(new Color(255, 255, 255, 14));
            for (int k : new int[]{1, 2, 3, 6}) {
                g2.drawLine(xs[k], y0 + 4, xs[k], tabelBawah - 4);
            }
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawLine(PAD - 8, tabelBawah, kanan + 8, tabelBawah);

            // ---------- lapisan "Memuat data" ----------
            if (memuat) {
                g2.setColor(alfa(BG1, 175));
                g2.fillRect(0, 0, w, h);
                long now = System.currentTimeMillis();
                int pw = 200, ph = 44;
                int px = (w - pw) / 2, py = y0 + 2 * ROW_H;
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

        // --- Scrollable ---
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return ROW_H;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return Math.max(60, r.height - 60);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport
                    && ((JViewport) getParent()).getHeight() > getPreferredSize().height;
        }
    }

    // ------------------------------------------------------------------
    private final Pemuat pemuat;
    private TabelPanel tabel;
    private HeaderPanel header;
    private List<Baris> data = new ArrayList<>();
    private String periodeExport = "";   // mis. "BULAN OKTOBER 2026" (judul di Excel/PDF)
    private String kodePeriode = "";     // mis. "202610" (untuk nama file)
    private int bulan, tahun;
    private int urutan = 0;
    private final JPanel areaHeader = new JPanel(new BorderLayout());
    private final JScrollPane sp;
    private final PemilihPeriode periode = new PemilihPeriode(this::atur);
    private final Tombol btnReplay = new Tombol("Ulangi Animasi", BIRU);
    private final Tombol btnPng = new Tombol("Simpan PNG", ORANYE);
    private final Tombol btnExcel = new Tombol("Export Excel", HIJAU);
    private final Tombol btnPdf = new Tombol("Save As PDF", MERAH);

    private RekapPenyakitRanap(Window owner, Pemuat pemuat, int bulan, int tahun) {
        super(owner, "Rekap 10 Besar Penyakit Rawat Inap", Dialog.ModalityType.APPLICATION_MODAL);
        this.pemuat = pemuat;
        LocalDate hari = LocalDate.now();
        int t = Math.max(2000, Math.min(tahun, hari.getYear()));
        int b = Math.max(1, Math.min(bulan, 12));
        if (t == hari.getYear() && b > hari.getMonthValue()) {
            b = hari.getMonthValue();
        }
        this.tahun = t;
        this.bulan = b;
        this.tabel = new TabelPanel(data);
        this.tabel.setMemuat(true);

        setLayout(new BorderLayout());
        header = new HeaderPanel(NAMA_BULAN[b - 1] + " " + t, data);
        areaHeader.add(header, BorderLayout.CENTER);
        JPanel utara = new JPanel(new BorderLayout());
        utara.add(areaHeader, BorderLayout.CENTER);
        utara.add(periode, BorderLayout.SOUTH);
        add(utara, BorderLayout.NORTH);
        periode.setPilihan(b, t);

        sp = new JScrollPane(tabel);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(TabelPanel.ROW_H);
        add(sp, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(0x0F, 0x1A, 0x33));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 22)),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        JLabel hint = new JLabel("Arahkan kursor ke baris untuk nama lengkap  \u2022  \u2190 \u2192 ganti bulan  \u2022  Esc: tutup");
        hint.setForeground(TEKS_REDUP);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.add(hint, BorderLayout.WEST);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        tombol.setOpaque(false);
        Tombol btnTutup = new Tombol("Tutup", new Color(0x8F, 0xA6, 0xBD));
        btnReplay.addActionListener(e -> {
            tabel.mulaiAnimasi();
            header.mulaiAnimasi();
        });
        btnPng.addActionListener(e -> simpanPng());
        btnExcel.addActionListener(e -> eksporExcel());
        btnPdf.addActionListener(e -> simpanPdf());
        btnTutup.addActionListener(e -> dispose());
        tombol.add(btnReplay);
        tombol.add(btnPng);
        tombol.add(btnExcel);
        tombol.add(btnPdf);
        tombol.add(btnTutup);
        footer.add(tombol, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> geser(-1),
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> geser(1),
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                atur(RekapPenyakitRanap.this.bulan, RekapPenyakitRanap.this.tahun);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                tabel.hentikan();
                header.hentikan();
                periode.hentikan();
            }
        });
        setKontrolAktif(false);

        Dimension layar = Toolkit.getDefaultToolkit().getScreenSize();
        int tinggi = 150 + PemilihPeriode.TINGGI + (TabelPanel.PAD_ATAS + TabelPanel.H_GRUP + TabelPanel.H_SUB
                + 10 * TabelPanel.ROW_H + TabelPanel.BOTTOM) + 62 + 34;
        tinggi = Math.max(480, Math.min(tinggi, layar.height - 90));
        setSize(Math.min(1120, layar.width - 40), tinggi);
        setLocationRelativeTo(owner);
    }

    // ------------------------------------------------------------------
    //  Pilihan bulan / tahun
    // ------------------------------------------------------------------
    private void geser(int delta) {
        if (!periode.isAktif() || (delta < 0 ? !periode.bisaMundur() : !periode.bisaMaju())) {
            return;
        }
        LocalDate t = LocalDate.of(tahun, bulan, 1).plusMonths(delta);
        atur(t.getMonthValue(), t.getYear());
    }

    /** Terapkan bulan/tahun (dibatasi 2000 s.d. bulan ini), segarkan kontrol, lalu muat datanya. */
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
        muatData();
    }

    private void setKontrolAktif(boolean aktif) {
        periode.setAktif(aktif);
        periode.setMemuat(!aktif);
        boolean ada = aktif && !data.isEmpty();
        btnReplay.setEnabled(ada);
        btnPng.setEnabled(ada);
        btnExcel.setEnabled(ada);
        btnPdf.setEnabled(ada);
    }

    private void muatData() {
        final int seq = ++urutan;
        final int b = bulan, t = tahun;
        final LocalDate dari = LocalDate.of(t, b, 1);
        final LocalDate sampai = dari.withDayOfMonth(dari.lengthOfMonth());
        setKontrolAktif(false);
        tabel.setMemuat(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<List<Baris>, Void>() {
            @Override
            protected List<Baris> doInBackground() throws Exception {
                return pemuat.muat(dari, sampai);
            }

            @Override
            protected void done() {
                if (seq != urutan) {
                    return;
                }
                setCursor(Cursor.getDefaultCursor());
                List<Baris> hasil = new ArrayList<>();
                try {
                    List<Baris> g = get();
                    if (g != null) {
                        hasil = g;
                    }
                } catch (Exception ex) {
                    Throwable c = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(RekapPenyakitRanap.this,
                            "Gagal mengambil data rekap:\n" + c.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                terapkan(hasil, b, t);
                setKontrolAktif(true);
            }
        }.execute();
    }

    /** Tampilkan data hasil muat (urut: pasien terbanyak -> tersedikit; sama banyak: urut kode). */
    private void terapkan(List<Baris> hasil, int b, int t) {
        data = new ArrayList<>(hasil);
        data.sort((x, y) -> x.total() != y.total() ? Integer.compare(y.total(), x.total()) : x.kode.compareToIgnoreCase(y.kode));
        periodeExport = "BULAN " + NAMA_BULAN[b - 1].toUpperCase(Locale.ROOT) + " " + t;
        kodePeriode = String.format(Locale.ROOT, "%04d%02d", t, b);
        tabel.hentikan();
        header.hentikan();
        tabel = new TabelPanel(data);
        sp.setViewportView(tabel);
        header = new HeaderPanel(NAMA_BULAN[b - 1] + " " + t, data);
        areaHeader.removeAll();
        areaHeader.add(header, BorderLayout.CENTER);
        areaHeader.revalidate();
        areaHeader.repaint();
        sp.getVerticalScrollBar().setValue(0);
        tabel.mulaiAnimasi();
        header.mulaiAnimasi();
    }

    /** Gambar lengkap (header + tabel) tanpa animasi; aman dipakai tanpa jendela terbuka. */
    static BufferedImage buatGambar(List<Baris> hs, String periode, int lebar) {
        TabelPanel tb = new TabelPanel(hs);
        tb.setStatis(true);
        HeaderPanel hd = new HeaderPanel(periode, hs);
        int tinggiTabel = tb.getPreferredSize().height;
        int tinggi = 150 + tinggiTabel;
        hd.setBounds(0, 0, lebar, 150);
        tb.setBounds(0, 150, lebar, tinggiTabel);
        BufferedImage img = new BufferedImage(lebar, tinggi, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        hd.paint(g);
        g.translate(0, 150);
        tb.paint(g);
        g.dispose();
        return img;
    }

    // ------------------------------------------------------------------
    //  Export Excel / Save As PDF
    // ------------------------------------------------------------------
    private java.io.File pilihFile(String judulDialog, String ext, String deskripsi) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(judulDialog);
        fc.setSelectedFile(new java.io.File(AWALAN_FILE + kodePeriode + "_"
                + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "." + ext));
        fc.setFileFilter(new FileNameExtensionFilter(deskripsi, ext));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        java.io.File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase(Locale.ROOT).endsWith("." + ext)) {
            f = new java.io.File(f.getParentFile(), f.getName() + "." + ext);
        }
        if (f.exists() && JOptionPane.showConfirmDialog(this, "File sudah ada. Timpa?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return null;
        }
        return f;
    }

    private void eksporExcel() {
        java.io.File f = pilihFile("Export Rekap ke Excel", "xlsx", "Excel (*.xlsx)");
        if (f == null) {
            return;
        }
        try {
            RekapEkspor.tulisXlsx(f, JUDUL, periodeExport, DASAR, GRUP1, GRUP2, kolomKode(), kolomNama(), kolomAngka());
            JOptionPane.showMessageDialog(this, "File Excel berhasil disimpan:\n" + f.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan Excel:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simpanPdf() {
        java.io.File f = pilihFile("Simpan Rekap sebagai PDF", "pdf", "Dokumen PDF (*.pdf)");
        if (f == null) {
            return;
        }
        try {
            RekapEkspor.tulisPdf(f, JUDUL, periodeExport, DASAR, GRUP1, GRUP2, kolomKode(), kolomNama(), kolomAngka());
            JOptionPane.showMessageDialog(this, "File PDF berhasil disimpan:\n" + f.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan PDF:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String[] kolomKode() {
        String[] r = new String[data.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = data.get(i).kode;
        }
        return r;
    }

    private String[] kolomNama() {
        String[] r = new String[data.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = data.get(i).nama;
        }
        return r;
    }

    private int[][] kolomAngka() {
        int[][] r = new int[data.size()][];
        for (int i = 0; i < r.length; i++) {
            Baris b = data.get(i);
            r[i] = new int[]{b.lk, b.pr, b.total(), b.matiLk, b.matiPr, b.mati()};
        }
        return r;
    }

    private int bulanData() {
        return Integer.parseInt(kodePeriode.substring(4));
    }

    private int tahunData() {
        return Integer.parseInt(kodePeriode.substring(0, 4));
    }

    private void simpanPng() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Rekap sebagai Gambar");
        fc.setSelectedFile(new java.io.File(AWALAN_FILE + kodePeriode + "_"
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
            javax.imageio.ImageIO.write(buatGambar(data, NAMA_BULAN[bulanData() - 1] + " " + tahunData(), Math.max(1000, getWidth())), "png", f);
            JOptionPane.showMessageDialog(this, "Gambar berhasil disimpan:\n" + f.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan gambar:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
