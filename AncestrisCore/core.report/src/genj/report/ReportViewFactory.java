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

import ancestris.core.pluginservice.AncestrisPlugin;
import genj.util.Resources;
import genj.util.swing.ImageIcon;
import genj.view.View;
import genj.view.ViewFactory;


/**
 * The factory for the TableView
 */
public class ReportViewFactory implements ViewFactory {

  /*package*/ final static ImageIcon IMG = new ImageIcon(ReportViewFactory.class, "View");

  /**
   * Factory method - create instance of view
   */
  public View createView() {
      View reportView = new ReportView();
      AncestrisPlugin.register(reportView);
    return reportView;
  }
  
  /**
   * @see genj.view.ViewFactory#getImage()
   */
  public ImageIcon getImage() {
    return IMG;
  }
  
  /**
   * @see genj.view.ViewFactory#getTitle(boolean)
   */
  public String getTitle() {
    return Resources.get(this).getString("title");
  }

  @Override
  public String getTooltip() {
    return Resources.get(this).getString("tooltip");
  }
  
} //ReportViewFactory
