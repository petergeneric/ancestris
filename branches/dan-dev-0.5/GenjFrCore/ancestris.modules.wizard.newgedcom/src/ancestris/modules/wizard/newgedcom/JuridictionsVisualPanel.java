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

import javax.swing.JPanel;

public final class JuridictionsVisualPanel extends JPanel implements NewGedcomSteps {

    private INewGedcomProvider gedcomProvider;

    /** Creates new form FamillyVisualPanel */
    public JuridictionsVisualPanel(INewGedcomProvider newGedcom) {
        gedcomProvider = newGedcom;
        initComponents();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        placeFormat.setFormatString(gedcomProvider.getContext().getGedcom().getPlaceFormat());
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(JuridictionsVisualPanel.class, "gedcom.properties.title");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        placeFormat = new ancestris.modules.beans.APlaceFormatBean();

        setPreferredSize(new java.awt.Dimension(622, 500));
        setRequestFocusEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(placeFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(placeFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.beans.APlaceFormatBean placeFormat;
    // End of variables declaration//GEN-END:variables
    @Override
    public void applyNext() {
        gedcomProvider.getContext().getGedcom().setPlaceFormat(placeFormat.getFormatString());
    }
}
