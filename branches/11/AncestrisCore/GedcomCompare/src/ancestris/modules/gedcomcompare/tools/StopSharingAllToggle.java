/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015-2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcomcompare.tools;

import ancestris.modules.gedcomcompare.GedcomCompareTopComponent;
import javax.swing.JToggleButton;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class StopSharingAllToggle extends JToggleButton {

    private final GedcomCompareTopComponent owner;

    /**
     * Creates new form StopSharingAllToggle
     */
    public StopSharingAllToggle(GedcomCompareTopComponent tstc, boolean on) {
        this.owner = tstc;
        initComponents();
        setToolTipText(on);
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/stopSharing.png"))); // NOI18N
        addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formActionPerformed(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

    private void formActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formActionPerformed
        if (!owner.isBusy()) {
            owner.stopSharing();
        }
    }//GEN-LAST:event_formActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


    public final void setToolTipText(boolean on) {
        setSelected(on);
        setEnabled(!on);
        setToolTipText(NbBundle.getMessage(StartSharingAllToggle.class, on ? "TIP_StopSharingisOn" : "TIP_StopSharingisOff"));
    }

}
