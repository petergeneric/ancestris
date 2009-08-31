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
import genj.util.Registry;
import genj.util.GridBagHelper;
import genj.util.swing.*;
import genj.view.ContextProvider;
import genj.view.ViewContext;
import genj.gedcom.time.PointInTime;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.swing.event.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;


/**
 * The editor for entering a birth document
 */
class BirthDocsEditor extends EditorDocs implements DocsListener {

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
  private IndiBirthPanel indiPanel;
  private WitnessesPanel witnessesPanel;
  private ActPanel actPanel;

  /** debugging */
  private boolean debug = false;

  /** number of witnesses for a birth */
  private int NB_WIT = 2;



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
     return view.translate("BirtDocsUpper");
  }

  public String getSmallTitle() {
     return view.translate("BirtDocsLower");
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
      HelperDocs.processOk(gedcom, view, registry, HelperDocs.BIRT_EDITOR);
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
      HelperDocs.processCancel(gedcom, view, registry, HelperDocs.BIRT_EDITOR);
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
   * Populate panel with birth document format
   */
  public void populatePanel(JPanel panel) {

    // define main panels
    datePanel       = new DatePanel(gedcom, this, dataSet.indis, dataSet, "INDI:BIRT");
    indiPanel       = new IndiBirthPanel(gedcom, this, dataSet);
    witnessesPanel  = new WitnessesPanel(gedcom, this, NB_WIT, dataSet, "INDI:BIRT", view.translate("Witnesses"));
    actPanel        = new ActPanel(gedcom, this, dataSet, registry, "INDI:BIRT", getSmallTitle());

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
       panel.add(indiPanel, c);

       c.gridx = 0; c.gridy = 2;
       panel.add(witnessesPanel, c);

       c.gridx = 0; c.gridy = 3;
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
       jp2.add(indiPanel);
       tabbedPane.add(view.translate("NewBorn"), jp2);

       JPanel jp3 = new JPanel(fl);
       jp3.add(witnessesPanel);
       tabbedPane.add(view.translate("Witnesses"), jp3);

       JPanel jp4 = new JPanel(fl);
       jp4.add(actPanel);
       tabbedPane.add(view.translate("SourSto"), jp4);

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

   // Child are mandatory
   if (!indiPanel.isOK(1)) throw new GedcomException(view.translate("msg_missingKid"));

   // Update or create child
   Indi child = indiPanel.upcreateIndi(1, datePanel.refLISTD, pitDocDate, datePanel.refPLACD, datePanel.refDATED, datePanel.refAGNCD.getText());

   // Update or create father
   Indi father = null;
   if (indiPanel.isOK(2)) father = indiPanel.upcreateIndi(2, null, pitDocDate, null, null, null);

   // Update or create mother
   Indi mother = null;
   if (indiPanel.isOK(3)) mother = indiPanel.upcreateIndi(3, null, pitDocDate, null, null, null);

   // Update or create family made of father and mother and linking child in
   HelperDocs.upcreateParents(gedcom, child, father, mother);

   // Update or Create repository and Source (skip if empty)
   actPanel.upcreateAct(child.getProperty("BIRT"));

   // Update or Create witnesses (if required and not empty, as notes or individuals; skip if empty)
   witnessesPanel.upcreateWitnesses(child.getProperty("BIRT"), pitDocDate);

   // Update all SOSA (not only for indis that might have been created above but for all the ones potentially missing in the gedcom)
   HelperDocs.calcSOSA(gedcom, registry);

   mainEntity = child;

   return mainEntity;
   }



  /**
   * Stores entities for copy
   */
  private class CopiedDocument {

    public List<Entity> listEnt = new ArrayList();
    public boolean valid = false;

    public Indi child = null;
    public Indi father = null;
    public Indi mother = null;
    public Fam family = null;
    public Indi[] witnesses = new Indi[NB_WIT];
    public Entity source = null;
    public Entity repo = null;
    public String invalidMsg = view.translate("msg_invalidBirtCopy");

    /** constructor */
    private CopiedDocument() {

       child = datePanel.getIndi();
       if (child != null) listEnt.add(child);
       father = indiPanel.getIndi(indiPanel.refFINDID);
       if (father != null) listEnt.add(father);
       mother = indiPanel.getIndi(indiPanel.refMINDID);
       if (mother != null) listEnt.add(mother);
       family = HelperDocs.getFamily(father, mother);
       if (family != null) listEnt.add(family);

       for (int i = 0; i < NB_WIT; i++) {
          witnesses[i] = witnessesPanel.getIndi(i);
          if (witnesses[i] != null) listEnt.add(witnesses[i]);
          }

       source = actPanel.getEntity(actPanel.refSOURD.getText());
       if (source != null) listEnt.add(source);
       repo = actPanel.getEntity(actPanel.refREPOD.getText());
       if (repo != null) listEnt.add(repo);

       valid = child != null && family != null && (father != null || mother != null);

       }

  }


  /**
   * Implementation of populateAll
   */
  public void populateAll(Entity ent) {

    if ((ent instanceof Indi) == false) return;

    Indi child = (Indi) ent;

    // Get parents of child and display them
    if (child != null) {
       Fam parents = child.getFamilyWhereBiologicalChild();
       Indi father = parents != null ? parents.getHusband() : null;
       Indi mother = parents != null ? parents.getWife() : null;
       indiPanel.setIndi(child, father, mother);
       }
    else {
       indiPanel.setIndi(null, null, null);
       }

    // Set witnesses
    witnessesPanel.setWitnesses(child);

    // Get source and display it
    actPanel.setSource(child);

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
   */
  public String getAge(Indi indi) {
    return "";
    }

  /**
   * Implementation of Get Family interface
   */
  public Entity getEntity() {
    return datePanel.getIndi();
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



} //BirthDocs
