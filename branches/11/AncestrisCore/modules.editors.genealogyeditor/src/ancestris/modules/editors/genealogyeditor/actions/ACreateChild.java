package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.api.editor.AncestrisEditor;
import ancestris.view.SelectionDispatcher;
import genj.edit.actions.CreateChild;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class ACreateChild extends AbstractAction {

    private Indi parent;
    private Fam famc;
    private int sex;
    private AncestrisEditor editor;

    public ACreateChild(Indi parent, AncestrisEditor editor) {
        super();
        this.parent = parent;
        this.famc = null;
        this.editor = editor;
    }

    public ACreateChild(Fam famc, AncestrisEditor editor) {
        super();
        this.parent = null;
        this.famc = famc;
        this.editor = editor;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            SelectionDispatcher.muteSelection(true);
            if (parent == null && famc == null) {
                return;
            }
            Gedcom gedcom;
            CreateChild ccAction;
            // tries to guess entity to attach new child to
            // Familly knows?
            if (famc != null) {
                gedcom = famc.getGedcom();
                ccAction = new CreateChild(famc, PropertySex.MALE);
                ccAction.actionPerformed(e);
            } else if (parent != null) {
                gedcom = parent.getGedcom();
                ccAction = new CreateChild(parent, PropertySex.MALE);
                ccAction.actionPerformed(e);
            } else {
                return;
            }
            Indi indi = (Indi) ccAction.getCreated();
            if (ccAction.isNew()) {
                if (editor.edit(indi, true)==null) {
                    if (gedcom != null) {
                        gedcom.undoUnitOfWork(false);
                    }
                }
            }
        } finally {
            SelectionDispatcher.muteSelection(false);
        }
    }
}
