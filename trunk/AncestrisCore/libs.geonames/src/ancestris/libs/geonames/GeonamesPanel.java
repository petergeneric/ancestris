/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2014 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.libs.geonames;

import genj.io.FileAssociation;
import java.net.MalformedURLException;
import java.net.URL;
import org.openide.util.Exceptions;

final class GeonamesPanel extends javax.swing.JPanel {

    private final GeonamesOptionsPanelController controller;

    GeonamesPanel(GeonamesOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        // TODO listen to changes in form fields and call controller.changed()
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jtUserName = new javax.swing.JTextField();
        jButtonCreateGeoAccount = new javax.swing.JButton();
        jExplanation = new javax.swing.JLabel();
        cbPostalCodes = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GeonamesPanel.class, "GeonamesPanel.jLabel1.text")); // NOI18N

        jtUserName.setColumns(20);
        jtUserName.setText(org.openide.util.NbBundle.getMessage(GeonamesPanel.class, "GeonamesPanel.jtUserName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonCreateGeoAccount, org.openide.util.NbBundle.getMessage(GeonamesPanel.class, "GeonamesPanel.jButtonCreateGeoAccount.text")); // NOI18N
        jButtonCreateGeoAccount.setToolTipText(org.openide.util.NbBundle.getMessage(GeonamesPanel.class, "GeonamesPanel.jButtonCreateGeoAccount.toolTipText")); // NOI18N
        jButtonCreateGeoAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCreateGeoAccountActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jExplanation, org.openide.util.NbBundle.getMessage(GeonamesPanel.class, "GeonamesPanel.jExplanation.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbPostalCodes, org.openide.util.NbBundle.getMessage(GeonamesPanel.class, "GeonamesPanel.cbPostalCodes.text")); // NOI18N
        cbPostalCodes.setToolTipText(org.openide.util.NbBundle.getMessage(GeonamesPanel.class, "GeonamesPanel.cbPostalCodes.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jExplanation)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonCreateGeoAccount))
                            .addComponent(cbPostalCodes))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonCreateGeoAccount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jExplanation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cbPostalCodes)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCreateGeoAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCreateGeoAccountActionPerformed
        try {
            FileAssociation.getDefault().execute(new URL("https://www.geonames.org/login"));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_jButtonCreateGeoAccountActionPerformed

    void load() {
        jtUserName.setText(GeonamesOptions.getInstance().getUserName());
        cbPostalCodes.setSelected(GeonamesOptions.getInstance().searchPostalCodes());
    }

    void store() {
        GeonamesOptions.getInstance().setUserName(jtUserName.getText());
        GeonamesOptions.getInstance().setPostalCodes(cbPostalCodes.isSelected());
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbPostalCodes;
    private javax.swing.JButton jButtonCreateGeoAccount;
    private javax.swing.JLabel jExplanation;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jtUserName;
    // End of variables declaration//GEN-END:variables
}
