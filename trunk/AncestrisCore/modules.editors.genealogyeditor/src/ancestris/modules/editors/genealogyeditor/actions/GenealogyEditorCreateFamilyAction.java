package ancestris.modules.editors.genealogyeditor.actions;

import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.view.AncestrisTopComponent;
import genj.gedcom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

@ActionID(category = "Edit",
        id = "ancestris.modules.editors.genealogyeditor.actions.GenealogyEditorAddFamilyAction")
@ActionRegistration(iconBase = "ancestris/modules/editors/genealogyeditor/resources/family_add.png",
        displayName = "#CTL_GenealogyEditorAddFamilyAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/GenealogyEditor", position = 300)
})
@Messages("CTL_GenealogyEditorAddFamilyAction=Create a Family")
public final class GenealogyEditorCreateFamilyAction implements ActionListener {

    private final DataObject context;
    Entity entity;

    public GenealogyEditorCreateFamilyAction(DataObject context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Context gedcomContext;

        if ((gedcomContext = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Gedcom gedcom = gedcomContext.getGedcom();
            final AriesTopComponent atc = AriesTopComponent.findEditorWindow(gedcom);
            if (atc == null) {
                
                AncestrisTopComponent win = new AriesTopComponent().create(gedcomContext);
                win.open();
                win.requestActive();
            }
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        entity = gedcom.createEntity(Gedcom.FAM);
                    }
                }); // end of doUnitOfWork
                FamilyEditor familyEditor = new FamilyEditor(true);
                familyEditor.setContext(new Context(entity));
                if (!familyEditor.showPanel()) {
                    gedcom.undoUnitOfWork(false);
                }
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
