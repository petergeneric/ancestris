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
package genj.option;

import genj.util.Registry;

import java.util.ArrayList;
import java.util.List;



/**
 * An option is simply a wrapped public field of a type
 * with meta-information (JavaBean 'light')
 */
public abstract class Option {

  private List listeners;

  private String category;

  /**
   * Accessor - category of this option
   * @return category or null
   */
  public String getCategory() {
    return category;
  }

  /**
   * Accessor - category of this option
   */
  public void setCategory(String set) {
    category = set;
  }

  /**
   * Accessor - name of this option
   */
  public abstract String getName();

  /**
   * Accessor - tool tip for this option
   */
  public abstract String getToolTip();

  /**
   * Restore option values from registry
   */
  public abstract void restore(Registry registry);

  /**
   * Persist option values to registry
   */
  public abstract void persist(Registry registry);

  /**
   * Create an editor
   */
  public abstract OptionUI getUI(OptionsWidget widget);

  /**
   * Add listener
   */
  public void addOptionListener(OptionListener listener) {
    if (listeners==null)
      listeners = new ArrayList(4);
    listeners.add(listener);
  }

  /**
   * Trigger for change notification
   */
  protected void fireChangeNotification() {
    if (listeners==null)
      return;
    for (int i = 0; i < listeners.size(); i++)
      ((OptionListener)listeners.get(i)).optionChanged(this);
  }

} //Option