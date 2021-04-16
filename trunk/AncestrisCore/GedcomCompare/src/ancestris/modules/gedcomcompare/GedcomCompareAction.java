/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015-2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcomcompare;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionID(
        category = "Tools",
        id = "ancestris.modules.gedcomcompare.GedcomCompareAction"
)
@ActionRegistration(
        iconBase = "ancestris/modules/gedcomcompare/resources/comparegedcom.png",
        displayName = "#CTL_GedcomCompareAction"
)
@ActionReferences({
    @ActionReference(path = "Toolbars/Misc", position = 99),
    @ActionReference(path = "Menu/Tools", position = 9),
})
public final class GedcomCompareAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        GedcomCompareTopComponent tc = GedcomCompareTopComponent.getDefault();
        tc.open();
        tc.requestActive();
    }
}