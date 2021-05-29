package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.api.editor.AncestrisEditor;
import genj.edit.actions.CreateParent;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class ACreateParent extends AbstractAction {

    private Indi child;
    private int sex;
    private AncestrisEditor editor;

    public ACreateParent(Indi child, int sex, AncestrisEditor editor) {
        super();
        this.child = child;
        this.sex = sex;
        this.editor = editor;
    }

    public void actionPerformed(ActionEvent e) {
        if (child == null) {
            return;
        }
        CreateParent cpAction = new CreateParent(child, sex);
        cpAction.actionPerformed(e);
        Indi parent = (Indi) cpAction.getCreated();
        if (cpAction.isNew()) {
            if (editor.edit(parent, true) == null) {
                child.getGedcom().undoUnitOfWork(false);
            }
        }
    }
}
