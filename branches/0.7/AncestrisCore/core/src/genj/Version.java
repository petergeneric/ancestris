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
package genj;

import ancestris.core.pluginservice.PluginInterface;

/**
 * Type that encapsulates the GenJ Version
 */
public class Version {

  /** Singleton reference */
  public static final Version singleton = new Version();
  public static final PluginInterface plugref = new ancestris.core.AncestrisCorePlugin();

  /**
   * Constructor
   */
  private Version() {

  }

  /**
   * Returns a text representation of the version
   */
  public String toString() {
    return getVersionString();
  }
  
  /**
   * The version number
   */
  public String getVersionString() {
    return plugref.getPluginVersion();
  }

  /**
   * The build text
   */
  public String getBuildString() {
    return plugref.getPluginVersion();
  }

  /**
   * Accessor for singleton Version
   */
  public static Version getInstance() {
    return singleton;
  }

}
