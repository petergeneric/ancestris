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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
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
 * Communication protocol between server and other clients
 * -------------------------------------------------------
 * 
 * Principle : 
 *  - use one single socket for all connected comms on the client side
 *  - first 5 bytes of message is the command 
 * 
 * 
 * Server
 * • receive from client
 *      ∘ REGIS - register pseudo
 *      ∘ UNREG - unregister pseudo (check incoming ip)
 *      ∘ THERE - I am there
 * • send to client
 *      ∘ REGOK - registration ok
 *      ∘ REGKO - registration ko
 *      ∘ UNROK - unregistration ok
 *      ∘ UNRKO - unregistration ko
 *      ∘ RUTHE - are you there ?
 * 
 * Client
 *  • send to server
 *      ∘ REGIS - register pseudo
 *      ∘ UNREG - unregister pseudo
 *      ∘ THERE - I am there
 * • send to client
 *      ∘ TAKSE - take my shared entities
 * • receive from server (check incoming IP)
 *      ∘ REGOK - registration ok
 *      ∘ REGKO - registration ko
 *      ∘ UNROK - unregistration ok
 *      ∘ UNRKO - unregistration ko
 *      ∘ RUTHE - are you there ?
 * • receive from client (check incoming IP is allowed)
 *      ∘ GETSE - get me your shared entities
 *  
 *  • client also queries server for list of registered pseudos and there connection details
 *      ∘ get_members.php as a web service
 *  
 */


/**
 *
 * @author frederic
 */
public class Comm {

    private TreeSharingTopComponent owner;
    
    private final static Logger LOG = Logger.getLogger("ancestris.modules.treesharing.communication");

    private static String COMM_SERVER = "vps187192.ovh.net";                                // for all comms
    private int COMM_PORT = 4584;                                                           // for all connected comms
    private static String COMM_CHARSET = "UTF-8";                                           // for packet exchange
    private static String COMM_PROTOCOL = "http://";                                        // for sql web service only
    private static String COMM_CREDENTIALS = "user=ancestrishare&pw=2fQB&format=xml";       // for sql web service only
    private int COMM_TIMEOUT = 1000; // One second

    private DatagramSocket socket = null;
    private int COMM_PACKET_SIZE = 65536;

    // Commands
    // Get members web service
    private static String CMD_GETMB = "/get_members.php?";
    private static String TAG_MEMBER = "member";
    private static String TAG_PSEUDO = "pseudo";
    private static String TAG_IPADDR = "ipaddress";
    private static String TAG_PORTAD = "portaddress";
    // Registration
    private static String CMD_REGIS = "REGIS";
    private static String CMD_REGOK = "REGOK";
    private static String CMD_REGKO = "REGKO";
    // Unregistration
    private static String CMD_UNREG = "UNREG";
    private static String CMD_UNROK = "UNROK";
    private static String CMD_UNRKO = "UNRKO";
    // Checking I a still there
    private static String CMD_RUTHE = "RUTHE";
    private static String CMD_THERE = "THERE";
    private static String CMD_CLOSE = "CLOSE";
    // Sharing my shared entities
    private static String CMD_GETSE = "GETSE";
    private static String CMD_TAKSE = "TAKSE";
    
    // Threads
    private volatile boolean stopRun;
    private Thread listeningThread;

    
    
    /**
     * Constructor
     */
    public Comm(TreeSharingTopComponent tstc) {
        this.owner = tstc;
    }

    
    
    /**
     * Identify the list of currently sharing friends from the ancestris server (crypted communication)
     * 
     * @return all Ancestris friends sharing something
     */
    public List<AncestrisMember> getAncestrisMembers() {
        
        List<AncestrisMember> ancestrisMembers = new ArrayList<AncestrisMember>();
        
        String outputString = getQueryResult(COMM_PROTOCOL + COMM_SERVER + CMD_GETMB + COMM_CREDENTIALS);

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(outputString));
            Document doc = db.parse(is);  
            doc.getDocumentElement().normalize();  
            NodeList nodeList = doc.getElementsByTagName(TAG_MEMBER);

            // Collect list of Ancestris friends (registered name and access details)
            for (int temp = 0; temp < nodeList.getLength(); temp++) {
                Node node = nodeList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element member = (Element) node;
                    String pseudo = StringEscapeUtils.unescapeHtml(member.getElementsByTagName(TAG_PSEUDO).item(0).getTextContent()); 
                    String ipAddress = member.getElementsByTagName(TAG_IPADDR).item(0).getTextContent();
                    String portAddress = member.getElementsByTagName(TAG_PORTAD).item(0).getTextContent();
                    ancestrisMembers.add(new AncestrisMember(pseudo, ipAddress, portAddress));
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
                
        // Return list
        return ancestrisMembers;
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
     * Register on Ancestris server that I am ready to share 
     * 
     * @param myName
     * @return 
     */
    public boolean registerMe(String pseudo) {

        LOG.log(Level.INFO, "Registering member " + pseudo + " on Ancestris server.");
        String command = CMD_REGIS + pseudo;
        byte[] bytesSent = command.getBytes(Charset.forName(COMM_CHARSET));
        try {
            // Send registering command
            socket = new DatagramSocket();
            DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(COMM_SERVER), COMM_PORT); 
            socket.send(packetSent);
            // Listen to reply
            byte[] bytesReceived = new byte[512];
            DatagramPacket packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
            socket.setSoTimeout(COMM_TIMEOUT);          // make sure there is a timeout to this
            socket.receive(packetReceived);     
            String reply = StringEscapeUtils.unescapeHtml(new String(bytesReceived));
            LOG.log(Level.INFO, "...Reply from server : " + reply);
            if (reply.substring(0, 5).equals(CMD_REGOK)) {
                LOG.log(Level.INFO, "...Registered " + pseudo + " on the Ancestris server.");
                socket.setSoTimeout(0);
            } else if (reply.substring(0, 5).equals(CMD_REGKO)) {
                String err = reply.substring(5);
                LOG.log(Level.INFO, "...Could not register " + pseudo + " on the Ancestris server. Error : " + err);
                if (err.startsWith("Duplicate entry")) {
                    err = NbBundle.getMessage(Comm.class, "ERR_DuplicatePseudo");
                }
                DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Registration"), err).setMessageType(DialogManager.ERROR_MESSAGE).show();
                return false;
            }
        } catch(SocketTimeoutException e) {
            String err = NbBundle.getMessage(Comm.class, "ERR_ServerNotResponding");
            LOG.log(Level.INFO, "...Could not register " + pseudo + " from the Ancestris server. Error : " + err);
            DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Registration"), err).setMessageType(DialogManager.ERROR_MESSAGE).show();
            return false;
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return false;
        }
        startListeningToFriends();  
        return true;
    }
    

    /**
     * Tell Ancestris server that I am no longer ready to share
     * 
     * @param myName
     * @return 
     */
    public boolean unregisterMe(String pseudo) {

        stopListeningToFriends();  
        LOG.log(Level.INFO, "Unregistering member " + pseudo + " from Ancestris server.");
        String command = CMD_UNREG + pseudo;
        byte[] bytesSent = command.getBytes(Charset.forName(COMM_CHARSET));
        try {
            // Send unrestering command
            DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(COMM_SERVER), COMM_PORT); 
            socket.send(packetSent);
            // Listen to reply
            byte[] bytesReceived = new byte[512];
            DatagramPacket packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
            socket.setSoTimeout(COMM_TIMEOUT);          // make sure there is a timeout to this
            socket.receive(packetReceived);     
            socket.setSoTimeout(0);
            String reply = StringEscapeUtils.unescapeHtml(new String(bytesReceived));
            LOG.log(Level.INFO, "...Reply from server : " + reply);
            if (reply.substring(0, 5).equals(CMD_UNROK)) {
                LOG.log(Level.INFO, "...Unregistered " + pseudo + " from the Ancestris server.");
            } else if (reply.substring(0, 5).equals(CMD_UNRKO)) {
                String err = reply.substring(5);
                LOG.log(Level.INFO, "...Could not unregister " + pseudo + " from the Ancestris server. Error : " + err);
                DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Unregistration"), err).setMessageType(DialogManager.ERROR_MESSAGE).show();
                return false;
            }
        } catch(SocketTimeoutException e) {
            String err = NbBundle.getMessage(Comm.class, "ERR_ServerNotResponding");
            LOG.log(Level.INFO, "...Could not unregister " + pseudo + " from the Ancestris server. Error : " + err);
            DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Unregistration"), err).setMessageType(DialogManager.ERROR_MESSAGE).show();
            return false;
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return false;
        }
        if (socket != null) {
            socket.close();
        }
        return true;
    }
    

    /**
     * Opens door allowing friends to connect to me
     */
    private boolean startListeningToFriends() {
        
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
     */
    private boolean stopListeningToFriends() {
        stopRun = true;
        LOG.log(Level.INFO, "Stopped listening to incoming calls");
        return true;
    }

    
    
    
    
    
    
    
    
    
    

    
    
    
    /**
     * Listens to incoming calls
     */
    public void listen() {
        
        LOG.log(Level.INFO, "...Listening using existing socket");
        try {
            byte[] bytesReceived = new byte[512];
            DatagramPacket packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
            while (!stopRun) {
                // Listen to incoming command
                socket.setSoTimeout(0);
                socket.receive(packetReceived);
                String command = StringEscapeUtils.unescapeHtml(new String(bytesReceived));
                if (command.substring(0, 5).equals(CMD_TAKSE)) {
                    String member = command.substring(5);
                    LOG.log(Level.INFO, "...Incoming message received from " + member + " when listening on " + socket.toString());
                    // If pseudo accepted, send data
                    AncestrisMember aMember = owner.getMember(member);
                    if (aMember.isAllowed() && packetReceived.getAddress().equals(aMember.getIPAddress()) && packetReceived.getPort() == Integer.valueOf(aMember.getPortAddress())) {
                        LOG.log(Level.INFO, "...Member " + member + " is allowed and address matches. Sending data.");
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(COMM_PACKET_SIZE);
                        ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                        os.flush();
                        os.writeObject(owner.getMySharedEntities());  // TODO : will need to compress and encrypt data at some point
                        os.flush();
                        byte[] bytesSent = byteStream.toByteArray();
                        DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, packetReceived.getAddress(), packetReceived.getPort());
                        int byteCount = packetSent.getLength();
                        socket.send(packetSent);
                        os.close();
                        LOG.log(Level.INFO, "...Sent shared entities to " + packetReceived.getAddress() + ":" + packetReceived.getPort() + "(" + byteCount + " bytes)");
                    } else {
                        LOG.log(Level.INFO, "...Member " + member + " is NOT allowed or address does not match pseudo. Nothing sent.");
                        //send KO to member
                    }
                } else if (command.substring(0, 5).equals(CMD_CLOSE)) {
                    LOG.log(Level.INFO, "...Received close listening loop command : " + command + " from " + packetReceived.getAddress() + ":" + packetReceived.getPort());
                } else {
                    LOG.log(Level.INFO, "...Received unknown command : " + command + " from " + packetReceived.getAddress() + ":" + packetReceived.getPort());
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

    }
    
    
    
    
    
    /**
     * Calls friend and expect something in return
     */
    public List<FriendGedcomEntity> call(AncestrisMember member) {

        List<FriendGedcomEntity> list = null;

        LOG.log(Level.INFO, "Calling member " + member.getMemberName());
        String command = CMD_GETSE + owner.getRegisteredPseudo();
        byte[] bytesSent = command.getBytes(Charset.forName(COMM_CHARSET));
        try {
            // Ask member for list of shared entities
            DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(member.getIPAddress()), Integer.valueOf(member.getPortAddress())); 
            socket.send(packetSent);
            // Expect answer back and get shared entities in return
            byte[] bytesReceived = new byte[COMM_PACKET_SIZE];
            DatagramPacket packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
            socket.setSoTimeout(5*COMM_TIMEOUT);          // make sure there is a timeout to this
            socket.receive(packetReceived);
            int byteCount = packetReceived.getLength();
            ByteArrayInputStream byteStream = new ByteArrayInputStream(bytesReceived);
            ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
            list = (List<FriendGedcomEntity>) is.readObject();
            is.close();
        } catch(SocketTimeoutException e) {
            String err = NbBundle.getMessage(Comm.class, "ERR_MemberNotResponding");
            LOG.log(Level.INFO, "...No response from " + member.getMemberName() + ". Error : " + err);
            DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Unregistration"), err).setMessageType(DialogManager.ERROR_MESSAGE).show();
            return null;
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        LOG.log(Level.INFO, "Returned from call to member " + member.getMemberName() + " with " + list.size() + " entities");
        return list;
    }


}




//        try {
//            // Create socket
//            InetAddress addr = InetAddress.getByName(member.getIPAddress());
//            LOG.log(Level.INFO, "Calling member '" + member.getMemberName() + "' at IP address " + addr.getCanonicalHostName());
//            Socket socket = new Socket();
//            socket.connect(new InetSocketAddress(addr, COMM_PORT), COMM_TIMEOUT);       // replace addr with "localhost" to do self-test
//            LOG.log(Level.INFO, "...Calling socket created " + socket.toString());
//            
//            // First send my pseudo
//            out = new PrintWriter(socket.getOutputStream(), true);
//            out.println(owner.getPreferredPseudo());
//
//            // Then get data
//            objectInputStream = new ObjectInputStream(socket.getInputStream());
//            while (true) {
//                FriendGedcomEntity item = (FriendGedcomEntity) objectInputStream.readObject();
//                list.add(item);
//            }
//        } catch (EOFException eofException) {
//        } catch (SocketTimeoutException stex) {
//            LOG.log(Level.INFO, "...Socket timeout");
//            return list;
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (ClassNotFoundException ex) {
//            Exceptions.printStackTrace(ex);
//        } finally {
//            // Close socket
//            if (objectInputStream != null) {
//                try {
//                    objectInputStream.close();
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//            if (out != null) {
//                out.close();
//            }
//        }


//        String myIPAddress = "default";
//        String duplicateError = "duplicate entry";
//        String timestamp = new Timestamp(new java.util.Date().getTime()).toString().replace(" ", "_");
//
//        String outputString = getQueryResult(COMM_SERVER + "register_member.php?" + COMM_CREDENTIALS + "&pseudo=" + myName + "&ipaddress=" + myIPAddress + "&timestamp=" + timestamp);
//
//        if (!outputString.trim().isEmpty()) {
//            String errorMsg = "";
//            if (outputString.toLowerCase().contains(duplicateError)) {
//                errorMsg = NbBundle.getMessage(Comm.class, "ERR_DuplicatePseudo");
//            } else {
//                errorMsg = outputString;
//            }
//            DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Registration"), errorMsg).setMessageType(DialogManager.ERROR_MESSAGE).show();
//            return false;
//        }
//        LOG.log(Level.INFO, "Registered " + myName + " on the Ancestris server.");
//        return true;




//        ancestrisMembers.add(new AncestrisMember("Tester", "93.0.227.55", "45872"));
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
        



//        BufferedReader in = null;
//        ObjectOutputStream objectOutputStream = null;
//        
//        try {
//            serversocket = new ServerSocket(COMM_PORT);  
//            while (!stopRun) {
//                // Create server socket and wait
//                LOG.log(Level.INFO, "...Opening server socket " + serversocket.toString());
//                Socket listeningSocket = serversocket.accept();
//                LOG.log(Level.INFO, "...Server socket accepting a socket " + listeningSocket.toString());
//
//                // Connection is made, get input and output streams
//                in = new BufferedReader(new InputStreamReader(listeningSocket.getInputStream()));
//                objectOutputStream = new ObjectOutputStream(listeningSocket.getOutputStream());
//                
//                // Get first line which should contain pseudo
//                String pseudo = in.readLine();
//                
//                // If pseudo accepted, send data
//                if (owner.isAllowedMember(pseudo)) {                                                // replace with always true to self-test
//                    LOG.log(Level.INFO, "...Member " + pseudo + " is allowed. Sending data.");
//                    for (FriendGedcomEntity item : owner.getMySharedEntities()) {
//                        objectOutputStream.writeObject(item);
//                    }
//                } else {
//                    LOG.log(Level.INFO, "...Member " + pseudo + " is NOT allowed. Nothing sent.");
//                }
//                
//            // Close socket
//            objectOutputStream.close();
//            in.close();
//            }
//        } catch (SocketException ex) {
//            // SocketException generated when socketserver is closed, and it is closed to ensure protection and to be reused and to close thread properly.
//        } catch (Exception ex) {
//            Exceptions.printStackTrace(ex);
//        }


//        String outputString = getQueryResult(COMM_SERVER + "unregister_member.php?" + COMM_CREDENTIALS + "&pseudo=" + myName);
//
//        if (!outputString.trim().isEmpty()) {
//            DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Unregistration"), outputString).setMessageType(DialogManager.ERROR_MESSAGE).show();
//            return false;
//        }
//        LOG.log(Level.INFO, "Unregistered " + myName + " from the Ancestris server.");
//        return true;
