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

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
@ActionID(
        category = "Tools",
        id = "ancestris.modules.gedcom.sosanumbers.DecujusButtonAction"
)
@ActionRegistration(
        displayName = "#CTL_DecujusButtonAction",
        lazy = false)
@ActionReferences({
    @ActionReference(path = "Toolbars/Misc", position = 400)
})
public class DecujusButtonAction extends AbstractAncestrisContextAction {

    public DecujusButtonAction() {
        super();
        //setImage("ancestris/modules/gedcom/sosanumbers/SosaNumbersIcon.png");
        putValue("iconBase", "ancestris/modules/gedcom/sosanumbers/SosaNumbersIcon.png"); // FL: use this instead to have both icon in 16x16 and 24x24 size for toolbar
        setText(NbBundle.getMessage(DecujusButtonAction.class, "CTL_GenerateSosaAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        Context contextToOpen = getContext();
        if (contextToOpen != null) {
            Gedcom gedcom = contextToOpen.getGedcom();
            Indi decujus = gedcom.getDeCujusIndi();
            String text = NbBundle.getMessage(DecujusButtonAction.class, "CTL_GenerateSosaAction");
            if (decujus != null) {
                text = NbBundle.getMessage(DecujusButtonAction.class, "CTL_DecujusButtonAction");
            }
            setText(text);
        }
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent e) {
        final Context contextToOpen = getContext();
        if (contextToOpen != null) {
            Gedcom gedcom = contextToOpen.getGedcom();
            Indi decujus = gedcom.getDeCujusIndi();
            if (decujus != null) {
                ActionEvent ae = new ActionEvent(e.getSource(), e.getID(), e.getActionCommand(), e.getModifiers() | ActionEvent.CTRL_MASK);
                SelectionDispatcher.fireSelection(ae, new Context(decujus));
            } else {
                new GenerateSosaAction().actionPerformed(e);
            }
        }

    }
}
