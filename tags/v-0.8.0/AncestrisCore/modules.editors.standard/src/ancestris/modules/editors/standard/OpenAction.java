/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2011 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard;

import ancestris.view.AncestrisTopComponent;
import genj.gedcom.Context;
import genj.gedcom.Property;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

@ActionID(id = "ancestris.editor.EditorStdAction", category = "Window")
@ActionRegistration(iconBase = "ancestris/modules/editors/standard/editeur_standard.png", displayName = "#CTL_EditorStdAction", iconInMenu = true)
@ActionReference(path = "Menu/View", name = "AncestrisEditorStdAction", position = -501)
public final class OpenAction implements ActionListener {

    private Context context = null;

    public OpenAction(Property context) {
        this.context = new Context(context);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (context != null) {
            AncestrisTopComponent win = new EditorTopComponent().create(context);
//            win.init(contextToOpen);
            win.open();
            win.requestActive();
        }
    }
}
