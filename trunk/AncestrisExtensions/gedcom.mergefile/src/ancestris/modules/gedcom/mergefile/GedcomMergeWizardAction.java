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
@NbBundle.Messages("CTL_GedcomMergeAction=Merge 2 Gedcom")
public final class GedcomMergeWizardAction implements ActionListener {

    private final static Logger LOG = Logger.getLogger(GedcomMergeWizardAction.class.getName(), null);

    @Override
    public void actionPerformed(ActionEvent e) {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new GedcomMergeWizardPanel1());
        panels.add(new GedcomMergeWizardPanel2());
        panels.add(new GedcomMergeWizardPanel3());
        panels.add(new GedcomMergeWizardPanel4());
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
            Context mergedGedcomContext;

            GedcomMergeWizardPanel2 gedcomMergeWizardPanel2 = (GedcomMergeWizardPanel2) panels.get(1);
            Context gedcomAContext = gedcomMergeWizardPanel2.getComponent().getGedcomContext();

            GedcomMergeWizardPanel3 gedcomMergeWizardPanel3 = (GedcomMergeWizardPanel3) panels.get(2);
            Context gedcomBContext = gedcomMergeWizardPanel3.getComponent().getGedcomContext();

            GedcomMergeWizardPanel4 gedcomMergeWizardPanel4 = (GedcomMergeWizardPanel4) panels.get(3);
            File gedcomMergeFile = gedcomMergeWizardPanel4.getComponent().getGedcomMergeFile();


            // form the origin
            Gedcom mergedGedcom;
            try {
                mergedGedcom = new Gedcom(Origin.create(gedcomMergeFile.toURI().toURL()));
            } catch (MalformedURLException ex) {
                LOG.log(Level.WARNING, "unexpected exception creating new gedcom", ex);
                return;
            }
            new GedcomMerge().merge(gedcomAContext.getGedcom(), gedcomBContext.getGedcom(), mergedGedcom);
            mergedGedcomContext = GedcomMgr.getDefault().setGedcom(mergedGedcom);
            Indi firstIndi = (Indi) mergedGedcomContext.getGedcom().getFirstEntity(Gedcom.INDI);

            // save gedcom file
            GedcomMgr.getDefault().saveGedcom(new Context(firstIndi), FileUtil.toFileObject(gedcomMergeFile));

            // and reopens the file
            GedcomDirectory.getDefault().openGedcom(FileUtil.toFileObject(gedcomMergeFile));
        }
    }
}
