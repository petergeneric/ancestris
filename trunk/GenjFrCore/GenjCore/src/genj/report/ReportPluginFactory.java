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
package genj.report;

import genj.app.PluginFactory;
import genj.app.Workbench;

/**
 * A plugin for adding report tools
 */
public class ReportPluginFactory implements PluginFactory {
  
  private static ReportPlugin instance;
  
  /** factory */
  public Object createPlugin(Workbench workbench) {
    if (instance==null)
      instance = new ReportPlugin(workbench);
    return instance;
  }
  
  public static ReportPlugin getInstance() {
    return instance;
  }

}
