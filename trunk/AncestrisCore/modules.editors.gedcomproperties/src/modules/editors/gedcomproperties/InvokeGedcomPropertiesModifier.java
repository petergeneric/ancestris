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
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.Property;
import genj.gedcom.Submitter;
import java.text.MessageFormat;
import java.util.List;
import modules.editors.gedcomproperties.utils.Utils;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author frederic
 */
@ServiceProvider(service=ModifyGedcom.class)
public class InvokeGedcomPropertiesModifier implements ModifyGedcom{
    
    @Override
    public Context create() {
        // Create brand new gedcom and head property
        Gedcom gedcom = new Gedcom();
        Property prop_HEAD;
        Submitter prop_SUBM;
        try {
            prop_HEAD = gedcom.createEntity("HEAD", "");
            gedcom.createEntity(Gedcom.SUBM);
            prop_SUBM = gedcom.getSubmitter();
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        
        // Call wizard
        WizardDescriptor wiz = new WizardDescriptor(new GedcomPropertiesWizardIterator(GedcomPropertiesWizardIterator.CREATION_MODE, prop_HEAD, prop_SUBM));
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "TITLE_create"));
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            // create temporary Adam // TODO : remove once panel 6 is done
            try {
                Entity adam = gedcom.createEntity(Gedcom.INDI);
                adam.addProperty("NAME", "Adam");
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            // save gedcom as new gedcom. User will be asked to name a filename and to save.
            // Function NewGedcom in GedcomDirectory will reopen it with default views, automatically
            String filename = prop_HEAD.getProperty("FILE").getDisplayValue();
            GedcomDirectory.getDefault().newGedcom(gedcom, NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "TITLE_create") + " '" + filename + "'", filename, false);
        } else {
            DialogManager.create(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomNotCreatedTitle"), 
                    NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomNotCreated")).setMessageType(DialogManager.INFORMATION_MESSAGE).show();
        }

        return null;
    }

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
    
    @Override
    public Context update(Context context) {
        // Get selected gedcom
        Gedcom originalGedcom = context.getGedcom();
        
        // Copy header and submitter to a working copy for the wizard. This avoids unwanted changes. Changes will only be applied if user clicks finish.
        Property prop_OriginalHeader = originalGedcom.getFirstEntity("HEAD");
        if (prop_OriginalHeader == null) {
            try {
                prop_OriginalHeader = originalGedcom.createEntity("HEAD");
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        Submitter prop_OriginalSubmitter = originalGedcom.getSubmitter();
        if (prop_OriginalSubmitter == null) {
            try {
                originalGedcom.createEntity(Gedcom.SUBM);
                prop_OriginalSubmitter = originalGedcom.getSubmitter();
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        
        Gedcom tmpGedcom ;
        Property prop_HEAD;
        Submitter prop_SUBM;
        try {
            tmpGedcom = new Gedcom();
            prop_HEAD = tmpGedcom.createEntity("HEAD", "");
            tmpGedcom.createEntity(Gedcom.SUBM);
            prop_SUBM = tmpGedcom.getSubmitter();
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        Utils.CopyProperty(prop_OriginalHeader, prop_HEAD);
        Utils.CopyProperty(prop_OriginalSubmitter, prop_SUBM);
        
        // Call wizard
        WizardDescriptor wiz = new WizardDescriptor(new GedcomPropertiesWizardIterator(GedcomPropertiesWizardIterator.UPDATE_MODE, prop_HEAD, prop_SUBM));
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "TITLE_update", originalGedcom.getName().replaceFirst("[.][^.]+$", "")));   // remove extension to filename
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            String title = NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomModifiedSuccessfullyTitle");
            String message = "<html><h1>Results:</h1>";
            
            // Replace header and report
            message += "<h2>Gedcom header</h2>";
            prop_OriginalHeader.delProperties();
            prop_OriginalSubmitter.delProperties();
            Utils.CopyProperty(prop_HEAD, prop_OriginalHeader);
            originalGedcom.setGrammar(prop_HEAD.getPropertyByPath("HEAD:GEDC:VERS").getDisplayValue().equals("5.5.1") ? Grammar.V551 : Grammar.V55);
            originalGedcom.setDestination(prop_HEAD.getPropertyByPath("HEAD:DEST").getDisplayValue());
            originalGedcom.setLanguage(prop_HEAD.getPropertyByPath("HEAD:LANG").getDisplayValue());
            originalGedcom.setEncoding(prop_HEAD.getPropertyByPath("HEAD:CHAR").getDisplayValue());
            Utils.CopyProperty(prop_SUBM, prop_OriginalSubmitter);
            message += "<p>Modified successfully</p>";
            
            // If conversion of gedcom norm requested, do it and report
            if (wiz.getProperty("Conversion") == "1") {
                message += "<h2>Conversion of Gedcom version from " + wiz.getProperty("ConversionFrom") + " to " + wiz.getProperty("ConversionTo") + "</h2>";
                // TODO: do it
                message += "<p>Conversion done successfully</p>";
            }
            // Save modified gedcom and report
            message += "<h2>Gedcom file</h2>";
            GedcomDirectory.getDefault().saveGedcom(context);
            message += "<p>Saved successfully</p>";
            
            // Display results
            message += "<br><p>" + NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomModifiedSuccessfully") + "</p>";
            message += "<br>&nbsp;<br>&nbsp;<br></html>";
            DialogManager.create(title, message).setMessageType(DialogManager.INFORMATION_MESSAGE).show();
        } else {
            DialogManager.create(
                    NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomNotModifiedTitle"), 
                    NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_GedcomNotModified")).setMessageType(DialogManager.INFORMATION_MESSAGE).show();
        }
        return null;
    }

    @Override
    public boolean isReady() {
        return true; // module is ready to be used
    }

}
