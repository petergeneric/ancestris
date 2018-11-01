package ancestris.modules.exports.cousinsgenweb;

import ancestris.core.actions.AbstractAncestrisContextAction;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyPlace;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JFrame;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.*;

@ActionID(id = "ancestris.modules.exports.cousinsgenweb.CousinsGenWebAction", category = "File")
@ActionRegistration(
        displayName = "#CTL_CousinsGenWebAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/File/Export", name = "CousinsGenWebAction", position = 300)
public final class CousinsGenWebAction extends AbstractAncestrisContextAction {

    CousinsGenWebPanel cousinGenWebPanel;
    DialogDescriptor cousinGenWebPanelDescriptor;

    public CousinsGenWebAction() {
        super();
        setImage("ancestris/modules/exports/cousinsgenweb/cousinsgenweb.png");
        setText(NbBundle.getMessage(CousinsGenWebAction.class, "CTL_CousinsGenWebAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
    }

    private class CousinGenWebPanelDescriptorActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            depPos = cousinGenWebPanel.getDepPos();
            cityPos = cousinGenWebPanel.getCityPos();
            depLen = cousinGenWebPanel.getDepLength();
            dir = cousinGenWebPanel.getFile();
        }
    };

    /**
     * option - Index jurisdiction for analysis in PLAC tags
     */
    public int depPos = 0;
    /**
     * option - Index jurisdiction for analysis in PLAC tags
     */
    public int cityPos = 0;
    /**
     * option - Meaningfull length for the Department Juridiction field to keep
     */
    public int depLen = 0;

    // Export directory
    File dir = null;

    InputOutput io = null;
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Create the file chooser
        Context contextToOpen = getContext();

        if (contextToOpen != null) {

            cousinGenWebPanel = new CousinsGenWebPanel(contextToOpen);
            cousinGenWebPanelDescriptor = new DialogDescriptor(
                    cousinGenWebPanel,
                    NbBundle.getMessage(CousinsGenWebPanel.class, "CTL_CousinsGenWebAction"),
                    true,
                    new CousinGenWebPanelDescriptorActionListener());

            Dialog dialog = DialogDisplayer.getDefault().createDialog(cousinGenWebPanelDescriptor);
            dialog.setVisible(true);
            dialog.toFront();

            if (cousinGenWebPanelDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
                if (dir == null) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(CousinsGenWebAction.class, "ERR_EmptyDirectory"), NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                    return;
                }
                showWaitCursor();
                Gedcom myGedcom = contextToOpen.getGedcom();
                Collection<Indi> indis = (Collection<Indi>) (myGedcom.getEntities(Gedcom.INDI));
                io = IOProvider.getDefault().getIO(NbBundle.getMessage(CousinsGenWebAction.class, "CousinsGenWebAction.TabTitle") + " " + myGedcom.getName(), true);
                io.getOut().println(String.format(NbBundle.getMessage(CousinsGenWebAction.class, "CousinsGenWebAction.Start"), myGedcom.getName()));

                // prepare our index
                Map<String, Object> primary = new TreeMap<String, Object>();
                for (Iterator<Indi> it = indis.iterator(); it.hasNext();) {
                    analyze(it.next(), primary);
                }

                // Create all the files
                for (Iterator<String> ps = primary.keySet().iterator(); ps.hasNext();) {
                    String p = ps.next();

                    try {
                        export(p, primary, dir);
                    } catch (IOException ioe) {
                        System.err.println("IO Exception!");
                        ioe.printStackTrace();
                    }
                }
                io.getOut().println(NbBundle.getMessage(CousinsGenWebAction.class, "CousinsGenWebAction.End"));
                io.getOut().close();
                io.getErr().close();
                hideWaitCursor();
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(CousinsGenWebAction.class, "CousinsGenWebAction.End"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);

            }
        }
    }

    private static void showWaitCursor() {
        Mutex.EVENT.readAccess(new Runnable() {

            @Override
            public void run() {
                JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
                mainWindow.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                mainWindow.getGlassPane().setVisible(true);
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(CousinsGenWebAction.class, "CousinsGenWebAction.Start"));
            }
        });
    }

    private static void hideWaitCursor() {
        Mutex.EVENT.readAccess(new Runnable() {

            @Override
            public void run() {
                StatusDisplayer.getDefault().setStatusText("");  //NOI18N
                JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
                mainWindow.getGlassPane().setVisible(false);
                mainWindow.getGlassPane().setCursor(null);
            }
        });
    }
    
    /**
     * Analyze an individual
     */
    private void analyze(Indi indi, Map<String, Object> primary) {

        // consider non-empty last names only
        String name = indi.getLastName();
        if (name.length() == 0) {
            return;
        }

        // loop over all places in indi
        for (PropertyPlace place : indi.getProperties(PropertyPlace.class)) {
            String dept = place.getJurisdiction(depPos);
            if (dept == null) {
                continue;
            }
            if (dept.length() == 0) {
                continue;
            }
            int l = Math.min(dept.length(), depLen);
            if (l > 0) {
                dept = dept.substring(0, l);
            }
            String jurisdiction = place.getJurisdiction(cityPos);
            if (jurisdiction.length() == 0) {
                jurisdiction = "???";
            }
            // keep it
            keep(name, jurisdiction, dept, primary);
        }
    }

    private void keep(String name, String place, String dept, Map<String, Object> primary) {

        // calculate primary and secondary key
        // remember
        Map<String, Object> secondary = (Map<String, Object>) lookup(primary, dept, TreeMap.class);
        Map<String, Object> namelist = (Map<String, Object>) lookup(secondary, place, TreeMap.class);
        lookup(namelist, name, TreeMap.class);
        // done
    }

    /**
     * Lookup an object in a map with a default class
     */
    private Object lookup(Map<String, Object> index, String key, Class<? extends Object> fallback) {
        // look up and create lazily if necessary
        Object result = index.get(key);
        if (result == null) {
            try {
                result = fallback.newInstance();
            } catch (Throwable t) {
                t.printStackTrace();
                throw new IllegalArgumentException("can't instantiate fallback " + fallback);
            }
            index.put(key, result);
        }
        // done
        return result;
    }

    private void export(String dept, Map<String, Object> primary, File directory) throws IOException {
        File file = new File(directory, dept + ".csv");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));

        io.getOut().println(("DepartmentJur") + " : " + dept);
        Map<String, Object> secondary = (Map) lookup(primary, dept, null);
        for (Iterator<String> ss = secondary.keySet().iterator(); ss.hasNext();) {
            String s = ss.next();

            Map<String, Object> namelist = (Map) lookup(secondary, s, null);
            for (Iterator<String> ns = namelist.keySet().iterator(); ns.hasNext();) {
                String t = ns.next();
                io.getOut().println("  " + t + " ; " + s);
                out.write(t + " ; " + s);
                out.newLine();
            }
        }
        out.close();
    }
}
