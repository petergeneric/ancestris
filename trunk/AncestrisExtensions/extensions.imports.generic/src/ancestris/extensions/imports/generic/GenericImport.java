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
package ancestris.extensions.imports.generic;

import ancestris.extensions.imports.api.Import;
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
    public void HeredisImport() {
    }

    @Override
    public String toString () {
        return "Generic";
    }
}
