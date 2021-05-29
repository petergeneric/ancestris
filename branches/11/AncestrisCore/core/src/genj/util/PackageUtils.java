/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genj.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 *
 * @author daniel
 */
        // XXX: PackageUtils class will be removed . It is used only for almanac
public class PackageUtils {

    public static List<Class> getClassesForPackage(String pckgname) throws ClassNotFoundException {
        return getClassesForPackage(pckgname, null);
    }

    public static List<Class> getClassesForPackage(String pckgname, String startWith)
            throws ClassNotFoundException {
        // This will hold a list of directories matching the pckgname.
        //There may be more than one if a package is split over multiple jars/paths
        List<Class> classes = new ArrayList<Class>();
        List<String> resPaths = findInPackage(pckgname, Pattern.compile((".*/" + (startWith == null ? "" : startWith) + "[^/]*").concat("\\.class")));
        for (String resPath : resPaths) {
            classes.add(Class.forName(resPath.substring(0, resPath.length() - 6)));
        }
        return classes;
    }

    /**
     * Find in package pckgname all resources whose name matches pattern
     * @param pckgname
     * @param pattern
     * @return
     * @throws ClassNotFoundException
     */
    public static List<String> findInPackage(String pckgname, Pattern pattern)
            throws ClassNotFoundException {
        // This will hold a list of directories matching the pckgname.
        //There may be more than one if a package is split over multiple jars/paths
        List<String> resPath = new ArrayList<String>();
        ArrayList<File> directories = new ArrayList<File>();
        String packagePath = pckgname.replace('.', '/');
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            // Ask for all resources for the path
            Enumeration<URL> resources = cld.getResources(packagePath + "/");
            while (resources.hasMoreElements()) {
                URL res = resources.nextElement();
                if (res.getProtocol().equalsIgnoreCase("jar")) {
                    JarURLConnection conn = (JarURLConnection) res.openConnection();
                    JarFile jar = conn.getJarFile();
                    for (JarEntry e : Collections.list(jar.entries())) {

                        if (e.getName().startsWith(packagePath)
                                && !e.getName().contains("$")
                                && pattern.matcher(e.getName().substring(packagePath.length())).matches()) {
                            String className = e.getName().replace("/", ".");
                            resPath.add(className);
                        }
                    }
                } else {
                    directories.add(new File(URLDecoder.decode(res.getPath(), "UTF-8")));
                }
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(pckgname + " does not appear to be "
                    + "a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(pckgname + " does not appear to be "
                    + "a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying "
                    + "to get all resources for " + pckgname);
        }

        // For every directory identified capture all the .class files
        for (File directory : directories) {
            if (directory.exists()) {
                // Get the list of the files contained in the package
                String[] files = directory.list();
                for (String file : files) {
                    // we are only interested in .class files
                    if (pattern.matcher(file).matches()) {
                        // removes the .class extension
                        resPath.add(pckgname + '.' + file);
                    }
                }
            } else {
                throw new ClassNotFoundException(pckgname + " (" + directory.getPath()
                        + ") does not appear to be a valid package");
            }
        }
        return resPath;
    }
}
