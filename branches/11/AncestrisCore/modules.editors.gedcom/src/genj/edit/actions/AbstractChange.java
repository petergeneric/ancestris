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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.edit.actions;

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.core.resources.Images;
import ancestris.util.swing.DialogManager;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import genj.util.Resources;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * ActionChange - change the gedcom information
 */
public abstract class AbstractChange extends AbstractAncestrisContextAction {

    /** resources */
    /* package */ final static Resources resources = Resources.get(AbstractChange.class);
    private Context selection;
    /** image *new* */
    protected final static ImageIcon imgNew = Images.imgNew;
    private JLabel confirm;
    private JButton confirmButton;

    /**
     * Returns the confirmation message - null if none
     */
    protected String getConfirmMessage() {
        return null;
    }

    protected String getHTMLMessage() {
        return "<html>" + getConfirmMessage() + "</html>";
    }
    
    
    /**
     * Return the dialog content to show to the user
     */
    protected JPanel getDialogContent() {
        JPanel result = new JPanel(new NestedBlockLayout("<col><text wx=\"1\" wy=\"1\"/></col>"));
        result.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        result.add(getConfirmComponent());
        return result;
    }

    protected JComponent getConfirmComponent() {
        if (confirm == null) {
            confirm = new JLabel(getHTMLMessage());
        }
        return confirm; //new JScrollPane(confirm);
    }
    
    protected void setConfirmEnabled(boolean set) {
        confirmButton.setEnabled(set);
    }

    /**
     * @see genj.util.swing.AbstractAncestrisAction#execute()
     */
    @Override
    public void actionPerformedImpl(final ActionEvent event) {
        // cleanup first
        confirm = null;

        // prepare confirmation message for user
        String msg = getConfirmMessage();
        if (msg != null) {
            confirmButton = new JButton(resources.getString("confirm.proceed", getText().toLowerCase()));  // NOI18N

            // Recheck with the user
            int msgType = (this instanceof DelProperty) ? DialogManager.WARNING_MESSAGE : DialogManager.PLAIN_MESSAGE;
            Object rc = DialogManager.create(getText(), getDialogContent())
                    .setOptions(new Object[] { confirmButton, DialogManager.CANCEL_OPTION })
                    .setDialogId("confirm.proceed." + getClass())
                    .setMessageType(msgType)
                    .show();
            if (rc != confirmButton) {
                return;
            }
        }

        // do the change
        try {
            getGedcom().doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    selection = execute(gedcom, event);
                }
            });
        } catch (Throwable t) {
            DialogManager.createError(null, t.getMessage()).show();
        }

        // propagate selection
        if (selection != null) {
            SelectionDispatcher.fireSelection(event, selection);
        }

    }

    /**
     * perform the actual change
     */
    protected abstract Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException;
} //Change

