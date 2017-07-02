/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Dominique Baron (lemovice-at-ancestris-dot-org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.imports.generic;

import ancestris.api.imports.Import;
import static ancestris.modules.imports.generic.Bundle.genericimport_name;
import static ancestris.modules.imports.generic.Bundle.genericimport_note;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author dominique
 */
@NbBundle.Messages({
    "genericimport.name=Unknown (use generic import)",
    "genericimport.note=This file has been modified by the Ancestris Generic Import plugin."
})
@ServiceProvider(service = Import.class)
public class GenericImport extends Import {

    /**
     * Constructor
     */
    public GenericImport() {
        super();
    }

    @Override
    public String toString() {
        return genericimport_name();
    }

    @Override
    protected String getImportComment() {
        return genericimport_note();
    }

}
