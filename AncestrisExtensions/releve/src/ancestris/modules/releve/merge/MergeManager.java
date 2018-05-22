 package ancestris.modules.releve.merge;

import ancestris.modules.releve.dnd.TransferableRecord;
import static ancestris.modules.releve.merge.MergeLogger.LOG;
import static ancestris.modules.releve.merge.MergeLogger.getAccept;
import static ancestris.modules.releve.merge.MergeLogger.getRefuse;
import ancestris.modules.releve.merge.MergeRecord.MergeParticipantType;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.UnitOfWork;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.swing.AbstractListModel;
import org.openide.util.NbPreferences;


/**
 *
 * @author michel
 */
public class MergeManager  {

    private final MergeRecord m_mergeRecord;
    private final Gedcom m_gedcom;
    private final Entity m_selectedEntity;
    static private boolean m_showAllParents = Boolean.parseBoolean(NbPreferences.forModule(MergeDialog.class).get("MergeDialogShowAllParents", "true"));
    private final ProposalList m_mergeModelList1= new ProposalList(false);
    private final ProposalList m_mergeModelList2= new ProposalList(true);
    protected boolean m_showFrenchCalendarDate = true;
    protected final ProposalHelper m_helper;
    static String m_cName = MergeManager.class.getName(); // pour les LOG

    public MergeManager(TransferableRecord.TransferableData transferableData, Gedcom gedcom, Entity selectedEntity)  {
        this.m_mergeRecord = new MergeRecord(transferableData);
        this.m_gedcom = gedcom;
        this.m_selectedEntity = selectedEntity;
        m_helper = new ProposalHelper(m_mergeRecord, selectedEntity, gedcom);
        m_mergeModelList1.showAllProposal(selectedEntity == null);
    }

    /**
     * cree un liste de propositions comparant le releve et
     * l'entité selectionnée dans le m_gedcom. Si m_selectedEntity = null, la liste
     * contient les modeles comparant le relevé avec les entités du m_gedcom dont
     * les noms, prenoms, et dates de naissance, mariage et décès sont
     * compatibles avec le relevé.
     *
     * @param mergeRecord releve
     * @param gedcom
     * @param selectedEntity entité sélectionnée dans le m_gedcom (peut être nul)
     */
    public void createProposals() throws Exception {
        m_mergeModelList1.clear();
        m_mergeModelList2.clear();
        if (null == m_mergeRecord.getRecordType()) {
            // je retourne une liste de modeles vide.
        } else {
            switch (m_mergeRecord.getRecordType()) {
                case BIRTH:
                    createProposalBirth();
                    break;
                case MARRIAGE:
                    createProposalMarriage();
                    break;
                case DEATH:
                    createProposalDeath();
                    break;
                case MISC:
                    switch (m_mergeRecord.getEventTypeTag()) {
                        case MARB: // Contrat de mariage ou bans de mariage
                        case MARC:
                        case MARL:
                            createProposalMiscMarc();
                            break;
                        case WILL: // Testament
                            createProposalMiscWill();
                            break;
                        default: // Autre evenement (quittance, obligation, emancipation, enregistrement, insinuation ...;
                            createProposalMiscOther();
                            break;
                    }
                    break;
                default:
                    break;
            }
        }

        // je trie les modeles par ordre décroissant du nombre de champs egaux entre le relevé et l'entité du m_gedcom
        m_mergeModelList1.sort();
        m_mergeModelList2.sort();

    }

    /**
     * model factory
     * cree un liste contenant un modele comparant le releve et l'entité
     * selectionnée dans le m_gedcom.
     * Si m_selectedEntity = null, la liste contient les modeles comparant le relevé
     * avec les entités du m_gedcom dont les noms, prenoms, et dates de naissance,
     * mariage et décès sont compatibles avec le relevé.
     * @param mergeRecord   releve
     * @param gedcom
     * @param selectedEntity entité sélectionnée dans le m_gedcom
     * @return
     */
    private void createProposalBirth () throws Exception {
        String mName = "createProposalBirth";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.entering(m_cName, mName,
                    m_mergeRecord.getIndi().getLastName()
                    + " "
                    + m_mergeRecord.getIndi().getFirstName()
            );
        }
        ProposalList proposals = m_mergeModelList1;
        MergeRecord.RecordParticipant participant = m_helper.getRecord().getIndi();
        if (m_selectedEntity instanceof Fam) {
            // 1.1) Record Birth : l'entité selectionnée est une famille
            Fam selectedFamily = (Fam) m_selectedEntity;
            // j'ajoute un nouvel individu
            //proposals.add(new Proposal(m_helper, null, selectedFamily, null, null));
            // je recherche les enfants de la famille sélectionnée compatibles avec le releve
            List<Indi> sameChildren = MergeQuery.findSameChild(m_mergeRecord, m_gedcom, selectedFamily);
            // j'ajoute les enfants compatibles
            for (Indi sameIndi : sameChildren) {
                proposals.add(new Proposal(m_helper, sameIndi, sameIndi.getFamilyWhereBiologicalChild(), null, null ) );
            }
        } else if (m_selectedEntity instanceof Indi) {
            // 1.2) Record Birth : l'entité selectionnée est un individu
            Indi selectedIndi = (Indi) m_selectedEntity;

            // je cherche les familles compatibles avec le releve de naissance
            List<Fam> families = MergeQuery.findFamilyCompatibleWithParticipantParents(m_mergeRecord, participant, m_gedcom);

            // j'ajoute l'individu selectionné par dnd
            proposals.add(new Proposal(m_helper, selectedIndi, (Fam) null, null, null) );
            // j'ajoute l'individu selectionné par dnd avec les familles compatibles
            for (Fam family : families) {
                proposals.add(new Proposal(m_helper, selectedIndi, family, null, null) );
            }

            // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
            // releve et avec les dates de naissance compatibles et les parents compatibles)
            // en excluant l'individu selectionne s'il a deja une famille
            List<Indi> sameIndis;
            if (selectedIndi.getFamilyWhereBiologicalChild() != null) {
                // l'individu est lié a une famille précise, je l'exclue de la recherche
                sameIndis = MergeQuery.findIndiCompatibleWithParticipant(m_mergeRecord, participant, m_gedcom, selectedIndi);
            } else {
                // l'individu n'est pas lié a une famille précise, je l'inclue dans la recherche
                sameIndis = MergeQuery.findIndiCompatibleWithParticipant(m_mergeRecord, participant, m_gedcom, null);
            }
            // j'ajoute les individus compatibles
            for (Indi sameIndi : sameIndis) {
                // j'ajoute les familles compatibles
                Fam sameIndiFamily = sameIndi.getFamilyWhereBiologicalChild();
                if (sameIndiFamily != null) {
                    proposals.add(new Proposal(m_helper, sameIndi, sameIndiFamily, null, null));
                } else {
                    for (Fam family : families) {
                        proposals.add(new Proposal(m_helper, sameIndi, family, null, null));
                    }
                }
            }
        }
        //} else {
            // 1.3) Record Birth : pas d'entité selectionnee

            // j'ajoute un nouvel individu , sans famille associée
            proposals.add(new Proposal(m_helper, null, null, null, null) );

            // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
            // releve et avec les dates de naissance compatibles et les parents compatibles)
            List<Indi> sameIndis = MergeQuery.findIndiCompatibleWithParticipant(m_mergeRecord, participant, m_gedcom, null);

            // je cherche les familles des parents compatibles avec le releve de naissance
            List<Fam> parentFamilies = MergeQuery.findFamilyCompatibleWithParticipantParents(m_mergeRecord, participant, m_gedcom);

            // j'ajoute un nouvel individu avec les familles compatibles
            for (Fam family : parentFamilies) {
                proposals.add(new Proposal(m_helper, null, family, null, null));
            }

            // j'ajoute les individus compatibles avec la famille de chacun
            for (Indi sameIndi : sameIndis) {
                Fam sameIndiFamily = sameIndi.getFamilyWhereBiologicalChild();
                if (sameIndiFamily != null) {
                    // j'ajoute l'individus compatible avec sa famille
                    proposals.add(new Proposal(m_helper, sameIndi, sameIndiFamily, null, null));
                } else {
                    // j'ajoute l'individus compatible sans famille
                    proposals.add(new Proposal(m_helper, sameIndi, (Fam) null, null, null));
                    if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                        LOG.log(getAccept("sameIndi %s without family",
                                sameIndi));
                    }
                    // j'ajoute l'individus compatible avec les familles compatibles
                    for (Fam parentFamily : parentFamilies) {
                        proposals.add(new Proposal(m_helper, sameIndi, parentFamily, null, null));
                    }
                }
            }

            if (m_showAllParents) {
                // j'ajoute un nouvel individu avec les couples qui ne sont pas des familles
                // mais qui pourraient être ses parents
                List<Indi> fathers = new ArrayList<Indi>();
                List<Indi> mothers = new ArrayList<Indi>();
                MergeQuery.findFatherMotherCompatibleWithBirthParticipant(m_mergeRecord, m_gedcom, parentFamilies, fathers, mothers);
                for (Indi father : fathers) {
                    for (Indi mother : mothers) {
                        proposals.add(new Proposal(m_helper, null, null, father, mother));
                    }
                }
            }
        //}

        if (LOG.isLoggable(Level.FINER)){
            LOG.exiting(m_cName, mName);
        }
    }

/**
     * model factory
 cree un liste contenant un modele comparant le releve et l'entité
 selectionnée dans le m_gedcom.
     * Si m_selectedEntity = null, la liste contient les modeles comparant le relevé
 avec les entités du m_gedcom dont les noms, prenoms, et dates de naissance,
 mariage et décès sont compatibles avec le relevé.
     * @param mergeRecord   releve
     * @param gedcom
     * @param selectedEntity entité sélectionnée dans le m_gedcom
     * @return
     */
    void createProposalDeath () throws Exception {
        String mName = "createProposalDeath";
        if (LOG.isLoggable(Level.FINER)){
            LOG.entering(m_cName, mName,
                    m_mergeRecord.getIndi().getLastName()
                    + " "
                    + m_mergeRecord.getIndi().getFirstName()
            );
        }
        ProposalList models = m_mergeModelList1;
        MergeRecord.RecordParticipant participant = m_helper.getRecord().getIndi();
        if (m_selectedEntity instanceof Fam) {
            // 1.1) Record Death : l'entité selectionnée est une famille
            Fam selectedFamily = (Fam) m_selectedEntity;
            // j'ajoute un nouvel individu
            //models.add(new Proposal(m_helper, null, selectedFamily, null, null) );
            // je recherche les enfants de la famille sélectionnée compatibles avec le releve
            List<Indi> sameChildren = MergeQuery.findSameChild(m_mergeRecord, m_gedcom, selectedFamily);
            // j'ajoute les enfants compatibles
            for (Indi sameIndi : sameChildren) {
                models.add(new Proposal(m_helper, sameIndi, sameIndi.getFamilyWhereBiologicalChild(), null, null) );
            }
            addIndiWithSpouseFamily(participant,(Indi) null, models);
        } else {
            addIndiWithSpouseFamily(participant,(Indi) m_selectedEntity, models);
        }

        if (LOG.isLoggable(Level.FINER)){
            LOG.exiting(m_cName, mName);
        }
    }

/**
     * model factory
 cree un liste contenant un modele comparant le releve et l'entité
 selectionnée dans le m_gedcom.
     * Si m_selectedEntity = null, la liste contient les modeles comparant le relevé
 avec les entités du m_gedcom dont les noms, prenoms, et dates de naissance,
 mariage et décès sont compatibles avec le relevé.
     * @param mergeRecord   releve
     * @param gedcom
     * @param selectedEntity entité sélectionnée dans le m_gedcom
     * @return
     */
    void createProposalMarriage () throws Exception {
        String mName = "createProposalMarriage";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.entering(m_cName, mName,
                    m_mergeRecord.getIndi().getLastName()
                    + " "
                    + m_mergeRecord.getIndi().getFirstName()
                    + " x "
                    + m_mergeRecord.getWife().getLastName()
                    + " "
                    + m_mergeRecord.getWife().getFirstName()
            );
        }

        ProposalList models = m_mergeModelList1;
        if (m_selectedEntity instanceof Fam) {
            // 2.1) Record Marriage : l'entité selectionnée est une famille
            Fam selectedFamily = (Fam) m_selectedEntity;

            // j'ajoute un modele avec la famille selectionne
            models.add(new Proposal(m_helper, selectedFamily, null, null, null, null,  null, null, null, null) );

        } else if (m_selectedEntity instanceof Indi) {
            // 2.2) Record Marriage : l'entité selectionnée est un individu
            Indi selectedIndi = (Indi) m_selectedEntity;

            // je cherche les familles avec l'individu selectionné
            Fam[] families = selectedIndi.getFamiliesWhereSpouse();
            // j'ajoute les familles compatibles
            for (Fam family : families) {
                models.add(new Proposal(m_helper, family, null, null, null, null,  null, null, null, null) );
            }

            if (m_showAllParents) {
                // j'ajoute les parents possibles non maries entre eux
                List<Indi> husbands = new ArrayList<Indi>();
                List<Indi> wifes = new ArrayList<Indi>();
                if (selectedIndi.getSex() == PropertySex.MALE) {
                    models.add(new Proposal(m_helper, null, selectedIndi, null, null, null,  null, null, null, null) );

                    husbands.add(selectedIndi);
                } else if (selectedIndi.getSex() == PropertySex.FEMALE) {
                    models.add(new Proposal(m_helper, null,   null, null, null, null,  selectedIndi, null, null, null));
                    wifes.add(selectedIndi);
                }
                MergeQuery.findHusbanWifeCompatibleWithMarriageRecord(m_mergeRecord, m_gedcom, Arrays.asList(families), husbands, wifes);
                for (Indi husband : husbands) {
                    for (Indi wife : wifes) {
                        //TODO  rechercher la famille de l'epoux et la famille de l'epouse et la prendre en compte si elle existe
                        models.add(new Proposal(m_helper, null, husband, null, null, null, wife, null, null, null));
                    }
                }
            }
        }//} else {
            // 2.3) Record Marriage : pas d'entité selectionnee
            // j'ajoute une nouvelle famille
            models.add(new Proposal(m_helper, null,  null, null, null, null,  null, null, null, null));

            // je recherche les familles compatibles
            List<Fam> families = MergeQuery.findFamilyCompatibleWithMarriageRecord(m_mergeRecord, m_gedcom, null);
            // j'ajoute les familles compatibles
            for (Fam family : families) {
                models.add(new Proposal(m_helper, family, null, null, null, null,  null, null, null, null ) );
            }

            // je recherche les individus compatibles avec l'epoux et l'epouse du releve
            List<Indi> husbands = new ArrayList<Indi>();
            List<Indi> wifes = new ArrayList<Indi>();
            MergeQuery.findHusbanWifeCompatibleWithMarriageRecord(m_mergeRecord, m_gedcom, families, husbands, wifes);
            for (Indi husband : husbands) {
                for (Indi wife : wifes) {
                    models.add(new Proposal(m_helper, null, husband, null, null, null, wife, null, null, null));
                }
                models.add(new Proposal(m_helper, null, husband, null, null, null, (Indi) null, null, null, null) );
            }
            for (Indi wife : wifes) {
                models.add(new Proposal(m_helper, null, (Indi) null, null, null, null, wife, null, null, null));
            }

            // je recherche les familles des parents compatibles qui ne sont pas
            // dans les modeles precedents
            if (m_showAllParents
                    || (!m_showAllParents
                    && !m_mergeRecord.getIndi().getFather().getFirstName().isEmpty()
                    && !m_mergeRecord.getIndi().getMother().getFirstName().isEmpty()
                    && !m_mergeRecord.getIndi().getMother().getLastName().isEmpty())) {

                List<Fam> husbandFamilies = new ArrayList<Fam>();
                List<Fam> wifeFamilies = new ArrayList<Fam>();

                for (Fam husbandFamily : MergeQuery.findFamilyCompatibleWithParticipantParents(m_mergeRecord, m_helper.getRecord().getIndi(),  m_gedcom)) {
                    Indi[] children = husbandFamily.getChildren();

                    boolean foundHusband = false;
                    for (Indi children1 : children) {
                        // l'enfant ne doit pas être dans husbands déjà retenus
                        if (husbands.contains(children1)) {
                            foundHusband = true;
                        }
                        // l'enfant ne doit pas être un epoux dans une famile déjà retenue
                        for (Fam family : families) {
                            if (family.getHusband() != null) {
                                if (family.getHusband().equals(children1)) {
                                    foundHusband = true;
                                }
                            }
                        }
                    }
                    if (!foundHusband) {
                        husbandFamilies.add(husbandFamily);
                    }
                }

                for (Fam wifeFamily : MergeQuery.findFamilyCompatibleWithParticipantParents(m_mergeRecord, m_helper.getRecord().getWife(), m_gedcom)) {
                    Indi[] children = wifeFamily.getChildren();

                    boolean foundWife = false;
                    for (Indi children1 : children) {
                        // l'enfant ne doit pas être dans husbands
                        if (wifes.contains(children1)) {
                            foundWife = true;
                        }
                        // l'enfant ne doit pas être un epoux dans une famile
                        for (Fam family : families) {
                            if (family.getWife() != null) {
                                if (family.getWife().equals(children1)) {
                                    foundWife = true;
                                }
                            }
                        }
                    }
                    if (!foundWife) {
                        wifeFamilies.add(wifeFamily);
                    }
                }

                for (Fam husbandFamily : husbandFamilies) {
                    for (Fam wifeFamily : wifeFamilies) {
                        models.add(new Proposal(m_helper, null, null, husbandFamily, null, null,  null, wifeFamily, null, null ));
                    }
                    models.add(new Proposal(m_helper, null, null, husbandFamily, null, null,  null, null, null, null) );
                }
                for (Fam wifeFamily : wifeFamilies) {
                    models.add(new Proposal(m_helper, null, null, null, null, null, null, wifeFamily, null, null));
                }

                // j'ajoute les combinaisons entre les epoux précedents et les familles
                for (Indi husband : husbands) {
                    for (Fam wifeFamily : wifeFamilies) {
                        models.add(new Proposal(m_helper, null, husband, null, null, null,  null, wifeFamily, null, null));
                    }
                }
                for (Indi wife : wifes) {
                    for (Fam husbandFamily : husbandFamilies) {
                        models.add(new Proposal(m_helper, null, null, husbandFamily, null, null, wife, null, null, null));
                    }
                }
            }
        //}
        if (LOG.isLoggable(Level.FINER)){
            LOG.exiting(m_cName, mName);
        }
    }

    private void createProposalMiscMarc() throws Exception {
        String mName = "createProposalMiscMarc";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.entering(m_cName, mName,
                    m_mergeRecord.getIndi().getLastName()
                    + " "
                    + m_mergeRecord.getIndi().getFirstName()
                    + " x "
                    + m_mergeRecord.getWife().getLastName()
                    + " "
                    + m_mergeRecord.getWife().getFirstName()
            );
        }


        ProposalList models = m_mergeModelList1;
        if ( m_selectedEntity instanceof Fam ) {
            // 4.1) Record Misc : l'entité selectionnée est une famille
            Fam selectedFamily = (Fam) m_selectedEntity;

            // j'ajoute un modele avec la famille selectionne
            models.add(new Proposal(m_helper, selectedFamily, null, null, null, null,  null, null, null, null));

        } else if ( m_selectedEntity instanceof Indi ) {
            // 4.2) Record Misc : l'entité selectionnée est un individu
            Indi selectedIndi = (Indi) m_selectedEntity;

            // je cherche les familles avec l'individu selectionné
            Fam[] families = selectedIndi.getFamiliesWhereSpouse();
            // j'ajoute les familles compatibles
            for(Fam family : families) {
                models.add(new Proposal(m_helper, family, null, null, null, null,  null, null, null, null));
            }

            if (m_showAllParents) {
                // j'ajoute les parents possibles non maries entre eux
                List<Indi> husbands = new ArrayList<Indi>();
                List<Indi> wifes = new ArrayList<Indi>();
                if (selectedIndi.getSex() == PropertySex.MALE) {
                    models.add(new Proposal(m_helper, null, selectedIndi, null, null, null,  null, null, null, null) );
                    husbands.add(selectedIndi);
                } else if (selectedIndi.getSex() == PropertySex.FEMALE) {
                    models.add(new Proposal(m_helper, null,  null, null, null, null,  selectedIndi, null, null, null));
                    wifes.add(selectedIndi);
                }
                MergeQuery.findHusbanWifeCompatibleWithMarriageRecord(m_mergeRecord, m_gedcom, Arrays.asList(families), husbands, wifes);
                for (Indi husband : husbands) {
                    for (Indi wife : wifes) {
                        //TODO  rechercher la famille de l'epoux et la famille de l'epouse et la prendre en compte si elle existe
                        models.add(new Proposal(m_helper, null, husband, null, null, null,  wife, null, null, null));
                    }
                }
            }
        } else {
            // 4.3) Record Misc : pas d'entité selectionnee

            // j'ajoute une nouvelle famille
            models.add(new Proposal(m_helper, null,  null, null, null, null,  null, null, null, null));

            // je recherche les familles compatibles
            List<Fam> families = MergeQuery.findFamilyCompatibleWithMarriageRecord(m_mergeRecord, m_gedcom, null);
            // j'ajoute les familles compatibles
            for(Fam family : families) {
                models.add(new Proposal(m_helper, family, null, null, null, null,  null, null, null, null));
            }

            // je recherche les individus compatibles avec l'epoux et l'epouse du releve
            List<Indi> husbands = new ArrayList<Indi>();
            List<Indi> wifes = new ArrayList<Indi>();
            MergeQuery.findHusbanWifeCompatibleWithMarriageRecord(m_mergeRecord, m_gedcom, families, husbands, wifes);
            for(Indi husband : husbands) {
                for(Indi wife : wifes) {
                    models.add(new Proposal(m_helper, null, husband, null, null, null,  wife, null, null, null));
                }
                models.add(new Proposal(m_helper, null, husband, null, null, null,  null, null, null, null));
            }
            for(Indi wife : wifes) {
                models.add(new Proposal(m_helper, null, (Indi)null, null, null, null, wife, null, null, null));
            }

            // je recherche les familles des parents compatibles qui ne sont pas
            // dans les modeles precedents
            if (m_showAllParents ||
                (m_showAllParents
                   && !m_mergeRecord.getIndi().getFather().getFirstName().isEmpty()
                   && !m_mergeRecord.getIndi().getMother().getFirstName().isEmpty()
                   && !m_mergeRecord.getIndi().getMother().getLastName().isEmpty()

                   ) ) {

                List<Fam> husbandFamilies = new ArrayList<Fam>();
                List<Fam> wifeFamilies = new ArrayList<Fam>();

                for (Fam husbandFamily : MergeQuery.findFamilyCompatibleWithParticipantParents(m_mergeRecord, m_helper.getRecord().getIndi(), m_gedcom) ) {
                    Indi[] children = husbandFamily.getChildren();

                    boolean foundHusband = false;
                    for (Indi children1 : children) {
                        // l'enfant ne doit pas être dans husbands déjà retenus
                        if (husbands.contains(children1)) {
                            foundHusband = true;
                        }
                        // l'enfant ne doit pas être un epoux dans une famile déjà retenue
                        for (Fam family : families) {
                            if (family.getHusband()!=null) {
                                if (family.getHusband().equals(children1)) {
                                    foundHusband = true;
                                }
                            }
                        }
                    }
                    if (!foundHusband ) {
                       husbandFamilies.add(husbandFamily);
                    }
                }

                for (Fam wifeFamily : MergeQuery.findFamilyCompatibleWithParticipantParents(m_mergeRecord, m_helper.getRecord().getWife(), m_gedcom)) {
                    Indi[] children = wifeFamily.getChildren();

                    boolean foundWife = false;
                    for (Indi children1 : children) {
                        // l'enfant ne doit pas être dans husbands
                        if (wifes.contains(children1)) {
                            foundWife = true;
                        }
                        // l'enfant ne doit pas être un epoux dans une famile
                        for (Fam family : families) {
                            if (family.getWife() != null) {
                                if (family.getWife().equals(children1)) {
                                    foundWife = true;
                                }
                            }
                        }
                    }
                    if (!foundWife ) {
                       wifeFamilies.add(wifeFamily);
                    }
                }

                for(Fam husbandFamily : husbandFamilies) {
                    for(Fam wifeFamily : wifeFamilies) {
                        models.add(new Proposal(m_helper, null, null, husbandFamily, null, null, null, wifeFamily, null, null ));
                    }
                    models.add(new Proposal(m_helper, null, null, husbandFamily, null, null,  null, (Fam)null , null, null));
                }
                for(Fam wifeFamily : wifeFamilies) {
                    models.add(new Proposal(m_helper, null, null, (Fam)null, null, null,  null, wifeFamily, null, null));
                }

                // j'ajoute les combinaisons entre les epoux précedents et les familles
                 for(Indi husband : husbands) {
                    for(Fam wifeFamily : wifeFamilies) {
                        models.add(new Proposal(m_helper, null, husband, null, null, null,  null, wifeFamily, null, null));
                    }
                }
                for(Indi wife : wifes) {
                    for (Fam husbandFamily : husbandFamilies) {
                        models.add(new Proposal(m_helper, null, null, husbandFamily, null, null, wife, null, null, null));
                    }
                }
            }
        }
        if (LOG.isLoggable(Level.FINER)){
            LOG.exiting(m_cName, mName);
        }
    }

    /**
     * model factory
 cree un liste contenant un modele comparant le releve et l'entité
 selectionnée dans le m_gedcom.
     * Si m_selectedEntity = null, la liste contient les modeles comparant le relevé
 avec les entités du m_gedcom dont les noms, prenoms, et dates de naissance,
 mariage et décès sont compatibles avec le relevé.
     * @param mergeRecord   releve
     * @param gedcom
     * @param selectedEntity entité sélectionnée dans le m_gedcom
     * @return
     */
    private void createProposalMiscOther () throws Exception {
        String mName = "createProposalMiscOther";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.entering(m_cName, mName,
                    "Participant 1 "
                    + m_mergeRecord.getIndi().getLastName()
                    + " "
                    + m_mergeRecord.getIndi().getFirstName()
            );
        }

        ProposalList models = m_mergeModelList1;

        // je recherche les propositions concernant le participant 1
        if (m_selectedEntity instanceof Fam) {
            // 1) l'entité selectionnée est une famille pour le participant 1
            MergeRecord.RecordParticipant participant = m_helper.getRecord().getIndi();
            SpouseTag tag = participant.getSex() == PropertySex.MALE ? SpouseTag.HUSB : SpouseTag.WIFE;
            Fam selectedParentFamily = (Fam) m_selectedEntity;
            // j'ajoute un nouvel individu
            //models.add(new Proposal(m_helper, participant, null, null, tag, selectedParentFamily, null, null));

            // je recherche les enfants de la famille sélectionnée compatibles avec le releve
            List<Indi> sameChildren = MergeQuery.findSameChild(m_mergeRecord, m_gedcom, selectedParentFamily);
            // j'ajoute les enfants compatibles
            for (Indi samedIndi : sameChildren) {
                models.add(new Proposal(m_helper, participant, samedIndi, null, tag, samedIndi.getFamilyWhereBiologicalChild(), null, null ));
            }
            addIndiWithSpouseFamily(participant,(Indi) null, models);
        } else {
            MergeRecord.RecordParticipant participant = m_helper.getRecord().getParticipant(MergeParticipantType.participant1);
            addIndiWithSpouseFamily(participant,(Indi) m_selectedEntity, models);
        }


        ProposalList models2 = m_mergeModelList2;

        if (LOG.isLoggable(Level.FINER)) {
            LOG.logp(Level.FINER, m_cName, mName,
                    "Participant 2 "
                    + m_mergeRecord.getWife().getLastName()
                    + " "
                    + m_mergeRecord.getWife().getFirstName()
            );
        }

        // 4)  pas d'entité selectionnee pour le participant 2
        // je recherche les propositions concernant le participant 2
        if (!m_mergeRecord.getWife().getLastName().isEmpty() || !m_mergeRecord.getWife().getFirstName().isEmpty()) {
            MergeRecord.RecordParticipant participant2 = m_helper.getRecord().getParticipant(MergeParticipantType.participant2);
            addIndiWithSpouseFamily(participant2, null, models2);
        }

        if (LOG.isLoggable(Level.FINER)){
            LOG.exiting(m_cName, mName);
        }
    }


    /**
     * model factory
 cree un liste contenant un modele comparant le releve et l'entité
 selectionnée dans le m_gedcom.
     * Si m_selectedEntity = null, la liste contient les modeles comparant le relevé
 avec les entités du m_gedcom dont les noms, prenoms, et dates de naissance,
 mariage et décès sont compatibles avec le relevé.
     * @param mergeRecord   releve
     * @param gedcom
     * @param selectedEntity entité sélectionnée dans le m_gedcom
     * @return
     */
    private void createProposalMiscWill () throws Exception {
        String mName = "createProposalMiscWill";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.entering(m_cName, mName,
                    m_mergeRecord.getIndi().getLastName()
                    + " "
                    + m_mergeRecord.getIndi().getFirstName()
                    + " x "
                    + m_mergeRecord.getWife().getLastName()
                    + " "
                    + m_mergeRecord.getWife().getFirstName()
            );
        }

        ProposalList models = m_mergeModelList1;
        MergeRecord.RecordParticipant participant = m_helper.getRecord().getIndi();

        if (m_selectedEntity instanceof Fam) {
            // 1) Record Will : l'entité selectionnée est une famille
            Fam selectedParentFamily = (Fam) m_selectedEntity;
            SpouseTag tag = participant.getSex() == PropertySex.MALE ? SpouseTag.HUSB : SpouseTag.WIFE;
            // j'ajoute un nouvel individu
            //models.add(new Proposal(m_helper, null, null, tag, selectedParentFamily, null, null));

            // je recherche les enfants de la famille sélectionnée compatibles avec le releve
            List<Indi> sameChildren = MergeQuery.findSameChild(m_mergeRecord, m_gedcom, selectedParentFamily);
            // j'ajoute les enfants compatibles
            for (Indi samedIndi : sameChildren) {
                models.add(new Proposal(m_helper, samedIndi, null, tag, samedIndi.getFamilyWhereBiologicalChild(), null, null));
            }
            addIndiWithSpouseFamily(participant,(Indi) null, models);
        } else {
            addIndiWithSpouseFamily(participant,(Indi) m_selectedEntity, models);
        }


        if (LOG.isLoggable(Level.FINER)){
            LOG.exiting(m_cName, mName);
        }
    }

    void addIndiWithSpouseFamily(MergeRecord.RecordParticipant participant, Indi selectedIndi, ProposalList models) throws Exception {
        String mName = "addIndiWithSpouseFamily";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.entering(m_cName, mName,
                    "participantType="+participant.getParticipantType()
                    + " selectedIndi="+ (selectedIndi==null ? "null" : selectedIndi.getId() )
            );
        }

        // je recherche la famille avec l'ex conjoint
        List<SpouseFamily> marriedFamilies = MergeQuery.findFamilyCompatibleWithParticipantMarried(m_mergeRecord, participant, m_gedcom);

        // je cherche les familles des parents compatibles avec le releve
        List<Fam> parentFamilies = MergeQuery.findFamilyCompatibleWithParticipantParents(m_mergeRecord, participant, m_gedcom);

        if (selectedIndi != null) {
            if (!marriedFamilies.isEmpty()) {
                for (SpouseFamily marriedFamily : marriedFamilies) {
                    Indi indi;
                    if (marriedFamily.tag == SpouseTag.HUSB) {
                        indi = marriedFamily.family.getHusband();
                    } else {
                        indi = marriedFamily.family.getWife();
                    }

                    if (selectedIndi.compareTo(indi) == 0) {
                        Fam husbandParentFamily = indi.getFamilyWhereBiologicalChild();
                        models.add(new Proposal(m_helper, participant, indi, marriedFamily.family, marriedFamily.tag, husbandParentFamily, null, null));
                        if (husbandParentFamily == null) {
                            // j'ajoute un nouvel individu avec les familles compatibles
                            for (Fam parentFamily : parentFamilies) {
                                models.add(new Proposal(m_helper, participant, indi, marriedFamily.family, marriedFamily.tag, parentFamily, null, null));
                            }
                        }
                    } else {
                        if (LOG.isLoggable(MergeLogger.REFUSE)) {
                            LOG.log(getRefuse("husband %s different from selectedIndi %s",
                                    indi, selectedIndi));
                        }
                    }

                    Fam husbandParentFamily = indi.getFamilyWhereBiologicalChild();
                    if (husbandParentFamily != null) {
                        models.add(new Proposal(m_helper, participant, indi, marriedFamily.family, marriedFamily.tag, husbandParentFamily, null, null));
                    } else {
                        models.add(new Proposal(m_helper, participant, indi, marriedFamily.family, marriedFamily.tag, null, null, null));
                        // j'ajoute un nouvel individu avec les familles compatibles
                        for (Fam parentFamily : parentFamilies) {
                            models.add(new Proposal(m_helper, participant, indi, marriedFamily.family, marriedFamily.tag, parentFamily, null, null));
                        }
                    }

                }
            } else {
                // l'individu n'a pas d'ex conjoint

                // j'ajoute l'individu selectionné par dnd
                if (selectedIndi.getFamilyWhereBiologicalChild() != null) {
                    // j'ajoute l'individu selectionné par dnd
                    SpouseTag tag = participant.getSex() == PropertySex.MALE ? SpouseTag.HUSB : SpouseTag.WIFE;
                    models.add(new Proposal(m_helper, participant, selectedIndi, null, tag, selectedIndi.getFamilyWhereBiologicalChild(), null, null));
                } else {
                    // je cherche les familles avec selectedIndi
                    Fam[] selectedIndiFamilies = selectedIndi.getFamiliesWhereSpouse();
                    for (Fam fam : selectedIndiFamilies) {
                        // je verifie si le mariage de selectedIndi est avant la date de deces
                        if (MergeQuery.isRecordBeforeThanDate(fam.getMarriageDate(), m_mergeRecord.getIndi().getDeathDate(), 0, 0)) {
                            SpouseTag tag = selectedIndi == fam.getHusband() ? SpouseTag.HUSB : SpouseTag.WIFE;
                            models.add(new Proposal(m_helper, participant, selectedIndi, fam, tag, null, null, null));

                            //  j'ajoute un modéle avec une nouvelle famille si le nom de l'epoux est différent entre le relevé et la famille trouvée
                            if (!m_mergeRecord.getIndi().getMarriedFamily().getMarried().getLastName().isEmpty() || !m_mergeRecord.getIndi().getMarriedFamily().getMarried().getFirstName().isEmpty()) {
                                if (selectedIndi.equals(fam.getHusband())) {
                                    if (!(MergeQuery.isSameLastName(fam.getWife().getLastName(), m_mergeRecord.getIndi().getMarriedFamily().getMarried().getLastName())
                                            && MergeQuery.isSameFirstName(fam.getWife().getFirstName(), m_mergeRecord.getIndi().getMarriedFamily().getMarried().getFirstName()))) {
                                        tag = participant.getSex() == PropertySex.MALE ? SpouseTag.HUSB : SpouseTag.WIFE;
                                        models.add(new Proposal(m_helper, participant, selectedIndi, null, tag, (Fam) null, null, null));
                                    }

                                } else {
                                    if (!(MergeQuery.isSameLastName(fam.getHusband().getLastName(), m_mergeRecord.getIndi().getMarriedFamily().getMarried().getLastName())
                                            && MergeQuery.isSameFirstName(fam.getHusband().getFirstName(), m_mergeRecord.getIndi().getMarriedFamily().getMarried().getFirstName()))) {
                                        tag = participant.getSex() == PropertySex.MALE ? SpouseTag.HUSB : SpouseTag.WIFE;
                                        models.add(new Proposal(m_helper, participant, selectedIndi, null, tag, (Fam) null, null, null));
                                    }

                                }
                            }
                        } else {
                            if (LOG.isLoggable(MergeLogger.REFUSE)) {
                                LOG.log(getRefuse("selectedIndi %s marriage %s date %s must be before deathDate %s",
                                        selectedIndi, fam, fam.getMarriageDate().getValue(), m_mergeRecord.getIndi().getDeathDate().getValue()));
                            }
                        }
                    }

                    // j'ajoute l'individu selectionné par dnd avec les familles compatibles
                    for (Fam parentFamily : parentFamilies) {
                        SpouseTag tag = participant.getSex() == PropertySex.MALE ? SpouseTag.HUSB : SpouseTag.WIFE;
                        models.add(new Proposal(m_helper, participant, selectedIndi, null, tag, parentFamily, null, null));

                    }
                }

                // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
                // releve et avec les dates de naissance compatibles et les parents compatibles)
                // en excluant l'individu selectionne s'il a deja une famille
                List<Indi> sameIndis;
                if (selectedIndi.getFamilyWhereBiologicalChild() != null) {
                    // l'individu est lié a une famille précise, je l'exclue de la recherche
                    sameIndis = MergeQuery.findIndiCompatibleWithParticipant(m_mergeRecord, participant, m_gedcom, selectedIndi);
                } else {
                    // l'individu n'est pas lié a une famille précise, je l'inclue dans la recherche
                    sameIndis = MergeQuery.findIndiCompatibleWithParticipant(m_mergeRecord, participant, m_gedcom, null);
                }
                // j'ajoute les individus compatibles
                for (Indi sameIndi : sameIndis) {
                    // j'ajoute les familles compatibles
                    Fam sameIndiParentFamily = sameIndi.getFamilyWhereBiologicalChild();
                    SpouseTag tag = participant.getSex() == PropertySex.MALE ? SpouseTag.HUSB : SpouseTag.WIFE;
                    if (sameIndiParentFamily != null) {
                        models.add(new Proposal(m_helper, participant, sameIndi, null, tag, sameIndiParentFamily, null, null));
                    } else {
                        if (parentFamilies.size() > 0) {
                            for (Fam parentFamily : parentFamilies) {
                                models.add(new Proposal(m_helper, participant, sameIndi, null, tag, parentFamily, null, null));
                            }
                        } else {
                            // j'ajoute l'individu selectionné sans famille
                            models.add(new Proposal(m_helper, participant, selectedIndi, null, tag, (Fam) null, null, null));

                        }
                    }
                }
            }
        } else {
            //  pas d'entité selectionnee

            // j'ajoute un nouvel individu , sans famille associée
            SpouseTag tag = participant.getSex() == PropertySex.MALE ? SpouseTag.HUSB : SpouseTag.WIFE;
            models.add(new Proposal(m_helper, participant, null, null, tag, null, null, null));

            if (!marriedFamilies.isEmpty()) {
                for (SpouseFamily marriedFamily : marriedFamilies) {
                    Indi indi;
                    if (marriedFamily.tag == SpouseTag.HUSB) {
                        indi = marriedFamily.family.getHusband();
                    } else {
                        indi = marriedFamily.family.getWife();
                    }
                    Fam husbandParentFamily = indi.getFamilyWhereBiologicalChild();
                    if (husbandParentFamily != null) {
                        models.add(new Proposal(m_helper, participant, indi, marriedFamily.family, marriedFamily.tag, husbandParentFamily, null, null));
                    } else {
                        models.add(new Proposal(m_helper, participant, indi, marriedFamily.family, marriedFamily.tag, null, null, null));
                        // j'ajoute un nouvel individu avec les familles compatibles
                        for (Fam parentFamily : parentFamilies) {
                            models.add(new Proposal(m_helper, participant, indi, marriedFamily.family, marriedFamily.tag, parentFamily, null, null));
                        }
                    }
                }

            } else {
                // il n'y a pas de famille pour de l'ex conjoint

                // j'ajoute un nouvel individu avec les familles parent compatibles
                for (Fam parentFamily : parentFamilies) {
                    models.add(new Proposal(m_helper, participant, null, null, tag, parentFamily, null, null));
                }

                // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
                // releve et avec les dates de naissance compatibles et les parents compatibles)
                List<Indi> sameIndis = MergeQuery.findIndiCompatibleWithParticipant(m_mergeRecord, participant, m_gedcom, null);

                // j'ajoute les individus compatibles avec la famille de chacun
                for (Indi sameIndi : sameIndis) {
                    Fam sameIndiParentFamily = sameIndi.getFamilyWhereBiologicalChild();
                    if (sameIndiParentFamily != null) {
                        // j'ajoute l'individus compatible avec sa famille
                        models.add(new Proposal(m_helper, participant, sameIndi, null, tag, sameIndiParentFamily, null, null));

                    } else {
                        // j'ajoute l'individus compatible sans famille
                        models.add(new Proposal(m_helper, participant, sameIndi, null, tag, null, null, null));
                        // j'ajoute l'individus compatible avec les familles compatibles
                        for (Fam parentFamily : parentFamilies) {
                            models.add(new Proposal(m_helper, participant, sameIndi, null, tag, parentFamily, null, null));
                        }
                    }
                }
            }

            if (m_showAllParents) {
                // j'ajoute un nouvel individu avec les couples qui ne forment pas des familles
                // mais qui pourraient être ses parents
                List<Indi> fathers = new ArrayList<Indi>();
                List<Indi> mothers = new ArrayList<Indi>();
                MergeQuery.findFatherMotherCompatibleWithBirthParticipant(m_mergeRecord, m_gedcom, parentFamilies, fathers, mothers);
                for (Indi father : fathers) {
                    for (Indi mother : mothers) {
                        models.add(new Proposal(m_helper, participant, null, null, tag, null, father, mother));
                    }
                }
            }
        }

        if (LOG.isLoggable(Level.FINER)){
            LOG.exiting(m_cName, mName);
        }

    }

    void copyRecordToEntity(final Proposal currentModel1, final Proposal currentModel2 ) throws Exception {

        getGedcom().doUnitOfWork(new UnitOfWork() {
            @Override
            public void perform(Gedcom gedcom) {
                try {
                    Property associatedProperty1;
                    currentModel1.copyRecordToEntity();
                    associatedProperty1 = currentModel1.getMainEvent();
                    if (currentModel2 != null ) {
                        currentModel2.copyRecordToEntity();
                        Entity associatedProperty2 = currentModel2.getMainEntity();
                        m_helper.copyAssociation(associatedProperty1, associatedProperty2);
                    }

                    // j'affiche l'entité principale dans l'arbre
                    Property propertyNote = associatedProperty1.getProperty("NOTE");
                    if (propertyNote != null) {
                        // je selectionne la note si elle existe dans la propriété
                        associatedProperty1 = propertyNote;
                    }

                    // j'affiche l'entité principale dans l'arbre
                    SelectionManager.setRootEntity(associatedProperty1);

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


    }

    ///////////////////////////////////////////////////////////////////////////
    // accessors
    ///////////////////////////////////////////////////////////////////////////
    protected Gedcom getGedcom() {
        return m_gedcom;
    }

    protected MergeRecord getMergeRecord() {
        return m_mergeRecord;
    }

    protected MergeRecord.RecordType getMergeRecordType() {
        return m_mergeRecord.getRecordType();
    }

    protected Entity getSelectedEntity() {
        return m_selectedEntity;
    }

    static boolean getShowAllParents() {
        return m_showAllParents;
    }

    ProposalList getProposalList1() {
        return m_mergeModelList1;
    }

    ProposalList getProposalList2() {
        return m_mergeModelList2;
    }

    /**
     * memorise le parametre et recalcule les propositions
     * @param aShowAllParents
     * @throws Exception
     */
    void setShowAllParents(boolean aShowAllParents) throws Exception {
        m_showAllParents = aShowAllParents;
        createProposals();
    }


    /**
     * active les logs et genere les propositions
     * @throws Exception
     */
    protected void generateLog() throws Exception {
        MergeLogger.enable();
        createProposals();
        MergeLogger.disable();
    }


    ///////////////////////////////////////////////////////////////////////////
    // class ProposalList
    ///////////////////////////////////////////////////////////////////////////

    public static class ProposalList extends AbstractListModel<Proposal>  {

        private final List<Proposal> m_list = new ArrayList<Proposal>();
        private final ArrayList<Integer> m_indices = new ArrayList<Integer>();
        private boolean m_showAllProposal;

        public ProposalList(boolean showAllProposal) {
            m_showAllProposal = showAllProposal;
        }

        protected void add(Proposal proposal) {

            // je verifie s'il n'est pa s deja present
            boolean alreadyExists = false;
            for (Proposal model : m_list) {
                if ( proposal.equalAs(model) ) {
                    alreadyExists = true;
                    break;
                }
            }
            if (!alreadyExists) {
                m_list.add(proposal);

                if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                    LOG.log(MergeLogger.getAccept( proposal.getMergeInfo() ) );
                }
            } else {
                if (LOG.isLoggable(MergeLogger.REFUSE)) {
                    MergeInfo loginfo = proposal.getMergeInfo();
                    loginfo.add(" already exists in models");
                    LOG.log(MergeLogger.getRefuse( loginfo ) );
                }
            }
        }

        private void clear() {
            m_list.clear();
            m_indices.clear();
        }

        private void sort() {
            Collections.sort( m_list);

            int count = m_list.size();
            for (int i = 0; i < count; i++) {
                Proposal element = m_list.get(i);
                if (element.isSelectedEntityProposed()) {
                    m_indices.add(i);
                }
            }
        }

        protected void fireContentsChanged() {
            fireContentsChanged(this, 0, getSize() -1);
        }

        public void showAllProposal( boolean showAllProposal) {
            if( showAllProposal != m_showAllProposal) {
                int size = getSize();
                m_showAllProposal = showAllProposal;
                fireContentsChanged(this, 0, size - 1);
            }
        }

        int getNbAllProposal() {
            return m_list.size();
        }

        boolean containsSelectedEntity(int index) {
            if( m_showAllProposal ) {
                return m_indices.contains(index);
            } else {
                return true;
            }
        }

        int getNbProposalWithSelectedEntity() {
            return m_indices.size();
        }

        int getNbBestProposal() {
            if (m_indices.size() > 0) {
                // le nombre de meilleures propositions correspond à l'index du premier element dans m_indices
                // exemple :  m_indices.get(0) = 2 signifie qu'il y a 2 meilleures propositions dans m_list
                return m_indices.get(0);
            }
            return 0;
        }

        ///////////////////////////////////////////////////////////////////////////
        // Implements  AbstractListModel<Proposal>
        ///////////////////////////////////////////////////////////////////////////

        @Override
        public int getSize() {
            return (m_showAllProposal == false) ? m_indices.size() : m_list.size();
        }

        @Override
        public Proposal getElementAt(int index) {
            return (m_showAllProposal == false) ? m_list.get(m_indices.get(index)) : m_list.get(index);
        }
    }
}
