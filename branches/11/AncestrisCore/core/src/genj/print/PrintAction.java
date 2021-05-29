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

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.util.swing.DialogManager;
import genj.util.Resources;
import genj.util.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintException;
import org.openide.DialogDescriptor;

/**
 * An action for printing
 */
public abstract class PrintAction extends AbstractAncestrisAction {
  
  private final static Resources RES = Resources.get(PrintAction.class);
  private final static ImageIcon IMG = new ImageIcon(PrintAction.class, "images/Print.png");
  private final static Logger LOG = Logger.getLogger("ancestris.print");
  
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
      DialogManager.createError(title, pe.getMessage()).show();
      return;
    }
    
    // show dialog
    PrintWidget widget = new PrintWidget(task);

    // show it in dialog
    String printChoice = new String(RES.getString("print"));
    Object choice = DialogManager.create(title, widget).
            setOptions(new Object[]{printChoice,DialogDescriptor.CANCEL_OPTION})
            .setDialogId("printaction")
            .show();
            
    // check choice
    if (choice != printChoice || task.getPages().width == 0 || task.getPages().height == 0)
      return;
    
    widget.commit();
    
    // print
    task.print();

    // FIXME setup progress dlg
    // progress = WindowManager.getInstance(owner).openNonModalDialog(null, title, WindowManager.INFORMATION_MESSAGE, new ProgressWidget(this, getThread()), AbstractAncestrisAction.cancelOnly(), owner);
  }

}
