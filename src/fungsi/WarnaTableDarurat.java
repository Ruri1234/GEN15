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
public class WarnaTableDarurat extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (row % 2 == 1){
            component.setBackground(new Color(225,235,249));
            component.setForeground(new Color(50,50,50));
        }else{
            component.setBackground(new Color(255,255,255));
            component.setForeground(new Color(50,50,50)); 
        } 
        if
          ((Float.parseFloat(table.getValueAt(row,6).toString())) < (Float.parseFloat(table.getValueAt(row,5).toString()))){
            component.setBackground(new Color(203,163,211));
            component.setForeground(new Color(50,50,50));
        }
        else if
            ((Float.parseFloat(table.getValueAt(row,6).toString())) >= (Float.parseFloat(table.getValueAt(row,5).toString()))){
            component.setBackground(new Color(144,223,184));
            component.setForeground(new Color(50,50,50));
        }
        return component;
    }
    
}
