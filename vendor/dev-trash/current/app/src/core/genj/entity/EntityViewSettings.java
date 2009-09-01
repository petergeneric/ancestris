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
package genj.entity;

import genj.renderer.BlueprintList;
import genj.renderer.BlueprintManager;
import genj.view.Settings;
import genj.view.ViewManager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;


/**
 * The settings editor for the EntityView
 */
public class EntityViewSettings extends JTabbedPane implements Settings {
  
  /** the entity view */
  private EntityView entityView; 
  
  /** the scheme editor */
  private BlueprintList blueprintList;
  
  /** Checkboxes */
  private JCheckBox 
    checkAntialiasing = new JCheckBox(EntityView.resources.getString("antialiasing" ));

  /**
   * @see genj.view.Settings#init(genj.view.ViewManager)
   */
  public void init(ViewManager manager) {
    
    // main options
    Box main = new Box(BoxLayout.Y_AXIS);

    checkAntialiasing.setToolTipText(EntityView.resources.getString("antialiasing.tip"));
    main.add(checkAntialiasing);
    
    // blueprint options
    blueprintList = new BlueprintList(BlueprintManager.getInstance());
    
    // add those tabs
    add(EntityView.resources.getString("page.main")      , main);
    add(EntityView.resources.getString("page.blueprints"), blueprintList);
    
    // done
  }
  
  
  /**
   * @see genj.view.Settings#setView(javax.swing.JComponent, genj.view.ViewManager)
   */
  public void setView(JComponent view) {

    // keep the view
    entityView = (EntityView)view;
    
    // characteristics
    blueprintList.setGedcom(entityView.gedcom);    
    
  }
  
  /**
   * Helper that wraps an entity type
   */
  private Integer wrap(int type) {
    return new Integer(type);
  }
  
  /**
   * Helper that unwraps an entity type
   */
  private int unwrap(Object type) {
    return ((Integer)type).intValue();
  }

  /**
   * @see genj.app.ViewSettingsWidget#apply()
   */
  public void apply() {
    entityView.setAntialiasing(checkAntialiasing.isSelected());
    entityView.setBlueprints(blueprintList.getSelection());
  }

  /**
   * @see genj.app.ViewSettingsWidget#reset()
   */
  public void reset() {
    checkAntialiasing.setSelected(entityView.isAntialiasing());
    blueprintList.setSelection(entityView.getBlueprints());
  }

  /**
   * @see genj.view.Settings#getEditor()
   */
  public JComponent getEditor() {
    return this;
  }

} //EntityViewSettings
