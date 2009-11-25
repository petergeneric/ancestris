/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * This is pretty much a brute force solution to this problem.
 * There is a 6 x 8 grid which spreads out various ancestors.
 * In hopes of devising a clever way to do this, I created a utility
 * ask for ancesters as if I was using the Swedish denotation:
 * moormoor is mother's mother. So, MMMM is the maternal great
 * great grandmother. The code as it stands is kind of sexist.
 * Only fathers are in the corners.
 *
 *   FFFF  FFF   FFFM      MFFM  MFF   MFFF
 *
 *   FFMF                              MFMF
 *   FFM   FF                    MF    MFM
 *   FFMM                              MFMM
 *         F                     M
 *   FMFM                              MMFM
 *   FMF   FM                    MM    MMF
 *   FMFF                              MMFF
 *
 *   FMMF  FMM   FMMM      MMMM  MMM   MMMF
 *
 */
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.report.Report;

/**
 * GenJ -  ReportAncestors
 * @version 0.1
 */
public class Report4Generations extends Report {

    public static final int BLOCK_WIDTH = 34;

    /**
     * Starting point - on Individual
     */
    public void start(Indi indi) {

        Indi indiffff = ancestor( indi, "FFFF");
        Indi indifff  = ancestor( indi, "FFF");
        Indi indifffm = ancestor( indi, "FFFM");
        Indi indimffm = ancestor( indi, "MFFM");
        Indi indimff  = ancestor( indi, "MFF");
        Indi indimfff = ancestor( indi, "MFFF");

        println(lineone(indiffff)+lineone(indifff)+lineone(indifffm)+lineone(indimffm)+lineone(indimff)+lineone(indimfff));
        println(linetwo(indiffff,false,(indiffff!=null))
        +linetwo(indifff,(indiffff!=null),(indifffm!=null),(indifff!=null))
        +linetwo(indifffm,(indifffm!=null),false)
        +linetwo(indimffm,false,(indimffm!=null))
        +linetwo(indimff,(indimffm!=null),(indimfff!=null),(indimff!=null))
        +linetwo(indimfff,(indimfff!=null),false));

        String spacer1 = pad(BLOCK_WIDTH)+connect(indifff!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimff!=null);
        println(spacer1); println(spacer1); println(spacer1);

        Indi indiffmf = ancestor( indi, "FFMF");
        Indi indimfmf = ancestor( indi, "MFMF");

        println(lineone(indiffmf)+connect(indifff!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimff!=null)+lineone(indimfmf));
        println(linetwo(indiffmf,false,false,(indiffmf!=null))+connect(indifff!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimff!=null)+linetwo(indimfmf,false,false,(indimfmf!=null)));

        String spacer2 = connect(indiffmf!=null)+connect(indifff!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimff!=null)+connect(indimfmf!=null);
        println(spacer2); println(spacer2);


        // *   MBNH    Collis               Arthur  Lily

        Indi indiffm = ancestor( indi, "FFM");
        Indi indiff  = ancestor( indi, "FF");
        Indi indimf  = ancestor( indi, "MF");
        Indi indimfm = ancestor( indi, "MFM");

        println(lineone(indiffm)+lineone(indiff)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+lineone(indimf)+lineone(indimfm));
        println(linetwo(indiffm,false,true,(indiffm!=null))+linetwo(indiff,(indiffm!=null),false,(indiff!=null))+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+linetwo(indimf,false,(indimfm!=null),(indimf!=null))+linetwo(indimfm,(indimfm!=null),false));


        // *   Alice                                MFMM
        Indi indiffmm  = ancestor( indi, "FFMM");
        Indi indimfmm  = ancestor( indi, "MFMM");

        String spacer3 = connect(indiffmm!=null)+connect(indiff!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimf!=null)+connect(indimfmm!=null);
        println(spacer3); println(spacer3);

        // FFMM
        println(lineone(indiffmm)+connect()+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect()+lineone(indimfmm));
        println(linetwo(indiffmm,false,false)+connect(indiff!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimf!=null)+linetwo(indimfmm,false,false));
        String spacer4 = pad(BLOCK_WIDTH)+connect(indiff!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimf!=null);
        println(spacer4); println(spacer4);


        // *            Jan                  Gail
        Indi indif  = ancestor( indi, "F");
        Indi indim  = ancestor( indi, "M");

        println(pad(BLOCK_WIDTH)+lineone(indif)+pad(BLOCK_WIDTH/2)+lineone(indi)+pad(BLOCK_WIDTH/2)+lineone(indim)+pad(BLOCK_WIDTH));
        println(pad(BLOCK_WIDTH)+linetwo(indif,false,true)+pad(BLOCK_WIDTH/2,true)+linetwo(indi,true,true)+pad(BLOCK_WIDTH/2,true)+linetwo(indim,true,false)+pad(BLOCK_WIDTH));

        // needed now for spacers
        Indi indifm  = ancestor( indi, "FM");
        Indi indimm  = ancestor( indi, "MM");

        // spacers now above
        String spacer5 = pad(BLOCK_WIDTH)+connect(indifm!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimm!=null);
        println(spacer5); println(spacer5);

        // *   Ida      c                       c      MMFM
        Indi indifmfm  = ancestor( indi, "FMFM");
        Indi indimmfm  = ancestor( indi, "MMFM");

        println(lineone(indifmfm)+connect(indifm!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimm!=null)+lineone(indimmfm));
        println(linetwo(indifmfm,false,false,(indifmfm!=null))+connect(indifm!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimm!=null)+linetwo(indimmfm,false,false,(indimmfm!=null)));

        //      c       c                       c       c
        // *   Alan c   Nancy                Mary Jane c John
        String spacer6 = connect(indifmfm!=null)+connect(indifm!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indifm!=null)+connect(indimmfm!=null);
        println(spacer6); println(spacer6);

        Indi indifmf = ancestor( indi, "FMF");
        Indi indimmf = ancestor( indi, "MMF");

        println(lineone(indifmf)+lineone(indifm)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+lineone(indimm)+lineone(indimmf));
        println(linetwo(indifmf,false,(indifmf!=null))+linetwo(indifm,(indifmf!=null),false)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+linetwo(indimm,false,(indimmf!=null))+linetwo(indimmf,(indimmf!=null),false));

        // need these to decide about connectors
        Indi indifmm  = ancestor( indi, "FMM");
        Indi indimmm  = ancestor( indi, "MMM");
        //      c       c                       c       c
        // *   James    c                       c      MMFF
        String spacer7 = connect(indifmf!=null)+connect(indifmm!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimmm!=null)+connect(indimmf!=null);
        println(spacer7); println(spacer7);



        Indi indifmff = ancestor( indi, "FMFF");
        Indi indimmff = ancestor( indi, "MMFF");
        println(lineone(indifmff)+connect(indifmm!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimmm!=null)+lineone(indimmff));
        println(linetwo(indifmff,false,false)+connect(indifmm!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimmm!=null)+linetwo(indimmff,false,false));

        //              c                       c
        // *   Henry   Ruth  Bessie      MMMM Marie     MMMF

        Indi indifmmf = ancestor( indi, "FMMF");
        Indi indifmmm = ancestor( indi, "FMMM");
        Indi indimmmm = ancestor( indi, "MMMM");
        Indi indimmmf = ancestor( indi, "MMMF");

        String spacer8 = pad(BLOCK_WIDTH)+connect(indifmm!=null)+pad(BLOCK_WIDTH)+pad(BLOCK_WIDTH)+connect(indimmm!=null);
        println(spacer8); println(spacer8); println(spacer8);

        println(lineone(indifmmf)+lineone(indifmm)+lineone(indifmmm)
        +lineone(indimmmm)+lineone(indimmm)+lineone(indimmmf));
        println(linetwo(indifmmf,false,(indifmmf!=null))
        +linetwo(indifmm,(indifmmf!=null),(indifmmm!=null))
        +linetwo(indifmmm,(indifmmm!=null),false)
        +linetwo(indimmmm,false,(indimmmm!=null))
        +linetwo(indimmm,(indimmmm!=null),(indimmmf!=null))
        +linetwo(indimmmf,(indimmmf!=null),false));


        // Done
    }

    /**
     * parent - prints information about one parent and then recurses
     */
    private Indi ancestor(Indi indi, String path) {

        //	println("debug: "+path+" ");
        if (path == null || indi == null)
            return null;

        Fam famc = indi.getFamilyWhereBiologicalChild();

        if (famc==null) {
            //  println("no Famc "+ format(indi));
            return null;
        }

        if (path.charAt(0) == 'F' && famc.getHusband()!=null) {
            if (path.length() == 1) {
                //println(format(indi));
                return famc.getHusband();
            } else
                return ancestor(famc.getHusband(), path.substring(1));
        } else if (path.charAt(0) == 'M' && famc.getWife()!=null) {
            if (path.length() == 1) {
                //println(format(indi));
                return famc.getWife();
            } else
                return ancestor(famc.getWife(), path.substring(1));
        }
        return null;
    }

    /**
     * resolves the information of one Indi
     */
    private String format(Indi indi) {

        // Might be null
        if (indi==null) {
            return "?";
        }

        // name
        String n = indi.getName();

        // birth?
        String b = " "+OPTIONS.getBirthSymbol() + indi.getBirthAsString();

        // death?
        String d = " "+OPTIONS.getDeathSymbol() + indi.getDeathAsString();

        Property place = indi.getProperty(new TagPath("INDI:BIRT:PLAC"));

        //
        Property propTitle = indi.getProperty(new TagPath("INDI:TITL"));

        // String t = indi.getProperty(new TagPath("INDI:TITL"),true).toString();
        String title = (propTitle == null)?"":propTitle.toString();
        if (title.length() > 0)
            title = " TITL: "+title;
        return n + b + d + " PLAC: "+place+title;
    }


    private String lineone(Indi indi) {

        // Might be null
        if (indi==null) {
            return pad(BLOCK_WIDTH);
        }

        // name
        String n = indi.getName();
        // name
        String l = indi.getLastName();
        String name;

        int offset = l.length() + 2;
        if (offset >= n.length())
            name = n;
        else
            name = n.substring(l.length()+2) + " "+ l;

        Property propTitle = indi.getProperty(new TagPath("INDI:TITL"));

        // String t = indi.getProperty(new TagPath("INDI:TITL"),true).toString();
        if (propTitle != null)
            name = propTitle.getValue()+" "+name;
        name = indi.getId()+" "+name;

        int padding = BLOCK_WIDTH - name.length();
        String padded = pad(padding/2)+name+pad(padding-padding/2);
        // here's the result
        return padded;

        // Could be a hyperlink, too
        //return "<a href=\"\">" + indi.getName() + "</a>" + b + d;
    }

    private String linetwo(Indi indi, boolean left, boolean right) {
        return linetwo(indi, left, right, false);
    }

    private String linetwo(Indi indi, boolean left, boolean right, boolean lower) {
        if (indi==null) {
            return pad(BLOCK_WIDTH);
        }
        // birth?
        String dates = "";

        if (indi.getBirthAsString().length() > 0)
            dates = " "+OPTIONS.getBirthSymbol() + indi.getBirthAsString();
        if (indi.getDeathAsString().length() > 0)
            dates += " "+OPTIONS.getDeathSymbol() + indi.getDeathAsString();

        if (dates.length() <= 0) {
            if (left)
                dates = "-+";
            else if (right)
                dates = " +";
            else if (lower)
                dates = " |";
            else
                dates = "  ";
        } else
            dates += " ";

        int padding = BLOCK_WIDTH - dates.length();
        String padded = pad(padding/2,left)+dates+pad(padding-padding/2,right);
        // here's the result
        return padded;
    }

    private String pad(int count) {return pad(count,false);}
    /**
     * padding
     */
    private String pad(int count, boolean dash) {
        String spaces = "                                              ";
        String dashes = "----------------------------------------------";
        int max = spaces.length();
        if (count < 1)
            count = 1;
        if (count > max)
            count = max;
        return (dash)?dashes.substring(max-count):spaces.substring(max-count);
    }

    private String connect() {
        return connect(true);
    }
    private String connect(boolean line) {
        if (line)
            return pad(BLOCK_WIDTH/2)+"|"+pad(BLOCK_WIDTH/2-1);
        else
            return pad(BLOCK_WIDTH);
    }

} //Report4Generations

