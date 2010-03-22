/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

public final class ActionSaveLayout implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        StringBuilder sb = new StringBuilder("fenetres");
        Gedcom selected = App.center.getSelectedGedcom();

        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(this.getClass(),"DLG_ActionSaveLayout"),NotifyDescriptor.OK_CANCEL_OPTION);
        DialogDisplayer.getDefault().notify(nd);
        if (!nd.getValue().equals(NotifyDescriptor.OK_OPTION))
            return;

        WindowManager wm = WindowManager.getDefault() ;
        List<String> openedViews = new ArrayList<String>();

        for (GenjViewInterface gjvTc : GenjViewTopComponent.getMyLookup().lookupAll(GenjViewInterface.class)) {
            if (((GenjViewTopComponent)gjvTc).isOpened() &&  selected.equals(gjvTc.getGedcom())){
                App.LOG.info(gjvTc.getClass().getName()+": "+gjvTc.getMode().getName());
                Mode mode = gjvTc.getMode();
                gjvTc.setDefaultMode(mode);
                openedViews.add(gjvTc.getClass().getName());
            }
        }

        Preferences prefs = NbPreferences.forModule(GenjViewTopComponent.class);

        for (int i = 0; i<20; i++){
            if (prefs.get("openViews" + i, null) == null)
                break;
            prefs.remove(null);
        }
        for (int i = 0; i < openedViews.size(); i++) {
            String str = openedViews.get(i);
            prefs.put("openViews" + i, str);
        }
    }
}
