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
    // Testing connection
    private static String CMD_PINGG = "PINGG";
    private static String CMD_PONGG = "PONGG";
    
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

        LOG.log(Level.INFO, "Registering member " + pseudo + " on Ancestris server.");
        String command = CMD_REGIS + pseudo;
        byte[] bytesSent = command.getBytes(Charset.forName(COMM_CHARSET));
        try {
            // Send registering command
            socket = new DatagramSocket();
            DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(COMM_SERVER), COMM_PORT); 
            LOG.log(Level.INFO, "......DEBUG : register - before sendPacket");
            socket.send(packetSent);
            LOG.log(Level.INFO, "......DEBUG : register - after sendPacket");
            // Listen to reply
            byte[] bytesReceived = new byte[512];
            DatagramPacket packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
            socket.setSoTimeout(COMM_TIMEOUT);          // make sure there is a timeout to this
            LOG.log(Level.INFO, "......DEBUG : register - before receivePacket");
            socket.receive(packetReceived);     
            LOG.log(Level.INFO, "......DEBUG : register - after receivePacket");
            String reply = StringEscapeUtils.unescapeHtml(new String(bytesReceived).split("\0")[0]);  // stop string at null char and convert html escape characters
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
     */
    public boolean unregisterMe(String pseudo) {

        stopListeningToFriends();  
        LOG.log(Level.INFO, "Unregistering member " + pseudo + " from Ancestris server.");
        String command = CMD_UNREG + pseudo;
        byte[] bytesSent = command.getBytes(Charset.forName(COMM_CHARSET));
        try {
            // Send unrestering command
            DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(COMM_SERVER), COMM_PORT); 
            LOG.log(Level.INFO, "......DEBUG : unregister - before sendPacket");
            socket.send(packetSent);
            LOG.log(Level.INFO, "......DEBUG : unregister - after sendPacket");
            // Listen to reply
            byte[] bytesReceived = new byte[512];
            DatagramPacket packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
            socket.setSoTimeout(COMM_TIMEOUT);          // make sure there is a timeout to this
            LOG.log(Level.INFO, "......DEBUG : unregister - before receivePacket");
            socket.receive(packetReceived);     
            LOG.log(Level.INFO, "......DEBUG : unregister - after receivePacket");
            socket.setSoTimeout(0);
            String reply = StringEscapeUtils.unescapeHtml(new String(bytesReceived).split("\0")[0]);  // stop string at null char and convert html escape characters
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
        
        LOG.log(Level.INFO, "...Listening using socket " + socket.toString());
        try {
            byte[] bytesReceived = new byte[COMM_PACKET_SIZE];
            DatagramPacket packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
            while (!stopRun) {
                // Listen to incoming calls
                socket.setSoTimeout(0);
                LOG.log(Level.INFO, "......DEBUG : listen - before receivePacket");
                socket.receive(packetReceived);
                LOG.log(Level.INFO, "......DEBUG : listen - after receivePacket");
                
                // Identify command
                String command = new String(Arrays.copyOfRange(bytesReceived, 0, 5));        
                
                // Case of CMD_GETSE command (another user asks for my shared entities so send shared entities if allowed)
                if (command.equals(CMD_GETSE)) {
                    String str = new String(bytesReceived).substring(5);
                    String member = StringEscapeUtils.unescapeHtml(str.substring(0, str.indexOf(" ")));
                    LOG.log(Level.INFO, "...Incoming GETSE command received from " + member + " (" + packetReceived.getLength() + " bytes)");
                    // If member allowed and IP address matches, send data
                    AncestrisMember aMember = owner.getMember(member);
                    LOG.log(Level.INFO, "......DEBUG : aMember = " + aMember.getMemberName());
                    LOG.log(Level.INFO, "......DEBUG : aMember isAllowed= " + aMember.isAllowed());
                    LOG.log(Level.INFO, "......DEBUG : aMember ipAddress = " + aMember.getIPAddress());
                    LOG.log(Level.INFO, "......DEBUG : ipAddress received = " + packetReceived.getAddress().getHostAddress());
                    LOG.log(Level.INFO, "......DEBUG : ipAddress match = " + packetReceived.getAddress().getHostAddress().equals(aMember.getIPAddress()));
                    LOG.log(Level.INFO, "......DEBUG : aMember portAddress = " + aMember.getPortAddress());
                    LOG.log(Level.INFO, "......DEBUG : portAddress received = " + packetReceived.getPort());
                    LOG.log(Level.INFO, "......DEBUG : portAddress match = " + (packetReceived.getPort() == Integer.valueOf(aMember.getPortAddress())));
                    
                    if (aMember == null) {
                        LOG.log(Level.INFO, "...Member " + member + " is not in the list of members.");
                    }
                    else if (aMember.isAllowed() && packetReceived.getAddress().getHostAddress().equals(aMember.getIPAddress()) && packetReceived.getPort() == Integer.valueOf(aMember.getPortAddress())) {
                        LOG.log(Level.INFO, "...Member " + member + " is allowed and address matches. Sending data.");
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
                        DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, packetReceived.getAddress(), packetReceived.getPort());
                        LOG.log(Level.INFO, "......DEBUG GETSE: packetSent = " + packetSent.toString());
                        int bytesCount = packetSent.getLength();
                        LOG.log(Level.INFO, "......DEBUG GETSE: byteCount = " + bytesCount);
                        LOG.log(Level.INFO, "......DEBUG GETSE: packetSent = " + packetSent);
                        LOG.log(Level.INFO, "......DEBUG GETSE: packetSent.getSocketAddress() = " + packetSent.getSocketAddress());
                        LOG.log(Level.INFO, "......DEBUG GETSE: packetSent.getData().length = " + packetSent.getData().length);
                        LOG.log(Level.INFO, "......DEBUG GETSE: before sendPacket");
                        socket.send(packetSent);
                        LOG.log(Level.INFO, "......DEBUG GETSE: after sendPacket");
                        LOG.log(Level.INFO, "......DEBUG GETSE: after socket send packet");
                        os.close();
                        LOG.log(Level.INFO, "...Sent shared entities to " + packetReceived.getAddress().getHostAddress() + ":" + packetReceived.getPort() + "(" + bytesCount + " bytes)");
                    } else {
                        LOG.log(Level.INFO, "...Member " + member + " is NOT allowed or address does not match pseudo. Nothing sent.");
                        //TODO send KO to member
                    }
                }

                // Case of CMD_TAKSE command (following my GETSE message to another member, he/she returns his/her shared entities. Take them.
                else if (command.equals(CMD_TAKSE)) {
                    LOG.log(Level.INFO, "...Incoming TAKSE command received from " + packetReceived.getAddress().getHostAddress() + ":" + packetReceived.getPort() + " (" + packetReceived.getLength() + " bytes)");
                    // Make sure there is a pending call expecting something from the ipaddress and port received
                    if (expectedCall && expectedCallIPAddress != null && expectedCallPortAddress != null
                            && packetReceived.getAddress().getHostAddress().equals(expectedCallIPAddress) && packetReceived.getPort() == Integer.valueOf(expectedCallPortAddress)) {
                        listOfEntities = null;
                        ByteArrayInputStream byteStream = new ByteArrayInputStream(Arrays.copyOfRange(bytesReceived, 5, bytesReceived.length-1)); // TODO : bout de la fin ?????
                        LOG.log(Level.INFO, "......DEBUG TAKSE: bytestream = " + byteStream.toString());
                        ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
                        LOG.log(Level.INFO, "......DEBUG TAKSE: is = " + is.toString());
                        listOfEntities = (List<FriendGedcomEntity>) is.readObject();
                        LOG.log(Level.INFO, "......DEBUG TAKSE: list size = " + listOfEntities.size());
                        is.close();
                        expectedCall = false;
                        }
                    }

                // Case of CMD_CLOSE command (following my unresgistration, server sends a close command)
                else if (command.equals(CMD_CLOSE)) {
                    LOG.log(Level.INFO, "...Incoming CLOSE command received from " + packetReceived.getAddress().getHostAddress() + ":" + packetReceived.getPort());
                } 
                
                // Case of PING commands (debug purpose)
                else if (command.equals(CMD_PINGG)) {
                    LOG.log(Level.INFO, "......DEBUG PINGG: Incoming PINGG command received from " + packetReceived.getAddress().getHostAddress() + ":" + packetReceived.getPort());
                    byte[] bytesSent = CMD_PONGG.getBytes(Charset.forName(COMM_CHARSET));
                    DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, packetReceived.getAddress(), packetReceived.getPort());
                    LOG.log(Level.INFO, "......DEBUG PINGG: packetSent = " + packetSent);
                    LOG.log(Level.INFO, "......DEBUG PINGG: packetSent.getSocketAddress() = " + packetSent.getSocketAddress());
                    LOG.log(Level.INFO, "......DEBUG PINGG: packetSent.getData().length = " + packetSent.getData().length);
                    LOG.log(Level.INFO, "......DEBUG PINGG: before sending PONGG");
                    socket.send(packetSent);
                    LOG.log(Level.INFO, "......DEBUG PINGG: after  sending PONGG");
                } 
                
                // Case of PONG commands (debug purpose)
                else if (command.equals(CMD_PONGG)) {
                    LOG.log(Level.INFO, "...Incoming PONGG command received from " + packetReceived.getAddress().getHostAddress() + ":" + packetReceived.getPort());
                } 
                
                // Case of other commands
                else {
                    LOG.log(Level.INFO, "...Incoming unknown command : " + command + " received from " + packetReceived.getAddress().getHostAddress() + ":" + packetReceived.getPort());
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
        
        LOG.log(Level.INFO, "Calling member " + member.getMemberName() + " on " + expectedCallIPAddress + ":" + expectedCallPortAddress + " using socket " + socket.toString());
        String command = CMD_GETSE + owner.getRegisteredPseudo() + " ";   // space is end-delimiter as theire is no space in pseudo
        byte[] bytesSent = command.getBytes(Charset.forName(COMM_CHARSET));
        try {
            // Ask member for list of shared entities
            DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(expectedCallIPAddress), Integer.valueOf(expectedCallPortAddress)); 
            LOG.log(Level.INFO, "......DEBUG CALL: packetSent = " + packetSent);
            LOG.log(Level.INFO, "......DEBUG CALL: packetSent.getSocketAddress() = " + packetSent.getSocketAddress());
            LOG.log(Level.INFO, "......DEBUG CALL: packetSent.getData().length = " + packetSent.getData().length);
            LOG.log(Level.INFO, "...Sending command " + command);
            socket = new DatagramSocket();
            LOG.log(Level.INFO, "......DEBUG CALL: before sendPacket using socket " + socket.toString());
            socket.send(packetSent);
            LOG.log(Level.INFO, "......DEBUG CALL: after sendPacket");
            
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
                socket.close();
                return null;
            }
            
            // There was a response
            if (listOfEntities == null) { // response happened but with no list
                LOG.log(Level.INFO, "...Returned call from member " + member.getMemberName() + " with no list");
                return null;
            } else if (listOfEntities.isEmpty()) {
                LOG.log(Level.INFO, "...Returned call from member " + member.getMemberName() + " with empty list");
                socket.close();
                return listOfEntities;
            }
            
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            if (socket != null) {
                socket.close();
            }
            return null;
        }
        socket.close();
        LOG.log(Level.INFO, "Returned call from member " + member.getMemberName() + " with " + listOfEntities.size() + " entities");
        return listOfEntities;
    }

    
    public void ping(AncestrisMember member) {

        if (socket == null || socket.isClosed()) {
            return;
        }
        
        try {
            LOG.log(Level.INFO, "Pinging member " + member.getMemberName() + " using socket " + socket.toString());
            byte[] bytesSent = CMD_PINGG.getBytes(Charset.forName(COMM_CHARSET));
            DatagramPacket packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(member.getIPAddress()), Integer.valueOf(member.getPortAddress()));
            LOG.log(Level.INFO, "......DEBUG PING: packetSent = " + packetSent);
            LOG.log(Level.INFO, "......DEBUG PING: packetSent.getSocketAddress() = " + packetSent.getSocketAddress());
            LOG.log(Level.INFO, "......DEBUG PING: packetSent.getData().length = " + packetSent.getData().length);
            socket = new DatagramSocket();
            LOG.log(Level.INFO, "......DEBUG PING: before sendPacket using socket " + socket.toString());
            socket.send(packetSent);
            LOG.log(Level.INFO, "......DEBUG PING: after sendPacket");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }


}

