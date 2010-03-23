/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
/**
 * TODO Daniel: voir avec ie (page break)
 * TODO Daniel: inclure dans la liste les sources, repo, ... fictifs pour faire un tri
 * TODO Daniel: classer les colonnes au choix, avec plusieurs cle
 * TODO Daniel: limiter aux evenements/general/tous
 * TODO Daniel: differencier les todos sur evt des todo globaux
 * TODO Daniel: ligne blanche entre la fin des taches, et le resume
 */
import genj.fo.Document;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyMultilineValue;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.report.Report;

import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

/**
 * GenJ - Report
 *
 * @author Daniel ANDRE <daniel.andre@free.fr>
 * @version 1.0
 */
public class ReportToDo extends Report {

  private final static String PLACE_AND_DATE_FORMAT = "{$V }{$D}{ $P}";

  public String todoTag = "NOTE";

  public String todoStart = "TODO:";

  public boolean outputWorkingSheet = false;

  public boolean outputSummary = true;

  private final static String
	ROW_FORMAT_HEADER1 = "font-size=larger,background-color=#00ccff,font-weight=bold";
  private final static String
	FORMAT_HEADER2 = "font-size=large,background-color=#33ffff,font-weight=bold";
  private final static String
	FORMAT_HEADER3 = "background-color=#ffffcc,font-weight=bold";
  private final static String
	FORMAT_HEADER3_TODO = "background-color=#99cccc,font-weight=bold";
  private final static String
  	FORMAT_HEADER4 = "background-color=#ffffcc";
  private final static String FORMAT_EMPHASIS = "font-weight=italic";
  private final static String FORMAT_STRONG = "font-weight=bold";

  /*
         ".head1{background-color:#00ccff;font-size:20px;font-weight:bold;}"+
         ".head2{background-color:#33ffff;font-size:16px;font-weight:bold;}"+
         ".head3{background-color:#ffffcc;font-weight:bold;}"+
         ".head3-todo{background-color:#99cccc;font-weight:bold;}"+
         ".head4{background-color:#ffffcc;}"
         */

  /**
   * Overriden image - we're using the provided FO image
   */
  protected ImageIcon getImage() {
    return Report.IMG_FO;
  }

  /**
   * we're not generating anything to stdout anymore aside from debugging ino
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * The report's entry point
   */
  public void start(Gedcom gedcom) {
    List ents = gedcom.getEntities();
    start((Entity[])ents.toArray(new Entity[ents.size()]));
  }

  /**
   * The report's entry point - for a single individual
   */
  public void start(Indi indi) {
    start(new Indi[] { indi });
  }

  /**
   * The report's entry point - for a single family
   */
  public void start(Fam fam) {
    start(new Fam[]{ fam });
  }

  /**
   * The report's entry point - for a bunch of entities
   */
  public void start(Entity[] entities) {

    // create an output document
    Document doc = new Document(translate("titletodos"));

    // generate a detailed working sheet?
    if (outputWorkingSheet) {

      doc.startTable();
      doc.addTableColumn("column-width=12%");
      doc.addTableColumn("column-width=10%");
      doc.addTableColumn("column-width=20%");
      doc.addTableColumn("column-width=20%");
      doc.addTableColumn("column-width=19%");
      doc.addTableColumn("column-width=19%");

      exportWorkingSheet(entities, doc);
      doc.endTable();

    }

    // generate a summary?
    if (outputSummary) {

      // Loop through individuals & families
    	doc.startTable("width=100%,border=0.5pt solid black,genj:csv=true");

      doc.nextTableRow(ROW_FORMAT_HEADER1);
      doc.addTableColumn("");
      doc.addTableColumn("");
      doc.addTableColumn("");
/*      doc.addTableColumn("column-width=8%");
      doc.addTableColumn("column-width=8%");
      doc.addTableColumn("column-width=8%");
*/      doc.addTableColumn("");
      doc.addTableColumn("");

      doc.nextTableCell("number-columns-spanned=5");
      doc.addText(translate("titletodos"),ROW_FORMAT_HEADER1);

      doc.nextTableRow();
      doc.addText( translate("evt.col"),FORMAT_STRONG );
      doc.nextTableCell();
      doc.addText( translate("date.col"),FORMAT_STRONG );
      doc.nextTableCell();
      doc.addText( translate("place.col"),FORMAT_STRONG );
      doc.nextTableCell();
      doc.addText( translate("indi.col"),FORMAT_STRONG );
      doc.nextTableCell();
      doc.addText( translate("todo.col"),FORMAT_STRONG );

      int nbTodos = exportSummary(entities, doc);
      doc.endTable();

      doc.addText( translate("nbtodos", "" + nbTodos) );
    }

    // Done
    showDocumentToUser(doc);

  }

  /**
   * Exports the working sheet
   */
  private void exportWorkingSheet(Entity[] entities, Document doc) {

    // loop over entities
    for (int e = 0; e < entities.length; e++) {

      Entity entity = entities[e];

      List todos = findProperties(entity);
      if (!todos.isEmpty()) {
        if (entity instanceof Indi)
          exportEntity((Indi)entity, doc);
        if (entity instanceof Fam)
          exportEntity((Fam)entity, doc);
      }
    }

  }

  /**
   * Exports a family
   */
  private void exportEntity(Fam fam, Document doc) {
    Property prop;
    Property[] propArray;
    List todos;
    String tempString = "";
    Indi tempIndi;
    Fam tempFam;

    todos = findProperties(fam);
    if (todos.size() == 0)
      return;

    doc.nextTableRow(ROW_FORMAT_HEADER1);
    doc.nextTableCell("number-columns-spanned=6");
    doc.addText( translate("titlefam", new String[] { fam.toString(), fam.getId() }) );

    // //// Epoux
    tempIndi = fam.getHusband();
    doc.nextTableRow(FORMAT_HEADER2);
    doc.addText( Gedcom.getName("HUSB"));
    doc.nextTableCell("number-columns-spanned=5");
    doc.addText( tempIndi.getName() ); 

    outputEventRow(tempIndi, "BIRT", todos, doc);
    outputEventRow(tempIndi, "BAPM", todos, doc);
    outputEventRow(tempIndi, "DEAT", todos, doc);
    outputEventRow(tempIndi, "BURI", todos, doc);

    if (tempIndi!=null) {
      tempFam = tempIndi .getFamilyWhereBiologicalChild();
      if (tempFam != null) {
        doc.nextTableRow();
        doc.nextTableCell(FORMAT_HEADER3);
        doc.addText( translate("father") + ":" );
        doc.nextTableCell("number-columns-spanned=5");
        addIndiString(tempFam.getHusband(), doc);
        doc.nextTableRow();
        doc.nextTableCell(FORMAT_HEADER3);
        doc.addText( translate("mother") + ":" );
        doc.nextTableCell("number-columns-spanned=5");
        addIndiString(tempFam.getWife(), doc);
      }
    }
    
    // //// Epouse
    tempIndi = fam.getWife();
    doc.nextTableRow(FORMAT_HEADER2);
    doc.addText( Gedcom.getName("WIFE") );
    doc.nextTableCell("number-columns-spanned=5");
    doc.addText( tempIndi.getName() );

    outputEventRow(tempIndi, "BIRT", todos, doc);
    outputEventRow(tempIndi, "BAPM", todos, doc);
    outputEventRow(tempIndi, "DEAT", todos, doc);
    outputEventRow(tempIndi, "BURI", todos, doc);

    if (tempIndi!=null) {
      tempFam = tempIndi .getFamilyWhereBiologicalChild();
      if (tempFam != null) {
        doc.nextTableRow();
        doc.nextTableCell(FORMAT_HEADER3);
        doc.addText( translate("father") );
        doc.nextTableCell("number-columns-spanned=5");
        addIndiString(tempFam.getHusband(), doc) ;
        doc.nextTableRow();
        doc.nextTableCell(FORMAT_HEADER3);
        doc.addText( translate("mother") + ":" );
        doc.nextTableCell("number-columns-spanned=5");
        addIndiString(tempFam.getWife(), doc) ;
      }
    }
    outputEventRow(fam, "MARR", todos, doc);

    // //// Enfants
    Indi[] children = fam.getChildren();
    if (children.length > 0) {
      doc.nextTableRow(FORMAT_HEADER2);
      doc.nextTableCell("number-columns-spanned=6");
      doc.addText( Gedcom.getName("CHIL", children.length > 1) );
      for (int c = 0; c < children.length; c++) {
        doc.nextTableRow();
        doc.nextTableCell(FORMAT_HEADER3);
        doc.addText("" + (c + 1) );
        doc.nextTableCell("number-columns-spanned=5");
        addIndiString(children[c], doc) ;
      }
    }

    /** ************** Notes */
    propArray = fam.getProperties("NOTE");
    boolean seenNote = false;
    for (int i = 0; i < propArray.length; i++) {
      prop = (Property) propArray[i];
      if (todos.contains(prop))
        continue;
      if (!seenNote) {
        doc.nextTableRow(FORMAT_HEADER2);
        doc.nextTableCell("number-columns-spanned=6");
        doc.addText( translate("main.notes") );
        seenNote = true;
      }
      doc.nextTableRow();
      doc.nextTableCell();
      doc.nextTableCell("number-columns-spanned=5");
      outputPropertyValue(prop, doc);
    }

    /** ************** Todos */
    doc.nextTableRow(FORMAT_HEADER2);
    doc.nextTableCell("number-columns-spanned=6");
    doc.addText( translate("titletodo") );
    for (int i = 0; i < todos.size(); i++) {
      prop = (Property) todos.get(i);
      Property parent = prop.getParent();
      doc.nextTableRow();
      if (parent instanceof Fam) {
        doc.nextTableCell();
        doc.nextTableCell("number-columns-spanned=5");
        outputPropertyValue(prop, doc);
      } else {
    	  doc.nextTableCell(FORMAT_HEADER3_TODO);
        doc.addText( Gedcom.getName(parent.getTag()) );
        doc.nextTableCell("number-columns-spanned=5,");
        doc.addText( parent.format(PLACE_AND_DATE_FORMAT) );
        doc.nextParagraph();
        outputPropertyValue(prop,doc);
        doc.nextParagraph();
        doc.addText( outputProperty(prop, prop.getPath().toString() + ":REPO") );
        doc.nextParagraph();
        doc.addText( outputProperty(prop, prop.getPath().toString() + ":NOTE") );
      }
    }

    // done with fam
  }

  /**
   * Exports an individual
   */
  private void exportEntity(Indi indi, Document doc) {
    Property prop;
    Property[] propArray;
    List todos;
    String tempString = "";

    todos = findProperties(indi);
    if (todos.size() == 0)
      return;

    doc.nextTableRow(ROW_FORMAT_HEADER1);
    doc.nextTableCell("number-columns-spanned=6");
    doc.addText( translate("titleindi", new String[] { indi.getName(), indi.getId() }) );

    doc.nextTableRow();
    doc.nextTableCell("number-columns-spanned=6,"+FORMAT_HEADER2);
    doc.addText( translate("titleinfosperso") );

    doc.nextTableRow();
    doc.nextTableCell(FORMAT_HEADER3);
    doc.addText( Gedcom.getName("NAME") );
    doc.nextTableCell("number-columns-spanned=3");
    doc.addText( indi.getLastName()+" ", FORMAT_STRONG );
    doc.addText( indi.getFirstName() );
    doc.nextTableCell();
    doc.addText( "ID: " + indi.getId() );
    doc.nextTableCell();
    doc.addText( Gedcom.getName("SEX") + ": " + PropertySex.getLabelForSex(indi.getSex()) );

    doc.nextTableRow();
    doc.nextTableCell(FORMAT_HEADER3);
    doc.addText( Gedcom.getName("NICK"));
    doc.nextTableCell("number-columns-spanned=5");
    doc.addText( outputProperty(indi, "INDI:NAME:NICK") );

    outputEventRow(indi, "BIRT", todos, doc);
    outputEventRow(indi, "BAPM", todos, doc);
    outputEventRow(indi, "DEAT", todos, doc);
    outputEventRow(indi, "BURI", todos, doc);

    doc.nextTableRow();
    doc.nextTableCell(FORMAT_HEADER3);
    doc.addText( Gedcom.getName("REFN") );
    doc.nextTableCell("number-columns-spanned=3");
    doc.addText( outputProperty(indi, "INDI:REFN") );
    doc.nextTableCell(FORMAT_HEADER3);
    doc.addText( Gedcom.getName("CHAN") );
    doc.nextTableCell();
    doc.addText( outputProperty(indi, "INDI:CHAN") );

    Fam fam = indi.getFamilyWhereBiologicalChild();
    if (fam != null) {
      doc.nextTableRow();
      doc.nextTableCell(FORMAT_HEADER3);
      doc.addText( translate("father") + ":" );
      doc.nextTableCell("number-columns-spanned=5");
      addIndiString(fam.getHusband(), doc) ;

      doc.nextTableRow();
      doc.nextTableCell(FORMAT_HEADER3);
      doc.addText( translate("mother") + ":" );
      doc.nextTableCell("number-columns-spanned=5");
      addIndiString(fam.getWife(), doc) ;
    }

    // And we loop through its families
    Fam[] fams = indi.getFamiliesWhereSpouse();
    if (fams.length > 0) {
      doc.nextTableRow();
      doc.nextTableCell("number-columns-spanned=6,"+FORMAT_HEADER2);
      doc.addText( Gedcom.getName("FAM", fams.length > 1) );
    }

    for (int f = 0; f < fams.length; f++) {
      // .. here's the fam and spouse
      Fam famc = fams[f];
      Indi spouse = famc.getOtherSpouse(indi);
      if (spouse != null) {
        Indi[] children = famc.getChildren();

        doc.nextTableRow();
        doc.nextTableCell("number-rows-spanned="+(children.length+1)+","+FORMAT_HEADER3);
        doc.addText(translate("spouse") + ":" );
        doc.nextTableCell("number-columns-spanned=5");
        addIndiString(spouse, doc) ;
        doc.nextParagraph();
        doc.addText( Gedcom.getName("MARR") + " : ",FORMAT_STRONG);
        doc.addText( famc.format("MARR", PLACE_AND_DATE_FORMAT) ); // 0, 5

        if (children.length > 0) {

          doc.nextTableRow();
          doc.nextTableCell("number-rows-spanned="+children.length+","+FORMAT_HEADER4);
          doc.addText(Gedcom.getName("CHIL", children.length > 1) );
          doc.nextTableCell("number-columns-spanned=4");
          addIndiString(children[0], doc) ;
          for (int c = 1; c < children.length; c++) {
            doc.nextTableRow();
            doc.nextTableCell("number-columns-spanned=4");
            addIndiString(children[c], doc) ;
          }
        }
      }
    }

    doc.nextTableRow();
    doc.nextTableCell("number-columns-spanned=6,"+FORMAT_HEADER2);
    doc.addText( Gedcom.getName("EVEN", true) );

    outputEventRow(indi, "OCCU", todos, doc);
    outputEventRow(indi, "RESI", todos, doc);

    /** ************** Notes */
    propArray = indi.getProperties("NOTE");
    boolean seenNote = false;
    for (int i = 0; i < propArray.length; i++) {
      prop = (Property) propArray[i];
      if (todos.contains(prop))
        continue;
      if (!seenNote) {
        doc.nextTableRow();
        doc.nextTableCell("number-columns-spanned=6,"+FORMAT_HEADER2);
        doc.addText( translate("main.notes") );
        seenNote = true;
      }
      doc.nextTableRow();
      doc.nextTableCell("number-columns-spanned=6");
      outputPropertyValue(prop, doc);
    }

    /** ************** Todos */
    doc.nextTableRow();
    doc.nextTableCell("number-columns-spanned=6,"+FORMAT_HEADER2);
    doc.addText( translate("titletodo") );
    for (int i = 0; i < todos.size(); i++) {
      prop = (Property) todos.get(i);
      Property parent = prop.getParent();
      String row;
      if (parent instanceof Indi) {
        doc.nextTableRow();
        doc.nextTableCell();
        doc.nextTableCell("number-columns-spanned=5");
        outputPropertyValue(prop,doc);
      } else {
        doc.nextTableRow();
        doc.nextTableCell(FORMAT_HEADER3_TODO);
        doc.addText( Gedcom.getName(parent.getTag()) );
        doc.nextTableCell("number-columns-spanned=5");
        doc.addText( parent.format(PLACE_AND_DATE_FORMAT) );
        doc.nextParagraph();
        outputPropertyValue(prop, doc);
        doc.nextParagraph();
        doc.addText( formatString("", outputProperty(prop, prop.getPath().toString() + ":REPO"), "") );
        doc.nextParagraph();
        doc.addText( formatString("", outputProperty(prop, prop.getPath().toString() + ":NOTE"), "") );
      }
    }
  }

  /**
   * create a row for an event
   */
  private void outputEventRow(Entity indi, String tag, List todos, Document doc) {

    if (indi == null)
      return;

    Property props[] = indi.getProperties(tag);
    if (props.length==0)
      return;

    if (props.length == 1) {

      doc.nextTableRow();
      doc.nextTableCell(FORMAT_HEADER3);
      doc.addText( Gedcom.getName(tag) );
      doc.nextTableCell("number-columns-spanned=5");
      doc.addText( indi.format(tag, PLACE_AND_DATE_FORMAT) );
      doc.nextParagraph();
      outputNotes( "Note : ", indi.getProperty(tag), todos, doc); // Note should be emphasized

      return;
    }

    for (int i = 0; i < props.length; i++) {
      doc.nextTableRow();
      doc.nextTableCell(FORMAT_HEADER3);
      doc.addText(Gedcom.getName(tag) );
      doc.nextTableCell("number-columns-spanned=5");
      doc.addText( props[i].format(PLACE_AND_DATE_FORMAT) );
      doc.nextParagraph();
      outputNotes( "Note : ", props[i], todos, doc); // Note should be emphasized
    }

    // done
  }

  /**
   * Export todo summary only into a 5 column table
   */
  private int exportSummary(Entity[] ents, Document doc) {

    List todos;
    boolean isFirstPage = true;
    int nbTodos = 0;

    // loop over all entities
    for (int e = 0; e < ents.length; e++) {

      todos = findProperties(ents[e]);
      if (todos.size() == 0)
        continue;

      // loop over todos for entity
      for (int i = 0; i < todos.size(); i++) {
        Property prop = (Property) todos.get(i);
        if ((prop instanceof PropertyMultilineValue)) continue;
        Property parent = prop.getParent();

        if (parent != null){
        	doc.nextTableRow();
        if ((parent instanceof Entity)) {
          doc.nextTableCell();
          doc.nextTableCell();
          doc.nextTableCell();
        } else {
          doc.addText( Gedcom.getName(parent.getTag()) );
          doc.nextTableCell();
          doc.addText( parent.getPropertyDisplayValue("DATE") );
          doc.nextTableCell();
          doc.addText( parent.getPropertyDisplayValue("PLAC") );
        }
        doc.nextTableCell();
        doc.addText( prop.getEntity().toString() );
        doc.nextTableCell();
        outputPropertyValue(prop, doc);

        nbTodos++;
        }
      }
    }

    // done
    return nbTodos;
  }

  // private void exportTodosCsv(Entity[] ents) {
  // List todos;
  // boolean isFirstPage=true;
  //
  // for (int e = 0; e < ents.length; e++) {
  // todos = findProperties(ents[e]);
  // if (todos.size() == 0){
  // continue;
  // }
  // for (int i = 0; i < todos.size(); i++){
  // Property prop = (Property) todos.get(i);
  // Property parent = prop.getParent();
  // String row;
  // if (parent instanceof Indi) {
  // row = ",,";
  // } else if (parent instanceof Fam) {
  // row = ",,";
  // } else {
  // row = "\""+Gedcom.getName(parent.getTag())+parent.getValue()+"\"";
  // row += ",\""+parent.getPropertyDisplayValue("DATE")+"\"";
  // row += ",\""+parent.getPropertyDisplayValue("PLAC")+"\"";
  // }
  // row += ",\""+(prop.getEntity()).toString()+"\"";
  // row += ",\""+outputPropertyValue(prop)+"\"";
  // println(row);
  // }
  // }
  // }

  /**
   * Output notes for given property
   */
  private void outputNotes(String prefix, Property prop, List exclude, Document doc) {
    // prop exists?
    if (prop == null)
      return;

    Property[] props = prop.getProperties("NOTE");
    for (int i = 0; i < props.length; i++) {
      if (exclude.contains(props[i]))
        continue;
      doc.addText( prefix ,FORMAT_STRONG);
      outputPropertyValue(props[i], doc);
    }

    // done
  }

  private String outputProperty(Property prop, String tagPath) {
    Property subProp = prop.getPropertyByPath(tagPath);
    return (subProp == null) ? "" : subProp.toString();
  }

  private String formatString(String start, String middle, String end) {
    if (middle != null && middle.length() != 0) {
      return ((start == null) ? "" : start) + middle
          + ((end == null) ? "" : end);
    } else {
      return "";
    }
  }

  private void addIndiString(Indi indi, Document doc) {
    // Might be null
    if (indi == null)
      return ;
    String birth = indi.format("BIRT", OPTIONS.getBirthSymbol() + PLACE_AND_DATE_FORMAT);
    String death = indi.format("DEAT", OPTIONS.getDeathSymbol() + PLACE_AND_DATE_FORMAT);
    doc.addText(indi.toString(),FORMAT_STRONG);
    doc.addText(" " + birth + " " + death);
  }

  /**
   * Exports the given property's value
   */
  private void outputPropertyValue(Property prop, Document doc) {

    // check for links to other indi/fams
    if (prop instanceof PropertyXRef) {
      PropertyXRef xref = (PropertyXRef) prop;
      outputPropertyValue( xref.getTargetEntity(), doc);
      return;
    }

    // simply property - use display value
    if (!(prop instanceof MultiLineProperty)) {
      doc.addText(prop.getDisplayValue());
      return;
    }

    // multilines
     StringTokenizer lines = new StringTokenizer(prop.getValue(), "\n");
     while (lines.hasMoreTokens()) {
         doc.nextParagraph();
         doc.addText(lines.nextToken());
     }
    // done
  }

  private List findProperties(Property of) {
    return of.findProperties(Pattern.compile(todoTag), Pattern.compile(
        todoStart + ".*", Pattern.DOTALL));
  }

} // ReportToDo
