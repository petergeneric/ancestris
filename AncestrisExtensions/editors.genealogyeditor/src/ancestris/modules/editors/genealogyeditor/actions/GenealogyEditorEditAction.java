package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.panels.*;
import ancestris.util.swing.DialogManager;
import genj.gedcom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
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
        DialogManager.ADialog editorDialog;
        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Entity entity = context.getEntity();
            if (entity instanceof Indi) {
                IndividualEditorPanel individualEditorPanel = new IndividualEditorPanel();
                individualEditorPanel.set((Indi) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(IndividualEditorPanel.class, "IndividualEditorPanel.title"),
                        individualEditorPanel);
                editorDialog.setDialogId(IndividualEditorPanel.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    individualEditorPanel.commit();
                }
            } else if (entity instanceof Fam) {
                FamilyEditorPanel familyEditorPanel = new FamilyEditorPanel();
                familyEditorPanel.set((Fam) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(FamilyEditorPanel.class, "FamilyEditorPanel.title"),
                        familyEditorPanel);
                editorDialog.setDialogId(FamilyEditorPanel.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    familyEditorPanel.commit();
                }
            } else if (entity instanceof Note) {
                NoteEditorPanel noteEditorPanel = new NoteEditorPanel();
                noteEditorPanel.set((Note) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(NoteEditorPanel.class, "NoteEditorPanel.title"),
                        noteEditorPanel);
                editorDialog.setDialogId(NoteEditorPanel.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    noteEditorPanel.commit();
                }
            } else if (entity instanceof Source) {
                SourceEditorPanel sourceEditorPanel = new SourceEditorPanel();
                sourceEditorPanel.setSource((Source) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(SourceEditorPanel.class, "SourceEditorPanel.title"),
                        sourceEditorPanel);
                editorDialog.setDialogId(SourceEditorPanel.class.getName());
                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    sourceEditorPanel.commit();
                }
            } else if (entity instanceof Repository) {
                RepositoryEditorPanel repositoryEditorPanel = new RepositoryEditorPanel();
                repositoryEditorPanel.setRepository((Repository) entity);

                editorDialog = new DialogManager.ADialog(
                        NbBundle.getMessage(RepositoryEditorPanel.class, "RepositoryEditorPanel.title"),
                        repositoryEditorPanel);
                editorDialog.setDialogId(SourceEditorPanel.class.getName());

                if (editorDialog.show() == DialogDescriptor.OK_OPTION) {
                    repositoryEditorPanel.commit();
                }
            }
        }
    }
}
