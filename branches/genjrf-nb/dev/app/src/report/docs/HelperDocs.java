/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package docs;

import docs.panels.*;

import genj.gedcom.*;
import genj.gedcom.time.*;
import genj.util.Registry;
import genj.util.swing.*;
import java.util.List;
import java.util.*;
import genj.window.WindowManager;

import java.text.DecimalFormat;

import javax.swing.*;

/**
 * The base class for our helpers
 */
public class HelperDocs {


  /**
   * Variables
   */
  static final String SOSATAG = "_SOSA";

  public static int MENU_DOCS   = 0;
  public static int BIRT_EDITOR = 1;
  public static int MARR_EDITOR = 2;
  public static int DEAT_EDITOR = 3;
  public static int STOR_EDITOR = 4;

  public static String MENU_STRING = "10000";
  public static String BIRT_STRING = "01000";
  public static String MARR_STRING = "00100";
  public static String DEAT_STRING = "00010";
  public static String STOR_STRING = "00001";


  /**
   * Message about form
   */
  static public boolean msg(EditDocsPanel view, String title, String msg, boolean error) {
     JOptionPane.showMessageDialog(view, msg, title, error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
     return !error;
     }

  static public boolean userConfirms(EditDocsPanel view, String title, String msg) {
     return (JOptionPane.showConfirmDialog(view, msg, title, JOptionPane.YES_NO_OPTION) == 0);
     }


  /**
   * Process the OK button
   */
  static public boolean processOk(Gedcom gedcom, EditDocsPanel view, Registry registry, int nextPanel) {
      return processOk(gedcom, view, registry, nextPanel, false);
      }

  static public boolean processOk(Gedcom gedcom, EditDocsPanel view, Registry registry, int nextPanel, boolean autosave) {

    boolean ret = true;

    final boolean confirm = registry.get("confirm", "0").equals("1");
    final EditDocsPanel viewFinal = view;
    final EditorDocs panelFinal = view.getCurrentEditorDocsPanel();

    String question = autosave ? "<br><font style='font-weight: normal'>(" + view.translate("autosaveIsOn")+")</font><br><br>" : "";

    if (!confirm || userConfirms(view, view.translate("updateAsked"), "<html>" + view.translate("confirmSave") + question)) {
       try {
          gedcom.doUnitOfWork(new UnitOfWork()
             { public void perform(Gedcom gedcom) throws GedcomException {
                  Entity newEnt = panelFinal.updateGedcom();
                  if (confirm) msg(viewFinal, viewFinal.translate("Saving"), viewFinal.translate("SuccessfulSave"), false);
                  viewFinal.setEntity(newEnt);
                  }
             }); // end of doUnitOfWork
          viewFinal.resetForm(nextPanel);
          ret = true;
          } catch (Exception e) {
             if (registry.get("debug", "0").equals("1")) e.printStackTrace();
             String title = view.translate("impossibleSave");
             String msg = "<html>" + view.translate("error") + ": <br><br><center><b><font color=#ff0000>"+e.getMessage() + "</font></b></center><br>";
             if (autosave) {
                msg += view.translate("cancelSave") + (confirm ? "" : question);
                ret = userConfirms(view, title, msg);
                }
             else {
                msg(view, title, msg, true);
                ret = false;
                }
             if (gedcom.hasChanged()) gedcom.undoUnitOfWork();
          }
       }
    return ret;
    }


  /**
   * Process the Cancel button
   */
  static public boolean processCancel(Gedcom gedcom, EditDocsPanel view, Registry registry, int nextPanel) {

    boolean ret = false;
    boolean confirm = registry.get("confirm", "0").equals("1");

    if (!confirm || userConfirms(view, view.translate("eraseAsked"), view.translate("confirmErase"))) {
       view.setEntity(view.getMainEntity());
       view.resetForm(nextPanel);
       }
    return ret;
    }


  /**
   * Process the Copy button
   */
  static public boolean processCopy(Gedcom gedcom, EditDocsPanel view, Registry registry, boolean isValid, String invalidMsg, List<Entity> listEnt) {

    boolean ret = false;

    final boolean confirm = registry.get("confirm", "0").equals("1");
    final EditDocsPanel viewFinal = view;
    final Registry registryFinal = registry;

    if (!isValid) {
       msg(view, view.translate("impossibleCopy"), invalidMsg, true);
       return ret;
       }

    final CopyPanel copyPanel = new CopyPanel(gedcom, view, listEnt);
    if (JOptionPane.showConfirmDialog(view, copyPanel, view.translate("confirmCopy"), JOptionPane.OK_CANCEL_OPTION) == 0 && (copyPanel.gedcom2 != null)) {
       try {
          copyPanel.gedcom2.doUnitOfWork(new UnitOfWork()
             { public void perform(Gedcom gedcom) throws GedcomException {
                  if (copyDocument(registryFinal, copyPanel)) {
                     if (confirm) msg(viewFinal, viewFinal.translate("copyAsked"), viewFinal.translate("successfulCopy"), false);
                     }
                  }
             }); // end of doUnitOfWork
          ret = true;
          } catch (Exception e) {
             msg(view, view.translate("impossibleCopy"), view.translate("error") + " : " + e.getMessage(), true);
             if (registry.get("debug", "0").equals("1")) e.printStackTrace();
             if (copyPanel.gedcom2 != null) copyPanel.gedcom2.undoUnitOfWork();
             ret = false;
          }
       }
    return ret;
    }

  /**
   * Process the Close button
   */
  static public boolean processClose(Gedcom gedcom, EditDocsPanel view, Registry registry, WindowManager winMgr) {

    boolean ret = true;
    boolean confirm = registry.get("confirm", "0").equals("1");
    boolean autosave = registry.get("autosave", "0").equals("1");

    if (!confirm || autosave || userConfirms(view, view.translate("closeAsked"), view.translate("confirmClose"))) {
       if (autosave) {
          ret = processOk(gedcom, view, registry, view.getCurrentEditorNb(), true);
          }
       if (ret) {
          winMgr.close("window.docs");
          Registry.persist(); // stores it
          }
       }

    return ret;
    }

  /**
   * Process change of document
   */
  static public boolean processChange(Gedcom gedcom, EditDocsPanel view, Registry registry, int nextPanel) {

    boolean ret = false;
    boolean confirm = registry.get("confirm", "0").equals("1");
    boolean autosave = registry.get("autosave", "0").equals("1");

    int currentPanel = view.getCurrentEditorNb();
    EditorDocs editor = view.getCurrentEditorDocsPanel();

    if (editor == null) {
       return true;
       }

    if (nextPanel == currentPanel) {
       return false;
       }

    if (currentPanel == MENU_DOCS) {
       return true;
       }

    if ( !confirm || autosave || userConfirms(view, view.translate("swapAsked"), view.translate("confirmSwap"))) {
       ret = true;
       if (autosave) ret = processOk(gedcom, view, registry, nextPanel, true);
       }

    return ret;
    }


  /**
   * Copy document to another Gedcom
   *
   */
  static public boolean copyDocument(Registry registry, CopyPanel cPanel) throws GedcomException {

   // Copy all entities and link them if necessary
   Map<String, String> id2id = new TreeMap<String, String>();
   boolean debug = registry.get("debug", "0").equals("1");
   for (int i = 0 ; i < cPanel.matches.length ; i++) {
       if (cPanel.matches[i].refCHKD.isSelected()) {
          cPanel.matches[i].ent2 = createEntity(cPanel.matches[i].ent1, cPanel.gedcom2, debug);
          if (cPanel.matches[i].ent1 == null || cPanel.matches[i].ent2 == null) return false;
          id2id.put("@"+cPanel.matches[i].ent1.getId()+"@", "@"+cPanel.matches[i].ent2.getId()+"@");
          }
       }
   for (int i = 0 ; i < cPanel.matches.length ; i++) {
       copyEntity(cPanel.matches[i].ent1, cPanel.matches[i].ent2, id2id, debug);
       }
   linkGedcom(cPanel.gedcom2);

   // Update all SOSA (not only for indis that might have been created above but for all the ones potentially missing in the gedcom)
   calcSOSA(cPanel.gedcom2, registry);

   return true;
   }




  /**
   * Get entity from a string
   */
  static public Entity getEntity(Gedcom gedcom, String ref) {

    if (!exists(ref)) return null;
    if (ref.lastIndexOf("(") == -1 && ref.lastIndexOf(")") == -1) return null;
    return gedcom.getEntity(ref.substring(ref.lastIndexOf("(")+1, ref.lastIndexOf(")")));
    }


  /**
   * Create or update property
   */
  static public Property upcreateProperty(Property propParent, String tag, String value) {

    if (propParent == null) return null;

    Property prop = propParent.getProperty(tag);
    if (prop == null) {
       prop = propParent.addProperty(tag, value);
       }
    else {
       String existingValue = propParent.getPropertyValue(tag);
       if (!value.equals(existingValue)) {
          if (prop instanceof PropertyXRef) {
             propParent.delProperty(prop);
             prop = propParent.addProperty(tag, value);
             }
          else {
             prop.setValue(value);
             }
          }
       }

    return prop;
    }

//if (tag.equals("SOUR")) System.err.println("debugB APRES prop.toString()="+prop.toString());


  /**
   * Check non empty field
   */
  static public boolean exists(String str) {
    return (str != null && str.trim().length() != 0);
    }

  static public boolean exists(FileChooserWidget fcw) {
    return !fcw.isEmpty() && fcw.getFile() != null;
    }
  static public boolean exists(PointInTime pit) {
    return (pit != null && pit.isValid() && pit.isComplete());
    }



  /**
   * Update or create an individual
   */
  static public Indi upcreateIndi(Gedcom gedcom, boolean male, PointInTime pitDocDate, ChoiceWidget refINDI, TextFieldWidget surn, TextFieldWidget givn, TextFieldWidget age, ChoiceWidget bplac, DateWidget bdate, ChoiceWidget occu, ChoiceWidget resi, ChoiceWidget dplac, DateWidget ddate) throws GedcomException {

    Indi indi = null;
    Property prop = null;
    boolean preciseBirth = false;

    // Check if individual is to be created
    String strIndi = (refINDI != null) ? refINDI.getText() : "";
    if (!exists(strIndi)) {
       indi = (Indi)gedcom.createEntity(Gedcom.INDI);
       }
    else {
       if (strIndi.lastIndexOf("(") == -1 && strIndi.lastIndexOf(")") == -1) throw new GedcomException("Unknown id "+strIndi);
       String id = strIndi.substring(strIndi.lastIndexOf("(")+1, strIndi.lastIndexOf(")"));
       indi = (Indi)gedcom.getEntity(id);
       }

    // Sex
    upcreateProperty(indi, "SEX", male ? "M" : "F");

    // Name, Givn and Surn
    String name = "";
    if (exists(givn.getText())) name += givn.getText();
    if (exists(surn.getText())) name += (name.length() > 0 ? " " : "") + "/" + surn.getText() + "/";
    if (exists(name)) {
       prop = upcreateProperty(indi, "NAME", name);
       upcreateProperty(prop, "GIVN", givn.getText());
       upcreateProperty(prop, "SURN", surn.getText());
       }

    // Birth (calculate birthdate from age if no birthdate provided)
    if ((bplac != null && exists(bplac.getText())) || (bdate != null && exists(bdate.getValue())) || (age != null && exists(age.getText()))) {
       prop = upcreateProperty(indi, "BIRT", "");
       if (bplac != null && exists(bplac.getText())) upcreateProperty(prop, "PLAC", bplac.getText());
       if (bdate != null && exists(bdate.getValue())) {
          upcreateProperty(prop, "DATE", bdate.getValue().getValue());
          preciseBirth = true;
          }
       else if (exists(age.getText())) {
          String birthdate = calcDate(age.getText(), pitDocDate);
          if (birthdate != null) {
             upcreateProperty(prop, "DATE", "CAL " + birthdate);
             preciseBirth = true;
             }
          }
       }

    // Occupation
    if (occu != null && exists(occu.getText())) {
       prop = upcreateProperty(indi, "OCCU", occu.getText());
       upcreateProperty(prop, "DATE", pitDocDate.getValue());
       }

    // Residence
    if (resi != null && exists(resi.getText())) {
       prop = upcreateProperty(indi, "RESI", "");
       upcreateProperty(prop, "PLAC", resi.getText());
       upcreateProperty(prop, "DATE", pitDocDate.getValue());
       if (age != null && exists(age.getText())) upcreateProperty(prop, "AGE", age.getText()+(age.getText().indexOf("y") == -1 ? "y" : ""));
       }

    // Death
    if (dplac != null && (exists(dplac.getText()) || exists(ddate.getValue()))) {
       prop = upcreateProperty(indi, "DEAT", "");
       if (exists(dplac.getText())) upcreateProperty(prop, "PLAC", dplac.getText());
       if (exists(ddate.getValue())) {
          upcreateProperty(prop, "DATE", ddate.getValue().getValue());
          if (preciseBirth) {
             PropertyAge pAge = (PropertyAge)upcreateProperty(prop, "AGE", "0d");
             pAge.updateAge();
             }
          }
       }

    return indi;
    }

  /**
   * Creation or Update a family made of simply a couple
   *
   */
  static public Fam upcreateFamily(Gedcom gedcom, Indi husband, Indi wife) throws GedcomException {

   if (husband == null || wife == null) return null;

   // Create or update parent's family
   Fam fam = getFamily(husband, wife);
   if (fam == null) {
      fam = (Fam)gedcom.createEntity(Gedcom.FAM);
      fam.setHusband(husband);
      fam.setWife(wife);
      }

   return fam;
   }



  /**
   * Creation or Update of parents' family
   *
   */
  static public Fam upcreateParents(Gedcom gedcom, Indi child, Indi father, Indi mother) throws GedcomException {

   if (father == null || mother == null) return null;

   if (child == null) throw new GedcomException("Child necessary to create family");

   // Check if parents correspond to child
   Fam famFound = child.getFamilyWhereBiologicalChild();
   if (famFound != null) {
       if (famFound.getHusband() != father || famFound.getWife() != mother) 
            throw new GedcomException("Child already belongs to family " + famFound.toString());
      }

   // Get parents' family or create it if does not exist
   Fam fam = getFamily(father, mother);
   if (fam == null) {
      fam = (Fam)gedcom.createEntity(Gedcom.FAM);
      fam.setHusband(father);
      fam.setWife(mother);
      }

   // Add child if child did not have parents before
   if (famFound == null) {
      PropertyXRef pxref = fam.addChild(child);
      try {
         pxref.link();
         } catch (Exception e) {
            if (e.getMessage().indexOf("Already linked") == -1) e.printStackTrace();
         }
      }

   return fam;
   }


  /**
   * Get family from two individual, if it exists
   */
  static public Fam getFamily(Indi husb, Indi wife) {

    if (husb == null || wife == null) return null;

    Fam family = null;
    Fam[] families = husb.getFamiliesWhereSpouse();
    for (int i = 0 ; i < families.length ; i++) {
       if (families[i].getWife() != wife) continue;
       family = families[i];
       break;
       }

    return family;
    }


  /**
   * Calculate birth date from an age and a current date
   */
  static public String calcDate(String age, PointInTime docDate) {

    if (age == null || docDate == null) return null;

    // Check age format (number or 99y 9m 9d)
    // split in 3 tokens, extract a number from each and assume Years, Months, Days
    String[] bits = age.split("\\s");
    int l = bits.length;
    int years = extractNumber(bits[0]);
    int months = (l>1) ? extractNumber(bits[1]) : 0;
    int days = (l>2) ? extractNumber(bits[2]) : 0;
    // Perform calculation
    Delta delta = Delta.get(new PointInTime(days, months, years), docDate);
    PointInTime birthdate = new PointInTime(delta.getDays(), delta.getMonths(), delta.getYears());
    return birthdate.getValue();
    }

  /**
   * Calculate age from a birthdate and a current date
   */
  static public String calcAge(PointInTime birth, PointInTime docDate) {

    if (birth == null || docDate == null) return null;

    Delta delta = Delta.get(birth, docDate);
    if (delta == null) return "";
    return delta.getYears()+"y " + delta.getMonths()+"m " + delta.getDays()+"d";
    }

  /**
   * Extract the first number bit in the string going from left to right
   */
  static public int extractNumber(String str) {

     int start = 0, end = 0;
     while (start<=end&&!Character.isDigit(str.charAt(start))) start++;
     end = start;
     while ((end<=str.length()-1)&&Character.isDigit(str.charAt(end))) end++;
     if (end == start) return 0;
     else return (int)Integer.parseInt(str.substring(start, end));
     }

  /**
   * Calculate SOSA for an individual
   * - Check all individuals to look for the de-cujus
   * - Start with him and go up the tree and down with daboville
   * - As we go, fill in all SOSA numbers missing
   */
  static public Indi calcSOSA(Gedcom gedcom, Registry registry) {

    // get settings
    boolean sosa = registry.get("sosa", "1").equals("1");
    boolean dabo = registry.get("dabo", "1").equals("1");
    String sosatag = registry.get("sosatag", SOSATAG);
    if (!sosa) return null;

    // Find de-cujus
    Indi decujus = null;
    int pad = 0;
    Entity[] indis = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
    for (int i = 0 ; i < indis.length ; i++) {
       Entity ent = indis[i];
       Property prop = ent.getProperty(sosatag);
       if (prop == null) continue;
       int sosaNb = extractNumber(prop.toString());
       if (sosaNb != 1) continue;
       decujus = (Indi) ent;
       pad = prop.toString().length();
       break;
       }

    // Climb the tree from decujus
    tagSosaParents(decujus, 1, new DecimalFormat("000000000000".substring(0,pad)), dabo, sosatag);

    return decujus;

    }

  /**
   * Puts recurringly the sosa or daboville tags to parents
   */
  static private void tagSosaParents(Indi indi, int sosa, DecimalFormat format, boolean dabo, String sosatag) {

    int sosaNb = 0;

    // Stop if individual does not exist
    if (indi == null) return;

    // Get father and mother and stop if does not exist
    Fam famc = indi.getFamilyWhereBiologicalChild();
    if (famc == null) return;

    // Tag father and mother if exist
    Indi father = famc.getHusband();
    upcreateProperty(father, sosatag, format.format(2*sosa));
    if (dabo) tagDaboDecendents(father, format.format(sosa)+"-1", sosatag);
    Indi mother = famc.getWife();
    upcreateProperty(mother, sosatag, format.format(2*sosa+1));

    // Recurrence on father
    tagSosaParents(father, 2*sosa, format, dabo, sosatag);

    // Recurrence on mother
    tagSosaParents(mother, 2*sosa+1, format, dabo, sosatag);

    }

  /**
   * Puts recurringly the dabo tags to decendents, skipping already tagged individuals
   */
  static private void tagDaboDecendents(Indi indi, String ancestor, String sosatag) {

    if (indi == null) return;

    // Get families
    Fam[] families = indi.getFamiliesWhereSpouse();
    if (families == null || families.length == 0) return;
    Character suffix = 'a';
    for (int f=0 ; f < families.length; f++) {
       Fam family = families[f];
       Indi[] kids = family.getChildren();
       if (kids == null || kids.length == 0) continue;
       for (int k=0 ; k < kids.length; k++) {
          Indi kid = kids[k];
          String counter = ancestor + (families.length > 1 ? suffix.toString() : "") + (k+1);
          Property p = kid.getProperty(sosatag);
          if (p == null || p.toString() == null) upcreateProperty(kid, sosatag, counter);
          tagDaboDecendents(kid, counter, sosatag);
          }
       suffix++;
       }
    }

 /**
  * Create an entity in another gedcom
  */
  static public Entity createEntity(Entity ent1, Gedcom gedcom2, boolean debug) throws GedcomException {

    if (ent1 == null) return null;

    Entity ent2 = gedcom2.createEntity(ent1.getTag());
    if (ent2 == null) {
       if (debug) System.err.println("Error creating entity "+ent1.toString()+" in gedcom "+gedcom2.toString());
       return null;
       }
    return ent2;
    }

 /**
  * Copy some gedcom entities to another gedcom
  * Existing properties are overwritten
  */
  static public void copyEntity(Entity ent1, Entity ent2, Map id2id, boolean debug) throws GedcomException {

    if (ent1 == null) return;

    Property prop2 = ent2;
    List<Property> props = new ArrayList();
    copyPropertiesRecursively(ent1, ent2, props, id2id);

    return;
    }

 /**
  * Copy an entity from one gedcom to another one
  * If a property already exists in the recurring loop, create another one (case where two tags coexist at the same level) 
  */
  static private void copyPropertiesRecursively(Property prop1, Property prop2, List props, Map id2id) {

   if (prop1 == null) return;

   Property[] children1 = prop1.getProperties();
   for (int i = 0 ; i < children1.length ; i++) {
      String tag = children1[i].getTag();
      String value = children1[i].getValue();
      if (tag.equals("XREF") || tag.equals("CHAN")) continue; // skip these tags, no need
      if (value.indexOf("@") == 0) { // change pointers to newly created entities
         value = (String)id2id.get(value);
         if (value == null) continue; // referenced entity is not part of the set of entities to be copied
         }
      Property child2 = prop2.getProperty(tag);
      if (child2 == null || props.contains(child2)) {
         child2 = prop2.addProperty(tag, value);
         }
      else {
         child2.setValue(value);
         props.add(child2);
         }
      copyPropertiesRecursively(children1[i], child2, props, id2id);
      }
   }

 /**
  * Links Gedcom XReferences
  */
  static public boolean linkGedcom(Gedcom gedcomX) {
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
               return false;
             }
          }
       }
    return true;
    }



} //Helper


