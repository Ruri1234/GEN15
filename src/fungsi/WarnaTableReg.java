package fungsi;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class WarnaTableReg extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column
        );

        // ===== WARNA DASAR BARIS =====
        if (row % 2 == 1) {
            c.setBackground(new Color(225, 235, 249));
        } else {
            c.setBackground(Color.WHITE);
        }
        c.setForeground(Color.BLACK);

        // ===== AMBIL DATA STATUS =====
        Object sepObj    = table.getValueAt(row, 24); // Status SEP
        Object berkasObj = table.getValueAt(row, 25); // Status Berkas

        String statusSEP    = sepObj == null ? "" : sepObj.toString().trim();
        String statusBerkas = berkasObj == null ? "" : berkasObj.toString().trim();

        boolean berkasKosong =
                statusBerkas.isEmpty()
                || statusBerkas.equals("-")
                || statusBerkas.equalsIgnoreCase("null");

        // 🟧 NAMA PASIEN ORANGE jika SEP = Belum Terbit
        if (column == 8 && statusSEP.equalsIgnoreCase("Belum Terbit")) {
            c.setBackground(new Color(222, 188, 87));
            c.setForeground(Color.BLACK);
        }

        // ⬜⬜⬜ NO.RAWAT ABU-ABU jika Status Berkas kosong
        if (column == 2 && berkasKosong) {
            c.setBackground(new Color(68, 68, 68));
            c.setForeground(Color.WHITE);
        }

        // ===== SELECT PRIORITAS =====
        if (isSelected) {
            c.setBackground(new Color(80, 129, 227));
            c.setForeground(Color.WHITE);
        }

        return c;
    }
}