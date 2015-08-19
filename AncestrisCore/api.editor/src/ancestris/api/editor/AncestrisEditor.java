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
import ancestris.core.actions.CommonActions;
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

    /**
     * return null if no editor can be found.
     * @param property
     * @return 
     */
    public static AncestrisEditor findEditor(Property property) {
        AncestrisEditor editor = null;
        if (property == null) {
            return null;
        }
        for (AncestrisEditor edt : Lookup.getDefault().lookupAll(AncestrisEditor.class)) {
            if (edt.canEdit(property)) {
                if (edt.isActive()) {
                    return edt;
                }
                editor = edt;
            }
        }
        return editor;
    }

    public abstract boolean canEdit(Property property);

    public abstract boolean isActive();

    /**
     * Open editor panel as in {@link #edit(genj.gedcom.Property) } but 
     * isNew parameter is used to display some meaningfull message if property
     * has been crated.
     * @param property
     * @param isNew
     * @return The property updated
     * @deprecated use {@link #edit(genj.gedcom.Property)} or {@link #add(genj.gedcom.Property) }
     */
    @Deprecated
    // We could add som setTitle method instead
    public abstract Property edit(Property property, boolean isNew);

//    public abstract Property edit(Property property, Property parent);
    public abstract Property add(Property parent);
//    public abstract Property edit(Property property);
    public Property edit(Property property) {
        return edit(property, false);
    }

    /**
     * This editor does nothing. It is created to avoid many check against null by findEditor
     */
    private static class NoOpEditor extends AncestrisEditor {

        public static final AncestrisEditor instance = new NoOpEditor();

        @Override
        public boolean canEdit(Property property) {
            return false;
        }

        @Override
        public Property edit(Property property, boolean isNew) {
            return null;
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

        @Override
        public Property add(Property parent) {
            return null;
        }
    }
@ActionID(category = "Edit",
        id = "ancestris.api.editor.OpenEditorAction")
@ActionRegistration(
        displayName = "#OpenInEditor.title",
        lazy=false
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
        Action action = CommonActions.NOOP;
        Property property = context.lookup(Property.class);
        AncestrisEditor editor = AncestrisEditor.findEditor(property);

        if (editor != null){
            action = new OpenEditor(property, editor);
        }
        return action;
    }

    private static final class OpenEditor extends AbstractAncestrisAction {

        private final Property property;
        private final AncestrisEditor editor;
        
        public OpenEditor(Property context, AncestrisEditor editor) {
            this.property = context;
            this.editor = editor;
            setText(OpenInEditor_title());  // NOI18N
            setImage(editorIcon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SelectionDispatcher.muteSelection(true);
            if (editor != null) {
                editor.edit(property);
            }
            SelectionDispatcher.muteSelection(false);
        }
    }
}
}
