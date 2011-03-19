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
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import genj.util.swing.Action2;
import genj.util.swing.Action2.Group;
import genj.view.ActionProvider;
import genjfr.app.pluginservice.GenjFrPlugin;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JScrollPane;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall implements ActionProvider{

    @Override
    public void restored() {
        GenjFrPlugin.register(this);
        // By default, do nothing.
        // Put your startup code here.
    }
    
    public void createActions(Context context, Purpose purpose, Group into) {
        if (purpose != Purpose.CONTEXT)
            return;
        for (EditTopComponent edit : (List<EditTopComponent>) GenjFrPlugin.lookupAll(EditTopComponent.class) ) {
            Context viewContext = edit.getContext();
            if (viewContext == null)
                continue;
            if (viewContext.getGedcom().equals(context.getGedcom())){
                into.add(new OpenInEditor(edit,context));
                return;
            }
        }
        into.add(new OpenInEditor(null,context));
    }

    private class OpenInEditor extends Action2 {

        EditTopComponent editor;
        Context context;

        public OpenInEditor(EditTopComponent editor, Context context) {
            this.editor = editor;
            this.context = context;
            setText(ResourcesSupport.getTitle("OpenInEditor"));
            setImage(ResourcesSupport.icon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (editor != null)
                editor.setContext(context,true);
            else{
                String title = NbBundle.getMessage(this.getClass(), "dialog.gedcom.edit.title", context.getGedcom());
                final GedcomPanel panel = new GedcomPanel();
                panel.setContext(context);
                NotifyDescriptor nd = new NotifyDescriptor(new JScrollPane(panel), title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
                DialogDisplayer.getDefault().notify(nd);
                if (nd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
                    panel.commit();
                }
            }
        }
    }

}
