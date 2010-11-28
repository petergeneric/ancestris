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
package genj.util;

import java.io.File;
import java.io.InputStream;

public interface IRegistryStorageFactory {

    public IRegistryStorage get(Class cls);

    public IRegistryStorage get(File file);

    public IRegistryStorage get(InputStream in);

    public IRegistryStorage get(String pckg);
}
