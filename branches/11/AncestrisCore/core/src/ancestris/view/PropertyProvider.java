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

import genj.gedcom.Property;
import java.awt.Component;
import java.awt.Point;

/**
 *
 * @author frederic
 *
 * Interface used to get the visible entity inside a component In particular in the case of DND where this entity could be different from the one in the Context (ex: sticked editor, treeview, extended navigator, etc.)
 *
 */
public interface PropertyProvider {

    /**
     * Provide visible entity
     *
     * @param point : coordinates in component (optional)
     * @return
     */
    public Property provideVisibleProperty(Point point);
    
    
    /**
     * Getter of first available property in the ancestors of component
     * @param component
     * @param point
     * @return 
     */

    public static Property getPropertyFromComponent(Component component, Point point) {

        Property property = null;
        Component parentComponent = null;

        if (component instanceof PropertyProvider) {
            property = ((PropertyProvider) component).provideVisibleProperty(point);
        }
        parentComponent = component.getParent();
        while (property == null && parentComponent != null) {
            if (parentComponent instanceof PropertyProvider) {
                property = ((PropertyProvider) parentComponent).provideVisibleProperty(point);
            }
            parentComponent = parentComponent.getParent();
        }

        return property;

    }

}
