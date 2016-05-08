package ancestris.modules.editors.geoplace;

import ancestris.api.place.Place;
import ancestris.api.place.PlaceFactory;
import ancestris.modules.place.geonames.GeonamesPlacesList;
import genj.gedcom.*;
import genj.util.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

/**
 * This class is the base element of the Place Format editor for any gedcom.
 * 
 * - It supports any length of gedcom place format found
 * - Labels of fields appear as they are in the gedcom file
 * - Changing the place format would need to use GedcomProperties
 * 
 * - Conversion used for geonames is a string "map" of numbers separated by commas. Its use is : gedcomPlaceFormat[i] = geonamesPlaceFormat[map[i]]
 * - For gedcom with different languages, user has the ability to set corresponding local translation (TODO)
 *
 * @author Frederic
 */
public class GedcomPlaceEditorPanel extends javax.swing.JPanel {

    private final static Logger logger = Logger.getLogger(GedcomPlaceEditorPanel.class.getName(), null);
    
    private Registry registry = null;
    
    private Gedcom gedcom;
    private String[] gedcomPlaceFormat;
    private JComponent[][] gedcomFields;

    private PropertyPlace mPlace;
    
    boolean placeModified = false;
    boolean updateOnGoing = false;

    
    
    /**
     * Creates form GedcomPlaceEditorPanel
     */
    public GedcomPlaceEditorPanel() {
        registry = Registry.get(getClass());
        initComponents();
    }


    /**
     * Defines and display gedcom panel fields
     */
    private void setGedcomPanel() {
        
        // Read place format from Gedcom, else falls back to saved Ancestris user preferences, else falls back to bundle
        gedcomPlaceFormat = PropertyPlace.getFormat(gedcom, true);
        
        // Defines corresponding gedcom fields
        gedcomFields = new JComponent[2][gedcomPlaceFormat.length];
        for (int i = 0; i < gedcomPlaceFormat.length; i++) {
            String label = gedcomPlaceFormat[i];
            gedcomFields[0][i] = new JLabel(label);
            gedcomFields[1][i] = new JTextField();
        }

        // Prepare panel to add gedcom fields in the fields panel
        javax.swing.GroupLayout placeFieldsPanelLayout = new javax.swing.GroupLayout(placeFieldsPanel);
        placeFieldsPanel.setLayout(placeFieldsPanelLayout);

        
        // Create Horizontal Groups
        Group groupLabels = placeFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
        Group groupTextFields = placeFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
        for (int i = 0; i < gedcomPlaceFormat.length; i++) {
            groupLabels.addComponent(gedcomFields[0][i]);
            groupTextFields.addComponent(gedcomFields[1][i]);
        }
        GroupLayout.SequentialGroup groupColumns = placeFieldsPanelLayout.createSequentialGroup();
        groupColumns.addGroup(groupLabels);
        groupColumns.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        groupColumns.addGroup(groupTextFields);

        placeFieldsPanelLayout.setHorizontalGroup(placeFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(groupColumns));
        
        // Create and add Vertical Groups
        Group groupLine[] = new Group[gedcomPlaceFormat.length];
        for (int i = 0; i < gedcomPlaceFormat.length; i++) {
            groupLine[i] = placeFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE);
            groupLine[i].addComponent(gedcomFields[0][i]);
            groupLine[i].addComponent(gedcomFields[1][i]);
        }
        GroupLayout.SequentialGroup groupRows = placeFieldsPanelLayout.createSequentialGroup();
        for (int i = 0; i < gedcomPlaceFormat.length; i++) {
            groupRows.addGroup(groupLine[i]);
            groupRows.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        }
        groupRows.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        placeFieldsPanelLayout.setVerticalGroup(placeFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(groupRows));

        
        // Define document listener for easy autocompletion of fields
        for (int i = 0; i < gedcomPlaceFormat.length; i++) {
            final int index = i;
            final JTextField jtf = (JTextField) gedcomFields[1][i];
            
            List<String> jurisdictions = Arrays.asList(PropertyPlace.getAllJurisdictions(getGedcom(), i, true));
            if (jurisdictions == null) {
                jurisdictions = new ArrayList<String>();
            }
            AutoCompleteDecorator.decorate(jtf, jurisdictions, false);
            jtf.getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_BACK_SPACE, 0), DefaultEditorKit.deletePrevCharAction);
            
            
            jtf.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateNextFields(index);
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    placeModified = true;
                }
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateNextFields(index);
                }
                
                private void updateNextFields(int index) {
                    if (!updateOnGoing) {
                        placeModified = true;
                        PropertyPlace[] sameChoices = PropertyPlace.getSameChoices(getGedcom(), index, jtf.getText());
                        if (sameChoices.length > 0) {
                            updatePlace(sameChoices[0], index + 1);
                        } else {
                            updatePlace(null, index + 1);
                        }
                    }
                }
            });
        }
        
        
        
    }
    
    
    
    
    /**
     * @param place the place to set
     */
    public void set(Gedcom gedcom, PropertyPlace place) {
        this.gedcom = gedcom;
        this.mPlace = place;
        
        // Design panel based on read gedcom fields and its corresponding mapping
        if (gedcomFields == null) {
            setGedcomPanel();
        }
        
        // Fill in fields
        updatePlace(mPlace, 0);
    }


    
    private void updatePlace(PropertyPlace place, int startIndex) {
        updatePlace(new PlaceFactory(place), startIndex, true);
    }
    
    public void updatePlace(Place place, int startIndex, boolean forceReplace) {
        updateOnGoing = true;

        if (place != null) {
            // Fill in place jurisdictions in an order which depends on the parameters for a PlaceFactory and on the geonames order for a geoname
            logger.log(Level.FINE, "startIndex {0}", new Object[] { startIndex });
            String[] map = null;
            if (place instanceof PlaceFactory) {
                map = new String[gedcomPlaceFormat.length];
                for (int i = 0 ; i < gedcomPlaceFormat.length ; i++) {
                    map[i] = ""+i;
                } 
            } else {
                String mapStr = registry.get(gedcom.getName() + ".geonamesPlaceConversionMap", "");
                if (mapStr.isEmpty()) {
                    map = getGeonamesMap();
                } else {
                    map = PropertyPlace.getFormat(mapStr);
                }
                if (map == null) {
                    updateOnGoing = false;
                    return;
                }
            }
            for (int i = startIndex; i < gedcomPlaceFormat.length; i++) {
                JTextField jtf = ((javax.swing.JTextField) (gedcomFields[1][i]));
                String j = map[i].trim();
                if (forceReplace || jtf.getText().isEmpty()) {
                    String str = j.isEmpty() ? "" : place.getJurisdiction(Integer.valueOf(j));
                    jtf.setText(str == null ? "" : str);
                }
            }

            // Fill in geocoordinates
            Double latitude = place.getLatitude();
            if (forceReplace || gedcomLatitudeTextField.getText().isEmpty()) {
                if (latitude != null) {
                    gedcomLatitudeTextField.setText(String.valueOf(latitude));
                } else {
                    gedcomLatitudeTextField.setText("");
                }
            }
            Double longitude = place.getLongitude();
            if (forceReplace || gedcomLongitudeTextField.getText().isEmpty()) {
                if (longitude != null) {
                    gedcomLongitudeTextField.setText(String.valueOf(longitude));
                } else {
                    gedcomLongitudeTextField.setText("");
                }
            }
        } else {
            // Empty every field
            logger.log(Level.FINE, "No place found startIndex {0}", new Object[] { startIndex });
            for (int index = startIndex; index < gedcomPlaceFormat.length; index++) {
                ((JTextField) (gedcomFields[1][index])).setText("");
            }
            gedcomLatitudeTextField.setText("");
            gedcomLongitudeTextField.setText("");
        }
        
        updateOnGoing = false;
    }

    public void modifyCoordinates(String lat, String lon, boolean forceReplace) {
        if (forceReplace || gedcomLatitudeTextField.getText().isEmpty()) {
            gedcomLatitudeTextField.setText(lat);
        }
        if (forceReplace || gedcomLongitudeTextField.getText().isEmpty()) {
            gedcomLongitudeTextField.setText(lon);
        }
    }

    
    
    
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        placeFieldsPanel = new javax.swing.JPanel();
        gedcomLongitudeTextField = new javax.swing.JTextField();
        gedcomLatitudeTextField = new javax.swing.JTextField();
        latitudeLabel = new javax.swing.JLabel();
        longitudeLabel = new javax.swing.JLabel();
        parametersLabel = new javax.swing.JLabel();
        parametersButton = new javax.swing.JButton();

        javax.swing.GroupLayout placeFieldsPanelLayout = new javax.swing.GroupLayout(placeFieldsPanel);
        placeFieldsPanel.setLayout(placeFieldsPanelLayout);
        placeFieldsPanelLayout.setHorizontalGroup(
            placeFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        placeFieldsPanelLayout.setVerticalGroup(
            placeFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 66, Short.MAX_VALUE)
        );

        gedcomLongitudeTextField.setColumns(16);
        gedcomLongitudeTextField.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPlaceEditorPanel.class, "RightClicOnMap")); // NOI18N
        gedcomLongitudeTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!updateOnGoing) {
                    placeModified = true;
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                placeModified = true;
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!updateOnGoing) {
                    placeModified = true;
                }
            }
        });

        gedcomLatitudeTextField.setColumns(16);
        gedcomLatitudeTextField.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomPlaceEditorPanel.class, "RightClicOnMap")); // NOI18N
        gedcomLatitudeTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!updateOnGoing) {
                    placeModified = true;
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                placeModified = true;
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!updateOnGoing) {
                    placeModified = true;
                }
            }
        });

        latitudeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/geoplace/resources/latitude.png"))); // NOI18N
        latitudeLabel.setText(org.openide.util.NbBundle.getMessage(GedcomPlaceEditorPanel.class, "GedcomPlaceEditorPanel.latitudeLabel.text")); // NOI18N

        longitudeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/geoplace/resources/longitude.png"))); // NOI18N
        longitudeLabel.setText(org.openide.util.NbBundle.getMessage(GedcomPlaceEditorPanel.class, "GedcomPlaceEditorPanel.longitudeLabel.text")); // NOI18N

        parametersLabel.setText(org.openide.util.NbBundle.getMessage(GedcomPlaceEditorPanel.class, "GedcomPlaceEditorPanel.parametersLabel.text")); // NOI18N

        parametersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/geoplace/resources/parameters.png"))); // NOI18N
        parametersButton.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/geoplace/Bundle").getString("GedcomPlaceEditorPanel.parametersButton.text"), new Object[] {})); // NOI18N
        parametersButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/geoplace/Bundle").getString("GedcomPlaceEditorPanel.parametersButton.toolTipText"), new Object[] {})); // NOI18N
        parametersButton.setFocusable(false);
        parametersButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        parametersButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        parametersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parametersButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(gedcomLatitudeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(latitudeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(gedcomLongitudeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(longitudeLabel))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(parametersLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(parametersButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(placeFieldsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(placeFieldsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(latitudeLabel)
                    .addComponent(longitudeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gedcomLatitudeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gedcomLongitudeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(parametersLabel)
                    .addComponent(parametersButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void parametersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parametersButtonActionPerformed
        getGeonamesMap();
    }//GEN-LAST:event_parametersButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField gedcomLatitudeTextField;
    private javax.swing.JTextField gedcomLongitudeTextField;
    private javax.swing.JLabel latitudeLabel;
    private javax.swing.JLabel longitudeLabel;
    private javax.swing.JButton parametersButton;
    private javax.swing.JLabel parametersLabel;
    private javax.swing.JPanel placeFieldsPanel;
    // End of variables declaration//GEN-END:variables

    
    
    private String[] getGeonamesMap() {
        return GeonamesPlacesList.getGeonamesMap(getClass(), gedcom);
    }
    
    
    
    
    
    public String getPlaceString() {
        if (mPlace == null) {
            return "";
        }
        return getPlaceString(mPlace.getCityIndex());
    }

    public String getPlaceString(int startingFrom) {

        String placeString = "";

        for (int i = 0; i < gedcomPlaceFormat.length; i++) {
            if (i > 0) {
                placeString += PropertyPlace.JURISDICTION_SEPARATOR;
            }
            placeString += ((JTextField) (gedcomFields[1][i])).getText();
        }

        return placeString;
    }

    
    
    private Gedcom getGedcom(){
        return gedcom;
    }

    public boolean isModified() {
        return placeModified;
    }

    public String getLatitude() {
        return gedcomLatitudeTextField.getText();
    }

    public String getLongitude() {
        return gedcomLongitudeTextField.getText();
    }

}
