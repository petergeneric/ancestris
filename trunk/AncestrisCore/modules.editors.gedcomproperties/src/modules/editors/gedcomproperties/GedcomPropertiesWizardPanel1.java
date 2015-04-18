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

import genj.gedcom.Property;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class GedcomPropertiesWizardPanel1 implements WizardDescriptor.Panel<WizardDescriptor> {

    private final int mode = GedcomPropertiesWizardIterator.getMode();
    private final Property prop_HEAD = GedcomPropertiesWizardIterator.getHead();

    private Property prop_FILE;
    private Property prop_NOTE;

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GedcomPropertiesVisualPanel1 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public GedcomPropertiesVisualPanel1 getComponent() {
        if (component == null) {
            component = new GedcomPropertiesVisualPanel1();
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
    public void readSettings(WizardDescriptor wiz) {
        // use wiz.getProperty to retrieve previous panel state
        
        // read gedcom head properties and ccreate them if they do not exist
        prop_FILE = prop_HEAD.getProperty("FILE");
        if (prop_FILE == null) {
            prop_FILE = prop_HEAD.addProperty("FILE", NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "Panel1.jTextField1.create"));
        }
        
        prop_NOTE = prop_HEAD.getProperty("NOTE");
        if (prop_NOTE == null) {
            prop_NOTE = prop_HEAD.addProperty("NOTE", "");
        }
        
        // set panel fields
        ((GedcomPropertiesVisualPanel1) getComponent()).setFILE(prop_FILE.getDisplayValue());
        ((GedcomPropertiesVisualPanel1) getComponent()).setNOTE(prop_NOTE.getDisplayValue());
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
        
        // read panel fields into properties directly
        prop_FILE.setValue(((GedcomPropertiesVisualPanel1) getComponent()).getFILE());
        prop_NOTE.setValue(((GedcomPropertiesVisualPanel1) getComponent()).getNOTE());
    }

}
