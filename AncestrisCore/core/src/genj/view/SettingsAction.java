/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2009 Nils Meier <nils@meiers.net>
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
package genj.view;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.util.swing.DialogManager;
import genj.util.Resources;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;

/**
 * A base action for settings on views
 */
public abstract class SettingsAction extends AbstractAncestrisAction {
  
  private final static Resources RESOURCES = Resources.get(SettingsAction.class);
  
  public SettingsAction() {
    setImage(Images.imgSettings);
    setTip(RESOURCES.getString("view.settings.tip"));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    
    JComponent editor = getEditor();
    
    DialogManager.create(RESOURCES.getString("view.settings.tip"),editor)
              .setDialogId(this.getClass().getName()+".settings")
              .setOptionType(DialogManager.OK_ONLY_OPTION)
              .show();
  }
  
  protected abstract JComponent getEditor();
  
}
