package fungsi;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class WarnaTableKasirRalan extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Component component = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column
        );

        // Warna dasar baris ganjil/genap
        if (row % 2 == 1) {
            component.setBackground(new Color(225, 235, 249));
            component.setForeground(new Color(50, 50, 50));
        } else {
            component.setBackground(new Color(255, 255, 255));
            component.setForeground(new Color(50, 50, 50));
        } 

        // Warna kondisi "Batal" dan "REGULER"
        try {
            if (table.getValueAt(row, 10).toString().equals("Batal")) {
                component.setBackground(new Color(235, 141, 134));
                component.setForeground(new Color(50, 50, 50));
            } else if (table.getValueAt(row, 9).toString().equals("REGULER")) {
                component.setBackground(new Color(175, 225, 195));
                component.setForeground(new Color(50, 50, 50));
        }
        } catch (Exception e) {
            // abaikan kalau kolom tidak ada
        }

        // 🧡 Warnai kolom No.RM jika kolom Resep Dokter = "Belum Ada Resep"
        try {
            int kolomResep = table.getColumnModel().getColumnIndex("Resep Dokter");
            int kolomNoRM = 2; // indeks kolom No.RM

            String nilaiResep = table.getValueAt(row, kolomResep).toString();

            if (nilaiResep.equalsIgnoreCase("Belum Ada Resep") && column == kolomNoRM) {
                component.setBackground(new Color(255, 200, 140));
                component.setForeground(Color.BLACK);
    }
        } catch (Exception e) {
            // abaikan error jika kolom tidak ditemukan
        }

        // 💖 Tambahan: Warnai kolom Nama Pasien jika status antrean = "Mobile JKN"
       try {
    int kolomAntrean = table.getColumnModel().getColumnIndex("Antrean");
    int kolomPasien  = table.getColumnModel().getColumnIndex("Pasien");

    String nilaiAntrean = table.getValueAt(row, kolomAntrean).toString();

    if (nilaiAntrean.equalsIgnoreCase("Mobile JKN") && column == kolomPasien) {
        component.setBackground(new Color(207, 148, 227));
        component.setForeground(Color.BLACK);
}
        } catch (Exception e) {
            // abaikan error jika kolom tidak ditemukan
        }

        // Warna saat baris dipilih (prioritas tertinggi)
        if (isSelected) {
            component.setBackground(new Color(80, 129, 227));
            component.setForeground(Color.WHITE);
        }

        return component;
    }
}
