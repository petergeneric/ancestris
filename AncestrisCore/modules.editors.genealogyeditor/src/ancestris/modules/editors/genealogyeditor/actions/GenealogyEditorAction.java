package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.api.editor.AncestrisEditor;
import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.modules.editors.genealogyeditor.editors.MultiMediaObjectEditor;
import ancestris.modules.editors.genealogyeditor.editors.NoteEditor;
import ancestris.modules.editors.genealogyeditor.editors.RepositoryEditor;
import ancestris.modules.editors.genealogyeditor.editors.SourceEditor;
import ancestris.modules.editors.genealogyeditor.editors.SubmitterEditor;
import genj.gedcom.Context;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.gedcom.Submitter;
import javax.swing.Action;
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
    public boolean edit(Property property, boolean isNew) {
        Context context;
        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {

            if (property instanceof Indi) {
                IndividualEditor individualEditor = new IndividualEditor(isNew);
                individualEditor.setContext(context);
                individualEditor.showPanel();
                return true;
            } else if (property instanceof Fam) {
                FamilyEditor familyEditor = new FamilyEditor(isNew);
                familyEditor.setContext(context);
               familyEditor.showPanel();
                return true;
            } else if (property instanceof Note) {
                NoteEditor noteEditor = new NoteEditor(isNew);
                noteEditor.setContext(context);
                noteEditor.showPanel();
                return true;
            } else if (property instanceof Media) {
                MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor(isNew);
                multiMediaObjectEditor.setContext(context);
                multiMediaObjectEditor.showPanel();
                return true;
            } else if (property instanceof Source) {
                SourceEditor sourceEditor = new SourceEditor(isNew);
                sourceEditor.setContext(context);
                sourceEditor.showPanel();
                return true;
            } else if (property instanceof Repository) {
                RepositoryEditor repositoryEditor = new RepositoryEditor(isNew);
                repositoryEditor.setContext(context);
                repositoryEditor.showPanel();
                return true;
            } else if (property instanceof Submitter) {
                SubmitterEditor submitterEditor = new SubmitterEditor(isNew);
                submitterEditor.setContext(context);
                submitterEditor.showPanel();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public Action getCreateParentAction(Indi child, int sex) {
        return AActions.alwaysEnabled(
                new ACreateParent(child, sex, this),
                "",
                org.openide.util.NbBundle.getMessage(GenealogyEditorAction.class, "action.createparent.title"),
                "ancestris/modules/editors/standard/images/add-child.png", // NOI18N
                true);
    }

    @Override
    public Action getCreateChildAction(Indi indi) {
        return AActions.alwaysEnabled(
                new ACreateChild(indi, this),
                "",
                org.openide.util.NbBundle.getMessage(GenealogyEditorAction.class, "action.createchild.title", indi),
                "ancestris/modules/editors/standard/images/add-child.png", // NOI18N
                true);
    }

    @Override
    public Action getCreateSpouseAction(Indi indi) {
        return AActions.alwaysEnabled(
                new ACreateSpouse(indi, this),
                "",
                org.openide.util.NbBundle.getMessage(GenealogyEditorAction.class, "action.addspouse.title"),
                "ancestris/modules/editors/standard/images/add-spouse.png", // NOI18N
                true);
    }
}
