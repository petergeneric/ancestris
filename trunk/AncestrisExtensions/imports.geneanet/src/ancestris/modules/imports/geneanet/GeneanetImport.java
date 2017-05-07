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
package ancestris.modules.imports.geneanet;

import ancestris.api.imports.Import;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * The import function for Geneanet
 */
@ServiceProvider(service = Import.class)
public class GeneanetImport extends Import {

    /**
     * Constructor
     */
    public GeneanetImport() {
        super();
    }

    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        super.convertAssociations(gedcom);
        return specialNote(gedcom);
    }

    @Override
    public String toString() {
        return "Geneanet";
    }

    @Override
    protected String getImportComment() {
        return NbBundle.getMessage(GeneanetImport.class, "note1", GeneanetImport.class);
    }

    private boolean specialNote(Gedcom gedcom) {

        Entity entity = gedcom.getFirstEntity("HEAD");
        String note = " - " + NbBundle.getMessage(GeneanetImport.class, "GeneanetImport.NoteWarning");
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

    private <T> void getPropertiesRecursively(List<T> props, String tag, Property parent) {
        for (Property child : parent.getProperties()) {
            if (tag.equals(child.getTag())) {
                props.add((T) child);
            }
            getPropertiesRecursively(props, tag, child);
        }
    }
    
    
}
