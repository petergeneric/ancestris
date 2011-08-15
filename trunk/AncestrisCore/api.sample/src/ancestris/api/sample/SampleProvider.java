/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2010 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.api.sample;

import java.io.File;
import java.net.URL;

/**
 *
 * @author daniel
 */
public interface SampleProvider {
    public File getSampleGedcomFile();
    public URL getSampleGedcomURL();
}
