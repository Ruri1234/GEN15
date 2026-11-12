package laporan;

/**
 * Aturan bersama untuk rekap kunjungan rawat jalan (dipakai DlgKunjunganRalan dan DlgRL4B).
 * Ubah daftar poli yang tidak ditampilkan CUKUP di sini.
 */
public final class KunjunganRalanAturan {

    private KunjunganRalanAturan() {
    }

    /** reg_periksa.kd_poli untuk IGD. */
    public static final String KD_POLI_IGD = "IGDK";

    /**
     * Daftar poliklinik.nm_poli yang TIDAK ditampilkan (pola LIKE).
     * Tanpa tanda % = harus sama persis (tidak peka huruf besar/kecil).
     */
    public static final String[] POLI_DIKECUALIKAN = {
        "%apotek%", "%apotik%",
        "Unit Laboratorium",
        "Medical Check Up",
        "MCU AKPER/KARYAWAN",
        "POLI NEONATAL",
        "PROMO KEMERDEKAAN",
        "POLI MATA OUTREACH",
        "PROMO LIBUR SEKOLAH",
        "UNIT RADIOLOGI"
    };

    /**
     * Potongan SQL siap pakai, mis. " and poliklinik.nm_poli not like '%apotek%' and ...".
     * Berupa literal (tanpa tanda tanya), jadi tidak menambah parameter PreparedStatement.
     * Nilai berasal dari konstanta di atas, bukan input pengguna; tanda petik tetap di-escape.
     */
    public static String sqlKecualiPoli() {
        StringBuilder sb = new StringBuilder();
        for (String poli : POLI_DIKECUALIKAN) {
            sb.append(" and poliklinik.nm_poli not like '")
              .append(poli.replace("\\", "\\\\").replace("'", "''"))
              .append("'");
        }
        return sb.toString();
    }

    /**
     * Awalan kode penyakit (penyakit.kd_penyakit) yang TIDAK dimasukkan ke rekap 10 besar diagnosa
     * (tombol Rekap di DlgKunjunganRalan). Tidak dipakai laporan lain.
     */
    public static final String[] DIAGNOSA_REKAP_DIKECUALIKAN = {"KG", "Z", "O", "R"};

    /**
     * Potongan SQL untuk rekap diagnosa, mis. " and diagnosa_pasien.kd_penyakit not like 'KG%' and ...".
     * Literal (tanpa tanda tanya), jadi tidak menambah parameter PreparedStatement.
     */
    public static String sqlKecualiDiagnosaRekap(String kolom) {
        StringBuilder sb = new StringBuilder();
        for (String awalan : DIAGNOSA_REKAP_DIKECUALIKAN) {
            sb.append(" and ").append(kolom).append(" not like '")
              .append(awalan.replace("\\", "\\\\").replace("'", "''"))
              .append("%'");
        }
        return sb.toString();
    }
}
