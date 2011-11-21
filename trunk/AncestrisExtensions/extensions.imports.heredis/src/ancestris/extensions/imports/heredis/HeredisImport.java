/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * sur une base de gedrepohr.pl (Patrick TEXIER) pour la correction des REPO
 * Le reste des traitements par Daniel ANDRE 
 */
package ancestris.extensions.imports.heredis;

import ancestris.extensions.imports.api.Import;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.NbBundle;

/**
 * The import function for Heredis originated Gedcom files
 */
public class HeredisImport extends Import {

    private static int clerepo;
    private static Hashtable<String, Integer> hashrepo;
    private static StringBuilder sb;

    /**
     * Constructor
     */
    public HeredisImport(File fileIn, File fileOut) {
        super(fileIn, fileOut, NbBundle.getMessage(HeredisImport.class, "OpenIDE-Module-Name"));
        clerepo = 0;
        hashrepo = new Hashtable<String, Integer>();
        sb = new StringBuilder();
    }

    @Override
    protected void finalise() throws IOException {
        super.finalise();
        finaliseRepo();
    }

    @Override
    protected void firstPass() {
        super.firstPass();
        firstPassRepo();
    }

    @Override
    protected boolean process() throws IOException {
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
        return false;
    }

    private void firstPassRepo() {
        if ((input.getLevel() == 1) && input.getTag().equals("REPO")) {
            if (!hashrepo.containsKey(input.getValue())) {
                clerepo++;
                hashrepo.put(input.getValue(), clerepo);
                sb.append("0 @" + typerepo + clerepo + "@ REPO" + EOL);
                sb.append("1 NAME " + input.getValue() + EOL);
            }
        }

    }

    private boolean processRepo() throws IOException {
        if ((input.getLevel() == 1) && input.getTag().equals("REPO")) {
            if (hashrepo.containsKey(input.getValue())) {
                output.writeLine(1, "REPO", "@" + typerepo
                        + hashrepo.get(input.getValue()) + "@");
                console.println(input.getLine());
                console.println("==> " + NbBundle.getMessage(HeredisImport.class, "corrected"));
            }
            return true;
        }
        return false;

    }

    private void finaliseRepo() throws IOException {
        output.write(sb.toString());
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
    // calendrier repub

    private boolean processFrenchRepHeredis() throws IOException {
        // C'est un tag DATE: on transforme les dates rep
        if (input.getTag().equals("DATE")) {
            String newValue = frenchCalCheck(input.getValue());
            if (newValue != null) {
                String result = output.writeLine(input.getLevel(), input.getTag(), newValue);
                console.println(input.getLine());
                console.println("==> " + result);
                return true;

            }
        }
        return false;
    }

    String frenchCalCheck(String in) {
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
        final Hashtable<String, String> repmonconvtable = new Hashtable<String, String>() {

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
}
