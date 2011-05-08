package genjreports.gedart;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
/**
 * TODO Daniel: inclure dans la liste les sources, repo, ... fictifs pour faire un tri
 * TODO Daniel: classer les colonnes au choix, avec plusieurs cle
 * TODO Daniel: limiter aux evenements/general/tous
 * TODO Daniel: differencier les todos sur evt des todo globaux
 * TODO Daniel: ligne blanche entre la fin des taches, et le resume
 * TODO Daniel: pouvoir lancer le rapport sur une lignée (asc ou desc)
 * TODO Daniel: dans le cas asc ou desc trier par generation/sosa
 * TODO Daniel: choisir d'inclure ou non les todo
 * TODO Daniel: pouvoir sortir le rapport dans une autre langue
 * TODO Daniel: Format de la fiche de travail:
 l'adoption, la naturalisation,l'emigration, l'immigration
 Plus une NOTE generale
 au niveau de la naissance, 
 au niveau du decès
 au niveau d'un evenement quelconque, ça fait une sorte de champ memo. Ceci bien sur ne vaut qu epour la fiche de travail
 la date du diplome, et la date de la retraite (evenement : Graduate, et Retired)
 liste type "checkboxes" de pouvoir cocher ce qu'on voulait comme 'events", dans une fiche de travail, 
 ou bien un champ texte pour rentrer les EVEN (PATH) 
 */
/**
 * TODO: 	pouvoir selectionner les proprietes. par exemple
 * 			"EVEN:TYPE=CONF": voir la selection des todo sous forme de note
 * TODO:	formatter les note (par exemple mettre un lien cliquable
 * TODO:	inclure le rapport de controle de validite
 * TODO: 	inclure les generation au dessus et en dessous (parents, gdparents, fratrie, enfants
 * TODO: 	recuperer l'evt lors d'une asso
 * TODO:	recuperer CHAN:DATE
 * TODO:	mettre une arbo des templates
 * TODO:	faire une extension de genj pour g�rer des rapports type gedart
 * TODO:	Aspect multilangue  
 * TODO:	voir getOptionsFromUser
 * TODO:	ajouter une m�thode pour trier: comme  public Entity[] getEntities(String tag, Comparator comparator) dans Gedcom
 * TODO:	ajouter des methodes 'getancestors getdescendants dans reportIndi
 * TODO:	Ajouter un fichier de properties pour pouvoir demander des options 
 * 
 */
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;
import genj.util.swing.Action2;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;

import javax.swing.ImageIcon;

/**
 * GenJ - Report
 * 
 * @author Daniel ANDRE <daniel.andre@free.fr>
 * @version 1.0
 */
/*
 * Liste des pistes et fonctions:
 * - lancer le rapport sur tous les indi lies à une personne	non faire un outils avance de selection des indis
 * - Ascendance/Descendance d'un indi ou une fam				non idem
 * - gestion des prives
 * - options:
 *   - limiter aux todos
 * 
 */
public class ReportGedart extends Report {
	private GedartTemplates gedartTemplates = new GedartTemplates();
	private GedartTemplate[] gedartTemplatesOption = gedartTemplates.toArray();
	private String todoTag = "NOTE"; // TODO: remettre public
	private String todoStart = "TODO:"; // TODO: remettre public
	public boolean includeIndi = true;
	public boolean includeFam = true;
	public boolean includeTOC = false;
	public boolean includeIndex = false;
	private boolean outputSummary = false; // TODO: remettre public
	public boolean showID = true;
	private boolean isTodo = false; // TODO: remettre public
	public boolean includeBlankCells = false;
	private boolean isOneFile = true; // TODO: remettre public
//	public boolean showInReportWindow = true;
	public boolean saveReportToFile = true;
	public int template;
	public String[] templates = GedartTemplate.getDescription(gedartTemplatesOption);
	// private PrintWriter out;
	private DocReport mydoc;
	private Gedcom theGedcom;
	
	public Object accepts(Object context) {
		return (gedartTemplates.toArray(context));
		
	}
	/**
	 * Overriden image - we're using the provided FO image
	 */
	protected ImageIcon getImage() {
		return Report.IMG_FO;
	}

	/**
	 * we're not generating anything to stdout anymore aside from debugging ino
	 */
	public boolean usesStandardOut() {
		return true;
	}

	/**
	 * The report's entry point
	 */
	public void start(Gedcom gedcom, GedartTemplate template) {
		theGedcom=gedcom;
		process(gedcom.getEntities("INDI", "INDI:NAME"), 
				gedcom.getEntities("FAM", "FAM:HUSB:*:..:NAME"),
				template);
	}

	/**
	 * The report's entry point - for a single individual
	 */
	
	public void start(Indi indi, GedartTemplate template) {
		theGedcom=indi.getGedcom();
		process(new Indi[] { indi }, new Fam[] {}, template);
	}
	
	public void start(Indi[] indis, GedartTemplate template) {
		theGedcom=indis[0].getGedcom();
		process(indis, new Fam[0],template);
	}

	/**
	 * The report's entry point - for a single family
	 */
	public void start(Fam fam, GedartTemplate template) {
		theGedcom=fam.getGedcom();
		process(new Indi[] {}, new Fam[] { fam },template);
	}

	public void start(Fam[] fams, GedartTemplate template) {
		theGedcom=fams[0].getGedcom();
		process(new Indi[0], fams,template);
	}

	public void start(Object context) throws Throwable{
		start(context,null);
	}
	

		/**
		 * Main logic
		 * 
		 * @param indis
		 * @param fams
		 */
	private void process(Entity[] indis, Entity[] fams, GedartTemplate  usetemplate) {
		String thetemplate;
		String extension =null;
		if (usetemplate == null)
			usetemplate = gedartTemplatesOption[template];
		
		thetemplate = usetemplate.getPath();
		LOG.log(Level.INFO,"template:"+thetemplate );
		extension = usetemplate.getFormat();
		// if only one item, special case
		File file = null;
		boolean isOneEntity = ((indis.length + fams.length) <= 1);
		if (isOneEntity) {
			isTodo = false;
		}
		// Init todo tags
		PropertyTodo.setTag(todoStart, todoStart);

		// TODO: voir mode multifile if (isOneFile){
		// create an output document
		// ask for file
		LOG.log(Level.INFO,"tofile="+saveReportToFile);
		if (!saveReportToFile)
			try{
				file = File.createTempFile("GenJ-", "-gedart");
			}catch (IOException ioe) {file = null;}
		else{
			file = getFileFromUser(translate("output.file"), Action2.TXT_OK, true,extension,true);
		}

		if (file == null){
			LOG.log(Level.INFO,"file = null");
			return;
		}

		LOG.log(Level.INFO,"file="+file);
		// open output stream
		try {
			mydoc = new DocReport(file, gedartTemplates,theGedcom.getEncoding());
			mydoc.put("options", this);
			mydoc.put("TEMPLATE",thetemplate);
		} catch (IOException ioe) {
			System.err.println("IO Exception!");
			ioe.printStackTrace();
			return; // abort
		}
		mydoc.put("INDIS", indis);
		mydoc.put("FAMS", fams);
		mydoc.put("GEDCOM", theGedcom);

		// TODO: faire autrement
		// generate a summary?
		// if (outputSummary && isTodo && !isOneEntity) {
		// mydoc.startSection("Liste des t�ches");
		// exportSummary(indis);
		// exportSummary(fams);
		// }

//		mydoc.render(thetemplate+"/index.vm");
		mydoc.render((new File(thetemplate,"index.vm")).getPath());
		mydoc.close();
		if (file.length() < 1000000l){
			try {
				println("" + file.toURL());
			} catch (MalformedURLException e) {}
		} else {
			if (!saveReportToFile)
				println("Attention: le fichier est trop gros pour �tre affich� dans cette fen�tre");
			println("Le fichier "+file.getPath()+" a �t� cr��.");
			if (file.length() > 10000000l)
				println("Attention: le fichier est trop gros pour �tre affich� par votre navigateur");
			else 
				showFileToUser(file);
			println("Taille du fichier: "+file.length());
		}
		LOG.log(Level.INFO,"taille du fichier={0}",file.length());
	}

	/**
	 * Exports a family
	 */
	// TODO: voir les diverses var et controler
	// void exportEntityold(Fam fam) {
	//
	// Property prop;
	// Property[] propArray;
	// List<PropertyTodo> todos;
	// Indi tempIndi;
	// Fam tempFam;
	// String theTitle = cleanID(fam.toString());
	//
	// todos = PropertyTodo.findTodos(fam);
	// if (isTodo && todos.size() == 0)
	// return ;
	//
	// mydoc.addTOCEntry(theTitle, fam.getId(), 1);
	// String ln = fam.getHusband() == null ? "?" : fam.getHusband()
	// .getLastName();
	// ln = stringOrQm(ln);
	// String fn = fam.getWife() == null ? "?" : fam.getWife().getLastName();
	// fn = stringOrQm(fn);
	// mydoc.addIndexTerm(ln.substring(0, 1) /*translate("indi-index-ttl")*/,
	// ln, "- " + fn);
	// mydoc.put("title", theTitle);
	// mydoc.put("FAM", fam);
	//		
	//
	// /** ************** Notes */
	// propArray = fam.getProperties("NOTE");
	// ArrayList<StringTokenizer> notes = new ArrayList<StringTokenizer>();
	// for (int i = 0; i < propArray.length; i++) {
	// prop = propArray[i];
	// if (todos.contains(prop))
	// continue;
	// notes.add(new StringTokenizer(prop.getDisplayValue(), "\n"));
	// }
	// mydoc.put("NOTES", notes);
	// mydoc.put("TODOS",todos);
	// mydoc.render("famSheet.vm");
	// // done with fam
	// }
	/**
	 * Exports an individual
	 */
	// private void exportEntity(Indi indi) {
	//
	// // Property prop;
	// // Property[] propArray;
	//	
	// List<PropertyTodo> todos = PropertyTodo.findTodos(indi);
	// if (isTodo && todos.size() == 0)
	// return ;
	//
	// mydoc.put("INDI",indi);
	//
	// //TODO: mettre dans reportIndi
	// // /** ************** Notes */
	// // propArray = indi.getProperties("NOTE");
	// // ArrayList<StringTokenizer> notes = new ArrayList<StringTokenizer>();
	// // for (int i = 0; i < propArray.length; i++) {
	// // prop = propArray[i];
	// // if (todos.contains(prop))
	// // continue;
	// // notes.add(new StringTokenizer(prop.getDisplayValue(), "\n"));
	// // }
	// //
	// // mydoc.put("NOTES", notes);
	// // mydoc.put("TODOS", todos);
	// mydoc.render("indiSheet.vm");
	// }
	/**
	 * Export todo summary only into a 5 column table
	 */
	// TODO: faire autrement
	// void exportSummary(Entity[] ents) {
	// ArrayList<PropertyTodo> alltodos = new ArrayList<PropertyTodo>();
	// int nbTodos = 0;
	//
	// mydoc.put("headers", new String[] { translate("titletodos"),
	// translate("evt.col"), translate("date.col"),
	// translate("place.col"), translate("indi.col"),
	// translate("todo.col") });
	//
	// // loop over all entities
	// for (int e = 0; e < ents.length; e++) {
	//
	// alltodos.addAll(PropertyTodo.findTodos(ents[e]));
	// }
	// // done
	// mydoc.put("TODOS", alltodos);
	// mydoc.put("nbtodos", nbTodos);
	// mydoc.render("todoSummary.vm");
	// }

}
