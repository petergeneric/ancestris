package ancestris.modules.editors.placeeditor.topcomponents;

import ancestris.modules.editors.geoplace.PlaceEditorPanel;
import ancestris.modules.editors.placeeditor.models.GedcomPlaceTableModel;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.FileChooserBuilder;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.UnitOfWork;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.RowFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.netbeans.api.settings.ConvertAsProperties;
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
public final class PlacesListTopComponent extends AncestrisTopComponent implements ExplorerManager.Provider, GedcomMetaListener {

    final static Logger LOG = Logger.getLogger("ancestris.editor");
    private genj.util.Registry registry = null;

    static final String ICON_PATH = "ancestris/modules/editors/placeeditor/actions/Place.png";

    private Gedcom gedcom = null;
    private GedcomPlaceTableModel gedcomPlaceTableModel;
    private TableRowSorter<TableModel> placeTableSorter;
    private PlaceEditorPanel placesEditor = null;

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
        
        filterGedcomPlaceButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "FILTER");
        filterGedcomPlaceButton.getActionMap().put("FILTER", new AbstractAction("FILTER") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                filterGedcomPlaceButtonActionPerformed(evt);
            }
        });
        
        placeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                LOG.log(Level.FINE, "NB click = {0}", e.getClickCount());
                if (e.getClickCount() == 2) {
                    int rowIndex = placeTable.convertRowIndexToModel(placeTable.getSelectedRow());
                    final Set<PropertyPlace> propertyPlaces = ((GedcomPlaceTableModel) placeTable.getModel()).getValueAt(rowIndex);
                    placesEditor = new PlaceEditorPanel();
                    placesEditor.set(gedcom, propertyPlaces);
                    final boolean search = propertyPlaces.iterator().next().getLatitude(true) == null;
                    WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                        @Override
                        public void run() {
                            if (search) {
                                placesEditor.runSearch();
                            }
                            JButton OKButton = new JButton(NbBundle.getMessage(getClass(), "Button_Ok"));
                            JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
                            Object[] options = new Object[]{OKButton, cancelButton};
                            Object o = DialogManager.create(NbBundle.getMessage(PlaceEditorPanel.class, "PlaceEditorPanel.edit.all", propertyPlaces.iterator().next().getGeoValue()), placesEditor).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
                            placesEditor.close();
                            if (o == OKButton) {
                                commit();
                                updateGedcomPlaceTable();
                            }
                        }
                    });
                }
                if (e.getClickCount() == 1) {
                    JToolTip tooltip = new JToolTip();
                    tooltip.setTipText(NbBundle.getMessage(getClass(), "PlacesListTopComponent.edit.tip"));
                    PopupFactory popupFactory = PopupFactory.getSharedInstance();
                    int x = e.getXOnScreen();
                    int y = e.getYOnScreen();
                    final Popup tooltipContainer = popupFactory.getPopup(e.getComponent(), tooltip, x, y);
                    tooltipContainer.show();
                    (new Thread(new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException ex) {
                            }
                            tooltipContainer.hide();
                        }

                    })).start();

                }
            }
        });

        String city = PropertyPlace.getCityTag(gedcom);
        String memoField = registry.get("placeTableFilter", city);
        if (!memoField.isEmpty()) {
            searchPlaceComboBox.setSelectedItem(memoField);
        }

        placeTable.setID(gedcom, PlacesListTopComponent.class.getName(), searchPlaceComboBox.getSelectedIndex());
        placeTableSorter = placeTable.getSorter();
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
            if (searchPlaceComboBox.getSelectedIndex() == 0) {
                rf = RowFilter.regexFilter("(?i)" + filter);
            } else {
                rf = RowFilter.regexFilter("(?i)" + filter, searchPlaceComboBox.getSelectedIndex()-1);
            }
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
        jBDownload = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(searchPlaceLabel, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.searchPlaceLabel.text")); // NOI18N

        filterGedcomPlaceTextField.setText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.filterGedcomPlaceTextField.text")); // NOI18N
        filterGedcomPlaceTextField.setToolTipText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.filterGedcomPlaceTextField.toolTipText")); // NOI18N
        filterGedcomPlaceTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                filterGedcomPlaceTextFieldKeyTyped(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(filterGedcomPlaceButton, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.filterGedcomPlaceButton.text")); // NOI18N
        filterGedcomPlaceButton.setToolTipText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.filterGedcomPlaceButton.toolTipText")); // NOI18N
        filterGedcomPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterGedcomPlaceButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(clearFilterGedcomPlaceButton, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.clearFilterGedcomPlaceButton.text")); // NOI18N
        clearFilterGedcomPlaceButton.setToolTipText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.clearFilterGedcomPlaceButton.toolTipText")); // NOI18N
        clearFilterGedcomPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFilterGedcomPlaceButtonActionPerformed(evt);
            }
        });

        String[] criteria = new String[PropertyPlace.getFormat(gedcom).length + 1];
        criteria[0] = "*";
        int pos = 1;
        for (String element : PropertyPlace.getFormat(gedcom)) {
            criteria[pos] = element;
            pos++;
        }
        searchPlaceComboBox.setModel(new DefaultComboBoxModel(criteria));
        searchPlaceComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.searchPlaceComboBox.toolTipText")); // NOI18N
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
        nbPlaces.setToolTipText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.nbPlaces.toolTipText")); // NOI18N

        jBDownload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/placeeditor/actions/Download.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jBDownload, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.jBDownload.text")); // NOI18N
        jBDownload.setToolTipText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.jBDownload.toolTipText")); // NOI18N
        jBDownload.setMinimumSize(new java.awt.Dimension(29, 25));
        jBDownload.setPreferredSize(new java.awt.Dimension(29, 25));
        jBDownload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBDownloadActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchPlaceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchPlaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterGedcomPlaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterGedcomPlaceButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearFilterGedcomPlaceButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                .addComponent(nbPlaces)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBDownload, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(searchPlaceLabel)
                    .addComponent(searchPlaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterGedcomPlaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterGedcomPlaceButton)
                    .addComponent(clearFilterGedcomPlaceButton)
                    .addComponent(nbPlaces)
                    .addComponent(jBDownload, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE))
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

    private void jBDownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBDownloadActionPerformed
        File file  = new FileChooserBuilder(PlacesListTopComponent.class)
                    .setTitle(NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.export"))
                    .setApproveText(NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.export"))
                    .setFileHiding(true)
                    .setParent(this)
                    .setFileFilter(new FileNameExtensionFilter(NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.export.filter.text"),"txt","csv"))
                    .setDefaultExtension(FileChooserBuilder.getTextFilter().getExtensions()[0])
                    .setDefaultBadgeProvider()
                    .setDefaultWorkingDirectory(new File(System.getProperty("user.home")))
                    .showSaveDialog(true);
            if (file == null) {
                return;
            }
            try {
                tsvExport(file);
            } catch (IOException e) {
                DialogManager.createError(NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.export"),
                        NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.export.error", file.getAbsoluteFile())).show();
            }
    }//GEN-LAST:event_jBDownloadActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearFilterGedcomPlaceButton;
    private javax.swing.JButton filterGedcomPlaceButton;
    private javax.swing.JTextField filterGedcomPlaceTextField;
    private javax.swing.JButton jBDownload;
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

    private boolean updateTable = false;

    @Override
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        if (!updateTable && !entity.getProperties(PropertyPlace.class).isEmpty()) {
            updateTable = true;
        }
    }

    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        if (!updateTable && !entity.getProperties(PropertyPlace.class).isEmpty()) {
            updateTable = true;
        }
    }

    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        if (!updateTable && property.getTag().equals("PLAC")) {
            updateTable = true;
        }
    }

    @Override
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        if (!updateTable && property.getTag().equals("PLAC")) {
            updateTable = true;
        }
    }

    @Override
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        if (!updateTable && property.getTag().equals("PLAC")) {
            updateTable = true;
        }
    }

    @Override
    public void gedcomHeaderChanged(Gedcom gedcom) {
    }

    @Override
    public void gedcomWriteLockAcquired(Gedcom gedcom) {
        updateTable = false;
    }

    @Override
    public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
    }

    @Override
    public void gedcomAfterUnitOfWork(Gedcom gedcom) {
    }

    @Override
    public void gedcomWriteLockReleased(Gedcom gedcom) {
        if (updateTable) {
            updateGedcomPlaceTable();
            updateTable = false;
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
                placesEditor.commit();
            } else {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        placesEditor.commit();
                    }
                });
            }

        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Error committing editor", t);
        } finally {
            isBusyCommitting = false;
        }
    }
    
    public void tsvExport(File file) throws IOException {
        final FileWriter writer = new FileWriter(file);

        for (int i = 0; i < gedcomPlaceTableModel.getColumnCount(); i++) {
            writer.write(gedcomPlaceTableModel.getColumnName(i) + "\t");
        }

        writer.write("\n");

        for (int r = 0; r < placeTableSorter.getViewRowCount(); r++) {
            for (int col = 0; col < gedcomPlaceTableModel.getColumnCount(); col++) {
                writer.write(exportCellValue(gedcomPlaceTableModel.getValueAt(placeTable.convertRowIndexToModel(r), col), r, col));
                writer.write("\t");
            }
            writer.write("\n");
        }
        writer.close();
    }
    
    private String exportCellValue(Object object, int row, int col) {
        if (object == null) {
            return "";
        }
        return object.toString();
    }

    private class UndoRedoListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            updateGedcomPlaceTable();
        }
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        gedcomPlaceTableModel.eraseModel();
    }

}
