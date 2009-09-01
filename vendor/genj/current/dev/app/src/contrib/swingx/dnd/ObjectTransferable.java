/*
 * swingx - Swing eXtensions
 * Copyright (C) 2004 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package swingx.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The transferable used to transfer objects.
 */
public class ObjectTransferable implements Transferable {

    /**
     * The dataFlavor used for transfers between different JVMs.
     */
    public static final DataFlavor serializedFlavor = new DataFlavor(
            java.io.Serializable.class, "Object");

    /**
     * The dataFlavor used for transfers in one JVM.
     */
    public static final DataFlavor localFlavor;
    
    static {
      try {
          localFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
                  + ";class=java.lang.Object");
      } catch (ClassNotFoundException e) {
          throw new Error(e);
      }
    }

    private List flavors;

    private Object object;

    public ObjectTransferable(Object object) {
        this.object = object;

        flavors = createFlavors();
    }

    protected List createFlavors() {
        List flavors = new ArrayList();

        flavors.add(localFlavor);

        boolean serializable = true;
        if (object.getClass().isArray()) {
            Object[] array = (Object[])object;
            for (int n = 0; n < array.length; n++) {
                serializable = serializable && (array[n] instanceof Serializable);
            }
        } else {
            serializable = object instanceof Serializable;
        }
        if (serializable) {
            flavors.add(serializedFlavor);
        }

        return flavors;
    }

    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {
        if (localFlavor.equals(flavor)) {
            return object;
        }
        if (serializedFlavor.equals(flavor)) {
            return object;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    public DataFlavor[] getTransferDataFlavors() {
        return (DataFlavor[]) flavors.toArray(new DataFlavor[flavors.size()]);
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavors.contains(flavor);
    }

    public static Object getObject(Transferable transferable) throws UnsupportedFlavorException, IOException {
        if (transferable.isDataFlavorSupported(localFlavor)) {
          return transferable.getTransferData(localFlavor);
        } else if (transferable.isDataFlavorSupported(serializedFlavor)) {
          return transferable.getTransferData(serializedFlavor);
        }
        throw new IOException();
    }
    
    /**
     * Get the transferable of a <code>DropTargetDragEvent</code> - only
     * supported since Java 1.5 Tiger.
     * 
     * @param dtde  event to get transferable from
     * @return      transferable or <code>null</code> if running under pre
     *              1.5 version of Java
     */
    public static Transferable getTigerTransferable(DropTargetDragEvent dtde) {
        try {
            return (Transferable)DropTargetDragEvent.class.getMethod("getTransferable", new Class[0]).invoke(dtde, new Object[0]);
        } catch (Throwable t) {
            return null;
        }
    }
    
}