/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.renderer.velocity;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;

/**
 *
 * @author daniel
 */
public class FamWrapper extends EntityWrapper {
    private Indi refIndi = null;

    FamWrapper(Fam f, Indi indi) {
        this(f);
        refIndi = indi;
    }

    FamWrapper(Fam f) {
        super(f);
    }

    public PropertyWrapper getOtherSpouse() {
        if (refIndi == null) {
            return null;
        }
        Indi i = ((Fam) property).getOtherSpouse(refIndi);
        return create(i);
    }
    
    public PropertyWrapper getOtherSpouse(IndiWrapper refIndi) {
        if (refIndi == null) {
            return null;
        }
        Indi i = ((Fam) property).getOtherSpouse((Indi) refIndi.property);
        return create(i);
    }

    public String getShortValue() {
        return toString();
    }

    @Override
    public String toString() {
        // Might be null
        if (property == null) {
            return "";
        }
        return property.toString() + ((Entity) property).format("MARR", "{ le $D}{ \ufffd $P}");
    }

    public PropertyWrapper[] getChildren() {
        Indi[] ch = ((Fam) property).getChildren();
        PropertyWrapper[] result = new PropertyWrapper[ch.length];
        for (int c = 0; c < ch.length; c++) {
            result[c] = create(ch[c]);
        }
        return result;
    }

    public PropertyWrapper getHusband() {
        Indi i = ((Fam) property).getHusband();
        return create(i);
    }

    public PropertyWrapper getWife() {
        Indi i = ((Fam) property).getWife();
        return create(i);
    }

    public String getName() {
        return ((Fam) property).getPropertyName();
    }

    
}
