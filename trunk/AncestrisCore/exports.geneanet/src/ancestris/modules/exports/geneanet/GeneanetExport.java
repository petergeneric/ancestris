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
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAlias;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertyChild;
import genj.gedcom.PropertyFamilyChild;
import genj.gedcom.PropertyXRef;
import genj.io.GedcomReader;
import genj.io.GedcomReaderContext;
import genj.io.GedcomReaderFactory;
import genj.util.Origin;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
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

    private final Gedcom gedcom;
    private Gedcom copyGedcom;
    private final SaveOptionsWidget options;
    private final File exportFile;

    GeneanetExport(Gedcom gedcom, File exportFile, SaveOptionsWidget options) {

        this.gedcom = gedcom;
        this.options = options;
        this.exportFile = exportFile;
    }

    public boolean execute() {
        boolean ok;

        // Open console trace
        LOG.log(Level.INFO, "{0} {1}", new Object[]{NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.TabTitle"), gedcom.getName()});
        LOG.log(Level.INFO, String.format(NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.Start"), gedcom.getName()));

        // Create file copy of gedcom file with filters done
        final Origin pOrigin = gedcom.getOrigin();
        try {
            final Origin nOrigin = Origin.create(exportFile.toURI().toURL());
            gedcom.setOrigin(nOrigin);
            ok = GedcomMgr.getDefault().saveGedcomImpl(gedcom, options.getFilters(), null);
            gedcom.setOrigin(pOrigin);
        } catch (IOException e) {
            ok = false;
            LOG.log(Level.INFO, "Can't create new file ", e);
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
            ok &= convertHeader(copyGedcom);
        }

        // Save gedcom copy
        if (ok) {
            ok = GedcomMgr.getDefault().saveGedcomImpl(copyGedcom, null, null);
            // remove backup files is exist
            final String str = copyGedcom.getName().replace(".ged", "") + "_";
            File dir = new File(exportFile.getParent());
            File[] foundFiles = dir.listFiles((File dir1, String name) -> name.startsWith(str));
            for (File file : foundFiles) {
                file.delete();
            }
        }

        // Export terminated
        LOG.log(Level.INFO, "====================");
        LOG.log(Level.INFO, NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.End"));
        return ok;
    }

    public Context openGedcomQuietly(final FileObject input) {
        if (input == null) {
            return null;
        }

        final Context context;
        try {
            Origin origin = Origin.create(input);
            GedcomReader reader = (GedcomReader) Spin.off(GedcomReaderFactory.createReader(origin, (GedcomReaderContext) Spin.over(new GedcomReaderContext() {
                @Override
                public String getPassword() {
                    return "";
                }

                @Override
                public void handleWarning(int line, String warning, Context context) {
                }
            })));
            Gedcom localGedcom = reader.read();
            context = new Context(localGedcom);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Unable to open copied gedcom", ex);
            return null;
        }
        return context;
    }

    private boolean convertAssociations(Gedcom gedcom) {

        String rela = null;

        LOG.log(Level.INFO, NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.ConvertingAssos"));
        
        boolean is55 = Grammar.V55.equals(gedcom.getGrammar());
          final String type = "INDI"; // in geneanet, type is always INDI, not "prop.getTargetType()"

        final List<PropertyAssociation> list = (List<PropertyAssociation>) gedcom.getPropertiesByClass(PropertyAssociation.class);
        for (PropertyAssociation prop : list) {
            // Get info
            LOG.log(Level.INFO, prop.getDisplayValue());
            final Indi indiRela = (Indi) prop.getEntity();
            final Property propAsso = prop.getTarget().getParent();
          
            final Property relaProp = prop.getProperty("RELA");
            if (relaProp != null) {
                rela = relaProp.getDisplayValue();
            }

            // Delete from first asso entity
            prop.getParent().delProperty(prop);

            // Add to second asso entity
            Property parent = propAsso.addProperty("ASSO", "@" + indiRela.getId() + "@");
           if (is55){
               parent.addProperty("TYPE", type);
           }
            parent.addProperty("RELA", rela);
        }

        LOG.log(Level.INFO, "====================");
        return true;
    }

    private boolean convertHeader(Gedcom gedcom1) throws MissingResourceException {
        final Entity entity = gedcom1.getFirstEntity("HEAD");
        final Property property = entity.getProperty("NOTE");
        final String note = NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.NoteWarning");
        if (property != null) {
            // forget old value : this is export for Geneanet.
            //note = property.getDisplayValue() + " - " + note;
            property.setValue(note);
        } else {
            entity.addProperty("NOTE", note);
        }
        // File for Geneanet.
       gedcom1.setDestination("Geneanet");
        LOG.log(Level.INFO, note);
        return true;
    }

    private boolean convertOther(Gedcom gedcom) {
        LOG.log(Level.INFO, NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.ConvertingOther"));

        // Process families
        for (Entity entity : gedcom.getFamilies()) {
            // Convert FAM:EVEN:TYPE from Relation to unmarried
            Property[] props = entity.getProperties("EVEN");
            for (Property p : props) {
                final Property prop = p.getProperty("TYPE");
                if (prop != null) {
                    final String rela = prop.getValue();
                    if (rela.equals("Relation")) {
                        prop.setValue("unmarried");
                    }
                }
            }

            // Process adoptions (remove adopted child from their adopting family)
            props = entity.getProperties("CHIL");
            for (Property p : props) {
                final Property prop = ((PropertyXRef) p).getTargetEntity();
                if (prop != null) {
                    Property adop = prop.getProperty("ADOP");
                    if (adop != null) {
                        // Get adopting family (famc)
                        Property famc = null;
                        for (PropertyFamilyChild pFamChild : adop.getProperties(PropertyFamilyChild.class)) { // use class, incase link to FAMC is lost, getProperty(FAMC) returns null.
                            famc = pFamChild;
                            break;
                        }
                        if (famc == null) {
                            continue;  // we do not know the adopting family, skip.
                        }
                        // add ADOP below FAMC in the INDI record, unless already there
                        adop = famc.getProperty("ADOP");
                        if (adop == null) {
                            famc.addProperty("ADOP", "BOTH");
                        }
                        // remove CHIL from adopting family
                        Fam adopFam = (Fam) ((PropertyXRef) famc).getTargetEntity();
                        if (adopFam == null || adopFam.getNoOfChildren() == 0) {
                            continue;
                        }
                        String currentChildId = prop.getEntity().getId();
                        for (PropertyChild pChild : adopFam.getProperties(PropertyChild.class)) {
                            if (pChild.getChild().getId().equals(currentChildId)) {
                                pChild.unlink();
                                adopFam.delProperty(pChild);
                                break;
                            }
                        }
                    }
                }
            }

        }

        // Conversion for indis 
        for (Entity entity : gedcom.getIndis()) {
            // Convert ALIA to NAME
            for (Property p : entity.getProperties("ALIA")) {
                Property parent = p.getParent();
                if (!(p instanceof PropertyAlias)) {
                    int pos = parent.getPropertyPosition(p);
                    String value = p.getValue();
                    parent.delProperty(p);
                    try {
                        Property pp = parent.addProperty("NAME", "", pos); // for names, set it in two steps.
                        pp.setValue(value);
                    } catch (GedcomException ex) {
                        LOG.log(Level.WARNING, "Error during ALIA conversion", ex);
                    }
                }
            }

            // Convert NSFX to NICK
            for (Property p : entity.getAllProperties("NSFX")) {
                final Property parent = p.getParent();
                int pos = parent.getPropertyPosition(p);
                String value = p.getValue();
                parent.delProperty(p);
                try {
                    parent.addProperty("NICK", value, pos);
                } catch (GedcomException ex) {
                    LOG.log(Level.WARNING, "Error during NSFX conversion", ex);
                }
            }
        }
        
         // Convert _TIME to TIME
         for (Entity entity : gedcom.getEntities()) {
            for (Property p : entity.getAllProperties("_TIME")) {
                final Property parent = p.getParent();
                final int pos = parent.getPropertyPosition(p);
                final String value = p.getValue();
                parent.delProperty(p);
                try {
                    parent.addProperty("TIME", value, pos);
                } catch (GedcomException ex) {
                    LOG.log(Level.WARNING, "Error during _TIME conversion", ex);
                }
            }
         }
         
          // Convert _URL to URL
         for (Entity entity : gedcom.getEntities()) {
            for (Property p : entity.getAllProperties("_URL")) {
                final Property parent = p.getParent();
                final int pos = parent.getPropertyPosition(p);
                final String value = p.getValue();
                parent.delProperty(p);
                try {
                    parent.addProperty("URL", value, pos);
                } catch (GedcomException ex) {
                    LOG.log(Level.WARNING, "Error during _URL conversion", ex);
                }
            }
         }
         
           // Convert Others names to remove slash
         for (Entity entity : gedcom.getEntities("INDI")) {
             final Property[] props = entity.getProperties("NAME");
             // Begin at the second name.
             for (int i=1;i<props.length;i++) {
                 final Property p = props[i];
                 final String newValue = p.getValue().replace("/", " ").trim();
                 p.setValue(newValue);
             }
         }
         
          // Remove all "_TAG"
         for (Entity entity : gedcom.getEntities()) {
            for (Property p : entity.getAllSpecificProperties()) {
                final Property parent = p.getParent();
                final int pos = parent.getPropertyPosition(p);
                parent.delProperty(p);
            }
         }

        return true;
    }
}
