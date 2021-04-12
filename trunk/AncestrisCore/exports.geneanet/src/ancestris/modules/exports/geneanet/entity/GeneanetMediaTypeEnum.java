/*
 * Copyright (C) 2020 Zurga
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ancestris.modules.exports.geneanet.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum to get Type available in Geneanet.
 * @author Zurga
 */
public enum GeneanetMediaTypeEnum {
     AUTRES("autres"), 
     PORTRAITS("portraits"),
     ARCHIVE_FAM("archive_familiale"),
     DOC_NOT("doc_notarial"), 
     DOC_MIL("doc_militaire"), 
     GROUPE("photo_groupe"), 
     ETAT_CIVIL("etat_civil"), 
     RECENS("recensement"),
     STOP("stop");
     
     private static final Map<String, GeneanetMediaTypeEnum> INDEX = new HashMap<>(GeneanetMediaTypeEnum.values().length);
     
     static {
         for (GeneanetMediaTypeEnum gmte : GeneanetMediaTypeEnum.values()) {
             INDEX.put(gmte.getType(), gmte);
         }
     }
     
     private String type;

    private GeneanetMediaTypeEnum(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    /**
     * Find an Enum from String. Default Value AUTRES.
     * @param leType type to find
     * @return  Enum
     */
    public static GeneanetMediaTypeEnum getValue(String leType) {
        GeneanetMediaTypeEnum retour = INDEX.get(leType);
        if (retour == null) {
            retour = AUTRES;
        }
        return retour;
    }
}
