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
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAlias;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertyXRef;
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
import org.openide.util.Exceptions;
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
            ok &= convertOther(copyGedcom);
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
        
        console.println("====================");
        Entity entity = gedcom.getFirstEntity("HEAD");
        Property property = entity.getProperty("NOTE");
        String note = NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.NoteWarning");
        if (property != null) {
            note = property.getDisplayValue() + " - " + note;
            property.setValue(note);
        } else {
            entity.addProperty("NOTE", note);
        }
        console.println(note);

        return true;
    }

    
    
    private boolean convertOther(Gedcom gedcom) {

        Property[] props = null;
        Property prop = null;
        String rela = null;
        List<Property> propList = null;
        
        console.println(NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.ConvertingOther"));

        // Process families
        for (Entity entity : gedcom.getFamilies()) {
            // Convert FAM:EVEN:TYPE from Relation to unmarried
            props = entity.getProperties("EVEN");
            for (Property p : props) {
                prop = p.getProperty("TYPE");
                if (prop != null) {
                    rela = prop.getValue();
                    if (rela.equals("Relation")) {
                        prop.setValue("unmarried");
                    }
                }
            }
            
            // Process adoptions (remove adopted child from families)
            props = entity.getProperties("CHIL");
            for (Property p : props) {
                prop = ((PropertyXRef) p).getTargetEntity();
                if (prop != null) {
                    Property adop = prop.getProperty("ADOP");
                    if (adop != null) {
                        // remove CHIL from family
                        String id = ((Entity) (p.getEntity())).getId();
                        entity.delProperty(p);
                        // add FAMC and ADOP below ADOP in the INDI record, unless already there
                        Property famc = adop.getProperty("FAMC");
                        if (famc == null) {
                            famc = adop.addProperty("FAMC", "@" + id + "@");
                        }
                        adop = famc.getProperty("ADOP");
                        if (adop == null) {
                            adop = famc.addProperty("ADOP", "BOTH");
                        }
                    }
                }
            }
            
        }
        
        // Conversion for indis 
        for (Entity entity : gedcom.getIndis()) {
            // Convert ALIA to NAME
            props = entity.getProperties("ALIA");
            for (Property p : props) {
                Property parent = p.getParent();
                if (p != null && !(p instanceof PropertyAlias)) {
                    int pos = parent.getPropertyPosition(p);
                    String value = p.getValue();
                    parent.delProperty(p);
                    try {
                        Property pp = parent.addProperty("NAME", "", pos); // for names, set it in two steps.
                        pp.setValue(value);
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            
            // Convert NSFX to NICK
            propList = entity.getAllProperties("NSFX");
            for (Property p : propList) {
                Property parent = p.getParent();
                if (p != null) {
                    int pos = parent.getPropertyPosition(p);
                    String value = p.getValue();
                    parent.delProperty(p);
                    try {
                        parent.addProperty("NICK", value, pos);
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            
        }
        
//        Stack propToDelete = new Stack();
//        
//        // Remove _SOSA and _SOSADABOVILLE and _DABOVILLE tags
//        for (Entity entity : gedcom.getIndis()) {
//            propToDelete.clear();
//            props = entity.getProperties("_SOSA");
//            for (Property p : props) {
//                propToDelete.add(p);
//            }
//            props = entity.getProperties("_SOSADABOVILLE");
//            for (Property p : props) {
//                propToDelete.add(p);
//            }
//            props = entity.getProperties("_DABOVILLE");
//            for (Property p : props) {
//                propToDelete.add(p);
//            }
//            while (!propToDelete.empty()) {
//                entity.delProperty((Property) propToDelete.pop());
//            }
//        }
        
        
        
        
        return true;
        
    }
}
