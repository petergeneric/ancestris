/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.gedcom.Gedcom;
import genj.util.Registry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;
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
        if (selected == null){
            return;
        }
        Preferences prefs = NbPreferences.forModule(GenjViewTopComponent.class);
        Object date = prefs.get("openViews.date", null) == null?
            new String(NbBundle.getMessage(this.getClass(), "TXT_ASL_never")):
            new Date(new Long(prefs.get("openViews.date", "0")));

        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(this.getClass(),"DLG_ActionSaveLayout",date),
                NbBundle.getMessage(this.getClass(),"TTL_ActionSaveLayout",selected.getName()),
                NotifyDescriptor.OK_CANCEL_OPTION);
        DialogDisplayer.getDefault().notify(nd);
        if (!nd.getValue().equals(NotifyDescriptor.OK_OPTION))
            return;

        WindowManager wm = WindowManager.getDefault() ;
        saveDefaultLayout(selected, prefs);
    }

    public void saveDefaultLayout(Gedcom gedcom, Preferences prefs){
        List<String> openedViews = new ArrayList<String>();

        for (GenjViewInterface gjvTc : GenjViewTopComponent.getMyLookup().lookupAll(GenjViewInterface.class)) {
            if (((GenjViewTopComponent)gjvTc).isOpened() &&  gedcom.equals(gjvTc.getGedcom())){
                App.LOG.info(gjvTc.getClass().getName()+": "+gjvTc.getMode().getName());
                Mode mode = gjvTc.getMode();
                gjvTc.setDefaultMode(mode);
                openedViews.add(gjvTc.getClass().getName());
            }
        }

        for (int i = 0; i<20; i++){
            if (prefs.get("openViews" + i, null) == null)
                break;
            prefs.remove(null);
        }
        for (int i = 0; i < openedViews.size(); i++) {
            String str = openedViews.get(i);
            prefs.put("openViews" + i, str);
        }
        prefs.put("openViews.date",System.currentTimeMillis()+"");
    }
    static void saveLayout(Gedcom gedcom) {
        List<String> openedViews = new ArrayList<String>();
        Registry prefs = App.getRegistry(gedcom);

        for (GenjViewInterface gjvTc : GenjViewTopComponent.getMyLookup().lookupAll(GenjViewInterface.class)) {
            if (((GenjViewTopComponent)gjvTc).isOpened() &&  gedcom.equals(gjvTc.getGedcom())){
                App.LOG.info(gjvTc.getClass().getName()+": "+gjvTc.getMode().getName());
                Mode mode = gjvTc.getMode();
                prefs.put(((GenjViewTopComponent)gjvTc).preferredID()+".dockMode", mode.getName());
                openedViews.add(gjvTc.getClass().getName());
            }
        }

        for (int i = 0; i<20; i++){
            if (prefs.get("openViews" + i, (String)null) == null)
                break;
            prefs.remove("openViews" + i);
        }
        for (int i = 0; i < openedViews.size(); i++) {
            String str = openedViews.get(i);
            prefs.put("openViews" + i, str);
        }
        prefs.put("openViews.date",System.currentTimeMillis()+"");
    }
}
