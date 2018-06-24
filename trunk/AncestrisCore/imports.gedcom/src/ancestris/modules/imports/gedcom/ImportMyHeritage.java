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
import static ancestris.modules.imports.gedcom.Bundle.importmyheritage_name;
import static ancestris.modules.imports.gedcom.Bundle.importmyheritage_note;
import static ancestris.util.swing.FileChooserBuilder.getExtension;
import genj.gedcom.Entity;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.NbBundle;
import genj.gedcom.Gedcom;
import genj.gedcom.Grammar;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertySource;
import genj.gedcom.TagPath;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author frederic
 */
/**
 * The import function for ImportMyHeritage
 */
@NbBundle.Messages({
    "importmyheritage.name=MyHeritage",
    "importmyheritage.note=This file has been modified by the Ancestris MyHeritage Import module."
})
@ServiceProvider(service = Import.class)
public class ImportMyHeritage extends Import {

    /**
     * Constructor
     */
    public ImportMyHeritage() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importmyheritage_name();
    }

    @Override
    protected String getImportComment() {
        return importmyheritage_note();
    }

    ///////////////////////////// START OF LOGIC ///////////////////////////////
    
    
    /**
     * *** 1 ***
     * - Run generic code
     * - <run specific code>
     */
    @Override
    protected void firstPass() {
        super.firstPass();
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
        if (processOther()) {
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
        ret |= fixOther(gedcom);
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


    
    
    
    ////////////////////////////////////////////////////////////////////////////
    //                     SPECIFIC IMPORT FIXES                              //
    ////////////////////////////////////////////////////////////////////////////
    
    
    /**
     * Specific code depending from import type
     * @return 
     */
    private boolean processOther() throws IOException {
        String tag = input.getTag();
        TagPath path = input.getPath();
        if ("RIN".equals(tag) && (path.length() != 2)) {  // invalid tag here
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        if ("FAM:NCHI:NOTE".equalsIgnoreCase(path.toString())) {  // invalid tag here
            String result = output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            console.println(NbBundle.getMessage(ImportHeredis.class, "Import.fixTagNotAllowed", input.getLine() + " ==> " + result));
            return true;
        }
        return false;
    }

    /**
     * Specific code depending from import type after Gedcom is processed
     * @return 
     */
    public boolean fixOther(Gedcom gedcom) {
        boolean hasErrors = false;
    
        // Move OBJE:FORM under OBJE:FILE for grammar 5.5.1
        if (gedcom.getGrammar().equals(Grammar.V551)) {
            List<Property> fileList = (List<Property>) gedcom.getPropertiesByClass(PropertyFile.class);
            Property obje = null;
            Property form = null;
            for (Property file : fileList) {
                obje = file.getParent();
                form = obje.getProperty("FORM");
                if (form != null) {
                    if (file.getProperty("FORM") == null) {
                        file.addProperty("FORM", form.getValue());
                    }
                    obje.delProperty(form);
                    console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixMediaForm", file.toString()));
                    hasErrors = true;
                } else {
                    if (file.getProperty("FORM") == null) {
                        String value = file.getValue();
                        String ext = "";
                        if (value.startsWith("http")) {
                            ext = "web";
                        } else {
                            ext = getExtension(value);
                        }
                        if (ext == null) {
                            ext = "none";
                        }
                        file.addProperty("FORM", ext);
                        console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixMediaForm", file.toString()));
                        hasErrors = true;
                    }
                }
            }
        }
        
        // Only one PAGE tag in a SOUR link
        List<PropertySource> sourceLinks = (List<PropertySource>) gedcom.getPropertiesByClass(PropertySource.class);
        for (PropertySource ps : sourceLinks) {
            Property[] props = ps.getProperties("PAGE");
            if (props.length > 1) {
                int cnt = 0;
                for (Property page : props) {
                    cnt++;
                    if (cnt > 1) {
                        ps.addProperty("_PAGE", page.getValue());
                        ps.delProperty(page);
                    }
                }
                console.println(NbBundle.getMessage(ImportGramps.class, "Import.fixSource") + " : " + ps.toString());
                hasErrors = true;
            }
        }
            
        for (Entity entity : gedcom.getEntities()) {
            
            // For individual with several BIRTs, BAPM, DEAT, etc. change the other ones into EVEN
            if (entity.getTag().equals("INDI")) {
                reduceEvents(entity, "BIRT");
                reduceEvents(entity, "BAPM");
                reduceEvents(entity, "DEAT");
                reduceEvents(entity, "BURI");
                reduceEvents(entity, "CREM");
                reduceEvents(entity, "CONF");
            }

            
        }
        
        
        
        return hasErrors;
    }

    
    
    
}
