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

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import static ancestris.api.editor.Bundle.*;
import javax.swing.ImageIcon;

/**
 *
 * @author daniel
 */
// XXX: write javadoc
// XXX: merge with Editor in core?
public abstract class AncestrisEditor {

    static AbstractAction NOOP = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            // NoOp
        }
    };

    public static AncestrisEditor findEditor(Property property) {
        AncestrisEditor editor = null;
        if (property == null) {
            return NoOpEditor.instance;
        }
        for (AncestrisEditor edt : Lookup.getDefault().lookupAll(AncestrisEditor.class)) {
            if (edt.canEdit(property)) {
                if (edt.isActive()) {
                    return edt;
                }
                editor = edt;
            }
        }
        return editor == null ? NoOpEditor.instance : editor;
    }

    public abstract boolean canEdit(Property property);

    public abstract boolean isActive();

    public abstract boolean edit(Property property, boolean isNew);

    public boolean edit(Property property) {
        return edit(property, false);
    }

    // Actions
    public abstract Action getCreateSpouseAction(Indi indi);

    public abstract Action getCreateParentAction(Indi child, int sex);

    public abstract Action getCreateChildAction(Indi indi);

    /**
     * This editor does nothing. It is created to avoid many check against null by findEditor
     */
    private static class NoOpEditor extends AncestrisEditor {

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
        public Action getCreateSpouseAction(Indi indi) {
            return NOOP;
        }

        @Override
        public Action getCreateParentAction(Indi child, int sex) {
            return NOOP;
        }

        @Override
        public Action getCreateChildAction(Indi indi) {
            return NOOP;
        }

        /**
         * default editor is never active
         *
         * @return always false
         */
        @Override
        public boolean isActive() {
            return false;
        }
    }
@ActionID(category = "Edit",
        id = "ancestris.api.editor.OpenEditorAction")
@ActionRegistration(displayName = "#OpenInEditor.title"
)
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty")})
@Messages("OpenInEditor.title=Edit/Modify")
public final static class OpenEditorAction
        extends AbstractAction
        implements ContextAwareAction {
    
    static ImageIcon editorIcon = new ImageIcon(AncestrisEditor.class.getResource("editor.png")); // NOI18N

    public @Override
    void actionPerformed(ActionEvent e) {
        assert false;
    }

    public @Override
    Action createContextAwareInstance(Lookup context) {
        return new OpenEditor(context.lookup(Entity.class));
    }

    private static final class OpenEditor extends AbstractAncestrisAction {

        Entity entity;

        public OpenEditor(Entity context) {
            this.entity = context;
            setText(OpenInEditor_title());  // NOI18N
            setImage(editorIcon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SelectionDispatcher.muteSelection(true);
            AncestrisEditor editor = AncestrisEditor.findEditor(entity);
            if (editor != null) {
                editor.edit(entity);
            }
            SelectionDispatcher.muteSelection(false);
        }
    }
}
}
