/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2022 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.familygroups;

import genj.util.Trackable;

/**
 *
 * @author frederic
 */
public interface FamilyGroupsRunner extends Runnable, Trackable {

    public FamilyGroupsPlugin getFgp();
    
}
