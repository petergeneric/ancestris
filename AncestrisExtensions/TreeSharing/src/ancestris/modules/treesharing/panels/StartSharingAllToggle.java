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
package ancestris.modules.treesharing.panels;

import ancestris.modules.treesharing.TreeSharingTopComponent;
import javax.swing.JToggleButton;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class StartSharingAllToggle extends JToggleButton {

    private final TreeSharingTopComponent owner;

    /**
     * Creates new form ShareeAllToggle
     */
    public StartSharingAllToggle(TreeSharingTopComponent tstc, boolean On) {
        this.owner = tstc;
        initComponents();
        setSelected(On);
        setEnabled(!On);
        setToolTipText(true);
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/startSharing.png"))); // NOI18N
        addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formActionPerformed(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

    private void formActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formActionPerformed
        if (!owner.isBusy()) {
            owner.startSharingAll();
        }
    }//GEN-LAST:event_formActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    public final void setToolTipText(boolean on) {
        setToolTipText(NbBundle.getMessage(StartSharingAllToggle.class, on ? "TIP_StartSharingisOn" : "TIP_StartSharingisOff"));
    }
}
