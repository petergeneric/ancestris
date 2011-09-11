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

import genj.option.Option;
import genj.option.OptionProvider;
import genj.option.PropertyOption;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;

import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * Global options for editor
 */
public class Options extends OptionProvider {
  
  private final static Resources RES = Resources.get(Options.class);
  
  /** option - whether changes are auto commit */
  public boolean isAutoCommit = false;
  
  /** option - whether to split jurisdictions into their components when editing places */
  public boolean isSplitJurisdictions = false;
  
  /** option - whether we correct names */
  public int correctName = 0;
  
  public final static String[] correctNames = { 
    RES.getString("option.correctName.none"),
    RES.getString("option.correctName.caps"),
    RES.getString("option.correctName.allcaps")
  };
  
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
  public List<? extends Option> getOptions() {
    return PropertyOption.introspect(instance);
  }
  /**
   * Ask the user whether he wants to commit changes
   */
  /*package*/ boolean isCommitChanges() {
    // check for auto commit
    if (Options.getInstance().isAutoCommit)
      return true;

      JCheckBox auto = new JCheckBox(RES.getString("confirm.autocomit"));
      auto.setFocusable(false);

      int rc = DialogHelper.openDialog(RES.getString("confirm.keep.changes"),
          DialogHelper.QUESTION_MESSAGE, new JComponent[] {
            new JLabel(RES.getString("confirm.keep.changes")),
            auto
          },
          Action2.yesNo(),
          this
      );

      if (rc!=0) {
            return false;
      }

      Options.getInstance().isAutoCommit = auto.isSelected();
            return true;

    }


} //Options
