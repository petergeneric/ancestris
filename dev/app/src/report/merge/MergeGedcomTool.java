/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package merge;

import genj.report.Report;

import genj.gedcom.Gedcom;
import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.Fam;
import genj.gedcom.Property;
import genj.gedcom.GedcomException;
import genj.gedcom.Fam;
import genj.gedcom.Note;
import genj.gedcom.Source;
import genj.gedcom.Repository;
import genj.gedcom.Submitter;
import genj.gedcom.TagPath;
import genj.gedcom.PropertyXRef;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.PointInTime;
import genj.gedcom.PropertyVisitor;

import genj.option.PropertyOption;

import genj.util.swing.Action2;
import genj.util.Origin;

import genj.io.GedcomReader;
import genj.io.GedcomWriter;
import genj.io.GedcomIOException;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Stack;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import javax.swing.ImageIcon;


/**
 * GenJ - Report
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
public class MergeGedcomTool {

  private final int UNKNOWN = 0;
  private final int LNS = 80;
  private String DASHES = "-----------------";

  private ReportMerge report = null;
  private Log log = null;
  private Gedcom gedcomInputB = null;
  private Gedcom gedcomOutput = null;
  private List confidenceListOutput = null;
  private ProgressStatus progress = null;
  private boolean debug = true;
  private String[] typeEnt = {
     Gedcom.INDI,
     Gedcom.FAM,
     Gedcom.NOTE,
     Gedcom.SOUR,
     Gedcom.REPO,
     Gedcom.SUBM 
     };

  // Check for duplicates only within original files
  private boolean setting_chkdup = true;
  // do not merge, assess overlap only
  private boolean setting_assessOnly = false;
  // do not merge, just add files together
  private boolean setting_appendFiles = false;

  int  RUN_CHKDUP = 0, 
       RUN_APPEND = 1, 
       RUN_MERGE = 2, 
       RUN_ASSESS = 3; 
  int  ANA1_WHOLE = 0, 
       ANA1_SUBSET = 1; 
  int  ANA3_CONNEC = 0, 
       ANA3_ENTONL = 1; 
  int  ALWAYS_A = 0, 
       ALWAYS_B = 1, 
       A_CONFLICT = 2, 
       B_CONFLICT = 3, 
       ASK_CONFLICT = 4; 

  protected final static int
    OPTION_YESNO    = 0,
    OPTION_OKCANCEL = 1,
    OPTION_OK       = 2;

  public MergeGedcomTool(ReportMerge report, Log log) {
     this.report = report;
     this.log = log;
     }

  /**
  * Merge Gedcom files Main entry point
  */
  public boolean run(Gedcom gedcom) {

    Map typeEntsA = new TreeMap();    // type -> entities from 1st file to be compared
    Map typeEntsB = new TreeMap();    // type -> entities from 2nd file to be compared
    Map confList = new TreeMap();     // id1xid2 -> pair confidence match score
    Map overlaps = new TreeMap();     // type of overlap -> list of entities
    Map scoreStats = new TreeMap();   // measure -> value
    Map idMap = new TreeMap();        // id -> id (for merged entities)
    double[] sizes = { 0, 0, 0 };     // number of combinations
    boolean existMatches = false;     // indicates if confidence match list not empty
    Map idNewOld = new TreeMap();     // newId -> oldId (for B file, to be able to identify the old entity from the B file)

    // Simplifies action options
    setting_chkdup = (report.setting_action == RUN_CHKDUP);
    setting_appendFiles = (report.setting_action == RUN_APPEND);
    setting_assessOnly = (report.setting_action == RUN_ASSESS);

    // Display execution options in log
    displayOptions(setting_chkdup);

    // Create progress window which allows to cancel process as well
    progress = new ProgressStatus(report.translate("progressTitle"), report.translate("progressMessage")+"...", "", 100, 
	report.translate("progressStop"));

    // Merge case. 
    log.write(1, 1, "=", LNS, report.translate("logPrep"));

    // Make a copy of gedcom to temporary file (A)
    String fileNameA = gedcom.getOrigin().getFile().getAbsolutePath();
    log.write(2, 3, "", 0, report.translate("logCopyA")+": "+fileNameA);
    log.timeStamp(6, report.translate("logStart")+": ");
    // Do not create Adam, do not create submitter, use header from gedcom
    Gedcom gedcomA = createGedcomFile(fileNameA+"~", false, gedcom);
    if (gedcomA == null) return false;
    if (!copyGedcom(gedcom, gedcomA)) return false;
    String subId   = gedcom.getSubmitter().getId(); 
    gedcomA.setSubmitter((Submitter) gedcomA.getEntity(subId));
    log.timeStamp(6, report.translate("logEnd")+": ");
    log.write(" ");

    // Open (B) Gedcom file to merge to by asking user to provide location
    Gedcom gedcomB = null;
    String fileNameB = "";
    if (setting_chkdup) {
       gedcomB = gedcomA;
       fileNameB = gedcomB.getOrigin().getFile().getAbsolutePath();
       }
    else {
       log.write(2, 3, "", 0, report.translate("logOpenB"));
       log.timeStamp(6, report.translate("logStart")+": ");
       gedcomB = getGedcomFromUser(report.translate("user_AskMergeFile"));
       if (gedcomB == null) return false;
       fileNameB = gedcomB.getOrigin().getFile().getAbsolutePath();
       log.write(0, 6, "", 0, report.translate("logBfileOpened", fileNameB)+".");
       log.timeStamp(6, report.translate("logEnd")+": ");
       log.write(" ");
       }

    // Changing encoding to user chosen "header file"
    if (!setting_chkdup) {
       log.write(2, 3, "", 0, report.translate("logAligningCoding"));
       log.timeStamp(6, report.translate("logStart")+": ");
       if (report.setting_headerChosen == 0) {
          gedcomB.setEncoding(gedcomA.getEncoding());
          log.write(0, 6, "", 0, report.translate("logBAligned")+" "+gedcomA.getEncoding()+".");
          } else {
          gedcomA.setEncoding(gedcomB.getEncoding());
          log.write(0, 6, "", 0, report.translate("logAAligned")+" "+gedcomB.getEncoding()+".");
          } 
       log.timeStamp(6, report.translate("logEnd")+": ");
       log.write(" ");
       }

    // Ensure that no 2 entities have the same id across both files (remap B if needed)
    if (!setting_chkdup) {
       log.write(2, 3, "", 0, report.translate("logMakingUnique"));
       log.timeStamp(6, report.translate("logStart")+": ");
       if (!makeUniqueIds(gedcomA, gedcomB, idNewOld)) return false;
       log.timeStamp(6, report.translate("logEnd")+": ");
       log.write(" ");
       }

    // Interactive matching of header & remap places of entities in the non chosen header
    if (!setting_chkdup) {
       log.write(2, 3, "", 0, report.translate("logCheckHeader"));
       log.timeStamp(6, report.translate("logStart")+": ");
       log.write(0, 6, "", 0, report.translate("logCheckPlace"));
       int[] placeMap = mapPlaceFormat(gedcomA, gedcomB, report.setting_headerChosen);
       if (placeMap == null) { 
          log.write(0, 6, "", 0, report.translate("logSamePlace"));
          }
       else {
          log.write(0, 6, "", 0, report.translate("logRemapPlace"));
          remapPlaces((report.setting_headerChosen == 0) ? gedcomB.getEntities() : gedcomA.getEntities(), placeMap);
          }
       log.timeStamp(6, report.translate("logEnd")+": ");
       log.write(" ");
       }

    // Prepare set of entities to compare (subset & optimisation)
    log.write(2, 3, "", 0, report.translate("logPrepEntities"));
    log.timeStamp(6, report.translate("logStart")+": ");
    prepareSets(gedcomA, gedcomB, typeEntsA, typeEntsB, sizes);
    log.timeStamp(6, report.translate("logEnd")+": ");
    log.write(" ");



    // Run a first assessment and display it
    if (!setting_appendFiles) {
       log.write(1, 1, "=", LNS, report.translate("logFirstAssess"));
       log.write(2, 3, "", 0, report.translate("logAnalysis"));
       log.timeStamp(6, report.translate("logStart")+": ");
       if (!assessMatches(gedcomA, gedcomB, typeEntsA, typeEntsB, confList, overlaps, scoreStats, idMap, progress, setting_chkdup, sizes[2], idNewOld))
          return false;
       //if (debug) displayConfList(confList);
       log.timeStamp(6, report.translate("logEnd")+": ");
       log.write(0, 6, "", 0, DASHES);
       log.write(0, 6, "", 0, report.translate("logTotalAnalysed") + ": " + NumberFormat.getIntegerInstance().format((int)progress.getSize()));
       log.write(" ");
       existMatches = displayMatches(confList, overlaps, scoreStats, setting_chkdup);
       log.write(" ");
       }
    else {
       log.write(1, 1, "=", LNS, report.translate("logAddingFiles"));
       log.write(2, 3, "", 0, report.translate("logMarking"));
       log.timeStamp(6, report.translate("logStart")+": ");
       if (!assessMatches(gedcomA, gedcomB, typeEntsA, typeEntsB, confList, overlaps, scoreStats, idMap, progress, setting_chkdup, sizes[2], idNewOld))
          return false;
       log.timeStamp(6, report.translate("logEnd")+": ");
       log.write(" ");
       }

    // Leave now if user only wanted to assess but not do the complete merge operation
    if (setting_assessOnly) {
       gedcomInputB = reRead(gedcomB);
       gedcomOutput = gedcom;
       return true;
       }

    // Interaction with user for matching of entities (in case list not empty)
    //  -1 => user cancels
    //   0 => return OK and nothing to do (assess 0 times after that)
    //   1 => return OK and finished with assessing (assess 1 time after that)
    //   2 => return OK and not finished with assessing (keep assessing)
    if (existMatches) {
       log.write(1, 1, "=", LNS, report.translate("logInteraction", new String[] { 
			""+report.setting_askThreshold, ""+report.setting_autoMergingLevel } ));
       log.write(2, 3, "", 0, report.translate("logAsking"));
       log.timeStamp(6, report.translate("logStart")+": ");
       int keepMatching = 2;
       while (keepMatching == 2) {
          // Confirm with user for information to keep
          keepMatching = confirmMatchesWithUser(confList);
          //if (debug) displayConfList(confList);
          if (keepMatching == -1)  
             return false;
          if (keepMatching > 0) { 
             log.timeStamp(6, report.translate("logEnd")+": ");
             log.write(" ");
             log.write(2, 3, "", 0, report.translate("logReassessing"));
             log.timeStamp(6, report.translate("logStart")+": ");
             if (!assessMatches(gedcomA, gedcomB, typeEntsA, typeEntsB, confList, overlaps, scoreStats, idMap, progress, setting_chkdup, 
		sizes[2], idNewOld))
                return false;
             log.write(0, 6, "", 0, DASHES);
             log.write(0, 6, "", 0, report.translate("logTotalAnalysed") + ": " + NumberFormat.getIntegerInstance().format((int)progress.getSize()));
             }
          }
       log.timeStamp(6, report.translate("logEnd")+": ");
       log.write(" ");
       }

    // Merge entities
    log.write(1, 1, "=", LNS, report.translate("logMergingReal"));
    log.write(2, 3, "", 0, report.translate("logApplyingRules"));
    log.timeStamp(6, report.translate("logStart")+": ");
    if (!mergeEntities(confList)) return false;
    //if (debug) displayConfList(confList);
    log.timeStamp(6, report.translate("logEnd")+": ");
    log.write(" ");

    // Display final assessment
    displayMatches(confList, overlaps, scoreStats, setting_chkdup);

    // Create (C) empty output Gedcom file which will hold merged result
    // (do not creata Adam, do not create submitter, use header from A)
    int pos = fileNameA.indexOf(".ged");
    if (pos == -1) pos = fileNameA.length();
    String fileNameC = fileNameA.substring(0, pos)+report.setting_outputFileExt;
    log.write(2, 3, "", 0, report.translate("logCreatingC")+": "+fileNameC);
    log.timeStamp(6, report.translate("logStart")+": ");
    Gedcom gedcomC = createGedcomFile(fileNameC, false, (report.setting_headerChosen == 0 ? gedcomA : gedcomB));
    log.timeStamp(6, report.translate("logEnd")+": ");
    log.write(" ");
    if (gedcomC == null) return false;

    // Produce result file
    log.write(2, 3, "", 0, report.translate("logProducingC"));
    log.timeStamp(6, report.translate("logStart")+": ");
    mergeGedcom(gedcomA, gedcomB, gedcomC, overlaps, idMap);
    log.timeStamp(6, report.translate("logEnd")+": ");
    log.write(" ");

    // Write result file for (C)
    log.write(2, 3, "", 0, report.translate("logSavingC"));
    log.timeStamp(6, report.translate("logStart")+": ");
    progress.reset(report.translate("progressSavingC")+"...", "",  100);
    if (!saveGedcom(gedcomC)) return false;
    progress.terminate();
    log.timeStamp(6, report.translate("logEnd")+": ");
    log.write(" ");

    gedcomInputB = setting_chkdup ? gedcomA : reRead(gedcomB);
    gedcomOutput = gedcomC;

    return true;
    // done

  }

 /**
  * Get Gedcom file from User
  */
  private Gedcom getGedcomFromUser(String msg) {
    // Variables
    Gedcom gedcomX = null;
    Origin originX = null;
    GedcomReader readerX;

    // Get file from user
    File fileX = report.getFileFromUser(msg, Action2.TXT_OK);
    if (fileX == null) return null;

    // Create pointer to file
    try {
       originX = Origin.create(new URL("file", "", fileX.getAbsolutePath()));
       } catch (MalformedURLException e) {
       log.write(9, 0, "=", LNS, "URLexception:"+e);
       return null;
       }

    // Create reader to pointer
    try {
       readerX = new GedcomReader(originX);
      } catch (IOException e) {
       log.write(9, 0, "=", LNS, "IOexception:"+e);
       return null;
      }

    // Build-up gedcom from reader
    try {
      gedcomX = readerX.read();
      } catch (GedcomIOException e) {
      log.write(9, 0, "=", LNS, report.translate("logErrorCreat")+":"+e);
      log.write(0, 3, "", 0, report.translate("logErrorLine")+":"+e.getLine());
      return null;
      }

    // Display warnings if any
    log.write(0, 6, "", 0, report.translate("logErrorRead")+": "+readerX.getLines());
    List warnings = readerX.getWarnings();
    log.write(0, 6, "", 0, report.translate("logWng")+": "+warnings.size());
    for (Iterator it = warnings.iterator(); it.hasNext();) {
       String wng = (String)it.next().toString();
       log.write(0, 9, "", 0, "   "+wng);
       } // end loop

    // Link entities
    linkGedcom(gedcomX);

    return gedcomX;
    }

 /**
  * Links Gedcom XReferences
  */
  private boolean linkGedcom(Gedcom gedcomX) {
    // Links gedcom XReferences
    List ents = gedcomX.getEntities();
    for (Iterator it = ents.iterator(); it.hasNext();) {
       Entity ent = (Entity)it.next();
       List ps = ent.getProperties(PropertyXRef.class);
       for (Iterator it2 = ps.iterator(); it2.hasNext();) {
          PropertyXRef xref = (PropertyXRef)it2.next();
          Property target = xref.getTarget(); 
          if (target==null) 
             try {
                xref.link();
             } catch (GedcomException e) {
               log.write(9, 0, "=", LNS, report.translate("logErrorLinking")+":"+e);
               report.getOptionFromUser(report.translate("user_wng_lnk")+": "+e, OPTION_OKCANCEL);
               return false;
             }
          }
       }
    return true;
    }

 /**
  * Re-read Gedcom (case of B file that has been changed)
  * Reason to re-read is to trace the change back to what B file was (A file is still in memory)
  */
  private Gedcom reRead(Gedcom gedcom) {

    GedcomReader readerX;
    Gedcom gedcomX;

    try {
       readerX = new GedcomReader(gedcom.getOrigin());
      } catch (IOException e) {
       return null;
      }
    try {
      gedcomX = readerX.read();
      } catch (GedcomIOException e) {
      return null;
      }
    linkGedcom(gedcomX);
    return gedcomX;
    }


 /**
  * Copy gedcom to another gedcom assuming destination gedcom is empty
  */
  private boolean copyGedcom(Gedcom gedcomX, Gedcom gedcomY) {
    // Get all entities from (X) and copy them to (Y)
    List entitiesX = gedcomX.getEntities();
    progress.reset(report.translate("progressCopying")+"...", report.translate("progressEntities"),  entitiesX.size());
    log.write(0, 6, "", 0, report.translate("logEntFound")+": "+entitiesX.size());
    int i = 0;
    try {
       for (Iterator it = entitiesX.iterator(); it.hasNext();) {
          // copy one entity
          Entity entX = (Entity)it.next();
          Entity entY = null;
          entY = gedcomY.createEntity(entX.getTag(), entX.getId());
          copyCluster(entX, entY);
          i++;
          if (((i/100)*100) == i) {
             progress.increment(100);
             if (!progress.isActive()) return false;
             }
          } // end loop to copy other entities
       } catch (GedcomException e) {
       log.write(9, 0, "=", LNS, "GedcomException:"+e);
       return false;
       }
    progress.terminate();
    log.write(0, 6, "", 0, report.translate("logEntCopied")+": "+i);

    return linkGedcom(gedcomY);
    }

  /**
   * Extract the first number bit in the string going from left to right
   */
  static private int extractNumber(String str) {

     int start = 0, end = 0;
     while (start<=end&&!Character.isDigit(str.charAt(start))) start++;
     end = start;
     while ((end<=str.length()-1)&&Character.isDigit(str.charAt(end))) end++;
     if (end == start) return 0;
     else return (int)Integer.parseInt(str.substring(start, end));
     }

 /**
  * Make unique Ids across 2 gedcom files 
  */
  private boolean makeUniqueIds(Gedcom gedcomX, Gedcom gedcomY, Map idNewOld) {
    // Ensure that all ids in Y are not already in X. If they are, find a new one
    Map idParams = new TreeMap();      // type of entity -> (int[]) last id, length
    List ids = null;                   // list of numerical ids
    int[] idp = null;                  // ipd[0] = max numeric id, idp[1] = max length
    String idStr = "";                 // temp id string

    // Store all ids from both files and keep in mind the last number of each type
    for (int i = 0 ; i < typeEnt.length ; i++) {
       // init lists and attach them to map
       idp = new int[2];
       idParams.put(typeEnt[i], idp);

       // get ids from X
       List listEnt = new ArrayList(gedcomX.getEntities(typeEnt[i]));
       Collections.sort(listEnt, sortEntities);
       int s = listEnt.size();
       if (s > 0) {
          idStr = ((Entity)listEnt.get(s-1)).getId();
          idp[1] = idStr.length();
          idp[0] = extractNumber(idStr);
          }
       log.write(0, 6, "", 0, report.translate("logNbEntB")+" "+typeEnt[i]+": "+s+"; "+report.translate("logMaxId")+" : "+idp[0]);

       // get ids from Y
       listEnt = new ArrayList(gedcomY.getEntities(typeEnt[i]));
       Collections.sort(listEnt, sortEntities);
       s = listEnt.size();
       if (s > 0) {
          idStr = ((Entity)listEnt.get(s-1)).getId();
          idp[1] = Math.max(idp[1], idStr.length());
          idp[0] = Math.max(idp[0], extractNumber(idStr));
          }
       idp[0]++;
       }

    // Scan gedcom Y to find unique ones not in X
    String idOld = "", idNew = "";
    Entity entY = null;
    try {
    for (int i = 0 ; i < typeEnt.length ; i++) {
        String prefix = Gedcom.getEntityPrefix(typeEnt[i]);
        idp = (int[])idParams.get(typeEnt[i]);
        StringBuffer buf = new StringBuffer(idp[1]);
        List listEnt = new ArrayList(gedcomY.getEntities(typeEnt[i]));
        Collections.sort(listEnt, sortEntities);
        for (Iterator it = listEnt.iterator(); it.hasNext();) {
           entY = (Entity)it.next();
           idOld = entY.getId();
           buf.setLength(0);
           buf.append(prefix);
           buf.append(idp[0]);
           while (buf.length() < idp[1]) {
              buf.insert(1, '0');
              }
           idNew = buf.toString();
           entY.setId(idNew); 
           //log.write("debug-"+idOld+" -> "+idNew);
           idNewOld.put(idNew, idOld);
           idp[0]++;
           idp[1] = Math.max(idp[1], idNew.length());
           }
        }
    } catch (GedcomException e) {
       log.write(9, 0, "=", LNS, "GedcomException:"+e);
       return false;
    }

    return true;
    }

 /**
  * Prepare sets of entities to analyse
  */
  private boolean prepareSets(Gedcom gedcomX, Gedcom gedcomY, Map typeEntsX, Map typeEntsY, double[] sizes) {

    prepareEntities(gedcomX, typeEntsX);

    prepareEntities(gedcomY, typeEntsY);

    if (!setting_chkdup) {
       log.write(0, 6, "", 0, report.translate("logBefF")+": "+ ((List)typeEntsX.get(typeEnt[0])).size() );
       log.write(0, 6, "", 0, report.translate("logBefS")+": "+ ((List)typeEntsX.get(typeEnt[0])).size() );
       trimSpans((List)typeEntsX.get(typeEnt[0]), (List)typeEntsY.get(typeEnt[0]));
       log.write(0, 6, "", 0, report.translate("logAftF")+": "+ ((List)typeEntsX.get(typeEnt[0])).size() );
       log.write(0, 6, "", 0, report.translate("logAftS")+": "+ ((List)typeEntsX.get(typeEnt[0])).size() );
       }

    double a, b;
    for (int i = 0 ; i < typeEnt.length ; i++) {
       if (i == 1) continue; // do not try to match families directly
       a = (double)((List)typeEntsX.get(typeEnt[i])).size();
       b = (double)((List)typeEntsY.get(typeEnt[i])).size();
       sizes[0] += a * a;
       sizes[1] += b * b;
       sizes[2] += a * b;
       }

    return true;
    }
 
 /**
  * Prepare entities from Gedcom (split in types, extract subset if required)
  */
  private boolean prepareEntities(Gedcom gedcomX, Map typeEntX) {

     int
       CHOICE_ANC = 0,
       CHOICE_DEC = 1;
     int choice = CHOICE_ANC;

     Indi indiSel = null;
     int n = -1;

     // Loop for each type of entity
     for (int i = 0 ; i < typeEnt.length ; i++) {
        List listEnt = new ArrayList(gedcomX.getEntities(typeEnt[i]));

        // In case of subset required, ask user to select (INDI only)
        if (i == 0) {
           if (report.setting_analysis1 == ANA1_SUBSET) {
              indiSel = (Indi)report.getEntityFromUser(report.translate("user_individual"), gedcomX, Gedcom.INDI);
              if (indiSel != null) {
                 // Ask whether we need to select ancestorsof or descendant of 
                 String choices[] = {
                    report.translate("user_limitAncestors")+" "+indiSel.toString(),
                    report.translate("user_limitDescendant")+" "+indiSel.toString()
                    };
                 String sel = (String)report.getValueFromUser(report.translate("user_choose_subset"), choices, choices[0]);
                 if (sel != null) {
                    log.write(0, 6, "", 0, sel);
                    n = ((sel == choices[0]) ? 0 : 1); 
                    // loop on list and remove undesired
                    for (Iterator it = listEnt.iterator(); it.hasNext();) {
                       Indi indi = (Indi)it.next();
                       if (indi == indiSel) continue;
                       if ((n == CHOICE_ANC) && (!indi.isAncestorOf(indiSel)))
                          it.remove();
                       if ((n == CHOICE_DEC) && (!indi.isDescendantOf(indiSel)))
                          it.remove();
                          }
                    } 
                 }
              }
           // Assigns lists for relevant sets
           // Populate persons table
           List listPersons = new ArrayList();
           Map indi2Person = new TreeMap();
           for (Iterator it = listEnt.iterator(); it.hasNext();) {
              Indi indi = (Indi)it.next();
              PersonFactory pf = new PersonFactory(indi);
              Person p = pf.create();
              indi2Person.put(indi, p);
              listPersons.add(p);
              //if (debug) log.write("   adding "+indi.toString());
              }
           // Allocate relatives of each person
           for (Iterator it = listPersons.iterator(); it.hasNext();) {
              Person p = (Person)it.next();
              PersonFactory.getRelatives(p, indi2Person);
              }
           // Sort list based on Person's span
           Collections.sort(listPersons, new PersonFactory());
           typeEntX.put(typeEnt[i], listPersons);
           }

        // Case of Families (empty set only)
        if (i == 1) {
           // Do nothing (put empty set)
           typeEntX.put(typeEnt[i], new ArrayList());
           }

        // Case of all other entities
        if (i > 1) {
           // Populate info table
           List listInfos = new ArrayList();
           for (Iterator it = listEnt.iterator(); it.hasNext();) {
              Entity ent = (Entity)it.next();
              InfoFactory inF = new InfoFactory(ent);
              Info info = inF.create();
              if (info.titleLength != 0) {
                 listInfos.add(info);
                 //if (debug) log.write("   adding "+inF.toString(info));
                 }
              }
           // Sort list based on ids
           Collections.sort(listInfos, new InfoFactory());
           typeEntX.put(typeEnt[i], listInfos);
           }

        } // end of loop

     return true;
     }

 /**
  * Trim edges of persons
  */
  private boolean trimSpans(List personX, List personY) {

     // Remove entities for which spans < max(min(A), min(B)) and 
     //                           spans > min(max(A), max(B))
     // (these entities would not have compatible entities in the other set)

     Person minMax = new Person();
     minMax.yearMin = -Integer.MAX_VALUE;
     minMax.yearMax = Integer.MAX_VALUE;

     // Get min and max of spanX     
     Person spanMinX = new Person();
     spanMinX.yearMin = Integer.MAX_VALUE;
     spanMinX.yearMax = Integer.MAX_VALUE;
     Person spanMaxX = new Person();
     spanMaxX.yearMin = -Integer.MAX_VALUE;
     spanMaxX.yearMax = -Integer.MAX_VALUE;
     for (Iterator it = personX.iterator(); it.hasNext(); ) {
        Person p = (Person)it.next();
        if (PersonFactory.compareSpans(p, spanMaxX) > 0) PersonFactory.copy(p, spanMaxX);
        if (PersonFactory.compareSpans(p, spanMinX) < 0) PersonFactory.copy(p, spanMinX);
        }

     // Get min and max of spanY     
     Person spanMinY = new Person();
     spanMinY.yearMin = Integer.MAX_VALUE;
     spanMinY.yearMax = Integer.MAX_VALUE;
     Person spanMaxY = new Person();
     spanMaxY.yearMin = -Integer.MAX_VALUE;
     spanMaxY.yearMax = -Integer.MAX_VALUE;
     for (Iterator it = personY.iterator(); it.hasNext(); ) {
        Person p = (Person)it.next();
        if (PersonFactory.compareSpans(p, spanMaxY) > 0) PersonFactory.copy(p, spanMaxY);
        if (PersonFactory.compareSpans(p, spanMinY) < 0) PersonFactory.copy(p, spanMinY);
        }

     // Trim edges of span sets
     for (Iterator it = personX.iterator(); it.hasNext(); ) {
        Person p = (Person)it.next();
        if ((PersonFactory.compareSpans(p, spanMinY) < 0) && (PersonFactory.areNotOverlapping(p, spanMinY))) {
           it.remove(); 
           }
        if ((PersonFactory.compareSpans(p, spanMaxY) > 0) && (PersonFactory.areNotOverlapping(p, spanMaxY))) {
           it.remove(); 
           }
        }
     for (Iterator it = personY.iterator(); it.hasNext(); ) {
        Person p = (Person)it.next();
        if ((PersonFactory.compareSpans(p, spanMinX) < 0) && (PersonFactory.areNotOverlapping(p, spanMinX))) {
           it.remove(); 
           }
        if ((PersonFactory.compareSpans(p, spanMaxX) > 0) && (PersonFactory.areNotOverlapping(p, spanMaxX))) {
           it.remove(); 
           }
        }

   return true;
   }

 /**
  * Assess matches across 2 Gedcom files (they can represent the same file)
  */
  private boolean assessMatches(Gedcom gedcomX, Gedcom gedcomY, Map typeEntsX, Map typeEntsY, Map confList, Map overlaps, Map scoreStats, Map idMap, ProgressStatus progress, boolean duplicates, double size, Map idNewOld) {

   // 4 things to do: 
   //   1/ Run algorithms to match entities
   //   2/ Scan entities to match corresponding families
   //   3/ Entities connected to the overlap 
   //   4/ Overall metrics 

   // 1/ and 2/ only if not a straight append of both gedcoms
   if (!setting_appendFiles) {
      // 1/ Assess all pair confidence of matching
      //    inputList = Map (id1xid2 -> confidence) 
      List listX = new ArrayList();
      List listY = new ArrayList();

      String msg = report.translate("logAssessMatches")+"...";
      if (duplicates) {
         msg = report.translate("logAssessDup")+"...";
         }
      progress.reset(report.translate("progressCombAnal")+" - "+msg, report.translate("progressCombCheck"),  size);
      for (int i = 0 ; i < typeEnt.length ; i++) {
         if (i == 1) continue; // do not try to match families directly
         log.write(0, 6, "", 0, report.translate("logAnalysingType")+" "+typeEnt[i]);
         progress.setTitle(report.translate("progressCombAnal")+" "+typeEnt[i]+" - "+msg);
         listX = (List)typeEntsX.get(typeEnt[i]);
         listY = (List)typeEntsY.get(typeEnt[i]);
         if ((listX == null) || (listX.isEmpty())) continue;
         if (i == 0) {
            if (duplicates) {
               if (!runAlgoDupIndi(listX, listY, confList, progress, idNewOld)) return false;
               }
            else {
               if (!runAlgoMatchIndi(listX, listY, confList, progress, idNewOld)) return false;
               }
            }
         else {
            if (duplicates) {
               if (!runAlgoDupInfo(listX, listY, confList, progress, idNewOld)) return false;
               }
            else {
               if (!runAlgoMatchInfo(listX, listY, confList, progress, idNewOld)) return false;
               }
            }
         }

      progress.terminate();

      // Perform second level assessment - ensures entities are matched only once
      // (prefer their best score) and remove toBeMerged flag if pair is de-matched
      // (PERFORM THIS ONLY IF DIFFERENT GEDCOMS)
      if (!duplicates) {
         List set1 = new ArrayList();
         List set2 = new ArrayList();
         ConfidenceMatch match = null;
         // Sort by descending confidence levels
         List valList = new ArrayList(confList.values());
         Collections.sort(valList, new ConfidenceMatch());
         for (Iterator it = valList.iterator(); it.hasNext(); ) {
            match = (ConfidenceMatch)it.next();
            if ((set1.contains(match.ent1)) || (set2.contains(match.ent2))) {
               match.confirmed = true;
               match.toBeMerged = false;
               match.choice = 0;
               }
            else {
               set1.add(match.ent1);
               set2.add(match.ent2);
               }
            }
         }

      //
      // 2/ Check impact of merged entities on families and match relevant ones
      //
      assessFamilies(gedcomX, gedcomY, duplicates, confList, idNewOld);
      }

   //
   // 3/ Determine sub-trees of entities 
   //    overlaps = Map (type of overlap -> list of entities); 

   List Xexclusive = new ArrayList();  // exclusive X file
   List Xconnected = new ArrayList();  // connected non mathcing X file
   List Xmatching  = new ArrayList();  // overlap X file
   List Yexclusive = new ArrayList();  // exclusive Y file
   List Yconnected = new ArrayList();  // connected non mathcing Y file
   List Ymatching  = new ArrayList();  // overlap Y file
   List ZfromX     = new ArrayList();  // Entities of Z coming from X
   List ZfromY     = new ArrayList();  // Entities of Z coming from Y

   HashSet Xsubtrees = getTrees(gedcomX);
   HashSet Ysubtrees = getTrees(gedcomY);

   HashSet XtempTrees = new HashSet(Xsubtrees);
   HashSet YtempTrees = new HashSet(Ysubtrees);

   ConfidenceMatch match = null;

   String fileNameB = gedcomY.getOrigin().getFile().getName();
   String key = "";
   for (Iterator it = confList.keySet().iterator(); it.hasNext(); ) {
     key = (String)it.next();
     match = (ConfidenceMatch)confList.get(key);
     if (match.toBeMerged) {
        Xmatching.add(match.ent1);
        Ymatching.add(match.ent2);
        Iterator itmp = XtempTrees.iterator();
        while (itmp.hasNext()) {
           HashSet listEnts = (HashSet)itmp.next();
           if (listEnts.contains(match.ent1))   {
              Xconnected.addAll(listEnts);
              XtempTrees.remove(listEnts);
              break;
              }
           }
        itmp = YtempTrees.iterator();
        while (itmp.hasNext()) {
           HashSet listEnts = (HashSet)itmp.next();
           if (listEnts.contains(match.ent2))   {
              Yconnected.addAll(listEnts);
              YtempTrees.remove(listEnts);
              break;
              }
           }
        if ((match.choice == 1) || (match.choice == 3)) {
           flagEntity(match.ent1, "UPD", fileNameB);
           ZfromX.add(match.ent1); 
           idMap.put(match.ent2.getId(), match.ent1.getId());
           }
        else { 
           flagEntity(match.ent2, "UPD", fileNameB);
           ZfromY.add(match.ent2);
           idMap.put(match.ent1.getId(), match.ent2.getId());
           }
        }
     }

   // Remove matching entities from the connected set to avoid double counting
   Xconnected.removeAll(Xmatching);
   Yconnected.removeAll(Ymatching);

   // Deduct the excluded sets
   while (!XtempTrees.isEmpty()) {
      HashSet listEnts = (HashSet)XtempTrees.iterator().next();
      Xexclusive.addAll(listEnts);
      XtempTrees.remove(listEnts);
      }
   while (!YtempTrees.isEmpty()) {
      HashSet listEnts = (HashSet)YtempTrees.iterator().next();
      Yexclusive.addAll(listEnts);
      YtempTrees.remove(listEnts);
      }

   // Clean for entities that won't make it to Z (that is the entities not kept in each pair matched...)
   for (Iterator it = Xmatching.iterator(); it.hasNext(); ) {
     Entity ent = (Entity)it.next();
     if (!ZfromX.contains(ent)) {
        it.remove();
        }
     }  
   for (Iterator it = Ymatching.iterator(); it.hasNext(); ) {
     Entity ent = (Entity)it.next();
     if (!ZfromY.contains(ent)) {
        it.remove();
        }
     if (duplicates) { // In case of duplicates, need to remove Y matching from X connected and X exclusive in any situation
        Xconnected.remove(ent);
        Xexclusive.remove(ent);
        }
     }
   //... and in case of duplicates, the Yconnected and Yexclusive sets
   if (duplicates) {
      Yconnected.clear();
      Yexclusive.clear();
      }

   // Consolidate
   if (report.setting_keepAecl) ZfromX.addAll(Xexclusive);
   if (report.setting_keepAcon) ZfromX.addAll(Xconnected);
   if (report.setting_keepBcon) ZfromY.addAll(Yconnected);
   if (report.setting_keepBecl) ZfromY.addAll(Yexclusive);

   // Store in overlaps table
   overlaps.put("A1ecl", Xexclusive);
   overlaps.put("A2con", Xconnected);
   overlaps.put("A3mat", Xmatching);
   overlaps.put("B3ecl", Yexclusive);
   overlaps.put("B2con", Yconnected);
   overlaps.put("B1mat", Ymatching);
   overlaps.put("ZfA",  ZfromX);
   overlaps.put("ZfB",  ZfromY);

   //
   // 4/ Assess overall score
   //    scoreStats = Map (measure -> value); size: 5
   int max = 0, sum = 0, count = 0, 
       manual = 0, automatic = 0, density = 0, average = 0;

   for (Iterator it = confList.keySet().iterator(); it.hasNext(); ) {
     key = (String)it.next();
     match = (ConfidenceMatch)confList.get(key);
     max = Math.max(match.confLevel, max);
     sum += match.confLevel;
     count += 1;
     automatic  += (match.toBeMerged && (match.confLevel > report.setting_autoMergingLevel)) ? 1 : 0;
     manual += (match.toBeMerged && (match.confLevel <= report.setting_autoMergingLevel)) ? 1 : 0;
     }
   if (count == 0) count = 1;
   average = sum/count;  

   for (Iterator it = confList.keySet().iterator(); it.hasNext(); ) {
     key = (String)it.next();
     match = (ConfidenceMatch)confList.get(key);
     if ((match.confLevel > max-5) && (match.confLevel <= max))
        density += 1;
     }

   scoreStats.put(report.translate("stats_1.Average"), String.valueOf(average));
   scoreStats.put(report.translate("stats_2.Max"),     String.valueOf(max));
   scoreStats.put(report.translate("stats_3.Density"), String.valueOf(density));
   scoreStats.put(report.translate("stats_4.Auto"),    String.valueOf(automatic));
   scoreStats.put(report.translate("stats_5.Manual"),  String.valueOf(manual));

   return true;
   }

 /**
  * Algorithms
  */
  private boolean runAlgoDupIndi(List listX, List listY, Map confList, ProgressStatus progress, Map idNewOld) {
   String key = "";
   double inc = (double)listY.size();
   ConfidenceMatch match = null;

   log.write(0, 9, "", 0, report.translate("logEntExistDup")+".");
   for (Iterator it1 = listX.iterator(); it1.hasNext();) {
      Person p1 = (Person)it1.next();
      for (Iterator it2 = listY.iterator(); it2.hasNext();) {
         Person p2 = (Person)it2.next();
         if (p1.id.compareTo(p2.id) >= 0) continue;
         if (p2.yearMin > p1.yearMax) break;
         if (p2.yearMax < p1.yearMin) continue;
         if ((p1.sex != p2.sex) && (p1.sex != UNKNOWN) && (p2.sex != UNKNOWN)) continue;
         key = p1.id+"x"+p2.id;
         match = (ConfidenceMatch)confList.get(key);
         // if assessment not already there or not confirmed, perform assessment
         if ((match == null) || ((!match.confirmed) && (!match.toBeMerged))) {
            match = assessConfidenceIndi(p1, p2, setting_chkdup, idNewOld);
            if (match.confLevel >= report.setting_askThreshold) 
               confList.put(key, match);
            }
         }
      progress.increment(inc);
      if (!progress.isActive()) return false;
      }
   return true;
   }

  private boolean runAlgoMatchIndi(List listX, List listY, Map confList, ProgressStatus progress, Map idNewOld) {
   String key = "";
   double inc = (double)listY.size();
   ConfidenceMatch match = null;

   log.write(0, 9, "", 0, report.translate("logEntExistMatch")+".");
   for (Iterator it1 = listX.iterator(); it1.hasNext();) {
      Person p1 = (Person)it1.next();
      for (Iterator it2 = listY.iterator(); it2.hasNext();) {
         Person p2 = (Person)it2.next();
         if (p2.yearMin > p1.yearMax) break;
         if (p2.yearMax < p1.yearMin) continue;
         if ((p1.sex != p2.sex) && (p1.sex != UNKNOWN) && (p2.sex != UNKNOWN)) continue;
         key = p1.id+"x"+p2.id;
         match = (ConfidenceMatch)confList.get(key);
         // if assessment not already there or not confirmed, perform assessment
         if ((match == null) || ((!match.confirmed) && (!match.toBeMerged))) {
            match = assessConfidenceIndi(p1, p2, setting_chkdup, idNewOld);
            if (match.confLevel >= report.setting_askThreshold) 
               confList.put(key, match); 
            }
         }
      progress.increment(inc);
      if (!progress.isActive()) return false;
      } 
   return true;   
   }
  
  private boolean runAlgoDupInfo(List listX, List listY, Map confList, ProgressStatus progress, Map idNewOld) {
   String key = "";
   double inc = (double)listY.size();
   ConfidenceMatch match = null;

   log.write(0, 9, "", 0, report.translate("logEntExistDup")+".");
   for (Iterator it1 = listX.iterator(); it1.hasNext();) {
      Info p1 = (Info)it1.next();
      for (Iterator it2 = listY.iterator(); it2.hasNext();) {
         Info p2 = (Info)it2.next();
         if (p1.id.compareTo(p2.id) >= 0) continue;
         key = p1.id+"x"+p2.id;
         match = (ConfidenceMatch)confList.get(key);
         // if assessment not already there or not confirmed, perform assessment
         if ((match == null) || ((!match.confirmed) && (!match.toBeMerged))) {
            match = assessConfidenceInfo(p1, p2, setting_chkdup, confList, idNewOld);
            if (match.confLevel >= report.setting_askThreshold) 
               confList.put(key, match); 
            }
         }
      progress.increment(inc);
      if (!progress.isActive()) return false;
      } 
   return true;   
   }
  
  private boolean runAlgoMatchInfo(List listX, List listY, Map confList, ProgressStatus progress, Map idNewOld) {
   String key = "";
   double inc = (double)listY.size();
   ConfidenceMatch match = null;

   log.write(0, 9, "", 0, report.translate("logEntExistMatch")+".");
   for (Iterator it1 = listX.iterator(); it1.hasNext();) {
      Info p1 = (Info)it1.next();
      for (Iterator it2 = listY.iterator(); it2.hasNext();) {
         Info p2 = (Info)it2.next();
         key = p1.id+"x"+p2.id;
         match = (ConfidenceMatch)confList.get(key);
         // if assessment not already there or not confirmed, perform assessment
         if ((match == null) || ((!match.confirmed) && (!match.toBeMerged))) {
            match = assessConfidenceInfo(p1, p2, setting_chkdup, confList, idNewOld);
            if (match.confLevel >= report.setting_askThreshold) 
               confList.put(key, match); 
            }
         }
      progress.increment(inc);
      if (!progress.isActive()) return false;
      } 
   return true;   
   }
      
 /**
  * Get Trees from a gedcom
  */
  private HashSet getTrees(Gedcom gedcomX) {
   
   HashSet subtrees = new HashSet();
   HashSet entities = new HashSet(gedcomX.getEntities());
   while (!entities.isEmpty()) {
      Entity ent = (Entity)entities.iterator().next();
      entities.remove(ent);
      HashSet subtree = new HashSet();
      Stack todos  = new Stack();
      HashSet alreadyStacked = new HashSet();
      todos.add(ent);
      alreadyStacked.add(ent);
      while (!todos.isEmpty()) {
        Entity todo = (Entity)todos.pop();
        subtree.add(todo);
        List ps = todo.getProperties(PropertyXRef.class);
        for (Iterator it = ps.iterator(); it.hasNext();) {
           PropertyXRef xref = (PropertyXRef)it.next();
           Entity target = (Entity) xref.getTargetEntity(); 
           if ((target != null) && !alreadyStacked.contains(target)) {
              entities.remove(target);
              todos.push(target);
              alreadyStacked.add(target);
              }
           }
        }
      subtrees.add(subtree);
      }
    
   return subtrees;
   }
       
   
 /**
  * Display matches
  */
  private boolean displayMatches(Map matches, Map overlaps, Map scoreStats, boolean duplicates) {
   // 3 things to display:
   //   1/ Overall score 
   //   2/ 5 sets of subtrees of entities
   //   3/ All entities AxB combinations

   List confList = new ArrayList(matches.values());
   Collections.sort(confList, new ConfidenceMatch());
   confidenceListOutput = confList;

   // Results on number of matches
   log.write(2, 3, "", 0, report.translate("logDispResults")+":");
   if (confList.isEmpty()) {
      log.write(0, 6, "", 0, report.translate("logDispNoMatch", report.setting_askThreshold)+".");
      if (!duplicates) {
         log.write(0, 6, "", 0, report.translate("logDispAdding")+".");
         }
      else {
         log.write(0, 6, "", 0, report.translate("logDispNoDup")+".");
         }
      }
   else {
      log.write(0, 6, "", 0, confList.size()+" "+report.translate("logDispMatchesFnd", report.setting_askThreshold)+".");
      if (!duplicates) {
         log.write(0, 6, "", 0, report.translate("logDispCommon")+".");
         }
      }
   log.write(" ");

   // Summary of matches
   if (!confList.isEmpty()) {
      log.write(2, 3, "", 0, report.translate("logDispSumRes")+":");
      for (Iterator it = scoreStats.keySet().iterator(); it.hasNext(); ) {
        String key = (String)it.next();
        String value = (String)scoreStats.get(key);
        log.write(0, 6, "", 0, key+" : \t"+value);
        }
      log.write(" ");
      }

   // Structures of files
      log.write(2, 3, "", 0, report.translate("logDispStruct")+":");
      log.write(" ");
      log.write(0, 6, "", 0, report.translate("logDispEntSet")+":\tTotal\tIndi\tFam\tNote\tSour\tRepo\tSubm");
      log.write(0, 6, "", 0, "            \t----------------------------------------------------");
      log.write(0, 6, "", 0, "* "+"A "+report.translate("logDispFile")+" *");
      int[] entitiesVol = new int[6];
      int[] totalVol = new int[6];
      int total = 0;
      for (Iterator it = overlaps.keySet().iterator(); it.hasNext(); ) {
        String key = (String)it.next();
        List lEnts = (List)overlaps.get(key);
        calcEntities(lEnts, entitiesVol);
        total += lEnts.size();
        String volumes = "";
        for (int i=0; i<entitiesVol.length;i++) {
           volumes += entitiesVol[i]+"\t";
           totalVol[i] += entitiesVol[i];
           }
        log.write(0, 6, "", 0, report.translate(key)+" :\t"+lEnts.size()+"\t"+volumes);
        //if (debug) {
        //   for (Iterator it2 = lEnts.iterator(); it2.hasNext();) {
        //      Entity ent = (Entity)it2.next();
        //      log.write(0, 6, "", 0, "   entity: "+ent.getId());
        //      }
        //   }
        // print totals and reinit them
        if ((key == "A3mat") || (key == "B3ecl") || (key == "ZfB")) {
           volumes = "";
           for (int i=0; i<totalVol.length;i++) {
              volumes += totalVol[i]+"\t";
              }
           log.write(0, 6, "", 0, "            \t----------------------------------------------------");
           log.write(0, 6, "", 0, report.translate("logDispTotal")+" :     \t"+total+"\t"+volumes);
           log.write(" ");
           if (key == "A3mat") {
              log.write(0, 6, "", 0, "* "+"B "+report.translate("logDispFile")+" *");
              }
           if (key == "B3ecl") {
              log.write(" ");
              log.write(0, 6, "", 0, "* "+"C "+report.translate("logDispFile")+" *");
              }
           for (int i=0; i<totalVol.length;i++) totalVol[i] = 0;
           total = 0;
           }
        }
      log.write(" ");

   // Display list of matches by descending confidence levels (only the first 1000)
   if (!confList.isEmpty()) {
      log.write(2, 3, "", 0, report.translate("logDispConfLvl")+":");
      boolean noMatch = true;
      String msg = "";
      int max = 1000, i = 0;
      for (Iterator it = confList.iterator(); (it.hasNext()) && (i < max); i++) {
        ConfidenceMatch match = (ConfidenceMatch)it.next();
        if (duplicates) {
           msg = report.translate("logDispConfItemDup", new String[] { ""+match.confLevel, match.ent1.getId(), match.ent2.getId() } ) + " : ";
           }
        else {
           msg = report.translate("logDispConfItem", new String[] { ""+match.confLevel, match.ent1.getId(), match.id2, match.ent2.getId() } ) + " : ";
           }
        if (match.confirmed) {
           if (match.toBeMerged) 
              msg += report.translate("logDispWbm") + ", "+ 
		(match.choice == 1 ? report.translate("logDispKpFirst") : report.translate("logDispKpSecnd"));
           else 
              msg += report.translate("logDispWnbm");
           }
        else {
           msg += report.translate("logDispPoss")+".";
           }
        log.write(0, 6, "", 0, msg);
        }
      if (i==max) log.write(0, 6, "", 0, report.translate("logDispOther")+".");
      log.write(" ");
      }

   return !confList.isEmpty();
   }

 /**
  * Calculate number of entity by type in a list
  */
  private boolean calcEntities(List entities, int[] volume) {

   for (int i=0; i<volume.length;i++) volume[i] = 0;
   for (Iterator it = entities.iterator(); it.hasNext(); ) {
     Entity ent = (Entity)it.next();
     for (int i=0; i<volume.length;i++) {
        if (ent.getTag() == typeEnt[i]) { volume[i]++; break; }
        }
     }    
   return true;
   } 
 
 /**
  * USER INTERFACE - Confirm matches with User
  * Return value:
  *  -1 => user cancels
  *   0 => return OK and nothing to do (assess 0 times after that)
  *   1 => return OK and finished with assessing (assess 1 time after that)
  *   2 => return OK and not finished with assessing (keep assessing)
  */
  private int confirmMatchesWithUser(Map matches) {
   // For now, very poor UI, will have to be much more interactive later
   // Display pairs by decreasing conf level for those with highest 
   int
     CHOICE_NO = 0,
     CHOICE_1 = 1,
     CHOICE_2 = 2,
     CHOICE_3 = 3,
     CHOICE_SKIP = 4,
     CHOICE_REDO = 5;
   int choice = CHOICE_NO;
   String choices[] = {
     report.translate("user_no"),
     report.translate("user_yes_first"),
     report.translate("user_yes_second"),
     report.translate("user_yes_both"),
     report.translate("user_skip"),
     report.translate("user_reassess")
   };

   String msg = report.translate("user_question_merge");
   String confText = "";
   EntityView entView = new EntityView(report.translate("user_title_merge"), msg, false);

   List confList = new ArrayList(matches.values());
   Collections.sort(confList, new ConfidenceMatch());
   boolean nothingToDo = true;
   for (Iterator it = confList.iterator(); it.hasNext(); ) {
      ConfidenceMatch match = (ConfidenceMatch)it.next();
      if ((!match.confirmed) && (!match.toBeMerged)) {
         confText = report.translate("logDispConfItem", new String[] { ""+match.confLevel, match.ent1.getId(), match.id2, match.ent2.getId() } );
         entView.setQuestion(msg+match.confLevel+"%)");
         choice = entView.getEntityFromUser(match.ent1, match.ent2, choices, choices[0]);
         if (choice == -1) {
            return -1;
            }
         if (choice == CHOICE_REDO) {
            return 2;
            }
         if (choice == CHOICE_SKIP) {
            log.write(0, 6, "", 0, confText+" - "+report.translate("logConfSkipped")+".");
            continue;
            }
         if (choice == CHOICE_NO) {
            match.toBeMerged = false;
            log.write(0, 6, "", 0, confText+" - "+report.translate("logConfWnbm")+".");
            }
         if (choice == CHOICE_3) {
            match.toBeMerged = mergeEntity(match, true);
            if (!match.toBeMerged) { 
               log.write(0, 6, "", 0, confText+" - "+report.translate("logConfAwnbm")+".");
               continue;
               }
            else {
               log.write(0, 6, "", 0, confText+" - "+report.translate("logConfAwbm")+".");
               }
            }
         if ((choice == CHOICE_1) || (choice == CHOICE_2)) {
            match.toBeMerged = true;
            match.choice = choice;
            log.write(0, 6, "", 0, confText+" - "+report.translate("logConfWbm")+".");
            }
         match.confirmed = true;
         matches.put(match.ent1.getId()+"x"+match.ent2.getId(), match);
         nothingToDo = false;
         }
      }
   if (nothingToDo) {
      log.write(0, 6, "", 0, report.translate("logConfNoConf")); 
      return 0;
      }

   if (report.getOptionFromUser(report.translate("user_ask_completed"), OPTION_YESNO))
      return 1;
   return 2;
   }

 /**
  * USER INTERFACE - Confirm property to keep with User
  */
  private int confirmPropertyWithUser(Property propA, Property propB, String str1, String str2) {
   // For now, very poor UI, much more interactive later
   int
     CHOICE_1 = 0,
     CHOICE_2 = 1,
     CHOICE_3 = 2;
   int choice = CHOICE_1;
   String choices[] = {
     report.translate("user_yes_firstProp"),
     report.translate("user_yes_secondProp"),
     report.translate("user_yes_bothProp")
   };

   String msg = report.translate("user_question_prop");
   EntityView entView = new EntityView(report.translate("user_title_prop"), msg, true);
   choice = entView.getEntityFromUser(propA, propB, str1, str2, choices, choices[0]);
   if (choice == CHOICE_1) {
      return 1;
      }
   if (choice == CHOICE_2) {
      return 2;
      }
   if (choice == CHOICE_3) {
      return 3;
      }

   return 0;
   }

 /**
  * Assess which families need to be merged
  * 
  * Need to merge families if husband and wife are identical and the date is the same, once individuals have in principle been merged
  *   It can be that only one was merged in case of duplicate search, or both in case of different files.
  *
  *   If only husband or wife exist in the marriage, merge family as well if only one spouse individual is merged.
  */
  private boolean assessFamilies(Gedcom gedcomX, Gedcom gedcomY, boolean duplicates, Map matches, Map idNewOld) {

   // Store individuals that are to be merged
   HashSet entsX = new HashSet();
   HashSet entsY = new HashSet();
   Map xToY = new TreeMap();
   Map xToConf = new TreeMap();
   List confList = new ArrayList(matches.values());
   boolean skip = true;
   for (Iterator it = confList.iterator(); it.hasNext(); ) {
     ConfidenceMatch match = (ConfidenceMatch)it.next();
     if ((match.toBeMerged) && (match.ent1 instanceof Indi)) {    
        entsX.add(match.ent1);
        entsY.add(match.ent2);
        xToY.put(match.ent1, match.ent2);
        xToConf.put(match.ent1, (Integer)match.confLevel);
        skip = false;
        }
     }
   if (skip) return true;

   // Scan families from X and check whether husband and wife to be merged
   List listFamsX = new ArrayList(gedcomX.getEntities(typeEnt[1]));
   List listFamsY = new ArrayList(gedcomY.getEntities(typeEnt[1]));
   for (Iterator itx = listFamsX.iterator(); itx.hasNext(); ) {
     Fam famX = (Fam)itx.next();
     if (famX == null) break;
     Entity husbandX = (Entity)famX.getHusband();
     Entity wifeX = (Entity)famX.getWife();
     Entity matchHusbandX = husbandX;
     Entity matchWifeX = wifeX;
     int conflevel = 0;
     skip = true;

     if (husbandX != null) {
        if (entsX.contains(husbandX)) {
           matchHusbandX = (Entity)xToY.get(husbandX);
           conflevel = (int)(Integer)xToConf.get(husbandX);
           skip = false;
           }
        }
     if (wifeX != null) {
        if (entsX.contains(wifeX)) { 
           matchWifeX = (Entity)xToY.get(wifeX);
           conflevel = Math.max(conflevel, (int)(Integer)xToConf.get(wifeX));
           skip = false;
           }
        }
     if (skip) {
        continue;
        }

     // If we get here, famx has either a husband or a wife or both that are to be merged
     // Check in the *other* famililes if we get the same husband and wife
     for (Iterator ity = listFamsY.iterator(); ity.hasNext(); ) {
        Fam famY = (Fam)ity.next();
        if (famY == null) break;
        if (famX.getId().equals(famY.getId())) continue;   // do not match same families
        Entity husbandY = (Entity)famY.getHusband();
        Entity wifeY = (Entity)famY.getWife();
        if (((husbandY == matchHusbandX) && (wifeY == matchWifeX)) || ((husbandY == matchWifeX) && (wifeY == matchHusbandX))) {
           // match found!
           ConfidenceMatch match = new ConfidenceMatch((Entity)famX, (Entity)famY);
           if (idNewOld != null) match.id2 = (String)idNewOld.get((String)(match.ent2.getId()));
           if (match.id2 == null && duplicates) match.id2 = (String)(match.ent2.getId());
           match.confLevel = conflevel-1;
           match.confirmed = true;
           match.toBeMerged = true;
           match.choice = 3;
           matches.put(famX.getId()+"x"+famY.getId(),match);
           break;
           }
        }
     }

   return true;
   }


 /**
  * Merge entities to be merged (information to keep, for automatically merged ones)
  */
  private boolean mergeEntities(Map matches) {

   // Sort by descending confidence levels
   List confList = new ArrayList(matches.values());
   Collections.sort(confList, new ConfidenceMatch());
   for (Iterator it = confList.iterator(); it.hasNext(); ) {
     ConfidenceMatch match = (ConfidenceMatch)it.next();
     if (match.toBeMerged && (match.choice == 3)) {
        if (!mergeEntity(match, true)) return false;
        }
     }

   return true;
   }


 /**
  * Merge entities' properties
  */
    //
    // Principles:
    // ----------
    // 1-Use copies of B and each time a property has been considered either side, remove it from the tmpB entity to avoid double counting
    // 2-No tmpA used because need to clean it which removes the precious links to families
    // 3-Some properties in an entity are unique (usually NAME, BIRTH, etc), some are multiple (RESI, GRAD, etc)
    //   This characteristic only depends on each pair of entities considered each time:
    //    - UNIQUE if found once on BOTH sides, only on one side or MULTIPLE otherwise
    //    - an entity can have several NAME if user wanted to, or several BIRTH
    //    - no rule is made than NAME should be UNIQUE, we don't care, user is the one who decided since GEDCOM allows it
    //    - we do not want to impose a grammar here
    // 4-Only properties of first level are manipulated, by "cluster". So we manipulate clusters of first level, not properties
    //    - we do not fine tune matching inside clusters, even for UNIQUE properties
    //    - two clusters are identical if everything inside is the same, there are different otherwise (have to build a algo there)
    // 5-Merging rule is: UNIQUE clusters are "merged", MULTIPLE ones are simply added, users will fine tune content themselves
    //
    // Algorithm:
    // ----------
    // 1-Copy entities to temporary ones, tmpA and tmpB
    // 2-Consider each property on tmpA (actually each cluster), identified by its tagpath
    //   If it is UNIQUE (i.e. found once on both sides):
    //    - if cluster tmpA = cluster tmpB, pass and remove it from tmpB
    //    - else ask user: 
    //       - if take tmpA, pass and remove it from tmpB
    //       - if take tmpB, replace cluster tmpA by cluster tmpB and remove it from tmpB
    //       - if add BOTH, add cluster tmpB to tmpA and remove it from tmpB
    //       - *not covered*: if "merge both" : not proposed to the user, that is the recurring part we could do later (nice to have)
    //   If it is NOT UNIQUE:
    //    - pass (we will need to add those of B to A)
    //   Continue for all cluster in tmp A
    // 3-Scan all left clusters in tmpB and copy/add them to tmpA
    //   (if they are UNIQUE, they do not exist in tmpA so should be copied)
    //   (if they are NOT UNIQUE, they have been either passed (should be added to A) or do not exist in A (so copy them)
    // 4-Delete temporary entity
    // 5-Conclude
    //
  private boolean mergeEntity(ConfidenceMatch match, boolean askUser) {

    if (!askUser && (report.setting_ruleEntity == ALWAYS_A)) {
       match.confirmed = true;
       match.toBeMerged = true;
       match.choice = 1;
       return true;
       }
    if (!askUser && (report.setting_ruleEntity == ALWAYS_B)) {
       match.confirmed = true;
       match.toBeMerged = true;
       match.choice = 2;
       return true;
       }

    // 1-Create copies of A and B (
    String title1 = match.ent1.getTag()+" : "+match.ent1.toString();
    String title2 = match.ent2.getTag()+" : "+match.ent2.toString();
    Entity tmpEntB = null;
    try {
       tmpEntB = match.ent2.getGedcom().createEntity(match.ent2.getTag());
       copyCluster(match.ent2, tmpEntB);
       } catch (GedcomException e) {
       log.write(9, 0, "=", LNS, "GedcomException:"+e);
       return false;
       }

    // 2-Scan properties of level 1 in A
    //    - store tagpaths of level 1 of A and store those of B
    List<TagPath> clustersA = new ArrayList();
    Property[] propertiesA = match.ent1.getProperties();
    for (int i = 0; i < propertiesA.length; i++) {
       clustersA.add(propertiesA[i].getPath());
       }
    List<TagPath> clustersB = new ArrayList();
    Property[] propertiesB = tmpEntB.getProperties();
    for (int i = 0; i < propertiesB.length; i++) {
       clustersB.add(propertiesB[i].getPath());
       }
    //    - scan each property of A
    Property clusterPropA = null;
    Property clusterPropB = null;
    TagPath pathA = null;
    TagPath pathB = null;
    boolean unique = false;
    for (int i = 0; i < propertiesA.length; i++) {
       clusterPropA = propertiesA[i];
       pathA = clusterPropA.getPath();
       unique = ( (clustersA.indexOf(pathA) == clustersA.lastIndexOf(pathA)) && 
                 ((clustersB.indexOf(pathA) != -1) && (clustersB.indexOf(pathA) == clustersB.lastIndexOf(pathA))));

       if (unique) {
          clusterPropB = tmpEntB.getPropertyByPath(pathA.toString());

          // If same clusters, remove the one in B and continue
          String strA = printCluster(clusterPropA);
          String strB = printCluster(clusterPropB);
          if (strA.equals(strB)) {
             tmpEntB.delProperty(clusterPropB); 
             continue;
             }

          // Determine choice of which cluster to keep based on rules
          int choice = 0;
          if (!askUser && (report.setting_ruleEntity == A_CONFLICT)) {
             choice = 1;
             }
          if (!askUser && (report.setting_ruleEntity == B_CONFLICT)) {
             choice = 2;
             }
          if (askUser || (report.setting_ruleEntity == ASK_CONFLICT)) {
             choice = confirmPropertyWithUser(clusterPropA, clusterPropB, title1, title2);
             if (choice == 0) return false;
             }

          // Apply choice
          if (choice == 2) { // keep B
             match.ent1.delProperty(clusterPropA);  
             copyCluster(clusterPropB, match.ent1.addProperty(clusterPropB.getTag(), clusterPropB.getValue()));  
             }
          if (choice == 3) { // add A and B
             copyCluster(clusterPropB, match.ent1.addProperty(clusterPropB.getTag(), clusterPropB.getValue()));  
             }
          tmpEntB.delProperty(clusterPropB); 
          }
       }
   
    // 3-Scan all left clusters in tmpB and copy/add them to tmpA
    propertiesB = tmpEntB.getProperties(); // re-load properties !
    for (int i = 0; i < propertiesB.length; i++) {
       clusterPropB = propertiesB[i];
       copyCluster(clusterPropB, match.ent1.addProperty(clusterPropB.getTag(), clusterPropB.getValue()));  
       }

    // 4-Delete temporary entity
    match.ent2.getGedcom().deleteEntity(tmpEntB);

    // 5-Conclude - Indicate that final entity will be the entity from file A
    match.choice = 1;

    return true;
    }


  /**
   * Add a property at given path
   */
  private Property addPropertyByPath(Entity ent, final TagPath path, final String value) {
    final Property[] result = new Property[1];
    final int[] level = { 1 };

    PropertyVisitor visitor = new PropertyVisitor() {
      protected boolean leaf(Property prop) {
        level[0] = 1;
        return false;
      }
      protected boolean recursion(Property parent,String child) {
        level[0]++;
        if ((parent.getProperty(child, false)==null) && (level[0] < path.length())) {
           parent.addProperty(child, "");
           }
        if (level[0] == path.length()) {
           result[0] = parent.addProperty(child, value);
           }
        return true;
      }
    };

    path.iterate((Property)ent, visitor);

    // done
    return result[0];
  }

 /**
  * Merge gedcom and macthes entities in particular
  */
  private boolean flagEntity(Entity ent, String flag, String fileName) {
   // Add _MRG_UPD tag for a merged entity with date and filename of origin
   // Add _MRG_NEW tag for a new entity added 

   if (!report.setting_flagChanges) return false;
   Property prop = (Property)ent;
   Date rightNow = Calendar.getInstance().getTime();
   SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd-HH:mm-E");
   String date = formatter.format(rightNow);
   prop = prop.addProperty("_MRG-"+flag, "");   
   prop.addProperty("DATE", date);   
   prop.addProperty("FILE", fileName);   
   return true;  
   } 
 
 /**
  * Copy properties beneath a property to another property (copy a cluster)
  */
  private boolean copyCluster(Property propA, Property propB) {

   if (propA == null || propB == null) return false;

   List listProp = new LinkedList();
   Property[] propertiesA = propA.getProperties();
   listProp.addAll(Arrays.asList(propertiesA));
   Property propItemA = null;
   Property propItemB = (Property) propB;
   Property lastpropItemB = null;
   TagPath tagPathA = propA.getPath();
   int len = tagPathA.length() + 1;

   while (listProp.size() > 0) {
      // manages list
      propItemA = (Property) ((LinkedList)listProp).removeFirst();
      Property[] subProps = propItemA.getProperties();
      listProp.addAll(0, Arrays.asList(subProps));
      // workout if we have changed level or tag
      tagPathA = propItemA.getPath();
      if (tagPathA.length() > len) {
         // we have moved down one level, move B to last property added
         propItemB = lastpropItemB;
         len = tagPathA.length();
         }
      while (tagPathA.length() < len) {
         // Otherwise we have moved up, move B to corresponding parent
         propItemB = propItemB.getParent();
         len--;
         }
      // copy the property
      String tag = propItemA.getTag();
      if (tag == "XREF") continue;
      // Special treatment of NOTE entities which have text in entity tag rather
      // than as a subtag in Gedcom file although GenJ stores this as a subtag
      if (tagPathA.toString().compareTo("NOTE:NOTE") == 0) {
         propB.setValue(propItemA.getValue());
         }
      else { 
         lastpropItemB = propItemB.addProperty(tag, propItemA.getValue());
         }
      }

   return true;
   }


 /**
  * USER INTERFACE - Get user to map place Format in case they are different
  */
  private int[] mapPlaceFormat(Gedcom gedcomX, Gedcom gedcomY, int headerChosen) {
   // "1" will be the header converted
   String pf1 = "";
   String pf2 = "";
   if (headerChosen == 1) { 
     pf1 = gedcomX.getPlaceFormat();
     pf2 = gedcomY.getPlaceFormat();
     }
   else {
     pf2 = gedcomX.getPlaceFormat();
     pf1 = gedcomY.getPlaceFormat();
     }

   // If same formats, return (default will be as per option)
   if (pf1.compareTo(pf2) == 0) return null;

   // If one file does not have a format, return (default will be as per option)
   if ((pf1.length() == 0) || (pf2.length() == 0)) return null;

   // Both format exist and are not null 
   String[] tags1 = pf1.split("\\,");
   int[] placeMap = new int[tags1.length];
   ArrayList tags2 = new ArrayList((Collection)Arrays.asList(pf2.split("\\,"))); 
   ArrayList tagsTemp = new ArrayList((Collection)Arrays.asList(pf2.split("\\,"))); 
   for (int i = 0; i < tags1.length; i++) {
      String tag = (String)tags1[i];
      String msg = report.translate("user_place_map_tag", tag);
      String selection = (String)report.getValueFromUser(msg, (Object[])tags2.toArray(), tags2.get(0));
      int iSel = 0;
      if (selection == null) selection = (String)tags2.get(0);
      iSel = tags2.indexOf(selection); 
      placeMap[i] = tagsTemp.indexOf(selection);
      log.write(0, 9, "", 0, tags1[i]+" -> "+tags2.get(iSel));
      if (tags2.size() > 1) tags2.remove(iSel); 
      }

   return placeMap;
   }

 /**
  * Remap a list of jurisdictions 
  */
  private boolean remapPlaces(List entities, int[] placeMap) {

    if (placeMap == null) return true;

    // Loop on all entities to get their place tag and remap it 
    for (Iterator it = entities.iterator(); it.hasNext();) {
       Entity ent = (Entity)it.next();
       List places = ent.getProperties(PropertyPlace.class);
       for (Iterator itp = places.iterator(); itp.hasNext();) {
          Property propPlace = (Property)itp.next();
          String place = propPlace.toString();
          //log.write("remap:"+place);
          String[] placeTab = place.split("\\,", -1);
          String newPlace = "";
          for (int i = 0; i < placeMap.length; i++) {
             if (placeMap[i] < placeTab.length) 
                newPlace += placeTab[placeMap[i]]+",";
             else {
                log.write(0, 9, "", 0, report.translate("logPlaceWng", new String[] { place, ""+placeMap.length, ""+placeTab.length } ) + ".");
                newPlace = place+",,,,,,,,,,,,,,,,".substring(0,placeMap.length-placeTab.length+1);
                break;
                }
             }
          newPlace = newPlace.substring(0,newPlace.length()-1); // remove last comma
          //log.write("remapped:"+newPlace);
          propPlace.setValue(newPlace);
          }
       }

   return true;
   }

 /**
  * Create gedcom file 
  */
  private Gedcom createGedcomFile(String fileNameX, boolean createSubmitter, Gedcom gedcomA) {
    // Create empty gedcom using header parameters from other gedcom provided
    Origin originX = null;
    File fileX = new File(fileNameX);
    if (fileX.exists()) {
       log.write(0, 6, "", 0, report.translate("logFileExist", fileNameX)+".");
       fileX.delete();
       log.write(0, 6, "", 0, report.translate("logFileDlt")+" "+fileNameX);
    }
    try {
       originX = Origin.create(new URL("file", "", fileX.getAbsolutePath()));
       } catch (MalformedURLException e) {
       log.write(9, 0, "=", LNS, "URLexception:"+e);
       return null;
       }
    Gedcom gedcomX = new Gedcom(originX);   
    gedcomX.setEncoding(gedcomA.getEncoding());
    gedcomX.setLanguage(gedcomA.getLanguage());
    gedcomX.setPassword(gedcomA.getPassword());
    gedcomX.setPlaceFormat(gedcomA.getPlaceFormat());

    // Submitter reference in header will be created separately as the actual merge
    // might get actual submitter from elsewhere
    if (createSubmitter) {
       try {
          Submitter sub = (Submitter) gedcomX.createEntity(Gedcom.SUBM, gedcomX.getNextAvailableID(Gedcom.SUBM));
          sub.addDefaultProperties();
          gedcomX.setSubmitter(sub);
          } catch (GedcomException e) {
          log.write(9, 0, "=", LNS, "GedcomException:"+e);
          return null;
          }
       }
    return gedcomX;
    }
    
 
 /**
  * Merge gedcom and macthes entities in particular
  */
  private boolean mergeGedcom(Gedcom gedcomX, Gedcom gedcomY, Gedcom gedcomZ, Map overlaps, Map idMap) {
    
    // Consolidate lists flaging the added ones (merged ones already flagged)
    String fileNameB = gedcomY.getOrigin().getFile().getName();
    List listEnts = (List)overlaps.get("ZfB");
    for (Iterator it = listEnts.iterator(); it.hasNext();) {
       Entity ent = (Entity)it.next();
       flagEntity(ent, "NEW", fileNameB);   
       } // end loop
    
    // Copy entities
    List entities = new ArrayList();
    entities.addAll((List)overlaps.get("ZfB"));
    entities.addAll((List)overlaps.get("ZfA"));
    Collections.sort(entities, sortEntities);
    copyEntities(entities, gedcomZ);
    
    // Reconstruct the links (focus on connected entities only)
    rebuildLinks(gedcomZ, idMap);   

    // Update the links
    linkGedcom(gedcomZ);   
      
    return true;
    }

 /**
  * Copy gedcom to another gedcom assuming destination gedcom is empty
  */
  private boolean copyEntities(List entities, Gedcom gedcomZ) {
      
    // Copy entities from list to Gedcom
    try {
       for (Iterator it = entities.iterator(); it.hasNext();) {
          // copy one entity
          Entity entX = (Entity)it.next();
          Entity entZ = null;
          entZ = gedcomZ.createEntity(entX.getTag(), entX.getId());
          copyCluster(entX, entZ);
          } // end loop to copy other entities
       } catch (GedcomException e) {
       log.write(9, 0, "=", LNS, "GedcomException:"+e);
       return false;
       }
    
    return true;   
    }
          
 /**
  * Rebuild links for overlapping entities
  */
  private boolean rebuildLinks(Gedcom gedcom, Map idMap) {
      
    List entities = gedcom.getEntities();
    for (Iterator it = entities.iterator(); it.hasNext();) {
       Entity ent = (Entity)it.next();
       // Get all references from this entity and store valid ones
       HashSet refs = new HashSet();
       String refi = "";
       List ps = ent.getProperties(PropertyXRef.class);
       for (Iterator itr = ps.iterator(); itr.hasNext();) {
          PropertyXRef xref = (PropertyXRef)itr.next();
          String targetId = (String) xref.getValue();
          targetId = targetId.substring(1,targetId.length()-1);
          refi = xref.getTag()+":"+targetId; // for instance, could be "FAMS:F1095"
          if (gedcom.getEntity(targetId) != null) {
             refs.add(refi);
             continue;
             }
          // Target entity not in Z: it has not been chosen; get new ref through idMap
          String newTargetId = (String)idMap.get(targetId);

          // When swaping the old reference to the new one, we need to make sure that 
          //    we do not end up with two identical reference
          //    (for instance, if a person has two families FAMS, if one changes to the other one, remove ref)
          // So if null or already there, remove ref
          if (newTargetId == null) {
             ent.delProperty(xref);
             }
          else {
             refi = xref.getTag()+":"+newTargetId;
             if (refs.contains(refi)) {
                ent.delProperty(xref);
                }
             else {
                xref.setValue("@"+newTargetId+"@");
                refs.add(refi);
                }
             }
          }
       }
    
    return true;   
    }
          
    
 /**
  * Save Gedcom file
  */
  private boolean saveGedcom(Gedcom gedcomX) {
    File fileX = gedcomX.getOrigin().getFile();
    GedcomWriter writerX = null;
    try {
       writerX = new GedcomWriter(gedcomX, fileX.getName(), gedcomX.getEncoding(), new FileOutputStream(fileX));
       } catch (IOException e) {
       log.write(9, 0, "=", LNS, "IOexception:"+e);
       return false;
       }
    try {
       writerX.write();
      } catch (GedcomIOException e) {
       log.write(9, 0, "=", LNS, report.translate("logErrorSaving"));
       log.write(0, 6, "", 0, ""+e);
       log.write(0, 6, "", 0, report.translate("logErrorLine")+":"+e.getLine());
       return false;
      }
    log.write(0, 6, "", 0, report.translate("logErrorLineWtn")+": "+writerX.getLines());

    return true;
    }

 /**
  * Comparator to sort entities
  */
  private Comparator sortEntities = new Comparator() {
     public int compare(Object o1, Object o2) {
        Entity ent1 = (Entity)o1;
        Entity ent2 = (Entity)o2;
        String id1 = ent1.getId();
        String id2 = ent2.getId();
        String tag1 = ent1.getTag();
        String tag2 = ent2.getTag();
        String s1 = "", s2 = "";
        int    n1 = 0,  n2 = 0;
        // tag1
        if (tag1 == Gedcom.INDI) s1 = "A"; 
        else if (tag1 == Gedcom.FAM)  s1 = "B"; 
        else if (tag1 == Gedcom.NOTE) s1 = "C"; 
        else if (tag1 == Gedcom.SOUR) s1 = "D"; 
        else if (tag1 == Gedcom.REPO) s1 = "E"; 
        else if (tag1 == Gedcom.SUBM) s1 = "F"; 
        // tag2
        if (tag2 == Gedcom.INDI) s2 = "A"; 
        else if (tag2 == Gedcom.FAM)  s2 = "B"; 
        else if (tag2 == Gedcom.NOTE) s2 = "C"; 
        else if (tag2 == Gedcom.SOUR) s2 = "D"; 
        else if (tag2 == Gedcom.REPO) s2 = "E"; 
        else if (tag2 == Gedcom.SUBM) s2 = "F"; 

        if (s1.compareTo(s2) != 0) return s1.compareTo(s2);

        int start = 0,
            end   = id1.length()-1;
        while (start<=end&&!Character.isDigit(id1.charAt(start))) start++;
        while (end>=start&&!Character.isDigit(id1.charAt(end))) end--;
        if (end<start) n1 = 0;
        else n1 = (int)Integer.parseInt(id1.substring(start, end+1));

        start = 0;
        end   = id2.length()-1;
        while (start<=end&&!Character.isDigit(id2.charAt(start))) start++;
        while (end>=start&&!Character.isDigit(id2.charAt(end))) end--;
        if (end<start) n2 = 0;
        else n2 = (int)Integer.parseInt(id2.substring(start, end+1));

        return (n1 - n2);
        }
     };

 /**
  * Calculates confidence level of matching between 2 individuals
  *
  * Principles:
  * 1/ Find match using only basic information (lastname, firstname, birthdate, birthcity)
  * 2/ Fill in gaps if this information is incomplete, using other bits of information (if setting_default is set)
  * 3/ Allow for a match even when information are only similar and not perfectly identical (if setting_approximate is set)
  * 4/ Beyond a certain difference, consider that entities are definitely not the same (if setting_differencemeansno is set)
  *
  */
  private ConfidenceMatch assessConfidenceIndi(Person p1, Person p2, boolean duplicates, Map idNewOld) {

   // Prepare match result
   ConfidenceMatch match = new ConfidenceMatch((Entity) p1.indi, (Entity) p2.indi);

   // Take care of the duplicates case
   if (idNewOld != null) match.id2 = (String)idNewOld.get((String)(match.ent2.getId()));
   if (match.id2 == null && duplicates) match.id2 = (String)(match.ent2.getId());

   // Assess individuals score based on basic information 
//if (debug) log.write("     --- assess p1 p2= "+p1.id+" "+p2.id);
   double score = assessIndi(p1, p2);
   if (score == 0) {
      match.confLevel = 0;
      return match;
      }

   // If one of the persons if missing key basic information, compensate by comparing relatives instead
   if (score >= 1 && score <= 100000000 && report.setting_default) {
//log.write("     --- assessRelatives p1 p2= "+p1.id+" "+p2.id);
      score *= assessRelatives(p1, p2);
      }

   // Final calculations
   match.confLevel = getMatchProba(score);
//if (debug) log.write("     --- SCORE= "+match.confLevel);

   // Manage automerge 
   if (match.confLevel > report.setting_autoMergingLevel) {
      match.confirmed = true;
      match.toBeMerged = true;
      match.choice = 3; // information kept is always in A, even if copied from B.
                        // 3 means "1" but merging of properties still to be done.
      }

   return match;
   }
 

 /**
  * Assess confidence level between 2 individuals based on lastname, firstname, and birth (or death or marriage)
  * 
  * Score is either 0=different, 1=could not compare (missing value), then any double between 2/3x and x; x is for instance 20,000 for lastnames, 7,000 for firstnames, 36,000 for places
  *
  */
  private double assessIndi(Person p1, Person p2) {

   double score = 0;

   if (p1 == null || p2 == null) return 0;

   // Get basic scores
   double scoreLastName = getMatchScore(1, p1, p2); 		// compares lastnames of persons
   if (scoreLastName == 0) {
      return 0;
      }
   double scoreFirstName = getMatchScore(2, p1, p2); 		// compares firstnames of persons 
   if (scoreFirstName == 0) {
      return 0;
      }

   double scoreBirth = getMatchScore(3, p1, p2); 		// Compares birth date of persons
   scoreBirth *= getMatchScore(4, p1, p2);        		// Place of Person's birth
   if (scoreBirth == 0 && !report.setting_approximate) {	// If no approximation is allowed, different values is not a match
      return 0;
      }

   double scoreDeath = getMatchScore(5, p1, p2); 		// Compares death date of persons
   scoreDeath *= getMatchScore(6, p1, p2); 			// Place of Person's death
   if (scoreDeath == 0 && !report.setting_approximate) {	// If no approximation is allowed, different values is not a match
      return 0;
      }

   double scoreMarr = getMatchScore(7, p1, p2);		 	// Compares marriage date of persons
   scoreMarr *= getMatchScore(8, p1, p2); 			// Place of Person's marriage
   if (scoreMarr == 0 && !report.setting_approximate) {		// If no approximation is allowed, different values is not a match
      return 0;
      }

   double scoreBurial = getMatchScore(9, p1, p2); 		// Compares burial date of persons
   scoreBurial *= getMatchScore(10, p1, p2); 			// Place of Person's burial
   if (scoreBurial == 0 && !report.setting_approximate) {	// If no approximation is allowed, different values is not a match
      return 0;
      }

   double scoreEvent = Math.max(scoreBirth, Math.max(scoreDeath, Math.max(scoreBurial, scoreMarr)));
   if (scoreEvent == 0) {
      return 0;
      }

   return scoreLastName * scoreFirstName * scoreEvent;
   }


 /**
  * Assess confidence level between 2 individuals based on relatives only 
  */
  private double assessRelatives(Person p1, Person p2) {

   // Compare Relatives
   int value = 20000;
   double score = 0;

   score  = getMatchProba(assessIndi(p1.father, p2.father)) * 0.05;
   score += getMatchProba(assessIndi(p1.mother, p2.mother)) * 0.05;
   score += getMatchProba(assessIndiTab(p1.partners, p2.partners)) * 0.25;
   score += getMatchProba(assessIndiTab(p1.kids, p2.kids)) * 0.25;
   score += getMatchProba(assessIndiTab(p1.siblings, p2.siblings)) * 0.05;
//if (debug) log.write("     --- assessRelatives - score = "+score);
   return score * value / 100;
   }



 /**
  * Assess confidence level between 2 arrays of individuals
  */
  private int assessIndiTab(HashSet persons1, HashSet persons2) {

   if ((persons1 == null) || (persons2 == null)) return 0;

   // The idea is to assess both sets for the best matching pairs and return this score as the matching score.
   int scoreMax = 0;
   int scoreTmp = 0;
   for (Iterator it1 = persons1.iterator(); it1.hasNext();) {
      Person p1 = (Person)it1.next();
      for (Iterator it2 = persons2.iterator(); it2.hasNext();) {
         Person p2 = (Person)it2.next();
         scoreTmp = getMatchProba(assessIndi(p1, p2));
         if (scoreTmp > scoreMax) {
            scoreMax = scoreTmp;
            }
         }
      }
   return scoreMax;
   } 

 
 /**
  * Get a matching score between one element of 2 persons
  *
  *  0          : not a match
  *  1          : cannot say, one element is missing
  *  2/ x value : a partial match
  *  value      : a match
  */
  private int getMatchScore(int type, Person p1, Person p2) {

   int score = 0; 
   int value = 0;
   int threshold = 70;

   // Compare LastName
   if (type == 1) {
      value = 20000;
      threshold = 80;
      //if (debug) log.write("           --- cmpLN= "+p1.deflnLength+" "+p2.deflnLength+" "+p1.defLastName+" "+p2.defLastName);
      if (p1.deflnLength == 0 || p2.deflnLength == 0) {
         return 1;
         }
      if (p1.defLastName.equals(p2.defLastName)) {
         return value;
         }
      if (report.setting_phonex && p1.lastPhonex.equals(p2.lastPhonex)) {
         return value;
         }
      if (!report.setting_approximate) {
         return 0;
         }
      score = matchCode(p1.deflnCode, p1.deflnLength, p2.deflnCode, p2.deflnLength);
      //if (debug) log.write("           --- score= "+score);
      if (report.setting_differencemeansno && (score < threshold)) {
         return 0;
         }
      return 10;
      }

   // Compare FirstName
   if (type == 2) {
      value = 7000;
      threshold = 50;
      //if (debug) log.write("           --- cmpFN= "+p1.firstNameLength+" "+p2.firstNameLength+" "+p1.firstName+" "+p2.firstName);
      if (p1.firstNameLength == 0 || p2.firstNameLength == 0) {
         return 1;
         }
      if (p1.firstName.equals(p2.firstName)) {
         return value;
         }
      if (report.setting_phonex && p1.firstPhonex.equals(p2.firstPhonex)) {
         return value;
         }
      if (!report.setting_approximate) {
         return 0;
         }
      score = matchCode(p1.firstNameCode, p1.firstNameLength, p2.firstNameCode, p2.firstNameLength);
      //if (debug) log.write("           --- score= "+score);
      if (report.setting_differencemeansno && (score < threshold)) {
         return 0;
         }
      return 10;
      }

   // Compare BirthDate
   if (type == 3) {
      value = 36500;
      threshold = 10;
      //if (debug) log.write("           --- cmpBD= "+p1.bS+" "+p2.bS+" "+p1.bE+" "+p2.bE);
      if (p1.bS == 0 || p2.bS == 0) {
         return 1;
         }
      if (p1.bS == p2.bS && p1.bE == p2.bE) {
         return value;
         }
      if (!report.setting_approximate) {
         return 0;
         }
      score = matchJD(p1.bS, p1.bE, p2.bS, p2.bE);
      //if (debug) log.write("           --- score= "+score);
      if (report.setting_differencemeansno && (score < threshold)) {
         return 0;
         }
      return 10;
      }

   // Compare BirthCityCtry
   if (type == 4) {
      value = 36000;
      threshold = 80;
      //if (debug) log.write("           --- cmpBP= "+p1.birthLength+" "+p2.birthLength+" "+p1.birth+" "+p2.birth);
      if (p1.birthLength == 0 || p2.birthLength == 0) {
         return 1;
         }
      if (p1.birth.equals(p2.birth)) {
         return value;
         }
      if (!report.setting_approximate) {
         return 0;
         }
      score = matchCode(p1.birthCode, p1.birthLength, p2.birthCode, p2.birthLength);
      //if (debug) log.write("           --- score= "+score);
      if (report.setting_differencemeansno && (score < threshold)) {
         return 0;
         }
      return 10;
      }

   // Compare DeathDate
   if (type == 5) {
      value = 36500;
      threshold = 10;
      //if (debug) log.write("           --- cmpDD= "+p1.dS+" "+p2.dS+" "+p1.dE+" "+p2.dE);
      if (p1.dS == 0 || p2.dS == 0) {
         return 1;
         }
      if (p1.dS == p2.dS && p1.dE == p2.dE) {
         return value;
         }
      if (!report.setting_approximate) {
         return 0;
         }
      score = matchJD(p1.dS, p1.dE, p2.dS, p2.dE);
      //if (debug) log.write("           --- score= "+score);
      if (report.setting_differencemeansno && (score < threshold)) {
         return 0;
         }
      return 10;
      }

   // Compare DeathCityCtry
   if (type == 6) {
      value = 36000;
      threshold = 80;
      //if (debug) log.write("           --- cmpDP= "+p1.deathLength+" "+p2.deathLength+" "+p1.death+" "+p2.death);
      if (p1.deathLength == 0 || p2.deathLength == 0) {
         return 1;
         }
      if (p1.death.equals(p2.death)) {
         return value;
         }
      if (!report.setting_approximate) {
         return 0;
         }
      score = matchCode(p1.deathCode, p1.deathLength, p2.deathCode, p2.deathLength);
      //if (debug) log.write("           --- score= "+score);
      if (report.setting_differencemeansno && (score < threshold)) {
         return 0;
         }
      return 10;
      }

   // Compare MarriageDate
   if (type == 7) {
      value = 36500;
      threshold = 10;
      //if (debug) log.write("           --- cmpMD= "+p1.mS+" "+p2.mS+" "+p1.mE+" "+p2.mE);
      if (p1.mS == 0 || p2.mS == 0) {
         return 1;
         }
      if (p1.mS == p2.mS && p1.mE == p2.mE) {
         return value;
         }
      if (!report.setting_approximate) {
         return 0;
         }
      score = matchJD(p1.mS, p1.mE, p2.mS, p2.mE);
      //if (debug) log.write("           --- score= "+score);
      if (report.setting_differencemeansno && (score < threshold)) {
         return 0;
         }
      return 10;
      }

   // Compare MarriageCityCtry
   if (type == 8) {
      value = 36000;
      threshold = 80;
      //if (debug) log.write("           --- cmpMP= "+p1.marrLength+" "+p2.marrLength+" "+p1.marr+" "+p2.marr);
      if (p1.marrLength == 0 || p2.marrLength == 0) {
         return 1;
         }
      if (p1.marr.equals(p2.marr)) {
         return value;
         }
      if (!report.setting_approximate) {
         return 0;
         }
      score = matchCode(p1.marrCode, p1.marrLength, p2.marrCode, p2.marrLength);
      //if (debug) log.write("           --- score= "+score);
      if (report.setting_differencemeansno && (score < threshold)) {
         return 0;
         }
      return 10;
      }

   // Compare BurialDate
   if (type == 9) {
      value = 36500;
      threshold = 10;
      //if (debug) log.write("           --- cmpDD= "+p1.dS+" "+p2.dS+" "+p1.dE+" "+p2.dE);
      if (p1.dS == 0 || p2.dS == 0) {
         return 1;
         }
      if (p1.dS == p2.dS && p1.dE == p2.dE) {
         return value;
         }
      if (!report.setting_approximate) {
         return 0;
         }
      score = matchJD(p1.dS, p1.dE, p2.dS, p2.dE);
      //if (debug) log.write("           --- score= "+score);
      if (report.setting_differencemeansno && (score < threshold)) {
         return 0;
         }
      return 10;
      }

   // Compare BurialCityCtry
   if (type == 10) {
      value = 36000;
      threshold = 80;
      //if (debug) log.write("           --- cmpDP= "+p1.deathLength+" "+p2.deathLength+" "+p1.death+" "+p2.death);
      if (p1.deathLength == 0 || p2.deathLength == 0) {
         return 1;
         }
      if (p1.death.equals(p2.death)) {
         return value;
         }
      if (!report.setting_approximate) {
         return 0;
         }
      score = matchCode(p1.deathCode, p1.deathLength, p2.deathCode, p2.deathLength);
      //if (debug) log.write("           --- score= "+score);
      if (report.setting_differencemeansno && (score < threshold)) {
         return 0;
         }
      return 10;
      }


   return 0;
   }


 /**
  * Match string using a representative code
  */
  private int matchCode(int[] c1, int l1, int[] c2, int l2) {
   if (l1 * l2 == 0) return 0;
   int sum = 0;
   for (int i = 0; i < c1.length ; i++) {
      //log.write("     i, c1 et c2="+i+"-"+c1[i]+"et"+c2[i]+"="+(0+Math.min(c1[i], c2[i]) * 2));
      sum += Math.min(c1[i], c2[i]) * 2;
      }
   return (int) (sum * 90 / (l1+l2));
   }


 /**
  * Match date using julian day 
  */
  private int matchJD(int s1, int e1, int s2, int e2) {

   int ret = 0;
   int points = 10;
   int gap = 10;

   if ((s1 == 0) || (s2 == 0) || (e1 == 0) || (e2 == 0)) return 0;

   if ((s1 == s2) && (e1 == e2)) return 100;

   // Now convert into years
   s1 = s1 / 365;
   e1 = e1 / 365;
   s2 = s2 / 365;
   e2 = e2 / 365;
   // General case where both dates are points in time
   if ((s1 == e1) && (s2 == e2)) {  
      if      (Math.abs(s1 - s2) <= gap)  ret = points;
      else ret = 0;
      }
   //    s1+-------+e1             s2+-------+e2     
   else if (e1 < s2){  
      if      ((s2 - e1) <= gap)  ret = points;
      else ret = 0;
      }
   //                 s2+-------+e2     s1+-------+e1
   else if (e2 < s1) {  
      if      ((s1 - e2) <= gap)  ret = points;
      else ret = 0;
      }
   //  s1+------------------+e1 
   //                 s2+(------------------+)(e2)
   else if (s2 >= s1) { ret = points; }
   //                            s1+(----------------+)(e1)
   //                s2+------------------+e2
   else { ret = points; }

   return ret;
   }
   

 /**
  * Calculate probability from a score
  * (formula of my own invention)
  */
  private int getMatchProba(double score) {

   if (score <= 0) return 0;

   double proba = 1 - (1 / ( 1.01 + Math.pow((Math.log10(score)/10), 7) ) );

   return (int) (proba * 100);
   }

 

 /**
  * Calculates confidence level of matching between 2 entities (non individuals)
  *
  * Match calculation depends on entity type
  * Based on a scoring system; result is a percentage point
  */
  private ConfidenceMatch assessConfidenceInfo(Info i1, Info i2, boolean duplicates, Map confList, Map idNewOld) {

   ConfidenceMatch match = new ConfidenceMatch((Entity)i1.entity, (Entity)i2.entity);
   if (idNewOld != null) match.id2 = (String)idNewOld.get((String)(match.ent2.getId()));
   if (match.id2 == null && duplicates) match.id2 = (String)(match.ent2.getId());
   int score = 0;
   double scoreInfo = 0;

   if (!report.setting_approximate) {
      if ((i1.titleLength > 0 && i2.titleLength > 0 && i1.title.compareTo(i2.title) != 0) ||
          (i1.authLength > 0 && i2.authLength > 0 && i1.auth.compareTo(i2.auth) != 0) ||
          (i1.abbrLength > 0 && i2.abbrLength > 0 && i1.abbr.compareTo(i2.abbr) != 0) ||
          (i1.textLength > 0 && i2.textLength > 0 && i1.text.compareTo(i2.text) != 0))  {
         match.confLevel = 0;
         return match;
         }
      }

   // Compare Title
   double scoreTitle = 1;
   if (i1.titleLength > 0 && i2.titleLength > 0) {
      if (i1.title.compareTo(i2.title) == 0) score = 100;
      else score = matchCode(i1.titleCode, i1.titleLength, i2.titleCode, i2.titleLength);
      if (score < 90) scoreTitle = 1;
      else scoreTitle = ((double)(score*10))/100;
      }

   // Compare Auth
   double scoreAuth = 1;
   if (i1.authLength > 0 && i2.authLength > 0) {
      if (i1.auth.compareTo(i2.auth) == 0) score = 100;
      else score = matchCode(i1.authCode, i1.authLength, i2.authCode, i2.authLength); 
      if (score < 90) scoreAuth = 1;
      else scoreAuth = ((double)(score*5))/100;
      }

   // Compare Abbr
   double scoreAbbr = 1;
   if (i1.abbrLength > 0 && i2.abbrLength > 0) {
      if (i1.abbr.compareTo(i2.abbr) == 0) score = 100;
      else score = matchCode(i1.abbrCode, i1.abbrLength, i2.abbrCode, i2.abbrLength); 
      if (score < 90) scoreAbbr = 1;
      else scoreAbbr = ((double)(score*5))/100;
      }

   // Compare Text
   double scoreText = 1;
   if (i1.textLength > 0 && i2.textLength > 0) {
      if (i1.text.compareTo(i2.text) == 0) score = 100;
      else score = matchText(i1.textCode, i2.textCode); 
      if (score < 70) scoreText = 1;
      else scoreText = ((double)(score*600))/100;
      }

   match.confLevel = (int)((1-1/(1+(scoreTitle*scoreAuth*scoreAbbr*scoreText)/1000))*100);

   // manage automerge 
   if (match.confLevel > report.setting_autoMergingLevel) {
      match.confirmed = true;
      match.toBeMerged = true;
      match.choice = 3; // information kept is always in A, even if copied from B.
                        // 3 means "1" but merging of properties still to be done.
      }

   return match;
   }

 /**
  * Match texts using their words
  */
  private int matchText(HashSet text1, HashSet text2) {

   int size = text1.size();
   if (size == 0) return 0;

   int sum = 0;
   Iterator it = text1.iterator();
   while (it.hasNext()) {
      String word = (String)it.next();
      if (text2.contains(word))   {
         sum++;
         }
      }
   return sum * 100 /size;
   }


 /**
  * Get Gedcom B
  */ 
  public Gedcom getGedcomB() {
     return gedcomInputB;
     }

 /**
  * Get Gedcom output
  */ 
  public Gedcom getGedcomOutput() {
     return gedcomOutput;
     }

 /**
  * Get confidence matches
  */ 
  public List getConfidenceListOutput() {
     return confidenceListOutput;
     }

 /**
  * Get Gedcom output
  */ 
  private void displayOptions(boolean isDuplicatesRun) {
     log.write(0, 3, "", 0, report.translate("setting_action")+" :   "+report.setting_actions[report.setting_action]);
     log.write(0, 3, "", 0, DASHES);
     log.write(0, 3, "", 0, report.translate("setting_askThreshold")+" :   "+report.setting_askThreshold);
     log.write(0, 3, "", 0, report.translate("setting_analysis1")+" :   "+report.setting_analysis1s[report.setting_analysis1]);
     log.write(0, 3, "", 0, report.translate("setting_default")+" :   "+report.setting_default);
     log.write(0, 3, "", 0, report.translate("setting_approximate")+" :   "+report.setting_approximate);
     log.write(0, 3, "", 0, report.translate("setting_differencemeansno")+" :   "+report.setting_differencemeansno);
     log.write(0, 3, "", 0, DASHES);
     log.write(0, 3, "", 0, report.translate("setting_displayMergeHistory")+" :   "+report.setting_displayMergeHistory);
     log.write(0, 3, "", 0, DASHES);
     log.write(0, 3, "", 0, report.translate("setting_logOption")+" :   "+report.setting_logOption);
     log.write(0, 3, "", 0, DASHES);
     String str = "";
     if (isDuplicatesRun) {
        str = "(ignored)"+" ";
        }
     log.write(0, 3, "", 0, report.translate("setting_autoMergingLevel")+" :   "+report.setting_autoMergingLevel);
     log.write(0, 3, "", 0, report.translate("setting_ruleEntity")+" :   "+report.setting_ruleEntitys[report.setting_ruleEntity]);
     log.write(0, 3, "", 0, str+report.translate("setting_headerChosen")+" :   "+report.setting_headerChosens[report.setting_headerChosen]);
     log.write(0, 3, "", 0, report.translate("setting_outputFileExt")+" :   "+report.setting_outputFileExt);
     log.write(0, 3, "", 0, report.translate("setting_keepAecl")+" :   "+report.setting_keepAecl);
     log.write(0, 3, "", 0, report.translate("setting_keepAcon")+" :   "+report.setting_keepAcon);
     log.write(0, 3, "", 0, str+report.translate("setting_keepBcon")+" :   "+report.setting_keepBcon);
     log.write(0, 3, "", 0, str+report.translate("setting_keepBecl")+" :   "+report.setting_keepBecl);
     log.write(0, 3, "", 0, report.translate("setting_flagChanges")+" :   "+report.setting_flagChanges);
     log.write(" ");
     return;
     }

 /**
  * For debug purposes, display raw data of confidence list
  */ 
  private void displayConfList(Map confList) {

     ConfidenceMatch match = null;
     log.write(0, 0, "", 0, "key:__x__"+ "\t" + "id1" + "\t" + "id2" + "\t" + "strid2" + "\t" + "lvl" 
		+ "\t" + "conf" + "\t" + "tbm" + "\t" + "choice" );
     for (Iterator it = confList.keySet().iterator(); it.hasNext(); ) {
       String key = (String)it.next();
       match = (ConfidenceMatch)confList.get(key);
       log.write(0, 0, "", 0, "key:"+key+ "\t" + match.ent1.getId() + "\t" + match.ent2.getId() + "\t" + match.id2 + "\t" + match.confLevel 
		+ "\t" + match.confirmed + "\t" + match.toBeMerged + "\t" + match.choice );
       }
     return;
     }

 /**
  * For debug purposes, get all elements of the entity in a text string 
  */
   private String printCluster(Property prop) {

     String text = "";
     String totalStr = "";

     //log.write(0, 0, "", 0, " ");

     if (!(prop instanceof Entity)) {
        text += " "+prop.getTag()+": "+prop.toString();
        totalStr += text;
        }
     List listProp = new LinkedList();
     Property[] properties = prop.getProperties();
     listProp.addAll(Arrays.asList(properties));
     Property propItem = null;
     while (listProp.size() > 0) {
        propItem = (Property) ((LinkedList)listProp).removeFirst();
        int indent = (propItem.getPath().length() - 2) * 3 + 1;
        Property[] subProps = propItem.getProperties();
        listProp.addAll(0, Arrays.asList(subProps));
        String value = propItem.toString();
        if (value.length() > 90) 
           value = value.substring(0, 90)+"...";
        text = propItem.getTag()+": "+value;
        //log.write(0, indent, "", 0, text);
        totalStr += text;
        }
     return totalStr;
     }


   
} // End_of_Report
    
