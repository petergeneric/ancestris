package ancestris.modules.editors.genealogyeditor.beans;

import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import javax.swing.JComponent;

/**
 *
 * @author dominique
 */
public class GedcomPlaceBean extends javax.swing.JPanel {

    private Property mRoot;
    private PropertyPlace mPlace;
    private String[] mPlaceFormat;
    private Property mLatitude = null;
    private Property mLongitude = null;

    /**
     * Creates new form GedcomPlaceBean
     */
    public GedcomPlaceBean() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        gedcomField0Label = new javax.swing.JLabel();
        gedcomField0TextField = new javax.swing.JTextField();
        gedcomField2Label = new javax.swing.JLabel();
        gedcomField2TextField = new javax.swing.JTextField();
        gedcomField1Label = new javax.swing.JLabel();
        gedcomField1TextField = new javax.swing.JTextField();
        gedcomField3Label = new javax.swing.JLabel();
        gedcomField3TextField = new javax.swing.JTextField();
        gedcomField4Label = new javax.swing.JLabel();
        gedcomField4TextField = new javax.swing.JTextField();
        gedcomField6Label = new javax.swing.JLabel();
        gedcomField6TextField = new javax.swing.JTextField();
        gedcomField5Label = new javax.swing.JLabel();
        gedcomField5TextField = new javax.swing.JTextField();
        gedcomField7Label = new javax.swing.JLabel();
        gedcomField7TextField = new javax.swing.JTextField();
        gedcomLatitudeLabel = new javax.swing.JLabel();
        gedcomLatitudeTextField = new javax.swing.JTextField();
        gedcomLongitudeLabel = new javax.swing.JLabel();
        gedcomLongitudeTextField = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(501, 159));
        setRequestFocusEnabled(false);

        gedcomField0Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gedcomField0Label.setText("Lieu dit"); // NOI18N

        gedcomField0TextField.setColumns(16);

        gedcomField2Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gedcomField2Label.setText("Commune"); // NOI18N

        gedcomField2TextField.setColumns(16);

        gedcomField1Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gedcomField1Label.setText("Paroisse"); // NOI18N

        gedcomField1TextField.setColumns(16);

        gedcomField3Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gedcomField3Label.setText("Code INSEE"); // NOI18N

        gedcomField3TextField.setColumns(16);

        gedcomField4Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gedcomField4Label.setText("Département"); // NOI18N

        gedcomField4TextField.setColumns(16);

        gedcomField6Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gedcomField6Label.setText("Région"); // NOI18N

        gedcomField6TextField.setColumns(16);

        gedcomField5Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gedcomField5Label.setText("Code Postal"); // NOI18N

        gedcomField5TextField.setColumns(16);

        gedcomField7Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gedcomField7Label.setText("Pays"); // NOI18N

        gedcomField7TextField.setColumns(16);

        gedcomLatitudeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gedcomLatitudeLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/beans/Bundle").getString("GedcomPlaceBean.gedcomLatitudeLabel.text"), new Object[] {})); // NOI18N

        gedcomLatitudeTextField.setColumns(16);

        gedcomLongitudeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gedcomLongitudeLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/beans/Bundle").getString("GedcomPlaceBean.gedcomLongitudeLabel.text"), new Object[] {})); // NOI18N

        gedcomLongitudeTextField.setColumns(16);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(gedcomField0Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gedcomField2Label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gedcomLatitudeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gedcomField6Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gedcomField4Label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(gedcomField6TextField)
                    .addComponent(gedcomField4TextField, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gedcomField2TextField, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gedcomField0TextField, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gedcomLatitudeTextField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(gedcomField1Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gedcomField3Label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gedcomField5Label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gedcomField7Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gedcomLongitudeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gedcomField3TextField, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                    .addComponent(gedcomField5TextField)
                    .addComponent(gedcomField7TextField)
                    .addComponent(gedcomLongitudeTextField)
                    .addComponent(gedcomField1TextField)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(gedcomField0TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(gedcomField0Label))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(gedcomField1Label)
                        .addComponent(gedcomField1TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gedcomField2Label)
                            .addComponent(gedcomField2TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gedcomField4Label)
                            .addComponent(gedcomField4TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gedcomField6Label)
                            .addComponent(gedcomField6TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gedcomLatitudeLabel)
                            .addComponent(gedcomLatitudeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gedcomField3Label)
                            .addComponent(gedcomField3TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gedcomField5Label)
                            .addComponent(gedcomField5TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gedcomField7TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(gedcomField7Label))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gedcomLongitudeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(gedcomLongitudeLabel)))))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel gedcomField0Label;
    private javax.swing.JTextField gedcomField0TextField;
    private javax.swing.JLabel gedcomField1Label;
    private javax.swing.JTextField gedcomField1TextField;
    private javax.swing.JLabel gedcomField2Label;
    private javax.swing.JTextField gedcomField2TextField;
    private javax.swing.JLabel gedcomField3Label;
    private javax.swing.JTextField gedcomField3TextField;
    private javax.swing.JLabel gedcomField4Label;
    private javax.swing.JTextField gedcomField4TextField;
    private javax.swing.JLabel gedcomField5Label;
    private javax.swing.JTextField gedcomField5TextField;
    private javax.swing.JLabel gedcomField6Label;
    private javax.swing.JTextField gedcomField6TextField;
    private javax.swing.JLabel gedcomField7Label;
    private javax.swing.JTextField gedcomField7TextField;
    private javax.swing.JLabel gedcomLatitudeLabel;
    private javax.swing.JTextField gedcomLatitudeTextField;
    private javax.swing.JLabel gedcomLongitudeLabel;
    private javax.swing.JTextField gedcomLongitudeTextField;
    // End of variables declaration//GEN-END:variables

    public void Set(Property root, PropertyPlace place) {
        this.mPlace = place;
        mPlaceFormat = PropertyPlace.getFormat(place.getGedcom());

        JComponent gedcomFields[][] = {
            {gedcomField0Label, gedcomField0TextField},
            {gedcomField1Label, gedcomField1TextField},
            {gedcomField2Label, gedcomField2TextField},
            {gedcomField3Label, gedcomField3TextField},
            {gedcomField4Label, gedcomField4TextField},
            {gedcomField5Label, gedcomField5TextField},
            {gedcomField6Label, gedcomField6TextField},
            {gedcomField7Label, gedcomField7TextField}
        };

        for (int index = 0; index < mPlaceFormat.length; index++) {
            gedcomFields[index][0].setVisible(true);
            ((javax.swing.JLabel) (gedcomFields[index][0])).setText(mPlaceFormat[index]);
            gedcomFields[index][1].setVisible(true);
            ((javax.swing.JTextField) (gedcomFields[index][1])).setText(mPlace.getJurisdiction(index));
        }

        for (int index = mPlaceFormat.length; index < gedcomFields.length; index++) {
            gedcomFields[index][0].setVisible(false);
            gedcomFields[index][1].setVisible(false);
        }

        if (mPlace.getGedcom().getGrammar().getVersion().equals("5.5.1")) {
            Property map = mPlace.getProperty("MAP");
            if (map != null) {
                mLatitude = map.getProperty("LATI");
                mLongitude = map.getProperty("LONG");
            }
        } else {
            Property map = mPlace.getProperty("_MAP");
            if (map != null) {
                mLatitude = map.getProperty("_LATI");
                mLongitude = map.getProperty("_LONG");
            }
        }

        if (mLatitude != null && mLongitude != null) {
            gedcomLatitudeTextField.setText(mLatitude.getValue());
            gedcomLongitudeTextField.setText(mLongitude.getValue());
        }
    }

    public Double getLatitude() {
        if (mLatitude != null && mLongitude != null) {
            return new Double(mLatitude.getValue());
        } else {
            return null;
        }
    }

    public Double getLongitude() {
        if (mLatitude != null && mLongitude != null) {
            return new Double(mLongitude.getValue());
        } else {
            return null;
        }
    }
}
