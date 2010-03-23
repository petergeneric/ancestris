/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.app.Images;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomDirectory;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import genj.gedcom.Submitter;
import genj.gedcom.UnitOfWork;
import genj.util.Origin;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.window.WindowManager;
import genjfr.app.pluginservice.PluginInterface;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JOptionPane;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

public final class ActionNew extends Action2 {

    private Resources resources = Resources.get(genj.app.ControlCenter.class);
    private WindowManager windowManager = App.center.getWindowManager();

    /** constructor */
    public ActionNew() {
//      setAccelerator(ACC_NEW);
        setText(resources, "cc.menu.new");
        setTip(resources, "cc.tip.create_file");
        setImage(Images.imgNew);
    }

    /** execute callback */
    protected void execute() {

        // let user choose a file
        File file = App.center.chooseFile(resources.getString("cc.create.title"), resources.getString("cc.create.action"), null);
        if (file == null) {
            return;
        }
        if (!file.getName().endsWith(".ged")) {
            file = new File(file.getAbsolutePath() + ".ged");
        }
        if (file.exists()) {
            int rc = windowManager.openDialog(
                    null,
                    resources.getString("cc.create.title"),
                    WindowManager.WARNING_MESSAGE,
                    resources.getString("cc.open.file_exists", file.getName()),
                    Action2.yesNo(),
                    App.center);
            if (rc != 0) {
                return;
            }
        }
        // form the origin
        try {
            Gedcom gedcom = new Gedcom(Origin.create(new URL("file", "", file.getAbsolutePath())));
            gedcom.doUnitOfWork(new UnitOfWork() {
                public void perform(Gedcom gedcom) throws GedcomException {
                    
                    // Create submitter
                    Submitter submitter = (Submitter) gedcom.createEntity(Gedcom.SUBM);
                    submitter.setName(NbPreferences.forModule(App.class).get("submName", ""));
                    submitter.setCity(NbPreferences.forModule(App.class).get("submCity", ""));
                    submitter.setPhone(NbPreferences.forModule(App.class).get("submPhone", ""));
                    submitter.setEmail(NbPreferences.forModule(App.class).get("submEmail", ""));
                    submitter.setCountry(NbPreferences.forModule(App.class).get("submCountry", ""));
                    submitter.setWeb(NbPreferences.forModule(App.class).get("submWeb", ""));

                    // Create place format
                    gedcom.setPlaceFormat(getPlaceFormatFromOptions());

                    //Create first INDI entity
                    Indi adam = (Indi) gedcom.createEntity(Gedcom.INDI);
                    adam.addDefaultProperties();
                    adam.setName("Adam", "");
                    adam.setSex(PropertySex.MALE);
                }
            });
            // remember
            GedcomDirectory.getInstance().registerGedcom(gedcom);
            ActionOpen.openDefaultViews(gedcom);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error " + e);
        }

    }

    String getPlaceFormatFromOptions() {
        String format = "";
        String jur = "";
        String space = getSpaceFromOptions("address_splitspaces");
        // go through all jursidictions possible
        jur = getJurisdictionFromOptions("fmt_address1");
        if (!jur.isEmpty()) {
            format += jur;
        }
        jur = getJurisdictionFromOptions("fmt_address2");
        if (!jur.isEmpty()) {
            format += "," + space + jur;
        }
        jur = getJurisdictionFromOptions("fmt_address3");
        if (!jur.isEmpty()) {
            format += "," + space + jur;
        }
        jur = getJurisdictionFromOptions("fmt_address4");
        if (!jur.isEmpty()) {
            format += "," + space + jur;
        }
        jur = getJurisdictionFromOptions("fmt_address5");
        if (!jur.isEmpty()) {
            format += "," + space + jur;
        }
        jur = getJurisdictionFromOptions("fmt_address6");
        if (!jur.isEmpty()) {
            format += "," + space + jur;
        }
        jur = getJurisdictionFromOptions("fmt_address7");
        if (!jur.isEmpty()) {
            format += "," + space + jur;
        }
        return format;
    }

    private String getJurisdictionFromOptions(String string) {
        return NbPreferences.forModule(genj.app.App.class).get(string, "");
    }

    private String getSpaceFromOptions(String string) {
        String option = NbPreferences.forModule(genj.app.App.class).get(string, "");
        return (option.equals("true")) ? " " : "";
    }
} //ActionNew

