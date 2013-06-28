/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard;

import genj.gedcom.Fam;
import genj.gedcom.Indi;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Entity;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import static ancestris.modules.editors.standard.Bundle.*;

@ActionID(category = "Edit",
id = "ancestris.modules.editors.standard.OpenEditorAction")
@ActionRegistration(displayName="#OpenInEditor.title"
        )
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty")})
@Messages("OpenInEditor.title=Edit/Modify")
public final class OpenEditorAction
        extends AbstractAction
        implements ContextAwareAction {

    public @Override
    void actionPerformed(ActionEvent e) {
        assert false;
    }

    public @Override
    Action createContextAwareInstance(Lookup context) {
        return new OpenEditor(context.lookup(Entity.class));
    }

    private static final class OpenEditor extends AbstractAncestrisAction {

        Entity entity;

        public OpenEditor(Entity context) {
            this.entity = context;
            setText(OpenInEditor_title());  // NOI18N
            setImage(ResourcesSupport.editorIcon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SelectionDispatcher.muteSelection(true);
            if (entity instanceof Indi) {
                EntityEditor.editEntity( (Indi) (entity), false);
            }
            if (entity instanceof Fam) {
                EntityEditor.editEntity((Fam) (entity), false);
            }
            SelectionDispatcher.muteSelection(false);
        }
    }
}
