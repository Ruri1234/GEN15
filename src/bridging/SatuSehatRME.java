package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import widget.Button;
import widget.InternalFrame;
import widget.Label;
import widget.PanelBiasa;
import widget.TextBox;

/**
 * Modul Integration SATUSEHAT RME v2 (CHL & SHL Viewer)
 * Menggunakan URLFHIRSATUSEHAT() & Auto Fetch Identitas Dokter dari User Login SIMRS
 */
public class SatuSehatRME extends javax.swing.JDialog {

    private final validasi Valid = new validasi();
    private final sekuel Sequel = new sekuel();
    private final ApiSatuSehat api = new ApiSatuSehat();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Connection koneksi = koneksiDB.condb();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Variable UI Components
    private InternalFrame internalFrame1;
    private PanelBiasa panelHeader, panelContent, panelFooter;
    private Label lblTitle, lblSubTitle, lblStatus;

    // Panel Pasien & Dokter
    private Label lblNikPasien, lblIhsPasien, lblNamaPasien;
    private TextBox txtNikPasien, txtIhsPasien, txtNamaPasien;

    private Label lblNikDokter, lblIhsDokter, lblNamaDokter;
    private TextBox txtNikDokter, txtIhsDokter, txtNamaDokter;

    private JCheckBox chkEmergency;

    // Output Response Tabs
    private JTextArea txtChlResult, txtShlResult, txtLog;
    private Button btnPostCHL, btnPostSHL, btnKeluar;

    public SatuSehatRME(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Bridging SATUSEHAT - Rekam Medis Elektronik");
        setUndecorated(true);
        setResizable(false);
        setSize(950, 680);

        internalFrame1 = new InternalFrame();
        internalFrame1.setBorder(BorderFactory.createLineBorder(new Color(210, 225, 205), 2));
        internalFrame1.setLayout(new BorderLayout(0, 0));

        // 1. HEADER PANEL
        panelHeader = new PanelBiasa();
        panelHeader.setBackground(new Color(245, 250, 240));
        panelHeader.setPreferredSize(new Dimension(950, 55));
        panelHeader.setLayout(new BorderLayout());
        panelHeader.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        lblTitle = new Label();
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblTitle.setForeground(new Color(50, 70, 50));
        lblTitle.setText("BRIDGING SATUSEHAT - REKAM MEDIS ELEKTRONIK (RME)");

        lblSubTitle = new Label();
        lblSubTitle.setText("Validasi Consent Handler Link (CHL) & Akses Smart Health Link (SHL) RME Pasien");
        lblSubTitle.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblSubTitle.setForeground(new Color(100, 120, 100));

        JPanel pnlTitleText = new JPanel(new BorderLayout());
        pnlTitleText.setOpaque(false);
        pnlTitleText.add(lblTitle, BorderLayout.NORTH);
        pnlTitleText.add(lblSubTitle, BorderLayout.SOUTH);

        panelHeader.add(pnlTitleText, BorderLayout.WEST);
        internalFrame1.add(panelHeader, BorderLayout.NORTH);

        // 2. CONTENT PANEL
        panelContent = new PanelBiasa();
        panelContent.setLayout(new BorderLayout(5, 5));
        panelContent.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // Sub-Panel Form Input
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
        pnlForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 205, 175)),
                "::[ Prepared Atribut Pasien & SDM Kesehatan ]::",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Tahoma", Font.BOLD, 11),
                new Color(60, 90, 60)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblNikPasien = createLabel("NIK Pasien :");
        txtNikPasien = createTextBox();
        lblNamaPasien = createLabel("Nama Pasien :");
        txtNamaPasien = createReadOnlyTextBox();
        lblIhsPasien = createLabel("IHS Pasien :");
        txtIhsPasien = createReadOnlyTextBox();

        lblNikDokter = createLabel("NIK Dokter :");
        txtNikDokter = createReadOnlyTextBox(); // Readonly karena otomatis dari User Login
        lblNamaDokter = createLabel("Nama Dokter :");
        txtNamaDokter = createReadOnlyTextBox();
        lblIhsDokter = createLabel("IHS Dokter :");
        txtIhsDokter = createReadOnlyTextBox();

        chkEmergency = new JCheckBox("KONDISI EMERGENCY / GAWAT DARURAT (Bypass Consent Pasien)");
        chkEmergency.setFont(new Font("Tahoma", Font.BOLD, 11));
        chkEmergency.setForeground(Color.RED);
        chkEmergency.setOpaque(false);

        // Baris 0: Pasien
        gbc.gridx = 0; gbc.gridy = 0; pnlForm.add(lblNikPasien, gbc);
        gbc.gridx = 1; pnlForm.add(txtNikPasien, gbc);
        gbc.gridx = 2; pnlForm.add(lblNamaPasien, gbc);
        gbc.gridx = 3; pnlForm.add(txtNamaPasien, gbc);
        gbc.gridx = 4; pnlForm.add(lblIhsPasien, gbc);
        gbc.gridx = 5; pnlForm.add(txtIhsPasien, gbc);

        // Baris 1: Dokter (User Login)
        gbc.gridx = 0; gbc.gridy = 1; pnlForm.add(lblNikDokter, gbc);
        gbc.gridx = 1; pnlForm.add(txtNikDokter, gbc);
        gbc.gridx = 2; pnlForm.add(lblNamaDokter, gbc);
        gbc.gridx = 3; pnlForm.add(txtNamaDokter, gbc);
        gbc.gridx = 4; pnlForm.add(lblIhsDokter, gbc);
        gbc.gridx = 5; pnlForm.add(txtIhsDokter, gbc);

        // Baris 2: Emergency
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 6;
        pnlForm.add(chkEmergency, gbc);

        panelContent.add(pnlForm, BorderLayout.NORTH);

        // Sub-Panel Tabs Output
        txtChlResult = createTextArea();
        txtShlResult = createTextArea();
        txtLog = createTextArea();

        JTabbedPane tab = new JTabbedPane();
        tab.addTab("01. Consent Handler Link (CHL)", new JScrollPane(txtChlResult));
        tab.addTab("02. Smart Health Link / RME (SHL)", new JScrollPane(txtShlResult));
        tab.addTab("JSON Log System", new JScrollPane(txtLog));

        panelContent.add(tab, BorderLayout.CENTER);
        internalFrame1.add(panelContent, BorderLayout.CENTER);

        // 3. FOOTER PANEL
        panelFooter = new PanelBiasa();
        panelFooter.setLayout(new BorderLayout());
        panelFooter.setPreferredSize(new Dimension(950, 50));
        panelFooter.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        lblStatus = new Label();
        lblStatus.setText("Status: Ready.");
        lblStatus.setFont(new Font("Tahoma", Font.ITALIC, 11));
        lblStatus.setForeground(new Color(100, 100, 100));

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlButtons.setOpaque(false);

        // Tombol 1: POST CHL & Auto Open Verification URL
        btnPostCHL = new Button();
        btnPostCHL.setIcon(getSafeIcon("/picture/accept.png"));
        btnPostCHL.setText("01. POST CHL & Buka Link");
        btnPostCHL.setPreferredSize(new Dimension(190, 32));
        btnPostCHL.addActionListener((ActionEvent e) -> runBackground(this::prosesPostCHL));

        // Tombol 2: POST SHL & Auto Open SHL RME URL
        btnPostSHL = new Button();
        btnPostSHL.setIcon(getSafeIcon("/picture/accept.png"));
        btnPostSHL.setText("02. POST SHL & Akses RME");
        btnPostSHL.setPreferredSize(new Dimension(190, 32));
        btnPostSHL.addActionListener((ActionEvent e) -> runBackground(this::prosesPostSHL));

        btnKeluar = new Button();
        btnKeluar.setIcon(getSafeIcon("/picture/exit.png"));
        btnKeluar.setText("Keluar");
        btnKeluar.setPreferredSize(new Dimension(90, 32));
        btnKeluar.addActionListener((ActionEvent e) -> dispose());

        pnlButtons.add(btnPostCHL);
        pnlButtons.add(btnPostSHL);
        pnlButtons.add(btnKeluar);

        panelFooter.add(lblStatus, BorderLayout.WEST);
        panelFooter.add(pnlButtons, BorderLayout.EAST);

        internalFrame1.add(panelFooter, BorderLayout.SOUTH);
        getContentPane().add(internalFrame1, BorderLayout.CENTER);
        pack();
    }

    /**
     * Dipanggil saat membuka form dari pemanggilan ralan/ranap
     */
    public void setPasien(String nikPasien) {
        if (nikPasien != null) txtNikPasien.setText(nikPasien.trim());

        runBackground(() -> {
            cariDataDokterLogin(); // Mengambil identitas dokter dari user login SIMRS
            cariDataPasienByNik();
        });
    }

    public void setPasien(String nikPasien, String nikDokterIgnored) {
        setPasien(nikPasien);
    }

    // ==========================================
    // LOGIKA PENGAMBILAN DOKTER DARI USER LOGIN
    // ==========================================

    private void cariDataDokterLogin() {
        String idUser = akses.getkode(); // Mengambil kode user yang login dari SIMRS Khanza
        if (idUser == null || idUser.trim().isEmpty()) {
            updateStatus("User Login tidak terdeteksi!");
            return;
        }

        updateStatus("Mengambil identitas dokter dari User Login (" + idUser + ")...");
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Priority 1: Cek di tabel Dokter & Pegawai
            String sql = "SELECT p.no_ktp, d.nm_dokter " +
                         "FROM dokter d " +
                         "INNER JOIN pegawai p ON d.kd_dokter = p.nik " +
                         "WHERE d.kd_dokter = ? OR p.no_ktp = ? LIMIT 1";

            ps = koneksi.prepareStatement(sql);
            ps.setString(1, idUser);
            ps.setString(2, idUser);
            rs = ps.executeQuery();

            String nikKtpDokter = "";
            String namaDokter = "";

            if (rs.next()) {
                nikKtpDokter = rs.getString("no_ktp");
                namaDokter = rs.getString("nm_dokter");
            } else {
                // Priority 2: Jika user login pegawai biasa/petugas
                if (ps != null) ps.close();
                if (rs != null) rs.close();

                sql = "SELECT no_ktp, nama FROM pegawai WHERE nik = ? OR no_ktp = ? LIMIT 1";
                ps = koneksi.prepareStatement(sql);
                ps.setString(1, idUser);
                ps.setString(2, idUser);
                rs = ps.executeQuery();

                if (rs.next()) {
                    nikKtpDokter = rs.getString("no_ktp");
                    namaDokter = rs.getString("nama");
                }
            }

            final String finalNik = (nikKtpDokter != null && !nikKtpDokter.trim().isEmpty()) ? nikKtpDokter.trim() : idUser;
            final String finalNama = namaDokter;

            SwingUtilities.invokeLater(() -> {
                txtNikDokter.setText(finalNik);
                if (finalNama != null && !finalNama.trim().isEmpty()) {
                    txtNamaDokter.setText(finalNama);
                }
            });

            // Cari IHS Practitioner di SATUSEHAT
            if (!finalNik.isEmpty()) {
                cariIhsDokterViaNik(finalNik);
            }

        } catch (Exception e) {
            logError("Gagal mengambil data dokter login", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (Exception e) {}
        }
    }

    private void cariIhsDokterViaNik(String nikKtp) {
        try {
            String token = api.TokenSatuSehat();
            if (token == null || token.isEmpty()) return;

            String baseFhir = getBaseFhirUrl();
            String targetUrl = baseFhir + "/Practitioner?identifier=https://fhir.kemkes.go.id/id/nik|" + URLEncoder.encode(nikKtp, StandardCharsets.UTF_8.name());

            JsonNode res = httpGet(targetUrl, token);
            if (res != null && res.has("entry") && res.path("entry").isArray() && res.path("entry").size() > 0) {
                JsonNode resource = res.path("entry").get(0).path("resource");
                String ihs = resource.path("id").asText();
                String name = extractNameFromFhirResource(resource);

                final String finalName = name;
                SwingUtilities.invokeLater(() -> {
                    txtIhsDokter.setText(ihs);
                    if (txtNamaDokter.getText().trim().isEmpty() && !finalName.isEmpty()) {
                        txtNamaDokter.setText(finalName);
                    }
                });
                updateStatus("Dokter Login Terdeteksi! IHS: " + ihs);
            } else {
                updateStatus("Dokter Login (NIK " + nikKtp + ") tidak ditemukan di SATUSEHAT.");
            }
        } catch (Exception e) {
            logError("Gagal Cari IHS Dokter", e);
        }
    }

    // ==========================================
    // LOGIKA PROSES SATUSEHAT RME (CHL & SHL v2)
    // ==========================================

    private void prosesPostCHL() {
        if (!validasiAtribut()) return;

        updateStatus("Mengirim POST CHL (Consent Handler Link)...");
        try {
            String token = api.TokenSatuSehat();
            if (token == null || token.isEmpty()) {
                updateStatus("Gagal memperoleh Access Token SATUSEHAT.");
                return;
            }

            Map<String, Object> bodyMap = preparePayloadMap();
            String jsonPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bodyMap);

            String urlChl = getBaseSSRMEUrl() + "/chl";
            appendLog("\n--- [POST CHL REQUEST] ---\nURL: " + urlChl + "\nPayload:\n" + jsonPayload);

            JsonNode response = httpPost(urlChl, token, jsonPayload);
            String prettyRes = formatJson(response);

            appendLog("\n--- [POST CHL RESPONSE] ---\n" + prettyRes);
            SwingUtilities.invokeLater(() -> txtChlResult.setText("=== CHL RESPONSE ===\n" + prettyRes));

            // Ekstrak verificationUrl dari JSON Response
            String targetUrl = "";
            if (response.has("data") && response.path("data").has("verificationUrl")) {
                targetUrl = response.path("data").path("verificationUrl").asText();
            } else if (response.has("data") && response.path("data").has("url")) {
                targetUrl = response.path("data").path("url").asText();
            } else if (response.has("verificationUrl")) {
                targetUrl = response.path("verificationUrl").asText();
            } else if (response.has("url")) {
                targetUrl = response.path("url").asText();
            }

            if (!targetUrl.isEmpty()) {
                updateStatus("CHL Berhasil! Membuka verificationUrl di Browser...");
                bukaUrlBrowser(targetUrl);
            } else {
                updateStatus("CHL Berhasil Diproses (URL Verifikasi tidak ditemukan di response).");
            }
        } catch (Exception e) {
            logError("Gagal POST CHL", e);
        }
    }

    private void prosesPostSHL() {
        if (!validasiAtribut()) return;

        updateStatus("Mengirim POST SHL (Akses Smart Health Link RME)...");
        try {
            String token = api.TokenSatuSehat();
            if (token == null || token.isEmpty()) {
                updateStatus("Gagal memperoleh Access Token SATUSEHAT.");
                return;
            }

            Map<String, Object> bodyMap = preparePayloadMap();
            String jsonPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bodyMap);

            String urlShl = getBaseSSRMEUrl() + "/shl";
            appendLog("\n--- [POST SHL REQUEST] ---\nURL: " + urlShl + "\nPayload:\n" + jsonPayload);

            JsonNode response = httpPost(urlShl, token, jsonPayload);
            String prettyRes = formatJson(response);

            appendLog("\n--- [POST SHL RESPONSE] ---\n" + prettyRes);
            SwingUtilities.invokeLater(() -> txtShlResult.setText("=== SHL RESPONSE ===\n" + prettyRes));

            // Ekstrak shlinkUrl dari JSON Response
            String targetUrl = "";
            if (response.has("data") && response.path("data").has("shlinkUrl")) {
                targetUrl = response.path("data").path("shlinkUrl").asText();
            } else if (response.has("data") && response.path("data").has("url")) {
                targetUrl = response.path("data").path("url").asText();
            } else if (response.has("shlinkUrl")) {
                targetUrl = response.path("shlinkUrl").asText();
            } else if (response.has("url")) {
                targetUrl = response.path("url").asText();
            }

            if (!targetUrl.isEmpty()) {
                updateStatus("SHL Berhasil! Membuka Web RME di Browser...");
                bukaUrlBrowser(targetUrl);
            } else {
                updateStatus("SHL Diproses. Periksa detail JSON Response.");
            }
        } catch (Exception e) {
            logError("Gagal POST SHL", e);
        }
    }

    private Map<String, Object> preparePayloadMap() {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("patient_id", txtIhsPasien.getText().trim());
        bodyMap.put("patient_name", txtNamaPasien.getText().trim());
        bodyMap.put("practitioner_id", txtIhsDokter.getText().trim());
        bodyMap.put("practitioner_name", txtNamaDokter.getText().trim());
        bodyMap.put("organization_id", koneksiDB.IDSATUSEHAT());
        bodyMap.put("organization_name", Sequel.cariIsi("select nama_instansi from setting limit 1"));

        if (chkEmergency.isSelected()) {
            bodyMap.put("type_medical_summary", "EMERGENCY");
        }
        return bodyMap;
    }

    private void bukaUrlBrowser(String urlTarget) {
        if (urlTarget == null || urlTarget.trim().isEmpty()) return;

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(urlTarget.trim()));
            } else {
                JOptionPane.showMessageDialog(this, "URL Target: " + urlTarget, "Akses Web SATUSEHAT", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            logError("Gagal Membuka Browser", e);
        }
    }

    // ==========================================
    // LOGIKA PENCARIAN IHS PASIEN
    // ==========================================

    private void cariDataPasienByNik() {
        String nik = txtNikPasien.getText().trim();
        if (nik.isEmpty()) return;

        updateStatus("Mencari IHS Pasien via NIK...");
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT nm_pasien FROM pasien WHERE no_ktp = ? OR no_rkm_medis = ? LIMIT 1";
            ps = koneksi.prepareStatement(sql);
            ps.setString(1, nik);
            ps.setString(2, nik);
            rs = ps.executeQuery();

            if (rs.next()) {
                String namaLokal = rs.getString("nm_pasien");
                SwingUtilities.invokeLater(() -> txtNamaPasien.setText(namaLokal));
            }

            String token = api.TokenSatuSehat();
            if (token == null || token.isEmpty()) return;

            String baseFhir = getBaseFhirUrl();
            String targetUrl = baseFhir + "/Patient?identifier=https://fhir.kemkes.go.id/id/nik|" + URLEncoder.encode(nik, StandardCharsets.UTF_8.name());

            JsonNode res = httpGet(targetUrl, token);
            if (res != null && res.has("entry") && res.path("entry").isArray() && res.path("entry").size() > 0) {
                JsonNode resource = res.path("entry").get(0).path("resource");
                String ihs = resource.path("id").asText();

                String name = extractNameFromFhirResource(resource);

                final String finalName = name;
                SwingUtilities.invokeLater(() -> {
                    txtIhsPasien.setText(ihs);
                    if (txtNamaPasien.getText().trim().isEmpty() && !finalName.isEmpty()) {
                        txtNamaPasien.setText(finalName);
                    }
                });
                updateStatus("Pasien Ditemukan! IHS: " + ihs);
            } else {
                updateStatus("Pasien dengan NIK tersebut tidak ditemukan di SATUSEHAT.");
            }
        } catch (Exception e) {
            logError("Gagal Cari Pasien", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (Exception e) {}
        }
    }

    // ==========================================
    // UTILITY HELPER METHODS
    // ==========================================

    private String getBaseFhirUrl() {
        String url = koneksiDB.URLFHIRSATUSEHAT();
        if (url == null || url.trim().isEmpty()) {
            url = "https://api-satusehat.kemkes.go.id/fhir-r4/v1";
        }
        url = url.trim().replaceAll("/+$", "");
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        return url;
    }

    private String getBaseSSRMEUrl() {
        String url = getBaseFhirUrl();
        if (url.contains("/fhir-r4/v1")) {
            url = url.replace("/fhir-r4/v1", "");
        }
        return url + "/ssrme/v2/ntl";
    }

    private JsonNode httpGet(String url, String token) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = api.getRest().exchange(url, HttpMethod.GET, entity, String.class);
            return mapper.readTree(response.getBody() == null ? "{}" : response.getBody());
        } catch (HttpStatusCodeException ex) {
            String err = ex.getResponseBodyAsString();
            appendLog("\n[HTTP GET ERROR " + ex.getStatusCode().value() + "]: " + err);
            return mapper.readTree(err.isEmpty() ? "{}" : err);
        }
    }

    private JsonNode httpPost(String url, String token, String jsonBody) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        try {
            ResponseEntity<String> response = api.getRest().exchange(url, HttpMethod.POST, entity, String.class);
            return mapper.readTree(response.getBody() == null ? "{}" : response.getBody());
        } catch (HttpStatusCodeException ex) {
            String err = ex.getResponseBodyAsString();
            appendLog("\n[HTTP POST ERROR " + ex.getStatusCode().value() + "]: " + err);
            return mapper.readTree(err.isEmpty() ? "{}" : err);
        }
    }

    private String extractNameFromFhirResource(JsonNode resource) {
        String name = "";
        JsonNode nameNode = resource.path("name");
        if (nameNode.isArray() && nameNode.size() > 0) {
            JsonNode textNode = nameNode.get(0).path("text");
            if (!textNode.isMissingNode() && !textNode.isNull()) {
                name = textNode.asText();
            }
        }
        return name;
    }

    private String formatJson(JsonNode json) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            return json != null ? json.toString() : "{}";
        }
    }

    private boolean validasiAtribut() {
        if (txtIhsPasien.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "IHS Pasien belum diisi/ditemukan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtIhsDokter.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "IHS Dokter (User Login) belum diisi/ditemukan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (koneksiDB.IDSATUSEHAT() == null || koneksiDB.IDSATUSEHAT().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Organization ID Faskes belum dikonfigurasi di koneksiDB!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void runBackground(Runnable task) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        executor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logError("Execution Error", e);
            } finally {
                SwingUtilities.invokeLater(() -> setCursor(Cursor.getDefaultCursor()));
            }
        });
    }

    private Label createLabel(String text) {
        Label lbl = new Label();
        lbl.setText(text);
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl.setPreferredSize(new Dimension(90, 23));
        return lbl;
    }

    private TextBox createTextBox() {
        TextBox txt = new TextBox();
        txt.setPreferredSize(new Dimension(130, 23));
        return txt;
    }

    private TextBox createReadOnlyTextBox() {
        TextBox txt = new TextBox();
        txt.setEditable(false);
        txt.setBackground(new Color(242, 245, 240));
        txt.setPreferredSize(new Dimension(140, 23));
        return txt;
    }

    private JTextArea createTextArea() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setMargin(new Insets(6, 6, 6, 6));
        return ta;
    }

    private ImageIcon getSafeIcon(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) return new ImageIcon(url);
        } catch (Exception e) {}
        return null;
    }

    private void updateStatus(String msg) {
        SwingUtilities.invokeLater(() -> lblStatus.setText("Status: " + msg));
    }

    private void appendLog(String text) {
        SwingUtilities.invokeLater(() -> txtLog.append(text + "\n"));
    }

    private void logError(String prefix, Exception e) {
        System.err.println("[" + prefix + "]: " + e.getMessage());
        updateStatus("Error: " + e.getMessage());
        appendLog("\n[ERROR " + prefix + "]: " + e.getMessage());
    }

    @Override
    public void dispose() {
        executor.shutdownNow();
        super.dispose();
    }
}