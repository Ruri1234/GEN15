/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class WarnaTableKasirRanap extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (row % 2 == 1){
            component.setBackground(new Color(225,235,249));
            component.setForeground(new Color(50,50,50));
        }else{
            component.setBackground(new Color(255,255,255));
            component.setForeground(new Color(50,50,50));
       }  
        if(table.getValueAt(row,6).toString().equals("REGULER")){
            component.setBackground(new Color(175, 225, 195));
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
