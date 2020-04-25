/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package modules.editors.gedcomproperties.utils;

import genj.gedcom.PropertyFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author frederic
 */
public class PathData implements Comparable<PathData> {

    public boolean found = true;                    // Found indicator for the files
    public int nbMedia = 0;                         // Nb of different media files in that set
    public boolean relative = false;                // Whether this new path should be absolute or relative
    public String newPath = "";                     // The new directory path
    private List<PropertyFile> files = null;        // The actual property files in the gedcom file that are depending on this directory path
    
    public PathData(boolean found, boolean relative, PropertyFile pf, String newPath) {
        files = new ArrayList<>();
        this.found = found;
        this.relative = relative;
        this.newPath = newPath;
        addFile(pf);
    }

    public String getKey() {
        return newPath + (found ? "1" : "0");
    }

    public void addFile(PropertyFile pf) {
        files.add(pf);
    }

    public List<PropertyFile> getFiles() {
        return files;
    }

    public int getNbMedia() {
        Set<String> list = new TreeSet<>();
        for (PropertyFile pf : files) {
            list.add(pf.getValue());
        }
        return list.size();
    }

    @Override
    public int compareTo(PathData o) {
        return this.getKey().compareTo(o.getKey());
    }
    
}
