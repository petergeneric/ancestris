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
package ancestris.modules.treesharing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

@ActionID(
        category = "Tools",
        id = "ancestris.modules.treesharing.TreeSharingAction"
)
@ActionRegistration(
        iconBase = "ancestris/modules/treesharing/resources/treesharing.png",
        displayName = "#CTL_TreeSharingAction"
)
@ActionReference(path = "Menu/Tools", position = 94) //, separatorBefore = 93, separatorAfter = 95)
public final class TreeSharingAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        TreeSharingTopComponent tc = TreeSharingTopComponent.getDefault();
        tc.open();
        tc.requestActive();
    }
}
