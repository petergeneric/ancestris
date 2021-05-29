/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2018 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.nav;

import ancestris.api.editor.AncestrisEditor;
import ancestris.modules.beans.ABluePrintBeans;
import genj.gedcom.Indi;
import java.awt.event.ActionListener;

/**
 *
 * @author Zurga
 */
class SpouseHandler extends ABeanHandler {

    private final ABluePrintBeans otherBean;

    public SpouseHandler(FamilyPanel fPane, ABluePrintBeans other) {
        super(fPane);
        this.otherBean = other;
    }

    @Override
    public ActionListener getCreateAction() {
        Indi indi = null;
        if (otherBean != null) {
            indi = (Indi) otherBean.getProperty();
        }
        AncestrisEditor editor = AncestrisEditor.findEditor(indi);
        if (editor == null) {
            // get NoOp editro
            editor = AncestrisEditor.findEditor(null);
        }
        return editor.getCreateSpouseAction(indi);
    }
}
