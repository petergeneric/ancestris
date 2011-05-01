package org.ancestris.ancestrisexport;

import genj.gedcom.Context;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertySource;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JFileChooser;
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

        public GwIndi() {
        }

        private GwIndi(String lastName, String firstName, int occurence) {
            this.lastName = lastName;
            this.firstName = firstName;
            this.occurence = occurence;
        }

        public String getFullName() {
            return lastName + " " + firstName;
        }

        public String getFullNameOccurenced() {
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
        FileNameExtensionFilter filter = new FileNameExtensionFilter(NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.fileType"), "gw");
        JFileChooser fc = new JFileChooser();

        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);

        indiNameOccurence.clear();
        indiMap.clear();

        if ((context = App.center.getSelectedContext(true)) != null) {
            Gedcom myGedcom = context.getGedcom();
            fc.setSelectedFile(new File(removeExtension(myGedcom.getOrigin().getFile().toString()) + ".gw"));

            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (file.exists() == false) {
                    String fileName = fc.getSelectedFile() + ".gw";
                    file = new File(fileName);
                }
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
                    out.write(indiMap.get(husband.getId()).getFullNameOccurenced() + " ");
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
                 * +[WeddingDate] [#mp WeddingPlace] [#ms WeddingSource]
                 *
                 */
                out.write("+");
                Property marriage = family.getProperty("MARR");
                if (marriage != null) {
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
                            out.write("- 0 ");
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
                    out.write(indiMap.get(wife.getId()).getFullNameOccurenced());
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

                /*
                 * [wit: Witness (use Person format, see Person Information section) ]
                 */
                if (marriage != null) {
                    Property[] witnesses = marriage.getProperties("RELA");
                    if (witnesses.length > 0) {
                        for (Property witness : witnesses) {
                            out.write("wit: " + witness.getDisplayValue() + "\n");
                        }

                    }
                }

                /*
                 * beg
                 * - [h | f | ] Person (see detailed description at the next section)
                 * end
                 */
                Collection<Indi> childrens = Arrays.asList(family.getChildren());
                Iterator childrensIterator = childrens.iterator();
                if (childrensIterator.hasNext()) {
                    out.write("beg\n");
                    while (childrensIterator.hasNext()) {
                        Indi children = (Indi) childrensIterator.next();
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
            String indiKey;
            Indi indi = indisIterator.next();
            GwIndi gwindi;
            String firstName = null;
            String lastName = null;

            PropertyName pIndiName = (PropertyName) indi.getProperty("NAME");
            if (pIndiName != null) {
                if (pIndiName.getLastName().length() > 0) {
                    lastName = pIndiName.getLastName().replaceAll(" ", "_");
                } else {
                    lastName = " ?";
                }
                if (pIndiName.getFirstName().length() > 0) {
                    firstName = pIndiName.getFirstName().replaceAll(" ", "_");
                } else {
                    firstName = " ?";
                }
            } else {
                firstName = "?";
                lastName = "?";
            }

            indiKey = (lastName.replaceAll("-", "_") + "_" + firstName.replaceAll("-", "_")).toLowerCase();
            Integer NameOccurence = indiNameOccurence.get(indiKey);
            indiNameOccurence.put(indiKey, (NameOccurence == null) ? 1 : NameOccurence + 1);
            GwIndi gwIndi = null;
            if (NameOccurence == null) {
                gwIndi = new GwIndi(lastName, firstName, 0);
            } else {
                gwIndi = new GwIndi(lastName, firstName, NameOccurence);
            }

            gwIndi.setDescription(analyzeIndi(indi));
            indiMap.put(indi.getId(), gwIndi);
        }
    }

    String analyzeIndi(Indi indi) {
        String indiDescription = "";
        /*
         * [{FirstNameAlias}] [#salias SurnameAlias] [(PublicName)]
         * [#image ImageFilePath] [#nick Qualifier] [#alias Alias]
         */
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
        if (indi.getProperty("NICK") != null) {
            indiDescription += "{" + indi.getProperty("NICK").getValue().replaceAll(" ", "_") + "}";
        }
        
        /*
         * [Titles (see Title section)]
         */

        /*
         * [#apubl | #apriv]
         */
        if (indi.isPrivate() == true) {
            indiDescription += "#apriv ";
        } else {
            indiDescription += "#apubl ";
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

        Property birth = indi.getProperty("BIRT");
        if (birth != null) {
            indiDescription += analyzeBirth(birth) + " ";
        }

        Property death = indi.getProperty("DEAT");
        if (death != null) {
            indiDescription += analyzeDeath(death) + " ";
        }

        return indiDescription;
    }

    /*
     * DateOfBirth [#bs BirthSource] [#bp PlaceOfBirth] [!BaptizeDate]
     * [#pp BaptizePlace] [#ps BaptizeSource]
     */
    String analyzeBirth(Property birth) {

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
            return startDate + ".." + endDate;
        } else if (dateFormat.equals(PropertyDate.FROM)) {
            return ">" + startDate;
        } else if (dateFormat.equals(PropertyDate.TO)) {
            return "<" + startDate;
        } else if (dateFormat.equals(PropertyDate.BETWEEN_AND)) {
            return startDate + ".." + endDate;
        } else if (dateFormat.equals(PropertyDate.BEFORE)) {
            return "<" + startDate;
        } else if (dateFormat.equals(PropertyDate.AFTER)) {
            return ">" + startDate;
        } else if (dateFormat.equals(PropertyDate.ABOUT)) {
            return "~" + startDate;
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
        Property src = null;
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
        if (source instanceof PropertySource) {
            src = ((PropertySource) source).getTargetEntity();
            srcString = ((Source) src).getTitle().replaceAll(" ", "_");
        } else if (source instanceof Source) {
            src = source;
            srcString = ((Source) src).getTitle().replaceAll(" ", "_");
        } else {
            src = source;
            srcString = source.getValue().replaceAll(" ", "_");
        }

        if (src.getProperty("PAGE") != null) {
            srcString += src.getProperty("PAGE").getValue().replaceAll(" ", "_");
        }

        if (src.getProperty("NOTE") != null) {
            srcString += src.getProperty("NOTE").getValue().replaceAll(" ", "_");
        }

        return srcString;
    }
}
