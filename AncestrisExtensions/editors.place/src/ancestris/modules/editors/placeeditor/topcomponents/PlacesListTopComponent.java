package ancestris.modules.editors.placeeditor.topcomponents;

import ancestris.modules.editors.geoplace.PlaceEditorPanel;
import ancestris.modules.editors.placeeditor.models.GedcomPlaceTableModel;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.UnitOfWork;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.RetainLocation;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ancestris.modules.editors.placeeditor.topcomponents//PlaceList//EN",
        autostore = false)
@ServiceProvider(service = AncestrisViewInterface.class)
@RetainLocation(AncestrisDockModes.OUTPUT)
public final class PlacesListTopComponent extends AncestrisTopComponent implements ExplorerManager.Provider, GedcomListener {

    final static Logger LOG = Logger.getLogger("ancestris.editor");
    private genj.util.Registry registry = null;

    static final String ICON_PATH = "ancestris/modules/editors/placeeditor/actions/Place.png";
    private static final String PREFERRED_ID = "PlaceListTopComponent";

    private Gedcom gedcom = null;
    private GedcomPlaceTableModel gedcomPlaceTableModel;
    private TableRowSorter<TableModel> placeTableSorter;
    private PlaceEditorPanel placesEditorPanel = null;
    
    private boolean isBusyCommitting = false;
    private UndoRedoListener undoRedoListener;
    

    public PlacesListTopComponent() {
        super();
    }

    @Override
    public Image getImageIcon() {
        return ImageUtilities.loadImage(ICON_PATH, true);
    }

    @Override
    public void setName() {
        setName(NbBundle.getMessage(getClass(), "CTL_PlacesTableTopComponent"));
    }

    @Override
    public void setToolTipText() {
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_PlacesTableTopComponent"));
    }

    @Override
    public boolean createPanel() {

        this.gedcom = getGedcom();
        registry = gedcom.getRegistry();
        gedcomPlaceTableModel = new GedcomPlaceTableModel(gedcom);

        initComponents();
        
        placeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int rowIndex = placeTable.convertRowIndexToModel(placeTable.getSelectedRow());
                    final Set<PropertyPlace> propertyPlaces = ((GedcomPlaceTableModel) placeTable.getModel()).getValueAt(rowIndex);
                    placesEditorPanel = new PlaceEditorPanel();
                    DialogDescriptor placesEditorPanelDescriptor = new DialogDescriptor(
                            placesEditorPanel,
                            NbBundle.getMessage(PlaceEditorPanel.class, "PlaceEditorPanel.edit.all", propertyPlaces.iterator().next().getGeoValue()),
                            true,
                            null);
                    placesEditorPanel.set(gedcom, propertyPlaces);
                    Dialog dialog = DialogDisplayer.getDefault().createDialog(placesEditorPanelDescriptor);
                    dialog.setVisible(true);
                    dialog.toFront();
                    if (placesEditorPanelDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
                        commit(); 
                        updateGedcomPlaceTable();
                    }
                }
            }
        });

        String memoField = registry.get("placeTableFilter", "");
        if (!memoField.isEmpty()) {
            searchPlaceComboBox.setSelectedItem(memoField);
        }
        int c = searchPlaceComboBox.getSelectedIndex();
        
        placeTable.setID(gedcom, PlacesListTopComponent.class.getName());
        placeTableSorter = (TableRowSorter) placeTable.getRowSorter();
        ArrayList list = new ArrayList();
        list.add( new RowSorter.SortKey(c, SortOrder.ASCENDING) );
        placeTableSorter.setSortKeys(list);
        updateGedcomPlaceTable();
        
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                filterGedcomPlaceTextField.requestFocusInWindow();
            }
        });
        
        return true; // registers the AncestrisTopComponent name, tooltip and gedcom context as it continues the code within AncestrisTopComponent
    }


    private void updateGedcomPlaceTable() {
        gedcomPlaceTableModel.update();
        nbPlaces.setText(NbBundle.getMessage(getClass(), "PlacesListTopComponent.nbPlaces.text", gedcomPlaceTableModel.getRowCount()));
        placeTableSorter.sort();
    }

    private void newFilter(String filter) {
        RowFilter<TableModel, Integer> rf;
        //If current expression doesn't parse, don't update.
        try {
            rf = RowFilter.regexFilter("(?i)" + filter, searchPlaceComboBox.getSelectedIndex());
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

        searchPlaceLabel = new javax.swing.JLabel();
        filterGedcomPlaceTextField = new javax.swing.JTextField();
        filterGedcomPlaceButton = new javax.swing.JButton();
        clearFilterGedcomPlaceButton = new javax.swing.JButton();
        searchPlaceComboBox = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        placeTable = new ancestris.modules.editors.placeeditor.topcomponents.EditorTable();
        nbPlaces = new javax.swing.JLabel();

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

        searchPlaceComboBox.setModel(new DefaultComboBoxModel(PropertyPlace.getFormat(gedcom)));
        searchPlaceComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchPlaceComboBoxItemStateChanged(evt);
            }
        });

        placeTable.setAutoCreateRowSorter(true);
        placeTable.setModel(gedcomPlaceTableModel);
        jScrollPane1.setViewportView(placeTable);

        nbPlaces.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(nbPlaces, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.nbPlaces.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(searchPlaceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchPlaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterGedcomPlaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterGedcomPlaceButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearFilterGedcomPlaceButton)
                .addGap(18, 18, 18)
                .addComponent(nbPlaces, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchPlaceLabel)
                    .addComponent(filterGedcomPlaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterGedcomPlaceButton)
                    .addComponent(clearFilterGedcomPlaceButton)
                    .addComponent(searchPlaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nbPlaces))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                .addGap(0, 0, 0))
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

    private void searchPlaceComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_searchPlaceComboBoxItemStateChanged
        registry.put("placeTableFilter", (String) searchPlaceComboBox.getSelectedItem());
    }//GEN-LAST:event_searchPlaceComboBoxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearFilterGedcomPlaceButton;
    private javax.swing.JButton filterGedcomPlaceButton;
    private javax.swing.JTextField filterGedcomPlaceTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel nbPlaces;
    private ancestris.modules.editors.placeeditor.topcomponents.EditorTable placeTable;
    private javax.swing.JComboBox searchPlaceComboBox;
    private javax.swing.JLabel searchPlaceLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // Set undo/redo
        undoRedoListener = new UndoRedoListener();
        UndoRedo undoRedo = getUndoRedo();
        undoRedo.addChangeListener(undoRedoListener);
    }
    

    @Override
    public void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    @Override
    public void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        if (!entity.getProperties(PropertyPlace.class).isEmpty()) {
            updateGedcomPlaceTable();
        }
    }

    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        if (!entity.getProperties(PropertyPlace.class).isEmpty()) {
            updateGedcomPlaceTable();
        }
    }

    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        if (property.getTag().equals("PLAC")) {
            updateGedcomPlaceTable();
        }
    }

    @Override
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        if (property.getTag().equals("PLAC")) {
            updateGedcomPlaceTable();
        }
    }

    @Override
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        if (property.getTag().equals("PLAC")) {
            updateGedcomPlaceTable();
        }
    }


    private void commit() {
        // Is busy committing ?
        if (isBusyCommitting) {
            return;
        }
        isBusyCommitting = true;
        try {
            if (gedcom.isWriteLocked()) {
                placesEditorPanel.commit();
            } else {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        placesEditorPanel.commit();
                    }
                });
            }

        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Error committing editor", t);
        } finally {
            isBusyCommitting = false;
        }
    }

    
    private class UndoRedoListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            updateGedcomPlaceTable();
        }
    }

    
}
