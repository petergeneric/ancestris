/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.fo;

import ancestris.api.core.Version;
import genj.util.ImageSniffer;
import genj.util.Resources;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.openide.util.Lookup;

/**
 * An abstract layer above docbook handling and transformations.
 *
 * <p>
 * Some methods have an attributes parameter that allows additional control over
 * the formatting. The attributes refer to the underlying XSL Format
 * representation, from which various formats of document can be generated, such
 * as PDF and HTML. Using them effectively require knowledge of XSL FO (see
 * below) and the internals of the method.
 *
 * <p>
 * Some useful resources about XSL FO and the Apache FOP we use to process it
 * are:
 * <ul>
 * <li><a href="http://www.w3.org/TR/xsl11/">The W3C specification of XSL FO</a>
 * <li><a href="http://xmlgraphics.apache.org/fop/resources.html">The Apache FOP
 * Resources page</a>
 * <li><a ref="http://www.renderx.com/demos/src_examples.html">Samples at
 * renderx.com</a>
 * </ul>
 */
public class Document {

    /**
     * Symbolic constant for font size for sections.
     *
     * @see #setSectionSizes
     * @see #startSection(String, String, int)
     */
    public final static int FONT_XX_SMALL = 0;
    /**
     * Symbolic constant for font size for sections.
     */
    public final static int FONT_X_SMALL = 1;
    /**
     * Symbolic constant for font size for sections.
     */
    public final static int FONT_SMALL = 2;
    /**
     * Symbolic constant for font size for sections.
     */
    public final static int FONT_MEDIUM = 3;
    /**
     * Symbolic constant for font size for sections.
     */
    public final static int FONT_LARGE = 4;
    /**
     * Symbolic constant for font size for sections.
     */
    public final static int FONT_X_LARGE = 5;
    /**
     * Symbolic constant for font size for sections.
     */
    public final static int FONT_XX_LARGE = 6;

    private final static Resources RESOURCES = Resources.get(Document.class);

    /**
     * matching a=b,c-d=e,f:g=h,x=y(m,n,o),z=1
     */
    protected final static Pattern REGEX_ATTR = Pattern.compile("([^,]+)=([^,\\(]*(\\(.*?\\))?)");

    /**
     * xsl fo namespace URI
     */
    private final static String NS_XSLFO = "http://www.w3.org/1999/XSL/Format",
            NS_GENJ = "http://genj.sourceforge.net/XSL/Format";

    private final static String FORMAT_SECTION = "font-weight=bold,space-before=0.5cm,space-after=0.2cm,keep-with-next.within-page=always";
    private final static String FORMAT_SECTION_LARGER = "font-size=larger," + FORMAT_SECTION;
    private static final String[] fontSizes = new String[]{
        "xx-small", "x-small", "small", "medium", "large", "x-large", "xx-large"
    };

    private org.w3c.dom.Document doc;
    private Element cursor;
    private String title;
    private boolean needsTOC = false;
    private final Map<File, List<Element>> file2elements = new HashMap<>();
    private final List<TOCEntry> toc = new ArrayList<>();
    private int minSectionFontSize;
    private int maxSectionFontSize;
    private final Map<String, Map<String, Map<String, List<Element>>>> index2primary2secondary2elements = new TreeMap<>();
    private int idSequence = 0;
    private boolean containsCSV = false;

    /**
     * Constructor
     *
     * @param title
     */
    public Document(String title) {
        createDoc(title, null, 0, FONT_MEDIUM, FONT_XX_LARGE, 0);
    }

    public Document(String title, String fontFamily, int fontSize, int sizeMin, int sizeMax, int orientation) {
        createDoc(title, fontFamily, fontSize, sizeMin, sizeMax, orientation);
    }

    private void createDoc(String title, String fontFamily, int fontSize, int sizeMin, int sizeMax, int orientation) {
        // remember title
        this.title = title;

        // section size range
        setSectionSizes(sizeMin, sizeMax);
        // create a dom document
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            doc = dbf.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException t) {
            throw new RuntimeException(t);
        }

        // boilerplate
        //  <root>
        //   <layout-master-set>
        //    <simple-page-master>
        //     <region-body/>
        //     <region-end/>
        //    </simple-page-master>
        //   </layout-master-set>
        //   <page-sequence>
        //    <title>
        //    <flow>
        //     <block/>
        //    </flow>
        //   </page-sequence>
        cursor = (Element) doc.appendChild(doc.createElementNS(NS_XSLFO, "root"));
        cursor.setAttribute("xmlns", NS_XSLFO);
        cursor.setAttribute("xmlns:genj", NS_GENJ);
        if (fontFamily != null) {
            cursor.setAttribute("font-family", fontFamily);
        } else {
            cursor.setAttribute("font-family", "Arial");
        }
        if (fontSize != 0) {
            cursor.setAttribute("font-size", "" + fontSize);
        }

        push("layout-master-set");
        if (orientation == 0) {
            push("simple-page-master", "master-name=master,margin-top=1cm,margin-bottom=1cm,margin-left=1cm,margin-right=1cm");
        } else {
            push("simple-page-master", "master-name=master,margin-top=1cm,margin-bottom=1cm,margin-left=1cm,margin-right=1cm, page-height=21cm, page-width=29.7cm");
        }
        push("region-body", "margin-bottom=1cm").pop();
        push("region-after", "extent=0.8cm").pop();
        pop().pop().push("page-sequence", "master-reference=master");
        push("title").text(getTitle(), "").pop();

        /*
      Paul Grosso offers this suggestion for left-center-right header formatting
      at http://www.dpawson.co.uk/xsl/sect3/headers.html#d13432e123:

    <fo:static-content flow-name="xsl-region-before">
        <!-- header-width is the width of the full header in picas -->
        <xsl:variable name="header-width" select="36"/>
        <xsl:variable name="header-field-width">
        <xsl:value-of select="$header-width * 0.3333"/><xsl:text>pc</xsl:text>
        </xsl:variable>
        <fo:list-block font-size="8pt" provisional-label-separation="0pt">
            <xsl:attribute name="provisional-distance-between-starts">
                <xsl:value-of select="$header-field-width"/>
            </xsl:attribute>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block text-align="left">
                        <xsl:text>The left header field</xsl:text>
                    </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:list-block provisional-label-separation="0pt">
                        <xsl:attribute
                     name="provisional-distance-between-starts">
                            <xsl:value-of select="$header-field-width"/>
                        </xsl:attribute>
                        <fo:list-item>
                            <fo:list-item-label end-indent="label-end()">
                                <fo:block text-align="center">
                                    <fo:page-number/>
                                </fo:block>
                            </fo:list-item-label>
                            <fo:list-item-body start-indent="body-start()">
                                <fo:block text-align="right">
                                    <xsl:text>The right header field</xsl:text>
                                </fo:block>
                            </fo:list-item-body>
                        </fo:list-item>
                    </fo:list-block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>
    </fo:static-content>
         */
        push("static-content", "flow-name=xsl-region-after");
        push("block", "text-align=left,font-size=7,space-before=2cm");
        text(" Ancestris / "  + Lookup.getDefault().lookup(Version.class).getVersionString() + " : " + (new SimpleDateFormat("yyyy-MM-dd")).format(new Date(System.currentTimeMillis()))+"    ", "");
        pop(); // </block>
        push("block", "text-align=right");
        push("page-number").pop();
        pop(); // </block>
        pop(); // </static-content>

        push("flow", "flow-name=xsl-region-body");
        push("block");

        // done - cursor points to first block
    }

    /**
     * Sets the range of logical font sizes to be used for section headings. The
     * outermost section (depth 1) has size {@link #FONT_XX_LARGE} by default,
     * with each nested section having a smaller font, until minSize (default
     * {@link #FONT_MEDIUM}) is reached, after which all section headings appear
     * the same.
     *
     * @param minSize Smallest size for nested section headings
     * @param maxSize Largest size for outermost section headings
     * @see #startSection(String, String, int)
     */
    public void setSectionSizes(int minSize, int maxSize) {
        if (minSize < 0 || minSize > maxSize || maxSize > fontSizes.length - 1) {
            throw new IllegalArgumentException("setSectionSizes(" + minSize + "," + maxSize + ")");
        }
        minSectionFontSize = minSize;
        maxSectionFontSize = maxSize;
    }

    /**
     * Check if there's any CSV in this document
     */
    protected boolean containsCSV() {
        return containsCSV;
    }

    /**
     * String representation - the title
     */
    @Override
    public String toString() {
        return getTitle();
    }

    /**
     * Closes the document finalizing output
     */
    protected void close() {

        // closed already?
        if (cursor == null) {
            return;
        }

        // generate indexes
        indexes();

        // generate TOC
        if (needsTOC) {
            toc();
        }

        // done
        cursor = null;
    }

    /**
     * Title access
     */
    public String getTitle() {
        return title;
    }

    /**
     * Access to DOM source
     */
    /*package*/ DOMSource getDOMSource() {
        return new DOMSource(doc);
    }

    /**
     * Add Table of Content
     */
    public Document addTOC() {
        needsTOC = true;
        // done
        return this;
    }

    /**
     * Add a Table of Content entry for current cursor position
     */
    public Document addTOCEntry(String title) {
        addTOC();
        // add anchor
        String id = "toc" + toc.size();
        addAnchor(id);
        // remember entry
        toc.add(new TOCEntry(id, title));
        // done
        return this;
    }

    /**
     * Outermost section with largest font size is 1.
     *
     * @param sectionDepth
     * @return XSL-FO font-size parameter
     */
    private String getFontSize(int sectionDepth) {
        int i = maxSectionFontSize + 1 - sectionDepth;
        if (i < minSectionFontSize) {
            i = minSectionFontSize;
        }
        return fontSizes[i];
    }

    /**
     * Add section at specified depth. The depth is used to determine the font
     * size and should in the future be used for numbering in X.Y.Z format. 1 is
     * the usual outermost section and maps to {@link #FONT_XX_LARGE} by
     * default.
     * <a href="http://www.w3.org/TR/REC-CSS2/fonts.html#font-styling">http://www.w3.org/TR/REC-CSS2/fonts.html#font-styling</a>
     * describes the meaning of logical font sizes in XSL/FO.
     *
     * @see #setSectionSizes
     */
    public Document startSection(String title, String id, int sectionDepth) {

        // check if
        if (id != null && id.startsWith("_")) {
            throw new IllegalArgumentException("underscore is reserved for internal IDs");
        }

        // return to the last block in flow
        pop("flow", "addSection() is not applicable outside document flow");
        cursor = (Element) cursor.getLastChild();

        // generate an id if necessary
        if (id == null || id.length() == 0) {
            id = "toc" + toc.size();
        }

        // start a new block
        String fontSize = getFontSize(sectionDepth);
        pop().push("block", "font-size=" + fontSize + "," + FORMAT_SECTION + ",id=" + id);

        // remember
        toc.add(new TOCEntry(id, title));

        // add the title
        addText(title);

        // create the following block
        nextParagraph();

        // done
        return this;
    }

    /**
     * Add section at depth 1.
     */
    public Document startSection(String title, String id) {
        return startSection(title, id, 1);
    }

    /**
     * Add section at specified depth with reference to a GEDCOM entity. This is
     * a quick fix, We should provide some LinkTarget interface and implement it
     * in Entity class
     */
//  public Document startSection(String title, Entity entity, int sectionDepth) {
//    return startSection(title,entity, sectionDepth);
//  }
    /**
     * Add section at depth.
     */
//  public Document startSection(String title, Entity entity) {
//    return startSection(title,entity);
//  }
    /**
     * Add section at specified depth.
     */
    public Document startSection(String title, int sectionDepth) {
        return startSection(title, "", sectionDepth);
    }

    /**
     * Add section at depth 1.
     */
    public Document startSection(String title) {
        return startSection(title, "");
    }

    /**
     * Add an index entry
     */
    public Document addIndexTerm(String index, String primary) {
        return addIndexTerm(index, primary, "");
    }

    /**
     * Add an index entry
     */
    public Document addIndexTerm(String index, String primary, String secondary) {

        // check index
        if (index == null) {
            throw new IllegalArgumentException("addIndexTerm() requires name of index");
        }
        index = index.trim();
        if (index.length() == 0) {
            throw new IllegalArgumentException("addIndexTerm() name of index can't be empty");
        }

        // check primary - ignore indexterm if empty
        primary = trimIndexTerm(primary);
        if (primary.length() == 0) {
            return this;
        }

        // check secondary
        secondary = trimIndexTerm(secondary);

        // remember
        Map<String, Map<String, List<Element>>> primary2secondary2elements = index2primary2secondary2elements.get(index);
        if (primary2secondary2elements == null) {
            primary2secondary2elements = new TreeMap<>();
            index2primary2secondary2elements.put(index, primary2secondary2elements);
        }
        Map<String, List<Element>> secondary2elements = primary2secondary2elements.get(primary);
        if (secondary2elements == null) {
            secondary2elements = new TreeMap<>();
            primary2secondary2elements.put(primary, secondary2elements);
        }
        List<Element> elements = secondary2elements.get(secondary);
        if (elements == null) {
            elements = new ArrayList<>();
            secondary2elements.put(secondary, elements);
        }

        // add anchor - normally that would be a fo:inline 
        //   push("inline", "id=_"+(++idSequence));
        // but FOP doesn't support IDs on those elements
        // so instead we attach an id to the surrounding block
        String id = cursor.getAttribute("id");
        if (id.length() == 0) {
            id = "" + (++idSequence);
            cursor.setAttribute("id", id);
        }

        // remember the element for primary+secondary if the element isn't in there already
        if (!elements.contains(cursor)) {
            elements.add(cursor);
        }

        return this;
    }

    private String trimIndexTerm(String term) {
        // null?
        if (term == null) {
            return "";
        }
        // remove anything after (
        int bracket = term.indexOf('(');
        if (bracket >= 0) {
            term = term.substring(0, bracket);
        }
        // remove anything after ,
        int comma = term.indexOf('(');
        if (comma >= 0) {
            term = term.substring(0, comma);
        }
        // trim
        return term.trim();
    }

    /**
     * Add text
     */
    public Document addText(String text) {
        return addText(text, "");
    }

    /**
     * Add text with given CSS styling. See
     * <a href="http://www.w3.org/TR/REC-CSS2/fonts.html#font-styling">http://www.w3.org/TR/REC-CSS2/fonts.html#font-styling</a>
     */
    public Document addText(String text, String atts) {
        text(text, atts);
        return this;
    }

    /**
     * Add image file reference to the document
     *
     * @param file the file pointing to the image
     * @param atts fo attributes for the image
     */
    public Document addImage(File file, String atts) {

        //TODO: Have to manage remote files.
        if (file == null || !file.exists()) {
            return this;
        }

        // check dimension - let's not make this bigger than 1x1 inch
        Dimension2D dim = new ImageSniffer(file).getDimensionInInches();
        if (dim == null) {
            return this;
        }
        if (dim.getWidth() > dim.getHeight()) {
            if (dim.getWidth() > 1) {
                atts = "width=1in,content-width=scale-to-fit," + atts; // can be overriden
            }
        } else {
            if (dim.getHeight() > 1) {
                atts = "height=1in,content-height=scale-to-fit," + atts; // can be overriden
            }
        }

        //  <fo:external-graphic src="file:///theFilewithonlyslashinpath"/> 
        push("external-graphic", "src=file:///" + file.getAbsolutePath().replace('\\', '/') + "," + atts);

        // remember file in case a formatter wants to resolve file location later
        List<Element> elements = file2elements.get(file);
        if (elements == null) {
            elements = new ArrayList<>(3);
            file2elements.put(file, elements);
        }
        elements.add(cursor);

        // back to enclosing block
        pop();

        // add opportunity to line break
        addText(" ");

        // done
        return this;
    }

    /**
     * Access to external image files
     */
    protected File[] getImages() {
        Set<File> files = file2elements.keySet();
        return files.toArray(new File[files.size()]);
    }

    /**
     * Replace referenced image file with a calculated value
     */
    protected void setImage(File file, String value) {
        List<Element> nodes = file2elements.get(file);
        for (Element external : nodes) {
            external.setAttribute("src", value);
        }
    }

    /**
     * Add a paragraph
     */
    public Document nextParagraph() {
        return nextParagraph("");
    }

    /**
     * Add a paragraph
     */
    public Document nextParagraph(String format) {

        // start a new block if the current is not-empty
        if (cursor.getFirstChild() != null) {
            pop().push("block", format);
        } else {
            attributes(cursor, format);
        }

        return this;
    }

    /**
     * Start a list
     */
    public Document startList() {
        return startList("");
    }

    /**
     * Start a list
     */
    public Document startList(String format) {

        //<list-block>
        pop();
        push("list-block", "provisional-distance-between-starts=0.6em, provisional-label-separation=0pt," + format);
        nextListItem();

        return this;
    }

    /**
     * Add a list item
     */
    public Document nextListItem() {
        return nextListItem("");
    }

    /**
     * Add a list item
     */
    public Document nextListItem(String format) {

        //<list-block>
        //  <list-item>
        //    <list-item-label end-indent="label-end()"><block>&#x2022;</block></list-item-label>
        //    <list-item-body start-indent="body-start()">
        //       <block/>
        //    </list-item-body>
        //  </list-item>
        // check containing list-block
        Element list = peek("list-block", "nextListItem() is not applicable outside list block");

        // a list with only one item containing an empty block?
        if (list.getChildNodes().getLength() == 1 && cursor.getFirstChild() == null && cursor.getPreviousSibling() == null && cursor.getParentNode().getLocalName().equals("list-item-body")) {
            // delete list-item and start over
            list.removeChild(list.getFirstChild());
        }

        // continue with list
        cursor = list;

        // find out what 'bullet' to use
        String label = attribute("genj:label", format);
        if (label != null) {
            // check provisional-distance-between-starts - we assume a certain 'em' per label character
            String dist = list.getAttribute("provisional-distance-between-starts");
            if (dist.endsWith("em")) {
                float len = label.length() * 0.6F;
                if (Float.parseFloat(dist.substring(0, dist.length() - 2)) < len) {
                    list.setAttribute("provisional-distance-between-starts", len + "em");
                }
            }
        } else {
            label = "\u2219";  // &bullet; /u2219 works in JEditPane, &bull; \u2022 doesn't
        }

        // add new item
        push("list-item");
        push("list-item-label", "end-indent=label-end()");
        push("block");
        text(label, "");
        pop();
        pop();
        push("list-item-body", "start-indent=body-start()");
        push("block");

        return this;
    }

    /**
     * End a list
     */
    public Document endList() {

        // *
        //  <list-block>
        pop("list-block", "endList() is not applicable outside list-block").pop();
        push("block", "");

        return this;
    }

    /**
     * Start a table
     */
    public Document startTable() {
        return startTable("width=100%,border=0.5pt solid black");
    }

    public Document startTable(String format) {

        // patch format - FOP only supports table-layout=fixed
        format = "table-layout=fixed," + format;

        //<table>
        // <table-header>
        //  <table-row>
        //   <table-cell>
        //    <block>
        //    ...
        // </table-header>
        // <table-body>
        //  <table-row>
        //   <table-cell>
        //    <block>    
        //    ...
        push("table", format);
        Element table = cursor;

        // mark as cvs if applicable - non fo namespace attributes won't be picked up by push() and attributes()
        if ("true".equals(attribute("genj:csv", format))) {
            containsCSV = true;
            cursor.setAttributeNS(NS_GENJ, "genj:csv", "true");

            String prefix = attribute("genj:csvprefix", format);
            if (prefix != null) {
                cursor.setAttributeNS(NS_GENJ, "genj:csvprefix", prefix);
            }
        }

        // head/body & row
        if (format.contains("genj:header=true")) {
            push("table-header");
            push("table-row", "color=#ffffff,background-color=#c0c0c0,font-weight=bold");
        } else {
            push("table-body");
            push("table-row");
        }

        // cell and done
        push("table-cell", "border=" + table.getAttribute("border"));
        push("block");

        return this;
    }

    /**
     * Add a column to the table (this is not necessary)
     */
    public Document addTableColumn(String atts) {

        //<table>
        // <table-column/>
        Element save = cursor;

        // find the enclosing table
        pop("table", "addTableColumn() is not applicable outside enclosing table");

        // find last table definition
        Node before = cursor.getFirstChild();
        while (before != null && before.getNodeName().equals("table-column")) {
            before = before.getNextSibling();
        }

        push("table-column", atts, before);

        // done for now
        cursor = save;

        return this;
    }

    /**
     * Jump to next cell in table
     */
    public Document nextTableCell() {
        return nextTableCell("");
    }

    public Document nextTableCell(String atts) {

        // peek at current cell - stay with it IF
        //  + it's the first cell in the row 
        //  + the current cursor points at the first child (a block)
        //  + the block pointed by cursor is empty
        Element cell = peek("table-cell", "nextTableCell() is not applicable outside enclosing table");
        if (cell.getPreviousSibling() == null && cursor == cell.getFirstChild() && !cursor.hasChildNodes()) {
            attributes(cell, atts);
            // add empty content to block so another call to nextTableCell() willl actually move forward
            push("inline", "").pop();
            return this;
        }

        // peek at row
        Element row = peek("table-row", "nextTableCell() is not applicable outside enclosing table row");
        int cells = row.getElementsByTagName("table-cell").getLength();

        // peek at table - add new row if we have all columns already
        Element table = peek("table", "nextTableCell() is not applicable outside enclosing table");
        int cols = table.getElementsByTagName("table-column").getLength();
        if (cols > 0 && cells == cols) {
            return nextTableRow();
        }

        // pop to row
        pop("table-row", "nextTableCell() is not applicable outside enclosing table row");

        // 20060215 wanted to use border=inherit here but that would require table-row
        // and table-body to have border=inherit as well. table-body can't have a
        // border property in a table with border-collapse=separate (which is the only
        // model FOP supports).
        // So we're simply doing our own 'inherit' here :) Alternative would be to do
        // us border=from-nearest-specified-value() on each cell or border=inherit
        // on the table-columns and then border=from-table-column() on the cells.
        // add now
        push("table-cell", "border=" + table.getAttribute("border") + ", border-style=" + table.getAttribute("border-style") + ", border-color=" + table.getAttribute("border-color") + "," + atts);
        push("block");

        // done 
        return this;
    }

    /**
     * Jump to next row in table
     */
    public Document nextTableRow() {
        return nextTableRow("");
    }

    public Document nextTableRow(String atts) {

        // peek at current cell - stay with it IF
        //  + it's the first cell in the row 
        //  + the current cursor points at the first child (a block)
        //  + the block pointed by cursor is empty
        Element cell = peek("table-cell", "nextTableRow() is not applicable outside enclosing table");
        if (cell.getPreviousSibling() == null && cursor == cell.getFirstChild() && !cursor.hasChildNodes()) {
            attributes((Element) cell.getParentNode(), atts);
            return this;
        }

        // pop to table
        pop("table", "nextTableRow() is not applicable outside enclosing table");
        Element table = cursor;

        // last child is already table-body?
        if (table.getLastChild().getNodeName().equals("table-body")) {
            cursor = (Element) table.getLastChild();
        } else {
            push("table-body");
        }

        // add row
        push("table-row", atts);

        // add cell
        push("table-cell", "border=" + table.getAttribute("border") + ", border-style=" + table.getAttribute("border-style") + ", border-color=" + table.getAttribute("border-color"));
        push("block");

        // done
        return this;
    }

    /**
     * Jump to next cell in table
     */
    public Document endTable() {

        // leave table
        pop("table", "endTable() is not applicable outside enclosing table").pop();

        // done
        return this;
    }

    /**
     * Force a page break
     */
    public Document nextPage() {
        pop();
        push("block", "page-break-before=always");
        return this;
    }

    /**
     * Add an anchor
     */
    public Document addAnchor(String id) {
        if (id.startsWith("_")) {
            throw new IllegalArgumentException("underscore is reserved for internal IDs");
        }
        // normally I'd use fo:inline for anchors but FOP can't handle IDs on those elements
        // so i have to use block here - since the EditorPane uses extra space even for
        // empty blocks i'm trying to reuse the current block here IF it doesn't have an ID
        // already
        if (cursor.getAttribute("id").length() == 0) {
            cursor.setAttribute("id", id);
        } else {
            push("block", "id=" + id).pop();
        }
        return this;
    }

    /**
     * Add an anchor
     */
//  public Document addAnchor(Entity entity) {
//    return addAnchor(entity);
//  }
    /**
     * Add a link
     */
    public Document addExternalLink(String text, String id) {

        // <basic-link external-destination="...">text</basic-link>
        push("basic-link", "external-destination=" + id);
        text(text, "");
        pop();
        // done
        return this;
    }

    /**
     * Add a link
     */
    public Document addLink(String text, String id) {

        // <basic-link>text</basic-link>
        push("basic-link", "internal-destination=" + id);
        text(text, "");
        pop();
        // done
        return this;
    }

    /**
     * Add indexes
     */
    private Document indexes() {

        // loop over indexes
        for (String index : index2primary2secondary2elements.keySet()) {
            Map<String, Map<String, List<Element>>> primary2secondary2elements = index2primary2secondary2elements.get(index);

            // add section
            nextPage();
            startSection(index);
            push("block", "start-indent=1cm");

            // loop over primaries
            for (String primary : primary2secondary2elements.keySet()) {
                Map<String, List<Element>> secondary2elements = primary2secondary2elements.get(primary);

                // add block and primary
                push("block", "");
                text(primary + " ", "");

                // loop over secondaries
                for (String secondary : secondary2elements.keySet()) {
                    List<Element> elements = secondary2elements.get(secondary);

                    if (secondary.length() > 0) {
                        push("block", "start-indent=2cm"); //start-indent?
                        text(secondary + " ", "");
                    }

                    // loop over elements
                    for (int e = 0; e < elements.size(); e++) {
                        if (e > 0) {
                            text(", ", "");
                        }
                        Element element = elements.get(e);
                        String id = element.getAttribute("id");

                        push("basic-link", "internal-destination=" + id);
                        push("page-number-citation", "ref-id=" + id);
                        cursor.setAttributeNS(NS_GENJ, "genj:citation", Integer.toString(e + 1));
                        pop();
                        pop();
                    }

                    if (secondary.length() > 0) {
                        pop();
                    }
                    // next
                }

                // next
                pop();
            }

            // next
            pop();
        }

        // done
        return this;
    }

    /**
     * Add Table of content
     */
    private Document toc() {

        // anything to do?
        if (toc.isEmpty()) {
            return this;
        }
        Element old = cursor;

        // pop back to flow
        pop("flow", "can't create TOC without enclosing flow");

        // add block for toc AS FIRST child
        push("block", "", cursor.getFirstChild());

        //<block>
        //  Table of Contents
        //  <block>
        //    Title 1<leader/><page-number-citation/>
        //   </block>
        //   ...
        //</block>
        // add toc header
        push("block", FORMAT_SECTION_LARGER);
        text(RESOURCES.getString("toc"), "");
        pop();

        // add toc entries
        addAnchor("toc");
        for (TOCEntry entry : toc) {
            push("block", "start-indent=1cm,end-indent=1cm,text-indent=0cm,text-align=left");
            nextParagraph("space-after=0.2cm");
            addLink(entry.text, entry.id);
            push("leader", "leader-pattern=dots").pop();
            push("page-number-citation", "ref-id=" + entry.id).pop();
            pop();
        }

        // done
        cursor = old;
        return this;
    }

    /**
     * Add qualified element to parent
     */
    private Document push(String name) {
        return push(name, "");
    }

    /**
     * Add qualified element to parent
     */
    private Document push(String name, String attributes) {
        return push(name, attributes, null);
    }

    /**
     * Add qualified element to parent
     */
    private Document push(String name, String attributes, Node before) {
        // create it, set attributes and hook it up
        Element elem = doc.createElementNS(NS_XSLFO, name);
        if (before != null) {
            cursor.insertBefore(elem, before);
        } else {
            cursor.appendChild(elem);
        }
        cursor = elem;
        // attribute it and done
        return attributes(elem, attributes);
    }

    /**
     * Set attributes on current element
     */
    private Document attributes(Element elem, String format) {
        // parse attributes
        Matcher m = REGEX_ATTR.matcher(format);
        while (m.find()) {
            // accept only fo attributes here
            String key = m.group(1).trim();
            if (key.indexOf(':') < 0) {
                String val = m.group(2).trim();
                elem.setAttribute(key, val);
            }
        }
        // done
        return this;
    }

    /**
     * find an attribute in format
     */
    private String attribute(String key, String format) {
        // parse attributes
        Matcher m = REGEX_ATTR.matcher(format);
        while (m.find()) {
            if (m.group(1).trim().equals(key)) {
                return m.group(2).trim();
            }
        }
        // not found
        return null;
    }

    /**
     * Add text element
     */
    private Document text(String text, String atts) {

        // ignore empties
        if (text.length() == 0) {
            return this;
        }

        // create a text node for it
        Node txt = doc.createTextNode(text);
        if (atts.length() > 0) {
            push("inline", atts);
            cursor.appendChild(txt);
            pop();
        } else {
            cursor.appendChild(txt);
        }
        return this;
    }

    /**
     * pop element from stack
     */
    private Document pop() {
        cursor = (Element) cursor.getParentNode();
        return this;
    }

    /**
     * pop element from stack
     */
    private Document pop(String qname, String error) {
        cursor = peek(qname, error);
        return this;
    }

    /**
     * find element in current stack upwards
     */
    private Element peek(String qname, String error) {
        Node loop = cursor;
        while (loop instanceof Element) {
            if (loop.getLocalName().equals(qname)) {
                return (Element) loop;
            }
            loop = loop.getParentNode();
        }
        throw new IllegalArgumentException(error);
    }

    /**
     * A table of content entry
     */
    private class TOCEntry {

        String id;
        String text;

        private TOCEntry(String id, String text) {
            this.id = id;
            this.text = text;
        }
    }

}
