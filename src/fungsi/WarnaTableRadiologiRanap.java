/*
 * Kontribusi
 * SIMRS RSU Bethesda Serukam.
 */

package fungsi;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Owner
 */
public class WarnaTableRadiologiRanap extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (row % 2 == 1){
            component.setBackground(new Color(225,235,249));
            component.setForeground(new Color(50,50,50));
        }else{
            component.setBackground(new Color(255,255,255));
            component.setForeground(new Color(50,50,50)); 
        
        }
        // Ambil nilai dari kolom 14 dan 16
        Object jenisValue = table.getValueAt(row, 14);
        Object statusValue = table.getValueAt(row, 16);

        boolean isCito = jenisValue != null && jenisValue.toString().equalsIgnoreCase("CITO");
        boolean isReguler = statusValue != null && statusValue.toString().equalsIgnoreCase("REGULER");

        // Aturan prioritas pewarnaan
        if (isCito) {
            component.setBackground(new Color(231, 160, 156)); // Warna untuk CITO
        } else if (isReguler) {
            component.setBackground(new Color(175, 225, 195)); // Warna untuk REGULER
        }

        // Jika baris sedang dipilih
        if (isSelected) {
            component.setBackground(new Color(80, 129, 227)); // Warna biru seleksi
            component.setForeground(Color.WHITE);
        }

        return component;
    }
}            