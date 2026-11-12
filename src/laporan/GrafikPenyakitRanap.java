package laporan;

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
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
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
 * Grafik 10 Besar Penyakit Rawat Inap (berdasarkan tanggal keluar).
 * Batang bertumpuk Laki-laki / Perempuan, tema gelap + animasi sama dengan grafik Rekap Operasi.
 */
public final class GrafikPenyakitRanap extends JDialog {

    private static final Color BG1 = new Color(0x0B, 0x13, 0x2B);
    private static final Color BG2 = new Color(0x1C, 0x25, 0x41);
    private static final Color TEKS = new Color(0xE6, 0xEE, 0xF8);
    private static final Color TEKS_REDUP = new Color(0x8F, 0xA6, 0xBD);
    private static final Color EMAS = new Color(0xFF, 0xD5, 0x6B);
    private static final Color MERAH = new Color(0xFF, 0x8F, 0xA3);
    private static final Color BIRU_KODE = new Color(0x6E, 0xE7, 0xF9);
    /** pasangan warna {atas, bawah} */
    private static final Color[] WARNA_LK = {new Color(0x6E, 0xE7, 0xF9), new Color(0x2F, 0x80, 0xED)};
    private static final Color[] WARNA_PR = {new Color(0xFF, 0xC3, 0x71), new Color(0xFF, 0x7A, 0x18)};

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

    /** Tampilkan dialog grafik (modal). */
    public static void tampil(Window owner, List<Baris> data, String periode) {
        if (data == null || data.isEmpty()) {
            JOptionPane.showMessageDialog(owner, "Tidak ada data pasien rawat inap yang keluar pada periode tersebut.");
            return;
        }
        GrafikPenyakitRanap d = new GrafikPenyakitRanap(owner, data, periode);
        d.setVisible(true);
        d.dispose();
    }

    /** Ikon grafik batang (gradasi) + garis tren - digambar sendiri, tidak butuh file gambar. */
    public static ImageIcon ikonGrafikBatang() {
        int n = 22;
        BufferedImage im = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        Color[][] c = {
            {new Color(0x6E, 0xE7, 0xF9), new Color(0x2F, 0x80, 0xED)},
            {new Color(0xFF, 0xC3, 0x71), new Color(0xFF, 0x7A, 0x18)},
            {new Color(0x7B, 0xF2, 0xA8), new Color(0x14, 0xA7, 0x6C)}};
        int[] tinggi = {9, 14, 19};
        for (int i = 0; i < 3; i++) {
            int x = 2 + i * 7, y = 20 - tinggi[i];
            g.setPaint(new GradientPaint(0, y, c[i][0], 0, 20, c[i][1]));
            g.fillRoundRect(x, y, 5, tinggi[i], 3, 3);
        }
        g.setColor(new Color(0x3C, 0x4F, 0x6B));
        g.fillRoundRect(1, 20, 20, 2, 2, 2);
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(0x1F, 0x3A, 0x5F));
        g.drawPolyline(new int[]{3, 9, 13, 19}, new int[]{11, 6, 9, 2}, 4);
        g.fillOval(17, 0, 4, 4);
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
            g2.drawString("10 BESAR PENYAKIT RAWAT INAP", 28, 42);
            g2.setColor(new Color(0x9F, 0xD8, 0xF0));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.drawString("Periode : " + periode + "   \u2022   berdasarkan tanggal keluar   \u2022   dibagi menurut jenis kelamin", 29, 64);

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
    static final class BarChartPanel extends JPanel implements Scrollable {
        private static final int BAR_H = 28, LEGEND_H = 38, BOTTOM = 26, MARGIN_KANAN = 190, ROW_H = 78;
        private static final int TOP = LEGEND_H + 46;
        private static final Font F_NAMA = new Font("Segoe UI", Font.PLAIN, 14);
        private static final Font F_NAMA_TOP = new Font("Segoe UI", Font.BOLD, 14);
        private static final Font F_NILAI = new Font("Segoe UI", Font.BOLD, 14);
        private static final Font F_KECIL = new Font("Segoe UI", Font.PLAIN, 12);
        private static final Font F_KECIL_TEBAL = new Font("Segoe UI", Font.BOLD, 12);

        private final List<Baris> data;
        private final int total, totalLk, totalPr, totalMati, max;
        private final int[] rank; // peringkat (jumlah sama = peringkat sama)
        private boolean statis = false; // true = tanpa animasi (mis. saat simpan gambar)
        private long mulai = 0;
        private Timer timer;
        private int hover = -1;

        BarChartPanel(List<Baris> data) {
            this.data = data;
            int t = 0, l = 0, p = 0, m = 0, mx = 1;
            for (Baris b : data) {
                t += b.total();
                l += b.lk;
                p += b.pr;
                m += b.mati();
                mx = Math.max(mx, b.total());
            }
            this.total = Math.max(1, t);
            this.totalLk = l;
            this.totalPr = p;
            this.totalMati = m;
            this.max = mx;
            this.rank = new int[data.size()];
            for (int i = 0; i < data.size(); i++) {
                rank[i] = (i > 0 && data.get(i).total() == data.get(i - 1).total()) ? rank[i - 1] : i + 1;
            }
            setOpaque(true);
            setPreferredSize(new Dimension(900, TOP + data.size() * ROW_H + tinggiRingkasan() + BOTTOM));
            ToolTipManager.sharedInstance().registerComponent(this);
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int idx = (e.getY() - TOP) / ROW_H;
                    if (e.getY() < TOP || idx < 0 || idx >= BarChartPanel.this.data.size()) {
                        idx = -1;
                    }
                    if (idx != hover) {
                        hover = idx;
                        setToolTipText(idx >= 0 ? tooltip(BarChartPanel.this.data.get(idx)) : null);
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

        private int tinggiRingkasan() {
            return 18 + 34 + 2 * 32 + 52;
        }

        private String tooltip(Baris d) {
            StringBuilder sb = new StringBuilder("<html><b>").append(esc(d.kode)).append(" - ").append(esc(d.nama)).append("</b><br>");
            sb.append("Laki-laki : <b>").append(d.lk).append("</b> pasien<br>");
            sb.append("Perempuan : <b>").append(d.pr).append("</b> pasien<br>");
            if (d.mati() > 0) {
                sb.append("Keluar mati : <b>").append(d.mati()).append("</b> (L ").append(d.matiLk)
                  .append(", P ").append(d.matiPr).append(")<br>");
            }
            sb.append("<b>Total : ").append(d.total()).append(" pasien</b> (")
              .append(String.format(Locale.US, "%.1f", d.total() * 100.0 / total)).append("%)</html>");
            return sb.toString();
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
            final long durasiTotal = 950L + (long) data.size() * delay;
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
            double t = (System.currentTimeMillis() - mulai - (long) i * delay) / 950.0;
            t = Math.max(0, Math.min(1, t));
            return 1 - Math.pow(1 - t, 3); // ease-out cubic
        }

        private static double langkahBagus(double kasar) {
            double pangkat = Math.pow(10, Math.floor(Math.log10(Math.max(kasar, 1e-9))));
            double f = kasar / pangkat;
            double n = f <= 1 ? 1 : f <= 2 ? 2 : f <= 5 ? 5 : 10;
            return Math.max(1, n * pangkat);
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

        private static Color alfa(Color c, int a) {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
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

        /** Satu butir rincian di bawah batang: titik warna + label + angka tebal. Mengembalikan x berikutnya. */
        private int butir(Graphics2D g2, int bx, int by, Color dot, String label, int nilai, int alpha) {
            g2.setColor(alfa(dot, alpha));
            g2.fillOval(bx, by - 9, 9, 9);
            g2.setFont(F_KECIL);
            g2.setColor(alfa(TEKS, alpha));
            g2.drawString(label, bx + 14, by);
            int tw = g2.getFontMetrics().stringWidth(label);
            g2.setFont(F_KECIL_TEBAL);
            String n = String.valueOf(nilai);
            g2.drawString(n, bx + 14 + tw, by);
            return bx + 14 + tw + g2.getFontMetrics().stringWidth(n) + 16;
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

            // ---------- lebar kolom nama & area plot ----------
            g2.setFont(F_NAMA_TOP);
            FontMetrics fm = g2.getFontMetrics();
            int namaW = 190;
            for (Baris d : data) {
                namaW = Math.max(namaW, fm.stringWidth(d.kode + "  " + d.nama) + 8);
            }
            namaW = Math.min(namaW, Math.max(190, (int) (w * 0.42)));
            int xBadge = 24;
            int xNama = xBadge + 28 + 12;
            int x0 = xNama + namaW + 16;
            int plotW = Math.max(120, w - x0 - MARGIN_KANAN);

            g2.setFont(F_KECIL);
            FontMetrics fl = g2.getFontMetrics();

            // ---------- legenda jenis kelamin ----------
            int lx = 24, ly = 24;
            g2.setColor(TEKS_REDUP);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String judulLegenda = "JENIS KELAMIN";
            g2.drawString(judulLegenda, lx, ly + 4);
            lx += g2.getFontMetrics().stringWidth(judulLegenda) + 14;
            g2.setFont(F_KECIL);
            String[] namaLeg = {"Laki-laki", "Perempuan"};
            Color[][] warnaLeg = {WARNA_LK, WARNA_PR};
            for (int k = 0; k < 2; k++) {
                Color[] c = warnaLeg[k];
                g2.setPaint(new GradientPaint(lx, ly - 6, c[0], lx, ly + 8, c[1]));
                g2.fillRoundRect(lx, ly - 6, 14, 14, 5, 5);
                g2.setColor(TEKS);
                g2.drawString(namaLeg[k], lx + 20, ly + 5);
                lx += 22 + fl.stringWidth(namaLeg[k]) + 18;
            }

            // ---------- grid + sumbu ----------
            double step = langkahBagus(max / 5.0);
            double axisMax = Math.ceil(max / step) * step;
            if (axisMax < max) {
                axisMax = max;
            }
            int gridBawah = TOP + data.size() * ROW_H;
            g2.setFont(F_KECIL);
            for (double v = 0; v <= axisMax + 1e-9; v += step) {
                int gx = x0 + (int) Math.round(plotW * v / axisMax);
                g2.setColor(new Color(255, 255, 255, v == 0 ? 60 : 22));
                g2.drawLine(gx, TOP - 8, gx, gridBawah);
                String lb = String.valueOf((long) Math.round(v));
                g2.setColor(TEKS_REDUP);
                g2.drawString(lb, gx - g2.getFontMetrics().stringWidth(lb) / 2, TOP - 16);
            }

            // ---------- baris penyakit ----------
            for (int i = 0; i < data.size(); i++) {
                Baris d = data.get(i);
                int y = TOP + i * ROW_H;
                int barTop = y + 12;
                int cy = barTop + BAR_H / 2;
                double p = progress(i);

                if (i == hover) {
                    g2.setColor(new Color(255, 255, 255, 18));
                    g2.fillRoundRect(10, y + 2, w - 20, ROW_H - 4, 14, 14);
                }

                // lencana peringkat
                if (rank[i] <= 3) {
                    Color mc = medali(rank[i]);
                    g2.setPaint(new GradientPaint(xBadge, cy - 14, mc.brighter(), xBadge, cy + 14, mc.darker()));
                } else {
                    g2.setColor(new Color(0x2B, 0x3A, 0x5C));
                }
                g2.fillOval(xBadge, cy - 14, 28, 28);
                if (rank[i] > 3) {
                    g2.setColor(new Color(255, 255, 255, 50));
                    g2.drawOval(xBadge, cy - 14, 28, 28);
                }
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(rank[i] <= 3 ? new Color(0x1A, 0x1A, 0x2E) : TEKS);
                String rk = String.valueOf(rank[i]);
                g2.drawString(rk, xBadge + 14 - g2.getFontMetrics().stringWidth(rk) / 2, cy + 5);

                // kode ICD-10 + nama penyakit
                g2.setFont(F_NAMA_TOP);
                FontMetrics fk = g2.getFontMetrics();
                g2.setColor(BIRU_KODE);
                g2.drawString(d.kode, xNama, cy + 5);
                int kw = fk.stringWidth(d.kode + "  ");
                g2.setFont(rank[i] == 1 ? F_NAMA_TOP : F_NAMA);
                g2.setColor(TEKS);
                g2.drawString(potong(d.nama, g2.getFontMetrics(), namaW - kw), xNama + kw, cy + 5);

                // ---- batang bertumpuk Laki-laki | Perempuan ----
                int bw = (int) Math.round(plotW * d.total() / axisMax * p);
                if (d.total() > 0 && p > 0) {
                    bw = Math.max(bw, (int) (BAR_H * Math.min(1.0, p)));
                }
                if (bw > 0) {
                    Shape bar = new RoundRectangle2D.Float(x0, barTop, bw, BAR_H, BAR_H, BAR_H);
                    g2.setColor(new Color(0, 0, 0, 80));
                    g2.fill(new RoundRectangle2D.Float(x0, barTop + 4, bw, BAR_H, BAR_H, BAR_H));
                    if (i == hover) {
                        g2.setColor(new Color(255, 255, 255, 40));
                        g2.fill(new RoundRectangle2D.Float(x0 - 4, barTop - 4, bw + 8, BAR_H + 8, BAR_H + 8, BAR_H + 8));
                    }
                    Shape oldClip = g2.getClip();
                    g2.clip(bar);
                    int[] jml = {d.lk, d.pr};
                    Color[][] wrn = {WARNA_LK, WARNA_PR};
                    int cum = 0;
                    int xPrev = x0;
                    for (int k = 0; k < 2; k++) {
                        if (jml[k] == 0) {
                            continue;
                        }
                        cum += jml[k];
                        int xAkhir = (cum == d.total()) ? x0 + bw : x0 + (int) Math.round(bw * (double) cum / d.total());
                        Color[] c = wrn[k];
                        g2.setPaint(new GradientPaint(0, barTop, c[0], 0, barTop + BAR_H, c[1]));
                        g2.fillRect(xPrev, barTop, Math.max(1, xAkhir - xPrev), BAR_H);
                        if (xPrev > x0) { // garis pemisah antar segmen
                            g2.setColor(new Color(11, 19, 43, 170));
                            g2.fillRect(xPrev - 1, barTop, 2, BAR_H);
                        }
                        if (xAkhir - xPrev >= 26) { // angka di dalam segmen bila cukup lebar
                            g2.setFont(F_KECIL_TEBAL);
                            String t = String.valueOf(jml[k]);
                            int tx = xPrev + (xAkhir - xPrev - g2.getFontMetrics().stringWidth(t)) / 2;
                            g2.setColor(new Color(0, 0, 0, 90));
                            g2.drawString(t, tx + 1, cy + 5);
                            g2.setColor(Color.WHITE);
                            g2.drawString(t, tx, cy + 4);
                        }
                        xPrev = xAkhir;
                    }
                    // kilau kaca di setengah bagian atas
                    g2.setPaint(new GradientPaint(0, barTop, new Color(255, 255, 255, 80), 0, barTop + BAR_H * 0.55f, new Color(255, 255, 255, 0)));
                    g2.fillRect(x0, barTop, bw, BAR_H);
                    g2.setClip(oldClip);
                    if (i == hover) {
                        g2.setColor(new Color(255, 255, 255, 190));
                        g2.setStroke(new BasicStroke(1.6f));
                        g2.draw(bar);
                    }
                }

                // total + persentase di ujung batang
                String nilai = Math.round(d.total() * p) + " pasien";
                g2.setFont(F_NILAI);
                g2.setColor(rank[i] == 1 ? EMAS : Color.WHITE);
                int tx = x0 + Math.max(bw, 0) + 12;
                g2.drawString(nilai, tx, cy + 5);
                int nw = g2.getFontMetrics().stringWidth(nilai);
                g2.setFont(F_KECIL);
                g2.setColor(TEKS_REDUP);
                g2.drawString(String.format(Locale.US, "(%.1f%%)", d.total() * 100.0 / total), tx + nw + 8, cy + 5);

                // ---- rincian di bawah batang ----
                int alpha = (int) (255 * Math.min(1.0, p * 1.3));
                int baris = barTop + BAR_H + 19;
                int bx = x0;
                bx = butir(g2, bx, baris, WARNA_LK[1].brighter(), "Laki-laki ", d.lk, alpha);
                bx = butir(g2, bx, baris, WARNA_PR[1].brighter(), "Perempuan ", d.pr, alpha);
                g2.setColor(alfa(Color.WHITE, (int) (alpha * 0.25)));
                g2.fillRect(bx - 6, baris - 11, 1, 13);
                g2.setFont(F_KECIL_TEBAL);
                g2.setColor(alfa(EMAS, alpha));
                String tt = "Total " + d.total();
                g2.drawString(tt, bx + 6, baris);
                bx += 6 + g2.getFontMetrics().stringWidth(tt) + 16;
                if (d.mati() > 0) {
                    g2.setColor(alfa(Color.WHITE, (int) (alpha * 0.25)));
                    g2.fillRect(bx - 6, baris - 11, 1, 13);
                    butir(g2, bx + 6, baris, MERAH, "Meninggal ", d.mati(), alpha);
                }
            }

            // ---------- ringkasan total per jenis kelamin ----------
            int ry = gridBawah + 18;
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawLine(24, ry, w - 24, ry);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(TEKS_REDUP);
            g2.drawString("TOTAL PER JENIS KELAMIN", 24, ry + 22);
            int maxJk = Math.max(1, Math.max(totalLk, totalPr));
            int rowY = ry + 34;
            String[] nmJk = {"Laki-laki", "Perempuan"};
            int[] cntJk = {totalLk, totalPr};
            Color[][] wrnJk = {WARNA_LK, WARNA_PR};
            for (int k = 0; k < 2; k++) {
                int cnt = cntJk[k];
                Color[] c = wrnJk[k];
                int cy2 = rowY + 14;
                g2.setPaint(new GradientPaint(24, cy2 - 7, c[0], 24, cy2 + 7, c[1]));
                g2.fillRoundRect(24, cy2 - 7, 14, 14, 5, 5);
                g2.setFont(F_NAMA);
                g2.setColor(TEKS);
                g2.drawString(nmJk[k], 48, cy2 + 5);
                int lw = Math.max(8, (int) Math.round(plotW * cnt / (double) maxJk * (statis ? 1.0 : progress(0))));
                g2.setColor(new Color(0, 0, 0, 70));
                g2.fillRoundRect(x0, cy2 - 6 + 3, lw, 12, 12, 12);
                g2.setPaint(new GradientPaint(x0, 0, c[1], x0 + lw, 0, c[0]));
                g2.fillRoundRect(x0, cy2 - 6, lw, 12, 12, 12);
                g2.setFont(F_NILAI);
                g2.setColor(Color.WHITE);
                String t = cnt + " pasien";
                g2.drawString(t, x0 + lw + 12, cy2 + 5);
                g2.setFont(F_KECIL);
                g2.setColor(TEKS_REDUP);
                g2.drawString(String.format(Locale.US, "(%.1f%%)", cnt * 100.0 / total),
                        x0 + lw + 12 + g2.getFontMetrics(F_NILAI).stringWidth(t) + 8, cy2 + 5);
                rowY += 32;
            }
            // baris TOTAL KESELURUHAN
            int ty = rowY + 6;
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawLine(24, ty, w - 24, ty);
            Font fTotal = new Font("Segoe UI", Font.BOLD, 15);
            g2.setFont(fTotal);
            g2.setColor(EMAS);
            g2.drawString("TOTAL", 48, ty + 28);
            String tot = total + " pasien";
            g2.drawString(tot, x0, ty + 28);
            g2.setFont(F_KECIL);
            g2.setColor(TEKS_REDUP);
            g2.drawString("dari " + data.size() + " penyakit"
                    + (totalMati > 0 ? "   \u2022   " + totalMati + " keluar mati" : ""),
                    x0 + g2.getFontMetrics(fTotal).stringWidth(tot) + 12, ty + 28);
            g2.dispose();
        }

        // --- Scrollable ---
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 28;
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
    private final BarChartPanel chart;
    private final List<Baris> data;
    private final String periode;

    private GrafikPenyakitRanap(Window owner, List<Baris> dataAsli, String periode) {
        super(owner, "Grafik 10 Besar Penyakit Rawat Inap", Dialog.ModalityType.APPLICATION_MODAL);
        // urutkan terbanyak -> tersedikit (sama banyak: urut kode)
        this.data = new ArrayList<>(dataAsli);
        this.data.sort((a, b) -> a.total() != b.total() ? Integer.compare(b.total(), a.total()) : a.kode.compareToIgnoreCase(b.kode));
        this.periode = periode;
        this.chart = new BarChartPanel(this.data);

        setLayout(new BorderLayout());
        add(new HeaderPanel(periode, this.data), BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(chart);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(28);
        add(sp, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(0x0F, 0x1A, 0x33));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JLabel hint = new JLabel("Arahkan kursor ke batang untuk melihat rincian jenis kelamin  \u2022  Esc untuk menutup");
        hint.setForeground(TEKS_REDUP);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.add(hint, BorderLayout.WEST);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.setOpaque(false);
        JButton btnReplay = gayaTombol(new JButton("Ulangi Animasi"));
        JButton btnPng = gayaTombol(new JButton("Simpan Gambar (PNG)"));
        JButton btnTutup = gayaTombol(new JButton("Tutup"));
        btnReplay.addActionListener(e -> chart.mulaiAnimasi());
        btnPng.addActionListener(e -> simpanPng());
        btnTutup.addActionListener(e -> dispose());
        tombol.add(btnReplay);
        tombol.add(btnPng);
        tombol.add(btnTutup);
        footer.add(tombol, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                chart.mulaiAnimasi();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                chart.hentikan();
            }
        });

        Dimension layar = Toolkit.getDefaultToolkit().getScreenSize();
        int tinggi = 150 + chart.getPreferredSize().height + 56 + 34;
        tinggi = Math.max(520, Math.min(tinggi, layar.height - 90));
        setSize(Math.min(1080, layar.width - 40), tinggi);
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

    /** Gambar lengkap (header + semua batang + ringkasan) tanpa animasi; aman dipakai tanpa jendela terbuka. */
    static BufferedImage buatGambar(List<Baris> hs, String periode, int lebar) {
        BarChartPanel ch = new BarChartPanel(hs);
        ch.setStatis(true);
        HeaderPanel hd = new HeaderPanel(periode, hs);
        int tinggiChart = ch.getPreferredSize().height;
        int tinggi = 150 + tinggiChart;
        hd.setBounds(0, 0, lebar, 150);
        ch.setBounds(0, 150, lebar, tinggiChart);
        BufferedImage img = new BufferedImage(lebar, tinggi, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        hd.paint(g);
        g.translate(0, 150);
        ch.paint(g);
        g.dispose();
        return img;
    }

    private void simpanPng() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Grafik sebagai Gambar");
        fc.setSelectedFile(new java.io.File("Grafik_10_Besar_Penyakit_Ranap_"
                + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".png"));
        fc.setFileFilter(new FileNameExtensionFilter("Gambar PNG (*.png)", "png"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".png")) {
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
