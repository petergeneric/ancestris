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
import static ancestris.modules.imports.gedcom.Bundle.importelie_name;
import static ancestris.modules.imports.gedcom.Bundle.importelie_note;
import ancestris.util.TimingUtility;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyNote;
import genj.gedcom.TagPath;
import java.io.IOException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author frederic
 */
@NbBundle.Messages({
    "importelie.name=Elie", 
    "importelie.note=This file has been modified by the Ancestris Elie Import module."
})
@ServiceProvider(service = Import.class)
public class ImportElie extends Import {

    /**
     * Constructor
     */
    public ImportElie() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importelie_name();
    }

    @Override
    protected String getImportComment() {
        return importelie_note();
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
        
        invalidPaths.add("INDI:_CRE:DATE");
        invalidPaths.add("INDI:_GED:DATE");
        
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
        
        TagPath path = input.getPath();
        String pathBefore = path.getShortName();
        String valueBefore = input.getValue();
        
        TimingUtility.getInstance().reset();

        if ("HEAD:GEDC:VERS".equalsIgnoreCase(pathBefore)) {  
            String valueAfter = GEDCOM_VERSION;
            if (!valueBefore.equals(valueAfter)) {
                output.writeLine(2, "VERS", GEDCOM_VERSION);
                fixes.add(new ImportFix(currentXref, "header.Version", pathBefore, pathBefore, valueBefore, valueAfter));
                return true;
            }
            return false;
        }

        // invalid tag here, replace with ..:_TIME
        if (path.toString().contains("DATE:TIME") && !path.toString().contains("CHAN") && !currentXref.equals("HEAD")) {  
            output.writeLine(input.getLevel()-1, "_TIME", input.getValue());
            fixes.add(new ImportFix(currentXref, "invalidTagLocation.1", pathBefore, path.getParent().getParent().getShortName()+":_TIME", valueBefore, valueBefore));
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
        Property prop = null;
        Property file = null;
        Property titl = null;

        
        for (Entity entity : gedcom.getEntities(Gedcom.OBJE)) {

            // Move NOTE to TITL
            prop = entity.getProperty("NOTE", false);
            if (prop == null || !(prop instanceof PropertyNote)) {
                continue;
            }
            Entity target = ((PropertyNote)prop).getTargetEntity();
            if (target == null) {
                continue;
            }
            Note note = (Note) target;
            String valueBefore = note.getValue();
            if (valueBefore.isEmpty()) {
                continue;
            }
            file = entity.getProperty("FILE", false);
            if (file == null) {
                continue;
            }
            titl = file.getProperty("TITL", false);
            if (titl != null) {
                continue;
            }
            String pathBefore = prop.getPath(true).getShortName();
            titl = file.addProperty("TITL", valueBefore);
            String pathAfter = titl.getPath(true).getShortName();
            entity.delProperty(prop);
            fixes.add(new ImportFix(entity.getId(), "invalidFileStructure.1", pathBefore, pathAfter, valueBefore, valueBefore));
            hasErrors = true;
        }
        
        return hasErrors;
    }


    
}
