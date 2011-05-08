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
package genj.report;

import genj.app.Workbench;
import genj.app.WorkbenchAdapter;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.Action2.Group;
import genj.view.ActionProvider;
import genj.view.View;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/** 
 * Plugin 
 */
public class ReportPlugin extends WorkbenchAdapter implements ActionProvider {
  
  private final static Resources RESOURCES = Resources.get(ReportPlugin.class);
  private final static int MAX_HISTORY = 5;

  private boolean showReportPickerOnOpen = true;
  
  private Workbench workbench;
  private Action2.Group workbenchActions = new Action2.Group(Resources.get(this).getString("report.reports"));
  
  public ReportPlugin(Workbench workbench) {
    this.workbench = workbench;
    
    workbench.addWorkbenchListener(this);

  }
  
    
  /**
   * actions we provide
   */
  public void createActions(Context context, Purpose purpose, Group result) {
    
    // nothing without gedcom
    if (context.getGedcom()==null)
      return;
    
    switch (purpose) {
      case TOOLBAR:
        break;
        
      case MENU:
        workbenchActions.clear();

        // Look through reports
        Map<String, Action2.Group> categories = new HashMap<String, Action2.Group>();
        for (Report report : ReportLoader.getInstance().getReports()) {
          try {
            ActionRun run = null;
            if (context.getEntity()!=null&&report.accepts(context.getEntity())!=null)
              run = new ActionRun(report.accepts(context.getEntity()), context.getEntity(), report);
            if (run==null&&report.accepts(context.getGedcom())!=null)
              run = new ActionRun(report.accepts(context.getGedcom()), context.getGedcom(), report);
            if (run!=null) {
              String cat = report.getCategory();
              Action2.Group catgroup = categories.get(cat);
              if (catgroup==null) {
                catgroup = new Action2.Group(cat, report.getIcon(), true);
                categories.put(cat, catgroup);
                workbenchActions.add(catgroup);
              }
              catgroup.add(run);
            }
          } catch (Throwable t) {
            ReportView.LOG.log(Level.WARNING, "Report "+report.getClass().getName()+" failed in accept()", t);
          }
        }
        
        result.add(workbenchActions);
        break;
        
      case CONTEXT:

        // props
        List<? extends Property> properties = context.getProperties();
        if (properties.size()>1) {
          Action2.Group group = new ActionProvider.PropertiesActionGroup(properties);
          getActions(properties, context.getGedcom(), group);
          result.add(group);
        } else if (properties.size()==1) {
          Action2.Group group = new ActionProvider.PropertyActionGroup(context.getProperty());
          getActions(context.getProperty(), context.getGedcom(), group);
          result.add(group);
        }
        
        // entities
        List<? extends Entity> entities = context.getEntities();
        if (entities.size()>1) {
          Action2.Group group = new ActionProvider.EntitiesActionGroup(entities);
          getActions(entities, context.getGedcom(), group);
          result.add(group);
        } else if (entities.size()==1) {
          Action2.Group group = new ActionProvider.EntityActionGroup(context.getEntity());
          getActions(context.getEntity(), context.getGedcom(), group);
          result.add(group);
        }
        
        // gedcom
        Action2.Group group = new ActionProvider.GedcomActionGroup(context.getGedcom());
        getActions(context.getGedcom(), context.getGedcom(), group);
        result.add(group);
        
        break;
    }

    // done
  }
  
  /**
   * Plugin actions for entities
   */
  public List<Action2> createActions(Property[] properties) {
    return getActions(properties, properties[0].getGedcom());
  }

  /**
   * Plugin actions for entity
   */
  public List<Action2> createActions(Entity entity) {
    return getActions(entity, entity.getGedcom());
  }

  /**
   * Plugin actions for gedcom
   */
  public List<Action2> createActions(Gedcom gedcom) {
    return getActions(gedcom, gedcom);
  }

  /**
   * Plugin actions for property
   */
  public List<Action2> createActions(Property property) {
    return getActions(property, property.getGedcom());
  }

  /**
   * collects actions for reports valid for given context
   */
  private List<Action2> getActions(Object context, Gedcom gedcom) {

    Action2.Group action = new Action2.Group("Reports", ReportViewFactory.IMG);
    getActions(context, gedcom, action);
    List<Action2> result = new ArrayList<Action2>();
    if (action.size()>0)
      result.add(action);
    return result;
    
  }
  
  private void getActions(Object context, Gedcom gedcom, Action2.Group group) {
  
    // Look through reports
    Map<String, Action2.Group> categories = new HashMap<String, Action2.Group>();
    for (Report report : ReportLoader.getInstance().getReports()) {
      try {
        String accept = report.accepts(context); 
        if (accept!=null) {
          ActionRun run = new ActionRun(accept, context, report);
          String cat = report.getCategory();
          if (cat==null)
            group.add(run);
          else {
            Action2.Group catgroup = categories.get(cat);
            if (catgroup==null) {
              catgroup = new Action2.Group(RESOURCES.getString("title")+" ("+cat+")", report.getIcon());
              categories.put(cat, catgroup);
            }
            catgroup.add(run);
          }
        }
      } catch (Throwable t) {
        ReportView.LOG.log(Level.WARNING, "Report "+report.getClass().getName()+" failed in accept()", t);
      }
    }
    
    for (Action2.Group cat : categories.values()) {
      group.add(cat);
    }
    
    // done
  }
  
  /**
   * Run a report
   */
  private class ActionRun extends Action2 {
    /** context */
    private Object context;
    /** report */
    private Report report;
    /** constructor */
    private ActionRun(Report report) {
      this(report.getName(), null, report);
    }
    /** constructor */
    private ActionRun(String txt, Object context, Report report) {
      // remember
      this.context = context;
      this.report = report;
      // show
      setText(txt);
      
      StringBuffer tip = new StringBuffer();
      tip.append("<html><body><table width=320><tr><td>");
      String info = report.getInfo();
      int br = info.indexOf("</p>");
      if (br>0) info = info.substring(0, br+4);
      tip.append(info);
      tip.append("</td></tr></table>");
      setTip(tip.toString());
    }
    
    /** callback */
    public void actionPerformed(ActionEvent event) {
      showReportPickerOnOpen = false;
      try {
        ReportView view = (ReportView)workbench.openView(ReportViewFactory.class);
        view.startReport(report, context);
      } finally {
        showReportPickerOnOpen = true;
      }
    }
  } //ActionRun

  public void viewClosed(Workbench workbench, View view) {
  }

  public void viewOpened(Workbench workbench, View view) {
    if (view instanceof ReportView) {
      if (showReportPickerOnOpen)
        ((ReportView)view).startReport();
    }
  }
  
  /*package*/ void setEnabled(boolean set) {
    if (workbenchActions!=null)
      workbenchActions.setEnabled(set);
  }

}
