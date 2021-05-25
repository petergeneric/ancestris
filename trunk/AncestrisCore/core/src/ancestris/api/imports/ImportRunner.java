/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2018 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.api.imports;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Trackable;
import java.io.File;

/**
 *
 * @author frederic
 */
public interface ImportRunner extends Trackable {
    
    public boolean run(File in, File out);
    
    public boolean fixGedcom(Gedcom gedcom);
    
    public void complete();
    
    public void showDetails(Context context, boolean extract);

}
