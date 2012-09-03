/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.ancestris.trancestris.application.utils.TipOfTheDay;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
    category = "Tools",
id = "org.ancestris.trancestris.application.actions.TipOfTheDayAction")
@ActionRegistration(
    displayName = "#CTL_TipOfTheDayAction")
@ActionReference(path = "Menu/Tools", position = 1450)
@Messages("CTL_TipOfTheDayAction=Tip Of the Day")
public final class TipOfTheDayAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        //Always show when menu item used:
        TipOfTheDay tipOfTheDay = new TipOfTheDay(true);
    }
}
