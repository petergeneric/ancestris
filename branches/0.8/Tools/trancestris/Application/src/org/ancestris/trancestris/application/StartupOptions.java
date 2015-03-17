/*
 * JVMOptionController.java
 *
 * Created on 17 de Dezembro de 2005, 01:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 *
 * @author claudio, daniel Some of this code is borrowed from
 * http://nbmodules.javaforge.com/
 */
public class StartupOptions {

    private String jdkHomePath = "";
    private String userDirPath = "";
    private String macuserDirPath = "";
    private Map<String, String> hParam = new HashMap<String, String>(10);
    private File ancestrisUserConfFile = new File(System.getProperty("netbeans.user") + "/etc/trancestris.conf");
    private Properties ancestrisConfProps;

    public StartupOptions() {
        String prefix = "";
        // Tries user conf file
        ancestrisConfProps = Util.loadProperties(ancestrisUserConfFile);
        // No user file? tries ancestris system
        File conf;
        if (ancestrisConfProps == null) {
            conf = InstalledFileLocator.getDefault().locate("../etc/trancestris.conf", "org.netbeans.core.startup", false);
            if (conf != null) {
                ancestrisConfProps = Util.loadProperties(conf);
            }
        }
        // No ancestris system? tries netbeans system (for run from netbeans ide)
        if (ancestrisConfProps == null) {
            conf = InstalledFileLocator.getDefault().locate("../etc/netbeans.conf", "org.netbeans.core.startup", false);
            if (conf != null) {
                ancestrisConfProps = Util.loadProperties(conf);
            }
            prefix = "netbeans_";
        }
        if (ancestrisConfProps != null) {
            loadConf(ancestrisConfProps, prefix);
        }
    }

    public void applyChanges() {
        ancestrisConfProps = getProperties();
        persistSettings();
    }

    private void persistSettings() {
        FileOutputStream fileOut = null;
        try {
            Util.createRecursively(ancestrisUserConfFile);
            fileOut = new FileOutputStream(ancestrisUserConfFile);
            ancestrisConfProps.store(fileOut, "properties written ancestris");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            Util.close(fileOut);
        }
    }

    /**
     * Gets startup settings as Property oblect. prefix is not used as user
     * setup file must not have this prefix set.
     *
     * @return
     */
    private Properties getProperties() {
        Properties props = new PropertiesLike();

        if (userDirPath.length() > 0) {
            props.setProperty("default_userdir", "\"" + userDirPath + "\"");
        }
        if (macuserDirPath.length() > 0) {
            props.setProperty("default_mac_userdir", "\"" + macuserDirPath + "\"");
        }
        if (jdkHomePath.length() > 0) {
            props.setProperty("jdkhome", "\"" + jdkHomePath + "\"");
        }
        props.setProperty("default_options", "\"" + getJvmParametersAsString() + "\"");
        return props;
    }

    private void loadConf(Properties confProperties, String prefix) {
        jdkHomePath = confProperties.getProperty(prefix + "jdkhome", "");
        if (Utilities.isWindows()) {
            jdkHomePath = jdkHomePath.replace('\\', '/');
        }
        if (jdkHomePath.indexOf('\"') > -1) {
            jdkHomePath = jdkHomePath.substring(1, jdkHomePath.length() - 1);
        }

        userDirPath = confProperties.getProperty(prefix + "default_userdir", "");
        if (userDirPath.indexOf('\"') > -1) {
            userDirPath = userDirPath.substring(1, userDirPath.length() - 1);
        }

        macuserDirPath = confProperties.getProperty(prefix + "default_mac_userdir", "");
        if (macuserDirPath.indexOf('\"') > -1) {
            macuserDirPath = macuserDirPath.substring(1, macuserDirPath.length() - 1);
        }
        loadJvmParameters(confProperties, prefix);
    }

    private void loadJvmParameters(final Properties confProperties, String prefix) {
        String val = confProperties.getProperty(prefix + "default_options", "");
        if (val.indexOf('\"') > -1) {
            val = val.substring(1, val.length() - 1).trim();
        }
        if (val.length() == 0) {
            return;
        }
        String[] _v = val.split("\\s+");
        for (int j = 0; j < _v.length; j++) {
            String parameter = _v[j];
            if (parameter.substring(0, 2).equals("--")) {
                // if parameter starts with --
                // as --fontsize 10
                // it is a parameter and its value. They need to be displayed on the same table line
                j++;
                hParam.put(parameter, _v[j]);
            } else if (parameter.startsWith("-J-Xm")) {
                // case -J-Xms and -J-Xmx
                hParam.put(parameter.substring(0, 6), parameter.substring(6));
            } else if (parameter.startsWith("-J-D")) {
                String kv[] = parameter.split("=");
                if (kv.length > 1) {
                    hParam.put(kv[0], kv[1]);
                }
            } else {
                hParam.put(parameter, "");
            }
        }
    }

    public String getUserDir() {
        return userDirPath;
    }

    public String getJdkHomeDir() {
        return jdkHomePath;
    }

    /**
     * Set JVM Parameter value. If value == null, remove parameter
     *
     * @param parameter
     * @param value
     * @return true if parameter has been changed
     */
    public boolean setJvmParameter(String parameter, String value) {
        String oldValue = getJvmParameter(parameter);
        if (value == null) {
            hParam.remove(parameter);
            return oldValue != null;
        } else {
            hParam.put(parameter, value);
            return !value.equals(oldValue);
        }
    }

    /**
     * Get JVM Parameter value
     *
     * @param parameter
     * @return
     */
    public String getJvmParameter(String parameter) {
        return hParam.get(parameter);
    }

    public boolean setJvmLocale(Locale locale) {
        return setJvmParameter("--locale", locale == null ? null : locale.toString().replace('_', ':'));
    }

    /**
     * return Locale from jvm settings, null if no --locale parameter which
     * means defult from system
     *
     * @return
     */
    public Locale getJvmLocale() {
        return getLocaleFromString(getJvmParameter("--locale"));
    }

    private Locale getLocaleFromString(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        String locale[] = (str + "::").split(":", 3);

        return new Locale(locale[0], locale[1], locale[2]);
    }

    public String getJvmParametersAsString() {
        StringBuilder sb = new StringBuilder(120);
        for (String key : hParam.keySet()) {
            // remove old language setting, replaced by --locale
            if (key.contentEquals("-J-Duser.language")) {
                continue;
            }
            sb.append(" ").append(key);
            if (key.startsWith("--")) {
                sb.append(" ").append(hParam.get(key));
            } else if (key.startsWith("-J-D")) {
                sb.append("=").append(hParam.get(key));
            } else {
                sb.append(hParam.get(key));
            }
        }
        // strip leading space character
        return sb.length() > 0 ? sb.substring(1) : "";
    }
}
