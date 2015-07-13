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

import ancestris.modules.treesharing.TreeSharingTopComponent;
import ancestris.util.swing.DialogManager;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.StringEscapeUtils;
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

    private TreeSharingTopComponent owner;
    
    private final static Logger LOG = Logger.getLogger("ancestris.modules.treesharing.communication");

    private static String COMM_SERVER = "http://share.ancestris.org/"; 
    private static String COMM_CREDENTIALS = "user=ancestrishare&pw=2fQB";
    private int COMM_PORT = 4584;
    private int COMM_TIMEOUT = 1000; // One second
    private volatile boolean stopRun;
    private Thread listeningThread;

    /**
     * Constructor
     */
    public Comm(TreeSharingTopComponent tstc) {
        this.owner = tstc;
    }
    
    /**
     * Register on Ancestris server that I am ready to share 
     * 
     * @param myName
     * @return 
     */
    public boolean registerMe(String myName) {

        String myIPAddress = "default";
        String duplicateError = "duplicate entry";
        String timestamp = new Timestamp(new java.util.Date().getTime()).toString().replace(" ", "_");

        String outputString = getQueryResult(COMM_SERVER + "register_member.php?" + COMM_CREDENTIALS + "&pseudo=" + myName + "&ipaddress=" + myIPAddress + "&timestamp=" + timestamp);

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
        LOG.log(Level.INFO, "Registered " + myName + " on the Ancestris server.");
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
        LOG.log(Level.INFO, "Unregistered " + myName + " from the Ancestris server.");
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
            String xmlTagAccess = "ipaddress";
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
                    String pseudo = StringEscapeUtils.unescapeHtml(member.getElementsByTagName(xmlTagPseudo).item(0).getTextContent()); 
                    String ipaddress = member.getElementsByTagName(xmlTagAccess).item(0).getTextContent();
                    ancestrisMembers.add(new AncestrisMember(pseudo, ipaddress));
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
        
        ancestrisMembers.add(new AncestrisMember("Tester", "93.0.227.55"));
//        ancestrisMembers.add(new AncestrisMember("Daniel", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Yannick", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Dominique", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Valérie", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Jeannot", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("FrançoiS", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Ben", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Patrice", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Monique", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Frédéric", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Rodolphe", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Agnès", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Eric", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Mathilde", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Christophe", "xxxx"));
//        ancestrisMembers.add(new AncestrisMember("Guillemette", "xxxx"));
        
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

        listeningThread = new Thread() {
            @Override
            public void run() {
                listen();
            }
        };
        LOG.log(Level.INFO, "Start listening to incoming calls");
        listeningThread.setName("TreeSharing thread : wait for Ancestris connection");
        listeningThread.start();
        
        return true;
    }


    /**
     * Closes door stopping friends from listening to me
     * 
     * @return 
     */
    public boolean stopListeningtoFriends() {
        stopRun = true;
        listeningThread.interrupt();
        LOG.log(Level.INFO, "Stopped listening to incoming calls");
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

    
    
    /**
     * Listens to incoming calls
     * 
     * Depending on calling person, call type, should return something or something else...
     * If ok to return something, get info to return from owner.provideMySharedEntitiesToMember()
     * 
     */
    public void listen() {
        
        BufferedReader in = null;
        ObjectOutputStream objectOutputStream = null;
        
        try {
            ServerSocket serversocket = new ServerSocket(COMM_PORT);        
            while (!stopRun) {
                // Create server socket and wait
                LOG.log(Level.INFO, "...Opening server socket " + serversocket.toString());
                Socket socket = serversocket.accept();
                LOG.log(Level.INFO, "...Server socket accepting a socket " + socket.toString());

                // Connection is made, get input and output streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                
                // Get first line which should contain pseudo
                String pseudo = in.readLine();
                
                // If pseudo accepted, send data
                if (owner.isAllowedMember(pseudo)) {                                                // replace with always true to self-test
                    LOG.log(Level.INFO, "...Member " + pseudo + " is allowed. Sending data.");
                    for (FriendGedcomEntity item : owner.getMySharedEntities()) {
                        objectOutputStream.writeObject(item);
                    }
                } else {
                    LOG.log(Level.INFO, "...Member " + pseudo + " is NOT allowed. Nothing sent.");
                }
                
            // Close socket
            objectOutputStream.close();
            in.close();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    
    
    
    /**
     * Calls friend and expect something in return
     * 
     * @param friend 
     */
    public List<FriendGedcomEntity> call(AncestrisMember member) {

        List<FriendGedcomEntity> list = new ArrayList<FriendGedcomEntity>();

        PrintWriter out = null;
        ObjectInputStream objectInputStream = null;
        
        try {
            // Create socket
            InetAddress addr = InetAddress.getByName(member.getAccess());
            LOG.log(Level.INFO, "Calling member '" + member.getMemberName() + "' at IP address " + addr.getCanonicalHostName());
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(addr, COMM_PORT), COMM_TIMEOUT);       // replace addr with "localhost" to do self-test
            LOG.log(Level.INFO, "...Calling socket created " + socket.toString());
            
            // First send my pseudo
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(owner.getPreferredPseudo());

            // Then get data
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            while (true) {
                FriendGedcomEntity item = (FriendGedcomEntity) objectInputStream.readObject();
                list.add(item);
            }
        } catch (EOFException eofException) {
        } catch (SocketTimeoutException stex) {
            LOG.log(Level.INFO, "...Socket timeout");
            return null;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            // Close socket
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (out != null) {
                out.close();
            }
        }
        
        LOG.log(Level.INFO, "Returned from call to member " + member.getMemberName() + " with " + list.size() + " entities");
        return list;
    }

    
}
