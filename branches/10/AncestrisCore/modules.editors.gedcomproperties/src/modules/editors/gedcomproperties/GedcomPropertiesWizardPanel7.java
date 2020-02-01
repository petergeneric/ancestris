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

public class GedcomPropertiesWizardPanel7 implements WizardDescriptor.ValidatingPanel, Constants {

    private WizardDescriptor wiz = null;

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GedcomPropertiesVisualPanel7 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public GedcomPropertiesVisualPanel7 getComponent() {
        if (component == null) {
            component = new GedcomPropertiesVisualPanel7();
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
        if (getComponent().getFirstName().isEmpty()){
            getComponent().setFirstNameFocus();
            throw new WizardValidationException(null, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_FirstNameIsMandatory"), null);
        }
        if (getComponent().getLastName().isEmpty()){
            getComponent().setLastNameFocus();
            throw new WizardValidationException(null, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_LastNameIsMandatory"), null);
        }
    }

    @Override
    public void readSettings(Object data) {
    }

    @Override
    public void storeSettings(Object data) {
        wiz = (WizardDescriptor) data;
        
        wiz.putProperty(INDI + ":" + FIRSTNAME, getComponent().getFirstName());
        wiz.putProperty(INDI + ":" + LASTNAME, getComponent().getLastName());
        wiz.putProperty(INDI + ":" + SEX, getComponent().getSex());
    }

}
