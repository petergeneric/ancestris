/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.treesharing.panels;

import ancestris.modules.treesharing.communication.EntityConversion;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import java.awt.Color;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class EntityBean extends javax.swing.JPanel {

    private final static ImageIcon ICON_FAM = new ImageIcon(ImageUtilities.loadImage("ancestris/modules/treesharing/resources/Fam.png"));
    private final static ImageIcon ICON_MALE = new ImageIcon(ImageUtilities.loadImage("ancestris/modules/treesharing/resources/Male.png"));
    private final static ImageIcon ICON_FEMALE = new ImageIcon(ImageUtilities.loadImage("ancestris/modules/treesharing/resources/Female.png"));
    private final static ImageIcon ICON_STAR = new ImageIcon(ImageUtilities.loadImage("ancestris/modules/treesharing/resources/star.png"));
    
    
    /**
     * Creates new form EntityBean
     */
    public EntityBean(Entity entity, List<MatchData> list) {

        initComponents();

        // First panel with my entity
        BoxLayout bl11 = new BoxLayout(jPanel1, BoxLayout.PAGE_AXIS);
        jPanel1.setLayout(bl11);
        JPanel miniPanel1 = new JPanel();
        BoxLayout bl12 = new BoxLayout(miniPanel1, BoxLayout.LINE_AXIS);
        miniPanel1.setLayout(bl12); 
        miniPanel1.add(new JLabel(getSexIcon(entity)));
        miniPanel1.add(new JLabel("   "));
        miniPanel1.add(new JLabel(EntityConversion.getStringFromEntity(entity, true)));
        jPanel1.add(miniPanel1);
        final Entity myEntity = entity;
        miniPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() != 2) {   
                    return;
                }
                SelectionDispatcher.fireSelection(new Context(myEntity));
            }
        });
        miniPanel1.setToolTipText(NbBundle.getMessage(EntitiesListPanel.class, "TIP_ClickableEntity"));
        
        
        // Second panel with matching entities
        BoxLayout bl2 = new BoxLayout(jPanel2, BoxLayout.PAGE_AXIS);
        jPanel2.setLayout(bl2);
        for (MatchData line : list) {
            JPanel miniPanel2 = new JPanel();
            BoxLayout bl3 = new BoxLayout(miniPanel2, BoxLayout.LINE_AXIS);
            miniPanel2.setLayout(bl3); 
            // Sex icon
            miniPanel2.add(new JLabel(getSexIcon(line.friendGedcomEntity)));
            miniPanel2.add(new JLabel("   "));
            miniPanel2.add(new JLabel(EntityConversion.getStringFromEntity(line.friendGedcomEntity, true)));
            miniPanel2.add(new JLabel(" "));
            for (int i = 0; i < 4-line.matchResult; i++) {
                miniPanel2.add(new JLabel(ICON_STAR));
            }
            //miniPanel.add(Box.createHorizontalGlue());
            jPanel2.add(miniPanel2);
            
        }
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel2.setPreferredSize(new java.awt.Dimension(281, 20));

        jPanel1.setPreferredSize(new java.awt.Dimension(230, 20));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 230, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(8, 8, 8))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables

    private ImageIcon getSexIcon(Entity entity) {
        ImageIcon icon = null;
        if (entity instanceof Fam) {
            icon = ICON_FAM;
        } else {
            Indi indi = (Indi) entity;
            if (indi.getSex() == PropertySex.MALE) {
                icon = ICON_MALE;
            } else {
                icon = ICON_FEMALE;
            }
        }
        return icon;
    }

    private ImageIcon getSexIcon(FriendGedcomEntity friendGedcomEntity) {
        ImageIcon icon = null;
        if (friendGedcomEntity.type.equals(Gedcom.FAM)) {
            icon = ICON_FAM;
        } else {
            if (friendGedcomEntity.indiSex == PropertySex.MALE) {
                icon = ICON_MALE;
            } else {
                icon = ICON_FEMALE;
            }
        }
        return icon;
    }
}
