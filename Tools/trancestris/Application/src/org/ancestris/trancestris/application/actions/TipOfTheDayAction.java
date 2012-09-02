/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.Properties;
import java.util.prefs.Preferences;
import org.jdesktop.swingx.JXTipOfTheDay;
import org.jdesktop.swingx.tips.TipLoader;
import org.jdesktop.swingx.tips.TipOfTheDayModel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

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
        try {
            final JXTipOfTheDay jXTipOfTheDay = new JXTipOfTheDay(loadModel());
            jXTipOfTheDay.setCurrentTip(getStartingTipLocation());
            jXTipOfTheDay.showDialog(null, new JXTipOfTheDay.ShowOnStartupChoice() {
                @Override
                public boolean isShowingOnStartup() {
                    //Always show when menu item used:
                    return true;
                }

                @Override
                public void setShowingOnStartup(boolean showOnStartup) {
                    //Store whether to show at start up next time:
                    setStartupChoiceOption(showOnStartup);
                    //Store next tip location, sending current and total tips:
                    setNextStartingTipLocation(jXTipOfTheDay.getCurrentTip(), jXTipOfTheDay.getModel().getTipCount());
                }
            });
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private TipOfTheDayModel loadModel() throws Exception {
        //Load the tips into the tip loader:
        InputStream propertiesIn = getClass().getResourceAsStream("tips.properties");
        Properties properties = new Properties();
        properties.load(propertiesIn);
        return TipLoader.load(properties);
    }

    //Store whether the tip dialog should be shown at start up:
    private static void setStartupChoiceOption(boolean val) {
        NbPreferences.forModule(TipOfTheDayAction.class).putBoolean("StartUpPref", val);
        System.out.println("Show Tips on Startup: " + val);
    }

    //Get the tip to be shown,
    //via the NbPreferences API:
    private static int getStartingTipLocation() {
        Preferences pref = NbPreferences.forModule(TipOfTheDayAction.class);
        //Return the first tip if pref is null:
        if (pref == null) {
            return 0;
            //Otherwise, return the tip found via NbPreferences API,
            //with '0' as the default:    
        } else {
            int s = pref.getInt("StartTipPref", 0);
            return s;
        }
    }

    //Set the tip that will be shown next time,
    //we receive the current tip and the total tips:
    private static void setNextStartingTipLocation(int loc, int tot) {

        int nextTip = 0;
        //Back to zero if the maximum is reached:
        if (loc + 1 == tot) {
            nextTip = 0;
            //Otherwise find the next tip and store it:
        } else {
            nextTip = loc + 1;
        }

        //Store the tip, via the NbPreferences API,
        //so that it will be stored in the NetBeans user directory:
        NbPreferences.forModule(TipOfTheDayAction.class).putInt("StartTipPref", nextTip);

        System.out.println("Total tips: " + tot);
        System.out.println("Current tip location: " + loc);
        System.out.println("Future tip location: " + nextTip);
    }
}
