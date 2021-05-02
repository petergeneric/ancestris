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
package genj.edit.beans;

import ancestris.core.actions.AncestrisActionProvider;
import ancestris.view.ExplorerHelper;
import ancestris.view.PropertyProvider;
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.PropertyChange;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintManager;
import genj.renderer.ChooseBlueprintAction;

import java.awt.BorderLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;

import javax.swing.JLabel;
import org.openide.nodes.Node;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : ENTITY
 */
public class EntityBean extends PropertyBean implements AncestrisActionProvider, PropertyProvider {

  private Preview preview;
  private JLabel changed;

  /**
   * Nothing to edit
   */  
  public boolean isEditable() {
    return false;
  }

  public EntityBean() {
    
    preview = new Preview();
    changed = new JLabel();
    
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, preview);
    add(BorderLayout.SOUTH, changed);
    
    new ExplorerHelper(this).setPopupAllowed(true);
    setToolTipText(RESOURCES.getString("entity.tooltip"));
  }
  
  @Override
  protected void commitImpl(Property property) {
    // noop
  }
  
  /**
   * Set context to edit
   */
  public void setPropertyImpl(Property prop) {

    // show it
    Entity entity = (Entity)prop;
    preview.setEntity(entity);

    // add change date/time
    changed.setVisible(false);
    if (entity!=null) {
      PropertyChange change = entity.getLastChange();
      if (change!=null)
        changed.setText(RESOURCES.getString("entity.change", change.getDateDisplayValue(), change.getTimeDisplayValue() ));      
        changed.setVisible(true);
    }
    
    // Done
  }

    public List<Action> getActions(boolean hasFocus, Node[] nodes) {
        if (!hasFocus) {
            return new ArrayList<Action>();
        }
        List<Action> actions = new ArrayList<Action>();
        String tag = property.getEntity().getTag();
        Blueprint bp = BlueprintManager.getInstance().getBlueprint(tag, REGISTRY.get("blueprint.entity" + tag, ""));
        actions.add(new ChooseBlueprintAction(property.getEntity(), bp) {
            @Override
            protected void commit(Entity recipient, Blueprint blueprint) {
                REGISTRY.put("blueprint.entity" + blueprint.getTag(), blueprint.getName());
                Entity entity = (Entity) property;
                preview.setEntity(entity);
            }
        });
        return actions;
    }

    @Override
    public Property provideVisibleProperty(Point point) {
        return preview.getEntity();
    }
  
} //ProxyEntity
