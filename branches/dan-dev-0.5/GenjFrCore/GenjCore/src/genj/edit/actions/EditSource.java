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
package genj.edit.actions;

import genj.edit.BeanPanel;
import genj.edit.Images;
import genj.edit.beans.ReferencesBean;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.Property;
import genj.gedcom.PropertySource;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import genj.gedcom.UnitOfWork;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TableWidget;
import genj.util.swing.NestedBlockLayout.Cell;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * Edit note for a property
 */
public class EditSource extends Action2 {
  
  private final static Resources RESOURCES = Resources.get(EditSource.class);
  
  public final static ImageIcon 
    EDIT_SOUR = Grammar.V551.getMeta(new TagPath("SOUR")).getImage(),
    NEW_SOUR = EDIT_SOUR.getOverLayed(Images.imgNew),
    NO_SOUR = EDIT_SOUR.getTransparent(128);
  
  private Property property;
  
  /**
   * Constructor
   * @param property the property the note is for
   */
  public EditSource(Property property) {
    this(property, false);
  }
  
  /**
   * Constructor
   * @param property the property the note is for
   */
  public EditSource(Property property, boolean showNone) {
    
    this.property = property;
    
    boolean has = !getSources(property).isEmpty();
    setImage(has ? EDIT_SOUR : (showNone ? NO_SOUR : NEW_SOUR));
    setText(has ? RESOURCES.getString("edit",Gedcom.getName(Gedcom.SOUR, true)) : RESOURCES.getString("new", Gedcom.getName(Gedcom.SOUR)));
    setTip(getText());
  }
  
  private List<PropertySource> getSources(Property property) {
    List<PropertySource> sources = new ArrayList<PropertySource>();
    for (Property source : property.getProperties(Gedcom.SOUR, true)) {
      if (source instanceof PropertySource)
        sources.add((PropertySource)source);
    }
    return sources;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    
    List<PropertySource> sources = getSources(property);
    if (!sources.isEmpty()) {
      
      final TableWidget<PropertySource> table = new TableWidget<PropertySource>();
      Action2[] actions = new Action2[]{ Action2.ok(), new Action2(RESOURCES.getString("link", Gedcom.getName("SOUR"))) };
      final GedcomDialog dlg = new GedcomDialog(property.getGedcom(), property.toString() + " - " + getTip(), DialogHelper.QUESTION_MESSAGE, new JScrollPane(table), actions, e);
    
      table.new Column(Gedcom.getName("SOUR")) {
        public Object getValue(PropertySource source) {
          return source.getTargetEntity().getId();
        }
      };
      table.new Column(Gedcom.getName("AUTH")) {
        public Object getValue(PropertySource source) {
          return source.getTargetEntity().getPropertyDisplayValue("AUTH");
        }
      };
      table.new Column(Gedcom.getName("TITL")) {
        public Object getValue(PropertySource source) {
          return source.getTargetEntity().getPropertyDisplayValue("TITL");
        }
      };
      table.new Column(Gedcom.getName("PAGE")) {
        public Object getValue(PropertySource source) {
          return source.getPropertyDisplayValue("PAGE");
        }
      };
      table.new Column("", Action2.class) {
        public Object getValue(PropertySource source) {
          return new Edit(source,false) {
            @Override
            public void actionPerformed(ActionEvent event) {
              dlg.cancel();
              super.actionPerformed(event);
            }
          };
        }
      };
      table.new Column("", Action2.class) {
        public Object getValue(PropertySource source) {
          return new DelProperty(source) {
            @Override
            public void actionPerformed(ActionEvent event) {
              dlg.cancel();
              super.actionPerformed(event);
            }
          };
        }
      };
      table.setRows(sources);

      if (dlg.show()<1)
        return;
      
    }

    // grab window now as current source component for e might go away during dialog/edit ops
    Window win = DialogHelper.getWindow(e);
    
    // create new source
    CreateXReference create = new CreateXReference(property, "SOUR");
    create.actionPerformed(e);
    PropertySource source = (PropertySource)create.getReference();
    if (source!=null)
      new Edit(source, true).actionPerformed(new ActionEvent(win, 0, ""));

  }
  
  private class Edit extends Action2 {
    
    private boolean deleteOnCancel;
    private PropertySource citation;
    
    public Edit(PropertySource citation, boolean deleteOnCancel) {
      
      this.deleteOnCancel = deleteOnCancel;
      
      setText(RESOURCES.getString("edit", Gedcom.getName("SOUR")));
      setTip(getText());
      setImage(Images.imgView);
      
      this.citation = citation;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      
      JTabbedPane tabs = new JTabbedPane();
      
      final BeanPanel citationPanel = new BeanPanel();
      citationPanel.setRoot(citation);
      tabs.add(RESOURCES.getString("citation"), citationPanel);
      
      final BeanPanel sourcePanel = new BeanPanel() {
        @Override
        protected JComponent createComponent(Property root, Property property, Cell cell, Set<String> beanifiedTags) {
          JComponent c = super.createComponent(root, property, cell, beanifiedTags);
          return c instanceof ReferencesBean || c instanceof NestedBlockLayout.Expander ? null : c;
        }
      };
      sourcePanel.setRoot(citation.getTargetEntity());
      tabs.add(Gedcom.getName("SOUR"), sourcePanel);
      
      GedcomDialog dlg = new GedcomDialog(citation.getGedcom(), getText(), DialogHelper.QUESTION_MESSAGE, tabs, Action2.okCancel(), e);
      if (0==dlg.show())
        citation.getGedcom().doMuteUnitOfWork(new UnitOfWork() {
          public void perform(Gedcom gedcom) throws GedcomException {
            sourcePanel.commit();
            citationPanel.commit();
          }
        });
      else 
        if (deleteOnCancel)
        citation.getGedcom().doMuteUnitOfWork(new UnitOfWork() {
          public void perform(Gedcom gedcom) throws GedcomException {
            Source source = (Source)citation.getTargetEntity();
            citation.getParent().delProperty(citation);
            if (!source.isConnected())
              gedcom.deleteEntity(source);
          }
        });
          
    }
  }

}
