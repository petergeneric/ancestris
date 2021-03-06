/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.io.input;

import genj.io.InputSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper for Remote URL management.
 *
 * @author Zurga
 */
public class URLInput extends InputSource {

    private static final Logger LOG = Logger.getLogger("ancestris.app");

    public final static String WEB = "web";

    private URL url;
    private String extension;
    private boolean isAvailable = true;
    private boolean checkRedirect = false;

    public URLInput(URL theUrl) {
        this(theUrl.getFile(), theUrl);
    }

    public URLInput(String name, URL theUrl) {
        super(name);
        url = theUrl;
        setLocation(theUrl.toString());
    }

    public URL getURL() {
        return url;
    }

    @Override
    public InputStream open() throws IOException {
        if (!isAvailable) {
            return null;
        }
        checkRedirection();
        try {
            return url.openStream();
        } catch (IOException ioe) {
            LOG.log(Level.INFO, "Unable to open remote adress " + url.toString(), ioe);
            isAvailable = false;
            return null;
        }
    }

    @Override
    public int hashCode() {
        return url.hashCode() * getName().hashCode();
    }

    @Override
    public String toString() {
        return "file name=" + getName() + " url=" + url.toString();
    }

    @Override
    public String getExtension() {
        if (extension == null) {
            extension = setExtension();
        }
        return extension;
    }

    private String setExtension() {
        if (!isAvailable) {
            return WEB;
        }
        checkRedirection();
        try {
            String type = URLConnection.guessContentTypeFromStream(url.openStream());
            LOG.log(Level.FINE, "Media " + getName() + " type from internet : " + type);
            if (type == null) {
                type = url.openConnection().getContentType();
                if (type == null) {
                    return WEB;
                }
                LOG.log(Level.FINE, "Media " + getName() + " type from internet : " + type);
            }
            if (type.startsWith("image/")) {
                return type.substring(6);
            }
            return WEB;

        } catch (IOException e) {
            LOG.log(Level.INFO, "Unable to open remote adress " + url.toString(), e);
            return WEB;
        }
    }

    private void checkRedirection() {
        // Don't check twice.
        if (checkRedirect) {
            return;
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            int rcode = conn.getResponseCode();

            if (rcode == HttpURLConnection.HTTP_MOVED_PERM || rcode == HttpURLConnection.HTTP_MOVED_TEMP || rcode == HttpURLConnection.HTTP_SEE_OTHER) {
                url = new URL(conn.getHeaderField("Location"));
            }
        } catch (IOException e) {
            LOG.log(Level.INFO, "Unable to open remote adress " + url.toString(), e);
            isAvailable = false;
        }
        //in any case, check is done
        checkRedirect = true;
    }

}
