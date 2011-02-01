/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.wizard.newgedcom;

import genj.gedcom.GedcomException;
import javax.swing.JPanel;
import org.openide.util.Exceptions;

public final class NewGedcomVisualPanel2 extends JPanel implements NewGedcomSteps {

    /** Creates new form NewGedcomVisualPanel2 */
    public NewGedcomVisualPanel2(CreateNewGedcom newGedcom) {
        initComponents();
        //FIXME: ce n'est pas sa place
        aIndiBean1.setRoot(newGedcom.getFirst());
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(NewGedcomVisualPanel2.class, "create.first.title");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        aIndiBean1 = new ancestris.modules.beans.AIndiBean();
        jTextPane1 = new javax.swing.JTextPane();

        setAutoscrolls(true);
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setHorizontalScrollBar(null);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(620, 450));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(NewGedcomVisualPanel2.class, "NewGedcomVisualPanel2.jPanel1.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), java.awt.Color.blue)); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(580, 450));
        jPanel1.setRequestFocusEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(aIndiBean1, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(aIndiBean1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(123, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        add(jScrollPane1);

        jTextPane1.setContentType("text/html"); // NOI18N
        jTextPane1.setEditable(false);
        jTextPane1.setText(org.openide.util.NbBundle.getMessage(NewGedcomVisualPanel2.class, "NewGedcomVisualPanel2.jTextPane1.text")); // NOI18N
        jTextPane1.setPreferredSize(new java.awt.Dimension(300, 107));
        add(jTextPane1);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.beans.AIndiBean aIndiBean1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void applyNext() {
        try {
            aIndiBean1.commit();
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
