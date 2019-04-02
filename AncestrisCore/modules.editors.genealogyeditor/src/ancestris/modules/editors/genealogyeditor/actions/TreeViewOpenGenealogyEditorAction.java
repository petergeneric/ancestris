package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import static ancestris.modules.editors.genealogyeditor.actions.Bundle.*;
import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
//@ActionID(category = "Edit", id = "ancestris.modules.editors.genealogyeditor")
//@ActionRegistration(displayName = "#OpenInEditor.title")
//@ActionReferences({
//    @ActionReference(path = "Ancestris/Actions/GedcomProperty")})
@NbBundle.Messages("OpenInEditor.title=Edit using the default Editor")
public class TreeViewOpenGenealogyEditorAction extends AbstractAction implements ContextAwareAction {

    private Gedcom gedcom = null;
    private Entity entity = null;

    @Override
    public void actionPerformed(ActionEvent ae) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(org.openide.util.Lookup context) {
        return new OpenEditor(context.lookup(Entity.class));
    }

    private static final class OpenEditor extends AbstractAncestrisAction {

        Entity entity;

        public OpenEditor(Entity context) {
            this.entity = context;
            setText(OpenInEditor_title());  // NOI18N
            setImage(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SelectionDispatcher.muteSelection(true);
            if (entity != null) {
                Gedcom gedcom = entity.getGedcom();
                int undoNb = gedcom.getUndoNb();

                final AriesTopComponent atc = AriesTopComponent.findEditorWindow(gedcom);
                if (atc == null) {
                    AncestrisTopComponent win = new AriesTopComponent().create(new Context(entity));
                    win.open();
                    win.requestActive();
                } else {
                    if (entity instanceof Indi) {
                        IndividualEditor individualEditor = new IndividualEditor();
                        individualEditor.setContext(new Context(entity));
                        if (!individualEditor.showPanel()) {
                            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                                gedcom.undoUnitOfWork(false);
                            }
                        }
                    } else if (entity instanceof Fam) {
                        FamilyEditor familyEditor = new FamilyEditor();
                        familyEditor.setContext(new Context(entity));

                        if (!familyEditor.showPanel()) {
                            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                                gedcom.undoUnitOfWork(false);
                            }
                        }
                    }
                }

                SelectionDispatcher.muteSelection(false);
            }
        }
    }
}
