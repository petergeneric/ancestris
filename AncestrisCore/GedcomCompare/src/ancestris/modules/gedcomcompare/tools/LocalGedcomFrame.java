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

import ancestris.api.place.Place;
import ancestris.modules.gedcomcompare.GedcomCompareTopComponent;
import ancestris.modules.gedcomcompare.communication.Comm;
import ancestris.modules.gedcomcompare.options.GedcomCompareOptionsPanel;
import ancestris.modules.geo.GeoPlacesList;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Property;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
public class LocalGedcomFrame extends DataFrame implements ComparedGedcom {

    private String uniqueID;
    private final Gedcom gedcom;
    private STMap stMap;
    
    private int nbIndis;
    private int nbFams;
    private int nbEvens;
    private int nbSTs;

    private boolean busyGedcom = false;

    // Connection stats
    private int connections;
    private Date startDate;
    private Date endDate;
    
    /**
     * Creates new form LocalGedcom
     */
    public LocalGedcomFrame(GedcomCompareTopComponent gctc, Gedcom gedcom, int type) {
        super(gedcom.getDisplayName());
        this.owner = gctc;
        this.gedcom = gedcom;
        this.stMap = null;
        this.type = type;
        uniqueID = UUID.randomUUID().toString();

        connections = 0;
        startDate = new Date();
        endDate = new Date();

        busyGedcom = true;

        initComponents();
        
        // replace existing context menu => only for remote gedcoms /Â for local ones, just get rid of menu
        JPopupMenu popup = new JPopupMenu();
        Action a = new AbstractAction(NbBundle.getMessage(this.getClass(), "ACT_ShowProfile"), getPhotoIcon()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogManager.create(NbBundle.getMessage(this.getClass(), "TITL_ProfilePanel", NbBundle.getMessage(this.getClass(), "TITL_ProfilePanel_Myself")), 
                        new ProfilePanel(owner.getMyProfile(), owner.getMyProfile(), connections, startDate, endDate))
                        .setMessageType(DialogManager.PLAIN_MESSAGE)
                        .setResizable(false)
                        .setOptionType(DialogManager.OK_ONLY_OPTION).show();
            }
        };
        popup.add(new JMenuItem(a));
        Action b = new AbstractAction(NbBundle.getMessage(this.getClass(), "ACT_RefreshMap"), 
                new ImageIcon(ImageUtilities.loadImage("ancestris/modules/gedcomcompare/resources/geost.png"))) {
            @Override
            public void actionPerformed(ActionEvent e) {
                prepareGedcom();
                owner.mainHasChanged(gedcom);
            }
        };
        popup.add(new JMenuItem(b));
        setComponentPopupMenu(popup);
        Container pane = ((BasicInternalFrameUI) getUI()).getNorthPane();
        JButton menuButton = (JButton) pane.getComponent(0);
        MouseListener[] mls = menuButton.getMouseListeners();
        if (mls.length>1 && mls[1] instanceof MouseAdapter) {
            menuButton.removeMouseListener(mls[1]);
        }
        
        
        super.updateColor();

        setReady(false);
        setOpen(false);
        setPrivate(false);
        updateInfo();

        gedcom.addGedcomListener(gedcomChanged);

        busyGedcom = false;
    }

    final public void updateInfo() {

        nbIndis = getNbEntities(Gedcom.INDI);   
        nbFams = getNbEntities(Gedcom.FAM);     
        nbEvens = getNbEvens();
        nbSTs = getNbSTs();

        visibleIndiLabel.setText("" + nbIndis);
        visibleFamLabel.setText("" + nbFams);
        visibleEvenLabel.setText("" + nbEvens);
        visibleSTLabel.setText("" + nbSTs);

        // Update icon if necessary
        owner.updateIcon();
        
        openCheckBox.setVisible(isMain());
        privateCheckBox.setVisible(isMain());
        
        openCheckBox.setEnabled(owner.isSharingOn());
        privateCheckBox.setEnabled(owner.isSharingOn());
        
        owner.updateStatsDisplay();

    }

    private int getNbEntities(String tag) {
        
        if (gedcom == null || gedcom.getName() == null) {
            return 0;
        }

        return gedcom.getEntities(tag).size();
    }

    private int getNbEvens() {
        if (stMap != null) {
            return stMap.getEventNb();
        }
        return 0;
    }
        

    private int getNbSTs() {
        if (stMap != null) {
            return stMap.keySet().size();
        }
        return 0;
    }
        
    
    
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
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
        readyCheckBox = new javax.swing.JCheckBox();
        openCheckBox = new javax.swing.JCheckBox();
        privateCheckBox = new javax.swing.JCheckBox();

        setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setToolTipText(org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.toolTipText")); // NOI18N
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/gedcom.png"))); // NOI18N
        setPreferredSize(new java.awt.Dimension(202, 101));
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
        org.openide.awt.Mnemonics.setLocalizedText(iconIndiLabel, org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.iconIndiLabel.text")); // NOI18N
        iconIndiLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.iconIndiLabel.toolTipText")); // NOI18N
        iconIndiLabel.setPreferredSize(new java.awt.Dimension(50, 14));

        visibleIndiLabel.setFont(visibleIndiLabel.getFont().deriveFont(visibleIndiLabel.getFont().getSize()-1f));
        visibleIndiLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(visibleIndiLabel, org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.visibleIndiLabel.text")); // NOI18N
        visibleIndiLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        iconFamLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        iconFamLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        iconFamLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/fam.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(iconFamLabel, org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.iconFamLabel.text")); // NOI18N
        iconFamLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.iconFamLabel.toolTipText")); // NOI18N
        iconFamLabel.setPreferredSize(new java.awt.Dimension(50, 14));

        visibleFamLabel.setFont(visibleFamLabel.getFont().deriveFont(visibleFamLabel.getFont().getSize()-1f));
        visibleFamLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(visibleFamLabel, org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.visibleFamLabel.text")); // NOI18N
        visibleFamLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        iconEvenLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        iconEvenLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        iconEvenLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/even.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(iconEvenLabel, org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.iconEvenLabel.text")); // NOI18N
        iconEvenLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.iconEvenLabel.toolTipText")); // NOI18N
        iconEvenLabel.setPreferredSize(new java.awt.Dimension(50, 14));

        visibleEvenLabel.setFont(visibleEvenLabel.getFont().deriveFont(visibleEvenLabel.getFont().getSize()-1f));
        visibleEvenLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(visibleEvenLabel, org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.visibleEvenLabel.text")); // NOI18N
        visibleEvenLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        iconSTLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        iconSTLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        iconSTLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/geost.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(iconSTLabel, org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.iconSTLabel.text")); // NOI18N
        iconSTLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.iconSTLabel.toolTipText")); // NOI18N
        iconSTLabel.setPreferredSize(new java.awt.Dimension(50, 14));

        visibleSTLabel.setFont(visibleSTLabel.getFont().deriveFont(visibleSTLabel.getFont().getSize()-1f));
        visibleSTLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(visibleSTLabel, org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.visibleSTLabel.text")); // NOI18N
        visibleSTLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        readyCheckBox.setFont(readyCheckBox.getFont().deriveFont(readyCheckBox.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(readyCheckBox, org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.readyCheckBox.text")); // NOI18N
        readyCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.readyCheckBox.toolTipText")); // NOI18N
        readyCheckBox.setContentAreaFilled(false);
        readyCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        readyCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        readyCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readyCheckBoxActionPerformed(evt);
            }
        });

        openCheckBox.setFont(openCheckBox.getFont().deriveFont(openCheckBox.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(openCheckBox, org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.openCheckBox.text")); // NOI18N
        openCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.openCheckBox.toolTipText")); // NOI18N
        openCheckBox.setContentAreaFilled(false);
        openCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        openCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        openCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openCheckBoxActionPerformed(evt);
            }
        });

        privateCheckBox.setFont(privateCheckBox.getFont().deriveFont(privateCheckBox.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(privateCheckBox, org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.privateCheckBox.text")); // NOI18N
        privateCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(LocalGedcomFrame.class, "LocalGedcomFrame.privateCheckBox.toolTipText")); // NOI18N
        privateCheckBox.setContentAreaFilled(false);
        privateCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        privateCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        privateCheckBox.setPreferredSize(new java.awt.Dimension(65, 18));
        privateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                privateCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(iconIndiLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(iconFamLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(visibleIndiLabel)
                            .addComponent(visibleFamLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(iconEvenLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(iconSTLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(visibleSTLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(visibleEvenLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(readyCheckBox, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(openCheckBox, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(privateCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(iconSTLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(visibleSTLabel)
                    .addComponent(readyCheckBox)
                    .addComponent(iconIndiLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(visibleIndiLabel))
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(visibleFamLabel)
                    .addComponent(openCheckBox)
                    .addComponent(iconFamLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iconEvenLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(visibleEvenLabel))
                .addGap(3, 3, 3)
                .addComponent(privateCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void privateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateCheckBoxActionPerformed
        setPrivate(isPrivate());
    }//GEN-LAST:event_privateCheckBoxActionPerformed

    private void readyCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readyCheckBoxActionPerformed
        prepareGedcom();
    }//GEN-LAST:event_readyCheckBoxActionPerformed

    private void formInternalFrameActivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameActivated
        if (!isMain()) {
            focusOther();
        }
    }//GEN-LAST:event_formInternalFrameActivated

    private void openCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openCheckBoxActionPerformed
        setOpen(owner.isSharingOn());
    }//GEN-LAST:event_openCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel iconEvenLabel;
    private javax.swing.JLabel iconFamLabel;
    private javax.swing.JLabel iconIndiLabel;
    private javax.swing.JLabel iconSTLabel;
    private javax.swing.JCheckBox openCheckBox;
    private javax.swing.JCheckBox privateCheckBox;
    private javax.swing.JCheckBox readyCheckBox;
    private javax.swing.JLabel visibleEvenLabel;
    private javax.swing.JLabel visibleFamLabel;
    private javax.swing.JLabel visibleIndiLabel;
    private javax.swing.JLabel visibleSTLabel;
    // End of variables declaration//GEN-END:variables

    public void close() {
        gedcom.removeGedcomListener(gedcomChanged);
    }

    @Override
    public String getID() {
        return uniqueID;
    }

    @Override
    public STMap getMap() {
        return stMap;
    }

    @Override
    public String getUserName() {
        return super.getTitle();
    }
    
    public Gedcom getGedcom() {
        return gedcom;
    }

    public void setMain(boolean set) {
        super.setMain(set);
        openCheckBox.setVisible(isMain());
        privateCheckBox.setVisible(isMain());
        owner.updateStatsDisplay();
        owner.mainHasChanged(gedcom);
    }

    public final void setReady(boolean set) {
        readyCheckBox.setSelected(set);
        readyCheckBox.setIcon(set ? owner.SELECTEDON_ICON : null);
        if (set) {
            ComparisonFrame cf = owner.getComparisonFrame(this);
            if (cf != null) {
                cf.launchCompare(true);
            }
        }
        updateInfo();
        owner.updateStatsDisplay();
    }
    
    public final void setOpen(boolean set) {
        openCheckBox.setSelected(set);
        openCheckBox.setIcon(set ? owner.SELECTEDON_ICON : null);
        setPrivate(set);
        updateInfo();
        updatePhoto(set);
    }
    
    public void updatePhoto(boolean set) {
        if (set) {
            setFrameIcon(getPhotoIcon());
        } else {
            setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/gedcom.png")));
        }
    }

    private ImageIcon getPhotoIcon() {
        return GedcomCompareOptionsPanel.getPhoto(0, owner.getMyProfile().photoBytes);
    }

    
    public final void setPrivate(boolean set) {
        privateCheckBox.setSelected(set);
        privateCheckBox.setSelectedIcon(set ? owner.PRIVATEON_ICON : null);
        if (owner.getCommHandler() != null) {
            owner.getCommHandler().resetPrivacy();
        }
    }
    
    public boolean isReady() {
        return readyCheckBox.isSelected();
    }
    
    public boolean isOpen() {
        return openCheckBox.isSelected();
    }

    public boolean isPrivate() {
        return privateCheckBox.isSelected();
    }


    


    // make gedcom ready to compare
    private void prepareGedcom() {

        setReady(false);
        //TimingUtility.getInstance().reset();
        Map<Place, Set<Property>> map = GeoPlacesList.getInstance(gedcom).getPlaces(true, okWhenDone(), cancelWhenDone());
        if (map == null) {
            readyCheckBox.setIcon(owner.ROTATING_ICON);
            return;
        }
        
        // Should never happen but we never know...
        stMap = STFactory.buildSTMap(map);
        setReady(true);
    }

    private Callable okWhenDone() {

        Callable ret = (Callable) () -> {
            Map<Place, Set<Property>> map = GeoPlacesList.getInstance(gedcom).getPlaces(true, null, null);
            if (map != null) {
                stMap = STFactory.buildSTMap(map);
                setReady(true);
            } else {
                setReady(false);
            }
            updateInfo();
            return null;
        };
        return ret;
    }

    private Callable cancelWhenDone() {

        Callable ret = (Callable) () -> {
            setReady(false);
            return null;
        };
        return ret;
    }

    


    public String[] getSummary(int length) {
        
        // Init with values
        String[] ret = new String[length];
        for (int i=0; i< ret.length; i++) {
            ret[i] = "-";
        }
        
        // Set preferences
        ret[0] = isPrivate() ? "1" : "0";
        ret[1] = ""+gedcom.getIndis().size();
        ret[2] = ""+gedcom.getFamilies().size();
        ret[3] = ""+stMap.keySet().size();
        ret[4] = ""+stMap.getEventNb();
        
        // Set map keys (COMM_NBST)
        String[] keys = stMap.getTopSpaceKeys(Comm.COMM_NBST);
        for (int i=0; i< keys.length; i++) {
            ret[5+i] = keys[i];
        }
        
        // Set stats
        ret[5+Comm.COMM_NBST] = "0";
        ret[5+Comm.COMM_NBST+1] = "0";
        ret[5+Comm.COMM_NBST+2] = "0";
        
        return ret;
    }


    public int getNbOfPublicIndis() {
        return nbIndis;
    }

    public int getNbOfPublicFams() {
        return nbFams;
    }

    public int getNbOfSTs() {
        return nbSTs;
    }

    public String[] getSTs() {
        String[] nulRet = { "" };
        return stMap != null ? stMap.getTopSpaceKeys(Comm.COMM_NBST) : nulRet;
    }

    public void addConnection() {
        if (connections == 0) {
            startDate = new Date();
        }
        connections++;
        endDate = new Date();
    }







    

    // Gedcom listeners
    private final GedcomChanged gedcomChanged = new GedcomChanged();

    private class GedcomChanged extends GedcomListenerAdapter {

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
                        updateInfo();
                        busyGedcom = false;
                    }
                });
                return;
            }
        });

        // Launch timer
        timer.start();
    }

}
