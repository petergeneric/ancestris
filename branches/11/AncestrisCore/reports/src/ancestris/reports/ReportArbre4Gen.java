package ancestris.reports;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import ancestris.gedcom.privacy.PrivacyPolicy;
import ancestris.util.swing.FileChooserBuilder;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.report.Report;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 * CHB - GenJ - Graphique Arbre 4 generations (02-2012)
 */
@ServiceProvider(service=Report.class)
public class ReportArbre4Gen extends Report
{

	/** option - our report types defined, the value and choices */

	int reportMinGenerations = 1;
	int reportMaxGenerations = 3;
	int index = 1;

	/** options
	 *  - numero sosa du 1er individu
	 *  - nombre de generations en partant de la 1 et considérées comme privées
	 */
	public int startSosa=1;
	public int privateGen = 0;

	/** option - évènements à afficher */

	public boolean reportDateOfBirth = true;
	public boolean reportPlaceOfBirth = true;

	public boolean reportDateOfMarriage = true;
	public boolean reportPlaceOfMarriage = true;

	public boolean reportDateOfDeath = true;
	public boolean reportPlaceOfDeath = true;

	public boolean Image_fond = false;
	public boolean Image_privee = false;
	public boolean Tous_Prenoms = false;

	/** option - Information à afficher pour chaque évènement */
	boolean showAllPlaceJurisdictions = false;

	// Events (BIRT and BAPM will be lumped together in terms of options to display)

	boolean[] dispEv = { true, true, true };
	String[] symbols = new String[3];

	/** ********************************************************************************/

	/**
	 * Main for argument individual
	 */
	public void start(Indi indi)
	{
		// Init some stuff
		PrivacyPolicy policy = PrivacyPolicy.getDefault();
                policy = PrivacyPolicy.getDefault().getAllPublic();
		Init_Variables();

		if (startSosa == 0) {
                    startSosa = 1;
                    String sosa = indi.getSosaString();
                    if (!sosa.isEmpty()) {
                        Pattern p = Pattern.compile("([\\d]+)");
                        Matcher m = p.matcher(sosa);
                        if (m.find()) {
                            startSosa = Integer.parseInt(m.group(1));
                        }
                    }
                }
                
                if (Image_privee) {
                    Image_fond = true;
                }

		ArrayList<String> All_String = new ArrayList<String>();     // toutes les datas lues
		ArrayList<String> Final_String = new ArrayList<String>();   // datas classées par individus
		ArrayList<String> Noms = new ArrayList<String>();			// noms
		ArrayList<String> Pnom = new ArrayList<String>(); 			// prénoms
		ArrayList<String> Dnai = new ArrayList<String>();			// dates de naissance
		ArrayList<String> Lnai = new ArrayList<String>();			// lieux de naissance
		ArrayList<String> Dmar = new ArrayList<String>();			// dates de mariage
		ArrayList<String> Lmar = new ArrayList<String>();			// lieux de mariage
		ArrayList<String> Ddec = new ArrayList<String>();			// dates de deces
		ArrayList<String> Ldec = new ArrayList<String>();			// Lieux de deces
		int offset = 0;					 			// position des datas dans Final_String 

		// titre du document
		String title = getTitle(indi);
		System.out.println ("titre : "+ title);// TODO

		// parents de l'individu
		debut(indi, policy, All_String, index);

		// arrangement des datas, construction de la table finale
		Classement (All_String, Final_String);

		// extraction des noms pour sosa 15 à 1 contenus dans All_String (ligne 0).
		extract_noms (Final_String, Noms, Pnom, 0, 15);

		// extraction des infos naissance pour sosa 15 à 1 contenus dans All_String.
		offset = 1 ; // ligne 1 de Final_String = naissances 
		extract_other (Final_String, Dnai, Lnai, 0, 15, offset, reportDateOfBirth, reportPlaceOfBirth, "N");

		// extraction des infos mariage pour sosa 15 à 1 contenus dans All_String.
		offset = 2 ; // ligne 2 de Final_String = mariage
		extract_other (Final_String, Dmar, Lmar, 0, 15, offset, reportDateOfMarriage, reportPlaceOfMarriage, "M");

		// extraction des infos deces pour sosa 15 à 1 contenus dans All_String.
		offset = 3 ; // ligne 3 de Final_String = deces
		extract_other (Final_String, Ddec, Ldec, 0, 15, offset, reportDateOfDeath, reportPlaceOfDeath, "D");

		Common_datas.cNoms = Noms ; // noms
		Common_datas.cPnom = Pnom ; // prénoms
		Common_datas.cDnai = Dnai ; // dates de naissance
		Common_datas.cLnai = Lnai ; // lieux de naissance
		Common_datas.cDmar = Dmar ; // dates de mariage
		Common_datas.cLmar = Lmar ; // lieux de mariage
		Common_datas.cDdec = Ddec ; // dates de deces
		Common_datas.cLdec = Ldec ; // Lieux de deces
		Common_datas.titre = title;
		Common_datas.fileToRead = "";

		// image de fond standard ou image privée ?
		if (Image_fond && Image_privee)
		{
			File file_priv = Choix_file (false);
			System.out.println ("image choisie : "+file_priv);// TODO
			if (file_priv!=null)
			{
				Common_datas.fileToRead = file_priv.toString();
			}
			else
			{
				Image_privee = false;
			}
		}

		// edition des datas
		MaFenetre_gr fen = new MaFenetre_gr() ;
		fen.setVisible(true) ;
	}

	/** ********************************************************************************/

	@SuppressWarnings("serial")
	class MaFenetre_gr  extends JFrame implements ActionListener
	{
		private JMenuItem Sortir, enreg_sous;
		private JMenu Actions;
		private JMenuBar barreMenus;


		MaFenetre_gr ()
		{
			setTitle (Common_datas.titre) ;
			setSize (800,600) ;
			pan = new Paneau_graphs();
			getContentPane().add(pan) ;
			Dimension d = new Dimension (1600,1200);
			pan.setPreferredSize(d);
			defil = new JScrollPane (pan);
			getContentPane().add(defil) ;

			barreMenus = new JMenuBar();
			setJMenuBar (barreMenus);
			Actions = new JMenu("Fichier");
			barreMenus.add(Actions);

			enreg_sous = new JMenuItem ("enregister sous");
			Actions.add(enreg_sous);
			enreg_sous.addActionListener(this);

			Sortir = new JMenuItem ("Fermer");
			Actions.add(Sortir);
			Sortir.addActionListener(this);
		}
		private JPanel pan ;
		private JScrollPane defil;

		/** ************************************************************* */

		public void actionPerformed (ActionEvent e)
		{
			Object source = e.getSource();
			if (source == Sortir)
			{
				setVisible (false);
				setDefaultCloseOperation (DISPOSE_ON_CLOSE);
			}
			if (source == enreg_sous)
			{
				File fileToStore = gestion_file();
				if (fileToStore!=null) enreg (fileToStore) ;
			}
		}
	} // end MaFenetre_gr	

	/** *********************************************************** */


	class Paneau_graphs extends JPanel
	{
        @Override
		public void  paintComponent (Graphics gx)
		{
			super.paintComponent(gx) ;
			ImageIcon img = new ImageIcon(graf());
			gx.drawImage(img.getImage(), 0, 0, null);
		}
	} //end Paneau_graphs

	/** ********************************************************************************/
	/** titre -  valeurs d'en-tête de colonne */
	String getTitle(Indi root) 
	{
		return translate("title.sosa", root.getName());
	}


	/** ********************************************************************************/

	/** debut */
	void debut(Indi indi, PrivacyPolicy policy, ArrayList<String> All_String, int index)
	{
		Fam[] fams = indi.getFamiliesWhereSpouse();
		Fam fam = null;
		if ((fams != null) && (fams.length > 0))
		{
			fam = fams[0];
		}
		else
		{
			fam = null;
		}
		recursion(indi, fam, 0, index, startSosa, policy, All_String);
	}

	/** ********************************************************************************/

	void recursion(Indi indi, Fam fam, int gen, int index, int sosa, PrivacyPolicy policy, ArrayList<String> All_String)
	{

		// stop here?
		if (gen > reportMaxGenerations)
			return;

		// let implementation handle individual
		formatIndi(indi, fam, gen, index, sosa, gen < privateGen ? PrivacyPolicy.getDefault().getAllPrivate() : policy, All_String);

		// go one generation up to father and mother
		Fam famc = indi.getFamilyWhereBiologicalChild();
		if (famc == null)
			return;

		Indi father = famc.getHusband();
		Indi mother = famc.getWife();
		if (father==null&&mother==null)
			return;

		// recurse into father
		if (father != null)
			recursion(father, famc, gen+1, index*2,  sosa*2, policy, All_String);

		// recurse into mother
		if (mother != null)
			recursion(mother, famc, gen+1, index*2+1, sosa*2+1, policy, All_String);

		// done
	}

	/** ********************************************************************************/

	void formatIndi(Indi indi, Fam fam, int gen, int index, int sosa, PrivacyPolicy policy, ArrayList<String> All_String) 
	{

		// go one generation up to father and mother
		// Go back if generation too low    
		if (gen < reportMinGenerations-1) return;

		// Pour chaque individu, nous allons stocker la liste des événements et leurs descriptions
		ArrayList<String> eventDesc = new ArrayList<String>();     //  Cartes évènement de leur description

		// a cell with sosa# and name 
		String indi_name =(getName(indi, index, sosa, policy)); // [sosa] name (id)
		All_String.add(index+"! "+indi_name);

		// then a cell with properies
		getProperties(indi, fam, policy, true, false, eventDesc);

		int j=eventDesc.size();
		if (j>0)
		{
			for (int i=0;i<j;i++)
			{
				Object tmp = eventDesc.get(i);
				All_String.add(tmp.toString());
			}
		}
		// done for now
	}

	/** ********************************************************************************/

	/** dump individual's name */
	String getName(Indi indi, int index, int sosa, PrivacyPolicy privacy)
	{
		return (sosa>0?sosa+": ":"") + privacy.getDisplayValue(indi, "NAME");
	}


	/** ********************************************************************************/

	void getProperties(Indi indi, Fam fam, PrivacyPolicy privacy, boolean usePrefixes, boolean returnEmpties, ArrayList<String> eDesc)
	{
		// Variables
		String event = "";
		String description = "";
		int ev = 0;

		// birth?
		ev = 0;
		event = "BIRT";
		if (dispEv[ev])
		{
			description = getProperty(indi, event, usePrefixes ? symbols[ev] : "", reportDateOfBirth, reportPlaceOfBirth, privacy);
			if (returnEmpties||description.length()>0)
			{
				eDesc.add("-1! "+event+";"+ description);
			}
			else
			{
				eDesc.add("-1! "+event+";"+symbols[ev]+" .");
			}

		}
		if ((!reportDateOfBirth)&(!reportPlaceOfBirth))
		{
			eDesc.add("-1! "+event+";"+symbols[ev]+" .");
		}

		// marriage?
		ev = 1;
		event = "MARR";
		if (dispEv[ev]) {
			if (fam!=null) {
				description = getProperty(fam, event, usePrefixes ? symbols[ev] : "", reportDateOfMarriage, reportPlaceOfMarriage, privacy);
				if (returnEmpties||description.length()>0)
				{
					eDesc.add("-2! "+event+";"+ description);
				}
				else
				{
					eDesc.add("-2! "+event+";"+symbols[ev]+" .");
				}
			}
		}
		if ((!reportDateOfMarriage)&(!reportPlaceOfMarriage))
		{
			eDesc.add("-2! "+event+";"+symbols[ev]+" .");
		}

		// death?
		ev = 2;
		event = "DEAT";
		if (dispEv[ev]) {
			description = getProperty(indi, event, usePrefixes ? symbols[ev] : "", reportDateOfDeath, reportPlaceOfDeath, privacy);
			if (returnEmpties||description.length()>0)
			{
				eDesc.add("-3! "+event+";"+ description);
			}
			else
			{
				eDesc.add("-3! "+event+";"+symbols[ev]+" .");
			}
		}
		if ((!reportDateOfDeath)&(!reportPlaceOfDeath))
		{
			eDesc.add("-3! "+event+";"+symbols[ev]+" .");
		}
	}

	/** ********************************************************************************/

	String getProperty(Entity entity, String tag, String prefix, boolean date, boolean place, PrivacyPolicy policy)
	{
		Property prop = entity.getProperty(tag);
		if (prop == null)
			return "";
		String format = prefix + (date ? "{ $D}," : ",") + (place && showAllPlaceJurisdictions ? "{ $P}" : "") + (place && !showAllPlaceJurisdictions ? "{ $p}" : "");
		return prop.format(format, policy).trim();
	}
	/** ********************************************************************************/

	void Classement (ArrayList<String> All_String, ArrayList<String> Final_String)
	{

		// table de correspondance des individus
		int Iedit[] = {8,9,10,11,12,13,14,15,4,5,6,7,2,3, 1};
		int Nedit = Iedit.length;

		int Icomp = 0;
		int Iallstr=All_String.size();
		int Itmp = 0;

		boolean trouve = false;

		// complement de 3 lignes à la fin de All_String
		for (int i=0;i<3;i++) All_String.add("0!");

		// classement des datas

		for (int Icptr=0;Icptr<Nedit;Icptr++)
		{
			Icomp = Iedit[Icptr];
			trouve = false;

			for (int Istr=0;Istr<Iallstr;Istr++)
			{
				String Str_Tmp = All_String.get(Istr);
				StringTokenizer tok = new StringTokenizer (Str_Tmp,"!");
				Itmp = Integer.parseInt(tok.nextToken());
				if (Icomp == Itmp )
				{
					Final_String.add(Str_Tmp);
					Addevent(All_String, Final_String, Istr);
					trouve = true;
				} //end if Icomp
			}  // end for Istr

			if (!trouve)
			{
				Final_String.add(Icomp+"! .");
				Final_String.add("-1! BIRT;* .");
				Final_String.add("-2! MARR;m .");
				Final_String.add("-3! DEAT;+ .");
			}
		} // end for Icptr

	}
	/** ********************************************************************************/
	void Addevent(ArrayList<String> All_String, ArrayList<String> Final_String, int Istr)
	{
		String[] event = { "BIRT",  "MARR", "DEAT" };
		String[] symbol = { "N ", "M ", "D "};
		int x1 = 0;
		int x2 = 0;
		for (int i=0;i<3;i++)
		{
			x1 = i+1;
			x2 = -x1;

			int Itmp = NumLine (All_String, Istr, x1);
			if (Itmp==x2)
			{
				Final_String.add(All_String.get(Istr+x1).toString());
			}
			else
			{
				Final_String.add(x2+"! "+event[x1-1]+";"+symbol[x1-1]+" .");
			} //end if Itmp==x2
		} // end for
	}

	/** ********************************************************************************/
	int NumLine (ArrayList<String> All_String, int Istr, int k)
	{
		String Str_Tmp = All_String.get(Istr+k).toString();
		StringTokenizer tok1 = new StringTokenizer (Str_Tmp,"!");
		int Itmp = Integer.parseInt(tok1.nextToken());
		return Itmp;
	}
	/** ********************************************************************************/
	void extract_noms (ArrayList<String> Final_String, ArrayList<String> noms, ArrayList<String> pnom, int i1, int i2)
	{
		String tmp1 = "", tmp2 = "";
		int p1 = 0, p2 = 0;

		for (int i=i1;i<i2;i++)
		{
			tmp1 = Final_String.get(i*4).toString();
			p1 = tmp1.indexOf("!")+1;
			p2 = tmp1.length();
			tmp2 =tmp1.substring(p1,p2);
			p1 = tmp2.indexOf(",");

			if (p1<0) // nom sans prenom
			{
				if (tmp2.indexOf("NON DENOMME")>0) tmp2 = "X";
				noms.add(tmp2);
				pnom.add(".");
			}
			else // prenoms multiples, on prend le premier
			{
				noms.add(tmp2.substring(1,p1));
				p2 = tmp2.length();                  // le nom
				tmp1 = tmp2.substring(p1+1, p2);     // les prenoms
				if (Tous_Prenoms)
				{
					p2 = tmp1.length();
					pnom.add(tmp1.substring(1, p2)); // tous les prenoms
				}
				else
				{
					p2 = tmp1.indexOf(",");
					if (p2<0) p2 = tmp1.length();
					pnom.add(tmp1.substring(1, p2)); // premier prenom
				}
			}
		}
	}

	/** ********************************************************************************/

	void extract_other (ArrayList<String> Final_String, ArrayList<String> ldate, ArrayList<String> lplac, int i1, int i2, int i3, boolean date, boolean place, String sym)
	{
		String tmp1 = "", tmp2 = "";
		int p1 = 0, p2 = 0, k = 0;
		if (!date & !place) k = 1;
		if (date & !place)  k = 2;
		if (!date & place)  k = 3;
		if (date & place)   k = 4;

		for (int i=i1;i<i2;i++)
		{
			tmp1 = Final_String.get(i*4+i3).toString();
			p1 = tmp1.indexOf(";")+1;
			p2 = tmp1.length();
			tmp2 =tmp1.substring(p1,p2);

			switch (k)
			{
				case 1 : // !date & !place
					ldate.add (sym+" .");
					lplac.add (" .");
					break;

				case 2 : // date & !place

					p1 = tmp2.indexOf(",");
					if (p1<0)
					{
						ldate.add (sym+" .");
						lplac.add (" .");
					}
					if (p1>0)
					{
						ldate.add (sym+tmp2.substring( 1, p1 ));
						lplac.add (" .");
					}
					break;

				case 3 : // !date & place

					p1 = tmp2.indexOf(",");
					if (p1<0)
					{
						ldate.add (sym+" .");
						lplac.add (" .");
					}
					if (p1>0)
					{
						ldate.add (sym+" .");
						lplac.add (tmp2.substring( p1+1, tmp2.length() ));
					}
					break;

				case 4 : // date & place	

					p1 = tmp2.indexOf(",");
					if (p1<0)
					{
						ldate.add (sym+" .");
						lplac.add (" .");
					}
					if (p1==1)
					{
						ldate.add (sym+" .");
						lplac.add (tmp2.substring( p1+1, tmp2.length() ));
					}
					if (p1>1)
					{
						ldate.add (sym+tmp2.substring( 1, p1 ));
						lplac.add (tmp2.substring( p1+1, tmp2.length() ));
					}
					break;

			} // end switch
		} // end for
	}  // end extract_other 

	/** ********************************************************************************/

	/** Initialises variables for all displays   */
	void Init_Variables()
	{
		// Assign events to consider and their characteristics
		symbols[0] = "N"; //OPTIONS.getBirthSymbol();
		symbols[1] = "M"; //OPTIONS.getMarriageSymbol(); 
		symbols[2] = "D"; //OPTIONS.getDeathSymbol(); 

		// No source should be displayed for events that are not to be displayed
		dispEv[0] = reportDateOfBirth    || reportPlaceOfBirth;
		dispEv[1] = reportDateOfMarriage || reportPlaceOfMarriage;
		dispEv[2] = reportDateOfDeath    || reportPlaceOfDeath;

	}

	/** ********************************************************************************/

	static class Common_datas 
	{
		public static ArrayList<String> cNoms ; // noms
		public static ArrayList<String> cPnom ; // prénoms
		public static ArrayList<String> cDnai ; // dates de naissance
		public static ArrayList<String> cLnai ; // lieux de naissance
		public static ArrayList<String> cDmar ; // dates de mariage
		public static ArrayList<String> cLmar ; // lieux de mariage
		public static ArrayList<String> cDdec ; // dates de deces
		public static ArrayList<String> cLdec ; // Lieux de deces
		public static String titre;
		public static String fileToRead;
	}
	// end Common_datas

	/** ****************************************************************** */

	static class Ajust_String 
	{
		public static String coupe1 (Graphics g, String texte_in, int larg )
		// coupe les noms de lieux à la taille des cadres
		{
			FontMetrics fm0 = g.getFontMetrics();
			int lg = fm0.stringWidth(texte_in);
			int len = texte_in.length();
			larg = larg - 20;
			String texte_out = "";
			int i = 0;
			if (lg>larg)
			{
				do
				{
					texte_out = texte_out + texte_in.substring(i,i+1);
					i++;
					if (fm0.stringWidth(texte_out)>larg) break;

				}
				while (i<len);
			}
			else
			{
				texte_out = texte_in;
			}
			return texte_out;
		}

		/** ****************************************************************** */

		public static String coupe2(String texte_org, String texte_court)
		// reprend la reste de la chaine 'coupée'
		{
			int len_to = texte_org.length();
			int len_ct = texte_court.length();
			String texte_out = texte_org.substring(len_ct,len_to);

			return texte_out;
		}

		/** ****************************************************************** */

		public static String coupe3 (Graphics g, String texte_in, int larg )
		// coupe les prénoms au droit des séparateurs ","
		{
			FontMetrics fm0 = g.getFontMetrics();
			int lg = fm0.stringWidth(texte_in);
			int len = texte_in.length();
			larg = larg - 20;
			String texte_out = "";

			if (lg>larg)
			{
				int i = 0, k = 0;
				int[] pn = {0,0,0,0,0,0,0,0};
				String tst = ",";

				for (i=0; i<len; i++) // recherche la position des ","
				{
					String ch = texte_in.substring(i,i+1);
					if (ch.equals(tst))
					{
						pn[k] = i;
						k++;
					} //end if ch
				} // end for i
				k--;

				i = 0;
				do // construit la chaine 
				{
					texte_out = texte_in.substring(0,pn[i]+1);
					if (fm0.stringWidth(texte_out)>larg )
					{
						texte_out = texte_in.substring(0,pn[i-1]+1);
						return texte_out;
					}
					i++;
				}
				while (i<=k);
			}
			else
			{
				texte_out = texte_in;
			}
			return texte_out;
		}
		// end Ajust_String 
	}

	/** ****************************************************************** */

	static class Draw_cadres
	{
		public static int Individu(Graphics grx, Color Cind, int xpos, int ypos, 
				int larg, int hautI, int interval, int decalV,
				int espace, int k1, int k2) 
		{
			String txt_tmp = null;

			for (int i=k1;i<=k2;i++)
			{
				grx.setColor (Cind) ;
				grx.fillRoundRect (xpos, ypos, larg, hautI, 10, 10) ;
				grx.setColor (Color.black) ;
				grx.drawRoundRect (xpos, ypos, larg, hautI, 10, 10) ;
				grx.drawString (Common_datas.cNoms.get(i).toString(), xpos + 5, ypos + espace);
				txt_tmp = Ajust_String.coupe3 (grx, Common_datas.cPnom.get(i).toString(), larg );
				grx.drawString (txt_tmp, xpos + 5, ypos + espace + decalV);
				txt_tmp = Ajust_String.coupe2 (Common_datas.cPnom.get(i).toString(), txt_tmp );
				grx.drawString (txt_tmp, xpos + 5, ypos + espace + decalV * 2);
				grx.drawString (Common_datas.cDnai.get(i).toString(), xpos + 5, ypos + espace + decalV * 4);
				txt_tmp = Ajust_String.coupe1 (grx, Common_datas.cLnai.get(i).toString(), larg );
				grx.drawString (txt_tmp, xpos + 5, ypos + espace + decalV * 5);
				txt_tmp = Ajust_String.coupe2 (Common_datas.cLnai.get(i).toString(), txt_tmp );
				grx.drawString (txt_tmp, xpos + 5, ypos + espace + decalV * 6);
				grx.drawString (Common_datas.cDdec.get(i).toString(), xpos + 5, ypos + espace + decalV * 8);
				txt_tmp = Ajust_String.coupe1 (grx, Common_datas.cLdec.get(i).toString(), larg );
				grx.drawString (txt_tmp, xpos + 5, ypos + espace + decalV * 9);
				txt_tmp = Ajust_String.coupe2 (Common_datas.cLdec.get(i).toString(), txt_tmp );
				grx.drawString (txt_tmp, xpos + 5, ypos + espace + decalV * 10);
				xpos = xpos + larg + interval;
			}
			return 0;
		}

		/** ****************************************************************** */

		public static int Famille(Graphics2D grx, Color Cfam, int xpos, int ypos,
				int larg, int hautF, int interval, int decalV, int espace, int k1, int k2)
		{
			String txt_tmp = null;
			for (int i=k1;i<=k2;i++)
			{
				grx.setColor (Cfam) ;
				grx.fillRoundRect (xpos, ypos, larg, hautF, 10, 10) ;
				grx.setColor (Color.black) ;
				grx.drawRoundRect (xpos, ypos, larg, hautF, 10, 10) ;
				grx.drawString (Common_datas.cDmar.get((i-8)*2).toString(), xpos + 5, ypos + espace);
				txt_tmp = Ajust_String.coupe1 (grx, Common_datas.cLmar.get((i-8)*2).toString(), larg );
				grx.drawString (txt_tmp, xpos + 5, ypos + espace + decalV);
				txt_tmp = Ajust_String.coupe2 (Common_datas.cLmar.get((i-8)*2).toString(), txt_tmp );
				grx.drawString (txt_tmp, xpos + 5, ypos + espace + decalV * 2);
				xpos = xpos + larg + interval;
			}
			return 0;
		}
		// end Draw_cadres
	}

	/** ********************************************************************************/
	/** construction de l'image */
	public BufferedImage graf()
	{
		ImageIcon file_fond;
		int x=0 , y=0;
		int x_max = 1600 , y_max = 1200 , marge = 20;

		BufferedImage tampon = new BufferedImage(x_max, y_max, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D grx = tampon.createGraphics();
		grx.setColor(Color.WHITE);
		grx.fillRect(x, y, x_max, y_max);

		if (Image_fond)
		{
			if (Image_privee)
			{
				file_fond = new ImageIcon(Common_datas.fileToRead);
			}
			else
			{
                                file_fond = new ImageIcon(getClass().getResource("ReportArbre4Gen_fond.jpg"));
			}
			grx.drawImage(file_fond.getImage(), marge, marge, x_max - marge*2, y_max - marge*2, null);
		}

		grx.setColor(Color.black);
		grx.drawRect (marge, marge, x_max - marge*2, y_max - marge*2) ;

		grx.setFont (new Font ("Arial", Font.BOLD, 20));
		grx.setColor (Color.YELLOW) ;
		grx.fillRoundRect ((x_max-800)/2, 30, 800, 40, 10, 10) ;
		grx.setColor (Color.black) ;
		grx.drawRoundRect ((x_max-800)/2, 30, 800, 40, 10, 10) ;
		FontMetrics fm0 = grx.getFontMetrics();
		int lg = fm0.stringWidth(Common_datas.titre);
		grx.drawString (Common_datas.titre, (x_max-lg)/2, 55);

		grx.setFont (new Font ("Lucida Sans", Font.BOLD, 10));
		FontMetrics fm = grx.getFontMetrics();
		Color Cind = new Color (255, 255, 200);
		Color Cfam = new Color (200, 255, 255);

		// 8 rectangles a coins arrondis de couleur jaune GENERATION 4

		x_max = 1600 - marge*2;
		y_max = 1200 - marge*2;
		int distI = 40, distF = 30 ;
		int larg = 175, hautI = 160 , hautF = 60;
		int interval = (x_max - 8*larg)/8;
		int xpos = interval / 2 + marge;
		int ypos = 30 + 40 + distI;
		int decalV = fm.getHeight(), espace = 15;
		xpos = Draw_cadres.Individu (grx, Cind, xpos, ypos,	larg, hautI, interval, decalV, espace, 0, 7 );

		// 4 rectangles a coins arrondis de couleur bleue MARIAGES G4

		interval = (x_max - 4*larg)/4;
		ypos = ypos + hautI + distF ;
		xpos = interval / 2 + marge ;
		xpos = Draw_cadres.Famille (grx, Cfam, xpos, ypos, larg, hautF, interval, decalV, espace, 8, 11 );

		// 4 rectangles a coins arrondis de couleur jaune GENERATION 3

		ypos = ypos + hautF + distI ;
		xpos = interval / 2 + marge ;
		xpos = Draw_cadres.Individu (grx, Cind, xpos, ypos,	larg, hautI, interval, decalV, espace, 8, 11 );

		// 2 rectangles a coins arrondis de couleur bleue MARIAGE G3

		interval = (x_max - 2*larg)/2;
		ypos = ypos + hautI + distF ;
		xpos = interval / 2 + marge ;
		xpos = Draw_cadres.Famille (grx, Cfam, xpos, ypos, larg, hautF, interval, decalV, espace, 12, 13 );

		// 2 rectangles a coins arrondis de couleur jaune GENERATION 2

		ypos = ypos + hautF + distI ;
		xpos = interval / 2 + marge ;
		xpos = Draw_cadres.Individu (grx, Cind, xpos, ypos,	larg, hautI, interval, decalV, espace, 12, 13 );

		// 1 rectangle a coins arrondis de couleur bleue MARIAGE G2 

		interval = (x_max - larg)/2;
		ypos = ypos + hautI + distF ;
		xpos = interval + marge ;
		xpos = Draw_cadres.Famille (grx, Cfam, xpos, ypos, larg, hautF, interval, decalV, espace, 14, 14 );

		// 1 rectangle a coins arrondis de couleur jaune SOSA 

		interval = (x_max - larg)/2;
		ypos = ypos + hautF + distI ;
		xpos = interval + marge ;
		xpos = Draw_cadres.Individu (grx, Cind, xpos, ypos,	larg, hautI, interval, decalV, espace, 14, 14 );

		return tampon ;
	} // end graf()

	/** ********************************************************************************/

	public File Choix_file(boolean rw)
	{
                FileChooserBuilder fcb = new FileChooserBuilder(ReportArbre4Gen.class)
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(translate("TITL_ChooseFile", ""))
                .setDefaultExtension(FileChooserBuilder.getImageFilter().getExtensions()[0])
                .setFileFilter(FileChooserBuilder.getImageFilter())
                .setAcceptAllFileFilterUsed(true)
                .setFileHiding(true)
                .setDefaultDirAsReportDirectory();
                
                File file = rw ? fcb.showSaveDialog() : fcb.showOpenDialog();
                return file;
	} // end Choix_file

	/** ****************************************************************** */

	protected File gestion_file ()
	{

		File SelectedFile = Choix_file(true);  // true pour store
		return SelectedFile;

	} // end gestion_file

	/** ****************************************************************** */

	public void enreg (File file_name)
	{
		BufferedImage buff_image = graf ();
		try
		{
			if(file_name.toString().endsWith("png"))
				ImageIO.write(buff_image, "PNG", file_name);
			else ImageIO.write(buff_image, "PNG", new File(file_name.toString()+".png"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println ("enregistrement fichier : "+file_name);// TODO
	} // end enreg

} // Report_Arbre_4Gen done


