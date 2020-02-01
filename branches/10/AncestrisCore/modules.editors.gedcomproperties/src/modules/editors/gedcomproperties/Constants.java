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
package modules.editors.gedcomproperties;

import genj.gedcom.Gedcom;

/**
 *
 * @author frederic
 */
public interface Constants {

    static final int CREATION_OR_UPDATE = 0;
    static final int CREATION = 1; 
    static final int UPDATE = 2;

    
    static final String HEADER = "HEAD";
    
    static final String COPR = "COPR";
    static final String FILE = "FILE";
    static final String NOTE = "NOTE";
    static final String LANG = "LANG";
    static final String CHAR = "CHAR";
    static final String GEDC = "GEDC";
    static final String VERS = "VERS";
    static final String DEST = "DEST";
    static final String PLAC = "PLAC";
    static final String FORM = "FORM";
    static final String SOUR = "SOUR";
    static final String CORP = "CORP";
    static final String DATE = "DATE";
    static final String TIME = "TIME";


//     Source format
//    
//    +1 SOUR <APPROVED_SYSTEM_ID>  {1:1}
//    +2 VERS <VERSION_NUMBER>  {0:1}
//    +2 NAME <NAME_OF_PRODUCT>  {0:1} (rarely used by others software but used by Ancestris)
//    +2 CORP <NAME_OF_BUSINESS>  {0:1}
//    +3 <<ADDRESS_STRUCTURE>>  {0:1}
//    
//    +1 DATE <TRANSMISSION_DATE>  {0:1}
//    +2 TIME <TIME_VALUE>  {0:1}

// unused:
//        +2 DATA <NAME_OF_SOURCE_DATA>  {0:1}
//        +3 DATE <PUBLICATION_DATE>  {0:1}
//        +3 COPR <COPYRIGHT_SOURCE_DATA>  {0:1}
    
//<<ADDRESS_STRUCTURE>>
//  n  ADDR <ADDRESS_LINE>  {0:1}
//    +1 CONT <ADDRESS_LINE>  {0:M}
//    +1 ADR1 <ADDRESS_LINE1>  {0:1}
//    +1 ADR2 <ADDRESS_LINE2>  {0:1}
//    +1 CITY <ADDRESS_CITY>  {0:1}
//    +1 STAE <ADDRESS_STATE>  {0:1}
//    +1 POST <ADDRESS_POSTAL_CODE>  {0:1}
//    +1 CTRY <ADDRESS_COUNTRY>  {0:1}
//  n  PHON <PHONE_NUMBER>  {0:3}    
//     (WEB, _WEB, ou _ADDR pour le site web)
//     (EMAIL pour email)

    
    
    
    static final String SUBM = "SUBM";
    
    static final String NAME = "NAME";
    static final String ADDR = "ADDR";
    static final String POST = "POST";
    static final String CITY = "CITY";
    static final String STAE = "STAE";
    static final String CTRY = "CTRY";
    static final String PHON = "PHON";
    static final String EMAI = "EMAIL";
    static final String WWW = "WWW";
    
    final static String SUBM_NAME = "submName";
    final static String SUBM_ADDR = "submAddress";
    final static String SUBM_POST = "submPostcode";
    final static String SUBM_CITY = "submCity";
    final static String SUBM_STAE = "submState";
    final static String SUBM_CTRY = "submCountry";
    final static String SUBM_PHON = "submPhone";
    final static String SUBM_EMAI = "submEmail";
    final static String SUBM_WWW  = "submWeb";

    
    static final String INDI = Gedcom.INDI;
    static final String FIRSTNAME = "FIRSTNAME";
    static final String LASTNAME = "LASTNAME";
    static final String SEX = "SEX";

    
    
    static final String NO_CONVERSION = "0";
    static final String CONVERSION = "1";
    
    static final String CONV_VERSION = "ConversionVersion";
    static final String CONV_VERSION_FROM = "ConversionVersionFrom";
    static final String CONV_VERSION_TO = "ConversionVersionTo";
    static final String CONV_MEDIA = "ConversionMedia";

    static final String CONV_PLACE = "ConversionPlaceFormat";
    static final String CONV_PLACE_FROM = "ConversionPlaceFormatFrom";
    static final String CONV_PLACE_TO = "ConversionPlaceFormatTo";
    static final String CONV_PLACE_MAP = "ConversionPlaceFormatMap";

    static final String ALIGN_PLACE = "AlignPlaceFormat";
    
    static final String RELOCATE_MEDIA = "RelocateMedia";
    static final String RELOCATE_MEDIA_MAP = "MediaMap";
}
