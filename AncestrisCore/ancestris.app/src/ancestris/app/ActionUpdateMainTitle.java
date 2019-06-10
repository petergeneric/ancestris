/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2019 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.app;

import ancestris.core.actions.AbstractAncestrisContextAction;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

@ActionID(id = "ancestris.app.ActionUpdateMainTitle", category = "File")
@ActionRegistration(displayName = "ActionUpdateMainTitle", iconInMenu = true, lazy = false)
@ActionReference(path = "Menu/File", name = "ActionUpdateMainTitle", position = -999)
public final class ActionUpdateMainTitle extends AbstractAncestrisContextAction {

    public ActionUpdateMainTitle() {
        super();
    }

    @Override
    protected void contextChanged() {
        ResourceBundle rb = NbBundle.getBundle("org/netbeans/core/windows/view/ui/Bundle");
        String gedcomName = "";
        String title = rb.getString("CTL_MainWindow_Title");
        Context localContext = getContext();
        if (localContext != null) {
            gedcomName = localContext.getGedcom().getName();
        }
        if (!gedcomName.isEmpty()) {
            title = gedcomName + " - " + title;
        }
        JFrame mainFrame = (JFrame) WindowManager.getDefault().getMainWindow();
        mainFrame.setTitle(title);
        
        // Continue
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        // do nothing
    }
}
