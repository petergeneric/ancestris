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
package genj.edit.actions;

import genj.edit.Images;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextAreaWidget;
import genj.view.ViewManager;
import genj.window.WindowManager;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * ActionChange - change the gedcom information
 */
public abstract class AbstractChange extends Action2 implements UnitOfWork {
  
  /** resources */
  /*package*/ final static Resources resources = Resources.get(AbstractChange.class);
  
  /** the gedcom we're working on */
  protected Gedcom gedcom;
  
  /** the manager in the background */
  protected ViewManager manager;
  
  /** image *new* */
  protected final static ImageIcon imgNew = Images.imgNewEntity;
  
  private JTextArea confirm;

  /**
   * Constructor
   */
  public AbstractChange(Gedcom ged, ImageIcon img, String text, ViewManager mgr) {
    gedcom = ged;
    manager = mgr;
    super.setImage(img);
    super.setText(text);
  }

  /**
   * Show a dialog for errors
   */  
  protected void handleThrowable(String phase, Throwable t) {
    // for a NPE I've seen a null message - better convert that to string here
    String message = ""+t.getMessage();
    // show it
    getWindowManager().openDialog("err", "Error", WindowManager.ERROR_MESSAGE, message, Action2.okOnly(), getTarget());
  }
  
  protected WindowManager getWindowManager() {
    return WindowManager.getInstance(getTarget());    
  }
  
  /** 
   * Returns the confirmation message - null if none
   */
  protected String getConfirmMessage() {
    return null;
  }
  
  /**
   * Return the dialog content to show to the user   */
  protected JPanel getDialogContent() {
    JPanel result = new JPanel(new NestedBlockLayout("<col><text wx=\"1\" wy=\"1\"/></col>"));
    result.add(getConfirmComponent());
    return result;
  }
  
  protected JComponent getConfirmComponent() {
    if (confirm==null) {
      confirm = new TextAreaWidget(getConfirmMessage(), 6, 40);
      confirm.setWrapStyleWord(true);
      confirm.setLineWrap(true);
      confirm.setEditable(false);
    }
    return new JScrollPane(confirm);
  }
  
  /** 
   * Callback to update confirm text
   */
  protected void refresh() {
    // might be no confirmation showing
    if (confirm!=null)
      confirm.setText(getConfirmMessage());
  }
  
  /**
   * @see genj.util.swing.Action2#execute()
   */
  protected void execute() {
    
    // prepare confirmation message for user
    String msg = getConfirmMessage();
    if (msg!=null) {
  
      // prepare actions
      Action[] actions = { 
          new Action2(resources.getString("confirm.proceed", getText())),
          Action2.cancel() 
      };
      
      // Recheck with the user
      int rc = getWindowManager().openDialog(getClass().getName(), getText(), WindowManager.QUESTION_MESSAGE, getDialogContent(), actions, getTarget() );
      if (rc!=0)
        return;
    }
        
    // do the change
    try {
      gedcom.doUnitOfWork(this);
    } catch (Throwable t) {
      getWindowManager().openDialog(getClass().getName(), null, WindowManager.ERROR_MESSAGE, t.getMessage(), Action2.okOnly(), getTarget());
    }
    
    // done
  }
  
  /**
   * perform the actual change
   */
  public abstract void perform(Gedcom gedcom) throws GedcomException;
  
} //Change

