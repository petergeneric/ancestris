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

package ancestris.api.imports;

import java.util.UUID;

/**
 *
 * @author frederic
 */
public class ImportFix {
    
    private UUID id;
    private String xref;
    private String code;
    private String oldTag;
    private String newTag;
    private String oldValue;
    private String newValue;
    
    /**
     * Constructor
     */
    public ImportFix(String xref, String code, String oldTag, String newTag, String oldValue, String newValue) {
        this.id = UUID.randomUUID();
        this.xref = xref;
        this.code = code;
        this.oldTag = oldTag;
        this.newTag = newTag;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }    
    
    public UUID getId() {
        return id;
    }
    
    public String getXref() {
        return xref;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getOldTag() {
        return oldTag;
    }
    
    public String getOldValue() {
        return oldValue;
    }
    
    public String getNewTag() {
        return newTag;
    }
    
    public String getNewValue() {
        return newValue;
    }
    
    
    
}
