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

package genj.edit.actions;

import ancestris.api.editor.*;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.editors.gedcom.EditTopComponent;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import java.util.List;
import javax.swing.Action;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author frederic
 */
@ServiceProvider(service = AncestrisEditor.class, position = 190)
public class GedcomEditorAction extends AncestrisEditor {

    @Override
    public boolean canEdit(Property property) {
        return true;
    }

    @Override
    public boolean isActive() {
        return true;
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
            EditTopComponent editTopComponent = getCurrentEditorTopComponent(contextToOpen);
            if (editTopComponent != null) {
                SelectionDispatcher.fireSelection(contextToOpen);
                editTopComponent.requestActive();
            } else {
                editTopComponent = EditTopComponent.getFactory();
                editTopComponent.init(contextToOpen);
                editTopComponent.open();
                editTopComponent.requestActive();
            }
        }
        return null;
    }

    @Override
    public Property add(Property parent) {
        return null;
    }

    @Override
    public String getName(boolean canonical) {
        if (canonical) {
            return getClass().getCanonicalName();
        } else {
            return NbBundle.getMessage(EditTopComponent.class, "OpenIDE-Module-Name");
        }
    }

    @Override
    public String toString() {
        return getName(false);
    }

    @Override
    public Action getCreateParentAction(Indi indi, int sex) {
        return getDefaultAction(indi);
    }

    @Override
    public Action getCreateSpouseAction(Indi indi) {
        return getDefaultAction(indi);
    }

    private EditTopComponent getCurrentEditorTopComponent(Context context) {
        EditTopComponent ret = null;
        for (EditTopComponent editTopComponent : (List<EditTopComponent>) AncestrisPlugin.lookupAll(EditTopComponent.class)) {
            Context tmpContext = editTopComponent.getContext();
            if (tmpContext != null) {
                if (tmpContext.getGedcom() == context.getGedcom())  {
                    return editTopComponent;
                }
                ret = editTopComponent;
            }
        }
        return null;
    }
    
    
}
