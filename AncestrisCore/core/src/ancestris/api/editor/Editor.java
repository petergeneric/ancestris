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
package ancestris.api.editor;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import genj.util.ChangeSupport;
import genj.view.ViewContext;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 * The base class for all the editors
 */
    public abstract class Editor extends JPanel {
  
  protected ChangeSupport changes = new ChangeSupport(this);
  protected List<Action> actions = new ArrayList<Action>();

      private String title = null;

  /** 
   * Accessor - current 
   */
  public abstract ViewContext getContext();
  
  /** 
   * Accessor - current 
   */
  public abstract void setContext(Context context);
  
  /**
   * commit changes
   */
  public abstract void commit() throws GedcomException;
  
  /**
   * Editor's actions
   */
  public List<Action> getActions() {
    return actions;
  }

  public void addChangeListener(ChangeListener listener) {
    changes.addChangeListener(listener);
  }
  
  public void removeChangeListener(ChangeListener listener) {
    changes.removeChangeListener(listener);
  }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle(){
        if (title != null)
        return title;
      return (getContext() == null?"":getContext().getText());
  }

    /**
     * Show editor in a dialog window
     * @return true if commit succesfull, false if not or cancel button has been clicked
     */
    public boolean showPanel(){
        NotifyDescriptor nd = new NotifyDescriptor(new JScrollPane(this), getTitle(), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        DialogDisplayer.getDefault().notify(nd);
        if (!nd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
            return false;
        }
        try {
            getContext().getGedcom().doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    commit();
                }
            });
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        return true;

    }

} //Editor
