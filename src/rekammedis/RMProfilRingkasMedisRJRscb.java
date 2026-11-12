package rekammedis;

import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.util.Date;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import widget.*;

public class RMProfilRingkasMedisRJRscb extends JDialog {

    private InternalFrame internalFrame1;
    private panelisi panelBawah;
    private Button BtnPrint, BtnKeluar, BtnCari, BtnAll;
    private TextBox TNoRw, TNoRM, TPasien, TCari;
    private Label LCount;
    private JRadioButton R1, R2, R3;
    private widget.Tanggal DTPCari1, DTPCari2;

    private java.sql.Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private java.sql.PreparedStatement ps, ps2;
    private java.sql.ResultSet rs, rs2;
    private StringBuilder htmlContent;
    private int urut;

    private JEditorPane LoadHTML;
    public widget.editorpane LoadHTMLCetak;

    public RMProfilRingkasMedisRJRscb(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setSize(900, 650);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        LoadHTMLCetak = new widget.editorpane();
        internalFrame1 = new InternalFrame();
        internalFrame1.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(240, 245, 235)),
            "::[ Profil Ringkas Medis Rawat Jalan (PRMRJ) ]::",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new java.awt.Font("Tahoma", 0, 11), new Color(50, 50, 50)));
        internalFrame1.setLayout(new BorderLayout(1, 1));

        // PANEL ATAS
        panelisi panelAtas = new panelisi();
        panelAtas.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 4));
        panelAtas.setPreferredSize(new Dimension(1024, 33));
        Label lblNoRw = new Label(); lblNoRw.setText("No.Rawat :");
        TNoRw = new TextBox(); TNoRw.setPreferredSize(new Dimension(130, 23)); TNoRw.setEditable(false);
        TNoRM = new TextBox(); TNoRM.setPreferredSize(new Dimension(100, 23)); TNoRM.setEditable(false);
        TPasien = new TextBox(); TPasien.setPreferredSize(new Dimension(250, 23)); TPasien.setEditable(false);
        panelAtas.add(lblNoRw); panelAtas.add(TNoRw); panelAtas.add(TNoRM); panelAtas.add(TPasien);

        TCari = new TextBox();

        // HTML VIEWER
        LoadHTML = new JEditorPane();
        LoadHTML.setEditable(false);
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet ss = kit.getStyleSheet();
        ss.addRule("body{font-family:Tahoma;font-size:9px;margin:5px;}");
        ss.addRule("table{width:100%;border-collapse:collapse;}");
        ss.addRule("tr.head td{background:#e8e8e8;font-weight:bold;border:1px solid #999;padding:4px 6px;font-size:10px;}");
        ss.addRule("tr.isi td{border-bottom:1px solid #ddd;padding:2px 6px;font-size:9px;}");
        ss.addRule("tr.sub td{background:#f5f5f5;font-weight:bold;padding:2px 6px;font-size:9px;border-bottom:1px solid #ccc;}");
        ss.addRule("tr.spacer td{padding:8px 0;border:none;}");
        LoadHTML.setEditorKit(kit);
        LoadHTML.setDocument(kit.createDefaultDocument());

        JScrollPane scrollHTML = new JScrollPane(LoadHTML);

        // PANEL BAWAH
        panelBawah = new panelisi(); panelBawah.setPreferredSize(new Dimension(44, 54));
        panelBawah.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 9));
        BtnPrint = new Button(); BtnPrint.setIcon(new ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnPrint.setMnemonic('T'); BtnPrint.setText("Cetak"); BtnPrint.setPreferredSize(new Dimension(100, 30));
        BtnKeluar = new Button(); BtnKeluar.setIcon(new ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('K'); BtnKeluar.setText("Keluar"); BtnKeluar.setPreferredSize(new Dimension(100, 30));

        // Radio buttons filter
        ButtonGroup bg = new ButtonGroup();
        R1 = new JRadioButton("5 Terakhir"); R1.setSelected(true); R1.setPreferredSize(new Dimension(90, 23)); R1.setFont(new java.awt.Font("Tahoma", 0, 11));
        R2 = new JRadioButton("Semua"); R2.setPreferredSize(new Dimension(65, 23)); R2.setFont(new java.awt.Font("Tahoma", 0, 11));
        R3 = new JRadioButton("Tanggal :"); R3.setPreferredSize(new Dimension(75, 23)); R3.setFont(new java.awt.Font("Tahoma", 0, 11));
        bg.add(R1); bg.add(R2); bg.add(R3);

        DTPCari1 = new widget.Tanggal(); DTPCari1.setDisplayFormat("dd-MM-yyyy"); DTPCari1.setPreferredSize(new Dimension(90, 23));
        Label lblSd = new Label(); lblSd.setText("s.d."); lblSd.setPreferredSize(new Dimension(20, 23));
        DTPCari2 = new widget.Tanggal(); DTPCari2.setDisplayFormat("dd-MM-yyyy"); DTPCari2.setPreferredSize(new Dimension(90, 23));

        BtnCari = new Button(); BtnCari.setIcon(new ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setPreferredSize(new Dimension(28, 23));
        BtnAll = new Button(); BtnAll.setIcon(new ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setPreferredSize(new Dimension(28, 23));
        LCount = new Label(); LCount.setText("Record : 0"); LCount.setPreferredSize(new Dimension(80, 23));

        panelBawah.add(BtnPrint); panelBawah.add(BtnKeluar);
        panelBawah.add(R1); panelBawah.add(R2); panelBawah.add(R3);
        panelBawah.add(DTPCari1); panelBawah.add(lblSd); panelBawah.add(DTPCari2);
        panelBawah.add(BtnCari); panelBawah.add(BtnAll); panelBawah.add(LCount);

        // ACTIONS
        BtnKeluar.addActionListener(evt -> dispose());
        BtnCari.addActionListener(evt -> tampil());
        BtnAll.addActionListener(evt -> { TCari.setText(""); tampil(); });
        BtnPrint.addActionListener(evt -> cetakPRMRJ());
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent evt) { if (evt.getKeyCode() == KeyEvent.VK_ENTER) tampil(); }
        });

        // ASSEMBLY
        internalFrame1.add(panelAtas, BorderLayout.PAGE_START);
        internalFrame1.add(scrollHTML, BorderLayout.CENTER);
        internalFrame1.add(panelBawah, BorderLayout.PAGE_END);
        setLayout(new BorderLayout());
        add(internalFrame1, BorderLayout.CENTER);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
    }

    public void tampil() {
        if (TNoRM.getText().trim().isEmpty()) return;

        htmlContent = new StringBuilder();
        htmlContent.append("<html><body><table>");
        String noRM = TNoRM.getText().trim();
        urut = 0;

        try {
            String baseQuery = "select rp.no_rawat, rp.tgl_registrasi, rp.jam_reg, rp.kd_dokter, " +
                "d.nm_dokter, pl.nm_poli, pj.png_jawab " +
                "from reg_periksa rp " +
                "inner join dokter d on rp.kd_dokter=d.kd_dokter " +
                "inner join poliklinik pl on rp.kd_poli=pl.kd_poli " +
                "inner join penjab pj on rp.kd_pj=pj.kd_pj " +
                "where rp.stts<>'Batal' and rp.no_rkm_medis=? and rp.status_lanjut='Ralan' ";

            if (R3.isSelected()) {
                baseQuery += "and rp.tgl_registrasi between ? and ? ";
            }
            if (!TCari.getText().trim().isEmpty()) {
                baseQuery += "and (pl.nm_poli like ? or d.nm_dokter like ?) ";
            }
            baseQuery += "order by rp.tgl_registrasi desc, rp.jam_reg desc";
            if (R1.isSelected()) {
                baseQuery += " limit 5";
            }
            // Pemilihan baris tetap dari yang terbaru (agar "5 Terakhir" benar),
            // tapi tampilannya diurutkan dari kunjungan terlama ke terbaru
            baseQuery = "select * from (" + baseQuery + ") x order by x.tgl_registrasi asc, x.jam_reg asc";

            ps = koneksi.prepareStatement(baseQuery);
            int idx = 1;
            ps.setString(idx++, noRM);
            if (R3.isSelected()) {
                ps.setString(idx++, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
                ps.setString(idx++, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
            }
            if (!TCari.getText().trim().isEmpty()) {
                ps.setString(idx++, "%" + TCari.getText().trim() + "%");
                ps.setString(idx++, "%" + TCari.getText().trim() + "%");
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                urut++;
                String norw = rs.getString("no_rawat");
                String tglReg = rs.getString("tgl_registrasi");
                // Header kunjungan
                htmlContent.append("<tr class='head'>")
                    .append("<td colspan='4'>Kunjungan ke-").append(urut)
                    .append(" &nbsp;&nbsp; ").append(tglReg).append(" ").append(rs.getString("jam_reg"))
                    .append(" &nbsp;&nbsp; ").append(rs.getString("nm_poli"))
                    .append("</td></tr>");

                addRow("Dokter", rs.getString("nm_dokter"));
                addRow("Cara Bayar", rs.getString("png_jawab"));

                // Diagnosis + ICD-10
                try {
                    ps2 = koneksi.prepareStatement(
                        "select dp.kd_penyakit, py.nm_penyakit from diagnosa_pasien dp " +
                        "inner join penyakit py on dp.kd_penyakit=py.kd_penyakit " +
                        "where dp.no_rawat=? order by dp.prioritas asc");
                    ps2.setString(1, norw);
                    rs2 = ps2.executeQuery();
                    StringBuilder sbDiag = new StringBuilder();
                    int nd = 1;
                    while (rs2.next()) {
                        if (sbDiag.length() > 0) sbDiag.append("<br>");
                        sbDiag.append(nd++).append(". ").append(rs2.getString("kd_penyakit")).append(" - ").append(rs2.getString("nm_penyakit"));
                    }
                    if (sbDiag.length() > 0) addRow("Diagnosis (ICD-10)", sbDiag.toString());
                } catch (Exception e) {} finally { closeSub(); }

                // Pemeriksaan Penunjang (Lab)
                try {
                    ps2 = koneksi.prepareStatement(
                        "select jpl.nm_perawatan from periksa_lab pl " +
                        "inner join jns_perawatan_lab jpl on pl.kd_jenis_prw=jpl.kd_jenis_prw " +
                        "where pl.no_rawat=?");
                    ps2.setString(1, norw);
                    rs2 = ps2.executeQuery();
                    StringBuilder sb = new StringBuilder();
                    int np = 1;
                    while (rs2.next()) {
                        if (sb.length() > 0) sb.append("<br>");
                        sb.append(np++).append(". ").append(rs2.getString("nm_perawatan"));
                    }
                    if (sb.length() > 0) addRow("Pemeriksaan Penunjang", sb.toString());
                } catch (Exception e) {} finally { closeSub(); }

                // Obat-obatan
                try {
                    ps2 = koneksi.prepareStatement(
                        "select db.nama_brng, dpo.jml, db.kode_sat from detail_pemberian_obat dpo " +
                        "inner join databarang db on dpo.kode_brng=db.kode_brng " +
                        "where dpo.no_rawat=? group by dpo.kode_brng");
                    ps2.setString(1, norw);
                    rs2 = ps2.executeQuery();
                    StringBuilder sb = new StringBuilder();
                    int no = 1;
                    while (rs2.next()) {
                        if (sb.length() > 0) sb.append("<br>");
                        sb.append(no++).append(". ").append(rs2.getString("nama_brng"));
                    }
                    if (sb.length() > 0) addRow("Obat-obatan", sb.toString());
                } catch (Exception e) {} finally { closeSub(); }

                // Riwayat Inap (semua rawat inap pasien s/d tanggal ini)
                try {
                    ps2 = koneksi.prepareStatement(
                        "select DATE_FORMAT(ki.tgl_masuk,'%d-%m-%Y') as masuk, " +
                        "DATE_FORMAT(ki.tgl_keluar,'%d-%m-%Y') as keluar, " +
                        "b.nm_bangsal, ki.diagnosa_akhir, ki.stts_pulang " +
                        "from kamar_inap ki inner join kamar k on ki.kd_kamar=k.kd_kamar " +
                        "inner join bangsal b on k.kd_bangsal=b.kd_bangsal " +
                        "inner join reg_periksa rp2 on ki.no_rawat=rp2.no_rawat " +
                        "where rp2.no_rkm_medis=? and ki.tgl_masuk<=? " +
                        "order by ki.tgl_masuk desc limit 3");
                    ps2.setString(1, noRM);
                    ps2.setString(2, tglReg);
                    rs2 = ps2.executeQuery();
                    StringBuilder sb = new StringBuilder();
                    int ni = 1;
                    while (rs2.next()) {
                        if (sb.length() > 0) sb.append("<br>");
                        sb.append(ni++).append(". ").append(rs2.getString("masuk"))
                            .append(" s/d ").append(rs2.getString("keluar") != null ? rs2.getString("keluar") : "-")
                            .append(" - ").append(rs2.getString("nm_bangsal"))
                            .append(rs2.getString("diagnosa_akhir") != null && !rs2.getString("diagnosa_akhir").isEmpty() ?
                                " ("+rs2.getString("diagnosa_akhir")+")" : "");
                    }
                    if (sb.length() > 0) addRow("Riwayat Rawat Inap", sb.toString());
                } catch (Exception e) {} finally { closeSub(); }

                // Prosedur Bedah/Operasi
                try {
                    ps2 = koneksi.prepareStatement(
                        "select po.nm_perawatan, DATE_FORMAT(bo.tanggal,'%d-%m-%Y') as tgl_op, d2.nm_dokter " +
                        "from booking_operasi bo " +
                        "inner join paket_operasi po on bo.kode_paket=po.kode_paket " +
                        "inner join dokter d2 on bo.kd_dokter=d2.kd_dokter " +
                        "inner join reg_periksa rp3 on bo.no_rawat=rp3.no_rawat " +
                        "where rp3.no_rkm_medis=? and bo.tanggal<=? " +
                        "order by bo.tanggal desc limit 3");
                    ps2.setString(1, noRM);
                    ps2.setString(2, tglReg);
                    rs2 = ps2.executeQuery();
                    StringBuilder sb = new StringBuilder();
                    int nb = 1;
                    while (rs2.next()) {
                        if (sb.length() > 0) sb.append("<br>");
                        sb.append(nb++).append(". ").append(rs2.getString("tgl_op"))
                            .append(" - ").append(rs2.getString("nm_perawatan"))
                            .append(" (").append(rs2.getString("nm_dokter")).append(")");
                    }
                    if (sb.length() > 0) addRow("Prosedur Bedah/Operasi", sb.toString());
                } catch (Exception e) {} finally { closeSub(); }

                // Spacer
                htmlContent.append("<tr class='spacer'><td colspan='4'></td></tr>");
            }
            LCount.setText("Record : " + urut);
        } catch (Exception e) {
            System.out.println("Notif PRMRJ : " + e);
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } catch (Exception e) {}
        }

        htmlContent.append("</table></body></html>");
        LoadHTML.setText(htmlContent.toString());
        LoadHTML.setCaretPosition(0);
    }

    private void addRow(String label, String value) {
        htmlContent.append("<tr class='isi'>")
            .append("<td valign='top' width='3%'></td>")
            .append("<td valign='top' width='18%'>").append(label).append("</td>")
            .append("<td valign='top' width='1%' align='center'>:</td>")
            .append("<td valign='top' width='78%'>").append(value).append("</td>")
            .append("</tr>");
    }

    private void closeSub() {
        try { if (rs2 != null) rs2.close(); if (ps2 != null) ps2.close(); } catch (Exception e) {}
    }

    public void getData() {}
    public void emptTeks() {}

    public void setNoRm(String norwt, Date tgl) {
        TNoRw.setText(norwt);
        TNoRM.setText(Sequel.cariIsi("select no_rkm_medis from reg_periksa where no_rawat=?", norwt));
        TPasien.setText(Sequel.cariIsi("select nm_pasien from pasien where no_rkm_medis=?", TNoRM.getText()));
        tampil();
    }

    public void setNoRm(String norwt, String norm, String namapasien) {
        TNoRw.setText(norwt);
        TNoRM.setText(norm);
        TPasien.setText(namapasien);
        TCari.setText("");
        tampil();
    }

    public void isCek() {
        BtnPrint.setEnabled(akses.getrujukan_keluar());
    }

    private void cetakPRMRJ() {
        if (urut == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, data kosong..!!!");
            return;
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String tglLahirPasien = Sequel.cariIsi("select DATE_FORMAT(tgl_lahir,'%d-%m-%Y') from pasien where no_rkm_medis=?", TNoRM.getText());
        String jkPasien = Sequel.cariIsi("select if(jk='L','L','P') from pasien where no_rkm_medis=?", TNoRM.getText());

        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>")
            .append("body{font-family:Tahoma;font-size:9px;margin:15px;color:#000;}")
            .append("table{border-collapse:collapse;width:100%;}")
            .append(".hdr{width:100%;border-bottom:2px solid #000;padding-bottom:5px;margin-bottom:5px;}")
            .append(".rm-box{border:1px solid #000;padding:5px;text-align:center;font-weight:bold;font-size:8px;width:120px;}")
            .append(".title{font-weight:bold;font-size:11px;margin:5px 0;}")
            .append(".pi td{padding:1px 5px;font-size:8px;}")
            .append("tr.head td{background:#d0d0d0;font-weight:bold;border:1px solid #999;padding:5px 8px;font-size:9px;}")
            .append("tr.isi td{border-bottom:1px solid #ddd;padding:3px 8px;font-size:8px;}")
            .append("tr.spacer td{padding:10px 0;border:none;}")
            .append("@media print{body{margin:10px;}}")
            .append("</style></head><body>");

        // Header RS
        html.append("<table class='hdr'><tr>")
            .append("<td style='text-align:center;'>")
            .append("<div style='font-size:10px;font-weight:bold;'>YAYASAN BETHESDA SERUKAM</div>")
            .append("<div style='font-size:11px;font-weight:bold;letter-spacing:5px;'>").append(akses.getnamars().toUpperCase().replace("RSUD","").replace("RUMAH SAKIT UMUM DAERAH","").trim()).append("</div>")
            .append("<div style='font-size:7px;'>").append(akses.getalamatrs()).append("</div>")
            .append("<div style='font-size:7px;'>Email : ").append(akses.getemailrs()).append(" Telp : ").append(akses.getkontakrs()).append("</div>")
            .append("</td>")
            .append("<td class='rm-box'>RM.RJ.04.006</td>")
            .append("</tr></table>");

        // Title + Patient Info
        html.append("<table style='width:100%;'><tr>")
            .append("<td class='title'>Profil Ringkas Medis Rawat Jalan (PRMRJ)</td>")
            .append("<td><table class='pi'>")
            .append("<tr><td><b>Nomor RM</b></td><td>: ").append(TNoRM.getText()).append("</td></tr>")
            .append("<tr><td><b>Nama</b></td><td>: ").append(TPasien.getText()).append("</td></tr>")
            .append("<tr><td><b>Tanggal Lahir</b></td><td>: ").append(tglLahirPasien).append("</td></tr>")
            .append("<tr><td><b>Jenis Kelamin</b></td><td>: ").append(jkPasien).append("</td></tr>")
            .append("</table></td></tr></table>");

        // Isi data — ambil dari htmlContent tapi strip html/body/table wrapper
        String isiData = htmlContent.toString()
            .replace("<html><body><table>", "")
            .replace("</table></body></html>", "");
        html.append("<table>").append(isiData).append("</table>");

        html.append("<script>window.onload=function(){window.print();}</script>")
            .append("</body></html>");

        try {
            java.io.File tmpFile = java.io.File.createTempFile("prmrj_", ".html");
            tmpFile.deleteOnExit();
            java.io.FileWriter fw = new java.io.FileWriter(tmpFile);
            fw.write(html.toString());
            fw.close();
            java.awt.Desktop.getDesktop().browse(tmpFile.toURI());
        } catch (Exception e) {
            System.out.println("Notif buka browser : " + e);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }
}
