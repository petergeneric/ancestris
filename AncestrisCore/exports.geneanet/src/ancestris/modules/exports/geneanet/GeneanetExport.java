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
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyRelationship;
import genj.gedcom.PropertyXRef;
import genj.io.Filter;
import genj.io.GedcomReader;
import genj.io.GedcomReaderContext;
import genj.io.GedcomReaderFactory;
import genj.util.DirectAccessTokenizer;
import genj.util.Origin;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            ok = GedcomMgr.getDefault().saveGedcomImpl(gedcom, options.getFilters(), null, options.getSort());
            gedcom.setOrigin(pOrigin);
        } catch (IOException e) {
            ok = false;
            LOG.log(Level.INFO, "Can't create new file ", e);
        }

        // Open gedcom copy
        if (ok) {
            Context context = openGedcomQuietly(FileUtil.toFileObject(exportFile), gedcom.getPassword());
            ok = context != null;
            if (ok) {
                copyGedcom = context.getGedcom();
            }
        }

        // Convert to geneanet format
        if (ok && copyGedcom != null) {
            try {
                ok = convertAssociations(copyGedcom);
                ok &= convertOther(copyGedcom);
                ok &= convertHeader(copyGedcom);
            } catch (Exception e) { // Log if a trouble occurs.
                LOG.log(Level.SEVERE, "Error during Geneanet conversion", e);
                ok = false;
            }
        }

        // Save gedcom copy
        if (ok) {
            ok = GedcomMgr.getDefault().saveGedcomImpl(copyGedcom, null, null, false);
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

    private Context openGedcomQuietly(final FileObject input, final String password) {
        if (input == null) {
            return null;
        }

        final Context context;
        try {
            Origin origin = Origin.create(input);
            GedcomReader reader = (GedcomReader) Spin.off(GedcomReaderFactory.createReader(origin, (GedcomReaderContext) Spin.over(new GedcomReaderContext() {
                @Override
                public String getPassword() {
                    return password;
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

        boolean returnAsso = true;
        for (Filter f : options.getFilters()) {
            if (f instanceof FilterAssociationOption) {
                returnAsso = false;
                break;
            }
        }

        LOG.log(Level.INFO, NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.ConvertingAssos"));

        boolean is55 = Grammar.V55.equals(gedcom.getGrammar());
        final String type = "INDI"; // in geneanet, type is always INDI, not "prop.getTargetType()"

        final List<PropertyAssociation> list = (List<PropertyAssociation>) gedcom.getPropertiesByClass(PropertyAssociation.class);
        for (PropertyAssociation prop : list) {
            // Get info
            // LOG.log(Level.INFO, prop.getDisplayValue());
            final Indi indiRela = (Indi) prop.getEntity();

            // No target, loop, no need to switch.
            if (prop.getTarget() == null) {
                continue;
            }
            final Property propAsso = prop.getTarget().getParent();

            final Property relaProp = prop.getProperty("RELA");
            boolean hasAnchor = false;
            if (relaProp != null) {
                rela = relaProp.getDisplayValue();
                hasAnchor = ((PropertyRelationship) relaProp).getAnchor() != null;
            }

            if (returnAsso) {
                // Delete from first asso entity
                prop.getParent().delProperty(prop);

                // Add to second asso entity
                // Need to be XRef to avoid duplication of @            
                Property parent = propAsso.addPropertyXref("ASSO", indiRela.getId(), -1);
                if (is55) {
                    parent.addProperty("TYPE", type);
                }
                parent.addProperty("RELA", rela);
            } else if (hasAnchor) {
                Property cible = indiRela.getProperty(((PropertyRelationship) relaProp).getAnchor());
                if (cible != null) {
                    Property parent = cible.addPropertyXref("ASSO", propAsso.getEntity().getId(), -1);
                    if (is55) {
                        parent.addProperty("TYPE", type);
                    }
                    parent.addProperty("RELA", rela);
                    prop.getParent().delProperty(prop);
                } else {
                    // Just unlink and remove unused anchor
                    prop.unlink();
                    relaProp.setValue(rela);
                }
                
            }
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
                    if ("Relation".equals(rela)) {
                        prop.setValue("unmarried");
                    }
                }
            }

            // In case of MARC and no MARR create a MARR without date.
            final Property[] marc = entity.getProperties("MARC");
            final Property[] marr = entity.getProperties("MARR");
            if (marc.length > 0 && marr.length == 0) {
                entity.addProperty("MARR", "Y");
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

            // Convert Others names to remove slash and remove GIVN and SURN
            final Property[] props = entity.getProperties("NAME");
            // Loop on NAMEs
            for (int i = 0; i < props.length; i++) {
                final Property p = props[i];

                // Remove GIVN and SURN (geneanet does not need them)
                Property tmpG = p.getProperty("GIVN");
                if (tmpG != null) {
                    tmpG.setGuessed(true);
                }
                Property tmpS = p.getProperty("SURN");
                if (tmpS != null) {
                    tmpS.setGuessed(true);
                }

                // Begin at the second name.
                if (i > 0) {
                    final String newValue = p.getValue().replace("/", " ").trim();
                    p.setValue(newValue);
                }
            }
        }

        for (Filter fil : options.getFilters()) {
            if (fil instanceof FilterPlaceOption) {
                manageGeneanetPlaceFormat(gedcom);
                break;
            }
        }

        // Adjust others tags
        for (Entity entity : gedcom.getEntities()) {
            // Convert _TIME to TIME
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

            // Convert _URL to URL
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

            // Replace all ADR1 ADR2 ADR3 by ADDR
            for (Property p : entity.getAllProperties("ADDR")) {
                String addresse = p.getValue();
                for (Property adr1 : p.getAllProperties("ADR1")) {
                    addresse = addresse + " " + adr1.getValue();
                    p.delProperty(adr1);
                }
                for (Property adr2 : p.getAllProperties("ADR2")) {
                    addresse = addresse + " " + adr2.getValue();
                    p.delProperty(adr2);
                }
                for (Property adr3 : p.getAllProperties("ADR3")) {
                    addresse = addresse + " " + adr3.getValue();
                    p.delProperty(adr3);
                }
                p.setValue(addresse);

            }

            //At the end :  Remove all "_TAG"
            for (Property p : entity.getAllSpecificProperties()) {
                if (entity.contains(p)) {
                    p.delProperties();
                    final Property parent = p.getParent();
                    if (parent != null) {
                        parent.delProperty(p);
                    }
                }
            }
        }

        return true;
    }

    private void manageGeneanetPlaceFormat(Gedcom gedcom) {
        final String format = gedcom.getPlaceFormat();
        final String cityTag = PropertyPlace.getCityTag(gedcom);

        final DirectAccessTokenizer datForm = new DirectAccessTokenizer(format, PropertyPlace.JURISDICTION_SEPARATOR);

        int pos = datForm.contains(cityTag);

        final StringBuilder sbForm = new StringBuilder(format.length() + 5);

        if (pos == -1) {
            return;
        }
        sbForm.append('[');
        for (int i = 0; i < pos; i++) {
            sbForm.append(datForm.get(i));
            if (i != pos - 1) {
                sbForm.append(PropertyPlace.JURISDICTION_SEPARATOR);
            }
        }
        sbForm.append("] - ");
        sbForm.append(datForm.getSubstringFrom(pos).trim());
        gedcom.setPlaceFormat(sbForm.toString());

        final List<Property> allPlaces = (List<Property>) gedcom.getPropertiesByClass(PropertyPlace.class);
        final Map<String, String> allUniquePlaces = new HashMap<>();
        for (Property place : allPlaces) {
            if (allUniquePlaces.containsKey(place.getValue())) {
                place.setValue(allUniquePlaces.get(place.getValue()));
            } else {
                final DirectAccessTokenizer dat = new DirectAccessTokenizer(place.getValue(), PropertyPlace.JURISDICTION_SEPARATOR);
                final StringBuilder sb = new StringBuilder(place.getValue().length() + 5);
                String subdivision = dat.getSubstring(0, pos).trim();
                if (subdivision.startsWith(",")) {
                    subdivision = subdivision.substring(1);
                }
                if (!"".equals(subdivision.replaceAll(PropertyPlace.JURISDICTION_SEPARATOR, "").trim())) {
                    sb.append('[').append(subdivision).append("] - ");
                }
                sb.append(dat.getSubstringFrom(pos).trim());

                final String result = sb.toString();
                allUniquePlaces.put(place.getValue(), result);
                place.setValue(result);
            }

        }
    }
}
