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
package genj.edit;

import java.util.List;

import genj.option.OptionProvider;
import genj.option.PropertyOption;

/**
 * Global options for editor
 */
public class Options extends OptionProvider {
  
  /** option - whether an editor should */
  public boolean isOpenEditor = true;
  
  /** option - whether changes are auto commit */
  public boolean isAutoCommit = false;
  
  /** option - whether to split jurisdictions into their components when editing places */
  public boolean isSplitJurisdictions = true;
  
  /** 'singleton' */
  private static Options instance = new Options();

  /**
   * 'singleton' access
   */
  public static Options getInstance() {
    return instance;
  }
  
  /**
   * provided options
   */
  public List getOptions() {
    return PropertyOption.introspect(instance);
  }

} //Options
