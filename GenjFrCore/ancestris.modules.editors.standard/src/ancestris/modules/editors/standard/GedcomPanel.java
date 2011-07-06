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
package ancestris.modules.editors.standard;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import javax.swing.JPanel;
import org.openide.util.Exceptions;

public final class GedcomPanel extends JPanel implements IEditorPanel {

    private Context context;

    /** Creates new form FamillyVisualPanel */
    public GedcomPanel() {
        initComponents();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        setContext(context);
    }

    /**
     * Fire a commit
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        commit();
    }


    public void setContext(Context context) {
        this.context = context;
        if (context == null) {
            return;
        }
        Gedcom gedcom = context.getGedcom();
        placeFormat.setJurisdictions(gedcom.getPlaceFormat());
        placeFormat.setShowJuridcitions(gedcom.getShowJuridictions());
        placeFormat.setDisplayFormat(gedcom.getPlaceDisplayFormat());
        placeFormat.setSortOrder(gedcom.getPlaceSortOrder());
        gedcomDescription.setTag("NOTE");
        gedcomDescription.setContext(gedcom.getFirstEntity("HEAD"),null);
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(GedcomPanel.class, "gedcom.properties.title");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        placeFormat = new ancestris.modules.beans.APlaceFormatBean();
        jLabel1 = new javax.swing.JLabel();
        gedcomDescription = new ancestris.modules.beans.AMLEBean();

        placeFormat.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(GedcomPanel.class, "GedcomPanel.placeFormat.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12))); // NOI18N

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 13));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GedcomPanel.class, "GedcomPanel.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(gedcomDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(placeFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(12, 12, 12)
                        .addComponent(gedcomDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(placeFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.beans.AMLEBean gedcomDescription;
    private javax.swing.JLabel jLabel1;
    private ancestris.modules.beans.APlaceFormatBean placeFormat;
    // End of variables declaration//GEN-END:variables

    @Override
    public void commit() {
        try {
            context.getGedcom().doUnitOfWork(new UnitOfWork() {
                public void perform(Gedcom gedcom) throws GedcomException {
                    context.getGedcom().setPlaceFormat(placeFormat.getJurisdictions());
                    context.getGedcom().setShowJuridictions(placeFormat.getShowJuridictions());
                    context.getGedcom().setPlaceDisplayFormat(placeFormat.getDisplayFormat());
                    context.getGedcom().setPlaceSortOrder(placeFormat.getSortOrder());
                    gedcomDescription.commit();
                }
            });
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
