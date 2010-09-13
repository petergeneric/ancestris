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
package genj.view;

import java.awt.Component;


/**
 * Support for providing context if asked
 */
public interface ContextProvider {

  /**
   * Get context
   */
  public ViewContext getContext();
  
  /**
   * Resolver for context by component 
   */
  public class Lookup {
    
    private ViewContext context;
    private ContextProvider provider;
    
    public Lookup(Component component) {
      // find context provider in component hierarchy
      while (component != null) {
        // component can provide context?
        if (component instanceof ContextProvider) {
          context = ((ContextProvider) component).getContext();
          if (context != null) {
            provider = (ContextProvider)component;
            break;
          }
        }
        // try parent
        component = component.getParent();
      }
    }
    
    public ViewContext getContext() {
      return context;
    }
    
    public ContextProvider getProvider() {
      return provider;
    }
  }
  

} //ContextProvider
