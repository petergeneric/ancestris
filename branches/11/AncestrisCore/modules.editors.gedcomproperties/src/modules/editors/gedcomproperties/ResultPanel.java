/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package modules.editors.gedcomproperties;

import javax.swing.DefaultComboBoxModel;
import modules.editors.gedcomproperties.utils.GedcomPlacesConverter;
import modules.editors.gedcomproperties.utils.GedcomVersionConverter;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class ResultPanel extends javax.swing.JPanel {
    
    // Version elements
    boolean displayVersion1 = false, displayVersion2 = false, displayVersion3 = false;
    String[] listVersion1, listVersion2, listVersion3;
    
    // Places elements
    boolean displayPlaces = false;
    String[] listPlaces1;

    /**
     * Creates new form ResultJPanel
     */
    public ResultPanel(String message, GedcomVersionConverter versionConverter, GedcomPlacesConverter placesConverter) {
        
        initComponents();
        jLabel1.setText(message);

        
        
        // Version
        if (versionConverter == null) {
            displayVersion1 = false;
            displayVersion2 = false;
            displayVersion3 = false;
        } else {
            listVersion1 = versionConverter.getInvalidPropsInvalidTags();
            listVersion2 = versionConverter.getInvalidPropsMultipleTags();
            listVersion3 = versionConverter.getInvalidPropsMissingTags();
            
            displayVersion1 = listVersion1 != null && listVersion1.length > 0;
            displayVersion2 = listVersion2 != null && listVersion2.length > 0;
            displayVersion3 = listVersion3 != null && listVersion3.length > 0;
            
            jLabel3.setText(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_ListOfInvalidTags", displayVersion1 ? listVersion1.length : 0));
            jLabel4.setText(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_ListOfInvalidMultiples", displayVersion2 ? listVersion2.length : 0));
            jLabel5.setText(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_ListOfMissingTags", displayVersion3 ? listVersion3.length : 0));
            
            if (displayVersion1) {
                jComboBox2.setModel(new DefaultComboBoxModel(listVersion1));
            }
            if (displayVersion2) {
                jComboBox3.setModel(new DefaultComboBoxModel(listVersion2));
            }
            if (displayVersion3) {
                jComboBox4.setModel(new DefaultComboBoxModel(listVersion3));
            }
        }
        jLabel3.setVisible(displayVersion1);
        jComboBox2.setVisible(displayVersion1);
        jLabel4.setVisible(displayVersion2);
        jComboBox3.setVisible(displayVersion2);
        jLabel5.setVisible(displayVersion3);
        jComboBox4.setVisible(displayVersion3);
        
        
        
        // Places
        if (placesConverter == null) {
            displayPlaces = false;
        } else {
            listPlaces1 = placesConverter.getIncorrectPlaces();
            displayPlaces = placesConverter.isWithError() && listPlaces1 != null && listPlaces1.length > 0;
            jLabel2.setText(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_ListOfIncorrectPlaces", displayPlaces ? listPlaces1.length : 0));
            if (displayPlaces) {
                jComboBox1.setModel(new DefaultComboBoxModel(listPlaces1));
            }
        }
        jLabel2.setVisible(displayPlaces);
        jComboBox1.setVisible(displayPlaces);
        
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ResultPanel.class, "ResultPanel.jLabel1.text")); // NOI18N

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ResultPanel.class, "ResultPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ResultPanel.class, "ResultPanel.jLabel3.text")); // NOI18N

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ResultPanel.class, "ResultPanel.jLabel4.text")); // NOI18N

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ResultPanel.class, "ResultPanel.jLabel5.text")); // NOI18N

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox3, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox4, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(179, 179, 179))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    // End of variables declaration//GEN-END:variables
}
