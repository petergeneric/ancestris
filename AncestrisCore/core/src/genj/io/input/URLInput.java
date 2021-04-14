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
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper for Remote URL management.
 * @author Zurga
 */
public class URLInput extends InputSource {
    private static final Logger LOG = Logger.getLogger("ancestris.app");
    
    private final URL url;
    private String extension; 
    
    public URLInput(URL theUrl) {
        this(theUrl.getFile(), theUrl);
    }
    
    public URLInput(String name, URL theUrl) {
        super(name);
        url = theUrl;
        setLocation(theUrl.toString());
    }
    
    public URL getURL(){
        return url;
    }

    @Override
    public InputStream open() throws IOException {
        try {
            return url.openStream();
        } catch (IOException ioe) {
            LOG.log(Level.INFO, "Unable to open remote adress " + url.toString(), ioe);
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
        try {
            String type = URLConnection.guessContentTypeFromStream(url.openStream());
            LOG.log(Level.FINE, "Media "+ getName() + " type from internet : " + type);
            if (type == null) {
                type = url.openConnection().getContentType();
                if (type == null){
                    return "web";
                }
                LOG.log(Level.FINE, "Media "+ getName() + " type from internet : " + type);
            }
            if (type.startsWith("image/")) {
                return type.substring(6);
            }
            return "web";
            
        } catch(IOException e) {
            LOG.log(Level.INFO, "Unable to open remote adress " + url.toString(), e);
            return "web";
        }
    }
    
   
}
