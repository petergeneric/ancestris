/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2021 Ancestris
 * 
 * Author: Zurga (zurga@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.tree;

/**
 * Class to design a separator in bookmark list Could have a description and
 * only a line
 *
 * @author Zurga
 */
public class BookmarkSeparator extends Bookmark {

    public BookmarkSeparator(String s) {
        super(s, null);
    }

    @Override
    public String getName() {
        final StringBuilder sb = new StringBuilder(super.getName());
        while (sb.length() < 50) {
            sb.append('-');
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getName();
    }

}
