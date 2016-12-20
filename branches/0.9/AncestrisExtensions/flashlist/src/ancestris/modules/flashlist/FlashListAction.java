package ancestris.modules.flashlist;

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.modules.document.view.FopDocumentView;
import static ancestris.modules.flashlist.Bundle.*;
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

@ActionID(id = "ancestris.modules.flashlist.FlashListAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_FlashListAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/View/Reports", name = "CommonAncestorOpenAction", position = 10)
@NbBundle.Messages({
    "title={0}: Flash Lists",
    "title.short=Flash Lists",
})
public final class FlashListAction extends AbstractAncestrisContextAction {

    public FlashListAction() {
        super();
        setImage("ancestris/modules/flashlist/ReportFO.png");
        setText(NbBundle.getMessage(FlashListAction.class, "CTL_FlashListAction"));
    }
    
    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        Preferences modulePreferences = NbPreferences.forModule(ReportFlashList.class);

        Context contextToOpen = getContext();
        if (contextToOpen != null) {
            if (modulePreferences.getInt("displayLegendComboBox", 3) == 3) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);

                OptionsDisplayer.getDefault().open("Extensions/FlashList");
            } else {
                Document doc = new ReportFlashList().start(contextToOpen.getGedcom(), modulePreferences.get("reportFilename", "flash-list"));
                if (doc != null) {
                    FopDocumentView window = new FopDocumentView(contextToOpen, title_short(),title(contextToOpen.getGedcom().getName()));
                    window.displayDocument(doc, modulePreferences);
                }
            }
        }
    }
}
