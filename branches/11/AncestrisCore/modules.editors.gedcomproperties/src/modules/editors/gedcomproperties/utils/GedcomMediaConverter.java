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

package modules.editors.gedcomproperties.utils;

import genj.gedcom.Gedcom;
import genj.gedcom.PropertyFile;
import java.io.File;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class GedcomMediaConverter {

    private Gedcom gedcom = null;
    private Map<PropertyFile, String> property2PathMap = null;
    private int nbOfMediaChanges = 0;
    private Exception error = null;
    
    public GedcomMediaConverter(Gedcom gedcom, Map<PropertyFile, String> property2PathMap) {
        this.gedcom = gedcom;
        this.property2PathMap = property2PathMap;
    }

    public boolean convert() {
        // Init variables
        nbOfMediaChanges = 0;
        Exception error = null;
        
        for (PropertyFile pFile : property2PathMap.keySet()) {
            String oldValue = pFile.getValue();
            File f = new File(oldValue);
            String name = f.getName();
            String newValue = property2PathMap.get(pFile) + name;
            if (!newValue.equals(oldValue)) {
                pFile.setValueAsIs(newValue);
                nbOfMediaChanges++;
            }
        }
        
        return true;
    }

    public int getNbOfChangedMedia() {
        return nbOfMediaChanges;
    }

    public Exception getError() {
        if (error == null) {
            error = new Exception(NbBundle.getMessage(PlaceFormatConverterPanel.class, "ERR_Unknown"));
        }
        return error;
    }

}
