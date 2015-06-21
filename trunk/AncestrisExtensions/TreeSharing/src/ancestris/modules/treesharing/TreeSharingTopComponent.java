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
package ancestris.modules.treesharing;

import ancestris.gedcom.GedcomDirectory;
import ancestris.modules.treesharing.communication.AncestrisMember;
import ancestris.modules.treesharing.panels.SettingsAction;
import ancestris.modules.treesharing.communication.Comm;
import ancestris.modules.treesharing.options.TreeSharingOptionsPanelController;
import ancestris.modules.treesharing.panels.AncestrisFriend;
import ancestris.modules.treesharing.panels.MembersPopup;
import ancestris.modules.treesharing.panels.PrivacyToggle;
import ancestris.modules.treesharing.panels.RearrangeAction;
import ancestris.modules.treesharing.panels.SharedGedcom;
import ancestris.modules.treesharing.panels.StartSharingAllToggle;
import ancestris.modules.treesharing.panels.StopSharingAllToggle;
import ancestris.modules.treesharing.panels.TimerPanel;
import org.openide.util.ImageUtilities;
import ancestris.modules.treesharing.panels.TreeSharingPanel;
import ancestris.swing.ToolBar;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.netbeans.api.settings.ConvertAsProperties;
import static org.openide.awt.DropDownButtonFactory.createDropDownButton;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;

/**
 *
 * @author frederic
 */
@ConvertAsProperties(dtd = "-//ancestris.modules.treesharing//EN",
autostore = false)
public class TreeSharingTopComponent extends TopComponent {

    // Top component elements
    private static TreeSharingTopComponent instance;
    private static final String PREFERRED_ID = "TreeSharingTopComponent";  // NOI18N
    private static final String ICON_PATH = "ancestris/modules/treesharing/resources/treesharing.png";
    
    // Panel elements
    private boolean isComponentCreated = false;
    private JToolBar toolbar = null;
    private TreeSharingPanel desktopPanel = null;
    private final String TOOLBAR_SPACE = "  ";
    private final BorderLayout borderLayout;
    private String defaultBorderLayout = BorderLayout.NORTH;
    private PrivacyToggle privacyToggle;
    private TimerPanel timerPanel;
    private boolean shareAll = false;
    private StartSharingAllToggle startSharingToggle;
    private StopSharingAllToggle stopSharingToggle;
    private final int LEFT_OFFSET_GEDCOM = 10;
    private final int TOP_OFFSET = 10;
    private final int VERTICAL_SPACE = 10;
    private final int LEFT_OFFSET_FRIENDS = 400;
    
    // Sharing elements
    private boolean isBusy = false;
    private Comm commHandler = null;
    private List<SharedGedcom> sharedGedcoms = null;
    private List<AncestrisMember> ancestrisMembers = null; // all connected members
    private List<AncestrisFriend> ancestrisFriends = null; // only members with entities in common
    

    
    
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized TreeSharingTopComponent getDefault() {
        if (instance == null) {
            instance = new TreeSharingTopComponent();
        }
        return instance;
    }

    
    

    /**
     * Constructor
     */
    public TreeSharingTopComponent() {
        if (instance == null) {
            instance = this;
        }
        setName(NbBundle.getMessage(TreeSharingTopComponent.class, "CTL_TreeSharingTopComponent"));
        setToolTipText(NbBundle.getMessage(TreeSharingTopComponent.class, "HINT_TreeSharingTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        borderLayout = new BorderLayout();  // has to be border
        this.setLayout(borderLayout);
    }

    
    @Override
    public void componentOpened() {
        if (!isComponentCreated) {
            initCommunication();
            initAncestrisMembers();

            // retrieve last position of toolbar when modified
            defaultBorderLayout = NbPreferences.forModule(TreeSharingTopComponent.class).get("ToolbarBorderLayout", BorderLayout.NORTH);
            initMainPanel();

            initSharedGedcoms();
        }
        
        privacyToggle.setPrivacy(getPreferredPrivacy());
        isComponentCreated = true;
    }

    private void initMainPanel() {

        // Create toolbar
        toolbar = new ToolBar();
        
        // Add toolbar elements
        
        // - Set Privacy ON/OFF
        privacyToggle = new PrivacyToggle(this, getPreferredPrivacy());
        toolbar.add(privacyToggle);
        
        // - Timer display
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 
        timerPanel = new TimerPanel(this);
        toolbar.add(timerPanel);

        // - Dropbox on all connected friends
        toolbar.add(new JLabel(TOOLBAR_SPACE));
        MembersPopup membersList = new MembersPopup(this, ancestrisMembers);
        JButton members = createDropDownButton(new ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/friend24.png")), membersList);
        members.setToolTipText(NbBundle.getMessage(MembersPopup.class, "TIP_MembersList"));
        toolbar.add(members);
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 
        toolbar.addSeparator();

        // - General share button : ON/OFF
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 
        startSharingToggle = new StartSharingAllToggle(this, shareAll);
        toolbar.add(startSharingToggle);
        stopSharingToggle = new StopSharingAllToggle(this, !shareAll);
        toolbar.add(stopSharingToggle);
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 
        toolbar.addSeparator();

        toolbar.add(new Box.Filler(null, null, null), "growx, pushx, center");

        // - Rearrange gedcoms and Friends on desktop
        toolbar.addSeparator();
        toolbar.add(new RearrangeAction(this));
        // - Settings
        toolbar.add(new SettingsAction());

        // Add toolbar
        add(toolbar, defaultBorderLayout);
        
        // Main panel
        desktopPanel = new TreeSharingPanel();
        add(desktopPanel);
    }

    
    @Override
    protected void addImpl(Component comp, Object constraints, int index) {

        // At toolbar modification, fix and store position of toolbar
        if ((toolbar != null) && (comp == toolbar)) {
            // find orientation
            int orientation = SwingConstants.HORIZONTAL;
            if (BorderLayout.WEST.equals(constraints) || BorderLayout.EAST.equals(constraints)) {
                orientation = SwingConstants.VERTICAL;
            }
            // fix orientation for toolbar
            defaultBorderLayout = (String) constraints;
            NbPreferences.forModule(TreeSharingTopComponent.class).put("ToolbarBorderLayout", defaultBorderLayout);
            toolbar.setOrientation(orientation);
        }

        // go ahead with super
        super.addImpl(comp, constraints, index);
    }
    
    @Override
    public void componentClosed() {
        stopSharingToggle.doClick();
    }

        
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }    

    public void readProperties(java.util.Properties p) {
    }

    public void writeProperties(java.util.Properties p) {
        // only called at Ancestris closing, not called at component closing, so do not use to save toolbar position
    }

    
    
    
    
    
    
    
    private void initCommunication() {
        commHandler = new Comm();
    }

    private void initAncestrisMembers() {
        ancestrisMembers = commHandler.getAncestrisMembers();
    }
    
    private void initSharedGedcoms() {
        
        // Init list
        sharedGedcoms = new LinkedList<SharedGedcom>();
        
        // Get open gedcoms and build shared objects
        for (Context context : GedcomDirectory.getDefault().getContexts()) {
            sharedGedcoms.add(new SharedGedcom(context.getGedcom(), privacyToggle.isSelected()));
        }
        
        // Display shared Gedcoms for the first time on the desktop
        desktopPanel.setFrames(sharedGedcoms, LEFT_OFFSET_GEDCOM, TOP_OFFSET, VERTICAL_SPACE, true);
    }

    private void initAncestrisFriends() {

        // Init list
        //TODO : ancestrisFriends = XXX();

        // Display shared Friends for the first time on the desktop
        desktopPanel.setFrames(ancestrisFriends, LEFT_OFFSET_FRIENDS, TOP_OFFSET, VERTICAL_SPACE, true);
    }

    public void rearrangeWindows() {
        if (sharedGedcoms != null && !sharedGedcoms.isEmpty()) {
            desktopPanel.setFrames(sharedGedcoms, LEFT_OFFSET_GEDCOM, TOP_OFFSET, VERTICAL_SPACE, false);
        }
        if (ancestrisFriends != null && !ancestrisFriends.isEmpty()) {
            desktopPanel.setFrames(ancestrisFriends, LEFT_OFFSET_FRIENDS, TOP_OFFSET, VERTICAL_SPACE, false);
        }
    }
    

    // Privacy management
    
    private boolean getPreferredPrivacy() {
        return NbPreferences.forModule(TreeSharingOptionsPanelController.class).getBoolean("RespectPrivacy", true);
    }

    public void dispatchPrivacy(boolean b) {
        if (sharedGedcoms == null) {
            return;
        }
        for (SharedGedcom sg : sharedGedcoms) {
            sg.setPrivacy(b);
        }
    }

    public void dispatchRecalc() {
        if (sharedGedcoms == null) {
            return;
        }
        for (SharedGedcom sg : sharedGedcoms) {
            sg.updateStats(true);
        }
    }

    
    
    
    public boolean isShareAllOn() {
        return shareAll;
    }
    
    public boolean isBusy() {
        return isBusy;
    }
    
    public void startSharingAll() {
        shareAll = true;
        isBusy = true;
        stopSharingToggle.setSelected(!shareAll);
        stopSharingToggle.setEnabled(shareAll);
        stopSharingToggle.setToolTipText();
        //timerPanel.setTimerEnabled(false);
        dispatchShare(true);
        isBusy = false;
    }

    public void stopSharingAll() {
        shareAll = false;
        isBusy = true;
        startSharingToggle.setSelected(shareAll);
        startSharingToggle.setEnabled(!shareAll);
        startSharingToggle.setToolTipText();
        //timerPanel.setTimerEnabled(true);
        dispatchShare(false);
        isBusy = false;
    }

    private void dispatchShare(boolean b) {
        if (sharedGedcoms == null) {
            return;
        }
        for (SharedGedcom sg : sharedGedcoms) {
            sg.setShared(b);
        }
    }

    public void gedcomOpened(Gedcom gedcom) {
        if (!isComponentCreated) {
            return;
        }
//        try {
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
        SharedGedcom newSharedGedcom = new SharedGedcom(gedcom, privacyToggle.isSelected());
        desktopPanel.addFrame(newSharedGedcom, findLocation(sharedGedcoms.size(), newSharedGedcom.getPreferredSize().height));
        sharedGedcoms.add(newSharedGedcom);
    }
    
    public void gedcomClosed(Gedcom gedcom) {
        if (!isComponentCreated) {
            return;
        }
        for (SharedGedcom sg : sharedGedcoms) {
            if (sg.getGedcom() == gedcom) {
                desktopPanel.removeFrame(sg);
                sharedGedcoms.remove(sg);
                break;
            }
        }
    }

    private Point findLocation(int nbElements, int height) {
        
        if (sharedGedcoms.isEmpty()) {
            return new Point(10, 10);
        }
        return new Point(LEFT_OFFSET_GEDCOM, TOP_OFFSET + nbElements * (height + VERTICAL_SPACE));
    }

}
