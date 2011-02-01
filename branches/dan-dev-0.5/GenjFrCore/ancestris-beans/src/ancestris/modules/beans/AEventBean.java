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
package ancestris.modules.beans;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import javax.swing.JPanel;

public final class AEventBean extends JPanel implements ABean {

    private String tag = "";
    private PropertyEvent event;

    /**
     * Get the value of tag
     *
     * @return the value of tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Set the value of tag
     *
     * @param tag new value of tag
     */
    public void setTag(String tag) {
        this.tag = tag;
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, getTagName());
        repaint();
    }
    private Entity root;

    /**
     * Get the value of root
     *
     * @return the value of root
     */
    public Entity getRoot() {
        return root;
    }

    /**
     * Set the value of root
     *
     * @param root new value of root
     */
    @Override
    public AEventBean setRoot(Property root) {
        if (!(root instanceof Entity)) {
            return this;
        }
        Property eventProp = null;
        if (root != null) {
            eventProp = root.getProperty(tag);
        }
        return setRoot(root, eventProp);
    }

    public AEventBean setRoot(Property root, Property event) {
        this.root = (Entity) root;
        this.event = (PropertyEvent) event;
        if (root != null) {
            cbIsKnown.setSelected(event != null);
            aDateBean1.setContext(root, tag);
            aPlaceBean1.setContext(root, tag);
            showOrHide();
        }
        return this;
    }

    /** Creates new form NewGedcomVisualPanel2 */
    public AEventBean() {
        initComponents();
        cbIsKnown.setSelected(false);
        showOrHide();
    }
    private boolean showKnown = false;

    /**
     * Set the value of showCheck
     *
     * @param showKnown new value of showCheck
     */
    public void setShowKnown(boolean showKnown) {
        this.showKnown = showKnown;
        showOrHide();
    }

    /**
     * commit beans - transaction has to be running already
     */
    @Override
    public void commit() throws GedcomException {
        aDateBean1.commit();
        aPlaceBean1.commit();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aDateBean1 = new ancestris.modules.beans.ADateBean();
        jLabel2 = new javax.swing.JLabel();
        aPlaceBean1 = new ancestris.modules.beans.APlaceBean();
        cbIsKnown = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();

        jLabel2.setFont(new java.awt.Font("DejaVu Sans", 1, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, getTagName());
        jLabel2.setMaximumSize(new java.awt.Dimension(76, 15));
        jLabel2.setMinimumSize(new java.awt.Dimension(76, 15));
        jLabel2.setPreferredSize(new java.awt.Dimension(76, 15));

        org.openide.awt.Mnemonics.setLocalizedText(cbIsKnown, null);
        cbIsKnown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbIsKnownActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 0, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AEventBean.class, "AEventBean.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(aPlaceBean1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                    .addComponent(aDateBean1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbIsKnown))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbIsKnown))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aDateBean1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(aPlaceBean1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbIsKnownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbIsKnownActionPerformed
        showOrHide();
    }//GEN-LAST:event_cbIsKnownActionPerformed

    private void showOrHide() {
        if (showKnown) {
            boolean checked = cbIsKnown.isSelected();
            aDateBean1.setVisible(checked);
            jLabel1.setVisible(checked);
            aPlaceBean1.setVisible(checked);
            cbIsKnown.setVisible(true);
        } else {
            cbIsKnown.setVisible(false);
//            cbIsKnown.setSelected(true);
        }
    }

    private String getTagName() {
        return Gedcom.getName(tag);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.beans.ADateBean aDateBean1;
    private ancestris.modules.beans.APlaceBean aPlaceBean1;
    private javax.swing.JCheckBox cbIsKnown;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables
}
