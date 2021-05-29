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
package ancestris.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Some utilities for URL to and from conversion
 * @author daniel
 */
public class URLUtil {
    
    // prevent instanciation
    private URLUtil(){
    }

    /**
     * just File.toURI.toURL wrapper. 
     * @param file
     * @return URL or null in case of exception
     */
    public static URL toURL(File file){
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException ex) {
            return null;
        }
    }
    
    /**
     * tries to convert text to a URL. If 'text' is a valid URL text representation
     * then return this URL. If not, then tries to convert 'text' to a File and return
     * this File URL. Returns null on error.
     * if text is empty string, return curent working directory
     * @param text
     * @return 
     */
    public static URL toURL(String text){
        URL result;
        try {
            result = new URL(text);
        } catch (MalformedURLException ex) {
            result = toURL(new File(text));
        }
        return result;
    }
}
