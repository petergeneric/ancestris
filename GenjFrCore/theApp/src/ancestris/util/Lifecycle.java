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
package ancestris.util;

import genjfr.app.App;
import java.util.ResourceBundle;
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
        ResourceBundle bundle = NbBundle.getBundle(App.class);

        String msg = message==null?bundle.getString("NeedRestart.text"):message;

        NotifyDescriptor nd = new NotifyDescriptor(msg,bundle.getString("NeedRestart.title"), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        DialogDisplayer.getDefault().notify(nd);
        if (nd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
            LifecycleManager.getDefault().markForRestart();
            LifecycleManager.getDefault().exit();
        }
    }

    public static void askForRestart() {
        askForRestart(null);
    }

    public static void askForStopAndStart(String message) {
        ResourceBundle bundle = NbBundle.getBundle(App.class);

        String msg = message==null?bundle.getString("NeedStopStart.text"):message;

        NotifyDescriptor nd = new NotifyDescriptor(msg,bundle.getString("NeedStopStart.title"), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        DialogDisplayer.getDefault().notify(nd);
        if (nd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
            LifecycleManager.getDefault().exit();
        }
    }

    public static void askForStopAndStart() {
        askForStopAndStart(null);
    }
}
