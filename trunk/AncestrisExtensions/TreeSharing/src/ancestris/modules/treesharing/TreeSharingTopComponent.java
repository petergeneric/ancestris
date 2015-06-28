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
import ancestris.modules.treesharing.communication.FriendGedcomEntity;
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
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
 * Purpose of the whole sharing tree game : 
 * ---------------------------------------
 * Get subtrees of other Ancestris friends which share common entities (INDI, FAM) with my own shared gedcom trees
 * 
 * Overall process, which might be split into visual steps from users:
 * - Identify the list of currently sharing friends from the ancestris server (crypted communication)
 * - Launch sequentially a 1-to-1 communications with each sharing Ancestris friend (crypted communication)
 * - Ask each member ancestris running program for the list of shared [gedcom x entities(INDI, FAM)] 
 *      / limited to owner's criteria (duration, selected members, privacy) 
 *      / one gedcom at a time, crypted and zipped
 *      / until all data collected on my ancestris
 * - Once data collected within my ancestris, without me knowing, compares all entities to all of mines,
 *      / on "Lastname" + "one firstname" + "birth or death date" for individuals and families 
 * - Flags all my entities which are found as matching across all shared gedcoms, and which ancestris member/gedcom/entityID it is matched to (pseudos and the matching entities list)
 * - Continue with each member
 * - Store permanently matching elements (crypted) and skip members and entities already matched (from crypted storage) (useful for performance reason)
 * - Notifies me of the existence of matches, but without revealing any data
 * - Notifies the identified members that I have identified common data with them
 * <pause>
 * - Requests authorisation to the identified friends 
 * - Once mutual agreement confirmed, asynchronously, provides each member (me and my matching mate) the matching entities list, the total number of entities
 * <pause>
 * - Upon subsequent agreement, subtrees and related sources/media/repos/note could be shared among users (qualify size and direction (ancestors, descendants, siblings) before transmitting
 * <pause>
 * - Upon agreement, communicate members one human contact detail (eg: email or tel)
 * 
 * Principle of security :
 *      1/ No data can be obtain without sharing one's own 
 *      2/ Data can only be obtained from matching entities in my trees => users can only get as much as they share !
 *      3/ Data remains crypted across the Internet
 *      4/ No gedcom data is stored on the ancestris centralised server : server only has members "access information" and public crypting key
 *      5/ Members do see connected members' pseudos (otherwise would not know when to run their search and would not be human!)
 *      5/ Ancestris friends do not get somebody else data without prior owner's authorisation
 *      6/ Only ancestris applications know who's who and manipulate the data until explicit authorisation from owners
 *      7/ Shared gedcom files have to be opened in Ancestris
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
    private JLabel rotating = null;
    private final int LEFT_OFFSET_GEDCOM = 10;
    private final int TOP_OFFSET = 10;
    private final int VERTICAL_SPACE = 10;
    private final int LEFT_OFFSET_FRIENDS = 400;
    
    // Sharing elements
    private boolean isBusy = false;
    private String commPseudo = "";
    private Comm commHandler = null;
    private List<SharedGedcom> sharedGedcoms = null;
    private List<AncestrisMember> ancestrisMembers = null; // all connected members
    private List<AncestrisFriend> ancestrisFriends = null; // only members with entities in common
    private Timer timer;
    
    // Searching elements
    private SearchSharedTrees searchThread;
    
    
    
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
        
        // - Dropbox on all connected friends
        MembersPopup membersList = new MembersPopup(this, ancestrisMembers);
        JButton members = createDropDownButton(new ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/friend24.png")), membersList);
        members.setToolTipText(NbBundle.getMessage(MembersPopup.class, "TIP_MembersList"));
        toolbar.add(members);
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 

        // - Timer display
        timerPanel = new TimerPanel(this);
        toolbar.add(timerPanel);
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 

        // - Set Privacy ON/OFF
        privacyToggle = new PrivacyToggle(this, getPreferredPrivacy());
        toolbar.add(privacyToggle);
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
        toolbar.add(new JLabel(TOOLBAR_SPACE)); 
        rotating = new JLabel(TOOLBAR_SPACE);
        toolbar.add(rotating);

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

    private void displayAncestrisFriends() {

        // Get list
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
    

    // Connexion preferences
    
    private String getPreferredPseudo() {
        return NbPreferences.forModule(TreeSharingOptionsPanelController.class).get("Pseudo", "");
    }

    private String getPreferredAccess() {
        return NbPreferences.forModule(TreeSharingOptionsPanelController.class).get("Access", "");
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
        
        // Toggle the buttons to show it is set to sharing
        toggleOn();

        // We have list of shared gedcoms in "sharedGedcoms" with isShared selected

        // We have list of allowed members in "ancestrisMembers" with isAllowed set to true
        //        for (AncestrisMember member : ancestrisMembers) {
        //            System.out.println(member.getName() + " - " + member.isAllowed());
        //        }
        
        // Create timer task to stop sharing after delay cooresponding to indicated date
        if (!setTimer()) {
            toggleOff();
            timerPanel.setFocus();
            return false;
        }
        
        // Open my ancestris communication in/out (I accept to receive communication requests)
        commHandler.startListeningtoFriends();

        // Register on the ancestris server that I am a sharing friend. Remember pseudo that is used
        commPseudo = getPreferredPseudo();
        commHandler.registerMe(commPseudo, getPreferredAccess());

        // Open the sharing locally
        shareAll = true;
        
        // Launch search engine
        launchSearchEngine();
        
        return true;
    }

    public boolean stopSharingAll() {

        // Launch search engine
        stopSearchEngine();
        
        // Stop timer
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        
        // Unregister from Ancestris server
        commHandler.unregisterMe(commPseudo);
        
        // Stop ancestris communication in/out
        commHandler.stopListeningtoFriends();
        
        // Stop the sharing locally
        shareAll = false;
        
        // Toggle the buttons to show it is no longer sharing
        toggleOff();
        
        return true;
    }

    private void toggleOn() {
        Toolkit.getDefaultToolkit().beep();
        isBusy = true;
        startSharingToggle.setEnabled(false);
        startSharingToggle.setToolTipText(true);
        stopSharingToggle.setSelected(false);
        stopSharingToggle.setEnabled(true);
        stopSharingToggle.setToolTipText(true);
        dispatchShare(true);
        isBusy = false;
    }

    private void toggleOff() {
        Toolkit.getDefaultToolkit().beep();
        Toolkit.getDefaultToolkit().beep();
        isBusy = true;
        stopSharingToggle.setEnabled(false);
        stopSharingToggle.setToolTipText(false);
        startSharingToggle.setSelected(false);
        startSharingToggle.setEnabled(true);
        startSharingToggle.setToolTipText(true);
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

    private boolean setTimer() {
        
        // Calculte edelay between bow and limit date
        Date limitDate = timerPanel.getTimerDate();
        Date currentDate = new java.util.Date();
        long delay = limitDate.getTime() - currentDate.getTime();
        if (delay < 0) {
            return false;
        }

        // Set timer
        TimerTask task;
        if (delay != 0) {

            task = new TimerTask() {

                @Override
                public void run() {
                    timer.cancel();
                    stopSharingToggle.doClick();
                }
            };
            timer = new Timer();

            // set delay in hours
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

    private void launchSearchEngine() {
        // Init thread (because cannot be launched twice)
        searchThread = new SearchSharedTrees(this);
        
        // Launch thread (cannot be launched twice)
        searchThread.start();
    }

    private void stopSearchEngine() {
        searchThread.stopGracefully();
    }

    public void setRotatingIcon(ImageIcon icon, String toolTip) {
        rotating.setIcon(icon);
        rotating.setToolTipText(toolTip);
        rotating.revalidate();
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


    /**
     * In case I am sharing my gedcoms and get called by an allowed member, I will return my shared entities
     * 
     * @param member
     * @return 
     */
    public List<FriendGedcomEntity> provideMySharedEntitiesToMember(AncestrisMember member) {
        
        // Check if member is in allowed list
        if (!member.isAllowed()) {
                return null;
            }
        
        // Get all shared entities for all gedcoms
        List<Entity> sharedEntities = new LinkedList<Entity>();
        for (SharedGedcom sharedGedcom : sharedGedcoms) {
            sharedEntities.addAll(sharedGedcom.getAllPublicEntities());
        }
        
        // Build return list
        List<FriendGedcomEntity> providedEntities = new LinkedList<FriendGedcomEntity>();
        for (Entity entity : sharedEntities) {
            providedEntities.add(new FriendGedcomEntity(new AncestrisFriend(commPseudo, getPreferredAccess()), entity.getGedcom(), entity));
        }
        
        return providedEntities;
    }

    
    
}
