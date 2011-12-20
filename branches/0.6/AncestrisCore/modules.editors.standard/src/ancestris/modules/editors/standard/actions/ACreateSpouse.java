package ancestris.modules.editors.standard.actions;

import ancestris.api.editor.AncestrisEditor;
import genj.edit.actions.CreateSpouse;
import genj.gedcom.Indi;
import genj.view.SelectionSink;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class ACreateSpouse extends AbstractAction {

    private Indi other;
    private AncestrisEditor editor;

    public ACreateSpouse(Indi indi, AncestrisEditor editor) {
        super();
        other = indi;
        this.editor = editor;
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
                if (!editor.edit(indi, true)) {
                    other.getGedcom().undoUnitOfWork(false);
                }
            }
        } finally {
            SelectionSink.Dispatcher.muteSelection(false);
        }
    }
}
