package ancestris.modules.familygroups;

import ancestris.app.App;
import ancestris.modules.document.view.DocumentViewTopComponent;
import genj.fo.Document;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class OpenFamilyGroupsAction implements ActionListener {

    private DocumentViewTopComponent window = null;
    private Context context;
    Preferences modulePreferences = NbPreferences.forModule(FamilyGroups.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        
        window = DocumentViewTopComponent.findInstance();
        context = App.center.getSelectedContext(true);
        if (context != null) {
            if (modulePreferences.getInt("minGroupSize", -1) == -1) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(FamilyGroups.class, "FlashListPanel.setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);

                OptionsDisplayer.getDefault().open("Extensions/FamilyGroups");
            } else {
                Document doc = new FamilyGroups().start(context.getGedcom());
                if (doc != null) {
                    window.displayDocument(doc, modulePreferences);
                    window.open();
                    window.requestActive();
                }
            }
        }
    }
}
