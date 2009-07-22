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

import genj.report.*;
import genj.edit.actions.Redo;
import genj.edit.actions.Undo;
import genj.edit.beans.BeanFactory;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Source;
import genj.gedcom.Repository;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyXRef;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.swing.PopupWidget;
import genj.window.WindowBroadcastListener;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.BoxLayout;

import spin.Spin;

/**
 * Component for editing genealogic entity properties
 */
public class EditDocsPanel extends JPanel  {

  /** the gedcom we're looking at */
  private Report report;
  private Gedcom gedcom;
  private Entity mainEntity;

  /** the registry we use */
  private Registry registry;

  /** the window handle */
  private WindowManager winMgr;

  /** the resources we use */
  static final Resources resources = Resources.get(EditDocsPanel.class);

  /** actions we offer */
  private MenuDocs         menuDocs;
  private DocTypeBirth     birthDoc;
  private DocTypeDeath     deathDoc;
  private DocTypeMarriage  marriageDoc;
  private DocTypeStorage   storageDoc;
  private ActionSearch     searchAction;

  /** editor menu values */
  private EditDocsPanel thisDocsPanel;
  private JScrollPane editScroll;
  private EditorDocs editor;
  private int currentEditor = 0;

  /** toolbar */
  private JToolBar bar;

  /** data */
  private DataSet dataSet;

  /** refresh callback */
  private GedcomListener callback = new GedcomListenerAdapter() {
    };


  /**
   * Constructor
   */
  public EditDocsPanel(Report report, Gedcom setGedcom, Entity entity, Registry setRegistry, WindowManager setWinMgr) {

    super(new BorderLayout());

    // remember
    this.report = report;
    gedcom = setGedcom;
    mainEntity = entity;
    registry = setRegistry;
    winMgr = setWinMgr;
    thisDocsPanel = this;

    // Get all the data
    dataSet = new DataSet(gedcom);

    // Create document buttons and toolbar
    menuDocs = new MenuDocs();
    birthDoc = new DocTypeBirth();
    deathDoc = new DocTypeDeath();
    marriageDoc = new DocTypeMarriage();
    storageDoc = new DocTypeStorage();
    searchAction = new ActionSearch();
    CreateToolBar();

    // Load memorised editor but overwrite depending on whether indi or fam or other exists
    int launchView = registry.get("view", HelperDocs.MENU_STRING).indexOf("1");
    if (mainEntity instanceof Indi) launchView = HelperDocs.BIRT_EDITOR;
    else if (mainEntity instanceof Fam) launchView = HelperDocs.MARR_EDITOR;
    else if (mainEntity instanceof Source) launchView = HelperDocs.STOR_EDITOR;
    else if (mainEntity instanceof Repository) launchView = HelperDocs.STOR_EDITOR;

    if      (launchView == HelperDocs.MENU_DOCS)   menuDocs.trigger();
    else if (launchView == HelperDocs.BIRT_EDITOR) birthDoc.trigger();
    else if (launchView == HelperDocs.MARR_EDITOR) marriageDoc.trigger();
    else if (launchView == HelperDocs.DEAT_EDITOR) deathDoc.trigger();
    else if (launchView == HelperDocs.STOR_EDITOR) storageDoc.trigger();

    // Done
  }


  /**
   * Action - Menu ***************
   */
  private class MenuDocs extends Action2 {
    private MenuDocs() {
      setImage(ImagesDocs.imgMenu);
      setTip(translate("Menu", gedcom.getName()));
      }
    protected void execute() {
      if (IsOkToSwitchTo(HelperDocs.MENU_DOCS)) {
         displayMenuPanel();
         currentEditor = HelperDocs.MENU_DOCS;
         registry.put("view", HelperDocs.MENU_STRING); // remember
         }
      }
  }

  /**
   * Action - Birth ***************
   */
  private class DocTypeBirth extends Action2 {
    private DocTypeBirth() {
      setImage(ImagesDocs.imgBirth);
      setTip(translate("BirthDocMenuItem"));
      }
    protected void execute() {
      if (IsOkToSwitchTo(HelperDocs.BIRT_EDITOR)) {
         setEditor(new BirthDocsEditor());
         currentEditor = HelperDocs.BIRT_EDITOR;
         registry.put("view", HelperDocs.BIRT_STRING); // remember
         }
      }
  }

  /**
   * Action - Marriage ***************
   */
  private class DocTypeMarriage extends Action2 {
    private DocTypeMarriage() {
      setImage(ImagesDocs.imgMarriage);
      setTip(translate("MarrDocMenuItem"));
    }
    protected void execute() {
      if (IsOkToSwitchTo(HelperDocs.MARR_EDITOR)) {
         setEditor(new MarriageDocsEditor());
         currentEditor = HelperDocs.MARR_EDITOR;
         registry.put("view", HelperDocs.MARR_STRING); // remember
         }
    }
  }

  /**
   * Action - Death ***************
   */
  private class DocTypeDeath extends Action2 {
    private DocTypeDeath() {
      setImage(ImagesDocs.imgDeath);
      setTip(translate("DeathDocMenuItem"));
    }
    protected void execute() {
      if (IsOkToSwitchTo(HelperDocs.DEAT_EDITOR)) {
         setEditor(new DeathDocsEditor());
         currentEditor = HelperDocs.DEAT_EDITOR;
         registry.put("view", HelperDocs.DEAT_STRING); // remember
         }
    }
  }


  /**
   * Action - Storage ***************
   */
  private class DocTypeStorage extends Action2 {
    private DocTypeStorage() {
      setImage(ImagesDocs.imgStore);
      setTip(translate("StorDocMenuItem"));
      }
    protected void execute() {
      if (IsOkToSwitchTo(HelperDocs.STOR_EDITOR)) {
         setEditor(new StorageDocsEditor());
         currentEditor = HelperDocs.STOR_EDITOR;
         registry.put("view", HelperDocs.STOR_STRING); // remember
         }
      }
  }

  /**
   * IsOkToSwitch test
   */
  private boolean IsOkToSwitchTo(int requiredEditor) {
      return HelperDocs.processChange(gedcom, thisDocsPanel, registry, requiredEditor);
  }


  /**
   * Action - search documents view
   */
  private class ActionSearch extends Action2 {
    /** constructor */
    protected ActionSearch() {
      setImage(ImagesDocs.imgSearch);
      setTip(translate("SearchMenuItem"));
    }
    /** run */
    protected void execute() {
      winMgr.openWindow("docs.search", translate("SearchTitle"), ImagesDocs.imgSearch, new SearchWidget(thisDocsPanel, registry, gedcom), null, null);
    }
  } //ActionSearch


  /**
   * Action - settings view
   */
  private class ActionSettings extends Action2 {
    /** constructor */
    protected ActionSettings() {
      setImage(ImagesDocs.imgSettings);
      setTip(translate("ParamMenuItem"));
    }
    /** run */
    protected void execute() {
      winMgr.openWindow("docs.settings", translate("ParamTitle"), ImagesDocs.imgSettings, new SettingsWidget(thisDocsPanel, registry), null, null);
    }
  } //ActionSettings


  /**
   * Action - close view
   */
  private class ActionClose extends Action2 {
    /** constructor */
    protected ActionClose() {
      setImage(ImagesDocs.imgClose);
      setTip(translate("CloseMenuItem"));
    }
    /** run */
    protected void execute() {
      HelperDocs.processClose(gedcom, thisDocsPanel, registry, winMgr);
    }
  } //ActionClose


  /**
   * Display s menu panel
   */
  private void displayMenuPanel() {

    // remove old editor
    if (editScroll != null) {
       remove(editScroll);
       }

    // set up new panel
    JPanel menu = new JPanel();

    // position elements
    JPanel menuButtons = new JPanel();
    menuButtons.setLayout(new BoxLayout(menuButtons, BoxLayout.Y_AXIS));
    menuButtons.add(getMenuButton(birthDoc, translate("BirthDocMenuItem")));
    menuButtons.add(getMenuButton(marriageDoc, translate("MarrDocMenuItem")));
    menuButtons.add(getMenuButton(deathDoc, translate("DeathDocMenuItem")));
    menuButtons.add(getMenuButton(storageDoc, translate("StorDocMenuItem")));
    menuButtons.add(getMenuButton(searchAction, translate("SearchMenuItem")));

    // add to layout
    menu.add(menuButtons, "Center");
    menu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    editScroll = new JScrollPane(menu);
    editScroll.getVerticalScrollBar().setFocusable(false);
    editScroll.getHorizontalScrollBar().setFocusable(false);
    editScroll.getVerticalScrollBar().setUnitIncrement(40);

    JLabel title = new JLabel(translate("Menu", gedcom.getName()), JLabel.CENTER);
    title.setFont(new Font("xxx", Font.BOLD, 18));
    title.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    editScroll.setColumnHeaderView(title);
    add(editScroll, BorderLayout.CENTER);

    // show
    revalidate();
    repaint();
  }


  /**
   * Menu button
   */
  private JPanel getMenuButton(Action2 action, String label) {
    JPanel button = new JPanel(new FlowLayout(FlowLayout.LEFT));
    ButtonHelper bh = new ButtonHelper().setInsets(7).setContainer(button);
    bh.create(action);
    JLabel labelComponent = new JLabel(label); 
    labelComponent.setFont(new Font("title", Font.BOLD, 16));
    button.add(labelComponent);
    button.setAlignmentX(Component.LEFT_ALIGNMENT);
    button.add(Box.createHorizontalGlue());
    button.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    return button;
    }


  /**
   * Set editor to use
   */
  private void setEditor(EditorDocs set) {

    // remove old editor
    if (editScroll != null) {
       remove(editScroll);
       }

    // set up new
    editor = set;
    editor.init(gedcom, mainEntity, this, registry, dataSet);
    editor.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    // add to layout
    editScroll = new JScrollPane(editor);
    editScroll.getVerticalScrollBar().setFocusable(false);
    editScroll.getHorizontalScrollBar().setFocusable(false);
    editScroll.getVerticalScrollBar().setUnitIncrement(40);

    JLabel title = new JLabel(editor.getTitle(), JLabel.CENTER);
    title.setFont(new Font("title", Font.BOLD, 18));
    title.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    editScroll.setColumnHeaderView(title);
    add(editScroll, BorderLayout.CENTER);

    // show
    revalidate();
    repaint();

  }

  /**
   * Manages a call to display the right panel based on a given property
   */
  public void setPanel(Property prop) {

     // quit if nothing
     if (prop == null) return;

     // set default entity for docs panels
     Entity ent = prop.getEntity();
     setEntity(ent);

     // display the editor corresponding to the event
     String tag = prop.getTag();
     currentEditor = 0;
     if (tag.equals("BIRT")) birthDoc.trigger();
     else if (tag.equals("MARR")) marriageDoc.trigger();
     else if (tag.equals("DEAT")) deathDoc.trigger();

     // restore the editor size to normal and put it to front 
     Component window = (Component)editor;
     while (window.getParent()!=null) window = window.getParent();
     ((JFrame)window).setState(java.awt.Frame.NORMAL);
     ((JFrame)window).toFront();
     }


  /**
   * Manages translations for all downstreams panels
   */
  public String translate(String key) {
     return report.translate(key);
     }
  public String translate(String key, Object value) {
     return report.translate(key, value);
     }

  /**
   * Set individual from child window
   */
  public void setEntity(Entity entity) {
     this.mainEntity = entity;
     }

  /**
   * Reset from child window
   */
  public void resetForm(int launchView) {
     dataSet = new DataSet(gedcom);
     if      (launchView == HelperDocs.MENU_DOCS)   menuDocs.trigger();
     else if (launchView == HelperDocs.BIRT_EDITOR) setEditor(new BirthDocsEditor());
     else if (launchView == HelperDocs.MARR_EDITOR) setEditor(new MarriageDocsEditor());
     else if (launchView == HelperDocs.DEAT_EDITOR) setEditor(new DeathDocsEditor());
     else if (launchView == HelperDocs.STOR_EDITOR) setEditor(new StorageDocsEditor());
     }

  /**
   * Create toolbar
   */
  private void CreateToolBar() {

    // Create toolbar
    bar = new JToolBar("");
    String layout = registry.get("toolbar", BorderLayout.WEST);
    bar.setOrientation((BorderLayout.WEST.equals(layout)||BorderLayout.EAST.equals(layout)) ? SwingConstants.VERTICAL : SwingConstants.HORIZONTAL);

    // buttons for property manipulation
    ButtonHelper bh = new ButtonHelper().setContainer(bar);
    bh.setInsets(0);

    // add buttons
    bh.create(menuDocs);
    bar.addSeparator();
    bh.create(birthDoc);
    bar.addSeparator();
    bh.create(marriageDoc);
    bar.addSeparator();
    bh.create(deathDoc);
    bar.addSeparator();
    bh.create(storageDoc);
    bar.addSeparator();
    bh.create(searchAction);
    bar.add(Box.createGlue());
    bh.create(new ActionSettings());
    bar.addSeparator();
    bh.create(new ActionClose());

    add(bar, layout);
    // done
  }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(256,480);
  }

  /**
   * When adding components we fix a Toolbar's sub-component's orientation
   */
  protected void addImpl(Component comp, Object constraints, int index) {
    if (comp==bar) {
       registry.put("toolbar", constraints.toString());
       }
    super.addImpl(comp, constraints, index);
  }

  /**
   * Accessors
   */
  public int getCurrentEditorNb() {
    return currentEditor;
    }

  public EditorDocs getCurrentEditorDocsPanel() {
    return editor;
    }

  public Entity getMainEntity() {
    return mainEntity;
    }


} //EditView
