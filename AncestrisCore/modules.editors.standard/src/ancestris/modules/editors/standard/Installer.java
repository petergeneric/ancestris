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

import genj.gedcom.Context;
import genj.util.swing.Action2;
import genj.util.swing.Action2.Group;
import genj.view.ActionProvider;
import ancestris.core.pluginservice.AncestrisPlugin;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.view.SelectionSink;
import java.awt.event.ActionEvent;
import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall implements ActionProvider {

    @Override
    public void restored() {
        AncestrisPlugin.register(this);
        // By default, do nothing.
        // Put your startup code here.
    }

    public void createActions(Context context, Purpose purpose, Group into) {
        if (purpose != Purpose.CONTEXT) {
            return;
        }
        // XXX: must be rewritten
        if (context == null) {
            return;
        }
        if (!(context.getEntity() instanceof Indi)
                && !(context.getEntity() instanceof Fam)) {
            return;
        }
        into.add(new OpenEditor(context));
    }

    private static class OpenEditor extends Action2 {

        Context context;

        public OpenEditor(Context context) {
            this.context = context;
            setText(ResourcesSupport.getTitle("OpenInEditor"));  // NOI18N
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
