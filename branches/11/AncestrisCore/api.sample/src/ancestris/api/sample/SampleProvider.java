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
import javax.swing.ImageIcon;

/**
 *
 * @author daniel
 */
public interface SampleProvider {
    public File getSampleGedcomFile();
    public URL getSampleGedcomURL();
    
    /**
     * Get sample gedcom Name
     * @return
     */
    public String getName();

    /**
     * Get short gedcom description. Implementor mays localize this description thru,
     * for instance, a Bundle Property
     * @return
     */
    public String getDescription();

    /**
     * Get Icon. 
     * @return
     */
    public ImageIcon getIcon();
}
