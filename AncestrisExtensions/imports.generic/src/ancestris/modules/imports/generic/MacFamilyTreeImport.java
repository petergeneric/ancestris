/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.imports.generic;

import ancestris.api.imports.Import;
import org.openide.util.lookup.ServiceProvider;
import static ancestris.modules.imports.generic.Bundle.macfamilytreeimport_note;
import org.openide.util.NbBundle;
import genj.gedcom.Gedcom;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author frederic
 */
/**
 * The import function for MacFamilyTree
 */
@NbBundle.Messages({
    "macfamilytreeimport.note=This file has been modified by the Ancestris MacFamilyTree Import plugin."
})
@ServiceProvider(service = Import.class)
public class MacFamilyTreeImport extends Import {

    private static int counter = 0;
    private static final HashMap<Integer, String> hashFixes = new HashMap<Integer, String>();
    String previousLine = "";
    private boolean header = false;
    
    /**
     * Constructor
     */
    public MacFamilyTreeImport() {
        super();
    }

    @Override
    public String toString() {
        return "MacFamilyTree";
    }

    @Override
    protected String getImportComment() {
        return macfamilytreeimport_note();
    }

    @Override
    protected void firstPass() {
        super.firstPass();
        detectBadLines();
    }

    @Override
    protected boolean process() throws IOException {
        // before super process
        if (beforeProcess()) {
            return true;
        }
        // Super process  (yes tags, invalid tags, etc)
        if (super.process()) {
            return true;
        }
        
        //After super process
        if (afterProcess()) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        return todo(gedcom);
    }

    
    
    
    
    
    
    private void detectBadLines() {
        //console.println(input.getLine());
        if (input.getLine().startsWith("/")) {
            hashFixes.put(counter, previousLine);
            //console.println("Storing into counter = " + counter + " value = " + previousLine);
            counter++;
        }
        previousLine = input.getLine();

    }

    
    
    private boolean beforeProcess() {
        //console.println(input.getLine());
        if (hashFixes.containsValue(input.getLine())) {
            try {
                String str = input.getValue()+"/";
                output.writeLine(input.getLevel(), input.getXref(), input.getTag(), str);
                console.println("Fixed line " + str);
                return true;
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }
        
        if ((input.getLevel() == 0) && (!header)) {
            try {
                header = true;
                output.writeLine(1, null, "DEST", "ANY");
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }
        
        
        return false;
    }

    private boolean afterProcess() {
        return false;
    }

    
    private boolean todo(Gedcom gedcom) {

        console.println(" ");
        
        return true;
    }

}
