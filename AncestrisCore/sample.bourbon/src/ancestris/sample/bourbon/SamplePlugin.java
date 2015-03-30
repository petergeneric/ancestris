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

package ancestris.sample.bourbon;

import ancestris.api.sample.SampleProvider;
import ancestris.core.pluginservice.AncestrisPlugin;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ImageIcon;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service=ancestris.core.pluginservice.PluginInterface.class)
public class SamplePlugin extends AncestrisPlugin implements SampleProvider {

    @Override
    public File getSampleGedcomFile() {
        try {
            final URL jarUrl = this.getClass().getResource("");
            final JarURLConnection connection = (JarURLConnection) jarUrl.openConnection();
            File basedir = FileUtil.archiveOrDirForURL(connection.getJarFileURL()).getParentFile().getParentFile();
            return new File(basedir, "exemples" + File.separator + "gen-bourbon" + File.separator + "bourbon.ged");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public URL getSampleGedcomURL() {
        try {
            return getSampleGedcomFile().toURI().toURL();
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public String getName() {
        return getPluginDisplayName();
    }

    @Override
    public String getDescription() {
        return getPluginShortDescription();
    }

    public ImageIcon getIcon() {
        return new ImageIcon(ImageUtilities.loadImage("ancestris/sample/bourbon/ico_bourbon.png"));
    }
}
