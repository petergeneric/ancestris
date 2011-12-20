/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import genj.util.AncestrisPreferences;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Registry;
import ancestris.core.pluginservice.AncestrisPlugin;
import genj.app.GedcomFileListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;

public final class ActionSaveLayout implements ActionListener,GedcomFileListener {

    public void actionPerformed(ActionEvent e) {
        Context selected = App.center.getSelectedContext(true);
        if (selected == null){
            return;
        }
        AncestrisPreferences prefs = Registry.get(AncestrisTopComponent.class);
        Object date = prefs.get("openViews.date", (String)null) == null?
            NbBundle.getMessage(this.getClass(), "TXT_ASL_never"):
            new Date(new Long(prefs.get("openViews.date", "0")));

        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(this.getClass(),"DLG_ActionSaveLayout",date),
                NbBundle.getMessage(this.getClass(),"TTL_ActionSaveLayout",selected.getGedcom().getName()),
                NotifyDescriptor.OK_CANCEL_OPTION);
        DialogDisplayer.getDefault().notify(nd);
        if (!nd.getValue().equals(NotifyDescriptor.OK_OPTION))
            return;

        saveDefaultLayout(selected.getGedcom(), prefs);
    }

    public void saveDefaultLayout(Gedcom gedcom, AncestrisPreferences prefs){
        List<String> openedViews = new ArrayList<String>();

        for (AncestrisViewInterface gjvTc : AncestrisPlugin.lookupAll(AncestrisViewInterface.class)) {
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
        Registry prefs = gedcom.getRegistry();

        TopComponent tcHasFocus = TopComponent.getRegistry().getActivated();
        
        for (AncestrisViewInterface gjvTc : AncestrisPlugin.lookupAll(AncestrisViewInterface.class)) {
            if (((AncestrisTopComponent)gjvTc).isOpened() &&  gedcom.equals(gjvTc.getGedcom())){
                App.LOG.info(gjvTc.getClass().getName()+": "+gjvTc.getMode().getName());
                Mode mode = gjvTc.getMode();
                prefs.put(((AncestrisTopComponent)gjvTc).getPreferencesKey("dockMode"), mode.getName());
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

    public void commitRequested(Context context) {
    }

    public void gedcomClosed(Gedcom gedcom) {
        saveLayout(gedcom);
    }

    public void gedcomOpened(Gedcom gedcom) {
    }
}
