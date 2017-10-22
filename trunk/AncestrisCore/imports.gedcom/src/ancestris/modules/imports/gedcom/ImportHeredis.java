/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.imports.gedcom;

import ancestris.api.imports.Import;
import static ancestris.modules.imports.gedcom.Bundle.importheredis_name;
import static ancestris.modules.imports.gedcom.Bundle.importheredis_note;
import static ancestris.util.swing.FileChooserBuilder.getExtension;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import java.util.HashMap;

/**
 * The import function for Heredis originated Gedcom files
 */
@NbBundle.Messages({
    "importheredis.name=Heredis",
    "importheredis.note=This file has been modified by Ancestris Heredis Import module."
})
@ServiceProvider(service = Import.class)
public class ImportHeredis extends Import {

    private boolean repoOK = false;
    private static int clerepo = 0;
    private static final HashMap<String, Integer> hashrepo = new HashMap<String, Integer>();
    private static final StringBuilder sb = new StringBuilder();

    /**
     * Constructor
     */
    public ImportHeredis() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importheredis_name();
    }

    @Override
    protected String getImportComment() {
        return importheredis_note();
    }

    ///////////////////////////// START OF LOGIC ///////////////////////////////
    
    /**
     * *** 0 *** Initialisation of variables
     */
    protected void init() {
        super.init();
    }

    
    
    /**
     * **** 1 ***
     * - Run generic code
     * - Prepare REPO
     */
    @Override
    protected void firstPass() {
        super.firstPass();
        firstPassRepo();
    }

    /**
     * *** 2 ***
     * - Run generic code
     * - Etc
     * 
     */
    @Override
    protected boolean process() throws IOException {
        // Overwrite version
        if ((input.getLevel() == 2) && (input.getTag().equals("VERS") && (input.getValue().startsWith("5.5")))) {
            GEDCOM_VERSION = "5.5.1"; // Heredis (2017) seems to consider 5.5.1 tags as being within 5.5 (WWW, OBJE:FILE, etc.), so force gedcom version to 5.5.1
            output.writeLine(2, "VERS", GEDCOM_VERSION);
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixVersion", GEDCOM_VERSION));
            return true;
        }
        
        // Web address
        if ((input.getLevel() == 3) && (input.getTag().equals("WEB"))) {
            output.writeLine(3, "WWW", "http://" + input.getValue());
            return true;
        }
        
        // Remove non diacritic characters from SURN
        if ((input.getLevel() == 2) && (input.getTag().equals("SURN"))) {
            output.writeLine(2, "SURN", input.getValue().replaceAll("\\(", ",\\(").replaceAll(",+", ", ").replaceAll(", \\Z", ""));
            return true;
        }
        
        if (processAddr()) {
            return true;
        }
        if (processRepo()) {
            return true;
        }
        if (super.process()) {
            return true;
        }
        if (processFrenchRepHeredis()) {
            return true;
        }
        if (processTagNotAllowed()) {
            return true;
        }
        return false;
    }

    
    /**
     * *** 3 ***
     * - Run generic code
     * - Write unused places to NOTEs @Pxxx@, using only user-defined tags
     * 
     */
    
    @Override
    protected void finalise() throws IOException {
        super.finalise();
        finaliseRepo();
    }

    
    /**
     * *** 4 ***
     * - Run generic code
     * - <run specific code>
     * - Quit *after* all have been run
     */
    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        boolean ret = super.fixGedcom(gedcom);
        ret |= processEntities(gedcom);
        ret |= super.convertAssociations(gedcom);
        return ret;
    }

    
    /**
     * *** 5 *** 
     * - Run generic code
     * - <run specific code>
     */
    @Override
    public void complete() {
        super.complete();
    }

    ////////////////////////////  END OF LOGIC /////////////////////////////////


    
    
    
    ////////////////////////////////////////////////////////////////////////////
    //                     SPECIFIC IMPORT FIXES                              //
    ////////////////////////////////////////////////////////////////////////////
    
    
    
    /**
     * REPO in Heredis 2017 are not stored as separated RECORDs so first, store them in memory
     * I Heredis 2018, it is ok
     */
    private void firstPassRepo() {
        if (!repoOK && (input.getLevel() == 1) && input.getTag().equals("REPO")) {
            String value = input.getValue();
            if (!value.matches("\\@[A-Z0-9]+\\@")) {
                if (!hashrepo.containsKey(value)) {
                    clerepo++;
                    hashrepo.put(input.getValue(), clerepo);
                    sb.append("0 @" + typerepo).append(clerepo).append("@ REPO").append(EOL);
                    sb.append("1 NAME ").append(input.getValue()).append(EOL);
                }
            } else {
                repoOK = true;
            }
        }

    }

    /**
     * Replace all pointers to REPO with their corresponding record
     * @return
     * @throws IOException 
     */
    private boolean processRepo() throws IOException {
        if (!repoOK && (input.getLevel() == 1) && input.getTag().equals("REPO")) {
            if (hashrepo.containsKey(input.getValue())) {
                output.writeLine(1, "REPO", "@" + typerepo + hashrepo.get(input.getValue()) + "@");
                String str = input.getLine();
                console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixRepo", str.substring(0, Math.min(str.length(), 30))));
            }
            return true;
        }
        return false;

    }

    /**
     * Write REPO records at the end
     * @throws IOException 
     */
    private void finaliseRepo() throws IOException {
        if (!repoOK) {
            output.write(sb.toString());
        }
    }

    
    
    // Addr sans resi
    private boolean processAddr() throws IOException {
        if (input.getTag().equals("ADDR")) {
            String prevTag = input.getPath().get(input.getPath().length() - 2);
            if (prevTag.equals("INDI")) {
                output.writeLine(input.getLevel(), null, "RESI", null);
                output.shiftLine(input);
                return true;
            }
            if (prevTag.equals("FAM")) {
                output.writeLine(input.getLevel(), null, "_RESI", null);
                output.shiftLine(input);
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * fix *:OBJE:DATE errors.
     * Remove DATE tag if no value, rename tag to _DATE otherwise
     * @return 
     */
    private boolean processTagNotAllowed() throws IOException {
        // C'est un tag DATE: on transforme les dates rep
        String tag = input.getTag();
        TagPath path = input.getPath();
        if ("SOUR:DATE".equalsIgnoreCase(path.toString())) {  // invalid tag here and redundant information, replace with _DATE
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("OBJE:DATE".equalsIgnoreCase(path.toString())) {  // invalid tag here and redundant information, replace with _DATE
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("INDI:ASSO:TITL".equalsIgnoreCase(path.toString())) {  // invalid tag here, replace with _RESN
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("SOUR:RESN".equalsIgnoreCase(path.toString())) {  // invalid tag here, replace with _RESN
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("OBJE:RESN".equalsIgnoreCase(path.toString())) {  // invalid tag here, replace with _RESN
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("RESN".equalsIgnoreCase(tag) && input.getLevel() == 3) {  // invalid tag here, replace with _RESN
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("SOUR:NOTE:RESN".equalsIgnoreCase(path.toString())) {  // invalid tag here, replace with _RESN
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("REPO:NOTE:RESN".equalsIgnoreCase(path.toString())) {  // invalid tag here, replace with _RESN
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("INDI:NOTE:RESN".equalsIgnoreCase(path.toString())) {  // invalid tag here, replace with RESN on level 2
            String result = output.writeLine(input.getLevel() - 1, tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("FAM:NOTE:RESN".equalsIgnoreCase(path.toString())) {  // invalid tag here, replace with RESN on level 2
            String result = output.writeLine(input.getLevel() - 1, tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("SOUR:TYPE".equalsIgnoreCase(path.toString())) {  // invalid tag here but useful information, replace with NOTE
            String result = output.writeLine(input.getLevel(), "NOTE", input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if (path.toString().endsWith("PAGE:CONT")) {  // invalid tag here, replace with _CONT
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if (path.toString().startsWith("INDI") && "HUSB".equalsIgnoreCase(tag)) {  // invalid tag here, replace with _HUSB
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if (path.toString().startsWith("INDI") && "AGE".equalsIgnoreCase(tag) && path.toString().contains("HUSB")) {  // invalid tag here, replace with _AGE
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if (path.toString().startsWith("INDI") && "WIFE".equalsIgnoreCase(tag)) {  // invalid tag here, replace with _WIFE
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if (path.toString().startsWith("INDI") && "AGE".equalsIgnoreCase(tag) && path.toString().contains("WIFE")) {  // invalid tag here, replace with _AGE
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("TIME".equals(tag) && (!path.toString().startsWith("HEAD") && !path.toString().contains("CHAN"))) {  // invalid tag here but useful information, replace with _TIME
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("AGE".equals(tag)) {  // Age tag value is missing "y" at the end
            String value = input.getValue();
            if (!value.contains("y") && !value.contains("m") && !value.contains("d")) {
                String result = output.writeLine(input.getLevel(), tag, input.getValue() + "y");
                console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixAge", input.getLine() + " ==> " + result));
                return true;
            }
        }
        if ("SEX".equals(tag) && ("?".equals(input.getValue()))) {  // Unknown sex should be "U", not "?"
            String result = output.writeLine(input.getLevel(), tag, "U");
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixUnknownSex", input.getLine() + " ==> " + result));
            return true;
        }
        return false;
    }

    // calendrier repub
    private boolean processFrenchRepHeredis() throws IOException {
        // C'est un tag DATE: on transforme les dates rep
        if (input.getTag().equals("DATE")) {
            String newValue = frenchCalCheck(input.getValue());
            if (newValue != null) {
                String result = output.writeLine(input.getLevel(), input.getTag(), newValue);
                console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixRepCalendar", input.getLine() + " ==> " + result));
                return true;

            }
        }
        return false;
    }

    private String frenchCalCheck(String in) {
        final Pattern french_cal = Pattern.compile("(@#DFRENCH R@ )(.*)");
        final Pattern date_value = Pattern.compile("(FROM|BEF|AFT|BET|INT|TO) (.*)");
        final Pattern date_range = Pattern.compile("(FROM|BEF|AFT|BET|INT|TO) (.*) (TO|AND) (.*)");

        String result = "";
        Matcher matcher = french_cal.matcher(in);
        if (matcher.matches() && (matcher.groupCount() > 1)) {
            // C'est un cal republicain, on essaie d'interpreter
            String date_parameter = matcher.group(2);
            Matcher m1 = date_range.matcher(date_parameter);
            if (m1.matches()) {
                result += m1.group(1) + " @#DFRENCH R@ "
                        + convDateFormat(m1.group(2));
                result += " " + m1.group(3) + " @#DFRENCH R@ "
                        + convDateFormat(m1.group(4));
                return result;
            }

            m1 = date_value.matcher(date_parameter);
            if (m1.matches()) {
                result += m1.group(1) + " @#DFRENCH R@ "
                        + convDateFormat(m1.group(2));
                return result;
            }
            result += "@#DFRENCH R@ " + convDateFormat(matcher.group(2));
            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings("serial")
    static private String convDateFormat(String from) {
        final HashMap<String, String> repmonconvtable = new HashMap<String, String>() {

            {
                put("I", "1");
                put("II", "2");
                put("III", "3");
                put("IV", "4");
                put("V", "5");
                put("VI", "6");
                put("VII", "7");
                put("VIII", "8");
                put("IX", "9");
                put("X", "10");
                put("XI", "11");
                put("XII", "12");
            }
        };
        final Pattern french_date = Pattern.compile("(.*) an (\\w*)(.*)");
        Matcher m = french_date.matcher(from);
        if (m.matches() && m.groupCount() > 2) {
            String result = m.group(1) + " " + repmonconvtable.get(m.group(2));
            if (m.groupCount() > 3) {
                result += m.group(3);
            }
            return result;
        }
        return from;
    }

    public boolean processEntities(Gedcom gedcom) {

        boolean hasErrors = false;
        Property[] props = null;
        Property prop = null;
        Property host = null;
        boolean moved = false;
        
        console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixMedia"));

        // Keep only one RESN tag per indi
        for (Indi indi : gedcom.getIndis()) {
            props = indi.getProperties("RESN");
            if (props != null && props.length > 1) {
                for (int i = 1 ; i< props.length ; i++) {
                    prop = props[i];
                    prop.getParent().delProperty(prop);
                }
                hasErrors = true;
            }
        }

        // Keep only one RESN tag per fam
        for (Fam fam : gedcom.getFamilies()) {
            props = fam.getProperties("RESN");
            if (props != null && props.length > 1) {
                for (int i = 1 ; i< props.length ; i++) {
                    prop = props[i];
                    prop.getParent().delProperty(prop);
                }
                hasErrors = true;
            }
        }

        // Put FORM underneath FILE on media
        for (Media media : gedcom.getMedias()) {
            prop = media.getProperty("FORM");
            if (prop != null) {
                host = media.getProperty("FILE");
                if (host != null) {
                    host.addProperty("FORM", prop.getValue());
                    prop.getParent().delProperty(prop);
                    hasErrors = true;
                }
            }
            host = media.getProperty("FILE");
            if (host != null) {
                prop = host.getProperty("FORM");
                if (prop == null) {
                    String ext = getExtension(host.getValue());
                    if (ext == null) {
                        ext = "none";
                    }
                    host.addProperty("FORM", ext);
                    hasErrors = true;
                }
                prop = host.getProperty("TITL");
                if (prop == null) {
                    PropertyFile filep = (PropertyFile) host;
                    File file = filep.getFile();
                    String title = file != null ? file.getName() : "";
                    int i = title.indexOf(".");
                    host.addProperty("TITL", i == -1 ? title : title.substring(0, i));
                    hasErrors = true;
                }
            }
            
        }

        
        for (Source source : gedcom.getSources()) {
            // Put QUAY in SOURCE citation rather than SOUR, if not already there
            moved = false;
            prop = source.getProperty("QUAY");
            if (prop != null) {
                for (PropertyXRef xref : source.getProperties(PropertyXRef.class)) {
                    if (xref.isValid()) {
                        host = xref.getTarget();
                        if (host instanceof PropertySource) {
                            Property quay = host.getProperty("QUAY");
                            if (quay == null) {
                                host.addProperty("QUAY", prop.getValue());
                                moved = true;
                            }
                        }
                    }
                }
                if (!moved) {
                    source.addProperty("_QUAY", prop.getValue());
                }
                source.delProperty(prop);
                hasErrors = true;
            }
            
            // Put Web site in REPO:NOTE rather than SOUR:WWW
            prop = source.getProperty("WWW");
            if (prop != null) {
                host = source.getProperty("REPO");
                if (host != null) {
                    host.addProperty("NOTE", prop.getValue());
                }
                source.delProperty(prop);
                hasErrors = true;
            }
            
            // Put MAIL in REPO entity rather than SOUR, else change to "_EMAIL"
            prop = source.getProperty("EMAIL");
            if (prop != null) {
                host = source.getProperty("REPO");
                if (host != null && host instanceof PropertyXRef) {
                    PropertyXRef repo = (PropertyXRef) host;
                    host = repo.getTarget();
                    if (host != null) {
                        host.getParent().addProperty("EMAIL", prop.getValue());
                    }
                } else {
                    source.addProperty("_EMAIL", prop.getValue());
                }
                source.delProperty(prop);
                hasErrors = true;
            }
        }
        
        console.println("=============================");
        
        return hasErrors;
    }

    
    
}
