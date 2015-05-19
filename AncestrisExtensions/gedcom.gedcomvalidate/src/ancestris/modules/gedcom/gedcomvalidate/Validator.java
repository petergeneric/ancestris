/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.Gedcom;
import genj.util.Trackable;
import genj.view.ViewContext;
import java.util.List;

/**
 *
 * @author daniel
 */
public interface Validator extends Trackable{
    public List<ViewContext> start();
}
