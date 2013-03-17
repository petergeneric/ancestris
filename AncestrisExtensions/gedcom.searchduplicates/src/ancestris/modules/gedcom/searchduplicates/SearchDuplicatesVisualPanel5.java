package ancestris.modules.gedcom.searchduplicates;

import static ancestris.modules.gedcom.searchduplicates.Bundle.CheckDuplicatesVisualPanel5_title;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

@NbBundle.Messages("CheckDuplicatesVisualPanel5.title=Resume")
public final class SearchDuplicatesVisualPanel5 extends JPanel {

    /**
     * Creates new form SearchDuplicatesVisualPanel1
     */
    public SearchDuplicatesVisualPanel5() {
        initComponents();
    }

    @Override
    public String getName() {
        return CheckDuplicatesVisualPanel5_title();
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
        jPanel1 = new javax.swing.JPanel();
        individualsCheckBox = new javax.swing.JCheckBox();
        familiesCheckBox = new javax.swing.JCheckBox();
        submittersCheckBox = new javax.swing.JCheckBox();
        repositoriesCheckBox = new javax.swing.JCheckBox();
        sourcesCheckBox = new javax.swing.JCheckBox();

        jEditorPane1.setContentType("text/html"); // NOI18N
        jEditorPane1.setText(org.openide.util.NbBundle.getMessage(SearchDuplicatesVisualPanel5.class, "SearchDuplicatesVisualPanel5.jEditorPane1.text")); // NOI18N
        jEditorPane1.setDisabledTextColor(new java.awt.Color(32, 32, 32));
        jEditorPane1.setEnabled(false);
        jEditorPane1.setFocusable(false);
        jScrollPane1.setViewportView(jEditorPane1);

        org.openide.awt.Mnemonics.setLocalizedText(individualsCheckBox, org.openide.util.NbBundle.getMessage(SearchDuplicatesVisualPanel5.class, "SearchDuplicatesVisualPanel5.individualsCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(familiesCheckBox, org.openide.util.NbBundle.getMessage(SearchDuplicatesVisualPanel5.class, "SearchDuplicatesVisualPanel5.familiesCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(submittersCheckBox, org.openide.util.NbBundle.getMessage(SearchDuplicatesVisualPanel5.class, "SearchDuplicatesVisualPanel5.submittersCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(repositoriesCheckBox, org.openide.util.NbBundle.getMessage(SearchDuplicatesVisualPanel5.class, "SearchDuplicatesVisualPanel5.repositoriesCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(sourcesCheckBox, org.openide.util.NbBundle.getMessage(SearchDuplicatesVisualPanel5.class, "SearchDuplicatesVisualPanel5.sourcesCheckBox.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(individualsCheckBox)
                    .addComponent(submittersCheckBox)
                    .addComponent(repositoriesCheckBox)
                    .addComponent(familiesCheckBox)
                    .addComponent(sourcesCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(individualsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(familiesCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(submittersCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(repositoriesCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourcesCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox familiesCheckBox;
    private javax.swing.JCheckBox individualsCheckBox;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox repositoriesCheckBox;
    private javax.swing.JCheckBox sourcesCheckBox;
    private javax.swing.JCheckBox submittersCheckBox;
    // End of variables declaration//GEN-END:variables
}
