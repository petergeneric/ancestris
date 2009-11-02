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
package genj.table;

import genj.common.AbstractPropertyTableModel;
import genj.common.PropertyTableWidget;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;

/**
 * Component for showing entities of a gedcom file in a tabular way
 */
public class TableView extends JPanel implements ToolBarSupport  {
  
  private final static Logger LOG = Logger.getLogger("genj.table");

  /** a static set of resources */
  private Resources resources = Resources.get(this);
  
  /** the gedcom we're looking at */
  /*package*/ Gedcom gedcom;
  
  /** the manager around us */
  private ViewManager manager;
  
  /** the registry we keep */
  private Registry registry;
  
  /** the title we keep */
  private String title;
  
  /** the table we're using */
  /*package*/ PropertyTableWidget propertyTable;
  
  /** the gedcom listener we're using */
  private GedcomListener listener;
  
  /** the modes we're offering */
  private Map modes = new HashMap();
    {
      modes.put(Gedcom.INDI, new Mode(Gedcom.INDI, new String[]{"INDI","INDI:NAME","INDI:SEX","INDI:BIRT:DATE","INDI:BIRT:PLAC","INDI:FAMS", "INDI:FAMC", "INDI:OBJE:FILE"}));
      modes.put(Gedcom.FAM , new Mode(Gedcom.FAM , new String[]{"FAM" ,"FAM:MARR:DATE","FAM:MARR:PLAC", "FAM:HUSB", "FAM:WIFE", "FAM:CHIL" }));
      modes.put(Gedcom.OBJE, new Mode(Gedcom.OBJE, new String[]{"OBJE","OBJE:TITL"}));
      modes.put(Gedcom.NOTE, new Mode(Gedcom.NOTE, new String[]{"NOTE","NOTE:NOTE"}));
      modes.put(Gedcom.SOUR, new Mode(Gedcom.SOUR, new String[]{"SOUR","SOUR:TITL", "SOUR:TEXT"}));
      modes.put(Gedcom.SUBM, new Mode(Gedcom.SUBM, new String[]{"SUBM","SUBM:NAME" }));
      modes.put(Gedcom.REPO, new Mode(Gedcom.REPO, new String[]{"REPO","REPO:NAME", "REPO:NOTE"}));
    };
  
  /** current type we're showing */
  private Mode currentMode = getMode(Gedcom.INDI);
  
  /**
   * Constructor
   */
  public TableView(String titl, Gedcom gedcom, Registry registry, ViewManager mgr) {
    
    // keep some stuff
    this.gedcom = gedcom;
    this.registry = registry;
    this.title = titl;
    this.manager = mgr;
    
    // read properties
    loadProperties();
    
    // create our table
    propertyTable = new PropertyTableWidget(null);
    propertyTable.setAutoResize(false);

    // lay it out
    setLayout(new BorderLayout());
    add(propertyTable, BorderLayout.CENTER);
    
    // shortcuts
    new NextMode(true).install(this, JComponent.WHEN_IN_FOCUSED_WINDOW);
    new NextMode(false).install(this, JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    // done
  }
  
  /*package*/ TableModel getModel() {
    return propertyTable.getTableModel();
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(480,320);
  }
  
  /**
   * callback - chance to hook-up on add
   */  
  public void addNotify() {
    // continue
    super.addNotify();
    // hook on
    Mode set = currentMode;
    currentMode = null;
    setMode(set);
  }

  /**
   * callback - chance to hook-off on remove
   */
  public void removeNotify() {
    // save state
    saveProperties();
    // delegate
    super.removeNotify();
    // make sure the swing model is disconnected from gedcom model
    propertyTable.setModel(null);
  }
  
  /**
   * Returns a mode for given tag
   */
  /*package*/ Mode getMode() {
    return currentMode;
  }
  
  /**
   * Returns a mode for given tag
   */
  /*package*/ Mode getMode(String tag) {
    // known mode?
    Mode mode = (Mode)modes.get(tag); 
    if (mode==null) {
      mode = new Mode(tag, new String[0]);
      modes.put(tag, mode);
    }
    return mode;
  }
  
  /**
   * Sets the type of entities to look at
   */
  /*package*/ void setMode(Mode set) {
    // give mode a change to grab what it wants to preserve
    if (currentMode!=null)
      currentMode.save(registry);
    // remember current mode
    currentMode = set;
    // tell to table
    propertyTable.setModel(new Model(currentMode));
    // update its layout
    propertyTable.setColumnLayout(currentMode.layout);
  }
  
  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {
    // create buttons for mode switch
    ButtonHelper bh = new ButtonHelper().setInsets(0).setContainer(bar);
    
    InputMap inputs = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    for (int i=0, j=1;i<Gedcom.ENTITIES.length;i++) {
      String tag = Gedcom.ENTITIES[i];
      SwitchMode change = new SwitchMode(getMode(tag));
      bh.create(change);
    }
    
    // done
  }
  
  /**
   * Read properties from registry
   */
  private void loadProperties() {

    // get modes
    Iterator it = modes.values().iterator();
    while (it.hasNext()) {
      Mode mode = (Mode)it.next();
      mode.load(registry);
    }

    // get current mode
    String tag = registry.get("mode", "");
    if (modes.containsKey(tag))
      currentMode = getMode(tag);
    
    // Done
  }
  
  /**
   * Write properties from registry
   */
  private void saveProperties() {
    
    // save current type
    registry.put("mode", currentMode.getTag());
    
    // save modes
    Iterator it = modes.values().iterator();
    while (it.hasNext()) {
      Mode mode = (Mode)it.next();
      mode.save(registry);
    }
    // Done
  }  
  
  /**
   * Action - go to next mode
   */
  private class NextMode extends Action2 {
    private int dir;
    private NextMode(boolean left) {
      int vk;
      if (left) {
        vk = KeyEvent.VK_LEFT;
        dir = -1;
      } else {
        vk = KeyEvent.VK_RIGHT;
        dir = 1;
      }
      setAccelerator(KeyStroke.getKeyStroke(vk, KeyEvent.CTRL_DOWN_MASK));
    }
    protected void execute() {
      int next = -1;
      for (int i=0,j=Gedcom.ENTITIES.length; i<j; i++) {
        next = (i+j+dir)%Gedcom.ENTITIES.length;
        if (currentMode == getMode(Gedcom.ENTITIES[i])) 
          break;
      }
      setMode(getMode(Gedcom.ENTITIES[next]));
    }
  } //NextMode
  
  /**
   * Action - flip view to entity type
   */
  private class SwitchMode extends Action2 {
    /** the mode this action triggers */
    private Mode mode;
    /** constructor */
    SwitchMode(Mode mode) {
      this.mode = mode;
      setTip(resources.getString("mode.tip", Gedcom.getName(mode.getTag(),true)));
      setImage(Gedcom.getEntityImage(mode.getTag()));
    }
    /** run */
    public void execute() {
      setMode(mode);
    }
  } //ActionMode
  
  /** 
   * A PropertyTableModelWrapper
   */
  private class Model extends AbstractPropertyTableModel {

    /** mode */
    private Mode mode;
    
    /** our cached rows */
    private List rows;
    
    /** constructor */
    private Model(Mode set) {
      mode = set;
    }
    
    /** gedcom */
    public Gedcom getGedcom() {
      return gedcom;
    }

    /** # columns */
    public int getNumCols() {
      return mode.getPaths().length;
    }
    
    /** # rows */
    public int getNumRows() {
      // cache entities if not there yet
      if (rows==null) 
        rows = new ArrayList(gedcom.getEntities(mode.getTag()));
      // ready 
      return rows.size();
    }
    
    /** path for colum */
    public TagPath getPath(int col) {
      return mode.getPaths()[col];
    }

    /** property for row */
    public Property getProperty(int row) {
      
      // init rows
      getNumRows();

      // and look it up
      Property result = (Property)rows.get(row);
      if (result==null)
        return result;
      
      // since we do a lazy update after a gedcom write lock we check if cached properties are still good 
      if (result.getEntity()==null) {
        result = null;
        rows.set(row, null);
      }
      
      // done
      return result;
    }
    
    /** gedcom callback */
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
      // an entity we're not looking at?
      if (!mode.getTag().equals(entity.getTag())) 
        return;
      // add it
      rows.add(entity);
      // tell about it
      fireRowsAdded(rows.size()-1, rows.size()-1);
      // done
    }

    /** gedcom callback */
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      // an entity we're not looking at?
      if (!mode.getTag().equals(entity.getTag())) 
        return;
      // delete it
      for (int i=0;i<rows.size();i++) {
        if (rows.get(i)==entity) {
          rows.remove(i);
          // tell about it
          fireRowsDeleted(i, i);
          // done
          return;
        }
      }
      // hmm, strange
      LOG.warning("got notified that entity "+entity.getId()+" was deleted but it wasn't in rows in the first place");
    }

    /** gedcom callback */
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
      invalidate(gedcom, property.getEntity(), property.getPath());
    }

    /** gedcom callback */
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
      invalidate(gedcom, property.getEntity(), property.getPath());
    }

    /** gedcom callback */
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
      invalidate(gedcom, property.getEntity(), new TagPath(property.getPath(), deleted.getTag()));
    }
    
    private void invalidate(Gedcom gedcom, Entity entity, TagPath path) {
      // an entity we're not looking at?
      if (!mode.getTag().equals(entity.getTag())) 
        return;
      // a path we're interested in?
      TagPath[] paths = mode.getPaths();
      for (int i=0;i<paths.length;i++) {
        if (paths[i].equals(path)) {
          for (int j=0;j<rows.size();j++) {
            if (rows.get(j)==entity) {
                fireRowsChanged(j,j,i);
                return;
            }
          }      
        }
      }
      // done
    }

  } //Model

  /**
   * A mode is a configuration for a set of entities
   */
  /*package*/ class Mode {
    
    /** attributes */
    private String tag;
    private String[] defaults;
    private TagPath[] paths;
    private String layout;
    
    /** constructor */
    private Mode(String t, String[] d) {
      // remember
      tag      = t;
      defaults = d;
      paths    = TagPath.toArray(defaults);
    }
    
    /** load properties from registry */
    private void load(Registry r) {
      
      String[] ps = r.get(tag+".paths" , (String[])null);
      if (ps!=null) 
        paths = TagPath.toArray(ps);

      layout = r.get(tag+".layout", (String)null);
      
    }
    
    /** set paths */
    /*package*/ void setPaths(TagPath[] set) {
      paths = set;
      if (currentMode==this)
        setMode(currentMode);
    }
    
    /** get paths */
    /*package*/ TagPath[] getPaths() {
      return paths;
    }
    
    /** save properties from registry */
    private void save(Registry r) {
      
      // grab current column widths & sort column
      if (currentMode==this) 
        layout = propertyTable.getColumnLayout();

	    registry.put(tag+".paths" , paths);
	    registry.put(tag+".layout", layout);
    }
    
    /** tag */
    /*package*/ String getTag() {
      return tag;
    }
    
  } //Mode
  
} //TableView
