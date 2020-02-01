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

import ancestris.api.editor.AncestrisEditor;
import ancestris.api.editor.Editor;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.editors.standard.CygnusTopComponent;
import ancestris.modules.editors.standard.tools.IndiCreator;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.gedcom.Submitter;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author frederic
 */
@ServiceProvider(service = AncestrisEditor.class, position = 195)
public class EditorAction extends AncestrisEditor {
    
    private ImageIcon editorIcon = new ImageIcon(CygnusTopComponent.class.getResource("editeur_standard.png")); // NOI18N

    @Override
    public boolean canEdit(Property property) {
        return (property instanceof Indi
                || property instanceof Fam
                || property instanceof Note
                || property instanceof Media
                || property instanceof Source
                || property instanceof Repository
                || property instanceof Submitter
                || property != null);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public Property edit(Property property, boolean isNew) {
        Context contextToOpen;
        if (property == null) {
            contextToOpen = Utilities.actionsGlobalContext().lookup(Context.class);
        } else {
            contextToOpen = new Context(property);
        }
        if (contextToOpen != null) {
            CygnusTopComponent editorTopComponent = getCurrentEditorTopComponent(contextToOpen);
            if (editorTopComponent != null) {
                SelectionDispatcher.fireSelection(contextToOpen);
                editorTopComponent.requestActive();
            } else {
                AncestrisTopComponent win = new CygnusTopComponent().create(contextToOpen);
                win.open();
                win.requestActive();
            }
        }
        return null;
    }

    @Override
    public Property add(Property parent) {
        return null;
    }

    public String getName(boolean canonical) {
        if (canonical) {
            return getClass().getCanonicalName();
        } else {
            return NbBundle.getMessage(CygnusTopComponent.class, "OpenIDE-Module-Name");
        }
    }
    
    @Override
    public ImageIcon getIcon() {
        return editorIcon;
    }

    @Override
    public String toString() {
        return getName(false);
    }

    @Override
    public Action getCreateParentAction(Indi indi, int sex) {
        CygnusTopComponent editorTopComponent = getCurrentEditorTopComponent(new Context(indi));
        if (editorTopComponent != null) {
            return new ActionCreation(
                        editorTopComponent, 
                        IndiCreator.CREATION, 
                        sex == PropertySex.MALE ? IndiCreator.REL_FATHER : IndiCreator.REL_MOTHER, indi);
        }
        return getDefaultAction(indi);
    }

    @Override
    public Action getCreateSpouseAction(Indi indi) {
        CygnusTopComponent editorTopComponent = getCurrentEditorTopComponent(new Context(indi));

        Fam fam = null;
        if (editorTopComponent != null) {
            Fam[] fams = indi.getFamiliesWhereSpouse(true);
            for (Fam f : fams) {
                if (f.getOtherSpouse(indi) == null) {
                    fam = f;
                    break;
                }
            }
            
            return new ActionCreation( 
                        editorTopComponent, 
                        IndiCreator.CREATION, 
                        IndiCreator.REL_PARTNER, indi, fam);
                
        }
        return getDefaultAction(indi);
    }
    
    
    
    

    private CygnusTopComponent getCurrentEditorTopComponent(Context context) {
        CygnusTopComponent ret = null;
        for (CygnusTopComponent editorTopComponent : (List<CygnusTopComponent>) AncestrisPlugin.lookupAll(CygnusTopComponent.class)) {
            Editor editor = editorTopComponent.getEditor();
            if (editor != null) {
                if (editor.getEditedEntity() == context.getEntity())  {
                    return editorTopComponent;
                }
                // In case same Gedcom, remember this editor as backup (no need to re-open another topcomponent)
                if (editor.getEditedEntity().getGedcom() == context.getEntity().getGedcom()) {
                    ret = editorTopComponent;
                }
            }
        }
        return ret;
    }
    
    
}
