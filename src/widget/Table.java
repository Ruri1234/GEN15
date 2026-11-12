package widget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class Table extends JTable {
    private static final long serialVersionUID = 3L;

    // Warna zebra (hanya dipakai jika setZebra(true))
    static final Color ZEBRA = new Color(248, 250, 247);
    static final int   ARC_PILIH = 8;

    private static final Border PADDING_SEL = BorderFactory.createEmptyBorder(0, 6, 0, 6);

    // Default false supaya tabel polos putih seperti modifikasi sebelumnya
    private boolean zebra = false;
    private JViewport viewportTerpasang;
    private final ChangeListener scrollHandler = e -> repaint();

    public Table() {
        super();
        // ===== WARNA KUSTOM (satu-satunya tempat mengatur warna tabel) =====
        setBackground(new Color(255,255,255));
        setGridColor(new Color(226,231,221));
        setForeground(new Color(50,50,50));
        setFont(new java.awt.Font("Tahoma", 0, 11));
        setRowHeight(24);
        setSelectionBackground(new Color(255,255,255));
        setSelectionForeground(new Color(255,0,0));

        JTableHeader header = getTableHeader();
        header.setForeground(new Color(255, 255, 255));
        header.setBackground(new Color(45, 85, 125));
        header.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(45, 85, 125)));
        header.setFont(new java.awt.Font("Tahoma", 0, 11));
        header.setPreferredSize(new Dimension(0, 30)); // header lebih tinggi/lega
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            private static final long serialVersionUID = 1L;

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                int used = getColumnModel().getTotalColumnWidth();
                if (used < getWidth()) {
                    JTable t = getTable();
                    g.setColor(t != null ? t.getBackground() : Color.WHITE);
                    g.fillRect(used, 0, getWidth() - used, getHeight());
                }
            }
        };
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (getParent() instanceof JViewport) {
            viewportTerpasang = (JViewport) getParent();
            viewportTerpasang.addChangeListener(scrollHandler);
        }
    }

    @Override
    public void removeNotify() {
        if (viewportTerpasang != null) {
            viewportTerpasang.removeChangeListener(scrollHandler);
            viewportTerpasang = null;
        }
        super.removeNotify();
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (renderer instanceof UIResource) {
            if (c instanceof JComponent) {
                ((JComponent) c).setOpaque(false);
                ((JComponent) c).setBorder(PADDING_SEL);
            }
            boolean pilih = isRowSelected(row) && getRowSelectionAllowed();
            // Warna teks mengikuti setForeground / setSelectionForeground
            c.setForeground(pilih ? getSelectionForeground() : getForeground());
        }
        return c;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle clip = g2.getClipBounds();
            if (clip == null) {
                clip = new Rectangle(0, 0, getWidth(), getHeight());
            }
            Rectangle terlihat = getVisibleRect();

            g2.setColor(getBackground());
            g2.fillRect(clip.x, clip.y, clip.width, clip.height);

            int n = getRowCount();
            if (n > 0) {
                int awal = Math.max(0, rowAtPoint(new java.awt.Point(0, clip.y)));
                int akhir = rowAtPoint(new java.awt.Point(0, clip.y + clip.height - 1));
                if (akhir < 0) {
                    akhir = n - 1;
                }

                for (int row = awal; row <= akhir; row++) {
                    Rectangle r = getCellRect(row, 0, true);

                    if (zebra && row % 2 == 1) {
                        g2.setColor(ZEBRA);
                        g2.fillRect(clip.x, r.y, clip.width, r.height);
                    }

                    if (isRowSelected(row) && getRowSelectionAllowed()) {
                        g2.setColor(getSelectionBackground());
                        g2.fill(new RoundRectangle2D.Float(terlihat.x + 4, r.y + 1,
                                terlihat.width - 8, r.height - 2, ARC_PILIH, ARC_PILIH));
                    }
                }
            }
        } finally {
            g2.dispose();
        }

        super.paintComponent(g);

        gambarGaris(g);
    }

    private void gambarGaris(Graphics g) {
        int n = getRowCount();
        int kolom = getColumnCount();
        if (n == 0 || kolom == 0) {
            return;
        }
        Rectangle clip = g.getClipBounds();
        if (clip == null) {
            clip = new Rectangle(0, 0, getWidth(), getHeight());
        }
        int awal = Math.max(0, rowAtPoint(new java.awt.Point(0, clip.y)));
        int akhir = rowAtPoint(new java.awt.Point(0, clip.y + clip.height - 1));
        if (akhir < 0) {
            akhir = n - 1;
        }
        int kolAwal = Math.max(0, columnAtPoint(new java.awt.Point(clip.x, 0)));
        int kolAkhir = columnAtPoint(new java.awt.Point(clip.x + clip.width - 1, 0));
        if (kolAkhir < 0) {
            kolAkhir = kolom - 1;
        }
        boolean adaSeleksi = getRowSelectionAllowed();
        Color warnaGaris = getGridColor();

        for (int row = awal; row <= akhir; row++) {
            if (adaSeleksi && isRowSelected(row)) {
                continue;
            }
            Rectangle r = getCellRect(row, 0, true);
            int yBawah = r.y + r.height - 1;

            g.setColor(warnaGaris);
            for (int col = kolAwal; col <= kolAkhir; col++) {
                Rectangle c = getCellRect(row, col, true);
                int x = c.x + c.width - 1;
                g.drawLine(x, r.y, x, yBawah);
            }

            if (!(adaSeleksi && row + 1 < n && isRowSelected(row + 1))) {
                g.setColor(warnaGaris);
                g.drawLine(clip.x, yBawah, clip.x + clip.width, yBawah);
            }
        }
    }

    private static class HeaderFlat extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;
        private static final Color LATAR    = new Color(255, 250, 250);
        private static final Color PEMISAH  = new Color(0xE2D9D9);
        private static final Color GARIS_BW = new Color(0xD9CFCF);
        private static final Border PADDING = BorderFactory.createEmptyBorder(0, 6, 0, 3);

        HeaderFlat() {
            setHorizontalAlignment(SwingConstants.LEFT);
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            if (table != null && table.getTableHeader() != null) {
                setFont(table.getTableHeader().getFont());
                setForeground(table.getTableHeader().getForeground());
            }
            setBorder(PADDING);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            g.setColor(LATAR);
            g.fillRect(0, 0, w, h);
            g.setColor(PEMISAH);
            g.drawLine(w - 1, 4, w - 1, h - 5);
            g.setColor(GARIS_BW);
            g.drawLine(0, h - 1, w, h - 1);
            super.paintComponent(g);
        }
    }

    public boolean isZebra() { return zebra; }
    public void setZebra(boolean z) { zebra = z; repaint(); }
}
