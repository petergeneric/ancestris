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

import ancestris.modules.treesharing.communication.MemberProfile;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.awt.Image;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import org.openide.util.NbBundle;

/**
 *
 * Represents Ancestris members with entities matching
 * 
 * @author frederic
 */
public class AncestrisFriend extends JInternalFrame {

    
    private final String name;
    private Set<MatchData> matchedIndis = null; 
    private Set<MatchData> matchedFams = null; 
    private MemberProfile memberProfile = null;
    private MemberProfile myProfile = null;

    
    /**
     * Creates new form AncestrisFriend
     */
    public AncestrisFriend(String name) {
        super(name);
        this.name = name;
        matchedIndis = new HashSet<MatchData>();
        matchedFams = new HashSet<MatchData>();
        initComponents();
        updateStats();
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setIconifiable(true);
        setToolTipText(org.openide.util.NbBundle.getMessage(AncestrisFriend.class, "AncestrisFriend.Form.toolTipText")); // NOI18N
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/friend.png"))); // NOI18N
        setMinimumSize(new java.awt.Dimension(320, 104));
        setPreferredSize(new java.awt.Dimension(320, 115));
        setVisible(true);

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/Indi.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AncestrisFriend.class, "AncestrisFriend.jLabel1.text")); // NOI18N

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/Fam.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(AncestrisFriend.class, "AncestrisFriend.jLabel2.text")); // NOI18N

        jLabel3.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(AncestrisFriend.class, "AncestrisFriend.jLabel3.text")); // NOI18N

        jLabel4.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(AncestrisFriend.class, "AncestrisFriend.jLabel4.text")); // NOI18N

        jLabel5.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(AncestrisFriend.class, "AncestrisFriend.jLabel5.text")); // NOI18N

        jLabel6.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(AncestrisFriend.class, "AncestrisFriend.jLabel6.text")); // NOI18N

        jButton1.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(AncestrisFriend.class, "AncestrisFriend.jButton1.text")); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(50, 20));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(AncestrisFriend.class, "AncestrisFriend.jButton2.text")); // NOI18N
        jButton2.setPreferredSize(new java.awt.Dimension(50, 20));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/profile.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(AncestrisFriend.class, "AncestrisFriend.jButton3.text")); // NOI18N
        jButton3.setEnabled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(52, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        showList(matchedIndis);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        showList(matchedFams);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        DialogManager.create(NbBundle.getMessage(StatsPanel.class, "TITL_ProfilePanel"),
                            new ProfilePanel(memberProfile, myProfile)).setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(DialogManager.OK_ONLY_OPTION).show();
    }//GEN-LAST:event_jButton3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    // End of variables declaration//GEN-END:variables

    public String getFriendName() {
        return name;
    }
    
    
    public void addEntity(Entity entity, FriendGedcomEntity friendGedcomEntity, int matchResult) {
        if (entity instanceof Indi) {
            matchedIndis.add(new MatchData(entity, friendGedcomEntity, matchResult));
            updateStats();
            return;
        }
        if (entity instanceof Fam) {
            matchedFams.add(new MatchData(entity, friendGedcomEntity, matchResult));
            updateStats();
            return;
        }
    }

    private void updateStats() {
        int nbCommonIndis = countIds(matchedIndis);
        int nbCommonFams = countIds(matchedFams);
        jButton1.setText("" + nbCommonIndis);
        jButton2.setText("" + nbCommonFams);
        jButton1.setEnabled(nbCommonIndis != 0);
        jButton2.setEnabled(nbCommonFams != 0);
    }

    private int countIds(Set<MatchData> matchedEntities) {
        Set<String> ret = new HashSet<String>();
        for (MatchData data : matchedEntities) {
            ret.add(data.myEntity.getId());
        }
        return ret.size();
    }

    
    private void showList(Set<MatchData> list) {
        DialogManager.create(NbBundle.getMessage(GedcomFriendMatch.class, "TITL_CommonEntities"), 
                new ListEntitiesPanel(NbBundle.getMessage(GedcomFriendMatch.class, "TITL_AllGedcoms"),  
                name, 
                list)).setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(DialogManager.OK_ONLY_OPTION).show();
    }

    public void removeGedcom(SharedGedcom sg) {
        
        List<MatchData> indisToRemove = new LinkedList<MatchData>();
        List<MatchData> famsToRemove = new LinkedList<MatchData>();
        
        Gedcom gedcom = sg.getGedcom();
        for (MatchData line : matchedIndis) {
            if (line.myEntity.getGedcom() == gedcom) {
                indisToRemove.add(line);
            }
        }
        for (MatchData line : matchedFams) {
            if (line.myEntity.getGedcom() == gedcom) {
               famsToRemove.add(line);
            }
        }
        
        for (MatchData match : indisToRemove) {
            matchedIndis.remove(match);
        }
        for (MatchData match : famsToRemove) {
            matchedFams.remove(match);
        }
    }

    public boolean isEmpty() {
        return matchedIndis.isEmpty() && matchedFams.isEmpty();
    }

    public void setTotals(int iIndis, int iFams) {
        jLabel5.setText(""+iIndis);
        jLabel6.setText(""+iFams);
    }

    public void setProfile(MemberProfile memberProfile, MemberProfile myProfile) {
        this.memberProfile = memberProfile;
        this.myProfile = myProfile;
        if (memberProfile != null) {
            jButton3.setEnabled(true);
            jButton3.setIcon(new ImageIcon(memberProfile.getPhoto().getScaledInstance(51, 62, Image.SCALE_DEFAULT)));
            jButton3.setToolTipText(NbBundle.getMessage(StatsPanel.class, "TITL_SeeProfile", name));
        } else {
            jButton3.setEnabled(false);
            jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/profile.png")));
            jButton3.setToolTipText(NbBundle.getMessage(StatsPanel.class, "TITL_NoProfile"));
        }
    }


}
