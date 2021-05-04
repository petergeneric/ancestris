/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package modules.editors.gedcomproperties.utils;

import genj.gedcom.Gedcom;

/**
 *
 * @author frederic
 */
public interface PlaceFormatterInterface {

    public Gedcom getGedcom();
    
    public void warnVersionChange(boolean canBeConverted);

    public String getOriginalPlaceFormat();

    public void setPlaceFormatConverter(PlaceFormatConverterPanel pfc);

    public PlaceFormatConverterPanel getPlaceFormatConverter();
    
    public int getMode();

    public void setConfirmEnabled(boolean set);

}
