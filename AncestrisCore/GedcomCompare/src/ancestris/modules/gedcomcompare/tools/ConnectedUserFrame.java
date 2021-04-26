/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015-2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcomcompare.tools;

import ancestris.modules.gedcomcompare.GedcomCompareTopComponent;
import ancestris.modules.gedcomcompare.communication.Comm;
import static ancestris.modules.gedcomcompare.communication.Comm.TAG_STS;
import ancestris.modules.gedcomcompare.communication.UserProfile;
import ancestris.modules.gedcomcompare.options.GedcomCompareOptionsPanel;
import ancestris.util.swing.DialogManager;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.UUID;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * Represents Ancestris users
 * 
 * @author frederic
 */
public class ConnectedUserFrame extends DataFrame implements ComparedGedcom {

    private static String HIDDEN_VALUE = "yes";
    private String uniqueID;
    
    // Profile information
    private UserProfile userProfile = null;
    
    // Gedcom information
    private int nbIndis;
    private int nbFamilies;
    private int nbSTs;
    private int nbEvens;
    private String[] sTs;
    private STMap stMap;
    private int stats_nbOverlaps;
    private int stats_nbCityNames;
    private int stats_nbEvents;
    
    // Flags
    private boolean isMe = false;    // This is me as a conneted user
    private boolean include;         // Include flag (include and Compare) 
    
    // Connection stats
    private int connections;
    private Date startDate;
    private Date endDate;
    
    // context action
    private AbstractAction profileAction;

    /**
     * Constructor
     */
    public ConnectedUserFrame(GedcomCompareTopComponent gctc, Comm.User user) {
        
        super(user.userProfile.pseudo); // tile
        setName(user.userProfile.pseudo); // component name

        initComponents();

        this.owner = gctc;
        this.type = DataFrame.GEDCOM_TYPE_REMOTE;
        uniqueID = UUID.randomUUID().toString();
        stMap = null;
        userProfile = new UserProfile();
        updateInfo(user);
        
        if (owner.getPreferredPseudo().equals(userProfile.pseudo)) {
            isMe = true;
        }
        overviewCheckBox.setEnabled(owner.isSharingOn());
        readyCheckBox.setEnabled(false);

        super.updateColor();
        
        this.include = !isPseudoHidden(userProfile.pseudo);
        connections = 0;
        startDate = new Date();
        endDate = new Date();
        
        // replace existing context menu => only for remote gedcoms / for local ones, just getPackets rid of menu
        JPopupMenu popup = new JPopupMenu();
        // ...profile
        profileAction = new AbstractAction(NbBundle.getMessage(this.getClass(), "ACT_ShowProfile"), getPhotoIcon(true)) {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogManager.create(NbBundle.getMessage(this.getClass(), "TITL_ProfilePanel", userProfile.pseudo), 
                        new ProfilePanel(userProfile, owner.getMyProfile(), connections, startDate, endDate))
                        .setMessageType(DialogManager.PLAIN_MESSAGE)
                        .setResizable(false)
                        .setOptionType(DialogManager.OK_ONLY_OPTION).show();
            }
        };
        popup.add(new JMenuItem(profileAction));
        // ...email
        Action b = new AbstractAction(NbBundle.getMessage(this.getClass(), "ACT_ContactUser"), 
                                      new ImageIcon(ImageUtilities.loadImage("ancestris/modules/gedcomcompare/resources/email.png"))) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProfilePanel panel = new ProfilePanel(userProfile, owner.getMyProfile(), connections, startDate, endDate);
                panel.sendMail(userProfile.email);
            }
        };
        popup.add(new JMenuItem(b));
        // ...reset
        Action c = new AbstractAction(NbBundle.getMessage(this.getClass(), "ACT_ResetFlags"), 
                                      new ImageIcon(ImageUtilities.loadImage("ancestris/modules/gedcomcompare/resources/reset.png"))) {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        };
        popup.add(new JMenuItem(c));
        // ...hide
        Action d = new AbstractAction(NbBundle.getMessage(this.getClass(), "ACT_HideUser"), 
                                      new ImageIcon(ImageUtilities.loadImage("ancestris/modules/gedcomcompare/resources/deleteuser.png"))) {
            @Override
            public void actionPerformed(ActionEvent e) {
                showUser(false);
            }
        };
        popup.add(new JMenuItem(d));

        setComponentPopupMenu(popup);
        Container pane = ((BasicInternalFrameUI) getUI()).getNorthPane();
        if (pane.getComponent(0) instanceof JButton) {
            JButton menuButton = (JButton) pane.getComponent(0);
            MouseListener[] mls = menuButton.getMouseListeners();
            if (mls.length>1 && mls[1] instanceof MouseAdapter) {
                menuButton.removeMouseListener(mls[1]);
            }
        }
        
    }

    @Override
    public String getID() {
        return uniqueID;
    }

    public void updateInfo(Comm.User user) {
        setActive(true);
        updateProfileConnection(user.userProfile);
        this.nbIndis = Integer.valueOf(user.f_NbIndis);
        this.nbFamilies = Integer.valueOf(user.f_NbFamilies);
        this.nbSTs = Integer.valueOf(user.f_NbSTs);
        this.nbEvens = Integer.valueOf(user.f_NbEvens);
        this.sTs = new String[Comm.TAG_STS.length];
        for (int i = 0; i < TAG_STS.length; i++) {
            sTs[i] = user.f_STs[i];
        }
        this.stats_nbOverlaps = Integer.valueOf(user.stats_nbOveraps);
        this.stats_nbCityNames = Integer.valueOf(user.stats_nbCityNames);
        this.stats_nbEvents = Integer.valueOf(user.stats_nbEvents);
        
        // Display
        visibleIndiLabel.setText("" + nbIndis);
        visibleFamLabel.setText("" + nbFamilies);
        visibleSTLabel.setText("" + nbSTs);
        visibleEvenLabel.setText("" + nbEvens);
    }
    
    private void updatePhoto(boolean set) {
        ImageIcon icon = getPhotoIcon(set);
        setFrameIcon(icon);
        profileAction.putValue(Action.SMALL_ICON, icon);
    }


    private ImageIcon getPhotoIcon(boolean set) {
        if (userProfile.photoBytes == null || !set) {
            return new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/friend16.png"));
        } else {
            return GedcomCompareOptionsPanel.getPhoto(1, userProfile.photoBytes);
        }
    }
    
    @Override
    public String getUserName() {
        return super.getTitle();
    }
    
    @Override
    public STMap getMap() {
        return stMap;
    }

    public final void setOpen(boolean set) {
        overviewCheckBox.setEnabled(set && isActive());
        readyCheckBox.setEnabled(overviewCheckBox.isSelected() && set && isActive());
    }
    
    public void setActive(boolean set) {
        type = set ? DataFrame.GEDCOM_TYPE_REMOTE : DataFrame.GEDCOM_TYPE_REMOTE_INACTIVE;
        updateColor();
        setOpen(owner.isSharingOn());
    }

    public boolean isActive() {
        return type == DataFrame.GEDCOM_TYPE_REMOTE;
    }


    public void setMap(STMap map) {
        stMap = map;
        if (owner.getComparisonFrame(this) != null) {
            owner.getComparisonFrame(this).reset();
        }
        overviewCheckBox.setSelected(map != null);
        if (map != null) {
            overviewCheckBox.setIcon(owner.SELECTEDON_ICON);
            owner.getComparisonFrame(this).launchCompare(true);
            overviewCheckBox.setEnabled(true);
            readyCheckBox.setEnabled(true);
        } else {
            updatePhoto(false);
            overviewCheckBox.setIcon(null);
            readyCheckBox.setIcon(null);
            readyCheckBox.setSelected(false);
            readyCheckBox.setEnabled(false);
            overviewCheckBox.setSelected(false);
        }
    }

    public void updateMap(STMapEventsCapsule capsule) {
        STFactory.updateMap(stMap, capsule);
        if (stMap != null) {
            stMap.setComplete(true);
        }
        readyCheckBox.setIcon(owner.SELECTEDON_ICON);
        if (owner.getComparisonFrame(this) != null) {
            owner.getComparisonFrame(this).launchCompare(true);
        }
    }

    public void updateProfileConnection(UserProfile profile) {
        userProfile.pseudo = profile.pseudo;
        userProfile.ipAddress = profile.ipAddress;
        userProfile.pipAddress = profile.pipAddress;
        userProfile.portAddress = profile.portAddress;
        userProfile.pportAddress = profile.pportAddress;
        userProfile.usePrivateIP = profile.usePrivateIP;
        userProfile.privacy = profile.privacy;
    }

    public void updateProfileInfo(UserProfile profile) {
        userProfile.name = profile.name;
        userProfile.email = profile.email;
        userProfile.city = profile.city;
        userProfile.country = profile.country;
        userProfile.photoBytes = profile.photoBytes;
        updatePhoto(true);
    }

    public void resetIcon(int step) {
        if (step == 1) {
            overviewCheckBox.setIcon(null);
            overviewCheckBox.setSelected(false);
        }
        if (step == 2) {
            readyCheckBox.setIcon(null);
            readyCheckBox.setSelected(false);
        }
        setOpen(owner.isSharingOn());
    }
    
    public void reset() {
        setMap(null);
    }
    
    public void showUser(boolean show) {
        setInclude(show);
        owner.showUserFrame(this, show);
    }
    
    public void addConnection() {
        connections++;
        endDate = new Date();
        owner.updateStatsDisplay();
    }
    
    public int getConnections() {
        return connections;
    }
    
    public boolean hasConnections() {
        return connections > 0;
    }
    
    public boolean hasOverlap() {
        if (owner != null && owner.getComparisonFrame(this) != null && owner.getComparisonFrame(this).getIntersectionKeys() != null) {
            return owner.getComparisonFrame(this).getIntersectionKeys().size() > 0;
        } else {
            return false;
        }
    }
    
    public int[] getStats() {
        return new int[] { stats_nbOverlaps, stats_nbCityNames, stats_nbEvents };
    }
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        iconIndiLabel = new javax.swing.JLabel();
        visibleIndiLabel = new javax.swing.JLabel();
        iconFamLabel = new javax.swing.JLabel();
        visibleFamLabel = new javax.swing.JLabel();
        iconEvenLabel = new javax.swing.JLabel();
        visibleEvenLabel = new javax.swing.JLabel();
        iconSTLabel = new javax.swing.JLabel();
        visibleSTLabel = new javax.swing.JLabel();
        overviewCheckBox = new javax.swing.JCheckBox();
        readyCheckBox = new javax.swing.JCheckBox();

        setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setToolTipText(org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.Form.toolTipText")); // NOI18N
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/friend16.png"))); // NOI18N
        setRequestFocusEnabled(false);
        setVisible(true);
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameActivated(evt);
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        iconIndiLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        iconIndiLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        iconIndiLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/indi.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(iconIndiLabel, org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.iconIndiLabel.text")); // NOI18N
        iconIndiLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.iconIndiLabel.toolTipText")); // NOI18N
        iconIndiLabel.setPreferredSize(new java.awt.Dimension(50, 14));

        visibleIndiLabel.setFont(visibleIndiLabel.getFont().deriveFont(visibleIndiLabel.getFont().getSize()-1f));
        visibleIndiLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(visibleIndiLabel, org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.visibleIndiLabel.text")); // NOI18N
        visibleIndiLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        iconFamLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        iconFamLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        iconFamLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/fam.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(iconFamLabel, org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.iconFamLabel.text")); // NOI18N
        iconFamLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.iconFamLabel.toolTipText")); // NOI18N
        iconFamLabel.setPreferredSize(new java.awt.Dimension(50, 14));

        visibleFamLabel.setFont(visibleFamLabel.getFont().deriveFont(visibleFamLabel.getFont().getSize()-1f));
        visibleFamLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(visibleFamLabel, org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.visibleFamLabel.text")); // NOI18N
        visibleFamLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        iconEvenLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        iconEvenLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        iconEvenLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/even.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(iconEvenLabel, org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.iconEvenLabel.text")); // NOI18N
        iconEvenLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.iconEvenLabel.toolTipText")); // NOI18N
        iconEvenLabel.setPreferredSize(new java.awt.Dimension(50, 14));

        visibleEvenLabel.setFont(visibleEvenLabel.getFont().deriveFont(visibleEvenLabel.getFont().getSize()-1f));
        visibleEvenLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(visibleEvenLabel, org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.visibleEvenLabel.text")); // NOI18N
        visibleEvenLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        iconSTLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        iconSTLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        iconSTLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/geost.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(iconSTLabel, org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.iconSTLabel.text")); // NOI18N
        iconSTLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.iconSTLabel.toolTipText")); // NOI18N
        iconSTLabel.setPreferredSize(new java.awt.Dimension(50, 14));

        visibleSTLabel.setFont(visibleSTLabel.getFont().deriveFont(visibleSTLabel.getFont().getSize()-1f));
        visibleSTLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(visibleSTLabel, org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.visibleSTLabel.text")); // NOI18N
        visibleSTLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        overviewCheckBox.setFont(overviewCheckBox.getFont().deriveFont(overviewCheckBox.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(overviewCheckBox, org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.overviewCheckBox.text")); // NOI18N
        overviewCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.overviewCheckBox.toolTipText")); // NOI18N
        overviewCheckBox.setContentAreaFilled(false);
        overviewCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        overviewCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        overviewCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overviewCheckBoxActionPerformed(evt);
            }
        });

        readyCheckBox.setFont(readyCheckBox.getFont().deriveFont(readyCheckBox.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(readyCheckBox, org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.readyCheckBox.text")); // NOI18N
        readyCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectedUserFrame.class, "ConnectedUserFrame.readyCheckBox.toolTipText")); // NOI18N
        readyCheckBox.setContentAreaFilled(false);
        readyCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        readyCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        readyCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readyCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iconFamLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iconIndiLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(visibleIndiLabel)
                    .addComponent(visibleFamLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iconEvenLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iconSTLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(visibleEvenLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(visibleSTLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(overviewCheckBox, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(readyCheckBox, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(visibleSTLabel)
                    .addComponent(overviewCheckBox)
                    .addComponent(iconIndiLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iconSTLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(visibleIndiLabel))
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(readyCheckBox)
                    .addComponent(iconFamLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(visibleFamLabel)
                    .addComponent(iconEvenLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(visibleEvenLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void overviewCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overviewCheckBoxActionPerformed
        if (overviewCheckBox.isSelected()) {
            overviewCheckBox.setIcon(owner.ROTATING_ICON);
            owner.launchMapsSearch(getUserName());
        } else {
            overviewCheckBox.setSelected(true);
        }
        
    }//GEN-LAST:event_overviewCheckBoxActionPerformed

    private void readyCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readyCheckBoxActionPerformed
        if (readyCheckBox.isSelected()) {
            readyCheckBox.setIcon(owner.ROTATING_ICON);
            owner.launchEventsSearch(getUserName());
        } else {
            readyCheckBox.setSelected(true);
        }

    }//GEN-LAST:event_readyCheckBoxActionPerformed

    private void formInternalFrameActivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameActivated
        focusOther();
    }//GEN-LAST:event_formInternalFrameActivated


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel iconEvenLabel;
    private javax.swing.JLabel iconFamLabel;
    private javax.swing.JLabel iconIndiLabel;
    private javax.swing.JLabel iconSTLabel;
    private javax.swing.JCheckBox overviewCheckBox;
    private javax.swing.JCheckBox readyCheckBox;
    private javax.swing.JLabel visibleEvenLabel;
    private javax.swing.JLabel visibleFamLabel;
    private javax.swing.JLabel visibleIndiLabel;
    private javax.swing.JLabel visibleSTLabel;
    // End of variables declaration//GEN-END:variables

    
    public boolean isMe() {
        return isMe;
    }
    
    public void setInclude(boolean b) {
        include = b;
        hidePseudo(!b, userProfile.pseudo);
    }
    
    public boolean isIncluded() {
        return include;
    }
    
    public boolean isComplete() {
        return stMap != null ? stMap.isComplete() : false;
    }
    
    public String getIPAddress() {
        return userProfile.usePrivateIP ? userProfile.pipAddress : userProfile.ipAddress;
    }

    public String getPortAddress() {
        return userProfile.usePrivateIP ? userProfile.pportAddress : userProfile.portAddress;
    }

    public boolean getUsePrivateIP() {
        return userProfile.usePrivateIP;
    }

    public void setUsePrivateIP(boolean flag) {
        userProfile.usePrivateIP = flag;
    }

    public String getxIPAddress() {
        return userProfile.ipAddress;
    }

    public String getxPortAddress() {
        return userProfile.portAddress;
    }

    public String getpIPAddress() {
        return userProfile.pipAddress;
    }

    public String getpPortAddress() {
        return userProfile.pportAddress;
    }

    public int getNbIndis() {
        return nbIndis;
    }

    public int getNbFams() {
        return nbFamilies;
    }

    public int getNbSTs() {
        return nbSTs;
    }

    public String getTopST() {
        return sTs[0];
    }

    public String[] getSTs() {
        return sTs;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
    
    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setIPAddress(String address) {
        String[] bits = address.split(":");
        if (bits.length != 2) {
            return;
        }
        if (!userProfile.usePrivateIP) {
            userProfile.ipAddress = bits[0];
            userProfile.portAddress = bits[1];
        } else {
            userProfile.pipAddress = bits[0];
            userProfile.pportAddress = bits[1];
        }
    }

    private boolean isPseudoHidden(String pseudo) {
        // return true if exist in list
        String pseudoKey = "HiddenUsers." + pseudo;
        String hiddenUser = NbPreferences.forModule(GedcomCompareOptionsPanel.class).get(pseudoKey, "");
        return hiddenUser.equals(HIDDEN_VALUE);
    }

    private void hidePseudo(boolean hide, String pseudo) {
        // add to list if hide is true else remove
        String pseudoKey = "HiddenUsers." + pseudo;
        if (hide) {
            NbPreferences.forModule(GedcomCompareOptionsPanel.class).put(pseudoKey, HIDDEN_VALUE);
        } else {
            NbPreferences.forModule(GedcomCompareOptionsPanel.class).remove(pseudoKey);
        }
    }


}
