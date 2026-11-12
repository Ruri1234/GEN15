package keuangan;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Penulis file PDF dan Excel (.xlsx) tanpa library tambahan.
 */
public final class KeuanganRekapExport {

    private KeuanganRekapExport() {}

    // =====================================================================
    //  PDF (A4 landscape, font Helvetica bawaan PDF)
    // =====================================================================
    private static final float[] HELV = {
        278,278,355,556,556,889,667,191,333,333,389,584,278,333,278,278,
        556,556,556,556,556,556,556,556,556,556,278,278,584,584,584,556,
        1015,667,667,722,722,667,611,778,722,278,500,667,556,833,722,778,
        667,778,722,667,611,722,667,944,667,667,611,278,278,278,469,556,
        333,556,556,500,556,556,278,556,556,222,222,500,222,833,556,556,
        556,556,333,500,278,556,500,722,500,500,500,334,260,334,584};

    private static float lebarTeks(String s, float fs, boolean tebal) {
        float w = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            w += (c >= 32 && c <= 126) ? HELV[c - 32] : 556;
        }
        return w * fs / 1000f * (tebal ? 1.07f : 1f);
    }

    private static List<String> bungkus(String teks, float lebar, float fs, boolean tebal) {
        List<String> baris = new ArrayList<>();
        if (teks == null) teks = "";
        teks = teks.replace('\r', ' ').replace('\n', ' ').trim();
        if (teks.isEmpty()) { baris.add(""); return baris; }
        StringBuilder cur = new StringBuilder();
        for (String kata : teks.split("\\s+")) {
            // pecah kata yang lebih panjang dari kolom
            while (lebarTeks(kata, fs, tebal) > lebar && kata.length() > 1) {
                int n = kata.length();
                while (n > 1 && lebarTeks(kata.substring(0, n), fs, tebal) > lebar) n--;
                if (cur.length() > 0) { baris.add(cur.toString()); cur.setLength(0); }
                baris.add(kata.substring(0, n));
                kata = kata.substring(n);
            }
            String coba = cur.length() == 0 ? kata : cur + " " + kata;
            if (lebarTeks(coba, fs, tebal) <= lebar) {
                cur.setLength(0);
                cur.append(coba);
            } else {
                baris.add(cur.toString());
                cur.setLength(0);
                cur.append(kata);
            }
        }
        if (cur.length() > 0) baris.add(cur.toString());
        return baris;
    }

    private static String pdfEsc(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 255) c = '?';
            if (c < 32) c = ' ';
            if (c == '(' || c == ')' || c == '\\') sb.append('\\').append(c);
            else if (c > 126) sb.append(String.format("\\%03o", (int) c));
            else sb.append(c);
        }
        return sb.toString();
    }

    private static final class Pdf {
        static final float PW = 842f, PH = 595f, M = 28f, FS = 7f, LH = 9f, PAD = 3.5f;
        final List<StringBuilder> halaman = new ArrayList<>();
        StringBuilder cur;
        float y;
        final String judul;
        final List<String> info;
        final String[] header;
        final float[] lebar;
        final float totalLebar;

        Pdf(String judul, List<String> info, String[] header, float[] lebar) {
            this.judul = judul; this.info = info; this.header = header; this.lebar = lebar;
            float t = 0; for (float l : lebar) t += l;
            totalLebar = t;
        }

        void teks(float x, float yy, boolean tebal, float fs, String s, String warna) {
            cur.append(warna).append(" rg BT /").append(tebal ? "F2" : "F1").append(' ').append(fs)
               .append(" Tf 1 0 0 1 ").append(x).append(' ').append(yy).append(" Td (")
               .append(pdfEsc(s)).append(") Tj ET\n");
        }

        void kotak(float x, float yy, float w, float h, String warna) {
            cur.append(warna).append(" rg ").append(x).append(' ').append(yy).append(' ')
               .append(w).append(' ').append(h).append(" re f\n");
        }

        void halamanBaru(boolean pertama) {
            cur = new StringBuilder();
            halaman.add(cur);
            y = PH - M;
            if (pertama) {
                kotak(M, y - 30, totalLebar, 30, "0.102 0.298 0.549");
                teks(M + 10, y - 20, true, 15, judul, "1 1 1");
                y -= 30 + 10;
                for (String s : info) {
                    teks(M, y - 8, false, 8.5f, s, "0.15 0.2 0.3");
                    y -= 12;
                }
                y -= 6;
            }
            barisHeader();
        }

        void barisHeader() {
            int maks = 1;
            List<List<String>> sel = new ArrayList<>();
            for (int i = 0; i < header.length; i++) {
                List<String> l = bungkus(header[i], lebar[i] - 2 * PAD, FS, true);
                sel.add(l); maks = Math.max(maks, l.size());
            }
            float h = maks * LH + 2 * PAD;
            kotak(M, y - h, totalLebar, h, "0.102 0.298 0.549");
            float x = M;
            for (int i = 0; i < header.length; i++) {
                List<String> l = sel.get(i);
                for (int k = 0; k < l.size(); k++) {
                    float tw = lebarTeks(l.get(k), FS, true);
                    teks(x + (lebar[i] - tw) / 2f, y - PAD - FS - k * LH, true, FS, l.get(k), "1 1 1");
                }
                x += lebar[i];
            }
            y -= h;
        }

        void barisData(Object[] row, int idx) {
            int maks = 1;
            List<List<String>> sel = new ArrayList<>();
            for (int i = 0; i < header.length; i++) {
                String v = i < row.length && row[i] != null ? row[i].toString() : "";
                List<String> l = bungkus(v, lebar[i] - 2 * PAD, FS, false);
                sel.add(l); maks = Math.max(maks, l.size());
            }
            float h = maks * LH + 2 * PAD;
            if (y - h < M + 16) halamanBaru(false);
            if (idx % 2 == 1) kotak(M, y - h, totalLebar, h, "0.95 0.962 0.985");
            float x = M;
            for (int i = 0; i < header.length; i++) {
                List<String> l = sel.get(i);
                boolean tengah = (i <= 2 || i == 4 || i == 5 || i == 6);
                for (int k = 0; k < l.size(); k++) {
                    float tx = tengah ? x + (lebar[i] - lebarTeks(l.get(k), FS, false)) / 2f : x + PAD;
                    teks(tx, y - PAD - FS - k * LH, false, FS, l.get(k), "0.12 0.16 0.24");
                }
                x += lebar[i];
            }
            cur.append("0.82 0.86 0.92 RG 0.5 w ").append(M).append(' ').append(y - h).append(" m ")
               .append(M + totalLebar).append(' ').append(y - h).append(" l S\n");
            y -= h;
        }
    }

    public static void simpanPdf(File f, String judul, List<String> info, String[] header,
            float[] lebar, List<Object[]> rows) throws IOException {
        Pdf p = new Pdf(judul, info, header, lebar);
        p.halamanBaru(true);
        int i = 0;
        for (Object[] r : rows) p.barisData(r, i++);

        String cetak = "Dicetak : " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
        int n = p.halaman.size();
        for (int k = 0; k < n; k++) {
            p.cur = p.halaman.get(k);
            p.teks(Pdf.M, 16, false, 7, cetak, "0.42 0.48 0.58");
            String h = "Halaman " + (k + 1) + " / " + n;
            p.teks(Pdf.PW - Pdf.M - lebarTeks(h, 7, false), 16, false, 7, h, "0.42 0.48 0.58");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offs = new ArrayList<>();
        tulis(out, "%PDF-1.4\n");
        offs.add(out.size());
        tulis(out, "1 0 obj\n<</Type/Catalog/Pages 2 0 R>>\nendobj\n");
        offs.add(out.size());
        StringBuilder kids = new StringBuilder();
        for (int k = 0; k < n; k++) kids.append(5 + 2 * k).append(" 0 R ");
        tulis(out, "2 0 obj\n<</Type/Pages/Kids [" + kids + "]/Count " + n + ">>\nendobj\n");
        offs.add(out.size());
        tulis(out, "3 0 obj\n<</Type/Font/Subtype/Type1/BaseFont/Helvetica/Encoding/WinAnsiEncoding>>\nendobj\n");
        offs.add(out.size());
        tulis(out, "4 0 obj\n<</Type/Font/Subtype/Type1/BaseFont/Helvetica-Bold/Encoding/WinAnsiEncoding>>\nendobj\n");
        for (int k = 0; k < n; k++) {
            int po = 5 + 2 * k, co = 6 + 2 * k;
            offs.add(out.size());
            tulis(out, po + " 0 obj\n<</Type/Page/Parent 2 0 R/MediaBox [0 0 " + (int) Pdf.PW + " " + (int) Pdf.PH
                    + "]/Resources <</Font <</F1 3 0 R/F2 4 0 R>>>>/Contents " + co + " 0 R>>\nendobj\n");
            byte[] isi = p.halaman.get(k).toString().getBytes(StandardCharsets.ISO_8859_1);
            offs.add(out.size());
            tulis(out, co + " 0 obj\n<</Length " + isi.length + ">>\nstream\n");
            out.write(isi);
            tulis(out, "\nendstream\nendobj\n");
        }
        int xref = out.size();
        tulis(out, "xref\n0 " + (offs.size() + 1) + "\n0000000000 65535 f \n");
        for (int o : offs) tulis(out, String.format("%010d 00000 n \n", o));
        tulis(out, "trailer\n<</Size " + (offs.size() + 1) + "/Root 1 0 R>>\nstartxref\n" + xref + "\n%%EOF\n");
        try (FileOutputStream fo = new FileOutputStream(f)) {
            fo.write(out.toByteArray());
        }
    }

    private static void tulis(ByteArrayOutputStream o, String s) throws IOException {
        o.write(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    // =====================================================================
    //  Excel (.xlsx)
    // =====================================================================
    public static void simpanXlsx(File f, String judul, List<String> info, String[] header,
            double[] lebarKolom, List<Object[]> rows,
            Map<String, ? extends Map<String, Integer>> ringkasan) throws IOException {
        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(f))) {
            isi(z, "[Content_Types].xml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet2.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "</Types>");
            isi(z, "_rels/.rels",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>");
            isi(z, "xl/workbook.xml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"Detail\" sheetId=\"1\" r:id=\"rId1\"/><sheet name=\"Ringkasan\" sheetId=\"2\" r:id=\"rId2\"/></sheets></workbook>");
            isi(z, "xl/_rels/workbook.xml.rels",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet2.xml\"/>"
                + "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>");
            isi(z, "xl/styles.xml", STYLES);

            // ---- sheet Detail
            StringBuilder sb = new StringBuilder();
            int r = 1;
            sb.append("<row r=\"").append(r).append("\" ht=\"24\" customHeight=\"1\">").append(sel(0, r, judul, 3)).append("</row>");
            r++;
            for (String s : info) {
                sb.append("<row r=\"").append(r).append("\">").append(sel(0, r, s, 0)).append("</row>");
                r++;
            }
            r++;
            int barisHeader = r;
            sb.append("<row r=\"").append(r).append("\" ht=\"26\" customHeight=\"1\">");
            for (int c = 0; c < header.length; c++) sb.append(sel(c, r, header[c], 1));
            sb.append("</row>");
            r++;
            for (Object[] row : rows) {
                sb.append("<row r=\"").append(r).append("\">");
                for (int c = 0; c < header.length; c++) {
                    Object v = c < row.length ? row[c] : "";
                    if (v instanceof Number) sb.append(num(c, r, ((Number) v).doubleValue(), 4));
                    else sb.append(sel(c, r, v == null ? "" : v.toString(), (c == 1 || c == 2 || c == 4 || c == 5 || c == 6) ? 4 : 2));
                }
                sb.append("</row>");
                r++;
            }
            StringBuilder cols = new StringBuilder("<cols>");
            for (int c = 0; c < lebarKolom.length; c++)
                cols.append("<col min=\"").append(c + 1).append("\" max=\"").append(c + 1)
                    .append("\" width=\"").append(lebarKolom[c]).append("\" customWidth=\"1\"/>");
            cols.append("</cols>");
            isi(z, "xl/worksheets/sheet1.xml", sheetXml(
                "<sheetViews><sheetView workbookViewId=\"0\" showGridLines=\"0\"><pane ySplit=\"" + barisHeader
                + "\" topLeftCell=\"A" + (barisHeader + 1) + "\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>",
                cols.toString(), sb.toString()));

            // ---- sheet Ringkasan
            sb = new StringBuilder();
            r = 1;
            sb.append("<row r=\"").append(r).append("\" ht=\"24\" customHeight=\"1\">").append(sel(0, r, "Ringkasan - " + judul, 3)).append("</row>");
            r += 2;
            for (Map.Entry<String, ? extends Map<String, Integer>> e : ringkasan.entrySet()) {
                sb.append("<row r=\"").append(r).append("\">").append(sel(0, r, e.getKey(), 1)).append(sel(1, r, "Jumlah", 1)).append("</row>");
                r++;
                for (Map.Entry<String, Integer> d : e.getValue().entrySet()) {
                    sb.append("<row r=\"").append(r).append("\">").append(sel(0, r, d.getKey(), 2)).append(num(1, r, d.getValue(), 4)).append("</row>");
                    r++;
                }
                r++;
            }
            isi(z, "xl/worksheets/sheet2.xml", sheetXml("<sheetViews><sheetView workbookViewId=\"0\" showGridLines=\"0\"/></sheetViews>",
                "<cols><col min=\"1\" max=\"1\" width=\"34\" customWidth=\"1\"/><col min=\"2\" max=\"2\" width=\"14\" customWidth=\"1\"/></cols>",
                sb.toString()));
        }
    }

    private static final String STYLES =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
        + "<fonts count=\"3\">"
        + "<font><sz val=\"10\"/><name val=\"Calibri\"/></font>"
        + "<font><b/><sz val=\"10\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>"
        + "<font><b/><sz val=\"14\"/><color rgb=\"FF1A4C8C\"/><name val=\"Calibri\"/></font>"
        + "</fonts>"
        + "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill>"
        + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1A4C8C\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>"
        + "<borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border>"
        + "<border><left style=\"thin\"><color rgb=\"FFCDD7E6\"/></left><right style=\"thin\"><color rgb=\"FFCDD7E6\"/></right>"
        + "<top style=\"thin\"><color rgb=\"FFCDD7E6\"/></top><bottom style=\"thin\"><color rgb=\"FFCDD7E6\"/></bottom><diagonal/></border></borders>"
        + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
        + "<cellXfs count=\"5\">"
        + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
        + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>"
        + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"top\" wrapText=\"1\"/></xf>"
        + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
        + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"top\"/></xf>"
        + "</cellXfs>"
        + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
        + "</styleSheet>";

    private static String sheetXml(String views, String cols, String data) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
            + views + cols + "<sheetData>" + data + "</sheetData></worksheet>";
    }

    private static void isi(ZipOutputStream z, String nama, String xml) throws IOException {
        z.putNextEntry(new ZipEntry(nama));
        z.write(xml.getBytes(StandardCharsets.UTF_8));
        z.closeEntry();
    }

    private static String kol(int c) {
        StringBuilder s = new StringBuilder();
        c++;
        while (c > 0) { int m = (c - 1) % 26; s.insert(0, (char) ('A' + m)); c = (c - 1) / 26; }
        return s.toString();
    }

    private static String sel(int c, int r, String v, int style) {
        return "<c r=\"" + kol(c) + r + "\" s=\"" + style + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">"
            + esc(v) + "</t></is></c>";
    }

    private static String num(int c, int r, double v, int style) {
        return "<c r=\"" + kol(c) + r + "\" s=\"" + style + "\"><v>" + (v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v)) + "</v></c>";
    }

    private static String esc(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&') sb.append("&amp;");
            else if (c == '<') sb.append("&lt;");
            else if (c == '>') sb.append("&gt;");
            else if (c == '"') sb.append("&quot;");
            else if (c < 32 && c != '\n' && c != '\t') sb.append(' ');
            else sb.append(c);
        }
        return sb.toString();
    }
}
