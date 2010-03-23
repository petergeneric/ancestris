/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.report.Report;
import genj.util.swing.Action2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.LinkedList;

/**
 * GenJ - ReportPSCirc
 * adapted from LifeLines ps-fan report
 *
 * TODO Daniel
 * - multipages split (ne pas oublier gsave grestore )
* - intergre la fenetre de choix de fichier
 */
public class ReportLinesFan extends Report {

    private PrintWriter writer;
    private final static Charset CHARSET = Charset.forName("ISO-8859-1");
    public int genPerPage = 6;
    public int reportMaxGenerations = 999;
    public boolean useColors=true;
    private int pageNo;


    private LinkedList indiList = new LinkedList();

    /**
     * Helper - Create a PrintWriter wrapper for output stream
     */
    private PrintWriter getWriter(OutputStream out) {
	return new PrintWriter(new OutputStreamWriter(out, CHARSET));
    }

    /**
     * Main for argument individual
     */
    public void start(Indi indi) {

        File file = getFileFromUser(translate("output.file"), Action2.TXT_OK,true);
        if (file == null)
        return ;

	try{

/*
      // .. exits ?
      if (file.exists()) {
        int rc = manager.getWindowManager().openDialog(null, title, WindowManager.WARNING_MESSAGE, "File exists. Overwrite?", WindowManager.ACTIONS_YES_NO, ReportView.this);
        if (rc!=0) {
          return;
        }
      }
*/
	    writer = getWriter(new FileOutputStream(file));
	    Reader in = new InputStreamReader(getClass().getResourceAsStream("ps-fan.ps"));

	    int c;
	    writer.println("%!PS-Adobe-3.0");
	    writer.println("%%Creator: genj 1.0");
	    writer.println("%%CreationDate: ");
	    writer.println("%%PageOrder: Ascend");
	    writer.println("%%Orientation: Landscape");
	    writer.println("%%EndComments");
	    writer.println("/maxlevel "+genPerPage+" def");
	    writer.println("/color "+(useColors?"true":"false")+" def");

	    while ((c = in.read()) != -1)
		writer.write(c);
	    in.close();
	}catch(IOException ioe){
	    System.err.println("IO Exception!");
	    ioe.printStackTrace();
	}

	//	indiList.add(indi);
	//indiList.add(new Integer(1));
	indiList.add(indi);
	indiList.add(new Integer(1));
	pageNo = 1;

	while (!indiList.isEmpty()){
	    Indi indiIterator = (Indi)(indiList.removeFirst());
	    //	    indiList.remove(0);
	    Integer genIndex = (Integer) (indiList.removeFirst());
	    //indiList.remove(0);
	    if (genIndex != null){
		writer.println("gsave");
		pedigree(1,genIndex.intValue(),1,1,indiIterator);
		writer.println("showpage");
		pageNo++;
		writer.println("grestore");
	    }
	}
	writer.flush();
        writer.close();

        // show file the result to the user
        showFileToUser(file);

    }

    private void  pedigree (int in, int gen, int lev, int ah, Indi indi){
	if (indi == null){
	    return;
	}
	if (gen > reportMaxGenerations){ return;}
	writer.println("("+fullname(indi,1,1,50)+")");
	if (in < 7) {
	    writer.println(" ("+esc(indi.format("BIRT", OPTIONS.getBirthSymbol()+"{$D}"))+")"+
			" ("+esc(indi.format("DEAT",OPTIONS.getDeathSymbol()+"{$D}"))+")");
	}else if (in == 7){
	    writer.println(" ("+esc(indi.format("BIRT",OPTIONS.getBirthSymbol()+"{$y}"))+")"+
			" ("+esc(indi.format("DEAT", OPTIONS.getDeathSymbol()+"{$y}"))+")");
	} else {
	    writer.println(" () () ");
	}

	Fam famc = indi.getFamilyWhereBiologicalChild();
	// test if link
	if (in < genPerPage ||
	    famc == null) {
	    writer.println(" "+(in-1)+
			" "+(ah-lev)+
			" i");
	} else {
	    indiList.add(indi);
	    indiList.add(new Integer(gen));
	    writer.println(" "+(in-1)+
			" "+(ah-lev)+
			" "+(indiList.size()/2+pageNo)+
			" j");
	}

        if (in < genPerPage) {
	    // And we loop through its ascendants
	    if (famc==null) {
		return;
	    }
	    Indi father = famc.getHusband();
	    Indi mother = famc.getWife();
	    pedigree(in+1, gen+1, lev*2, ah*2, father);
	    pedigree(in+1, gen+1, lev*2, ah*2+1, mother);
	}

    }
    /*
      Fullname returns the name of a person in a variety of formats.
      If the second parameter is true the surname is shown in upper case;
      otherwise the surname is as in the record.
      If the third parameter is true the parts of the name are shown in the order
      as found in the record; otherwise the surname is given first, followed
      by a comma, followed by the other name parts.
      The fourth parameter specifies the maximum length field that can be used
      to show the name; various conversions occur
      if it is necessary to shorten the name to fit this length.
    */
    private String fullname(Indi indi,int isUpper,int type,int length){
	return esc(indi.getName());
    }

    private String esc(String s){
	String result;
	result = s.replaceAll("\\\\","\\\\\\\\");
	result = result.replaceAll("\\(","\\\\(");
	result = result.replaceAll("\\)","\\\\)");
	return result;
    }


} //ReportLinesFan
