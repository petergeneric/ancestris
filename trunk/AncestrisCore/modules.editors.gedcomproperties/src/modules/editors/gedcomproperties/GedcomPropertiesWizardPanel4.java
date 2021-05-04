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

import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import javax.swing.event.ChangeListener;
import modules.editors.gedcomproperties.utils.PlaceFormatConverterPanel;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import modules.editors.gedcomproperties.utils.PlaceFormatterInterface;

public class GedcomPropertiesWizardPanel4 implements WizardDescriptor.ValidatingPanel, Constants, PlaceFormatterInterface {

    private WizardDescriptor wiz = null;
    private final int mode = GedcomPropertiesWizardIterator.getMode();
    private final Gedcom gedcom = GedcomPropertiesWizardIterator.getGedcom();

    // Place format
    private String originalPlaceformat = "";
    
    // Place Format Converter
    PlaceFormatConverterPanel pfc;

    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GedcomPropertiesPlaceFormatPanel component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public GedcomPropertiesPlaceFormatPanel getComponent() {
        if (component == null) {
            component = new GedcomPropertiesPlaceFormatPanel(this);
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
        
        // Remember original version
        if (originalPlaceformat.isEmpty()) {
            originalPlaceformat = (String) wiz.getProperty(HEADER + ":" + PLAC + ":" + FORM);
        }

        getComponent().setPLAC((String) wiz.getProperty(HEADER + ":" + PLAC + ":" + FORM));
    }

    @Override
    public void storeSettings(Object data) {
        wiz = (WizardDescriptor) data;
        
        wiz.putProperty(HEADER + ":" + PLAC + ":" + FORM, getComponent().getPLAC());

        if (getComponent().getConversionToBeDone()) {
            wiz.putProperty(CONV_PLACE, CONVERSION);
            wiz.putProperty(CONV_PLACE_FROM, originalPlaceformat); // from format
            wiz.putProperty(CONV_PLACE_TO, getComponent().getPLAC());  // to format
            if (pfc != null) {
                wiz.putProperty(CONV_PLACE_MAP,pfc.getConversionMapAsString());    // list to-fields0:from-fieldi,to-fields1:from-fieldj,etc
            }
        } else {
            wiz.putProperty(CONV_PLACE, NO_CONVERSION);
        }
        if (getComponent().getPlacesAlignmentToBeDone()) {
            wiz.putProperty(ALIGN_PLACE, CONVERSION);
        } else {
            wiz.putProperty(ALIGN_PLACE, NO_CONVERSION);
        }
    }

    @Override
    public void validate() throws WizardValidationException {
        Boolean canBeConverted = (mode == UPDATE) && !((GedcomPropertiesPlaceFormatPanel) getComponent()).getPLAC().equals(originalPlaceformat);
        Boolean isConversionRequired = ((GedcomPropertiesPlaceFormatPanel) getComponent()).getConversionToBeDone();
        if (canBeConverted && isConversionRequired) {
            if ((pfc == null) || !pfc.isValidatedMap()) {
                throw new WizardValidationException(null, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_ConversionMapMandatory"), null);
            }
            if ((pfc != null) & !pfc.isMapComplete()) {
                Object o = DialogManager.createYesNo(
                    NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "GedcomPropertiesPlaceFormatPanel.jCheckBox1.text"), 
                    NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "WNG_IncompletMapToConfirm")).setMessageType(DialogManager.YES_NO_OPTION).show();
                if (o != DialogManager.YES_OPTION) {
                    throw new WizardValidationException(null, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_ConversionMapMandatory"), null);
                }
            }
            
        }
    }

    @Override
    public void warnVersionChange(boolean canBeConverted) {
        if (wiz == null) return;
        if (canBeConverted) {
           wiz.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "WNG_PlaceFormatChanged"));    
        } else {
           wiz.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);     
        }
    }

    @Override
    public String getOriginalPlaceFormat() {
        return originalPlaceformat;
    }

    @Override
    public void setPlaceFormatConverter(PlaceFormatConverterPanel pfc) {
        this.pfc = pfc;
    }

    @Override
    public PlaceFormatConverterPanel getPlaceFormatConverter() {
        return this.pfc;
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public Gedcom getGedcom() {
        return gedcom;
    }

    @Override
    public void setConfirmEnabled(boolean set) {
    }

}
