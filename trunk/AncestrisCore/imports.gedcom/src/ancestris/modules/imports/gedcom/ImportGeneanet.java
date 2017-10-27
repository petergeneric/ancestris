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
/**
 *
 * @author frederic
 */
package ancestris.modules.imports.gedcom;

import ancestris.api.imports.Import;
import static ancestris.modules.imports.gedcom.Bundle.importgeneanet_name;
import static ancestris.modules.imports.gedcom.Bundle.importgeneanet_note;
import static ancestris.util.swing.FileChooserBuilder.getExtension;
import genj.gedcom.Gedcom;
import genj.gedcom.Grammar;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * The import function for Geneanet
 */
@NbBundle.Messages({
    "importgeneanet.name=Geneanet / GeneWeb",
    "importgeneanet.note=This file has been modified by the Ancestris Geneanet Import module.",
    "importgeneanet.notewarning=Gedcom file made to be uploaded to Geneanet. File is NOT to be used to modify your genealogy !"
})
@ServiceProvider(service = Import.class)
public class ImportGeneanet extends Import {

    /**
     * Constructor
     */
    public ImportGeneanet() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return importgeneanet_name();
    }

    @Override
    protected String getImportComment() {
        return importgeneanet_note();
    }

    
    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        boolean ret = super.fixGedcom(gedcom);
        ret |= super.convertAssociations(gedcom);
        ret |= fixOther(gedcom);
        return ret;
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
        
        
        return hasErrors;
    }

    
    

    
    
}
