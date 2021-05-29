package ancestris.modules.editors.genealogyeditor.actions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JSeparator;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

@ActionID(
        category = "Edit",
        id = "ancestris.modules.editors.genealogyeditor.actions.Separator"
)
@ActionRegistration(
        lazy = false,
        displayName = ""
)
@ActionReferences({
    //@ActionReference(path = "Menu/File", position = 3333),
    @ActionReference(path = "Toolbars/GenealogyEditor", position = 150)
})
@Messages("CTL_Separator=Separator")
public final class Separator extends AbstractAction implements Presenter.Toolbar {

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO implement action body
    }

    @Override
    public Component getToolbarPresenter() {
        JSeparator separator = new JSeparator();
        separator.setOrientation(JSeparator.VERTICAL);
        separator.setMaximumSize(new Dimension(3,32));
        return separator;
    }
}
