package fungsi;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class WarnaTableSoapRanap extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Warna default berdasarkan baris ganjil/genap
        if (row % 2 == 1) {
            component.setBackground(new Color(255, 244, 244)); // Warna merah muda pucat untuk baris ganjil
            component.setForeground(new Color(50, 50, 50));   // Warna teks abu-abu gelap
        } else {
            component.setBackground(new Color(255, 255, 255)); // Warna putih untuk baris genap
            component.setForeground(new Color(50, 50, 50));   // Warna teks abu-abu gelap
        }
        
        // Pengecekan kolom Dokter/Paramedis (kolom ke-25, index 24)
        if (table.getColumnCount() > 24) {
            Object pemeriksa = table.getValueAt(row, 23); // Kolom "Dokter/Paramedis" ada di index 24
            
            if (pemeriksa != null) {
                String dokterParamedis = pemeriksa.toString().toLowerCase();
                
                // Pengecekan apakah kolom tersebut mengandung "dr"
              if (dokterParamedis.contains("dr")) {
                    component.setBackground(new Color(129,210,169)); // Warna latar hijau untuk "dr"
                    component.setForeground(new Color(50, 50, 50)); // Warna teks hitam
                } else {
                    component.setBackground(new Color(203,163,211)); // Warna latar merah untuk yang bukan "dr"
                    component.setForeground(new Color(50, 50, 50)); // Warna teks putih agar kontras
                }
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
