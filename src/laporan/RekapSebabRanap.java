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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
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
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Rekap RL 4A - 10 Besar Sebab Penyakit (penyebab luar V-Y) Rawat Inap (berdasarkan tanggal keluar).
 * Menampilkan tabel rekapan (jenis kelamin hidup+mati dan keluar mati) dengan tema gelap + animasi
 * yang sama dengan jendela Rekap Operasi.
 */
public final class RekapSebabRanap extends JDialog {

    private static final String JUDUL = "RL 4A - 10 BESAR SEBAB PENYAKIT RAWAT INAP";
    private static final String DASAR = "penyebab luar (V-Y), berdasarkan tanggal keluar";
    private static final String GRUP1 = "Jumlah Pasien Hidup dan Mati Menurut Jenis Kelamin";
    private static final String GRUP2 = "Jumlah Pasien Keluar Mati";
    private static final String AWALAN_FILE = "Rekap_10_Besar_Sebab_Penyakit_Ranap_";

    private static final Color BG1 = new Color(0x0B, 0x13, 0x2B);
    private static final Color BG2 = new Color(0x1C, 0x25, 0x41);
    private static final Color TEKS = new Color(0xE6, 0xEE, 0xF8);
    private static final Color TEKS_REDUP = new Color(0x8F, 0xA6, 0xBD);
    private static final Color EMAS = new Color(0xFF, 0xD5, 0x6B);
    private static final Color MERAH = new Color(0xFF, 0x8F, 0xA3);
    private static final Color BIRU = new Color(0x6E, 0xE7, 0xF9);
    private static final Color ORANYE = new Color(0xFF, 0xC3, 0x71);

    /** Satu baris penyakit. */
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

    /** Tampilkan dialog rekap (modal). */
    public static void tampil(Window owner, List<Baris> data, String periode) {
        if (data == null || data.isEmpty()) {
            JOptionPane.showMessageDialog(owner, "Tidak ada data pasien rawat inap yang keluar pada periode tersebut.");
            return;
        }
        RekapSebabRanap d = new RekapSebabRanap(owner, data, periode);
        d.setVisible(true);
        d.dispose();
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

    // ------------------------------------------------------------------
    static final class HeaderPanel extends JPanel {
        private final String periode;
        private final int totalPasien, totalMati, topJumlah;
        private final String topNama;

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

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new LinearGradientPaint(0, 0, Math.max(w, 2), 0, new float[]{0f, 0.55f, 1f},
                    new Color[]{new Color(0x0F, 0x20, 0x27), new Color(0x20, 0x3A, 0x43), new Color(0x2C, 0x53, 0x64)}));
            g2.fillRect(0, 0, w, h);
            g2.setPaint(new RadialGradientPaint(w - 80, 10, 260f, new float[]{0f, 1f},
                    new Color[]{new Color(0x00, 0xD2, 0xFF, 70), new Color(0x00, 0xD2, 0xFF, 0)}));
            g2.fillRect(0, 0, w, h);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
            g2.drawString("RL 4A - 10 BESAR SEBAB PENYAKIT RAWAT INAP", 28, 42);
            g2.setColor(new Color(0x9F, 0xD8, 0xF0));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.drawString("Periode : " + periode + "   \u2022   penyebab luar (V-Y), berdasarkan tanggal keluar", 29, 64);

            String[] teksLabel = {"TOTAL PASIEN", "PASIEN MENINGGAL", "TERBANYAK"};
            String[] nilai = {String.valueOf(totalPasien), String.valueOf(totalMati), topJumlah + "  \u2022  " + topNama};
            int[] lebar = {170, 170, Math.max(260, w - 28 - 170 - 170 - 24 - 28)};
            int x = 28, y = 80;
            for (int i = 0; i < 3; i++) {
                g2.setColor(new Color(255, 255, 255, 26));
                g2.fillRoundRect(x, y, lebar[i], 56, 16, 16);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(x, y, lebar[i], 56, 16, 16);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.setColor(new Color(0x9F, 0xB8, 0xCC));
                g2.drawString(teksLabel[i], x + 14, y + 19);
                g2.setFont(new Font("Segoe UI", Font.BOLD, i == 2 ? 15 : 22));
                g2.setColor(i == 2 ? EMAS : (i == 1 && totalMati > 0 ? MERAH : Color.WHITE));
                String v = nilai[i];
                FontMetrics fm = g2.getFontMetrics();
                while (fm.stringWidth(v) > lebar[i] - 28 && v.length() > 4) {
                    v = v.substring(0, v.length() - 2);
                }
                if (!v.equals(nilai[i])) {
                    v = v.trim() + "\u2026";
                }
                g2.drawString(v, x + 14, y + 44);
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
        private long mulai = 0;
        private Timer timer;
        private int hover = -1;

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
            setPreferredSize(new Dimension(900, PAD_ATAS + H_GRUP + H_SUB + data.size() * ROW_H + BOTTOM));
            ToolTipManager.sharedInstance().registerComponent(this);
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
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    if (hover != -1) {
                        hover = -1;
                        repaint();
                    }
                }
            });
        }

        private static int barisAwal() {
            return PAD_ATAS + H_GRUP + H_SUB;
        }

        private String tooltip(Baris d) {
            return "<html><b>" + esc(d.kode) + " - " + esc(d.nama) + "</b><br>"
                    + "Laki-laki : <b>" + d.lk + "</b> &nbsp; Perempuan : <b>" + d.pr + "</b> &nbsp; Total : <b>" + d.total() + "</b><br>"
                    + "Keluar mati : <b>" + d.mati() + "</b> (L " + d.matiLk + ", P " + d.matiPr + ")</html>";
        }

        private static String esc(String s) {
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }

        void setStatis(boolean v) {
            statis = v;
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
        }

        private double progress(int i) {
            if (statis) {
                return 1.0;
            }
            int delay = data.isEmpty() ? 0 : Math.min(70, 700 / data.size());
            double t = (System.currentTimeMillis() - mulai - (long) i * delay) / 650.0;
            t = Math.max(0, Math.min(1, t));
            return 1 - Math.pow(1 - t, 3); // ease-out cubic
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
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
            g2.setColor(new Color(255, 255, 255, 16));
            g2.fillRoundRect(PAD - 8, yG, kanan - PAD + 16, H_GRUP + H_SUB, 14, 14);
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawRoundRect(PAD - 8, yG, kanan - PAD + 16, H_GRUP + H_SUB, 14, 14);

            g2.setFont(F_HEAD);
            g2.setColor(TEKS);
            int tengahKepala = yG + (H_GRUP + H_SUB) / 2;
            int bl = tengahKepala + 5;
            tengah(g2, "No.", xs[0], W_NO, bl);
            // "Kelompok ICD-10" dua baris
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
            g2.drawLine(xs[3], yG + 6, xs[3], yG + H_GRUP + H_SUB - 6);
            g2.drawLine(xs[6], yG + 6, xs[6], yG + H_GRUP + H_SUB - 6);
            g2.drawLine(xs[3], yS, xs[9], yS);

            // ---------- baris data ----------
            for (int i = 0; i < data.size(); i++) {
                Baris d = data.get(i);
                int y = y0 + i * ROW_H;
                double p = progress(i);
                if (p <= 0) {
                    continue;
                }
                java.awt.Composite oldC = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) p));
                g2.translate((1 - p) * 28, 0);

                if (i == hover) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(PAD - 8, y + 2, kanan - PAD + 16, ROW_H - 4, 12, 12);
                } else if (i % 2 == 0) {
                    g2.setColor(new Color(255, 255, 255, 9));
                    g2.fillRoundRect(PAD - 8, y + 2, kanan - PAD + 16, ROW_H - 4, 12, 12);
                }
                int cy = y + ROW_H / 2;
                int base = cy + 5;

                // lencana peringkat
                int bx = xs[0] + (W_NO - 28) / 2;
                if (i < 3) {
                    Color mc = medali(i + 1);
                    g2.setPaint(new GradientPaint(bx, cy - 14, mc.brighter(), bx, cy + 14, mc.darker()));
                } else {
                    g2.setColor(new Color(0x2B, 0x3A, 0x5C));
                }
                g2.fillOval(bx, cy - 14, 28, 28);
                if (i >= 3) {
                    g2.setColor(new Color(255, 255, 255, 50));
                    g2.drawOval(bx, cy - 14, 28, 28);
                }
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(i < 3 ? new Color(0x1A, 0x1A, 0x2E) : TEKS);
                tengah(g2, String.valueOf(i + 1), bx, 28, cy + 5);

                // kode ICD-10 + nama penyakit
                g2.setFont(F_TEBAL);
                g2.setColor(BIRU);
                tengah(g2, d.kode, xs[1], W_ICD, base);
                g2.setFont(rank[i] == 1 ? F_TEBAL : F_ISI);
                g2.setColor(TEKS);
                g2.drawString(potong(d.nama, g2.getFontMetrics(), diagW - 16), xs[2] + 8, base);

                // angka
                int[] nil = {d.lk, d.pr, d.total(), d.matiLk, d.matiPr, d.mati()};
                for (int k = 0; k < 6; k++) {
                    boolean mati = k >= 3;
                    boolean total = (k == 2 || k == 5);
                    g2.setFont(total || (mati && nil[k] > 0) ? F_TEBAL : F_ISI);
                    if (mati) {
                        g2.setColor(nil[k] > 0 ? MERAH : new Color(0x8F, 0xA6, 0xBD, 150));
                    } else if (k == 2) {
                        g2.setColor(rank[i] == 1 ? EMAS : Color.WHITE);
                    } else {
                        g2.setColor(TEKS);
                    }
                    tengah(g2, String.valueOf(Math.round(nil[k] * (statis ? 1.0 : p))), xs[3 + k], numW, base);
                }

                // bilah mini di bawah angka Total (skala terhadap penyakit terbanyak)
                int bxl = xs[5] + 16, bwMax = numW - 32;
                int bwTot = Math.max(3, (int) Math.round(bwMax * d.total() / (double) max));
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillRoundRect(bxl, y + ROW_H - 9, bwMax, 3, 3, 3);
                g2.setPaint(new GradientPaint(bxl, 0, new Color(0xFF, 0xE2, 0x7A), bxl + bwTot, 0, new Color(0xE5, 0xA8, 0x00)));
                g2.fillRoundRect(bxl, y + ROW_H - 9, bwTot, 3, 3, 3);

                g2.translate(-(1 - p) * 28, 0);
                g2.setComposite(oldC);
            }

            // garis pemisah halus antar kolom pada isi
            g2.setColor(new Color(255, 255, 255, 14));
            for (int k : new int[]{1, 2, 3, 6}) {
                g2.drawLine(xs[k], y0 + 4, xs[k], tabelBawah - 4);
            }
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawLine(PAD - 8, tabelBawah, kanan + 8, tabelBawah);
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
    private final TabelPanel tabel;
    private final List<Baris> data;
    private final String periode;

    private RekapSebabRanap(Window owner, List<Baris> dataAsli, String periode) {
        super(owner, "Rekap 10 Besar Sebab Penyakit Rawat Inap", Dialog.ModalityType.APPLICATION_MODAL);
        // urutkan terbanyak -> tersedikit (sama banyak: urut kode)
        this.data = new ArrayList<>(dataAsli);
        this.data.sort((a, b) -> a.total() != b.total() ? Integer.compare(b.total(), a.total()) : a.kode.compareToIgnoreCase(b.kode));
        this.periode = periode;
        this.tabel = new TabelPanel(this.data);

        setLayout(new BorderLayout());
        add(new HeaderPanel(periode, this.data), BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(tabel);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(TabelPanel.ROW_H);
        add(sp, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(0x0F, 0x1A, 0x33));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JLabel hint = new JLabel("Arahkan kursor ke baris untuk nama lengkap  \u2022  Esc: tutup");
        hint.setForeground(TEKS_REDUP);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.add(hint, BorderLayout.WEST);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.setOpaque(false);
        JButton btnReplay = gayaTombol(new JButton("Ulangi Animasi"));
        JButton btnPng = gayaTombol(new JButton("Simpan PNG"));
        JButton btnExcel = gayaTombol(new JButton("Export Excel"));
        JButton btnPdf = gayaTombol(new JButton("Save As PDF"));
        JButton btnTutup = gayaTombol(new JButton("Tutup"));
        btnReplay.addActionListener(e -> tabel.mulaiAnimasi());
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
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                tabel.mulaiAnimasi();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                tabel.hentikan();
            }
        });

        Dimension layar = Toolkit.getDefaultToolkit().getScreenSize();
        int tinggi = 150 + tabel.getPreferredSize().height + 56 + 34;
        tinggi = Math.max(480, Math.min(tinggi, layar.height - 90));
        setSize(Math.min(1120, layar.width - 40), tinggi);
        setLocationRelativeTo(owner);
    }

    private static JButton gayaTombol(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(new Color(0x24, 0x3B, 0x55));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x3E, 0x5C, 0x7E)),
                BorderFactory.createEmptyBorder(7, 16, 7, 16)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
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
        fc.setSelectedFile(new java.io.File(AWALAN_FILE
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
            RekapEkspor.tulisXlsx(f, JUDUL, periode, DASAR, GRUP1, GRUP2, kolomKode(), kolomNama(), kolomAngka());
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
            RekapEkspor.tulisPdf(f, JUDUL, periode, DASAR, GRUP1, GRUP2, kolomKode(), kolomNama(), kolomAngka());
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

    private void simpanPng() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Rekap sebagai Gambar");
        fc.setSelectedFile(new java.io.File("Rekap_10_Besar_Sebab_Penyakit_Ranap_"
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
            javax.imageio.ImageIO.write(buatGambar(data, periode, Math.max(1000, getWidth())), "png", f);
            JOptionPane.showMessageDialog(this, "Gambar berhasil disimpan:\n" + f.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan gambar:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
