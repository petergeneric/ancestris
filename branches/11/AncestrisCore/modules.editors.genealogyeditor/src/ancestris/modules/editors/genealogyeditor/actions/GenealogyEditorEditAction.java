package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.modules.editors.genealogyeditor.editors.MultiMediaObjectEditor;
import ancestris.modules.editors.genealogyeditor.editors.NoteEditor;
import ancestris.modules.editors.genealogyeditor.editors.RepositoryEditor;
import ancestris.modules.editors.genealogyeditor.editors.SourceEditor;
import ancestris.modules.editors.genealogyeditor.editors.SubmitterEditor;
import ancestris.view.AncestrisTopComponent;
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
        Context contextLocal;
        if ((contextLocal = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            //Open docked windows first.
            final AriesTopComponent atc = AriesTopComponent.findEditorWindow(contextLocal.getGedcom());
            if (atc == null) {
                AncestrisTopComponent win = new AriesTopComponent().create(contextLocal);
                win.open();
                win.requestActive();
            } else {
                Entity entity = contextLocal.getEntity();

                if (entity instanceof Indi) {
                    IndividualEditor individualEditor = new IndividualEditor();
                    individualEditor.setContext(contextLocal);

                    individualEditor.showPanel();
                } else if (entity instanceof Fam) {
                    FamilyEditor familyEditor = new FamilyEditor();
                    familyEditor.setContext(contextLocal);

                    familyEditor.showPanel();
                } else if (entity instanceof Note) {
                    NoteEditor noteEditor = new NoteEditor();
                    noteEditor.setContext(contextLocal);
                    noteEditor.showPanel();
                } else if (entity instanceof Media) {
                    MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor();
                    multiMediaObjectEditor.setContext(contextLocal);
                    multiMediaObjectEditor.showPanel();
                } else if (entity instanceof Source) {
                    SourceEditor sourceEditor = new SourceEditor();
                    sourceEditor.setContext(contextLocal);
                    sourceEditor.showPanel();
                } else if (entity instanceof Repository) {
                    RepositoryEditor repositoryEditor = new RepositoryEditor();
                    repositoryEditor.setContext(contextLocal);
                    repositoryEditor.showPanel();
                } else if (entity instanceof Submitter) {
                    SubmitterEditor submitterEditor = new SubmitterEditor();
                    submitterEditor.setContext(contextLocal);
                    submitterEditor.showPanel();
                }
            }
        }
    }
}
