/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genjreports.tools;

import genj.app.Workbench;
import genj.gedcom.*;
import genj.gedcom.time.PointInTime;
import genj.report.Report;
import genj.util.swing.Action2;
import genj.io.GedcomReader;
import genj.io.GedcomWriter;
import genj.io.GedcomIOException;
import genj.util.Origin;
import genjreports.tools.imports.ImportGenealogieDotCom;
import genjreports.tools.imports.ImportGeneric;
import genjreports.tools.imports.ImportHeredis;
import genjreports.tools.imports.Importer;

import java.text.DecimalFormat;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;


/**
 * GenJ - Report
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
// FIXME: remove @suppresswranings
@SuppressWarnings("unchecked")
public class ReportToolBox extends Report {

  /** option - Tool to run */
  private final static int
    TOOL_GENE_ID       = 0,
    TOOL_GENE_SOSA     = 1,
    TOOL_TREE_TOP      = 2,
    TOOL_REMOVE_TAG    = 3,
    TOOL_MNG_PLACES    = 4,
    TOOL_MNG_ASSO      = 5,
    TOOL_GENE_NAME     = 6,
    TOOL_GENE_AGES     = 7,
    TOOL_IMPORT_GEDCOM = 8;

  public int toolToRun = TOOL_GENE_ID;
  
  public String toolToRuns[] = {
      translate("geneIds"),
      translate("geneSosaNb"),
      translate("geneTreeTop"),
      translate("geneRemoveTag"),
      translate("geneMngPlaces"),
      translate("geneManageAsso"),
      translate("geneGivnSurn"),
      translate("geneAges"),
      translate("importGedcom")
   };

  /** entity types */
  private final static int
       ENT_ALL     = 0,
       ENT_INDI    = 1,
       ENT_FAM     = 2,
       ENT_NOTE    = 3,
       ENT_SOUR    = 4,
       ENT_SUBM    = 5,
       ENT_REPO    = 6;
  
  /** option - Log file */
  public boolean logOption = false;
  private Log log = null;
    
  /**
   * While we generate information on stdout it's not really
   * necessary because we're returning a Document
   * that is handled by the UI anyways
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * One of the report's entry point
   */
  public void start(Gedcom gedcom) {
    start(gedcom, null);
  }
 
  /**
   * Our main logic
   */
  private void start(Gedcom gedcom, Indi indiDeCujus) {

    // Launch tool chosen in the options
    boolean ret = false;
    Object settings;
    String title = translate("option_title")+" "+toolToRuns[toolToRun];
    log = new Log(this, title, translate("chooseLog"), logOption);
    if (logOption && (log == null || !log.writeFile)) return;

    switch (toolToRun) {
      case TOOL_GENE_ID:
        settings = new SettingIDs();
        if (!getOptionsFromUser(title, settings)) return;
        ret = toolSettingIDs(gedcom, settings);
        break;
      case TOOL_GENE_SOSA:
        settings = new SettingSosas();
        if (!getOptionsFromUser(title, settings)) return;
        ret = toolGeneSosaNbs(gedcom, settings, indiDeCujus, toolToRun);
        break;
      case TOOL_TREE_TOP:
        settings = new SettingTreeTops();
        if (!getOptionsFromUser(title, settings)) return;
        ret = toolGeneSosaNbs(gedcom, settings, indiDeCujus, toolToRun);
        break;
      case TOOL_REMOVE_TAG:
        settings = new SettingRmTags();
        if (!getOptionsFromUser(title, settings)) return;
        ret = deleteTags(gedcom, ((SettingRmTags)settings).removeTag, ((SettingRmTags)settings).entToDo);
        break;
      case TOOL_MNG_PLACES:
        settings = new SettingMngPlaceTags();
        if (!getOptionsFromUser(title, settings)) return;
        ret = managePlaces(gedcom, ((SettingMngPlaceTags)settings));
        break;
      case TOOL_MNG_ASSO:
        settings = new SettingMngAssoTags();
        if (!getOptionsFromUser(title, settings)) return;
        ret = manageAsso(gedcom, ((SettingMngAssoTags)settings));
        break;
      case TOOL_GENE_NAME:
        settings = new SettingGeneGivnSurn();
        if (!getOptionsFromUser(title, settings)) return;
        ret = geneGivnSurn(gedcom, ((SettingGeneGivnSurn)settings));
        break;
      case TOOL_GENE_AGES:
        settings = new SettingGeneAge();
        if (!getOptionsFromUser(title, settings)) return;
        ret = geneAges(gedcom, ((SettingGeneAge)settings));
        break;
      case TOOL_IMPORT_GEDCOM:
        settings = new SettingImportGedcom();
        //if (!getOptionsFromUser(title, settings)) return;
        ret = importGedcom(gedcom, ((SettingImportGedcom)settings));
        break;
      default:
        throw new IllegalArgumentException("no such report type");
    }

    // done with main output
    if (ret) log.write(translate("Completed"));      
    else log.write(translate("Error"));      
    
    // show log to the user
    if (logOption) log.close();
   

  } // end_of_start

  
  /**
  * ### 1 ### Re-Generation of Ids in Gedcom file
  */
  private boolean toolSettingIDs(Gedcom gedcom, Object object) {
    // Logic:
    //    Get all IDs for INDI, FAM, NOTE, SOUR, SUBM, REPO and assign new ones from 1
    //    (use temporary ids to avoid duplicates and locks)
    // Get the options
    final SettingIDs settings = (SettingIDs)object;
    String entityTypes[] = {
       "all", 
       Gedcom.INDI, 
       Gedcom.FAM, 
       Gedcom.NOTE, 
       Gedcom.SOUR,   
       Gedcom.SUBM,   
       Gedcom.REPO   
       };
  
    int pad = 1;
    if (settings.paddingId >0 && settings.paddingId < 11) 
       pad = settings.paddingId;
    final DecimalFormat formatNbrs = new DecimalFormat("000000000000".substring(0,pad));
   
    /* use the following in Java 1.5
    String formatNbrs = "%1d";
    if (settings.paddingId >0 && settings.paddingId < 11) 
       formatNbrs = "%0"+String.valueOf(settings.paddingId)+"d";
      */
    
    // Loop over all entity types
    for (int i = 0; i < entityTypes.length; i++) { 
       if ((i == 0) || ((settings.entToDo != 0) && (settings.entToDo != i))) continue;
       Collection<? extends Entity> entities = gedcom.getEntities(entityTypes[i]);
       final String entityIDPrefix = gedcom.getNextAvailableID(entityTypes[i]).substring(0,1);
       final Map<String,String> listID = new TreeMap<String,String>(); // sorted mapping list
       String oldID, newID = "";
       int iCounter = 0;
       String key, ID;
       
       // First loop to get list of ids and sort on value of entity
       log.write(translate("Entity")+" "+entityTypes[i]+"...");
       log.write("("+translate("MustPrefix")+" '"+entityIDPrefix+"')");
       for (Entity entity: entities) {
         ID = entity.getId();
         key = entity.toString();
         listID.put(key, ID);
         } // end loop
       log.write(translate("Size")+" "+listID.size());

       // Second loop to give temp ids in order to avoid duplicates
       try {
       gedcom.doUnitOfWork(new UnitOfWork() { public void perform(Gedcom gedcom) throws GedcomException { 

       int iCounter = 0;
       for (Iterator it = listID.keySet().iterator(); it.hasNext();) {
         String key = (String)it.next();
         String oldID = listID.get(key);
         Entity entity = gedcom.getEntity(oldID);
         iCounter++;       
         log.write(oldID+" --> "+entityIDPrefix+settings.prefixID+formatNbrs.format(iCounter)+settings.suffixID, false);
         String newID = entityIDPrefix+settings.prefixID+"XYZAWZ"+iCounter+settings.suffixID;  // Just a weird string ensuring no duplicates with existing ids
         try {
            entity.setId(newID); 
            listID.put(key, newID);
            } catch (GedcomException e) {
            log.write(e.getMessage());
            }
         } // proceed with other entity

       // Third loop to give final ids
       iCounter = 0;
       for (Iterator it = listID.keySet().iterator(); it.hasNext();) {
         String key = (String)it.next();
         String oldID = listID.get(key);
         Entity entity = gedcom.getEntity(oldID);
         iCounter++;
         String newID = entityIDPrefix+settings.prefixID+formatNbrs.format(iCounter)+settings.suffixID;
         try {
            entity.setId(newID); 
            } catch (GedcomException e) {
            log.write(e.getMessage());
            }
         } // proceed with other entity

       } }); // end of doUnitOfWork
       } catch (GedcomException e) {
       log.write(e.getMessage());
       }

       } // proceed with other entity type

    log.write(translate("EntityIdDone"));

    return true;  
    // done
  }

  /**
  * ### 2 + 3 ### Re-generation of sosa numbers to individuals of the gedcom
  */
  private boolean toolGeneSosaNbs(Gedcom gedcom, Object object, Indi indiDeCujus, int action) {
    // Get the options
    DecimalFormat formatNbrs = new DecimalFormat("0");
    String tagStr; 
    SettingSosas settings1 = new SettingSosas();
    SettingTreeTops settings2 = new SettingTreeTops();
    if (action == TOOL_GENE_SOSA) {
       settings1 = (SettingSosas) object;
       int pad = 1;
       if (settings1.paddingSize >0 && settings1.paddingSize < 11) 
          pad = settings1.paddingSize;
       formatNbrs = new DecimalFormat("000000000000".substring(0,pad));
       tagStr = settings1.sosaTag;
       }
    else {
       settings2 = (SettingTreeTops) object;
       tagStr = settings2.treeTopTag;
       }

    // get de-cujus (sosa 1) 
    if (indiDeCujus == null) {
      String msg = translate(toolToRuns[action])+" - "+translate("AskDeCujus");
      indiDeCujus = (Indi)getEntityFromUser(msg, gedcom, Gedcom.INDI);
      if (indiDeCujus == null) return false;
      }

    // Clean gedcom file for all tags
    deleteTags(gedcom, tagStr, ENT_INDI);

    List<Pair> sosaList = new ArrayList<Pair>();   // list only used to store ids of sosas
    Pair pair = new Pair("",0);
    String indiID = "";
    Indi indi, indiOther;
    Indi indiSiblings[];
    int sosaCounter = 0,
        sosaFathers = 0,
        sosaMothers = 0,
        sosaDabo = 0,
        sosaOSiblings = 0,
        sosaYSiblings = 0,
        treeTops = 0;
    Fam famc;

    // Put de-cujus first in list and update its sosa tag
    sosaList.add(new Pair(indiDeCujus.getId(), 1));
    if (action == TOOL_GENE_SOSA) {
       // Perform unit of work
       final DecimalFormat format = formatNbrs;
       final String tag = tagStr; 
       final Indi indiFirst = indiDeCujus;
       try {
          gedcom.doUnitOfWork(new UnitOfWork() { public void perform(Gedcom gedcom) throws GedcomException { 
             indiFirst.addProperty(tag, format.format(1), setPropertyPosition(indiFirst));
             } }); // end of doUnitOfWork
          } catch (GedcomException e) {
          log.write(e.getMessage());
          }
       }
    if ((action == TOOL_TREE_TOP) && (settings2.DisplayIndi)) {
       String line = "-----------------------------------------------------------";
       log.write(translate("DisplayIndi"));     
       log.write(line.substring(0,translate("DisplayIndi").length()));
       }

    // Iterate on the list to go up the tree. 
    // Update sosa tag according to action required
    // Store both parents in list
    try {
    for (ListIterator<Pair> listIter = sosaList.listIterator(); listIter.hasNext();) {
       pair = listIter.next();
       indiID = pair.ID;
       sosaCounter = pair.sosa;
       indi = (Indi)gedcom.getEntity(indiID);

       if (action == TOOL_GENE_SOSA && !settings1.daboville) {
          // Get older siblings
          for (Iterator siblings = Arrays.asList(indi.getOlderSiblings()).iterator(); siblings.hasNext();) {
             indiOther = (Indi)siblings.next();
             indiOther.addProperty(tagStr, formatNbrs.format(sosaCounter)+settings1.olderSign, setPropertyPosition(indiOther));
             sosaOSiblings++;
             log.write(indiOther.toString()+" -> "+formatNbrs.format(sosaCounter)+settings1.olderSign, false);
             }
          // Get younger siblings
          for (Iterator siblings = Arrays.asList(indi.getYoungerSiblings()).iterator(); siblings.hasNext();) {
             indiOther = (Indi)siblings.next();
             indiOther.addProperty(tagStr, formatNbrs.format(sosaCounter)+settings1.youngerSign, setPropertyPosition(indiOther));
             sosaYSiblings++;
             log.write(indiOther.toString()+" -> "+formatNbrs.format(sosaCounter)+settings1.youngerSign, false);
             }
          }

       // Get father and mother
       famc = indi.getFamilyWhereBiologicalChild();
       if (famc!=null)  {
         indiOther = famc.getWife();
         if (indiOther!=null) {
            if (action == TOOL_GENE_SOSA) { 
               indiOther.addProperty(tagStr, formatNbrs.format(2*sosaCounter+1), setPropertyPosition(indiOther));
               log.write(indiOther.toString()+" -> "+formatNbrs.format(2*sosaCounter+1), false);
               }
            listIter.add(new Pair(indiOther.getId(), 2*sosaCounter+1));
            listIter.previous();
            sosaMothers++;
            }
         indiOther = famc.getHusband();
         if (indiOther!=null) {
            if (action == TOOL_GENE_SOSA) {
               indiOther.addProperty(tagStr, formatNbrs.format(2*sosaCounter), setPropertyPosition(indiOther));
               if (settings1.daboville) sosaDabo += toolGeneDabo(indiOther, tagStr, formatNbrs.format(sosaCounter));
               log.write(indiOther.toString()+" -> "+formatNbrs.format(2*sosaCounter), false);
               }
            listIter.add(new Pair(indiOther.getId(), 2*sosaCounter));
            listIter.previous();
            sosaFathers++;
            }
          }
       else {
          if (action == TOOL_TREE_TOP) {
             indi.addProperty(tagStr, settings2.treeTopValue);
             treeTops++;
             log.write(indi.toString(), settings2.DisplayIndi);     
            }
          }
       }
       } catch (GedcomException e) {
       log.write(e.getMessage());
       }

    // Stops updating Gedcom  
    formatNbrs = new DecimalFormat("000000");
    if (action == TOOL_GENE_SOSA) {
       log.write(" ");
       log.write(translate("CreatedDC")+" "+formatNbrs.format(1));
       log.write(translate("CreatedF")+" "+formatNbrs.format(sosaFathers));
       log.write(translate("CreatedM")+" "+formatNbrs.format(sosaMothers));
       log.write("-------------------------------------------------------");
       log.write(translate("CreatedT")+" "+formatNbrs.format(sosaFathers+sosaMothers+1));
       if (settings1.daboville) log.write(translate("CreatedD")+" "+formatNbrs.format(sosaDabo));
       else {
          log.write(translate("CreatedO")+" "+formatNbrs.format(sosaOSiblings));
          log.write(translate("CreatedY")+" "+formatNbrs.format(sosaYSiblings));
          }
       log.write("=======================================================");
       log.write(translate("CreatedG")+" "+formatNbrs.format(sosaFathers+sosaMothers+sosaDabo+sosaOSiblings+sosaYSiblings+1));
       log.write(" ");
       }

    if (action == TOOL_TREE_TOP) {
       log.write(" ");
       log.write(" ");
       log.write(translate("CreatedTT")+" "+formatNbrs.format(treeTops));
       log.write(" ");
       }

    changeGedcom(gedcom);
    return true;  
    // done
  }



  /**
  * ### 4 ### Deletion of tags in Gedcom file
  */
  private boolean deleteTags(Gedcom gedcom, String removeTag, int entityType) {
    // name of entities
    String entityTypes[] = {
       "all", 
       Gedcom.INDI, 
       Gedcom.FAM, 
       Gedcom.NOTE, 
       Gedcom.SOUR,   
       Gedcom.SUBM,   
       Gedcom.REPO   
       };

    // Clean gedcom file for all tags
    log.write(translate("deleting_tag")+" "+removeTag+"...");  
    log.write(" ");
    Collection entities = null;
    if (entityType == ENT_ALL) {
       entities = gedcom.getEntities();
       }
    else { 
       entities = gedcom.getEntities(entityTypes[entityType]);
       }

    // will let us write log of all entites where tag has been deleted
    final String tag = removeTag;
    final List listEntities = new ArrayList(entities);
    final Gedcom gedFile = gedcom;
    Collections.sort(listEntities, sortEntities);

    // Perform unit of work
    try {
    gedcom.doUnitOfWork(new UnitOfWork() { public void perform(Gedcom gedcom) throws GedcomException { 
 
    Entity ent;
    int iCounter = 0;

    List propsToDelete = new ArrayList();
    for (Iterator it = listEntities.iterator(); it.hasNext();) {
       ent = (Entity) it.next();
       getPropertiesRecursively((Property)ent, propsToDelete, tag);
       for (Iterator props = propsToDelete.iterator(); props.hasNext();) {
         Property prop = (Property)props.next();
         if (prop != null) {
            Property parent = prop.getParent();
            if (parent != null) {
               String propText = parent.getTag()+" "+tag+" '"+prop.toString()+"'";
               parent.delProperty(prop);
               iCounter++;
               log.write(ent.getTag()+" "+ent.toString()+" - "+translate("DeletedProp")+": "+propText, false);
               }
            }
         }
       }

    DecimalFormat formatNbrs = new DecimalFormat("000000");
    log.write(" ");
    log.write(translate("DeletedNb")+" "+formatNbrs.format(iCounter));
    if (logOption) log.write(translate("Details"));
    log.write(" ");

   } }); // end of doUnitOfWork
   } catch (GedcomException e) {
   log.write(e.getMessage());
   }

    changeGedcom(gedcom);
    return true;  
    // done
  }
  
  private void getPropertiesRecursively(Property parent, List props, String tag) {
    Property[] children = parent.getProperties();
    for (int c=0;c<children.length;c++) {
      Property child = children[c];
      if (child.getTag().compareTo(tag) == 0) {
        props.add(child);
        }
      getPropertiesRecursively(child, props, tag);
      }
    }

  /**
  * ### 5 ### Manages places in gedcom files
  */
  private boolean managePlaces(Gedcom gedcom, SettingMngPlaceTags setting) {

    // Get places tag in header
    log.write(" ");
    log.write(translate("placeAssessingCurrent"));
    String[] tags = validatePlaceFormat(gedcom.getPlaceFormat());
    String currentPlaceFormat = convertPlaceFormat(tags);
    int size = (tags != null) ? tags.length : 0;

    // Assess place fields in gedcom
    Collection entities = gedcom.getEntities();
    List placesProps = new ArrayList();
    for (Iterator it = entities.iterator(); it.hasNext();) {
       Entity ent = (Entity) it.next();
       getPropertiesRecursively((Property)ent, placesProps, "PLAC");
       }

    int volume = placesProps.size();

    int[] counters = new int[100];
    int max = 0;
    List anomalies = new ArrayList();
    for (Iterator it = placesProps.iterator(); it.hasNext();) {
       Property prop = (Property) it.next();
       String[] place = prop.toString().split("\\,", -1);
       if ((size > 0) && (place.length != size)) { 
          if (isEmpty(place) && setting.emptyTagsAllowed)
             continue;
          anomalies.add(prop);
          }
       if (place.length > max) 
          max = place.length;
       for (int i=0; i < Math.min(counters.length, place.length); i++) {
          if (place[i].length() != 0) {
             counters[i]++;
             }
          }
       }

    log.write(" ");
    log.write(translate("placeStats"));
    log.write("   "+translate("placeVolume")+": "+volume);
    log.write("   "+translate("placeMax")+": "+max);
    log.write("   "+translate("placeFieldsCount"));
    for (int i=0; i < Math.min(max, 100); i++) {
       String field = (i<size) ? tags[i] : ""+(i+1);
       log.write("      "+field+":\t"+counters[i]);
       }

    log.write(" ");
    log.write(translate("placeAnomalies"));
    for (Iterator it = anomalies.iterator(); it.hasNext();) {
       PropertyPlace prop = (PropertyPlace) it.next();
       log.write("   "+prop.getEntity().getId()+" -  "+prop.getPath().toString()+":"+prop.toString());
       }
    if (anomalies.size() == 0)   
       log.write("   "+translate("placeNoAnomalies"));

    // Asks user what he wants to do
    boolean[] flags = getFlagsFromUser((size>0), (volume>0), translate("placeInputChoice"));

    String newPlaceFormat = "";
    String[] tags1 = null;
    if (flags[0]) {    
       // Get input from user about new tag and if to add/modify or remap
       log.write(" ");
       log.write(translate("placeAssessingNew"));
       newPlaceFormat = getValueFromUser("keyNewPlace", translate("placeInputNew")+" - "+currentPlaceFormat);
       if (newPlaceFormat == null) return false;
       tags1 = validatePlaceFormat(newPlaceFormat);
       newPlaceFormat = convertPlaceFormat(tags1);
       gedcom.setPlaceFormat(newPlaceFormat);
       log.write("   "+translate("placeFormatChangedTo"));
       log.write("   "+newPlaceFormat);
       }
    
    int[] placeMap = null;
    if (flags[1]) {    
       // Get mapping from user
       log.write(" ");
       log.write(translate("placeMapping"));
       placeMap = new int[tags1.length];
       ArrayList tags2 = null;
       ArrayList tagsTemp = null;
       if (tags == null) {
          tags2 = new ArrayList(); 
          tagsTemp = new ArrayList(); 
          }
       else {
          tags2 = new ArrayList((Collection)Arrays.asList(tags)); 
          tagsTemp = new ArrayList((Collection)Arrays.asList(tags)); 
          }  
       for (int i = tags2.size(); i < tags1.length; i++) {
          tags2.add(translate("placeNewField")+" ("+(i+1)+")");
          tagsTemp.add(translate("placeNewField")+" ("+(i+1)+")");
          }
       
       for (int i = 0; i < tags1.length; i++) {
          String tag = tags1[i];
          String msg2 = translate("placeAskMapping", tag);
          String selection = (String)getValueFromUser(msg2, tags2.toArray(), tags2.get(0));
          int iSel = 0;
          if (selection == null) selection = (String)tags2.get(0);
          iSel = tags2.indexOf(selection); 
          placeMap[i] = tagsTemp.indexOf(selection);
          log.write("   "+tags1[i]+" <- "+tags2.get(iSel));
          if (tags2.size() > 1) tags2.remove(iSel); 
          }
       }
       
    if (flags[2]) {    
       // Remap if required
       log.write(" ");
       log.write(translate("placeRemapping"));
       if (placeMap == null) {
          placeMap = new int[size];
          for (int i = 0; i < size; i++) {
             placeMap[i] = i;
             }
          }
       // start of doUnitOfWork
       final int[] uowplaceMap = placeMap;
       final List uowplacesProps = placesProps;
       try {
       gedcom.doUnitOfWork(new UnitOfWork() { public void perform(Gedcom gedcom) throws GedcomException { 
       int changes = 0;
       for (Iterator it = uowplacesProps.iterator(); it.hasNext();) {
          Property propPlace = (Property)it.next();
          String place = propPlace.toString();
          String[] placeTab = place.split("\\,", -1);
          String newPlace = "";
          for (int i = 0; i < uowplaceMap.length; i++) {
             if (uowplaceMap[i] < placeTab.length) 
                newPlace += placeTab[uowplaceMap[i]].trim()+",";
             else 
                newPlace += ",";
             }
          newPlace = newPlace.substring(0,newPlace.length()-1); // remove last comma
          propPlace.setValue(newPlace);
          changes++;
          }
       log.write("   "+translate("placeChanged")+": "+changes);
       } }); // end of doUnitOfWork
       } catch (GedcomException e) {
       log.write(e.getMessage());
       }
       }
       
    if (flags[3]) {    
       // Remove if required
       // start of doUnitOfWork
       final Gedcom uowGedcom = gedcom;
       try {
          gedcom.doUnitOfWork(new UnitOfWork() { public void perform(Gedcom gedcom) throws GedcomException { 
          uowGedcom.setPlaceFormat("");
          Entity entity = uowGedcom.createEntity(Gedcom.INDI, uowGedcom.getNextAvailableID(Gedcom.INDI));
          uowGedcom.deleteEntity(entity); 
          } }); // end of doUnitOfWork
          } catch (GedcomException e) {
          log.write(e.getMessage());
          }
       log.write(" ");
       log.write(translate("placeRemoved"));
       }
       
    log.write(" ");
    changeGedcom(gedcom);
    return true;  
    // done
  }
  
  /**
  * ### 6 ### Manages ASSO tags in Gedcom file
  */
  private boolean manageAsso(Gedcom gedcom, SettingMngAssoTags setting) {

    // Clean gedcom file for all tags
    log.write(translate("processing_tag")+" RELA...");  
    log.write(" ");

    Collection entities = gedcom.getEntities();
    final List listEntities = new ArrayList(entities);
    final Properties rela_properties = new Properties();
    String exportFile = null;
    Gedcom gedcomX = null; 

    if (setting.action == 0) {
      log.write(translate("SettingMngAssoTags.test"));
      log.write(" ");
      }

    // REMOVE (remove references from gedcom and saves gedcom in another file)
    if (setting.action == 1) {
      log.write(translate("SettingMngAssoTags.remove"));
      log.write(" ");
      }

    if (setting.action == 0 || setting.action == 1) {
      // Scann gedcom to identify all the references in ASSO:RELA tags
      // Perform unit of work to change dates of CHAN tags, otherwise integration when restore would show these records as changed.
      // (this is commented out for the moment though as there will anyway be a time difference with the restore).
      try {
      gedcom.doUnitOfWork(new UnitOfWork() { public void perform(Gedcom gedcom) throws GedcomException { 

      Entity ent;
      int iCounter = 0;
      for (Iterator it = listEntities.iterator(); it.hasNext();) {
          ent = (Entity) it.next();
          Property[] relaToDelete = ent.getProperties(new TagPath(".:ASSO:RELA"));
          for (int i=0; i<relaToDelete.length; i++) {
            Property prop = relaToDelete[i];
            if (prop != null) {
               String strParent = prop.getParent().getValue();
               String strFrom = prop.getValue();
               if (strFrom != null && strFrom.lastIndexOf("@") > 0) {
                  String strTo = strFrom.substring(0, strFrom.lastIndexOf("@"));
                  String logText = prop.getTag()+" RELA "+strFrom;
                  String strKey = ent.getId()+strParent+strTo;
                  String strExists = rela_properties.getProperty(strKey);
                  if (strExists != null) {
                     log.write(translate("WarningDuplicate", ent.toString()));
                     }
                  rela_properties.setProperty(strKey, strFrom);
                  //prop.setValue(strTo);
                  iCounter++;
                  log.write(ent.getTag()+" "+ent.toString()+" - "+strParent+" - "+translate("ProcessedProp")+": "+strFrom+" => "+strTo, false);
                  }
               }
            }
          }

      DecimalFormat formatNbrs = new DecimalFormat("000000");
      log.write(" ");
      log.write(translate("ProcessedNb")+" "+formatNbrs.format(iCounter));
      if (logOption) log.write(translate("DetailsProcessed"));
      log.write(" ");

      } }); // end of doUnitOfWork
      } catch (GedcomException e) {
      log.write(e.getMessage());
      }

      //changeGedcom(gedcom);

      // Saves properties
      try {
          String fileName = gedcom.getOrigin().getFile().getParentFile().getAbsolutePath() + File.separator + gedcom.getOrigin().getName() + "_rela.txt"; 
          OutputStream out = new FileOutputStream(fileName);
          if (out != null)
           {
           rela_properties.store(out, "");
           out.close();
           log.write(translate("RelaFile")+" "+fileName);
           }
          } catch (IOException e) { 
          e.printStackTrace(); 
          } 

      // Exports gedcom file in another one
      try {
          String gedcomFile = gedcom.getOrigin().getFile().getAbsolutePath();
          exportFile = gedcom.getOrigin().getFile().getParentFile().getAbsolutePath() + File.separator + gedcom.getOrigin().getName() + "_export.ged"; 
          BufferedInputStream  in  = new BufferedInputStream(new FileInputStream  (gedcomFile));
          FileOutputStream out = new FileOutputStream (exportFile); 

          if (in != null && out != null)
           {
           String export = null;
           String record = null;
           int LINE_SIZE = 512;
           byte[] bufferIn  = new byte[LINE_SIZE];
           byte[] bufferOut = new byte[LINE_SIZE];
           int recCount = 0;
           int bytes_read; 
           int i = 0;
           in.mark(LINE_SIZE);
           while ((bytes_read = in.read(bufferIn)) != -1 ) {
              for (i=0; i<bytes_read; i++) {
                 if (bufferIn[i] != System.getProperty("line.separator").charAt(0)) continue;
                 recCount++;
                 record = new String(bufferIn, 0, i);
                 if (record.startsWith("2 RELA")) {
                    int j=0;
                    while (bufferIn[j] != "@".charAt(0)) { bufferOut[j] = bufferIn[j]; j++; }
                    out.write(bufferOut, 0, j); 
                    out.write(System.getProperty("line.separator").charAt(0));  //eol
                    }
                 else {
                    int j=0;
                    while (j <= i) { bufferOut[j] = bufferIn[j]; j++; }
                    out.write(bufferOut, 0, i+1); 
                    }
                 in.reset();
                 in.skip(i+1);
                 in.mark(LINE_SIZE); 
                 break;
                 }
              }
           out.close();
           log.write(translate("ExportFile", new Object[] { exportFile, String.valueOf(recCount) }));
           }
          } catch (IOException e) { 
          e.printStackTrace(); 
          } 

      }

    if (setting.action == 0) {
       log.write(" ");
       log.write(translate("Opening Export"));
       gedcomX =  openGedcomFile(exportFile);
       }

    if (setting.action == 2) {
       gedcomX =  gedcom;
       }

    // RESTORE (restore references to current gedcom from properties)
    if (setting.action == 0 || setting.action == 2) {
      log.write(translate("SettingMngAssoTags.restore"));
      log.write(" ");
      entities = gedcomX.getEntities();
      final List listEntitiesX = new ArrayList(entities);
      String fileName = "";
      try {
          File file = getFileFromUser(translate("ImportTags"), Action2.TXT_OK);
          if (file == null) return false; 
          // gedcom.getOrigin().getFile().getParentFile().getAbsolutePath() + File.separator + gedcom.getOrigin().getName() + "_rela.txt";
          fileName = file.getAbsolutePath(); 
          InputStream in = new FileInputStream(fileName);
          if (in != null)
           {
           rela_properties.load(in);
           in.close();
           }
          } catch (IOException e) { 
          e.printStackTrace(); 
          log.write(translate("NoRelaFile")+" "+fileName);
          return false;
          } 

       // Perform unit of work
       try {
       gedcom.doUnitOfWork(new UnitOfWork() { public void perform(Gedcom gedcomX) throws GedcomException { 
 
       Entity ent;
       int iCounter = 0;

       for (Iterator it = listEntitiesX.iterator(); it.hasNext();) {
          ent = (Entity) it.next();
          Property[] relaToDelete = ent.getProperties(new TagPath(".:ASSO:RELA"));
          for (int i=0; i<relaToDelete.length; i++) {
            Property prop = relaToDelete[i];
            if (prop != null) {
               String strParent = prop.getParent().getValue();
               String strFrom = prop.getValue();
               if (strFrom != null) {
                  String strKey = ent.getId()+strParent+strFrom.substring(0, strFrom.lastIndexOf("@"));
                  String strTo = rela_properties.getProperty(strKey);
                  if (strTo != null && strTo.length() != 0) {
                     String logText = prop.getTag()+" RELA "+strFrom;
                     prop.setValue(strTo);
                     iCounter++;
                     log.write(ent.getTag()+" "+ent.toString()+" - "+strParent+" - "+translate("ProcessedProp")+": "+strTo, false);
                     }
                  }
               }
            }
          }

       DecimalFormat formatNbrs = new DecimalFormat("000000");
       log.write(" ");
       log.write(translate("ProcessedNb")+" "+formatNbrs.format(iCounter));
       if (logOption) log.write(translate("DetailsProcessed"));
       log.write(" ");

      } }); // end of doUnitOfWork
      } catch (GedcomException e) {
      log.write(e.getMessage());
      }

      changeGedcom(gedcomX);
      }

    if (setting.action == 0) {
       saveGedcom(gedcomX);
       }

    log.write(" ");
    return true;
    // done
  }

  /**
  * ### 7 ### Generation of SURN and GIVN from NAME
  */
  private boolean geneGivnSurn(Gedcom gedcom, Object object) {
    // Logic:
    //    Get all individuals where either GIVN or SURN is empty and propose one

    // Get the options
    final SettingGeneGivnSurn settings = (SettingGeneGivnSurn)object;

    Collection entities = gedcom.getEntities(Gedcom.INDI);
    final List listEntities = new ArrayList(entities);
    final Gedcom gedFile = gedcom;

    // Perform unit of work
    try {
    gedcom.doUnitOfWork(new UnitOfWork() { public void perform(Gedcom gedcom) throws GedcomException { 
 
    Indi indi;
    int iCounter = 0;
    boolean propChange = false;
    int rep = 0;

    List propsToChange = new ArrayList();
    for (Iterator it = listEntities.iterator(); it.hasNext();) {
       indi = (Indi) it.next();
       String surname = indi.getLastName();
       String firstname = indi.getFirstName();
       if (isToBeExcluded(surname, firstname, settings.geneExclude)) {
          log.write(translate("geneExcluded") + " : " + indi.toString());
          continue;
          }
       String surnValue = "";
       String givnValue = "";
       String surnValueNew = "";
       String givnValueNew = "";
       Property surnProp = indi.getPropertyByPath("INDI:NAME:SURN");
       Property givnProp = indi.getPropertyByPath("INDI:NAME:GIVN");
       if (surnProp != null) surnValue = surnProp.toString();
       if (givnProp != null) givnValue = givnProp.toString();

       // if lastname is not empty and (SURN does not exist or is empty) then fill it in
       if (surname.length() > 0 && (surnProp == null || surnValue.length() == 0)) {
          surnValueNew = surname;
          propChange = true;
          }

       // if firstname is not empty and (GIVN does not exist or is empty) then fill it in
       if (firstname.length() > 0 && (givnProp == null || givnValue.length() == 0)) {
          givnValueNew = firstname;
          propChange = true;
          }

       if (propChange) {
          Property propName = indi.getPropertyByPath("INDI:NAME");
          if (surnValueNew.length() == 0) surnValueNew = surnValue;
          if (givnValueNew.length() == 0) givnValueNew = givnValue;
          String msg = surname+", "+firstname+" ("+indi.getId().toString()+") :\r\n   SURN: '"+surnValue+"'\t ==> \t'"+surnValueNew+"'\r\n   GIVN: '"+givnValue+"'\t ==> \t'"+givnValueNew+"'";
          if (settings.geneAuto 
          || ((rep = JOptionPane.showConfirmDialog(null, msg, translate("geneGSAsk"), JOptionPane.YES_NO_CANCEL_OPTION)) == JOptionPane.YES_OPTION)) {
            if (surnProp == null) propName.addProperty("SURN", surnValueNew);
            else surnProp.setValue(surnValueNew);
            if (givnProp == null) propName.addProperty("GIVN", givnValueNew);
            else givnProp.setValue(givnValueNew);
            log.write(msg);
            }
          propChange = false;
          }

       if (rep == JOptionPane.CANCEL_OPTION) {
          log.write(translate("geneGSCancel"));
          break;
          }
       }

   } }); // end of doUnitOfWork
   } catch (GedcomException e) {
   log.write(e.getMessage());
   }

    changeGedcom(gedcom);

    return true;
    // done
  }


  /**
  * ### 8 ### Generation of AGES for ADOP, CHR, DEAT, GRAD, EVEN, IMMI, NATU
  *
  * Principle 1 : only update age if birth date fully determined, unless forced
  * Principle 2 : no age for an EVEN after death (even if forced)
  *
  */
  private boolean geneAges(Gedcom gedcom, Object object) {

    // Get the options
    final SettingGeneAge settings = (SettingGeneAge)object;

    //
    // Update des Individus
    //
    log.write(translate("geneAgeUpdatedIndi") + " : ");
    List entities = new ArrayList(gedcom.getEntities(Gedcom.INDI));
    Collections.sort(entities, sortEntities);
    final List listEntities = new ArrayList(entities);
    final Gedcom gedFile = gedcom;
    try {
      gedcom.doUnitOfWork(new UnitOfWork() { 
         public void perform(Gedcom gedcom) throws GedcomException { 
         Indi indi;
         List propsToChange = new ArrayList();
         for (Iterator it = listEntities.iterator(); it.hasNext();) {
            indi = (Indi) it.next();
            // Determine if birth date is fully determined and return otherwise
            if (!settings.geneForce && !preciseBirth(indi)) continue;
            updateAge(indi, "ADOP", settings.geneForce, settings.geneRefresh);
            updateAge(indi, "CHR", settings.geneForce, settings.geneRefresh);
            updateAge(indi, "DEAT", settings.geneForce, settings.geneRefresh);
            updateAge(indi, "GRAD", settings.geneForce, settings.geneRefresh);
            updateAge(indi, "EVEN", settings.geneForce, settings.geneRefresh);
            updateAge(indi, "IMMI", settings.geneForce, settings.geneRefresh);
            updateAge(indi, "NATU", settings.geneForce, settings.geneRefresh);
            }
         } 
      }); // end of doUnitOfWork
    } catch (GedcomException e) {
    log.write(e.getMessage());
    }

    //
    // Update des Familles
    //
    log.write(" ");
    log.write(" ");
    log.write(" ");
    log.write(translate("geneAgeUpdatedFam") + " : ");
    entities = new ArrayList(gedcom.getEntities(Gedcom.FAM));
    Collections.sort(entities, sortEntities);
    final List listFamilies = new ArrayList(entities);
    try {
      gedcom.doUnitOfWork(new UnitOfWork() { 
         public void perform(Gedcom gedcom) throws GedcomException { 
         Fam fam;
         List propsToChange = new ArrayList();
         for (Iterator it = listFamilies.iterator(); it.hasNext();) {
            fam = (Fam) it.next();
            updateAge(fam, "MARR", settings.geneForce, settings.geneRefresh);
            updateAge(fam, "MARS", settings.geneForce, settings.geneRefresh);
            updateAge(fam, "DIV", settings.geneForce, settings.geneRefresh);
            updateAge(fam, "DIVF", settings.geneForce, settings.geneRefresh);
            updateAge(fam, "ENGA", settings.geneForce, settings.geneRefresh);
            updateAge(fam, "EVEN", settings.geneForce, settings.geneRefresh);
            }
         } 
      }); // end of doUnitOfWork
   } catch (GedcomException e) {
   log.write(e.getMessage());
   }

    changeGedcom(gedcom);

    log.write(" ");
    log.write(" ");
    return true;
    // done
  }


  /**
  * ### 9 ### Import Gedcom
  *
  * Supported origins: Heredis
  *
  */
  private boolean importGedcom(Gedcom gedcom, Object object) {

    // Strings
    final String TYPE_HEREDIS  = "HEREDIS";
    final String TYPE_GENEATIC = "GENEATIC";
    final String TYPE_GENJ     = "GENJ";
    final String TYPE_GENEALOGIEDOTCOM     = "GENEALOGIE.COM";

    // Get the options
    final SettingImportGedcom settings = (SettingImportGedcom)object;
    log.write(translate("importGedcom"));

    // Get gedcom file from user
    File file = null;
    file = getFileFromUser(translate("importFile"), Action2.TXT_OK);
    if (file == null) return false;

    // Read header to identify import type
    String typeStr = getImportType(file);
    log.write(translate("importFileType", typeStr));

    Importer ig = null;
    // Initiate importing class and execute it depending on header
    if (typeStr.indexOf(TYPE_HEREDIS) > -1) {
    	ig = new ImportHeredis(this, file);
       }
    else if (typeStr.indexOf(TYPE_GENJ) > -1) {
        getOptionFromUser(translate("importFileType", typeStr)+". "+translate("importNotNecessary"), OPTION_OK);
        return true;
    } else if (typeStr.indexOf(TYPE_GENEALOGIEDOTCOM) > -1) {
    	ig = new ImportGenealogieDotCom(this, file);
       }else {
        ig = new ImportGeneric(this, file);
       }
    if (ig==null) {
        getOptionFromUser(translate("importFileType", typeStr)+". "+translate("importNotAvailableYet"), OPTION_OK);
       	return false; 
    } else {
        // Ask confirmation to user that we got the right detection, abort otherwise
        if (!getOptionFromUser(translate("importFileType", typeStr)+". "+translate("importConfirm",ig.getClass().getSimpleName()), OPTION_OKCANCEL)) return false; 
        ig.run();
        log.write(translate("importingDone", ig.getOutputName()));
    }
    log.write(" ");
    log.write(" ");

    return true;
    // done
  }


 /**
  * Read gedcom header to identify SOUR approved id of created file 
  */
  private String getImportType(File file) {
    String typeStr = translate("importTypeUnknown");
    if (file == null) return typeStr;

    FileInputStream from = null;
    try {
      from = new FileInputStream(file);
      byte[] buffer = new byte[256];         // A buffer big enough to hold the header
      int bytes_read;
      bytes_read = from.read(buffer);        // buffer holds header lines
      String header = new String(buffer);
      int posStart = header.indexOf("1 SOUR") + 7;
      int posEnd = header.indexOf("2", posStart) - 1;
      typeStr = header.substring(posStart, posEnd);
      // Always close the streams, even if exceptions were thrown
      } catch (Exception e) {
        e.printStackTrace();
        log.write(translate("importUnknownError"));
      }
      finally {
        if (from != null) try { from.close(); } catch (IOException e) { ; }
      }
    return typeStr;
    }



 /**
  * Set Property position 
  */
   private int setPropertyPosition(Property prop) {
    if (prop == null) return 1;
    Property pName = prop.getProperty("NAME");
    if (pName == null) return 1;
    return prop.getPropertyPosition(pName)+1;
    }


 /**
  * Updates age tags for an individual
  */
  private void updateAge(Indi indi, String eventTag, boolean force, boolean refresh) {

    boolean preciseDate = false;
    boolean afterDeath = false;
    Property evenProp = evenProp = indi.getPropertyByPath("INDI:"+eventTag);
    if (evenProp != null) {
       Property prop = indi.getPropertyByPath("INDI:"+eventTag+":DATE");
       if (prop != null && prop instanceof PropertyDate) {
           PropertyDate pDate = (PropertyDate) prop;
           preciseDate = preciseDate(pDate);
           PropertyDate deathDate = indi.getDeathDate();
           if (deathDate != null && deathDate.isValid() && pDate.compareTo(deathDate) > 0) afterDeath = true;
           }
       PropertyAge ageProp = (PropertyAge) indi.getPropertyByPath("INDI:"+eventTag+":AGE");
       updateAge(indi, evenProp, ageProp, eventTag, force, refresh, preciseDate, afterDeath);
       }

    return;
    }

 /**
  * Updates age tags for a family
  */
  private void updateAge(Fam fam, String eventTag, boolean force, boolean refresh) {

    Indi husband = fam.getHusband();
    Indi wife = fam.getWife();
    boolean preciseDate = false;
    boolean afterDeathHusband = false;
    boolean afterDeathWife = false;
    Property evenProp = evenProp = fam.getPropertyByPath("FAM:"+eventTag);
    String msg = "";
    boolean famChanged = false;
    if (evenProp != null) {
       Property prop = fam.getPropertyByPath("FAM:"+eventTag+":DATE");
       if (prop != null && prop instanceof PropertyDate) {
           PropertyDate pDate = (PropertyDate) prop;
           preciseDate = preciseDate(pDate);
           PropertyDate deathDateHusband = husband == null ? null : husband.getDeathDate();
           if (deathDateHusband != null && deathDateHusband.isValid() && pDate.compareTo(deathDateHusband) > 0) afterDeathHusband = true;
           PropertyDate deathDateWife = wife == null ? null : wife.getDeathDate();
           if (deathDateWife != null && deathDateWife.isValid() && pDate.compareTo(deathDateWife) > 0) afterDeathWife = true;
           }
       // update age for husband
       if (husband != null && ((preciseBirth(husband) && preciseDate && !afterDeathHusband) || (force && !afterDeathHusband))) {
          Property pHusb = fam.getPropertyByPath("FAM:"+eventTag+":HUSB");
          if (pHusb == null) { 
             pHusb = evenProp.addProperty("HUSB", "");
             log.write("  " + translate("geneEventAddingHusb"));
             famChanged = true;
             }
          PropertyAge ageProp = (PropertyAge) pHusb.getProperty("AGE");
          famChanged |= updateAge(husband, pHusb, ageProp, eventTag, force, refresh, preciseDate, afterDeathHusband);
          }
       // update age for wife
       if (wife != null && ((preciseBirth(wife) && preciseDate && !afterDeathWife) || (force && !afterDeathWife))) {
          Property pWife = fam.getPropertyByPath("FAM:"+eventTag+":WIFE");
          if (pWife == null) { 
             pWife = evenProp.addProperty("WIFE", "");
             log.write("  " + translate("geneEventAddingWife"));
             famChanged = true;
             }
          PropertyAge ageProp = (PropertyAge) pWife.getProperty("AGE");
          famChanged |= updateAge(wife, pWife, ageProp, eventTag, force, refresh, preciseDate, afterDeathWife);
          }
       if (famChanged) {
          log.write("      " + translate("geneEventFam") + " " + fam.toString() + " - " + eventTag + ":");
          log.write(" ");
          }
       }

    return;
    }

 /**
  * Updates age tag
  */
  private boolean updateAge(Indi indi, Property prop, PropertyAge ageProp, String eventTag, boolean force, boolean refresh, boolean preciseDate, boolean afterDeath) {

    boolean ret = false;
    if (!force && (!preciseDate || !preciseBirth(indi))) { 
       return false;
       }
    if (afterDeath) { 
       log.write("   " + indi.toString() + " - " + eventTag + " " + translate("geneEventAfterDeath"));
       ret = true;
       }
    if (ageProp == null && !afterDeath) { 
       ageProp = (PropertyAge) prop.addProperty("AGE", "0d");
       ageProp.updateAge();
       if (!force && (ageProp.getValue().toString().compareTo("0d") == 0)) { // remove AGE if nul and not force
          Property parent = ageProp.getParent();
          parent.delProperty(ageProp);
          Property grandParent = parent.getParent();
          if (grandParent != null && (parent.getTag().equals("HUSB") || parent.getTag().equals("WIFE"))) { // remove parent HUSB or WIFE 
             grandParent.delProperty(parent);
             log.write("   " + indi.toString() + " - " + eventTag + " - " + translate("geneEventRemovingAge"));
             }
          ret = false;
          }
       else {
          log.write("   " + indi.toString() + " - " + eventTag + " - " + translate("geneEventAddingAge"));
          ret = true;
          }
       }
    else if (ageProp != null && (force || refresh)) {
       String ageBefore = ageProp.getValue();
       if (!afterDeath) {
          ageProp.updateAge();
          }
       else { 
          ageProp.setValue("0y");
          }
       String ageAfter = ageProp.getValue();
       if (ageAfter.compareTo(ageBefore) != 0) {
          log.write("   " + indi.toString() + " - " + eventTag + " " + translate("geneAgeForcedFrom") + ageBefore + " " + translate("geneAgeForcedTo") + " " + ageProp.getValue());
          ret = true;
          }
       }
    return ret;
    }


 /**
  * Calculate is string contains bits of the parameter string
  */
  private boolean isToBeExcluded(String surname, String firstname, String parameter) {

    String bits[] = parameter.split(";");
    for (int i = 0; i < bits.length ; i++) {
       if (bits[i].trim().length() == 0) {
          if (surname.trim().length() == 0 || firstname.trim().length() == 0 ) return true;
          else continue;
          }
       if (surname.indexOf(bits[i]) > -1 || firstname.indexOf(bits[i]) > -1) return true;
       }
    return false;
    }


 /**************************************************************************************************
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

        n1 = extractNumber(id1);
        n2 = extractNumber(id2);

        return (n1 - n2);
        }
     };


  /**
   * Extract the first number bit in the string going from left to right
   */
  static private int extractNumber(String str) {

     int start = 0, end = 0;
     while (start<=end&&!Character.isDigit(str.charAt(start))) start++;
     end = start;
     while ((end<=str.length()-1)&&Character.isDigit(str.charAt(end))) end++;
     if (end == start) return 0;
     else return Integer.parseInt(str.substring(start, end));
     }


  private String[] validatePlaceFormat(String placeFormat) {
    String[] tags = null;
    if (placeFormat.length() == 0) {
       log.write("   "+translate("noPlaceFormat"));
       }
    else {
       log.write("   "+translate("placeFormatToValidate"));
       log.write("   "+placeFormat);
       tags = placeFormat.split("\\,", -1);

       for (int i=0; i < tags.length; i++) {
          if (tags[i].trim().length() == 0) {
             log.write("   "+translate("placeMissingTagMsg")+" ("+(i+1)+")");
             tags[i] = translate("placeMissingTag")+"_"+(i+1);
             }
          else {
             tags[i] = tags[i].trim();
             }
          }
       }
    return tags;
    }

  private String convertPlaceFormat(String[] tags) {
    String newPlaceStr = "";
    if (tags == null) return "";
    for (int i = 0; i < tags.length; i++) 
       newPlaceStr += tags[i].trim()+",";
    newPlaceStr = newPlaceStr.substring(0,newPlaceStr.length()-1);
    return newPlaceStr;
    }

  private boolean isEmpty(String[] tags) {
    if (tags == null) return true;
    for (int i = 0; i < tags.length; i++) 
       if (tags[i].trim().length() > 0) return false;
    return true;
    }

  private boolean[] getFlagsFromUser(boolean existPlaceInHeader, boolean existPlaceTags, String msg) {

    String todos[] = {
       translate("place_assess"),
       translate("place_add"),
       translate("place_remap"),
       translate("place_realign"),
       translate("place_remove")
       };

    ArrayList choices = new ArrayList((Collection)Arrays.asList(todos)); 
    if (existPlaceInHeader && existPlaceTags) {
       choices.remove(1);
       }
    if (existPlaceInHeader && !existPlaceTags) {
       choices.remove(3);
       choices.remove(2);
       }
    if (!existPlaceInHeader && existPlaceTags) {
       choices.remove(4);
       choices.remove(3);
       choices.remove(1);
       }
    if (!existPlaceInHeader && !existPlaceTags) {
       choices.remove(4);
       choices.remove(3);
       choices.remove(2);
       }

    String todo = (String)getValueFromUser(msg, choices.toArray(), choices.get(0));

    //                   ask&add new,  ask mapping, remap,   remove
    boolean [] flags = { false,        false,       false,   false };

    if (todo == todos[1]) { // add
       flags[0] = true;
       }
    if (todo == todos[2]) { // remap
       flags[0] = true;
       flags[1] = true;
       flags[2] = true;
       }
    if (todo == todos[3]) { // realign
       flags[2] = true;
       }
    if (todo == todos[4]) { // remove
       flags[3] = true;
       }

    return flags;
    }


  private boolean changeGedcom(Gedcom gedcom) {

    final Gedcom uowGedcom = gedcom;
    try {
       gedcom.doUnitOfWork(new UnitOfWork() { public void perform(Gedcom gedcom) throws GedcomException { 
       Entity entity = uowGedcom.createEntity(Gedcom.INDI, uowGedcom.getNextAvailableID(Gedcom.INDI));
       uowGedcom.deleteEntity(entity); 
       } }); // end of doUnitOfWork
       } catch (GedcomException e) {
       log.write(e.getMessage());
       }
    return true;
    }


 /**
  * Open Gedcom file
  */
@SuppressWarnings("deprecation")
  private Gedcom openGedcomFile(String filepath) {
        try {
            return Workbench.getInstance().openGedcom((new File(filepath)).toURL()).getGedcom();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ReportToolBox.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
            //    // Variables
            //    Gedcom gedcomX = null;
            //    Origin originX = null;
            //    GedcomReader readerX;
            //
            //    // Create pointer to file
            //    try {
            //       originX = Origin.create(new URL("file", "", filepath));
            //       } catch (MalformedURLException e) {
            //       log.write("URLexception:"+e);
            //       return null;
            //       }
            //
            //    // Create reader to pointer
            //    try {
            //       readerX = new GedcomReader(originX);
            //      } catch (IOException e) {
            //       log.write("IOexception:"+e);
            //       return null;
            //      }
            //
            //    // Build-up gedcom from reader
            //    try {
            //      gedcomX = readerX.read();
            //      } catch (GedcomIOException e) {
            //      log.write("GedcomIOexception:"+e);
            //      log.write("File:"+filepath);
            //      log.write("At line:"+e.getLine());
            //      return null;
            //      }
            //
            //    // Display warnings if any
            //    log.write("   "+translate("LinesRead")+": "+readerX.getLines());
            //    List warnings = readerX.getWarnings();
            //    log.write("   "+translate("Warnings")+": "+warnings.size());
            //    for (Iterator it = warnings.iterator(); it.hasNext();) {
            //       String wng = (String)it.next().toString();
            //       log.write("   "+wng);
            //       } // end loop
            //
            //    // Link entities
            //    linkGedcom(gedcomX);
            //    return gedcomX;
            //    return gedcomX;
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
               log.write("Linking:GedcomException:"+e);
               log.write("Warning at Linking: "+e);
               return false;
             }
          }
       }
    return true;   
    }   
    
 /**
  * Save Gedcom file
  */
  private boolean saveGedcom(Gedcom gedcomX) {
      return Workbench.getInstance().saveGedcom(new Context(gedcomX));
    }


 /**
  * Generates the d'Aboville numbering from given individual prefixed with sosa number from where it originates
  */
  private int toolGeneDabo(Indi indiHead, String tagStr, String sosaHead) {

    int sosaDabo = 0;
    List daboList = new ArrayList();
    daboList.add(new Pair2(indiHead.getId(), "1"));
    Pair2 pair = new Pair2("","");
    String indiID = "";
    Indi indi;
    // for each pair in the list:
    //   - get Id and number
    //   - get individual corresponding to the id
    //   - set daboCounter at the pair value 
    //   - get the families of the individual
    //   - if more than one family, suffix is a, b, c, etc else ""
    //   - for each family, get the kids
    //      - for each kid of the family, if already tags as sosa, skip, otherwise
    //         - assign him sosa nb made like:  sosaHead + "-" + counter where counter is daboCounter + suffix + kidnb
    //         - add him to the list with this counter
    try {
    for (ListIterator listIter = daboList.listIterator(); listIter.hasNext();) {
       pair = (Pair2) listIter.next();
       indiID = pair.ID;
       indi = (Indi)indiHead.getGedcom().getEntity(indiID);
       String daboCounter = pair.dabo;
       Fam[] families = indi.getFamiliesWhereSpouse();
       if (families == null || families.length == 0) continue;
       Character suffix = 'a';
       for (int f=0 ; f < families.length; f++) {
          Fam family = families[f];
          Indi[] kids = family.getChildren();
          if (kids == null || kids.length == 0) continue;
          for (int k=0 ; k < kids.length; k++) {
             Indi kid = kids[k];
             if (kid.getProperty("_SOSA") != null) continue;
             String counter = daboCounter + (families.length > 1 ? suffix.toString() : "") + (k+1);
             kid.addProperty(tagStr, sosaHead + "-" + counter, setPropertyPosition(kid));
             listIter.add(new Pair2(kid.getId(), counter));
             listIter.previous();
             sosaDabo++;
             }
          suffix++;
          }
       }
       } catch (GedcomException e) {
       log.write(e.getMessage());
       }

    return sosaDabo;
    }


 /**
  * Check if birth date is precise
  */
  private boolean preciseBirth(Indi indi) {
     if (indi == null) return false;
     return preciseDate(indi.getBirthDate());
     }

 /**
  * Check if date is precise
  */
  private boolean preciseDate(PropertyDate pDate) {
     if (pDate == null || !pDate.isValid() || pDate.isRange()) return false;
     PropertyDate.Format pf = pDate.getFormat();
     if (pf != PropertyDate.DATE) return false;
     PointInTime pit = pDate.getStart();
     if (!pit.isValid() || !pit.isComplete()) return false;
     return true;
     }




  /** ******************************************************************************
  * Class used by re-generation of sosa numbers to store sosa pairs
  */
  private class Pair {
     String ID = "";
     int    sosa = 0; 

     public Pair(String ID, int sosa) {
        this.ID = ID;
        this.sosa = sosa;
        }
  }

  private class Pair2 {
     String ID = "";
     String dabo = "";

     public Pair2(String ID, String dabo) {
        this.ID = ID;
        this.dabo = dabo;
        }
  }

  /**
  * Class used for the tool options
  */
  // Generation of IDs
  public class SettingIDs {
     public int entToDo = ENT_INDI;
     public String entToDos[] = { 
        translate("SettingIDs.All"),
        translate("SettingIDs.INDI"),
        translate("SettingIDs.FAM"),
        translate("SettingIDs.NOTE"),
        translate("SettingIDs.SOUR"),
        translate("SettingIDs.SUBM"),
        translate("SettingIDs.REPO") 
        };
     public String prefixID = "";
     public String suffixID = "";
     public int paddingId = 0;
     }

  // Generation of Sosas
  public class SettingSosas {
     public String sosaTag = "_SOSA";
     public boolean daboville = false;
     public String olderSign = "+";
     public String youngerSign = "-";
     public int paddingSize = 8;
     }

  // Generation of TreeTops
  public class SettingTreeTops {
     public String treeTopTag = "_TREETOP";
     public String treeTopValue = translate("Find_parents");
     public boolean DisplayIndi = false;
     }

  // Remove Tags
  public class SettingRmTags {
     public String removeTag = "_XXXX";
     public int entToDo = ENT_INDI;
     public String entToDos[] = { 
        translate("SettingIDs.All"),
        translate("SettingIDs.INDI"),
        translate("SettingIDs.FAM"),
        translate("SettingIDs.NOTE"),
        translate("SettingIDs.SOUR"),
        translate("SettingIDs.SUBM"),
        translate("SettingIDs.REPO") 
        };
     }

  // Manage PLAC tags
  public class SettingMngPlaceTags {
     public boolean emptyTagsAllowed = false;
     }

  // Remove Tags
  public class SettingMngAssoTags {
     public int action = 1;
     public String actions[] = { 
        translate("SettingMngAssoTags.test"),
        translate("SettingMngAssoTags.remove"),
        translate("SettingMngAssoTags.restore")
        };
     }

  // Gene Surn and Givn
  public class SettingGeneGivnSurn {
     public boolean geneAuto = false;
     public String geneExclude = "anonyme; ;?;";
     }

  // Gene Surn and Givn
  public class SettingGeneAge {
     public boolean geneForce = false;
     public boolean geneRefresh = true;
     }


  // Import Gedcom 
  public class SettingImportGedcom {
     private int nothingyet = 0;
     }

} // End_of_Report
 