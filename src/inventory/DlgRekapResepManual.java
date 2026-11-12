package inventory;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Rekap resep manual: resep yang diinput akun Apoteker / Tenaga Teknis Kefarmasian,
 * baik lewat DlgResepObat maupun lewat DlgPeresepanDokter.
 *
 * Penginput : resep_manual_apoteker.nip (lihat resep_manual_apoteker.sql)
 * Asal input: resep_obat.tgl_peresepan='0000-00-00' berarti DlgResepObat,
 *             selain itu DlgPeresepanDokter.
 * Hanya resep yang penginputnya tercatat dan berjabatan Apoteker / Tenaga Teknis Kefarmasian.
 */
public final class DlgRekapResepManual extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private final Connection koneksi = koneksiDB.condb();
    private final validasi Valid = new validasi();
    // Jabatan petugas yang dianggap penginput resep manual (sesuai jabatan.nm_jbtn).
    // Ubah di sini kalau penulisan di database Anda berbeda.
    private static final String[] JABATAN_PENGINPUT = {"Apoteker", "Tenaga Teknis Kefarmasian"};
    private PreparedStatement ps;
    private ResultSet rs;

    private final widget.Table tbRekap = new widget.Table();
    private final widget.Tanggal DTPCari1 = new widget.Tanggal();
    private final widget.Tanggal DTPCari2 = new widget.Tanggal();
    private final widget.TextBox TCari = new widget.TextBox();
    private final widget.Button BtnCari = new widget.Button();
    private final widget.Button BtnAll = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();
    private final widget.Label LCount = new widget.Label();
    private final widget.Label LTotal = new widget.Label();

    /**
     * @param parent
     * @param modal
     */
    public DlgRekapResepManual(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        this.setLocation(8, 1);
        setSize(900, 674);

        Object[] row = {"No.Resep", "Tgl.Resep", "No.Rawat", "No.RM", "Pasien", "Dokter",
            "Status", "Asal Input", "Apoteker Penginput", "Jml Item", "Total Biaya"};
        tabMode = new DefaultTableModel(null, row) {
            @Override public boolean isCellEditable(int rowIndex, int colIndex) { return false; }
        };
        tbRekap.setModel(tabMode);
        tbRekap.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbRekap.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebar = {100, 130, 120, 80, 200, 180, 60, 120, 240, 70, 110};
        for (int i = 0; i < lebar.length; i++) {
            TableColumn column = tbRekap.getColumnModel().getColumn(i);
            column.setPreferredWidth(lebar[i]);
        }
        tbRekap.setDefaultRenderer(Object.class, new WarnaTable());

        widget.InternalFrame frame = new widget.InternalFrame();
        frame.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
            "::[ Rekap Resep Manual (Input Apoteker) ]::"));
        frame.setLayout(new BorderLayout(1, 1));

        widget.ScrollPane scroll = new widget.ScrollPane();
        scroll.setViewportView(tbRekap);
        frame.add(scroll, BorderLayout.CENTER);

        // panel bawah: record, total, keluar
        widget.panelisi panelBawah = new widget.panelisi();
        panelBawah.setPreferredSize(new Dimension(55, 55));
        panelBawah.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        widget.Label lblRecord = new widget.Label();
        lblRecord.setText("Record :");
        lblRecord.setPreferredSize(new Dimension(60, 23));
        LCount.setText("0");
        LCount.setPreferredSize(new Dimension(50, 23));
        widget.Label lblTotal = new widget.Label();
        lblTotal.setText("Total Biaya :");
        lblTotal.setPreferredSize(new Dimension(80, 23));
        LTotal.setText("0");
        LTotal.setPreferredSize(new Dimension(120, 23));
        BtnKeluar.setText("Keluar");
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setPreferredSize(new Dimension(100, 30));
        BtnKeluar.addActionListener(e -> dispose());
        panelBawah.add(lblRecord);
        panelBawah.add(LCount);
        panelBawah.add(lblTotal);
        panelBawah.add(LTotal);
        panelBawah.add(BtnKeluar);

        // panel filter
        widget.panelisi panelFilter = new widget.panelisi();
        panelFilter.setPreferredSize(new Dimension(55, 55));
        panelFilter.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        widget.Label lblTgl = new widget.Label();
        lblTgl.setText("Tanggal :");
        lblTgl.setPreferredSize(new Dimension(60, 23));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setPreferredSize(new Dimension(95, 23));
        widget.Label lblSd = new widget.Label();
        lblSd.setText("s.d.");
        lblSd.setPreferredSize(new Dimension(23, 23));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setPreferredSize(new Dimension(95, 23));
        widget.Label lblKey = new widget.Label();
        lblKey.setText("Key Word :");
        lblKey.setPreferredSize(new Dimension(70, 23));
        TCari.setPreferredSize(new Dimension(220, 23));
        TCari.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampil();
                }
            }
        });
        BtnCari.setText("Cari");
        BtnCari.setMnemonic('C');
        BtnCari.setPreferredSize(new Dimension(80, 30));
        BtnCari.addActionListener(e -> tampil());
        BtnAll.setText("Semua");
        BtnAll.setMnemonic('M');
        BtnAll.setPreferredSize(new Dimension(80, 30));
        BtnAll.addActionListener(e -> {
            TCari.setText("");
            tampil();
        });
        panelFilter.add(lblTgl);
        panelFilter.add(DTPCari1);
        panelFilter.add(lblSd);
        panelFilter.add(DTPCari2);
        panelFilter.add(lblKey);
        panelFilter.add(TCari);
        panelFilter.add(BtnCari);
        panelFilter.add(BtnAll);

        JPanel bawah = new JPanel(new BorderLayout(1, 1));
        bawah.add(panelFilter, BorderLayout.CENTER);
        bawah.add(panelBawah, BorderLayout.SOUTH);
        frame.add(bawah, BorderLayout.SOUTH);

        getContentPane().add(frame, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) {
                DTPCari1.setDate(new Date());
                DTPCari2.setDate(new Date());
                tampil();
            }
        });
    }

    public void tampil() {
        Valid.tabelKosong(tabMode);
        double jumlahTotal = 0;
        try {
            ps = koneksi.prepareStatement(
                "select resep_obat.no_resep,if(resep_obat.tgl_peresepan='0000-00-00',resep_obat.tgl_perawatan,resep_obat.tgl_peresepan) as tgl_resep,"
                + "if(resep_obat.tgl_peresepan='0000-00-00',resep_obat.jam,resep_obat.jam_peresepan) as jam_resep,"
                + "if(resep_obat.tgl_peresepan='0000-00-00','Resep Obat (Farmasi)','Peresepan Dokter') as asal_input,resep_obat.no_rawat,"
                + "pasien.no_rkm_medis,pasien.nm_pasien,dokter.nm_dokter,resep_obat.status,"
                + "resep_manual_apoteker.nip,petugas.nama as nm_apoteker,jabatan.nm_jbtn,"
                + "if(resep_obat.tgl_peresepan='0000-00-00',"
                + " (select count(*) from detail_pemberian_obat where detail_pemberian_obat.no_rawat=resep_obat.no_rawat "
                + "  and detail_pemberian_obat.tgl_perawatan=resep_obat.tgl_perawatan and detail_pemberian_obat.jam=resep_obat.jam),"
                + " (select count(*) from resep_dokter where resep_dokter.no_resep=resep_obat.no_resep)"
                + " +(select count(*) from resep_dokter_racikan where resep_dokter_racikan.no_resep=resep_obat.no_resep)) as jml_item,"
                + "(select ifnull(sum(detail_pemberian_obat.total),0) from detail_pemberian_obat where detail_pemberian_obat.no_rawat=resep_obat.no_rawat "
                + " and detail_pemberian_obat.tgl_perawatan=resep_obat.tgl_perawatan and detail_pemberian_obat.jam=resep_obat.jam) as total_biaya "
                + "from resep_obat inner join reg_periksa on resep_obat.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "inner join dokter on resep_obat.kd_dokter=dokter.kd_dokter "
                + "inner join resep_manual_apoteker on resep_manual_apoteker.no_resep=resep_obat.no_resep "
                + "inner join petugas on petugas.nip=resep_manual_apoteker.nip "
                + "inner join jabatan on jabatan.kd_jbtn=petugas.kd_jbtn "
                + "where jabatan.nm_jbtn in (" + daftarJabatan() + ") "
                + "and if(resep_obat.tgl_peresepan='0000-00-00',resep_obat.tgl_perawatan,resep_obat.tgl_peresepan) between ? and ? "
                + (TCari.getText().trim().equals("") ? "" :
                    "and (resep_obat.no_resep like ? or resep_obat.no_rawat like ? or pasien.no_rkm_medis like ? "
                    + "or pasien.nm_pasien like ? or dokter.nm_dokter like ? or petugas.nama like ? "
                    + "or resep_manual_apoteker.nip like ?) ")
                + "order by tgl_resep,jam_resep");
            try {
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
                if (!TCari.getText().trim().equals("")) {
                    for (int i = 3; i <= 9; i++) {
                        ps.setString(i, "%" + TCari.getText().trim() + "%");
                    }
                }
                rs = ps.executeQuery();
                int jumlah = 0;
                while (rs.next()) {
                    String apoteker = rs.getString("nip") + " " + rs.getString("nm_apoteker")
                        + " (" + rs.getString("nm_jbtn") + ")";
                    tabMode.addRow(new Object[]{
                        rs.getString("no_resep"),
                        rs.getString("tgl_resep") + " " + rs.getString("jam_resep"),
                        rs.getString("no_rawat"), rs.getString("no_rkm_medis"),
                        rs.getString("nm_pasien"), rs.getString("nm_dokter"),
                        rs.getString("status"), rs.getString("asal_input"), apoteker,
                        rs.getInt("jml_item"), Valid.SetAngka(rs.getDouble("total_biaya"))
                    });
                    jumlahTotal += rs.getDouble("total_biaya");
                    jumlah++;
                }
                LCount.setText("" + jumlah);
                LTotal.setText(Valid.SetAngka(jumlahTotal));
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private String daftarJabatan() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < JABATAN_PENGINPUT.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("'").append(JABATAN_PENGINPUT[i].replace("'", "''")).append("'");
        }
        return sb.toString();
    }

    public void isCek() {
        BtnCari.setEnabled(akses.getresep_obat());
    }
}
