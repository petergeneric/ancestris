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

import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class GedcomPropertiesWizardPanel6 implements WizardDescriptor.ValidatingPanel {

    private final Indi firstIndi = GedcomPropertiesWizardIterator.getFirstIndi();

    // Place format
    private Property prop_NAME;
    private Property prop_BIRT;

    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GedcomPropertiesVisualPanel6 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public GedcomPropertiesVisualPanel6 getComponent() {
        if (component == null) {
            component = new GedcomPropertiesVisualPanel6();
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
    public void validate() throws WizardValidationException {
        if (((GedcomPropertiesVisualPanel6) getComponent()).getFirstName().isEmpty()){
            ((GedcomPropertiesVisualPanel6) getComponent()).setFirstNameFocus();
            throw new WizardValidationException(null, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_FirstNameIsMandatory"), null);
        }
        if (((GedcomPropertiesVisualPanel6) getComponent()).getLastName().isEmpty()){
            ((GedcomPropertiesVisualPanel6) getComponent()).setLastNameFocus();
            throw new WizardValidationException(null, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_LastNameIsMandatory"), null);
        }
    }

    @Override
    public void readSettings(Object data) {
    }

    @Override
    public void storeSettings(Object data) {
        firstIndi.setName((((GedcomPropertiesVisualPanel6) getComponent()).getFirstName()), (((GedcomPropertiesVisualPanel6) getComponent()).getLastName()));
        firstIndi.setSex((((GedcomPropertiesVisualPanel6) getComponent()).getSex()) ? PropertySex.MALE : PropertySex.FEMALE);
    }

}
