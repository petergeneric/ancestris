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

import ancestris.gedcom.privacy.standard.PrivacyPolicyImpl;
import ancestris.modules.treesharing.TreeSharingTopComponent;
import ancestris.modules.treesharing.communication.EntityConversion;
import ancestris.modules.treesharing.communication.GedcomFam;
import ancestris.modules.treesharing.communication.GedcomIndi;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JInternalFrame;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.Timer;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
public class SharedGedcom extends JInternalFrame {

    private final TreeSharingTopComponent owner;

    private final Gedcom gedcom;
    private final PrivacyPolicyImpl ppi;
    private Popup popup;
    private int nbTotalIndis = 0;
    private int nbTotalFams;
    private int nbPublicIndis;
    private int nbPublicFams;
    private int nbCommonIndis;
    private int nbCommonFams;
    
    private Set<MatchData> matchedIndis = null; 
    private Set<MatchData> matchedFams = null; 
    
    private boolean busyGedcom = false;
    
    
    
    /**
     * Creates new form SharedGedcom
     */
    public SharedGedcom(TreeSharingTopComponent tstc, Gedcom gedcom, boolean respectPrivacy) {
        super(gedcom.getName());
        this.owner = tstc;
        this.gedcom = gedcom;
        matchedIndis = new HashSet<MatchData>();
        matchedFams = new HashSet<MatchData>();
        
        ppi = new PrivacyPolicyImpl();
        popup = null;
        
        busyGedcom = true;
        
        initComponents();
        setShared(false);
        setPrivacy(respectPrivacy);
        updateStats(true);
        gedcom.addGedcomListener(callback);

        busyGedcom = false;
    }

    public void close() {
        gedcom.removeGedcomListener(callback);
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
        jLabel7 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jLabel10 = new javax.swing.JLabel();

        setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setIconifiable(true);
        setToolTipText(org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.toolTipText")); // NOI18N
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/tree.png"))); // NOI18N
        setPreferredSize(new java.awt.Dimension(340, 130));
        setRequestFocusEnabled(false);
        setVisible(true);

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/Indi.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jLabel1.toolTipText")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(50, 14));

        jLabel2.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/Fam.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jLabel2.text")); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jLabel2.toolTipText")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(50, 14));

        jLabel3.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jLabel3.text")); // NOI18N

        jLabel4.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jLabel4.text")); // NOI18N

        jLabel5.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jLabel5.text")); // NOI18N

        jLabel6.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jLabel6.text")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(50, 14));

        jLabel7.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jLabel7.text")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(50, 14));

        jButton1.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jButton1.text")); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(50, 20));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jLabel8.text")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(50, 14));

        jLabel9.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jLabel9.text")); // NOI18N
        jLabel9.setPreferredSize(new java.awt.Dimension(50, 14));

        jButton2.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jButton2.text")); // NOI18N
        jButton2.setPreferredSize(new java.awt.Dimension(50, 20));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jCheckBox1.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jCheckBox1.text")); // NOI18N
        jCheckBox1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jCheckBox1.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jCheckBox2.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox2, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jCheckBox2.text")); // NOI18N
        jCheckBox2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jCheckBox2.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/Title.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jLabel10.text")); // NOI18N
        jLabel10.setToolTipText(org.openide.util.NbBundle.getMessage(SharedGedcom.class, "SharedGedcom.jLabel10.toolTipText")); // NOI18N
        jLabel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel10MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel3)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBox2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCheckBox1)))
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox2))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        updateStats(true);
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        updateStats(true);
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jLabel10MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MouseClicked
        Entity entity = gedcom.getFirstEntity("HEAD");
        String str = "";
        if (entity != null) {
            Property note = entity.getProperty("NOTE");
            if (note != null) {
                str = note.getValue();
            }
        }
        GedcomDescriptionPanel descPanel = new GedcomDescriptionPanel(this, gedcom.getName(), gedcom.getOrigin().getFile().getAbsolutePath().replace(" ", "&nbsp;"), str);
        if (popup == null) {
            int x = evt.getXOnScreen();
            int y = evt.getYOnScreen();
            popup = PopupFactory.getSharedInstance().getPopup(this, descPanel, x, y);
            popup.show();
            
        } else {
            popup.hide();
            popup = null;
        }
        
    }//GEN-LAST:event_jLabel10MouseClicked

    public void closeDescription() {
        if (popup != null) {
            jLabel10MouseClicked(null);
        }
    }


    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        showList(Gedcom.INDI);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        showList(Gedcom.FAM);
    }//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    // End of variables declaration//GEN-END:variables


    public Gedcom getGedcom() {
        return gedcom;
    }
    
    public int getNbOfPublicIndis() {
        return isShared() ? (isPrivacySet() ? nbPublicIndis : nbTotalIndis) : 0;
    }
    
    public int getNbOfPublicFams() {
        return isShared() ? (isPrivacySet() ? nbPublicFams : nbTotalFams) : 0;
    }
    
    public void updateStats(boolean recalculate) {
        // Calculate
        if (recalculate || nbTotalIndis == 0) {
            nbTotalIndis = gedcom.getEntities(Gedcom.INDI).size();
            nbTotalFams = gedcom.getEntities(Gedcom.FAM).size();
            nbPublicIndis = getNbPublicEntities(Gedcom.INDI);   // has to be synchronized in case two gedcoms opening at the same time (merge for instance)
            nbPublicFams = getNbPublicEntities(Gedcom.FAM);     // has to be synchronized in case two gedcoms opening at the same time (merge for instance)
        }
        
        nbCommonIndis = countIds(matchedIndis);
        nbCommonFams = countIds(matchedFams);
        
        
        // Display
        jLabel6.setText(""+nbTotalIndis);
        jLabel8.setText(""+nbTotalFams);
        jLabel7.setText(isShared() ? (isPrivacySet() ? ""+nbPublicIndis : ""+nbTotalIndis) : "0");
        jLabel9.setText(isShared() ? (isPrivacySet() ? ""+nbPublicFams : ""+nbTotalFams) : "0");
        jButton1.setText(""+nbCommonIndis);
        jButton2.setText(""+nbCommonFams);
        jButton1.setEnabled(nbCommonIndis != 0);
        jButton2.setEnabled(nbCommonFams != 0);
        
        // Update icon if necessary
        owner.updateIcon();

    }

    public final void setShared(boolean shared) {
        jCheckBox1.setSelected(shared);
        updateStats(true);
    }
    
    public final boolean isShared() {
        return jCheckBox1.isSelected();
    }
    
    public final void setPrivacy(boolean privacy) {
        jCheckBox2.setSelected(privacy);
        updateStats(true);
    }

    private final boolean isPrivacySet() {
        return jCheckBox2.isSelected();
    }
    
    public final void resetResults() {
        matchedIndis.clear();
        matchedFams.clear();
        updateStats(true);
    }

    
    
    
    public List<Entity> getPublicEntities(String tag) {
        ppi.clear();
        List<Entity> ret = new LinkedList<Entity>();
        Collection<Entity> entities = (Collection<Entity>) gedcom.getEntities(tag);
        for (Entity entity : entities) {
            if (!isPrivacySet() || !ppi.isPrivate(entity)) {
                ret.add(entity);
            }
        }
        return ret;
    }


    /**
     * Build list of unique lastnames from gedcom file
     * (a set has no duplicates)
     * @return set
     */
    public Set<String> getPublicIndiLastnames() {
        if (!isShared()) {
            return new HashSet<String>();
        }
        Set<String> ret = new HashSet<String>();
        List<Entity> entities = getPublicEntities(Gedcom.INDI);
        for (Entity entity : entities) {
            Indi indi = (Indi) entity;
            if (indi != null && indi.getLastName() != null) {
                String str = indi.getLastName().replace("?", "").trim();
                if (!str.isEmpty()) {
                    ret.add(str);
                }
            }
        }
        return ret;
    }
    
    /**
     * Build list of unique husband lastname + "/" + wife lastname from gedcom file
     * (a set has no duplicates)
     * @return set
     */
    public Set<String> getPublicFamLastnames() {
        if (!isShared()) {
            return new HashSet<String>();
        }
        String str;
        Set<String> ret = new HashSet<String>();
        List<Entity> entities = getPublicEntities(Gedcom.FAM);
        for (Entity entity : entities) {
            Fam fam = (Fam) entity;
            str = getLastName(fam);
            if (!str.equals("/")) {
                ret.add(str);
            }
        }
        return ret;
    }
    

    public List<GedcomIndi> getPublicGedcomIndis(Set<String> commonIndiLastnames) {
        if (!isShared() || commonIndiLastnames == null) {
            return new ArrayList<GedcomIndi>();
        }
        List<GedcomIndi> ret = new ArrayList<GedcomIndi>();
        List<Entity> entities = getPublicEntities(Gedcom.INDI);
        for (Entity entity : entities) {
            Indi indi = (Indi) entity;
            if (indi != null) {
                String str = indi.getLastName();
                if (str != null && commonIndiLastnames != null && commonIndiLastnames.contains(str.trim())) {
                    GedcomIndi gedcomIndi = EntityConversion.indiToGedcomIndi(indi);   // I'd rather have no code in the GedcomIndi object for bandwidth and size reasons and have the code here
                    ret.add(gedcomIndi);
                }
            }
        }
        return ret;
        
    }



    public List<GedcomFam> getPublicGedcomFams(Set<String> commonFamLastnames) {
        if (!isShared()) {
            return new ArrayList<GedcomFam>();
        }
        List<GedcomFam> ret = new ArrayList<GedcomFam>();
        List<Entity> entities = getPublicEntities(Gedcom.FAM);
        for (Entity entity : entities) {
            Fam fam = (Fam) entity;
            if (commonFamLastnames != null && commonFamLastnames.contains(getLastName(fam))) {
                GedcomFam gedcomFam = EntityConversion.famToGedcomFam(fam);   // I'd rather have no code in the GedcomFam object for bandwidth and size reasons and have the code here
                ret.add(gedcomFam);
            }
        }
        return ret;
        
    }

    private String getLastName(Fam fam) {
        String strHusb = "";
        String strWife = "";
        if (fam != null) {
            if (fam.getHusband() != null) {
                strHusb = fam.getHusband().getLastName() != null ? fam.getHusband().getLastName().replace("?", "").trim() : "";
            }
            if (fam.getWife() != null) {
                strWife = fam.getWife().getLastName() != null ? fam.getWife().getLastName().replace("?", "").trim() : "";
            }
            
        }
        return strHusb + "/" + strWife;
    }

    
    
    private int getNbPublicEntities(String tag) {
        if (gedcom == null || gedcom.getName() == null) {
            return 0;
        }
        
        ppi.clear();
        int ret = 0;
        Collection<Entity> entities = (Collection<Entity>) gedcom.getEntities(tag); 
        for (Entity entity : entities) {
            ret += (ppi.isPrivate(entity)) ? 0 : 1;
        }
        return ret;
    }


    
    
    public void addEntity(Entity entity, FriendGedcomEntity friendGedcomEntity, int matchResult) {
        if (entity instanceof Indi) {
            matchedIndis.add(new MatchData(entity, friendGedcomEntity, matchResult));
            //updateStats(false);
            return;
        }
        if (entity instanceof Fam) {
            matchedFams.add(new MatchData(entity, friendGedcomEntity, matchResult));
            //updateStats(false);
        }
    }
    
    
    
    // Gedcom listeners
    private final Callback callback = new Callback();
    
    private class Callback extends GedcomListenerAdapter {

        @Override
        public void gedcomWriteLockReleased(Gedcom gedcom) {
            updateMe();
        }
    }

    
    
    private void updateMe() {
        
        // Quit if busy
        if (busyGedcom) {
            return;
        }
        
        // Now we're busy
        busyGedcom = true;

        // Set timer
        Timer timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer) e.getSource()).stop();
                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                    @Override
                    public void run() {
                        updateStats(true);
                        busyGedcom = false;
                    }
                });
                return;
            }
        });
        
        // Launch timer
        timer.start();
    }

    

    private void showList(String type) {
        owner.displayResultsPanel(getGedcom().getName(), NbBundle.getMessage(GedcomFriendMatch.class, "TITL_AllFriends"), type);
    }

    private int countIds(Set<MatchData> matchedEntities) {
        Set<String> ret = new HashSet<String>();
        for (MatchData data : matchedEntities) {
            ret.add(data.myEntity.getId());
        }
        return ret.size();
    }

    
    
    
    
    
}
