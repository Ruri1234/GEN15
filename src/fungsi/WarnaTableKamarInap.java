package fungsi;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class WarnaTableKamarInap extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        // Warna default berdasarkan baris ganjil/genap
        if (row % 2 == 1){
            component.setBackground(new Color(255, 244, 244)); // Warna merah muda pucat untuk baris ganjil
            component.setForeground(new Color(50, 50, 50));   // Warna teks abu-abu gelap
        } else {
            component.setBackground(new Color(255, 255, 255)); // Warna putih untuk baris genap
            component.setForeground(new Color(50, 50, 50));   // Warna teks abu-abu gelap
        }
        
        // Pengecekan jumlah kolom agar tidak terjadi out of bounds exception
        if (table.getColumnCount() > 20) {
            Object status = table.getValueAt(row, 20); // Kolom ke-21 (index 20)
            
            if (status != null && "Sudah Bayar".equals(status.toString())) {
                component.setBackground(new Color(50, 50, 50)); // Warna latar abu-abu gelap
                component.setForeground(new Color(255, 255, 255)); // Warna teks putih
            }
        }

        // Penanganan jika baris sedang dipilih
        if (isSelected) {
            component.setBackground(new Color(0, 120, 215)); // Warna biru untuk baris yang dipilih
            component.setForeground(Color.WHITE);           // Warna teks putih agar kontras
        }
        
        return component;
    }
}
