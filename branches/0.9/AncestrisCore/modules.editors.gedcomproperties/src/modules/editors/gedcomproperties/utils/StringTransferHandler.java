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

package modules.editors.gedcomproperties.utils;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;

/**
 *
 * @author frederic
 */
public class StringTransferHandler extends TransferHandler {

    PlaceFormatConverterPanel pfc;
    
    StringTransferHandler(PlaceFormatConverterPanel aThis) {
        this.pfc = aThis;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    // Text dragged
    public Transferable createTransferable(JComponent c) {
        return new StringSelection(((JTextComponent) c).getText());
    }


    @Override
    public boolean canImport(TransferSupport ts) {
        return ts.getComponent() instanceof JTextComponent;
    }

    @Override
    // Text dropped
    public boolean importData(TransferSupport ts) {
        try {
            ((JTextComponent) ts.getComponent()).setText((String) ts.getTransferable().getTransferData(DataFlavor.stringFlavor));
            return true;
        } catch(UnsupportedFlavorException e) {
            return false;
        } catch(IOException e) {
            return false;
        }
    }
    
    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        if (action == MOVE) {
            ((JTextComponent) c).setEnabled(false);
        }
        pfc.updateDisplay();
    }
    
    
}
