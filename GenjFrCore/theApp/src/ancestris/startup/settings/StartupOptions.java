/*
 * JVMOptionController.java
 *
 * Created on 17 de Dezembro de 2005, 01:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ancestris.startup.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author claudio, daniel
 * This code is borrowed from http://nbmodules.javaforge.com/
 */
public class StartupOptions {

    private JVMSettings jvmSetting = null;
    private File ancestrisUserConfFile = new File(System.getProperty("netbeans.user") + "/etc/ancestris.conf");
    private Properties ancestrisConfProps;

    public StartupOptions() {
        copyToUserDir();
        ancestrisConfProps = Util.loadProperties(ancestrisUserConfFile);
        if (ancestrisConfProps != null) {
            jvmSetting = new JVMSettings(ancestrisConfProps);
        }
    }

    public void applyChanges() {
        if (jvmSetting == null) {
            return;
        }
        ancestrisConfProps = jvmSetting.getProperties();
        persistSettings();
    }

    private void persistSettings() {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(ancestrisUserConfFile);
            ancestrisConfProps.store(fileOut, "properties written ancestris");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            Util.close(fileOut);
        }
    }

    private void copyToUserDir() {
        if (!ancestrisUserConfFile.exists()) {
            // use InstalledFileLocator ?
            File netbeansConfFile = new File(System.getProperty("netbeans.home") + "/../etc/ancestris.conf");
            System.out.println("system" + System.getProperties());
            System.out.println("file:" + netbeansConfFile);
            //InstalledFileLocator.getDefault().locate("../etc/ancestris.conf",null, false);
            Util.copy(netbeansConfFile, ancestrisUserConfFile);
        } else {
        }
    }

    /**
     * Set JVM Parameter value
     * @param parameter
     * @param value
     */
    public void setJvmParameter(String parameter, String value) {
        if (jvmSetting == null) {
            return;
        }
        jvmSetting.setJvmParameter(parameter, value);
    }

    /**
     * Get JVM Parameter value
     * @param parameter
     * @return
     */
    public String getJvmParameter(String parameter) {
        return (jvmSetting == null) ? null : jvmSetting.getJvmParameter(parameter);
    }
}
