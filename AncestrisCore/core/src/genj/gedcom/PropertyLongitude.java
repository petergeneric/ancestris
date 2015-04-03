/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package genj.gedcom;

/**
 *
 * @author daniel
 */
public class PropertyLongitude  extends PropertyCoordinate{

    public PropertyLongitude(String tag) {
        super(tag);
    }

    @Override
    char getDirection(double coordinate) {
        return coordinate<0?'S':'N';
    }
    
}
