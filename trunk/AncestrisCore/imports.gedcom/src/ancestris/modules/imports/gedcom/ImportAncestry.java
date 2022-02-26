/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2022 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.imports.gedcom;

import ancestris.api.imports.Import;
import ancestris.api.imports.ImportFix;
import static ancestris.modules.imports.gedcom.Bundle.importancestry_name;
import static ancestris.modules.imports.gedcom.Bundle.importancestry_note;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author frederic
 */
@NbBundle.Messages({
    "importancestry.name=Ancestry", 
    "importancestry.note=This file has been modified by the Ancestris Ancestry Import module."
})
@ServiceProvider(service = Import.class)
public class ImportAncestry extends Import {

    /**
     * Constructor
     */
    public ImportAncestry() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importancestry_name();
    }

    @Override
    protected String getImportComment() {
        return importancestry_note();
    }

    @Override
    public void showDetails(Context context, boolean extract) {
        new FixesWindow(summary, context, fixes).displayFixes(extract);
    }
    
    
    
    ///////////////////////////// START OF LOGIC ///////////////////////////////
    
       /**
     * *** 0 *** Initialisation of variables
     */
    protected void init() {
        super.init();
        
    }
 
    /**
     * *** 1 ***
     * - Run generic code
     * - <run specific code>
     */
    @Override
    protected void firstPass() {
        super.firstPass();
        GEDCOM_VERSION = "5.5.1";
    }

    /**
     * *** 2 ***
     * - Run generic code
     * - <run specific code>
     * - Quit at each fix if true
     */
    @Override
    protected boolean process() throws IOException {
        if (super.process()) {
            return true;
        }
        
        String tag = input.getTag();
        TagPath path = input.getPath();
        String pathBefore = path.getShortName();
        String valueBefore = input.getValue();
        
        if (path.toString().endsWith("_META") || path.toString().endsWith("_DSCR")) {
            output.writeLine(input.getLevel(), "NOTE", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidTagLocation.2", pathBefore, path.getParent().getShortName()+":NOTE", valueBefore, valueBefore));
            return true;
        }
        return false;
    }
    
    /**
     * *** 3 ***
     * - Run generic code
     * - <run specific code>
     * 
     */
    @Override
    protected void finalise() throws IOException {
        super.finalise();
    }
    
    
    /**
     * *** 4 ***
     * - Run generic code
     * - <run specific code>
     * - Quit *after* all have been run
     */
    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        boolean ret = super.fixGedcom(gedcom);
        ret |= processEntities(gedcom);
        incrementProgress();
        return ret;
        
        
    }

    
    /**
     * *** 5 *** 
     * - Run generic code
     * - <run specific code>
     */
    @Override
    public void complete() {
        super.complete();
    }

    ////////////////////////////  END OF LOGIC /////////////////////////////////


    public boolean processEntities(Gedcom gedcom) {

        boolean hasErrors = false;

        // Remove duplicate SOUR underneath SEX
        // Move MARR and DIV from INDI to unique FAMS
        for (Indi indi : gedcom.getIndis()) {
            
            Property sex = indi.getProperty("SEX", false);
            Property[] sources = (sex != null ? sex.getProperties("SOUR", false) : null);
            if (sex != null && sources != null && sources.length != 0) {
                Set<String> existingSources = getSources(indi.getProperty("NAME"));
                existingSources.addAll(getSources(indi));
                List<Property> deleteSources = new ArrayList<>();
                List<Property> moveupSources = new ArrayList<>();
                for (Property source : sources) {
                    String value = source.getValue();
                    if (value.isEmpty() || !value.startsWith("@") || !value.endsWith("@")) {
                        continue;
                    }
                    value = value.substring(1, value.length()-1);
                    // delete if existing, move one level up otherwise
                    if (existingSources.contains(value)) {
                        deleteSources.add(source);
                        String valueBefore = source.getValue();
                        String pathBefore = source.getPath(true).getShortName();
                        String pathAfter = "";
                        fixes.add(new ImportFix(indi.getId(), "invalidSourceLocation.1", pathBefore, pathAfter, valueBefore, valueBefore));
                        hasErrors = true;
                    } else {
                        moveupSources.add(source);
                        String valueBefore = source.getValue();
                        String pathBefore = source.getPath(true).getShortName();
                        String pathAfter = indi.getPath(true).getShortName() + ":SOUR";
                        fixes.add(new ImportFix(indi.getId(), "invalidSourceLocation.2", pathBefore, pathAfter, valueBefore, valueBefore));
                        hasErrors = true;
                    }
                }
                // Process deletion and move
                for (Property source : deleteSources) {
                    sex.delProperty(source);
                }
                for (Property source : moveupSources) {
                    movePropertiesRecursively(source, indi);
                }
            }
            
            // Move MARR and DIV
            Property[] marrs = indi.getProperties("MARR", false);
            Property[] divs  = indi.getProperties("DIV", false);
            Property[] famss = indi.getProperties("FAMS", false);
            if ((marrs.length>0 || divs.length>0) && famss.length == 1) {
                Property fam = ((PropertyXRef) famss[0]).getTargetEntity();
                for (Property marr : marrs) {
                    String valueBefore = marr.getValue();
                    String pathBefore = marr.getPath(true).getShortName();
                    String pathAfter = fam.getPath(true).getShortName() + ":MARR";
                    fixes.add(new ImportFix(indi.getId(), "invalidTagLocation.3", pathBefore, pathAfter, valueBefore, valueBefore));
                    hasErrors = true;
                    movePropertiesRecursively(marr, fam);
                }
                for (Property div : divs) {
                    String valueBefore = div.getValue();
                    String pathBefore = div.getPath(true).getShortName();
                    String pathAfter = fam.getPath(true).getShortName() + ":DIV";
                    fixes.add(new ImportFix(indi.getId(), "invalidTagLocation.3", pathBefore, pathAfter, valueBefore, valueBefore));
                    hasErrors = true;
                    movePropertiesRecursively(div, fam);
                }
            }
            
        }
        
        // Rename SOUR:DATA:NOTE
        for (Entity ent : gedcom.getEntities()) {
            
            List<Property> notes = ent.getAllProperties("NOTE");
            for (Property note : notes) {
                if (note.getPath().toString().contains("SOUR:DATA:NOTE")) {
                    String value = note.getValue();
                    Property parent = note.getParent();
                    Property newNote = parent.addProperty("_NOTE", value);
                    String valueBefore = value;
                    String pathBefore = note.getPath(true).getShortName();
                    String pathAfter = newNote.getPath(true).getShortName();
                    parent.delProperty(note);
                    fixes.add(new ImportFix(ent.getId(), "invalidTagLocation.1", pathBefore, pathAfter, valueBefore, valueBefore));
                    hasErrors = true;
                }
            }
        }
        
        
        return hasErrors;
    }

    private Set<String> getSources(Property host) {
        Set<String> ret = new HashSet<>();
        Property[] sources = host.getProperties("SOUR");
        for (Property source : sources) {
            if (source instanceof PropertySource) {
                PropertySource pSource = (PropertySource) source;
                ret.add(pSource.getTargetEntity().getId());
            }
        }
        return ret;
    }


    
}
