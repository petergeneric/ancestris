package ancestris.modules.releve.dnd;

import genj.gedcom.PropertyAge;
import genj.gedcom.time.Delta;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class MergeDialogTestMatch extends TestCase {


    /**
     * test match age
     */
    public void testMatchAge() {
        Pattern pattern = Pattern.compile("^([0-9\\p{javaWhitespace}]{0,4})a ([0-9\\p{javaWhitespace}]{0,2})m ([0-9\\p{javaWhitespace}]{0,2})j?$");
        String age;
        Matcher matcher;

        age = "10a 9m 3j" ;
         matcher = pattern.matcher(age);
        if (matcher.matches()) {
            assertEquals("années", "10", matcher.group(1));
            assertEquals("mois",   "9",  matcher.group(2));
            assertEquals("jours",  "3",  matcher.group(3));
        } else {
            fail("no match");
        }

        age = "a 9m 3j" ;
        matcher = pattern.matcher(age);
        if (matcher.matches()) {
            assertEquals("années", "", matcher.group(1));
            assertEquals("mois",   "9",  matcher.group(2));
            assertEquals("jours",  "3",  matcher.group(3));
        } else {
            fail("no match");
        }

        age = "1 a 9m   j" ;
        matcher = pattern.matcher(age);
        if (matcher.matches()) {
            assertEquals("années", "1 ", matcher.group(1));
            assertEquals("mois",   "9",  matcher.group(2));
            assertEquals("jours",  "  ",  matcher.group(3));
        } else {
            fail("no match");
        }

        age = "1a  m  j" ;
        matcher = pattern.matcher(age);
        if (matcher.matches()) {
            assertEquals("années", "1 ", matcher.group(1));
            assertEquals("mois",   "  ",  matcher.group(2));
            assertEquals("jours",  "  ",  matcher.group(3));
        } else {
            fail("no match");
        }
    }

    /**
     * test match age
     */
    public void testDelta() {
        Delta delta = new Delta(0,0,0);

        delta.setValue("12y 9m 7d");
        assertEquals("années", 12, delta.getYears());
        assertEquals("mois",   9,  delta.getMonths());
        assertEquals("jours",  7,  delta.getDays());

        delta.setValue("4y 7d");
        assertEquals("années", 4, delta.getYears());
        assertEquals("mois",   0,  delta.getMonths());
        assertEquals("jours",  7,  delta.getDays());
    }
    /**
     * test splitCSV
     */
    public void testFindSources1() {
        Pattern pattern = Pattern.compile("BMS ([\\S\\-]+) (\\d+)");
        String sourceTitle = "BMS city-namée 1720" ;
        Scanner scan = new Scanner(sourceTitle);
        if (scan.findInLine(pattern) != null) {
            MatchResult match = scan.match();
            assertEquals("nom commune", "city-namée",match.group(1));
            assertEquals("date début",  "1720",     match.group(2));
        } else {
            fail("no match");
        }
        scan.close();
    }

    /**
     * test match string
     */
    public void testFindSources2() {
        Pattern pattern = Pattern.compile("(?:BMS|Etat\\scivil) ([\\S\\-]+) (\\d+)\\-?(\\d+)?");
        String sourceTitle = "BMS city-namée 1720-1792" ;
        MatchResult match = findSources(pattern, sourceTitle);
        if ( match != null) {
            assertEquals("nom commune", "city-namée",match.group(1));
            assertEquals("date début",  "1720",     match.group(2));
            assertEquals("date début",  "1792",     match.group(3));
        } else {
            fail("no match");
        }
        
        sourceTitle = "BMS city-namée 1720" ;
        match = findSources(pattern, sourceTitle);
        if ( match != null) {
            assertEquals("nom commune", "city-namée",match.group(1));
            assertEquals("date début",  "1720",     match.group(2));
        } else {
            fail("no match");
        }

        sourceTitle = "Etat civil citynamée 1720" ;
        match = findSources(pattern, sourceTitle);
        if ( match != null) {
            assertEquals("nom commune", "citynamée",match.group(1));
            assertEquals("date début",  "1720",     match.group(2));
        } else {
            fail("no match");
        }
        
        sourceTitle = "xxxxx citynamée 1720" ;
        match = findSources(pattern, sourceTitle);
        assertEquals("prefixe xxxx", null, match);

    }

    /**
     * test match string
     */
    public void testFindSources3() {
        Pattern pattern = Pattern.compile("(\\d+)(?:\\s++)(\\D+)(?:\\s++)(?:BMS|Etat\\scivil)");
        String sourceTitle ;
        MatchResult match;

        sourceTitle = "75000 city-namée BMS" ;
        match = findSources(pattern, sourceTitle);
        if ( match != null) {
            assertEquals("nom commune", "75000",match.group(1));
            assertEquals("date début",  "city-namée",     match.group(2));
        } else {
            fail("no match");
        }

        sourceTitle = "75000 city-namée Etat civil" ;
        match = findSources(pattern, sourceTitle);
        if ( match != null) {
            assertEquals("nom commune", "75000",match.group(1));
            assertEquals("date début",  "city-namée",     match.group(2));
        } else {
            fail("no match");
        }

    }
     
    private MatchResult findSources(Pattern pattern, String line) {
        Scanner scan = new Scanner(line);
        MatchResult match = null;
        if (scan.findInLine(pattern) != null) {
            match = scan.match();
        } 
        scan.close();
        return match;

    }

    /**
     * test match string
     */
    public void testFindSources4() {
        String sourceTitle ;
        MatchResult match;

        sourceTitle = "75000 city-namée BMS" ;
        match = findSources(sourceTitle, "75", "75000", "city-namée");
        assertNotNull(match);
        
        sourceTitle = "75 city-namée BMS" ;
        match = findSources(sourceTitle, "75", "75111", "city-namée");
        assertNotNull(match);

        sourceTitle = "75 city-namée Etat civil" ;
        match = findSources(sourceTitle, "75", "", "city-namée");
        assertNotNull(match);

        sourceTitle = "75 La xxxx-xxx Etat civil" ;
        match = findSources(sourceTitle, "75", "", "La xxxx-xxx");
        assertNotNull(match);

        sourceTitle = "75 city-namée BMS" ;
        match = findSources(sourceTitle, "75", "", "city");
        assertNull(match);

    }

     private MatchResult findSources(String line, String countyCode, String cityCode, String cityName) {
        String stringPatter = String.format("(?:%s|%s)(?:\\s++)%s(?:\\s++)(?:BMS|Etat\\scivil)", countyCode, cityCode, cityName);
        Pattern pattern = Pattern.compile(stringPatter);

        Scanner scan = new Scanner(line);
        MatchResult match = null;
        if (scan.findInLine(pattern) != null) {
            match = scan.match();
        }
        scan.close();
        return match;

    }

     /**
     * test match string
     */
    public void testFindSources5() {
        String sourceTitle ;
        boolean match;

        sourceTitle = "75000 city-namée BMS" ;
        match = findSources2(sourceTitle, "75", "75000", "city-namée");
        assertTrue(match);

        sourceTitle = "75 city-namée BMS" ;
        match = findSources2(sourceTitle, "75", "75111", "city-namée");
        assertTrue(match);

        sourceTitle = "75 city-namée Etat civil" ;
        match = findSources2(sourceTitle, "75", "", "city-namée");
        assertTrue(match);

        sourceTitle = "75 La xxxx-xxx Etat civil" ;
        match = findSources2(sourceTitle, "75", "", "La xxxx-xxx");
        assertTrue(match);

        sourceTitle = "75 city-namée BMS 1720-1790" ;
        match = findSources2(sourceTitle, "75", "", "city");
        assertFalse(match);

        sourceTitle = "75 city-namée BMS" ;
        match = findSources2(sourceTitle, "75", "", "");
        assertFalse(match);

        sourceTitle = "75 city-namée BMS" ;
        match = findSources2(sourceTitle, "", "", "");
        assertFalse(match);

    }

    private boolean findSources2(String line, String countyCode, String cityCode, String cityName) {
        String stringPatter = String.format("(?:%s|%s)(?:\\s++)%s(?:\\s++)(?:BMS|Etat\\scivil)", countyCode, cityCode, cityName);
        Pattern pattern = Pattern.compile(stringPatter);
        return pattern.matcher(line).matches();

    }




   

}
