/*
  Dilarang keras menggandakan/mengcopy/menyebarkan/membajak/mendecompile 
  Software ini dalam bentuk apapun tanpa seijin pembuat software
  (Khanza.Soft Media). Bagi yang sengaja membajak softaware ini ta
  npa ijin, kami sumpahi sial 1000 turunan, miskin sampai 500 turu
  nan. Selalu mendapat kecelakaan sampai 400 turunan. Anak pertama
  nya cacat tidak punya kaki sampai 300 turunan. Susah cari jodoh
  sampai umur 50 tahun sampai 200 turunan. Ya Alloh maafkan kami 
  karena telah berdoa buruk, semua ini kami lakukan karena kami ti
  dak pernah rela karya kami dibajak tanpa ijin.
 */

package simrskhanza;

import fungsi.WarnaTable;
import fungsi.WarnaTable2;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import inventory.DlgCariKonversi;
import inventory.DlgCariObat;
import inventory.DlgCariObat2;
import inventory.DlgCariObat3;
import java.awt.Dimension;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

/**
 *
 * @author perpustakaan
 */
public class DlgKeteranganPenunjang extends javax.swing.JDialog {
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private final DefaultTableModel tabModeLab,tabModeRad,tabModeApotek;
    private int i;
    private String
            sqlpscarilab="select jns_perawatan_lab.nm_perawatan, count(periksa_lab.kd_jenis_prw) as jml,periksa_lab.biaya as biaya, "+
                    "sum(periksa_lab.biaya) as total,jns_perawatan_lab.kd_jenis_prw,sum(periksa_lab.tarif_perujuk+periksa_lab.tarif_tindakan_dokter) as totaldokter, "+
                    "sum(periksa_lab.tarif_tindakan_petugas) as totalpetugas,sum(periksa_lab.kso) as totalkso,sum(periksa_lab.bhp) as totalbhp "+
                    " from periksa_lab inner join jns_perawatan_lab on jns_perawatan_lab.kd_jenis_prw=periksa_lab.kd_jenis_prw where "+
                    " periksa_lab.no_rawat=? group by periksa_lab.kd_jenis_prw  ",
            sqlpscariobat="select databarang.nama_brng,jenis.nama,detail_pemberian_obat.biaya_obat,"+
                          "sum(detail_pemberian_obat.jml) as jml,sum(detail_pemberian_obat.embalase+detail_pemberian_obat.tuslah) as tambahan,"+
                          "(sum(detail_pemberian_obat.total)-sum(detail_pemberian_obat.embalase+detail_pemberian_obat.tuslah)) as total, "+
                          "sum((detail_pemberian_obat.h_beli*detail_pemberian_obat.jml)) as totalbeli "+
                          "from detail_pemberian_obat inner join databarang inner join jenis "+
                          "on detail_pemberian_obat.kode_brng=databarang.kode_brng and databarang.kdjns=jenis.kdjns where "+
                          "detail_pemberian_obat.no_rawat=? group by detail_pemberian_obat.kode_brng order by jenis.nama",
            sqlpsdetaillab="select sum(detail_periksa_lab.biaya_item) as total,sum(detail_periksa_lab.bagian_perujuk+detail_periksa_lab.bagian_dokter) as totaldokter, "+
                           "sum(detail_periksa_lab.bagian_laborat) as totalpetugas,sum(detail_periksa_lab.kso) as totalkso,sum(detail_periksa_lab.bhp) as totalbhp "+
                           "from detail_periksa_lab where detail_periksa_lab.no_rawat=? "+
                           "and detail_periksa_lab.kd_jenis_prw=?",
            sqlpscariradiologi="select jns_perawatan_radiologi.nm_perawatan, count(periksa_radiologi.kd_jenis_prw) as jml,periksa_radiologi.biaya as biaya, "+
                    "sum(periksa_radiologi.biaya) as total,jns_perawatan_radiologi.kd_jenis_prw,sum(periksa_radiologi.tarif_perujuk+periksa_radiologi.tarif_tindakan_dokter) as totaldokter, "+
                    "sum(periksa_radiologi.tarif_tindakan_petugas) as totalpetugas,sum(periksa_radiologi.kso) as totalkso,sum(periksa_radiologi.bhp) as totalbhp "+
                    " from periksa_radiologi inner join jns_perawatan_radiologi on jns_perawatan_radiologi.kd_jenis_prw=periksa_radiologi.kd_jenis_prw where "+
                    " periksa_radiologi.no_rawat=? group by periksa_radiologi.kd_jenis_prw  ";
    private PreparedStatement pscarilab,pscariobat,psobatlangsung,psdetaillab,pscariradiologi;
    private ResultSet rscarilab,rscariobat,rsdetaillab,rsobatlangsung,rscariradiologi;
    private WarnaTable2 warna=new WarnaTable2();
    private WarnaTable2 warna2=new WarnaTable2();

    /** Creates new form DlgPemberianObat
     * @param parent
     * @param modal */
    public DlgKeteranganPenunjang(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        tabModeLab=new DefaultTableModel(null,new Object[]{
            "No.Permintaan","Tanggal","Jam","Nama Pemeriksaan","Dokter Perujuk","Status"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbLab.setModel(tabModeLab);

        tbLab.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbLab.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 6; i++) {
            TableColumn column = tbLab.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(100);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(55);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(200);
            }else if(i==5){
                column.setPreferredWidth(100);
            }
        }
        tbLab.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeRad=new DefaultTableModel(null,new Object[]{
            "No.Permintaan","Tanggal","Jam","Nama Pemeriksaan","Dokter Perujuk","Status"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbRadiologi.setModel(tabModeRad);

        tbRadiologi.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbRadiologi.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 6; i++) {
            TableColumn column = tbRadiologi.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(100);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(55);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(200);
            }else if(i==5){
                column.setPreferredWidth(100);
            }
        }
        tbRadiologi.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeApotek=new DefaultTableModel(null,new Object[]{
            "No.Resep","Tanggal","Jam","Dokter Peresep","Status"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbApotek.setModel(tabModeApotek);

        tbApotek.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbApotek.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 5; i++) {
            TableColumn column = tbApotek.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(100);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(55);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(100);
            }
        }
        tbApotek.setDefaultRenderer(Object.class, new WarnaTable());

        this.setLocation(8,1);
        setSize(885,674);
        
        //TCatatan.setText(TCatatan); 
//        TCatatan.setLineWrap(true);
//        TCatatan.setWrapStyleWord(true);
    }

    //private DlgCariObatPenyakit dlgobtpny=new DlgCariObatPenyakit(null,false);
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        FormInput = new widget.PanelBiasa();
        panelPermintaan = new widget.panelisi();
        scrollPane5 = new widget.ScrollPane();
        tbLab = new widget.Table();
        scrollPane6 = new widget.ScrollPane();
        tbRadiologi = new widget.Table();
        scrollPane7 = new widget.ScrollPane();
        tbApotek = new widget.Table();
        jLabel3 = new widget.Label();
        TNoRw = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TPasien = new widget.TextBox();
        jLabel4 = new widget.Label();
        SEP = new widget.TextBox();
        jLabel5 = new widget.Label();
        Catatan = new widget.TextBox();
        panelGlass8 = new widget.panelisi();
        BtnKeluar = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Keterangan SEP & Permintaan Penunjang ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(875, 350));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(865, 800));
        FormInput.setLayout(null);

        panelPermintaan.setBorder(null);
        panelPermintaan.setName("panelPermintaan"); // NOI18N
        panelPermintaan.setPreferredSize(new java.awt.Dimension(100, 137));
        panelPermintaan.setLayout(new java.awt.GridLayout(3, 0));

        scrollPane5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)), "Permintaan Laboratorium : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        scrollPane5.setName("scrollPane5"); // NOI18N

        tbLab.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbLab.setToolTipText("");
        tbLab.setName("tbLab"); // NOI18N
        scrollPane5.setViewportView(tbLab);

        panelPermintaan.add(scrollPane5);

        scrollPane6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)), "Permintaan Radiologi : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        scrollPane6.setName("scrollPane6"); // NOI18N

        tbRadiologi.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbRadiologi.setToolTipText("");
        tbRadiologi.setName("tbRadiologi"); // NOI18N
        scrollPane6.setViewportView(tbRadiologi);

        panelPermintaan.add(scrollPane6);

        scrollPane7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)), "Permintaan Resep : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        scrollPane7.setName("scrollPane7"); // NOI18N

        tbApotek.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbApotek.setToolTipText("");
        tbApotek.setName("tbApotek"); // NOI18N
        scrollPane7.setViewportView(tbApotek);

        panelPermintaan.add(scrollPane7);

        FormInput.add(panelPermintaan);
        panelPermintaan.setBounds(0, 70, 840, 630);

        jLabel3.setText("No.Rawat :");
        jLabel3.setName("jLabel3"); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(jLabel3);
        jLabel3.setBounds(30, 10, 70, 23);

        TNoRw.setEditable(false);
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.setPreferredSize(new java.awt.Dimension(150, 23));
        TNoRw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TNoRwActionPerformed(evt);
            }
        });
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(110, 10, 150, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.setPreferredSize(new java.awt.Dimension(100, 23));
        TNoRM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TNoRMActionPerformed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(260, 10, 100, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.setPreferredSize(new java.awt.Dimension(320, 23));
        TPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TPasienActionPerformed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(360, 10, 320, 23);

        jLabel4.setText("No.SEP :");
        jLabel4.setName("jLabel4"); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(jLabel4);
        jLabel4.setBounds(30, 40, 70, 23);

        SEP.setEditable(false);
        SEP.setHighlighter(null);
        SEP.setName("SEP"); // NOI18N
        SEP.setPreferredSize(new java.awt.Dimension(150, 23));
        SEP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SEPActionPerformed(evt);
            }
        });
        SEP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SEPKeyPressed(evt);
            }
        });
        FormInput.add(SEP);
        SEP.setBounds(110, 40, 150, 23);

        jLabel5.setText("Catatan:");
        jLabel5.setName("jLabel5"); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(jLabel5);
        jLabel5.setBounds(280, 40, 70, 23);

        Catatan.setEditable(false);
        Catatan.setHighlighter(null);
        Catatan.setName("Catatan"); // NOI18N
        Catatan.setPreferredSize(new java.awt.Dimension(150, 23));
        Catatan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CatatanActionPerformed(evt);
            }
        });
        Catatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CatatanKeyPressed(evt);
            }
        });
        FormInput.add(Catatan);
        Catatan.setBounds(360, 40, 320, 23);

        internalFrame1.add(FormInput, java.awt.BorderLayout.CENTER);
        FormInput.getAccessibleContext().setAccessibleName("");
        FormInput.getAccessibleContext().setAccessibleDescription("");

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(100, 56));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cross.png"))); // NOI18N
        BtnKeluar.setMnemonic('T');
        BtnKeluar.setText("Tutup");
        BtnKeluar.setToolTipText("Alt+T");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnKeluar);

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        
    }//GEN-LAST:event_formWindowActivated

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            isRawat();
        }
    }//GEN-LAST:event_TNoRwKeyPressed

    private void TPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TPasienActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TPasienActionPerformed

    private void TNoRwActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TNoRwActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TNoRwActionPerformed

    private void TNoRMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TNoRMActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TNoRMActionPerformed

    private void SEPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SEPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SEPActionPerformed

    private void SEPKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SEPKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_SEPKeyPressed

    private void CatatanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CatatanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CatatanActionPerformed

    private void CatatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CatatanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_CatatanKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgKeteranganPenunjang dialog = new DlgKeteranganPenunjang(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnKeluar;
    public widget.TextBox Catatan;
    private widget.PanelBiasa FormInput;
    public widget.TextBox SEP;
    private widget.TextBox TNoRM;
    public widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel3;
    private widget.Label jLabel4;
    private widget.Label jLabel5;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelPermintaan;
    private widget.ScrollPane scrollPane5;
    private widget.ScrollPane scrollPane6;
    private widget.ScrollPane scrollPane7;
    private widget.Table tbApotek;
    private widget.Table tbLab;
    private widget.Table tbRadiologi;
    // End of variables declaration//GEN-END:variables
    

    private void isRawat() {
         Sequel.cariIsi("select no_rkm_medis from reg_periksa where no_rawat=? ",TNoRM,TNoRw.getText());
    }

    private void isPsien() {
        Sequel.cariIsi("select nm_pasien from pasien where no_rkm_medis=? ",TPasien,TNoRM.getText());
    }

    public void setNoRm(String norwt) {
        TNoRw.setText(norwt);
        Sequel.cariIsi("select no_rkm_medis from reg_periksa where no_rawat=? ",TNoRM,TNoRw.getText());
        Sequel.cariIsi("select nm_pasien from pasien where no_rkm_medis=? ",TPasien,TNoRM.getText());
        Sequel.cariIsi("select no_sep from bridging_sep where no_rawat=? ",SEP,TNoRw.getText());
        Sequel.cariIsi("select catatan from bridging_sep where no_rawat=? ",Catatan,TNoRw.getText());
        keterangan();
   
    }
    
    private void keterangan(){
            try {
                    Valid.tabelKosong(tabModeLab);
                    pscarilab=koneksi.prepareStatement("select permintaan_lab.noorder,permintaan_lab.tgl_permintaan,"+
                        "if(permintaan_lab.jam_permintaan='00:00:00','',permintaan_lab.jam_permintaan) as jam_permintaan,jns_perawatan_lab.nm_perawatan,"+
                        "if(permintaan_lab.tgl_hasil='0000-00-00','Belum Terlayani','Sudah Terlayani') as status,"+
                        "dokter.nm_dokter from permintaan_lab inner join dokter on permintaan_lab.dokter_perujuk=dokter.kd_dokter "+
                        "inner join permintaan_detail_permintaan_lab on permintaan_lab.noorder=permintaan_detail_permintaan_lab.noorder "+
                        "inner join jns_perawatan_lab on permintaan_detail_permintaan_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "+
                        "where permintaan_lab.status='ralan' and permintaan_lab.no_rawat=? group by permintaan_lab.noorder order by permintaan_lab.tgl_permintaan,permintaan_lab.jam_permintaan desc");
                    try {
                        pscarilab.setString(1,TNoRw.getText());
                        rscarilab=pscarilab.executeQuery();
                        while(rscarilab.next()){
                            tabModeLab.addRow(new String[]{
                                rscarilab.getString("noorder"),rscarilab.getString("tgl_permintaan"),rscarilab.getString("jam_permintaan"),rscarilab.getString("nm_perawatan"),rscarilab.getString("nm_dokter"),rscarilab.getString("status")
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rscarilab!=null){
                            rscarilab.close();
                        }
                        if(pscarilab!=null){
                            pscarilab.close();
                        }
                    }

                    pscarilab=koneksi.prepareStatement("select permintaan_labpa.noorder,permintaan_labpa.tgl_permintaan,"+
                        "if(permintaan_labpa.jam_permintaan='00:00:00','',permintaan_labpa.jam_permintaan) as jam_permintaan,"+
                        "if(permintaan_labpa.tgl_hasil='0000-00-00','Belum Terlayani','Sudah Terlayani') as status,"+
                        "dokter.nm_dokter from permintaan_labpa inner join dokter on permintaan_labpa.dokter_perujuk=dokter.kd_dokter "+
                        "where permintaan_labpa.status='ralan' and permintaan_labpa.no_rawat=? order by permintaan_labpa.tgl_permintaan,permintaan_labpa.jam_permintaan desc");
                    try {
                        pscarilab.setString(1,TNoRw.getText());
                        rscarilab=pscarilab.executeQuery();
                        while(rscarilab.next()){
                            tabModeLab.addRow(new String[]{
                                rscarilab.getString("noorder"),rscarilab.getString("tgl_permintaan"),rscarilab.getString("jam_permintaan"),rscarilab.getString("nm_dokter"),rscarilab.getString("status")
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rscarilab!=null){
                            rscarilab.close();
                        }
                        if(pscarilab!=null){
                            pscarilab.close();
                        }
                    }

                    pscarilab=koneksi.prepareStatement("select permintaan_labmb.noorder,permintaan_labmb.tgl_permintaan,"+
                        "if(permintaan_labmb.jam_permintaan='00:00:00','',permintaan_labmb.jam_permintaan) as jam_permintaan,"+
                        "if(permintaan_labmb.tgl_hasil='0000-00-00','Belum Terlayani','Sudah Terlayani') as status,"+
                        "dokter.nm_dokter from permintaan_labmb inner join dokter on permintaan_labmb.dokter_perujuk=dokter.kd_dokter "+
                        "where permintaan_labmb.status='ralan' and permintaan_labmb.no_rawat=? order by permintaan_labmb.tgl_permintaan,permintaan_labmb.jam_permintaan desc");
                    try {
                        pscarilab.setString(1,TNoRw.getText());
                        rscarilab=pscarilab.executeQuery();
                        while(rscarilab.next()){
                            tabModeLab.addRow(new String[]{
                                rscarilab.getString("noorder"),rscarilab.getString("tgl_permintaan"),rscarilab.getString("jam_permintaan"),rscarilab.getString("nm_dokter"),rscarilab.getString("status")
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rscarilab!=null){
                            rscarilab.close();
                        }
                        if(pscarilab!=null){
                            pscarilab.close();
                        }
                    }

                    Valid.tabelKosong(tabModeRad);
                    pscariradiologi=koneksi.prepareStatement("select permintaan_radiologi.noorder,permintaan_radiologi.tgl_permintaan,"+
                        "if(permintaan_radiologi.jam_permintaan='00:00:00','',permintaan_radiologi.jam_permintaan) as jam_permintaan,jns_perawatan_radiologi.nm_perawatan,"+
                        "if(permintaan_radiologi.tgl_hasil='0000-00-00','Belum Terlayani','Sudah Terlayani') as status,"+
                        "dokter.nm_dokter from permintaan_radiologi inner join dokter on permintaan_radiologi.dokter_perujuk=dokter.kd_dokter "+
                        "inner join permintaan_pemeriksaan_radiologi on permintaan_radiologi.noorder=permintaan_pemeriksaan_radiologi.noorder "+
                        "inner join jns_perawatan_radiologi on permintaan_pemeriksaan_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
                        "where permintaan_radiologi.status='ralan' and permintaan_radiologi.no_rawat=? group by permintaan_radiologi.noorder order by permintaan_radiologi.tgl_permintaan,permintaan_radiologi.jam_permintaan desc");
                    try {
                        pscariradiologi.setString(1,TNoRw.getText());
                        rscariradiologi=pscariradiologi.executeQuery();
                        while(rscariradiologi.next()){
                            tabModeRad.addRow(new String[]{
                                rscariradiologi.getString("noorder"),rscariradiologi.getString("tgl_permintaan"),rscariradiologi.getString("jam_permintaan"),rscariradiologi.getString("nm_perawatan"),rscariradiologi.getString("nm_dokter"),rscariradiologi.getString("status")
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rscariradiologi!=null){
                            rscariradiologi.close();
                        }
                        if(pscariradiologi!=null){
                            pscariradiologi.close();
                        }
                    }

                    Valid.tabelKosong(tabModeApotek);
                    psobatlangsung=koneksi.prepareStatement("select resep_obat.no_resep,resep_obat.tgl_peresepan,resep_obat.jam_peresepan,"+
                        " dokter.nm_dokter,if(resep_obat.tgl_perawatan='0000-00-00','Belum Terlayani','Sudah Terlayani') as status "+
                        " from resep_obat inner join dokter on resep_obat.kd_dokter=dokter.kd_dokter "+
                        " where resep_obat.tgl_peresepan<>'0000-00-00' and resep_obat.status='ralan' and resep_obat.no_rawat=? order by resep_obat.tgl_perawatan desc,resep_obat.jam desc");
                    try {
                        psobatlangsung.setString(1,TNoRw.getText());
                        rscariobat=psobatlangsung.executeQuery();
                        while(rscariobat.next()){
                            tabModeApotek.addRow(new String[]{
                                rscariobat.getString("no_resep"),rscariobat.getString("tgl_peresepan"),rscariobat.getString("jam_peresepan"),rscariobat.getString("nm_dokter"),rscariobat.getString("status")
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rscariobat!=null){
                            rscariobat.close();
                        }
                        if(psobatlangsung!=null){
                            psobatlangsung.close();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif : "+e);
                }
            }
    }
    
    
//    public void isCe?k(){
//        BtnSimpan.setEnabled(true);
//        BtnHapus.setEnabled(true);
//        BtnEdit.setEnabled(true);
//    }



//}
