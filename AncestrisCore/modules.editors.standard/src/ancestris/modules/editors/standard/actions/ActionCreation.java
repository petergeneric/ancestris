/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.editors.standard.actions;

import ancestris.api.editor.Editor;
import ancestris.modules.editors.standard.EditorTopComponent;
import ancestris.modules.editors.standard.IndiPanel;
import ancestris.modules.editors.standard.tools.IndiCreator;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.util.ChangeSupport;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author frederic
 */
public class ActionCreation extends AbstractAction {

    private EditorTopComponent editorTopComponent;
    private int type;
    private Indi indi;
    private Indi spouse;
    private int relation;
    private ChangeSupport changes;
    
    public ActionCreation(EditorTopComponent editorTopComponent, int type, int relation) {
        init(editorTopComponent, type, relation, null);
    }

    public ActionCreation(EditorTopComponent editorTopComponent, int type, int relation, Indi spouse) {
        init(editorTopComponent, type, relation, spouse);
    }

    private void init(EditorTopComponent editorTopComponent, int type, int relation, Indi spouse) {
        this.editorTopComponent = editorTopComponent;
        this.type = type;
        this.relation = relation;
        this.spouse = spouse;
        
        Entity entity = editorTopComponent.getEditor().getEditedEntity();
        if (entity instanceof Indi) {
            this.indi = (Indi) entity;
        } else {
            this.indi = null;
        }
        Editor editor = editorTopComponent.getEditor();
        if (editor instanceof IndiPanel) {
            this.changes = ((IndiPanel)editor).getChangeSupport();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (changes != null && changes.hasChanged()) {
            changes.fireChangeEvent(new Boolean(true));  // force changes to be saved (true) in a separate commit from the indi creation which is coming...
        }
        IndiCreator indiCreator = new IndiCreator(type, indi, relation, spouse, null);
        editorTopComponent.setContext(new Context(indiCreator.getIndi()));
        editorTopComponent.requestActive();
    }

    
}
