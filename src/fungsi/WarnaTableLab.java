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
public class WarnaTableLab extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        Component component = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        // Warna dasar baris
        if (row % 2 == 1) {
            component.setBackground(new Color(225,235,249));
        } else {
            component.setBackground(Color.WHITE);
        }

        component.setForeground(new Color(50,50,50));

        // REGULER
        if (table.getValueAt(row,15) != null &&
            table.getValueAt(row,15).toString().equals("REGULER")) {

            component.setBackground(new Color(175, 225, 195));

            // Hanya kolom nama pasien yang berwarna hitam jika belum bayar
            if (column == 2 &&
                table.getValueAt(row,17) != null &&
                table.getValueAt(row,17).toString().equals("Belum")) {

                component.setBackground(Color.BLACK);
                component.setForeground(Color.WHITE);
            }
        }

        // Jika dipilih
        if (isSelected) {
            component.setBackground(new Color(80,129,227));
            component.setForeground(Color.WHITE);
        }

        return component;
    }
}