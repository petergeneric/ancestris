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

import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * A settings component 
 */
/*package*/ class SettingsWidget extends JPanel {
  
  /** cached settings */
  private static Map cache = new WeakHashMap();
  
  /** components */
  private JPanel pSettings,pActions;
  private ActionApply apply = new ActionApply();
  private ActionReset reset = new ActionReset();
  
  /** settings */
  private Settings settings;
  
  /** ViewManager */
  private ViewManager viewManager;
  
  /**
   * Constructor
   */
  protected SettingsWidget(ViewManager manager) {
    
    // remember
    viewManager = manager;
    
    // Panel for ViewSettingsWidget
    pSettings = new JPanel(new BorderLayout());

    // Panel for Actions
    JPanel pActions = new JPanel();

    ButtonHelper bh = new ButtonHelper().setContainer(pActions);
    
    bh.create(apply);
    bh.create(reset);
    bh.create(new ActionClose());

    // Layout
    setLayout(new BorderLayout());
    add(pSettings,"Center");
    add(pActions ,"South" );
    
    // done
  }

  /**
   * Sets the ViewSettingsWidget to display
   */
  protected void setView(ViewHandle handle) {
    
    // clear content
    pSettings.removeAll();
    
    // try to get settings
    settings = getSettings(handle.getView());
    if (settings!=null) {
      settings.setView(handle.getView());
      JComponent editor = settings.getEditor();
      editor.setBorder(new TitledBorder(handle.getTitle()));
      pSettings.add(editor, BorderLayout.CENTER);
      settings.reset();
    }
      
    // enable buttons
    apply.setEnabled(settings!=null);
    reset.setEnabled(settings!=null);
    
    // show
    pSettings.revalidate();
    pSettings.repaint();
    
    // done
  }

  /**
   * closes the settings
   */
  private class ActionClose extends Action2 {
    private ActionClose() {
      setText(ViewManager.RESOURCES, "view.close");
    }
    protected void execute() {
      WindowManager.getInstance(getTarget()).close("settings");
    }
  } //ActionClose
  
  /**
   * Applies the changes currently being done
   */
  private class ActionApply extends Action2 {
    protected ActionApply() { 
      setText(ViewManager.RESOURCES, "view.apply"); 
      setEnabled(false);
    }
    protected void execute() {
      settings.apply();
    }
  }

  /**
   * Resets any change being done
   */
  private class ActionReset extends Action2 {
    protected ActionReset() { 
      setText(ViewManager.RESOURCES, "view.reset"); 
      setEnabled(false);
    }
    protected void execute() {
      settings.reset();
    }
  }
  
  /**
   * Finds out whether given view has settings   */
  /*package*/ static boolean hasSettings(JComponent view) {
    try {
      if (Settings.class.isAssignableFrom(Class.forName(view.getClass().getName()+"Settings")))
      return true;
    } catch (Throwable t) {
    }
    return false;
  }
  
  /**
   * Gets settings for given view
   */
  /*package*/ Settings getSettings(JComponent view) {
    
    // known?
    Class viewType = view.getClass(); 
    Settings result = (Settings)cache.get(viewType);
    if (result!=null) return result;
    
    // create
    String type = viewType.getName()+"Settings";
    try {
      result = (Settings)Class.forName(type).newInstance();
      result.init(viewManager);
      cache.put(viewType, result);
    } catch (Throwable t) {
      result = null;
      ViewManager.LOG.log(Level.WARNING, "couldn't instantiate settings for "+view, t);
    }
    
    // done
    return result;
  }
  
} //SettingsWidget

