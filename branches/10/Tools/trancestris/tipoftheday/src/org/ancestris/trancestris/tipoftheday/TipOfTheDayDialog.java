/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.tipoftheday;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jdesktop.swingx.tips.TipOfTheDayModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.*;

/**
 *
 * @author lemovice
 */
public class TipOfTheDayDialog implements ActionListener {

    public static final String PREFERENCE_KEY = "ShowTipOnStartup";
    public static final String NEXT_TIP_KEY = "NextTip";
    private TipOfTheDayPanel tipOfTheDayPanel = new TipOfTheDayPanel();
    private DialogDescriptor TipOfTheDayDialogDescriptor;
    private Dialog TipOfTheDayDialog;
    private ConfirmationCheckBox confirmationCheckBox;
    private Button previous;
    private Button next;
    private Button close;
    private TipOfTheDayModel model;
    private int currentTip = 0;

    public TipOfTheDayDialog(TipOfTheDayModel model) {
        this.model = model;
    }

    public Dialog createDialog() {
        confirmationCheckBox = new ConfirmationCheckBox(NbBundle.getMessage(this.getClass(), "TipOfTheDayDialog.CheckBox.Text"));

        previous = new Button(NbBundle.getMessage(this.getClass(), "TipOfTheDayDialog.Previous.Text"));
        previous.setActionCommand("Previous");
        // handle next button press
        previous.addActionListener(this);

        next = new Button(NbBundle.getMessage(this.getClass(), "TipOfTheDayDialog.Next.Text"));
        next.setActionCommand("Next");
        // handle next button press
        next.addActionListener(this);

        close = new Button(NbBundle.getMessage(this.getClass(), "TipOfTheDayDialog.Close.Text"));
        close.setActionCommand("Close");
        // handle next button press
        close.addActionListener(this);

        currentTip = NbPreferences.forModule(this.getClass()).getInt(NEXT_TIP_KEY, 0);

        setCurrentTip(currentTip);

        TipOfTheDayDialogDescriptor = new DialogDescriptor(
                tipOfTheDayPanel,
                NbBundle.getMessage(this.getClass(), "TipOfTheDayDialog.Title"),
                true,
                new Object[]{previous, next, close},
                close,
                DialogDescriptor.DEFAULT_ALIGN,
                (HelpCtx)null,
                null);
        TipOfTheDayDialogDescriptor.setAdditionalOptions(new Object[]{confirmationCheckBox});
        TipOfTheDayDialogDescriptor.setClosingOptions(new Object[]{close});
        TipOfTheDayDialog = DialogDisplayer.getDefault().createDialog(TipOfTheDayDialogDescriptor);
        confirmationCheckBox.setSelected(NbPreferences.forModule(this.getClass()).getBoolean(PREFERENCE_KEY, true));
        return TipOfTheDayDialog;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {

        NbPreferences.forModule(this.getClass()).putBoolean(PREFERENCE_KEY, confirmationCheckBox.isSelected());

        if (ae.getActionCommand().equals(next.getActionCommand())) {
            nextTip();
        } else if (ae.getActionCommand().equals(previous.getActionCommand())) {
            previousTip();
        } else if (ae.getActionCommand().equals(close.getActionCommand())) {
            int count = getModel().getTipCount();
            if (count == 0) {
                return;
            }

            currentTip += 1;
            if (currentTip >= count) {
                currentTip = 0;
            }
            NbPreferences.forModule(this.getClass()).putInt(NEXT_TIP_KEY, currentTip);
        }
    }

    public TipOfTheDayModel getModel() {
        return model;
    }

    public void setModel(TipOfTheDayModel model) {
        TipOfTheDayModel old = this.model;
        this.model = model;
    }

    public int getCurrentTip() {
        return currentTip;
    }

    /**
     * Sets the index of the tip to show
     *
     * @param currentTip
     * @throw IllegalArgumentException if currentTip is not within the bounds
     * [0, getModel().getTipCount()[.
     */
    public void setCurrentTip(int currentTip) {
        if (currentTip < 0 || currentTip >= getModel().getTipCount()) {
            throw new IllegalArgumentException(
                    "Current tip must be within the bounds [0, " + getModel().getTipCount()
                    + "[");
        }

        this.currentTip = currentTip;

        tipOfTheDayPanel.setText(getModel().getTipAt(currentTip).getTip().toString());

        System.out.println("Total tips: " + getModel().getTipCount());
        System.out.println("Tip location: " + currentTip);
        System.out.println("Tip: " + getModel().getTipAt(currentTip).getTip().toString());
    }

    /**
     * Shows the next tip in the list. It cycles the tip list.
     */
    public void nextTip() {
        int count = getModel().getTipCount();
        if (count == 0) {
            return;
        }

        int nextTip = currentTip + 1;
        if (nextTip >= count) {
            nextTip = 0;
        }
        setCurrentTip(nextTip);
    }

    /**
     * Shows the previous tip in the list. It cycles the tip list.
     */
    public void previousTip() {
        int count = getModel().getTipCount();
        if (count == 0) {
            return;
        }

        int previousTip = currentTip - 1;
        if (previousTip < 0) {
            previousTip = count - 1;
        }
        setCurrentTip(previousTip);
    }
}
