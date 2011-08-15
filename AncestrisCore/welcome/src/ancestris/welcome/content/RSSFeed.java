/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package ancestris.welcome.content;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class RSSFeed extends JPanel implements Constants, PropertyChangeListener {
    
    private String url;
    
    private boolean showProxyButton = true;

    private RequestProcessor.Task reloadTimer;
    protected long lastReload = 0;

    public static final String FEED_CONTENT_PROPERTY = "feedContent";
    
    private static DateFormat parsingDateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH ); // NOI18N
    private static DateFormat parsingDateFormatShort = new SimpleDateFormat( "EEE, dd MMM yyyy", Locale.ENGLISH ); // NOI18N
    private static DateFormat parsingDateFormatLong = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH ); // NOI18N
    private static DateFormat printingDateFormatShort = DateFormat.getDateInstance( DateFormat.SHORT );
    
    private boolean isCached = false;
    
    private final Logger LOGGER = Logger.getLogger( RSSFeed.class.getName() );
    
    private int maxDescriptionChars = -1;

    private static final RequestProcessor RP = new RequestProcessor("StartPage"); //NOI18N


    /** Returns file for caching of content. 
     * Enclosing folder is created if it does not exist yet.
     */
    private static File initCacheStore(String path) throws IOException {
        File cacheStore;
        String userDir = System.getProperty("netbeans.user"); // NOI18N
        if (userDir != null) {
            cacheStore = new File(new File(new File (userDir, "var"), "cache"), "welcome"); // NOI18N
        } else {
            File cachedir = FileUtil.toFile(FileUtil.getConfigRoot());
            cacheStore = new File(cachedir, "welcome"); // NOI18N
        }
        cacheStore = new File(cacheStore, path);
        cacheStore.getParentFile().mkdirs();
        cacheStore.createNewFile();
        return cacheStore;
    }
    
    public RSSFeed( String url, boolean showProxyButton ) {
        super( new BorderLayout() );
        setOpaque(false);
        this.url = url;
        this.showProxyButton = showProxyButton;
        setBorder(null);

        add( buildContentLoadingLabel(), BorderLayout.CENTER );
        
        HttpProxySettings.getDefault().addPropertyChangeListener( WeakListeners.propertyChange( this, HttpProxySettings.getDefault() ) );
    }
    
    public RSSFeed( boolean showProxyButton ) {
        this( null, showProxyButton );
    }
    
    public void setContent( Component content ) {
        removeAll();
        add( content, BorderLayout.CENTER );
        firePropertyChange( FEED_CONTENT_PROPERTY, null, content );
        revalidate();
        invalidate();
        repaint();
    }

    public Component getContent() {
        return this;
    }

    public void reload() {
        new Reload().start();
    }
    
    protected int getMaxItemCount() {
        return 5;
    }

    protected List<FeedItem> buildItemList() throws SAXException, ParserConfigurationException, IOException {
        XMLReader reader = XMLUtil.createXMLReader( false, true );
        FeedHandler handler = new FeedHandler( getMaxItemCount() );
        reader.setContentHandler( handler );
        reader.setEntityResolver( org.openide.xml.EntityCatalog.getDefault() );
        reader.setErrorHandler( new ErrorCatcher() );

        InputSource is = findInputSource(new URL(url));
        reader.parse( is );

        return handler.getItemList();
    }


    protected final String url2path( URL u ) {
        StringBuilder pathSB = new StringBuilder(u.getHost());
        if (u.getPort() != -1) {
            pathSB.append(u.getPort());
        }
        pathSB.append(u.getPath());
        if( null != u.getQuery() )
            pathSB.append(u.getQuery());
        return pathSB.toString();
    }
    
    /** Searches either for localy cached copy of URL content of original.
     */
    protected InputSource findInputSource( URL u ) throws IOException {
        HttpURLConnection httpCon = (HttpURLConnection) u.openConnection();
        httpCon.setUseCaches( true );
        httpCon.setRequestProperty( "Accept-Encoding", "gzip, deflate" );     // NOI18N

        Preferences prefs = NbPreferences.forModule( RSSFeed.class );
        String path = url2path( u );
        String lastModified = prefs.get( path, null );
        if( lastModified != null ) {
            httpCon.addRequestProperty("If-Modified-Since",lastModified); // NOI18N
        }

        httpCon.connect();
        //if it returns Not modified then we already have the content, return
        if( httpCon.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED ) {
            //disconnect() should only be used when you won't
            //connect to the same site in a while,
            //since it disconnects the socket. Only losing
            //the stream on an HTTP 1.1 connection will
            //maintain the connection waiting and be
            //faster the next time around
            File cacheFile = initCacheStore(path);
            LOGGER.log(Level.FINE, "Reading content of {0} from {1}", //NOI18N
                    new Object[] {u.toString(), cacheFile.getAbsolutePath()});
            isCached = true;
            return new org.xml.sax.InputSource( new BufferedInputStream( new FileInputStream(cacheFile) ) );
        }
        else {
            //obtain the encoding returned by the server
            String encoding = httpCon.getContentEncoding();
            LOGGER.log(Level.FINER, "Connection encoding: {0}", encoding); //NOI18N

            LOGGER.log(Level.FINER, "ETag: {0}", httpCon.getHeaderField("ETag")); //NOI18N

            InputStream is = null;
            if ("gzip".equalsIgnoreCase(encoding)) { //NOI18N
                is = new GZIPInputStream(httpCon.getInputStream());
            }
            else if ("deflate".equalsIgnoreCase(encoding)) { //NOI18N
                is = new InflaterInputStream(httpCon.getInputStream(), new Inflater(true));
            }
            else {
              is = httpCon.getInputStream();
            }
            LOGGER.log( Level.FINE, "Reading {0} from original source and caching", url ); //NOI18N
            return new org.xml.sax.InputSource(new CachingInputStream(is, path, httpCon.getHeaderField("Last-Modified"))); //NOI18N
        }
    }
    
        /** Inner class error catcher for handling SAXParseExceptions */
    static class ErrorCatcher implements org.xml.sax.ErrorHandler {
        private void message(Level level, org.xml.sax.SAXParseException e) {
            Logger l = Logger.getLogger(RSSFeed.class.getName());
            l.log(level, "Line number:"+e.getLineNumber()); //NOI18N
            l.log(level, "Column number:"+e.getColumnNumber()); //NOI18N
            l.log(level, "Public ID:"+e.getPublicId()); //NOI18N
            l.log(level, "System ID:"+e.getSystemId()); //NOI18N
            l.log(level, "Error message:"+e.getMessage()); //NOI18N
        }
        
        @Override
        public void error(org.xml.sax.SAXParseException e) {
            message(Level.SEVERE, e); //NOI18N
        }
        
        @Override
        public void warning(org.xml.sax.SAXParseException e) {
            message(Level.WARNING,e); //NOI18N
        }
        
        @Override
        public void fatalError(org.xml.sax.SAXParseException e) {
            message(Level.SEVERE,e); //NOI18N
        }
    } //end of inner class ErrorCatcher

    private class Reload extends Thread {
        @Override
        public void run() {
            try {
                lastReload = System.currentTimeMillis();

                List<FeedItem> itemList = buildItemList();
                final JPanel contentPanel = new JPanel( new GridBagLayout() );
                contentPanel.setOpaque( false );
                int contentRow = 0;

                Component header = getContentHeader();
                if( null != header ) {
                    contentPanel.add( header, new GridBagConstraints(0,contentRow++,1,1,0.0,0.0,
                                GridBagConstraints.CENTER,GridBagConstraints.BOTH,
                                new Insets(0,0,0,0),0,0 ) );
                }

                for( int i=0; i<Math.min(itemList.size(), getMaxItemCount()); i++ ) {
                    FeedItem item = itemList.get(i);

                    if( null != item.title && null != item.link ) {
                        
                        Component comp = createFeedItemComponent( item );

                        contentPanel.add( comp, new GridBagConstraints(0,contentRow++,1,1,1.0,0.0,
                                GridBagConstraints.NORTHWEST,GridBagConstraints.BOTH,
                                new Insets(contentRow==1 ? 0/*UNDER_HEADER_MARGIN*/ : 0,0,16,0),0,0 ) );
                    }
                }
                contentPanel.add( new JLabel(), new GridBagConstraints(0,contentRow++,1,1,0.0,1.0,
                                GridBagConstraints.CENTER,GridBagConstraints.VERTICAL,new Insets(0,0,0,0),0,0 ) );

                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
//                        contentPanel.setMinimumSize( contentPanel.getPreferredSize() );
                        setContent( contentPanel );
                    }
                });

                //schedule feed reload
                reloadTimer = RP.post( this, RSS_FEED_TIMER_RELOAD_MILLIS );

            } catch( UnknownHostException uhE ) {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        setContent( buildProxyPanel() );
                    }
                });
            } catch( SocketException sE ) {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        setContent( buildProxyPanel() );
                    }
                });
            } catch( IOException ioE ) {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        setContent( buildProxyPanel() );
                    }
                });
            } catch( Exception e ) {
                if( isContentCached()) {
                    isCached = false;
                    clearCache();
                    reload();
                    return;
                }
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        setContent( buildErrorLabel() );
                    }
                });
                ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
            }
        }
    }

    protected void clearCache() {
        try {
            NbPreferences.forModule( RSSFeed.class ).remove( url2path( new URL(url))) ;
        } catch( MalformedURLException mE ) {
            //ignore
        }
    }

    protected Component createFeedItemComponent( FeedItem item ) {
        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque( false );
        int row = 0;
        if( item.dateTime != null) {
            JLabel label = new JLabel();
            label.setFont( RSS_DESCRIPTION_FONT );
            label.setForeground( Utils.getColor(COLOR_RSS_DATE) );
            label.setText( formatDateTime( item.dateTime ) );
            panel.add( label, new GridBagConstraints(2,row,1,1,0.0,0.0,
                    GridBagConstraints.EAST,GridBagConstraints.NONE,
                    new Insets(0,TEXT_INSETS_LEFT+5,2,TEXT_INSETS_RIGHT),0,0 ) );
        }

        WebLink linkButton = new WebLink( stripHtml(item.title), item.link,
                Utils.getColor( COLOR_HEADER ), false );
        linkButton.getAccessibleContext().setAccessibleName( 
                BundleSupport.getAccessibilityName( "WebLink", item.title ) ); //NOI18N
        linkButton.getAccessibleContext().setAccessibleDescription( 
                BundleSupport.getAccessibilityDescription( "WebLink", item.link ) ); //NOI18N
        linkButton.setFont( BUTTON_FONT );
        panel.add( linkButton, new GridBagConstraints(0,row++,1,1,1.0,0.0,
                GridBagConstraints.WEST,GridBagConstraints.NONE,
                new Insets(0,0,2,TEXT_INSETS_RIGHT),0,0 ) );


        if (item.description != null) {
            JLabel label = new JLabel("<html>" + trimHtml(item.description) );
            label.setFont( RSS_DESCRIPTION_FONT );
            label.setForeground(Utils.getColor(COLOR_RSS_DETAILS));
            panel.add( label, new GridBagConstraints(0,row++,4,1,0.0,0.0,
                    GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,
                    new Insets(0,0,0,TEXT_INSETS_RIGHT),0,0 ) );
        }
        return panel;
    }

    protected static String getTextContent(Node node) {
        Node child = node.getFirstChild();
        if( null == child )
            return null;
        
        return child.getNodeValue();
    }

    protected String formatDateTime( String strDateTime ) {
        try {
            Date date = parsingDateFormat.parse( strDateTime );
            return printingDateFormatShort.format( date );
        } catch( NumberFormatException nfE ) {
            //ignore
        } catch( ParseException pE ) {
            try {
                Date date = parsingDateFormatShort.parse( strDateTime );
                return printingDateFormatShort.format( date );
            } catch( NumberFormatException nfE ) {
                //ignore
            } catch( ParseException otherPE ) {
                try {
                    Date date = parsingDateFormatLong.parse( strDateTime );
                    return printingDateFormatShort.format( date );
                } catch( NumberFormatException nfE ) {
                    //ignore
                } catch( ParseException e ) {
                    //ignore
                }
            }
        }
        return strDateTime;
    }
    
    private static final long serialVersionUID = 1L; 
    
    @Override
    public void removeNotify() {
        stopReloading();
        maxDescriptionChars = -1;
        super.removeNotify();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        getMaxDecsriptionLength();
        WindowManager.getDefault().invokeWhenUIReady( new Runnable() {
            @Override
            public void run() {
                startReloading();
            }
        });
    }

    protected void startReloading() {
        if( null == reloadTimer && !Boolean.getBoolean("netbeans.full.hack")) {
            if( System.currentTimeMillis() - lastReload >= RSS_FEED_TIMER_RELOAD_MILLIS ) {
                reload();
            } else {
                reloadTimer = RP.post( new Reload(),
                        Math.max(1, (int)(RSS_FEED_TIMER_RELOAD_MILLIS - (System.currentTimeMillis() - lastReload))) );
            }
        }
    }
    
    protected void stopReloading() {
        if( null != reloadTimer ) {
            reloadTimer.cancel();
            reloadTimer = null;
        }
    }
    
    private String trimHtml( String htmlSnippet ) {
        String res = stripHtml(htmlSnippet);
        int maxLen = getMaxDecsriptionLength();
        if( maxLen > 0 && res.length() > maxLen ) {
            res = res.substring( 0, maxLen ) + "..."; // NOI18N
        }
        return res;
    }
    
    private String stripHtml( String htmlSnippet ) {
        String res = htmlSnippet.replaceAll( "<[^>]*>", "" ); // NOI18N // NOI18N
        res = res.replaceAll( "&nbsp;", " " ); // NOI18N // NOI18N
        return res.trim();
    }
    
    protected int getMaxDecsriptionLength() {
        if( maxDescriptionChars < 0 && getWidth() > 0 ) {
            if( getWidth() <= 0 ) {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        getMaxDecsriptionLength();
                    }
                });
                return 200;
            }
            try {
                Graphics2D g = (Graphics2D)getGraphics();
                FontMetrics fm = g.getFontMetrics( RSS_DESCRIPTION_FONT );
                double charWidth = fm.getStringBounds( "Ab c", g ).getWidth() / 4;
                double feedWidth = getWidth() - 30;
                maxDescriptionChars = (int)(1.8 * feedWidth / charWidth);
            } catch( Throwable e ) {
                maxDescriptionChars = 200;
            }
        }
        return maxDescriptionChars;
    }

    protected Component getContentHeader() {
        return null;
    }

    private JComponent buildProxyPanel() {
        Component header = getContentHeader();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque( false );

        int row = 0;
        if( null != header ) {
            panel.add( header,  new GridBagConstraints(0,row++,1,1,1.0,0.0,
                    GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0 ) );
        }

        panel.add( new JLabel(BundleSupport.getLabel("ErrCannotConnect")),  // NOI18N
                new GridBagConstraints(0,row++,1,1,0.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,10,10,5),0,0 ) );
        if( showProxyButton ) {
            JButton button = new JButton();
            Mnemonics.setLocalizedText( button, BundleSupport.getLabel( "ProxyConfig" ) );  // NOI18N
            button.setOpaque( false );
            button.addActionListener( new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    HttpProxySettings.getDefault().showConfigurationDialog();
                }
            });
            panel.add( button, new GridBagConstraints(0,row++,1,1,0.0,0.0,
                    GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,10,10,5),0,0 ) );
        }
        return panel;
    }

    private JComponent buildContentLoadingLabel() {
        JLabel label = new JLabel( BundleSupport.getLabel( "ContentLoading" ) ); // NOI18N
        label.setHorizontalAlignment( JLabel.CENTER );
        label.setVerticalAlignment( JLabel.CENTER );
        label.setOpaque( false );
        Component header = getContentHeader();
        if( null != header ) {
            JPanel panel = new JPanel( new GridBagLayout() );
            panel.setOpaque( false );
            panel.add( header, new GridBagConstraints(0,0,1,1,1.0,1.0,
                GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0 ) );
            panel.add( label, new GridBagConstraints(0,1,1,1,1.0,1.0,
                GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0 ) );
            panel.setBorder( BorderFactory.createEmptyBorder(40, 0, 40, 0));
            return panel;
        }
        label.setBorder( BorderFactory.createEmptyBorder(40, 0, 40, 0));
        return label;
    }

    private JComponent buildErrorLabel() {
        Component header = getContentHeader();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque( false );

        int row = 0;
        if( null != header ) {
            panel.add( header,  new GridBagConstraints(0,row++,1,1,1.0,0.0,
                    GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0 ) );
        }

        panel.add( new JLabel(BundleSupport.getLabel("ErrLoadingFeed")),  // NOI18N
                new GridBagConstraints(0,row++,1,1,0.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,10,10,5),0,0 ) );
        JButton button = new JButton();
        Mnemonics.setLocalizedText( button, BundleSupport.getLabel( "Reload" ) );  // NOI18N
        button.setOpaque( false );
        button.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lastReload = 0;
                reload();
            }
        });
        panel.add( button, new GridBagConstraints(0,row++,1,1,0.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,10,10,5),0,0 ) );
        return panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if( HttpProxySettings.PROXY_SETTINGS.equals( evt.getPropertyName() ) ) {
            removeAll();
            add( buildContentLoadingLabel(), BorderLayout.CENTER );
            lastReload = 0;
            reload();
        }
    }
    
    public boolean isContentCached() {
        return isCached;
    }

    static class FeedHandler implements ContentHandler {
        private FeedItem currentItem;
        private StringBuffer textBuffer;
        private int maxItemCount;
        private ArrayList<FeedItem> itemList;
        
        public FeedHandler( int maxItemCount ) {
            this.maxItemCount = maxItemCount;
            itemList = new ArrayList<FeedItem>( maxItemCount );
        }

        @Override
        public void setDocumentLocator(Locator locator) {
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if( itemList.size() < maxItemCount ) {
                if( "item".equals( localName )
                        || "entry".equals( localName ) ) { // NOI18N
                    currentItem = new FeedItem();
                } else if( "link".equals( localName ) // NOI18N
                        || "pubDate".equals( localName ) // NOI18N
                        || "date".equals( localName ) // NOI18N
                        || "published".equals( localName ) // NOI18N
                        || "description".equals( localName ) // NOI18N
                        || "content".equals( localName ) // NOI18N
                        || "title".equals( localName ) ) { // NOI18N
                    textBuffer = new StringBuffer( 110 );

                    if( "link".equals(localName) && null != currentItem && null != atts.getValue("href") )
                        currentItem.link = fixFeedItemUrl(atts.getValue("href"));
                } else if( "enclosure".equals( localName ) && null != currentItem ) { //NOI18N
                    currentItem.enclosureUrl = atts.getValue( "url" ); //NOI18N
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if( itemList.size() < maxItemCount ) {
                if( "item".equals( localName )
                        || "entry".equals( localName ) ) { // NOI18N
                    if( null != currentItem && currentItem.isValid() ) {
                        itemList.add( currentItem );
                    }
                    currentItem = null;
                } else if( null != currentItem && null != textBuffer ) {
                    String text = textBuffer.toString().trim();
                    textBuffer = null;
                    if( 0 == text.length() )
                        text = null;

                    if( "link".equals( localName ) && null == currentItem.link ) { // NOI18N
                        currentItem.link = fixFeedItemUrl(text);
                    } else if( "pubDate".equals( localName ) // NOI18N
                            || "published".equals( localName )
                            || "date".equals( localName ) ) { // NOI18N
                        currentItem.dateTime = text;
                    } else if( "title".equals( localName ) ) { // NOI18N
                        currentItem.title = text;
                    } else if( "description".equals( localName )
                            || "content".equals(localName) ) { // NOI18N
                        currentItem.description = text;
                    }
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if( null != textBuffer )
                textBuffer.append( ch, start, length );
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
        }

        public ArrayList<FeedItem> getItemList() {
            return itemList;
        }

        protected String fixFeedItemUrl(String url) {
            if( null != url && (url.contains(".netbeans.org") || url.contains("/netbeans.org")) ) {//NOI18N
                if( url.contains("?") ) {
                    url = url + "&";
                } else {
                    url = url + "?";
                }
                url += "utm_source=netbeans&utm_campaign=welcomepage";
            }
            return url;
        }
    }

    protected static class FeedItem {
        public String title;
        public String link;
        public String description;
        public String dateTime;
        public String enclosureUrl;

        public boolean isValid() {
            return null != title && null != link;
        }
    }

    static class CachingInputStream extends FilterInputStream {
        private OutputStream os;
        private String modTime;
        private String path;
        
        CachingInputStream (InputStream is, String path, String time) 
        throws IOException {
            super(is);
            File storage = initCacheStore(path);
            os = new BufferedOutputStream(new FileOutputStream(storage));
            modTime = time;
            this.path = path;
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (modTime != null)
                NbPreferences.forModule(RSSFeed.class).put(path, modTime);
            os.close();
        }

        @Override
        public int read() throws IOException {
            int val = super.read();
            os.write(val);
            return val;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int res = super.read(b, off, len);
            if (res != -1) {
                os.write(b, off, res);
            }
            return res;
        }
    }
}
