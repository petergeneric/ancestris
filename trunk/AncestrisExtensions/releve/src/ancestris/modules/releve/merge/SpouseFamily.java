package ancestris.modules.releve.merge;

import genj.gedcom.Fam;

/**
 *
 * @author michel
 */


public class SpouseFamily {
    protected final Fam family;
    protected final SpouseTag tag; 
    SpouseFamily(Fam family, SpouseTag tag) {
        this.family = family;
        this.tag = tag;
    }
    
    @Override
    public boolean equals(Object object) {
        if( object instanceof SpouseFamily) {
            return this.family.getId().equals( ((SpouseFamily) object).family.getId())
                    && this.tag.equals(((SpouseFamily) object).tag);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.family != null ? this.family.hashCode() : 0);
        hash = 17 * hash + (this.tag != null ? this.tag.hashCode() : 0);
        return hash;
    }
    
}
