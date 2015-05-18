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
package ancestris.api.core;

/**
 *
 * @author daniel
 */
public interface Version {

    /**
     * The build text
     */
    String getBuildString();

    /**
     * The displayable version number
     */
    String getVersionString();
    
    /**
     * The description of this module (use only for main application)
     * @return 
     */
    String getDescription();
}
