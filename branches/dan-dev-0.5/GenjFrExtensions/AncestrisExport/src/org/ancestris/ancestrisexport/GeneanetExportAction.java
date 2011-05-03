package org.ancestris.ancestrisexport;

import genj.gedcom.Context;
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
import genjfr.app.App;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class GeneanetExportAction implements ActionListener {

    private class GwIndi {

        private boolean alreadyDescribed = false;
        private String firstName = null;
        private String lastName = null;
        private int occurence = 0;
        private String description = null;
        private String notes = null;

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

        void setAlreadyDescribed() {
            alreadyDescribed = true;
        }

        public boolean isDescribed() {
            return alreadyDescribed;
        }
    }
    Map<String, Integer> indiNameOccurence = new HashMap<String, Integer>();
    Map<String, GwIndi> indiMap = new HashMap<String, GwIndi>();

    @Override
    public void actionPerformed(ActionEvent e) {
        // Create the file chooser
        Context context;
        final FileNameExtensionFilter filter = new FileNameExtensionFilter(NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.fileType"), "gw");
        JFileChooser fc = new JFileChooser() {

            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(this, NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.Overwrite.Text"), NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.Overwrite.Title"), JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            super.cancelSelection();
                            return;
                    }
                } else {
                    if (filter.accept(f) == false) {
                        setSelectedFile(new File(f.getName() + ".gw"));
                    }
                }
                super.approveSelection();
            }
        };

        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);

        indiNameOccurence.clear();
        indiMap.clear();

        if ((context = App.center.getSelectedContext(true)) != null) {
            Gedcom myGedcom = context.getGedcom();
            fc.setSelectedFile(new File(removeExtension(myGedcom.getOrigin().getFile().toString()) + ".gw"));

            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                analyzeIndis(myGedcom.getIndis());
                analyzeFam(myGedcom.getFamilies(), file);
            }
        }
    }

    void analyzeFam(Collection<Fam> familys, File file) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));

            out.write("encoding: utf-8\n\n");
            Iterator familysIterator = familys.iterator();
            while (familysIterator.hasNext()) {
                Fam family = (Fam) familysIterator.next();

                /*
                 * fam HusbandLastName FirstName[.Number]
                 */
                out.write("fam ");
                Indi husband = family.getHusband();
                if (husband != null) {
                    /*
                     *  LastName FirstName.Occurence
                     */
                    out.write(indiMap.get(husband.getId()).getNameOccurenced() + " ");
                    Fam[] husbandFamc = husband.getFamiliesWhereChild();
                    if (husbandFamc.length == 0) {
                        GwIndi gwIndi = indiMap.get(husband.getId());
                        if (gwIndi.isDescribed() == false) {
                            gwIndi.setAlreadyDescribed();
                            indiMap.put(husband.getId(), gwIndi);
                            out.write(gwIndi.getDescription() + " ");
                        }
                    }
                } else {
                    out.write("? ? ");
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
                            out.write("#mp " + marriagePlace.getValue().replaceAll(" ", "_") + " ");
                        }
                    }

                    Property marriageSource = marriage.getProperty("SOUR");
                    if (marriageSource != null) {
                        out.write("#ms " + source2String(marriageSource) + " ");
                    }

                    /*
                     * [#sep | - DivorceDate]
                     *
                     */
                    Property divorce = family.getProperty("DIV");
                    if (divorce != null) {
                        PropertyDate divorceDate = (PropertyDate) divorce.getProperty("DATE");
                        if (divorceDate != null) {
                            out.write("-" + analyzeDate(divorceDate) + " ");
                        } else {
                            out.write("-0 ");
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
                Indi wife = family.getWife();
                if (wife != null) {
                    /*
                     *  LastName FirstName.Occurence
                     */
                    out.write(indiMap.get(wife.getId()).getNameOccurenced());
                    Fam[] wifeFamc = wife.getFamiliesWhereChild();
                    if (wifeFamc.length == 0) {
                        GwIndi gwIndi = indiMap.get(wife.getId());
                        if (gwIndi.isDescribed() == false) {
                            gwIndi.setAlreadyDescribed();
                            indiMap.put(wife.getId(), gwIndi);
                            out.write(" " + gwIndi.getDescription());
                        }
                    }
                } else {
                    out.write("? ?");
                }

                out.write("\n");

                /*
                 * [src Family source]
                 */
                Property familySource = family.getProperty("SOUR");
                if (familySource != null) {
                    out.write("src " + source2String(familySource) + "\n");
                }

                /*
                 * [comm Family comments in free format]
                 */
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

                /*
                 * [wit: Witness (use Person format, see Person Information section) ]
                 */
                if (marriage != null) {
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
                        out.write("\n");
                    }
                    out.write("end\n");
                }
                out.write("\n");
            }

            /*
             * indi Notes
             * notes LastName FirstName[.Number]
             * beg
             * Notes go here in a free format
             * end notes
             */
            Iterator it = indiMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                GwIndi indi = (GwIndi) entry.getValue();
                if (indi.getNotes() != null) {
                    out.write("notes " + indi.getNameOccurenced() + "\n");
                    out.write("beg\n" + indi.getNotes() + "\nend notes\n");
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
    }

    private void analyzeIndis(Collection<Indi> indis) {
        for (Iterator<Indi> indisIterator = indis.iterator(); indisIterator.hasNext();) {
            Indi indi = indisIterator.next();
            String firstName = null;
            String lastName = null;

            Property[] pIndiNames = (Property[]) indi.getProperties("NAME");
            if (pIndiNames.length > 0) {
                // extract First and last Name of the first property
                // name found for key generation
                if (((PropertyName) pIndiNames[0]).getLastName().length() > 0) {
                    lastName = ((PropertyName) pIndiNames[0]).getLastName().replaceAll(" ", "_");
                } else {
                    lastName = " ?";
                }
                Property pGivenName = pIndiNames[0].getProperty("GIVN");
                if (pGivenName != null) {
                    firstName = pGivenName.getValue();
                } else if (((PropertyName) pIndiNames[0]).getFirstName().length() > 0) {

                    firstName = ((PropertyName) pIndiNames[0]).getFirstName();
                    int index = firstName.indexOf(" ");
                    if (index > 0) {
                        firstName = firstName.substring(0, index);
                    } else {
                    }
                } else {
                    firstName = " ?";
                }
            } else {
                firstName = "?";
                lastName = "?";
            }

            // create the Key
            String indiKey = null;
            String tmpString = (lastName.replaceAll("-", "_") + "_" + firstName.replaceAll("-", "_")).toLowerCase();
            try {
                byte[] byteString = tmpString.getBytes("US-ASCII");
                indiKey = new String(byteString);
            } catch (UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
            }
            Integer NameOccurence = indiNameOccurence.get(indiKey);
            indiNameOccurence.put(indiKey, (NameOccurence == null) ? 1 : NameOccurence + 1);
            GwIndi gwIndi = null;
            if (NameOccurence == null) {
                gwIndi = new GwIndi(lastName, firstName, 0);
            } else {
                gwIndi = new GwIndi(lastName, firstName, NameOccurence);
            }

            gwIndi.setDescription(analyzeIndi(indi));

            /*
             * indi Notes
             */
            Property[] indiNotes = indi.getProperties("NOTE");
            if (indiNotes.length > 0) {
                String notes = "";
                // PropertyNote | PropertyMultilineValue
                for (Property note : indiNotes) {
                    boolean first = true;
                    if (note instanceof PropertyNote) {
                        note = ((PropertyNote) note).getTargetEntity();
                    }
                    if (first == true) {
                        first = false;
                        notes = note.getValue().replaceAll("\n", " ");
                    } else {
                        notes += "<br>" + note.getValue().replaceAll("\n", " ");
                    }
                }

                gwIndi.setNotes(notes);
            }
            indiMap.put(indi.getId(), gwIndi);
       }
    }

    String analyzeIndi(Indi indi) {
        String indiDescription = "";

        Property[] indiNames = indi.getProperties("NAME");
        if (indiNames != null) {
            indiDescription += analyseName(indiNames);
        }

        /*
         * [Titles (see Title section)]
         */

        /*
         * [#apubl | #apriv]
         */
        if ((indi.isPrivate() == true)) {
            indiDescription += " #apriv ";
        } else {
            indiDescription += " #apubl ";
        }

        /*
         * [#occu Occupation]
         */
        Property[] indiOccu = indi.getProperties("OCCU");
        if (indiOccu != null) {

            String occuString = new String();

            for (Property occu : indiOccu) {

                if (occuString.length() > 0) {
                    occuString += "_" + occu.getDisplayValue().replaceAll(" ", "_");
                } else {
                    occuString = occu.getDisplayValue().replaceAll(" ", "_");
                }

                Property[] occuProperties = occu.getProperties();
                for (Property occuProperty : occuProperties) {
                    occuProperty.getValue();
                    if (occuString.length() > 0) {
                        occuString += "_" + occuProperty.getDisplayValue().replaceAll(" ", "_");
                    } else {
                        occuString = occuProperty.getDisplayValue().replaceAll(" ", "_");
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
        Property indiSource = indi.getProperty("SOUR");
        if (indiSource != null) {
            indiDescription += "#src " + source2String(indiSource) + " ";
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

    /*
     *  
     * LastName FirstName [{FirstNameAlias}] [#salias SurnameAlias] [(PublicName)]
     * [#image ImageFilePath] [#nick Qualifier] [#alias Alias]
     * 
     * NB LastName FirstName already inserted by analyseFamily.
     */
    String analyseName(Property[] indiNames) {
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
            if (indiname.getFirstName().length() <= 0) {
                nameDescription += "#salias " + indiname.getLastName().replaceAll(" ", "_") + " ";
            }
        }

        /*
         * Extract [(PublicName)]  from the fisrt PropertyName found
         */
        if (indiNames[0].getProperty("NICK") != null) {
            nameDescription += "(" + indiNames[0].getProperty("NICK").getValue().replaceAll(" ", "_") + ") ";
        }

        /*
         * [#image ImageFilePath] [#nick Qualifier]
         */

        /*
         * loop over other Propertyname and extract [#alias Alias]
         */
        for (int index = 1; index < indiNames.length; index++) {
            PropertyName indiname = (PropertyName) indiNames[index];
            if (indiname.getFirstName().length() > 0) {
                nameDescription += "#alias" + indiname.getName().replaceAll(" ", "_") + " ";
            }
        }
        return nameDescription;

    }

    /*
     * DateOfBirth [#bs BirthSource] [#bp PlaceOfBirth] [!BaptizeDate]
     * [#pp BaptizePlace] [#ps BaptizeSource]
     */
    String analyzeBirth(
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
                birthString += "#bp " + birthPlace.getValue().replaceAll(" ", "_") + " ";
            }
        }

        // [#bs BirthSource]
        Property birthSource = birth.getProperty("SOUR");
        if (birthSource != null) {
            birthString += "#bs " + source2String(birthSource);
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
    String analyzeDeath(Property death) {

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
                deathString += "#dp " + deathPlace.getValue().replaceAll(" ", "_") + " ";
            }
        }

        // [#ds DeathSource]
        Property deathSource = death.getProperty("SOUR");
        if (deathSource != null) {
            deathString += "#ds " + source2String(deathSource) + " ";
        }

        // [#buri | #crem [BurialDate]] [#rp BurialPlace] [#rs BurialSource]
        return (deathString);
    }

    String date2String(PointInTime date) {
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

    String analyzeDate(PropertyDate date) {
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

    private String removeExtension(String filename) {

        String separator = System.getProperty("file.separator");

        // Remove the path upto the filename.
        int lastSeparatorIndex = filename.lastIndexOf(separator);
        if (lastSeparatorIndex != -1) {
            filename = filename.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1) {
            return filename;
        }

        return filename.substring(0, extensionIndex);
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
            srcString = ((Source) source).getTitle().replaceAll(" ", "_");
        } else {
            srcString = source.getValue().replaceAll(" ", "_");
        }

        if (source.getProperty("PAGE") != null) {
            srcString += "_" + source.getProperty("PAGE").getValue().replaceAll(" ", "_");
        }

        Property sourceNote = source.getProperty("NOTE");
        if (sourceNote != null) {
            if (sourceNote instanceof PropertyNote) {
                sourceNote = ((PropertyNote) sourceNote).getTargetEntity();
            }
            String stringNote = source.getProperty("NOTE").getValue();
            srcString += "_" + stringNote.replaceAll(" |\n", "_");
        }

        return srcString;
    }
}
