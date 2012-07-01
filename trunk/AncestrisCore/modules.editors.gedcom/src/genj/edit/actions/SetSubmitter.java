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

import java.awt.event.ActionEvent;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Submitter;

/**
 * Set the submitter of a gedcom file
 */
public class SetSubmitter extends AbstractChange {

    /** the submitter */
    private Submitter submitter;
    
    /**
     * Constructor
     */
    public SetSubmitter(Submitter sub) {
      super(sub.getGedcom(), Gedcom.getEntityImage(Gedcom.SUBM), resources.getString("submitter", sub.getGedcom().getName()));
      submitter = sub;
      if (sub.getGedcom().getSubmitter()==submitter) 
        setEnabled(false);
    }

    /**
     * set the submitter
     */
    protected Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {
      submitter.getGedcom().setSubmitter(submitter);
      return null;
    }

} //SetSubmitter

