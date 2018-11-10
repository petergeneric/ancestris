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
class ParentHandler extends ABeanHandler {
    int sex;
    private final ABluePrintBeans childBean;

    public ParentHandler(FamilyPanel fPane, ABluePrintBeans indiBean, int sex) {
        super(fPane);
        this.childBean = indiBean;
        this.sex = sex;
    }

    @Override
    public ActionListener getCreateAction() {
        Indi child = null;
        if (childBean != null) {
            child = (Indi) childBean.getProperty();
        }
        AncestrisEditor editor = AncestrisEditor.findEditor(child);
        if (editor == null) {
            // get NoOp editor
            editor = AncestrisEditor.findEditor(null);
        }

        return editor.getCreateParentAction(child, sex);
    }
}
