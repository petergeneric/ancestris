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
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


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
 * 
 * Hole punching
 *  • Client A and B both register with both internal and external ip/port addresses
 *      ∘ external : done
 *      ∘ internal : to do (from socket) : send "CMD_REGISMyPseudo a.b.c.d p" to server and save
 * 
 *  • Client A requests connection with Pseudo B: send "CMD_CONCTPseudo" to *server* (connectToMember)
 * 
 *  • Server receives CMD_CONCTPseudo from client A
 *      ∘ Server sends CMD_CONCTPseudo to Client A (server should send addresses to both Me and pseudo but because A and B know eachother, only send a synchro message)
 *      ∘ Server sends CMD_CONCTMyPseudo to Client B
 * 
 *  • Client A receives CMD_CONCTPseudo from server and sends CMD_SYNCHMyPseudo to B, Message is either dropped by B's NAT (if too early) or successful (listen) 
 *  • Client B receives CMD_CONCTMyPseudo from server and sends CMD_SYNCHPseudo to A, Message is either dropped by A's NAT (if too early) or successful (listen) 
 * 
 *  • If msg B goes through, Client A receives CMD_PINGGPseudo from B and replies back to B with CMD_PONGG (listen)
 *  • If msg A goes through, Client B receives CMD_PINGGMyPseudo from A and replies back to A with CMD_PONGG  
 *  • In either cases, A receives either CMD_PONGG from B, or CMD_PINGG from B, Connection is established
 * 
 *  • Client A can send GETSE to client B once connection is established (call : connect, wait for connection flag, then GETSE)
 * .
 */


/**
 *
 * @author frederic
 */
public class Comm {

    private TreeSharingTopComponent owner;
    
    private static final Logger LOG = Logger.getLogger("ancestris.treesharing");

    private static String COMM_SERVER = "vps187192.ovh.net";                                // for all comms
    private int COMM_PORT = 4584;                                                           // for all connected comms
    private static String COMM_CHARSET = "UTF-8";                                           // for packet exchange
    private static String COMM_PROTOCOL = "http://";                                        // for sql web service only
    private static String COMM_CREDENTIALS = "user=ancestrishare&pw=2fQB&format=xml";       // for sql web service only
    private int COMM_TIMEOUT = 1000; // One second

    private static DatagramSocket socket = null;
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
    // Establishing connection
    private static String CMD_CONCT = "CONCT";
    private static String CMD_PINGG = "PINGG";
    private static String CMD_PONGG = "PONGG";
    
    // Threads
    private volatile boolean stopRun;
    private Thread listeningThread;

    // Call info
    private boolean expectedConnection = false;
    private List<FriendGedcomEntity> listOfEntities = null;
    private String expectedCallIPAddress = null;
    private String expectedCallPortAddress = null;
    private boolean expectedCall = false;


    
    
    /**
     * Constructor
     */
    public Comm(TreeSharingTopComponent tstc) {
        this.owner = tstc;
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
        listeningThread.setName("TreeSharing thread : loop to wait for Ancestris connections");
        listeningThread.start();
        LOG.log(Level.INFO, "Start thread listening to incoming calls");
        
        return true;
    }


    /**
     * Closes door stopping friends from listening to me
     */
    private boolean stopListeningToFriends() {
        LOG.log(Level.INFO, "Stopped thread listening to incoming calls");
        return true;
    }

    
    
    /**
     * Identify the list of currently sharing friends from the ancestris server (crypted communication)
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
            
        } catch (Exception ex) {
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
     */
    public boolean registerMe(String pseudo) {

        LOG.log(Level.INFO, "***");
        LOG.log(Level.INFO, "Registering member " + pseudo + " on Ancestris server.");
        try {
            // Create our unique socket
            socket = new DatagramSocket();
            
            // Registers on server
            String command = CMD_REGIS + pseudo;
            byte[] bytesSent = command.getBytes(Charset.forName(COMM_CHARSET));
            DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(COMM_SERVER), COMM_PORT); 
            socket.send(packetSent);

            // Listen to reply
            byte[] bytesReceived = new byte[512];
            DatagramPacket packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
            socket.setSoTimeout(COMM_TIMEOUT);          // make sure there is a timeout to this
            socket.receive(packetReceived);     
            
            // Process reply
            String reply = StringEscapeUtils.unescapeHtml(new String(bytesReceived).split("\0")[0]);  // stop string at null char and convert html escape characters
            if (reply.substring(0, 5).equals(CMD_REGOK)) {
                LOG.log(Level.INFO, "...(REGOK) Registered " + pseudo + " on the Ancestris server.");
                socket.setSoTimeout(0);
            } else if (reply.substring(0, 5).equals(CMD_REGKO)) {
                String err = reply.substring(5);
                LOG.log(Level.INFO, "...(REGKO) Could not register " + pseudo + " on the Ancestris server. Error : " + err);
                if (err.startsWith("Duplicate entry")) {
                    err = NbBundle.getMessage(Comm.class, "ERR_DuplicatePseudo");
                }
                DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Registration"), err).setMessageType(DialogManager.ERROR_MESSAGE).show();
                return false;
            }
            
        } catch(SocketTimeoutException e) {
            String err = NbBundle.getMessage(Comm.class, "ERR_ServerNotResponding");
            LOG.log(Level.INFO, "...(TIMEOUT) Could not register " + pseudo + " from the Ancestris server. Error : " + err);
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
     */
    public boolean unregisterMe(String pseudo) {

        LOG.log(Level.INFO, "Unregistering member " + pseudo + " from Ancestris server.");
        try {
            // Send unrestering command
            String command = CMD_UNREG + pseudo;
            byte[] bytesSent = command.getBytes(Charset.forName(COMM_CHARSET));
            DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(COMM_SERVER), COMM_PORT); 
            socket.send(packetSent);
            
            // Expect answer back (wait for response from the other thread...)
            int s = 0;
            while (!stopRun && (s < 100)) {  // set give up time to 2 seconds
                TimeUnit.MILLISECONDS.sleep(20);
                s++;
            }
            if (!stopRun) { // response never came back after timeout, consider it failed
                String err = NbBundle.getMessage(Comm.class, "ERR_ServerNotResponding");
                LOG.log(Level.INFO, "...(TIMEOUT) Could not unregister " + pseudo + " from the Ancestris server. Error : " + err);
                DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Unregistration"), err).setMessageType(DialogManager.ERROR_MESSAGE).show();
                return false;
            }
            
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return false;
        }
        if (socket != null) {
            socket.close();
        }
        stopListeningToFriends();  
        return true;
    }
    

    
        
    

    private boolean connectToMember(AncestrisMember member) {

        if (socket == null || socket.isClosed()) {
            return false;
        }
        
        try {
            String command = CMD_CONCT + member.getMemberName();
            byte[] bytesSent = command.getBytes(Charset.forName(COMM_CHARSET));
            DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(COMM_SERVER), COMM_PORT);
            LOG.log(Level.INFO, "Connecting to member " + member.getMemberName() + " through server " + packetSent.getSocketAddress());
            socket.send(packetSent);

            // Expect that connection gets established (wait for response from the other thread...)
            expectedConnection = true;
            int s = 0;
            while (expectedConnection && (s < 10)) {  // set give up time to 10 seconds
                TimeUnit.SECONDS.sleep(1);
                s++;
            }
            
            if (expectedConnection) { // response never came back after 10 seconds, consider it failed
                expectedConnection = false;
                LOG.log(Level.INFO, "...(TIMEOUT) No connection to " + member.getMemberName() + " after timeout.");
                return false;
            }
            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        LOG.log(Level.INFO, "...(SUCCESS) Connected successfully to member " + member.getMemberName());
        return true;
    }

    
    
    /**
     * Calls friend and expect something in return
     */
    public List<FriendGedcomEntity> call(AncestrisMember member) {

        if (socket == null || socket.isClosed()) {
            return null;
        }
        
        // Connect to Member
        if (!connectToMember(member)) {
            return null;
        }
        
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
                LOG.log(Level.INFO, "...(TIMEOUT) No response from " + member.getMemberName() + " after timeout.");
                return null;
            }
            
            // There was a response
            if (listOfEntities == null) { // response happened but with no list
                LOG.log(Level.INFO, "...(NULL) Returned call from member " + member.getMemberName() + " with no list");
                return null;
            } else if (listOfEntities.isEmpty()) {
                LOG.log(Level.INFO, "...(EMPTY) Returned call from member " + member.getMemberName() + " with empty list");
                return listOfEntities;
            }
            
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return null;
        }
        LOG.log(Level.INFO, "...(SUCCESS) Returned call from member " + member.getMemberName() + " with " + listOfEntities.size() + " entities");
        return listOfEntities;
    }

    
    

    
    
    /**
     * Listens to incoming calls and process them all
     */
    public void listen() {

        String command = null;
        String senderAddress = null;
        String senderIP = null;
        int senderPort = 0;
        String content = null;
        byte[] bytesReceived = new byte[COMM_PACKET_SIZE];
        DatagramPacket packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
        
        LOG.log(Level.INFO, "Listening to all incoming calls indefinitely.......");

        try {
            while (!stopRun) {
                
                // Listen to incoming calls indefinitely
                socket.setSoTimeout(0);
                socket.receive(packetReceived);
                
                // Identify key elements of call
                senderIP = packetReceived.getAddress().getHostAddress();
                senderPort = packetReceived.getPort();
                senderAddress = senderIP + ":" + senderPort;
                command = new String(Arrays.copyOfRange(bytesReceived, 0, 5));        
                content = new String(bytesReceived).substring(5);
                String[] bits = content.split("\0");
                if (bits.length > 0) {
                    content = bits[0];
                }
                LOG.log(Level.INFO, "...Incoming " + command + " command received from " + senderAddress);

                
                // Case of CMD_CONCT command (server replies back to my connection request or asks me to connect to indicated pseudo)
                if (command.equals(CMD_CONCT)) {
                    String member = StringEscapeUtils.unescapeHtml(content.substring(0, content.indexOf(" ")));
                    LOG.log(Level.INFO, "...Request to connect to " + member);
                    AncestrisMember aMember = owner.getMember(member);
                    if (aMember == null) {
                        LOG.log(Level.INFO, "...Member " + member + " is not in the list of members.");
                    }
                    else if (aMember.isAllowed()) {
                        sendReply(CMD_PINGG + owner.getRegisteredPseudo() + " ", aMember.getIPAddress(), Integer.valueOf(aMember.getPortAddress()));
                        LOG.log(Level.INFO, "...Member " + member + " is allowed. Replied back with PINGG.");
                    } else {
                        LOG.log(Level.INFO, "...Member " + member + " is NOT allowed. No reply.");
                    }
                }
                
                // Case of PING command 
                else if (command.equals(CMD_PINGG)) {
                    sendReply(CMD_PONGG, senderIP, senderPort);
                    LOG.log(Level.INFO, "...Replied back with PONGG.");
                    expectedConnection = false;
                } 
                
                // Case of PONG command 
                else if (command.equals(CMD_PONGG)) {
                    expectedConnection = false;
                } 
                
                // Case of CMD_GETSE command (another user asks for my shared entities so send shared entities if allowed)
                else if (command.equals(CMD_GETSE)) {
                    String member = StringEscapeUtils.unescapeHtml(content.substring(0, content.indexOf(" ")));
                    LOG.log(Level.INFO, "...Request to give my shared entities to " + member);
                    // If member allowed and IP address matches, send data
                    AncestrisMember aMember = owner.getMember(member);
                    if (aMember == null) {
                        LOG.log(Level.INFO, "...Member " + member + " is not in the list of members.");
                    }
                    else if (aMember.isAllowed() && senderAddress.equals(aMember.getIPAddress()+":"+aMember.getPortAddress())) {
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(COMM_PACKET_SIZE - 5);
                        LOG.log(Level.INFO, "......DEBUG GETSE: bytestream = " + byteStream.toString());
                        byteStream.write(CMD_TAKSE.getBytes()); // start content with command
                        LOG.log(Level.INFO, "......DEBUG GETSE: after write");
                        ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                        LOG.log(Level.INFO, "......DEBUG GETSE: os = " + os.toString());
                        os.flush();
                        LOG.log(Level.INFO, "......DEBUG GETSE: after flush1");
                        os.writeObject(owner.getMySharedEntities());  // Add object to content. TODO : will need to compress and encrypt data at some point
                        LOG.log(Level.INFO, "......DEBUG GETSE: after writeObject");
                        os.flush();
                        LOG.log(Level.INFO, "......DEBUG GETSE: after flush2");
                        byte[] bytesSent = byteStream.toByteArray();  
                        LOG.log(Level.INFO, "......DEBUG GETSE: after bytesSent declaration");
                        DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(senderIP), senderPort);
                        int bytesCount = packetSent.getLength();
                        LOG.log(Level.INFO, "......DEBUG GETSE: byteCount = " + bytesCount);
                        LOG.log(Level.INFO, "......DEBUG GETSE: before sendPacket");
                        socket.send(packetSent);
                        LOG.log(Level.INFO, "......DEBUG GETSE: after sendPacket");
                        os.close();
                        LOG.log(Level.INFO, "...Member " + member + " is allowed and address matches. Sent shared entities to " + senderAddress + "(" + bytesCount + " bytes)");
                    } else {
                        sendReply(CMD_TAKSE, senderIP, senderPort);
                        LOG.log(Level.INFO, "...Member " + member + " is NOT allowed or address does not match pseudo. Sent empty content.");
                    }
                }

                // Case of CMD_TAKSE command (following my GETSE message to another member, he/she returns his/her shared entities. Take them.
                else if (command.equals(CMD_TAKSE)) {
                    LOG.log(Level.INFO, "...Packet size is " + packetReceived.getLength() + " bytes");
                    // Make sure there is a pending call expecting something from the ipaddress and port received
                    if (expectedCall && expectedCallIPAddress != null && expectedCallPortAddress != null && senderAddress.equals(expectedCallIPAddress+":"+expectedCallPortAddress)) {
                        listOfEntities = null;
                        if (!content.isEmpty()) {
                            ByteArrayInputStream byteStream = new ByteArrayInputStream(content.getBytes());
                            LOG.log(Level.INFO, "......DEBUG TAKSE: bytestream = " + byteStream.toString());
                            ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
                            LOG.log(Level.INFO, "......DEBUG TAKSE: is = " + is.toString());
                            listOfEntities = (List<FriendGedcomEntity>) is.readObject();
                            LOG.log(Level.INFO, "......DEBUG TAKSE: list size = " + listOfEntities.size());
                            is.close();
                        }
                        expectedCall = false;
                        }
                    }

                // Case of CMD_UNROK command (unregistration worked)
                else if (command.equals(CMD_UNROK)) {
                    stopRun = true;
                } 
                
                // Case of CMD_UNRKO command (unregistration did not work)
                else if (command.equals(CMD_UNRKO)) {
                    String err = new String(bytesReceived).substring(5);
                    LOG.log(Level.INFO, "...Could not unregister " + owner.getRegisteredPseudo() + " from the Ancestris server. Error : " + err);
                    DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Unregistration"), err).setMessageType(DialogManager.ERROR_MESSAGE).show();
                } 
                
                // Case of other commands
                else {
                    // nothing
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    
    
    
    private void sendReply(String reply, String ipAddress, int portAddress) {
        
        byte[] bytesSent = reply.getBytes(Charset.forName(COMM_CHARSET));
        DatagramPacket packetSent;
        try {
            packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(ipAddress), portAddress);
            socket.send(packetSent);
        } catch (UnknownHostException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }
    
    

}

