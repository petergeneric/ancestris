/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ancestris.trancestris.application.Installer;
import org.ancestris.trancestris.tipoftheday.TipOfTheDay;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Help",
        id = "org.ancestris.trancestris.application.actions.tipOfTheDayAction")
@ActionRegistration(displayName = "#CTL_tipOfTheDayAction")
@ActionReferences({
    @ActionReference(path = "Menu/Help", position = 350)
})
@Messages("CTL_tipOfTheDayAction=Tip of the day")
public final class tipOfTheDayAction implements ActionListener {

    private static final Logger logger = Logger.getLogger(Installer.class.getName());

    @Override
    public void actionPerformed(ActionEvent e) {
        //Load the tips into the tip loader:
        String tipsFileName = "resources/tips.properties";
        String selectedLocale = Locale.getDefault().getLanguage();

        if (selectedLocale.equals("en") == false) {
            tipsFileName = "resources/tips_" + selectedLocale + ".properties";
        }
        logger.log(Level.INFO, "selected tips {0}", tipsFileName);
        InputStream propertiesIn = Installer.class.getResourceAsStream(tipsFileName);
        if (propertiesIn == null) {
            logger.log(Level.INFO, "selected tips {0}", "resources/tips.properties");
            propertiesIn = Installer.class.getResourceAsStream("resources/tips.properties");
        }
        new TipOfTheDay(true, propertiesIn);
    }
}
