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
package genjreports.docs;

import genj.gedcom.*;
import genj.util.Registry;
import genj.util.swing.*;
import genj.gedcom.time.PointInTime;
import genjreports.docs.panels.ActPanel;
import genjreports.docs.panels.ContractPanel;
import genjreports.docs.panels.DatePanel;
import genjreports.docs.panels.DocsListener;
import genjreports.docs.panels.IndiMarrPanel;
import genjreports.docs.panels.WitnessesPanel;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.*;

import javax.swing.*;


/**
 * The editor for entering a marriage document
 */
class MarriageDocsEditor extends EditorDocs implements DocsListener {

  /** our gedcom */
  private Gedcom gedcom = null;
  private Entity mainEntity = null;

  /** registry and other data elements */
  private Registry registry;
  private DataSet dataSet;

  /** edit */
  private EditDocsPanel view;
  private EditorDocs parent;

  /** actions */
  private Action2 ok, cancel, copy;

  /** current panels */
  private JPanel contentPanel;

  /** other panels */
  private DatePanel datePanel;
  private IndiMarrPanel husbPanel;
  private IndiMarrPanel wifePanel;
  private ContractPanel contractPanel;
  private WitnessesPanel witnessesPanel;
  private ActPanel actPanel;

  /** debugging */
  private boolean debug = false;

  /** number of witnesses for a marriage */
  private int NB_WIT = 4;


  /**
   * Initialisation of panel
   */
  public void init(Gedcom gedcom, Entity entity, EditDocsPanel edit, Registry registry, DataSet dataSet) {

    // remember
    this.gedcom = gedcom;
    this.mainEntity = entity;
    this.view = edit;
    this.registry = registry;
    this.dataSet = dataSet;
    parent = this;

    // make user focus root
    ok = new OK();
    cancel = new Cancel();
    copy = new Copy();
    setFocusCycleRoot(true);

    // Align things to the left
    setLayout(new FlowLayout(FlowLayout.LEFT));

    // create panel for main content of docs and fields in editor
    contentPanel = new JPanel();
    populatePanel(contentPanel);
    add(contentPanel);

    // sets initial family
    datePanel.setEntity(mainEntity);

    // Set debug mode
    debug = registry.get("debug", "0").equals("1");

    // done
  }

  /**
   * Get Title
   */
  public String getTitle() {
     return view.translate("MarrDocsUpper");
  }

  public String getSmallTitle() {
     return view.translate("MarrDocsLower");
  }

  /**
   * Get Title
   */
  public void setEntity(Entity entity) {
     this.mainEntity = entity;
     datePanel.setEntity(entity);
     return;
  }



  /**
   * A ok action
   */
  private class OK extends Action2 {

    /** constructor */
    private OK() {
      setText(view.translate("OK"));
      setTip(view.translate("UpdateGedcom"));
      }

    /** cancel current proxy */
    protected void execute() {
      HelperDocs.processOk(gedcom, view, registry, HelperDocs.MARR_EDITOR);
      }
    }

  /**
   * A copy action
   */
  private class Copy extends Action2 {

    /** constructor */
    private Copy() {
      setText(view.translate("COPY"));
      setTip(view.translate("CopyDocs"));
    }

    /** cancel current proxy */
    protected void execute() {
      final CopiedDocument cDoc = new CopiedDocument();
      HelperDocs.processCopy(gedcom, view, registry, cDoc.valid, cDoc.invalidMsg, cDoc.listEnt);
    }
  }

  /**
   * A cancel action
   */
  private class Cancel extends Action2 {

    /** constructor */
    private Cancel() {
      setText(view.translate("ERASE"));
      setTip(view.translate("EraseDocs"));
      }

    /** cancel current proxy */
    protected void execute() {
      HelperDocs.processCancel(gedcom, view, registry, HelperDocs.MARR_EDITOR);
      }
  }




  /**
   * Manages translations for all downstreams panels
   */
  public String translate(String key) {
     return view.translate(key);
     }
  public String translate(String key, Object value) {
     return view.translate(key, value);
     }



  /**
   * Populate panel with marriage document format
   */
  public void populatePanel(JPanel panel) {

    // define main panels
    datePanel       = new DatePanel(gedcom, this, dataSet, "FAM:MARR");
    husbPanel       = new IndiMarrPanel(gedcom, this, true, dataSet);
    wifePanel       = new IndiMarrPanel(gedcom, this, false, dataSet);
    contractPanel   = new ContractPanel(this, dataSet);
    witnessesPanel  = new WitnessesPanel(gedcom, this, NB_WIT, dataSet, "FAM:MARR", view.translate("Witnesses"));
    actPanel        = new ActPanel(gedcom, this, dataSet, registry, "FAM:MARR", getSmallTitle());

    // define buttons panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    ButtonHelper bh = new ButtonHelper().setInsets(5).setContainer(buttonPanel);
    bh.create(ok).setFocusable(false);
    bh.create(copy).setFocusable(false);
    bh.create(cancel).setFocusable(false);

    // set grid
    panel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5,0,0,0);

    // get display mode (tabbed or not)
    boolean tabbed = registry.get("usetabs", "1").equals("1");

    if (!tabbed) {
       // position panels
       c.gridx = 0; c.gridy = 0;
       panel.add(datePanel, c);

       c.insets = new Insets(15,0,0,0);
       c.gridx = 0; c.gridy = 1;
       panel.add(husbPanel, c);

       c.gridx = 0; c.gridy = 2;
       panel.add(wifePanel, c);

       c.gridx = 0; c.gridy = 3;
       panel.add(contractPanel, c);

       c.gridx = 0; c.gridy = 4;
       panel.add(witnessesPanel, c);

       c.gridx = 0; c.gridy = 5;
       panel.add(actPanel, c);
       }
    else {
       // Create tabbed pane
       JTabbedPane tabbedPane = new JTabbedPane();
       FlowLayout fl = new FlowLayout(FlowLayout.LEFT);

       JPanel jp1 = new JPanel(fl);
       jp1.add(datePanel);
       tabbedPane.add(view.translate("Date"), jp1);

       JPanel jp2 = new JPanel(fl);
       jp2.add(husbPanel);
       tabbedPane.add(view.translate("Groom"), jp2);

       JPanel jp3 = new JPanel(fl);
       jp3.add(wifePanel);
       tabbedPane.add(view.translate("Bride"), jp3);

       JPanel jp4 = new JPanel(fl);
       jp4.add(contractPanel);
       tabbedPane.add(view.translate("Contract"), jp4);

       JPanel jp5 = new JPanel(fl);
       jp5.add(witnessesPanel);
       tabbedPane.add(view.translate("Witnesses"), jp5);

       JPanel jp6 = new JPanel(fl);
       jp6.add(actPanel);
       tabbedPane.add(view.translate("SourSto"), jp6);

       // position panels
       c.gridx = 0; c.gridy = 0;
       panel.add(tabbedPane, c);
       }

    c.weighty = 1.0;   //request any extra vertical space
    c.anchor = GridBagConstraints.PAGE_END; //bottom of space
    c.insets = new Insets(20,0,0,0);  //top padding
    c.gridx = 0; c.gridy = 6;       //third row
    panel.add(buttonPanel, c);

    }

  /**
   * Update Gedcom
   *
   * Will be done all or nothing so can just grab the information in the form and update/reject on the fly 
   */
  public Entity updateGedcom() throws GedcomException {


   // Date and place of event are mandatory (store in pDocDate and strDocPlace)
   PointInTime pitDocDate = datePanel.getDate();
   if (pitDocDate == null) throw new GedcomException(view.translate("msg_dateMissing"));
   String strDocPlace = datePanel.getPlace();
   if (strDocPlace == null) throw new GedcomException(view.translate("msg_locationMissing"));

   // Husband and Wife are mandatory
   if (!husbPanel.isOK(1)) throw new GedcomException(view.translate("msg_groomMissing"));
   if (!wifePanel.isOK(1)) throw new GedcomException(view.translate("msg_brideMissing"));


   // Update or Create husband (always)
   Indi husband = husbPanel.upcreateIndi(true, pitDocDate, 1);

   // Update or Create wife (always)
   Indi wife = wifePanel.upcreateIndi(false, pitDocDate, 1);

   // Update or Create family with event place, date and agent (always)
   Fam fam = upcreateFamily(husband, wife, pitDocDate, strDocPlace, datePanel.refAGNCD.getText());

   // Update or Create repository and Source (skip if empty)
   actPanel.upcreateAct(fam.getProperty("MARR"));

   // Update family with contract event (skip if empty)
   contractPanel.upcreateContract(fam);

   // Update or Create witnesses (if required and not empty, as notes or individuals; skip if empty)
   witnessesPanel.upcreateWitnesses(fam.getProperty("MARR"), pitDocDate);

   // Update or Create husband's father (skip if empty)
   Indi husbFather = null;
   if (husbPanel.isOK(2)) husbFather = husbPanel.upcreateIndi(true, pitDocDate, 2);

   // Update or Create husband's mother (skip if empty)
   Indi husbMother = null;
   if (husbPanel.isOK(3)) husbMother = husbPanel.upcreateIndi(false, pitDocDate, 3);

   // Update or Create husband's parents family (always if parents identified, skip otherwise)
   HelperDocs.upcreateParents(gedcom, husband, husbFather, husbMother);

   // Update or Create wife's father (skip if empty)
   Indi wifeFather = null;
   if (wifePanel.isOK(2)) wifeFather = wifePanel.upcreateIndi(true, pitDocDate, 2);

   // Update or Create wife's mother (skip if empty)
   Indi wifeMother = null;
   if (wifePanel.isOK(3)) wifeMother = wifePanel.upcreateIndi(false, pitDocDate, 3);

   // Update or Create wife's parents family (always if parents identified, skip otherwise)
   HelperDocs.upcreateParents(gedcom, wife, wifeFather, wifeMother);

   // Update all SOSA (not only for indis that might have been created above but for all the ones potentially missing in the gedcom)
   HelperDocs.calcSOSA(gedcom, registry);

   mainEntity = fam;

   return mainEntity;
   }


  /**
   * Stores entities for copy
   */
  private class CopiedDocument {

    public List<Entity> listEnt = new ArrayList();
    public boolean valid = false;

    public Fam family = null;
    public Indi husband = null;
    public Indi wife = null;
    public Indi husbandfather = null;
    public Indi husbandmother = null;
    public Fam husbandfamily = null;
    public Indi wifefather = null;
    public Indi wifemother = null;
    public Fam wifefamily = null;
    public Indi[] witnesses = new Indi[NB_WIT];
    public Entity source = null;
    public Entity repo = null;
    public String invalidMsg = view.translate("msg_invalidMarrCopy");

    /** constructor */
    private CopiedDocument() {

       family = datePanel.getFamily();
       if (family != null) listEnt.add(family);
       husband = husbPanel.getIndi();
       if (husband != null) listEnt.add(husband);
       wife = wifePanel.getIndi();
       if (wife != null) listEnt.add(wife);

       husbandfather = husbPanel.getIndi(husbPanel.refFINDID);
       if (husbandfather != null) listEnt.add(husbandfather);
       husbandmother = husbPanel.getIndi(husbPanel.refMINDID);
       if (husbandmother != null) listEnt.add(husbandmother);
       husbandfamily = HelperDocs.getFamily(husbandfather, husbandmother);
       if (husbandfamily != null) listEnt.add(husbandfamily);

       wifefather = wifePanel.getIndi(wifePanel.refFINDID);
       if (wifefather != null) listEnt.add(wifefather);
       wifemother = wifePanel.getIndi(wifePanel.refMINDID);
       if (wifemother != null) listEnt.add(wifemother);
       wifefamily = HelperDocs.getFamily(wifefather, wifemother);
       if (wifefamily != null) listEnt.add(wifefamily);

       for (int i = 0; i < NB_WIT; i++) {
          witnesses[i] = witnessesPanel.getIndi(i);
          if (witnesses[i] != null) listEnt.add(witnesses[i]);
          }

       source = actPanel.getEntity(actPanel.refSOURD.getText());
       if (source != null) listEnt.add(source);
       repo = actPanel.getEntity(actPanel.refREPOD.getText());
       if (repo != null) listEnt.add(repo);

       valid = family != null && husband != null && wife != null;

       }

  }


  /**
   * Creation or Update of the wed's family
   *
   */
  public Fam upcreateFamily(Indi husband, Indi wife, PointInTime pitDocDate, String strDocPlace, String strAgent) throws GedcomException {

   // Create or update family
   Fam fam = HelperDocs.upcreateFamily(gedcom, husband, wife);

   // Create/Update MARR event with Date and Place
   Property propEvent = upcreateProperty(fam, "MARR", "");
   upcreateProperty(propEvent, "DATE", pitDocDate.getValue());
   upcreateProperty(propEvent, "PLAC", strDocPlace);
   if (exists(strAgent)) upcreateProperty(propEvent, "AGNC", strAgent);

   return fam;
   }


  /**
   * Implementation of populateAll
   */
  public void populateAll(Entity ent) {

    if ((ent != null) && (ent instanceof Fam) == false) return;


    Fam fam = (Fam) ent;

    // Get husband and wife and display them
    if (fam != null) {
       Indi husband = fam.getHusband();
       Indi wife = fam.getWife();
       husbPanel.setIndi(husband);
       wifePanel.setIndi(wife);
       }
    else {
       husbPanel.setIndi(null);
       wifePanel.setIndi(null);
       }

    // Get contract and display it
    contractPanel.setContract(fam);

    // Get witnesses and display them
    witnessesPanel.setWitnesses(fam);

    // Get source and display it
    actPanel.setSource(fam);

    return;
    }


  /**
   * Implementation of Get Date interface
   */
  public PointInTime getDate() {

    if (datePanel == null) return null;
    return datePanel.getDate();
    }

  /**
   * Implementation of Get Age interface
   *    - getAge is called by indipanel when individual is to be displayed so take opportunity to display family elements if available
   *    - then returns the age at the time of the wedding for the individual represented by ent
   */
  public String getAge(Indi indi) {
    IndiMarrPanel spousePanel = null;
    boolean isMale = (indi.getSex() == PropertySex.MALE);
    // get spouse
    spousePanel = isMale ? wifePanel : husbPanel;
    Indi spouse = spousePanel.getIndi();
    if (spouse == null) return null;
    Fam family = isMale ? HelperDocs.getFamily(indi, spouse) : HelperDocs.getFamily(spouse, indi);
    if (family == null) return null;
    Property prop = family.getProperty("MARR");
    if (prop == null) return null;

    // Take this opportunity to update spouse age as well
    Property propSpouse = isMale ? prop.getProperty("WIFE") : prop.getProperty("HUSB");
    if (propSpouse != null) {
       propSpouse = propSpouse.getProperty("AGE");
       if (propSpouse != null) spousePanel.setAge(propSpouse.toString());
       }

    // Take this opportunity to update witnesses if any
    witnessesPanel.populateAsso();

    // Take this opportunity to update source fields if necessary
    actPanel.populateSource();

    // Now returns age of indi
    prop = isMale ? prop.getProperty("HUSB") : prop.getProperty("WIFE");
    if (prop == null) return null;
    prop = prop.getProperty("AGE");
    if (prop == null) return null;

    return prop.toString();
    }

  /**
   * Implementation of Get Family interface
   *
   */
  public Entity getEntity() {
    Indi husb = husbPanel.getIndi();
    Indi wife = wifePanel.getIndi();
    if (husb == null || wife == null) return null;
    return HelperDocs.getFamily(husb, wife);
    }



  /**
   * Shortcuts of Helper methods
   */
  private Property upcreateProperty(Property prop, String tag, String value) {
    return HelperDocs.upcreateProperty(prop, tag, value);
    }

  public boolean exists(String str) {
    return HelperDocs.exists(str);
    }



} //MarriageDocs
