package simrskhanza;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Rekap Operasi.
 * Hanya operasi yang SUDAH punya laporan operasi (laporan_operasi) yang ditampilkan.
 * Menampilkan satu baris per operasi (no.rawat + tgl.operasi) berisi: tanggal operasi,
 * jam mulai, jam selesai, durasi, No.RM, nama, alamat pasien, operator 1, jenis anestesi,
 * diagnosa pre/post-operatif dan kategori.
 *
 * Jam mulai   = operasi.tgl_operasi
 * Jam selesai = laporan_operasi.selesaioperasi
 */
public class DlgRekapOperasi extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    /** tgl_operasi asli (yyyy-MM-dd HH:mm:ss) tiap baris tabel - dipakai untuk cetak laporan. */
    private final java.util.List<String> tglOperasiRaw = new java.util.ArrayList<>();
    private javax.swing.JPopupMenu popupTabel;
    private javax.swing.JMenuItem MnCetakLaporan;

    private widget.InternalFrame internalFrame1;
    private widget.ScrollPane scrollPane1;
    private widget.Table tbRekap;
    private widget.panelisi panelFilter, panelTombol;
    private widget.Label lblTanggal, lblSd, lblNoRawat, lblKey, lblRecord, LTotal, lblPeriode;
    /** true = tampilkan SEMUA operasi tanpa batas tanggal (tombol Semua). */
    private boolean semuaTanggal = false;
    private widget.Tanggal Tgl1, Tgl2;
    private widget.TextBox NoRawat, TCari;
    private widget.Button BtnCari, BtnAll, BtnExcel, BtnGrafikOperasi, BtnGrafikAnestesi, BtnGrafikKategori, BtnKeluar;
    /** Periode yang dipakai pada pencarian terakhir (sama dengan isi tabel). */
    private String periodeAktif = "";

    public DlgRekapOperasi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initKomponen();

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "Tgl.Operasi", "Jam Mulai", "Jam Selesai", "Durasi", "No.RM", "Nama Pasien",
            "Alamat Pasien", "Operator 1", "Jenis Bayar", "Tindakan Operasi", "Jenis Anestesi", "Diagnosa Pre-Operatif",
            "Diagnosa Post-Operatif", "Kategori"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbRekap.setModel(tabMode);
        tbRekap.setPreferredScrollableViewportSize(new Dimension(800, 400));
        tbRekap.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] lebar = {120, 85, 75, 120, 110, 80, 180, 260, 180, 130, 250, 110, 220, 220, 110};
        for (int i = 0; i < lebar.length; i++) {
            TableColumn column = tbRekap.getColumnModel().getColumn(i);
            column.setPreferredWidth(lebar[i]);
        }
        tbRekap.setDefaultRenderer(Object.class, new WarnaTable());

        NoRawat.setDocument(new batasInput((byte) 17).getKata(NoRawat));
        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }
            });
        }

        // tanggal awal = tanggal 1 bulan ini, tanggal akhir = hari ini
        Calendar awal = Calendar.getInstance();
        awal.set(Calendar.DAY_OF_MONTH, 1);
        Tgl1.setDate(awal.getTime());
        Tgl2.setDate(new java.util.Date());

        // tampilkan data otomatis saat jendela dibuka
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                tampil();
            }
        });

        // tombol Esc menutup jendela
        getRootPane().registerKeyboardAction(evt -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        setSize(Math.min(1340, java.awt.Toolkit.getDefaultToolkit().getScreenSize().width), 600);
        setLocationRelativeTo(parent);
    }

    private void initKomponen() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("DlgRekapOperasi");
        setUndecorated(true);
        setResizable(false);

        internalFrame1 = new widget.InternalFrame();
        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Rekap Operasi ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50)));
        internalFrame1.setName("internalFrame1");
        internalFrame1.setLayout(new BorderLayout(1, 1));

        // ---------- tabel ----------
        scrollPane1 = new widget.ScrollPane();
        scrollPane1.setName("scrollPane1");
        scrollPane1.setOpaque(true);
        tbRekap = new widget.Table();
        tbRekap.setName("tbRekap");
        tbRekap.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION); // cetak per satu data yang dipilih
        scrollPane1.setViewportView(tbRekap);

        // ---------- popup klik kanan: Cetak Laporan Operasi ----------
        popupTabel = new javax.swing.JPopupMenu();
        popupTabel.setName("popupTabel");
        MnCetakLaporan = new javax.swing.JMenuItem();
        MnCetakLaporan.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakLaporan.setFont(new java.awt.Font("Tahoma", 0, 11));
        MnCetakLaporan.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakLaporan.setIcon(ikon("/picture/category.png"));
        MnCetakLaporan.setText("Cetak Laporan Operasi");
        MnCetakLaporan.setName("MnCetakLaporan");
        MnCetakLaporan.setPreferredSize(new Dimension(220, 26));
        MnCetakLaporan.addActionListener(evt -> cetakLaporanTerpilih());
        popupTabel.add(MnCetakLaporan);
        tbRekap.setComponentPopupMenu(popupTabel);
        scrollPane1.setComponentPopupMenu(popupTabel);
        // klik kanan = pilih dulu baris di bawah kursor
        tbRekap.addMouseListener(new java.awt.event.MouseAdapter() {
            private void pilih(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger() || javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    int r = tbRekap.rowAtPoint(e.getPoint());
                    if (r >= 0) {
                        tbRekap.setRowSelectionInterval(r, r);
                    }
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                pilih(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                pilih(e);
            }
        });
        internalFrame1.add(scrollPane1, BorderLayout.CENTER);

        // ---------- filter (atas) ----------
        panelFilter = new widget.panelisi();
        panelFilter.setName("panelFilter");
        panelFilter.setPreferredSize(new Dimension(100, 44));
        panelFilter.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

        lblTanggal = new widget.Label();
        lblTanggal.setText("Tgl.Operasi :");
        lblTanggal.setPreferredSize(new Dimension(75, 23));
        panelFilter.add(lblTanggal);

        Tgl1 = new widget.Tanggal();
        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setPreferredSize(new Dimension(100, 23));
        Tgl1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                Valid.pindah(evt, BtnKeluar, Tgl2);
            }
        });
        panelFilter.add(Tgl1);

        lblSd = new widget.Label();
        lblSd.setText("s.d.");
        lblSd.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSd.setPreferredSize(new Dimension(25, 23));
        panelFilter.add(lblSd);

        Tgl2 = new widget.Tanggal();
        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setPreferredSize(new Dimension(100, 23));
        Tgl2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                Valid.pindah(evt, Tgl1, NoRawat);
            }
        });
        panelFilter.add(Tgl2);

        lblNoRawat = new widget.Label();
        lblNoRawat.setText("No.Rawat :");
        lblNoRawat.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNoRawat.setPreferredSize(new Dimension(70, 23));
        panelFilter.add(lblNoRawat);

        NoRawat = new widget.TextBox();
        NoRawat.setPreferredSize(new Dimension(140, 23));
        NoRawat.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampil();
                } else {
                    Valid.pindah(evt, Tgl2, TCari);
                }
            }
        });
        panelFilter.add(NoRawat);
        internalFrame1.add(panelFilter, BorderLayout.PAGE_START);

        // ---------- tombol (bawah) ----------
        panelTombol = new widget.panelisi();
        panelTombol.setName("panelTombol");
        panelTombol.setPreferredSize(new Dimension(100, 56));
        panelTombol.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

        lblKey = new widget.Label();
        lblKey.setText("Key Word :");
        lblKey.setPreferredSize(new Dimension(70, 23));
        panelTombol.add(lblKey);

        TCari = new widget.TextBox();
        TCari.setPreferredSize(new Dimension(140, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampil();
                } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                    BtnCari.requestFocus();
                } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                    BtnKeluar.requestFocus();
                } else {
                    Valid.pindah(evt, NoRawat, BtnCari);
                }
            }
        });
        panelTombol.add(TCari);

        BtnCari = new widget.Button();
        BtnCari.setIcon(ikon("/picture/accept.png"));
        BtnCari.setMnemonic('5');
        BtnCari.setToolTipText("Alt+5");
        BtnCari.setPreferredSize(new Dimension(28, 23));
        BtnCari.addActionListener(evt -> cariPeriode());
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    cariPeriode();
                } else {
                    Valid.pindah(evt, TCari, BtnAll);
                }
            }
        });
        panelTombol.add(BtnCari);

        lblRecord = new widget.Label();
        lblRecord.setText("Record :");
        lblRecord.setPreferredSize(new Dimension(50, 30));
        panelTombol.add(lblRecord);

        LTotal = new widget.Label();
        LTotal.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LTotal.setText("0");
        LTotal.setPreferredSize(new Dimension(40, 30));
        panelTombol.add(LTotal);

        lblPeriode = new widget.Label();
        lblPeriode.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblPeriode.setPreferredSize(new Dimension(190, 30));
        panelTombol.add(lblPeriode);

        BtnAll = new widget.Button();
        BtnAll.setIcon(ikon("/picture/Search-16x16.png"));
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setPreferredSize(new Dimension(80, 30));
        BtnAll.addActionListener(evt -> semua());
        BtnAll.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    semua();
                } else {
                    Valid.pindah(evt, BtnCari, BtnExcel);
                }
            }
        });
        panelTombol.add(BtnAll);

        BtnExcel = new widget.Button();
        BtnExcel.setIcon(ikon("/picture/save-16x16.png"));
        BtnExcel.setMnemonic('E');
        BtnExcel.setText("Excel");
        BtnExcel.setToolTipText("Simpan data ke file Excel (.xlsx)  Alt+E");
        BtnExcel.setPreferredSize(new Dimension(80, 30));
        BtnExcel.addActionListener(evt -> eksporExcel());
        BtnExcel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    eksporExcel();
                } else {
                    Valid.pindah(evt, BtnAll, BtnGrafikOperasi);
                }
            }
        });
        panelTombol.add(BtnExcel);

        BtnGrafikOperasi = new widget.Button();
        BtnGrafikOperasi.setIcon(ikonGrafikBatang());
        BtnGrafikOperasi.setMnemonic('G');
        BtnGrafikOperasi.setText("Grafik Operasi");
        BtnGrafikOperasi.setToolTipText("Grafik jumlah operasi per Operator 1 (dibagi jenis bayar), pilih bulan/tahun dan bandingkan dengan tahun lalu  Alt+G");
        BtnGrafikOperasi.setPreferredSize(new Dimension(150, 30));
        BtnGrafikOperasi.addActionListener(evt -> tampilGrafik());
        BtnGrafikOperasi.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    tampilGrafik();
                } else {
                    Valid.pindah(evt, BtnExcel, BtnGrafikAnestesi);
                }
            }
        });
        panelTombol.add(BtnGrafikOperasi);

        BtnGrafikAnestesi = new widget.Button();
        BtnGrafikAnestesi.setIcon(ikonGrafikDonat());
        BtnGrafikAnestesi.setMnemonic('A');
        BtnGrafikAnestesi.setText("Grafik Anestesi");
        BtnGrafikAnestesi.setToolTipText("Jumlah operasi per Jenis Anestesi dalam periode  Alt+A");
        BtnGrafikAnestesi.setPreferredSize(new Dimension(150, 30));
        BtnGrafikAnestesi.addActionListener(evt -> tampilGrafikAnestesi());
        BtnGrafikAnestesi.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    tampilGrafikAnestesi();
                } else {
                    Valid.pindah(evt, BtnGrafikOperasi, BtnGrafikKategori);
                }
            }
        });
        panelTombol.add(BtnGrafikAnestesi);

        BtnGrafikKategori = new widget.Button();
        BtnGrafikKategori.setIcon(ikonGrafikBatang());
        BtnGrafikKategori.setMnemonic('T');
        BtnGrafikKategori.setText("Grafik Kategori");
        BtnGrafikKategori.setToolTipText("Jumlah operasi per Operator 1 (dibagi kategori operasi)  Alt+T");
        BtnGrafikKategori.setPreferredSize(new Dimension(150, 30));
        BtnGrafikKategori.addActionListener(evt -> tampilGrafikKategori());
        BtnGrafikKategori.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    tampilGrafikKategori();
                } else {
                    Valid.pindah(evt, BtnGrafikAnestesi, BtnKeluar);
                }
            }
        });
        panelTombol.add(BtnGrafikKategori);

        BtnKeluar = new widget.Button();
        BtnKeluar.setIcon(ikon("/picture/exit.png"));
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setPreferredSize(new Dimension(80, 30));
        BtnKeluar.addActionListener(evt -> dispose());
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    dispose();
                } else {
                    Valid.pindah(evt, BtnGrafikKategori, Tgl1);
                }
            }
        });
        panelTombol.add(BtnKeluar);
        internalFrame1.add(panelTombol, BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, BorderLayout.CENTER);
    }

    /** Cari dengan rentang tanggal yang dipilih. */
    private void cariPeriode() {
        semuaTanggal = false;
        tampil();
    }

    /** Tampilkan SEMUA operasi (tanpa batas tanggal, tanpa filter). */
    private void semua() {
        NoRawat.setText("");
        TCari.setText("");
        semuaTanggal = true;
        tampil();
    }

    /** Ubah menit menjadi teks, mis. 135 -> "2 jam 15 menit". */
    private String formatDurasi(int menit) {
        int jam = menit / 60;
        int sisa = menit % 60;
        if (jam == 0) {
            return sisa + " menit";
        }
        return jam + " jam " + sisa + " menit";
    }

    /**
     * Tindakan Operasi per operasi: hanya kode paket berawalan OK / VK, diambil SATU tindakan
     * dengan harga tertinggi (kolom harga: biayaoperator1). Memakai alias luar "o" (no_rawat, tgl_operasi).
     */
    private static final String SQL_TINDAKAN
        = "select po.nm_perawatan from operasi x "
        + "inner join paket_operasi po on po.kode_paket=x.kode_paket "
        + "where x.no_rawat=o.no_rawat and x.tgl_operasi=o.tgl_operasi "
        + "and (x.kode_paket like 'OK%' or x.kode_paket like 'VK%') "
        + "order by ifnull(x.biayaoperator1,0) desc, x.kode_paket "
        + "limit 1";

    private void tampil() {
        Valid.tabelKosong(tabMode);
        tglOperasiRaw.clear();

        StringBuilder sql = new StringBuilder(
            "select o.no_rawat, "
            + "date_format(o.tgl_operasi,'%Y-%m-%d %H:%i:%s') as tgl_raw, "
            + "date_format(o.tgl_operasi,'%d-%m-%Y') as tgl, "
            + "date_format(o.tgl_operasi,'%H:%i:%s') as jam_mulai, "
            + "case when lo.selesaioperasi is null then '' "
            + "     when date(lo.selesaioperasi)=date(o.tgl_operasi) then date_format(lo.selesaioperasi,'%H:%i:%s') "
            + "     else date_format(lo.selesaioperasi,'%d-%m-%Y %H:%i:%s') end as jam_selesai, "
            + "if(lo.selesaioperasi>o.tgl_operasi, timestampdiff(minute,o.tgl_operasi,lo.selesaioperasi), null) as menit, "
            + "rp.no_rkm_medis, p.nm_pasien, "
            + "concat_ws(', ',nullif(trim(p.alamat),''),kel.nm_kel,kec.nm_kec,kab.nm_kab) as alamat, "
            + "d.nm_dokter as operator1, pj.png_jawab as jenis_bayar, "
            + "(" + SQL_TINDAKAN + ") as tindakan_operasi, o.jenis_anasthesi, "
            + "lo.diagnosa_preop, lo.diagnosa_postop, o.kategori "
            + "from (select no_rawat, tgl_operasi, max(operator1) as operator1, "
            + "             max(jenis_anasthesi) as jenis_anasthesi, max(kategori) as kategori "
            + "      from operasi " + (semuaTanggal ? "" : "where tgl_operasi between ? and ? ")
            + "      group by no_rawat, tgl_operasi) o "
            + "inner join reg_periksa rp on o.no_rawat=rp.no_rawat "
            + "inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
            + "left join kelurahan kel on p.kd_kel=kel.kd_kel "
            + "left join kecamatan kec on p.kd_kec=kec.kd_kec "
            + "left join kabupaten kab on p.kd_kab=kab.kd_kab "
            + "left join dokter d on d.kd_dokter=o.operator1 "
            + "left join penjab pj on pj.kd_pj=rp.kd_pj "
            + "inner join laporan_operasi lo on lo.no_rawat=o.no_rawat and lo.tanggal=o.tgl_operasi "
            + "where 1=1 ");

        boolean pakaiNoRawat = !NoRawat.getText().trim().equals("");
        boolean pakaiKata = !TCari.getText().trim().equals("");
        if (pakaiNoRawat) {
            sql.append("and o.no_rawat=? ");
        }
        if (pakaiKata) {
            sql.append("and (o.no_rawat like ? or rp.no_rkm_medis like ? or p.nm_pasien like ? "
                + "or d.nm_dokter like ? or pj.png_jawab like ? or o.jenis_anasthesi like ? or o.kategori like ? "
                + "or lo.diagnosa_preop like ? or lo.diagnosa_postop like ?) ");
        }
        sql.append("order by o.tgl_operasi, o.no_rawat");

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement(sql.toString());
            int n = 1;
            if (!semuaTanggal) {
                ps.setString(n++, Valid.SetTgl(Tgl1.getSelectedItem() + "") + " 00:00:00");
                ps.setString(n++, Valid.SetTgl(Tgl2.getSelectedItem() + "") + " 23:59:59");
            }
            if (pakaiNoRawat) {
                ps.setString(n++, NoRawat.getText().trim());
            }
            if (pakaiKata) {
                String kata = "%" + TCari.getText().trim() + "%";
                for (int k = 0; k < 9; k++) {
                    ps.setString(n++, kata);
                }
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                int menit = rs.getInt("menit");
                String durasi = rs.wasNull() ? "-" : formatDurasi(menit);
                String jamSelesai = rs.getString("jam_selesai");
                tglOperasiRaw.add(rs.getString("tgl_raw"));
                tabMode.addRow(new Object[]{
                    rs.getString("no_rawat"),
                    rs.getString("tgl"),
                    rs.getString("jam_mulai"),
                    (jamSelesai == null || jamSelesai.equals("")) ? "-" : jamSelesai,
                    durasi,
                    rs.getString("no_rkm_medis"),
                    rs.getString("nm_pasien"),
                    rs.getString("alamat"),
                    rs.getString("operator1"),
                    rs.getString("jenis_bayar"),
                    rs.getString("tindakan_operasi"),
                    rs.getString("jenis_anasthesi"),
                    rs.getString("diagnosa_preop"),
                    rs.getString("diagnosa_postop"),
                    rs.getString("kategori")
                });
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
        LTotal.setText("" + tabMode.getRowCount());
        periodeAktif = semuaTanggal ? "SEMUA TANGGAL"
            : tglIndo(Valid.SetTgl(Tgl1.getSelectedItem() + "")) + " s.d. " + tglIndo(Valid.SetTgl(Tgl2.getSelectedItem() + ""));
        lblPeriode.setText("Periode : " + periodeAktif);
    }

    // ------------------------------------------------------------------
    //  CETAK LAPORAN OPERASI (klik kanan pada baris) - sama dengan menu
    //  "Laporan Operasi Pasien" di DlgCariTagihanOperasi, memakai rptLaporanOperasi.jasper
    // ------------------------------------------------------------------
    private void cetakLaporanTerpilih() {
        int baris = tbRekap.getSelectedRow();
        if (baris < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Silahkan pilih data terlebih dahulu ..!!");
            return;
        }
        baris = tbRekap.convertRowIndexToModel(baris);
        String noRawat = String.valueOf(tabMode.getValueAt(baris, 0));
        String tglOperasi = baris < tglOperasiRaw.size() ? tglOperasiRaw.get(baris) : null;
        Object op = tabMode.getValueAt(baris, 8); // kolom "Operator 1"
        if (noRawat.trim().equals("") || tglOperasi == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "No.Rawat / tanggal operasi pada baris ini tidak valid.");
            return;
        }
        try {
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
            java.util.Map<String, Object> param = new java.util.HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("norawat", noRawat);
            param.put("tanggaloperasi", tglOperasi);

            // tanda tangan elektronik operator 1
            String kodeoperator = Sequel.cariIsi("select operasi.operator1 from operasi where operasi.no_rawat='" + noRawat
                    + "' and tgl_operasi='" + tglOperasi + "'");
            String finger = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", kodeoperator);
            String namaOperator = (op == null || op.toString().trim().equals(""))
                    ? Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", kodeoperator) : op.toString();
            param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                    + "\nDitandatangani secara elektronik oleh " + namaOperator
                    + "\nID " + (finger.equals("") ? kodeoperator : finger) + "\n" + Valid.SetTgl3(tglOperasi));

            // tindakan: hanya yang sesuai filter (OK/VK, harga tertinggi) = isi kolom "Tindakan Operasi" pada baris ini
            Object tdk = tabMode.getValueAt(baris, 10);
            String tindakan = tdk == null ? "" : tdk.toString().trim();
            param.put("tindakan", tindakan);

            PreparedStatement ps = null;
            ResultSet rs = null;
            // pemeriksaan terakhir oleh operator 1 sebelum operasi (ralan / ranap)
            boolean ralan = Sequel.cariIsi("select reg_periksa.status_lanjut from reg_periksa where reg_periksa.no_rawat=?", noRawat).equals("Ralan");
            String tabel = ralan ? "pemeriksaan_ralan" : "pemeriksaan_ranap";
            try {
                ps = koneksi.prepareStatement("select " + tabel + ".no_rawat," + tabel + ".tgl_perawatan," + tabel + ".jam_rawat," + tabel + ".suhu_tubuh,"
                        + tabel + ".tensi," + tabel + ".nadi," + tabel + ".respirasi," + tabel + ".tinggi," + tabel + ".berat,"
                        + tabel + ".gcs," + tabel + ".keluhan," + tabel + ".pemeriksaan," + tabel + ".alergi," + tabel + ".rtl,"
                        + tabel + ".penilaian from " + tabel + " where " + tabel + ".no_rawat=? and " + tabel + ".nip=? "
                        + "and concat(" + tabel + ".tgl_perawatan,' '," + tabel + ".jam_rawat) <= ? "
                        + "order by " + tabel + ".tgl_perawatan desc," + tabel + ".jam_rawat desc limit 1");
                ps.setString(1, noRawat);
                ps.setString(2, kodeoperator);
                ps.setString(3, tglOperasi);
                rs = ps.executeQuery();
                if (rs.next()) {
                    param.put("tgl_perawatan", rs.getDate("tgl_perawatan"));
                    param.put("jam_rawat", rs.getString("jam_rawat"));
                    param.put("alergi", rs.getString("alergi"));
                    param.put("keluhan", rs.getString("keluhan"));
                    param.put("pemeriksaan", rs.getString("pemeriksaan"));
                    param.put("penilaian", rs.getString("penilaian"));
                    param.put("rtl", rs.getString("rtl"));
                    param.put("ruang", ralan
                            ? Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on reg_periksa.kd_poli=poliklinik.kd_poli where reg_periksa.no_rawat=?", rs.getString("no_rawat"))
                            : Sequel.cariIsi("select nm_bangsal from bangsal inner join kamar inner join kamar_inap on bangsal.kd_bangsal=kamar.kd_bangsal and kamar_inap.kd_kamar=kamar.kd_kamar where no_rawat=? order by tgl_masuk desc limit 1 ", rs.getString("no_rawat")));
                    param.put("suhu_tubuh", rs.getString("suhu_tubuh"));
                    param.put("tensi", rs.getString("tensi"));
                    param.put("tinggi", rs.getString("tinggi"));
                    param.put("berat", rs.getString("berat"));
                    param.put("nadi", rs.getString("nadi"));
                    param.put("respirasi", rs.getString("respirasi"));
                    param.put("gcs", rs.getString("gcs"));
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            } finally {
                tutup(rs, ps);
            }
            Valid.MyReport("rptLaporanOperasi.jasper", "report", "::[ Laporan Operasi ]::", param);
        } catch (Exception e) {
            System.out.println("Notif : " + e);
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal menyiapkan laporan operasi:\n" + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(java.awt.Cursor.getDefaultCursor());
        }
    }

    private void tutup(ResultSet rs, PreparedStatement ps) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    /** Ikon grafik batang (gradasi) + garis tren - digambar sendiri, tidak butuh file gambar. */
    static javax.swing.ImageIcon ikonGrafikBatang() {
        int n = 22;
        java.awt.image.BufferedImage im = new java.awt.image.BufferedImage(n, n, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = im.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        java.awt.Color[][] c = {
            {new java.awt.Color(0x6E, 0xE7, 0xF9), new java.awt.Color(0x2F, 0x80, 0xED)},
            {new java.awt.Color(0xFF, 0xC3, 0x71), new java.awt.Color(0xFF, 0x7A, 0x18)},
            {new java.awt.Color(0x7B, 0xF2, 0xA8), new java.awt.Color(0x14, 0xA7, 0x6C)}};
        int[] tinggi = {9, 14, 19};
        for (int i = 0; i < 3; i++) {
            int x = 2 + i * 7, y = 20 - tinggi[i];
            g.setPaint(new java.awt.GradientPaint(0, y, c[i][0], 0, 20, c[i][1]));
            g.fillRoundRect(x, y, 5, tinggi[i], 3, 3);
        }
        g.setColor(new java.awt.Color(0x3C, 0x4F, 0x6B));
        g.fillRoundRect(1, 20, 20, 2, 2, 2);
        g.setStroke(new java.awt.BasicStroke(1.7f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g.setColor(new java.awt.Color(0x1F, 0x3A, 0x5F));
        g.drawPolyline(new int[]{3, 9, 13, 19}, new int[]{11, 6, 9, 2}, 4);
        g.fillOval(17, 0, 4, 4);
        g.dispose();
        return new javax.swing.ImageIcon(im);
    }

    /** Ikon grafik donat berwarna (3 irisan) - digambar sendiri. */
    static javax.swing.ImageIcon ikonGrafikDonat() {
        int n = 22;
        java.awt.image.BufferedImage im = new java.awt.image.BufferedImage(n, n, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = im.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        java.awt.Color[][] c = {
            {new java.awt.Color(0x6E, 0xE7, 0xF9), new java.awt.Color(0x2F, 0x80, 0xED)},
            {new java.awt.Color(0xFF, 0xC3, 0x71), new java.awt.Color(0xFF, 0x7A, 0x18)},
            {new java.awt.Color(0x7B, 0xF2, 0xA8), new java.awt.Color(0x14, 0xA7, 0x6C)}};
        double[][] sudut = {{90, -150}, {-60, -110}, {-170, -100}}; // {awal, rentang} (derajat, searah jarum jam)
        for (int i = 0; i < 3; i++) {
            g.setPaint(new java.awt.GradientPaint(0, 0, c[i][0], 0, n, c[i][1]));
            g.fill(new java.awt.geom.Arc2D.Double(1, 1, 20, 20, sudut[i][0], sudut[i][1], java.awt.geom.Arc2D.PIE));
        }
        // celah putih tipis antar irisan
        g.setColor(new java.awt.Color(255, 255, 255, 235));
        g.setStroke(new java.awt.BasicStroke(1.3f));
        for (int i = 0; i < 3; i++) {
            double r = Math.toRadians(sudut[i][0]);
            g.drawLine(11, 11, (int) Math.round(11 + 10.5 * Math.cos(r)), (int) Math.round(11 - 10.5 * Math.sin(r)));
        }
        // lubang tengah (transparan) = donat
        g.setComposite(java.awt.AlphaComposite.Clear);
        g.fillOval(6, 6, 10, 10);
        g.setComposite(java.awt.AlphaComposite.SrcOver);
        g.setColor(new java.awt.Color(0x3C, 0x4F, 0x6B));
        g.setStroke(new java.awt.BasicStroke(1.0f));
        g.drawOval(6, 6, 10, 10);
        g.dispose();
        return new javax.swing.ImageIcon(im);
    }

    /** Ikon aman: bila file gambar tidak ada, tombol tetap tampil tanpa ikon. */
    private javax.swing.ImageIcon ikon(String path) {
        java.net.URL u = getClass().getResource(path);
        return u == null ? null : new javax.swing.ImageIcon(u);
    }

    /** yyyy-MM-dd -> dd-MM-yyyy */
    private String tglIndo(String ymd) {
        String[] p = ymd.split("-");
        return p.length == 3 ? p[2] + "-" + p[1] + "-" + p[0] : ymd;
    }

    /** Simpan isi tabel (sesuai filter/periode yang sedang tampil) ke file .xlsx yang rapi. */
    private void eksporExcel() {
        if (tabMode.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Tidak ada data untuk diexport ke Excel.\nSilakan klik Cari terlebih dahulu.");
            return;
        }
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setDialogTitle("Simpan Rekap Operasi ke Excel");
        fc.setSelectedFile(new java.io.File("Rekap_Operasi_"
                + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xlsx"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
        if (fc.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".xlsx")) {
            f = new java.io.File(f.getParentFile(), f.getName() + ".xlsx");
        }
        if (f.exists() && javax.swing.JOptionPane.showConfirmDialog(this, "File sudah ada. Timpa?", "Konfirmasi",
                javax.swing.JOptionPane.YES_NO_OPTION) != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        StringBuilder filter = new StringBuilder();
        if (!NoRawat.getText().trim().equals("")) {
            filter.append("No.Rawat : ").append(NoRawat.getText().trim());
        }
        if (!TCari.getText().trim().equals("")) {
            if (filter.length() > 0) {
                filter.append("   |   ");
            }
            filter.append("Key Word : ").append(TCari.getText().trim());
        }
        String namaRs;
        try {
            namaRs = fungsi.akses.getnamars();
        } catch (Exception e) {
            namaRs = "";
        }
        try {
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
            XlsxWriter.tulis(f, tabMode, namaRs, periodeAktif, filter.toString());
            setCursor(java.awt.Cursor.getDefaultCursor());
            if (javax.swing.JOptionPane.showConfirmDialog(this, "Data berhasil disimpan:\n" + f.getAbsolutePath()
                    + "\n\nBuka file sekarang?", "Export Excel", javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION) {
                try {
                    java.awt.Desktop.getDesktop().open(f);
                } catch (Exception ex) {
                    javax.swing.JOptionPane.showMessageDialog(this, "File tersimpan, tetapi tidak dapat dibuka otomatis.\nSilakan buka manual.");
                }
            }
        } catch (Exception e) {
            setCursor(java.awt.Cursor.getDefaultCursor());
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal menyimpan file Excel:\n" + e.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // ------------------------------------------------------------------
    //  REKAP JENIS ANESTESI
    //  Jenis anestesi diketik bebas (Spinal / SPINAL / spinal / "Spinal "), jadi sebelum
    //  direkap penulisannya disamakan dulu: tanpa beda huruf besar/kecil, spasi ganda dan
    //  spasi di ujung dibuang, titik pada singkatan (G.A.) dibuang, tanda baca di ujung dibuang.
    //  Data di database TIDAK diubah, hanya dikelompokkan saat rekap.
    // ------------------------------------------------------------------

    /**
     * Daftar "penulisan lain -> nama baku" untuk menggabungkan penulisan yang BERBEDA kata
     * tetapi sama maksudnya. Kunci ditulis huruf kecil. Contoh (hapus // untuk mengaktifkan):
     *   ALIAS_ANESTESI.put("ga", "UMUM");
     *   ALIAS_ANESTESI.put("general anestesi", "UMUM");
     *   ALIAS_ANESTESI.put("spinal anestesi", "SPINAL");
     */
    private static final java.util.Map<String, String> ALIAS_ANESTESI = new java.util.HashMap<>();

    static final String ANESTESI_KOSONG = "(Jenis anestesi belum diisi)";

    /** Kunci pengelompokan: huruf kecil, rapi, tanpa tanda baca di ujung. */
    static String kunciAnestesi(String raw) {
        if (raw == null) {
            return "";
        }
        String s = java.text.Normalizer.normalize(raw, java.text.Normalizer.Form.NFKC)
                .replace('\u00A0', ' ').toLowerCase(java.util.Locale.ROOT).trim();
        s = s.replaceAll("\\s+", " ");
        if (s.matches("(\\p{L}\\.)+\\p{L}?\\.?")) { // singkatan bertitik: g.a. -> ga
            s = s.replace(".", "");
        }
        s = s.replaceAll("\\s*([/+\\-,])\\s*", "$1");               // "spinal + sedasi" = "spinal+sedasi"
        s = s.replaceAll("^[\\p{Punct}\\s]+|[\\p{Punct}\\s]+$", ""); // buang tanda baca/spasi di ujung
        return s;
    }

    /** Nama yang ditampilkan di grafik (HURUF BESAR agar seragam). */
    static String labelAnestesi(String kunci) {
        if (kunci.isEmpty()) {
            return ANESTESI_KOSONG;
        }
        String alias = ALIAS_ANESTESI.get(kunci);
        return (alias != null ? alias : kunci).toUpperCase(java.util.Locale.ROOT);
    }

    /** Susun rekap per jenis anestesi (kolom 11 = Jenis Anestesi): berapa operasi tiap jenis anestesi. */
    static GrafikOperatorDialog.Hasil rekapAnestesi(javax.swing.table.TableModel m) {
        java.util.Map<String, java.util.Map<String, Integer>> hitung = new java.util.HashMap<>();
        java.util.Map<String, java.util.Map<String, Integer>> varian = new java.util.HashMap<>();
        for (int r = 0; r < m.getRowCount(); r++) {
            Object ja = m.getValueAt(r, 11);
            String asli = ja == null ? "" : ja.toString().trim();
            String label = labelAnestesi(kunciAnestesi(asli));
            // satu kategori per batang (nama anestesi itu sendiri) -> satu warna per batang
            hitung.computeIfAbsent(label, k -> new java.util.HashMap<>()).merge(label, 1, Integer::sum);
            if (!asli.isEmpty()) {
                varian.computeIfAbsent(label, k -> new java.util.HashMap<>()).merge(asli, 1, Integer::sum);
            }
        }
        GrafikOperatorDialog.Hasil h = GrafikOperatorDialog.susun(hitung, ANESTESI_KOSONG);
        h.judul = "REKAP OPERASI PER JENIS ANESTESI";
        h.label = "jenis anestesi";
        h.sederhana = true;
        for (java.util.Map.Entry<String, java.util.Map<String, Integer>> e : varian.entrySet()) {
            if (e.getValue().size() > 1) { // ada beberapa penulisan yang digabung
                java.util.List<java.util.Map.Entry<String, Integer>> daftar = new java.util.ArrayList<>(e.getValue().entrySet());
                daftar.sort((x, y) -> y.getValue().intValue() != x.getValue().intValue()
                        ? Integer.compare(y.getValue(), x.getValue()) : x.getKey().compareTo(y.getKey()));
                StringBuilder sb = new StringBuilder(daftar.size() + " penulisan digabung: ");
                for (int i = 0; i < daftar.size(); i++) {
                    sb.append(i > 0 ? ", " : "").append(daftar.get(i).getKey()).append(" (").append(daftar.get(i).getValue()).append(")");
                }
                h.catatan.put(e.getKey(), sb.toString());
            }
        }
        return h;
    }

    private void tampilGrafikAnestesi() {
        if (tabMode.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Tidak ada data untuk dibuat grafik.\nSilakan klik Cari terlebih dahulu.");
            return;
        }
        new GrafikOperatorDialog(this, rekapAnestesi(tabMode), periodeAktif).setVisible(true);
    }

    static final String KATEGORI_KOSONG = "(Kategori belum diisi)";

    /** Popup grafik: jumlah operasi per Operator 1, dibagi per Kategori operasi (kolom 14), pada data yang sedang tampil. */
    private void tampilGrafikKategori() {
        if (tabMode.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Tidak ada data untuk dibuat grafik.\nSilakan klik Cari terlebih dahulu.");
            return;
        }
        java.util.Map<String, java.util.Map<String, Integer>> hitung = new java.util.HashMap<>();
        for (int r = 0; r < tabMode.getRowCount(); r++) {
            Object o = tabMode.getValueAt(r, 8);   // kolom "Operator 1"
            Object k = tabMode.getValueAt(r, 14);  // kolom "Kategori"
            String nama = (o == null || o.toString().trim().equals("")) ? GrafikOperatorDialog.OPERATOR_KOSONG : o.toString().trim();
            String kat = (k == null || k.toString().trim().equals("")) ? KATEGORI_KOSONG : k.toString().trim();
            hitung.computeIfAbsent(nama, x -> new java.util.HashMap<>()).merge(kat, 1, Integer::sum);
        }
        GrafikOperatorDialog.Hasil h = GrafikOperatorDialog.susun(hitung, KATEGORI_KOSONG);
        h.judul = "REKAP OPERASI PER OPERATOR 1 BERDASARKAN KATEGORI";
        h.labelStack = "kategori";
        new GrafikOperatorDialog(this, h, periodeAktif).setVisible(true);
    }

    /** Dasar query grafik: sama dengan tampil() (hanya operasi yang punya laporan_operasi), tanpa kolom yang tidak diperlukan. */
    private static String sqlGrafikDari(String kondisiTanggal) {
        return "from (select no_rawat, tgl_operasi, max(operator1) as operator1, max(jenis_anasthesi) as jenis_anasthesi, max(kategori) as kategori "
            + "      from operasi where " + kondisiTanggal + " group by no_rawat, tgl_operasi) o "
            + "inner join reg_periksa rp on o.no_rawat=rp.no_rawat "
            + "inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
            + "left join dokter d on d.kd_dokter=o.operator1 "
            + "left join penjab pj on pj.kd_pj=rp.kd_pj "
            + "inner join laporan_operasi lo on lo.no_rawat=o.no_rawat and lo.tanggal=o.tgl_operasi "
            + "where 1=1 ";
    }

    private static final String SQL_GRAFIK_DARI = sqlGrafikDari("tgl_operasi between ? and ?");
    private static final String SQL_GRAFIK_DARI_2 = sqlGrafikDari("(tgl_operasi between ? and ? or tgl_operasi between ? and ?)");
    private static final int TIMEOUT_GRAFIK = 60; // detik; kueri macet tidak membuat grafik menggantung selamanya

    private static String klausaFilterGrafik(boolean pakaiNoRawat, boolean pakaiKata) {
        StringBuilder sb = new StringBuilder();
        if (pakaiNoRawat) {
            sb.append("and o.no_rawat=? ");
        }
        if (pakaiKata) {
            sb.append("and (o.no_rawat like ? or rp.no_rkm_medis like ? or p.nm_pasien like ? "
                + "or d.nm_dokter like ? or pj.png_jawab like ? or o.jenis_anasthesi like ? or o.kategori like ? "
                + "or lo.diagnosa_preop like ? or lo.diagnosa_postop like ?) ");
        }
        return sb.toString();
    }

    private static void ikatFilterGrafik(PreparedStatement ps, int n, boolean pakaiNoRawat, String noRawat, boolean pakaiKata, String kata) throws java.sql.SQLException {
        if (pakaiNoRawat) {
            ps.setString(n++, noRawat);
        }
        if (pakaiKata) {
            for (int k = 0; k < 9; k++) {
                ps.setString(n++, kata);
            }
        }
    }

    /**
     * Popup grafik: jumlah operasi per Operator 1, dibagi per Jenis Bayar. Bulan dan tahun dipilih lewat chip di atas grafik
     * (tidak terbatas pada rentang tanggal tabel) dan selalu dibandingkan dengan periode yang sama tahun lalu.
     * Filter No.Rawat / kata kunci yang sedang terisi ikut dipakai.
     */
    private void tampilGrafik() {
        java.time.LocalDate hari = java.time.LocalDate.now();
        int tahun = hari.getYear(), bulan = hari.getMonthValue();
        if (semuaTanggal) {
            bulan = 0;
        } else {
            try { // satu bulan -> bulan itu; rentang lebih panjang -> setahun
                java.time.LocalDate t1 = java.time.LocalDate.parse(Valid.SetTgl(Tgl1.getSelectedItem() + ""));
                java.time.LocalDate t2 = java.time.LocalDate.parse(Valid.SetTgl(Tgl2.getSelectedItem() + ""));
                tahun = t2.getYear();
                bulan = (t1.getYear() == t2.getYear() && t1.getMonthValue() == t2.getMonthValue()) ? t2.getMonthValue() : 0;
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
        final boolean pakaiNoRawat = !NoRawat.getText().trim().equals("");
        final String noRawat = NoRawat.getText().trim();
        final boolean pakaiKata = !TCari.getText().trim().equals("");
        final String kata = "%" + TCari.getText().trim() + "%";
        StringBuilder info = new StringBuilder();
        if (pakaiNoRawat) {
            info.append("No.Rawat ").append(noRawat);
        }
        if (pakaiKata) {
            info.append(info.length() > 0 ? ", " : "").append("kata kunci \"").append(TCari.getText().trim()).append("\"");
        }
        GrafikOperatorDialog.Pemuat pemuat = new GrafikOperatorDialog.Pemuat() {
            @Override
            public int[][] muatBulanan(int th) throws Exception {
                java.time.LocalDate skr = java.time.LocalDate.now();
                java.time.LocalDate iniDari = java.time.LocalDate.of(th, 1, 1);
                java.time.LocalDate iniSampai = th == skr.getYear() ? skr : java.time.LocalDate.of(th, 12, 31);
                java.time.LocalDate laluDari = iniDari.minusYears(1);
                java.time.LocalDate laluSampai = th == skr.getYear() ? skr.minusYears(1) : java.time.LocalDate.of(th - 1, 12, 31);
                int[][] r = new int[2][12];
                String sql = "select year(o.tgl_operasi) as thn, month(o.tgl_operasi) as bln, count(*) as jml " + SQL_GRAFIK_DARI_2
                        + klausaFilterGrafik(pakaiNoRawat, pakaiKata) + "group by year(o.tgl_operasi), month(o.tgl_operasi)";
                try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                    ps.setQueryTimeout(TIMEOUT_GRAFIK);
                    ps.setString(1, laluDari + " 00:00:00");
                    ps.setString(2, laluSampai + " 23:59:59");
                    ps.setString(3, iniDari + " 00:00:00");
                    ps.setString(4, iniSampai + " 23:59:59");
                    ikatFilterGrafik(ps, 5, pakaiNoRawat, noRawat, pakaiKata, kata);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int th2 = rs.getInt("thn"), bl = rs.getInt("bln");
                            int k = th2 == th ? 0 : (th2 == th - 1 ? 1 : -1);
                            if (k >= 0 && bl >= 1 && bl <= 12) {
                                r[k][bl - 1] = rs.getInt("jml");
                            }
                        }
                    }
                }
                return r;
            }

            @Override
            public java.util.List<java.util.Map<String, java.util.Map<String, Integer>>> muatRincianPasangan(
                    java.time.LocalDate dari, java.time.LocalDate sampai, java.time.LocalDate dariLalu, java.time.LocalDate sampaiLalu) throws Exception {
                java.util.Map<String, java.util.Map<String, Integer>> ini = new java.util.HashMap<>(), lalu = new java.util.HashMap<>();
                String sql = "select (o.tgl_operasi >= ?) as ini, d.nm_dokter as operator1, pj.png_jawab as jenis_bayar, count(*) as jml " + SQL_GRAFIK_DARI_2
                        + klausaFilterGrafik(pakaiNoRawat, pakaiKata) + "group by ini, d.nm_dokter, pj.png_jawab";
                try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                    ps.setQueryTimeout(TIMEOUT_GRAFIK);
                    String awalIni = dari + " 00:00:00";
                    ps.setString(1, awalIni);
                    ps.setString(2, dariLalu + " 00:00:00");
                    ps.setString(3, sampaiLalu + " 23:59:59");
                    ps.setString(4, awalIni);
                    ps.setString(5, sampai + " 23:59:59");
                    ikatFilterGrafik(ps, 6, pakaiNoRawat, noRawat, pakaiKata, kata);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String o = rs.getString("operator1"), jb = rs.getString("jenis_bayar");
                            String nama = (o == null || o.trim().equals("")) ? GrafikOperatorDialog.OPERATOR_KOSONG : o.trim();
                            String bayar = (jb == null || jb.trim().equals("")) ? GrafikOperatorDialog.BAYAR_KOSONG : jb.trim();
                            (rs.getInt("ini") == 1 ? ini : lalu).computeIfAbsent(nama, k -> new java.util.HashMap<>()).merge(bayar, rs.getInt("jml"), Integer::sum);
                        }
                    }
                }
                return java.util.Arrays.asList(ini, lalu);
            }

            @Override
            public java.util.Map<String, java.util.Map<String, Integer>> muatRincian(java.time.LocalDate dari, java.time.LocalDate sampai) throws Exception {
                java.util.Map<String, java.util.Map<String, Integer>> hasil = new java.util.HashMap<>();
                String sql = "select d.nm_dokter as operator1, pj.png_jawab as jenis_bayar, count(*) as jml " + SQL_GRAFIK_DARI
                        + klausaFilterGrafik(pakaiNoRawat, pakaiKata) + "group by d.nm_dokter, pj.png_jawab";
                try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                    ps.setQueryTimeout(TIMEOUT_GRAFIK);
                    ps.setString(1, dari + " 00:00:00");
                    ps.setString(2, sampai + " 23:59:59");
                    ikatFilterGrafik(ps, 3, pakaiNoRawat, noRawat, pakaiKata, kata);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String o = rs.getString("operator1"), jb = rs.getString("jenis_bayar");
                            String nama = (o == null || o.trim().equals("")) ? GrafikOperatorDialog.OPERATOR_KOSONG : o.trim();
                            String bayar = (jb == null || jb.trim().equals("")) ? GrafikOperatorDialog.BAYAR_KOSONG : jb.trim();
                            hasil.computeIfAbsent(nama, k -> new java.util.HashMap<>()).merge(bayar, rs.getInt("jml"), Integer::sum);
                        }
                    }
                }
                return hasil;
            }
        };
        new GrafikOperatorDialog(this, pemuat, tahun, bulan, info.length() == 0 ? null : "filter: " + info).setVisible(true);
    }

    // =====================================================================
    //  EXPORT EXCEL (.xlsx) - tanpa library tambahan (XML + ZIP)
    // =====================================================================
    static final class XlsxWriter {
        // indeks style (cellXfs) di styles.xml
        private static final int S_DEFAULT = 0, S_TITLE = 1, S_SUB = 2, S_HEADER = 3, S_BODY = 4,
                S_BODY_C = 5, S_ZEBRA = 6, S_ZEBRA_C = 7, S_TOTAL_L = 8, S_RS = 9, S_TOTAL_V = 10;

        private static String kolom(int idx) { // 0 -> A, 25 -> Z, 26 -> AA
            StringBuilder sb = new StringBuilder();
            int n = idx + 1;
            while (n > 0) {
                int r = (n - 1) % 26;
                sb.insert(0, (char) ('A' + r));
                n = (n - 1) / 26;
            }
            return sb.toString();
        }

        private static String esc(String s) {
            if (s == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder(s.length() + 8);
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '&') {
                    sb.append("&amp;");
                } else if (c == '<') {
                    sb.append("&lt;");
                } else if (c == '>') {
                    sb.append("&gt;");
                } else if (c == '"') {
                    sb.append("&quot;");
                } else if (c == '\t' || c == '\n' || c == '\r' || (c >= 0x20 && c != 0xFFFE && c != 0xFFFF
                        && !(c >= 0xD800 && c <= 0xDFFF))) {
                    sb.append(c);
                } else if (Character.isHighSurrogate(c) && i + 1 < s.length() && Character.isLowSurrogate(s.charAt(i + 1))) {
                    sb.append(c).append(s.charAt(++i));
                } // karakter kontrol lain dibuang agar XML tetap valid
            }
            return sb.toString();
        }

        private static void teks(StringBuilder sb, int col, int row, int style, String v) {
            sb.append("<c r=\"").append(kolom(col)).append(row).append("\" s=\"").append(style).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
              .append(esc(v)).append("</t></is></c>");
        }

        private static void angka(StringBuilder sb, int col, int row, int style, long v) {
            sb.append("<c r=\"").append(kolom(col)).append(row).append("\" s=\"").append(style).append("\"><v>").append(v).append("</v></c>");
        }

        private static void kosong(StringBuilder sb, int col, int row, int style) {
            sb.append("<c r=\"").append(kolom(col)).append(row).append("\" s=\"").append(style).append("\"/>");
        }

        /**
         * @param namaRs nama rumah sakit (boleh kosong)
         * @param periode teks periode, mis. "01-10-2026 s.d. 06-10-2026"
         * @param filter teks filter tambahan (boleh kosong)
         */
        static void tulis(java.io.File file, javax.swing.table.TableModel m, String namaRs, String periode, String filter) throws Exception {
            final int kolomData = m.getColumnCount();
            final int totalKolom = kolomData + 1; // + kolom "No"
            final int barisData = m.getRowCount();
            final int rowHeader = 6;
            final int rowAwal = rowHeader + 1;
            final int rowAkhirData = rowHeader + barisData;
            final int rowTotal = rowAkhirData + 1;
            final String namaSheet = "Rekap Operasi";
            final String lastCol = kolom(totalKolom - 1);

            // ---- lebar kolom ----
            double[] lebar = new double[totalKolom];
            lebar[0] = 6;
            for (int c = 0; c < kolomData; c++) {
                int maks = String.valueOf(m.getColumnName(c)).length() + 3;
                for (int r = 0; r < barisData; r++) {
                    Object o = m.getValueAt(r, c);
                    if (o != null) {
                        for (String baris : o.toString().split("\\r?\\n")) {
                            maks = Math.max(maks, baris.length());
                        }
                    }
                }
                lebar[c + 1] = Math.max(9, Math.min(48, maks * 1.12 + 2));
            }

            // ---- sheet1.xml ----
            StringBuilder sh = new StringBuilder(64 * 1024);
            sh.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
            sh.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ")
              .append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">");
            sh.append("<sheetPr><pageSetUpPr fitToPage=\"1\"/></sheetPr>");
            sh.append("<dimension ref=\"A1:").append(lastCol).append(rowTotal).append("\"/>");
            sh.append("<sheetViews><sheetView showGridLines=\"0\" workbookViewId=\"0\">")
              .append("<pane ySplit=\"").append(rowHeader).append("\" topLeftCell=\"A").append(rowAwal)
              .append("\" activePane=\"bottomLeft\" state=\"frozen\"/>")
              .append("<selection pane=\"bottomLeft\" activeCell=\"A").append(rowAwal).append("\" sqref=\"A").append(rowAwal).append("\"/>")
              .append("</sheetView></sheetViews>");
            sh.append("<sheetFormatPr defaultRowHeight=\"15\"/>");
            sh.append("<cols>");
            for (int c = 0; c < totalKolom; c++) {
                sh.append("<col min=\"").append(c + 1).append("\" max=\"").append(c + 1).append("\" width=\"")
                  .append(String.format(java.util.Locale.US, "%.2f", lebar[c])).append("\" customWidth=\"1\"/>");
            }
            sh.append("</cols>");
            sh.append("<sheetData>");

            // baris 1 : nama RS
            sh.append("<row r=\"1\" ht=\"20\" customHeight=\"1\">");
            teks(sh, 0, 1, S_RS, namaRs == null ? "" : namaRs);
            sh.append("</row>");
            // baris 2 : judul
            sh.append("<row r=\"2\" ht=\"28\" customHeight=\"1\">");
            teks(sh, 0, 2, S_TITLE, "REKAP OPERASI");
            sh.append("</row>");
            // baris 3 : periode
            sh.append("<row r=\"3\">");
            teks(sh, 0, 3, S_SUB, "Periode : " + periode);
            sh.append("</row>");
            // baris 4 : filter / jumlah
            sh.append("<row r=\"4\">");
            teks(sh, 0, 4, S_SUB, (filter == null || filter.isEmpty() ? "" : filter + "   |   ") + "Jumlah operasi : " + barisData);
            sh.append("</row>");
            // baris 5 : tanggal cetak
            sh.append("<row r=\"5\">");
            teks(sh, 0, 5, S_SUB, "Dibuat : " + new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new java.util.Date()));
            sh.append("</row>");

            // baris header
            sh.append("<row r=\"").append(rowHeader).append("\" ht=\"32\" customHeight=\"1\">");
            teks(sh, 0, rowHeader, S_HEADER, "No");
            for (int c = 0; c < kolomData; c++) {
                teks(sh, c + 1, rowHeader, S_HEADER, m.getColumnName(c));
            }
            sh.append("</row>");

            // data
            for (int r = 0; r < barisData; r++) {
                int row = rowAwal + r;
                boolean zebra = (r % 2 == 1);
                sh.append("<row r=\"").append(row).append("\">");
                angka(sh, 0, row, zebra ? S_ZEBRA_C : S_BODY_C, r + 1);
                for (int c = 0; c < kolomData; c++) {
                    Object o = m.getValueAt(r, c);
                    String v = o == null ? "" : o.toString();
                    // kolom pendek (tanggal/jam/durasi/no.rm/no.rawat) rata tengah, lainnya rata kiri
                    boolean tengah = (c == 0 || c == 1 || c == 2 || c == 3 || c == 4 || c == 5);
                    int st = zebra ? (tengah ? S_ZEBRA_C : S_ZEBRA) : (tengah ? S_BODY_C : S_BODY);
                    teks(sh, c + 1, row, st, v);
                }
                sh.append("</row>");
            }

            // baris total
            sh.append("<row r=\"").append(rowTotal).append("\" ht=\"22\" customHeight=\"1\">");
            teks(sh, 0, rowTotal, S_TOTAL_L, "Total Operasi");
            kosong(sh, 1, rowTotal, S_TOTAL_L);
            kosong(sh, 2, rowTotal, S_TOTAL_L);
            angka(sh, 3, rowTotal, S_TOTAL_V, barisData);
            sh.append("</row>");
            sh.append("</sheetData>");

            if (barisData > 0) {
                sh.append("<autoFilter ref=\"A").append(rowHeader).append(":").append(lastCol).append(rowAkhirData).append("\"/>");
            }
            sh.append("<mergeCells count=\"6\">")
              .append("<mergeCell ref=\"A1:").append(lastCol).append("1\"/>")
              .append("<mergeCell ref=\"A2:").append(lastCol).append("2\"/>")
              .append("<mergeCell ref=\"A3:").append(lastCol).append("3\"/>")
              .append("<mergeCell ref=\"A4:").append(lastCol).append("4\"/>")
              .append("<mergeCell ref=\"A5:").append(lastCol).append("5\"/>")
              .append("<mergeCell ref=\"A").append(rowTotal).append(":C").append(rowTotal).append("\"/>")
              .append("</mergeCells>");
            sh.append("<pageMargins left=\"0.4\" right=\"0.4\" top=\"0.6\" bottom=\"0.6\" header=\"0.3\" footer=\"0.3\"/>");
            sh.append("<pageSetup paperSize=\"9\" orientation=\"landscape\" fitToWidth=\"1\" fitToHeight=\"0\"/>");
            sh.append("<headerFooter><oddFooter>&amp;LRekap Operasi&amp;RHalaman &amp;P dari &amp;N</oddFooter></headerFooter>");
            sh.append("</worksheet>");

            // ---- workbook.xml ----
            StringBuilder wb = new StringBuilder();
            wb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
              .append("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ")
              .append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">")
              .append("<bookViews><workbookView xWindow=\"0\" yWindow=\"0\" windowWidth=\"28800\" windowHeight=\"15000\"/></bookViews>")
              .append("<sheets><sheet name=\"").append(namaSheet).append("\" sheetId=\"1\" r:id=\"rId1\"/></sheets>")
              .append("<definedNames>");
            if (barisData > 0) {
                wb.append("<definedName name=\"_xlnm._FilterDatabase\" localSheetId=\"0\" hidden=\"1\">'").append(namaSheet)
                  .append("'!$A$").append(rowHeader).append(":$").append(lastCol).append("$").append(rowAkhirData).append("</definedName>");
            }
            wb.append("<definedName name=\"_xlnm.Print_Titles\" localSheetId=\"0\">'").append(namaSheet)
              .append("'!$").append(rowHeader).append(":$").append(rowHeader).append("</definedName>")
              .append("</definedNames></workbook>");

            String styles = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<fonts count=\"6\">"
                + "<font><sz val=\"11\"/><name val=\"Calibri\"/><family val=\"2\"/></font>"
                + "<font><b/><sz val=\"18\"/><color rgb=\"FF1F4E78\"/><name val=\"Calibri\"/><family val=\"2\"/></font>"
                + "<font><i/><sz val=\"10\"/><color rgb=\"FF595959\"/><name val=\"Calibri\"/><family val=\"2\"/></font>"
                + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/><family val=\"2\"/></font>"
                + "<font><b/><sz val=\"11\"/><color rgb=\"FF1F4E78\"/><name val=\"Calibri\"/><family val=\"2\"/></font>"
                + "<font><b/><sz val=\"13\"/><color rgb=\"FF262626\"/><name val=\"Calibri\"/><family val=\"2\"/></font>"
                + "</fonts>"
                + "<fills count=\"5\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1F4E78\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFEAF1F8\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFD9E7F5\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"2\">"
                + "<border><left/><right/><top/><bottom/><diagonal/></border>"
                + "<border>"
                + "<left style=\"thin\"><color rgb=\"FFBFBFBF\"/></left><right style=\"thin\"><color rgb=\"FFBFBFBF\"/></right>"
                + "<top style=\"thin\"><color rgb=\"FFBFBFBF\"/></top><bottom style=\"thin\"><color rgb=\"FFBFBFBF\"/></bottom><diagonal/>"
                + "</border>"
                + "</borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"11\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"top\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"top\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"top\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"top\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"4\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"5\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"4\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
                + "</cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";

            String contentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "</Types>";
            String relsRoot = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
            String relsWb = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>";

            try (java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(file))) {
                tambah(zip, "[Content_Types].xml", contentTypes);
                tambah(zip, "_rels/.rels", relsRoot);
                tambah(zip, "xl/workbook.xml", wb.toString());
                tambah(zip, "xl/_rels/workbook.xml.rels", relsWb);
                tambah(zip, "xl/styles.xml", styles);
                tambah(zip, "xl/worksheets/sheet1.xml", sh.toString());
            }
        }

        private static void tambah(java.util.zip.ZipOutputStream zip, String nama, String isi) throws java.io.IOException {
            zip.putNextEntry(new java.util.zip.ZipEntry(nama));
            zip.write(isi.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zip.closeEntry();
        }
    }

    // =====================================================================
    //  GRAFIK REKAP OPERASI PER OPERATOR 1 (digambar sendiri dengan Java2D)
    //  Satu batang = satu operator, dibagi warna menurut jenis bayar.
    // =====================================================================
    /** Cache objek Font: paint berjalan 60x/detik, jangan membuat Font baru tiap frame. */
    static final class FontCache {
        private static final java.util.Map<Long, java.awt.Font> M = new java.util.HashMap<>();

        static synchronized java.awt.Font get(int style, int size) {
            Long k = ((long) style << 32) | size;
            java.awt.Font f = M.get(k);
            if (f == null) {
                f = new java.awt.Font("Segoe UI", style, size);
                M.put(k, f);
            }
            return f;
        }
    }

    static final class GrafikOperatorDialog extends javax.swing.JDialog {
        private static final java.awt.Color BG1 = new java.awt.Color(0x0B, 0x13, 0x2B);
        private static final java.awt.Color BG2 = new java.awt.Color(0x1C, 0x25, 0x41);
        private static final java.awt.Color TEKS = new java.awt.Color(0xE6, 0xEE, 0xF8);
        private static final java.awt.Color TEKS_REDUP = new java.awt.Color(0x8F, 0xA6, 0xBD);
        private static final java.awt.Color EMAS = new java.awt.Color(0xFF, 0xD5, 0x6B);

        /** pasangan warna {atas, bawah} untuk tiap jenis bayar */
        private static final java.awt.Color[][] PALET = {
            {new java.awt.Color(0x6E, 0xE7, 0xF9), new java.awt.Color(0x2F, 0x80, 0xED)},
            {new java.awt.Color(0xFF, 0xC3, 0x71), new java.awt.Color(0xFF, 0x7A, 0x18)},
            {new java.awt.Color(0x7B, 0xF2, 0xA8), new java.awt.Color(0x14, 0xA7, 0x6C)},
            {new java.awt.Color(0xD5, 0x9B, 0xFF), new java.awt.Color(0x8E, 0x54, 0xE9)},
            {new java.awt.Color(0xFF, 0xE2, 0x7A), new java.awt.Color(0xE5, 0xA8, 0x00)},
            {new java.awt.Color(0xFF, 0x8F, 0xA3), new java.awt.Color(0xE6, 0x39, 0x50)},
            {new java.awt.Color(0x9C, 0xF0, 0xE3), new java.awt.Color(0x1F, 0x9E, 0x8F)},
            {new java.awt.Color(0xC5, 0xD1, 0xE3), new java.awt.Color(0x6F, 0x86, 0xA6)}
        };
        private static final java.awt.Color[] ABU = {new java.awt.Color(0xB8, 0xC2, 0xCC), new java.awt.Color(0x6B, 0x78, 0x85)};
        static final String BAYAR_KOSONG = "(Jenis bayar belum diisi)";
        static final String OPERATOR_KOSONG = "(Operator 1 belum diisi)";

        static final class DataOperator {
            final String nama;
            int jumlah = 0;
            final java.util.Map<String, Integer> perBayar = new java.util.HashMap<>();
            int jumlahLalu = 0;                                                       // pembanding (periode sama tahun lalu)
            final java.util.Map<String, Integer> perBayarLalu = new java.util.HashMap<>();

            DataOperator(String nama) {
                this.nama = nama;
            }
        }

        /** Hasil pengolahan data: operator terurut + urutan jenis bayar + total per jenis bayar. */
        static final class Hasil {
            final java.util.List<DataOperator> operator = new java.util.ArrayList<>();
            final java.util.List<String> urutanBayar = new java.util.ArrayList<>();
            final java.util.Map<String, Integer> totalBayar = new java.util.HashMap<>();
            final java.util.Map<String, java.awt.Color[]> warna = new java.util.HashMap<>();
            int total = 0;
            int totalLalu = 0;                              // total pembanding (periode sama tahun lalu)
            boolean bandingkan = false;                     // true = tampilkan perbandingan dengan tahun lalu
            String namaPembanding = "";                     // mis. "Juli 2025"
            final java.util.Map<String, Integer> totalBayarLalu = new java.util.HashMap<>();
            String judul = "REKAP OPERASI PER OPERATOR 1"; // judul grafik
            String label = "operator";                      // nama kelompok (operator / jenis anestesi)
            String labelStack = "jenis bayar";              // pembagi warna dalam satu batang (jenis bayar / operator 1)
            boolean sederhana = false;                      // true = satu warna per batang, tanpa legenda/rincian/pembagi
            /** kelompok -> keterangan penulisan yang digabung (bila ada) */
            final java.util.Map<String, String> catatan = new java.util.HashMap<>();

            java.awt.Color[] warnaBayar(String bayar) {
                java.awt.Color[] c = warna.get(bayar);
                return c == null ? ABU : c;
            }
        }

        /**
         * @param data operator -> (jenis bayar -> jumlah operasi)
         * Operator diurutkan terbanyak -> tersedikit (sama banyak: urut nama).
         * Jenis bayar diurutkan menurut total terbanyak dan tiap jenis bayar mendapat warna tetap.
         */
        static Hasil susun(java.util.Map<String, java.util.Map<String, Integer>> data) {
            return susun(data, BAYAR_KOSONG);
        }

        /** @param stackKosong nama kategori "belum diisi" (diberi warna abu-abu) */
        static Hasil susun(java.util.Map<String, java.util.Map<String, Integer>> data, String stackKosong) {
            Hasil h = new Hasil();
            for (java.util.Map.Entry<String, java.util.Map<String, Integer>> e : data.entrySet()) {
                DataOperator d = new DataOperator(e.getKey());
                for (java.util.Map.Entry<String, Integer> b : e.getValue().entrySet()) {
                    d.perBayar.put(b.getKey(), b.getValue());
                    d.jumlah += b.getValue();
                    h.totalBayar.merge(b.getKey(), b.getValue(), Integer::sum);
                    h.total += b.getValue();
                }
                h.operator.add(d);
            }
            h.operator.sort((a, b) -> {
                if (a.jumlah != b.jumlah) {
                    return Integer.compare(b.jumlah, a.jumlah);
                }
                return a.nama.compareToIgnoreCase(b.nama);
            });
            h.urutanBayar.addAll(h.totalBayar.keySet());
            h.urutanBayar.sort((a, b) -> {
                int ca = h.totalBayar.get(a), cb = h.totalBayar.get(b);
                if (ca != cb) {
                    return Integer.compare(cb, ca);
                }
                return a.compareToIgnoreCase(b);
            });
            int idx = 0;
            for (String b : h.urutanBayar) {
                if (b.equals(stackKosong)) {
                    h.warna.put(b, ABU);
                } else {
                    h.warna.put(b, idx < PALET.length ? PALET[idx] : warnaHsb(idx));
                    idx++;
                }
            }
            return h;
        }

        private static final String[] NAMA_BULAN = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        private static final java.awt.Color NAIK = new java.awt.Color(0x7B, 0xF2, 0xA8), TURUN = new java.awt.Color(0xFF, 0x8F, 0xA3);

        /** Selisih terhadap pembanding dalam persen, mis. "▲ 12.5%"; tanpa pembanding: "baru" atau "-". */
        static String deltaPersen(int ini, int lalu) {
            if (lalu == 0) {
                return ini == 0 ? "-" : "baru";
            }
            double p = (ini - lalu) * 100.0 / lalu;
            if (Math.abs(p) < 0.05) {
                return "= 0%";
            }
            return (p > 0 ? "\u25B2 " : "\u25BC ") + String.format(java.util.Locale.US, "%.1f%%", Math.abs(p));
        }

        static java.awt.Color warnaDelta(int ini, int lalu) {
            return ini > lalu ? NAIK : (ini < lalu ? TURUN : TEKS_REDUP);
        }

        /**
         * Rentang tanggal {dari, sampai, dariLalu, sampaiLalu} untuk tahun dan bulan (0 = setahun). Periode berjalan dipotong
         * sampai hari ini, pembandingnya = tanggal yang sama setahun sebelumnya (periode yang sama).
         */
        static java.time.LocalDate[] rentang(int tahun, int bulan) {
            java.time.LocalDate hari = java.time.LocalDate.now(), dari, sampai;
            if (bulan == 0) {
                dari = java.time.LocalDate.of(tahun, 1, 1);
                sampai = tahun == hari.getYear() ? hari : java.time.LocalDate.of(tahun, 12, 31);
            } else {
                dari = java.time.LocalDate.of(tahun, bulan, 1);
                java.time.LocalDate akhir = dari.withDayOfMonth(dari.lengthOfMonth());
                sampai = (tahun == hari.getYear() && bulan == hari.getMonthValue()) ? hari : akhir;
            }
            return new java.time.LocalDate[]{dari, sampai, dari.minusYears(1), sampai.minusYears(1)};
        }

        /**
         * Versi dengan pembanding (periode yang sama tahun lalu). indeksWarna dipakai bersama antar-pemanggilan agar warna
         * tiap jenis bayar tidak berubah saat bulan diganti. Operator yang hanya ada di salah satu periode ikut tampil.
         */
        static Hasil susun(java.util.Map<String, java.util.Map<String, Integer>> ini, java.util.Map<String, java.util.Map<String, Integer>> lalu,
                String stackKosong, java.util.Map<String, Integer> indeksWarna, String namaPembanding) {
            Hasil h = new Hasil();
            h.bandingkan = true;
            h.namaPembanding = namaPembanding;
            java.util.Map<String, DataOperator> peta = new java.util.LinkedHashMap<>();
            for (java.util.Map.Entry<String, java.util.Map<String, Integer>> e : ini.entrySet()) {
                DataOperator d = peta.computeIfAbsent(e.getKey(), DataOperator::new);
                for (java.util.Map.Entry<String, Integer> b : e.getValue().entrySet()) {
                    d.perBayar.put(b.getKey(), b.getValue());
                    d.jumlah += b.getValue();
                    h.totalBayar.merge(b.getKey(), b.getValue(), Integer::sum);
                    h.total += b.getValue();
                }
            }
            for (java.util.Map.Entry<String, java.util.Map<String, Integer>> e : lalu.entrySet()) {
                DataOperator d = peta.computeIfAbsent(e.getKey(), DataOperator::new);
                for (java.util.Map.Entry<String, Integer> b : e.getValue().entrySet()) {
                    d.perBayarLalu.put(b.getKey(), b.getValue());
                    d.jumlahLalu += b.getValue();
                    h.totalBayarLalu.merge(b.getKey(), b.getValue(), Integer::sum);
                    h.totalLalu += b.getValue();
                }
            }
            h.operator.addAll(peta.values());
            h.operator.sort((a, b) -> {
                if (a.jumlah != b.jumlah) {
                    return Integer.compare(b.jumlah, a.jumlah);
                }
                if (a.jumlahLalu != b.jumlahLalu) {
                    return Integer.compare(b.jumlahLalu, a.jumlahLalu);
                }
                return a.nama.compareToIgnoreCase(b.nama);
            });
            h.urutanBayar.addAll(h.totalBayar.keySet());
            h.urutanBayar.sort((a, b) -> {
                int ca = h.totalBayar.get(a), cb = h.totalBayar.get(b);
                return ca != cb ? Integer.compare(cb, ca) : a.compareToIgnoreCase(b);
            });
            java.util.List<String> hanyaLalu = new java.util.ArrayList<>();
            for (String b : h.totalBayarLalu.keySet()) {
                if (!h.totalBayar.containsKey(b)) {
                    hanyaLalu.add(b);
                }
            }
            hanyaLalu.sort((a, b) -> {
                int ca = h.totalBayarLalu.get(a), cb = h.totalBayarLalu.get(b);
                return ca != cb ? Integer.compare(cb, ca) : a.compareToIgnoreCase(b);
            });
            h.urutanBayar.addAll(hanyaLalu);
            for (String b : h.urutanBayar) {
                if (b.equals(stackKosong)) {
                    h.warna.put(b, ABU);
                } else {
                    Integer idx = indeksWarna.get(b);
                    if (idx == null) {
                        idx = indeksWarna.size();
                        indeksWarna.put(b, idx);
                    }
                    h.warna.put(b, idx < PALET.length ? PALET[idx] : warnaHsb(idx));
                }
            }
            return h;
        }

        /** Warna tambahan (bila kategori > 8): sebaran hue sudut emas agar tetap berbeda-beda. */
        private static java.awt.Color[] warnaHsb(int idx) {
            float hue = (float) ((idx * 0.61803398875) % 1.0);
            return new java.awt.Color[]{java.awt.Color.getHSBColor(hue, 0.45f, 1.0f), java.awt.Color.getHSBColor(hue, 0.85f, 0.78f)};
        }

        // ------------------------------------------------------------------
        static final class HeaderPanel extends javax.swing.JPanel {
            private final String periode, judul, label, labelStack;
            private final boolean sederhana;
            private final int totalOperasi, jumlahOperator;
            private final String topNama, namaPembanding;
            private final int topJumlah, totalLalu;
            private final boolean bandingkan;

            HeaderPanel(String periode, Hasil h) {
                this.totalLalu = h.totalLalu;
                this.bandingkan = h.bandingkan;
                this.namaPembanding = h.namaPembanding;
                this.periode = periode;
                this.judul = h.judul;
                this.label = h.label;
                this.labelStack = h.labelStack;
                this.sederhana = h.sederhana;
                this.totalOperasi = h.total;
                this.jumlahOperator = h.operator.size();
                this.topNama = h.operator.isEmpty() ? "-" : h.operator.get(0).nama;
                this.topJumlah = h.operator.isEmpty() ? 0 : h.operator.get(0).jumlah;
                setPreferredSize(new java.awt.Dimension(900, 150));
                setOpaque(true);
            }

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setPaint(new java.awt.LinearGradientPaint(0, 0, Math.max(w, 2), 0, new float[]{0f, 0.55f, 1f},
                        new java.awt.Color[]{new java.awt.Color(0x0F, 0x20, 0x27), new java.awt.Color(0x20, 0x3A, 0x43), new java.awt.Color(0x2C, 0x53, 0x64)}));
                g2.fillRect(0, 0, w, h);
                g2.setPaint(new java.awt.RadialGradientPaint(w - 80, 10, 260f, new float[]{0f, 1f},
                        new java.awt.Color[]{new java.awt.Color(0x00, 0xD2, 0xFF, 70), new java.awt.Color(0x00, 0xD2, 0xFF, 0)}));
                g2.fillRect(0, 0, w, h);

                g2.setColor(java.awt.Color.WHITE);
                g2.setFont(FontCache.get(java.awt.Font.BOLD, 24));
                g2.drawString(judul, 28, 42);
                g2.setColor(new java.awt.Color(0x9F, 0xD8, 0xF0));
                g2.setFont(FontCache.get(java.awt.Font.PLAIN, 13));
                g2.drawString("Periode : " + periode + (sederhana ? "" : "   \u2022   dibagi menurut " + labelStack), 29, 64);

                int n = bandingkan ? 4 : 3, idxTop = n - 1;
                String[] teksLabel = new String[n], nilai = new String[n];
                int[] lebar = new int[n];
                teksLabel[0] = "TOTAL OPERASI";
                nilai[0] = String.valueOf(totalOperasi);
                teksLabel[1] = "JUMLAH " + label.toUpperCase(java.util.Locale.ROOT);
                nilai[1] = String.valueOf(jumlahOperator);
                if (bandingkan) {
                    int selisih = totalOperasi - totalLalu;
                    teksLabel[2] = "VS " + namaPembanding.toUpperCase(java.util.Locale.ROOT) + "  (" + totalLalu + ")";
                    nilai[2] = deltaPersen(totalOperasi, totalLalu) + "  \u2022  " + (selisih >= 0 ? "+" : "") + selisih;
                }
                teksLabel[idxTop] = "TERBANYAK";
                nilai[idxTop] = topJumlah + "  \u2022  " + topNama;
                int tetap = 0;
                for (int i = 0; i < idxTop; i++) {
                    lebar[i] = (bandingkan && i == 2) ? 240 : 170;
                    tetap += lebar[i];
                }
                lebar[idxTop] = Math.max(240, w - 28 - tetap - 12 * idxTop - 28);
                int x = 28, y = 80;
                for (int i = 0; i < n; i++) {
                    boolean kartuDelta = bandingkan && i == 2;
                    g2.setColor(new java.awt.Color(255, 255, 255, 26));
                    g2.fillRoundRect(x, y, lebar[i], 56, 16, 16);
                    g2.setColor(new java.awt.Color(255, 255, 255, 60));
                    g2.drawRoundRect(x, y, lebar[i], 56, 16, 16);
                    g2.setFont(FontCache.get(java.awt.Font.BOLD, 10));
                    g2.setColor(new java.awt.Color(0x9F, 0xB8, 0xCC));
                    g2.drawString(teksLabel[i], x + 14, y + 19);
                    g2.setFont(FontCache.get(java.awt.Font.BOLD, i == idxTop ? 15 : (kartuDelta ? 19 : 22)));
                    g2.setColor(i == idxTop ? EMAS : (kartuDelta ? warnaDelta(totalOperasi, totalLalu) : java.awt.Color.WHITE));
                    String v = nilai[i];
                    java.awt.FontMetrics fm = g2.getFontMetrics();
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
        static final class BarChartPanel extends javax.swing.JPanel implements javax.swing.Scrollable {
            private static final int BAR_H = 28, LEGEND_H = 38, BOTTOM = 26, MARGIN_KANAN = 190;
            private final int rowH, top; // tinggi baris & posisi awal (berbeda untuk mode sederhana)
            private final Hasil hasil;
            private final java.util.List<DataOperator> data;
            private final int total;
            private final int max;
            private final int[] rank; // peringkat (jumlah sama = peringkat sama)
            private boolean statis = false; // true = tanpa animasi (mis. saat simpan gambar)
            private long mulai = 0;
            private javax.swing.Timer timer;
            private int hover = -1;

            BarChartPanel(Hasil hasil) {
                this.hasil = hasil;
                this.rowH = hasil.sederhana ? 58 : (hasil.bandingkan ? 90 : 78);
                this.top = hasil.sederhana ? 56 : LEGEND_H + 46;
                this.data = hasil.operator;
                this.total = Math.max(1, hasil.total);
                int m = 1;
                for (DataOperator d : data) {
                    m = Math.max(m, Math.max(d.jumlah, d.jumlahLalu));
                }
                this.max = m;
                this.rank = new int[data.size()];
                for (int i = 0; i < data.size(); i++) {
                    rank[i] = (i > 0 && data.get(i).jumlah == data.get(i - 1).jumlah) ? rank[i - 1] : i + 1;
                }
                setOpaque(true);
                setPreferredSize(new java.awt.Dimension(900, Math.max(300, top + data.size() * rowH + tinggiRingkasan() + BOTTOM)));
                javax.swing.ToolTipManager.sharedInstance().registerComponent(this);
                addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(java.awt.event.MouseEvent e) {
                        int idx = (e.getY() - top) / rowH;
                        if (e.getY() < top || idx < 0 || idx >= data.size()) {
                            idx = -1;
                        }
                        if (idx != hover) {
                            hover = idx;
                            setToolTipText(idx >= 0 ? tooltip(data.get(idx)) : null);
                            repaint();
                        }
                    }
                });
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        if (hover != -1) {
                            hover = -1;
                            repaint();
                        }
                    }
                });
            }

            private int tinggiRingkasan() {
                if (hasil.sederhana) {
                    return 70;
                }
                return 18 + 34 + hasil.urutanBayar.size() * 32 + 52;
            }

            private String tooltip(DataOperator d) {
                StringBuilder sb = new StringBuilder("<html><b>").append(esc(d.nama)).append("</b><br>");
                if (!hasil.sederhana) {
                    for (String b : hasil.urutanBayar) {
                        Integer c = d.perBayar.get(b);
                        if (c != null && c > 0) {
                            sb.append(esc(b)).append(" : <b>").append(c).append("</b> operasi<br>");
                        }
                    }
                }
                String ct = hasil.catatan.get(d.nama);
                if (ct != null) {
                    sb.append("<i>").append(esc(ct)).append("</i><br>");
                }
                if (hasil.bandingkan) {
                    sb.append(esc(hasil.namaPembanding)).append(" : <b>").append(d.jumlahLalu).append("</b> operasi &nbsp;")
                      .append(deltaPersen(d.jumlah, d.jumlahLalu)).append("<br>");
                }
                sb.append("<b>Total : ").append(d.jumlah).append(" operasi</b> (")
                  .append(String.format(java.util.Locale.US, "%.1f", d.jumlah * 100.0 / total)).append("%)</html>");
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
                timer = new javax.swing.Timer(16, e -> {
                    repaint();
                    if (System.currentTimeMillis() - mulai >= durasiTotal + 40) {
                        ((javax.swing.Timer) e.getSource()).stop();
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

            private static java.awt.Color medali(int rank) {
                switch (rank) {
                    case 1:
                        return new java.awt.Color(0xFF, 0xC8, 0x37);
                    case 2:
                        return new java.awt.Color(0xC9, 0xD1, 0xDB);
                    default:
                        return new java.awt.Color(0xD9, 0x8E, 0x4A);
                }
            }

            private static java.awt.Color alfa(java.awt.Color c, int a) {
                return new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
            }

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
                int w = getWidth(), h = getHeight();
                g2.setPaint(new java.awt.GradientPaint(0, 0, BG1, 0, h, BG2));
                g2.fillRect(0, 0, w, h);
                if (data.isEmpty()) {
                    g2.setFont(FontCache.get(java.awt.Font.PLAIN, 15));
                    g2.setColor(TEKS_REDUP);
                    String kosong = hasil.bandingkan ? "Tidak ada operasi pada periode ini maupun periode pembandingnya." : "Tidak ada data.";
                    g2.drawString(kosong, (w - g2.getFontMetrics().stringWidth(kosong)) / 2, 120);
                    g2.dispose();
                    return;
                }

                final java.awt.Font fNama = FontCache.get(java.awt.Font.PLAIN, 14);
                final java.awt.Font fNamaTop = FontCache.get(java.awt.Font.BOLD, 14);
                final java.awt.Font fNilai = FontCache.get(java.awt.Font.BOLD, 14);
                final java.awt.Font fKecil = FontCache.get(java.awt.Font.PLAIN, 12);
                final java.awt.Font fKecilTebal = FontCache.get(java.awt.Font.BOLD, 12);

                // ---------- lebar kolom nama & area plot ----------
                g2.setFont(fNamaTop);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int namaW = 150;
                for (DataOperator d : data) {
                    namaW = Math.max(namaW, fm.stringWidth(d.nama) + 8);
                }
                namaW = Math.min(namaW, Math.max(150, w / 3));
                int xBadge = 24;
                int xNama = xBadge + 28 + 12;
                int x0 = xNama + namaW + 16;
                int plotW = Math.max(120, w - x0 - MARGIN_KANAN - (hasil.bandingkan ? 96 : 0));

                g2.setFont(fKecil);
                java.awt.FontMetrics fl = g2.getFontMetrics();
                if (!hasil.sederhana) {
                // ---------- legenda jenis bayar (satu baris) ----------
                int lx = 24, ly = 24;
                int batasLegend = w - 24 - (hasil.bandingkan ? 150 : 0);
                g2.setColor(TEKS_REDUP);
                g2.setFont(FontCache.get(java.awt.Font.BOLD, 10));
                String judulLegenda = hasil.labelStack.toUpperCase(java.util.Locale.ROOT);
                g2.drawString(judulLegenda, lx, ly + 4);
                lx += g2.getFontMetrics().stringWidth(judulLegenda) + 14;
                g2.setFont(fKecil);
                int tersisa = hasil.urutanBayar.size();
                for (String b : hasil.urutanBayar) {
                    int lebarItem = 22 + fl.stringWidth(b) + 18;
                    // sisakan ruang untuk tulisan "+N lainnya" bila masih ada item setelah ini
                    int cadangan = (tersisa > 1) ? fl.stringWidth("+" + (tersisa - 1) + " lainnya") + 8 : 0;
                    if (lx + lebarItem + cadangan > batasLegend && tersisa > 0 && lx > 24) {
                        String lain = "+" + tersisa + " lainnya";
                        g2.setColor(TEKS_REDUP);
                        g2.drawString(lain, lx, ly + 4);
                        break;
                    }
                    java.awt.Color[] c = hasil.warnaBayar(b);
                    g2.setPaint(new java.awt.GradientPaint(lx, ly - 6, c[0], lx, ly + 8, c[1]));
                    g2.fillRoundRect(lx, ly - 6, 14, 14, 5, 5);
                    g2.setColor(TEKS);
                    g2.drawString(b, lx + 20, ly + 5);
                    lx += lebarItem;
                    tersisa--;
                }
                if (hasil.bandingkan) { // keterangan batang bayangan (pembanding)
                    g2.setFont(fKecil);
                    String tg = "= " + hasil.namaPembanding;
                    int gx0 = w - 24 - g2.getFontMetrics().stringWidth(tg) - 34;
                    g2.setColor(alfa(EMAS, 70));
                    g2.fillRoundRect(gx0, ly - 2, 26, 7, 7, 7);
                    g2.setColor(alfa(EMAS, 200));
                    g2.drawRoundRect(gx0, ly - 2, 26, 7, 7, 7);
                    g2.setColor(TEKS_REDUP);
                    g2.drawString(tg, gx0 + 32, ly + 5);
                }

                }

                // ---------- grid + sumbu ----------
                double step = langkahBagus(max / 5.0);
                double axisMax = Math.ceil(max / step) * step;
                if (axisMax < max) {
                    axisMax = max;
                }
                int gridBawah = top + data.size() * rowH;
                g2.setFont(fKecil);
                for (double v = 0; v <= axisMax + 1e-9; v += step) {
                    int gx = x0 + (int) Math.round(plotW * v / axisMax);
                    g2.setColor(new java.awt.Color(255, 255, 255, v == 0 ? 60 : 22));
                    g2.drawLine(gx, top - 8, gx, gridBawah);
                    String lb = String.valueOf((long) Math.round(v));
                    g2.setColor(TEKS_REDUP);
                    g2.drawString(lb, gx - g2.getFontMetrics().stringWidth(lb) / 2, top - 16);
                }

                // ---------- baris operator ----------
                for (int i = 0; i < data.size(); i++) {
                    DataOperator d = data.get(i);
                    int y = top + i * rowH;
                    int barTop = y + 12;
                    int cy = barTop + BAR_H / 2;
                    double p = progress(i);

                    if (i == hover) {
                        g2.setColor(new java.awt.Color(255, 255, 255, 18));
                        g2.fillRoundRect(10, y + 2, w - 20, rowH - 4, 14, 14);
                    }

                    // lencana peringkat
                    boolean medaliAda = rank[i] <= 3 && d.jumlah > 0;
                    if (medaliAda) {
                        java.awt.Color mc = medali(rank[i]);
                        g2.setPaint(new java.awt.GradientPaint(xBadge, cy - 14, mc.brighter(), xBadge, cy + 14, mc.darker()));
                    } else {
                        g2.setColor(new java.awt.Color(0x2B, 0x3A, 0x5C));
                    }
                    g2.fillOval(xBadge, cy - 14, 28, 28);
                    if (!medaliAda) {
                        g2.setColor(new java.awt.Color(255, 255, 255, 50));
                        g2.drawOval(xBadge, cy - 14, 28, 28);
                    }
                    g2.setFont(FontCache.get(java.awt.Font.BOLD, 13));
                    g2.setColor(medaliAda ? new java.awt.Color(0x1A, 0x1A, 0x2E) : TEKS);
                    String rk = String.valueOf(rank[i]);
                    g2.drawString(rk, xBadge + 14 - g2.getFontMetrics().stringWidth(rk) / 2, cy + 5);

                    // nama operator
                    g2.setFont(rank[i] == 1 ? fNamaTop : fNama);
                    g2.setColor(TEKS);
                    java.awt.FontMetrics fn = g2.getFontMetrics();
                    String nm = d.nama;
                    if (fn.stringWidth(nm) > namaW) {
                        while (nm.length() > 1 && fn.stringWidth(nm + "\u2026") > namaW) {
                            nm = nm.substring(0, nm.length() - 1);
                        }
                        nm = nm + "\u2026";
                    }
                    g2.drawString(nm, xNama, cy + 5);
                    String ct = hasil.catatan.get(d.nama);
                    if (ct != null) {
                        g2.setFont(FontCache.get(java.awt.Font.ITALIC, 11));
                        g2.setColor(new java.awt.Color(0xF0, 0xC0, 0x6A));
                        String ringkas = ct.substring(0, ct.indexOf(':')); // "3 penulisan digabung"
                        g2.drawString(ringkas, xNama, cy + 21);
                    }

                    // ---- batang bertumpuk per jenis bayar ----
                    int bw = (int) Math.round(plotW * d.jumlah / axisMax * p);
                    if (d.jumlah > 0 && p > 0) {
                        bw = Math.max(bw, (int) (BAR_H * Math.min(1.0, p)));
                    }
                    if (bw > 0) {
                        java.awt.Shape bar = new java.awt.geom.RoundRectangle2D.Float(x0, barTop, bw, BAR_H, BAR_H, BAR_H);
                        g2.setColor(new java.awt.Color(0, 0, 0, 80));
                        g2.fill(new java.awt.geom.RoundRectangle2D.Float(x0, barTop + 4, bw, BAR_H, BAR_H, BAR_H));
                        if (i == hover) {
                            g2.setColor(new java.awt.Color(255, 255, 255, 40));
                            g2.fill(new java.awt.geom.RoundRectangle2D.Float(x0 - 4, barTop - 4, bw + 8, BAR_H + 8, BAR_H + 8, BAR_H + 8));
                        }
                        java.awt.Shape oldClip = g2.getClip();
                        g2.clip(bar);
                        int cum = 0;
                        int xPrev = x0;
                        for (String b : hasil.urutanBayar) {
                            Integer cnt = d.perBayar.get(b);
                            if (cnt == null || cnt == 0) {
                                continue;
                            }
                            cum += cnt;
                            int xAkhir = (cum == d.jumlah) ? x0 + bw : x0 + (int) Math.round(bw * (double) cum / d.jumlah);
                            java.awt.Color[] c = hasil.warnaBayar(b);
                            g2.setPaint(new java.awt.GradientPaint(0, barTop, c[0], 0, barTop + BAR_H, c[1]));
                            g2.fillRect(xPrev, barTop, Math.max(1, xAkhir - xPrev), BAR_H);
                            if (xPrev > x0) { // garis pemisah antar segmen
                                g2.setColor(new java.awt.Color(11, 19, 43, 170));
                                g2.fillRect(xPrev - 1, barTop, 2, BAR_H);
                            }
                            // angka di dalam segmen bila cukup lebar
                            if (!hasil.sederhana && xAkhir - xPrev >= 26) {
                                g2.setFont(fKecilTebal);
                                String t = String.valueOf(cnt);
                                int tx = xPrev + (xAkhir - xPrev - g2.getFontMetrics().stringWidth(t)) / 2;
                                g2.setColor(new java.awt.Color(0, 0, 0, 90));
                                g2.drawString(t, tx + 1, cy + 5);
                                g2.setColor(java.awt.Color.WHITE);
                                g2.drawString(t, tx, cy + 4);
                            }
                            xPrev = xAkhir;
                        }
                        // kilau kaca di setengah bagian atas
                        g2.setPaint(new java.awt.GradientPaint(0, barTop, new java.awt.Color(255, 255, 255, 80), 0, barTop + BAR_H * 0.55f, new java.awt.Color(255, 255, 255, 0)));
                        g2.fillRect(x0, barTop, bw, BAR_H);
                        g2.setClip(oldClip);
                        if (i == hover) {
                            g2.setColor(new java.awt.Color(255, 255, 255, 190));
                            g2.setStroke(new java.awt.BasicStroke(1.6f));
                            g2.draw(bar);
                        }
                    }

                    // batang bayangan = pembanding (periode yang sama tahun lalu)
                    if (hasil.bandingkan && !hasil.sederhana) {
                        int gh = 7, gy = barTop + BAR_H + 6;
                        int gw = (int) Math.round(plotW * d.jumlahLalu / axisMax * p);
                        if (d.jumlahLalu > 0 && gw > 0) {
                            gw = Math.max(gw, gh);
                            g2.setPaint(new java.awt.GradientPaint(x0, 0, alfa(EMAS, 60), x0 + gw, 0, alfa(EMAS, 190)));
                            g2.fillRoundRect(x0, gy, gw, gh, gh, gh);
                            g2.setColor(alfa(EMAS, 210));
                            g2.setStroke(new java.awt.BasicStroke(1f));
                            g2.drawRoundRect(x0, gy, gw, gh, gh, gh);
                            g2.setFont(FontCache.get(java.awt.Font.PLAIN, 11));
                            g2.setColor(alfa(EMAS, (int) (230 * Math.min(1.0, p * 1.3))));
                            g2.drawString(hasil.namaPembanding + " : " + d.jumlahLalu, x0 + gw + 8, gy + 8);
                        } else if (d.jumlahLalu == 0) {
                            g2.setFont(FontCache.get(java.awt.Font.PLAIN, 11));
                            g2.setColor(alfa(TEKS_REDUP, 200));
                            g2.drawString(hasil.namaPembanding + " : 0", x0, gy + 8);
                        }
                    }

                    // total + persentase di ujung batang
                    String nilai = Math.round(d.jumlah * p) + " operasi";
                    g2.setFont(fNilai);
                    g2.setColor(rank[i] == 1 ? EMAS : java.awt.Color.WHITE);
                    int tx = x0 + Math.max(bw, 0) + 12;
                    g2.drawString(nilai, tx, cy + 5);
                    int nw = g2.getFontMetrics().stringWidth(nilai);
                    g2.setFont(fKecil);
                    g2.setColor(TEKS_REDUP);
                    g2.drawString(String.format(java.util.Locale.US, "(%.1f%%)", d.jumlah * 100.0 / total), tx + nw + 8, cy + 5);
                    if (hasil.bandingkan) { // chip selisih terhadap tahun lalu
                        String pctTeks = String.format(java.util.Locale.US, "(%.1f%%)", d.jumlah * 100.0 / total);
                        int cxChip = tx + nw + 8 + g2.getFontMetrics().stringWidth(pctTeks) + 12;
                        String dt = deltaPersen(d.jumlah, d.jumlahLalu);
                        g2.setFont(fKecilTebal);
                        int cw2 = g2.getFontMetrics().stringWidth(dt) + 18;
                        java.awt.Color wd = warnaDelta(d.jumlah, d.jumlahLalu);
                        g2.setColor(alfa(wd, 38));
                        g2.fillRoundRect(cxChip, cy - 10, cw2, 20, 20, 20);
                        g2.setColor(alfa(wd, 130));
                        g2.setStroke(new java.awt.BasicStroke(1f));
                        g2.drawRoundRect(cxChip, cy - 10, cw2, 20, 20, 20);
                        g2.setColor(wd);
                        g2.drawString(dt, cxChip + 9, cy + 4);
                    }

                    if (!hasil.sederhana) {
                    // ---- rincian di bawah batang: "• Reguler 3   • BPJS 2   |  Total 5" ----
                    int alpha = (int) (255 * Math.min(1.0, p * 1.3));
                    int baris = barTop + BAR_H + (hasil.bandingkan ? 33 : 19);
                    int tersedia = w - x0 - 16;
                    java.util.List<String> nmItem = new java.util.ArrayList<>();
                    java.util.List<Integer> jmItem = new java.util.ArrayList<>();
                    for (String b : hasil.urutanBayar) {
                        Integer cnt = d.perBayar.get(b);
                        if (cnt != null && cnt > 0) {
                            nmItem.add(b);
                            jmItem.add(cnt);
                        }
                    }
                    java.awt.FontMetrics fTebal = g2.getFontMetrics(fKecilTebal);
                    int lebarTotalTeks = fTebal.stringWidth("Total " + d.jumlah) + 18;
                    boolean singkat = false;
                    int[] lebarItem = new int[nmItem.size()];
                    for (int percobaan = 0; percobaan < 2; percobaan++) {
                        int jumlahLebar = lebarTotalTeks;
                        for (int k = 0; k < lebarItem.length; k++) {
                            lebarItem[k] = 14 + fl.stringWidth(singkatNama(nmItem.get(k), singkat) + " ") + fTebal.stringWidth(String.valueOf(jmItem.get(k))) + 16;
                            jumlahLebar += lebarItem[k];
                        }
                        if (jumlahLebar <= tersedia || singkat) {
                            break;
                        }
                        singkat = true;
                    }
                    // berapa item yang muat (Total selalu ditampilkan)
                    int lebarLain = fl.stringWidth("+99 lainnya") + 16;
                    int dipakai = 0, tampil = 0;
                    for (int k = 0; k < lebarItem.length; k++) {
                        int sisaCadangan = (k < lebarItem.length - 1) ? lebarLain : 0;
                        if (tampil >= 1 && dipakai + lebarItem[k] + lebarTotalTeks + sisaCadangan > tersedia) {
                            break;
                        }
                        dipakai += lebarItem[k];
                        tampil++;
                    }
                    int bx = x0;
                    for (int k = 0; k < tampil; k++) {
                        java.awt.Color[] c = hasil.warnaBayar(nmItem.get(k));
                        g2.setColor(alfa(c[1].brighter(), alpha));
                        g2.fillOval(bx, baris - 9, 9, 9);
                        g2.setFont(fKecil);
                        g2.setColor(alfa(TEKS, alpha));
                        String t = singkatNama(nmItem.get(k), singkat) + " ";
                        g2.drawString(t, bx + 14, baris);
                        int tw = g2.getFontMetrics().stringWidth(t);
                        g2.setFont(fKecilTebal);
                        g2.drawString(String.valueOf(jmItem.get(k)), bx + 14 + tw, baris);
                        bx += lebarItem[k];
                    }
                    if (tampil < lebarItem.length) {
                        g2.setFont(fKecil);
                        g2.setColor(alfa(TEKS_REDUP, alpha));
                        g2.drawString("+" + (lebarItem.length - tampil) + " lainnya", bx, baris);
                        bx += fl.stringWidth("+" + (lebarItem.length - tampil) + " lainnya") + 16;
                    }
                    g2.setColor(alfa(java.awt.Color.WHITE, (int) (alpha * 0.25)));
                    g2.fillRect(bx - 6, baris - 11, 1, 13);
                    g2.setFont(fKecilTebal);
                    g2.setColor(alfa(EMAS, alpha));
                    g2.drawString("Total " + d.jumlah, bx + 6, baris);
                    }
                }

                if (hasil.sederhana) { // mode sederhana: hanya baris TOTAL di bawah
                    int sy = gridBawah + 14;
                    g2.setColor(new java.awt.Color(255, 255, 255, 40));
                    g2.drawLine(24, sy, w - 24, sy);
                    g2.setFont(FontCache.get(java.awt.Font.BOLD, 15));
                    g2.setColor(EMAS);
                    g2.drawString("TOTAL", xNama, sy + 30);
                    String tt = hasil.total + " operasi";
                    g2.drawString(tt, x0, sy + 30);
                    g2.setFont(fKecil);
                    g2.setColor(TEKS_REDUP);
                    g2.drawString("dari " + data.size() + " " + hasil.label,
                            x0 + g2.getFontMetrics(FontCache.get(java.awt.Font.BOLD, 15)).stringWidth(tt) + 12, sy + 30);
                    g2.dispose();
                    return;
                }
                // ---------- ringkasan total per jenis bayar (di bawah semua batang) ----------
                int ry = gridBawah + 18;
                g2.setColor(new java.awt.Color(255, 255, 255, 40));
                g2.drawLine(24, ry, w - 24, ry);
                g2.setFont(FontCache.get(java.awt.Font.BOLD, 11));
                g2.setColor(TEKS_REDUP);
                g2.drawString("TOTAL PER " + hasil.labelStack.toUpperCase(java.util.Locale.ROOT), 24, ry + 22);
                int maxBayar = 1;
                for (int v : hasil.totalBayar.values()) {
                    maxBayar = Math.max(maxBayar, v);
                }
                int rowY = ry + 34;
                for (String b : hasil.urutanBayar) {
                    int cnt = hasil.totalBayar.getOrDefault(b, 0); // jenis bayar yang hanya ada di periode pembanding tidak ada di totalBayar -> sebelumnya NPE
                    java.awt.Color[] c = hasil.warnaBayar(b);
                    int cy2 = rowY + 14;
                    g2.setPaint(new java.awt.GradientPaint(24, cy2 - 7, c[0], 24, cy2 + 7, c[1]));
                    g2.fillRoundRect(24, cy2 - 7, 14, 14, 5, 5);
                    g2.setFont(fNama);
                    g2.setColor(TEKS);
                    java.awt.FontMetrics fb = g2.getFontMetrics();
                    String nm = b;
                    int lebarNama = x0 - 16 - 48;
                    if (fb.stringWidth(nm) > lebarNama) {
                        while (nm.length() > 1 && fb.stringWidth(nm + "\u2026") > lebarNama) {
                            nm = nm.substring(0, nm.length() - 1);
                        }
                        nm += "\u2026";
                    }
                    g2.drawString(nm, 48, cy2 + 5);
                    int lw = Math.max(8, (int) Math.round(plotW * cnt / (double) maxBayar * (statis ? 1.0 : progress(0))));
                    g2.setColor(new java.awt.Color(0, 0, 0, 70));
                    g2.fillRoundRect(x0, cy2 - 6 + 3, lw, 12, 12, 12);
                    g2.setPaint(new java.awt.GradientPaint(x0, 0, c[1], x0 + lw, 0, c[0]));
                    g2.fillRoundRect(x0, cy2 - 6, lw, 12, 12, 12);
                    g2.setFont(fNilai);
                    g2.setColor(java.awt.Color.WHITE);
                    String t = cnt + " operasi";
                    g2.drawString(t, x0 + lw + 12, cy2 + 5);
                    g2.setFont(fKecil);
                    g2.setColor(TEKS_REDUP);
                    String pctB = String.format(java.util.Locale.US, "(%.1f%%)", cnt * 100.0 / total);
                    g2.drawString(pctB, x0 + lw + 12 + g2.getFontMetrics(fNilai).stringWidth(t) + 8, cy2 + 5);
                    if (hasil.bandingkan) {
                        int cl = hasil.totalBayarLalu.getOrDefault(b, 0);
                        int xd = x0 + lw + 12 + g2.getFontMetrics(fNilai).stringWidth(t) + 8 + g2.getFontMetrics(fKecil).stringWidth(pctB) + 14;
                        g2.setFont(fKecilTebal);
                        g2.setColor(warnaDelta(cnt, cl));
                        g2.drawString(deltaPersen(cnt, cl) + "   vs " + cl, xd, cy2 + 5);
                    }
                    rowY += 32;
                }
                // baris TOTAL KESELURUHAN
                int ty = rowY + 6;
                g2.setColor(new java.awt.Color(255, 255, 255, 40));
                g2.drawLine(24, ty, w - 24, ty);
                g2.setFont(FontCache.get(java.awt.Font.BOLD, 15));
                g2.setColor(EMAS);
                g2.drawString("TOTAL", 48, ty + 28);
                g2.drawString(hasil.total + " operasi", x0, ty + 28);
                g2.setFont(fKecil);
                g2.setColor(TEKS_REDUP);
                String sDari = "dari " + data.size() + " " + hasil.label;
                int xDari = x0 + g2.getFontMetrics(FontCache.get(java.awt.Font.BOLD, 15)).stringWidth(hasil.total + " operasi") + 12;
                g2.drawString(sDari, xDari, ty + 28);
                if (hasil.bandingkan) {
                    g2.setFont(FontCache.get(java.awt.Font.BOLD, 13));
                    g2.setColor(warnaDelta(hasil.total, hasil.totalLalu));
                    g2.drawString(deltaPersen(hasil.total, hasil.totalLalu) + "   vs " + hasil.totalLalu + " (" + hasil.namaPembanding + ")",
                            xDari + g2.getFontMetrics(fKecil).stringWidth(sDari) + 24, ty + 28);
                }
                g2.dispose();
            }

            private static String singkatNama(String s, boolean singkat) {
                if (!singkat || s.length() <= 16) {
                    return s;
                }
                return s.substring(0, 15) + "\u2026";
            }

            // --- Scrollable ---
            @Override
            public java.awt.Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }

            @Override
            public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) {
                return 28;
            }

            @Override
            public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) {
                return Math.max(60, r.height - 60);
            }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }

            @Override
            public boolean getScrollableTracksViewportHeight() {
                return getParent() instanceof javax.swing.JViewport
                        && ((javax.swing.JViewport) getParent()).getHeight() > getPreferredSize().height;
            }
        }

        // ------------------------------------------------------------------
        //  Pemilih bulan + tahun: chip Setahun/Jan..Des (berisi jumlah operasi + mini-bar tahun ini vs tahun lalu)
        // ------------------------------------------------------------------
        static final class PemilihBulan extends javax.swing.JComponent {

            static final int TINGGI = 84;
            private static final String[] BLN = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
            private static final java.awt.Color BIRU1 = new java.awt.Color(0x2F, 0x80, 0xED), BIRU2 = new java.awt.Color(0x00, 0xD2, 0xFF);
            private static final int GAP = 6, BOX_W = 272, H_CHIP = 62;

            private final java.util.function.IntConsumer aksiPilih, aksiTahun;
            private final Runnable aksiSekarang;
            private int tahun = java.time.LocalDate.now().getYear(), sel = 0, hover = -1, hoverBtn = 0;
            private int[] ini = new int[12], lalu = new int[12];
            private boolean aktif = true, memuat = false, snap = true;
            private double pillX = 0, pillW = 60, pillTX = 0, pillTW = 60;
            private javax.swing.Timer timer, timerMuat;
            private final java.awt.Rectangle[] rChip = new java.awt.Rectangle[13];
            private java.awt.Rectangle rYPrev = new java.awt.Rectangle(), rYLabel = new java.awt.Rectangle(), rYNext = new java.awt.Rectangle(),
                    rNow = new java.awt.Rectangle(), rBox = new java.awt.Rectangle();

            PemilihBulan(java.util.function.IntConsumer aksiPilih, java.util.function.IntConsumer aksiTahun, Runnable aksiSekarang) {
                this.aksiPilih = aksiPilih;
                this.aksiTahun = aksiTahun;
                this.aksiSekarang = aksiSekarang;
                setPreferredSize(new java.awt.Dimension(100, TINGGI));
                setOpaque(true);
                javax.swing.ToolTipManager.sharedInstance().registerComponent(this);
                addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(java.awt.event.MouseEvent e) {
                        susun();
                        int c = chipDi(e.getX(), e.getY()), b = tombolDi(e.getX(), e.getY());
                        if (c != hover || b != hoverBtn) {
                            hover = c;
                            hoverBtn = b;
                            setCursor(java.awt.Cursor.getPredefinedCursor(aktif && (bisaPilih(c) || b > 0) ? java.awt.Cursor.HAND_CURSOR : java.awt.Cursor.DEFAULT_CURSOR));
                            setToolTipText(c >= 0 ? tooltip(c) : null);
                            repaint();
                        }
                    }
                });
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hover = -1;
                        hoverBtn = 0;
                        repaint();
                    }

                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) { // bukan mouseClicked: klik dengan sedikit geseran tetap terbaca
                        if (!aktif || !javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                            return;
                        }
                        susun();
                        int c = chipDi(e.getX(), e.getY());
                        if (c >= 0) {
                            if (bisaPilih(c) && c != sel) {
                                pilih(c, true);
                            }
                            return;
                        }
                        switch (tombolDi(e.getX(), e.getY())) {
                            case 1:
                                if (tahun > 2000) {
                                    aksiTahun.accept(tahun - 1);
                                }
                                break;
                            case 2:
                                popupTahun();
                                break;
                            case 3:
                                if (tahun < java.time.LocalDate.now().getYear()) {
                                    aksiTahun.accept(tahun + 1);
                                }
                                break;
                            case 4:
                                aksiSekarang.run();
                                break;
                            default:
                        }
                    }
                });
            }

            // ---------- API ----------
            /** @param pilihan 0 = setahun, 1..12 = bulan. ini/lalu = jumlah operasi per bulan tahun terpilih / tahun sebelumnya. */
            void setData(int tahun, int pilihan, int[] ini, int[] lalu) {
                this.tahun = tahun;
                this.sel = pilihan;
                this.ini = ini == null ? new int[12] : ini;
                this.lalu = lalu == null ? new int[12] : lalu;
                susun();
                aturTarget();
                jalan();
                repaint();
            }

            void setMemuat(boolean v) {
                memuat = v;
                if (v) {
                    if (timerMuat == null) {
                        timerMuat = new javax.swing.Timer(33, e -> repaint());
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

            private boolean bisaPilih(int i) {
                if (i < 0) {
                    return false;
                }
                java.time.LocalDate h = java.time.LocalDate.now();
                return !(i > 0 && tahun == h.getYear() && i > h.getMonthValue());
            }

            // ---------- geometri ----------
            private void susun() {
                int w = getWidth(), h = getHeight(), cy = h / 2;
                rBox = new java.awt.Rectangle(w - 16 - BOX_W, cy - 19, BOX_W, 38);
                rYPrev = new java.awt.Rectangle(rBox.x + 6, cy - 14, 28, 28);
                rYLabel = new java.awt.Rectangle(rBox.x + 36, cy - 14, 90, 28);
                rYNext = new java.awt.Rectangle(rBox.x + 128, cy - 14, 28, 28);
                rNow = new java.awt.Rectangle(rBox.x + 166, cy - 13, 98, 26);
                int x0 = 92, x1 = rBox.x - 16;
                double satuan = (x1 - x0 - 12 * GAP) / 13.5;
                int x = x0;
                for (int i = 0; i < 13; i++) {
                    int cw = (int) Math.round(i == 0 ? satuan * 1.5 : satuan);
                    rChip[i] = new java.awt.Rectangle(x, cy - H_CHIP / 2, cw, H_CHIP);
                    x += cw + GAP;
                }
            }

            private int chipDi(int x, int y) {
                for (int i = 0; i < 13; i++) {
                    if (rChip[i] != null && rChip[i].contains(x, y)) {
                        return i;
                    }
                }
                return -1;
            }

            private int tombolDi(int x, int y) {
                if (rYPrev.contains(x, y)) {
                    return 1;
                }
                if (rYLabel.contains(x, y)) {
                    return 2;
                }
                if (rYNext.contains(x, y)) {
                    return 3;
                }
                return rNow.contains(x, y) ? 4 : 0;
            }

            private void pilih(int i, boolean panggil) {
                sel = i;
                aturTarget();
                jalan();
                if (panggil) {
                    aksiPilih.accept(i);
                }
            }

            private void aturTarget() {
                if (rChip[sel] != null) {
                    pillTX = rChip[sel].x;
                    pillTW = rChip[sel].width;
                }
            }

            private void jalan() {
                if (timer == null) {
                    timer = new javax.swing.Timer(16, e -> {
                        double d1 = pillTX - pillX, d2 = pillTW - pillW;
                        if (Math.abs(d1) > 0.3 || Math.abs(d2) > 0.3) {
                            pillX += d1 * 0.3;
                            pillW += d2 * 0.3;
                        } else {
                            pillX = pillTX;
                            pillW = pillTW;
                            ((javax.swing.Timer) e.getSource()).stop();
                        }
                        repaint();
                    });
                }
                if (!timer.isRunning()) {
                    timer.start();
                }
            }

            private String tooltip(int c) {
                if (!bisaPilih(c)) {
                    return null;
                }
                int a, b;
                String nama;
                if (c == 0) {
                    a = 0;
                    b = 0;
                    for (int i = 0; i < 12; i++) {
                        a += ini[i];
                        b += lalu[i];
                    }
                    nama = "Setahun " + tahun;
                } else {
                    a = ini[c - 1];
                    b = lalu[c - 1];
                    nama = NAMA_BULAN[c - 1] + " " + tahun;
                }
                return "<html><b>" + nama + "</b><br>" + a + " operasi<br>" + (tahun - 1) + " : " + b + " operasi &nbsp;" + deltaPersen(a, b) + "</html>";
            }

            private void popupTahun() {
                javax.swing.JPopupMenu pm = new javax.swing.JPopupMenu();
                int skr = java.time.LocalDate.now().getYear();
                for (int y = skr; y >= Math.max(2000, skr - 15); y--) {
                    final int yy = y;
                    javax.swing.JMenuItem mi = new javax.swing.JMenuItem(String.valueOf(y));
                    mi.setFont(FontCache.get(y == tahun ? java.awt.Font.BOLD : java.awt.Font.PLAIN, 13));
                    mi.addActionListener(e -> {
                        if (aktif && yy != tahun) {
                            aksiTahun.accept(yy);
                        }
                    });
                    pm.add(mi);
                }
                pm.show(this, rYLabel.x, rYLabel.y + rYLabel.height + 2);
            }

            // ---------- lukisan ----------
            private static java.awt.Color a(java.awt.Color c, int al) {
                return new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, al)));
            }

            private void panah(java.awt.Graphics2D g, java.awt.Rectangle r, boolean kiri, boolean bisa, boolean sorot) {
                boolean on = bisa && aktif;
                g.setColor(new java.awt.Color(255, 255, 255, on ? (sorot ? 42 : 18) : 6));
                g.fillOval(r.x, r.y, r.width, r.height);
                g.setStroke(new java.awt.BasicStroke(1f));
                g.setColor(new java.awt.Color(255, 255, 255, on ? 64 : 24));
                g.drawOval(r.x, r.y, r.width - 1, r.height - 1);
                g.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g.setColor(on ? TEKS : a(TEKS_REDUP, 110));
                int mx = r.x + r.width / 2, my = r.y + r.height / 2, s = 4;
                if (kiri) {
                    g.drawLine(mx + 2, my - s, mx - 2, my);
                    g.drawLine(mx - 2, my, mx + 2, my + s);
                } else {
                    g.drawLine(mx - 2, my - s, mx + 2, my);
                    g.drawLine(mx + 2, my, mx - 2, my + s);
                }
                g.setStroke(new java.awt.BasicStroke(1f));
            }

            private static void tengah(java.awt.Graphics2D g, String s, int x, int lebar, int y) {
                g.drawString(s, x + (lebar - g.getFontMetrics().stringWidth(s)) / 2, y);
            }

            @Override
            protected void paintComponent(java.awt.Graphics g0) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), cy = h / 2;
                susun();
                if (snap && w > 0) {
                    aturTarget();
                    pillX = pillTX;
                    pillW = pillTW;
                    snap = false;
                }
                g.setPaint(new java.awt.GradientPaint(0, 0, new java.awt.Color(0x0B, 0x13, 0x2B), w, 0, new java.awt.Color(0x0F, 0x1A, 0x33)));
                g.fillRect(0, 0, w, h);
                g.setColor(new java.awt.Color(255, 255, 255, 22));
                g.drawLine(0, h - 1, w, h - 1);
                g.setFont(FontCache.get(java.awt.Font.BOLD, 10));
                g.setColor(TEKS_REDUP);
                g.drawString("BULAN", 24, cy + 4);

                int maks = 1;
                for (int i = 0; i < 12; i++) {
                    maks = Math.max(maks, Math.max(ini[i], lalu[i]));
                }
                int totIni = 0, totLalu = 0;
                for (int i = 0; i < 12; i++) {
                    totIni += ini[i];
                    totLalu += lalu[i];
                }
                // sorot terpilih (meluncur)
                int px = (int) Math.round(pillX), pw = (int) Math.round(pillW);
                if (aktif || memuat) {
                    g.setPaint(new java.awt.RadialGradientPaint(px + pw / 2f, cy, Math.max(30f, pw * 0.9f), new float[]{0f, 1f},
                            new java.awt.Color[]{a(BIRU2, 90), a(BIRU2, 0)}));
                    g.fillRect(px - pw / 3, 0, pw + 2 * (pw / 3), h);
                }
                g.setPaint(new java.awt.GradientPaint(px, 0, BIRU1, px + pw, 0, BIRU2));
                g.fillRoundRect(px, cy - H_CHIP / 2, pw, H_CHIP, 18, 18);

                for (int i = 0; i < 13; i++) {
                    java.awt.Rectangle r = rChip[i];
                    boolean ok = bisaPilih(i), dipilih = i == sel, hv = i == hover && ok && aktif;
                    if (!dipilih) {
                        g.setColor(new java.awt.Color(255, 255, 255, hv ? 30 : (ok ? 12 : 5)));
                        g.fillRoundRect(r.x, r.y, r.width, r.height, 18, 18);
                        g.setColor(new java.awt.Color(255, 255, 255, ok ? 34 : 14));
                        g.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, 18, 18);
                    }
                    String nm = i == 0 ? "SETAHUN" : BLN[i - 1];
                    int a1 = i == 0 ? totIni : ini[i - 1], b1 = i == 0 ? totLalu : lalu[i - 1];
                    java.awt.Color tk = dipilih ? java.awt.Color.WHITE : (ok ? (hv ? java.awt.Color.WHITE : TEKS) : a(TEKS_REDUP, 120));
                    g.setFont(FontCache.get(java.awt.Font.BOLD, i == 0 ? 11 : 12));
                    g.setColor(dipilih ? java.awt.Color.WHITE : (ok ? new java.awt.Color(0xB6, 0xC8, 0xDC) : a(TEKS_REDUP, 110)));
                    tengah(g, nm, r.x, r.width, r.y + 17);
                    g.setFont(FontCache.get(java.awt.Font.BOLD, 16));
                    g.setColor(tk);
                    tengah(g, ok ? String.valueOf(a1) : "-", r.x, r.width, r.y + 37);
                    if (ok) {
                        // mini-bar: tahun ini (atas, cyan) dan tahun lalu (bawah, emas)
                        int bw = r.width - 22, bx = r.x + 11;
                        int pa = i == 0 ? Math.max(a1, b1) : maks;
                        pa = Math.max(1, pa);
                        g.setColor(new java.awt.Color(255, 255, 255, dipilih ? 50 : 20));
                        g.fillRoundRect(bx, r.y + 44, bw, 5, 5, 5);
                        g.fillRoundRect(bx, r.y + 51, bw, 4, 4, 4);
                        int wa = a1 > 0 ? Math.max(4, (int) Math.round(bw * a1 / (double) pa)) : 0;
                        int wb = b1 > 0 ? Math.max(4, (int) Math.round(bw * b1 / (double) pa)) : 0;
                        if (wa > 0) {
                            g.setPaint(new java.awt.GradientPaint(bx, 0, dipilih ? java.awt.Color.WHITE : BIRU2, bx + wa, 0, dipilih ? new java.awt.Color(0xCF, 0xF5, 0xFF) : BIRU1));
                            g.fillRoundRect(bx, r.y + 44, wa, 5, 5, 5);
                        }
                        if (wb > 0) {
                            g.setColor(dipilih ? new java.awt.Color(0xFF, 0xE9, 0xA8) : a(EMAS, 190));
                            g.fillRoundRect(bx, r.y + 51, wb, 4, 4, 4);
                        }
                    }
                }

                // kotak tahun
                g.setColor(new java.awt.Color(255, 255, 255, aktif ? 14 : 8));
                g.fillRoundRect(rBox.x, rBox.y, rBox.width, rBox.height, rBox.height, rBox.height);
                g.setColor(new java.awt.Color(255, 255, 255, 46));
                g.drawRoundRect(rBox.x, rBox.y, rBox.width, rBox.height, rBox.height, rBox.height);
                panah(g, rYPrev, true, tahun > 2000, hoverBtn == 1);
                panah(g, rYNext, false, tahun < java.time.LocalDate.now().getYear(), hoverBtn == 3);
                if (hoverBtn == 2 && aktif) {
                    g.setColor(new java.awt.Color(255, 255, 255, 22));
                    g.fillRoundRect(rYLabel.x, rYLabel.y, rYLabel.width, rYLabel.height, 14, 14);
                }
                g.setFont(FontCache.get(java.awt.Font.BOLD, 14));
                g.setColor(aktif ? java.awt.Color.WHITE : TEKS_REDUP);
                tengah(g, tahun + " \u25BE", rYLabel.x, rYLabel.width, rYLabel.y + 19);
                g.setColor(new java.awt.Color(255, 255, 255, 36));
                g.drawLine(rBox.x + 160, rBox.y + 8, rBox.x + 160, rBox.y + rBox.height - 8);
                g.setColor(new java.awt.Color(0x6E, 0xE7, 0xF9, hoverBtn == 4 && aktif ? 46 : 22));
                g.fillRoundRect(rNow.x, rNow.y, rNow.width, rNow.height, rNow.height, rNow.height);
                g.setFont(FontCache.get(java.awt.Font.BOLD, 12));
                g.setColor(aktif ? new java.awt.Color(0x6E, 0xE7, 0xF9) : a(new java.awt.Color(0x6E, 0xE7, 0xF9), 110));
                tengah(g, "Bulan ini", rNow.x, rNow.width, rNow.y + 17);

                // keterangan mini-bar
                g.setFont(FontCache.get(java.awt.Font.PLAIN, 10));
                g.setColor(a(BIRU2, 220));
                g.fillRoundRect(24, cy + 18, 10, 4, 4, 4);
                g.setColor(TEKS_REDUP);
                g.drawString(String.valueOf(tahun), 38, cy + 23);
                g.setColor(a(EMAS, 200));
                g.fillRoundRect(24, cy + 29, 10, 4, 4, 4);
                g.setColor(TEKS_REDUP);
                g.drawString(String.valueOf(tahun - 1), 38, cy + 34);

                if (memuat) {
                    long now = System.currentTimeMillis();
                    int sx = (int) ((now / 5) % (w + 220)) - 220;
                    g.setPaint(new java.awt.LinearGradientPaint(sx, 0, sx + 220, 0, new float[]{0f, 0.5f, 1f},
                            new java.awt.Color[]{a(BIRU2, 0), a(BIRU2, 230), a(BIRU2, 0)}));
                    g.fillRect(sx, h - 3, 220, 3);
                }
                g.dispose();
            }
        }

        /** Penanda sementara saat data sedang dimuat. */
        static final class MemuatPanel extends javax.swing.JPanel {

            private final javax.swing.Timer t = new javax.swing.Timer(33, e -> repaint());

            MemuatPanel() {
                setOpaque(true);
            }

            @Override
            public void addNotify() {
                super.addNotify();
                t.start();
            }

            @Override
            public void removeNotify() {
                t.stop();
                super.removeNotify();
            }

            @Override
            protected void paintComponent(java.awt.Graphics g0) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g.setPaint(new java.awt.GradientPaint(0, 0, BG1, 0, h, BG2));
                g.fillRect(0, 0, w, h);
                long now = System.currentTimeMillis();
                int pw = 200, ph = 44, px = (w - pw) / 2, py = Math.max(40, h / 3);
                g.setColor(new java.awt.Color(255, 255, 255, 22));
                g.fillRoundRect(px, py, pw, ph, ph, ph);
                g.setColor(new java.awt.Color(255, 255, 255, 60));
                g.drawRoundRect(px, py, pw, ph, ph, ph);
                g.setStroke(new java.awt.BasicStroke(3f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g.setColor(new java.awt.Color(255, 255, 255, 40));
                g.drawOval(px + 16, py + 11, 22, 22);
                g.setColor(new java.awt.Color(0x6E, 0xE7, 0xF9));
                g.drawArc(px + 16, py + 11, 22, 22, -(int) ((now / 4) % 360), 100);
                g.setFont(FontCache.get(java.awt.Font.BOLD, 13));
                g.setColor(TEKS);
                g.drawString("Memuat data" + "...".substring(0, (int) ((now / 350) % 4)), px + 50, py + 27);
                g.dispose();
            }
        }

        // ------------------------------------------------------------------
        //  Line chart: total operasi per bulan selama setahun, dibandingkan dengan tahun lalu pada tanggal yang sama
        // ------------------------------------------------------------------
        static final class LineChartPanel extends javax.swing.JPanel {

            private static final String[] BLN = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
            private static final java.awt.Color BIRU1 = new java.awt.Color(0x2F, 0x80, 0xED), BIRU2 = new java.awt.Color(0x00, 0xD2, 0xFF),
                    HIJAU2 = new java.awt.Color(0x7B, 0xF0, 0xC8);
            private static final int KIRI = 72, KANAN = 40, DURASI_MS = 1800;

            private int tahun = java.time.LocalDate.now().getYear(), bulanSel = 0, bulanBatas = 12;
            private final double[] ini = new double[12], lalu = new double[12];   // NaN = di luar periode
            private boolean statis;
            private double progress = 1, selPos = -1, axMax = 5;
            private long mulai;
            private int hover = -1, mx, my;
            private int pAtas, pBawah, pKiri, pLebar;
            private javax.swing.Timer timer;
            private final java.util.function.IntConsumer klik;

            LineChartPanel(java.util.function.IntConsumer klik) {
                this.klik = klik;
                setOpaque(true);
                setPreferredSize(new java.awt.Dimension(900, 560));
                java.util.Arrays.fill(ini, Double.NaN);
                java.util.Arrays.fill(lalu, Double.NaN);
                addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(java.awt.event.MouseEvent e) {
                        mx = e.getX();
                        my = e.getY();
                        int b = -1;
                        if (pLebar > 0 && mx >= pKiri && mx < pKiri + pLebar && my >= pAtas - 14 && my <= pBawah + 56) {
                            b = Math.min(11, (int) ((mx - pKiri) / (pLebar / 12.0)));
                            if (b >= bulanBatas) {
                                b = -1;
                            }
                        }
                        if (b != hover) {
                            hover = b;
                            setCursor(java.awt.Cursor.getPredefinedCursor(b >= 0 ? java.awt.Cursor.HAND_CURSOR : java.awt.Cursor.DEFAULT_CURSOR));
                        }
                        jalan();
                        repaint();
                    }
                });
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hover = -1;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (hover >= 0 && LineChartPanel.this.klik != null && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                            LineChartPanel.this.klik.accept(hover + 1);
                        }
                    }
                });
            }

            // ---------- data & animasi ----------
            /** ini / lalu = jumlah operasi per bulan (12 nilai). Bulan setelah hari ini (tahun berjalan) tidak digambar. */
            void setData(int tahun, int[] iniBulan, int[] laluBulan, int pilihan) {
                this.tahun = tahun;
                java.time.LocalDate hari = java.time.LocalDate.now();
                bulanBatas = tahun == hari.getYear() ? hari.getMonthValue() : 12;
                double mk = 0;
                for (int m = 0; m < 12; m++) {
                    boolean ada = m < bulanBatas;
                    ini[m] = ada ? iniBulan[m] : Double.NaN;
                    lalu[m] = ada ? laluBulan[m] : Double.NaN;
                    if (ada) {
                        mk = Math.max(mk, Math.max(iniBulan[m], laluBulan[m]));
                    }
                }
                axMax = Math.max(5, Math.ceil(niceMax(mk) / 5.0) * 5);
                bulanSel = pilihan;
                if (selPos < 0 || pilihan == 0) {
                    selPos = pilihan - 1;
                }
                hover = -1;
                repaint();
            }

            void setBulan(int b) {
                bulanSel = b;
                if (b > 0 && selPos < 0) {
                    selPos = b - 1;
                }
                jalan();
            }

            void mulaiAnimasi() {
                statis = false;
                progress = 0;
                mulai = System.currentTimeMillis();
                jalan();
            }

            void setStatis() {
                statis = true;
                progress = 1;
                hover = -1;
                selPos = bulanSel - 1;
                hentikan();
            }

            void hentikan() {
                if (timer != null) {
                    timer.stop();
                }
            }

            private void jalan() {
                if (timer == null) {
                    timer = new javax.swing.Timer(16, e -> {
                        boolean lagi = false;
                        if (!statis && progress < 1) {
                            progress = Math.min(1, (System.currentTimeMillis() - mulai) / (double) DURASI_MS);
                            lagi = progress < 1;
                        }
                        double target = bulanSel - 1;
                        if (bulanSel > 0 && Math.abs(target - selPos) > 0.005) {
                            selPos += (target - selPos) * 0.25;
                            lagi = true;
                        } else if (bulanSel > 0) {
                            selPos = target;
                        }
                        repaint();
                        if (!lagi) {
                            ((javax.swing.Timer) e.getSource()).stop();
                        }
                    });
                }
                if (!timer.isRunning()) {
                    timer.start();
                }
            }

            private static double niceMax(double max) {
                if (max <= 0) {
                    return 5;
                }
                double kasar = max / 5.0, pangkat = Math.pow(10, Math.floor(Math.log10(kasar))), f = kasar / pangkat, s = 10;
                for (double p : new double[]{1, 1.5, 2, 2.5, 3, 4, 5, 6, 8, 10}) {
                    if (f <= p + 1e-9) {
                        s = p;
                        break;
                    }
                }
                return s * pangkat * 5;
            }

            // ---------- koordinat ----------
            private double xm(int m) {
                return pKiri + (m + 0.5) * (pLebar / 12.0);
            }

            private double ym(double v) {
                return pBawah - (pBawah - pAtas) * Math.min(1.04, Math.max(0, v) / axMax);
            }

            private java.awt.geom.Path2D.Double jalur(double[] v, int n) {
                java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
                p.moveTo(xm(0), ym(v[0]));
                for (int i = 0; i < n - 1; i++) {
                    double x0 = xm(i), x1 = xm(i + 1), y0 = ym(v[i]), y1 = ym(v[i + 1]);
                    double xa = i > 0 ? xm(i - 1) : x0, ya = i > 0 ? ym(v[i - 1]) : y0;
                    double xb = i + 2 < n ? xm(i + 2) : x1, yb = i + 2 < n ? ym(v[i + 2]) : y1;
                    double lo = Math.min(y0, y1), hi = Math.max(y0, y1);
                    double c1y = Math.max(lo, Math.min(hi, y0 + (y1 - ya) / 6.0));
                    double c2y = Math.max(lo, Math.min(hi, y1 - (yb - y0) / 6.0));
                    p.curveTo(x0 + (x1 - xa) / 6.0, c1y, x1 - (xb - x0) / 6.0, c2y, x1, y1);
                }
                return p;
            }

            private static java.awt.Color a(java.awt.Color c, int al) {
                return new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, al)));
            }

            private static void tengah(java.awt.Graphics2D g, String s, double cx, int y) {
                g.drawString(s, (int) Math.round(cx - g.getFontMetrics().stringWidth(s) / 2.0), y);
            }

            private double rev() {
                if (statis) {
                    return 1.0;
                }
                double t = Math.max(0, Math.min(1, progress * 1.15));
                return 1 - Math.pow(1 - t, 3);
            }

            // ---------- lukisan ----------
            @Override
            protected void paintComponent(java.awt.Graphics g0) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g.setPaint(new java.awt.GradientPaint(0, 0, BG1, 0, h, BG2));
                g.fillRect(0, 0, w, h);
                pKiri = KIRI;
                pLebar = w - KIRI - KANAN;
                pAtas = 160;
                pBawah = h - 104;
                if (pLebar < 260 || pBawah - pAtas < 120) {
                    g.dispose();
                    return;
                }
                int n = bulanBatas;
                double totIni = 0, totLalu = 0;
                int puncak = 0;
                for (int m = 0; m < n; m++) {
                    totIni += ini[m];
                    totLalu += lalu[m];
                    if (ini[m] > ini[puncak]) {
                        puncak = m;
                    }
                }
                kartu(g, w, (int) totIni, (int) totLalu, puncak, n);
                legenda(g);

                // grid + sumbu
                int ph = pBawah - pAtas;
                double band = pLebar / 12.0;
                g.setFont(FontCache.get(java.awt.Font.PLAIN, 10));
                java.awt.FontMetrics fm = g.getFontMetrics();
                for (int i = 0; i <= 5; i++) {
                    int y = pBawah - (int) Math.round(ph * i / 5.0);
                    g.setColor(new java.awt.Color(255, 255, 255, i == 0 ? 60 : 17));
                    g.drawLine(pKiri, y, pKiri + pLebar, y);
                    g.setColor(TEKS_REDUP);
                    String s = String.valueOf(Math.round(axMax * i / 5.0));
                    g.drawString(s, pKiri - 10 - fm.stringWidth(s), y + 4);
                }
                // bulan terpilih (meluncur)
                if (bulanSel > 0 && selPos >= 0) {
                    double sxb = pKiri + selPos * band;
                    g.setPaint(new java.awt.GradientPaint(0, pAtas - 10, a(BIRU1, 62), 0, pBawah + 56, a(BIRU1, 8)));
                    g.fillRoundRect((int) sxb + 3, pAtas - 10, (int) band - 6, ph + 76, 14, 14);
                    g.setColor(a(BIRU2, 230));
                    g.fillRoundRect((int) (sxb + band / 2 - 18), pAtas - 10, 36, 3, 3, 3);
                }
                for (int m = 0; m < 12; m++) {
                    boolean ada = m < n, sel = m == bulanSel - 1, hv = m == hover;
                    g.setFont(FontCache.get(sel || hv ? java.awt.Font.BOLD : java.awt.Font.PLAIN, 12));
                    g.setColor(!ada ? a(TEKS_REDUP, 110) : (sel ? EMAS : (hv ? java.awt.Color.WHITE : new java.awt.Color(170, 185, 215))));
                    tengah(g, BLN[m], xm(m), pBawah + 20);
                }
                g.setFont(FontCache.get(java.awt.Font.BOLD, 10));
                g.setColor(TEKS_REDUP);
                g.drawString("OPERASI", 14, pAtas - 18);

                // baris selisih per bulan
                g.setFont(FontCache.get(java.awt.Font.BOLD, 10));
                g.setColor(a(TEKS_REDUP, 200));
                g.drawString("vs " + (tahun - 1), 14, pBawah + 45);
                for (int m = 0; m < n; m++) {
                    double e = Math.max(0, Math.min(1, rev() * 1.4 - m / 12.0 * 0.4));
                    if (e <= 0.02) {
                        continue;
                    }
                    java.awt.Color wd = warnaDelta((int) ini[m], (int) lalu[m]);
                    g.setColor(a(wd, (int) (255 * e)));
                    tengah(g, deltaPersen((int) ini[m], (int) lalu[m]), xm(m), pBawah + 45);
                }

                // rata-rata tahun ini
                double rata = n > 0 ? totIni / n : 0;
                if (rata > 0) {
                    int y = (int) ym(rata);
                    g.setStroke(new java.awt.BasicStroke(1f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 10f, new float[]{2f, 5f}, 0f));
                    g.setColor(a(java.awt.Color.WHITE, (int) (80 * rev())));
                    g.drawLine(pKiri, y, pKiri + pLebar, y);
                    g.setStroke(new java.awt.BasicStroke(1f));
                    g.setFont(FontCache.get(java.awt.Font.PLAIN, 10));
                    g.setColor(a(TEKS_REDUP, (int) (255 * rev())));
                    String t = "rata-rata " + String.format(java.util.Locale.US, "%.1f", rata);
                    g.drawString(t, pKiri + pLebar - g.getFontMetrics().stringWidth(t) - 4, y - 5);
                }

                java.awt.Shape klipAwal = g.getClip();
                double r = rev();
                // area gradasi tahun ini
                g.clip(new java.awt.geom.Rectangle2D.Double(pKiri - 14, 0, (pLebar + 28) * r, h));
                if (n >= 2) {
                    java.awt.geom.Path2D.Double area = new java.awt.geom.Path2D.Double(jalur(ini, n));
                    area.lineTo(xm(n - 1), pBawah);
                    area.lineTo(xm(0), pBawah);
                    area.closePath();
                    g.setPaint(new java.awt.GradientPaint(0, (float) ym(Math.max(1, ini[puncak])), a(BIRU1, 120), 0, pBawah, a(BIRU2, 0)));
                    g.fill(area);
                }
                // tahun lalu (putus-putus)
                if (n >= 2) {
                    g.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND, 10f, new float[]{7f, 6f}, 0f));
                    g.setColor(a(EMAS, 215));
                    g.draw(jalur(lalu, n));
                }
                g.setStroke(new java.awt.BasicStroke(1.6f));
                for (int m = 0; m < n; m++) {
                    double x = xm(m), y = ym(lalu[m]);
                    g.setColor(BG1);
                    g.fill(new java.awt.geom.Ellipse2D.Double(x - 3.5, y - 3.5, 7, 7));
                    g.setColor(a(EMAS, 225));
                    g.draw(new java.awt.geom.Ellipse2D.Double(x - 3.5, y - 3.5, 7, 7));
                }
                // tahun ini: garis bercahaya
                java.awt.Paint cat = new java.awt.LinearGradientPaint(pKiri, 0, pKiri + pLebar, 0, new float[]{0f, 0.55f, 1f},
                        new java.awt.Color[]{BIRU1, BIRU2, HIJAU2});
                if (n >= 2) {
                    java.awt.geom.Path2D.Double p = jalur(ini, n);
                    g.setPaint(cat);
                    g.setStroke(new java.awt.BasicStroke(9f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    g.setColor(a(BIRU1, 26));
                    g.draw(p);
                    g.setStroke(new java.awt.BasicStroke(5.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    g.setColor(a(BIRU2, 46));
                    g.draw(p);
                    g.setStroke(new java.awt.BasicStroke(3f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    g.setPaint(cat);
                    g.draw(p);
                }
                g.setClip(klipAwal);

                // titik + angka
                for (int m = 0; m < n; m++) {
                    double x = xm(m), y = ym(ini[m]);
                    double pop = statis ? 1.0 : Math.max(0, Math.min(1, (r * (pLebar + 28) - (x - pKiri + 14)) / 46.0));
                    if (pop <= 0.02) {
                        continue;
                    }
                    boolean sel = m == bulanSel - 1, hv = m == hover;
                    double rr = (sel ? 6.5 : (hv ? 6 : 4.4)) * pop;
                    if (sel || hv) {
                        g.setPaint(new java.awt.RadialGradientPaint((float) x, (float) y, 22f, new float[]{0f, 1f}, new java.awt.Color[]{a(BIRU2, 120), a(BIRU2, 0)}));
                        g.fill(new java.awt.geom.Ellipse2D.Double(x - 22, y - 22, 44, 44));
                    }
                    g.setColor(sel ? java.awt.Color.WHITE : BG1);
                    g.fill(new java.awt.geom.Ellipse2D.Double(x - rr, y - rr, 2 * rr, 2 * rr));
                    g.setStroke(new java.awt.BasicStroke(2.2f));
                    g.setColor(sel ? BIRU2 : BIRU1);
                    g.draw(new java.awt.geom.Ellipse2D.Double(x - rr, y - rr, 2 * rr, 2 * rr));
                    g.setStroke(new java.awt.BasicStroke(1f));
                    if (pop > 0.9 && m != puncak) {
                        g.setFont(FontCache.get(java.awt.Font.BOLD, 11));
                        g.setColor(a(java.awt.Color.WHITE, 215));
                        tengah(g, String.valueOf((int) ini[m]), x, (int) y - 12);
                    }
                }
                if ((statis || progress > 0.9) && n > 0 && ini[puncak] > 0) {
                    lencana(g, "\u25B2 Tertinggi  " + (int) ini[puncak], xm(puncak), ym(ini[puncak]) - 16);
                }
                if (hover >= 0 && (statis || progress > 0.85)) {
                    g.setStroke(new java.awt.BasicStroke(1f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 10f, new float[]{3f, 4f}, 0f));
                    g.setColor(a(java.awt.Color.WHITE, 90));
                    g.drawLine((int) xm(hover), pAtas - 6, (int) xm(hover), pBawah);
                    g.setStroke(new java.awt.BasicStroke(1f));
                    tooltip(g, w, h);
                }
                g.dispose();
            }

            private void kartu(java.awt.Graphics2D g, int w, int totIni, int totLalu, int puncak, int n) {
                int cw = (w - 28 * 2 - 3 * 12) / 4, x = 28, y = 20;
                int selisih = totIni - totLalu;
                String[] lb = {"TOTAL " + tahun + (n < 12 ? "  (JAN\u2013" + BLN[n - 1].toUpperCase(java.util.Locale.ROOT) + ")" : ""),
                    "VS " + (tahun - 1) + "  (" + totLalu + ")", "BULAN TERTINGGI", "RATA-RATA / BULAN"};
                String[] vl = {String.valueOf(totIni), deltaPersen(totIni, totLalu) + "  \u2022  " + (selisih >= 0 ? "+" : "") + selisih,
                    n > 0 ? NAMA_BULAN[puncak] + "  \u2022  " + (int) ini[puncak] : "-",
                    n > 0 ? String.format(java.util.Locale.US, "%.1f", totIni / (double) n) + "  \u2022  vs " + String.format(java.util.Locale.US, "%.1f", totLalu / (double) n) : "-"};
                for (int i = 0; i < 4; i++) {
                    g.setColor(new java.awt.Color(255, 255, 255, 26));
                    g.fillRoundRect(x, y, cw, 58, 16, 16);
                    g.setColor(new java.awt.Color(255, 255, 255, 60));
                    g.drawRoundRect(x, y, cw, 58, 16, 16);
                    g.setFont(FontCache.get(java.awt.Font.BOLD, 10));
                    g.setColor(new java.awt.Color(0x9F, 0xB8, 0xCC));
                    g.drawString(lb[i], x + 14, y + 20);
                    g.setFont(FontCache.get(java.awt.Font.BOLD, i == 0 ? 22 : 17));
                    g.setColor(i == 1 ? warnaDelta(totIni, totLalu) : (i == 2 ? EMAS : java.awt.Color.WHITE));
                    java.awt.FontMetrics fm = g.getFontMetrics();
                    String v = vl[i];
                    while (fm.stringWidth(v) > cw - 28 && v.length() > 4) {
                        v = v.substring(0, v.length() - 2);
                    }
                    g.drawString(v.equals(vl[i]) ? v : v.trim() + "\u2026", x + 14, y + 46);
                    x += cw + 12;
                }
            }

            private void legenda(java.awt.Graphics2D g) {
                g.setFont(FontCache.get(java.awt.Font.PLAIN, 12));
                java.awt.FontMetrics fm = g.getFontMetrics();
                int lx = 28, ly = 112;
                g.setPaint(new java.awt.GradientPaint(lx, 0, BIRU1, lx + 26, 0, BIRU2));
                g.fillRoundRect(lx, ly - 8, 26, 4, 4, 4);
                g.setColor(TEKS);
                g.drawString("Tahun " + tahun, lx + 34, ly);
                lx += 34 + fm.stringWidth("Tahun " + tahun) + 26;
                g.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND, 10f, new float[]{5f, 4f}, 0f));
                g.setColor(a(EMAS, 215));
                g.drawLine(lx, ly - 6, lx + 26, ly - 6);
                g.setStroke(new java.awt.BasicStroke(1f));
                g.setColor(TEKS);
                g.drawString("Tahun " + (tahun - 1) + " (periode yang sama)", lx + 34, ly);
                g.setFont(FontCache.get(java.awt.Font.PLAIN, 11));
                g.setColor(TEKS_REDUP);
                String ket = "klik titik / bulan untuk memilih bulan";
                g.drawString(ket, getWidth() - 28 - g.getFontMetrics().stringWidth(ket), ly);
            }

            private void lencana(java.awt.Graphics2D g, String s, double cx, double cy) {
                g.setFont(FontCache.get(java.awt.Font.BOLD, 11));
                int tw = g.getFontMetrics().stringWidth(s) + 18, th = 22;
                int x = (int) Math.max(pKiri, Math.min(pKiri + pLebar - tw, cx - tw / 2.0));
                int y = (int) Math.max(pAtas - 24, cy - th);
                g.setColor(a(BG1, 230));
                g.fillRoundRect(x, y, tw, th, th, th);
                g.setColor(a(EMAS, 170));
                g.setStroke(new java.awt.BasicStroke(1f));
                g.drawRoundRect(x, y, tw, th, th, th);
                g.setColor(EMAS);
                g.drawString(s, x + 9, y + 15);
            }

            private void tooltip(java.awt.Graphics2D g, int w, int h) {
                int m = hover;
                int a1 = (int) ini[m], b1 = (int) lalu[m], kumA = 0, kumB = 0;
                for (int i = 0; i <= m; i++) {
                    kumA += (int) ini[i];
                    kumB += (int) lalu[i];
                }
                String judul = NAMA_BULAN[m] + " " + tahun;
                String[] k = {"Tahun " + tahun, "Tahun " + (tahun - 1), "Selisih", "Kumulatif"};
                String[] v = {a1 + " operasi", b1 + " operasi", deltaPersen(a1, b1) + (b1 > 0 || a1 > 0 ? "  (" + (a1 >= b1 ? "+" : "") + (a1 - b1) + ")" : ""),
                    kumA + " vs " + kumB};
                java.awt.Color[] wv = {java.awt.Color.WHITE, java.awt.Color.WHITE, warnaDelta(a1, b1), warnaDelta(kumA, kumB)};
                java.awt.Color[] wk = {BIRU2, EMAS, TEKS_REDUP, TEKS_REDUP};
                java.awt.Font fN = FontCache.get(java.awt.Font.PLAIN, 12), fB = FontCache.get(java.awt.Font.BOLD, 12);
                g.setFont(fN);
                java.awt.FontMetrics fn = g.getFontMetrics();
                g.setFont(fB);
                java.awt.FontMetrics fb = g.getFontMetrics();
                int kw = 0, vw = 0;
                for (int i = 0; i < 4; i++) {
                    kw = Math.max(kw, fn.stringWidth(k[i]) + 14);
                    vw = Math.max(vw, fb.stringWidth(v[i]));
                }
                int bw = Math.max(16 + kw + 18 + vw + 14, 16 + fb.stringWidth(judul) + 16), bh = 38 + 4 * 19 + 6;
                int tx = mx + 18, ty = my + 12;
                if (tx + bw > w - 8) {
                    tx = mx - 18 - bw;
                }
                if (ty + bh > h - 8) {
                    ty = h - 8 - bh;
                }
                g.setColor(new java.awt.Color(8, 14, 32, 242));
                g.fillRoundRect(tx, ty, bw, bh, 14, 14);
                g.setColor(a(BIRU2, 130));
                g.setStroke(new java.awt.BasicStroke(1f));
                g.drawRoundRect(tx, ty, bw, bh, 14, 14);
                g.setFont(fB);
                g.setColor(EMAS);
                g.drawString(judul, tx + 14, ty + 23);
                int y = ty + 42;
                for (int i = 0; i < 4; i++) {
                    g.setColor(wk[i]);
                    g.fillOval(tx + 14, y - 5, 8, 8);
                    g.setFont(fN);
                    g.setColor(TEKS_REDUP);
                    g.drawString(k[i], tx + 28, y + 4);
                    g.setFont(fB);
                    g.setColor(wv[i]);
                    g.drawString(v[i], tx + bw - 14 - fb.stringWidth(v[i]), y + 4);
                    y += 19;
                }
            }
        }

        // ------------------------------------------------------------------
        /** Pengambil data mode bulanan; dipanggil di thread latar belakang (bukan thread tampilan). */
        interface Pemuat {

            /** Jumlah operasi per bulan: [0] = tahun terpilih, [1] = tahun sebelumnya (periode yang sama), masing-masing 12 nilai. */
            int[][] muatBulanan(int tahun) throws Exception;

            /** operator -> (jenis bayar -> jumlah operasi) pada rentang tanggal dari..sampai (inklusif). */
            java.util.Map<String, java.util.Map<String, Integer>> muatRincian(java.time.LocalDate dari, java.time.LocalDate sampai) throws Exception;

            /** Rincian periode ini dan pembandingnya sekaligus: [0] = dari..sampai, [1] = dariLalu..sampaiLalu. Bisa dioverride menjadi satu kueri. */
            default java.util.List<java.util.Map<String, java.util.Map<String, Integer>>> muatRincianPasangan(java.time.LocalDate dari, java.time.LocalDate sampai,
                    java.time.LocalDate dariLalu, java.time.LocalDate sampaiLalu) throws Exception {
                return java.util.Arrays.asList(muatRincian(dari, sampai), muatRincian(dariLalu, sampaiLalu));
            }
        }

        private BarChartPanel chart;
        private Hasil hasil;
        private String periode;
        private final Pemuat pemuat;
        private final String infoFilter;
        private final javax.swing.JPanel areaHeader = new javax.swing.JPanel(new java.awt.BorderLayout());
        private final javax.swing.JScrollPane sp;
        private final PemilihBulan pemilih;
        private final java.util.Map<String, Integer> indeksWarna = new java.util.HashMap<>();
        private int tahun, bulan, tahunBulanan = -1; // bulan: 0 = setahun, 1..12
        private volatile int urutan = 0;                  // nomor permintaan terakhir (dibaca juga dari thread DB)
        private int[][] bulanan = {new int[12], new int[12]};
        private boolean sedangMemuat = false;
        private int tahunTerapan = -1, bulanTerapan = -1;          // periode yang sedang benar-benar tampil
        private final MemuatPanel memuatPanel = new MemuatPanel(); // satu instance dipakai ulang
        /** Semua akses DB lewat satu thread: koneksi bersama tidak dipakai bersamaan, dan permintaan usang dilewati. */
        private final java.util.concurrent.ExecutorService dbExec = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
            Thread th = new Thread(r, "grafik-operasi-db");
            th.setDaemon(true);
            return th;
        });
        private static final long TTL_CACHE = 120_000L;
        private static final int MAKS_CACHE = 6;
        private final java.util.Map<String, Object[]> cacheRincian = new java.util.LinkedHashMap<String, Object[]>(8, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<String, Object[]> e) {
                return size() > MAKS_CACHE;
            }
        };
        private final java.util.Map<Integer, Object[]> cacheBulanan = new java.util.LinkedHashMap<Integer, Object[]>(4, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<Integer, Object[]> e) {
                return size() > 3;
            }
        };
        private final LineChartPanel garisPanel = new LineChartPanel(this::pilihBulan);
        private boolean tampilGaris = false;       // false = batang per operator, true = line chart setahun
        private final javax.swing.JButton btnGaris = new javax.swing.JButton("Grafik Line Chart");

        /** Mode statis: data sudah jadi (grafik anestesi / kategori). */
        GrafikOperatorDialog(java.awt.Window owner, Hasil hasil, String periode) {
            this(owner, hasil, periode, null, 0, 0, null);
        }

        /**
         * Mode bulanan: bulan dan tahun dipilih lewat chip di atas grafik, otomatis dibandingkan dengan periode yang sama
         * tahun lalu. bulanAwal 0 = setahun; infoFilter (boleh null) hanya keterangan tambahan pada judul periode.
         */
        GrafikOperatorDialog(java.awt.Window owner, Pemuat pemuat, int tahunAwal, int bulanAwal, String infoFilter) {
            this(owner, susun(new java.util.HashMap<>()), "", pemuat, tahunAwal, bulanAwal, infoFilter);
        }

        private GrafikOperatorDialog(java.awt.Window owner, Hasil hasil, String periode, Pemuat pemuat, int tahun, int bulan, String infoFilter) {
            super(owner, "Grafik " + hasil.judul, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
            this.hasil = hasil;
            this.periode = periode;
            this.pemuat = pemuat;
            this.infoFilter = infoFilter;
            java.time.LocalDate hari = java.time.LocalDate.now();
            this.tahun = Math.max(2000, Math.min(tahun == 0 ? hari.getYear() : tahun, hari.getYear()));
            int bln = Math.max(0, Math.min(bulan, 12));
            if (this.tahun == hari.getYear() && bln > hari.getMonthValue()) {
                bln = hari.getMonthValue();
            }
            this.bulan = bln;
            this.chart = new BarChartPanel(hasil);

            setLayout(new java.awt.BorderLayout());
            areaHeader.setOpaque(false);
            areaHeader.add(new HeaderPanel(periode, hasil), java.awt.BorderLayout.CENTER);
            javax.swing.JPanel utara = new javax.swing.JPanel(new java.awt.BorderLayout());
            utara.add(areaHeader, java.awt.BorderLayout.CENTER);
            if (pemuat != null) {
                pemilih = new PemilihBulan(this::pilihBulan, this::pilihTahun, this::keBulanIni);
                utara.add(pemilih, java.awt.BorderLayout.SOUTH);
            } else {
                pemilih = null;
            }
            add(utara, java.awt.BorderLayout.NORTH);

            sp = new javax.swing.JScrollPane(pemuat != null ? memuatPanel : chart);
            sp.setBorder(null);
            sp.getVerticalScrollBar().setUnitIncrement(28);
            add(sp, java.awt.BorderLayout.CENTER);

            javax.swing.JPanel footer = new javax.swing.JPanel(new java.awt.BorderLayout());
            footer.setBackground(new java.awt.Color(0x0F, 0x1A, 0x33));
            footer.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
            String petunjuk = pemuat != null
                    ? "\u2190 \u2192 bulan  \u2022  PgUp / PgDn tahun  \u2022  L: line chart  \u2022  kursor ke batang: rincian  \u2022  Esc: tutup"
                    : (hasil.sederhana ? "Arahkan kursor ke batang untuk melihat detail" : "Arahkan kursor ke batang untuk melihat rincian " + hasil.labelStack) + "  \u2022  Esc untuk menutup";
            javax.swing.JLabel hint = new javax.swing.JLabel(petunjuk);
            hint.setForeground(TEKS_REDUP);
            hint.setFont(FontCache.get(java.awt.Font.PLAIN, 12));
            footer.add(hint, java.awt.BorderLayout.WEST);

            javax.swing.JPanel tombol = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
            tombol.setOpaque(false);
            gayaTombol(btnGaris);
            btnGaris.addActionListener(e -> alihTampilan());
            javax.swing.JButton btnReplay = gayaTombol(new javax.swing.JButton("Ulangi Animasi"));
            javax.swing.JButton btnPng = gayaTombol(new javax.swing.JButton("Simpan Gambar (PNG)"));
            javax.swing.JButton btnTutup = gayaTombol(new javax.swing.JButton("Tutup"));
            btnReplay.addActionListener(e -> {
                if (tampilGaris) {
                    garisPanel.mulaiAnimasi();
                } else {
                    this.chart.mulaiAnimasi();
                }
            });
            btnPng.addActionListener(e -> simpanPng());
            btnTutup.addActionListener(e -> dispose());
            if (pemuat != null) {
                tombol.add(btnGaris);
            }
            tombol.add(btnReplay);
            tombol.add(btnPng);
            tombol.add(btnTutup);
            footer.add(tombol, java.awt.BorderLayout.EAST);
            add(footer, java.awt.BorderLayout.SOUTH);

            javax.swing.JRootPane rp = getRootPane();
            rp.registerKeyboardAction(e -> dispose(),
                    javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
            if (pemuat != null) {
                rp.registerKeyboardAction(e -> geserBulan(-1), javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
                rp.registerKeyboardAction(e -> geserBulan(1), javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
                rp.registerKeyboardAction(e -> pilihTahun(this.tahun - 1), javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_UP, 0), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
                rp.registerKeyboardAction(e -> pilihTahun(this.tahun + 1), javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_DOWN, 0), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
                rp.registerKeyboardAction(e -> pilihBulan(0), javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_HOME, 0), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
                rp.registerKeyboardAction(e -> alihTampilan(), javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, 0), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
            }
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowOpened(java.awt.event.WindowEvent e) {
                    if (GrafikOperatorDialog.this.pemuat != null) {
                        muat(true);
                    } else {
                        GrafikOperatorDialog.this.chart.mulaiAnimasi();
                    }
                }

                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    urutan++;
                    dbExec.shutdown(); // tanpa interrupt: kueri yang berjalan selesai wajar, koneksi bersama tetap aman
                    GrafikOperatorDialog.this.chart.hentikan();
                    garisPanel.hentikan();
                    if (pemilih != null) {
                        pemilih.hentikan();
                    }
                    javax.swing.ToolTipManager.sharedInstance().unregisterComponent(GrafikOperatorDialog.this.chart);
                    javax.swing.ToolTipManager.sharedInstance().unregisterComponent(garisPanel);
                    if (pemilih != null) {
                        javax.swing.ToolTipManager.sharedInstance().unregisterComponent(pemilih);
                    }
                    cacheRincian.clear();
                    cacheBulanan.clear();
                    sp.setViewportView(null); // lepas panel besar agar bisa di-GC
                }
            });

            java.awt.Dimension layar = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            if (pemuat != null) {
                setSize(Math.min(1200, layar.width - 40), Math.max(620, Math.min(820, layar.height - 90)));
            } else {
                int tinggi = 150 + chart.getPreferredSize().height + 56 + 34;
                tinggi = Math.max(520, Math.min(tinggi, layar.height - 90));
                setSize(Math.min(1080, layar.width - 40), tinggi);
            }
            setLocationRelativeTo(owner);
        }

        // ---------- mode bulanan ----------
        /** Beralih antara batang per operator dan line chart setahun. */
        private void alihTampilan() {
            if (pemuat == null || sedangMemuat) {
                return;
            }
            tampilGaris = !tampilGaris;
            btnGaris.setText(tampilGaris ? "Grafik Batang" : "Grafik Line Chart");
            if (tampilGaris) {
                chart.hentikan();
                garisPanel.setData(tahun, bulanan[0], bulanan[1], bulan);
                sp.setViewportView(garisPanel);
                garisPanel.mulaiAnimasi();
            } else {
                garisPanel.hentikan();
                sp.setViewportView(chart);
                sp.getVerticalScrollBar().setValue(0);
                chart.mulaiAnimasi();
            }
        }

        private void pilihBulan(int b) {
            if (pemuat == null || b == bulan) {
                return;
            }
            java.time.LocalDate hari = java.time.LocalDate.now();
            if (tahun == hari.getYear() && b > hari.getMonthValue()) {
                return;
            }
            bulan = b;
            if (tampilGaris) {
                garisPanel.setBulan(b); // sorotan langsung bergeser; rincian untuk header dimuat di latar belakang
            }
            muat(false);
        }

        private void pilihTahun(int t) {
            java.time.LocalDate hari = java.time.LocalDate.now();
            if (pemuat == null || t < 2000 || t > hari.getYear() || t == tahun) {
                return;
            }
            tahun = t;
            if (tahun == hari.getYear() && bulan > hari.getMonthValue()) {
                bulan = hari.getMonthValue();
            }
            muat(true);
        }

        private void keBulanIni() {
            java.time.LocalDate hari = java.time.LocalDate.now();
            if (pemuat == null || (tahun == hari.getYear() && bulan == hari.getMonthValue())) {
                return;
            }
            tahun = hari.getYear();
            bulan = hari.getMonthValue();
            muat(true);
        }

        private void geserBulan(int d) {
            java.time.LocalDate hari = java.time.LocalDate.now();
            int maks = tahun == hari.getYear() ? hari.getMonthValue() : 12;
            pilihBulan(Math.max(0, Math.min(maks, bulan + d)));
        }

        private String periodeTeks(int t, int b) {
            java.time.LocalDate[] r = rentang(t, b);
            java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String s = (b == 0 ? "Setahun " + t : NAMA_BULAN[b - 1] + " " + t) + "  (" + r[0].format(df) + " s.d. " + r[1].format(df) + ")";
            return infoFilter == null || infoFilter.isEmpty() ? s : s + "   \u2022   " + infoFilter;
        }

        private Object[] dariCache(Object[] e) {
            return e != null && System.currentTimeMillis() - (Long) e[0] < TTL_CACHE ? e : null;
        }

        /**
         * Muat data untuk tahun/bulan terpilih. Permintaan terbaru selalu menang: klik saat masih memuat tidak diabaikan,
         * permintaan lama yang belum jalan dilewati. Data yang baru saja dimuat diambil dari cache kecil (instan).
         */
        @SuppressWarnings("unchecked")
        private void muat(boolean tahunBerubah) {
            final int seq = ++urutan;
            final int t = tahun, b = bulan;
            final String kunci = t + "-" + b;
            final boolean perluBulanan = tahunBulanan != t;
            final Object[] cBl = perluBulanan ? dariCache(cacheBulanan.get(t)) : null;
            final Object[] cRc = dariCache(cacheRincian.get(kunci));
            if ((!perluBulanan || cBl != null) && cRc != null) {
                sedangMemuat = false;
                terapkan(t, b, perluBulanan ? (int[][]) cBl[1] : null, (java.util.Map<String, java.util.Map<String, Integer>>) cRc[1],
                        (java.util.Map<String, java.util.Map<String, Integer>>) cRc[2]);
                return;
            }
            sedangMemuat = true;
            pemilih.setData(t, b, bulanan[0], bulanan[1]);
            pemilih.setMemuat(true);
            chart.hentikan();
            btnGaris.setEnabled(false);
            if (!tampilGaris || perluBulanan) {
                garisPanel.hentikan();
                sp.setViewportView(memuatPanel);
            }
            final int[][] blSiap = cBl != null ? (int[][]) cBl[1] : null;
            final Object[] rcSiap = cRc;
            try {
                dbExec.execute(() -> {
                    if (seq != urutan) {
                        return; // sudah ada permintaan yang lebih baru / dialog ditutup
                    }
                    Object[] h = null;
                    Throwable err = null;
                    try {
                        int[][] bl = blSiap;
                        if (perluBulanan && bl == null) {
                            bl = pemuat.muatBulanan(t);
                        }
                        java.util.Map<String, java.util.Map<String, Integer>> ini, lalu;
                        if (rcSiap != null) {
                            ini = (java.util.Map<String, java.util.Map<String, Integer>>) rcSiap[1];
                            lalu = (java.util.Map<String, java.util.Map<String, Integer>>) rcSiap[2];
                        } else if (seq != urutan) {
                            return;
                        } else {
                            java.time.LocalDate[] r = rentang(t, b);
                            java.util.List<java.util.Map<String, java.util.Map<String, Integer>>> p = pemuat.muatRincianPasangan(r[0], r[1], r[2], r[3]);
                            ini = p.get(0);
                            lalu = p.get(1);
                        }
                        h = new Object[]{bl, ini, lalu};
                    } catch (Throwable ex) { // termasuk Error, supaya tampilan tidak menggantung di "Memuat data"
                        err = ex;
                    }
                    final Object[] hf = h;
                    final Throwable ef = err;
                    javax.swing.SwingUtilities.invokeLater(() -> selesaiMuat(seq, t, b, hf, ef));
                });
            } catch (java.util.concurrent.RejectedExecutionException ex) {
                sedangMemuat = false; // dialog sudah ditutup
            }
        }

        @SuppressWarnings("unchecked")
        private void selesaiMuat(int seq, int t, int b, Object[] h, Throwable err) {
            if (seq != urutan) {
                return; // permintaan yang lebih baru yang menangani tampilan
            }
            try {
                if (err != null) {
                    throw err;
                }
                long now = System.currentTimeMillis();
                if (h[0] != null) {
                    cacheBulanan.put(t, new Object[]{now, h[0]});
                }
                cacheRincian.put(t + "-" + b, new Object[]{now, h[1], h[2]});
                terapkan(t, b, (int[][]) h[0], (java.util.Map<String, java.util.Map<String, Integer>>) h[1],
                        (java.util.Map<String, java.util.Map<String, Integer>>) h[2]);
            } catch (Throwable ex) {
                sedangMemuat = false;
                pemilih.setMemuat(false);
                btnGaris.setEnabled(true);
                if (tahunTerapan > 0) { // kembalikan pilihan ke periode yang masih tampil, supaya bisa dicoba lagi
                    tahun = tahunTerapan;
                    bulan = bulanTerapan;
                    pemilih.setData(tahun, bulan, bulanan[0], bulanan[1]);
                }
                sp.setViewportView(tampilGaris ? garisPanel : chart);
                javax.swing.JOptionPane.showMessageDialog(GrafikOperatorDialog.this, "Gagal memuat data grafik:\n"
                        + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }

        private void terapkan(int t, int b, int[][] bl, java.util.Map<String, java.util.Map<String, Integer>> ini,
                java.util.Map<String, java.util.Map<String, Integer>> lalu) {
            if (bl != null) {
                bulanan = bl;
                tahunBulanan = t;
            }
            tahunTerapan = t;
            bulanTerapan = b;
            String pembanding = b == 0 ? "Tahun " + (t - 1) : NAMA_BULAN[b - 1] + " " + (t - 1);
            hasil = susun(ini, lalu, BAYAR_KOSONG, indeksWarna, pembanding);
            periode = periodeTeks(t, b);
            areaHeader.removeAll();
            areaHeader.add(new HeaderPanel(periode, hasil), java.awt.BorderLayout.CENTER);
            areaHeader.revalidate();
            areaHeader.repaint();
            BarChartPanel lama = chart;
            chart = new BarChartPanel(hasil);
            lama.hentikan();
            javax.swing.ToolTipManager.sharedInstance().unregisterComponent(lama);
            garisPanel.setData(t, bulanan[0], bulanan[1], b);
            if (tampilGaris) {
                sp.setViewportView(garisPanel);
                if (bl != null) {
                    garisPanel.mulaiAnimasi();
                } else {
                    garisPanel.setBulan(b);
                }
            } else {
                sp.setViewportView(chart);
                sp.getVerticalScrollBar().setValue(0);
                chart.mulaiAnimasi();
            }
            btnGaris.setEnabled(true);
            pemilih.setData(t, b, bulanan[0], bulanan[1]);
            pemilih.setMemuat(false);
            sedangMemuat = false;
        }

        private static javax.swing.JButton gayaTombol(javax.swing.JButton b) {
            b.setFocusPainted(false);
            b.setBackground(new java.awt.Color(0x24, 0x3B, 0x55));
            b.setForeground(java.awt.Color.WHITE);
            b.setFont(FontCache.get(java.awt.Font.BOLD, 12));
            b.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0x3E, 0x5C, 0x7E)),
                    javax.swing.BorderFactory.createEmptyBorder(7, 16, 7, 16)));
            b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            return b;
        }

        /** Gambar lengkap (header + semua operator + ringkasan) tanpa animasi; aman dipakai tanpa jendela terbuka. */
        static java.awt.image.BufferedImage buatGambar(Hasil hs, String periode, int lebar) {
            BarChartPanel ch = new BarChartPanel(hs);
            ch.setStatis(true);
            HeaderPanel hd = new HeaderPanel(periode, hs);
            int tinggiChart = ch.getPreferredSize().height;
            int tinggi = 150 + tinggiChart;
            hd.setBounds(0, 0, lebar, 150);
            ch.setBounds(0, 150, lebar, tinggiChart);
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(lebar, tinggi, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = img.createGraphics();
            hd.paint(g);
            g.translate(0, 150);
            ch.paint(g);
            g.dispose();
            return img;
        }

        /** Gambar header + line chart setahun tanpa animasi. */
        static java.awt.image.BufferedImage buatGambarLine(Hasil hs, String periode, int tahun, int[] ini, int[] lalu, int bulanSel, int lebar) {
            LineChartPanel lc = new LineChartPanel(null);
            lc.setData(tahun, ini, lalu, bulanSel);
            lc.setStatis();
            HeaderPanel hd = new HeaderPanel(periode, hs);
            int tinggiChart = 560;
            hd.setBounds(0, 0, lebar, 150);
            lc.setBounds(0, 150, lebar, tinggiChart);
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(lebar, 150 + tinggiChart, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = img.createGraphics();
            hd.paint(g);
            g.translate(0, 150);
            lc.paint(g);
            g.dispose();
            return img;
        }

        private void simpanPng() {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setDialogTitle("Simpan Grafik sebagai Gambar");
            fc.setSelectedFile(new java.io.File((tampilGaris ? "Grafik_Line_Operasi_" : "Grafik_Rekap_Operasi_")
                    + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".png"));
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Gambar PNG (*.png)", "png"));
            if (fc.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
                return;
            }
            java.io.File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".png")) {
                f = new java.io.File(f.getParentFile(), f.getName() + ".png");
            }
            if (f.exists() && javax.swing.JOptionPane.showConfirmDialog(this, "File sudah ada. Timpa?", "Konfirmasi",
                    javax.swing.JOptionPane.YES_NO_OPTION) != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }
            try {
                java.awt.image.BufferedImage img = tampilGaris ? buatGambarLine(hasil, periode, tahun, bulanan[0], bulanan[1], bulan, Math.max(1000, getWidth()))
                        : buatGambar(hasil, periode, Math.max(1000, getWidth()));
                try {
                    javax.imageio.ImageIO.write(img, "png", f);
                } finally {
                    img.flush();
                }
                javax.swing.JOptionPane.showMessageDialog(this, "Gambar berhasil disimpan:\n" + f.getAbsolutePath());
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal menyimpan gambar:\n" + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public JTable getTable() {
        return tbRekap;
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgRekapOperasi dialog = new DlgRekapOperasi(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }
}
