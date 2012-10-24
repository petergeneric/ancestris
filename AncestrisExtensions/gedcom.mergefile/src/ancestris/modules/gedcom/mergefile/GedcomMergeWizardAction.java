package ancestris.modules.gedcom.mergefile;

import ancestris.gedcom.GedcomDirectory;
import ancestris.gedcom.GedcomMgr;
import static ancestris.modules.gedcom.mergefile.Bundle.CTL_GedcomMergeAction;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.util.Origin;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

@ActionID(category = "Tools", id = "ancestris.modules.gedcom.merge.GedcomMergeAction")
@ActionRegistration(iconBase = "ancestris/modules/gedcom/mergefile/arrow_merge_270_left.png",
displayName = "#CTL_GedcomMergeAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools/Gedcom", position = 3333)
})
@NbBundle.Messages({
    "CTL_GedcomMergeAction=Merge Tool",})
public final class GedcomMergeWizardAction implements ActionListener {

    private final static Logger LOG = Logger.getLogger(GedcomMergeWizardAction.class.getName(), null);

    @Override
    public void actionPerformed(ActionEvent e) {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        GedcomMergeWizardPanel1 gedcomMergeWizardPanel1 = new GedcomMergeWizardPanel1();
        GedcomMergeWizardPanel2 gedcomMergeWizardPanel2 = new GedcomMergeWizardPanel2();
        GedcomMergeWizardPanel3 gedcomMergeWizardPanel3 = new GedcomMergeWizardPanel3();
        GedcomMergeWizardPanel4 gedcomMergeWizardPanel4 = new GedcomMergeWizardPanel4(gedcomMergeWizardPanel2.getComponent(), gedcomMergeWizardPanel3.getComponent());
        GedcomMergeWizardPanel5 gedcomMergeWizardPanel5 = new GedcomMergeWizardPanel5();

        panels.add(gedcomMergeWizardPanel1);
        panels.add(gedcomMergeWizardPanel2);
        panels.add(gedcomMergeWizardPanel3);
        panels.add(gedcomMergeWizardPanel4);
        panels.add(gedcomMergeWizardPanel5);
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wizardDescriptor = new WizardDescriptor(new WizardDescriptor.ArrayIterator<WizardDescriptor>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(CTL_GedcomMergeAction());
        if (DialogDisplayer.getDefault().notify(wizardDescriptor) == WizardDescriptor.FINISH_OPTION) {
            Context leftGedcomContext = gedcomMergeWizardPanel2.getComponent().getGedcomContext();
            Context rightGedcomContext = gedcomMergeWizardPanel3.getComponent().getGedcomContext();
            File gedcomMergeFile = gedcomMergeWizardPanel5.getComponent().getGedcomMergeFile();

            new GedcomMerge(leftGedcomContext.getGedcom(), rightGedcomContext.getGedcom(), gedcomMergeFile).run();
        }
    }
}
