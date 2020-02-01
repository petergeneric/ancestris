/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.treesharing;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomFileListener;
import ancestris.gedcom.privacy.standard.PrivacyOptionsPanelController;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author frederic
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class TreeSharingPlugin extends AncestrisPlugin implements GedcomFileListener, PropertyChangeListener {

    /**
     * 
     * GedcomFileListeners
     * 
     */
    @Override
    public void commitRequested(Context context) {
        // do nothing
    }
    
    @Override
    public void gedcomOpened(Gedcom gedcom) {
        TreeSharingTopComponent.getDefault().gedcomOpened(gedcom);
    }

    @Override
    public void gedcomClosed(Gedcom gedcom) {
        TreeSharingTopComponent.getDefault().gedcomClosed(gedcom);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PrivacyOptionsPanelController.PRIVACY_OPTIONS_CHANGED)) {
            TreeSharingTopComponent.getDefault().dispatchRecalc();
        }
    }

}
