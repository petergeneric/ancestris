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

import genj.gedcom.GedcomOptions;
import genj.gedcom.Property;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class GedcomPropertiesWizardPanel4 implements WizardDescriptor.ValidatingPanel {

    private WizardDescriptor wiz = null;
    private final int mode = GedcomPropertiesWizardIterator.getMode();
    private final Property prop_HEAD = GedcomPropertiesWizardIterator.getHead();

    // Place format
    private Property prop_PLAC;
    private Property prop_FORM;
    private String originalPlaceformat = "";

    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GedcomPropertiesVisualPanel4 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public GedcomPropertiesVisualPanel4 getComponent() {
        if (component == null) {
            component = new GedcomPropertiesVisualPanel4(this);
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
        // read gedcom head properties and create them if they do not exist
        wiz = (WizardDescriptor) data;
        
        prop_PLAC = prop_HEAD.getProperty("PLAC");
        if (prop_PLAC == null) {
            prop_PLAC = prop_HEAD.addProperty("PLAC", "");
        }
        
        prop_FORM = prop_PLAC.getProperty("FORM");
        if (prop_FORM == null) {
            prop_FORM = prop_PLAC.addProperty("FORM", GedcomOptions.getInstance().getPlaceFormat());
        }
        
        // Remember original version
        if (originalPlaceformat.isEmpty()) {
            originalPlaceformat = prop_FORM.getValue();
        }

        // set fields to read values
        ((GedcomPropertiesVisualPanel4) getComponent()).setPLAC(prop_FORM.getDisplayValue());
        
    }

    @Override
    public void storeSettings(Object data) {
        prop_FORM.setValue(((GedcomPropertiesVisualPanel4) getComponent()).getPLAC());
        wiz.putProperty("ConversionPlaceFormat", ((GedcomPropertiesVisualPanel4) getComponent()).getConversionToBeDone() ? "1" : "0");
        wiz.putProperty("ConversionPlaceFormatFrom", originalPlaceformat);
        wiz.putProperty("ConversionPlaceFormatTo", prop_FORM.getValue());
    }

    @Override
    public void validate() throws WizardValidationException {
    }

    public void warnVersionChange(boolean canBeConverted) {
        if (wiz == null) return;
        if (canBeConverted) {
           wiz.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "WNG_PlaceFormatChanged"));    
        } else {
           wiz.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);     
        }
    }

    
    public String getOriginalPlaceFormat() {
        return originalPlaceformat;
    }
}
