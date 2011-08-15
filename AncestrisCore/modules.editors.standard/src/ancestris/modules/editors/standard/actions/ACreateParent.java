package ancestris.modules.editors.standard.actions;

import ancestris.modules.editors.standard.EntityEditor;
import genj.edit.actions.CreateParent;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class ACreateParent extends AbstractAction {

    private Indi child;
    private int sex;

    public ACreateParent(Indi child, int sex) {
        super();
        this.child = child;
        this.sex = sex;
    }

    public void actionPerformed(ActionEvent e) {
        if (child == null) {
            return;
        }
        CreateParent cpAction = new CreateParent(child, sex);
        cpAction.actionPerformed(e);
        Indi parent = (Indi) cpAction.getCreated();
        if (cpAction.isNew()) {
            if (!EntityEditor.editEntity(parent, true)) {
                child.getGedcom().undoUnitOfWork(false);
            }
        }
    }
}
