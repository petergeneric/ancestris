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
import genj.util.WordBuffer;
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
import java.util.Iterator;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.Exceptions;

public final class GeneanetExportAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        // Create the file chooser
        Context context;
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Geneanet Files", "gw");
        JFileChooser fc = new JFileChooser();

        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);
        if ((context = App.center.getSelectedContext(true)) != null) {
            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (file.exists() == false) {
                    String fileName = fc.getSelectedFile() + ".gw";
                    file = new File(fileName);
                }
                Gedcom myGedcom = context.getGedcom();
                try {
                    analyzeFam(myGedcom.getFamilies(), file);
                } catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (UnsupportedEncodingException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    void analyzeFam(Collection<Fam> familys, File file) throws FileNotFoundException, UnsupportedEncodingException, IOException, GedcomException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));

        Iterator familysIterator = familys.iterator();
        while (familysIterator.hasNext()) {
            Fam family = (Fam) familysIterator.next();

            /*
             * fam HusbandLastName FirstName[.Number]
             */
            out.write("fam");
            Indi Husband = family.getHusband();
            if (Husband != null) {
                if (Husband.getFamiliesWhereChild() == null) {
                    analyzeIndi((Indi) family.getHusband(), out);
                } else {
                    out.write(" " + Husband.getLastName().replaceAll(" ", "_"));
                    out.write(" " + Husband.getFirstName().replaceAll(" ", "_"));
                }
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
                    out.write(convertDate(marriageDate));
                }

                PropertyPlace MarriagePlace = (PropertyPlace) marriage.getProperty("PLAC");
                if (MarriagePlace != null && MarriagePlace.getValue().isEmpty() != true) {
                    if (MarriagePlace.getValue().length() > 0) {
                        out.write(" #mp " + MarriagePlace.getValue().replaceAll(" ", "_"));
                    }
                }

                PropertySource marriageSource = (PropertySource) marriage.getProperty("SOUR");
                if (marriageSource != null) {
                    Source entitySource = (Source) marriageSource.getTargetEntity();
                    out.write(" #ms " + entitySource.getTitle().replaceAll(" ", "_"));
                }

                /*
                 * [#sep | - DivorceDate]
                 *
                 */
                Property divorce = family.getProperty("DIV");
                if (divorce != null) {
                    PropertyDate divorceDate = (PropertyDate) divorce.getProperty("DATE");
                    if (divorceDate != null) {
                        out.write("- " + convertDate(divorceDate));
                    } else {
                        out.write("- 0");
                    }
                }
            } else {
                /*
                 * not married
                 * [#nm | #eng]
                 *
                 */
                out.write(" #nm");
            }

            /*
             * WifeLastName FirstName[.Number]
             *
             */
            Indi Wife = family.getWife();
            if (Wife != null) {
                if (family.getWife().getFamiliesWhereChild() == null) {
                    analyzeIndi((Indi) family.getWife(), out);
                } else {
                    out.write(" " + Wife.getLastName().replaceAll(" ", "_"));
                    out.write(" " + Wife.getFirstName().replaceAll(" ", "_"));
                }
            }

            out.write("\n");

            /*
             * [src Family source]
             */

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
                            out.write("- f");
                            break;

                        case PropertySex.MALE:
                            out.write("- h");
                            break;

                        default:
                            out.write("-");
                            break;
                    }
                    analyzeIndi(children, out);
                }
                out.write("end\n");
            }
            out.write("\n");
        }
        out.close();
    }

    void analyzeIndi(Indi indi, BufferedWriter out) throws IOException, GedcomException {
        /*
         * LastName FirstName [{FirstNameAlias}] [#salias SurnameAlias]
         * [(PublicName)] [#image ImageFilePath] [#nick Qualifier] [#alias Alias]
         */
        PropertyName pIndiName = (PropertyName) indi.getProperty("NAME");
        if (pIndiName != null) {
            out.write(" " + pIndiName.getLastName().replaceAll(" ", "_"));
            out.write(" " + pIndiName.getFirstName().replaceAll(" ", "_"));
        }

        /*
         * [Titles (see Title section)]
         */

        /*
         * [#apubl | #apriv]
         */
        if (indi.isPrivate() == true) {
            out.write(" #apriv");
        } else {
            out.write(" #apubl");
        }

        /*
         * [#occu Occupation]
         */

        /*
         * [#src PersonSource]
         */

        /*
         * DateOfBirth [#bs BirthSource] [#bp PlaceOfBirth] [!BaptizeDate]
         * [#pp BaptizePlace] [#ps BaptizeSource]
         */
        Property birth = indi.getProperty("BIRT");
        if (birth != null) {
            PropertyDate birthDate = (PropertyDate) birth.getProperty("DATE");
            if (birthDate != null) {
                out.write(" " + convertDate(birthDate));
            } else {
                out.write(" 0");
            }

            PropertySource birthSource = (PropertySource) birth.getProperty("SOUR");
            if (birthSource != null) {
                Source entitySource = (Source) birthSource.getTargetEntity();
                out.write(" #bs " + entitySource.getTitle().replaceAll(" ", "_"));
            }

            PropertyPlace birthPlace = (PropertyPlace) birth.getProperty("PLAC");
            if (birthPlace != null) {
                if (birthPlace.getValue().length() > 0) {
                    out.write(" #bp " + birthPlace.getValue().replaceAll(" ", "_"));
                }
            }
        }

        /*
         * [DateOfDeath] [#dp PlaceOfDeath] [#ds DeathSource]
         * [#buri | #crem [BurialDate]] [#rp BurialPlace] [#rs BurialSource]
         */ if (indi.isDeceased()) {
            Property death = indi.getProperty("DEATH");
            if (death != null) {
                PropertyDate deathDate = (PropertyDate) death.getProperty("DATE");
                if (deathDate != null) {
                    out.write(" " + convertDate(deathDate));
                } else {
                    out.write(" 0");
                }

                PropertyPlace deathPlace = (PropertyPlace) death.getProperty("PLAC");
                if (deathPlace != null) {
                    if (deathPlace.getValue().length() > 0) {
                        out.write(" #dp " + deathPlace.getValue().replaceAll(" ", "_"));
                    }
                }

                PropertySource deathSource = (PropertySource) death.getProperty("SOUR");
                if (deathSource != null) {
                    Source entitySource = (Source) deathSource.getTargetEntity();
                    out.write(" #ds " + entitySource.getTitle().replaceAll(" ", "_"));
                }
            }
        }

        out.write("\n");
    }

    String convertDate(PropertyDate date) throws GedcomException {
        PropertyDate.Format dateFormat = date.getFormat();

        if (dateFormat.equals(PropertyDate.DATE)) {
            PointInTime pit = date.getStart();
            if (!pit.getCalendar().equals(PointInTime.GREGORIAN)) {
                pit.set(PointInTime.GREGORIAN);
            }
            return pit.toString(new WordBuffer(), PointInTime.FORMAT_NUMERIC).toString();
        } else if (dateFormat.equals(PropertyDate.FROM_TO)) {
            String stringBuffer = null;
            PointInTime start = date.getStart();
            if (!start.getCalendar().equals(PointInTime.GREGORIAN)) {
                start.set(PointInTime.GREGORIAN);
            }
            stringBuffer = start.toString(new WordBuffer(), PointInTime.FORMAT_NUMERIC).toString();
            PointInTime end = date.getEnd();
            if (!end.getCalendar().equals(PointInTime.GREGORIAN)) {
                end.set(PointInTime.GREGORIAN);
            }
            stringBuffer += end.toString(new WordBuffer(), PointInTime.FORMAT_NUMERIC).toString();
            return stringBuffer;
        } else if (dateFormat.equals(PropertyDate.FROM)) {
            PointInTime pit = date.getStart();
            if (!pit.getCalendar().equals(PointInTime.GREGORIAN)) {
                pit.set(PointInTime.GREGORIAN);
            }
            return ">" + pit.toString(new WordBuffer(), PointInTime.FORMAT_NUMERIC).toString();
        } else if (dateFormat.equals(PropertyDate.TO)) {
            PointInTime pit = date.getStart();
            if (!pit.getCalendar().equals(PointInTime.GREGORIAN)) {
                pit.set(PointInTime.GREGORIAN);
            }
            return "<" + pit.toString(new WordBuffer(), PointInTime.FORMAT_NUMERIC).toString();
        } else if (dateFormat.equals(PropertyDate.BETWEEN_AND)) {
            String stringBuffer = null;
            PointInTime start = date.getStart();
            if (!start.getCalendar().equals(PointInTime.GREGORIAN)) {
                start.set(PointInTime.GREGORIAN);
            }
            stringBuffer = start.toString(new WordBuffer(), PointInTime.FORMAT_NUMERIC).toString();
            PointInTime end = date.getEnd();
            if (!end.getCalendar().equals(PointInTime.GREGORIAN)) {
                end.set(PointInTime.GREGORIAN);
            }
            stringBuffer += end.toString(new WordBuffer(), PointInTime.FORMAT_NUMERIC).toString();
            return stringBuffer;
        } else if (dateFormat.equals(PropertyDate.BEFORE)) {
            PointInTime pit = date.getStart();
            if (!pit.getCalendar().equals(PointInTime.GREGORIAN)) {
                pit.set(PointInTime.GREGORIAN);
            }
            return "<" + pit.toString(new WordBuffer(), PointInTime.FORMAT_NUMERIC).toString();
        } else if (dateFormat.equals(PropertyDate.AFTER)) {
            PointInTime pit = date.getStart();
            if (!pit.getCalendar().equals(PointInTime.GREGORIAN)) {
                pit.set(PointInTime.GREGORIAN);
            }
            return ">" + pit.toString(new WordBuffer(), PointInTime.FORMAT_NUMERIC).toString();
        } else if (dateFormat.equals(PropertyDate.ABOUT)) {
            PointInTime pit = date.getStart();
            if (!pit.getCalendar().equals(PointInTime.GREGORIAN)) {
                pit.set(PointInTime.GREGORIAN);
            }
            return "~" + pit.toString(new WordBuffer(), PointInTime.FORMAT_NUMERIC).toString();
        } else if (dateFormat.equals(PropertyDate.CALCULATED)) {
            PointInTime pit = date.getStart();
            if (!pit.getCalendar().equals(PointInTime.GREGORIAN)) {
                pit.set(PointInTime.GREGORIAN);
            }
            return "~" + pit.toString(new WordBuffer(), PointInTime.FORMAT_NUMERIC).toString();
        } else if (dateFormat.equals(PropertyDate.ESTIMATED)) {
            PointInTime pit = date.getStart();
            if (!pit.getCalendar().equals(PointInTime.GREGORIAN)) {
                pit.set(PointInTime.GREGORIAN);
            }
            return "~" + pit.toString(new WordBuffer(), PointInTime.FORMAT_NUMERIC).toString();
        } else if (dateFormat.equals(PropertyDate.INTERPRETED)) {
            PointInTime pit = date.getStart();
            if (!pit.getCalendar().equals(PointInTime.GREGORIAN)) {
                pit.set(PointInTime.GREGORIAN);
            }
            return "~" + pit.toString(new WordBuffer(), PointInTime.FORMAT_NUMERIC).toString();
        }

        return ("0");

    }
}
