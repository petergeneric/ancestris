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
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

@NbBundle.Messages({
    "allfilter=All"
})
public final class TableFilterWidget implements Presenter.Toolbar {

    private static FilterCombo filter;
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

    public void setHeaders(AbstractTableModel model) {
        headers = new ArrayList<String>();
        headers.add(allfilter());
        for (int c = 0; c < model.getColumnCount(); c++) {
            headers.add(model.getColumnName(c));
        }
        // initialize tooltip
        getFilter().setIndex(getFilter().getIndex());
    }

    public void setColFilter(int colFilter) {
        getFilter().setIndex(colFilter);
    }

    public int getColFilter() {
        return getFilter().getIndex();
    }

    public void addFilterListener(TableFilterListener listener) {
        getFilter().filterListener = listener;
    }

    private class FilterCombo extends JPanel {

        private final Icon POPUP = GraphicsHelper.getIcon(Color.BLACK, 0, 0, 8, 0, 4, 4);
        private int index;
        private Popup pick = new Popup();
        private final JTextField filterText;
        private TableFilterListener filterListener;

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

            add(filterText);
            setIndex(0);
            add(pick);
        }
        
        private void setIndex(int index){
            this.index = index;
            if (!headers.isEmpty())
                pick.setToolTipText(headers.get(index));
        }
        private int getIndex(){
            return index;
        }
        
        private void invokeFilter() {
            if (filterListener != null) {
                filterListener.filter(filterText.getText(), getIndex() - 1);
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

    public interface TableFilterListener {

        public void filter(String text, int col);
    }
}
