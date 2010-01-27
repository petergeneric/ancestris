
/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyPlace;
import genj.report.Report;
import genj.util.swing.Action2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ImageIcon;

/**
 * GenJ - Report
 * @author Daniel ANDRE <daniel.andre@free.fr>
 * @version 1.0
 */

public class ReportCGW extends Report {

    private final static Charset UTF8 = Charset.forName("ISO-8859-1");

    /** option - Index jurisdiction for analysis in PLAC tags */
    public int depPos = 2;
    /** option - Index jurisdiction for analysis in PLAC tags */
    public int cityPos = 1;
    /** option - Meaningfull length for the Department Juridiction field to keep */
    public int depLen = 0;

    /**
     * Overriden image - we're using the provided FO image
     */
    protected ImageIcon getImage() {
      return Report.IMG_FO;
    }

    /**
     * One of the report's entry point
     */
    public void start(Gedcom gedcom) {
      start(gedcom, gedcom.getEntities(Gedcom.INDI));
    }

    /**
     * One of the report's entry point
     */
    public void start(Indi[] indis)  {
      start(indis[0].getGedcom(), Arrays.asList(indis));
    }

    /**
     * Our main logic
     */
    private void start(Gedcom gedcom, Collection indis) {

	// Get a directory to write to
	File dir = getDirectoryFromUser(translate("target.dir"), Action2.TXT_OK);
	if (dir==null)
	    return;

	// Make sure directory is there
	if (!dir.exists()&&!dir.mkdirs()) {
	    println("***Couldn't create output directory "+dir);
	    return;
	}

	// prepare our index
	Map primary = new TreeMap();
	for (Iterator it = indis.iterator(); it.hasNext();)
	    analyze(  (Indi) it.next(), primary);

	// Create all the files
	for (Iterator ps = primary.keySet().iterator(); ps.hasNext(); ) {
	    String p = (String)ps.next();

	try{
	    export (p, primary, dir);
	}catch(IOException ioe){
	    System.err.println("IO Exception!");
	    ioe.printStackTrace();
	}

	    //      doc.endSection();
	}

	// done
	//    showDocumentToUser(doc);
    }

    private void export(String dept, Map primary, File dir) throws IOException{
	File file = new File(dir, dept+".csv");
	PrintWriter out = getWriter(new FileOutputStream(file));

	println(translate("DepartmentJur")+" : "+dept);
	Map secondary = (Map)lookup(primary, dept, null);
	for (Iterator ss = secondary.keySet().iterator(); ss.hasNext(); ) {
	    String s = (String)ss.next();

	    Map namelist = (Map)lookup(secondary, s, null);
	    for (Iterator ns = namelist.keySet().iterator(); ns.hasNext(); ) {
		String t = (String)ns.next();
		println("  "+t + " ; " + s);
		out.println(t+" ; "+s);
	    }
	}
	out.close();
    }

    /**
     * Analyze an individual
     */
    private void analyze(Indi indi, Map primary) {

	// consider non-empty last names only
	String name = indi.getLastName();
	if (name.length()==0)
	    return;

	// loop over all dates in indi
	for (Iterator places = indi.getProperties(PropertyPlace.class).iterator(); places.hasNext(); ) {

	    PropertyPlace place = (PropertyPlace)places.next();

	    String dept = place.getJurisdiction(depPos);
	    if (dept == null)  continue;
	    if (dept.length()==0) continue;
	    int l = Math.min(dept.length(),depLen);
	    if (l > 0) dept = dept.substring(0,l);
	    String jurisdiction = place.getJurisdiction(cityPos);
	    if (jurisdiction.length()==0) jurisdiction = "???";
	    // keep it
	    keep(name, jurisdiction, dept, primary);

	}
    }

    private void keep(String name, String place, String dept, Map primary) {

	// calculate primary and secondary key
	// remember
	Map secondary = (Map)lookup(primary, dept, TreeMap.class);
	Map namelist = (Map)lookup(secondary, place, TreeMap.class);
	lookup(namelist, name, TreeMap.class);
	// done
    }

    /**
     * Lookup an object in a map with a default class
     */
    private Object lookup(Map index, String key, Class fallback) {
	// look up and create lazily if necessary
	Object result = index.get(key);
	if (result==null) {
	    try {
		result = fallback.newInstance();
	    } catch (Throwable t) {
		t.printStackTrace();
		throw new IllegalArgumentException("can't instantiate fallback "+fallback);
	    }
	    index.put(key, result);
	}
	// done
	return result;
    }

    /**
     * Helper - Create a PrintWriter wrapper for output stream
     */
    private PrintWriter getWriter(OutputStream out) {
	return new PrintWriter(new OutputStreamWriter(out, UTF8));
    }


} //ReportCGW
