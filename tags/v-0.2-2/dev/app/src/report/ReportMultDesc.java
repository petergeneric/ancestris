/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.fo.Document;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PrivacyPolicy;
import genj.gedcom.Property;
import genj.gedcom.PropertyMultilineValue;
import genj.report.Report;

import java.util.HashMap;

import javax.swing.ImageIcon;

/*
 * GenJ - ReportMultDesc
 * TODO Daniel titles statistics (nb pers distinctes, nbpers vivantes, nb fam, ...)
 * TODO Daniel: Remove bullet with possibly replacement with d'abboville number
 * TODO Daniel: Add table output (for csv)
 * TODO Daniel: reenable global privacy disabling
 */
public class ReportMultDesc extends Report {

  private final static String FORMAT_STRONG = "font-weight=bold";
  private final static String FORMAT_UNDERLINE = "text-decoration=underline";

  private int nbColumns;

  // Statistics
  private int nbIndi = 0;

  private int nbFam = 0;

  private int nbLiving = 0;

  private final static int ONE_LINE = 0, ONE_EVT_PER_LINE = 1, TABLE = 2;
  public int reportFormat = ONE_LINE;
  public String reportFormats[] = { translate("IndiPerLine"),
      translate("EventPerLine"),
      translate("Table")};

  private final static int NUM_NONE = 0, NUM_ABBO = 1;
  public int reportNumberScheme = NUM_ABBO;
  public String reportNumberSchemes[] = { translate("NumNone"),
      translate("NumAbbo") };

  public int reportMaxGenerations = 999;

  public boolean showAllPlaceJurisdictions = false;

  public boolean reportPlaceOfBirth = true;

  public boolean reportDateOfBirth = true;

  public boolean reportPlaceOfDeath = true;

  public boolean reportDateOfDeath = true;

  public boolean reportPlaceOfMarriage = true;

  public boolean reportDateOfMarriage = true;

  public boolean reportPlaceOfOccu = true;

  public boolean reportDateOfOccu = true;

  public boolean reportPlaceOfResi = true;

  public boolean reportDateOfResi = true;

  public boolean reportMailingAddress = true;
  
  public boolean reportIds = true;

  // outputer
  	private Output output;

  // Privacy
  public int publicGen = 0;

  /**
   * don't need stdout
   */
  public boolean usesStandardOut() {
    return true;
  }

  /**
   * use the fo image
   */
  protected ImageIcon getImage() {
    return Report.IMG_FO;
  }

  /**
   * Main for argument individual
   */
  public void start(Indi indi) {
    start( new Indi[] { indi }, translate("title.descendant", indi.getName()));
  }

  /**
   * One of the report's entry point
   */
  public void start(Indi[] indis) {
    start( indis, getName() + " - " + indis[0].getGedcom().getName());
  }

  /**
   * Our main private report point
   */
  private void start(Indi[] indis, String title) {

	  switch (reportFormat){
	  case TABLE:
		  output = new OutputTable();
		  break;
	  case ONE_LINE:
	  case ONE_EVT_PER_LINE:
		  output = new OutputStandard();
		  break;
      default:
          throw new IllegalArgumentException("no such report type");
	  }
	  // keep track of who we looked at already
    HashMap done = new HashMap();

    // Init some stuff
    PrivacyPolicy policy = OPTIONS.getPrivacyPolicy();

    nbColumns = 2;
    if (reportPlaceOfBirth || reportDateOfBirth)
      nbColumns++;
    if (reportPlaceOfMarriage || reportDateOfMarriage)
      nbColumns++;
    if (reportPlaceOfDeath || reportDateOfDeath)
      nbColumns++;
    if (reportPlaceOfOccu || reportDateOfOccu)
      nbColumns++;
    if (reportPlaceOfResi || reportDateOfResi)
      nbColumns++;

    Document doc = new Document(title);

    // iterate into individuals and all its descendants
    for (int i = 0; i < indis.length; i++) {
      Indi indi = indis[i];
      output.title(indi,doc);
      iterate(indi, 1, (new Integer(i+1).toString()), done, policy, doc);
    }

    output.statistiques(doc);

    // done
    showDocumentToUser(doc);

  }

  /**
   * Generate descendants information for one individual
   */
  private void iterate(Indi indi, int level, String num, HashMap done, PrivacyPolicy policy, Document doc) {

    nbIndi++;
    if (indi!=null&&!indi.isDeceased()) nbLiving ++;

    // no more?
    if (level > reportMaxGenerations)
      return;

    // still in a public generation?
    PrivacyPolicy localPolicy = level < publicGen + 1 ? PrivacyPolicy.PUBLIC : policy;

    output.startIndi(doc);
    format(indi, (Fam)null, num, localPolicy, doc);

    // And we loop through its families
    Fam[] fams = indi.getFamiliesWhereSpouse();
    for (int f = 0; f < fams.length; f++) {

      // .. here's the fam and spouse
      Fam fam = fams[f];

      Indi spouse = fam.getOtherSpouse(indi);

      // output the spouse
      output.startSpouse(doc);
        if (fams.length==1)
    	    format(spouse,fam,num+"x", localPolicy, doc);
    	else
    	    format(spouse,fam,num+"x"+(f+1), localPolicy, doc);

      // put out a link if we've seen the spouse already
      if (done.containsKey(fam)) {
    	  output.link(fam,(String)done.get(fam),doc);
      } else {

   	    output.anchor(fam, doc);
          done.put(fam,num);
        nbIndi++;
        nbFam++;
        if (spouse!=null&&!spouse.isDeceased()) nbLiving ++;

        // .. and all the kids
        Indi[] children = fam.getChildren();
        for (int c = 0; c < children.length; c++) {
          // do the recursive step
          if (fams.length == 1)
            iterate(children[c], level + 1, num+'.'+(c+1), done, policy, doc);
          else
            iterate(children[c], level + 1, num+'x'+(f+1)+'.'+(c+1), done, policy, doc);

          // .. next child
        }

      }
      // .. next family
    }

    // done
    output.endIndi(indi, doc);
  }

  /**
   * resolves the information of one Indi
   */
  private void format(Indi indi, Fam fam, String prefix, PrivacyPolicy policy, Document doc) {

    // Might be null
    if (indi == null)
      return;

    // FIXME Nils re-enable anchors for individuals processes
    output.number(prefix,doc);
    output.name(policy.getDisplayValue(indi, "NAME"),doc);
    if (reportIds)
      output.id(indi.getId(),doc);

    String birt = output.format(indi, "BIRT", OPTIONS.getBirthSymbol(), reportDateOfBirth, reportPlaceOfBirth, policy);
    String marr = fam!=null ? output.format(fam, "MARR", OPTIONS.getMarriageSymbol(), reportDateOfMarriage, reportPlaceOfMarriage, policy) : "";
    String deat = output.format(indi, "DEAT", OPTIONS.getDeathSymbol(), reportDateOfDeath, reportPlaceOfDeath, policy);
    String occu = output.format(indi, "OCCU", "{$T}", reportDateOfOccu, reportPlaceOfOccu, policy);
    String resi = output.format(indi, "RESI", "{$T}", reportDateOfResi, reportPlaceOfResi, policy);
    PropertyMultilineValue addr = reportMailingAddress ? indi.getAddress() : null;
    if (addr != null && policy.isPrivate(addr)) addr = null;

    // dump the information

    	output.startEvents(doc);

    String[] infos = new String[] { birt, marr, deat, occu, resi };
    for (int i=0, j=0; i<infos.length ; i++) {
    	output.event(infos[i],doc);
    }
	if (addr != null) {
		output.addressPrefix(doc);
		String[] lines = addr.getLines();
		output.startEvents(doc);
		for (int i = 0; i < lines.length; i++) {
			output.event(lines[i],doc);
		}
	    output.endEvents(doc);
	}
    output.endEvents(doc);
    // done
  }

  abstract class Output{
	  abstract void title(Indi indi, Document doc);
	  abstract void statistiques(Document doc);
	  abstract void startIndi(Document doc);
	  abstract void startSpouse(Document doc);
	  abstract void link(Fam fam, String label, Document doc);
	  abstract void anchor(Fam fam, Document doc);
	  abstract void endIndi(Indi indi, Document doc);
	  abstract void name(String name, Document doc);
	  abstract void id(String id, Document doc);
	  abstract void startEvents(Document doc);
	  abstract void endEvents(Document doc);
	  abstract void event(String event, Document doc);
	  abstract void number(String num, Document doc);
	  abstract void addressPrefix(Document doc);

	  private HashMap format(Indi indi, Fam fam, String prefix, PrivacyPolicy policy) {
		  HashMap result = new HashMap();
		  // Might be null
		  if (indi == null)
			  return null;

		  result.put("birt", format(indi, "BIRT", OPTIONS.getBirthSymbol(), reportDateOfBirth, reportPlaceOfBirth, policy));
		  result.put("marr", fam!=null ? format(fam, "MARR", OPTIONS.getMarriageSymbol(), reportDateOfMarriage, reportPlaceOfMarriage, policy) : "");
		  result.put("deat", format(indi, "DEAT", OPTIONS.getDeathSymbol(), reportDateOfDeath, reportPlaceOfDeath, policy));
		  result.put("occu", format(indi, "OCCU", "{$T}{ $V}", reportDateOfOccu, reportPlaceOfOccu, policy));
		  result.put("resi", format(indi, "RESI", "{$T}", reportDateOfResi, reportPlaceOfResi, policy));
		  PropertyMultilineValue addr = reportMailingAddress ? indi.getAddress() : null;
		  if (addr != null && policy.isPrivate(addr)) addr = null;
		  result.put("addr", addr);
		  return result;

	  }
	  /**
	   * convert given prefix, date and place switches into a format string
	   */
	  String format(Entity e, String tag, String prefix, boolean date, boolean place, PrivacyPolicy policy) {

	    Property prop = e.getProperty(tag);
	    if (prop == null)
	      return "";

	    String format = prefix + "{ $v}"+(date ? "{ $D}" : "")
	        + (place && showAllPlaceJurisdictions ? "{ $P}" : "")
	        + (place && !showAllPlaceJurisdictions ? "{ $p}" : "");

	    return prop.format(format, policy);

	  }

  }
  class OutputStandard extends Output{
	  private boolean isFirstEvent = true;
	  void title(Indi indi, Document doc){
	      doc.startSection( translate("title.descendant", indi.getName()) );
	  }
	  void statistiques(Document doc){
		  doc.startSection( translate("title.stats") );
		  doc.addText( translate("nb.fam", nbFam) );
		  doc.nextParagraph();
		  doc.addText( translate("nb.indi", nbIndi) );
		  doc.nextParagraph();
		  doc.addText( translate("nb.living", nbLiving) );
	  }
	  void startIndi(Document doc){
		  doc.startList();
	  }
	  void startSpouse(Document doc){
	  }
	  void link(Fam fam,String label, Document doc){
    	  doc.nextParagraph();
        doc.addText("====> " + translate("see") +" ");
        if (reportNumberScheme != NUM_NONE)
        	doc.addLink(label, fam);
        else
        	doc.addLink(fam.getDisplayValue(), fam);
	  }
	  void anchor(Fam fam, Document doc){
	   	    doc.addAnchor(fam);
	  }
	  void endIndi(Indi indi, Document doc){
		  doc.endList();
	  }
	  void number(String number, Document doc){
		  //FIXME: should be in startindi?
		  doc.nextParagraph();
		  if (reportNumberScheme != NUM_NONE)
			  doc.nextListItem("genj:label="+number);
	  }
	  void name(String name, Document doc){
		  doc.addText(name, FORMAT_STRONG);
	  }
	  void id(String id, Document doc){
		  doc.addText(" (" + id + ")" );
	  }
	  void startEvents(Document doc){
		  if (reportFormat!=ONE_LINE)
			  doc.startList();
		  isFirstEvent = true;
	  }
	  void endEvents(Document doc){
		  if (reportFormat!=ONE_LINE)
			  doc.endList();
	  }
	  void event(String event, Document doc){
	      if (event.length()==0)
	    	  return;
	      // dump the information
	      if (!isFirstEvent) {
	    	  if (reportFormat==ONE_LINE)  doc.addText(", ");
	    	  else doc.nextListItem();
	      }
	      doc.addText(event);
	      isFirstEvent = false;
	  }
	  void addressPrefix(Document doc){
	      // dump the information
	      if (!isFirstEvent) {
	    	  if (reportFormat==ONE_LINE)  doc.addText(", ");
	    	  else doc.nextListItem();
	      }
	  }
  }

  // Loop through individuals & families



  class OutputTable extends Output{

	  String format(Entity e, String tag, String prefix, boolean date, boolean place, PrivacyPolicy policy) {
		  return super.format(e,tag,"",date,place,policy);
	  }

	void title(Indi indi, Document doc) {
		  doc.startTable("genj:csv=true");

		  doc.nextTableRow();
/*		  doc.addTableColumn("");
		  doc.addTableColumn("");
		  doc.addTableColumn("");
		  doc.addTableColumn("");
*/
		  doc.nextTableCell("number-columns-spanned=7,"+FORMAT_STRONG );
		  doc.addText(translate("title.descendant", indi.getName()) );

		  doc.nextTableRow();
		  doc.addText( translate("num.col"),FORMAT_STRONG );
		  doc.nextTableCell();
		  doc.addText( Gedcom.getName("NAME"),FORMAT_STRONG );
		  doc.nextTableCell();
		  doc.addText( Gedcom.getName("BIRT"),FORMAT_STRONG );
		  doc.nextTableCell();
		  doc.addText( Gedcom.getName("MARR"),FORMAT_STRONG );
		  doc.nextTableCell();
		  doc.addText( Gedcom.getName("DEAT"),FORMAT_STRONG );
		  doc.nextTableCell();
		  doc.addText( Gedcom.getName("OCCU"),FORMAT_STRONG );
		  doc.nextTableCell();
		  doc.addText( Gedcom.getName("RESI"),FORMAT_STRONG );
		  doc.nextTableCell();
		  doc.addText( translate("addr1.col"),FORMAT_STRONG );
		  doc.nextTableCell();
		  doc.addText( translate("addr2.col"),FORMAT_STRONG );
		  doc.nextTableCell();
		  doc.addText( translate("addr3.col"),FORMAT_STRONG );
		  doc.nextTableCell();
		  doc.addText( translate("addr4.col"),FORMAT_STRONG );
		  doc.nextTableCell();
		  doc.addText( translate("addr5.col"),FORMAT_STRONG );
	}

	void statistiques(Document doc) {
		  doc.startSection( translate("title.stats") );
		  doc.addText( translate("nb.fam", nbFam) );
		  doc.nextParagraph();
		  doc.addText( translate("nb.indi", nbIndi) );
		  doc.nextParagraph();
		  doc.addText( translate("nb.living", nbLiving) );
	}

	void startIndi(Document doc) {
	    // format the indi's information
		doc.nextTableRow();
	}

	void startSpouse(Document doc) {
	    // format the indi's information
		doc.nextTableRow();
	}

	void link(Fam fam, String label, Document doc) {
		doc.nextTableRow();
		  doc.nextTableCell();
		  doc.nextTableCell();
      doc.addText("====> " + translate("see") +" ");
      if (reportNumberScheme != NUM_NONE)
      	doc.addText(label);
      else
      	doc.addText(fam.getDisplayValue());
	}

	void anchor(Fam fam, Document doc) {
	}

	void endIndi(Indi indi, Document doc) {
	}

	void name(String name, Document doc) {
		doc.nextTableCell();
		doc.addText(name, FORMAT_STRONG);
	}

	void id(String id, Document doc) {
		doc.addText(" (" + id + ")" );
	}

	void startEvents(Document doc) {
	}

	void endEvents(Document doc) {
	}

	void event(String event, Document doc) {
		doc.nextTableCell();
		doc.addText(event);
	}

	void number(String num, Document doc) {
		doc.nextTableCell();
		doc.addText(num);
	}

	void addressPrefix(Document doc){
	}
  }
} // ReportMulDesv
