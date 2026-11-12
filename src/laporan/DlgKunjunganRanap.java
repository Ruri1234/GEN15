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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import simrskhanza.DlgCariBangsal;
import simrskhanza.DlgKabupaten;
import simrskhanza.DlgKecamatan;
import simrskhanza.DlgKelurahan;
import simrskhanza.DlgCariCaraBayar;

/**
 *
 * @author perpustakaan
 */
public final class DlgKunjunganRanap extends javax.swing.JDialog {
    private final DefaultTableModel tabMode,tabMode2;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps,ps2;
    private ResultSet rs,rs2;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private int i=0,lama=0,baru=0,laki=0,per=0;  
    private String setbaru="",setlama="",umurlk="",umurpr="",kddiagnosa="",diagnosa="",dokterdpjp="",status="";
    /** Creates new form DlgLhtBiaya
     * @param parent
     * @param modal */
    public DlgKunjunganRanap(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initTombolGrafik();
        this.setLocation(8,1);
        setSize(885,674);
        tabMode=new DefaultTableModel(null,new String[]{"No.","Lama","Baru","Nama Pasien","L","P","Alamat","Kode","Diagnosa","Ruang","Stts.Pulang","Tgl.Masuk","DPJP","Asal Poliklinik","Lama Rawat"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbBangsal.setModel(tabMode);
        //tbBangsal.setDefaultRenderer(Object.class, new WarnaTable(jPanel2.getBackground(),tbBangsal.getBackground()));
        tbBangsal.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbBangsal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 15; i++) {
            TableColumn column = tbBangsal.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(35);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(70);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(40);
            }else if(i==5){
                column.setPreferredWidth(40);
            }else if(i==6){
                column.setPreferredWidth(200);
            }else if(i==7){
                column.setPreferredWidth(40);
            }else if(i==8){
                column.setPreferredWidth(200);
            }else if(i==9){
                column.setPreferredWidth(200);
            }else if(i==10){
                column.setPreferredWidth(85);
            }else if(i==11){
                column.setPreferredWidth(75);
            }else if(i==12){
                column.setPreferredWidth(250);
            }else if(i==13){
                column.setPreferredWidth(110);
            }else if(i==14){
                column.setPreferredWidth(70);
            }
        }
        tbBangsal.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabMode2=new DefaultTableModel(null,new String[]{"No.","Lama","Baru","Nama Pasien","L","P","Alamat","Kode","Diagnosa","Ruang","Stts.Pulang","Tgl.Pulang","DPJP","Asal Poliklinik","Lama Rawat"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbBangsal2.setModel(tabMode2);
        //tbBangsal2.setDefaultRenderer(Object.class, new WarnaTable(jPanel2.getBackground(),tbBangsal2.getBackground()));
        tbBangsal2.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbBangsal2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 15; i++) {
            TableColumn column = tbBangsal2.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(35);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(70);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(40);
            }else if(i==5){
                column.setPreferredWidth(40);
            }else if(i==6){
                column.setPreferredWidth(200);
            }else if(i==7){
                column.setPreferredWidth(40);
            }else if(i==8){
                column.setPreferredWidth(200);
            }else if(i==9){
                column.setPreferredWidth(200);
            }else if(i==10){
                column.setPreferredWidth(85);
            }else if(i==11){
                column.setPreferredWidth(75);
            }else if(i==12){
                column.setPreferredWidth(200);
            }else if(i==13){
                column.setPreferredWidth(110);
            }else if(i==14){
                column.setPreferredWidth(70);
            }
        }
        tbBangsal2.setDefaultRenderer(Object.class, new WarnaTable());

        TCari.setDocument(new batasInput((int)90).getKata(TCari));
        ChkInput.setSelected(false);
        isForm();       
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
        ppTampilkanBaru = new widget.MenuItem();
        ppTampilkanLama = new widget.MenuItem();
        internalFrame1 = new widget.InternalFrame();
        TabRawat = new widget.TabPane();
        Scroll = new widget.ScrollPane();
        tbBangsal = new widget.Table();
        Scroll2 = new widget.ScrollPane();
        tbBangsal2 = new widget.Table();
        panelGlass5 = new widget.panelisi();
        label11 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        jLabel7 = new widget.Label();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.panelisi();
        label17 = new widget.Label();
        kdkamar = new widget.TextBox();
        nmkamar = new widget.TextBox();
        label19 = new widget.Label();
        kdpenjab = new widget.TextBox();
        nmpenjab = new widget.TextBox();
        label20 = new widget.Label();
        kddokter = new widget.TextBox();
        nmdokter = new widget.TextBox();
        label21 = new widget.Label();
        nmkabupaten = new widget.TextBox();
        label22 = new widget.Label();
        nmkecamatan = new widget.TextBox();
        nmkelurahan = new widget.TextBox();
        label23 = new widget.Label();
        BtnSeek2 = new widget.Button();
        BtnSeek4 = new widget.Button();
        BtnSeek3 = new widget.Button();
        BtnSeek7 = new widget.Button();
        BtnSeek6 = new widget.Button();
        BtnSeek5 = new widget.Button();

        TKd.setForeground(new java.awt.Color(255, 255, 255));
        TKd.setName("TKd"); // NOI18N

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        ppTampilkanBaru.setBackground(new java.awt.Color(255, 255, 254));
        ppTampilkanBaru.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppTampilkanBaru.setForeground(java.awt.Color.darkGray);
        ppTampilkanBaru.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppTampilkanBaru.setText("Tampilkan Pasien Baru");
        ppTampilkanBaru.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppTampilkanBaru.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppTampilkanBaru.setName("ppTampilkanBaru"); // NOI18N
        ppTampilkanBaru.setPreferredSize(new java.awt.Dimension(175, 25));
        ppTampilkanBaru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppTampilkanBaruBtnPrintActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppTampilkanBaru);

        ppTampilkanLama.setBackground(new java.awt.Color(255, 255, 254));
        ppTampilkanLama.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppTampilkanLama.setForeground(java.awt.Color.darkGray);
        ppTampilkanLama.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppTampilkanLama.setText("Tampilkan Pasien Lama");
        ppTampilkanLama.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppTampilkanLama.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppTampilkanLama.setName("ppTampilkanLama"); // NOI18N
        ppTampilkanLama.setPreferredSize(new java.awt.Dimension(175, 25));
        ppTampilkanLama.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppTampilkanLamaBtnPrintActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppTampilkanLama);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Kunjungan Rawat Inap ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        TabRawat.setBackground(new java.awt.Color(255, 255, 254));
        TabRawat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        Scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
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

        TabRawat.addTab("Berdasar Tanggal Masuk", Scroll);

        Scroll2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll2.setComponentPopupMenu(jPopupMenu1);
        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);

        tbBangsal2.setComponentPopupMenu(jPopupMenu1);
        tbBangsal2.setName("tbBangsal2"); // NOI18N
        tbBangsal2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbBangsal2MouseClicked(evt);
            }
        });
        tbBangsal2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbBangsal2KeyPressed(evt);
            }
        });
        Scroll2.setViewportView(tbBangsal2);

        TabRawat.addTab("Berdasar Tanggal Keluar", Scroll2);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        panelGlass5.setName("panelGlass5"); // NOI18N
        panelGlass5.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(55, 23));
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

        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(30, 23));
        panelGlass5.add(jLabel7);

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

        PanelInput.setBackground(new java.awt.Color(255, 255, 255));
        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('M');
        ChkInput.setText(".: Filter Data");
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

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 104));
        FormInput.setLayout(null);

        label17.setText("Ruang :");
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(35, 23));
        FormInput.add(label17);
        label17.setBounds(0, 10, 75, 23);

        kdkamar.setEditable(false);
        kdkamar.setName("kdkamar"); // NOI18N
        kdkamar.setPreferredSize(new java.awt.Dimension(75, 23));
        kdkamar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdkamarKeyPressed(evt);
            }
        });
        FormInput.add(kdkamar);
        kdkamar.setBounds(78, 10, 85, 23);

        nmkamar.setEditable(false);
        nmkamar.setName("nmkamar"); // NOI18N
        nmkamar.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmkamar);
        nmkamar.setBounds(165, 10, 228, 23);

        label19.setText("Cara Bayar :");
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(label19);
        label19.setBounds(0, 70, 75, 23);

        kdpenjab.setEditable(false);
        kdpenjab.setName("kdpenjab"); // NOI18N
        kdpenjab.setPreferredSize(new java.awt.Dimension(75, 23));
        kdpenjab.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdpenjabKeyPressed(evt);
            }
        });
        FormInput.add(kdpenjab);
        kdpenjab.setBounds(78, 70, 85, 23);

        nmpenjab.setEditable(false);
        nmpenjab.setName("nmpenjab"); // NOI18N
        nmpenjab.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmpenjab);
        nmpenjab.setBounds(165, 70, 228, 23);

        label20.setText("Dokter :");
        label20.setName("label20"); // NOI18N
        label20.setPreferredSize(new java.awt.Dimension(35, 23));
        FormInput.add(label20);
        label20.setBounds(0, 40, 75, 23);

        kddokter.setEditable(false);
        kddokter.setName("kddokter"); // NOI18N
        kddokter.setPreferredSize(new java.awt.Dimension(75, 23));
        kddokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kddokterKeyPressed(evt);
            }
        });
        FormInput.add(kddokter);
        kddokter.setBounds(78, 40, 85, 23);

        nmdokter.setEditable(false);
        nmdokter.setName("nmdokter"); // NOI18N
        nmdokter.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmdokter);
        nmdokter.setBounds(165, 40, 228, 23);

        label21.setText("Kab/Kota :");
        label21.setName("label21"); // NOI18N
        label21.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(label21);
        label21.setBounds(429, 10, 87, 23);

        nmkabupaten.setEditable(false);
        nmkabupaten.setName("nmkabupaten"); // NOI18N
        nmkabupaten.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmkabupaten);
        nmkabupaten.setBounds(519, 10, 260, 23);

        label22.setText("Kecamatan :");
        label22.setName("label22"); // NOI18N
        label22.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(label22);
        label22.setBounds(429, 40, 87, 23);

        nmkecamatan.setEditable(false);
        nmkecamatan.setName("nmkecamatan"); // NOI18N
        nmkecamatan.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmkecamatan);
        nmkecamatan.setBounds(519, 40, 260, 23);

        nmkelurahan.setEditable(false);
        nmkelurahan.setName("nmkelurahan"); // NOI18N
        nmkelurahan.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmkelurahan);
        nmkelurahan.setBounds(519, 70, 260, 23);

        label23.setText("Kelurahan :");
        label23.setName("label23"); // NOI18N
        label23.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(label23);
        label23.setBounds(429, 70, 87, 23);

        BtnSeek2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek2.setMnemonic('3');
        BtnSeek2.setToolTipText("Alt+3");
        BtnSeek2.setName("BtnSeek2"); // NOI18N
        BtnSeek2.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek2ActionPerformed(evt);
            }
        });
        BtnSeek2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek2KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek2);
        BtnSeek2.setBounds(396, 10, 28, 23);

        BtnSeek4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek4.setMnemonic('3');
        BtnSeek4.setToolTipText("Alt+3");
        BtnSeek4.setName("BtnSeek4"); // NOI18N
        BtnSeek4.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek4ActionPerformed(evt);
            }
        });
        FormInput.add(BtnSeek4);
        BtnSeek4.setBounds(396, 40, 28, 23);

        BtnSeek3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek3.setMnemonic('3');
        BtnSeek3.setToolTipText("Alt+3");
        BtnSeek3.setName("BtnSeek3"); // NOI18N
        BtnSeek3.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek3ActionPerformed(evt);
            }
        });
        FormInput.add(BtnSeek3);
        BtnSeek3.setBounds(396, 70, 28, 23);

        BtnSeek7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek7.setMnemonic('3');
        BtnSeek7.setToolTipText("Alt+3");
        BtnSeek7.setName("BtnSeek7"); // NOI18N
        BtnSeek7.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek7ActionPerformed(evt);
            }
        });
        FormInput.add(BtnSeek7);
        BtnSeek7.setBounds(782, 70, 28, 23);

        BtnSeek6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek6.setMnemonic('3');
        BtnSeek6.setToolTipText("Alt+3");
        BtnSeek6.setName("BtnSeek6"); // NOI18N
        BtnSeek6.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek6ActionPerformed(evt);
            }
        });
        FormInput.add(BtnSeek6);
        BtnSeek6.setBounds(782, 40, 28, 23);

        BtnSeek5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek5.setMnemonic('3');
        BtnSeek5.setToolTipText("Alt+3");
        BtnSeek5.setName("BtnSeek5"); // NOI18N
        BtnSeek5.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek5ActionPerformed(evt);
            }
        });
        FormInput.add(BtnSeek5);
        BtnSeek5.setBounds(782, 10, 28, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        if(ceksukses==false){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Map<String, Object> param = new HashMap<>();         
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());   
            param.put("periode",Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem());   
            param.put("lama",lama);   
            param.put("baru",baru);   
            param.put("total",(lama+baru));   
            param.put("laki",laki);   
            param.put("perempuan",per);   
            param.put("tanggal",Tgl2.getDate());  
            if(TabRawat.getSelectedIndex()==0){
                if(tabMode.getRowCount()==0){
                    JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
                    //TCari.requestFocus();
                }else if(tabMode.getRowCount()!=0){
                    if(ceksukses==false){
                        Sequel.queryu("delete from temporary where temp37='"+akses.getalamatip()+"'");
                        for(int r=0;r<tabMode.getRowCount();r++){ 
                            if(!tbBangsal.getValueAt(r,0).toString().contains(">>")){
                                try {
                                    Sequel.menyimpan("temporary","'"+r+"','"+
                                                tabMode.getValueAt(r,0).toString()+"','"+
                                                tabMode.getValueAt(r,1).toString()+"','"+
                                                tabMode.getValueAt(r,2).toString()+"','"+
                                                tabMode.getValueAt(r,3).toString()+"','"+
                                                tabMode.getValueAt(r,4).toString()+"','"+
                                                tabMode.getValueAt(r,5).toString()+"','"+
                                                tabMode.getValueAt(r,6).toString()+"','"+
                                                tabMode.getValueAt(r,7).toString()+"','"+
                                                tabMode.getValueAt(r,8).toString()+"','"+
                                                tabMode.getValueAt(r,9).toString()+"','"+
                                                tabMode.getValueAt(r,10).toString()+"','"+
                                                tabMode.getValueAt(r,11).toString()+"','"+
                                                tabMode.getValueAt(r,12).toString()+"','','','','','','','','','','','','','','','','','','','','','','','','"+akses.getalamatip()+"'","Rekap Nota Pembayaran");
                                } catch (Exception e) {
                                }   
                            }                    
                        }

                        Valid.MyReportqry("rptKunjunganRanap.jasper","report","::[ Laporan Kunjungan Rawat Inap ]::","select * from temporary where temporary.temp37='"+akses.getalamatip()+"' order by temporary.no",param);
                    }else{
                        JOptionPane.showMessageDialog(null,"Masih proses menampilkan data, harap tunggu terlebih dahulu...!");
                    }
                }
            }else if(TabRawat.getSelectedIndex()==1){
                if(tabMode2.getRowCount()==0){
                    JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
                    //TCari.requestFocus();
                }else if(tabMode2.getRowCount()!=0){
                    if(ceksukses==false){
                        Sequel.queryu("delete from temporary where temp37='"+akses.getalamatip()+"'");
                        for(int r=0;r<tabMode2.getRowCount();r++){ 
                            if(!tbBangsal2.getValueAt(r,0).toString().contains(">>")){
                                try{
                                    Sequel.menyimpan("temporary","'"+r+"','"+
                                                tabMode2.getValueAt(r,0).toString()+"','"+
                                                tabMode2.getValueAt(r,1).toString()+"','"+
                                                tabMode2.getValueAt(r,2).toString()+"','"+
                                                tabMode2.getValueAt(r,3).toString()+"','"+
                                                tabMode2.getValueAt(r,4).toString()+"','"+
                                                tabMode2.getValueAt(r,5).toString()+"','"+
                                                tabMode2.getValueAt(r,6).toString()+"','"+
                                                tabMode2.getValueAt(r,7).toString()+"','"+
                                                tabMode2.getValueAt(r,8).toString()+"','"+
                                                tabMode2.getValueAt(r,9).toString()+"','"+
                                                tabMode2.getValueAt(r,10).toString()+"','"+
                                                tabMode2.getValueAt(r,11).toString()+"','"+
                                                tabMode2.getValueAt(r,12).toString()+"','','','','','','','','','','','','','','','','','','','','','','','','"+akses.getalamatip()+"'","Rekap Nota Pembayaran");
                                } catch (Exception e) {
                                }  
                            }                    
                        }

                        Valid.MyReportqry("rptKunjunganRanap.jasper","report","::[ Laporan Kunjungan Rawat Inap ]::","select * from temporary where temporary.temp37='"+akses.getalamatip()+"' order by temporary.no",param);
                    }else{
                        JOptionPane.showMessageDialog(null,"Masih proses menampilkan data, harap tunggu terlebih dahulu...!");
                    }
                }            
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
        if(TabRawat.getSelectedIndex()==0){
            runBackground(() ->tampil());
        }else if(TabRawat.getSelectedIndex()==1){
            runBackground(() ->tampil2());
        }
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

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
           TCari.setText("");
           kdkamar.setText("");
           nmkamar.setText("");
           kddokter.setText("");
           nmdokter.setText("");
           kdpenjab.setText("");
           nmpenjab.setText("");
           nmkabupaten.setText("");
           nmkecamatan.setText("");
           nmkelurahan.setText("");
           status="";
           if(TabRawat.getSelectedIndex()==0){
               runBackground(() ->tampil());
           }else if(TabRawat.getSelectedIndex()==1){
               runBackground(() ->tampil2());
           }
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{
            
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==0){
            runBackground(() ->tampil());
        }else if(TabRawat.getSelectedIndex()==1){
            runBackground(() ->tampil2());
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void tbBangsal2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbBangsal2MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tbBangsal2MouseClicked

    private void tbBangsal2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbBangsal2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbBangsal2KeyPressed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void kdkamarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdkamarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select bangsal.nm_bangsal from bangsal where bangsal.kd_bangsal=?", nmkamar,kdkamar.getText());
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            Sequel.cariIsi("select bangsal.nm_bangsal from bangsal where bangsal.kd_bangsal=?", nmkamar,kdkamar.getText());
            BtnAll.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Sequel.cariIsi("select bangsal.nm_bangsal from bangsal where bangsal.kd_bangsal=?", nmkamar,kdkamar.getText());
            Tgl2.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnSeek2ActionPerformed(null);
        }
    }//GEN-LAST:event_kdkamarKeyPressed

    private void kdpenjabKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdpenjabKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnAll.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Tgl2.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnSeek2ActionPerformed(null);
        }
    }//GEN-LAST:event_kdpenjabKeyPressed

    private void kddokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kddokterKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kddokterKeyPressed

    private void ppTampilkanBaruBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppTampilkanBaruBtnPrintActionPerformed
        status="Baru";
        BtnCariActionPerformed(null);
    }//GEN-LAST:event_ppTampilkanBaruBtnPrintActionPerformed

    private void ppTampilkanLamaBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppTampilkanLamaBtnPrintActionPerformed
        status="Lama";
        BtnCariActionPerformed(null);
    }//GEN-LAST:event_ppTampilkanLamaBtnPrintActionPerformed

    private void BtnSeek2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek2ActionPerformed
        DlgCariBangsal kamar=new DlgCariBangsal(null,false);
        kamar.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(kamar.getTable().getSelectedRow()!= -1){
                    kdkamar.setText(kamar.getTable().getValueAt(kamar.getTable().getSelectedRow(),0).toString());
                    nmkamar.setText(kamar.getTable().getValueAt(kamar.getTable().getSelectedRow(),1).toString());
                    kdkamar.requestFocus();
                }
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        kamar.isCek();
        kamar.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        kamar.setLocationRelativeTo(internalFrame1);
        kamar.setAlwaysOnTop(false);
        kamar.setVisible(true);
    }//GEN-LAST:event_BtnSeek2ActionPerformed

    private void BtnSeek2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek2KeyPressed
        //Valid.pindah(evt,DTPCari2,TCari);
    }//GEN-LAST:event_BtnSeek2KeyPressed

    private void BtnSeek4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek4ActionPerformed
        DlgCariDokter dokter=new DlgCariDokter(null,false);
        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter.getTable().getSelectedRow()!= -1){
                    kddokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                    nmdokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                }
                kddokter.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {dokter.emptTeks();}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });

        dokter.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    dokter.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setAlwaysOnTop(false);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnSeek4ActionPerformed

    private void BtnSeek3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek3ActionPerformed
        DlgCariCaraBayar penjab=new DlgCariCaraBayar(null,false);
        penjab.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(penjab.getTable().getSelectedRow()!= -1){
                    kdpenjab.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(),1).toString());
                    nmpenjab.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(),2).toString());
                }
                kdpenjab.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {penjab.emptTeks();}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });

        penjab.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    penjab.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        penjab.isCek();
        penjab.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        penjab.setLocationRelativeTo(internalFrame1);
        penjab.setAlwaysOnTop(false);
        penjab.setVisible(true);
    }//GEN-LAST:event_BtnSeek3ActionPerformed

    private void BtnSeek7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek7ActionPerformed
        if(nmkecamatan.getText().equals("")){
            JOptionPane.showMessageDialog(null,"Silahkan pilih Kecamatan terlebih dahulu..!!");
        }else{
            DlgKelurahan kelurahan=new DlgKelurahan(null,false);
            kelurahan.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {}
                @Override
                public void windowClosing(WindowEvent e) {}
                @Override
                public void windowClosed(WindowEvent e) {
                    if(kelurahan.getTable().getSelectedRow()!= -1){
                        nmkelurahan.setText(kelurahan.getTable().getValueAt(kelurahan.getTable().getSelectedRow(),0).toString());
                    }
                    nmkelurahan.requestFocus();
                }
                @Override
                public void windowIconified(WindowEvent e) {}
                @Override
                public void windowDeiconified(WindowEvent e) {}
                @Override
                public void windowActivated(WindowEvent e) {kelurahan.emptTeks();}
                @Override
                public void windowDeactivated(WindowEvent e) {}
            });

            kelurahan.getTable().addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {}
                @Override
                public void keyPressed(KeyEvent e) {
                    if(e.getKeyCode()==KeyEvent.VK_SPACE){
                        kelurahan.dispose();
                    }
                }
                @Override
                public void keyReleased(KeyEvent e) {}
            });
            kelurahan.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            kelurahan.setLocationRelativeTo(internalFrame1);
            kelurahan.setAlwaysOnTop(false);
            kelurahan.setVisible(true);
        }
    }//GEN-LAST:event_BtnSeek7ActionPerformed

    private void BtnSeek6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek6ActionPerformed
        if(nmkabupaten.getText().equals("")){
            JOptionPane.showMessageDialog(null,"Silahkan pilih Kabupaten terlebih dahulu..!!");
        }else{
            DlgKecamatan kecamatan=new DlgKecamatan(null,false);
            kecamatan.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {}
                @Override
                public void windowClosing(WindowEvent e) {}
                @Override
                public void windowClosed(WindowEvent e) {
                    if(kecamatan.getTable().getSelectedRow()!= -1){
                        nmkecamatan.setText(kecamatan.getTable().getValueAt(kecamatan.getTable().getSelectedRow(),0).toString());
                    }
                    nmkecamatan.requestFocus();
                }
                @Override
                public void windowIconified(WindowEvent e) {}
                @Override
                public void windowDeiconified(WindowEvent e) {}
                @Override
                public void windowActivated(WindowEvent e) {kecamatan.emptTeks();}
                @Override
                public void windowDeactivated(WindowEvent e) {}
            });

            kecamatan.getTable().addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {}
                @Override
                public void keyPressed(KeyEvent e) {
                    if(e.getKeyCode()==KeyEvent.VK_SPACE){
                        kecamatan.dispose();
                    }
                }
                @Override
                public void keyReleased(KeyEvent e) {}
            });
            kecamatan.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            kecamatan.setLocationRelativeTo(internalFrame1);
            kecamatan.setAlwaysOnTop(false);
            kecamatan.setVisible(true);
        }
    }//GEN-LAST:event_BtnSeek6ActionPerformed

    private void BtnSeek5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek5ActionPerformed
        DlgKabupaten kabupaten=new DlgKabupaten(null,false);
        kabupaten.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(kabupaten.getTable().getSelectedRow()!= -1){
                    nmkabupaten.setText(kabupaten.getTable().getValueAt(kabupaten.getTable().getSelectedRow(),0).toString());
                }
                nmkabupaten.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {kabupaten.emptTeks();}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });

        kabupaten.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    kabupaten.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        kabupaten.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        kabupaten.setLocationRelativeTo(internalFrame1);
        kabupaten.setAlwaysOnTop(false);
        kabupaten.setVisible(true);
    }//GEN-LAST:event_BtnSeek5ActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        if(TabRawat.getSelectedIndex()==0){
                            runBackground(() ->tampil());
                        }else if(TabRawat.getSelectedIndex()==1){
                            runBackground(() ->tampil2());
                        }
                    }                        
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        if(TabRawat.getSelectedIndex()==0){
                            runBackground(() ->tampil());
                        }else if(TabRawat.getSelectedIndex()==1){
                            runBackground(() ->tampil2());
                        }
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        if(TabRawat.getSelectedIndex()==0){
                            runBackground(() ->tampil());
                        }else if(TabRawat.getSelectedIndex()==1){
                            runBackground(() ->tampil2());
                        }
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
            DlgKunjunganRanap dialog = new DlgKunjunganRanap(new javax.swing.JFrame(), true);
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
    private widget.Button BtnSeek2;
    private widget.Button BtnSeek3;
    private widget.Button BtnSeek4;
    private widget.Button BtnSeek5;
    private widget.Button BtnSeek6;
    private widget.Button BtnSeek7;
    private widget.CekBox ChkInput;
    private widget.panelisi FormInput;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll2;
    private widget.TextBox TCari;
    private widget.TextBox TKd;
    private widget.TabPane TabRawat;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.TextBox kddokter;
    private widget.TextBox kdkamar;
    private widget.TextBox kdpenjab;
    private widget.Label label11;
    private widget.Label label17;
    private widget.Label label18;
    private widget.Label label19;
    private widget.Label label20;
    private widget.Label label21;
    private widget.Label label22;
    private widget.Label label23;
    private widget.TextBox nmdokter;
    private widget.TextBox nmkabupaten;
    private widget.TextBox nmkamar;
    private widget.TextBox nmkecamatan;
    private widget.TextBox nmkelurahan;
    private widget.TextBox nmpenjab;
    private widget.panelisi panelGlass5;
    private widget.MenuItem ppTampilkanBaru;
    private widget.MenuItem ppTampilkanLama;
    private widget.Table tbBangsal;
    private widget.Table tbBangsal2;
    // End of variables declaration//GEN-END:variables

    public void tampil(){        
        try{   
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            Valid.tabelKosong(tabMode);   
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.alamat,pasien.jk,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,pasien.tgl_daftar,reg_periksa.stts_daftar,"+
                    "kamar_inap.kd_kamar,bangsal.nm_bangsal,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab)as almt_pj,kamar_inap.stts_pulang,kamar_inap.tgl_masuk,dokter.nm_dokter,poliklinik.nm_poli,kamar_inap.lama  "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join kamar_inap on reg_periksa.no_rawat=kamar_inap.no_rawat "+
                    "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj inner join kabupaten on pasien.kd_kab=kabupaten.kd_kab inner join kecamatan on pasien.kd_kec=kecamatan.kd_kec inner join kelurahan on pasien.kd_kel=kelurahan.kd_kel inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli  "+
                    "where reg_periksa.stts_daftar like '%"+status+"%' and reg_periksa.status_lanjut='Ranap' and reg_periksa.stts<>'Batal' and kamar_inap.stts_pulang<>'Pindah Kamar' and reg_periksa.tgl_registrasi between ? and ? and bangsal.nm_bangsal like ? and penjab.png_jawab like ? and kabupaten.nm_kab like ? and kecamatan.nm_kec like ? and kelurahan.nm_kel like ? and "+
                    "(pasien.alamat like ? or pasien.nm_pasien like ? or dokter.nm_dokter like ? or reg_periksa.no_rkm_medis like ? or kamar_inap.kd_kamar like ? or poliklinik.nm_poli like ? ) "+
                    "group by reg_periksa.no_rawat order by reg_periksa.tgl_registrasi");
            try {
                ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                ps.setString(3,"%"+nmkamar.getText().trim()+"%");
                ps.setString(4,"%"+nmpenjab.getText().trim()+"%");
                ps.setString(5,"%"+nmkabupaten.getText().trim()+"%");
                ps.setString(6,"%"+nmkecamatan.getText().trim()+"%");
                ps.setString(7,"%"+nmkelurahan.getText().trim()+"%");
                ps.setString(8,"%"+TCari.getText().trim()+"%");
                ps.setString(9,"%"+TCari.getText().trim()+"%");
                ps.setString(10,"%"+TCari.getText().trim()+"%");
                ps.setString(11,"%"+TCari.getText().trim()+"%");
                ps.setString(12,"%"+TCari.getText().trim()+"%");
                ps.setString(13,"%"+TCari.getText().trim()+"%"); 
                rs=ps.executeQuery();
                i=1;   
                lama=0;baru=0;laki=0;per=0;
                while(rs.next()){
                    dokterdpjp=rs.getString("nm_dokter");
                    try{
                        ps2=koneksi.prepareStatement("select dokter.nm_dokter from dpjp_ranap inner join dokter "+
                            "on dpjp_ranap.kd_dokter=dokter.kd_dokter where dpjp_ranap.no_rawat=? ");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();                    
                            while(rs2.next()){
                                dokterdpjp=rs2.getString("nm_dokter")+", "+dokterdpjp;
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }
                    } catch(Exception e){
                        System.out.println("Notifikasi : "+e);
                    }
                    
                    if(dokterdpjp.toLowerCase().contains(nmdokter.getText().toLowerCase())){
                        setbaru="";
                        setlama="";
                        if(rs.getString("stts_daftar").equals("Baru")){
                            setbaru=rs.getString("no_rkm_medis");
                            baru++;
                        }else if(rs.getString("stts_daftar").equals("Lama")){
                            setlama=rs.getString("no_rkm_medis");
                            lama++;
                        }
                        umurlk="";
                        umurpr="";
                        switch (rs.getString("jk")) {
                            case "L":
                                umurlk=rs.getString("umur");
                                laki++;
                                break;
                            case "P":
                                umurpr=rs.getString("umur");
                                per++;
                                break;
                        }
                        diagnosa="";
                        kddiagnosa="";
                        ps2=koneksi.prepareStatement(
                                "select penyakit.kd_penyakit,penyakit.nm_penyakit from penyakit inner join diagnosa_pasien " +
                                "on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit " +
                                "where diagnosa_pasien.no_rawat=? order by prioritas asc limit 1");
                        try {                                    
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                kddiagnosa=rs2.getString(1);
                                diagnosa=rs2.getString(2);
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }
                        
                        tabMode.addRow(new Object[]{
                            i,setlama,setbaru,rs.getString("nm_pasien"),umurlk,umurpr,rs.getString("almt_pj"),kddiagnosa,diagnosa,rs.getString("kd_kamar")+" "+rs.getString("nm_bangsal"),rs.getString("stts_pulang"),rs.getString("tgl_masuk"),dokterdpjp,rs.getString("nm_poli"),rs.getString("lama")
                        });                
                        i++;
                    }
                        
                }
                if(i>=2){
                    tabMode.addRow(new Object[]{
                        ">>",lama,baru,"",laki,per,"","","",""
                    });
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }    
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }

    public void tampil2(){        
        try{   
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            Valid.tabelKosong(tabMode2);   
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.alamat,pasien.jk,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,pasien.tgl_daftar,reg_periksa.stts_daftar,"+
                    "kamar_inap.kd_kamar,bangsal.nm_bangsal,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab)as almt_pj,kamar_inap.stts_pulang,kamar_inap.tgl_keluar,dokter.nm_dokter,poliklinik.nm_poli,kamar_inap.lama "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join kamar_inap on reg_periksa.no_rawat=kamar_inap.no_rawat "+
                    "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj inner join kabupaten on pasien.kd_kab=kabupaten.kd_kab inner join kecamatan on pasien.kd_kec=kecamatan.kd_kec inner join kelurahan on pasien.kd_kel=kelurahan.kd_kel inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli  "+
                    "where reg_periksa.stts_daftar like '%"+status+"%' and kamar_inap.stts_pulang<>'Pindah Kamar' and kamar_inap.tgl_keluar between ? and ? and bangsal.nm_bangsal like ? and penjab.png_jawab like ? and kabupaten.nm_kab like ? and kecamatan.nm_kec like ? and kelurahan.nm_kel like ? and "+
                    "(dokter.nm_dokter like ? or pasien.alamat like ? or pasien.nm_pasien like ? or reg_periksa.no_rkm_medis like ? or kamar_inap.kd_kamar like ? or poliklinik.nm_poli like ? ) "+
                    "group by reg_periksa.no_rawat order by kamar_inap.tgl_keluar");
            try {
                ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                ps.setString(3,"%"+nmkamar.getText().trim()+"%");
                ps.setString(4,"%"+nmpenjab.getText().trim()+"%");
                ps.setString(5,"%"+nmkabupaten.getText().trim()+"%");
                ps.setString(6,"%"+nmkecamatan.getText().trim()+"%");
                ps.setString(7,"%"+nmkelurahan.getText().trim()+"%");
                ps.setString(8,"%"+TCari.getText().trim()+"%");
                ps.setString(9,"%"+TCari.getText().trim()+"%");
                ps.setString(10,"%"+TCari.getText().trim()+"%");
                ps.setString(11,"%"+TCari.getText().trim()+"%");
                ps.setString(12,"%"+TCari.getText().trim()+"%");
                ps.setString(13,"%"+TCari.getText().trim()+"%"); 
                rs=ps.executeQuery();
                i=1;   
                lama=0;baru=0;laki=0;per=0;
                while(rs.next()){
                    dokterdpjp=rs.getString("nm_dokter");
                    try{
                        ps2=koneksi.prepareStatement("select dokter.nm_dokter from dpjp_ranap inner join dokter "+
                            "on dpjp_ranap.kd_dokter=dokter.kd_dokter where dpjp_ranap.no_rawat=? ");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();                    
                            while(rs2.next()){
                                dokterdpjp=rs2.getString("nm_dokter")+", "+dokterdpjp;
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }
                    } catch(Exception e){
                        System.out.println("Notifikasi : "+e);
                    }
                    if(dokterdpjp.toLowerCase().contains(nmdokter.getText().toLowerCase())){
                        setbaru="";
                        setlama="";
                        if(rs.getString("stts_daftar").equals("Baru")){
                            setbaru=rs.getString("no_rkm_medis");
                            baru++;
                        }else if(rs.getString("stts_daftar").equals("Lama")){
                            setlama=rs.getString("no_rkm_medis");
                            lama++;
                        }
                        umurlk="";
                        umurpr="";
                        switch (rs.getString("jk")) {
                            case "L":
                                umurlk=rs.getString("umur");
                                laki++;
                                break;
                            case "P":
                                umurpr=rs.getString("umur");
                                per++;
                                break;
                        }
                        diagnosa="";
                        kddiagnosa="";
                        ps2=koneksi.prepareStatement(
                                "select penyakit.kd_penyakit,penyakit.nm_penyakit from penyakit inner join diagnosa_pasien " +
                                "on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit " +
                                "where diagnosa_pasien.no_rawat=? order by prioritas asc limit 1");
                        try {                                    
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                kddiagnosa=rs2.getString(1);
                                diagnosa=rs2.getString(2);
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }
                        tabMode2.addRow(new Object[]{
                            i,setlama,setbaru,rs.getString("nm_pasien"),umurlk,umurpr,rs.getString("almt_pj"),kddiagnosa,diagnosa,rs.getString("kd_kamar")+" "+rs.getString("nm_bangsal"),rs.getString("stts_pulang"),rs.getString("tgl_keluar"),dokterdpjp,rs.getString("nm_poli"),rs.getString("lama")
                        });                
                        i++;
                    }
                }
                if(i>=2){
                    tabMode2.addRow(new Object[]{
                        ">>",lama,baru,"",laki,per,"","","",""
                    });
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }    
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }
    
    private void getData() {
        int row=tbBangsal.getSelectedRow();
        if(row!= -1){
            TKd.setText(tabMode.getValueAt(row,0).toString());
        }
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,126));
            FormInput.setVisible(true);      
            ChkInput.setVisible(true);
        }else if(ChkInput.isSelected()==false){           
            ChkInput.setVisible(false);            
            PanelInput.setPreferredSize(new Dimension(WIDTH,20));
            FormInput.setVisible(false);      
            ChkInput.setVisible(true);
        }
    }

    private void runBackground(Runnable task) {
        if (ceksukses) return;
        if (executor.isShutdown() || executor.isTerminated()) return;
        if (!isDisplayable()) return;
        ceksukses = true;
        executor.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            } finally {
                ceksukses = false;
            }
        });
    }

    // ===================== GRAFIK KUNJUNGAN NON BATAL =====================
    // Sesuaikan konstanta berikut dengan data di database RS Anda bila berbeda.
    private static final String KD_POLI_IGD="IGDK";             // reg_periksa.kd_poli untuk IGD (asal pasien rawat inap dari IGD)
    private static final String PJ_BPJS="BPJS Kesehatan";       // penjab.png_jawab untuk BPJS Kesehatan
    private static final String[] NAMA_BULAN={"Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember"};
    private widget.Button BtnGrafik;

    /** Ikon tombol Grafik: ubin biru dengan batang bertumpuk (putih = BPJS, oranye = Reguler), digambar lewat kode. */
    private static javax.swing.ImageIcon buatIkonGrafik(){
        int s=96;
        BufferedImage big=new BufferedImage(s,s,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=big.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g.setPaint(new GradientPaint(0,0,new Color(100,190,255),0,s,new Color(22,85,205)));
        g.fillRoundRect(0,0,s,s,30,30);
        g.setColor(new Color(255,255,255,45));
        g.setStroke(new java.awt.BasicStroke(2f));
        g.drawRoundRect(1,1,s-3,s-3,30,30);
        int[] tinggi={26,44,64};
        int base=78,lebar=16,x0=16,jarak=8;
        for(int k=0;k<3;k++){
            int x=x0+k*(lebar+jarak);
            int h=tinggi[k];
            int reg=Math.round(h*0.32f);
            java.awt.Shape lama=g.getClip();
            g.clip(new java.awt.geom.Rectangle2D.Double(0,0,s,base));
            g.clip(new RoundRectangle2D.Double(x,base-h,lebar,h+lebar,10,10));
            g.setColor(new Color(255,255,255,245));
            g.fillRect(x,base-h+reg,lebar,h-reg);
            g.setPaint(new GradientPaint(0,base-h,new Color(255,213,110),0,base-h+reg,new Color(255,150,20)));
            g.fillRect(x,base-h,lebar,reg);
            g.setClip(lama);
                        }
        g.setColor(new Color(255,255,255,150));
        g.fillRoundRect(10,81,76,4,4,4);
        g.dispose();
        return new javax.swing.ImageIcon(big.getScaledInstance(22,22,java.awt.Image.SCALE_SMOOTH));
    }

    private void initTombolGrafik(){
        BtnGrafik=new widget.Button();
        BtnGrafik.setMnemonic('G');
        BtnGrafik.setIcon(buatIkonGrafik());
        BtnGrafik.setText("Grafik");
        BtnGrafik.setToolTipText("Alt+G");
        BtnGrafik.setName("BtnGrafik");
        BtnGrafik.setPreferredSize(new Dimension(90,30));
        BtnGrafik.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tampilGrafik();
            }
                    });
        int idx=-1;
        java.awt.Component[] komp=panelGlass5.getComponents();
        for(int k=0;k<komp.length;k++){
            if(komp[k]==BtnPrint){
                idx=k;
                break;
                }
        }
        panelGlass5.add(BtnGrafik,idx);
        panelGlass5.revalidate();
        panelGlass5.repaint();
    }

    private void tampilGrafik(){
        int tahunIni=java.time.LocalDate.now().getYear();
        int tahun;
        try{
            tahun=Integer.parseInt(Valid.SetTgl(Tgl1.getSelectedItem()+"").substring(0,4));
        }catch(Exception e){
            tahun=tahunIni;
        }
        if(tahun>tahunIni){
            tahun=tahunIni;
        }
        String namaRS="";
        try{
            namaRS=Sequel.cariIsi("select nama_instansi from setting limit 1");
        }catch(Exception e){
            namaRS="";
        }
        if(namaRS==null){
            namaRS="";
        }
        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            DialogGrafik dlg=new DialogGrafik(this,koneksi,namaRS,tahun);
            this.setCursor(Cursor.getDefaultCursor());
            dlg.setVisible(true);
        }finally{
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private static String fmt(int n){
        return java.text.NumberFormat.getIntegerInstance(new java.util.Locale("id","ID")).format(n);
    }

    private static String delta(int sekarang,int lalu){
        if(lalu==0){
            return sekarang==0?"Sama dengan tahun lalu":"Tidak ada pembanding";
        }
        return (sekarang>=lalu?"Naik ":"Turun ")+String.format("%.1f%%",Math.abs(sekarang-lalu)*100.0/lalu)+" ("+(sekarang>=lalu?"+":"-")+fmt(Math.abs(sekarang-lalu))+")";
    }

    private static int jumlah(int[] a){
        int s=0;
        for(int v:a){
            s+=v;
        }
        return s;
    }

    // ---------------------------------------------------------------------------------
    private static class PanelGelap extends JPanel {
        PanelGelap(){
            super(new BorderLayout());
        }
        @Override
        protected void paintComponent(Graphics g0){
            Graphics2D g=(Graphics2D)g0.create();
            g.setColor(new Color(11,18,40));
            g.fillRect(0,0,getWidth(),getHeight());
            g.setPaint(new GradientPaint(0,0,new Color(18,72,96),0,240,new Color(11,18,40)));
            g.fillRect(0,0,getWidth(),240);
            g.dispose();
        }
    }

    private static class TombolGelap extends JToggleButton {
        private final boolean aksi;
        TombolGelap(String teks,boolean aksi){
            super(teks);
            this.aksi=aksi;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif",Font.BOLD,11));
            setBorder(BorderFactory.createEmptyBorder(7,14,7,14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g0){
            Graphics2D g=(Graphics2D)g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight();
            boolean sel=isSelected()&&!aksi;
            if(!isEnabled()){
                g.setColor(new Color(255,255,255,10));
            }else if(sel){
                g.setPaint(new GradientPaint(0,0,new Color(66,165,245),0,h,new Color(30,100,220)));
            }else if(getModel().isRollover()||getModel().isArmed()){
                g.setColor(new Color(255,255,255,50));
            }else{
                g.setColor(new Color(255,255,255,24));
            }
            g.fillRoundRect(0,0,w-1,h-1,12,12);
            if(!sel){
                g.setColor(new Color(255,255,255,40));
                g.drawRoundRect(0,0,w-1,h-1,12,12);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    private static class KartuKpi extends JPanel {
        private final JLabel cap=new JLabel(" ");
        private final JLabel val=new JLabel(" ");
        private final JLabel sub=new JLabel(" ");
        KartuKpi(){
            setOpaque(false);
            setLayout(new javax.swing.BoxLayout(this,javax.swing.BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(9,14,9,14));
            cap.setFont(new Font("SansSerif",Font.BOLD,10));
            cap.setForeground(new Color(150,175,205));
            val.setFont(new Font("SansSerif",Font.BOLD,20));
            val.setForeground(Color.WHITE);
            sub.setFont(new Font("SansSerif",Font.PLAIN,11));
            sub.setForeground(new Color(170,185,215));
            add(cap);
            add(val);
            add(sub);
        }
        void set(String c,String v,String s,Color warnaSub){
            cap.setText(c);
            val.setText(v);
            sub.setText(s);
            sub.setForeground(warnaSub);
        }
        @Override
        protected void paintComponent(Graphics g0){
            Graphics2D g=(Graphics2D)g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(255,255,255,20));
            g.fillRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
            g.setColor(new Color(255,255,255,45));
            g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    // ---------------------------------------------------------------------------------
    /** Grafik batang bertumpuk (BPJS + Reguler) per bulan: tahun lalu (pudar) berdampingan dengan tahun ini. */
    private static class PanelGrafik extends JPanel {
        private int tahun;
        private int[][] cur=new int[2][12];
        private int[][] prev=new int[2][12];
        private String labelData="Gabungan";
        private float progress=1f;
        private int hover=-1,mx=0,my=0;
        private javax.swing.Timer timer;
        private long mulai;
        private int pKiri,pAtas,pBawah;
        private double pGw=1;

        PanelGrafik(){
            setOpaque(false);
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e){
                    mx=e.getX();
                    my=e.getY();
                    int baru=-1;
                    if(pGw>1&&e.getX()>=pKiri&&e.getX()<pKiri+pGw*12&&e.getY()>=pAtas-10&&e.getY()<=pBawah+24){
                        baru=(int)((e.getX()-pKiri)/pGw);
                        if(baru>11){
                            baru=11;
                        }
                    }
                    hover=baru;
                    repaint();
                }
            });
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseExited(java.awt.event.MouseEvent e){
                    hover=-1;
                    repaint();
        }
            });
    }
    
        void setData(int tahun,int[][] cur,int[][] prev,String labelData){
            this.tahun=tahun;
            this.cur=cur;
            this.prev=prev;
            this.labelData=labelData;
        }

        void mulaiAnimasi(){
            if(timer!=null){
                timer.stop();
            }
            mulai=System.nanoTime();
            progress=0f;
            timer=new javax.swing.Timer(16,new java.awt.event.ActionListener() {
    @Override
                public void actionPerformed(java.awt.event.ActionEvent e){
                    float p=(System.nanoTime()-mulai)/1.2e9f;
                    if(p>=1f){
                        p=1f;
                        timer.stop();
    }
                    progress=p;
                    repaint();
}
            });
            timer.start();
        }

        void selesaikanAnimasi(){
            if(timer!=null){
                timer.stop();
            }
            progress=1f;
            hover=-1;
        }

        private void batang(Graphics2D g,double x,double bw,int bpjs,int reg,double base,int ph,double max,float e,boolean pudar){
            double hB=ph*bpjs/max*e,hR=ph*reg/max*e,tot=hB+hR;
            if(tot<=0){
                return;
            }
            double top=base-tot;
            java.awt.Shape lama=g.getClip();
            g.clip(new java.awt.geom.Rectangle2D.Double(0,0,100000,base));
            g.clip(new RoundRectangle2D.Double(x,top,bw,tot+bw,10,10));
            int a=pudar?100:255;
            g.setPaint(new GradientPaint(0,(float)(base-hB),new Color(79,195,247,a),0,(float)base,new Color(33,115,230,a)));
            g.fill(new java.awt.geom.Rectangle2D.Double(x,base-hB,bw,hB));
            g.setPaint(new GradientPaint(0,(float)top,new Color(255,200,110,a),0,(float)(top+Math.max(hR,1)),new Color(251,140,0,a)));
            g.fill(new java.awt.geom.Rectangle2D.Double(x,top,bw,hR));
            g.setClip(lama);
        }

        private void barisJenis(Graphics2D g,int y,String nama,int n,int total,Color c1,Color c2,int bx,int lebar,float e){
            g.setPaint(new GradientPaint(0,y,c1,0,y+12,c2));
            g.fillRoundRect(28,y,12,12,4,4);
            g.setFont(new Font("SansSerif",Font.PLAIN,12));
            g.setColor(new Color(225,232,248));
            g.drawString(nama,48,y+11);
            g.setColor(new Color(255,255,255,22));
            g.fillRoundRect(bx,y-1,lebar,14,14,14);
            int len=total>0?(int)Math.round(lebar*(double)n/total*e):0;
            if(len>0){
                g.setPaint(new GradientPaint(bx,0,c2,bx+lebar,0,c1));
                g.fillRoundRect(bx,y-1,Math.max(14,len),14,14,14);
            }
            String s1=fmt(n)+" pasien";
            String s2="  ("+String.format("%.1f%%",total>0?n*100.0/total:0.0)+")";
            g.setFont(new Font("SansSerif",Font.BOLD,12));
            g.setColor(Color.WHITE);
            g.drawString(s1,bx+lebar+14,y+11);
            int w1=g.getFontMetrics().stringWidth(s1);
            g.setFont(new Font("SansSerif",Font.PLAIN,11));
            g.setColor(new Color(150,165,195));
            g.drawString(s2,bx+lebar+14+w1,y+11);
        }

        @Override
        protected void paintComponent(Graphics g0){
            Graphics2D g=(Graphics2D)g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight();
            g.setPaint(new GradientPaint(0,0,new Color(19,31,64),0,h,new Color(12,19,42)));
            g.fillRoundRect(0,0,w-1,h-1,18,18);
            g.setColor(new Color(255,255,255,22));
            g.drawRoundRect(0,0,w-1,h-1,18,18);

            int ringkas=130,kiri=64,kanan=28,atas=62;
            int bawah=h-ringkas-30;
            int pw=w-kiri-kanan,ph=bawah-atas;
            if(pw<120||ph<60){
                g.dispose();
                return;
            }
            pKiri=kiri;pAtas=atas;pBawah=bawah;pGw=(double)pw/12;
            float eAll=1-(1-progress)*(1-progress)*(1-progress);

            // legenda
            g.setFont(new Font("SansSerif",Font.PLAIN,12));
            FontMetrics fm=g.getFontMetrics();
            int lx=kiri,ly=28;
            g.setPaint(new GradientPaint(0,ly-11,new Color(79,195,247),0,ly+1,new Color(33,115,230)));
            g.fillRoundRect(lx,ly-11,12,12,4,4);
            g.setColor(new Color(225,232,248));
            g.drawString("BPJS Kesehatan",lx+18,ly);
            lx+=18+fm.stringWidth("BPJS Kesehatan")+22;
            g.setPaint(new GradientPaint(0,ly-11,new Color(255,200,110),0,ly+1,new Color(251,140,0)));
            g.fillRoundRect(lx,ly-11,12,12,4,4);
            g.setColor(new Color(225,232,248));
            g.drawString("Reguler",lx+18,ly);
            lx+=18+fm.stringWidth("Reguler")+36;
            g.setColor(new Color(255,255,255,235));
            g.fillRoundRect(lx,ly-11,12,12,4,4);
            g.setColor(new Color(225,232,248));
            g.drawString("Tahun "+tahun,lx+18,ly);
            lx+=18+fm.stringWidth("Tahun "+tahun)+22;
            g.setColor(new Color(255,255,255,90));
            g.fillRoundRect(lx,ly-11,12,12,4,4);
            g.setColor(new Color(225,232,248));
            g.drawString("Tahun "+(tahun-1)+" (pudar)",lx+18,ly);
            g.setFont(new Font("SansSerif",Font.BOLD,11));
            g.setColor(new Color(255,213,79));
            String ket="DATA : "+labelData.toUpperCase();
            g.drawString(ket,w-kanan-g.getFontMetrics().stringWidth(ket),ly);

            // skala
            int max=0;
            for(int m=0;m<12;m++){
                max=Math.max(max,Math.max(cur[0][m]+cur[1][m],prev[0][m]+prev[1][m]));
            }
            double kasar=Math.max(max,5)/5.0;
            double pangkat=Math.pow(10,Math.floor(Math.log10(kasar)));
            double f=kasar/pangkat;
            double langkah=(f<=1?1:f<=2?2:f<=5?5:10)*pangkat;
            int garis=(int)Math.ceil(Math.max(max,1)/langkah);
            double sumbuMax=langkah*garis;

            g.setFont(new Font("SansSerif",Font.PLAIN,10));
            fm=g.getFontMetrics();
            for(int i=0;i<=garis;i++){
                int y=bawah-(int)Math.round(ph*(i*langkah)/sumbuMax);
                g.setColor(new Color(255,255,255,i==0?60:20));
                g.drawLine(kiri,y,kiri+pw,y);
                g.setColor(new Color(140,160,195));
                String t=fmt((int)Math.round(i*langkah));
                g.drawString(t,kiri-8-fm.stringWidth(t),y+4);
            }

            // sorotan bulan yang ditunjuk
            if(hover>=0){
                g.setColor(new Color(255,255,255,16));
                g.fillRoundRect((int)(kiri+hover*pGw)+2,atas-6,(int)pGw-4,ph+34,10,10);
            }

            // batang
            double gw=pGw;
            double bw=Math.min(36,gw*0.34);
            double gap=3;
            for(int m=0;m<12;m++){
                double cx=kiri+m*gw+gw/2;
                float pm=Math.max(0f,Math.min(1f,progress*1.4f-m*0.035f));
                float e=1-(1-pm)*(1-pm)*(1-pm);
                double xPrev=cx-gap/2-bw,xCur=cx+gap/2;
                batang(g,xPrev,bw,prev[0][m],prev[1][m],bawah,ph,sumbuMax,e,true);
                batang(g,xCur,bw,cur[0][m],cur[1][m],bawah,ph,sumbuMax,e,false);
                if(e>0.92f){
                    g.setFont(new Font("SansSerif",Font.BOLD,10));
                    fm=g.getFontMetrics();
                    int tp=prev[0][m]+prev[1][m],tc=cur[0][m]+cur[1][m];
                    if(tp>0){
                        String s=String.valueOf(tp);
                        g.setColor(new Color(150,165,195));
                        g.drawString(s,(int)(xPrev+bw/2-fm.stringWidth(s)/2.0),(int)(bawah-ph*tp/sumbuMax)-5);
                    }
                    if(tc>0){
                        String s=String.valueOf(tc);
                        g.setColor(Color.WHITE);
                        g.drawString(s,(int)(xCur+bw/2-fm.stringWidth(s)/2.0),(int)(bawah-ph*tc/sumbuMax)-5);
                    }
                }
                g.setFont(new Font("SansSerif",m==hover?Font.BOLD:Font.PLAIN,11));
                fm=g.getFontMetrics();
                String bl=NAMA_BULAN[m].substring(0,3);
                g.setColor(m==hover?Color.WHITE:new Color(170,185,215));
                g.drawString(bl,(int)(cx-fm.stringWidth(bl)/2.0),bawah+18);
            }

            // ringkasan per jenis bayar
            int y0=h-ringkas;
            g.setColor(new Color(255,255,255,28));
            g.drawLine(24,y0,w-24,y0);
            g.setFont(new Font("SansSerif",Font.BOLD,10));
            g.setColor(new Color(140,160,195));
            g.drawString("TOTAL PER JENIS BAYAR  \u2022  "+labelData.toUpperCase()+"  \u2022  TAHUN "+tahun,28,y0+22);
            int sb=jumlah(cur[0]),sr=jumlah(cur[1]),st=sb+sr;
            int bx=210,bl=Math.max(60,w-bx-260);
            barisJenis(g,y0+36,"BPJS KESEHATAN",sb,st,new Color(79,195,247),new Color(33,115,230),bx,bl,eAll);
            barisJenis(g,y0+62,"REGULER",sr,st,new Color(255,200,110),new Color(251,140,0),bx,bl,eAll);
            g.setColor(new Color(255,255,255,28));
            g.drawLine(24,y0+84,w-24,y0+84);
            int sp=jumlah(prev[0])+jumlah(prev[1]);
            g.setFont(new Font("SansSerif",Font.BOLD,12));
            g.setColor(new Color(255,213,79));
            g.drawString("TOTAL",28,y0+108);
            g.setColor(Color.WHITE);
            String tx=fmt(st)+" pasien";
            g.drawString(tx,bx,y0+108);
            g.setFont(new Font("SansSerif",Font.PLAIN,11));
            g.setColor(new Color(150,165,195));
            g.drawString("\u2022  tahun "+(tahun-1)+" : "+fmt(sp)+"  \u2022  "+delta(st,sp),bx+g.getFontMetrics().stringWidth(tx)+30,y0+108);

            // tooltip
            if(hover>=0&&progress>=1f){
                int m=hover;
                int cb=cur[0][m],cr=cur[1][m],pb=prev[0][m],pr=prev[1][m];
                String[] baris={
                    NAMA_BULAN[m],
                    "Tahun "+tahun+" : "+fmt(cb+cr)+" pasien",
                    "    BPJS "+fmt(cb)+"   \u2022   Reguler "+fmt(cr),
                    "Tahun "+(tahun-1)+" : "+fmt(pb+pr)+" pasien",
                    "    BPJS "+fmt(pb)+"   \u2022   Reguler "+fmt(pr),
                    delta(cb+cr,pb+pr)
                };
                g.setFont(new Font("SansSerif",Font.PLAIN,12));
                fm=g.getFontMetrics();
                int tw=0;
                for(String s:baris){
                    tw=Math.max(tw,fm.stringWidth(s));
                }
                int bwid=tw+28,bhgt=baris.length*18+16;
                int tx0=mx+16,ty0=my+14;
                if(tx0+bwid>w-8){
                    tx0=mx-16-bwid;
                }
                if(ty0+bhgt>h-8){
                    ty0=h-8-bhgt;
                }
                g.setColor(new Color(8,14,32,238));
                g.fillRoundRect(tx0,ty0,bwid,bhgt,12,12);
                g.setColor(new Color(255,255,255,70));
                g.drawRoundRect(tx0,ty0,bwid,bhgt,12,12);
                for(int k=0;k<baris.length;k++){
                    if(k==0){
                        g.setFont(new Font("SansSerif",Font.BOLD,13));
                        g.setColor(new Color(255,213,79));
                    }else if(k==5){
                        g.setFont(new Font("SansSerif",Font.BOLD,12));
                        g.setColor(cb+cr>=pb+pr?new Color(102,187,106):new Color(239,83,80));
                    }else{
                        g.setFont(new Font("SansSerif",k==1||k==3?Font.BOLD:Font.PLAIN,12));
                        g.setColor(k<=2?Color.WHITE:new Color(170,185,215));
                    }
                    g.drawString(baris[k],tx0+14,ty0+22+k*18);
                }
            }
            g.dispose();
        }
    }

    // ---------------------------------------------------------------------------------
    private static class DialogGrafik extends JDialog {
        private final Connection kon;
        private final String namaRS;
        private int tahun;
        private int[][][][] data=new int[2][2][2][12]; // [0=RJ,1=IGD][0=tahun ini,1=tahun lalu][0=BPJS,1=Reguler][bulan]
        private java.time.LocalDate a1,a2,b1,b2;
        private int pilihGrup=0;
        private boolean lihatGrafik=true;
        private final KartuKpi[] kpi=new KartuKpi[5];
        private final PanelGrafik grafik=new PanelGrafik();
        private final JPanel panelTabel=new JPanel(new java.awt.GridLayout(1,2,26,0));
        private final JPanel kartu=new JPanel(new java.awt.CardLayout());
        private final JLabel lblTahun=new JLabel("",SwingConstants.CENTER);
        private final JLabel lblSub=new JLabel(" ");
        private final TombolGelap tbGrafik=new TombolGelap("Grafik",false);
        private final TombolGelap tbTabel=new TombolGelap("Tabel Rekap",false);
        private final TombolGelap tbGab=new TombolGelap("Gabungan",false);
        private final TombolGelap tbRj=new TombolGelap("Rawat Jalan",false);
        private final TombolGelap tbIgd=new TombolGelap("IGD",false);
        private final TombolGelap btnPrev=new TombolGelap("\u25C0",true);
        private final TombolGelap btnNext=new TombolGelap("\u25B6",true);

        DialogGrafik(java.awt.Window parent,Connection kon,String namaRS,int tahun){
            super(parent,"Grafik Rekap Kunjungan Rawat Inap",java.awt.Dialog.ModalityType.APPLICATION_MODAL);
            this.kon=kon;
            this.namaRS=namaRS;
            PanelGelap root=new PanelGelap();
            setContentPane(root);

            // header
            JPanel header=new JPanel(new BorderLayout(0,12));
            header.setOpaque(false);
            header.setBorder(BorderFactory.createEmptyBorder(18,28,12,28));
            JLabel judul=new JLabel("REKAP KUNJUNGAN PASIEN RAWAT INAP");
            judul.setFont(new Font("SansSerif",Font.BOLD,22));
            judul.setForeground(Color.WHITE);
            lblSub.setFont(new Font("SansSerif",Font.PLAIN,12));
            lblSub.setForeground(new Color(160,200,220));
            JPanel tj=new JPanel(new java.awt.GridLayout(2,1,0,2));
            tj.setOpaque(false);
            tj.add(judul);
            tj.add(lblSub);
            header.add(tj,BorderLayout.NORTH);
            JPanel baris=new JPanel(new java.awt.GridLayout(1,5,12,0));
            baris.setOpaque(false);
            baris.setPreferredSize(new Dimension(100,86));
            for(int k=0;k<5;k++){
                kpi[k]=new KartuKpi();
                baris.add(kpi[k]);
            }
            header.add(baris,BorderLayout.CENTER);
            root.add(header,BorderLayout.NORTH);

            // kontrol + isi
            JPanel tengah=new JPanel(new BorderLayout(0,10));
            tengah.setOpaque(false);
            tengah.setBorder(BorderFactory.createEmptyBorder(0,28,0,28));
            JPanel kontrol=new JPanel(new BorderLayout());
            kontrol.setOpaque(false);
            JPanel kiriP=new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,6,0));
            kiriP.setOpaque(false);
            kiriP.add(label("TAMPILAN"));
            kiriP.add(tbGrafik);
            kiriP.add(tbTabel);
            kiriP.add(javax.swing.Box.createHorizontalStrut(18));
            kiriP.add(label("DATA GRAFIK"));
            kiriP.add(tbGab);
            kiriP.add(tbRj);
            kiriP.add(tbIgd);
            JPanel kananP=new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT,6,0));
            kananP.setOpaque(false);
            kananP.add(label("TAHUN"));
            kananP.add(btnPrev);
            lblTahun.setFont(new Font("SansSerif",Font.BOLD,16));
            lblTahun.setForeground(Color.WHITE);
            lblTahun.setPreferredSize(new Dimension(60,28));
            kananP.add(lblTahun);
            kananP.add(btnNext);
            kontrol.add(kiriP,BorderLayout.WEST);
            kontrol.add(kananP,BorderLayout.EAST);
            tengah.add(kontrol,BorderLayout.NORTH);
            kartu.setOpaque(false);
            panelTabel.setOpaque(false);
            kartu.add(grafik,"grafik");
            kartu.add(panelTabel,"tabel");
            tengah.add(kartu,BorderLayout.CENTER);
            root.add(tengah,BorderLayout.CENTER);

            // footer
            JPanel footer=new JPanel(new BorderLayout());
            footer.setOpaque(false);
            footer.setBorder(BorderFactory.createEmptyBorder(10,28,14,28));
            JLabel hint=new JLabel("Arahkan kursor ke batang untuk melihat rincian  \u2022  Esc untuk menutup");
            hint.setFont(new Font("SansSerif",Font.PLAIN,11));
            hint.setForeground(new Color(130,160,190));
            footer.add(hint,BorderLayout.WEST);
            JPanel tombol=new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT,8,0));
            tombol.setOpaque(false);
            TombolGelap ulang=new TombolGelap("Ulangi Animasi",true);
            TombolGelap simpan=new TombolGelap("Simpan Gambar (PNG)",true);
            TombolGelap tutup=new TombolGelap("Tutup",true);
            tombol.add(ulang);
            tombol.add(simpan);
            tombol.add(tutup);
            footer.add(tombol,BorderLayout.EAST);
            root.add(footer,BorderLayout.SOUTH);

            javax.swing.ButtonGroup g1=new javax.swing.ButtonGroup();
            g1.add(tbGrafik);
            g1.add(tbTabel);
            tbGrafik.setSelected(true);
            javax.swing.ButtonGroup g2=new javax.swing.ButtonGroup();
            g2.add(tbGab);
            g2.add(tbRj);
            g2.add(tbIgd);
            tbGab.setSelected(true);

            tbGrafik.addActionListener(new java.awt.event.ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e){ gantiTampilan(true); }
            });
            tbTabel.addActionListener(new java.awt.event.ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e){ gantiTampilan(false); }
            });
            tbGab.addActionListener(new java.awt.event.ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e){ pilihGrup=0; perbaruiGrafik(); }
            });
            tbRj.addActionListener(new java.awt.event.ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e){ pilihGrup=1; perbaruiGrafik(); }
            });
            tbIgd.addActionListener(new java.awt.event.ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e){ pilihGrup=2; perbaruiGrafik(); }
            });
            btnPrev.addActionListener(new java.awt.event.ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e){ muat(DialogGrafik.this.tahun-1); }
            });
            btnNext.addActionListener(new java.awt.event.ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e){ muat(DialogGrafik.this.tahun+1); }
            });
            ulang.addActionListener(new java.awt.event.ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e){ if(lihatGrafik){ grafik.mulaiAnimasi(); } }
            });
            simpan.addActionListener(new java.awt.event.ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e){ simpanGambar(); }
            });
            java.awt.event.ActionListener aksiTutup=new java.awt.event.ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e){ dispose(); }
            };
            tutup.addActionListener(aksiTutup);
            getRootPane().registerKeyboardAction(aksiTutup,KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);

            setSize(1260,880);
            setMinimumSize(new Dimension(1000,740));
            setLocationRelativeTo(parent);
            muat(tahun);
        }

        private JLabel label(String t){
            JLabel l=new JLabel(t);
            l.setFont(new Font("SansSerif",Font.BOLD,10));
            l.setForeground(new Color(140,165,200));
            return l;
        }

        private void gantiTampilan(boolean grafikDipilih){
            lihatGrafik=grafikDipilih;
            ((java.awt.CardLayout)kartu.getLayout()).show(kartu,grafikDipilih?"grafik":"tabel");
            tbGab.setEnabled(grafikDipilih);
            tbRj.setEnabled(grafikDipilih);
            tbIgd.setEnabled(grafikDipilih);
            if(grafikDipilih){
                grafik.mulaiAnimasi();
            }
        }

        private void muat(int t){
            java.time.LocalDate hari=java.time.LocalDate.now();
            if(t>hari.getYear()||t<2000){
                return;
            }
            tahun=t;
            lblTahun.setText(String.valueOf(t));
            a1=java.time.LocalDate.of(t,1,1);
            a2=(t==hari.getYear())?hari:java.time.LocalDate.of(t,12,31);
            b1=a1.minusYears(1);
            b2=a2.minusYears(1);
            data=new int[2][2][2][12];
            PreparedStatement ps=null;
            ResultSet rs=null;
            try{
                ps=kon.prepareStatement(
                    "select year(kamar_inap.tgl_keluar) as thn,month(kamar_inap.tgl_keluar) as bln,"+
                    "if(reg_periksa.kd_poli=?,1,0) as grp,if(penjab.png_jawab=?,0,1) as jns,count(distinct reg_periksa.no_rawat) as jml "+
                    "from reg_periksa inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "inner join kamar_inap on reg_periksa.no_rawat=kamar_inap.no_rawat "+
                    "where reg_periksa.stts<>'Batal' and kamar_inap.stts_pulang<>'Pindah Kamar' "+
                    "and ((kamar_inap.tgl_keluar between ? and ?) or (kamar_inap.tgl_keluar between ? and ?)) "+
                    "group by thn,bln,grp,jns");
                ps.setString(1,KD_POLI_IGD);
                ps.setString(2,PJ_BPJS);
                ps.setString(3,a1.toString());
                ps.setString(4,a2.toString());
                ps.setString(5,b1.toString());
                ps.setString(6,b2.toString());
                rs=ps.executeQuery();
                while(rs.next()){
                    int idxTahun=(rs.getInt("thn")==t)?0:1;
                    data[rs.getInt("grp")][idxTahun][rs.getInt("jns")][rs.getInt("bln")-1]+=rs.getInt("jml");
                }
            }catch(Exception ex){
                System.out.println("laporan.DlgKunjunganRalan.DialogGrafik.muat() : "+ex);
                JOptionPane.showMessageDialog(this,"Gagal mengambil data grafik : "+ex.getMessage());
            }finally{
                try{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps!=null){
                        ps.close();
                    }
                }catch(Exception ignore){
                }
            }
            segarkan();
        }

        private int tot(int g,int y){
            return jumlah(data[g][y][0])+jumlah(data[g][y][1]);
        }

        private int[][] seri(int sel,int y){
            int[][] r=new int[2][12];
            for(int g=0;g<2;g++){
                if(sel==0||sel==g+1){
                    for(int j=0;j<2;j++){
                        for(int m=0;m<12;m++){
                            r[j][m]+=data[g][y][j][m];
                        }
                    }
                }
            }
            return r;
        }

        private void perbaruiGrafik(){
            String[] nm={"Gabungan","Rawat Jalan","IGD"};
            grafik.setData(tahun,seri(pilihGrup,0),seri(pilihGrup,1),nm[pilihGrup]);
            grafik.mulaiAnimasi();
        }

        private void segarkan(){
            java.time.format.DateTimeFormatter df=java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
            lblSub.setText("Periode : "+a1.format(df)+" s.d. "+a2.format(df)+"   \u2022   dibanding "+b1.format(df)+" s.d. "+b2.format(df)
                +"   \u2022   pasien rawat inap non batal berdasarkan tanggal keluar   \u2022   asal pasien : Rawat Jalan / IGD   \u2022   dibagi menurut jenis bayar");
            Color netral=new Color(170,185,215);
            int[][] gab=seri(0,0);
            int tIni=tot(0,0)+tot(1,0),tLalu=tot(0,1)+tot(1,1);
            kpi[0].set("TOTAL RAWAT INAP "+tahun,fmt(tIni),"BPJS "+fmt(jumlah(gab[0]))+"  \u2022  Reguler "+fmt(jumlah(gab[1])),netral);
            kpi[1].set("TAHUN "+(tahun-1)+" (PERIODE SAMA)",fmt(tLalu),delta(tIni,tLalu),tIni>=tLalu?new Color(102,187,106):new Color(239,83,80));
            for(int g=0;g<2;g++){
                kpi[2+g].set(g==0?"DARI RAWAT JALAN":"DARI IGD",fmt(tot(g,0)),"BPJS "+fmt(jumlah(data[g][0][0]))+"  \u2022  Reguler "+fmt(jumlah(data[g][0][1])),netral);
            }
            int mTop=0,vTop=-1;
            for(int m=0;m<12;m++){
                int v=gab[0][m]+gab[1][m];
                if(v>vTop){
                    vTop=v;
                    mTop=m;
                }
            }
            if(vTop>0){
                kpi[4].set("BULAN TERTINGGI",NAMA_BULAN[mTop],fmt(vTop)+" pasien",new Color(255,213,79));
            }else{
                kpi[4].set("BULAN TERTINGGI","-","Belum ada data",netral);
            }
            btnPrev.setEnabled(tahun>2000);
            btnNext.setEnabled(tahun<java.time.LocalDate.now().getYear());

            panelTabel.removeAll();
            panelTabel.add(buatTabel(0,tahun,a1,a2));
            panelTabel.add(buatTabel(1,tahun-1,b1,b2));
            panelTabel.revalidate();
            panelTabel.repaint();
            perbaruiGrafik();
        }

        private String tglIndo(java.time.LocalDate d,boolean denganTahun){
            return d.getDayOfMonth()+" "+NAMA_BULAN[d.getMonthValue()-1].toUpperCase()+(denganTahun?" "+d.getYear():"");
        }

        private JLabel sel(JPanel p,String t,int x,int y,int w,int h,Color bg,Color fg,boolean tebal,int rata,double wx){
            JLabel l=new JLabel(t,rata);
            l.setOpaque(true);
            l.setBackground(bg);
            l.setForeground(fg);
            l.setFont(new Font("SansSerif",tebal?Font.BOLD:Font.PLAIN,12));
            l.setBorder(BorderFactory.createEmptyBorder(5,9,5,9));
            l.setPreferredSize(new Dimension(w>1?150:(x==0?120:78),h>1?54:27));
            java.awt.GridBagConstraints c=new java.awt.GridBagConstraints();
            c.gridx=x;
            c.gridy=y;
            c.gridwidth=w;
            c.gridheight=h;
            c.fill=java.awt.GridBagConstraints.BOTH;
            c.weightx=wx;
            c.insets=new java.awt.Insets(0,0,1,1);
            p.add(l,c);
            return l;
        }

        /** y=0 tahun ini, y=1 tahun lalu. Tampilan mengikuti contoh rekap: Rawat Jalan (BPJS,Reguler) dan IGD (BPJS,Reguler). */
        private JPanel buatTabel(int y,int thn,java.time.LocalDate d1,java.time.LocalDate d2){
            JPanel kertas=new JPanel(new BorderLayout(0,10));
            kertas.setBackground(Color.WHITE);
            kertas.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
            JPanel jdl=new JPanel(new java.awt.GridLayout(4,1));
            jdl.setOpaque(false);
            String[] t={"REKAP KUNJUNGAN PASIEN","RAWAT INAP (ASAL : RAWAT JALAN DAN IGD)",namaRS.toUpperCase(),tglIndo(d1,false)+" - "+tglIndo(d2,true)};
            for(String s:t){
                JLabel l=new JLabel(s,SwingConstants.CENTER);
                l.setFont(new Font("SansSerif",Font.PLAIN,13));
                l.setForeground(new Color(30,30,30));
                jdl.add(l);
            }
            kertas.add(jdl,BorderLayout.NORTH);

            JPanel grid=new JPanel(new java.awt.GridBagLayout());
            grid.setBackground(Color.BLACK);
            grid.setBorder(BorderFactory.createMatteBorder(1,1,0,0,Color.BLACK));
            Color hijau=new Color(255,217,102);
            Color hitam=Color.black;
            sel(grid,"BULAN",0,0,1,2,hijau,hitam,false,SwingConstants.CENTER,1.5);
            sel(grid,"RAWAT JALAN",1,0,2,1,hijau,hitam,false,SwingConstants.CENTER,0);
            sel(grid,"IGD",3,0,2,1,hijau,hitam,false,SwingConstants.CENTER,0);
            String[] sub={"BPJS","REGULER","BPJS","REGULER"};
            for(int k=0;k<4;k++){
                sel(grid,sub[k],1+k,1,1,1,Color.WHITE,hitam,false,SwingConstants.CENTER,1);
            }
            int[] total=new int[4];
            for(int m=0;m<12;m++){
                sel(grid,NAMA_BULAN[m].toUpperCase(),0,2+m,1,1,Color.WHITE,hitam,false,SwingConstants.LEFT,1.5);
                for(int k=0;k<4;k++){
                    int v=data[k/2][y][k%2][m];
                    total[k]+=v;
                    sel(grid,String.valueOf(v),1+k,2+m,1,1,Color.WHITE,hitam,false,SwingConstants.RIGHT,1);
                }
            }
            sel(grid,"TOTAL",0,14,1,1,hitam,Color.WHITE,true,SwingConstants.LEFT,1.5);
            for(int k=0;k<4;k++){
                sel(grid,String.valueOf(total[k]),1+k,14,1,1,hitam,Color.WHITE,true,SwingConstants.RIGHT,1);
            }
            JPanel isi=new JPanel(new BorderLayout(0,6));
            isi.setOpaque(false);
            JLabel pink=new JLabel("RAWAT INAP");
            pink.setOpaque(true);
            pink.setBackground(new Color(204,102,163));
            pink.setForeground(Color.BLACK);
            pink.setFont(new Font("SansSerif",Font.PLAIN,12));
            pink.setBorder(BorderFactory.createEmptyBorder(5,9,5,40));
            JPanel pinkWrap=new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,0,0));
            pinkWrap.setOpaque(false);
            pinkWrap.add(pink);
            isi.add(pinkWrap,BorderLayout.NORTH);
            isi.add(grid,BorderLayout.CENTER);
            kertas.add(isi,BorderLayout.CENTER);
            return kertas;
        }

        private void simpanGambar(){
            JFileChooser fc=new JFileChooser();
            fc.setSelectedFile(new java.io.File("rekap_kunjungan_"+tahun+(lihatGrafik?"_grafik":"_tabel")+".png"));
            if(fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION){
                return;
            }
            java.io.File f=fc.getSelectedFile();
            if(!f.getName().toLowerCase().endsWith(".png")){
                f=new java.io.File(f.getAbsolutePath()+".png");
            }
            try{
                grafik.selesaikanAnimasi();
                java.awt.Component c=lihatGrafik?(java.awt.Component)grafik:(java.awt.Component)panelTabel;
                BufferedImage img=new BufferedImage(Math.max(1,c.getWidth()),Math.max(1,c.getHeight()),BufferedImage.TYPE_INT_RGB);
                Graphics2D g=img.createGraphics();
                g.setColor(new Color(13,22,48));
                g.fillRect(0,0,img.getWidth(),img.getHeight());
                c.printAll(g);
                g.dispose();
                javax.imageio.ImageIO.write(img,"png",f);
                JOptionPane.showMessageDialog(this,"Gambar tersimpan :\n"+f.getAbsolutePath());
            }catch(Exception ex){
                JOptionPane.showMessageDialog(this,"Gagal menyimpan gambar : "+ex.getMessage());
            }
        }
    }

}
