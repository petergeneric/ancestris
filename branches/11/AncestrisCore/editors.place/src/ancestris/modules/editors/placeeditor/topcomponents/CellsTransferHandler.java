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

package ancestris.modules.editors.placeeditor.topcomponents;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 *
 * @author frederic
 */
public class CellsTransferHandler extends TransferHandler {

    private EditorTable editor;
    
    public CellsTransferHandler() {
    }

    public CellsTransferHandler(EditorTable editor) {
        this.editor = editor;
    }

    /**
     * Bundle up the data for export.
     */
    @Override
    protected Transferable createTransferable(JComponent c) {
        editor.setpendingPaste(false);
        return new CellsData(editor.exportData());
    }

    /**
     * The list handles both copy and move actions.
     */
    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    /**
     * Data flavors suported
     */
    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        return support.isDataFlavorSupported(CellsData.CELL_DATA_FLAVOR);
    }    
    
    /**
     * Perform the actual data import.
     */
    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        
        // If we can't handle the import, bail now.
        if (!canImport(support) || !support.isDrop()) {
            return false;
        }

        // Fetch the data (we know it is of Object[][] format because we only accept the call flavor
        Object[][] data = null;
        try {
            data = (Object[][]) support.getTransferable().getTransferData(CellsData.CELL_DATA_FLAVOR);
        } catch (UnsupportedFlavorException ufe) {
            System.out.println("importData: unsupported data flavor");
            return false;
        } catch (IOException ioe) {
            System.out.println("importData: I/O exception");
            return false;
        }

        // In case of MOVE, erase data before importing
        if ((MOVE & support.getDropAction()) == MOVE) {
            editor.eraseSelection();
        }

        EditorTable.DropLocation dl = (EditorTable.DropLocation) support.getDropLocation();
        int rows[] = new int[] { dl.getRow() };
        int cols[] = new int[] { dl.getColumn() };
        editor.importData(data, rows, cols);
        
        return true;
    }

}
