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

import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author daniel
 */
//XXX: Write javadoc
public abstract class AbstractPropertyFinder implements PropertyFinder {

    public PropertyFinder or(PropertyFinder finder) {
        return new Or(this, finder);
    }

    public PropertyFinder and(PropertyFinder finder) {
        return new And(this, finder);
    }

    public PropertyFinder sex(int sex) {
        return new SexPathFinder(this, sex);
    }

    /**
     * Find all relatives of given roots and expression
     */
    public Collection<Entity> find(Collection<Entity> roots) {
        List<Entity> result = new ArrayList<Entity>();
        for (Entity indi : roots) {
            result.addAll(find(indi));
        }
        return result;
    }

    public static class Or extends AbstractPropertyFinder {

        PropertyFinder pf1;
        PropertyFinder pf2;

        Or(PropertyFinder pf1, PropertyFinder pf2) {
            this.pf1 = pf1;
            this.pf2 = pf2;
        }

        public Collection<Entity> find(Entity entity) {
            List<Entity> result = new ArrayList<Entity>();
            result.addAll(pf1.find(entity));
            result.addAll(pf2.find(entity));
            return result;
        }
    }

    public static class And extends AbstractPropertyFinder {

        PropertyFinder pf1;
        PropertyFinder pf2;

        And(PropertyFinder pf1, PropertyFinder pf2) {
            this.pf1 = pf1;
            this.pf2 = pf2;
        }

        public Collection<Entity> find(Entity entity) {
            return pf2.find(pf1.find(entity));
        }
    }

    public static class SexPathFinder extends AbstractPropertyFinder {

        PropertyFinder finder;
        int sex;

        SexPathFinder(PropertyFinder finder, int sex) {
            this.finder = finder;
            this.sex = sex;
        }

        public Collection<Entity> find(Entity entity) {
            List<Entity> result = new ArrayList<Entity>();
            for (Entity found : finder.find(entity)) {
                if (found instanceof Indi && (((Indi) found).getSex() == sex)) {
                    result.add(found);
                }
            }
            return result;
        }
    }

    public static class TagPathFinder extends AbstractPropertyFinder {

        Class<? extends Entity> applyOn;
        TagPath path;

        public TagPathFinder(Class<? extends Entity> applyOn, TagPath path) {
            this.applyOn = applyOn;
            this.path = path;
        }

        public TagPathFinder(Class<? extends Entity> applyOn, String path) {
            this.applyOn = applyOn;
            this.path = new TagPath(path);
        }

        public Collection<Entity> find(Entity entity) {
            Collection<Entity> result = new ArrayList<Entity>();
            if (entity.getClass().isAssignableFrom(applyOn)) {
                for (Property p : entity.getProperties(path)) {
                    if (!entity.equals(p.getEntity())) {
                        result.add(p.getEntity());
                    }
                }
            }
            return result;
        }
    }
}
