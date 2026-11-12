/*
 * grafiksqlttv.java — Grafik TTV (Suhu, Nadi, Respirasi, SpO2, Tensi) untuk SIMRS Khanza
 * memakai JFreeChart (CombinedDomainCategoryPlot).
 *
 * SYARAT QUERY (urutan kolom):
 *   1 tanggal, 2 jam, 3 suhu, 4 nadi, 5 respirasi, 6 spo2, 7 tensi ("120/80"), 8 NIP penginput SOAP
 *   (posisi bisa diubah lewat VITALS[].columnIndex, TENSI dan KOLOM_NIP).
 *
 * FILTER: hanya SOAP perawat + dokter umum yang digambar (SOAP dokter spesialis diabaikan).
 *
 * VERSI RINGAN:
 *  - Query data dijalankan SEKALI saja (sebelumnya 6x per halaman, dan tiap kali
 *    ditambah 3 query filter NIP). Pindah halaman tidak menyentuh database lagi.
 *  - Daftar NIP yang diizinkan di-cache (lihat MASA_CACHE_MS).
 *  - Hanya baris yang lolos filter yang disimpan di memori, dalam array kecil.
 *  - Objek Font/Color dipakai ulang (tidak dibuat ulang tiap menggambar).
 *  - Label identitas pasien tidak lagi memakai HTML (lebih cepat & hemat RAM).
 *
 * Catatan JFreeChart: memakai org.jfree.ui.* (versi lama). Jika project memakai
 * JFreeChart >= 1.5, ganti org.jfree.ui.* menjadi org.jfree.chart.ui.*.
 */

package grafikanalisa;

import fungsi.koneksiDB;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.text.TextBlock;
import org.jfree.text.TextLine;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

/**
 * @author Via (versi ringan)
 */
public class grafiksqlttv extends JDialog {

    // Jumlah data (kolom waktu pemeriksaan) per 1 halaman grafik.
    private static final int DATA_PER_HALAMAN = 20;

    // Font & warna dipakai ulang agar tidak membuat objek baru tiap kali menggambar.
    private static final Font FONT_SUMBU_JUDUL = new Font("Tahoma", Font.BOLD, 11);
    private static final Font FONT_SUMBU_TICK = new Font("Tahoma", Font.PLAIN, 10);
    private static final Font FONT_WAKTU_TICK = new Font("Tahoma", Font.PLAIN, 9);
    private static final Font FONT_MARKER = new Font("Tahoma", Font.PLAIN, 10);
    private static final Color WARNA_GRID = new Color(230, 230, 230);
    private static final Color WARNA_KOTAK = new Color(255, 255, 255, 235);
    private static final Color WARNA_KOTAK_TEPI = new Color(190, 190, 190);
    private static final Color WARNA_TENSI_TEPI = new Color(170, 120, 210);
    private static final Color WARNA_TENSI_TEKS = new Color(120, 60, 170);
    private static final Color WARNA_GARIS_PEMISAH = new Color(220, 220, 220);

    // ==================================================================
    //  KONFIGURASI PARAMETER VITAL (garis). Ubah rentang normal/warna/axis di sini.
    // ==================================================================
    private static final class VitalConfig {
        final String seriesName;
        final int columnIndex;   // kolom di ResultSet
        final String axisLabel;
        final Color warna;
        final double normalMin, normalMax;
        final double axisMin, axisMax, tickUnit;
        final double validMin;   // nilai di bawah ini dianggap data sampah -> diperlakukan kosong
        final DecimalFormat format;
        final double[] tampilkanTickSaja; // null = semua angka sumbu ditampilkan

        VitalConfig(String seriesName, int columnIndex, String axisLabel, Color warna,
                    double normalMin, double normalMax,
                    double axisMin, double axisMax, double tickUnit,
                    double validMin, String pattern, double[] tampilkanTickSaja) {
            this.seriesName = seriesName;
            this.columnIndex = columnIndex;
            this.axisLabel = axisLabel;
            this.warna = warna;
            this.normalMin = normalMin;
            this.normalMax = normalMax;
            this.axisMin = axisMin;
            this.axisMax = axisMax;
            this.tickUnit = tickUnit;
            this.validMin = validMin;
            this.format = new DecimalFormat(pattern);
            this.tampilkanTickSaja = tampilkanTickSaja;
        }
    }

    private static final VitalConfig[] VITALS = {
        new VitalConfig("Suhu", 3, "Suhu (°C)", new Color(0, 150, 80),
                36.0, 37.5, 30.0, 45.0, 5.0, 30.0, "0.0", null),
        new VitalConfig("Nadi", 4, "Nadi (/menit)", new Color(0, 120, 220),
                60, 100, 0, 200, 50.0, 20.0, "0", null),
        new VitalConfig("Respirasi", 5, "RR (/menit)", new Color(220, 40, 40),
                12, 20, 0, 80, 20.0, 3.0, "0", null),
        new VitalConfig("SpO2", 6, "SpO\u2082 (%)", new Color(255, 140, 0),
                95, 100, 70, 130, 10.0, 50.0, "0", new double[]{70, 90, 110, 130})
    };

    // Tensi: bukan garis, hanya teks "sistol/diastol" per waktu pemeriksaan.
    private static final class TensiConfig {
        final String seriesName = "Tensi";
        final int kolomTensi;
        final String axisLabel;

        TensiConfig(int kolomTensi, String axisLabel) {
            this.kolomTensi = kolomTensi;
            this.axisLabel = axisLabel;
        }
    }

    private static final TensiConfig TENSI = new TensiConfig(7, "Tensi (mmHg)");

    // ==================================================================
    //  FILTER PENGINPUT SOAP (perawat + dokter umum)
    //  Kolom ke-8 query = NIP penginput. Baris tanpa NIP dilewati.
    // ==================================================================
    private static final int KOLOM_NIP = 8;
    private static final String JABATAN_DOKTER_UMUM = "dokter umum"; // LIKE '%...%' pada pegawai.jbtn
    private static final String SPESIALIS_UMUM = "umum";             // = spesialis.nm_sps

    // Daftar NIP jarang berubah, jadi disimpan sementara agar membuka grafik berikutnya
    // tidak perlu query ulang. Perubahan data pegawai baru terbaca setelah masa cache habis.
    private static final long MASA_CACHE_MS = 10 * 60 * 1000L;
    private static Set<String> cacheNip;
    private static long waktuCache;

    private static synchronized Set<String> ambilNipDiizinkan() {
        long sekarang = System.currentTimeMillis();
        if (cacheNip != null && sekarang - waktuCache < MASA_CACHE_MS) {
            return cacheNip;
        }
        Set<String> baru = new HashSet<>(512);

        // 1. Petugas/perawat (kecuali yang kodenya juga terdaftar sebagai dokter)
        muatNip(baru, "SELECT petugas.nip FROM petugas "
                + "WHERE NOT EXISTS (SELECT 1 FROM dokter WHERE dokter.kd_dokter=petugas.nip)", null);
        // 2. Dokter umum lewat jabatan pegawai
        muatNip(baru, "SELECT dokter.kd_dokter FROM dokter "
                + "INNER JOIN pegawai ON pegawai.nik=dokter.kd_dokter "
                + "WHERE LOWER(pegawai.jbtn) LIKE ?", "%" + JABATAN_DOKTER_UMUM.toLowerCase() + "%");
        // 3. Dokter umum lewat nama spesialis
        muatNip(baru, "SELECT dokter.kd_dokter FROM dokter "
                + "INNER JOIN spesialis ON spesialis.kd_sps=dokter.kd_sps "
                + "WHERE LOWER(spesialis.nm_sps)=?", SPESIALIS_UMUM.toLowerCase());

        // Hasil kosong (mis. semua query gagal) tidak disimpan supaya dicoba lagi lain kali.
        if (!baru.isEmpty()) {
            cacheNip = baru;
            waktuCache = sekarang;
        }
        return baru;
    }

    // Tiap query terpisah supaya kalau satu gagal (mis. tabel berbeda) yang lain tetap jalan.
    private static void muatNip(Set<String> tujuan, String sql, String parameter) {
        try (PreparedStatement ps = koneksiDB.condb().prepareStatement(sql)) {
            if (parameter != null) {
                ps.setString(1, parameter);
            }
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    String nip = r.getString(1);
                    if (nip != null) {
                        tujuan.add(nip.trim());
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    // ==================================================================
    //  DATA — dibaca SEKALI dari database, lalu dipakai semua subplot & semua halaman.
    // ==================================================================
    private static final class Baris {
        final String waktu;   // "tanggal\njam"
        final double[] nilai; // sesuai urutan VITALS; NaN = kosong/tidak valid
        final String tensi;   // null = kosong

        Baris(String waktu, double[] nilai, String tensi) {
            this.waktu = waktu;
            this.nilai = nilai;
            this.tensi = tensi;
        }
    }

    private static List<Baris> muatData(String query) {
        List<Baris> hasil = new ArrayList<>();
        Set<String> nipOk = ambilNipDiizinkan(); // dimuat dulu, sebelum query utama dibuka

        try (Statement stat = koneksiDB.condb().createStatement();
             ResultSet rs = stat.executeQuery(query)) {

            boolean pakaiFilter = rs.getMetaData().getColumnCount() >= KOLOM_NIP;
            if (!pakaiFilter) {
                System.out.println("PERINGATAN grafiksqlttv: query tidak punya kolom ke-" + KOLOM_NIP
                        + " (NIP penginput SOAP). Filter TIDAK aktif.");
            }

            while (rs.next()) {
                if (pakaiFilter) {
                    String nip = rs.getString(KOLOM_NIP);
                    if (nip == null || !nipOk.contains(nip.trim())) {
                        continue; // bukan perawat/dokter umum
                    }
                }
                double[] nilai = new double[VITALS.length];
                for (int i = 0; i < VITALS.length; i++) {
                    double v = rs.getDouble(VITALS[i].columnIndex);
                    nilai[i] = (rs.wasNull() || v < VITALS[i].validMin) ? Double.NaN : v;
                }
                String tensi = rs.getString(TENSI.kolomTensi);
                if (tensi != null) {
                    tensi = tensi.trim();
                    if (tensi.isEmpty()) {
                        tensi = null;
                    }
                }
                hasil.add(new Baris(rs.getString(1) + "\n" + rs.getString(2), nilai, tensi));
            }
        } catch (SQLException e) {
            System.out.println("Notifikasi : " + e);
        }
        return hasil;
    }

    // ==================================================================
    //  RENDERER GARIS: warna tetap per parameter, label nilai dalam kotak putih rounded
    // ==================================================================
    private static final class ModernVitalRenderer extends LineAndShapeRenderer {

        ModernVitalRenderer(final VitalConfig cfg) {
            super(true, true);

            setSeriesPaint(0, cfg.warna);
            setSeriesStroke(0, new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            setBaseShapesVisible(true);
            setBaseShapesFilled(true);
            setSeriesShape(0, new Ellipse2D.Double(-5.5, -5.5, 11, 11));

            setBaseItemLabelsVisible(true);
            setBaseItemLabelGenerator(new CategoryItemLabelGenerator() {
                @Override
                public String generateRowLabel(CategoryDataset dataset, int row) {
                    return String.valueOf(dataset.getRowKey(row));
                }
                @Override
                public String generateColumnLabel(CategoryDataset dataset, int column) {
                    return String.valueOf(dataset.getColumnKey(column));
                }
                @Override
                public String generateLabel(CategoryDataset dataset, int row, int column) {
                    Number v = dataset.getValue(row, column);
                    return (v == null) ? null : cfg.format.format(v);
                }
            });
        }

        @Override
        protected void drawItemLabel(Graphics2D g2, PlotOrientation orientation,
                CategoryDataset dataset, int row, int column,
                double x, double y, boolean negative) {

            CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column);
            if (generator == null) {
                return;
            }
            String label = generator.generateLabel(dataset, row, column);
            if (label == null) {
                return;
            }

            g2.setFont(getItemLabelFont(row, column));
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(label);
            int textHeight = fm.getHeight();

            double boxX = x - (textWidth / 2.0) - 4;
            double boxY = y - textHeight - 10;
            RoundRectangle2D box = new RoundRectangle2D.Double(
                    boxX, boxY, textWidth + 8, textHeight + 4, 6, 6);

            g2.setColor(WARNA_KOTAK);
            g2.fill(box);
            g2.setColor(WARNA_KOTAK_TEPI);
            g2.draw(box);

            Paint itemPaint = getItemPaint(row, column);
            g2.setColor(itemPaint instanceof Color ? (Color) itemPaint : Color.BLACK);
            g2.drawString(label, (float) (boxX + 4), (float) (boxY + textHeight - fm.getDescent()));
        }
    }

    // Renderer Tensi: hanya menggambar teks di tengah tiap kolom kategori.
    private static final class TensiTextRenderer extends AbstractCategoryItemRenderer {

        private final Map<String, String> labelMap;

        TensiTextRenderer(Map<String, String> labelMap) {
            this.labelMap = labelMap;
        }

        @Override
        public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea,
                CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis,
                CategoryDataset dataset, int row, int column, int pass) {

            String label = labelMap.get(String.valueOf(dataset.getColumnKey(column)));
            if (label == null) {
                return;
            }

            double x = domainAxis.getCategoryMiddle(column, dataset.getColumnCount(), dataArea,
                    plot.getDomainAxisEdge());
            double y = dataArea.getCenterY();

            g2.setFont(FONT_SUMBU_JUDUL);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(label);
            int textHeight = fm.getHeight();

            double boxX = x - (textWidth / 2.0) - 6;
            double boxY = y - (textHeight / 2.0) - 2;
            RoundRectangle2D box = new RoundRectangle2D.Double(
                    boxX, boxY, textWidth + 12, textHeight + 4, 8, 8);

            g2.setColor(WARNA_KOTAK);
            g2.fill(box);
            g2.setColor(WARNA_TENSI_TEPI);
            g2.draw(box);

            g2.setColor(WARNA_TENSI_TEKS);
            g2.drawString(label, (float) (boxX + 6), (float) (boxY + textHeight - fm.getDescent()));
        }
    }

    // ==================================================================
    //  TOOLTIP
    // ==================================================================
    private static CategoryToolTipGenerator buildTooltip(final VitalConfig cfg) {
        return new CategoryToolTipGenerator() {
            @Override
            public String generateToolTip(CategoryDataset dataset, int row, int column) {
                Number value = dataset.getValue(row, column);
                String waktu = String.valueOf(dataset.getColumnKey(column)).replace("\n", " ");
                return "<html><b>" + cfg.seriesName + "</b><br>" + waktu + "<br>"
                        + (value == null ? "<i>Tidak ada data</i>"
                                : "Nilai: <b>" + cfg.format.format(value) + "</b>")
                        + "</html>";
            }
        };
    }

    private static CategoryToolTipGenerator buildTooltipTensi(final Map<String, String> labelMap) {
        return new CategoryToolTipGenerator() {
            @Override
            public String generateToolTip(CategoryDataset dataset, int row, int column) {
                String kategori = String.valueOf(dataset.getColumnKey(column));
                String nilai = labelMap.get(kategori);
                return "<html><b>" + TENSI.seriesName + "</b><br>" + kategori.replace("\n", " ") + "<br>"
                        + (nilai == null ? "<i>Tidak ada data</i>" : "Nilai: <b>" + nilai + "</b>")
                        + "</html>";
            }
        };
    }

    // ==================================================================
    //  SUBPLOT
    // ==================================================================
    private static CategoryPlot createSubplot(List<Baris> data, int awal, int akhir, int idx) {
        VitalConfig cfg = VITALS[idx];

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = awal; i < akhir; i++) {
            Baris b = data.get(i);
            double v = b.nilai[idx];
            // null -> garis putus (tidak ada titik palsu)
            dataset.addValue(Double.isNaN(v) ? null : Double.valueOf(v), cfg.seriesName, b.waktu);
        }

        NumberAxis rangeAxis = new NumberAxis(cfg.axisLabel);
        rangeAxis.setTickUnit(new NumberTickUnit(cfg.tickUnit));
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setRange(cfg.axisMin, cfg.axisMax);
        // Normal yang menyentuh batas atas axis (mis. SpO2) butuh margin atas ekstra
        // supaya kotak label tidak terpotong.
        rangeAxis.setUpperMargin(cfg.normalMax >= cfg.axisMax ? 0.45 : 0.30);
        rangeAxis.setLowerMargin(0.05);

        if (cfg.tampilkanTickSaja != null) {
            final Set<Long> boleh = new HashSet<>();
            for (double v : cfg.tampilkanTickSaja) {
                boleh.add(Math.round(v));
            }
            rangeAxis.setNumberFormatOverride(new DecimalFormat("0") {
                @Override
                public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
                    return boleh.contains(Math.round(number))
                            ? super.format(number, toAppendTo, pos) : toAppendTo;
                }
            });
        }

        ModernVitalRenderer renderer = new ModernVitalRenderer(cfg);
        renderer.setBaseToolTipGenerator(buildTooltip(cfg));

        CategoryPlot subplot = new CategoryPlot(dataset, null, rangeAxis, renderer);
        subplot.setBackgroundPaint(Color.WHITE);
        subplot.setRangeGridlinePaint(WARNA_GRID);
        subplot.setDomainGridlinePaint(WARNA_GRID);
        subplot.setDomainGridlinesVisible(true);
        subplot.setOutlineVisible(false);

        IntervalMarker normalRange = new IntervalMarker(
                cfg.normalMin, cfg.normalMax, new Color(180, 255, 180, 90));
        normalRange.setLabel("Normal");
        normalRange.setLabelFont(FONT_MARKER);
        normalRange.setLabelPaint(new Color(0, 130, 0));
        normalRange.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        normalRange.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        subplot.addRangeMarker(normalRange, Layer.BACKGROUND);

        return subplot;
    }

    private static CategoryPlot createSubplotTensi(List<Baris> data, int awal, int akhir) {
        // Dataset hanya penyelaras kategori waktu; nilai teks ada di labelMap.
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, String> labelMap = new HashMap<>();
        for (int i = awal; i < akhir; i++) {
            Baris b = data.get(i);
            dataset.addValue(1.0, TENSI.seriesName, b.waktu);
            if (b.tensi != null) {
                labelMap.put(b.waktu, b.tensi);
            }
        }

        NumberAxis rangeAxis = new NumberAxis(TENSI.axisLabel);
        rangeAxis.setRange(0, 2);
        rangeAxis.setTickLabelsVisible(false);
        rangeAxis.setTickMarksVisible(false);
        rangeAxis.setTickMarkPaint(Color.WHITE);
        rangeAxis.setLabelFont(FONT_SUMBU_JUDUL);
        rangeAxis.setTickLabelFont(FONT_SUMBU_TICK);

        TensiTextRenderer renderer = new TensiTextRenderer(labelMap);
        renderer.setBaseToolTipGenerator(buildTooltipTensi(labelMap));

        CategoryPlot subplot = new CategoryPlot(dataset, null, rangeAxis, renderer);
        subplot.setBackgroundPaint(Color.WHITE);
        subplot.setRangeGridlinesVisible(false);
        subplot.setDomainGridlinePaint(WARNA_GRID);
        subplot.setDomainGridlinesVisible(true);
        subplot.setOutlineVisible(false);
        return subplot;
    }

    // Sumbu waktu: label dipaksa 2 baris (tanggal di atas, jam di bawah).
    private static final class TwoLineCategoryAxis extends CategoryAxis {
        TwoLineCategoryAxis(String label) {
            super(label);
        }

        @Override
        protected TextBlock createLabel(Comparable category, float width,
                RectangleEdge edge, Graphics2D g2) {
            TextBlock block = new TextBlock();
            Font font = getTickLabelFont(category);
            for (String line : category.toString().split("\n")) {
                block.addLine(new TextLine(line, font, Color.BLACK));
            }
            return block;
        }
    }

    // ==================================================================
    //  CHART GABUNGAN — menggambar baris [awal, awal+jumlah) dari data yang sudah dimuat
    // ==================================================================
    private static JFreeChart createChart(List<Baris> data, int awal, int jumlah) {
        int akhir = (int) Math.min((long) awal + jumlah, data.size());
        awal = Math.min(awal, akhir);

        CategoryAxis domainAxis = new TwoLineCategoryAxis("Waktu Pemeriksaan");
        domainAxis.setLabelFont(FONT_SUMBU_JUDUL);
        domainAxis.setTickLabelFont(FONT_WAKTU_TICK);

        CombinedDomainCategoryPlot plot = new CombinedDomainCategoryPlot(domainAxis);
        for (int i = 0; i < VITALS.length; i++) {
            plot.add(createSubplot(data, awal, akhir, i), 3);
        }
        plot.add(createSubplotTensi(data, awal, akhir), 2);
        plot.setGap(24);
        plot.setBackgroundPaint(Color.WHITE);

        JFreeChart chart = new JFreeChart("", new Font("Tahoma", Font.BOLD, 14), plot, true);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        return chart;
    }

    /**
     * Grafik sebagai gambar (untuk disisipkan ke laporan JasperReports).
     * Parameter kolom..kolom4 dipertahankan demi kompatibilitas, tidak dipakai.
     *
     *   BufferedImage grafik = grafiksqlttv.createChartImage(query, k, k2, k3, k4, 800, 600);
     *   params.put("grafikTTV", grafik);   // di .jrxml: parameter class java.awt.Image
     */
    public static BufferedImage createChartImage(String query, String kolom, String kolom2, String kolom3, String kolom4,
            int width, int height) {
        return createChart(muatData(query), 0, Integer.MAX_VALUE).createBufferedImage(width, height);
    }

    /** Panel untuk dipakai di form lain (menampilkan semua data). */
    public static JPanel createDemoPanel(String query, String kolom, String kolom2, String kolom3, String kolom4) {
        ChartPanel panel = new ChartPanel(createChart(muatData(query), 0, Integer.MAX_VALUE));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    // ==================================================================
    //  DATA PASIEN (tabel kamar_inap, skema Khanza umum)
    // ==================================================================
    private static final class DataPasien {
        String nama = "", noRM = "", noRawat = "", umur = "", jenisKelamin = "", ruangan = "", dpjp = "";
    }

    private static DataPasien ambilDataPasien(String noRawat) {
        DataPasien data = new DataPasien();
        data.noRawat = noRawat;

        String sql =
                "SELECT pasien.nm_pasien, reg_periksa.no_rkm_medis, "
              + "       concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) AS umur, "
              + "       pasien.jk, "
              + "       concat(kamar_inap.kd_kamar,' ',bangsal.nm_bangsal) AS ruangan, "
              + "       dokter.nm_dokter "
              + "FROM kamar_inap "
              + "INNER JOIN reg_periksa ON kamar_inap.no_rawat = reg_periksa.no_rawat "
              + "INNER JOIN pasien      ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
              + "INNER JOIN kamar       ON kamar_inap.kd_kamar = kamar.kd_kamar "
              + "INNER JOIN bangsal     ON kamar.kd_bangsal = bangsal.kd_bangsal "
              + "INNER JOIN dokter      ON reg_periksa.kd_dokter = dokter.kd_dokter "
              + "WHERE kamar_inap.no_rawat = ? "
              + "LIMIT 1";

        try (PreparedStatement ps = koneksiDB.condb().prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data.nama = safe(rs.getString("nm_pasien"));
                    data.noRM = safe(rs.getString("no_rkm_medis"));
                    data.umur = safe(rs.getString("umur"));
                    String jk = rs.getString("jk");
                    data.jenisKelamin = "L".equalsIgnoreCase(jk) ? "Laki-laki"
                            : "P".equalsIgnoreCase(jk) ? "Perempuan" : "";
                    data.ruangan = safe(rs.getString("ruangan"));
                    data.dpjp = safe(rs.getString("nm_dokter"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Notifikasi (ambil data pasien dari kamar_inap) : " + e);
        }
        return data;
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    /**
     * Cara termudah: cukup kasih no_rawat, identitas pasien diambil otomatis.
     *   grafiksqlttv dlg = grafiksqlttv.bukaDenganNoRawat(noRawat, query, kolom, kolom2, kolom3, kolom4);
     *   dlg.setVisible(true);
     */
    public static grafiksqlttv bukaDenganNoRawat(String noRawat, String query,
            String kolom, String kolom2, String kolom3, String kolom4) {
        DataPasien dp = ambilDataPasien(noRawat);
        return new grafiksqlttv(dp.nama, dp.noRM, dp.noRawat, dp.umur, dp.jenisKelamin, dp.ruangan, dp.dpjp,
                query, kolom, kolom2, kolom3, kolom4);
    }

    /** Konstruktor lama (kompatibel): identitas pasien kosong kecuali title dipakai sebagai nama. */
    public grafiksqlttv(String title, String query, String kolom, String kolom2, String kolom3, String kolom4) {
        this(title, "", "", "", "", "", "", query, kolom, kolom2, kolom3, kolom4);
    }

    /** Konstruktor lengkap dengan identitas pasien di header. */
    public grafiksqlttv(String namaPasien, String noRM, String noRawat, String umur, String jenisKelamin,
            String ruangan, String dpjp,
            String query, String kolom, String kolom2, String kolom3, String kolom4) {

        setTitle("Grafik TTV - " + namaPasien);
        setUndecorated(true); // header custom di bawah menggantikan title bar OS

        // Data dibaca SEKALI; pindah halaman hanya mengambil potongan dari memori.
        final List<Baris> data = muatData(query);
        final int totalData = data.size();
        final int totalHalaman = Math.max(1, (int) Math.ceil(totalData / (double) DATA_PER_HALAMAN));
        final int[] halamanAktif = {0};

        final ChartPanel chartPanel = new ChartPanel(createChart(data, 0, DATA_PER_HALAMAN));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setMinimumDrawWidth(0);
        chartPanel.setMinimumDrawHeight(0);
        chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);

        // ---------- HEADER: judul + tombol tutup ----------
        JButton tombolTutup = new JButton("\u2715 Tutup");
        tombolTutup.setFocusPainted(false);
        tombolTutup.addActionListener(e -> dispose());

        JLabel judul = new JLabel("GRAFIK TANDA TANDA VITAL", SwingConstants.CENTER);
        judul.setFont(new Font("Tahoma", Font.BOLD, 18));
        judul.setForeground(new Color(40, 40, 40));

        JPanel judulBar = new JPanel(new BorderLayout());
        judulBar.setBackground(Color.WHITE);
        judulBar.add(judul, BorderLayout.CENTER);
        judulBar.add(tombolTutup, BorderLayout.EAST);
        judulBar.setBorder(BorderFactory.createEmptyBorder(10, 12, 4, 12));

        // ---------- HEADER: identitas pasien, 2 kolom ----------
        JPanel jkUmur = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        jkUmur.setBackground(Color.WHITE);
        jkUmur.add(buatBarisIdentitas("Jenis Kelamin", jenisKelamin));
        jkUmur.add(buatBarisIdentitas("Umur", umur));

        JPanel kolomKiri = buatKolom(
                buatBarisIdentitas("Nama Pasien", namaPasien),
                buatBarisIdentitas("No RM", noRM),
                buatBarisIdentitas("No Rawat", noRawat));
        JPanel kolomKanan = buatKolom(
                jkUmur,
                buatBarisIdentitas("DPJP", dpjp),
                buatBarisIdentitas("Ruangan", ruangan));

        JPanel identitasPanel = new JPanel();
        identitasPanel.setLayout(new BoxLayout(identitasPanel, BoxLayout.X_AXIS));
        identitasPanel.setBackground(Color.WHITE);
        identitasPanel.setBorder(BorderFactory.createEmptyBorder(2, 12, 8, 12));
        identitasPanel.add(kolomKiri);
        identitasPanel.add(Box.createHorizontalStrut(70));
        identitasPanel.add(kolomKanan);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.add(judulBar, BorderLayout.NORTH);
        topBar.add(identitasPanel, BorderLayout.SOUTH);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, WARNA_GARIS_PEMISAH));

        // Satu listener di topBar cukup: komponen anak tanpa listener meneruskan event ke induknya.
        MouseAdapter penggeser = new MouseAdapter() {
            private Point titikAwal;

            @Override
            public void mousePressed(MouseEvent e) {
                titikAwal = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (titikAwal == null) {
                    return;
                }
                Point p = e.getLocationOnScreen();
                setLocation(p.x - titikAwal.x, p.y - titikAwal.y);
            }
        };
        topBar.addMouseListener(penggeser);
        topBar.addMouseMotionListener(penggeser);

        // ---------- NAVIGASI HALAMAN ----------
        final JButton tombolSebelumnya = new JButton("\u25C0 Sebelumnya");
        final JButton tombolBerikutnya = new JButton("Berikutnya \u25B6");
        tombolSebelumnya.setFocusPainted(false);
        tombolBerikutnya.setFocusPainted(false);
        final JLabel infoHalaman = new JLabel("", SwingConstants.CENTER);
        infoHalaman.setFont(new Font("Tahoma", Font.BOLD, 13));

        final Runnable muatHalaman = () -> {
            int awal = halamanAktif[0] * DATA_PER_HALAMAN;
            chartPanel.setChart(createChart(data, awal, DATA_PER_HALAMAN));
            int dari = totalData == 0 ? 0 : awal + 1;
            int sampai = Math.min(awal + DATA_PER_HALAMAN, totalData);
            infoHalaman.setText("Halaman " + (halamanAktif[0] + 1) + " / " + totalHalaman
                    + "   (data " + dari + " - " + sampai + " dari " + totalData + ")");
            tombolSebelumnya.setEnabled(halamanAktif[0] > 0);
            tombolBerikutnya.setEnabled(halamanAktif[0] < totalHalaman - 1);
        };
        muatHalaman.run();

        tombolSebelumnya.addActionListener(e -> {
            if (halamanAktif[0] > 0) {
                halamanAktif[0]--;
                muatHalaman.run();
            }
        });
        tombolBerikutnya.addActionListener(e -> {
            if (halamanAktif[0] < totalHalaman - 1) {
                halamanAktif[0]++;
                muatHalaman.run();
            }
        });

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        navBar.setBackground(Color.WHITE);
        navBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, WARNA_GARIS_PEMISAH));
        navBar.add(tombolSebelumnya);
        navBar.add(infoHalaman);
        navBar.add(tombolBerikutnya);

        JPanel root = new JPanel(new BorderLayout());
        root.add(topBar, BorderLayout.NORTH);
        root.add(chartPanel, BorderLayout.CENTER);
        root.add(navBar, BorderLayout.SOUTH);
        setContentPane(root);

        // ESC = tutup, panah kiri/kanan = pindah halaman.
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> tombolSebelumnya.doClick(),
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> tombolBerikutnya.doClick(),
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Ukuran proporsional terhadap layar (92%, dengan batas atas), di tengah layar.
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min((int) (screen.width * 0.92), 1500), Math.min((int) (screen.height * 0.92), 950));
        setLocationRelativeTo(null);

        setModal(true);
        URL ikon = getClass().getResource("/picture/addressbook-edit24.png");
        if (ikon != null) {
            setIconImage(new ImageIcon(ikon).getImage());
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    // Satu baris identitas: label tebal + nilai biasa, tanpa HTML.
    private static JPanel buatBarisIdentitas(String label, String nilai) {
        JPanel baris = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        baris.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(label + " : ");
        lbl.setFont(new Font("Tahoma", Font.BOLD, 13));
        JLabel val = new JLabel((nilai == null || nilai.isEmpty()) ? "-" : nilai);
        val.setFont(new Font("Tahoma", Font.PLAIN, 13));

        baris.add(lbl);
        baris.add(val);
        return baris;
    }

    private static JPanel buatKolom(JComponent... baris) {
        JPanel kolom = new JPanel();
        kolom.setLayout(new BoxLayout(kolom, BoxLayout.Y_AXIS));
        kolom.setBackground(Color.WHITE);
        kolom.setAlignmentY(Component.TOP_ALIGNMENT);
        for (JComponent b : baris) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            kolom.add(b);
        }
        return kolom;
    }
}
