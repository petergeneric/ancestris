/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.gedcom.privacy.standard;

import genj.util.Registry;
import org.openide.util.NbBundle;

/**
 *
 * @author daniel
 */
public class Options {
      private static Options instance = null;

      public static Options getInstance(){
          if (instance == null)
              instance = new Options();
          return instance;
      }
  /**
   * Ancestris way
   */
    private Registry getPreferences() {
        return Registry.get(Options.class);
    }

    // GENERAL PRIVACY RULE 1 (GP1) : "privacy years property"
    // GP1-1 : Any information (=property) dated within the most recent n years is private.
    //      For this we need to define the privacy date of a property : it will be the most recent date of any "DATE" property underneath that property
    // GP1-2 : Any 'entity' father of a private years property is also a private years property
    //      (for instance, a person whose death is private, is private, even if its birth says it is not.
    //      (this re-inforces protection by default)
    // GP1-3 : Any 'entity' SOUR, REPO, NOTE, MEDIA, regardless of the date, child of a private years property, is considered a private years property
    //      (this does not apply to INDI nor FAM entities.
    //      (e.g. if a SOUR entity or a MEDIA supports a private birth, it is also private, even if this SOUR or MEDIA has no date or is not marked private itself.
    //      (this is to protect sources documents for instance where the date is not an info of SOUR but still is
    //      (somewhere in the document, the picture, etc.
    private static final String PRIVATE_YEARS   = "private.years";         // NOI18N
    public void setPrivateYears(int value) {
        getPreferences().put(PRIVATE_YEARS,Math.max(value, 0));
    }
    public int  getPrivateYears() {
        return getPreferences().get(PRIVATE_YEARS,75);
    }

    //
    // LOCAL PRIVACY RULE (LP1) : "privacy marked property"
    // All individuals or information *marked* private is private along with information held "below" such mark
    // As a result, information held "above" such mark is not private marked, therefore is public
    private static final String PRIVATE_TAG   = "private.tag";         // NOI18N
    public void setPrivateTag(String privateTag) {
        getPreferences().put(PRIVATE_TAG,privateTag);
    }

    public String getPrivateTag() {
        return getPreferences().get(PRIVATE_TAG,"_PRIV");
    }

    //
    // GENERAL PRIVACY RULE 2 (GP2) : "alive persons"
    // GP2 : Alive persons are private years entity
    // All alive person (or supposed alive : no death event and birth < 120 years) is private, even though the rule of "n" years does not capture it as private
    // All info of a private personne is considered private (applying GR1-1).
    private static final String PRIVATE_ALIVE   = "private.alive";         // NOI18N
    public void setAlivePrivate(boolean value) {
        getPreferences().put(PRIVATE_ALIVE,value);
    }
    public boolean aliveIsPrivate() {
        return getPreferences().get(PRIVATE_ALIVE,true);
    }
    
    private static final String PRIVATE_YEARSALIVE = "private.yearsalive";         // NOI18N
    public void setYearsIndiCanBeAlive(int years) {
        getPreferences().put(PRIVATE_YEARSALIVE,years);
    }
    public int getYearsIndiCanBeAlive() {
        return getPreferences().get(PRIVATE_YEARSALIVE,130);
    }

    
    //
    // EXCEPTION RULE (ER1) : All information of deceased is public, even though any of the above rules says it should be private
    private static final String PUBLIC_DEAD   = "public.dead";         // NOI18N
    public void setDeadIsPublic(boolean value) {
        getPreferences().put(PUBLIC_DEAD,value);
    }
    public boolean deadIsPublic() {
        return getPreferences().get(PUBLIC_DEAD,false);
    }

    private static final String PRIVATE_MASK   = "private.mask";         // NOI18N
    public void setPrivateMask(String value) {
        getPreferences().put(PRIVATE_MASK,value);
    }

    public String getPrivateMask() {
        return getPreferences().get(PRIVATE_MASK,NbBundle.getMessage(Options.class,PRIVATE_MASK));
    }



}
