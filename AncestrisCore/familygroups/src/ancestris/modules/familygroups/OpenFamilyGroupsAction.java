package ancestris.modules.familygroups;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.modules.document.view.FopDocumentView;
import static ancestris.modules.familygroups.Bundle.*;
import genj.fo.Document;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

@ActionID(id = "ancestris.modules.familygroups.OpenFamilyGroupsAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_FamilyGroupsAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Tools/Reports", name = "OpenFamilyGroupsAction", position = 200)
@NbBundle.Messages({"# {0} - Name",
        "title={0}: Family Groups",
        "title.short=Family Groups"})
public final class OpenFamilyGroupsAction  extends AbstractAncestrisContextAction {

    Preferences modulePreferences = NbPreferences.forModule(FamilyGroupsPlugin.class);

    public OpenFamilyGroupsAction() {
        super();
        setImage("ancestris/modules/familygroups/FamilyGroups.png");
        setText(NbBundle.getMessage(OpenFamilyGroupsAction.class, "CTL_FamilyGroupsAction"));
    }
    
    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }


    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        Context contextToOpen = getContext();
        if (contextToOpen != null) {
            if (modulePreferences.getInt("minGroupSize", -1) == -1) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(FamilyGroupsPlugin.class, "OpenFamilyGroupsAction.setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);

                OptionsDisplayer.getDefault().open("Extensions/FamilyGroups");
            } else {
                final FamilyGroupsPlugin fgp = new FamilyGroupsPlugin();
                Document doc = fgp.start(contextToOpen.getGedcom());
                if (doc != null) {
                    FopDocumentView window = new FopDocumentView(contextToOpen, title_short(),title(contextToOpen.getGedcom().getName()), 
                            new AbstractAncestrisAction[]{ fgp.getAction(contextToOpen.getGedcom()) });
                    window.executeOnClose(new Runnable() {
                        @Override
                        public void run() {
                            fgp.stop();
                        }
                    });
                    window.displayDocument(doc, modulePreferences);
                }
            }
        }
    }
}
