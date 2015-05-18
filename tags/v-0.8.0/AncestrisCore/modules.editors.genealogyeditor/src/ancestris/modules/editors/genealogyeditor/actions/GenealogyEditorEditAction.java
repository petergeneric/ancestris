package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.modules.editors.genealogyeditor.editors.MultiMediaObjectEditor;
import ancestris.modules.editors.genealogyeditor.editors.NoteEditor;
import ancestris.modules.editors.genealogyeditor.editors.RepositoryEditor;
import ancestris.modules.editors.genealogyeditor.editors.SourceEditor;
import ancestris.modules.editors.genealogyeditor.editors.SubmitterEditor;
import genj.gedcom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

@ActionID(category = "Edit",
        id = "ancestris.modules.editors.genealogyeditor.GenealogyEditorAction")
@ActionRegistration(iconBase = "ancestris/modules/editors/genealogyeditor/resources/edit.png",
        displayName = "#CTL_IndividualEditorAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/GenealogyEditor", position = 100)
})
@Messages("CTL_IndividualEditorAction=Edit current individual")
public final class GenealogyEditorEditAction implements ActionListener {

    private final DataObject context;

    public GenealogyEditorEditAction(DataObject context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Context context;
        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Entity entity = context.getEntity();
            Gedcom gedcom = entity.getGedcom();

            if (entity instanceof Indi) {
                IndividualEditor individualEditor = new IndividualEditor();
                individualEditor.setContext(context);

                individualEditor.showPanel();
            } else if (entity instanceof Fam) {
                FamilyEditor familyEditor = new FamilyEditor();
                familyEditor.setContext(context);

                familyEditor.showPanel();
            } else if (entity instanceof Note) {
                NoteEditor noteEditor = new NoteEditor();
                noteEditor.setContext(context);
                noteEditor.showPanel();
            } else if (entity instanceof Media) {
                MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor();
                multiMediaObjectEditor.setContext(context);
                multiMediaObjectEditor.showPanel();
            } else if (entity instanceof Source) {
                SourceEditor sourceEditor = new SourceEditor();
                sourceEditor.setContext(context);
                sourceEditor.showPanel();
            } else if (entity instanceof Repository) {
                RepositoryEditor repositoryEditor = new RepositoryEditor();
                repositoryEditor.setContext(context);
                repositoryEditor.showPanel();
            } else if (entity instanceof Submitter) {
                SubmitterEditor submitterEditor = new SubmitterEditor();
                submitterEditor.setContext(context);
                submitterEditor.showPanel();
            }
        }
    }
}
