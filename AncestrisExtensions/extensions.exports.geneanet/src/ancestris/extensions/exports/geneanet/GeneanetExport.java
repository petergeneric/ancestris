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
package ancestris.extensions.exports.geneanet;

import ancestris.extensions.console.Console;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyNote;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.time.PointInTime;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Lemovice
 */
public class GeneanetExport {

    private class GwIndi {

        private boolean alreadyDescribed = false;
        private String firstName = null;
        private String lastName = null;
        private int occurence = 0;
        private String description = null;
        private String notes = null;
        private String relations = null;
        private boolean canBeExported = false;

        public GwIndi() {
        }

        private GwIndi(String lastName, String firstName, int occurence) {
            this.lastName = lastName;
            this.firstName = firstName;
            this.occurence = occurence;
        }

        public String getName() {
            return lastName + " " + firstName;
        }

        public String getNameOccurenced() {
            if (occurence > 0) {
                return lastName + " " + firstName + "." + occurence;
            } else {
                return lastName + " " + firstName;
            }
        }

        public String getLastName() {
            return lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getFirstNameOccurenced() {
            if (occurence > 0) {
                return firstName + "." + occurence;
            } else {
                return firstName;
            }
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getNotes() {
            return notes;
        }

        public void setRelations(String relations) {
            this.relations = relations;
        }

        public String getRelations() {
            return relations;
        }

        void setAlreadyDescribed() {
            alreadyDescribed = true;
        }

        public boolean isDescribed() {
            return alreadyDescribed;
        }

        public boolean canBeExported() {
            return canBeExported;
        }

        public void setCanBeExported(boolean canBeExported) {
            this.canBeExported = canBeExported;
        }
    }
    private Map<String, Integer> indiNameOccurence = new HashMap<String, Integer>();
    private Map<String, GwIndi> indiMap = new HashMap<String, GwIndi>();
    private final static Logger LOG = Logger.getLogger("genj.app", null);
    private Console console = null;
    private boolean notesExported = true;
    private boolean sourcesExported = true;
    private boolean exportedRestricted = true;
    private boolean logEnabled = false;
    private boolean aliveExported = false;
    private boolean divorceExported = false;
    private boolean relationsExported = false;
    private boolean weddingDetailExported = false;
    private boolean eventsExported = false;
    private int ExportRestriction = 100;
    private Gedcom myGedcom = null;
    private File exportFile = null;

    GeneanetExport(Gedcom gedcom, File exportFile) {
        Preferences modulePreferences = NbPreferences.forModule(GeneanetExport.class);

        this.myGedcom = gedcom;
        this.exportFile = exportFile;
        this.exportedRestricted = modulePreferences.getBoolean("ExportRestricited", true);
        this.ExportRestriction = modulePreferences.getInt("RestricitionDuration", 100);
        this.notesExported = modulePreferences.getBoolean("ExportNotes", true);
        this.logEnabled = modulePreferences.getBoolean("LogEnable", false);

        this.sourcesExported = modulePreferences.getBoolean("ExportSources", true);
        this.aliveExported = modulePreferences.getBoolean("ExportAlive", false);
        this.eventsExported = modulePreferences.getBoolean("ExportEvents", true);
        this.relationsExported = modulePreferences.getBoolean("ExportRelations", false);
        this.weddingDetailExported = modulePreferences.getBoolean("ExportWeddingDetails", true);
        this.divorceExported = modulePreferences.getBoolean("ExportDivorce", false);
    }

    public void start() {
        console = new Console (NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.TabTitle") + " " + myGedcom.getName());

        console.println(String.format(NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.Start"), myGedcom.getName()));

        // Analyze all Individuals
        analyzeIndis(myGedcom.getIndis());
        IndisRelations(myGedcom.getIndis());

        exportFamilys(myGedcom.getFamilies(), exportFile);

        /*
         * List all unexported Individuals
         */
        Iterator iterator = indiMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            GwIndi indi = indiMap.get(key);

            if (indi.canBeExported() == true && indi.isDescribed() == false) {
                console.println(key + " " + indi.getName());
            }
        }

        // export terminated
        console.println(NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.End"));
        console.close();
    }

    private boolean canbeExported(Indi indi) {
        PointInTime CurrentDate = PointInTime.getNow();
        PropertyDate birthDate = indi != null ? indi.getBirthDate() : null;
        PointInTime date = birthDate != null ? birthDate.getStart() : null;

        if (date != null) {
            if (exportedRestricted == true) {
                if (CurrentDate.getYear() - date.getYear() > ExportRestriction) {
                    if (aliveExported == true) {
                        return true;
                    } else {
                        if (indi.isDeceased()) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } else {
            // dans le doute on s'abstient
            if (exportedRestricted == true) {
                return false;
            } else {
                return true;
            }
        }
    }

    private boolean canbeExported(Fam family) {
        PointInTime CurrentDate = PointInTime.getNow();
        PropertyDate marriageDate = family != null ? family.getMarriageDate() : null;
        PointInTime date = marriageDate != null ? marriageDate.getStart() : null;

        if (date != null) {
            if (exportedRestricted == true) {
                return CurrentDate.getYear() - date.getYear() > ExportRestriction;
            } else {
                return true;
            }
        } else {
            // dans le doute on s'abstient
            if (exportedRestricted == true) {
                return false;
            } else {
                return true;
            }
        }
    }

    private void exportFamilys(Collection<Fam> familys, File file) {
        BufferedWriter out = null;
        int nbExportedFamilys = 0;
        int nbExportedindis = 0;

        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));

            out.write("encoding: utf-8\n\n");
            Iterator familysIterator = familys.iterator();
            while (familysIterator.hasNext()) {
                Fam family = (Fam) familysIterator.next();
                Indi husband = family.getHusband();
                Indi wife = family.getWife();

                if (canbeExported(family)) {
                    nbExportedFamilys += 1;
                    if (logEnabled == true) {
                        LOG.log(Level.INFO, NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.ExportFamilleText"), family.toString(true));
                    }

                    /*
                     * fam HusbandLastName FirstName[.Number]
                     */
                    out.write("fam ");
                    if (husband != null) {
                        /*
                         *  LastName FirstName.Occurence
                         */
                        out.write(indiMap.get(husband.getId()).getNameOccurenced() + " ");
                        Fam[] husbandFamc = husband.getFamiliesWhereChild();
                        if (husbandFamc.length == 0) {
                            GwIndi gwIndi = indiMap.get(husband.getId());

                            if (gwIndi.isDescribed() == false) {
                                if (logEnabled == true) {
                                    LOG.log(Level.INFO, NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.ExportIndividuText"), husband.toString(true));
                                }
                                gwIndi.setAlreadyDescribed();
                                nbExportedindis += 1;
                                indiMap.put(husband.getId(), gwIndi);
                                out.write(gwIndi.getDescription() + " ");
                            }
                        }
                    } else {
                        Integer NameOccurence = indiNameOccurence.get("?_?");
                        indiNameOccurence.put("?_?", (NameOccurence == null) ? 1 : NameOccurence + 1);
                        if (NameOccurence == null) {
                            out.write("? ? ");
                        } else {
                            out.write("? ?." + NameOccurence + " ");
                        }
                    }

                    /*
                     * +
                     */
                    out.write("+");
                    Property marriage = family.getProperty("MARR");
                    if (marriage != null) {
                        /*
                         * [WeddingDate] [#mp WeddingPlace] [#ms WeddingSource]
                         */
                        PropertyDate marriageDate = (PropertyDate) marriage.getProperty("DATE");
                        if (marriageDate != null) {
                            out.write(analyzeDate(marriageDate) + " ");
                        } else {
                            out.write("0 ");
                        }

                        PropertyPlace marriagePlace = (PropertyPlace) marriage.getProperty("PLAC");
                        if (marriagePlace != null && marriagePlace.getValue().isEmpty() != true) {
                            if (marriagePlace.getValue().length() > 0) {
                                out.write("#mp " + marriagePlace.getValue().replaceAll(" |\n", "_") + " ");
                            }
                        }

                        if (sourcesExported == true) {
                            Property marriageSource = marriage.getProperty("SOUR");
                            if (marriageSource != null) {
                                out.write("#ms " + source2String(marriageSource) + " ");
                            }
                        }

                        /*
                         * [#sep | - DivorceDate]
                         *
                         */
                        if (divorceExported == true) {
                            Property divorce = family.getProperty("DIV");
                            if (divorce != null) {
                                PropertyDate divorceDate = (PropertyDate) divorce.getProperty("DATE");
                                if (divorceDate != null) {
                                    out.write("-" + analyzeDate(divorceDate) + " ");
                                } else {
                                    out.write("-0 ");
                                }
                            }
                        }
                    } else {
                        /*
                         * not married
                         * [#nm | #eng]
                         *
                         */
                        out.write(" #nm ");
                    }

                    /*
                     * WifeLastName FirstName[.Number]
                     *
                     */
                    if (wife != null) {
                        /*
                         *  LastName FirstName.Occurence
                         */
                        out.write(indiMap.get(wife.getId()).getNameOccurenced());
                        Fam[] wifeFamc = wife.getFamiliesWhereChild();
                        if (wifeFamc.length == 0) {
                            GwIndi gwIndi = indiMap.get(wife.getId());

                            if (gwIndi.isDescribed() == false) {
                                if (logEnabled == true) {
                                    LOG.log(Level.INFO, NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.ExportIndividuText"), wife.toString(true));
                                }
                                gwIndi.setAlreadyDescribed();
                                nbExportedindis += 1;
                                indiMap.put(wife.getId(), gwIndi);
                                out.write(" " + gwIndi.getDescription());
                            }
                        }
                    } else {
                        Integer NameOccurence = indiNameOccurence.get("?_?");
                        indiNameOccurence.put("?_?", (NameOccurence == null) ? 1 : NameOccurence + 1);
                        if (NameOccurence == null) {
                            out.write("? ? ");
                        } else {
                            out.write("? ?." + NameOccurence + " ");
                        }
                    }

                    out.write("\n");

                    /*
                     * [src Family source]
                     */
                    if (sourcesExported == true) {
                        Property familySource = family.getProperty("SOUR");
                        if (familySource != null) {
                            out.write("src " + source2String(familySource) + "\n");
                        }
                    }

                    /*
                     * [comm Family comments in free format]
                     */
                    if (notesExported == true) {
                        Property[] familyNotes = family.getProperties("NOTE");
                        if (familyNotes.length > 0) {
                            // PropertyNote|PropertyMultilineValue
                            out.write("comm ");
                            for (Property note : familyNotes) {
                                boolean first = true;
                                if (note instanceof PropertyNote) {
                                    note = ((PropertyNote) note).getTargetEntity();
                                }
                                if (first == true) {
                                    first = false;
                                    out.write(note.getValue().replaceAll("\n", " "));
                                } else {
                                    out.write("<br>" + note.getValue().replaceAll("\n", " "));
                                }
                            }
                            out.write("\n");
                        }
                    }

                    /*
                     * [wit: Witness (use Person format, see Person Information section) ]
                     */
                    if (marriage != null && weddingDetailExported == true) {
                        Property[] propertiesXRef = marriage.getProperties("XREF");
                        if (propertiesXRef.length > 0) {
                            for (Property xrefProperty : propertiesXRef) {
                                if (xrefProperty instanceof PropertyXRef) {
                                    if (((PropertyXRef) xrefProperty).getTarget() instanceof PropertyAssociation) {
                                        PropertyAssociation association = (PropertyAssociation) ((PropertyXRef) xrefProperty).getTarget();
                                        if (association.getParent() instanceof Indi) {
                                            Indi witness = (Indi) association.getParent();
                                            out.write("wit: " + indiMap.get(witness.getId()).getNameOccurenced() + "\n");
                                        }
                                    }
                                }
                            }

                        }
                    }

                    /*
                     * beg
                     * - [h | f | ] Person (see detailed description at the next section)
                     * end
                     */
                    Indi[] childrens = family.getChildren();
                    if (childrens.length > 0) {
                        out.write("beg\n");
                        for (Indi children : childrens) {
                            if (canbeExported(children) == true) {
                                nbExportedindis += 1;
                                if (logEnabled == true) {
                                    LOG.log(Level.INFO, NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.ExportIndividuText"), children.toString(true));
                                }

                                switch (children.getSex()) {
                                    case PropertySex.FEMALE:
                                        out.write("- f ");
                                        break;

                                    case PropertySex.MALE:
                                        out.write("- h ");
                                        break;

                                    default:
                                        out.write("- ");
                                        break;
                                }
                                if (husband != null) {
                                    /*
                                     * FirstName.Occurence
                                     */
                                    out.write(indiMap.get(children.getId()).getFirstNameOccurenced() + " ");
                                    if (children.getLastName().equals(husband.getLastName()) != true) {
                                        /*
                                         *  LastName FirstName.Occurence
                                         */
                                        out.write(indiMap.get(children.getId()).getLastName() + " ");
                                    }
                                } else {
                                    /*
                                     * FirstName.Occurence
                                     */
                                    out.write(indiMap.get(children.getId()).getFirstNameOccurenced() + " ");
                                    if (children.getLastName().equals(wife.getLastName())) {
                                        /*
                                         *  LastName
                                         */
                                        out.write(indiMap.get(children.getId()).getLastName() + " ");
                                    }
                                }
                                out.write(indiMap.get(children.getId()).getDescription());
                                indiMap.get(children.getId()).setAlreadyDescribed();
                                out.write("\n");
                            }
                        }
                        out.write("end\n");
                    }
                    out.write("\n");
                }
            }

            Iterator it = indiMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                GwIndi indi = (GwIndi) entry.getValue();
                if (indi.canBeExported()) {
                    /*
                     * indi Notes
                     * notes LastName FirstName[.Number]
                     * beg
                     * Notes go here in a free format
                     * end notes
                     */
                    if (notesExported == true) {
                        if (indi.getNotes() != null) {
                            out.write("notes " + indi.getNameOccurenced() + "\n");
                            out.write(indi.getNotes());
                        }
                    }

                    /*
                     * indi Relations
                     * rel LastName FirstName[.Number]
                     */
                    if (relationsExported == true) {
                        if (indi.getRelations() != null) {
                            out.write("rel " + indi.getNameOccurenced() + "\n");
                            out.write("beg\n");
                            out.write(indi.getRelations());
                            out.write("end\n");
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex1) {
                    Exceptions.printStackTrace(ex1);
                }
            }

            Exceptions.printStackTrace(ex);
        } catch (UnsupportedEncodingException ex) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex1) {
                    Exceptions.printStackTrace(ex1);
                }
            }
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex1) {
                    Exceptions.printStackTrace(ex1);
                }
            }
            Exceptions.printStackTrace(ex);
        } catch (Exception ex) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex1) {
                    Exceptions.printStackTrace(ex1);
                }
            }
            Exceptions.printStackTrace(ex);
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException ex1) {
                Exceptions.printStackTrace(ex1);
            }
        }

        console.println("Nombre de familles exportées " + nbExportedFamilys);
        console.println("Nombre d'individus exportés " + nbExportedindis);
        if (logEnabled == true) {
            LOG.log(Level.INFO, NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.NbFamillesExportText"), nbExportedFamilys);
            LOG.log(Level.INFO, NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.NbIndividusExportText"), nbExportedindis);
        }

    }

    /*
     * Generate geneanet individuals keys
     */
    private void analyzeIndis(Collection<Indi> indis) {

        for (Iterator<Indi> indisIterator = indis.iterator(); indisIterator.hasNext();) {
            String firstName = null;
            String lastName = null;
            Indi indi = indisIterator.next();

            Property[] pIndiNames = indi.getProperties("NAME");
            if (pIndiNames.length > 0) {
                // extract First and last Name of the first property
                // name found for key generation
                if (((PropertyName) pIndiNames[0]).getLastName().length() > 0) {
                    lastName = ((PropertyName) pIndiNames[0]).getLastName().replaceAll(" |\n", "_");
                } else {
                    lastName = " ?";
                }
                if (((PropertyName) pIndiNames[0]).getFirstName().length() > 0) {
                    firstName = ((PropertyName) pIndiNames[0]).getFirstName();
                    firstName = firstName.replaceAll(",|;", "");
                    int index = firstName.indexOf(" ");
                    if (index > 0) {
                        firstName = firstName.substring(0, index);
                    }
                } else {
                    firstName = " ?";
                }
            } else {
                firstName = "?";
                lastName = "?";
            }

            // create the Key
            String fullName = (lastName.replaceAll("-", "_") + "_" + firstName.replaceAll("-", "_")).toLowerCase();
            String indiKey = Normalizer.normalize(fullName, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            console.println(indiKey);

            Integer NameOccurence = indiNameOccurence.get(indiKey);
            indiNameOccurence.put(indiKey, (NameOccurence == null) ? 1 : NameOccurence + 1);
            GwIndi gwIndi = null;
            if (NameOccurence == null) {
                gwIndi = new GwIndi(lastName, firstName, 0);
            } else {
                gwIndi = new GwIndi(lastName, firstName, NameOccurence);
            }

            gwIndi.setCanBeExported(canbeExported(indi));
            gwIndi.setDescription(analyzeIndi(indi));

            /*
             * indi Notes
             */
            if (notesExported == true) {
                Property[] indiNotes = indi.getProperties("NOTE");
                if (indiNotes.length > 0) {
                    String stringNotes = "";
                    stringNotes = "beg\n";
                    // PropertyNote | PropertyMultilineValue
                    for (Property note : indiNotes) {
                        boolean first = true;
                        if (note instanceof PropertyNote) {
                            note = ((PropertyNote) note).getTargetEntity();
                        }
                        if (first == true) {
                            first = false;
                            stringNotes += note.getValue().replaceAll("\n", " ");
                        } else {
                            stringNotes += "<br>" + note.getValue().replaceAll("\n", " ");
                        }
                    }
                    stringNotes += "\nend notes\n";
                    gwIndi.setNotes(stringNotes);
                }
            }

            indiMap.put(indi.getId(), gwIndi);
        }
    }

    private String analyzeIndi(Indi indi) {
        String indiDescription = "";


        Property[] indiNames = indi.getProperties("NAME");
        if (indiNames != null) {
            indiDescription += analyseName(indiNames);
        }

        /*
         * [ TitleName:Title:TitlePlace:StartDate:EndDate:Nth]
         */

        /*
         * [#apubl | #apriv]
         */
        /*
        if ((indi.isPrivate() == true)) {
        indiDescription += " #apriv ";
        } else {
        indiDescription += " #apubl ";
        }
         */

        /*
         * [#occu Occupation]
         */
        Property[] indiOccu = indi.getProperties("OCCU");
        if (indiOccu != null) {

            String occuString = new String();

            for (Property occu : indiOccu) {

                if (occuString.length() > 0) {
                    occuString += "_" + occu.getDisplayValue().replaceAll(" |\n", "_");
                } else {
                    occuString = occu.getDisplayValue().replaceAll(" |\n", "_");
                }

                Property[] occuProperties = occu.getProperties();
                for (Property occuProperty : occuProperties) {
                    occuProperty.getValue();
                    if (occuString.length() > 0) {
                        occuString += "_" + occuProperty.getDisplayValue().replaceAll(" |\n", "_");
                    } else {
                        occuString = occuProperty.getDisplayValue().replaceAll(" |\n", "_");
                    }
                }
            }

            if (occuString.length() > 0) {
                indiDescription += "#occu " + occuString + " ";
            }
        }

        /*
         * [#src PersonSource]
         */
        if (sourcesExported == true) {
            Property indiSource = indi.getProperty("SOUR");
            if (indiSource != null) {
                indiDescription += "#src " + source2String(indiSource) + " ";
            }
        }

        /*
         * DateOfBirth [#bs BirthSource] [#bp PlaceOfBirth] [!BaptizeDate]
         * [#pp BaptizePlace] [#ps BaptizeSource]
         */
        Property birth = indi.getProperty("BIRT");
        if (birth != null) {
            indiDescription += analyzeBirth(birth) + " ";
        }

        /*
         * [DateOfDeath] [#dp PlaceOfDeath] [#ds DeathSource]
         * [#buri | #crem [BurialDate]] [#rp BurialPlace] [#rs BurialSource]
         */
        Property death = indi.getProperty("DEAT");
        if (death != null) {
            indiDescription += analyzeDeath(death) + " ";
        }

        return indiDescription;
    }

    public void IndisRelations(Collection<Indi> indis) {
        for (Iterator<Indi> indisIterator = indis.iterator(); indisIterator.hasNext();) {
            Indi indi = indisIterator.next();
            /*
             * rel LastName FirstName[.Number]
             * beg
             * - adop: AdoptiveFather + AdoptiveMother
             * - adop fath : AdoptiveFather
             * - adop moth : AdoptiveMother
             * - reco: RecognizingFather + RecognizingMother
             * - reco fath : RecognizingFather
             * - reco moth : RecognizingMother
             * - cand: CandidateFather + CandidateMother
             * - cand fath : CandidateFather
             * - cand moth : CandidateMother
             * - godp: GodFather + GodMother
             * - godp fath : GodFather
             * - godp moth : GodMother
             * - fost: FosterFather + FosterMother
             * - fost fath : FosterFather
             * - fost moth : FosterMother
             * end
             */
            String relations = "";
            Property propertyBapm = indi.getProperty("BAPM");
            if (propertyBapm != null) {
                Property[] propertiesXRef = propertyBapm.getProperties("XREF");
                for (Property propertyXRef : propertiesXRef) {
                    Entity targetEntity = ((PropertyXRef) propertyXRef).getTargetEntity();
                    if (((Indi) targetEntity).getSex() == PropertySex.MALE) {
                        relations += "- godp fath: " + indiMap.get(((Indi) targetEntity).getId()).getNameOccurenced() + "\n";
                    } else {
                        relations += "- godp moth: " + indiMap.get(((Indi) targetEntity).getId()).getNameOccurenced() + "\n";
                    }
                }
            }
            if (relations.length() > 0) {
                GwIndi gwIndi = indiMap.get(indi.getId());
                gwIndi.setRelations(relations);
                indiMap.put(indi.getId(), gwIndi);
            }
        }
    }

    /*
     *
     * LastName FirstName [{FirstNameAlias}] [#salias SurnameAlias] [(PublicName)]
     * [#image ImageFilePath] [#nick Qualifier] [#alias Alias]
     *
     * NB LastName FirstName already inserted by analyseFamily.
     */
    private String analyseName(Property[] indiNames) {
        String nameDescription = "";
        /*
         * <NAME default="1" type="PropertyName" img="Name">
         *   <NPFX type="PropertySimpleValue" img="Name"/>
         *   <GIVN type="PropertySimpleValue" img="Name"/>
         *   <NICK type="PropertySimpleValue" img="Name"/>
         *   <SPFX type="PropertySimpleValue" img="Name"/>
         *   <SURN type="PropertySimpleValue" img="Name"/>
         *   <NSFX type="PropertySimpleValue" img="Name"/>
         *   <SOUR/>
         *   <NOTE>
         *      <SOUR/>
         *   </NOTE>
         *</NAME>
         */

        /*
         * Extract [{FirstNameAlias}] from the fisrt PropertyName found
         */
        if (((PropertyName) indiNames[0]).getFirstName().indexOf(" ") > 0) {
            nameDescription += "{" + ((PropertyName) indiNames[0]).getFirstName().replaceAll(" ", "_") + "} ";
        }

        /*
         * loop over other Propertyname and extract [#salias SurnameAlias]
         */
        for (int index = 1; index < indiNames.length; index++) {
            PropertyName indiname = (PropertyName) indiNames[index];
            if (indiname.getFirstName().length() > 0) {
                nameDescription += "#salias " + indiname.getLastName().replaceAll(",|;", "").replaceAll(" ", "_") + " ";
            }
        }

        /*
         * Extract [(PublicName)]  from the fisrt PropertyName found
         */
        Property pGivenName = indiNames[0].getProperty("GIVN");
        if (pGivenName != null) {
            nameDescription += "(" + indiNames[0].getProperty("GIVN").getValue().replaceAll(",|;", "").replaceAll(" ", "_") + ") ";
        }

        /*
         * [#image ImageFilePath] [#nick Qualifier]
         */

        /*
         * extract [#alias Alias]
         */
        if (indiNames[0].getProperty("NICK") != null) {
            if (indiNames[0].getProperty("NICK").getValue().length() > 0) {
                nameDescription += "#alias " + indiNames[0].getProperty("NICK").getValue().replaceAll(",|;", "").replaceAll(" ", "_") + " ";
            }
        }

        return nameDescription;

    }

    /*
     * DateOfBirth [#bs BirthSource] [#bp PlaceOfBirth] [!BaptizeDate]
     * [#pp BaptizePlace] [#ps BaptizeSource]
     */
    private String analyzeBirth(
            Property birth) {

        // DateOfBirth
        String birthString = new String();
        PropertyDate birthDate = (PropertyDate) birth.getProperty("DATE");
        if (birthDate != null) {
            birthString = analyzeDate(birthDate) + " ";
        } else {
            birthString = "0 ";
        }

        // [#bp PlaceOfBirth]
        PropertyPlace birthPlace = (PropertyPlace) birth.getProperty("PLAC");
        if (birthPlace != null) {
            if (birthPlace.getValue().length() > 0) {
                birthString += "#bp " + birthPlace.getValue().replaceAll(" |\n", "_") + " ";
            }
        }

        // [#bs BirthSource]
        if (sourcesExported == true) {
            Property birthSource = birth.getProperty("SOUR");
            if (birthSource != null) {
                birthString += "#bs " + source2String(birthSource);
            }
        }

        //  [!BaptizeDate]

        //  [#pp BaptizePlace]

        //  [#ps BaptizeSource]

        return (birthString);
    }

    /*
     * [DateOfDeath] [#dp PlaceOfDeath] [#ds DeathSource]
     * [#buri | #crem [BurialDate]] [#rp BurialPlace] [#rs BurialSource]
     */
    private String analyzeDeath(Property death) {

        String deathString = new String();

        // [DateOfDeath]
        PropertyDate deathDate = (PropertyDate) death.getProperty("DATE");
        if (deathDate != null) {
            deathString = analyzeDate(deathDate) + " ";
        } else {
            deathString = "0 ";
        }

        // [#dp PlaceOfDeath]
        PropertyPlace deathPlace = (PropertyPlace) death.getProperty("PLAC");
        if (deathPlace != null) {
            if (deathPlace.getValue().length() > 0) {
                deathString += "#dp " + deathPlace.getValue().replaceAll(" |\n", "_") + " ";
            }
        }

        // [#ds DeathSource]
        if (sourcesExported == true) {
            Property deathSource = death.getProperty("SOUR");
            if (deathSource != null) {
                deathString += "#ds " + source2String(deathSource) + " ";
            }
        }

        // [#buri | #crem [BurialDate]] [#rp BurialPlace] [#rs BurialSource]
        return (deathString);
    }

    private String date2String(PointInTime date) {
        String stringDate =
                new String();
        if (date.isValid()) {
            if (!date.getCalendar().equals(PointInTime.GREGORIAN)) {
                try {
                    date.set(PointInTime.GREGORIAN);
                    stringDate = null;
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                    return " 0";
                }
            }

            if (date.getYear() != PointInTime.UNKNOWN) {
                if (date.getMonth() != PointInTime.UNKNOWN) {
                    if (date.getDay() != PointInTime.UNKNOWN) {
                        stringDate = Integer.toString(date.getDay() + 1) + "/";
                    }
                    stringDate += Integer.toString(date.getMonth() + 1) + "/";
                }
                stringDate += Integer.toString(date.getYear());
            }
        } else {
            stringDate = "0";
        }

        return stringDate;
    }

    private String analyzeDate(PropertyDate date) {
        PropertyDate.Format dateFormat = date.getFormat();
        String startDate = date2String(date.getStart());
        String endDate = date2String(date.getEnd());

        if (dateFormat.equals(PropertyDate.DATE)) {
            return startDate;
        } else if (dateFormat.equals(PropertyDate.FROM_TO)) {
            return date.getStart().getYear() + ".." + date.getEnd().getYear();
        } else if (dateFormat.equals(PropertyDate.FROM)) {
            return ">" + date.getStart().getYear();
        } else if (dateFormat.equals(PropertyDate.TO)) {
            return "<" + date.getStart().getYear();
        } else if (dateFormat.equals(PropertyDate.BETWEEN_AND)) {
            return date.getStart().getYear() + ".." + date.getEnd().getYear();
        } else if (dateFormat.equals(PropertyDate.BEFORE)) {
            return "<" + date.getStart().getYear();
        } else if (dateFormat.equals(PropertyDate.AFTER)) {
            return ">" + date.getStart().getYear();
        } else if (dateFormat.equals(PropertyDate.ABOUT)) {
            return "~" + date.getStart().getYear();
        } else if (dateFormat.equals(PropertyDate.CALCULATED)) {
            return "~" + startDate;
        } else if (dateFormat.equals(PropertyDate.ESTIMATED)) {
            return "~" + startDate;
        } else if (dateFormat.equals(PropertyDate.INTERPRETED)) {
            return "~" + startDate;
        } else {
            return ("0");
        }
    }

    private String source2String(Property source) {
        String srcString = "";
        /*
         *   <SOUR>
         *       <PAGE/>
         *       <EVEN>
         *           <ROLE/>
         *       </EVEN>
         *       <DATA>
         *           <DATE/>
         *       <TEXT/>
         *       </DATA>
         *       <QUAY/>
         *       <OBJE>
         *           <TITL/>
         *           <FILE>
         *           <FORM/>
         *           </FILE>
         *           <NOTE/>
         *       </OBJE>
         *       <TEXT/>
         *       <NOTE/>
         *   </SOUR>
         */
        if (source instanceof PropertySource || source instanceof Source) {
            if (source instanceof PropertySource) {
                source = ((PropertySource) source).getTargetEntity();
            }
            srcString = ((Source) source).getTitle().replaceAll(" |\n", "_");
        } else {
            srcString = source.getValue().replaceAll(" |\n", "_");
        }

        if (source.getProperty("PAGE") != null) {
            srcString += "_" + source.getProperty("PAGE").getValue().replaceAll(" |\n", "_");
        }

        if (notesExported == true) {
            Property sourceNote = source.getProperty("NOTE");
            if (sourceNote != null) {
                if (sourceNote instanceof PropertyNote) {
                    sourceNote = ((PropertyNote) sourceNote).getTargetEntity();
                }
                String stringNote = source.getProperty("NOTE").getValue();
                srcString += "_" + stringNote.replaceAll(" |\n", "_");
            }
        }

        return srcString;
    }
}
