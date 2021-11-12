package ancestris.modules.commonAncestor;

import ancestris.modules.commonAncestor.graphics.IGraphicsRenderer;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.PointInTime;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Calendar;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author michel
 */
public class Renderer implements IGraphicsRenderer {

    private static final int FAMILY_WIDTH = 300;
    private static final int FAMILY_HEIGH = 100;
    private static final int SPACE_BETWEEN_RECTANGLES = 20;
    private static final int SPACE_BETWEEN_TITLE_AND_COMMON_ANCESTOR = 30;
    private static final int SPACE_BETWEEN_LINES = 18;
    private static final int SPACE_BEFORE_DATE = 20;
    private static final int SHADOW_SIZE = 3;
    private static final int SPACE_BETWEEN_BORDER_AND_RECTANGLE = 5;
    private static final int SPACE_BETWEEN_BORDER_AND_TITLE = 25;
    private Font boldFontStyle;
    private Font plainFontStyle;
    private final Font dateFontStyle;
    private Font titleFontStyle;
    private final Font smallFontStyle;
    private final int YEAR_LIMIT_NUMBER = 81;
    private final int YEAR_LIMIT = Calendar.getInstance().get(Calendar.YEAR) - YEAR_LIMIT_NUMBER;
    /**
     * Whether to use colors (or only black and white).
     */
    public boolean use_colors = true;
    /**
     * Whether to display indi and family ids.
     */
    private boolean displayedId = true;
    /**
     * Whether to display under YEAR_LIMIT_NUMBER year dates.
     */
    public boolean displayRecentYears = true;
    /**
     * Whether to display the husband or the wife first in each step bloc.
     */
    public int husband_or_wife_first = 0;
    public String husband_or_wife_firsts[] = {
        "wife",
        "husband"
    };

    /**
     * where to position the representation parts
     */
    private enum Position {

        LEFT, RIGHT, CENTER
    };
    /**
     * choose font to use
     */
    public int ufont_name = 0;
    public String ufont_names[] = {"ufont_name.0",
        "ufont_name.1", "ufont_name.2",
        "ufont_name.3", "ufont_name.4",
        "ufont_name.5"};

    //private String font_name = "Helvetica";
    //private String font_name = translate("ufont_name."+ufont_name);
    private final String font_name = "Times-Roman";

    /**
     * options
     */
    protected final static int OPTION_YESNO = 0,
            OPTION_OKCANCEL = 1,
            OPTION_OK = 2;
    protected final static int DISPLAY_HUSB = 0,
            DISPLAY_WIFE = 1,
            DISPLAY_ALL = 2;
    private double zoom = 1.0D;
    private final Indi firstIndi;
    private final Indi secondIndi;
    private final List<Step> firstIndiDirectLinks;
    private final List<Step> secondIndiDirectLinks;
    private final int width;
    private final int height;
    private final double cx;
    private double cy;

    /**
     * renderer's constructor. The entry point to the ouput generation
     *
     * @param firstIndi
     * @param secondIndi
     * @param firstIndiDirectLinks
     * @param secondIndiDirectLinks
     * @param displayedId
     * @param displayRecentYears
     * @param husband_or_wife_first
     */
    public Renderer(Indi firstIndi, Indi secondIndi, List<Step> firstIndiDirectLinks, List<Step> secondIndiDirectLinks, boolean displayedId, boolean displayRecentYears, int husband_or_wife_first) {
        this.firstIndi = firstIndi;
        this.secondIndi = secondIndi;
        this.firstIndiDirectLinks = firstIndiDirectLinks;
        this.secondIndiDirectLinks = secondIndiDirectLinks;
        this.displayedId = displayedId;
        this.displayRecentYears = displayRecentYears;
        this.husband_or_wife_first = husband_or_wife_first;

        titleFontStyle = new Font(font_name, Font.BOLD, 20);
        boldFontStyle = new Font(font_name, Font.BOLD, 16);
        plainFontStyle = new Font(font_name, Font.PLAIN, 16);
        dateFontStyle = new Font(font_name, Font.PLAIN, 12);
        smallFontStyle = new Font(font_name, Font.BOLD, 12);

        width = 3 * FAMILY_WIDTH + FAMILY_WIDTH / 2;
        height = (Math.max(firstIndiDirectLinks.size(), secondIndiDirectLinks.size())) * FAMILY_HEIGH
                + (Math.max(firstIndiDirectLinks.size(), secondIndiDirectLinks.size()) + 3) * SPACE_BETWEEN_RECTANGLES
                + SPACE_BEFORE_DATE
                + SPACE_BETWEEN_TITLE_AND_COMMON_ANCESTOR
                + SPACE_BETWEEN_BORDER_AND_RECTANGLE
                + SPACE_BETWEEN_BORDER_AND_TITLE;

        cx = width / 2;
    }

    /* (non-Javadoc)
     * @see tree.graphics.GraphicsRenderer#render(java.awt.Graphics2D)
     */
    @Override
    public void render(Graphics2D graphics) {
        // apply zoom
        graphics.scale(zoom, zoom);
        cy = 0;
        graphics.setPaint(Color.BLACK);
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, width, height);
        if (firstIndi == null || secondIndi == null) {
            return;
        }
        graphics.drawRoundRect(SPACE_BETWEEN_BORDER_AND_RECTANGLE,
                SPACE_BETWEEN_BORDER_AND_RECTANGLE,
                width - SPACE_BETWEEN_BORDER_AND_RECTANGLE * 2,
                height - SPACE_BETWEEN_BORDER_AND_RECTANGLE * 2, 50, 50);
        graphics.setFont(plainFontStyle);
        graphics.setStroke(new BasicStroke(2));
        cy += SPACE_BETWEEN_BORDER_AND_RECTANGLE;
        int nbMaxGen = Math.max(firstIndiDirectLinks.size(), secondIndiDirectLinks.size());

        // the title
        graphics.setFont(titleFontStyle);
        cy += SPACE_BETWEEN_BORDER_AND_TITLE;

        // decrease title font size if it doesn't fill in round rectangle
        String titleString = getTitleLine(firstIndi, secondIndi, nbMaxGen);
        for (int fontSize = titleFontStyle.getSize(); fontSize >= 8; fontSize--) {
            Rectangle2D rect = graphics.getFont().getStringBounds(titleString, graphics.getFontRenderContext());
            if ((int) rect.getWidth() > width - SPACE_BETWEEN_BORDER_AND_RECTANGLE * 4) {
                titleFontStyle = new Font(titleFontStyle.getFontName(), titleFontStyle.getStyle(), fontSize);
            } else {
                break;
            }
        }

        // j'affiche le titre
        centerString(graphics, NbBundle.getMessage(Renderer.class, "Renderer.title1"), (int) cx, (int) cy);
        cy += SPACE_BETWEEN_TITLE_AND_COMMON_ANCESTOR;
        centerString(graphics, getTitleLine(firstIndi, secondIndi, nbMaxGen), (int) cx, (int) cy);
        cy += SPACE_BETWEEN_TITLE_AND_COMMON_ANCESTOR;

        if (firstIndiDirectLinks.isEmpty() || secondIndiDirectLinks.isEmpty()) {
            return;
        }

        int familyWidth = FAMILY_WIDTH;
        String largestName = "";

        // je cherche le nom le plus long et taille de son rectangle
        for (int i = 1; i < nbMaxGen; i++) {
            Step step;
            Rectangle2D rect;
            String name;
            if (firstIndiDirectLinks.size() > i) {
                step = firstIndiDirectLinks.get(i);

                name = getNameLine(step.getHusband());
                rect = graphics.getFont().getStringBounds(name, graphics.getFontRenderContext());
                if ((int) rect.getWidth() + SPACE_BETWEEN_BORDER_AND_RECTANGLE > familyWidth) {
                    familyWidth = (int) rect.getWidth() + SPACE_BETWEEN_BORDER_AND_RECTANGLE;
                    largestName = name;
                }
                name = getNameLine(step.getWife());
                rect = graphics.getFont().getStringBounds(name, graphics.getFontRenderContext());
                if ((int) rect.getWidth() + SPACE_BETWEEN_BORDER_AND_RECTANGLE > familyWidth) {
                    familyWidth = (int) rect.getWidth() + SPACE_BETWEEN_BORDER_AND_RECTANGLE;
                    largestName = name;
                }
            }
            if (secondIndiDirectLinks.size() > i) {
                step = secondIndiDirectLinks.get(i);
                name = getNameLine(step.getHusband());
                rect = graphics.getFont().getStringBounds(name, graphics.getFontRenderContext());
                if ((int) rect.getWidth() + SPACE_BETWEEN_BORDER_AND_RECTANGLE > familyWidth) {
                    familyWidth = (int) rect.getWidth() + SPACE_BETWEEN_BORDER_AND_RECTANGLE;
                    largestName = name;
                }
                name = getNameLine(step.getWife());
                rect = graphics.getFont().getStringBounds(name, graphics.getFontRenderContext());
                if ((int) rect.getWidth() + SPACE_BETWEEN_BORDER_AND_RECTANGLE > familyWidth) {
                    familyWidth = (int) rect.getWidth() + SPACE_BETWEEN_BORDER_AND_RECTANGLE;
                    largestName = name;
                }
            }
        }

        // je verifie que le rectangle contient dans dans la colonne
        if (familyWidth > (width / 2 - 5 * SPACE_BETWEEN_BORDER_AND_RECTANGLE)) {
            // Si le rectangle est trop grand, je limite la largeur du rectangle
            familyWidth = (width / 2 - 5 * SPACE_BETWEEN_BORDER_AND_RECTANGLE);
            // et je diminue la taille de la police pour que le nom le plus long contienne
            // dans le rectangle
            graphics.setFont(boldFontStyle);
            for (int fontSize = boldFontStyle.getSize(); fontSize >= 8; fontSize--) {
                Rectangle2D rect = graphics.getFont().getStringBounds(largestName, graphics.getFontRenderContext());
                if ((int) rect.getWidth() > familyWidth - SPACE_BETWEEN_BORDER_AND_RECTANGLE) {
                    boldFontStyle = new Font(boldFontStyle.getFontName(), boldFontStyle.getStyle(), fontSize);
                    plainFontStyle = new Font(plainFontStyle.getFontName(), plainFontStyle.getStyle(), fontSize);
                    graphics.setFont(boldFontStyle);
                } else {
                    break;
                }
            }
        }

        // je calcule le centre des colonnes des descendants
        int cxLeft = (int) (cx - SPACE_BETWEEN_BORDER_AND_RECTANGLE) / 2;
        int cxRight = (int) (width - SPACE_BETWEEN_BORDER_AND_RECTANGLE + cx) / 2;

        //the common ancestor
        graphics.setFont(plainFontStyle);
        // j'affiche l'ancetre sur la colonne centrale.
        //Check if the family is the same and which spouse has to be dispayed
        Fam firstFam = firstIndiDirectLinks.get(0).famWhereSpouse;
        Fam secondFam = secondIndiDirectLinks.get(0).famWhereSpouse;
        if (firstFam.equals(secondFam)) {
            firstIndiDirectLinks.get(0).linkSex=Step.ALL;
            render(graphics, firstIndiDirectLinks.get(0), familyWidth, (int) cx, DISPLAY_ALL);
        } else if (firstFam.getHusband().equals(secondFam.getHusband())|| firstFam.getHusband().equals(secondFam.getWife())) {
            render(graphics, firstIndiDirectLinks.get(0), familyWidth, (int) cx, DISPLAY_HUSB);
        } else if (firstFam.getWife().equals(secondFam.getWife())||firstFam.getWife().equals(secondFam.getHusband())) {
            render(graphics, firstIndiDirectLinks.get(0), familyWidth, (int) cx, DISPLAY_WIFE);
        }

        // je trace la ligne verticale sous l'ancetre
        graphics.drawLine((int) cx, (int) cy + FAMILY_HEIGH, (int) cx, (int) cy + FAMILY_HEIGH + SPACE_BETWEEN_RECTANGLES);
        // je trace la ligne horizontale sous l'ancetre 
        if (firstIndiDirectLinks.size() > 1) {
            graphics.drawLine(cxLeft, (int) cy + FAMILY_HEIGH + SPACE_BETWEEN_RECTANGLES, (int) cx, (int) cy + FAMILY_HEIGH + SPACE_BETWEEN_RECTANGLES);
        }
        if (secondIndiDirectLinks.size() > 1) {
            graphics.drawLine((int) cx, (int) cy + FAMILY_HEIGH + SPACE_BETWEEN_RECTANGLES, cxRight, (int) cy + FAMILY_HEIGH + SPACE_BETWEEN_RECTANGLES);
        }

        cy += SPACE_BETWEEN_RECTANGLES;

        // the two branches
        for (int i = 1; i < nbMaxGen; i++) {
            cy += FAMILY_HEIGH + SPACE_BETWEEN_RECTANGLES;
            if (firstIndiDirectLinks.size() > i) {
                graphics.drawLine(cxLeft, (int) cy - SPACE_BETWEEN_RECTANGLES, cxLeft, (int) cy);
                render(graphics, firstIndiDirectLinks.get(i), familyWidth, cxLeft, DISPLAY_ALL);
            }
            if (secondIndiDirectLinks.size() > i) {
                graphics.drawLine(cxRight, (int) cy - SPACE_BETWEEN_RECTANGLES, cxRight, (int) cy);
                render(graphics, secondIndiDirectLinks.get(i), familyWidth, cxRight, DISPLAY_ALL);
            }
        }

        // date of generation
        graphics.setFont(smallFontStyle);
        centerString(graphics, PointInTime.getNow().toString(), (int) cx + FAMILY_WIDTH, (int) cy + FAMILY_HEIGH + SPACE_BEFORE_DATE);
    }

    @Override
    public double getZoom() {
        return zoom;

    }

    @Override
    public void setZoom(double zoom) {
        this.zoom = zoom;

    }

    /* ----------------- */
 /* (non-Javadoc)
     * @see tree.graphics.GraphicsRenderer#getImageWidth()
     */
    @Override
    public int getImageWidth() {
        return (int) (width * zoom);
    }

    /* ----------------- */
 /* (non-Javadoc)
     * @see tree.graphics.GraphicsRenderer#getImageHeight()
     */
    @Override
    public int getImageHeight() {
        return (int) (height * zoom);
    }

    /* ----------------- */
    /**
     * draw a rectangle<br/>
     * write the couple names (and dates) in this rectangle<br/>
     * put the ascendant name in bold<br/>
     *
     * @param step a link between the common ancestor and a descendant, with its
     * spouse
     * @param familyWidth rectangle width
     * @param cxStep abcisse of center of column
     */
    private void render(Graphics2D graphics, Step step, int familyWidth, int cxStep, int display) {

        graphics.setPaint(Color.BLACK);

        // get string size
        int recWidth = familyWidth;

        graphics.setFont(boldFontStyle);

        // the rectangle containing one step
        graphics.setPaint(Color.LIGHT_GRAY);
        graphics.fillRect(cxStep - recWidth / 2 + SHADOW_SIZE, (int) cy + SHADOW_SIZE, recWidth + SHADOW_SIZE, FAMILY_HEIGH + SHADOW_SIZE);
        graphics.setPaint(Color.BLACK);
        graphics.clearRect(cxStep - recWidth / 2, (int) cy, recWidth, FAMILY_HEIGH);
        graphics.drawRect(cxStep - recWidth / 2, (int) cy, recWidth, FAMILY_HEIGH);

        if (husband_or_wife_first == 0) {
            if (display == DISPLAY_ALL || display == DISPLAY_WIFE) {
                renderWife(graphics, step, cxStep, (int) cy);
            }
            if (display == DISPLAY_ALL || display == DISPLAY_HUSB) {
                renderHusband(graphics, step, cxStep, (int) cy + SPACE_BETWEEN_LINES * 2);
            }
        } else {
            if (display == DISPLAY_ALL || display == DISPLAY_HUSB) {
                renderHusband(graphics, step, cxStep, (int) cy);
            }
            if (display == DISPLAY_ALL || display == DISPLAY_WIFE) {
                renderWife(graphics, step, cxStep, (int) cy + SPACE_BETWEEN_LINES * 2);
            }
        }

        // Marriage if it does exist
        if (step.famWhereSpouse != null
                && step.famWhereSpouse.getMarriageDate() != null && display == DISPLAY_ALL) {
            graphics.setFont(dateFontStyle);
            centerString(graphics, getMarriageLine(step), cxStep, (int) cy + SPACE_BETWEEN_LINES * 5);
        }
    }

    /* ------------- */
    /**
     * render the wife of a step in a bloc
     *
     * @param graphics
     * @param step
     * @param cxStep
     */
    private void renderWife(Graphics2D graphics, Step step, int cxStep, int cyStep) {
        if (step.getWife() != null) {
            if (step.linkSex == Step.FEMALE || step.linkSex == Step.ALL) {
                graphics.setFont(boldFontStyle);
                if (use_colors) {
                    graphics.setPaint(Color.MAGENTA);
                }
                centerString(graphics, getNameLine(step.getWife()), cxStep, cyStep + SPACE_BETWEEN_LINES);
                graphics.setFont(plainFontStyle);
                graphics.setPaint(Color.BLACK);
            } else {
                graphics.setFont(plainFontStyle);
                centerString(graphics, getNameLine(step.getWife()), cxStep, cyStep + SPACE_BETWEEN_LINES);
            }
            graphics.setFont(dateFontStyle);
            centerString(graphics, getDateLine(step.getWife()), cxStep, cyStep + SPACE_BETWEEN_LINES * 2);
            graphics.setFont(plainFontStyle);
        }
    }

    /* ------------- */
    /**
     * render the husband of a step in a bloc
     *
     * @param graphics
     * @param step
     * @param cxStep
     */
    private void renderHusband(Graphics2D graphics, Step step, int cxStep, int cyStep) {
        if (step.getHusband() != null) {
            if (step.linkSex == Step.MALE || step.linkSex == Step.ALL) {
                graphics.setFont(boldFontStyle);
                if (use_colors) {
                    graphics.setPaint(Color.BLUE);
                }
                centerString(graphics, getNameLine(step.getHusband()), cxStep, cyStep + SPACE_BETWEEN_LINES);
                graphics.setFont(plainFontStyle);
                graphics.setPaint(Color.BLACK);
            } else {
                graphics.setFont(plainFontStyle);
                centerString(graphics, getNameLine(step.getHusband()), cxStep, cyStep + SPACE_BETWEEN_LINES);
            }
            graphics.setFont(dateFontStyle);
            centerString(graphics, getDateLine(step.getHusband()), cxStep, cyStep + SPACE_BETWEEN_LINES * 2);
            graphics.setFont(plainFontStyle);
        }
    }

    /* ----------------- */
    /**
     * build the name line for an Indi, (ie) his name and id<br/>
     * if the option "do not display ids" is selected, only the name is returned
     *
     * @param indi the indi to build the name line for
     * @return the name line formatted as follow : "name [indi id]"
     */
    private String getNameLine(Indi indi) {
        StringBuilder sb = new StringBuilder();
        if (indi != null) {
            if (displayedId) {
                String sosa = indi.getSosaString();
                if (!sosa.isEmpty()) {
                    sb.append("[").append(sosa).append("] - ");
                }
            }
            sb.append(indi.getFirstName()).append(" ").append(indi.getLastName());
            if (displayedId) {
                sb.append(" (").append(indi.getId()).append(")");
            }
        } else {
            sb.append("");
        }

        return sb.toString();
    }

    /* ----------------- */
    /**
     * build the date line for an Indi, (ie) his birth and death dates<br/>
     * if the option "do not display recent years" is selected, builds a blank
     * line instead of the dates
     *
     * @param indi the indi to build the date line for
     * @return the date line formatted as follow : "(birth date - death date)"
     */
    private String getDateLine(Indi indi) {
        StringBuilder sb = new StringBuilder();
        PropertyDate deathDate = indi.getDeathDate();
        if (deathDate == null) {
            deathDate = new PropertyDate();
        }
        PropertyDate birthDate = indi.getBirthDate();
        if (birthDate == null) {
            birthDate = new PropertyDate();
        }

        if (displayRecentYears
                || deathDate.getStart().getYear() < YEAR_LIMIT
                || birthDate.getStart().getYear() < YEAR_LIMIT) {
            sb.append(birthDate.getDisplayValue()).append(" - ").append(deathDate.getDisplayValue());
        }

        return sb.toString();
    }

    /* ----------------- */
    /**
     * build the marriage line for a step, (ie) the marriage date<br/>
     * if the option "do not display recent years" is selected, builds a blank
     * line instead of the whole marriage line
     *
     * @param step the couple to build the marriage line for
     * @return the marriage line formatted as follow : "marriage on : marriage
     * date [couple ID]")
     */
    private String getMarriageLine(Step step) {
        StringBuilder sb = new StringBuilder();

        if (displayRecentYears || step.famWhereSpouse.getMarriageDate(true).getStart().getYear() < YEAR_LIMIT) {
            PropertyDate date = step.famWhereSpouse.getMarriageDate(true);
            // modification pour ne pas afficher le mote "Date" avant la valeur de la date
            sb.append(NbBundle.getMessage(Renderer.class, "Renderer.marriage.date")).append(" ").append(date.getDisplayValue());

            if (displayedId) {
                sb.append(" (").append(step.famWhereSpouse.getId()).append(")");
            }
        }
        return sb.toString();
    }

    /* ----------------- */
    /**
     * the title line atop the diagram
     *
     * @param indi the first indi selected by the user
     * @param other the second indi selected by the user
     * @param generationCount the number of generations between the two selected
     * indis
     * @return
     */
    private String getTitleLine(Indi indi, Indi other, int generationCount) {
        //TODO deal with cases when generationCount =1 or 2 or when one is ascendant from the other
        Object[] args = {getNameLine(indi), getNameLine(other)};
        return NbBundle.getMessage(Renderer.class, "Renderer.title2", args);
    }


    /* ----------------- */
    /**
     * Outputs a centered string.
     */
    private void centerString(Graphics2D graphics, String text, int x, int y) {
        Rectangle2D rect = graphics.getFont().getStringBounds(text,
                graphics.getFontRenderContext());
        int iwidth = (int) rect.getWidth();
        graphics.drawString(text, x - iwidth / 2, y);
    }
}
