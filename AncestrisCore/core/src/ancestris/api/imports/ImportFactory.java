/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2018 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.api.imports;

/**
 *
 * @author frederic
 */
public class ImportFactory {

    public static ImportRunner createImport(Import i) {
        return i;
    }

}
