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
import java.io.EOFException;
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
import java.net.SocketTimeoutException;
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
    private static String COMM_CREDENTIALS = "user=treeshare01&pw=DhZP8imP&format=xml";       // for sql web service only
    private int COMM_TIMEOUT = 1000; // One second
    private boolean isCommError = false;                                                    // true if a communicaiton error exists

    private static DatagramSocket socket = null;

    // Get members web service
    private static String CMD_GETMB = "/get_members.php?";
    private static String TAG_MEMBER = "member";
    private static String TAG_PSEUDO = "pseudo";
    private static String TAG_IPADDR = "ipaddress";
    private static String TAG_PORTAD = "portaddress";
    private static String TAG_PIPADD = "pipaddress";
    private static String TAG_PPORTA = "pportaddress";

    // Command and Packets size
    private int COMM_PACKET_SIZE = 1400;   // max size of UDP packet seems to be 16384 (on my box), sometimes 8192 (on François' box for instance)
                    // Here it says 1400 : https://stackoverflow.com/questions/9203403/java-datagrampacket-udp-maximum-send-recv-buffer-size
    private double COMM_COMPRESSING_FACTOR = 3;   // estimated maximum compressing factor of GZIP in order to calculate the size under the above limit
    private String FMT_IDX = "%03d"; // size 3
    private int COMM_CMD_PFX_SIZE = 2;
    private int COMM_CMD_SIZE = 5;    // = 2 + size 3 (changes here means changing on the server as well)
    private int COMM_PACKET_NB = 1000;
    private static String STR_DELIMITER = " ";
    private int REQUEST_TIMEOUT = 4;        // wait for that many seconds before calling timout on each packet
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
    // Sharing my entities
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
    private static String CMD_TSPRF = "TY";   // I give my simple profile (no picture)
    
    // Threads
    private volatile boolean sharing;
    private Thread listeningThread;
    private Thread pingingThread;
    private int refreshDelay;

    // Call info
    private boolean communicationInProgress = false;
    private AncestrisMember memberInProgress = null;
    private boolean expectedConnection = false;
//    private String expectedCallIPAddress = null;
//    private String expectedCallPortAddress = null;
//    private boolean expectedCall = false;
    private Set<ExpectedResponse> expectedResponses = null;

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
    private String memberIPaddress = "";
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
        if (outputString == null) {
            return null;
        }
        if (outputString.isEmpty()) {
            return ancestrisMembers;
        }
        
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
                    String pipAddress = member.getElementsByTagName(TAG_PIPADD).item(0).getTextContent();
                    String pportAddress = member.getElementsByTagName(TAG_PPORTA).item(0).getTextContent();
                    ancestrisMembers.add(new AncestrisMember(pseudo, ipAddress, portAddress, pipAddress, pportAddress));
                }
            }
            
        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
            displayErrorMessage(false, "getAncestrisMembers Exception", "ERR_ParsingError", ex.getLocalizedMessage());
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
            isCommError = false;
        } catch (UnknownHostException ex) {
            displayErrorMessage(true, "getQueryResult UnknownHostException", "ERR_UnknownHostException", ex.getLocalizedMessage());
            ret = null;
        } catch (SocketException ex) {
            displayErrorMessage(true, "getQueryResult SocketException", "ERR_SocketException", ex.getLocalizedMessage());
            ret = null;
        } catch (IOException ex) {
            displayErrorMessage(true, "getQueryResult IOException", "ERR_IOException", ex.getLocalizedMessage());
            ret = null;
        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
            displayErrorMessage(true, "getQueryResult Exception", "ERR_Exception", ex.getLocalizedMessage());
            ret = null;
        }

        return ret;
    }



    
    
    
    
    
    /**
     * Register on Ancestris server that I am ready to share 
     */
    public boolean registerMe(String pseudo) {

        LOG.log(Level.FINE, "***");
        LOG.log(Level.FINE, "Communication packet size is "+COMM_PACKET_SIZE);
        clearCommunicationError();

        try {
            // Create our unique socket
            socket = new DatagramSocket();
            
            // Registers on server
            String content = pseudo + " " + getLocalHostLANAddress().getHostAddress() + " " + socket.getLocalPort();
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
            
        } catch(SocketTimeoutException ex) {
            String log = "...(TIMEOUT) Could not register " + pseudo + " from the Ancestris server.";
            displayErrorMessage(false, log, "ERR_ServerNotResponding", NbBundle.getMessage(Comm.class, "MSG_Registration") + " ; " + ex.getLocalizedMessage());
            return false;
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
            String log = "...(TIMEOUT) Could not register " + pseudo + " from the Ancestris server.";
            displayErrorMessage(false, log, "ERR_IOException", ex.getLocalizedMessage());
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
            boolean ret = sendCommand(CMD_UNREG, pseudo, null, COMM_SERVER, COMM_PORT);
            if (!ret) {
                return false;
            }
            
            // Expect answer back (wait for response from the other thread...)
            int s = 0;
            while (sharing && (s < 200)) {  // set give up time to 4 seconds
                TimeUnit.MILLISECONDS.sleep(20);
                s++;
            }
            if (sharing) { // response never came back after timeout, consider it failed
                String err = NbBundle.getMessage(Comm.class, "ERR_ServerNotResponding");
                String log = "...(TIMEOUT) Could not unregister " + pseudo + " from the Ancestris server.";
                displayErrorMessage(false, log, "ERR_ServerNotResponding", NbBundle.getMessage(Comm.class, "MSG_Unregistration"));
                return false;
            }
            
        } catch (Exception ex) {
            //Exceptions.printStackTrace(e);
            displayErrorMessage(false, "unregisterMe Exception", "ERR_Exception", NbBundle.getMessage(Comm.class, "MSG_Unregistration") + " ; " + ex.getLocalizedMessage());
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
                displayErrorMessage(true, "ping InterruptedException", "ERR_InterruptedException", ex.getLocalizedMessage());
                Exceptions.printStackTrace(ex);
            }
        }

    }
  
    public void sendPing() {

        if (sharing) {
            sendCommand(CMD_PONGG, owner.getRegisteredPseudo(false), null, COMM_SERVER, COMM_PORT);
        }

    }
    
    

    public void clearCommunicationError() {
        isCommError = false;
    }
    

    public void setCommunicationInProgress(boolean inProgress) {
        communicationInProgress = inProgress;
    }
    
    
    public boolean giveSimpleProfile(AncestrisMember member) {
        MemberProfile mp = owner.getMyProfile();
        //mp.photoBytes = null;
        return put(member, CMD_TSPRF, buildPacketsOfProfile(mp));
    }
        
    
    public GedcomNumbers getNbOfEntities(AncestrisMember member) {
        if (gedcomNumbers == null) {
            gedcomNumbers = new GedcomNumbers();
        }
        gedcomNumbers.nbIndis = 0;
        gedcomNumbers.nbFams = 0;
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
        MemberProfile mp = (MemberProfile) unwrapObject(memberProfile.toByteArray());
        if (mp != null) {
            mp.ipaddress = memberIPaddress;
        }
        memberIPaddress = "";
        return mp;
    }
    

    public void thankMember(AncestrisMember member) {
        if (packetsOfProfile == null) {
            packetsOfProfile = buildPacketsOfProfile(owner.getMyProfile());
        }
        put(member, CMD_TXPxx, packetsOfProfile);
    }
        
            
            
            
    
    
        
    

    /**
     * Generic call method to friend and expect something in return
     */
    public boolean call(AncestrisMember member, String command, Object object) {

        if (socket == null || socket.isClosed()) {
            return false;
        }
        
        // Connect to Member only if no communication in progress
        if (!communicationInProgress && !connectToMember(member)) {
            return false;
        }
        
        // Loop on packets. Last packet number is COMM_PACKET_NB-1.
        int iPacket = 0;
        boolean retry = true;
        int nbNoResponses = 0; // nb of consecutive no responses
        LOG.log(Level.FINE, "Calling member " + member.getMemberName() + " with " + command);
        while (iPacket < COMM_PACKET_NB && nbNoResponses < COMM_NB_FAILS) {  // stop at the last packet or after nb consecutive retry/skips
            String commandIndexed = command + String.format(FMT_IDX, iPacket);
            try {
                // Ask member for list of something
                sendCommand(commandIndexed, owner.getRegisteredPseudo() + STR_DELIMITER, (iPacket == 0 ? object : null), member.getIPAddress(), Integer.valueOf(member.getPortAddress()));
            
                // Expect answer back and get shared entities in return (wait for response from the other thread...)
                ExpectedResponse exResp = new ExpectedResponse(member, commandIndexed);
                expectedResponses.add(exResp);
                int s = 0;
                while (expectedResponses.contains(exResp) && (s < REQUEST_TIMEOUT*100)) {  
                    TimeUnit.MILLISECONDS.sleep(10);
                    s++;
                }
                if (expectedResponses.contains(exResp)) { // response never came back after timeout, retry once or consider it failed
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
            
            } catch (Exception ex) {
                displayErrorMessage(false, "call Exception", "ERR_CallException", ex.getLocalizedMessage());
                //Exceptions.printStackTrace(e);
                return false;
            }
        }
        LOG.log(Level.FINE, "...(END) Returned call from member " + member.getMemberName() + " after " + iPacket + " packets");
        return true;
    }

    
    /**
     * Generic call method to friend and expect something in return
     */
    public boolean put(AncestrisMember member, String command, Map<Integer, byte[]> packets) {

        if (socket == null || socket.isClosed()) {
            return false;
        }
        
        // Connect to Member only if no communication in progress
        if (!communicationInProgress && !connectToMember(member)) {
            return false;
        }
        
        // Loop on packets. Last packet number is COMM_PACKET_NB-1.
        String senderIP = member.getIPAddress();
        int senderPort = Integer.valueOf(member.getPortAddress());
        int iPacket = 0;
        LOG.log(Level.FINE, "Putting member " + member.getMemberName() + " with " + command);
        while (iPacket < COMM_PACKET_NB) {  // stop at the last packet 
            String commandIndexed = command + String.format(FMT_IDX, iPacket);
            byte[] set = packets.get(iPacket);
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
                return false;
            }
        }
        LOG.log(Level.FINE, "...(END) Enf of put to member " + member.getMemberName() + " with " + iPacket + " packets");
        return true;
    }

    
    
    
    private boolean connectToMember(AncestrisMember member) {
        
        memberInProgress = member;
        try {
            sendCommand(CMD_CONCT, member.getMemberName(), null, COMM_SERVER, COMM_PORT);

            // Expect that connection gets established (wait for response from the other thread...)
            expectedConnection = true;
            int s = 0;
            while (expectedConnection && (s < REQUEST_TIMEOUT * 100)) {
                TimeUnit.MILLISECONDS.sleep(10);
                s++;
            }
            
            if (expectedConnection) { // response never came back after timeout, consider it failed
                expectedConnection = false;
                LOG.log(Level.FINE, "...(TIMEOUT) No connection to " + member.getMemberName() + " after " + REQUEST_TIMEOUT + "s timeout.");
                return false;
            }
            // wait a bit for my generated pings and pongs have been processed
            TimeUnit.MILLISECONDS.sleep(500);
            memberInProgress = null;
            
        } catch (Exception ex) {
            displayErrorMessage(false, "connectToMember Exception", "ERR_ConnectException", ex.getLocalizedMessage());
            //Exceptions.printStackTrace(ex);
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
        byte[] bytesReceived = new byte[COMM_PACKET_SIZE*7];   // receiving packet can be much larger than garanteed size
        DatagramPacket packetReceived;
        
        byte[] contentMemberBytes = null;
        String contentMemberStr = null;
        byte[] contentObj = null;
        String member = null;
        AncestrisMember aMember = null;
        
        LOG.log(Level.FINE, "Listening to all incoming calls indefinitely.......");
        
        // Upload profile picture once for all
        packetsOfProfile = buildPacketsOfProfile(owner.getMyProfile());
        
        // Reset expected responses
        expectedResponses = new HashSet<ExpectedResponse>();

        try {
            while (sharing) {
                
                // Listen to incoming calls indefinitely
                packetReceived = new DatagramPacket(bytesReceived, bytesReceived.length);
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
                
                LOG.log(Level.FINE, "...Incoming " + command + " command received from " + senderAddress + " with packet of size ("+ packetReceived.getLength() + " bytes).");

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
                    LOG.log(Level.FINE, "...Could not unregister " + owner.getRegisteredPseudo(false) + " from the Ancestris server. Error : " + err);
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
                        // public connection
                        sendCommand(CMD_PINGG, owner.getRegisteredPseudo() + STR_DELIMITER, null, aMember.getxIPAddress(), Integer.valueOf(aMember.getxPortAddress()));
                        // private connection
                        if (!aMember.getpIPAddress().isEmpty() && Integer.valueOf(aMember.getpPortAddress()) != 0) {
                            sendCommand(CMD_PINGG, owner.getRegisteredPseudo() + STR_DELIMITER, null, aMember.getpIPAddress(), Integer.valueOf(aMember.getpPortAddress()));
                        }
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
                } else if (!aMember.isAllowed() ||  !isSameAddress(senderAddress, aMember)) {
                    LOG.log(Level.FINE, "...Member " + member + " is NOT allowed or address does not match the one I know. Do not reply.");
                    continue;
                } 
                contentObj = Arrays.copyOfRange(bytesReceived, COMM_CMD_SIZE + contentMemberBytes.length + STR_DELIMITER.length(), bytesReceived.length);
                if (contentObj == null || contentObj.length == 0) {
                    LOG.log(Level.FINE, "...Member " + member + " has sent an empty packet. Break process.");
                    continue;
                }

                ExpectedResponse response = new ExpectedResponse(aMember, command);
                
                
                // Case of PING command 
                if (command.equals(CMD_PINGG)) {
                    sendCommand(CMD_PONGG, owner.getRegisteredPseudo() + STR_DELIMITER, null, senderIP, senderPort);
                    expectedConnection = false;
                    continue;
                } 
                
                // Case of PONG command 
                if (command.equals(CMD_PONGG)) {
                    expectedConnection = false;
                    if (memberInProgress == null) {
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
                    // Make sure there is a pending call expecting something from the ipaddress and port received
                    ExpectedResponse er = getExpectedResponse(response);
                    if (er != null) {
                        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                        if (iPacket == COMM_PACKET_NB - 1) { 
                            gedcomNumbersEOF = true;
                            gedcomNumbers = (GedcomNumbers) unwrapObject(contentObj);
                        }
                        expectedResponses.remove(er);
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
                    // Make sure there is a pending call expecting something from the ipaddress and port received
                    ExpectedResponse er = getExpectedResponse(response);
                    if (er != null) {
                        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                        if (iPacket == COMM_PACKET_NB - 1) { // no more packet
                            listOfIndiLastnamesEOF = true;
                            packetsOfIndiLastnames = null;
                        } else {
                            listOfIndiLastnames.addAll((Set<String>) unwrapObject(contentObj));
                        }
                        expectedResponses.remove(er);
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
                    ExpectedResponse er = getExpectedResponse(response);
                    if (er != null) {
                        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                        if (iPacket == COMM_PACKET_NB - 1) { // no more packet
                            listOfIndiDetailsEOF = true;
                            packetsOfIndiDetails = null;
                        } else {
                            listOfIndiDetails.addAll((Set<GedcomIndi>) unwrapObject(contentObj));
                        }
                        expectedResponses.remove(er);
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
                    ExpectedResponse er = getExpectedResponse(response);
                    if (er != null) {
                        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                        if (iPacket == COMM_PACKET_NB - 1) { // no more packet
                            listOfFamLastnamesEOF = true;
                            packetsOfFamLastnames = null;
                        } else {
                            listOfFamLastnames.addAll((Set<String>) unwrapObject(contentObj));
                        }
                        expectedResponses.remove(er);
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
                    ExpectedResponse er = getExpectedResponse(response);
                    if (er != null) {
                        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                        if (iPacket == COMM_PACKET_NB - 1) { // no more packet
                            listOfFamDetailsEOF = true;
                            packetsOfFamDetails = null;
                        } else {
                            listOfFamDetails.addAll((Set<GedcomFam>) unwrapObject(contentObj));
                        }
                        expectedResponses.remove(er);
                    }
                    continue;
                }

                
                
                //********************** Get and receive profile **********************
                
                // Case of CMD_GPFxx command (member asks for the profile. Send back.
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_GPFxx)) {
                    Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                    if (packetsOfProfile == null) {
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
                    ExpectedResponse er = getExpectedResponse(response);
                    if (er != null) {
                        Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                        if (iPacket == COMM_PACKET_NB - 1) { // no more packet
                            memberProfileEOF = true;
                            memberIPaddress = senderIP;
                        } else {
                            memberProfile.write((byte[])unwrapObject(contentObj));
                        }
                        expectedResponses.remove(er);
                    }
                    continue;
                }

                
                
                
                
                //********************** Receive profile **********************
                
                // Case of Simple Profile command, take profile and log in incoming stats
                if (command.substring(0, COMM_CMD_PFX_SIZE).equals(CMD_TSPRF)) {
                    Integer iPacket = Integer.valueOf(command.substring(COMM_CMD_PFX_SIZE, COMM_CMD_SIZE));
                    if (iPacket == 0) { // first packet
                        if (memberProfileRcv == null) {
                            memberProfileRcv = new ByteArrayOutputStream();
                        } else {
                            memberProfileRcv.reset();
                        }
                        memberProfileRcv.write((byte[]) unwrapObject(contentObj));
                    } else if (iPacket == COMM_PACKET_NB - 1) { // last packet, log in stats
                        owner.addUniqueFriend(member, (MemberProfile) unwrapObject(memberProfileRcv.toByteArray()), senderIP, false); 
                    } else {
                        memberProfileRcv.write((byte[]) unwrapObject(contentObj));
                    }
                    continue;
                }
                
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
                        owner.addUniqueFriend(member, (MemberProfile) unwrapObject(memberProfileRcv.toByteArray()), senderIP, true); 
                    } else {
                        memberProfileRcv.write((byte[]) unwrapObject(contentObj));
                    }
                    continue;
                }
                
                
            }
        } catch (Exception ex) {
            DialogManager.create(NbBundle.getMessage(Comm.class, "MSG_CommunicationError"), ex.getLocalizedMessage()).setMessageType(DialogManager.ERROR_MESSAGE).show();
            Exceptions.printStackTrace(ex);
        }

    }

    
    
    
    

    
    
    
    

    
    
    
    
    /**
     * Used to send command to Server and Members
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
            //LOG.log(Level.SEVERE, "Sending command " + command + " with " + string + " to " + ipAddress + ":" + portAddress + " => Cannot wrap message. Abort communication.");
            LOG.log(Level.FINE, "Sending command " + command + " with " + string + " => Cannot wrap message. Abort communication.");
            return false;
            }
        
        // no need to log this PONGG message as it is sent every few minutes to the server
        if (!command.equals(CMD_PONGG)) {   
            //LOG.log(Level.INFO, "Sending command " + command + " with " + string + " and object of size (" + msgBytes.length + " bytes) to " + ipAddress + ":" + portAddress);
            LOG.log(Level.FINE, "Sending command " + command + " with " + string + " and object of size (" + msgBytes.length + " bytes)");
            }
        
        // Truncate package if object is too bug
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
            displayErrorMessage(false, "getWrappedObject", "ERR_WrappedException", ex.getLocalizedMessage());
            //Exceptions.printStackTrace(ex);
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
            displayErrorMessage(false, "wrapObject", "ERR_WrapException", ex.getLocalizedMessage());
            //Exceptions.printStackTrace(ex);
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
        } catch (EOFException ex) {
            String log = "Receiving message. Packet size was probably larger than the maximum packet size and therefore has been truncated, or packets have different sizes between the sender and the receiver. Please update your version of Ancestris or contact the Ancestris support.";
            displayErrorMessage(false, log, "ERR_UnwrapException", ex.getLocalizedMessage());
            //Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            displayErrorMessage(false, "unwrapObject IOException", "ERR_UnwrapException", ex.getLocalizedMessage());
            //Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            displayErrorMessage(false, "unwrapObject ClassNotFoundException", "ERR_UnwrapException", ex.getLocalizedMessage());
            //Exceptions.printStackTrace(ex);
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
        } catch (UnknownHostException ex) {
            //Exceptions.printStackTrace(ex);
            displayErrorMessage(true, "sendObject UnknownHostException", "ERR_UnknownHostException", ex.getLocalizedMessage());
            return false;
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
            displayErrorMessage(false, "sendObject IOException", "ERR_SendException", ex.getLocalizedMessage());
            return false;
        }
        return true;
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
        int nbPackets = (int) (Math.min(COMM_PACKET_NB, (masterPacket.length * COMM_COMPRESSING_FACTOR) / COMM_PACKET_SIZE) + 1);
        LOG.log(Level.FINE, "......Compressing " + masterSet.size() + " strings in " + nbPackets + " packets.");
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
        int nbPackets = (int) (Math.min(COMM_PACKET_NB, (masterPacket.length * COMM_COMPRESSING_FACTOR) / COMM_PACKET_SIZE) + 1);
        LOG.log(Level.FINE, "......Compressing " + masterSet.size() + " indis in " + nbPackets + " packets.");
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
        int nbPackets = (int) (Math.min(COMM_PACKET_NB, (masterPacket.length * COMM_COMPRESSING_FACTOR) / COMM_PACKET_SIZE) + 1);
        LOG.log(Level.FINE, "......Compressing " + masterSet.size() + " fams in " + nbPackets + " packets.");
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

    /**
     * 
     * @param response
     * @return 
     * - item with exact same member/command if exxists
     * - only remaining item if response includes 999 
     */
    private ExpectedResponse getExpectedResponse(ExpectedResponse response) {

        boolean sameMember = false;
        
        for (Comm.ExpectedResponse er : expectedResponses) {
            sameMember = (response.fromMember.getMemberName().equals(er.fromMember.getMemberName())
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


    public void clearMember(AncestrisMember member) {

        Set<ExpectedResponse> listToRemove = new HashSet<ExpectedResponse>();
        
        for (Comm.ExpectedResponse er : expectedResponses) {
            if (member.getMemberName().equals(er.fromMember.getMemberName())
                       && member.getIPAddress().equals(er.fromMember.getIPAddress())
                       && member.getPortAddress().equals(er.fromMember.getPortAddress()) ) {
                listToRemove.add(er);
            }
        }
        
        if (!listToRemove.isEmpty()) {
            expectedResponses.removeAll(listToRemove);
        }
        
    }

    private boolean isSameAddress(String senderAddress, AncestrisMember aMember) {
        if (senderAddress.equals(aMember.getxIPAddress()+":"+aMember.getxPortAddress())) {
            return true;
        }
        if (senderAddress.equals(aMember.getpIPAddress()+":"+aMember.getpPortAddress())) {
            if (memberInProgress != null) {
                memberInProgress.setUsePrivate(true);
            }
            return true;
        }
        return false;
        }

    private void displayErrorMessage(boolean mute, String log, String err, String sub_err) {
        
        if (mute && isCommError) {
            return;
        }
        
        final String title = NbBundle.getMessage(TreeSharingTopComponent.class, "OpenIDE-Module-Name") + " - " + NbBundle.getMessage(Comm.class, "MSG_CommunicationError");
        sub_err = sub_err.replace(COMM_SERVER, "www");
        final String msg = NbBundle.getMessage(Comm.class, err, sub_err);
        
        LOG.log(Level.FINE, log + "   (" + title + ": " + msg + ")");
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DialogManager.create(title, msg).setOptionType(DialogManager.OK_ONLY_OPTION).setMessageType(DialogManager.ERROR_MESSAGE).show();
            }
        });
        isCommError = true;
    }

    

    
    
    
    // Classes
    
    private class ExpectedResponse {
        private AncestrisMember fromMember = null;
        private String forCommand = "";
        
        public ExpectedResponse(AncestrisMember member, String command) {
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
    
    
     
    
}

