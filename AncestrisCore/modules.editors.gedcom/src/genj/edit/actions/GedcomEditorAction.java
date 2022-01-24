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
import ancestris.modules.editors.gedcom.GedcomTopComponent;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author frederic
 */
@ServiceProvider(service = AncestrisEditor.class, position = 190)
public class GedcomEditorAction extends AncestrisEditor {

    private ImageIcon editorIcon = new ImageIcon(GedcomEditorAction.class.getResource("/genj/edit/images/Editor.png")); // NOI18N

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
            GedcomTopComponent editTopComponent = getCurrentEditorTopComponent(contextToOpen);
            if (editTopComponent != null) {
                SelectionDispatcher.fireSelection(contextToOpen);
                editTopComponent.requestActive();
            } else {
                // 2021-05-20 FL : it seems to be a bad idea to use getFactory() as it is likely to reuse another Gedcom component with the side effect of destroying its context.
                // => so create a new TopComponent.
                // editTopComponent = GedcomTopComponent.getFactory();
                editTopComponent = new GedcomTopComponent(); 
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
            return NbBundle.getMessage(GedcomTopComponent.class, "OpenIDE-Module-Name");
        }
    }

    @Override
    public ImageIcon getIcon() {
        return editorIcon;
    }

    @Override
    public String toString() {
        return getName(false);
    }

    @Override
    public Action getCreateParentAction(Indi indi, int sex) {
        return new CreateParent(indi, sex);
    }

    @Override
    public Action getCreateSpouseAction(Indi indi) {
        return new CreateSpouse(indi);
    }

    private GedcomTopComponent getCurrentEditorTopComponent(Context context) {
        GedcomTopComponent ret = null;
        for (GedcomTopComponent editTopComponent : (List<GedcomTopComponent>) AncestrisPlugin.lookupAll(GedcomTopComponent.class)) {
            Context tmpContext = editTopComponent.getContext();
            if (tmpContext != null) {
                if (tmpContext.getGedcom() == context.getGedcom())  {
                    return editTopComponent;
                }
            }
        }
        return ret;
    }
    
    
}
