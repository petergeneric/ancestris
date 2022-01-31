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

import ancestris.core.actions.AbstractAncestrisContextAction;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.io.FileAssociation;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

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
public class WebBookButtonAction extends AbstractAncestrisContextAction {

    private genj.util.Registry registry;

    public WebBookButtonAction() {
        super();
        putValue("iconBase", "ancestris/modules/webbook/WebBook.png"); // FL: use this instead to have both icon in 16x16 and 24x24 size for toolbar
        setText(NbBundle.getMessage(WebBookButtonAction.class, "CTL_WebBookAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        Context contextToOpen = getContext();
        String text = getLatestLink(contextToOpen);
        if (text.isEmpty()) {
            text = NbBundle.getMessage(WebBookButtonAction.class, "CTL_WebBookAction");
        } else {
            text = NbBundle.getMessage(WebBookButtonAction.class, "CTL_WebBookButtonAction");
        }
        setText(text);
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        final Context contextToOpen = getContext();
        if (contextToOpen != null) {            
            boolean regenerate = false;
            String link = getLatestLink(contextToOpen);
            if (!link.isEmpty()) {
                try {
                    FileAssociation.getDefault().execute(new URL(link));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                regenerate = true;
            }
            if (regenerate) {
                new WebBookWizardAction().actionPerformed(event);
            }
        }
    }
    
    private String getLatestLink(Context contextToOpen) {
        if (contextToOpen == null) {
            return "";
        }
        Gedcom gedcom = contextToOpen.getGedcom();
        registry = gedcom.getRegistry();
        return registry.get("localwebsite", "");
    }
}
