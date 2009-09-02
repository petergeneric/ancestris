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

import genj.common.PathTreeWidget;
import genj.gedcom.Gedcom;
import genj.gedcom.Grammar;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.GridBagHelper;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.ListSelectionWidget;
import genj.view.Settings;
import genj.view.ViewManager;

import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Class for providing ViewInfo information to a ViewEditor
 */
public class TableViewSettings extends JPanel implements Settings {

  /** components */
  private JComboBox           cTypes;
  private PathTreeWidget      pathTree;
  private ListSelectionWidget pathList;
  private TableView           table;
  private Resources           resources = Resources.get(this);

  /**
   * @see genj.view.Settings#init(genj.view.ViewManager)
   */
  public void init(ViewManager manager) {

    // Create!
    GridBagHelper gh = new GridBagHelper(this);

    // Chooseable type
    cTypes = new JComboBox();

    for (int i=0;i<Gedcom.ENTITIES.length;i++) {
      cTypes.addItem(Gedcom.getName(Gedcom.ENTITIES[i],true));
    }
    cTypes.addActionListener(new ActionChooseEntity());

    // Tree of TagPaths
    pathTree = new PathTreeWidget();

    PathTreeWidget.Listener plistener = new PathTreeWidget.Listener() {
      // LCD
      /** selection notification */
      public void handleSelection(TagPath path, boolean on) {
        if (!on) {
          pathList.removeChoice(path);
        } else {
          pathList.addChoice(path);
        }
      }
      // EOC
    };
    pathTree.addListener(plistener);

    // List of TagPaths
    pathList = new ListSelectionWidget() {
      protected ImageIcon getIcon(Object choice) {
	      TagPath path = (TagPath)choice;
	      return Grammar.V55.getMeta(path).getImage();
      }
    };

    // Up/Down of ordering
    ButtonHelper bh = new ButtonHelper().setInsets(0);
    AbstractButton bUp   = bh.create(new ActionUpDown(true));
    AbstractButton bDown = bh.create(new ActionUpDown(false));
    
    // Layout
    gh.add(new JLabel(resources.getString("info.entities"))  ,0,1,1,1);
    gh.add(cTypes                  ,1,1,2,1,GridBagHelper.GROWFILL_HORIZONTAL);

    gh.add(new JLabel(resources.getString("info.columns"))   ,0,2,1,1);
    gh.add(pathTree                ,1,2,2,2,GridBagHelper.GROWFILL_BOTH);

    gh.add(new JLabel(resources.getString("info.order"))  ,0,4,1,1);
    gh.add(bUp                                            ,0,5,1,1,GridBagHelper.FILL_HORIZONTAL);
    gh.add(bDown                                          ,0,6,1,1,GridBagHelper.FILL_HORIZONTAL);
    gh.add(pathList                                       ,1,4,2,4,GridBagHelper.GROWFILL_BOTH);

    // Done
  }
  
  /**
   * @see genj.view.Settings#setView(javax.swing.JComponent, genj.view.ViewManager)
   */
  public void setView(JComponent view) {
    // remember
    table = (TableView)view;
    // switch type
    cTypes.setSelectedItem(Gedcom.getName(table.getMode().getTag(), true));
    // done
  }


  /**
   * Tells the ViewInfo to apply made changes
   */
  public void apply() {
    // Write columns by TagPaths
    String tag = Gedcom.ENTITIES[cTypes.getSelectedIndex()];
    List choices = pathList.getChoices();
    TagPath[] paths = (TagPath[])choices.toArray(new TagPath[choices.size()]);
    table.getMode(tag).setPaths(paths);
    // Done
  }

  /**
   * Tells the ViewInfo to reset made changes
   */
  public void reset() {

    // Reflect columns by TagPaths
    String tag = Gedcom.ENTITIES[cTypes.getSelectedIndex()];
    
    TagPath[] selectedPaths = table.getMode(tag).getPaths();
    TagPath[] usedPaths     = table.gedcom.getGrammar().getAllPaths(tag, Property.class);

    pathTree.setPaths(usedPaths, selectedPaths);
    pathList.setChoices(selectedPaths);

    // Done
  }
  
  /**
   * @see genj.view.Settings#getEditor()
   */
  public JComponent getEditor() {
    return this;
  }

  /**
   * Action - ActionChooseEntity
   */
  private class ActionChooseEntity extends Action2 {
    /** constructor */
    /** run */
    public void execute() {
      if (table==null) return;
      table.setMode(table.getMode(Gedcom.ENTITIES[cTypes.getSelectedIndex()]));
      reset();
    }
  } //ActionChooseEntity
  
  /**
   * Action - ActionUpDown
   */
  private class ActionUpDown extends Action2 {
    /** up or down */
    private boolean up;
    /** constructor */
    protected ActionUpDown(boolean up) {
      this.up=up;
      if (up) setText(resources, "info.up");
      else setText(resources, "info.down");
    }
    /** run */
    public void execute() {
      if (up)
        pathList.up();
      else 
        pathList.down();
    }
  } //ActionUpDown
  
}
