package ancestris.modules.editors.placeeditor.gedcom;

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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.util.Exceptions;

/**
 *
 * @author dominique
 */
public class GedcomPlacesEditorPanel extends javax.swing.JPanel {

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
    int currentRowIndex = 0;

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
                    List<Place> findPlaces = new GeonamesPlacesList().findPlace(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 1)));
                    if (findPlaces != null) {
                        geonamePostalCodeListModel.update(findPlaces);
                    }

                }
            }
        }
    }

    private void updatePlaceEditorPanel() {
        jTextField1.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 0)));
        jTextField2.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 1)));
        jTextField3.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 2)));
        jTextField4.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 3)));
        jTextField5.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 4)));
        jTextField6.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 5)));
        jTextField7.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 6)));
        jTextField8.setText(((String) gedcomCurrentPlaceTableModel.getValueAt(currentRowIndex, 7)));
    }

    /**
     * Creates new form GedcomPlacesEditorPanel
     */
    public GedcomPlacesEditorPanel(Gedcom gedcom) {
        this.gedcom = gedcom;
        placeFormat = PropertyPlace.getFormat(gedcom);

        gedcomCompletePlaceTableModel = new GedcomPlaceTableModel(placeFormat);
        gedcomUncompletePlaceTableModel = new GedcomPlaceTableModel(placeFormat);

        initComponents();

        updateGedcomPlaceTable();

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
        searchPlaceTextField = new javax.swing.JTextField();
        searchPlaceButton = new javax.swing.JButton();
        clearFilterButton = new javax.swing.JButton();
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
        geonameSearchResultPanel = new javax.swing.JPanel();
        geonameSearchResultScrollPane = new javax.swing.JScrollPane();
        geonameSearchResultList = new javax.swing.JList();

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(searchPlaceLabel, org.openide.util.NbBundle.getMessage(GedcomPlacesEditorPanel.class, "GedcomPlacesEditorPanel.searchPlaceLabel.text")); // NOI18N

        searchPlaceTextField.setText(org.openide.util.NbBundle.getMessage(GedcomPlacesEditorPanel.class, "GedcomPlacesEditorPanel.searchPlaceTextField.text")); // NOI18N
        searchPlaceTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                searchPlaceTextFieldKeyTyped(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(searchPlaceButton, org.openide.util.NbBundle.getMessage(GedcomPlacesEditorPanel.class, "GedcomPlacesEditorPanel.searchPlaceButton.text")); // NOI18N
        searchPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchPlaceButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(clearFilterButton, org.openide.util.NbBundle.getMessage(GedcomPlacesEditorPanel.class, "GedcomPlacesEditorPanel.clearFilterButton.text")); // NOI18N
        clearFilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFilterButtonActionPerformed(evt);
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
                .addComponent(searchPlaceTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchPlaceButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearFilterButton)
                .addContainerGap())
        );
        searchPlacePanelLayout.setVerticalGroup(
            searchPlacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPlacePanelLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(searchPlacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchPlaceLabel)
                    .addComponent(searchPlaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchPlaceButton)
                    .addComponent(clearFilterButton))
                .addGap(2, 2, 2))
        );

        add(searchPlacePanel, java.awt.BorderLayout.NORTH);

        gedcomCompletePlaceTable.setAutoCreateRowSorter(true);
        gedcomCompletePlaceTable.setModel(gedcomCompletePlaceTableModel);
        gedcomCompletePlaceTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gedcomCompletePlaceTable.setShowHorizontalLines(false);
        gedcomCompletePlaceTable.setShowVerticalLines(false);
        gedcomCompletePlacesScrollPane.setViewportView(gedcomCompletePlaceTable);

        gedcomPlacesTabbedPane.addTab(org.openide.util.NbBundle.getMessage(GedcomPlacesEditorPanel.class, "GedcomPlacesEditorPanel.gedcomCompletePlacesScrollPane.TabConstraints.tabTitle"), gedcomCompletePlacesScrollPane); // NOI18N

        gedcomUncompletePlaceTable.setAutoCreateRowSorter(true);
        gedcomUncompletePlaceTable.setModel(gedcomUncompletePlaceTableModel);
        gedcomUncompletePlaceTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gedcomUncompletePlaceTable.setShowHorizontalLines(false);
        gedcomUncompletePlaceTable.setShowVerticalLines(false);
        gedcomUncompletePlacesScrollPane.setViewportView(gedcomUncompletePlaceTable);

        gedcomPlacesTabbedPane.addTab(org.openide.util.NbBundle.getMessage(GedcomPlacesEditorPanel.class, "GedcomPlacesEditorPanel.gedcomUncompletePlacesScrollPane.TabConstraints.tabTitle"), gedcomUncompletePlacesScrollPane); // NOI18N

        add(gedcomPlacesTabbedPane, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "jLabel1"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "jLabel2"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "jLabel3"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "jLabel4"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, "jLabel5"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, "jLabel6"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, "jLabel7"); // NOI18N

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

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(GedcomPlacesEditorPanel.class, "GedcomPlacesEditorPanel.cancelButton.text")); // NOI18N
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(modifiedButton, org.openide.util.NbBundle.getMessage(GedcomPlacesEditorPanel.class, "GedcomPlacesEditorPanel.modifiedButton.text")); // NOI18N
        modifiedButton.setEnabled(false);
        modifiedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifiedButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PlaceEditorPanelLayout = new javax.swing.GroupLayout(PlaceEditorPanel);
        PlaceEditorPanel.setLayout(PlaceEditorPanelLayout);
        PlaceEditorPanelLayout.setHorizontalGroup(
            PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlaceEditorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PlaceEditorPanelLayout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PlaceEditorPanelLayout.createSequentialGroup()
                                .addComponent(cancelButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modifiedButton))
                            .addGroup(PlaceEditorPanelLayout.createSequentialGroup()
                                .addComponent(jTextField8, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                                .addGap(33, 33, 33))))
                    .addGroup(PlaceEditorPanelLayout.createSequentialGroup()
                        .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PlaceEditorPanelLayout.createSequentialGroup()
                                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)))
                            .addGroup(PlaceEditorPanelLayout.createSequentialGroup()
                                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                                    .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                                    .addComponent(jTextField6, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                                    .addComponent(jTextField7, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE))))
                        .addGap(33, 33, 33)))
                .addContainerGap())
        );

        PlaceEditorPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, jLabel2, jLabel3, jLabel4, jLabel5, jLabel6, jLabel7, jLabel8});

        PlaceEditorPanelLayout.setVerticalGroup(
            PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlaceEditorPanelLayout.createSequentialGroup()
                .addContainerGap()
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cancelButton)
                    .addComponent(modifiedButton)))
        );

        add(PlaceEditorPanel, java.awt.BorderLayout.EAST);

        geonameSearchResultList.setModel(geonamePostalCodeListModel);
        geonameSearchResultScrollPane.setViewportView(geonameSearchResultList);

        javax.swing.GroupLayout geonameSearchResultPanelLayout = new javax.swing.GroupLayout(geonameSearchResultPanel);
        geonameSearchResultPanel.setLayout(geonameSearchResultPanelLayout);
        geonameSearchResultPanelLayout.setHorizontalGroup(
            geonameSearchResultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(geonameSearchResultScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
        );
        geonameSearchResultPanelLayout.setVerticalGroup(
            geonameSearchResultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(geonameSearchResultScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
        );

        add(geonameSearchResultPanel, java.awt.BorderLayout.SOUTH);
        geonameSearchResultPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GedcomPlacesEditorPanel.class, "GedcomPlacesEditorPanel.geonameSearchResultPanel.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void onTextFieldKeyType(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_onTextFieldKeyType
        cancelButton.setEnabled(true);
        modifiedButton.setEnabled(true);
    }//GEN-LAST:event_onTextFieldKeyType

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelButton.setEnabled(false);
        modifiedButton.setEnabled(false);
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

    private void searchPlaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPlaceButtonActionPerformed
        newFilter(searchPlaceTextField.getText());
    }//GEN-LAST:event_searchPlaceButtonActionPerformed

    private void clearFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFilterButtonActionPerformed
        searchPlaceTextField.setText("");
        newFilter(searchPlaceTextField.getText());
    }//GEN-LAST:event_clearFilterButtonActionPerformed

    private void searchPlaceTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchPlaceTextFieldKeyTyped
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            newFilter(searchPlaceTextField.getText());
        }
    }//GEN-LAST:event_searchPlaceTextFieldKeyTyped
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PlaceEditorPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton clearFilterButton;
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
    private javax.swing.JButton searchPlaceButton;
    private javax.swing.JLabel searchPlaceLabel;
    private javax.swing.JPanel searchPlacePanel;
    private javax.swing.JTextField searchPlaceTextField;
    // End of variables declaration//GEN-END:variables
}
