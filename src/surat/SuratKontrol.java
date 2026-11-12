package surat;

import fungsi.WarnaTableKasirRalan;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.validasi1;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariDokter2;
import laporan.DlgCariPenyakit;
import simrskhanza.DlgCariPoli;
import simrskhanza.DlgCariPoli2;

/**
 *
 * @author dosen
 */
public class SuratKontrol extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private validasi1 Valid1=new validasi1();
    private PreparedStatement ps,ps2;
    private ResultSet rs,rs2;
    private int i=0,kuota=0;
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    private DlgCariDokter2 dokter2=new DlgCariDokter2(null,false);
    private DlgCariPoli poli=new DlgCariPoli(null,false);
    private DlgCariPoli2 poli2=new DlgCariPoli2(null,false);
    private String URUTNOREG="",status="",kdpoli="",nmpoli="",noantri="",aktifjadwal="",bln_angka="",bln_romawi="";
    private DlgCariPenyakit penyakit=new DlgCariPenyakit(null,false);
    private String finger="",JADIKANBOOKINGSURATKONTROL="no";
    

    /** Creates new form DlgPemberianInfus
     * @param parent
     * @param modal */
    public SuratKontrol(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        tabMode=new DefaultTableModel(null,new Object[]{
                "Tahun","No.RM","Nama Pasien","Diagnosa","Puasa","Tujuan Kontrol","Asal Booking",
                "No Hp / Keterangan","Rencana Tindak Lanjut","Periksa Kembali","Tanggal Surat","No.Surat","No.Reg","Kode Dokter","Nama Dokter",
                "Kode Poli","Nama Poli","Status","Jenis Surat","Terapi","Pengobatan dengan Obat-obatan","Lain-lain","Masa Berlaku Rujukan","Kesimpulan"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 24; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==1){
                column.setPreferredWidth(80);
            }else if(i==2){
                column.setPreferredWidth(150);
            }else if(i==3){
                column.setPreferredWidth(150);
            }else if(i==4){
                column.setPreferredWidth(150);
            }else if(i==5){
                column.setPreferredWidth(220);
            }else if(i==6){
                column.setPreferredWidth(120);
            }else if(i==7){
                column.setPreferredWidth(220);
            }else if(i==8){
                column.setPreferredWidth(220);
            }else if(i==9){
                column.setPreferredWidth(150);
            }else if(i==10){
                column.setPreferredWidth(150);
            }else if(i==11){
                column.setPreferredWidth(75);
            }else if(i==12){
                column.setPreferredWidth(60);
            }else if(i==13){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==14){
                column.setPreferredWidth(150);
            }else if(i==15){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==16){
                column.setPreferredWidth(150);
            }else if(i==17){
                column.setPreferredWidth(150);
            }else if(i==18){
                column.setPreferredWidth(150);
            }else if(i==19){
                column.setPreferredWidth(150);
            }else if(i==20){
                column.setPreferredWidth(150);
            }else if(i==21){
                column.setPreferredWidth(150);
            }else if(i==22){
                column.setPreferredWidth(150);
            }else if(i==23){
                column.setPreferredWidth(150);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTableKasirRalan());


        TNoRM.setDocument(new batasInput((byte)15).getKata(TNoRM));
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        Diagnosa.setDocument(new batasInput((int)100).getKata(Diagnosa));
        //Terapi=Puasa, Alasan1=Tujuan, Alasan2=StatusKontrol
        Tujuan.setDocument(new batasInput((int)200).getKata(Tujuan));
        ket.setDocument(new batasInput((int)200).getKata(ket));
        Rtl.setDocument(new batasInput((int)200).getKata(Rtl));
        NoReg.setDocument(new batasInput((byte)20).getKata(NoReg));
        KdDokter.setDocument(new batasInput((byte)20).getKata(KdDokter));
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil();
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil();
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil();
                    }
                }
            });
        } 
        
        ChkInput.setSelected(false);
        isForm();
        
        penyakit.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if( penyakit.getTable().getSelectedRow()!= -1){ 
                    if((penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),0).toString()+" - "+penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),1).toString()).length()<50){
                        Diagnosa.setText(penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),0).toString()+" - "+penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),1).toString());
                    }else{
                        Diagnosa.setText((penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),0).toString()+" - "+penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),1).toString()).substring(0,50));
                    }   
                }  
                Diagnosa.requestFocus();
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
        
        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {;}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter.getTable().getSelectedRow()!= -1){                    
                    KdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                    NmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                    isNomer();
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
        
        dokter2.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {;}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter2.getTable().getSelectedRow()!= -1){                    
                    KdDokter.setText(dokter2.getTable().getValueAt(dokter2.getTable().getSelectedRow(),0).toString());
                    NmDokter.setText(dokter2.getTable().getValueAt(dokter2.getTable().getSelectedRow(),1).toString());
                    if(aktifjadwal.equals("aktif")){
                        kuota=Integer.parseInt(dokter2.getTable().getValueAt(dokter2.getTable().getSelectedRow(),13).toString());
                    }
                    isNomer();                        
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
        
        poli.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(poli.getTable().getSelectedRow()!= -1){                    
                    KdPoli.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(),0).toString());
                    NmPoli.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(),1).toString());
                    isNomer();
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
        
        poli2.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(poli2.getTable().getSelectedRow()!= -1){                    
                    KdPoli.setText(poli2.getTable().getValueAt(poli2.getTable().getSelectedRow(),0).toString());
                    NmPoli.setText(poli2.getTable().getValueAt(poli2.getTable().getSelectedRow(),1).toString());
                    isNomer();
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
        
        try {
            aktifjadwal=koneksiDB.JADWALDOKTERDIREGISTRASI();
            URUTNOREG=koneksiDB.URUTNOREG();
        } catch (Exception ex) {
            aktifjadwal="";
            URUTNOREG="";
        }
        
        try {
            JADIKANBOOKINGSURATKONTROL=koneksiDB.JADIKANBOOKINGSURATKONTROL();
        } catch (Exception ex) {
            JADIKANBOOKINGSURATKONTROL="no";
        }
        
        TanggalPeriksa.setDate(new Date());
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
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnSurat = new widget.MenuItem();
        MnSurat1 = new widget.MenuItem();
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
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass10 = new widget.panelisi();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        panelCari = new widget.panelisi();
        R1 = new widget.RadioButton();
        R2 = new widget.RadioButton();
        DTPCari1 = new widget.Tanggal();
        jLabel22 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        R3 = new widget.RadioButton();
        DTPCari3 = new widget.Tanggal();
        jLabel25 = new widget.Label();
        DTPCari4 = new widget.Tanggal();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        TNoRM = new widget.TextBox();
        TPasien = new widget.TextBox();
        TanggalSurat = new widget.Tanggal();
        jLabel10 = new widget.Label();
        Diagnosa = new widget.TextBox();
        jLabel16 = new widget.Label();
        btnDiagnosa = new widget.Button();
        jLabel8 = new widget.Label();
        Sumber = new widget.ComboBox();
        jLabel15 = new widget.Label();
        NoSurat = new widget.TextBox();
        jLabel5 = new widget.Label();
        Tujuan = new widget.TextBox();
        jLabel13 = new widget.Label();
        Rtl = new widget.TextBox();
        jLabel12 = new widget.Label();
        ket = new widget.TextBox();
        jLabel9 = new widget.Label();
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        jLabel17 = new widget.Label();
        Puasa = new widget.ComboBox();
        jLabel11 = new widget.Label();
        KdPoli = new widget.TextBox();
        NmPoli = new widget.TextBox();
        BtnPoli = new widget.Button();
        jLabel18 = new widget.Label();
        NoReg = new widget.TextBox();
        jLabel37 = new widget.Label();
        Status = new widget.ComboBox();
        jLabel14 = new widget.Label();
        TanggalPeriksa = new widget.Tanggal();
        jLabel30 = new widget.Label();
        jLabel27 = new widget.Label();
        Jenis = new widget.ComboBox();
        jLabel34 = new widget.Label();
        Kesimpulan = new widget.ComboBox();
        jLabel28 = new widget.Label();
        Terapi = new widget.TextBox();
        jLabel29 = new widget.Label();
        jLabel31 = new widget.Label();
        Obatan = new widget.TextBox();
        jLabel32 = new widget.Label();
        Lain = new widget.TextBox();
        jLabel33 = new widget.Label();
        Masa = new widget.TextBox();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnSurat.setBackground(new java.awt.Color(255, 255, 254));
        MnSurat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnSurat.setForeground(new java.awt.Color(50, 50, 50));
        MnSurat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnSurat.setText("Surat Kontrol");
        MnSurat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnSurat.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnSurat.setName("MnSurat"); // NOI18N
        MnSurat.setPreferredSize(new java.awt.Dimension(180, 26));
        MnSurat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnSuratActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnSurat);

        MnSurat1.setBackground(new java.awt.Color(255, 255, 254));
        MnSurat1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnSurat1.setForeground(new java.awt.Color(50, 50, 50));
        MnSurat1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnSurat1.setText("Cetak Surat Rujuk Balik");
        MnSurat1.setToolTipText("");
        MnSurat1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnSurat1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnSurat1.setName("MnSurat1"); // NOI18N
        MnSurat1.setPreferredSize(new java.awt.Dimension(180, 26));
        MnSurat1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnSurat1ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnSurat1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Surat Kontrol ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbObat.setAutoCreateRowSorter(false);
        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbObatKeyReleased(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 144));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(55, 55));
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

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(100, 30));
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
        panelGlass8.add(BtnAll);

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

        jPanel3.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        panelGlass10.setName("panelGlass10"); // NOI18N
        panelGlass10.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass10.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(450, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass10.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
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
        panelGlass10.add(BtnCari);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(65, 23));
        panelGlass10.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass10.add(LCount);

        jPanel3.add(panelGlass10, java.awt.BorderLayout.CENTER);

        panelCari.setName("panelCari"); // NOI18N
        panelCari.setPreferredSize(new java.awt.Dimension(44, 43));
        panelCari.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 9));

        R1.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.pink));
        buttonGroup1.add(R1);
        R1.setSelected(true);
        R1.setText("Menunggu");
        R1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        R1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R1.setName("R1"); // NOI18N
        R1.setPreferredSize(new java.awt.Dimension(85, 23));
        panelCari.add(R1);

        R2.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.pink));
        buttonGroup1.add(R2);
        R2.setText("Tanggal :");
        R2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        R2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R2.setName("R2"); // NOI18N
        R2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelCari.add(R2);

        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "29-04-2026" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(100, 23));
        DTPCari1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                DTPCari1ItemStateChanged(evt);
            }
        });
        DTPCari1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPCari1KeyPressed(evt);
            }
        });
        panelCari.add(DTPCari1);

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("s.d");
        jLabel22.setName("jLabel22"); // NOI18N
        jLabel22.setPreferredSize(new java.awt.Dimension(25, 23));
        panelCari.add(jLabel22);

        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "29-04-2026" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(100, 23));
        DTPCari2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPCari2KeyPressed(evt);
            }
        });
        panelCari.add(DTPCari2);

        R3.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.pink));
        buttonGroup1.add(R3);
        R3.setText("Selesai :");
        R3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        R3.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        R3.setName("R3"); // NOI18N
        R3.setPreferredSize(new java.awt.Dimension(85, 23));
        panelCari.add(R3);

        DTPCari3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "29-04-2026" }));
        DTPCari3.setDisplayFormat("dd-MM-yyyy");
        DTPCari3.setName("DTPCari3"); // NOI18N
        DTPCari3.setOpaque(false);
        DTPCari3.setPreferredSize(new java.awt.Dimension(100, 23));
        DTPCari3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                DTPCari3ItemStateChanged(evt);
            }
        });
        DTPCari3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPCari3KeyPressed(evt);
            }
        });
        panelCari.add(DTPCari3);

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setText("s.d");
        jLabel25.setName("jLabel25"); // NOI18N
        jLabel25.setPreferredSize(new java.awt.Dimension(25, 23));
        panelCari.add(jLabel25);

        DTPCari4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "29-04-2026" }));
        DTPCari4.setDisplayFormat("dd-MM-yyyy");
        DTPCari4.setName("DTPCari4"); // NOI18N
        DTPCari4.setOpaque(false);
        DTPCari4.setPreferredSize(new java.awt.Dimension(100, 23));
        DTPCari4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                DTPCari4ItemStateChanged(evt);
            }
        });
        DTPCari4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPCari4KeyPressed(evt);
            }
        });
        panelCari.add(DTPCari4);

        jPanel3.add(panelCari, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 216));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('M');
        ChkInput.setText(".: Input Data");
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
        FormInput.setPreferredSize(new java.awt.Dimension(190, 107));
        FormInput.setLayout(null);

        jLabel4.setText("Pasien :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 92, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRMKeyPressed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(96, 10, 87, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(185, 10, 190, 23);

        TanggalSurat.setForeground(new java.awt.Color(50, 70, 50));
        TanggalSurat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "29-04-2026 10:45:25" }));
        TanggalSurat.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TanggalSurat.setName("TanggalSurat"); // NOI18N
        TanggalSurat.setOpaque(false);
        TanggalSurat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalSuratKeyPressed(evt);
            }
        });
        FormInput.add(TanggalSurat);
        TanggalSurat.setBounds(474, 10, 132, 23);

        jLabel10.setText("Tanggal Surat :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(375, 10, 95, 23);

        Diagnosa.setHighlighter(null);
        Diagnosa.setName("Diagnosa"); // NOI18N
        Diagnosa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiagnosaKeyPressed(evt);
            }
        });
        FormInput.add(Diagnosa);
        Diagnosa.setBounds(96, 40, 249, 23);

        jLabel16.setText("Diagnosa :");
        jLabel16.setName("jLabel16"); // NOI18N
        FormInput.add(jLabel16);
        jLabel16.setBounds(0, 40, 92, 23);

        btnDiagnosa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnDiagnosa.setMnemonic('3');
        btnDiagnosa.setToolTipText("Alt+3");
        btnDiagnosa.setName("btnDiagnosa"); // NOI18N
        btnDiagnosa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDiagnosaActionPerformed(evt);
            }
        });
        btnDiagnosa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnDiagnosaKeyPressed(evt);
            }
        });
        FormInput.add(btnDiagnosa);
        btnDiagnosa.setBounds(347, 40, 28, 23);

        jLabel8.setText("Asal Booking :");
        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(380, 40, 90, 23);

        Sumber.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Rawat Jalan", "Post Rawat Inap" }));
        Sumber.setName("Sumber"); // NOI18N
        Sumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SumberKeyPressed(evt);
            }
        });
        FormInput.add(Sumber);
        Sumber.setBounds(475, 40, 132, 23);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel15.setText("No.Surat :");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(630, 40, 60, 23);

        NoSurat.setHighlighter(null);
        NoSurat.setName("NoSurat"); // NOI18N
        NoSurat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoSuratKeyPressed(evt);
            }
        });
        FormInput.add(NoSurat);
        NoSurat.setBounds(690, 40, 140, 23);

        jLabel5.setText("Tujuan Kontrol :");
        jLabel5.setName("jLabel5"); // NOI18N
        FormInput.add(jLabel5);
        jLabel5.setBounds(0, 70, 92, 23);

        Tujuan.setHighlighter(null);
        Tujuan.setName("Tujuan"); // NOI18N
        Tujuan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TujuanKeyPressed(evt);
            }
        });
        FormInput.add(Tujuan);
        Tujuan.setBounds(96, 70, 530, 23);

        jLabel13.setText("Tindak Lanjut :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(0, 100, 92, 23);

        Rtl.setHighlighter(null);
        Rtl.setName("Rtl"); // NOI18N
        Rtl.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RtlKeyPressed(evt);
            }
        });
        FormInput.add(Rtl);
        Rtl.setBounds(96, 100, 530, 23);

        jLabel12.setText("No HP :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(0, 130, 92, 23);

        ket.setHighlighter(null);
        ket.setName("ket"); // NOI18N
        ket.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ketKeyPressed(evt);
            }
        });
        FormInput.add(ket);
        ket.setBounds(96, 130, 170, 23);

        jLabel9.setText("Dokter :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(0, 160, 92, 23);

        KdDokter.setEditable(false);
        KdDokter.setHighlighter(null);
        KdDokter.setName("KdDokter"); // NOI18N
        FormInput.add(KdDokter);
        KdDokter.setBounds(96, 160, 87, 23);

        NmDokter.setEditable(false);
        NmDokter.setHighlighter(null);
        NmDokter.setName("NmDokter"); // NOI18N
        FormInput.add(NmDokter);
        NmDokter.setBounds(185, 160, 160, 23);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter.setMnemonic('X');
        BtnDokter.setToolTipText("Alt+X");
        BtnDokter.setName("BtnDokter"); // NOI18N
        BtnDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDokterActionPerformed(evt);
            }
        });
        BtnDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnDokterKeyPressed(evt);
            }
        });
        FormInput.add(BtnDokter);
        BtnDokter.setBounds(347, 160, 28, 23);

        jLabel17.setText("Puasa :");
        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(375, 130, 95, 23);

        Puasa.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Perlu Puasa", "Perlu Puasa Tengah Malam" }));
        Puasa.setName("Puasa"); // NOI18N
        Puasa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PuasaKeyPressed(evt);
            }
        });
        FormInput.add(Puasa);
        Puasa.setBounds(475, 130, 150, 23);

        jLabel11.setText("Unit/Poli :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(375, 160, 95, 23);

        KdPoli.setEditable(false);
        KdPoli.setHighlighter(null);
        KdPoli.setName("KdPoli"); // NOI18N
        FormInput.add(KdPoli);
        KdPoli.setBounds(474, 160, 70, 23);

        NmPoli.setEditable(false);
        NmPoli.setHighlighter(null);
        NmPoli.setName("NmPoli"); // NOI18N
        FormInput.add(NmPoli);
        NmPoli.setBounds(546, 160, 165, 23);

        BtnPoli.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPoli.setMnemonic('X');
        BtnPoli.setToolTipText("Alt+X");
        BtnPoli.setName("BtnPoli"); // NOI18N
        BtnPoli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPoliActionPerformed(evt);
            }
        });
        BtnPoli.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPoliKeyPressed(evt);
            }
        });
        FormInput.add(BtnPoli);
        BtnPoli.setBounds(712, 160, 28, 23);

        jLabel18.setText("No.Reg :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(375, 190, 95, 23);

        NoReg.setHighlighter(null);
        NoReg.setName("NoReg"); // NOI18N
        NoReg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoRegKeyPressed(evt);
            }
        });
        FormInput.add(NoReg);
        NoReg.setBounds(474, 190, 70, 23);

        jLabel37.setText("Status Booking :");
        jLabel37.setName("jLabel37"); // NOI18N
        FormInput.add(jLabel37);
        jLabel37.setBounds(550, 190, 80, 23);

        Status.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Menunggu", "Sudah Periksa", "Batal Periksa" }));
        Status.setName("Status"); // NOI18N
        Status.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                StatusKeyPressed(evt);
            }
        });
        FormInput.add(Status);
        Status.setBounds(635, 190, 130, 23);

        jLabel14.setText("Tanggal Kontrol Ulang :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(0, 190, 140, 23);

        TanggalPeriksa.setForeground(new java.awt.Color(50, 70, 50));
        TanggalPeriksa.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "29-04-2026 10:45:25" }));
        TanggalPeriksa.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TanggalPeriksa.setName("TanggalPeriksa"); // NOI18N
        TanggalPeriksa.setOpaque(false);
        TanggalPeriksa.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                TanggalPeriksaItemStateChanged(evt);
            }
        });
        TanggalPeriksa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalPeriksaKeyPressed(evt);
            }
        });
        FormInput.add(TanggalPeriksa);
        TanggalPeriksa.setBounds(145, 190, 180, 23);

        jLabel30.setForeground(new java.awt.Color(0, 0, 0));
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel30.setText("Tambahan form untuk SURAT RUJUK BALIK, jika ingin membuat surat kontrol tidak perlu isi form di bawah ini.");
        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel30.setName("jLabel30"); // NOI18N
        FormInput.add(jLabel30);
        jLabel30.setBounds(30, 230, 870, 23);

        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel27.setText("Jenis Surat :");
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(30, 260, 90, 23);

        Jenis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Surat Kontrol", "Surat Rujuk Balik" }));
        Jenis.setName("Jenis"); // NOI18N
        Jenis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JenisKeyPressed(evt);
            }
        });
        FormInput.add(Jenis);
        Jenis.setBounds(100, 260, 120, 23);

        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel34.setText("Kesimpulan :");
        jLabel34.setName("jLabel34"); // NOI18N
        FormInput.add(jLabel34);
        jLabel34.setBounds(240, 260, 90, 23);

        Kesimpulan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "Perlu Rawat Inap", "Konsultasi Selesai" }));
        Kesimpulan.setName("Kesimpulan"); // NOI18N
        Kesimpulan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KesimpulanKeyPressed(evt);
            }
        });
        FormInput.add(Kesimpulan);
        Kesimpulan.setBounds(310, 260, 120, 23);

        jLabel28.setText("Terapi :");
        jLabel28.setName("jLabel28"); // NOI18N
        FormInput.add(jLabel28);
        jLabel28.setBounds(430, 260, 70, 23);

        Terapi.setHighlighter(null);
        Terapi.setName("Terapi"); // NOI18N
        Terapi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TerapiKeyPressed(evt);
            }
        });
        FormInput.add(Terapi);
        Terapi.setBounds(510, 260, 530, 23);

        jLabel29.setForeground(new java.awt.Color(0, 0, 0));
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel29.setText("Tindak lanjut yang dianjurkan,");
        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel29.setName("jLabel29"); // NOI18N
        FormInput.add(jLabel29);
        jLabel29.setBounds(30, 290, 180, 23);

        jLabel31.setText("Pengobatan dengan Obat-obatan :");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(180, 290, 200, 23);

        Obatan.setHighlighter(null);
        Obatan.setName("Obatan"); // NOI18N
        Obatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ObatanKeyPressed(evt);
            }
        });
        FormInput.add(Obatan);
        Obatan.setBounds(390, 290, 550, 23);

        jLabel32.setText("Lain - lain :");
        jLabel32.setName("jLabel32"); // NOI18N
        FormInput.add(jLabel32);
        jLabel32.setBounds(180, 320, 200, 23);

        Lain.setHighlighter(null);
        Lain.setName("Lain"); // NOI18N
        Lain.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LainKeyPressed(evt);
            }
        });
        FormInput.add(Lain);
        Lain.setBounds(390, 320, 550, 23);

        jLabel33.setText("Masa berlaku rujukan :");
        jLabel33.setName("jLabel33"); // NOI18N
        FormInput.add(jLabel33);
        jLabel33.setBounds(180, 350, 200, 23);

        Masa.setHighlighter(null);
        Masa.setName("Masa"); // NOI18N
        Masa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MasaKeyPressed(evt);
            }
        });
        FormInput.add(Masa);
        Masa.setBounds(390, 350, 550, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRMKeyPressed
        Valid.pindah(evt,Status,KdDokter);
        
}//GEN-LAST:event_TNoRMKeyPressed

    private void TanggalSuratKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalSuratKeyPressed
        Valid.pindah(evt,TCari,Status);
}//GEN-LAST:event_TanggalSuratKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRM.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRM,"pasien");
        }else if(NmDokter.getText().trim().equals("")||KdDokter.getText().trim().equals("")){
            Valid.textKosong(KdDokter,"Dokter");
        }else if(NmPoli.getText().trim().equals("")||NmPoli.getText().trim().equals("")){
            Valid.textKosong(KdPoli,"Poli");
        }else if(NoSurat.getText().trim().equals("")){
            Valid.textKosong(NoSurat,"No.Surat");
        }else if(NoReg.getText().trim().equals("")){
            Valid.textKosong(NoReg,"No.Antri");
        }else if(Tujuan.getText().trim().equals("")){
            Valid.textKosong(Tujuan,"Tujuan Kontrol");
        }else if(Diagnosa.getText().trim().equals("")){
            Valid.textKosong(Diagnosa,"Diagnosa");
        }else{
             if(akses.getkode().equals("Admin Utama")){
                isBooking();
            }else{
                if(aktifjadwal.equals("aktif")){
                    if(Sequel.cariInteger("select count(booking_registrasi.no_rkm_medis) from booking_registrasi where booking_registrasi.kd_dokter='"+KdDokter.getText()+"' and booking_registrasi.tanggal_periksa='"+Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+"' ")>=kuota){
                        JOptionPane.showMessageDialog(null,"Yaahh, Kuota registrasi penuh guys..!!!");
                        TCari.requestFocus();
                    }else{
                        isBooking();
                    }                    
                }else{
                    isBooking();
                } 
            }            
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,NoReg,BtnBatal);
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
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TanggalSurat.requestFocus();
        }else if(TPasien.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal menghapus. Pilih dulu data yang mau dihapus.\nKlik data pada table untuk memilih...!!!!");
        }else if(!(TPasien.getText().trim().equals(""))){
            if(tbObat.getSelectedRow()!= -1){
                if(Sequel.queryu2tf("delete from skdp_bpjs where tahun=? and no_antrian=?",2,new String[]{
                    tbObat.getValueAt(tbObat.getSelectedRow(),0).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),11).toString()
                })==true){
                    Sequel.queryu2("delete from booking_registrasi where no_rkm_medis=? and tanggal_periksa=?",2,new String[]{
                        tbObat.getValueAt(tbObat.getSelectedRow(),1).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),9).toString()
                    });
                    tabMode.removeRow(tbObat.getSelectedRow());
                    LCount.setText(""+tabMode.getRowCount());
                    emptTeks();
                }                        
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih terlebih dulu data yang mau anda hapus...\n Klik data pada table untuk memilih data...!!!!");
            }  
        }
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnBatal, BtnPrint);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnPrint,TCari);}
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
            
            Sequel.queryu("delete from temporary_booking_registrasi");
            for(i=0;i<tabMode.getRowCount();i++){ 
                Sequel.menyimpan("temporary_booking_registrasi","'0','"+
                    tabMode.getValueAt(i,0).toString()+"','"+
                    tabMode.getValueAt(i,1).toString()+"','"+
                    tabMode.getValueAt(i,2).toString()+"','"+
                    tabMode.getValueAt(i,3).toString()+"','"+
                    tabMode.getValueAt(i,4).toString()+"','"+
                    tabMode.getValueAt(i,5).toString()+"','"+
                    tabMode.getValueAt(i,6).toString()+"','"+
                    tabMode.getValueAt(i,7).toString()+"','"+
                    tabMode.getValueAt(i,8).toString()+"','"+
                    tabMode.getValueAt(i,9).toString()+"','"+
                    tabMode.getValueAt(i,10).toString()+"','"+
                    tabMode.getValueAt(i,11).toString()+"','"+
                    tabMode.getValueAt(i,12).toString()+"','"+
                    tabMode.getValueAt(i,13).toString()+"','"+
                    tabMode.getValueAt(i,14).toString()+"','"+
                    tabMode.getValueAt(i,15).toString()+"','"+
                    tabMode.getValueAt(i,16).toString()+"','"+
                    tabMode.getValueAt(i,17).toString()+"','','','','','','','','','','','','','','','','','','',''","Rekap Nota Pembayaran");
            }
             
            Valid.MyReport("rptSKDPBPJS.jasper","report","::[ Laporan Daftar Surat Kontrol ]::",param);
        }
        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnKeluar);
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
        tampil();
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
        tampil();
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            tampil();
            TCari.setText("");
        }else{
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
  isForm();                
}//GEN-LAST:event_ChkInputActionPerformed

    private void DTPCari1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPCari1KeyPressed

    }//GEN-LAST:event_DTPCari1KeyPressed

    private void DTPCari2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPCari2KeyPressed
        R2.setSelected(true);
    }//GEN-LAST:event_DTPCari2KeyPressed

    private void DTPCari3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPCari3KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_DTPCari3KeyPressed

    private void DTPCari4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPCari4KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_DTPCari4KeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if(TNoRM.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRM,"pasien");
        }else if(NmDokter.getText().trim().equals("")||KdDokter.getText().trim().equals("")){
            Valid.textKosong(KdDokter,"Dokter");
        }else if(NmPoli.getText().trim().equals("")||NmPoli.getText().trim().equals("")){
            Valid.textKosong(KdPoli,"Poli");
        }else if(NoSurat.getText().trim().equals("")){
            Valid.textKosong(NoSurat,"No.Surat");
        }else if(NoReg.getText().trim().equals("")){
            Valid.textKosong(NoReg,"No.Antri");
        }else if(Tujuan.getText().trim().equals("")){
            Valid.textKosong(Tujuan,"Tujuan Kontrol");
        }else if(Diagnosa.getText().trim().equals("")){
            Valid.textKosong(Diagnosa,"Diagnosa");
        }else{
            if(tbObat.getSelectedRow()!= -1){
                if(Sequel.mengedittf("skdp_bpjs","tahun=? and no_antrian=?","tahun=?,no_rkm_medis=?,diagnosa=?,puasa=?,tujuan=?,sumber=?,ket=?,rtl=?,tanggal_datang=?,tanggal_rujukan=?,no_antrian=?,kd_dokter=?,status=?,jenis=?,terapi=?,obatan=?,lain=?,masa=?,kesimpulan=?",21,new String[]{
                        TanggalPeriksa.getSelectedItem().toString().substring(6,10),TNoRM.getText(),Diagnosa.getText(),Puasa.getSelectedItem()+"",
                        Tujuan.getText(),Sumber.getSelectedItem()+"",ket.getText(),Rtl.getText(),Valid.SetTgl(TanggalPeriksa.getSelectedItem()+""),
                        Valid.SetTgl(TanggalSurat.getSelectedItem()+""),NoSurat.getText(),KdDokter.getText(),Status.getSelectedItem().toString(),Jenis.getSelectedItem()+"",Terapi.getText(),Obatan.getText(),Lain.getText(),Masa.getText(),Kesimpulan.getSelectedItem()+"",
                        tbObat.getValueAt(tbObat.getSelectedRow(),0).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),11).toString()
                  })==true){
                    Sequel.mengedit3("booking_registrasi","no_rkm_medis=? and tanggal_periksa=?","tanggal_booking=?,no_rkm_medis=?,tanggal_periksa=?,kd_dokter=?,kd_poli=?,no_reg=?",8,new String[]{
                         Valid.SetTgl(TanggalSurat.getSelectedItem()+""),TNoRM.getText(),
                         Valid.SetTgl(TanggalPeriksa.getSelectedItem()+""),KdDokter.getText(),
                         KdPoli.getText(),NoReg.getText(),tbObat.getValueAt(tbObat.getSelectedRow(),1).toString(),
                         tbObat.getValueAt(tbObat.getSelectedRow(),9).toString()
                    });
                    tbObat.setValueAt(TanggalPeriksa.getSelectedItem().toString().substring(6,10),tbObat.getSelectedRow(),0);
                    tbObat.setValueAt(TNoRM.getText(),tbObat.getSelectedRow(),1);
                    tbObat.setValueAt(TPasien.getText(),tbObat.getSelectedRow(),2);
                    tbObat.setValueAt(Diagnosa.getText(),tbObat.getSelectedRow(),3);
                    tbObat.setValueAt(Puasa.getSelectedItem().toString(),tbObat.getSelectedRow(),4);
                    tbObat.setValueAt(Tujuan.getText(),tbObat.getSelectedRow(),5);
                    tbObat.setValueAt(Sumber.getSelectedItem().toString(),tbObat.getSelectedRow(),6);
                    tbObat.setValueAt(ket.getText(),tbObat.getSelectedRow(),7);
                    tbObat.setValueAt(Rtl.getText(),tbObat.getSelectedRow(),8);
                    tbObat.setValueAt(Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+" "+TanggalPeriksa.getSelectedItem().toString().substring(11,19),tbObat.getSelectedRow(),9);
                    tbObat.setValueAt(Valid.SetTgl(TanggalSurat.getSelectedItem()+"")+" "+TanggalSurat.getSelectedItem().toString().substring(11,19),tbObat.getSelectedRow(),10);
                    tbObat.setValueAt(NoSurat.getText(),tbObat.getSelectedRow(),11);
                    tbObat.setValueAt(NoReg.getText(),tbObat.getSelectedRow(),12);
                    tbObat.setValueAt(KdDokter.getText(),tbObat.getSelectedRow(),13);
                    tbObat.setValueAt(NmDokter.getText(),tbObat.getSelectedRow(),14);
                    tbObat.setValueAt(KdPoli.getText(),tbObat.getSelectedRow(),15);
                    tbObat.setValueAt(NmPoli.getText(),tbObat.getSelectedRow(),16);
                    tbObat.setValueAt(Status.getSelectedItem().toString(),tbObat.getSelectedRow(),17);
                    tbObat.setValueAt(Jenis.getSelectedItem().toString(),tbObat.getSelectedRow(),18);
                    tbObat.setValueAt(Terapi.getText(),tbObat.getSelectedRow(),19);
                    tbObat.setValueAt(Obatan.getText(),tbObat.getSelectedRow(),20);
                    tbObat.setValueAt(Lain.getText(),tbObat.getSelectedRow(),21);
                    tbObat.setValueAt(Masa.getText(),tbObat.getSelectedRow(),22);
                    tbObat.setValueAt(Kesimpulan.getSelectedItem().toString(),tbObat.getSelectedRow(),23);
                    emptTeks();
                }
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih terlebih dulu data yang mau anda ganti...\n Klik data pada table untuk memilih data...!!!!");
            }                
        }
    }//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnKeluar);
        }
    }//GEN-LAST:event_BtnEditKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // Otomatis ubah Menunggu -> Sudah Periksa jika sudah lewat 1 hari dari tanggal Periksa Kembali
        Sequel.queryu("update skdp_bpjs set status='Sudah Periksa' where status='Menunggu' and tanggal_datang < curdate()");
        tampil();
    }//GEN-LAST:event_formWindowOpened

    private void DTPCari1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DTPCari1ItemStateChanged
        R2.setSelected(true);
    }//GEN-LAST:event_DTPCari1ItemStateChanged

    private void DTPCari3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DTPCari3ItemStateChanged
        R3.setSelected(true);
    }//GEN-LAST:event_DTPCari3ItemStateChanged

    private void DTPCari4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DTPCari4ItemStateChanged
        R3.setSelected(true);
    }//GEN-LAST:event_DTPCari4ItemStateChanged

    private void DiagnosaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiagnosaKeyPressed
        Valid.pindah(evt,Status,Rtl);
    }//GEN-LAST:event_DiagnosaKeyPressed

    private void MnSuratActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnSuratActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TanggalSurat.requestFocus();
        }else if(TPasien.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal menghapus. Pilih dulu data yang mau dihapus.\nKlik data pada table untuk memilih...!!!!");
        }else if(!(TPasien.getText().trim().equals(""))){
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Map<String, Object> param = new HashMap<>();  
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs());   
                param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
                param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),14).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),13).toString():finger)+"\n"+Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString()));
                Sequel.queryu("delete from temporary_booking_registrasi");                
                Sequel.menyimpan("temporary_booking_registrasi","'0','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),0).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),1).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),2).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),3).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),4).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),5).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),6).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),7).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),8).toString()+"','"+
                    TanggalPeriksa.getSelectedItem()+"','"+
                    TanggalSurat.getSelectedItem()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),11).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),12).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),13).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),14).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),15).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),16).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),17).toString()+"','"+ 
                    tabMode.getValueAt(tbObat.getSelectedRow(),18).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),19).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),20).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),21).toString()+"','"+ 
                    tabMode.getValueAt(tbObat.getSelectedRow(),22).toString()+"','"+ 
                    tabMode.getValueAt(tbObat.getSelectedRow(),23).toString()+"','','','','','','','','','','','','',''","Surat Kotrol");

                Valid.MyReport("rptSuratSKDPBPJS.jasper","report","::[ Surat Kontrol ]::",param); 
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih terlebih dulu data yang mau anda hapus...\n Klik data pada table untuk memilih data...!!!!");
            }  
        }
    }//GEN-LAST:event_MnSuratActionPerformed

    private void tbObatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyReleased
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbObatKeyReleased

    private void btnDiagnosaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiagnosaActionPerformed
        penyakit.isCek();
        penyakit.emptTeks();
        penyakit.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        penyakit.setLocationRelativeTo(internalFrame1);
        penyakit.setVisible(true);
    }//GEN-LAST:event_btnDiagnosaActionPerformed

    private void btnDiagnosaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDiagnosaKeyPressed
        Valid.pindah(evt,Status,Puasa);
    }//GEN-LAST:event_btnDiagnosaKeyPressed

    private void SumberKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SumberKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_SumberKeyPressed

    private void NoSuratKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoSuratKeyPressed
        Valid.pindah(evt,TanggalPeriksa,NoReg);
    }//GEN-LAST:event_NoSuratKeyPressed

    private void TujuanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TujuanKeyPressed
        Valid.pindah(evt,Rtl,ket);
    }//GEN-LAST:event_TujuanKeyPressed

    private void RtlKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RtlKeyPressed
        Valid.pindah(evt,Puasa,ket);
    }//GEN-LAST:event_RtlKeyPressed

    private void ketKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ketKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ketKeyPressed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        if(aktifjadwal.equals("aktif")){
            if(akses.getkode().equals("Admin Utama")){
                dokter.isCek();
                dokter.TCari.requestFocus();
                dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                dokter.setLocationRelativeTo(internalFrame1);
                dokter.setVisible(true);
            }else{
                dokter2.setPoli(NmPoli.getText());
                dokter2.isCek();
                dokter2.SetHari(TanggalPeriksa.getDate());
                dokter2.tampil3();
                dokter2.TCari.requestFocus();
                dokter2.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                dokter2.setLocationRelativeTo(internalFrame1);
                dokter2.setVisible(true);
            }
        }else{
            dokter.isCek();
            dokter.TCari.requestFocus();
            dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dokter.setLocationRelativeTo(internalFrame1);
            dokter.setVisible(true);
        }
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnDokterActionPerformed(null);
        }else{
            Valid.pindah(evt,ket,BtnPoli);
        }
    }//GEN-LAST:event_BtnDokterKeyPressed

    private void PuasaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PuasaKeyPressed
        Valid.pindah(evt,NoSurat,Tujuan);
    }//GEN-LAST:event_PuasaKeyPressed

    private void BtnPoliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPoliActionPerformed
        if(aktifjadwal.equals("aktif")){
            if(akses.getkode().equals("Admin Utama")){
                poli.isCek();
                poli.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                poli.setLocationRelativeTo(internalFrame1);
                poli.setVisible(true);
            }else{
                poli2.isCek();
                poli2.SetHari(TanggalPeriksa.getDate());
                poli2.tampil3();
                poli2.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                poli2.setLocationRelativeTo(internalFrame1);
                poli2.setVisible(true);
            }
        }else{
            poli.isCek();
            poli.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            poli.setLocationRelativeTo(internalFrame1);
            poli.setVisible(true);
        }
    }//GEN-LAST:event_BtnPoliActionPerformed

    private void BtnPoliKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPoliKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPoliActionPerformed(null);
        }else{
            Valid.pindah(evt,BtnDokter,TanggalPeriksa);
        }
    }//GEN-LAST:event_BtnPoliKeyPressed

    private void NoRegKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoRegKeyPressed
        Valid.pindah(evt,NoSurat,BtnSimpan);
    }//GEN-LAST:event_NoRegKeyPressed

    private void StatusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_StatusKeyPressed
        Valid.pindah(evt,TanggalSurat,Diagnosa);
    }//GEN-LAST:event_StatusKeyPressed

    private void TanggalPeriksaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_TanggalPeriksaItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            if(TanggalPeriksa.getSelectedItem()!=null){
                try {
                    isNomer();
                } catch (Exception e) {
                }
            }
        }
    }//GEN-LAST:event_TanggalPeriksaItemStateChanged

    private void TanggalPeriksaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalPeriksaKeyPressed
        Valid.pindah(evt,BtnPoli,NoSurat);
    }//GEN-LAST:event_TanggalPeriksaKeyPressed

    private void JenisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JenisKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_JenisKeyPressed

    private void KesimpulanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KesimpulanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_KesimpulanKeyPressed

    private void TerapiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TerapiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TerapiKeyPressed

    private void ObatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ObatanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_ObatanKeyPressed

    private void LainKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LainKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_LainKeyPressed

    private void MasaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MasaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_MasaKeyPressed

    private void MnSurat1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnSurat1ActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TanggalSurat.requestFocus();
        }else if(TPasien.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Gagal menghapus. Pilih dulu data yang mau dihapus.\nKlik data pada table untuk memilih...!!!!");
        }else if(!(TPasien.getText().trim().equals(""))){
            if(tbObat.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Map<String, Object> param = new HashMap<>();
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs());
                param.put("logo",Sequel.cariGambar("select gambar.bpjs from gambar"));
                finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
                param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),14).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),13).toString():finger)+"\n"+Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString()));
                Sequel.queryu("delete from temporary_booking_registrasi");
                Sequel.menyimpan("temporary_booking_registrasi","'0','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),0).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),1).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),2).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),3).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),4).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),5).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),6).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),7).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),8).toString()+"','"+
                    TanggalPeriksa.getSelectedItem()+"','"+
                    TanggalSurat.getSelectedItem()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),11).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),12).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),13).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),14).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),15).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),16).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),17).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),18).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),19).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),20).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),21).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),22).toString()+"','"+
                    tabMode.getValueAt(tbObat.getSelectedRow(),23).toString()+"','','','','','','','','','','','','',''","Surat Kotrol");

                Valid.MyReport("rptSuratSRBBPJS.jasper","report","::[ Surat Rujuk Balik ]::",param);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, Silahkan anda pilih terlebih dulu data yang mau anda hapus...\n Klik data pada table untuk memilih data...!!!!");
            }
        }
    }//GEN-LAST:event_MnSurat1ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            SuratKontrol dialog = new SuratKontrol(new javax.swing.JFrame(), true);
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
    private widget.Button BtnDokter;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPoli;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkInput;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Tanggal DTPCari3;
    private widget.Tanggal DTPCari4;
    private widget.TextBox Diagnosa;
    private widget.PanelBiasa FormInput;
    private widget.ComboBox Jenis;
    private widget.TextBox KdDokter;
    private widget.TextBox KdPoli;
    private widget.ComboBox Kesimpulan;
    private widget.Label LCount;
    private widget.TextBox Lain;
    private widget.TextBox Masa;
    private widget.MenuItem MnSurat;
    private widget.MenuItem MnSurat1;
    private widget.TextBox NmDokter;
    private widget.TextBox NmPoli;
    private widget.TextBox NoReg;
    private widget.TextBox NoSurat;
    private widget.TextBox Obatan;
    private javax.swing.JPanel PanelInput;
    private widget.ComboBox Puasa;
    private widget.RadioButton R1;
    private widget.RadioButton R2;
    private widget.RadioButton R3;
    private widget.TextBox Rtl;
    private widget.ScrollPane Scroll;
    private widget.ComboBox Status;
    private widget.ComboBox Sumber;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TPasien;
    private widget.Tanggal TanggalPeriksa;
    private widget.Tanggal TanggalSurat;
    private widget.TextBox Terapi;
    private widget.TextBox Tujuan;
    private widget.Button btnDiagnosa;
    private javax.swing.ButtonGroup buttonGroup1;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel22;
    private widget.Label jLabel25;
    private widget.Label jLabel27;
    private widget.Label jLabel28;
    private widget.Label jLabel29;
    private widget.Label jLabel30;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel33;
    private widget.Label jLabel34;
    private widget.Label jLabel37;
    private widget.Label jLabel4;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.TextBox ket;
    private widget.panelisi panelCari;
    private widget.panelisi panelGlass10;
    private widget.panelisi panelGlass8;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    private void tampil() {     
        if(R1.isSelected()==true){
            status=" skdp_bpjs.status='Menunggu' ";
        }else if(R2.isSelected()==true){
            status=" skdp_bpjs.tanggal_rujukan between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+"' ";
        }else if(R3.isSelected()==true){
            status=" skdp_bpjs.status<>'Menunggu' and skdp_bpjs.tanggal_rujukan between '"+Valid.SetTgl(DTPCari3.getSelectedItem()+"")+"' and '"+Valid.SetTgl(DTPCari4.getSelectedItem()+"")+"' ";           
        }
        Valid.tabelKosong(tabMode);
        try {
            ps=koneksi.prepareStatement(
                    "select skdp_bpjs.tahun,skdp_bpjs.no_rkm_medis,pasien.nm_pasien,"+
                    "skdp_bpjs.diagnosa,skdp_bpjs.puasa,skdp_bpjs.tujuan,skdp_bpjs.sumber,"+
                    "skdp_bpjs.ket,skdp_bpjs.rtl,skdp_bpjs.tanggal_datang,skdp_bpjs.tanggal_rujukan,"+
                    "skdp_bpjs.no_antrian,skdp_bpjs.kd_dokter,dokter.nm_dokter,skdp_bpjs.status,skdp_bpjs.jenis,skdp_bpjs.terapi,skdp_bpjs.obatan,skdp_bpjs.lain,skdp_bpjs.masa,skdp_bpjs.kesimpulan "+
                    "from skdp_bpjs inner join pasien inner join dokter on "+
                    "skdp_bpjs.no_rkm_medis=pasien.no_rkm_medis and skdp_bpjs.kd_dokter=dokter.kd_dokter "+
                    "where "+status+" and skdp_bpjs.no_rkm_medis like ? or "+
                    status+" and pasien.nm_pasien like ? or "+
                    status+" and skdp_bpjs.diagnosa like ? or "+
                    status+" and skdp_bpjs.puasa like ? or "+
                    status+" and skdp_bpjs.no_antrian like ? or "+
                    status+" and dokter.nm_dokter like ? order by skdp_bpjs.tanggal_rujukan,skdp_bpjs.no_antrian");
            try {
                ps.setString(1,"%"+TCari.getText().trim()+"%");
                ps.setString(2,"%"+TCari.getText().trim()+"%");
                ps.setString(3,"%"+TCari.getText().trim()+"%");
                ps.setString(4,"%"+TCari.getText().trim()+"%");
                ps.setString(5,"%"+TCari.getText().trim()+"%");
                ps.setString(6,"%"+TCari.getText().trim()+"%");
                rs=ps.executeQuery();
                while(rs.next()){
                    kdpoli="";nmpoli="";noantri="";
                    ps2=koneksi.prepareStatement(
                            "select booking_registrasi.kd_poli,poliklinik.nm_poli,booking_registrasi.no_reg "+
                            "from booking_registrasi inner join poliklinik on booking_registrasi.kd_poli=poliklinik.kd_poli "+
                            "where booking_registrasi.kd_dokter=? and booking_registrasi.tanggal_periksa=? and booking_registrasi.no_rkm_medis=?");
                    try {
                        ps2.setString(1,rs.getString("kd_dokter"));
                        ps2.setString(2,rs.getString("tanggal_datang").substring(0,10));
                        ps2.setString(3,rs.getString("no_rkm_medis"));
                        rs2=ps2.executeQuery();
                        if(rs2.next()){
                            kdpoli=rs2.getString("kd_poli");
                            nmpoli=rs2.getString("nm_poli");
                            noantri=rs2.getString("no_reg");
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rs2!=null){
                            rs2.close();
                        }
                        if(ps2!=null){
                            ps2.close();
                        }
                    }
                    tabMode.addRow(new Object[]{
                        rs.getInt("tahun"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
                        rs.getString("diagnosa"),rs.getString("puasa"),rs.getString("tujuan"),
                        rs.getString("sumber"),rs.getString("ket"),rs.getString("rtl"),
                        rs.getString("tanggal_datang"),rs.getString("tanggal_rujukan"),rs.getString("no_antrian"),
                        noantri,rs.getString("kd_dokter"),rs.getString("nm_dokter"),kdpoli,
                        nmpoli,rs.getString("status"),rs.getString("jenis"),rs.getString("terapi"),rs.getString("obatan"),rs.getString("lain"),rs.getString("masa"),rs.getString("kesimpulan")
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
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        } 
        LCount.setText(""+tabMode.getRowCount());
    }


    public void emptTeks() {
        KdDokter.setText("");
        NmDokter.setText("");
        KdPoli.setText("");
        NmPoli.setText("");
        Tujuan.setText("-");
        ket.setText("-");
        Rtl.setText("-");
        Sumber.setSelectedItem("");
        Puasa.setSelectedItem("");
        Jenis.setSelectedItem("");
        Terapi.setText("-");
        Obatan.setText("-");
        Lain.setText("-");
        Masa.setText("-");
        Kesimpulan.setSelectedItem("");
        Diagnosa.setText("-");
        TanggalSurat.setDate(new Date());
        TanggalSurat.requestFocus();
        nomorSurat();
    }
    
    private void isNomer(){
        switch (URUTNOREG) {
            case "poli":
                Valid.autoNomer3("select ifnull(MAX(CONVERT(booking_registrasi.no_reg,signed)),0) from booking_registrasi where booking_registrasi.kd_poli='"+KdPoli.getText()+"' and booking_registrasi.tanggal_periksa='"+Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+"'","",3,NoReg);
                break;
            case "dokter":
                Valid.autoNomer3("select ifnull(MAX(CONVERT(booking_registrasi.no_reg,signed)),0) from booking_registrasi where booking_registrasi.kd_dokter='"+KdDokter.getText()+"' and booking_registrasi.tanggal_periksa='"+Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+"'","",3,NoReg);
                break;
            case "dokter + poli":             
                Valid.autoNomer3("select ifnull(MAX(CONVERT(booking_registrasi.no_reg,signed)),0) from booking_registrasi where booking_registrasi.kd_dokter='"+KdDokter.getText()+"' and booking_registrasi.kd_poli='"+KdPoli.getText()+"' and booking_registrasi.tanggal_periksa='"+Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+"'","",3,NoReg);
                break;
            default:
                Valid.autoNomer3("select ifnull(MAX(CONVERT(booking_registrasi.no_reg,signed)),0) from booking_registrasi where booking_registrasi.kd_dokter='"+KdDokter.getText()+"' and booking_registrasi.tanggal_periksa='"+Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+"'","",3,NoReg);
                break;
        }
        //Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(skdp_bpjs.no_antrian,6),signed)),0) from skdp_bpjs where skdp_bpjs.tahun='"+TanggalPeriksa.getSelectedItem().toString().substring(6,10)+"' ","",6,NoSurat);  
    }

    private void getData() {
    if(tbObat.getSelectedRow()!= -1){
        TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString()); 

        // Ambil nomor HP dari tabel pasien
        ket.setText(
            Sequel.cariIsi(
                "select no_tlp from pasien where no_rkm_medis=?",
                TNoRM.getText()
            )
        );

        TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
        Diagnosa.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());
        Puasa.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString());  
        Tujuan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());
        Sumber.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());  
        // ket.setText(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString());  // <-- HAPUS atau komentar baris ini
        Rtl.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());
        Valid.SetTgl2(TanggalPeriksa,tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
        Valid.SetTgl2(TanggalSurat,tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
        NoSurat.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
        NoReg.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
        KdDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
        NmDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
        KdPoli.setText(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
        NmPoli.setText(tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());
        Status.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),17).toString());
        Jenis.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),18).toString()); 
        Terapi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),19).toString());
        Obatan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),20).toString());
        Lain.setText(tbObat.getValueAt(tbObat.getSelectedRow(),21).toString());
        Masa.setText(tbObat.getValueAt(tbObat.getSelectedRow(),22).toString());
        Kesimpulan.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),23).toString()); 
    }
}
    
    public void setNoRm(String norm,String nama) {
    TNoRM.setText(norm);
    TPasien.setText(nama);

    // Ambil nomor HP pasien
    ket.setText(
        Sequel.cariIsi(
            "select no_tlp from pasien where no_rkm_medis=?",
            norm
        )
    );

    TCari.setText(norm);
    ChkInput.setSelected(true);
    isForm();
    tampil();
}
    
   public void setNoRm(String norm,String nama,String kodepoli,String namapoli,String kodedokter,String namadokter) {
    TNoRM.setText(norm);
    TPasien.setText(nama);
    KdPoli.setText(kodepoli);
    NmPoli.setText(namapoli);
    KdDokter.setText(kodedokter);
    NmDokter.setText(namadokter);

    // Ambil nomor HP pasien
    ket.setText(
        Sequel.cariIsi(
            "select no_tlp from pasien where no_rkm_medis=?",
            norm
        )
    );

    TCari.setText(norm);
    ChkInput.setSelected(true);
    isForm();
    tampil();
}
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,410));
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
        BtnSimpan.setEnabled(akses.getskdp_bpjs());
        BtnHapus.setEnabled(akses.getskdp_bpjs());
        BtnPrint.setEnabled(akses.getskdp_bpjs());
        BtnEdit.setEnabled(akses.getskdp_bpjs());
    }

    public JTable getTable(){
        return tbObat;
    }

    private void isBooking(){
        if(Sequel.menyimpantf("skdp_bpjs","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","Tahun dan nomor surat",19,new String[]{
TanggalPeriksa.getSelectedItem().toString().substring(6,10),TNoRM.getText(),Diagnosa.getText(),Puasa.getSelectedItem()+"",
             Tujuan.getText(),Sumber.getSelectedItem()+"",ket.getText(),Rtl.getText(),Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+" "+TanggalPeriksa.getSelectedItem().toString().substring(11,19),
             Valid.SetTgl(TanggalSurat.getSelectedItem()+"")+" "+TanggalSurat.getSelectedItem().toString().substring(11,19),NoSurat.getText(),KdDokter.getText(),Status.getSelectedItem().toString(),Jenis.getSelectedItem().toString(),Terapi.getText(),Obatan.getText(),Lain.getText(),Masa.getText(),Kesimpulan.getSelectedItem().toString()
         })==true){
                Sequel.menyimpan2("booking_registrasi","?,?,?,?,?,?,?,?,?,?,?,?,?,?","Pasien dan Tanggal",14,new String[]{
                   Valid.SetTgl(TanggalSurat.getSelectedItem()+""),TanggalSurat.getSelectedItem().toString().substring(11,19),TNoRM.getText(),
                   Valid.SetTgl(TanggalPeriksa.getSelectedItem()+""),KdDokter.getText(),
                   KdPoli.getText(),NoReg.getText(),Sequel.cariIsi("select pasien.kd_pj from pasien where pasien.no_rkm_medis=?",TNoRM.getText()),"0",
                   Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+" "+TanggalPeriksa.getSelectedItem().toString().substring(11,19),
                "belum",Sumber.getSelectedItem()+"",ket.getText(),Jenis.getSelectedItem().toString()
                });
             tabMode.addRow(new String[]{
               TanggalPeriksa.getSelectedItem().toString().substring(6,10),TNoRM.getText(),TPasien.getText(),Diagnosa.getText(),Puasa.getSelectedItem()+"",Tujuan.getText(),Sumber.getSelectedItem()+"",
                ket.getText(),Rtl.getText(),Valid.SetTgl(TanggalPeriksa.getSelectedItem()+"")+" "+TanggalPeriksa.getSelectedItem().toString().substring(11,19),
                Valid.SetTgl(TanggalSurat.getSelectedItem()+"")+" "+TanggalSurat.getSelectedItem().toString().substring(11,19),NoSurat.getText(),NoReg.getText(),
                KdDokter.getText(),NmDokter.getText(),KdPoli.getText(),NmPoli.getText(),Status.getSelectedItem().toString(),Jenis.getSelectedItem().toString(),Terapi.getText(),Obatan.getText(),Lain.getText(),Masa.getText(),Kesimpulan.getSelectedItem().toString()
             });
             emptTeks();
             LCount.setText(""+tabMode.getRowCount());
         } 
    }
    private void nomorSurat() {
        bln_angka = "";
        bln_romawi = "";
        
        bln_angka = TanggalSurat.getSelectedItem().toString().substring(3,5);
        
        if (bln_angka.equals("01")) {
            //bln_angka  = "01";
            bln_romawi = "I";
        } else if (bln_angka.equals("02")) {
            //bln_angka  = "02";
            bln_romawi = "II";
        } else if (bln_angka.equals("03")) {
            //bln_angka  = "03";
            bln_romawi = "III";
        } else if (bln_angka.equals("04")) {
            //bln_angka  = "04";
            bln_romawi = "IV";
        } else if (bln_angka.equals("05")) {
            //bln_angka  = "05";
            bln_romawi = "V";
        } else if (bln_angka.equals("06")) {
            //bln_angka  = "06";
            bln_romawi = "VI";
        } else if (bln_angka.equals("07")) {
            //bln_angka  = "07";
            bln_romawi = "VII";
        } else if (bln_angka.equals("08")) {
            //bln_angka  = "08";
            bln_romawi = "VIII";
        } else if (bln_angka.equals("09")) {
            //bln_angka  = "09";
            bln_romawi = "IX";
        } else if (bln_angka.equals("10")) {
            //bln_angka  = "10";
            bln_romawi = "X";
        } else if (bln_angka.equals("11")) {
            //bln_angka  = "11";
            bln_romawi = "XI";
        } else if (bln_angka.equals("12")) {
            //bln_angka  = "12";
            bln_romawi = "XII";
}

        Valid1.autoNomerSuratKhusus1("select ifnull(MAX(CONVERT(left(no_antrian,5),signed)),0), MONTH(tanggal_rujukan) bln from skdp_bpjs where "
                + "tanggal_rujukan like '%" + Valid.SetTgl(TanggalSurat.getSelectedItem() + "").substring(0, 7) + "%' ", "/SKN/RSUB/" + bln_romawi + "/" + TanggalSurat.getSelectedItem().toString().substring(6,10), 5, NoSurat);
        bln_angka = Sequel.cariIsi("SELECT MONTH(tanggal_rujukan) bln FROM skdp_bpjs WHERE tanggal_rujukan like '%" + Valid.SetTgl(TanggalSurat.getSelectedItem() + "").substring(0, 7) + "%'");
    } 
}
