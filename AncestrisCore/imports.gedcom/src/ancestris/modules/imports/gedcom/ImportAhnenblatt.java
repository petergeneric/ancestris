/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2020 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.imports.gedcom;

import ancestris.api.imports.Import;
import java.io.IOException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Specific import for Ahnenblatt.
 *
 * @author Zurga
 */
@NbBundle.Messages({
    "importahnenblatt.name=Ahnenblatt",
    "importahnenblatt.note=This file has been modified by Ancestris Ahnenblatt Import module."
})
@ServiceProvider(service = Import.class)
public class ImportAhnenblatt extends Import {

    /**
     * Constructor
     */
    public ImportAhnenblatt() {
        super();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(ImportAhnenblatt.class, "importahnenblatt.name");
    }

    @Override
    protected String getImportComment() {
        return NbBundle.getMessage(ImportAhnenblatt.class, "importahnenblatt.note");
    }

    ///////////////////////////// START OF LOGIC ///////////////////////////////
    /**
     * *** 0 *** Initialisation of variables
     */
    protected void init() {
        super.init();
    }

    /**
     * *** 2 ***
     * - Run generic code - Etc
     *
     */
    @Override
    protected boolean process() throws IOException {
        if (processEventDefn()) {
            return true;
        }
        return super.process();
    }

    ////////////////////////////  END OF LOGIC /////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    //                     SPECIFIC IMPORT FIXES                              //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Ahnenblatt Add _EVEN_DEFN to define specific event. All structure is
     * contratory to GEDCOM entities definition.
     */
    private boolean processEventDefn() throws IOException {
        if (input.getValue().contains("_EVENT_DEFN")) {
            String[] value = input.getLine().split(" ");
            output.writeLine(0, value[1], "_EVENT_DEFN", null);
            console.println(NbBundle.getMessage(ImportAhnenblatt.class, "Import.invalidRecord", "_EVENT_DEFN", input.getLine()));
            return true;
        }

        return false;

    }

}
