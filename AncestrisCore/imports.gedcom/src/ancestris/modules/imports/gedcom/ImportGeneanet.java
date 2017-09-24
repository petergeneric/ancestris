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
import static ancestris.modules.imports.gedcom.Bundle.importgeneanet_notewarning;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * The import function for Geneanet
 */
@NbBundle.Messages({
    "importgeneanet.name=Geneanet",
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
        super.convertAssociations(gedcom);
        return specialNote(gedcom);
    }

    private boolean specialNote(Gedcom gedcom) {

        Entity entity = gedcom.getFirstEntity("HEAD");
        String note = " - " + importgeneanet_notewarning();
        Property[] noteProps = entity.getProperties("NOTE");
        for (Property noteProp : noteProps) {
            String str = noteProp.getDisplayValue();
            if (str.contains(note)) {
                noteProp.setValue(str.replace(note, ""));
            }
        }
        console.println(note);
        return true;
    }

    
    
}
