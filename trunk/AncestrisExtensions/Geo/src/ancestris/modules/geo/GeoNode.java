/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.modules.editors.genealogyeditor.panels.PlaceEditorPanel;
import ancestris.util.swing.DialogManager.ADialog;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertyPlace;
import genj.gedcom.UnitOfWork;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.netbeans.api.javahelp.Help;
import org.openide.DialogDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;

/**
 *
 * @author frederic
 */
class GeoNode extends AbstractNode implements PropertyChangeListener {

    public GeoNode(GeoPlacesList gpl) {
        super(new GeoChildrenNodes(gpl));
        setDisplayName(NbBundle.getMessage(GeoListTopComponent.class, "GeoListRoot") + " " + gpl.getGedcom().getName());
    }

    public GeoNode(GeoNodeObject obj) {
        super(obj != null && !obj.isEvent ? new GeoChildrenNodes(obj) : Children.LEAF, Lookups.singleton(obj));
        if (obj != null) {
            setDisplayName(obj.toString());
            obj.addPropertyChangeListener(WeakListeners.propertyChange(this, obj));
        }
    }

    @Override
    public String getHtmlDisplayName() {
        GeoNodeObject obj = getLookup().lookup(GeoNodeObject.class);
        if (obj != null) {
            String color = obj.isUnknown ? "color='#ff2300'" : "color='#03a60d'"; // color='!textText'"; "color='!controlShadow'"
            return obj.isEvent ? "<font color='!textText'>" + obj.toString() + " </font>&nbsp;"
                    : "<font color='!textText'>" + obj.toString() + "</font><font " + color + "> [" + obj.getCoordinates() + "]"
                    + " <i></font><font color='!textText'>(" + obj.getNbOfEvents() + ")" + "</i></font>&nbsp;";
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public Image getIcon(int type) {
        GeoNodeObject obj = getLookup().lookup(GeoNodeObject.class);
        return (obj != null) ? obj.getIcon() : ImageUtilities.loadImage("ancestris/modules/geo/geo.png");
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
            return new GeoAction("ACTION_SelectEvent");
        } else {
            return new GeoAction("ACTION_ShowPlace");
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
                        new GeoAction("ACTION_EditEvent"),
                        null,
                        new GeoAction("ACTION_HelpEvent")};
        } else {
            GeoNodeObject obj = getLookup().lookup(GeoNodeObject.class);
            if (obj == null) {
                return null;
            }
            if (obj.toString().equals(NbBundle.getMessage(GeoListTopComponent.class, "GeoEmpty"))) {
                return new Action[]{
                            new GeoAction("ACTION_None")};

            } else {
                return new Action[]{
                            new GeoAction("ACTION_ShowPlace"),
                            new GeoAction("ACTION_FindPlace"),
                            null,
                            new GeoAction("ACTION_EditPlace"),
                            null,
                            new GeoAction("ACTION_UpdateList"),
                            null,
                            new GeoAction("ACTION_HelpPlace")};
            }
        }
    }

    public void propertyChange(PropertyChangeEvent pce) {
        if ("topo".equals(pce.getPropertyName())) {
            this.fireDisplayNameChange(null, getDisplayName());
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
            if (actionName.equals("ACTION_None")) {
                // nothing
            } else if (actionName.equals("ACTION_ShowPlace")) {
                // display place on map
                GeoMapTopComponent theMap = getMapTopComponent(obj);
                if (theMap != null) {
                    theMap.requestActive();
                    theMap.CenterMarker(obj);
                    theMap.ShowMarker(obj);
                }

            } else if (actionName.equals("ACTION_FindPlace")) {
                // display place details
                String info = obj.displayToponym(obj.getToponymFromPlace(obj.getPlace(), false));
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), info, NbBundle.getMessage(GeoNode.class, "TXT_geoinfo"), JOptionPane.INFORMATION_MESSAGE,
                        new ImageIcon(ImageUtilities.loadImage("ancestris/modules/geo/geoicon.png")));

            } else if (actionName.equals("ACTION_EditPlace")) {
                // *** 2015-20-22 - the GeoPlaceEditor is no longer called
                //                  Call the Ancestris place editor instead
                //
                //final GeoPlaceEditor editorPanel = new GeoPlaceEditor(obj);
                //DialogDescriptor dd = new DialogDescriptor(editorPanel, "Edition d'un lieu", false, new ActionListener() {
                //
                //    public void actionPerformed(ActionEvent ae) {
                //        if (ae.getSource().equals(DialogDescriptor.OK_OPTION)) {
                //            editorPanel.updateGedcom();
                //        }
                //    }
                //});
                //editorPanel.requestFocus();
                //DialogDisplayer.getDefault().createDialog(dd).show();
                //
                
                
                // Popup editor
                Gedcom gedcom = obj.getPlace().getGedcom();
                int undoNb = gedcom.getUndoNb();
                final PlaceEditorPanel placeEditorPanel = new PlaceEditorPanel();
                placeEditorPanel.set(obj.getProperty(), obj.getPlace(), null);
                placeEditorPanel.hideAddressPanel();
                placeEditorPanel.runSearch();
                ADialog eventEditorDialog = new ADialog(NbBundle.getMessage(PlaceEditorPanel.class, "PlaceEditorPanel.edit.title"), placeEditorPanel);
                eventEditorDialog.setDialogId(PlaceEditorPanel.class.getName());

                if (eventEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                    try {
                        GeoPlacesList.getInstance(gedcom).stopListening();
                        gedcom.doUnitOfWork(new UnitOfWork() {

                            @Override
                            public void perform(Gedcom gedcom) throws GedcomException {
                                placeEditorPanel.commit();  // writes place edited and also writes geocoordinates into gedcom file
                                
                            }
                        });
                        
                        // update all places in gedcom
                        String place = obj.getPlace().getDisplayValue();
                        List<PropertyPlace> propPlaces = obj.getEventsPlaces();
                        for (PropertyPlace pp : propPlaces) {
                            pp.setValue(place);
                        }

                        // *** 2015-02-23 - geocoordinates are no longer stored locally
                        //                  Risk of loosing data when changing computer or upgrading system, ancestris, etc....
                        // remember for next time
                        //Toponym topo = obj.getToponym();
                        //updateTopoFromDialog(topo);
                        //obj.setToponym(topo);
                        //NbPreferences.forModule(GeoPlacesList.class).put(geoObj.getPlaceAsLongString(), geoObj.Toponym2Code(topo));

                        GeoPlacesList.getInstance(gedcom).refreshPlaceName();
                        
                        GeoPlacesList.getInstance(gedcom).startListening();    
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            } else if (actionName.equals("ACTION_UpdateList")) {
                GeoPlacesList.getInstance(obj.getGedcom()).launchPlacesSearch();

            } else if (actionName.equals("ACTION_EditEvent")) {
                // Pop up editor
                Entity entity = obj.getProperty().getEntity();
                if (entity instanceof Fam) {
                    Fam family = (Fam) entity;
                    FamilyEditor familyEditor = new FamilyEditor();
                    familyEditor.setContext(new Context(family));
                    familyEditor.showPanel();
                } else if (entity instanceof Indi) {
                    Indi child = (Indi) entity;
                    IndividualEditor individualEditor = new IndividualEditor();
                    individualEditor.setContext(new Context(child));
                    individualEditor.showPanel();
                }
            } else if (actionName.equals("ACTION_SelectEvent")) {
                SelectionDispatcher.fireSelection(new Context(obj.getProperty()));
            } else if (actionName.equals("ACTION_HelpPlace")) {
                String id = "ancestris.app.view.geo.menuplace";
                Help help = Lookup.getDefault().lookup(Help.class);
                if (help != null && help.isValidID(id, true)) {
                    help.showHelp(new HelpCtx(id));
                }
            } else if (actionName.equals("ACTION_HelpEvent")) {
                String id = "ancestris.app.view.geo.menuevent";
                Help help = Lookup.getDefault().lookup(Help.class);
                if (help != null && help.isValidID(id, true)) {
                    help.showHelp(new HelpCtx(id));
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
}
