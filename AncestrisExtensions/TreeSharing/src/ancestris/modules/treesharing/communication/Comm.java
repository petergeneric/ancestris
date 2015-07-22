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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
 * • receive from server (check incoming IP)
 *      ∘ REGOK - registration ok
 *      ∘ REGKO - registration ko
 *      ∘ UNROK - unregistration ok
 *      ∘ UNRKO - unregistration ko
 *      ∘ RUTHE - are you there ?
 * • receive from and send to client (check incoming IP is allowed)
 *      ∘ GETSE - get me your shared entities so build them and send them
 *      ∘ TAKSE - take my shared entities because you asked so receive them
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
    
    private static Logger LOG = Logger.getLogger("ancestris.treesharing");

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

    
    // Call info
    private List<FriendGedcomEntity> listOfEntities = null;
    private String expectedCallIPAddress = null;
    private String expectedCallPortAddress = null;
    private boolean expectedCall = false;
    
    /**
     * Constructor
     */
    public Comm(TreeSharingTopComponent tstc) {
        this.owner = tstc;
        //LOG.setResourceBundle(null);
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
            LOG.log(Level.INFO, "...Reply from server : " + reply.substring(0, 5));
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
            LOG.log(Level.INFO, "...Reply from server : " + reply.substring(0, 5));
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
        
        LOG.log(Level.INFO, "...Listening using main socket " + socket.toString());
        try {
            byte[] bytesReceived = new byte[COMM_PACKET_SIZE];
            DatagramPacket packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
            while (!stopRun) {
                // Listen to incoming calls
                socket.setSoTimeout(0);
                socket.receive(packetReceived);
                
                // Identify command
                String command = new String(Arrays.copyOfRange(bytesReceived, 0, 5));        
                
                // Case of CMD_GETSE command (another user asks for my shared entities so send shared entities if allowed)
                if (command.equals(CMD_GETSE)) {
                    String str = new String(bytesReceived).substring(5);
                    String member = StringEscapeUtils.unescapeHtml(str.substring(0, str.indexOf(" ")));
                    LOG.log(Level.INFO, "...Incoming GETSE command received from " + member + " (" + packetReceived.getLength() + " bytes)");
                    // If member allowed and IP address matches, send data
                    AncestrisMember aMember = owner.getMember(member);
                    if (aMember == null) {
                        LOG.log(Level.INFO, "...Member " + member + " is not in the list of members.");
                    }
                    else if (aMember.isAllowed() && packetReceived.getAddress().equals(aMember.getIPAddress()) && packetReceived.getPort() == Integer.valueOf(aMember.getPortAddress())) {
                        LOG.log(Level.INFO, "...Member " + member + " is allowed and address matches. Sending data.");
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(COMM_PACKET_SIZE - 5);
                        byteStream.write(CMD_TAKSE.getBytes()); // start content with command
                        ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                        os.flush();
                        os.writeObject(owner.getMySharedEntities());  // Add object to content. TODO : will need to compress and encrypt data at some point
                        os.flush();
                        byte[] bytesSent = byteStream.toByteArray();  
                        DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, packetReceived.getAddress(), packetReceived.getPort());
                        int byteCount = packetSent.getLength();
                        socket.send(packetSent);
                        os.close();
                        LOG.log(Level.INFO, "...Sent shared entities to " + packetReceived.getAddress() + ":" + packetReceived.getPort() + "(" + byteCount + " bytes)");
                    } else {
                        LOG.log(Level.INFO, "...Member " + member + " is NOT allowed or address does not match pseudo. Nothing sent.");
                        //TODO send KO to member
                    }
                }

                // Case of CMD_TAKSE command (following my GETSE message to another member, he/she returns his/her shared entities. Take them.
                else if (command.equals(CMD_TAKSE)) {
                    LOG.log(Level.INFO, "...Incoming TAKSE command received from " + packetReceived.getAddress() + ":" + packetReceived.getPort() + " (" + packetReceived.getLength() + " bytes)");
                    // Make sure there is a pending call expecting something from the ipaddress and port received
                    if (expectedCall && expectedCallIPAddress != null && expectedCallPortAddress != null
                            && packetReceived.getAddress().equals(expectedCallIPAddress) && packetReceived.getPort() == Integer.valueOf(expectedCallPortAddress)) {
                        listOfEntities = null;
                        ByteArrayInputStream byteStream = new ByteArrayInputStream(Arrays.copyOfRange(bytesReceived, 5, bytesReceived.length-1));
                        LOG.log(Level.INFO, "...DEBUG : bytestream = " + byteStream.toString());
                        ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
                        LOG.log(Level.INFO, "...DEBUG : is = " + is.toString());
                        listOfEntities = (List<FriendGedcomEntity>) is.readObject();
                        LOG.log(Level.INFO, "...DEBUG : list size = " + listOfEntities.size());
                        is.close();
                        }
                    }

                // Case of CMD_CLOSE command (following my unresgistration, server sends a close command)
                else if (command.equals(CMD_CLOSE)) {
                    LOG.log(Level.INFO, "...Incoming CLOSE command received from " + packetReceived.getAddress() + ":" + packetReceived.getPort());
                } 
                
                // Case of other commands
                else {
                    LOG.log(Level.INFO, "...Incoming unknown command : " + command + " received from " + packetReceived.getAddress() + ":" + packetReceived.getPort());
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

        // Init call data
        expectedCallIPAddress = member.getIPAddress();
        expectedCallPortAddress = member.getPortAddress();
        if (listOfEntities == null) {
            listOfEntities = new ArrayList<FriendGedcomEntity>();
        } else {
            listOfEntities.clear();
        }
        
        LOG.log(Level.INFO, "Calling member " + member.getMemberName() + " on " + expectedCallIPAddress + ":" + expectedCallPortAddress);
        String command = CMD_GETSE + owner.getRegisteredPseudo() + " ";   // space is end-delimiter as theire is no space in pseudo
        byte[] bytesSent = command.getBytes(Charset.forName(COMM_CHARSET));
        try {
            // Ask member for list of shared entities
            DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(expectedCallIPAddress), Integer.valueOf(expectedCallPortAddress)); 
            LOG.log(Level.INFO, "...Sending command " + command);
            socket.send(packetSent);
            
            // Expect answer back and get shared entities in return (wait for response from the other thread...)
            expectedCall = true;
            int s = 0;
            while (expectedCall && (s < 10)) {  // set give up time to 10 seconds
                TimeUnit.SECONDS.sleep(1);
                s++;
            }
            if (expectedCall) { // response never came back after 10 seconds, consider it failed
                expectedCall = false;
                LOG.log(Level.INFO, "...No response from " + member.getMemberName() + " after timeout.");
                return null;
            }
            
            // There was a response
            if (listOfEntities == null) { // response happened but with no list
                LOG.log(Level.INFO, "...Returned call from member " + member.getMemberName() + " with no list");
                return null;
            } else if (listOfEntities.isEmpty()) {
                LOG.log(Level.INFO, "...Returned call from member " + member.getMemberName() + " with empty list");
                return listOfEntities;
            }
            
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return null;
        }
        LOG.log(Level.INFO, "Returned call from member " + member.getMemberName() + " with " + listOfEntities.size() + " entities");
        return listOfEntities;
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
