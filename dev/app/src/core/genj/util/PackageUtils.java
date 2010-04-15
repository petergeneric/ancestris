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

/**
 *
 * @author daniel
 */
public class PackageUtils {
public static List<Class> getClassesForPackage(String pckgname) throws ClassNotFoundException {
    return getClassesForPackage(pckgname, null);
}
public static List<Class> getClassesForPackage(String pckgname,String startWith)
                                                throws ClassNotFoundException {
        // This will hold a list of directories matching the pckgname.
        //There may be more than one if a package is split over multiple jars/paths
        List<Class> classes = new ArrayList<Class>();
        ArrayList<File> directories = new ArrayList<File>();
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            // Ask for all resources for the path
            Enumeration<URL> resources = cld.getResources(pckgname.replace('.', '/'));
            while (resources.hasMoreElements()) {
                URL res = resources.nextElement();
                if (res.getProtocol().equalsIgnoreCase("jar")){
                    JarURLConnection conn = (JarURLConnection) res.openConnection();
                    JarFile jar = conn.getJarFile();
                    for (JarEntry e:Collections.list(jar.entries())){

                        if (e.getName().startsWith(pckgname.replace('.', '/'))
                            && e.getName().endsWith(".class") && !e.getName().contains("$")
                            && e.getName().matches(".*/"+(startWith==null?"":startWith)+"[^/]*\\.class")){
                            String className =
                                    e.getName().replace("/",".").substring(0,e.getName().length() - 6);
                            System.out.println(className);
                            classes.add(Class.forName(className));
                        }
                    }
                }else
                    directories.add(new File(URLDecoder.decode(res.getPath(), "UTF-8")));
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(pckgname + " does not appear to be " +
                    "a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(pckgname + " does not appear to be " +
                    "a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying " +
                    "to get all resources for " + pckgname);
        }

        // For every directory identified capture all the .class files
        for (File directory : directories) {
            if (directory.exists()) {
                // Get the list of the files contained in the package
                String[] files = directory.list();
                for (String file : files) {
                    // we are only interested in .class files
                    if (file.endsWith(".class")) {
                        // removes the .class extension
                        classes.add(Class.forName(pckgname + '.'
                                + file.substring(0, file.length() - 6)));
                    }
                }
            } else {
                throw new ClassNotFoundException(pckgname + " (" + directory.getPath() +
                                    ") does not appear to be a valid package");
            }
        }
        return classes;
    }


//    public static List<Class> getClassessOfInterface(String thePackage, Class theInterface) {
//        List<Class> classList = new ArrayList<Class>();
//        try {
//            for (Class discovered : getClassesForPackage(thePackage)) {
//                if (Arrays.asList(discovered.getInterfaces()).contains(theInterface)) {
//                    classList.add(discovered);
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(ClassDiscover.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return classList;
//    }
//
    public static void main(String[] args) {
		try {
			// test getting classes
			List<Class> ddd = getClassesForPackage("genjreports");
			for (Class c:ddd) {
				System.out.println(c.getCanonicalName());
			}

//			// test getting packages
//			ArrayList<String> packs = getPackageNames("genj");
//			for (String s:packs) {
//				System.out.println(s);
//			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
