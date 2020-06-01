/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application.actions;

import java.awt.event.ActionEvent;
import org.ancestris.trancestris.application.Installer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.actions.BooleanStateAction;

@ActionID(
        category = "File",
        id = "org.ancestris.trancestris.application.actions.CheckFileOnStartupAction"
)
@ActionRegistration(
        displayName = "#CTL_CheckFileOnStartupAction"
)
@ActionReference(path = "Menu/File", position = 1450)
@Messages("CTL_CheckFileOnStartupAction=Check for new Bundles package on startup")
public final class CheckFileOnStartupAction extends BooleanStateAction {

    CheckFileOnStartupAction() {
        setBooleanState(NbPreferences.forModule(Installer.class).getBoolean("Check-New-File-On-Server", true));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setBooleanState(!getBooleanState());
        NbPreferences.forModule(Installer.class).putBoolean("Check-New-File-On-Server", getBooleanState());
    }

    @Override
    public String getName() {
        return Bundle.CTL_CheckFileOnStartupAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
