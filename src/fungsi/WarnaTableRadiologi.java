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
public class WarnaTableRadiologi extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        Component component = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        if (row % 2 == 1) {
            component.setBackground(new Color(225,235,249));
        } else {
            component.setBackground(Color.WHITE);
        }

        component.setForeground(new Color(50,50,50));

        try {

            String statusCito = table.getValueAt(row,14) == null ? ""
                    : table.getValueAt(row,14).toString();

            String jenisBayar = table.getValueAt(row,16) == null ? ""
                    : table.getValueAt(row,16).toString();

            String statusBayar = table.getValueAt(row,20) == null ? ""
                    : table.getValueAt(row,20).toString();

            // CITO
            if (statusCito.equalsIgnoreCase("CITO")) {
                component.setBackground(new Color(231,160,156));
            }

            // REGULER
            else if (jenisBayar.equalsIgnoreCase("REGULER")) {
                component.setBackground(new Color(175, 225, 195));
            }

            // REGULER + BELUM BAYAR
            if (jenisBayar.equalsIgnoreCase("REGULER")
                    && statusBayar.equalsIgnoreCase("Belum")
                    && column == 2) {

                component.setBackground(Color.BLACK);
                component.setForeground(Color.WHITE);
            }

        } catch (Exception e) {
        }
// 14 = Status CITO
// 16 = Jenis Bayar
// 20 = Status Bayar
        if (isSelected) {
            component.setBackground(new Color(80,129,227));
            component.setForeground(Color.WHITE);
        }

        return component;
    }
}