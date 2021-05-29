/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2018 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.sosanumbers;

import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        id = "ancestris.modules.gedcom.sosanumbers"
)
@ActionRegistration(
        iconBase = "ancestris/modules/gedcom/sosanumbers/SosaNumbersIcon.png",
        displayName = "#CTL_DecujusButtonAction"
)
@ActionReferences({
    @ActionReference(path = "Toolbars/Misc", position = 400)
})
public class DecujusButtonAction implements ActionListener {

    public DecujusButtonAction(DataObject context) {  // The simple presence of DataObject as a parameter ensures action is enabled/disabled depending on selected gedcom context
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Context context;
        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Gedcom gedcom = context.getGedcom();
            Indi decujus = gedcom.getDeCujusIndi();
            if (decujus != null) {
                ActionEvent ae = new ActionEvent(e.getSource(), e.getID(), e.getActionCommand(), e.getModifiers() | ActionEvent.CTRL_MASK);
                SelectionDispatcher.fireSelection(ae, new Context(decujus));
            } else {
                new GenerateSosaAction().actionPerformed(null);
            }
        }

    }
}
