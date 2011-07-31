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
package genj.edit.actions;

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import genj.util.swing.DialogHelper;

import javax.swing.Action;
import javax.swing.JComponent;

/**
 * Editing dialog for a gedcom context that auto-dismisses on edit
 */
public class GedcomDialog extends DialogHelper.Dialog {
  
  private Gedcom gedcom;
  private GedcomListener listener = new GedcomListenerAdapter() {
    public void gedcomWriteLockAcquired(Gedcom gedcom) {
      cancel();
    }
  };
  
  public GedcomDialog(Gedcom gedcom, String title, int messageType, final JComponent content, Action[] actions, Object source) {
    super(title, messageType, content, actions, source);
    this.gedcom = gedcom;
  }
  
  @Override
  public int show() {
    try {
      gedcom.addGedcomListener(listener);
      return super.show();
    } finally {
      gedcom.removeGedcomListener(listener);
    }

  }
}