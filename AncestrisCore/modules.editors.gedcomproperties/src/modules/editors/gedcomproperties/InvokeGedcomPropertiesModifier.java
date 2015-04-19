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
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
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
    
    private Gedcom gedcom;

    @Override
    public Context create() {
        // Create brand new gedcom and head property
        gedcom = new Gedcom();
        Property prop_HEAD;
        try {
            prop_HEAD = gedcom.createEntity("HEAD");
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        
        // Call wizard
        WizardDescriptor wiz = new WizardDescriptor(new GedcomPropertiesWizardIterator(GedcomPropertiesWizardIterator.CREATION_MODE, prop_HEAD));
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "TITLE_create"));
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            // create temporary Adam // TODO : remove once panel 5 is done
            try {
                Entity adam = gedcom.createEntity(Gedcom.INDI);
                adam.addProperty("NAME", "Adam");
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            // save gedcom as new gedcom. NewGedcom in defaultDirectory will reopen it with default views, automatically
            GedcomDirectory.getDefault().newGedcom(gedcom, "title", "defaultname");
        }
        return null;
    }

    @Override
    public Context update() {
        // Get selected gedcom
        Context context = Utilities.actionsGlobalContext().lookup(Context.class);
        if (context == null) {
            List<Context> cs = GedcomDirectory.getDefault().getContexts();
            if (cs == null || cs.isEmpty()) {
                Utils.DisplayMessage(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_NoGedcomFound"));
                return null;
            }
            context = cs.get(0);
        }
        gedcom = context.getGedcom();
        
        // Copy header to a working copy for the wizard. This avoids unwanted changes. Changes will only be applied if user clicks finish.
        Property prop_OriginalHeader = gedcom.getFirstEntity("HEAD");
        if (prop_OriginalHeader == null) {
            try {
                prop_OriginalHeader = gedcom.createEntity("HEAD");
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        
        Property prop_HEAD;
        try {
            prop_HEAD = new Gedcom().createEntity("HEAD");
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        Utils.CopyProperty(prop_OriginalHeader, prop_HEAD);
        
        // Call wizard
        WizardDescriptor wiz = new WizardDescriptor(new GedcomPropertiesWizardIterator(GedcomPropertiesWizardIterator.UPDATE_MODE, prop_HEAD));
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "TITLE_update", gedcom.getName().replaceFirst("[.][^.]+$", "")));   // remove extension to filename
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            prop_OriginalHeader.delProperties();
            Utils.CopyProperty(prop_HEAD, prop_OriginalHeader);
            GedcomDirectory.getDefault().saveGedcom(context);
        }
        return null;
    }

    @Override
    public boolean isReady() {
        return true; // module is ready to be used
    }
}
