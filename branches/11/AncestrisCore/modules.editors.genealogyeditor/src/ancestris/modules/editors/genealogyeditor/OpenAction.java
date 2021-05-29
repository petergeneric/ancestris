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
package ancestris.modules.editors.genealogyeditor;

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.view.AncestrisTopComponent;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(id = "ancestris.editor.EditorGenealogyAction", category = "Window")
@ActionRegistration(
        displayName = "#CTL_EditorGenealogyAction",
        iconInMenu = false,
        lazy = false)
@ActionReference(path = "Menu/View", name = "AncestrisEditorGenealogyAction", position = -490)
public final class OpenAction extends AbstractAncestrisContextAction {

    public OpenAction() {
        super();
        setImage("ancestris/modules/editors/genealogyeditor/resources/Editor.png");
        setText(NbBundle.getMessage(OpenAction.class, "CTL_EditorGenealogyAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        Context contextToOpen = getContext();
        if (contextToOpen != null) {
            AncestrisTopComponent win = new AriesTopComponent().create(contextToOpen);
            win.open();
            win.requestActive();
        }
    }
}
