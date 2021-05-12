/*
 * Ancestris - https://www.ancestris.org
 * 
 * Copyright 2018 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.webbook;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

/**
 *
 * @author frederic
 */
@ActionID(
        category = "Tools",
        id = "ancestris.modules.webbook.WebBookButtonAction"
)
@ActionRegistration(
        iconBase = "ancestris/modules/webbook/WebBook.png",
        displayName = "#CTL_WebBookButtonAction"
)
@ActionReferences({
    @ActionReference(path = "Toolbars/Misc", position = 300)
})
public class WebBookButtonAction implements ActionListener {

    private genj.util.Registry registry;

    public WebBookButtonAction(DataObject context) {  // The simple presence of DataObject as a parameter ensures action is enabled/disabled depending on selected gedcom context
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Context context;
        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            boolean regenerate = false;
            Gedcom gedcom = context.getGedcom();
            registry = gedcom.getRegistry();
            String latestLink = registry.get("localwebsite", "");
            if (!latestLink.isEmpty() && Desktop.isDesktopSupported()) {
                try {
                    URI uri = new URI(latestLink);
                    Desktop.getDesktop().browse(uri);
                    return;
                } catch (IOException | URISyntaxException ex) {
                    regenerate = true;
                }
            } else {
                regenerate = true;
            }
            if (regenerate) {
                new WebBookWizardAction().actionPerformed(null);
            }
        }

    }
}
