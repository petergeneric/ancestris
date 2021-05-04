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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringReader;
import java.util.StringJoiner;

/**
 *
 * @author frederic
 */
public class CellsData implements Transferable {

    private Object data[][];

    public final static DataFlavor CELL_DATA_FLAVOR = new DataFlavor(Object.class, "application/x-cell-values"),
            STRING_FLAVOR = DataFlavor.stringFlavor,
            TEXT_FLAVOR = DataFlavor.getTextPlainUnicodeFlavor();

    private final static DataFlavor[] FLAVORS = {CELL_DATA_FLAVOR, STRING_FLAVOR, TEXT_FLAVOR};

    public CellsData(Object data[][]) {
        this.data = data;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return FLAVORS;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (int i = 0; i < FLAVORS.length; i++) {
            if (flavor.equals(FLAVORS[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        // text flavor?
        if (flavor.equals(TEXT_FLAVOR)) {
            return new StringReader(getStringData());
        }
        // string flavor?
        if (flavor.equals(STRING_FLAVOR)) {
            return getStringData();
        }
        // cells flavor?
        if (flavor.equals(CELL_DATA_FLAVOR)) {
            return data;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    /**
     * Return as string transferrable
     */
    public StringSelection getStringTransferable() throws IOException {
        return new StringSelection(getStringData());
    }

    /**
     * lazy lookup string representation
     */
    private String getStringData() throws IOException {

        if (data == null || data.length == 0 || data[0].length == 0) {
            return "";
        }
        
        int nbRows = data.length;
        int nbCols = data[0].length;
        
        StringJoiner joinerRows = new StringJoiner("\r\n");
        for (int row = 0; row < nbRows; row++) {
            StringJoiner joinerCols = new StringJoiner("\t");
            for (int col = 0; col < nbCols; col++) {
                joinerCols.add(data[row][col] == null ? "" : data[row][col].toString());
            }
            joinerRows.add(joinerCols.toString());
        }

        return joinerRows.toString();
    }

}
