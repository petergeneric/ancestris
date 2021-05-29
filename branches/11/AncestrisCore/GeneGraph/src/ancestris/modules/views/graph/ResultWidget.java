/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.views.graph;

import ancestris.awt.FilteredMouseAdapter;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Property;
import genj.view.ViewContext;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createLineBorder;
import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Widget to display the list of element.
 * @author Zurga
 */
public class ResultWidget extends JList<DisplayPathElement> implements ListSelectionListener, ListCellRenderer<DisplayPathElement> {

        private final ResultsList results;
        private final JTextPane text = new JTextPane();
        private final Context context;
        
        /**
         * Constructeur pour l'affichage Netbeans.
         */
        public ResultWidget() {
            results = new ResultsList();
            context = new Context();
            
        }

        /**
         * Constructor
         */
        public ResultWidget(final ResultsList results, final Context context) {
            super(results);
            this.results = results;
            this.context = context;

            init();
        }

        private void init() {
            // rendering
            setCellRenderer(this);
            // Default size doesn't work in Java 11, set 2 pixels up and down from icon size.
            setFixedCellHeight(18);
            addListSelectionListener(this);
            text.setOpaque(true);
            addMouseListener(new FilteredMouseAdapter() {
                @Override
                public void mouseClickedFiltered(MouseEvent e) {
                    int row = getSelectedIndex();
                    if (row >= 0) {
                        Property cell = results.getHit(row).getProperty();
                        if (cell != null) {
                            SelectionDispatcher.fireSelection(e, new Context(cell));
                        }
                    }
                }
            });
        }

        /**
         * ContextProvider - callback
         */
        public ViewContext getContext() {

            if (context == null) {
                return null;
            }

            List<Property> properties = new ArrayList<>();
            for (Object selection1 : getSelectedValuesList()) {
                DisplayPathElement hit = (DisplayPathElement) selection1;
                properties.add(hit.getProperty());
            }
            return new ViewContext(context.getGedcom(), null, properties);
        }

        @Override
        public Component getListCellRendererComponent(JList list, DisplayPathElement value, int index, boolean isSelected, boolean cellHasFocus) {
            // prepare color
            text.setBorder(isSelected ? createLineBorder(getSelectionBackground(), 1, false) : createEmptyBorder(3, 3, 3, 3));

            // show hit document (includes image and text)
            text.setDocument(value.getDocument());
            return text;
        }

        /**
         * @see
         * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            int row = getSelectedIndex();
            if (row >= 0) {
                SelectionDispatcher.fireSelection(new Context(results.getHit(row).getProperty()));
            }
        }
    
 } //ResultWidget
