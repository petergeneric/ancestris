/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.geo;

import genjfr.app.EditTopComponent;
import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
class GeoNode extends AbstractNode {

    public GeoNode(GeoPlacesList gpl) {
        super(new GeoChildrenNodes(gpl));
        setDisplayName(NbBundle.getMessage(GeoListTopComponent.class, "GeoListRoot") + " " + gpl.getGedcom().getName());
    }

    public GeoNode(GeoNodeObject obj) {
        super(obj != null && !obj.isEvent ? new GeoChildrenNodes(obj) : Children.LEAF, Lookups.singleton(obj));
    }

    @Override
    public String getHtmlDisplayName() {
        GeoNodeObject obj = getLookup().lookup(GeoNodeObject.class);
        if (obj != null) {
            return obj.isEvent ? "<font color='!textText'>" + obj.toString() + " </font>&nbsp;"
                    : "<font color='!textText'>" + obj.toString() + "</font><font color='!controlShadow'> [" + obj.getCoordinates() + "]" + " <i>(" + obj.getNbOfEvents() + ")" + "</i></font>&nbsp;";
        } else {
            return null;
        }

    }

    @Override
    @SuppressWarnings("deprecation")
    public Image getIcon(int type) {
        GeoNodeObject obj = getLookup().lookup(GeoNodeObject.class);
        return (obj != null) ? obj.getIcon() : Utilities.loadImage("genjfr/app/geo/geo.png");
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }

    /**
     * Double-click
     * @return
     */
    @Override
    public Action getPreferredAction() {
        if (isLeaf()) {
            return new GeoAction("ACTION_EditEvent");
        } else {
            return new GeoAction("ACTION_CenterPlace");
        }
    }

    /**
     * Right-click
     * @param popup
     * @return
     */
    @Override
    public Action[] getActions(boolean popup) {
        if (isLeaf()) {
            return new Action[]{
                        new GeoAction("ACTION_EditEvent")};
        } else {
            return new Action[]{
                        new GeoAction("ACTION_CenterPlace"),
                        new GeoAction("ACTION_ShowPlace"),
                        null,
                        new GeoAction("ACTION_FindPlace"),
                        null,
                        new GeoAction("ACTION_EditPlace")};
        }
    }

    private class GeoAction extends AbstractAction {

        private String actionName = "";

        public GeoAction(String name) {
            this.actionName = name;
            putValue(NAME, NbBundle.getMessage(GeoNode.class, name));
        }

        @SuppressWarnings("deprecation")
        public void actionPerformed(ActionEvent e) {
            GeoNodeObject obj = getLookup().lookup(GeoNodeObject.class);
            if (obj == null) {
                return;
            }
            if (actionName.equals("ACTION_CenterPlace")) {
                GeoMapTopComponent theMap = getMapTopComponent(obj);
                if (theMap != null) {
                    theMap.requestActive();
                    theMap.CenterMarker(obj);
                }
            } else if (actionName.equals("ACTION_ShowPlace")) {
                GeoMapTopComponent theMap = getMapTopComponent(obj);
                if (theMap != null) {
                    theMap.requestActive();
                    theMap.ShowMarker(obj);
                }
            } else if (actionName.equals("ACTION_FindPlace")) {
                String info = obj.displayToponym(obj.getToponymFromPlace(obj.getPlace(), false));
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), info, NbBundle.getMessage(GeoNode.class, "TXT_geoinfo"), JOptionPane.INFORMATION_MESSAGE,
                        new ImageIcon(Utilities.loadImage("genjfr/app/geo/geoicon.png")));
            } else if (actionName.equals("ACTION_EditPlace")) {
                GeoPlaceEditor editorPanel = new GeoPlaceEditor(obj);
                DialogDescriptor dd = new DialogDescriptor(editorPanel, "Edition d'un lieu", true, null);
                editorPanel.requestFocus();
                DialogDisplayer.getDefault().createDialog(dd).show();
                if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                    editorPanel.updateGedcom(obj.getPlace());
                } else {
                    //cancel button was pressed
                }

            } else if (actionName.equals("ACTION_EditEvent")) {
                EditTopComponent etc = getEditTopComponent(obj);
                if (etc != null) {
                    etc.requestActive();
                    etc.setCurrentEntity(obj.getProperty());
                }
            }
        }
    }

    private GeoMapTopComponent getMapTopComponent(GeoNodeObject obj) {
        GeoMapTopComponent theList = null;
        if (obj == null) {
            return theList;
        }
        // get map top component
        for (TopComponent tc : WindowManager.getDefault().getRegistry().getOpened()) {
            if (tc instanceof GeoMapTopComponent) {
                GeoMapTopComponent gmtc = (GeoMapTopComponent) tc;
                if (gmtc.getGedcom() == obj.getGedcom()) {
                    theList = gmtc;
                    break;
                }
            }
        }
        return theList;
    }

    private EditTopComponent getEditTopComponent(GeoNodeObject obj) {
        EditTopComponent theEditor = null;
        if (obj == null) {
            return theEditor;
        }
        // get map top component
        for (TopComponent tc : WindowManager.getDefault().getRegistry().getOpened()) {
            if (tc instanceof EditTopComponent) {
                EditTopComponent gmtc = (EditTopComponent) tc;
                if (gmtc.getCurrentEntity().getGedcom() == obj.getGedcom()) {
                    theEditor = gmtc;
                    break;
                }
            }
        }
        return theEditor;
    }
}
