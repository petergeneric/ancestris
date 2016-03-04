package ancestris.modules.editors.placeeditor.topcomponents;

import ancestris.modules.editors.placeeditor.models.GedcomPlaceTableModel;
import ancestris.modules.editors.placeeditor.panels.PlaceEditorPanel;
import ancestris.modules.gedcom.utilities.GedcomUtilities;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.RetainLocation;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ancestris.modules.editors.placeeditor.topcomponents//PlaceList//EN",
        autostore = false)
@ServiceProvider(service = AncestrisViewInterface.class)
@RetainLocation(AncestrisDockModes.OUTPUT)
public final class PlacesListTopComponent extends AncestrisTopComponent implements ExplorerManager.Provider {

    //
    // Path to the icon used by the component and its open action
    static final String ICON_PATH = "ancestris/modules/editors/placeeditor/actions/Place.png";
    private static final String PREFERRED_ID = "PlaceListTopComponent";

    private Map<String, Set<PropertyPlace>> placesMap = new HashMap<String, Set<PropertyPlace>>();
    private GedcomPlaceTableModel gedcomPlaceTableModel;
    private TableRowSorter<TableModel> placeTableSorter;
    private Gedcom gedcom = null;
    int currentRowIndex = -1;

    public PlacesListTopComponent() {

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
        return true; // registers the AncestrisTopComponent name, tooltip and gedcom context as it continues the code within AncestrisTopComponent
    }

    public PlacesListTopComponent(final Gedcom gedcom) {
        super();
        this.gedcom = gedcom;

        gedcomPlaceTableModel = new GedcomPlaceTableModel(PropertyPlace.getFormat(gedcom));

        initComponents();
        placeTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int rowIndex = placeTable.convertRowIndexToModel(placeTable.getSelectedRow());
                    final Set<PropertyPlace> propertyPlaces = ((GedcomPlaceTableModel) placeTable.getModel()).getValueAt(rowIndex);
                    PlaceEditorPanel placesEditorPanel = new PlaceEditorPanel();
                    DialogDescriptor placesEditorPanelDescriptor = new DialogDescriptor(
                            placesEditorPanel,
                            NbBundle.getMessage(PlaceEditorPanel.class, "PlaceEditorPanel.edit.title"),
                            true,
                            null);
                    placesEditorPanel.set(gedcom, propertyPlaces);
                    Dialog dialog = DialogDisplayer.getDefault().createDialog(placesEditorPanelDescriptor);
                    dialog.setVisible(true);
                    dialog.toFront();
                    if (placesEditorPanelDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
                        placesEditorPanel.commit();
                        updateGedcomPlaceTable();
                    }
                }
            }
        });

        // Set sorter 
        // FL: 2016-02-28 : for some unknown reason, default row sorter sorts strings excluding spaces... Using string sorter below solves the issue.
        // Returning getColumnClass as String does not solve the issue (!?!?)
        placeTableSorter = new TableRowSorter<TableModel>(placeTable.getModel());
        Comparator strComparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
            }
        };
        for (int c = 0; c < placeTable.getModel().getColumnCount(); c++) {
            placeTableSorter.setComparator(c, strComparator);
        }
        placeTable.setRowSorter(placeTableSorter);
        
        placeTable.setID(PlacesListTopComponent.class.getName());

    }

    private void updateGedcomPlaceTable() {
        List<PropertyPlace> gedcomPlacesList = GedcomUtilities.searchProperties(gedcom, PropertyPlace.class, GedcomUtilities.ENT_ALL);

        placesMap.clear();

        for (PropertyPlace propertyPlace : gedcomPlacesList) {
            Property latitude = propertyPlace.getLatitude(false);
            Property longitude = propertyPlace.getLongitude(false);

            String gedcomPlace = propertyPlace.getDisplayValue()
                    + PropertyPlace.JURISDICTION_SEPARATOR
                    + (latitude != null ? latitude.getValue() : "")
                    + PropertyPlace.JURISDICTION_SEPARATOR
                    + (longitude != null ? longitude.getValue() : "");

            Set<PropertyPlace> propertySet = placesMap.get(gedcomPlace);
            if (propertySet == null) {
                propertySet = new HashSet<PropertyPlace>();
                placesMap.put(gedcomPlace, propertySet);
            }
            propertySet.add((PropertyPlace) propertyPlace);
        }

        gedcomPlaceTableModel.update(placesMap);
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

        placeTable.setAutoCreateRowSorter(true);
        placeTable.setModel(gedcomPlaceTableModel);
        jScrollPane1.setViewportView(placeTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(searchPlaceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchPlaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterGedcomPlaceTextField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterGedcomPlaceButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearFilterGedcomPlaceButton))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchPlaceLabel)
                    .addComponent(filterGedcomPlaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterGedcomPlaceButton)
                    .addComponent(clearFilterGedcomPlaceButton)
                    .addComponent(searchPlaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearFilterGedcomPlaceButton;
    private javax.swing.JButton filterGedcomPlaceButton;
    private javax.swing.JTextField filterGedcomPlaceTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private ancestris.modules.editors.placeeditor.topcomponents.EditorTable placeTable;
    private javax.swing.JComboBox searchPlaceComboBox;
    private javax.swing.JLabel searchPlaceLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        if (gedcom != null) {
            updateGedcomPlaceTable();
        }
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
}
