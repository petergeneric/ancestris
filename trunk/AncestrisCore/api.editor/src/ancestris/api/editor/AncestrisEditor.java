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

package ancestris.api.editor;

import genj.gedcom.Entity;
import genj.gedcom.Property;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.Lookup;

/**
 *
 * @author daniel
 */
// XXX: write javadoc
public abstract class AncestrisEditor {

    public static AncestrisEditor findEditor(Property property){
        if (property == null)
            return NoOpEditor.instance;
        for (AncestrisEditor editor: Lookup.getDefault().lookupAll(AncestrisEditor.class)){
            if (editor.canEdit(property))
                return editor;
        }
        return NoOpEditor.instance;
    }
    public abstract boolean canEdit(Property property);
    public abstract boolean edit(Property property, boolean isNew);
    public boolean edit(Property property){
        return edit(property, false);
    }

    // Actions
    public abstract AbstractAction getCreateSpouseAction(Property indi);
    public abstract AbstractAction getCreateParentAction(Property child, int sex);
    public abstract AbstractAction getCreateChildAction(Property indi);

    /**
     * This editor does nothing. It is created to avoid many check against null by findEditor
     */
    private static class NoOpEditor extends AncestrisEditor{

        private static AbstractAction NOOP = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // NoOp
            }
        };

        public static final AncestrisEditor instance = new NoOpEditor();

        @Override
        public boolean canEdit(Property property) {
            return true;
        }

        @Override
        public boolean edit(Property property, boolean isNew) {
            return false;
        }

        @Override
        public AbstractAction getCreateSpouseAction(Property indi) {
            return NOOP;
        }

        @Override
        public AbstractAction getCreateParentAction(Property child, int sex) {
            return NOOP;
        }

        @Override
        public AbstractAction getCreateChildAction(Property indi) {
            return NOOP;
        }

    }
}
