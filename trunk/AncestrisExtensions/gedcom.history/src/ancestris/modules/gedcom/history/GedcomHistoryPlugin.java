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
package ancestris.modules.gedcom.history;

import ancestris.core.pluginservice.AncestrisPlugin;
import genj.app.GedcomFileListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.io.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lemovice
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class GedcomHistoryPlugin extends AncestrisPlugin implements GedcomFileListener {

    private final static Logger log = Logger.getLogger(GedcomHistoryPlugin.class.getName());
    private final static HashMap<String, GedcomHistory> gedcomHistoryMap = new <String, GedcomHistory>HashMap();

    @Override
    public void commitRequested(Context context) {
        log.log(Level.INFO, "Commit requested {0}", context.getGedcom().getName());
    }

    @Override
    public void gedcomClosed(Gedcom gedcom) {
        String gedcomName = gedcom.getName().substring(0, gedcom.getName().lastIndexOf(".") == -1 ? gedcom.getName().length() : gedcom.getName().lastIndexOf("."));
        File cacheSubdirectory = Places.getCacheSubdirectory(GedcomHistoryPlugin.class.getCanonicalName());
        File historyFile = new File(cacheSubdirectory.getAbsolutePath() + System.getProperty("file.separator") + gedcomName + ".hist");
        log.log(Level.INFO, "saving history File {0}", historyFile.getAbsoluteFile());

        GedcomHistory gedcomHistory = gedcomHistoryMap.get(gedcomName);
        gedcom.removeGedcomListener(gedcomHistoryMap.get(gedcomName));
        gedcomHistoryMap.remove(gedcomName);
        try {
            if (gedcomHistory.getHistoryList().isEmpty() ==  false) {
                // create JAXB context and instantiate marshaller
                JAXBContext context = JAXBContext.newInstance(GedcomHistory.class);
                Marshaller m = context.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                m.marshal(gedcomHistory, new FileWriter(historyFile));
            }
        } catch (JAXBException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void gedcomOpened(Gedcom gedcom) {
        String gedcomName = gedcom.getName().substring(0, gedcom.getName().lastIndexOf(".") == -1 ? gedcom.getName().length() : gedcom.getName().lastIndexOf("."));
        File cacheSubdirectory = Places.getCacheSubdirectory(GedcomHistoryPlugin.class.getCanonicalName());
        File historyFile = new File(cacheSubdirectory.getAbsolutePath() + System.getProperty("file.separator") + gedcomName + ".hist");
        log.log(Level.INFO, "opening history file {0}", historyFile.getAbsoluteFile());

        if (historyFile.exists() == true) {
            try {
                JAXBContext context = JAXBContext.newInstance(GedcomHistory.class);

                Unmarshaller um = context.createUnmarshaller();
                GedcomHistory gedcomHistory;
                try {
                    gedcomHistory = (GedcomHistory) um.unmarshal(new FileReader(historyFile));
                    gedcomHistoryMap.put(gedcomName, gedcomHistory);
                    gedcom.addGedcomListener(gedcomHistoryMap.get(gedcomName));
                } catch (FileNotFoundException ex) {
                    gedcomHistoryMap.put(gedcomName, new GedcomHistory(gedcomName));
                    gedcom.addGedcomListener(gedcomHistoryMap.get(gedcomName));
                }
            } catch (JAXBException ex) {
                gedcomHistoryMap.put(gedcomName, new GedcomHistory(gedcomName));
                gedcom.addGedcomListener(gedcomHistoryMap.get(gedcomName));
            }
        } else {
            gedcomHistoryMap.put(gedcomName, new GedcomHistory(gedcomName));
            gedcom.addGedcomListener(gedcomHistoryMap.get(gedcomName));
        }
    }

    public GedcomHistory getGedcomHistory(String gedcomName) {
        return gedcomHistoryMap.get(gedcomName);
    }
}
