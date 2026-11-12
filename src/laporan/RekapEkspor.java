package laporan;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Ekspor tabel rekap 10 besar ke Excel (.xlsx) dan PDF.
 * Ditulis langsung (OOXML + PDF 1.4) sehingga TIDAK butuh library tambahan (POI / iText).
 * Dipakai bersama oleh RekapPenyakitRanap, RekapPenyakitRalan, RekapSebabRanap, dan RekapSebabRalan.
 *
 * Parameter angka[i] = {laki-laki, perempuan, total, mati laki-laki, mati perempuan, total mati}.
 */
final class RekapEkspor {

    private RekapEkspor() {
    }

    private static final String[] SUB = {"Laki-Laki", "Perempuan", "Total", "Laki-Laki", "Perempuan", "Total"};

    // ==================================================================
    //  EXCEL (.xlsx)
    // ==================================================================
    static void tulisXlsx(File f, String judul, String periode, String dasar, String grup1, String grup2,
            String[] kode, String[] nama, int[][] angka) throws IOException {
        final String kol = "ABCDEFGHI";
        int n = kode.length;
        int rAwal = 6, rAkhir = rAwal + n - 1, rTotal = rAkhir + 1;

        StringBuilder sh = new StringBuilder();
        sh.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sh.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        sh.append("<sheetPr><pageSetUpPr fitToPage=\"1\"/></sheetPr>");
        sh.append("<sheetViews><sheetView showGridLines=\"0\" workbookViewId=\"0\"/></sheetViews>");
        sh.append("<sheetFormatPr defaultRowHeight=\"15\"/>");
        sh.append("<cols>")
                .append("<col min=\"1\" max=\"1\" width=\"6\" customWidth=\"1\"/>")
                .append("<col min=\"2\" max=\"2\" width=\"18\" customWidth=\"1\"/>")
                .append("<col min=\"3\" max=\"3\" width=\"62\" customWidth=\"1\"/>")
                .append("<col min=\"4\" max=\"9\" width=\"13\" customWidth=\"1\"/>")
                .append("</cols><sheetData>");

        // baris 1-2: judul + periode
        sh.append("<row r=\"1\" ht=\"24\" customHeight=\"1\">");
        sel(sh, "A1", 1, judul);
        sh.append("</row>");
        sh.append("<row r=\"2\">");
        sel(sh, "A2", 6, "Periode : " + periode + "   |   " + dasar);
        sh.append("</row>");

        // baris 4-5: kepala tabel
        sh.append("<row r=\"4\" ht=\"32\" customHeight=\"1\">");
        sel(sh, "A4", 2, "No.");
        sel(sh, "B4", 2, "Kelompok ICD-10");
        sel(sh, "C4", 2, "Kelompok Diagnosa Penyakit");
        sel(sh, "D4", 2, grup1);
        kosong(sh, "E4", 2);
        kosong(sh, "F4", 2);
        sel(sh, "G4", 2, grup2);
        kosong(sh, "H4", 2);
        kosong(sh, "I4", 2);
        sh.append("</row>");
        sh.append("<row r=\"5\" ht=\"18\" customHeight=\"1\">");
        kosong(sh, "A5", 2);
        kosong(sh, "B5", 2);
        kosong(sh, "C5", 2);
        for (int k = 0; k < 6; k++) {
            sel(sh, kol.charAt(3 + k) + "5", 2, SUB[k]);
        }
        sh.append("</row>");

        // data
        for (int i = 0; i < n; i++) {
            int r = rAwal + i;
            sh.append("<row r=\"").append(r).append("\">");
            num(sh, "A" + r, 5, i + 1);
            sel(sh, "B" + r, 5, kode[i]);
            sel(sh, "C" + r, 3, nama[i]);
            for (int k = 0; k < 6; k++) {
                num(sh, kol.charAt(3 + k) + "" + r, 4, angka[i][k]);
            }
            sh.append("</row>");
        }

        // baris jumlah
        sh.append("<row r=\"").append(rTotal).append("\">");
        sel(sh, "A" + rTotal, 7, "Jumlah");
        kosong(sh, "B" + rTotal, 7);
        kosong(sh, "C" + rTotal, 7);
        for (int k = 0; k < 6; k++) {
            int jml = 0;
            for (int i = 0; i < n; i++) {
                jml += angka[i][k];
            }
            String c = String.valueOf(kol.charAt(3 + k));
            if (n > 0) {
                rumus(sh, c + rTotal, 8, "SUM(" + c + rAwal + ":" + c + rAkhir + ")", jml);
            } else {
                num(sh, c + rTotal, 8, 0);
            }
        }
        sh.append("</row></sheetData>");

        sh.append("<mergeCells count=\"8\">")
                .append("<mergeCell ref=\"A1:I1\"/>")
                .append("<mergeCell ref=\"A2:I2\"/>")
                .append("<mergeCell ref=\"A4:A5\"/>")
                .append("<mergeCell ref=\"B4:B5\"/>")
                .append("<mergeCell ref=\"C4:C5\"/>")
                .append("<mergeCell ref=\"D4:F4\"/>")
                .append("<mergeCell ref=\"G4:I4\"/>")
                .append("<mergeCell ref=\"A").append(rTotal).append(":C").append(rTotal).append("\"/>")
                .append("</mergeCells>");
        sh.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.6\" bottom=\"0.6\" header=\"0.3\" footer=\"0.3\"/>");
        sh.append("<pageSetup paperSize=\"9\" orientation=\"landscape\" fitToWidth=\"1\" fitToHeight=\"0\"/>");
        sh.append("</worksheet>");

        String border = "<border><left style=\"thin\"><color rgb=\"FF808080\"/></left><right style=\"thin\"><color rgb=\"FF808080\"/></right>"
                + "<top style=\"thin\"><color rgb=\"FF808080\"/></top><bottom style=\"thin\"><color rgb=\"FF808080\"/></bottom><diagonal/></border>";
        String styles = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<fonts count=\"4\">"
                + "<font><sz val=\"10\"/><name val=\"Arial\"/></font>"
                + "<font><b/><sz val=\"14\"/><name val=\"Arial\"/></font>"
                + "<font><b/><sz val=\"10\"/><name val=\"Arial\"/></font>"
                + "<font><sz val=\"10\"/><color rgb=\"FF555555\"/><name val=\"Arial\"/></font>"
                + "</fonts>"
                + "<fills count=\"4\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFDCE6F1\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFF2F2F2\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border>" + border + "</borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"9\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
                + "</cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";

        tulisPaket(f, sh.toString(), styles);
    }

    private static void tulisPaket(File f, String sheetXml, String stylesXml) throws IOException {
        String contentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "</Types>";
        String relsRoot = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
        String workbook = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"Rekap 10 Besar\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";
        String relsWb = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>";

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(f))) {
            tambah(zip, "[Content_Types].xml", contentTypes);
            tambah(zip, "_rels/.rels", relsRoot);
            tambah(zip, "xl/workbook.xml", workbook);
            tambah(zip, "xl/_rels/workbook.xml.rels", relsWb);
            tambah(zip, "xl/styles.xml", stylesXml);
            tambah(zip, "xl/worksheets/sheet1.xml", sheetXml);
        }
    }

    private static void tambah(ZipOutputStream zip, String nama, String isi) throws IOException {
        zip.putNextEntry(new ZipEntry(nama));
        zip.write(isi.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static void sel(StringBuilder sb, String ref, int s, String teks) {
        sb.append("<c r=\"").append(ref).append("\" s=\"").append(s)
                .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">").append(xml(teks)).append("</t></is></c>");
    }

    private static void kosong(StringBuilder sb, String ref, int s) {
        sb.append("<c r=\"").append(ref).append("\" s=\"").append(s).append("\"/>");
    }

    private static void num(StringBuilder sb, String ref, int s, int v) {
        sb.append("<c r=\"").append(ref).append("\" s=\"").append(s).append("\"><v>").append(v).append("</v></c>");
    }

    private static void rumus(StringBuilder sb, String ref, int s, String rumus, int nilai) {
        sb.append("<c r=\"").append(ref).append("\" s=\"").append(s).append("\"><f>").append(rumus)
                .append("</f><v>").append(nilai).append("</v></c>");
    }

    private static String xml(String t) {
        if (t == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(t.length() + 8);
        for (int i = 0; i < t.length(); i++) {
            char ch = t.charAt(i);
            switch (ch) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    if (ch >= 0x20 || ch == '\t' || ch == '\n') {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    // ==================================================================
    //  PDF (A4 landscape, font standar Helvetica, tanpa library)
    // ==================================================================
    private static final float PW = 842f, PH = 595f, M = 36f;
    private static final float W_NO = 30f, W_KODE = 58f, W_NUM = 56f;
    private static final float W_DIAG = PW - 2 * M - W_NO - W_KODE - 6 * W_NUM;
    private static final float H_GRP = 26f, H_SUB = 16f;
    private static final float[] X = new float[10];

    static {
        X[0] = M;
        X[1] = X[0] + W_NO;
        X[2] = X[1] + W_KODE;
        X[3] = X[2] + W_DIAG;
        for (int k = 4; k <= 9; k++) {
            X[k] = X[k - 1] + W_NUM;
        }
    }

    // lebar karakter Helvetica (per 1000 unit), ASCII 32..126
    private static final int[] LEBAR = {
        278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
        1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
        333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
        556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584};
    // lebar karakter Helvetica-Bold
    private static final int[] LEBAR_B = {
        278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 333, 333, 584, 584, 584, 611,
        975, 722, 722, 722, 722, 667, 611, 778, 722, 278, 556, 722, 611, 833, 722, 778,
        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 333, 278, 333, 584, 556,
        333, 556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611,
        611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500, 389, 280, 389, 584};

    static void tulisPdf(File f, String judul, String periode, String dasar, String grup1, String grup2,
            String[] kode, String[] nama, int[][] angka) throws IOException {
        int n = kode.length;
        List<StringBuilder> hal = new ArrayList<>();
        StringBuilder c = new StringBuilder();
        hal.add(c);
        float top = M;

        teks(c, true, 14, M, top + 12, judul);
        teks(c, false, 9, M, top + 28, "Periode : " + periode + "   |   " + dasar);
        top += 42;
        top = kepala(c, top, grup1, grup2);

        int[] jml = new int[6];
        for (int i = 0; i < n; i++) {
            List<String> ln = bungkus(nama[i], false, 9f, W_DIAG - 10f, 2);
            float h = ln.size() > 1 ? 28f : 20f;
            if (top + h > PH - M - 24f) {
                c = new StringBuilder();
                hal.add(c);
                top = kepala(c, M, grup1, grup2);
            }
            kotak(c, X[0], top, W_NO, h, -1f);
            kotak(c, X[1], top, W_KODE, h, -1f);
            kotak(c, X[2], top, W_DIAG, h, -1f);
            for (int k = 0; k < 6; k++) {
                kotak(c, X[3 + k], top, W_NUM, h, -1f);
            }
            float tengahV = top + h / 2f + 3f;
            tengah(c, false, 9f, X[0], W_NO, tengahV, String.valueOf(i + 1));
            tengah(c, true, 9f, X[1], W_KODE, tengahV, kode[i]);
            if (ln.size() > 1) {
                teks(c, false, 9f, X[2] + 5f, top + 11f, ln.get(0));
                teks(c, false, 9f, X[2] + 5f, top + 22f, ln.get(1));
            } else {
                teks(c, false, 9f, X[2] + 5f, tengahV, ln.get(0));
            }
            for (int k = 0; k < 6; k++) {
                jml[k] += angka[i][k];
                tengah(c, k == 2 || k == 5, 9f, X[3 + k], W_NUM, tengahV, String.valueOf(angka[i][k]));
            }
            top += h;
        }

        // baris jumlah
        if (top + 20f > PH - M - 24f) {
            c = new StringBuilder();
            hal.add(c);
            top = M;
        }
        kotak(c, X[0], top, W_NO + W_KODE + W_DIAG, 20f, 0.93f);
        for (int k = 0; k < 6; k++) {
            kotak(c, X[3 + k], top, W_NUM, 20f, 0.93f);
        }
        teks(c, true, 9f, X[0] + 6f, top + 13.5f, "Jumlah");
        for (int k = 0; k < 6; k++) {
            tengah(c, true, 9f, X[3 + k], W_NUM, top + 13.5f, String.valueOf(jml[k]));
        }

        // footer tiap halaman
        String cetak = "Dicetak : " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
        for (int p = 0; p < hal.size(); p++) {
            StringBuilder pg = hal.get(p);
            teks(pg, false, 7.5f, M, PH - 18f, cetak);
            String h = "Halaman " + (p + 1) + " / " + hal.size();
            teks(pg, false, 7.5f, PW - M - lebarTeks(h, false, 7.5f), PH - 18f, h);
        }

        simpanPdf(f, hal);
    }

    private static void simpanPdf(File f, List<StringBuilder> hal) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int totalObj = 4 + 2 * hal.size();
        int[] off = new int[totalObj + 1];
        tulis(out, "%PDF-1.4\n");
        off[1] = out.size();
        tulis(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        StringBuilder kids = new StringBuilder();
        for (int p = 0; p < hal.size(); p++) {
            kids.append(5 + 2 * p).append(" 0 R ");
        }
        off[2] = out.size();
        tulis(out, "2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count " + hal.size() + " >>\nendobj\n");
        off[3] = out.size();
        tulis(out, "3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n");
        off[4] = out.size();
        tulis(out, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n");
        for (int p = 0; p < hal.size(); p++) {
            int po = 5 + 2 * p, co = 6 + 2 * p;
            off[po] = out.size();
            tulis(out, po + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + (int) PW + " " + (int) PH
                    + "] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + co + " 0 R >>\nendobj\n");
            byte[] isi = hal.get(p).toString().getBytes(StandardCharsets.ISO_8859_1);
            off[co] = out.size();
            tulis(out, co + " 0 obj\n<< /Length " + isi.length + " >>\nstream\n");
            out.write(isi, 0, isi.length);
            tulis(out, "\nendstream\nendobj\n");
        }
        int xref = out.size();
        StringBuilder xr = new StringBuilder();
        xr.append("xref\n0 ").append(totalObj + 1).append("\n0000000000 65535 f \n");
        for (int i = 1; i <= totalObj; i++) {
            xr.append(String.format(Locale.US, "%010d 00000 n \n", off[i]));
        }
        xr.append("trailer\n<< /Size ").append(totalObj + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(xref).append("\n%%EOF\n");
        tulis(out, xr.toString());

        try (FileOutputStream fos = new FileOutputStream(f)) {
            out.writeTo(fos);
        }
    }

    private static float kepala(StringBuilder c, float top, String grup1, String grup2) {
        final float abu = 0.86f;
        float tinggi = H_GRP + H_SUB;
        kotak(c, X[0], top, W_NO, tinggi, abu);
        kotak(c, X[1], top, W_KODE, tinggi, abu);
        kotak(c, X[2], top, W_DIAG, tinggi, abu);
        kotak(c, X[3], top, 3 * W_NUM, H_GRP, abu);
        kotak(c, X[6], top, 3 * W_NUM, H_GRP, abu);
        for (int k = 0; k < 6; k++) {
            kotak(c, X[3 + k], top + H_GRP, W_NUM, H_SUB, abu);
        }
        float tv = top + tinggi / 2f;
        tengah(c, true, 9f, X[0], W_NO, tv + 3f, "No.");
        tengah(c, true, 8f, X[1], W_KODE, tv - 1f, "Kelompok");
        tengah(c, true, 8f, X[1], W_KODE, tv + 9f, "ICD-10");
        tengah(c, true, 9f, X[2], W_DIAG, tv + 3f, "Kelompok Diagnosa Penyakit");
        teksGrup(c, grup1, X[3], top);
        teksGrup(c, grup2, X[6], top);
        for (int k = 0; k < 6; k++) {
            tengah(c, true, 8f, X[3 + k], W_NUM, top + H_GRP + 11.5f, SUB[k]);
        }
        return top + tinggi;
    }

    private static void teksGrup(StringBuilder c, String s, float x, float top) {
        List<String> ln = bungkus(s, true, 8f, 3 * W_NUM - 8f, 2);
        if (ln.size() == 1) {
            tengah(c, true, 8f, x, 3 * W_NUM, top + H_GRP / 2f + 3f, ln.get(0));
        } else {
            tengah(c, true, 8f, x, 3 * W_NUM, top + 11f, ln.get(0));
            tengah(c, true, 8f, x, 3 * W_NUM, top + 21f, ln.get(1));
        }
    }

    private static void kotak(StringBuilder c, float x, float top, float w, float h, float abu) {
        float y = PH - top - h;
        if (abu >= 0f) {
            c.append(n(abu)).append(" g ").append(n(x)).append(' ').append(n(y)).append(' ')
                    .append(n(w)).append(' ').append(n(h)).append(" re f 0 g\n");
        }
        c.append("0.5 w 0.45 G ").append(n(x)).append(' ').append(n(y)).append(' ')
                .append(n(w)).append(' ').append(n(h)).append(" re S\n");
    }

    private static void teks(StringBuilder c, boolean tebal, float ukuran, float x, float baseTop, String s) {
        c.append("BT /").append(tebal ? "F2" : "F1").append(' ').append(n(ukuran)).append(" Tf ")
                .append(n(x)).append(' ').append(n(PH - baseTop)).append(" Td (")
                .append(esc(s)).append(") Tj ET\n");
    }

    private static void tengah(StringBuilder c, boolean tebal, float ukuran, float xKiri, float lebar,
            float baseTop, String s) {
        teks(c, tebal, ukuran, xKiri + (lebar - lebarTeks(s, tebal, ukuran)) / 2f, baseTop, s);
    }

    private static float lebarTeks(String s, boolean tebal, float ukuran) {
        int[] t = tebal ? LEBAR_B : LEBAR;
        int total = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            total += (ch >= 32 && ch <= 126) ? t[ch - 32] : 556;
        }
        return total * ukuran / 1000f;
    }

    private static String potong(String s, boolean tebal, float ukuran, float maxW) {
        if (lebarTeks(s, tebal, ukuran) <= maxW) {
            return s;
        }
        String t = s;
        while (t.length() > 0 && lebarTeks(t + "...", tebal, ukuran) > maxW) {
            t = t.substring(0, t.length() - 1);
        }
        return t.trim() + "...";
    }

    /** Bagi teks per kata menjadi paling banyak maxBaris baris; sisa yang tidak muat dipotong dengan "...". */
    private static List<String> bungkus(String s, boolean tebal, float ukuran, float maxW, int maxBaris) {
        List<String> out = new ArrayList<>();
        String[] kata = (s == null || s.trim().isEmpty()) ? new String[0] : s.trim().split("\\s+");
        String cur = "";
        int i = 0;
        for (; i < kata.length; i++) {
            String coba = cur.isEmpty() ? kata[i] : cur + " " + kata[i];
            if (cur.isEmpty() || lebarTeks(coba, tebal, ukuran) <= maxW) {
                cur = coba;
            } else {
                if (out.size() == maxBaris - 1) {
                    break;
                }
                out.add(cur);
                cur = kata[i];
            }
        }
        if (i < kata.length) {
            StringBuilder sisa = new StringBuilder(cur);
            for (; i < kata.length; i++) {
                sisa.append(' ').append(kata[i]);
            }
            cur = sisa.toString();
        }
        out.add(cur);
        for (int k = 0; k < out.size(); k++) {
            out.set(k, potong(out.get(k), tebal, ukuran, maxW));
        }
        return out;
    }

    private static String n(float v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private static String esc(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\\' || ch == '(' || ch == ')') {
                sb.append('\\').append(ch);
            } else if (ch < 32 || ch > 255) {
                sb.append('?');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static void tulis(ByteArrayOutputStream out, String s) {
        byte[] b = s.getBytes(StandardCharsets.ISO_8859_1);
        out.write(b, 0, b.length);
    }

    // ==================================================================
    //  FORMAT 10 BESAR PENYAKIT RAWAT JALAN
    //  NO | KODE ICD 10 | DISKRIPSI | KASUS BARU (LAKI, PEREMPUAN) | JUMLAH KASUS | JUMLAH KUNJUNGAN
    // ==================================================================
    static void tulisXlsxRalan(File f, String judul, String periode, String[] kode, String[] nama,
            int[] lk, int[] pr, int[] kunjungan) throws IOException {
        final String kol = "ABCDEFG";
        int n = kode.length;
        int rAwal = 7;

        StringBuilder sh = new StringBuilder();
        sh.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sh.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        sh.append("<sheetPr><pageSetUpPr fitToPage=\"1\"/></sheetPr>");
        sh.append("<sheetViews><sheetView showGridLines=\"0\" workbookViewId=\"0\"/></sheetViews>");
        sh.append("<sheetFormatPr defaultRowHeight=\"15\"/>");
        sh.append("<cols>")
                .append("<col min=\"1\" max=\"1\" width=\"6\" customWidth=\"1\"/>")
                .append("<col min=\"2\" max=\"2\" width=\"20\" customWidth=\"1\"/>")
                .append("<col min=\"3\" max=\"3\" width=\"58\" customWidth=\"1\"/>")
                .append("<col min=\"4\" max=\"4\" width=\"12\" customWidth=\"1\"/>")
                .append("<col min=\"5\" max=\"5\" width=\"14\" customWidth=\"1\"/>")
                .append("<col min=\"6\" max=\"6\" width=\"14\" customWidth=\"1\"/>")
                .append("<col min=\"7\" max=\"7\" width=\"17\" customWidth=\"1\"/>")
                .append("</cols><sheetData>");

        sh.append("<row r=\"1\" ht=\"22\" customHeight=\"1\">");
        sel(sh, "A1", 1, judul);
        sh.append("</row><row r=\"2\" ht=\"20\" customHeight=\"1\">");
        sel(sh, "A2", 1, periode);
        sh.append("</row>");

        sh.append("<row r=\"4\" ht=\"30\" customHeight=\"1\">");
        sel(sh, "A4", 2, "NO");
        sel(sh, "B4", 2, "KODE ICD 10");
        sel(sh, "C4", 2, "DISKRIPSI");
        sel(sh, "D4", 2, "KASUS BARU MENURUT JENIS KELAMIN");
        kosong(sh, "E4", 2);
        sel(sh, "F4", 2, "JUMLAH KASUS");
        sel(sh, "G4", 2, "JUMLAH KUNJUNGAN");
        sh.append("</row><row r=\"5\" ht=\"18\" customHeight=\"1\">");
        kosong(sh, "A5", 2);
        kosong(sh, "B5", 2);
        kosong(sh, "C5", 2);
        sel(sh, "D5", 2, "LAKI");
        sel(sh, "E5", 2, "PEREMPUAN");
        kosong(sh, "F5", 2);
        kosong(sh, "G5", 2);
        sh.append("</row><row r=\"6\" ht=\"16\" customHeight=\"1\">");
        for (int k = 0; k < 7; k++) {
            num(sh, kol.charAt(k) + "6", 3, k + 1);
        }
        sh.append("</row>");

        for (int i = 0; i < n; i++) {
            int r = rAwal + i;
            sh.append("<row r=\"").append(r).append("\" ht=\"30\" customHeight=\"1\">");
            num(sh, "A" + r, 4, i + 1);
            sel(sh, "B" + r, 5, kode[i]);
            sel(sh, "C" + r, 5, nama[i]);
            num(sh, "D" + r, 6, lk[i]);
            num(sh, "E" + r, 6, pr[i]);
            rumus(sh, "F" + r, 6, "D" + r + "+E" + r, lk[i] + pr[i]);
            num(sh, "G" + r, 6, kunjungan[i]);
            sh.append("</row>");
        }
        sh.append("</sheetData>");
        sh.append("<mergeCells count=\"8\">")
                .append("<mergeCell ref=\"A1:G1\"/><mergeCell ref=\"A2:G2\"/>")
                .append("<mergeCell ref=\"A4:A5\"/><mergeCell ref=\"B4:B5\"/><mergeCell ref=\"C4:C5\"/>")
                .append("<mergeCell ref=\"D4:E4\"/><mergeCell ref=\"F4:F5\"/><mergeCell ref=\"G4:G5\"/>")
                .append("</mergeCells>");
        sh.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.6\" bottom=\"0.6\" header=\"0.3\" footer=\"0.3\"/>");
        sh.append("<pageSetup paperSize=\"9\" orientation=\"landscape\" fitToWidth=\"1\" fitToHeight=\"0\"/>");
        sh.append("</worksheet>");

        String hitam = "<border><left style=\"thin\"><color rgb=\"FF000000\"/></left><right style=\"thin\"><color rgb=\"FF000000\"/></right>"
                + "<top style=\"thin\"><color rgb=\"FF000000\"/></top><bottom style=\"thin\"><color rgb=\"FF000000\"/></bottom><diagonal/></border>";
        String styles = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<fonts count=\"3\">"
                + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"12\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "</fonts>"
                + "<fills count=\"3\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFBFBFBF\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border>" + hitam + "</borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"7\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"3\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>"
                + "</cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";
        tulisPaket(f, sh.toString(), styles);
    }

    static void tulisPdfRalan(File f, String judul, String periode, String[] kode, String[] nama,
            int[] lk, int[] pr, int[] kunjungan) throws IOException {
        final float wNo = 32f, wKode = 110f, wLk = 70f, wPr = 82f, wKasus = 80f, wKunj = 92f;
        final float wDiag = PW - 2 * M - wNo - wKode - wLk - wPr - wKasus - wKunj;
        final float[] x = new float[8];
        x[0] = M;
        x[1] = x[0] + wNo;
        x[2] = x[1] + wKode;
        x[3] = x[2] + wDiag;
        x[4] = x[3] + wLk;
        x[5] = x[4] + wPr;
        x[6] = x[5] + wKasus;
        x[7] = x[6] + wKunj;
        int n = kode.length;

        List<StringBuilder> hal = new ArrayList<>();
        StringBuilder c = new StringBuilder();
        hal.add(c);
        float top = M;
        tengah(c, true, 13f, 0f, PW, top + 12f, judul);
        tengah(c, true, 11f, 0f, PW, top + 30f, periode);
        top += 46f;
        top = kepalaRalan(c, top, x);

        for (int i = 0; i < n; i++) {
            List<String> ln = bungkus(nama[i], false, 9f, wDiag - 12f, 2);
            List<String> lk2 = bungkus(kode[i], false, 9f, wKode - 12f, 2);
            float h = (ln.size() > 1 || lk2.size() > 1) ? 30f : 22f;
            if (top + h > PH - M - 24f) {
                c = new StringBuilder();
                hal.add(c);
                top = kepalaRalan(c, M, x);
            }
            for (int k = 0; k < 7; k++) {
                kotakHitam(c, x[k], top, x[k + 1] - x[k], h, -1f);
            }
            float tv = top + h / 2f + 3f;
            tengah(c, false, 9f, x[0], wNo, tv, String.valueOf(i + 1));
            tulisBaris(c, lk2, x[1] + 6f, top, h);
            tulisBaris(c, ln, x[2] + 6f, top, h);
            int[] nil = {lk[i], pr[i], lk[i] + pr[i], kunjungan[i]};
            for (int k = 0; k < 4; k++) {
                String t = ribuan(nil[k]);
                teks(c, false, 9f, x[4 + k] - 6f - lebarTeks(t, false, 9f), tv, t);
            }
            top += h;
        }

        String cetak = "Dicetak : " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
        for (int p = 0; p < hal.size(); p++) {
            StringBuilder pg = hal.get(p);
            teks(pg, false, 7.5f, M, PH - 18f, cetak);
            String h = "Halaman " + (p + 1) + " / " + hal.size();
            teks(pg, false, 7.5f, PW - M - lebarTeks(h, false, 7.5f), PH - 18f, h);
        }
        simpanPdf(f, hal);
    }

    private static void tulisBaris(StringBuilder c, List<String> ln, float xKiri, float top, float h) {
        if (ln.size() > 1) {
            teks(c, false, 9f, xKiri, top + 12f, ln.get(0));
            teks(c, false, 9f, xKiri, top + 24f, ln.get(1));
        } else {
            teks(c, false, 9f, xKiri, top + h / 2f + 3f, ln.get(0));
        }
    }

    private static float kepalaRalan(StringBuilder c, float top, float[] x) {
        final float hGrp = 24f, hSub = 16f, hNom = 14f;
        float tinggi = hGrp + hSub;
        float wLk = x[4] - x[3], wPr = x[5] - x[4], wKasus = x[6] - x[5], wKunj = x[7] - x[6];
        kotakHitam(c, x[0], top, x[1] - x[0], tinggi, -1f);
        kotakHitam(c, x[1], top, x[2] - x[1], tinggi, -1f);
        kotakHitam(c, x[2], top, x[3] - x[2], tinggi, -1f);
        kotakHitam(c, x[3], top, wLk + wPr, hGrp, -1f);
        kotakHitam(c, x[3], top + hGrp, wLk, hSub, -1f);
        kotakHitam(c, x[4], top + hGrp, wPr, hSub, -1f);
        kotakHitam(c, x[5], top, wKasus, tinggi, -1f);
        kotakHitam(c, x[6], top, wKunj, tinggi, -1f);
        float tv = top + tinggi / 2f;
        tengah(c, true, 8.5f, x[0], x[1] - x[0], tv + 3f, "NO");
        tengah(c, true, 8.5f, x[1], x[2] - x[1], tv + 3f, "KODE ICD 10");
        tengah(c, true, 8.5f, x[2], x[3] - x[2], tv + 3f, "DISKRIPSI");
        List<String> g = bungkus("KASUS BARU MENURUT JENIS KELAMIN", true, 8.5f, wLk + wPr - 8f, 2);
        if (g.size() == 1) {
            tengah(c, true, 8.5f, x[3], wLk + wPr, top + hGrp / 2f + 3f, g.get(0));
        } else {
            tengah(c, true, 8.5f, x[3], wLk + wPr, top + 10f, g.get(0));
            tengah(c, true, 8.5f, x[3], wLk + wPr, top + 20f, g.get(1));
        }
        tengah(c, true, 8.5f, x[3], wLk, top + hGrp + 11.5f, "LAKI");
        tengah(c, true, 8.5f, x[4], wPr, top + hGrp + 11.5f, "PEREMPUAN");
        tengah(c, true, 8.5f, x[5], wKasus, tv - 1f, "JUMLAH");
        tengah(c, true, 8.5f, x[5], wKasus, tv + 9f, "KASUS");
        tengah(c, true, 8.5f, x[6], wKunj, tv - 1f, "JUMLAH");
        tengah(c, true, 8.5f, x[6], wKunj, tv + 9f, "KUNJUNGAN");
        // baris nomor kolom 1..7 (abu-abu)
        for (int k = 0; k < 7; k++) {
            kotakHitam(c, x[k], top + tinggi, x[k + 1] - x[k], hNom, 0.72f);
            tengah(c, true, 8f, x[k], x[k + 1] - x[k], top + tinggi + 10.5f, String.valueOf(k + 1));
        }
        return top + tinggi + hNom;
    }

    private static void kotakHitam(StringBuilder c, float x, float top, float w, float h, float abu) {
        float y = PH - top - h;
        if (abu >= 0f) {
            c.append(n(abu)).append(" g ").append(n(x)).append(' ').append(n(y)).append(' ')
                    .append(n(w)).append(' ').append(n(h)).append(" re f 0 g\n");
        }
        c.append("0.7 w 0 G ").append(n(x)).append(' ').append(n(y)).append(' ')
                .append(n(w)).append(' ').append(n(h)).append(" re S\n");
    }

    /** Angka dengan pemisah ribuan titik, mis. 1272 -> "1.272". */
    static String ribuan(long v) {
        String s = Long.toString(Math.abs(v));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) {
                sb.append('.');
            }
            sb.append(s.charAt(i));
        }
        return (v < 0 ? "-" : "") + sb;
    }
}
