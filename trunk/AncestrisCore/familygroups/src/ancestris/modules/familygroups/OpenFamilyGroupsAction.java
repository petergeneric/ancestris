package ancestris.modules.familygroups;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.modules.document.view.FopDocumentView;
import static ancestris.modules.familygroups.Bundle.*;
import ancestris.util.ProgressListener;
import genj.fo.Document;
import genj.gedcom.Context;
import genj.view.Images;
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
import spin.Spin;

@ActionID(id = "ancestris.modules.familygroups.OpenFamilyGroupsAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_FamilyGroupsAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Tools/Reports", name = "OpenFamilyGroupsAction", position = 200)
@NbBundle.Messages({"# {0} - Name",
    "title={0}: Family Groups",
    "title.tip=Report of the Family Groups for genealogy {0}"})
public final class OpenFamilyGroupsAction extends AbstractAncestrisContextAction {

    private Preferences modulePreferences = NbPreferences.forModule(FamilyGroupsPlugin.class);

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
                OptionsDisplayer.getDefault().open("Extensions/FamilyGroups", true);
            }
            run();
        }
    }
    
    private void run() {
        Context contextToOpen = getContext();
        FamilyGroupsRunner fgrunner = (FamilyGroupsRunner) Spin.off(new FamilyGroupsPlugin(contextToOpen.getGedcom()));
        ProgressListener.Dispatcher.processStarted(fgrunner);
        fgrunner.run();
        ProgressListener.Dispatcher.processStopped(fgrunner);

        FamilyGroupsPlugin fgp = fgrunner.getFgp();
        Document doc = fgp.getDocument();
        if (doc != null) {
            String gen = contextToOpen.getGedcom().getDisplayName();
            FopDocumentView window = new FopDocumentView(contextToOpen, title(gen), title_tip(gen), 
                    new AbstractAncestrisAction[]{ 
                        runAction(),
                        fgp.getExtractAction(contextToOpen.getGedcom()), 
                        fgp.getMarkAction(contextToOpen.getGedcom()), 
                        optionAction() 
                    });
            window.executeOnClose(new Runnable() {
                @Override
                public void run() {
                    fgp.stop();
                }
            });
            window.displayDocument(doc, modulePreferences);
        }
    }

    private AbstractAncestrisAction runAction() {
        AbstractAncestrisAction option = new AbstractAncestrisAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        };
        option.setImage("ancestris/modules/familygroups/Start.png");
        option.setTip(NbBundle.getMessage(FamilyGroupsPlugin.class, "OpenFamilyGroupsAction.rerun"));
        
    return option;
    }

    private AbstractAncestrisAction optionAction() {
        AbstractAncestrisAction option = new AbstractAncestrisAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsDisplayer.getDefault().open("Extensions/FamilyGroups", true);
            }
        };
        option.setImage(Images.imgSettings);
        option.setTip(NbBundle.getMessage(FamilyGroupsPlugin.class, "OpenFamilyGroupsAction.parameters"));
        
    return option;
    }
}
