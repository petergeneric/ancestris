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
import ancestris.modules.treesharing.communication.Comm;
import ancestris.modules.treesharing.communication.MemberProfile;
import ancestris.modules.treesharing.options.TreeSharingOptionsPanel;
import ancestris.modules.treesharing.options.TreeSharingOptionsPanelController;
import ancestris.modules.treesharing.panels.AncestrisFriend;
import ancestris.modules.treesharing.panels.DisplayStatsAction;
import ancestris.modules.treesharing.panels.EntitiesListPanel;
import ancestris.modules.treesharing.panels.FriendGedcomEntity;
import ancestris.modules.treesharing.panels.GedcomFriendMatch;
import ancestris.modules.treesharing.panels.MatchData;
import ancestris.modules.treesharing.panels.MembersPopup;
import ancestris.modules.treesharing.panels.PrivacyToggle;
import ancestris.modules.treesharing.panels.RearrangeAction;
import ancestris.modules.treesharing.panels.ResetResults;
import ancestris.modules.treesharing.panels.SearchAction;
import ancestris.modules.treesharing.panels.SettingsAction;
import ancestris.modules.treesharing.panels.SharedGedcom;
import ancestris.modules.treesharing.panels.StartSharingAllToggle;
import ancestris.modules.treesharing.panels.StatsData;
import ancestris.modules.treesharing.panels.StatsPanel;
import ancestris.modules.treesharing.panels.StopSharingAllToggle;
import ancestris.modules.treesharing.panels.TimerPanel;
import ancestris.modules.treesharing.panels.TreeSharingPanel;
import ancestris.swing.ToolBar;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * Purpose of the whole sharing tree game : 
 * ---------------------------------------
 * Get subtrees of other Ancestris friends which share common entities (INDI, FAM) with my own shared gedcom trees
 * 
 * Overall process, which might be split into visual steps from users:
 * X Identify the list of currently sharing friends from the ancestris server (crypted communication)
 * X Launch sequentially a 1-to-1 communications with each sharing Ancestris friend (crypted communication)
 * X Ask each member ancestris running program for the list of shared [gedcom x entities(INDI, FAM)] 
 *      / limited to owner's criteria (duration, selected members, privacy) 
 *      / one gedcom at a time, crypted and zipped
 *      / until all data collected on my ancestris
 * X Once data collected within my ancestris, without me knowing, compares all entities to all of mines,
 *      / on "Lastname" + "one firstname" + "birth or death date" for individuals and families 
 * X Flags all my entities which are found as matching across all shared gedcoms, and which ancestris member/gedcom/entityID it is matched to (pseudos and the matching entities list)
 * X Continue with each member
 * X Notifies me of the existence of matches, but without revealing any data
 * X Notifies the identified members that I have identified common data with them
 * <pause>
 * - Store permanently matching elements (crypted) and skip members and entities already matched (from crypted storage) (useful for performance reason)
 * - Requests authorisation to the identified friends 
 * - Once mutual agreement confirmed, asynchronously, provides each member (me and my matching mate) the matching entities list, the total number of entities
 * <pause>
 * - Upon subsequent agreement, subtrees and related sources/media/repos/note could be shared among users (qualify size and direction (ancestors, descendants, siblings) before transmitting
 * <pause>
 * - Upon agreement, communicate members one human contact detail (eg: email or tel)
 * 
 * Principle of security :
 *      1/ Shared gedcom files have to be opened in Ancestris
 *      2/ No data can be obtain without sharing one's own 
 *      3/ Data can only be obtained from matching entities in my trees => users can only get as much as they share !
 *      4/ Data remains crypted across the Internet
 *      5/ No gedcom data is stored on the ancestris centralised server : server only has members "access information" and public crypting key
 *      6/ Members do see connected members' pseudos (otherwise would not know when to run their search and would not be human!)
 *      7/ Ancestris friends do not get somebody else data without prior owner's authorisation
 *      8/ Only ancestris applications know who's who and manipulate the data until explicit authorisation from owners
 *             
 * Principle of usage :
 *      1/ Make interaction as simple as possible : do not explicit all steps and unecessary ones, make it as automated as possible
 *      2/ At every new entering member or tree shared, previous requests run again on added data (do no implement this yet)
 * 
 * 
 * Note : depending on number of shared gedcoms across the Ancestris community, performance might justify to optimise/change the concept and the architecture
 *
 *
 * @author frederic
 */
@ServiceProvider(service = TopComponent.class)
public class TreeSharingTopComponent extends TopComponent {

    // Top component elements
    private static TreeSharingTopComponent instance;
    private static final String PREFERRED_ID = "TreeSharingTopComponent";  // NOI18N
    private static final String ICON_PATH = "ancestris/modules/treesharing/resources/treesharing.png";
    private static final String ICON_PATH_ON = "ancestris/modules/treesharing/resources/treesharingON.png";
    
    // Logger
    private static final Logger LOG = Logger.getLogger("ancestris.treesharing");
    
    // Panel elements
    private String titleComponent = "";
    private boolean isComponentCreated = false;
    private JToolBar toolbar = null;
    private TreeSharingPanel desktopPanel = null;
    private final String TOOLBAR_SPACE = "  ";
    private final BorderLayout borderLayout;
    private String defaultBorderLayout = BorderLayout.NORTH;
    private PrivacyToggle privacyToggle;
    private JLabel membersNumber = null;
    private MembersPopup membersList = null;
    private TimerPanel timerPanel;
    private boolean shareAll = false;
    private StartSharingAllToggle startSharingToggle;
    private StopSharingAllToggle stopSharingToggle;
    private SearchAction searchButton = null;
    private JLabel memberInProgress = null;
    private DisplayStatsAction statsButton = null;
    private SettingsAction settings = null;
    private final int LEFT_OFFSET_GEDCOM = 10;
    private final int LEFT_OFFSET_MATCHES = 400;
    private final int LEFT_OFFSET_FRIENDS = 590;
    private final int TOP_OFFSET = 10;
    private final int VERTICAL_SPACE = 10;
    
    // Sharing elements
    private boolean isBusy = false;
    private String commPseudo = "";
    private Comm commHandler = null;
    private java.util.Timer timer;
    private List<AncestrisMember> ancestrisMembers = null;          // list of all connected members
    private List<SharedGedcom> sharedGedcoms = null;                // iFrames : all open gedcoms
    private List<AncestrisFriend> ancestrisFriends = null;          // iFrames : only members with entities in common
    private List<GedcomFriendMatch> gedcomFriendMatches = null;     // iFrames : matches between gedcoms and friends

    // Searching elements
    private SearchSharedTrees searchThread;
    private final int PING_DELAY = 150; // seconds to check connected members
    
    // Swing timer to refresh toolbar members and stats
    private javax.swing.Timer swingTimer;
    private final int REFRESH_DELAY = 50; // seconds to refresh toolbar

    // Stats elements
    private JLabel rcvdConnections = null;
    private JLabel rcvdUniqueMembers = null;
    private JLabel rcvdUniqueFriends = null;
    private Map<String, StatsData> connectionStats = null;
    private boolean resetStats = false;
    
    // Results
    private Set<MatchData> matchedResults = null; 

    
    
    
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
    public TreeSharingTopComponent() {
        if (instance == null) {
            instance = this;
        }
        titleComponent = NbBundle.getMessage(TreeSharingTopComponent.class, "CTL_TreeSharingTopComponent");
        setName(titleComponent);
        setToolTipText(NbBundle.getMessage(TreeSharingTopComponent.class, "HINT_TreeSharingTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        borderLayout = new BorderLayout();  // has to be border
        this.setLayout(borderLayout);
    }

    
    @Override
    public void componentOpened() {
        if (!isComponentCreated) {

            initCommunication();

            // retrieve last position of toolbar when modified
            defaultBorderLayout = NbPreferences.forModule(TreeSharingTopComponent.class).get("ToolbarBorderLayout", BorderLayout.NORTH);
            
            initMainPanel();

            initSharedGedcoms();
            
            initConnectionStats();
            
            initResults();

            initSwingTimerRefreshValues();
    }
        
        privacyToggle.setPrivacy(getPreferredPrivacy());
        isComponentCreated = true;
    }

    private void initCommunication() {
        LOG.log(Level.FINE, "Creating communication handler.");

        commHandler = new Comm(this, PING_DELAY);
    }


    private void initMainPanel() {
        LOG.log(Level.FINE, "Initializing main panel.");

        // Create toolbar
        toolbar = new ToolBar();
        
        // Sharing space
        // - Sharing space : Dropbox on all connected friends
        LOG.log(Level.FINE, "   - Users button.");
        membersNumber = new JLabel("");
        LOG.log(Level.FINE, "   - Initatilize Ancestris members list.");
        updateMembersList(); 
        membersList = new MembersPopup(this);
        JButton members = createDropDownButton(new ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/friend24.png")), membersList);
        members.setToolTipText(NbBundle.getMessage(MembersPopup.class, "TIP_MembersList"));
        members.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (commHandler != null) {
                    commHandler.clearCommunicationError();
                }
                boolean ret = updateMembersList();
                if (ret) {
                    updateStatsDisplay();
                    if (commHandler != null) {
                        commHandler.sendPing();
                    }
                }
            }
        });
        toolbar.add(members);
        toolbar.add(membersNumber);
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 

        LOG.log(Level.FINE, "   - Other button.");

        // - Sharing space : Timer display
        timerPanel = new TimerPanel(this);
        toolbar.add(timerPanel);
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 

        // - Sharing space : Set Privacy ON/OFF
        privacyToggle = new PrivacyToggle(this, getPreferredPrivacy());
        toolbar.add(privacyToggle);
        toolbar.add(new JLabel(TOOLBAR_SPACE));
        
        // - Sharing space : General share button : ON/OFF
        toolbar.addSeparator();
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 
        startSharingToggle = new StartSharingAllToggle(this, shareAll);
        toolbar.add(startSharingToggle);
        stopSharingToggle = new StopSharingAllToggle(this, !shareAll);
        toolbar.add(stopSharingToggle);
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 
        
        // Search space
        // - Search space : search button
        toolbar.addSeparator();
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 
        searchButton = new SearchAction(this);
        searchButton.setOff();
        toolbar.add(searchButton);
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 

        // - Search space : member in progress
        memberInProgress = new JLabel("");
        toolbar.add(memberInProgress);
        memberInProgress.setToolTipText(NbBundle.getMessage(MembersPopup.class, "TIP_memberInProgress"));
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 

        // - Search space : reset search results
        toolbar.add(new ResetResults(this));
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 

        // Connection space
        // - Connection space : labels
        toolbar.addSeparator();
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 
        statsButton = new DisplayStatsAction(this);
        toolbar.add(statsButton);
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 
        //...
        rcvdConnections = new JLabel("");
        toolbar.add(rcvdConnections);
        rcvdConnections.setToolTipText(NbBundle.getMessage(MembersPopup.class, "TIP_rcvdConnections"));
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 
        //...
        rcvdUniqueMembers = new JLabel("");
        toolbar.add(rcvdUniqueMembers);
        rcvdUniqueMembers.setToolTipText(NbBundle.getMessage(MembersPopup.class, "TIP_rcvdUniqueMembers"));
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 
        //...
        rcvdUniqueFriends = new JLabel("");
        toolbar.add(rcvdUniqueFriends);
        rcvdUniqueFriends.setToolTipText(NbBundle.getMessage(MembersPopup.class, "TIP_rcvdUniqueFriends"));
        updateStatsDisplay();


        // - Glue
        toolbar.add(new Box.Filler(null, null, null), "growx, pushx, center");

        // - Settings space : Rearrange gedcoms and Friends on desktop
        toolbar.addSeparator();
        toolbar.add(new RearrangeAction(this));
        // - Settings space : Settings
        settings = new SettingsAction();
        toolbar.add(settings);

        
        
        // Add toolbar
        add(toolbar, defaultBorderLayout);
        
        // Main panel
        desktopPanel = new TreeSharingPanel();
        add(desktopPanel);
    }

    
    private void initSharedGedcoms() {
        LOG.log(Level.FINE, "Initializing gedcoms panels.");

        
        // Init list
        sharedGedcoms = new LinkedList<SharedGedcom>();
        
        // Get open gedcoms and build shared objects
        for (Context context : GedcomDirectory.getDefault().getContexts()) {
            sharedGedcoms.add(new SharedGedcom(this, context.getGedcom(), privacyToggle.isSelected()));
        }
        
        // Display shared Gedcoms for the first time on the desktop
        desktopPanel.setFrames(sharedGedcoms, LEFT_OFFSET_GEDCOM, TOP_OFFSET, VERTICAL_SPACE, true);
        
    }
    


    public void initConnectionStats() {
        LOG.log(Level.FINE, "Initializing connection statistics.");

        if (connectionStats == null) {
            connectionStats = new HashMap<String, StatsData>();
        } else {
            connectionStats.clear();
        }
    }
    
    
    public void initResults() {
        LOG.log(Level.FINE, "Initializing matched results.");

        if (matchedResults == null) {
            matchedResults = new HashSet<MatchData>();    
        } else {
            matchedResults.clear();
        }
    }

    
    
    private void initSwingTimerRefreshValues() {
        LOG.log(Level.FINE, "Creating refreshing toolbar swing timer.");
        
        swingTimer = new javax.swing.Timer(REFRESH_DELAY*1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean ret = updateMembersList();
                if (!ret && isShareAllOn()) {
                    stopSharingAll();
                }
                if (ret) {
                    checkDisconnection();
                    updateStatsDisplay();
                }
            }
        });
        swingTimer.setInitialDelay(REFRESH_DELAY*1000);
        swingTimer.start(); 
    }


    
    public boolean updateMembersList() {
        boolean ret = resetAncestrisMembers();

        final int n = ancestrisMembers.size() - (shareAll ? 1 : 0);
        membersNumber.setToolTipText(NbBundle.getMessage(MembersPopup.class, "TIP_MembersNumber", n));
        // 
        if (membersList != null) {
            final String nb;
            final String name;
            membersList.updateTable();
            if (n > 0) {
                nb = " " + n + " ";
                name = titleComponent + " (" + n + ")";
            } else {
                nb = " ";
                name = titleComponent;
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    membersNumber.setText(nb);
                    instance.setDisplayName(name);
                }
            });
            rememberMembers();
        }
        return ret;
    }
    
    public void updateStatsDisplay() {
        if (connectionStats == null || connectionStats.isEmpty()) {
            statsButton.setEnabled(false);
            rcvdConnections.setText("");
            rcvdUniqueMembers.setText("");
            rcvdUniqueFriends.setText("");
        } else {
            statsButton.setEnabled(true);
            int rcvdConnectionsNb = 0;
            int rcvdUniqueMembersNb = 0;
            int rcvdUniqueFriendsNb = 0;
            for (String member : connectionStats.keySet()) {
                StatsData stats = connectionStats.get(member);
                rcvdConnectionsNb += stats.connections;
                rcvdUniqueMembersNb++;
                if (stats.match) {
                    rcvdUniqueFriendsNb++;
                }
            }
            rcvdConnections.setText("" + rcvdConnectionsNb);
            rcvdUniqueMembers.setText("" + rcvdUniqueMembersNb);
            rcvdUniqueFriends.setText("" + rcvdUniqueFriendsNb);
        }
        revalidate();
        repaint();
    }


    
    

    public void rememberMembers() {
        String key = "";
        for (AncestrisMember member : ancestrisMembers) {
            key = "memberip-" + member.getxIPAddress();
            NbPreferences.forModule(TreeSharingOptionsPanel.class).put(key, member.isAllowed() ? "1" : "0");
            key = "memberps-" + member.getMemberName();
            NbPreferences.forModule(TreeSharingOptionsPanel.class).put(key, member.isAllowed() ? "1" : "0");
        }
    }
    
        
    private boolean resetAncestrisMembers() {
        // Clear existing list 
        if (ancestrisMembers == null) {
            ancestrisMembers = new ArrayList<AncestrisMember>(); 
        } else {
            ancestrisMembers.clear();
        }

        // Get new list from server
        List<AncestrisMember> newList = commHandler.getAncestrisMembers();
        String key = "";
        boolean isAllowed = true;

        // If a list is found on the server, 
        if (newList != null) {
            for (AncestrisMember tempItem : newList) {
                if (ancestrisMembers != null && !ancestrisMembers.isEmpty()) {
                    for (AncestrisMember member : ancestrisMembers) {
                        if (tempItem.getMemberName().equals(member.getMemberName())) {
                            tempItem.setAllowed(member.isAllowed());
                            tempItem.setUsePrivate(member.getUsePrivate());
                            continue;
                        }
                    }
                }
                key = "memberip-" + tempItem.getxIPAddress();
                isAllowed = NbPreferences.forModule(TreeSharingOptionsPanel.class).get(key, "1").equals("1");
                key = "memberps-" + tempItem.getMemberName();
                isAllowed &= NbPreferences.forModule(TreeSharingOptionsPanel.class).get(key, "1").equals("1");
                tempItem.setAllowed(isAllowed);
            }

            // Add newList to previous list
            ancestrisMembers.addAll(newList);
        }
        
        return (newList != null);
    }

    
    private void checkDisconnection() {
        // If sharing is ON, and if I am no longer in the membersList (unknown disconnexion ?), and until the time left to share, restart sharing
        boolean stillConnected = false;
        for (AncestrisMember member : ancestrisMembers) {
            if (member.getMemberName().equals(commPseudo)) {
                stillConnected = true;
            }
        }
        if (!stillConnected && isShareAllOn()) {
            stopSharingAll();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                //Exceptions.printStackTrace(ex);
            }
            startSharingAll();
            LOG.log(Level.FINE, "Connection lost. Turning sharing off and back on...   " + getRegisteredEndDate());
        }

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
        swingTimer.stop();
        updateIcon();
        rememberMembers();
    }
    
    public void resetResults() {
        matchedResults.clear();
        for (SharedGedcom sg : sharedGedcoms) {
            removeMatch(sg);
            sg.resetResults();
        }
    }


    public void rearrangeWindows() {
        if (sharedGedcoms != null && !sharedGedcoms.isEmpty()) {
            desktopPanel.setFrames(sharedGedcoms, LEFT_OFFSET_GEDCOM, TOP_OFFSET, VERTICAL_SPACE, false);
        }
        if (ancestrisFriends != null && !ancestrisFriends.isEmpty()) {
            desktopPanel.setFrames(ancestrisFriends, LEFT_OFFSET_FRIENDS, TOP_OFFSET, VERTICAL_SPACE, false);
        }
        if (gedcomFriendMatches != null && !gedcomFriendMatches.isEmpty()) {
            desktopPanel.setFrames(gedcomFriendMatches, LEFT_OFFSET_MATCHES, TOP_OFFSET, VERTICAL_SPACE, false);
        }
    }
    

    // Connexion preferences
    public String getPreferredPseudo() {
        return TreeSharingOptionsPanel.getPseudo();
    }

    public String getRegisteredPseudo() {
        return getRegisteredPseudo(true);
    }
    
    public String getRegisteredPseudo(boolean escape) {
        return escape ? StringEscapeUtils.escapeHtml(commPseudo) : commPseudo;
    }
    
    public String getRegisteredIPAddress() {
        return getMember(commPseudo).getIPAddress();
    }

    public String getRegisteredPortAddress() {
        return getMember(commPseudo).getPortAddress();
    }

    public String getRegisteredEndDate() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(timerPanel.getTimerDate());
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
    
    public boolean startSharingAll() {
        
        // Get pseudo and ask user to go to parameters if not set
        if (isMyProfileOK()) {
            commPseudo = getPreferredPseudo();
        } else {
            return false;
        }
        
        // Ask user to load a gedcom file if not up
        if (!isGedcomLoaded()) {
            return false;
        }
        

        // Toggle the buttons to show it is set to sharing
        toggleOn();

        // We have list of shared gedcoms in "sharedGedcoms" with isShared selected
        // OK

        // Create timer task to stop sharing after delay corresponding to indicated date
        if (!setTimer()) {
            toggleOff();
            timerPanel.setFocus();
            return false;
        }
        
        // Register on the ancestris server that I am a sharing friend. Remember pseudo that is used
        if (!commHandler.registerMe(commPseudo)) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            toggleOff();
            return false;
        };

        // Open the sharing locally
        shareAll = true;
        updateIcon();
        
        // We have list of allowed members in "ancestrisMembers" with isAllowed set to true
        updateMembersList();
        
        return true;
    }

    public boolean stopSharingAll() {

        // Launch search engine in case it is running
        stopSearchEngine();
        
        // Stop timer
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        
        // Unregister from Ancestris server
        commHandler.unregisterMe(commPseudo);
        
        // Stop the sharing locally
        shareAll = false;
        updateIcon();
        
        // Update members list
        updateMembersList();
        
        // Toggle the buttons to show it is no longer sharing
        toggleOff();
        
        return true;
    }

    private void toggleOn() {
        Toolkit.getDefaultToolkit().beep();
        isBusy = true;
        startSharingToggle.setToolTipText(true);
        stopSharingToggle.setToolTipText(false);
        dispatchShare(true);
        searchButton.setOn();
        isBusy = false;
    }

    private void toggleOff() {
        Toolkit.getDefaultToolkit().beep();
        Toolkit.getDefaultToolkit().beep();
        isBusy = true;
        startSharingToggle.setToolTipText(false);
        stopSharingToggle.setToolTipText(true);
        dispatchShare(false);
        searchButton.setOff();
        isBusy = false;
    }

    
    public void updateIcon() {
        boolean atLeastAFlagIsOn = false;
        for (SharedGedcom sg : sharedGedcoms) {
            atLeastAFlagIsOn |= sg.isShared();
        }
        final boolean flag = atLeastAFlagIsOn;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                instance.setIcon(ImageUtilities.loadImage(isShareAllOn() && flag ? ICON_PATH_ON : ICON_PATH, true));
            }
        });
    }
    
    
    private boolean isMyProfileOK() {

        showWelcomeMessages();
        
        String error = "";

        if (getPreferredPseudo().equals("")) {
            error = NbBundle.getMessage(TreeSharingOptionsPanel.class, "ERR_NullPseudo");
        } else {
            error = TreeSharingOptionsPanel.getProfileError();
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

    public MemberProfile getMyProfile() {
        return TreeSharingOptionsPanel.getProfile();
    }

    private boolean isGedcomLoaded() {
        if (sharedGedcoms != null && !sharedGedcoms.isEmpty()) {
            return true;
        }
        DialogManager.create("", NbBundle.getMessage(TreeSharingTopComponent.class, "MSG_NoGedcomLoaded"))
                .setOptionType(DialogManager.OK_ONLY_OPTION)
                .setMessageType(DialogManager.ERROR_MESSAGE).show();
        return false;
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
        LOG.log(Level.FINE, "Gedcom opened... (" + gedcom.getName() + ")");
        if (!isComponentCreated) {
            LOG.log(Level.FINE, "   - Do nothing. Component not created yet.");
            return;
        }
        LOG.log(Level.FINE, "   - Creating gedcom panel.");
        SharedGedcom newSharedGedcom = new SharedGedcom(this, gedcom, privacyToggle.isSelected());
        desktopPanel.addFrame(newSharedGedcom, findLocation(sharedGedcoms.size(), LEFT_OFFSET_GEDCOM, newSharedGedcom.getPreferredSize().height));
        sharedGedcoms.add(newSharedGedcom);
        updateIcon();
    }

    
    public void gedcomClosed(Gedcom gedcom) {
        LOG.log(Level.FINE, "Gedcom closed... (" + gedcom.getName() + ")");
        if (!isComponentCreated) {
            LOG.log(Level.FINE, "   - Do nothing. Component not created yet.");
            return;
        }
        LOG.log(Level.FINE, "   - Removing gedcom panel.");
        for (SharedGedcom sg : sharedGedcoms) {
            if (sg.getGedcom() == gedcom) {
                // Remove gedcom from desktop and list
                removeGedcom(sg);
                // Remove matches related to gedcom from desktop and list
                removeMatch(sg);
                break;
            }
        }
        updateIcon();
    }

    
    private void removeGedcom(SharedGedcom sg) {
        sg.close();
        desktopPanel.removeFrame(sg);
        desktopPanel.removeLink(sg);
        sharedGedcoms.remove(sg);
    }

    private void removeMatch(SharedGedcom sg) {
        
        if (gedcomFriendMatches == null || ancestrisFriends == null) {
            return;
        }

        List<GedcomFriendMatch> removedMatches = new LinkedList<GedcomFriendMatch>();
        
        for (GedcomFriendMatch match : gedcomFriendMatches) {
            if (match.getSharedGedcom() == sg) {
                removedMatches.add(match);
            }
        }

        for (GedcomFriendMatch match : removedMatches) {
            AncestrisFriend friend = match.getFriend();
            desktopPanel.removeFrame(match);
            desktopPanel.removeLink(match);
            gedcomFriendMatches.remove(match);
            friend.removeGedcom(sg);
            if (friend.isEmpty()) {
                desktopPanel.removeFrame(friend);
                desktopPanel.removeLink(friend);
                ancestrisFriends.remove(friend);
            }
        }
    
    }

    
    
    
    private Point findLocation(int size, int offset, int height) {
        return new Point(offset, TOP_OFFSET + size * (height + VERTICAL_SPACE));
    }

    private boolean setTimer() {
        
        // Calculte delay between now and limit date
        Date limitDate = timerPanel.getTimerDate();
        Date currentDate = new java.util.Date();
        long delay = limitDate.getTime() - currentDate.getTime();
        if (delay < 0) {
            delay = TimerPanel.DEFAULT_DELAY; 
            timerPanel.setTimerDate((int) delay);
            delay *= 3600000; // convert hours delay into milliseconds
            //return false; 
        }

        // Set timer
        TimerTask task;
        if (delay != 0) {

            task = new TimerTask() {

                @Override
                public void run() {
                    timer.cancel();
                    stopSharingToggle.doClick();
                    updateIcon();
                }
            };
            timer = new Timer();

            // set delay in milliseconds
            timer.schedule(task, delay);  
        }
        return true;
    }

    public void resetTimer() {
        if (timer == null) {
            return;
        }
        timer.cancel();
        setTimer();
    }

    public void launchSearchEngine() {
        
        // Init thread (because cannot be launched twice)
        searchThread = new SearchSharedTrees(this);
        
        // Launch thread (cannot be launched twice)
        searchThread.start();
        
    }

    private void stopSearchEngine() {
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


    public void displaySearchedMember(String memberName) {
        Font font = memberInProgress.getFont();
        memberInProgress.setFont(font.deriveFont(Font.ITALIC));
        memberInProgress.setText(memberName + (memberName.isEmpty() ? "" : "..."));
    }

    
    public void updateSearchStats() {
        if (sharedGedcoms != null) {
            for (SharedGedcom sg : sharedGedcoms) {
                sg.updateStats(true);
            }
        }
        if(gedcomFriendMatches != null) {
            for (GedcomFriendMatch match : gedcomFriendMatches) {
                match.updateStats();
            }
        }
        if (ancestrisFriends != null) {
            for (AncestrisFriend f : ancestrisFriends) {
                f.updateStats();
            }
        }
    }

    
    public void setResetStats() {
        resetStats = true;
    }

    
    public void displayStats() {
        revalidate();
        repaint();

        DialogManager.create(NbBundle.getMessage(StatsPanel.class, "TITL_StatsPanel"), 
                new StatsPanel(connectionStats, this)).setMessageType(DialogManager.PLAIN_MESSAGE).setDialogId(StatsPanel.class).setOptionType(DialogManager.OK_ONLY_OPTION).show();

        if (resetStats) {
            initConnectionStats();
            updateStatsDisplay();
            resetStats = false;
        }
    }

    
    public void addConnection(String member) {
        if (connectionStats == null) {
            initConnectionStats();
        }
        
        StatsData stats = connectionStats.get(member);
        if (stats == null) {
            stats = new StatsData();
        }
        
        stats.connections++;
        stats.endDate = new Date();
        connectionStats.put(member, stats);

        updateStatsDisplay();
    }
    
    public void addUniqueFriend(String member, MemberProfile profile, String ipaddress, boolean resultFound) {
        if (connectionStats == null) {
            initConnectionStats();
        }
        
        StatsData stats = connectionStats.get(member);
        if (stats == null) {  // should never happen but we never know !
            stats = new StatsData();
            stats.connections++;
            stats.endDate = new Date();
        }
        
        if (resultFound) {
            stats.match = true;
        }
        if (stats.profile == null || (stats.profile.photoBytes == null && profile.photoBytes != null)) {
            stats.profile = profile;
        }
        stats.profile.ipaddress = ipaddress;
        connectionStats.put(member, stats);

        updateStatsDisplay();
    }
    
    
    
    
    
    
    
    public Comm getCommHandler() {
        return commHandler;
    }

    public List<SharedGedcom> getSharedGedcoms() {
        return sharedGedcoms;
    }

    public List<AncestrisMember> getAncestrisMembers() {
        return ancestrisMembers;
    }

    public AncestrisMember getMember(String memberToFind) {
        for (AncestrisMember member : ancestrisMembers) {
            if (member.getMemberName().equals(memberToFind)) {
                return member;
            }
        }
        return null;
    }

    

    public AncestrisFriend createMatch(SharedGedcom sharedGedcom, Entity myEntity, FriendGedcomEntity memberEntity, AncestrisMember member, int matchResult) {

        // Update matched results
        matchedResults.add(new MatchData(myEntity, memberEntity, matchResult));
        
        // Update or Create AncestrisFriend
        AncestrisFriend friend = getFriend(memberEntity.friend);
        memberEntity.setFriend(friend);
        
        // Update or Create MatchFrame
        GedcomFriendMatch match = getGedcomFriendMatch(sharedGedcom, friend);
        
        // Propagate updates
        sharedGedcom.addEntity(myEntity, memberEntity, matchResult);
        match.addEntity(myEntity, memberEntity, matchResult);
        friend.addEntity(myEntity, memberEntity, matchResult);

        // Create links
        desktopPanel.linkFrames(sharedGedcom, match);
        desktopPanel.linkFrames(match, friend);
        
        return friend;
    }

    
    private GedcomFriendMatch getGedcomFriendMatch(SharedGedcom sharedGedcom, AncestrisFriend friend) {
        
        GedcomFriendMatch match = null;
        
        // If list of matches null, create it
        if (gedcomFriendMatches == null) {
            gedcomFriendMatches = new LinkedList<GedcomFriendMatch>();
        }

        // If list of matches not empty, try to find match
        if (!gedcomFriendMatches.isEmpty()) {
            for (GedcomFriendMatch gfm : gedcomFriendMatches) {
                if (gfm.getSharedGedcom().getGedcom().getOrigin().getFile().getAbsolutePath().equals(sharedGedcom.getGedcom().getOrigin().getFile().getAbsolutePath())
                 && gfm.getFriend().getFriendName().equals(friend.getFriendName())) {
                    match = gfm;
                    break;
                }
            }
        }
        
        // If match still null, then create it
        if (match == null) {
            match = new GedcomFriendMatch(this, sharedGedcom, friend);
            desktopPanel.addFrame(match, findLocation(gedcomFriendMatches.size(), LEFT_OFFSET_MATCHES, match.getPreferredSize().height));
            gedcomFriendMatches.add(match);
        }
        
        return match;
    }

    private AncestrisFriend getFriend(String foundFriend) {

        AncestrisFriend friend = null;
        
        // If list of matches null, create it
        if (ancestrisFriends == null) {
            ancestrisFriends = new LinkedList<AncestrisFriend>();
        }

        // If list of matches not empty, try to find match
        if (!ancestrisFriends.isEmpty()) {
            for (AncestrisFriend f : ancestrisFriends) {
                if (f.getFriendName().equals(foundFriend)) {
                    friend = f;
                    break;
                }
            }
        }
        
        // If match still null, then create it
        if (friend == null) {
            friend = new AncestrisFriend(this, foundFriend);
            desktopPanel.addFrame(friend, findLocation(ancestrisFriends.size(), LEFT_OFFSET_FRIENDS, friend.getPreferredSize().height));
            ancestrisFriends.add(friend);
        }
        
        return friend;
    }

    private void showWelcomeMessages() {
        if ("1".equals(NbPreferences.forModule(TreeSharingOptionsPanel.class).get("Welcome", "1"))) {
            DialogManager.create(NbBundle.getMessage(getClass(), "TITL_Welcome"),
                    new WelcomePanel()).setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(DialogManager.OK_ONLY_OPTION).show();
            NbPreferences.forModule(TreeSharingOptionsPanel.class).put("Welcome", "0");
            settings.displayOptionsPanel();
        }
    }

    

    public void displayResultsPanel(String gedcom, String friend, String typeOfEntity) {
        DialogManager.create(NbBundle.getMessage(EntitiesListPanel.class, "TIP_TitleResults"), new EntitiesListPanel(gedcom, friend, matchedResults, typeOfEntity))
                .setMessageType(DialogManager.PLAIN_MESSAGE).setDialogId(EntitiesListPanel.class).setOptionType(DialogManager.OK_ONLY_OPTION).show();
    }


}
