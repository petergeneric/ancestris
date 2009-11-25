/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package merge;

import genj.fo.Document;
import genj.report.Report;
import genj.gedcom.Gedcom;
import genj.gedcom.TagPath;
import genj.gedcom.Entity;
import genj.gedcom.Property;

import javax.swing.ImageIcon;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Arrays;
import javax.swing.JOptionPane;


/**
 * GenJ - Report
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
public class ReportMerge extends Report {

  private static int LINESIZE = 45;
  private final int LNS = 80;

  /** Settings */
  public int setting_action = 0;
  public String setting_actions[] = { 
     translate("setting_chkdup_action"),
     translate("setting_appendFiles_action"),
     translate("setting_merge_action"),
     translate("setting_assessOnly_action"),
     };

  // ask above, do not ask below (no merge)
  public int setting_askThreshold = 50;

  // entities included in the analysis
  public int setting_analysis1 = 0;
  public String setting_analysis1s[] = { 
     translate("setting_whole"),
     translate("setting_filter")
     };

  // Assessment options
  public boolean setting_default = true;              // true means to default missing information to next best information
  public boolean setting_approximate = true;          // true means that a non perfect equality is still considered
  public boolean setting_differencemeansno = true;    // true means that in case of approximation, if the difference is too big, it means it is not a match
  public boolean setting_phonex = true;               // true means that lastnames (or firstnames) of identical phonex will be considered to match


  // execution options
  public boolean setting_displayMergeHistory = true; 
  public boolean setting_logOption = false;

  // merge without asking above this level
  public int setting_autoMergingLevel = 90;
  public int setting_ruleEntity = 0;
  public String setting_ruleEntitys[] = { 
     translate("setting_alwaysA"),
     translate("setting_alwaysB"),
     translate("setting_Aconflict"),
     translate("setting_Bconflict"),
     translate("setting_askConflict")
     };
  public int setting_headerChosen = 0;
  public String setting_headerChosens[] = { 
     translate("setting_headerA"),
     translate("setting_headerB")
     };
  public String setting_outputFileExt = "-new.ged";
  public boolean setting_keepAecl = true;
  public boolean setting_keepAcon = true;
  public boolean setting_keepBcon = true;
  public boolean setting_keepBecl = true;
  public boolean setting_flagChanges = true;

  /** other variables */
  Document doc = null;
  private Log log = null;
  private final static String FORMAT_CBACKGROUND = "background-color=#ffffcc,font-weight=bold,text-align=center";

  public boolean usesStandardOut() { return true; }

  /**
   * Our main logic
   */
  public void start(Gedcom gedcom) {

    // Open log
    log = new Log(this, translate("mergeGedcom"), translate("chooseLog"), setting_logOption);
    if (log == null || log.logFile == null) return;

    // Fix settings if out of bound
    setting_autoMergingLevel = Math.min(Math.max(0,setting_autoMergingLevel),100);
    setting_askThreshold     = Math.min(Math.max(0,setting_askThreshold),100);
    if (setting_outputFileExt.length() == 0) setting_outputFileExt = "-new.ged";

    // Run merge tool
    MergeGedcomTool mergeTool = new MergeGedcomTool(this, log);
    boolean ret = mergeTool.run(gedcom);

    // Produce Merge History Report if required
    if (ret && setting_displayMergeHistory) {
       List confidenceListOutput = mergeTool.getConfidenceListOutput();
       Gedcom gedcomB = mergeTool.getGedcomB();
       Gedcom gedcomOutput = mergeTool.getGedcomOutput();
       log.write(1, 1, "=", LNS, translate("prodFile"));
       log.write(0, 3, "", 0, translate("gedcomOutput")+"="+gedcomOutput);
       log.timeStamp(6, translate("logStart")+": ");
       int i = showMergeResults(confidenceListOutput, gedcom, gedcomB, gedcomOutput);
       log.timeStamp(6, translate("logEnd")+": ");
       log.write(0, 6, "", 0, translate("logErrorLineWtn")+": "+i);
       }
    log.write(" ");

    // done
    if (ret) {
       log.write(1, 1, "=", LNS, translate("completed"));
       String msg = translate("completed")+"!  ";
       if ((setting_action != 0) && (setting_action != 3)) 
          msg += translate("fileIn", mergeTool.getGedcomOutput().getOrigin().getFile().getAbsolutePath()); 
       JOptionPane.showMessageDialog(null, msg , getName(), JOptionPane.INFORMATION_MESSAGE);
       }
    else log.write(9, 0, "=", LNS, translate("Error"));

    // close log
    if (setting_logOption) {
       log.close();
       log.write(0, 3, "", 0, translate("logLogFile")+": "+log.getLogName());
       }

    // Done
    if (doc != null) showDocumentToUser(doc);

  } // end_of_start




  /**
   * Report for merge
   */
  public int showMergeResults(List confList, Gedcom gedcomA, Gedcom gedcomB, Gedcom gedcomC) {

     int i = 0; 

     // write main file out
     doc = new Document(getName());
     i = produceMergeResults(doc, confList, gedcomA, gedcomB, gedcomC);
     i += produceMergeHistory(doc, gedcomC);

     return i;
     }


  public int produceMergeResults(Document doc, List confList, Gedcom gedcomA, Gedcom gedcomB, Gedcom gedcomC) {

     // write merge results
     doc.startSection(translate("repSecMrgRes"));
     doc.startTable("genj:csv=true,width=150%"); 
     doc.addTableColumn("column-width=10%"); 
     doc.addTableColumn("column-width=40%"); 
     doc.addTableColumn("column-width=40%"); 
     doc.addTableColumn("column-width=45%"); 

     doc.nextTableRow(FORMAT_CBACKGROUND);
     doc.addText(translate("reptext_Conf")); 
     doc.nextTableCell(FORMAT_CBACKGROUND);
     doc.addText(translate("reptext_EntityA")); 
     doc.nextTableCell(FORMAT_CBACKGROUND);
     doc.addText(translate("reptext_EntityB")); 
     doc.nextTableCell(FORMAT_CBACKGROUND);
     doc.addText(translate("reptext_EntityC")); 

     // loop on list items and display entities old A, old B and new C only for merged entities 
     for (Iterator it = confList.iterator(); it.hasNext(); ) {
       ConfidenceMatch match = (ConfidenceMatch)it.next();
       if (!match.confirmed || !match.toBeMerged) continue;

       doc.nextTableRow();
       doc.addText(match.confLevel+"%");
       // idA is the id of ent1
       // idB is id2
       // idC is the id of ent1 if choice is 1, of ent2 otherwise
       // old entity A is from 'gedcomA' with idA
       // old entity B is from 'gedcomB' with idB
       // new entity C is from GedcomC with idC
       doc.nextTableCell();
       addText(doc, gedcomA.getEntity(match.ent1.getId()), (match.choice == 1) ? "#207320" : "#c92525");
       doc.nextTableCell();
       addText(doc, gedcomB.getEntity(match.id2), (match.choice == 2) ? "#207320" : "#c92525");
       doc.nextTableCell();
       addText(doc, (match.choice == 1) ? gedcomC.getEntity(match.ent1.getId()) : gedcomC.getEntity(match.ent2.getId()), "#000000");

       doc.nextTableRow();
       doc.addText("__________");
       doc.nextTableCell();
       doc.addText("__________________________________________________");
       doc.nextTableCell();
       doc.addText("__________________________________________________");
       doc.nextTableCell();
       doc.addText("__________________________________________________");
       }

     return confList.size();
     }


  public int produceMergeHistory(Document doc, Gedcom gedcom) {

     List entList = gedcom.getEntities();
     int i = 0; 

     // write merge history of the main file
     doc.startSection(translate("repSecMrgHist"));
     doc.startTable("genj:csv=true,width=100%"); 
     doc.addTableColumn("column-width=30%"); 
     doc.addTableColumn("column-width=20%"); 
     doc.addTableColumn("column-width=25%"); 
     doc.addTableColumn("column-width=25%"); 

     doc.nextTableRow(FORMAT_CBACKGROUND);
     doc.addText(translate("reptext_Date")); 
     doc.nextTableCell(FORMAT_CBACKGROUND);
     doc.addText(translate("reptext_File")); 
     doc.nextTableCell(FORMAT_CBACKGROUND);
     doc.addText(translate("reptext_Updated")); 
     doc.nextTableCell(FORMAT_CBACKGROUND);
     doc.addText(translate("reptext_Added")); 

     // loop on properties
     for (Iterator it = entList.iterator(); it.hasNext();) {
       Property prop = (Property) it.next();
       List propList = new ArrayList();
       propList.addAll(Arrays.asList(prop.getProperties(new TagPath(".:_MRG-NEW"))));
       propList.addAll(Arrays.asList(prop.getProperties(new TagPath(".:_MRG-UPD"))));
       for (Iterator it2 = propList.iterator(); it2.hasNext();) {
         Property prop2 = (Property) it2.next();
         Property propDate = (Property)prop2.getProperty(new TagPath(".:DATE"));
         Property propFile = (Property)prop2.getProperty(new TagPath(".:FILE"));
         if ((propDate == null) || (propFile == null)) continue;
         String date = propDate.getValue();
         String file = propFile.getValue();
         String entityUpdated = "";
         String entityAdded = "";
         boolean added = (prop2.getTag().compareTo("_MRG-NEW") == 0);
         if (added)
            entityAdded = ((Entity)prop).toString();
         else
            entityUpdated = ((Entity)prop).toString();

         doc.nextTableRow();
         doc.addText(date);
         doc.nextTableCell();
         doc.addText(file);
         doc.nextTableCell();
         doc.addText(entityUpdated);
         doc.nextTableCell();
         doc.addText(entityAdded);
         i++;
         }
       }

     return i;
     }

  /**
   * Get all elements of the entity in a text string 
   */
    private void addText(Document doc, Entity ent, String color) {

      if (ent == null) {
         doc.addText("null entity");
         return;
         }

      String text = ent.toString();
      doc.addText(text);

      List listProp = new LinkedList();
      Property[] properties = ent.getProperties();
      listProp.addAll(Arrays.asList(properties));
      Property propItem = null;
      while (listProp.size() > 0) {
         propItem = (Property) ((LinkedList)listProp).removeFirst();
         int indent = (propItem.getPath().length() - 2) * 3 + 1;
         Property[] subProps = propItem.getProperties();
         listProp.addAll(0, Arrays.asList(subProps));
         String value = propItem.toString();
         if (value.length() > LINESIZE) 
            value = value.substring(0, LINESIZE)+"...";
         text = propItem.getTag()+": "+value;
         doc.nextParagraph("start-indent="+(indent*12)+"pt");
         doc.addText(text, "color="+color);
         }
      return;
      }

} // End_of_Report
