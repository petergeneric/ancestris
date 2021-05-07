/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2021 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Repository;
import genj.gedcom.Submitter;
import genj.gedcom.TagPath;
import genj.view.ViewContext;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Find isolated entities.
 * Find notes unused, family with no spouse nor children, indi isolated, source unused, repo unused, media unused.
 * @author Zurga
 */

public class TestIsolated extends Test {
    
    public TestIsolated() {
        super(Gedcom.ENTITIES, Entity.class);
    }

    @Override
    void test(Property prop, TagPath path, List<ViewContext> issues, GedcomValidate report) {
       if (prop instanceof Entity) {
           Entity e = (Entity) prop;
           Entity[] liste = PropertyXRef.getReferences(e);
           if (liste.length == 0){
               issues.add(new ViewContext(prop).setCode(getCode()).setText(NbBundle.getMessage(this.getClass(), "err.isolated")));
               return;
           }
           // Repo or Submitter are not relataed to humans, but to header or sources.
           // Media are frequently linked directly to sources.
           if (e instanceof Repository || e instanceof Submitter|| e instanceof Media) {
               return;
           }
           // Check Indi, Fam, Source, Note
           List<Entity> humans = new ArrayList<>();
           for (Entity ref : liste) {
               if (ref instanceof Indi || ref instanceof Fam) {
                   humans.add(ref);
               }
           }
           if (humans.isEmpty()) {
               issues.add(new ViewContext(prop).setCode(getCode()).setText(NbBundle.getMessage(this.getClass(), "err.isolatedHumans")));
           }
       }
    }

    @Override
    String getCode() {
        return "15";
    }
    
}
