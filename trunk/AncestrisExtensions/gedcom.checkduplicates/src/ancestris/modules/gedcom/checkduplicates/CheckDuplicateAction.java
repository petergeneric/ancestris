package ancestris.modules.gedcom.checkduplicates;

import ancestris.app.App;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

@ActionID(id = "ancestris.modules.gedcom.checkduplicates.CheckDuplicateAction", category = "Tools")
@ActionRegistration(iconInMenu = true, displayName = "#CTL_CheckDuplicateAction")
@ActionReference(path = "Menu/Tools/Gedcom")
public final class CheckDuplicateAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Context context;

        if ((context = App.center.getSelectedContext(true)) != null) {
            Gedcom myGedcom = context.getGedcom();
            Thread t = new Thread(new CheckDuplicates(myGedcom, myGedcom));
            t.start();
        }
    }
}
