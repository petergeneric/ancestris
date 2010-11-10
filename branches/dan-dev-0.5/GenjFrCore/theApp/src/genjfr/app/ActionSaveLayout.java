/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import ancestris.util.AncestrisPreferences;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Registry;
import genjfr.app.pluginservice.GenjFrPlugin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class ActionSaveLayout implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        StringBuilder sb = new StringBuilder("fenetres");
        Context selected = App.center.getSelectedContext(true);
        if (selected == null){
            return;
        }
//        Preferences prefs = NbPreferences.forModule(AncestrisTopComponent.class);
        AncestrisPreferences prefs = AncestrisPreferences.get(AncestrisTopComponent.class);
        Object date = prefs.get("openViews.date", (String)null) == null?
            new String(NbBundle.getMessage(this.getClass(), "TXT_ASL_never")):
            new Date(new Long(prefs.get("openViews.date", "0")));

        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(this.getClass(),"DLG_ActionSaveLayout",date),
                NbBundle.getMessage(this.getClass(),"TTL_ActionSaveLayout",selected.getGedcom().getName()),
                NotifyDescriptor.OK_CANCEL_OPTION);
        DialogDisplayer.getDefault().notify(nd);
        if (!nd.getValue().equals(NotifyDescriptor.OK_OPTION))
            return;

        WindowManager wm = WindowManager.getDefault() ;
        saveDefaultLayout(selected.getGedcom(), prefs);
    }

    public void saveDefaultLayout(Gedcom gedcom, AncestrisPreferences prefs){
        List<String> openedViews = new ArrayList<String>();

        for (GenjViewInterface gjvTc : GenjFrPlugin.lookupAll(GenjViewInterface.class)) {
            if (((AncestrisTopComponent)gjvTc).isOpened() &&  gedcom.equals(gjvTc.getGedcom())){
                App.LOG.info(gjvTc.getClass().getName()+": "+gjvTc.getMode().getName());
                Mode mode = gjvTc.getMode();
                gjvTc.setDefaultMode(mode);
                openedViews.add(gjvTc.getClass().getName());
            }
        }

        prefs.put("openViews", openedViews.toArray());
        prefs.put("openViews.date",System.currentTimeMillis()+"");
    }
    public static void saveLayout(Gedcom gedcom) {
        List<String> openedViews = new ArrayList<String>();
        List<String> focusViews = new ArrayList<String>();
        Registry prefs = App.getRegistry(gedcom);

        TopComponent tcHasFocus = TopComponent.getRegistry().getActivated();
        
        for (GenjViewInterface gjvTc : GenjFrPlugin.lookupAll(GenjViewInterface.class)) {
            if (((AncestrisTopComponent)gjvTc).isOpened() &&  gedcom.equals(gjvTc.getGedcom())){
                App.LOG.info(gjvTc.getClass().getName()+": "+gjvTc.getMode().getName());
                Mode mode = gjvTc.getMode();
                prefs.put(((AncestrisTopComponent)gjvTc).preferredID()+".dockMode", mode.getName());
                if (gjvTc.equals(mode.getSelectedTopComponent()) && (!gjvTc.equals(tcHasFocus)))
                    focusViews.add(gjvTc.getClass().getName());
                openedViews.add(gjvTc.getClass().getName());
            }
        }
        if (tcHasFocus instanceof AncestrisTopComponent)
            focusViews.add(tcHasFocus.getClass().getName());

        prefs.put("openViews", openedViews);
        prefs.put("focusViews", focusViews);
        prefs.put("openViews.date",System.currentTimeMillis()+"");
    }
}
