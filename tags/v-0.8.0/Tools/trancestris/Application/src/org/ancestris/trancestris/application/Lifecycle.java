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
package org.ancestris.trancestris.application;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author daniel
 */
public class Lifecycle {

    public static void askForRestart(String message) {
        ResourceBundle bundle = NbBundle.getBundle(Lifecycle.class);

        String msg = message == null ? bundle.getString("NeedRestart.text") : message;

        DialogDescriptor dd = new DialogDescriptor(msg, bundle.getString("NeedRestart.title"), false, NotifyDescriptor.OK_CANCEL_OPTION, null,
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        if (arg0.getSource() == NotifyDescriptor.OK_OPTION) {
                            LifecycleManager.getDefault().markForRestart();
                            LifecycleManager.getDefault().exit();
                        }
                    }
                });
        DialogDisplayer.getDefault().notifyLater(dd);
    }

    public static void askForRestart() {
        askForRestart(null);
    }

    public static void askForStopAndStart(String message, Locale locale) {
        ResourceBundle bundle = locale == null
                ? NbBundle.getBundle(Lifecycle.class)
                : NbBundle.getBundle(findName(Lifecycle.class), locale, Lifecycle.class.getClassLoader());

        String msg = message == null ? bundle.getString("NeedStopStart.text") : message;

        DialogDescriptor dd = new DialogDescriptor(msg, bundle.getString("NeedStopStart.title"), false, NotifyDescriptor.OK_CANCEL_OPTION, null,
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        if (arg0.getSource() == NotifyDescriptor.OK_OPTION) {
                            LifecycleManager.getDefault().exit();
                        }
                    }
                });
        DialogDisplayer.getDefault().notifyLater(dd);
    }

    public static void askForStopAndStart() {
        askForStopAndStart(null, null);
    }

    // Code from NbBundel source
    /**
     * Finds package name for given class
     */
    private static String findName(Class<?> clazz) {
        String pref = clazz.getName();
        int last = pref.lastIndexOf('.');

        if (last >= 0) {
            pref = pref.substring(0, last + 1);

            return pref + "Bundle"; // NOI18N
        } else {
            // base package, search for bundle
            return "Bundle"; // NOI18N
        }
    }

}
