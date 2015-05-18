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
package genj.option;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.spi.ServiceRegistry;

/**
 * A service that can provide options
 */
public abstract class OptionProvider {

  /** all known options */
  private static List<Option> options;
  
  /**
   * Accessor - options
   */
  public abstract List<? extends Option> getOptions();

  /**
   * Persist all options from all OptionProviders to registry
   */
  public static void persistAll() {
    
    // loop over all options
    for (Option option : getAllOptions()) try {
      option.persist();
    } catch (Throwable t) {
    }
    
    // done
    
  }
  
  /**
   * Static Accessor - all options available from OptionProviders
   */
  public synchronized static List<Option> getAllOptions() {  

    // known?
    if (options!=null)
      return options;    
  
    List<Option> ops = new ArrayList<Option>(32);
  
    // prepare options
    Iterator<OptionProvider> providers = lookupProviders();
    while (providers.hasNext()) {
      
      // one provider at a time
      OptionProvider provider = providers.next();
      
      // grab optirestore their value
      for (Option option : provider.getOptions()) {
        try {
          option.restore();
          ops.add(option);
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
      // next provider
    }
    
    options = ops;
    
    // done
    return options;
  }
  
  /**
   * Lookup providers where considering
   */
  private static Iterator<OptionProvider> lookupProviders() {
    return ServiceRegistry.lookupProviders(OptionProvider.class);
  }

} //OptionProvider
