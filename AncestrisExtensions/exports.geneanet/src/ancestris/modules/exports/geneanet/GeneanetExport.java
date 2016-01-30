/*
 *  Copyright (C) 2011 lemovice
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ancestris.modules.exports.geneanet;

import ancestris.gedcom.GedcomMgr;
import ancestris.gedcom.SaveOptionsWidget;
import ancestris.modules.console.Console;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAssociation;
import genj.io.GedcomReader;
import genj.io.GedcomReaderContext;
import genj.io.GedcomReaderFactory;
import genj.util.Origin;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.List;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import spin.Spin;

/**
 *
 * @author Frederic
 */
public class GeneanetExport {

    private final static Logger LOG = Logger.getLogger("ancestris.app", null);
    private Console console = null;
    private Gedcom gedcom = null;
    private Gedcom copyGedcom = null;
    private SaveOptionsWidget options = null;
    private File exportFile = null;

    GeneanetExport(Gedcom gedcom, File exportFile, SaveOptionsWidget options) {

        this.gedcom = gedcom;
        this.options = options;
        this.exportFile = exportFile;
    }

    public boolean execute() {
        boolean ok = true;

        // Open console trace
        console = new Console(NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.TabTitle") + " " + gedcom.getName());
        console.println(String.format(NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.Start"), gedcom.getName()));

        // Create file copy of gedcom file
        try {
            Files.copy(gedcom.getOrigin().getFile().toPath(), exportFile.toPath(), REPLACE_EXISTING);
        } catch (Throwable t) {
            console.println("Failed to create origin for file " + exportFile.getAbsolutePath());
            console.println(t.toString());
            ok = false;
        }

        // Open gedcom copy
        if (ok) {
            Context context = openGedcomQuietly(FileUtil.toFileObject(exportFile));
            ok = context != null;
            if (ok) {
                copyGedcom = context.getGedcom();
            }
        }

        // Convert to geneanet format (only associations are to be converted)
        if (ok && copyGedcom != null) {
            ok = convertAssociations(copyGedcom);
        }

        // Save gedcom copy
        if (ok) {
            ok = GedcomMgr.getDefault().saveGedcomImpl(copyGedcom, options.getFilters(), null);
            // remove backup files is exist
            final String str = copyGedcom.getName().replace(".ged", "")+"_";
            File dir = new File(exportFile.getParent());
            File[] foundFiles = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith(str);
                }
            });
            for (File file : foundFiles) {
                file.delete();
            }
        }

        // Export terminated
        console.println("====================");
        console.println(NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.End"));
        console.close();
        return ok;
    }

    public Context openGedcomQuietly(final FileObject input) {
        if (input == null) {
            return null;
        }

        Context context = null;

        try {
            Origin origin = Origin.create(input);
            GedcomReader reader = (GedcomReader) Spin.off(GedcomReaderFactory.createReader(origin, (GedcomReaderContext) Spin.over(new GedcomReaderContext() {
                public String getPassword() {
                    return "";
                }

                public void handleWarning(int line, String warning, Context context) {
                }
            })));
            Gedcom localGedcom = reader.read();
            context = new Context(localGedcom);
        } catch (Exception ex) {
            return null;
        }
        return context;
    }

    private boolean convertAssociations(Gedcom gedcom) {

        Indi indiRela = null;
        Property propAsso = null;
        String type = null;
        Property relaProp = null;
        String rela = null;

        console.println(NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.ConvertingAssos"));

        List<PropertyAssociation> list = (List<PropertyAssociation>) gedcom.getPropertiesByClass(PropertyAssociation.class);
        for (PropertyAssociation prop : list) {
            // Get info
            console.println(prop.getDisplayValue());
            indiRela = (Indi) prop.getEntity();
            propAsso = prop.getTarget().getParent();
            type = "INDI"; // in geneanet, type is always INDI, not "prop.getTargetType()"
            relaProp = prop.getProperty("RELA");
            if (relaProp != null) {
                rela = relaProp.getDisplayValue();
            }

            // Delete from first asso entity
            prop.getParent().delProperty(prop);

            // Add to second asso entity
            Property parent = propAsso.addProperty("ASSO", "@" + indiRela.getId() + "@");
            parent.addProperty("TYPE", type);
            parent.addProperty("RELA", rela);
        }

        return true;
    }

}
