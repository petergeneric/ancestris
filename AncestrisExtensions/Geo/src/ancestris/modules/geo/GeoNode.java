/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.api.editor.AncestrisEditor;
//XXX: DAN: remove direct dependency to editors, use lookup
import ancestris.modules.editors.gedcom.EditTopComponent;
import ancestris.modules.editors.gedcom.GedcomEditorPlugin;
import ancestris.modules.editors.genealogyeditor.GenealogyEditorPlugin;
import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.modules.editors.geoplace.MapPlaceEditor;
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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.netbeans.api.javahelp.Help;
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

    private final static String GEDCOM_EDITOR = NbBundle.getMessage(GedcomEditorPlugin.class, "OpenIDE-Module-Name");
    private final static String ANCESTRIS_EDITOR = NbBundle.getMessage(GenealogyEditorPlugin.class, "OpenIDE-Module-Name");
    
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
            String text = obj.toDisplayString();
            String color = obj.getColor();
            return obj.isEvent ? "<font color='!textText'>" + text + " </font>&nbsp;"
                    : "<font color='!textText'>" + text + "</font><font " + color + "> [" + obj.getTextCoordinates() + "]"
                    + " <i></font><font color='!textText'>(" + obj.getNbOfEvents() + ")" + "</i></font>&nbsp;";
        }
        return "";
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
                        new GeoAction("ACTION_SelectEvent"),
                        null,
                        new GeoAction("ACTION_EditEvent", ANCESTRIS_EDITOR),
                        new GeoAction("ACTION_EditEvent", GEDCOM_EDITOR),
                        null,
                        new GeoAction("ACTION_HelpEvent")};
        } else {
            GeoNodeObject obj = getLookup().lookup(GeoNodeObject.class);
            if (obj == null) {
                return null;
            }
            if (obj.toDisplayString().equals(NbBundle.getMessage(GeoListTopComponent.class, "GeoEmpty"))) {
                return new Action[]{
                            new GeoAction("ACTION_None")};

            } else {
                return new Action[]{
                            new GeoAction("ACTION_ShowPlace"),
                            new GeoAction("ACTION_FindPlace"),
                            null,
                            new GeoAction("ACTION_EditPlace"),
                            null,
                            new GeoAction("ACTION_CopyPlace"),
                            new GeoAction("ACTION_PastePlace", GeoPlacesList.getInstance(obj.getPlace().getGedcom()).getCopiedPlace() != null),
                            null,
                            new GeoAction("ACTION_UpdateList"),
                            new GeoAction("ACTION_UpdatePlaceOptions"),
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

        public GeoAction(String name, String extension) {
            this.actionName = name + extension;
            putValue(NAME, NbBundle.getMessage(GeoNode.class, name, extension));
        }

        public GeoAction(String name, boolean enabled) {
            this.actionName = name;
            putValue(NAME, NbBundle.getMessage(GeoNode.class, name));
            setEnabled(enabled);
        }

        @SuppressWarnings("deprecation")
        public void actionPerformed(ActionEvent e) {
            final GeoNodeObject obj = getLookup().lookup(GeoNodeObject.class);
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
                    theMap.setZoom(4);
                    theMap.CenterMarker(obj);
                    theMap.ShowMarker(obj);
                }

            } else if (actionName.equals("ACTION_FindPlace")) {
                // display place details
                String info = obj.displayToponym(obj.getToponymFromPlace(obj.getPlace(), false));
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), info, NbBundle.getMessage(GeoNode.class, "TXT_geoinfo"), JOptionPane.INFORMATION_MESSAGE,
                        new ImageIcon(ImageUtilities.loadImage("ancestris/modules/geo/geoicon.png")));

            } else if (actionName.equals("ACTION_EditPlace")) {
                // Popup editor
                Gedcom gedcom = obj.getGedcom();
                int undoNb = gedcom.getUndoNb();
                // XXX: use lookup
                final AncestrisEditor editor = new MapPlaceEditor();
                GeoPlacesList.getInstance(gedcom).stopListening();
                genj.gedcom.Property p = obj.getPlace();
                if (p == null){
                    p = editor.add(obj.getProperty());
                } else {
                    p = editor.edit(p);
                }
                try {
//                (PropertyPlace)editor.edit(obj.getPlace(),obj.getProperty());
                    obj.updateAllEventsPlaces((PropertyPlace)p); // need to update everytime (even if p == null) as listener has been stopped
                } catch (ClassCastException ex){}
//                
//                if (editor.edit(obj.getPlace(),obj.getProperty()) != null){
//                    try {
//                        gedcom.doUnitOfWork(new UnitOfWork() {
//
//                            @Override
//                            public void perform(Gedcom gedcom) throws GedcomException {
//                                PropertyPlace p = editor.commit();  // writes place edited and also writes geocoordinates into gedcom file
//                                if (p != null){
//                                    // update all other places in gedcom and refresh list
//                                    obj.updateAllEventsPlaces(p);
//                                }
//                            }
//                        });
                GeoPlacesList.getInstance(gedcom).refreshPlaceName();
                GeoPlacesList.getInstance(gedcom).startListening();    
//                    } catch (GedcomException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//                } else {
//                    while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
//                        gedcom.undoUnitOfWork(false);
//                    }
//                }
            } else if (actionName.equals("ACTION_CopyPlace")) {
                GeoPlacesList.getInstance(obj.getGedcom()).setCopiedPlace(obj.getPlace());
            } else if (actionName.equals("ACTION_PastePlace")) {
                Gedcom gedcom = obj.getPlace().getGedcom();
                try {
                    GeoPlacesList.getInstance(gedcom).stopListening();
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            obj.updateAllEventsPlaces(GeoPlacesList.getInstance(gedcom).getCopiedPlace());
                            GeoPlacesList.getInstance(gedcom).setCopiedPlace(null);
                        }
                    });
                    GeoPlacesList.getInstance(gedcom).refreshPlaceName();
                    GeoPlacesList.getInstance(gedcom).startListening();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (actionName.equals("ACTION_UpdateList")) {
                GeoPlacesList.getInstance(obj.getGedcom()).launchPlacesSearch();
            } else if (actionName.equals("ACTION_UpdatePlaceOptions")) {
                if (GeoPlacesList.getInstance(obj.getGedcom()).initPlaceDisplayFormat(true)) {
                    GeoPlacesList.getInstance(obj.getGedcom()).launchPlacesSearch();
                }
            } else if (actionName.equals("ACTION_EditEvent"+GEDCOM_EDITOR)) {
                EditTopComponent etc = new EditTopComponent();
                etc.init(new Context( obj.getProperty()));
                etc.open();
                etc.requestActive();
            } else if (actionName.equals("ACTION_EditEvent"+ANCESTRIS_EDITOR)) {
                // Pop up editor
                Entity entity = obj.getProperty().getEntity();
                if (entity instanceof Fam) {
                    Fam family = (Fam) entity;
                    //XXX: use Editor API to find proper editor
                    FamilyEditor familyEditor = new FamilyEditor();
                    familyEditor.setContext(new Context(obj.getProperty()));
                    familyEditor.showPanel();
                } else if (entity instanceof Indi) {
                    Indi child = (Indi) entity;
                    //XXX: use Editor API to find proper editor
                    IndividualEditor individualEditor = new IndividualEditor();
                    individualEditor.setContext(new Context(obj.getProperty()));
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

//    // FIXME : should not be here nor static. Fix later.
//    public static void editPlace(GeoNodeObject gno) {
//        final GeoNodeObject obj = gno;
//        // Popup editor
//        Gedcom gedcom = obj.getGedcom();
//        int undoNb = gedcom.getUndoNb();
//        final PlaceEditorPanel placeEditorPanel = new PlaceEditorPanel();
//        placeEditorPanel.set(obj.getProperty(), obj.getPlace(), null, obj.getGeoPosition());
//        placeEditorPanel.hideAddressPanel();
//        //placeEditorPanel.runSearch();
//        ADialog eventEditorDialog = new ADialog(NbBundle.getMessage(PlaceEditorPanel.class, "PlaceEditorPanel.edit.title"), placeEditorPanel);
//        eventEditorDialog.setDialogId(PlaceEditorPanel.class.getName());
//
//        if (eventEditorDialog.show() == DialogDescriptor.OK_OPTION) {
//            try {
//                GeoPlacesList.getInstance(gedcom).stopListening();
//                gedcom.doUnitOfWork(new UnitOfWork() {
//
//                    @Override
//                    public void perform(Gedcom gedcom) throws GedcomException {
//                        placeEditorPanel.commit();  // writes place edited and also writes geocoordinates into gedcom file
//                        // update all other places in gedcom and refresh list
//                        obj.updateAllEventsPlaces(obj.getPlace());
//                    }
//                });
//                GeoPlacesList.getInstance(gedcom).refreshPlaceName();
//                GeoPlacesList.getInstance(gedcom).startListening();
//            } catch (GedcomException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        } else {
//            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
//                gedcom.undoUnitOfWork(false);
//            }
//        }
//    }
//    
    
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
