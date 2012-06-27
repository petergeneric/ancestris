package ancestris.modules.gedcom.checkduplicates;

import ancestris.app.App;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class CheckDuplicateAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Context context;

        if ((context = App.center.getSelectedContext(true)) != null) {
            Gedcom myGedcom = context.getGedcom();
            new CheckDuplicates(myGedcom, myGedcom).run();
        }
    }
}
