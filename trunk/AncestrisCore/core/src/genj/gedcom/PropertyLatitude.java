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
/**
 * Gedcom Property for Latitude representation
 */
public class PropertyLatitude extends PropertyCoordinate{

    public PropertyLatitude(String tag) {
        super(tag);
    }

    @Override
    char getDirection(double coordinate) {
        return coordinate<0?'W':'E';
    }
    
}
