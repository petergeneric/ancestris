package ancestris.modules.gedcom.mergefile;

import ancestris.gedcom.GedcomDirectory;
import static ancestris.modules.gedcom.mergefile.Bundle.stepIndex3;
import genj.gedcom.Context;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

@NbBundle.Messages("stepIndex3=Chose second gedcom file to merge")
public final class GedcomMergeVisualPanel3 extends JPanel {

    private Context gedcomContext;

    /**
     * Creates new form GedcomMergeVisualPanel3
     */
    public GedcomMergeVisualPanel3() {
        initComponents();
    }

    @Override
    public String getName() {
        return stepIndex3();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setBorder(null);

        jScrollPane1.setBorder(null);
        jScrollPane1.setViewportBorder(null);

        jEditorPane1.setBorder(null);
        jEditorPane1.setContentType("text/html"); // NOI18N
        jEditorPane1.setEditable(false);
        jEditorPane1.setText(org.openide.util.NbBundle.getMessage(GedcomMergeVisualPanel3.class, "GedcomMergeVisualPanel3.jEditorPane1.text")); // NOI18N
        jEditorPane1.setEnabled(false);
        jScrollPane1.setViewportView(jEditorPane1);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(GedcomMergeVisualPanel3.class, "GedcomMergeVisualPanel3.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        // Choose Gedcom File 
        if ((gedcomContext = GedcomDirectory.getDefault().openGedcom()) != null) {
            jTextField1.setText(gedcomContext.getGedcom().getOrigin().getFile().toString());
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the gedcomContext
     */
    public Context getGedcomContext() {
        return gedcomContext;
    }
}
