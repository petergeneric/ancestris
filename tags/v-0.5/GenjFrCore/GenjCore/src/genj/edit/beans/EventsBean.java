/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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
package genj.edit.beans;

import genj.edit.ChoosePropertyBean;
import genj.edit.Images;
import genj.edit.actions.DelProperty;
import genj.edit.actions.EditEvent;
import genj.edit.actions.EditNote;
import genj.edit.actions.EditSource;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyComparator;
import genj.gedcom.PropertyEvent;
import genj.gedcom.UnitOfWork;
import genj.util.WordBuffer;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.TableWidget;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.JScrollPane;

/**
 * A complex bean displaying events of an individual or family
 */
public class EventsBean extends PropertyBean {
  
  private TableWidget<Property> table = new TableWidget<Property>();
  
  public EventsBean() {
    
    // event name
    table.new Column(Gedcom.getName("EVEN")) {
      public String getValue(Property prop) {
        return prop.getPropertyName();
      }
    };
    
    // event detail
    table.new Column(RESOURCES.getString("even.detail")) {
      public String getValue(Property event) {
        String val = event.getDisplayValue();
        if (val.length()>0)
          return val;
        String type = event.getPropertyDisplayValue("TYPE");
        if (type.length()>0)
          return type;
        String plac = event.getPropertyDisplayValue("PLAC");
        if (plac.length()>0)
          return plac;
        Property addr = event.getProperty("ADDR");
        if (addr!=null) {
          WordBuffer buf = new WordBuffer(",");
          buf.append(addr.getDisplayValue());
          buf.append(addr.getPropertyDisplayValue("CITY"));
          buf.append(addr.getPropertyDisplayValue("POST"));
          buf.append(addr.getPropertyDisplayValue("CTRY"));
          buf.append(addr.getPropertyDisplayValue("STAE"));
          if (buf.length()>0)
            return buf.toString();
        }
        return "";
      }
    };
    
    // event date
    table.new Column(Gedcom.getName("DATE")) {
      public String getValue(Property row) {
        return row.getPropertyDisplayValue("DATE");
      } 
    };
    
    // event place
    table.new Column(Gedcom.getName("PLAC")) {
      public String getValue(Property row) {
        return row.getPropertyDisplayValue("PLAC");
      }
    };

    // Note
    table.new Column("",Action2.class) {
      public Action2 getValue(Property event) {
        return new EditNote(event, true);
      }
    };
        
    // Source
    table.new Column("",Action2.class) {
      public Action2 getValue(Property event) {
        return new EditSource(event, true);
      }
    };

    
    // Edit
    table.new Column("",Action2.class) {
      public Action2 getValue(Property event) {
        return new EditEvent(event);
      }
    };    
    
    // Delete
    table.new Column("",Action2.class) {
      public Action2 getValue(Property event) {
        return new DelProperty(event);
      }
    };

    // show
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(table));

  }
  
  @Override
  public List<? extends Action> getActions() {
    return Collections.singletonList(new Add());
  }
  
  @Override
  protected void commitImpl(Property property) throws GedcomException {
  }
  
  private void commit(Gedcom gedcom, UnitOfWork commit) {
    changeSupport.fireChangeEvent();
    gedcom.doMuteUnitOfWork(commit);
  }
  
  private boolean isEvent(MetaProperty meta) {
    
    // crude filter
    while (meta!=null) {
      if (PropertyEvent.class.isAssignableFrom(meta.getType()))
        return true;
      meta = meta.getSuper();
    }
    return false;
  }

  @Override
  protected void setPropertyImpl(Property prop) {
    
    // scan for events
    List<Property> events = new ArrayList<Property>();
    if (root!=null) for (Property child : root.getProperties()) {
      if (!isEvent(child.getMetaProperty()))
        continue;
      // keep
      events.add(child);
    }
    Collections.sort(events, new PropertyComparator(".:DATE"));
    table.setRows(events);
  }
    
  private void add(Property event) {
    table.addRow(event);
  }
    
  private void del(Property event) {
    table.deleteRow(event);
  }
    
  
  /**
   * add an event
   */
  private class Add extends Action2 {
    
    private Property added;
    
    Add() {
      setImage(PropertyEvent.IMG.getOverLayed(Images.imgNew));
      setTip(RESOURCES.getString("even.add"));
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
      
      final Property root = getProperty();
      
      MetaProperty[] metas = root.getNestedMetaProperties(MetaProperty.WHERE_NOT_HIDDEN | MetaProperty.WHERE_CARDINALITY_ALLOWS);
      List<MetaProperty> choices = new ArrayList<MetaProperty>(metas.length);
      for (MetaProperty meta : metas) {
        if (isEvent(meta))
          choices.add(meta);
      }
      final ChoosePropertyBean choose = new ChoosePropertyBean(choices.toArray(new MetaProperty[choices.size()]));
      choose.setSingleSelection(true);
      if (0!=DialogHelper.openDialog(getTip(), DialogHelper.QUESTION_MESSAGE, 
          choose, Action2.okCancel(), EventsBean.this))
        return;
      
      final String add = choose.getSelectedTags()[0];
      
      root.getGedcom().doMuteUnitOfWork(new UnitOfWork() {
        public void perform(Gedcom gedcom) {
          added = root.addProperty(add, "");
          table.addRow(added);
        }
      });

      if (added!=null)
        new EditEvent(added).actionPerformed(event);
            
    }
    
  } //Add

}
