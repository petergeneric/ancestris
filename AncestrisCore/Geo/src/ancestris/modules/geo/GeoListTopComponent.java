/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.util.EventUsage;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Gedcom;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.swing.tree.TreeSelectionModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public final class GeoListTopComponent extends AncestrisTopComponent implements ExplorerManager.Provider, GeoPlacesListener, PropertyChangeListener {

    //
    // Path to the icon used by the component and its open action
    static final String ICON_PATH = "ancestris/modules/geo/list.png";
    private static final String PREFERRED_ID = "GeoListTopComponent";
    //
    // Each node is a location object. We also store the list of places
    private GeoPlacesList gpl = null;
    private int selectedNode = -1;
    //
    // Setup an explorer structure for the list of locations
    private ExplorerManager mgr = new ExplorerManager();
    //
    // Is used from the Map Top Component to set the foxus to the List TopComponent
    private boolean isInitialised = false;
    //
    // Used to sort events
    private static Map<String, EventUsage> eventUsages = null;

    //
    // Runs the Ancestris componenet defaults
    public GeoListTopComponent() {
        super();
        initEventUsages();
    }

    @Override
    public String getAncestrisDockMode() {
        return AncestrisDockModes.OUTPUT;
    }

    
    private static void initEventUsages() {
        eventUsages = new HashMap<String, EventUsage>();
        EventUsage.init(eventUsages);
    }

    @Override
    public Image getImageIcon() {
        return ImageUtilities.loadImage(ICON_PATH, true);
    }

    @Override
    public void setName() {
        setName(NbBundle.getMessage(GeoListTopComponent.class, "CTL_GeoListTopComponent"));
    }

    @Override
    public void setToolTipText() {
        setToolTipText(NbBundle.getMessage(GeoListTopComponent.class, "HINT_GeoListTopComponent", 
                (getGedcom() != null ? getGedcom().getDisplayName() : ""), 
                (gpl != null ? gpl.getNodes().length : 0)));
    }

    @Override
    public boolean createPanel() {
        // TopComponent window parameters
        initComponents();

        // Set mono selection and allow substring quick search (requires nodes to have a valid getName())
        ((BeanTreeView) jScrollPane1).setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        ((MyBeanTreeView) jScrollPane1).setQuickSearchAllowed(true);

        // Initialise tree of locations
        initTree();
        isInitialised = true;
        return true;
    }

    /**
     * InitTree.
     *
     * For each given Gedcom, a list object of locations is retrieved or built
     * when it is the first time
     *
     * If places are empty, the internet search is launched ; otherwise we
     * notify a gedcom change
     *
     */
    private void initTree() {
        // Set default place format
        setPlaceFormatStartingWithCity();
        gpl = GeoPlacesList.getInstance(getGedcom());
        // Launch search for locations
        if (gpl.getNodes() == null) {
            gpl.launchPlacesSearch(GeoNodeObject.GEO_SEARCH_LOCAL_ONLY, true, false, null, null);
        } else {
            geoPlacesChanged(gpl, GeoPlacesList.TYPEOFCHANGE_GEDCOM);
        }
        // Make sure this list component gets notified when geoplacelist object makes notifications
        gpl.addGeoPlacesListener(this);
    }

    public void geoPlacesChanged(GeoPlacesList gpl, String change) {
        if (change.equals(GeoPlacesList.TYPEOFCHANGE_COORDINATES) || (change.equals(GeoPlacesList.TYPEOFCHANGE_NAME)) || (change.equals(GeoPlacesList.TYPEOFCHANGE_GEDCOM))) {
            int currentNode = getSelectedNode();
            if (currentNode != -1 && currentNode != selectedNode) {
                selectedNode = currentNode;
            }
            mgr.setRootContext(new GeoNode(gpl));
            ((BeanTreeView) jScrollPane1).setRootVisible(false);
            jScrollPane1.repaint();
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

                public void run() {
                    jScrollPane1.updateUI();
                    selectNode(selectedNode);
                    setToolTipText();
                }
            });

        }
    }

    private int getSelectedNode() {

        // Get selected node
        Node[] geonodes = mgr.getSelectedNodes();
        if (geonodes.length == 0) {
            return -1;
        }
        GeoNodeObject selectedObj = geonodes[0].getLookup().lookup(GeoNodeObject.class);
        Node[] scannedNodes = mgr.getRootContext().getChildren().getNodes();
        for (int i = 0; i < scannedNodes.length; i++) {
            Node node = scannedNodes[i];
            GeoNodeObject obj = node.getLookup().lookup(GeoNodeObject.class);
            if (obj.equals(selectedObj)) {
                return i;
            }
        }
        return -1;
    }

    private void selectNode(int nodeIndex) {
        if (nodeIndex < 0) {
            nodeIndex = 0;
        }
        // scan nodes to find the one
        Node[] scannedNodes = mgr.getRootContext().getChildren().getNodes();
        if (nodeIndex >= scannedNodes.length) {
            nodeIndex = scannedNodes.length-1;
        }
        Node node = scannedNodes[nodeIndex];
        try {
            mgr.setSelectedNodes(new Node[]{node});
        } catch (PropertyVetoException ex) {
            // nothing
        }
    }


    /**
     * Set default display format starting with city
     */
    private void setPlaceFormatStartingWithCity() {
        String displayFormat = "";
        Gedcom gedcom = getGedcom();
        displayFormat = gedcom.getPlaceDisplayFormat();
        if (displayFormat == null) {
            gedcom.setPlaceDisplayFormat(gedcom.getPlaceDisplayFormatStartingWithCity());
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new MyBeanTreeView();

        setLayout(new java.awt.BorderLayout());
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // Set lookup to listen to selection
        mgr.addPropertyChangeListener(this);
    }

    @Override
    public void componentClosed() {
        Gedcom gedcom = getGedcom();
        if (gedcom != null) {
            GeoPlacesList gpl2 = GeoPlacesList.getInstance(getGedcom());
            gpl2.remove(gedcom);
            gpl2.removeGeoPlacesListener(this);
        }
        mgr.removePropertyChangeListener(this);
        AncestrisPlugin.unregister(this);
    }


    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    public boolean isInitialised() {
        return isInitialised;
    }

    /**
     * Case of node selection
     *
     * @param pce
     */
    public void propertyChange(PropertyChangeEvent pce) {
    }

    public void showLocation(GeoNodeObject gno) {
        if (mgr == null || gno == null) {
            return;
        }
        Node rootNode = mgr.getRootContext();
        Node[] kids = rootNode.getChildren().getNodes();
        for (int i = 0; i < kids.length; i++) {
            final Node node = kids[i];
            ((BeanTreeView) jScrollPane1).collapseNode(node);
            GeoNodeObject obj = node.getLookup().lookup(GeoNodeObject.class);
            if (obj.toDisplayString().equals(gno.toDisplayString())) { // found
                ((MyBeanTreeView) jScrollPane1).setScrollOnExpand(true);
                ((MyBeanTreeView) jScrollPane1).expandNode(node);
                try {
                    mgr.setSelectedNodes(new Node[]{kids[kids.length-1]}); // select last node
                    mgr.setSelectedNodes(new Node[]{node}); // then select found node so that expanded lines can be seen
                } catch (PropertyVetoException ex) {
                    // nothing
                }
                break;
            }
        }
    }
    

    /**
     * Subclass BeanTreeView to be able to use the setScrollOnExpand function
     * (tree is protected)
     */
    private static class MyBeanTreeView extends BeanTreeView {

        public boolean getScrollOnExpand() {
            return tree.getScrollsOnExpand();
        }

        public void setScrollOnExpand(boolean scroll) {
            this.tree.setScrollsOnExpand(scroll);
        }
    }
    
    
    /**
     * Comparator to sort events
     */
    public static Comparator<GeoNodeObject> sortEvents = new Comparator<GeoNodeObject>() {

        public int compare(GeoNodeObject o1, GeoNodeObject o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return +1;
            }
            if (o2 == null) {
                return -1;
            }
            if (eventUsages == null) {
                initEventUsages();
            }
            EventUsage eu1 = eventUsages.get(o1.getEventTag());
            EventUsage eu2 = eventUsages.get(o2.getEventTag());
            if (eu1 == null) {
                return +1;
            }
            if (eu2 == null) {
                return -1;
            }
            String s1 = eu1.getOrder() + o1.toDisplayString();
            String s2 = eu2.getOrder() + o2.toDisplayString();
            return s1.compareTo(s2);
        }
    };

    
}
