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
package ancestris.modules.gedcomcompare;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomDirectory;
import ancestris.modules.console.Console;
import ancestris.modules.gedcomcompare.communication.Comm;
import ancestris.modules.gedcomcompare.communication.Comm.User;
import ancestris.modules.gedcomcompare.communication.UserProfile;
import ancestris.modules.gedcomcompare.options.GedcomCompareOptionsPanel;
import ancestris.modules.gedcomcompare.tools.ComparedGedcom;
import ancestris.modules.gedcomcompare.tools.ComparisonFrame;
import ancestris.modules.gedcomcompare.tools.ConnectedGedcomsPopup;
import ancestris.modules.gedcomcompare.tools.ConnectedUserFrame;
import ancestris.modules.gedcomcompare.tools.DataFrame;
import ancestris.modules.gedcomcompare.tools.DisplayStatsAction;
import ancestris.modules.gedcomcompare.tools.GedcomComparePanel;
import ancestris.modules.gedcomcompare.tools.LocalGedcomFrame;
import ancestris.modules.gedcomcompare.tools.LocalGedcomsPopup;
import ancestris.modules.gedcomcompare.tools.RearrangeAction;
import ancestris.modules.gedcomcompare.tools.STFactory;
import ancestris.modules.gedcomcompare.tools.STMapCapsule;
import ancestris.modules.gedcomcompare.tools.STMapEventsCapsule;
import ancestris.modules.gedcomcompare.tools.SearchAction;
import ancestris.modules.gedcomcompare.tools.SettingsAction;
import ancestris.modules.gedcomcompare.tools.StartSharingAllToggle;
import ancestris.modules.gedcomcompare.tools.StatsPanel;
import ancestris.modules.gedcomcompare.tools.StopSharingAllToggle;
import ancestris.swing.ToolBar;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.apache.commons.lang.StringEscapeUtils;
import static org.openide.awt.DropDownButtonFactory.createDropDownButton;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
@ServiceProvider(service = TopComponent.class)
public class GedcomCompareTopComponent extends TopComponent {

    // Top component elements
    private static final String PREFERRED_ID = "GedcomCompareTopComponent";  // NOI18N
    private static final Logger LOG = Logger.getLogger("ancestris.gedcomcompare");   // NOI18N
    private static final Console console = new Console(NbBundle.getMessage(GedcomCompareTopComponent.class, "CTL_GedcomCompareTopComponent"));

    private static GedcomComparePlugin gedcomComparePlugin;
    private static GedcomCompareTopComponent instance;

    // icons
    private static final String ICON_PATH = "ancestris/modules/gedcomcompare/resources/comparegedcom.png";   // NOI18N
    private static final String ICON_PATH_ON = "ancestris/modules/gedcomcompare/resources/comparegedcomON.png";   // NOI18N
    public final ImageIcon ROTATING_ICON = new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/rotating16.gif"));   // NOI18N
    public final ImageIcon SELECTEDON_ICON = new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/selected.png"));   // NOI18N
    public final ImageIcon PRIVATEON_ICON = new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/private.png"));   // NOI18N
    private final ImageIcon INDI_ICON = new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/indi.png"));   // NOI18N
    private final ImageIcon FAM_ICON = new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/fam.png"));   // NOI18N
    private final ImageIcon GEOST_ICON = new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/geost.png"));   // NOI18N
    private final ImageIcon STAR_ICON = new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/star.png"));   // NOI18N
    private final ImageIcon CONM_ICON = new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/connm.png"));   // NOI18N
    private final ImageIcon CONU_ICON = new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/connu.png"));   // NOI18N
    private final ImageIcon CONOVERLAP_ICON = new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/connoverlap.png"));   // NOI18N

    // Main data elements
    private List<LocalGedcomFrame> localGedcomFrames = null;            // iFrames : all local gedcoms
    private List<ComparisonFrame> comparisonFrames = null;              // iFrames : comparison between gedcoms
    private List<ConnectedUserFrame> connectedUserFrames = null;        // iFrames : all remote frames

    // Main Panel elements
    private final String TOOLBAR_SPACE = "  ";
    private String titleComponent = "";
    private boolean isComponentCreated = false;
    private JToolBar toolbar = null;
    private GedcomComparePanel desktopPanel = null;
    private StartSharingAllToggle startSharingToggle;
    private StopSharingAllToggle stopSharingToggle;
    private SearchAction searchButton = null;
    private JLabel memberInProgress = null;
    private DisplayStatsAction statsButton = null;
    private SettingsAction settings = null;

    // Local gedcoms menu and elements
    private LocalGedcomsPopup localGedcomsPopup = null;
    private JButton gedcomsButton = null;

    // Connected newUsersList menu and elements
    private ConnectedGedcomsPopup connectedUsersPopup = null;

    // Sharing elements
    private final int PING_DELAY = 150;   // seconds to maintain socket with server
    private Comm commHandler = null;
    private boolean isBusy = false;
    private boolean sharing = false;
    private String commPseudo = "";
    private String[] commMyInfo = new String[Comm.COMM_PREF + Comm.COMM_NBST];
    private javax.swing.Timer swingTimer;
    private final int REFRESH_DELAY = 50; // seconds to refresh newUsersList list

    // Searching elements
    private SearchUsers searchThread;

    // Stats elements
    private StatsPanel stats = null;
    private JLabel statsConnectedUsers = null;
    private JLabel statsIndis = null;
    private JLabel statsFams = null;
    private JLabel statsSTs = null;
    private JLabel statsAreas = null;
    private JLabel statsReceivedConnections = null;
    private JLabel statsReceivedUniqueUsers = null;
    private JLabel statsUniqueOverlaps = null;

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only, i.e. deserialization routines; otherwise you could get a non-deserialized instance. To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized GedcomCompareTopComponent getDefault() {
        if (instance == null) {
            instance = new GedcomCompareTopComponent();
        }
        return instance;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public void open() {
        Mode mode = WindowManager.getDefault().findMode("ancestris-output");
        if (mode != null) {
            mode.dockInto(this);
        }
        super.open();
    }

    /**
     * Constructor
     */
    public GedcomCompareTopComponent() {

        if (instance == null) {
            instance = this;
        }
        gedcomComparePlugin = new GedcomComparePlugin();

        titleComponent = NbBundle.getMessage(GedcomCompareTopComponent.class, "CTL_GedcomCompareTopComponent");
        setName(titleComponent);
        setToolTipText(NbBundle.getMessage(GedcomCompareTopComponent.class, "HINT_GedcomCompareTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        setLayout(new BorderLayout());

        statsConnectedUsers = new JLabel("");
        statsIndis = new JLabel("", INDI_ICON, SwingConstants.TRAILING);
        statsFams = new JLabel("", FAM_ICON, SwingConstants.TRAILING);
        statsSTs = new JLabel("", GEOST_ICON, SwingConstants.TRAILING);
        statsAreas = new JLabel("", STAR_ICON, SwingConstants.TRAILING);
        statsReceivedConnections = new JLabel("", CONM_ICON, SwingConstants.TRAILING);
        statsReceivedUniqueUsers = new JLabel("", CONU_ICON, SwingConstants.TRAILING);
        statsUniqueOverlaps = new JLabel("", CONOVERLAP_ICON, SwingConstants.TRAILING);
        
    }

    @Override
    public void componentOpened() {
        AncestrisPlugin.register(gedcomComparePlugin);

        if (!isComponentCreated) {

            initMainPanel();
            
            initLocalGedcoms();

            initConnectedUsers();

            initToolbar();

            initSwingTimerRefreshUsers();

        }

        isComponentCreated = true;
    }

    @Override
    public boolean canClose() {
        if (sharing) {
            LOG.log(Level.FINE, "Closing component connection.");
            stopSharing();
        }
        if (swingTimer != null) {
            swingTimer.stop();
        }
        updateIcon();
        AncestrisPlugin.unregister(gedcomComparePlugin);
        return super.canClose();
    }

    private void initMainPanel() {

        desktopPanel = new GedcomComparePanel();
        add(desktopPanel);
        desktopPanel.addComponentListener(new ComponentListener() { // make sure windows are rearranged at opening (and let them be rearranged afterwards)
            @Override
            public void componentResized(ComponentEvent e) {
                rearrangeWindows(false);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

    }
    
    private void initLocalGedcoms() {
        LOG.log(Level.FINE, "Initializing gedcoms panels.");

        // create local gedcoms
        localGedcomFrames = new ArrayList<>();
        boolean isFirst = true;
        for (Context context : GedcomDirectory.getDefault().getContexts()) {
            localGedcomFrames.add(new LocalGedcomFrame(this, context.getGedcom(), isFirst ? DataFrame.GEDCOM_TYPE_LOCAL_MAIN : DataFrame.GEDCOM_TYPE_LOCAL_OTHER));
            isFirst = false;
        }
        

    }

    private void initConnectedUsers() {
        LOG.log(Level.FINE, "Creating communication handler.");

        // open communication
        commHandler = new Comm(this, PING_DELAY);

        // getPackets connected newUsersList for the first time and update counter labels
        connectedUserFrames = new ArrayList<>();
        updateConnectedUsers(false);

        // detect change of profile
        NbPreferences.forModule(GedcomCompareOptionsPanel.class).addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            if (evt.getKey().equals("Photo")) {
                resetProfile();
            }
        });
        
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
            NbPreferences.forModule(GedcomCompareTopComponent.class).put("ToolbarBorderLayout", (String) constraints);
            toolbar.setOrientation(orientation);
        }

        // go ahead with super
        super.addImpl(comp, constraints, index);
    }

    private void initToolbar() {
        LOG.log(Level.FINE, "Initializing main panel.");

        // Create toolbar
        toolbar = new ToolBar();

        // Local Gedcoms 
        localGedcomsPopup = new LocalGedcomsPopup(localGedcomFrames);
        gedcomsButton = createDropDownButton(new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/gedcom.png")), localGedcomsPopup);
        gedcomsButton.setToolTipText(NbBundle.getMessage(getClass(), "TIP_GedcomsList"));
        gedcomsButton.setEnabled(!localGedcomFrames.isEmpty());
        toolbar.add(gedcomsButton);

        //----------
        toolbar.add(new JLabel(TOOLBAR_SPACE));
        toolbar.addSeparator();
        toolbar.add(new JLabel(TOOLBAR_SPACE));

        // Connected User space
        connectedUsersPopup = new ConnectedGedcomsPopup(connectedUserFrames);
        JButton membersButton = createDropDownButton(new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/friend16.png")), connectedUsersPopup);
        membersButton.setToolTipText(NbBundle.getMessage(getClass(), "TIP_UsersList"));
        membersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (commHandler != null) {
                    commHandler.clearCommunicationError();
                }
                boolean ret = GedcomCompareTopComponent.this.updateConnectedUsers(false);;
                if (ret) {
                    updateStatsDisplay();
                    if (commHandler != null) {
                        commHandler.sendPing();
                    }
                }
            }
        });
        toolbar.add(membersButton);
        toolbar.add(statsConnectedUsers);
        toolbar.add(new JLabel(TOOLBAR_SPACE));

        
        // - Sharing space : General share button : ON/OFF
        startSharingToggle = new StartSharingAllToggle(this, sharing);
        toolbar.add(startSharingToggle);
        stopSharingToggle = new StopSharingAllToggle(this, !sharing);
        toolbar.add(stopSharingToggle);


        //----------
        toolbar.add(new JLabel(TOOLBAR_SPACE));
        toolbar.addSeparator();
        toolbar.add(new JLabel(TOOLBAR_SPACE));

        // Search space : button
        searchButton = new SearchAction(this);
        searchButton.setOff();
        toolbar.add(searchButton);
        toolbar.add(new JLabel(TOOLBAR_SPACE));

        // - Search space : user in progress
        memberInProgress = new JLabel("");
        memberInProgress.setToolTipText(NbBundle.getMessage(getClass(), "TIP_UserInProgress"));
        toolbar.add(memberInProgress);
        toolbar.add(new JLabel(TOOLBAR_SPACE));

        //----------
        toolbar.add(new JLabel(TOOLBAR_SPACE));
        toolbar.addSeparator();
        toolbar.add(new JLabel(TOOLBAR_SPACE));

        // Stats space
        statsButton = new DisplayStatsAction(this);
        statsButton.setEnabled(true);
        toolbar.add(statsButton);
        toolbar.add(new JLabel(TOOLBAR_SPACE));
        //...
        statsIndis.setToolTipText(NbBundle.getMessage(getClass(), "TIP_statsIndis"));
        toolbar.add(statsIndis);
        toolbar.add(new JLabel(TOOLBAR_SPACE));

        statsFams.setToolTipText(NbBundle.getMessage(getClass(), "TIP_statsFams"));
        toolbar.add(statsFams);
        toolbar.add(new JLabel(TOOLBAR_SPACE));

        statsSTs.setToolTipText(NbBundle.getMessage(getClass(), "TIP_statsSTs"));
        toolbar.add(statsSTs);
        toolbar.add(new JLabel(TOOLBAR_SPACE));

        statsAreas.setToolTipText(NbBundle.getMessage(getClass(), "TIP_statsAreas"));
        toolbar.add(statsAreas);
        toolbar.add(new JLabel(TOOLBAR_SPACE));
        toolbar.add(new JLabel(TOOLBAR_SPACE));

        //  Connections
        toolbar.add(statsReceivedConnections);
        toolbar.add(new JLabel(TOOLBAR_SPACE));
        statsReceivedConnections.setToolTipText(NbBundle.getMessage(getClass(), "TIP_statsReceivedConnections"));
        //...
        toolbar.add(statsReceivedUniqueUsers);
        toolbar.add(new JLabel(TOOLBAR_SPACE));
        statsReceivedUniqueUsers.setToolTipText(NbBundle.getMessage(getClass(), "TIP_statsReceivedUniqueUsers"));
        //...
        toolbar.add(statsUniqueOverlaps);
        toolbar.add(new JLabel(TOOLBAR_SPACE));
        statsUniqueOverlaps.setToolTipText(NbBundle.getMessage(getClass(), "TIP_statsUniqueOverlaps"));

        // - Glue
        toolbar.add(new Box.Filler(null, null, null), "growx, pushx, center");

        // - Settings space : Rearrange gedcoms and Friends on desktop
        toolbar.addSeparator();
        toolbar.add(new RearrangeAction(this));
        // - Settings space : Settings
        settings = new SettingsAction();
        toolbar.add(settings);

        // Add toolbar
        add(toolbar, NbPreferences.forModule(GedcomCompareTopComponent.class).get("ToolbarBorderLayout", BorderLayout.NORTH));

        // Display Gedcoms for the first time on the desktop (TRUE)
        updateStatsDisplay();
        createComparisonFrames();
        rearrangeWindows(true);

    }

    private void initSwingTimerRefreshUsers() {
        
        LOG.log(Level.FINE, "Creating refreshing toolbar swing timer.");

        swingTimer = new javax.swing.Timer(REFRESH_DELAY * 1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean ok = updateConnectedUsers(true);
                if (!ok && isSharingOn()) {
                    stopSharing();
                }
            }
        });
        swingTimer.setInitialDelay(REFRESH_DELAY * 1000);
        swingTimer.start();
    }


    // ***************************

    public void rearrangeWindows(boolean create) {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                desktopPanel.placeFrames(getComparedFrames(), comparisonFrames, create);
            }
        });

    }

    public void createComparisonFrames() {

        // create comparison pairs between main and the others
        if (comparisonFrames == null) {
            comparisonFrames = new ArrayList<>();
        }
        
        // Need at least a main file and one other, local or remote
        if (localGedcomFrames.size() == 0 || (localGedcomFrames.size() + connectedUserFrames.size()) < 2) {
            return;
        }

        // Update
        LocalGedcomFrame mainGedcom = getMain();
        if (mainGedcom != null) {
            for (DataFrame frame : getComparedFrames()) {
                if (frame == mainGedcom) {
                    continue;
                }
                ComparisonFrame cf = getComparisonFrame(mainGedcom, (ComparedGedcom) frame);
                if (cf == null) {
                    cf = new ComparisonFrame(this, mainGedcom, (ComparedGedcom) frame);
                    comparisonFrames.add(cf);
                    desktopPanel.addFrame(cf);
                } else {
                    cf.updateMain(mainGedcom);
                }
            }
        }
    }
    
    public List<LocalGedcomFrame> getLocalGedcoms() {
        return localGedcomFrames;
    }

    private List<? extends DataFrame> getComparedFrames() {
        List<DataFrame> ret = new ArrayList<>();
        if (localGedcomFrames != null) {
            ret.addAll(localGedcomFrames);
        }
        if (connectedUserFrames != null) {
            ret.addAll(connectedUserFrames);
        }
        return ret;
    }

    
    public void gedcomOpened(Gedcom gedcom) {
        LOG.log(Level.FINE, "Gedcom opened... (" + gedcom.getName() + ")");
        if (!isComponentCreated) {
            return;
        }
        LocalGedcomFrame newGedcomFrame = new LocalGedcomFrame(this, gedcom, DataFrame.GEDCOM_TYPE_LOCAL_OTHER);
        localGedcomFrames.add(newGedcomFrame);
        if (localGedcomFrames.size() == 1) {
            newGedcomFrame.setMain(true);
        }
        createComparisonFrames();
        desktopPanel.addFrame(newGedcomFrame);
        rearrangeWindows(false);
        gedcomsButton.setEnabled(!localGedcomFrames.isEmpty());
        localGedcomsPopup.updateItems();
        updateIcon();
        updateStatsDisplay();
    }

    public void gedcomClosed(Gedcom gedcom) {
        LOG.log(Level.FINE, "Gedcom closed... (" + gedcom.getName() + ")");
        if (!isComponentCreated) {
            return;
        }
        for (LocalGedcomFrame sg : localGedcomFrames) {
            if (sg.getGedcom() == gedcom) {
                removeGedcom(sg);
                break;
            }
        }
        gedcomsButton.setEnabled(!localGedcomFrames.isEmpty());
        localGedcomsPopup.updateItems();
        updateIcon();
        updateStatsDisplay();
        
        // if last gedcom, close connection
        if (sharing && localGedcomFrames.isEmpty()) {
            LOG.log(Level.FINE, "Closing connection, no more gedcom to share.");
            stopSharing();
        }
    }

    private void removeGedcom(LocalGedcomFrame sg) {
        sg.close();
        desktopPanel.removeFrame(sg);
        desktopPanel.removeLink(sg);
        localGedcomFrames.remove(sg);

        // remove comparison frames
        if (comparisonFrames == null) {
            return;
        }

        List<ComparisonFrame> removedComparison = new ArrayList<ComparisonFrame>();
        for (ComparisonFrame frame : comparisonFrames) {
            if (frame.contains(sg)) {
                removedComparison.add(frame);
            }
        }
        for (ComparisonFrame frame : removedComparison) {
            desktopPanel.removeFrame(frame);
            desktopPanel.removeLink(frame);
            comparisonFrames.remove(frame);
        }
    }

    public void showUserFrame(ConnectedUserFrame user, boolean show) {
        
        if (show) {
            desktopPanel.showFrame(user, true);
            rearrangeWindows(false);
        } else {
            desktopPanel.showFrame(user, false);
            desktopPanel.removeLink(user);

            // remove comparison frames
            if (comparisonFrames == null) {
                return;
            }

            List<ComparisonFrame> removedComparison = new ArrayList<ComparisonFrame>();
            for (ComparisonFrame frame : comparisonFrames) {
                if (frame.contains(user)) {
                    removedComparison.add(frame);
                }
            }

            for (ComparisonFrame frame : removedComparison) {
                desktopPanel.showFrame(frame, false);
                desktopPanel.removeLink(frame);
            }
            
            // refresh users popup list
            updateConnectedUsers(true);
        }

    }
    
    //*************************************************************************************************************************

    public LocalGedcomFrame getMain() {
        for (LocalGedcomFrame f : localGedcomFrames) {
            if (f.isMain()) {
                return f;
            }
        }
        return null;
        
    }
    
    private boolean getPrivacy() {
        LocalGedcomFrame gf = getMain();
        return gf != null ? gf.isPrivate() : false;
    }

    private ComparisonFrame getComparisonFrame(LocalGedcomFrame mainGedcom, ComparedGedcom frame) {
        for (ComparisonFrame f : comparisonFrames) {
            if (f.contains(mainGedcom, frame)) {
                return f;
            }
        }
        return null;
    }

    public void setFocusToComparisonFrame(DataFrame frame) {
        LocalGedcomFrame main = getMain();
        for (ComparisonFrame f : comparisonFrames) {
            if (f.contains(main, (ComparedGedcom)frame)) {
                f.moveToFront();
                return;
            }
        }
    }

    public ComparisonFrame getComparisonFrame(DataFrame frame) {
        LocalGedcomFrame main = getMain();
        for (ComparisonFrame f : comparisonFrames) {
            if (f.contains(main, (ComparedGedcom)frame)) {
                return f;
            }
        }
        return null;
    }
    //*************************************************************************************************************************

    public Comm getCommHandler() {
        return commHandler;
    }

    public List<ConnectedUserFrame> getConnectedUsers() {
        return connectedUserFrames;
    }

    public ConnectedUserFrame getUser(String memberToFind) {
        for (ConnectedUserFrame user : connectedUserFrames) {
            if (user.getName().equals(memberToFind)) {
                return user;
            }
        }
        return null;
    }

    public boolean updateConnectedUsers(boolean quiet) {

        
        // A user is identified by its pseudo (unique identifyer) even though different users can use the same pseudo at different times
        
        // Get new list from server
        List<User> newUsersList = commHandler.getConnectedUsers(quiet);

        // Update connected frames list based on this new list
        if (newUsersList != null) {
            // If user is in the existing connected frames list, update it, otherwise add it
            Set<ConnectedUserFrame> usedFrames = new HashSet<>();
            for (User newUser : newUsersList) {
                ConnectedUserFrame frame = null;
                for (ConnectedUserFrame currentUser : connectedUserFrames) {
                    if (newUser.userProfile.pseudo.equals(currentUser.getUserProfile().pseudo))  {
                        frame = currentUser;
                    }
                }
                if (frame == null) {
                    frame = new ConnectedUserFrame(this, newUser);
                    connectedUserFrames.add(frame);
                    createComparisonFrames();
                    if (desktopPanel != null) {
                        desktopPanel.addFrame(frame);
                        showUserFrame(frame, frame.isIncluded());
                        rearrangeWindows(false);
                    }
                } else {
                    frame.updateInfo(newUser);
                }
                usedFrames.add(frame);
            }
            
            // If a connected user in the existing list is no longer in the new list, mark it as gone and disable it from the list
            for (ConnectedUserFrame currentUser : connectedUserFrames) {
                if (!usedFrames.contains(currentUser)) {
                    currentUser.setActive(false);
                }
            }
            
            // update labels
            updateConnectedUsersDisplay();
            updateStatsDisplay();
        }

        // return status
        return (newUsersList != null);
    }

    private void updateConnectedUsersDisplay() {

        // Update title
        final int n = connectedUserFrames.size();
        statsConnectedUsers.setToolTipText(NbBundle.getMessage(getClass(), "TIP_UsersNumber", n));
        // 
        final String nb;
        final String name;
        if (n > 0) {
            nb = " " + n + " ";
            name = titleComponent + " (" + n + ")";
        } else {
            nb = " ";
            name = titleComponent;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                statsConnectedUsers.setText(nb);
                instance.setDisplayName(name);
            }
        });
        
        // Update list
        if (connectedUsersPopup != null) {
            connectedUsersPopup.updateTable(connectedUserFrames);
        }
        
    }

    public boolean startSharing() {
        
        // Get pseudo and ask user to go to parameters if not set
        if (isMyProfileOK()) {
            commPseudo = getPreferredPseudo();
        } else {
            return false;
        }

        // Ask user to load a gedcom file and make it ready
        if (isGedcomReady()) {
            commMyInfo = getMain().getSummary(commMyInfo.length);
        } else {
            return false;
        }

        // Refresh list of "connectedUsers"
        updateConnectedUsers(false);

        // Register on the ancestris server that I am a sharing friend. Remember pseudo that is used
        if (!commHandler.registerMe(commPseudo, commMyInfo)) {
            toggleOff();
            return false;
        };

        // Toggle the buttons to show it is set to sharing
        toggleOn();

        // Open the sharing locally
        updateIcon();

        playSound("resources/soundopen.wav");

        return true;
    }

    public boolean stopSharing() {

        // Stop search engine in case it is running
        stopMapsSearch();

        // Unregister from Ancestris server
        commHandler.unregisterMe(commPseudo);

        // Stop the sharing locally
        updateIcon();

        // Update membersButton list
        updateConnectedUsers(false);

        // Toggle the buttons to show it is no longer sharing
        toggleOff();

        playSound("resources/soundclose.wav");

        return true;
    }

    private void toggleOn() {
        isBusy = true;
        sharing = true;
        startSharingToggle.setToolTipText(true);
        stopSharingToggle.setToolTipText(false);
        dispatchShare(true);
        searchButton.setOn();
        isBusy = false;
    }

    private void toggleOff() {
        isBusy = true;
        sharing = false;
        startSharingToggle.setToolTipText(false);
        stopSharingToggle.setToolTipText(true);
        dispatchShare(false);
        searchButton.setOff();
        isBusy = false;
    }
    
    private void playSound(String sound) {
        try {
            InputStream is = getClass().getResourceAsStream(sound);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(is);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateIcon() {
        SwingUtilities.invokeLater(() -> {
            instance.setIcon(ImageUtilities.loadImage(isSharingOn() ? ICON_PATH_ON : ICON_PATH, true));
        });
    }

    private boolean isMyProfileOK() {

        String error = "";

        if (getPreferredPseudo().equals("")) {
            error = NbBundle.getMessage(GedcomCompareOptionsPanel.class, "ERR_NullPseudo");
        } else {
            error = GedcomCompareOptionsPanel.getProfileError();
        }

        if (!error.isEmpty()) {
            DialogManager.create("", error)
                    .setOptionType(DialogManager.OK_ONLY_OPTION)
                    .setMessageType(DialogManager.ERROR_MESSAGE).show();
            settings.displayOptionsPanel();
            return false;
        }

        return true;
    }

    public UserProfile getMyProfile() {
        return GedcomCompareOptionsPanel.getProfile();
    }

    public void resetProfile() {
        if (commHandler != null) {
            commHandler.resetProfile();
        }
        if (isSharingOn()) {
            for (LocalGedcomFrame sg : localGedcomFrames) {
                sg.updatePhoto(true);
            }
        }
    }

    private boolean isGedcomReady() {
        if (localGedcomFrames != null && !localGedcomFrames.isEmpty()) {
            for (LocalGedcomFrame sg : localGedcomFrames) {
                if (sg.isMain() && sg.isReady()) {
                    return true;
                }
            }
        }
        DialogManager.create("", NbBundle.getMessage(GedcomCompareTopComponent.class, "MSG_NoGedcomLoaded"))
                .setOptionType(DialogManager.OK_ONLY_OPTION)
                .setMessageType(DialogManager.ERROR_MESSAGE).show();
        return false;
    }

    private void dispatchShare(boolean b) {
        if (localGedcomFrames == null) {
            return;
        }
        for (LocalGedcomFrame sg : localGedcomFrames) {
            sg.setOpen(b);
        }
        for (ConnectedUserFrame user : connectedUserFrames) {
            user.setOpen(b);
        }
    }

    public boolean isSharingOn() {
        return sharing;
    }

    public void checkSharingisOff() {
        if (sharing) {
            stopSharing();
            DialogManager.create("", NbBundle.getMessage(GedcomCompareTopComponent.class, "MSG_CannotPerformWhileSharing"))
                    .setOptionType(DialogManager.OK_ONLY_OPTION)
                    .setMessageType(DialogManager.INFORMATION_MESSAGE).show();
        }
    }

    public boolean isBusy() {
        return isBusy;
    }

    //*************************************************************************************************************************

    public void launchMapsSearch() {

        // Start new thread (because cannot be launched twice)
        searchThread = new SearchUsers(this, SearchUsers.SEARCH_TYPE_MAPS, null);

        // Launch thread (cannot be launched twice)
        searchThread.start();
        
    }

    public void launchMapsSearch(String user) {

        // Start new thread (because cannot be launched twice)
        searchThread = new SearchUsers(this, SearchUsers.SEARCH_TYPE_MAPS, getUser(user));

        // Launch thread (cannot be launched twice)
        searchThread.start();

    }

    public void launchEventsSearch(String user) {

        // Start new thread (because cannot be launched twice)
        searchThread = new SearchUsers(this, SearchUsers.SEARCH_TYPE_EVENTS, getUser(user));

        // Launch thread (cannot be launched twice)
        searchThread.start();

    }

    private void stopMapsSearch() {
        if (searchThread != null) {
            searchThread.stopGracefully();
        }
    }

    public void setRotatingIcon(boolean search) {
        if (search) {
            searchButton.setSearching();
        } else {
            searchButton.setOn();
        }
    }

    public void displaySearchedUser(String memberName) {
        Font font = memberInProgress.getFont();
        memberInProgress.setFont(font.deriveFont(Font.ITALIC));
        memberInProgress.setText(memberName + (memberName.isEmpty() ? "" : "..."));
    }


    // Data providers for connections with users
    public String getPreferredPseudo() {  // for registration
        return GedcomCompareOptionsPanel.getPseudo();
    }

    public String getRegisteredPseudo() {  // once registered
        return getRegisteredPseudo(true);
    }

    public String getRegisteredPseudo(boolean escape) {
        return escape ? StringEscapeUtils.escapeHtml(commPseudo) : commPseudo;
    }

    public String getRegisteredIPAddress() {
        return getUser(commPseudo).getIPAddress();
    }

    public String getRegisteredPortAddress() {
        return getUser(commPseudo).getPortAddress();
    }

    public STMapCapsule getMapCapsule() {
        return STFactory.getSerializedSTMap(getMain().getMap());
    }

    public STMapEventsCapsule getMapEventsCapsule(ConnectedUserFrame user) {
        ComparisonFrame cf = getComparisonFrame(getMain(), user);
        Set<String> keys = cf.getIntersectionKeys();
        if (keys == null) {
            return new STMapEventsCapsule();
        }
        return STFactory.getSerializedSTMapEvents(getMain().getMap(), keys, getPrivacy());
    }

    public void updateUser(ConnectedUserFrame user, STMapCapsule mapCapsule) {
        LOG.log(Level.FINE, "Update user map - "+user.getTitle());
        user.setMap(STFactory.getUnserializedSTMap(mapCapsule));
    }

    public void updateUser(ConnectedUserFrame user, STMapEventsCapsule mapEventsCapsule) {
        LOG.log(Level.FINE, "Update user events - "+user.getTitle());
        user.updateMap(mapEventsCapsule);
    }

    public void updateUser(ConnectedUserFrame user, UserProfile userProfile) {
        LOG.log(Level.FINE, "Update user profile - "+user.getTitle());
        user.updateProfileInfo(userProfile);
    }

    public void addConnection() {
        LocalGedcomFrame mgf = getMain();
        mgf.addConnection();
    }

    public void mainHasChanged(Gedcom gedcom) {
        LOG.log(Level.FINE, "Main has changed");
        commHandler.resetMap();
        localGedcomFrames.forEach((lgf) -> {
            if (lgf.isReady()) {
                lgf.setReady(true);
            }
        });
        LocalGedcomFrame mgf = getMain();
        if (mgf != null && mgf.getGedcom() == gedcom) {
            connectedUserFrames.forEach((user) -> {
                user.reset();
            });
        }
    }

    

    public void updateStatsDisplay() {
        
        if (stats == null) {
            stats = new StatsPanel(this);
        } else {
            stats.calcValues();
        }

        statsIndis.setText("" + stats.getNbIndis());
        statsFams.setText("" + stats.getNbFams());
        statsSTs.setText("" + stats.getNbSTs());
        statsAreas.setText(stats.getMaxArea());
        statsReceivedConnections.setText("" + stats.getNbConnections());
        statsReceivedUniqueUsers.setText("" + stats.getNbUniqueUsers());
        statsUniqueOverlaps.setText("" + stats.getNbOverlaps());
        
        revalidate();
        repaint();
                
    }

    
    
    public void displayStats() {
        
        updateStatsDisplay();
        revalidate();
        repaint();

        if (commHandler != null) {
            commHandler.sendStats(stats.getValues());
        }

        DialogManager.create(NbBundle.getMessage(StatsPanel.class, "TITL_StatsPanel"), stats)
                .setMessageType(DialogManager.PLAIN_MESSAGE).setDialogId(StatsPanel.class).setOptionType(DialogManager.OK_ONLY_OPTION).show();

    }

    public Console getConsole() {
        return console;
    }
    
}
