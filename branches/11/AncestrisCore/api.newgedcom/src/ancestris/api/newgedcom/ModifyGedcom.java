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
package ancestris.api.newgedcom;

import genj.gedcom.Context;


/**
 *
 * @author daniel
 */
public interface ModifyGedcom {
    public boolean isReady();
    public Context create();
    public Context update();
    public Context update(Context context);
}
