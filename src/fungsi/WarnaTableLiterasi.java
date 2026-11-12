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
public class WarnaTableLiterasi extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (row % 2 == 1){
            component.setBackground(new Color(225,235,249));
            component.setForeground(new Color(50,50,50));
        }else{
            component.setBackground(new Color(255,255,255));
            component.setForeground(new Color(50,50,50)); 
        } 
        if (table.getValueAt(row,16).toString().equals("Ya")){
            component.setBackground(new Color(251,197,141));
            component.setForeground(new Color(50,50,50));
        }
        else if(table.getValueAt(row,17).toString().equals("Kronis")){
            component.setBackground(new Color(166,77,121));
            component.setForeground(new Color(255,255,255));
        }
         else if(table.getValueAt(row,9).toString().equals("POLI DOTS")){
            component.setBackground(new Color(83,202,188));
            component.setForeground(new Color(50,50,50));
        }
        else if(table.getValueAt(row,7).toString().equals("Belum Terlayani")){
            component.setBackground(new Color(203,163,211));
            component.setForeground(new Color(50,50,50));
        }
         // Penanganan jika baris sedang dipilih
        if (isSelected) {
            component.setBackground(new Color(80,129,227)); // Warna biru untuk baris yang dipilih
            component.setForeground(Color.WHITE);           // Warna teks putih agar kontras
        }
        return component;
    }
    
}
