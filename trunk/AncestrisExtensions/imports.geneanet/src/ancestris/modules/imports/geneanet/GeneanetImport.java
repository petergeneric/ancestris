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
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAssociation;
import java.util.ArrayList;
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
        return convertAssociations(gedcom);
    }

    @Override
    public String toString() {
        return "Geneanet";
    }

    @Override
    protected String getImportComment() {
        return NbBundle.getMessage(GeneanetImport.class, "note1", GeneanetImport.class);
    }

    private boolean convertAssociations(Gedcom gedcom) {

        String id = "";
        Indi indiRela = null;
        PropertyAssociation propAsso = null;
        String type = null;
        Property relaProp = null;
        String rela = null;

        console.println(NbBundle.getMessage(GeneanetImport.class, "GeneanetImport.ConvertingAssos"));

        List<Property> list = new ArrayList<Property>();
        for (Entity entity : gedcom.getEntities()) {
            getPropertiesRecursively(list, "ASSO", entity);
        }
        for (Property prop : list) {
            // Get info
            console.println(prop.getEntity().toString());
            id = prop.getValue().replace("@", "");
            indiRela = (Indi) gedcom.getEntity(id);
            if (indiRela == null) {
                console.println(NbBundle.getMessage(GeneanetImport.class, "GeneanetImport.IndiNotFound", id));
                continue;
            }
            type = prop.getEntity().getTag();
            relaProp = prop.getProperty("RELA");
            if (relaProp != null) {
                rela = relaProp.getDisplayValue();
            }

            // Add to second asso entity
            id = prop.getEntity().getId();
            propAsso = (PropertyAssociation) indiRela.addProperty("ASSO", "@" + id + "@");
            propAsso.addProperty("TYPE", type);
            propAsso.addProperty("RELA", rela);
            try {
                propAsso.link();
            } catch (GedcomException ex) {
                return false;
            }

            // Delete from first asso entity
            prop.getParent().delProperty(prop);
        }
        
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
