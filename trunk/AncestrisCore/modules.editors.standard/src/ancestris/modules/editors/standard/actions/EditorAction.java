/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.editors.standard.actions;

import ancestris.api.editor.AncestrisEditor;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.editors.standard.EditorTopComponent;
import ancestris.modules.editors.standard.tools.IndiCreator;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import java.util.List;
import javax.swing.Action;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author frederic
 */
@ServiceProvider(service = AncestrisEditor.class, position = 195)
public class EditorAction extends AncestrisEditor {
    
    @Override
    public boolean canEdit(Property property) {
        return (property instanceof Indi
                || property instanceof Fam);
//                || property instanceof Note
//                || property instanceof Media
//                || property instanceof Source
//                || property instanceof Repository);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public Property edit(Property property, boolean isNew) {
        Context contextToOpen;
        if (property == null) {
            contextToOpen = Utilities.actionsGlobalContext().lookup(Context.class);
        } else {
            contextToOpen = new Context(property);
        }
        if (contextToOpen != null) {
            EditorTopComponent editorTopComponent = getCurrentEditorTopComponent(contextToOpen);
            if (editorTopComponent != null) {
                //editorTopComponent.setContext(contextToOpen);
                //editorTopComponent.requestActive();
                SelectionDispatcher.fireSelection(contextToOpen);
            } else {
                AncestrisTopComponent win = new EditorTopComponent().create(contextToOpen);
                win.open();
                win.requestActive();
            }
        }
        return null;
    }

    @Override
    public Property add(Property parent) {
        return null;
    }

    public String getName(boolean canonical) {
        if (canonical) {
            return getClass().getCanonicalName();
        } else {
            return NbBundle.getMessage(EditorTopComponent.class, "OpenIDE-Module-Name");
        }
    }
    
    @Override
    public String toString() {
        return getName(false);
    }

    @Override
    public Action getCreateParentAction(Indi indi, int sex) {
        EditorTopComponent editorTopComponent = getCurrentEditorTopComponent(new Context(indi));
        if (editorTopComponent != null) {
            return new ActionCreation(
                        editorTopComponent, 
                        IndiCreator.CREATION, 
                        sex == PropertySex.MALE ? IndiCreator.REL_FATHER : IndiCreator.REL_MOTHER);
        }
        return getDefaultAction(indi);
    }

    @Override
    public Action getCreateSpouseAction(Indi indi) {
        EditorTopComponent editorTopComponent = getCurrentEditorTopComponent(new Context(indi));
        if (editorTopComponent != null) {
            return new ActionCreation(
                        editorTopComponent, 
                        IndiCreator.CREATION, 
                        IndiCreator.REL_PARTNER);
                
        }
        return getDefaultAction(indi);
    }
    
    
    
    

    private EditorTopComponent getCurrentEditorTopComponent(Context context) {
        EditorTopComponent ret = null;
        for (EditorTopComponent editorTopComponent : (List<EditorTopComponent>) AncestrisPlugin.lookupAll(EditorTopComponent.class)) {
            if (editorTopComponent.getEditor() != null) {
                if (editorTopComponent.getEditor().getEditedIndi() == context.getEntity()) {
                    return editorTopComponent;
                }
                ret = editorTopComponent;
            }
        }
        return ret;
    }
    
    
}
