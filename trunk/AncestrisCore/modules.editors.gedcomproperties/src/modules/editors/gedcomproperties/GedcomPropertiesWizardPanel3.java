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

import genj.gedcom.Gedcom;
import genj.gedcom.Grammar;
import genj.gedcom.Property;
import java.util.Locale;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class GedcomPropertiesWizardPanel3 implements WizardDescriptor.ValidatingPanel {

    private WizardDescriptor wiz = null;
    private final int mode = GedcomPropertiesWizardIterator.getMode();
    private final Property prop_HEAD = GedcomPropertiesWizardIterator.getHead();

    // Language
    private Property prop_LANG;
    
    // Encoding
    private Property prop_CHAR;
    
    // Gedcom version
    private Property prop_GEDC;
    private Property prop_VERS;
    private Property prop_FORM;
    private String originalVersion = "";
    
    // Destination
    private Property prop_DEST;
    
    
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
        // read gedcom head properties and create them if they do not exist
        wiz = (WizardDescriptor) data;
        
        prop_LANG = prop_HEAD.getProperty("LANG");
        if (prop_LANG == null) {
            prop_LANG = prop_HEAD.addProperty("LANG", Locale.getDefault().getDisplayLanguage(new Locale("en", "EN")));
        }
        prop_CHAR = prop_HEAD.getProperty("CHAR");
        if (prop_CHAR == null) {
            prop_CHAR = prop_HEAD.addProperty("CHAR", Gedcom.UTF8);
        }
        prop_GEDC = prop_HEAD.getProperty("GEDC");
        if (prop_GEDC == null) {
            prop_GEDC = prop_HEAD.addProperty("GEDC", "");
        }
        prop_VERS = prop_GEDC.getProperty("VERS");
        if (prop_VERS == null) {
            prop_VERS = prop_GEDC.addProperty("VERS", Grammar.GRAMMAR551);
        }
        prop_FORM = prop_GEDC.getProperty("FORM");
        if (prop_FORM == null) {
            prop_FORM = prop_GEDC.addProperty("FORM", "Lineage-Linked");
        }
        prop_DEST = prop_HEAD.getProperty("DEST");
        if (prop_DEST == null) {
            prop_DEST = prop_HEAD.addProperty("DEST", "ANY");
        }

        // Remember original version
        if (originalVersion.isEmpty()) {
            originalVersion = prop_VERS.getValue();
        }

        // set fields to read values
        ((GedcomPropertiesVisualPanel3) getComponent()).setLANG(prop_LANG.getDisplayValue());
        ((GedcomPropertiesVisualPanel3) getComponent()).setCHAR(prop_CHAR.getDisplayValue());
        ((GedcomPropertiesVisualPanel3) getComponent()).setVERS(prop_VERS.getDisplayValue());
        ((GedcomPropertiesVisualPanel3) getComponent()).setDEST(prop_DEST.getDisplayValue());

    }

    @Override
    public void storeSettings(Object data) {
        // read panel fields into properties directly
        prop_LANG.setValue(((GedcomPropertiesVisualPanel3) getComponent()).getLANG());
        prop_CHAR.setValue(((GedcomPropertiesVisualPanel3) getComponent()).getCHAR());
        prop_VERS.setValue(((GedcomPropertiesVisualPanel3) getComponent()).getVERS());
        prop_DEST.setValue(((GedcomPropertiesVisualPanel3) getComponent()).getDEST());
        wiz.putProperty("ConversionVersion", ((GedcomPropertiesVisualPanel3) getComponent()).getConversionToBeDone() ? "1" : "0");
        wiz.putProperty("ConversionVersionFrom", originalVersion);
        wiz.putProperty("ConversionVersionTo", prop_VERS.getValue());
    }

    @Override
    public void validate() throws WizardValidationException {
    }
    
    public void warnVersionChange(boolean canBeConverted) {
        if (wiz == null) return;
        if (canBeConverted) {
           wiz.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "WNG_GedcomChanged", originalVersion, ((GedcomPropertiesVisualPanel3) getComponent()).getVERS()));    
        } else {
           wiz.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);     
        }
    }

    public String getOriginalVersion() {
        return originalVersion;
    }
}
