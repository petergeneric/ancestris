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
package genj.view;

import genj.edit.actions.Redo;
import genj.edit.actions.Undo;
import genj.print.PrintRegistry;
import genj.print.PrintTask;
import genj.print.Printer;
import genj.util.EnvironmentChecker;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

/**
 * A swing container for a view widget 
 */
/*package*/ class ViewContainer extends JPanel {
  
  private final static String
    ACC_CLOSE = "ctrl W",
    ACC_UNDO = "ctrl Z",
    ACC_REDO = "ctrl Y";

  /** the toolbar we're using */
  private JToolBar bar;
  
  /** the view's handle */
  private ViewHandle viewHandle;
  
  /** 
   * Constructor
   */
  /*package*/ ViewContainer(ViewHandle handle) {
    
    ViewManager mgr = handle.getManager();
    
    // remember
    viewHandle = handle;
    JComponent view = viewHandle.getView();
    
    // setup layout
    setLayout(new BorderLayout());
    add(view, BorderLayout.CENTER);
    
    // .. factory accelerators
    for (Iterator it = mgr.keyStrokes2factories.keySet().iterator(); it.hasNext();) {
      String keystroke = it.next().toString();
      ViewFactory factory = (ViewFactory)mgr.keyStrokes2factories.get(keystroke);
      ActionOpen open = new ActionOpen(factory);
      open.setAccelerator(keystroke);
      open.install(view, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // ... default view accelerators (overwriting anything else)
    new ActionClose().setAccelerator(ACC_CLOSE).install(view, JComponent.WHEN_IN_FOCUSED_WINDOW);
    new Undo(viewHandle.getGedcom(), true).setAccelerator(ACC_UNDO).install(view, JComponent.WHEN_IN_FOCUSED_WINDOW);
    new Redo(viewHandle.getGedcom(), true).setAccelerator(ACC_REDO).install(view, JComponent.WHEN_IN_FOCUSED_WINDOW);

    // done
  }
  
  /**
   * Install toolbar at time of add
   */
  public void addNotify() {
    
    // let super do its things
    super.addNotify();
    
    // only if ToolBarSupport and no bar installed
    JComponent view = viewHandle.getView();
    if (!(view instanceof ToolBarSupport)||bar!=null) 
      return;

    // Create one
    bar = new JToolBar();
    
    // Fill Toolbar
    ((ToolBarSupport)view).populate(bar);
    if (EnvironmentChecker.getProperty(this, "genj.view.toolbarproblem", null, "checking for switch to not use glue in toolbar")==null)
      bar.add(Box.createGlue());

    // add our buttons     
    ButtonHelper bh = new ButtonHelper().setContainer(bar);
    bh.setInsets(0);

    // .. a button for editing the View's settings
    if (SettingsWidget.hasSettings(view))
      bh.create(new ActionOpenSettings());
    
    // .. a button for printing View
    try {
      Printer printer = (Printer)Class.forName(view.getClass().getName()+"Printer").newInstance();
      try {
        printer.setView(view);
        PrintTask print = new PrintTask(printer, viewHandle.getTitle(), view,  new PrintRegistry(viewHandle.getRegistry(), "print"));
        print.setTip(ViewManager.RESOURCES, "view.print.tip");
        bh.create(print);
      } catch (Throwable t) {
        ViewManager.LOG.log(Level.WARNING, "can't setup printing for printer "+printer.getClass().getName());
        ViewManager.LOG.log(Level.FINE, "throwable while setting up "+printer.getClass().getName(), t);
      }
    } catch (Throwable t) {
    }

    // .. a button for closing the View
    bh.create(new ActionClose());

    // add it
    add(bar, viewHandle.getRegistry().get("toolbar", BorderLayout.WEST));
    
    // done
  }
  
  /**
   * When adding components we fix a Toolbar's sub-component's orientation
   */
  protected void addImpl(Component comp, Object constraints, int index) {
    // restore toolbar orientation?
    if (comp==bar) {
      // remember
      viewHandle.getRegistry().put("toolbar", constraints.toString());
      // find orientation
      int orientation = SwingConstants.HORIZONTAL;
      if (BorderLayout.WEST.equals(constraints)||BorderLayout.EAST.equals(constraints))
        orientation = SwingConstants.VERTICAL;
      // fix orientation for toolbar
      bar.setOrientation(orientation);
      // toolbar o.k.
    }
    // go ahead with super
    super.addImpl(comp, constraints, index);
    // done
  }

  /**
   * Action - close view
   */
  private class ActionClose extends Action2 {
    /** constructor */
    protected ActionClose() {
      setImage(Images.imgClose);
      setTip(ViewManager.RESOURCES, "view.close.tip");
    }
    /** run */
    protected void execute() {
      viewHandle.getManager().closeView(viewHandle);
    }
  } //ActionClose
  
  /**
   * Action - open the settings of a view
   */
  private class ActionOpenSettings extends Action2 {
    /** constructor */
    protected ActionOpenSettings() {
      super.setImage(Images.imgSettings).setTip(ViewManager.RESOURCES, "view.settings.tip");
    }
    /** run */
    protected void execute() {
      viewHandle.getManager().openSettings(viewHandle);
    }
  } //ActionOpenSettings
  
  /**
   * Open a view or bring to front
   */
  private class ActionOpen extends Action2 {
    private ViewFactory factory;
    /** constructor */
    private ActionOpen(ViewFactory factory) {
      this.factory = factory;
    }
    /** run */
    protected void execute() {
      viewHandle.getManager().openView(viewHandle.getGedcom(), factory, 1);
    }
  }
    
} //ViewWidget
