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

import ancestris.api.editor.PlaceEditor;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.DialogManager.ADialog;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import javax.swing.JComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author daniel
 */
public class MapPlaceEditor implements PlaceEditor{
    private final PlaceEditorPanel editorPanel;

    public MapPlaceEditor() {
        editorPanel = new PlaceEditorPanel();
    }

    
    @Override
    public PlaceEditor setup(Property parent,PropertyPlace place) {
        editorPanel.set(parent, place);
        return this;
    }

    @Override
    public JComponent getEditorPanel() {
        return editorPanel;
    }

    @Override
    public PropertyPlace edit() {
        ADialog dialog = new ADialog(NbBundle.getMessage(MapPlaceEditor.class, "PlaceEditorPanel.edit.title"), getEditorPanel());
        if (dialog.show() == ADialog.OK_OPTION) {
            return editorPanel.get();
        } else {
            return null;
        }
    }

    @Override
    public PropertyPlace commit() {
        editorPanel.commit();
        return editorPanel.get();
    }
    
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
