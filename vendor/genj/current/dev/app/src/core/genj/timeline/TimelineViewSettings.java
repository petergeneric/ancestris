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
package genj.timeline;

import genj.almanac.Almanac;
import genj.gedcom.PropertyEvent;
import genj.gedcom.TagPath;
import genj.util.Resources;
import genj.util.swing.ColorsWidget;
import genj.util.swing.ImageIcon;
import genj.util.swing.ListSelectionWidget;
import genj.util.swing.NestedBlockLayout;
import genj.view.Settings;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;

/**
 * The ViewInfo representing settings of a TimelineView
 * +remember the event last visible in the middle
 * +colors
 *  ruler 
 *    background
 *    foreground
 *  content 
 *  background
 *  tag color
 *  txt color
 *  date color
 *  line color
 */
public class TimelineViewSettings extends JTabbedPane implements Settings {
  
  /** resources we use */
  private Resources resources = Resources.get(this);
  
  /** keeping track of timeline these settings are for */
  private TimelineView view;
  
  /** a widget for selecting paths to show */
  private ListSelectionWidget pathsList = new ListSelectionWidget() {
    protected ImageIcon getIcon(Object choice) {
      TagPath path = (TagPath)choice;
      return view.getModel().gedcom.getGrammar().getMeta(path).getImage();
    }
  };
  
  /** a widget for selecting almanac event libraries / categories */
  private ListSelectionWidget almanacCategories = new ListSelectionWidget() {
    protected String getText(Object choice) {
      return "<html><body>"+choice+"</body></html>";
    }
  };
  
  /** Checkbox for options */
  private JCheckBox 
    checkTags = new JCheckBox(resources.getString("info.show.tags" )),
    checkDates = new JCheckBox(resources.getString("info.show.dates")),
    checkGrid = new JCheckBox(resources.getString("info.show.grid" ));
  
  /** spinners */
  private JSpinner spinCmBefEvent, spinCmAftEvent;
     
  /** colorchooser for colors */
  private ColorsWidget colorWidget;
    
  /**
   * @see genj.view.Settings#init(genj.view.ViewManager)
   */
  public void init(ViewManager manager) {
    
    // create a panel for check and cm options
    JPanel panelOptions = new JPanel(new NestedBlockLayout(
        "<col><check gx=\"1\"/><check gx=\"1\"/><check gx=\"1\"/><row><label/><spin/></row><row><label/><spin/></row></col>"
        ));
    
    // ... checkboxes    
    panelOptions.add(checkTags);
    panelOptions.add(checkDates);
    panelOptions.add(checkGrid);
    
    spinCmBefEvent = createSpinner(TimelineView.MIN_CM_BEF_EVENT, TimelineView.MAX_CM_BEF_EVENT, resources.getString("info.befevent.tip"));
    panelOptions.add(new JLabel(resources.getString("info.befevent")));
    panelOptions.add(spinCmBefEvent);

    spinCmAftEvent = createSpinner(TimelineView.MIN_CM_AFT_EVENT, TimelineView.MAX_CM_AFT_EVENT, resources.getString("info.aftevent.tip"));
    panelOptions.add(new JLabel(resources.getString("info.aftevent")));
    panelOptions.add(spinCmAftEvent);
    
    // panel for main options
    JPanel panelMain = new JPanel(new BorderLayout());
    panelMain.add(new JLabel(resources.getString("info.events")), BorderLayout.NORTH);
    panelMain.add(pathsList   , BorderLayout.CENTER);
    panelMain.add(panelOptions, BorderLayout.SOUTH);
    
    // panel for history options
    JPanel panelEvents = new JPanel(new BorderLayout());
    panelEvents.add(almanacCategories, BorderLayout.CENTER);
    
    // color chooser
    colorWidget = new ColorsWidget();
    
    // add those tabs
    add(resources.getString("page.main")  , panelMain);
    add(resources.getString("page.colors"), colorWidget);
    add(resources.getString("page.almanac"), panelEvents);

    // done
  }
  
  private JSpinner createSpinner(double min, double max, String tip) {
    
    JSpinner result = new JSpinner(new SpinnerNumberModel(min, min, max, 0.1D));
    JSpinner.NumberEditor editor = new JSpinner.NumberEditor(result, "##0.0");
    result.setEditor(editor);
    result.addChangeListener(editor);
    result.setToolTipText(tip);
    return result;
  }

  /**
   * Tells the ViewInfo to apply made changes
   */
  public void apply() {
    
    // choosen EventTags
    view.getModel().setPaths(pathsList.getSelection());
    
    // checks
    view.setPaintTags(checkTags.isSelected());
    view.setPaintDates(checkDates.isSelected());
    view.setPaintGrid(checkGrid.isSelected());
    
    // sliders
    view.setCMPerEvents(
       ((Double)spinCmBefEvent.getModel().getValue()).doubleValue(), 
       ((Double)spinCmAftEvent.getModel().getValue()).doubleValue()
    );
    
    // colors
    Iterator colors = view.colors.keySet().iterator();
    while (colors.hasNext()) {
      String key = colors.next().toString();
      view.colors.put(key, colorWidget.getColor(key));
    }
    
    // almanac categories
    view.setAlmanacCategories(almanacCategories.getSelection());
    
    // Done
  }
  
  /**
   * @see genj.view.Settings#setView(javax.swing.JComponent, genj.view.ViewManager)
   */
  public void setView(JComponent viEw) {
    // remember
    view = (TimelineView)viEw;
  }


  /**
   * Tells the ViewInfo to reset made changes
   */
  public void reset() {
    
    // EventTags to choose from
    pathsList.setChoices(PropertyEvent.getTagPaths(view.getModel().gedcom));
    pathsList.setSelection(view.getModel().getPaths());
    
    // Checks
    checkTags.setSelected(view.isPaintTags());
    checkDates.setSelected(view.isPaintDates());
    checkGrid.setSelected(view.isPaintGrid());

    // sliders
    spinCmBefEvent.setValue(new Double(view.getCmBeforeEvents()));
    spinCmAftEvent.setValue(new Double(view.getCmAfterEvents()));
    
    // colors
    colorWidget.removeAllColors();
    Iterator keys = view.colors.keySet().iterator();
    while (keys.hasNext()) {
      String key = keys.next().toString();
      String name = resources.getString("color."+key);
      Color color = (Color)view.colors.get(key);
      colorWidget.addColor(key, name, color);
    }
    
    // almanac categories
    Almanac almanac = Almanac.getInstance();
    almanac.waitLoaded();
    List cats = almanac.getCategories();
    almanacCategories.setChoices(cats);
    almanacCategories.setSelection(view.getAlmanacCategories());
    
    // Done
  }
  
  /**
   * @see genj.view.Settings#getEditor()
   */
  public JComponent getEditor() {
    return this;
  }


} //TimelineViewSettings
