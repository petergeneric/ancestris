/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.app;

import ancestris.api.newgedcom.ModifyGedcom;
import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.view.Images;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JFrame;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

@ActionID(
        category = "File",
        id = "ancestris.app.ActionProperties"
)
@ActionRegistration(
        iconBase = "ancestris/app/Properties.png",
        displayName = "#CTL_ActionProperties"
)
@ActionReference(path = "Menu/File", position = 400)
public final class ActionProperties extends AbstractAncestrisContextAction {

    /** gedcom */
    private Context contextBeingModified = null;
    
    /** constructor */
    public ActionProperties() {
//      setAccelerator(ACC_PROP);
        setText(NbBundle.getMessage(ActionNew.class,"CTL_ActionProperties"));
        setTip(NbBundle.getMessage(ActionNew.class,"HINT_ActionProperties"));
        setImage(Images.imgProperties);
    }

    public ActionProperties(Context context) {
        this();
        contextBeingModified = context;
    }


    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        
        //
        // SET MAIN WINDOW TITLE
        //
        String gedcomName = "";
        String title = NbBundle.getBundle("org/netbeans/core/windows/view/ui/Bundle").getString("CTL_MainWindow_Title");
        Context localContext = getContext();
        if (localContext != null && localContext.getGedcom() != null) {
            //gedcomName = localContext.getGedcom().getFilePath();
            gedcomName = localContext.getGedcom().getDisplayName();
        }
        if (!gedcomName.isEmpty()) {
            title = gedcomName + " - " + title;
        }
        JFrame mainFrame = (JFrame) WindowManager.getDefault().getMainWindow();
        mainFrame.setTitle(title);
        
        // Continue
        super.contextChanged();
        
        
        super.contextChanged();
    }

    

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        Collection list = Lookup.getDefault().lookupAll(ModifyGedcom.class);
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            ModifyGedcom wiz = (ModifyGedcom) iterator.next();
            if (wiz.isReady()) {
                if (contextBeingModified == null) {
                    wiz.update();
                } else {
                    wiz.update(contextBeingModified);
                }
                return;
            }
        }
    }
}
