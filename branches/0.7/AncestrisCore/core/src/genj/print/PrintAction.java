/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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
package genj.print;

import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.PrintException;
import javax.swing.Action;

/**
 * An action for printing
 */
public abstract class PrintAction extends Action2 {
  
  private final static Resources RES = Resources.get(PrintAction.class);
  private final static ImageIcon IMG = new ImageIcon(PrintAction.class, "images/Print.png");
  private final static Logger LOG = Logger.getLogger("genj.print");
  
  private String title;
  
  /**
   * Constructor
   */
  public PrintAction(String title) {
    setText(RES.getString("print"));
    setTip(getText());
    setImage(IMG);
    this.title = RES.getString("title", title);
  }
  
  protected abstract PrintRenderer getRenderer();

  /**
   * do the print ui flow
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    
    PrintTask task;
    try {
      task = new PrintTask(title, getRenderer());
    } catch (PrintException pe) {
      LOG.log(Level.INFO, "can't setup print task", pe);
      DialogHelper.openDialog(title, DialogHelper.ERROR_MESSAGE, pe.getMessage(), Action2.okOnly(), e);
      return;
    }
    
    // show dialog
    PrintWidget widget = new PrintWidget(task);

    // prepare actions
    Action[] actions = { 
        new Action2(RES.getString("print")),
        Action2.cancel() 
    };
    
    // show it in dialog
    int choice = DialogHelper.openDialog(
        title, 
        DialogHelper.QUESTION_MESSAGE, 
        widget, actions, e);

    // check choice
    if (choice != 0 || task.getPages().width == 0 || task.getPages().height == 0)
      return;
    
    widget.commit();
    
    // print
    task.print();

    // FIXME setup progress dlg
    // progress = WindowManager.getInstance(owner).openNonModalDialog(null, title, WindowManager.INFORMATION_MESSAGE, new ProgressWidget(this, getThread()), Action2.cancelOnly(), owner);
  }

}
