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
public class PropertyMap extends PropertySimpleReadOnly{

  /**
   * need tag-argument constructor for all properties
   */
    public PropertyMap(String tag) {
        super(tag);
    }
  
  /**
   * Constructor with tag & value
   */
  public PropertyMap(String tag, String value) {
    super(tag, value);
  }

/**
 * Return PropertyLatitude for this Map.
 * Resolve aginst gedcom version
 * @return 
 */
  public PropertyLatitude getLatitude(){
      if (isVersion55()){
          return (PropertyLatitude)getProperty("_LATI");
      } else {
          return (PropertyLatitude)getProperty("LATI");
      }
  }
  
/**
 * Return PropertyLongitude for this Map.
 * Resolve aginst gedcom version
 * @return 
 */
  public PropertyLongitude getLongitude(){
      if (isVersion55()){
          return (PropertyLongitude)getProperty("_LONG");
      } else {
          return (PropertyLongitude)getProperty("LONG");
      }
  }
}
