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
import genj.gedcom.Grammar;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.GridBagHelper;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;
import genj.util.swing.ListSelectionWidget;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Class for providing ViewInfo information to a ViewEditor
 */
public class TableViewSettings extends JPanel {

  private PathTreeWidget      pathTree;
  private ListSelectionWidget<TagPath> pathList;
  private Resources           resources = Resources.get(this);

  public TableViewSettings(final TableView view) {
    
    final Grammar grammar = view.getGedcom()!=null ? view.getGedcom().getGrammar() : Grammar.V55;
    
    // Create!
    GridBagHelper gh = new GridBagHelper(this);

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
    String tag = view.getMode().getTag();
    TagPath[] selectedPaths = view.getMode(tag).getPaths();
    TagPath[] usedPaths     = grammar.getAllPaths(tag, Property.class);
    pathTree.setGrammar(grammar);
    pathTree.setPaths(usedPaths, selectedPaths);
    pathTree.addListener(plistener);

    // Up/Down of ordering
    final Move up = new Move(true);
    final Move dn = new Move(false);
    final Del del = new Del();
    
    // List of TagPaths
    pathList = new ListSelectionWidget<TagPath>() {
      protected ImageIcon getIcon(TagPath path) {
	      return grammar.getMeta(path).getImage();
      }
    };
    pathList.setChoices(selectedPaths);
    pathList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        TagPath path = pathList.getChoice(e.getPoint());
        if (path!=null&&e.getClickCount()==2) 
          pathTree.setSelected(path, false);
      }
    });
    pathList.addSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        // update actions
        int i = pathList.getSelectedIndex();
        up.setEnabled(i>0);
        dn.setEnabled(i>=0&&i<pathList.getChoices().size()-1);
        del.setEnabled(i>=0);
      }
    });
    pathList.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        // commit selected choices
        List<TagPath> choices = pathList.getChoices();
        view.getMode().setPaths(choices.toArray(new TagPath[choices.size()]));
      }      
    });

    // Layout
    gh.add(new JLabel(resources.getString("info.columns")),0,0,4,1, GridBagHelper.FILL_HORIZONTAL);
    gh.add(pathTree                                       ,0,1,4,1,GridBagHelper.GROWFILL_BOTH);

    gh.add(new JLabel(resources.getString("info.order"))  ,0,2,4,1, GridBagHelper.FILL_HORIZONTAL);
    gh.add(pathList                                       ,0,3,4,1,GridBagHelper.GROWFILL_BOTH);
    gh.add(new JButton(up)                                ,0,4,1,1,GridBagHelper.FILL_HORIZONTAL);
    gh.add(new JButton(dn)                                ,1,4,1,1,GridBagHelper.FILL_HORIZONTAL);
    gh.add(new JButton(del)                               ,2,4,1,1,GridBagHelper.FILL_HORIZONTAL);

  }

  /**
   * Action - ActionUpDown
   */
  private class Move extends Action2 {
    /** up or down */
    private boolean up;
    /** constructor */
    protected Move(boolean up) {
      this.up=up;
      setEnabled(false);
      if (up) setText(resources, "info.up");
      else setText(resources, "info.down");
    }
    /** run */
    public void actionPerformed(java.awt.event.ActionEvent e) {
      int i = pathList.getSelectedIndex();
      if (up)
        pathList.swapChoices(i,i-1);
      else 
        pathList.swapChoices(i,i+1);
    }
  }
  
  /**
   * Action - ActionUpDown
   */
  private class Del extends Action2 {
    /** constructor */
    protected Del() {
      setEnabled(false);
      setText(resources, "info.del");
    }
    /** run */
    public void actionPerformed(java.awt.event.ActionEvent e) {
      pathTree.setSelected(pathList.getSelectedChoice(), false);
    }
  } //ActionUpDown
}
