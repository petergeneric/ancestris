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
package ancestris.swing.atable;

import static ancestris.swing.atable.Bundle.*;
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
import javax.swing.BoxLayout;
import javax.swing.DefaultRowSorter;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

@NbBundle.Messages({
    //    "allfilter=All",
    "exactmatch=Match case",
    "# {0} - number of occurences",
    "occurrences.label={0} occurrences"
})
public final class ATableFilterWidget implements Presenter.Toolbar {

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
        if (headers == null) {
            headers = new ArrayList<String>();
        } else {
            headers.clear();
        }
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
        private final Popup pick = new Popup();
        private final JTextField filterText;
        private final JCheckBox exactMatch;
        private DefaultRowSorter sorter;
        private final JLabel number;
        
        private Timer docTimer;
        private final int timerDelay = 300; 

        /**
         * Constructor
         */
        FilterCombo() {
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            filterText = new JTextField() {
                @Override
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }

                @Override
                public Dimension getPreferredSize() {
                    Dimension size = super.getPreferredSize();
                    size.width = 220;
                    return size;
                }
            };
            filterText.getDocument().addDocumentListener(
                    new DocumentListener() {

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            textChangedAction(e);
                        }

                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            textChangedAction(e);
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            textChangedAction(e);
                        }
                    });

            exactMatch = new JCheckBox(exactmatch());
            exactMatch.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    invokeFilter();
                }
            });
            number = new JLabel() {

                @Override
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }

                @Override
                public Dimension getPreferredSize() {
                    Dimension size = super.getPreferredSize();
                    size.width = 140;
                    return size;
                }
            };
            number.setHorizontalAlignment(SwingConstants.RIGHT);
            add(number);
            add(filterText);
            setIndex(0);
            add(pick);
            add(exactMatch);
            invokeFilter();
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

        private void textChangedAction(DocumentEvent e) {
            if (docTimer != null && docTimer.isRunning()) {
                docTimer.stop();
            }
            docTimer = new Timer(timerDelay, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    invokeFilter();
                }
            });
            docTimer.setRepeats(false);
            docTimer.start();
        }
        
        /**
         * Update the row filter regular expression from the expression in
         * the text box.
         */
        private void invokeFilter() {
            if (sorter != null) {
                RowFilter<TableModel, Object> rf;
                int col = getIndex();
                String flags = "";
                if (!exactMatch.isSelected()) {
                    flags = "(?i)(?u)";
                }
                //If current expression doesn't parse, don't update.
                try {
                    rf = RowFilter.regexFilter(flags + filterText.getText(), col);
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
