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
import ancestris.gedcom.GedcomFileListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.io.*;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

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
        String gedcomName = context.getGedcom().getName().substring(0, context.getGedcom().getName().lastIndexOf(".") == -1 ? context.getGedcom().getName().length() : context.getGedcom().getName().lastIndexOf("."));

        log.log(Level.INFO, "Commit requested {0}", context.getGedcom().getName());

        File cacheSubdirectory = Places.getCacheSubdirectory(GedcomHistoryPlugin.class.getCanonicalName());
        File historyFile = new File(cacheSubdirectory.getAbsolutePath() + System.getProperty("file.separator") + gedcomName + ".hist");
        GedcomHistory gedcomHistory = gedcomHistoryMap.get(gedcomName);

        if (gedcomHistory.getHistoryList().isEmpty() == false) {
            log.log(Level.INFO, "saving history File {0}", historyFile.getAbsoluteFile());
            try {
                // create JAXB context and instantiate marshaller
                JAXBContext jaxbContext = JAXBContext.newInstance(GedcomHistory.class);
                Marshaller m = jaxbContext.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                m.marshal(gedcomHistory, new FileWriter(historyFile));
            } catch (JAXBException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public void gedcomClosed(Gedcom gedcom) {
        String gedcomName = gedcom.getName().substring(0, gedcom.getName().lastIndexOf(".") == -1 ? gedcom.getName().length() : gedcom.getName().lastIndexOf("."));

        log.log(Level.INFO, "closing gedcom {0}", gedcomName);

        gedcom.removeGedcomListener(gedcomHistoryMap.get(gedcomName));
        gedcomHistoryMap.remove(gedcomName);
        Set<TopComponent> openedTopComponent = TopComponent.getRegistry().getOpened();
        for (TopComponent topComponent : openedTopComponent) {
            if (topComponent instanceof GedcomHistoryTopComponent) {
                if (((GedcomHistoryTopComponent) topComponent).getGedcom().equals(gedcom) == true) {
                    topComponent.close();
                }
            }
        }
    }

    @Override
    public void gedcomOpened(Gedcom gedcom) {
        String gedcomName = gedcom.getName().substring(0, gedcom.getName().lastIndexOf(".") == -1 ? gedcom.getName().length() : gedcom.getName().lastIndexOf("."));
        if (gedcomHistoryMap.containsKey(gedcomName) == false) {
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
        } else {
            log.log(Level.INFO, "history file already open");
        }
    }

    public GedcomHistory getGedcomHistory(String gedcomName) {
        return gedcomHistoryMap.get(gedcomName);
    }
}
