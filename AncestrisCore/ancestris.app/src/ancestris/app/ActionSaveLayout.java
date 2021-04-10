/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomDirectory;
import ancestris.gedcom.GedcomFileListener;
import ancestris.util.Lifecycle;
import ancestris.util.swing.DialogManager;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.EnvironmentChecker;
import genj.util.Origin;
import genj.util.Registry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;

// ActionSaveLayout is registered in App Module installer. I don't think it is a good idea.
// FIXME: may be we could register as a global service or create e constructor and register here
public final class ActionSaveLayout implements ActionListener, GedcomFileListener {

    @Override
    public void commitRequested(Context context) {
    }

    @Override
    public void gedcomClosed(Gedcom gedcom) {
        saveLayout(gedcom);
    }

    @Override
    public void gedcomOpened(Gedcom gedcom) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // Get current context of else select unique gedcom file if focus is somewhere else
        Context selected = Utilities.actionsGlobalContext().lookup(Context.class);
        if (selected == null) {
            List<Context> ctx = GedcomDirectory.getDefault().getContexts();
            if (!ctx.isEmpty()) {
                selected = ctx.get(0);
            }
        }
        Gedcom gedcom = selected != null ? selected.getGedcom() : null;

        // Collect settings information
        boolean existsGedcomSettings = false;   // true if .dockMode exist in Gedcom registry
        boolean existsDefaultSettings = false;  // true if .dockMode exist in modules properties
        if (gedcom != null) {
            Registry reg = gedcom.getRegistry();    // .ancestris/config/Preferences/gedcoms/settings/<gedcom>.ged
            existsGedcomSettings = existDock(reg);
        }
        String absolutePath = EnvironmentChecker.getProperty("user.home.ancestris", "", "");
        if (!absolutePath.isEmpty()) {
            absolutePath += "/../config/Preferences/ancestris/modules/";
            File dir = new File(absolutePath);
            for (File file : FileUtils.listFiles(dir, new String[]{"properties"}, true)) {
                if (file.getAbsolutePath().contains("ancestris-modules")) {
                    String debug = getNodeFromFile(file);
                    Registry reg = Registry.get(getNodeFromFile(file));
                    existsDefaultSettings |= existDock(reg);
                }
            }
        }
        Registry defaultSettings = Registry.get(AncestrisViewInterface.class);    // .ancestris/config/Preferences/ancestris/core/ancestris-view.properties
        DateFormat mediumDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        String date = "";
        if (defaultSettings.get("openViews.date", (String) null) != null) {
            date = mediumDateFormat.format(new Date(new Long(defaultSettings.get("openViews.date", "0"))));
        } else {
            date = NbBundle.getMessage(this.getClass(), "TXT_ASL_never");
        }

        // Alter user
        DisplaySettingsPanel layoutPanel = new DisplaySettingsPanel(gedcom, existsGedcomSettings, existsDefaultSettings, date);
        Object choice = DialogManager.create(NbBundle.getMessage(ActionSaveLayout.class, "TTL_LayoutSetting"), layoutPanel)
                .setMessageType(DialogManager.PLAIN_MESSAGE)
                .setOptionType(DialogManager.OK_CANCEL_OPTION)
                .setDialogId("windowSetting")
                .show();

        // Execute action
        if (choice == DialogManager.OK_OPTION) {
            switch (layoutPanel.getAction()) {
                case "currentSave":
                    saveLayout(gedcom, false);
                    break;
                case "currentRestore":
                    eraseLayout(gedcom, selected, existsDefaultSettings);
                    break;
                case "defaultSave":
                    saveDefaultLayout(gedcom);
                    break;
                case "defaultRestore":
                    eraseDefaultLayout(false);
                    break;
                case "allErase":
                    eraseAllLayout(gedcom != null);
                    break;
                default:
                    throw new AssertionError();
            }

        }
    }

    public static void saveLayout(Gedcom gedcom) {
        saveLayout(gedcom, true);
    }

    public static void saveLayout(Gedcom gedcom, boolean quiet) {

        List<String> openedViews = new ArrayList<>();
        List<String> focusViews = new ArrayList<>();
        Registry gedcomPrefs = gedcom.getRegistry();    // .ancestris/config/Preferences/gedcoms/settings/kennedy.ged

        TopComponent tcHasFocus = TopComponent.getRegistry().getActivated();

        //for (AncestrisViewInterface topComponent : Lookup.getDefault().lookupAll(AncestrisViewInterface.class))
        for (AncestrisViewInterface gjvTc : AncestrisPlugin.lookupAll(AncestrisViewInterface.class)) {
            if (((AncestrisTopComponent) gjvTc).isOpened() && gedcom.equals(gjvTc.getGedcom())) {
                Mode mode = gjvTc.getMode();
                App.LOG.info("Saving current mode for gedcom " + gedcom.getName() + " and for " + gjvTc.getClass().getName() + ": " + mode.getName());
                gedcomPrefs.put(((AncestrisTopComponent) gjvTc).getPreferencesKey("dockMode"), mode.getName());
                if (gjvTc.equals(mode.getSelectedTopComponent()) && (!gjvTc.equals(tcHasFocus))) {
                    focusViews.add(gjvTc.getClass().getName());
                }
                openedViews.add(gjvTc.getClass().getName());
            }
        }
        if (tcHasFocus instanceof AncestrisTopComponent) {
            focusViews.add(tcHasFocus.getClass().getName());
        }

        gedcomPrefs.put("openViews", openedViews);
        gedcomPrefs.put("focusViews", focusViews);
        gedcomPrefs.put("openViews.date", System.currentTimeMillis() + "");

        if (quiet) {
            return;
        }

        DialogManager.create(NbBundle.getMessage(ActionSaveLayout.class, "TTL_LayoutSetting"),
                NbBundle.getMessage(ActionSaveLayout.class, "DisplaySettingsPanel.saveCurrentRadioButton.text") + ": " + NbBundle.getMessage(ActionSaveLayout.class, "DLG_Done"))
                .setMessageType(DialogManager.INFORMATION_MESSAGE)
                .setOptionType(DialogManager.OK_ONLY_OPTION)
                .show();

    }

    private void eraseLayout(Gedcom gedcom, Context context, boolean existsDefaultSettings) {

        Object choice = DialogManager.create(NbBundle.getMessage(this.getClass(), "DisplaySettingsPanel.currentLabel.text", gedcom.getDisplayName() + " "),
                NbBundle.getMessage(this.getClass(), "DLG_ActionEraseLayout", gedcom.getDisplayName()))
                .setMessageType(DialogManager.QUESTION_MESSAGE)
                .setOptionType(DialogManager.YES_NO_OPTION)
                .setDialogId("windowSetting.eraseLayout")
                .show();
        if (choice != DialogManager.YES_OPTION) {
            return;
        }

        // Closes all views of current gedcom
        Origin origin = gedcom.getOrigin();
        GedcomDirectory.getDefault().closeGedcom(context);

        // Clean settings
        Registry gedcomPrefs = gedcom.getRegistry();    // .ancestris/config/Preferences/gedcoms/settings/kennedy.ged
        gedcomPrefs.remove("openViews");
        gedcomPrefs.remove("focusViews");
        removeModeFromRegistry(gedcomPrefs);

        // Reopen gedcom
        GedcomDirectory.getDefault().openAncestrisGedcom(FileUtil.toFileObject(origin.getFile()));

        String text = NbBundle.getMessage(ActionSaveLayout.class, "DisplaySettingsPanel.resetCurrentRadioButton.text", existsDefaultSettings
                ? NbBundle.getMessage(ActionSaveLayout.class, "DisplaySettingsPanel.resetCurrentRadioButton.userLayout") : NbBundle.getMessage(ActionSaveLayout.class, "DisplaySettingsPanel.resetCurrentRadioButton.AncestrisLayout"));
        DialogManager.create(NbBundle.getMessage(ActionSaveLayout.class, "TTL_LayoutSetting"),
                text + ": " + NbBundle.getMessage(ActionSaveLayout.class, "DLG_Done"))
                .setMessageType(DialogManager.INFORMATION_MESSAGE)
                .setOptionType(DialogManager.OK_ONLY_OPTION)
                .show();

    }

    public void saveDefaultLayout(Gedcom gedcom) {

        Registry defaultSettings = Registry.get(AncestrisViewInterface.class);    // .ancestris/config/Preferences/ancestris/core/ancestris-view.properties
        Object date = defaultSettings.get("openViews.date", (String) null) == null ? NbBundle.getMessage(this.getClass(), "TXT_ASL_never") : new Date(new Long(defaultSettings.get("openViews.date", "0")));

        Object choice = DialogManager.create(NbBundle.getMessage(this.getClass(), "DisplaySettingsPanel.userLabel.text"),
                NbBundle.getMessage(this.getClass(), "DLG_ActionSaveLayout", gedcom.getDisplayName(), date))
                .setMessageType(DialogManager.QUESTION_MESSAGE)
                .setOptionType(DialogManager.YES_NO_OPTION)
                .setDialogId("windowSetting.saveDefaultLayout")
                .show();
        if (choice != DialogManager.YES_OPTION) {
            return;
        }

        List<String> openedViews = new ArrayList<>();
        for (AncestrisViewInterface gjvTc : AncestrisPlugin.lookupAll(AncestrisViewInterface.class)) {
            if (((AncestrisTopComponent) gjvTc).isOpened() && gedcom.equals(gjvTc.getGedcom())) {
                Mode mode = gjvTc.getMode();
                App.LOG.info("Changing default mode for " + gjvTc.getClass().getName() + ": " + mode.getName());
                gjvTc.setDefaultMode(mode);                                                 // saves default dockmode in each respective module properties file
                openedViews.add(gjvTc.getClass().getName());
            }
        }

        defaultSettings.put("openViews", openedViews.toArray());
        defaultSettings.put("openViews.date", System.currentTimeMillis() + "");

        DialogManager.create(NbBundle.getMessage(ActionSaveLayout.class, "TTL_LayoutSetting"),
                NbBundle.getMessage(ActionSaveLayout.class, "DisplaySettingsPanel.saveUserRadioButton.text", gedcom.getDisplayName() + " ") + ": " + NbBundle.getMessage(ActionSaveLayout.class, "DLG_Done"))
                .setMessageType(DialogManager.INFORMATION_MESSAGE)
                .setOptionType(DialogManager.OK_ONLY_OPTION)
                .show();

    }

    private void eraseDefaultLayout(boolean quiet) {

        // remove openviews
        Registry defaultSettings = Registry.get(AncestrisViewInterface.class);    // .ancestris/config/Preferences/ancestris/core/ancestris-view.properties
        defaultSettings.remove("openViews");

        // remove dockmodes in modules
        // scan files under Preferences/ancestris/modules/*.properties, check for each file if a ".dockMode" line exists and remove it
        String absolutePath = EnvironmentChecker.getProperty("user.home.ancestris", "", "");
        if (!absolutePath.isEmpty()) {
            absolutePath += "/../config/Preferences/ancestris/";
            File dir = new File(absolutePath);
            for (File file : FileUtils.listFiles(dir, new String[]{"properties"}, true)) {
                if (file.getAbsolutePath().contains("ancestris-modules")) {
                    Registry reg = Registry.get(getNodeFromFile(file));
                    removeModeFromRegistry(reg);
                }
            }
        }

        if (quiet) {
            return;
        }

        DialogManager.create(NbBundle.getMessage(ActionSaveLayout.class, "TTL_LayoutSetting"),
                NbBundle.getMessage(ActionSaveLayout.class, "DisplaySettingsPanel.resetUserRadioButton.text") + ": " + NbBundle.getMessage(ActionSaveLayout.class, "DLG_Done"))
                .setMessageType(DialogManager.INFORMATION_MESSAGE)
                .setOptionType(DialogManager.OK_ONLY_OPTION)
                .show();

    }

    private void eraseAllLayout(boolean openGen) {

        String text1 = NbBundle.getMessage(this.getClass(), "DLG_ActionEraseAllLayout1");
        String text2 = NbBundle.getMessage(this.getClass(), "DLG_ActionEraseAllLayout2");
        String text3 = NbBundle.getMessage(this.getClass(), "DLG_ActionEraseAllLayout3");
        String text = openGen ? text1 + text2 + text3 : text1 + text3;
        Object choice = DialogManager.create(NbBundle.getMessage(this.getClass(), "DisplaySettingsPanel.allLayoutLabel.text"),
                text)
                .setMessageType(DialogManager.QUESTION_MESSAGE)
                .setOptionType(DialogManager.YES_NO_OPTION)
                .setDialogId("windowSetting.eraseAllLayout")
                .show();
        if (choice != DialogManager.YES_OPTION) {
            return;
        }

        // Close all gedcoms
        for (Context ctx : GedcomDirectory.getDefault().getContexts()) {
            GedcomDirectory.getDefault().closeGedcom(ctx);
        }

        // Erase all dockmodes in all gedcoms/settings
        // scan files under Preferences/ancestris/modules/*.properties, check for each file if a ".dockMode" line exists and remove it
        String absolutePath = EnvironmentChecker.getProperty("user.home.ancestris", "", "");
        if (!absolutePath.isEmpty()) {
            absolutePath += "/../config/Preferences/gedcoms/settings/";
            File dir = new File(absolutePath);
            if (dir.exists()) {
                for (File file : FileUtils.listFiles(dir, new String[]{"properties"}, true)) {
                    Registry reg = Registry.get(getNodeFromFile(file));
                    removeModeFromRegistry(reg);
                    reg.remove("openViews");
                    reg.remove("focusViews");
                }
            }
        }

        // Erase default layout
        eraseDefaultLayout(true);

        // Erase Windows2Local folder
        absolutePath = EnvironmentChecker.getProperty("user.home.ancestris", "", "");
        if (!absolutePath.isEmpty()) {
            absolutePath += "/../config/Windows2Local/";
            File dir = new File(absolutePath);
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        DialogManager.create(NbBundle.getMessage(ActionSaveLayout.class, "TTL_LayoutSetting"),
                NbBundle.getMessage(ActionSaveLayout.class, "DisplaySettingsPanel.resetAllLayoutRadioButton.text") + ": " + NbBundle.getMessage(ActionSaveLayout.class, "DLG_Done"))
                .setMessageType(DialogManager.INFORMATION_MESSAGE)
                .setOptionType(DialogManager.OK_ONLY_OPTION)
                .show();

        Lifecycle.askForStopAndStart(null, null);
    }

    private void removeModeFromRegistry(Registry reg) {
        Set<String> keys = reg.getProperties();
        for (String key : keys) {
            if (key.endsWith(".dockMode")) {
                reg.remove(key);
            }
        }
    }

    private boolean existDock(Registry reg) {
        boolean found = false;
        Set<String> keys = reg.getProperties();
        for (String key : keys) {
            if (key.endsWith(".dockMode")) {
                found = true;
                break;
            }
        }
        return found;
    }

    private String getNodeFromFile(File file) {
        String filename = file.getAbsolutePath();
        int i = filename.indexOf("Preferences");
        int j = filename.indexOf(".properties");
        String ret = filename.substring(i + 12, j);   // 12 = len of "Preferences/"
        ret = ret.replaceAll("\\\\", "/");          // on windows, replace backslashes otherwise reg.get(node) will fail with a too long string
        return ret;
    }
}
