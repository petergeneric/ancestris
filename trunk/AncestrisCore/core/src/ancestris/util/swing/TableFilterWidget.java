/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2014 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.util.swing;

import static ancestris.util.swing.Bundle.*;
import genj.util.swing.GraphicsHelper;
import genj.util.swing.PopupWidget;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultRowSorter;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

@NbBundle.Messages({
    "allfilter=All",
    "exactmatch=Exact Match",
    "occurrences.label={0} occurrences"
})
public final class TableFilterWidget implements Presenter.Toolbar {

    private FilterCombo filter;
    private List<String> headers = new ArrayList<String>();

    @Override
    public java.awt.Component getToolbarPresenter() {
        return getFilter();
    }

    private FilterCombo getFilter() {
        if (filter == null) {
            filter = new FilterCombo();
        }
        return filter;
    }

    public void setColumn(int colFilter) {
        getFilter().setIndex(colFilter);
    }

    public int getColFilter() {
        return getFilter().getIndex();
    }

    public void setSorter(DefaultRowSorter sorter) {
        getFilter().sorter = sorter;
        // get headers from underlying model
        if (sorter != null) {
            setHeaders((TableModel) sorter.getModel());
        }
    }

    private void setHeaders(TableModel model) {
        headers = new ArrayList<String>();
        headers.add(allfilter());
        for (int c = 0; c < model.getColumnCount(); c++) {
            headers.add(model.getColumnName(c));
        }
        // initialize tooltip
        getFilter().setIndex(getFilter().getIndex());
    }

    /**
     * refresh filtered view
     */
    public void refresh() {
        getFilter().invokeFilter();
    }

    private class FilterCombo extends JPanel {

        private final Icon POPUP = GraphicsHelper.getIcon(Color.BLACK, 0, 0, 8, 0, 4, 4);
        private int index;
        private Popup pick = new Popup();
        private final JTextField filterText;
        private final JCheckBox exactMatch;
        private DefaultRowSorter sorter;
        private JLabel number;

        /**
         * Constructor
         */
        FilterCombo() {
            setLayout(new java.awt.GridBagLayout());
            filterText = new JTextField(10) {

                @Override
                public Dimension getMaximumSize() {
                    return getPreferredSize();
                }
            };
            filterText.setMinimumSize(new Dimension(30, 5));
            filterText.getDocument().addDocumentListener(
                    new DocumentListener() {

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            invokeFilter();
                        }

                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            invokeFilter();
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            invokeFilter();
                        }
                    });

            exactMatch = new JCheckBox(exactmatch());
            exactMatch.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    invokeFilter();
                }
            });
            number = new JLabel();
            number.setHorizontalAlignment(SwingConstants.RIGHT);
            number.setPreferredSize(new Dimension(120, 12));
            add(number);
            add(filterText);
            setIndex(0);
            add(pick);
            add(exactMatch);
        }

        private void setIndex(int index) {
            if (index < 0 || index > headers.size()) {
                index = 0;
            }
            this.index = index;
            if (!headers.isEmpty()) {
                if (index < headers.size()) {
                    pick.setToolTipText(headers.get(index));
                }
            }
        }

        private int getIndex() {
            return index;
        }

        /**
         * Update the row filter regular expression from the expression in
         * the text box.
         */
        private void invokeFilter() {
            if (sorter != null) {
                RowFilter<TableModel, Object> rf;
                int col = getIndex() - 1;
                String flags = "";
                if (!exactMatch.isSelected()) {
                    flags = "(?i)(?u)";
                }
                //If current expression doesn't parse, don't update.
                try {
                    if (col < 0) {
                        rf = RowFilter.regexFilter(flags + filterText.getText());
                    } else {
                        rf = RowFilter.regexFilter(flags + filterText.getText(), col);
                    }
                } catch (java.util.regex.PatternSyntaxException e) {
                    return;
                }
                sorter.setRowFilter(rf);
                String numberText = "";
                if (!filterText.getText().isEmpty()) {
                    numberText = occurrences_label(sorter.getViewRowCount());
                }
                number.setText(numberText + " ");
            }
        }

        private class Popup extends PopupWidget {

            Popup() {
                super(POPUP);
            }

            @Override
            public void showPopup() {
                removeItems();
                for (int i = 0; i < headers.size(); i++) {
                    final int j = i;
                    JMenuItem item = new JMenuItem(new AbstractAction(headers.get(i)) {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            setIndex(j);
                            FilterCombo.this.invokeFilter();
                        }
                    });
                    if (getIndex() == i) {
                        item.setFont(item.getFont().deriveFont(Font.BOLD));
                    }
                    addItem(item);
                }
                super.showPopup();
            }
        }
    }
}
