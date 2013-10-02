package ancestris.modules.editors.placeeditor.panels;

import ancestris.api.place.Place;
import ancestris.modules.editors.placeeditor.models.GeonamePostalCodeListModel;
import ancestris.place.geonames.GeonamesPlacesList;
import genj.gedcom.PropertyPlace;
import java.util.List;

/**
 *
 * @author dominique
 */
public class PlacesEditorPanel extends javax.swing.JPanel {

    GeonamePostalCodeListModel geonamePostalCodeListModel = new GeonamePostalCodeListModel();
    String[] placeFormat;
    PropertyPlace propertyPlace;

    private void searchPlace(String city) {
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
    public PlacesEditorPanel(String[] placeFormat, PropertyPlace propertyPlace) {

        this.placeFormat = placeFormat;
        this.propertyPlace = propertyPlace;

        initComponents();

        String[] jurisdictions = propertyPlace.getJurisdictions();

        if (placeFormat.length > 0) {
            jLabel1.setText(placeFormat[0]);
            jTextField1.setVisible(true);
            jTextField1.setText(jurisdictions.length > 0 ? jurisdictions[0] : "");
        } else {
            jLabel1.setText("");
            jTextField1.setVisible(false);
        }

        if (placeFormat.length > 1) {
            jLabel2.setText(placeFormat[1]);
            jTextField2.setVisible(true);
            jTextField2.setText(jurisdictions.length > 1 ? jurisdictions[1] : "");
        } else {
            jLabel2.setText("");
            jTextField2.setVisible(false);
        }

        if (placeFormat.length > 2) {
            jLabel3.setText(placeFormat[2]);
            jTextField3.setVisible(true);
            jTextField3.setText(jurisdictions.length > 2 ? jurisdictions[2] : "");
        } else {
            jLabel3.setText("");
            jTextField3.setVisible(false);
        }

        if (placeFormat.length > 3) {
            jLabel4.setText(placeFormat[3]);
            jTextField4.setVisible(true);
            jTextField4.setText(jurisdictions.length > 3 ? jurisdictions[3] : "");
        } else {
            jLabel4.setText("");
            jTextField4.setVisible(false);
        }

        if (placeFormat.length > 4) {
            jLabel5.setText(placeFormat[4]);
            jTextField5.setVisible(true);
            jTextField5.setText(jurisdictions.length > 4 ? jurisdictions[4] : "");
        } else {
            jLabel5.setText("");
            jTextField5.setVisible(false);
        }

        if (placeFormat.length > 5) {
            jLabel6.setText(placeFormat[5]);
            jTextField6.setVisible(true);
            jTextField6.setText(jurisdictions.length > 5 ? jurisdictions[5] : "");
        } else {
            jLabel6.setText("");
            jTextField6.setVisible(false);
        }

        if (placeFormat.length > 6) {
            jLabel7.setText(placeFormat[6]);
            jTextField7.setVisible(true);
            jTextField7.setText(jurisdictions.length > 6 ? jurisdictions[6] : "");
        } else {
            jLabel7.setText("");
            jTextField7.setVisible(false);
        }

        if (placeFormat.length > 7) {
            jLabel8.setText(placeFormat[7]);
            jTextField8.setVisible(true);
            jTextField8.setText(jurisdictions.length > 7 ? jurisdictions[7] : "");
        } else {
            jLabel8.setText("");
            jTextField8.setVisible(false);
        }
    }

    public String getPlaceString() {
        String placeString = "";

        if (placeFormat.length > 0) {
            placeString = jTextField1.getText();
        }

        if (placeFormat.length > 1) {
            placeString += PropertyPlace.JURISDICTION_SEPARATOR;
            placeString += jTextField2.getText();
        }

        if (placeFormat.length > 2) {
            placeString += PropertyPlace.JURISDICTION_SEPARATOR;
            placeString += jTextField3.getText();
        }

        if (placeFormat.length > 3) {
            placeString += PropertyPlace.JURISDICTION_SEPARATOR;
            placeString += jTextField4.getText();
        }

        if (placeFormat.length > 4) {
            placeString += PropertyPlace.JURISDICTION_SEPARATOR;
            placeString += jTextField5.getText();
        }

        if (placeFormat.length > 5) {
            placeString += PropertyPlace.JURISDICTION_SEPARATOR;
            placeString += jTextField6.getText();
        }

        if (placeFormat.length > 6) {
            placeString += PropertyPlace.JURISDICTION_SEPARATOR;
            placeString += jTextField7.getText();
        }

        if (placeFormat.length > 7) {
            placeString += PropertyPlace.JURISDICTION_SEPARATOR;
            placeString += jTextField8.getText();
        }

        return placeString;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        PlaceEditorPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        PlaceReferencesPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTextField12 = new javax.swing.JTextField();

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        PlaceEditorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, "Pays"); // NOI18N

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, "Région"); // NOI18N

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Lieudit"); // NOI18N

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "Commune"); // NOI18N

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, "Département"); // NOI18N

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "Paroisse"); // NOI18N

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "Code Postal"); // NOI18N

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, "Code INSEE"); // NOI18N

        javax.swing.GroupLayout PlaceEditorPanelLayout = new javax.swing.GroupLayout(PlaceEditorPanel);
        PlaceEditorPanel.setLayout(PlaceEditorPanelLayout);
        PlaceEditorPanelLayout.setHorizontalGroup(
            PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlaceEditorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField7, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                    .addComponent(jTextField8)
                    .addComponent(jTextField6)
                    .addComponent(jTextField4)
                    .addComponent(jTextField2)
                    .addComponent(jTextField1))
                .addGap(27, 27, 27)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField3)
                    .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE))
                .addGap(53, 53, 53))
        );
        PlaceEditorPanelLayout.setVerticalGroup(
            PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlaceEditorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField1)
                        .addComponent(jLabel3)
                        .addComponent(jLabel1))
                    .addComponent(jTextField3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlaceEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/placeeditor/panels/Bundle").getString("PlacesEditorPanel.PlaceEditorPanel.TabConstraints.tabTitle"), new Object[] {}), PlaceEditorPanel); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout PlaceReferencesPanelLayout = new javax.swing.GroupLayout(PlaceReferencesPanel);
        PlaceReferencesPanel.setLayout(PlaceReferencesPanelLayout);
        PlaceReferencesPanelLayout.setHorizontalGroup(
            PlaceReferencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlaceReferencesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                .addContainerGap())
        );
        PlaceReferencesPanelLayout.setVerticalGroup(
            PlaceReferencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlaceReferencesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/placeeditor/panels/Bundle").getString("PlacesEditorPanel.PlaceReferencesPanel.TabConstraints.tabTitle"), new Object[] {}), PlaceReferencesPanel); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/placeeditor/panels/Bundle").getString("PlacesEditorPanel.jLabel9.text"), new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/placeeditor/panels/Bundle").getString("PlacesEditorPanel.jLabel10.text"), new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/placeeditor/panels/Bundle").getString("PlacesEditorPanel.jLabel11.text"), new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/placeeditor/panels/Bundle").getString("PlacesEditorPanel.jLabel12.text"), new Object[] {})); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField9)
                        .addGap(115, 115, 115))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField12, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField10, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(28, 28, 28)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField11)
                        .addGap(35, 35, 35))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel10)
                        .addComponent(jLabel11)
                        .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PlaceEditorPanel;
    private javax.swing.JPanel PlaceReferencesPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables
}
