package ancestris.modules.editors.standard.actions;

import ancestris.modules.editors.standard.FamilyPanel;
import genj.edit.actions.CreateSpouse;
import genj.gedcom.Indi;
import genj.view.SelectionSink;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class ACreateSpouse extends AbstractAction {

    private Indi other;
    FamilyPanel editor;

    public ACreateSpouse(Indi indi, FamilyPanel editor) {
        super();
        this.editor = editor;
        other = indi;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            SelectionSink.Dispatcher.muteSelection(true);
            if (other == null) {
                return;
            }
            CreateSpouse csAction = new CreateSpouse(other);
            csAction.actionPerformed(e);
            Indi indi = (Indi) csAction.getCreated();
            if (csAction.isNew()) {
                if (!editor.editEntity(indi, true)) {
                    other.getGedcom().undoUnitOfWork(false);
                }
            }
        } finally {
            SelectionSink.Dispatcher.muteSelection(false);
        }
    }
}
