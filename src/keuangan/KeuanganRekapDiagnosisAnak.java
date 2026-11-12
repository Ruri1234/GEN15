package keuangan;

import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * Rekap pasien Poli Anak dengan diagnosis E44.0, E34.3 dan E43.
 * Filter : dokter, bulan, tahun (bulan "Semua Bulan" = rekap satu tahun penuh).
 * Tab Detail dan Grafik, dapat disimpan ke PDF dan Excel.
 */
public final class KeuanganRekapDiagnosisAnak extends JDialog {

    private static final Color PRIMARY = new Color(26, 76, 140);
    private static final Color PRIMARY_DARK = new Color(12, 38, 84);
    private static final Color BG = new Color(240, 244, 250);
    private static final Color TEXT = new Color(30, 40, 60);
    private static final Color MUTED = new Color(100, 115, 140);
    private static final Color LINE = new Color(215, 223, 235);
    private static final Color[] PALET = {
        new Color(26, 76, 140), new Color(55, 138, 221), new Color(216, 120, 48), new Color(127, 119, 221)};

    private static final String[] KODE = {"E44.0", "E34.3", "E43"};
    private static final String[] BULAN = {"Semua Bulan", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
    private static final String[] BULAN_PENDEK = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep",
        "Okt", "Nov", "Des"};
    private static final String[] HEADER = {"No.", "Tgl. Berobat", "No.RM", "Nama Pasien", "Usia", "BB (kg)",
        "TB (cm)", "Diagnosis", "Diagnosis Lain", "Terapi", "Asal Rujukan", "Dokter"};
    private static final int[] LEBAR_TABEL = {40, 90, 80, 170, 75, 65, 65, 300, 220, 300, 160, 200};
    private static final float[] LEBAR_PDF = {20, 48, 44, 86, 36, 26, 26, 100, 120, 110, 68, 102};
    private static final double[] LEBAR_XLSX = {6, 13, 11, 28, 11, 9, 9, 38, 45, 50, 26, 28};

    private final Connection koneksi = koneksiDB.condb();
    private final DefaultTableModel tabModel;
    private final JTable tbDetail;
    private final JLabel lblInfo = new JLabel(" ");
    private final JComboBox<Item> cmbDokter = widget("widget.ComboBox", new JComboBox<Item>());
    private final JComboBox<String> cmbBulan = widget("widget.ComboBox", new JComboBox<String>());
    private final JComboBox<Integer> cmbTahun = widget("widget.ComboBox", new JComboBox<Integer>());
    private final StatCard cardTotal = new StatCard("Total Kunjungan", PRIMARY);
    private final StatCard[] cardKode = {
        new StatCard("E44.0", PALET[1]), new StatCard("E34.3", PALET[2]), new StatCard("E43", PALET[3])};
    private final BarChartPanel chartDiagnosis = new BarChartPanel("Kunjungan per Diagnosis", true);
    private final BarChartPanel chartUsia = new BarChartPanel("Kunjungan per Kelompok Usia", true);
    private final BarChartPanel chartTren = new BarChartPanel("Kunjungan per Tanggal", false);

    private volatile boolean siap = false;
    private int versi = 0;
    private Hasil hasil = new Hasil();

    /** Item combo dokter. */
    private static final class Item {
        final String kode, nama;
        Item(String kode, String nama) { this.kode = kode; this.nama = nama; }
        @Override public String toString() { return nama; }
    }

    /** Hasil query. */
    private static final class Hasil {
        final List<Object[]> baris = new ArrayList<>();
        final Map<String, Integer> perDiagnosis = new LinkedHashMap<>();
        final Map<String, Integer> perUsia = new LinkedHashMap<>();
        final Map<String, Integer> tren = new LinkedHashMap<>();
        int totalKunjungan = 0;
        String judulTren = "Kunjungan per Tanggal";
    }

    public KeuanganRekapDiagnosisAnak(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Rekap Diagnosis E44.0, E34.3, E43 - Poli Anak ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ---------- tabel
        tabModel = new DefaultTableModel(HEADER, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbDetail = new JTable(tabModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(246, 249, 253));
                    c.setForeground(TEXT);
                }
                return c;
            }
            @Override
            public String getToolTipText(MouseEvent e) {
                int r = rowAtPoint(e.getPoint()), c = columnAtPoint(e.getPoint());
                if (r < 0 || c < 0) return null;
                Object v = getValueAt(r, c);
                if (v == null || v.toString().trim().isEmpty()) return null;
                String t = v.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                return "<html><body style='width:320px'>" + t + "</body></html>";
            }
        };
        tbDetail.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbDetail.setRowHeight(28);
        tbDetail.setShowVerticalLines(false);
        tbDetail.setShowHorizontalLines(true);
        tbDetail.setGridColor(LINE);
        tbDetail.setIntercellSpacing(new Dimension(0, 1));
        tbDetail.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tbDetail.setSelectionBackground(new Color(205, 222, 245));
        tbDetail.setSelectionForeground(TEXT);
        tbDetail.setFillsViewportHeight(true);
        JTableHeader th = tbDetail.getTableHeader();
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(100, 32));
        th.setDefaultRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = new JLabel(v == null ? "" : v.toString(), JLabel.CENTER);
                l.setOpaque(true);
                l.setBackground(PRIMARY);
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Tahoma", Font.BOLD, 12));
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, PRIMARY_DARK));
                return l;
            }
        });
        for (int i = 0; i < LEBAR_TABEL.length; i++) {
            tbDetail.getColumnModel().getColumn(i).setPreferredWidth(LEBAR_TABEL[i]);
            boolean tengah = (i <= 2 || i == 4 || i == 5 || i == 6);
            tbDetail.getColumnModel().getColumn(i).setCellRenderer(new SelRenderer(tengah));
        }
        JScrollPane sp = new JScrollPane(tbDetail);
        sp.setBorder(BorderFactory.createLineBorder(LINE));
        sp.getViewport().setBackground(Color.WHITE);
        JPanel panelDetail = new JPanel(new BorderLayout());
        panelDetail.setBackground(BG);
        panelDetail.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        panelDetail.add(sp, BorderLayout.CENTER);

        // ---------- grafik
        JPanel atas = new JPanel(new GridLayout(1, 2, 10, 0));
        atas.setOpaque(false);
        atas.add(chartDiagnosis);
        atas.add(chartUsia);
        JPanel panelGrafik = new JPanel(new GridLayout(2, 1, 0, 10));
        panelGrafik.setBackground(BG);
        panelGrafik.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        panelGrafik.add(atas);
        panelGrafik.add(chartTren);

        JTabbedPane tab = widget("widget.TabPane", new JTabbedPane());
        tab.setFont(new Font("Tahoma", Font.BOLD, 12));
        tab.addTab("Detail", panelDetail);
        tab.addTab("Grafik", panelGrafik);

        // ---------- header, filter, kartu ringkasan
        JPanel judul = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        judul.setPreferredSize(new Dimension(100, 62));
        judul.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        JLabel t1 = new JLabel("Rekap Diagnosis Poli Anak");
        t1.setFont(new Font("Tahoma", Font.BOLD, 18));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel("Kode diagnosis E44.0  \u2022  E34.3  \u2022  E43");
        t2.setFont(new Font("Tahoma", Font.PLAIN, 12));
        t2.setForeground(new Color(190, 210, 240));
        JPanel tt = new JPanel(new GridLayout(2, 1));
        tt.setOpaque(false);
        tt.add(t1);
        tt.add(t2);
        judul.add(tt, BorderLayout.CENTER);

        cmbBulan.setModel(new javax.swing.DefaultComboBoxModel<>(BULAN));
        cmbDokter.addItem(new Item("", "Semua Dokter"));
        cmbDokter.setPreferredSize(new Dimension(260, 28));
        cmbBulan.setPreferredSize(new Dimension(130, 28));
        cmbTahun.setPreferredSize(new Dimension(80, 28));
        int tahunIni = LocalDate.now().getYear();
        for (int y = tahunIni; y >= tahunIni - 8; y--) cmbTahun.addItem(y);
        cmbBulan.setSelectedIndex(LocalDate.now().getMonthValue());
        cmbTahun.setSelectedItem(tahunIni);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        filter.setBackground(Color.WHITE);
        filter.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE));
        filter.add(label("Dokter"));
        filter.add(cmbDokter);
        filter.add(Box(12));
        filter.add(label("Bulan"));
        filter.add(cmbBulan);
        filter.add(label("Tahun"));
        filter.add(cmbTahun);

        JPanel kartu = new JPanel(new GridLayout(1, 4, 10, 0));
        kartu.setOpaque(false);
        kartu.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        kartu.add(cardTotal);
        for (StatCard c : cardKode) kartu.add(c);
        kartu.setPreferredSize(new Dimension(100, 76));

        JPanel utara = new JPanel();
        utara.setLayout(new BoxLayout(utara, BoxLayout.Y_AXIS));
        utara.setOpaque(false);
        judul.setAlignmentX(0f); filter.setAlignmentX(0f); kartu.setAlignmentX(0f);
        utara.add(judul);
        utara.add(filter);
        utara.add(kartu);

        // ---------- bawah
        lblInfo.setFont(new Font("Tahoma", Font.PLAIN, 12));
        lblInfo.setForeground(MUTED);
        RoundButton btnPdf = new RoundButton("Simpan PDF", new Color(192, 57, 43));
        RoundButton btnExcel = new RoundButton("Simpan Excel", new Color(33, 115, 70));
        RoundButton btnKeluar = new RoundButton("Keluar", new Color(100, 115, 140));
        btnPdf.addActionListener(e -> simpan(true));
        btnExcel.addActionListener(e -> simpan(false));
        btnKeluar.addActionListener(e -> dispose());
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(btnPdf);
        tombol.add(btnExcel);
        tombol.add(btnKeluar);
        JPanel bawah = new JPanel(new BorderLayout());
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, LINE),
                BorderFactory.createEmptyBorder(8, 14, 8, 10)));
        bawah.add(lblInfo, BorderLayout.CENTER);
        bawah.add(tombol, BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(utara, BorderLayout.NORTH);
        root.add(tab, BorderLayout.CENTER);
        root.add(bawah, BorderLayout.SOUTH);
        setContentPane(root);
        setSize(1000, 640);

        java.awt.event.ActionListener ganti = e -> { if (siap) muat(); };
        cmbDokter.addActionListener(ganti);
        cmbBulan.addActionListener(ganti);
        cmbTahun.addActionListener(ganti);

        muatDokter();
    }

    /**
     * Memakai komponen bergaya Mac bawaan SIMRS Khanza (widget.ComboBox, widget.TabPane).
     * Jika kelasnya tidak ditemukan, dipakai komponen Swing biasa sebagai cadangan.
     */
    @SuppressWarnings("unchecked")
    private static <T> T widget(String kelas, T cadangan) {
        try {
            Object o = Class.forName(kelas).getDeclaredConstructor().newInstance();
            if (cadangan.getClass().isInstance(o)) return (T) o;
        } catch (Throwable e) {
            // pakai cadangan
        }
        return cadangan;
    }

    private static JLabel label(String s) {
        JLabel l = new JLabel(s);
        l.setFont(new Font("Tahoma", Font.BOLD, 12));
        l.setForeground(TEXT);
        return l;
    }

    private static Component Box(int w) {
        return javax.swing.Box.createHorizontalStrut(w);
    }

    // ------------------------------------------------------------------ data
    private void muatDokter() {
        new SwingWorker<List<Item>, Void>() {
            @Override
            protected List<Item> doInBackground() throws Exception {
                List<Item> l = new ArrayList<>();
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "select distinct dokter.kd_dokter,dokter.nm_dokter from reg_periksa "
                        + "inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "
                        + "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "
                        + "where poliklinik.nm_poli like '%anak%' order by dokter.nm_dokter");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) l.add(new Item(rs.getString(1), rs.getString(2)));
                }
                return l;
            }
            @Override
            protected void done() {
                try {
                    for (Item i : get()) cmbDokter.addItem(i);
                } catch (Exception e) {
                    System.out.println("Notifikasi Dokter Rekap : " + e);
                }
                siap = true;
                muat();
            }
        }.execute();
    }

    private String namaPeriode() {
        int b = cmbBulan.getSelectedIndex();
        return b == 0 ? "Tahun " + cmbTahun.getSelectedItem() : BULAN[b] + " " + cmbTahun.getSelectedItem();
    }

    private void muat() {
        final int ver = ++versi;
        final int tahun = (Integer) cmbTahun.getSelectedItem();
        final int bulan = cmbBulan.getSelectedIndex();
        final Item dokter = (Item) cmbDokter.getSelectedItem();
        final LocalDate d1 = bulan == 0 ? LocalDate.of(tahun, 1, 1) : LocalDate.of(tahun, bulan, 1);
        final LocalDate d2 = bulan == 0 ? LocalDate.of(tahun, 12, 31) : YearMonth.of(tahun, bulan).atEndOfMonth();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblInfo.setText("Memuat data...");

        new SwingWorker<Hasil, Void>() {
            @Override
            protected Hasil doInBackground() throws Exception {
                Hasil h = new Hasil();
                for (String k : KODE) h.perDiagnosis.put(k, 0);
                h.perUsia.put("< 1 thn", 0);
                h.perUsia.put("1-4 thn", 0);
                h.perUsia.put("5-14 thn", 0);
                h.perUsia.put("> 14 thn", 0);
                if (bulan == 0) {
                    h.judulTren = "Kunjungan per Bulan";
                    for (String b : BULAN_PENDEK) h.tren.put(b, 0);
                } else {
                    h.judulTren = "Kunjungan per Tanggal";
                    for (int i = 1; i <= d2.getDayOfMonth(); i++) h.tren.put(String.valueOf(i), 0);
                }
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                Set<String> dilihat = new HashSet<>();

                final String kodeIn = "('E44.0','E34.3','E43')";
                final String dxDari = "from diagnosa_pasien dp inner join penyakit pk on pk.kd_penyakit=dp.kd_penyakit "
                        + "where dp.no_rawat=reg_periksa.no_rawat and dp.status='Ralan' ";
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,pasien.no_rkm_medis,pasien.nm_pasien,"
                        + "reg_periksa.umurdaftar,reg_periksa.sttsumur,dokter.nm_dokter,"
                        // diagnosis rekap (E44.0 / E34.3 / E43) pada kunjungan ini
                        + "(select group_concat(concat(dp.kd_penyakit,' - ',pk.nm_penyakit) order by dp.prioritas separator '; ') "
                        + dxDari + "and dp.kd_penyakit in " + kodeIn + ") as diagnosis,"
                        + "(select group_concat(dp.kd_penyakit separator ',') " + dxDari + "and dp.kd_penyakit in " + kodeIn + ") as kodes,"
                        // semua diagnosis lain pada kunjungan yang sama
                        + "(select group_concat(concat(dp.kd_penyakit,' - ',pk.nm_penyakit) order by dp.prioritas separator '; ') "
                        + dxDari + "and dp.kd_penyakit not in " + kodeIn + ") as diagnosis_lain,"
                        + "(select pemeriksaan_ralan.berat from pemeriksaan_ralan where pemeriksaan_ralan.no_rawat=reg_periksa.no_rawat "
                        + " and pemeriksaan_ralan.berat<>'' order by pemeriksaan_ralan.tgl_perawatan desc,pemeriksaan_ralan.jam_rawat desc limit 1) as berat,"
                        + "(select pemeriksaan_ralan.tinggi from pemeriksaan_ralan where pemeriksaan_ralan.no_rawat=reg_periksa.no_rawat "
                        + " and pemeriksaan_ralan.tinggi<>'' order by pemeriksaan_ralan.tgl_perawatan desc,pemeriksaan_ralan.jam_rawat desc limit 1) as tinggi,"
                        + "(select group_concat(concat(databarang.nama_brng,if(resep_dokter.aturan_pakai<>'',concat(' (',resep_dokter.aturan_pakai,')'),'')) separator ', ') "
                        + " from resep_obat inner join resep_dokter on resep_obat.no_resep=resep_dokter.no_resep "
                        + " inner join databarang on resep_dokter.kode_brng=databarang.kode_brng "
                        + " where resep_obat.no_rawat=reg_periksa.no_rawat) as terapi,"
                        + "(select rujuk_masuk.perujuk from rujuk_masuk where rujuk_masuk.no_rawat=reg_periksa.no_rawat limit 1) as rujukan "
                        + "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "
                        + "inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "
                        + "where reg_periksa.tgl_registrasi between ? and ? and poliklinik.nm_poli like '%anak%' "
                        + "and reg_periksa.kd_dokter like ? "
                        // kunjungan masuk rekap bila punya salah satu dari 3 diagnosis
                        + "and exists (select 1 from diagnosa_pasien x where x.no_rawat=reg_periksa.no_rawat "
                        + "and x.status='Ralan' and x.kd_penyakit in " + kodeIn + ") "
                        + "order by reg_periksa.tgl_registrasi,reg_periksa.no_reg")) {
                    ps.setString(1, d1.toString());
                    ps.setString(2, d2.toString());
                    ps.setString(3, dokter == null || dokter.kode.isEmpty() ? "%" : dokter.kode);
                    try (ResultSet rs = ps.executeQuery()) {
                        int no = 1;
                        while (rs.next()) {
                            LocalDate tgl = LocalDate.parse(rs.getString("tgl_registrasi").substring(0, 10));
                            int umur = rs.getInt("umurdaftar");
                            String satuan = rs.getString("sttsumur");
                            h.baris.add(new Object[]{
                                no++,
                                tgl.format(fmt),
                                rs.getString("no_rkm_medis"),
                                rs.getString("nm_pasien"),
                                umur + " " + satuan,
                                nvl(rs.getString("berat")),
                                nvl(rs.getString("tinggi")),
                                nvl(rs.getString("diagnosis")),
                                nvl(rs.getString("diagnosis_lain")),
                                nvl(rs.getString("terapi")),
                                nvl(rs.getString("rujukan")),
                                rs.getString("nm_dokter")
                            });
                            Set<String> kodeRow = new HashSet<>();
                            for (String k : nvl(rs.getString("kodes")).split(",")) kodeRow.add(k.trim());
                            for (String k : kodeRow) {
                                if (h.perDiagnosis.containsKey(k)) h.perDiagnosis.merge(k, 1, Integer::sum);
                            }
                            dilihat.add(rs.getString("no_rawat"));
                            h.perUsia.merge(kelompokUsia(umur, satuan), 1, Integer::sum);
                            String kunci = bulan == 0 ? BULAN_PENDEK[tgl.getMonthValue() - 1]
                                    : String.valueOf(tgl.getDayOfMonth());
                            h.tren.merge(kunci, 1, Integer::sum);
                        }
                    }
                }
                h.totalKunjungan = dilihat.size();
                return h;
            }

            @Override
            protected void done() {
                if (ver != versi) return;
                try {
                    hasil = get();
                    tabModel.setRowCount(0);
                    for (Object[] b : hasil.baris) tabModel.addRow(b);
                    cardTotal.setNilai(hasil.totalKunjungan);
                    for (int i = 0; i < KODE.length; i++) cardKode[i].setNilai(hasil.perDiagnosis.get(KODE[i]));
                    chartDiagnosis.setData(hasil.perDiagnosis);
                    chartUsia.setData(hasil.perUsia);
                    chartTren.setJudul(hasil.judulTren);
                    chartTren.setData(hasil.tren);
                    lblInfo.setText(namaPeriode() + "   \u2022   " + hasil.totalKunjungan + " kunjungan");
                } catch (Exception e) {
                    lblInfo.setText("Gagal memuat data");
                    System.out.println("Notifikasi Rekap Diagnosis : " + e);
                }
                setCursor(Cursor.getDefaultCursor());
            }
        }.execute();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String kelompokUsia(int umur, String satuan) {
        if ("Hr".equals(satuan) || "Bl".equals(satuan)) return "< 1 thn";
        if (umur <= 4) return "1-4 thn";
        if (umur <= 14) return "5-14 thn";
        return "> 14 thn";
    }

    // ------------------------------------------------------------------ export
    private void simpan(boolean pdf) {
        if (hasil.baris.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk disimpan pada periode ini.",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String ext = pdf ? ".pdf" : ".xlsx";
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(pdf ? "Simpan sebagai PDF" : "Simpan sebagai Excel");
        fc.setFileFilter(new FileNameExtensionFilter(pdf ? "File PDF (*.pdf)" : "File Excel (*.xlsx)", ext.substring(1)));
        fc.setSelectedFile(new File("Rekap_Diagnosis_Poli_Anak_" + namaPeriode().replace(' ', '_') + ext));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(ext)) f = new File(f.getParentFile(), f.getName() + ext);
        if (f.exists() && JOptionPane.showConfirmDialog(this, "File sudah ada. Timpa?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        String rs = "";
        try { rs = akses.getnamars(); } catch (Exception e) { /* abaikan */ }
        Item dk = (Item) cmbDokter.getSelectedItem();
        List<String> info = new ArrayList<>();
        if (rs != null && !rs.trim().isEmpty()) info.add(rs);
        info.add("Periode : " + namaPeriode() + "     Dokter : " + (dk == null ? "Semua Dokter" : dk.nama));
        info.add("Total kunjungan : " + hasil.totalKunjungan + "     E44.0 : " + hasil.perDiagnosis.get("E44.0")
                + "     E34.3 : " + hasil.perDiagnosis.get("E34.3") + "     E43 : " + hasil.perDiagnosis.get("E43"));
        String judulFile = "Rekap Diagnosis Poli Anak (E44.0, E34.3, E43)";
        try {
            if (pdf) {
                KeuanganRekapExport.simpanPdf(f, judulFile, info, HEADER, LEBAR_PDF, hasil.baris);
            } else {
                Map<String, Map<String, Integer>> ring = new LinkedHashMap<>();
                ring.put("Diagnosis", hasil.perDiagnosis);
                ring.put("Kelompok Usia", hasil.perUsia);
                ring.put(hasil.judulTren.replace("Kunjungan ", ""), hasil.tren);
                KeuanganRekapExport.simpanXlsx(f, judulFile, info, HEADER, LEBAR_XLSX, hasil.baris, ring);
            }
            if (JOptionPane.showConfirmDialog(this, "File berhasil disimpan:\n" + f.getAbsolutePath()
                    + "\n\nBuka file sekarang?", "Berhasil", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION
                    && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(f);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan file : " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ------------------------------------------------------------------ komponen
    private static final class SelRenderer extends DefaultTableCellRenderer {
        SelRenderer(boolean tengah) {
            setHorizontalAlignment(tengah ? CENTER : LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        }
    }

    private static final class RoundButton extends JButton {
        private final Color warna;
        private boolean hover;
        RoundButton(String teks, Color warna) {
            super(teks);
            this.warna = warna;
            setForeground(Color.WHITE);
            setFont(new Font("Tahoma", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(125, 34));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hover ? warna.darker() : warna);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class StatCard extends JPanel {
        private final JLabel lblNilai = new JLabel("0");
        private final Color warna;
        StatCard(String judul, Color warna) {
            this.warna = warna;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 10));
            JLabel l = new JLabel(judul);
            l.setFont(new Font("Tahoma", Font.PLAIN, 11));
            l.setForeground(MUTED);
            lblNilai.setFont(new Font("Tahoma", Font.BOLD, 22));
            lblNilai.setForeground(warna);
            add(l, BorderLayout.NORTH);
            add(lblNilai, BorderLayout.CENTER);
        }
        void setNilai(int n) { lblNilai.setText(String.valueOf(n)); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.setColor(LINE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.setColor(warna);
            g2.fillRoundRect(0, 8, 5, getHeight() - 16, 5, 5);
            g2.dispose();
        }
    }

    /** Grafik batang sederhana (tanpa library tambahan). */
    private static final class BarChartPanel extends JPanel {
        private String judul;
        private final boolean beragam;
        private Map<String, Integer> data = new LinkedHashMap<>();

        BarChartPanel(String judul, boolean beragam) {
            this.judul = judul;
            this.beragam = beragam;
            setOpaque(false);
            setPreferredSize(new Dimension(400, 260));
        }

        void setJudul(String j) { this.judul = j; }

        void setData(Map<String, Integer> d) {
            this.data = new LinkedHashMap<>(d);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setColor(Color.WHITE);
            g.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g.setColor(LINE);
            g.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            g.setFont(new Font("Tahoma", Font.BOLD, 13));
            g.setColor(TEXT);
            g.drawString(judul, 16, 26);

            int kiri = 44, kanan = 18, atas = 44, bawah = 34;
            int pw = w - kiri - kanan, ph = h - atas - bawah;
            int max = 0, total = 0;
            for (int v : data.values()) { max = Math.max(max, v); total += v; }
            g.setFont(new Font("Tahoma", Font.PLAIN, 11));
            FontMetrics fm = g.getFontMetrics();
            if (pw <= 0 || ph <= 0) { g.dispose(); return; }
            if (total == 0) {
                g.setColor(MUTED);
                String s = "Tidak ada data";
                g.drawString(s, (w - fm.stringWidth(s)) / 2, h / 2);
                g.dispose();
                return;
            }
            int skala = niceMax(max);
            int langkah = Math.max(1, (int) Math.ceil(skala / 5.0));
            for (int v = 0; v <= skala; v += langkah) {
                int y = atas + ph - (int) ((double) v / skala * ph);
                g.setColor(new Color(232, 238, 247));
                g.drawLine(kiri, y, kiri + pw, y);
                g.setColor(MUTED);
                String s = String.valueOf(v);
                g.drawString(s, kiri - 8 - fm.stringWidth(s), y + 4);
            }

            int n = data.size();
            int slot = pw / n;
            int lebarBar = Math.max(4, Math.min(64, (int) (slot * 0.62)));
            int maxLabel = 0;
            for (String k : data.keySet()) maxLabel = Math.max(maxLabel, fm.stringWidth(k));
            int step = Math.max(1, (int) Math.ceil((maxLabel + 6.0) / slot));
            int idx = 0;
            for (Map.Entry<String, Integer> e : data.entrySet()) {
                int tinggi = (int) ((double) e.getValue() / skala * ph);
                int x = kiri + idx * slot + (slot - lebarBar) / 2;
                int y = atas + ph - tinggi;
                Color c = beragam ? PALET[idx % PALET.length] : PRIMARY;
                if (tinggi > 0) {
                    g.setPaint(new GradientPaint(0, y, c.brighter(), 0, y + tinggi, c));
                    g.fillRoundRect(x, y, lebarBar, tinggi + 6, 8, 8);
                    g.setColor(Color.WHITE);
                    g.fillRect(x - 1, atas + ph, lebarBar + 2, 8);
                }
                g.setColor(TEXT);
                if (e.getValue() > 0 && lebarBar >= 12) {
                    String nilai = String.valueOf(e.getValue());
                    g.drawString(nilai, x + (lebarBar - fm.stringWidth(nilai)) / 2, y - 5);
                }
                if (idx % step == 0) {
                    g.setColor(MUTED);
                    g.drawString(e.getKey(), x + (lebarBar - fm.stringWidth(e.getKey())) / 2, atas + ph + 18);
                }
                idx++;
            }
            g.setColor(new Color(185, 197, 215));
            g.drawLine(kiri, atas + ph, kiri + pw, atas + ph);
            g.dispose();
        }

        private static int niceMax(int max) {
            if (max <= 5) return 5;
            int step = (int) Math.pow(10, Math.floor(Math.log10(max)));
            return ((max + step - 1) / step) * step;
        }
    }
}
