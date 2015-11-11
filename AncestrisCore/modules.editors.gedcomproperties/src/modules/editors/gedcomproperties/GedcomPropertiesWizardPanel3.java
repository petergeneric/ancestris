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
package modules.editors.gedcomproperties;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class GedcomPropertiesWizardPanel3 implements WizardDescriptor.ValidatingPanel, Constants {

    private WizardDescriptor wiz = null;
    private final int mode = GedcomPropertiesWizardIterator.getMode();
    private String originalVersion = "";
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GedcomPropertiesVisualPanel3 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public GedcomPropertiesVisualPanel3 getComponent() {
        if (component == null) {
            component = new GedcomPropertiesVisualPanel3(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(Object data) {
        wiz = (WizardDescriptor) data;
        
        if (originalVersion.isEmpty()) {
            originalVersion = (String) wiz.getProperty(HEADER + ":" + GEDC + ":" + VERS);
        }
        getComponent().setLANG((String) wiz.getProperty(HEADER + ":" + LANG));
        getComponent().setCHAR((String) wiz.getProperty(HEADER + ":" + CHAR));
        getComponent().setVERS((String) wiz.getProperty(HEADER + ":" + GEDC + ":" + VERS));
        getComponent().setDEST((String) wiz.getProperty(HEADER + ":" + DEST));
        
    }

    @Override
    public void storeSettings(Object data) {
        wiz = (WizardDescriptor) data;
        
        wiz.putProperty(HEADER + ":" + LANG, getComponent().getLANG());
        wiz.putProperty(HEADER + ":" + CHAR, getComponent().getCHAR());
        wiz.putProperty(HEADER + ":" + GEDC + ":" + VERS, getComponent().getVERS());
        wiz.putProperty(HEADER + ":" + DEST, getComponent().getDEST());
        wiz.putProperty(CONV_VERSION, getComponent().getConversionToBeDone() ? CONVERSION : NO_CONVERSION);
        wiz.putProperty(CONV_VERSION_FROM, originalVersion);
        wiz.putProperty(CONV_VERSION_TO, getComponent().getVERS());
        wiz.putProperty(CONV_MEDIA, getComponent().getMediaTransformation());
    }

    @Override
    public void validate() throws WizardValidationException {
    }
    
    public void warnVersionChange(boolean canBeConverted) {
        if (wiz == null) return;
        if (canBeConverted) {
           wiz.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "WNG_GedcomChanged", originalVersion, getComponent().getVERS()));    
        } else {
           wiz.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);     
        }
    }

    public String getOriginalVersion() {
        return originalVersion;
    }
}
