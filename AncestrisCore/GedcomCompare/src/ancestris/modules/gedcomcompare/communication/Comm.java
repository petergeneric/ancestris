/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015-2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.gedcomcompare.communication;

import ancestris.modules.gedcomcompare.GedcomCompareTopComponent;
import ancestris.modules.gedcomcompare.tools.ConnectedUserFrame;
import ancestris.modules.gedcomcompare.tools.STMapCapsule;
import ancestris.modules.gedcomcompare.tools.STMapEventsCapsule;
import ancestris.usage.Constants;
import ancestris.util.swing.DialogManager;
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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.SwingUtilities;
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
 *  - sent command are all "command|pseudo+space|object"
 * 
 * 
 * Server
 * • receive from client
 *      ∘ REGIS - register pseudo
 *      ∘ UNREG - unregister pseudo (check incoming ip)
 * • send to client
 *      ∘ REGOK - registration ok
 *      ∘ REGKO - registration ko
 *      ∘ UNROK - unregistration ok
 *      ∘ UNRKO - unregistration ko
 * 
 * Client
 *  • send to server
 *      ∘ REGIS - register pseudo
 *      ∘ UNREG - unregister pseudo
 * • receive from server (check incoming IP)
 *      ∘ REGOK - registration ok
 *      ∘ REGKO - registration ko
 *      ∘ UNROK - unregistration ok
 *      ∘ UNRKO - unregistration ko
 * • receive from and send to client (check incoming IP is allowed)
 *      ∘ Gxxxx - get me your shared entities so build them and send them
 *      ∘ Txxxx - take my shared entities because you asked so receive them
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
 * 
 */


/**
 *
 * @author frederic
 */
public class Comm implements Constants {

    private GedcomCompareTopComponent owner;
    
    private static final Logger LOG = Logger.getLogger("ancestris.gedcomcompare");

    private int COMM_PORT = 5448;                                                           // for all connected comms
    private static String COMM_CHARSET = "UTF-8";                                           // for packet exchange
    private int COMM_TIMEOUT = 1000; // One second
    private boolean isCommError = false;                                                    // true if a communicaiton error exists
    private String serverIP = "";
    public static int PING_DELAY = 60;   // seconds to maintain socket with server (port hole closes after 180 sec ; reduced to 60 in 01/2021 in case that delay has reduced.

    private static DatagramSocket socket = null;

    // Get users web service
    public static int COMM_PREF = 8;
    public static int COMM_NBST = 10;
    private static String CMD_GETMB = "/compare-get-users.php?";
    private static String TAG_MEMBER = "user";
    private static String TAG_PSEUDO = "pseudo";
    private static String TAG_IPADDR = "ipaddress";
    private static String TAG_PORTAD = "portaddress";
    private static String TAG_PIPADD = "pipaddress";
    private static String TAG_PPORTA = "pportaddress";
    private static String TAG_PRIVAT = "private";
    private static String TAG_NBINDS = "NbIndis";
    private static String TAG_NBFAMS = "NbFams";
    private static String TAG_NBSTS = "NbSTs";
    private static String TAG_NBEVS = "NbEvens";
    public static String[] TAG_STS = { "ST01", "ST02", "ST03", "ST04", "ST05", "ST06", "ST07", "ST08", "ST09", "ST10" };
    private static String TAG_STATS_OVER = "stats_overlap";
    private static String TAG_STATS_CINA = "stats_citynames";
    private static String TAG_STATS_EVEN = "stats_events";

    // Command and Packets size
    private int COMM_PACKET_SIZE = 1400;   // max size of UDP packet seems to be 16384 (on my box), sometimes 8192 (on François' box for instance)
                    // Here it says 1400 : https://stackoverflow.com/questions/9203403/java-datagrampacket-udp-maximum-send-recv-buffer-size
    private double COMM_COMPRESSING_FACTOR = 3;   // estimated maximum compressing factor of GZIP in order to calculate the size under the above limit
    private String FMT_IDX = "%03d"; // size 3
    private int COMM_CMD_PFX_SIZE = 2;
    private int COMM_CMD_SIZE = 5;    // = 2 + size 3 (changes here means changing on the server as well)
    private int COMM_PACKET_NB = 1000;
    private static String STR_DELIMITER = " ";
    private int REQUEST_TIMEOUT = 3;        // wait for that many seconds before calling timout on each packet
    private int COMM_NB_FAILS = 4;          // give up after this nb of "no response"
    private int COMM_RESPONSE_DELAY = 50;   // in milliseconds for the waiting loop
    private boolean isBusy = false;

    // Commands
    // Registration on server
    private static String CMD_REGIS = "REGIS";
    private static String CMD_REGOK = "REGOK";
    private static String CMD_REGKO = "REGKO";
    // Unregistration from server
    private static String CMD_UNREG = "UNREG";
    private static String CMD_UNROK = "UNROK";
    private static String CMD_UNRKO = "UNRKO";
    // Establishing connection 
    private static String CMD_CONCT = "CONCT";
    private static String CMD_PINGG = "PINGG";
    private static String CMD_PONGG = "PONGG";
    // Sending statistics
    private static String CMD_STATS = "STATS";

    
    // Exchanging data
    private static String CMD_GMCxx = "GM";   // Get map capsule
    private static String CMD_TMCxx = "TM";   // Take map capsule
    private static String CMD_GMExx = "GE";   // Get map events capsule
    private static String CMD_TMExx = "TE";   // Take map events capsule
    private static String CMD_GPFxx = "GP";   // Get profile
    private static String CMD_TPFxx = "TP";   // Take profile
    
    // Possible data objects to be received and sent
    private CommStreamObject csoMapCapsule = new CommStreamObject();
    private CommStreamObject csoMapEventsCapsule = new CommStreamObject();
    private CommStreamObject csoProfile = new CommStreamObject();

    // Threads
    private volatile boolean sharing;
    private Thread listeningThread;
    private Thread pingingThread;

    // Call info
    private boolean communicationInProgress = false;
    private ConnectedUserFrame userInProgress = null;
    private boolean expectMoreResponse = false;
    private Set<ExpectedResponse> expectedResponses = null;




    /**
     * Constructor
     */
    public Comm(GedcomCompareTopComponent tstc) {
        this.owner = tstc;
    }



    /**
     * User connections and registration functions *************************************************************************************************************
     */

    /**
     * Identify the list of currently sharing friends from the ancestris server
     */
    public List<User> getConnectedUsers(boolean quiet) {
        
        List<User> users = new ArrayList<>();
        
        String outputString = getQueryResult(COMM_PROTOCOL + COMM_SERVER + CMD_GETMB + COMM_CREDENTIALS, quiet);
        if (outputString == null) {
            return null;
        }
        if (outputString.isEmpty()) {
            return users;
        }
        
        NodeList nodeList = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(outputString));
            Document doc = db.parse(is);  
            doc.getDocumentElement().normalize();  
            nodeList = doc.getElementsByTagName(TAG_MEMBER);
        } catch (Exception ex) {
            displayErrorMessage("ERR_ParsingConnectedUsers", null, "getConnectedUsers", ex, true);
        }

        // Collect list of Ancestris friends (registered name and access details)
        for (int temp = 0; nodeList != null && temp < nodeList.getLength(); temp++) {
            Node node = nodeList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element member = (Element) node;
                UserProfile userProfile = new UserProfile();
                userProfile.pseudo = StringEscapeUtils.unescapeHtml(member.getElementsByTagName(TAG_PSEUDO).item(0).getTextContent());
                userProfile.ipAddress = member.getElementsByTagName(TAG_IPADDR).item(0).getTextContent();
                userProfile.portAddress = member.getElementsByTagName(TAG_PORTAD).item(0).getTextContent();
                userProfile.pipAddress = member.getElementsByTagName(TAG_PIPADD).item(0).getTextContent();
                userProfile.pportAddress = member.getElementsByTagName(TAG_PPORTA).item(0).getTextContent();
                userProfile.usePrivateIP = false;
                userProfile.privacy = member.getElementsByTagName(TAG_PRIVAT).item(0).getTextContent();
                String f_NbIndis = member.getElementsByTagName(TAG_NBINDS).item(0).getTextContent();
                String f_NbFamilies = member.getElementsByTagName(TAG_NBFAMS).item(0).getTextContent();
                String f_NbSTs = member.getElementsByTagName(TAG_NBSTS).item(0).getTextContent();
                String f_NbEvens = member.getElementsByTagName(TAG_NBEVS).item(0).getTextContent();
                String[] f_STs = new String[TAG_STS.length];
                for (int i = 0; i < TAG_STS.length; i++) {
                    f_STs[i] = member.getElementsByTagName(TAG_STS[i]).item(0).getTextContent();
                }
                String stats_Over = member.getElementsByTagName(TAG_STATS_OVER).item(0).getTextContent();
                String stats_CiNa = member.getElementsByTagName(TAG_STATS_CINA).item(0).getTextContent();
                String stats_Even = member.getElementsByTagName(TAG_STATS_EVEN).item(0).getTextContent();

                if (!userProfile.pseudo.equals(owner.getPreferredPseudo())) {  // I exclude myself
                    users.add(new User(userProfile, f_NbIndis, f_NbFamilies, f_NbSTs, f_NbEvens, f_STs, stats_Over, stats_CiNa, stats_Even));
                }
            }
        }

       
        // Return list
        return users;
    }
    
    private String getQueryResult(String url, boolean quiet) {

        String ret = "";
        String log = "Connecting to url=[" + url.substring(24, 46) + "]";

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
            isCommError = false;
        } catch (UnknownHostException ex) {
            displayErrorMessage(owner.isSharingOn() ? "ERR_UnknownHostExceptionON" : "ERR_UnknownHostException", log, "getQueryResult", ex, !quiet);
            ret = null;
        } catch (SocketException ex) {
            displayErrorMessage("ERR_SocketException", log, "getQueryResult", ex, !quiet);
            ret = null;
        } catch (IOException ex) {
            displayErrorMessage("ERR_IOException", log, "getQueryResult", ex, !quiet);
            ret = null;
        } catch (Exception ex) {
            displayErrorMessage("ERR_Exception", log, "getQueryResult", ex, !quiet);
            ret = null;
        }

        return ret;
    }



      

    public void resetPrivacy() {
        csoMapEventsCapsule.sentPackets = null;
    }
    
    public void resetMap() {
        csoMapCapsule.sentPackets = null;
        csoMapEventsCapsule.sentPackets = null;
    }
    
    public void resetProfile() {
        csoProfile.sentPackets = null;
    }
    
    public void setCommunicationInProgress(boolean inProgress) {
        communicationInProgress = inProgress;
    }
    
    
    
    /**
     * Register on Ancestris server that I am ready to share and compare 
     */
    public boolean registerMe(String pseudo, String[] data) {

        LOG.log(Level.FINE, "***");
        LOG.log(Level.FINE, "Communication packet size is "+COMM_PACKET_SIZE);
        isCommError = false;

        try {
            // Create our unique socket
            socket = new DatagramSocket();
            
            // Registers on server
            
            String content = pseudo + " " + getLocalHostLANAddress().getHostAddress() + " " + socket.getLocalPort() + " " + String.join(" ", data);
            boolean ret = sendCommand(CMD_REGIS, content, null, COMM_SERVER, COMM_PORT);
            if (!ret) {
                return false;
            }

            // Listen to reply
            byte[] bytesReceived = new byte[512];
            DatagramPacket packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
            socket.setSoTimeout(COMM_TIMEOUT);          // make sure there is a timeout to this
            socket.receive(packetReceived);     
            
            // Process reply
            serverIP = packetReceived.getAddress().getHostAddress();
            String reply = StringEscapeUtils.unescapeHtml(new String(bytesReceived).split("\0")[0]);  // stop string at null char and convert html escape characters
            if (reply.substring(0, COMM_CMD_SIZE).equals(CMD_REGOK)) {
                LOG.log(Level.FINE, "...(REGOK) Registered " + pseudo + " on the Ancestris server.");
                owner.getConsole().println("\n\n", false);
                owner.getConsole().println(NbBundle.getMessage(getClass(), "MSG_SharingModeIsON", pseudo), true);
                socket.setSoTimeout(0);
            } else if (reply.substring(0, COMM_CMD_SIZE).equals(CMD_REGKO)) {
                String err = reply.substring(COMM_CMD_SIZE);
                LOG.log(Level.FINE, "...(REGKO) Could not register " + pseudo + " on the Ancestris server. Error : " + err);
                if (err.startsWith("Duplicate entry")) {
                    err = NbBundle.getMessage(Comm.class, "ERR_DuplicatePseudo");
                }
                DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Registration"), err).setMessageType(DialogManager.WARNING_MESSAGE).show();
                return false;
            }
            
        } catch (IOException ex) {
            String log = "...(TIMEOUT) Could not register " + pseudo + " on the Ancestris server.";
            displayErrorMessage("ERR_SharingON", log, "registerMe", ex, true);
            return false;
        }
        startListeningToFriends();  
        
        return true;
    }
    
    /**
     * Tell Ancestris server that I am no longer ready to share
     */
    public boolean unregisterMe(String pseudo) {

        String log = "...(TIMEOUT) Could not unregister " + pseudo + " from the Ancestris server.";
        try {
            // Send unrestering command
            boolean ret = sendCommand(CMD_UNREG, pseudo, null, COMM_SERVER, COMM_PORT);
            if (!ret) {
                stopListeningToFriends();  
                return false;
            }
            
            // Expect answer back (wait for response from the other thread...)
            int s = 0;
            while (sharing && (s < 200)) {  // set give up time
                TimeUnit.MILLISECONDS.sleep(20);
                s++;
            }
            if (sharing) { // response never came back after timeout, consider it failed
                displayErrorMessage("ERR_ServerNotResponding", log, "unregisterMe", null, true);
                stopListeningToFriends();  
                return false;
            }
            
        } catch (Exception ex) {
            displayErrorMessage("ERR_SharingOFF", log, "unregisterMe", ex, true);
            stopListeningToFriends();  
            return false;
        }
        if (socket != null) {
            socket.close();
        }
        LOG.log(Level.FINE, "...(UNREGOK) Unregistered " + pseudo + " from the Ancestris server.");
        owner.getConsole().println(NbBundle.getMessage(getClass(), "MSG_SharingModeIsOFF", pseudo) + "\n\n", true);
        stopListeningToFriends();  
        return true;
    }
    

    



    
    /**
     * High level listening loop *************************************************************************************************************
     */

    
    /**
     * Opens door allowing friends to connect to me
     */
    private boolean startListeningToFriends() {
        
        sharing = true;

        listeningThread = new Thread() {
            @Override
            public void run() {
                listen();
            }
        };
        listeningThread.setName("GedcomCompare thread : loop to wait for Ancestris connections");
        listeningThread.start();
        
        pingingThread = new Thread() {
            @Override
            public void run() {
                ping();
            }
        };

        pingingThread.setName("GedcomCompare thread : loop to stay alive with server");
        pingingThread.start();

        return true;
    }


    

    /**
     * Calls the server every PING_DELAY seconds (hole lasts 180 seconds in general)
     */
    private void ping() {

        while (sharing) {
            sendPing();
            try {
                TimeUnit.SECONDS.sleep(PING_DELAY);
            } catch (InterruptedException ex) {
                displayErrorMessage("ERR_InterruptedException", null, "ping", ex, false);
            }
        }

    }
  
    public void sendPing() {

        if (sharing) {
            sendCommand(CMD_PONGG, owner.getRegisteredPseudo(false), null, COMM_SERVER, COMM_PORT);
        }

    }
    
    
    /**
     * Closes door stopping friends from listening to me
     */
    private boolean stopListeningToFriends() {
        sharing = false;
        LOG.log(Level.INFO, "Stopped thread listening to incoming calls");
        return true;
    }


    
    




    /**
     * High level messaging *************************************************************************************************************
     */

    /**
     * Exchange packages
     */

    public boolean getMapCapsule(STMapCapsule myMap, ConnectedUserFrame user) {

        boolean ret = false;
        
        owner.updateConnectedUsers(true);
        csoMapCapsule.setPackets(buildPacketsOfObject(myMap));
        if (putPackets(user, CMD_TMCxx, csoMapCapsule.getPackets())) {
            communicationInProgress = true;
            csoMapCapsule.reset();
            ret = getPackets(user, CMD_GMCxx, null); // packets will come back in userMapCapsuleStream and update user automatically
        }
        communicationInProgress = false;
        return ret;
    }

        
    public boolean getMapEventsCapsule(STMapEventsCapsule myMapEvents, ConnectedUserFrame user) {

        boolean ret = false;
        
        owner.updateConnectedUsers(true);
        csoMapEventsCapsule.setPackets(buildPacketsOfObject(myMapEvents));
        if (putPackets(user, CMD_TMExx, csoMapEventsCapsule.getPackets())) {
            communicationInProgress = true;
            csoMapEventsCapsule.reset();
            ret = getPackets(user, CMD_GMExx, null); // packets will come back in userMapEventsCapsuleStream and update user automatically
        }
        communicationInProgress = false;
        return ret;
    }

    
    public boolean getUserProfile(UserProfile myProfile, ConnectedUserFrame user) {

        boolean ret = false;
        
        communicationInProgress = true; // getUserProfile ususally follows a previous communication, so set it on to avoid reconnecting users
        csoProfile.setPackets(buildPacketsOfObject(myProfile));
        if (putPackets(user, CMD_TPFxx, csoProfile.getPackets())) {
            communicationInProgress = true;
            csoProfile.reset();
            ret = getPackets(user, CMD_GPFxx, null); // packets will come back in userProfileStream and update user automatically
        }
        communicationInProgress = false;
        return ret;
    }


    
    public void sendStats(String values) {

        if (sharing && !isBusy) { // do no send this command if a receive is being processed
            sendCommand(CMD_STATS, owner.getRegisteredPseudo(false) + " " +values, null, COMM_SERVER, COMM_PORT);
        }

    }
    
    
    
    
        
    /**
     * Low level messaging *************************************************************************************************************
     * 
     * RECEIVE SIDE:
     * => listen = process incoming calls
     * 
     * SEND SIDE:
     * => connect = establish connection with a user
     * => call =  
     * => put  = 
     */

    /**
     * Low level listening loop : listens to incoming calls and process them all
     */
    public void listen() {

        String command = null;
        String sender = "";
        String senderAddress = null;
        String senderIP = null;
        int senderPort = 0;
        byte[] bytesReceived = new byte[COMM_PACKET_SIZE*7];   // receiving packet can be much larger than garanteed size
        DatagramPacket packetReceived;
        
        byte[] contentMemberBytes = null;
        String contentMemberStr = null;
        byte[] contentObj = null;
        String member = null;
        ConnectedUserFrame aMember = null;
        
        LOG.log(Level.FINE, "Listening to all incoming calls indefinitely.......");
        
        // Reset expected responses
        expectedResponses = new HashSet<ExpectedResponse>();

        try {
            while (sharing) {
                
                // Listen to incoming calls indefinitely
                isBusy = false;
                packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
                socket.setSoTimeout(0);
                socket.receive(packetReceived);
                isBusy = true;
                
                // Identify key elements of incoming calls for all calls
                senderIP = packetReceived.getAddress().getHostAddress();
                senderPort = packetReceived.getPort();
                senderAddress = senderIP + ":" + senderPort;
                sender = serverIP.equals(senderIP) ? "Server" : senderIP + ":" + senderPort;
                
                // Identify command
                command = new String(Arrays.copyOfRange(bytesReceived, 0, COMM_CMD_SIZE));        
                
                // Identify member part of bytes until STR_DELIMITER
                contentMemberBytes = extractBytes(Arrays.copyOfRange(bytesReceived, COMM_CMD_SIZE, bytesReceived.length), STR_DELIMITER.getBytes()[0]);
                contentMemberStr = new String(contentMemberBytes);
                member = StringEscapeUtils.unescapeHtml(contentMemberStr);
                
                
                LOG.log(Level.FINE, "...Incoming " + command + " command received with string '" + member + "' from " + sender + " with packet of size ("+ packetReceived.getLength() + " bytes).");

                //
                // PROCESS COMMANDS FROM SERVER
                //
                
                // Case of CMD_UNROK command (unregistration worked)
                if (command.equals(CMD_UNROK)) {
                    sharing = false;
                    continue;
                } 
                
                // Case of CMD_UNRKO command (unregistration did not work)
                if (command.equals(CMD_UNRKO)) {
                    String err = new String(bytesReceived).substring(COMM_CMD_SIZE);
                    LOG.log(Level.FINE, "......Could not unregister " + owner.getRegisteredPseudo(false) + " from the Ancestris server. Error : " + err);
                    DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Unregistration"), err).setMessageType(DialogManager.ERROR_MESSAGE).show();
                    continue;
                } 

                // Case of CMD_CONCT command (server replies back to my connection request or asks me to connect to indicated pseudo)
                if (command.equals(CMD_CONCT)) {
                    owner.updateConnectedUsers(true);
                    aMember = owner.getUser(member);
                    LOG.log(Level.FINE, "......Attempt to connect to member '" + member + "' at " + aMember.getxIPAddress() + ":" + Integer.valueOf(aMember.getxPortAddress()));
                    if (aMember == null) {
                        LOG.log(Level.FINE, "......Member '" + member + "' is not in the list of members.");
                    }
                    else if (aMember.isIncluded()) {
                        // public connection
                        sendCommand(CMD_PINGG, owner.getRegisteredPseudo() + STR_DELIMITER, null, aMember.getxIPAddress(), Integer.valueOf(aMember.getxPortAddress()));
                        // private connection
                        if (!aMember.getpIPAddress().isEmpty() && Integer.valueOf(aMember.getpPortAddress()) != 0) {
                            sendCommand(CMD_PINGG, owner.getRegisteredPseudo() + STR_DELIMITER, null, aMember.getpIPAddress(), Integer.valueOf(aMember.getpPortAddress()));
                        }
                        LOG.log(Level.FINE, "......Member " + member + " is allowed. Connection attempt sent with PINGG to user.");
                    } else {
                        LOG.log(Level.FINE, "......Member " + member + " is NOT allowed. I did not reply.");
                    }
                    continue;
                }
                
                
                //
                // PROCESS CONNECTION COMMANDS WTH OTHER ANCESTRIS USERS
                //
                
                // Identify member elements of getPackets and content. If member not allowed, continue
                String userError = "00";
                aMember = owner.getUser(member);
                if (aMember == null) {
                    owner.updateConnectedUsers(true);
                    aMember = owner.getUser(member);
                }
                if (aMember == null) {
                    userError = "01 User unknown";
                    LOG.log(Level.FINE, "......Calling member '" + member + "' is not in the list of members ("+userError+").");
                } else if (!aMember.isIncluded()) {
                    userError = "02 User not included";
                    LOG.log(Level.FINE, "......Member '" + member + "' is NOT included. Do not reply ("+userError+").");
                } else if (!isSameAddress(senderAddress, aMember)) {
                    userError = "03 User address mismatch";
                    LOG.log(Level.FINE, "......Member '" + member + "' address does not match the one I know. Do not reply ("+userError+").");
                    owner.updateConnectedUsers(true);
                } 

                contentObj = Arrays.copyOfRange(bytesReceived, COMM_CMD_SIZE + contentMemberBytes.length + STR_DELIMITER.length(), bytesReceived.length);
                if (contentObj == null || contentObj.length == 0) {
                    userError = "04 User packet is empty";
                    LOG.log(Level.FINE, "......Member " + member + " has sent an empty packet. Break process ("+userError+").");
                }

                // Case of PING command 
                if (command.equals(CMD_PINGG) || !userError.equals("00")) {
                    String code = userError.substring(0, 2);
                    String msg = NbBundle.getMessage(getClass(), "MSG_ReceivingConnection", member, NbBundle.getMessage(getClass(), "ERR_CODE_"+code));
                    LOG.log(Level.FINE, "......handling PINGG : " + msg);
                    owner.getConsole().println(msg, true);
                    sendCommand(CMD_PONGG, owner.getRegisteredPseudo() + STR_DELIMITER + code + STR_DELIMITER, null, senderIP, senderPort);
                    expectMoreResponse = false;
                    continue;
                } 
                
                // Case of PONG command 
                if (command.equals(CMD_PONGG)) {
                    LOG.log(Level.FINE, "......handling PONGG command from user " + member + ".");
                    String code = new String(bytesReceived).substring(COMM_CMD_SIZE + contentMemberBytes.length + 1, COMM_CMD_SIZE + contentMemberBytes.length + 3).trim();
                    if (code.isEmpty() || !code.startsWith("0")) {    // factor in previous versions which did not had the error code
                        code = "00";
                    }
                    if ("00".equals(code)) {
                        String msg = NbBundle.getMessage(getClass(), "MSG_SuccessConnection", member, code);
                        LOG.log(Level.FINE, "......" + msg);
                        //owner.getConsole().println(msg, true);
                        if (userInProgress == null) {
                            aMember.addConnection();
                        }
                        expectMoreResponse = false;
                    } else {
                        String msg = NbBundle.getMessage(getClass(), "ERR_FailedConnection", member, NbBundle.getMessage(getClass(), "ERR_CODE_"+code));
                        LOG.log(Level.FINE, "......" + msg);
                        owner.getConsole().printError(msg, true);
                        expectMoreResponse = true; // trigger timeout to indicate failure
                }
                    continue;
                } 

                ExpectedResponse response = new ExpectedResponse(aMember, command);
                
                
                //
                // PROCESS EXCHANGES WITH OTHER ANCESTRIS USERS
                //
                
                //********************** Get and receive comm stream objects **********************
                
                // Case of CMD_GMCxx command: user asks me to send the packet i of the map capsule. Send it.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_GMCxx)) {
                    if (csoMapCapsule.isEmpty()) {
                        csoMapCapsule.setPackets(buildPacketsOfObject(owner.getMapCapsule()));
                    }
                    processReceiveCommandGet(command, CMD_GMCxx, CMD_TMCxx, member, senderIP, senderPort, csoMapCapsule);
                    continue;
                }
                
                // Case of CMD_TMCxx command : user asks me to take packet i of a map capsule. Take it.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_TMCxx)) {
                    processReceiveCommandTake(command, CMD_TMCxx, member, csoMapCapsule, contentObj, response);
                    if (csoMapCapsule.isComplete()) {
                        owner.updateUser(aMember, (STMapCapsule) unwrapObject(csoMapCapsule.getStream()));
                    }
                    continue;
                }

                //*************//
                
                // Case of CMD_GMExx command: user asks me to send the packet i of the map capsule. Send it.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_GMExx)) {
                    if (csoMapEventsCapsule.isEmpty()) {
                        csoMapEventsCapsule.setPackets(buildPacketsOfObject(owner.getMapEventsCapsule(aMember)));
                    }
                    processReceiveCommandGet(command, CMD_GMExx, CMD_TMExx, member, senderIP, senderPort, csoMapEventsCapsule);
                    continue;
                }

                // Case of CMD_TMExx command : user asks me to take packet i of a map capsule. Take it.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_TMExx)) {
                    processReceiveCommandTake(command, CMD_TMExx, member, csoMapEventsCapsule, contentObj, response);
                    if (csoMapEventsCapsule.isComplete()) {
                        owner.updateUser(aMember, (STMapEventsCapsule) unwrapObject(csoMapEventsCapsule.getStream()));
                    }
                    continue;
                }


                //*************//
                
                // Case of CMD_GPFxx command: user asks me to send the packet i of the map capsule. Send it.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_GPFxx)) {
                    if (csoProfile.isEmpty()) {
                        csoProfile.setPackets(buildPacketsOfObject(owner.getMyProfile()));
                    }
                    processReceiveCommandGet(command, CMD_GPFxx, CMD_TPFxx, member, senderIP, senderPort, csoProfile);
                    continue;
                }

                // Case of CMD_TPFxx command : user asks me to take packet i of a map capsule. Take it.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_TPFxx)) {
                    processReceiveCommandTake(command, CMD_TPFxx, member, csoProfile, contentObj, response);
                    if (csoProfile.isComplete()) {
                        owner.updateUser(aMember, (UserProfile) unwrapObject(csoProfile.getStream()));
                    }
                    continue;
                }

                
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            displayErrorMessage("ERR_ReceivingMsg", null, "listen", ex, true);
        }

    }


    private void processReceiveCommandGet(String command, String commandGet, String commandBack, String member, String senderIP, int senderPort, CommStreamObject stream) {

        LOG.log(Level.FINE, "......handling " + commandGet + " command from user " + member + ".");
        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
        String commandIndexed = commandBack + String.format(FMT_IDX, iPacket);
        byte[] set = stream.sentPackets.get(iPacket);
        try {
            TimeUnit.MILLISECONDS.sleep(COMM_RESPONSE_DELAY);
        } catch (InterruptedException ex) {
            displayErrorMessage("ERR_ProcessingReceivedMsgGet", null, "processReceiveCommandGet", ex, true);
            return;
        }
        if (set == null) {
            commandIndexed = commandBack + String.format(FMT_IDX, COMM_PACKET_NB - 1);
            sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, null, senderIP, senderPort);
        } else {
            sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, set, senderIP, senderPort);
        }
    }

    private void processReceiveCommandTake(String command, String commandPut, String member, CommStreamObject stream, 
            byte[] contentObj, ExpectedResponse response) {
        
        LOG.log(Level.FINE, "......handling "+ commandPut + " command from user " + member + ".");
        stream.setComplete(false);
        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
        if (iPacket == 0) { // first packet
            stream.init();
            LOG.log(Level.FINE, ".........iPacket=0 : initializing stream " + stream + " and storing packet 0.");
            try {
                stream.write((byte[]) unwrapObject(contentObj));
            } catch (IOException ex) {
                displayErrorMessage("ERR_ProcessingReceivedMsgTake", null, "processReceiveCommandTake1", ex, true);
                return;
            }
        } else if (iPacket == COMM_PACKET_NB - 1) { // last packet : finalize packet and update user content
            LOG.log(Level.FINE, ".........iPacket="+iPacket+" (last) : Closing stream " + stream + " and updating user.");
            stream.setComplete(true);
        } else {
            LOG.log(Level.FINE, ".........iPacket="+iPacket+" : storing packet to stream " + stream + " .");
            try {
                stream.write((byte[]) unwrapObject(contentObj));
            } catch (IOException ex) {
                displayErrorMessage("ERR_ProcessingReceivedMsgTake", null, "processReceiveCommandTake2", ex, true);
                return;
            }
        }

        ExpectedResponse er = getExpectedResponse(response);
        if (er != null) {
            LOG.log(Level.FINE, ".........A response was expected and received, Clear it.");
            expectedResponses.remove(er);
        }

    }

    
    
    /**
     * Extract first part of bytes until a given delimiter
     */
    private byte[] extractBytes(byte[] content, byte delimiter) {
        byte[] ret = null;
        for (int i = 0; i < content.length; i++) {
            byte b = content[i];
            if (b == delimiter || b == 0) {
                ret = Arrays.copyOfRange(content, 0, i);
                return ret;
            }
        }
        ret = content;
        return ret;
    }
    

    
    /**
     * Connect to user via server
    */
    private boolean connectToUser(ConnectedUserFrame user) {
        
        userInProgress = user;
        String log = "Connect to user=" +  user != null ? user.getName() : "null";
        try {
            sendCommand(CMD_CONCT, user.getName(), null, COMM_SERVER, COMM_PORT);

            // Expect that connection gets established (wait for response from the other thread...)
            expectMoreResponse = true;
            int s = 0;
            while (expectMoreResponse && (s < REQUEST_TIMEOUT * 100)) {
                TimeUnit.MILLISECONDS.sleep(10);
                s++;
            }
            
            if (expectMoreResponse) { // response never came back after timeout, consider it failed
                expectMoreResponse = false;
                String msg = NbBundle.getMessage(getClass(), "ERR_UserLeft", user.getName(), REQUEST_TIMEOUT);
                LOG.log(Level.FINE, "...(TIMEOUT) " + msg);
                owner.getConsole().printError(msg, true);
                return false;
            }
            // wait a bit that my generated pings and pongs have been processed
            TimeUnit.MILLISECONDS.sleep(500);
            userInProgress = null;
            
        } catch (Exception ex) {
            displayErrorMessage("ERR_ConnectException", log, "connectToUser", ex, true);
            return false;
        }
        
        String msg = NbBundle.getMessage(getClass(), "MSG_SuccessConnection", user.getName() + " (" + user.getNbIndis() + "/" + user.getNbFams() + ").", "OK");
        LOG.log(Level.FINE, "...(SUCCESS) "+ msg);
        owner.getConsole().println(msg, true);
        return true;
    }

    
    
    

    /**
     * Low level getPackets method to a user to expect something in return which will arrive in the listen loop (object is used by receiver depending on the call to fullfil request)
     */
    public boolean getPackets(ConnectedUserFrame user, String command, Object object) {

        boolean returnStatus = false;
        
        if (socket == null || socket.isClosed()) {
            return returnStatus;
        }
        
        // Connect to user only if no communication in progress
        if (!communicationInProgress && !connectToUser(user)) {
            return returnStatus;
        }
        
        // Loop on packets. Last packet number is COMM_PACKET_NB-1.
        int iPacket = 0;
        boolean retry = true;
        int nbNoResponses = 0; // nb of consecutive no responses
        LOG.log(Level.FINE, "Asking a GET to user " + user.getName() + " with command " + command);
        // Prepare loop to receive a number of packets
        while (iPacket < COMM_PACKET_NB && nbNoResponses < COMM_NB_FAILS) {  // stop at the last packet or after nb consecutive retry/skips
            String commandIndexed = command + String.format(FMT_IDX, iPacket);
            try {
                // Ask user for list of something
                sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, (iPacket == 0 ? object : null), user.getIPAddress(), Integer.valueOf(user.getPortAddress()));
            
                // Expect answer back and collect packets in sequence (wait for response from the other thread...)
                ExpectedResponse exResp = new ExpectedResponse(user, commandIndexed);
                expectedResponses.add(exResp);
                int s = 0;
                while (expectedResponses.contains(exResp) && (s < REQUEST_TIMEOUT*100)) {  
                    TimeUnit.MILLISECONDS.sleep(10);
                    s++;
                }
                if (expectedResponses.contains(exResp)) { // response never came back after timeout, retry once or consider it failed
                    nbNoResponses++;
                    if (retry) {
                        LOG.log(Level.FINE, "...(TIMEOUT) No response from " + user.getName() + " after " + REQUEST_TIMEOUT + "s timeout. Retrying once...");
                        retry = false;
                    } else {
                        LOG.log(Level.FINE, "...(TIMEOUT) No response from " + user.getName() + " after " + REQUEST_TIMEOUT + "s timeout. Skip");
                        retry = true;
                        iPacket++;
                    }
                    continue;
                }
                
                // packet received
                nbNoResponses = 0;
                retry = true;
                iPacket++;
            
                // No more packet
                if (command.equals(CMD_GMCxx) && csoMapCapsule.receivedStreamEOF) {
                    returnStatus = true;
                    break;
                }
                if (command.equals(CMD_GMExx) && csoMapEventsCapsule.receivedStreamEOF) {
                    returnStatus = true;
                    break;
                }
                if (command.equals(CMD_GPFxx) && csoProfile.receivedStreamEOF) {
                    returnStatus = true;
                    break;
                }

            
            } catch (Exception ex) {
                displayErrorMessage("ERR_CallException", "Error getting packets", "getPackets", ex, true);
                return returnStatus;
            }
        }
        LOG.log(Level.FINE, "...(END) Returned call from member " + user.getName() + " after " + iPacket + " packets");
        return returnStatus;
    }

    
    /**
     * High level putPackets method to give the packet to a user 
     */
    public boolean putPackets(ConnectedUserFrame user, String command, Map<Integer, byte[]> packets) {

        boolean returnStatus = false;
        
        if (socket == null || socket.isClosed()) {
            return returnStatus;
        }
        
        // Connect to user only if no communication in progress
        if (!communicationInProgress && !connectToUser(user)) {
            return returnStatus;
        }
        
        // Loop on packets. Last packet number is COMM_PACKET_NB-1.
        String senderIP = user.getIPAddress();
        int senderPort = Integer.valueOf(user.getPortAddress());
        int iPacket = 0;
        LOG.log(Level.FINE, "Asking a PUT to user " + user.getName() + " with command " + command);
        while (iPacket < COMM_PACKET_NB) {  // stop at the last packet 
            String commandIndexed = command + String.format(FMT_IDX, iPacket);
            byte[] set = packets.get(iPacket);
            if (set == null) {
                commandIndexed = command + String.format(FMT_IDX, COMM_PACKET_NB - 1);
                sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, null, senderIP, senderPort);
                returnStatus = true;
                break;
            } else {
                sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, set, senderIP, senderPort);
            }
            iPacket++;
            // wait a bit before sending next packet
            try {
                TimeUnit.MILLISECONDS.sleep(COMM_RESPONSE_DELAY);
            } catch (InterruptedException ex) {
                displayErrorMessage("ERR_PutException", "Error putting packets", "putPackets", ex, true);
                return returnStatus;
            }
        }
        LOG.log(Level.FINE, "...(END) Enf of put to member " + user.getName() + " with " + iPacket + " packets");
        return returnStatus;
    }

    
    
    
    /**
     * Send command to Server and Members
     */
    private boolean sendCommand(String command, String string, Object object, String ipAddress, int portAddress) {
        
        byte[] msgBytes = null; // content to send

        String contentStr = command + string; 
        byte[] contentBytes = contentStr.getBytes(Charset.forName(COMM_CHARSET));
        
        // Return just this if no object else add wrapped object
        if (object == null) {
            msgBytes = contentBytes;
        } else {
            msgBytes = getWrappedObject(contentBytes, object);
        }

        // abort if msgBytes failed
        if (msgBytes == null) {   
            LOG.log(Level.FINE, "Sending command " + command + " using string '" + string + "' => Cannot wrap message. Abort communication.");
            return false;
            }
        
        // log message but no need to log the PONGG message as it is sent every few minutes to the server to show we are alive
        if (!command.equals(CMD_PONGG)) {
            String dest = ipAddress + ":" + portAddress;
            if (COMM_SERVER.equals(ipAddress)) {
                dest = "Server (" + ipAddress + ")";
            }
            LOG.log(Level.FINE, "Sending command " + command + " using string '"+ string + "' and object of size (" + msgBytes.length + " bytes) to " + dest);
            }
        
        // Truncate package if object is too big
        int s = msgBytes.length;
        if (s > COMM_PACKET_SIZE) {
            LOG.log(Level.FINE, "./!\\ Object of size (" + s + " bytes) is larger than maximum packet size of " + COMM_PACKET_SIZE);
            // test class of object
            boolean abort = true;
            if (object instanceof Set) {
                Set<Object> testSet = (Set<Object>) object;
                if (testSet.iterator().next() instanceof String) {
                    abort = false;
                }
            }
            // truncate object if object is a set of strings (packet has not been optimised in this case)
            // reduce object size by a factor of factor, and add strings until object reaches maximum size
            if (!abort) {
                Set<String> set = (Set<String>) object;
                Set<String> subSet = new HashSet<String>();  
                int factor = 2 * s / COMM_PACKET_SIZE + 3;  
                int limit = set.size() / factor;
                int index = 0;
                byte[] tmpBytes = null;
                for (String str : set) {
                    subSet.add(str);
                    if (index > limit) {
                        // check packet size and continue adding by lots of 10 strings until it reaches max size
                        tmpBytes = getWrappedObject(contentBytes, subSet);
                        s = tmpBytes.length;
                        if (s < COMM_PACKET_SIZE) {
                            msgBytes = tmpBytes;
                            limit += 10;
                        } else {
                            LOG.log(Level.FINE, ".You are the caller : number of common names has been truncated to first " + (limit - 10) + " names instead of " + set.size() + ".");
                            LOG.log(Level.FINE, ".Packet size is now (" + msgBytes.length + ") bytes.");
                            break;  // use msgBytes
                        }
                    }
                    index++;
                }
            } else {
                LOG.log(Level.FINE, ".You are the receiver : compression factor is currently set to " + COMM_COMPRESSING_FACTOR + " and should be increased by the developpers.");
                LOG.log(Level.FINE, ".=> Abort communication.");
                return false;
            }
        }
        
        // Send msg with object
        return sendObject(msgBytes, ipAddress, portAddress);
    }
    
    
    private byte[] getWrappedObject(byte[] contentBytes, Object object) {
        byte[] ret = null;
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byteStream.write(contentBytes);
            byteStream.write(wrapObject(object));
            ret = byteStream.toByteArray();
        } catch (IOException ex) {
            displayErrorMessage("ERR_WrappedException", null, "getWrappedObject", ex, true);
        }

        return ret;
    }
    

    /**
     * Build packet from an object
     */
    private byte[] wrapObject(Object object) {
        byte[] bytes = null;
        try {
            ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
            GZIPOutputStream gz = new GZIPOutputStream(contentStream);
            ObjectOutputStream os = new ObjectOutputStream(gz);
            os.flush();
            os.writeObject(object);
            os.flush();
            gz.close();
            bytes = contentStream.toByteArray();
            
        } catch (IOException ex) {
            displayErrorMessage("ERR_WrapException", null, "wrapObject", ex, true);
        }
        return bytes;
    }

    /**
     * Detatch object from packet
     */
    private Object unwrapObject(byte[] content) {

        Object object = null;
        if (content == null || content.length == 0) {
            return null;
        }
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(content);
            ObjectInputStream is = new ObjectInputStream(new GZIPInputStream(byteStream));
            object = is.readObject();
            is.close();
        } catch (Exception ex) {
            String log = "Receiving message. Packet size was probably larger than the maximum packet size and therefore has been truncated, or packets have different sizes between the sender and the receiver. Please update your version of Ancestris or contact the Ancestris support.";
            displayErrorMessage("ERR_UnwrapException", log, "unwrapObject", ex, true);
        }
        return object;
    }

    /**
     * Elementary method to send object once packet has been built
     */
    private boolean sendObject(byte[] bytesSent, String ipAddress, int portAddress) {
        DatagramPacket packetSent;
        try {
            packetSent = new DatagramPacket(bytesSent, bytesSent.length, InetAddress.getByName(ipAddress), portAddress);
            socket.send(packetSent);
        } catch (IOException ex) {
            displayErrorMessage("ERR_UnknownHostException", "Could not send object to "+ipAddress+":"+portAddress, "sendObject", ex, true);
            return false;
        }
        return true;
    }
    

    
    /**
     * Builds packets from any object
     */
    private Map<Integer, byte[]> buildPacketsOfObject(Object capsule) {
        Map<Integer, byte[]> packets = new HashMap<>();
        byte[] masterPacket = wrapObject(capsule);
        int nbResized = 1024;// make small packets to make sure it goes through
        int nbPackets = (int) (Math.min(COMM_PACKET_NB, masterPacket.length / nbResized) + 1);   
        for (Integer i = 0; i < nbPackets; i++) {
            if (i < nbPackets-1) {
                packets.put(i, Arrays.copyOfRange(masterPacket, i*nbResized, (i+1)*nbResized));
            } else {
                packets.put(i, Arrays.copyOfRange(masterPacket, i*nbResized, masterPacket.length));
            }
        }
        return packets;
    }


    /**
     * 
     * @param response
     * @return 
     * - item with exact same member/command if exists
     * - only remaining item if response includes 999 
     */
    private ExpectedResponse getExpectedResponse(ExpectedResponse response) {

        boolean sameMember = false;
        
        for (Comm.ExpectedResponse er : expectedResponses) {
            sameMember = (response.fromMember.getName().equals(er.fromMember.getName())
                       && response.fromMember.getxIPAddress().equals(er.fromMember.getxIPAddress())
                       && response.fromMember.getxPortAddress().equals(er.fromMember.getxPortAddress()) );
            if (sameMember && (response.forCommand.equals(er.forCommand))) {
                return er;
            }
            if (sameMember) {
                String prefix1 = response.forCommand.substring(0, COMM_CMD_PFX_SIZE);
                String prefix2 = er.forCommand.substring(0, COMM_CMD_PFX_SIZE);
                Integer iPacket = Integer.valueOf(response.forCommand.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                if (prefix1.equals(prefix2) && iPacket == COMM_PACKET_NB - 1) {
                    return er;
                }
            }
        }
        
        return null;
    }


    public void clearUserCommunication(ConnectedUserFrame member) {

        Set<ExpectedResponse> listToRemove = new HashSet<ExpectedResponse>();
        
        for (Comm.ExpectedResponse er : expectedResponses) {
            if (member.getName().equals(er.fromMember.getName())
                       && member.getIPAddress().equals(er.fromMember.getIPAddress())
                       && member.getPortAddress().equals(er.fromMember.getPortAddress()) ) {
                listToRemove.add(er);
            }
        }
        
        if (!listToRemove.isEmpty()) {
            expectedResponses.removeAll(listToRemove);
        }
        
    }

    private boolean isSameAddress(String senderAddress, ConnectedUserFrame aMember) {
        if (!aMember.isActive()) {
            LOG.log(Level.FINE, ".........member was not active (probably disconnected then reconnected). Reactivate it and memorize new address.");
            aMember.setActive(true);
            aMember.setIPAddress(senderAddress);
            return true;
        }
        if (senderAddress.equals(aMember.getxIPAddress() + ":" + aMember.getxPortAddress())) {
            return true;
        }
        if (senderAddress.equals(aMember.getpIPAddress() + ":" + aMember.getpPortAddress())) {
            if (userInProgress != null) {
                userInProgress.setUsePrivateIP(true);
            }
            return true;
        }
        LOG.log(Level.FINE, ".........address mismatch : senderAddress="+senderAddress + " - IPAddress="+aMember.getxIPAddress() + ":" + aMember.getxPortAddress() + " - IPpAddress="+aMember.getpIPAddress() + ":" + aMember.getpPortAddress());
        return false;
    }

    
    /**
     * Manage communicaiton errors
     * 
     * @param bundle_err : official bundle error message code (ERR_...)
     * @param log_msg : optional additional msg to put in the log file
     * @param location : method name where the error took place
     * @param ex : exception to print
     * @param display : true to display message box to user
     */
    private void displayErrorMessage(String bundle_err, String log_msg, String location, Exception ex, boolean display) {
        
        // Prepare localized message
        final String title = NbBundle.getMessage(GedcomCompareTopComponent.class, "OpenIDE-Module-Name") + " - " + NbBundle.getMessage(Comm.class, "TTL_CommunicationError");
        String exception_msg = (ex != null && ex.getMessage() != null) ? ex.getMessage().replace(COMM_SERVER, "www.ancestris.server") : ""; // simplify server url
        final String msg = NbBundle.getMessage(Comm.class, bundle_err, exception_msg);

        // Log exception
        LOG.log(Level.SEVERE, title + " : " + msg);
        LOG.log(Level.SEVERE, location + "()");
        if (log_msg != null && !log_msg.isEmpty()) {
            LOG.log(Level.SEVERE, log_msg);
        }
        
        // Stop the ping to the server whatsoever
        sharing = false;  

        // For the first error, process it completely
        if (!isCommError) {

            // Declare error status after initial message in order not come here again
            isCommError = true;

            // Print the first exception
            if (ex != null && display) {
                Exceptions.printStackTrace(ex);        
            }

            // Display first error message to user
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    owner.getConsole().printError(title + " : " + msg, true);
                    DialogManager.create(title, msg).setOptionType(DialogManager.OK_ONLY_OPTION).setMessageType(DialogManager.ERROR_MESSAGE).show();
                }
            });

            // Unregister if possible to make sure user start anew 
            owner.stopSharing(); // if possible (might generate eror message itself
        }

        
    }

    
  
    
    
    // Classes
    
    private class ExpectedResponse {
        private ConnectedUserFrame fromMember = null;
        private String forCommand = "";
        
        public ExpectedResponse(ConnectedUserFrame member, String command) {
            this.fromMember = member;
            this.forCommand = command.replace("G", "T");
        }
    }
    

    
    
    /**
     * Returns an <code>InetAddress</code> object encapsulating what is most
     * likely the machine's LAN IP address.
     * <p/>
     * This method is intended for use as a replacement of JDK method
     * <code>InetAddress.getLocalHost</code>, because that method is ambiguous
     * on Linux systems. Linux systems enumerate the loopback network interface
     * the same way as regular LAN network interfaces, but the JDK
     * <code>InetAddress.getLocalHost</code> method does not specify the
     * algorithm used to select the address returned under such circumstances,
     * and will often return the loopback address, which is not valid for
     * network communication. Details
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
     * <p/>
     * This method will scan all IP addresses on all network interfaces on the
     * host machine to determine the IP address most likely to be the machine's
     * LAN address. If the machine has multiple IP addresses, this method will
     * prefer a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually
     * IPv4) if the machine has one (and will return the first site-local
     * address if the machine has more than one), but if the machine does not
     * hold a site-local address, this method will return simply the first
     * non-loopback address found (IPv4 or IPv6).
     * <p/>
     * If this method cannot find a non-loopback address using this selection
     * algorithm, it will fall back to calling and returning the result of JDK
     * method <code>InetAddress.getLocalHost</code>.
     * <p/>
     *
     * @throws UnknownHostException If the LAN address of the machine cannot be
     * found.
     */
    private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {

                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it immediately...
                            return inetAddr;
                        } else if (candidateAddress == null) {
                        // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            candidateAddress = inetAddr;
                        // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will be non-null.
                        }
                    }
                }
            }
            if (candidateAddress != null) {
            // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it might be running
                // IPv6 which deprecates the "site-local" concept).
                // Return this non-loopback candidate address...
                return candidateAddress;
            }
        // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception ex) {
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + ex);
            unknownHostException.initCause(ex);
            throw unknownHostException;
        }
    }
    
    
    public class User {
        
        public UserProfile userProfile;
        public String f_NbIndis;
        public String f_NbFamilies;
        public String f_NbSTs;
        public String f_NbEvens;
        public String[] f_STs;
        public String stats_nbOveraps;
        public String stats_nbCityNames;
        public String stats_nbEvents;
        
        public User(UserProfile userProfile, String f_NbIndis, String f_NbFamilies, String f_NbSTs, String f_NbEvens, String[] f_STs, String over, String cina, String even) {
            
            this.userProfile = userProfile;
            this.f_NbIndis = f_NbIndis;
            this.f_NbFamilies = f_NbFamilies;
            this.f_NbSTs = f_NbSTs;
            this.f_NbEvens = f_NbEvens;
            this.f_STs = f_STs;
            this.stats_nbOveraps = over;
            this.stats_nbCityNames = cina;
            this.stats_nbEvents = even;
        }
    }

    private class CommStreamObject {
        
        private Map<Integer, byte[]> sentPackets = null;               // sent
        private ByteArrayOutputStream receivedStream = null;           // received
        private boolean receivedStreamEOF = false;                     // EOF flag when receiving

        public CommStreamObject () {
        }
        
        public void reset() {
            receivedStream = null;
            receivedStreamEOF = false;
        }
        
        public void setPackets(Map<Integer, byte[]> packets) {
            this.sentPackets = packets;
        }
        
        public Map<Integer, byte[]> getPackets() {
            return this.sentPackets;
        }
        
        public boolean isEmpty() {
            return this.sentPackets == null;
        }
        
        public boolean isComplete() {
            return receivedStreamEOF;
        }

        public void setComplete(boolean set) {
            receivedStreamEOF = set;
        }

        public byte[] getStream() {
            return receivedStream.toByteArray();
        }

        public void init() {
            if (receivedStream == null) {
                receivedStream = new ByteArrayOutputStream();
            } else {
                receivedStream.reset();
            }
        }
        
        public void write(byte[] bytes) throws IOException {
            receivedStream.write(bytes);
        }
    }
    
}

