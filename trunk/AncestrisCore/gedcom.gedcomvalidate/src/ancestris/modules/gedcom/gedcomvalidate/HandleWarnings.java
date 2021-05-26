/**
 * Ancestris - http://www.ancestris.org (Formerly GenJ - GenealogyJ)
 *
 * Copyright (C) 2020 Ancestris
 * Author: Frederic Lapeyre <frederic@ancestris.org>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ancestris.modules.gedcom.gedcomvalidate;

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.util.GedcomUtilities;
import ancestris.util.swing.DialogManager;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Handles all entity Warnings
 * TODO : later transform this action into a more global "View Entity Warnings" (move the Cygnus code here probably) with tools to fix warnings.
 * - Open panel
 * - Manage warnings
 * @author frederic
 * 
 */
@ActionID(category = "Edit/Gedcom", id = "ancestris.modules.gedcom.gedcomvalidate")
@ActionRegistration(
        displayName = "#SolveAnomallies",
        lazy = false)
@ActionReference(path = "Ancestris/Actions/GedcomProperty", position = 525)
public final class HandleWarnings extends AbstractAncestrisContextAction {
    
    private Gedcom gedcom = null;
    private Entity entity = null;
    
    public HandleWarnings() {
        super();
        setImage("ancestris/modules/gedcom/gedcomvalidate/Warning.png");
        setText(NbBundle.getMessage(getClass(), "SolveAnomallies"));
        setTip(NbBundle.getMessage(getClass(), "SolveAnomallies_tip"));
    }

    @Override
    protected void contextChanged() {
        if (getContext() == null) {
            setEnabled(false);
            return;
        }
        gedcom = getContext().getGedcom();
        entity = getContext().getEntity();
        setEnabled(entity != null && !entity.isValid());
        super.contextChanged();
    }
 
    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        // For the moment, move entity value because should be empty apart from NOTEs
        // And it's better to be able to let the user fix it
        // Later will be done in a panel button
        try {
            entity.getGedcom().doUnitOfWork(new UnitOfWork() {
                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    // First offer to change entity type...
                    if (Gedcom.getEntityType(entity.getTag()) == null) {
                        ErrorPanel panel = new ErrorPanel();
                        Object o = DialogManager.create(
                                NbBundle.getMessage(getClass(), "SolveAnomallies_title", entity.getDisplayTitle()), panel)
                                .setMessageType(DialogManager.QUESTION_MESSAGE)
                                .setOptionType(DialogManager.OK_CANCEL_OPTION)
                                .setDialogId(getClass().getCanonicalName())
                                .show();
                        if (o == DialogManager.OK_OPTION) {
                            // Move entity to newly created entity and rebuild links
                            String newTag = panel.getTag();
                            Entity newEntity = gedcom.createEntity(newTag);
                            entity.moveEntityValue(entity.getTag());
                            GedcomUtilities.MergeEntities(newEntity, entity);
                            SelectionDispatcher.fireSelection(new Context(newEntity));
                        }
                    } else {
                        // Then offer to move value
                        entity.moveEntityValue(); // this move cannot be completely undone because setting value back to the envity will not be done (never mind).
                    }
                }
            });
        } catch (GedcomException ge) {
        }

        
    }
}
