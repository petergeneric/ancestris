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
import ancestris.modules.treesharing.panels.SharedGedcom;
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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
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
 *      ∘ THERE - I am there
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
 * Documentation : UDP - http://www.brynosaurus.com/pub/net/p2pnat/ 
 * Packet - http://www.javaworld.com/article/2077539/learn-java/java-tip-40--object-transport-via-datagram-packets.html
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

    private static DatagramSocket socket = null;

    // Get members web service
    private static String CMD_GETMB = "/get_members.php?";
    private static String TAG_MEMBER = "member";
    private static String TAG_PSEUDO = "pseudo";
    private static String TAG_IPADDR = "ipaddress";
    private static String TAG_PORTAD = "portaddress";

    // Command and Packets size
    private int COMM_PACKET_SIZE = 10000;   // max size of UDP packet seems to be 16384 (on my box), sometimes 8192 (on François' box for instance)
    private double COMM_COMPRESSING_FACTOR = 1.3;   // estimated maximum compressing factor of GZIP in order to calculate the size under the above limit
    private int COMM_CMD_SIZE = 5;
    private int COMM_CMD_PFX_SIZE = 2;
    private String FMT_IDX = "%03d";
    private int COMM_PACKET_NB = 1000;
    private static String STR_DELIMITER = " ";
    private int REQUEST_TIMEOUT = 3;        // wait for that many seconds before calling timout 
    private int COMM_NB_FAILS = 6;          // give up after this nb of "no response"
    private int COMM_RESPONSE_DELAY = 50;   // in milliseconds for the waiting loop

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
    // Sharing my shared entities
    private static String CMD_GILxx = "GA";   // Get Individual lastnames
    private static String CMD_TILxx = "TA";   // Take individual lastnames
    private static String CMD_GIDxx = "GB";   // Get individual details
    private static String CMD_TIDxx = "TB";   // Take individual details
    private static String CMD_GFLxx = "GC";   // Get family lastnames
    private static String CMD_TFLxx = "TC";   // Take family lastnames
    private static String CMD_GFDxx = "GD";   // Get family details
    private static String CMD_TFDxx = "TD";   // Take family details
    private static String CMD_GSTAT = "GS";   // Get stats (one packet will be enough)
    private static String CMD_TSTAT = "TS";   // Take stats (one packet will be enough)
    private static String CMD_GPFxx = "GP";   // Get profile
    private static String CMD_TPFxx = "TP";   // Take profile
    private static String CMD_TXPxx = "TX";   // I say thanks and provide my profile
    
    // Threads
    private volatile boolean sharing;
    private Thread listeningThread;
    private Thread pingingThread;
    private int refreshDelay;

    // Call info
    private boolean communicationInProgress = false;
    private boolean connectionInProgress = false;
    private boolean expectedConnection = false;
    private String expectedCallIPAddress = null;
    private String expectedCallPortAddress = null;
    private boolean expectedCall = false;

    // Possible data objects to be received
    private boolean gedcomNumbersEOF = false;
    private GedcomNumbers gedcomNumbers = null;
    private boolean listOfIndiLastnamesEOF = false;
    private Set<String> listOfIndiLastnames = null;
    private boolean listOfIndiDetailsEOF = false;
    private Set<GedcomIndi> listOfIndiDetails = null;
    private boolean listOfFamLastnamesEOF = false;
    private Set<String> listOfFamLastnames = null;
    private boolean listOfFamDetailsEOF = false;
    private Set<GedcomFam> listOfFamDetails = null;
    private boolean memberProfileEOF = false;
    private ByteArrayOutputStream memberProfile = null;
    private ByteArrayOutputStream memberProfileRcv = null;
    
    // Possible data objects to be sent by packets
    private Map<Integer, Set<String>> packetsOfIndiLastnames = null;
    private Map<Integer, Set<GedcomIndi>> packetsOfIndiDetails = null;
    private Map<Integer, Set<String>> packetsOfFamLastnames = null;
    private Map<Integer, Set<GedcomFam>> packetsOfFamDetails = null;
    private Map<Integer, byte[]> packetsOfProfile = null;
    
    
    
    
    
    /**
     * Constructor
     */
    public Comm(TreeSharingTopComponent tstc, int refreshDelay) {
        this.owner = tstc;
        this.refreshDelay = refreshDelay;
    }

    
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
        listeningThread.setName("TreeSharing thread : loop to wait for Ancestris connections");
        listeningThread.start();
        
        pingingThread = new Thread() {
            @Override
            public void run() {
                ping();
            }
        };

        pingingThread.setName("TreeSharing thread : loop to stay alive with server");
        pingingThread.start();

        LOG.log(Level.INFO, "Start thread listening to incoming calls until " + owner.getRegisteredEndDate());

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
     * Identify the list of currently sharing friends from the ancestris server
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
            //Exceptions.printStackTrace(ex);
            LOG.log(Level.INFO, "Error while getting ancestris members : " + ex.getMessage());
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
            //Exceptions.printStackTrace(ex);
            LOG.log(Level.INFO, "Error while querying the server : " + ex.getMessage());
        }

        return ret;
    }

    

    
    
    
    
    
    /**
     * Register on Ancestris server that I am ready to share 
     */
    public boolean registerMe(String pseudo) {

        LOG.log(Level.INFO, "***");
        try {
            // Create our unique socket
            socket = new DatagramSocket();
            
            // Registers on server
            sendCommand(CMD_REGIS, pseudo, null, COMM_SERVER, COMM_PORT);

            // Listen to reply
            byte[] bytesReceived = new byte[512];
            DatagramPacket packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
            socket.setSoTimeout(COMM_TIMEOUT);          // make sure there is a timeout to this
            socket.receive(packetReceived);     
            
            // Process reply
            String reply = StringEscapeUtils.unescapeHtml(new String(bytesReceived).split("\0")[0]);  // stop string at null char and convert html escape characters
            if (reply.substring(0, COMM_CMD_SIZE).equals(CMD_REGOK)) {
                LOG.log(Level.FINE, "...(REGOK) Registered " + pseudo + " on the Ancestris server.");
                socket.setSoTimeout(0);
            } else if (reply.substring(0, COMM_CMD_SIZE).equals(CMD_REGKO)) {
                String err = reply.substring(COMM_CMD_SIZE);
                LOG.log(Level.FINE, "...(REGKO) Could not register " + pseudo + " on the Ancestris server. Error : " + err);
                if (err.startsWith("Duplicate entry")) {
                    err = NbBundle.getMessage(Comm.class, "ERR_DuplicatePseudo");
                }
                DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Registration"), err).setMessageType(DialogManager.ERROR_MESSAGE).show();
                return false;
            }
            
        } catch(SocketTimeoutException e) {
            String err = NbBundle.getMessage(Comm.class, "ERR_ServerNotResponding");
            LOG.log(Level.FINE, "...(TIMEOUT) Could not register " + pseudo + " from the Ancestris server. Error : " + err);
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

        try {
            // Send unrestering command
            sendCommand(CMD_UNREG, pseudo, null, COMM_SERVER, COMM_PORT);
            
            // Expect answer back (wait for response from the other thread...)
            int s = 0;
            while (sharing && (s < 200)) {  // set give up time to 4 seconds
                TimeUnit.MILLISECONDS.sleep(20);
                s++;
            }
            if (sharing) { // response never came back after timeout, consider it failed
                String err = NbBundle.getMessage(Comm.class, "ERR_ServerNotResponding");
                LOG.log(Level.FINE, "...(TIMEOUT) Could not unregister " + pseudo + " from the Ancestris server. Error : " + err);
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
    

    
    
    
    

    /**
     * Calls the server every 150 seconds (hole lasts 180 seconds in general)
     */
    private void ping() {

        while (sharing) {
            sendPing();
            try {
                TimeUnit.SECONDS.sleep(refreshDelay);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }
  
    public void sendPing() {

        if (sharing) {
            sendCommand(CMD_PONGG, owner.getRegisteredPseudo(), null, COMM_SERVER, COMM_PORT);
        }

    }
    
    
    
    public void setCommunicationInProgress(boolean inProgress) {
        communicationInProgress = inProgress;
    }
    
    
    
    public GedcomNumbers getNbOfEntities(AncestrisMember member) {
        if (gedcomNumbers == null) {
            gedcomNumbers = new GedcomNumbers();
        }
        gedcomNumbersEOF = false;
        call(member, CMD_GSTAT, null);
        return gedcomNumbers;
    }
    
    public Set<String> getSharedIndiLastnamesFromMember(AncestrisMember member) {
        if (listOfIndiLastnames == null) {
            listOfIndiLastnames = new HashSet<String>();
        } else {
            listOfIndiLastnames.clear();
        }
        listOfIndiLastnamesEOF = false;
        call(member, CMD_GILxx, null);
        return listOfIndiLastnames;
    }

    public Set<GedcomIndi> getGedcomIndisFromMember(AncestrisMember member, Set<String> commonIndiLastnames) {
        if (listOfIndiDetails == null) {
            listOfIndiDetails = new HashSet<GedcomIndi>();
        } else {
            listOfIndiDetails.clear();
        }
        listOfIndiDetailsEOF = false;
        call(member, CMD_GIDxx, commonIndiLastnames);
        return listOfIndiDetails;
    }

    
    public Set<String> getSharedFamLastnamesFromMember(AncestrisMember member) {
        if (listOfFamLastnames == null) {
            listOfFamLastnames = new HashSet<String>();
        } else {
            listOfFamLastnames.clear();
        }
        listOfFamLastnamesEOF = false;
        call(member, CMD_GFLxx, null);
        return listOfFamLastnames;
    }

    public Set<GedcomFam> getGedcomFamsFromMember(AncestrisMember member, Set<String> commonFamLastnames) {
        if (listOfFamDetails == null) {
            listOfFamDetails = new HashSet<GedcomFam>();
        } else {
            listOfFamDetails.clear();
        }
        listOfFamDetailsEOF = false;
        call(member, CMD_GFDxx, commonFamLastnames);
        return listOfFamDetails;
    }
    
    
    public MemberProfile getProfileMember(AncestrisMember member) {
        if (memberProfile == null) {
            memberProfile = new ByteArrayOutputStream();
        } else {
            memberProfile.reset();
        }
        memberProfileEOF = false;
        call(member, CMD_GPFxx, null);
        return (MemberProfile) unwrapObject(memberProfile.toByteArray());
    }
    

    public void thankMember(AncestrisMember member) {
        packetsOfProfile = buildPacketsOfProfile(owner.getMyProfile());
        put(member, CMD_TXPxx, packetsOfProfile);
    }
        
            
            
            
    
    
        
    

    /**
     * Generic call method to friend and expect something in return
     */
    public void call(AncestrisMember member, String command, Object object) {

        if (socket == null || socket.isClosed()) {
            return;
        }
        
        // Connect to Member only if no communication in progress
        if (!communicationInProgress && !connectToMember(member)) {
            return;
        }
        
        // Init call data
        expectedCallIPAddress = member.getIPAddress();
        expectedCallPortAddress = member.getPortAddress();
        
        // Loop on packets. Last packet number is COMM_PACKET_NB-1.
        int iPacket = 0;
        boolean retry = true;
        int nbNoResponses = 0; // nb of consecutive no responses
        LOG.log(Level.FINE, "Calling member " + member.getMemberName() + " with " + command);
        while (iPacket < COMM_PACKET_NB && nbNoResponses < COMM_NB_FAILS) {  // stop at the last packet or after 10 consecutive retry/skips
            String commandIndexed = command + String.format(FMT_IDX, iPacket);
            try {
                // Ask member for list of something
                // FIXME : possible bug if object size > limit (case of common indi or fam lastnames)
                sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, (iPacket == 0 ? object : null), expectedCallIPAddress, Integer.valueOf(expectedCallPortAddress));
            
                // Expect answer back and get shared entities in return (wait for response from the other thread...)
                expectedCall = true;
                int s = 0;
                while (expectedCall && (s < REQUEST_TIMEOUT*100)) {  
                    TimeUnit.MILLISECONDS.sleep(10);
                    s++;
                }
                if (expectedCall) { // response never came back after timeout, retry once or consider it failed
                    nbNoResponses++;
                    if (retry) {
                        LOG.log(Level.FINE, "...(TIMEOUT) No response from " + member.getMemberName() + " after " + REQUEST_TIMEOUT + "s timeout. Retrying once...");
                        retry = false;
                    } else {
                        LOG.log(Level.FINE, "...(TIMEOUT) No response from " + member.getMemberName() + " after " + REQUEST_TIMEOUT + "s timeout. Skip");
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
                if (command.equals(CMD_GSTAT) && gedcomNumbersEOF) {
                    break;
                }
                if (command.equals(CMD_GILxx) && listOfIndiLastnamesEOF) {
                    break;
                }
                if (command.equals(CMD_GIDxx) && listOfIndiDetailsEOF) {
                    break;
                }
                if (command.equals(CMD_GFLxx) && listOfFamLastnamesEOF) {
                    break;
                }
                if (command.equals(CMD_GFDxx) && listOfFamDetailsEOF) {
                    break;
                }
                if (command.equals(CMD_GPFxx) && memberProfileEOF) {
                    break;
                }
            
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
                return;
            }
        }
        LOG.log(Level.FINE, "...(END) Returned call from member " + member.getMemberName() + " after " + iPacket + " packets");
    }

    
    /**
     * Generic call method to friend and expect something in return
     */
    public void put(AncestrisMember member, String command, Map<Integer, byte[]> packets) {

        if (socket == null || socket.isClosed()) {
            return;
        }
        
        // Connect to Member only if no communication in progress
        if (!communicationInProgress && !connectToMember(member)) {
            return;
        }
        
        // Loop on packets. Last packet number is COMM_PACKET_NB-1.
        String senderIP = member.getIPAddress();
        int senderPort = Integer.valueOf(member.getPortAddress());
        int iPacket = 0;
        LOG.log(Level.FINE, "Puting member " + member.getMemberName() + " with " + command);
        while (iPacket < COMM_PACKET_NB) {  // stop at the last packet 
            String commandIndexed = command + String.format(FMT_IDX, iPacket);
            byte[] set = packetsOfProfile.get(iPacket);
            if (set == null) {
                commandIndexed = command + String.format(FMT_IDX, COMM_PACKET_NB - 1);
                sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, null, senderIP, senderPort);
                break;
            } else {
                sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, set, senderIP, senderPort);
            }
            iPacket++;
            // wait a bit before sending next packet
            try {
                TimeUnit.MILLISECONDS.sleep(COMM_RESPONSE_DELAY);
            } catch (InterruptedException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }
        LOG.log(Level.FINE, "...(END) Enf of put to member " + member.getMemberName() + " with " + iPacket + " packets");
    }

    
    
    
    private boolean connectToMember(AncestrisMember member) {
        
        connectionInProgress = true;
        try {
            sendCommand(CMD_CONCT, member.getMemberName(), null, COMM_SERVER, COMM_PORT);

            // Expect that connection gets established (wait for response from the other thread...)
            expectedConnection = true;
            int s = 0;
            while (expectedConnection && (s < REQUEST_TIMEOUT * 100)) {
                TimeUnit.MILLISECONDS.sleep(10);
                s++;
            }
            
            if (expectedConnection) { // response never came back after 10 seconds, consider it failed
                expectedConnection = false;
                LOG.log(Level.FINE, "...(TIMEOUT) No connection to " + member.getMemberName() + " after " + REQUEST_TIMEOUT + "s timeout.");
                return false;
            }
            // wait a bit for my generated pings and pongs have been processed
            TimeUnit.MILLISECONDS.sleep(500);
            connectionInProgress = false;
            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        LOG.log(Level.FINE, "...(SUCCESS) Connected successfully to member " + member.getMemberName());
        return true;
    }

    
    
    
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
     * Main listen method : listens to incoming calls and process them all
     */
    public void listen() {

        String command = null;
        String senderAddress = null;
        String senderIP = null;
        int senderPort = 0;
        byte[] bytesReceived = new byte[COMM_PACKET_SIZE];
        DatagramPacket packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
        
        byte[] contentMemberBytes = null;
        String contentMemberStr = null;
        byte[] contentObj = null;
        String member = null;
        AncestrisMember aMember = null;
        
        LOG.log(Level.INFO, "Listening to all incoming calls indefinitely.......");

        try {
            while (sharing) {
                
                // Listen to incoming calls indefinitely
                socket.setSoTimeout(0);
                socket.receive(packetReceived);
                
                // Identify key elements of call for all calls
                senderIP = packetReceived.getAddress().getHostAddress();
                senderPort = packetReceived.getPort();
                senderAddress = senderIP + ":" + senderPort;
                
                // Identify command
                command = new String(Arrays.copyOfRange(bytesReceived, 0, COMM_CMD_SIZE));        
                
                // Identify member part of bytes until STR_DELIMITER
                contentMemberBytes = extractBytes(Arrays.copyOfRange(bytesReceived, COMM_CMD_SIZE, bytesReceived.length), STR_DELIMITER.getBytes()[0]);
                contentMemberStr = new String(contentMemberBytes);
                
                LOG.log(Level.FINE, "...Incoming " + command + " command received from " + senderAddress);

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
                    LOG.log(Level.FINE, "...Could not unregister " + owner.getRegisteredPseudo() + " from the Ancestris server. Error : " + err);
                    DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_Unregistration"), err).setMessageType(DialogManager.ERROR_MESSAGE).show();
                    continue;
                } 

                // Case of CMD_CONCT command (server replies back to my connection request or asks me to connect to indicated pseudo)
                if (command.equals(CMD_CONCT)) {
                    member = StringEscapeUtils.unescapeHtml(contentMemberStr);
                    LOG.log(Level.FINE, "...Request to connect to " + member);
                    owner.updateMembersList();
                    aMember = owner.getMember(member);
                    if (aMember == null) {
                        LOG.log(Level.FINE, "...Member " + member + " is not in the list of members.");
                    }
                    else if (aMember.isAllowed()) {
                        sendCommand(CMD_PINGG, owner.getRegisteredPseudo() + STR_DELIMITER, null, aMember.getIPAddress(), Integer.valueOf(aMember.getPortAddress()));
                        LOG.log(Level.FINE, "...Member " + member + " is allowed. Replied back with PINGG.");
                    } else {
                        LOG.log(Level.FINE, "...Member " + member + " is NOT allowed. No reply.");
                    }
                    continue;
                }
                


                
                
                //
                // PROCESS COMMANDS FROM OTHER ANCESTRIS MEMBER
                //
                
                // Identify member elements of call and content. If member not allowed, continue
                member = StringEscapeUtils.unescapeHtml(contentMemberStr);
                aMember = owner.getMember(member);
                if (aMember == null) {
                    LOG.log(Level.FINE, "...Calling member " + member + " is not in the list of members.");
                    continue;
                } else if (!aMember.isAllowed() ||  !senderAddress.equals(aMember.getIPAddress()+":"+aMember.getPortAddress())) {
                    LOG.log(Level.FINE, "...Member " + member + " is NOT allowed or address does not match the one I know. Do not reply.");
                    continue;
                } 
                contentObj = Arrays.copyOfRange(bytesReceived, COMM_CMD_SIZE + contentMemberBytes.length + STR_DELIMITER.length(), bytesReceived.length);
                
                
                // Case of PING command 
                if (command.equals(CMD_PINGG)) {
                    sendCommand(CMD_PONGG, owner.getRegisteredPseudo() + STR_DELIMITER, null, senderIP, senderPort);
                    expectedConnection = false;
                    continue;
                } 
                
                // Case of PONG command 
                if (command.equals(CMD_PONGG)) {
                    expectedConnection = false;
                    if (!connectionInProgress) {
                        owner.addConnection(member);
                    }
                    continue;
                } 


                
                //********************** Get and receive statistics **********************
                
                // Case of CMD_GSTAT command (member asks for the number of entities. Send back.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_GSTAT)) {
                    String commandIndexed = CMD_TSTAT + String.format(FMT_IDX, COMM_PACKET_NB - 1);
                    GedcomNumbers gn = getMySharedNumbers(owner.getSharedGedcoms());
                    TimeUnit.MILLISECONDS.sleep(COMM_RESPONSE_DELAY);
                    sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, gn, senderIP, senderPort);
                    continue;
                }

                // Case of CMD_TSTAT command (following my GSTAT message to another member, he/she returns his/her nb of entities. Take them.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_TSTAT)) {
                    LOG.log(Level.FINE, "...Packet size is " + packetReceived.getLength() + " bytes");
                    // Make sure there is a pending call expecting something from the ipaddress and port received
                    if (expectedCall && expectedCallIPAddress != null && expectedCallPortAddress != null && senderAddress.equals(expectedCallIPAddress + ":" + expectedCallPortAddress)) {
                        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                        if (iPacket == COMM_PACKET_NB - 1) { 
                            gedcomNumbersEOF = true;
                            gedcomNumbers = (GedcomNumbers) unwrapObject(contentObj);
                        }
                        expectedCall = false;
                    }
                    continue;
                }

                
                
                
                
                //********************** Get and receive lastnames **********************
                
                // Case of CMD_GILxx command (member asks for the list of lastnames I am sharing). Send back.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_GILxx)) {
                    Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                    if (iPacket == 0 || packetsOfIndiLastnames == null) {
                        packetsOfIndiLastnames = buildPacketsOfString(getMySharedIndiLastnames(owner.getSharedGedcoms()));
                    }
                    String commandIndexed = CMD_TILxx + String.format(FMT_IDX, iPacket);
                    Set<String> set = packetsOfIndiLastnames.get(iPacket);
                    TimeUnit.MILLISECONDS.sleep(COMM_RESPONSE_DELAY);
                    if (set == null) {
                        commandIndexed = CMD_TILxx + String.format(FMT_IDX, COMM_PACKET_NB - 1);
                        sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, null, senderIP, senderPort);
                    } else {
                        sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, set, senderIP, senderPort);
                    }
                    continue;
                }

                // Case of CMD_TILxx command (following my GILxx message to another member, he/she returns his/her shared entities. Take them.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_TILxx)) {
                    LOG.log(Level.FINE, "...Packet size is " + packetReceived.getLength() + " bytes");
                    // Make sure there is a pending call expecting something from the ipaddress and port received
                    if (expectedCall && expectedCallIPAddress != null && expectedCallPortAddress != null && senderAddress.equals(expectedCallIPAddress + ":" + expectedCallPortAddress)) {
                        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                        if (iPacket == COMM_PACKET_NB - 1) { // no more packet
                            listOfIndiLastnamesEOF = true;
                            packetsOfIndiLastnames = null;
                        } else {
                            listOfIndiLastnames.addAll((Set<String>) unwrapObject(contentObj));
                        }
                        expectedCall = false;
                    }
                    continue;
                }

                
                
                
                //********************** Get and receive individuals **********************
                
                // Case of CMD_GIDxx command (member asks for the details on individuals for a given list of lastnames. Send back.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_GIDxx)) {
                    Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                    if (iPacket == 0 || packetsOfIndiDetails == null) {
                        packetsOfIndiDetails = buildPacketsOfIndis(getMySharedGedcomIndis(owner.getSharedGedcoms(), (Set<String>) unwrapObject(contentObj)));
                    }
                    String commandIndexed = CMD_TIDxx + String.format(FMT_IDX, iPacket);
                    Set<GedcomIndi> set = packetsOfIndiDetails.get(iPacket);
                    TimeUnit.MILLISECONDS.sleep(COMM_RESPONSE_DELAY);
                    if (set == null) {
                        commandIndexed = CMD_TIDxx + String.format(FMT_IDX, COMM_PACKET_NB - 1);
                        sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, null, senderIP, senderPort);
                    } else {
                        sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, set, senderIP, senderPort);
                        
                    }
                    continue;
                }

                // Case of CMD_TIDxx command (following my GIDxx message to another member, he/she returns his/her shared entities. Take them.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_TIDxx)) {
                    // Make sure there is a pending call expecting something from the ipaddress and port received
                    if (expectedCall && expectedCallIPAddress != null && expectedCallPortAddress != null && senderAddress.equals(expectedCallIPAddress + ":" + expectedCallPortAddress)) {
                        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                        if (iPacket == COMM_PACKET_NB - 1) { // no more packet
                            listOfIndiDetailsEOF = true;
                            packetsOfIndiDetails = null;
                        } else {
                            listOfIndiDetails.addAll((Set<GedcomIndi>) unwrapObject(contentObj));
                        }
                        expectedCall = false;
                    }
                    continue;
                }

                
                
                //********************** Get and receive family names **********************
                
                // Case of CMD_GFLxx command (member asks for the list of lastnames I am sharing). Send back.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_GFLxx)) {
                    Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                    if (iPacket == 0 || packetsOfFamLastnames == null) {
                        packetsOfFamLastnames = buildPacketsOfString(getMySharedFamLastnames(owner.getSharedGedcoms()));
                    }
                    String commandIndexed = CMD_TFLxx + String.format(FMT_IDX, iPacket);
                    Set<String> set = packetsOfFamLastnames.get(iPacket);
                    TimeUnit.MILLISECONDS.sleep(COMM_RESPONSE_DELAY);
                    if (set == null) {
                        commandIndexed = CMD_TFLxx + String.format(FMT_IDX, COMM_PACKET_NB - 1);
                        sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, null, senderIP, senderPort);
                    } else {
                        sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, set, senderIP, senderPort);
                    }
                    continue;
                }

                // Case of CMD_TFLxx command (following my GFLxx message to another member, he/she returns his/her shared entities. Take them.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_TFLxx)) {
                    // Make sure there is a pending call expecting something from the ipaddress and port received
                    if (expectedCall && expectedCallIPAddress != null && expectedCallPortAddress != null && senderAddress.equals(expectedCallIPAddress + ":" + expectedCallPortAddress)) {
                        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                        if (iPacket == COMM_PACKET_NB - 1) { // no more packet
                            listOfFamLastnamesEOF = true;
                            packetsOfFamLastnames = null;
                        } else {
                            listOfFamLastnames.addAll((Set<String>) unwrapObject(contentObj));
                        }
                        expectedCall = false;
                    }
                    continue;
                }

                
                
                
                //********************** Get and receive families **********************
                
                // Case of CMD_GFDxx command (member asks for the details on individuals for a given list of lastnames. Send back.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_GFDxx)) {
                    Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                    if (iPacket == 0 || packetsOfFamDetails == null) {
                        packetsOfFamDetails = buildPacketsOfFams(getMySharedGedcomFams(owner.getSharedGedcoms(), (Set<String>) unwrapObject(contentObj)));
                    }
                    String commandIndexed = CMD_TFDxx + String.format(FMT_IDX, iPacket);
                    Set<GedcomFam> set = packetsOfFamDetails.get(iPacket);
                    TimeUnit.MILLISECONDS.sleep(COMM_RESPONSE_DELAY);
                    if (set == null) {
                        commandIndexed = CMD_TFDxx + String.format(FMT_IDX, COMM_PACKET_NB - 1);
                        sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, null, senderIP, senderPort);
                    } else {
                        sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, set, senderIP, senderPort);
                    }
                    continue;
                }

                // Case of CMD_TFDxx command (following my GFDxx message to another member, he/she returns his/her shared entities. Take them.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_TFDxx)) {
                    // Make sure there is a pending call expecting something from the ipaddress and port received
                    if (expectedCall && expectedCallIPAddress != null && expectedCallPortAddress != null && senderAddress.equals(expectedCallIPAddress + ":" + expectedCallPortAddress)) {
                        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                        if (iPacket == COMM_PACKET_NB - 1) { // no more packet
                            listOfFamDetailsEOF = true;
                            packetsOfFamDetails = null;
                        } else {
                            listOfFamDetails.addAll((Set<GedcomFam>) unwrapObject(contentObj));
                        }
                        expectedCall = false;
                    }
                    continue;
                }

                
                
                //********************** Get and receive profile **********************
                
                // Case of CMD_GPFxx command (member asks for the profile. Send back.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_GPFxx)) {
                    Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                    if (iPacket == 0 || packetsOfProfile == null) {
                        packetsOfProfile = buildPacketsOfProfile(owner.getMyProfile());
                    }
                    String commandIndexed = CMD_TPFxx + String.format(FMT_IDX, iPacket);
                    byte[] set = packetsOfProfile.get(iPacket);
                    TimeUnit.MILLISECONDS.sleep(COMM_RESPONSE_DELAY);
                    if (set == null) {
                        commandIndexed = CMD_TPFxx + String.format(FMT_IDX, COMM_PACKET_NB - 1);
                        sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, null, senderIP, senderPort);
                    } else {
                        sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, set, senderIP, senderPort);
                    }
                    continue;
                }

                // Case of CMD_TPFxx command (following my GPFxx message to another member, he/she returns his/her profile. Take it.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_TPFxx)) {
                    // Make sure there is a pending call expecting something from the ipaddress and port received
                    if (expectedCall && expectedCallIPAddress != null && expectedCallPortAddress != null && senderAddress.equals(expectedCallIPAddress + ":" + expectedCallPortAddress)) {
                        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                        if (iPacket == COMM_PACKET_NB - 1) { // no more packet
                            memberProfileEOF = true;
                            packetsOfProfile = null;
                        } else {
                            memberProfile.write((byte[])unwrapObject(contentObj));
                        }
                        expectedCall = false;
                    }
                    continue;
                }

                
                
                
                
                //********************** Receive profile **********************
                
                // Case of THANX Profile command, take profile and log in incoming stats
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_TXPxx)) {
                    Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                    if (iPacket == 0) { // first packet
                        if (memberProfileRcv == null) {
                            memberProfileRcv = new ByteArrayOutputStream();
                        } else {
                            memberProfileRcv.reset();
                        }
                        memberProfileRcv.write((byte[]) unwrapObject(contentObj));
                    } else if (iPacket == COMM_PACKET_NB - 1) { // last packet, log in stats
                        owner.addUniqueFriend(member, (MemberProfile) unwrapObject(memberProfileRcv.toByteArray())); 
                    } else {
                        memberProfileRcv.write((byte[]) unwrapObject(contentObj));
                    }
                    continue;
                }
                
                
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    
    
    
    

    
    
    
    

    
    
    
    
    /**
     * Used to send command to Server and Members
     */
    private void sendCommand(String command, String string, Object object, String ipAddress, int portAddress) {
        
        byte[] msgBytes = null; // content to send

        String contentStr = command + string;
        byte[] contentBytes = contentStr.getBytes(Charset.forName(COMM_CHARSET));
        
        // Return just this if no object
        if (object == null) {
            msgBytes = contentBytes;
        } else {
            // ...else add wrapped object
            try {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byteStream.write(contentBytes);
                byteStream.write(wrapObject(object));
                msgBytes = byteStream.toByteArray();

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // Send whole msg
        if (!command.equals(CMD_PONGG)) {   // no need to log this PONGG message as it is sent every few minutes to the server
            LOG.log(Level.FINE, "Sending command " + command + " with " + string + (object != null ? " and object of size " + msgBytes.length : "") + " to " + ipAddress + ":" + portAddress);
        }
        sendObject(msgBytes, ipAddress, portAddress);
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
            Exceptions.printStackTrace(ex);
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
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return object;
    }

    /**
     * Elementary method to send object once packet has been built
     */
    private void sendObject(byte[] bytesSent, String ipAddress, int portAddress) {
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
    

    
    
    
    
    
    
    
    public GedcomNumbers getMySharedNumbers(List<SharedGedcom> sharedGedcoms) {
        GedcomNumbers gn = new GedcomNumbers();
        for (SharedGedcom sharedGedcom : sharedGedcoms) {
            gn.nbIndis += sharedGedcom.getNbOfPublicIndis();
            gn.nbFams += sharedGedcom.getNbOfPublicFams();
        }
        return gn;
    }

    
    public Set<String> getMySharedIndiLastnames(List<SharedGedcom> sharedGedcoms) {
        Set<String> ret = new HashSet<String>();
        for (SharedGedcom sharedGedcom : sharedGedcoms) {
            ret.addAll(sharedGedcom.getPublicIndiLastnames());
        }
        return ret;
    }

    public Set<String> getMySharedFamLastnames(List<SharedGedcom> sharedGedcoms) {
        Set<String> ret = new HashSet<String>();
        for (SharedGedcom sharedGedcom : sharedGedcoms) {
            ret.addAll(sharedGedcom.getPublicFamLastnames());
        }
        return ret;
    }

    public Set<GedcomIndi> getMySharedGedcomIndis(List<SharedGedcom> sharedGedcoms, Set<String> commonIndiLastnames) {
        Set<GedcomIndi> ret = new HashSet<GedcomIndi>();
        for (SharedGedcom sharedGedcom : sharedGedcoms) {
            ret.addAll(sharedGedcom.getPublicGedcomIndis(commonIndiLastnames));
        }
        return ret;
    }

    public Set<GedcomFam> getMySharedGedcomFams(List<SharedGedcom> sharedGedcoms, Set<String> commonFamLastnames) {
        Set<GedcomFam> ret = new HashSet<GedcomFam>();
        for (SharedGedcom sharedGedcom : sharedGedcoms) {
            ret.addAll(sharedGedcom.getPublicGedcomFams(commonFamLastnames));
        }
        return ret;
    }

    
    
    /**
     * Builds packets of strings, not of bytes, so that each packets can be unwrapped into lists without the other packets
     */
    private Map<Integer, Set<String>> buildPacketsOfString(Set<String> masterSet) {
        Map<Integer, Set<String>> packets = new HashMap<Integer, Set<String>>();
        byte[] masterPacket = wrapObject(masterSet);
        int nbPackets = (int) (Math.min(COMM_PACKET_NB, (masterPacket.length * COMM_COMPRESSING_FACTOR) / COMM_PACKET_SIZE) + 1);   // + 15% and +1 to round up and have some margin because packets will not all be of same size and are not as compressed when split than together
        for (Integer i = 0; i < nbPackets; i++) {
            packets.put(i, new HashSet<String>());
        }
        int index = 0;
        for (String obj : masterSet) {
            packets.get(index++ % nbPackets).add(obj);
        }
        return packets;
    }

    /**
     * Builds packets of GedcomIndi, not of bytes, so that each packets can be unwrapped into lists without the other packets
     */
    private Map<Integer, Set<GedcomIndi>> buildPacketsOfIndis(Set<GedcomIndi> masterSet) {
        Map<Integer, Set<GedcomIndi>> packets = new HashMap<Integer, Set<GedcomIndi>>();
        byte[] masterPacket = wrapObject(masterSet);
        int nbPackets = (int) (Math.min(COMM_PACKET_NB, (masterPacket.length * COMM_COMPRESSING_FACTOR) / COMM_PACKET_SIZE) + 1);   // + 15% and +1 to round up and have some margin because packets will not all be of same size and are not as compressed when split than together
        for (Integer i = 0; i < nbPackets; i++) {
            packets.put(i, new HashSet<GedcomIndi>());
        }
        int index = 0;
        for (GedcomIndi obj : masterSet) {
            packets.get(index++ % nbPackets).add(obj);
        }
        return packets;
    }

    /**
     * Builds packets of GedcomFam, not of bytes, so that each packets can be unwrapped into lists without the other packets
     */
    private Map<Integer, Set<GedcomFam>> buildPacketsOfFams(Set<GedcomFam> masterSet) {
        Map<Integer, Set<GedcomFam>> packets = new HashMap<Integer, Set<GedcomFam>>();
        byte[] masterPacket = wrapObject(masterSet);
        int nbPackets = (int) (Math.min(COMM_PACKET_NB, (masterPacket.length * COMM_COMPRESSING_FACTOR) / COMM_PACKET_SIZE) + 1);   // + 15% and +1 to round up and have some margin because packets will not all be of same size and are not as compressed when split than together
        for (Integer i = 0; i < nbPackets; i++) {
            packets.put(i, new HashSet<GedcomFam>());
        }
        int index = 0;
        for (GedcomFam obj : masterSet) {
            packets.get(index++ % nbPackets).add(obj);
        }
        return packets;
    }

    private Map<Integer, byte[]> buildPacketsOfProfile(MemberProfile profile) {
        Map<Integer, byte[]> packets = new HashMap<Integer, byte[]>();
        byte[] masterPacket = wrapObject(profile);
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


}

