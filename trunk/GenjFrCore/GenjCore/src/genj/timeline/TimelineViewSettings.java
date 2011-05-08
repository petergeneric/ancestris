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
import genj.gedcom.Gedcom;
import genj.gedcom.Grammar;
import genj.gedcom.PropertyEvent;
import genj.gedcom.TagPath;
import genj.util.Resources;
import genj.util.swing.ColorsWidget;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.ListSelectionWidget;
import genj.util.swing.NestedBlockLayout;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The ViewInfo representing settings of a TimelineView
 */
public class TimelineViewSettings extends JTabbedPane {
  
  /** resources we use */
  private Resources resources = Resources.get(this);
  
  /** a widget for selecting paths to show */
  private ListSelectionWidget<TagPath> pathsList;
  
  /** a widget for selecting almanac event libraries / categories */
  private ListSelectionWidget<String> almanacsList;
  
  /** Checkbox for options */
  private JCheckBox checkTags,checkDates,checkGrid;
  
  /** spinners */
  private JSpinner spinCmBefEvent, spinCmAftEvent;
     
  /** colorchooser for colors */
  private ColorsWidget colorWidget;
  
  private Commit commit;
    
  /**
   * @see genj.view.Settings#init(genj.view.ViewManager)
   */
  TimelineViewSettings(final TimelineView view) {
    
    final Gedcom gedcom = view.getModel().getGedcom();

    commit = new Commit(view);

    // events to pick from
    pathsList = new ListSelectionWidget<TagPath>() {
      protected ImageIcon getIcon(TagPath path) {
        Grammar grammar = Grammar.V55;
        if (gedcom!=null)
          grammar = gedcom.getGrammar();
        return grammar.getMeta(path).getImage();
      }
    };
    if (gedcom!=null)
      pathsList.setChoices(PropertyEvent.getTagPaths(gedcom));
    pathsList.setCheckedChoices(view.getModel().getPaths());
    pathsList.addChangeListener(commit);

    // categories to select from
    almanacsList = new ListSelectionWidget<String>() {
      protected String getText(String choice) {
        return "<html><body>"+choice+"</body></html>";
      }
    };
    Almanac almanac = Almanac.getInstance();
    almanac.waitLoaded();
    List<String> cats = almanac.getCategories();
    almanacsList.setChoices(cats);
    almanacsList.setCheckedChoices(view.getAlmanacCategories());
    almanacsList.addChangeListener(commit);
    

    // create a panel for options
    JPanel panelOptions = new JPanel(new NestedBlockLayout(
        "<col><check gx=\"1\"/><check gx=\"1\"/><check gx=\"1\"/><row><label/><spin/></row><row><label/><spin/></row></col>"
        ));
    panelOptions.setOpaque(false);
    
    // ... checkboxes    
    checkTags = createCheck("info.show.tags", view.isPaintTags());
    checkDates = createCheck("info.show.dates", view.isPaintDates());
    checkGrid = createCheck("info.show.grid", view.isPaintGrid());
    panelOptions.add(checkTags);
    panelOptions.add(checkDates);
    panelOptions.add(checkGrid);
    
    spinCmBefEvent = createSpinner(TimelineView.MIN_CM_BEF_EVENT, view.getCmBeforeEvents(), TimelineView.MAX_CM_BEF_EVENT, "info.befevent.tip");
    panelOptions.add(new JLabel(resources.getString("info.befevent")));
    panelOptions.add(spinCmBefEvent);

    spinCmAftEvent = createSpinner(TimelineView.MIN_CM_AFT_EVENT, view.getCmAfterEvents(), TimelineView.MAX_CM_AFT_EVENT, "info.aftevent.tip");
    panelOptions.add(new JLabel(resources.getString("info.aftevent")));
    panelOptions.add(spinCmAftEvent);
    
    // panel for main options
    JPanel panelMain = new JPanel(new BorderLayout());
    panelMain.add(new JLabel(resources.getString("info.events")), BorderLayout.NORTH);
    panelMain.add(pathsList   , BorderLayout.CENTER);
    panelMain.add(panelOptions, BorderLayout.SOUTH);
    
    // color chooser
    colorWidget = new ColorsWidget();
    for (String key : view.colors.keySet()) 
      colorWidget.addColor(key, resources.getString("color."+key), view.colors.get(key));
    colorWidget.addChangeListener(commit);
    
    // layout 
    add(resources.getString("page.main")  , panelMain);
    add(resources.getString("page.colors"), colorWidget);
    add(resources.getString("page.almanac"), almanacsList);

    // done
  }
  
  private JCheckBox createCheck(String key, boolean on) {
    JCheckBox result = new JCheckBox(resources.getString(key), on);
    result.addActionListener(commit);
    result.setOpaque(false);
    return result;
  }

  private class Commit implements ChangeListener, ActionListener {
    
    private TimelineView view;
    
    private Commit(TimelineView view) {
      this.view = view;
    }

    public void stateChanged(ChangeEvent e) {
      actionPerformed(null);
    }
    
    public void actionPerformed(ActionEvent e) {
      
      // choosen EventTags
      view.getModel().setPaths(pathsList.getCheckedChoices());
      
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
      for (String key : view.colors.keySet()) 
        view.colors.put(key, colorWidget.getColor(key));
      
      // almanac categories
      view.setAlmanacCategories(almanacsList.getCheckedChoices());
    }
  }
  
  private JSpinner createSpinner(double min, double value, double max, String tip) {
    JSpinner result = new JSpinner(new SpinnerNumberModel(value, min, max, 0.1D));
    JSpinner.NumberEditor editor = new JSpinner.NumberEditor(result, "##0.0");
    result.setEditor(editor);
    result.addChangeListener(editor);
    result.addChangeListener(commit);
    result.setToolTipText(resources.getString(tip));
    return result;
  }

} //TimelineViewSettings
