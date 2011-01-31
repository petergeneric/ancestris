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

import genj.gedcom.Context;
import genj.gedcom.Submitter;
import genj.gedcom.TagPath;
import genjfr.util.GedcomDirectory;
import javax.swing.JPanel;

public final class NewGedcomVisualPanel1 extends JPanel implements NewGedcomSteps {

    /** Creates new form NewGedcomVisualPanel1 */
    public NewGedcomVisualPanel1() {
        initComponents();
        //FIXME: ce n'est pas sa place

        Context c = GedcomDirectory.getInstance().getLastContext();
        if (c != null) {
            Submitter subm = CreateNewGedcom.getGedcom().getSubmitter();
            aAddrBean1.setRoot(subm);
            aSimpleBean1.setContext(subm, new TagPath("NAME"), subm.getProperty("NAME"));
        }
    }

    @Override
    public String getName() {
        return "Creation du gedcom";
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        aTagBean1 = new ancestris.modules.beans.ATagBean();
        aSimpleBean1 = new ancestris.modules.beans.ASimpleBean();
        aAddrBean1 = new ancestris.modules.beans.AAddrBean();
        jTextPane1 = new javax.swing.JTextPane();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        aTagBean1.setTag(org.openide.util.NbBundle.getMessage(NewGedcomVisualPanel1.class, "NewGedcomVisualPanel1.aTagBean1.tag")); // NOI18N

        aAddrBean1.setPreferredSize(new java.awt.Dimension(580, 285));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(aAddrBean1, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                        .addGap(17, 17, 17))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(aTagBean1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(52, 52, 52)
                        .addComponent(aSimpleBean1, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(aSimpleBean1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aTagBean1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(aAddrBean1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(158, Short.MAX_VALUE))
        );

        add(jPanel1);

        jTextPane1.setContentType("text/html"); // NOI18N
        jTextPane1.setEditable(false);
        jTextPane1.setText(org.openide.util.NbBundle.getMessage(NewGedcomVisualPanel1.class, "NewGedcomVisualPanel1.jTextPane1.text")); // NOI18N
        jTextPane1.setPreferredSize(new java.awt.Dimension(300, 107));
        add(jTextPane1);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.beans.AAddrBean aAddrBean1;
    private ancestris.modules.beans.ASimpleBean aSimpleBean1;
    private ancestris.modules.beans.ATagBean aTagBean1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void applyNext() {
    }
}
