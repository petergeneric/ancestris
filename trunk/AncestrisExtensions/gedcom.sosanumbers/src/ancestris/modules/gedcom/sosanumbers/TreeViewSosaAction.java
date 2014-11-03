package ancestris.modules.gedcom.sosanumbers;

import ancestris.core.actions.CommonActions;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.util.Registry;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author dominique
 */
@ActionID(category = "Tree", id = "genj.tree.actions.sosa")
@ActionRegistration(
        displayName = "SosaNumbering",
        lazy = false
)
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty/Tools", position = 1010)})
public class TreeViewSosaAction
        extends AbstractAction
        implements ContextAwareAction {

    private final Preferences modulePreferences = NbPreferences.forModule(SosaNumbers.class);
    Gedcom myGedcom = null;
    Indi indiDeCujus = null;

    @Override
    public void actionPerformed(ActionEvent ae) {
        Registry registry = myGedcom.getRegistry();
        registry.put("INDI.decujus.id", indiDeCujus.getId());
        new SosaNumbers().generateSosaNbs(myGedcom, indiDeCujus);
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GenerateSosaAction.class, "GenerateSosaAction.done", indiDeCujus.getName()), NotifyDescriptor.INFORMATION_MESSAGE));
    }

    @Override
    public Action createContextAwareInstance(org.openide.util.Lookup context) {
        Entity e = context.lookup(Entity.class);
        if (e == null || !(e instanceof Indi)) {
            return CommonActions.NOOP;
        } else {
            indiDeCujus = (Indi) e;
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            putValue(Action.NAME, NbBundle.getMessage(this.getClass(), "ContextSosaAction", indiDeCujus.getName()));
            putValue(Action.SMALL_ICON, new ImageIcon("ancestris/modules/gedcom/sosanumbers/SosaNumbersIcon.png"));
            myGedcom = e.getGedcom();
            return this;
        }
    }
}
