/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: lemovice (lemovice-at-ancestris-dot-org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.mergefile;

import ancestris.gedcom.GedcomDirectory;
import ancestris.gedcom.GedcomMgr;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.util.Origin;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import static ancestris.modules.gedcom.mergefile.Bundle.*;

@ActionID(category = "Tools",
id = "ancestris.modules.gedcom.merge.GedcomMergeAction")
@ActionRegistration(iconBase = "ancestris/modules/gedcom/merge/arrow_merge_270_left.png",
displayName = "#CTL_GedcomMergeAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools/Gedcom", position = 3333)
})
@Messages("CTL_GedcomMergeAction=Merge 2 Gedcom")
public final class GedcomMergeAction implements ActionListener {

    private final static Logger LOG = Logger.getLogger(GedcomMerge.class.getName(), null);

    @NbBundle.Messages({
        "create.action=Create",
        "create.title=Create Gedcom",
        "# {0} - file path",
        "file.exists=File {0} already exists. Proceed?"
    })
    @Override
    public void actionPerformed(ActionEvent e) {
        Context gedcomAContext;
        Context gedcomBContext;
        Context mergedGedcomContext;

        // ask user
        if ((gedcomAContext = GedcomDirectory.getDefault().openGedcom()) == null) {
            return;
        }
        if ((gedcomBContext = GedcomDirectory.getDefault().openGedcom()) == null) {
            return;
        }

        // let user choose a file
        File file = GedcomDirectory.getDefault().chooseFile(create_title(), create_action(), null);
        if (file == null) {
            return;
        }
        if (!file.getName().endsWith(".ged")) {
            file = new File(file.getAbsolutePath() + ".ged");
        }
        if (file.exists()) {
            int rc = DialogHelper.openDialog(create_title(), DialogHelper.WARNING_MESSAGE, file_exists(file.getName()), Action2.yesNo(), null);
            if (rc != 0) {
                return;
            }
        }

        // form the origin
        Gedcom mergedGedcom;
        try {
            mergedGedcom = new Gedcom(Origin.create(file.toURI().toURL()));
        } catch (MalformedURLException ex) {
            LOG.log(Level.WARNING, "unexpected exception creating new gedcom", ex);
            return;
        }

        new GedcomMerge().merge(gedcomAContext.getGedcom(), gedcomBContext.getGedcom(), mergedGedcom);

        mergedGedcomContext = GedcomMgr.getDefault().setGedcom(mergedGedcom);

        Indi firstIndi = (Indi) mergedGedcomContext.getGedcom().getFirstEntity(Gedcom.INDI);

        // save gedcom file
        GedcomMgr.getDefault().saveGedcom(new Context(firstIndi), FileUtil.toFileObject(file));

        // and reopens the file
        GedcomDirectory.getDefault().openGedcom(FileUtil.toFileObject(file));
    }
}
