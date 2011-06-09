/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.startup.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.openide.util.Utilities;

/**
 *
 * @author daniel
 */
public class JVMSettings {

    private String jdkHomePath = "";
    private String userDirPath = "";
    private String macuserDirPath = "";
    private Map<String, String> hParam = new HashMap<String, String>(10);

    public JVMSettings(Properties confProperties) {
        loadConf(confProperties);
    }

    public Properties getProperties() {
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
        props.setProperty("default_options", "\"" + getJvmParameters() + "\"");
        return props;
    }

    public final void loadConf(Properties confProperties) {
        jdkHomePath = confProperties.getProperty("jdkhome", "");
        if (Utilities.isWindows()) {
            jdkHomePath = jdkHomePath.replace('\\', '/');
        }
        if (jdkHomePath.indexOf('\"') > -1) {
            jdkHomePath = jdkHomePath.substring(1, jdkHomePath.length() - 1);
        }

        userDirPath = confProperties.getProperty("default_userdir", "");
        if (userDirPath.indexOf('\"') > -1) {
            userDirPath = userDirPath.substring(1, userDirPath.length() - 1);
        }

        macuserDirPath = confProperties.getProperty("default_mac_userdir", "");
        if (macuserDirPath.indexOf('\"') > -1) {
            macuserDirPath = macuserDirPath.substring(1, macuserDirPath.length() - 1);
        }
        loadJvmParameters(confProperties);
    }

    private void loadJvmParameters(final Properties confProperties) {
        String val = confProperties.getProperty("default_options", "");
        if (val.indexOf('\"') > -1) {
            val = val.substring(1, val.length() - 1).trim();
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
                hParam.put(kv[0], kv[1]);
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
     * Set JVM Parameter value
     * @param parameter
     * @param value
     */
    public void setJvmParameter(String parameter, String value) {
        hParam.put(parameter, value);
    }

    /**
     * Get JVM Parameter value
     * @param parameter
     * @return
     */
    public String getJvmParameter(String parameter) {
        return hParam.get(parameter);
    }

    public String getJvmParameters() {
        StringBuilder sb = new StringBuilder(120);
        for (String key : hParam.keySet()) {
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
        return sb.substring(1);
    }
}
