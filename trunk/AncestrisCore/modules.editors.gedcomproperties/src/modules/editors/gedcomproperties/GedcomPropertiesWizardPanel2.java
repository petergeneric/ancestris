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
import genj.gedcom.Property;
import genj.gedcom.Submitter;
import genj.util.AncestrisPreferences;
import genj.util.Registry;
import java.util.Calendar;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class GedcomPropertiesWizardPanel2 implements WizardDescriptor.ValidatingPanel {

    private final static String SUBM_NAME = "submName";
    private final static String SUBM_ADDR = "submAddress";
    private final static String SUBM_POST = "submPostcode";
    private final static String SUBM_CITY = "submCity";
    private final static String SUBM_STAE = "submState";
    private final static String SUBM_CTRY = "submCountry";
    private final static String SUBM_PHON = "submPhone";
    private final static String SUBM_EMAI = "submEmail";
    private final static String SUBM_WWW  = "submWeb";
    
    private final int mode = GedcomPropertiesWizardIterator.getMode();
    private final Property prop_HEAD = GedcomPropertiesWizardIterator.getHead();
    private final Submitter prop_SUBM = GedcomPropertiesWizardIterator.getSubmitter();

    private Property prop_COPR;
    
    private boolean doNotRepeatQuestion = false;

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GedcomPropertiesVisualPanel2 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public GedcomPropertiesVisualPanel2 getComponent() {
        if (component == null) {
            component = new GedcomPropertiesVisualPanel2();
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
        // If it is always OK to press Next or Finish, then return true.
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

    /**
     * FIXME:
     * Copyright par défaut : "(C) Copyright - Ancestris 201X"
     * Précédé du prénom et nom de l'auteur au fur et à mesure que
     * l'utilisateur tape son nom :
     *          "Frédéric Lapeyre (C) Copyright - Ancestris 2015"
     * 
     * @param data 
     */
    @Override
    public void readSettings(Object data) {
        // read gedcom head properties and create them if they do not exist
        
        // set panel fields
        String defaultName = "";
        if (mode == GedcomPropertiesWizardIterator.CREATION_MODE) {
            setSubmitterFromDefault(prop_SUBM);
            defaultName = prop_SUBM.getName();
        }
        prop_COPR = prop_HEAD.getProperty("COPR");
        if (prop_COPR == null) {
            String defaultCopr = NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "DFT_Copyright") + " " + Calendar.getInstance().get(Calendar.YEAR);
            prop_COPR = prop_HEAD.addProperty("COPR", defaultName + " " + defaultCopr);
        }
        
        // display fields
        ((GedcomPropertiesVisualPanel2) getComponent()).setSUBMName(prop_SUBM.getName());
        ((GedcomPropertiesVisualPanel2) getComponent()).setSUBMAddress(prop_SUBM.getAddress());
        ((GedcomPropertiesVisualPanel2) getComponent()).setSUBMPostcode(prop_SUBM.getPostcode());
        ((GedcomPropertiesVisualPanel2) getComponent()).setSUBMCity(prop_SUBM.getCity());
        ((GedcomPropertiesVisualPanel2) getComponent()).setSUBMState(prop_SUBM.getState());
        ((GedcomPropertiesVisualPanel2) getComponent()).setSUBMCountry(prop_SUBM.getCountry());
        ((GedcomPropertiesVisualPanel2) getComponent()).setSUBMPhone(prop_SUBM.getPhone());
        ((GedcomPropertiesVisualPanel2) getComponent()).setSUBMEmail(prop_SUBM.getEmail());
        ((GedcomPropertiesVisualPanel2) getComponent()).setSUBMWeb(prop_SUBM.getWeb());
        ((GedcomPropertiesVisualPanel2) getComponent()).setCOPR(prop_COPR.getDisplayValue());
    }

    @Override
    public void storeSettings(Object data) {
        // read panel fields into properties directly
        prop_SUBM.setName(((GedcomPropertiesVisualPanel2) getComponent()).getSUBMName());
        prop_SUBM.setAddress(((GedcomPropertiesVisualPanel2) getComponent()).getSUBMAddress());
        prop_SUBM.setPostcode(((GedcomPropertiesVisualPanel2) getComponent()).getSUBMPostcode());
        prop_SUBM.setCity(((GedcomPropertiesVisualPanel2) getComponent()).getSUBMCity());
        prop_SUBM.setState(((GedcomPropertiesVisualPanel2) getComponent()).getSUBMState());
        prop_SUBM.setCountry(((GedcomPropertiesVisualPanel2) getComponent()).getSUBMCountry());
        prop_SUBM.setPhone(((GedcomPropertiesVisualPanel2) getComponent()).getSUBMPhone());
        prop_SUBM.setEmail(((GedcomPropertiesVisualPanel2) getComponent()).getSUBMEmail());
        prop_SUBM.setWeb(((GedcomPropertiesVisualPanel2) getComponent()).getSUBMWeb());
        prop_COPR.setValue(((GedcomPropertiesVisualPanel2) getComponent()).getCOPR());
        
        // if submitter has changed, ask used if it needs to be saved as the new defaut
        if (!doNotRepeatQuestion && hasChangedSubmitter(prop_SUBM) && UserConfirmsToSaveAsDefault()) {
            saveSubmitterAsDefault(prop_SUBM);
        }
    }

    private boolean hasChangedSubmitter(Submitter submitter) {
        AncestrisPreferences submPref = Registry.get(GedcomPropertiesWizardIterator.class);
        return !submitter.getName().equals(submPref.get(SUBM_NAME, ""))
            || !submitter.getAddress().equals(submPref.get(SUBM_ADDR, ""))
            || !submitter.getPostcode().equals(submPref.get(SUBM_POST, ""))
            || !submitter.getCity().equals(submPref.get(SUBM_CITY, ""))
            || !submitter.getState().equals(submPref.get(SUBM_STAE, ""))
            || !submitter.getCountry().equals(submPref.get(SUBM_CTRY, ""))
            || !submitter.getPhone().equals(submPref.get(SUBM_PHON, ""))
            || !submitter.getEmail().equals(submPref.get(SUBM_EMAI, ""))
            || !submitter.getWeb().equals(submPref.get(SUBM_WWW, ""));
    }

    private void setSubmitterFromDefault(Submitter submitter) {
        AncestrisPreferences submPref = Registry.get(GedcomPropertiesWizardIterator.class);
        submitter.setName(submPref.get(SUBM_NAME, ""));            // NAME
        submitter.setAddress(submPref.get(SUBM_ADDR, ""));         // ADDR : POST, CITY, STAE, CTRY
        submitter.setPostcode(submPref.get(SUBM_POST, ""));
        submitter.setCity(submPref.get(SUBM_CITY, ""));   
        submitter.setState(submPref.get(SUBM_STAE, ""));   
        submitter.setCountry(submPref.get(SUBM_CTRY, ""));
        submitter.setPhone(submPref.get(SUBM_PHON, ""));          // PHON
        submitter.setEmail(submPref.get(SUBM_EMAI, ""));          // _EMAIL
        submitter.setWeb(submPref.get(SUBM_WWW, ""));              // _WWW
    }

    private void saveSubmitterAsDefault(Submitter submitter) {
        AncestrisPreferences submPref = Registry.get(GedcomPropertiesWizardIterator.class);
        submPref.put(SUBM_NAME, submitter.getName());
        submPref.put(SUBM_ADDR, submitter.getAddress());
        submPref.put(SUBM_POST, submitter.getPostcode());
        submPref.put(SUBM_CITY, submitter.getCity());
        submPref.put(SUBM_STAE, submitter.getState());
        submPref.put(SUBM_CTRY, submitter.getCountry());
        submPref.put(SUBM_PHON, submitter.getPhone());
        submPref.put(SUBM_EMAI, submitter.getEmail());
        submPref.put(SUBM_WWW, submitter.getWeb());
    }

    private boolean UserConfirmsToSaveAsDefault() {
        boolean ret = DialogManager.YES_OPTION == DialogManager.createYesNo(null, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "QST_SaveNewSubmitter")).show();
        doNotRepeatQuestion = true;
        return ret;
    }

    @Override
    public void validate() throws WizardValidationException {
        if (((GedcomPropertiesVisualPanel2) getComponent()).getSUBMName().isEmpty()){
            throw new WizardValidationException(null, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_AuthorIsMandatory"), null);
        }
    }

}
