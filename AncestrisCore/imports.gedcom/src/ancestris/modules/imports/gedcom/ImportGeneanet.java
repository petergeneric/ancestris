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
import static ancestris.api.imports.Import.LOG;
import ancestris.api.imports.ImportFix;
import static ancestris.modules.imports.gedcom.Bundle.importgeneanet_name;
import static ancestris.modules.imports.gedcom.Bundle.importgeneanet_note;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertyFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
    public void showDetails(Context context, boolean extract) {
        new FixesWindow(summary, context, fixes).displayFixes(extract);
    }
    
    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        boolean ret = super.fixGedcom(gedcom);
        ret |= fixMediaName(gedcom);
        incrementProgress();
        ret |= removeDoubleAssociations(gedcom);
        incrementProgress();
        return ret;
    }
    
    /**
     * Geneanet create association from indi to fam and fam to indi.
     * This leads to duplicate association.
     * @param gedcom The file to import.
     * @return  true if errors occurs.
     */
    private boolean removeDoubleAssociations(Gedcom gedcom) {
        
        boolean fixed = false;
        
        List<Property> list = new ArrayList<>();
        gedcom.getIndis().forEach((entity) -> {
            getPropertiesRecursively(list, "ASSO", entity);
        });
        
        Map<String,PropertyAssociation> mapL = new HashMap<>();
        
        for (Property prop : list) {
            if (prop instanceof PropertyAssociation) {
                PropertyAssociation pa = (PropertyAssociation) prop;
                String origine = pa.getEntity().getId();
                String destination = pa.getTargetEntity().getId();
                String key = origine + " -;- " + destination;
                // Same association already there.
                // Remove asso not Ancestris style.
                //
                if (mapL.containsKey(key)) {
                    PropertyAssociation po = mapL.get(key);
                    Property relaO = po.getProperty("RELA", false);
                    if (!relaO.getValue().contains("@")) {
                        Entity ent = po.getEntity();
                        fixes.add(new ImportFix(ent.getId(), "duplicateAssociations.1", po.getPath(true).getShortName(), "", po.getValue(), ""));
                        fixed = true;
                        ent.delProperty(po);
                        mapL.remove(key);
                        mapL.put(key, pa);
                    } else {
                        // New value not ancestris style.
                        // Remove.
                        Property relaA = pa.getProperty("RELA", false);
                        if (!relaA.getValue().contains("@")) {
                            Entity ent = pa.getEntity();
                            fixes.add(new ImportFix(ent.getId(), "duplicateAssociations.1", pa.getPath(true).getShortName(), "", pa.getValue(), ""));
                            fixed = true;
                            ent.delProperty(pa);
                        }
                    }
                } else {
                    mapL.put(key, pa);
                }
                
            } else {
                LOG.log(Level.WARNING, "The following association property is not of type PropertyAssociation: " + prop);
            }
        }
        
        return fixed;
    }
    
    private boolean fixMediaName(Gedcom gedcom){
        boolean hasErrors = false;
    
        // change medium name for normal to get better quality OBJE:FILE
        List<Property> fileList = (List<Property>) gedcom.getPropertiesByClass(PropertyFile.class);
        for (Property file : fileList) {
            String valeur = file.getValue();
            String oldValue = valeur;
            if (valeur.startsWith("http://gw.geneanet.org")){
                // Geneanet export file with HTTP instead of HTTPS
                valeur = valeur.replace("http://", "https://");
                // Replace medium value by normal value
                if (valeur.indexOf("medium") > 0) {
                    valeur = valeur.replace("medium", "normal");
                }
                file.setValue(valeur);
                fixes.add(new ImportFix(file.getEntity().getId(), "textformatting.3", file.getPath().getShortName(), "", oldValue, valeur));
            }
            
        }
        return hasErrors;
    }
    
}
