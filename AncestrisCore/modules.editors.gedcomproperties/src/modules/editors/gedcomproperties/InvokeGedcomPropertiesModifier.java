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

import ancestris.api.newgedcom.ModifyGedcom;
import ancestris.gedcom.GedcomDirectory;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Grammar;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.Submitter;
import genj.gedcom.UnitOfWork;
import genj.util.AncestrisPreferences;
import genj.util.Registry;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import javax.swing.JPanel;
import modules.editors.gedcomproperties.utils.GedcomPlacesConverter;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author frederic
 */
@ServiceProvider(service=ModifyGedcom.class)
public class InvokeGedcomPropertiesModifier implements ModifyGedcom, Constants {
    
    private Gedcom gedcom = null;
    private WizardDescriptor wiz = null;
    private int mode = CREATION_OR_UPDATE;
    
    
    @Override
    public boolean isReady() {
        return true; // module is ready to be used
    }

    
    /**
     * Start assistant in case of Gedcom creation
     * @return 
     */
    @Override
    public Context create() {
        // Create brand new gedcom and head property
        gedcom = new Gedcom();

        // Initiate key entities
        Property prop_HEAD;
        Indi firstIndi;
        try {
            prop_HEAD = gedcom.createEntity(HEADER, "");
            gedcom.createEntity(Gedcom.SUBM);
            gedcom.getSubmitter();
            firstIndi = (Indi) gedcom.createEntity(Gedcom.INDI);
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        
        // Create wizard and feed its data properties from gedcom
        gedcom.initLanguages();
        mode = CREATION;
        wiz = new WizardDescriptor(new GedcomPropertiesWizardIterator(mode));
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "TITLE_create"));
        copyGedcomToProperties();

        // Call wizard and process user choice
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        commit();
                    }
                });
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }

            // Save gedcom as new gedcom. User will be asked to name a filename and to save.
            // Function NewGedcom in GedcomDirectory will reopen it with default views, automatically
            String filename = prop_HEAD.getProperty(FILE).getDisplayValue();
            GedcomDirectory.getDefault().newGedcom(gedcom, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "TITLE_create") + " '" + filename + "'", filename, false);
            return new Context(firstIndi);
            
        } else {
            DialogManager.create(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomNotCreatedTitle"), 
                    NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomNotCreated")).setMessageType(DialogManager.INFORMATION_MESSAGE).show();
            return null;
        }
    }

    /**
     * Start assistant in case of Gedcom modification and context is not indicated
     * 
     * @return 
     */
    @Override
    public Context update() {
        // Get first selected context
        Context context = Utilities.actionsGlobalContext().lookup(Context.class);
        if (context == null) {
            List<Context> cs = GedcomDirectory.getDefault().getContexts();
            if (cs == null || cs.isEmpty()) {
                DialogManager.createError(null, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_NoGedcomFound")).show();
                return null;
            }
            context = cs.get(0);
        }
        update(context);
        return context;
    }
    
    /**
     * Start assistant in case of Gedcom modification with a given context
     * 
     * @return 
     */
    @Override
    public Context update(Context context) {
        // Get selected gedcom
        gedcom = context.getGedcom();
        
        // Create wizard and feed its data properties from gedcom
        mode = UPDATE;
        gedcom.initLanguages();
        wiz = new WizardDescriptor(new GedcomPropertiesWizardIterator(mode));
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "TITLE_update", gedcom.getName().replaceFirst("[.][^.]+$", "")));   // remove extension to filename
        copyGedcomToProperties();

        
        // Call wizard and process user choice
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            if (wiz.getProperty(CONV_VERSION) == CONVERSION) {
                Object o = DialogManager.createYesNo(
                        NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "Panel3.jCheckBox1.update"),
                        NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "WNG_ConfirmVersionConversion")).setMessageType(DialogManager.YES_NO_OPTION).show();
                if (o != DialogManager.YES_OPTION) {
                    notifyCancellation();
                }
            }
            if (wiz.getProperty(CONV_PLACE) == CONVERSION) {
                Object o = DialogManager.createYesNo(
                        NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "GedcomPropertiesVisualPanel4.jCheckBox1.text"),
                        NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "WNG_ConfirmPlaceConversion")).setMessageType(DialogManager.YES_NO_OPTION).show();
                if (o != DialogManager.YES_OPTION) {
                    notifyCancellation();
                }
            }
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        commit();
                    }
                });
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            notifyCancellation();
        }
        return null;
    }

    
    
    /**
     * Copy Gedcom to wizard properties to be displayed in the panels.
     */
    private void copyGedcomToProperties() {
        // Init variables
        AncestrisPreferences submPref = Registry.get(GedcomPropertiesWizardIterator.class);

        // Feed properties
        copyGedPropToWizProp(HEADER + ":" + FILE, CREATION_OR_UPDATE, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "Panel1.jTextField1.create"));
        copyGedPropToWizProp(HEADER + ":" + NOTE, CREATION_OR_UPDATE, "");
        
        String defaultName = "", defaultCopr = "";
        if (mode == CREATION) {
            defaultName = submPref.get(SUBM_NAME, "");
            defaultCopr = defaultName + " " + NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "DFT_Copyright") + " " + Calendar.getInstance().get(Calendar.YEAR);
        }
        copyGedPropToWizProp(SUBM + ":" + NAME, CREATION, defaultName);
        copyGedPropToWizProp(SUBM + ":" + ADDR, CREATION, submPref.get(SUBM_ADDR, ""));
        copyGedPropToWizProp(SUBM + ":" + ADDR + ":" + POST, CREATION, submPref.get(SUBM_POST, ""));
        copyGedPropToWizProp(SUBM + ":" + ADDR + ":" + CITY, CREATION, submPref.get(SUBM_CITY, ""));
        copyGedPropToWizProp(SUBM + ":" + ADDR + ":" + STAE, CREATION, submPref.get(SUBM_STAE, ""));
        copyGedPropToWizProp(SUBM + ":" + ADDR + ":" + CTRY, CREATION, submPref.get(SUBM_CTRY, ""));
        copyGedPropToWizProp(SUBM + ":" + PHON, CREATION, submPref.get(SUBM_PHON, ""));
        copyGedPropToWizProp(SUBM + ":" + EMAI, CREATION, submPref.get(SUBM_EMAI, ""));
        copyGedPropToWizProp(SUBM + ":" + WWW, CREATION, submPref.get(SUBM_WWW, ""));
        copyGedPropToWizProp(HEADER + ":" + COPR, CREATION, defaultCopr);

        copyGedPropToWizProp(HEADER + ":" + LANG, CREATION_OR_UPDATE, Locale.getDefault().getDisplayLanguage(new Locale("en", "EN")));
        copyGedPropToWizProp(HEADER + ":" + CHAR, CREATION_OR_UPDATE, Gedcom.UTF8);
        copyGedPropToWizProp(HEADER + ":" + GEDC + ":" + VERS, CREATION_OR_UPDATE, Grammar.GRAMMAR551);
        copyGedPropToWizProp(HEADER + ":" + DEST, CREATION_OR_UPDATE, "ANY");
        
        copyGedPropToWizProp(HEADER + ":" + PLAC + ":" + FORM, CREATION_OR_UPDATE, GedcomOptions.getInstance().getPlaceFormat());

        copyGedPropToWizProp(HEADER + ":" + SOUR, CREATION, "ANCESTRIS");
        copyGedPropToWizProp(HEADER + ":" + SOUR + ":" + VERS, CREATION, Lookup.getDefault().lookup(ancestris.api.core.Version.class).getVersionString());
        copyGedPropToWizProp(HEADER + ":" + SOUR + ":" + NAME, CREATION, "Ancestris");
        copyGedPropToWizProp(HEADER + ":" + SOUR + ":" + CORP, CREATION, "Ancestris Team");
        
        copyGedPropertiesToWizProp(HEADER + ":" + SOUR + ":" + CORP + ":" + ADDR, CREATION, "http://www.ancestris.org");

        copyGedPropToWizProp(HEADER + ":" + DATE, CREATION_OR_UPDATE, new SimpleDateFormat("dd MMM yyyy").format(Calendar.getInstance().getTime()));
        copyGedPropToWizProp(HEADER + ":" + DATE + ":" + TIME, CREATION_OR_UPDATE, new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime()));
    }
    

    /**
     * Copy wizard properties to Gedcom.
     */
    public void commit() throws GedcomException {

        // Update gedcom header
        Property prop_HEAD = gedcom.getFirstEntity(HEADER);

        replaceOrCreateProperty(HEADER + ":" + FILE);
        replaceOrCreateProperty(HEADER + ":" + NOTE);
        replaceOrCreateProperty(HEADER + ":" + COPR);
        replaceOrCreateProperty(HEADER + ":" + LANG);
        replaceOrCreateProperty(HEADER + ":" + CHAR);
        replaceOrCreateProperty(HEADER + ":" + GEDC + ":" + VERS);
        replaceOrCreateProperty(HEADER + ":" + DEST);
        replaceOrCreateProperty(HEADER + ":" + PLAC + ":" + FORM);
        replaceOrCreateProperty(HEADER + ":" + SOUR);
        replaceOrCreateProperty(HEADER + ":" + SOUR + ":" + VERS);
        replaceOrCreateProperty(HEADER + ":" + SOUR + ":" + NAME);
        replaceOrCreateProperty(HEADER + ":" + SOUR + ":" + CORP);
        replaceOrCreateProperty(HEADER + ":" + SOUR + ":" + CORP + ":" + ADDR);
        
        gedcom.setLanguage((String) wiz.getProperty(HEADER + ":" + LANG));
        gedcom.setEncoding((String) wiz.getProperty(HEADER + ":" + CHAR));
        gedcom.setGrammar(((String) wiz.getProperty(HEADER + ":" + GEDC + ":" + VERS)).equals(Grammar.GRAMMAR551) ? Grammar.V551 : Grammar.V55);
        gedcom.setDestination((String) wiz.getProperty(HEADER + ":" + DEST));
        gedcom.setPlaceFormat((String) wiz.getProperty(HEADER + ":" + PLAC + ":" + FORM));

        
        // Update gedcom submitter
        Submitter submitter = gedcom.getSubmitter();
        
        submitter.setName((String) wiz.getProperty(SUBM + ":" + NAME));
        submitter.setAddress((String) wiz.getProperty(SUBM + ":" + ADDR));
        submitter.setPostcode((String) wiz.getProperty(SUBM + ":" + ADDR + ":" + POST));
        submitter.setCity((String) wiz.getProperty(SUBM + ":" + ADDR + ":" + CITY));
        submitter.setState((String) wiz.getProperty(SUBM + ":" + ADDR + ":" + STAE));
        submitter.setCountry((String) wiz.getProperty(SUBM + ":" + ADDR + ":" + CTRY));
        submitter.setPhone((String) wiz.getProperty(SUBM + ":" + PHON));
        submitter.setEmail((String) wiz.getProperty(SUBM + ":" + EMAI));
        submitter.setWeb((String) wiz.getProperty(SUBM + ":" + WWW));
        
        
        // Update gedcom first individual
        if (mode == CREATION) {
            Indi firstIndi = (Indi) gedcom.getFirstEntity(Gedcom.INDI);
            firstIndi.setName((String) wiz.getProperty(INDI + ":" + FIRSTNAME), (String) wiz.getProperty(INDI + ":" + LASTNAME));
            firstIndi.setSex((Boolean) wiz.getProperty(INDI + ":" + SEX) ? PropertySex.MALE : PropertySex.FEMALE);
            return; // stop here in case of creation. No report displayed. 
        }
        
        
        
        
        // Prepare report of changes
        boolean withErrors = false;
        GedcomPlacesConverter placesConverter = null;
        String message = NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "RSLT_Title");
        message += NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "RSLT_HeaderChanged"); 
        
        // Convert gedcom version if requested
        if (wiz.getProperty(CONV_VERSION) == CONVERSION) {
            // FIXME/TODO: do it
            message += NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "RSLT_VersionChanged", wiz.getProperty(CONV_VERSION_FROM), wiz.getProperty(CONV_VERSION_TO)); 
        }
        
        // Convert place format if requested
        if (wiz.getProperty(CONV_PLACE) == CONVERSION) {
            message += NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "RSLT_PlaceChanged", wiz.getProperty(CONV_PLACE_FROM), wiz.getProperty(CONV_PLACE_TO)); 
            placesConverter = new GedcomPlacesConverter(gedcom, wiz.getProperty(CONV_PLACE_FROM).toString(), wiz.getProperty(CONV_PLACE_TO).toString(), wiz.getProperty(CONV_PLACE_MAP).toString());
            if (placesConverter.convert()) {
                message += NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "RSLT_PlaceSuccessChanged", placesConverter.getNbOfChangedPlaces(), placesConverter.getNbOfFoundPlaces(), placesConverter.getNbOfDifferentChangedPlaces(), placesConverter.getNbOfDifferentFoundPlaces());
            } else {
                message += NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "RSLT_PlaceFailureChanged", placesConverter.getError().getMessage(), placesConverter.getNbOfChangedPlaces(), placesConverter.getNbOfFoundPlaces(), placesConverter.getNbOfDifferentChangedPlaces(), placesConverter.getNbOfDifferentFoundPlaces());
                withErrors = true;
            }
        }

        // Complete report message
        if (!withErrors) {
            message += NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomModifiedSuccessfully");
        } else {
            message += NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomModifiedPartiallySuccessfully");
        }
        message += NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomModifiedEndOfMessage");

        // Display results
        String title = NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomModifiedSuccessfullyTitle");
        JPanel jPanel = new ResultPanel(message, withErrors, placesConverter != null ? placesConverter.getIncorrectPlaces() : null);
        DialogManager.create(title, jPanel).setMessageType(DialogManager.INFORMATION_MESSAGE).show();
    }

    
    
    private void notifyCancellation() { 
        DialogManager.create(
                    NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomNotModifiedTitle"),
                    NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomNotModified")).setMessageType(DialogManager.INFORMATION_MESSAGE).show();
    }

    
    
    
    private void copyGedPropToWizProp(String tagPath, int modeForDefault, String defaultvalue) {
        Property[] props = getProperties(tagPath, false);
        String str = "";
        if (modeForDefault == CREATION_OR_UPDATE) {
            str = defaultvalue;
        } else {
            str = mode == modeForDefault ? defaultvalue : "";
        }
        wiz.putProperty(tagPath, props != null && props.length != 0 ? props[0].getDisplayValue() : str);
    }

    private void copyGedPropertiesToWizProp(String tagPath, int modeForDefault, String defaultvalue) {
        String value = "", str = "";

        // Find property corresponding to tag if any
        Property[] props = getProperties(tagPath, false);
        
        // If not null, get all tags recursively below parent and concatenate them
        if (props != null && props.length != 0) {  
            Property parent = props[0].getParent();
            if (parent != null) {
                List<Property> properties = new ArrayList<Property>();
                getPropertiesRecursively(parent, properties);
                for (Property prop : properties) {
                    value += prop.getDisplayValue() + "<br>";
                }
            }
        }
        
        if (modeForDefault == CREATION_OR_UPDATE) {
            str = defaultvalue;
        } else {
            str = mode == modeForDefault ? defaultvalue : "";
        }
        
        wiz.putProperty(tagPath, props != null ? value : str);
    }

    
    
    private void replaceOrCreateProperty(String tagPath) {
        Property[] props = getProperties(tagPath, true);
        props[0].setValue((String) wiz.getProperty(tagPath));
    }
    
    private Property[] getProperties(String tagPath, boolean createProperty) {
        return getProperties(null, tagPath, createProperty);
    }
    
    private Property[] getProperties(Property parent, String tagPath, boolean createProperty) {
        Property property;
        if (tagPath.contains(":")) {
            String firstTag = tagPath.substring(0, tagPath.indexOf(":"));
            if (firstTag.equals(HEADER) || firstTag.equals(SUBM)) {
                property = gedcom.getFirstEntity(firstTag);
            } else {
                property = parent != null ? parent.getProperty(firstTag) : null;
            }
            if (parent != null && property == null && createProperty) {
                property = parent.addProperty(firstTag, "");
            }
            String restOfPath = tagPath.substring(firstTag.length() + 1);
            return getProperties(property, restOfPath, createProperty);
        }
        if (parent != null) {
            property = parent.getProperty(tagPath);
            if (property == null && createProperty) {
                parent.addProperty(tagPath, "");
            }
            return parent.getProperties(tagPath);
        }
        return null;
    }

    public void getPropertiesRecursively(Property parent, List props) {
        Property[] children = parent.getProperties();
        for (Property child : children) {
            props.add(child);
            getPropertiesRecursively(child, props);
        }
    }

}
