/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard.actions;

import ancestris.api.editor.AncestrisEditor;
import genj.gedcom.Indi;
import javax.swing.Action;

/**
 *
 * @author daniel
 */
public class EditorActions {
    public static Action getCreateParentAction(Indi child, int sex, AncestrisEditor editor) {
        return AActions.alwaysEnabled(
                new ACreateParent(child, sex, editor),
                "",
                org.openide.util.NbBundle.getMessage(EditorActions.class, "action.createparent.title"),
                "ancestris/modules/editors/standard/images/add-child.png", // NOI18N
                true);
    }

    public static Action getCreateSpouseAction(Indi indi, AncestrisEditor editor) {
        return AActions.alwaysEnabled(
                new ACreateSpouse(indi, editor),
                "",
                org.openide.util.NbBundle.getMessage(EditorActions.class, "action.addspouse.title"),
                "ancestris/modules/editors/standard/images/add-spouse.png", // NOI18N
                true);
    }
}
