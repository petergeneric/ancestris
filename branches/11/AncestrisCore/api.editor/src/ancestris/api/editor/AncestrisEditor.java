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
import genj.gedcom.GedcomOptions;
import genj.gedcom.Indi;
import genj.gedcom.PropertyPlace;
import java.util.Collection;
import java.util.List;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;

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

    public static List<AncestrisEditor> findEditors() {
        return (List<AncestrisEditor>) Lookup.getDefault().lookupAll(AncestrisEditor.class);
    }

    /**
     * Find the first editor that can edit the property, that has the default editor's name (otherwise backup), and that corresponds to the same opened gedcom
     * 
     * @param property
     * @return null if no editor can be found.
     */
    public static AncestrisEditor findEditor(Property property) {
        AncestrisEditor editor = null;
        AncestrisEditor backupEditor = null;
        if (property == null) {
            return NoOpEditor.instance;
        }
        String canonicalName = GedcomOptions.getInstance().getDefaultEditor();
        for (AncestrisEditor edt : findEditors()) {
            // Return the editor that can edit property && is the default one && is active, or else just the last looped AncestrisEditor
            if (edt.canEdit(property) && edt.getName(true).contains(canonicalName)) {
                if (edt.isActive()) {
                    return edt;
                }
                editor = edt;
            } else if (edt.canEdit(property) && (property instanceof PropertyPlace) && (edt.getName(true).contains("PlaceEditor"))) {
                return edt;
            } else if (edt.canEdit(property)) {
                backupEditor = edt;
            }
        }
        return editor != null ? editor : backupEditor;
    }

    public abstract boolean canEdit(Property property);

    public abstract boolean isActive();

    /**
     * Open editor panel as in {@link #edit(genj.gedcom.Property) } but isNew
     * parameter is used to display some meaningfull message if property has
     * been crated.
     *
     * @param property
     * @param isNew
     * @return The property updated
     * @deprecated use {@link #edit(genj.gedcom.Property)} or {@link #add(genj.gedcom.Property)
     * }
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

    public abstract ImageIcon getIcon();

    public abstract String getName(boolean canonical);

    
    public abstract Action getCreateParentAction(Indi indi, int sex);
    
    public abstract Action getCreateSpouseAction(Indi indi);
    
    public Action getDefaultAction(final Property property) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit(property);
            }
        };
    }

    
    
    /**
     * This editor does nothing. It is created to avoid many check against null
     * by findEditor
     */
    private static class NoOpEditor extends AncestrisEditor {

        public static final AncestrisEditor instance = new NoOpEditor();
        private static ImageIcon editorIcon = new ImageIcon(AncestrisEditor.class.getResource("editor_cygnus.png")); // NOI18N


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

        @Override
        public String getName(boolean canonical) {
            if (canonical) {
                return getClass().getCanonicalName();
            } else {
                return NbBundle.getMessage(AncestrisEditor.class, "OpenIDE-Module-Name");
            }
        }
        
        @Override
        public String toString() {
            return getName(false);
        }

        @Override
        public Action getCreateParentAction(Indi indi, int sex) {
            return getDefaultAction(indi);
        }

        @Override
        public Action getCreateSpouseAction(Indi indi) {
            return getDefaultAction(indi);
        }

        @Override
        public ImageIcon getIcon() {
            return editorIcon; // default
        }

    }
    
    
    
    
    
    
    
    

    @ActionID(category = "Edit",
            id = "ancestris.api.editor.OpenEditorAction")
    @ActionRegistration(
            displayName = "#OpenInEditor.title",
            lazy = false
    )
    @ActionReferences({@ActionReference(path = "Ancestris/Actions/GedcomProperty", position= 660)})
    @Messages("OpenInEditor.title=Edit/Modify")
    public final static class OpenEditorAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            Collection<? extends Property> props = context.lookupAll(Property.class);
            if (props == null || props.isEmpty() || props.size() > 1) {
                return CommonActions.NOOP;
            }

            Action action = CommonActions.NOOP;
            Property property = context.lookup(Property.class);
            AncestrisEditor editor = AncestrisEditor.findEditor(property);

            if (editor != null) {
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
                setTip(NbBundle.getMessage(Bundle.class, "OpenInEditor.tip")); // NOI18N
                setImage(editor.getIcon());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                //SelectionDispatcher.muteSelection(true);
                if (editor != null) {
                    editor.edit(property);
                }
                //SelectionDispatcher.muteSelection(false);
            }
        }
    }
}
