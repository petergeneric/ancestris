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
package ancestris.util;

import genj.util.IRegistryStorage;
import genj.util.IRegistryStorageFactory;
import java.io.File;
import java.io.InputStream;

public class RegistryStorageFactory implements IRegistryStorageFactory {
    private static RegistryStorageFactory factory;

    private RegistryStorageFactory() {
    }

    public static IRegistryStorageFactory getFactory() {
        if (factory == null){
            factory = new RegistryStorageFactory();
        }
        return factory;
    }

    public IRegistryStorage get(Class cls) {
        return new RegistryStorage.Preferences(cls);
    }

    public IRegistryStorage get(File file) {
        return new RegistryStorage.Properties(file);
    }

    public IRegistryStorage get(InputStream in) {
        return new RegistryStorage.Properties(in);
    }

    public IRegistryStorage get(String pckg) {
        return new RegistryStorage.Preferences(pckg);
    }

}
