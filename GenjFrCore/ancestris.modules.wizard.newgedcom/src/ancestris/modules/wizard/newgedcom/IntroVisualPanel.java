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

/*
 * IntroVisualPanel.java
 *
 * Created on 3 févr. 2011, 00:29:15
 */

package ancestris.modules.wizard.newgedcom;

import javax.swing.JPanel;

/**
 *
 * @author daniel
 */
public class IntroVisualPanel extends JPanel implements NewGedcomSteps{

    /** Creates new form IntroVisualPanel */
    public IntroVisualPanel() {
        initComponents();
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(IntroVisualPanel.class, "intro.title");
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        cbSkipIntro = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(622, 380));

        jTextPane1.setContentType("text/html"); // NOI18N
        jTextPane1.setEditable(false);
        jTextPane1.setText(org.openide.util.NbBundle.getMessage(IntroVisualPanel.class, "IntroVisualPanel.jTextPane1.text")); // NOI18N
        jScrollPane1.setViewportView(jTextPane1);

        cbSkipIntro.setText(org.openide.util.NbBundle.getMessage(IntroVisualPanel.class, "IntroVisualPanel.cbSkipIntro.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
                    .addComponent(cbSkipIntro))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSkipIntro)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbSkipIntro;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void applyNext() {
        NewGedcomOptions.getInstance().setSkipIntro(cbSkipIntro.isSelected());
    }

}
