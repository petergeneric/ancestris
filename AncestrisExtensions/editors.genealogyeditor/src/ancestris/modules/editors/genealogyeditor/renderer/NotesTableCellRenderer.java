package ancestris.modules.editors.genealogyeditor.renderer;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author dominique
 */
public class NotesTableCellRenderer extends JViewport implements TableCellRenderer {
JTextPane textPane;
        
    public NotesTableCellRenderer() {
        textPane = new JTextPane();
        add(textPane);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            textPane.setBackground(table.getSelectionBackground());
            textPane.setForeground(table.getSelectionForeground());
        } else {
            textPane.setBackground(table.getBackground());
            textPane.setForeground(table.getForeground());
        }
        textPane.setText(value.toString());
        table.setRowHeight(row, (int) getPreferredSize().getHeight());
        return this;
    }
}
