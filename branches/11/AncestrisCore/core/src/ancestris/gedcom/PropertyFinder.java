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
package ancestris.gedcom;

import ancestris.gedcom.AbstractPropertyFinder.TagPathFinder;
import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import java.util.Collection;

/**
 *
 * @author daniel
 */
//XXX: write javadoc
public interface PropertyFinder {

    public Collection<Entity> find(Entity entity);

    public Collection<Entity> find(Collection<Entity> roots);

    public PropertyFinder or(PropertyFinder finder);

    public PropertyFinder and(PropertyFinder finder);

    public PropertyFinder sex(int sex);

    public static final class Constants {

        private Constants() {
        }
        //FIXME: Do we need all these shortcuts?
        /** Father */
        public static final PropertyFinder FATHER = new TagPathFinder(Indi.class, "INDI:FAMC:*:..:HUSB:*");
        /** Mother */
        public static final PropertyFinder MOTHER = new TagPathFinder(Indi.class, "INDI:FAMC:*:..:WIFE:*");
        /** Parent (ie Father or Mother) */
        public static final PropertyFinder PARENT = FATHER.or(MOTHER);
        /** GrandParent (parents of parents) */
        public static final PropertyFinder GRANDPARENT = PARENT.and(PARENT);
        /** Sibling (Brother or sister) */
        public static final PropertyFinder SIBLING = new TagPathFinder(Indi.class, "INDI:FAMC:*:..:CHIL:*");
        /** Brother */
        public static final PropertyFinder BROTHER = SIBLING.sex(PropertySex.MALE);
        /** Sister */
        public static final PropertyFinder SISTER = SIBLING.sex(PropertySex.FEMALE);
        /** Husband */
        public static final PropertyFinder HUSBAND = new TagPathFinder(Indi.class, "INDI:FAMS:*:..:HUSB:*");
        /** Wife */
        public static final PropertyFinder WIFE = new TagPathFinder(Indi.class, "INDI:FAMS:*:..:WIFE:*");
        /** Spouse (generic spouse access for an Indi).
         * For a Male Indi, returned values will be all wives,
         * for a Female Ind, returned values will be all husbands
         */
        public static final PropertyFinder SPOUSE = HUSBAND.or(WIFE);
        /** Child */
        public static final PropertyFinder CHILD = new TagPathFinder(Indi.class, "INDI:FAMS:*:..:CHIL:*");
        /** Daughter */
        public static final PropertyFinder DAUGHTER = CHILD.sex(PropertySex.FEMALE);
        /** Son */
        public static final PropertyFinder SON = CHILD.sex(PropertySex.MALE);
        /** GrandSon: son of children */
        public static final PropertyFinder GRANDSON = CHILD.and(SON);
        /** GrandDaughter: daughter of children */
        public static final PropertyFinder GRANDDAUGHTER = CHILD.and(DAUGHTER);
        /** Child of children */
        public static final PropertyFinder GRANDCHILD = CHILD.and(CHILD);
        /** */
        public static final PropertyFinder UNCLE = PARENT.and(BROTHER).or(PARENT.and(SISTER).and(HUSBAND));
        public static final PropertyFinder AUNT = PARENT.and(SISTER).or(PARENT.and(BROTHER).and(WIFE));
        public static final PropertyFinder UNCLE_AUNT = PARENT.and(SIBLING);
        /** */
        public static final PropertyFinder NEPHEW = SIBLING.and(CHILD);
        public static final PropertyFinder FIRSTCOUSIN = PARENT.and(SIBLING).and(CHILD);
    }
}
