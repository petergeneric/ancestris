/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app.pluginservice;

import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 *
 * @author daniel
 */
public class PluginHelper {
    static public Attributes getManifestMainAttributes(Class clazz) {
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
          // Class not from JAR
          return null;
        }
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
            "/META-INF/MANIFEST.MF";
        Manifest manifest;
        try {
            manifest = new Manifest(new URL(manifestPath).openStream());
        } catch (Exception e) {
            return new Attributes();
        }
        return manifest.getMainAttributes();
    }

}
