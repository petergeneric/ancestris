/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.util;

import genj.app.Workbench;
import genj.gedcom.Gedcom;
import genj.util.Registry;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.openide.util.Exceptions;

/**
 *
 * @author daniel
 */
public class RecentFiles {

    private static RecentFiles instance;

    public static RecentFiles getDefault() {
        if (instance == null) {
            instance = new RecentFiles();
        }
        return instance;
    }

    private RecentFiles() {
    }
    public List<GedcomFileInformation> getRecentFilesInformation(){
          List<String>  list = Registry.get(Workbench.class).get("history", new ArrayList<String>());
          List<GedcomFileInformation> result=new ArrayList<GedcomFileInformation>();
          for (String file:list){
              result.add(new GedcomFileInformation(file));
          }
          return result;
    }

    public static class GedcomFileInformation{

        URL url;
        public GedcomFileInformation(String url) {
            try {
                this.url = new URL(url);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public GedcomFileInformation(URL url) {
            this.url = url;
        }

        public Icon getIcon(){
            return Gedcom.getImage();
        }

        public String getDisplayName(){
            return ("Genealogie "+url);
        }
        public URL getURL() {
            return url;
        }

    }

}
