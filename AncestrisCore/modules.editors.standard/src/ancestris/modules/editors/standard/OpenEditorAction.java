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

import ancestris.core.actions.AbstractAncestrisAction;
import static ancestris.modules.editors.standard.Bundle.*;
import ancestris.view.SelectionSink;
import genj.gedcom.Context;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Edit",
id = "ancestris.modules.editors.standard.OpenEditorAction")
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty")})
public final class OpenEditorAction
        extends AbstractAction
        implements ContextAwareAction {

    public @Override
    void actionPerformed(ActionEvent e) {
        assert false;
    }

    public @Override
    Action createContextAwareInstance(Lookup context) {
        return new OpenEditor(context.lookup(Context.class));
    }

    @Messages("OpenInEditor.title=Edit/Modify")
    private static final class OpenEditor extends AbstractAncestrisAction {

        Context context;

        public OpenEditor(Context context) {
            this.context = context;
            setText(OpenInEditor_title());  // NOI18N
            setImage(ResourcesSupport.editorIcon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SelectionSink.Dispatcher.muteSelection(true);
            if (context.getEntity() instanceof Indi) {
                EntityEditor.editEntity((Indi) (context.getEntity()), false);
            }
            if (context.getEntity() instanceof Fam) {
                EntityEditor.editEntity((Fam) (context.getEntity()), false);
            }
            SelectionSink.Dispatcher.muteSelection(false);
        }
    }
}
