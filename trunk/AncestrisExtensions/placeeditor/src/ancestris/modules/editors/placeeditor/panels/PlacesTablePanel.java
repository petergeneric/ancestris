package ancestris.modules.editors.placeeditor.panels;

import ancestris.api.place.Place;
import ancestris.modules.editors.placeeditor.models.GedcomPlaceTableModel;
import ancestris.modules.editors.placeeditor.models.GeonamePostalCodeListModel;
import ancestris.modules.gedcom.utilities.GedcomUtilities;
import ancestris.place.geonames.GeonamesPlacesList;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyPlace;
import genj.gedcom.UnitOfWork;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.util.Exceptions;

/**
 *
 * @author dominique
 */
public class PlacesTablePanel extends javax.swing.JPanel {

    Gedcom gedcom;
    GedcomPlaceTableModel gedcomCompletePlaceTableModel;
    GedcomPlaceTableModel gedcomUncompletePlaceTableModel;
    Map<String, Set<PropertyPlace>> gedcomCompletePlacesMap = new HashMap<String, Set<PropertyPlace>>();
    Map<String, Set<PropertyPlace>> gedcomUncompletePlacesMap = new HashMap<String, Set<PropertyPlace>>();
    GedcomPlaceTableModel gedcomCurrentPlaceTableModel;
    GeonamePostalCodeListModel geonamePostalCodeListModel = new GeonamePostalCodeListModel();
    TableRowSorter<TableModel> completePlaceTableSorter;
    TableRowSorter<TableModel> uncompletePlaceTableSorter;
    String[] placeFormat;
    int currentRowIndex = -1;

    private class GedcomPlaceTableRowSelectionHandler implements ListSelectionListener {

        JTable gedcomPlaceTable;

        private GedcomPlaceTableRowSelectionHandler(JTable gedcomPlaceTable) {
            this.gedcomPlaceTable = gedcomPlaceTable;
        }

        @Override
        public void valueChanged(ListSelectionEvent lse) {
            ListSelectionModel lsm = (ListSelectionModel) lse.getSource();
            if (lsm.isSelectionEmpty() == false) {
                if (lse.getValueIsAdjusting() == false) {
                    gedcomCurrentPlaceTableModel = (GedcomPlaceTableModel) gedcomPlaceTable.getModel();
                    currentRowIndex = gedcomPlaceTable.convertRowIndexToModel(lsm.getLeadSelectionIndex());
                    updatePlaceEditorPanel();
                    searchPlace();
                }
            }
        }
    }

    private class JTabbedPaneSelectionChangeListener implements ChangeListener {

        public JTabbedPaneSelectionChangeListener() {
            gedcomPlacesTabbedPane.addChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            gedcomPlacesTabbedPane.getSelectedIndex();
            if (gedcomPlacesTabbedPane.getSelectedIndex() == 0) {
                gedcomCurrentPlaceTableModel = gedcomCompletePlaceTableModel;
                int selectedRow = gedcomCompletePlaceTable.getSelectedRow();
                if (selectedRow == -1) {
                    currentRowIndex = -1;
                } else {
                    currentRowIndex = gedcomCompletePlaceTable.convertRowIndexToModel(selectedRow);
                }
            } else {
                gedcomCurrentPlaceTableModel = gedcomUncompletePlaceTableModel;
                int selectedRow = gedcomUncompletePlaceTable.getSelectedRow();
                if (selectedRow == -1) {
                    currentRowIndex = -1;
                } else {
                    currentRowIndex = gedcomUncompletePlaceTable.convertRowIndexToModel(selectedRow);
                }
            }

            updatePlaceEditorPanel();
        }
    }

    private void updatePlaceEditorPanel() {
        if (currentRowIndex == -1) {
            jTextField1.setText("");
            jTextField2.setText("");
            jTextField3.setText("");
            jTextField4.setText("");
            jTextField5.setText("");
            jTextField6.setText("");
            jTextField7.setText("");
            jTextField8.setText("");
        } else {
            jTextField1.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 0)));
            jTextField2.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 1)));
            jTextField3.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 2)));
            jTextField4.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 3)));
            jTextField5.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 4)));
            jTextField6.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 5)));
            jTextField7.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 6)));
            jTextField8.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 7)));
        }

        cancelButton.setEnabled(false);
        modifiedButton.setEnabled(false);
        searchButton.setEnabled(false);
    }

    private void searchPlace() {
        String city = jTextField2.getText();
        if (city.length() > 0) {
            List<Place> findPlaces = new GeonamesPlacesList().findPlace(city);
            if (findPlaces != null) {
                geonamePostalCodeListModel.update(findPlaces);
            }
        }
    }

    /**
     * Creates new form GedcomPlacesEditorPanel
     */
    public PlacesTablePanel(Gedcom gedcom) {
        this.gedcom = gedcom;
        placeFormat = PropertyPlace.getFormat(gedcom);

        gedcomCompletePlaceTableModel = new GedcomPlaceTableModel(placeFormat);
        gedcomUncompletePlaceTableModel = new GedcomPlaceTableModel(placeFormat);

        initComponents();

        updateGedcomPlaceTable();
        JTabbedPaneSelectionChangeListener jTabbedPaneSelectionChangeListener = new JTabbedPaneSelectionChangeListener();
        if (placeFormat.length > 0) {
            jLabel1.setText(placeFormat[0]);
            jTextField1.setVisible(true);
        } else {
            jLabel1.setText("");
            jTextField1.setVisible(false);
        }

        if (placeFormat.length > 1) {
            jLabel2.setText(placeFormat[1]);
            jTextField2.setVisible(true);
        } else {
            jLabel2.setText("");
            jTextField2.setVisible(false);
        }

        if (placeFormat.length > 2) {
            jLabel3.setText(placeFormat[2]);
            jTextField3.setVisible(true);
        } else {
            jLabel3.setText("");
            jTextField3.setVisible(false);
        }

        if (placeFormat.length > 3) {
            jLabel4.setText(placeFormat[3]);
            jTextField4.setVisible(true);
        } else {
            jLabel4.setText("");
            jTextField4.setVisible(false);
        }

        if (placeFormat.length > 4) {
            jLabel5.setText(placeFormat[4]);
            jTextField5.setVisible(true);
        } else {
            jLabel5.setText("");
            jTextField5.setVisible(false);
        }

        if (placeFormat.length > 5) {
            jLabel6.setText(placeFormat[5]);
            jTextField6.setVisible(true);
        } else {
            jLabel6.setText("");
            jTextField6.setVisible(false);
        }

        if (placeFormat.length > 6) {
            jLabel7.setText(placeFormat[6]);
            jTextField7.setVisible(true);
        } else {
            jLabel7.setText("");
            jTextField7.setVisible(false);
        }

        if (placeFormat.length > 7) {
            jLabel8.setText(placeFormat[7]);
            jTextField8.setVisible(true);
        } else {
            jLabel8.setText("");
            jTextField8.setVisible(false);
        }

        completePlaceTableSorter = new TableRowSorter<TableModel>(gedcomCompletePlaceTable.getModel());
        uncompletePlaceTableSorter = new TableRowSorter<TableModel>(gedcomUncompletePlaceTable.getModel());
        /*
         List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
         int index = placeFormat.length;
         while (index > 0) {
         sortKeys.add(new RowSorter.SortKey(--index, SortOrder.ASCENDING));
         }
         completePlaceTableSorter.setSortKeys(sortKeys);
         uncompletePlaceTableSorter.setSortKeys(sortKeys);
         */
        gedcomCompletePlaceTable.setRowSorter(completePlaceTableSorter);
        gedcomUncompletePlaceTable.setRowSorter(uncompletePlaceTableSorter);

        gedcomCompletePlaceTable.getSelectionModel().addListSelectionListener(new GedcomPlaceTableRowSelectionHandler(gedcomCompletePlaceTable));
        gedcomUncompletePlaceTable.getSelectionModel().addListSelectionListener(new GedcomPlaceTableRowSelectionHandler(gedcomUncompletePlaceTable));
    }

    private void updateGedcomPlaceTable() {
        List<PropertyPlace> gedcomPlacesList = GedcomUtilities.searchProperties(gedcom, PropertyPlace.class, GedcomUtilities.ENT_ALL);

        gedcomUncompletePlacesMap.clear();
        gedcomCompletePlacesMap.clear();

        for (PropertyPlace propertyPlace : gedcomPlacesList) {
            boolean uncomplete = false;
            for (String jurisdiction : propertyPlace.getJurisdictions()) {
                if (jurisdiction.isEmpty()) {
                    // incomplete juridiction
                    uncomplete = true;
                    break;
                }
            }
            String gedcomPlace = propertyPlace.getDisplayValue();

            if (uncomplete) {
                Set<PropertyPlace> propertySet = gedcomUncompletePlacesMap.get(gedcomPlace);
                if (propertySet == null) {
                    propertySet = new HashSet<PropertyPlace>();
                    gedcomUncompletePlacesMap.put(gedcomPlace, propertySet);
                }
                propertySet.add((PropertyPlace) propertyPlace);
            } else {
                Set<PropertyPlace> propertySet = gedcomCompletePlacesMap.get(gedcomPlace);
                if (propertySet == null) {
                    propertySet = new HashSet<PropertyPlace>();
                    gedcomCompletePlacesMap.put(gedcomPlace, propertySet);
                }
                propertySet.add((PropertyPlace) propertyPlace);
            }
        }

        gedcomCompletePlaceTableModel.update(gedcomCompletePlacesMap);
        gedcomUncompletePlaceTableModel.update(gedcomUncompletePlacesMap);

    }

    private void newFilter(String filter) {
        RowFilter<TableModel, Integer> rf;
        //If current expression doesn't parse, don't update.
        try {
            rf = RowFilter.regexFilter(filter);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        completePlaceTableSorter.setRowFilter(rf);
        uncompletePlaceTableSorter.setRowFilter(rf);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchPlacePanel = new javax.swing.JPanel();
        searchPlaceLabel = new javax.swing.JLabel();
        filterGedcomPlaceTextField = new javax.swing.JTextField();
        filterGedcomPlaceButton = new javax.swing.JButton();
        clearFilterGedcomPlaceButton = new javax.swing.JButton();
        gedcomPlacesTabbedPane = new javax.swing.JTabbedPane();
        gedcomCompletePlacesScrollPane = new javax.swing.JScrollPane();
        gedcomCompletePlaceTable = new javax.swing.JTable();
        gedcomUncompletePlacesScrollPane = new javax.swing.JScrollPane();
        gedcomUncompletePlaceTable = new javax.swing.JTable();
        PlaceEditorPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        cancelButton = new javax.swing.JButton();
        modifiedButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        geonameSearchResultPanel = new javax.swing.JPanel();
        geonameSearchResultScrollPane = new javax.swing.JScrollPane();
        geonameSearchResultList = new javax.swing.JList();

        org.openide.awt.Mnemonics.setLocalizedText(searchPlaceLabel, org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.searchPlaceLabel.text")); // NOI18N

        filterGedcomPlaceTextField.setText(org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.filterGedcomPlaceTextField.text")); // NOI18N
        filterGedcomPlaceTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                filterGedcomPlaceTextFieldKeyTyped(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(filterGedcomPlaceButton, org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.filterGedcomPlaceButton.text")); // NOI18N
        filterGedcomPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterGedcomPlaceButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(clearFilterGedcomPlaceButton, org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.clearFilterGedcomPlaceButton.text")); // NOI18N
        clearFilterGedcomPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFilterGedcomPlaceButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout searchPlacePanelLayout = new javax.swing.GroupLayout(searchPlacePanel);
        searchPlacePanel.setLayout(searchPlacePanelLayout);
        searchPlacePanelLayout.setHorizontalGroup(
            searchPlacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPlacePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchPlaceLabel)
                .addGap(6, 6, 6)
                .addComponent(filterGedcomPlaceTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterGedcomPlaceButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearFilterGedcomPlaceButton)
                .addContainerGap())
        );
        searchPlacePanelLayout.setVerticalGroup(
            searchPlacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPlacePanelLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(searchPlacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchPlaceLabel)
                    .addComponent(filterGedcomPlaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterGedcomPlaceButton)
                    .addComponent(clearFilterGedcomPlaceButton))
                .addGap(2, 2, 2))
        );

        gedcomCompletePlaceTable.setAutoCreateRowSorter(true);
        gedcomCompletePlaceTable.setModel(gedcomCompletePlaceTableModel);
        gedcomCompletePlaceTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gedcomCompletePlaceTable.setShowHorizontalLines(false);
        gedcomCompletePlaceTable.setShowVerticalLines(false);
        gedcomCompletePlacesScrollPane.setViewportView(gedcomCompletePlaceTable);

        gedcomPlacesTabbedPane.addTab(org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.gedcomCompletePlacesScrollPane.TabConstraints.tabTitle"), gedcomCompletePlacesScrollPane); // NOI18N

        gedcomUncompletePlaceTable.setAutoCreateRowSorter(true);
        gedcomUncompletePlaceTable.setModel(gedcomUncompletePlaceTableModel);
        gedcomUncompletePlaceTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gedcomUncompletePlaceTable.setShowHorizontalLines(false);
        gedcomUncompletePlaceTable.setShowVerticalLines(false);
        gedcomUncompletePlacesScrollPane.setViewportView(gedcomUncompletePlaceTable);

        gedcomPlacesTabbedPane.addTab(org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.gedcomUncompletePlacesScrollPane.TabConstraints.tabTitle"), gedcomUncompletePlacesScrollPane); // NOI18N

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "jLabel1"); // NOI18N

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "jLabel2"); // NOI18N

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "jLabel3"); // NOI18N

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "jLabel4"); // NOI18N

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, "jLabel5"); // NOI18N

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, "jLabel6"); // NOI18N

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, "jLabel7"); // NOI18N

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, "jLabel8"); // NOI18N

        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                onTextFieldKeyType(evt);
            }
        });

        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                onTextFieldKeyType(evt);
            }
        });

        jTextField3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                onTextFieldKeyType(evt);
            }
        });

        jTextField4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                onTextFieldKeyType(evt);
            }
        });

        jTextField5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                onTextFieldKeyType(evt);
            }
        });

        jTextField6.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                onTextFieldKeyType(evt);
            }
        });

        jTextField7.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                onTextFieldKeyType(evt);
            }
        });

        jTextField8.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                onTextFieldKeyType(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.cancelButton.text")); // NOI18N
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(modifiedButton, org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.modifiedButton.text")); // NOI18N
        modifiedButton.setEnabled(false);
        modifiedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifiedButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.searchButton.text")); // NOI18N
        searchButton.setEnabled(false);
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PlaceEditorPanelLayout = new javax.swing.GroupLayout(PlaceEditorPanel);
        PlaceEditorPanel.setLayout(PlaceEditorPanelLayout);
        PlaceEditorPanelLayout.setHorizontalGroup(
            PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PlaceEditorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(PlaceEditorPanelLayout.createSequentialGroup()
                        .addGap(0, 61, Short.MAX_VALUE)
                        .addComponent(searchButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modifiedButton))
                    .addGroup(PlaceEditorPanelLayout.createSequentialGroup()
                        .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField1)
                            .addComponent(jTextField2)
                            .addComponent(jTextField3)
                            .addComponent(jTextField4)
                            .addComponent(jTextField5)
                            .addComponent(jTextField6)
                            .addComponent(jTextField7)
                            .addComponent(jTextField8))))
                .addContainerGap())
        );
        PlaceEditorPanelLayout.setVerticalGroup(
            PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PlaceEditorPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(modifiedButton)
                    .addComponent(searchButton))
                .addContainerGap())
        );

        geonameSearchResultPanel.setLayout(new javax.swing.BoxLayout(geonameSearchResultPanel, javax.swing.BoxLayout.LINE_AXIS));

        geonameSearchResultList.setModel(geonamePostalCodeListModel);
        geonameSearchResultScrollPane.setViewportView(geonameSearchResultList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(geonameSearchResultPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchPlacePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(geonameSearchResultScrollPane, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gedcomPlacesTabbedPane))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(PlaceEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(searchPlacePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(PlaceEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(gedcomPlacesTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(geonameSearchResultScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(252, 252, 252)
                        .addComponent(geonameSearchResultPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35))))
        );

        geonameSearchResultPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "GedcomPlacesEditorPanel.geonameSearchResultPanel.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void onTextFieldKeyType(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_onTextFieldKeyType
        cancelButton.setEnabled(true);
        modifiedButton.setEnabled(true);
        searchButton.setEnabled(true);
    }//GEN-LAST:event_onTextFieldKeyType

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        updatePlaceEditorPanel();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void modifiedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifiedButtonActionPerformed
        cancelButton.setEnabled(false);
        modifiedButton.setEnabled(false);
        final Set<PropertyPlace> propertyPlaces = gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex);
        String propertyPlaceString = "";
        if (placeFormat.length > 0) {
            propertyPlaceString = jTextField1.getText();
        }

        if (placeFormat.length > 1) {
            propertyPlaceString += PropertyPlace.JURISDICTION_SEPARATOR;
            propertyPlaceString += jTextField2.getText();
        }

        if (placeFormat.length > 2) {
            propertyPlaceString += PropertyPlace.JURISDICTION_SEPARATOR;
            propertyPlaceString += jTextField3.getText();
        }

        if (placeFormat.length > 3) {
            propertyPlaceString += PropertyPlace.JURISDICTION_SEPARATOR;
            propertyPlaceString += jTextField4.getText();
        }

        if (placeFormat.length > 4) {
            propertyPlaceString += PropertyPlace.JURISDICTION_SEPARATOR;
            propertyPlaceString += jTextField5.getText();
        }

        if (placeFormat.length > 5) {
            propertyPlaceString += PropertyPlace.JURISDICTION_SEPARATOR;
            propertyPlaceString += jTextField6.getText();
        }

        if (placeFormat.length > 6) {
            propertyPlaceString += PropertyPlace.JURISDICTION_SEPARATOR;
            propertyPlaceString += jTextField7.getText();
        }

        if (placeFormat.length > 7) {
            propertyPlaceString += PropertyPlace.JURISDICTION_SEPARATOR;
            propertyPlaceString += jTextField8.getText();
        }

        try {
            final String tmp = propertyPlaceString;
            gedcom.doUnitOfWork(new UnitOfWork() {
                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    for (PropertyPlace propertyPlace : propertyPlaces) {
                        propertyPlace.setValue(tmp);
                    }
                }
            }); // end of doUnitOfWork
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }

        updateGedcomPlaceTable();
    }//GEN-LAST:event_modifiedButtonActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        searchPlace();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void clearFilterGedcomPlaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFilterGedcomPlaceButtonActionPerformed
        filterGedcomPlaceTextField.setText("");
        newFilter(filterGedcomPlaceTextField.getText());
    }//GEN-LAST:event_clearFilterGedcomPlaceButtonActionPerformed

    private void filterGedcomPlaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterGedcomPlaceButtonActionPerformed
        newFilter(filterGedcomPlaceTextField.getText());
    }//GEN-LAST:event_filterGedcomPlaceButtonActionPerformed

    private void filterGedcomPlaceTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterGedcomPlaceTextFieldKeyTyped
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            newFilter(filterGedcomPlaceTextField.getText());
        }
    }//GEN-LAST:event_filterGedcomPlaceTextFieldKeyTyped

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PlaceEditorPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton clearFilterGedcomPlaceButton;
    private javax.swing.JButton filterGedcomPlaceButton;
    private javax.swing.JTextField filterGedcomPlaceTextField;
    private javax.swing.JTable gedcomCompletePlaceTable;
    private javax.swing.JScrollPane gedcomCompletePlacesScrollPane;
    private javax.swing.JTabbedPane gedcomPlacesTabbedPane;
    private javax.swing.JTable gedcomUncompletePlaceTable;
    private javax.swing.JScrollPane gedcomUncompletePlacesScrollPane;
    private javax.swing.JList geonameSearchResultList;
    private javax.swing.JPanel geonameSearchResultPanel;
    private javax.swing.JScrollPane geonameSearchResultScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JButton modifiedButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JLabel searchPlaceLabel;
    private javax.swing.JPanel searchPlacePanel;
    // End of variables declaration//GEN-END:variables
}
