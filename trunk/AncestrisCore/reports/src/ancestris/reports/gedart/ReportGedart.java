package ancestris.reports.gedart;

/*
 * FIXME: le rapport gedart sera totalement reecrit pour ancestris sous la forme d'un module
 * ce module sera dans le meme esprit que gedart c'est a dire permettre a tous de creer ses rapport
 * mais probablement base sur d'autre technologies de reporting (voir par exemple gasper ou les
 * possibilites de script dans java 1.6) */
/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 */
/**
 * TODO Daniel: inclure dans la liste les sources, repo, ... fictifs pour faire
 * un tri 
 * TODO Daniel: classer les colonnes au choix, avec plusieurs cle 
 * TODO Daniel: limiter aux evenements/general/tous 
 * TODO Daniel: differencier les todos sur evt des todo globaux 
 * TODO Daniel: ligne blanche entre la fin des taches, et le resume 
 * TODO Daniel: pouvoir lancer le rapport sur une lignée (asc ou desc) 
 * TODO Daniel: dans le cas asc ou desc trier par generation/sosa
 * TODO Daniel: choisir d'inclure ou non les todo 
 * TODO Daniel: pouvoir sortir le rapport dans une autre langue 
 * TODO Daniel: Format de la fiche de travail: l'adoption, la naturalisation,
 * l'emigration, l'immigration Plus une NOTE generale au niveau de la naissance, 
 * au niveau du decès au niveau d'un evenement quelconque, ça fait une sorte de 
 * champ memo. Ceci bien sur ne vaut que pour la fiche de travail la date du 
 * diplome, et la date de la retraite (evenement : Graduate, et Retired) liste 
 * type "checkboxes" de pouvoir cocher ce qu'on voulait comme 'events", dans 
 * une fiche de travail, ou bien un champ texte pour rentrer les EVEN (PATH)
 */
/**
 * TODO: pouvoir selectionner les proprietes. 
 * par exemple "EVEN:TYPE=CONF": voir la selection des todo sous forme de note 
 * TODO: formatter les note (par exemple mettre un lien cliquable 
 * TODO: inclure le rapport de controle de validite 
 * TODO: inclure les generation au dessus et en dessous (parents, gdparents, 
 * fratrie, enfants 
 * TODO: recuperer l'evt lors d'une asso 
 * TODO: recuperer CHAN:DATE 
 * TODO: mettre une arbo des templates 
 * TODO: faire un plugin ancestris pour gerer des rapports type gedart 
 * TODO: Aspect multilangue 
 * TODO: voir getOptionsFromUser 
 * TODO: ajouter une methode pour trier: comme public Entity[] getEntities
 * (String tag, Comparator comparator) dans Gedcom 
 * TODO: ajouter des methodes 'getancestors getdescendants dans reportIndi 
 * TODO: Ajouter un fichier de properties pour pouvoir demander des options
 *
 */
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.reports.relatives.ReportRelatives;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.openide.util.Exceptions;

import org.openide.util.lookup.ServiceProvider;

/**
 * Ancestris - Report
 *
 * @author Daniel ANDRE <daniel.andre@free.fr>
 * @version 1.0
 */
/*
 * Liste des pistes et fonctions:
 * - lancer le rapport sur tous les indi lies a une personne	non faire un outils avance de selection des indis
 * - Ascendance/Descendance d'un indi ou une fam				non idem
 * - gestion des prives
 * - options:
 *   - limiter aux todos
 * 
 */
@ServiceProvider(service = Report.class)
public class ReportGedart extends Report {

    private String todoTag = "NOTE"; // TODO: remettre public
    private String todoStart = "TODO:"; // TODO: remettre public
    private boolean outputSummary = false; // TODO: remettre public
    private boolean isTodo = false; // TODO: remettre public
    private boolean isOneFile = true; // TODO: remettre public
    private DocReport mydoc;
    private Gedcom theGedcom;
//	public boolean showInReportWindow = true;
    // private PrintWriter out;

    private GedartTemplates gedartTemplates = new GedartTemplates();
    private GedartTemplate[] gedartTemplatesOption = gedartTemplates.toArray();
    public int templateModel = 0;
    public String templateModels[] = GedartTemplate.getDescription(gedartTemplatesOption);
    public boolean includeIndi = true;
    public boolean includeFam = true;
    public boolean includeBlankCells = false;
    public boolean showID = true;
    public boolean includeTOC = false;
    public boolean includeIndex = false;
    public boolean saveReportToFile = true;

        

    /**
     * The report's entry point
     */
    public File start(Gedcom gedcom, GedartTemplate template) {
        theGedcom = gedcom;
        
        // This report cannot be run on the whole gedcom. It does not make sense.
        // Therefore we will use for instance the result of the advanced research view
        List<Entity> searchResult = getSearchEntities(gedcom);
        // If empty, default to the list of relatives as an initial set of indivuals and families to avoid an empty report
        if (searchResult.isEmpty()) {
            Indi indi = getActiveIndi(gedcom);
            if (indi != null) {
                List<Indi> relatives = new ReportRelatives().getRelatives(indi);
                searchResult = new ArrayList<>(relatives);
            }
        }

        List<Entity> indis = new ArrayList<>();
        List<Entity> fams = new ArrayList<>();
        for (Entity ent : searchResult) {
            if (ent instanceof Indi) {
                indis.add(ent);
            } else if (ent instanceof Fam) {
                fams.add(ent);
            }
        }
        
        return process(indis.toArray(new Entity[indis.size()]),
                       fams.toArray(new Entity[fams.size()]),
                       template);
    }

    /**
     * The report's entry point - for a single individual
     */
    public File start(Indi indi, GedartTemplate template) {
        theGedcom = indi.getGedcom();
        return process(new Indi[]{indi}, new Fam[]{}, template);
    }

    public File start(Indi[] indis, GedartTemplate template) {
        theGedcom = indis[0].getGedcom();
        return process(indis, new Fam[0], template);
    }

    /**
     * The report's entry point - for a single family
     */
    public File start(Fam fam, GedartTemplate template) {
        theGedcom = fam.getGedcom();
        return process(new Indi[]{}, new Fam[]{fam}, template);
    }

    public File start(Fam[] fams, GedartTemplate template) {
        theGedcom = fams[0].getGedcom();
        return process(new Indi[0], fams, template);
    }

    public String accepts(Object context) {
        return (getName());
//		return (gedartTemplates.toArray(context));
    }

    public File start(Object context) throws Throwable {
        if (context instanceof Gedcom) {
            return start((Gedcom) context, null);
        }
        if (context instanceof Indi) {
            return start((Indi) context, null);
        }
        if (context instanceof Fam) {
            return start((Fam) context, null);
        }
        if (context instanceof Indi[]) {
            return start((Indi[]) context, null);
        }
        if (context instanceof Fam[]) {
            return start((Fam[]) context, null);
        }
        return null;
    }

    /**
     * Main logic
     *
     * @param indis
     * @param fams
     */
    private File process(Entity[] indis, Entity[] fams, GedartTemplate usetemplate) {
        
        String thetemplate;
        String extension = null;
        if (usetemplate == null && gedartTemplatesOption.length > templateModel) {
            usetemplate = gedartTemplatesOption[templateModel];
        }
        if (usetemplate == null) {
            return null;
        }

        thetemplate = usetemplate.getPath();
        LOG.log(Level.INFO, "template:{0}", thetemplate);
        extension = usetemplate.getFormat();
        if (extension == null || extension.isEmpty()) {
            extension = "html";
        }
        // if only one item, special case
        boolean isOneEntity = ((indis.length + fams.length) <= 1);
        if (isOneEntity) {
            isTodo = false;
        }
		// Init todo tags
//		PropertyTodo.setTag(todoStart, todoStart);

        // TODO: voir mode multifile if (isOneFile){
        // create an output document
        // ask for file
        LOG.log(Level.INFO, "tofile=" + saveReportToFile);
        File file;
        if (!saveReportToFile) {
            try {
                file = File.createTempFile("Ancestris-", null);
            } catch (IOException ioe) {
                file = null;
            }
        } else {
            file = getFileFromUser(translate("output.file"), AbstractAncestrisAction.TXT_OK, true, extension);
        }

        if (file == null) {
            LOG.log(Level.INFO, "file = null");
            return null;
        }

        LOG.log(Level.INFO, "file=" + file);
        // open output stream
        try {
            mydoc = new DocReport(file, gedartTemplates, theGedcom.getEncoding());
            mydoc.put("options", this);
            mydoc.put("TEMPLATE", thetemplate);
        } catch (IOException ioe) {
            System.err.println("IO Exception!");
            ioe.printStackTrace();
            return null; // abort
        }
        
        if (includeIndi) {
            mydoc.put("INDIS", indis);
        }
        
        if (includeFam) {
            mydoc.put("FAMS", fams);
        }
        
        mydoc.put("GEDCOM", theGedcom);

        mydoc.render(thetemplate + "/index.vm");
        mydoc.close();


        long size = file.length();
        if (size > 500000) {
            if (DialogManager.OK_OPTION != DialogManager.create(translate("TITL_SizeWarning"), 
                    translate("MSG_SizeWarning", size))
                    .setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).setDialogId("report.ReportGraphicalTree").show()) {
                println("Completed but file not opened as probably too big.");
                return null;
            }
        }
        
        if (thetemplate.contains("A03_TreePretty")) {
            try {
                String resource = "oaktreeC.jpg";
                String source = "/ancestris/reports/gedart/templates/A03_TreePretty/" + resource;
                URL inputUrl = GedartTemplates.class.getResource(source);
                FileUtils.copyURLToFile(inputUrl, new File(file.getParentFile(), resource));
            } catch (IOException ex) {
                println("Failed to copy image. Error:" + ex.getMessage());
                Exceptions.printStackTrace(ex);
            }
        }

        println("Completed.");
        return file;
    }
    
}
