package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.api.place.Place;
import ancestris.modules.editors.genealogyeditor.models.GeonamePlacesListModel;
import ancestris.modules.place.geonames.GeonamesPlacesList;
import genj.gedcom.*;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.openide.util.*;

/**
 *
 * @author dominique
 */
public class PlaceEditorPanel extends javax.swing.JPanel {

    private final static Logger logger = Logger.getLogger(PlaceEditorPanel.class.getName(), null);
    private PropertyPlace mPlace;
    private GeonamePlacesListModel geonamePlacesListModel = new GeonamePlacesListModel();
    private Property mAddress;

    /**
     * Creates new form GedcomPlacesEditorPanel
     */
    public PlaceEditorPanel() {
        initComponents();
        jXMapKit1.setDataProviderCreditShown(true);
        jXMapKit1.getMainMap().setRecenterOnClickEnabled(true);
        jXMapKit1.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps);
        jXMapKit1.setMiniMapVisible(false);
        jXMapKit1.getZoomSlider().setValue(5);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        editorsTabbedPane = new javax.swing.JTabbedPane();
        placeEditorTabPanel = new javax.swing.JPanel();
        placeEditorTabbedPane = new javax.swing.JTabbedPane();
        mapPanel = new javax.swing.JPanel();
        MapScrollPane = new javax.swing.JScrollPane();
        jXMapKit1 = new org.jdesktop.swingx.JXMapKit();
        searchPlacePanel = new javax.swing.JPanel();
        searchPlaceTextField = new javax.swing.JTextField();
        searchPlaceButton = new javax.swing.JButton();
        geonamesScrollPane = new javax.swing.JScrollPane();
        geonamesPlacesList = new javax.swing.JList<String>();
        gedcomPlaceEditorPanel = new ancestris.modules.editors.genealogyeditor.panels.GedcomPlaceEditorPanel();
        addressEditorTabPanel = new javax.swing.JPanel();
        addressEditorPanel = new ancestris.modules.editors.genealogyeditor.panels.AddressEditorPanel();

        setMinimumSize(new java.awt.Dimension(537, 414));

        placeEditorTabbedPane.setMinimumSize(new java.awt.Dimension(513, 263));

        MapScrollPane.setViewportView(jXMapKit1);

        javax.swing.GroupLayout mapPanelLayout = new javax.swing.GroupLayout(mapPanel);
        mapPanel.setLayout(mapPanelLayout);
        mapPanelLayout.setHorizontalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MapScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE)
        );
        mapPanelLayout.setVerticalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MapScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
        );

        placeEditorTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("PlaceEditorPanel.mapPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/map.png")), mapPanel); // NOI18N

        searchPlaceTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("PlaceEditorPanel.searchPlaceTextField.text_1"), new Object[] {})); // NOI18N
        searchPlaceTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchPlaceButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(searchPlaceButton, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("PlaceEditorPanel.searchPlaceButton.text_1"), new Object[] {})); // NOI18N
        searchPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchPlaceButtonActionPerformed(evt);
            }
        });

        geonamesPlacesList.setModel(geonamePlacesListModel);
        geonamesPlacesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        geonamesPlacesList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                geonamesPlacesListValueChanged(evt);
            }
        });
        geonamesScrollPane.setViewportView(geonamesPlacesList);

        javax.swing.GroupLayout searchPlacePanelLayout = new javax.swing.GroupLayout(searchPlacePanel);
        searchPlacePanel.setLayout(searchPlacePanelLayout);
        searchPlacePanelLayout.setHorizontalGroup(
            searchPlacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPlacePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchPlacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(searchPlacePanelLayout.createSequentialGroup()
                        .addComponent(searchPlaceTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchPlaceButton))
                    .addComponent(geonamesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 663, Short.MAX_VALUE))
                .addContainerGap())
        );
        searchPlacePanelLayout.setVerticalGroup(
            searchPlacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPlacePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchPlacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchPlaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchPlaceButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(geonamesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                .addContainerGap())
        );

        placeEditorTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("PlaceEditorPanel.searchPlacePanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Place.png")), searchPlacePanel); // NOI18N

        javax.swing.GroupLayout placeEditorTabPanelLayout = new javax.swing.GroupLayout(placeEditorTabPanel);
        placeEditorTabPanel.setLayout(placeEditorTabPanelLayout);
        placeEditorTabPanelLayout.setHorizontalGroup(
            placeEditorTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gedcomPlaceEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(placeEditorTabPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(placeEditorTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        placeEditorTabPanelLayout.setVerticalGroup(
            placeEditorTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(placeEditorTabPanelLayout.createSequentialGroup()
                .addComponent(gedcomPlaceEditorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(placeEditorTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                .addContainerGap())
        );

        editorsTabbedPane.addTab(org.openide.util.NbBundle.getMessage(PlaceEditorPanel.class, "PlaceEditorPanel.placeEditorTabPanel.TabConstraints.tabTitle"), placeEditorTabPanel); // NOI18N

        javax.swing.GroupLayout addressEditorTabPanelLayout = new javax.swing.GroupLayout(addressEditorTabPanel);
        addressEditorTabPanel.setLayout(addressEditorTabPanelLayout);
        addressEditorTabPanelLayout.setHorizontalGroup(
            addressEditorTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addressEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
        );
        addressEditorTabPanelLayout.setVerticalGroup(
            addressEditorTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addressEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
        );

        editorsTabbedPane.addTab(org.openide.util.NbBundle.getMessage(PlaceEditorPanel.class, "PlaceEditorPanel.addressEditorTabPanel.TabConstraints.tabTitle"), addressEditorTabPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(editorsTabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(editorsTabbedPane)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void searchPlaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPlaceButtonActionPerformed
        String searchedPlace = searchPlaceTextField.getText();

        if (searchedPlace.isEmpty() == false) {
            searchPlaceButton.setEnabled(false);
            geonamePlacesListModel.clear();
            GeonamesPlacesList geonamesPlacesList1 = new GeonamesPlacesList();
            geonamesPlacesList1.searchPlace(searchedPlace, geonamePlacesListModel);
            geonamesPlacesList1.getTask().addTaskListener(new TaskListener() {

                @Override
                public void taskFinished(Task task) {
                    searchPlaceButton.setEnabled(true);
                }
            });
        }
    }//GEN-LAST:event_searchPlaceButtonActionPerformed

    private void geonamesPlacesListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_geonamesPlacesListValueChanged
        if (!evt.getValueIsAdjusting()) {
            Place place = geonamePlacesListModel.getPlaceAt(geonamesPlacesList.getSelectedIndex());
            gedcomPlaceEditorPanel.setPlace(place);
            jXMapKit1.setAddressLocation(new GeoPosition(place.getLatitude(), place.getLongitude()));
        }
    }//GEN-LAST:event_geonamesPlacesListValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane MapScrollPane;
    private ancestris.modules.editors.genealogyeditor.panels.AddressEditorPanel addressEditorPanel;
    private javax.swing.JPanel addressEditorTabPanel;
    private javax.swing.JTabbedPane editorsTabbedPane;
    private ancestris.modules.editors.genealogyeditor.panels.GedcomPlaceEditorPanel gedcomPlaceEditorPanel;
    private javax.swing.JList<String> geonamesPlacesList;
    private javax.swing.JScrollPane geonamesScrollPane;
    private org.jdesktop.swingx.JXMapKit jXMapKit1;
    private javax.swing.JPanel mapPanel;
    private javax.swing.JPanel placeEditorTabPanel;
    private javax.swing.JTabbedPane placeEditorTabbedPane;
    private javax.swing.JButton searchPlaceButton;
    private javax.swing.JPanel searchPlacePanel;
    private javax.swing.JTextField searchPlaceTextField;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the place
     */
    public PropertyPlace get() {
        return mPlace;
    }

    /**
     * @param place the place to set
     */
    public void set(Property root, PropertyPlace place, Property address) {

        this.mPlace = place;
        this.mAddress = address;
        gedcomPlaceEditorPanel.set(root, mPlace);
        addressEditorPanel.set(root, mAddress);
        if (mPlace != null) {
            Property latitude = null;
            Property longitude = null;

            editorsTabbedPane.setSelectedComponent(placeEditorTabPanel);

            if (place.getGedcom().getGrammar().getVersion().equals("5.5.1")) {
                Property map = place.getProperty("MAP");
                if (map != null) {
                    latitude = map.getProperty("LATI");
                    longitude = map.getProperty("LONG");
                }
            } else {
                Property map = place.getProperty("_MAP");
                if (map != null) {
                    latitude = map.getProperty("_LATI");
                    longitude = map.getProperty("_LONG");
                }
            }

            if (latitude != null && longitude != null) {
                jXMapKit1.setAddressLocation(new GeoPosition(Double.parseDouble(latitude.getValue()), Double.parseDouble(longitude.getValue())));
            } else {
                placeEditorTabbedPane.setSelectedComponent(searchPlacePanel);

            }
        } else if (mAddress != null) {
            editorsTabbedPane.setSelectedComponent(addressEditorPanel);
            placeEditorTabbedPane.setSelectedComponent(searchPlacePanel);
        } else {
            editorsTabbedPane.setSelectedComponent(placeEditorTabPanel);
            placeEditorTabbedPane.setSelectedComponent(searchPlacePanel);
        }
    }

    public void commit() {
        addressEditorPanel.commit();
        gedcomPlaceEditorPanel.commit();
    }
}
