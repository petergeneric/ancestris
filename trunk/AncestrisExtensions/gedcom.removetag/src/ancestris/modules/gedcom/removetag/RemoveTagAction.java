/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Dominique Baron (lemovice-at-ancestris-dot-org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.removetag;

import ancestris.app.App;
import ancestris.modules.gedcom.utlilities.GedcomUtilities;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public final class RemoveTagAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Context context;
        RemoveTagPanel removeTagPanel = new RemoveTagPanel();
        if ((context = App.center.getSelectedContext(true)) != null) {
            Gedcom gedcom = context.getGedcom();

            // Create a custom NotifyDescriptor, specify the panel instance as a parameter + other params
            NotifyDescriptor notifyDescriptor = new NotifyDescriptor(
                    removeTagPanel, // instance of your panel
                    "test", // title of the dialog
                    NotifyDescriptor.OK_CANCEL_OPTION, // it is Yes/No dialog ...
                    NotifyDescriptor.QUESTION_MESSAGE, // ... of a question type => a question mark icon
                    null,
                    NotifyDescriptor.NO_OPTION // default option is "Yes"
                    );
            if (DialogDisplayer.getDefault().notify(notifyDescriptor) == NotifyDescriptor.OK_OPTION) {
                new GedcomUtilities(gedcom).deleteTags(removeTagPanel.getTag(), removeTagPanel.getSelectedEntity());
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Hello...", NotifyDescriptor.INFORMATION_MESSAGE));
            }
        }
    }
}
