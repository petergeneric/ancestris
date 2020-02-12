/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.views.graph;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import java.util.HashSet;
import java.util.Set;

/**
 * Clas to keep entities Modified.
 * @author Zurga
 */
public class ModifEntity {
   private final Set<Indi> indiAdded = new HashSet<>();
   private final Set<Indi> indiDeleted = new HashSet<>();
   private final Set<Indi> indiModified = new HashSet<>();
   private final Set<Fam> famAdded = new HashSet<>();
   private final Set<Fam> famDeleted = new HashSet<>();
   private final Set<Fam> famModified = new HashSet<>();
   
   /**
    * Add entity in added set.
    * @param entity entity to add
    */
   public void addEntity(Entity entity) {
       if (entity instanceof Indi) {
           indiAdded.add((Indi) entity);
       }
       if (entity instanceof Fam) {
           famAdded.add((Fam) entity);
       }
   }
   
   /**
    * Add entity in deleted set.
    * @param entity entity to add
    */
   public void deleteEntity(Entity entity) {
       if (entity instanceof Indi) {
           indiDeleted.add((Indi) entity);
       }
       if (entity instanceof Fam) {
           famDeleted.add((Fam) entity);
       }
   }
   
   /**
    * Add entity in modified set.
    * @param entity entity to add
    */
   public void modifyEntity(Entity entity) {
       if (entity instanceof Indi) {
           indiModified.add((Indi) entity);
       }
       if (entity instanceof Fam) {
           famModified.add((Fam) entity);
       }
   }
   
   public void clear() {
       indiAdded.clear(); 
       indiDeleted.clear();
       indiModified.clear();
       famAdded.clear();
       famDeleted.clear();
       famModified.clear();
   }
   
    public Set<Indi> getIndiAdded() {
        return indiAdded;
    }

    public Set<Indi> getIndiDeleted() {
        return indiDeleted;
    }

    public Set<Indi> getIndiModified() {
        return indiModified;
    }

    public Set<Fam> getFamAdded() {
        return famAdded;
    }

    public Set<Fam> getFamDeleted() {
        return famDeleted;
    }

    public Set<Fam> getFamModified() {
        return famModified;
    }
    
}
