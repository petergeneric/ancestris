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
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.view.Images;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@ActionID(
        category = "File",
        id = "ancestris.app.ActionProperties"
)
@ActionRegistration(
        iconBase = "ancestris/app/Properties.png",
        displayName = "#CTL_ActionProperties"
)
@ActionReference(path = "Menu/File", position = 2050)
public final class ActionProperties extends AbstractAncestrisAction implements ActionListener {

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
    public void actionPerformed(ActionEvent e) {
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
