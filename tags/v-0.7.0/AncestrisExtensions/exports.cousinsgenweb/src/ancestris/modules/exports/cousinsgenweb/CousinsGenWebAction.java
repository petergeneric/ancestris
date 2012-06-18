package ancestris.modules.exports.cousinsgenweb;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyPlace;
import ancestris.app.App;
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
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

public final class CousinsGenWebAction implements ActionListener {

    private class CousinGenWebPanelDescriptorActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            depPos = cousinGenWebPanel.getDepPos();
            cityPos = cousinGenWebPanel.getCityPos();
            depLen = cousinGenWebPanel.getDepLength();
            file = cousinGenWebPanel.getFile();
        }
    };
    CousinsGenWebPanel cousinGenWebPanel = new CousinsGenWebPanel();
    DialogDescriptor cousinGenWebPanelDescriptor = new DialogDescriptor(
            cousinGenWebPanel,
            NbBundle.getMessage(CousinsGenWebPanel.class, "CTL_CousinsGenWebAction"),
            true,
            new CousinGenWebPanelDescriptorActionListener());
    /** option - Index jurisdiction for analysis in PLAC tags */
    public int depPos = 0;
    /** option - Index jurisdiction for analysis in PLAC tags */
    public int cityPos = 0;
    /** option - Meaningfull length for the Department Juridiction field to keep */
    public int depLen = 0;
    File file = null;
    InputOutput io = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // Create the file chooser
        Context context;

        if ((context = App.center.getSelectedContext(true)) != null) {
            Dialog dialog = DialogDisplayer.getDefault().createDialog(cousinGenWebPanelDescriptor);
            dialog.setVisible(true);
            dialog.toFront();

            if (cousinGenWebPanelDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
                Gedcom myGedcom = context.getGedcom();
                Collection<? extends Entity> indis = myGedcom.getEntities(Gedcom.INDI);
                io = IOProvider.getDefault().getIO(NbBundle.getMessage(CousinsGenWebAction.class, "CousinsGenWebAction.TabTitle") + " " + myGedcom.getName(), true);
                io.getOut().println(String.format(NbBundle.getMessage(CousinsGenWebAction.class, "CousinsGenWebAction.Start"), myGedcom.getName()));

                // prepare our index
                Map primary = new TreeMap();
                for (Iterator it = indis.iterator(); it.hasNext();) {
                    analyze((Indi) it.next(), primary);
                }

                // Create all the files
                for (Iterator ps = primary.keySet().iterator(); ps.hasNext();) {
                    String p = (String) ps.next();

                    try {
                        export(p, primary, file);
                    } catch (IOException ioe) {
                        System.err.println("IO Exception!");
                        ioe.printStackTrace();
                    }
                }
                io.getOut().println(NbBundle.getMessage(CousinsGenWebAction.class, "CousinsGenWebAction.End"));
                io.getOut().close();
                io.getErr().close();

            }
        }
    }

    /**
     * Analyze an individual
     */
    private void analyze(Indi indi, Map primary) {

        // consider non-empty last names only
        String name = indi.getLastName();
        if (name.length() == 0) {
            return;
        }

        // loop over all dates in indi
        for (Iterator places = indi.getProperties(PropertyPlace.class).iterator(); places.hasNext();) {

            PropertyPlace place = (PropertyPlace) places.next();

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

    private void keep(String name, String place, String dept, Map primary) {

        // calculate primary and secondary key
        // remember
        Map secondary = (Map) lookup(primary, dept, TreeMap.class);
        Map namelist = (Map) lookup(secondary, place, TreeMap.class);
        lookup(namelist, name, TreeMap.class);
        // done
    }

    /**
     * Lookup an object in a map with a default class
     */
    private Object lookup(Map index, String key, Class fallback) {
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

    private void export(String dept, Map primary, File dir) throws IOException {
        File file = new File(dir, dept + ".csv");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));

        io.getOut().println(("DepartmentJur") + " : " + dept);
        Map secondary = (Map) lookup(primary, dept, null);
        for (Iterator ss = secondary.keySet().iterator(); ss.hasNext();) {
            String s = (String) ss.next();

            Map namelist = (Map) lookup(secondary, s, null);
            for (Iterator ns = namelist.keySet().iterator(); ns.hasNext();) {
                String t = (String) ns.next();
                io.getOut().println("  " + t + " ; " + s);
                out.write(t + " ; " + s);
                out.newLine();
            }
        }
        out.close();
    }
}
