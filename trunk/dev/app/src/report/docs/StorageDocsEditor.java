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
import genj.util.swing.*;
import genj.gedcom.time.PointInTime;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.*;

import javax.swing.*;


/**
 * The editor for entering a marriage document
 */
class StorageDocsEditor extends EditorDocs implements DocsListener {

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
  private ActPanel actPanel;

  /** debugging */
  private boolean debug = false;



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

    // sets initial entity
    if (mainEntity instanceof Source)
       actPanel.setSource((Source)mainEntity);
    else if (mainEntity instanceof Repository)
       actPanel.setRepository((Repository)mainEntity);

    // Set debug mode
    debug = registry.get("debug", "0").equals("1");

    // done
  }

  /**
   * Get Title
   */
  public String getTitle() {
     return view.translate("SourDocsUpper");
  }

  public String getSmallTitle() {
     return view.translate("SourDocsLower");
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
      HelperDocs.processOk(gedcom, view, registry, HelperDocs.STOR_EDITOR);
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
      HelperDocs.processCancel(gedcom, view, registry, HelperDocs.STOR_EDITOR);
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
    actPanel = new ActPanel(gedcom, this, dataSet, registry, null, getSmallTitle());

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
       panel.add(actPanel, c);

       c.insets = new Insets(15,0,0,0);
       c.gridx = 0; c.gridy = 1;
       //panel.add(husbPanel, c);

       }
    else {
       // Create tabbed pane
       JTabbedPane tabbedPane = new JTabbedPane();
       FlowLayout fl = new FlowLayout(FlowLayout.LEFT);

       JPanel jp1 = new JPanel(fl);
       jp1.add(actPanel);
       tabbedPane.add(view.translate("SourcesRepo"), jp1);

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
   * - saves SOUR only or SOUR with REPO or REPO only
   */
  public Entity updateGedcom() throws GedcomException {


   if (!actPanel.existsSourceField() && !actPanel.existsRepositoryField()) throw new GedcomException(view.translate("msg_missingSour"));

   // Update or Create repository and Source with no event associated
   mainEntity = actPanel.upcreateAct(null);

   return mainEntity;
   }




  /**
   * Stores entities for copy
   */
  private class CopiedDocument {

    public List<Entity> listEnt = new ArrayList<Entity>();
    public boolean valid = false;

    public Entity source = null;
    public Entity repo = null;
    public String invalidMsg = view.translate("msg_invalidSourCopy");

    /** constructor */
    private CopiedDocument() {

       source = actPanel.getEntity(actPanel.refSOURD.getText());
       if (source != null) listEnt.add(source);
       repo = actPanel.getEntity(actPanel.refREPOD.getText());
       if (repo != null) listEnt.add(repo);

       valid = source != null || repo != null;

       }

  }


    public void populateAll(Entity ent) { return; }

    public PointInTime getDate() { return null; }

    public String getAge(Indi indi) { return null; }

    public Entity getEntity() { return null; }


  /**
   * Shortcuts of Helper methods
   */
  private Property upcreateProperty(Property prop, String tag, String value) {
    return HelperDocs.upcreateProperty(prop, tag, value);
    }

  public boolean exists(String str) {
    return HelperDocs.exists(str);
    }



} //StorageDocs
