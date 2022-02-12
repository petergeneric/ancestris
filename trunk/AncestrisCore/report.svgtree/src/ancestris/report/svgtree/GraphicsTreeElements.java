/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.report.svgtree;

import ancestris.core.TextOptions;
import ancestris.report.svgtree.graphics.GraphicsUtil;
import ancestris.report.svgtree.output.TreeElements;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.renderer.MediaRenderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

/**
 * Draws tree elements to a Graphics2D object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class GraphicsTreeElements implements TreeElements {

    /**
     * Minimal indibox width in pixels.
     */
    private static final int DEFAULT_INDIBOX_WIDTH = 110;

    /**
     * Minimal indibox width in pixels when "shrink mode" is enabled..
     */
    private static final int SHRINKED_INDIBOX_WIDTH = 50;

    /**
     * Indibox height in pixels.
     */
    private static final int DEFAULT_INDIBOX_HEIGHT = 64;

    /**
     * Minimal family box width in pixels.
     */
    private static final int DEFAULT_FAMBOX_WIDTH = 100;

    /**
     * Minimal family box height in pixels.
     */
    private static final int DEFAULT_FAMBOX_HEIGHT = 27;

    /**
     * Width of the image inside an individual box.
     */
    private static final int MAX_IMAGE_WIDTH = 50;

    private static final int DEFAULT_INDIBOX_LINES = 2;
    private static final int DEFAULT_FAMBOX_LINES = 1;
    private static final int TEXT_MARGIN = 5;
    private static final int NAME_LINE_HEIGHT = 12;
    private static final int LINE_HEIGHT = 10;
    private static final TagPath PATH_INDIBIRTPLAC = new TagPath("INDI:BIRT:PLAC");
    private static final TagPath PATH_INDIDEATPLAC = new TagPath("INDI:DEAT:PLAC");
    private static final TagPath PATH_INDIOCCU = new TagPath("INDI:OCCU");
    private static final TagPath PATH_INDITITL = new TagPath("INDI:TITL");
    private static final TagPath PATH_FAMMARRPLAC = new TagPath("FAM:MARR:PLAC");
    private static final TagPath PATH_FAMDIVPLAC = new TagPath("FAM:DIV:PLAC");

    /**
     * Factor for scaling images to achieve higher quality in the PDF renderer.
     */
    private final double IMAGE_SCALE_FACTOR = 4;

    /**
     * Used to determine text width.
     */
    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, false, false);

    private static final float STROKE_WIDTH = 2.0f;

    /**
     * Male sex symbol.
     */
    private static final String MALE_SYMBOL = "\u2642";

    /**
     * Female sex symbol.
     */
    private static final String FEMALE_SYMBOL = "\u2640";

    /**
     * Unknown sex symbol.
     */
    private static final String UNKNOWN_SYMBOL = "?";

    /**
     * Stroke for drawing dashed lines.
     */
    private static final Stroke DASHED_STROKE = new BasicStroke(STROKE_WIDTH,
            BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
            new float[]{3.0f, 6.0f}, 0.0f);

    /**
     * Font for drawing the sex symbols.
     */
    private static Font sexSymbolFont = new Font("SansSerif", Font.PLAIN, 10);

    static {
        // Find a font with the MALE_SYMBOL in it
        String[] candidateFontNames = {"sansserif", "apple symbol", "symbol"};
        for (String candidateFontName : candidateFontNames) {
            Font candidateFont = new Font(candidateFontName, Font.PLAIN, 10);
            if (candidateFont.canDisplay(MALE_SYMBOL.charAt(0))) {
                sexSymbolFont = candidateFont;
                break;
            }
        }
    }

    /**
     * Font name. line and last name is in the second.
     */
    public int fontNameDetail = 0;
    public String fontNameDetails[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    /**
     * Whether to display last name first. By default first name is in the first line and last name is in the second.
     */
    public boolean swap_names = false;

    /**
     * Maximal number of first names to display.
     */
    public int max_names = 0;

    public String[] max_namess = {"nolimit", "1", "2", "3"};

    /**
     * Maximal number of first names per line.
     */
    public int max_names_per_line = 2;

    public String[] max_names_per_lines = {"nolimit", "1", "2", "3"};

    /**
     * Whether to display the title of an individual
     */
    public boolean draw_title = false;

    /**
     * Whether to display suffix from a name
     */
    public boolean draw_name_suffix = false;

    /**
     * Font style for display name suffix
     */
    public int font_name_suffix = Font.BOLD + Font.ITALIC;

    public String[] font_name_suffixs = {"plain", "bold", "italic", "bolditalic"};

    /**
     * Whether to IDs of individuals.
     */
    public boolean draw_indi_ids = false;
    public boolean draw_indi_sosas = false;

    /**
     * Whether to IDs of families.
     */
    public boolean draw_fam_ids = false;

    /**
     * Whether to display sex symbols.
     */
    public boolean draw_sex_symbols = true;

    /**
     * Whether to display dates of birth and death.
     */
    public boolean draw_dates = true;

    /**
     * Whether to display places of birth and death.
     */
    public boolean draw_places = true;

    /**
     * Format to display places
     */
    public String place_display_format = "place_display_format.full";

    /**
     * Whether to display occupations.
     */
    public boolean draw_occupation = true;

    /**
     * Whether to display divorce information.
     */
    public boolean draw_divorce = true;

    /**
     * Whether to display images.
     */
    public boolean draw_images = true;

    /**
     * Whether to produce high quality images (photos) in PDF files. Note: Produced PDF files can be several times larger with this option enabled.
     */
    public boolean high_quality_images = true;

    /**
     * Whether to shrink boxes when possible.
     */
    public boolean shrink_boxes = false;

    /**
     * Whether to use colors (or only black and white).
     */
    public boolean use_colors = true;

    private Font detailsFont;
    private Font nameFont;
    private Font idFont;
    private final Translator translator;

    public GraphicsTreeElements(Translator translator) {
        this.translator = translator;
        max_namess[0] = translator.translate(max_namess[0]);
        max_names_per_lines[0] = translator.translate(max_names_per_lines[0]);
        for (int i = 0; i < font_name_suffixs.length; i++) {
            font_name_suffixs[i] = translator.translate(font_name_suffixs[i]);
        }
        place_display_format = translator.translate(place_display_format);
    }

    private void checkFont() {
        if (detailsFont != null && detailsFont.getFamily().equals(fontNameDetails[fontNameDetail])) {
            return;
        }
        if (!GraphicsUtil.checkFont(fontNameDetails[fontNameDetail])) {
            fontNameDetails[fontNameDetail] = "verdana";
        }
        detailsFont = new Font(fontNameDetails[fontNameDetail], Font.PLAIN, 10);
        nameFont = new Font(fontNameDetails[fontNameDetail], Font.BOLD, 12);
        idFont = new Font(fontNameDetails[fontNameDetail], Font.ITALIC, 10);
    }

    /**
     * Box background colors.
     */
    private ColorManager colorManager;

    public void setColorManager(ColorManager colorManager) {
        this.colorManager = colorManager;
    }

    /**
     * The graphics object to paint on.
     */
    private Graphics2D graphics = null;

    /**
     * Sets the Graphics2D object to draw on.
     */
    @Override
    public void setGraphics(Graphics2D graphics) {
        this.graphics = graphics;
    }

    /**
     * Outputs an individual box.
     *
     * @param i individual
     * @param x x coordinate
     * @param y y coordinate
     * @param gen generation number
     */
    @Override
    public void drawIndiBox(IndiBox indibox, int x, int y, int gen) {

        checkFont();
        // Don't draw if it's not visible
        if (!graphics.hitClip(x, y, indibox.width, indibox.height)) {
            return;
        }

        Indi i = indibox.individual;

        // Determine photo size
        int imageWidth = 0;
        int imageHeight = indibox.height;
        if (draw_images) {
            Dimension d = MediaRenderer.getSize(i);
            if (d.width > 0 && d.height > 0) {
                imageWidth = d.width * indibox.height / d.height;
            }
            if (imageWidth > MAX_IMAGE_WIDTH) {
                imageWidth = MAX_IMAGE_WIDTH;
                imageHeight = d.height * imageWidth / d.width;
            }
        }
        int dataWidth = indibox.width - imageWidth;

        Color color = getBoxColor(gen);
        Shape box = new RoundRectangle2D.Double(x, y, indibox.width, indibox.height, 15, 15);
        graphics.setColor(color);
        graphics.fill(box);
        graphics.setColor(Color.BLACK);

        Shape oldClip = graphics.getClip();
        graphics.clip(box);

        // Name suffix
        String nameSuffix = null;
        if (draw_name_suffix) {
            nameSuffix = i.getNameSuffix();
            if (nameSuffix != null && nameSuffix.equals("")) {
                nameSuffix = null;
            }
        }

        // Name
        int currentY = y + 14;
        String[] firstNames = getFirstNames(i);
        String lastName = "";

        // generate Title + LastName
        PropertyName prop = i.getNameProperty();
        if (prop != null) {
            String spfx = prop.getSurnamePrefix();
            if (spfx != null && !spfx.isEmpty()) {
                lastName = prop.getSurnamePrefix() + " ";
            }
            lastName = lastName + prop.getLastName();
        }
        if (draw_title && i.getProperty(PATH_INDITITL) != null) {
            lastName = i.getProperty(PATH_INDITITL).getDisplayValue() + " " + lastName;
        }

        if (swap_names) { // last name
            graphics.setFont(nameFont);
            centerString(graphics, lastName, x + dataWidth / 2, currentY);
            currentY += NAME_LINE_HEIGHT;

            if (nameSuffix != null) {
                graphics.setFont(new Font(fontNameDetails[fontNameDetail], font_name_suffix, 12));
                centerString(graphics, nameSuffix, x + dataWidth / 2, currentY);
                currentY += NAME_LINE_HEIGHT;
            }
        }

        graphics.setFont(nameFont);
        for (String firstName : firstNames) {
            // first names
            centerString(graphics, firstName, x + dataWidth / 2, currentY);
            currentY += NAME_LINE_HEIGHT;
        }

        if (!swap_names) { // last name
            graphics.setFont(nameFont);
            centerString(graphics, lastName, x + dataWidth / 2, currentY);
            currentY += NAME_LINE_HEIGHT;

            if (nameSuffix != null) {
                graphics.setFont(new Font(fontNameDetails[fontNameDetail], font_name_suffix, 12));
                centerString(graphics, nameSuffix, x + dataWidth / 2, currentY);
                currentY += NAME_LINE_HEIGHT;
            }
        }

        graphics.setFont(detailsFont);

        Property birthDate = null;
        Property deathDate = null;
        PropertyPlace birthPlace = null;
        PropertyPlace deathPlace = null;
        Property occupation = null;

        if (draw_dates) {
            birthDate = i.getBirthDate();
            if (birthDate != null && !birthDate.isValid()) {
                birthDate = null;
            }
            deathDate = i.getDeathDate();
            if (deathDate != null && !deathDate.isValid()) {
                deathDate = null;
            }
        }

        if (draw_places) {
            birthPlace = (PropertyPlace) i.getProperty(PATH_INDIBIRTPLAC);
            if (birthPlace != null && birthPlace.getDisplayValue().equals("")) {
                birthPlace = null;
            }
            deathPlace = (PropertyPlace) i.getProperty(PATH_INDIDEATPLAC);
            if (deathPlace != null && deathPlace.getDisplayValue().equals("")) {
                deathPlace = null;
            }
        }

        if (draw_occupation) {
            occupation = i.getProperty(PATH_INDIOCCU);
        }

        // Date and place of birth
        if (birthDate != null || birthPlace != null) {
            centerString(graphics, TextOptions.getInstance().getBirthSymbol(), x + 7, currentY);
            if (birthDate != null) {
                graphics.drawString(birthDate.getDisplayValue(), x + 13, currentY);
                currentY += LINE_HEIGHT;
            }
            if (birthPlace != null) {
                graphics.drawString((birthPlace).format(getPlaceDisplayFormat()).replaceAll("^(,|(, ))*", "").trim(), x + 13, currentY);
                currentY += LINE_HEIGHT;
            }
        }

        // Date and place of death
        if (i.getDeathDate() != null || i.getProperty(PATH_INDIDEATPLAC) != null) {
            centerString(graphics, TextOptions.getInstance().getDeathSymbol(), x + 7, currentY);
            if (deathDate != null) {
                graphics.drawString(deathDate.getDisplayValue(), x + 13, currentY);
                currentY += LINE_HEIGHT;
            }
            if (deathPlace != null) {
                graphics.drawString((deathPlace).format(getPlaceDisplayFormat()).replaceAll("^(,|(, ))*", "").trim(), x + 13, currentY);
                currentY += LINE_HEIGHT;
            }
            if (deathDate == null && deathPlace == null) {
                currentY += LINE_HEIGHT;
            }
        }

        // Occupation
        if (occupation != null) {
            graphics.drawString(occupation.getDisplayValue(), x + 7, currentY);
        }

        // Sex symbol
        if (draw_sex_symbols) {
            int symbolX = x + dataWidth - 14;
            int symbolY = y + indibox.height - 5;
            graphics.setFont(sexSymbolFont);
            graphics.drawString(getSexSymbol(i.getSex()), symbolX, symbolY);
        }

        // Id
        if (draw_indi_ids || draw_indi_sosas) {
            graphics.setFont(idFont);
            graphics.drawString(getIdString(i), x + 7, y + indibox.height - 4);
        }

        // Photo
        if (imageWidth > 0) {
            AffineTransform transform = null;
            double scale = 1;
            if (high_quality_images) {
                transform = graphics.getTransform();
                graphics.scale(1 / IMAGE_SCALE_FACTOR, 1 / IMAGE_SCALE_FACTOR);
                scale = IMAGE_SCALE_FACTOR;
            }

            MediaRenderer.render(graphics,
                    new Rectangle((int) (x + dataWidth * scale), (y), (int) (imageWidth * scale), (int) (imageHeight * scale)),
                    i);

            if (high_quality_images) {
                graphics.setTransform(transform);
            }
        }

        graphics.setClip(oldClip);
        graphics.draw(box);
    }

    /**
     * Outputs a family box.
     *
     * @param i individual
     * @param x x coordinate
     * @param y y coordinate
     * @param gen generation number
     */
    @Override
    public void drawFamBox(FamBox fambox, int x, int y, int gen) {

        checkFont();
        // Don't draw if it's not visible
        if (!graphics.hitClip(x, y, fambox.width, fambox.height)) {
            return;
        }

        Fam f = fambox.family;

        Color color = getBoxColor(gen);
        Shape box = new RoundRectangle2D.Double(x, y, fambox.width, fambox.height, 5, 5);
        graphics.setColor(color);
        graphics.fill(box);
        graphics.setColor(Color.BLACK);

        Shape oldClip = graphics.getClip();
        graphics.clip(box);

        int currentY = y + 12;

        graphics.setFont(detailsFont);

        Property marriageDate = null;
        Property divorceDate = null;
        PropertyPlace marriagePlace = null;
        PropertyPlace divorcePlace = null;

        if (draw_dates) {
            marriageDate = f.getMarriageDate();
            if (marriageDate != null && !marriageDate.isValid()) {
                marriageDate = null;
            }
            divorceDate = f.getDivorceDate();
            if (divorceDate != null && !divorceDate.isValid()) {
                divorceDate = null;
            }
        }

        if (draw_places) {
            marriagePlace = (PropertyPlace) f.getProperty(PATH_FAMMARRPLAC);
            if (marriagePlace != null && marriagePlace.getDisplayValue().equals("")) {
                marriagePlace = null;
            }
            divorcePlace = (PropertyPlace) f.getProperty(PATH_FAMDIVPLAC);
            if (divorcePlace != null && divorcePlace.getDisplayValue().equals("")) {
                divorcePlace = null;
            }
        }

        // Date and place of marriage
        if (f.getMarriageDate() != null) {
            centerString(graphics, TextOptions.getInstance().getMarriageSymbol(), x + 13, currentY);
            if (marriageDate != null) {
                graphics.drawString(marriageDate.getDisplayValue(), x + 25, currentY);
                currentY += LINE_HEIGHT;
            }
            if (marriagePlace != null) {
                graphics.drawString((marriagePlace).format(getPlaceDisplayFormat()).replaceAll("^(,|(, ))*", "").trim(), x + 25, currentY);
                currentY += LINE_HEIGHT;
            }
            if (marriageDate == null && marriagePlace == null) {
                currentY += LINE_HEIGHT;
            }
        }

        // Date and place of divorce
        if (draw_divorce && f.getDivorceDate() != null) {
            centerString(graphics, TextOptions.getInstance().getDivorceSymbol(), x + 13, currentY);
            if (divorceDate != null) {
                graphics.drawString(divorceDate.getDisplayValue(), x + 25, currentY);
                currentY += LINE_HEIGHT;
            }
            if (divorcePlace != null) {
                graphics.drawString((divorcePlace).format(getPlaceDisplayFormat()).replaceAll("^(,|(, ))*", "").trim(), x + 25, currentY);
                currentY += LINE_HEIGHT;
            }
            if (divorceDate == null && divorcePlace == null) {
                currentY += LINE_HEIGHT;
            }
        }

        // Id
        if (draw_fam_ids) {
            graphics.setFont(idFont);
            graphics.drawString(f.getId(), x + 8, y + fambox.height - 4);
        }

        graphics.setClip(oldClip);
        graphics.draw(box);
    }

    /**
     * Outputs a line.
     *
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        graphics.drawLine(x1, y1, x2, y2);
    }

    /**
     * Outputs a dashed line.
     *
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
    @Override
    public void drawDashedLine(int x1, int y1, int x2, int y2) {
        Stroke oldStroke = graphics.getStroke();
        graphics.setStroke(DASHED_STROKE);
        graphics.drawLine(x1, y1, x2, y2);
        graphics.setStroke(oldStroke);
    }

    /**
     * Initializes the graphics.
     */
    @Override
    public void header(int width, int height) {
        graphics.setStroke(new BasicStroke(STROKE_WIDTH));
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, width, height);
    }

    /**
     * Does nothing.
     */
    @Override
    public void footer() {
        // Nothing to do.
    }

    /**
     * Outputs a string centered.
     */
    public static void centerString(Graphics2D graphics, String text, int x, int y) {
        int width = getTextWidth(text, graphics.getFont(), graphics);
        graphics.drawString(text, x - width / 2, y);
    }

    /**
     * Outputs a string aligned right.
     */
    public static void alignRightString(Graphics2D graphics, String text, int x, int y) {
        int width = getTextWidth(text, graphics.getFont(), graphics);
        graphics.drawString(text, x - width, y);
    }

    public static int getTextWidth(String text, Font font, Graphics2D graphics) {
        FontRenderContext fontRenderContext = FONT_RENDER_CONTEXT;
        if (graphics != null) {
            fontRenderContext = graphics.getFontRenderContext();
        }
        int width = (int) font.getStringBounds(text, fontRenderContext).getWidth();
        if (!text.isEmpty() && width == 0) {  // FL: bug for some font (cantarell plain returns 0, cantarell bold does not !)
            // try bold
            width = (int) font.deriveFont(Font.BOLD).getStringBounds(text, fontRenderContext).getWidth();
        }
        return width;
    }

    private static int getTextWidth(String text, Font font) {
        return getTextWidth(text, font, null);
    }

    private static String getSexSymbol(int sex) {
        if (sex == PropertySex.MALE) {
            return MALE_SYMBOL;
        }
        if (sex == PropertySex.FEMALE) {
            return FEMALE_SYMBOL;
        }
        return UNKNOWN_SYMBOL;
    }

    /**
     * Returns the box color for the given generation.
     */
    private Color getBoxColor(int gen) {
        if (!use_colors) {
            return Color.WHITE;
        }
        if (gen == 0) {
            return colorManager.getBoxColors()[colorManager.getColorGenerations()];
        }
        if (gen < 0) {
            return colorManager.getBoxColors()[-((-gen - 1) % colorManager.getColorGenerations()) + colorManager.getColorGenerations() - 1];
        }
        // else (gen > 0)
        return colorManager.getBoxColors()[(gen - 1) % colorManager.getColorGenerations() + colorManager.getColorGenerations() + 1];
    }

    /**
     * Returns a maximum of <code>maxNames</code> given names of the given individual. If <code>maxNames</code> is 0, this method returns all given names. The names are split into lines, where the maximum number of names in one line is specified by <code>maxNamesPerLine</code>. if <code>maxNamesPerLine</code> is 0, only one line is returned.
     *
     * @return array of lines to display
     */
    private String[] getFirstNames(Indi indi) {
        String firstName = indi.getFirstName();
        if (max_names <= 0 && max_names_per_line <= 0) {
            return new String[]{firstName};
        }
        if (firstName.trim().equals("")) {
            return new String[]{""};
        }

        String[] names = firstName.split("  *");
        int namesCount = names.length;
        if (max_names > 0 && max_names < namesCount) {
            namesCount = max_names;
        }
        int linesCount = 1;
        if (max_names_per_line > 0) {
            linesCount = (namesCount - 1) / max_names_per_line + 1;
        }
        String[] lines = new String[linesCount];
        for (int j = 0; j < linesCount; j++) {
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < max_names_per_line; k++) {
                int n = j * max_names_per_line + k;
                if (n >= namesCount) {
                    break;
                }
                sb.append(names[n]).append(" ");
            }
            if (sb.length() > 0) {
                lines[j] = sb.substring(0, sb.length() - 1);
            } else {
                lines[j] = "";
            }
        }

        return lines;
    }

    @Override
    public void getIndiBoxSize(IndiBox indibox) {

        checkFont();
        Indi i = indibox.individual;
        indibox.height = DEFAULT_INDIBOX_HEIGHT;
        if (shrink_boxes) {
            indibox.width = SHRINKED_INDIBOX_WIDTH;
        } else {
            indibox.width = DEFAULT_INDIBOX_WIDTH;
        }

        // Number of lines
        int lines = 0;
        if (draw_dates && i.getBirthDate() != null && i.getBirthDate().isValid()) {
            lines++;
        }
        PropertyPlace birthPlace = (PropertyPlace) i.getProperty(PATH_INDIBIRTPLAC);
        if (draw_places && birthPlace != null && !birthPlace.getDisplayValue().equals("")) {
            lines++;
        }

        PropertyPlace deathPlace = (PropertyPlace) i.getProperty(PATH_INDIDEATPLAC);
        if (deathPlace != null && deathPlace.getDisplayValue().equals("")) {
            deathPlace = null;
        }
        if (i.getDeathDate() != null || deathPlace != null) {
            lines++;
            if (draw_dates && draw_places && i.getDeathDate() != null && i.getDeathDate().isValid() && deathPlace != null) {
                lines++;
            }
        }
        if (draw_occupation && i.getProperty(PATH_INDIOCCU) != null) {
            lines++;
        }
        if (lines - DEFAULT_INDIBOX_LINES > 0) {
            indibox.height += (lines - DEFAULT_INDIBOX_LINES) * LINE_HEIGHT;
        }

        // height and width computations for first names
        int width = 0;
        String[] firstNames = getFirstNames(i);
        for (String firstName : firstNames) {
            int w2 = getTextWidth(firstName, nameFont);
            width = width > w2 ? width : w2;
        }

        // Additional first names
        indibox.height += (firstNames.length - 1) * NAME_LINE_HEIGHT;

        // optional name suffix
        if (draw_name_suffix && i.getNameSuffix() != null && i.getNameSuffix().length() > 0) {
            indibox.height += NAME_LINE_HEIGHT;
        }

        // Text data width
        if (width + 2 * TEXT_MARGIN > indibox.width) {
            indibox.width = width + 2 * TEXT_MARGIN;
        }

        String lastName = "";
        PropertyName prop = i.getNameProperty();
        if (prop != null) {
            if (prop.getSurnamePrefix() != null) {
                lastName = prop.getSurnamePrefix() + " ";
            }
            lastName = lastName + prop.getLastName();
        }

        if (draw_title && i.getProperty(PATH_INDITITL) != null) {
            width = getTextWidth(i.getProperty(PATH_INDITITL).getDisplayValue() + " " + lastName, nameFont);
        } else {
            width = getTextWidth(lastName, nameFont);
        }

        if (width + 2 * TEXT_MARGIN > indibox.width) {
            indibox.width = width + 2 * TEXT_MARGIN;
        }
        width = getTextWidth(i.getNameSuffix(), nameFont);
        if (width + 2 * TEXT_MARGIN > indibox.width) {
            indibox.width = width + 2 * TEXT_MARGIN;
        }

        if (i.getBirthDate() != null) {
            width = getTextWidth(i.getBirthDate().getDisplayValue(), detailsFont);
            if (width + 13 + 2 * TEXT_MARGIN > indibox.width) {
                indibox.width = width + 13 + 2 * TEXT_MARGIN;
            }
        }
        if (i.getDeathDate() != null) {
            width = getTextWidth(i.getDeathDate().getDisplayValue(), detailsFont);
            if (width + 13 + 2 * TEXT_MARGIN > indibox.width) {
                indibox.width = width + 13 + 2 * TEXT_MARGIN;
            }
        }

        if (draw_places) {
            if (birthPlace != null) {
                width = getTextWidth((birthPlace).format(getPlaceDisplayFormat()).replaceAll("^(,|(, ))*", "").trim(), detailsFont);
                if (width + 13 + 2 * TEXT_MARGIN > indibox.width) {
                    indibox.width = width + 13 + 2 * TEXT_MARGIN;
                }
            }
            if (deathPlace != null) {
                width = getTextWidth((deathPlace).format(getPlaceDisplayFormat()).replaceAll("^(,|(, ))*", "").trim(), detailsFont);
                if (width + 13 + 2 * TEXT_MARGIN > indibox.width) {
                    indibox.width = width + 13 + 2 * TEXT_MARGIN;
                }
            }
        }

        if (draw_occupation && i.getProperty(PATH_INDIOCCU) != null) {
            width = getTextWidth(i.getProperty(PATH_INDIOCCU).getDisplayValue(), detailsFont);
            if (width + 14 + 2 * TEXT_MARGIN > indibox.width) {
                indibox.width = width + 14 + 2 * TEXT_MARGIN;
            }
        }

        if (draw_indi_ids || draw_indi_sosas) {
            width = getTextWidth(getIdString(i), idFont);
            if (draw_sex_symbols) {
                width += 24;
            }
            if (width + 8 + 2 * TEXT_MARGIN > indibox.width) {
                indibox.width = width + 7 + 2 * TEXT_MARGIN;
            }
        }

        // Image
        if (draw_images) {
            Dimension d = MediaRenderer.getSize(i);
            if (d.width > 0 && d.height > 0) {
                int newWidth = d.width * DEFAULT_INDIBOX_HEIGHT / d.height;
                if (newWidth < MAX_IMAGE_WIDTH) {
                    indibox.width += newWidth;
                } else {
                    indibox.width += MAX_IMAGE_WIDTH;
                }
            }
        }
    }

    @Override
    public void getFamBoxSize(FamBox fambox) {

        checkFont();
        Fam f = fambox.family;
        fambox.width = DEFAULT_FAMBOX_WIDTH;
        fambox.height = DEFAULT_FAMBOX_HEIGHT;

        // Number of lines
        int lines = 0;
        PropertyPlace marriagePlace = (PropertyPlace) f.getProperty(PATH_FAMMARRPLAC);
        if (f.getMarriageDate() != null) {
            lines++;
            if (draw_dates && draw_places && f.getMarriageDate().isValid() && marriagePlace != null && !marriagePlace.getDisplayValue().equals("")) {
                lines++;
            }
        }
        PropertyPlace divorcePlace = (PropertyPlace) f.getProperty(PATH_FAMDIVPLAC);
        if (draw_divorce && f.getDivorceDate() != null) {
            lines++;
            if (draw_dates && draw_places && f.getDivorceDate().isValid() && divorcePlace != null && !divorcePlace.getDisplayValue().equals("")) {
                lines++;
            }
        }

        if (lines - DEFAULT_FAMBOX_LINES > 0) {
            fambox.height += (lines - DEFAULT_FAMBOX_LINES) * LINE_HEIGHT;
        }

        // Text data width
        if (f.getMarriageDate() != null) {
            int width = getTextWidth(f.getMarriageDate().getDisplayValue(), detailsFont);
            if (width + 25 + 2 * TEXT_MARGIN > fambox.width) {
                fambox.width = width + 25 + 2 * TEXT_MARGIN;
            }
        }
        if (draw_divorce && f.getDivorceDate() != null) {
            int width = getTextWidth(f.getDivorceDate().getDisplayValue(), detailsFont);
            if (width + 25 + 2 * TEXT_MARGIN > fambox.width) {
                fambox.width = width + 25 + 2 * TEXT_MARGIN;
            }
        }

        if (draw_places) {
            if (marriagePlace != null) {
                int width = getTextWidth((marriagePlace).format(getPlaceDisplayFormat()).replaceAll("^(,|(, ))*", "").trim(), detailsFont);
                if (width + 35 + 2 * TEXT_MARGIN > fambox.width) {
                    fambox.width = width + 35 + 2 * TEXT_MARGIN;
                }
            }
            if (draw_divorce && divorcePlace != null) {
                int width = getTextWidth((divorcePlace).format(getPlaceDisplayFormat()).replaceAll("^(,|(, ))*", "").trim(), detailsFont);
                if (width + 35 + 2 * TEXT_MARGIN > fambox.width) {
                    fambox.width = width + 35 + 2 * TEXT_MARGIN;
                }
            }
        }
    }

    private String getIdString(Indi i) {
        return (draw_indi_ids ? i.getId() : "") + (draw_indi_ids && draw_indi_sosas ? " / " : "") + (draw_indi_sosas ? i.getSosaString() : "");
    }

    private String getPlaceDisplayFormat() {

        if (place_display_format.equals(translator.translate("place_display_format.full"))) {
            return "all";
        }
        return place_display_format;
    }
}
