/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgLhtBiaya.java
 *
 * Created on 12 Jul 10, 16:21:34
 */

package simrskhanza;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import rekammedis.RMRiwayatPerawatan;
import simrskhanza.DlgCariPasien;

/**
 *
 * @author windiarto
 */
public final class DlgRiwayatIO extends javax.swing.JDialog {    
    private validasi Valid=new validasi();    
    private final sekuel Sequel=new sekuel();
//    private final DefaultTableModel tabModeRegistrasi;
    private PreparedStatement ps,ps2;
    private ResultSet rs,rs2,rs3,rs4;
    private Connection koneksi=koneksiDB.condb();
    private int i=0,urut=0,w=0,s=0,urutdpjp=0;
    private double biayaperawatan=0;
    private String kddpjp="",dpjp="",dokterrujukan="",polirujukan="",keputusan="",ke1="",ke2="",ke3="",ke4="",ke5="",ke6="",file="";
    private StringBuilder htmlContent;
    private HttpClient http = new HttpClient();
    private GetMethod get;
    private DlgCariPasien pasien=new DlgCariPasien(null,true);
    private String htmlIsiTabel="";   // hanya isi baris tabel; HTML cetak dirakit saat tombol Cetak ditekan
    // Header judul pasien: panel Swing biasa (tinggi mengikuti isi sehingga tidak bisa terpotong/tertimpa),
    // dipasang di luar Scroll supaya tetap diam saat tabel di-scroll
    private javax.swing.JPanel HeaderPasien;
    private javax.swing.JLabel lblJudul,lblInfo;
    private javax.swing.JLabel[] kartuNilai=new javax.swing.JLabel[4], kartuSub=new javax.swing.JLabel[4];
    private String noRawatSekarang=""; // nomor rawat yang sedang ditampilkan (kosong = rawat inap aktif terakhir)

    // =====================================================================================
    //  DEFINISI KOLOM TABEL RIWAYAT IO
    //  Header beku (panel di atas scroll) dan baris isi memakai lebar persen yang SAMA
    //  supaya selalu sejajar. Total lebar = 100.
    //  Tipe kolom: 0=umum, 1=input, 2=output, 3=balance, 4=tindakan keperawatan
    // =====================================================================================
    private static final String[] KOLOM_LABEL={
        "Tanggal\n& Jam","Infus","Tranfusi","Minum","Selang\nHidung",
        "Urine","Selang\nHidung","BAB","Drain\nKiri","Drain\nKanan",
        "Balance\nCairan",
        "Tusukan\nInfus","Mandi","Aktifitas","Latihan\nPernafasan","Mengganti\nAlat Tenun",
        "Keterangan","Nama\nPerawat"};
    private static final int[] KOLOM_LEBAR={13,4,4,4,4,4,4,3,3,3,5,5,4,5,6,6,14,8};
    private static final int[] KOLOM_TIPE ={0,1,1,1,1,2,2,2,2,2,3,4,4,4,4,4,0,0};
    private static final String[] GRUP_NAMA ={"","INPUT","OUTPUT","","TINDAKAN KEPERAWATAN"};
    // ---- PALET TEMA (ubah di sini jika ingin ganti warna) ----
    private static final String WARNA_HEADER="#1f3b5c";   // semua kolom header tabel memakai warna yang sama
    private static final String[] WARNA_KUAT ={"#1f3b5c","#2b6cb0","#c24141","#d9912a","#2a9d8f"};
    // warna baris TOTAL per tipe kolom: 0=umum, 1=input, 2=output, 3=balance, 4=tindakan (versi soft)
    private static final String[] WARNA_TOTAL     ={"#e8eef9","#e1f4e3","#fde6e8","#fdeccf","#e8eef9"};
    private static final String[] WARNA_TEKS_TOTAL={"#475569","#256a35","#9b2c3a","#8a5a14","#475569"};
    private static final int TINGGI_HEADER=52;
    private static final String ATRIBUT_TABEL="width='100%' border='0' cellspacing='0' cellpadding='5'";

    private static final String CSS_TABEL=
        "td.vacc2{background-color:#d9912a}"+
        "td.vh{background-color:#1f3b5c;color:#ffffff;font: bold 12px tahoma}"+
        "td.vtgl{background-color:#1f3b5c;color:#bcd2ee;font: 11px tahoma}"+
        "td.vchip{background-color:#1f3b5c;color:#ffffff;font: 11px tahoma}"+
        "td.ranap{background-color:#d9912a;color:#ffffff;font: bold 10px tahoma}"+
        "td.hari{background-color:#e8eff8;color:#1f3b5c;font: bold 11px tahoma;border-bottom:1px solid #d5dde8}"+
        "td.kosong{background-color:#f8fafc;color:#94a3b8;font: 11px tahoma;border-bottom:1px solid #e2e8f0}"+
        ".za td{font: 11px tahoma;background: #ffffff;color:#1e293b;border-right: 1px solid #edf1f6;border-bottom: 1px solid #e3e9f0;}"+
        ".zb td{font: 11px tahoma;background: #f5f8fc;color:#1e293b;border-right: 1px solid #edf1f6;border-bottom: 1px solid #e3e9f0;}"+
        ".tot td{font: bold 12px tahoma;background: #f6f8fb;color:#475569;border-right: 1px solid #ffffff;border-bottom: 1px solid #d5dde8;}"+
        ".gap td{font: 4px tahoma;background: #f1f5f9;color:#f1f5f9;}";

    /** Creates new form DlgLhtBiaya
     * @param parent
     * @param modal */
    public DlgRiwayatIO(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);
        
        
        
        pasien.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(pasien.getTable().getSelectedRow()!= -1){                   
                    NoRM.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),0).toString());
                    NmPasien.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),1).toString());
                    Jk.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),3).toString());
                    TempatLahir.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),4).toString());
                    TanggalLahir.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),5).toString());
                    IbuKandung.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),6).toString());
                    Alamat.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),7).toString());
                    GD.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),8).toString());
                    StatusNikah.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),10).toString());
                    Agama.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),11).toString());
                    Pendidikan.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),15).toString());
                    Bahasa.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),26).toString());
                    CacatFisik.setText(pasien.getTable().getValueAt(pasien.getTable().getSelectedRow(),32).toString());
                }    
                NoRM.requestFocus();
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
        
        pasien.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    pasien.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        // StyleSheet PRIBADI untuk dialog ini: aturan CSS tidak ditumpuk ke stylesheet global Swing
        // (yang dipakai bersama seluruh form Khanza) dan tidak ikut berubah oleh form lain.
        HTMLEditorKit kit = new HTMLEditorKit() {
            private StyleSheet gayaPribadi;
            @Override
            public StyleSheet getStyleSheet() {
                if (gayaPribadi == null) {
                    gayaPribadi = new StyleSheet();
                    gayaPribadi.addStyleSheet(super.getStyleSheet());
                }
                return gayaPribadi;
            }
        };
        LoadHTMLRiwayatIO.setEditorKit(kit);
        
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(".isi2 td{border-right: 1px solid #e2e7dd;font: bold 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #add8e6;color:#323232;}.isi a{text-decoration:none;color:#8b9b95;padding:0 0 0 0px;font-family: Tahoma;font-size: 8.5px;border: white;font-weight: bold;}");
        styleSheet.addRule(".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}.isi a{text-decoration:none;color:#8b9b95;padding:0 0 0 0px;font-family: Tahoma;font-size: 8.5px;border: white;}");
        styleSheet.addRule(CSS_TABEL);
        

        Document doc = kit.createDefaultDocument();
        LoadHTMLRiwayatIO.setDocument(doc);
        LoadHTMLRiwayatIO.setEditable(false);
        LoadHTMLRiwayatIO.addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
              Desktop desktop = Desktop.getDesktop();
              try {
                desktop.browse(e.getURL().toURI());
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
        });
        
        NoRM.setDocument(new batasInput((byte)20).getKata(NoRM));
        NoRawat.setDocument(new batasInput((byte)20).getKata(NoRawat));
        
        terapkanTampilan();
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        Pekerjaan = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        panelGlass5 = new widget.panelisi();
        R1 = new widget.RadioButton();
        R2 = new widget.RadioButton();
        R3 = new widget.RadioButton();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        R4 = new widget.RadioButton();
        NoRawat = new widget.TextBox();
        BtnCari1 = new widget.Button();
        label19 = new widget.Label();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        LoadHTMLRiwayatIO = new widget.editorpane();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.panelisi();
        label17 = new widget.Label();
        NoRM = new widget.TextBox();
        NmPasien = new widget.TextBox();
        BtnPasien = new widget.Button();
        label20 = new widget.Label();
        Jk = new widget.TextBox();
        label21 = new widget.Label();
        TempatLahir = new widget.TextBox();
        label22 = new widget.Label();
        Alamat = new widget.TextBox();
        label23 = new widget.Label();
        GD = new widget.TextBox();
        label24 = new widget.Label();
        IbuKandung = new widget.TextBox();
        TanggalLahir = new widget.TextBox();
        label25 = new widget.Label();
        Agama = new widget.TextBox();
        StatusNikah = new widget.TextBox();
        label26 = new widget.Label();
        Pendidikan = new widget.TextBox();
        label27 = new widget.Label();
        label28 = new widget.Label();
        Bahasa = new widget.TextBox();
        label29 = new widget.Label();
        CacatFisik = new widget.TextBox();

        Pekerjaan.setEditable(false);
        Pekerjaan.setName("Pekerjaan"); // NOI18N
        Pekerjaan.setPreferredSize(new java.awt.Dimension(100, 23));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Riwayat IO Pasien ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass5.setName("panelGlass5"); // NOI18N
        panelGlass5.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        R1.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.pink));
        buttonGroup1.add(R1);
        R1.setSelected(true);
        R1.setText("5 Riwayat Terakhir");
        R1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        R1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R1.setName("R1"); // NOI18N
        R1.setPreferredSize(new java.awt.Dimension(120, 23));
        panelGlass5.add(R1);

        R2.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.pink));
        buttonGroup1.add(R2);
        R2.setText("Semua Riwayat");
        R2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        R2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R2.setName("R2"); // NOI18N
        R2.setPreferredSize(new java.awt.Dimension(104, 23));
        panelGlass5.add(R2);

        R3.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.pink));
        buttonGroup1.add(R3);
        R3.setText("Tanggal :");
        R3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        R3.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R3.setName("R3"); // NOI18N
        R3.setPreferredSize(new java.awt.Dimension(75, 23));
        panelGlass5.add(R3);

        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.setPreferredSize(new java.awt.Dimension(90, 23));
        Tgl1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tgl1KeyPressed(evt);
            }
        });
        panelGlass5.add(Tgl1);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(25, 23));
        panelGlass5.add(label18);

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.setPreferredSize(new java.awt.Dimension(90, 23));
        Tgl2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tgl2KeyPressed(evt);
            }
        });
        panelGlass5.add(Tgl2);

        R4.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.pink));
        buttonGroup1.add(R4);
        R4.setText("Nomor :");
        R4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        R4.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R4.setName("R4"); // NOI18N
        R4.setPreferredSize(new java.awt.Dimension(67, 23));
        panelGlass5.add(R4);

        NoRawat.setName("NoRawat"); // NOI18N
        NoRawat.setPreferredSize(new java.awt.Dimension(135, 23));
        NoRawat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoRawatKeyPressed(evt);
            }
        });
        panelGlass5.add(NoRawat);

        BtnCari1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari1.setMnemonic('2');
        BtnCari1.setToolTipText("Alt+2");
        BtnCari1.setName("BtnCari1"); // NOI18N
        BtnCari1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCari1ActionPerformed(evt);
            }
        });
        panelGlass5.add(BtnCari1);

        label19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(15, 23));
        panelGlass5.add(label19);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        panelGlass5.add(BtnPrint);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setGlassColor(new java.awt.Color(80, 129, 227));
        BtnKeluar.setLabel("Keluar");
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

        TabRawat.setBackground(new java.awt.Color(255, 255, 254));
        TabRawat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        internalFrame2.setBackground(new java.awt.Color(235, 255, 235));
        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        LoadHTMLRiwayatIO.setBorder(null);
        LoadHTMLRiwayatIO.setName("LoadHTMLRiwayatIO"); // NOI18N
        Scroll.setViewportView(LoadHTMLRiwayatIO);

        internalFrame2.add(Scroll, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Riwayat IO", internalFrame2);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);
        TabRawat.getAccessibleContext().setAccessibleName("Riwayat IO");

        PanelInput.setBackground(new java.awt.Color(255, 255, 255));
        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.setMnemonic('M');
        ChkInput.setSelected(true);
        ChkInput.setText(".: Tampilkan/Sembunyikan Data Pasien");
        ChkInput.setBorderPainted(true);
        ChkInput.setBorderPaintedFlat(true);
        ChkInput.setFocusable(false);
        ChkInput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput.setName("ChkInput"); // NOI18N
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
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

        label17.setText("Pasien :");
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(55, 23));
        FormInput.add(label17);
        label17.setBounds(5, 10, 55, 23);

        NoRM.setName("NoRM"); // NOI18N
        NoRM.setPreferredSize(new java.awt.Dimension(100, 23));
        NoRM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoRMKeyPressed(evt);
            }
        });
        FormInput.add(NoRM);
        NoRM.setBounds(64, 10, 100, 23);

        NmPasien.setEditable(false);
        NmPasien.setName("NmPasien"); // NOI18N
        NmPasien.setPreferredSize(new java.awt.Dimension(220, 23));
        FormInput.add(NmPasien);
        NmPasien.setBounds(167, 10, 220, 23);

        BtnPasien.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPasien.setMnemonic('3');
        BtnPasien.setToolTipText("Alt+3");
        BtnPasien.setName("BtnPasien"); // NOI18N
        BtnPasien.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPasienActionPerformed(evt);
            }
        });
        BtnPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPasienKeyPressed(evt);
            }
        });
        FormInput.add(BtnPasien);
        BtnPasien.setBounds(390, 10, 28, 23);

        label20.setText("J.K. :");
        label20.setName("label20"); // NOI18N
        label20.setPreferredSize(new java.awt.Dimension(55, 23));
        FormInput.add(label20);
        label20.setBounds(436, 10, 30, 23);

        Jk.setEditable(false);
        Jk.setName("Jk"); // NOI18N
        Jk.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(Jk);
        Jk.setBounds(470, 10, 40, 23);

        label21.setText("Tempat & Tgl.Lahir :");
        label21.setName("label21"); // NOI18N
        label21.setPreferredSize(new java.awt.Dimension(55, 23));
        FormInput.add(label21);
        label21.setBounds(523, 10, 110, 23);

        TempatLahir.setEditable(false);
        TempatLahir.setName("TempatLahir"); // NOI18N
        TempatLahir.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(TempatLahir);
        TempatLahir.setBounds(637, 10, 140, 23);

        label22.setText("Alamat :");
        label22.setName("label22"); // NOI18N
        label22.setPreferredSize(new java.awt.Dimension(55, 23));
        FormInput.add(label22);
        label22.setBounds(5, 40, 55, 23);

        Alamat.setEditable(false);
        Alamat.setName("Alamat"); // NOI18N
        Alamat.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(Alamat);
        Alamat.setBounds(64, 40, 354, 23);

        label23.setText("G.D. :");
        label23.setName("label23"); // NOI18N
        label23.setPreferredSize(new java.awt.Dimension(55, 23));
        FormInput.add(label23);
        label23.setBounds(436, 40, 30, 23);

        GD.setEditable(false);
        GD.setName("GD"); // NOI18N
        GD.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(GD);
        GD.setBounds(470, 40, 40, 23);

        label24.setText("Nama Ibu Kandung :");
        label24.setName("label24"); // NOI18N
        label24.setPreferredSize(new java.awt.Dimension(55, 23));
        FormInput.add(label24);
        label24.setBounds(523, 40, 110, 23);

        IbuKandung.setEditable(false);
        IbuKandung.setName("IbuKandung"); // NOI18N
        IbuKandung.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(IbuKandung);
        IbuKandung.setBounds(637, 40, 225, 23);

        TanggalLahir.setEditable(false);
        TanggalLahir.setName("TanggalLahir"); // NOI18N
        TanggalLahir.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(TanggalLahir);
        TanggalLahir.setBounds(779, 10, 83, 23);

        label25.setText("Agama :");
        label25.setName("label25"); // NOI18N
        label25.setPreferredSize(new java.awt.Dimension(55, 23));
        FormInput.add(label25);
        label25.setBounds(5, 70, 55, 23);

        Agama.setEditable(false);
        Agama.setName("Agama"); // NOI18N
        Agama.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(Agama);
        Agama.setBounds(64, 70, 100, 23);

        StatusNikah.setEditable(false);
        StatusNikah.setName("StatusNikah"); // NOI18N
        StatusNikah.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(StatusNikah);
        StatusNikah.setBounds(245, 70, 100, 23);

        label26.setText("Stts.Nikah :");
        label26.setName("label26"); // NOI18N
        label26.setPreferredSize(new java.awt.Dimension(55, 23));
        FormInput.add(label26);
        label26.setBounds(176, 70, 65, 23);

        Pendidikan.setEditable(false);
        Pendidikan.setName("Pendidikan"); // NOI18N
        Pendidikan.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(Pendidikan);
        Pendidikan.setBounds(429, 70, 80, 23);

        label27.setText("Pendidikan :");
        label27.setName("label27"); // NOI18N
        label27.setPreferredSize(new java.awt.Dimension(55, 23));
        FormInput.add(label27);
        label27.setBounds(355, 70, 70, 23);

        label28.setText("Bahasa :");
        label28.setName("label28"); // NOI18N
        label28.setPreferredSize(new java.awt.Dimension(55, 23));
        FormInput.add(label28);
        label28.setBounds(520, 70, 50, 23);

        Bahasa.setEditable(false);
        Bahasa.setName("Bahasa"); // NOI18N
        Bahasa.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(Bahasa);
        Bahasa.setBounds(574, 70, 100, 23);

        label29.setText("Cacat Fisik :");
        label29.setName("label29"); // NOI18N
        label29.setPreferredSize(new java.awt.Dimension(55, 23));
        FormInput.add(label29);
        label29.setBounds(683, 70, 70, 23);

        CacatFisik.setEditable(false);
        CacatFisik.setName("CacatFisik"); // NOI18N
        CacatFisik.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(CacatFisik);
        CacatFisik.setBounds(757, 70, 105, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void NoRMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoRMKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isPasien();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            isPasien();
            BtnKeluar.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnPasienActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            isPasien();
            BtnPrint.requestFocus();
        }
}//GEN-LAST:event_NoRMKeyPressed

private void BtnPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPasienActionPerformed
    if(akses.getpasien()==true){
        pasien.isCek();
        pasien.emptTeks();
        pasien.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        pasien.setLocationRelativeTo(internalFrame1);
        pasien.setVisible(true);
    }   
}//GEN-LAST:event_BtnPasienActionPerformed

private void BtnPasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPasienKeyPressed
    //Valid.pindah(evt,Tgl2,TKd);
}//GEN-LAST:event_BtnPasienKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        if(NoRM.getText().trim().equals("")||NmPasien.getText().equals("")){
            JOptionPane.showMessageDialog(null,"Pasien masih kosong...!!!");
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            panggilLaporan(htmlIsiTabel.isEmpty()?LoadHTMLRiwayatIO.getText():bangunHtmlCetak());
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnPrintActionPerformed

    private void Tgl1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tgl1KeyPressed
        Valid.pindah(evt, BtnKeluar, Tgl2);
    }//GEN-LAST:event_Tgl1KeyPressed

    private void Tgl2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tgl2KeyPressed
        Valid.pindah(evt, Tgl1,NoRM);
    }//GEN-LAST:event_Tgl2KeyPressed

    private void BtnCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari1ActionPerformed
        if(NoRM.getText().trim().equals("")||NmPasien.getText().equals("")){
            JOptionPane.showMessageDialog(null,"Pasien masih kosong...!!!");
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            tampilriwayatIO();
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnCari1ActionPerformed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        BtnCari1ActionPerformed(null);
    }//GEN-LAST:event_TabRawatMouseClicked

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
      
    }//GEN-LAST:event_ChkInputActionPerformed

    private void NoRawatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoRawatKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCari1ActionPerformed(null);
        }
    }//GEN-LAST:event_NoRawatKeyPressed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,Tgl1,NoRM);}
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMRiwayatPerawatan dialog = new RMRiwayatPerawatan(new javax.swing.JFrame(), true);
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
    private widget.TextBox Agama;
    private widget.TextBox Alamat;
    private widget.TextBox Bahasa;
    private widget.Button BtnCari1;
    private widget.Button BtnKeluar;
    private widget.Button BtnPasien;
    private widget.Button BtnPrint;
    private widget.TextBox CacatFisik;
    private widget.CekBox ChkInput;
    private widget.panelisi FormInput;
    private widget.TextBox GD;
    private widget.TextBox IbuKandung;
    private widget.TextBox Jk;
    private widget.editorpane LoadHTMLRiwayatIO;
    private widget.TextBox NmPasien;
    private widget.TextBox NoRM;
    private widget.TextBox NoRawat;
    private javax.swing.JPanel PanelInput;
    private widget.TextBox Pekerjaan;
    private widget.TextBox Pendidikan;
    private widget.RadioButton R1;
    private widget.RadioButton R2;
    private widget.RadioButton R3;
    private widget.RadioButton R4;
    private widget.ScrollPane Scroll;
    private widget.TextBox StatusNikah;
    private javax.swing.JTabbedPane TabRawat;
    private widget.TextBox TanggalLahir;
    private widget.TextBox TempatLahir;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private javax.swing.ButtonGroup buttonGroup1;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.Label label17;
    private widget.Label label18;
    private widget.Label label19;
    private widget.Label label20;
    private widget.Label label21;
    private widget.Label label22;
    private widget.Label label23;
    private widget.Label label24;
    private widget.Label label25;
    private widget.Label label26;
    private widget.Label label27;
    private widget.Label label28;
    private widget.Label label29;
    private widget.panelisi panelGlass5;
    // End of variables declaration//GEN-END:variables

    public void setNoRm(String norm,String nama) {
        setNoRm(norm,nama,"");
    }

    /** tampilkan hanya nomor rawat yang diberikan (rawat sekarang) */
    public void setNoRm(String norm,String nama,String norawat) {
        noRawatSekarang=(norawat==null)?"":norawat.trim();
        NoRM.setText(norm);
        NmPasien.setText(nama);
        isPasien();
        BtnCari1ActionPerformed(null);
    }

    private void isPasien() {
        try{
            ps=koneksi.prepareStatement(
                    "select pasien.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.tmp_lahir,pasien.tgl_lahir,pasien.agama,"+
                    "bahasa_pasien.nama_bahasa,cacat_fisik.nama_cacat,pasien.gol_darah,pasien.nm_ibu,pasien.stts_nikah,pasien.pnd, "+
                    "concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,pasien.pekerjaan "+
                    "from pasien inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                    "inner join cacat_fisik on cacat_fisik.id=pasien.cacat_fisik "+
                    "inner join kelurahan on pasien.kd_kel=kelurahan.kd_kel "+
                    "inner join kecamatan on pasien.kd_kec=kecamatan.kd_kec "+
                    "inner join kabupaten on pasien.kd_kab=kabupaten.kd_kab "+
                    "where pasien.no_rkm_medis=?");
            try {
                ps.setString(1,NoRM.getText().trim());
                rs=ps.executeQuery();
                if(rs.next()){
                    NoRM.setText(rs.getString("no_rkm_medis"));
                    NmPasien.setText(rs.getString("nm_pasien"));
                    Jk.setText(rs.getString("jk"));
                    TempatLahir.setText(rs.getString("tmp_lahir"));
                    TanggalLahir.setText(rs.getString("tgl_lahir"));
                    Alamat.setText(rs.getString("alamat"));
                    GD.setText(rs.getString("gol_darah"));
                    IbuKandung.setText(rs.getString("nm_ibu"));
                    Agama.setText(rs.getString("agama"));
                    StatusNikah.setText(rs.getString("stts_nikah"));
                    Pendidikan.setText(rs.getString("pnd"));
                    Bahasa.setText(rs.getString("nama_bahasa"));
                    CacatFisik.setText(rs.getString("nama_cacat"));
                    Pekerjaan.setText(rs.getString("pekerjaan"));
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
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }
    }
    
/* RIWAYAT IO FIX PENJUMLAHAN KESAMPING
 
private void tampilriwayatIO() {
    try {
        htmlContent = new StringBuilder();
        htmlContent.append("<tr class='isi2'>" +
                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='5%'>Tgl.Reg</td>" +
                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='8%'>No.Rawat</td>" +
                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='3%'>Status</td>" +
                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='84%'>Riwayat IO" +
                "<div style='margin-top:5px;'>" +
                "<table border='0' width='100%'>" +
                "<tr>" +
                "<td valign='middle' bgcolor='#FFFFF8' align='center' width='10%'>Tanggal & Jam</td>"+
                                 "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>Infus</td>"+
                                "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>Tranfusi</td>"+
                                "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>Minum</td>"+
                               "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>Selang Hidung</td>"+
                               "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>JUMLAH INPUT</td>"+
                               "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>Urine</td>"+
                               "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>Selang Hidung</td>"+
                                "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>BAB</td>"+
                                "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>DLL</td>"+
                               "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>JUMLAH OUTPUT</td>"+
                               "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>Tusukan Infus</td>"+
                               "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>Mandi</td>"+
                               "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>Aktifitas</td>"+
                               "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>Latihan Pernafasan</td>"+
                                "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>Mengganti Alat Tenun</td>"+
                                 "<td valign='middle' bgcolor='#FFFFF8' align='center' width='5%'>BALANCE CAIRAN</td>"+
                               "<td valign='middle' bgcolor='#FFFFF8' align='center' width='10%'>Keterangan</td>"+
                                "<td valign='middle' bgcolor='#FFFFF8' align='center' width='10%'>Nama Perawat</td>"+
                "</tr>" +
                "</table>" +
                "</div>" +
                "</td>" +
                "</tr>");

        ps = koneksi.prepareStatement("SELECT no_rawat, DATE_FORMAT(tgl_registrasi, '%d-%m-%Y') AS tgl_registrasi FROM reg_periksa WHERE no_rkm_medis = ? AND status_lanjut = 'Ranap' ORDER BY tgl_registrasi");
        ps.setString(1, NoRM.getText().trim());
        rs = ps.executeQuery();

        while (rs.next()) {
            htmlContent.append("<tr class='isi'>" +
                    "<td align='center'>" + rs.getString("tgl_registrasi") + "</td>" +
                    "<td align='center'>" + rs.getString("no_rawat") + "</td>" +
                    "<td align='center'>Ranap</td>" +
                    "<td align='center'><table width='100%' border='0'>");

            PreparedStatement ps2 = koneksi.prepareStatement("SELECT DATE_FORMAT(tgl_perawatan, '%d-%m-%Y') AS tgl_perawatan, jam_rawat,infus,tranfusi,minum,sh, jumlahmasuk,urine,drain,ngt,iwl,jumlahkeluar,"
                    + "                                       tusuk,mandi,aktifitas,nafas,tenun,bc,keterangan,petugas.nama FROM catatan_keseimbangan_cairan "
                    + "INNER JOIN petugas ON catatan_keseimbangan_cairan.nip = petugas.nip WHERE no_rawat = ? ORDER BY tgl_perawatan, jam_rawat");
            ps2.setString(1, rs.getString("no_rawat"));
            ResultSet rs2 = ps2.executeQuery();

            String lastTglPerawatan = "";
            int totalJumlahMasuk = 0;
            int totalJumlahKeluar = 0;
            int totalJumlahBC = 0;

            while (rs2.next()) {
                String currentTglPerawatan = rs2.getString("tgl_perawatan");
                int jumlahMasuk = rs2.getInt("jumlahmasuk");
                int jumlahKeluar = rs2.getInt("jumlahkeluar");
                int jumlahBC = rs2.getInt("bc");
                
                if (!currentTglPerawatan.equals(lastTglPerawatan) && !lastTglPerawatan.equals("")) {
                    htmlContent.append("<tr class='isi2'>" +
                            "<td colspan='5' align='center' style='font-weight: bold; background-color: #e1ebf9;'>TOTAL INPUT</td>" +
                            "<td align='center' style='font-weight: bold; background-color: #9ecaff; font-size: 11px;'>" + totalJumlahMasuk + "</td>" +
                           "<td colspan='4' align='center' style='font-weight: bold; background-color: #e1ebf9;'>TOTAL OUTPUT</td>" +
                            "<td align='center' style='font-weight: bold; background-color: #FFB6C1; font-size: 11px;'>" + totalJumlahKeluar + "</td>" +
                          "<td colspan='5' align='center' style='font-weight: bold; background-color: #e1ebf9;'>TOTAL BALANCE CAIRAN</td>" +
                            "<td align='center' style='font-weight: bold; background-color: #90EE90; font-size: 11px;'>" + totalJumlahBC + "</td>" +
                            "</tr>");
                    totalJumlahMasuk = 0;
                    totalJumlahKeluar = 0;
                    totalJumlahBC = 0;
                }

                totalJumlahMasuk += jumlahMasuk;
                totalJumlahKeluar += jumlahKeluar;
                totalJumlahBC += jumlahBC;
                lastTglPerawatan = currentTglPerawatan;

                htmlContent.append("<tr class='isi'>" +
                        "<td align='center' width='9%'>" + currentTglPerawatan + " | " + rs2.getString("jam_rawat") + "</td>" +
                        "<td align='center' width='5%'>" + rs2.getString("infus") + "</td>" +
                        "<td align='center' width='5%'>" + rs2.getString("tranfusi") + "</td>" +
                        "<td align='center' width='5%'>" + rs2.getString("minum") + "</td>" +
                        "<td align='center' width='5%'>" + rs2.getString("sh") + "</td>" +
                        "<td align='center' width='5%'>" + jumlahMasuk + "</td>" +
                        "<td align='center' width='4%'>" + rs2.getString("urine") + "</td>" +
                        "<td align='center' width='5%'>" + rs2.getString("drain") + "</td>" +
                        "<td align='center' width='5%'>" + rs2.getString("ngt") + "</td>" +
                       "<td align='center' width='4%'>" + rs2.getString("iwl") + "</td>" +
                       "<td align='center' width='5%'>" + jumlahKeluar + "</td>" +
                       "<td align='center' width='5%'>" + rs2.getString("tusuk") + "</td>" +
                       "<td align='center' width='5%'>" + rs2.getString("mandi") + "</td>" +
                       "<td align='center' width='5%'>" + rs2.getString("aktifitas") + "</td>" +
                       "<td align='center' width='5%'>" + rs2.getString("nafas") + "</td>" +
                       "<td align='center' width='5%'>" + rs2.getString("tenun") + "</td>" +
                       "<td align='center' width='5%'>" + jumlahBC + "</td>" +
                       "<td align='center' width='10%'>" + rs2.getString("keterangan") + "</td>" +
                       "<td align='center' width='9%'>" + rs2.getString("nama") + "</td>" +
                        "</tr>");
            }

            if (!lastTglPerawatan.equals("")) {
                htmlContent.append("<tr class='isi2'>" +
                        "<td colspan='5' align='center' style='font-weight: bold; background-color: #e1ebf9;'>TOTAL INPUT</td>" +
                        "<td  align='center' style='font-weight: bold; background-color: #9ecaff; font-size: 11px;'>" + totalJumlahMasuk + "</td>" +
                        "<td colspan='4' align='center' style='font-weight: bold; background-color: #e1ebf9;'>TOTAL OUTPUT</td>" +
                        "<td  align='center' style='font-weight: bold; background-color: #FFB6C1; font-size: 11px;'>" + totalJumlahKeluar + "</td>" +
                        "<td colspan='5' align='center' style='font-weight: bold; background-color: #e1ebf9;'>TOTAL BALANCE CAIRAN</td>" +
                        "<td  align='center' style='font-weight: bold; background-color: #90EE90; font-size: 11px;'>" + totalJumlahBC + "</td>" +
                              
                        
                                
                                "</tr>");
            }

            rs2.close();
            ps2.close();

            htmlContent.append("</table></td></tr>");
        }
        rs.close();
        ps.close();

        LoadHTMLRiwayatIO.setText("<html><table width='100%' border='0' class='tbl_form'>" + htmlContent.toString() + "</table></html>");
    } catch (Exception e) {
        System.out.println("Error: " + e);
    }
}

*/
   
    private void tampilriwayatIO() {
        try {
            htmlContent = new StringBuilder(8192);
            int jmlRawat = 0;
            int lastIn = 0, lastOut = 0, gHari = 0, gCatatan = 0, lastBal = 0;
            String lastWaktu = "-";

            // hanya SATU nomor rawat: yang dikirim lewat setNoRm(norm,nama,norawat), atau jika tidak ada,
            // rawat inap yang masih aktif (kamar_inap.stts_pulang='-'), atau registrasi ranap terbaru
            boolean adaNoRawat = !noRawatSekarang.isEmpty();
            ps = koneksi.prepareStatement("SELECT reg_periksa.no_rawat, DATE_FORMAT(reg_periksa.tgl_registrasi, '%d-%m-%Y') AS tgl_registrasi FROM reg_periksa "
                    + "WHERE reg_periksa.no_rkm_medis = ? AND reg_periksa.status_lanjut = 'Ranap' "
                    + (adaNoRawat ? "AND reg_periksa.no_rawat = ? " : "")
                    + "ORDER BY (SELECT COUNT(*) FROM kamar_inap WHERE kamar_inap.no_rawat = reg_periksa.no_rawat AND kamar_inap.stts_pulang = '-') DESC, "
                    + "reg_periksa.tgl_registrasi DESC, reg_periksa.jam_reg DESC LIMIT 1");
            try {
                ps.setString(1, NoRM.getText().trim());
                if (adaNoRawat) {
                    ps.setString(2, noRawatSekarang);
                }
                rs = ps.executeQuery();

                while (rs.next()) {
                    jmlRawat++;
                    if (jmlRawat > 1) {
                        htmlContent.append("<tr class='gap'><td colspan='" + KOLOM_LABEL.length + "'>&nbsp;</td></tr>");
                    }
                    int posBanner = htmlContent.length();   // banner disisipkan di awal blok setelah jumlah catatan diketahui
                    int jmlCatatan = 0;
                    int hariKe = 0;

                    PreparedStatement ps2 = koneksi.prepareStatement("SELECT DATE_FORMAT(tgl_perawatan, '%d-%m-%Y') AS tgl_perawatan, DATE_FORMAT(DATE_SUB(CONCAT(tgl_perawatan,' ',jam_rawat), INTERVAL 7 HOUR), '%d-%m-%Y') AS tgl_shift, jam_rawat,infus,tranfusi,minum,sh, urine,drain,ngt,iwl,iwl2,"
                            + "                                       tusuk,mandi,aktifitas,nafas,tenun,keterangan,petugas.nama FROM catatan_keseimbangan_cairan "
                            + "INNER JOIN petugas ON catatan_keseimbangan_cairan.nip = petugas.nip WHERE catatan_keseimbangan_cairan.no_rawat = ? ORDER BY catatan_keseimbangan_cairan.tgl_perawatan, catatan_keseimbangan_cairan.jam_rawat");
                    ResultSet rs2 = null;
                    try {
                        ps2.setString(1, rs.getString("no_rawat"));
                        rs2 = ps2.executeQuery();

                        String lastTglShift = "";
                        int totalInfus = 0, totalTranfusi = 0, totalMinum = 0, totalSH = 0;
                        int totalUrine = 0, totalDrain = 0, totalNgt = 0, totalIwl = 0, totalIwl2 = 0;
                        int baris = 0;

                        while (rs2.next()) {
                            String currentTglShift = rs2.getString("tgl_shift");
                            int jumlahInfus = rs2.getInt("infus");
                            int jumlahTranfusi = rs2.getInt("tranfusi");
                            int jumlahMinum = rs2.getInt("minum");
                            int jumlahSH = rs2.getInt("sh");
                            int jumlahUrine = rs2.getInt("urine");
                            int jumlahDrain = rs2.getInt("drain");
                            int jumlahNgt = rs2.getInt("ngt");
                            int jumlahIwl = rs2.getInt("iwl");
                            int jumlahIwl2 = rs2.getInt("iwl2");

                            // ganti hari shift -> tutup dengan baris TOTAL
                            if (!currentTglShift.equals(lastTglShift) && !lastTglShift.equals("")) {
                                htmlContent.append(barisTotal(totalInfus, totalTranfusi, totalMinum, totalSH,
                                        totalUrine, totalDrain, totalNgt, totalIwl, totalIwl2));
                                totalInfus = 0; totalTranfusi = 0; totalMinum = 0; totalSH = 0;
                                totalUrine = 0; totalDrain = 0; totalNgt = 0; totalIwl = 0; totalIwl2 = 0;
                                baris = 0;
                            }

                            if (!currentTglShift.equals(lastTglShift)) {
                                hariKe++;
                                gHari++;
                                htmlContent.append(barisHari(hariKe, currentTglShift));
                            }

                            totalInfus += jumlahInfus;
                            totalTranfusi += jumlahTranfusi;
                            totalMinum += jumlahMinum;
                            totalSH += jumlahSH;
                            totalUrine += jumlahUrine;
                            totalDrain += jumlahDrain;
                            totalNgt += jumlahNgt;
                            totalIwl += jumlahIwl;
                            totalIwl2 += jumlahIwl2;
                            lastTglShift = currentTglShift;
                            // total hari perawatan terakhir (totalXxx di-reset tiap ganti hari)
                            lastIn = totalInfus + totalTranfusi + totalMinum + totalSH;
                            lastOut = totalUrine + totalDrain + totalNgt + totalIwl + totalIwl2;
                            lastBal = (totalInfus + totalTranfusi + totalMinum + totalSH)
                                    - (totalUrine + totalDrain + totalNgt + totalIwl + totalIwl2);
                            lastWaktu = rs2.getString("tgl_perawatan") + " " + rs2.getString("jam_rawat");
                            gCatatan++;

                            htmlContent.append("<tr class='" + (baris % 2 == 0 ? "za" : "zb") + "'>"
                                    + sel(0, "<b>" + esc(rs2.getString("tgl_perawatan")) + "</b>&nbsp;<font color='#94a3b8'>|</font>&nbsp;<font color='" + warnaShift(rs2.getString("jam_rawat")) + "'><b>" + esc(rs2.getString("jam_rawat")) + "</b></font>", "center")
                                    + sel(1, angka(jumlahInfus, "#1d4ed8"), "center")
                                    + sel(2, angka(jumlahTranfusi, "#1d4ed8"), "center")
                                    + sel(3, angka(jumlahMinum, "#1d4ed8"), "center")
                                    + sel(4, angka(jumlahSH, "#1d4ed8"), "center")
                                    + sel(5, angka(jumlahUrine, "#b91c1c"), "center")
                                    + sel(6, angka(jumlahDrain, "#b91c1c"), "center")
                                    + sel(7, angka(jumlahNgt, "#b91c1c"), "center")
                                    + sel(8, angka(jumlahIwl, "#b91c1c"), "center")
                                    + sel(9, angka(jumlahIwl2, "#b91c1c"), "center")
                                    + sel(10, teks(null), "center")
                                    + sel(11, teks(rs2.getString("tusuk")), "center")
                                    + sel(12, teks(rs2.getString("mandi")), "center")
                                    + sel(13, teks(rs2.getString("aktifitas")), "center")
                                    + sel(14, teks(rs2.getString("nafas")), "center")
                                    + sel(15, teks(rs2.getString("tenun")), "center")
                                    + sel(16, teks(rs2.getString("keterangan")), "left")
                                    + sel(17, teks(rs2.getString("nama")), "left")
                                    + "</tr>");
                            baris++;
                            jmlCatatan++;
                        }

                        if (!lastTglShift.equals("")) {
                            htmlContent.append(barisTotal(totalInfus, totalTranfusi, totalMinum, totalSH,
                                    totalUrine, totalDrain, totalNgt, totalIwl, totalIwl2));
                        } else {
                            htmlContent.append("<tr><td colspan='" + KOLOM_LABEL.length + "' class='kosong' align='center'>"
                                    + "Belum ada catatan keseimbangan cairan pada perawatan ini</td></tr>");
                        }
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        ps2.close();
                    }
                    htmlContent.insert(posBanner, bannerRawat(jmlRawat, rs.getString("no_rawat"), rs.getString("tgl_registrasi"), jmlCatatan, hariKe));
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }

            if (jmlRawat == 0) {
                htmlContent.append("<tr><td colspan='" + KOLOM_LABEL.length + "' class='kosong' align='center'>"
                        + "<br>Tidak ditemukan riwayat rawat inap untuk pasien ini<br>&nbsp;</td></tr>");
            }

            isiKartu(lastIn, lastOut, gHari, lastBal, gCatatan, lastWaktu);

            // tampilan layar: header ada di panel beku (lihat terapkanTampilan)
            String isi = htmlContent.toString();
            htmlContent = null;          // lepaskan StringBuilder besar secepatnya
            htmlIsiTabel = isi;          // satu-satunya salinan yang disimpan (untuk cetak)
            LoadHTMLRiwayatIO.setText("<html><body><table " + ATRIBUT_TABEL + ">" + isi + "</table></body></html>");
            // kembali ke atas tanpa setCaretPosition (yang memaksa layout seluruh dokumen saat itu juga)
            javax.swing.SwingUtilities.invokeLater(() -> Scroll.getViewport().setViewPosition(new java.awt.Point(0, 0)));
            isiHeaderPasien();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    // ===================== Helper tampilan Riwayat IO =====================

    private String bannerRawat(int no, String noRawat, String tglReg, int jmlCatatan, int jmlHari) {
        return "<tr><td colspan='" + KOLOM_LABEL.length + "' style='background-color: #f1f5f9;'>"
                + "<table width='100%' border='0' cellspacing='0' cellpadding='7'><tr>"
                + "<td class='vacc2' width='1%'>&nbsp;</td>"
                + "<td class='vh' width='49%'>No.Rawat &nbsp;:&nbsp; " + esc(noRawat) + "</td>"
                + "<td class='vtgl' width='25%' align='right'>Tgl.Reg : " + esc(tglReg) + "</td>"
                + "<td class='vchip' width='15%' align='right'>" + jmlCatatan + " catatan &middot; " + jmlHari + " hari</td>"
                + "<td class='ranap' width='10%' align='center'>RANAP</td>"
                + "</tr></table></td></tr>";
    }

    private void isiHeaderPasien() {
        String jenisKelamin = Jk.getText().trim();
        if (jenisKelamin.equals("L")) {
            jenisKelamin = "Laki-Laki";
        } else if (jenisKelamin.equals("P")) {
            jenisKelamin = "Perempuan";
        }
        lblJudul.setText("RIWAYAT INTAKE OUTPUT (I/O)  \u00B7  " + NmPasien.getText().trim().toUpperCase());
        String umur = "";
        try {
            java.time.LocalDate lhr = java.time.LocalDate.parse(TanggalLahir.getText().trim());
            umur = " <font color='#9db8d9'>(" + java.time.Period.between(lhr, java.time.LocalDate.now()).getYears() + " thn)</font>";
        } catch (Exception e) {
        }
        String pisah = "&nbsp;&nbsp;&nbsp;<font color='#5f7fa6'>|</font>&nbsp;&nbsp;&nbsp;";
        lblInfo.setText("<html><font color='#9db8d9'>No.RM</font> <b><font color='#ffffff'>" + esc(NoRM.getText().trim()) + "</font></b>"
                + pisah + "<b><font color='#ffffff'>" + esc(jenisKelamin) + "</font></b>"
                + pisah + "<font color='#9db8d9'>Lahir</font> <b><font color='#ffffff'>" + esc(TempatLahir.getText().trim()) + ", " + esc(TanggalLahir.getText().trim()) + "</font></b>" + umur
                + pisah + "<font color='#9db8d9'>Gol. Darah</font> <b><font color='#ffffff'>" + esc(GD.getText().trim()) + "</font></b></html>");
    }

    /** header judul pasien (3 baris). Tinggi dihitung otomatis dari isi, jadi tidak akan terpotong. */
    private javax.swing.JPanel buatHeaderPasien() {
        lblJudul = new javax.swing.JLabel("RIWAYAT INTAKE OUTPUT (I/O)");
        lblJudul.setOpaque(true);
        lblJudul.setBackground(Color.decode(WARNA_KUAT[0]));
        lblJudul.setForeground(Color.WHITE);
        lblJudul.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblJudul.setBorder(javax.swing.BorderFactory.createEmptyBorder(9, 14, 9, 14));

        lblInfo = new javax.swing.JLabel(" ");
        lblInfo.setOpaque(true);
        lblInfo.setBackground(Color.decode("#2d4f78"));
        lblInfo.setForeground(Color.decode("#dbe7f5"));
        lblInfo.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 14, 6, 14));

        lblJudul.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 3, 0, Color.decode(WARNA_KUAT[3])),
                javax.swing.BorderFactory.createEmptyBorder(9, 14, 8, 14)));

        javax.swing.JPanel isi = new javax.swing.JPanel(new java.awt.BorderLayout());
        isi.add(buatLegenda(), java.awt.BorderLayout.NORTH);
        isi.add(buatKartu(), java.awt.BorderLayout.CENTER);
        javax.swing.JPanel bawah = new javax.swing.JPanel(new java.awt.BorderLayout());
        bawah.add(lblInfo, java.awt.BorderLayout.NORTH);
        bawah.add(isi, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout());
        p.add(lblJudul, java.awt.BorderLayout.NORTH);
        p.add(bawah, java.awt.BorderLayout.CENTER);
        return p;
    }

    private static final String[] KARTU_JUDUL = {"TOTAL INPUT", "TOTAL OUTPUT", "BALANCE", "CATATAN"};
    private static final String[] KARTU_WARNA = {"#2e8b4a", "#c24141", "#e07b00", "#1f3b5c"};

    /** deretan kartu ringkasan: input, output, balance, balance hari terakhir, jumlah catatan */
    private javax.swing.JPanel buatKartu() {
        javax.swing.JPanel baris = new javax.swing.JPanel(new java.awt.GridLayout(1, 4, 6, 0));
        baris.setBackground(Color.decode("#f1f5f9"));
        baris.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 4, 8));
        for (int k = 0; k < 4; k++) {
            final int kk = k;
            final Color warna = Color.decode(KARTU_WARNA[k]);
            javax.swing.JLabel judul = new javax.swing.JLabel(KARTU_JUDUL[k]);
            judul.setFont(new Font("Tahoma", Font.BOLD, 9));
            judul.setForeground(Color.decode("#64748b"));
            kartuNilai[k] = new javax.swing.JLabel(nilaiHtml(k, "-", ""));
            kartuNilai[k].setFont(new Font("Tahoma", Font.BOLD, 18));
            kartuSub[k] = new javax.swing.JLabel(" ");
            kartuSub[k].setFont(new Font("Tahoma", Font.PLAIN, 9));
            kartuSub[k].setForeground(Color.decode("#94a3b8"));

            // kartu digambar sendiri: sudut membulat, bayangan lembut, semburat warna, strip aksen, dan ikon bulat
            javax.swing.JPanel kartu = new javax.swing.JPanel(new java.awt.BorderLayout(0, 1)) {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    int bx = 3, by = 2, bw = w - 7, bh = h - 7;
                    for (int i = 3; i >= 1; i--) {
                        g2.setColor(new Color(15, 42, 74, 5 + (3 - i) * 4));
                        g2.fillRoundRect(bx - i, by - i + 2, bw + 2 * i, bh + 2 * i, 18 + i, 18 + i);
                    }
                    java.awt.Shape badan = new java.awt.geom.RoundRectangle2D.Float(bx, by, bw, bh, 16, 16);
                    g2.setColor(Color.WHITE);
                    g2.fill(badan);
                    g2.setClip(badan);
                    g2.setPaint(new java.awt.GradientPaint(bx, by, new Color(warna.getRed(), warna.getGreen(), warna.getBlue(), 34),
                            bx + bw, by + bh, new Color(255, 255, 255, 0)));
                    g2.fillRect(bx, by, bw, bh);
                    g2.setColor(warna);
                    g2.fillRect(bx, by, 5, bh);
                    g2.setClip(null);
                    g2.setColor(new Color(203, 213, 225));
                    g2.draw(new java.awt.geom.RoundRectangle2D.Float(bx, by, bw - 1, bh - 1, 16, 16));

                    int d = 32, cx = bx + bw - d / 2 - 11, cy = by + bh / 2;
                    g2.setColor(new Color(warna.getRed(), warna.getGreen(), warna.getBlue(), 38));
                    g2.fillOval(cx - d / 2, cy - d / 2, d, d);
                    g2.setColor(warna);
                    g2.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    gambarIkon(g2, kk == 3 ? 4 : kk, cx, cy);
                    g2.dispose();
                }
            };
            kartu.setOpaque(false);
            kartu.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 15, 7, 50));
            kartu.setPreferredSize(new Dimension(100, 78));
            kartu.add(judul, java.awt.BorderLayout.NORTH);
            kartu.add(kartuNilai[k], java.awt.BorderLayout.CENTER);
            kartu.add(kartuSub[k], java.awt.BorderLayout.SOUTH);
            baris.add(kartu);
        }
        return baris;
    }

    /** ikon sederhana dari garis: panah turun (input), panah naik (output), grafik, kalender, dokumen */
    private static void gambarIkon(java.awt.Graphics2D g2, int jenis, int cx, int cy) {
        switch (jenis) {
            case 0:
                g2.drawLine(cx, cy - 7, cx, cy + 7);
                g2.drawLine(cx - 5, cy + 2, cx, cy + 7);
                g2.drawLine(cx + 5, cy + 2, cx, cy + 7);
                break;
            case 1:
                g2.drawLine(cx, cy + 7, cx, cy - 7);
                g2.drawLine(cx - 5, cy - 2, cx, cy - 7);
                g2.drawLine(cx + 5, cy - 2, cx, cy - 7);
                break;
            case 2:
                g2.drawLine(cx - 8, cy + 5, cx - 3, cy);
                g2.drawLine(cx - 3, cy, cx + 1, cy + 3);
                g2.drawLine(cx + 1, cy + 3, cx + 8, cy - 6);
                g2.drawLine(cx + 8, cy - 6, cx + 3, cy - 6);
                g2.drawLine(cx + 8, cy - 6, cx + 8, cy - 1);
                break;
            case 3:
                g2.drawRoundRect(cx - 8, cy - 6, 16, 14, 4, 4);
                g2.drawLine(cx - 8, cy - 2, cx + 8, cy - 2);
                g2.drawLine(cx - 4, cy - 9, cx - 4, cy - 5);
                g2.drawLine(cx + 4, cy - 9, cx + 4, cy - 5);
                break;
            default:
                g2.drawRoundRect(cx - 6, cy - 8, 12, 16, 4, 4);
                g2.drawLine(cx - 3, cy - 3, cx + 3, cy - 3);
                g2.drawLine(cx - 3, cy + 1, cx + 3, cy + 1);
                g2.drawLine(cx - 3, cy + 5, cx + 1, cy + 5);
                break;
        }
    }

    /** angka besar berwarna + satuan kecil abu-abu */
    private static String nilaiHtml(int k, String angka, String satuan) {
        return "<html><span style='font-size:20px;font-weight:bold;color:" + KARTU_WARNA[k] + "'>" + angka + "</span>"
                + (satuan.isEmpty() ? "" : "&nbsp;<span style='font-size:10px;font-weight:normal;color:#94a3b8'>" + satuan + "</span>")
                + "</html>";
    }

    private static String bertanda(int v) {
        return v > 0 ? "+" + v : String.valueOf(v);
    }

    /** kartu menampilkan total SATU hari perawatan (hari terakhir pada data), bukan akumulasi semua hari */
    private void isiKartu(int in, int out, int hari, int lastBal, int catatan, String lastWaktu) {
        if (kartuNilai[0] == null) {
            return;
        }
        boolean ada = catatan > 0;
        String hr = "Hari ke-" + hari + " perawatan";
        kartuNilai[0].setText(ada ? nilaiHtml(0, String.valueOf(in), "ml") : nilaiHtml(0, "-", ""));
        kartuSub[0].setText(ada ? hr : " ");
        kartuNilai[1].setText(ada ? nilaiHtml(1, String.valueOf(out), "ml") : nilaiHtml(1, "-", ""));
        kartuSub[1].setText(ada ? hr : " ");
        kartuNilai[2].setText(ada ? nilaiHtml(2, bertanda(lastBal), "ml") : nilaiHtml(2, "-", ""));
        kartuSub[2].setText(ada ? hr + " \u00B7 " + (lastBal > 0 ? "surplus" : (lastBal < 0 ? "defisit" : "seimbang")) : " ");
        kartuNilai[3].setText(ada ? nilaiHtml(3, String.valueOf(catatan), "catatan") : nilaiHtml(3, "-", ""));
        kartuSub[3].setText(ada ? hari + " hari \u00B7 terakhir " + lastWaktu.substring(0, Math.min(16, lastWaktu.length())) : " ");
    }

    private javax.swing.JLabel teksLabel(String teks, String warna, boolean tebal) {
        javax.swing.JLabel l = new javax.swing.JLabel(teks);
        l.setForeground(Color.decode(warna));
        l.setFont(new Font("Tahoma", tebal ? Font.BOLD : Font.PLAIN, 10));
        return l;
    }

    /** baris keterangan warna, dari JLabel teks biasa (tanpa HTML) */
    private javax.swing.JPanel buatLegenda() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        p.setBackground(Color.decode("#f8fafc"));
        p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#e2e8f0")),
                javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        p.add(teksLabel("Keterangan :", "#64748b", false));
        p.add(teksLabel("INPUT", WARNA_TEKS_TOTAL[1], true));
        p.add(teksLabel("\u00B7", "#64748b", false));
        p.add(teksLabel("OUTPUT", WARNA_TEKS_TOTAL[2], true));
        p.add(teksLabel("\u00B7", "#64748b", false));
        p.add(teksLabel("BALANCE", WARNA_TEKS_TOTAL[3], true));
        p.add(teksLabel("= Input - Output (+ surplus, - defisit)", "#64748b", false));
        p.add(teksLabel("   |   Jam :", "#64748b", false));
        p.add(teksLabel("Pagi 07-14", "#d97706", true));
        p.add(teksLabel("\u00B7", "#64748b", false));
        p.add(teksLabel("Sore 14-21", "#0d9488", true));
        p.add(teksLabel("\u00B7", "#64748b", false));
        p.add(teksLabel("Malam 21-07", "#4f46e5", true));
        p.add(teksLabel("   |   Total per hari perawatan (07.00 - 06.59)", "#64748b", false));
        return p;
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        for (int i = 0, n = s.length(); i < n; i++) {
            char c = s.charAt(i);
            if (c == '&' || c == '<' || c == '>') {   // jalur lambat hanya jika memang ada karakter khusus
                return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            }
        }
        return s;
    }

    /** versi cetak: header ikut dimasukkan ke tabel. Dirakit hanya saat dibutuhkan (hemat RAM). */
    private String bangunHtmlCetak() {
        return "<html><head></head><body><table " + ATRIBUT_TABEL + " class='tbl_form'><thead>" + headerHtml()
                + "</thead>" + htmlIsiTabel + "</table></body></html>";
    }

    /** lepaskan DOM HTML dan string besar saat dialog ditutup */
    private void kosongkanData() {
        htmlContent = null;
        htmlIsiTabel = "";
        isiKartu(0, 0, 0, 0, 0, "-");
        LoadHTMLRiwayatIO.setText("");
    }

    private static String angka(int v, String warna) {
        return v == 0 ? "<font color='#94a3b8'>-</font>" : "<font color='" + warna + "'>" + v + "</font>";
    }

    private static String balanceTeks(int b) {
        if (b > 0) {
            return "<font color='#0f766e'>+" + b + "</font>";
        }
        return b < 0 ? "<font color='#b91c1c'>" + b + "</font>" : "0";
    }

    /** warna jam menurut shift: Pagi 07-14, Sore 14-21, Malam 21-07 */
    private static String warnaShift(String jam) {
        int h = 0;
        try {
            h = Integer.parseInt(jam.substring(0, 2));
        } catch (Exception e) {
        }
        return h >= 7 && h < 14 ? "#d97706" : (h >= 14 && h < 21 ? "#0d9488" : "#4f46e5");
    }

    private static final String[] NAMA_HARI = {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"};
    private static final String[] NAMA_BULAN = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};

    /** baris pembuka tiap hari perawatan: "HARI KE-1 | Kamis, 29 Okt 2026" */
    private String barisHari(int ke, String tglDdMMyyyy) {
        String tgl = tglDdMMyyyy;
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(tglDdMMyyyy, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            tgl = NAMA_HARI[d.getDayOfWeek().getValue() - 1] + ", " + d.getDayOfMonth() + " " + NAMA_BULAN[d.getMonthValue() - 1] + " " + d.getYear();
        } catch (Exception e) {
        }
        return "<tr><td colspan='" + KOLOM_LABEL.length + "' class='hari' bgcolor='#e8eff8'>&nbsp;<font color='#2b6cb0'><b>HARI KE-" + ke
                + "</b></font>&nbsp;&nbsp;<font color='#94a3b8'>|</font>&nbsp;&nbsp;<b>" + esc(tgl) + "</b>"
                + "&nbsp;&nbsp;<font color='#64748b'>(07.00 - 06.59)</font></td></tr>";
    }

    private static String angka(int v) {
        return v == 0 ? "<font color='#94a3b8'>-</font>" : String.valueOf(v);
    }

    private static String teks(String s) {
        if (s == null || s.trim().isEmpty()) {
            return "<font color='#94a3b8'>-</font>";
        }
        return esc(s.trim());
    }

    /** satu sel isi dengan lebar sesuai kolom ke-idx */
    private static String sel(int idx, String isi, String align) {
        return "<td align='" + align + "' valign='middle' width='" + KOLOM_LEBAR[idx] + "%'>" + isi + "</td>";
    }

    /** satu sel baris total, diwarnai sesuai tipe kolom */
    private static String selTotal(int idx, String isi) {
        int t = KOLOM_TIPE[idx];
        return "<td align='center' valign='middle' width='" + KOLOM_LEBAR[idx] + "%' style='background-color: " + WARNA_TOTAL[t]
                + "; color: " + WARNA_TEKS_TOTAL[t] + ";'>" + isi + "</td>";
    }

    private String barisTotal(int infus, int tranfusi, int minum, int sh,
            int urine, int drain, int ngt, int iwl, int iwl2) {
        int balance = (infus + tranfusi + minum + sh) - (urine + drain + ngt + iwl + iwl2);
        StringBuilder sb = new StringBuilder("<tr class='tot'>");
        sb.append(selTotal(0, "TOTAL"));
        sb.append(selTotal(1, String.valueOf(infus)));
        sb.append(selTotal(2, String.valueOf(tranfusi)));
        sb.append(selTotal(3, String.valueOf(minum)));
        sb.append(selTotal(4, String.valueOf(sh)));
        sb.append(selTotal(5, String.valueOf(urine)));
        sb.append(selTotal(6, String.valueOf(drain)));
        sb.append(selTotal(7, String.valueOf(ngt)));
        sb.append(selTotal(8, String.valueOf(iwl)));
        sb.append(selTotal(9, String.valueOf(iwl2)));
        sb.append(selTotal(10, balanceTeks(balance)));
        for (int i = 11; i < KOLOM_LABEL.length; i++) {
            sb.append(selTotal(i, "&nbsp;"));
        }
        return sb.append("</tr>").toString();
    }

    /** header dalam bentuk HTML (dipakai untuk cetak/browser) */
    private String headerHtml() {
        int n = KOLOM_LABEL.length;
        StringBuilder atas = new StringBuilder("<tr>");
        StringBuilder bawah = new StringBuilder("<tr>");
        int i = 0;
        while (i < n) {
            int t = KOLOM_TIPE[i];
            if (t == 1 || t == 2 || t == 4) {
                int j = i, lebar = KOLOM_LEBAR[i];
                while (j + 1 < n && KOLOM_TIPE[j + 1] == t) {
                    j++;
                    lebar += KOLOM_LEBAR[j];
                }
                atas.append("<td colspan='" + (j - i + 1) + "' width='" + lebar + "%' align='center' bgcolor='" + WARNA_HEADER
                        + "'><font color='#ffffff'><b>" + GRUP_NAMA[t] + "</b></font></td>");
                for (int k = i; k <= j; k++) {
                    bawah.append("<td align='center' bgcolor='" + WARNA_HEADER + "'><font color='#ffffff'><b>"
                            + esc(KOLOM_LABEL[k]).replace("\n", "<br>") + "</b></font></td>");
                }
                i = j + 1;
            } else {
                atas.append("<td rowspan='2' width='" + KOLOM_LEBAR[i] + "%' align='center' bgcolor='" + WARNA_HEADER
                        + "'><font color='#ffffff'><b>" + esc(KOLOM_LABEL[i]).replace("\n", "<br>") + "</b></font></td>");
                i++;
            }
        }
        return atas.append("</tr>").append(bawah).append("</tr>").toString();
    }

    /**
     * Label header ringan: menggambar teks sendiri (tanpa HTML/View Swing), sehingga lebih cepat,
     * hemat RAM, dan tidak terpengaruh stylesheet HTML milik form lain (penyebab kotak putih).
     */
    private static final class LabelHeader extends javax.swing.JComponent {
        private final String[] baris;
        private final Color bg, fg;

        LabelHeader(String teks, Color bg, Color fg, int ukuran) {
            this.baris = teks.isEmpty() ? new String[0] : teks.split("\n");
            this.bg = bg;
            this.fg = fg;
            setFont(new Font("Tahoma", Font.BOLD, ukuran));
            setOpaque(true);
            if (!teks.isEmpty()) {
                setToolTipText(teks.replace("\n", " "));
            }
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            int w = getWidth(), h = getHeight();
            g.setColor(bg);
            g.fillRect(0, 0, w, h);
            g.setColor(Color.WHITE);                 // garis pemisah kanan & bawah
            g.fillRect(w - 1, 0, 1, h);
            g.fillRect(0, h - 1, w, 1);
            if (baris.length == 0) {
                return;
            }
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            g2.setColor(fg);
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int lh = fm.getHeight();
            int y = (h - lh * baris.length) / 2 + fm.getAscent();
            for (String b : baris) {
                g2.drawString(b, (w - fm.stringWidth(b)) / 2, y);
                y += lh;
            }
            g2.dispose();
        }
    }

    private LabelHeader buatLabelHeader(String teks, Color bg, Color fg, int ukuran) {
        return new LabelHeader(teks, bg, fg, ukuran);
    }

    /**
     * Header tabel yang "beku": dipasang sebagai column header pada JScrollPane,
     * sehingga tetap terlihat di atas saat isi di-scroll ke bawah.
     */
    private javax.swing.JPanel buatHeaderBeku() {
        final int n = KOLOM_LABEL.length;
        final LabelHeader[] kol = new LabelHeader[n];
        final boolean[] penuh = new boolean[n];
        final java.util.List<int[]> grup = new java.util.ArrayList<>();
        final java.util.List<LabelHeader> grupLbl = new java.util.ArrayList<>();

        int i = 0;
        while (i < n) {
            int t = KOLOM_TIPE[i];
            if (t == 1 || t == 2 || t == 4) {
                int j = i;
                while (j + 1 < n && KOLOM_TIPE[j + 1] == t) {
                    j++;
                }
                grup.add(new int[]{i, j});
                grupLbl.add(buatLabelHeader(GRUP_NAMA[t], Color.decode(WARNA_HEADER), Color.WHITE, 10));
                for (int k = i; k <= j; k++) {
                    kol[k] = buatLabelHeader(KOLOM_LABEL[k], Color.decode(WARNA_HEADER), Color.WHITE, 9);
                }
                i = j + 1;
            } else {
                penuh[i] = true;
                kol[i] = buatLabelHeader(KOLOM_LABEL[i], Color.decode(WARNA_HEADER), Color.WHITE, 9);
                i++;
            }
        }

        javax.swing.JPanel p = new javax.swing.JPanel(null) {
            @Override
            public void doLayout() {
                int w = getWidth(), h = getHeight() - getInsets().bottom;
                int h1 = Math.round(h * 0.38f);
                int[] x = new int[n + 1];
                int cum = 0;
                for (int c = 0; c < n; c++) {
                    x[c] = Math.round(w * cum / 100f);
                    cum += KOLOM_LEBAR[c];
                }
                x[n] = w;
                for (int c = 0; c < n; c++) {
                    if (penuh[c]) {
                        kol[c].setBounds(x[c], 0, x[c + 1] - x[c], h);
                    } else {
                        kol[c].setBounds(x[c], h1, x[c + 1] - x[c], h - h1);
                    }
                }
                for (int g = 0; g < grup.size(); g++) {
                    int[] r = grup.get(g);
                    grupLbl.get(g).setBounds(x[r[0]], 0, x[r[1] + 1] - x[r[0]], h1);
                }
            }
        };
        p.setBackground(Color.decode(WARNA_KUAT[0]));
        p.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#cbd5e1")));
        p.setPreferredSize(new Dimension(100, TINGGI_HEADER));
        p.setMinimumSize(new Dimension(0, TINGGI_HEADER));
        for (LabelHeader l : kol) {
            p.add(l);
        }
        for (LabelHeader l : grupLbl) {
            p.add(l);
        }
        return p;
    }

    /** pengaturan tampilan tambahan (di luar kode hasil Form Editor agar tidak tertimpa) */
    private void terapkanTampilan() {
        Color navy = Color.decode("#0f2a4a");
        internalFrame1.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));

        // pilihan filter: hilangkan garis pink
        javax.swing.JRadioButton[] radio = {R1, R2, R3, R4};
        for (javax.swing.JRadioButton r : radio) {
            r.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 2, 0, 2));
        }

        // area tabel
        Color latar = Color.decode("#f1f5f9");
        Scroll.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode("#e2e8f0")));
        Scroll.getViewport().setBackground(latar);
        LoadHTMLRiwayatIO.setBackground(latar);

        // semua yang dikotaki merah dihilangkan: panel pemilih pasien, serta strip tab "Riwayat IO"
        PanelInput.setVisible(false);
        internalFrame1.remove(TabRawat);
        internalFrame1.add(internalFrame2, java.awt.BorderLayout.CENTER);

        // caret tidak perlu bergerak/menggulir otomatis (lebih ringan saat setText)
        ((javax.swing.text.DefaultCaret) LoadHTMLRiwayatIO.getCaret()).setUpdatePolicy(javax.swing.text.DefaultCaret.NEVER_UPDATE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                kosongkanData();
            }
        });

        // header judul pasien beku di atas area scroll
        HeaderPasien = buatHeaderPasien();
        internalFrame2.add(HeaderPasien, java.awt.BorderLayout.NORTH);
        Scroll.getVerticalScrollBar().setUnitIncrement(24);

        // HEADER BEKU: tetap di atas saat isi di-scroll
        Scroll.setColumnHeaderView(buatHeaderBeku());
        javax.swing.JPanel sudut = new javax.swing.JPanel();
        sudut.setBackground(Color.decode(WARNA_KUAT[0]));
        Scroll.setCorner(javax.swing.JScrollPane.UPPER_RIGHT_CORNER, sudut);
    }

    private void panggilLaporan(String teks) {
        try{
            File g = new File("file.css");            
            BufferedWriter bg = new BufferedWriter(new FileWriter(g));
            bg.write(".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}.isi a{text-decoration:none;color:#8b9b95;padding:0 0 0 0px;font-family: Tahoma;font-size: 8.5px;border: white;}" + CSS_TABEL);
            bg.close();

            File f = new File("riwayat.html");            
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(
                 teks.replaceAll("<head>","<head><link href=\"file.css\" rel=\"stylesheet\" type=\"text/css\" />").
                      replaceAll("<body>",
                                 "<body>"+
                                    "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                       "<tr class='isi'>"+ 
                                         "<td valign='top' width='20%'>No.RM</td>"+
                                         "<td valign='top' width='1%' align='center'>:</td>"+
                                         "<td valign='top' width='79%'>"+NoRM.getText().trim()+"</td>"+
                                       "</tr>"+
                                       "<tr class='isi'>"+ 
                                         "<td valign='top' width='20%'>Nama Pasien</td>"+
                                         "<td valign='top' width='1%' align='center'>:</td>"+
                                         "<td valign='top' width='79%'>"+NmPasien.getText()+"</td>"+
                                       "</tr>"+
                                       "<tr class='isi'>"+ 
                                         "<td valign='top' width='20%'>Alamat</td>"+
                                         "<td valign='top' width='1%' align='center'>:</td>"+
                                         "<td valign='top' width='79%'>"+Alamat.getText()+"</td>"+
                                       "</tr>"+
                                       "<tr class='isi'>"+ 
                                         "<td valign='top' width='20%'>Jenis Kelamin</td>"+
                                         "<td valign='top' width='1%' align='center'>:</td>"+
                                         "<td valign='top' width='79%'>"+Jk.getText().replaceAll("L","Laki-Laki").replaceAll("P","Perempuan")+"</td>"+
                                       "</tr>"+
                                       "<tr class='isi'>"+ 
                                         "<td valign='top' width='20%'>Tempat & Tanggal Lahir</td>"+
                                         "<td valign='top' width='1%' align='center'>:</td>"+
                                         "<td valign='top' width='79%'>"+TempatLahir.getText()+" "+TanggalLahir.getText()+"</td>"+
                                       "</tr>"+
                                       "<tr class='isi'>"+ 
                                         "<td valign='top' width='20%'>Ibu Kandung</td>"+
                                         "<td valign='top' width='1%' align='center'>:</td>"+
                                         "<td valign='top' width='79%'>"+IbuKandung.getText()+"</td>"+
                                       "</tr>"+
                                       "<tr class='isi'>"+ 
                                         "<td valign='top' width='20%'>Golongan Darah</td>"+
                                         "<td valign='top' width='1%' align='center'>:</td>"+
                                         "<td valign='top' width='79%'>"+GD.getText()+"</td>"+
                                       "</tr>"+
                                       "<tr class='isi'>"+ 
                                         "<td valign='top' width='20%'>Status Nikah</td>"+
                                         "<td valign='top' width='1%' align='center'>:</td>"+
                                         "<td valign='top' width='79%'>"+StatusNikah.getText()+"</td>"+
                                       "</tr>"+
                                       "<tr class='isi'>"+ 
                                         "<td valign='top' width='20%'>Agama</td>"+
                                         "<td valign='top' width='1%' align='center'>:</td>"+
                                         "<td valign='top' width='79%'>"+Agama.getText()+"</td>"+
                                       "</tr>"+
                                       "<tr class='isi'>"+ 
                                         "<td valign='top' width='20%'>Pendidikan Terakhir</td>"+
                                         "<td valign='top' width='1%' align='center'>:</td>"+
                                         "<td valign='top' width='79%'>"+Pendidikan.getText()+"</td>"+
                                       "</tr>"+
                                       "<tr class='isi'>"+ 
                                         "<td valign='top' width='20%'>Bahasa Dipakai</td>"+
                                         "<td valign='top' width='1%' align='center'>:</td>"+
                                         "<td valign='top' width='79%'>"+Bahasa.getText()+"</td>"+
                                       "</tr>"+
                                       "<tr class='isi'>"+ 
                                         "<td valign='top' width='20%'>Cacat Fisik</td>"+
                                         "<td valign='top' width='1%' align='center'>:</td>"+
                                         "<td valign='top' width='79%'>"+CacatFisik.getText()+"</td>"+
                                       "</tr>"+
                                    "</table>"            
                      )
            );  
            bw.close();
            Desktop.getDesktop().browse(f.toURI());
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }   
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,20));
            FormInput.setVisible(false);      
            ChkInput.setVisible(true);
        }else if(ChkInput.isSelected()==false){           
            ChkInput.setVisible(false);            
            PanelInput.setPreferredSize(new Dimension(WIDTH,126));
            FormInput.setVisible(true);      
            ChkInput.setVisible(true);
        }
    }
}
