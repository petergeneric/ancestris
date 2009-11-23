/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.io;


import genj.gedcom.Property;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;


/**
 * A transferable for property and its sub-properties
 */
public class PropertyTransferable implements Transferable {

  public final static DataFlavor 
    VMLOCAL_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + "; class=java.util.List", "GENJ"),
    STRING_FLAVOR = DataFlavor.stringFlavor,
    TEXT_FLAVOR = DataFlavor.getTextPlainUnicodeFlavor();
  
  private final static DataFlavor[] FLAVORS = { VMLOCAL_FLAVOR, STRING_FLAVOR, TEXT_FLAVOR };
  
  /** properties */
  private List props;
  
  /** string representation */
  private String string;
  
  /**
   * Constructor
   */
  public PropertyTransferable(List properties) {
    
    // remember
    props = properties;
    
    // done
  }
  
  /**
   * supporting property and text
   */
  public DataFlavor[] getTransferDataFlavors() {
    return FLAVORS;
  }

  /**
   * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
   */
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    for (int i = 0; i < FLAVORS.length; i++) {
      if (flavor.equals(FLAVORS[i]))
        return true;
    }
    return false;
  }

  /**
   * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
   */
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    // text flavor?
    if (flavor.equals(TEXT_FLAVOR))
      return new StringReader(getStringData());
    // string flavor?
    if (flavor.equals(STRING_FLAVOR))
      return getStringData();
    // property flavor?
    if (flavor.equals(VMLOCAL_FLAVOR))
      return props;
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
    
    StringWriter out = new StringWriter();
    PropertyWriter writer = new PropertyWriter(out, true);
    for (int i=0;i<props.size();i++)
      writer.write(0, (Property)props.get(i));
    
    return out.toString();
  }
  
} //PropertyTransferable