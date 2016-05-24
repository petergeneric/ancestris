package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.api.editor.AncestrisEditor;
import ancestris.modules.editors.genealogyeditor.EditorTopComponent;
import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.modules.editors.genealogyeditor.editors.MultiMediaObjectEditor;
import ancestris.modules.editors.genealogyeditor.editors.NoteEditor;
import ancestris.modules.editors.genealogyeditor.editors.RepositoryEditor;
import ancestris.modules.editors.genealogyeditor.editors.SourceEditor;
import ancestris.modules.editors.genealogyeditor.editors.SubmitterEditor;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.gedcom.Submitter;
import javax.swing.Action;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author dominique
 */
@ServiceProvider(service = AncestrisEditor.class, position = 200)
public class GenealogyEditorAction extends AncestrisEditor {

    @Override
    public boolean canEdit(Property property) {
        return (property instanceof Indi
                || property instanceof Fam
                || property instanceof Note
                || property instanceof Media
                || property instanceof Source
                || property instanceof Submitter
                || property instanceof Repository);
    }

    @Override
    public boolean isActive() {
        return false;
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
            if (!(property instanceof Entity))  {
                property = property.getEntity();
            }
            if (property instanceof Indi) {
                IndividualEditor individualEditor = new IndividualEditor(isNew);
                individualEditor.setContext(contextToOpen);
                individualEditor.showPanel();
                return property;
            } else if (property instanceof Fam) {
                FamilyEditor familyEditor = new FamilyEditor(isNew);
                familyEditor.setContext(contextToOpen);
               familyEditor.showPanel();
                return property;
            } else if (property instanceof Note) {
                NoteEditor noteEditor = new NoteEditor(isNew);
                noteEditor.setContext(contextToOpen);
                noteEditor.showPanel();
                return property;
            } else if (property instanceof Media) {
                MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor(isNew);
                multiMediaObjectEditor.setContext(contextToOpen);
                multiMediaObjectEditor.showPanel();
                return property;
            } else if (property instanceof Source) {
                SourceEditor sourceEditor = new SourceEditor(isNew);
                sourceEditor.setContext(contextToOpen);
                sourceEditor.showPanel();
                return property;
            } else if (property instanceof Repository) {
                RepositoryEditor repositoryEditor = new RepositoryEditor(isNew);
                repositoryEditor.setContext(contextToOpen);
                repositoryEditor.showPanel();
                return property;
            } else if (property instanceof Submitter) {
                SubmitterEditor submitterEditor = new SubmitterEditor(isNew);
                submitterEditor.setContext(contextToOpen);
                submitterEditor.showPanel();
                return property;
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public Property add(Property parent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public String getName(boolean canonical) {
        if (canonical) {
            return getClass().getCanonicalName();
        } else {
            return NbBundle.getMessage(EditorTopComponent.class, "OpenIDE-Module-Name");
        }
    }

    @Override
    public String toString() {
        return getName(false);
    }

    @Override
    public Action getCreateParentAction(Indi indi, int sex) {
        return AActions.alwaysEnabled(
                new ACreateParent(indi, sex, this),
                "",
                org.openide.util.NbBundle.getMessage(GenealogyEditorAction.class, "action.createparent.title"),
                "ancestris/modules/editors/genealogyeditor/resources/indi_add.png", // NOI18N
                true);
    }

    @Override
    public Action getCreateSpouseAction(Indi indi) {
        return AActions.alwaysEnabled(
                new ACreateSpouse(indi, this),
                "",
                org.openide.util.NbBundle.getMessage(GenealogyEditorAction.class, "action.addspouse.title"),
                "ancestris/modules/editors/standard/images/indi_add.png", // NOI18N
                true);
    }

}
