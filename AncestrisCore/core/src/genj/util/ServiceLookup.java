/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2009 Nils Meier <nils@meiers.net>
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
package genj.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.spi.ServiceRegistry;

public class ServiceLookup {
  
  private static Logger LOG = Logger.getLogger("genj.util");

  public static <X> List<X> lookup(Class<X> service) {
    List<X> result = new ArrayList<X>();
    Iterator<X> it = ServiceRegistry.lookupProviders(service);
    while (it.hasNext()) try {
      result.add(it.next());
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "Error retrieving service for "+service, t);
    }
    return result;
  }
  
}
