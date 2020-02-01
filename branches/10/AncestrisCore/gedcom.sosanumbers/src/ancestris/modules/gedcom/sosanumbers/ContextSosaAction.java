package ancestris.modules.gedcom.sosanumbers;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.actions.CommonActions;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique, frédéric
 */
@ActionID(category = "Tree", id = "genj.tree.actions.sosa")
@ActionRegistration(
        displayName = "SosaNumbering",
        iconInMenu = true,
        lazy = false
)
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty", position = 540)})
public class ContextSosaAction extends AbstractAncestrisAction implements Constants, ContextAwareAction {

    private Context context = null;

    public ContextSosaAction() {
        super();
        setImage("ancestris/modules/gedcom/sosanumbers/SosaNumbersIcon.png");
        setText(NbBundle.getMessage(getClass(), "ContextSosaAction", ""));
        setTip(NbBundle.getMessage(getClass(), "ContextSosaAction.tip"));
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        GenerateSosaAction.runSosaAction(context);
    }

    @Override
    public Action createContextAwareInstance(org.openide.util.Lookup context) {
        Entity e = context.lookup(Entity.class);
        if (e == null || !(e instanceof Indi)) {
            return CommonActions.NOOP;
        } else {
            this.context = new Context(e);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            setText(NbBundle.getMessage(getClass(), "ContextSosaAction", ((Indi)e).toString(true)));
            setTip(NbBundle.getMessage(getClass(), "ContextSosaAction.tip"));
            return this;
        }
    }
    
}
