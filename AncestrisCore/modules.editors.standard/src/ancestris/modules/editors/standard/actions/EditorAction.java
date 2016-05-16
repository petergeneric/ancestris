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
import ancestris.modules.editors.standard.EditorTopComponent;
import ancestris.view.AncestrisTopComponent;
import genj.gedcom.Context;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
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
        if ((contextToOpen = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            AncestrisTopComponent win = new EditorTopComponent().create(contextToOpen);
            win.open();
            win.requestActive();
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
    
    
}
