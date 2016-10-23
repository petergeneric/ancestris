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

import genj.util.Registry;
import java.awt.Dimension;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

public final class GedcomPropertiesVisualPanel5 extends JPanel implements Constants {
    
    private final int mode = GedcomPropertiesWizardIterator.getMode();
    private Registry registry = null;
    private final String winWidth = "gedcomProperties5Width";
    private final String winHeight = "gedcomProperties5Height";

    private String strSour = "";
    private String strName = "";
    private String strVersion = "";
    private String strCorporation = "";
    private String strAddress = "";
    private String strDate = "";
    private String strTime = "";

    /**
     * Creates new form GedcomPropertiesVisualPanel5
     */
    public GedcomPropertiesVisualPanel5() {
        initComponents();
        registry = Registry.get(getClass());
        this.setPreferredSize(new Dimension(registry.get(winWidth, this.getPreferredSize().width), registry.get(winHeight, this.getPreferredSize().height)));
        
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "STEP_5_name");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setBorder(null);
        setPreferredSize(new java.awt.Dimension(520, 400));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jScrollPane1.setBorder(null);
        jScrollPane1.setViewportBorder(null);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(520, 400));

        jPanel1.setBorder(null);
        jPanel1.setPreferredSize(new java.awt.Dimension(520, 400));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel5.class, "GedcomPropertiesVisualPanel5.jLabel1.text")); // NOI18N

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GedcomPropertiesVisualPanel5.class, "GedcomPropertiesVisualPanel5.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jLabel1)
                .addGap(60, 60, 60)
                .addComponent(jLabel2)
                .addContainerGap(270, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        registry.put(winWidth, evt.getComponent().getWidth());
        registry.put(winHeight, evt.getComponent().getHeight());
    }//GEN-LAST:event_formComponentResized

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    void setSOUR(String displayValue) {
        strSour = displayValue;
        updateDisplay();
    }

    void setVERS(String displayValue) {
        strVersion = displayValue;
        updateDisplay();
    }

    void setNAME(String displayValue) {
        strName = displayValue;
        updateDisplay();
    }

    void setCORP(String displayValue) {
        strCorporation = displayValue;
        updateDisplay();
    }

    void setADDR(String displayValue) {
        strAddress = displayValue;
        updateDisplay();
    }

    void setDATE(String displayValue) {
        strDate = displayValue;
        updateDisplay();
    }

    void setTIME(String displayValue) {
        strTime = displayValue;
        updateDisplay();
    }

    private void updateDisplay() {
        String strMode = mode == CREATION ? "create" : "update";
        jLabel1.setText(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "Panel5.jLabel1."+strMode, strSour, strName, strVersion, strCorporation, strAddress));
        jLabel2.setText(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "Panel5.jLabel2."+strMode, strDate, strTime));
    }
}
