/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2021 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.view;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import javax.swing.TransferHandler;
import org.openide.windows.TopComponent;

/**
 *
 * @author frederic
 */
public interface DelegatedTransferable extends Transferable {
    
    public static final DataFlavor DELEGATED_FLAVOR = new DataFlavor(Object.class, "Ancestris_Delegation");
    
    public boolean runDelegation(TopComponent tc, Gedcom targetGedcom, Entity targetEntiry, TransferHandler.TransferSupport support);
    
}
