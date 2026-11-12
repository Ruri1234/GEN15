/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rekammedis;

import fungsi.WarnaTableKasirRalan;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariPetugas;

/**
 *
 * @author perpustakaan
 */
public final class RMDataCatatanKeseimbanganCairan extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private int i=0;    
    private DlgCariPetugas petugas;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private String TANGGALMUNDUR="yes";
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public RMDataCatatanKeseimbanganCairan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        this.setLocation(8,1);
        setSize(628,674);

        tabMode=new DefaultTableModel(null,new Object[]{
            "No.Rawat","No.R.M.","Nama Pasien","Umur","JK","Tgl.Lahir","Tanggal","Jam","Infus",
            "Tranfusi","Minum","Selang Hidung","Urine","Selang Hidung","BAB","Drain Kiri","Drain Kanan","Mandi","Aktifitas","Lat. Nafas","Ganti Tenun","Tusukan Infus",
            "Keterangan","NIP","Nama Petugas"
        }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 25; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(160);
            }else if(i==3){
                column.setPreferredWidth(35);
            }else if(i==4){
                column.setPreferredWidth(20);
            }else if(i==5){
                column.setPreferredWidth(80);
            }else if(i==6){
                column.setPreferredWidth(65);
            }else if(i==7){
                column.setPreferredWidth(60);
            }else if(i==8){
                column.setPreferredWidth(50);
            }else if(i==9){
                column.setPreferredWidth(50);
            }else if(i==10){
                column.setPreferredWidth(50);
            }else if(i==11){
                column.setPreferredWidth(80);
            }else if(i==12){
                column.setPreferredWidth(80);
            }else if(i==13){
                column.setPreferredWidth(80);
            }else if(i==14){
                column.setPreferredWidth(50);
            }else if(i==15){
                column.setPreferredWidth(50);
            }else if(i==16){
                column.setPreferredWidth(80);
            }else if(i==17){
                column.setPreferredWidth(80);
            }else if(i==18){
                column.setPreferredWidth(80);
            }else if(i==19){
                column.setPreferredWidth(80);
            }else if(i==20){
                column.setPreferredWidth(90);
            }else if(i==21){
                column.setPreferredWidth(90);
            }else if(i==22){
                column.setPreferredWidth(200);
            }else if(i==23){
                column.setPreferredWidth(100);
            }else if(i==24){
                column.setPreferredWidth(180);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTableKasirRalan());

        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        NIP.setDocument(new batasInput((byte)20).getKata(NIP));
        Infus.setDocument(new batasInput((byte)4).getOnlyAngka(Infus));
        Tranfusi.setDocument(new batasInput((byte)4).getOnlyAngka(Tranfusi));
        Minum.setDocument(new batasInput((byte)4).getOnlyAngka(Minum));
        sh.setDocument(new batasInput((byte)10).getOnlyAngka(sh));
        Urine.setDocument(new batasInput((byte)4).getOnlyAngka(Urine));
        sh2.setDocument(new batasInput((byte)4).getOnlyAngka(sh2));
        bab.setDocument(new batasInput((byte)4).getOnlyAngka(bab));
        dll.setDocument(new batasInput((byte)4).getOnlyAngka(dll));
        Keterangan.setDocument(new batasInput((int)200).getKata(Keterangan));
        
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
        
        ChkInput.setSelected(false);
        isForm();
        jam();
        
        try {
            TANGGALMUNDUR=koneksiDB.TANGGALMUNDUR();
        } catch (Exception e) {
            TANGGALMUNDUR="yes";
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCatatanKeseimbanganCairan = new widget.MenuItem();
        JK = new widget.TextBox();
        Umur = new widget.TextBox();
        TanggalRegistrasi = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        FormInput = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        Tanggal = new widget.Tanggal();
        TNoRM = new widget.TextBox();
        jLabel16 = new widget.Label();
        Jam = new widget.ComboBox();
        Menit = new widget.ComboBox();
        Detik = new widget.ComboBox();
        ChkKejadian = new widget.CekBox();
        jLabel18 = new widget.Label();
        NIP = new widget.TextBox();
        NamaPetugas = new widget.TextBox();
        btnPetugas = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel17 = new widget.Label();
        jLabel12 = new widget.Label();
        Infus = new widget.TextBox();
        jLabel23 = new widget.Label();
        Tranfusi = new widget.TextBox();
        jLabel20 = new widget.Label();
        Minum = new widget.TextBox();
        jLabel30 = new widget.Label();
        sh = new widget.TextBox();
        jLabel25 = new widget.Label();
        jLabel13 = new widget.Label();
        Urine = new widget.TextBox();
        jLabel24 = new widget.Label();
        sh2 = new widget.TextBox();
        jLabel22 = new widget.Label();
        bab = new widget.TextBox();
        jLabel26 = new widget.Label();
        dll = new widget.TextBox();
        jLabel27 = new widget.Label();
        dll1 = new widget.TextBox();
        jLabel28 = new widget.Label();
        jLabel33 = new widget.Label();
        CmbMandi = new widget.ComboBox();
        jLabel34 = new widget.Label();
        CmbAktifitas = new widget.ComboBox();
        jLabel35 = new widget.Label();
        nafas = new widget.TextBox();
        jLabel36 = new widget.Label();
        CmbTenun = new widget.ComboBox();
        jLabel32 = new widget.Label();
        CmbTusuk = new widget.ComboBox();
        jLabel29 = new widget.Label();
        Keterangan = new widget.TextBox();
        jLabel38 = new widget.Label();
        jLabel39 = new widget.Label();
        jLabel40 = new widget.Label();
        jLabel41 = new widget.Label();
        jLabel42 = new widget.Label();
        jLabel43 = new widget.Label();
        jLabel44 = new widget.Label();
        jLabel45 = new widget.Label();
        jLabel46 = new widget.Label();
        jLabel47 = new widget.Label();
        jLabel48 = new widget.Label();
        jLabel49 = new widget.Label();
        jLabel50 = new widget.Label();
        jLabel51 = new widget.Label();
        jLabel52 = new widget.Label();
        jLabel53 = new widget.Label();
        jLabel56 = new widget.Label();
        jLabel55 = new widget.Label();
        jLabel54 = new widget.Label();
        jLabel57 = new widget.Label();
        jLabel58 = new widget.Label();
        jLabel59 = new widget.Label();
        jLabel60 = new widget.Label();
        ChkInput = new widget.CekBox();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnCatatanKeseimbanganCairan.setBackground(new java.awt.Color(255, 255, 254));
        MnCatatanKeseimbanganCairan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCatatanKeseimbanganCairan.setForeground(new java.awt.Color(50, 50, 50));
        MnCatatanKeseimbanganCairan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCatatanKeseimbanganCairan.setText("Formulir Catatan Keseimbangan Cairan");
        MnCatatanKeseimbanganCairan.setName("MnCatatanKeseimbanganCairan"); // NOI18N
        MnCatatanKeseimbanganCairan.setPreferredSize(new java.awt.Dimension(240, 26));
        MnCatatanKeseimbanganCairan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCatatanKeseimbanganCairanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCatatanKeseimbanganCairan);

        JK.setHighlighter(null);
        JK.setName("JK"); // NOI18N

        Umur.setHighlighter(null);
        Umur.setName("Umur"); // NOI18N

        TanggalRegistrasi.setHighlighter(null);
        TanggalRegistrasi.setName("TanggalRegistrasi"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Keseimbangan Cairan ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanActionPerformed(evt);
            }
        });
        BtnSimpan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal"); // NOI18N
        BtnBatal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBatalActionPerformed(evt);
            }
        });
        BtnBatal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnBatalKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnBatal);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusActionPerformed(evt);
            }
        });
        BtnHapus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnHapusKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnHapus);

        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnEdit.setMnemonic('G');
        BtnEdit.setText("Ganti");
        BtnEdit.setToolTipText("Alt+G");
        BtnEdit.setName("BtnEdit"); // NOI18N
        BtnEdit.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnEditActionPerformed(evt);
            }
        });
        BtnEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnEditKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnEdit);

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
        panelGlass8.add(BtnPrint);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass8.add(LCount);

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
        panelGlass8.add(BtnKeluar);

        jPanel3.add(panelGlass8, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tanggal :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "24-04-2026" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "24-04-2026" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(310, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('3');
        BtnCari.setToolTipText("Alt+3");
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
        panelGlass9.add(BtnCari);

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
        panelGlass9.add(BtnAll);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 174));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setBackground(new java.awt.Color(250, 255, 245));
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 225));
        FormInput.setLayout(null);

        jLabel4.setText("No.Rawat :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 70, 23);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(74, 10, 136, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TPasienKeyPressed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(326, 10, 295, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "24-04-2026" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        FormInput.add(Tanggal);
        Tanggal.setBounds(74, 40, 90, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(212, 10, 112, 23);

        jLabel16.setText("Tanggal :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel16);
        jLabel16.setBounds(0, 40, 70, 23);

        Jam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        Jam.setName("Jam"); // NOI18N
        Jam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JamKeyPressed(evt);
            }
        });
        FormInput.add(Jam);
        Jam.setBounds(168, 40, 62, 23);

        Menit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Menit.setName("Menit"); // NOI18N
        Menit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MenitKeyPressed(evt);
            }
        });
        FormInput.add(Menit);
        Menit.setBounds(233, 40, 62, 23);

        Detik.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Detik.setName("Detik"); // NOI18N
        Detik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DetikKeyPressed(evt);
            }
        });
        FormInput.add(Detik);
        Detik.setBounds(298, 40, 62, 23);

        ChkKejadian.setBorder(null);
        ChkKejadian.setSelected(true);
        ChkKejadian.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ChkKejadian.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setName("ChkKejadian"); // NOI18N
        FormInput.add(ChkKejadian);
        ChkKejadian.setBounds(363, 40, 23, 23);

        jLabel18.setText("Petugas :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(400, 40, 70, 23);

        NIP.setEditable(false);
        NIP.setHighlighter(null);
        NIP.setName("NIP"); // NOI18N
        NIP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NIPKeyPressed(evt);
            }
        });
        FormInput.add(NIP);
        NIP.setBounds(474, 40, 94, 23);

        NamaPetugas.setEditable(false);
        NamaPetugas.setName("NamaPetugas"); // NOI18N
        FormInput.add(NamaPetugas);
        NamaPetugas.setBounds(570, 40, 187, 23);

        btnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPetugas.setMnemonic('2');
        btnPetugas.setToolTipText("ALt+2");
        btnPetugas.setName("btnPetugas"); // NOI18N
        btnPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPetugasActionPerformed(evt);
            }
        });
        btnPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnPetugasKeyPressed(evt);
            }
        });
        FormInput.add(btnPetugas);
        btnPetugas.setBounds(761, 40, 28, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(625, 10, 60, 23);

        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(689, 10, 100, 23);

        jSeparator2.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator2.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator2.setName("jSeparator2"); // NOI18N
        FormInput.add(jSeparator2);
        jSeparator2.setBounds(0, 70, 800, 1);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel17.setText("INPUT :");
        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N
        jLabel17.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel17);
        jLabel17.setBounds(14, 70, 70, 23);

        jLabel12.setText("Infus :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(0, 90, 62, 23);

        Infus.setFocusTraversalPolicyProvider(true);
        Infus.setName("Infus"); // NOI18N
        Infus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                InfusKeyPressed(evt);
            }
        });
        FormInput.add(Infus);
        Infus.setBounds(66, 90, 70, 23);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel23.setText("Tranfusi :");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(160, 90, 60, 23);

        Tranfusi.setFocusTraversalPolicyProvider(true);
        Tranfusi.setName("Tranfusi"); // NOI18N
        Tranfusi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TranfusiKeyPressed(evt);
            }
        });
        FormInput.add(Tranfusi);
        Tranfusi.setBounds(210, 90, 100, 23);

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel20.setText("Minum :");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(335, 90, 50, 23);

        Minum.setFocusTraversalPolicyProvider(true);
        Minum.setName("Minum"); // NOI18N
        Minum.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MinumKeyPressed(evt);
            }
        });
        FormInput.add(Minum);
        Minum.setBounds(375, 90, 70, 23);

        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel30.setText("Selang Hidung :");
        jLabel30.setName("jLabel30"); // NOI18N
        jLabel30.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel30);
        jLabel30.setBounds(470, 90, 90, 23);

        sh.setFocusTraversalPolicyProvider(true);
        sh.setName("sh"); // NOI18N
        sh.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                shKeyPressed(evt);
            }
        });
        FormInput.add(sh);
        sh.setBounds(550, 90, 70, 23);

        jLabel25.setForeground(new java.awt.Color(0, 0, 0));
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel25.setText("OUTPUT :");
        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel25.setName("jLabel25"); // NOI18N
        jLabel25.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel25);
        jLabel25.setBounds(14, 125, 80, 23);

        jLabel13.setForeground(new java.awt.Color(0, 0, 0));
        jLabel13.setText("Urine :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(10, 145, 50, 23);

        Urine.setFocusTraversalPolicyProvider(true);
        Urine.setName("Urine"); // NOI18N
        Urine.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                UrineKeyPressed(evt);
            }
        });
        FormInput.add(Urine);
        Urine.setBounds(65, 145, 70, 23);

        jLabel24.setForeground(new java.awt.Color(0, 0, 0));
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel24.setText("Selang Hidung :");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(160, 145, 80, 23);

        sh2.setFocusTraversalPolicyProvider(true);
        sh2.setName("sh2"); // NOI18N
        sh2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sh2KeyPressed(evt);
            }
        });
        FormInput.add(sh2);
        sh2.setBounds(240, 145, 70, 23);

        jLabel22.setForeground(new java.awt.Color(0, 0, 0));
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel22.setText("BAB :");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(335, 145, 40, 23);

        bab.setFocusTraversalPolicyProvider(true);
        bab.setName("bab"); // NOI18N
        bab.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                babKeyPressed(evt);
            }
        });
        FormInput.add(bab);
        bab.setBounds(365, 145, 80, 23);

        jLabel26.setForeground(new java.awt.Color(0, 0, 0));
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel26.setText("Drain Kiri :");
        jLabel26.setName("jLabel26"); // NOI18N
        FormInput.add(jLabel26);
        jLabel26.setBounds(470, 145, 50, 23);

        dll.setFocusTraversalPolicyProvider(true);
        dll.setName("dll"); // NOI18N
        dll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                dllKeyPressed(evt);
            }
        });
        FormInput.add(dll);
        dll.setBounds(525, 145, 70, 23);

        jLabel27.setForeground(new java.awt.Color(0, 0, 0));
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel27.setText("Drain Kanan :");
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(610, 145, 70, 23);

        dll1.setFocusTraversalPolicyProvider(true);
        dll1.setName("dll1"); // NOI18N
        dll1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                dll1KeyPressed(evt);
            }
        });
        FormInput.add(dll1);
        dll1.setBounds(680, 145, 70, 23);

        jLabel28.setForeground(new java.awt.Color(0, 0, 0));
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel28.setText("ADL :");
        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel28.setName("jLabel28"); // NOI18N
        jLabel28.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel28);
        jLabel28.setBounds(14, 180, 70, 23);

        jLabel33.setForeground(new java.awt.Color(0, 0, 0));
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel33.setText("Mandi :");
        jLabel33.setName("jLabel33"); // NOI18N
        jLabel33.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel33);
        jLabel33.setBounds(30, 200, 40, 23);

        CmbMandi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TT", "KM", "S", "P", "K" }));
        CmbMandi.setName("CmbMandi"); // NOI18N
        CmbMandi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CmbMandiKeyPressed(evt);
            }
        });
        FormInput.add(CmbMandi);
        CmbMandi.setBounds(70, 200, 60, 23);

        jLabel34.setForeground(new java.awt.Color(0, 0, 0));
        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel34.setText("Aktifitas :");
        jLabel34.setName("jLabel34"); // NOI18N
        jLabel34.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel34);
        jLabel34.setBounds(150, 200, 50, 23);

        CmbAktifitas.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "R", "ROM", "JJ", "MM", "D" }));
        CmbAktifitas.setName("CmbAktifitas"); // NOI18N
        CmbAktifitas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CmbAktifitasKeyPressed(evt);
            }
        });
        FormInput.add(CmbAktifitas);
        CmbAktifitas.setBounds(200, 200, 60, 23);

        jLabel35.setForeground(new java.awt.Color(0, 0, 0));
        jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel35.setText("Lat. Nafas :");
        jLabel35.setName("jLabel35"); // NOI18N
        jLabel35.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel35);
        jLabel35.setBounds(280, 200, 60, 23);

        nafas.setFocusTraversalPolicyProvider(true);
        nafas.setName("nafas"); // NOI18N
        nafas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                nafasKeyPressed(evt);
            }
        });
        FormInput.add(nafas);
        nafas.setBounds(340, 200, 80, 23);

        jLabel36.setForeground(new java.awt.Color(0, 0, 0));
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel36.setText("Ganti Tenun :");
        jLabel36.setName("jLabel36"); // NOI18N
        jLabel36.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel36);
        jLabel36.setBounds(440, 200, 70, 23);

        CmbTenun.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "T", "1/2", "R" }));
        CmbTenun.setName("CmbTenun"); // NOI18N
        CmbTenun.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CmbTenunKeyPressed(evt);
            }
        });
        FormInput.add(CmbTenun);
        CmbTenun.setBounds(510, 200, 60, 23);

        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel32.setText("Tusukan Infus :");
        jLabel32.setName("jLabel32"); // NOI18N
        jLabel32.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel32);
        jLabel32.setBounds(610, 200, 80, 23);

        CmbTusuk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DBN", "P", "G" }));
        CmbTusuk.setName("CmbTusuk"); // NOI18N
        CmbTusuk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CmbTusukActionPerformed(evt);
            }
        });
        CmbTusuk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CmbTusukKeyPressed(evt);
            }
        });
        FormInput.add(CmbTusuk);
        CmbTusuk.setBounds(690, 200, 70, 23);

        jLabel29.setText("Keterangan :");
        jLabel29.setName("jLabel29"); // NOI18N
        FormInput.add(jLabel29);
        jLabel29.setBounds(10, 240, 80, 23);

        Keterangan.setFocusTraversalPolicyProvider(true);
        Keterangan.setName("Keterangan"); // NOI18N
        Keterangan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganKeyPressed(evt);
            }
        });
        FormInput.add(Keterangan);
        Keterangan.setBounds(100, 240, 670, 23);

        jLabel38.setForeground(new java.awt.Color(255, 102, 102));
        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel38.setText("Tusukan Infus");
        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel38.setName("jLabel38"); // NOI18N
        jLabel38.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel38);
        jLabel38.setBounds(30, 275, 90, 23);

        jLabel39.setForeground(new java.awt.Color(0, 0, 0));
        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel39.setText("DBN : Dalam batas normal");
        jLabel39.setName("jLabel39"); // NOI18N
        jLabel39.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel39);
        jLabel39.setBounds(30, 290, 140, 23);

        jLabel40.setForeground(new java.awt.Color(0, 0, 0));
        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel40.setText("P : Perlu Diganti");
        jLabel40.setName("jLabel40"); // NOI18N
        jLabel40.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel40);
        jLabel40.setBounds(30, 305, 140, 23);

        jLabel41.setForeground(new java.awt.Color(0, 0, 0));
        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel41.setText("G : Sudah diganti");
        jLabel41.setName("jLabel41"); // NOI18N
        jLabel41.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel41);
        jLabel41.setBounds(30, 320, 140, 23);

        jLabel42.setForeground(new java.awt.Color(255, 102, 102));
        jLabel42.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel42.setText("Mandi");
        jLabel42.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel42.setName("jLabel42"); // NOI18N
        jLabel42.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel42);
        jLabel42.setBounds(180, 275, 90, 23);

        jLabel43.setForeground(new java.awt.Color(0, 0, 0));
        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel43.setText("TT : Di Tempat Tidur");
        jLabel43.setName("jLabel43"); // NOI18N
        jLabel43.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel43);
        jLabel43.setBounds(180, 290, 140, 23);

        jLabel44.setForeground(new java.awt.Color(0, 0, 0));
        jLabel44.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel44.setText("KM : Di Kamar Mandi");
        jLabel44.setName("jLabel44"); // NOI18N
        jLabel44.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel44);
        jLabel44.setBounds(180, 305, 140, 23);

        jLabel45.setForeground(new java.awt.Color(0, 0, 0));
        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel45.setText("S : Klien Sendiri");
        jLabel45.setName("jLabel45"); // NOI18N
        jLabel45.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel45);
        jLabel45.setBounds(180, 320, 140, 23);

        jLabel46.setForeground(new java.awt.Color(0, 0, 0));
        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel46.setText("P : Oleh Perawat");
        jLabel46.setName("jLabel46"); // NOI18N
        jLabel46.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel46);
        jLabel46.setBounds(180, 335, 140, 23);

        jLabel47.setForeground(new java.awt.Color(0, 0, 0));
        jLabel47.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel47.setText("K : Oleh Keluarga");
        jLabel47.setName("jLabel47"); // NOI18N
        jLabel47.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel47);
        jLabel47.setBounds(180, 350, 140, 23);

        jLabel48.setForeground(new java.awt.Color(255, 102, 102));
        jLabel48.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel48.setText("Aktifitas");
        jLabel48.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel48.setName("jLabel48"); // NOI18N
        jLabel48.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel48);
        jLabel48.setBounds(300, 275, 90, 23);

        jLabel49.setForeground(new java.awt.Color(0, 0, 0));
        jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel49.setText("R : Berbaring Rata");
        jLabel49.setName("jLabel49"); // NOI18N
        jLabel49.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel49);
        jLabel49.setBounds(300, 290, 140, 23);

        jLabel50.setForeground(new java.awt.Color(0, 0, 0));
        jLabel50.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel50.setText("ROM : Latihan Pasif");
        jLabel50.setName("jLabel50"); // NOI18N
        jLabel50.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel50);
        jLabel50.setBounds(300, 305, 140, 23);

        jLabel51.setForeground(new java.awt.Color(0, 0, 0));
        jLabel51.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel51.setText("JJ : Jalan-jalan");
        jLabel51.setName("jLabel51"); // NOI18N
        jLabel51.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel51);
        jLabel51.setBounds(300, 320, 140, 23);

        jLabel52.setForeground(new java.awt.Color(0, 0, 0));
        jLabel52.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel52.setText("MM : Miring Kiri-Miring Kanan");
        jLabel52.setName("jLabel52"); // NOI18N
        jLabel52.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel52);
        jLabel52.setBounds(300, 335, 140, 23);

        jLabel53.setForeground(new java.awt.Color(0, 0, 0));
        jLabel53.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel53.setText("D : Duduk di Kursi");
        jLabel53.setName("jLabel53"); // NOI18N
        jLabel53.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel53);
        jLabel53.setBounds(300, 350, 140, 23);

        jLabel56.setForeground(new java.awt.Color(255, 102, 102));
        jLabel56.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel56.setText("Latihan Pernafasan");
        jLabel56.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel56.setName("jLabel56"); // NOI18N
        jLabel56.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel56);
        jLabel56.setBounds(460, 275, 120, 23);

        jLabel55.setForeground(new java.awt.Color(0, 0, 0));
        jLabel55.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel55.setText("Kalau ada, tulis berapa kali dibuat");
        jLabel55.setName("jLabel55"); // NOI18N
        jLabel55.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel55);
        jLabel55.setBounds(460, 290, 170, 23);

        jLabel54.setForeground(new java.awt.Color(0, 0, 0));
        jLabel54.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel54.setText("0 : Tidak ada");
        jLabel54.setName("jLabel54"); // NOI18N
        jLabel54.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel54);
        jLabel54.setBounds(460, 305, 140, 23);

        jLabel57.setForeground(new java.awt.Color(255, 102, 102));
        jLabel57.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel57.setText("Mengganti Alat Tenun");
        jLabel57.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel57.setName("jLabel57"); // NOI18N
        jLabel57.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel57);
        jLabel57.setBounds(650, 275, 140, 23);

        jLabel58.setForeground(new java.awt.Color(0, 0, 0));
        jLabel58.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel58.setText("T : Ganti Total");
        jLabel58.setName("jLabel58"); // NOI18N
        jLabel58.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel58);
        jLabel58.setBounds(650, 290, 170, 23);

        jLabel59.setForeground(new java.awt.Color(0, 0, 0));
        jLabel59.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel59.setText("1/2 : Ganti Setengah");
        jLabel59.setName("jLabel59"); // NOI18N
        jLabel59.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel59);
        jLabel59.setBounds(650, 305, 140, 23);

        jLabel60.setForeground(new java.awt.Color(0, 0, 0));
        jLabel60.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel60.setText("R : Dirapikan saja");
        jLabel60.setName("jLabel60"); // NOI18N
        jLabel60.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel60);
        jLabel60.setBounds(650, 320, 140, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('I');
        ChkInput.setText(".: Input Data");
        ChkInput.setToolTipText("Alt+I");
        ChkInput.setBorderPainted(true);
        ChkInput.setBorderPaintedFlat(true);
        ChkInput.setFocusable(false);
        ChkInput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput.setName("ChkInput"); // NOI18N
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInputActionPerformed(evt);
            }
        });
        PanelInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
        }else{            
            Valid.pindah(evt,TCari,Tanggal);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void TPasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TPasienKeyPressed
        Valid.pindah(evt,TCari,BtnSimpan);
}//GEN-LAST:event_TPasienKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"pasien");
        }else if(NIP.getText().trim().equals("")||NamaPetugas.getText().trim().equals("")){
            Valid.textKosong(NIP,"Petugas");
        }else if(Infus.getText().trim().equals("")){
            Valid.textKosong(Infus,"Infus");
        }else if(Tranfusi.getText().trim().equals("")){
            Valid.textKosong(Tranfusi,"Tranfusi");
        }else if(Minum.getText().trim().equals("")){
            Valid.textKosong(Minum,"Minum");
        }else if(sh.getText().trim().equals("")){
            Valid.textKosong(sh,"Selang Hidung");
        }else if(Urine.getText().trim().equals("")){
            Valid.textKosong(Urine,"Urine/Feses");
        }else if(sh2.getText().trim().equals("")){
            Valid.textKosong(sh2,"Selang Hidung");
        }else if(bab.getText().trim().equals("")){
            Valid.textKosong(bab,"BAB");
        }else if(dll.getText().trim().equals("")){
            Valid.textKosong(dll,"Drain Kiri");
        }else if(dll1.getText().trim().equals("")){
            Valid.textKosong(dll1,"Drain Kanan");
        }else{
            if(akses.getkode().equals("Admin Utama")){
                simpan();
            }else{
                if(TanggalRegistrasi.getText().equals("")){
                    TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                }
                if(Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem())==true){
                    simpan();
                }
            }
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,Keterangan,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
        ChkInput.setSelected(true);
        isForm(); 
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(tbObat.getSelectedRow()>-1){
            if(akses.getkode().equals("Admin Utama")){
                hapus();
            }else{
                if(NIP.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),23).toString())){
                    if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString()+" "+tbObat.getValueAt(tbObat.getSelectedRow(),7).toString(),Sequel.ambiltanggalsekarang())==true){
                        hapus();
                    }
                }else{
                    JOptionPane.showMessageDialog(null,"Hanya bisa dihapus oleh petugas yang bersangkutan..!!");
                }
            }
        }else{
            JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
        }   
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"pasien");
        }else if(NIP.getText().trim().equals("")||NamaPetugas.getText().trim().equals("")){
            Valid.textKosong(NIP,"Petugas");
        }else if(Infus.getText().trim().equals("")){
            Valid.textKosong(Infus,"Infus");
        }else if(Tranfusi.getText().trim().equals("")){
            Valid.textKosong(Tranfusi,"Tranfusi");
        }else if(Minum.getText().trim().equals("")){
            Valid.textKosong(Minum,"Minum");
        }else if(sh.getText().trim().equals("")){
            Valid.textKosong(sh,"Selang Hidung");
        }else if(Urine.getText().trim().equals("")){
            Valid.textKosong(Urine,"Urine/Feses");
        }else if(sh2.getText().trim().equals("")){
            Valid.textKosong(sh2,"Selang Hidung");
        }else if(bab.getText().trim().equals("")){
            Valid.textKosong(bab,"BAB");
        }else if(dll.getText().trim().equals("")){
            Valid.textKosong(dll,"Drain Kiri");
        }else if(dll1.getText().trim().equals("")){
            Valid.textKosong(dll1,"Drain Kanan");
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(akses.getkode().equals("Admin Utama")){
                    ganti();
                }else{
                    if(NIP.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),23).toString())){
                        if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString()+" "+tbObat.getValueAt(tbObat.getSelectedRow(),7).toString(),Sequel.ambiltanggalsekarang())==true){
                            if(TanggalRegistrasi.getText().equals("")){
                                TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                            }
                            if(Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem())==true){
                                ganti();
                            }
                        }
                    }else{
                        JOptionPane.showMessageDialog(null,"Hanya bisa diganti oleh petugas yang bersangkutan..!!");
                    }
                }
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
            }
        }
}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnPrint);
        }
}//GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnKeluarActionPerformed(null);
        }else{Valid.pindah(evt,BtnEdit,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        }else if(tabMode.getRowCount()!=0){
            Map<String, Object> param = new HashMap<>(); 
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());   
            param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
            
            if(TCari.getText().trim().equals("")){
                Valid.MyReportqry("rptDataCatatanKeseimbanganCairan.jasper","report","::[ Data Catatan Keseimbangan Cairan ]::",
                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"+
                    "pasien.jk,pasien.tgl_lahir,catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat,catatan_keseimbangan_cairan.infus,"+
                    "catatan_keseimbangan_cairan.tranfusi,catatan_keseimbangan_cairan.minum,catatan_keseimbangan_cairan.sh,catatan_keseimbangan_cairan.urine,catatan_keseimbangan_cairan.drain,"+
                    "catatan_keseimbangan_cairan.ngt,catatan_keseimbangan_cairan.iwl,catatan_keseimbangan_cairan.iwl2,catatan_keseimbangan_cairan.mandi,catatan_keseimbangan_cairan.aktifitas,catatan_keseimbangan_cairan.nafas,catatan_keseimbangan_cairan.tenun,catatan_keseimbangan_cairan.tusuk,catatan_keseimbangan_cairan.keterangan,"+
                    "catatan_keseimbangan_cairan.nip,petugas.nama "+
                    "from catatan_keseimbangan_cairan inner join reg_periksa on catatan_keseimbangan_cairan.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join petugas on catatan_keseimbangan_cairan.nip=petugas.nip where "+
                    "catatan_keseimbangan_cairan.tgl_perawatan between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' order by catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat",param);
            }else{
                Valid.MyReportqry("rptDataCatatanKeseimbanganCairan.jasper","report","::[ Data Catatan Keseimbangan Cairan ]::",
                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"+
                    "pasien.jk,pasien.tgl_lahir,catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat,catatan_keseimbangan_cairan.infus,"+
                    "catatan_keseimbangan_cairan.tranfusi,catatan_keseimbangan_cairan.minum,catatan_keseimbangan_cairan.sh,catatan_keseimbangan_cairan.urine,catatan_keseimbangan_cairan.drain,"+
                   "catatan_keseimbangan_cairan.ngt,catatan_keseimbangan_cairan.iwl,catatan_keseimbangan_cairan.iwl2,catatan_keseimbangan_cairan.mandi,catatan_keseimbangan_cairan.aktifitas,catatan_keseimbangan_cairan.nafas,catatan_keseimbangan_cairan.tenun,catatan_keseimbangan_cairan.tusuk,catatan_keseimbangan_cairan.keterangan,"+
                    "catatan_keseimbangan_cairan.nip,petugas.nama "+
                    "from catatan_keseimbangan_cairan inner join reg_periksa on catatan_keseimbangan_cairan.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join petugas on catatan_keseimbangan_cairan.nip=petugas.nip where "+
                    "catatan_keseimbangan_cairan.tgl_perawatan between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' and "+
                    "(reg_periksa.no_rawat like '%"+TCari.getText().trim()+"%' or pasien.no_rkm_medis like '%"+TCari.getText().trim()+"%' or "+
                    "pasien.nm_pasien like '%"+TCari.getText().trim()+"%' or catatan_keseimbangan_cairan.nip like '%"+TCari.getText().trim()+"%' or petugas.nama like '%"+TCari.getText().trim()+"%') "+
                    "order by catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat ",param);
            }  
        }
        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        runBackground(() ->tampil());
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        runBackground(() ->tampil());
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            TCari.setText("");
            runBackground(() ->tampil());
        }else{
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah(evt,TCari,Jam);
}//GEN-LAST:event_TanggalKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void JamKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JamKeyPressed
        Valid.pindah(evt,Tanggal,Menit);
    }//GEN-LAST:event_JamKeyPressed

    private void MenitKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MenitKeyPressed
        Valid.pindah(evt,Jam,Detik);
    }//GEN-LAST:event_MenitKeyPressed

    private void DetikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DetikKeyPressed
        Valid.pindah(evt,Menit,btnPetugas);
    }//GEN-LAST:event_DetikKeyPressed

    private void NIPKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NIPKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            NamaPetugas.setText(Sequel.CariPetugas(NIP.getText()));
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Detik.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            Infus.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            btnPetugasActionPerformed(null);
        }
    }//GEN-LAST:event_NIPKeyPressed

    private void btnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugasActionPerformed
        if (petugas == null || !petugas.isDisplayable()) {
            petugas=new DlgCariPetugas(null,false);
            petugas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            petugas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(petugas!=null && petugas.getTable().getSelectedRow()!= -1){
                        NIP.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                        NamaPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());
                    }
                    NIP.requestFocus();
                    petugas=null;
                }
            });
            petugas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            petugas.setLocationRelativeTo(internalFrame1);
        }

        if (petugas == null) return;
        if (!petugas.isVisible()) {
            petugas.isCek();
            petugas.emptTeks();
        }
        if (petugas.isVisible()) {
            petugas.toFront();
            return;
        }
        petugas.setVisible(true);
    }//GEN-LAST:event_btnPetugasActionPerformed

    private void btnPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPetugasKeyPressed
        Valid.pindah(evt,Detik,Infus);
    }//GEN-LAST:event_btnPetugasKeyPressed

    private void MnCatatanKeseimbanganCairanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCatatanKeseimbanganCairanActionPerformed
        if(tbObat.getSelectedRow()>-1){
            Map<String, Object> param = new HashMap<>();
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());   
            param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
            Valid.MyReportqry("rptFormulirCatatanKeseimbanganCairan.jasper","report","::[ Formulir Catatan Keseimbangan Cairan ]::",
                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,reg_periksa.tgl_registrasi,reg_periksa.jam_reg,"+
                    "pasien.jk,pasien.tgl_lahir,catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat,catatan_keseimbangan_cairan.infus,"+
                     "catatan_keseimbangan_cairan.tranfusi,catatan_keseimbangan_cairan.minum,catatan_keseimbangan_cairan.sh,catatan_keseimbangan_cairan.urine,catatan_keseimbangan_cairan.drain,"+
                     "catatan_keseimbangan_cairan.ngt,catatan_keseimbangan_cairan.iwl,catatan_keseimbangan_cairan.iwl2,catatan_keseimbangan_cairan.mandi,catatan_keseimbangan_cairan.aktifitas,catatan_keseimbangan_cairan.nafas,catatan_keseimbangan_cairan.tenun,catatan_keseimbangan_cairan.tusuk,catatan_keseimbangan_cairan.keterangan,"+
                    "catatan_keseimbangan_cairan.nip,petugas.nama "+
                    "from catatan_keseimbangan_cairan inner join reg_periksa on catatan_keseimbangan_cairan.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join petugas on catatan_keseimbangan_cairan.nip=petugas.nip where reg_periksa.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"' order by catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat",param);
        }
    }//GEN-LAST:event_MnCatatanKeseimbanganCairanActionPerformed

    private void InfusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_InfusKeyPressed

    }//GEN-LAST:event_InfusKeyPressed

    private void TranfusiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TranfusiKeyPressed

    }//GEN-LAST:event_TranfusiKeyPressed

    private void MinumKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MinumKeyPressed

    }//GEN-LAST:event_MinumKeyPressed

    private void shKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_shKeyPressed

    }//GEN-LAST:event_shKeyPressed

    private void UrineKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_UrineKeyPressed

    }//GEN-LAST:event_UrineKeyPressed

    private void sh2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sh2KeyPressed

    }//GEN-LAST:event_sh2KeyPressed

    private void babKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_babKeyPressed

    }//GEN-LAST:event_babKeyPressed

    private void dllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dllKeyPressed

    }//GEN-LAST:event_dllKeyPressed

    private void dll1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dll1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_dll1KeyPressed

    private void CmbMandiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CmbMandiKeyPressed
        Valid.pindah(evt,CmbTusuk,BtnSimpan);
    }//GEN-LAST:event_CmbMandiKeyPressed

    private void CmbAktifitasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CmbAktifitasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_CmbAktifitasKeyPressed

    private void nafasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nafasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_nafasKeyPressed

    private void CmbTenunKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CmbTenunKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_CmbTenunKeyPressed

    private void CmbTusukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CmbTusukActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CmbTusukActionPerformed

    private void CmbTusukKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CmbTusukKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_CmbTusukKeyPressed

    private void KeteranganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganKeyPressed
        Valid.pindah(evt,CmbMandi,BtnSimpan);
    }//GEN-LAST:event_KeteranganKeyPressed

    
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMDataCatatanKeseimbanganCairan dialog = new RMDataCatatanKeseimbanganCairan(new javax.swing.JFrame(), true);
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
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkInput;
    private widget.CekBox ChkKejadian;
    private widget.ComboBox CmbAktifitas;
    private widget.ComboBox CmbMandi;
    private widget.ComboBox CmbTenun;
    private widget.ComboBox CmbTusuk;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.ComboBox Detik;
    private widget.PanelBiasa FormInput;
    private widget.TextBox Infus;
    private widget.TextBox JK;
    private widget.ComboBox Jam;
    private widget.TextBox Keterangan;
    private widget.Label LCount;
    private widget.ComboBox Menit;
    private widget.TextBox Minum;
    private widget.MenuItem MnCatatanKeseimbanganCairan;
    private widget.TextBox NIP;
    private widget.TextBox NamaPetugas;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.Tanggal Tanggal;
    private widget.TextBox TanggalRegistrasi;
    private widget.TextBox TglLahir;
    private widget.TextBox Tranfusi;
    private widget.TextBox Umur;
    private widget.TextBox Urine;
    private widget.TextBox bab;
    private widget.Button btnPetugas;
    private widget.TextBox dll;
    private widget.TextBox dll1;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel21;
    private widget.Label jLabel22;
    private widget.Label jLabel23;
    private widget.Label jLabel24;
    private widget.Label jLabel25;
    private widget.Label jLabel26;
    private widget.Label jLabel27;
    private widget.Label jLabel28;
    private widget.Label jLabel29;
    private widget.Label jLabel30;
    private widget.Label jLabel32;
    private widget.Label jLabel33;
    private widget.Label jLabel34;
    private widget.Label jLabel35;
    private widget.Label jLabel36;
    private widget.Label jLabel38;
    private widget.Label jLabel39;
    private widget.Label jLabel4;
    private widget.Label jLabel40;
    private widget.Label jLabel41;
    private widget.Label jLabel42;
    private widget.Label jLabel43;
    private widget.Label jLabel44;
    private widget.Label jLabel45;
    private widget.Label jLabel46;
    private widget.Label jLabel47;
    private widget.Label jLabel48;
    private widget.Label jLabel49;
    private widget.Label jLabel50;
    private widget.Label jLabel51;
    private widget.Label jLabel52;
    private widget.Label jLabel53;
    private widget.Label jLabel54;
    private widget.Label jLabel55;
    private widget.Label jLabel56;
    private widget.Label jLabel57;
    private widget.Label jLabel58;
    private widget.Label jLabel59;
    private widget.Label jLabel6;
    private widget.Label jLabel60;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JSeparator jSeparator2;
    private widget.TextBox nafas;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.TextBox sh;
    private widget.TextBox sh2;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables
    
    public synchronized void tampil() {
        final java.util.List<Object[]> data=new java.util.ArrayList<>();
        PreparedStatement ps=null;
        ResultSet rs=null;
        try{
            if(TCari.getText().trim().equals("")){
                ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"+
                    "pasien.jk,pasien.tgl_lahir,catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat,catatan_keseimbangan_cairan.infus,"+
                    "catatan_keseimbangan_cairan.tranfusi,catatan_keseimbangan_cairan.minum,catatan_keseimbangan_cairan.sh,catatan_keseimbangan_cairan.urine,catatan_keseimbangan_cairan.drain,"+
                    "catatan_keseimbangan_cairan.ngt,catatan_keseimbangan_cairan.iwl,catatan_keseimbangan_cairan.iwl2,catatan_keseimbangan_cairan.mandi,catatan_keseimbangan_cairan.aktifitas,catatan_keseimbangan_cairan.nafas,catatan_keseimbangan_cairan.tenun,catatan_keseimbangan_cairan.tusuk,catatan_keseimbangan_cairan.keterangan,"+
                    "catatan_keseimbangan_cairan.nip,petugas.nama "+
                    "from catatan_keseimbangan_cairan inner join reg_periksa on catatan_keseimbangan_cairan.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join petugas on catatan_keseimbangan_cairan.nip=petugas.nip where "+
                    "catatan_keseimbangan_cairan.tgl_perawatan between ? and ? order by catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat");
            }else{
                ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"+
                    "pasien.jk,pasien.tgl_lahir,catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat,catatan_keseimbangan_cairan.infus,"+
                    "catatan_keseimbangan_cairan.tranfusi,catatan_keseimbangan_cairan.minum,catatan_keseimbangan_cairan.sh,catatan_keseimbangan_cairan.urine,catatan_keseimbangan_cairan.drain,"+
                   "catatan_keseimbangan_cairan.ngt,catatan_keseimbangan_cairan.iwl,catatan_keseimbangan_cairan.iwl2,catatan_keseimbangan_cairan.mandi,catatan_keseimbangan_cairan.aktifitas,catatan_keseimbangan_cairan.nafas,catatan_keseimbangan_cairan.tenun,catatan_keseimbangan_cairan.tusuk,catatan_keseimbangan_cairan.keterangan,"+
                    "catatan_keseimbangan_cairan.nip,petugas.nama "+
                    "from catatan_keseimbangan_cairan inner join reg_periksa on catatan_keseimbangan_cairan.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join petugas on catatan_keseimbangan_cairan.nip=petugas.nip where "+
                    "catatan_keseimbangan_cairan.tgl_perawatan between ? and ? and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? "+
                    "or pasien.nm_pasien like ? or catatan_keseimbangan_cairan.nip like ? or petugas.nama like ?) "+
                    "order by catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat ");
            }
                
            try {
                if(TCari.getText().trim().equals("")){
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                }else{
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,"%"+TCari.getText()+"%");
                    ps.setString(5,"%"+TCari.getText()+"%");
                    ps.setString(6,"%"+TCari.getText()+"%");
                    ps.setString(7,"%"+TCari.getText()+"%");
                }
                    
                rs=ps.executeQuery();
                while(rs.next()){
                    data.add(new Object[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
                        rs.getString("umurdaftar")+" "+rs.getString("sttsumur"),rs.getString("jk"),rs.getString("tgl_lahir"),
                        rs.getString("tgl_perawatan"),rs.getString("jam_rawat"),rs.getString("infus"),rs.getString("tranfusi"),
                        rs.getString("minum"),rs.getString("sh"),rs.getString("urine"),rs.getString("drain"),rs.getString("ngt"),rs.getString("iwl"),rs.getString("iwl2"),
                        rs.getString("mandi"),rs.getString("aktifitas"),rs.getString("nafas"),rs.getString("tenun"),rs.getString("tusuk"),
                        rs.getString("keterangan"),rs.getString("nip"),rs.getString("nama")
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
        }catch(SQLException e){
            System.out.println("Notifikasi : "+e);
        }
        if(SwingUtilities.isEventDispatchThread()){
            isiTabel(data);
        }else{
            SwingUtilities.invokeLater(() -> isiTabel(data));
        }
    }
    
    private void isiTabel(java.util.List<Object[]> data) {
        Valid.tabelKosong(tabMode);
        for(Object[] row : data){
            tabMode.addRow(row);
        }
        LCount.setText(""+tabMode.getRowCount());
    }
    
    public void emptTeks() {
        Infus.setText("0");
        Tranfusi.setText("0");
        Minum.setText("0");
        sh.setText("0");
        Urine.setText("0");
        sh2.setText("0");
        bab.setText("0");
        dll.setText("0");
        dll1.setText("0");
        Keterangan.setText("");
        Tanggal.setDate(new Date());
        TNoRw.requestFocus();
    } 

    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            Umur.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());
            JK.setText(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString());
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());
            Jam.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().substring(0,2));
            Menit.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().substring(3,5));
            Detik.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString().substring(6,8));
            Infus.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());
            Tranfusi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
            Minum.setText(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
            sh.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
            Urine.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            sh2.setText(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
            bab.setText(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            dll.setText(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
            dll1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());
            CmbMandi.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),17).toString()); 
            CmbAktifitas.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),18).toString()); 
            nafas.setText(tbObat.getValueAt(tbObat.getSelectedRow(),19).toString());
            CmbTenun.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),20).toString());
            CmbTusuk.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),21).toString());
            Keterangan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),22).toString());
            Valid.SetTgl(Tanggal,tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());  
        }
    }
    
    private void isRawat() {
        PreparedStatement psRawat=null;
        ResultSet rsRawat=null;
        try {
            psRawat=koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.tgl_lahir,reg_periksa.tgl_registrasi,reg_periksa.umurdaftar,"+
                    "reg_periksa.sttsumur,reg_periksa.jam_reg from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat=?");
            psRawat.setString(1,TNoRw.getText().trim());
            rsRawat=psRawat.executeQuery();
            if(rsRawat.next()){
                TNoRM.setText(rsRawat.getString("no_rkm_medis"));
                DTPCari1.setDate(rsRawat.getDate("tgl_registrasi"));
                TPasien.setText(rsRawat.getString("nm_pasien"));
                JK.setText(rsRawat.getString("jk"));
                Umur.setText(rsRawat.getString("umurdaftar")+" "+rsRawat.getString("sttsumur"));
                TglLahir.setText(rsRawat.getString("tgl_lahir"));
                TanggalRegistrasi.setText(rsRawat.getString("tgl_registrasi")+" "+rsRawat.getString("jam_reg"));
            }
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        } finally {
            try {
                if(rsRawat!=null){
                    rsRawat.close();
                }
                if(psRawat!=null){
                    psRawat.close();
                }
            } catch (SQLException e) {
                System.out.println("Notif : "+e);
            }
        }
    }
    
    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        isRawat();
        DTPCari2.setDate(tgl2);
        TCari.setText(norwt);
        ChkInput.setSelected(true);
        isForm();
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,400));
            FormInput.setVisible(true);      
            ChkInput.setVisible(true);
        }else if(ChkInput.isSelected()==false){           
            ChkInput.setVisible(false);            
            PanelInput.setPreferredSize(new Dimension(WIDTH,20));
            FormInput.setVisible(false);      
            ChkInput.setVisible(true);
        }
    }
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.getbalance_cairan());
        BtnHapus.setEnabled(akses.getbalance_cairan());
        BtnEdit.setEnabled(akses.getbalance_cairan());
        BtnPrint.setEnabled(akses.getbalance_cairan()); 
        if(akses.getjml2()>=1){
            NIP.setEditable(false);
            btnPetugas.setEnabled(false);
            NIP.setText(akses.getkode());
            NamaPetugas.setText(Sequel.CariPetugas(NIP.getText()));
            if(NamaPetugas.getText().equals("")){
                NIP.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan petugas...!!");
            }
        }  
        
        if(TANGGALMUNDUR.equals("no")){
            if(!akses.getkode().equals("Admin Utama")){
                Tanggal.setEditable(false);
                Tanggal.setEnabled(false);
                ChkKejadian.setEnabled(false);
                Jam.setEnabled(false);
                Menit.setEnabled(false);
                Detik.setEnabled(false);
            }
        }
    }

    private void jam(){
        ActionListener taskPerformer = new ActionListener(){
            private int nilai_jam;
            private int nilai_menit;
            private int nilai_detik;
            public void actionPerformed(ActionEvent e) {
                String nol_jam = "";
                String nol_menit = "";
                String nol_detik = "";
                
                Date now = Calendar.getInstance().getTime();

                // Mengambil nilaj JAM, MENIT, dan DETIK Sekarang
                if(ChkKejadian.isSelected()==true){
                    nilai_jam = now.getHours();
                    nilai_menit = now.getMinutes();
                    nilai_detik = now.getSeconds();
                }else if(ChkKejadian.isSelected()==false){
                    nilai_jam =Jam.getSelectedIndex();
                    nilai_menit =Menit.getSelectedIndex();
                    nilai_detik =Detik.getSelectedIndex();
                }

                // Jika nilai JAM lebih kecil dari 10 (hanya 1 digit)
                if (nilai_jam <= 9) {
                    // Tambahkan "0" didepannya
                    nol_jam = "0";
                }
                // Jika nilai MENIT lebih kecil dari 10 (hanya 1 digit)
                if (nilai_menit <= 9) {
                    // Tambahkan "0" didepannya
                    nol_menit = "0";
                }
                // Jika nilai DETIK lebih kecil dari 10 (hanya 1 digit)
                if (nilai_detik <= 9) {
                    // Tambahkan "0" didepannya
                    nol_detik = "0";
                }
                // Membuat String JAM, MENIT, DETIK
                String jam = nol_jam + Integer.toString(nilai_jam);
                String menit = nol_menit + Integer.toString(nilai_menit);
                String detik = nol_detik + Integer.toString(nilai_detik);
                // Menampilkan pada Layar
                //tampil_jam.setText("  " + jam + " : " + menit + " : " + detik + "  ");
                Jam.setSelectedItem(jam);
                Menit.setSelectedItem(menit);
                Detik.setSelectedItem(detik);
            }
        };
        // Timer
        new Timer(1000, taskPerformer).start();
    }

    private void ganti() {
        if(Sequel.mengedittf("catatan_keseimbangan_cairan","tgl_perawatan=? and jam_rawat=? and no_rawat=?","no_rawat=?,tgl_perawatan=?,jam_rawat=?,infus=?,"+
                "tranfusi=?,minum=?,sh=?,urine=?,drain=?,ngt=?,iwl=?,iwl2=?,mandi=?,aktifitas=?,nafas=?,tenun=?,tusuk=?,keterangan=?,nip=?",22,new String[]{
                TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+""),Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem(),
                Infus.getText(),Tranfusi.getText(),Minum.getText(),sh.getText(),Urine.getText(),sh2.getText(),bab.getText(),dll.getText(),dll1.getText(),CmbMandi.getSelectedItem()+"",CmbAktifitas.getSelectedItem()+"",nafas.getText(),CmbTenun.getSelectedItem()+"",CmbTusuk.getSelectedItem()+"",
                Keterangan.getText(),NIP.getText(),tbObat.getValueAt(tbObat.getSelectedRow(),6).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),7).toString(),
                tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
            })==true){
            tbObat.setValueAt(TNoRw.getText(),tbObat.getSelectedRow(),0);
            tbObat.setValueAt(TNoRM.getText(),tbObat.getSelectedRow(),1);
            tbObat.setValueAt(TPasien.getText(),tbObat.getSelectedRow(),2);
            tbObat.setValueAt(Umur.getText(),tbObat.getSelectedRow(),3);
            tbObat.setValueAt(JK.getText(),tbObat.getSelectedRow(),4);
            tbObat.setValueAt(TglLahir.getText(),tbObat.getSelectedRow(),5);
            tbObat.setValueAt(Valid.SetTgl(Tanggal.getSelectedItem()+""),tbObat.getSelectedRow(),6);
            tbObat.setValueAt(Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem(),tbObat.getSelectedRow(),7);
            tbObat.setValueAt(Infus.getText(),tbObat.getSelectedRow(),8);
            tbObat.setValueAt(Tranfusi.getText(),tbObat.getSelectedRow(),9);
            tbObat.setValueAt(Minum.getText(),tbObat.getSelectedRow(),10);
            tbObat.setValueAt(sh.getText(),tbObat.getSelectedRow(),11);
            tbObat.setValueAt(Urine.getText(),tbObat.getSelectedRow(),12);
            tbObat.setValueAt(sh2.getText(),tbObat.getSelectedRow(),13);
            tbObat.setValueAt(bab.getText(),tbObat.getSelectedRow(),14);
            tbObat.setValueAt(dll.getText(),tbObat.getSelectedRow(),15);
            tbObat.setValueAt(dll1.getText(),tbObat.getSelectedRow(),16);
            tbObat.setValueAt(CmbMandi.getSelectedItem()+"",tbObat.getSelectedRow(),17);
            tbObat.setValueAt(CmbAktifitas.getSelectedItem()+"",tbObat.getSelectedRow(),18);
            tbObat.setValueAt(nafas.getText(),tbObat.getSelectedRow(),19);
            tbObat.setValueAt(CmbTenun.getSelectedItem()+"",tbObat.getSelectedRow(),20);
            tbObat.setValueAt(CmbTusuk.getSelectedItem()+"",tbObat.getSelectedRow(),21);
            tbObat.setValueAt(Keterangan.getText(),tbObat.getSelectedRow(),22);
            tbObat.setValueAt(NIP.getText(),tbObat.getSelectedRow(),23);
            tbObat.setValueAt(NamaPetugas.getText(),tbObat.getSelectedRow(),24);
            emptTeks();
        }
    }

    private void hapus() {
        if(Sequel.queryu2tf("delete from catatan_keseimbangan_cairan where tgl_perawatan=? and jam_rawat=? and no_rawat=?",3,new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(),6).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),7).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
        })==true){
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText(""+tabMode.getRowCount());
            emptTeks();
        }else{
            JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
        }
    }

    private void simpan() {
        if(Sequel.menyimpantf("catatan_keseimbangan_cairan","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","Data",19,new String[]{
            TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+""),Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem(),
            Infus.getText(),Tranfusi.getText(),Minum.getText(),sh.getText(),Urine.getText(),sh2.getText(),bab.getText(),dll.getText(),dll1.getText(),CmbMandi.getSelectedItem().toString(),CmbAktifitas.getSelectedItem().toString(),nafas.getText(),CmbTenun.getSelectedItem().toString(),CmbTusuk.getSelectedItem().toString(),
            Keterangan.getText(),NIP.getText()
        })==true){
            tabMode.addRow(new Object[]{
                TNoRw.getText(),TNoRM.getText(),TPasien.getText(),Umur.getText(),JK.getText(),TglLahir.getText(),
                Valid.SetTgl(Tanggal.getSelectedItem()+""),Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem(),
                Infus.getText(),Tranfusi.getText(),Minum.getText(),sh.getText(),Urine.getText(),sh2.getText(),bab.getText(),dll.getText(),dll1.getText(),CmbMandi.getSelectedItem().toString(),CmbAktifitas.getSelectedItem().toString(),nafas.getText(),CmbTenun.getSelectedItem().toString(),CmbTusuk.getSelectedItem().toString(),
                Keterangan.getText(),NIP.getText(),NamaPetugas.getText()
            });
            LCount.setText(""+tabMode.getRowCount());
            emptTeks();
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
        if (petugas != null) {
            petugas.dispose();
        }
        super.dispose();
    }
}
