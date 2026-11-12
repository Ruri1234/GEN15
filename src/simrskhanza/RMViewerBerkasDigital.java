package simrskhanza;

import fungsi.koneksiDB;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * Viewer Berkas Digital Perawatan (hanya melihat berkas yang sudah di-input).
 * Daftar berkas di kiri, pratinjau di kanan. Gambar bisa di-zoom/geser/putar,
 * PDF ditampilkan jika pustaka PDFBox tersedia di proyek (jika tidak, dibuka dengan aplikasi bawaan).
 *
 * Tampilan: kartu membulat, tombol berikon dengan animasi hover, daftar yang muncul bertahap,
 * zoom halus mengikuti kursor, transisi fade/putar pada pratinjau, serta animasi buka/tutup jendela.
 */
public final class RMViewerBerkasDigital extends JDialog {
    private final PanelViewer panel;
    private Point awalDrag;
    private boolean fadeOk = false;
    private boolean menutup = false;
    private javax.swing.Timer tmMasuk, tmKeluar;

    public RMViewerBerkasDigital(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        panel = new PanelViewer(new PenyediaDB(), new Runnable() {
            @Override public void run() { tutupAnimasi(); }
        });
        panel.setBorder(BorderFactory.createLineBorder(Warna.NAVY, 1));
        setContentPane(panel);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        try {
            setOpacity(0f);
            fadeOk = true;
        } catch (Throwable e) {
            fadeOk = false;
        }
        MouseAdapter geser = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { awalDrag = e.getPoint(); }
            @Override public void mouseDragged(MouseEvent e) {
                if (awalDrag != null) {
                    Point p = getLocation();
                    setLocation(p.x + e.getX() - awalDrag.x, p.y + e.getY() - awalDrag.y);
                }
            }
        };
        panel.header.addMouseListener(geser);
        panel.header.addMouseMotionListener(geser);
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { animasiMasuk(); }
            @Override public void windowClosed(WindowEvent e) { panel.bersihkan(); }
        });
    }

    /** Isi data pasien lalu muat daftar berkas digital. */
    public void setData(String noRawat, String noRM, String namaPasien) {
        panel.setPasien(noRawat, noRM, namaPasien);
        panel.muat();
    }

    // ============================================================ animasi jendela

    private void animasiMasuk() {
        if (!fadeOk) return;
        final Point dasar = getLocation();
        final long mulai = System.currentTimeMillis();
        setLocation(dasar.x, dasar.y + 18);
        tmMasuk = new javax.swing.Timer(15, null);
        tmMasuk.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                float p = ease((System.currentTimeMillis() - mulai) / 260f);
                try { setOpacity(Math.min(1f, Math.max(0f, p))); } catch (Throwable x) { }
                setLocation(dasar.x, dasar.y + Math.round(18 * (1 - p)));
                if (p >= 1f) {
                    tmMasuk.stop();
                    try { setOpacity(1f); } catch (Throwable x) { }
                    setLocation(dasar);
                }
            }
        });
        tmMasuk.start();
    }

    private void tutupAnimasi() {
        if (menutup) return;
        menutup = true;
        if (!fadeOk) { dispose(); return; }
        if (tmMasuk != null) tmMasuk.stop();
        final float awal = Math.max(0.2f, getOpacity());
        final long mulai = System.currentTimeMillis();
        tmKeluar = new javax.swing.Timer(15, null);
        tmKeluar.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                float p = Math.min(1f, (System.currentTimeMillis() - mulai) / 170f);
                try { setOpacity(Math.max(0f, awal * (1 - p))); } catch (Throwable x) { }
                if (p >= 1f) {
                    tmKeluar.stop();
                    dispose();
                }
            }
        });
        tmKeluar.start();
    }

    // ============================================================ data

    static final class Berkas {
        final String noRawat, kode, nama, lokasi, tgl, jam, ext;
        Berkas(String noRawat, String kode, String nama, String lokasi, String tgl, String jam) {
            this.noRawat = noRawat == null ? "" : noRawat;
            this.kode = kode == null ? "" : kode;
            this.nama = nama == null ? "" : nama;
            this.lokasi = lokasi == null ? "" : lokasi;
            this.tgl = tgl == null ? "" : tgl;
            this.jam = jam == null ? "" : jam;
            this.ext = ekstensi(this.lokasi);
        }
        boolean gambar() { return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("gif") || ext.equals("bmp"); }
        boolean pdf() { return ext.equals("pdf"); }
        String namaFile() { int i = lokasi.lastIndexOf('/'); return i >= 0 ? lokasi.substring(i + 1) : lokasi; }
        String judulKunjungan() { return tglIndo(tgl) + (jam.isEmpty() ? "" : " " + jam.substring(0, Math.min(5, jam.length()))) + "   \u00B7   " + noRawat; }
        String labelExt() { String e = ext.isEmpty() ? "FILE" : ext.toUpperCase(); return e.length() > 4 ? e.substring(0, 4) : e; }
    }

    interface Penyedia {
        List<Berkas> muat(boolean semuaKunjungan, String noRawat, String noRM) throws Exception;
        String urlDasar();
    }

    /** Pengambil data dari database SIMRS (tabel berkas_digital_perawatan + master_berkas_digital). */
    static final class PenyediaDB implements Penyedia {
        @Override public List<Berkas> muat(boolean semua, String noRawat, String noRM) throws Exception {
            List<Berkas> hasil = new ArrayList<Berkas>();
            String sql = "select b.no_rawat,b.kode,ifnull(m.nama,b.kode) as nama,b.lokasi_file,r.tgl_registrasi,r.jam_reg "
                    + "from berkas_digital_perawatan b "
                    + "left join master_berkas_digital m on b.kode=m.kode "
                    + "inner join reg_periksa r on b.no_rawat=r.no_rawat "
                    + "where " + (semua ? "r.no_rkm_medis=?" : "b.no_rawat=?") + " "
                    + "order by r.tgl_registrasi desc,r.jam_reg desc,b.no_rawat desc,m.nama,b.lokasi_file";
            PreparedStatement ps = koneksiDB.condb().prepareStatement(sql);
            try {
                ps.setString(1, semua ? noRM : noRawat);
                ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        hasil.add(new Berkas(rs.getString("no_rawat"), rs.getString("kode"), rs.getString("nama"),
                                rs.getString("lokasi_file"), rs.getString("tgl_registrasi"), rs.getString("jam_reg")));
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
            return hasil;
        }
        @Override public String urlDasar() {
            return "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/berkasrawat/";
        }
    }

    // ============================================================ util

    static String ekstensi(String f) {
        if (f == null) return "";
        int q = f.indexOf('?');
        if (q >= 0) f = f.substring(0, q);
        int i = f.lastIndexOf('.');
        return (i >= 0 && i < f.length() - 1) ? f.substring(i + 1).toLowerCase() : "";
    }

    static String tglIndo(String t) {
        if (t != null && t.length() >= 10 && t.charAt(4) == '-') {
            return t.substring(8, 10) + "-" + t.substring(5, 7) + "-" + t.substring(0, 4);
        }
        return t == null ? "" : t;
    }

    static Font font(int gaya, float ukuran) {
        return new Font("Segoe UI", gaya, 12).deriveFont(gaya, ukuran);
    }

    static String potong(String s, FontMetrics fm, int lebar) {
        if (s == null) return "";
        if (fm.stringWidth(s) <= lebar) return s;
        String t = s;
        while (t.length() > 1 && fm.stringWidth(t + "\u2026") > lebar) t = t.substring(0, t.length() - 1);
        return t + "\u2026";
    }

    static void kualitas(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /** Easing keluar (cepat di awal, melambat di akhir). Masukan 0..1. */
    static float ease(float t) {
        t = Math.max(0f, Math.min(1f, t));
        float u = 1 - t;
        return 1 - u * u * u;
    }

    static Color mix(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    static Color alfa(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    static final class Warna {
        static final Color NAVY_GELAP = new Color(15, 42, 74);
        static final Color NAVY = new Color(29, 65, 112);
        static final Color TOSCA = new Color(20, 184, 166);
        static final Color TOSCA_GELAP = new Color(13, 148, 136);
        static final Color BIRU = new Color(37, 99, 235);
        static final Color LANGIT = new Color(56, 189, 248);
        static final Color MERAH = new Color(220, 38, 38);
        static final Color ABU_TUA = new Color(100, 116, 139);
        static final Color TEKS = new Color(15, 23, 42);
        static final Color LATAR = new Color(241, 245, 249);
        static final Color GARIS = new Color(226, 232, 240);
        static final Color KANVAS = new Color(232, 237, 243);
        static final Color PILIH = new Color(230, 244, 245);

        static Color jenis(Berkas b) {
            if (b.pdf()) return new Color(220, 38, 38);
            if (b.gambar()) return new Color(37, 99, 235);
            return new Color(100, 116, 139);
        }
    }

    /** Pembantu transisi hover: nilai() bergerak halus 0..1 menuju target. */
    static final class Hover {
        private float p = 0f;
        private boolean target;
        private final JComponent c;
        private javax.swing.Timer t;
        Hover(JComponent comp) {
            c = comp;
            t = new javax.swing.Timer(15, new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    float tujuan = target ? 1f : 0f;
                    p += (tujuan - p) * 0.28f;
                    if (Math.abs(tujuan - p) < 0.01f) { p = tujuan; t.stop(); }
                    c.repaint();
                }
            });
        }
        void ke(boolean on) { target = on; if (!t.isRunning()) t.start(); }
        float nilai() { return p; }
    }

    // ============================================================ ikon garis

    static final int IK_NONE = 0, IK_PLUS = 1, IK_MINUS = 2, IK_PAS = 3, IK_PUTAR = 4, IK_BUKA = 5,
            IK_SIMPAN = 6, IK_SEGAR = 7, IK_PREV = 8, IK_NEXT = 9;

    /** Gambar ikon kecil (14x14) di (x,y) memakai warna c. */
    static void ikon(Graphics2D g0, int jenis, float x, float y, Color c) {
        Graphics2D g = (Graphics2D) g0.create();
        kualitas(g);
        g.translate(x, y);
        g.setColor(c);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (jenis) {
            case IK_PLUS:
                g.draw(new java.awt.geom.Line2D.Float(7, 2, 7, 12));
                g.draw(new java.awt.geom.Line2D.Float(2, 7, 12, 7));
                break;
            case IK_MINUS:
                g.draw(new java.awt.geom.Line2D.Float(2, 7, 12, 7));
                break;
            case IK_PAS: {
                Path2D p = new Path2D.Float();
                p.moveTo(1.5, 5); p.lineTo(1.5, 1.5); p.lineTo(5, 1.5);
                p.moveTo(9, 1.5); p.lineTo(12.5, 1.5); p.lineTo(12.5, 5);
                p.moveTo(12.5, 9); p.lineTo(12.5, 12.5); p.lineTo(9, 12.5);
                p.moveTo(5, 12.5); p.lineTo(1.5, 12.5); p.lineTo(1.5, 9);
                g.draw(p);
                break;
            }
            case IK_PUTAR:
            case IK_SEGAR: {
                if (jenis == IK_SEGAR) { g.translate(14, 0); g.scale(-1, 1); }
                g.draw(new Arc2D.Float(2, 2, 10, 10, 60, 280, Arc2D.OPEN));
                Path2D h = new Path2D.Float();
                h.moveTo(8.2, 0.6); h.lineTo(7.0, 3.6); h.lineTo(10.4, 3.9);
                g.fill(h);
                break;
            }
            case IK_BUKA: {
                Path2D p = new Path2D.Float();
                p.moveTo(6, 2.5); p.lineTo(3.5, 2.5); p.quadTo(1.8, 2.5, 1.8, 4.2); p.lineTo(1.8, 10.3);
                p.quadTo(1.8, 12, 3.5, 12); p.lineTo(9.8, 12); p.quadTo(11.5, 12, 11.5, 10.3); p.lineTo(11.5, 8);
                g.draw(p);
                g.draw(new java.awt.geom.Line2D.Float(6.5f, 7.5f, 12, 2));
                Path2D h = new Path2D.Float();
                h.moveTo(8, 1.4); h.lineTo(12.6, 1.4); h.lineTo(12.6, 6);
                g.draw(h);
                break;
            }
            case IK_SIMPAN: {
                g.draw(new java.awt.geom.Line2D.Float(7, 1.5f, 7, 9));
                Path2D h = new Path2D.Float();
                h.moveTo(3.6, 6); h.lineTo(7, 9.4); h.lineTo(10.4, 6);
                g.draw(h);
                Path2D t = new Path2D.Float();
                t.moveTo(2, 10.5); t.lineTo(2, 12.4); t.lineTo(12, 12.4); t.lineTo(12, 10.5);
                g.draw(t);
                break;
            }
            case IK_PREV: {
                Path2D p = new Path2D.Float();
                p.moveTo(9, 2); p.lineTo(4.5, 7); p.lineTo(9, 12);
                g.draw(p);
                break;
            }
            case IK_NEXT: {
                Path2D p = new Path2D.Float();
                p.moveTo(5, 2); p.lineTo(9.5, 7); p.lineTo(5, 12);
                g.draw(p);
                break;
            }
            default:
        }
        g.dispose();
    }

    // ============================================================ komponen kecil

    /** Tombol bergaya modern: ikon + teks, transisi hover halus, efek tekan. */
    static final class Tombol extends JButton {
        private final boolean utama;
        private final int jenisIkon;
        private final Hover hv;

        Tombol(String teks, int jenisIkon, boolean utama) {
            super(teks);
            this.utama = utama;
            this.jenisIkon = jenisIkon;
            hv = new Hover(this);
            setFont(font(Font.BOLD, 12f));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hv.ke(true); }
                @Override public void mouseExited(MouseEvent e) { hv.ke(false); repaint(); }
                @Override public void mousePressed(MouseEvent e) { repaint(); }
                @Override public void mouseReleased(MouseEvent e) { repaint(); }
            });
            FontMetrics fm = getFontMetrics(getFont());
            int lebar = 24;
            if (jenisIkon != IK_NONE) lebar += 14;
            if (!teks.isEmpty()) lebar += fm.stringWidth(teks) + (jenisIkon != IK_NONE ? 6 : 0);
            setPreferredSize(new Dimension(Math.max(lebar, 30), 30));
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            kualitas(g);
            boolean on = isEnabled();
            float h = on ? hv.nilai() : 0f;
            boolean tekan = on && getModel().isArmed() && getModel().isPressed();
            int w = getWidth(), ht = getHeight();
            if (tekan) g.translate(0, 1);
            Color fg;
            if (utama) {
                if (!on) {
                    g.setColor(new Color(203, 213, 225));
                    g.fill(new RoundRectangle2D.Float(0, 0, w, ht, 10, 10));
                    fg = Color.WHITE;
                } else {
                    if (h > 0.02f) {
                        g.setColor(new Color(20, 184, 166, Math.round(55 * h)));
                        g.fill(new RoundRectangle2D.Float(0, 2, w, ht, 10, 10));
                    }
                    Color a = mix(new Color(45, 212, 191), new Color(20, 184, 166), h);
                    Color b = mix(Warna.TOSCA, Warna.TOSCA_GELAP, h);
                    g.setPaint(new GradientPaint(0, 0, a, 0, ht, b));
                    g.fill(new RoundRectangle2D.Float(0, 0, w, ht - 1, 10, 10));
                    fg = Color.WHITE;
                }
            } else {
                g.setColor(mix(new Color(241, 245, 249), new Color(226, 238, 247), h));
                g.fill(new RoundRectangle2D.Float(0, 0, w, ht - 1, 10, 10));
                g.setColor(mix(Warna.GARIS, alfa(Warna.TOSCA, 190), h));
                g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1.5f, ht - 2.5f, 10, 10));
                fg = on ? mix(new Color(51, 65, 85), Warna.NAVY, h) : new Color(148, 163, 184);
            }
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            String t = getText();
            int isi = (jenisIkon != IK_NONE ? 14 : 0) + (t.isEmpty() ? 0 : fm.stringWidth(t) + (jenisIkon != IK_NONE ? 6 : 0));
            float x = (w - isi) / 2f;
            if (jenisIkon != IK_NONE) {
                ikon(g, jenisIkon, x, (ht - 1 - 14) / 2f, fg);
                x += 20;
            }
            if (!t.isEmpty()) {
                g.setColor(fg);
                g.drawString(t, x, (ht - 1 - fm.getHeight()) / 2f + fm.getAscent());
            }
            g.dispose();
        }
    }

    static Tombol tombol(String teks, int ikon, boolean utama, String tip) {
        Tombol b = new Tombol(teks, ikon, utama);
        if (tip != null) b.setToolTipText(tip);
        return b;
    }

    static JLabel pil(String teks, Color latar, Color huruf) {
        final Color bg = latar;
        JLabel l = new JLabel(teks, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                kualitas(g);
                g.setColor(bg);
                g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g.dispose();
                super.paintComponent(g0);
            }
        };
        l.setOpaque(false);
        l.setForeground(huruf);
        l.setFont(font(Font.BOLD, 11f));
        FontMetrics fm = l.getFontMetrics(l.getFont());
        l.setPreferredSize(new Dimension(fm.stringWidth(teks) + 22, 22));
        return l;
    }

    /** Kartu putih bersudut bulat; isinya dipotong mengikuti sudut. */
    static final class Kartu extends JPanel {
        private final int r;
        Kartu(LayoutManager lm, int r) {
            super(lm);
            this.r = r;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            kualitas(g);
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), r, r));
            g.dispose();
        }
        @Override public void paint(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            kualitas(g);
            g.clip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), r, r));
            super.paint(g);
            g.dispose();
            Graphics2D b = (Graphics2D) g0.create();
            kualitas(b);
            b.setColor(Warna.GARIS);
            b.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, r, r));
            b.dispose();
        }
    }

    /** Pemilih dua pilihan bergaya segmented-control; penanda bergeser halus. */
    static final class Segmented extends JComponent {
        private final String[] label;
        private int pilih = 0;
        private final IntConsumer cb;
        private final int lebarSeg;
        private float xThumb = 4f;
        private javax.swing.Timer tm;

        Segmented(String[] label, IntConsumer cb) {
            this.label = label;
            this.cb = cb;
            FontMetrics fm = getFontMetrics(font(Font.BOLD, 12f));
            int m = 0;
            for (String s : label) m = Math.max(m, fm.stringWidth(s));
            lebarSeg = m + 30;
            setPreferredSize(new Dimension(lebarSeg * label.length + 8, 34));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tm = new javax.swing.Timer(15, new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    float tujuan = 4 + Segmented.this.pilih * Segmented.this.lebarSeg;
                    xThumb += (tujuan - xThumb) * 0.30f;
                    if (Math.abs(tujuan - xThumb) < 0.5f) { xThumb = tujuan; tm.stop(); }
                    repaint();
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    int i = (e.getX() - 4) / Segmented.this.lebarSeg;
                    if (i >= 0 && i < Segmented.this.label.length && i != Segmented.this.pilih) {
                        Segmented.this.pilih = i;
                        if (!tm.isRunning()) tm.start();
                        Segmented.this.cb.accept(i);
                    }
                }
            });
        }

        void setPilih(int i) {
            pilih = i;
            xThumb = 4 + i * lebarSeg;
            repaint();
        }

        @Override public void removeNotify() { tm.stop(); super.removeNotify(); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            kualitas(g);
            g.setColor(new Color(226, 232, 240));
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            g.setColor(new Color(15, 23, 42, 22));
            g.fill(new RoundRectangle2D.Float(xThumb, 5, lebarSeg, getHeight() - 8, 10, 10));
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(xThumb, 4, lebarSeg, getHeight() - 8, 10, 10));
            g.setColor(Warna.TOSCA);
            g.fill(new RoundRectangle2D.Float(xThumb + lebarSeg / 2f - 10, getHeight() - 8, 20, 3, 3, 3));
            g.setFont(font(Font.BOLD, 12f));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < label.length; i++) {
                int x = 4 + i * lebarSeg;
                float dekat = 1f - Math.min(1f, Math.abs(xThumb - x) / lebarSeg);
                g.setColor(mix(Warna.ABU_TUA, Warna.NAVY, dekat));
                g.drawString(label[i], x + (lebarSeg - fm.stringWidth(label[i])) / 2, (getHeight() - 2 - fm.getHeight()) / 2 + fm.getAscent());
            }
            g.dispose();
        }
    }

    /** Kolom pencarian bersudut bulat: placeholder, sorotan fokus beranimasi, tombol hapus. */
    static final class KolomCari extends JTextField {
        private final Hover fokus;
        private boolean diatasHapus = false;

        KolomCari() {
            fokus = new Hover(this);
            setOpaque(false);
            setFont(font(Font.PLAIN, 12f));
            setForeground(Warna.TEKS);
            setCaretColor(Warna.NAVY);
            setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 30));
            setPreferredSize(new Dimension(240, 34));
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { fokus.ke(true); }
                @Override public void focusLost(FocusEvent e) { fokus.ke(false); }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    boolean d = !getText().isEmpty() && e.getX() > getWidth() - 28;
                    if (d != diatasHapus) {
                        diatasHapus = d;
                        setCursor(Cursor.getPredefinedCursor(d ? Cursor.HAND_CURSOR : Cursor.TEXT_CURSOR));
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (!getText().isEmpty() && e.getX() > getWidth() - 28) { setText(""); requestFocusInWindow(); }
                }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            kualitas(g);
            float h = fokus.nilai();
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            if (h > 0.01f) {
                g.setColor(new Color(20, 184, 166, Math.round(55 * h)));
                g.setStroke(new BasicStroke(3.5f));
                g.draw(new RoundRectangle2D.Float(1.5f, 1.5f, getWidth() - 3, getHeight() - 3, 11, 11));
            }
            g.setStroke(new BasicStroke(1f));
            g.setColor(mix(new Color(203, 213, 225), Warna.TOSCA, h));
            g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
            // kaca pembesar
            g.setColor(mix(new Color(148, 163, 184), Warna.TOSCA_GELAP, h));
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Float(11, 10, 10, 10));
            g.drawLine(19, 18, 23, 22);
            // tombol hapus
            if (!getText().isEmpty()) {
                int cx = getWidth() - 17, cy = getHeight() / 2;
                g.setColor(diatasHapus ? new Color(203, 213, 225) : new Color(226, 232, 240));
                g.fill(new Ellipse2D.Float(cx - 8, cy - 8, 16, 16));
                g.setColor(diatasHapus ? Warna.TEKS : Warna.ABU_TUA);
                g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(cx - 3, cy - 3, cx + 3, cy + 3);
                g.drawLine(cx - 3, cy + 3, cx + 3, cy - 3);
            }
            g.dispose();
            super.paintComponent(g0);
            if (getText().isEmpty()) {
                Graphics2D t = (Graphics2D) g0.create();
                kualitas(t);
                t.setFont(getFont());
                t.setColor(new Color(148, 163, 184));
                FontMetrics fm = t.getFontMetrics();
                t.drawString("Cari nama / jenis berkas...", 32, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                t.dispose();
            }
        }
    }

    static final class ScrollTipis extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(203, 213, 225);
            trackColor = new Color(0, 0, 0, 0);
        }
        @Override protected JButton createDecreaseButton(int o) { return nol(); }
        @Override protected JButton createIncreaseButton(int o) { return nol(); }
        private JButton nol() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }
        @Override protected void paintThumb(Graphics g0, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g = (Graphics2D) g0.create();
            kualitas(g);
            g.setColor(isDragging ? new Color(148, 163, 184) : isThumbRollover() ? new Color(176, 189, 205) : thumbColor);
            g.fill(new RoundRectangle2D.Float(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8));
            g.dispose();
        }
    }

    /** Titik status: hijau saat siap, kuning berdenyut saat memuat. */
    static final class Titik extends JComponent {
        private boolean sibuk = false;
        private javax.swing.Timer tm;
        Titik() {
            setPreferredSize(new Dimension(16, 16));
            tm = new javax.swing.Timer(30, new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) { repaint(); }
            });
        }
        void setSibuk(boolean b) {
            sibuk = b;
            if (b) tm.start(); else tm.stop();
            repaint();
        }
        @Override public void removeNotify() { tm.stop(); super.removeNotify(); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            kualitas(g);
            int cx = getWidth() / 2, cy = getHeight() / 2;
            Color c = sibuk ? new Color(245, 158, 11) : new Color(34, 197, 94);
            if (sibuk) {
                float ph = (System.currentTimeMillis() % 1000) / 1000f;
                g.setColor(alfa(c, Math.round(110 * (1 - ph))));
                float r = 4 + 4 * ph;
                g.fill(new Ellipse2D.Float(cx - r, cy - r, 2 * r, 2 * r));
            }
            g.setColor(c);
            g.fill(new Ellipse2D.Float(cx - 4, cy - 4, 8, 8));
            g.dispose();
        }
    }

    // ============================================================ header

    static final class Header extends JPanel {
        final JLabel judul = new JLabel("Berkas Digital Pasien");
        final JLabel sub = new JLabel(" ");
        final JPanel chip = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        private boolean sibuk = false;
        private javax.swing.Timer tm;

        Header(final Runnable tutup) {
            super(new BorderLayout(14, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(14, 18, 15, 14));
            tm = new javax.swing.Timer(20, new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) { repaint(0, getHeight() - 5, getWidth(), 5); }
            });
            JComponent ikon = new JComponent() {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    kualitas(g);
                    g.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 60), 0, 46, new Color(255, 255, 255, 22)));
                    g.fill(new RoundRectangle2D.Float(0, 0, 46, 46, 14, 14));
                    g.setColor(new Color(255, 255, 255, 50));
                    g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, 45, 45, 14, 14));
                    // dokumen belakang
                    g.setColor(new Color(255, 255, 255, 90));
                    g.fill(new RoundRectangle2D.Float(12, 8, 20, 28, 4, 4));
                    Path2D dok = new Path2D.Float();
                    dok.moveTo(16, 11); dok.lineTo(27, 11); dok.lineTo(34, 18); dok.lineTo(34, 37); dok.lineTo(16, 37); dok.closePath();
                    g.setColor(Color.WHITE);
                    g.fill(dok);
                    g.setColor(new Color(167, 243, 208));
                    g.fillPolygon(new int[]{27, 34, 27}, new int[]{11, 18, 18}, 3);
                    g.setColor(Warna.TOSCA);
                    g.fillRoundRect(20, 24, 10, 2, 2, 2);
                    g.fillRoundRect(20, 29, 10, 2, 2, 2);
                    g.dispose();
                }
            };
            ikon.setPreferredSize(new Dimension(46, 46));
            add(ikon, BorderLayout.WEST);

            judul.setFont(font(Font.BOLD, 18f));
            judul.setForeground(Color.WHITE);
            sub.setFont(font(Font.PLAIN, 12.5f));
            sub.setForeground(new Color(191, 210, 235));
            JPanel teks = new JPanel(new GridLayout(2, 1, 0, 1));
            teks.setOpaque(false);
            teks.add(judul);
            teks.add(sub);
            add(teks, BorderLayout.CENTER);

            chip.setOpaque(false);
            final JLabel x = new JLabel("\u2715", SwingConstants.CENTER) {
                private final Hover hover = new Hover(this);
                {
                    addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) { hover.ke(true); }
                        @Override public void mouseExited(MouseEvent e) { hover.ke(false); }
                    });
                }
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    kualitas(g);
                    float h = hover.nilai();
                    if (h > 0.01f) {
                        g.setColor(mix(new Color(255, 255, 255, 40), new Color(239, 68, 68, 230), h));
                        g.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                    }
                    g.dispose();
                    super.paintComponent(g0);
                }
            };
            x.setFont(font(Font.BOLD, 13f));
            x.setForeground(Color.WHITE);
            x.setPreferredSize(new Dimension(30, 30));
            x.setToolTipText("Tutup (Esc)");
            x.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            x.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { tutup.run(); }
            });
            JPanel kanan = new JPanel(new BorderLayout(10, 0));
            kanan.setOpaque(false);
            kanan.add(chip, BorderLayout.CENTER);
            kanan.add(x, BorderLayout.EAST);
            add(kanan, BorderLayout.EAST);
        }

        void setSibuk(boolean b) {
            sibuk = b;
            if (b) tm.start(); else tm.stop();
            repaint();
        }

        @Override public void removeNotify() { tm.stop(); super.removeNotify(); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            kualitas(g);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, Warna.NAVY_GELAP, w, 0, Warna.NAVY));
            g.fillRect(0, 0, w, h);
            // hiasan lingkaran lembut
            g.setColor(new Color(255, 255, 255, 12));
            g.fill(new Ellipse2D.Float(w - 190, -90, 260, 260));
            g.setColor(new Color(20, 184, 166, 28));
            g.fill(new Ellipse2D.Float(w - 330, -40, 150, 150));
            g.setColor(new Color(255, 255, 255, 8));
            g.fill(new Ellipse2D.Float(w * 0.42f, h - 40, 110, 110));
            // garis aksen bawah
            g.setPaint(new GradientPaint(0, 0, Warna.TOSCA, w, 0, Warna.LANGIT));
            g.fillRect(0, h - 3, w, 3);
            if (sibuk) {
                float ph = (System.currentTimeMillis() % 1100) / 1100f;
                float cx = -120 + ph * (w + 240);
                g.setPaint(new GradientPaint(cx - 120, 0, new Color(255, 255, 255, 0), cx, 0, new Color(255, 255, 255, 220)));
                g.fillRect((int) cx - 120, h - 3, 120, 3);
                g.setPaint(new GradientPaint(cx, 0, new Color(255, 255, 255, 220), cx + 120, 0, new Color(255, 255, 255, 0)));
                g.fillRect((int) cx, h - 3, 120, 3);
            }
            g.dispose();
        }
    }

    // ============================================================ daftar berkas

    static final class DaftarBerkas extends JList<Object> {
        String pesanKosong = "";
        private final Map<Integer, Float> hv = new HashMap<Integer, Float>();
        private final Map<Integer, Float> sl = new HashMap<Integer, Float>();
        private int hoverIdx = -1;
        private int sebelum = -1;
        private long mulai = 0;
        private javax.swing.Timer tm;
        static final int JEDA = 38, DURASI = 300, MAKS_URUT = 14;

        DaftarBerkas(DefaultListModel<Object> m) {
            super(m);
            tm = new javax.swing.Timer(15, new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) { langkah(); }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) { setHover(indeksDi(e.getPoint())); }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) { setHover(-1); }
            });
            addListSelectionListener(new ListSelectionListener() {
                @Override public void valueChanged(ListSelectionEvent e) {
                    int baru = getSelectedIndex();
                    if (sebelum >= 0 && !sl.containsKey(sebelum)) sl.put(sebelum, 1f);
                    if (baru >= 0 && !sl.containsKey(baru)) sl.put(baru, 0f);
                    sebelum = baru;
                    animasi();
                }
            });
        }

        private int indeksDi(Point p) {
            int i = locationToIndex(p);
            if (i < 0) return -1;
            Rectangle r = getCellBounds(i, i);
            if (r == null || !r.contains(p) || !(getModel().getElementAt(i) instanceof Berkas)) return -1;
            return i;
        }

        private void setHover(int i) {
            if (i == hoverIdx) return;
            hoverIdx = i;
            setCursor(Cursor.getPredefinedCursor(i >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            if (i >= 0 && !hv.containsKey(i)) hv.put(i, 0f);
            animasi();
        }

        void mulaiMasuk() {
            mulai = System.currentTimeMillis();
            hv.clear();
            sl.clear();
            sebelum = getSelectedIndex();
            animasi();
        }

        private void animasi() { if (!tm.isRunning()) tm.start(); }

        @Override public void removeNotify() { tm.stop(); super.removeNotify(); }

        private void langkah() {
            boolean lanjut = System.currentTimeMillis() - mulai < MAKS_URUT * JEDA + DURASI + 40;
            lanjut |= geser(hv, hoverIdx);
            lanjut |= geser(sl, getSelectedIndex());
            repaint();
            if (!lanjut) tm.stop();
        }

        private boolean geser(Map<Integer, Float> m, int tujuanIdx) {
            boolean lanjut = false;
            for (Integer k : new ArrayList<Integer>(m.keySet())) {
                float tuj = k.intValue() == tujuanIdx ? 1f : 0f;
                float v = m.get(k);
                v += (tuj - v) * 0.30f;
                if (Math.abs(tuj - v) < 0.02f) {
                    v = tuj;
                    if (tuj == 0f) { m.remove(k); continue; }
                } else {
                    lanjut = true;
                }
                m.put(k, v);
            }
            return lanjut;
        }

        float nilaiHover(int i) { Float v = hv.get(i); return v == null ? 0f : v; }
        float nilaiPilih(int i) {
            Float v = sl.get(i);
            if (v == null) return i == getSelectedIndex() ? 1f : 0f;
            return v;
        }
        float masuk(int i) {
            long dt = System.currentTimeMillis() - mulai - Math.min(i, MAKS_URUT) * (long) JEDA;
            if (dt < 0) return 0f;
            return ease(dt / (float) DURASI);
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            if (getModel().getSize() == 0 && !pesanKosong.isEmpty()) {
                Graphics2D g = (Graphics2D) g0.create();
                kualitas(g);
                String[] baris = pesanKosong.split("\n");
                g.setFont(font(Font.PLAIN, 12.5f));
                FontMetrics fm = g.getFontMetrics();
                int tot = baris.length * fm.getHeight();
                int cy = getHeight() / 2 - 40;
                // ilustrasi map kosong
                int cx = getWidth() / 2;
                g.setColor(new Color(226, 232, 240));
                g.fill(new RoundRectangle2D.Float(cx - 26, cy - 22, 52, 40, 8, 8));
                g.fill(new RoundRectangle2D.Float(cx - 26, cy - 28, 22, 10, 6, 6));
                g.setColor(new Color(203, 213, 225));
                g.fill(new RoundRectangle2D.Float(cx - 26, cy - 14, 52, 32, 8, 8));
                g.setColor(new Color(148, 163, 184));
                int y = cy + 44 + (fm.getHeight() - tot) / 2;
                for (String b : baris) {
                    g.drawString(b, (getWidth() - fm.stringWidth(b)) / 2, y += fm.getHeight());
                }
                g.dispose();
            }
        }
    }

    static final class BarisRenderer extends JComponent implements ListCellRenderer<Object> {
        private Object nilai;
        private float hov, sel, masuk = 1f;

        @Override public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            nilai = value;
            if (list instanceof DaftarBerkas) {
                DaftarBerkas d = (DaftarBerkas) list;
                hov = d.nilaiHover(index);
                sel = d.nilaiPilih(index);
                masuk = d.masuk(index);
            } else {
                hov = 0f;
                sel = isSelected ? 1f : 0f;
                masuk = 1f;
            }
            return this;
        }
        @Override public Dimension getPreferredSize() { return new Dimension(10, nilai instanceof Berkas ? 64 : 34); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            kualitas(g);
            int w = getWidth(), h = getHeight();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);
            if (masuk <= 0.001f) { g.dispose(); return; }
            g.setComposite(AlphaComposite.SrcOver.derive(masuk));
            g.translate((1 - masuk) * 22, 0);
            if (nilai instanceof Berkas) {
                Berkas b = (Berkas) nilai;
                // latar: hover lalu pilih
                g.setColor(mix(Color.WHITE, new Color(246, 249, 252), hov));
                g.fillRect(-30, 0, w + 30, h);
                if (sel > 0.01f) {
                    g.setPaint(new GradientPaint(0, 0, alfa(Warna.PILIH, Math.round(255 * sel)), w, 0, alfa(new Color(238, 246, 255), Math.round(255 * sel))));
                    g.fillRect(-30, 0, w + 30, h);
                }
                g.setColor(new Color(241, 245, 249));
                g.fillRect(16, h - 1, w - 32, 1);
                // aksen kiri yang tumbuh dari tengah
                if (sel > 0.01f) {
                    float tinggi = (h - 18) * sel;
                    g.setColor(Warna.TOSCA);
                    g.fill(new RoundRectangle2D.Float(-2, (h - tinggi) / 2f, 6, tinggi, 6, 6));
                }
                // ikon dokumen berwarna sesuai jenis
                Color wj = Warna.jenis(b);
                float sk = 1f + 0.06f * Math.max(hov, sel);
                AffineTransform at = g.getTransform();
                g.translate(35, h / 2f);
                g.scale(sk, sk);
                g.translate(-35, -h / 2f);
                float dx = 20, dy = (h - 40) / 2f;
                g.setColor(new Color(15, 23, 42, 18));
                g.fill(new RoundRectangle2D.Float(dx, dy + 2, 30, 40, 7, 7));
                Path2D dok = new Path2D.Float();
                dok.moveTo(dx + 6, dy); dok.lineTo(dx + 21, dy); dok.lineTo(dx + 30, dy + 9); dok.lineTo(dx + 30, dy + 34);
                dok.quadTo(dx + 30, dy + 40, dx + 24, dy + 40); dok.lineTo(dx + 6, dy + 40);
                dok.quadTo(dx, dy + 40, dx, dy + 34); dok.lineTo(dx, dy + 6); dok.quadTo(dx, dy, dx + 6, dy);
                g.setPaint(new GradientPaint(0, dy, mix(wj, Color.WHITE, 0.18f), 0, dy + 40, wj));
                g.fill(dok);
                g.setColor(new Color(255, 255, 255, 120));
                g.fillPolygon(new int[]{(int) dx + 21, (int) dx + 30, (int) dx + 21}, new int[]{(int) dy, (int) dy + 9, (int) dy + 9}, 3);
                g.setColor(Color.WHITE);
                g.setFont(font(Font.BOLD, 9.5f));
                FontMetrics fm = g.getFontMetrics();
                String e = b.labelExt();
                g.drawString(e, dx + (30 - fm.stringWidth(e)) / 2f, dy + 29);
                g.setTransform(at);
                // teks
                g.setColor(Warna.TEKS);
                g.setFont(font(Font.BOLD, 12.5f));
                fm = g.getFontMetrics();
                g.drawString(potong(b.nama, fm, w - 70 - 34), 66, h / 2 - 5);
                g.setColor(Warna.ABU_TUA);
                g.setFont(font(Font.PLAIN, 11f));
                fm = g.getFontMetrics();
                g.drawString(potong(b.namaFile(), fm, w - 70 - 34), 66, h / 2 + 12);
                // panah kanan muncul saat hover / terpilih
                float pa = Math.max(hov, sel);
                if (pa > 0.02f) {
                    g.setColor(alfa(sel > hov ? Warna.TOSCA_GELAP : Warna.ABU_TUA, Math.round(230 * pa)));
                    ikon(g, IK_NEXT, w - 28 - (1 - pa) * 8, (h - 14) / 2f, g.getColor());
                }
            } else {
                g.setColor(Warna.LATAR);
                g.fillRect(-30, 0, w + 30, h);
                g.setColor(Warna.GARIS);
                g.fillRect(-30, h - 1, w + 30, 1);
                g.setColor(alfa(Warna.TOSCA, 50));
                g.fill(new Ellipse2D.Float(14, h / 2f - 6, 12, 12));
                g.setColor(Warna.TOSCA);
                g.fill(new Ellipse2D.Float(17, h / 2f - 3, 6, 6));
                String teks = String.valueOf(nilai);
                boolean ini = teks.endsWith("\u0001");
                if (ini) teks = teks.substring(0, teks.length() - 1);
                g.setColor(Warna.NAVY);
                g.setFont(font(Font.BOLD, 11.5f));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(potong(teks, fm, w - 48 - (ini ? 84 : 0)), 34, (h - fm.getHeight()) / 2 + fm.getAscent());
                if (ini) {
                    g.setFont(font(Font.BOLD, 10f));
                    fm = g.getFontMetrics();
                    String t = "Kunjungan ini";
                    int pw = fm.stringWidth(t) + 14;
                    g.setColor(Warna.TOSCA);
                    g.fill(new RoundRectangle2D.Float(w - pw - 12, (h - 18) / 2f, pw, 18, 18, 18));
                    g.setColor(Color.WHITE);
                    g.drawString(t, w - pw - 12 + 7, (h - fm.getHeight()) / 2 + fm.getAscent());
                }
            }
            g.dispose();
        }
    }

    // ============================================================ kanvas pratinjau

    static final class Kanvas extends JComponent {
        static final int KOSONG = 0, MEMUAT = 1, GALAT = 2, GAMBAR = 3, TAKDUKUNG = 4;
        private int status = KOSONG;
        private BufferedImage gambar;
        private String judul = "", ket = "";
        private String badge = "";
        private boolean pas = true;
        private boolean kePas = false, pusatkan = false;
        private double zoom = 1, zoomTarget = 1;
        private double ox = 0, oy = 0;
        private double ancX = -1, ancY = -1;
        private Point tekan;
        private long tMasuk = 0, tPutar = 0, tInfo = 0;
        private final javax.swing.Timer anim;
        private final TexturePaint titik;
        Runnable padaZoom;

        Kanvas() {
            setOpaque(true);
            BufferedImage tx = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
            Graphics2D tg = tx.createGraphics();
            kualitas(tg);
            tg.setColor(new Color(148, 163, 184, 70));
            tg.fill(new Ellipse2D.Float(10, 10, 2.2f, 2.2f));
            tg.dispose();
            titik = new TexturePaint(tx, new Rectangle(0, 0, 22, 22));
            anim = new javax.swing.Timer(15, new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) { langkah(); }
            });
            MouseAdapter ma = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { tekan = e.getPoint(); }
                @Override public void mouseDragged(MouseEvent e) {
                    if (status == GAMBAR && tekan != null) {
                        ox += e.getX() - tekan.x;
                        oy += e.getY() - tekan.y;
                        tekan = e.getPoint();
                        pusatkan = false;
                        batasi();
                        repaint();
                    }
                }
                @Override public void mouseClicked(MouseEvent e) {
                    if (status == GAMBAR && e.getClickCount() == 2) {
                        if (pas) ukuranAsli(); else fitkan();
                    }
                }
                @Override public void mouseWheelMoved(MouseWheelEvent e) {
                    zoomBy(e.getWheelRotation() < 0 ? 1.18 : 1 / 1.18, e.getX(), e.getY());
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
            addMouseWheelListener(ma);
            addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) { batasi(); if (padaZoom != null) padaZoom.run(); }
            });
        }

        @Override public void removeNotify() { anim.stop(); super.removeNotify(); }

        int status() { return status; }
        void henti() { anim.stop(); }

        void kosong() { ubah(KOSONG, null, "", ""); }
        void memuat(String j) { ubah(MEMUAT, null, j, ""); }
        void galat(String j, String k) { ubah(GALAT, null, j, k); }
        void takDidukung(String badge, String j, String k) { this.badge = badge; ubah(TAKDUKUNG, null, j, k); }
        void tampil(BufferedImage img) {
            pas = true; kePas = false; pusatkan = false; ox = 0; oy = 0; zoomTarget = 1; tInfo = 0; tPutar = 0;
            ubah(GAMBAR, img, "", "");
        }

        private void ubah(int st, BufferedImage img, String j, String k) {
            status = st;
            gambar = img;
            judul = j;
            ket = k;
            tMasuk = System.currentTimeMillis();
            setCursor(Cursor.getPredefinedCursor(st == GAMBAR ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR));
            mulaiAnim();
            repaint();
            if (padaZoom != null) padaZoom.run();
        }

        private void mulaiAnim() { if (!anim.isRunning()) anim.start(); }

        double skala() {
            if (status != GAMBAR) return 1;
            return pas ? hitungPas() : zoom;
        }

        private double hitungPas() {
            double sw = (getWidth() - 48.0) / gambar.getWidth();
            double sh = (getHeight() - 48.0) / gambar.getHeight();
            return Math.max(0.02, Math.min(Math.min(sw, sh), 1.5));
        }

        /** Satu langkah animasi: zoom halus, pemusatan, transisi, dan elemen hidup di layar status. */
        private void langkah() {
            long now = System.currentTimeMillis();
            boolean lanjut = false;
            if (status == GAMBAR) {
                if (!pas) {
                    double zt = kePas ? hitungPas() : zoomTarget;
                    double lama = zoom;
                    zoom += (zt - zoom) * 0.26;
                    boolean zSelesai = Math.abs(zt - zoom) < zt * 0.002;
                    if (zSelesai) zoom = zt;
                    double rasio = zoom / lama;
                    double cx = getWidth() / 2.0, cy = getHeight() / 2.0;
                    double ax = ancX < 0 ? cx : ancX, ay = ancY < 0 ? cy : ancY;
                    ox = (ax - cx) - ((ax - cx) - ox) * rasio;
                    oy = (ay - cy) - ((ay - cy) - oy) * rasio;
                    boolean oSelesai = true;
                    if (pusatkan || kePas) {
                        ox += (0 - ox) * 0.26;
                        oy += (0 - oy) * 0.26;
                        if (Math.abs(ox) < 0.6) ox = 0;
                        if (Math.abs(oy) < 0.6) oy = 0;
                        oSelesai = ox == 0 && oy == 0;
                    }
                    if (zSelesai && oSelesai) {
                        if (kePas) { pas = true; kePas = false; ox = 0; oy = 0; }
                        pusatkan = false;
                    } else {
                        lanjut = true;
                    }
                    batasi();
                    if (padaZoom != null) padaZoom.run();
                }
                if (now - tMasuk < 420 || now - tPutar < 420) lanjut = true;
                if (tInfo > 0 && now - tInfo < 1550) lanjut = true;
            } else {
                lanjut = true;
            }
            repaint();
            if (!lanjut) anim.stop();
        }

        void zoomBy(double f) { zoomBy(f, -1, -1); }

        void zoomBy(double f, double ax, double ay) {
            if (status != GAMBAR) return;
            double dasar = pas ? skala() : (kePas ? zoom : zoomTarget);
            if (pas) { zoom = skala(); pas = false; }
            kePas = false;
            pusatkan = false;
            zoomTarget = Math.max(0.05, Math.min(8.0, dasar * f));
            ancX = ax;
            ancY = ay;
            tInfo = System.currentTimeMillis();
            mulaiAnim();
        }

        void fitkan() {
            if (status != GAMBAR) return;
            tInfo = System.currentTimeMillis();
            if (pas) { ox = 0; oy = 0; mulaiAnim(); repaint(); if (padaZoom != null) padaZoom.run(); return; }
            kePas = true;
            pusatkan = true;
            ancX = -1;
            ancY = -1;
            mulaiAnim();
        }

        void ukuranAsli() {
            if (status != GAMBAR) return;
            if (pas) { zoom = skala(); pas = false; }
            kePas = false;
            zoomTarget = 1;
            pusatkan = true;
            ancX = -1;
            ancY = -1;
            tInfo = System.currentTimeMillis();
            mulaiAnim();
        }

        void putar90() {
            if (status != GAMBAR) return;
            BufferedImage r = new BufferedImage(gambar.getHeight(), gambar.getWidth(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = r.createGraphics();
            g.translate(gambar.getHeight(), 0);
            g.rotate(Math.PI / 2);
            g.drawImage(gambar, 0, 0, null);
            g.dispose();
            gambar = r;
            tPutar = System.currentTimeMillis();
            if (pas) { ox = 0; oy = 0; } else { kePas = true; pusatkan = true; ancX = -1; ancY = -1; }
            mulaiAnim();
            repaint();
            if (padaZoom != null) padaZoom.run();
        }

        private void batasi() {
            if (status != GAMBAR) return;
            double s = skala();
            int dw = (int) Math.round(gambar.getWidth() * s), dh = (int) Math.round(gambar.getHeight() * s);
            double mx = Math.max(0, (dw - getWidth()) / 2.0 + 24), my = Math.max(0, (dh - getHeight()) / 2.0 + 24);
            ox = Math.max(-mx, Math.min(mx, ox));
            oy = Math.max(-my, Math.min(my, oy));
            if (dw + 48 <= getWidth()) ox = 0;
            if (dh + 48 <= getHeight()) oy = 0;
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            kualitas(g);
            long now = System.currentTimeMillis();
            g.setColor(Warna.KANVAS);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setPaint(titik);
            g.fillRect(0, 0, getWidth(), getHeight());
            int cx = getWidth() / 2, cy = getHeight() / 2;
            float pm = ease((now - tMasuk) / 320f);
            double ap = Math.sin(now / 520.0) * 4;
            switch (status) {
                case GAMBAR: {
                    batasi();
                    double s = skala();
                    int dw = (int) Math.round(gambar.getWidth() * s), dh = (int) Math.round(gambar.getHeight() * s);
                    int x = (int) Math.round((getWidth() - dw) / 2.0 + ox), y = (int) Math.round((getHeight() - dh) / 2.0 + oy);
                    Graphics2D h = (Graphics2D) g.create();
                    kualitas(h);
                    h.setComposite(AlphaComposite.SrcOver.derive(pm));
                    double pusX = x + dw / 2.0, pusY = y + dh / 2.0;
                    h.translate(pusX, pusY);
                    float pp = ease((now - tPutar) / 380f);
                    if (pp < 1f) h.rotate(Math.toRadians(-90 * (1 - pp)));
                    double sk = 0.94 + 0.06 * pm;
                    h.scale(sk, sk);
                    h.translate(-pusX, -pusY);
                    for (int i = 8; i >= 1; i--) {
                        h.setColor(new Color(15, 23, 42, 7));
                        h.fill(new RoundRectangle2D.Float(x - i + 1, y - i + 5, dw + 2 * i - 2, dh + 2 * i - 2, 8, 8));
                    }
                    h.setColor(Color.WHITE);
                    h.fillRect(x, y, dw, dh);
                    h.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            s >= 2 ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    h.drawImage(gambar, x, y, dw, dh, null);
                    h.dispose();
                    // lencana zoom melayang yang memudar sendiri
                    if (tInfo > 0) {
                        long dt = now - tInfo;
                        if (dt < 1550) {
                            float a = dt < 1000 ? 1f : (1550 - dt) / 550f;
                            float masuk = ease(dt / 160f);
                            String t = Math.round(s * 100) + "%";
                            g.setFont(font(Font.BOLD, 13f));
                            FontMetrics fm = g.getFontMetrics();
                            int pw = fm.stringWidth(t) + 30, ph = 30;
                            float px = (getWidth() - pw) / 2f, py = getHeight() - 52 + (1 - masuk) * 8;
                            g.setColor(new Color(15, 23, 42, Math.round(205 * a * masuk)));
                            g.fill(new RoundRectangle2D.Float(px, py, pw, ph, ph, ph));
                            g.setColor(new Color(255, 255, 255, Math.round(255 * a * masuk)));
                            g.drawString(t, px + 15, py + (ph - fm.getHeight()) / 2f + fm.getAscent());
                        }
                    }
                    break;
                }
                case MEMUAT: {
                    g.setComposite(AlphaComposite.SrcOver.derive(pm));
                    int lead = (int) ((now / 70) % 12);
                    g.setStroke(new BasicStroke(3.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    for (int k = 0; k < 12; k++) {
                        double a = Math.toRadians(k * 30 - 90);
                        float f = ((lead - k + 12) % 12) / 12f;
                        g.setColor(alfa(Warna.TOSCA, Math.round(255 * (1 - f) * (1 - f) + 22)));
                        g.drawLine((int) Math.round(cx + Math.cos(a) * 15), (int) Math.round(cy - 44 + Math.sin(a) * 15),
                                (int) Math.round(cx + Math.cos(a) * 26), (int) Math.round(cy - 44 + Math.sin(a) * 26));
                    }
                    String dasar = judul.isEmpty() ? "Memuat berkas" : judul.replace("...", "");
                    teksTengah(g, dasar + "...".substring(0, (int) ((now / 380) % 4)), "Mohon tunggu sebentar", cy + 22);
                    break;
                }
                case GALAT: {
                    g.setComposite(AlphaComposite.SrcOver.derive(pm));
                    float ph = (now % 1700) / 1700f;
                    float rr = 30 + 18 * ph;
                    g.setColor(alfa(Warna.MERAH, Math.round(60 * (1 - ph))));
                    g.fill(new Ellipse2D.Float(cx - rr, cy - 34 - rr, 2 * rr, 2 * rr));
                    ikonBulat(g, cx, cy - 34, Warna.MERAH, "!");
                    teksTengah(g, judul, ket, cy + 22);
                    break;
                }
                case TAKDUKUNG: {
                    g.setComposite(AlphaComposite.SrcOver.derive(pm));
                    g.translate(0, ap);
                    g.setColor(new Color(15, 23, 42, 14));
                    g.fill(new RoundRectangle2D.Float(cx - 44, cy - 94, 92, 104, 18, 18));
                    g.setColor(Color.WHITE);
                    g.fill(new RoundRectangle2D.Float(cx - 46, cy - 98, 92, 104, 18, 18));
                    g.setColor(new Color(220, 38, 38, 30));
                    g.fill(new RoundRectangle2D.Float(cx - 30, cy - 62, 60, 30, 10, 10));
                    g.setColor(Warna.MERAH);
                    g.setFont(font(Font.BOLD, 16f));
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(badge, cx - fm.stringWidth(badge) / 2, cy - 41 + fm.getAscent() / 2 - 2);
                    g.translate(0, -ap);
                    teksTengah(g, judul, ket, cy + 28);
                    break;
                }
                default: {
                    g.setComposite(AlphaComposite.SrcOver.derive(pm));
                    gambarKosong(g, cx, cy - 36, ap);
                    teksTengah(g, "Pilih berkas di daftar sebelah kiri", "Pratinjau akan tampil di sini", cy + 44);
                }
            }
            g.dispose();
        }

        /** Ilustrasi keadaan kosong: tumpukan kertas yang melayang pelan. */
        private void gambarKosong(Graphics2D g0, int cx, int cy, double ap) {
            Graphics2D g = (Graphics2D) g0.create();
            kualitas(g);
            g.translate(cx, cy + ap);
            // bayangan di tanah
            float sh = (float) (1 - ap / 30);
            g.setColor(new Color(15, 23, 42, 16));
            g.fill(new Ellipse2D.Float(-34 * sh, 66, 68 * sh, 8));
            // kertas belakang miring
            AffineTransform at = g.getTransform();
            g.rotate(Math.toRadians(-9), 0, 20);
            g.setColor(new Color(15, 23, 42, 14));
            g.fill(new RoundRectangle2D.Float(-35, -50, 70, 92, 12, 12));
            g.setColor(new Color(219, 234, 254));
            g.fill(new RoundRectangle2D.Float(-36, -52, 70, 92, 12, 12));
            g.setTransform(at);
            g.rotate(Math.toRadians(6), 0, 20);
            g.setColor(new Color(15, 23, 42, 14));
            g.fill(new RoundRectangle2D.Float(-35, -50, 70, 92, 12, 12));
            g.setColor(new Color(204, 251, 241));
            g.fill(new RoundRectangle2D.Float(-36, -52, 70, 92, 12, 12));
            g.setTransform(at);
            // kertas depan
            g.setColor(new Color(15, 23, 42, 20));
            g.fill(new RoundRectangle2D.Float(-35, -46, 70, 92, 12, 12));
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(-36, -48, 72, 94, 12, 12));
            g.setColor(Warna.TOSCA);
            g.fill(new RoundRectangle2D.Float(-26, -38, 26, 7, 4, 4));
            g.setColor(new Color(226, 232, 240));
            g.fill(new RoundRectangle2D.Float(-26, -22, 52, 5, 3, 3));
            g.fill(new RoundRectangle2D.Float(-26, -11, 52, 5, 3, 3));
            g.fill(new RoundRectangle2D.Float(-26, 0, 36, 5, 3, 3));
            // kaca pembesar
            g.setColor(new Color(37, 99, 235, 235));
            g.setStroke(new BasicStroke(3.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Float(8, 14, 22, 22));
            g.draw(new java.awt.geom.Line2D.Float(28, 34, 38, 44));
            g.setColor(new Color(37, 99, 235, 35));
            g.fill(new Ellipse2D.Float(8, 14, 22, 22));
            g.dispose();
        }

        private void ikonBulat(Graphics2D g, int cx, int cy, Color c, String s) {
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
            g.fill(new Ellipse2D.Float(cx - 30, cy - 30, 60, 60));
            g.setColor(c);
            g.setFont(font(Font.BOLD, 26f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(s, cx - fm.stringWidth(s) / 2, cy + fm.getAscent() / 2 - 3);
        }

        private void teksTengah(Graphics2D g, String atas, String bawah, int y) {
            g.setColor(Warna.TEKS);
            g.setFont(font(Font.BOLD, 15f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(atas, (getWidth() - fm.stringWidth(atas)) / 2, y);
            g.setColor(Warna.ABU_TUA);
            g.setFont(font(Font.PLAIN, 12.5f));
            fm = g.getFontMetrics();
            String[] baris = bawah.split("\n");
            for (int i = 0; i < baris.length; i++) {
                g.drawString(baris[i], (getWidth() - fm.stringWidth(baris[i])) / 2, y + 22 + i * (fm.getHeight() + 1));
            }
        }
    }

    // ============================================================ PDF (lewat refleksi; opsional)

    static final class PdfRefleksi {
        static Object buka(byte[] data) throws Exception {
            try {
                Class<?> loader = Class.forName("org.apache.pdfbox.Loader");
                return loader.getMethod("loadPDF", byte[].class).invoke(null, (Object) data);
            } catch (ClassNotFoundException e) {
                Class<?> c = Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
                return c.getMethod("load", byte[].class).invoke(null, (Object) data);
            }
        }
        static int jumlah(Object doc) throws Exception {
            return ((Integer) doc.getClass().getMethod("getNumberOfPages").invoke(doc)).intValue();
        }
        static BufferedImage render(Object doc, int hal, float dpi) throws Exception {
            Class<?> r = Class.forName("org.apache.pdfbox.rendering.PDFRenderer");
            Class<?> d = Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
            Object rend = r.getConstructor(d).newInstance(doc);
            return (BufferedImage) r.getMethod("renderImageWithDPI", int.class, float.class).invoke(rend, Integer.valueOf(hal), Float.valueOf(dpi));
        }
        static void tutup(Object doc) {
            try { doc.getClass().getMethod("close").invoke(doc); } catch (Exception e) { }
        }
    }

    // ============================================================ panel utama

    static final class PanelViewer extends JPanel {
        final Header header;
        private final Penyedia penyedia;
        private final Runnable aksiTutup;
        private String noRawat = "", noRM = "", nama = "";
        private boolean semua = true;
        private Segmented segmen;
        private List<Berkas> semuaBerkas = new ArrayList<Berkas>();
        private final DefaultListModel<Object> model = new DefaultListModel<Object>();
        private final DaftarBerkas daftar = new DaftarBerkas(model);
        private final Kanvas kanvas = new Kanvas();
        private final KolomCari cari = new KolomCari();
        private final JLabel judulFile = new JLabel("Belum ada berkas dipilih");
        private final JLabel infoFile = new JLabel(" ");
        private final JPanel barisJudul = new JPanel(new BorderLayout(8, 0));
        private final JLabel status = new JLabel(" ");
        private final JLabel jumlahLabel = new JLabel(" ");
        private final Titik titikStatus = new Titik();
        private final JLabel zoomLabel = new JLabel("100%", SwingConstants.CENTER);
        private final JLabel halLabel = new JLabel("1 / 1", SwingConstants.CENTER);
        private final Tombol btnKurang = tombol("", IK_MINUS, false, "Perkecil  ( - )"),
                btnTambah = tombol("", IK_PLUS, false, "Perbesar  ( + )"),
                btnPas = tombol("", IK_PAS, false, "Pas layar  (atau klik 2x pada gambar)"),
                btnAsli = tombol("100%", IK_NONE, false, "Ukuran asli"),
                btnPutar = tombol("", IK_PUTAR, false, "Putar 90\u00B0"),
                btnBuka = tombol("Buka", IK_BUKA, true, "Buka dengan aplikasi bawaan"),
                btnSimpan = tombol("", IK_SIMPAN, false, "Simpan salinan berkas"),
                btnPrev = tombol("", IK_PREV, false, "Halaman sebelumnya"),
                btnNext = tombol("", IK_NEXT, false, "Halaman berikutnya");
        private final JPanel navHal = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        private Berkas aktif;
        private Object pdfAktif;
        private int halaman = 0, jumlahHalaman = 0;
        private long token = 0;
        private int tugas = 0;
        private final Map<String, byte[]> cache = new LinkedHashMap<String, byte[]>() {
            @Override protected boolean removeEldestEntry(Map.Entry<String, byte[]> e) { return size() > 10; }
        };
        private boolean sedangIsi = false;

        PanelViewer(Penyedia penyedia, Runnable aksiTutup) {
            super(new BorderLayout());
            this.penyedia = penyedia;
            this.aksiTutup = aksiTutup;
            setBackground(Warna.LATAR);
            header = new Header(aksiTutup);

            // baris alat
            JPanel alat = new JPanel(new BorderLayout(10, 0));
            alat.setBackground(Warna.LATAR);
            alat.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            segmen = new Segmented(new String[]{"Kunjungan ini", "Semua kunjungan pasien"}, new IntConsumer() {
                @Override public void accept(int i) { semua = (i == 1); muat(); }
            });
            segmen.setPilih(1);
            alat.add(segmen, BorderLayout.WEST);
            JPanel kananAlat = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            kananAlat.setOpaque(false);
            kananAlat.add(cari);
            Tombol segar = tombol("Muat ulang", IK_SEGAR, false, "Muat ulang daftar  ( F5 )");
            segar.setPreferredSize(new Dimension(segar.getPreferredSize().width, 34));
            segar.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) { muat(); }
            });
            kananAlat.add(segar);
            alat.add(kananAlat, BorderLayout.EAST);

            JPanel atas = new JPanel(new BorderLayout());
            atas.add(header, BorderLayout.NORTH);
            atas.add(alat, BorderLayout.CENTER);
            add(atas, BorderLayout.NORTH);

            // daftar
            daftar.setCellRenderer(new BarisRenderer());
            daftar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            daftar.setFixedCellHeight(-1);
            daftar.addListSelectionListener(new ListSelectionListener() {
                private int terakhir = -1;
                @Override public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting() || sedangIsi) return;
                    int i = daftar.getSelectedIndex();
                    if (i < 0) return;
                    if (!(model.get(i) instanceof Berkas)) {
                        int arah = i >= terakhir ? 1 : -1;
                        int j = i + arah;
                        while (j >= 0 && j < model.size() && !(model.get(j) instanceof Berkas)) j += arah;
                        if (j < 0 || j >= model.size()) {
                            j = i - arah;
                            while (j >= 0 && j < model.size() && !(model.get(j) instanceof Berkas)) j -= arah;
                        }
                        if (j >= 0 && j < model.size()) {
                            daftar.setSelectedIndex(j);
                        } else {
                            daftar.clearSelection();
                        }
                        return;
                    }
                    terakhir = i;
                    pilih((Berkas) model.get(i));
                }
            });
            daftar.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) bukaEksternal(); }
            });
            JScrollPane sp = new JScrollPane(daftar);
            sp.setBorder(null);
            sp.getViewport().setBackground(Color.WHITE);
            sp.getVerticalScrollBar().setUI(new ScrollTipis());
            sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
            sp.getVerticalScrollBar().setUnitIncrement(18);

            JPanel judulKiri = new JPanel(new BorderLayout());
            judulKiri.setBackground(Color.WHITE);
            judulKiri.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Warna.GARIS), BorderFactory.createEmptyBorder(11, 16, 10, 16)));
            JLabel jk = new JLabel("Daftar Berkas");
            jk.setFont(font(Font.BOLD, 13.5f));
            jk.setForeground(Warna.TEKS);
            jumlahLabel.setFont(font(Font.PLAIN, 11.5f));
            jumlahLabel.setForeground(Warna.ABU_TUA);
            judulKiri.add(jk, BorderLayout.WEST);
            judulKiri.add(jumlahLabel, BorderLayout.EAST);

            Kartu kiri = new Kartu(new BorderLayout(), 16);
            kiri.add(judulKiri, BorderLayout.NORTH);
            kiri.add(sp, BorderLayout.CENTER);
            kiri.setPreferredSize(new Dimension(350, 100));
            kiri.setMinimumSize(new Dimension(260, 100));

            // pratinjau
            Kartu kanan = new Kartu(new BorderLayout(), 16);
            kanan.add(buatBarKanan(), BorderLayout.NORTH);
            kanan.add(kanvas, BorderLayout.CENTER);

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, kiri, kanan);
            split.setBorder(null);
            split.setDividerSize(12);
            split.setContinuousLayout(true);
            split.setResizeWeight(0.0);
            split.setDividerLocation(350);
            split.setBackground(Warna.LATAR);
            split.setOpaque(true);
            if (split.getUI() instanceof BasicSplitPaneUI) {
                javax.swing.plaf.basic.BasicSplitPaneDivider dv = ((BasicSplitPaneUI) split.getUI()).getDivider();
                dv.setBorder(null);
                dv.setBackground(Warna.LATAR);
            }
            JPanel tengah = new JPanel(new BorderLayout());
            tengah.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            tengah.setBackground(Warna.LATAR);
            tengah.add(split, BorderLayout.CENTER);
            add(tengah, BorderLayout.CENTER);

            // status
            status.setFont(font(Font.PLAIN, 11.5f));
            status.setForeground(Warna.ABU_TUA);
            JLabel pintas = new JLabel("Klik 2x pada daftar = buka  \u00B7  Roda mouse = zoom  \u00B7  Seret = geser  \u00B7  Esc = tutup");
            pintas.setFont(font(Font.PLAIN, 11f));
            pintas.setForeground(new Color(148, 163, 184));
            JPanel kiriBawah = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            kiriBawah.setOpaque(false);
            kiriBawah.add(titikStatus);
            kiriBawah.add(status);
            JPanel bawah = new JPanel(new BorderLayout());
            bawah.setBackground(Warna.LATAR);
            bawah.setBorder(BorderFactory.createEmptyBorder(7, 14, 8, 18));
            bawah.add(kiriBawah, BorderLayout.WEST);
            bawah.add(pintas, BorderLayout.EAST);
            add(bawah, BorderLayout.SOUTH);

            // aksi
            kanvas.padaZoom = new Runnable() {
                @Override public void run() { perbaruiAlat(); }
            };
            btnKurang.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { kanvas.zoomBy(1 / 1.25); } });
            btnTambah.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { kanvas.zoomBy(1.25); } });
            btnPas.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { kanvas.fitkan(); } });
            btnAsli.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { kanvas.ukuranAsli(); } });
            btnPutar.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { kanvas.putar90(); } });
            btnBuka.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { bukaEksternal(); } });
            btnSimpan.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { simpanSebagai(); } });
            btnPrev.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { gantiHalaman(-1); } });
            btnNext.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { gantiHalaman(1); } });
            cari.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { isiDaftar(); }
                @Override public void removeUpdate(DocumentEvent e) { isiDaftar(); }
                @Override public void changedUpdate(DocumentEvent e) { isiDaftar(); }
            });
            pasangPintasan();
            kanvas.kosong();
            perbaruiAlat();
        }

        private JComponent buatBarKanan() {
            JPanel bar = new JPanel(new BorderLayout(12, 0));
            bar.setBackground(Color.WHITE);
            bar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Warna.GARIS), BorderFactory.createEmptyBorder(9, 16, 9, 12)));
            judulFile.setFont(font(Font.BOLD, 13.5f));
            judulFile.setForeground(Warna.TEKS);
            infoFile.setFont(font(Font.PLAIN, 11.5f));
            infoFile.setForeground(Warna.ABU_TUA);
            barisJudul.setOpaque(false);
            barisJudul.add(judulFile, BorderLayout.CENTER);
            bar.setMinimumSize(new Dimension(0, 40));
            JPanel t = new JPanel(new GridLayout(2, 1, 0, 0));
            t.setOpaque(false);
            t.add(barisJudul);
            t.add(infoFile);
            bar.add(t, BorderLayout.CENTER);

            zoomLabel.setFont(font(Font.BOLD, 12f));
            zoomLabel.setForeground(Warna.NAVY);
            zoomLabel.setPreferredSize(new Dimension(46, 30));
            halLabel.setFont(font(Font.BOLD, 12f));
            halLabel.setForeground(Warna.NAVY);
            halLabel.setPreferredSize(new Dimension(56, 30));
            btnKurang.setPreferredSize(new Dimension(32, 30));
            btnTambah.setPreferredSize(new Dimension(32, 30));
            btnPas.setPreferredSize(new Dimension(32, 30));
            btnSimpan.setPreferredSize(new Dimension(32, 30));
            btnPutar.setPreferredSize(new Dimension(32, 30));
            btnPrev.setPreferredSize(new Dimension(30, 30));
            btnNext.setPreferredSize(new Dimension(30, 30));
            navHal.setOpaque(false);
            navHal.add(btnPrev);
            navHal.add(halLabel);
            navHal.add(btnNext);

            JPanel a = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
            a.setOpaque(false);
            a.add(navHal);
            a.add(btnKurang);
            a.add(zoomLabel);
            a.add(btnTambah);
            a.add(btnPas);
            a.add(btnAsli);
            a.add(btnPutar);
            a.add(Box.createHorizontalStrut(6));
            a.add(btnSimpan);
            a.add(btnBuka);
            bar.add(a, BorderLayout.EAST);
            return bar;
        }

        private void setJudulFile(Berkas b) {
            barisJudul.removeAll();
            if (b != null) {
                Color w = Warna.jenis(b);
                barisJudul.add(pil(b.labelExt(), alfa(w, 30), w), BorderLayout.WEST);
            }
            barisJudul.add(judulFile, BorderLayout.CENTER);
            barisJudul.revalidate();
            barisJudul.repaint();
        }

        private void pasangPintasan() {
            InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = getActionMap();
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "tutup");
            am.put("tutup", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { aksiTutup.run(); } });
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "segar");
            am.put("segar", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { muat(); } });
            int[] tambah = {KeyEvent.VK_ADD, KeyEvent.VK_EQUALS, KeyEvent.VK_PLUS};
            for (int k : tambah) im.put(KeyStroke.getKeyStroke(k, 0), "zoomin");
            int[] kurang = {KeyEvent.VK_SUBTRACT, KeyEvent.VK_MINUS};
            for (int k : kurang) im.put(KeyStroke.getKeyStroke(k, 0), "zoomout");
            am.put("zoomin", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { if (!cari.isFocusOwner()) kanvas.zoomBy(1.25); } });
            am.put("zoomout", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { if (!cari.isFocusOwner()) kanvas.zoomBy(1 / 1.25); } });
        }

        // ------------------------------------------------------------ data

        void setPasien(String noRawat, String noRM, String nama) {
            this.noRawat = noRawat == null ? "" : noRawat;
            this.noRM = noRM == null ? "" : noRM;
            this.nama = nama == null ? "" : nama;
            // default: tampilkan berkas SEMUA kunjungan berdasarkan No.RM; jika No.RM kosong, pakai No.Rawat
            semua = !this.noRM.isEmpty();
            segmen.setPilih(semua ? 1 : 0);
            header.sub.setText(this.nama + "   \u00B7   No.RM : " + this.noRM + "   \u00B7   No.Rawat : " + this.noRawat);
        }

        void muat() {
            daftar.pesanKosong = "Memuat daftar berkas...";
            model.clear();
            semuaBerkas = new ArrayList<Berkas>();
            tutupPdf();
            aktif = null;
            kanvas.kosong();
            perbaruiHeaderChip();
            status.setText("Memuat...");
            jumlahLabel.setText(" ");
            judulFile.setText("Belum ada berkas dipilih");
            setJudulFile(null);
            infoFile.setText(" ");
            perbaruiAlat();
            jalankan(new Callable<List<Berkas>>() {
                @Override public List<Berkas> call() throws Exception { return penyedia.muat(semua, noRawat, noRM); }
            }, new Consumer<List<Berkas>>() {
                @Override public void accept(List<Berkas> hasil) {
                    semuaBerkas = hasil;
                    isiDaftar();
                    daftar.mulaiMasuk();
                    perbaruiHeaderChip();
                    if (hasil.isEmpty()) {
                        daftar.pesanKosong = semua ? "Pasien ini belum memiliki berkas digital." : "Kunjungan ini belum memiliki berkas digital.\nCoba pilih \"Semua kunjungan pasien\".";
                    } else {
                        int pilihIdx = -1;
                        for (int i = 0; i < model.size(); i++) {
                            if (model.get(i) instanceof Berkas) {
                                if (pilihIdx < 0) pilihIdx = i;
                                if (((Berkas) model.get(i)).noRawat.equals(noRawat)) { pilihIdx = i; break; }
                            }
                        }
                        if (pilihIdx >= 0) {
                            daftar.setSelectedIndex(pilihIdx);
                            daftar.ensureIndexIsVisible(pilihIdx);
                        }
                    }
                }
            }, new Consumer<Throwable>() {
                @Override public void accept(Throwable t) {
                    daftar.pesanKosong = "Gagal memuat daftar berkas.\n" + t.getMessage();
                    status.setText("Gagal memuat data");
                    daftar.repaint();
                }
            });
        }

        private void isiDaftar() {
            String q = cari.getText().trim().toLowerCase();
            Object terpilih = daftar.getSelectedValue();
            sedangIsi = true;
            model.clear();
            String grup = null;
            int n = 0;
            for (Berkas b : semuaBerkas) {
                if (!q.isEmpty() && !(b.nama.toLowerCase().contains(q) || b.namaFile().toLowerCase().contains(q) || b.noRawat.toLowerCase().contains(q))) continue;
                String g = b.noRawat + "|" + b.tgl + "|" + b.jam;
                if (!g.equals(grup)) {
                    model.addElement(b.judulKunjungan() + (b.noRawat.equals(noRawat) ? "\u0001" : ""));
                    grup = g;
                }
                model.addElement(b);
                n++;
            }
            if (terpilih instanceof Berkas && model.contains(terpilih)) daftar.setSelectedValue(terpilih, true);
            sedangIsi = false;
            status.setText(n + " dari " + semuaBerkas.size() + " berkas ditampilkan" + (semua ? "  \u00B7  semua kunjungan" : "  \u00B7  kunjungan ini"));
            jumlahLabel.setText(n + " berkas");
            if (!q.isEmpty() && n == 0) daftar.pesanKosong = "Tidak ada berkas yang cocok dengan pencarian.";
            daftar.repaint();
        }

        private void perbaruiHeaderChip() {
            header.chip.removeAll();
            int pdf = 0, img = 0;
            for (Berkas b : semuaBerkas) { if (b.pdf()) pdf++; else if (b.gambar()) img++; }
            Color putihTipis = new Color(255, 255, 255, 40);
            header.chip.add(pil(semuaBerkas.size() + " berkas", putihTipis, Color.WHITE));
            if (pdf > 0) header.chip.add(pil(pdf + " PDF", new Color(220, 38, 38, 190), Color.WHITE));
            if (img > 0) header.chip.add(pil(img + " gambar", new Color(37, 99, 235, 190), Color.WHITE));
            header.chip.revalidate();
            header.chip.repaint();
        }

        // ------------------------------------------------------------ pratinjau

        private void pilih(final Berkas b) {
            tutupPdf();
            aktif = b;
            final long t = ++token;
            judulFile.setText(b.nama);
            setJudulFile(b);
            infoFile.setText(b.namaFile());
            kanvas.memuat("Memuat berkas...");
            perbaruiAlat();
            jalankan(new Callable<Object[]>() {
                @Override public Object[] call() throws Exception {
                    byte[] data = ambil(b);
                    if (b.gambar()) {
                        BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                        return new Object[]{img, null, Integer.valueOf(0)};
                    } else if (b.pdf()) {
                        Object doc = PdfRefleksi.buka(data);
                        int n = PdfRefleksi.jumlah(doc);
                        BufferedImage img = PdfRefleksi.render(doc, 0, 130f);
                        return new Object[]{img, doc, Integer.valueOf(n)};
                    }
                    return new Object[]{null, null, Integer.valueOf(0)};
                }
            }, new Consumer<Object[]>() {
                @Override public void accept(Object[] r) {
                    if (t != token) { if (r[1] != null) PdfRefleksi.tutup(r[1]); return; }
                    BufferedImage img = (BufferedImage) r[0];
                    if (img != null) {
                        pdfAktif = r[1];
                        halaman = 0;
                        jumlahHalaman = ((Integer) r[2]).intValue();
                        kanvas.tampil(img);
                    } else {
                        kanvas.takDidukung(b.ext.isEmpty() ? "FILE" : b.ext.toUpperCase(), "Pratinjau tidak tersedia untuk berkas ini",
                                "Gunakan tombol \"Buka\" untuk melihatnya dengan aplikasi bawaan.");
                    }
                    perbaruiAlat();
                }
            }, new Consumer<Throwable>() {
                @Override public void accept(Throwable e) {
                    if (t != token) return;
                    Throwable dasar = e;
                    while (dasar.getCause() != null) dasar = dasar.getCause();
                    if (b.pdf() && (dasar instanceof ClassNotFoundException || dasar instanceof NoClassDefFoundError)) {
                        kanvas.takDidukung("PDF", "Pratinjau PDF tidak tersedia",
                                "Pustaka PDFBox tidak ditemukan di aplikasi.\nGunakan tombol \"Buka\" untuk melihat PDF.");
                    } else if (dasar instanceof FileNotFoundException) {
                        kanvas.galat("Berkas tidak ditemukan di server", "Periksa apakah file masih ada di folder berkas digital.\n" + b.lokasi);
                    } else {
                        kanvas.galat("Gagal memuat berkas", String.valueOf(dasar.getMessage()));
                    }
                    perbaruiAlat();
                }
            });
        }

        private void gantiHalaman(int d) {
            if (pdfAktif == null) return;
            final int baru = halaman + d;
            if (baru < 0 || baru >= jumlahHalaman) return;
            final Object doc = pdfAktif;
            final long t = token;
            jalankan(new Callable<BufferedImage>() {
                @Override public BufferedImage call() throws Exception { return PdfRefleksi.render(doc, baru, 130f); }
            }, new Consumer<BufferedImage>() {
                @Override public void accept(BufferedImage img) {
                    if (t != token || doc != pdfAktif) return;
                    halaman = baru;
                    kanvas.tampil(img);
                    perbaruiAlat();
                }
            }, new Consumer<Throwable>() {
                @Override public void accept(Throwable e) { kanvas.galat("Gagal menampilkan halaman", String.valueOf(e.getMessage())); }
            });
        }

        private void tutupPdf() {
            if (pdfAktif != null) { PdfRefleksi.tutup(pdfAktif); pdfAktif = null; }
            halaman = 0;
            jumlahHalaman = 0;
        }

        private void perbaruiAlat() {
            boolean gambar = kanvas.status() == Kanvas.GAMBAR;
            btnKurang.setEnabled(gambar);
            btnTambah.setEnabled(gambar);
            btnPas.setEnabled(gambar);
            btnAsli.setEnabled(gambar);
            btnPutar.setEnabled(gambar);
            btnBuka.setEnabled(aktif != null);
            btnSimpan.setEnabled(aktif != null);
            zoomLabel.setText(gambar ? Math.round(kanvas.skala() * 100) + "%" : "-");
            navHal.setVisible(pdfAktif != null && jumlahHalaman > 1);
            halLabel.setText((halaman + 1) + " / " + Math.max(1, jumlahHalaman));
            btnPrev.setEnabled(halaman > 0);
            btnNext.setEnabled(halaman < jumlahHalaman - 1);
        }

        // ------------------------------------------------------------ berkas

        private String alamat(Berkas b) {
            String l = b.lokasi.replace(" ", "%20");
            if (l.startsWith("http://") || l.startsWith("https://") || l.startsWith("file:")) return l;
            return penyedia.urlDasar() + (l.startsWith("/") ? l.substring(1) : l);
        }

        private byte[] ambil(Berkas b) throws IOException {
            String kunci = alamat(b);
            synchronized (cache) {
                byte[] ada = cache.get(kunci);
                if (ada != null) return ada;
            }
            URLConnection c = new URL(kunci).openConnection();
            c.setConnectTimeout(8000);
            c.setReadTimeout(30000);
            InputStream in = c.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                byte[] buf = new byte[16384];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            } finally {
                in.close();
            }
            byte[] data = out.toByteArray();
            synchronized (cache) { cache.put(kunci, data); }
            return data;
        }

        private void bukaEksternal() {
            final Berkas b = aktif;
            if (b == null) return;
            status.setText("Menyiapkan berkas...");
            jalankan(new Callable<File>() {
                @Override public File call() throws Exception {
                    byte[] data = ambil(b);
                    File f = File.createTempFile("berkas_", "." + (b.ext.isEmpty() ? "bin" : b.ext));
                    f.deleteOnExit();
                    FileOutputStream o = new FileOutputStream(f);
                    try { o.write(data); } finally { o.close(); }
                    Desktop.getDesktop().open(f);
                    return f;
                }
            }, new Consumer<File>() {
                @Override public void accept(File f) { status.setText("Dibuka dengan aplikasi bawaan : " + f.getName()); }
            }, new Consumer<Throwable>() {
                @Override public void accept(Throwable e) {
                    status.setText("Gagal membuka berkas");
                    JOptionPane.showMessageDialog(PanelViewer.this, "Gagal membuka berkas : " + e.getMessage());
                }
            });
        }

        private void simpanSebagai() {
            final Berkas b = aktif;
            if (b == null) return;
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(b.namaFile()));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            final File tujuan = fc.getSelectedFile();
            jalankan(new Callable<File>() {
                @Override public File call() throws Exception {
                    byte[] data = ambil(b);
                    FileOutputStream o = new FileOutputStream(tujuan);
                    try { o.write(data); } finally { o.close(); }
                    return tujuan;
                }
            }, new Consumer<File>() {
                @Override public void accept(File f) { status.setText("Tersimpan : " + f.getAbsolutePath()); }
            }, new Consumer<Throwable>() {
                @Override public void accept(Throwable e) { JOptionPane.showMessageDialog(PanelViewer.this, "Gagal menyimpan berkas : " + e.getMessage()); }
            });
        }

        void bersihkan() {
            token++;
            tugas = 0;
            header.setSibuk(false);
            titikStatus.setSibuk(false);
            kanvas.henti();
            tutupPdf();
            synchronized (cache) { cache.clear(); }
        }

        private void ubahTugas(int d) {
            tugas = Math.max(0, tugas + d);
            header.setSibuk(tugas > 0);
            titikStatus.setSibuk(tugas > 0);
        }

        private <T> void jalankan(final Callable<T> kerja, final Consumer<T> sukses, final Consumer<Throwable> gagal) {
            ubahTugas(1);
            new SwingWorker<T, Void>() {
                @Override protected T doInBackground() throws Exception { return kerja.call(); }
                @Override protected void done() {
                    ubahTugas(-1);
                    try {
                        sukses.accept(get());
                    } catch (Throwable e) {
                        Throwable sebab = (e instanceof java.util.concurrent.ExecutionException && e.getCause() != null) ? e.getCause() : e;
                        gagal.accept(sebab);
                    }
                }
            }.execute();
        }
    }
}
