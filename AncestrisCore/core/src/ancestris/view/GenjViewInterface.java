/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.view;

import genj.view.View;

/**
 *
 * @author daniel
 * XXX: temporary bridge API from ancestris.app to genj. Must be removed
 */
public interface GenjViewInterface extends AncestrisViewInterface{
    View getView();

}
