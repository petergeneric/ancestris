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

    Map<String, Integer> indiNameOccurence = new HashMap<String, Integer>();
    Map<String, String> indiMap = new HashMap<String, String>();

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
                try {
                    analyzeIndi(myGedcom.getIndis());
                    analyzeFam(myGedcom.getFamilies(), file);
                } catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (UnsupportedEncodingException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    void analyzeFam(Collection<Fam> familys, File file) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));

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
                out.write(indiMap.get(husband.getId()));
                if (husband.getFamiliesWhereChild() == null) {
                    analyzeIndi(family.getHusband(), out);
                }
            } else {
                out.write("? ?");
            }

            /*
             * +[WeddingDate] [#mp WeddingPlace] [#ms WeddingSource]
             *
             */
            out.write(" +");
            Property marriage = family.getProperty("MARR");
            if (marriage != null) {
                PropertyDate marriageDate = (PropertyDate) marriage.getProperty("DATE");
                if (marriageDate != null) {
                    out.write(analyzeDate(marriageDate) + " ");
                } else {
                    out.write("0 ");
                }

                /*
                 * [#sep | - DivorceDate]
                 *
                 */
                /*                Property divorce = family.getProperty("DIV");
                if (divorce != null) {
                PropertyDate divorceDate = (PropertyDate) divorce.getProperty("DATE");
                if (divorceDate != null) {
                out.write("- " + analyzeDate(divorceDate) + " ");
                } else {
                out.write("- 0 ");
                }
                }
                 */
                PropertyPlace marriagePlace = (PropertyPlace) marriage.getProperty("PLAC");
                if (marriagePlace != null && marriagePlace.getValue().isEmpty() != true) {
                    if (marriagePlace.getValue().length() > 0) {
                        out.write("#mp " + marriagePlace.getValue().replaceAll(" ", "_") + " ");
                    }
                }

                PropertySource marriageSource = (PropertySource) marriage.getProperty("SOUR");
                if (marriageSource != null) {
                    Source entitySource = (Source) marriageSource.getTargetEntity();
                    out.write("#ms " + entitySource.getTitle().replaceAll(" ", "_") + " ");
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
                out.write(indiMap.get(wife.getId()));
                if (family.getWife().getFamiliesWhereChild() == null) {
                    analyzeIndi(family.getWife(), out);
                }
            } else {
                out.write("? ?");
            }

            out.write("\n");

            /*
             * [src Family source]
             */
            PropertySource familyPropertySource = (PropertySource) family.getProperty("SOUR");
            if (familyPropertySource != null) {
                Source familySource = (Source) familyPropertySource.getTargetEntity();
                out.write("src " + familySource.getTitle().replaceAll(" ", "_") + "\n");
            }

            /*
             * [comm Family comments in free format]
             */

            /*
             * [wit: Witness (use Person format, see Person Information section) ]
             */

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
                        if (children.getLastName().equals(husband.getLastName())) {
                            /*
                             * FirstName.Occurence
                             */
                            String childFullName = indiMap.get(children.getId());
                            out.write(childFullName.substring(childFullName.indexOf(' ') + 1) + " ");
                        } else {
                            /*
                             *  LastName FirstName.Occurence
                             */
                            out.write(indiMap.get(children.getId()) + " ");
                        }
                    } else {
                        if (children.getLastName().equals(wife.getLastName())) {
                            /*
                             * FirstName.Occurence
                             */
                            String childFullName = indiMap.get(children.getId());
                            out.write(childFullName.substring(childFullName.indexOf(' ')) + " ");
                        } else {
                            /*
                             *  LastName FirstName.Occurence
                             */
                            out.write(indiMap.get(children.getId()) + " ");
                        }
                    }
                    analyzeIndi(children, out);
                }
                out.write("end\n");
            }
            out.write("\n");
        }
        out.close();
    }

    private void analyzeIndi(Collection<Indi> indis) {
        for (Iterator<Indi> indisIterator = indis.iterator(); indisIterator.hasNext();) {
            String indiKey;
            Indi indi = indisIterator.next();

            PropertyName pIndiName = (PropertyName) indi.getProperty("NAME");
            if (pIndiName != null) {
                if (pIndiName.getLastName().length() > 0) {
                    indiKey = pIndiName.getLastName().replaceAll(" ", "_");
                } else {
                    indiKey = " ?";
                }
                if (pIndiName.getFirstName().length() > 0) {
                    indiKey += " " + pIndiName.getFirstName().replaceAll(" ", "_");
                } else {
                    indiKey += " ?";
                }
            } else {
                indiKey = "? ?";
            }

            Integer NameOccurence = indiNameOccurence.get(indiKey.toLowerCase());
            indiNameOccurence.put(indiKey.toLowerCase(), (NameOccurence == null) ? 1 : NameOccurence + 1);

            if (NameOccurence == null) {
                indiMap.put(indi.getId(), indiKey);
            } else {
                indiMap.put(indi.getId(), (indiKey + "." + NameOccurence));
            }
        }
    }

    void analyzeIndi(Indi indi, BufferedWriter out) throws IOException {
        /*
         * [{FirstNameAlias}] [#salias SurnameAlias] [(PublicName)]
         * [#image ImageFilePath] [#nick Qualifier] [#alias Alias]
         */

        /*
         * [Titles (see Title section)]
         */

        /*
         * [#apubl | #apriv]
         */
        if (indi.isPrivate() == true) {
            out.write("#apriv ");
        } else {
            out.write("#apubl ");
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
            }
            if (occuString.length() > 0) {
                out.write("#occu " + occuString + " ");
            }
        }

        /*
         * [#src PersonSource]
         */
        PropertySource indiPropertySource = (PropertySource) indi.getProperty("SOUR");
        if (indiPropertySource != null) {
            Source indiSource = (Source) indiPropertySource.getTargetEntity();
            out.write("#src " + indiSource.getTitle().replaceAll(" ", "_") + " ");
        }

        Property birth = indi.getProperty("BIRT");
        if (birth != null) {
            out.write(analyzeBirth(birth) + " ");
        }

        Property death = indi.getProperty("DEATH");
        if (death != null) {
            out.write(analyzeDeath(death) + " ");
        }

        out.write("\n");
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

        // [#bs BirthSource]
        PropertySource birthSource = (PropertySource) birth.getProperty("SOUR");
        if (birthSource != null) {
            Source entitySource = (Source) birthSource.getTargetEntity();
            birthString += "#bs " + entitySource.getValue().replaceAll(" ", "_") + " ";
        }

        // [#bp PlaceOfBirth]
        PropertyPlace birthPlace = (PropertyPlace) birth.getProperty("PLAC");
        if (birthPlace != null) {
            if (birthPlace.getValue().length() > 0) {
                birthString += "#bp " + birthPlace.getValue().replaceAll(" ", "_");
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
        PropertySource deathSource = (PropertySource) death.getProperty("SOUR");
        if (deathSource != null) {
            Source entitySource = (Source) deathSource.getTargetEntity();
            deathString += "#ds " + entitySource.getTitle().replaceAll(" ", "_");
        }

        // [#buri | #crem [BurialDate]] [#rp BurialPlace] [#rs BurialSource]
        return (deathString);
    }

    String date2String(PointInTime date) {
        String stringDate = new String();
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
}
