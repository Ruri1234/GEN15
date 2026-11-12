/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgLhtBiaya.java
 *
 * Created on 12 Jul 10, 16:21:34
 */

package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
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
import simrskhanza.DlgCariPasien;

/**
 *
 * @author windiarto
 */
public final class RMRiwayatOperasi extends javax.swing.JDialog {    
    private validasi Valid=new validasi();    
    private final sekuel Sequel=new sekuel();
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

    /** Creates new form DlgLhtBiaya
     * @param parent
     * @param modal */
    public RMRiwayatOperasi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);
        
        NoRM.setDocument(new batasInput((byte)20).getKata(NoRM));
        NoRawat.setDocument(new batasInput((byte)20).getKata(NoRawat));
        
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
        
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTMLRiwayatPerawatan.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}.isi a{text-decoration:none;color:#8b9b95;padding:0 0 0 0px;font-family: Tahoma;font-size: 8.5px;border: white;}");
        Document doc = kit.createDefaultDocument();
        LoadHTMLRiwayatPerawatan.setDocument(doc);
        LoadHTMLRiwayatPerawatan.setEditable(false);
        LoadHTMLRiwayatPerawatan.addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
              Desktop desktop = Desktop.getDesktop();
              try {
                desktop.browse(e.getURL().toURI());
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
        });
        
        
        ChkAccor.setSelected(false);
        isMenu();
        
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
        LoadHTMLRiwayatPerawatan = new widget.editorpane();
        PanelAccor = new widget.PanelBiasa();
        ChkAccor = new widget.CekBox();
        ScrollMenu = new widget.ScrollPane();
        FormMenu = new widget.PanelBiasa();
        chkSemua = new widget.CekBox();
        chkChecklistPreOperasi = new widget.CekBox();
        chkSignInSebelumAnestesi = new widget.CekBox();
        chkTimeOutSebelumInsisi = new widget.CekBox();
        chkSignOutSebelumMenutupLuka = new widget.CekBox();
        chkChecklistPostOperasi = new widget.CekBox();
        chkAsuhanPreOperasi = new widget.CekBox();
        chkAsuhanPreAnestesi = new widget.CekBox();
        chkOperasiVK = new widget.CekBox();
        chkPenggunaanObatOperasi = new widget.CekBox();
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Riwayat/Rincian Tindakan/Terapi Pasien ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
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
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(28, 23));
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

        LoadHTMLRiwayatPerawatan.setBorder(null);
        LoadHTMLRiwayatPerawatan.setName("LoadHTMLRiwayatPerawatan"); // NOI18N
        Scroll.setViewportView(LoadHTMLRiwayatPerawatan);

        internalFrame2.add(Scroll, java.awt.BorderLayout.CENTER);

        PanelAccor.setBackground(new java.awt.Color(255, 255, 255));
        PanelAccor.setName("PanelAccor"); // NOI18N
        PanelAccor.setPreferredSize(new java.awt.Dimension(275, 43));
        PanelAccor.setLayout(new java.awt.BorderLayout());

        ChkAccor.setBackground(new java.awt.Color(255, 250, 250));
        ChkAccor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(250, 255, 248)));
        ChkAccor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png"))); // NOI18N
        ChkAccor.setFocusable(false);
        ChkAccor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkAccor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkAccor.setName("ChkAccor"); // NOI18N
        ChkAccor.setPreferredSize(new java.awt.Dimension(15, 20));
        ChkAccor.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png"))); // NOI18N
        ChkAccor.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png"))); // NOI18N
        ChkAccor.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png"))); // NOI18N
        ChkAccor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkAccorActionPerformed(evt);
            }
        });
        PanelAccor.add(ChkAccor, java.awt.BorderLayout.EAST);

        ScrollMenu.setBorder(null);
        ScrollMenu.setName("ScrollMenu"); // NOI18N
        ScrollMenu.setOpaque(true);
        ScrollMenu.setPreferredSize(new java.awt.Dimension(255, 1197));

        FormMenu.setBackground(new java.awt.Color(255, 255, 255));
        FormMenu.setBorder(null);
        FormMenu.setName("FormMenu"); // NOI18N
        FormMenu.setPreferredSize(new java.awt.Dimension(255, 2148));
        FormMenu.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 1, 1));

        chkSemua.setSelected(true);
        chkSemua.setText("Semua");
        chkSemua.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkSemua.setName("chkSemua"); // NOI18N
        chkSemua.setOpaque(false);
        chkSemua.setPreferredSize(new java.awt.Dimension(245, 22));
        chkSemua.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkSemuaItemStateChanged(evt);
            }
        });
        FormMenu.add(chkSemua);

        chkChecklistPreOperasi.setSelected(true);
        chkChecklistPreOperasi.setText("Check List Pre Operasi");
        chkChecklistPreOperasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkChecklistPreOperasi.setName("chkChecklistPreOperasi"); // NOI18N
        chkChecklistPreOperasi.setOpaque(false);
        chkChecklistPreOperasi.setPreferredSize(new java.awt.Dimension(245, 22));
        FormMenu.add(chkChecklistPreOperasi);

        chkSignInSebelumAnestesi.setSelected(true);
        chkSignInSebelumAnestesi.setText("Sign-In Sebelum Anestesi");
        chkSignInSebelumAnestesi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkSignInSebelumAnestesi.setName("chkSignInSebelumAnestesi"); // NOI18N
        chkSignInSebelumAnestesi.setOpaque(false);
        chkSignInSebelumAnestesi.setPreferredSize(new java.awt.Dimension(245, 22));
        FormMenu.add(chkSignInSebelumAnestesi);

        chkTimeOutSebelumInsisi.setSelected(true);
        chkTimeOutSebelumInsisi.setText("Time-Out Sebelum Insisi");
        chkTimeOutSebelumInsisi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkTimeOutSebelumInsisi.setName("chkTimeOutSebelumInsisi"); // NOI18N
        chkTimeOutSebelumInsisi.setOpaque(false);
        chkTimeOutSebelumInsisi.setPreferredSize(new java.awt.Dimension(245, 22));
        FormMenu.add(chkTimeOutSebelumInsisi);

        chkSignOutSebelumMenutupLuka.setSelected(true);
        chkSignOutSebelumMenutupLuka.setText("Sign-Out Sebelum Menutup Luka");
        chkSignOutSebelumMenutupLuka.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkSignOutSebelumMenutupLuka.setName("chkSignOutSebelumMenutupLuka"); // NOI18N
        chkSignOutSebelumMenutupLuka.setOpaque(false);
        chkSignOutSebelumMenutupLuka.setPreferredSize(new java.awt.Dimension(245, 22));
        FormMenu.add(chkSignOutSebelumMenutupLuka);

        chkChecklistPostOperasi.setSelected(true);
        chkChecklistPostOperasi.setText("Check List Post Operasi");
        chkChecklistPostOperasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkChecklistPostOperasi.setName("chkChecklistPostOperasi"); // NOI18N
        chkChecklistPostOperasi.setOpaque(false);
        chkChecklistPostOperasi.setPreferredSize(new java.awt.Dimension(245, 22));
        FormMenu.add(chkChecklistPostOperasi);

        chkAsuhanPreOperasi.setSelected(true);
        chkAsuhanPreOperasi.setText("Penilaian Pre Operasi");
        chkAsuhanPreOperasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkAsuhanPreOperasi.setName("chkAsuhanPreOperasi"); // NOI18N
        chkAsuhanPreOperasi.setOpaque(false);
        chkAsuhanPreOperasi.setPreferredSize(new java.awt.Dimension(245, 22));
        FormMenu.add(chkAsuhanPreOperasi);

        chkAsuhanPreAnestesi.setSelected(true);
        chkAsuhanPreAnestesi.setText("Penilaian Pre Anastesi");
        chkAsuhanPreAnestesi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkAsuhanPreAnestesi.setName("chkAsuhanPreAnestesi"); // NOI18N
        chkAsuhanPreAnestesi.setOpaque(false);
        chkAsuhanPreAnestesi.setPreferredSize(new java.awt.Dimension(245, 22));
        FormMenu.add(chkAsuhanPreAnestesi);

        chkOperasiVK.setSelected(true);
        chkOperasiVK.setText("Operasi/VK");
        chkOperasiVK.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkOperasiVK.setName("chkOperasiVK"); // NOI18N
        chkOperasiVK.setOpaque(false);
        chkOperasiVK.setPreferredSize(new java.awt.Dimension(245, 22));
        FormMenu.add(chkOperasiVK);

        chkPenggunaanObatOperasi.setSelected(true);
        chkPenggunaanObatOperasi.setText("Penggunaan Obat/BHP Operasi");
        chkPenggunaanObatOperasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkPenggunaanObatOperasi.setName("chkPenggunaanObatOperasi"); // NOI18N
        chkPenggunaanObatOperasi.setOpaque(false);
        chkPenggunaanObatOperasi.setPreferredSize(new java.awt.Dimension(245, 22));
        FormMenu.add(chkPenggunaanObatOperasi);

        ScrollMenu.setViewportView(FormMenu);

        PanelAccor.add(ScrollMenu, java.awt.BorderLayout.CENTER);

        internalFrame2.add(PanelAccor, java.awt.BorderLayout.WEST);

        TabRawat.addTab("Riwayat Perawatan", internalFrame2);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        PanelInput.setBackground(new java.awt.Color(255, 255, 255));
        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
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

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,Tgl1,NoRM);}
}//GEN-LAST:event_BtnKeluarKeyPressed

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
            switch (TabRawat.getSelectedIndex()) {
                case 0:
                    panggilLaporan(LoadHTMLRiwayatPerawatan.getText()); 
                default:
                    break;
            }
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
            switch (TabRawat.getSelectedIndex()) {
                case 0:
                    tampilPerawatan();
                    ChkAccor.setSelected(false);
                    isMenu();
                    break;
                default:
                    break;
            }
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnCari1ActionPerformed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        BtnCari1ActionPerformed(null);
    }//GEN-LAST:event_TabRawatMouseClicked

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkAccorActionPerformed
        isMenu();
    }//GEN-LAST:event_ChkAccorActionPerformed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void NoRawatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoRawatKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCari1ActionPerformed(null);
        }
    }//GEN-LAST:event_NoRawatKeyPressed

    private void chkSemuaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkSemuaItemStateChanged
        if(chkSemua.isSelected()==true){
            chkOperasiVK.setSelected(true);
            chkPenggunaanObatOperasi.setSelected(true);
            chkAsuhanPreOperasi.setSelected(true);
            chkAsuhanPreAnestesi.setSelected(true);
            chkChecklistPreOperasi.setSelected(true);
            chkSignInSebelumAnestesi.setSelected(true);
            chkTimeOutSebelumInsisi.setSelected(true);
            chkSignOutSebelumMenutupLuka.setSelected(true);
            chkChecklistPostOperasi.setSelected(true);
            
        }else{
            chkOperasiVK.setSelected(false);
            chkPenggunaanObatOperasi.setSelected(false);
            chkAsuhanPreOperasi.setSelected(false);
            chkAsuhanPreAnestesi.setSelected(false);
            chkChecklistPreOperasi.setSelected(false);
            chkSignInSebelumAnestesi.setSelected(false);
            chkTimeOutSebelumInsisi.setSelected(false);
            chkSignOutSebelumMenutupLuka.setSelected(false);
            chkChecklistPostOperasi.setSelected(false);
            
        }
    }//GEN-LAST:event_chkSemuaItemStateChanged

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMRiwayatOperasi dialog = new RMRiwayatOperasi(new javax.swing.JFrame(), true);
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
    private widget.CekBox ChkAccor;
    private widget.CekBox ChkInput;
    private widget.panelisi FormInput;
    private widget.PanelBiasa FormMenu;
    private widget.TextBox GD;
    private widget.TextBox IbuKandung;
    private widget.TextBox Jk;
    private widget.editorpane LoadHTMLRiwayatPerawatan;
    private widget.TextBox NmPasien;
    private widget.TextBox NoRM;
    private widget.TextBox NoRawat;
    private widget.PanelBiasa PanelAccor;
    private javax.swing.JPanel PanelInput;
    private widget.TextBox Pekerjaan;
    private widget.TextBox Pendidikan;
    private widget.RadioButton R1;
    private widget.RadioButton R2;
    private widget.RadioButton R3;
    private widget.RadioButton R4;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane ScrollMenu;
    private widget.TextBox StatusNikah;
    private javax.swing.JTabbedPane TabRawat;
    private widget.TextBox TanggalLahir;
    private widget.TextBox TempatLahir;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private javax.swing.ButtonGroup buttonGroup1;
    private widget.CekBox chkAsuhanPreAnestesi;
    private widget.CekBox chkAsuhanPreOperasi;
    private widget.CekBox chkChecklistPostOperasi;
    private widget.CekBox chkChecklistPreOperasi;
    private widget.CekBox chkOperasiVK;
    private widget.CekBox chkPenggunaanObatOperasi;
    private widget.CekBox chkSemua;
    private widget.CekBox chkSignInSebelumAnestesi;
    private widget.CekBox chkSignOutSebelumMenutupLuka;
    private widget.CekBox chkTimeOutSebelumInsisi;
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
    
    private void isMenu(){
        if(ChkAccor.isSelected()==true){
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(550,HEIGHT));
            FormMenu.setVisible(true); 
            ChkAccor.setVisible(true);
        }else if(ChkAccor.isSelected()==false){  
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(15,HEIGHT));
            FormMenu.setVisible(false);    
            ChkAccor.setVisible(true);
        }
    }
    

    private void tampilPerawatan() {
        try{   
            htmlContent = new StringBuilder();
            if(R1.isSelected()==true){
                ps=koneksi.prepareStatement(
                    "select reg_periksa.no_reg,reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.jam_reg,"+
                    "reg_periksa.kd_dokter,dokter.nm_dokter,poliklinik.nm_poli,reg_periksa.p_jawab,reg_periksa.almt_pj,"+
                    "reg_periksa.hubunganpj,reg_periksa.biaya_reg,reg_periksa.status_lanjut,penjab.png_jawab "+
                    "from reg_periksa inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "where reg_periksa.stts<>'Batal' and reg_periksa.no_rkm_medis=? order by reg_periksa.tgl_registrasi desc limit 5");
            }else if(R2.isSelected()==true){
                ps=koneksi.prepareStatement(
                    "select reg_periksa.no_reg,reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.jam_reg,"+
                    "reg_periksa.kd_dokter,dokter.nm_dokter,poliklinik.nm_poli,reg_periksa.p_jawab,reg_periksa.almt_pj,"+
                    "reg_periksa.hubunganpj,reg_periksa.biaya_reg,reg_periksa.status_lanjut,penjab.png_jawab "+
                    "from reg_periksa inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "where reg_periksa.stts<>'Batal' and reg_periksa.no_rkm_medis=? order by reg_periksa.tgl_registrasi");
            }else if(R3.isSelected()==true){
                ps=koneksi.prepareStatement(
                    "select reg_periksa.no_reg,reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.jam_reg,"+
                    "reg_periksa.kd_dokter,dokter.nm_dokter,poliklinik.nm_poli,reg_periksa.p_jawab,reg_periksa.almt_pj,"+
                    "reg_periksa.hubunganpj,reg_periksa.biaya_reg,reg_periksa.status_lanjut,penjab.png_jawab "+
                    "from reg_periksa inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "where reg_periksa.stts<>'Batal' and reg_periksa.no_rkm_medis=? and "+
                    "reg_periksa.tgl_registrasi between ? and ? order by reg_periksa.tgl_registrasi");
            }else if(R4.isSelected()==true){
                ps=koneksi.prepareStatement(
                    "select reg_periksa.no_reg,reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.jam_reg,"+
                    "reg_periksa.kd_dokter,dokter.nm_dokter,poliklinik.nm_poli,reg_periksa.p_jawab,reg_periksa.almt_pj,"+
                    "reg_periksa.hubunganpj,reg_periksa.biaya_reg,reg_periksa.status_lanjut,penjab.png_jawab "+
                    "from reg_periksa inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
                    "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                    "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                    "where reg_periksa.stts<>'Batal' and reg_periksa.no_rkm_medis=? and reg_periksa.no_rawat=?");
            }
            
            try {
                i=0;
                if(R1.isSelected()==true){
                    ps.setString(1,NoRM.getText().trim());
                }else if(R2.isSelected()==true){
                    ps.setString(1,NoRM.getText().trim());
                }else if(R3.isSelected()==true){
                    ps.setString(1,NoRM.getText().trim());
                    ps.setString(2,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                    ps.setString(3,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                }else if(R4.isSelected()==true){
                    ps.setString(1,NoRM.getText().trim());
                    ps.setString(2,NoRawat.getText().trim());
                }            
                urut=1;
                rs=ps.executeQuery();
                while(rs.next()){
                    try {
                        dokterrujukan="";
                        polirujukan="";
                        rs2=koneksi.prepareStatement(
                            "select poliklinik.nm_poli,dokter.nm_dokter from rujukan_internal_poli "+
                            "inner join poliklinik on rujukan_internal_poli.kd_poli=poliklinik.kd_poli "+
                            "inner join dokter on rujukan_internal_poli.kd_dokter=dokter.kd_dokter "+
                            "where no_rawat='"+rs.getString("no_rawat")+"'").executeQuery();
                        while(rs2.next()){
                            polirujukan=polirujukan+", "+rs2.getString("nm_poli");
                            dokterrujukan=dokterrujukan+", "+rs2.getString("nm_dokter");
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rs2!=null){
                            rs2.close();
                        }
                    }   

                    htmlContent.append(
                      "<tr class='isi'>"+ 
                        "<td valign='top' width='2%'>"+urut+"</td>"+
                        "<td valign='top' width='18%'>No.Rawat</td>"+
                        "<td valign='top' width='1%' align='center'>:</td>"+
                        "<td valign='top' width='79%'>"+rs.getString("no_rawat")+"</td>"+
                      "</tr>"+
                      "<tr class='isi'>"+ 
                        "<td valign='top' width='2%'></td>"+
                        "<td valign='top' width='18%'>No.Registrasi</td>"+
                        "<td valign='top' width='1%' align='center'>:</td>"+
                        "<td valign='top' width='79%'>"+rs.getString("no_reg")+"</td>"+
                      "</tr>"+
                      "<tr class='isi'>"+ 
                        "<td valign='top' width='2%'></td>"+
                        "<td valign='top' width='18%'>Tanggal Registrasi</td>"+
                        "<td valign='top' width='1%' align='center'>:</td>"+
                        "<td valign='top' width='79%'>"+rs.getString("tgl_registrasi")+" "+rs.getString("jam_reg")+"</td>"+
                      "</tr>"+
                      "<tr class='isi'>"+ 
                        "<td valign='top' width='2%'></td>"+
                        "<td valign='top' width='18%'>Unit/Poliklinik</td>"+
                        "<td valign='top' width='1%' align='center'>:</td>"+
                        "<td valign='top' width='79%'>"+rs.getString("nm_poli")+polirujukan+"</td>"+
                      "</tr>"+
                      "<tr class='isi'>"+ 
                        "<td valign='top' width='2%'></td>"+        
                        "<td valign='top' width='18%'>Dokter Poli</td>"+
                        "<td valign='top' width='1%' align='center'>:</td>"+
                        "<td valign='top' width='79%'>"+rs.getString("nm_dokter")+dokterrujukan+"</td>"+
                      "</tr>"
                    );
                    if(rs.getString("status_lanjut").equals("Ranap")){
                        try{
                            rs3=koneksi.prepareStatement(
                                "select dokter.nm_dokter from dpjp_ranap inner join dokter on dpjp_ranap.kd_dokter=dokter.kd_dokter where dpjp_ranap.no_rawat='"+rs.getString("no_rawat")+"'").executeQuery();
                            if(rs3.next()){
                                htmlContent.append(
                                  "<tr class='isi'>"+ 
                                    "<td valign='top' width='2%'></td>"+        
                                    "<td valign='top' width='18%'>DPJP Ranap</td>"+
                                    "<td valign='top' width='1%' align='center'>:</td>"+
                                    "<td valign='top' width='79%'>"
                                );
                                rs3.beforeFirst();
                                urutdpjp=1;
                                while(rs3.next()){
                                    htmlContent.append(urutdpjp+". "+rs3.getString("nm_dokter")+"&nbsp;&nbsp;");
                                    urutdpjp++;
                                }
                                htmlContent.append("</td>"+
                                  "</tr>"
                                );    
                            }
                        } catch (Exception e) {
                            System.out.println("Status Lanjut : "+e);
                        } finally{
                            if(rs3!=null){
                                rs3.close();
                            }
                        }
                    }
                    htmlContent.append( 
                      "<tr class='isi'>"+ 
                        "<td valign='top' width='2%'></td>"+
                        "<td valign='top' width='18%'>Cara Bayar</td>"+
                        "<td valign='top' width='1%' align='center'>:</td>"+
                        "<td valign='top' width='79%'>"+rs.getString("png_jawab")+"</td>"+
                      "</tr>"+
                      "<tr class='isi'>"+ 
                        "<td valign='top' width='2%'></td>"+        
                        "<td valign='top' width='18%'>Penanggung Jawab</td>"+
                        "<td valign='top' width='1%' align='center'>:</td>"+
                        "<td valign='top' width='79%'>"+rs.getString("p_jawab")+"</td>"+
                      "</tr>"+
                      "<tr class='isi'>"+ 
                        "<td valign='top' width='2%'></td>"+         
                        "<td valign='top' width='18%'>Alamat P.J.</td>"+
                        "<td valign='top' width='1%' align='center'>:</td>"+
                        "<td valign='top' width='79%'>"+rs.getString("almt_pj")+"</td>"+
                      "</tr>"+
                      "<tr class='isi'>"+ 
                        "<td valign='top' width='2%'></td>"+        
                        "<td valign='top' width='18%'>Hubungan P.J.</td>"+
                        "<td valign='top' width='1%' align='center'>:</td>"+
                        "<td valign='top' width='79%'>"+rs.getString("hubunganpj")+"</td>"+
                      "</tr>"+
                      "<tr class='isi'>"+ 
                        "<td valign='top' width='2%'></td>"+        
                        "<td valign='top' width='18%'>Status</td>"+
                        "<td valign='top' width='1%' align='center'>:</td>"+
                        "<td valign='top' width='79%'>"+rs.getString("status_lanjut")+"</td>"+
                      "</tr>"
                    );                            
                    urut++;
                    
                    
                    //menampilkan checlist pre operasi
                    menampilkanChecklistPreOperasi(rs.getString("no_rawat"));
                    //menampilkan signin sebelum tindakan anestesi
                    menampilkanSignInSebelumAnestesi(rs.getString("no_rawat"));
                    //menampilkan timeout sebelum tindakan insisi
                    menampilkanTimeOutSebelumInsisi(rs.getString("no_rawat"));
                    //menampilkan signout sebelum menutup luka
                    menampilkanSignOutSebelumMenutupLuka(rs.getString("no_rawat"));
                    //menampilkan checlist post operasi
                    menampilkanChecklistPostOperasi(rs.getString("no_rawat"));
                    //menampilkan asuhan awal pre operasi
                    menampilkanAsuhanPreOperasi(rs.getString("no_rawat"));
                    //menampilkan asuhan awal pre anestesi
                    menampilkanAsuhanPreAnestesi(rs.getString("no_rawat"));
                    
                    
                    
                                      
                    biayaperawatan=rs.getDouble("biaya_reg");
                    //biaya administrasi
                    htmlContent.append(
                       "<tr class='isi'>"+ 
                         "<td valign='top' width='2%'></td>"+        
                         "<td valign='top' width='18%'>Biaya & Perawatan</td>"+
                         "<td valign='top' width='1%' align='center'>:</td>"+
                         "<td valign='top' width='79%'>"+
                             "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                               "<tr>"+
                                 "<td valign='top' width='89%'>Administrasi</td>"+
                                 "<td valign='top' width='1%' align='right'>:</td>"+
                                 "<td valign='top' width='10%' align='right'>"+Valid.SetAngka(rs.getDouble("biaya_reg"))+"</td>"+
                               "</tr>"+
                             "</table>"
                    );
                    
                    //menampilkan operasi/vk
                    if(chkOperasiVK.isSelected()==true){
                        try{
                            rs2=koneksi.prepareStatement(
                                    "select operasi.tgl_operasi,operasi.jenis_anasthesi,operasi.operator1, operasi.operator2, operasi.operator3, operasi.asisten_operator1,"+
                                    "operasi.asisten_operator2,operasi.asisten_operator3,operasi.biayaasisten_operator3, operasi.instrumen, operasi.dokter_anak, operasi.perawaat_resusitas, "+
                                    "operasi.dokter_anestesi, operasi.asisten_anestesi, operasi.asisten_anestesi2,operasi.asisten_anestesi2, operasi.bidan, operasi.bidan2, operasi.bidan3, operasi.perawat_luar, operasi.omloop,"+
                                    "operasi.omloop2,operasi.omloop3,operasi.omloop4,operasi.omloop5,operasi.dokter_pjanak,operasi.dokter_umum, "+
                                    "operasi.kode_paket,paket_operasi.nm_perawatan, operasi.biayaoperator1, operasi.biayaoperator2, operasi.biayaoperator3, "+
                                    "operasi.biayaasisten_operator1, operasi.biayaasisten_operator2, operasi.biayaasisten_operator3, operasi.biayainstrumen, "+
                                    "operasi.biayadokter_anak, operasi.biayaperawaat_resusitas, operasi.biayadokter_anestesi, "+
                                    "operasi.biayaasisten_anestesi,operasi.biayaasisten_anestesi2, operasi.biayabidan,operasi.biayabidan2,operasi.biayabidan3, operasi.biayaperawat_luar, operasi.biayaalat,"+
                                    "operasi.biayasewaok,operasi.akomodasi,operasi.bagian_rs,operasi.biaya_omloop,operasi.biaya_omloop2,operasi.biaya_omloop3,operasi.biaya_omloop4,operasi.biaya_omloop5,"+
                                    "operasi.biayasarpras,operasi.biaya_dokter_pjanak,operasi.biaya_dokter_umum,"+
                                    "(operasi.biayaoperator1+operasi.biayaoperator2+operasi.biayaoperator3+"+
                                    "operasi.biayaasisten_operator1+operasi.biayaasisten_operator2+operasi.biayaasisten_operator3+operasi.biayainstrumen+"+
                                    "operasi.biayadokter_anak+operasi.biayaperawaat_resusitas+operasi.biayadokter_anestesi+"+
                                    "operasi.biayaasisten_anestesi+operasi.biayaasisten_anestesi2+operasi.biayabidan+operasi.biayabidan2+operasi.biayabidan3+operasi.biayaperawat_luar+operasi.biayaalat+"+
                                    "operasi.biayasewaok+operasi.akomodasi+operasi.bagian_rs+operasi.biaya_omloop+operasi.biaya_omloop2+operasi.biaya_omloop3+operasi.biaya_omloop4+operasi.biaya_omloop5+"+
                                    "operasi.biayasarpras+operasi.biaya_dokter_pjanak+operasi.biaya_dokter_umum) as total "+
                                    "from operasi inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "+
                                    "where operasi.no_rawat='"+rs.getString("no_rawat")+"' order by operasi.tgl_operasi").executeQuery();
                            if(rs2.next()){                                    
                                htmlContent.append(  
                                  "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                    "<tr><td valign='top' colspan='4'>Operasi/VK</td><td valign='top' colspan='1' align='right'>:</td><td valign='top'></td></tr>"+            
                                    "<tr align='center'>"+
                                      "<td valign='top' width='4%' bgcolor='#FFFAF8'>No.</td>"+
                                      "<td valign='top' width='15%' bgcolor='#FFFAF8'>Tanggal</td>"+
                                      "<td valign='top' width='10%' bgcolor='#FFFAF8'>Kode</td>"+
                                      "<td valign='top' width='51%' bgcolor='#FFFAF8'>Nama Tindakan</td>"+
                                      "<td valign='top' width='10%' bgcolor='#FFFAF8'>Anastesi</td>"+
                                      "<td valign='top' width='10%' bgcolor='#FFFAF8'>Biaya</td>"+
                                    "</tr>");
                                rs2.beforeFirst();
                                w=1;
                                while(rs2.next()){
                                    htmlContent.append(
                                         "<tr>"+
                                            "<td valign='top' align='center'>"+w+"</td>"+
                                            "<td valign='top'>"+rs2.getString("tgl_operasi")+"</td>"+
                                            "<td valign='top'>"+rs2.getString("kode_paket")+"</td>"+
                                            "<td valign='top'>"+rs2.getString("nm_perawatan")+" (");
                                    if(rs2.getDouble("biayaoperator1")>0){
                                        htmlContent.append("Operator 1 : "+Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs2.getString("operator1"))+", ");
                                    }
                                    if(rs2.getDouble("biayaoperator2")>0){
                                        htmlContent.append("Operator 2 : "+Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs2.getString("operator2"))+", ");
                                    }
                                    if(rs2.getDouble("biayaoperator3")>0){
                                        htmlContent.append("Operator 3 : "+Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs2.getString("operator3"))+", ");
                                    }
                                    if(rs2.getDouble("biayaasisten_operator1")>0){
                                        htmlContent.append("Asisten Operator 1 : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("asisten_operator1"))+", ");
                                    }
                                    if(rs2.getDouble("biayaasisten_operator2")>0){
                                        htmlContent.append("Asisten Operator 2 : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("asisten_operator2"))+", ");
                                    }
                                    if(rs2.getDouble("biayaasisten_operator3")>0){
                                        htmlContent.append("Asisten Operator 3 : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("asisten_operator3"))+", ");
                                    }
                                    if(rs2.getDouble("biayainstrumen")>0){
                                        htmlContent.append("Instrumen : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("instrumen"))+", ");
                                    }
                                    if(rs2.getDouble("biayadokter_anak")>0){
                                        htmlContent.append("Dokter Anak : "+Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs2.getString("dokter_anak"))+", ");
                                    }
                                    if(rs2.getDouble("biayaperawaat_resusitas")>0){
                                        htmlContent.append("Perawat Resusitas : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("perawaat_resusitas"))+", ");
                                    }
                                    if(rs2.getDouble("biayadokter_anestesi")>0){
                                        htmlContent.append("Dokter Anestesi : "+Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs2.getString("dokter_anestesi"))+", ");
                                    }
                                    if(rs2.getDouble("biayaasisten_anestesi")>0){
                                        htmlContent.append("Asisten Anestesi : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("asisten_anestesi"))+", ");
                                    }
                                    if(rs2.getDouble("biayaasisten_anestesi2")>0){
                                        htmlContent.append("Asisten Anestesi 2 : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("asisten_anestesi2"))+", ");
                                    }
                                    if(rs2.getDouble("biayabidan")>0){
                                        htmlContent.append("Bidan 1 : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("bidan"))+", ");
                                    }
                                    if(rs2.getDouble("biayabidan2")>0){
                                        htmlContent.append("Bidan 2 : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("bidan2"))+", ");
                                    }
                                    if(rs2.getDouble("biayabidan3")>0){
                                        htmlContent.append("Bidan 3 : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("bidan3"))+", ");
                                    }
                                    if(rs2.getDouble("biayaperawat_luar")>0){
                                        htmlContent.append("Perawat Luar : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("perawat_luar"))+", ");
                                    }
                                    if(rs2.getDouble("biaya_omloop")>0){
                                        htmlContent.append("Onloop 1 : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("omloop"))+", ");
                                    }
                                    if(rs2.getDouble("biaya_omloop2")>0){
                                        htmlContent.append("Onloop 2 : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("omloop2"))+", ");
                                    }
                                    if(rs2.getDouble("biaya_omloop3")>0){
                                        htmlContent.append("Onloop 3 : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("omloop3"))+", ");
                                    }
                                    if(rs2.getDouble("biaya_omloop4")>0){
                                        htmlContent.append("Onloop 4 : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("omloop4"))+", ");
                                    }
                                    if(rs2.getDouble("biaya_omloop5")>0){
                                        htmlContent.append("Onloop 5 : "+Sequel.cariIsi("select petugas.nama from petugas where petugas.nip=?",rs2.getString("omloop5"))+", ");
                                    }
                                    if(rs2.getDouble("biaya_dokter_pjanak")>0){
                                        htmlContent.append("Dokter Pj Anak : "+Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs2.getString("dokter_pjanak"))+", ");
                                    }
                                    if(rs2.getDouble("biaya_dokter_umum")>0){
                                        htmlContent.append("Dokter Umum : "+Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",rs2.getString("dokter_umum"))+", ");
                                    }
                                    htmlContent.append(
                                            ")</td>"+
                                            "<td valign='top'>"+rs2.getString("jenis_anasthesi")+"</td>"+
                                            "<td valign='top' align='right'>"+Valid.SetAngka(rs2.getDouble("total"))+"</td>"+
                                         "</tr>"); 
                                    w++;
                                    biayaperawatan=biayaperawatan+rs2.getDouble("total");
                                }
                                htmlContent.append(
                                  "</table>");
                            }                                
                        } catch (Exception e) {
                            System.out.println("Notifikasi : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                        }
                       
                        try{
                            rs2=koneksi.prepareStatement(
                                    "select tanggal, diagnosa_preop, diagnosa_postop, jaringan_dieksekusi, selesaioperasi, permintaan_pa, laporan_operasi "+
                                    "from laporan_operasi where no_rawat='"+rs.getString("no_rawat")+"' group by no_rawat,tanggal order by tanggal").executeQuery();
                            if(rs2.next()){                                    
                                htmlContent.append(  
                                  "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                    "<tr><td valign='top' colspan='3'>Laporan Operasi :</td></tr>");
                                rs2.beforeFirst();
                                w=1;
                                while(rs2.next()){
                                    htmlContent.append(
                                         "<tr>"+
                                            "<td valign='top' width='4%' align='center'>"+w+"</td>"+
                                            "<td valign='top' width='21%'>Mulai Operasi</td>"+
                                            "<td valign='top' width='75%'>:&nbsp;"+rs2.getString("tanggal")+"</td>"+
                                         "</tr>"+
                                         "<tr>"+
                                            "<td valign='top' width='4%' align='center'></td>"+
                                            "<td valign='top' width='21%'>Diagnosa Pre-operatif</td>"+
                                            "<td valign='top' width='75%'>:&nbsp;"+rs2.getString("diagnosa_preop")+"</td>"+
                                         "</tr>"+
                                         "<tr>"+
                                            "<td valign='top' width='4%' align='center'></td>"+
                                            "<td valign='top' width='21%'>Jaringan Yang di-Eksisi/-Insisi</td>"+
                                            "<td valign='top' width='75%'>:&nbsp;"+rs2.getString("jaringan_dieksekusi")+"</td>"+
                                         "</tr>"+
                                         "<tr>"+
                                            "<td valign='top' width='4%' align='center'></td>"+
                                            "<td valign='top' width='21%'>Diagnosa Post-operatif</td>"+
                                            "<td valign='top' width='75%'>:&nbsp;"+rs2.getString("diagnosa_postop")+"</td>"+
                                         "</tr>"+
                                         "<tr>"+
                                            "<td valign='top' width='4%' align='center'></td>"+
                                            "<td valign='top' width='21%'>Selesai Operasi</td>"+
                                            "<td valign='top' width='75%'>:&nbsp;"+rs2.getString("selesaioperasi")+"</td>"+
                                         "</tr>"+
                                         "<tr>"+
                                            "<td valign='top' width='4%' align='center'></td>"+
                                            "<td valign='top' width='21%'>Dikirim Untuk Pemeriksaan PA</td>"+
                                            "<td valign='top' width='75%'>:&nbsp;"+rs2.getString("permintaan_pa")+"</td>"+
                                         "</tr>"+
                                         "<tr>"+
                                            "<td valign='top' width='4%' align='center'></td>"+
                                            "<td valign='top' width='21%'>Laporan</td>"+
                                            "<td valign='top' width='75%'>:&nbsp;"+rs2.getString("laporan_operasi").replaceAll("(\r\n|\r|\n|\n\r)","<br>")+"</td>"+
                                         "</tr>"); 
                                    w++;
                                }
                                htmlContent.append(
                                  "</table>");
                            }                                
                        } catch (Exception e) {
                            System.out.println("Notifikasi : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                        }
                    }
                    
                    //menampilkan penggunaan obat operasi
                    if(chkPenggunaanObatOperasi.isSelected()==true){
                        try{
                            rs2=koneksi.prepareStatement(
                                "select beri_obat_operasi.tanggal,beri_obat_operasi.kd_obat,beri_obat_operasi.hargasatuan,obatbhp_ok.kode_sat, "+
                                "beri_obat_operasi.jumlah, obatbhp_ok.nm_obat,(beri_obat_operasi.hargasatuan*beri_obat_operasi.jumlah) as total "+
                                "from beri_obat_operasi inner join obatbhp_ok  on  beri_obat_operasi.kd_obat=obatbhp_ok.kd_obat  "+
                                "where beri_obat_operasi.no_rawat='"+rs.getString("no_rawat")+"' order by beri_obat_operasi.tanggal").executeQuery();
                            if(rs2.next()){                                    
                                htmlContent.append(  
                                  "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                    "<tr><td valign='top' colspan='4'>Penggunaan Obat/BHP Operasi</td><td valign='top' colspan='1' align='right'>:</td><td></td></tr>"+            
                                    "<tr align='center'>"+
                                      "<td valign='top' width='4%' bgcolor='#FFFAF8'>No.</td>"+
                                      "<td valign='top' width='15%' bgcolor='#FFFAF8'>Tanggal</td>"+
                                      "<td valign='top' width='10%' bgcolor='#FFFAF8'>Kode</td>"+
                                      "<td valign='top' width='51%' bgcolor='#FFFAF8'>Nama Obat/BHP</td>"+
                                      "<td valign='top' width='10%' bgcolor='#FFFAF8'>Jumlah</td>"+
                                      "<td valign='top' width='10%' bgcolor='#FFFAF8'>Biaya</td>"+
                                    "</tr>");
                                rs2.beforeFirst();
                                w=1;
                                while(rs2.next()){
                                    htmlContent.append(
                                         "<tr>"+
                                            "<td valign='top' align='center'>"+w+"</td>"+
                                            "<td valign='top'>"+rs2.getString("tanggal")+"</td>"+
                                            "<td valign='top'>"+rs2.getString("kd_obat")+"</td>"+
                                            "<td valign='top'>"+rs2.getString("nm_obat")+"</td>"+
                                            "<td valign='top'>"+rs2.getDouble("jumlah")+" "+rs2.getString("kode_sat")+"</td>"+
                                            "<td valign='top' align='right'>"+Valid.SetAngka(rs2.getDouble("total"))+"</td>"+
                                         "</tr>"); 
                                    w++;
                                    biayaperawatan=biayaperawatan+rs2.getDouble("total");
                                }
                                htmlContent.append(
                                  "</table>");
                            }                                
                        } catch (Exception e) {
                            System.out.println("Notifikasi : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                        }
                    }
                    
                    if(R4.isSelected()==true){
                        if(rs.getString("status_lanjut").equals("Ralan")){
                            get = new GetMethod("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/penggajian/generateqrcode.php?kodedokter="+rs.getString("kd_dokter").replace(" ","_"));
                            http.executeMethod(get);

                            htmlContent.append(
                                "<tr class='isi'>"+ 
                                   "<td valign='top' width='2%'></td>"+        
                                   "<td valign='middle' width='18%'>Tanda Tangan/Verifikasi</td>"+
                                   "<td valign='middle' width='1%' align='center'>:</td>"+
                                   "<td valign='middle' width='79%' align='center'>Dokter Poli<br><img width='90' height='90' src='http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/penggajian/temp/"+rs.getString("kd_dokter")+".png'/><br>"+rs.getString("nm_dokter")+"</td>"+
                                "</tr>"
                            );
                        }else if(rs.getString("status_lanjut").equals("Ranap")){
                            try{
                                rs3=koneksi.prepareStatement(
                                    "select dpjp_ranap.kd_dokter,dokter.nm_dokter from dpjp_ranap inner join dokter on dpjp_ranap.kd_dokter=dokter.kd_dokter where dpjp_ranap.no_rawat='"+rs.getString("no_rawat")+"'").executeQuery();
                                if(rs3.next()){
                                    htmlContent.append(
                                        "<tr class='isi'>"+ 
                                          "<td valign='top' width='2%'></td>"+        
                                          "<td valign='middle' width='18%'>Tanda Tangan/Verifikasi</td>"+
                                          "<td valign='middle' width='1%' align='center'>:</td>"+
                                          "<td valign='top' width='79%' align='center'>"+
                                              "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                                 "<tr class='isi'>"
                                      );
                                      rs3.beforeFirst();
                                      urutdpjp=1;
                                      while(rs3.next()){
                                          get = new GetMethod("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/penggajian/generateqrcode.php?kodedokter="+rs3.getString("kd_dokter").replace(" ","_"));
                                          http.executeMethod(get);
                                          htmlContent.append("<td border='0' align='center'>Dokter DPJP "+urutdpjp+"<br><img width='90' height='90' src='http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/penggajian/temp/"+rs3.getString("kd_dokter")+".png'/><br>"+rs3.getString("nm_dokter")+"</td>");
                                          urutdpjp++;
                                      }
                                      htmlContent.append(
                                                  "</tr>"+
                                              "</table>"+
                                          "</td>"+
                                        "</tr>"
                                      );    
                                }else{
                                    get = new GetMethod("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/penggajian/generateqrcode.php?kodedokter="+rs.getString("kd_dokter").replace(" ","_"));
                                    http.executeMethod(get);

                                    htmlContent.append(
                                        "<tr class='isi'>"+ 
                                           "<td valign='top' width='2%'></td>"+        
                                           "<td valign='middle' width='18%'>Tanda Tangan/Verifikasi</td>"+
                                           "<td valign='middle' width='1%' align='center'>:</td>"+
                                           "<td valign='middle' width='79%' align='center'>Dokter DPJP<br><img width='90' height='90' src='http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/penggajian/temp/"+rs.getString("kd_dokter")+".png'/><br>"+rs.getString("nm_dokter")+"</td>"+
                                        "</tr>"
                                    );
                                }
                            } catch (Exception e) {
                                System.out.println("Tanda Tangan IGD : "+e);
                            } finally{
                                if(rs3!=null){
                                    rs3.close();
                                }
                            }
                        }
                    }
                    htmlContent.append(
                        "<tr class='isi'><td></td><td colspan='3' align='right'>&nbsp;</tr>"
                    );
                    
                }
                
                LoadHTMLRiwayatPerawatan.setText(
                    "<html>"+
                      "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                       htmlContent.toString()+
                      "</table>"+
                    "</html>");
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
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }
    
    private void panggilLaporan(String teks) {
        try{
            File g = new File("file.css");            
            BufferedWriter bg = new BufferedWriter(new FileWriter(g));
            bg.write(".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}.isi a{text-decoration:none;color:#8b9b95;padding:0 0 0 0px;font-family: Tahoma;font-size: 8.5px;border: white;}");
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

    private void menampilkanChecklistPreOperasi(String norawat) {
        try {
            if(chkChecklistPreOperasi.isSelected()==true){
                try {
                    rs2=koneksi.prepareStatement(
                            "select checklist_pre_operasi.tanggal,checklist_pre_operasi.sncn,checklist_pre_operasi.tindakan,checklist_pre_operasi.kd_dokter_bedah,"+
                            "dokterbedah.nm_dokter as dokterbedah,checklist_pre_operasi.kd_dokter_anestesi,dokteranestesi.nm_dokter as dokteranestesi,checklist_pre_operasi.identitas,"+
                            "checklist_pre_operasi.surat_ijin_bedah,checklist_pre_operasi.surat_ijin_anestesi,checklist_pre_operasi.surat_ijin_transfusi,"+
                            "checklist_pre_operasi.penandaan_area_operasi,checklist_pre_operasi.keadaan_umum,checklist_pre_operasi.pemeriksaan_penunjang_rontgen,"+
                            "checklist_pre_operasi.keterangan_pemeriksaan_penunjang_rontgen,checklist_pre_operasi.pemeriksaan_penunjang_ekg,"+
                            "checklist_pre_operasi.keterangan_pemeriksaan_penunjang_ekg,checklist_pre_operasi.pemeriksaan_penunjang_usg,"+
                            "checklist_pre_operasi.keterangan_pemeriksaan_penunjang_usg,checklist_pre_operasi.pemeriksaan_penunjang_ctscan,"+
                            "checklist_pre_operasi.keterangan_pemeriksaan_penunjang_ctscan,checklist_pre_operasi.pemeriksaan_penunjang_mri,"+
                            "checklist_pre_operasi.keterangan_pemeriksaan_penunjang_mri,checklist_pre_operasi.persiapan_darah,checklist_pre_operasi.keterangan_persiapan_darah,"+
                            "checklist_pre_operasi.perlengkapan_khusus,checklist_pre_operasi.nip_petugas_ruangan,petugasruangan.nama as petugasruangan,"+
                            "checklist_pre_operasi.nip_perawat_ok,petugasok.nama as petugasok "+
                            "from checklist_pre_operasi inner join dokter as dokterbedah on dokterbedah.kd_dokter=checklist_pre_operasi.kd_dokter_bedah "+
                            "inner join dokter as dokteranestesi on dokteranestesi.kd_dokter=checklist_pre_operasi.kd_dokter_anestesi "+
                            "inner join petugas as petugasruangan on petugasruangan.nip=checklist_pre_operasi.nip_petugas_ruangan "+
                            "inner join petugas as petugasok on petugasok.nip=checklist_pre_operasi.nip_perawat_ok "+
                            "where checklist_pre_operasi.no_rawat='"+norawat+"' order by checklist_pre_operasi.tanggal").executeQuery();
                    if(rs2.next()){
                        htmlContent.append(
                          "<tr class='isi'>"+ 
                            "<td valign='top' width='2%'></td>"+        
                            "<td valign='top' width='18%'>Check List Pre Operasi</td>"+
                            "<td valign='top' width='1%' align='center'>:</td>"+
                            "<td valign='top' width='79%'>"+
                              "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                        );
                        rs2.beforeFirst();
                        while(rs2.next()){
                            htmlContent.append(
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "YANG MELAKUKAN PENGKAJIAN"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='50%' border='0'>Petugas Ruangan : "+rs2.getString("nip_petugas_ruangan")+" "+rs2.getString("petugasruangan")+"</td>"+
                                              "<td width='50%' border='0'>Perawat Kamar Operasi : "+rs2.getString("nip_perawat_ok")+" "+rs2.getString("petugasok")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "RENCANA OPERASI"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Tanggal : "+rs2.getString("tanggal")+"</td>"+
                                              "<td width='67%' border='0'>Dokter Bedah : "+rs2.getString("kd_dokter_bedah")+" "+rs2.getString("dokterbedah")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>SN/CN : "+rs2.getString("sncn")+"</td>"+
                                              "<td width='67%' border='0'>Dokter Anestesi : "+rs2.getString("kd_dokter_anestesi")+" "+rs2.getString("dokteranestesi")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' border='0' colspan='2'>Tindakan/Operasi : "+rs2.getString("tindakan")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "PERAWAT MELAKUKAN KONFIRMASI"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Identitas : "+rs2.getString("identitas")+"</td>"+
                                              "<td width='33%' border='0'>Keadaan Umum Pasien : "+rs2.getString("keadaan_umum")+"</td>"+
                                              "<td width='33%' border='0'>Penandaan Area Operasi : "+rs2.getString("penandaan_area_operasi")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Surat Ijin Bedah : "+rs2.getString("surat_ijin_bedah")+"</td>"+
                                              "<td width='33%' border='0'>Surat Ijin Anestesi : "+rs2.getString("surat_ijin_anestesi")+"</td>"+
                                              "<td width='33%' border='0'>Surat Ijin Tranfusi : "+rs2.getString("surat_ijin_transfusi")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='66%' border='0' colspan='2'>Persiapan Darah : "+rs2.getString("persiapan_darah")+(rs2.getString("keterangan_persiapan_darah").equals("")?"":", "+rs2.getString("keterangan_persiapan_darah"))+"</td>"+
                                              "<td width='33%' border='0'>Perlengkapan Khusus, Alat/Implan : "+rs2.getString("perlengkapan_khusus")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' colspan='3'>"+
                                                  "Hasil Pemeriksaan Penunjang :"+
                                                  "<table width='98%' border='0' align='right' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                                     "<tr>"+
                                                        "<td width='50%' border='0'>Radiologi : "+rs2.getString("pemeriksaan_penunjang_rontgen")+(rs2.getString("keterangan_pemeriksaan_penunjang_rontgen").equals("")?"":", "+rs2.getString("keterangan_pemeriksaan_penunjang_rontgen"))+"</td>"+
                                                        "<td width='50%' border='0'>EKG : "+rs2.getString("pemeriksaan_penunjang_ekg")+(rs2.getString("keterangan_pemeriksaan_penunjang_ekg").equals("")?"":", "+rs2.getString("keterangan_pemeriksaan_penunjang_ekg"))+"</td>"+
                                                     "</tr>"+
                                                     "<tr>"+
                                                        "<td width='50%' border='0'>USG : "+rs2.getString("pemeriksaan_penunjang_usg")+(rs2.getString("keterangan_pemeriksaan_penunjang_usg").equals("")?"":", "+rs2.getString("keterangan_pemeriksaan_penunjang_usg"))+"</td>"+
                                                        "<td width='50%' border='0'>CT Scan : "+rs2.getString("pemeriksaan_penunjang_ctscan")+(rs2.getString("keterangan_pemeriksaan_penunjang_ctscan").equals("")?"":", "+rs2.getString("keterangan_pemeriksaan_penunjang_ctscan"))+"</td>"+
                                                     "</tr>"+
                                                     "<tr>"+
                                                        "<td width='100%' border='0' colspan='2'>MRI : "+rs2.getString("pemeriksaan_penunjang_mri")+(rs2.getString("keterangan_pemeriksaan_penunjang_mri").equals("")?"":", "+rs2.getString("keterangan_pemeriksaan_penunjang_mri"))+"</td>"+
                                                     "</tr>"+
                                                  "</table>"+
                                              "</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"); 
                        }
                        htmlContent.append(
                              "</table>"+
                            "</td>"+
                          "</tr>");
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                } finally{
                    if(rs2!=null){
                        rs2.close();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Checklist Pre Operasi : "+e);
        }
    }
    
    private void menampilkanSignInSebelumAnestesi(String norawat) {
        try {
            if(chkSignInSebelumAnestesi.isSelected()==true){
                try {
                    rs2=koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,pasien.jk,signin_sebelum_anestesi.tanggal,"+
                            "signin_sebelum_anestesi.sncn,signin_sebelum_anestesi.tindakan,signin_sebelum_anestesi.kd_dokter_bedah,dokterbedah.nm_dokter as dokterbedah,"+
                            "signin_sebelum_anestesi.kd_dokter_anestesi,dokteranestesi.nm_dokter as dokteranestesi,signin_sebelum_anestesi.identitas,signin_sebelum_anestesi.penandaan_area_operasi,"+
                            "signin_sebelum_anestesi.alergi,signin_sebelum_anestesi.resiko_aspirasi,signin_sebelum_anestesi.resiko_aspirasi_rencana_antisipasi,"+
                            "signin_sebelum_anestesi.resiko_kehilangan_darah,signin_sebelum_anestesi.resiko_kehilangan_darah_line,signin_sebelum_anestesi.resiko_kehilangan_darah_rencana_antisipasi,"+
                            "signin_sebelum_anestesi.kesiapan_alat_obat_anestesi,signin_sebelum_anestesi.kesiapan_alat_obat_anestesi_rencana_antisipasi,signin_sebelum_anestesi.nip_perawat_ok,"+
                            "petugas.nama from signin_sebelum_anestesi inner join reg_periksa on signin_sebelum_anestesi.no_rawat=reg_periksa.no_rawat "+
                            "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join dokter as dokterbedah on dokterbedah.kd_dokter=signin_sebelum_anestesi.kd_dokter_bedah "+
                            "inner join dokter as dokteranestesi on dokteranestesi.kd_dokter=signin_sebelum_anestesi.kd_dokter_anestesi "+
                            "inner join petugas on petugas.nip=signin_sebelum_anestesi.nip_perawat_ok "+
                            "where signin_sebelum_anestesi.no_rawat='"+norawat+"' order by signin_sebelum_anestesi.tanggal").executeQuery();
                    if(rs2.next()){
                        htmlContent.append(
                          "<tr class='isi'>"+ 
                            "<td valign='top' width='2%'></td>"+        
                            "<td valign='top' width='18%'>Sign-In Sebelum Tindakan Anestesi</td>"+
                            "<td valign='top' width='1%' align='center'>:</td>"+
                            "<td valign='top' width='79%'>"+
                              "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                        );
                        rs2.beforeFirst();
                        while(rs2.next()){
                            htmlContent.append(
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "YANG MELAKUKAN PENGKAJIAN"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='50%' border='0'>Dokter Anestesi : "+rs2.getString("kd_dokter_anestesi")+" "+rs2.getString("dokteranestesi")+"</td>"+
                                              "<td width='50%' border='0'>Perawat Kamar Operasi : "+rs2.getString("nip_perawat_ok")+" "+rs2.getString("nama")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "RENCANA OPERASI"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Tanggal : "+rs2.getString("tanggal")+"</td>"+
                                              "<td width='67%' border='0'>Dokter Bedah : "+rs2.getString("kd_dokter_bedah")+" "+rs2.getString("dokterbedah")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>SN/CN : "+rs2.getString("sncn")+"</td>"+
                                              "<td width='67%' border='0'>Tindakan/Operasi : "+rs2.getString("tindakan")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "PERAWAT OK DAN TIM ANESTESI MELAKUKAN KONFIRMASI"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Identitas : "+rs2.getString("identitas")+"</td>"+
                                              "<td width='33%' border='0'>Alergi : "+rs2.getString("alergi")+"</td>"+
                                              "<td width='33%' border='0'>Penandaan Area Operasi : "+rs2.getString("penandaan_area_operasi")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Resiko Aspirasi & Faktor Penyulit : "+rs2.getString("resiko_aspirasi")+"</td>"+
                                              "<td width='66%' border='0' colspan='2'>Bila Ada Resiko, Rencana Antisipasi : "+rs2.getString("resiko_aspirasi_rencana_antisipasi")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='66%' border='0' colspan='2'>Resiko Kehilangan Darah > 500 ml (7 ml/Kg Berat Badan Untuk Anak) : "+rs2.getString("resiko_kehilangan_darah")+"</td>"+
                                              "<td width='33%' border='0'>Jika Ada, Jalur IV Line : "+rs2.getString("resiko_kehilangan_darah_line")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' border='0' colspan='3' style='margin-left: 10px'>Jika Ada Resiko Kehilangan Darah, Rencana Antisipasi : "+rs2.getString("resiko_kehilangan_darah_rencana_antisipasi")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Kesiapan Alat & Obat Anestesi : "+rs2.getString("kesiapan_alat_obat_anestesi")+"</td>"+
                                              "<td width='66%' border='0' colspan='2'>Bila Tidak Lengkap, Rencana Antisipasi : "+rs2.getString("kesiapan_alat_obat_anestesi_rencana_antisipasi")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"); 
                        }
                        htmlContent.append(
                              "</table>"+
                            "</td>"+
                          "</tr>");
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                } finally{
                    if(rs2!=null){
                        rs2.close();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Checklist Pre Operasi : "+e);
        }
    }
    
    private void menampilkanTimeOutSebelumInsisi(String norawat) {
        try {
            if(chkTimeOutSebelumInsisi.isSelected()==true){
                try {
                    rs2=koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,pasien.jk,timeout_sebelum_insisi.tanggal,"+
                            "timeout_sebelum_insisi.sncn,timeout_sebelum_insisi.tindakan,timeout_sebelum_insisi.kd_dokter_bedah,dokterbedah.nm_dokter as dokterbedah,"+
                            "timeout_sebelum_insisi.kd_dokter_anestesi,dokteranestesi.nm_dokter as dokteranestesi,timeout_sebelum_insisi.verbal_identitas,"+
                            "timeout_sebelum_insisi.verbal_tindakan,timeout_sebelum_insisi.verbal_area_insisi,timeout_sebelum_insisi.penandaan_area_operasi,"+
                            "timeout_sebelum_insisi.lama_operasi,timeout_sebelum_insisi.penayangan_radiologi,timeout_sebelum_insisi.penayangan_ctscan,"+
                            "timeout_sebelum_insisi.penayangan_mri,timeout_sebelum_insisi.antibiotik_profilaks,timeout_sebelum_insisi.nama_antibiotik,"+
                            "timeout_sebelum_insisi.jam_pemberian,timeout_sebelum_insisi.antisipasi_kehilangan_darah,timeout_sebelum_insisi.hal_khusus,"+
                            "timeout_sebelum_insisi.hal_khusus_diperhatikan,timeout_sebelum_insisi.tanggal_steril,timeout_sebelum_insisi.petujuk_sterilisasi,"+
                            "timeout_sebelum_insisi.verifikasi_preoperatif,timeout_sebelum_insisi.nip_perawat_ok,petugas.nama "+
                            "from timeout_sebelum_insisi inner join reg_periksa on timeout_sebelum_insisi.no_rawat=reg_periksa.no_rawat "+
                            "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join dokter as dokterbedah on dokterbedah.kd_dokter=timeout_sebelum_insisi.kd_dokter_bedah "+
                            "inner join dokter as dokteranestesi on dokteranestesi.kd_dokter=timeout_sebelum_insisi.kd_dokter_anestesi "+
                            "inner join petugas on petugas.nip=timeout_sebelum_insisi.nip_perawat_ok "+
                            "where timeout_sebelum_insisi.no_rawat='"+norawat+"' order by timeout_sebelum_insisi.tanggal").executeQuery();
                    if(rs2.next()){
                        htmlContent.append(
                          "<tr class='isi'>"+ 
                            "<td valign='top' width='2%'></td>"+        
                            "<td valign='top' width='18%'>Time-Out Sebelum Tindakan Insisi</td>"+
                            "<td valign='top' width='1%' align='center'>:</td>"+
                            "<td valign='top' width='79%'>"+
                              "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                        );
                        rs2.beforeFirst();
                        while(rs2.next()){
                            htmlContent.append(
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "YANG MELAKUKAN PENGKAJIAN"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='50%' border='0'>Dokter Anestesi : "+rs2.getString("kd_dokter_anestesi")+" "+rs2.getString("dokteranestesi")+"</td>"+
                                              "<td width='50%' border='0'>Perawat Kamar Operasi : "+rs2.getString("nip_perawat_ok")+" "+rs2.getString("nama")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "RENCANA OPERASI"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Tanggal : "+rs2.getString("tanggal")+"</td>"+
                                              "<td width='67%' border='0'>Dokter Bedah : "+rs2.getString("kd_dokter_bedah")+" "+rs2.getString("dokterbedah")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>SN/CN : "+rs2.getString("sncn")+"</td>"+
                                              "<td width='67%' border='0'>Tindakan/Operasi : "+rs2.getString("tindakan")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "KONFIRMASI DIPIMPIN OLEH SALAH SATU ANGGOTA TIM, SEMUA KEGIATAN DITANGGUHKAN KECUALI JIKA MENGANCAM JIWA"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+
                                                 "Verbalisasi Tim, Konfirmasi :"+
                                                 "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                                    "<tr>"+
                                                        "<td width='33%' border='0' style='margin-left: 10px'>Identitas : "+rs2.getString("verbal_identitas")+"</td>"+
                                                        "<td width='33%' border='0' style='margin-left: 10px'>Tindakan : "+rs2.getString("verbal_tindakan")+"</td>"+
                                                        "<td width='33%' border='0' style='margin-left: 10px'>Area Insisi : "+rs2.getString("verbal_area_insisi")+"</td>"+
                                                    "</tr>"+
                                                 "</table>"+
                                              "</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+
                                                 "Penandaan Area Operasi : "+rs2.getString("penandaan_area_operasi")+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+
                                                 "Perkiraan Lama Operasi : "+rs2.getString("lama_operasi")+
                                              "</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+
                                                 "Penayangan Hasil Pemeriksaan Penunjang :"+
                                                 "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                                    "<tr>"+
                                                        "<td width='33%' border='0' style='margin-left: 10px'>Radiologi : "+rs2.getString("penayangan_radiologi")+"</td>"+
                                                        "<td width='33%' border='0' style='margin-left: 10px'>CT Scan : "+rs2.getString("penayangan_ctscan")+"</td>"+
                                                        "<td width='33%' border='0' style='margin-left: 10px'>MRI : "+rs2.getString("penayangan_mri")+"</td>"+
                                                    "</tr>"+
                                                 "</table>"+
                                              "</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+
                                                 "Pemberian Antibiotik Profilaksis : "+rs2.getString("antibiotik_profilaks")+(rs2.getString("antibiotik_profilaks").equals("Ya")?"":", Jika Diberikan : "+rs2.getString("nama_antibiotik")+", Jam Pemberian : "+rs2.getString("jam_pemberian"))+
                                              "</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+
                                                 "Antisipasi Kehilangan Darah > 500 ml (7 ml/Kg BB Untuk Anak) : "+rs2.getString("antisipasi_kehilangan_darah")+
                                              "</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+
                                                 "Hal Khusus Yang Perlu Perhatian : "+rs2.getString("hal_khusus")+(rs2.getString("hal_khusus").equals("Ada")?"":", Jika Ada "+rs2.getString("hal_khusus_diperhatikan"))+
                                              "</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+
                                                 "Tanggal Steril "+rs2.getString("tanggal_steril")+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Petunjuk Sterilisasi Telah Dikonfirmasi : "+rs2.getString("petujuk_sterilisasi")+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Verifikasi Pre Operatif Telah Dilakukan : "+rs2.getString("verifikasi_preoperatif")+
                                              "</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"); 
                        }
                        htmlContent.append(
                              "</table>"+
                            "</td>"+
                          "</tr>");
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                } finally{
                    if(rs2!=null){
                        rs2.close();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Checklist Pre Operasi : "+e);
        }
    }
    
    private void menampilkanSignOutSebelumMenutupLuka(String norawat) {
        try {
            if(chkSignOutSebelumMenutupLuka.isSelected()==true){
                try {
                    rs2=koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,pasien.jk,signout_sebelum_menutup_luka.tanggal,"+
                            "signout_sebelum_menutup_luka.sncn,signout_sebelum_menutup_luka.tindakan,signout_sebelum_menutup_luka.kd_dokter_bedah,dokterbedah.nm_dokter as dokterbedah,"+
                            "signout_sebelum_menutup_luka.kd_dokter_anestesi,dokteranestesi.nm_dokter as dokteranestesi,signout_sebelum_menutup_luka.verbal_tindakan,"+
                            "signout_sebelum_menutup_luka.verbal_kelengkapan_kasa,signout_sebelum_menutup_luka.verbal_instrumen,signout_sebelum_menutup_luka.verbal_alat_tajam,"+
                            "signout_sebelum_menutup_luka.kelengkapan_specimen_label,signout_sebelum_menutup_luka.kelengkapan_specimen_formulir,"+
                            "signout_sebelum_menutup_luka.peninjauan_kegiatan_dokter_bedah,signout_sebelum_menutup_luka.peninjauan_kegiatan_dokter_anestesi,"+
                            "signout_sebelum_menutup_luka.peninjauan_kegiatan_perawat_kamar_ok,signout_sebelum_menutup_luka.perhatian_utama_fase_pemulihan,"+
                            "signout_sebelum_menutup_luka.nip_perawat_ok,petugas.nama from signout_sebelum_menutup_luka inner join reg_periksa "+
                            "on signout_sebelum_menutup_luka.no_rawat=reg_periksa.no_rawat inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join dokter as dokterbedah on dokterbedah.kd_dokter=signout_sebelum_menutup_luka.kd_dokter_bedah "+
                            "inner join dokter as dokteranestesi on dokteranestesi.kd_dokter=signout_sebelum_menutup_luka.kd_dokter_anestesi "+
                            "inner join petugas on petugas.nip=signout_sebelum_menutup_luka.nip_perawat_ok "+
                            "where signout_sebelum_menutup_luka.no_rawat='"+norawat+"' order by signout_sebelum_menutup_luka.tanggal").executeQuery();
                    if(rs2.next()){
                        htmlContent.append(
                          "<tr class='isi'>"+ 
                            "<td valign='top' width='2%'></td>"+        
                            "<td valign='top' width='18%'>Sign-Out Sebelum Menutup Luka</td>"+
                            "<td valign='top' width='1%' align='center'>:</td>"+
                            "<td valign='top' width='79%'>"+
                              "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                        );
                        rs2.beforeFirst();
                        while(rs2.next()){
                            htmlContent.append(
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "YANG MELAKUKAN PENGKAJIAN"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='50%' border='0'>Dokter Anestesi : "+rs2.getString("kd_dokter_anestesi")+" "+rs2.getString("dokteranestesi")+"</td>"+
                                              "<td width='50%' border='0'>Perawat Kamar Operasi : "+rs2.getString("nip_perawat_ok")+" "+rs2.getString("nama")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "RENCANA OPERASI"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Tanggal : "+rs2.getString("tanggal")+"</td>"+
                                              "<td width='67%' border='0'>Dokter Bedah : "+rs2.getString("kd_dokter_bedah")+" "+rs2.getString("dokterbedah")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>SN/CN : "+rs2.getString("sncn")+"</td>"+
                                              "<td width='67%' border='0'>Tindakan/Operasi : "+rs2.getString("tindakan")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "SEBELUM MENUTUP LUKA & MENINGGALKAN KAMAR OPERASI"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+
                                                 "Perawat Melakukan Konfirmasi Secara Verbal :"+
                                                 "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                                    "<tr>"+
                                                        "<td width='25%' border='0' style='margin-left: 10px'>Tindakan : "+rs2.getString("verbal_tindakan")+"</td>"+
                                                        "<td width='25%' border='0' style='margin-left: 10px'>Kelengkapan Kasa : "+rs2.getString("verbal_kelengkapan_kasa")+"</td>"+
                                                        "<td width='25%' border='0' style='margin-left: 10px'>Instrumen : "+rs2.getString("verbal_instrumen")+"</td>"+
                                                        "<td width='25%' border='0' style='margin-left: 10px'>Alat Tajam : "+rs2.getString("verbal_alat_tajam")+"</td>"+
                                                    "</tr>"+
                                                 "</table>"+
                                              "</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+
                                                 "Kelengkapan Spesimen Jika Ada :"+
                                                 "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                                    "<tr>"+
                                                        "<td width='50%' border='0' style='margin-left: 10px'>Label : "+rs2.getString("kelengkapan_specimen_label")+"</td>"+
                                                        "<td width='50%' border='0' style='margin-left: 10px'>Formulir : "+rs2.getString("kelengkapan_specimen_formulir")+"</td>"+
                                                    "</tr>"+
                                                 "</table>"+
                                              "</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+
                                                 "Peninjauan Kembali Kegiatan :"+
                                                 "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                                    "<tr>"+
                                                        "<td width='33%' border='0' style='margin-left: 10px'>Dokter Bedah : "+rs2.getString("peninjauan_kegiatan_dokter_bedah")+"</td>"+
                                                        "<td width='33%' border='0' style='margin-left: 10px'>Dokter Anestesi : "+rs2.getString("peninjauan_kegiatan_dokter_anestesi")+"</td>"+
                                                        "<td width='33%' border='0' style='margin-left: 10px'>Perawat Kamar Operasi : "+rs2.getString("peninjauan_kegiatan_perawat_kamar_ok")+"</td>"+
                                                    "</tr>"+
                                                 "</table>"+
                                              "</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+
                                                 "Perhatian Utama Fase Pemulihan : "+rs2.getString("perhatian_utama_fase_pemulihan")+
                                              "</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"); 
                        }
                        htmlContent.append(
                              "</table>"+
                            "</td>"+
                          "</tr>");
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                } finally{
                    if(rs2!=null){
                        rs2.close();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Checklist Pre Operasi : "+e);
        }
    }
    
    private void menampilkanChecklistPostOperasi(String norawat) {
        try {
            if(chkChecklistPostOperasi.isSelected()==true){
                try {
                    rs2=koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,pasien.jk,checklist_post_operasi.tanggal,"+
                            "checklist_post_operasi.sncn,checklist_post_operasi.tindakan,checklist_post_operasi.kd_dokter_bedah,dokterbedah.nm_dokter as dokterbedah,"+
                            "checklist_post_operasi.kd_dokter_anestesi,dokteranestesi.nm_dokter as dokteranestesi,checklist_post_operasi.keadaan_umum,"+
                            "checklist_post_operasi.pemeriksaan_penunjang_rontgen,checklist_post_operasi.keterangan_pemeriksaan_penunjang_rontgen,"+
                            "checklist_post_operasi.pemeriksaan_penunjang_ekg,checklist_post_operasi.keterangan_pemeriksaan_penunjang_ekg,checklist_post_operasi.pemeriksaan_penunjang_usg,"+
                            "checklist_post_operasi.keterangan_pemeriksaan_penunjang_usg,checklist_post_operasi.pemeriksaan_penunjang_ctscan,"+
                            "checklist_post_operasi.keterangan_pemeriksaan_penunjang_ctscan,checklist_post_operasi.pemeriksaan_penunjang_mri,"+
                            "checklist_post_operasi.keterangan_pemeriksaan_penunjang_mri,checklist_post_operasi.jenis_cairan_infus,checklist_post_operasi.kateter_urine,"+
                            "checklist_post_operasi.tanggal_pemasangan_kateter,checklist_post_operasi.warna_kateter,checklist_post_operasi.jumlah_kateter,"+
                            "checklist_post_operasi.area_luka_operasi,checklist_post_operasi.drain,checklist_post_operasi.jumlah_drain,checklist_post_operasi.letak_drain,"+
                            "checklist_post_operasi.warna_drain,checklist_post_operasi.jaringan_pa,checklist_post_operasi.nip_perawat_ok,petugasok.nama as petugasok,"+
                            "checklist_post_operasi.nip_perawat_anestesi,petugasanestesi.nama as petugasanestesi "+
                            "from checklist_post_operasi inner join reg_periksa on checklist_post_operasi.no_rawat=reg_periksa.no_rawat "+
                            "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join dokter as dokterbedah on dokterbedah.kd_dokter=checklist_post_operasi.kd_dokter_bedah "+
                            "inner join dokter as dokteranestesi on dokteranestesi.kd_dokter=checklist_post_operasi.kd_dokter_anestesi "+
                            "inner join petugas as petugasanestesi on petugasanestesi.nip=checklist_post_operasi.nip_perawat_anestesi "+
                            "inner join petugas as petugasok on petugasok.nip=checklist_post_operasi.nip_perawat_ok "+
                            "where checklist_post_operasi.no_rawat='"+norawat+"' order by checklist_post_operasi.tanggal").executeQuery();
                    if(rs2.next()){
                        htmlContent.append(
                          "<tr class='isi'>"+ 
                            "<td valign='top' width='2%'></td>"+        
                            "<td valign='top' width='18%'>Check List Post Operasi</td>"+
                            "<td valign='top' width='1%' align='center'>:</td>"+
                            "<td valign='top' width='79%'>"+
                              "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                        );
                        rs2.beforeFirst();
                        while(rs2.next()){
                            htmlContent.append(
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "YANG MELAKUKAN PENGKAJIAN"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='50%' border='0'>Petugas Anestesi : "+rs2.getString("nip_perawat_anestesi")+" "+rs2.getString("petugasanestesi")+"</td>"+
                                              "<td width='50%' border='0'>Petugas Kamar Operasi : "+rs2.getString("nip_perawat_ok")+" "+rs2.getString("petugasok")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "RENCANA OPERASI"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Tanggal : "+rs2.getString("tanggal")+"</td>"+
                                              "<td width='67%' border='0'>Dokter Bedah : "+rs2.getString("kd_dokter_bedah")+" "+rs2.getString("dokterbedah")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>SN/CN : "+rs2.getString("sncn")+"</td>"+
                                              "<td width='67%' border='0'>Dokter Anestesi : "+rs2.getString("kd_dokter_anestesi")+" "+rs2.getString("dokteranestesi")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' border='0' colspan='2'>Tindakan/Operasi : "+rs2.getString("tindakan")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "SERAH TERIMA PERAWAT KAMAR OPERASI DENGAN ANESTESI / INTENSIF / RUANGAN. PERAWAT MELAKUKAN SERAH TERIMA SECARA VERBAL"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Keadaan Umum : "+rs2.getString("keadaan_umum")+"</td>"+
                                              "<td width='33%' border='0'>Jenis Cairan Infus : "+rs2.getString("jenis_cairan_infus")+"</td>"+
                                              "<td width='33%' border='0'>Jaringan/Organ Tubuh PA/VC : "+rs2.getString("jaringan_pa")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Kateter Urine : "+rs2.getString("kateter_urine")+"</td>"+
                                              "<td width='33%' border='0'>Jika Ada, Tgl.Pemasangan : "+rs2.getString("tanggal_pemasangan_kateter")+"</td>"+
                                              "<td width='33%' border='0'>Warna : "+rs2.getString("warna_kateter")+", Jumlah : "+rs2.getString("jumlah_kateter")+" cc</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Drain : "+rs2.getString("drain")+"</td>"+
                                              "<td width='33%' border='0'>Jika Ada, Jumlah : "+rs2.getString("jumlah_drain")+" buah</td>"+
                                              "<td width='33%' border='0'>Letak : "+rs2.getString("letak_drain")+", Warna/Produksi : "+rs2.getString("warna_drain")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' colspan='3'>"+
                                                  "Hasil Pemeriksaan Penunjang :"+
                                                  "<table width='98%' border='0' align='right' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                                     "<tr>"+
                                                        "<td width='50%' border='0'>Radiologi : "+rs2.getString("pemeriksaan_penunjang_rontgen")+(rs2.getString("keterangan_pemeriksaan_penunjang_rontgen").equals("")?"":", "+rs2.getString("keterangan_pemeriksaan_penunjang_rontgen"))+"</td>"+
                                                        "<td width='50%' border='0'>EKG : "+rs2.getString("pemeriksaan_penunjang_ekg")+(rs2.getString("keterangan_pemeriksaan_penunjang_ekg").equals("")?"":", "+rs2.getString("keterangan_pemeriksaan_penunjang_ekg"))+"</td>"+
                                                     "</tr>"+
                                                     "<tr>"+
                                                        "<td width='50%' border='0'>USG : "+rs2.getString("pemeriksaan_penunjang_usg")+(rs2.getString("keterangan_pemeriksaan_penunjang_usg").equals("")?"":", "+rs2.getString("keterangan_pemeriksaan_penunjang_usg"))+"</td>"+
                                                        "<td width='50%' border='0'>CT Scan : "+rs2.getString("pemeriksaan_penunjang_ctscan")+(rs2.getString("keterangan_pemeriksaan_penunjang_ctscan").equals("")?"":", "+rs2.getString("keterangan_pemeriksaan_penunjang_ctscan"))+"</td>"+
                                                     "</tr>"+
                                                     "<tr>"+
                                                        "<td width='100%' border='0' colspan='2'>MRI : "+rs2.getString("pemeriksaan_penunjang_mri")+(rs2.getString("keterangan_pemeriksaan_penunjang_mri").equals("")?"":", "+rs2.getString("keterangan_pemeriksaan_penunjang_mri"))+"</td>"+
                                                     "</tr>"+
                                                  "</table>"+
                                              "</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%' border='0' colspan='3'>Area Luka Operasi : "+rs2.getString("area_luka_operasi")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"); 
                        }
                        htmlContent.append(
                              "</table>"+
                            "</td>"+
                          "</tr>");
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                } finally{
                    if(rs2!=null){
                        rs2.close();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Checklist Pre Operasi : "+e);
        }
    }
    
    private void menampilkanAsuhanPreOperasi(String norawat) {
        try {
            if(chkAsuhanPreOperasi.isSelected()==true){
                try {
                    rs2=koneksi.prepareStatement(
                            "select penilaian_pre_operasi.tanggal,penilaian_pre_operasi.kd_dokter,penilaian_pre_operasi.ringkasan_klinik,penilaian_pre_operasi.pemeriksaan_fisik,"+
                            "penilaian_pre_operasi.pemeriksaan_diagnostik,penilaian_pre_operasi.diagnosa_pre_operasi,penilaian_pre_operasi.rencana_tindakan_bedah,"+
                            "penilaian_pre_operasi.hal_hal_yang_perludi_persiapkan,penilaian_pre_operasi.terapi_pre_operasi,dokter.nm_dokter "+
                            "from penilaian_pre_operasi inner join dokter on penilaian_pre_operasi.kd_dokter=dokter.kd_dokter "+
                            "where penilaian_pre_operasi.no_rawat='"+norawat+"' order by penilaian_pre_operasi.tanggal").executeQuery();
                    if(rs2.next()){
                        htmlContent.append(
                          "<tr class='isi'>"+ 
                            "<td valign='top' width='2%'></td>"+        
                            "<td valign='top' width='18%'>Penilaian Pre Operasi</td>"+
                            "<td valign='top' width='1%' align='center'>:</td>"+
                            "<td valign='top' width='79%'>"+
                              "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                        );
                        rs2.beforeFirst();
                        while(rs2.next()){
                            htmlContent.append(
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "YANG MELAKUKAN PENGKAJIAN"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='33%' border='0'>Tanggal : "+rs2.getString("tanggal")+"</td>"+
                                              "<td width='67%' border='0'>Dokter : "+rs2.getString("kd_dokter")+" "+rs2.getString("nm_dokter")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "I. RINGKASAN KLINIK :"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+rs2.getString("ringkasan_klinik").replaceAll("(\r\n|\r|\n|\n\r)","<br>")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "II. PEMERIKSAAN FISIK"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+rs2.getString("pemeriksaan_fisik").replaceAll("(\r\n|\r|\n|\n\r)","<br>")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "III. PEMERIKSAAN DIAGNOSTIK"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+rs2.getString("pemeriksaan_diagnostik").replaceAll("(\r\n|\r|\n|\n\r)","<br>")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "IV. DIAGNOSA PRE OPERASI"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+rs2.getString("diagnosa_pre_operasi").replaceAll("(\r\n|\r|\n|\n\r)","<br>")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "V. RENCANA TINDAKAN BEDAH"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+rs2.getString("rencana_tindakan_bedah").replaceAll("(\r\n|\r|\n|\n\r)","<br>")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "VI. HAL-HAL YANG PERLU DIPERSIAPKAN"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='100%' border='0'>"+rs2.getString("hal_hal_yang_perludi_persiapkan").replaceAll("(\r\n|\r|\n|\n\r)","<br>")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "VII. TERAPI PRE OPERASI"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                               "<td width='100%' border='0'>"+rs2.getString("terapi_pre_operasi").replaceAll("(\r\n|\r|\n|\n\r)","<br>")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"); 
                        }
                        htmlContent.append(
                              "</table>"+
                            "</td>"+
                          "</tr>");
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                } finally{
                    if(rs2!=null){
                        rs2.close();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Asuhan Medis Rawat Jalan : "+e);
        }
    }
    
    private void menampilkanAsuhanPreAnestesi(String norawat) {
        try {
            if(chkAsuhanPreAnestesi.isSelected()==true){
                try {
                    rs2=koneksi.prepareStatement(
                            "select penilaian_pre_anestesi.tanggal,penilaian_pre_anestesi.kd_dokter,penilaian_pre_anestesi.tanggal_operasi,penilaian_pre_anestesi.diagnosa,penilaian_pre_anestesi.rencana_tindakan,penilaian_pre_anestesi.tb,"+
                            "penilaian_pre_anestesi.bb,penilaian_pre_anestesi.td,penilaian_pre_anestesi.io2,penilaian_pre_anestesi.nadi,penilaian_pre_anestesi.pernapasan,penilaian_pre_anestesi.suhu,"+
                            "penilaian_pre_anestesi.fisik_cardiovasculer,penilaian_pre_anestesi.fisik_paru,penilaian_pre_anestesi.fisik_abdomen,penilaian_pre_anestesi.fisik_extrimitas,"+
                            "penilaian_pre_anestesi.fisik_endokrin,penilaian_pre_anestesi.fisik_ginjal,penilaian_pre_anestesi.fisik_obatobatan,penilaian_pre_anestesi.fisik_laborat,"+
                            "penilaian_pre_anestesi.fisik_penunjang,penilaian_pre_anestesi.riwayat_penyakit_alergiobat,penilaian_pre_anestesi.riwayat_penyakit_alergilainnya,"+
                            "penilaian_pre_anestesi.riwayat_penyakit_terapi,penilaian_pre_anestesi.riwayat_kebiasaan_merokok,penilaian_pre_anestesi.riwayat_kebiasaan_ket_merokok,"+
                            "penilaian_pre_anestesi.riwayat_kebiasaan_alkohol,penilaian_pre_anestesi.riwayat_kebiasaan_ket_alkohol,penilaian_pre_anestesi.riwayat_kebiasaan_obat,"+
                            "penilaian_pre_anestesi.riwayat_kebiasaan_ket_obat,penilaian_pre_anestesi.riwayat_medis_cardiovasculer,penilaian_pre_anestesi.riwayat_medis_respiratory,"+
                            "penilaian_pre_anestesi.riwayat_medis_endocrine,penilaian_pre_anestesi.riwayat_medis_lainnya,penilaian_pre_anestesi.asa,penilaian_pre_anestesi.puasa,"+
                            "penilaian_pre_anestesi.rencana_anestesi,penilaian_pre_anestesi.rencana_perawatan,penilaian_pre_anestesi.catatan_khusus,dokter.nm_dokter "+
                            "from penilaian_pre_anestesi inner join dokter on penilaian_pre_anestesi.kd_dokter=dokter.kd_dokter "+
                            "where penilaian_pre_anestesi.no_rawat='"+norawat+"' order by penilaian_pre_anestesi.tanggal").executeQuery();
                    if(rs2.next()){
                        htmlContent.append(
                          "<tr class='isi'>"+ 
                            "<td valign='top' width='2%'></td>"+        
                            "<td valign='top' width='18%'>Penilaian Pre Anestesi</td>"+
                            "<td valign='top' width='1%' align='center'>:</td>"+
                            "<td valign='top' width='79%'>"+
                              "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                        );
                        rs2.beforeFirst();
                        while(rs2.next()){
                            htmlContent.append(
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "YANG MELAKUKAN PENGKAJIAN"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='30%' border='0'>Tanggal : "+rs2.getString("tanggal")+"</td>"+
                                              "<td width='40%' border='0'>Dokter : "+rs2.getString("kd_dokter")+" "+rs2.getString("nm_dokter")+"</td>"+
                                              "<td width='30%' border='0'>Tgl.Operasi : "+rs2.getString("tanggal_operasi")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "I. ASESMEN FISIK"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                              "<td width='100%'>TB : "+rs2.getString("tb")+" Cm&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;BB : "+rs2.getString("bb")+" Kg&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TD : "+rs2.getString("td")+" mmHg&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;IO2 : "+rs2.getString("io2")+" %&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Nadi : "+rs2.getString("nadi")+" x/menit&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Suhu : "+rs2.getString("suhu")+" °C&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Pernapasan : "+rs2.getString("pernapasan")+" x/menit</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%'>Cardiovasculer : "+rs2.getString("fisik_cardiovasculer")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%'>Paru : "+rs2.getString("fisik_paru")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%'>Abdomen : "+rs2.getString("fisik_abdomen")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%'>Extrimitas : "+rs2.getString("fisik_extrimitas")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%'>Endokrin : "+rs2.getString("fisik_endokrin")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%'>Ginjal : "+rs2.getString("fisik_ginjal")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%'>Obat-obatan : "+rs2.getString("fisik_obatobatan")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%'>Laboratorium : "+rs2.getString("fisik_laborat")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                              "<td width='100%'>Penunjang : "+rs2.getString("fisik_penunjang")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "II. RIWAYAT PENYAKIT"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                               "<td width='100%'>Alergi Obat : "+rs2.getString("riwayat_penyakit_alergiobat")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                               "<td width='100%'>Alergi Lainnya : "+rs2.getString("riwayat_penyakit_alergilainnya")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                               "<td width='100%'>Terapi : "+rs2.getString("riwayat_penyakit_terapi")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                               "<td width='100%'>Merokok : "+rs2.getString("riwayat_kebiasaan_merokok")+(rs2.getString("riwayat_kebiasaan_ket_merokok").equals("")?"":", "+rs2.getString("riwayat_kebiasaan_ket_merokok")+" batang/hari")+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Alkohol : "+rs2.getString("riwayat_kebiasaan_alkohol")+(rs2.getString("riwayat_kebiasaan_ket_alkohol").equals("")?"":", "+rs2.getString("riwayat_kebiasaan_ket_alkohol")+" gelas/hari")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                               "<td width='100%'>Penggunaan Obat : "+rs2.getString("riwayat_kebiasaan_obat")+(rs2.getString("riwayat_kebiasaan_ket_obat").equals("")?"":", "+rs2.getString("riwayat_kebiasaan_ket_obat"))+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "III. RIWAYAT MEDIS"+  
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                               "<td width='100%'>Cardiovasculer : "+rs2.getString("riwayat_medis_cardiovasculer")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                               "<td width='100%'>Respiratory : "+rs2.getString("riwayat_medis_respiratory")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                               "<td width='100%'>Endocrine : "+rs2.getString("riwayat_medis_endocrine")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                               "<td width='100%'>Lainnya : "+rs2.getString("riwayat_medis_lainnya")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"+
                                 "<tr>"+
                                    "<td valign='top'>"+
                                       "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0px' class='tbl_form'>"+
                                          "<tr>"+
                                               "<td width='100%' width='33%'>Rencana Anestesi : "+rs2.getString("rencana_anestesi")+"</td>"+
                                               "<td width='100%' width='33%'>Angka ASA : "+rs2.getString("asa")+"</td>"+
                                               "<td width='100%' width='33%'>Puasa : "+rs2.getString("puasa")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                               "<td width='100%' colspan='3'>Rencana Perawatan : "+rs2.getString("rencana_perawatan")+"</td>"+
                                          "</tr>"+
                                          "<tr>"+
                                               "<td width='100%' colspan='3'>Catatan Khusus : "+rs2.getString("catatan_khusus")+"</td>"+
                                          "</tr>"+
                                       "</table>"+
                                    "</td>"+
                                 "</tr>"); 
                        }
                        htmlContent.append(
                              "</table>"+
                            "</td>"+
                          "</tr>");
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                } finally{
                    if(rs2!=null){
                        rs2.close();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Asuhan Medis Rawat Jalan : "+e);
        }
    }
    
    
    
}
