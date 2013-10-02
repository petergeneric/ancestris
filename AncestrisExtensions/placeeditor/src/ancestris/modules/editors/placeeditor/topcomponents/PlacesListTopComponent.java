package ancestris.modules.editors.placeeditor.topcomponents;

import ancestris.modules.editors.placeeditor.models.GedcomPlaceTableModel;
import ancestris.modules.editors.placeeditor.panels.PlacesEditorPanel;
import ancestris.modules.gedcom.utilities.GedcomUtilities;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyPlace;
import genj.gedcom.UnitOfWork;
import java.awt.Dialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@TopComponent.Description(preferredID = "PlacesTableTopComponent",
iconBase="ancestris/modules/editors/placeeditor/actions/Place.png", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "ancestris.modules.editors.placeeditor.topcomponents.PlacesTableTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@Messages({
    "CTL_PlacesTableAction=PlacesTable",
    "CTL_PlacesTableTopComponent=PlacesTable Window",
    "HINT_PlacesTableTopComponent=This is a PlacesTable window"
})
public final class PlacesListTopComponent extends TopComponent {

    private Map<String, Set<PropertyPlace>> placesMap = new HashMap<String, Set<PropertyPlace>>();
    private GedcomPlaceTableModel placeTableModel;
    private TableRowSorter<TableModel> placeTableSorter;
    private Gedcom gedcom = null;
    int currentRowIndex = -1;

    public PlacesListTopComponent(final Gedcom gedcom) {
        this.gedcom = gedcom;
        String[] placeFormat = PropertyPlace.getFormat(gedcom);

        placeTableModel = new GedcomPlaceTableModel(placeFormat);

        initComponents();
        placeTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int rowIndex = placeTable.convertRowIndexToModel(placeTable.getSelectedRow());
                    final Set<PropertyPlace> propertyPlaces = ((GedcomPlaceTableModel) placeTable.getModel()).getValueAt(rowIndex);
                    Object[] propertyPlaceArray = propertyPlaces.toArray();
                    PlacesEditorPanel placesEditorPanel = new PlacesEditorPanel(PropertyPlace.getFormat(gedcom), (PropertyPlace) propertyPlaceArray[0]);
                    DialogDescriptor placesEditorPanelDescriptor = new DialogDescriptor(
                            placesEditorPanel,
                            NbBundle.getMessage(PlacesEditorPanel.class, "PlacesEditorPanel.title"),
                            true,
                            null);
                    Dialog dialog = DialogDisplayer.getDefault().createDialog(placesEditorPanelDescriptor);
                    dialog.setVisible(true);
                    dialog.toFront();
                    if (placesEditorPanelDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
                        try {
                            final String placeString = placesEditorPanel.getPlaceString();
                            gedcom.doUnitOfWork(new UnitOfWork() {

                                @Override
                                public void perform(Gedcom gedcom) throws GedcomException {
                                    for (PropertyPlace propertyPlace : propertyPlaces) {
                                        propertyPlace.setValue(placeString);
                                    }
                                }
                            }); // end of doUnitOfWork
                        } catch (GedcomException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        updateGedcomPlaceTable();
                    }
                }
            }
        });

        placeTableSorter = new TableRowSorter<TableModel>(placeTable.getModel());
        placeTable.setRowSorter(placeTableSorter);

        setName(Bundle.CTL_PlacesTableTopComponent());
        setToolTipText(Bundle.HINT_PlacesTableTopComponent());
        associateLookup(Lookups.singleton(gedcom));

    }

    private void updateGedcomPlaceTable() {
        List<PropertyPlace> gedcomPlacesList = GedcomUtilities.searchProperties(gedcom, PropertyPlace.class, GedcomUtilities.ENT_ALL);

        placesMap.clear();

        for (PropertyPlace propertyPlace : gedcomPlacesList) {
            String gedcomPlace = propertyPlace.getDisplayValue();

            Set<PropertyPlace> propertySet = placesMap.get(gedcomPlace);
            if (propertySet == null) {
                propertySet = new HashSet<PropertyPlace>();
                placesMap.put(gedcomPlace, propertySet);
            }
            propertySet.add((PropertyPlace) propertyPlace);
        }

        placeTableModel.update(placesMap);

    }

    private void newFilter(String filter) {
        RowFilter<TableModel, Integer> rf;
        //If current expression doesn't parse, don't update.
        try {
            rf = RowFilter.regexFilter(filter);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }

        placeTableSorter.setRowFilter(rf);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        placesScrollPane = new javax.swing.JScrollPane();
        placeTable = new javax.swing.JTable();
        searchPlaceLabel = new javax.swing.JLabel();
        filterGedcomPlaceTextField = new javax.swing.JTextField();
        filterGedcomPlaceButton = new javax.swing.JButton();
        clearFilterGedcomPlaceButton = new javax.swing.JButton();

        placeTable.setAutoCreateRowSorter(true);
        placeTable.setModel(placeTableModel);
        placeTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        placeTable.setShowHorizontalLines(false);
        placeTable.setShowVerticalLines(false);
        placesScrollPane.setViewportView(placeTable);

        org.openide.awt.Mnemonics.setLocalizedText(searchPlaceLabel, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.searchPlaceLabel.text")); // NOI18N

        filterGedcomPlaceTextField.setText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.filterGedcomPlaceTextField.text")); // NOI18N
        filterGedcomPlaceTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                filterGedcomPlaceTextFieldKeyTyped(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(filterGedcomPlaceButton, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.filterGedcomPlaceButton.text")); // NOI18N
        filterGedcomPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterGedcomPlaceButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(clearFilterGedcomPlaceButton, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.clearFilterGedcomPlaceButton.text")); // NOI18N
        clearFilterGedcomPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFilterGedcomPlaceButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(searchPlaceLabel)
                .addGap(6, 6, 6)
                .addComponent(filterGedcomPlaceTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterGedcomPlaceButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearFilterGedcomPlaceButton))
            .addComponent(placesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchPlaceLabel)
                    .addComponent(filterGedcomPlaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterGedcomPlaceButton)
                    .addComponent(clearFilterGedcomPlaceButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(placesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void filterGedcomPlaceTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterGedcomPlaceTextFieldKeyTyped
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            newFilter(filterGedcomPlaceTextField.getText());
        }
    }//GEN-LAST:event_filterGedcomPlaceTextFieldKeyTyped

    private void filterGedcomPlaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterGedcomPlaceButtonActionPerformed
        newFilter(filterGedcomPlaceTextField.getText());
    }//GEN-LAST:event_filterGedcomPlaceButtonActionPerformed

    private void clearFilterGedcomPlaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFilterGedcomPlaceButtonActionPerformed
        filterGedcomPlaceTextField.setText("");
        newFilter(filterGedcomPlaceTextField.getText());
    }//GEN-LAST:event_clearFilterGedcomPlaceButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearFilterGedcomPlaceButton;
    private javax.swing.JButton filterGedcomPlaceButton;
    private javax.swing.JTextField filterGedcomPlaceTextField;
    private javax.swing.JTable placeTable;
    private javax.swing.JScrollPane placesScrollPane;
    private javax.swing.JLabel searchPlaceLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        if (gedcom != null) {
            updateGedcomPlaceTable();
        }
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
