/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.treesharing.communication;

import ancestris.util.swing.DialogManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author frederic
 */
public class Comm {

    private static String COMM_SERVER = "http://www.lapeyre-frederic.com/sharingmembers/";
    private static String COMM_CREDENTIALS = "user=lapeyre_smanc&pw=GtresF789et";
    private int COMM_PORT = 4584;

    private volatile boolean stopRun;
    private Thread listeningThread;
    
    /**
     * Constructor
     */
    public void Comm() {
        
    }
    
    /**
     * Register on Ancestris server that I am ready to share 
     * 
     * @param myName
     * @param myAccess
     * @return 
     */
    public boolean registerMe(String myName, String myAccess) {

        String myIPAddress = "default";
        String duplicateError = "duplicate entry";
        String timestamp = new Timestamp(new java.util.Date().getTime()).toString().replace(" ", "_");

        String outputString = getQueryResult(COMM_SERVER + "register_member.php?" + COMM_CREDENTIALS + "&pseudo=" + myName + "&access=" + myIPAddress + "&comment=" + timestamp);

        if (!outputString.trim().isEmpty()) {
            String errorMsg = "";
            if (outputString.toLowerCase().contains(duplicateError)) {
                errorMsg = NbBundle.getMessage(Comm.class, "ERR_DuplicatePseudo");
            } else {
                errorMsg = outputString;
            }
            DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Registration"), errorMsg).setMessageType(DialogManager.ERROR_MESSAGE).show();
            return false;
        }
         
        return true;
    }
    

    /**
     * Tell Ancestris server that I am no longer ready to share
     * 
     * @param myName
     * @return 
     */
    public boolean unregisterMe(String myName) {

        String outputString = getQueryResult(COMM_SERVER + "unregister_member.php?" + COMM_CREDENTIALS + "&pseudo=" + myName);

        if (!outputString.trim().isEmpty()) {
            DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Unregistration"), outputString).setMessageType(DialogManager.ERROR_MESSAGE).show();
            return false;
        }
        
        return true;
    }
    

    /**
     * Identify the list of currently sharing friends from the ancestris server (crypted communication)
     * 
     * @return all Ancestris friends sharing something
     */
    public List<AncestrisMember> getAncestrisMembers() {

        List<AncestrisMember> ancestrisMembers = new ArrayList<AncestrisMember>();
        
        String outputString = getQueryResult(COMM_SERVER + "get_members.php?" + COMM_CREDENTIALS + "&format=xml");

        try {
            String xmlTagMember = "member";
            String xmlTagPseudo = "pseudo";
            String xmlTagAccess = "access";
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(outputString));
            Document doc = db.parse(is);  
            doc.getDocumentElement().normalize();  
            NodeList nodeList = doc.getElementsByTagName(xmlTagMember);

            for (int temp = 0; temp < nodeList.getLength(); temp++) {
                Node node = nodeList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element member = (Element) node;
                    String pseudo = member.getElementsByTagName(xmlTagPseudo).item(0).getTextContent();
                    String access = member.getElementsByTagName(xmlTagAccess).item(0).getTextContent();
                    ancestrisMembers.add(new AncestrisMember(pseudo, access));
                }
            }
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        } catch (DOMException ex) {
            Exceptions.printStackTrace(ex);
        }
                
        // Collect list of Ancestris friends (registered name and access)
        ancestrisMembers.add(new AncestrisMember("François", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Daniel", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Yannick", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Dominique", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Valérie", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Jeannot", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("FrançoiS", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Ben", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Patrice", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Monique", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Frédéric", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Rodolphe", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Agnès", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Eric", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Mathilde", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Christophe", "xxxx"));
        ancestrisMembers.add(new AncestrisMember("Guillemette", "xxxx"));
        
        // Return list
        return ancestrisMembers;
    }
    
    
    
    

    
    
    
    
    /**
     * Opens door allowing friends to connect to me
     * 
     * @return 
     */
    public boolean startListeningtoFriends() {
        stopRun = false;

//        listeningThread = new Thread() {
//            @Override
//            public void run() {
//                listen();
//            }
//        };
//        listeningThread.start();
//        
//        call();
        return true;
    }


    /**
     * Closes door stopping friends from listening to me
     * 
     * @return 
     */
    public boolean stopListeningtoFriends() {
        stopRun = true;
        //listeningThread.interrupt();
        return true;
    }

    
    /**
     * Establish a 1-to-1 communications with a sharing Ancestris friend (crypted communication)
     *      
     * @param friend
     * @return  true if connection established
     *          false if sharing criteria not matching request, or if connection is not working
     */
    public boolean connectToAncestrisFriend(AncestrisMember member) {
        // Connect to ancestris program of friend
        
        return true;
    }

    
    private String getQueryResult(String url) {

        String ret = "";

        try {
            String responseString = "";
            URL data = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) data.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((responseString = in.readLine()) != null) {
                ret = ret + responseString;
            }
            in.close();
            connection.disconnect();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return ret;
    }

    
    
    
    public void listen() {
        try {
            ServerSocket serversock = new ServerSocket(COMM_PORT);

            while (!stopRun) {
                Socket outgoing = serversock.accept();
                PrintWriter writer = new PrintWriter(outgoing.getOutputStream());
                writer.println("Hello there");
                writer.close();
            }
        } catch (Exception e) {
            System.out.println("Server Side Error");
        }
    }

    
    public void call() {
        try {
            Socket incoming = new Socket("127.0.0.1", COMM_PORT);
            InputStreamReader stream = new InputStreamReader(incoming.getInputStream());
            BufferedReader reader = new BufferedReader(stream);
            String advice = reader.readLine();
            reader.close();
            System.out.println("Today's advice is " + advice);
        } catch (Exception e) {
            System.out.println("Client Side Error");
        }
    }

    
}
