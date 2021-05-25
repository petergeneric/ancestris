/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
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
import static ancestris.modules.imports.gedcom.Bundle.importheredis_name;
import static ancestris.modules.imports.gedcom.Bundle.importheredis_note;
import ancestris.util.TimingUtility;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.io.IOException;
import java.util.HashMap;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * The import function for Heredis originated Gedcom files
 */
@NbBundle.Messages({
    "importheredis.name=Heredis",
    "importheredis.note=This file has been modified by Ancestris Heredis Import module."
})
@ServiceProvider(service = Import.class)
public class ImportHeredis extends Import {

    private boolean repoOK = false;
    private static int clerepo = 0;
    private static final HashMap<String, Integer> hashrepo = new HashMap<String, Integer>();
    private static final StringBuilder sb = new StringBuilder();

    /**
     * Constructor
     */
    public ImportHeredis() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importheredis_name();
    }

    @Override
    protected String getImportComment() {
        return importheredis_note();
    }

    ///////////////////////////// START OF LOGIC ///////////////////////////////
    
    /**
     * *** 0 *** Initialisation of variables
     */
    protected void init() {
        super.init();
        
        invalidPaths.add("OBJE:DATE");
        invalidPaths.add("INDI:ASSO:TITL");
        invalidPaths.add("NOTE:RESN");
        invalidPaths.add("SOUR:RESN");
        invalidPaths.add("OBJE:RESN");
        invalidPaths.add("_SUBMAP:LATI"); // is redundant to MAP, so alternative to change SUBMAP to MAP does not work
        invalidPaths.add("_SUBMAP:LONG");
        invalidPaths.add("ASSO:TYPE");  // Heredis assumes 5.5.1 but left TYPE tags under ASSOs
    }

    
    
    /**
     * **** 1 ***
     * - Run generic code
     * - Prepare REPO
     */
    @Override
    protected void firstPass() {
        super.firstPass();
        GEDCOM_VERSION = "5.5.1"; // Heredis (from 2017) seems to consider 5.5.1 tags as being within 5.5 (WWW, OBJE:FILE, etc.), so force gedcom version to 5.5.1
        firstPassRepo();
    }

    /**
     * *** 2 ***
     * - Run generic code
     * - Etc
     * 
     */
    @Override
    protected boolean process() throws IOException {

        TagPath path = input.getPath();
        String pathBefore = path.getShortName();
        String valueBefore = input.getValue();
        
        TimingUtility.getInstance().reset();

        // Overwrite version
        if ("HEAD:GEDC:VERS".equalsIgnoreCase(pathBefore)) {  
            String valueAfter = GEDCOM_VERSION;
            if (!valueBefore.equals(valueAfter)) {
                output.writeLine(2, "VERS", GEDCOM_VERSION);
                fixes.add(new ImportFix(currentXref, "header.Version", pathBefore, pathBefore, valueBefore, valueAfter));
                return true;
            }
            return false;
        }
        
        // Web address
        if ((input.getLevel() == 3) && (input.getTag().equals("WEB"))) {
            String valueAfter = "http://" + valueBefore;
            output.writeLine(3, "WWW", valueAfter);
            fixes.add(new ImportFix(currentXref, "header.Web", pathBefore, pathBefore, valueBefore, valueAfter));
            return true;
        }
        
        if (processRepo()) {
            return true;
        }
        if (processTagNotAllowed()) {
            return true;
        }

        if (super.process()) {
            return true;
        }

        return false;
    }

    
    /**
     * *** 3 ***
     * - Run generic code
     * - Write unused places to NOTEs @Pxxx@, using only user-defined tags
     * 
     */
    
    @Override
    protected void finalise() throws IOException {
        super.finalise();
        finaliseRepo();
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

    @Override
    public void showDetails(Context context, boolean extract) {
        new FixesWindow(summary, context, fixes).displayFixes(extract);
    }
    
    
    ////////////////////////////  END OF LOGIC /////////////////////////////////


    
    
    
    ////////////////////////////////////////////////////////////////////////////
    //                     SPECIFIC IMPORT FIXES                              //
    ////////////////////////////////////////////////////////////////////////////
    
    
    
    /**
     * REPO in Heredis 2017 are not stored as separated RECORDs so first, store them in memory
     * In Heredis 2018, it is ok
     */
    private void firstPassRepo() {
        if (!repoOK && (input.getLevel() == 1) && input.getTag().equals("REPO")) {
            String value = input.getValue();
            if (!value.matches("\\@[A-Z0-9]+\\@")) {
                if (!hashrepo.containsKey(value)) {
                    clerepo++;
                    hashrepo.put(input.getValue(), clerepo);
                    sb.append("0 @" + typerepo).append(clerepo).append("@ REPO").append(EOL);
                    sb.append("1 NAME ").append(input.getValue()).append(EOL);
                }
            } else {
                repoOK = true;
            }
        }

    }

    /**
     * Replace all pointers to REPO with their corresponding record
     * @return
     * @throws IOException 
     */
    private boolean processRepo() throws IOException {
        if (!repoOK && (input.getLevel() == 1) && input.getTag().equals("REPO")) {
            String pathBefore = input.getPath().getShortName();
            String valueBefore = input.getValue();
            if (hashrepo.containsKey(valueBefore)) {
                String valueAfter = "@" + typerepo + hashrepo.get(input.getValue()) + "@";
                output.writeLine(1, "REPO", valueAfter);
                fixes.add(new ImportFix(currentXref, "invalidEntity.20", pathBefore, pathBefore, valueBefore, valueAfter));
            }
            return true;
        }
        return false;

    }

    /**
     * Write REPO records at the end
     * @throws IOException 
     */
    private void finaliseRepo() throws IOException {
        if (!repoOK) {
            output.write(sb.toString());
            for (String repoName : hashrepo.keySet()) {
                int index = hashrepo.get(repoName);
                fixes.add(new ImportFix("REPO"+index, "invalidEntity.21", "", "REPO", "", repoName));
            }
        }
    }

    
    /**
     * fix *:OBJE:DATE errors.
     * Remove DATE tag if no value, rename tag to _DATE otherwise
     * @return 
     */
    private boolean processTagNotAllowed() throws IOException {
        String tag = input.getTag();
        TagPath path = input.getPath();
        String pathBefore = path.getShortName();
        String valueBefore = input.getValue();
        
        if ("SOUR:TYPE".equalsIgnoreCase(path.toString())) {  // invalid tag here but useful information, replace with NOTE
            output.writeLine(input.getLevel(), "NOTE", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidTagLocation.2", pathBefore, path.getParent().getShortName()+":NOTE", valueBefore, valueBefore));
            return true;
        }
        if ("ACQU".equalsIgnoreCase(tag)) {  // invalid tag here but useful information, replace with EVEN
            output.writeLine(input.getLevel(), "EVEN", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidTag.2", pathBefore, path.getParent().getShortName()+":EVEN", valueBefore, valueBefore));
            return true;
        }
        if ((path.toString().startsWith("INDI") && "HUSB".equalsIgnoreCase(tag))
         || (path.toString().startsWith("INDI") && "AGE".equalsIgnoreCase(tag) && path.toString().contains("HUSB"))
         || (path.toString().startsWith("INDI") && "WIFE".equalsIgnoreCase(tag))
         || (path.toString().startsWith("INDI") && "AGE".equalsIgnoreCase(tag) && path.toString().contains("WIFE"))       
         || ("TIME".equals(tag) && (!path.toString().startsWith("HEAD") && !path.toString().contains("CHAN")))       ) {
            
            output.writeLine(input.getLevel(), "_" + tag, valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidTagLocation.1", pathBefore, path.getParent().getShortName()+":_"+tag, valueBefore, valueBefore));
            return true;
            
        }

        if ("SEX".equals(tag) && ("?".equals(input.getValue()))) {
            output.writeLine(input.getLevel(), tag, "U");
            fixes.add(new ImportFix(currentXref, "invalidSex.1", pathBefore, pathBefore, valueBefore, "U"));
            return true;
        }
        return false;
    }

    public boolean processEntities(Gedcom gedcom) {

        boolean hasErrors = false;
        Property[] props = null;
        
        // Clean multiple RESN and multiple EVEN:TYPE
        
        for (Entity entity : gedcom.getEntities()) {
            

            // Move second EVEN:TYPE to EVEN value if empty
            props = entity.getProperties("EVEN", false);
            for (Property prop : props) {
                if (!prop.getValue().isEmpty()) {
                    continue;
                }
                Property[] types = prop.getProperties("TYPE", false);
                if (types != null && types.length > 1) {
                    Property type2 = types[1];
                    String valueBefore = type2.getValue();
                    String pathBefore = type2.getPath(true).getShortName();
                    String pathAfter = prop.getPath(true).getShortName();
                    prop.setValue(valueBefore);
                    prop.delProperty(type2);
                    fixes.add(new ImportFix(entity.getId(), "invalidCardinality.1", pathBefore, pathAfter, valueBefore, valueBefore));
                    hasErrors = true;
                }
            }
            
        }
        
        return hasErrors;
    }

    
}
