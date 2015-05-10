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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

public class GedcomPropertiesWizardPanel5 implements WizardDescriptor.Panel<WizardDescriptor> {

    private final int mode = GedcomPropertiesWizardIterator.getMode();
    private final Property prop_HEAD = GedcomPropertiesWizardIterator.getHead();

    // Source format
    private Property prop_SOUR; //+1 SOUR <APPROVED_SYSTEM_ID>  {1:1}
    private Property prop_VERS; //+2 VERS <VERSION_NUMBER>  {0:1}
    private Property prop_NAME; //+2 NAME <NAME_OF_PRODUCT>  {0:1} (rarely used by others software but used by Ancestris)
    private Property prop_CORP; //+2 CORP <NAME_OF_BUSINESS>  {0:1}
    private Property prop_ADDR; //+3 <<ADDRESS_STRUCTURE>>  {0:1}
    
    private Property prop_DATE; //+1 DATE <TRANSMISSION_DATE>  {0:1}
    private Property prop_TIME; //+2 TIME <TIME_VALUE>  {0:1}

// unused:
//          +2 DATA <NAME_OF_SOURCE_DATA>  {0:1}
//        +3 DATE <PUBLICATION_DATE>  {0:1}
//        +3 COPR <COPYRIGHT_SOURCE_DATA>  {0:1}
    
//<<ADDRESS_STRUCTURE>>
//  n  ADDR <ADDRESS_LINE>  {0:1}
//    +1 CONT <ADDRESS_LINE>  {0:M}
//    +1 ADR1 <ADDRESS_LINE1>  {0:1}
//    +1 ADR2 <ADDRESS_LINE2>  {0:1}
//    +1 CITY <ADDRESS_CITY>  {0:1}
//    +1 STAE <ADDRESS_STATE>  {0:1}
//    +1 POST <ADDRESS_POSTAL_CODE>  {0:1}
//    +1 CTRY <ADDRESS_COUNTRY>  {0:1}
//  n  PHON <PHONE_NUMBER>  {0:3}    
//     (WEB, _WEB, ou _ADDR pour le site web)
//     (EMAIL pour email)

    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GedcomPropertiesVisualPanel5 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public GedcomPropertiesVisualPanel5 getComponent() {
        if (component == null) {
            component = new GedcomPropertiesVisualPanel5();
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
        prop_SOUR = prop_HEAD.getProperty("SOUR");
        if (prop_SOUR == null) {
            prop_SOUR = prop_HEAD.addProperty("SOUR", "ANCESTRIS");
        }
        
        prop_VERS = prop_SOUR.getProperty("VERS");
        if (prop_VERS == null) {
            prop_VERS = prop_SOUR.addProperty("VERS", Lookup.getDefault().lookup(ancestris.api.core.Version.class).getVersionString());
        }
        
        prop_NAME = prop_SOUR.getProperty("NAME");
        if (prop_NAME == null) {
            prop_NAME = prop_SOUR.addProperty("NAME", "Ancestris");
        }
        
        prop_CORP = prop_SOUR.getProperty("CORP");
        if (prop_CORP == null) {
            prop_CORP = prop_SOUR.addProperty("CORP", "Ancestris Team");
        }
        
        prop_ADDR = prop_CORP.getProperty("ADDR");
        if (prop_ADDR == null) {
            prop_ADDR = prop_CORP.addProperty("ADDR", "http://www.ancestris.org");
        }
        
        prop_DATE = prop_HEAD.getProperty("DATE");
        if (prop_DATE == null) {
            prop_DATE = prop_HEAD.addProperty("DATE", new SimpleDateFormat("dd MMM yyyy").format(Calendar.getInstance().getTime()));
        }
        
        prop_TIME = prop_DATE.getProperty("TIME");
        if (prop_TIME == null) {
            prop_TIME = prop_DATE.addProperty("TIME", new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime()));
        }
        
        // set fields to read values
        ((GedcomPropertiesVisualPanel5) getComponent()).setSOUR(prop_SOUR.getDisplayValue());
        ((GedcomPropertiesVisualPanel5) getComponent()).setVERS(prop_VERS.getDisplayValue());
        ((GedcomPropertiesVisualPanel5) getComponent()).setNAME(prop_NAME.getDisplayValue());
        ((GedcomPropertiesVisualPanel5) getComponent()).setCORP(prop_CORP.getDisplayValue());
        ((GedcomPropertiesVisualPanel5) getComponent()).setADDR(prop_CORP);
        ((GedcomPropertiesVisualPanel5) getComponent()).setDATE(prop_DATE.getDisplayValue());
        ((GedcomPropertiesVisualPanel5) getComponent()).setTIME(prop_TIME.getDisplayValue());
        
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
    }

}
