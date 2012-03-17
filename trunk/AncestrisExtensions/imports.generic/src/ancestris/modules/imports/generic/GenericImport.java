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
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author dominique
 */
@ServiceProvider(service = Import.class)
public class GenericImport extends Import {

    /**
     * Constructor
     */
    public void GenericImport() {
    }

    @Override
    public String toString () {
        return "Generic";
    }

    @Override
    protected String getImportComment() {
        return NbBundle.getMessage(GenericImport.class, "note1", GenericImport.class);
    }
}
