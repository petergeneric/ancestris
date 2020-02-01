/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.tipoftheday;

import java.awt.Dialog;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.jdesktop.swingx.tips.DefaultTipOfTheDayModel;
import org.jdesktop.swingx.tips.TipLoader;
import org.jdesktop.swingx.tips.TipOfTheDayModel;
import org.jdesktop.swingx.tips.TipOfTheDayModel.Tip;
import org.openide.util.NbPreferences;

/**
 *
 * @author lemovice
 */
public class TipOfTheDay {

    public static final String PREFERENCE_KEY = "ShowTipOnStartup";
    private static final Logger logger = Logger.getLogger(TipOfTheDay.class.getName());
    TipOfTheDayDialog tipOfTheDayDialog;

    public TipOfTheDay(InputStream propertiesIn) {
        this(isStartupChoiceOption(), propertiesIn);
    }

    /**
     *
     * @param showingOnStartup
     * @param propertiesIn
     */
    public TipOfTheDay(boolean showingOnStartup, InputStream propertiesIn) {
        if (showingOnStartup) {
            tipOfTheDayDialog = new TipOfTheDayDialog(loadModel(propertiesIn));
            Dialog createDialog = tipOfTheDayDialog.createDialog();
            createDialog.toFront();
            createDialog.setVisible(showingOnStartup);
        }
    }

    private static TipOfTheDayModel loadModel(InputStream propertiesIn) {
        try {
            //Load the tips into the tip loader:
            Properties properties = new Properties();
            properties.load(propertiesIn);
            return TipLoader.load(properties);
        } catch (IOException ex) {
            return new DefaultTipOfTheDayModel(new Tip[0]);
        }
    }

    //Store whether the tip dialog should be shown at start up:
    private static void setStartupChoiceOption(boolean val) {
        NbPreferences.forModule(TipOfTheDay.class).putBoolean("StartUpPref", val);
        logger.log(Level.INFO, "Show Tips on Startup: ", val);
    }

    //Return whether the tip dialog should be shown at start up:
    private static boolean isStartupChoiceOption() {
        Preferences modulePreferences = NbPreferences.forModule(TipOfTheDay.class);
        boolean s = modulePreferences.getBoolean(PREFERENCE_KEY, true);
        return s;
    }

    //Get the tip to be shown,
    private static int getStartingTipLocation() {
        Preferences pref = NbPreferences.forModule(TipOfTheDay.class);
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
        NbPreferences.forModule(TipOfTheDay.class).putInt("StartTipPref", nextTip);

        logger.log(Level.INFO, "Total number of tips: {0}", tot);
        logger.log(Level.INFO, "Current tip location: ", loc);
        logger.log(Level.INFO, "Future tip location: ", nextTip);
    }
}
