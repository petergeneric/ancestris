/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.geoplace;

import ancestris.api.editor.AncestrisEditor;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.DialogManager.ADialog;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.UnitOfWork;
import javax.swing.Action;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = AncestrisEditor.class,position = 100)
public class MapPlaceEditor extends AncestrisEditor{
    private final PlaceEditorPanel editorPanel;

    public MapPlaceEditor() {
        editorPanel = new PlaceEditorPanel();
    }

    @Override
    public boolean canEdit(Property property) {
        return property instanceof PropertyPlace;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public Property edit(Property property, boolean isNew) {
        return edit(property,null);
    }

    @Override
    public Property add(Property parent) {
        return edit(null,parent);
    }

//    @Override
//    public PlaceEditor setup(Property parent,PropertyPlace place) {
//        editorPanel.set(parent, place);
//        return this;
//    }

//    @Override
//    public JComponent getEditorPanel() {
//        return editorPanel;
//    }
//
    private Property edit(Property place, Property parent) {
        Gedcom gedcom=null;
        if (parent != null){
            gedcom = parent.getGedcom();
        }
        if (place != null){
            gedcom = place.getGedcom();
        }
        if (gedcom != null && (place instanceof PropertyPlace || place == null)){
            editorPanel.set(parent, (PropertyPlace)place);
            ADialog dialog = new ADialog(NbBundle.getMessage(MapPlaceEditor.class, "PlaceEditorPanel.edit.title"), editorPanel);
            if (dialog.show() == ADialog.OK_OPTION) {
                // Add dow:
                    try {
                        gedcom.doUnitOfWork(new UnitOfWork() {
                            @Override
                            public void perform(Gedcom gedcom) throws GedcomException {
                                editorPanel.commit();
                            }
                        });
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                return editorPanel.get();
            }
        }
        return null;
    }

//    @Override
//    public PropertyPlace commit() {
//        editorPanel.commit();
//        return editorPanel.get();
//    }
    
    /**
     * Open Place Format option dialog.
     * @param gedcom
     * @param forceEdit
     * @return true if place format has been edited
     */
    // XXX: use lookup and co to get options from editor
    // This should be put in PlaceEditor interface (API) and change PlaceEditor to abstract class
    public static boolean updatePlaceFormat(Gedcom gedcom,boolean forceEdit){
        
        boolean ret = false;

        PlaceFormatEditorOptionsPanel pfeop = new PlaceFormatEditorOptionsPanel(gedcom);
        if (!pfeop.isRegisteredPlaceSortOrder() || forceEdit) {
            DialogManager.ADialog gedcomPlaceFormatEditorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(PlaceFormatEditorOptionsPanel.class, "PlaceFormatEditorOptionsPanel.title"),
                    pfeop);
            gedcomPlaceFormatEditorDialog.setDialogId(PlaceFormatEditorOptionsPanel.class.getName());
            if (gedcomPlaceFormatEditorDialog.show() == ADialog.OK_OPTION) {
                pfeop.commit();
                ret = true;
            }
        } 
        return ret;
    }
}
