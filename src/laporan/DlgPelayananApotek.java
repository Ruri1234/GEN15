/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgLhtBiaya.java
 *
 * Created on 12 Jul 10, 16:21:34
 */

package laporan;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author perpustakaan
 */
public final class DlgPelayananApotek extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;
    // indeks metrik: 0=Peresepan-Validasi, 1=Peresepan-Obat Siap, 2=Validasi-Obat Siap,
    // 3=Validasi-Penyerahan, 4=Obat Siap-Penyerahan, 5=Peresepan-Penyerahan
    private final double[] sum=new double[6];
    private final int[] cnt=new int[6];
    private final int[][] bucket=new int[6][4]; // 0-15, >15-30, >30-60, >60 menit
    private static final int JML_KOLOM=21;
    private String filter="";
    private final java.util.List<Object[]> semuaBaris=new java.util.ArrayList<>(); // seluruh baris hasil tampil()
    private volatile boolean resetCombo=false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    /** Creates new form DlgLhtBiaya
     * @param parent
     * @param modal */
    public DlgPelayananApotek(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        Object[] rowRwJlDr={
            "No.","No.Rawat","No.RM","Nama Pasien","No Resep","Dokter","Poli","Penanggung Jawab","Jenis Resep","Metode Racik","Jam Peresepan","Jam Validasi","Jam Obat Siap","Jam Penyerahan","Peresepan-Validasi","Peresepan-Obat Siap","Validasi-Obat Siap","Validasi-Penyerahan","Obat Siap-Penyerahan","Peresepan-Penyerahan","Keterangan"
        };
        tabMode=new DefaultTableModel(null,rowRwJlDr){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbBangsal.setModel(tabMode);
        //tbBangsal.setDefaultRenderer(Object.class, new WarnaTable(jPanel2.getBackground(),tbBangsal.getBackground()));
        tbBangsal.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbBangsal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < JML_KOLOM; i++) {
            TableColumn column = tbBangsal.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(30);
            }else if(i==1){
                column.setPreferredWidth(110);
            }else if(i==2){
                column.setPreferredWidth(65);
            }else if(i==3){
                column.setPreferredWidth(170);
            }else if(i==4){
                column.setPreferredWidth(90);
            }else if(i==5){
                column.setPreferredWidth(130);
            }else if(i==6){
                column.setPreferredWidth(110);
            }else if(i==7){
                column.setPreferredWidth(110);
            }else if(i==8){
                column.setPreferredWidth(90);
            }else if(i==9){
                column.setPreferredWidth(90);
            }else if(i==10){
                column.setPreferredWidth(120);
            }else if(i==11){
                column.setPreferredWidth(120);
            }else if(i==12){
                column.setPreferredWidth(120);
            }else if(i==13){
                column.setPreferredWidth(120);
            }else if(i==14){
                column.setPreferredWidth(120);
            }else if(i==15){
                column.setPreferredWidth(120);
            }else if(i==16){
                column.setPreferredWidth(120);
            }else if(i==17){
                column.setPreferredWidth(120);
            }else if(i==18){
                column.setPreferredWidth(130);
            }else if(i==19){
                column.setPreferredWidth(120);
            }else if(i==20){
                column.setPreferredWidth(190);
            }
        }
        tbBangsal.setDefaultRenderer(Object.class, new WarnaTable());

        TKd.setDocument(new batasInput((byte)20).getKata(TKd));
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
    }    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TKd = new widget.TextBox();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnResepRacikan = new widget.MenuItem();
        MnResepNonRacikan = new widget.MenuItem();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbBangsal = new widget.Table();
        panelGlass5 = new widget.panelisi();
        label11 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        jLabel8 = new widget.Label();
        BtnPrint = new widget.Button();
        CmbKelengkapan = new widget.ComboBox();
        BtnKeluar = new widget.Button();

        TKd.setForeground(new java.awt.Color(255, 255, 255));
        TKd.setName("TKd"); // NOI18N

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnResepRacikan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnResepRacikan.setText("Filter Resep Racikan");
        MnResepRacikan.setName("MnResepRacikan"); // NOI18N
        MnResepRacikan.setPreferredSize(new java.awt.Dimension(180, 26));
        MnResepRacikan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnResepRacikanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnResepRacikan);

        MnResepNonRacikan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnResepNonRacikan.setText("Filter Resep Non Racikan");
        MnResepNonRacikan.setName("MnResepNonRacikan"); // NOI18N
        MnResepNonRacikan.setPreferredSize(new java.awt.Dimension(180, 26));
        MnResepNonRacikan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnResepNonRacikanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnResepNonRacikan);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Lama Pelayanan Apotek ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setComponentPopupMenu(jPopupMenu1);
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbBangsal.setComponentPopupMenu(jPopupMenu1);
        tbBangsal.setName("tbBangsal"); // NOI18N
        tbBangsal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbBangsalMouseClicked(evt);
            }
        });
        tbBangsal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbBangsalKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbBangsal);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass5.setName("panelGlass5"); // NOI18N
        panelGlass5.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass5.add(label11);

        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl1);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(25, 23));
        panelGlass5.add(label18);

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass5.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(155, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        TCari.setToolTipText("Filter Jenis Resep (Racikan / Non Racikan) dan Metode Racik");
        panelGlass5.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
        BtnCari.setToolTipText("Alt+2");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariKeyPressed(evt);
            }
        });
        panelGlass5.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        BtnAll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllKeyPressed(evt);
            }
        });
        panelGlass5.add(BtnAll);

        jLabel8.setText("Kelengkapan :");
        jLabel8.setName("jLabel8"); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(85, 23));
        panelGlass5.add(jLabel8);

        CmbKelengkapan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Lengkap", "Tidak Lengkap" }));
        CmbKelengkapan.setName("CmbKelengkapan"); // NOI18N
        CmbKelengkapan.setPreferredSize(new java.awt.Dimension(110, 23));
        CmbKelengkapan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CmbKelengkapanActionPerformed(evt);
            }
        });
        panelGlass5.add(CmbKelengkapan);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        BtnPrint.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKeyPressed(evt);
            }
        });
        panelGlass5.add(BtnPrint);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
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
        panelGlass5.add(BtnKeluar);

        internalFrame1.add(panelGlass5, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        if(ceksukses==false){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if(tabMode.getRowCount()==0){
                JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
                //TCari.requestFocus();
            }else if(tabMode.getRowCount()!=0){

                Map<String, Object> param = new HashMap<>();         
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs());   
                param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                param.put("limabelas",""+bucket[0][0]);  
                param.put("rata",""+Valid.SetAngka6(cnt[0]>0?sum[0]/cnt[0]:0));  
                param.put("tigapuluh",""+bucket[0][1]);  
                param.put("satujam",""+bucket[0][2]);  
                param.put("lebihsatujam",""+bucket[0][3]);  

                Sequel.queryu("delete from temporary_resep where temp37='"+akses.getalamatip()+"'");

                for(int i=0;i<tabMode.getRowCount();i++){  
                    Sequel.menyimpan("temporary_resep","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?",38,new String[]{
                        ""+i,tabMode.getValueAt(i,0).toString(),tabMode.getValueAt(i,1).toString(),tabMode.getValueAt(i,2).toString(),
                        tabMode.getValueAt(i,3).toString(),tabMode.getValueAt(i,4).toString(),tabMode.getValueAt(i,5).toString(),
                        tabMode.getValueAt(i,6).toString(),tabMode.getValueAt(i,7).toString(),tabMode.getValueAt(i,8).toString(),
                        tabMode.getValueAt(i,9).toString(),tabMode.getValueAt(i,10).toString(),"","","","","","","","","",
                        "","","","","","","","","","","","","","","","",akses.getalamatip()
                    });
                }
                Valid.MyReportqry("rptPelayananApotek.jasper","report","::[ Laporan Data Pelayanan Apotek ]::","select * from temporary_resep where temporary_resep.temp37='"+akses.getalamatip()+"' order by temporary_resep.no",param);
            }
            this.setCursor(Cursor.getDefaultCursor());
        }else{
            JOptionPane.showMessageDialog(null,"Masih proses menampilkan data, harap tunggu terlebih dahulu...!");
        }
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            //Valid.pindah(evt, BtnHapus, BtnAll);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnKeluar,TKd);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void tbBangsalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbBangsalMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbBangsalMouseClicked

    private void tbBangsalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbBangsalKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbBangsalKeyPressed

private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
       runBackground(() ->tampil());
}//GEN-LAST:event_BtnCariActionPerformed

private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            runBackground(() ->tampil());
            this.setCursor(Cursor.getDefaultCursor());
        }else{
            Valid.pindah(evt, TKd, BtnPrint);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void CmbKelengkapanActionPerformed(java.awt.event.ActionEvent evt) {
        if(resetCombo) return;
        // Hanya menyaring tabel berdasarkan kolom Keterangan, tanpa query ulang
        runBackground(() ->isiTabel());
    }

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
           TCari.setText("");
           filter="";
           resetCombo=true;
           CmbKelengkapan.setSelectedIndex(0);
           resetCombo=false;
           runBackground(() ->tampil());
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void MnResepRacikanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnResepRacikanActionPerformed
        filter="and resep_obat.no_resep in (select resep_dokter_racikan.no_resep from resep_dokter_racikan) ";
        runBackground(() ->tampil());
    }//GEN-LAST:event_MnResepRacikanActionPerformed

    private void MnResepNonRacikanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnResepNonRacikanActionPerformed
        filter="and resep_obat.no_resep not in (select resep_dokter_racikan.no_resep from resep_dokter_racikan) ";
        runBackground(() ->tampil());
    }//GEN-LAST:event_MnResepNonRacikanActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
            });
        }  
    }//GEN-LAST:event_formWindowOpened

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgPelayananApotek dialog = new DlgPelayananApotek(new javax.swing.JFrame(), true);
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
    private widget.Button BtnAll;
    private widget.Button BtnCari;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.ComboBox CmbKelengkapan;
    private widget.MenuItem MnResepNonRacikan;
    private widget.MenuItem MnResepRacikan;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.TextBox TKd;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel6;
    private widget.Label jLabel8;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.Label label11;
    private widget.Label label18;
    private widget.panelisi panelGlass5;
    private widget.Table tbBangsal;
    // End of variables declaration//GEN-END:variables

    /** Gabungkan tanggal + jam, null jika tanggal kosong/0000-00-00 atau format tidak valid. */
    private java.time.LocalDateTime gabungWaktu(String tgl,String jam,boolean jamNolDianggapKosong){
        if(tgl==null||jam==null) return null;
        tgl=tgl.trim();
        jam=jam.trim();
        if(tgl.length()<10||tgl.startsWith("0000-00-00")) return null;
        if(jamNolDianggapKosong&&jam.startsWith("00:00:00")) return null;
        try{
            if(jam.length()>8) jam=jam.substring(0,8);
            return java.time.LocalDateTime.parse(tgl.substring(0,10)+"T"+jam);
        }catch(Exception e){
            return null;
        }
    }

    /** Selisih menit (minimal 1, sama seperti rumus lama), null jika salah satu waktu kosong. */
    private Double selisihMenit(java.time.LocalDateTime awal,java.time.LocalDateTime akhir){
        if(awal==null||akhir==null) return null;
        long menit=java.time.Duration.between(awal,akhir).toMinutes();
        return (double)Math.max(1,menit);
    }

    private Object[] barisKosong(){
        Object[] r=new Object[JML_KOLOM];
        java.util.Arrays.fill(r,"");
        return r;
    }

    /** Baris ringkasan statistik: label di kolom 12, nilai di kolom 14-19. */
    private Object[] barisStatistik(String label,Object[] nilai){
        Object[] r=barisKosong();
        r[12]=label;
        r[13]=": ";
        for(int k=0;k<6;k++){
            r[14+k]=nilai[k];
        }
        return r;
    }

    /** Baris rekap: label di kolom Nama Pasien, nilai di kolom No Resep. */
    private Object[] barisRekap(String label,Object nilai){
        Object[] r=barisKosong();
        r[3]=label;
        r[4]=nilai;
        return r;
    }

    public void tampil(){        
        try{   
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            Valid.tabelKosong(tabMode);   
            semuaBaris.clear();

            for(int k=0;k<6;k++){
                sum[k]=0;
                cnt[k]=0;
                for(int b=0;b<4;b++){
                    bucket[k][b]=0;
                }
            }
            int totalResep=0,resepLengkap=0,belumValidasi=0,belumDiserahkan=0,obatSiapKosong=0;

            // Semua resep pada periode ikut tampil (tanpa syarat tanggal <> 0000-00-00).
            // Periode memakai tgl_peresepan karena selalu terisi untuk setiap resep.
            ps = koneksi.prepareStatement(
                "SELECT " +
                "resep_obat.no_resep, " +
                "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,reg_periksa.kd_pj," +
                "pasien.nm_pasien,dokter.nm_dokter,poliklinik.nm_poli,penjab.png_jawab," +
                "IF(resep_dokter_racikan.no_resep IS NULL,'2. Non Racikan','1. Racikan') AS jenis_resep," +
                "metode_racik.nm_racik," +
                "MIN(resep_obat.tgl_peresepan) AS tgl_peresepan," +
                "MIN(resep_obat.jam_peresepan) AS jam_peresepan," +
                "MIN(resep_obat.tgl_perawatan) AS tgl_perawatan," +
                "MIN(resep_obat.jam) AS jam," +
                "MAX(resep_obat.tgl_obat_siap) AS tgl_obat_siap," +
                "MAX(resep_obat.jam_obat_siap) AS jam_obat_siap," +
                "MAX(resep_obat.tgl_penyerahan) AS tgl_penyerahan," +
                "MAX(resep_obat.jam_penyerahan) AS jam_penyerahan " +
                "FROM reg_periksa " +
                "INNER JOIN dokter ON reg_periksa.kd_dokter=dokter.kd_dokter " +
                "INNER JOIN pasien ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "INNER JOIN poliklinik ON reg_periksa.kd_poli=poliklinik.kd_poli " +
                "INNER JOIN resep_obat ON reg_periksa.no_rawat=resep_obat.no_rawat " +
                "INNER JOIN penjab ON reg_periksa.kd_pj=penjab.kd_pj " +
                "LEFT JOIN resep_dokter_racikan ON resep_obat.no_resep=resep_dokter_racikan.no_resep " +
                "LEFT JOIN metode_racik ON resep_dokter_racikan.kd_racik=metode_racik.kd_racik " +
                "WHERE resep_obat.tgl_peresepan BETWEEN ? AND ? " +
                "AND (poliklinik.nm_poli LIKE ? " +
                "OR dokter.nm_dokter LIKE ? " +
                "OR penjab.png_jawab LIKE ? " +
                "OR reg_periksa.no_rkm_medis LIKE ? " +
                "OR pasien.nm_pasien LIKE ? " +
                "OR metode_racik.nm_racik LIKE ? " +
                "OR IF(resep_dokter_racikan.no_resep IS NULL,'2. Non Racikan','1. Racikan') LIKE ?) " +
                filter +
                "GROUP BY resep_obat.no_resep " +
                "ORDER BY tgl_peresepan, jam_peresepan"
            );

            ps.setString(1, Valid.SetTgl(Tgl1.getSelectedItem()+""));
            ps.setString(2, Valid.SetTgl(Tgl2.getSelectedItem()+""));
            for(int x=3;x<=9;x++){
                ps.setString(x,"%"+TCari.getText().trim()+"%");
            }

            rs = ps.executeQuery();

            while(rs.next()){
                java.time.LocalDateTime tResep=gabungWaktu(rs.getString("tgl_peresepan"),rs.getString("jam_peresepan"),false);
                java.time.LocalDateTime tValidasi=gabungWaktu(rs.getString("tgl_perawatan"),rs.getString("jam"),false);
                java.time.LocalDateTime tSiap=gabungWaktu(rs.getString("tgl_obat_siap"),rs.getString("jam_obat_siap"),true);
                java.time.LocalDateTime tSerah=gabungWaktu(rs.getString("tgl_penyerahan"),rs.getString("jam_penyerahan"),true);

                // Resep dianggap lengkap bila jam peresepan, validasi, dan penyerahan terisi.
                // Jam obat siap boleh kosong (hanya kolom durasi terkait yang tidak dihitung).
                java.util.List<String> kurang=new java.util.ArrayList<>();
                if(tValidasi==null){
                    kurang.add("Belum divalidasi");
                }
                if(tSerah==null){
                    kurang.add("Belum diserahkan");
                }
                if(tResep==null){
                    kurang.add("Jam peresepan kosong");
                }
                boolean lengkap=kurang.isEmpty();

                totalResep++;
                if(tValidasi==null){
                    belumValidasi++;
                }
                if(tSerah==null){
                    belumDiserahkan++;
                }
                if(tSiap==null){
                    obatSiapKosong++;
                }

                String keterangan;
                if(lengkap){
                    keterangan=(tSiap==null)?"Lengkap (jam obat siap kosong)":"Lengkap";
                    resepLengkap++;
                }else{
                    keterangan="Tidak lengkap: "+String.join(", ",kurang);
                }

                Double[] d=new Double[]{
                    selisihMenit(tResep,tValidasi),   // Peresepan-Validasi
                    selisihMenit(tResep,tSiap),       // Peresepan-Obat Siap
                    selisihMenit(tValidasi,tSiap),    // Validasi-Obat Siap
                    selisihMenit(tValidasi,tSerah),   // Validasi-Penyerahan
                    selisihMenit(tSiap,tSerah),       // Obat Siap-Penyerahan
                    selisihMenit(tResep,tSerah)       // Peresepan-Penyerahan
                };

                Object[] baris=new Object[]{
                    totalResep,
                    rs.getString("no_rawat"),
                    rs.getString("no_rkm_medis"),
                    rs.getString("nm_pasien"),
                    rs.getString("no_resep"),
                    rs.getString("nm_dokter"),
                    rs.getString("nm_poli"),
                    rs.getString("png_jawab"),
                    rs.getString("jenis_resep"),
                    rs.getString("nm_racik")==null?"-":rs.getString("nm_racik"),
                    tResep==null?"-":rs.getString("tgl_peresepan")+" "+rs.getString("jam_peresepan"),
                    tValidasi==null?"-":rs.getString("tgl_perawatan")+" "+rs.getString("jam"),
                    tSiap==null?"-":rs.getString("tgl_obat_siap")+" "+rs.getString("jam_obat_siap"),
                    tSerah==null?"-":rs.getString("tgl_penyerahan")+" "+rs.getString("jam_penyerahan"),
                    "-","-","-","-","-","-",
                    keterangan
                };

                // Durasi hanya ditampilkan dan dihitung untuk resep yang lengkap.
                if(lengkap){
                    for(int k=0;k<6;k++){
                        if(d[k]!=null){
                            baris[14+k]=d[k];
                            sum[k]+=d[k];
                            cnt[k]++;
                            if(d[k]<=15) bucket[k][0]++;
                            else if(d[k]<=30) bucket[k][1]++;
                            else if(d[k]<=60) bucket[k][2]++;
                            else bucket[k][3]++;
                        }
                    }
                }
                semuaBaris.add(baris);
            }

            if(totalResep>0){
                // Rekap kelengkapan resep
                int tidakLengkap=totalResep-resepLengkap;
                semuaBaris.add(barisKosong());
                semuaBaris.add(barisRekap("REKAP KELENGKAPAN RESEP",""));
                semuaBaris.add(barisRekap("Total Resep",totalResep));
                semuaBaris.add(barisRekap("Resep Lengkap (dihitung)",resepLengkap));
                semuaBaris.add(barisRekap("Resep Tidak Lengkap (tidak dihitung)",tidakLengkap));
                semuaBaris.add(barisRekap("   - Belum divalidasi",belumValidasi));
                semuaBaris.add(barisRekap("   - Belum diserahkan",belumDiserahkan));
                semuaBaris.add(barisRekap("Info: jam obat siap kosong",obatSiapKosong));

                // Hitungan (total, rata-rata, rentang menit) di paling bawah
                semuaBaris.add(barisKosong());
                Object[] nilaiTotal=new Object[6];
                Object[] nilaiRata=new Object[6];
                Object[] nilaiJumlah=new Object[6];
                for(int k=0;k<6;k++){
                    nilaiTotal[k]=Valid.SetAngka6(sum[k]);
                    nilaiRata[k]=Valid.SetAngka6(cnt[k]>0?sum[k]/cnt[k]:0);
                    nilaiJumlah[k]=cnt[k];
                }
                semuaBaris.add(barisStatistik("Total (Menit)",nilaiTotal));
                semuaBaris.add(barisStatistik("Rata-rata (Menit)",nilaiRata));
                semuaBaris.add(barisStatistik("Jumlah Resep Dihitung",nilaiJumlah));

                String[] labelBucket={"0 - 15 Menit",">15 - <=30 Menit",">30 - <=60 Menit",">60 Menit"};
                for(int b=0;b<4;b++){
                    Object[] nilaiBucket=new Object[6];
                    for(int k=0;k<6;k++){
                        nilaiBucket[k]=bucket[k][b];
                    }
                    semuaBaris.add(barisStatistik(labelBucket[b],nilaiBucket));
                }

            }
            isiTabel();
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }finally{
            try{
                if(rs!=null) rs.close();
                if(ps!=null) ps.close();
            }catch(Exception e){}
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    /** Isi tabel dari semuaBaris. Filter dropdown membaca kolom Keterangan (kolom ke-21):
     *  "Lengkap..." atau "Tidak lengkap...". Baris tanpa keterangan (rekap dan hitungan) selalu tampil. */
    private void isiTabel(){
        final int pilih=CmbKelengkapan.getSelectedIndex(); // 0=Semua, 1=Lengkap, 2=Tidak Lengkap
        Valid.tabelKosong(tabMode);
        for(Object[] baris:semuaBaris){
            Object ket=baris[JML_KOLOM-1];
            String k=(ket==null)?"":ket.toString();
            boolean tampilkan;
            if(pilih==0||k.isEmpty()){
                tampilkan=true;
            }else if(pilih==1){
                tampilkan=k.startsWith("Lengkap");
            }else{
                tampilkan=k.startsWith("Tidak lengkap");
            }
            if(tampilkan){
                tabMode.addRow(baris);
            }
        }
    }

    private void getData() {
        int row=tbBangsal.getSelectedRow();
        if(row!= -1){
            TKd.setText(tabMode.getValueAt(row,0).toString());
        }
    }

    private void runBackground(Runnable task) {
        if (ceksukses) return;
        if (executor.isShutdown() || executor.isTerminated()) return;
        if (!isDisplayable()) return;

        ceksukses = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    ceksukses = false;
                    SwingUtilities.invokeLater(() -> {
                        if (isDisplayable()) {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ex) {
            ceksukses = false;
        }
    }
    
    @Override
    public void dispose() {
        executor.shutdownNow();
        super.dispose();
    }
}
