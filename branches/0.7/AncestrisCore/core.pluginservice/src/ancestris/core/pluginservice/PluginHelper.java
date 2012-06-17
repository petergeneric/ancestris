/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.core.pluginservice;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 *
 * @author daniel
 */
public class PluginHelper {
    static public Attributes getManifestMainAttributes(Class clazz) {
        Manifest manifest;
        try {
            manifest = new Manifest(clazz.getResource("/META-INF/MANIFEST.MF").openStream());
        } catch (Exception e) {
            return new Attributes();
        }
        return manifest.getMainAttributes();
    }

}
