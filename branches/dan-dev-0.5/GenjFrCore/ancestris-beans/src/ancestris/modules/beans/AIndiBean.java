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

import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import javax.swing.JPanel;

public final class AIndiBean extends JPanel implements ABean {

    /** Creates new form NewGedcomVisualPanel2 */
    public AIndiBean() {
        initComponents();
    }

    private Indi indi;

    /**
     * Get the value of indi
     *
     * @return the value of indi
     */
    public Indi getIndi() {
        return indi;
    }

    /**
     * Set the value of indi
     *
     * @param entity new value of indi
     */
    @Override
    public AIndiBean setRoot(Property entity) {
        if (!(entity instanceof Indi))
            return this;
        this.indi = (Indi)entity;
        aEventBean1.setRoot(indi);
        aEventBean2.setRoot(indi);
        aSexBean1.setRoot(indi);
        aNameBean2.setRoot(indi);
        aSimpleBean1.setContext(indi,"OCCU");
        aPlaceBean2.setContext(entity, new TagPath("RESI:PLAC"), indi.getPropertyByPath("RESI:PLAC"));
        return this;
    }

    /**
     * commit beans - transaction has to be running already
     */
    @Override
    public void commit() throws GedcomException {
        aEventBean1.commit();
        aEventBean2.commit();
        aSexBean1.commit();
        aNameBean2.commit();
        aSimpleBean1.commit();
        aPlaceBean2.commit();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aNameBean2 = new ancestris.modules.beans.ANameBean();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        aPlaceBean2 = new ancestris.modules.beans.APlaceBean();
        jLabel5 = new javax.swing.JLabel();
        aSexBean1 = new ancestris.modules.beans.ASexBean();
        aEventBean1 = new ancestris.modules.beans.AEventBean();
        aEventBean2 = new ancestris.modules.beans.AEventBean();
        aSimpleBean1 = new ancestris.modules.beans.AChoiceBean();

        jLabel4.setFont(new java.awt.Font("DejaVu Sans", 1, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(AIndiBean.class, "AIndiBean.jLabel4.text")); // NOI18N

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AIndiBean.class, "AIndiBean.jLabel1.text")); // NOI18N
        jLabel1.setMaximumSize(new java.awt.Dimension(76, 15));
        jLabel1.setMinimumSize(new java.awt.Dimension(76, 15));
        jLabel1.setPreferredSize(new java.awt.Dimension(76, 15));

        jLabel5.setFont(new java.awt.Font("DejaVu Sans", 1, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(AIndiBean.class, "AIndiBean.jLabel5.text")); // NOI18N

        aEventBean1.setRequestFocusEnabled(false);
        aEventBean1.setShowKnown(true);
        aEventBean1.setTag("DEAT"); // NOI18N

        aEventBean2.setTag("BIRT"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(aEventBean2, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(32, 32, 32)
                        .addComponent(aNameBean2, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(aSexBean1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(aPlaceBean2, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE))
                    .addComponent(aEventBean1, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(aSimpleBean1, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jLabel4))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(aNameBean2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(aSexBean1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aEventBean2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aEventBean1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aPlaceBean2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel5)
                    .addComponent(aSimpleBean1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.beans.AEventBean aEventBean1;
    private ancestris.modules.beans.AEventBean aEventBean2;
    private ancestris.modules.beans.ANameBean aNameBean2;
    private ancestris.modules.beans.APlaceBean aPlaceBean2;
    private ancestris.modules.beans.ASexBean aSexBean1;
    private ancestris.modules.beans.AChoiceBean aSimpleBean1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    // End of variables declaration//GEN-END:variables

}
