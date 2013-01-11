package ancestris.modules.familygroups;

import ancestris.modules.document.view.DocumentViewTopComponent;
import genj.fo.Document;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

@ActionID(id = "ancestris.modules.familygroups.OpenFamilyGroupsAction", category = "Tools")
@ActionRegistration(iconInMenu = true, displayName = "#CTL_OpenFamilyGroups", iconBase = "ancestris/modules/familygroups/FamilyGroups.png")
@ActionReference(path = "Menu/Tools", name = "ancestris-modules-familygroups-OpenFamilyGroups", position = 82)
public final class OpenFamilyGroupsAction implements ActionListener {

    private DocumentViewTopComponent window = null;
    private Context context;
    Preferences modulePreferences = NbPreferences.forModule(FamilyGroupsPlugin.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        
        window = DocumentViewTopComponent.findInstance();
        context = Utilities.actionsGlobalContext().lookup(Context.class);
        if (context != null) {
            if (modulePreferences.getInt("minGroupSize", -1) == -1) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(FamilyGroupsPlugin.class, "OpenFamilyGroupsAction.setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);

                OptionsDisplayer.getDefault().open("Extensions/FamilyGroups");
            } else {
                Document doc = new FamilyGroupsPlugin().start(context.getGedcom());
                if (doc != null) {
                    window.displayDocument(doc, modulePreferences);
                    window.open();
                    window.requestActive();
                }
            }
        }
    }
}
